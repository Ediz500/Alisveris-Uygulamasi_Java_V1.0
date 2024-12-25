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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MusteriEkran extends Ekran implements Initializable {
    String URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    String USER = "BBB";  // Kullanıcı adı
    String PASSWORD = "BBB";  // Şifre

    @FXML
    TextField aramaKutusu;
    @FXML
    ListView<Button> butonlar;
    @FXML
    Label uyari;
    @FXML
    Button admin_gec;
    @FXML
    Label bakiye;
    @FXML
    Label isim;
    @FXML
    Label stok;
    @FXML
    Label ucret;

    @FXML
    ListView<String> urunler = new ListView<>();
    @FXML
    ListView<Integer> stoklar = new ListView<>();
    @FXML
    ListView<Double> fiyatlar = new ListView<>();

    public static List<String> uruns = new ArrayList<>();
    public static List<Double> fiyats = new ArrayList<>();
    public static List<Integer> stoks = new ArrayList<>();

    public void gorunurYap(){
        if (MainController.yetkiliMi){
            admin_gec.setVisible(true);
            admin_gec.setDisable(false);
        }
    }

    public void mouseUstunde(){
        uyari.setText("");
    }

    public void bakiyeGoruntule(){
        bakiye.setText(String.valueOf(YuklemeEkran.Bakiye));
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
    public void fiyatSiralaAzalan() throws SQLException {
        urunleriSirala("Fiyat DESC");
    }

    @FXML
    public void isimeGoreSirala() throws SQLException {
        urunleriSirala("UrunAdi ASC");
    }

    @FXML
    public void stokMiktarinaGoreSirala() throws SQLException {
        urunleriSirala("UrunMiktar ASC");
    }

    private void urunleriSirala(String orderByColumn) throws SQLException {
        String sql = "SELECT UrunAdi, UrunMiktar, Fiyat FROM Urunler ORDER BY " + orderByColumn;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            urunler.getItems().clear();
            stoklar.getItems().clear();
            fiyatlar.getItems().clear();
            butonlar.getItems().clear();

            int satir = 0;
            while (resultSet.next()) {
                urunler.getItems().add(resultSet.getString("UrunAdi"));
                stoklar.getItems().add(resultSet.getInt("UrunMiktar"));
                fiyatlar.getItems().add(resultSet.getDouble("Fiyat"));

                Button button = new Button("Sepete Ekle");
                button.setPrefWidth(100);
                button.setPrefHeight(30);
                final int finalSatir = satir;
                button.setOnMouseExited(e -> mouseUstunde());
                button.setOnAction(e -> {
                    try {
                        sepeteEkleButton(finalSatir);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                butonlar.getItems().add(button);
                satir++;
            }
        }
    }

    @FXML
    private void aramaYap() throws SQLException {
        String aramaKelimesi = aramaKutusu.getText().trim();  // Arama kelimesini al
        String sql = "SELECT UrunAdi, UrunMiktar, Fiyat FROM Urunler WHERE UrunAdi LIKE ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);  // Bağlantıyı burada açıyoruz
             PreparedStatement statement = conn.prepareStatement(sql)) {

            if (!aramaKelimesi.isEmpty()) {
                statement.setString(1, "%" + aramaKelimesi + "%");
            } else {
                statement.setString(1, "%"); // Eğer arama kutusu boşsa, tüm ürünler için 'LIKE %' kullan
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                urunler.getItems().clear();
                stoklar.getItems().clear();
                fiyatlar.getItems().clear();
                butonlar.getItems().clear();

                int satir = 0;
                while (resultSet.next()) {
                    urunler.getItems().add(resultSet.getString("UrunAdi"));
                    stoklar.getItems().add(resultSet.getInt("UrunMiktar"));
                    fiyatlar.getItems().add(resultSet.getDouble("Fiyat"));

                    Button button = new Button("Sepete Ekle");
                    button.setPrefWidth(100);
                    button.setPrefHeight(30);
                    final int finalSatir = satir;
                    button.setOnMouseExited(e -> mouseUstunde());
                    button.setOnAction(e -> {
                        try {
                            sepeteEkleButton(finalSatir);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                    butonlar.getItems().add(button);
                    satir++;
                }
            }
        } catch (SQLException e) {
            uyari.setText("Arama sırasında bir hata oluştu.");
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

    public void admin_gec() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("yoneticiEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 1050, 720);
        stage.setTitle("Admin Ekran");
        stage.setScene(scene);
        stage.show();
        Stage stage1 = (Stage) fiyatlar.getScene().getWindow();
        stage1.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "SELECT MusteriAdi, MusteriSifresi, Bakiye FROM Musteriler WHERE MusteriAdi = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, MainController.aktifMusteri);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                YuklemeEkran.Bakiye = resultSet.getDouble("Bakiye");
            }

            resultSet.close();
            statement.close();

            yaz();  // Ürünleri veritabanından yazdır
        } catch (SQLException e) {
            e.printStackTrace();
        }

        gorunurYap();
        bakiyeGoruntule();
    }

    @Override
    public void yenile() throws IOException {
        Stage stage1 = (Stage) uyari.getScene().getWindow();
        stage1.close();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("musteriEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 1050, 720);
        stage.setTitle("Ana Ekran");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void yaz() throws SQLException {
        String sql = "SELECT UrunAdi, UrunMiktar, Fiyat FROM Urunler";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            int satir = 0;
            while (resultSet.next()) {
                urunler.getItems().add(resultSet.getString("UrunAdi"));
                stoklar.getItems().add(resultSet.getInt("UrunMiktar"));
                fiyatlar.getItems().add(resultSet.getDouble("Fiyat"));

                Button button = new Button("Sepete Ekle");
                button.setPrefWidth(100);
                button.setPrefHeight(30);
                final int finalSatir = satir;
                button.setOnMouseExited(e -> mouseUstunde());
                button.setOnAction(e -> {
                    try {
                        sepeteEkleButton(finalSatir);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                butonlar.getItems().add(button);
                satir++;
            }
        }
    }

    private void sepeteEkleButton(int satir) throws IOException {
        try {
            listedeVarmi(satir);
        } catch (Exception e) {
            uruns.add(urunler.getItems().get(satir));
            fiyats.add(fiyatlar.getItems().get(satir));
            stoks.add(1);
            uyari.setText("Ürün Sepete Eklendi.");
        }
    }

    public void listedeVarmi(int satir) {
        int b = 0;
        int deger = 0;
        if (stoklar.getItems().get(satir) == 0) {
            b = 2;
        }

        for (int i = 0; i < uruns.size(); i++) {
            if (urunler.getItems().get(satir).equals(uruns.get(i))) {
                if (stoklar.getItems().get(satir) == 0) {
                    b = 2;
                    break;
                } else {
                    if (stoklar.getItems().get(satir) == stoks.get(i)) {
                        b = 2;
                        break;
                    } else {
                        b = 1;
                        deger = i;
                        break;
                    }
                }
            }
        }

        if (b == 1) {
            int stok = stoks.get(deger);
            stoks.set(deger, stok + 1);
            uyari.setText("Ürün Sepete Eklendi.");
        } else if (b == 2) {
            uyari.setText("Stokta yeterince ürün bulunmuyor.");
        } else {
            uruns.add(urunler.getItems().get(satir));
            fiyats.add(fiyatlar.getItems().get(satir));
            stoks.add(1);
            uyari.setText("Ürün Sepete Eklendi.");
        }
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
