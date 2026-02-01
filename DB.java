package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    // Change to your local settings:
    private static final String URL  = "jdbc:postgresql://localhost:5432/assignment4_oop";
    private static final String USER = "postgres";
    private static final String PASS = "020408SMZH";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
