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

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AdminEkleEkran extends Ekran implements Initializable {
    @FXML
    Label uyari;
    @FXML
    TextField loginUsername;
    @FXML
    TextField loginPassword;
    @FXML
    ListView<String> adminler = new ListView<>();
    @FXML
    ListView<Button> butonlar = new ListView<>();

    @FXML
    public void adminEkle() throws IOException {
        String username = loginUsername.getText();
        String password = loginPassword.getText();
        if (adminKontrol(username)) {
            uyari.setText("Kullanıcı Adı Kullanılıyor.");
        }else {
            if (Objects.equals(username, "") || Objects.equals(password, "")) {
                uyari.setText("Kullanıcı Adı ya da Şifre Boş Olamaz.");
            } else {
                try (FileWriter writer = new FileWriter("adminler.txt", true)) {
                    writer.write(username + ":" + password + "\n");
                } catch (Exception e) {
                    uyari.setText("Hata Meydana Geldi.");
                }
                adminler.getItems().clear();
                butonlar.getItems().clear();
                yaz();
            }
        }
    }
    private boolean adminKontrol(String girilenAd) {
        try (BufferedReader reader = new BufferedReader(new FileReader("adminler.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
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
    public void cikis(){
        Stage stage1 = (Stage) uyari.getScene().getWindow();
        stage1.close();
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        yaz();
    }
    @Override
    public void yaz(){
        try (BufferedReader reader = new BufferedReader(new FileReader("adminler.txt"))) {
            String line;
            int satir =0;
            while ((line = reader.readLine()) != null) {
                String[] kullanici = line.split(":");
                adminler.getItems().add(kullanici[0].trim());
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

            }reader.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void silButton(int satir) throws IOException {
        adminler.getItems().remove(satir);
        butonlar.getItems().remove(satir);
        try {
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader("adminler.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);

                }
                br.close();}
            if (satir >= 0 && satir <= lines.size()) {
                lines.remove(satir);
            } else {
                System.out.println("Geçersiz satır numarası.");
                return;
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("adminler.txt"))) {
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            bw.close();}
            System.out.println("Dosya başarıyla güncellendi.");

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            yenile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void yenile() throws IOException {
        Stage stage1 = (Stage) uyari.getScene().getWindow();
        stage1.close();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("adminEkleEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 420, 457);
        stage.setTitle("Admin Ekle/Çıkar");
        stage.setScene(scene);
        stage.show();
    }
}
