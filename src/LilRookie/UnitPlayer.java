package LilRookie;

import aic2022.user.*;

public class UnitPlayer {

	public void run(UnitController uc) {

		MyUnit me = null;
		if(uc.getType() == UnitType.BASE) {
			me = new ABase(uc);
		}
		else if(uc.getType() == UnitType.KNIGHT) {
			me = new AKnight(uc);
		}

		while (true) {
			me.countMe();
			me.updateMicroInfo();
			me.play();
			uc.yield(); // End of turn
		}
	}
}
