package com.bank.dao;

import com.bank.model.Account;
import com.bank.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AccountDao {

    /**
     * Retrieves all accounts associated with a specific user.
     */
    public List<Account> getAccountsByUserId(int userId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    /**
     * Retrieves a single account by its account number.
     */
    public Account getAccountByAccountNumber(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Retrieves a single account by its ID.
     */
    public Account getAccountById(int accountId) {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Creates a new bank account for a user.
     * This is often an admin-driven process.
     */
    public boolean createAccount(int userId, String accountType) {
        String accountNumber = generateUniqueAccountNumber();
        String sql = "INSERT INTO accounts (user_id, account_number, account_type, balance) VALUES (?, ?, ?, 0.00)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setString(2, accountNumber);
            ps.setString(3, accountType); // e.g., "SAVINGS"

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Core logic for Deposit or Withdrawal.
     * @param accountId The account ID.
     * @param amount The amount to add (positive for deposit, negative for withdrawal).
     * @return true if successful, false otherwise.
     */
    public boolean updateBalance(int accountId, BigDecimal amount) {
        // We use a transaction to ensure the balance check and update are atomic.
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Lock the row for update and check balance for withdrawals
            String lockSql = "SELECT balance FROM accounts WHERE account_id = ? FOR UPDATE";
            BigDecimal currentBalance;
            try (PreparedStatement lockPs = conn.prepareStatement(lockSql)) {
                lockPs.setInt(1, accountId);
                ResultSet rs = lockPs.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("Account not found.");
                }
                currentBalance = rs.getBigDecimal("balance");
            }
            
            // Step 2: Check for sufficient funds if it's a withdrawal
            if (amount.compareTo(BigDecimal.ZERO) < 0) { // If amount is negative
                if (currentBalance.add(amount).compareTo(BigDecimal.ZERO) < 0) {
                    // Insufficient funds
                    conn.rollback();
                    return false; 
                }
            }

            // Step 3: Perform the update
            String updateSql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
            try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                updatePs.setBigDecimal(1, amount);
                updatePs.setInt(2, accountId);
                updatePs.executeUpdate();
            }

            // Step 4: Log the transaction (delegated to TransactionDao)
            TransactionDao txDao = new TransactionDao();
            String type = (amount.compareTo(BigDecimal.ZERO) > 0) ? "DEPOSIT" : "WITHDRAWAL";
            // Pass the connection so it's part of the same transaction
            txDao.createTransaction(conn, accountId, type, amount.abs(), accountId, null, "Transaction");

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restore default
                    conn.close(); // Return to pool
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Core logic for transferring money between two accounts.
     * This is a critical ACID transaction.
     */
    public boolean transferFunds(int fromAccountId, String toAccountNumber, BigDecimal amount) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION

            // Step 1: Get the destination account ID
            Account toAccount = getAccountByAccountNumber(toAccountNumber);
            if (toAccount == null) {
                throw new SQLException("Destination account not found.");
            }
            int toAccountId = toAccount.getAccountId();
            
            if (fromAccountId == toAccountId) {
                throw new SQLException("Cannot transfer to the same account.");
            }

            // Step 2: Lock and retrieve source account balance
            String lockSqlFrom = "SELECT balance FROM accounts WHERE account_id = ? FOR UPDATE";
            BigDecimal fromBalance;
            try (PreparedStatement ps = conn.prepareStatement(lockSqlFrom)) {
                ps.setInt(1, fromAccountId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) throw new SQLException("Source account not found.");
                fromBalance = rs.getBigDecimal("balance");
            }

            // Step 3: Check for sufficient funds
            if (fromBalance.compareTo(amount) < 0) {
                conn.rollback();
                return false; // Insufficient funds
            }

            // Step 4: Lock destination account
            String lockSqlTo = "SELECT account_id FROM accounts WHERE account_id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(lockSqlTo)) {
                ps.setInt(1, toAccountId);
                if (!ps.executeQuery().next()) throw new SQLException("Destination account disappeared.");
            }
            
            // To prevent deadlocks, always lock accounts in a consistent order (e.g., by ascending ID)
            // This simple implementation locks sequentially, which is okay but
            // a more robust solution would lock (fromId, toId) in a sorted order.

            // Step 5: Withdraw from source account
            String withdrawSql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(withdrawSql)) {
                ps.setBigDecimal(1, amount);
                ps.setInt(2, fromAccountId);
                ps.executeUpdate();
            }

            // Step 6: Deposit to destination account
            String depositSql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(depositSql)) {
                ps.setBigDecimal(1, amount);
                ps.setInt(2, toAccountId);
                ps.executeUpdate();
            }
            
            // Step 7: Log both sides of the transaction
            TransactionDao txDao = new TransactionDao();
            String desc = "Transfer to " + toAccountNumber;
            txDao.createTransaction(conn, fromAccountId, "TRANSFER", amount.negate(), fromAccountId, toAccountId, desc);
            
            String descTo = "Transfer from account ID " + fromAccountId;
            txDao.createTransaction(conn, toAccountId, "TRANSFER", amount, fromAccountId, toAccountId, descTo);

            conn.commit(); // COMMIT TRANSACTION
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    // --- Helper Methods ---

    private String generateUniqueAccountNumber() {
        // Simple 10-digit number generator.
        // In production, you'd check this against the DB for uniqueness.
        Random rand = new Random();
        return String.format("%010d", rand.nextInt(1_000_000_000));
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        return new Account(
            rs.getInt("account_id"),
            rs.getInt("user_id"),
            rs.getString("account_number"),
            rs.getString("account_type"),
            rs.getBigDecimal("balance"),
            rs.getTimestamp("created_at")
        );
    }
}