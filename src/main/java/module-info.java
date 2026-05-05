module org.example {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example to javafx.base, javafx.graphics, javafx.fxml;
    exports org.example;
}