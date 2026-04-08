package org.example;

abstract class Match {
    protected Team teamA;
    protected Team teamB;
    protected int scoreA;
    protected int scoreB;   // different for the report we need 2 team scores separately
    protected boolean isFinished = false;

    public Match(Team teamA, Team teamB) {
        this.teamA = teamA;
        this.teamB = teamB;
    }

    public abstract void play();

    public String getResult() {
        return teamA.getName() + " " + scoreA + " --- " + scoreB + " " + teamB.getName();
    }

    public Team getTeamA() { return teamA; }
    public Team getTeamB() { return teamB; }
    public int getScoreA() { return scoreA; }
    public int getScoreB() { return scoreB; }
    public boolean isFinished() { return isFinished; }
}

