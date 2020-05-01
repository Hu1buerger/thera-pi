package org.thera_pi.updater;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ChoiceDialog;

public class DefaultUpdateUI extends ChoiceDialog<UpdateConsent> implements UpdateUI {

    private static UpdateConsent both = new UpdateConsent(true, true);
    private static UpdateConsent downloadOnly = new UpdateConsent(true, false);
    private static UpdateConsent donothing = new UpdateConsent(false, false);

    public DefaultUpdateUI() {
        super(both, both, downloadOnly, donothing);
    }

    @Override
    public UpdateConsent askForConsent() {

        return showAndWait().get();
    }

    public static void main(String[] args) throws InterruptedException {
        initToolkit();
        Platform.runLater(() -> {
            DefaultUpdateUI dialog = new DefaultUpdateUI();
          System.out.println(  dialog.askForConsent());
          Platform.exit();
        });

    }

     static void initToolkit() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // initializes JavaFX environment
            latch.countDown();
        });

        if (!latch.await(5L, TimeUnit.SECONDS))
            throw new ExceptionInInitializerError();
    }
}
