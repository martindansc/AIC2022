package LilRookie;

import aic2022.user.*;

public class MicroInfo {

    Location location;
    UnitType myType;

    int numEnemies;
    double enemyDanger;
    int minDistToEnemy;
    UnitInfo enemyToAttack;
    MapObjective shrineObjective;
    MapObjective chestObjective;

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
        enemyDanger = 0;
        minDistToEnemy = Integer.MAX_VALUE;
        enemyToAttack = null;
        chestObjective = new MapObjective<ChestInfo>(uc, location);
        shrineObjective = new MapObjective<ShrineInfo>(uc, location);

        direction = uc.getLocation().directionTo(location);
        canMoveLocation = uc.canMove(direction);
    }

    public void updateUnits(UnitInfo unit) {
        if(!canMoveLocation || direction == Direction.ZERO) return;

        if(unit.getTeam() != uc.getTeam()) {
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
        chestObjective.getPassableNearest(chestLocation, chest);
    }

    public void updateShrines(ShrineInfo shrine) {
        if(!canMoveLocation) return;

        if(shrine.getOwner() != uc.getTeam() || !shrine.hasFullInfluence()) {
            Location shrineLocation = shrine.getLocation();
            shrineObjective.getAttackablePassableNearest(shrineLocation, shrine);
        }
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
        if (Helper.canAttackLocation(uc, location, enemyLocation)) {
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

    boolean isUtil() {
        if(shrineObjective.initialized) return true;
        if(numEnemies > 0) return  true;
        if(chestObjective.initialized) return true;
        if(enemyToAttack != null) return true;
        return false;
    }

    Location getAttackLocation() {
        if(enemyToAttack != null) {
            return enemyToAttack.getLocation();
        }
        else if(shrineObjective.initialized) {
            return shrineObjective.location;
        }

        return null;
    }

    double selfPoints() {
        double points = 0;
        points -= numEnemies;

        if(isPathfinderDirection) {
            points += 1;
        }

        if(uc.canSenseLocation(location) && uc.senseTileTypeAtLocation(location) == TileType.SHRINE){
            points -= 1.5;
        }

        if(MapObjective.canAttack(shrineObjective)){
            points += 0.5;
        }

        return points;
    }

    static boolean isBetterAThanB(MicroInfo a, MicroInfo b) {
        if(a.selfPoints() >= b.selfPoints()) return true;
        return false;
    }
}
