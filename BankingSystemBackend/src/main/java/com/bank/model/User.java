// src/main/java/com/bank/model/User.java
package com.bank.model;

// This class is a "Java Bean" or "POJO" (Plain Old Java Object).
// It's used to pass user data between the DAO and Servlets.
public class User {
    private int userId;
    private String username;
    // We intentionally exclude passwordHash from this model
    // to avoid ever sending it to the frontend.
    private String email;
    private String firstName;
    private String lastName;
    private String role;     // "CUSTOMER" or "ADMIN"
    private String status;   // "PENDING", "ACTIVE", "DEACTIVATED"

    // Default constructor
    public User() {}

    // Full constructor
    public User(int userId, String username, String email, String firstName, String lastName, String role, String status) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.status = status;
    }

    // --- Getters and Setters ---

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}