package hauptFenster;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.sun.star.uno.Exception;

import CommonTools.SqlInfo;
import Suchen.ICDrahmen;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.text.TextException;
import arztBaustein.ArztBaustein;
import dialoge.DatumWahl;
import environment.LadeProg;
import environment.Path;
import generalSplash.RehaSplash;
import gui.Cursors;
import io.RehaIOMessages;
import mandant.IK;
import oOorgTools.OOTools;
import rechteTools.Rechte;
import rehaWissen.RehaWissen;
import systemEinstellungen.SystemConfig;
import systemTools.TestePatStamm;
import terminKalender.TerminFenster.Ansicht;
import textBausteine.textbaus;
import wecker.Wecker;

final class LinkeTaskPaneListener implements ActionListener {
    /**
     *
     */
    private final Reha reha;

    /**
     * @param reha
     */
    LinkeTaskPaneListener(Reha reha) {
        this.reha = reha;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String cmd = e.getActionCommand();
        switch (cmd) {

        case "System Initialisierung":
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {

                    Reha.getThisFrame()
                        .setCursor(Cursors.wartenCursor);
                    Reha.instance.progLoader.SystemInit(1, "");
                    Reha.getThisFrame()
                        .setCursor(Cursors.normalCursor);

                    return null;
                }

            }.execute();
            break;
        case "Krankenkassen":
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Reha.getThisFrame()
                        .setCursor(Cursors.wartenCursor);
                    Reha.instance.progLoader.KassenFenster(0, TestePatStamm.PatStammKasseID());

                    Reha.getThisFrame()
                        .setCursor(Cursors.normalCursor);
                    return null;
                }
            }.execute();
            break;
        case "Terminkalender starten":
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Reha.getThisFrame()
                        .setCursor(Cursors.wartenCursor);
                    Reha.instance.progLoader.ProgTerminFenster(1,
                            SystemConfig.KalenderStartWochenAnsicht ? Ansicht.WOCHE : Ansicht.NORMAL);

                    Reha.getThisFrame()
                        .setCursor(Cursors.normalCursor);
                    return null;
                }
            }.execute();
            break;
        case "Arztstamm":
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Reha.getThisFrame().setCursor(Cursors.wartenCursor);
                    Reha.instance.progLoader.ArztFenster(0, TestePatStamm.PatStammArztID());

                    Reha.getThisFrame()
                        .setCursor(Cursors.normalCursor);
                    return null;
                }
            }.execute();
            break;
        case "Wochenarbeitszeiten definieren":
            JComponent termin = AktiveFenster.getFensterAlle("TerminFenster");
            if (termin != null) {
                JOptionPane.showMessageDialog(null,
                        "Achtung!!!!! \n\nWährend der Arbeitszeit-Definition\n"
                                + "darf der Terminkalender aus Sicherheitsgründen nicht geöffnet sein.\n"
                                + "Beenden Sie den Terminkalender und rufen Sie diese Funktion erneut auf.\n\n");
                return;
            }
            Reha.instance.progLoader.ProgTerminFenster(0, Ansicht.MASKE);
            break;
        case "monthview":
            new DatumWahl(200, 200);
            break;
        case "OpenOffice-Writer":
            OOTools.starteLeerenWriter();
            break;
        case "OpenOffice-Calc":
            OOTools.starteLeerenCalc();
            break;
        case "OpenOffice-Impress":
            OOTools.starteLeerenImpress();
            break;
        case "Benutzerverwaltung":
            Reha.instance.progLoader.BenutzerrechteFenster(1, "");
            break;
        case "[Ru:gl] - Die Terminsuchmaschine":
            Reha.instance.progLoader.ProgRoogleFenster(0, null);
            break;
        case "RTA-Wisssen das Universalwissen":
            break;
        case "Thera-PI - Browser":
            new Thread() {
                @Override
                public void run() {
                    RehaWissen.main(new String[0]);
                }
            }.start();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    RehaSplash rspl = new RehaSplash(null,
                            "Hilfebrowser laden....dieser Vorgang kann einige Sekunden dauern...");
                    long zeit = System.currentTimeMillis();
                    while (true) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Reha.logger.error("thread was interrupted", e);
                        }
                        if (System.currentTimeMillis() - zeit > 6000) {
                            break;
                        }
                    }
                    rspl.dispose();
                    return null;
                }

            }.execute();
            break;
        case "Thera-PI - Nachrichten":
            if (!RehaIOServer.rehaMailIsActive) {
                if (Reha.aktUser.startsWith("Therapeut")) {
                    return;
                }
                Reha.getThisFrame()
                    .setCursor(Cursors.wartenCursor);
                new Thread() {
                    @Override
                    public void run() {
                        new LadeProg(Path.Instance.getProghome() + "RehaMail.jar" + " "
                                + Path.Instance.getProghome() + " " + Reha.getAktIK() + " " + Reha.xport + " "
                                + Reha.aktUser.replace(" ", "#"));
                    }
                }.start();

            } else {
                if (Reha.aktUser.startsWith("Therapeut")) {
                    return;
                }
                new ReverseSocket().setzeRehaNachricht(RehaIOServer.rehaMailreversePort,
                        "Reha#" + RehaIOMessages.MUST_GOTOFRONT);
            }
            break;
        case "Neuer Wissensbeitrag anlegen":
            JOptionPane.showMessageDialog(null, "Achtung!!!!! \n\nDer Wissens-Generator ist auf diesem System\n\n"
                    + "nicht installiert - oder konnte nicht gefunden werden...\n\n");
            break;
        case "Patienten und Rezepte":
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Reha.thisFrame.setCursor(Cursors.wartenCursor);
                    Reha.instance.progLoader.ProgPatientenVerwaltung(1, LinkeTaskPaneListener.this.reha.conn);

                    Reha.thisFrame.setCursor(Cursors.normalCursor);
                    return null;
                }

            }.execute();
            break;
        case "piHelp":
            new Thread() {
                @Override
                public void run() {

                    new LadeProg(Path.Instance.getProghome() + "piHelp.jar" + " " + Path.Instance.getProghome() + " "
                            + Reha.getAktIK());
                }
            }.start();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    RehaSplash rspl = new RehaSplash(null,
                            "piHelp - Hilfetextgenerator laden....dieser Vorgang kann einige Sekunden dauern...");
                    long zeit = System.currentTimeMillis();
                    while (true) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Reha.logger.error("thread was interrupted", e);
                        }
                        if (System.currentTimeMillis() - zeit > 2000) {
                            break;
                        }
                    }
                    rspl.dispose();
                    return null;
                }

            }.execute();
            break;
        case "piTool":
            new LadeProg(Path.Instance.getProghome() + "piTool.jar");
            break;
        case "piTextb":
            textbaus.main(new String[] { Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/rehajava.ini",
                    Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/thbericht.ini"

            });

            break;
        case "piArztTextb":
            if (!Rechte.hatRecht(Rechte.Sonstiges_textbausteinegutachten, true)) {
                return;
            }
            new Thread() {
                @Override
                public void run() {
                    try {
                        ArztBaustein.start(new IK(Reha.getAktIK()), Reha.officeapplication);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    RehaSplash rspl = new RehaSplash(null,
                            "Textbaustein-Editor laden....dieser Vorgang kann einige Sekunden dauern...");
                    long zeit = System.currentTimeMillis();
                    while (true) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Reha.logger.error("thread was interrupted", e);
                        }
                        if (System.currentTimeMillis() - zeit > 2000) {
                            break;
                        }
                    }
                    rspl.dispose();
                    return null;
                }
            }.execute();
            break;
        case "piIcd10":
            SwingUtilities.invokeLater(new ICDrahmen(this.reha.conn));
            break;
        case "piQM":
            new Thread() {
                @Override
                public void run() {
                    new LadeProg(Path.Instance.getProghome() + "QMHandbuch.jar");
                }
            }.start();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    RehaSplash rspl = new RehaSplash(null,
                            "QM-Handbuch laden....dieser Vorgang kann einige Sekunden dauern...");
                    long zeit = System.currentTimeMillis();
                    while (true) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Reha.logger.error("thread was interrupted", e);
                        }
                        if (System.currentTimeMillis() - zeit > 4000) {
                            break;
                        }
                    }
                    rspl.dispose();
                    return null;
                }

            }.execute();
            break;
        case "piAW":
            new Thread() {
                @Override
                public void run() {
                    new LadeProg(Path.Instance.getProghome() + "QMAuswertung.jar" + " "
                            + Path.Instance.getProghome() + " " + Reha.getAktIK());
                }
            }.start();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    RehaSplash rspl = new RehaSplash(null,
                            "QM-Auswertung laden....dieser Vorgang kann einige Sekunden dauern...");
                    long zeit = System.currentTimeMillis();
                    while (true) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Reha.logger.error("thread was interrupted", e);
                        }
                        if (System.currentTimeMillis() - zeit > 4000) {
                            break;
                        }
                    }
                    rspl.dispose();
                    return null;
                }

            }.execute();
            break;
        case "Akutliste":
            if (SqlInfo.holeEinzelFeld("select id from pat5 where akutpat='T' LIMIT 1")
                       .equals("")) {
                JOptionPane.showMessageDialog(null, "Keine Akutpatienten im Patientenstamm vermerkt");
                return;
            }
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Reha.getThisFrame()
                        .setCursor(Cursors.wartenCursor);
                    try {
                        new AkutListe(Reha.officeapplication.getDocumentService());
                    } catch (TextException | OfficeApplicationException e) {

                        e.printStackTrace();
                    }
                    Reha.getThisFrame()
                        .setCursor(Cursors.cdefault);
                    return null;
                }
            }.execute();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    RehaSplash rspl = new RehaSplash(null,
                            "Akutliste starten -  dieser Vorgang kann einige Sekunden dauern...");
                    long zeit = System.currentTimeMillis();
                    while (true) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Reha.logger.error("thread was interrupted", e);
                        }
                        if (System.currentTimeMillis() - zeit > 2000) {
                            break;
                        }
                    }
                    rspl.dispose();
                    return null;
                }

            }.execute();
            break;
        case "neuerwecker":
            Wecker wecker = new Wecker(null);
            wecker.pack();
            wecker.setVisible(true);
            wecker = null;
            break;
        }
        if (cmd.startsWith("UserTask-")) {
            try {
                int taskNummer = Integer.parseInt(cmd.toString()
                                                     .split("-")[1]);
                Runtime.getRuntime()
                       .exec(SystemConfig.vUserTasks.get(taskNummer)
                                                    .get(2));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
}
