package com.example.ediz_engin_proje;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class UrunIcerigi implements Initializable {

    String URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    String USER = "BBB";  // Kullanıcı adı
    String PASSWORD = "BBB";  // Şifre

    @FXML
    Button cikis;

    @FXML
    private Label uyari;

    @FXML
    private ListView<Integer> IDler;  // ID'leri göstermek için ListView

    @FXML
    private ListView<String> urunler;

    private ObservableList<Integer> idListesi = FXCollections.observableArrayList();
    private ObservableList<String> urunListesi = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            urunleriListele(null);

            IDler.setItems(idListesi);
            urunler.setItems(urunListesi);

            // ListView'e çift tıklama olayı ekleyin
            urunler.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {  // Çift tıklama kontrolü
                    String selectedUrun = urunler.getSelectionModel().getSelectedItem();
                    if (selectedUrun != null) {
                        try {
                            urunDetaySayfasiniAc(selectedUrun);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

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

            while (rs.next()) {
                int urunID = rs.getInt("UrunID");
                String urunAdi = rs.getString("UrunAdi");

                idListesi.add(urunID);
                urunListesi.add(urunAdi);
            }
        }
    }

    public void isimeGoreSirala() throws SQLException {
        urunleriListele("UrunAdi ASC");
    }



    public void cikis() throws IOException {
        Stage stage1 = (Stage) uyari.getScene().getWindow();
        stage1.close();
    }

    private void urunDetaySayfasiniAc(String urunAdi) throws IOException {
        // Yeni bir pencere açmak için FXML dosyasını yükle
        FXMLLoader loader = new FXMLLoader(getClass().getResource("UrunDetay.fxml"));  // UrunDetay.fxml, açmak istediğiniz sayfa
        Parent root = loader.load();

        // Yeni sayfa kontrolünü al
        UrunDetayController controller = loader.getController();
        controller.setUrunAdi(urunAdi);  // Detay sayfasına ürün adını aktar

        // Yeni sahneyi oluştur ve göster
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
