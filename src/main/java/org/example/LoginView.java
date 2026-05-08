package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.util.function.Consumer;

public class LoginView {

    public static void show(Stage stage, Consumer<String> onLogin) {
        // ---- Arka plan görseli ----
        StackPane root = new StackPane();
        Image bg = loadBackground();
        if (bg != null) {
            ImageView iv = new ImageView(bg);
            iv.setPreserveRatio(false);
            iv.fitWidthProperty().bind(root.widthProperty());
            iv.fitHeightProperty().bind(root.heightProperty());
            // Hafif blur ki kart üstünde okunsun
            iv.setEffect(new GaussianBlur(6));
            root.getChildren().add(iv);
        } else {
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #0b2545, #1f6f3d);");
        }

        // Karartma katmanı
        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: rgba(8, 18, 35, 0.45);");
        root.getChildren().add(overlay);

        // ---- Giriş kartı ----
        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(32, 36, 32, 36));
        card.setMaxWidth(380);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.10);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: rgba(255,255,255,0.25);" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-width: 1;"
        );
        DropShadow ds = new DropShadow(24, Color.rgb(0, 0, 0, 0.55));
        card.setEffect(ds);

        Label title = new Label("⚽  SPOR SİMÜLATÖRÜ  🤾");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Oyuna başlamak için kullanıcı adını gir");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #d8e1ec;");

        TextField tfUser = new TextField();
        tfUser.setPromptText("Kullanıcı adı");
        styleField(tfUser);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #ff8a8a; -fx-font-size: 12px;");
        lblError.setVisible(false);
        lblError.setManaged(false);

        Button btnLogin = new Button("GİRİŞ YAP");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;" +
                        "-fx-padding: 12 20;" +
                        "-fx-background-radius: 10;" +
                        "-fx-background-color: linear-gradient(to right, #2ecc71, #27ae60);" +
                        "-fx-cursor: hand;"
        );

        Runnable doLogin = () -> {
            String u = tfUser.getText() == null ? "" : tfUser.getText().trim();
            if (u.isEmpty()) {
                lblError.setText("Kullanıcı adı boş bırakılamaz.");
                lblError.setVisible(true);
                lblError.setManaged(true);
                return;
            }
            onLogin.accept(u);
        };

        btnLogin.setOnAction(e -> doLogin.run());
        tfUser.setOnAction(e -> doLogin.run());

        card.getChildren().addAll(title, subtitle, new Region() {{ setMinHeight(8); }},
                tfUser, lblError, btnLogin);

        StackPane.setAlignment(card, Pos.CENTER);
        StackPane.setMargin(card, new Insets(20));
        root.getChildren().add(card);

        Scene scene = new Scene(root, 1050, 650);
        stage.setTitle("Spor Simülatörü — Giriş");
        stage.setScene(scene);
        stage.show();
    }

    private static void styleField(TextField f) {
        f.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-padding: 10 14;" +
                        "-fx-background-radius: 10;" +
                        "-fx-background-color: rgba(255,255,255,0.92);" +
                        "-fx-text-fill: #1a1a1a;" +
                        "-fx-prompt-text-fill: #6b7785;"
        );
    }

    /** background.png dosyasını çalışma dizininden veya classpath'ten yükler. */
    private static Image loadBackground() {
        // 1) Çalışma dizini
        try {
            File f = new File("background.png");
            if (f.exists()) return new Image(new FileInputStream(f));
        } catch (Exception ignored) {}
        // 2) Resources / classpath
        try {
            var url = LoginView.class.getResource("/background.png");
            if (url != null) return new Image(url.toExternalForm());
        } catch (Exception ignored) {}
        return null;
    }
}