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

    MicroInfo[] microInfos = null;
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

    void updateMicroInfo() {
        UnitInfo[] units = uc.senseUnits();
        ChestInfo[] chests = uc.senseChests();
        ShrineInfo[] shrines = uc.senseShrines();

        MicroInfo[] microInfos = new MicroInfo[Direction.values().length];
        bestMicro = null;
        for(Direction dir: Direction.values()) {
            MicroInfo microInfo = new MicroInfo(uc, uc.getLocation().add(dir), false);

            for(UnitInfo unit: units) {
                microInfo.updateUnits(unit);
            }

            for(ChestInfo chest: chests) {
                microInfo.updateChest(chest);
            }

            for(ShrineInfo shrine: shrines) {
                microInfo.updateShrines(shrine);
            }

            microInfos[dir.ordinal()]  = microInfo;

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
        ShrineInfo[] shrines = uc.senseShrines();
        for(ShrineInfo shrine: shrines) {
            Location shrineLocation = shrine.getLocation();
            if(uc.canAttack(shrineLocation) && !shrineLocation.isEqual(uc.getLocation())) {
                uc.attack(shrineLocation);
            }
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

        boolean attacked = false;
        UnitInfo bestEnemyToAttack = null;

        if(bestMicro.enemyToAttack != null && uc.canAttack(bestMicro.enemyToAttack.getLocation())) {
            bestEnemyToAttack = bestMicro.enemyToAttack;
        }

        UnitInfo[] enemies = uc.senseUnits(uc.getOpponent());
        for(UnitInfo enemy: enemies) {
            if(uc.canAttack() && (bestEnemyToAttack == null || bestEnemyToAttack.getHealth() > enemy.getHealth())) {
                bestEnemyToAttack = enemy;
                attacked = tryAttack(bestEnemyToAttack);
            }
        }

        if(!attacked) {
            UnitInfo[] neutrals = uc.senseUnits(Team.NEUTRAL);
            for (UnitInfo enemy : neutrals) {
                if (uc.canAttack() && (bestEnemyToAttack == null || bestEnemyToAttack.getHealth() > enemy.getHealth())) {
                    bestEnemyToAttack = enemy;
                }
            }
        }

        tryAttack(bestEnemyToAttack);

        if(bestEnemyToAttack != null) {
            uc.setOrientation(uc.getLocation().directionTo(bestEnemyToAttack.getLocation()));
        }
    }

    public void doMove() {

    }

    public void explore() {
        if(!uc.canMove()) return;

        if(stepsToChangeLocation == 0 || !uc.canMove(exploreDirection)) {
            exploreDirection = Direction.values()[(int) (uc.getRandomDouble() * 8)];
            stepsToChangeLocation = 100;
        }

        if(uc.canMove(exploreDirection)) {
            stepsToChangeLocation--;
            pathfinding.resetPathfinding();
            uc.move(exploreDirection);
        }
    }
}