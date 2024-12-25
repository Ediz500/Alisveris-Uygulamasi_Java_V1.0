package com.example.ediz_engin_proje;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class YuklemeEkran extends Ekran implements Initializable {
    public static double Bakiye;

    @FXML
    TextField para;

    @FXML
    Label paraMiktar;

    // Veritabanı bağlantısı için bilgiler
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    private static final String DB_USER = "BBB";
    private static final String DB_PASSWORD = "BBB";

    @Override
    public void yaz() throws SQLException {
        // Bakiye bilgisini veritabanından çekmek
        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        String query = "SELECT Bakiye FROM Musteriler WHERE MusteriAdi = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, MainController.aktifMusteri);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Bakiye = resultSet.getDouble("Bakiye");
            }
            paraMiktar.setText(Double.toString(Bakiye));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Veritabanı bağlantısı sırasında bir hata oluştu.");
        } finally {
            connection.close();
        }
    }

    public void BakiyeYukle() throws SQLException {
        // Bakiye güncellemesi
        Bakiye += Double.parseDouble(para.getText());
        sahsiBakiye();
        try {
            yenile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void bes() {
        para.setText("5");
    }

    public void on() {
        para.setText("10");
    }

    public void yirmi() {
        para.setText("20");
    }

    public void elli() {
        para.setText("50");
    }

    public void yuz() {
        para.setText("100");
    }

    public void ikiyuz() {
        para.setText("200");
    }

    @Override
    public void yenile() throws IOException {
        Stage stage1 = (Stage) para.getScene().getWindow();
        stage1.close();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("yuklemeEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 303, 345);
        stage.setTitle("Bakiye Yükle");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            yaz(); // Bakiye bilgisini al
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sahsiBakiye() {
        Connection connection = null;
        try {
            // Veritabanı bağlantısını kur
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Bakiye güncelleme işlemi
            String query = "UPDATE Musteriler SET Bakiye = ? WHERE MusteriAdi = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setDouble(1, Bakiye);
                preparedStatement.setString(2, MainController.aktifMusteri);
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();  // Veritabanı bağlantısını kapat
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
