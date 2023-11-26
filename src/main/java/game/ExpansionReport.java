package game;

import java.util.ArrayList;

import map.MapCell;

public class ExpansionReport {
    public Player player;
    public ArrayList<MapCell> territoryGained;
    public ArrayList<MapCell> territoryWeakened;
    public ArrayList<MapCell> territoryImproved;
    public Integer expansionsAttempted, successfulExpansions;

    public ExpansionReport(Player player) {
        this.player = player;
        this.territoryGained = new ArrayList<>();
        this.territoryWeakened = new ArrayList<>();
        this.territoryImproved = new ArrayList<>();
        this.expansionsAttempted = 0;
        this.successfulExpansions = 0;
    }

}
