package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

public class LineupView {

    /** Geri uyumluluk için eski imza — varsayılan dizilişle gösterir. */
    public static void show(Stage owner, Team teamA, Team teamB, boolean isHandball) {
        show(owner, teamA, teamB, isHandball, null, null);
    }

    /**
     * @param myTeam   hangi takım kullanıcının (null ise ikisi de varsayılan)
     * @param formation seçilen diziliş, ör. "4-3-3 (Hücum)" / "5-1 (Dengeli)"
     */
    public static void show(Stage owner, Team teamA, Team teamB,
                            boolean isHandball, Team myTeam, String formation) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Maç Öncesi Diziliş");

        Label title = new Label((isHandball ? "🤾 " : "⚽ ")
                + teamA.getName() + "  vs  " + teamB.getName());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        String defaultFormation = isHandball ? "6-0" : "4-4-2";
        String formA = (myTeam == teamA && formation != null) ? formation : defaultFormation;
        String formB = (myTeam == teamB && formation != null) ? formation : defaultFormation;

        VBox fieldA = buildField(teamA, formA, "#27ae60", true);
        VBox fieldB = buildField(teamB, formB, "#2980b9", false);

        Label vs = new Label("VS");
        vs.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        HBox fields = new HBox(15, fieldA, vs, fieldB);
        fields.setAlignment(Pos.CENTER);
        HBox.setHgrow(fieldA, Priority.ALWAYS);
        HBox.setHgrow(fieldB, Priority.ALWAYS);

        Button btnStart = new Button("▶ Maça Başla");
        btnStart.setMaxWidth(Double.MAX_VALUE);
        btnStart.setStyle("-fx-font-size: 15px; -fx-padding: 10; -fx-background-color: #2ecc71; "
                + "-fx-text-fill: white; -fx-font-weight: bold;");
        btnStart.setOnAction(e -> dialog.close());

        VBox root = new VBox(15, title, fields, btnStart);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #ecf0f1;");

        dialog.setScene(new Scene(root, 1000, 650));
        dialog.showAndWait();
    }

    /** Takım için saha + formasyona göre dizilmiş oyuncular. */
    private static VBox buildField(Team team, String formation, String bgColor, boolean isMine) {
        VBox field = new VBox(18);
        field.setAlignment(Pos.TOP_CENTER);
        field.setPadding(new Insets(15));
        field.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10;");
        field.setMinWidth(380);

        Label name = new Label(team.getName() + (isMine ? "  (Senin Takımın)" : ""));
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        field.getChildren().add(name);

        Label formLbl = new Label("Diziliş: " + formation);
        formLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
        field.getChildren().add(formLbl);

        int[] lines = parseFormation(formation);
        List<Player> players = new ArrayList<>(team.getPlayers());
        int idx = 0;

        // Formasyon satırları (hücum → orta → defans) — ters sırayla, kaleci en altta
        int[] reversed = new int[lines.length];
        for (int i = 0; i < lines.length; i++) reversed[i] = lines[lines.length - 1 - i];

        // Önce sahadaki oyuncuları say (kaleci hariç tüm formasyon)
        int outfieldCount = 0;
        for (int c : lines) outfieldCount += c;

        // Kaleciyi ayır, ilk oyuncu kaleci
        Player gk = (idx < players.size()) ? players.get(idx++) : null;

        // Hücum → defans sırasıyla satırları ekle
        for (int count : reversed) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER);
            for (int i = 0; i < count && idx < players.size(); i++) {
                row.getChildren().add(playerNode(players.get(idx++)));
            }
            field.getChildren().add(row);
        }

        // Kaleci satırı en altta
        if (gk != null) {
            HBox gkRow = new HBox(10);
            gkRow.setAlignment(Pos.CENTER);
            gkRow.getChildren().add(playerNode(gk));
            field.getChildren().add(gkRow);
        }


        // Yedekler
        if (idx < players.size()) {
            Label benchLbl = new Label("Yedekler");
            benchLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: white; -fx-font-style: italic;");
            field.getChildren().add(benchLbl);
            HBox bench = new HBox(8);
            bench.setAlignment(Pos.CENTER);
            while (idx < players.size()) {
                bench.getChildren().add(playerNode(players.get(idx++)));
            }
            field.getChildren().add(bench);
        }

        return field;
    }

    private static int[] parseFormation(String f) {
        try {
            String nums = f.split(" ")[0]; // "4-3-3 (Hücum)" -> "4-3-3"
            String[] parts = nums.split("-");
            int[] r = new int[parts.length];
            for (int i = 0; i < parts.length; i++) r[i] = Integer.parseInt(parts[i].trim());
            return r;
        } catch (Exception e) {
            return new int[]{4, 4, 2};
        }
    }

    private static Node playerNode(Player p) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);

        Circle dot = new Circle(14, Color.WHITE);
        dot.setStroke(Color.BLACK);

        Label nameLbl = new Label(p.getName());
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;");
        nameLbl.setWrapText(true);
        nameLbl.setMaxWidth(75);
        nameLbl.setAlignment(Pos.CENTER);
        nameLbl.setStyle(nameLbl.getStyle() + "; -fx-text-alignment: center;");

        box.getChildren().addAll(dot, nameLbl);
        return box;
    }
}