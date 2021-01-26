package therapi.updatehint;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public final class HyperLInkEventHandler implements EventHandler<ActionEvent> {
     public static final String UPDATES_PAGE = "https://www.thera-pi-software.de/downloads/";
    private static final Logger logger =LoggerFactory.getLogger(HyperLInkEventHandler.class);
    @Override
    public void handle(ActionEvent event) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(new URL(UPDATES_PAGE).toURI());
                } catch (IOException | URISyntaxException e) {
                     logger.error("cannot open url in system browser", e);
                }
            }
        }

    }
}
