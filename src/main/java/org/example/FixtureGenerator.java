package org.example;

import java.util.ArrayList;
import java.util.List;

class FixtureGenerator {

    public List<Match> generate(List<Team> teams, MatchEngine engine) {
        List<Match> schedule = new ArrayList<>();
        int numTeams = teams.size();
        if (numTeams % 2 != 0) {
            return schedule;
        }

        List<Team> pool = new ArrayList<>(teams);
        int rounds = numTeams - 1;
        int halfSize = numTeams / 2;

        for (int round = 0; round < rounds; round++) {
            for (int i = 0; i < halfSize; i++) {
                Team a = pool.get(i);
                Team b = pool.get(numTeams - 1 - i);
                schedule.add(new FootballMatch(a, b, engine));
            }
            // Rotate pool (keep first element fixed)
            Team last = pool.remove(numTeams - 1);
            pool.add(1, last);
        }
        return schedule;
    }
}