package org.example;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Sports Manager: Milestone 2 Simulation ===");

        League league = new League();
        Team t1 = new Team("Veteran FC");
        Team t2 = new Team("Eagle United");
        Team t3 = new Team("Lion City");
        Team t4 = new Team("Shark Base");


        for (int i = 0; i < 11; i++) {
            t1.addPlayer(new FootballPlayer("Player " + i, i));
            t2.addPlayer(new FootballPlayer("Opponent " + i, i + 100));
            t3.addPlayer(new FootballPlayer("Lion " + i, i + 200));
            t4.addPlayer(new FootballPlayer("Shark " + i, i + 300));
        }

        league.addTeam(t1);
        league.addTeam(t2);
        league.addTeam(t3);


        league.addTeam(t4);

        FixtureGenerator gen = new FixtureGenerator();
        MatchEngine engine = new FootballEngine();
        List<Match> fixtures = gen.generate(league.getTeams(), engine);
        league.setSchedule(fixtures);

        System.out.println("Fixtures generated: " + fixtures.size());

        league.runSeason();

        System.out.println("\nFinal League Standings:");
        league.getTeams().stream()
                .sorted((a, b) -> b.getPoints() - a.getPoints())
                .forEach(t -> System.out.println(t.getName() + " - Pts: " + t.getPoints() + " | GD: " + t.getGoalDifference()));

    }













    }
















