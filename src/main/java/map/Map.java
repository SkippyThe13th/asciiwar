package map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import game.Player;

public class Map {
    private MapCell[][] map;
    private final ArrayList<MapCell> westShores, northShores, eastShores, southShores, unclaimedLand, spawnPoints;
    private Integer width, height;

    public Map(Integer numOfPlayers, double landRatio) {
        westShores = new ArrayList<>();
        northShores = new ArrayList<>();
        eastShores = new ArrayList<>();
        southShores = new ArrayList<>();
        unclaimedLand = new ArrayList<>();
        spawnPoints = new ArrayList<>();
        if (numOfPlayers < 4) {
            numOfPlayers = 4;
        }
        generateSpawnPointMap(numOfPlayers);
        //Add additional land to flesh out map
        generateLand(map, landRatio);
    }

    /**
     * Creates a map with a height:width ratio of 1:3 with enough spawn points for the indicated number of
     * players.  game.Player spawns will be connected by land in such a way that any spawn
     * location can "access" any other be enough moves across land cells
     * @param numOfPlayers the number of players that the map should have spawn points for
     */
    private void generateSpawnPointMap (Integer numOfPlayers) {
        HashMap<Integer, MapPoint> idPointMap = new HashMap<>();
        ArrayList<MapCell> cornerSpawns = new ArrayList<>();
        int xOffset, yOffset;
        boolean mapSizeIsValid = false;
        Random rand = new Random();
        MapPoint farLeft = null,
            farRight = null,
            farUp = null,
            farDown = null;

        //Create spawn points to define map
        while (!mapSizeIsValid){
            generateSpawnPoints(numOfPlayers, idPointMap);
            assert !idPointMap.isEmpty();
            //Determine the extreme x and y values that where generated
            for (MapPoint point : idPointMap.values()) {
                if (farLeft == null || point.x < farLeft.x) {
                    farLeft = point;
                }
                if (farRight == null || point.x > farRight.x) {
                    farRight = point;
                }
                if (farUp == null || point.y > farUp.y) {
                    farUp = point;
                }
                if (farDown == null ||point.y < farDown.y) {
                    farDown = point;
                }
            }

            width = Math.abs(farRight.x - farLeft.x) + 6;
            height = Math.abs(farUp.y - farDown.y) + 5;
            if (height > numOfPlayers / 4 && 4 >= width / height && width / height >= 2) {
                mapSizeIsValid = true;
            } else {
                idPointMap = new HashMap<>();
                farLeft = null;
                farRight = null;
                farUp = null;
                farDown = null;
            }
        }

        map = new MapCell[width][height];
        fillSea(map);
        xOffset = (farLeft.x * -1) + 2;
        yOffset = (farDown.y * -1) + 2;
        for (MapPoint point : idPointMap.values()) {
            point.x += xOffset;
            point.y += yOffset;
            map[point.x][point.y].makeUnclaimedLand();
            addToShoresLists(map, map[point.x][point.y]);
            spawnPoints.add(map[point.x][point.y]);
        }

        cornerSpawns.add(map[farLeft.x][farLeft.y]);
        cornerSpawns.add(map[farRight.x][farRight.y]);
        cornerSpawns.add(map[farUp.x][farUp.y]);
        cornerSpawns.add(map[farDown.x][farDown.y]);

        //Connect all the spawn points with land cells
        connectSpawnsWithLand(map[farLeft.x][farLeft.y], map[farRight.x][farRight.y]);
        connectSpawnsWithLand(map[farUp.x][farUp.y], map[farDown.x][farDown.y]);
        for (MapPoint point : idPointMap.values()) {
            if (!cornerSpawns.contains(map[point.x][point.y])) {
                connectSpawnsWithLand(map[point.x][point.y], cornerSpawns.get(rand.nextInt(4)));
            }
        }

        //Make all neighboring cells land to ensure at least 8 neighboring land cells
        for (MapPoint point : idPointMap.values()) {
            for (NeighborLocation nloc : NeighborLocation.values()) {
                if (!map[point.x + nloc.x][point.y + nloc.y].isLand()) {
                    map[point.x + nloc.x][point.y + nloc.y] = MapCell.createLandCell(point.x + nloc.x, point.y + nloc.y);
                    addToShoresLists(map, map[point.x + nloc.x][point.y + nloc.y]);
                }
            }
        }
    }

