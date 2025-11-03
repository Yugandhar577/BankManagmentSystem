// --- src/main/java/com/bank/controller/CORSFilter.java ---

package com.bank.controller; // Or com.bank.util

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter adds the necessary CORS headers to the response
 * to allow requests from the React frontend (running on http://localhost:3000).
 */
public class CORSFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code, if any
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Set the allowed origin (your React app's URL)
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        
        // Set the allowed HTTP methods (e.g., GET, POST, PUT, DELETE)
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        
        // Set the allowed headers
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        
        // Allow credentials (if you use sessions/cookies, though we use JWT)
        response.setHeader("Access-Control-Allow-Credentials", "true");
        
        // Set the max age for pre-flight requests (in seconds)
        response.setHeader("Access-Control-Max-Age", "3600");

        // Handle the OPTIONS "pre-flight" request
        // The browser sends this first to check if the actual request is safe
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            // Pass the request along the filter chain
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        // Cleanup code, if any
    }
}