import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import game.ExpansionReport;
import game.Game;
import game.Player;

public class Main {
    public static void main (String[] args) {
        int jackpot = 100;
        ArrayList<String> playerNames = new ArrayList<>(Arrays.asList("player1", "jeff", "player3", "me", "bob"));
        HashMap<String, Player> namePlayerMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        Game game = new Game(Game.LandDensity.NORMAL, jackpot);
        for (String name : playerNames) {
            namePlayerMap.put(name, game.addPlayer(new Player(name)));
        }
        game.startGame();
        System.out.println(game.getMapString());

        ExpansionReport report;
        for (Player player : namePlayerMap.values()) {
            report = game.expand(player, 8);
            sb.append(player.getUsername()).append(" (").append((char)player.getId().intValue()).append(")");
            sb.append(" attempted to expand ").append(report.expansionsAttempted).append(" time(s).").append(System.lineSeparator());
            sb.append("They succeeded ").append(report.successfulExpansions).append(" time(s).").append(System.lineSeparator());
            sb.append("They claimed ").append(report.territoryGained.size()).append(" new cells.").append(System.lineSeparator());
            sb.append("They improved ").append(report.territoryImproved.size()).append(" of their cells.").append(System.lineSeparator());
            sb.append("They attacked ").append(report.territoryWeakened.size()).append(" enemy cells.").append(System.lineSeparator());
            System.out.println(sb);
            sb = new StringBuilder();
        }
        System.out.println(game.getMapString());
    }
}