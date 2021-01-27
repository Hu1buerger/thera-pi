package therapi.updatehint;

import therapi.updatehint.fx.UpdatesMainFX;
import therapi.updatehint.swing.UpdatesMainSwing;

public class runit {
    public static void main(String[] args) {
        HintMain updateCheck = new HintMain(new UpdatesMainSwing());
        updateCheck.execute();
    }

}
