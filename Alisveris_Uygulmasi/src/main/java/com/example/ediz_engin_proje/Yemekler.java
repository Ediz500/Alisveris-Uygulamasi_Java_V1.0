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

public class Yemekler implements Initializable {

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
    private ListView<Integer> IDler;  // ID'leri göstermek için ListView

    @FXML
    private ListView<String> urunler;
    @FXML
    private ListView<Integer> stoklar;
    @FXML
    private ListView<Double> fiyatlar;

    private ObservableList<Integer> idListesi = FXCollections.observableArrayList();
    private ObservableList<String> urunListesi = FXCollections.observableArrayList();
    private ObservableList<Integer> stokListesi = FXCollections.observableArrayList();
    private ObservableList<Double> fiyatListesi = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            urunleriListele(null);

            IDler.setItems(idListesi);
            urunler.setItems(urunListesi);
            stoklar.setItems(stokListesi);
            fiyatlar.setItems(fiyatListesi);

            // Çift tıklama olayları
            urunler.setOnMouseClicked(event -> duzenleOlayi(event, "urun"));
            stoklar.setOnMouseClicked(event -> duzenleOlayi(event, "stok"));
            fiyatlar.setOnMouseClicked(event -> duzenleOlayi(event, "fiyat"));
        } catch (SQLException e) {
            uyari.setText("Başlatma sırasında bir hata oluştu.");
            e.printStackTrace();
        }
    }

    private void urunleriListele(String orderBy) throws SQLException {
        String sql = "SELECT UrunID, UrunAdi, UrunMiktar, Fiyat FROM Urunler";
        if (orderBy != null) {
            sql += " ORDER BY " + orderBy;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            idListesi.clear();
            urunListesi.clear();
            stokListesi.clear();
            fiyatListesi.clear();

            while (rs.next()) {
                int urunID = rs.getInt("UrunID");
                String urunAdi = rs.getString("UrunAdi");
                int stok = rs.getInt("UrunMiktar");
                double fiyat = rs.getDouble("Fiyat");

                idListesi.add(urunID);
                urunListesi.add(urunAdi);
                stokListesi.add(stok);
                fiyatListesi.add(fiyat);
            }
        }
    }

    // Çift tıklama ile düzenleme
    private void duzenleOlayi(MouseEvent event, String tip) {
        if (event.getClickCount() == 2) { // Çift tıklama
            int selectedIndex;
            TextInputDialog dialog = new TextInputDialog();

            switch (tip) {
                case "urun":
                    selectedIndex = urunler.getSelectionModel().getSelectedIndex();
                    if (selectedIndex >= 0) {
                        dialog.setTitle("Ürün Adı Düzenleme");
                        dialog.setHeaderText("Ürünü Düzenleyin");
                        dialog.setContentText("Yeni Ürün Adı:");
                        dialog.showAndWait().ifPresent(yeniDeger -> urunListesi.set(selectedIndex, yeniDeger));
                    }
                    break;

                case "stok":
                    selectedIndex = stoklar.getSelectionModel().getSelectedIndex();
                    if (selectedIndex >= 0) {
                        dialog.setTitle("Stok Düzenleme");
                        dialog.setHeaderText("Stok Miktarını Düzenleyin");
                        dialog.setContentText("Yeni Stok:");
                        dialog.showAndWait().ifPresent(yeniDeger -> {
                            try {
                                stokListesi.set(selectedIndex, Integer.parseInt(yeniDeger));
                            } catch (NumberFormatException e) {
                                uyari.setText("Geçersiz stok miktarı!");
                            }
                        });
                    }
                    break;

                case "fiyat":
                    selectedIndex = fiyatlar.getSelectionModel().getSelectedIndex();
                    if (selectedIndex >= 0) {
                        dialog.setTitle("Fiyat Düzenleme");
                        dialog.setHeaderText("Fiyatı Düzenleyin");
                        dialog.setContentText("Yeni Fiyat:");
                        dialog.showAndWait().ifPresent(yeniDeger -> {
                            try {
                                fiyatListesi.set(selectedIndex, Double.parseDouble(yeniDeger));
                            } catch (NumberFormatException e) {
                                uyari.setText("Geçersiz fiyat!");
                            }
                        });
                    }
                    break;
            }
        }
    }

    // Kaydet butonuna basıldığında SQL'e güncelleme
    public void kaydet() throws SQLException {
        String sql = "UPDATE Urunler SET UrunAdi = ?, UrunMiktar = ?, Fiyat = ? WHERE UrunID = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = conn.prepareStatement(sql)) {

            for (int i = 0; i < urunListesi.size(); i++) {
                String yeniUrunAdi = urunListesi.get(i);
                int yeniStok = stokListesi.get(i);
                double yeniFiyat = fiyatListesi.get(i);
                int urunID = idListesi.get(i);  // Güncellemede kullanılacak ID

                statement.setString(1, yeniUrunAdi);
                statement.setInt(2, yeniStok);
                statement.setDouble(3, yeniFiyat);
                statement.setInt(4, urunID);
                statement.executeUpdate();
            }
            uyari.setText("Veriler başarıyla güncellendi!");
        } catch (SQLException e) {
            uyari.setText("Veri güncelleme sırasında hata oluştu.");
            e.printStackTrace();
        }
    }

    public void fiyatSiralaAzalan() throws SQLException {
        urunleriListele("Fiyat DESC");
    }

    public void isimeGoreSirala() throws SQLException {
        urunleriListele("UrunAdi ASC");
    }

    public void stokMiktarinaGoreSirala() throws SQLException {
        urunleriListele("UrunMiktar ASC");
    }

    public void cikis() throws IOException {
        Stage stage1 = (Stage) uyari.getScene().getWindow();
        stage1.close();
    }
}
