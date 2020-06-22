package org.therapi.reha.patient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import CommonTools.StringTools;
import dialoge.EmailDialog;
import dialoge.SMSDialog;
import dialoge.ToolsDialog;
import environment.LadeProg;
import environment.Path;
import gui.Cursors;
import hauptFenster.Reha;
import hauptFenster.RehaIOServer;
import hauptFenster.ReverseSocket;
import io.RehaIOMessages;
import rechteTools.Rechte;
import systemEinstellungen.SystemConfig;
import systemTools.IconListRenderer;
import terminKalender.iCalRehaExporter;

public class PatientToolBarLogic {
    PatientHauptPanel patientHauptPanel = null;
    PatientToolBarPanel patientToolBarPanel = null;

    public PatientToolBarLogic(PatientHauptPanel patientHauptPanel, PatientToolBarPanel patientTbPanel) {
        this.patientHauptPanel = patientHauptPanel;
        this.patientToolBarPanel = patientTbPanel;
    }

    public void reactOnMouseClicked(MouseEvent arg0) {
        if (arg0.getSource() instanceof JLabel) {
            if (((JComponent) arg0.getSource()).getName()
                                               .equals("Suchen")) {
                if (patientHauptPanel.inMemo > -1) {
                    Reha.instance.patpanel.pmemo[patientHauptPanel.inMemo].requestFocus();
                    return;
                }
                patientHauptPanel.starteSuche();
                return;
            }
        }
    }

