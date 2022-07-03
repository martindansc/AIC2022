package LilRookie;

import aic2022.user.*;
public class Counter {

    UnitController uc;
    public final int COUNTERS_SPACE = 4;

    Counter(UnitController unitController) {
        uc = unitController;
    }

    public void reset(int key) {
        for(int i = 0; i < COUNTERS_SPACE; i++) {
            uc.writeOnSharedArray(key + i, 0);
        }
    }

    private void roundClear(int key) {
        int shift = uc.getRound() & 1;
        if(uc.readOnSharedArray(key + shift) != uc.getRound()) {
            uc.writeOnSharedArray(key + shift, uc.getRound());
            uc.writeOnSharedArray(key + shift + 2, 0);
        }
    }

    public void increaseValue(int key, int amount) {
        this.roundClear(key);

        int shift = uc.getRound() & 1;

        int realId = key + 2 + shift;
        int value = uc.readOnSharedArray(realId);
        uc.writeOnSharedArray(realId, value + amount);
    }

    public void increaseValueByOne(int key) {
        this.increaseValue(key, 1);
    }

    public int read(int key) {
        int lshift = uc.getRound() & 1;
        int rshift = (uc.getRound() + 1) & 1;

        int left = 0;
        int right = 0;

        if(uc.readOnSharedArray(key + lshift) == uc.getRound()) {
            left = uc.readOnSharedArray(key + lshift + 2);
        }

        if(uc.readOnSharedArray(key + rshift) == uc.getRound() - 1) {
            right = uc.readOnSharedArray(key + rshift + 2);
        }

        return Math.max(left, right);
    }

    public int readThisRoundOnly(int key) {
        int lshift = uc.getRound() & 1;
        int left = 0;
        if(uc.readOnSharedArray(key + lshift) != uc.getRound()) {
            left = uc.readOnSharedArray(key + lshift + 2);
        }
        return left;
    }

}
