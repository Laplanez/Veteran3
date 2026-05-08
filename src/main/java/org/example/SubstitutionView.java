package org.example;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

/**
 * Devre arası oyuncu değişikliği menüsü.
 * Sol: saha kadrosu (ilk N).  Sağ: yedek kulübesi.
 * Sahadan bir oyuncu + yedekten aynı pozisyondan biri seçilip "Değiş" tuşuna basılır.
 * Pozisyon eşleşmesi: kaleci↔kaleci, outfield↔outfield (futbolda Defender/Midfielder/Forward
 * birbirine geçişli sayılır — koç kararı; istersen sıkılaştırılabilir).
 *
 * Onaylanınca Team.players listesi swap'lara göre güncellenir (ilk N saha kadrosu).
 */
public class SubstitutionView {

    public static void show(Stage owner, Team myTeam, boolean isHandball) {
        int starters = isHandball ? 7 : 11;
        if (myTeam.getPlayers().size() <= starters) {
            // Yedek yoksa hiç açma
            return;
        }

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Devre Arası — Oyuncu Değişikliği");

        // Çalışma kopyaları
        List<Player> field = new ArrayList<>(myTeam.getPlayers().subList(0, starters));
        List<Player> bench = new ArrayList<>(myTeam.getPlayers().subList(starters, myTeam.getPlayers().size()));

        Label title = new Label("🔁 " + myTeam.getName() + " — Devre Arası Değişiklik");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");
        Label info = new Label("Sahadan ve yedekten birer oyuncu seç, sonra 'Değiş' bas. Aynı pozisyon olmalı (Kaleci ↔ Kaleci).");
        info.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        info.setWrapText(true);

        ListView<Player> fieldList = new ListView<>(FXCollections.observableArrayList(field));
        ListView<Player> benchList = new ListView<>(FXCollections.observableArrayList(bench));
        styleList(fieldList);
        styleList(benchList);

        VBox left = new VBox(6, new Label("⚽ Sahadakiler"), fieldList);
        VBox right = new VBox(6, new Label("🪑 Yedekler"), benchList);
        VBox.setVgrow(fieldList, Priority.ALWAYS);
        VBox.setVgrow(benchList, Priority.ALWAYS);

        Label warn = new Label();
        warn.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 12px;");
        Label changeLog = new Label("Yapılan değişiklik: 0");
        changeLog.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        final int[] changeCount = {0};

        Button btnSwap = new Button("🔁 Değiş");
        btnSwap.setStyle("-fx-padding: 8 16; -fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSwap.setOnAction(e -> {
            warn.setText("");
            Player f = fieldList.getSelectionModel().getSelectedItem();
            Player b = benchList.getSelectionModel().getSelectedItem();
            if (f == null || b == null) { warn.setText("⚠ Hem sahadan hem yedekten birer oyuncu seç."); return; }
            if (!compatible(f, b)) {
                warn.setText("⚠ Pozisyon uyumsuz: " + f.getPosition() + " ↔ " + b.getPosition());
                return;
            }
            int fi = field.indexOf(f);
            int bi = bench.indexOf(b);
            field.set(fi, b);
            bench.set(bi, f);
            fieldList.setItems(FXCollections.observableArrayList(field));
            benchList.setItems(FXCollections.observableArrayList(bench));
            changeCount[0]++;
            changeLog.setText("Yapılan değişiklik: " + changeCount[0]);
        });

        Button btnDone = new Button("✅ Bitir ve Devam Et");
        btnDone.setStyle("-fx-padding: 8 16; -fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        btnDone.setOnAction(e -> {
            // Team listesini güncelle
            List<Player> merged = new ArrayList<>(field);
            merged.addAll(bench);
            myTeam.getPlayers().clear();
            myTeam.getPlayers().addAll(merged);
            dialog.close();
        });

        Button btnSkip = new Button("⏭ Değişiklik Yapma");
        btnSkip.setStyle("-fx-padding: 8 16;");
        btnSkip.setOnAction(e -> dialog.close());

        HBox actions = new HBox(10, btnSwap, btnSkip, btnDone);
        actions.setAlignment(Pos.CENTER);

        HBox columns = new HBox(15, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        VBox.setVgrow(columns, Priority.ALWAYS);

        VBox root = new VBox(10, title, info, columns, warn, changeLog, actions);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #ecf0f1;");

        dialog.setScene(new Scene(root, 720, 520));
        dialog.showAndWait();
    }

    private static void styleList(ListView<Player> lv) {
        lv.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(Player p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) { setText(null); }
                else { setText(p.getName() + "   [" + p.getPosition() + "]"); }
            }
        });
        lv.setPrefHeight(320);
    }

    private static boolean compatible(Player a, Player b) {
        boolean aGK = isGK(a), bGK = isGK(b);
        return aGK == bGK; // kaleci sadece kaleciyle, outfield sadece outfield ile
    }

    private static boolean isGK(Player p) {
        String pos = p.getPosition() == null ? "" : p.getPosition();
        return pos.equalsIgnoreCase("GK") || pos.equalsIgnoreCase("Goalkeeper");
    }
}
