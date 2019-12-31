package hmrCheck;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import javax.swing.JOptionPane;

import CommonTools.DatFunk;
import CommonTools.SqlInfo;
import abrechnung.Disziplinen;
import commonData.Rezept;
import hauptFenster.Reha;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;

public class HMRCheck {
    Vector<Integer> anzahl = null;
    Vector<String> positionen = null;
    Vector<Vector<String>> preisvec = null;
    String indischluessel = null;
    int disziplin;
    int preisgruppe;
    final String maxanzahl = "Die Höchstmenge pro Rezept bei ";
    final String rotein = "<>";
    boolean testok = true;
    FehlerTxt fehlertext = null;
    int rezeptart;
    String reznummer = null;
    String rezdatum = null;
    String letztbeginn = null;
    boolean isAdRrezept = false;
    boolean isFolgerezept = false;  // 1x benutzt -> 0x
    boolean isErstVO = false;       // 1x benutzt
    boolean neurezept = false;
    boolean doppelbehandlung = false;
    boolean unter18 = false;
    static SimpleDateFormat sdDeutsch = new SimpleDateFormat("dd.MM.yyyy");
    static SimpleDateFormat sdSql = new SimpleDateFormat("yyyy-MM-dd");

    String[] rezarten = { "Erstverordnung", "Folgeverordnung", "Verordnung außerhalb des Regelfalles" };

    String[] keinefolgevo = { "EX1a", "EX1b", "EX1c", "WS1a", "WS1b", "WS1c", "WS1d", "WS1e", "AT1a", "AT1b", "AT1c",
            "SB4", "ST3" };
    String[] nurunter18 = { "ZN1a", "ZN1b", "ZN1c", "EN1", "PS1", "EX4a" };
    String[] nurueber18 = { "ZN2a", "ZN2b", "ZN2c", "EN2" };
    String[] wechselOK = { "WS2", "EX2", "EX3", "AT2", "LY2", "LY3", "SB5" }; // !nur! vom jeweils niedrigeren Index ist
                                                                              // Wechsel möglich (es sei denn der ist
                                                                              // auch enthalten, dann Rekursion)

    int maxprorezept = 0;
    int maxprofall = 0;
    Disziplinen diszis = null;

