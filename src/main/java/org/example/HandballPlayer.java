package org.example;

public class HandballPlayer extends Player {


    public HandballPlayer(String name, int id) {
        super(name, id);
        setAttribute("Throwing Power", 30);
        setAttribute("Jumping", 30);
        setAttribute("Goalkeeping", 50);
        setAttribute("Strength", 30);
    }




}
class HandballMatch extends Match {
    private MatchEngine engine;

    public HandballMatch(Team a, Team b, MatchEngine engine) {
        super(a, b);
        this.engine = engine;
    }

    @Override
    public void play() {
        //since a handball game consists two 30 minute halves no changes has been made
        simulateTwoHalves();
               isFinished = true;
    }

    private void simulateTwoHalves() {
        engine.simulate(this);
    }

    public void addGoalA() { scoreA++; }
    public void addGoalB() { scoreB++; }
}
