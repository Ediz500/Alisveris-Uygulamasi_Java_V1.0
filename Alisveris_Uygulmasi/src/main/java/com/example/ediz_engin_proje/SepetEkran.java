package com.example.ediz_engin_proje;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SepetEkran extends Ekran implements Initializable {

    String URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    String USER = "BBB";  // Kullanıcı adı
    String PASSWORD = "BBB";  // Şifre
    @FXML
    ListView<Double> toplam = new ListView<>();
    @FXML
    ListView<String> urunler = new ListView<>();
    @FXML
    ListView<Integer> stoklar = new ListView<>();
    @FXML
    ListView<Double> fiyatlar = new ListView<>();
    @FXML
    ListView<Button> butonlar = new ListView<>();
    @FXML
    Label bakiye;
    @FXML
    Label uyari;
    @FXML
    Label tutar;
    @FXML
    Button sepetTemizle;
    List<String> urunAdlari = new ArrayList<>();
    List<String> urunStoklari = new ArrayList<>();
    List<String> urunFiyatlari = new ArrayList<>();
    double toplamtutar;

    public void bakiyeGoruntule() {
        bakiye.setText(String.valueOf(YuklemeEkran.Bakiye));
    }

    public void bakiyeYukle() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("yuklemeEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 303, 345);
        stage.setTitle("Bakiye Yükle");
        stage.setScene(scene);
        stage.show();
    }

    public void geri_git() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("musteriEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 1050, 720);
        stage.setTitle("Ana Ekran");
        stage.setScene(scene);
        stage.show();
        Stage stage1 = (Stage) urunler.getScene().getWindow();
        stage1.close();
    }

    @FXML
    public void odemeYap() throws IOException {
        for (int j = 0; j < toplam.getItems().size(); j++) {
            toplamtutar += toplam.getItems().get(j);
        }

        if (toplamtutar > YuklemeEkran.Bakiye) {
            uyari.setText("Bakiyeniz Yetersiz");
        } else if (toplamtutar == 0) {
            uyari.setText("Sepetiniz Boş.");
        } else {
            YuklemeEkran.Bakiye -= toplamtutar;

            // Bakiye güncelleme SQL sorgusu
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                String updateQuery = "UPDATE Musteriler SET Bakiye = ? WHERE MusteriAdi = ?";
                PreparedStatement stmt = conn.prepareStatement(updateQuery);
                stmt.setDouble(1, YuklemeEkran.Bakiye);
                stmt.setString(2, "MusteriAdi"); // Kullanıcı adı burada belirlenmeli
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            stokDus();
            MusteriEkran.fiyats.clear();
            MusteriEkran.uruns.clear();
            MusteriEkran.stoks.clear();
            butonlar.getItems().clear();
            YuklemeEkran.sahsiBakiye();
            yenile();
        }
    }

    @Override
    public void yaz(){
        int satir=0;
        while (satir<=MusteriEkran.uruns.size()-1){
            toplamtutar=0;
            urunler.getItems().add(MusteriEkran.uruns.get(satir));
            stoklar.getItems().add(MusteriEkran.stoks.get(satir));
            fiyatlar.getItems().add(MusteriEkran.fiyats.get(satir));
            toplam.getItems().add(MusteriEkran.fiyats.get(satir)*MusteriEkran.stoks.get(satir));
            for (int j=0; j<toplam.getItems().size(); j++){
                toplamtutar += toplam.getItems().get(j);
            }
            tutar.setText(String.valueOf(toplamtutar));
            Button button = new Button("Sepetten Sil");
            button.setPrefWidth(100);
            button.setPrefHeight(30);
            final int finalSatir = satir;
            button.setOnAction(e -> {
                        try {
                            silButton(finalSatir);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
            );
            butonlar.getItems().add(button);
            satir++;
        }
    }

    private void silButton(int satir) throws IOException {
        if (MusteriEkran.stoks.get(satir)>1){
            int i = MusteriEkran.stoks.get(satir);
            MusteriEkran.stoks.set(satir,i-1);
            uyari.setText("Sepet Güncellendi");
        }
        else {
            MusteriEkran.fiyats.remove(satir);
            MusteriEkran.uruns.remove(satir);
            MusteriEkran.stoks.remove(satir);
            butonlar.getItems().remove(satir);
            uyari.setText("Sepet Güncellendi");
        }
        yenile();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        yaz();
        bakiyeGoruntule();
        gorunurYap();
        toplamtutar = 0;
    }

    @Override
    public void yenile() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sepetEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 1050, 720);
        stage.setTitle("Sepet");
        stage.setScene(scene);
        stage.show();
        Stage stage1 = (Stage) urunler.getScene().getWindow();
        stage1.close();
    }

    public void stokDus() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Sepetteki her bir ürünü kontrol ediyoruz
            for (int i = 0; i < MusteriEkran.uruns.size(); i++) {
                String urunAdi = MusteriEkran.uruns.get(i);
                int stok = MusteriEkran.stoks.get(i);

                // SQL sorgusu ile veritabanındaki stokları güncelliyoruz
                String updateQuery = "UPDATE Urunler SET UrunMiktar = UrunMiktar - ? WHERE UrunAdi = ?";
                PreparedStatement stmt = conn.prepareStatement(updateQuery);
                stmt.setInt(1, stok); // Sepetteki stok miktarı kadar azaltma
                stmt.setString(2, urunAdi); // Ürün adı
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void sepetiTemizle() throws IOException {
        MusteriEkran.fiyats.clear();
        MusteriEkran.uruns.clear();
        MusteriEkran.stoks.clear();
        butonlar.getItems().clear();
        yenile();
    }

    public void gorunurYap() {
        try {
            if (MusteriEkran.uruns.size() > 0) {
                sepetTemizle.setVisible(true);
                sepetTemizle.setDisable(false);
            }
        } catch (Exception e) {
            sepetTemizle.setVisible(false);
            sepetTemizle.setDisable(true);
        }
    }
}
