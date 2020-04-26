package hauptFenster;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.sun.star.uno.Exception;

import ag.ion.bion.officelayer.text.TextException;
import dialoge.About;
import environment.LadeProg;
import environment.Path;
import io.RehaIOMessages;
import opRgaf.OpRgaf;
import rechteTools.Rechte;
import systemEinstellungen.SystemConfig;
import systemTools.RezeptFahnder;
import systemTools.TestePatStamm;
import wecker.Wecker;

final class MenuActionListener implements ActionListener {
    /**
     *
     */
    private final Reha reha;

    /**
     * @param reha
     */
    MenuActionListener(Reha reha) {
        this.reha = reha;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        String cmd = arg0.getActionCommand();
        switch (cmd) {
        case "ueberTheraPi":
            About dialog = new About();
            dialog.collectValues();
            dialog.setVisible(true);

            break;
        case "f2Rescue":
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws java.lang.Exception {
                    if (Reha.terminLookup.size() <= 0) {
                        JOptionPane.showMessageDialog(null, "Bislang sind noch keine F3 / F2-Termine aufgezeichnet");
                        return null;
                    }
                    try {
                        new F2RettungsAnker();
                    } catch (TextException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }.execute();
            break;
        case "testeFango":
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws java.lang.Exception {
                    Wecker.testeWecker();
                    return null;
                }
            }.execute();
            break;
        case "patient":
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    reha.progLoader.ProgPatientenVerwaltung(1, reha.conn);
                    reha.progressStarten(false);
                    return null;
                }
            }.execute();
            return;
        case "kasse":
            reha.progLoader.KassenFenster(0, TestePatStamm.PatStammKasseID());
            return;
        case "arzt":
            reha.progLoader.ArztFenster(0, TestePatStamm.PatStammArztID());
            return;
        case "hmabrechnung":
            try {
                if (SystemConfig.hmEmailExtern.get("SenderAdresse") == null
                        || SystemConfig.hmEmailExtern.get("SenderAdresse")
                                                     .trim()
                                                     .equals("")) {
                    JOptionPane.showMessageDialog(null,
                            "<html>Bevor zum ersten Mal mit der GKV abgerechnet wird<br><br><b>muß(!) der Emailaccount in der System-Init konfiguriert werden.</b><br><br></html>");
                    return;
                }
            } catch (NullPointerException ex) {
                JOptionPane.showMessageDialog(null,
                        "<html>Bevor zum ersten Mal mit der GKV abgerechnet wird<br><br><b>muß(!) der Emailaccount in der System-Init konfiguriert werden.</b><br><br></html>");
                return;
            }
            reha.progLoader.AbrechnungFenster(1, this.reha.conn);
            return;
        case "rehaabrechnung":
            reha.progLoader.RehaabrechnungFenster(1, "");
            return;
        case "barkasse":
            reha.progLoader.BarkassenFenster(1, "");
            return;
        case "anmeldezahlen":
            reha.progLoader.AnmeldungenFenster(1, "", this.reha.conn);
            return;
        case "tagesumsatz":
            reha.progLoader.UmsatzFenster(1, "");
            return;
        case "verkauf":
            reha.progLoader.VerkaufFenster(1, "");
            return;
        case "urlaub":
            if (!Rechte.hatRecht(Rechte.Funktion_urlaubueberstunden, true)) {
                return;
            }
            new LadeProg(Path.Instance.getProghome() + "RehaUrlaub.jar" + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK());
            return;
        case "umsatzbeteiligung":
            reha.progLoader.BeteiligungFenster(1, "");
            return;
        case "lvastatistik":
            new LadeProg(Path.Instance.getProghome() + "RehaStatistik.jar" + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK());
            return;
        case "offeneposten":
            if (!Rechte.hatRecht(Rechte.Funktion_offeneposten, true)) {
                return;
            }
            if (!RehaIOServer.offenePostenIsActive) {
                new LadeProg(Path.Instance.getProghome() + "OffenePosten.jar" + " " + Path.Instance.getProghome() + " "
                        + Reha.getAktIK() + " " + Reha.xport);
            } else {
                new ReverseSocket().setzeRehaNachricht(RehaIOServer.offenePostenreversePort,
                        "Reha#" + RehaIOMessages.MUST_GOTOFRONT);
            }
            return;
        case "rezeptfahnder":
            new RezeptFahnder(true, this.reha.conn);
            return;
        case "rgaffaktura":
            if (!Rechte.hatRecht(Rechte.Funktion_barkasse, false)) {
                JOptionPane.showMessageDialog(null, "Keine Berechtigung -> Funktion Ausbuchen RGAF-Faktura");
                return;
            }
            if (!RehaIOServer.rgAfIsActive) {
            	String[] args = new String[] {Path.Instance.getProghome(),
            			 Reha.getAktIK() , String.valueOf(Reha.xport)};
                new LadeProg(Path.Instance.getProghome() + "OpRgaf.jar" + " " + Path.Instance.getProghome() + " "
                        + Reha.getAktIK() + " " + Reha.xport);
            } else {
                new ReverseSocket().setzeRehaNachricht(RehaIOServer.rgAfreversePort,
                        "Reha#" + RehaIOMessages.MUST_GOTOFRONT);
            }
            return;
        case "kassenbuch":
            if (!Rechte.hatRecht(Rechte.Funktion_kassenbuch, true)) {
                return;
            }
            new LadeProg(Path.Instance.getProghome() + "RehaKassenbuch.jar" + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK());
            return;
        case "geburtstagsbriefe":
            if (!Rechte.hatRecht(Rechte.Sonstiges_geburtstagsbriefe, true)) {
                return;
            }
            new LadeProg(Path.Instance.getProghome() + "GBriefe.jar" + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK());
            return;
        case "sqlmodul":
            if (!Rechte.hatRecht(Rechte.Sonstiges_sqlmodul, true)) {
                return;
            }
            if (!RehaIOServer.rehaSqlIsActive) {
                new LadeProg(Path.Instance.getProghome() + "RehaSql.jar" + " " + Path.Instance.getProghome() + " "
                        + Reha.getAktIK() + " " + String.valueOf(Integer.toString(Reha.xport))
                        + (!Rechte.hatRecht(Rechte.BenutzerSuper_user, false) ? " readonly" : " full"));
            } else {
                new ReverseSocket().setzeRehaNachricht(RehaIOServer.rehaSqlreversePort,
                        "Reha#" + RehaIOMessages.MUST_GOTOFRONT);
            }
            return;
        case "fallsteuerung":
            if (!Rechte.hatRecht(Rechte.Sonstiges_Reha301, true)) {
                return;
            }
            if (RehaIOServer.reha301IsActive) {
                JOptionPane.showMessageDialog(null, "Das 301-er Modul läuft bereits");
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new ReverseSocket().setzeRehaNachricht(RehaIOServer.reha301reversePort,
                                "Reha301#" + RehaIOMessages.MUST_GOTOFRONT);
                    }
                });
                return;
            }
            new LadeProg(Path.Instance.getProghome() + "Reha301.jar" + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK() + " " + String.valueOf(Integer.toString(Reha.xport)));
            return;
        case "workflow":
            if (!Rechte.hatRecht(Rechte.Sonstiges_Reha301, true)) {
                return;
            }
            if (RehaIOServer.rehaWorkFlowIsActive) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new ReverseSocket().setzeRehaNachricht(RehaIOServer.rehaWorkFlowreversePort,
                                "ZeigeFrame#" + RehaIOMessages.MUST_GOTOFRONT);
                    }
                });
                return;
            }
            new LadeProg(Path.Instance.getProghome() + "WorkFlow.jar" + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK() + " " + String.valueOf(Integer.toString(Reha.xport)));
            return;
        case "hmrsearch":
            System.out.println("isActive = " + RehaIOServer.rehaHMKIsActive);
            if (RehaIOServer.rehaHMKIsActive) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String searchrez = (reha.patpanel != null
                                ? " " + reha.patpanel.vecaktrez.get(1)
                                : "");
                        new ReverseSocket().setzeRehaNachricht(RehaIOServer.rehaHMKreversePort,
                                "Reha#" + RehaIOMessages.MUST_GOTOFRONT);
                        if (!searchrez.isEmpty()) {
                            new ReverseSocket().setzeRehaNachricht(RehaIOServer.rehaHMKreversePort,
                                    "Reha#" + RehaIOMessages.MUST_REZFIND + "#" + searchrez);
                        }
                    }
                });
                return;
            }
            new LadeProg(Path.Instance.getProghome() + "RehaHMK.jar" + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK() + " " + String.valueOf(Integer.toString(Reha.xport))
                    + (reha.patpanel != null ? " " + reha.patpanel.vecaktrez.get(1) : ""));
            // System.out.println("Übergebe Rezeptnummer:
            // "+SystemConfig.hmAdrRDaten.get("Rnummer"));
            // Reha.thisFrame.setCursor(reha.wartenCursor);
            return;
        case "iniedit":
            if (!Rechte.hatRecht(Rechte.Sonstiges_sqlmodul, true)) {
                return;
            }
            new LadeProg(Path.Instance.getProghome() + "RehaIniedit.jar" + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK());
            return;
        case "ocr":
            new LadeProg(Path.Instance.getProghome() + "RehaOCR.jar" + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK() + " " + String.valueOf(Integer.toString(Reha.xport)));
            return;
        }

    }
}
