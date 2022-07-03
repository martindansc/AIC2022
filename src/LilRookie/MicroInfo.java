package LilRookie;

import aic2022.user.*;

public class MicroInfo {

    Location location;
    UnitType myType;

    int numEnemies;
    int minDistToEnemy;
    UnitInfo enemyToAttack;

    MapObjective shrineObjective;

    int minDistToChest;
    ChestInfo chestToGet;

    UnitController uc;

    Direction direction;
    boolean canMoveLocation;
    boolean needsToMoveToAttack;

    boolean isPathfinderDirection;

    MicroInfo(UnitController uc, Location location, boolean isPathfinderDirection) {
        this.uc = uc;
        this.location = location;
        this.myType = uc.getType();
        this.isPathfinderDirection = isPathfinderDirection;
        numEnemies = 0;
        minDistToEnemy = Integer.MAX_VALUE;
        minDistToChest = Integer.MAX_VALUE;
        enemyToAttack = null;

        direction = uc.getLocation().directionTo(location);
        canMoveLocation = uc.canMove(direction);
    }

    public void updateUnits(UnitInfo unit) {
        if(!canMoveLocation) return;

        if(unit.getTeam() != uc.getTeam() && canMoveLocation) {
            Location enemyLocation = unit.getLocation();
            Direction directionToMe = enemyLocation.directionTo(location);
            Location oneStepEnemyLocation = enemyLocation.add(directionToMe);

            int distance = location.distanceSquared(enemyLocation);

            canEnemyAttackMe(unit, oneStepEnemyLocation);
            canIAttackEnemy(unit, enemyLocation);

            if (distance < minDistToEnemy) minDistToEnemy = distance;
        }
    }

    public void updateChest(ChestInfo chest) {
        if(!canMoveLocation) return;

        Location chestLocation = chest.getLocation();
        int distance = location.distanceSquared(chestLocation);
        if (distance < minDistToChest) {
            minDistToChest = distance;
            chestToGet = chest;
        }
    }

    public void updateShrines(ShrineInfo shrine) {
        if(!canMoveLocation) return;

        Location shrineLocation = shrine.getLocation();
        shrineObjective.getAttackablePassableNearest(shrineLocation, shrine);
    }

    private void canEnemyAttackMe(UnitInfo unit, Location oneStepEnemyLocation) {
        UnitType enemyType = unit.getType();
        if(enemyType.getStat(UnitStat.ATTACK) == 0) return;

        // can he attack me if I move here?
        int distance = oneStepEnemyLocation.distanceSquared(location);
        if (distance <= enemyType.getStat(UnitStat.ATTACK_RANGE) &&
                distance >= enemyType.getStat(UnitStat.MIN_ATTACK_RANGE) &&
                !uc.isObstructed(oneStepEnemyLocation, location)) {
            numEnemies++;
        }
    }

    private void canIAttackEnemy(UnitInfo unit, Location enemyLocation) {
        if(myType.getStat(UnitStat.ATTACK) == 0) return;

        // can I attack him if I move here?
        if (Helper.canAttackLocation(uc, unit,enemyLocation)) {
            if(enemyToAttack == null || isBetterToAttack(unit, enemyToAttack)) {
                setUnitToAttack(unit, enemyLocation);
            }
        }
    }

    public boolean canKillEnemy() {
        if(enemyToAttack == null) return false;
        return enemyToAttack.getHealth() < myType.getStat(UnitStat.ATTACK_RANGE);
    }

    private void setUnitToAttack(UnitInfo unit, Location enemyLocation) {
        enemyToAttack = unit;

        int distance = uc.getLocation().distanceSquared(enemyLocation);
        needsToMoveToAttack = distance <= myType.getStat(UnitStat.ATTACK_RANGE) &&
                distance >= myType.getStat(UnitStat.MIN_ATTACK_RANGE) &&
                !uc.isObstructed(enemyLocation, location);
    }

    public Location moveLocation() {
        return location;
    }

    public Location attackLocation() {
        if(enemyToAttack != null) {
            return enemyToAttack.getLocation();
        }
        return null;
    }

    private static boolean isBetterToAttack(UnitInfo a, UnitInfo b) {

        if(a == null && b == null) return false;
        if(a == null) {
            return false;
        }
        if(b == null) {
            return true;
        }

        int aEnemyHealth = a.getHealth();
        int bEnemyHealth = b.getHealth();
        int damage = (int) b.getType().getStat(UnitStat.ATTACK);

        if(bEnemyHealth > damage && aEnemyHealth < damage) {
            return true;
        }

        if(bEnemyHealth < damage && aEnemyHealth > damage) {
            return false;
        }

        if(aEnemyHealth < damage) {
            return aEnemyHealth > bEnemyHealth;
        }

        return aEnemyHealth < bEnemyHealth;
    }

    double selfPoints() {
        double points = 0;
        points -= numEnemies;
        if(isPathfinderDirection) {
            points += 1;
        }

        if(MapObjective.canAttack(shrineObjective)){
            points += 0.5;
        }

        return points;
    }

    static boolean isBetterAThanB(MicroInfo a, MicroInfo b) {
        double pa = 0, pb = 0;

        if(a.canMoveLocation && !b.canMoveLocation) {
            return true;
        }

        if(b.canMoveLocation && !a.canMoveLocation) {
            return false;
        }

        if(MapObjective.isCloser(a.shrineObjective, b.shrineObjective)) {
            pa += 1;
        }
        else if(MapObjective.isCloser(b.shrineObjective, a.shrineObjective)) {
            pb += 1;
        }

        if(a.minDistToChest < b.minDistToChest) {
            pa += 2;
        }
        else if(b.minDistToChest < a.minDistToChest) {
            pb += 2;
        }

        if(a.minDistToEnemy < b.minDistToEnemy) {
            pa -= 1;
        }

        if(a.enemyToAttack == null && b.enemyToAttack != null) {
            pb -= 1;
        }

        if(b.enemyToAttack == null && a.enemyToAttack != null) {
            pa += 1;
        }

        pa += a.selfPoints();
        pb += b.selfPoints();

        return pa > pb;
    }
}
