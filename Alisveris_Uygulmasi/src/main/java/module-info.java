module com.example.ediz_engin_proje {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.ediz_engin_proje to javafx.fxml;
    exports com.example.ediz_engin_proje;
}