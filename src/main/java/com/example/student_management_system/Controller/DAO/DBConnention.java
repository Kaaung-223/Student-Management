package com.example.student_management_system.Controller.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place that opens a JDBC connection to the `student_management` database.
 *
 * REQUIREMENT: you need the MySQL Connector/J jar on your classpath.
 *   Maven:  <dependency>
 *               <groupId>com.mysql</groupId>
 *               <artifactId>mysql-connector-j</artifactId>
 *               <version>8.4.0</version>
 *           </dependency>
 *   Gradle: implementation 'com.mysql:mysql-connector-j:8.4.0'
 *   (No dependency manager: download the jar from
 *    https://dev.mysql.com/downloads/connector/j/ and add it to your module path.)
 *
 * EDIT THESE THREE VALUES to match your local MySQL setup:
 */
public class DBConnention {

    private static final String HOST = "dotenv.mysql_username";
    private static final String PORT = "dotenv.mysql_password";
    private static final String DATABASE = "dotenv.mysql_user_database";

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String USER = "root";
    private static final String PASSWORD = "kk2386mm";   // <-- put your MySQL root password here

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Quick standalone check — right click this file and "Run" (or run it from your
     * IDE / `java` command) to confirm the connection works before wiring up the DAOs.
     * Prints "CONNECT OK" on success, or the exact JDBC error on failure.
     */
    public static void main(String[] args) {
        try (Connection con = getConnection()) {
            if (con != null && !con.isClosed()) {
                System.out.println("CONNECT OK — connected to database: " + DATABASE);
            }
        } catch (SQLException e) {
            System.out.println("CONNECT FAILED");
            System.out.println("Reason: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
