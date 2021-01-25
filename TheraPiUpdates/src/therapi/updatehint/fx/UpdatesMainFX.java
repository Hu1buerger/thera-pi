package therapi.updatehint.fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thera_pi.updater.Version;

import com.sun.javafx.application.PlatformImpl;

import javafx.stage.Stage;
import therapi.updatehint.Hint;
import therapi.updatehint.HintGui;

public class UpdatesMainFX implements HintGui , Runnable{
    private static final Logger logger = LoggerFactory.getLogger(UpdatesMainFX.class);
    private Hint hinweis;

    public UpdatesMainFX() {
    }

    @Override
    public void run() {
        try {
            new Main(hinweis).start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Hint hinweis = new Hint();

        if (hinweis.isEnabled()) {
            hinweis.ladeUpdateFiles();

            if (hinweis.updatesVorhanden()) {
                PlatformImpl.startup(new UpdatesMainFX());
            } else {
                logger.debug("No Updates found for Version: " + new Version().number());
            }
        }
    }

    @Override
    public void show(Hint hint) {

        PlatformImpl.startup(new Main(hint));

    }
}
