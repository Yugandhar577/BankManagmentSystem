package com.bank.controller;

import com.bank.dao.UserDao;
import com.bank.model.User;
import com.bank.util.PasswordUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

// Mapped in web.xml to /api/auth/*
public class AuthServlet extends BaseServlet {

    private final UserDao userDao = new UserDao();

    /**
     * Handles POST requests for /login, /register, /logout
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        // Read the JSON payload from the request
        // We use a generic Map for flexibility
        Map<String, String> payload = readPayload(req, Map.class);

        try {
            switch (pathInfo) {
                case "/login":
                    handleLogin(req, resp, payload);
                    break;
                case "/register":
                    handleRegister(req, resp, payload);
                    break;
                case "/logout":
                    handleLogout(req, resp);
                    break;
                default:
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Not Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal error occurred.");
        }
    }
    
    /**
     * Handles GET requests for /session
     * This endpoint is used by the frontend on page load to check
     * if a user is already authenticated.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.equals("/session")) {
            HttpSession session = req.getSession(false); // false = don't create new session
            if (session != null && session.getAttribute("user") != null) {
                User user = (User) session.getAttribute("user");
                sendJsonResponse(resp, user); // Send back the logged-in user's data
            } else {
                // No valid session, send 401
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
            }
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Not Found");
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp, Map<String, String> payload) throws IOException {
        String username = payload.get("username");
        String password = payload.get("password");

        // Input validation
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Username and password are required.");
            return;
        }

        String storedHash = userDao.getPasswordHashByUsername(username);

        if (storedHash != null && PasswordUtils.checkPassword(password, storedHash)) {
            User user = userDao.getUserByUsername(username);
            
            // Check if user's account is 'ACTIVE'
            if (!"ACTIVE".equals(user.getStatus())) {
                 sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Account is " + user.getStatus() + ". Please contact admin.");
                 return;
            }

            // --- Create Session ---
            // req.getSession(true) creates a new session if one doesn't exist.
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user); // Store user object in session
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            // Send back the user object as confirmation
            sendJsonResponse(resp, user);
        } else {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password.");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp, Map<String, String> payload) throws IOException {
        String username = payload.get("username");
        String password = payload.get("password");
        String email = payload.get("email");
        String firstName = payload.get("firstName");
        String lastName = payload.get("lastName");

        // ** ADD ROBUST VALIDATION HERE (check for null, empty, email format, etc.) **
        if (username == null || password == null || email == null || firstName == null || lastName == null ||
            username.isEmpty() || password.isEmpty() || email.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "All fields are required.");
            return;
        }

        boolean success = userDao.createUser(username, password, email, firstName, lastName);

        if (success) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            sendJsonResponse(resp, Map.of("message", "Registration successful. Please wait for admin approval."));
        } else {
            sendError(resp, HttpServletResponse.SC_CONFLICT, "Registration failed. Username or email may already exist.");
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false); // Get existing session, don't create
        if (session != null) {
            session.invalidate(); // Invalidate the session, logs the user out
        }
        sendJsonResponse(resp, Map.of("message", "Logged out successfully."));
    }
}