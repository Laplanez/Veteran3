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

    // === Tema paleti ===
    private static final String BG        = "#0f1623";
    private static final String BG2       = "#0a0f1c";
    private static final String PANEL     = "#1a2332";
    private static final String TEXT      = "#e6edf7";
    private static final String MUTED     = "#8aa0bd";
    private static final String ACCENT    = "#22d3ee";
    private static final String BORDER    = "#2a3954";
    private static final String SUCCESS   = "linear-gradient(to right, #16a34a, #22c55e)";
    // Saha degradeleri (oyun temasına uygun)
    private static final String FIELD_MINE  = "linear-gradient(to bottom, #14532d, #166534)";
    private static final String FIELD_OPP   = "linear-gradient(to bottom, #1e3a8a, #1e40af)";
    private static final String VS_COLOR    = "linear-gradient(to right, #ef4444, #f97316)";

    public static void show(Stage owner, Team teamA, Team teamB, boolean isHandball) {
        show(owner, teamA, teamB, isHandball, null, null);
    }

    public static void show(Stage owner, Team teamA, Team teamB,
                            boolean isHandball, Team myTeam, String formation) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Maç Öncesi Diziliş");

        Label title = new Label((isHandball ? "🤾  " : "⚽  ")
                + teamA.getName() + "   vs   " + teamB.getName());
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: " + ACCENT + ";");

        Label subtitle = new Label("Maç öncesi diziliş — kadrolar sahada");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED + ";");

        String defaultFormation = isHandball ? "6-0" : "4-4-2";
        String formA = (myTeam == teamA && formation != null) ? formation : defaultFormation;
        String formB = (myTeam == teamB && formation != null) ? formation : defaultFormation;

        VBox fieldA = buildField(teamA, formA, FIELD_MINE, myTeam == teamA);
        VBox fieldB = buildField(teamB, formB, FIELD_OPP,  myTeam == teamB);

        Label vs = new Label("VS");
        vs.setStyle("-fx-font-size: 30px; -fx-font-weight: 900; -fx-text-fill: white;"
                + " -fx-background-color: " + VS_COLOR + ";"
                + " -fx-padding: 10 18; -fx-background-radius: 999;");

        HBox fields = new HBox(18, fieldA, vs, fieldB);
        fields.setAlignment(Pos.CENTER);
        HBox.setHgrow(fieldA, Priority.ALWAYS);
        HBox.setHgrow(fieldB, Priority.ALWAYS);

        Button btnStart = new Button("▶  Maça Başla");
        btnStart.setMaxWidth(Double.MAX_VALUE);
        btnStart.setStyle("-fx-font-size: 15px; -fx-padding: 12 22; -fx-background-color: " + SUCCESS + ";"
                + " -fx-text-fill: white; -fx-font-weight: bold;"
                + " -fx-background-radius: 12; -fx-border-radius: 12; -fx-cursor: hand;");
        btnStart.setOnAction(e -> dialog.close());

        VBox card = new VBox(15, title, subtitle, fields, btnStart);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + PANEL + "; -fx-background-radius: 16;"
                + " -fx-border-color: " + BORDER + "; -fx-border-radius: 16; -fx-border-width: 1;");

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, " + BG + ", " + BG2 + ");");

        dialog.setScene(new Scene(root, 1040, 700));
        dialog.showAndWait();
    }

    /** Takım için saha + formasyona göre dizilmiş oyuncular. */
    private static VBox buildField(Team team, String formation, String bgGradient, boolean isMine) {
        VBox field = new VBox(18);
        field.setAlignment(Pos.TOP_CENTER);
        field.setPadding(new Insets(16));
        field.setStyle("-fx-background-color: " + bgGradient + ";"
                + " -fx-background-radius: 14;"
                + " -fx-border-color: " + (isMine ? ACCENT : BORDER) + ";"
                + " -fx-border-width: 2; -fx-border-radius: 14;");
        field.setMinWidth(380);

        Label name = new Label(team.getName() + (isMine ? "   (Senin Takımın)" : ""));
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: 900; -fx-text-fill: white;");
        field.getChildren().add(name);

        Label formLbl = new Label("Diziliş: " + formation);
        formLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #d8e1ec; -fx-font-weight: bold;"
                + " -fx-background-color: rgba(0,0,0,0.25); -fx-padding: 4 10;"
                + " -fx-background-radius: 999;");
        field.getChildren().add(formLbl);

        int[] lines = parseFormation(formation);
        List<Player> players = new ArrayList<>(team.getPlayers());
        int idx = 0;

        int[] reversed = new int[lines.length];
        for (int i = 0; i < lines.length; i++) reversed[i] = lines[lines.length - 1 - i];

        Player gk = (idx < players.size()) ? players.get(idx++) : null;

        for (int count : reversed) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER);
            for (int i = 0; i < count && idx < players.size(); i++) {
                row.getChildren().add(playerNode(players.get(idx++), isMine));
            }
            field.getChildren().add(row);
        }

        if (gk != null) {
            HBox gkRow = new HBox(10);
            gkRow.setAlignment(Pos.CENTER);
            gkRow.getChildren().add(playerNode(gk, isMine));
            field.getChildren().add(gkRow);
        }

        if (idx < players.size()) {
            Label benchLbl = new Label("— Yedekler —");
            benchLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #d8e1ec; -fx-font-style: italic;"
                    + " -fx-padding: 6 0 2 0;");
            field.getChildren().add(benchLbl);
            HBox bench = new HBox(8);
            bench.setAlignment(Pos.CENTER);
            while (idx < players.size()) {
                bench.getChildren().add(playerNode(players.get(idx++), isMine));
            }
            field.getChildren().add(bench);
        }

        return field;
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

    private static Node playerNode(Player p, boolean isMine) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);

        Circle dot = new Circle(15, isMine ? Color.web("#22d3ee") : Color.web("#e6edf7"));
        dot.setStroke(Color.web("#0f1623"));
        dot.setStrokeWidth(2);

        Label nameLbl = new Label(p.getName());
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-background-color: rgba(0,0,0,0.35); -fx-padding: 2 6;"
                + " -fx-background-radius: 6; -fx-text-alignment: center;");
        nameLbl.setWrapText(true);
        nameLbl.setMaxWidth(85);
        nameLbl.setAlignment(Pos.CENTER);

        box.getChildren().addAll(dot, nameLbl);
        return box;
    }
}
