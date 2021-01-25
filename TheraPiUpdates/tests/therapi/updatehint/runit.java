package therapi.updatehint;

import therapi.updatehint.fx.UpdatesMainFX;

public class runit {
    public static void main(String[] args) {
        HintMain updateCheck = new HintMain(new UpdatesMainFX());
        updateCheck.execute();
    }

}
