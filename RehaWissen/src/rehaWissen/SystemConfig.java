package rehaWissen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import CommonTools.ini.INIFile;
import environment.Path;

public class SystemConfig {
    public static java.net.InetAddress dieseMaschine = null;
    public static String wissenURL = null;
    public static String homeDir = null;
    public static String homePageURL = null;
    public static int TerminUeberlappung = 1;

    public static Vector<ArrayList<String>> InetSeiten = null;
    public static String HilfeServer = null;
    public static boolean HilfeServerIstDatenServer;
    public static HashMap<String, String> hmHilfeServer;
    
    public SystemConfig() {

    }

    public static void InetSeitenEinlesen() {
        INIFile inif = new INIFile( Path.Instance.getProghome()+ "ini/rehabrowser.ini");
        int seitenanzahl = inif.getIntegerProperty("RehaBrowser", "SeitenAnzahl");
        InetSeiten = new Vector<ArrayList<String>>();
        ArrayList<String> seite = null;
        for (int i = 0; i < seitenanzahl; i++) {
            seite = new ArrayList<String>();
            seite.add(inif.getStringProperty("RehaBrowser", "SeitenName" + (i + 1)));
            seite.add(inif.getStringProperty("RehaBrowser", "SeitenIcon" + (i + 1)));
            seite.add(inif.getStringProperty("RehaBrowser", "SeitenAdresse" + (i + 1)));
            InetSeiten.add(seite);
        }
        HilfeServer = inif.getStringProperty("TheraPiHilfe", "HilfeServer");
        HilfeServerIstDatenServer = (inif.getIntegerProperty("TheraPiHilfe", "HilfeDBIstDatenDB") > 0 ? true : false);

    }

}

/*****************************************/
