package org.thera_pi.updater;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;

public class Updater implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(Updater.class);

    private Stoppable myParent;

    private UpdateRepository repo;

    private CountDownLatch latch = new CountDownLatch(1);

    private UpdateUI ui;

    private List<File> neededList;

    private DateiSieb sieb;

    Updater(UpdateRepository repo, Stoppable killme) {
        this.setRepo(repo);
        if (killme != null) {
            setMyParent(killme);
        }
        latch = new CountDownLatch(1);
    }

    Updater() {
    }

    public void killParent() {

        myParent.stop(latch);
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("", e);
        }

    }

    public List<File> needsFrom(List<File> filesList) {

        return sieb.select(filesList);
    }

    public UpdateConsent askUserforConsent() {
        return ui.askForConsent();
    }

    public int downloadzips() {
        return repo.downloadFiles(neededList, null);
    }

    public Integer unpack() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void restartMain() {
        // TODO Auto-generated method stub

    }

    public void run() {

    }

    @Override
    public Integer call() throws Exception {

        List<File> downloadedFiles = Collections.emptyList();
        Integer ausgepackte = 0;
        List<File> filesList = repo.filesList();
        neededList = needsFrom(filesList);
        if (!neededList.isEmpty()) {
            UpdateConsent consent = askUserforConsent();
            if (consent.allowsDownload()) {

                downloadzips();
            }

            if (!downloadedFiles.isEmpty() && consent.allowsInstall()) {
                killParent();
                ausgepackte = unpack();
                restartMain();
            }
        }
        return ausgepackte;
    }

    void setUI(UpdateUI updateUI) {

        setUi(updateUI);
    }

    void setRepo(UpdateRepository repo) {
        this.repo = repo;
    }

    void setMyParent(Stoppable myParent) {
        this.myParent = myParent;
    }

    void setUi(UpdateUI ui) {
        this.ui = ui;
    }

    public static void main(String[] args) throws Exception {
        DefaultUpdateUI.initToolkit();

        Platform.runLater(() -> {
            DefaultUpdateUI userinterface;
            userinterface = new DefaultUpdateUI();

            Updater updater;
            try {
                updater = new UpdaterFactory().withRepository(new FTPRepository())
                                              .withUI(userinterface)
                                              .build();
                updater.call();
                Platform.exit();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logger.error("bad things happen here", e);
            }

        });

    }

    public void SetDateiSieb(DateiSieb dateiSieb) {
        sieb = dateiSieb;
    }
}
