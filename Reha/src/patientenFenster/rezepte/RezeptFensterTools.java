package patientenFenster.rezepte;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.DateTimeFormatters;
import CommonTools.SqlInfo;
import core.Disziplin;
import hmrCheck.HMRCheck;
import mandant.IK;
import rezept.Rezept;
import rezept.RezeptDto;
import systemEinstellungen.SystemPreislisten;

public final class RezeptFensterTools {
    private static Logger logger = LoggerFactory.getLogger(RezeptFensterTools.class);
    
    private RezeptFensterTools() {
        // don't try this...
    }

    static LocalDate chkLastBeginDat(LocalDate rezDat, String lastDat, String preisGroup, String aktDiszi) {
        LocalDate spaetestAnfang;
        if (lastDat == null || lastDat.trim().equals(".  .")) { // spaetester Beginn nicht angegeben? -> aus Preisgruppe holen
            // Preisgruppe holen
            int pg = Integer.parseInt(preisGroup) - 1;
            // Frist zwischen Rezeptdatum und erster Behandlung
            int frist = (Integer) ((Vector<?>) SystemPreislisten.hmFristen.get(aktDiszi)
                                                                          .get(0)).get(pg);
            // Kalendertage
            if ((Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(aktDiszi)
                                                                  .get(1)).get(pg)) {
                
                spaetestAnfang = rezDat.plusDays(frist);
            } else { // Werktage
                boolean mitsamstag = (Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(aktDiszi)
                                                                                       .get(4)).get(pg);
                spaetestAnfang = HMRCheck.hmrLetztesDatum(rezDat,
                        (Integer) ((Vector<?>) SystemPreislisten.hmFristen.get(aktDiszi)
                                                                          .get(0)).get(pg),
                        mitsamstag);
            }
        } else {
            spaetestAnfang =  LocalDate.parse(lastDat, DateTimeFormatters.ddMMYYYYmitPunkt);
        }
        return spaetestAnfang;
    }

    /**
    *
    * Test, ob eine Langfristverordnung vorliegt
    */
   public static String[] holeLFV(String hole_feld, String db, String where_feld, String suchen, String voart) {
       String cmd = "select " + hole_feld + " from " + db + " where " + where_feld + "='" + suchen + "' LIMIT 1";
       String anamnese = SqlInfo.holeEinzelFeld(cmd);
       String[] retstring = { "", "" };
       if (anamnese.indexOf("$$LFV$$" + voart.toUpperCase() + "$$") >= 0) {
           String[] zeilen = anamnese.split("\n");
           for (int i = 0; i < zeilen.length; i++) {
               if (zeilen[i].startsWith("$$LFV$$" + voart.toUpperCase() + "$$")) {
                   String[] woerter = zeilen[i].split(Pattern.quote("$$"));
                   try {
                       retstring[1] = "LangfristVerordnung: " + woerter[1] + "\n" + "Disziplin: " + woerter[2] + "\n"
                               + "Aktenzeichen: " + woerter[3] + "\n" + "Genehmigungsdatum: " + woerter[4] + "\n"
                               + "G\u00fcltig ab: " + woerter[5] + "\n" + "G\u00fcltig bis: " + woerter[6] + "\n";
                       retstring[0] = String.valueOf(zeilen[i]);
                   } catch (Exception ex) {
                       ex.printStackTrace();
                   }
                   return retstring;
               }
           }

       }
       return retstring;
   }

   public static String macheIcdString(String string) {
       String String1 = string.trim()
                              .substring(0, 1)
                              .toUpperCase();
       String String2 = string.trim()
                              .substring(1)
                              .toUpperCase()
                              .replace(" ", "")
                              .replace("*", "")
                              .replace("!", "")
                              .replace("+", "")
                              .replace("R", "")
                              .replace("L", "")
                              .replace("B", "")
                              .replace("G", "")
                              .replace("V", "")
                              .replace("Z", "");
       ;
       return String1 + String2;

   }

   static String chkIcdFormat(String string) {
       int posDot = string.indexOf(".");
       if ((string.length() > 3) && (posDot < 0)) {
           String tmp1 = string.substring(0, 3);
           String tmp2 = string.substring(3);
           return tmp1 + "." + tmp2;
       }
       return string;
   }

