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
 * Maç öncesi, formasyona göre saha kadrosu seçimi.
 * Her pozisyon için sadece o pozisyona uygun oyuncular ComboBox'ta listelenir.
 * Onaylanınca myTeam.getPlayers() listesi yeniden sıralanır:
 *   [GK, DEF*, MID*, FW*  (futbol)  |  GK, outfield*  (hentbol)]  ardından yedekler.
 *
 * Dönüş: true = onaylandı, false = iptal (varsayılan otomatik dolduruldu).
 */
public class SquadSelectorView {

    public static boolean show(Stage owner, Team myTeam, String formation, boolean isHandball) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("İlk Kadro Seç — " + myTeam.getName());

        // Slot tanımları: her slot = (etiket, uygun pozisyon kümesi)
        List<Slot> slots = buildSlots(formation, isHandball);

        Label title = new Label("📋 " + myTeam.getName() + " — İlk " + slots.size() + " seç");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");
        Label sub = new Label("Diziliş: " + formation
                + "   |  Her oyuncu yalnızca kendi pozisyonundan seçilebilir.");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        List<ComboBox<Player>> boxes = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            Slot s = slots.get(i);
            Label l = new Label(s.label);
            l.setStyle("-fx-font-weight: bold; -fx-min-width: 90;");
            ComboBox<Player> cb = new ComboBox<>();
            cb.setPrefWidth(280);
            cb.setItems(FXCollections.observableArrayList(eligible(myTeam, s)));
            cb.setConverter(new javafx.util.StringConverter<Player>() {
                @Override public String toString(Player p) {
                    return p == null ? "" : p.getName() + "  [" + p.getPosition() + "]";
                }
                @Override public Player fromString(String s) { return null; }
            });
            grid.add(l, 0, i);
            grid.add(cb, 1, i);
            boxes.add(cb);
        }

        // Otomatik ön doldurma: her slota uygun ilk seçilmemiş oyuncuyu ata
        autoFill(boxes, slots, myTeam);

        Label warn = new Label();
        warn.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 12px;");

        Button confirm = new Button("✅ Onayla ve Dizilişi Görüntüle");
        confirm.setMaxWidth(Double.MAX_VALUE);
        confirm.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #2ecc71; "
                + "-fx-text-fill: white; -fx-font-weight: bold;");

        Button autoBtn = new Button("⚡ Otomatik Doldur");
        autoBtn.setStyle("-fx-padding: 6 12;");
        autoBtn.setOnAction(e -> autoFill(boxes, slots, myTeam));

        final boolean[] ok = {false};
        confirm.setOnAction(e -> {
            // Doğrulama: hepsi dolu, tekrar yok
            Set<Player> seen = new HashSet<>();
            for (ComboBox<Player> cb : boxes) {
                Player p = cb.getValue();
                if (p == null) { warn.setText("⚠ Tüm slotları doldur."); return; }
                if (!seen.add(p)) { warn.setText("⚠ Aynı oyuncu birden çok slotta: " + p.getName()); return; }
            }
            // Reorder: önce seçilenler (slot sırasıyla), sonra geri kalanlar (yedek)
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

        VBox root = new VBox(10, title, sub, new Separator(), scroll, warn, btnRow);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #ecf0f1;");

        dialog.setScene(new Scene(root, 480, 600));
        dialog.showAndWait();

        // Dialog kapanırken onaylanmadıysa otomatik doldurma uygula
        if (!ok[0]) {
            autoFill(boxes, slots, myTeam);
            applySquad(myTeam, boxes);
        }
        return ok[0];
    }

    // ========================================================
    private static class Slot {
        final String label;
        final Set<String> positions; // boş => tüm outfield
        final boolean isGK;
        Slot(String label, Set<String> positions, boolean isGK) {
            this.label = label; this.positions = positions; this.isGK = isGK;
        }
    }

    private static List<Slot> buildSlots(String formation, boolean isHandball) {
        List<Slot> slots = new ArrayList<>();
        if (isHandball) {
            // 1 GK + 6 outfield (formasyonun nasıl olduğu önemli değil; pozisyon kuralı sade)
            slots.add(new Slot("Kaleci",  setOf("GK"), true));
            for (int i = 1; i <= 6; i++) {
                slots.add(new Slot("Saha " + i, Collections.emptySet(), false)); // her outfield
            }
        } else {
            // Futbol: parse "4-3-3" / "4-4-2" / ...
            int[] lines = parseFormation(formation); // ör. [4,3,3] = [DEF, MID, FW]
            slots.add(new Slot("Kaleci", setOf("Goalkeeper"), true));
            // LineupView formasyon satırlarını hücum→defans okuyor; aynı sırayla slot oluştur
            // (lines'i sondan başa yürü)
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

    private static Set<String> setOf(String... s) {
        return new HashSet<>(Arrays.asList(s));
    }

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
                // outfield: GK olmayan herkes
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
        // Yedekler: kadroda olup seçilmeyenler
        for (Player p : new ArrayList<>(team.getPlayers())) {
            if (!chosen.contains(p)) ordered.add(p);
        }
        team.getPlayers().clear();
        team.getPlayers().addAll(ordered);
    }
}
