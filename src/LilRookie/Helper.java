package LilRookie;

import aic2022.user.*;
// this class is for small utility methods that can be called from anywhere in the code
public class Helper {

    static public Direction getRandomDirection() {
        int randomNumber = (int)(Math.random()*Direction.values().length);
        return Direction.values()[randomNumber];
    }

    static public int getCounterIndex(UnitType type){
        return 4*type.ordinal();
    }

    static public int locationToInt(Location loc, Location base) {
        int MAX_MAP_SIZE = GameConstants.MAX_MAP_SIZE;
        int x = (loc.x - base.x) + MAX_MAP_SIZE - 1;
        int y = (loc.y - base.y) + MAX_MAP_SIZE - 1;
        return x * MAX_MAP_SIZE*2 + y;
    }

    static public Location intToLocation(int loc, Location base) {
        int MAX_MAP_SIZE = GameConstants.MAX_MAP_SIZE;

        int x = loc/(MAX_MAP_SIZE*2) - MAX_MAP_SIZE + 1 + base.x;
        int y = loc%(MAX_MAP_SIZE*2) - MAX_MAP_SIZE + 1 + base.y;

        return new Location(x, y);
    }

    static public boolean canAttackLocation(UnitController uc, UnitInfo unit, Location loc) {
        Location myLocation = unit.getLocation();
        int distance = myLocation.distanceSquared(loc);
        if (distance <= unit.getStat(UnitStat.ATTACK_RANGE) &&
                distance >= unit.getStat(UnitStat.MIN_ATTACK_RANGE) &&
                !uc.isObstructed(loc, myLocation)) {
            return true;
        }
        return  false;
    }
}
