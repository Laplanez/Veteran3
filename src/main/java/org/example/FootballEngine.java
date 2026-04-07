package org.example;

import java.util.Random;

class FootballEngine implements MatchEngine {
    private Random random = new Random();

    @Override
    public void simulate(Match match) {
        if (!(match instanceof FootballMatch)) return;
        FootballMatch fm = (FootballMatch) match;

        int powerA = calculateTeamPower(match.getTeamA());
        int powerB = calculateTeamPower(match.getTeamB());

        for (int i = 0; i < 5; i++) { // 5 chances per game
            if (random.nextInt(100) < (powerA / 10)) fm.addGoalA();
            if (random.nextInt(100) < (powerB / 10)) fm.addGoalB();
        }
    }

    private int calculateTeamPower(Team team) {
        return (int) team.getPlayers().stream()
                .mapToInt(p -> p.getAttribute("Shooting") + p.getAttribute("Speed"))
                .average().orElse(50);
    }
}