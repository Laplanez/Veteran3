module Veteran3 {
    // JavaFX'in arayüz araçlarını kullanabilmemiz için gerekli izinler
    requires javafx.controls;

    // Eğer projende ileride .fxml tasarımları kullanacaksan aşağıdaki satırın başındaki // işaretlerini silersin
    // requires javafx.fxml;

    // Senin kodlarının (org.example paketi) dışarıya ve JavaFX'e açık olduğunu belirtiyoruz
    exports org.example;
}