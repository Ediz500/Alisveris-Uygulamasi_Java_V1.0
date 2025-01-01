package com.example.ediz_engin_proje;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class SepetEkran extends Ekran implements Initializable {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    private static final String USER = "BBB";
    private static final String PASSWORD = "BBB";

    @FXML
    private ListView<Double> toplam;
    @FXML
    private ListView<String> urunler;
    @FXML
    private ListView<Integer> stoklar;
    @FXML
    private ListView<Double> fiyatlar;
    @FXML
    private ListView<Button> butonlar;
    @FXML
    private Label bakiye;
    @FXML
    private Label uyari;
    @FXML
    private Label tutar;
    @FXML
    private Button sepetTemizle;

    private double toplamTutar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        toplamTutar = 0;
        bakiyeGoruntule();
        try {
            sepetiYazdir();
        } catch (SQLException e) {
            uyari.setText("Sepeti yüklerken bir hata oluştu.");
            e.printStackTrace();
        }
        gorunurYap();
    }

    private void bakiyeGoruntule() {
        bakiye.setText(String.format("%.2f", YuklemeEkran.Bakiye));
    }

    private void sepetiYazdir() throws SQLException {
        urunler.getItems().clear();
        stoklar.getItems().clear();
        fiyatlar.getItems().clear();
        toplam.getItems().clear();
        butonlar.getItems().clear();
        toplamTutar = 0;

        String sql = "SELECT UrunAdi, Adet, Fiyat FROM Sepet WHERE MusteriID = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, getMusteriID(MainController.aktifMusteri));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String urunAdi = rs.getString("UrunAdi");
                    int adet = rs.getInt("Adet");
                    double fiyat = rs.getDouble("Fiyat");
                    double toplamFiyat = fiyat * adet;

                    urunler.getItems().add(urunAdi);
                    stoklar.getItems().add(adet);
                    fiyatlar.getItems().add(fiyat);
                    toplam.getItems().add(toplamFiyat);

                    toplamTutar += toplamFiyat;

                    Button silButton = new Button("Sepetten Sil");
                    silButton.setOnAction(e -> {
                        try {
                            sepettenUrunSil(urunAdi);
                        } catch (SQLException ex) {
                            uyari.setText("Ürün silinirken bir hata oluştu.");
                            ex.printStackTrace();
                        }
                    });
                    butonlar.getItems().add(silButton);
                }
            }
        }

        tutar.setText(String.format("%.2f", toplamTutar));
    }

    @FXML
    public void bakiyeYukle() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("yuklemeEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 303, 345);
        stage.setTitle("Bakiye Yükle");
        stage.setScene(scene);
        stage.show();
    }
    private void sepettenUrunSil(String urunAdi) throws SQLException {
        String sql = "DELETE FROM Sepet WHERE MusteriID = ? AND UrunAdi = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, getMusteriID(MainController.aktifMusteri));
            stmt.setString(2, urunAdi);
            stmt.executeUpdate();
        }

        sepetiYazdir();
        uyari.setText("Ürün sepetten silindi.");
    }



    @FXML
    private void odemeYap() {
        if (toplamTutar == 0) {
            uyari.setText("Sepetiniz boş.");
            return;
        }

        if (YuklemeEkran.Bakiye < toplamTutar) {
            uyari.setText("Bakiyeniz yetersiz.");
            return;
        }

        // Yeni siparişi veritabanına kaydet
        try {
            siparisVer();
        } catch (SQLException e) {
            uyari.setText("Sipariş oluşturulurken bir hata oluştu.");
            e.printStackTrace();
            return;
        }

        // Bakiyeyi güncelle
        YuklemeEkran.Bakiye -= toplamTutar;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String updateQuery = "UPDATE Musteriler SET Bakiye = ? WHERE MusteriAdi = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setDouble(1, YuklemeEkran.Bakiye);
            stmt.setString(2, MainController.aktifMusteri); // Kullanıcı adı burada belirlenmeli
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Stok güncellemesi
            stokDus(conn); // Stok güncellemelerini buraya aktaralım

            // Sepeti temizle
            sepetTemizle();

            // Kişisel bakiye güncelleme
            YuklemeEkran.sahsiBakiye();

            yenile();

            // Başarı mesajı göster
            uyari.setText("Ödeme Başarılı");
        } catch (SQLException e) {
            uyari.setText("Ödeme sırasında bir hata oluştu.");
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sepetTemizle() throws SQLException {
        String sql = "DELETE FROM Sepet WHERE MusteriID = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, getMusteriID(MainController.aktifMusteri)); // Aktif müşteri ID'sini kullan
            stmt.executeUpdate();
        }
    }



    @FXML
    private void sepetiTemizle() throws SQLException {
        sepetTemizle();

        toplamTutar = 0;
        sepetiYazdir();
        uyari.setText("Sepet temizlendi.");
    }

    // Siparişi ve Sipariş Ürünlerini veritabanına kaydeden fonksiyon
    private void siparisVer() throws SQLException {
        // Siparişi Siparisler tablosuna kaydet
        String siparisSQL = "INSERT INTO Siparisler (MusteriID, SiparisTarihi, Tutar) VALUES (?, ?, ?)";
        double toplamTutar = this.toplamTutar; // Sepetteki toplam tutar
        Date siparisTarihi = new Date(System.currentTimeMillis()); // Bilgisayarın local saatini al

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement siparisStmt = conn.prepareStatement(siparisSQL, Statement.RETURN_GENERATED_KEYS)) {

            siparisStmt.setInt(1, getMusteriID(MainController.aktifMusteri));
            siparisStmt.setDate(2, siparisTarihi);
            siparisStmt.setDouble(3, toplamTutar);
            siparisStmt.executeUpdate();

            // Siparişin ID'sini almak için
            try (ResultSet generatedKeys = siparisStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int siparisID = generatedKeys.getInt(1);
                    // Şimdi siparişin ürünlerini SiparisUrunleri tablosuna kaydedelim
                    siparisUrunleriKaydet(conn, siparisID);
                }
            }
        }
    }

    // Siparişin ürünlerini SiparisUrunleri tablosuna kaydeden fonksiyon
    private void siparisUrunleriKaydet(Connection conn, int siparisID) throws SQLException {
        String siparisUrunSQL = "INSERT INTO SiparisUrunleri (SiparisID, UrunID, SiparisUrunMiktari) VALUES (?, ?, ?)";
        String sepetSQL = "SELECT UrunAdi, Adet FROM Sepet WHERE MusteriID = ?";

        try (PreparedStatement sepetStmt = conn.prepareStatement(sepetSQL)) {
            sepetStmt.setInt(1, getMusteriID(MainController.aktifMusteri));

            try (ResultSet rs = sepetStmt.executeQuery()) {
                while (rs.next()) {
                    String urunAdi = rs.getString("UrunAdi");
                    int adet = rs.getInt("Adet");

                    // UrunID'yi almak için
                    int urunID = getUrunID(urunAdi, conn);

                    try (PreparedStatement siparisUrunStmt = conn.prepareStatement(siparisUrunSQL)) {
                        siparisUrunStmt.setInt(1, siparisID);
                        siparisUrunStmt.setInt(2, urunID);
                        siparisUrunStmt.setInt(3, adet);
                        siparisUrunStmt.executeUpdate();
                    }
                }
            }
        }
    }

    // UrunAdı'na göre UrunID'yi döndüren fonksiyon
    private int getUrunID(String urunAdi, Connection conn) throws SQLException {
        String urunSQL = "SELECT UrunID FROM Urunler WHERE UrunAdi = ?";
        try (PreparedStatement stmt = conn.prepareStatement(urunSQL)) {
            stmt.setString(1, urunAdi);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("UrunID");
                }
            }
        }
        throw new SQLException("Ürün ID'si bulunamadı.");
    }

    @FXML
    private void geriGit() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("musteriEkran.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Ana Ekran");
        stage.show();
        ((Stage) urunler.getScene().getWindow()).close();
    }

    private int getMusteriID(String musteriAdi) throws SQLException {
        String sql = "SELECT MusteriID FROM Musteriler WHERE MusteriAdi = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, musteriAdi);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("MusteriID");
                }
            }
        }
        throw new SQLException("Müşteri ID bulunamadı.");
    }

    public void stokDus(Connection conn) {
        String stokDusSQL = "UPDATE Urunler SET UrunMiktar = UrunMiktar - ? WHERE UrunAdi = ?";
        String sepetSQL = "SELECT UrunAdi, Adet FROM Sepet WHERE MusteriID = ?";

        try (PreparedStatement sepetStmt = conn.prepareStatement(sepetSQL)) {
            sepetStmt.setInt(1, getMusteriID(MainController.aktifMusteri)); // Aktif müşterinin ID'sini al

            try (ResultSet rs = sepetStmt.executeQuery()) {
                while (rs.next()) {
                    String urunAdi = rs.getString("UrunAdi");
                    int adet = rs.getInt("Adet");

                    try (PreparedStatement stokStmt = conn.prepareStatement(stokDusSQL)) {
                        stokStmt.setInt(1, adet); // Sepetteki ürün miktarını kullan
                        stokStmt.setString(2, urunAdi);
                        stokStmt.executeUpdate(); // Stok güncellemesini uygula
                    }
                }
            }

            conn.commit(); // Veritabanı işlemlerini kaydet
        } catch (SQLException e) {
            try {
                conn.rollback(); // Hata durumunda rollback yap
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }
            e.printStackTrace();
        }
    }


    @Override
    public void yenile() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sepetEkran.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 1050, 720);
        stage.setTitle("Sepet");
        stage.setScene(scene);
        stage.show();
        Stage stage1 = (Stage) urunler.getScene().getWindow();
        stage1.close();
    }

    private void gorunurYap() {
        boolean sepetBosMu = toplamTutar == 0;
        sepetTemizle.setVisible(!sepetBosMu);
        sepetTemizle.setDisable(sepetBosMu);
    }
}
