package com.example.ediz_engin_proje;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class KontrolPanel {
    @FXML
    Button geriDon;
    @FXML
    Button yemekler;
    @FXML
    Button musteriler;
    @FXML
    Button malzemeler;
    @FXML
    Button siparisler;
    @FXML
    Button urunIcerigi;

    @FXML
    Label uyari;


    public void geriDon() throws SQLException, IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("yoneticiEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 1050, 720);
        stage.setTitle("Admin Ekran");
        stage.setScene(scene);
        stage.show();
        Stage stage3 = (Stage) uyari.getScene().getWindow();
        stage3.close();
    }

    public void yemekler() throws SQLException, IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("yemekler.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 402, 371);
        stage.setTitle("Yemekler");
        stage.setScene(scene);
        stage.show();

    }

    public void musteriler() throws SQLException, IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("musteriler.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 402, 371);
        stage.setTitle("Müşteriler");
        stage.setScene(scene);
        stage.show();

    }

    public void malzemeler() throws SQLException, IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("malzemeler.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 305, 371);
        stage.setTitle("Malzemeler");
        stage.setScene(scene);
        stage.show();

    }

    public void siparisler() throws SQLException, IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("siparisler.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 607, 371);
        stage.setTitle("Siparişler");
        stage.setScene(scene);
        stage.show();

    }

    public void urunIcerigi() throws SQLException, IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("urunMalzemeleri.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 195, 371);
        stage.setTitle("Ürün İçeriği");
        stage.setScene(scene);
        stage.show();

    }







}
