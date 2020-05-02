package systemEinstellungen;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.SqlInfo;
import CommonTools.ini.INIFile;
import CommonTools.ini.INITool;
import core.Disziplin;
import environment.Path;
import hauptFenster.Reha;

public class SystemPreislisten {

    private static final Logger logger = LoggerFactory.getLogger(SystemPreislisten.class);

    public static HashMap<String, Vector<Vector<Vector<String>>>> hmPreise = new HashMap<String, Vector<Vector<Vector<String>>>>();

    public static HashMap<String, Vector<String>> hmPreisGruppen = new HashMap<String, Vector<String>>();

    public static HashMap<String, Vector<String>> hmPreisBereich = new HashMap<String, Vector<String>>();

    public static HashMap<String, Vector<String>> hmPreisBesonderheit = new HashMap<String, Vector<String>>();

    public static HashMap<String, Vector<Integer>> hmZuzahlRegeln = new HashMap<String, Vector<Integer>>();
    public static HashMap<String, Vector<Integer>> hmZuzahlModus = new HashMap<String, Vector<Integer>>();

    public static HashMap<String, Vector<Integer>> hmHMRAbrechnung = new HashMap<String, Vector<Integer>>();

    public static HashMap<String, Vector<String>> hmNeuePreiseAb = new HashMap<String, Vector<String>>();

    public static HashMap<String, Vector<Integer>> hmNeuePreiseRegel = new HashMap<String, Vector<Integer>>();

    public static HashMap<String, Vector<Vector<String>>> hmHBRegeln = new HashMap<String, Vector<Vector<String>>>();

    public static HashMap<String, Vector<String>> hmBerichtRegeln = new HashMap<String, Vector<String>>();

    public static HashMap<String, Vector<Object>> hmFristen = new HashMap<String, Vector<Object>>();

    private static Vector<String> dummy = new Vector<String>();
    private static Vector<Integer> intdummy = new Vector<Integer>();
    private static Vector<Vector<String>> hbdummy = new Vector<Vector<String>>();
    private static Vector<String> hbdummy_1 = new Vector<String>();

    private static Vector<Integer> modusdummy = new Vector<Integer>();
    private static Vector<Object> odummy = new Vector<Object>();

    public static void ladePreise(String disziplin) {
        String aktIK = Reha.getAktIK();
        ladepreise(disziplin, aktIK);

    }

    static INIFile fristenini = null;

    public static void ladepreise(String disziplin, String aktIK) {

        INIFile fristeninilocal = null;

        INIFile inif = null;
        if (Disziplin.ofMedium(disziplin) == Disziplin.INV) {
            return;
        }
        int tarife = -1;
        try {
            inif = INITool.openIni(Path.Instance.getProghome() + "ini" + File.separator + aktIK + File.separator, "preisgruppen.ini");
            tarife = inif.getIntegerProperty("PreisGruppen_" + disziplin, "AnzahlPreisGruppen");
            fristeninilocal = INITool.openIni(Path.Instance.getProghome() + "ini" + File.separator + aktIK + File.separator, "fristen.ini");
            fristenini = fristeninilocal;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Problems retrieving Preisgruppen and/or Fristen");
        }

        ladepreise(disziplin, fristeninilocal, inif, tarife);

    }

