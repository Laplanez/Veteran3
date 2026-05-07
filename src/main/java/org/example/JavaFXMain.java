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

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class JavaFXMain extends Application {
    private TableView<Team> table = new TableView<>();
    private ListView<String> logs = new ListView<>();
    private Button btnFootball = new Button("⚽ Futbol");
    private Button btnHandball = new Button("🤾 Hentbol");
    private Button btnNextWeek = new Button("▶ Sonraki Hafta");
    private Button btnReset = new Button("🔄 Yeni Sezona Başla");
    private Label lblWeek = new Label("");

    private ComboBox<String> teamSelector = new ComboBox<>();
    private Label lblSelectTeam = new Label("Takımını Seç:");

    private VBox tacticPanel = new VBox();
    private ToggleGroup tacticGroup = new ToggleGroup();
    private Button btnContinue = new Button("▶ 2. Yarıya Devam Et");
    private CountDownLatch tacticLatch;

    private String currentSport = null;
    private Team myTeam = null;
    private int currentWeek = 0;
    private int totalWeeks = 0;
    private List<List<Match>> weeklySchedule = new ArrayList<>();
    private List<Team> teams = new ArrayList<>();
    private boolean isHandball = false;

    private List<String> namePool = new ArrayList<>();
    private List<String> surnamePool = new ArrayList<>();

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        loadNameFiles();
        setupTable();
        setupTacticPanel();

        btnFootball.setMaxWidth(Double.MAX_VALUE);
        btnHandball.setMaxWidth(Double.MAX_VALUE);
        btnNextWeek.setMaxWidth(Double.MAX_VALUE);
        btnReset.setMaxWidth(Double.MAX_VALUE);
        teamSelector.setMaxWidth(Double.MAX_VALUE);

        btnFootball.setStyle("-fx-font-size: 16px; -fx-padding: 12 24; -fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnHandball.setStyle("-fx-font-size: 16px; -fx-padding: 12 24; -fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnNextWeek.setStyle("-fx-font-size: 15px; -fx-padding: 10 20; -fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnReset.setStyle("-fx-font-size: 13px; -fx-padding: 8 16; -fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        lblWeek.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        lblSelectTeam.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        teamSelector.setStyle("-fx-font-size: 14px;");

        btnNextWeek.setDisable(true);
        btnNextWeek.setVisible(false);
        btnReset.setDisable(true);
        btnReset.setVisible(false);
        teamSelector.setVisible(false);
        lblSelectTeam.setVisible(false);
        tacticPanel.setVisible(false);
        tacticPanel.setManaged(false);

        btnFootball.setOnAction(e -> showTeamSelection("football"));
        btnHandball.setOnAction(e -> showTeamSelection("handball"));
        btnNextWeek.setOnAction(e -> playNextWeek());
        btnReset.setOnAction(e -> resetAll());

        HBox sportButtons = new HBox(15, btnFootball, btnHandball);
        sportButtons.setAlignment(Pos.CENTER);

        HBox teamSelectBox = new HBox(10, lblSelectTeam, teamSelector);
        teamSelectBox.setAlignment(Pos.CENTER);

        HBox controlButtons = new HBox(15, btnNextWeek, btnReset);
        controlButtons.setAlignment(Pos.CENTER);

        Label title = new Label("Spor Simülatörü");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        VBox right = new VBox(10, new Label("Maç Sonuçları"), logs, tacticPanel);
        VBox.setVgrow(logs, Priority.ALWAYS);

        HBox content = new HBox(15, table, right);
        HBox.setHgrow(table, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        VBox root = new VBox(12, title, sportButtons, teamSelectBox, lblWeek, controlButtons, content);
        root.setPadding(new Insets(15));
        VBox.setVgrow(content, Priority.ALWAYS);

        stage.setScene(new Scene(root, 1050, 650));
        stage.setTitle("Spor Simülatörü - Hafta Hafta");
        stage.show();
    }

    // ==================== İSİM DOSYALARI ====================
    private void loadNameFiles() {
        namePool = readLinesFromFile("Names.txt");
        surnamePool = readLinesFromFile("Surnames.txt");
        if (namePool.isEmpty()) {
            namePool = List.of("Ali","Veli","Ahmet","Mehmet","Can","Ege","Deniz","Barış","Mert","Kaan",
                    "Emre","Burak","Oğuz","Cem","Serkan","Tolga","Onur","Yusuf","Arda","Eren",
                    "Selim","Fatih","Taner","Volkan","Uğur","Kerem","Sinan","Okan","Cenk","Kaan");
        }
        if (surnamePool.isEmpty()) {
            surnamePool = List.of("Yılmaz","Kaya","Demir","Çelik","Şahin","Yıldız","Öztürk","Aydın",
                    "Arslan","Doğan","Kılıç","Aslan","Çetin","Korkmaz","Aksoy","Erdoğan","Koç","Kurt",
                    "Özkan","Polat","Güneş","Aktaş","Kaplan","Acar","Bayrak","Taş","Tunç","Balcı","Yıldırım","Özdemir");
        }
    }

    private List<String> readLinesFromFile(String filename) {
        List<String> lines = new ArrayList<>();
        try {
            Path path = Paths.get(filename);
            if (!Files.exists(path)) {
                String jarDir = System.getProperty("user.dir");
                path = Paths.get(jarDir, filename);
            }
            if (Files.exists(path)) {
                for (String line : Files.readAllLines(path)) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) lines.add(trimmed);
                }
            } else {
                System.err.println("⚠ Dosya bulunamadı: " + filename);
            }
        } catch (IOException e) {
            System.err.println("⚠ Dosya okunamadı: " + filename + " — " + e.getMessage());
        }
        return lines;
    }

    private String randomFullName(Set<String> usedNames) {
        Random random = new Random();
        int attempts = 0;
        while (attempts < 50) {
            String first = namePool.get(random.nextInt(namePool.size()));
            String last = surnamePool.get(random.nextInt(surnamePool.size()));
            String full = first + " " + last;
            if (!usedNames.contains(full)) {
                usedNames.add(full);
                return full;
            }
            attempts++;
        }
        return "Oyuncu " + random.nextInt(9999);
    }

    // ==================== TABLO ====================
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

    private void setupTacticPanel() {
        Label lblTactic = new Label("⚙ TAKTİK DEĞİŞİKLİĞİ");
        lblTactic.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        Label lblInfo = new Label("2. yarı için taktik seç:");
        lblInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: white;");

        RadioButton rbAttack = new RadioButton("⚔ Atak (Hücum x1.2 / Savunma x0.8)");
        RadioButton rbBalanced = new RadioButton("⚖ Dengeli (Hücum x1.0 / Savunma x1.0)");
        RadioButton rbDefense = new RadioButton("🛡 Defans (Hücum x0.8 / Savunma x1.2)");

        rbAttack.setToggleGroup(tacticGroup);
        rbBalanced.setToggleGroup(tacticGroup);
        rbDefense.setToggleGroup(tacticGroup);
        rbAttack.setUserData("Attack");
        rbBalanced.setUserData("Balanced");
        rbDefense.setUserData("Defense");
        rbBalanced.setSelected(true);

        rbAttack.setStyle("-fx-font-size: 13px; -fx-text-fill: white;");
        rbBalanced.setStyle("-fx-font-size: 13px; -fx-text-fill: white;");
        rbDefense.setStyle("-fx-font-size: 13px; -fx-text-fill: white;");

        btnContinue.setMaxWidth(Double.MAX_VALUE);
        btnContinue.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");

        btnContinue.setOnAction(e -> {
            String selected = tacticGroup.getSelectedToggle().getUserData().toString();
            Tactic tactic;
            switch (selected) {
                case "Attack":  tactic = Tactic.createAttack(); break;
                case "Defense": tactic = Tactic.createDefense(); break;
                default:        tactic = Tactic.createBalanced(); break;
            }
            myTeam.setTactic(tactic);
            logs.getItems().add("   ⚙ Taktik değişti: " + tactic.getName()
                    + " (Hücum x" + tactic.getAttackModifier() + " / Savunma x" + tactic.getDefenseModifier() + ")");
            logs.scrollTo(logs.getItems().size() - 1);
            tacticPanel.setVisible(false);
            tacticPanel.setManaged(false);
            if (tacticLatch != null) tacticLatch.countDown();
        });

        Separator sep = new Separator();
        tacticPanel.setSpacing(8);
        tacticPanel.setPadding(new Insets(10));
        tacticPanel.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 8; -fx-border-color: #e74c3c; -fx-border-radius: 8; -fx-border-width: 2;");
        tacticPanel.getChildren().addAll(lblTactic, sep, lblInfo, rbAttack, rbBalanced, rbDefense, btnContinue);
    }

    // ==================== TAKIM SEÇİMİ ====================
    private void showTeamSelection(String sport) {
        currentSport = sport;
        isHandball = sport.equals("handball");
        List<String> teamNames = isHandball
                ? List.of("Alpha HC","Beta United","Gamma SK","Delta Club")
                : List.of("Galatasaray","Fenerbahçe","Beşiktaş","Trabzonspor");
        teamSelector.setItems(FXCollections.observableArrayList(teamNames));
        teamSelector.setValue(null);
        teamSelector.setPromptText("Takım seç...");
        teamSelector.setVisible(true);
        lblSelectTeam.setVisible(true);
        btnFootball.setDisable(true);
        btnHandball.setDisable(true);
        teamSelector.setOnAction(e -> {
            String selected = teamSelector.getValue();
            if (selected != null) initSeason(selected);
        });
    }

    // ==================== SEZON BAŞLAT ====================
    private void initSeason(String selectedTeamName) {
        currentWeek = 0;
        teams.clear();
        weeklySchedule.clear();
        logs.getItems().clear();
        table.getItems().clear();
        Set<String> usedNames = new HashSet<>();

        if (isHandball) {
            teams.addAll(List.of(new Team("Alpha HC"), new Team("Beta United"),
                    new Team("Gamma SK"), new Team("Delta Club")));
            for (Team t : teams) initHandballPlayers(t, usedNames);
        } else {
            teams.addAll(List.of(new Team("Galatasaray"), new Team("Fenerbahçe"),
                    new Team("Beşiktaş"), new Team("Trabzonspor")));
            for (Team t : teams) initFootballPlayers(t, usedNames);
        }

        myTeam = teams.stream().filter(t -> t.getName().equals(selectedTeamName))
                .findFirst().orElse(teams.get(0));

        generateWeeklyFixture();
        totalWeeks = weeklySchedule.size();

        String sportName = isHandball ? "HENTBOL" : "FUTBOL";
        lblWeek.setText(sportName + " LİGİ — Takımın: " + myTeam.getName());
        logs.getItems().add("🏆 " + sportName + " ligi! Takımın: " + myTeam.getName());
        logs.getItems().add("▶ 'Sonraki Hafta' ile haftaları oyna.");

        table.setItems(FXCollections.observableArrayList(teams));
        table.refresh();

        teamSelector.setVisible(false);
        lblSelectTeam.setVisible(false);
        btnNextWeek.setDisable(false);
        btnNextWeek.setVisible(true);
        btnReset.setDisable(false);
        btnReset.setVisible(true);
    }

    private void generateWeeklyFixture() {
        weeklySchedule.clear();
        int n = teams.size();
        List<Team> pool = new ArrayList<>(teams);
        int rounds = n - 1;
        int halfSize = n / 2;
        MatchEngine footballEngine = isHandball ? null : new FootballEngine();

        for (int round = 0; round < rounds; round++) {
            List<Match> weekMatches = new ArrayList<>();
            for (int i = 0; i < halfSize; i++) {
                Team a = pool.get(i);
                Team b = pool.get(n - 1 - i);
                if (isHandball) weekMatches.add(new HandballMatch(a, b, new HandballEngine()));
                else weekMatches.add(new FootballMatch(a, b, footballEngine));
            }
            weeklySchedule.add(weekMatches);
            Team last = pool.remove(n - 1);
            pool.add(1, last);
        }
    }

    // ==================== HAFTA OYNA ====================
    private void playNextWeek() {
        if (currentWeek >= totalWeeks) return;
        List<Match> weekMatches = weeklySchedule.get(currentWeek);
        currentWeek++;

        Match myMatch = null;
        List<Match> otherMatches = new ArrayList<>();
        for (Match m : weekMatches) {
            if (m.getTeamA() == myTeam || m.getTeamB() == myTeam) myMatch = m;
            else otherMatches.add(m);
        }

        int winPoints = isHandball ? 2 : 3;
        String emoji = isHandball ? "🤾" : "⚽";

        logs.getItems().add("");
        logs.getItems().add("═══════ HAFTA " + currentWeek + " ═══════");

        for (Match m : otherMatches) {
            if (isHandball) simulateHandballSilent(m);
            else m.play();
            updateStats(m, winPoints);
            logs.getItems().add(String.format("   %s  %s %d - %d %s",
                    emoji, m.getTeamA().getName(), m.getScoreA(), m.getScoreB(), m.getTeamB().getName()));
        }

        if (myMatch != null) {
            logs.getItems().add("");
            logs.getItems().add("🔴 SENİN MAÇIN: " + myMatch.getTeamA().getName() + " vs " + myMatch.getTeamB().getName());
            logs.getItems().add("─────────────────────────");
            if (isHandball) playMyHandballMatchLive(myMatch, winPoints);
            else playMyFootballMatchLive(myMatch, winPoints);
        } else {
            logs.getItems().add("📋 Bu hafta maçın yok.");
            logs.scrollTo(logs.getItems().size() - 1);
            refreshStandings();
            checkSeasonEnd();
        }
    }

    // ==================== FUTBOL: CANLI + TAKTİK ====================
    private void playMyFootballMatchLive(Match m, int winPoints) {
        btnNextWeek.setDisable(true);
        btnReset.setDisable(true);

        // 🆕 Önce diziliş seç
        String formation = LineupSelectorView.show(primaryStage, myTeam, false);
        logs.getItems().add("   📋 " + myTeam.getName() + " dizilişi: " + formation);
        logs.scrollTo(logs.getItems().size() - 1);

        // Sonra sahadaki oyuncuları göster
        LineupView.show(primaryStage, m.getTeamA(), m.getTeamB(), false, myTeam, formation);

        new Thread(() -> {
            Random random = new Random();
            FootballMatch fm = (FootballMatch) m;
            int powerA = calculateFootballPower(m.getTeamA());
            int powerB = calculateFootballPower(m.getTeamB());

            int[] firstHalf = {15, 30, 45};
            for (int minute : firstHalf) {
                try { Thread.sleep(800); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                checkGoal(fm, random, powerA, powerB, minute);
                if (minute == 45) {
                    Platform.runLater(() -> {
                        logs.getItems().add("   ⏱ DEVRE: " + fm.getScoreA() + "-" + fm.getScoreB());
                        logs.scrollTo(logs.getItems().size() - 1);
                    });
                }
            }

            showTacticMenuAndWait();

            int powerA2 = calculateFootballPower(m.getTeamA());
            int powerB2 = calculateFootballPower(m.getTeamB());
            int[] secondHalf = {60, 75, 90};
            for (int minute : secondHalf) {
                try { Thread.sleep(800); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                checkGoal(fm, random, powerA2, powerB2, minute);
            }

            setMatchFinished(m);

            Platform.runLater(() -> {
                myTeam.setTactic(null);
                updateStats(m, winPoints);
                logs.getItems().add("─────────────────────────");
                logs.getItems().add(String.format("   ⏱ 90' MAÇ BİTTİ! SONUÇ: %s %d - %d %s",
                        m.getTeamA().getName(), m.getScoreA(), m.getScoreB(), m.getTeamB().getName()));
                addWinLoseMsg(m);
                logs.scrollTo(logs.getItems().size() - 1);
                refreshStandings();
                checkSeasonEnd();
                if (currentWeek < totalWeeks) btnNextWeek.setDisable(false);
                btnReset.setDisable(false);
            });
        }).start();
    }

    private void checkGoal(FootballMatch fm, Random random, int powerA, int powerB, int periodEnd) {
        if (random.nextInt(100) < (powerA / 8)) {
            fm.addGoalA();
            Player scorer = fm.getTeamA().getPlayers().get(random.nextInt(fm.getTeamA().getPlayers().size()));
            int goalMin = periodEnd - 14 + random.nextInt(14);
            final int sA = fm.getScoreA(), sB = fm.getScoreB();
            Platform.runLater(() -> {
                logs.getItems().add(String.format("   ⚽ %d' GOL! %s — %s (%d-%d)",
                        goalMin, fm.getTeamA().getName(), scorer.getName(), sA, sB));
                logs.scrollTo(logs.getItems().size() - 1);
            });
        }
        if (random.nextInt(100) < (powerB / 8)) {
            fm.addGoalB();
            Player scorer = fm.getTeamB().getPlayers().get(random.nextInt(fm.getTeamB().getPlayers().size()));
            int goalMin = periodEnd - 14 + random.nextInt(14);
            final int sA = fm.getScoreA(), sB = fm.getScoreB();
            Platform.runLater(() -> {
                logs.getItems().add(String.format("   ⚽ %d' GOL! %s — %s (%d-%d)",
                        goalMin, fm.getTeamB().getName(), scorer.getName(), sA, sB));
                logs.scrollTo(logs.getItems().size() - 1);
            });
        }
    }

    // ==================== HENTBOL: CANLI + TAKTİK ====================
    private void playMyHandballMatchLive(Match m, int winPoints) {
        btnNextWeek.setDisable(true);
        btnReset.setDisable(true);

        // 🆕 Önce diziliş seç
        String formation = LineupSelectorView.show(primaryStage, myTeam, true);
        logs.getItems().add("   📋 " + myTeam.getName() + " dizilişi: " + formation);
        logs.scrollTo(logs.getItems().size() - 1);

        // Sonra sahadaki oyuncuları göster
        LineupView.show(primaryStage, m.getTeamA(), m.getTeamB(), true, myTeam, formation);

        new Thread(() -> {
            Random random = new Random();
            HandballMatch hm = (HandballMatch) m;
            int attackA = calcPower(m.getTeamA(), "Throwing", "Speed");
            int defenseB = calcPower(m.getTeamB(), "Goalkeeping", "Defense");
            int attackB = calcPower(m.getTeamB(), "Throwing", "Speed");
            int defenseA = calcPower(m.getTeamA(), "Goalkeeping", "Defense");

            int totalSeconds = 0;
            while (totalSeconds < 1800) {
                int timeStep = 25 + random.nextInt(11);
                totalSeconds += timeStep;
                if (totalSeconds > 1800) totalSeconds = 1800;
                int min = totalSeconds / 60;
                int sec = totalSeconds % 60;
                checkHandballGoal(hm, random, attackA, defenseB, attackB, defenseA, min, sec);
                try { Thread.sleep(70); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }

            Platform.runLater(() -> {
                logs.getItems().add("   ⏱ DEVRE: " + hm.getScoreA() + "-" + hm.getScoreB());
                logs.scrollTo(logs.getItems().size() - 1);
            });

            showTacticMenuAndWait();

            int attackA2 = calcPower(m.getTeamA(), "Throwing", "Speed");
            int defenseB2 = calcPower(m.getTeamB(), "Goalkeeping", "Defense");
            int attackB2 = calcPower(m.getTeamB(), "Throwing", "Speed");
            int defenseA2 = calcPower(m.getTeamA(), "Goalkeeping", "Defense");

            if (myTeam.getTactic() != null) {
                if (m.getTeamA() == myTeam) {
                    attackA2 = (int)(attackA2 * myTeam.getTactic().getAttackModifier());
                    defenseA2 = (int)(defenseA2 * myTeam.getTactic().getDefenseModifier());
                } else {
                    attackB2 = (int)(attackB2 * myTeam.getTactic().getAttackModifier());
                    defenseB2 = (int)(defenseB2 * myTeam.getTactic().getDefenseModifier());
                }
            }

            while (totalSeconds < 3600) {
                int timeStep = 25 + random.nextInt(11);
                totalSeconds += timeStep;
                if (totalSeconds > 3600) totalSeconds = 3600;
                int min = totalSeconds / 60;
                int sec = totalSeconds % 60;
                checkHandballGoal(hm, random, attackA2, defenseB2, attackB2, defenseA2, min, sec);
                try { Thread.sleep(70); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }

            setMatchFinished(m);

            Platform.runLater(() -> {
                myTeam.setTactic(null);
                updateStats(m, winPoints);
                logs.getItems().add("─────────────────────────");
                logs.getItems().add(String.format("   ⏱ 60' MAÇ BİTTİ! SONUÇ: %s %d - %d %s",
                        m.getTeamA().getName(), m.getScoreA(), m.getScoreB(), m.getTeamB().getName()));
                addWinLoseMsg(m);
                logs.scrollTo(logs.getItems().size() - 1);
                refreshStandings();
                checkSeasonEnd();
                if (currentWeek < totalWeeks) btnNextWeek.setDisable(false);
                btnReset.setDisable(false);
            });
        }).start();
    }

    private void checkHandballGoal(HandballMatch hm, Random random,
                                   int attackA, int defenseB, int attackB, int defenseA,
                                   int min, int sec) {
        double chanceA = ((double) attackA / (attackA + defenseB)) * 0.50;
        if (random.nextDouble() < chanceA) {
            hm.addGoalA();
            Player scorer = hm.getTeamA().getPlayers().get(random.nextInt(hm.getTeamA().getPlayers().size()));
            final int sA = hm.getScoreA(), sB = hm.getScoreB();
            Platform.runLater(() -> {
                logs.getItems().add(String.format("   🤾 [%02d:%02d] GOL! %s — %s (%d-%d)",
                        min, sec, hm.getTeamA().getName(), scorer.getName(), sA, sB));
                logs.scrollTo(logs.getItems().size() - 1);
            });
        }
        double chanceB = ((double) attackB / (attackB + defenseA)) * 0.50;
        if (random.nextDouble() < chanceB) {
            hm.addGoalB();
            Player scorer = hm.getTeamB().getPlayers().get(random.nextInt(hm.getTeamB().getPlayers().size()));
            final int sA = hm.getScoreA(), sB = hm.getScoreB();
            Platform.runLater(() -> {
                logs.getItems().add(String.format("   🤾 [%02d:%02d] GOL! %s — %s (%d-%d)",
                        min, sec, hm.getTeamB().getName(), scorer.getName(), sA, sB));
                logs.scrollTo(logs.getItems().size() - 1);
            });
        }
    }

    // ==================== TAKTİK MENÜSÜ ====================
    private void showTacticMenuAndWait() {
        tacticLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            logs.getItems().add("");
            logs.getItems().add("   ⏸ DEVRE ARASI — Taktik seç ve devam et!");
            logs.scrollTo(logs.getItems().size() - 1);
            tacticGroup.getToggles().forEach(t -> {
                if ("Balanced".equals(t.getUserData())) t.setSelected(true);
            });
            tacticPanel.setVisible(true);
            tacticPanel.setManaged(true);
        });
        try { tacticLatch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ==================== SESSİZ HENTBOL ====================
    private void simulateHandballSilent(Match m) {
        if (m instanceof HandballMatch) {
            Random random = new Random();
            int attackA = calcPower(m.getTeamA(), "Throwing", "Speed");
            int defenseB = calcPower(m.getTeamB(), "Goalkeeping", "Defense");
            int attackB = calcPower(m.getTeamB(), "Throwing", "Speed");
            int defenseA = calcPower(m.getTeamA(), "Goalkeeping", "Defense");
            HandballMatch hm = (HandballMatch) m;
            int totalSeconds = 0;
            while (totalSeconds < 3600) {
                totalSeconds += 25 + random.nextInt(11);
                if (totalSeconds > 3600) totalSeconds = 3600;
                double chanceA = ((double) attackA / (attackA + defenseB)) * 0.50;
                if (random.nextDouble() < chanceA) hm.addGoalA();
                double chanceB = ((double) attackB / (attackB + defenseA)) * 0.50;
                if (random.nextDouble() < chanceB) hm.addGoalB();
            }
            setMatchFinished(m);
        }
    }

    // ==================== YARDIMCILAR ====================
    private int calculateFootballPower(Team team) {
        double base = team.getPlayers().stream()
                .mapToInt(p -> p.getAttribute("Shooting") + p.getAttribute("Speed"))
                .average().orElse(50);
        if (team.getTactic() != null) base *= team.getTactic().getAttackModifier();
        return (int) base;
    }

    private int calcPower(Team team, String attr1, String attr2) {
        return (int) team.getPlayers().stream()
                .mapToInt(p -> (p.getAttribute(attr1) + p.getAttribute(attr2)) / 2)
                .average().orElse(50);
    }

    private void setMatchFinished(Match m) {
        try {
            var field = Match.class.getDeclaredField("isFinished");
            field.setAccessible(true);
            field.set(m, true);
        } catch (Exception ignored) {}
    }

    private void updateStats(Match m, int winPoints) {
        Team a = m.getTeamA(); Team b = m.getTeamB();
        a.recordMatch(m.getScoreA(), m.getScoreB());
        b.recordMatch(m.getScoreB(), m.getScoreA());
        if (m.getScoreA() > m.getScoreB()) a.addPoints(winPoints);
        else if (m.getScoreB() > m.getScoreA()) b.addPoints(winPoints);
        else { a.addPoints(1); b.addPoints(1); }
    }

    private void addWinLoseMsg(Match m) {
        if ((m.getTeamA() == myTeam && m.getScoreA() > m.getScoreB()) ||
                (m.getTeamB() == myTeam && m.getScoreB() > m.getScoreA())) {
            logs.getItems().add("   🎉 TEBRİKLER! Kazandın!");
        } else if (m.getScoreA() == m.getScoreB()) {
            logs.getItems().add("   🤝 Berabere!");
        } else {
            logs.getItems().add("   😞 Kaybettin...");
        }
    }

    private void refreshStandings() {
        teams.sort((t1, t2) -> {
            if (t2.getPoints() != t1.getPoints()) return t2.getPoints() - t1.getPoints();
            return t2.getGoalDifference() - t1.getGoalDifference();
        });
        table.setItems(FXCollections.observableArrayList(teams));
        table.refresh();
    }

    private void checkSeasonEnd() {
        if (currentWeek >= totalWeeks) {
            logs.getItems().add("");
            logs.getItems().add("🏆🏆🏆 LİG BİTTİ! ŞAMPİYON: " + teams.get(0).getName() + " 🏆🏆🏆");
            if (teams.get(0) == myTeam) logs.getItems().add("🥇 SEN ŞAMPİYON OLDUN! TEBRİKLER!");
            else {
                int myRank = teams.indexOf(myTeam) + 1;
                logs.getItems().add("📊 Sen " + myRank + ". sırada bitirdin. (" + myTeam.getName() + ")");
            }
            logs.scrollTo(logs.getItems().size() - 1);
            btnNextWeek.setDisable(true);
            btnNextWeek.setText("✅ Lig Tamamlandı");
            lblWeek.setText("LİG BİTTİ — Şampiyon: " + teams.get(0).getName());
        } else {
            lblWeek.setText((isHandball ? "HENTBOL" : "FUTBOL") + " LİGİ — Hafta " + currentWeek + "/" + totalWeeks + " | Takımın: " + myTeam.getName());
        }
    }

    // ==================== SIFIRLA ====================
    private void resetAll() {
        currentSport = null; myTeam = null; currentWeek = 0; totalWeeks = 0;
        weeklySchedule.clear(); teams.clear();
        logs.getItems().clear(); table.getItems().clear();
        lblWeek.setText("");
        btnFootball.setDisable(false);
        btnHandball.setDisable(false);
        btnNextWeek.setDisable(true);
        btnNextWeek.setVisible(false);
        btnNextWeek.setText("▶ Sonraki Hafta");
        btnReset.setDisable(true);
        btnReset.setVisible(false);
        teamSelector.setVisible(false);
        lblSelectTeam.setVisible(false);
        tacticPanel.setVisible(false);
        tacticPanel.setManaged(false);
    }

    // ==================== OYUNCU OLUŞTUR ====================
    private void initFootballPlayers(Team t, Set<String> usedNames) {
        String[] positions = {"Goalkeeper","Defender","Defender","Defender","Defender",
                "Midfielder","Midfielder","Midfielder","Forward","Forward","Forward"};
        for (int i = 0; i < 11; i++) {
            String playerName = randomFullName(usedNames);
            FootballPlayer p = new FootballPlayer(playerName, i + 1, positions[i]);
            p.setAttribute("Shooting", 50 + (int)(Math.random() * 25));
            p.setAttribute("Speed", 50 + (int)(Math.random() * 25));
            p.setAttribute("Passing", 50 + (int)(Math.random() * 25));
            p.setAttribute("Goalkeeping", positions[i].equals("Goalkeeper") ? 70 + (int)(Math.random() * 20) : 30);
            t.addPlayer(p);
        }
    }

    private void initHandballPlayers(Team t, Set<String> usedNames) {
        String[] positions = {"GK","LW","LB","CB","RB","RW","PV"};
        for (int i = 0; i < 7; i++) {
            String playerName = randomFullName(usedNames);
            HandballPlayer p = new HandballPlayer(playerName, 20 + i, positions[i]);
            p.setAttribute("Throwing", 65 + (int)(Math.random() * 20));
            p.setAttribute("Speed", 60 + (int)(Math.random() * 20));
            p.setAttribute("Goalkeeping", positions[i].equals("GK") ? 70 + (int)(Math.random() * 20) : 40);
            p.setAttribute("Defense", 55 + (int)(Math.random() * 20));
            t.addPlayer(p);
        }
    }

    public static void main(String[] args) { launch(args); }
}
