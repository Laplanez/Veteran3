package org.example;

import java.util.Random;

class HandballEngine implements MatchEngine {
    private Random random = new Random();

    @Override
    public void simulate(Match match) {
        if (!(match instanceof HandballMatch)) return;
        HandballMatch hm = (HandballMatch) match;

        int attackA  = calculateAttackPower(match.getTeamA());
        int defenseA = calculateDefensePower(match.getTeamA());
        int attackB  = calculateAttackPower(match.getTeamB());
        int defenseB = calculateDefensePower(match.getTeamB());

        // Handball is high-scoring: ~60 attacking chances per team
        int chances = 60;
        for (int i = 0; i < chances; i++) {
            if (random.nextInt(100) < Math.max(10, attackA - (defenseB / 2))) hm.addGoalA();
            if (random.nextInt(100) < Math.max(10, attackB - (defenseA / 2))) hm.addGoalB();
        }
    }

    private int calculateAttackPower(Team team) {
        return (int) team.getPlayers().stream()
                .mapToInt(p -> p.getAttribute("Throwing")
                        + p.getAttribute("Speed")
                        + p.getAttribute("Passing"))
                .average().orElse(150) / 3;
    }

    private int calculateDefensePower(Team team) {
        return (int) team.getPlayers().stream()
                .mapToInt(p -> p.getAttribute("Defense")
                        + p.getAttribute("Goalkeeping"))
                .average().orElse(100) / 2;
    }
}

