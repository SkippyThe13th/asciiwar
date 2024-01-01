package map;

import game.Player;

public class MapCellComparator {
    //if the cell is unclaimed land
    //

    Type type;
    Player player;

    public MapCellComparator(Type type, Player player) {
        this.type = type;
        this.player = player;
    }

    public boolean matches (MapCell cell) {
        boolean result;
        switch (type) {
            case UNCLAIMED_LAND:
                result = isUnclaimedLand(cell);
                break;
            case ENEMY_LAND:
                result = isEnemyLand(cell);
                break;
            case WEAK_OWNED_LAND:
                result = isWeakOwnedLand(cell);
                break;
            default:
                result = false;
                break;
        }
        return result;
    }

    private boolean isWeakOwnedLand (MapCell cell) {
        return cell != null && cell.getHp().equals(MapCell.HP.WEAK) && cell.getOwnerId().equals(player.getId());
    }

    private boolean isEnemyLand (MapCell cell) {
        return cell != null && player.getEnemyIdList().contains(cell.getOwnerId());
    }

    private boolean isUnclaimedLand (MapCell cell) {
        return cell != null && cell.getOwnerId().equals(MapCell.LAND);
    }

    public enum Type {
        UNCLAIMED_LAND, ENEMY_LAND, WEAK_OWNED_LAND;
    }
}
