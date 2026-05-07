package org.example;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.stage.*;
import java.util.*;

public class LineupSelectorView {

    /** Diziliş seçtirir ve seçilen formasyona göre takımın oyuncularını sıralar (blocking). */
    public static String show(Stage owner, Team myTeam, boolean isHandball) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Diziliş Seç — " + myTeam.getName());

        Label title = new Label("⚙ " + myTeam.getName() + " — Diziliş Seç");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ToggleGroup group = new ToggleGroup();
        VBox options = new VBox(8);
        options.setPadding(new Insets(10));

        List<String> formations = isHandball
                ? List.of("6-0 (Defansif)", "5-1 (Dengeli)", "3-2-1 (Hücum)")
                : List.of("4-4-2 (Dengeli)", "4-3-3 (Hücum)", "5-3-2 (Defansif)", "3-5-2 (Orta Saha)");

        for (String f : formations) {
            RadioButton rb = new RadioButton(f);
            rb.setUserData(f);
            rb.setToggleGroup(group);
            rb.setStyle("-fx-font-size: 14px;");
            options.getChildren().add(rb);
        }
        ((RadioButton) options.getChildren().get(0)).setSelected(true);

        // Önizleme: oyuncu listesi
        ListView<String> preview = new ListView<>();
        for (Player p : myTeam.getPlayers()) {
            preview.getItems().add(p.getName()); // veya: p.getPosition() + " — " + p.getName()
        }
        preview.setPrefHeight(220);

        final String[] result = {(String) formations.get(0)};
        Button confirm = new Button("✅ Onayla ve Maça Başla");
        confirm.setMaxWidth(Double.MAX_VALUE);
        confirm.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        confirm.setOnAction(e -> {
            Toggle t = group.getSelectedToggle();
            if (t != null) result[0] = t.getUserData().toString();
            dialog.close();
        });

        VBox root = new VBox(12, title, new Separator(),
                new Label("Formasyon:"), options,
                new Label("Kadron:"), preview, confirm);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #ecf0f1;");

        dialog.setScene(new Scene(root, 420, 560));
        dialog.showAndWait(); // bloklar
        return result[0];
    }
}