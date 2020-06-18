package stammDatenTools;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.DatFunk;
import CommonTools.SqlInfo;
import hauptFenster.Reha;
import mandant.IK;
import rezept.Rezept;
import rezept.RezeptDto;

public class RezepteTools {
    private static Logger logger = LoggerFactory.getLogger(RezepteTools.class);
    
    private static IK ik;
    
    public RezepteTools(IK Ik) {
        ik=Ik;
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
    public final List<String[]> doDoublettenTest(Rezept rez) {
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

}
