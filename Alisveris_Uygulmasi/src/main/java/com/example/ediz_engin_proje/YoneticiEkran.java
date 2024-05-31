package com.example.ediz_engin_proje;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class YoneticiEkran extends Ekran implements Initializable {
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
    public void gorunurYap(){
        if (MainController.yoneticiMi){
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
    public void urunEkle() throws IOException {
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
                    try (FileWriter writer = new FileWriter("urunler.txt", true)) {
                        writer.write(urunAdiText + ":" + stokSayisiText + ":" + fiyatText + "\n");
                    } catch (Exception e) {
                        uyari.setText("Okuyamadım Abii, affet :(.");
                    }
                    urunler.getItems().clear();
                    stoklar.getItems().clear();
                    fiyatlar.getItems().clear();
                    butonlar.getItems().clear();
                    yaz();
                }
            }
        }
        catch (Exception e){
            uyari.setText("Bilgileri doğru giriniz.");
        }
    }
    @Override
    public void yaz(){
        try (BufferedReader reader = new BufferedReader(new FileReader("urunler.txt"))) {
            String line;
            int satir =0;
            while ((line = reader.readLine()) != null) {
                String[] urun = line.split(":");
                urunler.getItems().add(urun[0].trim());
                stoklar.getItems().add(Integer.parseInt(urun[1].trim()));
                fiyatlar.getItems().add(Double.parseDouble(urun[2].trim()));
                Button button = new Button("Sil");
                button.setPrefWidth(50);
                button.setPrefHeight(30);
                final int finalSatir = satir;
                button.setOnAction(e -> {
                    try {
                        silButton(finalSatir);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                butonlar.getItems().add(button);
                satir++;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void silButton(int satir) throws IOException {
        urunler.getItems().remove(satir);
        stoklar.getItems().remove(satir);
        fiyatlar.getItems().remove(satir);
        butonlar.getItems().remove(satir);
        try {
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader("urunler.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
                br.close();
            }
            if (satir >= 0 && satir <= lines.size()) {
                lines.remove(satir);
            } else {
                System.out.println("Geçersiz satır numarası.");
                return;
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("urunler.txt"))) {
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
                bw.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
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
    private boolean urunKontrol(String girilenAd) {
        try (BufferedReader reader = new BufferedReader(new FileReader("urunler.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    String storedUsername = parts[0].trim();
                    if (girilenAd.equals(storedUsername)) {
                        return true;
                    }
                }
            }reader.close();
        }catch (Exception e) {
            uyari.setText("Hata Meydana Geldi.");
        }
        return false;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        yaz();
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
