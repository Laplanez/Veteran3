package org.example;

class HandballPlayer extends Player {
    public HandballPlayer(String name, int age, String position) {
        super(name, age, position);
        setAttribute("Throwing", 50);
        setAttribute("Speed", 50);
        setAttribute("Defense", 50);
        setAttribute("Goalkeeping", 50);
    }

    public int getThrowing()    { return getAttribute("Throwing"); }
    public int getSpeed()       { return getAttribute("Speed"); }
    public int getDefense()     { return getAttribute("Defense"); }
    public int getGoalkeeping() { return getAttribute("Goalkeeping"); }

    @Override
    public String toString() {
        return getName() + " (" + getPosition() + ") "
                + "THR:" + getThrowing()
                + " SPD:" + getSpeed()
                + " DEF:" + getDefense()
                + " GK:"  + getGoalkeeping();
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
        engine.simulate(this);
        isFinished = true;
    }

    private void simulateTwoHalves() {
        engine.simulate(this);
    }

    public void addGoalA() { scoreA++; }
    public void addGoalB() { scoreB++; }
}
