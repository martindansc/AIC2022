package LilRookie;

import aic2022.user.UnitController;

public class ARanger extends MyUnit {

    ARanger(UnitController unitController) {
        super(unitController);
    }

    @Override
    void play() {
        openNearChest();
        attack();
        conquerShrines();
        doMove();
        attack();
        conquerShrines();
    }
}