   /**
    * Will return a List of String-Arrays of this layout:
    * <BR/> - RezNR
    * <BR/> - Termin(e?)
    * <BR/> - origin (verordn vs. LZA)
    *  
    * @param Rezept for which double-termine are to be located
    * @return
    */
   public static final List<String[]> doDoublettenTest(Rezept rez, IK ik) {
       List<String[]> doublette = new ArrayList<String[]>();

       RezeptDto rDto = new RezeptDto(ik);
       
       try {
           List<Rezept> rezepteToTest;
           LocalDate lastrezdate = rez.getRezDatum().minusDays(90);
           logger.debug("Rez: lastrezdate=" + lastrezdate.toString());
           // TODO: change to new Rezeptnummern + diszi class
           String diszi = rez.getRezNr().substring(0, 2);
           logger.debug("Rez: diszi=" + diszi);
           for (boolean rezOrt : new boolean[] {true, false} ) {       // rezOrt == true == aktuelle Rezepte
               rezepteToTest = rDto.holeDatumUndTermineNachPatientExclRezNr(rez.getPatIntern(),
                                                                            rez.getRezNr().toString(),
                                                                            rezOrt,
                                                                            lastrezdate);
               logger.debug("Rez: tests=" + rezepteToTest.toString());
               if (rezepteToTest.isEmpty())
                   continue;
               // zuerst in den aktuellen Rezepten nachsehen
               // wir holen uns Rezeptnummer,Rezeptdatum und die Termine
               // Anzahl der Termine
               // dtermm.getValueAt(i-1,0);
               // 1. for next fuer jeden einzelnen Tag des Rezeptes, darin enthalten eine neue
               // for next fuer alle vorhandenen Rezepte
               // 2. nur dieselbe Disziplin ueberpuefen
               // 3. dann durch alle Rezepte hangeln und testen ob irgend ein Tag in den
               // Terminen enthalten ist

               for (Rezept rezept : rezepteToTest) {
                   // RezNr:
                   if (diszi.equals(rezept.getRezClass())) {
                       logger.debug("Anzahl Termine in original rezept: " + rez.AnzahlTermineInRezept());
                       for (String termin : rez.getTermine().split("\n")) {
                           // Termine:
                           if (rezept.getTermine()
                                    .contains(termin)) {
                               doublette.add(new String[] { rezept.getRezNr(),
                                                            termin.split("@")[0],
                                                            rezOrt ? "Aktuelle" : "Historische"});
                           }
                       }
                   }
               }
           }
       } catch (Exception ex) {
           ex.printStackTrace();
           JOptionPane.showMessageDialog(null, "Fehler im Doublettentest\n" + ex.getMessage());
           logger.error("Error in RezepteTools-DoublettenTest: " + ex.getLocalizedMessage());
       }

       /*****************/
       return doublette;
   }
   
   /**
    * Adds/changes a comment of a Termin in the Termine-String of a Rezept.
    * <BR/>Alters the passed in Rezept and also updates the database with new Termine
    * <BR/>
    * <BR/>TODO: think about transferring this to Rezept/-Dto...
    *  
    * @param rez           Rezept to be altered
    * @param welcherTermin Which of the termine should be altered. 0=first Termin
    * @param kommentar     The kommentar (Unterbrech-Begr.?) to be set
    **/
   public static void updateKommentarInTermin(Rezept rez, int welcherTermin, String kommentar, IK ik) {
       RezeptDto rDto = new RezeptDto(ik);
       
       String[] termine = rez.getTermine().split("\n");
       String[] eintrag = termine[welcherTermin].split("@");
       eintrag[2] = kommentar;
       String neuerEintrag = eintrag[0];
       for (int i=1; i<eintrag.length; i++)
           neuerEintrag.concat("@" + eintrag[i]);
       termine[welcherTermin] = neuerEintrag;
       String neueTermine = termine[0];
       for (int i=1; i<termine.length; i++)
           neueTermine.concat("\n" + termine[i]);
       rDto.updateRezeptTermine(rez.getId(), neueTermine);
       rez.setTermine(neueTermine);
   }

   /**
    * Calculates the default 'latestStartDate' for a Rezept
    */
   public static LocalDate calcLatestStartDate(Rezept rez) {
       Disziplin diszi = Disziplin.ofShort(rez.getRezClass());
       int pg = rez.getPreisGruppe();
       LocalDate startDatum = rez.getRezDatum();
       // This take Werktage vs Kalendertage into account:
       return chkLastBeginDat(startDatum,
                              rez.getLastDate() == null? null : rez.getLastDate().format(DateTimeFormatters.ddMMYYYYmitPunkt),
                              Integer.toString(pg),
                              diszi.medium);
       // Q&D, just add Frist to startDate, don't care about Werktage...
       // return calcLatestStartDate(diszi, pg - 1, startDatum);
   }
   
   // Simple, Q&D...
   private static final LocalDate calcLatestStartDate(Disziplin diszi, int preisgruppe, LocalDate rezDate) {
       String disziplin = diszi.medium;
       
       // Frist zwischen RezDat (bzw. spaetester BehBeginn) und tatsaechlichem BehBeginn
       int fristBisBeginn = (Integer) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                           .get(0)).get(preisgruppe);
       return rezDate.plusDays(fristBisBeginn);
   }
}
