package org.example;

abstract class Sport {

    protected String sportName;
    protected int numberOfTeams;
    protected int numberOfPlayers;
    protected int matchDuration;


    public Sport(String sportName, int numberOfTeams, int numberOfPlayers, int matchDuration) {
        this.sportName = sportName;
        this.numberOfTeams = numberOfTeams;
        this.numberOfPlayers = numberOfPlayers;
        this.matchDuration = matchDuration;

    }

    public abstract void startGame();
    public abstract void endGame();
    public abstract void totalScore();


    public void displayInfo() {
        System.out.println("Sport: " + sportName);
        System.out.println("Teams: " + numberOfTeams);
        System.out.println("Players per team: " + numberOfPlayers);
        System.out.println("Match duration: " + matchDuration);
    }

}