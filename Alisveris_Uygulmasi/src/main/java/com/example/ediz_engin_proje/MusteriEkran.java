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

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class MusteriEkran extends Ekran implements Initializable {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    private static final String USER = "BBB";
    private static final String PASSWORD = "BBB";

    @FXML
    private TextField aramaKutusu;
    @FXML
    private ListView<Button> butonlar;
    @FXML
    private Label uyari;
    @FXML
    private Button admin_gec;
    @FXML
    private Label bakiye;
    @FXML
    private ListView<String> urunler;
    @FXML
    private ListView<Integer> stoklar;
    @FXML
    private ListView<Double> fiyatlar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            setMusteriBakiye();
            urunleriListele(null);
            gorunurYap();
        } catch (SQLException e) {
            uyari.setText("Başlatma sırasında bir hata oluştu.");
            e.printStackTrace();
        }
    }

    @FXML
    public void sepet() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sepetEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 1050, 720);
        stage.setTitle("Sepet");
        stage.setScene(scene);
        stage.show();
        Stage stage1 = (Stage) urunler.getScene().getWindow();
        stage1.close();
    }

    private void setMusteriBakiye() throws SQLException {
        String sql = "SELECT Bakiye FROM Musteriler WHERE MusteriAdi = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, MainController.aktifMusteri);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    YuklemeEkran.Bakiye = rs.getDouble("Bakiye");
                    bakiye.setText(String.valueOf(YuklemeEkran.Bakiye));
                }
            }
        }
    }

    private void urunleriListele(String orderBy) throws SQLException {
        String sql = "SELECT UrunAdi, UrunMiktar, Fiyat FROM Urunler";
        if (orderBy != null) {
            sql += " ORDER BY " + orderBy;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            urunler.getItems().clear();
            stoklar.getItems().clear();
            fiyatlar.getItems().clear();
            butonlar.getItems().clear();

            while (rs.next()) {
                String urunAdi = rs.getString("UrunAdi");
                int stok = rs.getInt("UrunMiktar");
                double fiyat = rs.getDouble("Fiyat");

                urunler.getItems().add(urunAdi);
                stoklar.getItems().add(stok);
                fiyatlar.getItems().add(fiyat);

                Button button = new Button("Sepete Ekle");
                button.setOnAction(e -> sepeteEkle(urunAdi, fiyat, stok));
                butonlar.getItems().add(button);
            }
        }
    }

    private void sepeteEkle(String urunAdi, double fiyat, int stok) {
        if (stok <= 0) {
            uyari.setText("Stokta yeterince ürün yok.");
            return;
        }

        String sqlKontrol = "SELECT Adet FROM Sepet WHERE MusteriID = ? AND UrunAdi = ?";
        String sqlEkle = "INSERT INTO Sepet (SepetID, MusteriID, UrunAdi, Fiyat, Adet) VALUES (?, ?, ?, ?, ?)";
        String sqlGuncelle = "UPDATE Sepet SET Adet = Adet + 1 WHERE MusteriID = ? AND UrunAdi = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            int musteriID = getMusteriID(MainController.aktifMusteri); // Get customer ID

            try (PreparedStatement kontrolStmt = conn.prepareStatement(sqlKontrol)) {
                kontrolStmt.setInt(1, musteriID);
                kontrolStmt.setString(2, urunAdi);

                try (ResultSet rs = kontrolStmt.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement guncelleStmt = conn.prepareStatement(sqlGuncelle)) {
                            guncelleStmt.setInt(1, musteriID);
                            guncelleStmt.setString(2, urunAdi);
                            guncelleStmt.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement ekleStmt = conn.prepareStatement(sqlEkle)) {
                            ekleStmt.setInt(1, musteriID);  // Set SepetID to MusteriID
                            ekleStmt.setInt(2, musteriID);  // Also set MusteriID
                            ekleStmt.setString(3, urunAdi);
                            ekleStmt.setDouble(4, fiyat);
                            ekleStmt.setInt(5, 1);
                            ekleStmt.executeUpdate();
                        }
                    }
                }
            }
            uyari.setText("Ürün sepete eklendi.");
        } catch (SQLException e) {
            uyari.setText("Sepete ekleme sırasında bir hata oluştu.");
            e.printStackTrace();
        }
    }


    private int getMusteriID(String musteriAdi) throws SQLException {
        String sql = "SELECT MusteriID FROM Musteriler WHERE MusteriAdi = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, musteriAdi);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("MusteriID");
                }
            }
        }
        throw new SQLException("Müşteri ID bulunamadı.");
    }

    public void gorunurYap() {
        if (MainController.yetkiliMi) {
            admin_gec.setVisible(true);
        }
    }

    public void aramaYap() throws SQLException {
        urunleriListele("UrunAdi LIKE '%" + aramaKutusu.getText().trim() + "%'");
    }

    public void fiyatSiralaAzalan() throws SQLException {
        urunleriListele("Fiyat DESC");
    }

    public void isimeGoreSirala() throws SQLException {
        urunleriListele("UrunAdi ASC");
    }

    public void stokMiktarinaGoreSirala() throws SQLException {
        urunleriListele("UrunMiktar ASC");
    }

    @FXML
    public void bakiyeYukle() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("yuklemeEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 303, 345);
        stage.setTitle("Bakiye Yükle");
        stage.setScene(scene);
        stage.show();
    }
    @FXML
    public void admin_gec() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("yoneticiEkran.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(fxmlLoader.load(), 1050, 720));
        stage.setTitle("Admin Ekran");
        stage.show();
        ((Stage) fiyatlar.getScene().getWindow()).close();
    }
    @FXML
    public void cikis() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("uygulama.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(fxmlLoader.load(), 600, 400));
        stage.setTitle("Giriş");
        stage.show();
        ((Stage) uyari.getScene().getWindow()).close();
    }
}
