package org.therapi.hmrCheck;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import javax.swing.JOptionPane;

import CommonTools.DatFunk;
import CommonTools.SqlInfo;
import abrechnung.Disziplinen;
import commonData.Rezeptvector;
import commonData.VerordnungsArten;
import hauptFenster.Reha;
import hmrCheck.FehlerTxt;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;

public class HMRCheck2020 {
    private Vector<Integer> anzahl = null;
    private Vector<String> positionenVorr = null;
    private Vector<String> positionenErg = null;
    private Vector<String> positionenAll = null;
    private Vector<Vector<String>> preisvec = null;
    private String diagnosegruppe = null;
    private int disziIdx;
    private String disziKurz = null;
    private String disziKapHMR = null;
    private int preisgruppe;
//    final String maxanzahl = "Die Höchstmenge pro Rezept bei ";
//    final String rotein = "<>";
    private boolean testok = true;
    private FehlerTxt fehlertext = null;
    private int rezeptart;
    private String reznummer = null;
    private String rezdatum = null;
    private String letztbeginn = null;
    private boolean neurezept = false;
    private boolean doppelbehandlung = false;
    private boolean unter18 = false;
    private String icd10_1 = null;
    private String icd10_2 = null;
    private static SimpleDateFormat sdDeutsch = new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat sdSql = new SimpleDateFormat("yyyy-MM-dd");

    VerordnungsArten voArten = new VerordnungsArten();

    int maxprorezept = 0;
    int maxprofall = 0;
    int behFreqMax = 0;
    Disziplinen diszis = null;

// mal sehen, was bleibt:
// - maxProFall (aka 'orientierende Behandlungsmenge') ist uninteressant
// - es 'zählen' nur maxProVo, entweder Standard oder BVB/LHM
//   dabei berücksichtigen: Summe HM1..3; HM4 separat

    public HMRCheck2020(Rezeptvector rezept, String currTypeOfVO, Vector<Vector<String>> xpreisvec) {
        diszis = new Disziplinen();
        diszis.setCurrTypeOfVO(currTypeOfVO);
        anzahl = new Vector<Integer>();
        positionenVorr = new Vector<String>();
        positionenErg = new Vector<String>();
        positionenAll = new Vector<String>();
        diagnosegruppe = rezept.getIndiSchluessel();
        disziIdx = diszis.getCurrDisziIdx();
        disziKurz = diszis.getDisziKurz(disziIdx);
        behFreqMax = rezept.getFrequenzMax();
        int idxVorr = 0;
        int idxErg = 0;
        for (int i = 0; i < 4; i++) {
            String tmp = rezept.getHmPos(i + 1);
            if (tmp != "") {
                anzahl.add(i, rezept.getAnzBeh(i + 1));
                if (i < 3) {
                    positionenVorr.add(idxVorr++, tmp);                    
                } else {
                    positionenErg.add(idxErg++, tmp);
                }
            } else {
                anzahl.add(i, 0);
            }
        }
        try {
            positionenAll.addAll(positionenVorr);
            positionenAll.addAll(positionenErg);
        } catch (Exception e) {
        }
        preisvec = xpreisvec;
        rezeptart = rezept.getRezArt();
        reznummer = rezept.getRezNb();
        unter18 = rezept.getUnter18();
        rezdatum = DatFunk.sDatInDeutsch(rezept.getRezeptDatum());
        letztbeginn = DatFunk.sDatInDeutsch(rezept.getLastDate());
        icd10_1 = rezept.getICD10();
        icd10_2 = rezept.getICD10_2();
        if (reznummer.equals("")) {
            neurezept = true;
        }
        fehlertext = new FehlerTxt();
    }

