package com.bank.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class UserServlet
 */
@WebServlet("/user") // Maps this servlet to the URL pattern /user
public class UserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     * Handles HTTP GET requests, typically for displaying data or a form.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
        
        // Example: If the request is /user?action=list, it might fetch all users
        String action = request.getParameter("action");
        
        if ("list".equals(action)) {
            // Placeholder for business logic: fetch a list of users from a service/DAO
            // List<User> userList = userService.getAllUsers();
            
            // Set the data as an attribute to be accessed by the JSP
            // request.setAttribute("users", userList);
            
            // Forward the request to a JSP to render the list
            request.getRequestDispatcher("/userList.jsp").forward(request, response);
        } else {
            // Default action: maybe show a user profile or an input form
            response.getWriter().append("Served at: ").append(request.getContextPath());
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     * Handles HTTP POST requests, typically for form submissions (creating or updating a user).
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
        
        // 1. Get parameters from the HTML form
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        
        // 2. Placeholder for business logic: validate and save the user data
        // userService.saveUser(username, email);
        
        // 3. Set a message for the user
        request.setAttribute("message", "User " + username + " registered successfully!");
        
        // 4. Redirect or forward
        // Use a redirect to prevent double-submission (Post-Redirect-Get pattern)
        response.sendRedirect(request.getContextPath() + "/success.jsp");
    }
}