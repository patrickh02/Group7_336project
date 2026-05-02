package com.flightreservation.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // ── CHANGE THESE to match your MySQL setup ──────────────────────────────
    private static final String HOST = "localhost";  // MySQL host
    private static final String PORT = "3306";       // MySQL port
    private static final String DB   = "flight_reservation"; // database name
    private static final String USER = "root";       // MySQL username
    private static final String PASS = "password";   // MySQL password
    // ────────────────────────────────────────────────────────────────────────

    private static final String URL =
        "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "MySQL JDBC Driver not found. Place mysql-connector-j-<version>.jar in lib/", e);
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
