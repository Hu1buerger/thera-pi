package org.thera_pi.updater;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class UpdaterFactory {


    UpdateRepository  emptyrepo =new EmptyRepository();
    private UpdateRepository repository=emptyrepo;
    private Stoppable toBeKilled = (latch) -> latch.countDown();
    private UpdateUI ui = () -> (new UpdateConsent(true, true));
    private DateiSieb sieb = filesList -> filesList;
    public UpdaterFactory withRepository(UpdateRepository repo) {
        this.repository = repo;
        return this;
    }

    public UpdaterFactory withStoppable(Stoppable killme) {
        this.toBeKilled = killme;
        return this;
    }

    public UpdaterFactory withUI(UpdateUI userinterface) {
        this.ui = userinterface;
        return this;
    }

    public UpdaterFactory withDateiSieb(DateiSieb dateiSieb) {
        this.sieb=dateiSieb;
        return this;
    }

    public Updater build() {
        Updater updater = new Updater();
        updater.setRepo(repository);
        updater.setMyParent(toBeKilled);
        updater.setUi(ui);
        updater.SetDateiSieb(sieb);
        return updater;
    }
    private static final class EmptyRepository implements UpdateRepository {
        @Override
        public List<File> filesList() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public int downloadFiles(List<File> neededList, Path path) {
            return 0;
        }
    }
}
