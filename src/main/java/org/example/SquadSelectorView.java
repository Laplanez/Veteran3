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


public class SquadSelectorView {

    // === Tema paleti ===
    private static final String BG       = "#0f1623";
    private static final String BG2      = "#0a0f1c";
    private static final String PANEL    = "#1a2332";
    private static final String PANEL_2  = "#222e42";
    private static final String TEXT     = "#e6edf7";
    private static final String MUTED    = "#8aa0bd";
    private static final String ACCENT   = "#22d3ee";
    private static final String BORDER   = "#2a3954";
    private static final String GHOST    = "#2b3a55";
    private static final String SUCCESS  = "linear-gradient(to right, #16a34a, #22c55e)";
    private static final String DANGER   = "#ff8a8a";

    public static boolean show(Stage owner, Team myTeam, String formation, boolean isHandball) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("İlk Kadro Seç — " + myTeam.getName());

        List<Slot> slots = buildSlots(formation, isHandball);

        Label title = new Label("📋  " + myTeam.getName() + " — İlk " + slots.size() + " seç");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: " + ACCENT + ";");

        Label sub = new Label("Diziliş: " + formation
                + "   |   Her oyuncu yalnızca kendi pozisyonundan seçilebilir.");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED + ";");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(14));
        grid.setStyle("-fx-background-color: " + PANEL_2 + "; -fx-background-radius: 12;"
                + " -fx-border-color: " + BORDER + "; -fx-border-radius: 12; -fx-border-width: 1;");

        List<ComboBox<Player>> boxes = new ArrayList<>();
        String comboStyle =
                "-fx-background-color: " + PANEL + ";"
                        + " -fx-text-fill: " + TEXT + ";"
                        + " -fx-background-radius: 10; -fx-border-radius: 10;"
                        + " -fx-border-color: " + BORDER + "; -fx-border-width: 1;"
                        + " -fx-font-size: 13px;";

        for (int i = 0; i < slots.size(); i++) {
            Slot s = slots.get(i);
            Label l = new Label(s.label);
            l.setStyle("-fx-font-weight: bold; -fx-min-width: 100; -fx-text-fill: " + TEXT + ";"
                    + " -fx-font-size: 13px;");
            ComboBox<Player> cb = new ComboBox<>();
            cb.setPrefWidth(290);
            cb.setStyle(comboStyle);
            cb.setItems(FXCollections.observableArrayList(eligible(myTeam, s)));
            cb.setConverter(new javafx.util.StringConverter<Player>() {
                @Override public String toString(Player p) {
                    return p == null ? "" : p.getName() + "  [" + p.getPosition() + "]";
                }
                @Override public Player fromString(String s) { return null; }
            });
            // Beyaz isim + pozisyon (hem dropdown hem seçili gösterim)
            javafx.util.Callback<ListView<Player>, ListCell<Player>> cellFactory = lv -> new ListCell<Player>() {
                @Override protected void updateItem(Player p, boolean empty) {
                    super.updateItem(p, empty);
                    if (empty || p == null) {
                        setText(null); setGraphic(null);
                        setStyle("-fx-background-color: " + PANEL + ";");
                        return;
                    }
                    Label nameL = new Label(p.getName());
                    nameL.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
                    Label posL = new Label("[" + p.getPosition() + "]");
                    posL.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-opacity: 0.95;");
                    HBox row = new HBox(8, nameL, posL);
                    row.setAlignment(Pos.CENTER_LEFT);
                    setText(null);
                    setGraphic(row);
                    setStyle("-fx-background-color: " + (isSelected() ? PANEL_2 : PANEL) + ";"
                            + " -fx-padding: 6 10;");
                }
            };
            cb.setCellFactory(cellFactory);
            cb.setButtonCell(cellFactory.call(null));
            grid.add(l, 0, i);
            grid.add(cb, 1, i);
            boxes.add(cb);
        }

        autoFill(boxes, slots, myTeam);

        Label warn = new Label();
        warn.setStyle("-fx-text-fill: " + DANGER + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        Button confirm = new Button("✅  Onayla ve Dizilişi Görüntüle");
        confirm.setMaxWidth(Double.MAX_VALUE);
        confirm.setStyle(
                "-fx-font-size: 14px; -fx-padding: 12 22; -fx-background-color: " + SUCCESS + ";"
                        + " -fx-text-fill: white; -fx-font-weight: bold;"
                        + " -fx-background-radius: 12; -fx-border-radius: 12; -fx-cursor: hand;");

        Button autoBtn = new Button("⚡  Otomatik Doldur");
        autoBtn.setStyle(
                "-fx-padding: 10 16; -fx-background-color: " + GHOST + ";"
                        + " -fx-text-fill: " + TEXT + "; -fx-font-weight: bold;"
                        + " -fx-background-radius: 10; -fx-border-radius: 10;"
                        + " -fx-border-color: " + BORDER + "; -fx-border-width: 1; -fx-cursor: hand;");
        autoBtn.setOnAction(e -> autoFill(boxes, slots, myTeam));

        final boolean[] ok = {false};
        confirm.setOnAction(e -> {
            Set<Player> seen = new HashSet<>();
            for (ComboBox<Player> cb : boxes) {
                Player p = cb.getValue();
                if (p == null) { warn.setText("⚠  Tüm slotları doldur."); return; }
                if (!seen.add(p)) { warn.setText("⚠  Aynı oyuncu birden çok slotta: " + p.getName()); return; }
            }
            applySquad(myTeam, boxes);
            ok[0] = true;
            dialog.close();
        });

        HBox btnRow = new HBox(10, autoBtn, confirm);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(confirm, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(380);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;"
                + " -fx-border-color: transparent;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + BORDER + ";");

        VBox card = new VBox(12, title, sub, sep, scroll, warn, btnRow);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + PANEL + "; -fx-background-radius: 16;"
                + " -fx-border-color: " + BORDER + "; -fx-border-radius: 16; -fx-border-width: 1;");

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, " + BG + ", " + BG2 + ");");

        dialog.setScene(new Scene(root, 520, 640));
        dialog.showAndWait();

        if (!ok[0]) {
            autoFill(boxes, slots, myTeam);
            applySquad(myTeam, boxes);
        }
        return ok[0];
    }

    // ========================================================
    private static class Slot {
        final String label;
        final Set<String> positions;
        final boolean isGK;
        Slot(String label, Set<String> positions, boolean isGK) {
            this.label = label; this.positions = positions; this.isGK = isGK;
        }
    }

    private static List<Slot> buildSlots(String formation, boolean isHandball) {
        List<Slot> slots = new ArrayList<>();
        if (isHandball) {
            slots.add(new Slot("Kaleci",  setOf("GK"), true));
            for (int i = 1; i <= 6; i++) {
                slots.add(new Slot("Saha " + i, Collections.emptySet(), false));
            }
        } else {
            int[] lines = parseFormation(formation);
            slots.add(new Slot("Kaleci", setOf("Goalkeeper"), true));
            for (int i = lines.length - 1; i >= 0; i--) {
                String role;
                Set<String> pos;
                if (i == 0) { role = "Defans"; pos = setOf("Defender"); }
                else if (i == lines.length - 1) { role = "Forvet"; pos = setOf("Forward"); }
                else { role = "Orta Saha"; pos = setOf("Midfielder"); }
                for (int k = 1; k <= lines[i]; k++) {
                    slots.add(new Slot(role + " " + k, pos, false));
                }
            }
        }
        return slots;
    }

    private static Set<String> setOf(String... s) { return new HashSet<>(Arrays.asList(s)); }

    private static int[] parseFormation(String f) {
        try {
            String nums = f.split(" ")[0];
            String[] parts = nums.split("-");
            int[] r = new int[parts.length];
            for (int i = 0; i < parts.length; i++) r[i] = Integer.parseInt(parts[i].trim());
            return r;
        } catch (Exception e) {
            return new int[]{4, 4, 2};
        }
    }

    private static List<Player> eligible(Team team, Slot s) {
        List<Player> out = new ArrayList<>();
        for (Player p : team.getPlayers()) {
            String pos = p.getPosition() == null ? "" : p.getPosition();
            if (s.isGK) {
                if (pos.equalsIgnoreCase("GK") || pos.equalsIgnoreCase("Goalkeeper")) out.add(p);
            } else if (s.positions.isEmpty()) {
                if (!pos.equalsIgnoreCase("GK") && !pos.equalsIgnoreCase("Goalkeeper")) out.add(p);
            } else {
                if (s.positions.contains(pos)) out.add(p);
            }
        }
        return out;
    }

    private static void autoFill(List<ComboBox<Player>> boxes, List<Slot> slots, Team team) {
        Set<Player> used = new HashSet<>();
        for (int i = 0; i < boxes.size(); i++) {
            ComboBox<Player> cb = boxes.get(i);
            Player chosen = null;
            for (Player p : eligible(team, slots.get(i))) {
                if (!used.contains(p)) { chosen = p; break; }
            }
            if (chosen != null) used.add(chosen);
            cb.setValue(chosen);
        }
    }

    private static void applySquad(Team team, List<ComboBox<Player>> boxes) {
        List<Player> ordered = new ArrayList<>();
        Set<Player> chosen = new LinkedHashSet<>();
        for (ComboBox<Player> cb : boxes) {
            Player p = cb.getValue();
            if (p != null && chosen.add(p)) ordered.add(p);
        }
        for (Player p : new ArrayList<>(team.getPlayers())) {
            if (!chosen.contains(p)) ordered.add(p);
        }
        team.getPlayers().clear();
        team.getPlayers().addAll(ordered);
    }
}
