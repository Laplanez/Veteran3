package org.example;

import java.util.ArrayList;
import java.util.List;

class Team {
    private String name;
    private List<Player> players = new ArrayList<>();
    private Tactic tactic;
    private Coach coach;

    // Stats for league
    private int points;
    private int goalsFor;
    private int goalsAgainst;

    public Team(String name) { this.name = name; }

    public void addPlayer(Player p) { players.add(p); }
    public List<Player> getPlayers() { return players; }
    public String getName() { return name; }
    public void setTactic(Tactic t) { this.tactic = t; }
    public Tactic getTactic() { return tactic; }
    public void setCoach(Coach c) { this.coach = c; }
    public Coach getCoach() { return coach; }

    // League Stats Getters/Setters
    public int getPoints() { return points; }
    public void addPoints(int p) { this.points += p; }
    public int getGoalDifference() { return goalsFor - goalsAgainst; }
    public void recordMatch(int scored, int conceded) {
        this.goalsFor += scored;
        this.goalsAgainst += conceded;
    }
}