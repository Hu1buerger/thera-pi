package org.thera_pi.updater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

/**
 * wenn es Dateien zum Download gibt
 * <p>
 * wird der Benutzer gefragt, ob das jetzt geschehen soll
 * <p>
 * bei Bestaetigung werden die zip-Dateien heruntergeladen
 * <p>
 * wenn alle Dateine heruntergeladen sind wird geprueft ob sie installiert werden
 * müssen. Ist dies der Fall wird der Benutzer gefragt, ob das jetzt passieren
 * soll
 * <p>
 * Der Updater schliesst die Hauptanwendung und entpackt das zip und startet die
 * Hauptanwendung neu.
 *
 */
public class UpdaterTest {
    private static final class AskmeUI implements UpdateUI {
        boolean beenasked = false;
        @Override
        public UpdateConsent askForConsent() {
           beenasked=true;
            return new UpdateConsent(true, true);
        }

        public Object beenAsked() {
            return beenasked;
        }
    }

    @Test
    public void derUpdaterKannseinenCallerBeenden() throws Exception {
        ;
        Victim victim = new Victim();
        Thread victimThread = new Thread(victim);
        victimThread.start();
        assertTrue(victimThread.isAlive());

        Updater updater = new UpdaterFactory().withStoppable(victim).build();
         updater.killParent();
        //There is a delay between end of method and death of thread  :-(
       Thread.sleep(20);
        assertFalse(victimThread.isAlive());

    }

    @Test
    public void UpdaterKannauchAlsWaiseleben() throws Exception {
            Updater updater = new UpdaterFactory().build();
            updater.killParent();
    }


    /**wenn es Dateien zum Download gibt
    * <p>
    * wird der Benutzer gefragt, ob das jetzt geschehen soll
    * <p>
    * bei Bestaetigung werden die zip-Dateien heruntergeladen
    * <p>
    * wenn alle Dateine heruntergeladen sind wird geprueft ob sie installiert werden
    * müssen. Ist dies der Fall wird der Benutzer gefragt, ob das jetzt passieren
    * soll
    * <p>
    * Der Updater schliesst die Hauptanwendung und entpackt das zip und startet die
    * Hauptanwendung neu.
    * */

    @Test
    public void anEmptyRepoUpdatesNoFiles() throws Exception {

        UpdateRepository  emptyrepo =new UpdateRepository() {

            @Override
            public List<File> filesList() {
                return Collections.emptyList();
            }

            @Override
            public int downloadFiles(List<File> neededList, Path path) {
                // TODO Auto-generated method stub
                return 0;
            }
        };
        Updater updater= new UpdaterFactory().withRepository(emptyrepo).build();
        assertEquals(Integer.valueOf(0),updater.call());
    }

    @Test
    public void nonEmptyRepoListasksUserforConsent() throws Exception {
        UpdateRepository nonEmpty = new UpdateRepository() {

            @Override
            public List<File> filesList() {
                List<File> files = new LinkedList<File>();
                files.add(new File("whereever"));
                return files;
            }

            @Override
            public int downloadFiles(List<File> neededList, Path path) {
                for (File file : neededList) {

                }

                return 0;
            }
        };

        AskmeUI updateUI= new AskmeUI();

        Updater updater= new UpdaterFactory().withRepository(nonEmpty).build();
        updater.setUI(updateUI);
        assertEquals(Integer.valueOf(0),updater.call());
        assertEquals(true, updateUI.beenAsked());
    }

}