    public void reactOnKeyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) { // ENTER im Suchfeld
            String name = ((JComponent) e.getSource()).getName();
            if (name != null) {
                if (name.equals("suchenach")) {
                    patientHauptPanel.patientLogic.starteSuche();
                } else if (name.equals("Suchkriterium") && patientHauptPanel.patToolBarPanel.getSucheOhneEingabe(patientHauptPanel.jcom.getSelectedIndex())) {
                    patientHauptPanel.patientLogic.starteSuche();
                }
            }
        } else if (e.getKeyCode() == KeyEvent.VK_F1 && (!patientHauptPanel.aktPatID.equals(""))) {
            if (Reha.bRGAFoffen || Reha.bHatMerkmale) {
                new Thread() {
                    @Override
                    public void run() {
                        new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                try {
                                    patientHauptPanel.holeWichtigeInfos(patientHauptPanel.aktPatID);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                return null;
                            }
                        }.execute();
                    }
                }.start();
            }
        }
    }

    public void fireAufraeumen() {
        for (int i = 0; i < patientHauptPanel.jbut.length; i++) {
            if (patientHauptPanel.jbut[i] != null) {
                patientHauptPanel.jbut[i].removeActionListener(patientHauptPanel.toolBarAction);
                patientHauptPanel.jbut[i] = null;
            }
        }
        patientHauptPanel.toolBarAction = null;
        patientHauptPanel.tfsuchen.removeKeyListener(patientHauptPanel.toolBarKeys);
        patientHauptPanel.toolBarKeys = null;
        patientToolBarPanel.sucheLabel.removeMouseListener(patientHauptPanel.toolBarMouse);
        patientHauptPanel.toolBarMouse = null;
        patientToolBarPanel.sucheLabel.removeFocusListener(patientHauptPanel.toolBarFocus);
        patientHauptPanel.tfsuchen.removeFocusListener(patientHauptPanel.toolBarFocus);
        patientHauptPanel.tfsuchen.getDropTarget()
                                  .removeDropTargetListener(patientHauptPanel.dropTargetListener);
        patientHauptPanel.dropTargetListener = null;
        patientHauptPanel.toolBarFocus = null;
        patientToolBarPanel.sucheLabel = null;
        patientHauptPanel = null;

    }

    public void reactOnFocusGained(FocusEvent e) {
        if (((JComponent) e.getSource()).getName()
                                        .equals("suchenach")
                && patientHauptPanel.inMemo > -1) {
            Reha.instance.patpanel.pmemo[patientHauptPanel.inMemo].requestFocus();
        }
        if (!patientHauptPanel.getInternal()
                              .getActive()) {
            patientHauptPanel.getInternal()
                             .setSpecialActive(true);
        }
    }

    public void reactOnAction(ActionEvent arg0) {
        String cmd = arg0.getActionCommand();
        if (cmd.equals("neu")) {
            patientHauptPanel.getLogic()
                             .patNeu();
        }
        if (cmd.equals("edit")) {
            patientHauptPanel.getLogic()
                             .patEdit();
        }
        if (cmd.equals("delete")) {
            patientHauptPanel.getLogic()
                             .patDelete();
        }
        if (cmd.equals("formulare")) {
            patientHauptPanel.getLogic()
                             .patStarteFormulare();
            // patientHauptPanel.getLogic().setzeFocus();
        }
        if (cmd.equals("email")) {
            patientHauptPanel.getLogic()
                             .setzeFocus();
        }
        if (cmd.equals("sms")) {
            // new SMS();
            patientHauptPanel.getLogic()
                             .setzeFocus();
        }
        if (cmd.equals("werkzeuge")) {
            new ToolsDlgPatient("", patientHauptPanel.jbut[4].getLocationOnScreen());
            // patientHauptPanel.getLogic().setzeFocus();
        }
        if (cmd.equals("comboBoxChanged")) {
            int selIdx = patientHauptPanel.jcom.getSelectedIndex();
            if (patientHauptPanel.patToolBarPanel.getSucheOhneEingabe(selIdx)) {
                // Eingabe sperren
                patientHauptPanel.tfsuchen.setBackground(Color.lightGray);
                patientHauptPanel.tfsuchen.setForeground(Color.red);
                patientHauptPanel.tfsuchen.setOpaque(true);
                patientHauptPanel.tfsuchen.setEditable(false);
            } else {
                // Eingabe freigeben
                patientHauptPanel.tfsuchen.setForeground(Color.gray);
                patientHauptPanel.tfsuchen.setOpaque(false);
                patientHauptPanel.tfsuchen.setEditable(true);
            }
            String txt = patientHauptPanel.patToolBarPanel.getToolTipText(selIdx);
            patientHauptPanel.tfsuchen.setToolTipText(txt);
        }
    }

    public void doPatNachricht() {
        new Thread() {
            @Override
            public void run() {
                try {
                    // hier Pat_intern und Rezeptnummer ermitteln
                    if (patientHauptPanel.aktPatID.equals("")) {
                        return;
                    }
                    String spat_intern = patientHauptPanel.patDaten.get(29);
                    String srez_nr = "-1";
                    String sbetreff = "Patient(in): " + StringTools.EGross(patientHauptPanel.patDaten.get(2)) + ", "
                            + StringTools.EGross(patientHauptPanel.patDaten.get(3)) + " PatID: " + spat_intern;
                    if (AktuelleRezepte.tabaktrez.getRowCount() > 0) {
                        srez_nr = Reha.instance.patpanel.vecaktrez.get(1);
                    }
                    if (!RehaIOServer.rehaMailIsActive) {
                        if (Reha.aktUser.startsWith("Therapeut")) {
                            return;
                        }
                        Reha.getThisFrame()
                            .setCursor(Cursors.wartenCursor);

                        new LadeProg(Path.Instance.getProghome() + "RehaMail.jar" + " " + Path.Instance.getProghome()
                                + " " + Reha.getAktIK() + " " + Reha.xport + " " + Reha.aktUser.replace(" ", "#"));
                        long warten = System.currentTimeMillis();
                        while ((!RehaIOServer.rehaMailIsActive) && (System.currentTimeMillis() - warten < 15000)) {
                            Thread.sleep(75);
                        }
                        if (!RehaIOServer.rehaMailIsActive) {
                            JOptionPane.showMessageDialog(null, "Kann Thera-Pi Nachrichten nicht starten");
                            return;
                        }

                    } else {
                        if (Reha.aktUser.startsWith("Therapeut")) {
                            return;
                        }
                        new ReverseSocket().setzeRehaNachricht(RehaIOServer.rehaMailreversePort,
                                "Reha#" + RehaIOMessages.MUST_GOTOFRONT);
                    }
                    new ReverseSocket().setzeRehaNachricht(RehaIOServer.rehaMailreversePort,
                            "Reha#WANTPATMESSAGE#" + spat_intern + "#" + srez_nr + "#" + sbetreff);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();

    }

    public void doPatFragebogen() {
        try {
            if (patientHauptPanel.aktPatID.equals("")) {
                return;
            }
            String srez_nr = "-1";
            if (AktuelleRezepte.tabaktrez.getRowCount() > 0) {
                srez_nr = Reha.instance.patpanel.vecaktrez.get(1);
            }
            if (srez_nr.isEmpty() || (!srez_nr.startsWith("RH"))) {
                try {
                    srez_nr = Reha.instance.patpanel.vecakthistor.get(1);
                } catch (Exception ex) {
                    srez_nr = "-1";
                }

            }
            final String spat_intern = patientHauptPanel.patDaten.get(29);
            final String xsrez_nr = srez_nr;
            new Thread() {
                @Override
                public void run() {
                    new LadeProg(Path.Instance.getProghome() + "RehaFbBrowser.jar" + " ?pat_intern=" + spat_intern
                            + "#rez_nr=" + xsrez_nr);
                }
            }.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void doSMS() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // nur wenn SMS-Service aktiviert ist
                    if (SystemConfig.activateSMS && (!patientHauptPanel.aktPatID.equals(""))
                            && (SystemConfig.hmSMS.get("SMS")
                                                  .equals("1"))) {
                        // nur wenn einen Mobilfunknummer eingetragen ist
                        if (!patientHauptPanel.patDaten.get(20)
                                                       .isEmpty()) {
                            Point pt = patientHauptPanel.jbut[4].getLocationOnScreen();
                            String stitel = ("SMS für Patient erstellen");
                            final SMSDialog smsDlg = new SMSDialog(Reha.getThisFrame(), stitel,
                                    PatientToolBarLogic.this, true, "SMS über " + SystemConfig.hmSMS.get("NAME")
                                            + " an " + patientHauptPanel.patDaten.get(20),
                                    patientHauptPanel.patDaten.get(20));
                            smsDlg.setPreferredSize(new Dimension(475, 200));
                            smsDlg.setLocation(pt.x - 350, pt.y + 100);
                            smsDlg.pack();
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    smsDlg.setTextCursor(0);
                                }
                            });

                            smsDlg.setVisible(true);
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    smsDlg.setTextCursor(0);
                                }
                            });
                        } else {
                            JOptionPane.showMessageDialog(null, "Keine Mobilfunknummer im Patientenstamm hinterlegt");
                        }

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return null;
            }

        }.execute();
    }

    public void doEmail() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // nur wenn SMS-Service aktiviert ist

                    // nur wenn einen Mobilfunknummer eingetragen ist
                    if (patientHauptPanel.patDaten.size() > 0) {
                        Point pt = patientHauptPanel.jbut[4].getLocationOnScreen();
                        String stitel = ("");
                        String mailtext = SystemConfig.hmAdrPDaten.get("<Pbanrede>")
                                + ",\n\n\nMit freundlichen Grüßen\n\n\n" + SystemConfig.hmFirmenDaten.get("Firma1")
                                + "\n" + SystemConfig.hmFirmenDaten.get("Strasse") + "\n"
                                + SystemConfig.hmFirmenDaten.get("Plz") + " " + SystemConfig.hmFirmenDaten.get("Ort")
                                + "\n" + "Telefon: " + SystemConfig.hmFirmenDaten.get("Telefon") + "\n"
                                + (SystemConfig.hmFirmenDaten.get("Telefax")
                                                             .equals("")
                                                                     ? ""
                                                                     : "Telefax: "
                                                                             + SystemConfig.hmFirmenDaten.get("Telefax")
                                                                             + "\n")
                                + (SystemConfig.hmFirmenDaten.get("Internet")
                                                             .equals("") ? ""
                                                                     : "Internet: " + SystemConfig.hmFirmenDaten.get(
                                                                             "Internet"));
                        String recipient = Reha.instance.patpanel.patDaten.get(50);
                        // +((Boolean) SystemConfig.hmIcalSettings.get("aufeigeneemail") ?
                        // ","+SystemConfig.hmEmailExtern.get("SenderAdresse") : "");
                        final EmailDialog emlDlg = new EmailDialog(Reha.getThisFrame(), stitel, recipient, "", mailtext,
                                new ArrayList<String[]>(), (Integer) SystemConfig.hmIcalSettings.get("postfach"),
                                false);
                        emlDlg.setPreferredSize(new Dimension(575, 370));
                        emlDlg.setLocation(pt.x - 350, pt.y + 100);
                        emlDlg.pack();
                        emlDlg.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(null, "Kein Patient ausgewählt");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return null;
            }

        }.execute();
    }

    class ToolsDlgPatient {
        public ToolsDlgPatient(String command, Point pt) {
            Map<Object, ImageIcon> icons = new HashMap<Object, ImageIcon>();
            icons.put("Patientenbezogene Nachricht erstellen", SystemConfig.hmSysIcons.get("patnachrichten"));
            icons.put("(e)Mail für Patient erstellen (Alt+M)", SystemConfig.hmSysIcons.get("email"));
            icons.put("SMS für Patient erstellen (Alt+S)", SystemConfig.hmSysIcons.get("sms"));
            icons.put("Zusatz-Info zum aktuellen Patient (Alt+I)", SystemConfig.hmSysIcons.get("info"));
            icons.put("Rehaplandatei -> iCalendar per Email", SystemConfig.hmSysIcons.get("email"));
            // create a list with some test data
            JList list = new JList(new Object[] { "Patientenbezogene Nachricht erstellen",
                    "(e)Mail für Patient erstellen (Alt+M)", "SMS für Patient erstellen (Alt+S)",
                    "Zusatz-Info zum aktuellen Patient (Alt+I)", "Rehaplandatei -> iCalendar per Email" });
            list.setCellRenderer(new IconListRenderer(icons));
            Reha.toolsDlgRueckgabe = -1;
            ToolsDialog tDlg = new ToolsDialog(Reha.getThisFrame(), "Werkzeuge: aktueller Patient", list);
            tDlg.setPreferredSize(new Dimension(300,
                    200 + ((Boolean) SystemConfig.hmPatientenWerkzeugDlgIni.get("ToolsDlgShowButton") ? 25 : 0)));
            tDlg.setLocation(pt.x - 200, pt.y + 30);
            tDlg.pack();
            tDlg.setModal(true);
            tDlg.activateListener();
            tDlg.setVisible(true);
            switch (Reha.toolsDlgRueckgabe) {
            case 0:
                doPatNachricht();
                return;
            case 1:
                if (!Rechte.hatRecht(Rechte.Patient_email, true)) {
                    return;
                }
                doEmail();
                break;
            case 2:
                if (!Rechte.hatRecht(Rechte.Patient_sms, true)) {
                    return;
                }
                doSMS();
                break;
            case 3:
                doPatFragebogen();
                if (!Rechte.hatRecht(Rechte.Patient_zusatzinfo, true)) {

                    return;
                }
                break;
            case 4:
                new iCalRehaExporter();
                break;

            }
            tDlg = null;
        }
    }

}
