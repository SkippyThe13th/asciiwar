import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Map {
    private final MapCell[][] map;
    private final ArrayList<MapCell> westShores, northShores, eastShores, southShores, unclaimedLand, spawnPoints;
    private Integer width, height;
    private final Random rand = new Random();

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
        this.map = generateSpawnPointMap(numOfPlayers);
        //Add additional land to flesh out map
        generateLand(map, landRatio);
        System.out.println(printMap(map));
    }

    /**
     * Creates a map with a height:width ratio of 1:3 with enough spawn points for the indicated number of
     * players.  Player spawns will be connected by land in such a way that any spawn
     * location can "access" any other be enough moves across land cells
     * @param numOfPlayers the number of players that the map should have spawn points for
     * @return a MapCell[][] containing the minimum map, ready to be filled in with more land
     */
    private MapCell[][] generateSpawnPointMap (Integer numOfPlayers) {
        HashMap<Integer, MapPoint> idPointMap = new HashMap<>();
        ArrayList<MapCell> cornerSpawns = new ArrayList<>();
        MapCell[][] spawnMap;
        int xOffset, yOffset;
        boolean mapSizeIsValid = false;
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

        spawnMap = new MapCell[width][height];
        fillSea(spawnMap);
        xOffset = (farLeft.x * -1) + 2;
        yOffset = (farDown.y * -1) + 2;
        System.out.println("xOffset = " + xOffset);
        System.out.println("yOffset = " + yOffset);
        for (MapPoint point : idPointMap.values()) {
            System.out.println("Before applying offsets: " + point.x + ", " + point.y);
            point.x += xOffset;
            point.y += yOffset;
            System.out.println("After applying offsets: " + point.x + ", " + point.y);
            System.out.println("---");
            spawnMap[point.x][point.y].makeLand();
            removeNeighborsFromShoreLists(spawnMap, spawnMap[point.x][point.y]);
            addToShoresLists(spawnMap, spawnMap[point.x][point.y]);
            spawnPoints.add(spawnMap[point.x][point.y]);
        }

        System.out.println("Map width: " + width + " Map height: " + height);
        System.out.println(printMap(spawnMap));

        cornerSpawns.add(spawnMap[farLeft.x][farLeft.y]);
        cornerSpawns.add(spawnMap[farRight.x][farRight.y]);
        cornerSpawns.add(spawnMap[farUp.x][farUp.y]);
        cornerSpawns.add(spawnMap[farDown.x][farDown.y]);

        //Connect all the spawn points with land cells
        connectSpawnsWithLand(spawnMap, spawnMap[farLeft.x][farLeft.y], spawnMap[farRight.x][farRight.y]);
        connectSpawnsWithLand(spawnMap, spawnMap[farUp.x][farUp.y], spawnMap[farDown.x][farDown.y]);
        for (MapPoint point : idPointMap.values()) {
            if (!cornerSpawns.contains(spawnMap[point.x][point.y])) {
                connectSpawnsWithLand(spawnMap, spawnMap[point.x][point.y], cornerSpawns.get(rand.nextInt(4)));
            }
        }

        //Make all neighboring cells land to ensure at least 8 neighboring land cells
        for (MapPoint point : idPointMap.values()) {
            for (NeighborLocation nloc : NeighborLocation.values()) {
                if (!spawnMap[point.x + nloc.x][point.y + nloc.y].isLand()) {
                    spawnMap[point.x + nloc.x][point.y + nloc.y] = MapCell.createLandCell(point.x + nloc.x, point.y + nloc.y);
                    removeNeighborsFromShoreLists(spawnMap, spawnMap[point.x + nloc.x][point.y + nloc.y]);
                    addToShoresLists(spawnMap, spawnMap[point.x + nloc.x][point.y + nloc.y]);
                }
            }
        }

        return spawnMap;
    }

    /**
     * Populates the provided HashMap with a number of spawn points.  Spawns will have at least 2
     * cells of space between them and any other spawn. Player spawns will not be an equal
     * distance from every other player spawn.
     * @param numOfPoints the number of points to be generated and added to the map
     * @param pointMap the map that the points should be added to
     */
    private void generateSpawnPoints (int numOfPoints, HashMap<Integer, MapPoint> pointMap) {
        int tempX, tempY, relX, relY, nextId, nextRel;
        boolean neg, valid;

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

    private void connectSpawnsWithLand (MapCell[][] spawnMap, MapCell spawn1, MapCell spawn2) {
        int currentX = spawn1.getxLoc(),
            currentY = spawn1.getyLoc();
        while (currentX != spawn2.getxLoc() || currentY != spawn2.getyLoc()) {
            if (currentX < spawn2.getxLoc()) {
                currentX++;
                spawnMap[currentX][currentY].makeLand();
                removeNeighborsFromShoreLists(spawnMap, spawnMap[currentX][currentY]);
                addToShoresLists(spawnMap, spawnMap[currentX][currentY]);
            }
            if (currentY < spawn2.getyLoc()) {
                currentY++;
                spawnMap[currentX][currentY].makeLand();
                removeNeighborsFromShoreLists(spawnMap, spawnMap[currentX][currentY]);
                addToShoresLists(spawnMap, spawnMap[currentX][currentY]);
            }
            if (currentX > spawn2.getxLoc()) {
                currentX--;
                spawnMap[currentX][currentY].makeLand();
                removeNeighborsFromShoreLists(spawnMap, spawnMap[currentX][currentY]);
                addToShoresLists(spawnMap, spawnMap[currentX][currentY]);
            }
            if (currentY > spawn2.getyLoc()) {
                currentY--;
                spawnMap[currentX][currentY].makeLand();
                removeNeighborsFromShoreLists(spawnMap, spawnMap[currentX][currentY]);
                addToShoresLists(spawnMap, spawnMap[currentX][currentY]);
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
        MapCell[] neighbors;
        int x,y, dirCode;

        switch (shore) {
            case WEST -> {
                if(westShores.size() > 0) {
                    originShore = westShores.get(rand.nextInt(westShores.size()));
                    x = originShore.getxLoc() - 1;
                    y = originShore.getyLoc();
                    map[x][y] = MapCell.createLandCell(x, y);
                    //with new cell created, need to manage lists of shores
                    removeNeighborsFromShoreLists(map, map[x][y]);
                    addToShoresLists(map, map[x][y]);
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
                    map[x][y] = MapCell.createLandCell(x, y);
                    removeNeighborsFromShoreLists(map, map[x][y]);
                    addToShoresLists(map, map[x][y]);
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
                    map[x][y] = MapCell.createLandCell(x, y);
                    removeNeighborsFromShoreLists(map, map[x][y]);
                    addToShoresLists(map, map[x][y]);
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
                    map[x][y] = MapCell.createLandCell(x, y);
                    removeNeighborsFromShoreLists(map, map[x][y]);
                    addToShoresLists(map, map[x][y]);
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
    private void removeNeighborsFromShoreLists (MapCell[][] map, MapCell cell) {
        MapCell[] neighbors  = getNeighbors(map, cell);
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
    private void addToShoresLists(MapCell[][]map, MapCell cell) {
        MapCell[] neighbors  = getNeighbors(map, cell);
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

    private MapCell[] getNeighbors(MapCell[][] map, MapCell cell) {
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

    private boolean isValidSpawn(MapCell[][] map, MapCell cell) {
        MapCell[] neighbors = getNeighbors(map, cell);
        boolean isValid = true;

        for (MapCell neighbor : neighbors) {
            if (neighbor.getDisplayCharId() != MapCell.LAND || neighbor.getDisplayCharId() != MapCell.SEA) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    public String printMap(MapCell[][] map) {
        StringBuilder mapString = new StringBuilder();

        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                mapString.append((char) map[x][y].getDisplayCharId());
            }
            mapString.append(System.lineSeparator());
        }
        return mapString.toString();
    }

    public MapCell[][] getMap() {
        return map;
    }

    public ArrayList<MapCell> getSpawnPoints () {
        return spawnPoints;
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
