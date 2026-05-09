package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PauseMenu {

    public interface SaveProvider {
        /** Kaydedilecek oyun durumunu metin olarak döner. */
        String collectSaveData();
    }

    public interface MainMenuAction {
        void goToMainMenu();
    }

    private static Stage activeStage = null;

    /** Sahnedeki ESC tuşuna menüyü bağlar. Sahne her değiştiğinde bir kez çağır. */
    public static void install(Scene scene, Stage owner,
                               SaveProvider saveProvider,
                               MainMenuAction mainMenuAction) {
        scene.setOnKeyPressed(ev -> {
            if (ev.getCode().toString().equals("ESCAPE")) {
                if (activeStage != null && activeStage.isShowing()) {
                    activeStage.close();
                    activeStage = null;
                } else {
                    show(owner, saveProvider, mainMenuAction);
                }
            }
        });
    }

    public static void show(Stage owner, SaveProvider saveProvider, MainMenuAction mainMenuAction) {
        // Tema renkleri (JavaFXMain ile aynı palette)
        final String COLOR_BG      = "#0f1623";
        final String COLOR_PANEL   = "#1a2332";
        final String COLOR_PANEL_2 = "#222e42";
        final String COLOR_TEXT    = "#e6edf7";
        final String COLOR_MUTED   = "#8aa0bd";
        final String COLOR_ACCENT  = "#22d3ee";
        final String COLOR_PRIMARY = "linear-gradient(to right, #2563eb, #22d3ee)";
        final String COLOR_DANGER  = "linear-gradient(to right, #ef4444, #f97316)";
        final String COLOR_GHOST   = "#2b3a55";

        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        activeStage = dialog;

        Label title = new Label("⏸  DURAKLATILDI");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: " + COLOR_ACCENT
                + "; -fx-effect: dropshadow(gaussian, rgba(34,211,238,0.55), 12, 0.3, 0, 0);");

        Label hint = new Label("ESC ile kapat");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: " + COLOR_MUTED + ";");

        String btnBase = "-fx-font-size: 15px; -fx-padding: 12 26; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 14, 0.2, 0, 4);";

        Button btnResume = new Button("▶  Devam Et");
        Button btnSave   = new Button("💾  Oyunu Kaydet");
        Button btnSettings = new Button("⚙  Ayarlar");
        Button btnMain   = new Button("🏠  Ana Menüye Dön");
        Button btnExit   = new Button("⏻  Oyundan Çık");

        for (Button b : List.of(btnResume, btnSave, btnSettings, btnMain, btnExit)) {
            b.setMaxWidth(Double.MAX_VALUE);
        }
        btnResume.setStyle(btnBase + "-fx-background-color: " + COLOR_PRIMARY + ";");
        btnSave.setStyle(btnBase + "-fx-background-color: linear-gradient(to right, #16a34a, #22c55e);");
        btnSettings.setStyle(btnBase + "-fx-background-color: " + COLOR_GHOST + ";");
        btnMain.setStyle(btnBase + "-fx-background-color: " + COLOR_GHOST + ";");
        btnExit.setStyle(btnBase + "-fx-background-color: " + COLOR_DANGER + ";");

        btnResume.setOnAction(e -> { activeStage = null; dialog.close(); });

        btnSave.setOnAction(e -> {
            String data = saveProvider != null ? saveProvider.collectSaveData() : "";
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File file = new File("savegame_" + stamp + ".txt");
            try (PrintWriter w = new PrintWriter(new FileWriter(file))) {
                w.println("# Veteran3 Save  " + stamp);
                w.println(data);
                showInfo(owner, "Kaydedildi", "Oyun kaydedildi:\n" + file.getAbsolutePath());
            } catch (IOException ex) {
                showInfo(owner, "Hata", "Kaydedilemedi: " + ex.getMessage());
            }
        });

        btnSettings.setOnAction(e -> showSettings(owner));

        btnMain.setOnAction(e -> {
            activeStage = null;
            dialog.close();
            if (mainMenuAction != null) mainMenuAction.goToMainMenu();
        });

        btnExit.setOnAction(e -> {
            activeStage = null;
            javafx.application.Platform.exit();
            System.exit(0);
        });

        VBox box = new VBox(12, title, hint, new Separator(),
                btnResume, btnSave, btnSettings, btnMain, btnExit);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(28, 36, 28, 36));
        box.setStyle("-fx-background-color: linear-gradient(to bottom, " + COLOR_PANEL + ", " + COLOR_PANEL_2 + ");"
                + " -fx-background-radius: 18; -fx-border-radius: 18;"
                + " -fx-border-color: " + COLOR_ACCENT + "; -fx-border-width: 2;"
                + " -fx-effect: dropshadow(gaussian, rgba(34,211,238,0.35), 24, 0.3, 0, 6);");

        StackPane root = new StackPane(box);
        root.setStyle("-fx-background-color: rgba(8,12,22,0.78);");
        root.setPadding(new Insets(40));

        Scene s = new Scene(root, 460, 520);
        s.setFill(Color.TRANSPARENT);
        s.setOnKeyPressed(ev -> {
            if (ev.getCode().toString().equals("ESCAPE")) {
                activeStage = null;
                dialog.close();
            }
        });
        dialog.setScene(s);
        dialog.setOnHidden(ev -> { if (activeStage == dialog) activeStage = null; });
        dialog.showAndWait();
    }

    // ==================== AYARLAR ====================
    private static void showSettings(Stage owner) {
        final String COLOR_PANEL   = "#1a2332";
        final String COLOR_PANEL_2 = "#222e42";
        final String COLOR_TEXT    = "#e6edf7";
        final String COLOR_ACCENT  = "#22d3ee";
        final String COLOR_PRIMARY = "linear-gradient(to right, #2563eb, #22d3ee)";

        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label title = new Label("⚙  Ayarlar");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: " + COLOR_ACCENT + ";");

        // Tam ekran
        Label lFs = label("Tam Ekran", COLOR_TEXT);
        CheckBox cbFs = new CheckBox();
        cbFs.setSelected(GameSettings.fullscreen || (owner != null && owner.isFullScreen()));
        cbFs.selectedProperty().addListener((o, ov, nv) -> {
            GameSettings.fullscreen = nv;
            if (owner != null) owner.setFullScreen(nv);
        });
        styleCheck(cbFs, COLOR_TEXT);

        // Maç hızı
        Label lSpeed = label("Maç Hızı: " + fmt(GameSettings.matchSpeed) + "x", COLOR_TEXT);
        Slider sSpeed = new Slider(0.5, 3.0, GameSettings.matchSpeed);
        sSpeed.setMajorTickUnit(0.5);
        sSpeed.setBlockIncrement(0.25);
        sSpeed.valueProperty().addListener((o, ov, nv) -> {
            GameSettings.matchSpeed = nv.doubleValue();
            lSpeed.setText("Maç Hızı: " + fmt(GameSettings.matchSpeed) + "x");
        });

        // Ses
        Label lSnd = label("Ses Efektleri", COLOR_TEXT);
        CheckBox cbSnd = new CheckBox();
        cbSnd.setSelected(GameSettings.soundEnabled);
        cbSnd.selectedProperty().addListener((o, ov, nv) -> GameSettings.soundEnabled = nv);
        styleCheck(cbSnd, COLOR_TEXT);

        // Otomatik log scroll
        Label lScroll = label("Logları Otomatik Kaydır", COLOR_TEXT);
        CheckBox cbScroll = new CheckBox();
        cbScroll.setSelected(GameSettings.autoScrollLogs);
        cbScroll.selectedProperty().addListener((o, ov, nv) -> GameSettings.autoScrollLogs = nv);
        styleCheck(cbScroll, COLOR_TEXT);

        // Gol animasyonları
        Label lAnim = label("Gol Vurgusu", COLOR_TEXT);
        CheckBox cbAnim = new CheckBox();
        cbAnim.setSelected(GameSettings.showGoalAnimations);
        cbAnim.selectedProperty().addListener((o, ov, nv) -> GameSettings.showGoalAnimations = nv);
        styleCheck(cbAnim, COLOR_TEXT);

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(14);
        grid.add(lFs, 0, 0);     grid.add(cbFs, 1, 0);
        grid.add(lSpeed, 0, 1, 2, 1); grid.add(sSpeed, 0, 2, 2, 1);
        grid.add(lSnd, 0, 3);    grid.add(cbSnd, 1, 3);
        grid.add(lScroll, 0, 4); grid.add(cbScroll, 1, 4);
        grid.add(lAnim, 0, 5);   grid.add(cbAnim, 1, 5);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, new ColumnConstraints());

        Button btnClose = new Button("✓  Tamam");
        btnClose.setMaxWidth(Double.MAX_VALUE);
        btnClose.setStyle("-fx-font-size: 14px; -fx-padding: 10 22; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand; "
                + "-fx-background-color: " + COLOR_PRIMARY + ";");
        btnClose.setOnAction(e -> dialog.close());

        VBox box = new VBox(16, title, new Separator(), grid, btnClose);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(24, 28, 24, 28));
        box.setStyle("-fx-background-color: linear-gradient(to bottom, " + COLOR_PANEL + ", " + COLOR_PANEL_2 + ");"
                + " -fx-background-radius: 16; -fx-border-radius: 16;"
                + " -fx-border-color: " + COLOR_ACCENT + "; -fx-border-width: 2;");

        StackPane root = new StackPane(box);
        root.setStyle("-fx-background-color: rgba(8,12,22,0.78);");
        root.setPadding(new Insets(40));

        Scene s = new Scene(root, 480, 480);
        s.setFill(Color.TRANSPARENT);
        s.setOnKeyPressed(ev -> { if (ev.getCode().toString().equals("ESCAPE")) dialog.close(); });
        dialog.setScene(s);
        dialog.showAndWait();
    }

    private static Label label(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        return l;
    }

    private static void styleCheck(CheckBox cb, String color) {
        cb.setStyle("-fx-text-fill: " + color + ";");
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }

    private static void showInfo(Stage owner, String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.setTitle(title);
        if (owner != null) a.initOwner(owner);
        a.showAndWait();
    }
}
