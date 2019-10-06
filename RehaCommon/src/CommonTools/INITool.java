package CommonTools;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

public class INITool {
    static String[] dbInis = null;

    public static void setDBInis(String[] xdbini) {
        dbInis = xdbini;
    }

    public static String[] getDBInis() {
        return dbInis;
    }

    public static void init(String pfad) {
        INIFile file = new INIFile(pfad + "inicontrol.ini");
        try {
            int anzahl = file.getIntegerProperty("INIinDB", "INIAnzahl");
            if (anzahl == 0) {
                dbInis = new String[] { "nix" };
            } else {
                dbInis = new String[anzahl];
                for (int i = 0; i < dbInis.length; i++) {
                    dbInis[i] = String.valueOf(file.getStringProperty("INIinDB", "DBIni" + Integer.toString(i + 1)));
                }
            }
        } catch (Exception ex) {
            dbInis = new String[] { "nix" };
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
            if (Arrays.asList(dbInis)
                      .contains(iniToOpen)) {
                InputStream stream = SqlInfo.liesIniAusTabelle(iniToOpen);
                inif = new INIFile(stream, iniToOpen);
            } else {
                inif = new INIFile(path + iniToOpen);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
            if (Arrays.asList(dbInis)
                      .contains(iniToOpen)) {
                inif = new INIFile(path + iniToOpen);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return inif;
    }

    /************************/
    public static boolean saveIni(INIFile iniToSave) {
        boolean ret = false;
        try {
            if (Arrays.asList(dbInis)
                      .contains(iniToSave.getFileName())) {
                SqlInfo.schreibeIniInTabelle(iniToSave.getFileName(), iniToSave.saveToStringBuffer()
                                                                               .toString()
                                                                               .getBytes());
                iniToSave.getInputStream()
                         .close();
                iniToSave = null;
            } else {
                iniToSave.save();
            }
            ret = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

}
