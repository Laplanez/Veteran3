package org.example;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.stage.*;
import java.util.*;

public class LineupSelectorView {

    // === Tema paleti (JavaFXMain ile uyumlu) ===
    private static final String BG        = "#0f1623";
    private static final String BG2       = "#0a0f1c";
    private static final String PANEL     = "#1a2332";
    private static final String PANEL_2   = "#222e42";
    private static final String TEXT      = "#e6edf7";
    private static final String MUTED     = "#8aa0bd";
    private static final String ACCENT    = "#22d3ee";
    private static final String BORDER    = "#2a3954";
    private static final String PRIMARY   = "linear-gradient(to right, #2563eb, #22d3ee)";
    private static final String SUCCESS   = "linear-gradient(to right, #16a34a, #22c55e)";

    /** Diziliş seçtirir ve seçilen formasyona göre takımın oyuncularını sıralar (blocking). */
    public static String show(Stage owner, Team myTeam, boolean isHandball) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Diziliş Seç — " + myTeam.getName());

        Label title = new Label("⚙  " + myTeam.getName() + " — Diziliş Seç");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: " + ACCENT + ";");

        Label subtitle = new Label("Takımının sahaya çıkacağı formasyonu belirle");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED + ";");

        ToggleGroup group = new ToggleGroup();
        VBox options = new VBox(8);
        options.setPadding(new Insets(14));
        options.setStyle("-fx-background-color: " + PANEL_2 + "; -fx-background-radius: 12;"
                + " -fx-border-color: " + BORDER + "; -fx-border-radius: 12; -fx-border-width: 1;");

        List<String> formations = isHandball
                ? List.of("6-0 (Defansif)", "5-1 (Dengeli)", "3-2-1 (Hücum)")
                : List.of("4-4-2 (Dengeli)", "4-3-3 (Hücum)", "5-3-2 (Defansif)", "3-5-2 (Orta Saha)");

        for (String f : formations) {
            RadioButton rb = new RadioButton(f);
            rb.setUserData(f);
            rb.setToggleGroup(group);
            rb.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT + "; -fx-font-weight: bold;");
            options.getChildren().add(rb);
        }
        ((RadioButton) options.getChildren().get(0)).setSelected(true);

        Label formLabel = sectionLabel("Formasyon");
        Label squadLabel = sectionLabel("Kadron");

        // Önizleme: oyuncu listesi
        ListView<String> preview = new ListView<>();
        for (Player p : myTeam.getPlayers()) {
            preview.getItems().add(p.getName());
        }
        preview.setPrefHeight(220);
        preview.setStyle(
                "-fx-background-color: " + PANEL_2 + ";"
                        + "-fx-control-inner-background: " + PANEL_2 + ";"
                        + "-fx-control-inner-background-alt: " + PANEL + ";"
                        + "-fx-text-fill: " + TEXT + ";"
                        + "-fx-background-radius: 12; -fx-border-radius: 12;"
                        + "-fx-border-color: " + BORDER + "; -fx-border-width: 1;");

        final String[] result = { null };
        Button confirm = new Button("✅  Onayla ve Maça Başla");
        confirm.setMaxWidth(Double.MAX_VALUE);
        confirm.setStyle(buttonStyle(SUCCESS));
        confirm.setOnAction(e -> {
            Toggle t = group.getSelectedToggle();
            if (t != null) result[0] = t.getUserData().toString();
            else result[0] = (String) formations.get(0);
            dialog.close();
        });

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + BORDER + ";");

        VBox card = new VBox(12, title, subtitle, sep,
                formLabel, options,
                squadLabel, preview, confirm);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + PANEL + "; -fx-background-radius: 16;"
                + " -fx-border-color: " + BORDER + "; -fx-border-radius: 16; -fx-border-width: 1;");

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, " + BG + ", " + BG2 + ");");

        // Sağ üstteki çarpı (X) → önceki ekrana dönmeyi belirtmek için null döndür
        // (result[0] yalnızca Onay butonu ile dolar)
        dialog.setScene(new Scene(root, 460, 620));
        dialog.showAndWait();
        return result[0];
    }

    private static Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT + ";"
                + " -fx-padding: 4 0 2 2;");
        return l;
    }

    private static String buttonStyle(String bg) {
        return "-fx-font-size: 14px; -fx-padding: 12 20; -fx-text-fill: white; -fx-font-weight: bold;"
                + " -fx-background-color: " + bg + ";"
                + " -fx-background-radius: 12; -fx-border-radius: 12; -fx-cursor: hand;";
    }
}
