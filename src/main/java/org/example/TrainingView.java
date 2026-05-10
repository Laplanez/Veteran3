package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

public final class TrainingView {

    public static final int DEFAULT_BUDGET = 8;
    public static final int MAX_PER_PLAYER = 3;
    public static final int ATTR_CAP = 99;

    // Renk paleti
    private static final String COLOR_BG     = "#0f1623";
    private static final String COLOR_PANEL  = "#161f30";
    private static final String COLOR_PANEL2 = "#1c2740";
    private static final String COLOR_TEXT   = "#e6edf7";
    private static final String COLOR_MUTED  = "#94a3b8";
    private static final String COLOR_ACCENT = "#22d3ee";
    private static final String COLOR_OK     = "#34d399";
    private static final String COLOR_WARN   = "#f97316";
    private static final String COLOR_BORDER = "rgba(255,255,255,0.10)";

    private TrainingView() {}

    public static int show(Stage owner, Team team, boolean isHandball, int budget) {
        final int[] remaining = { Math.max(0, budget) };
        final int initialBudget = remaining[0];
        final Map<Player, Integer> usedPerPlayer = new IdentityHashMap<>();

        List<String> attrs = isHandball
                ? Arrays.asList("Throwing", "Speed", "Goalkeeping", "Defense")
                : Arrays.asList("Shooting", "Speed", "Passing", "Goalkeeping");

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Antrenman");

        Label title = new Label("🏋  HAFTALIK ANTRENMAN");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_TEXT + ";");
        Label subtitle = new Label(team.getName() + (isHandball ? "  •  HENTBOL" : "  •  FUTBOL")
                + "  •  Oyuncu başına en fazla " + MAX_PER_PLAYER + " gelişim");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: " + COLOR_MUTED + ";");

