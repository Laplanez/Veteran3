package org.example;

public class FootballPlayer extends Player {
    public FootballPlayer(String name, int id) {
        super(name, id);
        // Default is 50 but we can change to 10 maybe
        setAttribute("Shooting", 50);
        setAttribute("Goalkeeping", 50);
        setAttribute("Passing", 50);
        setAttribute("Speed", 50);
    }
}

class FootballMatch extends Match {
    private MatchEngine engine;

    public FootballMatch(Team a, Team b, MatchEngine engine) {
        super(a, b);
        this.engine = engine;
    }

    @Override
    public void play() {
        simulateTwoHalves();
        isFinished = true;
    }

    private void simulateTwoHalves() {
        engine.simulate(this);
    }

    public void addGoalA() { scoreA++; }
    public void addGoalB() { scoreB++; }
}