    /**
     *
     * Abhängig von der Diagnosegruppe und ICD10-Codes muß geprüft werden <p>
     * 1. ist die Anzahl pro Rezept o.k.<p>
     * 2. sind die gewählten Heilmittel o.k.<p>
     * 3. ist das ergänzende Heilmittel o.k.<p>
     *
     */
    public boolean check() {
        final int idxMaxProFall = 1;
        final int idxMaxProVO = 2;
        final int idxVorrHM = 3;
        final int idxMaxVorr = 4;
        final int idxErgHM = 5;
        final int idxMaxErg = 6;
        if (reznummer.startsWith("RS") || reznummer.startsWith("FT") || reznummer.startsWith("RH")
                || diszis.currIsRsport() || diszis.currIsFtrain()) {
            return true;
        }

        Vector<Vector<String>> tmpVec = SqlInfo.holeFelder(
                "select * from hmrcheck where indischluessel='" + diagnosegruppe + "' LIMIT 1");

        if ((tmpVec.size() <= 0 || diagnosegruppe.equals("")) && (!diagnosegruppe.equals("k.A."))) {
            JOptionPane.showMessageDialog(null,
                    "Diagnosegruppe " + diagnosegruppe + " unbekannt oder nicht angegeben!");
            return false;
        } else if (diagnosegruppe.equals("k.A.")) {
            JOptionPane.showMessageDialog(null,
                    "Diagnosegruppe " + diagnosegruppe
                            + " (keine Angaben) wurde gewählt, HMR-Check wird abgebrochen.\n"
                            + "Bitte stellen Sie selbst sicher daß alle übrigen Pflichtangaben vorhanden sind");
            return true;
        }
        Vector<String> vec = tmpVec.get(0);
        // System.out.println(vec);
        maxprorezept = Integer.parseInt(vec.get(idxMaxProVO));
        maxprofall = Integer.parseInt(vec.get(idxMaxProFall));
        String[] vorrangig = vec.get(idxVorrHM)
                                .split("@");
        String[] ergaenzend = vec.get(idxErgHM)
                                 .split("@");
        for (int i = 0; i < vorrangig.length; i++) {
            vorrangig[i] = diszis.getPrefix(disziIdx) + vorrangig[i];
        }
        for (int i = 0; i < ergaenzend.length; i++) {
            ergaenzend[i] = diszis.getPrefix(disziIdx) + ergaenzend[i];
        }

        // hier erstmal testen, ob BVB / LHM (ändert maxprorezept je nach Behandlungsfrequenz)
        System.out.println("max. Beh.-Frequenz: " + behFreqMax);
        //icd10_1, icd10_2 
        
        // mögliche Höchstmenge pro Rezept wurde überschritten?
        for (int i = 0; i < anzahl.size(); i++) {   // <== HMR2020: vorr HM aufsummieren!
            if (anzahl.get(i) > maxprorezept) {
                fehlertext.add("<b>Bei Diagnosegruppe " + diagnosegruppe
                        + " sind maximal<br><font color='#ff0000'>" + Integer.toString(maxprorezept)
                        + " Behandlungen</font> pro Rezept erlaubt!!<br><br></b>");
                testok = false;
            }
        }

        try {
            if (positionenAll.size() >= 2) {
                if (positionenAll.get(0)
                              .equals(positionenAll.get(1))) { // sind auch pos2 + pos3 mögl.?
                    doppelbehandlung = true;
                    int doppelgesamt = anzahl.get(0) + anzahl.get(1);
                    if (doppelgesamt > maxprorezept) {
                        fehlertext.add("<b>Die Doppelbehandlung bei Diagnosegruppe "
                                + diagnosegruppe
                                + ", übersteigt<br>die maximal erlaubte Höchstverordnungsmenge pro Rezept von<br><font color='#ff0000'>"
                                + Integer.toString(maxprorezept)
                                + " Behandlungen</font>!!</b><br><br>Wechsel auf -> außerhalb des Regelfalles <- ist erforderlich<br><br>");
                        testok = false;
                    }
                }
            }

            int posGesamt = positionenAll.size();
            for (int i = 0; i < posGesamt; i++) {
                String currPos = positionenAll.get(i);
                boolean isOptional = Arrays.asList(ergaenzend)
                                           .contains(currPos);
                if (i == 0) {
                    if (!Arrays.asList(vorrangig)
                               .contains(currPos)) {
                        // kein vorrangiges HM -> Test, ob 'ergänzend, aber einzeln erlaubt'!
                        boolean isoliertErlaubt = false;
                        for (int j = 0; j < preisvec.size(); j++) {
                            if (currPos == preisvec.get(j)
                                                   .get(2)) {
                                boolean[] vorrUisoliert = stammDatenTools.RezTools.isVorrangigAndExtra(preisvec.get(j)
                                                                                                               .get(1),
                                                                                                               diszis.getRezClass(disziIdx));
                                isoliertErlaubt = vorrUisoliert[1];
                            }
                        }
                        if (isOptional && isoliertErlaubt && (posGesamt == 1)) {
                            // ergänzendes HM darf isoliert verordnet werden (betrifft ET,EST,US)
                        } else {
                            fehlertext.add(getDialogText(true, getHeilmittel(currPos), currPos, vorrangig));
                            testok = false;
                        }
                    }
                } else if (i == 1 && doppelbehandlung) {

                } else {
                    if (!isOptional) {
                        fehlertext.add(getDialogText(false, getHeilmittel(currPos), currPos, ergaenzend));
                        testok = false;
                    }
                }
            }

            // Jetzt auf Rezeptbeginn testen
            if (neurezept) {
                long differenz = DatFunk.TageDifferenz(rezdatum, DatFunk.sHeute());
                if (differenz < 0) {
                    fehlertext.add("<br><b><font color='#ff0000'>Rezeptdatum ist absolut kritisch!</font><br>Spanne zwischen Behandlungsbeginn und Rezeptdatum beträgt <font color='#ff0000'>"
                            + Long.toString(differenz)
                            + " Tag(e) </font>.<br>Behandlungsbeginn ist also <font color='#ff0000'>vor</font> dem  Ausstellungsdatum!!</b><br><br>");
                    testok = false;
                }
                if ((differenz = DatFunk.TageDifferenz(letztbeginn, DatFunk.sHeute())) > 0) {
                    // System.out.println("Differenz 2 = "+differenz);
                    fehlertext.add("<br><b><font color='#ff0000'>Behandlungsbeginn ist kritisch!</font><br><br>Die Differenz zwischen <font color='#ff0000'>spätester Behandlungsbeginn</font> und 1.Behandlung<br>beträgt <font color='#ff0000'>"
                            + Long.toString(differenz) + " Tage </font><br>" + "</b><br><br>");
                    testok = false;
                }
            } else {
                String cmd = "select termine from verordn where rez_nr='" + reznummer + "' LIMIT 1";
                String termine = SqlInfo.holeFeld(cmd)
                                        .get(0);
                // Keine Termine notiert
                if (termine.trim()
                           .equals("")) {
                    // LetzterBeginn abhandeln
                    long differenz = DatFunk.TageDifferenz(rezdatum, DatFunk.sHeute());
                    if (differenz < 0) {
                        fehlertext.add("<br><b><font color='#ff0000'>Rezeptdatum ist absolut kritisch!</font><br>Spanne zwischen Behandlungsbeginn und Rezeptdatum beträgt <font color='#ff0000'>"
                                + Long.toString(differenz)
                                + " Tag(e) </font>.<br>Behandlungsbeginn ist also <font color='#ff0000'>vor</font> dem  Ausstellungsdatum!!</b><br><br>");
                        testok = false;
                    }
                    if ((differenz = DatFunk.TageDifferenz(letztbeginn, DatFunk.sHeute())) > 0) {
                        // System.out.println("Differenz 2 = "+differenz);
                        fehlertext.add("<br><b><font color='#ff0000'>Behandlungsbeginn ist kritisch!</font><br><br>Die Differenz zwischen <font color='#ff0000'>spätester Behandlungsbeginn</font> und 1.Behandlung<br>beträgt <font color='#ff0000'>"
                                + Long.toString(differenz) + " Tage </font><br>" + "</b><br><br>");
                        testok = false;
                    }

                } else {
                    // LetzterBeginn abhandeln
                    String erstbehandlung = RezTools.holeEinzelTermineAusRezept(null, termine)
                                                    .get(0);
                    long differenz = DatFunk.TageDifferenz(rezdatum, erstbehandlung);
                    if (differenz < 0) {
                        fehlertext.add("<br><b><font color='#ff0000'>Rezeptdatum ist absolut kritisch!</font><br>Spanne zwischen Behandlungsbeginn und Rezeptdatum beträgt <font color='#ff0000'>"
                                + Long.toString(differenz)
                                + " Tag(e) </font>.<br>Behandlungsbeginn ist also <font color='#ff0000'>vor</font> dem  Ausstellungsdatum!!</b><br><br>");
                        testok = false;
                    }
                    if ((differenz = DatFunk.TageDifferenz(letztbeginn, erstbehandlung)) > 0) {
                        // System.out.println("Differenz 2 = "+differenz);
                        fehlertext.add("<br><b><font color='#ff0000'>Behandlungsbeginn ist kritisch!</font><br><br>Die Differenz zwischen <font color='#ff0000'>spätester Behandlungsbeginn</font> und 1.Behandlung<br>beträgt <font color='#ff0000'>"
                                + Long.toString(differenz) + " Tage </font><br>" + "</b><br><br>");
                        testok = false;
                    }
                    // Test auf Anregung von Michael Schütt
                    Vector<String> vtagetest = RezTools.holeEinzelTermineAusRezept(null, termine);
                    for (int i = 0; i < vtagetest.size(); i++) {
                        if ((differenz = DatFunk.TageDifferenz(vtagetest.get(i), DatFunk.sHeute())) < 0) {
                            fehlertext.add("<br><b><font color='#ff0000'>Behandlungsdatum " + vtagetest.get(i)
                                    + " ist kritisch!</font><br><br>"
                                    + "Das Behandlungsdatum <font color='#ff0000'></font> liegt in der Zukunft<br> <font color='#ff0000'>"
                                    + "um " + Long.toString(differenz * -1) + " Tage </font><br>" + "</b><br><br>");
                            testok = false;
                        }
                    }

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!testok) {
            JOptionPane.showMessageDialog(null, fehlertext.getTxt());
        }
        return testok;
    }

    private String getDialogText(boolean vorrangig, String heilmittel, String hmpos, String[] positionen) {
        String meldung = "Bei der Diagnosegruppe <b><font color='#ff0000'>" + diagnosegruppe + "</font></b> ist das "
                + (vorrangig ? "vorrangige " : "ergänzende") + " Heilmittel<br><br>--> <b><font color='#ff0000'>"
                + heilmittel + "</font></b> <-- nicht erlaubt!<br><br><br>" + "Mögliche "
                + (vorrangig ? "vorrangige " : "ergänzende") + " Heilmittel sind:<br><b><font color='#ff0000'>"
                + getErlaubteHeilmittel(positionen) + "</font></b><br><br>";
        return meldung;

    }

    /************************/
    private String getErlaubteHeilmittel(String[] heilmittel) {
        StringBuffer buf = new StringBuffer();
        String hm = "";
        for (int i = 0; i < heilmittel.length; i++) {
            hm = getHeilmittel(heilmittel[i]);
            if (!hm.equals("")) {
                buf.append(getHeilmittel(heilmittel[i]) + "<br>");
            }
        }
        return (buf.toString()
                   .equals("") ? "<br>keine<br>" : buf.toString());
    }

    /************************/
    private String getHeilmittel(String heilmittel) {
        for (int i = 0; i < preisvec.size(); i++) {
            if (preisvec.get(i)
                        .get(2)
                        .equals(heilmittel)) {
                return preisvec.get(i)
                               .get(0);
            }
        }
        return "";
    }


    public static int hmrTageDifferenz(String referenzdatum, String vergleichsdatum, int differenz,
            boolean samstagistwerktag) {
        int ret = 1;
        try {
            String letztesdatum = hmrLetztesDatum(referenzdatum, differenz, samstagistwerktag);
            ret = Integer.parseInt(Long.toString(DatFunk.TageDifferenz(letztesdatum, vergleichsdatum)));
        } catch (Exception ex) {
            System.out.println("Fehler in der Ermittlung der Unterbrechungszeiträume");
            ex.printStackTrace();
        }
        return ret;
    }

    public static String hmrLetztesDatum(String startdatum, int differenz, boolean samstagistwerktag) {

        int werktage = 0;
        Date date = null;


        try {
            date = sdDeutsch.parse(startdatum);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        while (true) {
            // System.out.println("Getestetes Datum = "+sd.format(date));
            if ((!(date.getDay() % 7 == 0)) && (samstagistwerktag)) {
                if (!istFeiertag(date)) {
                    if (werktage == differenz) {
                        return sdDeutsch.format(date);
                    }
                    werktage++;
                }
            } else if ((!(date.getDay() % 7 == 0)) && (!samstagistwerktag) && (!(date.getDay() % 6 == 0))) {
                if (!istFeiertag(date)) {
                    if (werktage == differenz) {
                        return sdDeutsch.format(date);
                    }
                    werktage++;
                }
            }

            date = new Date(date.getTime() + (24 * 60 * 60 * 1000));
        }
    }


    public static boolean istFeiertag(Date date) {

        if (SystemConfig.vFeiertage.contains(sdSql.format(date))) {
            return true;
        }
        return false;
    }

    private String pauseText (String rezNb, long pause) {
        return "Therapiepause <font color='#ff0000'>vor " + rezNb + "</font> beträgt <font color='#ff0000'>"
                + pause + "</font> Tage.</b><br>";
    }

}
