package com.example.ediz_engin_proje;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class YeniMalzemeEkleController implements Initializable {

    private int selectedUrunID;  // Seçilen UrunID

    @FXML
    private ComboBox<String> malzemeComboBox;  // Malzeme seçimi için ComboBox
    @FXML
    private TextField miktarTextField;  // Miktar girişi için TextField

    private ObservableList<String> malzemeComboBoxItems = FXCollections.observableArrayList();

    // Veritabanı bağlantı bilgileri
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    private static final String USER = "BBB";
    private static final String PASSWORD = "BBB";

    // UrunID'yi dışarıdan alıyoruz
    public void setUrunID(Integer urunID) {
        this.selectedUrunID = urunID;
    }

    // Veritabanındaki malzemeleri listele
    private void veritabanindakiMalzemeleriListele() throws SQLException {
        String sql = "SELECT MalzemeAdi FROM Malzemeler";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            malzemeComboBoxItems.clear();

            while (rs.next()) {
                String malzemeAdi = rs.getString("MalzemeAdi");
                malzemeComboBoxItems.add(malzemeAdi);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // "Ekle" butonuna tıklanınca malzeme ekleme işlemi yapılır
    @FXML
    private void ekleMalzeme() throws SQLException {
        String selectedMalzemeAdi = malzemeComboBox.getValue();  // Seçilen malzeme
        String miktarStr = miktarTextField.getText();  // Girilen miktar

        if (selectedMalzemeAdi == null || miktarStr.isEmpty()) {
            // Kullanıcı malzeme veya miktar girmemişse uyarı verebilirsiniz
            showAlert("Lütfen bir malzeme seçin ve miktar girin!");
            return;
        }

        int miktar;
        try {
            miktar = Integer.parseInt(miktarStr);
        } catch (NumberFormatException e) {
            showAlert("Geçersiz miktar girdiniz!");
            return;
        }

        // Malzeme ID'sini almak için
        String sql = "SELECT MalzemeID FROM Malzemeler WHERE MalzemeAdi = ?";
        int malzemeID = -1;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, selectedMalzemeAdi);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                malzemeID = rs.getInt("MalzemeID");
            }
        }

        if (malzemeID == -1) {
            showAlert("Seçilen malzeme veritabanında bulunamadı!");
            return;
        }

        // Malzemeyi ürüne ekleyin
        String insertSQL = "INSERT INTO UrunMalzemeleri (UrunID, MalzemeID, UrunMalzemeMiktari) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(insertSQL)) {
            // UrunID'yi burada kullanıyoruz
            ps.setInt(1, selectedUrunID);  // Seçilen UrunID
            ps.setInt(2, malzemeID);  // Seçilen malzeme ID'si
            ps.setInt(3, miktar);  // Seçilen miktar
            ps.executeUpdate();
            showAlert("Yeni malzeme başarıyla eklendi!");
        } catch (SQLException e) {
            showAlert("Malzeme eklerken bir hata oluştu.");
            e.printStackTrace();
        }
    }

    // İptal butonuna tıklanınca pencereyi kapatıyoruz
    @FXML
    private void iptal() {
        Stage stage = (Stage) malzemeComboBox.getScene().getWindow();
        stage.close();
    }

    // Uyarı mesajı göstermek için
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bilgi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        malzemeComboBox.setItems(malzemeComboBoxItems);
        try {
            veritabanindakiMalzemeleriListele();  // Veritabanındaki malzemeleri listele
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void cikis() {
        Stage stage = (Stage) malzemeComboBox.getScene().getWindow();
        stage.close();  // Pencereyi kapat
    }

}
