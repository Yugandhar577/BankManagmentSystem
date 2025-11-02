package com.bank.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Account {
    private int accountId;
    private int userId;
    private String accountNumber;
    private String accountType; // "SAVINGS", "CHECKING"
    private BigDecimal balance;
    private Timestamp createdAt;

    // Default constructor
    public Account() {}

    // Full constructor
    public Account(int accountId, int userId, String accountNumber, String accountType, BigDecimal balance, Timestamp createdAt) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    // --- Getters and Setters ---

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}