    /**
     * Populates the provided HashMap with a number of spawn points.  Spawns will have at least 2
     * cells of space between them and any other spawn. game.Player spawns will not be an equal
     * distance from every other player spawn.
     * @param numOfPoints the number of points to be generated and added to the map
     * @param pointMap the map that the points should be added to
     */
    private void generateSpawnPoints (int numOfPoints, HashMap<Integer, MapPoint> pointMap) {
        int tempX, tempY, relX, relY, nextId, nextRel;
        boolean neg, valid;
        Random rand = new Random();

        nextId = 0;
        //insert starting point
        pointMap.put(nextId++, new MapPoint(0, 0));
        relX = 0;
        relY = 0;

        while (pointMap.keySet().size() < numOfPoints) {
            //Generate a tentative x and y value for a new point
            tempX = rand.nextInt(10);
            if (tempX < 2) {
                tempY = rand.nextInt(4) + 2;
            } else {
                tempY = rand.nextInt(4);
            }

            //Determine if the new x and y will be less than or larger than
            // our selected relative point
            neg = rand.nextInt(2) == 0;
            if (neg) {
                tempX -= relX;
            } else {
                tempX += relX;
            }
            neg = rand.nextInt(2) == 0;
            if (neg) {
                tempY -= relY;
            } else {
                tempY += relY;
            }

            valid = true;
            //if the new point is within 2 cells of another spawn point, it isn't valid and can't be added.
            for (MapPoint point : pointMap.values()) {
                if (Math.abs(tempX - point.x) <= 3 && Math.abs(tempY - point.y) <= 3){
                    valid = false;
                    break;
                }
            }
            if (valid) {
                pointMap.put(nextId++, new MapPoint(tempX, tempY));
            }
            nextRel = rand.nextInt(pointMap.size());
            relX = pointMap.get(nextRel).x;
            relY = pointMap.get(nextRel).y;
        }
    }

    private void connectSpawnsWithLand (MapCell spawn1, MapCell spawn2) {
        int currentX = spawn1.getxLoc(),
            currentY = spawn1.getyLoc();
        while (currentX != spawn2.getxLoc() || currentY != spawn2.getyLoc()) {
            if (currentX < spawn2.getxLoc()) {
                currentX++;
                map[currentX][currentY].makeUnclaimedLand();
                addToShoresLists(map, map[currentX][currentY]);
            }
            if (currentY < spawn2.getyLoc()) {
                currentY++;
                map[currentX][currentY].makeUnclaimedLand();
                addToShoresLists(map, map[currentX][currentY]);
            }
            if (currentX > spawn2.getxLoc()) {
                currentX--;
                map[currentX][currentY].makeUnclaimedLand();
                addToShoresLists(map, map[currentX][currentY]);
            }
            if (currentY > spawn2.getyLoc()) {
                currentY--;
                map[currentX][currentY].makeUnclaimedLand();
                addToShoresLists(map, map[currentX][currentY]);
            }
        }
    }

