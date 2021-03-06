package systemTools;

import javax.swing.JComponent;

import hauptFenster.AktiveFenster;
import hauptFenster.Reha;

public class TestePatStamm {
    public static String PatStammArztID() {
        // String ret = "";
        JComponent patient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
        if (patient == null) {
            return "";
        }
        if (!(Reha.instance.patpanel.vecaktrez == null)) {
            if (Reha.instance.patpanel.vecaktrez.size() > 0) {
                return Reha.instance.patpanel.vecaktrez.get(16);
            }
        }
        if (!(Reha.instance.patpanel.patDaten == null)) {
            if (Reha.instance.patpanel.patDaten.size() > 0) {
                return Reha.instance.patpanel.patDaten.get(67);
            }
        } else {
            return "";
        }
        return "";
    }

    public static String PatStammKasseID() {
        // String ret = "";
        JComponent patient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
        if (patient == null) {
            return "";
        }
        if (!(Reha.instance.patpanel.vecaktrez == null)) {
            if (Reha.instance.patpanel.vecaktrez.size() > 0) {
                return Reha.instance.patpanel.vecaktrez.get(37);
            }
        }
        if (!(Reha.instance.patpanel.patDaten == null)) {
            if (Reha.instance.patpanel.patDaten.size() > 0) {
                return Reha.instance.patpanel.patDaten.get(68);
            }
        } else {
            return "";
        }
        return "";
    }

}
