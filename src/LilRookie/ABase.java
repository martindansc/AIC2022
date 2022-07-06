package LilRookie;

import aic2022.user.*;

public class ABase extends MyUnit {

    int counter = 0;

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
            /*if(uc.canSpawn(UnitType.RANGER, dir)) {
                uc.spawn(UnitType.RANGER, dir);
            }*/

            if(uc.canSpawn(UnitType.KNIGHT, dir) && counter%3 > 0) {
                uc.spawn(UnitType.KNIGHT, dir);
                counter++;
            }
            else if(uc.canSpawn(UnitType.RANGER, dir) && counter%3 == 0) {
                uc.spawn(UnitType.RANGER, dir);
                counter++;
            }
        }
    }
}
