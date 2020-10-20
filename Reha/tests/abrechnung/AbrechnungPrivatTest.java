package abrechnung;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXTable;
import org.junit.Test;
import org.therapi.reha.patient.AktuelleRezepte;

import CommonTools.SqlInfo;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import office.OOService;
import sql.DatenquellenFactory;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;

public class AbrechnungPrivatTest {

    private static final Data HM0000 = new Data(10, "1769", "ER1555", "349", "T");
    private static final String libPath = "C:/RehaVerwaltung/Libraries/lib/openofficeorg";
    private static final String ooPath = "C:/Program Files (x86)/OpenOffice 4";

    @Test
    public void testName() throws Exception {

    }


    public static void main(String[] args) throws SQLException, FileNotFoundException, OfficeApplicationException {
        String aktik = "123456789";
        JXFrame frame = new JXFrame();

        new SqlInfo().setConnection(new DatenquellenFactory(aktik).createConnection());

        OOService.setLibpath(libPath, ooPath);

        AktuelleRezepte.tabelleaktrez = new JXTable();
        Data data1 = new Data(2, "1728", "ER1516", "260", "T");
    //   data = HM0000;
        Data data = new Data(10, "1704", "ER1411", "30", "T");
        Vector<String> rezeptVector = (SqlInfo.holeSatz("verordn", " * ", "id = '" + data.rezeptDBId + "'",
                Arrays.asList(new String[] {})));

        Vector<String> patientenDatenVector = SqlInfo.holeSatz("pat5", " * ", "id ='" + data.patDBId + "'",
                Arrays.asList(new String[] {}));

        String disziplinFromRezNr = RezTools.getDisziplinFromRezNr(data.rezeptNummer);
        int rueckgabeIN = 0;

        SystemPreislisten.ladepreise(disziplinFromRezNr, aktik);
        Vector<Vector<String>> preisliste = SystemPreislisten.hmPreise.get(disziplinFromRezNr)
                                                                      .get(data.preisgruppe - 1);

        SystemConfig.AbrechnungParameter();
        HashMap<String, String> hmAbrechnung = SystemConfig.hmAbrechnung;
        hmAbrechnung.put("hmallinoffice", "1");

        AbrechnungPrivat rg = new AbrechnungPrivat(frame, "privateabrechnung", rueckgabeIN, data.preisgruppe,
                (JComponent) frame.getGlassPane(), data.rezeptNummer, preisliste, data.hatAbweichendeAdresse,
                data.patDBId, rezeptVector, patientenDatenVector, "123456789", "HMRechnungPrivat.ott", hmAbrechnung) {
        protected void doUebertrag() {};

        }

                ;

        rg.setLocationRelativeTo(null);
        rg.pack();
        rg.setModal(true);
        rg.setVisible(true);
        int rueckgabeOUT = rg.rueckgabe;
    }

    static class Data {

        int preisgruppe = 10; // steht im rezept in tabelle spalte 42 im vektor 43(?)
        String rezeptDBId = "1769";
        String rezeptNummer = "ER1555";
        String patDBId = "349";
        String hatAbweichendeAdresse = "T"; // TODO : mit "F" wiederholen

        public Data(int preisgruppe, String rezeptDBId, String rezeptNummer, String patDBId,
                String hatAbweichendeAdresse) {
            super();
            this.preisgruppe = preisgruppe;
            this.rezeptDBId = rezeptDBId;
            this.rezeptNummer = rezeptNummer;
            this.patDBId = patDBId;
            this.hatAbweichendeAdresse = hatAbweichendeAdresse;
        }
    }
}
