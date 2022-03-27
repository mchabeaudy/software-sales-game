import com.codingame.gameengine.runner.MultiplayerGameRunner;

public class Main {


    public static void main(String[] args)
    {
        MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();
        gameRunner.addAgent(Player1.class, "Bob");
        gameRunner.addAgent(Player2.class, "Alice");
        gameRunner.addAgent(Player2.class, "John");
        gameRunner.addAgent(Player2.class, "Iris");
        gameRunner.setLeagueLevel(1);
        gameRunner.start();
    }
}
