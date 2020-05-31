package opRgaf;

import java.io.File;
import java.util.HashMap;

import org.jdesktop.swingworker.SwingWorker;

import CommonTools.OpCommon;
import CommonTools.ini.INIFile;
import CommonTools.ini.INITool;

public class OpRgAfIni {
    private HashMap<String, Object>  mahnParam = new HashMap<>();

    /** INIFile inif;. */
    private String path2IniFile;
    private String path2TemplateFiles;
    private String iniFile;
    private boolean incRG, incAR, incVK, settingsLocked;
    private int vorauswahlSuchkriterium = -1;
    private String progHome, aktIK;
    private boolean allowCashInSalesReceipt, allowCashSales, iniValuesValid;

    /**
     * Liest Einträge aus 'oprgaf.ini'
     *
     * @param installationspfad, ini-verzeichnis, ik, filename
     */
    // args[0],"ini/",args[1],"oprgaf.ini"
    public OpRgAfIni(String home, String subPath, String IK, String file) {
        progHome = home;
        aktIK = IK;
        this.path2IniFile = progHome + subPath + aktIK + "/";
        this.path2TemplateFiles = progHome + "vorlagen/" + aktIK;
        INITool.init(path2IniFile);
        this.iniFile = file;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String section = "offenePosten";
                INIFile inif = INITool.openIni(path2IniFile, iniFile);
                if (inif.getStringProperty(section, "lockSettings") != null) {
                    settingsLocked = inif.getBooleanProperty(section, "lockSettings");
                }

                readLastSelectRgAfVk(inif);
                OpCommon.readMahnParamCommon(inif, mahnParam);
                readMahnParamRgAfVk(inif, mahnParam, path2TemplateFiles);

                readCashSettings(inif);

                iniValuesValid = true;
                File f = new File(progHome + subPath + aktIK + "oprgaf.ini");
                if (f.exists()) {
                    f.delete();
                }
                return null;
            }
        }.execute();
    }

    /**
     * Liest die zuletzt verwandten (Checkbox-)Einstellungen aus der oprgaf.ini ist
     * keine Einstellung vorhanden, werden hier die Defaults gesetzt
     */
    private void readLastSelectRgAfVk(INIFile inif) {
        String section = "offenePosten";
        if (inif.getStringProperty(section, "Rezeptgebuehren") != null) { // Eintraege in ini vorhanden
                                                                          //  (alle oder keiner)
            incRG = inif.getBooleanProperty(section, "Rezeptgebuehren");
            incAR = inif.getBooleanProperty(section, "Ausfallrechnungen");
            incVK = inif.getBooleanProperty(section, "Verkaeufe");
            vorauswahlSuchkriterium = inif.getIntegerProperty(section, "Suchkriterium");
        } else {
            incRG = true;
            incAR = true;
            incVK = false;
            vorauswahlSuchkriterium = 0;
        }
    }

    /**
     * Liest RgAfVk-spezif. Mahnparameter aus der ini-Datei ist keine Einstellung
     * vorhanden, werden hier die Defaults gesetzt
     */
    private void readMahnParamRgAfVk(INIFile inif, HashMap<String, Object> mahnParam, String path2Templates) {
        if (inif.getStringProperty("General", "DirAlteRechnungen") != null) {
            mahnParam.put("diralterechnungen", (String) inif.getStringProperty("General", "DirAlteRechnungen"));
        } else {
            mahnParam.put("diralterechnungen", (String) progHome + "rechnung/");
        }
        if (inif.getStringProperty("General", "WohinBuchen") != null) {
            mahnParam.put("inkasse", (String) inif.getStringProperty("General", "WohinBuchen"));
        } else {
            mahnParam.put("inkasse", (String) "Bank");
        }
        for (int i = 1; i <= 4; i++) {
            OpCommon.addFormNb(inif, "General", "FormularMahnung", "RGAFMahnung", i, mahnParam, path2Templates);
        }
    }

    /**
     * Liest BarzahlungsEinstellungen aus der ini-Datei ist keine Einstellung
     * vorhanden, werden hier die Defaults gesetzt.
     */
    private void readCashSettings(INIFile inif) {
        String section = "offenePosten";
        if (inif.getStringProperty(section, "erlaubeVRinBarkasse") != null) {
            allowCashInSalesReceipt = inif.getBooleanProperty(section, "erlaubeVRinBarkasse");
            allowCashSales = inif.getBooleanProperty(section, "erlaubeVBoninBarkasse");
        } else {
            allowCashInSalesReceipt = false;
            allowCashSales = false;
            initCashSettings();
        }
    }

    private boolean valuesValid() {
        int waitTimes = 20;
        while (!iniValuesValid && waitTimes-- > 1) { // lesen aus ini ist noch nicht fertig...
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        if (waitTimes == 0) {
            System.out.println("OpRgaf: Abbruch ini-read");
            return false;
        } else {
            return true;
        }
    }

    public void setIncRG(boolean value) {
        incRG = value;
    }

    public boolean getIncRG() {
        valuesValid();
        return incRG;
    }

    public void setIncAR(boolean value) {
        incAR = value;
    }

    public boolean getIncAR() {
        valuesValid();
        return incAR;
    }

    public void setIncVK(boolean value) {
        incVK = value;
    }

    public boolean getIncVK() {
        valuesValid();
        return incVK;
    }

    public void setVorauswahl(int value) {
        vorauswahlSuchkriterium = value;
    }

    int getVorauswahl(int max) {
        if (valuesValid()) {
            return vorauswahlSuchkriterium < max ? vorauswahlSuchkriterium : 0;
        } else {
            // System.out.println("OpRgaf getVorauswahl: " + 0 +"(Abbruch ini-read)");
            return 0;
        }
    }

    public HashMap<String, Object> getMahnParameter() {
        valuesValid();
        return mahnParam;
    }

    public String getWohinBuchen() {
        valuesValid();
        return (String) mahnParam.get("inkasse");
    }

    String getFormNb(int lfdNb) {
        valuesValid();
        return (String) mahnParam.get("formular" + lfdNb);
    }

    int getFrist(int lfdNb) {
        valuesValid();
        return (Integer) mahnParam.get("frist" + lfdNb);
    }

    public String getDrucker() {
        valuesValid();
        return (String) mahnParam.get("drucker");
    }

    public void setVrCashAllowed(boolean value) {
        valuesValid();
        allowCashInSalesReceipt = value;
    }

    public boolean getVrCashAllowed() {
        valuesValid();
        return allowCashInSalesReceipt;
    }

    public void setVbCashAllowed(boolean value) {
        valuesValid();
        allowCashSales = value;
    }

    public boolean getVbCashAllowed() {
        valuesValid();
        return allowCashSales;
    }

    public void setSettingsLocked(boolean value) {
        settingsLocked = value;
    }

    public boolean getSettingsLocked() {
        return settingsLocked;
    }

    /**
     * Schreibt die zuletzt verwandten OpRgAf-Einstellungen (falls geändert) in die
     * oprgaf.ini
     */
    void saveLastSelection() {
        INIFile inif = INITool.openIni(path2IniFile, iniFile);
        String section = "offenePosten";
        if (!settingsLocked && (incRG != inif.getBooleanProperty(section, "Rezeptgebuehren")
                || incAR != inif.getBooleanProperty(section, "Ausfallrechnungen")
                || incVK != inif.getBooleanProperty(section, "Verkaeufe")
                || vorauswahlSuchkriterium != inif.getIntegerProperty(section, "Suchkriterium"))) {
            inif.setBooleanProperty(section, "Rezeptgebuehren", incRG, "offenePosten RgAfVk beruecksichtigt");
            inif.setBooleanProperty(section, "Ausfallrechnungen", incAR, null);
            inif.setBooleanProperty(section, "Verkaeufe", incVK, null);

            inif.setIntegerProperty(section, "Suchkriterium", vorauswahlSuchkriterium, "zuletzt gesucht");

            if (inif.getStringProperty(section, "lockSettings") == null) { // Wert noch nicht vorhanden?
        inif.setBooleanProperty(section, "lockSettings", false, "Aktualisieren der Eintraege gesperrt");
            }
            INITool.saveIni(inif);
         }
    }

    /**
     * Legt die Lock- und BarzahlungsEinstellungen in der oprgaf.ini mit
     * Defaultwerten an
     */
    private void initCashSettings() {
        INIFile inif = INITool.openIni(path2IniFile, iniFile);
        String section = "offenePosten";
        boolean saveChanges = false;
        if (inif.getStringProperty(section, "lockSettings") == null) {
            settingsLocked = true;
            inif.setBooleanProperty(section, "lockSettings", settingsLocked, "Aktualisieren der Eintraege gesperrt");
            saveChanges = true;
        }
        if (inif.getStringProperty(section, "erlaubeVRinBarkasse") == null) {
            writeSalesRhg(inif);
            saveChanges = true;
        }
        if (inif.getStringProperty(section, "erlaubeVBoninBarkasse") == null) {
            writeSalesBon(inif);
            saveChanges = true;
        }
        if (saveChanges) {
            INITool.saveIni(inif);
        }
    }

    /**
     * Schreibt die zuletzt verwandten BarzahlungsEinstellungen (falls geändert) in
     * die oprgaf.ini
     */
    public boolean saveLastCashSettings() {
        INIFile inif = INITool.openIni(path2IniFile, iniFile);
        String section = "offenePosten";
        boolean saveChanges = false;
        if (!settingsLocked) { // ini-Einträge dürfen aktualisiert werden
            if (allowCashInSalesReceipt != inif.getBooleanProperty(section, "erlaubeVRinBarkasse")) {
                writeSalesRhg(inif);
                saveChanges = true;
            }
            if (allowCashSales != inif.getBooleanProperty(section, "erlaubeVBoninBarkasse")) {
                writeSalesBon(inif);
                saveChanges = true;
            }
            if (saveChanges) {
                INITool.saveIni(inif);
                return true;
            }
        }
        return false;
    }

    /**
     * Schreibt die zuletzt verwandten Lock-Einstellungen (falls geändert) in die
     * oprgaf.ini
     */
    public boolean saveLockSettings() {
        INIFile inif = INITool.openIni(path2IniFile, iniFile);
        String section = "offenePosten";
        if (settingsLocked != inif.getBooleanProperty(section, "lockSettings")) {
            inif.setBooleanProperty(section, "lockSettings", settingsLocked, "Aktualisieren der Eintraege gesperrt");
            INITool.saveIni(inif);
            return true;
        }
        return false;
    }

    private void writeSalesBon(INIFile inif) {
        inif.setBooleanProperty("offenePosten", "erlaubeVBoninBarkasse", allowCashSales,
                "Barverkaeufe (Bondruck) duerfen in Barkasse gebucht werden");
    }

    private void writeSalesRhg(INIFile inif) {
        inif.setBooleanProperty("offenePosten", "erlaubeVRinBarkasse", allowCashInSalesReceipt,
                "Verkaufsrechnungen duerfen in Barkasse gebucht werden");
    }
}
