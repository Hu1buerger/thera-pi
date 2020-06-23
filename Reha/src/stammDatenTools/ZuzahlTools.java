package stammDatenTools;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.therapi.reha.patient.RezepteAktuell;

import CommonTools.DatFunk;
import CommonTools.ExUndHop;
import CommonTools.SqlInfo;
import hauptFenster.Reha;
import rezept.Zuzahlung;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;

public class ZuzahlTools {
    private static final Logger logger = LoggerFactory.getLogger(ZuzahlTools.class);
    /**
     * TODO: EnumMap anlegen, mit ZuZahlStatus als key, Icon als value 
     * **/
    public enum ZZStat {
        ZUZAHLFREI,
        ZUZAHLOK,
        ZUZAHLNICHTOK,
        ZUZAHLRGR,      // Rez is in rgaffaktura
        ZUZAHLNOTSET
    }

    static EnumMap<ZZStat, ImageIcon> zzIcon = new EnumMap<ZZStat, ImageIcon>(ZZStat.class); // ersetzt das imgzuzahl[]
                                                                                             // Array

    public static boolean zzStatusEdit(String pat_int, String geboren, String rez_nr, String frei, String kassid) {
        // String preisgrp = "";
        String zzid = "";
        String rez_geb = "";
        int zzregel = -1;
        if (kassid.equals("-1")) {
            JOptionPane.showMessageDialog(null, "Keine g\u00fcltige Kasse angegeben");
            return false;
        }
        if (rez_nr.equals("")) {
            Vector<Vector<String>> vec = SqlInfo.holeFelder(
                    "select rez_nr,kid,rez_geb,befr,id from verordn where pat_intern='" + pat_int + "'");
            for (int i = 0; i < vec.size(); i++) {
                zzregel = getZuzahlRegel((String) ((Vector<?>) vec.get(i)).get(1));
                zzid = (String) ((Vector<?>) vec.get(i)).get(4);
                rez_geb = (String) ((Vector<?>) vec.get(i)).get(2);
                // System.out.println("Rezeptnummer = "+((Vector)vec.get(i)).get(0)+"
                // Zuzahlregel = "+zzregel);
                if (zzregel > 0 && rez_geb.equals("0.00")) {
                    if (frei.equals("F")) {
                        SqlInfo.aktualisiereSaetze("verordn", "befr='F',zzstatus='2'", "id='" + zzid + "'");
                    } else {
                        SqlInfo.aktualisiereSaetze("verordn", "befr='T',zzstatus='0'", "id='" + zzid + "'");
                    }
                } else if (zzregel > 0 && (!rez_geb.equals("0.00"))) {
                    if (frei.equals("F")) {
                        SqlInfo.aktualisiereSaetze("verordn", "befr='F',zzstatus='1'", "id='" + zzid + "'");
                    }
                }
            }
            return true;
        } else {
            // Wenn Rezeptnummer nicht leer, dann kommt der Aufruf aus Rezept!!!
        }
        return false;
    }

    /**********************************************************/

