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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MusteriEkran extends Ekran implements Initializable {
    @FXML
    ListView<Button> butonlar;
    @FXML
    Label uyari;
    @FXML
    Button admin_gec;
    @FXML
    Label bakiye;
    @FXML
    ListView<String> urunler = new ListView<>();
    @FXML
    ListView<Integer> stoklar = new ListView<>();
    @FXML
    ListView<Double> fiyatlar = new ListView<>();
    static List<String> uruns = new ArrayList<>();
    static List<Double> fiyats = new ArrayList<>();
    static List<Integer> stoks = new ArrayList<>();
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
        try (BufferedReader reader = new BufferedReader(new FileReader("musteriler.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] musteri = line.split(":");
                YuklemeEkran.musteriIsim.add(musteri[0].trim());
                YuklemeEkran.musteriParola.add(musteri[1].trim());
                YuklemeEkran.musteriBakiye.add(musteri[2].trim());
            }
            reader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i<YuklemeEkran.musteriIsim.size(); i++){

            if (YuklemeEkran.musteriIsim.get(i).equals(MainController.aktifMusteri))
            {
                YuklemeEkran.Bakiye = Double.parseDouble(YuklemeEkran.musteriBakiye.get(i));

            }

        }
        try {
            yaz();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        gorunurYap();
        bakiyeGoruntule();
        YuklemeEkran.musteriIsim.clear();
        YuklemeEkran.musteriParola.clear();
        YuklemeEkran.musteriBakiye.clear();
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
    public void yaz() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("urunler.txt"))) {
            String line;
            int satir =0;
            while ((line = reader.readLine()) != null) {
                String[] urun = line.split(":");
                urunler.getItems().add(urun[0].trim());
                stoklar.getItems().add(Integer.parseInt(urun[1].trim()));
                fiyatlar.getItems().add(Double.parseDouble(urun[2].trim()));
                Button button = new Button("Sepete Ekle");
                button.setPrefWidth(100);
                button.setPrefHeight(30);
                final int finalSatir = satir;
                button.setOnMouseExited(e ->{
                    mouseUstunde();
                });
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
            reader.close();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void sepeteEkleButton(int satir) throws IOException {
        try {
            listedeVarmi(satir);
            }

        catch (Exception e){
            uruns.add(urunler.getItems().get(satir));
            fiyats.add(fiyatlar.getItems().get(satir));
            stoks.add(1);
            uyari.setText("Ürün Sepete Eklendi.");
        }
    }
    public void listedeVarmi(int satir){
        int b = 0;
        int deger = 0;
        if (stoklar.getItems().get(satir) == 0){
            b = 2;
        }
        for (int i = 0; i<uruns.size();i++){
            if (urunler.getItems().get(satir).equals(uruns.get(i))){
                if (stoklar.getItems().get(satir) == 0){
                    b = 2;
                    break;
                }
                else {
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
        if (b == 1){
            int stok = stoks.get(deger);
            stoks.set(deger, stok + 1);
            uyari.setText("Ürün Sepete Eklendi.");
        }
        else if (b == 2){
            uyari.setText("Stokta yeterince ürün bulunmuyor.");
        }
        else {
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
