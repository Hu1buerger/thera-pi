package stammDatenTools;

import java.awt.Point;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.DatFunk;
import CommonTools.SqlInfo;
import CommonTools.StringTools;
import abrechnung.Disziplinen;
import commonData.Rezeptvector;
import core.Disziplin;
import environment.Path;
import hauptFenster.Reha;
import rezept.RezeptDto;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;
import terminKalender.BestaetigungsDaten;
import terminKalender.TerminBestaetigenAuswahlFenster;
import terminKalender.TermineErfassen;
import wecker.AePlayWave;

public class RezTools {
    public static final int REZEPT_IST_JETZ_VOLL = 0;
    public static final int REZEPT_IST_BEREITS_VOLL = 1;
    private static final int REZEPT_HAT_LUFT = 2;
    public static final int REZEPT_FEHLER = 3;
    public static final int REZEPT_ABBRUCH = 4;
    public static final int DIALOG_ABBRUCH = -1;
    public static final int DIALOG_OK = 0;
    public static int DIALOG_WERT;

    private static Logger logger = LoggerFactory.getLogger(RezTools.class);

    public static boolean mitJahresWechsel(String datum) {
        boolean ret = false;
        try {
            if (Integer.parseInt(datum.substring(6)) - Integer.parseInt(SystemConfig.aktJahr) < 0) {
                ret = true;
            }
        } catch (Exception ex) {
        }
        return ret;
    }

    public static Vector<ArrayList<?>> Y_holePosUndAnzahlAusRezept(String xreznr) { // unbenutzt (?)
        Vector<ArrayList<?>> xvec = new Vector<ArrayList<?>>();
        ArrayList<String> positionen = new ArrayList<String>();
        ArrayList<String> doppeltest = new ArrayList<String>();
        ArrayList<Integer> anzahl = new ArrayList<Integer>();

        Vector<String> rezvec;

        rezvec = SqlInfo.holeSatz("verordn",
                "art_dbeh1,art_dbeh2,art_dbeh3,art_dbeh4," + "anzahl1,anzahl2,anzahl3,anzahl4",
                "rez_nr='" + xreznr + "'", Arrays.asList(new String[] {}));
        for (int i = 0; i < 4; i++) {
            if (!"0".equals(rezvec.get(i))) {
                if (i == 0) {
                    positionen.add(holePosAusIdUndRezNr(rezvec.get(i), xreznr));
                    anzahl.add(Integer.parseInt(rezvec.get(i + 4)));
                    doppeltest.add(rezvec.get(i));
                } else if (i >= 1) {
                    if (rezvec.indexOf(rezvec.get(i)) != i) { // Doppelbehandlung, wenn die HMPos vor i schon mal
                                                              // aufgeführt ist. Hintergrund: Doppelbehandlungen müssen
                                                              // nicht auf i=0 und i=1 sein
                        anzahl.set(0, Integer.parseInt(rezvec.get(i + 4))
                                + Integer.parseInt(rezvec.get(rezvec.indexOf(rezvec.get(i)) + 4)));
                    } else {
                        doppeltest.add(rezvec.get(i));
                        positionen.add(holePosAusIdUndRezNr(rezvec.get(i), xreznr));
                        anzahl.add(Integer.parseInt(rezvec.get(i + 4)));
                    }
                } else {
                    doppeltest.add(rezvec.get(i));
                    positionen.add(holePosAusIdUndRezNr(rezvec.get(i), xreznr));
                    anzahl.add(Integer.parseInt(rezvec.get(i + 4)));
                }
            }
        }
        xvec.add((ArrayList<?>) positionen.clone());
        xvec.add((ArrayList<?>) anzahl.clone());
        xvec.add((ArrayList<?>) doppeltest.clone()); // zum Test ausgeschaltet
        return xvec;
    }

    private static Object[] sucheDoppel(int pos, List<String> list, String comperator) {
        // System.out.println("Position="+pos+" fistIndex="+list.indexOf(comperator)+"
        // lastIndex="+list.lastIndexOf(comperator));
        if (pos == list.indexOf(comperator)) {
            return new Object[] { true, list.indexOf(comperator), list.lastIndexOf(comperator) };
        } else {
            return new Object[] { true, list.lastIndexOf(comperator), list.indexOf(comperator) };
        }
    }

