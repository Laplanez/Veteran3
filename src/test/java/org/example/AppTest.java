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


}