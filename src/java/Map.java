import java.util.ArrayList;
import java.util.Random;

public class Map {
    private final MapCell[][] map;
    private final ArrayList<MapCell> westShores, northShores, eastShores, southShores, unclaimedLand;
    private final Integer width, height;
    private final Random rand = new Random();

    public Map(Integer height, Integer width, double landRatio) {
        this.height = height;
        this.width = width;
        map = new MapCell[width][height];
        westShores = new ArrayList<>();
        northShores = new ArrayList<>();
        eastShores = new ArrayList<>();
        southShores = new ArrayList<>();
        unclaimedLand = new ArrayList<>();
        fillSea();
        generateLand(landRatio);
    }

    public Map(Integer numOfPlayers, double landRatio) {
        this.map = generateSpawnPoints(numOfPlayers);
    }

    /**
     * Creates a map with a height:width ratio of 1:3 with enough spawn points for the indicated number of
     * players.  Player spawns will have at least 2 cells of space between them and any other spawn, but will
     * not be any further than 5 cells away from another player.  Player spawns will not be an equal distance from
     * every other player spawn.  Player spawns will be connected by land in such a way that any spawn
     * location can "access" any other be enough moves across land cells
     * @param numOfPlayers the number of players that the map should have spawn points for
     * @return a MapCell[][] containing the minimum map, ready to be filled in with more land
     */
    private MapCell[][] generateSpawnPoints(Integer numOfPlayers) {

    }

    private void generateLand(double landRatio) {
        double landCellMax = Math.floor((height * width) * landRatio);
        Random rand = new Random();
        int dirCode;

        MapCell origin = MapCell.createOriginCell(height, width);
        map[origin.getxLoc()][origin.getyLoc()] = origin;
        westShores.add(origin);
        northShores.add(origin);
        eastShores.add(origin);
        southShores.add(origin);
        unclaimedLand.add(origin);

        MapCell newCell;
        int totalTries = 0;
        int landCellCount;
        for (landCellCount = 1; landCellCount < landCellMax; landCellCount++) {
            dirCode = rand.nextInt(4);
            if (dirCode == 0) {
                newCell = addNewLand(Direction.WEST);
            } else if (dirCode == 1) {
                newCell = addNewLand(Direction.NORTH);
            } else if (dirCode == 2) {
                newCell = addNewLand(Direction.EAST);
            } else {
                newCell = addNewLand(Direction.SOUTH);
            }
            unclaimedLand.add(newCell);
            if (newCell == null) {
                landCellCount--;
            }
            totalTries++;
            if (totalTries == 10000) {
                System.out.println("land generation aborted after maximum tries");
                break;
            }
        }
    }

    private void fillSea() {
        for (int y=0; y<height; y++){
            for (int x = 0; x < width; x++){
                if (map[x][y] == null) {
                    map[x][y] = MapCell.createOceanCell(x, y);
                }
            }
        }
    }

