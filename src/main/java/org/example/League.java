package org.example;

import java.util.ArrayList;
import java.util.List;

public class League {
    private List<Team> teams = new ArrayList<>();
    private List<Match> schedule = new ArrayList<>();

    public void addTeam(Team t) { teams.add(t); }
    public List<Team> getTeams() { return teams; }
    public void setSchedule(List<Match> s) { this.schedule = s; }
    public List<Match> getSchedule() { return schedule; }

    public void runSeason() {
        for (Match m : schedule) {
            m.play();
            updateTeamStats(m);
        }
    }

    private void updateTeamStats(Match m) {
        Team a = m.getTeamA();
        Team b = m.getTeamB();
        a.recordMatch(m.getScoreA(), m.getScoreB());
        b.recordMatch(m.getScoreB(), m.getScoreA());

        if (m.getScoreA() > m.getScoreB()) a.addPoints(3);
        else if (m.getScoreB() > m.getScoreA()) b.addPoints(3);
        else {
            a.addPoints(1);
            b.addPoints(1);
        }
    }
}