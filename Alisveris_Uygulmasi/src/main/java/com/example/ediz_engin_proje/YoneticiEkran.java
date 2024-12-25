package com.example.ediz_engin_proje;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class YoneticiEkran extends Ekran implements Initializable {

    String URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    String USER = "BBB";  // Kullanıcı adı
    String PASSWORD = "BBB";  // Şifre


    @FXML
    TextField urunAdi;
    @FXML
    TextField stokSayisi;
    @FXML
    TextField fiyat;
    @FXML
    Label uyari;
    @FXML
    ListView<String> urunler = new ListView<>();
    @FXML
    ListView<Integer> stoklar = new ListView<>();
    @FXML
    ListView<Double> fiyatlar = new ListView<>();
    @FXML
    ListView<Button> butonlar = new ListView<>();
    @FXML
    Button adminEkleCikar;

    private Connection connection;

    public YoneticiEkran() throws SQLException {
        // Establish a connection to the database
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public void gorunurYap() {
        if (MainController.yoneticiMi) {
            adminEkleCikar.setVisible(true);
            adminEkleCikar.setDisable(false);
        }
    }

    public void musteri_gec() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("musteriEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 1050, 720);
        stage.setTitle("Ana Ekran");
        stage.setScene(scene);
        stage.show();
        Stage stage1 = (Stage) uyari.getScene().getWindow();
        stage1.close();
    }

    @FXML
    public void adminEkleCikar() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("adminEkleEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 420, 457);
        stage.setTitle("Admin Ekle/Çıkar");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void urunEkle() throws SQLException {
        try {
            String urunAdiText = urunAdi.getText();
            int stokSayisiText = Integer.parseInt(stokSayisi.getText());
            double fiyatText = Double.parseDouble(fiyat.getText());
            if (urunKontrol(urunAdiText)) {
                uyari.setText("Bu Ürün Zaten Satışta.");
            } else {
                if (Objects.equals(urunAdiText, "") || stokSayisiText <= 0 || fiyatText <= 0) {
                    uyari.setText("Tüm bilgileri giriniz.");
                } else {
                    // Insert into database
                    String query = "INSERT INTO Urunler (UrunAdi, UrunMiktar, Fiyat) VALUES (?, ?, ?)";
                    try (PreparedStatement ps = connection.prepareStatement(query)) {
                        ps.setString(1, urunAdiText);
                        ps.setInt(2, stokSayisiText);
                        ps.setDouble(3, fiyatText);
                        ps.executeUpdate();
                    }
                    urunler.getItems().clear();
                    stoklar.getItems().clear();
                    fiyatlar.getItems().clear();
                    butonlar.getItems().clear();
                    yaz();
                }
            }
        } catch (Exception e) {
            uyari.setText("Bilgileri doğru giriniz.");
        }
    }

    @Override
    public void yaz() {
        try {
            String query = "SELECT UrunAdi, UrunMiktar, Fiyat FROM Urunler";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            int satir = 0;
            while (resultSet.next()) {
                String urunAdi = resultSet.getString("UrunAdi");
                int stokSayisi = resultSet.getInt("UrunMiktar");
                double fiyat = resultSet.getDouble("Fiyat");

                urunler.getItems().add(urunAdi);
                stoklar.getItems().add(stokSayisi);
                fiyatlar.getItems().add(fiyat);

                Button button = new Button("Sil");
                button.setPrefWidth(50);
                button.setPrefHeight(30);
                final int finalSatir = satir;
                button.setOnAction(e -> {
                    try {
                        silButton(finalSatir);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                butonlar.getItems().add(button);
                satir++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void silButton(int satir) throws SQLException {
        String urunAdiToDelete = urunler.getItems().get(satir);

        // 1. UrunMalzemeleri tablosundaki ilgili kayıtları silme
        String deleteUrunMalzemeleriQuery = "DELETE FROM UrunMalzemeleri WHERE UrunID = (SELECT UrunID FROM Urunler WHERE UrunAdi = ?)";
        try (PreparedStatement ps = connection.prepareStatement(deleteUrunMalzemeleriQuery)) {
            ps.setString(1, urunAdiToDelete);
            ps.executeUpdate();
        }

        // 2. SiparisUrunleri tablosundaki ilgili kayıtları silme (Eğer gerekli ise)
        String deleteSiparisUrunleriQuery = "DELETE FROM SiparisUrunleri WHERE UrunID = (SELECT UrunID FROM Urunler WHERE UrunAdi = ?)";
        try (PreparedStatement ps = connection.prepareStatement(deleteSiparisUrunleriQuery)) {
            ps.setString(1, urunAdiToDelete);
            ps.executeUpdate();
        }

        // 3. Urunler tablosundaki ürünü silme
        String deleteUrunQuery = "DELETE FROM Urunler WHERE UrunAdi = ?";
        try (PreparedStatement ps = connection.prepareStatement(deleteUrunQuery)) {
            ps.setString(1, urunAdiToDelete);
            ps.executeUpdate();
        }

        // 4. ListView'den ürün ve ilgili bilgileri kaldırma
        urunler.getItems().remove(satir);
        stoklar.getItems().remove(satir);
        fiyatlar.getItems().remove(satir);
        butonlar.getItems().remove(satir);

        // 5. Ekranı yenileme
        try {
            yenile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void yenile() throws IOException {
        Stage stage1 = (Stage) uyari.getScene().getWindow();
        stage1.close();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("yoneticiEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 1050, 720);
        stage.setTitle("Admin Ekran");
        stage.setScene(scene);
        stage.show();
    }

    private boolean urunKontrol(String girilenAd) throws SQLException {
        String query = "SELECT COUNT(*) FROM Urunler WHERE UrunAdi = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, girilenAd);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        }
        return false;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            yaz();
        } catch (Exception e) {
            e.printStackTrace();
        }
        gorunurYap();
    }

    @Override
    public void cikis() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("uygulama.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Giriş");
        stage.setScene(scene);
        stage.show();
        Stage stage1 = (Stage) uyari.getScene().getWindow();
        stage1.close();
    }
}
