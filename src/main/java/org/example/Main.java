package org.example;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Sport and Engine Setup
        HandballSport handball = new HandballSport();
        MatchEngine engine = handball.getMatchEngine();

        // 2. Tactics Setup
        Tactic attackTactic = new Tactic("Attack", 1.2, 0.8);
        Tactic defenseTactic = new Tactic("Defense", 0.8, 1.2);

        // 3. Team A Setup
        Team teamA = new Team("Team Alpha");
        teamA.setTactic(attackTactic);
        for (int i = 0; i < 7; i++) {
            HandballPlayer p = new HandballPlayer("A-Player" + i, 20 + i, "Field");
            p.setAttribute("Throwing", 60);
            p.setAttribute("Speed", 55);
            p.setAttribute("Passing", 50);
            p.setAttribute("Defense", 40);
            p.setAttribute("Goalkeeping", 40);
            teamA.addPlayer(p);
        }

        // 4. Team B Setup
        Team teamB = new Team("Team Beta");
        teamB.setTactic(defenseTactic);
        for (int i = 0; i < 7; i++) {
            HandballPlayer p = new HandballPlayer("B-Player" + i, 20 + i, "Field");
            p.setAttribute("Throwing", 45);
            p.setAttribute("Speed", 45);
            p.setAttribute("Passing", 50);
            p.setAttribute("Defense", 65);
            p.setAttribute("Goalkeeping", 60);
            teamB.addPlayer(p);
        }

        // 5. League and Fixture Generation
        HandballLeague league = new HandballLeague();
        league.addTeam(teamA);
        league.addTeam(teamB);

        List<Team> teams = new ArrayList<>();
        teams.add(teamA);
        teams.add(teamB);

        FixtureGenerator fg = new FixtureGenerator();
        List<Match> matches = fg.generate(teams, engine);

        // Manual conversion if FixtureGenerator returns FootballMatch by default
        // normally it would return sport-specific match types
        List<Match> handballMatches = new ArrayList<>();
        for (Match m : matches) {
            handballMatches.add(new HandballMatch(m.getTeamA(), m.getTeamB(), engine));
        }
        league.setSchedule(handballMatches);

        // 6. Run Season
        System.out.println("Starting Handball Match Simulation...");
        league.runSeason();

        // 7. Results Output
        System.out.println("\n--- Match Results ---");
        for (Match m : league.getSchedule()) {
            System.out.println(m.getResult());
        }

        System.out.println("\n--- League Standings ---");
        for (Team t : league.getTeams()) {
            System.out.println("Team: " + t.getName() +
                    " | Points: " + t.getPoints() +
                    " | Goal Diff: " + t.getGoalDifference());
        }
    }
}