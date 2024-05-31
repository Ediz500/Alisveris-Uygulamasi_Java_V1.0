package com.example.ediz_engin_proje;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class MainController extends Ekran implements Initializable {
    @FXML
    TextField kullaniciAdi;
    @FXML
    TextField sifre;
    @FXML
    Label uyari;
    static List<String> yoneticiler = new ArrayList<>();
    static List<String> yoneticiSifreler = new ArrayList<>();
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
    public void giris() throws IOException {
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
    public void kullaniciKontrol() throws IOException {
        girisKontrol = 0;
        yetkiliMi = false;
        BufferedReader reader = new BufferedReader(new FileReader("adminler.txt"));
        String line;
        int satir = 0;
        while ((line = reader.readLine()) != null) {
            String[] kullanici = line.split(":");
            if ((yoneticiler.get(satir).equals(kullaniciAdi.getText()) && (yoneticiSifreler.get(satir).equals(sifre.getText()))) || (kullanici[0].trim().equals(kullaniciAdi.getText()) && kullanici[1].trim().equals(sifre.getText())))
            {
                yoneticiMi=false;
                yetkiliMi=true;
                if ((yoneticiler.get(satir).equals(kullaniciAdi.getText()) && (yoneticiSifreler.get(satir).equals(sifre.getText())))){
                    yoneticiMi=true;
                }
                girisKontrol = 1;
                break;
            }
            else {
                BufferedReader reader2 = new BufferedReader(new FileReader("musteriler.txt"));
                String line2;
                while ((line2 = reader2.readLine()) != null){
                    String[] kullanici2 = line2.split(":");
                    if (kullanici2[0].trim().equals(kullaniciAdi.getText()) && kullanici2[1].trim().equals(sifre.getText())){
                        girisKontrol = 2;
                        aktifMusteri = kullaniciAdi.getText();
                    }

                }
                reader2.close();

            }
            if (satir < yoneticiSifreler.size()-1){
                satir += 1;
            }
        }
        reader.close();

    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        yoneticiler.add("ediz");
        yoneticiler.add("damla");
        yoneticiSifreler.add("ediz");
        yoneticiSifreler.add("damla");
        MusteriEkran.uruns.clear();
        MusteriEkran.stoks.clear();
        MusteriEkran.fiyats.clear();
    }

    @Override
    public void cikis() throws IOException {
        Stage stage1 = (Stage) uyari.getScene().getWindow();
        stage1.close();
    }
}