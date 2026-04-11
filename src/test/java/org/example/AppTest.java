package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class AppTest {

    private Team teamA;
    private Team teamB;
    private MatchEngine engine;

    @BeforeEach
    void setup() {
        teamA = new Team("Team A");
        teamB = new Team("Team B");
        engine = new FootballEngine();

        // Add minimal players for power calculation
        teamA.addPlayer(new FootballPlayer("P1", 1));
        teamB.addPlayer(new FootballPlayer("P2", 2));
    }


    @Test
    void testPlayerNameAndId() {
        Player p = new FootballPlayer("Efe", 101);
        assertEquals("Efe", p.getName());
        assertEquals(101, p.getId());
    }

    @Test
    void testAttributeManagement() {
        Player p = new FootballPlayer("Berk", 1);
        p.setAttribute("Speed", 90);
        assertEquals(90, p.getAttribute("Speed"));
    }

    @Test
    void testDefaultAttributeValue() {
        Player p = new FootballPlayer("Umut", 1);
        assertEquals(0, p.getAttribute("NonExistent"));
    }

    @Test
    void testCoachTraining() {
        Player p = new FootballPlayer("Ege", 1);
        Coach c = new Coach("Trainer", 99);
        int initial = p.getAttribute("Shooting");
        c.train(p, "Shooting");
        assertEquals(initial + 1, p.getAttribute("Shooting"));
    }

    @Test
    void testAllAttributesMap() {
        Player p = new FootballPlayer("Ahmet", 1);
        assertNotNull(p.getAllAttributes());
        assertTrue(p.getAllAttributes().containsKey("Shooting"));
    }


    @Test
    void testAddPlayerToTeam() {
        teamA.addPlayer(new FootballPlayer("New", 5));
        assertEquals(2, teamA.getPlayers().size());
    }

    @Test
    void testTeamTacticAssignment() {
        Tactic t = new Tactic("4-4-2");
        teamA.setTactic(t);
        assertEquals("4-4-2", teamA.getTactic().getName());
    }

    @Test
    void testTeamCoachAssignment() {
        Coach c = new Coach("Mourinho", 7);
        teamA.setCoach(c);
        assertEquals("Mourinho", teamA.getCoach().getName());
    }

    @Test
    void testInitialPointsAreZero() {
        assertEquals(0, teamA.getPoints());
    }

    @Test
    void testGoalDifferenceCalculation() {
        teamA.recordMatch(3, 1);
        assertEquals(2, teamA.getGoalDifference());
    }


    @Test
    void testMatchSimulationFinishes() {
        Match m = new FootballMatch(teamA, teamB, engine);
        assertFalse(m.isFinished());
        m.play();
        assertTrue(m.isFinished());
    }

    @Test
    void testMatchScoreNotNegative() {
        Match m = new FootballMatch(teamA, teamB, engine);
        m.play();
        assertTrue(m.getScoreA() >= 0);
        assertTrue(m.getScoreB() >= 0);
    }

    @Test
    void testFootballMatchGoalIncrement() {
        FootballMatch fm = new FootballMatch(teamA, teamB, engine);
        fm.addGoalA();
        fm.addGoalB();
        fm.addGoalA();
        assertEquals(2, fm.getScoreA());
        assertEquals(1, fm.getScoreB());
    }

    @Test
    void testResultStringFormat() {
        Match m = new FootballMatch(teamA, teamB, engine);
        String res = m.getResult();
        assertTrue(res.contains("Team A") && res.contains("-"));
    }

    @Test
    void testMatchEngineTypeSafety() {
        Match m = new Match(teamA, teamB) {
            @Override public void play() {}
        };
        // Should not crash even if wrong match type is passed
        engine.simulate(m);
        assertEquals(0, m.getScoreA());
    }


    @Test
    void testFixtureGeneratorCount() {
        FixtureGenerator fg = new FixtureGenerator();
        List<Team> teams = Arrays.asList(teamA, teamB, new Team("C"), new Team("D"));
        List<Match> fixtures = fg.generate(teams, engine);
        // Round robin for 4 teams: (n-1)*n/2 = 3*4/2 = 6
        assertEquals(6, fixtures.size());
    }

    @Test
    void testFixtureGeneratorOddTeams() {
        FixtureGenerator fg = new FixtureGenerator();
        List<Team> teams = Arrays.asList(teamA, teamB, new Team("C"));
        List<Match> fixtures = fg.generate(teams, engine);
        assertEquals(0, fixtures.size()); // Current impl returns 0 for odd
    }

    @Test
    void testLeaguePointsUpdateWin() {
        League l = new League();
        l.addTeam(teamA);
        l.addTeam(teamB);
        // Create custom match where A wins
        Match m = new FootballMatch(teamA, teamB, engine) {
            @Override public void play() { scoreA = 2; scoreB = 0; isFinished = true; }
        };
        l.setSchedule(Arrays.asList(m));
        l.runSeason();
        assertEquals(3, teamA.getPoints());
        assertEquals(0, teamB.getPoints());
    }

    @Test
    void testLeaguePointsUpdateDraw() {
        League l = new League();
        l.addTeam(teamA);
        l.addTeam(teamB);
        Match m = new FootballMatch(teamA, teamB, engine) {
            @Override public void play() { scoreA = 1; scoreB = 1; isFinished = true; }
        };
        l.setSchedule(Arrays.asList(m));
        l.runSeason();
        assertEquals(1, teamA.getPoints());
        assertEquals(1, teamB.getPoints());
    }

    @Test
    void testLeagueStatePersistenceStub() {
        // Simple test to ensure league object holds matches
        League l = new League();
        List<Match> matches = Arrays.asList(new FootballMatch(teamA, teamB, engine));
        l.setSchedule(matches);
        assertEquals(1, l.getSchedule().size());
    }
}