        Label budgetBadge = new Label();
        Runnable updateBadge = () -> budgetBadge.setText("⚡ Kalan puan: " + remaining[0] + " / " + initialBudget);
        updateBadge.run();
        budgetBadge.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_BG + ";"
                + "-fx-background-color: linear-gradient(to right, " + COLOR_ACCENT + ", " + COLOR_OK + ");"
                + "-fx-background-radius: 999; -fx-padding: 6 14;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        VBox titleBox = new VBox(2, title, subtitle);
        HBox header = new HBox(16, titleBox, spacer, budgetBadge);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 22, 14, 22));
        header.setStyle("-fx-background-color: linear-gradient(to right, " + COLOR_PANEL2 + ", " + COLOR_PANEL + ");"
                + "-fx-border-color: " + COLOR_BORDER + "; -fx-border-width: 0 0 1 0;");

        TableView<Player> table = new TableView<>();
        table.setPlaceholder(new Label("Kadro boş."));
        ObservableList<Player> rows = FXCollections.observableArrayList(team.getPlayers());
        table.setItems(rows);
        table.setStyle("-fx-background-color: " + COLOR_PANEL + "; -fx-control-inner-background: " + COLOR_PANEL
                + "; -fx-control-inner-background-alt: " + COLOR_PANEL2 + "; -fx-text-fill: " + COLOR_TEXT
                + "; -fx-table-cell-border-color: transparent;");

        TableColumn<Player, String> cName = new TableColumn<>("Oyuncu");
        cName.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));
        cName.setMinWidth(170);

        TableColumn<Player, String> cPos = new TableColumn<>("Poz.");
        cPos.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getPosition()));
        cPos.setMinWidth(70);

        table.getColumns().add(cName);
        table.getColumns().add(cPos);

        for (String attr : attrs) {
            TableColumn<Player, Number> col = new TableColumn<>(attr);
            col.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getAttribute(attr)));
            col.setMinWidth(85);
            table.getColumns().add(col);
        }

        TableColumn<Player, String> cUsed = new TableColumn<>("Kullanım");
        cUsed.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                usedPerPlayer.getOrDefault(d.getValue(), 0) + " / " + MAX_PER_PLAYER));
        cUsed.setMinWidth(85);
        table.getColumns().add(cUsed);

        // --- DEĞİŞİKLİK BURADA ---
        ComboBox<String> attrBox = new ComboBox<>(FXCollections.observableArrayList(attrs));
        attrBox.getSelectionModel().selectFirst();
        attrBox.setStyle("-fx-background-color: " + COLOR_PANEL2 + "; -fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 5;");

        // Seçili görünen metni beyaz yapmak için:
        attrBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                }
            }
        });

        // Açılır listedeki seçenekleri beyaz yapmak için:
        attrBox.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: " + COLOR_PANEL2 + ";");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: " + COLOR_PANEL2 + "; -fx-text-fill: white;");
                }
            }
        });
        // -------------------------

        Button btnTrain = new Button("➕  Antrenman Yap (+1)");
        btnTrain.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white; -fx-cursor: hand;"
                + "-fx-padding: 10 18; -fx-background-radius: 10;"
                + "-fx-background-color: linear-gradient(to right, #16a34a, #22c55e);");

        Button btnDone = new Button("✔  Bitir");
        btnDone.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white; -fx-cursor: hand;"
                + "-fx-padding: 10 18; -fx-background-radius: 10;"
                + "-fx-background-color: linear-gradient(to right, #2563eb, #22d3ee);");

        Label hint = new Label("Bir oyuncu seç, gelişecek yeteneği belirle ve +1 yap.");
        hint.setStyle("-fx-text-fill: " + COLOR_MUTED + "; -fx-font-size: 12px;");

        btnTrain.setOnAction(e -> {
            Player p = table.getSelectionModel().getSelectedItem();
            if (p == null) { hint.setText("⚠ Önce bir oyuncu seç."); hint.setStyle("-fx-text-fill: " + COLOR_WARN + ";"); return; }
            String attr = attrBox.getValue();
            if (attr == null) return;
            if (remaining[0] <= 0) { hint.setText("⚠ Bu hafta için puan kalmadı."); hint.setStyle("-fx-text-fill: " + COLOR_WARN + ";"); return; }
            int used = usedPerPlayer.getOrDefault(p, 0);
            if (used >= MAX_PER_PLAYER) { hint.setText("⚠ Bu oyuncu bu hafta için sınıra ulaştı."); hint.setStyle("-fx-text-fill: " + COLOR_WARN + ";"); return; }
            int cur = p.getAttribute(attr);
            if (cur >= ATTR_CAP) { hint.setText("⚠ " + attr + " zaten tavanda (" + ATTR_CAP + ")."); hint.setStyle("-fx-text-fill: " + COLOR_WARN + ";"); return; }
            p.setAttribute(attr, Math.min(ATTR_CAP, cur + 1));
            usedPerPlayer.put(p, used + 1);
            remaining[0] -= 1;
            updateBadge.run();
            hint.setText("✅ " + p.getName() + " — " + attr + " +1");
            hint.setStyle("-fx-text-fill: " + COLOR_OK + ";");
            table.refresh();
        });

        btnDone.setOnAction(e -> dialog.close());

        HBox controls = new HBox(10, new Label("Yetenek:") {{
            setStyle("-fx-text-fill: " + COLOR_TEXT + "; -fx-font-weight: bold;");
        }}, attrBox, btnTrain, spacer(), btnDone);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(12, 18, 14, 18));

        VBox body = new VBox(10, table, hint);
        body.setPadding(new Insets(12, 18, 6, 18));
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox root = new VBox(header, body, controls);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, " + COLOR_BG + ", #0a0f1c);");
        VBox.setVgrow(body, Priority.ALWAYS);

        Scene scene = new Scene(root, 820, 540);
        dialog.setScene(scene);
        dialog.showAndWait();

        return initialBudget - remaining[0];
    }

    private static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }
}