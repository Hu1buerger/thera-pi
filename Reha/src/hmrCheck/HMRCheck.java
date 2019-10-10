package hmrCheck;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import javax.swing.JOptionPane;

import CommonTools.DatFunk;
import CommonTools.SqlInfo;
import abrechnung.Disziplinen;
import hauptFenster.Reha;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;

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
    String fehlertext = "";
    int rezeptart;
    String reznummer = null;
    String rezdatum = null;
    String letztbeginn = null;
    boolean isAdRrezept = false;
    boolean isFolgerezept = false;
    boolean isErstVO = false;
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
    }

    /*
     *
     * Abhängig vom Indikationsschlüssel muß geprüft werden 
     * 1. Ist die Anzahl pro Rezept o.k.
     * 2. Ist das gewählte Heilmittel o.k.
     * 3. ist das ergänzende Heilmittel o.k.
     *
     */
    public boolean check() {
        if (reznummer.startsWith("RS") || reznummer.startsWith("FT") || reznummer.startsWith("RH")) { 
            // McM: ist das korrekt? (Reha taucht im HMK nicht auf)
            return true;
        }
        // isAdRrezept = (rezeptart == 2);
        isAdRrezept = chkIsAdR(rezeptart);
        // isFolgerezept = (rezeptart == 1);
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
                fehlertext = String.valueOf("<html><b>Bei Indikationsschlüssel " + indischluessel
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
            fehlertext = fehlertext + String.valueOf((fehlertext.length() <= 0 ? "<html>" : "")
                    + "<b>Bei Indikationsschlüssel " + indischluessel + " ist keine<br><font color='#ff0000'>"
                    + rezarten[rezeptart] + "</font> erlaubt!!</b><br><br>");
            testok = false;
        }
        // Hier den Folgeverordnungstest einbauen, bzw. testen ob Höchstverordnungsmenge
        // überschritten wird und deshalb VoAdr fällig ist
        if (this.isFolgerezept) { // kann das nicht auch falsch angegeben sein?
            testok = checkeVoFolgeKorrekt();
        }
        // Hier der Check ob für Kinder ein Erwachsenen-Indischlüssel verwendet wurde
        // z.B. ZN2a
        if ((unter18) && (Arrays.asList(nurueber18)
                                .contains(indischluessel))) {
            fehlertext = fehlertext + String.valueOf((fehlertext.length() <= 0 ? "<html>" : "")
                    + "<b>Der Indikationsschlüssel " + indischluessel + " ist nur bei <br><font color='#ff0000'>"
                    + "Erwachsenen über 18 Jahren" + "</font> erlaubt!!</b><br><br>");
            testok = false;
            // Hier der Check ob für Erwachsene ein Kinder-Indischlüssel verwendet wurde
            // z.B. ZN1a
        } else if ((!unter18) && (Arrays.asList(nurunter18)
                                        .contains(indischluessel))) {
            fehlertext = fehlertext + String.valueOf((fehlertext.length() <= 0 ? "<html>" : "")
                    + "<b>Der Indikationsschlüssel " + indischluessel + " ist nur bei <br><font color='#ff0000'>"
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
                        fehlertext = String.valueOf("<html><b>Die Doppelbehandlung bei Indikationsschlüssel "
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
                            fehlertext = fehlertext
                                    + String.valueOf(getDialogText(true, getHeilmittel(currPos), currPos, vorrangig));
                            testok = false;
                        }
                    }
                } else if (i == 1 && doppelbehandlung) {

                } else {
                    if (!isOptional) {
                        fehlertext = fehlertext
                                + String.valueOf(getDialogText(false, getHeilmittel(currPos), currPos, ergaenzend));
                        testok = false;
                    }
                }
                // Hier Doppelbehandlung einbauen ende
            }

            // Jetzt auf Rezeptbeginn testen
            if (neurezept) {
                long differenz = DatFunk.TageDifferenz(rezdatum, DatFunk.sHeute());
                if (differenz < 0) {
                    fehlertext = fehlertext + (fehlertext.length() <= 0 ? "<html>" : "")
                            + "<br><b><font color='#ff0000'>Rezeptdatum ist absolut kritisch!</font><br>Spanne zwischen Behandlungsbeginn und Rezeptdatum beträgt <font color='#ff0000'>"
                            + Long.toString(differenz)
                            + " Tag(e) </font>.<br>Behandlungsbeginn ist also <font color='#ff0000'>vor</font> dem  Ausstellungsdatum!!</b><br><br>";
                    testok = false;
                }
                if ((differenz = DatFunk.TageDifferenz(letztbeginn, DatFunk.sHeute())) > 0) {
                    // System.out.println("Differenz 2 = "+differenz);
                    fehlertext = fehlertext + (fehlertext.length() <= 0 ? "<html>" : "")
                            + "<br><b><font color='#ff0000'>Behandlungsbeginn ist kritisch!</font><br><br>Die Differenz zwischen <font color='#ff0000'>spätester Behandlungsbeginn</font> und 1.Behandlung<br>beträgt <font color='#ff0000'>"
                            + Long.toString(differenz) + " Tage </font><br>" + "</b><br><br>";
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
                        fehlertext = fehlertext + (fehlertext.length() <= 0 ? "<html>" : "")
                                + "<br><b><font color='#ff0000'>Rezeptdatum ist absolut kritisch!</font><br>Spanne zwischen Behandlungsbeginn und Rezeptdatum beträgt <font color='#ff0000'>"
                                + Long.toString(differenz)
                                + " Tag(e) </font>.<br>Behandlungsbeginn ist also <font color='#ff0000'>vor</font> dem  Ausstellungsdatum!!</b><br><br>";
                        testok = false;
                    }
                    if ((differenz = DatFunk.TageDifferenz(letztbeginn, DatFunk.sHeute())) > 0) {
                        // System.out.println("Differenz 2 = "+differenz);
                        fehlertext = fehlertext + (fehlertext.length() <= 0 ? "<html>" : "")
                                + "<br><b><font color='#ff0000'>Behandlungsbeginn ist kritisch!</font><br><br>Die Differenz zwischen <font color='#ff0000'>spätester Behandlungsbeginn</font> und 1.Behandlung<br>beträgt <font color='#ff0000'>"
                                + Long.toString(differenz) + " Tage </font><br>" + "</b><br><br>";
                        testok = false;
                    }

                } else {
                    // LetzterBeginn abhandeln
                    String erstbehandlung = RezTools.holeEinzelTermineAusRezept(null, termine)
                                                    .get(0);
                    long differenz = DatFunk.TageDifferenz(rezdatum, erstbehandlung);
                    if (differenz < 0) {
                        fehlertext = fehlertext + (fehlertext.length() <= 0 ? "<html>" : "")
                                + "<br><b><font color='#ff0000'>Rezeptdatum ist absolut kritisch!</font><br>Spanne zwischen Behandlungsbeginn und Rezeptdatum beträgt <font color='#ff0000'>"
                                + Long.toString(differenz)
                                + " Tag(e) </font>.<br>Behandlungsbeginn ist also <font color='#ff0000'>vor</font> dem  Ausstellungsdatum!!</b><br><br>";
                        testok = false;
                    }
                    if ((differenz = DatFunk.TageDifferenz(letztbeginn, erstbehandlung)) > 0) {
                        // System.out.println("Differenz 2 = "+differenz);
                        fehlertext = fehlertext + (fehlertext.length() <= 0 ? "<html>" : "")
                                + "<br><b><font color='#ff0000'>Behandlungsbeginn ist kritisch!</font><br><br>Die Differenz zwischen <font color='#ff0000'>spätester Behandlungsbeginn</font> und 1.Behandlung<br>beträgt <font color='#ff0000'>"
                                + Long.toString(differenz) + " Tage </font><br>" + "</b><br><br>";
                        testok = false;
                    }
                    // Test auf Anregung von Michael Schütt
                    Vector<String> vtagetest = RezTools.holeEinzelTermineAusRezept(null, termine);
                    for (int i = 0; i < vtagetest.size(); i++) {
                        if ((differenz = DatFunk.TageDifferenz(vtagetest.get(i), DatFunk.sHeute())) < 0) {
                            fehlertext = fehlertext + (fehlertext.length() <= 0 ? "<html>" : "")
                                    + "<br><b><font color='#ff0000'>Behandlungsdatum " + vtagetest.get(i)
                                    + " ist kritisch!</font><br><br>"
                                    + "Das Behandlungsdatum <font color='#ff0000'></font> liegt in der Zukunft<br> <font color='#ff0000'>"
                                    + "um " + Long.toString(differenz * -1) + " Tage </font><br>" + "</b><br><br>";
                            testok = false;
                        }
                    }

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!testok) {
            JOptionPane.showMessageDialog(null, fehlertext + "</html>");
        }
        return testok;
    }

    private String getDialogText(boolean vorrangig, String heilmittel, String hmpos, String[] positionen) {
        String meldung = (fehlertext.length() <= 0 ? "<html>" : "")
                + "Bei dem Indikationsschlüssel <b><font color='#ff0000'>" + indischluessel + "</font></b> ist das "
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

    /************************/

    public static String X_holeLetztMoeglichenTag(boolean erstebehandlung, String disziplin, int preisgruppe,
            String referenz) {
        int frist = (Integer) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                      .get((erstebehandlung ? 0 : 2))).get(preisgruppe);
        boolean kalendertage = (Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                                 .get((erstebehandlung ? 1 : 3))).get(
                                                                                         preisgruppe);
        boolean mitsamstag = (Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                               .get((erstebehandlung ? 4 : 5))).get(
                                                                                       preisgruppe);
        if (kalendertage) {
            return DatFunk.sDatPlusTage(referenz, frist);
        }
        return hmrLetztesDatum(referenz, frist, mitsamstag);
    }

    public static long X_hmrTageErmitteln(int preisgruppe, String reznum, String rezdatum, String testdatum,
            String letzter) {
        long differenz = DatFunk.TageDifferenz(rezdatum, testdatum);
        int wotag = DatFunk.TagDerWoche(rezdatum);
        // System.out.println("Tag der Woche der Rezeptausstellung = "+wotag);
        // System.out.println("Preisgruppe = "+preisgruppe);
        // System.out.println("Tage Differenz insgesamt = "+differenz);
        if (preisgruppe != 1 && differenz > 10) {
            if (wotag >= 3) {
                differenz -= 2;
            } else {
                differenz -= 1;
            }
        }

        return differenz;
    }

    public static int[] XX_hmrArbeitsTage(Date start, Date ende, boolean samstagWochenende, boolean checkFeiertage) {
        int[] ret = { 0, 0, 0 }; // 0=Arbeitstag,1=wochenende,2=Feiertag

        for (Date date = start; date.compareTo(ende) <= 0; date = new Date(date.getTime() + 24 * 60 * 60 * 1000)) {
            if (date.getDay() % 7 == 0) {
                ret[1]++;
            } else if (checkFeiertage && istFeiertag(date)) { // selber implementieren
                ret[2]++;
            } else {
                ret[0]++;
            }
        }
        return ret;
    }

    /******************************************/
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

    /******************************************/

    public static String hmrLetztesDatum(String startdatum, int differenz, boolean samstagistwerktag) {
        // SimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy");
        int i = 0;
        int werktage = 0;
        Date date = null;
        // System.out.println("Übergabe = "+startdatum);

        try {
            date = sdDeutsch.parse(startdatum);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // System.out.println("geparstes Datum = "+sd.format(date));
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
            i++;
            date = new Date(date.getTime() + (24 * 60 * 60 * 1000));
        }
    }

    public static int XX_hmrAnzahlTageLetztesDatum(String startdatum, int differenz) {
        // SimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy");
        int i = 0;
        int werktage = 0;

        Date date = null;
        // System.out.println("Übergabe = "+startdatum);

        try {
            date = sdDeutsch.parse(startdatum);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // System.out.println("geparstes Datum = "+sd.format(date));
        while (true) {
            // System.out.println("Getestetes Datum = "+sd.format(date));
            if (!(date.getDay() % 7 == 0)) {
                if (!istFeiertag(date)) {
                    if (werktage == differenz) {
                        return Integer.valueOf(i);
                    }
                    werktage++;
                }
            }
            i++;
            date = new Date(date.getTime() + (24 * 60 * 60 * 1000));
        }
    }

    public static boolean istFeiertag(Date date) {
        // SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
        // System.out.println("*******************"+sd.format(date));
        if (SystemConfig.vFeiertage.contains(sdSql.format(date))) {
            return true;
        }
        return false;
    }

    private String separiereLeitsymptomatik(String indiSchl) {
        String tmp = indiSchl.trim();
        if ("abcdef".indexOf(indiSchl.substring(indiSchl.length() - 1, indiSchl.length())) >= 0) {
            // Leerzeichen zw. Diagnosengruppe (2 oder 3 Zeichen) u. Leitsymptomatik
            tmp = indiSchl.substring(0, indiSchl.length() - 1) + " " + indiSchl.substring(indiSchl.length() - 1);
        } else {
            // System.out.println("HMRCheck: Indikationsschlüssel ohne Leitsymptomatik: "+tmp); 
            // Norm bei Ergo/Logo - wird hier als Text angegeben
        }
        return tmp;
    }

    private String getDg(String indiSchl) {
        String[] tmp = indiSchl.split(" ");
        String diagnosegruppe = tmp[0];
        return diagnosegruppe;
    }

    private int getDgIndex(String indiSchl) {
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
        int dgIndex = getDgIndex(indiSchl);
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
//            return "indikatschl = '"+indiSchl+"'";
            return "indikatschl like '" + diagGrp + "%'"; // aber Wechsel der Leitsymptomatik ist auch hier erlaubt
        } else {
            return "(" + search4indi + ")";
        }
    }

    public boolean checkeVoFolgeKorrekt() {
        // variable für höchstverordnungsmenge
        // maxprofall
        // indischluessel
        // anzahl.get(0);
        String patintern = null;
        String aktindischl = indischluessel;
        int index = 0;
        String startdatum_neu = null;
        String enddatum_alt = null;
        long therapiepause = 12 * 7;
        long tagedifferenz = 0;
        try {

            aktindischl = separiereLeitsymptomatik(indischluessel);
            // System.out.println(aktindischl);

            if (Reha.instance.patpanel != null) {
                patintern = Reha.instance.patpanel.patDaten.get(29);
                String selFieldsFrom = "select rez_datum,rez_nr,rezeptart,anzahl1,indikatschl,termine,pat_intern from "; 
                String selCond = "where pat_intern = '" + patintern + "'" + " and rez_nr like '"
                        + diszis.getRezClass(disziplin) + "%' and " + mkIndiSearch(aktindischl);
                String stmt = "(" + selFieldsFrom + " verordn " + selCond + ")" + " union " + "(" + selFieldsFrom
                        + " lza " + selCond + ")" + " order by rez_datum DESC, indikatschl";
                Vector<Vector<String>> testvec = SqlInfo.holeFelder(stmt); // alle Rezepte aus verordn u. lza, sortiert
                                                                           // nach datum u. Indi-Schlüssel

                // erstmal die eindeutigen Faelle aussortieren

                if (testvec.size() == 1) { // entweder schon gespeichert, oder es gibt genau ein Vorrezept
                    if (!neurezept) {
                        String currRez = testvec.get(0)
                                                .get(1);
                        if (currRez.equals(reznummer)) {          // schon gespeichert, kein Vorrezept
                            return shouldBeErstVO(rezeptart, ""); // muesste dann Erstverordnung sein
                        }
                    }
                } else if (testvec.size() == 0 && neurezept) {
                    return shouldBeErstVO(rezeptart, "");         // muesste auch Erstverordnung sein
                }

                boolean zaehlen = false;
                int gesamt = 0;
                int aktanzahl = 0, idxVorg = 0;
                int rezDgIdx = getDgIndex(aktindischl);
                boolean neudummy = Boolean.valueOf(neurezept);
                String currRezVoArt = null,  vorgaengerVoArt = null;

                //   0         1      2         3        4          5       6
                // rez_datum,rez_nr,rezeptart,anzahl1,indikatschl,termine,pat_intern
                for (int i = 0; i < testvec.size(); i++) {
                    String currRez = testvec.get(i)
                                            .get(1);
                    if (currRez.equals(reznummer) || zaehlen || neudummy) {
                        zaehlen = true;
                        if (currRez.equals(reznummer)) {
                            currRezVoArt = testvec.get(i)
                                              .get(2);
                        }
                        int currIdx = getDgIndex(testvec.get(i)
                                                        .get(4));
                        if (rezDgIdx < currIdx) {
                            continue; // Idx darf nur kleiner werden -> Rez. ignorieren (? besser Abbruch ?)
                        } else if (rezDgIdx > currIdx) {
                            rezDgIdx = currIdx;
                        }

                        // erst prüfen ob zwischen letzter Behandlung des alten Rezeptes und dem ersten
                        // Termin der neuen VO 12 Wochen Therapiepause lagen, 
                        // sofern ja -> nicht summieren (muesste dann Erstverordnung sein)
                        if (neudummy) {
                            idxVorg = 0; // Index '0' ist Vorgänger des gerade angelegten
                                         // (u. noch nicht in der DB gespeicherten) Rezeptes
                            aktanzahl = anzahl.get(0);
                        } else {
                            idxVorg = i + 1;
                            aktanzahl = Integer.parseInt(testvec.get(i)
                                                                .get(3));
                        }
                        if (idxVorg <= testvec.size()) {
                            if (neudummy) {
                                startdatum_neu = rezdatum; // McM: Ausstellungsdatum ist entscheidend für 12 Wochen
                                                           // Frist, nicht erster Behandlungstermin
                                neudummy = false;
                            } else {
                                startdatum_neu = DatFunk.sDatInDeutsch(testvec.get(i)
                                                                              .get(0)); // McM: dito
                            }
                            while ((idxVorg < testvec.size()) && (testvec.get(idxVorg)
                                                                         .get(5)
                                                                         .length() == 0)) { // falls keine Termine
                                                                                            // eingetragen sind -> zum
                                                                                            // Vorgänger wechseln
                                gesamt = gesamt + Integer.parseInt(testvec.get(idxVorg)
                                                                          .get(3)); // die verordneten Behandlungen
                                                                                    // zaehlen trotzdem mit
                                // test ob gesamt > maxProFall
                                vorgaengerVoArt = testvec.get(idxVorg)
                                               .get(2);
                                if (chkIsErstVO(vorgaengerVoArt)) { // wenn die Uebersprungene die Erst-VO ist -> Abbruch
                                                          // (Verordnungsmenge ok)
                                    return true;
                                }
                                if (chkIsAdR(vorgaengerVoArt)) { // wenn die Uebersprungene eine AdR-VO ist -> Abbruch
                                    return shouldBeAdR(currRezVoArt);
                                }
                                idxVorg++; // jetzt Index auf Vorgänger setzen
                            }
                            if ((idxVorg >= testvec.size()) || (testvec.get(idxVorg)
                                                                       .get(5)
                                                                       .length() == 0)) {
                                // kein Vorgänger oder der enthält (auch) keine Behandlungen
                                
                                enddatum_alt = RezTools.holeLetztenTermin(null, testvec.get(i)
                                                                                       .get(5)); // dummy (damit die
                                                                                                 // Berechnung
                                                                                                 // durchlaeuft)
                            } else { // Vorgänger ist vorhanden und es sind Termine eingetragen
                                enddatum_alt = RezTools.holeLetztenTermin(null, testvec.get(idxVorg)
                                                                                       .get(5));
                            }
                            // System.out.println("Startdatum: "+startdatum_neu);
                            // System.out.println("Enddatum: "+enddatum_alt);

                            // Änderung Ablauflogik:
                            // Prüfung auf Überschreiten der Verordnungsmenge und ob es sich bei 'currRez'
                            // um eine Erst- oder AdR-VO handelt
                            // erfolgt _bevor_ die Therapiepause zwischen 'currRez' und seinem Vorgänger
                            // geprüft wird.
                            gesamt = gesamt + aktanzahl;
                            // System.out.println("Gesamt: "+gesamt);
                            if (gesamt > maxprofall) {
                                fehlertext = fehlertext + (fehlertext.length() <= 0 ? "<html>" : "")
                                        + "<br><b><font color='#ff0000'>Höchstverordnungsmenge ist überschritten -> "
                                        + Integer.toString(gesamt) + " Behandlungen"
                                        + "</font><br>Wechsel auf <font color='#ff0000'>außerhalb des Regelfalles</font> ist erforderlich<br>"
                                        + "Höchstverordnungsmenge im Regelfall ist<br>bei <font color='#ff0000'>"
                                        + aktindischl + " = " + Integer.toString(maxprofall)
                                        + "</font> Behandlungen<br>" + "</b><br><br>";
                                return false;
                            }

                            vorgaengerVoArt = testvec.get(i)
                                           .get(2); // VO-Art zu currRez
                            if (chkIsErstVO(vorgaengerVoArt)) {
                                return true; // Erst-VO gefunden -> Abbruch
                            }
                            if (chkIsAdR(vorgaengerVoArt)) {
                                return shouldBeAdR(currRezVoArt); // AdR-VO unter Vorgänger-VOs gefunden -> VO 'reznummer'
                                                              // muesste auch AdR-VO sein
                            }

                            tagedifferenz = DatFunk.TageDifferenz(enddatum_alt, startdatum_neu);
                            // System.out.println(startdatum_neu+" - "+ enddatum_alt+" -
                            // "+Long.toString(tagedifferenz));
                            if (tagedifferenz > therapiepause) { // Therapiepause ueberschritten
                                if (currRez.equals(reznummer)) {
                                    return shouldBeErstVO(vorgaengerVoArt, "Therapiepause beträgt<b><font color='#ff0000'> "
                                            + tagedifferenz + " </font></b>Tage.<br>");
                                } else {
                                    String htmlTxt = "<html>"
                                            + "<br>Keine <b><font color='#ff0000'> Erstverordnung </font></b>gefunden!<br>"
                                            + "<br><b>Therapiepause <font color='#ff0000'>vor " + currRez
                                            + "</font> beträgt <font color='#ff0000'>" + tagedifferenz
                                            + "</font> Tage.</b><br>" + "<br>Bitte Rezeptfolge manuell prüfen!<br>"
                                            + "<br><br>";

                                    fehlertext = fehlertext + htmlTxt;
                                    return false;
                                    // return shouldBeErstVO(voArt,"Therapiepause vor "+currRez+" beträgt<b><font
                                    // color='#ff0000'> "+tagedifferenz+" </font></b>Tage.<br>");
                                }
                            }

                            i = --idxVorg; // Index anpassen, falls Neurezept oder Rezepte uebersprungen wurden
                        }

                    } else {
                        // System.out.println("Nicht mitgezählt: "+testvec.get(i).get(1)+" - Anzahl:
                        // "+Integer.parseInt(testvec.get(i).get(3)));
                        // gesamt = gesamt + Integer.parseInt(testvec.get(i).get(3));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Fehler  in der Prüfung auf Folgerezepte\n\nBitte Informieren Sie den Systemadministrator");
        }
        // true damit die restliche Prüfung durchlaufen wird.
        return true;
    }

    private String errTxtNoEVO(String htmlTxt, String reason) {
        htmlTxt = htmlTxt + (htmlTxt.length() <= 0 ? "<html>" : "") + (reason.length() > 0 ? "<br>" : "") + reason
                + "<br>Verordnung müsste<b><font color='#ff0000'> Erstverordnung </font></b>sein.<br><br>" + "<br><br>";
        return htmlTxt;
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
            fehlertext = errTxtNoEVO(fehlertext, why);
            return false;
        }
    }

    private boolean shouldBeErstVO(String rezeptArt, String why) {
        return shouldBeErstVO(Integer.parseInt(rezeptArt), why);
    }

    private String errTxtNoFoVO(String htmlTxt, String reason) {
        htmlTxt = htmlTxt + (htmlTxt.length() <= 0 ? "<html>" : "") + (reason.length() > 0 ? "<br>" : "") + reason
                + "<br>Verordnung müsste<b><font color='#ff0000'> Folgeverordnung </font></b>sein.<br><br>" + "<br><br>";
        return htmlTxt;
    }

    private boolean chkIsFolgeVO(int rezeptArt) {
        if (rezarten[rezeptArt] == "Folgeverordnung") {
            return true;
        } else {
            return false;
        }
    }

    private boolean chkIsFolgeVO(String rezeptArt) {
        return chkIsErstVO(Integer.parseInt(rezeptArt));
    }

    private boolean shouldBeFolgeVO(int rezeptArt, String why) {
        if (chkIsErstVO(rezeptArt)) {
            return true;
        } else {
            fehlertext = errTxtNoFoVO(fehlertext, why);
            return false;
        }
    }

    private boolean shouldBeFolgeVO(String rezeptArt, String why) {
        return shouldBeErstVO(Integer.parseInt(rezeptArt), why);
    }

    private String errTxtNoAdR(String htmlTxt) {
            htmlTxt = htmlTxt + (htmlTxt.length() <= 0 ? "<html>" : "")
                    + "<br>A.d.R.-Verordnung unter den Vorgänger_VOs gefunden.<br><br>"
                    + "Verordnung müsste ebenfalls<b><font color='#ff0000'> A.d.R.-Verordnung </font></b>sein.<br>"
                    + "<font color='#ff0000'> Bitte v. Hd. nachprüfen! </font><br><br>";
            return htmlTxt;
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

    private boolean shouldBeAdR(int rezeptArt) {
        if (chkIsAdR(rezeptArt)) {
            return true;
        } else {
            fehlertext = errTxtNoAdR(fehlertext);
            return false;
        }
    }

    private boolean shouldBeAdR(String rezeptArt) {
        return shouldBeAdR(Integer.parseInt(rezeptArt));
    }
}
