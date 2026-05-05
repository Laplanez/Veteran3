package org.example;

// Same as League, but a win is worth 2 points (handball rules)
// instead of 3. Draw still gives 1 point to each team.
class HandballLeague extends League {

    @Override
    public void runSeason() {
        for (Match m : getSchedule()) {
            m.play();
            updateHandballStats(m);
        }
    }

    private void updateHandballStats(Match m) {
        Team a = m.getTeamA();
        Team b = m.getTeamB();
        a.recordMatch(m.getScoreA(), m.getScoreB());
        b.recordMatch(m.getScoreB(), m.getScoreA());

        if (m.getScoreA() > m.getScoreB()) {
            a.addPoints(2);
        } else if (m.getScoreB() > m.getScoreA()) {
            b.addPoints(2);
        } else {
            a.addPoints(1);
            b.addPoints(1);
        }
    }
}
