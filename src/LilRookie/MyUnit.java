package LilRookie;
import aic2022.user.*;

public abstract class MyUnit {
    //Beginning of the shared array slots used to store the total number of units of this type.
    final int COUNTER_INDEX;

    //Since locations can range from (0,0) to (1000,1000), we communicate the position of other objects relative to a common reference location.
    final Location referenceLocation;

    //Each unit should have an instance of each of these
    UnitController uc;
    Pathfinding pathfinding;
    Counter counters;

    Direction exploreDirection = Direction.EAST;
    int stepsToChangeLocation = 0;

    MicroInfo bestMicro = null;

    MyUnit(UnitController unitController) {
        uc = unitController;
        COUNTER_INDEX = Helper.getCounterIndex(uc.getType());
        counters = new Counter(uc);
        referenceLocation = MyUnit.getReferenceLocation(uc);
        pathfinding = new Pathfinding(uc);
    }

    static Location getReferenceLocation(UnitController uc) {
        if(uc.getType() == UnitType.BASE) {
            return uc.getLocation();
        }

        UnitInfo[] units = uc.senseUnits(uc.getTeam());
        for (UnitInfo unit: units) {
            if(unit.getType() == UnitType.BASE) {
                return unit.getLocation();
            }
        }

        uc.println("Error! I should always see my base when spawned");
        uc.killSelf();
        return null;
    }

    /*
    Play is an abstract method, which means that each unit should implement its own play(). This allows different types of units to
    have different behaviors.
     */
    abstract void play();


    /*
    Methods that can be used by all units. This is useful to avoid having duplicate code.
     */

    //Method that updates the counter of the given unit type.
    void countMe() {
        counters.increaseValueByOne(COUNTER_INDEX);
    }

    MicroInfo getMicroInfo(Location nextLocation, boolean isPathfindingDirection) {
        return new MicroInfo(uc, nextLocation, isPathfindingDirection);
    }

    void updateMicroInfo() {
        UnitInfo[] units = uc.senseUnits();
        ChestInfo[] chests = uc.senseChests();
        ShrineInfo[] shrines = uc.senseShrines();

        bestMicro = null;
        for(Direction dir: Direction.values()) {
            Location nextLocation = uc.getLocation().add(dir);
            MicroInfo microInfo = getMicroInfo(nextLocation, exploreDirection.isEqual(dir));

            for(UnitInfo unit: units) {
                microInfo.updateUnits(unit);
            }

            for(ChestInfo chest: chests) {
                microInfo.updateChest(chest);
            }

            for(ShrineInfo shrine: shrines) {
                microInfo.updateShrines(shrine);
            }

            if(bestMicro == null || MicroInfo.isBetterAThanB(microInfo, bestMicro)) {
                bestMicro = microInfo;
            }
        }


    }

    void move(Location loc) {
        pathfinding.moveTo(loc);
    }

    public void openNearChest() {
        ChestInfo[] chests = uc.senseChests();
        for (ChestInfo chest: chests) {
            Location chestLocation = chest.getLocation();
            Direction dir = uc.getLocation().directionTo(chestLocation);
            if(uc.canOpenChest(dir)) {
                uc.openChest(dir);
            }
        }
    }

    public void conquerShrines() {
        if(!uc.canAttack() || !bestMicro.shrineObjective.initialized) {
            return;
        }

        if(uc.canAttack(bestMicro.shrineObjective.location)) {
            uc.attack(bestMicro.shrineObjective.location);
        }
        else {
            uc.println("I calculated I could attack but I actually didn't");
        }

    }

    public boolean tryAttack(UnitInfo enemy) {
        if(enemy != null && uc.canAttack(enemy.getLocation())) {
            uc.attack(enemy.getLocation());
            return true;
        }
        return false;
    }

    public void attack() {
        if(!uc.canAttack()) return;

        Location attackLocation = bestMicro.getAttackLocation();
        if(attackLocation != null && uc.canAttack(attackLocation)) {
            uc.attack(attackLocation);
        }
    }

    public void doMove() {
        if(!uc.canMove()) return;
        if(bestMicro.isUtil()) {
            if(bestMicro.canMoveLocation) {
                if(uc.canMove(bestMicro.direction)) uc.move(bestMicro.direction);
            }
        }
        else {
            if(uc.canMove(exploreDirection) && stepsToChangeLocation > 0) {
                uc.move(exploreDirection);
                stepsToChangeLocation--;
            }
            else {
                exploreDirection = Direction.values()[(int) (uc.getRandomDouble() * 8)];
                stepsToChangeLocation = 100;
            }
        }


    }
}