package org.example;

import javafx.application.Platform;
import java.util.Random;
import java.util.function.Consumer;

class HandballEngine implements MatchEngine {
    private final Random random = new Random();
    private Consumer<String> goalLogger;

    public void setOnGoalScored(Consumer<String> logger) {
        this.goalLogger = logger;
    }

    @Override
    public void simulate(Match match) {
        if (!(match instanceof HandballMatch hm)) return;

        // Güç dengelerini hesapla
        int attackA = calculatePower(match.getTeamA(), "Throwing", "Speed");
        int defenseB = calculatePower(match.getTeamB(), "Goalkeeping", "Defense");
        int attackB = calculatePower(match.getTeamB(), "Throwing", "Speed");
        int defenseA = calculatePower(match.getTeamA(), "Goalkeeping", "Defense");

        int totalSeconds = 0;
        // Maç tam 3600 saniye (60 dakika) sürecek
        while (totalSeconds < 3600) {
            // Her hücum yaklaşık 30 saniye sürer (Hentbol temposu)
            int timeStep = 25 + random.nextInt(11);
            totalSeconds += timeStep;

            // 60 dakikayı aşmamasını sağla
            if (totalSeconds > 3600) totalSeconds = 3600;

            int min = totalSeconds / 60;
            int sec = totalSeconds % 60;

            // Katsayıyı 0.50 civarına çektik.
            // 30 saniyelik adımlarla 120 pozisyon olur, %50 başarı = ~60 Toplam Gol[cite: 1]
            double chanceA = ((double) attackA / (attackA + defenseB)) * 0.50;
            if (random.nextDouble() < chanceA) {
                hm.addGoalA();
                log(min, sec, match.getTeamA(), hm);
            }

            double chanceB = ((double) attackB / (attackB + defenseA)) * 0.50;
            if (random.nextDouble() < chanceB) {
                hm.addGoalB();
                log(min, sec, match.getTeamB(), hm);
            }

            try {
                // Arayüz akış hızı (Hızlı izlemek için 50-100ms idealdir)[cite: 1]
                Thread.sleep(70);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (goalLogger != null) {
            Platform.runLater(() -> goalLogger.accept("--- MAÇ BİTTİ (60:00) ---"));
        }
    }

    private void log(int m, int s, Team t, HandballMatch hm) {
        Player p = t.getPlayers().get(random.nextInt(t.getPlayers().size()));
        String msg = String.format("[%02d:%02d] GOL! %s - %s (%d-%d)", m, s, t.getName(), p.getName(), hm.getScoreA(), hm.getScoreB());
        if (goalLogger != null) Platform.runLater(() -> goalLogger.accept(msg));
    }

    // Birden fazla özelliği hesaba katan yeni güç hesaplayıcı
    private int calculatePower(Team team, String attr1, String attr2) {
        return (int) team.getPlayers().stream()
                .mapToInt(p -> (p.getAttribute(attr1) + p.getAttribute(attr2)) / 2)
                .average().orElse(50);
    }
}