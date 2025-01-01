package com.example.ediz_engin_proje;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class Malzemeler implements Initializable {

    String URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    String USER = "BBB";  // Kullanıcı adı
    String PASSWORD = "BBB";  // Şifre

    @FXML
    private ListView<Integer> IDler;

    @FXML
    private ListView<String> malzemeAdlari;

    @FXML
    private ListView<Integer> miktarlar;

    @FXML
    private Button kaydet;

    @FXML
    private Button cikis;

    @FXML
    private Button yeniEkle;

    @FXML
    private Label uyari;

    private ObservableList<Integer> idListesi = FXCollections.observableArrayList();
    private ObservableList<String> adListesi = FXCollections.observableArrayList();
    private ObservableList<Integer> miktarListesi = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            malzemeleriListele(null);

            IDler.setItems(idListesi);
            malzemeAdlari.setItems(adListesi);
            miktarlar.setItems(miktarListesi);

            // Çift tıklama olayları
            malzemeAdlari.setOnMouseClicked(event -> duzenleOlayi(event, "ad"));
            miktarlar.setOnMouseClicked(event -> duzenleOlayi(event, "miktar"));
        } catch (SQLException e) {
            uyari.setText("Başlatma sırasında bir hata oluştu.");
            e.printStackTrace();
        }
    }

    private void malzemeleriListele(String orderBy) throws SQLException {
        String sql = "SELECT MalzemeID, MalzemeAdi, MalzemeMiktar FROM Malzemeler";
        if (orderBy != null) {
            sql += " ORDER BY " + orderBy;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            idListesi.clear();
            adListesi.clear();
            miktarListesi.clear();

            while (rs.next()) {
                int malzemeID = rs.getInt("MalzemeID");
                String malzemeAdi = rs.getString("MalzemeAdi");
                int malzemeMiktar = rs.getInt("MalzemeMiktar");

                idListesi.add(malzemeID);
                adListesi.add(malzemeAdi);
                miktarListesi.add(malzemeMiktar);
            }
        }
    }

    private void duzenleOlayi(MouseEvent event, String tip) {
        if (event.getClickCount() == 2) { // Çift tıklama
            int selectedIndex;
            TextInputDialog dialog = new TextInputDialog();

            switch (tip) {
                case "ad":
                    selectedIndex = malzemeAdlari.getSelectionModel().getSelectedIndex();
                    if (selectedIndex >= 0) {
                        dialog.setTitle("Malzeme Adı Düzenleme");
                        dialog.setHeaderText("Malzeme Adını Düzenleyin");
                        dialog.setContentText("Yeni Malzeme Adı:");
                        dialog.showAndWait().ifPresent(yeniDeger -> adListesi.set(selectedIndex, yeniDeger));
                    }
                    break;

                case "miktar":
                    selectedIndex = miktarlar.getSelectionModel().getSelectedIndex();
                    if (selectedIndex >= 0) {
                        dialog.setTitle("Malzeme Miktarı Düzenleme");
                        dialog.setHeaderText("Malzeme Miktarını Düzenleyin");
                        dialog.setContentText("Yeni Malzeme Miktarı:");
                        dialog.showAndWait().ifPresent(yeniDeger -> {
                            try {
                                miktarListesi.set(selectedIndex, Integer.parseInt(yeniDeger));
                            } catch (NumberFormatException e) {
                                uyari.setText("Geçersiz miktar girdiniz!");
                            }
                        });
                    }
                    break;
            }
        }
    }

    public void kaydet() throws SQLException {
        String sql = "UPDATE Malzemeler SET MalzemeAdi = ?, MalzemeMiktar = ? WHERE MalzemeID = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = conn.prepareStatement(sql)) {

            for (int i = 0; i < adListesi.size(); i++) {
                String yeniAd = adListesi.get(i);
                int yeniMiktar = miktarListesi.get(i);
                int malzemeID = idListesi.get(i);

                statement.setString(1, yeniAd);
                statement.setInt(2, yeniMiktar);
                statement.setInt(3, malzemeID);
                statement.executeUpdate();
            }
            uyari.setText("Malzemeler başarıyla güncellendi!");
        } catch (SQLException e) {
            uyari.setText("Güncelleme sırasında hata oluştu.");
            e.printStackTrace();
        }
    }

    public void yeniMalzemeEkle() throws SQLException {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Yeni Malzeme Ekle");
        dialog.setHeaderText("Yeni Malzeme Bilgileri");

        // Malzeme Adı
        dialog.setContentText("Malzeme Adı:");
        Optional<String> yeniAd = dialog.showAndWait();
        if (!yeniAd.isPresent()) return;

        // Malzeme Miktarı
        dialog.setContentText("Malzeme Miktarı:");
        Optional<String> yeniMiktarStr = dialog.showAndWait();
        if (!yeniMiktarStr.isPresent()) return;

        int yeniMiktar;
        try {
            yeniMiktar = Integer.parseInt(yeniMiktarStr.get());
        } catch (NumberFormatException e) {
            uyari.setText("Geçersiz miktar girdiniz!");
            return;
        }

        String sql = "INSERT INTO Malzemeler (MalzemeAdi, MalzemeMiktar) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, yeniAd.get());
            statement.setInt(2, yeniMiktar);
            statement.executeUpdate();
            uyari.setText("Yeni malzeme başarıyla eklendi!");
            malzemeleriListele(null);
        } catch (SQLException e) {
            uyari.setText("Malzeme ekleme sırasında hata oluştu.");
            e.printStackTrace();
        }
    }

    public void isimeGoreSirala() throws SQLException {
        malzemeleriListele("MalzemeAdi ASC");
    }

    public void miktaraGoreSirala() throws SQLException {
        malzemeleriListele("MalzemeMiktar ASC");
    }

    public void cikis() throws IOException {
        Stage stage1 = (Stage) uyari.getScene().getWindow();
        stage1.close();
    }
}
