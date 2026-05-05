package org.example;

import java.util.Random;

class FootballEngine implements MatchEngine {
    private java.util.Random random = new java.util.Random();

    @Override
    public void simulate(Match match) {
        if (!(match instanceof FootballMatch)) return;
        FootballMatch fm = (FootballMatch) match;

        int powerA = calculateTeamPower(match.getTeamA());
        int powerB = calculateTeamPower(match.getTeamB());

        for (int i = 0; i < 5; i++) {
            if (random.nextInt(100) < (powerA / 10)) fm.addGoalA();
            if (random.nextInt(100) < (powerB / 10)) fm.addGoalB();
        }
    }

    private int calculateTeamPower(Team team) {
        double base = team.getPlayers().stream()
                .mapToInt(p -> p.getAttribute("Shooting") + p.getAttribute("Speed"))
                .average().orElse(50);

        if (team.getTactic() != null) {
            base *= team.getTactic().getAttackModifier();
        }
        return (int) base;
    }
}