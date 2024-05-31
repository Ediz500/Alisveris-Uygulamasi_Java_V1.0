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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class YuklemeEkran extends Ekran implements Initializable {
    public static double Bakiye;
    static List<String> musteriIsim= new ArrayList<>();
    static List<String> musteriParola = new ArrayList<>();
    static List<String> musteriBakiye = new ArrayList<>();
    @FXML
    TextField para;
    @FXML
    Label paraMiktar;
    @Override
    public void yaz() throws IOException {
        paraMiktar.setText(Double.toString(Bakiye));
    }

    public void BakiyeYukle() throws IOException {
        Bakiye += Double.parseDouble(para.getText());
        sahsiBakiye();
        yenile();
    }
    public void bes(){
        para.setText("5");
    }
    public void on(){
        para.setText("10");
    }
    public void yirmi(){
        para.setText("20");
    }
    public void elli(){
        para.setText("50");
    }
    public void yuz(){
        para.setText("100");
    }
    public void ikiyuz(){
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
            yaz();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void sahsiBakiye(){
        try (BufferedReader reader = new BufferedReader(new FileReader("musteriler.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] musteri = line.split(":");
                musteriIsim.add(musteri[0].trim());
                musteriParola.add(musteri[1].trim());
                musteriBakiye.add(musteri[2].trim());
            }
            reader.close();
            Path txt = Path.of("musteriler.txt");
            Files.deleteIfExists(txt);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i<musteriIsim.size(); i++){

            if (musteriIsim.get(i).equals(MainController.aktifMusteri))
            {
                musteriBakiye.set(i, String.valueOf(Bakiye));
            }

        }
        for (int i = 0; i<musteriIsim.size(); i++){
            try (FileWriter writer = new FileWriter("musteriler.txt", true)) {
                writer.write(musteriIsim.get(i) + ":" + musteriParola.get(i) + ":" + musteriBakiye.get(i) + "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        YuklemeEkran.musteriIsim.clear();
        YuklemeEkran.musteriParola.clear();
        YuklemeEkran.musteriBakiye.clear();

    }
}
