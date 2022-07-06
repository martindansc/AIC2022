package LilRookie;

import aic2022.user.*;

public class MapObjective<T> {
    UnitController uc;

    int minDist = Integer.MAX_VALUE;
    boolean canAttack;
    boolean isObstructed;
    T objectiveInfo;
    Location location;
    Location myLocation;
    boolean initialized = false;

    MapObjective(UnitController uc, Location myLocation) {
        this.uc = uc;
        this.myLocation = myLocation;
    }

    private void assign(Location newLocation, T newInfo) {
        initialized = true;
        location = newLocation;
        minDist = myLocation.distanceSquared(newLocation);
        canAttack = Helper.canAttackLocation(uc, myLocation, newLocation);
        isObstructed = uc.isObstructed(uc.getLocation(), newLocation);
        objectiveInfo = newInfo;
    }

    public void getPassableNearest(Location newLocation, T newInfo) {
        if(!initialized) {
            assign(newLocation, newInfo);
            return;
        }

        int distance = location.distanceSquared(newLocation);
        Boolean isObstructedNewLocation = uc.isObstructed(uc.getLocation(), newLocation);

        if(!isObstructedNewLocation && isObstructed) {
            assign(newLocation, newInfo);
        }
        else if(distance < this.minDist) {
            assign(newLocation, newInfo);
        }
    }

    public void getAttackablePassableNearest(Location newLocation, T newInfo) {
        if(!initialized) {
            assign(newLocation, newInfo);
            return;
        }

        int distance = location.distanceSquared(newLocation);
        Boolean canAttackNewLocation = Helper.canAttackLocation(uc, myLocation, newLocation);
        Boolean isObstructedNewLocation = uc.isObstructed(uc.getLocation(), newLocation);

        if(canAttackNewLocation && !canAttack) {
           assign(newLocation, newInfo);
        }
        else if(!isObstructedNewLocation && isObstructed) {
            assign(newLocation, newInfo);
        }
        else if(distance < this.minDist) {
            assign(newLocation, newInfo);
        }

    }

    // STATIC FUNCTIONS
    public static boolean canAttack(MapObjective a) {
        if(a == null) return false;
        return a.canAttack;
    }

    public static boolean isCloser(MapObjective a, MapObjective b){
        if(a == null && b == null) return  false;
        if(a == null && b != null) return false;
        if(b == null && a != null) return true;

        if(!a.isObstructed && b.isObstructed) return true;
        if(!b.isObstructed && a.isObstructed) return false;

        return a.minDist < b.minDist;
    }

    public static boolean isBetterAttackablePassableNearest(MapObjective a, MapObjective b) {
        if(a == null && b == null) return  false;
        if(a == null && b != null) return false;
        if(b == null && a != null) return true;


        if(a.canAttack && !b.canAttack) return true;
        if(b.canAttack && !a.canAttack) return false;
        if(a.isObstructed && !b.isObstructed) return false;
        if(b.isObstructed && !a.isObstructed) return true;

        return a.minDist > b.minDist;
    }

    public static boolean isBetterAttackablePassableFurthest(MapObjective a, MapObjective b) {
        if(a == null && b == null) return  false;
        if(a == null && b != null) return false;
        if(b == null && a != null) return true;


        if(a.canAttack && !b.canAttack) return true;
        if(b.canAttack && !a.canAttack) return false;
        if(a.isObstructed && !b.isObstructed) return false;
        if(b.isObstructed && !a.isObstructed) return true;

        return a.minDist > b.minDist;
    }
}
