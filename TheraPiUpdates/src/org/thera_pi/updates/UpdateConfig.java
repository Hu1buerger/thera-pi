package org.thera_pi.updates;

import java.io.File;

import CommonTools.INIFile;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import crypt.Verschluesseln;
import environment.Path;

public class UpdateConfig {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateConfig.class);
    private static final String NONRTASPEZIALSCHLUESSEL = "NurFuerRegistrierteUserjeLaengerJeBesserPasswortRehaVerwaltung";
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
            updateHost = man.decrypt(ini.getStringProperty("TheraPiUpdates", "UpdateFTP"));
            updateDir = man.decrypt(ini.getStringProperty("TheraPiUpdates", "UpdateVerzeichnis"));
            updateUser = man.decrypt(ini.getStringProperty("TheraPiUpdates", "UpdateUser"));
            updatePasswd = man.decrypt(ini.getStringProperty("TheraPiUpdates", "UpdatePasswd"));
            developerMode = ("1".equals(ini.getStringProperty("TheraPiUpdates", "UpdateEntwickler")));
            useActiveMode = ("1".equals(ini.getStringProperty("TheraPiUpdates", "UseFtpActiveMode")));
        } else {
            INIFile ini = new INIFile(proghome + "/ini/tpupdate.ini");
            LOG.debug(ini.getFileName());

            updateHost = ini.getStringProperty("TheraPiUpdates", "UpdateFTP");
            updateDir = ini.getStringProperty("TheraPiUpdates", "UpdateVerzeichnis");
            updateUser = ini.getStringProperty("TheraPiUpdates", "UpdateUser");

            String pw = ini.getStringProperty("TheraPiUpdates", "UpdatePasswd");
            Verschluesseln man = Verschluesseln.getInstance();

            if (pw.length() <= 20) {
                ini.setStringProperty("TheraPiUpdates", "UpdatePasswd", man.encrypt(pw), null);
                ini.save();
                updatePasswd = pw;
            } else {
                updatePasswd = man.decrypt(ini.getStringProperty("TheraPiUpdates", "UpdatePasswd"));
            }
            developerMode = ("1".equals(ini.getStringProperty("TheraPiUpdates", "UpdateEntwickler")));
            useActiveMode = ("1".equals(ini.getStringProperty("TheraPiUpdates", "UseFtpActiveMode")));
        }
    }

    public String getUpdateDir() {
        return updateDir;
    }

    public String getUpdateHost() {
        return updateHost;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public String getUpdatePasswd() {
        return updatePasswd;
    }

    public boolean isUseActiveMode() {
        return useActiveMode;
    }

    public boolean isDeveloperMode() {
        return developerMode;
    }
}