    /**
     * Adds more land to the given map until the land to sea ratio matches the given ratio.  Land is added one cell
     * at a time along either a northern, eastern, southern, or western shore selected at random.
     * @param map the map to fill with land
     * @param landRatio the ratio of land to sea that the map should match.  Value must be between 0 and 1
     */
    private void generateLand(MapCell[][] map, double landRatio) {
        double landCellMax = Math.floor((height * width) * landRatio);
        int dirCode, landCellCount = 0;
        Random rand = new Random();

        for (MapCell[] cellRow : map) {
            for(MapCell cell : cellRow) {
                if (cell.isLand()) {
                    landCellCount++;
                }
            }
        }

        MapCell newCell;
        int totalTries = 0;
        while (landCellCount < landCellMax) {
            dirCode = rand.nextInt(4);
            if (dirCode == 0) {
                newCell = addRandomLandToShore(Direction.WEST);
            } else if (dirCode == 1) {
                newCell = addRandomLandToShore(Direction.NORTH);
            } else if (dirCode == 2) {
                newCell = addRandomLandToShore(Direction.EAST);
            } else {
                newCell = addRandomLandToShore(Direction.SOUTH);
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
            landCellCount++;
        }
    }

    /**
     * Fills all empty cells in a map with Sea Cells
     * @param map the map to fill with sea cells
     */
    private void fillSea(MapCell[][] map) {
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
     * determined by the passed shore.
     * @param shore determines what kind of shore the new cell will be placed along.
     * @return the newly placed cell, or null if no new cell could be placed.
     */
    private MapCell addRandomLandToShore (Direction shore) {
        MapCell originShore;
        int x,y;
        Random rand = new Random();

        switch (shore) {
            case WEST:
                if(westShores.size() > 0) {
                    originShore = westShores.get(rand.nextInt(westShores.size()));
                    x = originShore.getxLoc() - 1;
                    y = originShore.getyLoc();
                    map[x][y] = MapCell.createLandCell(x, y);
                    //with new cell created, need to manage lists of shores
                    addToShoresLists(map, map[x][y]);
                    return map[x][y];
                } else {
                    return null;
                }
            case NORTH:
                if (northShores.size() > 0) {
                    originShore = northShores.get(rand.nextInt(northShores.size()));
                    x = originShore.getxLoc();
                    y = originShore.getyLoc() - 1;
                    map[x][y] = MapCell.createLandCell(x, y);
                    addToShoresLists(map, map[x][y]);
                    return map[x][y];
                } else {
                    return null;
                }
            case EAST:
                if (eastShores.size() > 0) {
                    originShore = eastShores.get(rand.nextInt(eastShores.size()));
                    x = originShore.getxLoc() + 1;
                    y = originShore.getyLoc();
                    map[x][y] = MapCell.createLandCell(x, y);
                    addToShoresLists(map, map[x][y]);
                    return map[x][y];
                } else {
                    return null;
                }
            case SOUTH:
                if (southShores.size() > 0) {
                    originShore = southShores.get(rand.nextInt(southShores.size()));
                    x = originShore.getxLoc();
                    y = originShore.getyLoc() + 1;
                    map[x][y] = MapCell.createLandCell(x, y);
                    addToShoresLists(map, map[x][y]);
                    return map[x][y];
                } else {
                    return null;
                }
            default:
                return null;
        }
    }

    /**
     * Adds the given cell to the corresponding shore list based on whether it has neighboring land cells
     * and removes the neighbors of the cell from the corresponding shores list
     * @param cell the cell to be added
     */
    private void addToShoresLists(MapCell[][]map, MapCell cell) {
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
     * Attempts to find a cell adjacent to a game.Player's territory that is a valid expansion target.
     * Uses a {@link MapCellComparator} to determine the validity of the expansion target
     * @param player The player whose borders should be searched
     * @param comparatorType The type of expansion target to be searched for
     * @return A map.MapCell that is a valid expansion target, null if none are found
     */
    public MapCell findExpansionTarget (Player player, MapCellComparator.Type comparatorType) {
        MapCellComparator comparator = new MapCellComparator(comparatorType, player);
        MapCell possibleExpansion;
        Map.NeighborLocation expansionDirection;
        ArrayList<MapCell> borders;
        int x, y, startDir, bordersChecked;
        Random rand = new Random();

        //Determine border direction to begin search along
        startDir = rand.nextInt(4);
        if (startDir == 0) {
            expansionDirection = NeighborLocation.LEFT;
        } else if (startDir == 1) {
            expansionDirection = NeighborLocation.UP;
        } else if (startDir == 2) {
            expansionDirection = NeighborLocation.RIGHT;
        } else {
            expansionDirection = NeighborLocation.DOWN;
        }
        //Loop through each border list, return if valid expansion target is found
        bordersChecked = 0;
        while (bordersChecked < 4) {
            switch (expansionDirection) {
                case LEFT:
                    borders = player.getWestBorders();
                    break;
                case UP:
                    borders = player.getNorthBorders();
                    break;
                case RIGHT:
                    borders = player.getEastBorders();
                    break;
                case DOWN:
                    borders = player.getSouthBorders();
                    break;
                default:
                    return null;
            }
            if (borders.size() > 0){
                for (MapCell borderCell : borders) {
                    x = borderCell.getxLoc() + expansionDirection.x;
                    y = borderCell.getyLoc() + expansionDirection.y;
                    possibleExpansion = map[x][y];
                    if (comparator.matches(possibleExpansion)){
                        return possibleExpansion;
                    }
                }
            }
            //Since we didn't return, rotate to the next list of border cells
            switch (expansionDirection) {
                case LEFT:
                    expansionDirection = NeighborLocation.UP;
                    break;
                case UP:
                    expansionDirection = NeighborLocation.RIGHT;
                    break;
                case RIGHT:
                    expansionDirection = NeighborLocation.DOWN;
                    break;
                case DOWN:
                    expansionDirection = NeighborLocation.LEFT;
                    break;
            }
            bordersChecked++;
        }
        //No valid expansion target was found
        return null;
    }

    /**
     * Overwrites the existing map cell with newCell if newCell's coordinates exist within
     * the map.
     * @param newCell the cell value to be inserted in the map, overwriting the existing cell
     * @return the newly updated cell, or null if the coordinates of the new cell were invalid
     */
    public MapCell updateCell(MapCell newCell) {
        if (getCell(newCell.getxLoc(), newCell.getyLoc()) != null) {
            map[newCell.getxLoc()][newCell.getyLoc()] = newCell;
            return map[newCell.getxLoc()][newCell.getyLoc()];
        } else {
            return null;
        }
    }

    public MapCell findImprovementTarget (Player player) {
        for (MapCell cell : player.getTerritory()) {
            if (cell.getHp() == MapCell.HP.WEAK) {
                return cell;
            }
        }
        return null;
    }

    /**
     * Returns the cell found at coordinates (x, y) if it exists.
     * @return the map.MapCell at the given coordinates, null if the coordinates are invalid
     */
    public MapCell getCell(Integer x, Integer y) {
        if ((0 < x && x < width) && (0 < y && y < height)) {
            return map[x][y];
        } else {
            return null;
        }
    }

    public MapCell[] getNeighbors(MapCell cell) {
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

    @Override
    public String toString () {
        StringBuilder mapString = new StringBuilder();

        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                mapString.append((char) map[x][y].getDisplayCharId());
            }
            mapString.append(System.lineSeparator());
        }
        return mapString.toString();
    }

    public ArrayList<MapCell> getSpawnPoints () {
        return spawnPoints;
    }

    public Integer getWidth () {
        return width;
    }

    public Integer getHeight () {
        return height;
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

    private class MapPoint {
        int x,y;

        public MapPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
