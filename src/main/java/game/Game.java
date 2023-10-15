package game;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import map.Map;
import map.MapCell;
import map.MapCellComparator;

import static map.Map.NeighborLocation.DOWN;
import static map.Map.NeighborLocation.LEFT;
import static map.Map.NeighborLocation.RIGHT;
import static map.Map.NeighborLocation.UP;

public class Game {
    private HashMap<Integer, Player> idPlayerMap;
    private Map map;
    private LandDensity landRatio;
    private LocalDate startDate, endDate;
    private Integer jackpot, gameStage;

    public Game(LandDensity landRatio, int startingJackpot) {
        this.idPlayerMap = new HashMap<>();
        this.startDate = LocalDate.now();
        this.endDate = startDate.plusWeeks(1);
        this.jackpot = startingJackpot;
        this.landRatio = landRatio;
        this.gameStage = 0;
    }

    /**
     * Moves the game into an active state, where players may begin claiming cells.
     * Players are spawned on the map, and given a starting expansion fund of 8 units.
     * Players are unable to attack other players until the game progresses to stage 2.
     */
    public void startGame() {
        this.map = new Map(idPlayerMap.size(), landRatio.getLandRatio());
        spawnPlayers();
        advanceGameStage();
    }

    public GameReport endGame() {
        GameReport report = new GameReport(this);

        return report;
    }

    /**
     * Spawns the players currently registered with the game on the map.
     * Each player is spawned on a designated spawn point and given a
     * starting expansion fund of 8 units.
     */
    private void spawnPlayers() {
        MapCell cell;
        int i = 0;
        for (Player player : idPlayerMap.values()) {
            cell = map.getSpawnPoints().get(i);
            player.addToTerritory(cell);
            player.addToWestBorder(cell);
            player.addToNorthBorder(cell);
            player.addToEastBorder(cell);
            player.addToSouthBorder(cell);
            cell.setOwnership(player);
            player.addToExpansionFund(8);
            i++;
        }
    }