    /**
     * Adds a new cell of land to a random shore.  The shore is picked from one of the shore lists, the specific list
     * determined by the passed direction.
     * @param direction determines what kind of shore the new cell will be placed along.
     * @return the newly placed cell, or null if no new cell could be placed.
     */
    private MapCell addNewLand(Direction direction) {
        MapCell originShore;
        MapCell[] neighbors;
        int x,y, dirCode;

        switch (direction) {
            case WEST -> {
                if(westShores.size() > 0) {
                    originShore = westShores.get(rand.nextInt(westShores.size()));
                    x = originShore.getxLoc() - 1;
                    y = originShore.getyLoc();
                    map[x][y] = new MapCell(x, y);
                    //with new cell created, need to manage lists of shores
                    removeNeighborsFromShoreLists(map[x][y]);
                    addToShoresLists(map[x][y]);
                    return map[x][y];
                } else {
                    return null;
                }
            }
            case NORTH -> {
                if (northShores.size() > 0) {
                    originShore = northShores.get(rand.nextInt(northShores.size()));
                    x = originShore.getxLoc();
                    y = originShore.getyLoc() - 1;
                    map[x][y] = new MapCell(x, y);
                    removeNeighborsFromShoreLists(map[x][y]);
                    addToShoresLists(map[x][y]);
                    return map[x][y];
                } else {
                    return null;
                }
            }
            case EAST -> {
                if (eastShores.size() > 0) {
                    originShore = eastShores.get(rand.nextInt(eastShores.size()));
                    x = originShore.getxLoc() + 1;
                    y = originShore.getyLoc();
                    map[x][y] = new MapCell(x, y);
                    removeNeighborsFromShoreLists(map[x][y]);
                    addToShoresLists(map[x][y]);
                    return map[x][y];
                } else {
                    return null;
                }
            }
            case SOUTH -> {
                if (southShores.size() > 0) {
                    originShore = southShores.get(rand.nextInt(southShores.size()));
                    x = originShore.getxLoc();
                    y = originShore.getyLoc() + 1;
                    map[x][y] = new MapCell(x, y);
                    removeNeighborsFromShoreLists(map[x][y]);
                    addToShoresLists(map[x][y]);
                    return map[x][y];
                } else {
                    return null;
                }
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Removes the neighbors of a map cell from the corresponding shores list
     * @param cell the cell whose neighbors should be removed from their shores list
     */
    private void removeNeighborsFromShoreLists (MapCell cell) {
        MapCell[] neighbors  = getNeighbors(cell);
            if (neighbors[NeighborLocation.LEFT.locCode] != null && neighbors[NeighborLocation.LEFT.locCode].isLand()) {
                eastShores.remove(neighbors[NeighborLocation.LEFT.locCode]);
            }
            if (neighbors[NeighborLocation.UP.locCode] != null && neighbors[NeighborLocation.UP.locCode].isLand()) {
                southShores.remove(neighbors[NeighborLocation.UP.locCode]);
            }
            if (neighbors[NeighborLocation.RIGHT.locCode] != null && neighbors[NeighborLocation.RIGHT.locCode].isLand()) {
                westShores.remove(neighbors[NeighborLocation.RIGHT.locCode]);
            }
            if (neighbors[NeighborLocation.DOWN.locCode] != null && neighbors[NeighborLocation.DOWN.locCode].isLand()) {
                northShores.remove(neighbors[NeighborLocation.DOWN.locCode]);
            }
    }

    /**
     * Adds the given cell to the corresponding shore list based on whether it has neighboring land cells
     * @param cell the cell to be added
     */
    private void addToShoresLists(MapCell cell) {
        MapCell[] neighbors  = getNeighbors(cell);
        if (neighbors[NeighborLocation.LEFT.locCode] == null || !neighbors[NeighborLocation.LEFT.locCode].isLand()) {
            if (cell.getxLoc() > 0) {
                westShores.add(cell);
            }
        }
        if (neighbors[NeighborLocation.UP.locCode] == null || !neighbors[NeighborLocation.UP.locCode].isLand()) {
            if (cell.getyLoc() > 0) {
                northShores.add(cell);
            }
        }
        if (neighbors[NeighborLocation.RIGHT.locCode] == null || !neighbors[NeighborLocation.RIGHT.locCode].isLand()) {
            if (cell.getxLoc() < width - 1) {
                eastShores.add(cell);
            }
        }
        if (neighbors[NeighborLocation.DOWN.locCode] == null || !neighbors[NeighborLocation.DOWN.locCode].isLand()) {
            if (cell.getyLoc() < height - 1) {
                southShores.add(cell);
            }
        }
    }

    private MapCell[] getNeighbors(MapCell cell) {
        MapCell[] neighbors = new MapCell[8];
        int neighborX, neighborY;

        for (NeighborLocation nloc : NeighborLocation.values()) {
            neighborX = cell.getxLoc() + nloc.x;
            neighborY = cell.getyLoc() + nloc.y;
            if (0 <= neighborX && neighborX < width) {
                if (0 <= neighborY && neighborY < height) {
                    neighbors[nloc.locCode] = map[neighborX][neighborY];
                } else {
                    neighbors[nloc.locCode] = null;
                }
            } else {
                neighbors[nloc.locCode] = null;
            }
        }

        return neighbors;
    }

    /**
     * Spawns the player on the map of the game.  A player can only spawn if there is an unclaimed land cell
     * that is not a neighbor to another player's claimed territory.
     * @param player The player to spawn
     * @return the MapCell that the player was spawned on.  Null if there was no valid spawn location.
     */
    public MapCell spawnPlayer(Player player) {
        for (MapCell unclaimedCell : unclaimedLand) {
            if (isValidSpawn(unclaimedCell)) {
                unclaimedCell.bolster(player.getId());
                return unclaimedCell;
            }
        }
        return null;
    }

    private boolean isValidSpawn(MapCell cell) {
        MapCell[] neighbors = getNeighbors(cell);
        boolean isValid = true;

        for (MapCell neighbor : neighbors) {
            if (neighbor.getDisplayCharId() != MapCell.LAND || neighbor.getDisplayCharId() != MapCell.SEA) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    public String printMap() {
        StringBuilder mapString = new StringBuilder();

        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                mapString.append((char) map[x][y].getDisplayCharId());
            }
            mapString.append(System.lineSeparator());
        }
        return mapString.toString();
    }

    public enum Direction {
        WEST(0),
        NORTH(1),
        EAST(2),
        SOUTH(3);

        public final int dirCode;

        private Direction (int dirCode) {
            this.dirCode = dirCode;
        }
    }
    public enum NeighborLocation {
        LEFT_DOWN(-1, 1, 7),
        DOWN(0, 1, 6),
        RIGHT_DOWN(1, 1, 5),
        RIGHT(1, 0, 4),
        RIGHT_UP(1, -1, 3),
        UP(0, -1, 2),
        LEFT_UP(-1, -1, 1),
        LEFT(-1, 0, 0);

        public final int x;
        public final int y;
        public final int locCode;

        //locCod starts with left, then moves clockwise around main cell
        private NeighborLocation (int x, int y, int locCode) {
            this.x = x;
            this.y = y;
            this.locCode = locCode;
        }
    }
}
