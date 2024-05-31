package com.example.ediz_engin_proje;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class Ekran {
    Label uyari;
    public void yenile() throws IOException {
    }
    public void yaz() throws IOException {
    }
    public void cikis() throws IOException {
        Stage stage1 = (Stage) uyari.getScene().getWindow();
        stage1.close();
    }
}
