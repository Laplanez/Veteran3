package org.example;

import java.util.ArrayList;
import java.util.List;

class HandballSport implements ISport {
    private final MatchEngine engine = new HandballEngine();

    @Override
    public String getSportName() { return "Handball"; }

    @Override
    public int getPlayersPerTeam() { return 7; } // 6 outfield + 1 goalkeeper

    @Override
    public MatchEngine getMatchEngine() { return engine; }

    @Override
    public List<String> getPlayerAttributes() {
        List<String> attrs = new ArrayList<>();
        attrs.add("Throwing");
        attrs.add("Goalkeeping");
        attrs.add("Passing");
        attrs.add("Speed");
        attrs.add("Defense");
        return attrs;
    }
}
