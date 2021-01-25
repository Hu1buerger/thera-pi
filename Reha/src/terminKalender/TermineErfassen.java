package terminKalender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import CommonTools.DatFunk;
import CommonTools.IntegerTools;
import CommonTools.SqlInfo;
import hauptFenster.AktiveFenster;
import hauptFenster.Reha;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;
import terminKalender.TerminFenster.Ansicht;

public class TermineErfassen implements Runnable {
    private String scanrez = null;
    private Vector alleterm;
    private Vector<String> vec = null;

    private String copyright = null;
    private String heute = null;
    private int erstfund = -1;
    private String kollege = "";
    private boolean firstfound;

    private static boolean success = true;
    private boolean unter18 = false;
    private boolean vorjahrfrei = false;
    private static int errorint = 0;
    private StringBuffer sbuftermine = new StringBuffer();
    private static TermineErfassen thisClass = null;

    public TermineErfassen(String reznr, Vector termvec) {
        scanrez = reznr.trim();
        firstfound = false;
        erstfund = -1;
        thisClass = this;

    }

    @Override
    public void run() {
        heute = DatFunk.sHeute();
        copyright = "\u00AE";
        int ret = -1;
        try {
            // Sonderfall Rehasport und Funktionstraining // ohne Nutzung des TK
            if ((scanrez.startsWith("RS") || scanrez.startsWith("FT")) && SystemConfig.RsFtOhneKalender) {
                scanrez = scanrez.replace("_", "");
                if ((ret = testeVerordnung()) == 0) {
                    String terminNeu = vec.get(0) + macheNeuTermin2(vec.get(1), vec.get(2), vec.get(3), vec.get(4),
                            Reha.aktUser, DatFunk.sHeute());
                    SqlInfo.sqlAusfuehren("update verordn set termine = '" + terminNeu + "' where rez_nr = '" + scanrez
                            + "' LIMIT 1");
                    JComponent patient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
                    if (patient != null) {
                        // System.out.println("in aktualisierung");
                        // System.out.println("angezeigt wird aktuell
                        // "+Reha.instance.patpanel.aktRezept.rezAngezeigt);
                        if (Reha.instance.patpanel.aktRezept.rezAngezeigt.equalsIgnoreCase(scanrez.trim())) {
                            try {
                                // System.out.println("Ansicht ist gleich aktuellem Rezept");
                                Reha.instance.patpanel.aktRezept.updateEinzelTermine(terminNeu);
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null, "Fehler bei der Aktualisierung der Rezeptansicht");
                                ex.printStackTrace();
                            }
                        }
                    }
                } else {
                    // evtl. JOptionPane zeigen
                }
                return;
            }
            // Zunächst testen ob der Tag bereits erfaßt war
            if ((ret = testeVerordnung()) == 0) {

                // termok liefert false wenn der Termin bereits mit dem "copyright"-Zeichen im
                // Terminkalender steht.
                boolean termok = testeTermine();
                if (!termok) {
                    //// System.out.println("Rezept steht an diesem Tag nicht im Kalender");
                    // JOptionPane.showMessageDialog(null, "Dieses Rezept ist nicht am heutigen Tag
                    //// im Kalender\n\n"+
                    // "Gescannte Rezeptnummer -> "+scanrez);
                    // return;
                }
                // System.out.println("Erstfund = "+erstfund);
                if (erstfund >= 0) {
                    scheibeTermin();
                    JComponent patient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
                    if (patient != null) {
                        // System.out.println("in aktualisierung");
                        // System.out.println("angezeigt wird aktuell
                        // "+Reha.instance.patpanel.aktRezept.rezAngezeigt);
                        if (Reha.instance.patpanel.aktRezept.rezAngezeigt.equalsIgnoreCase(scanrez.trim())) {
                            try {
                                // System.out.println("Ansicht ist gleich aktuellem Rezept");
                                Reha.instance.patpanel.aktRezept.updateEinzelTermine(sbuftermine.toString());
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null, "Fehler bei der Aktualisierung der Rezeptansicht");
                                ex.printStackTrace();
                            }
                        }
                    }

                } else {
                    String htmlmeldung = "<html><b><font size='5'>Dieses Rezept ist am heutigen Tag nicht im Kalender eingetragen<br><br>"
                            + "Gescannte Rezeptnummer -><font color='#ff0000'> " + scanrez
                            + "</font></font></b></html>";
                    JOptionPane.showMessageDialog(null, htmlmeldung);
                    // System.out.println("Rezept steht an diesem Tag nicht im Kalender");
                    setTerminSuccess(false);
                }
                if (firstfound && scanrez.startsWith("RH")) {
                    fahreFortMitTerminen();
                }
                // Tag
            } else {
                if (ret > 0) {
                    setTerminSuccess(false);
                    // System.out.println("hier die Fehlerbehandlung einbauen---->Fehler = "+ret);
                    switch (ret) {
                    case 1:
                        // System.out.println("Das Rezept nicht in Historie und nicht in Rez-Stamm");
                        String htmlmeldung = "<html><b><font size='5'>Das gescannte Rezept -><font size='6' color='#ff0000'> "
                                + scanrez + "<br></font>"
                                + "existiert weder im<font color='#ff0000'> aktuellen Rezeptstamm</font><br>noch in der<font color='#ff0000'> Historie</font>"
                                + "<br><br>Bitte melden Sie dieses Rezept dem Administrator</font></b></html>";
                        JOptionPane.showMessageDialog(null, htmlmeldung);
                        break;
                    case 2:
                        JOptionPane.showMessageDialog(null,
                                "<html><b><font size='5'>Dieses Rezept wurde bereits abgerechnet!</font><br><br>"
                                        + "Das gescannte Rezept -><font size='6' color='#ff0000'> " + scanrez
                                        + "<br></font></html>");
                        break;
                    case 3:
                        JOptionPane.showMessageDialog(null,
                                "<html><b><font size='5'>Dieses Rezept wurde am heutigen Tab bereits erfaßt!<br><br>"
                                        + "Das gescannte Rezept -><font size='6' color='#ff0000'> " + scanrez
                                        + "<br></font></html>");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            setTerminSuccess(false);

        }
        // System.out.println("Terminerfassen beendet");
        alleterm = null;
        return;
    }

    private void setTerminSuccess(boolean xsuccess) {
        TermineErfassen.success = xsuccess;
    }

    /********************/
    public int testeVerordnung() throws Exception {
        vec = SqlInfo.holeSatz("verordn",
                "termine,pos1,pos2,pos3,pos4,hausbes,unter18,jahrfrei,pat_intern,preisgruppe,zzregel,anzahl1,anzahl2,anzahl3,anzahl4,preisgruppe",
                " rez_nr='" + scanrez + "'", Arrays.asList(new String[] {}));
        if (vec.size() == 0) {
            vec = SqlInfo.holeSatz("lza", "termine", " rez_nr='" + scanrez + "'", Arrays.asList(new String[] {}));
            if (vec.size() == 0) {
                //// System.out.println("Rezept ist weder im aktuellen Rezeptstamm noch in der
                //// Historie");
                return 1;
            } else {
                //// System.out.println("Das Rezept wurde bereits abgerechnet");
                return 2;
            }

        }
        String termine = vec.get(0);
        // Tag ist bereits erfaßt !
        if (termine.contains(DatFunk.sHeute())) {
            return 3;
        }
        unter18 = (vec.get(6)
                      .equals("T") ? true : false);
        vorjahrfrei = (vec.get(7)
                          .equals("") ? false : true);
        // 0 = Tage ist noch nicht erfaßt
        return 0;
    }

    /********************/
    private boolean testeTermine() throws Exception {
        long zeit1 = System.currentTimeMillis();
        boolean ret;
        alleterm = SqlInfo.holeFelder("select * from flexkc where datum='" + DatFunk.sDatInSQL(DatFunk.sHeute())
                + "' LIMIT " + Integer.toString(KollegenListe.maxKalZeile));
        /*******************************************/
        Object[] obj = untersucheTermine();
        /*******************************************/
        String string = null;
        // System.out.println("Rückgabewert der Untersuchung = "+obj[0]);
        if (!(Boolean) obj[0]) {
            ret = false;
        } else {
            // System.out.println(obj[4]);
            // System.out.println(copyright.trim());
            // System.out.println("if(!(String)obj[4]).contains(copyright.trim()) "+
            // obj[4]+" contains("+copyright.trim()+")");
            if (!((String) obj[4]).contains(copyright.trim())) {
                this.kollege = String.valueOf(obj[1]);
                string = "Rezeptnummer wurde gefunden bei Kollege " + (String) obj[1] + " an Block " + obj[2]
                        + " Rezeptnummer:" + (String) obj[3];
                String stmt = " sperre = '" + (String) obj[1] + heute + "'";
                //// System.out.println(stmt);
                int gesperrt = SqlInfo.zaehleSaetze("flexlock", stmt);
                // if( gesperrt == 0 ){
                String sblock = Integer.toString((((Integer) obj[2] / 5) + 1));
                SqlInfo.aktualisiereSatz("flexkc", "T" + sblock + " = '" + copyright + (String) obj[4] + "'",
                        "datum='" + (String) obj[7] + "' AND " + "behandler='" + (String) obj[1] + "' AND TS" + sblock
                                + "='" + (String) obj[5] + "' AND T" + sblock + "='" + (String) obj[4] + "' AND N"
                                + sblock + " LIKE '%" + scanrez + "%'");

                try {
                    String snum = ((String) obj[1]).substring(0, 2);
                    int inum;
                    if (snum.substring(0, 1)
                            .equals("0)")) {
                        inum = Integer.valueOf(snum.substring(1, 2)) - 1;
                    } else {
                        inum = Integer.valueOf(snum.substring(0, 2)) - 1;
                    }

                    JComponent termin = AktiveFenster.getFensterAlle("TerminFenster");
                    if (termin != null) {

                        if (Reha.instance.terminpanel.aktAnsicht == Ansicht.NORMAL) {
                            if (Reha.instance.terminpanel.getAktuellerTag()
                                                         .equals(DatFunk.sHeute())) {
                                int iblock = Integer.valueOf(sblock) - 1;
                                ((ArrayList<Vector<String>>) Reha.instance.terminpanel.getDatenVector()
                                                                                      .get(inum)).get(0)
                                                                                                 .set(iblock, copyright
                                                                                                         + (String) obj[4]);
                                Reha.instance.terminpanel.viewPanel.repaint();
                            } else {
                                // System.out.println("Aktueller Tag =
                                // "+Reha.instance.terminpanel.getAktuellerTag());
                            }
                        } else {
                            // System.out.println("Ansicht im TK = "+ansicht);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                ret = true;
            } else {

                this.kollege = (String) obj[1];
                // System.out.println("this.kollege = "+(String)obj[1]);
                ret = false;
            }
        }
        return ret;
    }

    /********************/
    private int getKollegenInt(String kollege) {
        return IntegerTools.trimLeadingNullAndRetInt(kollege.substring(0, 2));
    }

    private Object[] untersucheTermine() throws Exception {

        int spalten = alleterm.size();
        //// System.out.println("eingelesene Spalten = "+spalten);
        int i, y;
        boolean gefunden = false;

        Object[] obj = { Boolean.valueOf(false), null, null, null, null, null, null, null };

        try {

            boolean isReha = scanrez.startsWith("RH");
            boolean isKG = false;

            for (i = 0; i < spalten; i++) {
                // System.out.println("Untersuche Terminspalte "+Integer.toString(i+1));
                int bloecke = ((Vector) alleterm.get(0)).size();
                int belegt = Integer.parseInt((String) ((Vector) alleterm.get(i)).get(bloecke - 6));
                try {
                    isKG = KollegenListe.getAbteilung(
                            getKollegenInt((String) ((Vector) alleterm.get(i)).get(bloecke - 4)))
                                         .equals("KG");
                } catch (Exception ex) {
                    isKG = false;
                }
                // System.out.println(getKollegenInt((String)
                // ((Vector)alleterm.get(i)).get(bloecke-4))+" - Abteilung:
                // "+ParameterLaden.getAbteilung(getKollegenInt((String)
                // ((Vector)alleterm.get(i)).get(bloecke-4))) );
                for (y = 0; y < belegt; y++) {
                    // System.out.println("Untersuche Block "+Integer.toString(y+1)+" von
                    // "+Integer.toString(belegt));
                    // int block = ((y*5)+1);
                    if (((String) ((Vector) alleterm.get(i)).get(((y * 5) + 1))).contains(scanrez)) {
                        obj[0] = Boolean.valueOf(true); // gefunden
                        obj[1] = ((Vector) alleterm.get(i)).get(bloecke - 4);// Kollege
                        obj[2] = ((y * 5) + 1);// Blocknummer
                        obj[3] = ((Vector) alleterm.get(i)).get(((y * 5) + 1)); // Rezeptnummer
                        obj[4] = ((Vector) alleterm.get(i)).get(((y * 5))); // Name
                        obj[5] = ((Vector) alleterm.get(i)).get(((y * 5)) + 2); // Beginn
                        obj[6] = ((Vector) alleterm.get(i)).get(((y * 5))); // Name
                        obj[7] = ((Vector) alleterm.get(i)).get(bloecke - 2);// Datum
                        // System.out.println("Gefunden in Spalte "+Integer.toString(i+1)+
                        // " in Block "+Integer.toString(y+1)+" Ergebnis = "+obj[3]);

                        // Hier muß ermittelt werden ob der Spalteninhaber zur Abteilung KG gehört

                        if (!isReha) {
                            gefunden = true;
                            erstfund = i;
                            break;
                        } else if (isReha && isKG) {
                            gefunden = true;
                            erstfund = i;
                            break;
                        } else if (isReha && !isKG) {
                            gefunden = true;
                            erstfund = i;
                        }
                    }
                }
                if (!isReha && gefunden) {
                    firstfound = true;
                    break;
                } else if (isReha && isKG && gefunden) {
                    firstfound = true;
                    break;
                } else if (isReha && !isKG && gefunden) {
                    firstfound = true;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return obj;
    }

    /********************/
    private void fahreFortMitTerminen() throws Exception {
        int spalten = alleterm.size();
        int mehrstellen = 0;
        boolean termOk = false;
        //// System.out.println("eingelesene Spalten = "+spalten);
        int i, y;
        Object[] obj = { Boolean.valueOf(false), null, null, null, null, null, null, null };
        for (i = (erstfund + 1); i < spalten; i++) {
            int bloecke = ((Vector) alleterm.get(0)).size();
            int belegt = Integer.parseInt((String) ((Vector) alleterm.get(i)).get(bloecke - 6));
            for (y = 0; y < belegt; y++) {
                // int block = ((y*5)+1);
                if (((String) ((Vector) alleterm.get(i)).get(((y * 5) + 1))).contains(scanrez)) {
                    obj[0] = Boolean.valueOf(true); // gefunden
                    obj[1] = ((Vector) alleterm.get(i)).get(bloecke - 4);// Kollege
                    obj[2] = ((y * 5) + 1);// Blocknummer
                    obj[3] = ((Vector) alleterm.get(i)).get(((y * 5) + 1)); // Rezeptnummer
                    obj[4] = ((Vector) alleterm.get(i)).get(((y * 5))); // Name
                    obj[5] = ((Vector) alleterm.get(i)).get(((y * 5)) + 2); // Beginn
                    obj[6] = ((Vector) alleterm.get(i)).get(((y * 5))); // Name
                    obj[7] = ((Vector) alleterm.get(i)).get(bloecke - 2);// Datum

                    if (!((String) obj[4]).contains(copyright.trim())) {
                        mehrstellen++;
                        String stmt = " sperre = '" + (String) obj[1] + heute + "'";
                        int gesperrt = SqlInfo.zaehleSaetze("flexlock", stmt);
                        String sblock = "";
                        // if( gesperrt == 0 ){
                        sblock = Integer.toString((((Integer) obj[2] / 5) + 1));
                        SqlInfo.aktualisiereSatz("flexkc", "T" + sblock + " = '" + copyright + (String) obj[4] + "'",
                                "datum='" + (String) obj[7] + "' AND " + "behandler='" + (String) obj[1] + "' AND TS"
                                        + sblock + "='" + (String) obj[5] + "' AND T" + sblock + "='" + (String) obj[4]
                                        + "' AND N" + sblock + " LIKE '%" + scanrez + "%'");

                        try {
                            String snum = ((String) obj[1]).substring(0, 2);
                            int inum;
                            if (snum.substring(0, 1)
                                    .equals("0)")) {
                                inum = Integer.parseInt(snum.substring(1, 2)) - 1;
                            } else {
                                inum = Integer.parseInt(snum.substring(0, 2)) - 1;
                            }

                            JComponent termin = AktiveFenster.getFensterAlle("TerminFenster");
                            if (termin != null) {
                                if (Reha.instance.terminpanel.aktAnsicht == Ansicht.NORMAL) {
                                    if (Reha.instance.terminpanel.getAktuellerTag()
                                                                 .equals(DatFunk.sHeute())) {
                                        if (!termOk) {
                                            termOk = true;
                                        }
                                        int iblock = Integer.valueOf(sblock) - 1;
                                        ((ArrayList<Vector<String>>) Reha.instance.terminpanel.getDatenVector()
                                                                                              .get(inum)).get(0)
                                                                                                         .set(iblock,
                                                                                                                 copyright
                                                                                                                         + (String) obj[4]);
                                    } else {
                                        // System.out.println("Aktueller Tag =
                                        // "+Reha.instance.terminpanel.getAktuellerTag());
                                    }
                                } else {
                                    // System.out.println("Ansicht im TK = "+ansicht);
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        if (termOk) {
            Reha.instance.terminpanel.viewPanel.repaint();
        }
        //// System.out.println("Anzahl zusätzlicher Fundstellen = "+mehrstellen);
    }

    /********************/
    private void scheibeTermin() throws Exception {
        int ikoll = (kollege.substring(0, 1)
                            .equals("0") ? Integer.valueOf(kollege.substring(1, 2))
                                    : Integer.valueOf(kollege.substring(0, 2)));
        try {
            //// System.out.println("Kollegen-Nummer = "+ikoll);
            this.kollege = KollegenListe.getMatchCodeUeberDBZeile(ikoll);
            sbuftermine.setLength(0);
            sbuftermine.toString();
            if (!vec.get(0)
                    .trim()
                    .equals("")) {
                sbuftermine.append(vec.get(0));
            }

            Object[] objTerm = RezTools.BehandlungenAnalysieren(scanrez, false, false, false,
                    ((Vector<String>) vec.clone()), null, this.kollege, DatFunk.sHeute());
            if (objTerm == null) {
                return;
            }
            if ((Integer) objTerm[1] == RezTools.REZEPT_ABBRUCH) {
                return;
            } else if ((Integer) objTerm[1] == RezTools.REZEPT_IST_BEREITS_VOLL) {
                String anzahl = "??????"; // TodDo
                String message = "<html><b><font size='5'>Auf dieses Rezept wurden bereits<font size='6' color='#ff0000'> "
                        + anzahl + " </font>Behandlungen durchgeführt!"
                        + "<br>Verordnete Menge ist<font size='6' color='#ff0000'> " + vec.get(11)
                        + "</font><br>Das Rezept ist somit bereits voll und darf für aktuelle Behandlung nicht mehr<br>"
                        + "verwendet werden!!!!<br><br>" + "Gescannte Rezeptnummer =<font size='6' color='#ff0000'> "
                        + scanrez + "</font><br><br></html>";
                JOptionPane.showMessageDialog(null, message);
                return;
            } else {
                sbuftermine.append((String) objTerm[0]);
                if ((Integer) objTerm[1] == RezTools.REZEPT_IST_JETZ_VOLL) {
                    String message = "<html><b><font size='5'>Das Rezept ist jetzt voll"
                            + "<br>Rezeptnummer = <font size='6' color='#ff0000'> " + scanrez + "</font><br>"
                            + "<br>Bitte das Rezept zur Abrechnung vorbereiten.</font></b></html>";
                    JOptionPane.showMessageDialog(null, message);
                    try {
                        RezTools.fuelleVolleTabelle(scanrez, this.kollege);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Fehler beim Aufruf von 'fuelleVolleTabelle'");
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        /*******************************/
        // 0=termine,
        // 1=pos1
        // 2=pos2
        // 3=pos3
        // 4=pos4
        // 5=hausbes
        // 6=unter18
        // 7=jahrfrei
        // 8=pat_intern
        // 9=preisgruppe
        // 10=zzregel
        // 11=anzahl1
        // 12=anzahl2
        // 13=anzahl3
        // 14=anzahl4
        // 15=preisgruppe
        // System.out.println("Unter 18 = "+unter18+" Vorjahrfei = "+vorjahrfrei);
        if (!unter18 && !vorjahrfrei) {
            // System.out.println("In Variante 1");
            SqlInfo.aktualisiereSatz("verordn", "termine='" + sbuftermine.toString() + "'", "rez_nr='" + scanrez + "'");
            // Teste ob Rezeptgebühr bezahlt!
            if (SystemConfig.RezGebWarnung) {
                RezTools.RezGebSignal(scanrez);
            }
        } else if ((unter18) && (!vorjahrfrei)) {
            /// Testen ob immer noch unter 18 ansonsten ZuZahlungsstatus �ndern;
            // System.out.println("Pat_intern = "+vec.get(9));
            String geboren = DatFunk.sDatInDeutsch(SqlInfo.holePatFeld("geboren", "pat_intern='" + vec.get(8) + "'"));
            // System.out.println("Geboren = "+geboren);
            boolean u18 = DatFunk.Unter18(DatFunk.sHeute(), geboren);
            // System.out.println(u18);
            if (u18) {
                // System.out.println("In Variante 2");
                SqlInfo.aktualisiereSatz("verordn", "termine='" + sbuftermine.toString() + "'",
                        "rez_nr='" + scanrez + "'");
            } else {
                // System.out.println("In Variante 3");
                SqlInfo.aktualisiereSatz("verordn", "termine='" + sbuftermine.toString() + "', zzstatus='2'",
                        "rez_nr='" + scanrez + "'");
                // Teste ob Rezeptgebühr bezahlt!
                if (SystemConfig.RezGebWarnung) {
                    RezTools.RezGebSignal(scanrez);
                }
            }

        } else if (!unter18 && vorjahrfrei) {
            String befreit = SqlInfo.holePatFeld("befreit", "pat_intern='" + vec.get(8) + "'");
            String bezahlt = SqlInfo.holeRezFeld("rez_bez", "rez_nr='" + scanrez + "'");
            if (!befreit.equals("T") && bezahlt.equals("F")) {
                if ((DatFunk.DatumsWert("31.12." + vec.get(7)) < DatFunk.DatumsWert(DatFunk.sHeute()))) {
                    // System.out.println("In Variante 4");
                    SqlInfo.aktualisiereSatz("verordn", "termine='" + sbuftermine.toString() + "', zzstatus='2'",
                            "rez_nr='" + scanrez + "'");
                    // Teste ob Rezeptgebühr bezahlt!
                    if (SystemConfig.RezGebWarnung) {
                        RezTools.RezGebSignal(scanrez);
                    }
                } else {
                    // System.out.println("In Variante 5");
                    SqlInfo.aktualisiereSatz("verordn", "termine='" + sbuftermine.toString() + "'",
                            "rez_nr='" + scanrez + "'");
                }
            } else {
                SqlInfo.aktualisiereSatz("verordn", "termine='" + sbuftermine.toString() + "'",
                        "rez_nr='" + scanrez + "'");

            }
        } else {
            // System.out.println("In Variante 6");
            SqlInfo.aktualisiereSatz("verordn", "termine='" + sbuftermine.toString() + "'", "rez_nr='" + scanrez + "'");
            // Teste ob Rezeptgebühr bezahlt!
            if (SystemConfig.RezGebWarnung) {
                RezTools.RezGebSignal(scanrez);
            }
        }
    }

    /********************/
    private String macheNeuTermin(String text) {
        String ret = DatFunk.sHeute() + "@" + this.kollege + "@" + text + "@" + vec.get(1) + (vec.get(2)
                                                                                                 .trim()
                                                                                                 .equals("")
                                                                                                         ? ""
                                                                                                         : "," + (vec.get(
                                                                                                                 2)))
                + (vec.get(3)
                      .trim()
                      .equals("") ? "" : "," + (vec.get(3)))
                + (vec.get(4)
                      .trim()
                      .equals("") ? "" : "," + (vec.get(4)))
                + "@" + DatFunk.sDatInSQL(DatFunk.sHeute()) + "\n";
        return ret;

    }

    public static String macheNeuTermin2(String pos1, String pos2, String pos3, String pos4, String xkollege,
            String datum) {
        String ret = datum + "@" + (xkollege == null ? "" : xkollege) + "@" + "" + "@" +
                machePositionsString(Arrays.asList(pos1, pos2, pos3, pos4)) + "@" + DatFunk.sDatInSQL(datum) + "\n";
        return ret;
    }

    private static String machePositionsString(List<String> list) {
        String ret = "";
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i)
                     .equals("")) {
                if (i == 0) {
                    ret = ret + list.get(i);
                } else {
                    if (ret.length() > 0) {
                        // erstes element war nicht leer
                        ret = ret + "," + list.get(i);
                    } else {
                        ret = ret + list.get(i);
                    }
                }
            }
        }
        return String.valueOf(ret);
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
}
/************************************/