    /**
     * Adds a new player to the game.
     * @param newPlayer The player to be added
     * @return the new player within the game. Null if no more players can be added.
     */
    public Player addPlayer(Player newPlayer) {
        if (idPlayerMap.keySet().size() < 26) {
            //game.Player ids are unique per game, and range from 97-122 [inclusive-inclusive] which are the ascii decimal codes for lower case letters.
            //ids are assigned first based on the first letter of the game.Player's username.  If a player has a duplicate id, then the new player
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
        return newPlayer;
    }

    public void adjustPlayerFunds(Player player, int fundsToAdd) {
        idPlayerMap.get(player.getId()).addToExpansionFund(fundsToAdd);
        jackpot += fundsToAdd;
    }

    public ExpansionReport expand(Player player, int timesToExpand) {
        ExpansionReport report = new ExpansionReport(player);
        MapCell result;
        if (player.canExpand(timesToExpand)) {
            for (int i = 0; i < timesToExpand; i++) {
                result = executeExpansion(player);

                if (result != null && result.getOwnerId().equals(player.getId())) {
                    if (result.getHp() == MapCell.HP.STRONG) {
                        report.territoryImproved.add(result);
                    } else {
                        report.territoryGained.add(result);
                    }
                    report.expansionsAttempted++;
                    report.successfulExpansions++;
                } else if (result != null && !result.getOwnerId().equals(player.getId())) {
                    report.territoryWeakened.add(result);
                    report.expansionsAttempted++;
                    report.successfulExpansions++;
                } else if (result == null) {
                    report.expansionsAttempted++;
                }
            }
        } else {
            //The player tried to expand more times than their budget allows, so expand as many times as their
            //funds allow.
            for (int i = 0; i < player.getExpansionFund(); i++) {

                result = executeExpansion(player);
                if (result != null && result.getOwnerId().equals(player.getId())) {
                    if (result.getHp() == MapCell.HP.STRONG) {
                        report.territoryImproved.add(result);
                    } else {
                        report.territoryGained.add(result);
                    }
                    report.expansionsAttempted++;
                    report.successfulExpansions++;
                } else if (result != null && !result.getOwnerId().equals(player.getId())){
                    report.territoryWeakened.add(result);
                } else if (result == null) {
                    report.expansionsAttempted++;
                }
            }
        }
        return report;
    }

    /**
     * Attempts to find and expand onto a valid target for a game.Player's territory.  A target is searched
     * for based on the following steps: <br>
     * -If there is unclaimed land adjacent to the player's territory, then claim a random cell of adjacent land <br>
     * -If there is no adjacent land, then attack a random adjacent enemy's cell <br>
     * -If there is no adjacent enemy or the player isn't at war, then choose a random WEAK cell in the player's
     *      territory to improve to STRONG <br>
     * If no valid expansion target is found to fulfill any of the above steps, then this method will return null.
     * This method also takes into account the game.Game's current gameStage.  During gameStage 1, players may only claim
     * unclaimed land or improve their existing land.  During gameStage 2, players may also attack enemies.
     * @param player The game.Player who is attempting to expand their territory
     * @return The target map.MapCell for expansion, or null if none exists
     */
    private MapCell executeExpansion (Player player) {
        MapCell expansionTarget = null;
        Player playerToReevaluate;

        //find adjacent empty land
        if (gameStage == 1 || gameStage == 2) {
            expansionTarget = map.findExpansionTarget(player, MapCellComparator.Type.UNCLAIMED_LAND);
        }
        //if none, attack adjacent enemy land
        if (expansionTarget == null && (gameStage == 2 && player.getEnemyMap().size() > 0)) {
            expansionTarget = map.findExpansionTarget(player, MapCellComparator.Type.ENEMY_LAND);
        }
        //if not at war or no adjacent enemies, look to improve WEAK land
        if (expansionTarget == null && (gameStage == 1 || gameStage == 2)) {
            expansionTarget = map.findImprovementTarget(player);
        }

        if (expansionTarget != null) {
            playerToReevaluate = expansionTarget.attack(player);
            if (playerToReevaluate != null) {
                evaluateBorders(playerToReevaluate, expansionTarget);
            }
            player.chargeForExpansion();
            return expansionTarget;
        }

        //if none of above possible, return null
        return expansionTarget;
    }

    /**
     * Reevaluates a game.Player's borders, adding a new cell to any respective border lists for future
     * use.  Neighbors of the passed cell will also be removed from any applicable border lists.
     * @param player The game.Player whose borders should be reevaluated
     * @param cell The new cell that the game.Player owns.
     */
    private void evaluateBorders(Player player, MapCell cell) {
        MapCell[] neighbors  = map.getNeighbors(cell);
        if (player.getTerritory().contains(cell)) {
            //The player gained the given cell, and it should be added to any applicable borders lists
            if (neighbors[LEFT.locCode] != null && !neighbors[LEFT.locCode].getOwnerId().equals(MapCell.SEA)
                && !neighbors[LEFT.locCode].getOwnerId().equals(player.getId())) {
                    player.addToWestBorder(cell);
            }
            if (neighbors[UP.locCode] != null && !neighbors[UP.locCode].getOwnerId().equals(MapCell.SEA)
                && !neighbors[UP.locCode].getOwnerId().equals(player.getId())) {
                    player.addToNorthBorder(cell);
            }
            if (neighbors[RIGHT.locCode] != null && !neighbors[RIGHT.locCode].getOwnerId().equals(MapCell.SEA)
                && !neighbors[RIGHT.locCode].getOwnerId().equals(player.getId())) {
                    player.addToEastBorder(cell);
            }
            if (neighbors[DOWN.locCode] != null && !neighbors[DOWN.locCode].getOwnerId().equals(MapCell.SEA)
                && !neighbors[DOWN.locCode].getOwnerId().equals(player.getId())) {
                    player.addToSouthBorder(cell);
            }
            //remove neighbors of the cell from border lists if applicable
            if (neighbors[LEFT.locCode] != null && neighbors[LEFT.locCode].getOwnerId().equals(player.getId())) {
                player.removeFromEastBorder(neighbors[LEFT.locCode]);
            }
            if (neighbors[UP.locCode] != null && neighbors[UP.locCode].getOwnerId().equals(player.getId())) {
                player.removeFromSouthBorder(neighbors[UP.locCode]);
            }
            if (neighbors[RIGHT.locCode] != null && neighbors[RIGHT.locCode].getOwnerId().equals(player.getId())) {
                player.removeFromWestBorder(neighbors[RIGHT.locCode]);
            }
            if (neighbors[DOWN.locCode] != null && neighbors[DOWN.locCode].getOwnerId().equals(player.getId())) {
                player.removeFromNorthBorder(neighbors[DOWN.locCode]);
            }
        } else {
            //The player lost the given cell, so it should be removed from border lists
            player.removeFromWestBorder(cell);
            player.removeFromNorthBorder(cell);
            player.removeFromEastBorder(cell);
            player.removeFromSouthBorder(cell);
            //The neighbors of the lost cell should be reevaluated, because they may be new borders
            if (neighbors[LEFT.locCode] != null && neighbors[LEFT.locCode].getOwnerId().equals(player.getId())) {
                player.addToEastBorder(neighbors[LEFT.locCode]);
            }
            if (neighbors[UP.locCode] != null && neighbors[UP.locCode].getOwnerId().equals(player.getId())) {
                player.addToSouthBorder(neighbors[UP.locCode]);
            }
            if (neighbors[RIGHT.locCode] != null && neighbors[RIGHT.locCode].getOwnerId().equals(player.getId())) {
                player.addToWestBorder(neighbors[RIGHT.locCode]);
            }
            if (neighbors[DOWN.locCode] != null && neighbors[DOWN.locCode].getOwnerId().equals(player.getId())) {
                player.addToNorthBorder(neighbors[DOWN.locCode]);
            }
        }

    }

    public void declareWar(Player initiator, Player target) {
        if (!initiator.getId().equals(target.getId())) {
            initiator.addEnemy(target);
            target.addEnemy(initiator);
        }
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

    public void advanceGameStage() {
        if (gameStage < 3) {
            gameStage++;
        }
    }

    public Player getPlayer(int id) {
        return idPlayerMap.get(id);
    }

    public Integer getJackpot () {
        return jackpot;
    }

    public String getMapString() {
        return map.toString();
    }

    public LocalDate getStartDate () {
        return startDate;
    }

    public LocalDate getEndDate () {
        return endDate;
    }

    public Integer getGameStage () {
        return gameStage;
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

        public int[] getPayouts() {
            return payouts;
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
