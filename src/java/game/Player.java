package game;

import java.util.ArrayList;
import java.util.HashMap;

import map.MapCell;

public class Player {
    //game.Player ids are unique per game, and range from 97-122 [inclusive-inclusive] which are the ascii decimal codes for lower case letters.
    private Integer id, expansionFund;
    private char weakDisplay, strongDisplay;
    private ArrayList<MapCell> territory, westBorders, northBorders, eastBorders, southBorders;
    private String username;
    private HashMap<Integer, Player> enemyMap;

    public Player(String username) {
        this.expansionFund = 0;
        this.username = username;
        this.territory = new ArrayList<>();
        this.westBorders = new ArrayList<>();
        this.northBorders = new ArrayList<>();
        this.eastBorders = new ArrayList<>();
        this.southBorders = new ArrayList<>();
        this.enemyMap = new HashMap<>();
    }

    protected Player (String username, Integer id, char weakDisplay, char strongDisplay) {
        this.username = username;
        this.id = id;
        this.weakDisplay = weakDisplay;
        this.strongDisplay = strongDisplay;
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

    public void addToWestBorder(MapCell cell) {
        westBorders.add(cell);
    }

    public void addToNorthBorder(MapCell cell) {
        northBorders.add(cell);
    }

    public void addToEastBorder(MapCell cell) {
        eastBorders.add(cell);
    }

    public void addToSouthBorder(MapCell cell) {
        southBorders.add(cell);
    }

    public void removeFromWestBorder(MapCell cell) {
        westBorders.remove(cell);
    }

    public void removeFromNorthBorder(MapCell cell) {
        northBorders.remove(cell);
    }

    public void removeFromEastBorder(MapCell cell) {
        eastBorders.remove(cell);
    }

    public void removeFromSouthBorder(MapCell cell) {
        southBorders.remove(cell);
    }

    public void removeFromTerritory(MapCell cell) {
        territory.remove(cell);
    }

    public void addToExpansionFund(Integer units) {
        expansionFund += units;
    }

    public void chargeForExpansion() {
        expansionFund -= 1;
    }

    public boolean canExpand(Integer times) {
        return expansionFund >= times;
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

    public Integer getExpansionFund () {
        return expansionFund;
    }

    public HashMap<Integer, Player> getEnemyMap () {
        return enemyMap;
    }

    public ArrayList<MapCell> getWestBorders () {
        return westBorders;
    }

    public ArrayList<MapCell> getNorthBorders () {
        return northBorders;
    }

    public ArrayList<MapCell> getEastBorders () {
        return eastBorders;
    }

    public ArrayList<MapCell> getSouthBorders () {
        return southBorders;
    }

    public ArrayList<MapCell> getTerritory () {
        return territory;
    }
}