    /** Mistding - elendes aber jetzt haben wir dich! */
    private static Vector<ArrayList<?>> holePosUndAnzahlAusTerminen(String xreznr) {
        Vector<ArrayList<?>> xvec = new Vector<>();

        Vector<String> rezvec = SqlInfo.holeSatz("verordn",
                "termine,pos1,pos2,pos3," + "pos4,kuerzel1,kuerzel2,kuerzel3,kuerzel4,preisgruppe",
                "rez_nr='" + xreznr + "'", Arrays.asList(new String[] {}));
        Vector<String> termvec = holeEinzelZiffernAusRezept(null, rezvec.get(0));

        List<String> tmpList = Arrays.asList(rezvec.get(1), rezvec.get(2), rezvec.get(3), rezvec.get(4));
        List<String> list = new ArrayList<String>();
        for (int i = tmpList.size() - 1; i >= 0; i--) {
            String sTmp = tmpList.get(i);
            if (!sTmp.equals("")) { // Liste umsortieren auf 'alten Stand' (belegte Felder vorn)
                list.add(0, sTmp);
            } else {
                list.add(sTmp);                
            }
        }

        ArrayList<String> positionen = new ArrayList<>();
        String behandlungen = null;
        String[] einzelbehandlung = null;
        ArrayList<Integer> anzahl = new ArrayList<>();
        ArrayList<Boolean> vorrangig = new ArrayList<>();
        ArrayList<Boolean> einzelerlaubt = new ArrayList<>();
        ArrayList<Object[]> doppelpos = new ArrayList<>();
        boolean[] bvorrangig = null;
        String aktHmPos = "";
        int idxPos = 0;
        for (int i = 1; i < 5; i++) {
            aktHmPos = rezvec.get(i)
                             .trim();
            if (! "".equals(aktHmPos)) {
                positionen.add(String.valueOf(aktHmPos));
                bvorrangig = isVorrangigAndExtra(rezvec.get(i + 4), xreznr.substring(0, 2));
                vorrangig.add(Boolean.valueOf(bvorrangig[0]));
                einzelerlaubt.add(Boolean.valueOf(bvorrangig[1]));
                anzahl.add(0);
                if (countOccurence(list, aktHmPos) > 1) {
                    doppelpos.add(sucheDoppel(idxPos, list, aktHmPos));
                } else {
                    Object[] obj = { false, idxPos, idxPos };
                    doppelpos.add(obj.clone());
                }
                idxPos++;
            }
        }

        Vector<String> imtag = new Vector<>();
        Object[] tests = null;
        for (int i = 0; i < termvec.size(); i++) {
            // Über alle Behandlungstage hinweg
            try {
                behandlungen = termvec.get(i);
                if (!"".equals(behandlungen)) {
                    einzelbehandlung = behandlungen.split(",");
                    imtag.clear();
                    int i2;
                    for (i2 = 0; i2 < einzelbehandlung.length; i2++) {  // über Behandlungen des Tages
                        try {
                            // Jetzt testen ob Doppelbehandlung
                            tests = doppelpos.get(list.indexOf(einzelbehandlung[i2]));
                            if ((Boolean) tests[0]) {
                                // Ja Doppelbehandlung
                                imtag.add(String.valueOf(einzelbehandlung[i2]));
                                // Jetzt testen ob erste oder Zweite
                                if (imtag.indexOf(einzelbehandlung[i2]) == imtag.lastIndexOf(einzelbehandlung[i2])) {
                                    // Erstes mal
                                    anzahl.set((Integer) tests[1], anzahl.get((Integer) tests[1]) + 1);
                                } else {
                                    // Zweites mal
                                    anzahl.set((Integer) tests[2], anzahl.get((Integer) tests[2]) + 1);
                                }
                            } else {
                                // Nein keine Doppelbehandlung
                                anzahl.set((Integer) tests[1], anzahl.get((Integer) tests[1]) + 1);
                            }
                        } catch (Exception ex) {
                            try {
                                String disziplin = getDisziplinFromRezNr(xreznr);
                                String kuerzel = getKurzformFromPos(einzelbehandlung[i2], rezvec.get(9),
                                        SystemPreislisten.hmPreise.get(disziplin)
                                                                  .get(Integer.parseInt(rezvec.get(9)) - 1));
                                JOptionPane.showMessageDialog(null,
                                        "<html><font color='#ff0000' size=+2>Fehler in der Ermittlung der Behandlungspositionen!</font><br><br>"
                                                + "<b>Bitte kontrollieren sie die bereits gespeicherten Behandlungspositionen!!<br><br>"
                                                + "Der problematische Termin ist der <font color='#ff0000'>" + (i + 1)
                                                + ".Termin</font>,<br>bestätigte Behandlungsart ist <font color='#ff0000'>"
                                                + kuerzel + " (" + einzelbehandlung[i2] + ")<br>"
                                                + "<br>Diese Behandlungsart ist im Rezeptblatt nicht, oder nicht mehr verzeichnet</font><br><br>"
                                                + "<br>"
                                                + "<b><font color='#ff0000'>Lösung:</font> Klicken Sie die Termintabelle an, drücken Sie dann die rechte Maustaste und wählen Sie dann die Option<br><br>"
                                                + "<b><u>\"alle Behandlungsarten den Rezeptdaten angleichen\"</u></b><br>"
                                                + "</b>oder<br><b><u>\"alle im Rezept gespeicherten Behandlungsarten löschen\"</u></b><br></html>");
                                return xvec;
                            } catch (Exception ex2) {
                                JOptionPane.showMessageDialog(null,
                                        "<html><font color='#ff0000' size=+2>Fehler in der Ermittlung der Behandlungspositionen!</font><br><br>"
                                                + "<b>Bitte kontrollieren sie die bereits gespeicherten Behandlungspositionen!!<br><br>"
                                                + "Der Fehler kann nicht genau lokalisiert werden!<br><br>"
                                                + "Vermutlich wurden in den bisherigen Terminen Positionen bestätigt, die im Rezeptblatt<br>"
                                                + "<u>nicht oder nicht mehr aufgeführt sind.</u><br><br>"
                                                + "<b>Klicken Sie die Termintabelle an, drücken Sie dann die rechte Maustaste und wählen Sie eine Option aus.<b><br></html>");
                                return xvec;
                            }
                        }
                    }
                } else {
                    for (int i3 = 0; i3 < positionen.size(); i3++) {
                        anzahl.set(i3, anzahl.get(i3) + 1);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        xvec.add((ArrayList<?>) positionen.clone());
        xvec.add((ArrayList<?>) anzahl.clone());
        xvec.add((ArrayList<?>) vorrangig.clone());
        xvec.add((ArrayList<?>) einzelerlaubt.clone());
        xvec.add((ArrayList<?>) doppelpos.clone());
        return xvec;
    }

    private static int countOccurence(List<String> list, String comperator) {
        int ret = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i)
                    .trim()
                    .equals(comperator.trim())) {
                ret++;
            }
        }
        return ret;
    }

    public static boolean[] isVorrangigAndExtra(String kuerzel, String rezClass) {
        Disziplinen diszis = new Disziplinen();
        final int VORRANGIG = 0, EXTRAOK = 1;
        boolean[] bret = { false, false };
        try {
            Vector<String> vec = SqlInfo.holeFelder("select vorrangig,extraok from kuerzel where kuerzel='" + kuerzel
                    + "' and disziplin ='" + rezClass + "' LIMIT 1")
                                        .get(0);
            if (vec.isEmpty()) {
                String msg = "Achtung!\n\n" + "Ihre Kürzelzuordnung in den Preislisten ist nicht korrekt!!!!!\n"
                        + "Kürzel: " + kuerzel + "\n" + "Disziplin: " + diszis.getDisziKurzFromRK(rezClass) + "\n\n"
                        + "Für die ausgewählte Diziplin ist das angegebene Kürzel nicht in der Kürzeltabelle vermerkt!";
                JOptionPane.showMessageDialog(null, msg);
                return null;
            }
            bret[0] = "T".equals(vec.get(VORRANGIG));
            bret[1] = "T".equals(vec.get(EXTRAOK));
        } catch (Exception e) {
            logger.error("could not retrieve is vorrangig for " + kuerzel + " " + rezClass, e);
        }
        return bret;
    }

    private static Vector<String> holeEinzelZiffernAusRezept(String xreznr, String termine) {
        Vector<String> xvec = null;
        Vector<String> retvec = new Vector<>();
        String terms = null;
        if ("".equals(termine)) {
            xvec = SqlInfo.holeSatz("verordn", "termine,pat_intern", "rez_nr='" + xreznr + "'",
                    Arrays.asList(new String[] {}));
            if (xvec.isEmpty()) {
                return retvec;
            } else {
                terms = xvec.get(0);
            }
        } else {
            terms = termine;
        }
        if (terms == null || "".equals(terms)) {
            return retvec;
        }
        String[] tlines = terms.split("\n");
        int lines = tlines.length;

        for (int i = 0; i < lines; i++) {
            String[] terdat = tlines[i].split("@");
            // int ieinzel = terdat.length;
            retvec.add(terdat[3].trim());
        }
        return retvec;
    }

    public static Object[] holeTermineAnzahlUndLetzter(String termine) {
        Object[] retobj = { null, null };
        try {
            String[] tlines = termine.split("\n");
            int lines = tlines.length;
            if (lines <= 0) {
                retobj[0] = 0;
                retobj[1] = null;
                return retobj;
            }
            String[] terdat;
            terdat = tlines[lines - 1].split("@");
            retobj[0] = Integer.valueOf(lines);
            retobj[1] = String.valueOf(terdat[0]);
        } catch (Exception ex) {
        }
        return retobj;
    }

    public static String holeErstenTermin(String xreznr, String termine) {
        try {
            Vector<String> xvec = null;
            String terms = null;
            if ("".equals(termine)) {
                xvec = SqlInfo.holeSatz("verordn", "termine,pat_intern", "rez_nr='" + xreznr + "'",
                        Arrays.asList(new String[] {}));
                if (xvec.isEmpty()) {
                    return "";
                } else {
                    terms = xvec.get(0);
                }
            } else {
                terms = termine;
            }
            if (terms == null || "".equals(terms)) {
                return "";
            }
            String[] tlines = terms.split("\n");
            String[] terdat = tlines[0].split("@");
            return terdat[0];
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static String holeLetztenTermin(String xreznr, String termine) {
        try {
            Vector<String> xvec = null;
            String terms = null;
            if ("".equals(termine)) {
                xvec = SqlInfo.holeSatz("verordn", "termine,pat_intern", "rez_nr='" + xreznr + "'",
                        Arrays.asList(new String[] {}));
                if (xvec.isEmpty()) {
                    return "";
                } else {
                    terms = xvec.get(0);
                }
            } else {
                terms = termine;
            }
            if (terms == null || "".equals(terms)) {
                return "";
            }
            String[] tlines = terms.split("\n");
            int line = tlines.length - 1;
            String[] terdat = tlines[line].split("@");
            return terdat[0];
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static Vector<String> holeEinzelTermineAusRezept(String xreznr, String termine) {
        // RezeptDto dto = new RezeptDto(Reha.getMandant().ik());
        Vector<String> xvec = null;
        Vector<String> retvec = new Vector<>();
        String terms = null;
        if ("".equals(termine)) {
            xvec = SqlInfo.holeSatz("verordn", "termine,pat_intern", "rez_nr='" + xreznr + "'",
                    Arrays.asList(new String[] {}));
            if (xvec.isEmpty()) {
                return (Vector<String>) retvec.clone();
            } else {
                terms = xvec.get(0);
            }
        } else {
            terms = termine;
        }
        if (terms == null || "".equals(terms)) {
            return (Vector<String>) retvec.clone();
        }
        String[] tlines = terms.split("\n");
        int lines = tlines.length;
        String[] terdat = null;
        for (int i = 0; i < lines; i++) {
            terdat = tlines[i].split("@");
            // int ieinzel = terdat.length;
            retvec.add("".equals(terdat[0].trim()) ? "  .  .    " : String.valueOf(terdat[0]));
        }
        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                String strings1 = DatFunk.sDatInSQL(s1);
                String strings2 = DatFunk.sDatInSQL(s2);
                return strings1.compareTo(strings2);
            }
        };
        Collections.sort(retvec, comparator);
        return (Vector<String>) retvec.clone();
    }

    public static Vector<Vector<String>> holeTermineUndBehandlerAusRezept(String xreznr, String termine) {
        Vector<String> xvec = null;
        Vector<String> retvec = new Vector<>();
        Vector<Vector<String>> retbeides = new Vector<>();
        String terms = null;
        if ("".equals(termine)) {
            xvec = SqlInfo.holeSatz("verordn", "termine,pat_intern", "rez_nr='" + xreznr + "'",
                    Arrays.asList(new String[] {}));
            if (xvec.isEmpty()) {
                return (Vector<Vector<String>>) retbeides.clone();
            } else {
                terms = xvec.get(0);
            }
        } else {
            terms = termine;
        }
        if (terms == null || "".equals(terms)) {
            return (Vector<Vector<String>>) retbeides.clone();
        }
        String[] tlines = terms.split("\n");
        int lines = tlines.length;
        String[] terdat = null;
        for (int i = 0; i < lines; i++) {
            retvec.clear();
            terdat = tlines[i].split("@");
            // int ieinzel = terdat.length;
            retvec.add("".equals(terdat[0].trim()) ? "  .  .    " : String.valueOf(terdat[0]));
            retvec.add("".equals(terdat[1].trim()) ? "k.A." : String.valueOf(terdat[1]));
            retbeides.add((Vector) retvec.clone());
        }
        Comparator<Vector> comparator = new Comparator<Vector>() {
            @Override
            public int compare(Vector o1, Vector o2) {
                String s1 = DatFunk.sDatInSQL((String) o1.get(0));
                String s2 = DatFunk.sDatInSQL((String) o2.get(0));
                return s1.compareTo(s2);
            }
        };
        Collections.sort(retbeides, comparator);

        return (Vector<Vector<String>>) retbeides.clone();
    }

    private static String holePosAusIdUndRezNr(String id, String reznr) {
        String diszi = getDisziplinFromRezNr(reznr);
        String preisgruppe = SqlInfo.holeEinzelFeld(
                "select preisgruppe from verordn where rez_nr='" + reznr + "' LIMIT 1");

        Vector<Vector<Vector<String>>> vector = null;
        try {
            vector = SystemPreislisten.hmPreise.get(diszi);
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Keine SystemPreislisten eingelesen? Error: " + e.getLocalizedMessage());
            // e.printStackTrace();
        }

        if (vector == null) {
            System.out.println("baeh"); // Boo! If inner Vector was NULL, it can still be wrapped & outer Vec != NULL
        }
        Vector<Vector<String>> preisvec = vector.get(Integer.parseInt(preisgruppe) - 1);
        String pos = getPosFromID(id, preisgruppe, preisvec);
        return pos == null ? "" : pos;
    }

    public static Vector<Vector<String>> macheTerminVector(String termine) {
        String[] tlines = termine.split("\n");
        int lines = tlines.length;
        //// System.out.println("Anzahl Termine = "+lines);
        Vector<Vector<String>> tagevec = new Vector<>();
        Vector<String> tvec = new Vector<>();
        String[] terdat = null;
        for (int i = 0; i < lines; i++) {
            terdat = tlines[i].split("@");
            int ieinzel = terdat.length;
            //// System.out.println("Anzahl Splits = "+ieinzel);
            tvec.clear();
            for (int y = 0; y < ieinzel; y++) {
                if (y == 0) {
                    tvec.add(String.valueOf("".equals(terdat[y].trim()) ? "  .  .    " : terdat[y]));
                    if (i == 0) {
                        SystemConfig.hmAdrRDaten.put("<Rerstdat>",
                                String.valueOf("".equals(terdat[y].trim()) ? "  .  .    " : terdat[y]));
                    }
                } else {
                    tvec.add(String.valueOf(terdat[y]));
                }
                //// System.out.println("Feld "+y+" = "+terdat[y]);
            }
            //// System.out.println("Termivector = "+tvec);
            tagevec.add((Vector<String>) tvec.clone());
        }
        if (!tagevec.isEmpty()) {
            Comparator<Vector<String>> comparator = new Comparator<Vector<String>>() {
                @Override
                public int compare(Vector<String> o1, Vector<String> o2) {
                    String s1 = o1.get(4);
                    String s2 = o2.get(4);
                    return s1.compareTo(s2);
                }
            };
            Collections.sort(tagevec, comparator);
        }
        return (Vector<Vector<String>>) tagevec.clone();
    }

    public static boolean zweiPositionenBeiHB(String disziplin, String preisgruppe) {
        int pg = Integer.parseInt(preisgruppe) - 1;
        return !"".equals(SystemPreislisten.hmHBRegeln.get(disziplin)
                                                      .get(pg)
                                                      .get(2)
                                                      .trim())
                || !"".equals(SystemPreislisten.hmHBRegeln.get(disziplin)
                                                          .get(pg)
                                                          .get(3)
                                                          .trim());
    }

    private static boolean keineWeggebuehrBeiHB(String disziplin, String preisgruppe) {
        int pg = Integer.parseInt(preisgruppe) - 1;
        return "".equals(SystemPreislisten.hmHBRegeln.get(disziplin)
                                                     .get(pg)
                                                     .get(2)
                                                     .trim())
                && "".equals(SystemPreislisten.hmHBRegeln.get(disziplin)
                                                         .get(pg)
                                                         .get(3)
                                                         .trim());
    }

    public static String getLangtextFromID(String id, String preisgruppe, Vector<Vector<String>> vec) {
        String ret = "kein Lantext vorhanden";
        int lang = vec.size();
        if (lang == 0 || vec.get(0) == null) {
            return ret;
        }
        int idpos = vec.get(0)
                       .size()
                - 1;
        for (int i = 0; i < lang; i++) {
            if (vec.get(i)
                   .get(idpos)
                   .equals(id)) {
                ret = vec.get(i)
                         .get(0);
                break;
            }
        }
        return ret;
    }

    public static String getPreisAktFromID(String id, String preisgruppe, Vector<Vector<String>> vec) {
        int lang = vec.size(), i;
        int idpos = vec.get(0)
                       .size()
                - 1;
        String ret = "0.00";
        for (i = 0; i < lang; i++) {
            if (vec.get(i)
                   .get(idpos)
                   .equals(id)) {
                ret = vec.get(i)
                         .get(3);
                break;
            }
        }
        return ret;
    }

    public static String getPreisAltFromID(String id, String preisgruppe, Vector<Vector<String>> vec) {
        int lang = vec.size(), i;
        int idpos = vec.get(0)
                       .size()
                - 1;
        String ret = "0.00";
        for (i = 0; i < lang; i++) {
            if (vec.get(i)
                   .get(idpos)
                   .equals(id)) {
                ret = vec.get(i)
                         .get(4);
                break;
            }
        }
        return ret;
    }

    public static String getPreisAktFromPos(String pos, String preisgruppe, Vector<Vector<String>> vec) {
        try {
            int lang = vec.size(), i;
            String ret = "0.00";
            for (i = 0; i < lang; i++) {
                if (vec.get(i)
                       .get(2)
                       .equals(pos)) {
                    ret = vec.get(i)
                             .get(3);
                    break;
                }
            }
            return ret;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "0.00";
        }
    }

    public static String getPreisAltFromPos(String pos, String preisgruppe, Vector<Vector<String>> vec) {
        int lang = vec.size(), i;
        String ret = "0.00";
        for (i = 0; i < lang; i++) {
            if (vec.get(i)
                   .get(1)
                   .equals(pos)) {
                ret = vec.get(i)
                         .get(4);
                break;
            }
        }
        return ret;
    }

    public static String getPreisAltFromPosNeu(String pos, String preisgruppe, Vector<Vector<String>> vec) {
        int lang = vec.size(), i;
        String ret = "0.00";
        for (i = 0; i < lang; i++) {
            if (vec.get(i)
                   .get(2)
                   .equals(pos)) {
                ret = vec.get(i)
                         .get(4);
                break;
            }
        }
        return ret;
    }

    public static String getIDFromPos(String pos, String preisgruppe, Vector<Vector<String>> vec) {
        int lang = vec.size(), i;
        int idpos = vec.get(0)
                       .size()
                - 1;
        String ret = "-1";
        for (i = 0; i < lang; i++) {
            if (vec.get(i)
                   .get(2)
                   .equals(pos)) {
                ret = vec.get(i)
                         .get(idpos);
                break;
            }
        }
        return ret;
    }

    public static String getIDFromPosX(String pos, String preisgruppe, String disziplin) {
        Vector<Vector<String>> vec = holePreisVector(disziplin, Integer.parseInt(preisgruppe) - 1);
        int lang = vec.size(), i;
        int idpos = vec.get(0)
                       .size()
                - 1;
        String ret = "-1";
        for (i = 0; i < lang; i++) {
            if (vec.get(i)
                   .get(2)
                   .equals(pos)) {
                ret = vec.get(i)
                         .get(idpos);
                break;
            }
        }
        return ret;
    }

    public static String getPosFromID(String id, String preisgruppe, Vector<Vector<String>> vec) {
        int lang = vec.size(), i;
        int idpos = vec.get(0)
                       .size()
                - 1;
        String ret = "";
        for (i = 0; i < lang; i++) {
            if (vec.get(i)
                   .get(idpos)
                   .equals(id)) {
                ret = vec.get(i)
                         .get(2);
                break;
            }
        }
        return ret;
    }

    public static String getKurzformFromID(String id, Vector<Vector<String>> vec) {
        int lang = vec.size(), i;
        int idpos = vec.get(0)
                       .size()
                - 1;
        String ret = "";
        for (i = 0; i < lang; i++) {
            if (vec.get(i)
                   .get(idpos)
                   .equals(id)) {
                ret = vec.get(i)
                         .get(1);
                break;
            }
        }
        return ret;
    }

    public static String getKurzformFromPos(String pos, String preisgruppe, Vector<Vector<String>> vec) {
        // Parameter preisgruppe wird nicht ausgewertet
        int lang = vec.size(), i;
        // int suchenin = (Integer.parseInt(preisgruppe)*4)-2;
        String ret = "";
        try {
            for (i = 0; i < lang; i++) {
                if (vec.get(i)
                       .get(2)
                       .trim()
                       .equals(pos.trim())
                        && !"Isokin".equals(vec.get(i)
                                               .get(1))) {
                    ret = vec.get(i)
                             .get(1);
                    break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Parameter pos = " + pos);
            System.out.println("Parameter preisgruppe = " + preisgruppe);
            System.out.println("Parameter vec = " + vec);
            System.out.println("Parameter ret = " + ret);
            System.out.println("Nachfolgend die Excepiton von getKurzformFromPos");
            ex.printStackTrace();
        }
        return ret;
    }

    public static Object[] getKurzformUndIDFromPos(String pos, String preisgruppe, Vector<Vector<String>> vec) {
        int lang = vec.size(), i;
        // int suchenin = (Integer.parseInt(preisgruppe)*4)-2;
        int idpos = vec.get(0)
                       .size()
                - 1;
        Object[] retobj = { "", "" };
        for (i = 0; i < lang; i++) {
            if (vec.get(i)
                   .get(2)
                   .equals(pos)
                    && !"Isokin".equals(vec.get(i)
                                           .get(1))) {
                retobj[0] = vec.get(i)
                               .get(1);
                retobj[1] = vec.get(i)
                               .get(idpos);
                break;
            }
        }
        return retobj.clone();
    }

    public static String getIDFromKurzform(String kurzform, Vector<Vector<String>> vec) {
        int lang = vec.size(), i;
        int idpos = vec.get(0)
                       .size()
                - 1;
        String ret = "";
        for (i = 0; i < lang; i++) {
            if (vec.get(i)
                   .get(1)
                   .equals(kurzform)) {
                ret = vec.get(i)
                         .get(idpos);
                break;
            }
        }
        return ret;
    }

    public static Vector<Vector<String>> holePreisVector(String disziplin, int preisgruppe) {
        try {
            if (disziplin.startsWith("KG")) {
                // return (Vector<Vector<String>>)ParameterLaden.vKGPreise;
                return SystemPreislisten.hmPreise.get("Physio")
                                                 .get(preisgruppe);
            } else if (disziplin.startsWith("MA")) {
                // return (Vector<Vector<String>>)ParameterLaden.vMAPreise;
                return SystemPreislisten.hmPreise.get("Massage")
                                                 .get(preisgruppe);
            } else if (disziplin.startsWith("ER")) {
                // return (Vector<Vector<String>>)ParameterLaden.vERPreise;
                return SystemPreislisten.hmPreise.get("Ergo")
                                                 .get(preisgruppe);
            } else if (disziplin.startsWith("LO")) {
                // return (Vector<Vector<String>>)ParameterLaden.vLOPreise;
                return SystemPreislisten.hmPreise.get("Logo")
                                                 .get(preisgruppe);
            } else if (disziplin.startsWith("RH")) {
                // return (Vector<Vector<String>>)ParameterLaden.vRHPreise;
                return SystemPreislisten.hmPreise.get("Reha")
                                                 .get(preisgruppe);
            } else if (disziplin.startsWith("PO")) {
                // return (Vector<Vector<String>>)ParameterLaden.vRHPreise;
                return SystemPreislisten.hmPreise.get("Podo")
                                                 .get(preisgruppe);
            } else if (disziplin.startsWith("RS")) {
                // return (Vector<Vector<String>>)ParameterLaden.vRHPreise;
                return SystemPreislisten.hmPreise.get("Rsport")
                                                 .get(preisgruppe);
            } else if (disziplin.startsWith("FT")) {
                // return (Vector<Vector<String>>)ParameterLaden.vRHPreise;
                return SystemPreislisten.hmPreise.get("Ftrain")
                                                 .get(preisgruppe);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler im Preislistenbezug!\n\nVermutete Ursache:\nSie haben eine oder mehrere Tarifgruppen gelöscht!\nSelbst Schuld, sowas sollte man nicht machen");
        }
        return null;
    }

    public static boolean neuePreisNachRezeptdatumOderStichtag(String aktDisziplin, int tarifgruppe, String rez_datum,
            boolean neuanlage, Vector<String> rezvec) {
        try {
            String datum = SystemPreislisten.hmNeuePreiseAb.get(aktDisziplin)
                                                           .get(tarifgruppe);
            int regel = SystemPreislisten.hmNeuePreiseRegel.get(aktDisziplin)
                                                           .get(tarifgruppe);
            Vector<String> tage = null;
            // Regel 1=nach Behandlungsbeginn, 2=nach Rezeptdatum, 3=irgend eine Behandlung
            // ab Datum //der Rest wird nicht ausgewertet
            if (!"".equals(datum.trim()) && regel == 2) {
                return DatFunk.TageDifferenz(datum, rez_datum) >= 0;
            } else if (!"".equals(datum.trim()) && regel == 1) {
                // Neuanlage
                if (neuanlage) {
                    return DatFunk.TageDifferenz(datum, DatFunk.sHeute()) >= 0;
                }
                // Tage holen
                tage = holeEinzelTermineAusRezept(null, rezvec.get(34));
                if (tage.isEmpty()) {
                    // keine Tage vorhanden und Datum heute >= Regeldatum
                    return DatFunk.TageDifferenz(datum, DatFunk.sHeute()) >= 0;
                } else if (!tage.isEmpty()) {
                    // Tage vorhanden dann Datum testen
                    return DatFunk.TageDifferenz(datum, tage.get(0)) >= 0;
                }
            } else if (!"".equals(datum.trim()) && regel == 3) {
                // Neuanlage
                if (neuanlage) {
                    return DatFunk.TageDifferenz(datum, DatFunk.sHeute()) >= 0;
                }
                // Tage holen
                tage = holeEinzelTermineAusRezept(null, rezvec.get(34));
                if (tage.isEmpty()) {
                    // keine Tage vorhanden und Datum heute >= Regeldatum
                    return DatFunk.TageDifferenz(datum, DatFunk.sHeute()) >= 0;
                } else if (!tage.isEmpty()) {
                    // Tage vorhanden dann Datum testen ob irgend ein Datum >=
                    for (int i = 0; i < tage.size(); i++) {
                        if (DatFunk.TageDifferenz(datum, tage.get(i)) >= 0) {
                            return true;
                        }
                    }
                    return DatFunk.TageDifferenz(datum, DatFunk.sHeute()) >= 0;
                }
            } else {
                // Neuanlage
                /*
                 * if(neuanlage){ if(DatFunk.TageDifferenz(datum,DatFunk.sHeute() ) < 0){ return
                 * false; } return true; }
                 */
                // Bei Regel 4
                return "".equals(datum.trim()) || regel != 4 || DatFunk.TageDifferenz(datum, DatFunk.sHeute()) >= 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Bei Regel 0 oder wenn alles nicht zutrifft
        return true;
    }

    public static int testeRezGebArt(boolean testefuerbarcode, boolean hintergrund, String srez, String termine) {
        int iret = 0;
        Vector<String> vAktTermine = null;
        boolean bTermine = false;
        boolean u18Test = false;
        boolean bMitJahresWechsel = false;
        ZuzahlModell zm = new ZuzahlModell();
        Rezeptvector myRezept = new Rezeptvector();
        myRezept.setVec_rez(Reha.instance.patpanel.vecaktrez);
        String geburtstag = DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4));
        boolean warImVorjahrBefreit = Reha.instance.patpanel.patDaten.get(69)
                                                                     .trim()
                                                                     .equals(SystemConfig.vorJahr);
        boolean istBefreit = "T".equals(Reha.instance.patpanel.patDaten.get(30));

        // 1. Schritt haben wir bereits Termineintr�ge die man auswerten kann
        if (!(vAktTermine = holeEinzelTermineAusRezept("", termine)).isEmpty()) {
            // Es gibt Termine in der Tabelle
            bTermine = true;
            if (vAktTermine.get(0)
                           .substring(6)
                           .equals(SystemConfig.vorJahr)) {
                bMitJahresWechsel = true;
            }
            if (DatFunk.Unter18(vAktTermine.get(0), geburtstag)) {
                u18Test = true;
            }
        }

        for (int i = 0; i < 1; i++) {
            if (myRezept.getZzRegel() <= 0) {
                // Kasse erfordert keine Zuzahlung
                zm.allefrei = true;
                iret = 0;
                break;
            }
            if (Integer.parseInt(myRezept.getZzStat()) == 1) {
                // Hat bereits bezahlt normal behandeln (zzstatus == 1)
                zm.allezuzahl = true;
                iret = 2;
            }

            /* Jetzt der Ober-Scheißdreck für den Achtzehner-Test */
            if (myRezept.getUnter18() || u18Test) {
                // Es ist ein unter 18 Jahre Test notwendig
                if (bTermine) {
                    int[] test = ZuzahlTools.terminNachAchtzehn(vAktTermine, geburtstag);
                    if (test[0] > 0) {
                        // muß zuzahlen

                        zm.allefrei = false;
                        if (test[1] > 0) {
                            zm.allefrei = false;
                            zm.allezuzahl = false;
                            zm.anfangfrei = true;
                            zm.teil1 = test[1];
                            zm.teil2 = maxAnzahl() - test[1];
                            // System.out.println("Splitten frei für "+test[1]+" Tage, bezahlen für
                            // "+(maxAnzahl()-test[1]));
                            iret = 1;
                        } else {
                            zm.allezuzahl = true;
                            zm.teil1 = test[1];
                            // System.out.println("Jeden Termin bezahlen insgesamt bezahlen für
                            // "+(maxAnzahl()-test[1]));
                            iret = 2;
                        }
                    } else {
                        // Voll befreit
                        zm.allefrei = true;
                        iret = 0;
                    }
                } else {
                    // Es stehen keine Termine für Analyse zur Verfügung also muß das Fenster für
                    // manuelle Eingabe geöffnet werden!!
                    String stichtag = DatFunk.sHeute()
                                             .substring(0, 6)
                            + Integer.valueOf(Integer.valueOf(SystemConfig.aktJahr) - 18);
                    if (DatFunk.TageDifferenz(geburtstag, stichtag) >= 0) {
                        // System.out.println("Normale Zuzahlung....");
                        zm.allefrei = false;
                        zm.allezuzahl = true;
                        iret = 2;
                    } else {
                        // System.out.println("Alle Frei....");
                        zm.allefrei = true;
                        zm.allezuzahl = false;
                        iret = 0;
                    }
                }
                break;
            }

            /* Keine Befreiung Aktuell und keine Vorjahr (Normalfall) */
            if (!istBefreit && !warImVorjahrBefreit) {
                iret = 2;
                break;
            }
            /* Aktuell befreit und im Vorjahr auch befreit */
            if (istBefreit && warImVorjahrBefreit) {
                iret = 0;
                break;
            }
            /* aktuell nicht frei, Vorjahr frei */
            if (!istBefreit && warImVorjahrBefreit) {
                if (!bMitJahresWechsel) {// Alle Termine aktuell
                    iret = 2;
                } else {// es gibt Termine im Vorjahr
                    Object[] obj = JahresWechsel(vAktTermine, SystemConfig.vorJahr);
                    if (!(Boolean) obj[0]) {// alle Termine waren im Vorjahr
                        if (vAktTermine.size() < maxAnzahl()) {
                            String meldung = "<html>Während der Befreiung wurden <b>" + vAktTermine.size() + "  von "
                                    + maxAnzahl() + " Behandlungen</b> durchgeführt!<br>"
                                    + "Rezeptgebühren müssen also noch für <b>" + (maxAnzahl() - vAktTermine.size())
                                    + " Behandlungen</b> entrichtet werden.<br>"
                                    + "<br><br>Ist das korrekt?<br><br></html>";
                            int anfrage = JOptionPane.showConfirmDialog(null, meldung,
                                    "Achtung wichtige Benutzeranfrage", JOptionPane.YES_NO_CANCEL_OPTION);
                            if (anfrage == JOptionPane.YES_OPTION) {
                                zm.allefrei = false;
                                zm.allezuzahl = false;
                                zm.anfangfrei = true;
                                zm.teil1 = vAktTermine.size();
                                zm.teil2 = maxAnzahl() - vAktTermine.size();
                                iret = 1;
                            } else if (anfrage == JOptionPane.CANCEL_OPTION) {
                                return -1;
                            } else {
                                Object ret = JOptionPane.showInputDialog(null,
                                        "Geben Sie bitte die Anzahl Behandlungen ein für die\nRezeptgebühren berechnet werden sollen:",
                                        Integer.toString(maxAnzahl() - vAktTermine.size()));
                                if (ret == null) {
                                    // iret = 0;
                                    return -1;
                                } else {
                                    zm.allefrei = false;
                                    zm.allezuzahl = false;
                                    zm.anfangfrei = true;
                                    zm.teil1 = maxAnzahl() - Integer.parseInt((String) ret);
                                    zm.teil2 = Integer.parseInt((String) ret);
                                    iret = 1;
                                }
                            }
                        } else {
                            iret = 0;
                        }
                    } else {// gemischte Termine
                        zm.allefrei = false;
                        zm.allezuzahl = false;
                        zm.anfangfrei = true;
                        zm.teil1 = (Integer) obj[1];
                        zm.teil2 = (Integer) obj[2];
                        iret = 1;
                    }
                }
                break;
            }
            /* Aktuelle Befreiung aber nicht im Vorjahr */
            if (istBefreit && !warImVorjahrBefreit) {
                if (!bMitJahresWechsel) {// Alle Termine aktuell
                    iret = 0;
                } else {// es gibt Termine im Vorjahr
                    Object[] obj = JahresWechsel(vAktTermine, SystemConfig.vorJahr);
                    if (!(Boolean) obj[0]) {// alle Termine waren im Vorjahr
                        iret = 2;
                    } else {// gemischte Termine
                            // System.out.println("Termine aus dem Vorjahr(Zuzahlung) = "+obj[1]+" Termine
                            // aus diesem Jahr(frei) = "+obj[2]);
                        zm.allefrei = false;
                        zm.allezuzahl = false;
                        zm.anfangfrei = false;
                        zm.teil1 = (Integer) obj[1];
                        zm.teil2 = (Integer) obj[2];
                        iret = 3;
                    }
                }
                break;
            }
        }

        zm.hausbesuch = "T".equals(Reha.instance.patpanel.vecaktrez.get(43));
        zm.hbvoll = "T".equals(Reha.instance.patpanel.vecaktrez.get(61));
        zm.hbheim = "T".equals(Reha.instance.patpanel.patDaten.get(44));
        zm.km = StringTools.ZahlTest(Reha.instance.patpanel.patDaten.get(48));
        zm.preisgruppe = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(41));
        zm.gesamtZahl = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(64));
        if (iret == 0) {
            if (testefuerbarcode) {
                constructGanzFreiRezHMap(zm);
                constructNormalRezHMap(zm, false);
                SystemConfig.hmAdrRDaten.put("<Rendbetrag>", "0,00");
            } else {
                constructGanzFreiRezHMap(zm);
            }
        }
        if (iret == 1) {
            constructAnfangFreiRezHMap(zm, true);
        }
        if (iret == 2) {
            constructNormalRezHMap(zm, false);
        }
        if (iret == 3) {
            constructEndeFreiRezHMap(zm, false);
        }
        return iret;
    }

    private static void constructNormalRezHMap(ZuzahlModell zm, boolean unregelmaessig) {
        // System.out.println("*****In Normal HMap*********");
        Double rezgeb;
        BigDecimal[] preise = { null, null, null, null };
        BigDecimal xrezgeb = BigDecimal.valueOf(Double.valueOf(0.000));

        //// System.out.println("nach nullzuweisung " +xrezgeb.toString());
        int[] anzahl = { 0, 0, 0, 0 };
        int[] artdbeh = { 0, 0, 0, 0 };
        int i;

        BigDecimal einzelpreis = null;
        BigDecimal poswert = null;
        BigDecimal rezwert = BigDecimal.valueOf(Double.valueOf(0.000));
        BigDecimal preistest = null;
        String stmt = null;
        String meldung = null;
        DecimalFormat dfx = new DecimalFormat("0.00");
        String xdiszi = getDisziplinFromRezNr(Reha.instance.patpanel.vecaktrez.get(1));
        int xpreisgr = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(41)) - 1;
        String xrezdatum = DatFunk.sDatInDeutsch(Reha.instance.patpanel.vecaktrez.get(2));

        SystemConfig.hmAdrRDaten.put("<Rid>", Reha.instance.patpanel.vecaktrez.get(35));
        SystemConfig.hmAdrRDaten.put("<Rnummer>", Reha.instance.patpanel.vecaktrez.get(1));
        SystemConfig.hmAdrRDaten.put("<Rdatum>", DatFunk.sDatInDeutsch(Reha.instance.patpanel.vecaktrez.get(2)));
        boolean neuerpreis = neuePreisNachRezeptdatumOderStichtag(xdiszi, xpreisgr, xrezdatum, false,
                Reha.instance.patpanel.vecaktrez);
        // System.out.println("Neuer Preis = "+neuerpreis+"\n");

        for (i = 0; i < 4; i++) {
            anzahl[i] = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(i + 3));
            artdbeh[i] = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(i + 8));
            if (!neuerpreis) {
                if (artdbeh[i] > 0) {
                    preise[i] = BigDecimal.valueOf(Double.valueOf(getPreisAltFromID(Integer.toString(artdbeh[i]),
                            Integer.toString(xpreisgr), SystemPreislisten.hmPreise.get(xdiszi)
                                                                                  .get(xpreisgr))));
                } else {
                    preise[i] = BigDecimal.valueOf(Double.valueOf("0.00"));
                }
            } else {
                try {
                    if (artdbeh[i] > 0) {
                        preistest = BigDecimal.valueOf(Double.valueOf(Reha.instance.patpanel.vecaktrez.get(i + 18)));
                        preise[i] = BigDecimal.valueOf(Double.valueOf(getPreisAktFromID(Integer.toString(artdbeh[i]),
                                Integer.toString(xpreisgr), SystemPreislisten.hmPreise.get(xdiszi)
                                                                                      .get(xpreisgr))));
                        if (preistest.compareTo(preise[i]) != 0) {
                            meldung = "Achtung Unterschiedliche Preise!!!\n\n"
                                    + "Im Rezept gespeicherter Preis für Position "
                                    + Reha.instance.patpanel.vecaktrez.get(48 + i) + " = " + dfx.format(preistest)
                                    + "\n" + "In der Preisliste gespeicherter Preis für Position "
                                    + Reha.instance.patpanel.vecaktrez.get(48 + i) + " = " + dfx.format(preise[i])
                                    + "\n\n"
                                    + "Vermutete Ursache: Die Preisliste wurde nach der Rezeptanlage aktualisiert\n"
                                    + "Berechung erfolgt mit dem Preis aus der Preisliste, Rezept wird aktualisiert!";
                            JOptionPane.showMessageDialog(null, meldung);
                            stmt = "update verordn set preise" + (i + 1) + "='" + dfx.format(preise[i])
                                                                                     .replace(",", ".")
                                    + "' where id='" + Reha.instance.patpanel.vecaktrez.get(35) + "' LIMIT 1";
                            /// System.out.println(stmt);
                            SqlInfo.sqlAusfuehren(stmt);
                        }
                    } else {
                        preise[i] = BigDecimal.valueOf(Double.valueOf("0.00"));
                    }
                } catch (Exception ex) {
                    preise[i] = BigDecimal.valueOf(Double.valueOf(Reha.instance.patpanel.vecaktrez.get(i + 18)));
                }
            }
        }
        xrezgeb = xrezgeb.add(BigDecimal.valueOf(Double.valueOf(10.00)));
        rezgeb = 10.00;
        //// System.out.println("nach 10.00 zuweisung " +rezgeb.toString());
//        String runden;

        BigDecimal endpos;
        SystemConfig.hmAdrRDaten.put("<Rnummer>", Reha.instance.patpanel.vecaktrez.get(1));
        SystemConfig.hmAdrRDaten.put("<Rpatid>", Reha.instance.patpanel.vecaktrez.get(0));
        SystemConfig.hmAdrRDaten.put("<Rdatum>", DatFunk.sDatInDeutsch(Reha.instance.patpanel.vecaktrez.get(2)));
        SystemConfig.hmAdrRDaten.put("<Rpauschale>", dfx.format(rezgeb));

        for (i = 0; i < 4; i++) {
            /*
             * //System.out.println(Integer.valueOf(anzahl[i]).toString()+" / "+
             * Integer.valueOf(artdbeh[i]).toString()+" / "+ preise[i].toString() );
             */
            if (artdbeh[i] > 0) {
                SystemConfig.hmAdrRDaten.put("<Rposition" + (i + 1) + ">",
                        Reha.instance.patpanel.vecaktrez.get(48 + i));

                String id = Reha.instance.patpanel.vecaktrez.get(8 + i);
                SystemConfig.hmAdrRDaten.put("<Rlangtext" + (i + 1) + ">",
                        RezTools.getLangtextFromID(id, "", SystemPreislisten.hmPreise.get(xdiszi)
                                                                                     .get(xpreisgr)));

                SystemConfig.hmAdrRDaten.put("<Rkuerzel" + (i + 1) + ">",
                        RezTools.getKurzformFromID(id, SystemPreislisten.hmPreise.get(xdiszi)
                                                                                 .get(xpreisgr)));
                
                SystemConfig.hmAdrRDaten.put("<Rpreis" + (i + 1) + ">", dfx.format(preise[i]));

                einzelpreis = preise[i].divide(BigDecimal.valueOf(Double.valueOf(10.000)));

                poswert = preise[i].multiply(BigDecimal.valueOf(Double.valueOf(anzahl[i])));
                rezwert = rezwert.add(poswert);
                //// System.out.println("Einzelpreis "+i+" = "+einzelpreis);
                BigDecimal testpr = einzelpreis.setScale(2, BigDecimal.ROUND_HALF_UP);
                //// System.out.println("test->Einzelpreis "+i+" = "+testpr);

                SystemConfig.hmAdrRDaten.put("<Rproz" + (i + 1) + ">", dfx.format(testpr));
                SystemConfig.hmAdrRDaten.put("<Ranzahl" + (i + 1) + ">", Integer.valueOf(anzahl[i])
                                                                                .toString());

                endpos = testpr.multiply(BigDecimal.valueOf(Double.valueOf(anzahl[i])));
                SystemConfig.hmAdrRDaten.put("<Rgesamt" + (i + 1) + ">", dfx.format(endpos));
                rezgeb = rezgeb + endpos.doubleValue();
                //// System.out.println(rezgeb.toString());
            } else {
                SystemConfig.hmAdrRDaten.put("<Rposition" + (i + 1) + ">", "----");
                SystemConfig.hmAdrRDaten.put("<Rpreis" + (i + 1) + ">", "0,00");
                SystemConfig.hmAdrRDaten.put("<Rproz" + (i + 1) + ">", "0,00");
                SystemConfig.hmAdrRDaten.put("<Rgesamt" + (i + 1) + ">", "0,00");
                SystemConfig.hmAdrRDaten.put("<Ranzahl" + (i + 1) + ">", "----");
                SystemConfig.hmAdrRDaten.put("<Rkuerzel" + (i + 1) + ">", "");
            }
        }
        sortHmAdrRdaten();
        /*****************************************************/
        if (zm.hausbesuch) { // Hausbesuch
            Object[] obi = hbNormal(zm, rezwert, rezgeb, Integer.valueOf(Reha.instance.patpanel.vecaktrez.get(64)),
                    neuerpreis);
            rezwert = (BigDecimal) obi[0];
            rezgeb = (Double) obi[1];
        }

        Double drezwert = rezwert.doubleValue();
        SystemConfig.hmAdrRDaten.put("<Rendbetrag>", dfx.format(rezgeb));
        SystemConfig.hmAdrRDaten.put("<Rwert>", dfx.format(drezwert));
        DecimalFormat df = new DecimalFormat("0.00");
        df.format(rezgeb);
        //// System.out.println("----------------------------------------------------");
        //// System.out.println("Endgültige und geparste Rezeptgebühr = "+s+" EUR");
        //// System.out.println(SystemConfig.hmAdrRDaten);

        // Hier muß noch Hausbesuchshandling eingebaut werden
        // Ebenso das Wegegeldhandling
    }

    public static void constructRawHMap() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    DecimalFormat df = new DecimalFormat("0.00");
                    String diszi = getDisziplinFromRezNr(Reha.instance.patpanel.vecaktrez.get(1));

                    int pg = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(41)) - 1;
                    String id = "";
                    SystemConfig.hmAdrRDaten.put("<Rid>", Reha.instance.patpanel.vecaktrez.get(35));
                    SystemConfig.hmAdrRDaten.put("<Rnummer>", Reha.instance.patpanel.vecaktrez.get(1));
                    SystemConfig.hmAdrRDaten.put("<Rdatum>",
                            DatFunk.sDatInDeutsch(Reha.instance.patpanel.vecaktrez.get(2)));
                    SystemConfig.hmAdrRDaten.put("<Rdiagnose>", Reha.instance.patpanel.vecaktrez.get(23));

