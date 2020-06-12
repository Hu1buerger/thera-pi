package patientenFenster.rezepte;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import CommonTools.DatFunk;
import CommonTools.DateTimeFormatters;
import CommonTools.JRtaComboBox;
import CommonTools.JRtaTextField;
import CommonTools.SqlInfo;
import abrechnung.Disziplinen;
import environment.LadeProg;
import environment.Path;
import hmrCheck.HMRCheck;
import mandant.Mandant;
import rezept.Rezept;
import systemEinstellungen.SystemPreislisten;

public final class RezeptFensterTools {
    
    private RezeptFensterTools() {
        // don't try this...
    }

    static LocalDate chkLastBeginDat(LocalDate rezDat, String lastDat, String preisGroup, String aktDiszi) {
        LocalDate spaetestAnfang;
        if (lastDat.trim().equals(".  .")) { // spaetester Beginn nicht angegeben? -> aus Preisgruppe holen
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

}
