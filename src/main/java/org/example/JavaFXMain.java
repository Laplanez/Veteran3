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

    private String currentUser = null;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        // Kayıtlı oyun varsa giriş ekranını atla, doğrudan kaldığı yerden devam et
        File saveFile = new File("savegame.txt");
        if (saveFile.exists() && tryLoadAndResume(saveFile)) {
            return;
        }
        // Önce giriş ekranı, başarılı olunca ana ekranı kur
        LoginView.show(stage, username -> {
            this.currentUser = username;
            startMainApp(stage);
        });
    }

    /** savegame.txt dosyasını okuyup oyunu kaldığı yerden başlatır. */
    private boolean tryLoadAndResume(File f) {
        try {
            Map<String,String> kv = new LinkedHashMap<>();
            List<String[]> teamRows = new ArrayList<>();
            for (String line : Files.readAllLines(f.toPath())) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("#")) continue;
                if (t.startsWith("TEAM:")) {
                    teamRows.add(t.substring(5).split("\\|", -1));
                } else {
                    int idx = t.indexOf('=');
                    if (idx > 0) kv.put(t.substring(0, idx).trim(), t.substring(idx + 1).trim());
                }
            }
            if (teamRows.isEmpty() || !kv.containsKey("sport")) return false;

            this.currentUser = kv.getOrDefault("user", "Misafir");
            this.currentSport = kv.getOrDefault("sport", "football");
            this.isHandball = "handball".equalsIgnoreCase(this.currentSport);
            int week = 0;
            try { week = Integer.parseInt(kv.getOrDefault("week", "0")); } catch (Exception ignore) {}
            String myTeamName = kv.getOrDefault("myTeam", "");

            startMainApp(primaryStage);
            restoreSeason(teamRows, myTeamName, week);
            return true;
        } catch (Exception e) {
            System.err.println("⚠ Kayıt yüklenemedi: " + e.getMessage());
            return false;
        }
    }

    private void startMainApp(Stage stage) {
        loadNameFiles();
        setupTable();
        setupTacticPanel();

        // Modern renk paleti (koyu tema + neon vurgular)
        final String COLOR_BG       = "#0f1623";
        final String COLOR_PANEL    = "#1a2332";
        final String COLOR_PANEL_2  = "#222e42";
        final String COLOR_TEXT     = "#e6edf7";
        final String COLOR_MUTED    = "#8aa0bd";
        final String COLOR_ACCENT   = "#22d3ee"; // cyan
        final String COLOR_FOOTBALL = "linear-gradient(to right, #16a34a, #22c55e)";
        final String COLOR_HANDBALL = "linear-gradient(to right, #f97316, #fbbf24)";
        final String COLOR_PRIMARY  = "linear-gradient(to right, #2563eb, #22d3ee)";
        final String COLOR_DANGER   = "linear-gradient(to right, #ef4444, #f97316)";
        final String COLOR_GHOST    = "#2b3a55";

        String btnBase = "-fx-font-size: 15px; -fx-padding: 12 26; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 14, 0.2, 0, 4);";

        btnFootball.setMaxWidth(Double.MAX_VALUE);
        btnHandball.setMaxWidth(Double.MAX_VALUE);
        btnNextWeek.setMaxWidth(Double.MAX_VALUE);
        btnReset.setMaxWidth(Double.MAX_VALUE);
        teamSelector.setMaxWidth(Double.MAX_VALUE);

        btnFootball.setStyle(btnBase + "-fx-background-color: " + COLOR_FOOTBALL + ";");
        btnHandball.setStyle(btnBase + "-fx-background-color: " + COLOR_HANDBALL + ";");
        btnNextWeek.setStyle(btnBase + "-fx-background-color: " + COLOR_PRIMARY + ";");
        btnReset.setStyle("-fx-font-size: 13px; -fx-padding: 10 20; -fx-background-color: " + COLOR_GHOST
                + "; -fx-text-fill: " + COLOR_TEXT + "; -fx-font-weight: bold; -fx-cursor: hand; "
                + "-fx-background-radius: 12; -fx-border-color: #3b4a6b; -fx-border-radius: 12;");
        lblWeek.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_ACCENT + ";");
        lblSelectTeam.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_TEXT + ";");
        teamSelector.setStyle("-fx-font-size: 14px; -fx-background-color: " + COLOR_PANEL_2
                + "; -fx-text-fill: " + COLOR_TEXT + "; -fx-background-radius: 10; -fx-border-radius: 10;");

        // Tablo modern stil
        table.setStyle("-fx-background-color: " + COLOR_PANEL + "; -fx-control-inner-background: " + COLOR_PANEL
                + "; -fx-control-inner-background-alt: " + COLOR_PANEL_2 + "; -fx-text-fill: " + COLOR_TEXT
                + "; -fx-table-cell-border-color: transparent; -fx-background-radius: 12; -fx-border-radius: 12;"
                + "-fx-border-color: #2a3954; -fx-border-width: 1;");

        logs.setStyle("-fx-background-color: " + COLOR_PANEL + "; -fx-control-inner-background: " + COLOR_PANEL
                + "; -fx-text-fill: " + COLOR_TEXT + "; -fx-font-family: 'Consolas','Menlo',monospace; "
                + "-fx-font-size: 13px; -fx-background-radius: 12; -fx-border-radius: 12; "
                + "-fx-border-color: #2a3954; -fx-border-width: 1;");

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

        HBox sportButtons = new HBox(18, btnFootball, btnHandball);
        sportButtons.setAlignment(Pos.CENTER);

        HBox teamSelectBox = new HBox(12, lblSelectTeam, teamSelector);
        teamSelectBox.setAlignment(Pos.CENTER);

        HBox controlButtons = new HBox(15, btnNextWeek, btnReset);
        controlButtons.setAlignment(Pos.CENTER);

        // Üst başlık şeridi
        Label brand = new Label("⚡ Veteran3");
        brand.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: " + COLOR_ACCENT
                + "; -fx-effect: dropshadow(gaussian, rgba(34,211,238,0.55), 12, 0.3, 0, 0);");
        Label subtitle = new Label("Spor Simülatörü");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + COLOR_MUTED + "; -fx-font-weight: bold;");
        VBox brandBox = new VBox(0, brand, subtitle);
        brandBox.setAlignment(Pos.CENTER_LEFT);

        Label userBadge = new Label("👤  " + (currentUser == null ? "Misafir" : currentUser));
        userBadge.setStyle("-fx-font-size: 13px; -fx-text-fill: " + COLOR_TEXT
                + "; -fx-padding: 8 16; -fx-background-color: " + COLOR_PANEL_2
                + "; -fx-background-radius: 999; -fx-border-radius: 999; -fx-border-color: #2f3f5e; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(12, brandBox, spacer, userBadge);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setStyle("-fx-background-color: linear-gradient(to right, #131c2e, #1a2540);"
                + " -fx-background-radius: 16; -fx-border-radius: 16;"
                + " -fx-border-color: #2a3954; -fx-border-width: 1;"
                + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 18, 0.2, 0, 6);");

        // Sağ panel başlığı
        Label logsTitle = new Label("📜 Maç Sonuçları");
        logsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_ACCENT + ";");

        VBox right = new VBox(10, logsTitle, logs, tacticPanel);
        right.setPadding(new Insets(14));
        right.setStyle("-fx-background-color: " + COLOR_PANEL + "; -fx-background-radius: 16;"
                + " -fx-border-color: #2a3954; -fx-border-radius: 16; -fx-border-width: 1;");
        VBox.setVgrow(logs, Priority.ALWAYS);

        Label tableTitle = new Label("🏆 Puan Durumu");
        tableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_ACCENT + ";");
        VBox left = new VBox(10, tableTitle, table);
        left.setPadding(new Insets(14));
        left.setStyle("-fx-background-color: " + COLOR_PANEL + "; -fx-background-radius: 16;"
                + " -fx-border-color: #2a3954; -fx-border-radius: 16; -fx-border-width: 1;");
        VBox.setVgrow(table, Priority.ALWAYS);

        HBox content = new HBox(16, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        // Kontrol kartı
        VBox controlsCard = new VBox(12, sportButtons, teamSelectBox, lblWeek, controlButtons);
        controlsCard.setAlignment(Pos.CENTER);
        controlsCard.setPadding(new Insets(18));
        controlsCard.setStyle("-fx-background-color: " + COLOR_PANEL + "; -fx-background-radius: 16;"
                + " -fx-border-color: #2a3954; -fx-border-radius: 16; -fx-border-width: 1;");

        VBox root = new VBox(14, header, controlsCard, content);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, " + COLOR_BG + ", #0a0f1c);");
        VBox.setVgrow(content, Priority.ALWAYS);

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.setTitle("⚡ Arena — Spor Simülatörü");
        // 🆕 ESC menüsü
        PauseMenu.install(scene, stage,
                () -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("user=").append(currentUser == null ? "Misafir" : currentUser).append('\n');
                    sb.append("sport=").append(currentSport == null ? "football" : currentSport).append('\n');
                    sb.append("week=").append(currentWeek).append('\n');
                    sb.append("totalWeeks=").append(totalWeeks).append('\n');
                    sb.append("myTeam=").append(myTeam == null ? "-" : myTeam.getName()).append('\n');
                    for (Team t : teams) {
                        int gd = t.getGoalDifference();
                        // gf-ga = gd; basit yaklaşım: pozitifse gf=gd,ga=0; negatifse gf=0,ga=-gd
                        int gf = Math.max(0, gd);
                        int ga = Math.max(0, -gd);
                        sb.append("TEAM:").append(t.getName())
                          .append('|').append(t.getPoints())
                          .append('|').append(gf)
                          .append('|').append(ga).append('\n');
                    }
                    return sb.toString();
                },
                this::resetAll
        );

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
        tacticPanel.setSpacing(10);
        tacticPanel.setPadding(new Insets(14));
        tacticPanel.setStyle("-fx-background-color: linear-gradient(to bottom, #1f2a44, #16203a);"
                + " -fx-background-radius: 14; -fx-border-color: #ef4444; -fx-border-radius: 14; -fx-border-width: 2;"
                + " -fx-effect: dropshadow(gaussian, rgba(239,68,68,0.35), 16, 0.2, 0, 4);");
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
        teamSelector.setStyle("-fx-prompt-text-fill: white;");
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

        Match myMatch = null;
        List<Match> otherMatches = new ArrayList<>();
        for (Match m : weekMatches) {
            if (m.getTeamA() == myTeam || m.getTeamB() == myTeam) myMatch = m;
            else otherMatches.add(m);
        }

        // 🆕 Maç hazırlığı (diziliş + ilk 11 + maç öncesi diziliş ekranı) HAFTA OYNANMADAN ÖNCE.
        // Kullanıcı çarpıya basıp geri dönerse hafta hiç oynanmaz.
        String formation = null;
        if (myMatch != null) {
            formation = doMatchPrep(myMatch);
            if (formation == null) {
                logs.getItems().add("↩  Maç hazırlığı iptal edildi — hafta oynanmadı.");
                logs.scrollTo(logs.getItems().size() - 1);
                return;
            }
        }

        currentWeek++;
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
            logs.getItems().add("   📋 " + myTeam.getName() + " dizilişi: " + formation);
            logs.getItems().add("   👥 İlk " + (isHandball ? 7 : 11) + " onaylandı.");
            logs.scrollTo(logs.getItems().size() - 1);
            if (isHandball) playMyHandballMatchLive(myMatch, winPoints);
            else playMyFootballMatchLive(myMatch, winPoints);
        } else {
            logs.getItems().add("📋 Bu hafta maçın yok.");
            logs.scrollTo(logs.getItems().size() - 1);
            refreshStandings();
            checkSeasonEnd();
        }
    }

    /**
     * Maç öncesi hazırlık akışı: Diziliş → İlk Kadro → Maç Öncesi Diziliş.
     * Her ekrandaki sağ üst çarpıya basıldığında bir önceki ekrana dönülür.
     * En baştaki "Diziliş Seç" ekranında çarpıya basılırsa null döner (iptal).
     */
    private String doMatchPrep(Match m) {
        String formation = null;
        int step = 0;
        while (step < 3) {
            if (step == 0) {
                formation = LineupSelectorView.show(primaryStage, myTeam, isHandball);
                if (formation == null) return null; // iptal: önceki ekran (lig ana ekranı)
                step = 1;
            } else if (step == 1) {
                boolean ok = SquadSelectorView.show(primaryStage, myTeam, formation, isHandball);
                if (!ok) { step = 0; continue; } // geri: diziliş seç
                step = 2;
            } else {
                boolean started = LineupView.show(primaryStage, m.getTeamA(), m.getTeamB(),
                        isHandball, myTeam, formation);
                if (!started) { step = 1; continue; } // geri: ilk kadro
                step = 3;
            }
        }
        return formation;
    }

    // ==================== FUTBOL: CANLI + TAKTİK ====================
    private void playMyFootballMatchLive(Match m, int winPoints) {
        btnNextWeek.setDisable(true);
        btnReset.setDisable(true);
        // Maç öncesi hazırlık (diziliş + ilk 11 + saha) playNextWeek() içinde yapıldı.

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

            // 🆕 Devre arası: önce yedek değişikliği menüsü, sonra taktik
            showSubstitutionAndWait();
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
        // Sadece sahadaki ilk 11'den golcü seçilsin
        int startersA = Math.min(11, fm.getTeamA().getPlayers().size());
        int startersB = Math.min(11, fm.getTeamB().getPlayers().size());

        if (random.nextInt(100) < (powerA / 8)) {
            fm.addGoalA();
            Player scorer = fm.getTeamA().getPlayers().get(random.nextInt(startersA));
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
            Player scorer = fm.getTeamB().getPlayers().get(random.nextInt(startersB));
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
        // Maç öncesi hazırlık (diziliş + ilk 7 + saha) playNextWeek() içinde yapıldı.

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

            // 🆕 Devre arası: önce yedek değişikliği menüsü, sonra taktik
            showSubstitutionAndWait();
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
        // Golcü sadece sahadaki ilk 7'den
        int startersA = Math.min(7, hm.getTeamA().getPlayers().size());
        int startersB = Math.min(7, hm.getTeamB().getPlayers().size());

        double chanceA = ((double) attackA / (attackA + defenseB)) * 0.50;
        if (random.nextDouble() < chanceA) {
            hm.addGoalA();
            Player scorer = hm.getTeamA().getPlayers().get(random.nextInt(startersA));
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
            Player scorer = hm.getTeamB().getPlayers().get(random.nextInt(startersB));
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

    // 🆕 Devre arası yedek değişikliği — FX thread'inde blocking dialog açar, biter ve simülasyon devam eder
    private void showSubstitutionAndWait() {
        CountDownLatch latch = new CountDownLatch(1);
        int beforeSize = myTeam.getPlayers().size();
        // Saha kadrosu snapshot — log için
        int starters = isHandball ? 7 : 11;
        List<Player> beforeField = new ArrayList<>(myTeam.getPlayers().subList(0, Math.min(starters, beforeSize)));

        Platform.runLater(() -> {
            try {
                logs.getItems().add("");
                logs.getItems().add("   🔁 DEVRE ARASI — Oyuncu değişikliği menüsü");
                logs.scrollTo(logs.getItems().size() - 1);
                SubstitutionView.show(primaryStage, myTeam, isHandball);
                // Değişen oyuncuları logla
                List<Player> afterField = new ArrayList<>(
                        myTeam.getPlayers().subList(0, Math.min(starters, myTeam.getPlayers().size())));
                int subs = 0;
                for (int i = 0; i < beforeField.size(); i++) {
                    if (i >= afterField.size() || beforeField.get(i) != afterField.get(i)) subs++;
                }
                if (subs > 0) {
                    logs.getItems().add("   ✏ " + subs + " değişiklik yapıldı.");
                } else {
                    logs.getItems().add("   ⏭ Değişiklik yapılmadı.");
                }
                logs.scrollTo(logs.getItems().size() - 1);
            } finally {
                latch.countDown();
            }
        });
        try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
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
        // Sadece ilk 11'in gücü
        int starters = Math.min(11, team.getPlayers().size());
        double base = team.getPlayers().subList(0, starters).stream()
                .mapToInt(p -> p.getAttribute("Shooting") + p.getAttribute("Speed"))
                .average().orElse(50);
        if (team.getTactic() != null) base *= team.getTactic().getAttackModifier();
        return (int) base;
    }

    private int calcPower(Team team, String attr1, String attr2) {
        // Sadece ilk 7'nin gücü
        int starters = Math.min(7, team.getPlayers().size());
        return (int) team.getPlayers().subList(0, starters).stream()
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
        // Yeni sezona başlanırken kayıt dosyasını da temizle
        try { new File("savegame.txt").delete(); } catch (Exception ignored) {}
    }

    /** Kayıttan sezonu yeniden kurar: takımlar, fikstür, puan durumu ve hafta. */
    private void restoreSeason(List<String[]> teamRows, String myTeamName, int week) {
        loadNameFiles();
        Set<String> usedNames = new HashSet<>();
        teams.clear();
        weeklySchedule.clear();
        logs.getItems().clear();

        for (String[] r : teamRows) {
            if (r.length < 1) continue;
            Team t = new Team(r[0]);
            if (isHandball) initHandballPlayers(t, usedNames);
            else initFootballPlayers(t, usedNames);
            try {
                if (r.length >= 2) t.addPoints(Integer.parseInt(r[1]));
                int gf = r.length >= 3 ? Integer.parseInt(r[2]) : 0;
                int ga = r.length >= 4 ? Integer.parseInt(r[3]) : 0;
                if (gf != 0 || ga != 0) t.recordMatch(gf, ga);
            } catch (Exception ignore) {}
            teams.add(t);
        }

        myTeam = teams.stream().filter(t -> t.getName().equals(myTeamName))
                .findFirst().orElse(teams.isEmpty() ? null : teams.get(0));

        generateWeeklyFixture();
        totalWeeks = weeklySchedule.size();
        currentWeek = Math.min(Math.max(0, week), totalWeeks);

        String sportName = isHandball ? "HENTBOL" : "FUTBOL";
        logs.getItems().add("💾 Kayıtlı oyun yüklendi — " + sportName + " ligi.");
        logs.getItems().add("   Takımın: " + (myTeam == null ? "-" : myTeam.getName())
                + "  |  Hafta: " + currentWeek + "/" + totalWeeks);
        logs.getItems().add("▶ 'Sonraki Hafta' ile devam edebilirsin.");

        table.setItems(FXCollections.observableArrayList(teams));
        refreshStandings();

        // UI durumunu, sezon devam ediyormuş gibi ayarla
        btnFootball.setDisable(true);
        btnHandball.setDisable(true);
        teamSelector.setVisible(false);
        lblSelectTeam.setVisible(false);
        btnNextWeek.setVisible(true);
        btnNextWeek.setDisable(currentWeek >= totalWeeks);
        if (currentWeek >= totalWeeks) btnNextWeek.setText("✅ Lig Tamamlandı");
        btnReset.setVisible(true);
        btnReset.setDisable(false);

        if (currentWeek >= totalWeeks) {
            lblWeek.setText("LİG BİTTİ — Şampiyon: " + teams.get(0).getName());
        } else {
            lblWeek.setText(sportName + " LİGİ — Hafta " + currentWeek + "/" + totalWeeks
                    + " | Takımın: " + (myTeam == null ? "-" : myTeam.getName()));
        }
    }

    // ==================== OYUNCU OLUŞTUR ====================
    // 🆕 18 oyuncu: 2 GK + 6 DEF + 6 MID + 4 FW (yedekleri pozisyona göre seçebilmek için)
    private void initFootballPlayers(Team t, Set<String> usedNames) {
        String[] positions = {
                "Goalkeeper","Goalkeeper",
                "Defender","Defender","Defender","Defender","Defender","Defender",
                "Midfielder","Midfielder","Midfielder","Midfielder","Midfielder","Midfielder",
                "Forward","Forward","Forward","Forward"
        };
        for (int i = 0; i < positions.length; i++) {
            String playerName = randomFullName(usedNames);
            FootballPlayer p = new FootballPlayer(playerName, i + 1, positions[i]);
            p.setAttribute("Shooting", 50 + (int)(Math.random() * 25));
            p.setAttribute("Speed", 50 + (int)(Math.random() * 25));
            p.setAttribute("Passing", 50 + (int)(Math.random() * 25));
            p.setAttribute("Goalkeeping", positions[i].equals("Goalkeeper") ? 70 + (int)(Math.random() * 20) : 30);
            t.addPlayer(p);
        }
    }

    // 🆕 12 oyuncu: 2 GK + 10 outfield (LW/LB/CB/RB/RW/PV ikişerli)
    private void initHandballPlayers(Team t, Set<String> usedNames) {
        String[] positions = {
                "GK","GK",
                "LW","LW","LB","LB","CB","CB","RB","RB","RW","PV"
        };
        for (int i = 0; i < positions.length; i++) {
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
