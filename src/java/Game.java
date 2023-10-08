import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Game {
    private HashMap<Integer, Player> idPlayerMap;
    private Map map;
    private LandDensity landRatio;
    private LocalDate startDate, endDate;
    private Integer jackpot;

    public Game(LandDensity landRatio, int startingJackpot) {
        this.idPlayerMap = new HashMap<>();
        this.startDate = LocalDate.now();
        this.endDate = startDate.plusWeeks(1);
        this.jackpot = startingJackpot;
        this.landRatio = landRatio;
    }

    public void startGame() {
        this.map = new Map(idPlayerMap.size(), landRatio.getLandRatio());
        spawnPlayers();
    }

    public GameReport endGame() {
        GameReport report = new GameReport(this);

        return report;
    }

    private void spawnPlayers() {
        MapCell cell;
        int i = 0;
        for (Player player : idPlayerMap.values()) {
            cell = map.getSpawnPoints().get(i);
            player.addToTerritory(cell);
            cell.setOwnership(player);
            player.addToExpansionFund(8);
            i++;
        }
    }

    /**
     * Adds a new player to the game.
     * @param newPlayer The player to be added
     * @return the id of the new player within the game. Null if no more players can be added.
     */
    public Integer addPlayer(Player newPlayer) {
        if (idPlayerMap.keySet().size() < 26) {
            //Player ids are unique per game, and range from 97-122 [inclusive-inclusive] which are the ascii decimal codes for lower case letters.
            //ids are assigned first based on the first letter of the Player's username.  If a player has a duplicate id, then the new player
            //is assigned the next letter of the alphabet.
            char id = newPlayer.getUsername().charAt(0);
            if (idPlayerMap.get((int)id) == null) {
                newPlayer.setId((int)id);
                idPlayerMap.put((int)id, newPlayer);
            } else {
                newPlayer.setId(getNextId(id));
                idPlayerMap.put(newPlayer.getId(), newPlayer);
            }
        } else {
            return null;
        }
        return newPlayer.getId();
    }

    public void adjustPlayerFunds(Player player, int fundsToAdd) {
        idPlayerMap.get(player.getId()).addToExpansionFund(fundsToAdd);
        jackpot += fundsToAdd;
    }

    public ExpansionReport expand(Player player, int expansionNum) {
        ExpansionReport report = new ExpansionReport(player);
        if (player.canExpand()) {
            //TODO: Implement this
            //call method on map to expand player's territory or improve it
            //increment jackpot by units expended to expand
        }
        return report;
    }

    public void declareWar(Player initiator, Player target) {
        idPlayerMap.get(initiator.getId()).addEnemy(target);
        idPlayerMap.get(target.getId()).addEnemy(initiator);
    }

    /**
     * Removes the target player from the initiator's enemy map.  This method only updates the initiating player's
     * enemy map.  If the target never petitions for peace, then the target player will still be able to attack the
     * player initiating a petition for peace.  To resume hostile action, the initiating player must declare war on
     * the target player
     * @param initiator the player who made a call for peace
     * @param target the player the initiator would like to make peace with.
     * @return true if mutual peace is established, false if the target player is still at war with the initiating player
     */
    public boolean petitionPeace(Player initiator, Player target) {
        idPlayerMap.get(initiator.getId()).removeEnemy(target);
        //The initiator is not an enemy to the target, so mutual peace has been established
        return target.getEnemyMap().get(initiator.getId()) == null;
    }

    /**
     * Returns a list of Players in descending order based on the amount of cells within their territory
     * @return an ArrayList of Players, where the first 3 elements represent the 1st, 2nd, and 3rd place Players
     */
    public ArrayList<Player> getScores () {
        ArrayList<Player> winners;

        winners = new ArrayList<>(idPlayerMap.values().stream().sorted(Comparator.comparingInt(player -> player.getTerritory().size())).toList());
        winners.sort(Collections.reverseOrder());

        return winners;
    }

    private int getNextId(int initialId) {
        int nextId = 0;
        for (int i = initialId + 1; 97 <= i && i <= 122; i++) {
            if (idPlayerMap.get(i) == null) {
                nextId = i;
                break;
            }
            if (i == 122) {
                i = 96;
            }
        }
        return nextId;
    }

    public Player getPlayer(int id) {
        return idPlayerMap.get(id);
    }

    private Integer getJackpot () {
        return jackpot;
    }

    private Map getMap () {
        return map;
    }

    private class GameReport {
        ArrayList<Player> winners;
        int[] payouts;

        private GameReport (Game game) {
            winners = game.getScores();
            payouts = new int[3];
            payouts[0] = game.getJackpot() / 2;
            payouts[1] = game.getJackpot() / 3;
            payouts[2] = game.getJackpot() / 5;
        }

        public ArrayList<Player> getWinners () {
            return winners;
        }
    }

    public static enum LandDensity {
        NORMAL(.55),
        SPARSE(.40),
        DENSE(.80);

        private final double ratio;
        private LandDensity(double ratio) {
            this.ratio = ratio;
        }

        public double getLandRatio() {
            return this.ratio;
        }
    }
}
