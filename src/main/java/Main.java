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

        Game game = new Game(Game.LandDensity.NORMAL, jackpot);
        for (String name : playerNames) {
            //namePlayerMap.put(name, game.addPlayer(new Player(name)));
        }
        game.startGame();
        System.out.println(game.getMapString());

        ExpansionReport report;
        for (Player player : namePlayerMap.values()) {
            report = game.expand(player, 8);
            printExpansionReport(report);
        }
        System.out.println(game.getMapString());

        game.declareWar(namePlayerMap.get("me"), namePlayerMap.get("bob"));
        game.declareWar(namePlayerMap.get("me"), namePlayerMap.get("jeff"));
        game.declareWar(namePlayerMap.get("me"), namePlayerMap.get("player1"));
        game.declareWar(namePlayerMap.get("me"), namePlayerMap.get("player3"));

        namePlayerMap.get("player3").addToExpansionFund(10);
        report = game.expand(namePlayerMap.get("player3"), 10);
        printExpansionReport(report);

        namePlayerMap.get("bob").addToExpansionFund(10);
        report = game.expand(namePlayerMap.get("bob"), 10);
        printExpansionReport(report);

        namePlayerMap.get("jeff").addToExpansionFund(10);
        report = game.expand(namePlayerMap.get("jeff"), 10);
        printExpansionReport(report);

        namePlayerMap.get("player1").addToExpansionFund(10);
        report = game.expand(namePlayerMap.get("player1"), 10);
        printExpansionReport(report);

        game.advanceGameStage();

        namePlayerMap.get("player3").addToExpansionFund(30);
        report = game.expand(namePlayerMap.get("player3"), 30);
        printExpansionReport(report);

        namePlayerMap.get("bob").addToExpansionFund(30);
        game.declareWar(namePlayerMap.get("bob"), namePlayerMap.get("jeff"));
        report = game.expand(namePlayerMap.get("bob"), 30);
        printExpansionReport(report);

        namePlayerMap.get("jeff").addToExpansionFund(30);
        report = game.expand(namePlayerMap.get("jeff"), 30);
        printExpansionReport(report);

        namePlayerMap.get("player1").addToExpansionFund(30);
        report = game.expand(namePlayerMap.get("player1"), 30);
        printExpansionReport(report);

        System.out.println(game.getMapString());

        namePlayerMap.get("me").addToExpansionFund(30);
        report = game.expand(namePlayerMap.get("me"), 30);
        printExpansionReport(report);

        System.out.println(game.getMapString());
    }

    public static void printExpansionReport (ExpansionReport report) {
        StringBuilder sb = new StringBuilder();
        Player player = report.player;
        sb.append(player.getUsername()).append(" (").append((char)player.getId().intValue()).append(")");
        sb.append(" attempted to expand ").append(report.expansionsAttempted).append(" time(s).").append(System.lineSeparator());
        sb.append("They succeeded ").append(report.successfulExpansions).append(" time(s).").append(System.lineSeparator());
        sb.append("They claimed ").append(report.territoryGained.size()).append(" new cells.").append(System.lineSeparator());
        sb.append("They improved ").append(report.territoryImproved.size()).append(" of their cells.").append(System.lineSeparator());
        sb.append("They attacked ").append(report.territoryWeakened.size()).append(" enemy cells.").append(System.lineSeparator());
        sb.append("They now control ").append(player.getTerritory().size()).append(" cells.").append(System.lineSeparator());
        System.out.println(sb);
    }
}