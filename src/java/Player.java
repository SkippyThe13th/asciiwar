import java.util.ArrayList;
import java.util.HashMap;

public class Player {
    //Player ids are unique per game, and range from 97-122 [inclusive-inclusive] which are the ascii decimal codes for lower case letters.
    private Integer id, expansionFund;
    private char weakDisplay, strongDisplay;
    private ArrayList<MapCell> territory;
    private String username;
    private HashMap<Integer, Player> enemyMap;

    public Player(String username) {
        this.expansionFund = 0;
        this.username = username;
        this.territory = new ArrayList<>();
        this.enemyMap = new HashMap<>();
    }

    public void addEnemy(Player player) {
        enemyMap.put(player.getId(), player);
    }

    public void removeEnemy(Player player) {
        enemyMap.remove(player.getId());
    }

    public void addToTerritory(MapCell cell) {
        territory.add(cell);
    }

    public void removeFromTerritory(MapCell cell) {
        territory.remove(cell);
    }

    public void addToExpansionFund(Integer units) {
        expansionFund += units;
    }

    public void chargeForExpansion() {
        expansionFund -= 2;
    }

    public boolean canExpand() {
        return expansionFund >= 2;
    }

    public Integer getId () {
        return id;
    }

    public void setId (Integer id) {
        this.id = id;
        weakDisplay = (char) id.intValue();
        strongDisplay = (char) (id - 32);
    }

    public String getUsername () {
        return username;
    }

    public char getWeakDisplay () {
        return weakDisplay;
    }

    public char getStrongDisplay () {
        return strongDisplay;
    }

    public HashMap<Integer, Player> getEnemyMap () {
        return enemyMap;
    }

    public ArrayList<MapCell> getTerritory () {
        return territory;
    }
}