    private static void ladepreise(String disziplin, INIFile fristeninilocal, INIFile inif, int tarife) {

        Disziplin diszi = Disziplin.ofMedium(disziplin);

        switch (diszi) {
        case KG: {

            Vector<Vector<Vector<String>>> preise = new Vector<Vector<Vector<String>>>();
            for (int i = 0; i < tarife; i++) {
                Vector<Vector<String>> preisliste = SqlInfo.holeFelder(
                        "select * from kgtarif" + Integer.toString(i + 1) + " order by LEISTUNG");

                preise.add((Vector<Vector<String>>) preisliste);
            }

            hmPreise.put(diszi.medium, preise);
            ladepreise(fristeninilocal, inif, tarife, diszi.medium);
        }
            break;
        case MA: {
            Vector<Vector<Vector<String>>> preise = new Vector<Vector<Vector<String>>>();
            for (int i = 0; i < tarife; i++) {
                Vector<Vector<String>> preisliste = SqlInfo.holeFelder(
                        "select * from matarif" + Integer.toString(i + 1) + " order by LEISTUNG");

                preise.add((Vector<Vector<String>>) preisliste);
            }

            hmPreise.put(diszi.medium, preise);
            ladepreise(fristeninilocal, inif, tarife, diszi.medium);
        }
            break;
        case ER: {
            Vector<Vector<Vector<String>>> preise = new Vector<Vector<Vector<String>>>();
            for (int i = 0; i < tarife; i++) {
                Vector<Vector<String>> preisliste = SqlInfo.holeFelder(
                        "select * from ertarif" + Integer.toString(i + 1) + " order by LEISTUNG");

                preise.add((Vector<Vector<String>>) preisliste);
            }

            hmPreise.put(diszi.medium, preise);
            ladepreise(fristeninilocal, inif, tarife, diszi.medium);
        }
            break;
        case LO: {
            Vector<Vector<Vector<String>>> preise = new Vector<Vector<Vector<String>>>();
            for (int i = 0; i < tarife; i++) {
                Vector<Vector<String>> preisliste = SqlInfo.holeFelder(
                        "select * from lotarif" + Integer.toString(i + 1) + " order by LEISTUNG");

                preise.add((Vector<Vector<String>>) preisliste);
            }

            hmPreise.put(diszi.medium, preise);
            ladepreise(fristeninilocal, inif, tarife, diszi.medium);
        }
            break;
        case RH: {
            Vector<Vector<Vector<String>>> preise = new Vector<Vector<Vector<String>>>();
            for (int i = 0; i < tarife; i++) {
                Vector<Vector<String>> preisliste = SqlInfo.holeFelder(
                        "select * from rhtarif" + Integer.toString(i + 1) + " order by LEISTUNG");

                preise.add((Vector<Vector<String>>) preisliste);
            }

            hmPreise.put(diszi.medium, preise);
            ladepreise(fristeninilocal, inif, tarife, diszi.medium);
        }
            break;
        case PO: {
            Vector<Vector<Vector<String>>> preise = new Vector<Vector<Vector<String>>>();
            for (int i = 0; i < tarife; i++) {
                Vector<Vector<String>> preisliste = SqlInfo.holeFelder(
                        "select * from potarif" + Integer.toString(i + 1) + " order by LEISTUNG");

                preise.add((Vector<Vector<String>>) preisliste);
            }

            hmPreise.put(diszi.medium, preise);
            ladepreise(fristeninilocal, inif, tarife, diszi.medium);
        }
            break;
        case RS: {
            try {
                Vector<Vector<Vector<String>>> preise = new Vector<Vector<Vector<String>>>();

                for (int i = 0; i < tarife; i++) {
                    Vector<Vector<String>> preisliste = SqlInfo.holeFelder(
                            "select * from rstarif" + Integer.toString(i + 1) + " order by LEISTUNG");

                    preise.add((Vector<Vector<String>>) preisliste);
                }

                hmPreise.put(diszi.medium, preise);
                ladepreise(fristeninilocal, inif, tarife, diszi.medium);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
            break;
        case FT: {
            try {
                Vector<Vector<Vector<String>>> preise = new Vector<Vector<Vector<String>>>();

                for (int i = 0; i < tarife; i++) {
                    Vector<Vector<String>> preisliste = SqlInfo.holeFelder(
                            "select * from fttarif" + Integer.toString(i + 1) + " order by LEISTUNG");

                    preise.add((Vector<Vector<String>>) preisliste);
                }

                hmPreise.put(diszi.medium, preise);
                ladepreise(fristeninilocal, inif, tarife, diszi.medium);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
            break;
        case COMMON: {
            dummy.clear();
            getPreisGruppen(inif, "Common", tarife);
            hmPreisGruppen.put("Common", (Vector<String>) dummy.clone());
            dummy.clear();
        }
            break;
        }
    }

    private static void ladepreise(INIFile fristeninilocal, INIFile inif, int tarife, String key) {
        dummy.clear();
        getPreisGruppen(inif, key, tarife);
        hmPreisGruppen.put(key, (Vector<String>) dummy.clone());
        dummy.clear();
        getPreisBereich(inif, key, tarife);
        hmPreisBereich.put(key, (Vector<String>) dummy.clone());
        dummy.clear();
        getPreisBesonderheit(inif, key, tarife);
        hmPreisBesonderheit.put(key, (Vector<String>) dummy.clone());
        dummy.clear();
        intdummy.clear();
        getNeuePreiseRegeln(inif, key, tarife);
        hmNeuePreiseRegel.put(key, (Vector<Integer>) intdummy.clone());
        intdummy.clear();
        getNeuePreiseAb(inif, key, tarife);
        hmNeuePreiseAb.put(key, (Vector<String>) dummy.clone());
        dummy.clear();
        intdummy.clear();
        getZuzahlRegeln(inif, key, tarife);
        hmZuzahlRegeln.put(key, (Vector<Integer>) intdummy.clone());
        hmZuzahlModus.put(key, (Vector<Integer>) modusdummy.clone());
        intdummy.clear();
        modusdummy.clear();
        getHMRAbrechnung(inif, key, tarife);
        hmHMRAbrechnung.put(key, (Vector<Integer>) intdummy.clone());
        intdummy.clear();
        hbdummy.clear();
        getHBRegeln(inif, key, tarife);
        hmHBRegeln.put(key, (Vector<Vector<String>>) hbdummy.clone());
        hbdummy.clear();
        dummy.clear();
        getBerichtRegeln(inif, key, tarife);
        hmBerichtRegeln.put(key, (Vector<String>) dummy.clone());
        dummy.clear();
        doFristen(key, tarife, fristeninilocal);
    }

    /**********
     *
     *
     * @param f
     * @param disziplin
     * @param tarife
     * @param dummy
     * @return
     */
    public static void getPreisGruppen(INIFile f, String disziplin, int tarife) {
        for (int i = 0; i < tarife; i++) {
            dummy.add(f.getStringProperty("PreisGruppen_" + disziplin, "PGName" + Integer.toString(i + 1)));
        }
    }

    public static void getPreisBereich(INIFile f, String disziplin, int tarife) {
        for (int i = 0; i < tarife; i++) {
            dummy.add(f.getStringProperty("PreisGruppen_" + disziplin, "PGBereich" + Integer.toString(i + 1)));
        }
    }

    public static void getPreisBesonderheit(INIFile f, String disziplin, int tarife) {
        boolean mustsave = false;
        for (int i = 0; i < tarife; i++) {
            if ((f.getStringProperty("PreisGruppen_" + disziplin,
                    "PGBesonderheit" + Integer.toString(i + 1))) == null) {
                f.setStringProperty("PreisGruppen_" + disziplin, "PGBesonderheit" + Integer.toString(i + 1), "000",
                        null);
                mustsave = true;
            }
            dummy.add(f.getStringProperty("PreisGruppen_" + disziplin, "PGBesonderheit" + Integer.toString(i + 1)));
        }
        if (mustsave) {
            INITool.saveIni(f);
        }
    }

    public static void getZuzahlRegeln(INIFile f, String disziplin, int tarife) {
        boolean mustsave = false;
        String sdummy;
        for (int i = 0; i < tarife; i++) {
            intdummy.add(f.getIntegerProperty("ZuzahlRegeln_" + disziplin, "ZuzahlRegel" + Integer.toString(i + 1)));
            if ((sdummy = f.getStringProperty("ZuzahlRegeln_" + disziplin,
                    "ZuzahlModus" + Integer.toString(i + 1))) == null) {
                f.setStringProperty("ZuzahlRegeln_" + disziplin, "ZuzahlModus" + Integer.toString(i + 1), "1", null);
                mustsave = true;
                sdummy = "1";
            }
            modusdummy.add(Integer.parseInt(sdummy));
        }
        if (mustsave) {
            INITool.saveIni(f);
        }
    }

    public static void getHMRAbrechnung(INIFile f, String disziplin, int tarife) { // $302-Abrechnung Ja/nein
        for (int i = 0; i < tarife; i++) {
            intdummy.add(f.getIntegerProperty("HMRAbrechnung_" + disziplin, "HMRAbrechnung" + Integer.toString(i + 1)));
        }
    }

    public static void getHBRegeln(INIFile f, String disziplin, int tarife) {
        for (int i = 0; i < tarife; i++) {
            hbdummy_1.clear();
            hbdummy_1.add(f.getStringProperty("HBRegeln_" + disziplin, "HBPosVoll" + (i + 1)));
            hbdummy_1.add(f.getStringProperty("HBRegeln_" + disziplin, "HBPosMit" + (i + 1)));
            hbdummy_1.add(f.getStringProperty("HBRegeln_" + disziplin, "HBKilometer" + (i + 1)));
            hbdummy_1.add(f.getStringProperty("HBRegeln_" + disziplin, "HBPauschal" + (i + 1)));
            hbdummy_1.add(f.getStringProperty("HBRegeln_" + disziplin, "HBHeimMitZuZahl" + (i + 1)));
            hbdummy.add((Vector<String>) hbdummy_1.clone());
        }
        hbdummy_1.clear();
    }

    public static void getNeuePreiseRegeln(INIFile f, String disziplin, int tarife) {
        for (int i = 0; i < tarife; i++) {
            intdummy.add(f.getIntegerProperty("PreisRegeln_" + disziplin, "PreisRegel" + Integer.toString(i + 1)));
        }
    }

    public static void getNeuePreiseAb(INIFile f, String disziplin, int tarife) {
        for (int i = 0; i < tarife; i++) {
            dummy.add(f.getStringProperty("PreisRegeln_" + disziplin, "PreisAb" + Integer.toString(i + 1)));
        }
    }

    public static void getBerichtRegeln(INIFile f, String disziplin, int tarife) {
        for (int i = 0; i < tarife; i++) {
            dummy.add(f.getStringProperty("BerichtRegeln_" + disziplin, "Bericht" + Integer.toString(i + 1)));
        }
    }

    public static void loescheHashMaps() {
        hmPreise.clear();
        hmPreisGruppen.clear();
        hmPreisBereich.clear();
        hmPreisBesonderheit.clear();
        hmZuzahlRegeln.clear();
        hmHMRAbrechnung.clear();
        hmNeuePreiseAb.clear();
        hmNeuePreiseRegel.clear();
        hmHBRegeln.clear();
        hmBerichtRegeln.clear();
        hmFristen.clear();

        dummy.clear();
        intdummy.clear();
        hbdummy.clear();
        hbdummy_1.clear();
        hmFristen.clear();
        odummy.clear();

        hmPreisGruppen.clear();
        hmPreisBereich.clear();
        hmZuzahlRegeln.clear();
        hmHMRAbrechnung.clear();
        hmNeuePreiseAb.clear();
        hmNeuePreiseRegel.clear();
        hmHBRegeln.clear();
        hmBerichtRegeln.clear();

        dummy.trimToSize();
        intdummy.trimToSize();
        hbdummy.trimToSize();
        hbdummy_1.trimToSize();

    }

    class Sortiere {
        Vector<Vector<String>> vector = null;

        public Sortiere(Vector<Vector<String>> vec) {
            this.vector = vec;
        }

        public Vector<Vector<String>> sortieren() {
            Comparator<Vector> comparator = new Comparator<Vector>() {

                @Override
                public int compare(Vector o1, Vector o2) {
                    String s1 = o1.get(0)
                                  .toString();
                    String s2 = o2.get(0)
                                  .toString();
                    return s1.compareTo(s2);
                }
            };

            Collections.sort((Vector) this.vector, comparator);
            return this.vector;
        }
    }

    /**
     * @param fristenini TODO
     *******************************/
    private static void doFristen(String disziplin, int anzahl, INIFile fristenini) {
        odummy.clear();
        Vector<Object> xdummy = new Vector<Object>();
        for (int i = 0; i < anzahl; i++) {
            xdummy.add(
                    fristenTesten("Fristen_" + disziplin, "FristBeginn" + Integer.toString(i + 1), true, fristenini));
            // xdummy.add(fristenini.getIntegerProperty("Fristen_"+disziplin,
            // "FristBeginn"+Integer.toString(i+1)));
        }
        odummy.add(xdummy.clone());
        xdummy.clear();
        for (int i = 0; i < anzahl; i++) {
            // xdummy.add( (fristenini.getIntegerProperty("Fristen_"+disziplin,
            // "BeginnKalendertage"+Integer.toString(i+1))==1 ? true : false));
            xdummy.add(fristenTesten("Fristen_" + disziplin, "BeginnKalendertage" + Integer.toString(i + 1), false,
                    fristenini));
        }
        odummy.add(xdummy.clone());
        xdummy.clear();
        for (int i = 0; i < anzahl; i++) {
            xdummy.add(fristenTesten("Fristen_" + disziplin, "FristUnterbrechung" + Integer.toString(i + 1), true,
                    fristenini));
            // xdummy.add(fristenini.getIntegerProperty("Fristen_"+disziplin,
            // "FristUnterbrechung"+Integer.toString(i+1)));
        }
        odummy.add(xdummy.clone());
        xdummy.clear();
        for (int i = 0; i < anzahl; i++) {
            xdummy.add(fristenTesten("Fristen_" + disziplin, "UnterbrechungKalendertage" + Integer.toString(i + 1),
                    false, fristenini));
            // xdummy.add( (fristenini.getIntegerProperty("Fristen_"+disziplin,
            // "UnterbrechungKalendertage"+Integer.toString(i+1))==1 ? true : false));
        }
        odummy.add(xdummy.clone());
        xdummy.clear();
        for (int i = 0; i < anzahl; i++) {
            xdummy.add(fristenTesten("Fristen_" + disziplin, "BeginnMitSamstag" + Integer.toString(i + 1), false,
                    fristenini));
            // xdummy.add( (fristenini.getIntegerProperty("Fristen_"+disziplin,
            // "UnterbrechungKalendertage"+Integer.toString(i+1))==1 ? true : false));
        }
        odummy.add(xdummy.clone());
        xdummy.clear();
        for (int i = 0; i < anzahl; i++) {
            xdummy.add(fristenTesten("Fristen_" + disziplin, "UnterbrechungMitSamstag" + Integer.toString(i + 1), false,
                    fristenini));
            // xdummy.add( (fristenini.getIntegerProperty("Fristen_"+disziplin,
            // "UnterbrechungKalendertage"+Integer.toString(i+1))==1 ? true : false));
        }
        odummy.add(xdummy.clone());
        xdummy.clear();

        hmFristen.put(disziplin, (Vector<Object>) odummy.clone());
        odummy.clear();
        // System.out.println(hmFristen);
    }

    /***
     *
     *
     * @param kategorie
     * @param item
     * @param retint
     * @param fristenini TODO
     * @return
     */
    private static Object fristenTesten(String kategorie, String item, boolean retint, INIFile fristenini) {
        INIFile fristenini2 = fristenini;
        if (fristenini2.getIntegerProperty(kategorie, item) == null) {
            System.out.println(
                    "SytemPreislisten: erstelle Parameter für Kategorie=" + kategorie + " Preisgruppe=" + item);
            if (retint) {
                fristenini2.setIntegerProperty(kategorie, item, 14, null);
            } else {
                fristenini2.setIntegerProperty(kategorie, item, 1, null);
            }
            INITool.saveIni(fristenini2);
        }
        if (retint) {
            return fristenini2.getIntegerProperty(kategorie, item);
        } else {
            return (fristenini2.getIntegerProperty(kategorie, item) == 1 ? true : false);
        }
    }

    /*********************************/
}
