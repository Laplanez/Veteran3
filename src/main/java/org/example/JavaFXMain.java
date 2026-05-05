package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class JavaFXMain extends Application {
    private TableView<Team> table = new TableView<>();
    private ListView<String> logs = new ListView<>();
    private Button btnFootball = new Button("⚽ Futbol");
    private Button btnHandball = new Button("🤾 Hentbol");
    private Button btnRestart = new Button("🔄 Tekrar Simüle Et");
    private String currentSport = null;

    @Override
    public void start(Stage stage) {
        setupTable();

        btnFootball.setMaxWidth(Double.MAX_VALUE);
        btnHandball.setMaxWidth(Double.MAX_VALUE);
        btnRestart.setMaxWidth(Double.MAX_VALUE);
        btnRestart.setDisable(true);

        btnFootball.setStyle("-fx-font-size: 16px; -fx-padding: 12 24; -fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnHandball.setStyle("-fx-font-size: 16px; -fx-padding: 12 24; -fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnRestart.setStyle("-fx-font-size: 14px; -fx-padding: 8 16; -fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");

        btnFootball.setOnAction(e -> startSim("football"));
        btnHandball.setOnAction(e -> startSim("handball"));
        btnRestart.setOnAction(e -> { if (currentSport != null) startSim(currentSport); });

        HBox buttonBox = new HBox(15, btnFootball, btnHandball);
        buttonBox.setAlignment(Pos.CENTER);

        Label title = new Label("Spor Simülatörü");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        VBox right = new VBox(10, new Label("Canlı Skorlar"), logs, btnRestart);
        VBox.setVgrow(logs, Priority.ALWAYS);

        HBox content = new HBox(15, table, right);
        HBox.setHgrow(table, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        VBox root = new VBox(15, title, buttonBox, content);
        root.setPadding(new Insets(15));
        VBox.setVgrow(content, Priority.ALWAYS);

        stage.setScene(new Scene(root, 1000, 600));
        stage.setTitle("Spor Simülatörü - Futbol & Hentbol");
        stage.show();
    }

    private void setupTable() {
        TableColumn<Team, String> c1 = new TableColumn<>("Takım");
        c1.setCellValueFactory(new PropertyValueFactory<>("name"));
        c1.setMinWidth(150);
        TableColumn<Team, Integer> c2 = new TableColumn<>("Puan");
        c2.setCellValueFactory(new PropertyValueFactory<>("points"));
        TableColumn<Team, Integer> c3 = new TableColumn<>("Averaj");
        c3.setCellValueFactory(new PropertyValueFactory<>("goalDifference"));
        table.getColumns().addAll(c1, c2, c3);
    }

    private void startSim(String sport) {
        currentSport = sport;
        logs.getItems().clear();
        table.getItems().clear();
        btnFootball.setDisable(true);
        btnHandball.setDisable(true);
        btnRestart.setDisable(true);

        if (sport.equals("football")) {
            startFootballSim();
        } else {
            startHandballSim();
        }
    }

    private void startFootballSim() {
        Platform.runLater(() -> logs.getItems().add("⚽ FUTBOL LİGİ BAŞLIYOR..."));

        FootballEngine engine = new FootballEngine();

        Team a = new Team("Galatasaray");
        Team b = new Team("Fenerbahçe");
        Team c = new Team("Beşiktaş");
        Team d = new Team("Trabzonspor");

        initFootballPlayers(a);
        initFootballPlayers(b);
        initFootballPlayers(c);
        initFootballPlayers(d);

        List<Team> teams = List.of(a, b, c, d);

        League league = new League();
        for (Team t : teams) league.addTeam(t);

        // Round-robin fikstür
        List<Match> schedule = new ArrayList<>();
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                schedule.add(new FootballMatch(teams.get(i), teams.get(j), engine));
            }
        }
        league.setSchedule(schedule);

        new Thread(() -> {
            for (Match m : league.getSchedule()) {
                m.play();
                // Futbol için stats güncelle
                Team ta = m.getTeamA();
                Team tb = m.getTeamB();
                ta.recordMatch(m.getScoreA(), m.getScoreB());
                tb.recordMatch(m.getScoreB(), m.getScoreA());
                if (m.getScoreA() > m.getScoreB()) ta.addPoints(3);
                else if (m.getScoreB() > m.getScoreA()) tb.addPoints(3);
                else { ta.addPoints(1); tb.addPoints(1); }

                String result = String.format("⚽ %s %d - %d %s",
                        ta.getName(), m.getScoreA(), m.getScoreB(), tb.getName());
                Platform.runLater(() -> {
                    logs.getItems().add(result);
                    logs.scrollTo(logs.getItems().size() - 1);
                });

                try { Thread.sleep(500); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            Platform.runLater(() -> {
                table.setItems(FXCollections.observableArrayList(league.getTeams()));
                table.refresh();
                logs.getItems().add("--- FUTBOL LİGİ BİTTİ ---");
                btnFootball.setDisable(false);
                btnHandball.setDisable(false);
                btnRestart.setDisable(false);
            });
        }).start();
    }

    private void startHandballSim() {
        HandballEngine engine = new HandballEngine();
        engine.setOnGoalScored(msg -> {
            logs.getItems().add(msg);
            logs.scrollTo(logs.getItems().size() - 1);
        });

        Team a = new Team("Alpha HC");
        Team b = new Team("Beta United");
        initHandballPlayers(a);
        initHandballPlayers(b);

        HandballLeague league = new HandballLeague();
        league.addTeam(a);
        league.addTeam(b);
        league.setSchedule(new ArrayList<>(List.of(new HandballMatch(a, b, engine))));

        new Thread(() -> {
            league.runSeason();
            Platform.runLater(() -> {
                table.setItems(FXCollections.observableArrayList(league.getTeams()));
                table.refresh();
                btnFootball.setDisable(false);
                btnHandball.setDisable(false);
                btnRestart.setDisable(false);
            });
        }).start();
    }

    private void initFootballPlayers(Team t) {
        String[] positions = {"Goalkeeper", "Defender", "Defender", "Defender", "Defender",
                "Midfielder", "Midfielder", "Midfielder", "Forward", "Forward", "Forward"};
        for (int i = 0; i < 11; i++) {
            FootballPlayer p = new FootballPlayer(t.getName() + "-P" + (i+1), i+1, positions[i]);
            p.setAttribute("Shooting", 50 + (int)(Math.random() * 25));
            p.setAttribute("Speed", 50 + (int)(Math.random() * 25));
            p.setAttribute("Passing", 50 + (int)(Math.random() * 25));
            p.setAttribute("Goalkeeping", positions[i].equals("Goalkeeper") ? 70 + (int)(Math.random() * 20) : 30);
            t.addPlayer(p);
        }
    }

    private void initHandballPlayers(Team t) {
        for (int i = 0; i < 7; i++) {
            HandballPlayer p = new HandballPlayer(t.getName() + "-P" + i, 20, "Pro");
            p.setAttribute("Throwing", 72);
            p.setAttribute("Speed", 68);
            p.setAttribute("Goalkeeping", 65);
            p.setAttribute("Defense", 60);
            t.addPlayer(p);
        }
    }

    public static void main(String[] args) { launch(args); }
}
