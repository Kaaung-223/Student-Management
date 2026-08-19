package com.example.student_management_system.Controller.DataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase_Connection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/student_management?useSSL=false&serverTimezone=UTC";

    private static final String USER = "root";

    // XAMPP MySQL password မရှိရင် ""
    private static final String PASSWORD = "kk2386mm";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}