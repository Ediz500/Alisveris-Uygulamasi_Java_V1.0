package com.example.ediz_engin_proje;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    private static final String USERNAME = "BBB";  // Kullanıcı adı
    private static final String PASSWORD = "BBB";  // Şifre

    connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
