package stammDatenTools;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import CommonTools.SqlInfo;
import CommonTools.StringTools;
import hauptFenster.Reha;
import systemEinstellungen.SystemConfig;

public class KasseTools {
    public static void constructKasseHMap(String id) {
        try {
            int xid;
            if (id.equals("")) {
                xid = StringTools.ZahlTest(Reha.instance.patpanel.getPatDaten().get(68));
            } else {
                xid = Integer.parseInt(id);
            }
            if (xid <= 0) {
                return;
            }

            List<String> nichtlesen = Arrays.asList(new String[] { "" });
            Vector<String> vec = SqlInfo.holeSatz("kass_adr",
                    "kassen_nam1,kassen_nam2,strasse,plz,ort,telefon,fax,email1", "id='" + xid + "'", nichtlesen);
            SystemConfig.hmAdrKDaten.put("<Kadr1>", vec.get(0)
                                                       .trim());
            SystemConfig.hmAdrKDaten.put("<Kadr2>", vec.get(1)
                                                       .trim());
            SystemConfig.hmAdrKDaten.put("<Kadr3>", vec.get(2)
                                                       .trim());
            SystemConfig.hmAdrKDaten.put("<Kadr4>", vec.get(3)
                                                       .trim()
                    + " " + vec.get(4)
                               .trim());
            SystemConfig.hmAdrKDaten.put("<Ktel>", vec.get(5)
                                                      .trim());
            SystemConfig.hmAdrKDaten.put("<Kfax>", vec.get(6)
                                                      .trim());
            SystemConfig.hmAdrKDaten.put("<Kemail>", vec.get(7)
                                                        .trim());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Fehler bei der Aufbereitung der Kassenadresse");
        }
    }

}
