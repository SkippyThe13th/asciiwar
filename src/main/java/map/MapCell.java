package map;

import java.util.HashMap;

import game.Player;

public class MapCell {
    private static final Land land = new Land();
    private static final Sea sea = new Sea();
    private long ownerExternalId;
    private int ownerId;
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
        this.ownerId = sea.getId();
        this.ownerExternalId = 0;
        this.hp = HP.WEAK;
        this.isLand = false;
    }

    private MapCell(Integer x, Integer y) {
        this.xLoc = x;
        this.yLoc = y;
        this.ownerId = land.getId();
        this.ownerExternalId = 1;
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

    /**
     * Resolves an attack from a player.  If the attack is against unclaimed land,
     * then the player gains that land.  If the attack is against the land of another
     * player, then the hp of the land is reduced.  If the owned land has an hp of
     * WEAK, then the owner loses control of the land
     * @param attacker the attacking player
     * @return the Player whose borders should be reevaluated.  If the attack gained land,
     *          this will be the attacker.  If not, then it will be the owner of the land
     *          prior to the attack.  Or, if no reevaluation is needed, null.
     */
    public Player attack(Player attacker, HashMap<Integer, Player> idPlayerMap) {
        Player playerToReevaluate = null;
        //If the attacked space is empty land
        if (ownerId == LAND) {
            this.hp = HP.WEAK;
            this.setOwnership(attacker);
            attacker.addToTerritory(this);
            playerToReevaluate = attacker;
        //If the attacked space belongs to an enemy
        } else if (attacker.getEnemyIdList().contains(this.getOwnerId())) {
            Player owner = idPlayerMap.get(ownerId);
            playerToReevaluate = owner;
            if (this.hp == HP.WEAK && owner.getTerritory().size() > 1) {
                owner.removeFromTerritory(this);
                this.makeUnclaimedLand();
            } else if (this.hp == HP.STRONG) {
                this.hp = HP.WEAK;
            }
        //If the "attacked" space belongs to the attacker
        } else if (this.getOwnerId().equals(attacker.getId())) {
            this.hp = HP.STRONG;
            playerToReevaluate = attacker;
        }
        return playerToReevaluate;
    }

    public void setOwnership(Player player) {
        this.ownerId = player.getId();
        this.ownerExternalId = player.getExternalId();

    }

    public void makeUnclaimedLand () {
        this.isLand = true;
        this.ownerId = land.getId();
        this.ownerExternalId = 1;
        this.hp = HP.WEAK;
    }

    public void makeSea() {
        this.ownerId = sea.getId();
        this.ownerExternalId = 0;
        this.isLand = false;
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
        if (this.hp == HP.WEAK) {
            return ownerId;
        } else {
            return ownerId - 32;
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
