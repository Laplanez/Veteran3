public class Football extends Sport {
    private int team1Goals;
    private int team2Goals;
    private String team1Name;
    private String team2Name;

    public Football(String sportName, int numberOfTeams , int numberOfPlayers, int matchDuration) {
        super("Football", 2, 11, 90);
    }


    public void goalTeam1() {
        team1Goals++;
    }

    public void goalTeam2() {
        team2Goals++;
    }

    @Override
    public void startGame() {
        System.out.println("Football game is starting!");
    }

    @Override
    public void endGame() {
        System.out.println("Football game has ended!");
    }

    @Override
    public void totalScore() {
        System.out.println(team1Name + " " + team1Goals + " - " + team2Name + " " + team2Goals);
    }
}