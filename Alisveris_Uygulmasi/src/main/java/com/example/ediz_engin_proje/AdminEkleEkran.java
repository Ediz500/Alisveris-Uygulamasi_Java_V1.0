package com.example.ediz_engin_proje;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AdminEkleEkran extends Ekran implements Initializable {

    String URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    String USER = "BBB";  // Kullanıcı adı
    String PASSWORD = "BBB";  // Şifre

    @FXML
    Label uyari;
    @FXML
    TextField loginUsername;
    @FXML
    TextField loginPassword;
    @FXML
    ListView<String> adminler = new ListView<>();
    @FXML
    ListView<Button> butonlar = new ListView<>();

    @FXML
    public void adminEkle() throws IOException {
        String username = loginUsername.getText(); // Kullanıcı adı
        String password = loginPassword.getText(); // Şifre

        // Admin adı kontrolü
        if (adminKontrol(username)) {
            uyari.setText("Kullanıcı Adı Kullanılıyor.");
        } else {
            if (username.isEmpty() || password.isEmpty()) {
                uyari.setText("Kullanıcı Adı ya da Şifre Boş Olamaz.");
            } else {
                // Admini veritabanına ekleme
                try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                    String sql = "INSERT INTO Adminler (AdminAdi, AdminSifre) VALUES (?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, username); // Kullanıcı adını sorguya ekle
                        stmt.setString(2, password); // Şifreyi sorguya ekle
                        stmt.executeUpdate(); // Veritabanına ekle
                        uyari.setText("Admin başarıyla eklendi.");
                    }
                } catch (SQLException e) {
                    uyari.setText("Hata Meydana Geldi.");
                    e.printStackTrace();
                }
                yenile(); // Yenileme işlemi
            }
        }
    }

    private boolean adminKontrol(String girilenAd) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "SELECT COUNT(*) FROM Adminler WHERE AdminAdi = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, girilenAd); // Kullanıcı adı parametresini sorguya ekliyoruz
                ResultSet rs = stmt.executeQuery(); // Sorguyu çalıştırıyoruz
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Eğer admin adı varsa, count > 0 olacak
                }
            }
        } catch (SQLException e) {
            uyari.setText("Veritabanı hatası.");
            e.printStackTrace();
        }
        return false; // Admin adı bulunmadıysa false döner
    }
    @Override
    public void cikis(){
        Stage stage1 = (Stage) uyari.getScene().getWindow();
        stage1.close();
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        yaz();
    }
    @Override
    public void yaz() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "SELECT AdminAdi FROM Adminler";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery(); // Adminleri sorgula
                while (rs.next()) {
                    String adminAdi = rs.getString("AdminAdi");
                    adminler.getItems().add(adminAdi); // Admini listeye ekle

                    // Sil butonunu oluştur
                    Button button = new Button("Sil");
                    button.setPrefWidth(50);
                    button.setPrefHeight(30);
                    button.setOnAction(e -> {
                        try {
                            silButton(adminAdi); // Sil butonuna tıklandığında admini sil
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                    butonlar.getItems().add(button); // Butonu listeye ekle
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void silButton(String adminAdi) throws IOException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "DELETE FROM Adminler WHERE AdminAdi = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, adminAdi); // Silinecek adminin adını sorguya ekle
                stmt.executeUpdate(); // Admini veritabanından sil
                uyari.setText("Admin başarıyla silindi.");
            }
        } catch (SQLException e) {
            uyari.setText("Hata Meydana Geldi.");
            e.printStackTrace();
        }
        yenile(); // Yenileme işlemi
    }

    @Override
    public void yenile() throws IOException {
        Stage stage1 = (Stage) uyari.getScene().getWindow(); // Mevcut pencereyi kapat
        stage1.close();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("adminEkleEkran.fxml")); // Yeni ekranı yükle
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 420, 457);
        stage.setTitle("Admin Ekle/Çıkar");
        stage.setScene(scene);
        stage.show(); // Yeni pencereyi göster
    }
}
