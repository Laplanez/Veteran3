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

// --- Team & Tactic Tests (5) ---

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