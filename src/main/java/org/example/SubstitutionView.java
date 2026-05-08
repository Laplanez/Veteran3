package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SubstitutionView {

    // ---- ARENA renk paleti ----
    private static final String COLOR_BG       = "#0f1623";
    private static final String COLOR_PANEL    = "#161f30";
    private static final String COLOR_PANEL_2  = "#1c2740";
    private static final String COLOR_BORDER   = "rgba(255,255,255,0.10)";
    private static final String COLOR_TEXT     = "#e6edf7";
    private static final String COLOR_MUTED    = "#94a3b8";
    private static final String COLOR_ACCENT   = "#22d3ee"; // cyan neon
    private static final String COLOR_ACCENT_2 = "#a78bfa"; // mor
    private static final String COLOR_OK       = "#34d399"; // yeşil
    private static final String COLOR_WARN     = "#f97316"; // turuncu
    private static final String COLOR_DANGER   = "#ef4444"; // kırmızı

    public static void show(Stage owner, Team team, boolean isHandball) {
        int starters = isHandball ? 7 : 11;
        int totalSubs = isHandball ? 5 : 3; // toplam izin verilen değişiklik

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Devre Arası — Oyuncu Değişikliği");

        // ---- Üst başlık ----
        Label title = new Label("🔁  OYUNCU DEĞİŞİKLİĞİ");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_TEXT + ";");

        Label subtitle = new Label("Devre arası — " + team.getName()
                + (isHandball ? "  •  HENTBOL" : "  •  FUTBOL"));
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: " + COLOR_MUTED + ";");

        Label subsLeft = new Label();
        subsLeft.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_BG + ";" +
                        "-fx-background-color: linear-gradient(to right, " + COLOR_ACCENT + ", " + COLOR_ACCENT_2 + ");" +
                        "-fx-background-radius: 999; -fx-padding: 4 12;"
        );

        VBox titleBox = new VBox(2, title, subtitle);
        HBox header = new HBox(16, titleBox, spacer(), subsLeft);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 22, 14, 22));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, " + COLOR_PANEL_2 + ", " + COLOR_PANEL + ");" +
                        "-fx-background-radius: 16 16 0 0;" +
                        "-fx-border-color: " + COLOR_BORDER + ";" +
                        "-fx-border-width: 0 0 1 0;"
        );

        // ---- Liste verileri ----
        List<Player> all = team.getPlayers();
        int s = Math.min(starters, all.size());
        ObservableList<Player> fieldItems = FXCollections.observableArrayList(new ArrayList<>(all.subList(0, s)));
        ObservableList<Player> benchItems = FXCollections.observableArrayList(
                all.size() > s ? new ArrayList<>(all.subList(s, all.size())) : new ArrayList<>());

        ListView<Player> fieldList = buildList(fieldItems, true);
        ListView<Player> benchList = buildList(benchItems, false);

        VBox fieldBox = listPanel("🟢  SAHADA  (" + s + ")", fieldList, COLOR_OK);
        VBox benchBox = listPanel("🪑  YEDEK KULÜBESİ", benchList, COLOR_WARN);

        // Orta kısım: değiştir butonu + sayaç
        Button btnSwap = new Button("⇄  DEĞİŞTİR");
        btnSwap.setStyle(neonButton(COLOR_ACCENT, COLOR_ACCENT_2));
        btnSwap.setMaxWidth(Double.MAX_VALUE);

        Button btnReset = new Button("↺  Geri al");
        btnReset.setStyle(ghostButton());
        btnReset.setMaxWidth(Double.MAX_VALUE);

        Label hint = new Label("Sahadan bir oyuncu ve\nyedekten bir oyuncu seç.");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: " + COLOR_MUTED + "; -fx-font-size: 11px;");
        hint.setAlignment(Pos.CENTER);

        VBox center = new VBox(10, btnSwap, btnReset, new Region() {{ setMinHeight(8); }}, hint);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(20, 8, 20, 8));
        center.setMaxWidth(170);

        HBox lists = new HBox(14, fieldBox, center, benchBox);
        HBox.setHgrow(fieldBox, Priority.ALWAYS);
        HBox.setHgrow(benchBox, Priority.ALWAYS);
        lists.setPadding(new Insets(18, 22, 6, 22));

        // ---- Alt aksiyon barı ----
        Button btnCancel = new Button("Vazgeç");
        btnCancel.setStyle(ghostButton());

        Button btnConfirm = new Button("✓  ONAYLA & DEVAM ET");
        btnConfirm.setStyle(neonButton(COLOR_OK, "#10b981"));

        HBox actions = new HBox(12, spacer(), btnCancel, btnConfirm);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(14, 22, 18, 22));
        actions.setStyle(
                "-fx-background-color: " + COLOR_PANEL + ";" +
                        "-fx-background-radius: 0 0 16 16;" +
                        "-fx-border-color: " + COLOR_BORDER + ";" +
                        "-fx-border-width: 1 0 0 0;"
        );

        VBox root = new VBox(header, lists, actions);
        root.setStyle(
                "-fx-background-color: " + COLOR_PANEL + ";" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: " + COLOR_BORDER + ";" +
                        "-fx-border-radius: 16; -fx-border-width: 1;"
        );
        root.setEffect(new DropShadow(28, Color.rgb(0, 0, 0, 0.55)));

        StackPane outer = new StackPane(root);
        outer.setPadding(new Insets(20));
        outer.setStyle("-fx-background-color: " + COLOR_BG + ";");

        // ---- Davranış ----
        final int[] subsUsed = {0};
        Runnable refreshCounter = () -> subsLeft.setText(
                "Kalan değişiklik: " + (totalSubs - subsUsed[0]) + " / " + totalSubs);
        refreshCounter.run();

        btnSwap.setOnAction(e -> {
            Player on = fieldList.getSelectionModel().getSelectedItem();
            Player off = benchList.getSelectionModel().getSelectedItem();
            if (on == null || off == null) return;
            if (subsUsed[0] >= totalSubs) return;
            int fi = fieldItems.indexOf(on);
            int bi = benchItems.indexOf(off);
            fieldItems.set(fi, off);
            benchItems.set(bi, on);
            subsUsed[0]++;
            refreshCounter.run();
            if (subsUsed[0] >= totalSubs) {
                btnSwap.setDisable(true);
                btnSwap.setStyle(disabledButton());
            }
        });

        btnReset.setOnAction(e -> {
            fieldItems.setAll(new ArrayList<>(all.subList(0, s)));
            benchItems.setAll(all.size() > s ? new ArrayList<>(all.subList(s, all.size())) : new ArrayList<>());
            subsUsed[0] = 0;
            btnSwap.setDisable(false);
            btnSwap.setStyle(neonButton(COLOR_ACCENT, COLOR_ACCENT_2));
            refreshCounter.run();
        });

        btnCancel.setOnAction(e -> dialog.close());

        btnConfirm.setOnAction(e -> {
            // Listeyi yerinde güncelle: önce sahadakiler, sonra yedekler
            List<Player> merged = new ArrayList<>(fieldItems.size() + benchItems.size());
            merged.addAll(fieldItems);
            merged.addAll(benchItems);
            all.clear();
            all.addAll(merged);
            dialog.close();
        });

        Scene scene = new Scene(outer, 880, 560);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    // ===================== UI yardımcıları =====================

    private static VBox listPanel(String titleText, ListView<Player> list, String accent) {
        Label t = new Label(titleText);
        t.setStyle(
                "-fx-text-fill: " + COLOR_TEXT + "; -fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-padding: 8 12; -fx-background-radius: 10 10 0 0;" +
                        "-fx-background-color: linear-gradient(to right, " + accent + "33, transparent);" +
                        "-fx-border-color: " + accent + "; -fx-border-width: 0 0 2 0;"
        );
        list.setStyle(
                "-fx-background-color: " + COLOR_PANEL_2 + ";" +
                        "-fx-control-inner-background: " + COLOR_PANEL_2 + ";" +
                        "-fx-background-insets: 0; -fx-padding: 0;" +
                        "-fx-background-radius: 0 0 10 10;"
        );
        VBox box = new VBox(t, list);
        VBox.setVgrow(list, Priority.ALWAYS);
        box.setStyle(
                "-fx-background-color: " + COLOR_PANEL_2 + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + COLOR_BORDER + ";" +
                        "-fx-border-radius: 10; -fx-border-width: 1;"
        );
        return box;
    }

    private static ListView<Player> buildList(ObservableList<Player> items, boolean field) {
        ListView<Player> lv = new ListView<>(items);
        lv.setPrefHeight(360);
        lv.setCellFactory(v -> new ListCell<Player>() {
            @Override
            protected void updateItem(Player p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                int idx = getIndex() + 1;
                Label num = new Label(String.format("%02d", idx));
                num.setStyle(
                        "-fx-font-family: 'Consolas','Courier New',monospace;" +
                                "-fx-font-weight: bold; -fx-font-size: 12px;" +
                                "-fx-text-fill: " + (field ? COLOR_OK : COLOR_WARN) + ";" +
                                "-fx-background-color: " + (field ? COLOR_OK : COLOR_WARN) + "22;" +
                                "-fx-background-radius: 6; -fx-padding: 3 8;"
                );

                Label name = new Label(safeName(p));
                name.setStyle("-fx-text-fill: " + COLOR_TEXT + "; -fx-font-size: 13px; -fx-font-weight: bold;");

                Label pos = new Label("[" + safePos(p) + "]");
                pos.setStyle(
                        "-fx-text-fill: " + COLOR_ACCENT + "; -fx-font-size: 11px; -fx-font-weight: bold;" +
                                "-fx-background-color: " + COLOR_ACCENT + "1f;" +
                                "-fx-background-radius: 6; -fx-padding: 2 8;"
                );

                Label tag = new Label(field ? "SAHADA" : "YEDEK");
                tag.setStyle(
                        "-fx-text-fill: " + COLOR_MUTED + "; -fx-font-size: 10px;" +
                                "-fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 999;" +
                                "-fx-padding: 1 8;"
                );

                HBox row = new HBox(10, num, name, pos, spacer(), tag);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6, 10, 6, 10));

                setText(null);
                setGraphic(row);

                boolean sel = isSelected();
                setStyle(
                        "-fx-background-color: " + (sel
                                ? "linear-gradient(to right, " + COLOR_ACCENT + "33, " + COLOR_ACCENT_2 + "22)"
                                : "transparent") + ";" +
                                "-fx-border-color: " + (sel ? COLOR_ACCENT : "transparent") + ";" +
                                "-fx-border-width: 0 0 0 3;"
                );
            }
        });
        return lv;
    }

    private static String safeName(Player p) {
        try { return p.getName(); } catch (Throwable t) { return p.toString(); }
    }

    private static String safePos(Player p) {
        try {
            String s = p.getPosition();
            return s == null || s.isEmpty() ? "-" : s;
        } catch (Throwable t) { return "-"; }
    }

    private static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    private static String neonButton(String c1, String c2) {
        return "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_BG + ";" +
                "-fx-padding: 10 18; -fx-background-radius: 10; -fx-cursor: hand;" +
                "-fx-background-color: linear-gradient(to right, " + c1 + ", " + c2 + ");";
    }

    private static String ghostButton() {
        return "-fx-font-size: 12px; -fx-text-fill: " + COLOR_TEXT + ";" +
                "-fx-background-color: transparent;" +
                "-fx-border-color: " + COLOR_BORDER + ";" +
                "-fx-border-radius: 10; -fx-background-radius: 10;" +
                "-fx-padding: 8 16; -fx-cursor: hand;";
    }

    private static String disabledButton() {
        return "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_MUTED + ";" +
                "-fx-padding: 10 18; -fx-background-radius: 10;" +
                "-fx-background-color: " + COLOR_PANEL_2 + ";" +
                "-fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 10;";
    }
}
