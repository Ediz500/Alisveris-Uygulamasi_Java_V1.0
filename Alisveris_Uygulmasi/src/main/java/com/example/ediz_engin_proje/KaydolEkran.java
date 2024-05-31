package com.example.ediz_engin_proje;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class KaydolEkran extends Ekran{
    @FXML
    Label uyari;
    @FXML
    TextField loginUsername;
    @FXML
    TextField loginPassword;
    @FXML
    public void kayitol() throws IOException {
        String username = loginUsername.getText();
        String password = loginPassword.getText();
        if (musteriKontrol(username)) {
            uyari.setText("Kullanıcı Adı Kullanılıyor.");
        }else {
            if (Objects.equals(username, "") || Objects.equals(password, "")) {
                uyari.setText("Kullanıcı Adı ya da Şifre Boş Olamaz.");
            } else {
                try (FileWriter writer = new FileWriter("musteriler.txt", true)) {
                    writer.write(username + ":" + password + ":0\n");
                } catch (Exception e) {
                    uyari.setText("Hata Meydana Geldi.");
                }
                Stage stage1 = (Stage) uyari.getScene().getWindow();
                stage1.close();
            }
        }
    }
    private boolean musteriKontrol(String girilenAd) {
        try (BufferedReader reader = new BufferedReader(new FileReader("musteriler.txt"))) {
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
}

