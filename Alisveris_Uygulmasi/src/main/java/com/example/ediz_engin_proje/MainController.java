package com.example.ediz_engin_proje;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController extends Ekran implements Initializable {
    String URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    String USER = "BBB";  // Kullanıcı adı
    String PASSWORD = "BBB";  // Şifre
    
    
    @FXML
    TextField kullaniciAdi;
    @FXML
    TextField sifre;
    @FXML
    Label uyari;
    static boolean yoneticiMi = false;
    static boolean yetkiliMi = false;
    static int girisKontrol = 0;
    static String aktifMusteri;

    @FXML
    public void kayitOl() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("kaydolEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Kaydol");
        stage.setScene(scene);
        stage.show();
    }

    public void giris() throws SQLException, IOException {
        kullaniciKontrol();
        switch (girisKontrol){
            case 0:
                uyari.setText("Kullanıcı Bulunamadı.");
                break;
            case 1:
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("yoneticiEkran.fxml"));
                Stage stage = new Stage();
                Scene scene = new Scene(fxmlLoader.load(), 1050, 720);
                stage.setTitle("Admin Ekran");
                stage.setScene(scene);
                stage.show();
                Stage stage3 = (Stage) uyari.getScene().getWindow();
                stage3.close();
                break;
            case 2:
                FXMLLoader fxmlLoader1 = new FXMLLoader(getClass().getResource("musteriEkran.fxml"));
                Stage stage1 = new Stage();
                Scene scene1 = new Scene(fxmlLoader1.load(), 1050, 720);
                stage1.setTitle("Ana Ekran");
                stage1.setScene(scene1);
                stage1.show();
                Stage stage2 = (Stage) uyari.getScene().getWindow();
                stage2.close();
                break;
        }
    }

    @FXML
    public void kullaniciKontrol() throws SQLException {
        girisKontrol = 0;
        yetkiliMi = false;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Yönetici kontrolü
            String sql = "SELECT YoneticiAdi, YoneticiSifre FROM Yoneticiler WHERE YoneticiAdi = ? AND YoneticiSifre = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, kullaniciAdi.getText());
                statement.setString(2, sifre.getText());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Yöneticinin giriş yaptığı tespit edildi
                        yoneticiMi = true;
                        yetkiliMi = true;
                        girisKontrol = 1;
                        return; // Yönetici girişi doğrulandı, metottan çık
                    }
                }
            }

            // Müşteri kontrolü
            String sql2 = "SELECT MusteriAdi, MusteriSifresi FROM Musteriler WHERE MusteriAdi = ? AND MusteriSifresi = ?";
            try (PreparedStatement statement2 = conn.prepareStatement(sql2)) {
                statement2.setString(1, kullaniciAdi.getText());
                statement2.setString(2, sifre.getText());
                try (ResultSet resultSet2 = statement2.executeQuery()) {
                    if (resultSet2.next()) {
                        // Müşteri giriş yaptı
                        girisKontrol = 2;
                        aktifMusteri = kullaniciAdi.getText();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            uyari.setText("Veritabanı bağlantı hatası.");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Bu kısımda yöneticilerin başlangıçta sisteme tanıtılması gerekebilir
    }

    @Override
    public void cikis() throws IOException {
        Stage stage1 = (Stage) uyari.getScene().getWindow();
        stage1.close();
    }
}
