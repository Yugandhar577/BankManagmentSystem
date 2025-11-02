package com.bank.dao;

import com.bank.model.Transaction;
import com.bank.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao {

    /**
     * Logs a new transaction. This method is designed to be called
     * *within* another DAO's database transaction.
     * @param conn The existing database connection.
     * @param accountId The primary account involved.
     * @param type "DEPOSIT", "WITHDRAWAL", "TRANSFER"
     * @param amount The amount (use negative for withdrawals/transfers out)
     * @param sourceId Source account ID (nullable)
     * @param destId Destination account ID (nullable)
     * @param description A description of the transaction.
     */
    public boolean createTransaction(Connection conn, int accountId, String type, BigDecimal amount, 
                                     Integer sourceId, Integer destId, String description) throws SQLException {
        
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, " +
                     "source_account_id, destination_account_id, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        // We do *not* close the connection here, as it's managed by the calling method.
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setString(2, type);
            ps.setBigDecimal(3, amount);
            
            // Handle nullable foreign keys
            if (sourceId != null) ps.setInt(4, sourceId);
            else ps.setNull(4, java.sql.Types.INTEGER);
            
            if (destId != null) ps.setInt(5, destId);
            else ps.setNull(5, java.sql.Types.INTEGER);
            
            ps.setString(6, description);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
        // Let SQLException propagate up to be handled by the calling method's rollback logic
    }

    /**
     * Retrieves all transactions for a specific account.
     */
    public List<Transaction> getTransactionsByAccountId(int accountId) {
        List<Transaction> transactions = new ArrayList<>();
        // Order by date descending to show newest first
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    /**
     * (Admin) Retrieves all transactions in the system.
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction();
        tx.setTransactionId(rs.getInt("transaction_id"));
        tx.setAccountId(rs.getInt("account_id"));
        tx.setTransactionType(rs.getString("transaction_type"));
        tx.setAmount(rs.getBigDecimal("amount"));
        tx.setSourceAccountId((Integer) rs.getObject("source_account_id"));
        tx.setDestinationAccountId((Integer) rs.getObject("destination_account_id"));
        tx.setDescription(rs.getString("description"));
        tx.setTransactionDate(rs.getTimestamp("transaction_date"));
        return tx;
    }
}