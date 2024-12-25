package com.example.ediz_engin_proje;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.*;

public class KaydolEkran extends Ekran {

    String URL = "jdbc:sqlserver://localhost:1433;databaseName=BBB;encrypt=false;trustServerCertificate=true";
    String USER = "BBB";  // Kullanıcı adı
    String PASSWORD = "BBB";  // Şifre


    @FXML
    Label uyari;
    @FXML
    TextField loginUsername;
    @FXML
    TextField loginPassword;

    @FXML
    public void kayitol() {
        String username = loginUsername.getText();
        String password = loginPassword.getText();

        try {
            if (musteriKontrol(username)) {
                uyari.setText("Kullanıcı Adı Kullanılıyor.");
            } else {
                if (username.isEmpty() || password.isEmpty()) {
                    uyari.setText("Kullanıcı Adı ya da Şifre Boş Olamaz.");
                } else {
                    // Veritabanına bağlanma ve kullanıcı kaydını ekleme
                    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                        if (conn == null || conn.isClosed()) {
                            uyari.setText("Veritabanı bağlantısı hatalı.");
                            return; // Bağlantı hatalıysa işleme devam etme
                        }

                        String sql = "INSERT INTO Musteriler (MusteriAdi, MusteriSifresi) VALUES (?, ?)";
                        try (PreparedStatement statement = conn.prepareStatement(sql)) {
                            statement.setString(1, username);
                            statement.setString(2, password);

                            // Kayıt işlemi
                            statement.executeUpdate();
                            uyari.setText("Kayıt Başarıyla Tamamlandı.");
                            Thread.sleep(2000);

                            // Kayıt başarılı ise pencereyi kapatma
                            Stage stage1 = (Stage) uyari.getScene().getWindow();
                            stage1.close();
                        } catch (SQLException e) {
                            uyari.setText("Kayıt sırasında hata meydana geldi.");
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    } catch (SQLException e) {
                        uyari.setText("Veritabanı bağlantısı hatası.");
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            uyari.setText("Veritabanı hatası.");
            e.printStackTrace();
        }
    }

    private boolean musteriKontrol(String girilenAd) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null || conn.isClosed()) {
                uyari.setText("Veritabanı bağlantısı hatalı.");
                return false; // Bağlantı hatalıysa işlemi durdur
            }

            String sql = "SELECT COUNT(*) FROM Musteriler WHERE MusteriAdi = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, girilenAd);

                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() && resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            uyari.setText("Veritabanı bağlantısı hatası.");
            e.printStackTrace();
            return false;
        }
    }
}
