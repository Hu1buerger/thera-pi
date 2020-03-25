package CommonTools;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class INITool {

    private static final Logger LOG = LoggerFactory.getLogger(INITool.class);

    private static List<String> iniFilesInDatabase = null;

    /**
     * private CTor to avoid creating an instance of an tooling class without
     * members and functionality.
     */
    private INITool() {
        // nothing to do here ;)
    }

    public static String[] getDBInis() {
        if (iniFilesInDatabase == null) {
            iniFilesInDatabase = new ArrayList<>();
        }
        return iniFilesInDatabase.toArray(new String[0]);
    }

    public static void init(String path) {
        INIFile file = new INIFile(path + "inicontrol.ini");
        try {
            Integer amountOfIniFilesInDatabase = file.getIntegerProperty("INIinDB", "INIAnzahl");
            if (amountOfIniFilesInDatabase == null || amountOfIniFilesInDatabase == 0) {
                iniFilesInDatabase = new ArrayList<>();
            } else {
                iniFilesInDatabase = new ArrayList<>(amountOfIniFilesInDatabase);
                for (int index = 0; index < amountOfIniFilesInDatabase; index++) {
                    iniFilesInDatabase.add(String.valueOf(file.getStringProperty("INIinDB", "DBIni" + (index + 1))));
                }
            }
        } catch (Exception ex) {
            LOG.error("Exception in init!", ex);
            iniFilesInDatabase = new ArrayList<>();
        }
    }

    /**
     * liefert INIFile Objekt mit aktuell verwandter ini, unabhaengig ob die eine
     * lokale Datei oder in der DB abgelegt ist
     *
     * @param path      Pfad zu lokaler ini
     * @param iniToOpen Name der ini-Datei
     * @return INIFile Objekt
     */
    public static INIFile openIni(String path, String iniToOpen) {
        INIFile inif = null;
        try {
            if (iniFilesInDatabase.contains(iniToOpen)) {
                String contentAsSingleString = SqlInfo.liesIniAusTabelle(iniToOpen);
                inif = new INIFile(contentAsSingleString, iniToOpen);
            } else {
                inif = new INIFile(path + iniToOpen);
            }
        } catch (Exception ex) {
            LOG.error("Exception in openIni!", ex);
        }
        return inif;
    }

    /**
     * liefert INIFile Objekt mit lokaler ini-Datei, falls die ini in der DB
     * abgelegt ist, sonst null
     *
     * @param path      Pfad zu lokaler ini
     * @param iniToOpen Name der ini-Datei
     * @return INIFile Objekt
     */
    public static INIFile openIniFallback(String path, String iniToOpen) {
        INIFile inif = null;
        try {
            if (iniFilesInDatabase.contains(iniToOpen)) {
                inif = new INIFile(path + iniToOpen);
            }
        } catch (Exception ex) {
            LOG.error("Exception in openIniFallback!", ex);
        }
        return inif;
    }

    /************************/
    public static boolean saveIni(INIFile iniToSave) {
        boolean ret = false;
        try {
            if (iniFilesInDatabase.contains(iniToSave.getFileName())) {
                SqlInfo.schreibeIniInTabelle(iniToSave.getFileName(), iniToSave.saveToString()
                                                                               .getBytes());
            } else {
                iniToSave.save();
            }
            ret = true;
        } catch (Exception ex) {
            LOG.error("Exception in saveIni!", ex);
        }
        return ret;
    }

}
