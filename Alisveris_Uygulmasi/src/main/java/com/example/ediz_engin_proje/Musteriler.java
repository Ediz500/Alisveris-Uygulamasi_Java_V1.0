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
import java.util.ResourceBundle;

public class Musteriler implements Initializable {

    String URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    String USER = "BBB";  // Kullanıcı adı
    String PASSWORD = "BBB";  // Şifre

    @FXML
    Button cikis;

    @FXML
    Button kaydet;

    @FXML
    private Label uyari;

    @FXML
    private ListView<Integer> IDler;  // MusteriID'leri göstermek için ListView

    @FXML
    private ListView<String> musteriler;
    @FXML
    private ListView<String> sifreler;  // Şifreler ListView
    @FXML
    private ListView<Double> bakiyeler;

    private ObservableList<Integer> idListesi = FXCollections.observableArrayList();
    private ObservableList<String> musteriListesi = FXCollections.observableArrayList();
    private ObservableList<String> sifreListesi = FXCollections.observableArrayList();
    private ObservableList<Double> bakiyeListesi = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            musterileriListele(null);

            IDler.setItems(idListesi);
            musteriler.setItems(musteriListesi);
            sifreler.setItems(sifreListesi);
            bakiyeler.setItems(bakiyeListesi);

            // Çift tıklama olayları
            musteriler.setOnMouseClicked(event -> duzenleOlayi(event, "musteriAdi"));
            sifreler.setOnMouseClicked(event -> duzenleOlayi(event, "sifre"));
            bakiyeler.setOnMouseClicked(event -> duzenleOlayi(event, "bakiye"));
        } catch (SQLException e) {
            uyari.setText("Başlatma sırasında bir hata oluştu.");
            e.printStackTrace();
        }
    }

    private void musterileriListele(String orderBy) throws SQLException {
        String sql = "SELECT MusteriID, MusteriAdi, MusteriSifresi, Bakiye FROM Musteriler";
        if (orderBy != null) {
            sql += " ORDER BY " + orderBy;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            idListesi.clear();
            musteriListesi.clear();
            sifreListesi.clear();
            bakiyeListesi.clear();

            while (rs.next()) {
                int musteriID = rs.getInt("MusteriID");
                String musteriAdi = rs.getString("MusteriAdi");
                String sifre = rs.getString("MusteriSifresi");
                double bakiye = rs.getDouble("Bakiye");

                idListesi.add(musteriID);
                musteriListesi.add(musteriAdi);
                sifreListesi.add(sifre);
                bakiyeListesi.add(bakiye);
            }
        }
    }

    // Çift tıklama ile düzenleme
    private void duzenleOlayi(MouseEvent event, String tip) {
        if (event.getClickCount() == 2) { // Çift tıklama
            int selectedIndex;
            TextInputDialog dialog = new TextInputDialog();

            switch (tip) {
                case "musteriAdi":
                    selectedIndex = musteriler.getSelectionModel().getSelectedIndex();
                    if (selectedIndex >= 0) {
                        dialog.setTitle("Müşteri Adı Düzenleme");
                        dialog.setHeaderText("Müşteriyi Düzenleyin");
                        dialog.setContentText("Yeni Müşteri Adı:");
                        dialog.showAndWait().ifPresent(yeniDeger -> musteriListesi.set(selectedIndex, yeniDeger));
                    }
                    break;

                case "sifre":
                    selectedIndex = sifreler.getSelectionModel().getSelectedIndex();
                    if (selectedIndex >= 0) {
                        dialog.setTitle("Şifre Düzenleme");
                        dialog.setHeaderText("Şifreyi Düzenleyin");
                        dialog.setContentText("Yeni Şifre:");
                        dialog.showAndWait().ifPresent(yeniDeger -> sifreListesi.set(selectedIndex, yeniDeger));
                    }
                    break;

                case "bakiye":
                    selectedIndex = bakiyeler.getSelectionModel().getSelectedIndex();
                    if (selectedIndex >= 0) {
                        dialog.setTitle("Bakiye Düzenleme");
                        dialog.setHeaderText("Bakiye Miktarını Düzenleyin");
                        dialog.setContentText("Yeni Bakiye:");
                        dialog.showAndWait().ifPresent(yeniDeger -> {
                            try {
                                bakiyeListesi.set(selectedIndex, Double.parseDouble(yeniDeger));
                            } catch (NumberFormatException e) {
                                uyari.setText("Geçersiz bakiye miktarı!");
                            }
                        });
                    }
                    break;
            }
        }
    }

    // Kaydet butonuna basıldığında SQL'e güncelleme
    public void kaydet() throws SQLException {
        String sql = "UPDATE Musteriler SET MusteriAdi = ?, MusteriSifresi = ?, Bakiye = ? WHERE MusteriID = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = conn.prepareStatement(sql)) {

            for (int i = 0; i < musteriListesi.size(); i++) {
                String yeniMusteriAdi = musteriListesi.get(i);
                String yeniSifre = sifreListesi.get(i);
                double yeniBakiye = bakiyeListesi.get(i);
                int musteriID = idListesi.get(i);  // Güncellemede kullanılacak ID

                statement.setString(1, yeniMusteriAdi);
                statement.setString(2, yeniSifre);
                statement.setDouble(3, yeniBakiye);
                statement.setInt(4, musteriID);
                statement.executeUpdate();
            }
            uyari.setText("Veriler başarıyla güncellendi!");
        } catch (SQLException e) {
            uyari.setText("Veri güncelleme sırasında hata oluştu.");
            e.printStackTrace();
        }
    }

    public void isimeGoreSirala() throws SQLException {
        musterileriListele("MusteriAdi ASC");
    }

    public void bakiyeSiralaAzalan() throws SQLException {
        musterileriListele("Bakiye DESC");
    }

    public void cikis() throws IOException {
        Stage stage1 = (Stage) uyari.getScene().getWindow();
        stage1.close();
    }
}
