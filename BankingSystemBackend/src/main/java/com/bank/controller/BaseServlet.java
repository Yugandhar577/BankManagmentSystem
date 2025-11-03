package com.bank.controller;

import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * A base servlet to handle common tasks like CORS and JSON responses.
 */
public abstract class BaseServlet extends HttpServlet {

    protected final Gson gson = new Gson();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCORS(resp);
        super.service(req, resp);
    }
    
    /**
     * Handles CORS Preflight (OPTIONS) requests.
     */
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupCORS(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Configures CORS headers for development (http://localhost:3000).
     */
    private void setupCORS(HttpServletResponse resp) {
        // !! IMPORTANT: In production, restrict this to your actual frontend domain.
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With");
        // !! CRITICAL: This allows the JSESSIONID cookie to be sent
        resp.setHeader("Access-Control-Allow-Credentials", "true");
    }

    /**
     * Helper to read the JSON payload from a POST/PUT request.
     */
    protected <T> T readPayload(HttpServletRequest req, Class<T> classOfT) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return gson.fromJson(sb.toString(), classOfT);
    }

    /**
     * Helper to send a JSON response.
     */
    protected void sendJsonResponse(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(gson.toJson(data));
            out.flush();
        }
    }

    /**
     * Helper to send an error response.
     */
    protected void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        sendJsonResponse(resp, Map.of("error", message));
    }
}