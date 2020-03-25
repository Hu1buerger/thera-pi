package org.thera_pi.updates;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.INIFile;
import crypt.Verschluesseln;
import environment.Path;

public class UpdateConfig {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateConfig.class);
    private static final String NONRTASPEZIALSCHLUESSEL = "NurFuerRegistrierteUserjeLaengerJeBesserPasswortRehaVerwaltung";
    private static final String SECTION_THERA_PI_UPDATES = "TheraPiUpdates";
    private String updateDir = "";
    private String updateHost = "";
    private String updateUser = "";
    private String updatePasswd = "";
    private boolean useActiveMode = false;
    private boolean developerMode = false;

    private static UpdateConfig instance = null;

    private static String proghome = Path.Instance.getProghome();

    private UpdateConfig() {
        readIniFile();
    }

    public static UpdateConfig getInstance() {
        if (instance == null) {
            instance = new UpdateConfig();
        }
        return instance;
    }

    private void readIniFile() {

        File f = new File(proghome + "ini/tpupdateneu.ini");
        if (f.exists()) {
            INIFile ini = new INIFile(proghome + "/ini/tpupdateneu.ini");

            Verschluesseln man = Verschluesseln.getInstance(NONRTASPEZIALSCHLUESSEL);
            updateHost = man.decrypt(ini.getStringProperty(SECTION_THERA_PI_UPDATES, "UpdateFTP"));
            updateDir = man.decrypt(ini.getStringProperty(SECTION_THERA_PI_UPDATES, "UpdateVerzeichnis"));
            updateUser = man.decrypt(ini.getStringProperty(SECTION_THERA_PI_UPDATES, "UpdateUser"));
            updatePasswd = man.decrypt(ini.getStringProperty(SECTION_THERA_PI_UPDATES, "UpdatePasswd"));
            developerMode = ("1".equals(ini.getStringProperty(SECTION_THERA_PI_UPDATES, "UpdateEntwickler")));
            useActiveMode = ("1".equals(ini.getStringProperty(SECTION_THERA_PI_UPDATES, "UseFtpActiveMode")));
        } else {
            INIFile ini = new INIFile(proghome + "/ini/tpupdate.ini");
            LOG.debug(ini.getFileName());

            updateHost = ini.getStringProperty(SECTION_THERA_PI_UPDATES, "UpdateFTP");
            updateDir = ini.getStringProperty(SECTION_THERA_PI_UPDATES, "UpdateVerzeichnis");
            updateUser = ini.getStringProperty(SECTION_THERA_PI_UPDATES, "UpdateUser");

            String pw = ini.getStringProperty(SECTION_THERA_PI_UPDATES, "UpdatePasswd");
            Verschluesseln man = Verschluesseln.getInstance();

            if (pw.length() <= 20) {
                ini.setStringProperty(SECTION_THERA_PI_UPDATES, "UpdatePasswd", man.encrypt(pw), null);
                ini.save();
                updatePasswd = pw;
            } else {
                updatePasswd = man.decrypt(ini.getStringProperty(SECTION_THERA_PI_UPDATES, "UpdatePasswd"));
            }
            developerMode = ("1".equals(ini.getStringProperty(SECTION_THERA_PI_UPDATES, "UpdateEntwickler")));
            useActiveMode = ("1".equals(ini.getStringProperty(SECTION_THERA_PI_UPDATES, "UseFtpActiveMode")));
        }
    }

    String getUpdateDir() {
        return updateDir;
    }

    String getUpdateHost() {
        return updateHost;
    }

    String getUpdateUser() {
        return updateUser;
    }

    String getUpdatePasswd() {
        return updatePasswd;
    }

    boolean isUseActiveMode() {
        return useActiveMode;
    }

    boolean isDeveloperMode() {
        return developerMode;
    }
}
