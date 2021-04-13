package abrechnung.privat;

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
import CommonTools.StringTools;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import mandant.Mandant;
import office.OOService;
import sql.DatenquellenFactory;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;
import umfeld.Betriebsumfeld;

public class AbrechnungPrivatTest {

    private static final Data HM0000 = new Data(10, "1769", "ER1555", "349", "T");
    private static final String libPath = "C:/RehaVerwaltung/Libraries/lib/openofficeorg";
    private static final String ooPath = "C:/Program Files (x86)/OpenOffice 4";




    public static void main(String[] args) throws SQLException, FileNotFoundException, OfficeApplicationException {
        String aktik = "123456789";
        JXFrame frame = new JXFrame();

        new SqlInfo().setConnection(new DatenquellenFactory(aktik).createConnection());

       OOService.setLibpath(libPath, ooPath);
       new OOService().start();


        AktuelleRezepte.tabelleaktrez = new JXTable();
        Data data1 = new Data(2, "1728", "ER1516", "260", "T");
    //   data = HM0000;
        Data data = new Data(10, "1704", "ER1411", "30", "T");
        Vector<String> rezeptVector = SqlInfo.holeSatz("verordn", " * ", "id = '" + data.rezeptDBId + "'");

        Vector<String> patientenDatenVector = SqlInfo.holeSatz("pat5", " * ", "id ='" + data.patDBId + "'");

        String disziplinFromRezNr = RezTools.getDisziplinFromRezNr(data.rezeptNummer);

        SystemPreislisten.ladepreise(disziplinFromRezNr, aktik);
        Vector<Vector<String>> preisliste = SystemPreislisten.hmPreise.get(disziplinFromRezNr)
                                                                      .get(data.preisgruppe - 1);
new Betriebsumfeld(new Mandant("123456789", "testmandant"));
        SystemConfig.AbrechnungParameter();
        HashMap<String, String> hmAbrechnung = SystemConfig.hmAbrechnung;
        hmAbrechnung.put("hmallinoffice", "1");

        HashMap<String, Vector<String>> hmPreisGruppen = SystemPreislisten.hmPreisGruppen;
        AbrechnungPrivat rg = new AbrechnungPrivat(frame, "privateabrechnung", data.preisgruppe, (JComponent) frame.getGlassPane(),
                data.rezeptNummer, preisliste, data.hatAbweichendeAdresse, data.patDBId,
                rezeptVector, patientenDatenVector, "123456789", "HMRechnungPrivat.ott", hmAbrechnung, hmPreisGruppen.get(StringTools.getDisziplin(data.rezeptNummer))) {
        protected void doUebertrag() {};

        };

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
