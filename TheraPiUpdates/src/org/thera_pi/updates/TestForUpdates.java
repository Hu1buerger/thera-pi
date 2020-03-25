package org.thera_pi.updates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.*;

import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.INIFile;
import environment.Path;

public class TestForUpdates {

    private static final Logger LOG = LoggerFactory.getLogger(TestForUpdates.class);

    private static List<List<String>> updatefiles = new ArrayList<>();
    private static List<String[]> mandvec = new ArrayList<>();

    public TestForUpdates() {
        try {
            doHoleUpdateConfSilent();
        } catch (Exception ex) {
            LOG.error("Exception: " + ex.getMessage(), ex);
        }
    }

    private void doHoleUpdateConfSilent() {
        try {
            FTPTools ftpt = new FTPTools();
            ftpt.holeDateiSilent("update.files", Path.Instance.getProghome());
            updateCheck(Path.Instance.getProghome() + "update.files");
        } catch (Exception ex) {
            LOG.error("", ex);
        }
    }

    public boolean doFtpTest() {
        FTPTools ftpt = new FTPTools();
        FTPFile[] ffile = ftpt.holeDatNamen();
        for (int i = 0; i < ffile.length; i++) {
            try {
                String name = ffile[i].getName()
                                      .trim();
                if ((!name.equals(".")) && (!name.equals("..")) && (!name.startsWith("update."))
                        && mussUpdaten(name, ffile[i].getTimestamp()
                                                     .getTime()
                                                     .getTime())) {
                    ftpt.connectTest();
                    return true;
                }
            } catch (Exception ex) {
                String message = "Fehler beim Bezug der Datei Nr. " + (i + 1);
                JOptionPane.showMessageDialog(null, message);
                LOG.error(message, ex);
            }
        }
        ftpt.connectTest();
        return false;

    }

    private boolean mussUpdaten(String datei, Long datum) {
        try {
            File f;
            for (List<String> updatefile : updatefiles) {
                if (updatefile.get(0)
                              .equals(datei)) {
                    f = new File(updatefile.get(1));
                    if (!f.exists()) {
                        return true;
                    }
                    if (f.lastModified() < datum) {
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            LOG.error("Exception: " + ex.getMessage(), ex);
        }
        return false;
    }

    /************************************************************************/
    private void updateCheck(String xupdatefile) {

        INIFile inif = new INIFile(Path.Instance.getProghome() + "ini/mandanten.ini");
        try {
            int anzahlMandanten = inif.getIntegerProperty("TheraPiMandanten", "AnzahlMandanten");
            for (int i = 0; i < anzahlMandanten; i++) {
                String[] mand = { null, null };
                mand[0] = inif.getStringProperty("TheraPiMandanten", "MAND-IK" + (i + 1));
                mand[1] = inif.getStringProperty("TheraPiMandanten", "MAND-NAME" + (i + 1));
                mandvec.add(mand);
            }

        } catch (Exception ex) {
            LOG.error("Exception: " + ex.getMessage(), ex);
        }

        try (FileReader reader = new FileReader(xupdatefile); BufferedReader in = new BufferedReader(reader)) {
            List<String> dummy = new ArrayList<>();

            String[] sourceAndTarget;
            List<Object> targetvec = new ArrayList<>();
            String zeile;
            while ((zeile = in.readLine()) != null) {
                if (!zeile.startsWith("#") && zeile.length() > 5) {
                    sourceAndTarget = zeile.split("@");
                    if (sourceAndTarget.length == 2) {
                        if (sourceAndTarget[1].contains("%proghome%")) {
                            dummy.clear();
                            dummy.add("" + sourceAndTarget[0].trim());
                            dummy.add(sourceAndTarget[1].trim()
                                                        .replace("%proghome%", Path.Instance.getProghome())
                                                        .replace("//", "/"));
                            if (!targetvec.contains(dummy.get(1))) {
                                targetvec.add(dummy.get(1));
                                updatefiles.add(new Vector<>(dummy));
                            }
                        } else if (sourceAndTarget[1].contains("%userdir%")) {
                            String home = sourceAndTarget[1].trim()
                                                            .replace("%userdir%", Path.Instance.getProghome())
                                                            .replace("//", "/");
                            for (String[] strings : mandvec) {
                                dummy.clear();
                                dummy.add("" + sourceAndTarget[0].trim());
                                dummy.add(home.replace("%mandantik%", strings[0]));
                                if (!targetvec.contains(dummy.get(1))) {
                                    updatefiles.add(new Vector<>(dummy));
                                }
                            }
                        }
                        // Ende nur dann Dateien eintragen
                    }
                }
            }
            if (!updatefiles.isEmpty()) {
                LOG.debug("Anzahl Update-Dateien = {}", updatefiles.size());
            }
        } catch (IOException e) {
            String message = "Fehler beim Bezug der Update-Informationen.\nBitte informieren Sie den Administrator umgehend";
            LOG.error(message, e);
            JOptionPane.showMessageDialog(null, message);
        }
    }
}
