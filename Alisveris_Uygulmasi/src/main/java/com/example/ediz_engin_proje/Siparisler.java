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

public class Siparisler implements Initializable {

    String URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    String USER = "BBB";  // Kullanıcı adı
    String PASSWORD = "BBB";  // Şifre

    @FXML
    private ListView<Integer> siparisIDler;

    @FXML
    private ListView<Integer> musteriIDler;

    @FXML
    private ListView<String> musteriAdlari;

    @FXML
    private ListView<String> siparisTarihleri;

    @FXML
    private ListView<Double> tutarlar;

    @FXML
    private ListView<CheckBox> siparisDurumlari;

    @FXML
    private ListView<Button> silButonlari;

    @FXML
    private Button kaydet;

    @FXML
    private Button cikis;

    @FXML
    private Label uyari;

    private ObservableList<Integer> siparisIDListesi = FXCollections.observableArrayList();
    private ObservableList<Integer> musteriIDListesi = FXCollections.observableArrayList();
    private ObservableList<String> musteriAdiListesi = FXCollections.observableArrayList();
    private ObservableList<String> siparisTarihiListesi = FXCollections.observableArrayList();
    private ObservableList<Double> tutarListesi = FXCollections.observableArrayList();
    private ObservableList<CheckBox> siparisDurumListesi = FXCollections.observableArrayList();
    private ObservableList<Button> silButonListesi = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            siparisleriListele();

            siparisIDler.setItems(siparisIDListesi);
            musteriIDler.setItems(musteriIDListesi);
            musteriAdlari.setItems(musteriAdiListesi);
            siparisTarihleri.setItems(siparisTarihiListesi);
            tutarlar.setItems(tutarListesi);
            siparisDurumlari.setItems(siparisDurumListesi);
            silButonlari.setItems(silButonListesi);

            // Sipariş ID'ye çift tıklama olayı
            siparisIDler.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) { // Çift tıklama
                    int selectedIndex = siparisIDler.getSelectionModel().getSelectedIndex();
                    if (selectedIndex >= 0) {
                        int siparisID = siparisIDListesi.get(selectedIndex);
                        try {
                            siparisUrunleriniGoster(siparisID);
                        } catch (SQLException e) {
                            uyari.setText("Sipariş ürünlerini yüklerken hata oluştu.");
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (SQLException e) {
            uyari.setText("Siparişler yüklenirken hata oluştu.");
            e.printStackTrace();
        }
    }

    private void siparisleriListele() throws SQLException {
        String sql = """
                SELECT s.SiparisID, s.MusteriID, m.MusteriAdi, s.SiparisTarihi, s.Tutar, s.SiparisDurumu
                FROM Siparisler s
                JOIN Musteriler m ON s.MusteriID = m.MusteriID
                """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            siparisIDListesi.clear();
            musteriIDListesi.clear();
            musteriAdiListesi.clear();
            siparisTarihiListesi.clear();
            tutarListesi.clear();
            siparisDurumListesi.clear();
            silButonListesi.clear();

            while (rs.next()) {
                int siparisID = rs.getInt("SiparisID");
                int musteriID = rs.getInt("MusteriID");
                String musteriAdi = rs.getString("MusteriAdi");
                String siparisTarihi = rs.getDate("SiparisTarihi").toString();
                double tutar = rs.getDouble("Tutar");
                boolean siparisDurumu = rs.getBoolean("SiparisDurumu");

                CheckBox durumCheckBox = new CheckBox();
                durumCheckBox.setSelected(siparisDurumu);

                Button silButonu = new Button("Sil");
                silButonu.setOnAction(event -> {
                    try {
                        siparisiSil(siparisID);
                        siparisleriListele(); // Listeyi güncelle
                    } catch (SQLException e) {
                        uyari.setText("Sipariş silinirken hata oluştu.");
                        e.printStackTrace();
                    }
                });

                siparisIDListesi.add(siparisID);
                musteriIDListesi.add(musteriID);
                musteriAdiListesi.add(musteriAdi);
                siparisTarihiListesi.add(siparisTarihi);
                tutarListesi.add(tutar);
                siparisDurumListesi.add(durumCheckBox);
                silButonListesi.add(silButonu);
            }
        }
    }

    private void siparisiSil(int siparisID) throws SQLException {
        String deleteUrunlerSql = "DELETE FROM SiparisUrunleri WHERE SiparisID = ?";
        String deleteSiparisSql = "DELETE FROM Siparisler WHERE SiparisID = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // İlk olarak, SiparisUrunleri tablosundaki sipariş ürünlerini sil
            try (PreparedStatement deleteUrunlerStmt = conn.prepareStatement(deleteUrunlerSql)) {
                deleteUrunlerStmt.setInt(1, siparisID);
                deleteUrunlerStmt.executeUpdate();
            }

            // Sonra, Siparisler tablosundaki siparişi sil
            try (PreparedStatement deleteSiparisStmt = conn.prepareStatement(deleteSiparisSql)) {
                deleteSiparisStmt.setInt(1, siparisID);
                deleteSiparisStmt.executeUpdate();
            }
        }
    }

    private void siparisUrunleriniGoster(int siparisID) throws SQLException {
        String sql = """
                SELECT u.UrunAdi, su.SiparisUrunMiktari
                FROM SiparisUrunleri su
                JOIN Urunler u ON su.UrunID = u.UrunID
                WHERE su.SiparisID = ?
                """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, siparisID);

            try (ResultSet rs = statement.executeQuery()) {
                StringBuilder urunBilgisi = new StringBuilder("Sipariş Ürünleri:\n");
                while (rs.next()) {
                    String urunAdi = rs.getString("UrunAdi");
                    int miktar = rs.getInt("SiparisUrunMiktari");
                    urunBilgisi.append("- ").append(urunAdi).append(": ").append(miktar).append("\n");
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sipariş Ürünleri");
                alert.setHeaderText("Sipariş ID: " + siparisID);
                alert.setContentText(urunBilgisi.toString());
                alert.showAndWait();
            }
        }
    }

    public void kaydet() throws SQLException {
        String sql = "UPDATE Siparisler SET SiparisDurumu = ? WHERE SiparisID = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = conn.prepareStatement(sql)) {

            for (int i = 0; i < siparisIDListesi.size(); i++) {
                int siparisID = siparisIDListesi.get(i);
                boolean durum = siparisDurumListesi.get(i).isSelected();

                statement.setBoolean(1, durum);
                statement.setInt(2, siparisID);
                statement.executeUpdate();
            }
            uyari.setText("Sipariş durumları başarıyla güncellendi!");
        } catch (SQLException e) {
            uyari.setText("Güncelleme sırasında hata oluştu.");
            e.printStackTrace();
        }
    }

    public void cikis() throws IOException {
        Stage stage1 = (Stage) uyari.getScene().getWindow();
        stage1.close();
    }
}
