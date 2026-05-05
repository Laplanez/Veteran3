package org.example;

import java.util.ArrayList;
import java.util.List;

public class MainFootball {
    public static void main(String[] args) {
        // 1. Motor ve Taktik Kurulumu
        // Futbol motoru 90 dakika üzerinden simülasyon yapar
        MatchEngine footballEngine = new FootballEngine();
        Tactic attackTactic = Tactic.createAttack(); // %120 Hücum, %80 Savunma[cite: 1]
        Tactic defenseTactic = Tactic.createDefense(); // %80 Hücum, %120 Savunma[cite: 1]

        // 2. Takım A Kurulumu (Hücum Odaklı)
        Team teamA = new Team("FC Barcelona");
        teamA.setTactic(attackTactic);
        for (int i = 0; i < 11; i++) {
            // Futbolcular için Shooting, Speed, Passing ve Goalkeeping atanır
            FootballPlayer p = new FootballPlayer("A-Striker" + i, 100 + i, "Field");
            p.setAttribute("Shooting", 75);
            p.setAttribute("Speed", 70);
            p.setAttribute("Passing", 65);
            teamA.addPlayer(p);
        }

        // 3. Takım B Kurulumu (Savunma Odaklı)
        Team teamB = new Team("Juventus");
        teamB.setTactic(defenseTactic);
        for (int i = 0; i < 11; i++) {
            FootballPlayer p = new FootballPlayer("B-Defender" + i, 200 + i, "Field");
            p.setAttribute("Shooting", 40);
            p.setAttribute("Speed", 50);
            p.setAttribute("Passing", 60);
            p.setAttribute("Goalkeeping", 70); // Kaleci yeteneği daha yüksek[cite: 4]
            teamB.addPlayer(p);
        }

        // 4. Lig ve Fikstür Oluşturma
        // Futbolda galibiyet 3, beraberlik 1 puandır
        League footballLeague = new League();
        footballLeague.addTeam(teamA);
        footballLeague.addTeam(teamB);

        List<Team> participants = new ArrayList<>();
        participants.add(teamA);
        participants.add(teamB);

        FixtureGenerator fg = new FixtureGenerator();
        // Fikstür jeneratörü varsayılan olarak FootballMatch döner[cite: 2]
        List<Match> matches = fg.generate(participants, footballEngine);
        footballLeague.setSchedule(matches);

        // 5. Sezonu Başlat
        System.out.println("Futbol Sezonu Başlıyor (90 Dakikalık Maçlar)...");
        footballLeague.runSeason();

        // 6. Sonuçları Yazdır[cite: 12]
        System.out.println("\n--- Maç Sonuçları ---");
        for (Match m : footballLeague.getSchedule()) {
            System.out.println(m.getResult());
        }

        System.out.println("\n--- Puan Durumu ---");
        for (Team t : footballLeague.getTeams()) {
            System.out.println("Takım: " + t.getName() +
                    " | Puan: " + t.getPoints() +
                    " | Averaj: " + t.getGoalDifference());
        }
    }
}