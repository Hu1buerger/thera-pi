package therapi.updatehint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thera_pi.updater.Version;

import therapi.updatehint.swing.UpdatesMainSwing;

public class HintMain {
    private HintGui gui;

    public HintMain() {
        this(new UpdatesMainSwing());
    }

    public HintMain(HintGui gui) {
        this.gui = gui;
    }

    private final static Logger logger = LoggerFactory.getLogger(HintMain.class);

    public void execute() {
        Hint hinweis = new Hint();

        if (hinweis.isEnabled()) {
            hinweis.ladeUpdateFiles();

            if (hinweis.updatesVorhanden()) {

                gui.show(hinweis);

            } else {
                logger.debug("No Updates found for Version: " + new Version().number());
            }
        }

    }

}
