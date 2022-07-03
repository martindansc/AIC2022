package LilRookie;

import aic2022.user.*;

public class ABase extends MyUnit {

    ABase(UnitController unitController) {
        super(unitController);
    }

    @Override
    void play() {
        spawn();
        attack();
    }

    void spawn() {
        Direction[] directions = Direction.values();
        for (Direction dir: directions) {
            if(uc.canSpawn(UnitType.KNIGHT, dir)) {
                uc.spawn(UnitType.KNIGHT, dir);
            }
        }
    }
}
