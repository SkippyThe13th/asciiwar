package map;

import game.Player;

public class MapCell {
    private static final Land land = new Land();
    private static final Sea sea = new Sea();
    private Player owner;
    private Integer xLoc, yLoc;
    private HP hp;
    private boolean isLand;

    //acsii codes for '=' (land) and '~' (sea)
    public final static int LAND = 35, SEA = 126;

    public enum HP {
        STRONG,
        WEAK;
    }

    private MapCell() {
        this.xLoc = 0;
        this.yLoc = 0;
        this.owner = sea;
        this.hp = HP.WEAK;
        this.isLand = false;
    }

    private MapCell(Integer x, Integer y) {
        this.xLoc = x;
        this.yLoc = y;
        this.owner = land;
        this.hp = HP.WEAK;
        this.isLand = true;
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

    public void attack(Player attacker) {
        if (owner.getId().equals(LAND)) {
            this.hp = HP.WEAK;
            this.setOwnership(attacker);
        } else if (!owner.getId().equals(SEA)) {
            if (this.hp == HP.WEAK) {
                this.makeUnclaimedLand();
            } else if (this.hp == HP.STRONG) {
                this.hp = HP.WEAK;
            }
        }
    }

    public void setOwnership(Player player) {
        this.owner = player;
    }

    public void makeUnclaimedLand () {
        this.isLand = true;
        this.owner = land;
        this.hp = HP.WEAK;
    }

    public void makeSea() {
        this.owner = sea;
        this.isLand = false;
    }
    public Integer getOwnerId () {
        return owner.getId();
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
        if (this.hp == HP.WEAK) {
            return owner.getWeakDisplay();
        } else {
            return owner.getStrongDisplay();
        }
    }

    public HP getHp () {
        return hp;
    }

    public boolean isLand() {
        return this.isLand;
    }

    private static class Land extends Player {

        private Land () {
            super("Land", LAND, (char)LAND, (char)LAND);
        }
    }

    private static class Sea extends Player {

        private Sea () {
            super("Sea", SEA, (char)SEA, (char)LAND);
        }
    }
}
