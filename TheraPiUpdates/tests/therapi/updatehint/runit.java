package therapi.updatehint;

import therapi.updatehint.fx.UpdatesMainFX;
import therapi.updatehint.swing.UpdatesMain;

public class runit {
    public static void main(String[] args) {
        HintMain updateCheck = new HintMain(new UpdatesMain());
        updateCheck.execute();
    }

}
