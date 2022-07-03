package LilRookie;

import aic2022.user.*;

public class AKnight extends MyUnit {

    AKnight(UnitController unitController) {
        super(unitController);
    }

    @Override
    void play() {
        openNearChest();
        attack();
        doMove();
        attack();
        conquerShrines();
        explore();
    }
}