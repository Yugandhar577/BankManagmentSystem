package com.bank.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DataSource dataSource;

    // Static block to initialize the DataSource from JNDI
    static {
        try {
            // JNDI lookup to find the resource defined in context.xml
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            dataSource = (DataSource) envContext.lookup("jdbc/bankDB");
        } catch (NamingException e) {
            // This is a critical failure; the application cannot run without the DB.
            throw new RuntimeException("Cannot find JNDI DataSource 'jdbc/bankDB'", e);
        }
    }

    /**
     * Gets a connection from the Tomcat-managed connection pool.
     * @return A java.sql.Connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}