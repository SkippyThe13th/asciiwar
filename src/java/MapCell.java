public class MapCell {
    private Integer xLoc, yLoc, ownerId;
    private int displayCharId;
    private HP hp;
    private boolean isLand;

    //acsii codes for '=' (land) and '~' (sea)
    public final static Integer LAND = 35, SEA = 126;

    public enum HP {
        STRONG,
        WEAK,
        VULN;
    }

    private MapCell() {
        this.xLoc = 0;
        this.yLoc = 0;
        this.ownerId = SEA;
        this.hp = HP.VULN;
        this.isLand = false;
        this.displayCharId = SEA;
    }

    private MapCell(Integer x, Integer y) {
        this.xLoc = x;
        this.yLoc = y;
        this.ownerId = LAND;
        this.hp = HP.VULN;
        this.isLand = true;
        this.displayCharId = LAND;
    }

    /**
     * Returns a new MapCell that is roughly in the center of a map with the given dimensions
     * @param height the height of the map the cell will belong to.
     * @param width the width of the map the cell will belong to.
     * @return a MapCell with its x and y values set to roughly the center of the map
     */
    public static MapCell createOriginCell(Integer height, Integer width) {
        return new MapCell(width/2, height/2);
    }

    public static MapCell createOceanCell(int x, int y) {
        MapCell oceanCell = new MapCell();
        oceanCell.setxLoc(x);
        oceanCell.setyLoc(y);
        return oceanCell;
    }

    public static MapCell createLandCell(int x, int y) {
        return new MapCell(x, y);
    }

    public void bolster(int ownerId) {
        if (hp == HP.VULN && isLand) {
            hp = HP.WEAK;
            this.ownerId = ownerId;
            displayCharId = ownerId;
        } else if (hp == HP.WEAK) {
            hp = HP.STRONG;
            displayCharId -= 32;
        }
    }

    public void weaken() {
        if (hp == HP.STRONG) {
            hp = HP.WEAK;
        } else if (hp == HP.WEAK) {
            hp = HP.VULN;
            ownerId = 0;
        }
    }

    public void makeLand() {
        this.isLand = true;
        this.ownerId = LAND;
        this.displayCharId = LAND;
    }

    public void makeSea() {
        this.isLand = false;
        this.ownerId = SEA;
        this.displayCharId = SEA;
    }
    public Integer getOwnerId () {
        return ownerId;
    }

    public Integer getxLoc () {
        return xLoc;
    }

    private void setxLoc(Integer xLoc) {
        this.xLoc = xLoc;
    }

    public Integer getyLoc () {
        return yLoc;
    }

    private void setyLoc(Integer yLoc) {
        this.yLoc = yLoc;
    }

    public int getDisplayCharId () {
        return displayCharId;
    }

    public HP getHp () {
        return hp;
    }

    public boolean isLand() {
        return this.isLand;
    }

}