    public HMRCheck(String indi, int idiszi, Vector<Integer> vecanzahl, Vector<String> vecpositionen, int xpreisgruppe,
            Vector<Vector<String>> xpreisvec, int xrezeptart, String xreznr, String xrezdatum, String xletztbeginn) {
        indischluessel = indi;
        disziplin = idiszi;
        anzahl = vecanzahl;
        positionen = vecpositionen;
        preisgruppe = xpreisgruppe;
        preisvec = xpreisvec;
        rezeptart = xrezeptart;
        reznummer = xreznr;
        unter18 = DatFunk.Unter18(DatFunk.sHeute(), DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)));
        if (reznummer.equals("")) {
            neurezept = true;
        }
        rezdatum = xrezdatum;
        letztbeginn = xletztbeginn;
        // System.out.println("IDdiszi = "+idiszi);
        diszis = new Disziplinen();
        fehlertext = new FehlerTxt();
    }

    public HMRCheck(Rezept rezept, String currDiszi, Vector<Vector<String>> xpreisvec) {
        diszis = new Disziplinen();
        anzahl = new Vector<Integer>();
        positionen = new Vector<String>();
        indischluessel = rezept.getIndiSchluessel().replace(" ", "");
        disziplin = diszis.getIndex(currDiszi);
        for (int i = 0; i < 4; i++) {
            String tmp = rezept.getHmPos(i + 1);
            if (tmp != "") {
                anzahl.add(i, rezept.getAnzBeh(i + 1));
                positionen.add(i, tmp);
            } else {
                break;
            }
        }
        preisvec = xpreisvec;
        rezeptart = rezept.getRezArt();
        reznummer = rezept.getRezNb();
        unter18 = rezept.getUnter18();
        rezdatum = DatFunk.sDatInDeutsch(rezept.getRezeptDatum());
        letztbeginn = DatFunk.sDatInDeutsch(rezept.getLastDate());
        if (reznummer.equals("")) {
            neurezept = true;
        }
        fehlertext = new FehlerTxt();
    }

    /**
     *
     * Abhängig vom Indikationsschlüssel muß geprüft werden <p>
     * 1. Ist die Anzahl pro Rezept o.k.<p>
     * 2. Ist das gewählte Heilmittel o.k.<p>
     * 3. ist das ergänzende Heilmittel o.k.<p>
     *
     */
    public boolean check() {
        if (reznummer.startsWith("RS") || reznummer.startsWith("FT") || reznummer.startsWith("RH")) {
            // McM: ist das korrekt? (Reha taucht im HMK nicht auf)
            return true;
        }
        isAdRrezept = chkIsAdR(rezeptart);
        isFolgerezept = chkIsFolgeVO(rezeptart);
        isErstVO = chkIsErstVO(rezeptart);

        if (reznummer.startsWith("PO") && isErstVO) {
            try {
                if (Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(3)) > 3) {
                    JOptionPane.showMessageDialog(null,
                            "Fehler!\nAnzahl der Behandlungen bei Erstverordnung Podologie sind maximal 3 erlaubt!");
                    return false;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Fehler bei der Mengenermittlung Podologie-Rezept und Erstverordnung!");
                return false;
            }
        }
        Vector<Vector<String>> vec = SqlInfo.holeFelder(
                "select * from hmrcheck where indischluessel='" + indischluessel + "' LIMIT 1");

        if ((vec.size() <= 0 || indischluessel.equals("")) && (!indischluessel.equals("k.A."))) {
            JOptionPane.showMessageDialog(null,
                    "Indikationsschlüssel " + indischluessel + " unbekannt oder nicht angegeben!");
            return false;
        } else if (indischluessel.equals("k.A.")) {
            JOptionPane.showMessageDialog(null,
                    "Indikationsschlüssel " + indischluessel
                            + " (keine Angaben) wurde gewählt, HMR-Check wird abgebrochen.\n"
                            + "Bitte stellen Sie selbst sicher daß alle übrigen Pflichtangaben vorhanden sind");
            return true;
        }
        // System.out.println(vec);
        maxprorezept = Integer.parseInt(vec.get(0)
                                           .get(2));
        maxprofall = Integer.parseInt(vec.get(0)
                                         .get(1));
        String[] vorrangig = vec.get(0)
                                .get(3)
                                .split("@");
        String[] ergaenzend = vec.get(0)
                                 .get(5)
                                 .split("@");
        for (int i = 0; i < vorrangig.length; i++) {
            vorrangig[i] = diszis.getPrefix(disziplin) + vorrangig[i];
        }
        for (int i = 0; i < ergaenzend.length; i++) {
            ergaenzend[i] = diszis.getPrefix(disziplin) + ergaenzend[i];
        }
        // hier einbauen:
        // testen auf WS1,Ex1 etc. hier ist keine Folgeverordnung möglich
        // Status:erledigt!!
        // testen auf Doppelbehandlung und Verordnungsmenge
        // Status:erledigt
        // testen auf außerhalb des Regelfalles (hebt) die Verordnungsmenge auf
        // Status:erledigt aber halblebig
        // testen auf Rezdatum und Behandlungsbeginn = 0.k.
        // Status:ausstehend
        // testen ob Unterbrechungen zwischen den Behandlungen o.k.
        // Status:ausstehend

        // mögliche Höchstmenge pro Rezept wurde überschritten?
        // System.out.println("Max pro Rezept="+maxprorezept);
        // System.out.println("Anzahlen = "+anzahl);
        for (int i = 0; i < anzahl.size(); i++) {
            if ((anzahl.get(i) > maxprorezept) && (!isAdRrezept)) {
                fehlertext.add("<b>Bei Indikationsschlüssel " + indischluessel
                        + " sind maximal<br><font color='#ff0000'>" + Integer.toString(maxprorezept)
                        + " Behandlungen</font> pro Rezept erlaubt!!<br><br>"
                        + "Möglickeit -> Ändern der Rezeptart auf außerhalb des Regelfalles<br><br></b>");
                testok = false;
            }
        }
        // Checken ob Indischlüssel in der Liste der Schlüssel ohne Folgeverordnung
        // enthalten ist
        if ((Arrays.asList(keinefolgevo)
                   .contains(indischluessel))
                && (rezeptart > 0)) {
            fehlertext.add("<b>Bei Indikationsschlüssel " + indischluessel + " ist keine<br><font color='#ff0000'>"
                    + rezarten[rezeptart] + "</font> erlaubt!!</b><br><br>");
            testok = false;
        }
        // Hier den Folgeverordnungstest einbauen, bzw. testen ob Höchstverordnungsmenge
        // überschritten wird und deshalb VoAdr fällig ist
        if (!checkeVoFolgeKorrekt()) {
            testok = false;
        }
        // Hier der Check ob für Kinder ein Erwachsenen-Indischlüssel verwendet wurde
        // z.B. ZN2a
        if ((unter18) && (Arrays.asList(nurueber18)
                                .contains(indischluessel))) {
            fehlertext.add("<b>Der Indikationsschlüssel " + indischluessel + " ist nur bei <br><font color='#ff0000'>"
                    + "Erwachsenen über 18 Jahren" + "</font> erlaubt!!</b><br><br>");
            testok = false;
            // Hier der Check ob für Erwachsene ein Kinder-Indischlüssel verwendet wurde
            // z.B. ZN1a
        } else if ((!unter18) && (Arrays.asList(nurunter18)
                                        .contains(indischluessel))) {
            fehlertext.add("<b>Der Indikationsschlüssel " + indischluessel + " ist nur bei <br><font color='#ff0000'>"
                    + "Kindern und Jugendlichen bis 18 Jahren" + "</font> erlaubt!!</b><br><br>");
            testok = false;
        }

        try {
            if (positionen.size() >= 2) {
                if (positionen.get(0)
                              .equals(positionen.get(1))) {
                    doppelbehandlung = true;
                    int doppelgesamt = anzahl.get(0) + anzahl.get(1);
                    if ((doppelgesamt > maxprorezept) && (!isAdRrezept)) {
                        fehlertext.add("<b>Die Doppelbehandlung bei Indikationsschlüssel "
                                + indischluessel
                                + ", übersteigt<br>die maximal erlaubte Höchstverordnungsmenge pro Rezept von<br><font color='#ff0000'>"
                                + Integer.toString(maxprorezept)
                                + " Behandlungen</font>!!</b><br><br>Wechsel auf -> außerhalb des Regelfalles <- ist erforderlich<br><br>");
                        testok = false;
                    }
                }
            }
            // jetzt haben wir schon einmal die Doppelbehandlung
            // dann testen ob die Positionsnummer überhaupt ein zugelassenes vorrangiges
            // Heilmittel ist.

            int posGesamt = positionen.size();
            for (int i = 0; i < posGesamt; i++) {
                // Hier Doppelbehandlung einbauen start
                String currPos = positionen.get(i);
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
                                        reznummer);
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
                // Hier Doppelbehandlung einbauen ende
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
        String meldung = "Bei dem Indikationsschlüssel <b><font color='#ff0000'>" + indischluessel + "</font></b> ist das "
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

    /**
    * Leerzeichen zw. Diagnosengruppe (2 oder 3 Zeichen) u. Leitsymptomatik
    *
    */
    private String separiereLeitsymptomatik(String indiSchl) {
        String tmp = indiSchl.trim();
        if ("abcdef".indexOf(indiSchl.substring(indiSchl.length() - 1, indiSchl.length())) >= 0) {

            tmp = indiSchl.substring(0, indiSchl.length() - 1) + " " + indiSchl.substring(indiSchl.length() - 1);
        } else {

            // Norm bei Ergo/Logo - wird hier als Text angegeben
        }
        return tmp;
    }

    private String getDg(String indiSchl) {
        String[] tmp = indiSchl.split(" ");
        String diagnosegruppe = tmp[0];
        return diagnosegruppe;
    }


    private int getDiagGrpIdx(String indiSchl) {
        String dGroup = getDg(indiSchl);
        String lastChar = dGroup.substring(dGroup.length() - 1);
        if ("123456789".indexOf(lastChar) >= 0) { // bisher im HMK vorh. Werte 1..7
            int dgIndex = Integer.valueOf(lastChar);
            return dgIndex;
        }
        return -1; // Diagnosegruppe enthaelt keinen numer. Index
    }

    private String mkIndiSearch(String indiSchl) {
        String search4indi = null;
        String diagGrp = getDg(indiSchl);
        int dgIndex = getDiagGrpIdx(indiSchl);
        while (Arrays.asList(wechselOK)
                     .contains(diagGrp)) { // betrifft nur Diagnosegruppen mit numer. Index
            if (search4indi == null) {     // wenn Wechsel der Diagnosegruppe möglich kann diese bei
                                           // Vorgängerrezepten einen niedrigeren Index haben
                search4indi = "indikatschl like '" + diagGrp + "%'";
            }
            diagGrp = diagGrp.replace(Integer.toString(dgIndex), Integer.toString(dgIndex - 1));
            search4indi = search4indi + " OR indikatschl like '" + diagGrp + "%'";
            dgIndex--;
        }
        if (search4indi == null) { // kein Wechsel der Diagnosegruppe möglich
            return "indikatschl like '" + diagGrp + "%'"; // aber Wechsel der Leitsymptomatik ist auch hier erlaubt
        } else {
            return "(" + search4indi + ")";
        }
    }

    public boolean checkeVoFolgeKorrekt() {

        String patintern = null;
        String aktindischl = indischluessel;
        int index = 0;
        String startdatum_neu = null;
        String enddatum_alt = null;
        long therapiepause = 12 * 7;
        long tagedifferenz = 0;
        try {

            aktindischl = separiereLeitsymptomatik(indischluessel);


            if (Reha.instance.patpanel != null) {
                patintern = Reha.instance.patpanel.patDaten.get(29);
                String selFieldsFrom = "select rez_datum,rez_nr,rezeptart,anzahl1,indikatschl,termine,pat_intern from ";
                String selCond = "where pat_intern = '" + patintern + "'" + " and rez_nr like '"
                        + diszis.getRezClass(disziplin) + "%' and " + mkIndiSearch(aktindischl);
                String stmt = "(" + selFieldsFrom + " verordn " + selCond + ")" + " union " + "(" + selFieldsFrom
                        + " lza " + selCond + ")" + " order by rez_datum DESC, indikatschl";
                Vector<Vector<String>> testvec = SqlInfo.holeFelder(stmt); // alle Rezepte aus verordn u. lza, sortiert
                                                                           // nach datum u. Indi-Schlüssel
                // Indices für Zugriffe auf testvec
                final int REZ_DATUM = 0, REZ_NR = 1, REZEPTART = 2, ANZAHL = 3, INDIKATSCHL = 4, TERMINE = 5, PAT_INTERN = 6;

                // erstmal die eindeutigen Faelle aussortieren
                if (chkIsAdR(rezeptart)) { // AdR-VO: keine Einschränkung
                    return true;
                }

                String fail = "Keine Vorgänger-VO vorhanden. <br>";
                if (testvec.size() == 1) { // entweder schon gespeichert, oder es gibt genau ein Vorrezept
                    if (!neurezept) {
                        String currRez = testvec.get(0)
                                                .get(REZ_NR);
                        if (currRez.equals(reznummer)) {            // schon gespeichert, kein Vorrezept
                            return shouldBeErstVO(rezeptart, fail); // muesste dann Erstverordnung sein
                        }
                    }
                } else if (testvec.size() == 0 && neurezept) {
                    return shouldBeErstVO(rezeptart, fail);         // muesste auch Erstverordnung sein
                }

                boolean zaehlen = false;
                int gesamt = 0;
                int aktanzahl = 0, idxVorg = 0;
                int rezDiagGrpIdx = getDiagGrpIdx(aktindischl);
                boolean neudummy = Boolean.valueOf(neurezept);
                String currRezVoArt = null,  vorgaengerVoArt = null;

                if (neudummy) {
                    // Daten aus Formular in testvec einfügen
                    Vector<String> neuRez = new Vector<String>();
                    Collections.addAll(neuRez, rezdatum, diszis.getRezClass(disziplin) + "(noch nicht gespeichert)", String.valueOf(rezeptart),
                            String.valueOf(anzahl.get(0)), aktindischl, "", "");
                    testvec.insertElementAt(neuRez, 0);
                    reznummer = neuRez.get(REZ_NR);
                }
                for (int i = 0; i < testvec.size(); i++) {
                    String currRez = testvec.get(i)
                                            .get(REZ_NR);
                    if (currRez.equals(reznummer) || zaehlen || neudummy) {
                        zaehlen = true;
                        neudummy = false;
                      if (currRez.equals(reznummer)) {
                            // beim Test geaenderte(?) Werte aus Formular verwenden
                            testvec.get(i).set(REZ_DATUM, DatFunk.sDatInSQL(rezdatum));
                            testvec.get(i).set(REZEPTART, String.valueOf(rezeptart));
                            testvec.get(i).set(ANZAHL, String.valueOf(anzahl.get(0)));
                            testvec.get(i).set(INDIKATSCHL, aktindischl);
                            // rez_nr, termine u. pat_intern koennen im Formular nicht geändert werden

                            currRezVoArt = testvec.get(i)
                                                  .get(REZEPTART);
                        }
                        int currIdx = getDiagGrpIdx(testvec.get(i)
                                                           .get(INDIKATSCHL));
                        if (rezDiagGrpIdx < currIdx) {
                            continue; // Idx darf nur kleiner werden -> Rez. ignorieren (? besser Abbruch ?)
                        } else if (rezDiagGrpIdx > currIdx) {
                            rezDiagGrpIdx = currIdx;
                        }

                        // erst prüfen ob zwischen letzter Behandlung des alten Rezeptes und dem ersten
                        // Termin der neuen VO 12 Wochen Therapiepause lagen,
                        // sofern ja -> nicht summieren (muesste dann Erstverordnung sein)
                            idxVorg = i + 1;
                            aktanzahl = Integer.parseInt(testvec.get(i)
                                                                .get(ANZAHL));
                        if (idxVorg <= testvec.size()) {
                                startdatum_neu = DatFunk.sDatInDeutsch(testvec.get(i)
                                                                              .get(REZ_DATUM)); // McM: dito
                            while ((idxVorg < testvec.size()) && (testvec.get(idxVorg)
                                                                         .get(TERMINE)
                                                                         .length() == 0)) { // falls keine Termine
                                                                                            // eingetragen sind -> zum
                                                                                            // Vorgänger wechseln
                                gesamt = gesamt + Integer.parseInt(testvec.get(idxVorg)
                                                                          .get(ANZAHL)); // die verordneten Behandlungen
                                                                                    // zaehlen trotzdem mit
                                // test ob gesamt > maxProFall
                                vorgaengerVoArt = testvec.get(idxVorg)
                                                         .get(REZEPTART);
                                String vorgaengerVoNr = testvec.get(idxVorg)
                                                               .get(REZ_NR);
                                if (chkIsErstVO(vorgaengerVoArt)) { // wenn die Uebersprungene die Erst-VO ist -> Abbruch
                                    return chkIf2ErstVO(currRezVoArt, vorgaengerVoNr);
                                }
                                if (chkIsAdR(vorgaengerVoArt)) { // wenn die Uebersprungene eine AdR-VO ist -> Abbruch
                                    return chkIf2AdrVO(currRezVoArt, vorgaengerVoNr);
                                }
                                currRez = testvec.get(idxVorg)
                                                 .get(REZ_NR);
                                currRezVoArt = testvec.get(idxVorg)
                                                      .get(REZEPTART);
                                idxVorg++; // jetzt Index auf Vorgänger setzen
                            }

                            if (idxVorg >= testvec.size()) {
                                if (chkIsErstVO(currRezVoArt)) {
                                    return true;
                                } else {
                                    return shouldBeErstVO(currRezVoArt, "<br>Keine zu " + reznummer
                                            + "  gehörige<b><font color='#ff0000'> Erstverordnung </font></b>gefunden!<br>");
                                }
                            } else if (testvec.get(idxVorg)
                                              .get(TERMINE)
                                              .length() == 0) {
                                // Vorgänger enthält (auch) keine Behandlungen
                                enddatum_alt = RezTools.holeLetztenTermin(null,
                                        testvec.get(i)
                                               .get(TERMINE)); // dummy (damit die Berechnung durchlaeuft)
                            } else { // Vorgänger ist vorhanden und es sind Termine eingetragen
                                enddatum_alt = RezTools.holeLetztenTermin(null, testvec.get(idxVorg)
                                                                                       .get(TERMINE));
                            }


                            gesamt = gesamt + aktanzahl;

                            if (gesamt > maxprofall) {
                                fehlertext.add("<br><b><font color='#ff0000'>Höchstverordnungsmenge ist überschritten -> "
                                        + Integer.toString(gesamt) + " Behandlungen"
                                        + "</font><br>Wechsel auf <font color='#ff0000'>außerhalb des Regelfalles</font> ist erforderlich<br>"
                                        + "Höchstverordnungsmenge im Regelfall ist<br>bei <font color='#ff0000'>"
                                        + aktindischl + " = " + Integer.toString(maxprofall)
                                        + "</font> Behandlungen<br>" + "</b><br><br>");
                                return false;
                            }

                            tagedifferenz = DatFunk.TageDifferenz(enddatum_alt, startdatum_neu);
                            vorgaengerVoArt = testvec.get(idxVorg)
                                                     .get(REZEPTART);


                            if (tagedifferenz > therapiepause) { // Therapiepause ueberschritten
                                if (chkIsErstVO(currRezVoArt)) {
                                    return true;
                                } else {
                                    return shouldBeErstVO(currRezVoArt, pauseText(currRez, tagedifferenz));
                                }
                            } else {
                                String vorgaengerVoNr = testvec.get(idxVorg)
                                                               .get(REZ_NR);
                                if (chkIsErstVO(vorgaengerVoArt)) {
                                    return chkIf2ErstVO(currRezVoArt, vorgaengerVoNr);
                                }
                                if (chkIsAdR(vorgaengerVoArt)) {
                                    return chkIf2AdrVO(currRezVoArt, vorgaengerVoNr);
                                }
                            }

                            i = --idxVorg; // Index anpassen, falls Neurezept oder Rezepte uebersprungen wurden
                        }

                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Fehler in der Prüfung auf Folgerezepte\n\nBitte Informieren Sie den Systemadministrator");
        }
        // true damit die restliche Prüfung durchlaufen wird.
        return true;
    }

    private boolean chkIf2ErstVO(String currRezVoArt, String vorgaengerVoNr) {
        if (chkIsErstVO(currRezVoArt)) {
            return shouldBeFolgeVO(currRezVoArt,
                    "<b><font color='#ff0000'>Erstverordnung " + vorgaengerVoNr
                            + " </font></b>unter den Vorgängern von <b><font color='#ff0000'>"
                            + reznummer + "</font></b> gefunden.<br>");
        } else {
            return true;
        }
    }

    private boolean chkIf2AdrVO(String currRezVoArt, String vorgaengerVoNr) {
        return shouldBeAdR(currRezVoArt, "<b><font color='#ff0000'>A.d.R.-Verordnung "
                + vorgaengerVoNr + "</font></b> unter den Vorgänger_VOs gefunden.<br><br>");
    }

    private String pauseText (String rezNb, long pause) {
        return "Therapiepause <font color='#ff0000'>vor " + rezNb + "</font> beträgt <font color='#ff0000'>"
                + pause + "</font> Tage.</b><br>";
    }

    private boolean chkIsErstVO(int rezeptArt) {
        if (rezarten[rezeptArt] == "Erstverordnung") {
            return true;
        } else {
            return false;
        }
    }

    private boolean chkIsErstVO(String rezeptArt) {
        return chkIsErstVO(Integer.parseInt(rezeptArt));
    }

    private boolean shouldBeErstVO(int rezeptArt, String why) {
        if (chkIsErstVO(rezeptArt)) {
            return true;
        } else {
            fehlertext.add((why.length() > 0 ? "<br>" : "") + why
                    + "<br>Verordnung müsste<b><font color='#ff0000'> Erstverordnung </font></b>sein.<br><br>"
                    + "<br><br>");
            return false;
        }
    }

    private boolean shouldBeErstVO(String rezeptArt, String why) {
        return shouldBeErstVO(Integer.parseInt(rezeptArt), why);
    }

    private boolean chkIsFolgeVO(int rezeptArt) {
        if (rezarten[rezeptArt] == "Folgeverordnung") {
            return true;
        } else {
            return false;
        }
    }

    private boolean shouldBeFolgeVO(int rezeptArt, String why) {
        if (chkIsFolgeVO(rezeptArt)) {
            return true;
        } else {
            fehlertext.add((why.length() > 0 ? "<br>" : "") + why
                    + "<br>Verordnung müsste<b><font color='#ff0000'> Folgeverordnung </font></b>sein.<br>"
                    + "<font color='#ff0000'> Bitte v. Hd. nachprüfen! </font><br><br>");
            return false;
        }
    }

    private boolean shouldBeFolgeVO(String rezeptArt, String why) {
        return shouldBeFolgeVO(Integer.parseInt(rezeptArt), why);
    }

    private boolean chkIsAdR(int rezeptArt) {
        if (rezarten[rezeptArt].equals("Verordnung außerhalb des Regelfalles")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean chkIsAdR(String rezeptArt) {
        return chkIsAdR(Integer.parseInt(rezeptArt));
    }

    private boolean shouldBeAdR(int rezeptArt, String why) {
        if (chkIsAdR(rezeptArt)) {
            return true;
        } else {
            fehlertext.add((why.length() > 0 ? "<br>" : "") + why
                    + "Verordnung müsste <b><font color='#ff0000'> A.d.R.-Verordnung </font></b>sein.<br>"
                    + "<font color='#ff0000'> Bitte v. Hd. nachprüfen! </font><br><br>");
            return false;
        }
    }

    private boolean shouldBeAdR(String rezeptArt, String why) {
        return shouldBeAdR(Integer.parseInt(rezeptArt), why);
    }
}