    public static Object[] unter18TestDirekt(Vector<String> termine, boolean azTest, boolean jahrTest) {
        // Rez geb f\ufffdllig //Anzahl Term //Anzahl frei //Anzahl unfrei //Zuzahlstatus
        Object[] ret = { new Boolean(false), Integer.valueOf(-1), Integer.valueOf(-1), Integer.valueOf(-1),
                Integer.valueOf(-1) };
        // Vector vec = SqlInfo.holeFelder("select
        // termine,id,pat_intern,jahrfrei,unter18,zzregel,zzstatus from verordn where
        // rez_nr='"+rez_nr+"' LIMIT 1");
        Vector<String> tage = (Vector<String>) termine.clone();
        if (tage.size() == 0) {
            return ret;
        }

        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                String strings1 = DatFunk.sDatInSQL(s1);
                String strings2 = DatFunk.sDatInSQL(s2);
                return strings1.compareTo(strings2);
            }
        };
        Collections.sort(tage, comparator);
        // TODO: delete me once Rezepte have been sorted
        String rez_nr = Reha.instance.patpanel.vecaktrez.get(1);
        logger.debug("Vec: rez_nr=" + rez_nr);
        rez_nr = Reha.instance.patpanel.rezAktRez.getRezNr();
        logger.debug("Rez: rez_nr=" + rez_nr);
        String sUnter18 = Reha.instance.patpanel.vecaktrez.get(60);
        logger.debug("Vec: sUnter18=" + sUnter18);
        boolean unter18 = Reha.instance.patpanel.rezAktRez.isUnter18();
        logger.debug("Rez: unter18=" + unter18);
        // String pat_int = (String) Reha.instance.patpanel.vecaktrez.get(0);
        String sAktzzstatus = Reha.instance.patpanel.vecaktrez.get(39);
        logger.debug("Vec: aktzzstatus=" + sAktzzstatus);
        int aktzzstatus = Reha.instance.patpanel.rezAktRez.getZZStatus();
        logger.debug("Rez: aktzzstatus=" + aktzzstatus);
        String sAktzzregel = Reha.instance.patpanel.vecaktrez.get(63);
        logger.debug("Vec: aktzzregel=" + sAktzzregel);
        int aktzzregel = Reha.instance.patpanel.rezAktRez.getZZRegel();
        logger.debug("Rez: aktzzregel=" + aktzzregel);
        if (unter18 && (aktzzregel == 0)) {
            String stichtag = "";
            String geburtstag = DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4));
            String gebtag = (DatFunk.sDatInDeutsch(Reha.instance.patpanel.vecaktrez.get(22))).substring(0, 6)
                    + Integer.valueOf(Integer.valueOf(SystemConfig.aktJahr) - 18)
                             .toString();
            logger.debug("Vec: gebtag=" + gebtag);
            gebtag = Reha.instance.patpanel.rezAktRez.getRezDatum().format(DateTimeFormatter.ofPattern("dd.MM."))
                    + Integer.valueOf(Integer.valueOf(SystemConfig.aktJahr) - 18)
                                                                                 .toString();
            logger.debug("Rez: gebtag=" + gebtag);

            boolean einergroesser = false;
            int erstergroesser = -1;
            for (int i = 0; i < tage.size(); i++) {
                stichtag = tage.get(i)
                               .substring(0, 6)
                        + Integer.valueOf(Integer.valueOf(SystemConfig.aktJahr) - 18)
                                 .toString();
                if (DatFunk.TageDifferenz(geburtstag, stichtag) >= 0) {
                    einergroesser = true;
                    break;
                }
                //// System.out.println("Differenz an Tagen zwischen Behandlung vom
                //// "+tage.get(i)+
                // " und dem Geburtstag "+geburtstag+" = "+
                // datFunk.TageDifferenz(geburtstag ,stichtag));

            }
            // Der urspruengliche Code hate hier int-Werte benutzt. Sollte die Abfrage mit den Namen
            // keinen Sinn ergeben, so eine andere int <-> Namen/Bdeutung Aufluesung irgendwo...
            if ((aktzzstatus == Zuzahlung.ZZSTATUS_BALD18
                    || aktzzstatus == Zuzahlung.ZZSTATUS_BEFREIT
                    || aktzzstatus == Zuzahlung.ZZSTATUS_NOTOK)
                 && einergroesser) {
                // String cmd = "update verordn set zzstatus='2' where rez_nr='"+rez_nr+" LIMIT
                // 1";
                // new ExUndHop().setzeStatement(cmd);
                SqlInfo.aktualisiereSaetze("verordn", "zzstatus='" + Zuzahlung.ZZSTATUS_NOTOK + "'", "rez_nr='" + rez_nr + "' LIMIT 1");
                Reha.instance.patpanel.aktRezept.setzeBild(RezepteAktuell.tabaktrez.getSelectedRow(), Zuzahlung.ZZSTATUS_NOTOK);
                ret[0] = new Boolean(true);
                ret[1] = Integer.valueOf(tage.size());
                ret[2] = Integer.valueOf(erstergroesser - 1);
                ret[3] = ((Integer) ret[1]) - (Integer) ret[2];
                ret[4] = Integer.valueOf(Zuzahlung.ZZSTATUS_NOTOK);
                return ret.clone();
            }
            // Der urspruengliche Code hate hier int-Werte benutzt. Sollte die Abfrage mit den Namen
            // keinen Sinn ergeben, so eine andere int <-> Namen/Bdeutung Aufluesung irgendwo...
            if ((aktzzstatus == Zuzahlung.ZZSTATUS_NOTOK
                    || aktzzstatus == Zuzahlung.ZZSTATUS_OK)
                 && (!einergroesser)) {
                // String cmd = "update verordn set zzstatus='3' where rez_nr='"+rez_nr+" LIMIT
                // 1";
                // new ExUndHop().setzeStatement(cmd);
                long tagex = DatFunk.TageDifferenz(geburtstag, gebtag);
                //// System.out.println("Tagex = ---------------> "+tagex);
                if (tagex <= 0 && tagex > -45) {
                    // JOptionPane.showMessageDialog(null ,"Achtung es sind noch "+(tagex*-1)+" Tage
                    // bis zur Vollj\ufffdhrigkeit\n"+
                    // "Unter Umst\ufffdnden wechselt der Zuzahlungsstatus im Verlauf dieses Rezeptes");
                    Reha.instance.patpanel.aktRezept.setzeBild(RezepteAktuell.tabaktrez.getSelectedRow(), Zuzahlung.ZZSTATUS_BALD18);
                    SqlInfo.aktualisiereSaetze("verordn", "zzstatus='" + Zuzahlung.ZZSTATUS_BALD18 + "'", "rez_nr='" + rez_nr + "' LIMIT 1");
                    ret[4] = Integer.valueOf(Zuzahlung.ZZSTATUS_BALD18);
                } else {
                    Reha.instance.patpanel.aktRezept.setzeBild(RezepteAktuell.tabaktrez.getSelectedRow(), Zuzahlung.ZZSTATUS_BEFREIT);
                    SqlInfo.aktualisiereSaetze("verordn", "zzstatus='" + Zuzahlung.ZZSTATUS_BEFREIT + "'", "rez_nr='" + rez_nr + "' LIMIT 1");
                    ret[4] = Integer.valueOf(Zuzahlung.ZZSTATUS_BEFREIT);
                }
                ret[0] = Boolean.valueOf(false);
                ret[1] = tage.size();
                return ret.clone();

            }
        } else if (unter18 && (aktzzregel == 0)) {
            Reha.instance.patpanel.aktRezept.setzeBild(RezepteAktuell.tabaktrez.getSelectedRow(), Zuzahlung.ZZSTATUS_BEFREIT);
            ret[0] = Boolean.valueOf(false);
            ret[1] = tage.size();
            ret[4] = Integer.valueOf(Zuzahlung.ZZSTATUS_BEFREIT);

        }
        // AktuelleRezepte.aktRez.tabaktrez.validate();
        return ret.clone();
    }

    /********************************************************/

    /********************************************************/

    public static Object[] unter18TestAllesSuchen(String rez_nr, boolean azTest, boolean jahrTest) {
        // TODO: this method could be updated to use new Rezept-class, both to retrieve the Rezept and use the ZZStatus-ints

        Object[] ret = { new Boolean(false), Integer.valueOf(-1), Integer.valueOf(-1), Integer.valueOf(-1) };
        Vector<Vector<String>> vec = SqlInfo.holeFelder(
                "select termine,id,pat_intern,jahrfrei,unter18,zzregel,zzstatus from verordn where rez_nr='" + rez_nr
                        + "' LIMIT 1");
        Vector<String> tage = RezTools.holeEinzelTermineAusRezept(null, vec.get(0)
                                                                           .get(0));
        if (tage.size() == 0) {
            return ret;
        }

        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                String strings1 = DatFunk.sDatInSQL(s1);
                String strings2 = DatFunk.sDatInSQL(s2);
                return strings1.compareTo(strings2);
            }
        };
        Collections.sort(tage, comparator);
        String unter18 = vec.get(0)
                            .get(4);
        String pat_int = vec.get(0)
                            .get(2);
        String aktzzstatus = vec.get(0)
                                .get(6);
        String aktzzregel = vec.get(0)
                               .get(5);
        if (unter18.equals("T") && (!aktzzregel.equals("0"))) {
            String stichtag = "";
            String geburtstag = DatFunk.sDatInDeutsch(SqlInfo.holePatFeld("geboren", "pat_intern='" + pat_int + "'"));
            boolean einergroesser = false;
            int erstergroesser = -1;
            for (int i = 0; i < tage.size(); i++) {
                stichtag = tage.get(i)
                               .substring(0, 6)
                        + Integer.valueOf(Integer.valueOf(SystemConfig.aktJahr) - 18)
                                 .toString();
                if (DatFunk.TageDifferenz(geburtstag, stichtag) >= 0) {
                    einergroesser = true;
                    break;
                }
                /*
                 * //System.out.println("Differenz an Tagen zwischen Behandlung vom "+tage.get(i
                 * )+ " und dem Geburtstag "+geburtstag+" = "+ datFunk.TageDifferenz(geburtstag
                 * ,stichtag));
                 */
            }
            if (aktzzstatus.equals("3") && einergroesser) {
                String cmd = "update verordn set zzstatus='2' where rez_nr='" + rez_nr + " LIMIT 1";
                new ExUndHop().setzeStatement(cmd);
                ret[0] = Boolean.valueOf(true);
                ret[1] = tage.size();
                ret[2] = Integer.toString(erstergroesser - 1);
                ret[3] = ((Integer) ret[1]) - erstergroesser;
            }
            if ((aktzzstatus.equals("2") || aktzzstatus.equals("1")) && (!einergroesser)) {
                String cmd = "update verordn set zzstatus='3' where rez_nr='" + rez_nr + " LIMIT 1";
                new ExUndHop().setzeStatement(cmd);
                ret[0] = Boolean.valueOf(false);
                ret[1] = tage.size();
            }
        } else if (unter18.equals("T") && (aktzzregel.equals("0"))) {
            ret[0] = Boolean.valueOf(false);
            ret[1] = tage.size();
        }
        return ret;
    }

    /********************************************************/

    public static void jahresWechselTest(String rez_nr, boolean azTest, boolean jahrTest) {
        Vector vec = SqlInfo.holeFelder("select termine,id from verordn where rez_nr='" + rez_nr + "' LIMIT 1");
        vec = RezTools.holeEinzelTermineAusRezept(null, (String) ((Vector) vec.get(0)).get(0));
        //// System.out.println(vec);
    }

    public static int getZuzahlRegel(String kassid) {
        String preisgrp = "";
        int zzregel = -1;
        if (kassid.equals("-1")) {
            JOptionPane.showMessageDialog(null, "Keine g\u00fcltige Kasse angegeben");
            return -1;
        }
        Vector<Vector<String>> vec = SqlInfo.holeFelder(
                "select preisgruppe from kass_adr where id='" + kassid.trim() + "' LIMIT 1");
        // System.out.println("Die Preisgruppe von KassenID ="+kassid.trim()+" =
        // "+((String)((Vector)vec.get(0)).get(0)) );
        preisgrp = (vec.get(0)
                       .get(0));

        // zzregel = SystemConfig.vZuzahlRegeln.get(Integer.valueOf(preisgrp)-1);
        String defaultHM = null;
        String[] xdisziplin = { "Physio", "Massage", "Ergo", "Logo", "Reha", "Podo" };
        for (int y = 0; y < xdisziplin.length; y++) {
            if (SystemConfig.initRezeptKlasse.toLowerCase()
                                             .startsWith(xdisziplin[y].toLowerCase())) {
                defaultHM = xdisziplin[y].toString();
                break;
            }
        }
        zzregel = SystemPreislisten.hmZuzahlRegeln.get((defaultHM != null ? defaultHM : "Physio"))
                                                  .get(Integer.valueOf(preisgrp) - 1);
        // zzregel =
        // SystemPreislisten.hmZuzahlRegeln.get("Physio").get(Integer.valueOf(preisgrp)-1);
        return zzregel;
    }

    public static int[] terminNachAchtzehn(Vector<String> tage, String geburtstag) {
        // int ret = -1;
        for (int i = 0; i < tage.size(); i++) {
            if (!DatFunk.Unter18(tage.get(i), geburtstag)) {
                return new int[] { 1, i };
            }
            /*
             * stichtag =
             * ((String)tage.get(i)).substring(0,6)+Integer.valueOf(Integer.valueOf(
             * SystemConfig.aktJahr)-18).toString(); if(DatFunk.TageDifferenz(geburtstag
             * ,stichtag) >= 0 ){ return new int[]{1,i}; }
             */

        }
        return new int[] { 0, -1 };
    }

    /**
     * f\u00fcllt die EnumMap: ZuZahlStatus als key, Icon als value
     *
     * @author McM
     */
    public static void setZzIcons() {
        zzIcon.put(ZZStat.ZUZAHLFREI, SystemConfig.hmSysIcons.get("zuzahlfrei"));
        zzIcon.put(ZZStat.ZUZAHLOK, SystemConfig.hmSysIcons.get("zuzahlok"));
        zzIcon.put(ZZStat.ZUZAHLNICHTOK, SystemConfig.hmSysIcons.get("zuzahlnichtok"));
        zzIcon.put(ZZStat.ZUZAHLRGR, SystemConfig.hmSysIcons.get("zuzahlRGR"));
        zzIcon.put(ZZStat.ZUZAHLNOTSET, SystemConfig.hmSysIcons.get("kleinehilfe"));
        return;
    }

    /**
     * liefert Icon passend zum (ZZStat-)Key
     *
     * @param Zuzahlstatus
     * @return das zugehoerige Icon
     *
     * @author McM
     */
    public static ImageIcon getZzIcon(ZZStat key) {
        return zzIcon.get(key);
    }

    /**
     * liefert zum bisher verwandten Integer-Index den passenden ZZStat
     *
     * @param oldIdx
     * @param rezNr
     * @return
     */
    public static ZZStat getIconKey(int oldIdx, String rezNr) {
        ZZStat iconKey;
        switch (oldIdx) {
        case 0:
            iconKey = ZZStat.ZUZAHLFREI;
            break;
        case 1:
            iconKey = ZZStat.ZUZAHLOK;
            break;
        case 2:
            if (existsRGR(rezNr)) { // Pruefen, ob RGR existiert. Falls ja, Icon entspr. setzen!
                iconKey = ZZStat.ZUZAHLRGR;
            } else {
                iconKey = ZZStat.ZUZAHLNICHTOK;
            }
            break;
        default:
            iconKey = ZZStat.ZUZAHLNOTSET;
        }
        return iconKey;
    }

    /**
     * Pruefung, ob es zum Rezept bereits eine RG-Rechnung gibt
     *
     * @param String Rezeptnummer
     * @return true/false
     *
     * @author McM
     */
    public static boolean existsRGR(String rezNb) {
        Vector<Vector<String>> testvec = SqlInfo.holeFelder(
                "select rnr from rgaffaktura where reznr='" + rezNb + "' AND rnr LIKE 'RGR-%' LIMIT 1");
        if (testvec.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Nummer der RG-Rechnung zu einem Rezept ermitteln
     *
     * @param xreznr
     * @return Rechnungsnummer
     *
     * @author McM
     */
    public static String getRgr(String rezNb) {
        String rnr = SqlInfo.holeEinzelFeld(
                "select rnr from rgaffaktura where reznr='" + rezNb + "' AND rnr LIKE 'RGR-%' LIMIT 1");
        if (rnr.length() > 0) {
            return rnr;
        }
        return "";
    }

    /**
     * Vorhandensein der RG-Rechnung zu einem Rezept bestaetigen
     *
     * @param xreznr
     * @return OK-String (enth\u00e4lt HTML-Tags!)
     *
     * @author McM
     */
    public static String rgrOK(String rezNb) {
        String rgrNr = getRgr(rezNb);
        if (rgrNr.length() > 0) {
            return "F\u00fcr dieses Rezept wurde bereits eine Rezeptgeb\u00fchrrechnung <b>" + rgrNr + "</b> angelegt!";
        }
        return "Fehler: rgrOK()";
    }

    /**
     * Pruefung, ob eine RG-Rechnung bar bezahlt wurde
     *
     * @param xreznr
     * @return true/false
     *
     * @author McM
     */
    public static boolean existsRgrBarInKasse(String rezNb) {
        Vector<Vector<String>> testvec = SqlInfo.holeFelder(
                "select einnahme,datum,ktext from kasse where rez_nr='" + rezNb + "' AND ktext LIKE 'RGR-%' LIMIT 1");
        if (testvec.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Pruefung, ob die Zuzahlung fuer ein Rezept bar bezahlt wurde
     *
     * @param xreznr
     * @return true/false
     *
     * @author McM
     */
    public static boolean existsBarQuittung(String rezNb) {
        // ktext hat das Format 'NachnamePatient,rezNb'
        String patId = SqlInfo.holeEinzelFeld("select pat_intern from verordn where rez_nr='" + rezNb + "' LIMIT 1");
        String patNN = SqlInfo.holeEinzelFeld("select n_name from pat5 where pat_intern='" + patId + "' LIMIT 1");
        Vector<Vector<String>> testvec = SqlInfo.holeFelder("select einnahme,datum,ktext from kasse where rez_nr='"
                + rezNb + "' AND ktext LIKE '" + patNN + "," + rezNb + "' LIMIT 1");
        if (testvec.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Pruefung, ob die Zuzahlung fuer ein Rezept kassiert oder eine RGR erstellt
     * wurde
     *
     * @param xreznr
     * @return true/false
     *
     * @author McM
     */
    public static boolean bereitsBezahlt(String rezNb) {
        if (existsBarQuittung(rezNb) || existsRgrBarInKasse(rezNb)) {
            return true;
        }
        return false;
    }

}
