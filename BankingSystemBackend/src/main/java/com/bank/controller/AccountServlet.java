package com.bank.controller;

import com.bank.dao.AccountDao;
import com.bank.model.Account;
import com.bank.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// Mapped in web.xml to /api/accounts/*
public class AccountServlet extends BaseServlet {

    private final AccountDao accountDao = new AccountDao();

    /**
     * Authorization Check: Validates user session for every request.
     * @return The authenticated User object, or null if not authenticated.
     */
    private User checkAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to perform this action.");
            return null;
        }
        return (User) session.getAttribute("user");
    }

    /**
     * Handles GET requests: /api/accounts/
     * Retrieves all accounts for the currently logged-in user.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = checkAuth(req, resp);
        if (user == null) return; // Auth failed

        try {
            List<Account> accounts = accountDao.getAccountsByUserId(user.getUserId());
            sendJsonResponse(resp, accounts);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve accounts.");
        }
    }

    /**
     * Handles POST requests: /api/accounts/deposit, /withdraw, /transfer
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = checkAuth(req, resp);
        if (user == null) return; // Auth failed

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint.");
            return;
        }
        
        try {
            Map<String, String> payload = readPayload(req, Map.class);
            switch (pathInfo) {
                case "/deposit":
                    handleDeposit(req, resp, user, payload);
                    break;
                case "/withdraw":
                    handleWithdraw(req, resp, user, payload);
                    break;
                case "/transfer":
                    handleTransfer(req, resp, user, payload);
                    break;
                default:
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Not Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while processing your request.");
        }
    }

    // --- Private Handler Methods ---

    private void handleDeposit(HttpServletRequest req, HttpServletResponse resp, User user, Map<String, String> payload) throws IOException {
        try {
            int accountId = Integer.parseInt(payload.get("accountId"));
            BigDecimal amount = new BigDecimal(payload.get("amount"));

            // Validation
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Deposit amount must be positive.");
                return;
            }
            
            // Security Check: Does this account belong to the logged-in user?
            if (!isAccountOwner(user, accountId)) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "You do not own this account.");
                return;
            }
            
            boolean success = accountDao.updateBalance(accountId, amount);
            if (success) {
                sendJsonResponse(resp, Map.of("message", "Deposit successful."));
            } else {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Deposit failed.");
            }
            
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid account ID or amount.");
        }
    }

    private void handleWithdraw(HttpServletRequest req, HttpServletResponse resp, User user, Map<String, String> payload) throws IOException {
         try {
            int accountId = Integer.parseInt(payload.get("accountId"));
            BigDecimal amount = new BigDecimal(payload.get("amount"));

            // Validation
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Withdrawal amount must be positive.");
                return;
            }
            
            // Security Check
            if (!isAccountOwner(user, accountId)) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "You do not own this account.");
                return;
            }
            
            // We pass a negative amount to the updateBalance method
            boolean success = accountDao.updateBalance(accountId, amount.negate());
            
            if (success) {
                sendJsonResponse(resp, Map.of("message", "Withdrawal successful."));
            } else {
                // This typically means insufficient funds
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Withdrawal failed. Check for sufficient funds.");
            }
            
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid account ID or amount.");
        }
    }

    private void handleTransfer(HttpServletRequest req, HttpServletResponse resp, User user, Map<String, String> payload) throws IOException {
        try {
            int fromAccountId = Integer.parseInt(payload.get("fromAccountId"));
            String toAccountNumber = payload.get("toAccountNumber");
            BigDecimal amount = new BigDecimal(payload.get("amount"));

            // Validation
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Transfer amount must be positive.");
                return;
            }
            if (toAccountNumber == null || toAccountNumber.isBlank()) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Destination account number is required.");
                return;
            }
            
            // Security Check
            if (!isAccountOwner(user, fromAccountId)) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "You do not own the source account.");
                return;
            }
            
            boolean success = accountDao.transferFunds(fromAccountId, toAccountNumber, amount);
            
            if (success) {
                sendJsonResponse(resp, Map.of("message", "Transfer successful."));
            } else {
                // This can be insufficient funds, or "account not found"
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Transfer failed. Check funds or destination account number.");
            }

        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid account ID or amount.");
        } catch (Exception e) {
            // Catch errors from the DAO (e.g., "account not found")
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
    
    /**
     * Security helper to verify a user owns an account.
     * Prevents a user from highjacking another user's account ID.
     */
    private boolean isAccountOwner(User user, int accountId) {
        // Admins can do anything
        if ("ADMIN".equals(user.getRole())) {
            return true;
        }
        
        // Check for customer ownership
        Account account = accountDao.getAccountById(accountId);
        return account != null && account.getUserId() == user.getUserId();
    }
}