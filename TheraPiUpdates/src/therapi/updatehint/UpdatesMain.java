package therapi.updatehint;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thera_pi.updater.HTTPRepository;
import org.thera_pi.updater.Version;
import org.thera_pi.updater.VersionsSieb;

import com.sun.javafx.application.PlatformImpl;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;

public class UpdatesMain implements Runnable {


    @Override
    public void run() {


            try {


                Application app2;
                app2 = Main.class.newInstance();

                Stage anotherStage = new Stage();
                app2.start(anotherStage);

            } catch ( Exception e) {
                e.printStackTrace();
            }

    }


    public static void main(String[] args) {
        List<File> updatefiles = new VersionsSieb(new Version()).select( new HTTPRepository().filesList());


        if(!updatefiles.isEmpty()) {
            Main.updatefiles = updatefiles;
            PlatformImpl.startup(new UpdatesMain());
        } else {
            Logger logger =LoggerFactory.getLogger(UpdatesMain.class);
            logger.debug("No Updates found for Version: " + new Version().number());
        }
    }
}
