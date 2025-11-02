package com.bank.dao;

import com.bank.model.User;
import com.bank.util.DatabaseConnection;
import com.bank.util.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    /**
     * Finds a user by username.
     * @param username The username to search for.
     * @return A User object, or null if not found.
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        // try-with-resources ensures Connection and PreparedStatement are closed
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Use a proper logger in production
        }
        return null;
    }

    /**
     * Retrieves only the password hash for a user.
     * This is more secure for login validation.
     */
    public String getPasswordHashByUsername(String username) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password_hash");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a new user in the database (for registration).
     * @return true if creation was successful, false otherwise.
     */
    public boolean createUser(String username, String plainPassword, String email, String firstName, String lastName) {
        String hashedPassword = PasswordUtils.hashPassword(plainPassword);
        String sql = "INSERT INTO users (username, password_hash, email, first_name, last_name, role, status) " +
                     "VALUES (?, ?, ?, ?, ?, 'CUSTOMER', 'PENDING')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.setString(3, email);
            ps.setString(4, firstName);
            ps.setString(5, lastName);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            // e.g., unique constraint violation (username/email exists)
            e.printStackTrace();
            return false;
        }
    }

    /**
     * (Admin) Retrieves a list of all users.
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    /**
     * (Admin) Updates a user's status.
     */
    public boolean updateUserStatus(int userId, String status) {
        // Validate status to prevent SQL injection or bad data
        if (!"ACTIVE".equals(status) && !"DEACTIVATED".equals(status) && !"PENDING".equals(status)) {
            return false;
        }
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setInt(2, userId);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Helper method to map a ResultSet row to a User object.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("role"),
            rs.getString("status")
        );
    }
}