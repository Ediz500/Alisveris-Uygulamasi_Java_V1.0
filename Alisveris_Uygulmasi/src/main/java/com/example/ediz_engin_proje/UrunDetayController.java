package com.example.ediz_engin_proje;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class UrunDetayController implements Initializable {

    @FXML
    private Label urunAdiLabel;
    @FXML
    private ListView<String> malzemelerListView;
    @FXML
    private ListView<Integer> malzemeIDListView;
    @FXML
    private ListView<Integer> urunMalzemeMiktarListView;

    @FXML
    private ListView<Button> silButonlari;
    @FXML
    private Button cikisButton;
    @FXML
    private Button kaydetButton;
    @FXML
    private Button yeniUrunButton;

    private ObservableList<String> malzemeAdlari = FXCollections.observableArrayList();
    private ObservableList<Integer> malzemeIDleri = FXCollections.observableArrayList();
    private ObservableList<Integer> urunMalzemeMiktarları = FXCollections.observableArrayList();

    private ObservableList<Button> silButonListesi = FXCollections.observableArrayList();

    private String selectedUrunAdi;
    private int selectedUrunID;

    // Veritabanı bağlantı bilgileri
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    private static final String USER = "BBB";
    private static final String PASSWORD = "BBB";

    public void setUrunAdi(String urunAdi) {
        this.selectedUrunAdi = urunAdi;
        urunAdiLabel.setText(urunAdi);
        try {
            // UrunAdi ayarlandıktan sonra UrunID'yi al ve malzemeleri listele
            selectedUrunID = getUrunIDByAdi(selectedUrunAdi);
            urunMalzemeleriListele();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getUrunIDByAdi(String urunAdi) throws SQLException {
        String sql = "SELECT UrunID FROM Urunler WHERE UrunAdi = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, urunAdi);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("UrunID");
            } else {
                throw new SQLException("Ürün adıyla eşleşen bir ürün bulunamadı.");
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        malzemelerListView.setItems(malzemeAdlari);
        malzemeIDListView.setItems(malzemeIDleri);
        urunMalzemeMiktarListView.setItems(urunMalzemeMiktarları);
        silButonlari.setItems(silButonListesi);

        // Eğer selectedUrunAdi null değilse, malzemeleri listele
        if (selectedUrunAdi != null && !selectedUrunAdi.isEmpty()) {
            try {
                urunMalzemeleriListele();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void urunMalzemeleriListele() throws SQLException {
        if (selectedUrunAdi == null || selectedUrunAdi.isEmpty()) {
            return; // Eğer selectedUrunAdi boş ise listeyi doldurma
        }

        String sql = "SELECT m.MalzemeAdi, m.MalzemeID, um.UrunMalzemeMiktari " +
                "FROM UrunMalzemeleri um " +
                "JOIN Malzemeler m ON um.MalzemeID = m.MalzemeID " +
                "WHERE um.UrunID = (SELECT UrunID FROM Urunler WHERE UrunAdi = ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, selectedUrunAdi);
            ResultSet rs = ps.executeQuery();

            malzemeAdlari.clear();
            malzemeIDleri.clear();
            urunMalzemeMiktarları.clear();
            silButonListesi.clear();

            while (rs.next()) {
                String malzemeAdi = rs.getString("MalzemeAdi");
                int malzemeID = rs.getInt("MalzemeID");
                int urunMalzemeMiktari = rs.getInt("UrunMalzemeMiktari");

                Button silButonu = new Button("Sil");
                silButonu.setOnAction(event -> {
                    try {
                        deleteMalzeme(malzemeID);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

                malzemeAdlari.add(malzemeAdi);
                malzemeIDleri.add(malzemeID);
                urunMalzemeMiktarları.add(urunMalzemeMiktari);
                silButonListesi.add(silButonu);
            }
        }
    }

    private void deleteMalzeme(int malzemeID) throws SQLException {
        String sql = "DELETE FROM UrunMalzemeleri WHERE UrunID = (SELECT UrunID FROM Urunler WHERE UrunAdi = ?) AND MalzemeID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, selectedUrunAdi);
            ps.setInt(2, malzemeID);
            ps.executeUpdate();
            urunMalzemeleriListele();  // Listeyi yeniden yükle
        }
    }

    @FXML
    private void kaydet() {
        // Kaydetme işlemleri yapılabilir
    }

    @FXML
    private void cikis() {
        Stage stage = (Stage) cikisButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void yeniMalzemeEkle() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("YeniMalzemeEkle.fxml"));
        Parent root = loader.load();  // FXML dosyasını yalnızca bir kez yükle

        // Yeni sayfa kontrolünü al
        YeniMalzemeEkleController controller = loader.getController();
        controller.setUrunID(selectedUrunID);  // Detay sayfasına ürün adını aktar

        // Yeni pencereyi aç
        Stage stage = new Stage();
        stage.setScene(new Scene(root));  // Kök öğeyi kullanarak yeni sahne oluştur
        stage.show();
    }
}