                    BigDecimal dummyproz = null;
                    BigDecimal roundproz = null;
                    for (int i = 0; i < 4; i++) {
                        id = Reha.instance.patpanel.vecaktrez.get(8 + i);
                        SystemConfig.hmAdrRDaten.put("<Rposition" + (i + 1) + ">",
                                Reha.instance.patpanel.vecaktrez.get(48 + i));
                        SystemConfig.hmAdrRDaten.put("<Rpreis" + (i + 1) + ">",
                                Reha.instance.patpanel.vecaktrez.get(18 + i)
                                                                .replace(".", ","));
                        SystemConfig.hmAdrRDaten.put("<Ranzahl" + (i + 1) + ">",
                                Reha.instance.patpanel.vecaktrez.get(3 + i));
                        // SystemConfig.hmAdrRDaten.put("<Rgesamt"+(i+1)+">", df.format(
                        // ((BigDecimal)BigDecimal.valueOf(Double.valueOf(SystemConfig.hmAdrRDaten.get("<Ranzahl"+(i+1)+">"))).multiply(BigDecimal.valueOf(Double.valueOf(SystemConfig.hmAdrRDaten.get("<Rpreis"+(i+1)+">").replace(",","."))))).doubleValue()
                        // ));
                        dummyproz = BigDecimal.valueOf(Double.valueOf(Reha.instance.patpanel.vecaktrez.get(18 + i)))
                                              .divide(BigDecimal.valueOf(Double.valueOf(10.000)));
                        roundproz = dummyproz.setScale(2, BigDecimal.ROUND_HALF_UP);
                        SystemConfig.hmAdrRDaten.put("<Rgesamt" + (i + 1) + ">",
                                df.format(roundproz
                                                   .multiply(BigDecimal.valueOf(Double.valueOf(
                                                           SystemConfig.hmAdrRDaten.get("<Ranzahl" + (i + 1) + ">"))))
                                                   .doubleValue()));
                        if (!"0".equals(id)) {
                            SystemConfig.hmAdrRDaten.put("<Rkuerzel" + (i + 1) + ">",
                                    getKurzformFromID(id, SystemPreislisten.hmPreise.get(diszi)
                                                                                    .get(pg)));
                            SystemConfig.hmAdrRDaten.put("<Rlangtext" + (i + 1) + ">",
                                    getLangtextFromID(id, "", SystemPreislisten.hmPreise.get(diszi)
                                                                                        .get(pg)));
                        } else {
                            SystemConfig.hmAdrRDaten.put("<Rkuerzel" + (i + 1) + ">", "");
                            SystemConfig.hmAdrRDaten.put("<Rlangtext" + (i + 1) + ">", "");
                        }
                    }
                    sortHmAdrRdaten();
                    // Hausbesuche
                    if ("T".equals(Reha.instance.patpanel.vecaktrez.get(43))) {
                        SystemConfig.hmAdrRDaten.put("<Rhbpos>", SystemPreislisten.hmHBRegeln.get(diszi)
                                                                                             .get(pg)
                                                                                             .get(0));
                        SystemConfig.hmAdrRDaten.put("<Rhbanzahl>", Reha.instance.patpanel.vecaktrez.get(64));
                        SystemConfig.hmAdrRDaten.put("<Rhbpreis>",
                                getPreisAktFromPos(SystemConfig.hmAdrRDaten.get("<Rhbpos>"), "",
                                        SystemPreislisten.hmPreise.get(diszi)
                                                                  .get(pg)).replace(".", ","));
                        SystemConfig.hmAdrRDaten.put("<Rwegpos>", SystemPreislisten.hmHBRegeln.get(diszi)
                                                                                              .get(pg)
                                                                                              .get(2));
                        SystemConfig.hmAdrRDaten.put("<Rweganzahl>", Reha.instance.patpanel.vecaktrez.get(7));
                        SystemConfig.hmAdrRDaten.put("<Rwegpreis>",
                                getPreisAktFromPos(SystemConfig.hmAdrRDaten.get("<Rwegpos>"), "",
                                        SystemPreislisten.hmPreise.get(diszi)
                                                                  .get(pg)).replace(".", ","));
                    } else {
                        SystemConfig.hmAdrRDaten.put("<Rhbpos>", "");
                        SystemConfig.hmAdrRDaten.put("<Rhbanzahl>", "");
                        SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "");
                        SystemConfig.hmAdrRDaten.put("<Rwegpos>", "");
                        SystemConfig.hmAdrRDaten.put("<Rweganzahl>", "");
                        SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                // System.out.println(SystemConfig.hmAdrRDaten);
                return null;
            }
        }.execute();
    }

    private static void constructGanzFreiRezHMap(ZuzahlModell zm) {
        SystemConfig.hmAdrRDaten.put("<Rid>", Reha.instance.patpanel.vecaktrez.get(35));
        SystemConfig.hmAdrRDaten.put("<Rnummer>", Reha.instance.patpanel.vecaktrez.get(1));
        SystemConfig.hmAdrRDaten.put("<Rdatum>", DatFunk.sDatInDeutsch(Reha.instance.patpanel.vecaktrez.get(2)));
        SystemConfig.hmAdrRDaten.put("<Rpatid>", Reha.instance.patpanel.vecaktrez.get(0));
        SystemConfig.hmAdrRDaten.put("<Rpauschale>", "0,00");
        for (int i = 0; i < 5; i++) {
            SystemConfig.hmAdrRDaten.put("<Rposition" + (i + 1) + ">", "----");
            SystemConfig.hmAdrRDaten.put("<Rpreis" + (i + 1) + ">", "0,00");
            SystemConfig.hmAdrRDaten.put("<Rproz" + (i + 1) + ">", "0,00");
            SystemConfig.hmAdrRDaten.put("<Rgesamt" + (i + 1) + ">", "0,00");
            SystemConfig.hmAdrRDaten.put("<Ranzahl" + (i + 1) + ">", "----");
            SystemConfig.hmAdrRDaten.put("<Rkuerzel" + (i + 1) + ">", "");
        }
        SystemConfig.hmAdrRDaten.put("<Rhbpos>", "----");
        SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
        SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
        SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
        SystemConfig.hmAdrRDaten.put("<Rwegpos>", "----");
        SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
        SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
        SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
        SystemConfig.hmAdrRDaten.put("<Rendbetrag>", "0,00");
        SystemConfig.hmAdrRDaten.put("<Rwert>", "0,00");
    }

    private static void constructAnfangFreiRezHMap(ZuzahlModell zm, boolean anfang) {
        try {
            // System.out.println("*****In Anfang-frei*********");
            if (anfang) {
                zm.gesamtZahl = zm.teil2;
                // System.out.println("Restliche Behandlungen berechnen = "+zm.gesamtZahl);
            } else {
                zm.gesamtZahl = zm.teil1;
                // System.out.println("Beginn der Behandlung berechnen = "+zm.gesamtZahl);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Double rezgeb = 0.000;
        BigDecimal[] preise = { null, null, null, null };
        BigDecimal xrezgeb = BigDecimal.valueOf(Double.valueOf(0.000));

        String xdiszi = getDisziplinFromRezNr(Reha.instance.patpanel.vecaktrez.get(1));
        int xpreisgr = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(41)) - 1;
        String xrezdatum = DatFunk.sDatInDeutsch(Reha.instance.patpanel.vecaktrez.get(2));
        boolean neuerpreis = neuePreisNachRezeptdatumOderStichtag(xdiszi, xpreisgr, xrezdatum, false,
                Reha.instance.patpanel.vecaktrez);

        //// System.out.println("nach nullzuweisung " +xrezgeb.toString());
        int[] anzahl = { 0, 0, 0, 0 };
        int[] artdbeh = { 0, 0, 0, 0 };
// Einbauen für Barcode
        int[] gesanzahl = { 0, 0, 0, 0 };
        int i;
        BigDecimal einzelpreis = null;
        BigDecimal poswert = null;
        BigDecimal rezwert = BigDecimal.valueOf(Double.valueOf(0.000));
        SystemConfig.hmAdrRDaten.put("<Rid>", Reha.instance.patpanel.vecaktrez.get(35));
        SystemConfig.hmAdrRDaten.put("<Rnummer>", Reha.instance.patpanel.vecaktrez.get(1));
        SystemConfig.hmAdrRDaten.put("<Rdatum>", Reha.instance.patpanel.vecaktrez.get(2));
        for (i = 0; i < 4; i++) {
            gesanzahl[i] = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(i + 3));
            anzahl[i] = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(i + 3));
            if (anzahl[i] >= zm.gesamtZahl) {
                anzahl[i] = zm.gesamtZahl;
            }
            artdbeh[i] = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(i + 8));
            if (!neuerpreis) {
                if (artdbeh[i] > 0) {
                    preise[i] = BigDecimal.valueOf(Double.valueOf(getPreisAltFromID(Integer.toString(artdbeh[i]),
                            Integer.toString(xpreisgr), SystemPreislisten.hmPreise.get(xdiszi)
                                                                                  .get(xpreisgr))));
                } else {
                    preise[i] = BigDecimal.valueOf(Double.valueOf("0.00"));
                }
            } else {
                preise[i] = BigDecimal.valueOf(Double.valueOf(Reha.instance.patpanel.vecaktrez.get(i + 18)));
            }
            // preise[i] = BigDecimal.valueOf(new
            // Double((String)Reha.instance.patpanel.vecaktrez.get(i+18)));
        }
        xrezgeb = xrezgeb.add(BigDecimal.valueOf(Double.valueOf(10.00)));
        if (anfang) {
            rezgeb = 00.00;
        } else {
            rezgeb = 10.00;
        }

        //// System.out.println("nach 10.00 zuweisung " +rezgeb.toString());
        // String runden;
        DecimalFormat dfx = new DecimalFormat("0.00");
        BigDecimal endpos;
        SystemConfig.hmAdrRDaten.put("<Rnummer>", Reha.instance.patpanel.vecaktrez.get(1));
        SystemConfig.hmAdrRDaten.put("<Rpatid>", Reha.instance.patpanel.vecaktrez.get(0));
        SystemConfig.hmAdrRDaten.put("<Rdatum>", DatFunk.sDatInDeutsch(Reha.instance.patpanel.vecaktrez.get(2)));
        SystemConfig.hmAdrRDaten.put("<Rpauschale>", dfx.format(rezgeb));

        for (i = 0; i < 4; i++) {
            /*
             * //System.out.println(Integer.valueOf(anzahl[i]).toString()+" / "+
             * Integer.valueOf(artdbeh[i]).toString()+" / "+ preise[i].toString() );
             */
            if (artdbeh[i] > 0) {
                SystemConfig.hmAdrRDaten.put("<Rposition" + (i + 1) + ">",
                        Reha.instance.patpanel.vecaktrez.get(48 + i));
                SystemConfig.hmAdrRDaten.put("<Rpreis" + (i + 1) + ">", dfx.format(preise[i]));

                einzelpreis = preise[i].divide(BigDecimal.valueOf(Double.valueOf(10.000)));
                // ***********vorher nur anzahl[]*****************/
                poswert = preise[i].multiply(BigDecimal.valueOf(Double.valueOf(gesanzahl[i])));
                rezwert = rezwert.add(poswert);
                //// System.out.println("Einzelpreis "+i+" = "+einzelpreis);
                BigDecimal testpr = einzelpreis.setScale(2, BigDecimal.ROUND_HALF_UP);
                //// System.out.println("test->Einzelpreis "+i+" = "+testpr);

                SystemConfig.hmAdrRDaten.put("<Rproz" + (i + 1) + ">", dfx.format(testpr));
                SystemConfig.hmAdrRDaten.put("<Ranzahl" + (i + 1) + ">", Integer.toString(anzahl[i]));

                endpos = testpr.multiply(BigDecimal.valueOf(Double.valueOf(anzahl[i])));
                SystemConfig.hmAdrRDaten.put("<Rgesamt" + (i + 1) + ">", dfx.format(endpos));
                rezgeb = rezgeb + endpos.doubleValue();
                //// System.out.println(rezgeb.toString());
            } else {
                SystemConfig.hmAdrRDaten.put("<Rposition" + (i + 1) + ">", "----");
                SystemConfig.hmAdrRDaten.put("<Rpreis" + (i + 1) + ">", "0,00");
                SystemConfig.hmAdrRDaten.put("<Rproz" + (i + 1) + ">", "0,00");
                SystemConfig.hmAdrRDaten.put("<Rgesamt" + (i + 1) + ">", "0,00");
                SystemConfig.hmAdrRDaten.put("<Ranzahl" + (i + 1) + ">", "----");
            }
            sortHmAdrRdaten();
        }
        if (zm.hausbesuch) { // Hausbesuch
            if (zm.gesamtZahl > Integer.valueOf(Reha.instance.patpanel.vecaktrez.get(64))) {
                zm.gesamtZahl = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(64));
            }
            Object[] obi = hbNormal(zm, rezwert, rezgeb, Integer.valueOf(Reha.instance.patpanel.vecaktrez.get(64)),
                    neuerpreis);
            rezwert = (BigDecimal) obi[0];
            rezgeb = (Double) obi[1];
        }
        Double drezwert = rezwert.doubleValue();
        SystemConfig.hmAdrRDaten.put("<Rendbetrag>", dfx.format(rezgeb));
        SystemConfig.hmAdrRDaten.put("<Rwert>", dfx.format(drezwert));
        DecimalFormat df = new DecimalFormat("0.00");
        df.format(rezgeb);
        // System.out.println("----------------------------------------------------");
        // System.out.println("Endgültige und geparste Rezeptgebühr = "+s+" EUR");
        //// System.out.println(SystemConfig.hmAdrRDaten);
    }

    private static void sortHmAdrRdaten() {
        int onceAgain = 0;
        HashMap<String, String> voData = SystemConfig.hmAdrRDaten;
        
        do {
            onceAgain = 0;
            for (int i = 1; i < 4; i++) {
                String sCurr = SystemConfig.hmAdrRDaten.get("<Rposition" + (i) + ">");
                String sNext = SystemConfig.hmAdrRDaten.get("<Rposition" + (i + 1) + ">");
                if (sCurr.equals("----") && !sNext.equals("----")) {
                    // akt. Platz ist leer; Nachfolger nicht -> Plaetze tauschen
                    String currIdx = String.valueOf(i);
                    String nextIdx = String.valueOf(1 + i);
                    String sTmp = SystemConfig.hmAdrRDaten.get("<Rposition" + currIdx + ">");
                    String sTmp2 = SystemConfig.hmAdrRDaten.get("<Rposition" + nextIdx + ">");
                    SystemConfig.hmAdrRDaten.put("<Rposition" + currIdx + ">", sTmp2);
                    SystemConfig.hmAdrRDaten.put("<Rposition" + nextIdx + ">", sTmp);

                    sTmp = SystemConfig.hmAdrRDaten.get("<Rpreis" + currIdx + ">");
                    sTmp2 = SystemConfig.hmAdrRDaten.get("<Rpreis" + nextIdx + ">");
                    SystemConfig.hmAdrRDaten.put("<Rpreis" + currIdx + ">", sTmp2);
                    SystemConfig.hmAdrRDaten.put("<Rpreis" + nextIdx + ">", sTmp);

                    sTmp = SystemConfig.hmAdrRDaten.get("<Rproz" + currIdx + ">");
                    sTmp2 = SystemConfig.hmAdrRDaten.get("<Rproz" + nextIdx + ">");
                    SystemConfig.hmAdrRDaten.put("<Rproz" + currIdx + ">", sTmp2);
                    SystemConfig.hmAdrRDaten.put("<Rproz" + nextIdx + ">", sTmp);

                    sTmp = SystemConfig.hmAdrRDaten.get("<Rgesamt" + currIdx + ">");
                    sTmp2 = SystemConfig.hmAdrRDaten.get("<Rgesamt" + nextIdx + ">");
                    SystemConfig.hmAdrRDaten.put("<Rgesamt" + currIdx + ">", sTmp2);
                    SystemConfig.hmAdrRDaten.put("<Rgesamt" + nextIdx + ">", sTmp);

                    sTmp = SystemConfig.hmAdrRDaten.get("<Ranzahl" + currIdx + ">");
                    sTmp2 = SystemConfig.hmAdrRDaten.get("<Ranzahl" + nextIdx + ">");
                    SystemConfig.hmAdrRDaten.put("<Ranzahl" + currIdx + ">", sTmp2);
                    SystemConfig.hmAdrRDaten.put("<Ranzahl" + nextIdx + ">", sTmp);

                    sTmp = SystemConfig.hmAdrRDaten.get("<Rkuerzel" + currIdx + ">");
                    sTmp2 = SystemConfig.hmAdrRDaten.get("<Rkuerzel" + nextIdx + ">");
                    SystemConfig.hmAdrRDaten.put("<Rkuerzel" + currIdx + ">", sTmp2);
                    SystemConfig.hmAdrRDaten.put("<Rkuerzel" + nextIdx + ">", sTmp);

                    sTmp = SystemConfig.hmAdrRDaten.get("<Rlangtext" + currIdx + ">");
                    sTmp2 = SystemConfig.hmAdrRDaten.get("<Rlangtext" + nextIdx + ">");
                    SystemConfig.hmAdrRDaten.put("<Rlangtext" + currIdx + ">", sTmp2);
                    SystemConfig.hmAdrRDaten.put("<Rlangtext" + nextIdx + ">", sTmp);
                    
                    onceAgain++;
                }
            }
        } while (onceAgain > 0);
    }

    public static void constructEndeFreiRezHMap(ZuzahlModell zm, boolean anfang) {
        // System.out.println("*****Über Ende Frei*********");
        constructAnfangFreiRezHMap(zm, anfang);
    }

    public static Vector<Vector<String>> splitteTermine(String terms) {
        Vector<Vector<String>> termine = new Vector<>();
        String[] tlines = terms.split("\n");
        int lines = tlines.length;
        //// System.out.println("Anzahl Termine = "+lines);
        Vector<String> tvec = new Vector<>();
        String[] terdat = null;
        for (int i = 0; i < lines; i++) {
            terdat = tlines[i].split("@");
            int ieinzel = terdat.length;
            if (ieinzel <= 1) {
                return (Vector<Vector<String>>) termine.clone();
            }
            //// System.out.println("Anzahl Splits = "+ieinzel);
            tvec.clear();
            for (int y = 0; y < ieinzel; y++) {
                tvec.add("".equals(terdat[y].trim()) ? "  .  .    " : terdat[y]);
            }
            termine.add((Vector<String>) tvec.clone());
        }
        return (Vector<Vector<String>>) termine.clone();
    }

    public static Object[] JahrEnthalten(Vector<String> vtage, String jahr) {
        Object[] ret = { Boolean.FALSE, -1 };
        for (int i = 0; i < vtage.size(); i++) {
            if (vtage.get(i)
                     .equals(jahr)) {
                ret[0] = true;
                ret[1] = Integer.valueOf(i);
                break;
            }
        }
        return ret;
    }

    private static Object[] JahresWechsel(Vector<String> vtage, String jahr) {
        Object[] ret = { Boolean.FALSE, -1, -1 };
        for (int i = 0; i < vtage.size(); i++) {
            if (!vtage.get(i)
                      .substring(6)
                      .equals(jahr)) {
                ret[0] = true;
                ret[1] = Integer.valueOf(i);
                ret[2] = maxAnzahl() - (Integer) ret[1];
                return ret;
            }
        }
        /*
         * if(maxAnzahl() > vtage.size()){ ret[0] = true; ret[1] =
         * Integer.valueOf(vtage.size()); ret[2] = maxAnzahl()-(Integer)ret[1]; }
         * System.out.println("maximale Anzahl "+maxAnzahl());
         */
        return ret;
    }

    private static int maxAnzahl() {
        int ret = -1;
        int test;
        for (int i = 3; i < 7; i++) {
            test = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(i));
            if (test > ret) {
                ret = test;
            }
        }
        return ret;
    }

    private static String PreisUeberPosition(String position, int preisgruppe, String disziplin, boolean neu) {
        // JOptionPane.showMessageDialog(null, "Aufruf der Funktion
        // PreisUeberPosition");
        String ret = null;
        Vector<?> preisvec;
        preisvec = SystemPreislisten.hmPreise.get(getDisziplinFromRezNr(disziplin))
                                             .get(preisgruppe - 1);
        for (int i = 0; i < preisvec.size(); i++) {
            if (((String) ((Vector<?>) preisvec.get(i)).get(2)).equals(position)) {
//                //System.out.println("Der Preis von "+position+" = "+ret);
                return (String) ((Vector<?>) preisvec.get(i)).get(3 + (neu ? 0 : 1));
            }
        }
        //// System.out.println("Der Preis von "+position+" wurde nicht gefunden!!");
        return ret;
    }

    public static String getDisziplinFromRezNr(String reznr) {
        String diszi = reznr.substring(0, 2);
        return Disziplin.valueOf(diszi).medium;
    }

    public static Object[] ermittleHBwert(Vector<String> vec) {
        Object[] retobj = { null, null, null };
        String disziplin = getDisziplinFromRezNr(vec.get(1));
        String pos = "";
        Double preis = 0.00;
        Double wgkm = "".equals(vec.get(7)) ? 0.00 : Double.parseDouble(vec.get(7));

        String pospauschale = "";
        Double preispauschale = 0.00;
        // Double wgpauschal = 0.00;
        // erst testen ob HB-Einzeln oder HB-Mehrere
        int anzahl = Integer.parseInt(vec.get(64));
        int preisgruppe = Integer.parseInt(vec.get(41));
        if ("T".equals(vec.get(61))) {
            // Einzelhausbesuch
            pos = SystemPreislisten.hmHBRegeln.get(disziplin)
                                              .get(preisgruppe - 1)
                                              .get(0);
            preis = Double.parseDouble(getPreisAktFromPos(pos, Integer.toString(preisgruppe),
                    SystemPreislisten.hmPreise.get(disziplin)
                                              .get(preisgruppe - 1)));
            retobj[0] = BigDecimal.valueOf(preis)
                                  .multiply(BigDecimal.valueOf(Double.parseDouble(Integer.toString(anzahl))))
                                  .doubleValue();
            // testen ob Fahrtgeldüberhaupt gezahlt wird;
            if (keineWeggebuehrBeiHB(disziplin, Integer.toString(preisgruppe))) {
                return retobj;
            }
            if (zweiPositionenBeiHB(disziplin, Integer.toString(preisgruppe))) {
                // Weggebühr und pauschale
                /*
                 * In Betrieb bis 26.11.2010 ***************** if(
                 * (wgkm=Double.parseDouble(vec.get(7))) > 7 ){ //Kilometer verwenden pos =
                 * SystemPreislisten.hmHBRegeln.get(disziplin).get(preisgruppe-1).get(2); preis
                 * = Double.parseDouble(RezTools.getPreisAktFromPos(pos,
                 * Integer.toString(preisgruppe),
                 * SystemPreislisten.hmPreise.get(disziplin).get(preisgruppe-1))); BigDecimal
                 * kms =
                 * BigDecimal.valueOf(preis).multiply(BigDecimal.valueOf(Double.parseDouble(
                 * Integer.toString(anzahl)))); kms = kms.multiply(BigDecimal.valueOf(wgkm));
                 * retobj[1] = kms.doubleValue(); return retobj; }else{ //Pauschale verwenden
                 * pos = SystemPreislisten.hmHBRegeln.get(disziplin).get(preisgruppe-1).get(3);
                 * preis = Double.parseDouble(RezTools.getPreisAktFromPos(pos,
                 * Integer.toString(preisgruppe),
                 * SystemPreislisten.hmPreise.get(disziplin).get(preisgruppe-1))); retobj[1] =
                 * BigDecimal.valueOf(preis).multiply(BigDecimal.valueOf(Double.parseDouble(
                 * Integer.toString(anzahl)))).doubleValue(); return retobj; }
                 */
                pos = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                  .get(preisgruppe - 1)
                                                  .get(2);
                preis = Double.parseDouble(getPreisAktFromPos(pos, Integer.toString(preisgruppe),
                        SystemPreislisten.hmPreise.get(disziplin)
                                                  .get(preisgruppe - 1)));
                BigDecimal kms = BigDecimal.valueOf(preis)
                                           .multiply(BigDecimal.valueOf(Double.parseDouble(Integer.toString(anzahl))));
                kms = kms.multiply(BigDecimal.valueOf(wgkm));

                pospauschale = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                           .get(preisgruppe - 1)
                                                           .get(3);
                preispauschale = Double.parseDouble(getPreisAktFromPos(pospauschale, Integer.toString(preisgruppe),
                        SystemPreislisten.hmPreise.get(disziplin)
                                                  .get(preisgruppe - 1)));
                // System.out.println("kms="+kms);
                // System.out.println(BigDecimal.valueOf(preispauschale).multiply(BigDecimal.valueOf(Double.parseDouble(Integer.toString(anzahl)))));
                // System.out.println("pospauschale="+pospauschale);
                // System.out.println("preispauschale="+preispauschale);
                if (kms.doubleValue() > BigDecimal.valueOf(preispauschale)
                                                  .multiply(BigDecimal.valueOf(
                                                          Double.parseDouble(Integer.toString(anzahl))))
                                                  .doubleValue()) {
                    retobj[1] = kms.doubleValue();
                } else {
                    retobj[1] = BigDecimal.valueOf(preispauschale)
                                          .multiply(BigDecimal.valueOf(Double.parseDouble(Integer.toString(anzahl))))
                                          .doubleValue();
                }
                return retobj;
            }
        } else {
            // Mehrere Hausbesuch
            pos = SystemPreislisten.hmHBRegeln.get(disziplin)
                                              .get(preisgruppe - 1)
                                              .get(1);
            preis = Double.parseDouble(getPreisAktFromPos(pos, Integer.toString(preisgruppe),
                    SystemPreislisten.hmPreise.get(disziplin)
                                              .get(preisgruppe - 1)));
            retobj[0] = BigDecimal.valueOf(preis)
                                  .multiply(BigDecimal.valueOf(Double.parseDouble(Integer.toString(anzahl))))
                                  .doubleValue();
        }

        return retobj;
    }

    public static Vector<String> macheUmsatzZeile(Vector<Vector<String>> vec, String tag, String kaluser) {
        Vector<String> retvec = new Vector<>();
        for (int i = 0; i < 13; i++) {
            retvec.add("");
        }

        String disziplin = getDisziplinFromRezNr(vec.get(0)
                                                    .get(1));
        String pos = "";
        String preis = "";
        String pospauschale = "";

        String preispauschale = "";
        int preisgruppe = Integer.parseInt(vec.get(0)
                                              .get(41));
        String termine = vec.get(0)
                            .get(34);
        boolean rezept = false;
        Double wgkm;
        int fehlerstufe = 0;
        int ipos = 0;
        String kform = "";
        String[] posbestaetigt = null;
        Object[][] preisobj = { { null, null, null, null }, { null, null, null, null } };
        // 1. Termine aus Rezept holen
        String bestaetigte = vec.get(0)
                                .get(34);
        // 2. Testen ob der Tag erfaßt wenn nicht weiter mit der vollen Packung +
        // Fehlerstufe 1
        if (!termine.contains(tag)) {
            fehlerstufe = 1;
        } else {
            // 3. Sofern der Tagin der Termintabelle vorhanden ist,
            // die Positionen ermitteln
            // wenn keine Positionen vorhanden, weiter mit voller Packung + Fehlerstufe 2
            posbestaetigt = bestaetigte.substring(bestaetigte.indexOf(tag))
                                       .split("@")[3].split(",");
            if ("".equals(posbestaetigt[0].trim())) {
                fehlerstufe = 2;
            }
        }
        // 4. Überprüfen ob die Positionen in der Tarifgruppe existieren,
        // sofern nicht, Preise und Positionen aus Rezept entnehmen, also volle Packung
        // + Fehlerstufe 3
        if (fehlerstufe == 0) {
            for (int j = 0; j < posbestaetigt.length; j++) {
                if (!"".equals(posbestaetigt[j].trim())) {
                    if ("".equals(kform = getKurzformFromPos(posbestaetigt[j].trim(), Integer.toString(preisgruppe),
                            SystemPreislisten.hmPreise.get(disziplin)
                                                      .get(preisgruppe == 0 ? 0 : preisgruppe - 1)))) {
                        fehlerstufe = 3;
                        break;
                    }
                    preisobj[0][j] = kform;
                    preisobj[1][j] = getPreisAktFromPos(posbestaetigt[j], Integer.toString(preisgruppe),
                            SystemPreislisten.hmPreise.get(disziplin)
                                                      .get(preisgruppe == 0 ? 0 : preisgruppe - 1));
                }
            }
        }

        if (fehlerstufe == 0) {
            // 5. Wenn hier angekommen die Preise und Positionen aus der Preisliste
            // entnehmen
            for (int j = 0; j < 4; j++) {
                retvec.set(j, String.valueOf(preisobj[0][j] != null ? preisobj[0][j] : "-----"));
                retvec.set(j + 6, String.valueOf(preisobj[1][j] != null ? preisobj[1][j] : "0.00"));
            }
            retvec.set(12, "0");
        } else {
            for (int i = 0; i < 4; i++) {
                if (!"0".equals(vec.get(0)
                                   .get(i + 8)
                                   .trim())) {
                    pos = getKurzformFromID(vec.get(0)
                                               .get(i + 8)
                                               .trim(),
                            SystemPreislisten.hmPreise.get(disziplin)
                                                      .get(preisgruppe == 0 ? 0 : preisgruppe - 1));
                    if ("".equals(pos.trim())) {
                        pos = getKurzformFromPos(vec.get(0)
                                                    .get(i + 48)
                                                    .trim(),
                                Integer.toString(preisgruppe), SystemPreislisten.hmPreise.get(disziplin)
                                                                                         .get(preisgruppe == 0 ? 0
                                                                                                 : preisgruppe - 1));
                    }
                    retvec.set(i, pos);
                    retvec.set(i + 6, vec.get(0)
                                         .get(i + 18)
                                         .trim());
                } else {
                    retvec.set(i, "-----");
                    retvec.set(i + 6, "0.00");
                }
            }
            retvec.set(12, Integer.toString(fehlerstufe));
        }
        // mit Hausbesuch?
        if ("T".equals(vec.get(0)
                          .get(43))) {
            // Hausbesuch einzeln?
            if ("T".equals(vec.get(0)
                              .get(61))) {
                pos = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                  .get(preisgruppe == 0 ? 0 : preisgruppe - 1)
                                                  .get(0);
                preis = getPreisAktFromPos(pos, Integer.toString(preisgruppe), SystemPreislisten.hmPreise.get(disziplin)
                                                                                                         .get(preisgruppe == 0
                                                                                                                 ? 0
                                                                                                                 : preisgruppe
                                                                                                                         - 1));
                retvec.set(4, pos);
                retvec.set(10, preis);

                if (!keineWeggebuehrBeiHB(disziplin, Integer.toString(preisgruppe == 0 ? 1 : preisgruppe))) {
                    //// System.out.println("Kasse kennt Weggebühr...");
                    if (zweiPositionenBeiHB(disziplin, Integer.toString(preisgruppe == 0 ? 1 : preisgruppe))) {
                        // Weggebühr und pauschale
                        //// System.out.println("Kasse kennt km und Pauschale...");
                        pos = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                          .get(preisgruppe == 0 ? 0 : preisgruppe - 1)
                                                          .get(2);
                        pospauschale = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                                   .get(preisgruppe == 0 ? 0 : preisgruppe - 1)
                                                                   .get(3);
                        wgkm = Double.parseDouble(vec.get(0)
                                                     .get(7));

                        if (kmBesserAlsPauschale(pospauschale, pos, wgkm, preisgruppe, disziplin)) {
                            // if( (wgkm=Double.parseDouble(vec.get(0).get(7))) > 7 ){
                            // Kilometer verwenden
                            //// System.out.println("Kilometer verwenden...");
                            pos = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                              .get(preisgruppe == 0 ? 0 : preisgruppe - 1)
                                                              .get(2);
                            preis = getPreisAktFromPos(pos, Integer.toString(preisgruppe),
                                    SystemPreislisten.hmPreise.get(disziplin)
                                                              .get(preisgruppe == 0 ? 0 : preisgruppe - 1));
                            BigDecimal kms = BigDecimal.valueOf(Double.parseDouble(preis))
                                                       .multiply(BigDecimal.valueOf(wgkm));
                            retvec.set(5, pos);
                            retvec.set(11, Double.toString(kms.doubleValue()));
                            //// System.out.println("Pos = "+pos);
                            //// System.out.println("Preis = "+preis);
                        } else {
                            // Pauschale verwenden
                            //// System.out.println("Pauschale verwenden....");
                            pospauschale = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                                       .get(preisgruppe == 0 ? 0 : preisgruppe - 1)
                                                                       .get(3);
                            preis = getPreisAktFromPos(pospauschale, Integer.toString(preisgruppe),
                                    SystemPreislisten.hmPreise.get(disziplin)
                                                              .get(preisgruppe == 0 ? 0 : preisgruppe - 1));
                            // System.out.println("Pos = "+pos);
                            // System.out.println("Preis = "+preis);
                            retvec.set(5, pospauschale);
                            retvec.set(11, preis);
                        }
                    }
                } else {
                    // System.out.println("Kasse kennt keine Weggebühr....");
                    retvec.set(5, "-----");
                    retvec.set(11, "0.00");
                }
            } else {
                // Hausbesuch mit
                pos = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                  .get(preisgruppe == 0 ? 0 : preisgruppe - 1)
                                                  .get(1);
                preis = getPreisAktFromPos(pos, Integer.toString(preisgruppe), SystemPreislisten.hmPreise.get(disziplin)
                                                                                                         .get(preisgruppe == 0
                                                                                                                 ? 0
                                                                                                                 : preisgruppe
                                                                                                                         - 1));
                retvec.set(4, pos);
                retvec.set(10, preis);
                retvec.set(5, "-----");
                retvec.set(11, "0.00");
            }
        } else {
            retvec.set(4, "-----");
            retvec.set(10, "0.00");
            retvec.set(5, "-----");
            retvec.set(11, "0.00");
        }
        return retvec;
    }

    public static boolean kmBesserAlsPauschale(String pospauschal, String poskm, Double anzahlkm, int preisgruppe,
            String disziplin) {
        String meldung = "";
        try {
            String preiskm;
            String preispauschal;
            meldung = " Pospauschal = " + pospauschal + "\n" + "PosKilometer = " + poskm + "\n" + "   Anzahl km = "
                    + anzahlkm + "\n" + " Preisgruppe = " + preisgruppe + "\n" + "   Disziplin = " + disziplin;
            preiskm = getPreisAktFromPos(poskm, Integer.toString(preisgruppe), SystemPreislisten.hmPreise.get(disziplin)
                                                                                                         .get(preisgruppe == 0
                                                                                                                 ? 0
                                                                                                                 : preisgruppe
                                                                                                                         - 1));
            BigDecimal kms = BigDecimal.valueOf(Double.parseDouble(preiskm))
                                       .multiply(BigDecimal.valueOf(anzahlkm));
            preispauschal = getPreisAktFromPos(pospauschal, Integer.toString(preisgruppe),
                    SystemPreislisten.hmPreise.get(disziplin)
                                              .get(preisgruppe == 0 ? 0 : preisgruppe - 1));
            BigDecimal pauschal = BigDecimal.valueOf(Double.parseDouble(preispauschal));
            return kms.doubleValue() > pauschal.doubleValue();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Fehler in der Ermittlung km-Abrechnung besser als Pauschale\n" + meldung);
        }
        return false;
    }

    private static Object[] hbNormal(ZuzahlModell zm, BigDecimal rezwert, Double rezgeb, int realhbAnz,
            boolean neuerpreis) {
        // Object[] retobj = {new BigDecimal(new Double(0.00)),(Double)rezgeb};
        // ((BigDecimal)retobj[0]).add(BigDecimal.valueOf(new Double(1.00)));
        // ((BigDecimal) retobj[0]).add(new BigDecimal(rezwert));
        Object[] retobj = { rezwert, rezgeb };
        // System.out.println("Die tatsächlich HB-Anzahl = "+realhbAnz);
        // System.out.println("Der Rezeptwert zu Beginn = "+retobj[0]);
        if (zm.hausbesuch) { // Hausbesuch
            // System.out.println("Hausbesuch ist angesagt");
            // String[] praefix = {"1","2","5","3","MA","KG","ER","LO"};
            String rezid = SystemConfig.hmAdrRDaten.get("<Rnummer>")
                                                   .substring(0, 2);
            /*
             * String zz = SystemConfig.vHBRegeln.get(zm.preisgruppe-1).get(4); String
             * kmgeld = SystemConfig.vHBRegeln.get(zm.preisgruppe-1).get(2); String kmpausch
             * = SystemConfig.vHBRegeln.get(zm.preisgruppe-1).get(3); String hbpos =
             * SystemConfig.vHBRegeln.get(zm.preisgruppe-1).get(0); String hbmit =
             * SystemConfig.vHBRegeln.get(zm.preisgruppe-1).get(1);
             */
            String zz = SystemPreislisten.hmHBRegeln.get(getDisziplinFromRezNr(rezid))
                                                    .get(zm.preisgruppe - 1)
                                                    .get(4);
            String kmgeld = SystemPreislisten.hmHBRegeln.get(getDisziplinFromRezNr(rezid))
                                                        .get(zm.preisgruppe - 1)
                                                        .get(2);
            String kmpausch = SystemPreislisten.hmHBRegeln.get(getDisziplinFromRezNr(rezid))
                                                          .get(zm.preisgruppe - 1)
                                                          .get(3);
            String hbpos = SystemPreislisten.hmHBRegeln.get(getDisziplinFromRezNr(rezid))
                                                       .get(zm.preisgruppe - 1)
                                                       .get(0);
            String hbmit = SystemPreislisten.hmHBRegeln.get(getDisziplinFromRezNr(rezid))
                                                       .get(zm.preisgruppe - 1)
                                                       .get(1);

            // für jede Disziplin eine anderes praefix
            // String ersatz = praefix[Arrays.asList(praefix).indexOf(rezid)-4];
            /*
             * kmgeld = kmgeld.replaceAll("x",ersatz); kmpausch =
             * kmpausch.replaceAll("x",ersatz); hbpos = hbpos.replaceAll("x",ersatz); hbmit
             * = hbmit.replaceAll("x",ersatz);
             */
            String preis = "";
            BigDecimal bdrezgeb;
            BigDecimal bdposwert;
            BigDecimal bdpreis;
            BigDecimal bdendrezgeb;
            BigDecimal testpr;
            SystemConfig.hmAdrRDaten.put("<Rwegkm>", Integer.toString(zm.km));
            SystemConfig.hmAdrRDaten.put("<Rhbanzahl>", Integer.toString(zm.gesamtZahl));
            DecimalFormat dfx = new DecimalFormat("0.00");

            if (zm.hbheim) { // und zwar im Heim
                // System.out.println("Der HB ist im Heim");
                if (zm.hbvoll) {// Volle Ziffer abrechnen?
                    // System.out.println("Es kann der volle Hausbesuch abgerechnet werden");
                    SystemConfig.hmAdrRDaten.put("<Rhbpos>", hbpos);
                    preis = PreisUeberPosition(SystemConfig.hmAdrRDaten.get("<Rhbpos>"), zm.preisgruppe,
                            SystemConfig.hmAdrRDaten.get("<Rnummer>")
                                                    .substring(0, 2),
                            neuerpreis);
                    // ,"<Rhbpos>","<Rwegpos>","<Rhbpreis>","<Rwegpreis>","<Rhbproz>","<Rwegproz>","<Rhbanzahl>"
                    // ,"<Rhbgesamt>","<Rweggesamt>","<Rwegkm>"});
                    bdpreis = new BigDecimal(Double.valueOf(preis));
                    // bdposwert = bdpreis.multiply(BigDecimal.valueOf(new Double(zm.gesamtZahl)));
                    bdposwert = bdpreis.multiply(BigDecimal.valueOf(Double.valueOf(realhbAnz)));
                    retobj[0] = ((BigDecimal) retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));

                    if ("1".equals(zz)) {// Zuzahlungspflichtig
                        SystemConfig.hmAdrRDaten.put("<Rhbpreis>", preis);
                        bdrezgeb = bdpreis.divide(BigDecimal.valueOf(Double.valueOf(10.000)));
                        testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
                        bdendrezgeb = testpr.multiply(BigDecimal.valueOf(Double.valueOf(zm.gesamtZahl)));
                        SystemConfig.hmAdrRDaten.put("<Rhbproz>", dfx.format(testpr.doubleValue()));
                        SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", dfx.format(bdendrezgeb.doubleValue()));
                        retobj[1] = (Double) retobj[1] + bdendrezgeb.doubleValue();
                    } else {
                        SystemConfig.hmAdrRDaten.put("<Rhbanzahl>", "----");
                        SystemConfig.hmAdrRDaten.put("<Rhbpos>", "----");
                        SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
                        SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
                        SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
                        SystemConfig.hmAdrRDaten.put("<Rweganzahl>", "----");
                        SystemConfig.hmAdrRDaten.put("<Rwegpos>", "----");
                        SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
                        SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
                        SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
                    }
                    if (!"".equals(kmgeld)) {// Wenn Kilometer abgerechnet werden können
                        // System.out.println("Es könnten Kilometer abgerechnet werden");
                        if (kmBesserAlsPauschale(kmpausch, kmgeld, Double.parseDouble(Integer.toString(zm.km)),
                                zm.preisgruppe, getDisziplinFromRezNr(SystemConfig.hmAdrRDaten.get("<Rnummer>")))) {
                            // Mit Kilometerabrechnung verdient man mehr
                            preis = PreisUeberPosition(kmgeld, zm.preisgruppe, SystemConfig.hmAdrRDaten.get("<Rnummer>")
                                                                                                       .substring(0, 2),
                                    neuerpreis);
                            SystemConfig.hmAdrRDaten.put("<Rwegpos>", "" + zm.km + "km*" + preis);
                            bdpreis = new BigDecimal(Double.valueOf(preis)).multiply(
                                    new BigDecimal(Double.valueOf(zm.km)));
                            bdposwert = bdpreis.multiply(BigDecimal.valueOf(Double.valueOf(realhbAnz)));
                            retobj[0] = ((BigDecimal) retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));
                            SystemConfig.hmAdrRDaten.put("<Rwegpreis>", dfx.format(bdpreis.doubleValue()));
                            // System.out.println("Zuzahlungsmodus =
                            // "+SystemPreislisten.hmZuzahlModus.get(putRezNrGetDisziplin(rezid)).get(zm.preisgruppe-1));
                            if ("1".equals(zz)) {// Zuzahlungspflichtig
                                /* Hier noch den bayrischen Modus einbauen. */

                                if (SystemPreislisten.hmZuzahlModus.get(getDisziplinFromRezNr(rezid))
                                                                   .get(zm.preisgruppe - 1) == 1) {
                                    bdrezgeb = bdpreis.divide(BigDecimal.valueOf(Double.valueOf(10.000)));
                                    testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
                                    bdendrezgeb = testpr.multiply(BigDecimal.valueOf(Double.valueOf(zm.gesamtZahl)));
                                    SystemConfig.hmAdrRDaten.put("<Rwegproz>", dfx.format(testpr.doubleValue()));
                                } else { // bayrische Variante
                                         // bdpreis.divide(BigDecimal.valueOf(new Double(10.000)))
                                    bdrezgeb = BigDecimal.valueOf(Double.valueOf(preis))
                                                         .divide(BigDecimal.valueOf(Double.valueOf(10.000)));
                                    testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
                                    bdendrezgeb = testpr.multiply(BigDecimal.valueOf(Double.valueOf(zm.gesamtZahl)))
                                                        .multiply(new BigDecimal(Double.valueOf(zm.km)));
                                    SystemConfig.hmAdrRDaten.put("<Rwegproz>",
                                            dfx.format(testpr.doubleValue()) + "(*" + zm.km + "km)");
                                }
                                SystemConfig.hmAdrRDaten.put("<Rweggesamt>", dfx.format(bdendrezgeb.doubleValue()));
                                SystemConfig.hmAdrRDaten.put("<Rweganzahl>", Integer.valueOf(zm.gesamtZahl)
                                                                                    .toString());
                                retobj[1] = (Double) retobj[1] + bdendrezgeb.doubleValue();
                            } else {
                                SystemConfig.hmAdrRDaten.put("<Rhbanzahl>", "----");
                                SystemConfig.hmAdrRDaten.put("<Rhbpos>", "----");
                                SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
                                SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
                                SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
                                SystemConfig.hmAdrRDaten.put("<Rweganzahl>", "----");
                                SystemConfig.hmAdrRDaten.put("<Rwegpos>", "----");
                                SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
                                SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
                                SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
                            }

                            // hier zuerst die kilometer ermitteln mal Kilometerpreis = der Endpreis
                        } else // System.out.println("Es wurden keine Kilometer angegeben also wird nach
                               // Ortspauschale abgerechnet");
                        if (!"".equals(kmpausch)) {// Wenn die Kasse keine Pauschale zur Verfügung stellt
                            SystemConfig.hmAdrRDaten.put("<Rwegpos>", kmpausch);
                            preis = PreisUeberPosition(SystemConfig.hmAdrRDaten.get("<Rwegpos>"), zm.preisgruppe,
                                    SystemConfig.hmAdrRDaten.get("<Rnummer>")
                                                            .substring(0, 2),
                                    neuerpreis);
                            bdpreis = new BigDecimal(Double.valueOf(preis));
                            bdposwert = bdpreis.multiply(BigDecimal.valueOf(Double.valueOf(realhbAnz)));
                            retobj[0] = ((BigDecimal) retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));
                            SystemConfig.hmAdrRDaten.put("<Rwegpreis>", dfx.format(bdpreis.doubleValue()));
                            if ("1".equals(zz)) {// Zuzahlungspflichtig
                                bdrezgeb = bdpreis.divide(BigDecimal.valueOf(Double.valueOf(10.000)));
                                testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
                                bdendrezgeb = testpr.multiply(BigDecimal.valueOf(Double.valueOf(zm.gesamtZahl)));
                                SystemConfig.hmAdrRDaten.put("<Rwegproz>", dfx.format(testpr.doubleValue()));
                                SystemConfig.hmAdrRDaten.put("<Rweggesamt>", dfx.format(bdendrezgeb.doubleValue()));
                                SystemConfig.hmAdrRDaten.put("<Rweganzahl>", Integer.valueOf(zm.gesamtZahl)
                                                                                    .toString());
                                retobj[1] = (Double) retobj[1] + bdendrezgeb.doubleValue();
                            } else {
                                SystemConfig.hmAdrRDaten.put("<Rhbanzahl>", "----");
                                SystemConfig.hmAdrRDaten.put("<Rhbpos>", "----");
                                SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
                                SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
                                SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
                                SystemConfig.hmAdrRDaten.put("<Rweganzahl>", "----");
                                SystemConfig.hmAdrRDaten.put("<Rwegpos>", "----");
                                SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
                                SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
                                SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
                            }
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "Dieser Kostenträger kennt keine Weg-Pauschale, geben Sie im Patientenstamm die Anzahl Kilometer an");
                            SystemConfig.hmAdrRDaten.put("<Rweganzahl>", "----");
                            SystemConfig.hmAdrRDaten.put("<Rwegpos>", "----");
                            SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
                            SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
                            SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
                        }
                    } else {// es können keine Kilometer abgerechnet werden
                            // System.out.println("Zuzahlungsmodus =
                            // "+SystemPreislisten.hmZuzahlModus.get(putRezNrGetDisziplin(rezid)).get(zm.preisgruppe-1));
                        SystemConfig.hmAdrRDaten.put("<Rwegpos>", "----");
                        preis = PreisUeberPosition(SystemConfig.hmAdrRDaten.get("<Rwegpos>"), zm.preisgruppe,
                                SystemConfig.hmAdrRDaten.get("<Rnummer>")
                                                        .substring(0, 2),
                                neuerpreis);
                        if (preis != null) {
                            bdpreis = new BigDecimal(Double.valueOf(preis));
                            bdposwert = bdpreis.multiply(BigDecimal.valueOf(Double.valueOf(realhbAnz)));
                            retobj[0] = ((BigDecimal) retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));
                            SystemConfig.hmAdrRDaten.put("<Rwegpreis>", dfx.format(bdpreis.doubleValue()));
                            if ("1".equals(zz)) {// Zuzahlungspflichtig
                                bdrezgeb = bdpreis.divide(BigDecimal.valueOf(Double.valueOf(10.000)));
                                testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
                                bdendrezgeb = testpr.multiply(BigDecimal.valueOf(Double.valueOf(zm.gesamtZahl)));
                                SystemConfig.hmAdrRDaten.put("<Rwegproz>", dfx.format(testpr.doubleValue()));
                                SystemConfig.hmAdrRDaten.put("<Rweganzahl>", Integer.valueOf(zm.gesamtZahl)
                                                                                    .toString());
                                SystemConfig.hmAdrRDaten.put("<Rweggesamt>", dfx.format(bdendrezgeb.doubleValue()));
                                retobj[1] = (Double) retobj[1] + bdendrezgeb.doubleValue();
                            } else {
                                SystemConfig.hmAdrRDaten.put("<Rhbanzahl>", "----");
                                SystemConfig.hmAdrRDaten.put("<Rhbpos>", "----");
                                SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
                                SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
                                SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
                                SystemConfig.hmAdrRDaten.put("<Rweganzahl>", "----");
                                SystemConfig.hmAdrRDaten.put("<Rwegpos>", "----");
                                SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
                                SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
                                SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
                            }
                        } else {
                            SystemConfig.hmAdrRDaten.put("<Rweganzahl>", "----");
                            SystemConfig.hmAdrRDaten.put("<Rwegpos>", "----");
                            SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
                            SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
                            SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
                        }
                    }
                } else {// nur Mit-Hausbesuch
                    SystemConfig.hmAdrRDaten.put("<Rhbpos>", hbmit);
                    preis = PreisUeberPosition(SystemConfig.hmAdrRDaten.get("<Rhbpos>"), zm.preisgruppe,
                            SystemConfig.hmAdrRDaten.get("<Rnummer>")
                                                    .substring(0, 2),
                            neuerpreis);
                    if (preis != null) {
                        bdpreis = new BigDecimal(Double.valueOf(preis));
                        bdposwert = bdpreis.multiply(BigDecimal.valueOf(Double.valueOf(realhbAnz)));
                        retobj[0] = ((BigDecimal) retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));
                        if ("1".equals(zz)) {// Zuzahlungspflichtig
                            SystemConfig.hmAdrRDaten.put("<Rhbpreis>", preis);
                            bdrezgeb = bdpreis.divide(BigDecimal.valueOf(Double.valueOf(10.000)));
                            testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
                            bdendrezgeb = testpr.multiply(BigDecimal.valueOf(Double.valueOf(zm.gesamtZahl)));
                            SystemConfig.hmAdrRDaten.put("<Rhbproz>", dfx.format(testpr.doubleValue()));
                            SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", dfx.format(bdendrezgeb.doubleValue()));
                            // SystemConfig.hmAdrRDaten.put("<Rweganzahl>",Integer.valueOf(zm.gesamtZahl).toString()
                            // );
                            SystemConfig.hmAdrRDaten.put("<Rwegpos>", "----");
                            SystemConfig.hmAdrRDaten.put("<Rwpreis>", "0,00");
                            SystemConfig.hmAdrRDaten.put("<Rweganzahl>", "----");
                            retobj[1] = (Double) retobj[1] + bdendrezgeb.doubleValue();
                        } else {
                            SystemConfig.hmAdrRDaten.put("<Rhbanzahl>", "----");
                            SystemConfig.hmAdrRDaten.put("<Rhbpos>", "----");
                            SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
                            SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
                            SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
                            SystemConfig.hmAdrRDaten.put("<Rweganzahl>", "----");
                            SystemConfig.hmAdrRDaten.put("<Rwegpos>", "----");
                            SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
                            SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
                            SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
                        }
                    } else {
                        SystemConfig.hmAdrRDaten.put("<Rhbanzahl>", "----");
                        SystemConfig.hmAdrRDaten.put("<Rhbpos>", "----");
                        SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
                        SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
                        SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
                        SystemConfig.hmAdrRDaten.put("<Rweganzahl>", "----");
                        SystemConfig.hmAdrRDaten.put("<Rwegpos>", "----");
                        SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
                        SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
                        SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
                    }
                }
            } else {// nicht im Heim
                    // System.out.println("Der Hausbesuch ist nicht in einem Heim");
                SystemConfig.hmAdrRDaten.put("<Rhbpos>", hbpos);
                preis = PreisUeberPosition(SystemConfig.hmAdrRDaten.get("<Rhbpos>"), zm.preisgruppe,
                        SystemConfig.hmAdrRDaten.get("<Rnummer>")
                                                .substring(0, 2),
                        neuerpreis);
                bdpreis = new BigDecimal(Double.valueOf(preis));
                bdposwert = bdpreis.multiply(BigDecimal.valueOf(Double.valueOf(realhbAnz)));
                retobj[0] = ((BigDecimal) retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));
                SystemConfig.hmAdrRDaten.put("<Rhbpreis>", preis);

                bdrezgeb = bdpreis.divide(BigDecimal.valueOf(Double.valueOf(10.000)));
                testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
                bdendrezgeb = testpr.multiply(BigDecimal.valueOf(Double.valueOf(zm.gesamtZahl)));
                SystemConfig.hmAdrRDaten.put("<Rhbproz>", dfx.format(testpr.doubleValue()));
                SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", dfx.format(bdendrezgeb.doubleValue()));
                retobj[1] = (Double) retobj[1] + bdendrezgeb.doubleValue();

                if (!"".equals(kmgeld)) {// Wenn Kilometer abgerechnet werden k�nnen
                    // System.out.println("Es könnten Kilometer abgerechnet werden");
                    if (kmBesserAlsPauschale(kmpausch, kmgeld, Double.parseDouble(Integer.toString(zm.km)),
                            zm.preisgruppe, getDisziplinFromRezNr(SystemConfig.hmAdrRDaten.get("<Rnummer>")))) {
                        // Kilometerabrechnung besser als Pauschale
                        preis = PreisUeberPosition(kmgeld, zm.preisgruppe, SystemConfig.hmAdrRDaten.get("<Rnummer>")
                                                                                                   .substring(0, 2),
                                neuerpreis);
                        SystemConfig.hmAdrRDaten.put("<Rwegpos>", "" + zm.km + "km*" + preis);

                        bdpreis = new BigDecimal(Double.valueOf(preis)).multiply(new BigDecimal(Double.valueOf(zm.km)));
                        bdposwert = bdpreis.multiply(BigDecimal.valueOf(Double.valueOf(realhbAnz)));
                        retobj[0] = ((BigDecimal) retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));
                        SystemConfig.hmAdrRDaten.put("<Rwegpreis>", dfx.format(bdpreis.doubleValue()));
                        if (SystemPreislisten.hmZuzahlModus.get(getDisziplinFromRezNr(rezid))
                                                           .get(zm.preisgruppe - 1) == 1) {
                            bdrezgeb = bdpreis.divide(BigDecimal.valueOf(Double.valueOf(10.000)));
                            testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
                            bdendrezgeb = testpr.multiply(BigDecimal.valueOf(Double.valueOf(zm.gesamtZahl)));
                            SystemConfig.hmAdrRDaten.put("<Rwegproz>", dfx.format(testpr.doubleValue()));
                        } else {
                            // bdpreis.divide(BigDecimal.valueOf(new Double(10.000)))
                            bdrezgeb = BigDecimal.valueOf(Double.valueOf(preis))
                                                 .divide(BigDecimal.valueOf(Double.valueOf(10.000)));
                            testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
                            bdendrezgeb = testpr.multiply(BigDecimal.valueOf(Double.valueOf(zm.gesamtZahl)))
                                                .multiply(new BigDecimal(Double.valueOf(zm.km)));
                            SystemConfig.hmAdrRDaten.put("<Rwegproz>",
                                    dfx.format(testpr.doubleValue()) + "(*" + zm.km + "km)");
                        }
                        SystemConfig.hmAdrRDaten.put("<Rweggesamt>", dfx.format(bdendrezgeb.doubleValue()));
                        SystemConfig.hmAdrRDaten.put("<Rweganzahl>", Integer.valueOf(zm.gesamtZahl)
                                                                            .toString());
                        retobj[1] = (Double) retobj[1] + bdendrezgeb.doubleValue();
                    } else // System.out.println("Es wurden keine Kilometer angegeben also wird nach
                    // Ortspauschale abgerechnet");
                    if (!"".equals(kmpausch)) {// Wenn die Kasse keine Pauschale zur Verfügung stellt
                        SystemConfig.hmAdrRDaten.put("<Rwegpos>", kmpausch);
                        preis = PreisUeberPosition(SystemConfig.hmAdrRDaten.get("<Rwegpos>"), zm.preisgruppe,
                                SystemConfig.hmAdrRDaten.get("<Rnummer>")
                                                        .substring(0, 2),
                                neuerpreis);
                        bdpreis = new BigDecimal(Double.valueOf(preis));
                        bdposwert = bdpreis.multiply(BigDecimal.valueOf(Double.valueOf(realhbAnz)));
                        retobj[0] = ((BigDecimal) retobj[0]).add(BigDecimal.valueOf(bdposwert.doubleValue()));
                        SystemConfig.hmAdrRDaten.put("<Rwegpreis>", dfx.format(bdpreis.doubleValue()));

                        bdrezgeb = bdpreis.divide(BigDecimal.valueOf(Double.valueOf(10.000)));
                        testpr = bdrezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
                        bdendrezgeb = testpr.multiply(BigDecimal.valueOf(Double.valueOf(zm.gesamtZahl)));
                        SystemConfig.hmAdrRDaten.put("<Rwegproz>", dfx.format(testpr.doubleValue()));
                        SystemConfig.hmAdrRDaten.put("<Rweggesamt>", dfx.format(bdendrezgeb.doubleValue()));
                        SystemConfig.hmAdrRDaten.put("<Rweganzahl>", Integer.valueOf(zm.gesamtZahl)
                                                                            .toString());
                        retobj[1] = (Double) retobj[1] + bdendrezgeb.doubleValue();
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Dieser Kostenträger kennt keine Weg-Pauschale, geben Sie im Patientenstamm die Anzahl Kilometer an");
                        SystemConfig.hmAdrRDaten.put("<Rwegpos>", "----");
                        SystemConfig.hmAdrRDaten.put("<Rweganzahl>", "----");
                        SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
                        SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
                        SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
                    }
                } else {// es können keine Kilometer abgerechnet werden
                    SystemConfig.hmAdrRDaten.put("<Rwegpos>", "----");
                    SystemConfig.hmAdrRDaten.put("<Rweganzahl>", "----");
                    SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
                    SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
                    SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
                }
            }
        } else {
            SystemConfig.hmAdrRDaten.put("<Rhbanzahl>", "----");
            SystemConfig.hmAdrRDaten.put("<Rhbpos>", "----");
            SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
            SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
            SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
            SystemConfig.hmAdrRDaten.put("<Rweganzahl>", "----");
            SystemConfig.hmAdrRDaten.put("<Rwegpos>", "----");
            SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
            SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
            SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
        }
        // System.out.println("Der Rezeptwert = "+retobj[0]);
        return retobj;
    }

    public static void constructVirginHMap() {
        try {
            SystemConfig.hmAdrRDaten.put("<Rid>", Reha.instance.patpanel.vecaktrez.get(35));
            SystemConfig.hmAdrRDaten.put("<Rnummer>", Reha.instance.patpanel.vecaktrez.get(1));
            SystemConfig.hmAdrRDaten.put("<Rdatum>", DatFunk.sDatInDeutsch(Reha.instance.patpanel.vecaktrez.get(2)));
            SystemConfig.hmAdrRDaten.put("<Rdiagnose>", Reha.instance.patpanel.vecaktrez.get(23));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void constructFormularHMap() { // unbenutzt (?)
        try {
            DecimalFormat dfx = new DecimalFormat("0.00");
            SystemConfig.hmAdrRDaten.put("<Rid>", Reha.instance.patpanel.vecaktrez.get(35));
            SystemConfig.hmAdrRDaten.put("<Rnummer>", Reha.instance.patpanel.vecaktrez.get(1));
            SystemConfig.hmAdrRDaten.put("<Rdatum>", DatFunk.sDatInDeutsch(Reha.instance.patpanel.vecaktrez.get(2)));
            for (int i = 3; i < 7; i++) {
                if (!"0".equals(Reha.instance.patpanel.vecaktrez.get(i))) {
                    SystemConfig.hmAdrRDaten.put("<Rposition" + (i - 2) + ">",
                            Reha.instance.patpanel.vecaktrez.get(45 + i));
                    Double preis = Double.parseDouble(Reha.instance.patpanel.vecaktrez.get(15 + i));

                    SystemConfig.hmAdrRDaten.put("<Rpreis" + (i - 2) + ">", dfx.format(preis)
                                                                               .replace(".", ","));
                    SystemConfig.hmAdrRDaten.put("<Ranzahl" + (i - 2) + ">", Reha.instance.patpanel.vecaktrez.get(i));
                    BigDecimal gesamt = BigDecimal.valueOf(preis)
                                                  .multiply(BigDecimal.valueOf(
                                                          Double.parseDouble(Reha.instance.patpanel.vecaktrez.get(i))));
                    SystemConfig.hmAdrRDaten.put("<Rgesamt" + (i - 2) + ">", dfx.format(gesamt)
                                                                                .replace(".", ","));
                }
            }
            sortHmAdrRdaten();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** * Funktionen für Abrechnung nach §302. */
    public static Object[] unter18Check(Vector<Vector<Object>> behandlungsfall, String geburtstag) {
        // unter 18 ab Vector x über 18 gesplittet
        Object[] ret = { Boolean.TRUE, behandlungsfall.size(), Boolean.FALSE };
        String tag1 = (String) behandlungsfall.get(behandlungsfall.size() - 1)
                                              .get(0);
        String tag2 = (String) behandlungsfall.get(0)
                                              .get(0);
        if (DatFunk.Unter18(tag1, geburtstag)) {
            return ret;
        }
        if (!DatFunk.Unter18(tag1, geburtstag) && !DatFunk.Unter18(tag2, geburtstag)) {
            ret[0] = false;
            ret[1] = -1;
            ret[2] = false;
            return ret;
        }

        int i;
        for (i = 0; i < behandlungsfall.size(); i++) {
            tag1 = (String) behandlungsfall.get(i)
                                           .get(0);
            if (!DatFunk.Unter18(tag1, geburtstag)) {
                break;
            }
        }
        ret[0] = true;
        ret[1] = i;
        ret[2] = true;
        return ret;
    }

    public static Object[] jahresWechselCheck(Vector<Vector<Object>> behandlungsfall, boolean unter18) {
        // Jahreswechsel ab Position vollständig im alten Jahr
        // unter18 wird hier nicht mehr ausgewertet, als Parameter aber noch belassen
        Object[] ret = { Boolean.FALSE, -1, Boolean.FALSE };
        if (((String) behandlungsfall.get(0)
                                     .get(0)).endsWith(SystemConfig.aktJahr)) {
            return ret;
        }
        for (int i = 0; i < behandlungsfall.size(); i++) {
            if (!((String) behandlungsfall.get(i)
                                          .get(0)).endsWith(SystemConfig.aktJahr)) {
                ret[0] = true;
                ret[2] = true;
            } else {
                ret[0] = true;
                ret[1] = i;
                ret[2] = false;
                break;
            }
        }
        return ret;
    }

    public static void loescheRezAusVolleTabelle(final String reznr) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                SqlInfo.sqlAusfuehren("delete from volle where rez_nr='" + reznr + "'");
                return null;
            }

        }.execute();
    }

    public static void fuelleVolleTabelle(final String reznr, final String rezbehandler) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    if (SqlInfo.gibtsSchon("select rez_nr from volle where rez_nr ='" + reznr + "' LIMIT 1")) {
                        return null;
                    }
                    Vector<Vector<String>> vec = SqlInfo.holeFelder(
                            "select pat_intern,rez_datum,id from verordn where rez_nr = '" + reznr + "' LIMIT 1");
                    String cmd = "insert into volle set rez_nr='" + reznr + "', " + "pat_intern='" + vec.get(0)
                                                                                                        .get(0)
                            + "', behandler='" + rezbehandler + "', " + "fertigam='"
                            + DatFunk.sDatInSQL(DatFunk.sHeute()) + "', " + "rez_datum='" + vec.get(0)
                                                                                               .get(1)
                            + "', rezid='" + vec.get(0)
                                                .get(2)
                            + "'";
                    SqlInfo.sqlAusfuehren(cmd);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Fehler bei der Ausführung 'fuelleVolleTabelle'");
                }
                return null;
            }
        }.execute();
    }

    public static void RezGebSignal(String rez_num) {
        final String xrez_num = rez_num;
        if (!SystemConfig.RezGebWarnung) {
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    if ("2".equals(SqlInfo.holeEinzelFeld(
                            "select zzstatus from verordn where rez_nr = '" + xrez_num + "' LIMIT 1"))) {
                        new AePlayWave(Path.Instance.getProghome() + "sounds/" + "doorbell.wav").start();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

        }.execute();
    }

    public static Object[] BehandlungenAnalysieren(String swreznum, boolean doppeltOk, boolean xforceDlg,
            boolean alletermine, Vector<String> vecx, Point pt, String xkollege, String datum) {
        int i, j, count = 0;
        boolean dlgZeigen = false; // unterdrückt die Anzeige des TeminBestätigenAuswahlFensters
        boolean jetztVoll = false;
        boolean anzahlRezeptGleich = true;
        boolean nochOffenGleich = true;
        Vector<BestaetigungsDaten> hMPos = new Vector<>();
        hMPos.add(new BestaetigungsDaten(false, "./.", 0, 0, false, false));
        hMPos.add(new BestaetigungsDaten(false, "./.", 0, 0, false, false));
        hMPos.add(new BestaetigungsDaten(false, "./.", 0, 0, false, false));
        hMPos.add(new BestaetigungsDaten(false, "./.", 0, 0, false, false));
        Vector<String> vec = null;

        StringBuffer termbuf = new StringBuffer();
        int preisgruppe = -1;
        String disziplin = "";
        boolean hmrtest = false;

        Object[] retObj = { null, null, null };
        try {
            // die anzahlen 1-4 werden jetzt zusammenhängend ab index 11 abgerufen
            if (vecx != null) {
                vec = vecx;
            } else {
                vec = SqlInfo.holeSatz("verordn",
                        "termine,pos1,pos2,pos3,pos4,hausbes,unter18,jahrfrei,pat_intern,preisgruppe,zzregel,anzahl1,anzahl2,anzahl3,anzahl4,preisgruppe",
                        "rez_nr='" + swreznum + "'", Arrays.asList(new String[] {}));
            }
            preisgruppe = Integer.parseInt(vec.get(9));
            disziplin = getDisziplinFromRezNr(swreznum);
            hmrtest = SystemPreislisten.hmHMRAbrechnung.get(disziplin)
                                                       .get(preisgruppe - 1) == 1;
            if (!vec.isEmpty()) {
                termbuf = new StringBuffer();
                if (alletermine) {
                    termbuf.append(vec.get(0));
                }

                Vector<ArrayList<?>> termine = holePosUndAnzahlAusTerminen(swreznum);
                // System.out.println(termine);
                if (termine.isEmpty()) {
                    return null;
                }
                // Arrays innerhalb dem Vector termine
                // termine.get(0) = Positionen (String)
                // termine.get(1) = Anzahlen (Integer)
                // termine.get(2) = Vorrangiges Heilmittel (Boolean)
                // termine.get(3) = als Standalone ergänzendes Heilmittel erlaubt (Boolean)
                // termine.get(4) = Object[]
                // {doppelbehandlung(Boolean),erstepos(Integer),letztepos(Integer)}

                int termineIdx = 0;
                for (i = 0; i <= 3; i++) {  // pos1..pos4
                    if ("".equals(vec.get(1 + i)
                            .trim())) {
                        hMPos.get(i).hMPosNr = "./.";
                        hMPos.get(i).vOMenge = 0;
                        hMPos.get(i).anzBBT = 0;
                    } else {
                        hMPos.get(i).hMPosNr = String.valueOf(vec.get(1 + i));
                        hMPos.get(i).vOMenge = Integer.parseInt(vec.get(i + 11));
                        hMPos.get(i).vorrangig = (Boolean) ((ArrayList<?>) ((Vector<?>) termine).get(2)).get(termineIdx);
                        hMPos.get(i).invOBelegt = true;
                        hMPos.get(i).anzBBT = (Integer) ((ArrayList<?>) ((Vector<?>) termine).get(1)).get(termineIdx);
                        termineIdx++;
                    }
                    hMPos.get(i)
                         .gehtNochEiner();
                }
                // Jetzt alle Objekte die unbelegt sind löschen
                for (i = 3; i >= 0; i--) {
                    if (!hMPos.get(i).invOBelegt) {
                        hMPos.remove(i);
                    }
                }
                hMPos.trimToSize();
                // Die Variable j erhält jetzt den Wert der Anzahl der verbliebenen Objekte
                j = hMPos.size();

                // Nur wenn nach HMR-geprüft werden muß
                if (hmrtest) {
                    // 1. erst Prüfen ob das Rezept bereits voll ist
                    for (i = 0; i < j; i++) {
                        if (!hMPos.get(i).einerOk && hMPos.get(i).vorrangig) {
                            // ein vorrangiges Heilmittel ist voll
                            // testen ob es sich um eine Doppelposition dreht
                            if (((Object[]) ((ArrayList<?>) ((Vector<?>) termine).get(4)).get(i))[0] == Boolean.TRUE) {
                                // testen ob es die 1-te Pos der Doppelbehandlung ist
                                if ((Integer) ((Object[]) ((ArrayList<?>) ((Vector<?>) termine).get(4)).get(
                                        i))[1] < (Integer) ((Object[]) ((ArrayList<?>) ((Vector<?>) termine).get(
                                                4)).get(i))[2]) {
                                    // Es ist die 1-te Position die voll ist also Ende-Gelände
                                    retObj[0] = String.valueOf(termbuf.toString());
                                    retObj[1] = Integer.valueOf(REZEPT_IST_BEREITS_VOLL);
                                    // if(debug){System.out.println("erste Position = voll + Doppelbehandlung");}
                                    // if(debug){System.out.println(hMPos.get(i).hMPosNr+"-"+hMPos.get(i).vOMenge+"-"+hMPos.get(i).anzBBT);}
                                    return retObj;
                                }
                            } else {
                                // nein keine Doppelposition also Ende-Gelände      // TODO HMR2020: kann noch weiter gehen (bis 3 vorr. HM)
                                retObj[0] = String.valueOf(termbuf.toString());
                                retObj[1] = Integer.valueOf(REZEPT_IST_BEREITS_VOLL);
                                // if(debug){System.out.println("erste Position = voll und keine
                                // Doppelbehandlung");}
                                // if(debug){System.out.println(hMPos.get(i).hMPosNr+"-"+hMPos.get(i).vOMenge+"-"+hMPos.get(i).anzBBT);}
                                return retObj;
                            }
                        } else if (!hMPos.get(i).einerOk && !hMPos.get(i).vorrangig && j == 1) {
                            // Falls eines der wenigen ergänzenden Heilmittel solo verordnet wurde
                            // z.B. Ultraschall oder Elektrotherapie
                            retObj[0] = String.valueOf(termbuf.toString());
                            retObj[1] = Integer.valueOf(REZEPT_IST_BEREITS_VOLL);
                            // if(debug){System.out.println("es geht kein zusätzlicher");}
                            // if(debug){System.out.println(hMPos.get(i).hMPosNr+"-"+hMPos.get(i).vOMenge+"-"+hMPos.get(i).anzBBT);}
                            return retObj;
                        } else if (!hMPos.get(i).vorrangig && j == 1
                                && (Boolean) ((ArrayList<?>) ((Vector<?>) termine).get(2)).get(i)) {
                            // Ein ergänzendes Heilmittel wurde separat verordent das nicht zulässig ist
                            // könnte man auswerten, dann verbaut man sich aber die Möglichkeit
                            // bei PrivatPat. abzurechnen was geht....
                            // if(debug){System.out.println("unerlaubtes Ergänzendes Heilmittel solo
                            // verordnet");}
                            // if(debug){System.out.println(hMPos.get(i).hMPosNr+"-"+hMPos.get(i).vOMenge+"-"+hMPos.get(i).anzBBT);}
                        }
                        // if(debug){System.out.println("Position kann bestätigt werden");}
                        // if(debug){System.out.println(hMPos.get(i).hMPosNr+"-"+hMPos.get(i).vOMenge+"-"+hMPos.get(i).anzBBT);}
                    }
                    // Ende nur wenn Tarifgruppe HMR-Gruppe ist
                }

                // 2. dann prüfen welche Behandlungsformen noch einen vertragen können
                int hMmitOffenenBehandlungen = 0;
                int anzOffeneVorrHM = 0;
                int ianzahl = hMPos.get(0).vOMenge;
                int ioffen = hMPos.get(0).vORestMenge;
                for (i = 0; i < j; i++) {
                    BestaetigungsDaten currHmPos = hMPos.get(i);
                    currHmPos.danachVoll();
                    // wenn eine Behandlung noch frei ist 
                    if (currHmPos.einerOk ) {
                        hMmitOffenenBehandlungen++;
                        if (currHmPos.vorrangig) {
                            anzOffeneVorrHM++;
                        }
                    }
                    if (ianzahl != currHmPos.vOMenge) {  // HMR2020: bis zu 3 vorr. u. 1 erg. HM (nur 1 vorr. pro Behandlg.!)
                        anzahlRezeptGleich = false;
                    }
                    if (ioffen != currHmPos.vORestMenge) {
                        nochOffenGleich = false;
                    }
                }
                // Keine Postition mehr frei
                if (hMmitOffenenBehandlungen == 0) {
                    retObj[0] = String.valueOf(termbuf.toString());
                    retObj[1] = Integer.valueOf(REZEPT_IST_BEREITS_VOLL);
                    // if(debug){System.out.println("Rezept war bereits voll");}
                    return retObj;
                }
                // Nur Wenn mehrere Behandlungen im Rezept vermerkt
                boolean mustSelect = false;
                if (j > 1) {
                    // Wenn mehrere noch offen sind aber ungleiche noch Offen
                    if ((hMmitOffenenBehandlungen > 1) && (!(/* anzahlRezeptGleich && */ nochOffenGleich))) {
                        dlgZeigen = true;
                    }
                    if (anzOffeneVorrHM > 1) { // HMR2020: vorr. HM einzeln behandeln!
                        mustSelect = true;
                    }
                }
                // 3. Dann Dialog zeigen
                // TerminBestätigenAuswahlFenster anzeigen oder überspringen
                // Evtl. noch Einbauen ob bei unterschiedlichen Anzahlen
                // (System-Initialisierung) immer geöffnet wird.
                if (xforceDlg || mustSelect || (dlgZeigen && (Boolean) SystemConfig.hmTerminBestaetigen.get("dlgzeigen"))) {

                    TerminBestaetigenAuswahlFenster termBestAusw = new TerminBestaetigenAuswahlFenster(
                            Reha.getThisFrame(), null, hMPos, swreznum, Integer.parseInt(vec.get(15)));
                    termBestAusw.pack();
                    if (pt != null) {
                        termBestAusw.setLocation(pt);
                    } else {
                        termBestAusw.setLocationRelativeTo(null);
                    }
                    termBestAusw.setVisible(true);
                    if (DIALOG_WERT == DIALOG_ABBRUCH) {
                        retObj[0] = String.valueOf(termbuf.toString());
                        retObj[1] = Integer.valueOf(REZEPT_ABBRUCH);
                        return retObj;
                    }
                    for (i = 0; i < j; i++) {
                        BestaetigungsDaten currHmPos = hMPos.get(i);
                        if (currHmPos.best) {
                            currHmPos.anzBBT += 1;
                            // gleichzeitig prüfen ob voll
                            if ((currHmPos.jetztVoll() && currHmPos.vorrangig) 
                                    || (currHmPos.jetztVoll() && (!currHmPos.vorrangig) && j == 1)) {
                                jetztVoll = true;
                            }
                        }
                    }
                } else {
                    /*
                     * Der Nutzer wünscht kein Auswahlfenster: bestätige alle noch offenen
                     * Heilmittel
                     */
                    for (i = 0; i < j; i++) {
                        BestaetigungsDaten currHmPos = hMPos.get(i);
                        currHmPos.best = Boolean.valueOf(currHmPos.einerOk);
                        if (currHmPos.einerOk) {
                            currHmPos.anzBBT += 1;
                            currHmPos.best = true;
                            if (currHmPos.jetztVoll() && currHmPos.vorrangig) {
                                jetztVoll = true;
                            } else if (currHmPos.jetztVoll() && (!currHmPos.vorrangig) && j == 1) {
                                jetztVoll = true;
                            }
                        }
                    }
                }
                String[] params = { null, null, null, null };
                count = 0;
                for (i = 0; i < j; i++) {
                    BestaetigungsDaten currHmPos = hMPos.get(i);
                    if (currHmPos.best) {
                        params[i] = currHmPos.hMPosNr;
                        count++;
                    }
                }
                if (count == 0) {
                    jetztVoll = true;
                }
                termbuf.append(TermineErfassen.macheNeuTermin2(params[0] != null ? params[0] : "",
                        params[1] != null ? params[1] : "", params[2] != null ? params[2] : "",
                        params[3] != null ? params[3] : "", xkollege, datum));

                retObj[0] = String.valueOf(termbuf.toString());
                retObj[1] = jetztVoll ? Integer.valueOf(REZEPT_IST_JETZ_VOLL) : Integer.valueOf(REZEPT_HAT_LUFT);
            }
            return retObj;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // vec = null;
            hMPos = null;
        }
        return retObj;
    }

    private static int welcheIstMaxInt(int i1, int i2) {
        if (i1 > i2) {
            return 1;
        }
        if (i1 == i2) {
            return 0;
        }
        return 2;
    }

    /**
     * Pruefen, ob Rezept dringend abgerechnet werden sollte (Jahresfrist nach
     * letzter Behandlung).
     */
    public static boolean isLate(String thisRezNr) {
        int tageBisWarnung = 310;
        String cmd = "select termine from verordn where rez_nr='" + thisRezNr + "' LIMIT 1";
        String termineDB = SqlInfo.holeEinzelFeld(cmd);
        String[] behDat = termineDB.split("\n");
        String letzteBeh = behDat[behDat.length - 1].split("@")[0];
        return DatFunk.TageDifferenz(letzteBeh, DatFunk.sHeute()) > tageBisWarnung;
    }
}

class ZuzahlModell {
    public int gesamtZahl;
    public boolean allefrei;
    public boolean allezuzahl;
    public boolean anfangfrei;
    public int teil1;
    public int teil2;
    public int preisgruppe;
    public boolean hausbesuch;
    boolean hbvoll;
    boolean hbheim;
    int km;

    public ZuzahlModell() {
    }
}
