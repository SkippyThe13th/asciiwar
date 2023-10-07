import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class Game {
    HashMap<Integer, Player> idPlayerMap;
    Map map;
    LocalDate startDate, endDate;

    public Game(Map map) {
        this.idPlayerMap = new HashMap<>();
        this.map = map;
        this.startDate = LocalDate.now();
        this.endDate = startDate.plusWeeks(1);
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
            MapCell playerSpawn = map.spawnPlayer(map.getMap(), newPlayer);
            if (playerSpawn != null) {
                newPlayer.addToTerritory(playerSpawn);
            }
        } else {
            return null;
        }
        return newPlayer.getId();
    }

    public ExpansionReport expand(Player player, int expansionNum) {
        ExpansionReport report = new ExpansionReport(player);
        if (player.canExpand()) {

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
        if (target.getEnemyMap().get(initiator.getId()) == null) {
            //The initiator is not an enemy to the target, so mutual peace has been established
            return true;
        } else {
            return false;
        }
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

    public Player getPlayer(Integer id) {
        return idPlayerMap.get(id);
    }
}
