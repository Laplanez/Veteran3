package org.example;

import java.util.List;

// Im gonna add all interfaces here

interface ISport {
    String getSportName();
    int getPlayersPerTeam();
    MatchEngine getMatchEngine();
    List<String> getPlayerAttributes();
}


interface MatchEngine {
    void simulate(Match match);
}

//the methods in here may wrong
interface StandingCalculator {
    int getPoints();
    int getGoalDiff();
    boolean coinToss();
    void calculateStandings(List<Team> teams, List<Match> matches);
}