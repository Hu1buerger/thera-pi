package patientenFenster;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.LinearGradientPaint;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.MattePainter;
import org.therapi.hmrCheck.HMRCheck2020;
import org.therapi.reha.patient.AktuelleRezepte;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.Colors;
import CommonTools.DatFunk;
import CommonTools.JCompTools;
import CommonTools.JRtaCheckBox;
import CommonTools.JRtaComboBox;
import CommonTools.JRtaTextField;
import CommonTools.SqlInfo;
import CommonTools.StringTools;
import Suchen.ICDrahmen;
import abrechnung.Disziplinen;
import commonData.ArztVec;
import commonData.Rezeptvector;
import commonData.VerordnungsArten;
import environment.LadeProg;
import environment.Path;
import events.RehaTPEvent;
import events.RehaTPEventClass;
import events.RehaTPEventListener;
import gui.Cursors;
import hauptFenster.Reha;
import hmrCheck.HMRCheck;
import rechteTools.Rechte;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;
import systemTools.ListenerTools;

public class RezNeuanlage2020 extends JXPanel implements ActionListener, KeyListener, FocusListener, RehaTPEventListener {

    /**
     * McM '21: Maske für VO nach HMR2020
     *     Konzept:
     *         - Basis ist eine Kopie von 'RezNeuanlage' um minimalen Impact auf den 'Rest' des Programmes zu gewährleisten
     *         - 'ausdünnen' soweit möglich
     *
     */
    // Text-Eingabefgelder der Rezept-Maske
    public JRtaTextField[] jtf = { null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null,  null };
    // Index-Konstanten für Text-Eingabefgelder
    final int cKTRAEG = 0;
    final int cARZT = 1;
    final int cREZDAT = 2;
    final int cBEGINDAT = 3;
    final int cANZ1 = 4; // ACHTUNG, die Positionen cANZ1 bis cANZ4 müssen immer
                         // nacheinander definiert sein
    final int cANZ2 = 5;
    final int cANZ3 = 6;
    final int cANZ4 = 7;
    final int cFREQ = 8;
    final int cDAUER = 9;
    final int cANGEL = 10;
    final int cKASID = 11;
    final int cARZTID = 12;
    final int cPREISGR = 13;
    final int cHEIMBEW = 14;
    final int cBEFREIT = 15;
    final int cPOS1 = 16; // ACHTUNG, die Positionen cPOS1 bis cPOS4 müssen immer
                          // nacheinander definiert sein
    final int cPOS2 = 17;
    final int cPOS3 = 18;
    final int cPOS4 = 19;
    final int cPREIS1 = 20; // ACHTUNG, die Positionen cPREIS1 bis cPREIS4 müssen
                            // immer nacheinander definiert sein
    final int cPREIS2 = 21;
    final int cPREIS3 = 22;
    final int cPREIS4 = 23;
    final int cANLAGDAT = 24;
    final int cANZKM = 25;
    final int cPATID = 26;
    final int cPATINT = 27;
    final int cZZSTAT = 28;
    final int cHEIMBEWPATSTAM = 29;
    final int cICD10 = 30;
    final int cICD10_2 = 31;
    final int cAKUTDATUM = 32;

    // Merken der Originalwerte der eingelesenen Textfelder, Combo- und
    // Check-Boxen
    Vector<Object> originale = new Vector<Object>();

    public JRtaCheckBox[] jcb = { null, null, null, null, null, null, null, null, null };
    // Index-Konstanten für Checkboxen
    final int cHAUSB = 0;
    final int cTBANGEF = 1;
    final int cVOLLHB = 2;
    final int cdringBehBedarf = 3;
    final int cLeitSymptA = 4;
    final int cLeitSymptB = 5;
    final int cLeitSymptC = 6;
    final int cLeitSymptX = 7;
    final int cHygienePausch = 8;

    public JRtaComboBox[] jcmb = { null, null, null, null, null, null, null, null, null };

    final int cRKLASSE = 0;
    final int cLEIST1 = 1; // ACHTUNG, die Positionen cLEIST1 bis cLEIST4 müssen
                           // immer nacheinander definiert sein
    final int cLEIST2 = 2;
    final int cLEIST3 = 3;
    final int cLEIST4 = 4;
    final int cINDI = 5;
    final int cBARCOD = 6;
    final int cFARBCOD = 7;
    final int cVERORDART = 8;

    public JTextArea diagnose_txt = null;
    private JTextArea lsym_x_txt;
    private JTextArea thZielBefund_txt;

    public JButton speichern = null;
    public JButton abbrechen = null;
    public JButton hmrcheck = null;

    public boolean neu = false;

    public Vector<String> vec = null; // Lemmi Doku: Das bekommt den 'vecaktrez' aus dem rufenden Programm
                                      // (AktuelleRezepte)
    public Vector<Vector<String>> preisvec = null;
    private boolean klassenReady = false;
    private boolean initReady = false;
    private static final long serialVersionUID = 1L;
    private int preisgruppe = -1;
    public boolean feldergefuellt = false;
    private String nummer = null;
    private String rezKlasse = null;
    private ArrayList<String> farbcodes = new ArrayList<>();


    private String aktuelleDisziplin = "";
    private int preisgruppen[] = { 0, 0, 0, 0, 0, 0, 0, 0 };
    int[] comboid = { -1, -1, -1, -1 };

    MattePainter mp = null;
    LinearGradientPaint p = null;
    private RehaTPEventClass rtp = null;

    JLabel kassenLab;
    JLabel arztLab;

    String[] strRezepklassenAktiv = null;

    // McM 16/11: Steuerung der Abkürzungen bei Rezepteingabe   
    // ToDo: neu organisieren für *2020!
    private boolean ctrlIsPressed = false;
    private Component eingabeRezDate = null;
    private Component eingabeBehFrequ = null;
    private Component eingabeVerordnArt = null;
    private Component eingabeVerordn1 = null;
    private Component eingabeICD = null;
    private Component eingabeDiag = null;
    private Connection connection;

    private Rezeptvector myRezept = null;
    private Rezeptvector tmpRezept = null;
    private ArztVec verordnenderArzt = null;
    private Disziplinen diszis = null;

    public String[] diagGroupsPhysio = null;
    public String[] diagGroupsErgo = null;
    public String[] diagGroupsLogo = null;
    public String[] diagGroupsPodo = null;
    private String noDiagGrpSelected = "keine DiagGrp";
    private String noDiagGrpInVO = "k.A.";

    public RezNeuanlage2020(Vector<String> vec, boolean neu, Connection connection) { 
        super();
        try {
            this.neu = neu;
            this.vec = vec;
            myRezept = new Rezeptvector();
            verordnenderArzt = new ArztVec();
            myRezept.setVec_rez(vec);
            diszis = new Disziplinen();

            mkDiagGroups();

            if (vec.size() > 0 && this.neu) {
                aktuelleDisziplin = StringTools.getDisziplin(vec.get(1)); 
            }

            setName("RezeptNeuanlage");
            rtp = new RehaTPEventClass();
            rtp.addRehaTPEventListener((RehaTPEventListener) this);

            addKeyListener(this);

            setLayout(new BorderLayout());
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            add(getDatenPanel(), BorderLayout.CENTER);
            add(getButtonPanel(), BorderLayout.SOUTH);
            setBackgroundPainter(Reha.instance.compoundPainter.get("ArztPanel"));
            validate();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setzeFocus();
                }
            });
            initReady = true;
            if (!neu) {
                if (!Rechte.hatRecht(Rechte.Rezept_editvoll, false)) { // Read-Only-Modus für das Rezept
                    for (int i = 0; i < jtf.length; i++) { // alle Textfelder unbedienbar machen
                        if (jtf[i] != null) {
                            jtf[i].setEnabled(false);
                        }
                    }
                    for (int i = 0; i < jcb.length; i++) { // alle CheckBoxen unbedienbar machen
                        if (jcb[i] != null) {
                            jcb[i].setEnabled(false);
                        }
                    }
                    for (int i = 0; i < jcmb.length; i++) { // alle ComboBoxen unbedienbar machen
                        if (jcmb[i] != null) {
                            jcmb[i].setEnabled(false);
                        }
                    }
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler im Konstruktor RezNeuanlage2020\n" + RezNeuanlage2020.makeStacktraceToString(ex));
        }

    }

    public void macheFarbcodes() {
        try {
            farbcodes.add("kein Farbcode");
            jcmb[cFARBCOD].addItem(farbcodes.get(0));
            Vector<String> farbnamen = SystemConfig.vSysColsNamen;
            for (int i = 0; i < farbnamen.size(); i++) {
                if (farbnamen.get(i).startsWith("Col")) {
                String bedeutung = SystemConfig.vSysColsBedeut.get(i);
                farbcodes.add(bedeutung);

                jcmb[cFARBCOD].addItem(bedeutung);
                }
            }
            if (!this.neu) {
                int itest = myRezept.getFarbCode();
                if (itest >= 0) {
                    jcmb[cFARBCOD].setSelectedItem((String) SystemConfig.vSysColsBedeut.get(itest));
                } else {
                    jcmb[cFARBCOD].setSelectedIndex(0);
                }
            } else {
                jcmb[cFARBCOD].setSelectedIndex(0);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fehler bei Farbcodes erstellen\n" + ex.getMessage());
        }

    }

    public void setzeFocus() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (neu) {
                    int aid, kid;
                    boolean beenden = false;
                    String meldung = "";
                    kid = StringTools.ZahlTest(jtf[cKASID].getText());
                    aid = StringTools.ZahlTest(jtf[cARZTID].getText());
                    if (kid < 0 && aid < 0) {
                        beenden = true;
                        meldung = "Achtung - dem Patientenstamm ist keine verwertbare Krankenkasse\n"
                                + "sowie kein verwertbarer Arzt zugeordnet\n\n"
                                + "Gehen Sie im Patientenstamm auf ->Ändern/Editieren<- und ordnen Sie verwertaber Daten zu!";
                    } else if (kid >= 0 && aid < 0) {
                        beenden = true;
                        meldung = "Achtung - dem Patientenstamm ist kein verwertbarer Arzt zugeordnet\n\n"
                                + "Gehen Sie im Patientenstamm auf ->Ändern/Editieren<- und ordnen Sie verwertaber Daten zu!";
                    } else if (kid < 0 && aid >= 0) {
                        beenden = true;
                        meldung = "Achtung - dem Patientenstamm ist keine verwertbare Krankenkasse zugeordnet\n\n"
                                + "Gehen Sie im Patientenstamm auf ->Ändern/Editieren<- und ordnen Sie verwertaber Daten zu!";
                    }
                    if (beenden) {
                        JOptionPane.showMessageDialog(null, meldung);
                        aufraeumen();
                        ((JXDialog) getParent().getParent()
                                               .getParent()
                                               .getParent()
                                               .getParent()).dispose();
                    } else {
                        holePreisGruppe(jtf[cKASID].getText()
                                                   .trim());
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                jcmb[cRKLASSE].requestFocusInWindow();
                            }
                        });
                    }
                    // else bedeutet nicht neu - sondern aendern
                } else {
                    int aid, kid;
                    kid = StringTools.ZahlTest(jtf[cKASID].getText());
                    aid = StringTools.ZahlTest(jtf[cARZTID].getText());
                    if (kid < 0) {
                        jtf[cKASID].setText(Integer.toString(Reha.instance.patpanel.kid));
                        jtf[cKTRAEG].setText(Reha.instance.patpanel.patDaten.get(13));
                    }
                    if (aid < 0) {
                        jtf[cARZTID].setText(Integer.toString(Reha.instance.patpanel.aid));
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            jtf[cKTRAEG].requestFocusInWindow();
                        }
                    });
                }

            }
        });
    }

    public JXPanel getButtonPanel() {
        JXPanel jpan = JCompTools.getEmptyJXPanel();
        jpan.addKeyListener(this);
        jpan.setOpaque(false);
        FormLayout lay = new FormLayout(
                // 1 2 3 4 5 6 7
                "fill:0:grow(0.25),50dlu,fill:0:grow(0.25),50dlu,fill:0:grow(0.25),50dlu,fill:0:grow(0.25)",
                // 1 2 3
                "5dlu,p,5dlu");
        CellConstraints cc = new CellConstraints();
        jpan.setLayout(lay);
        speichern = new JButton("speichern");
        speichern.setActionCommand("speichern");
        speichern.addActionListener(this);
        speichern.addKeyListener(this);
        speichern.setMnemonic(KeyEvent.VK_S);
        jpan.add(speichern, cc.xy(2, 2));

        hmrcheck = new JButton("HMR-Check");
        hmrcheck.setActionCommand("hmrcheck");
        hmrcheck.addActionListener(this);
        hmrcheck.addKeyListener(this);
        hmrcheck.setMnemonic(KeyEvent.VK_H);
        jpan.add(hmrcheck, cc.xy(4, 2));

        abbrechen = new JButton("abbrechen");
        abbrechen.setActionCommand("abbrechen");
        abbrechen.addActionListener(this);
        abbrechen.addKeyListener(this);
        abbrechen.setMnemonic(KeyEvent.VK_A);
        jpan.add(abbrechen, cc.xy(6, 2));

        return jpan;
    }

    /********************************************/

    // Merken der Originalwerte der eingelesenen Textfelder
    // ACHTUNG: Die Reihenfolge der Abfragen muß in SaveChangeStatus() und
    // HasChanged() exakt identisch sein !
    private void SaveChangeStatus() {
        int i;
        originale.clear();

        // Alle Text-Eingabefelder
        for (i = 0; i < jtf.length; i++) {
            // String strText = jtf[i].getText();
            originale.add(jtf[i].getText());
        }

        // Das Feld mit "Ärztliche Diagnose"
        originale.add(diagnose_txt.getText());
        // indiv. Leitsyptomatik
        originale.add(lsym_x_txt.getText());
        // Therapieziele
        originale.add(thZielBefund_txt.getText());

        // alle ComboBoxen
        for (i = 0; i < jcmb.length; i++) {
            originale.add((Integer) jcmb[i].getSelectedIndex());
        }

        // alle CheckBoxen
        for (i = 0; i < jcb.length; i++) {
            originale.add((Boolean) (jcb[i].isSelected()));
        }
    }

    // prüft, ob sich Einträge geändert haben
    // ACHTUNG: Die Reihenfolge der Abfragen muß in SaveChangeStatus() und
    // HasChanged() exakt identisch sein !
    public Boolean HasChanged() {
        int i, idx = 0;

        // Alle Text-Eingabefelder
        for (i = 0; i < jtf.length; i++) {
            if (!jtf[i].getText()
                       .equals(originale.get(idx++)))
                return true;
        }

        // Das Feld mit "Ärztliche Diagnose"
        if (!diagnose_txt.getText()
                .equals(originale.get(idx++)))
            return true;
        // indiv. Leitsyptomatik
        if (!lsym_x_txt.getText()
                .equals(originale.get(idx++)))
            return true;
        // Therapieziele
        if (!thZielBefund_txt.getText()
                .equals(originale.get(idx++)))
            return true;

        // alle ComboBoxen
        for (i = 0; i < jcmb.length; i++) {
            if (jcmb[i].getSelectedIndex() != (Integer) originale.get(idx++))
                return true;
        }

        // alle CheckBoxen
        for (i = 0; i < jcb.length; i++) {
            if (jcb[i].isSelected() != (Boolean) originale.get(idx++))
                return true;
        }

        return false;
    }

    // Abfrage wenn sich Einträge geändert haben: fragt nach, ob wirklich
    // ungesichert abgebrochen werden soll !
    public int askForCancelUsaved() {
        String[] strOptions = { "ja", "nein" }; // Defaultwert auf "nein" gesetzt !
        return JOptionPane.showOptionDialog(null,
                "Es wurden Rezept-Angaben geändert!\nWollen sie die Änderung(en) wirklich verwerfen?",
                "Angaben wurden geändert", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, strOptions,
                strOptions[1]);
    }

    private JPanel getLeitSymPanel() {
        FormLayout lay = new FormLayout(
             //       LS        A        B        C         X
                "2dlu, p, 4dlu, p, 2dlu, p, 2dlu, p, 13dlu, p", "p");
        CellConstraints cc = new CellConstraints();
        PanelBuilder jpan = new PanelBuilder(lay);
        //PanelBuilder jpan = new PanelBuilder(lay, new FormDebugPanel()); // debug mode
        jpan.getPanel()
            .setOpaque(false);

        jcb[cLeitSymptA] = new JRtaCheckBox("a");
        jcb[cLeitSymptA].setOpaque(false);
        jcb[cLeitSymptA].setActionCommand("leitSymA");
        jpan.addLabel("Leitsymptomatik", cc.xy(2,1));
        jpan.add(jcb[cLeitSymptA], cc.xy(4,1));

        jcb[cLeitSymptB] = new JRtaCheckBox("b");
        jcb[cLeitSymptB].setOpaque(false);
        jcb[cLeitSymptB].setActionCommand("leitSymB");
        jpan.add(jcb[cLeitSymptB], cc.xy(6,1));

        jcb[cLeitSymptC] = new JRtaCheckBox("c");
        jcb[cLeitSymptC].setOpaque(false);
        jcb[cLeitSymptC].setActionCommand("leitSymC");
        jpan.add(jcb[cLeitSymptC], cc.xy(8,1));

        jcb[cLeitSymptX] = new JRtaCheckBox("indiv.");
        jcb[cLeitSymptX].setOpaque(false);
        jcb[cLeitSymptX].setActionCommand("leitSymX");
        jpan.add(jcb[cLeitSymptX], cc.xy(10,1));
        
        return jpan.getPanel();
    }

    /**
     * @return
     */
    private JScrollPane getDatenPanel() { // 1 2 3 4 5 6 7 8
        FormLayout lay = new FormLayout("right:max(80dlu;p), 4dlu, 70dlu, 5dlu, right:max(60dlu;p), 4dlu, 70dlu",
              // RK   2.  sp   4.    KT  6   RD    8   sp   10  I1   12  I2   14  DG   16  iL       19  sp   21  H1   23  H2   25  H3
                "p, 10dlu, p, 5dlu,  p, 2dlu, p, 10dlu, p, 5dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, p, 10dlu, p, 5dlu, p, 2dlu, p, 2dlu, p, "
                        +
                       // 27  eH    29  sp   31  TB   33  HB   35  BF   37  DB   39   TZ      42  sp   44  FC   46  VA   48  av   50    
                        "7dlu, p, 10dlu, p, 5dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, p, 10dlu, p, 5dlu, p, 2dlu, p, 2dlu, p, 2dlu");

        CellConstraints cc = new CellConstraints();
        PanelBuilder jpan = new PanelBuilder(lay);
        //PanelBuilder jpan = new PanelBuilder(lay, new FormDebugPanel()); // debug mode
        jpan.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        jpan.getPanel()
            .setOpaque(false);
        JScrollPane jscr = null;

        try {
            jtf[cKTRAEG] = new JRtaTextField("NIX", false); // kasse/kostenträger
            jtf[cARZT] = new JRtaTextField("NIX", false); // arzt
            jtf[cREZDAT] = new JRtaTextField("DATUM", true); // rezeptdatum
            jtf[cBEGINDAT] = new JRtaTextField("DATUM", true); // spätester beginn
            jtf[cANZ1] = new JRtaTextField("ZAHLEN", true); // Anzahl 1
            jtf[cANZ2] = new JRtaTextField("ZAHLEN", true); // Anzahl 2
            jtf[cANZ3] = new JRtaTextField("ZAHLEN", true); // Anzahl 3
            jtf[cANZ4] = new JRtaTextField("ZAHLEN", true); // Anzahl 4
            jtf[cFREQ] = new JRtaTextField("GROSS", true); // Frequenz
            jtf[cDAUER] = new JRtaTextField("ZAHLEN", true); // Dauer
            jtf[cANGEL] = new JRtaTextField("GROSS", true); // angelegt von
            jtf[cKASID] = new JRtaTextField("GROSS", false); // kassenid
            jtf[cARZTID] = new JRtaTextField("GROSS", false); // arztid
            jtf[cPREISGR] = new JRtaTextField("GROSS", false); // preisgruppe
            jtf[cHEIMBEW] = new JRtaTextField("GROSS", false); // heimbewohner
            jtf[cBEFREIT] = new JRtaTextField("GROSS", false); // befreit
            jtf[cPOS1] = new JRtaTextField("", false); // POS1
            jtf[cPOS2] = new JRtaTextField("", false); // POS2
            jtf[cPOS3] = new JRtaTextField("", false); // POS3
            jtf[cPOS4] = new JRtaTextField("", false); // POS4
            jtf[cPREIS1] = new JRtaTextField("", false); // PREIS1
            jtf[cPREIS2] = new JRtaTextField("", false); // PREIS2
            jtf[cPREIS3] = new JRtaTextField("", false); // PREIS3
            jtf[cPREIS4] = new JRtaTextField("", false); // PREIS4
            jtf[cANLAGDAT] = new JRtaTextField("DATUM", false); // ANLAGEDATUM
            jtf[cANZKM] = new JRtaTextField("", false); // KILOMETER
            jtf[cPATID] = new JRtaTextField("", false); // id von Patient
            jtf[cPATINT] = new JRtaTextField("", false); // pat_intern von Patient
            jtf[cZZSTAT] = new JRtaTextField("", false); // zzstatus
            jtf[cHEIMBEWPATSTAM] = new JRtaTextField("", false); // Heimbewohner aus PatStamm
            jtf[cICD10] = new JRtaTextField("GROSS", false); // 1. ICD10-Code
            jtf[cICD10_2] = new JRtaTextField("GROSS", false); // 2. ICD10-Code
            jtf[cAKUTDATUM] = new JRtaTextField("DATUM", true); // akutDatum
            jcmb[cRKLASSE] = new JRtaComboBox();
            strRezepklassenAktiv = diszis.getActiveRK();
            jcmb[cRKLASSE] = diszis.getComboBoxActiveRK();

            if (SystemConfig.AngelegtVonUser) {
                jtf[cANGEL].setText(Reha.aktUser);
                jtf[cANGEL].setEditable(false);
            }

            int rowCnt = 0;
            /********************/
            jpan.addLabel("Rezeptklasse auswählen", cc.xy(1, ++rowCnt));    // 1
            jpan.add(jcmb[cRKLASSE], cc.xyw(3, rowCnt++, 5));
            jcmb[cRKLASSE].setActionCommand("rezeptklasse");
            jcmb[cRKLASSE].addActionListener(this);
            allowShortCut((Component) jcmb[cRKLASSE], "RezeptClass");

            if (myRezept.isEmpty()) {
                jcmb[cRKLASSE].setSelectedItem(SystemConfig.initRezeptKlasse);                    
            } else {
                String rezClassInVO = myRezept.getRezClass();
                for (int i = 0; i < strRezepklassenAktiv.length; i++) {
                    if (strRezepklassenAktiv[i].equals(rezClassInVO)) {
                        jcmb[cRKLASSE].setSelectedIndex(i);
                    }
                }
            }
            if (!this.neu) {
                jcmb[cRKLASSE].setEnabled(false);
            }

            /********************/
            jpan.addSeparator("Rezeptkopf", cc.xyw(1, ++rowCnt, 7));    // 3

            /********************/
            rowCnt++;
            kassenLab = new JLabel("Kostenträger");
            kassenLab.setIcon(SystemConfig.hmSysIcons.get("kleinehilfe"));
            kassenLab.setHorizontalTextPosition(JLabel.LEFT);
            kassenLab.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent ev) {
                    if (!Rechte.hatRecht(Rechte.Rezept_editvoll, false)) {
                        return;
                    }
                    if (jtf[cKTRAEG].getText()
                                    .trim()
                                    .startsWith("?")) {
                        jtf[cKTRAEG].requestFocusInWindow();
                    } else {
                        jtf[cKTRAEG].setText("?" + jtf[cKTRAEG].getText()
                                                               .trim());
                        jtf[cKTRAEG].requestFocusInWindow();
                    }
                    String[] suchkrit = new String[] { jtf[cKTRAEG].getText()
                                                                   .replace("?", ""),
                            jtf[cKASID].getText() };
                    jtf[cKTRAEG].setText(String.valueOf(suchkrit[0]));
                    kassenAuswahl(suchkrit);
                }
            });

            jtf[cKTRAEG].setName("ktraeger");
            jtf[cKTRAEG].addKeyListener(this);
            allowShortCut((Component) jtf[cKTRAEG], "ktraeger");
            jpan.add(kassenLab, cc.xy(1, ++rowCnt));    // 5
            jpan.add(jtf[cKTRAEG], cc.xy(3, rowCnt));

            arztLab = new JLabel("verordn. Arzt");
            arztLab.setIcon(SystemConfig.hmSysIcons.get("kleinehilfe"));
            arztLab.setHorizontalTextPosition(JLabel.LEFT);
            arztLab.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent ev) {
                    if (!Rechte.hatRecht(Rechte.Rezept_editvoll, false)) {
                        return;
                    }
                    if (jtf[cARZT].getText()
                                  .trim()
                                  .startsWith("?")) {
                        jtf[cARZT].requestFocusInWindow();
                    } else {
                        jtf[cARZT].setText("?" + jtf[cARZT].getText()
                                                           .trim());
                        jtf[cARZT].requestFocusInWindow();
                    }
                    String[] suchkrit = new String[] { jtf[cARZT].getText()
                                                                 .replace("?", ""),
                            jtf[cARZTID].getText() };
                    jtf[cARZT].setText(String.valueOf(suchkrit[0]));
                    arztAuswahl(suchkrit);
                }
            });

            jtf[cARZT].setName("arzt");
            jtf[cARZT].addKeyListener(this);
            jpan.add(arztLab, cc.xy(5, rowCnt));
            jpan.add(jtf[cARZT], cc.xy(7, rowCnt++));

            /********************/
            jtf[cREZDAT].setName("rez_datum");
            allowShortCut((Component) jtf[cREZDAT], "rez_datum");
            jpan.addLabel("Rezeptdatum", cc.xy(1, ++rowCnt));   // 7
            eingabeRezDate = jpan.add(jtf[cREZDAT], cc.xy(3, rowCnt));

            allowShortCut((Component) jtf[cBEGINDAT], "lastdate");
            jpan.addLabel("spätester Beh.Beginn", cc.xy(5, rowCnt));
            jpan.add(jtf[cBEGINDAT], cc.xy(7, rowCnt++));

            /********************/
            jpan.addSeparator("Behandlungsrelevante Diagnosen", cc.xyw(1, ++rowCnt, 7));    // 9

            /********************/
            rowCnt++;
            jpan.addLabel("1. ICD-10-Code", cc.xy(1, ++rowCnt));    // 11
            allowShortCut((Component) jtf[cICD10], "icd10");
            eingabeICD = jpan.add(jtf[cICD10], cc.xy(3, rowCnt));

            diagnose_txt = new JTextArea();
            diagnose_txt.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.5f)));
            diagnose_txt.setFont(new Font("Courier", Font.PLAIN, 11));
            diagnose_txt.setLineWrap(true);
            diagnose_txt.setName("notitzen");
            diagnose_txt.setWrapStyleWord(true);
            diagnose_txt.setEditable(true);
            diagnose_txt.setBackground(Color.WHITE);
            diagnose_txt.setForeground(Color.RED);
            diagnose_txt.setToolTipText("Diagnose");
            eingabeDiag = diagnose_txt;
            JScrollPane spanD = JCompTools.getTransparentScrollPane(diagnose_txt);
            spanD.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.25f)));
            jpan.add(spanD, cc.xywh(5, rowCnt++, 3, 3));

            /********************/
            jpan.addLabel("2. ICD-10-Code", cc.xy(1, ++rowCnt));    // 13
            allowShortCut((Component) jtf[cICD10_2], "icd10_2");
            jpan.add(jtf[cICD10_2], cc.xy(3, rowCnt++));

            /********************/
            jpan.addLabel("Diagnosegruppe", cc.xy(1, ++rowCnt));    // 15
            jcmb[cINDI] = new JRtaComboBox();
            jcmb[cINDI].setActionCommand("selDiagGrp");
            allowShortCut((Component)jcmb[cINDI],"Diagnosegruppe");
            jpan.add(jcmb[cINDI], cc.xy(3, rowCnt));

            klassenReady = true;
            this.fuelleIndis((String) jcmb[cRKLASSE].getSelectedItem());

            jpan.add(getLeitSymPanel(), cc.xyw(5, rowCnt++, 3));

            /********************/
            jpan.addLabel("Freitext individuelle", cc.xy(1, ++rowCnt));    // 17
            lsym_x_txt = new JTextArea();
            lsym_x_txt.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.5f)));
            lsym_x_txt.setFont(new Font("Courier", Font.PLAIN, 11));
            lsym_x_txt.setLineWrap(true);
            lsym_x_txt.setName("leitsym_x_txt");
            lsym_x_txt.setWrapStyleWord(true);
            lsym_x_txt.setEditable(true);
            lsym_x_txt.setBackground(Color.WHITE);
            lsym_x_txt.setForeground(Color.RED);
            JScrollPane spanLS = JCompTools.getTransparentScrollPane(lsym_x_txt);
            spanLS.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.25f)));
            jpan.add(spanLS, cc.xywh(3, rowCnt++, 5, 2));   // Freitext indiv. Leitsymptomatik
            jpan.addLabel("Leitsymptomatik", cc.xy(1, rowCnt++));    // 18
            
            /********************/
            jpan.addSeparator("Verordnete Heilmittel", cc.xyw(1, ++rowCnt, 7));    // 20

            /********************/
            rowCnt++;
            jtf[cANZ1].setName("anzahl1");
            jtf[cANZ1].addFocusListener(this);
            jtf[cANZ1].addKeyListener(this);
            jpan.addLabel("Heilmittel 1 / Anzahl", cc.xy(1, ++rowCnt)); // 22
            jcmb[cLEIST1] = new JRtaComboBox();
            jcmb[cLEIST1].setActionCommand("leistung1");
            jcmb[cLEIST1].addActionListener(this);
            allowShortCut((Component) jcmb[cLEIST1], "leistung1");
            jpan.add(jcmb[cLEIST1], cc.xyw(3, rowCnt, 3));
            eingabeVerordn1 = jpan.add(jtf[cANZ1], cc.xy(7, rowCnt++));

            jpan.addLabel("Heilmittel 2 / Anzahl", cc.xy(1, ++rowCnt)); // 24
            jtf[cANZ2].addKeyListener(this);
            jcmb[cLEIST2] = new JRtaComboBox();
            jcmb[cLEIST2].setActionCommand("leistung2");
            jcmb[cLEIST2].addActionListener(this);
            allowShortCut((Component) jcmb[cLEIST2], "leistung2");
            jpan.add(jcmb[cLEIST2], cc.xyw(3, rowCnt, 3));
            jpan.add(jtf[cANZ2], cc.xy(7, rowCnt++));

            jpan.addLabel("Heilmittel 3 / Anzahl", cc.xy(1, ++rowCnt)); // 26
            jtf[cANZ3].addKeyListener(this);
            jcmb[cLEIST3] = new JRtaComboBox();
            jcmb[cLEIST3].setActionCommand("leistung3");
            jcmb[cLEIST3].addActionListener(this);
            allowShortCut((Component) jcmb[cLEIST3], "leistung3");
            jpan.add(jcmb[cLEIST3], cc.xyw(3, rowCnt, 3));
            jpan.add(jtf[cANZ3], cc.xy(7, rowCnt++));

            jpan.addLabel("erg. Heilmittel / Anzahl", cc.xy(1, ++rowCnt)); // 28
            jtf[cANZ4].addKeyListener(this);
            jcmb[cLEIST4] = new JRtaComboBox();
            jcmb[cLEIST4].setActionCommand("leistung4");
            jcmb[cLEIST4].setName("leistung4");
            jcmb[cLEIST4].addActionListener(this);
            jpan.add(jcmb[cLEIST4], cc.xyw(3, rowCnt, 3));
            jpan.add(jtf[cANZ4], cc.xy(7, rowCnt++));

            /********************/
            jpan.addSeparator("ergänzende Angaben", cc.xyw(1, ++rowCnt, 7));    // 30

            /********************/
            rowCnt++;
            jcb[cTBANGEF] = new JRtaCheckBox("angefordert");
            jcb[cTBANGEF].setOpaque(false);
            jpan.addLabel("Therapiebericht", cc.xy(1, ++rowCnt));   // 32
            jcb[cTBANGEF].addKeyListener(this);
            jpan.add(jcb[cTBANGEF], cc.xy(3, rowCnt++));
            
            /********************/
            jcb[cHAUSB] = new JRtaCheckBox("Ja / Nein");
            jcb[cHAUSB].setOpaque(false);
            jcb[cHAUSB].setActionCommand("Hausbesuche");
            jcb[cHAUSB].addActionListener(this);
            allowShortCut((Component) jcb[cHAUSB], "hbCheck");
            jpan.addLabel("Hausbesuch", cc.xy(1, ++rowCnt));    //34
            jpan.add(jcb[cHAUSB], cc.xy(3, rowCnt));

            jcb[cVOLLHB] = new JRtaCheckBox("abrechnen");
            jcb[cVOLLHB].setOpaque(false);
            jcb[cVOLLHB].setToolTipText("Nur aktiv wenn Patient Heimbewohner und Hausbesuch angekreuzt");
            jpan.addLabel("volle HB-Gebühr", cc.xy(5, rowCnt));
            if (neu) {
                jcb[cVOLLHB].setEnabled(false);
                jcb[cVOLLHB].setSelected(false);
            } else {
                if (Reha.instance.patpanel.patDaten.get(44)
                                                   .equals("T")) {
                    // Wenn Heimbewohner
                    if (myRezept.getHausbesuch()) {
                        jcb[cVOLLHB].setEnabled(true);
                        jcb[cVOLLHB].setSelected((myRezept.getHbVoll() ? true : false));
                    } else {
                        jcb[cVOLLHB].setEnabled(false);
                        jcb[cVOLLHB].setSelected(false);
                    }
                } else {
                    // Wenn kein(!!) Heimbewohner
                    if (myRezept.getHausbesuch()) {
                        jcb[cVOLLHB].setEnabled(false);
                        jcb[cVOLLHB].setSelected(true);
                    } else {
                        jcb[cVOLLHB].setEnabled(false);
                        jcb[cVOLLHB].setSelected(false);
                    }
                }
            }
            allowShortCut((Component) jcb[cVOLLHB], "hbVollCheck");
            jpan.add(jcb[cVOLLHB], cc.xy(7, rowCnt++));

            /********************/
            jtf[cFREQ].addKeyListener(this);
            jpan.addLabel("Behandlungsfrequenz", cc.xy(1, ++rowCnt));   // 36
            eingabeBehFrequ = jpan.add(jtf[cFREQ], cc.xy(3, rowCnt));

            jpan.addLabel("Dauer der Behandl. in Min.", cc.xy(5, rowCnt));
            jtf[cDAUER].addKeyListener(this);
            jpan.add(jtf[cDAUER], cc.xy(7, rowCnt++));

            jcb[cdringBehBedarf] = new JRtaCheckBox("Dringlicher Behandlungsbedarf (innerhalb von 14 Tagen)");
            jcb[cdringBehBedarf].setOpaque(false);
            jcb[cdringBehBedarf].addKeyListener(this);
            jpan.add(jcb[cdringBehBedarf], cc.xyw(3, ++rowCnt, 5));   // 38

            rowCnt++;
            /********************/
            jpan.addLabel("Therapieziele / Befunde", cc.xy(1, ++rowCnt));    // 40
            thZielBefund_txt = new JTextArea();
            thZielBefund_txt.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.5f)));
            thZielBefund_txt.setFont(new Font("Courier", Font.PLAIN, 11));
            thZielBefund_txt.setLineWrap(true);
            thZielBefund_txt.setName("leitsym_x_txt");
            thZielBefund_txt.setWrapStyleWord(true);
            thZielBefund_txt.setEditable(true);
            thZielBefund_txt.setBackground(Color.WHITE);
            thZielBefund_txt.setForeground(Color.RED);
            JScrollPane spanTZ = JCompTools.getTransparentScrollPane(thZielBefund_txt);
            spanTZ.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.25f)));
            jpan.add(spanTZ, cc.xywh(3, rowCnt++, 5, 2));
            jpan.addLabel("Hinweise", cc.xy(1, rowCnt++));    // 41

            /********************/
            jpan.addSeparator("interne Daten", cc.xyw(1, ++rowCnt, 7)); // 43

            /********************/
            rowCnt++;
            jpan.addLabel("FarbCode im TK", cc.xy(1, ++rowCnt));    // 45
            jcmb[cFARBCOD] = new JRtaComboBox();
            jcmb[cFARBCOD].addKeyListener(this);
            macheFarbcodes();

            jpan.add(jcmb[cFARBCOD], cc.xy(3, rowCnt));

            jpan.addLabel("Barcode-Format", cc.xy(5, rowCnt));
            jcmb[cBARCOD] = new JRtaComboBox(SystemConfig.rezBarCodName);
            jcmb[cBARCOD].addKeyListener(this);
            jpan.add(jcmb[cBARCOD], cc.xy(7, rowCnt++));

            /********************/
            jcmb[cVERORDART] = new JRtaComboBox(
                    new VerordnungsArten().getHmr2020() );
            jcmb[cVERORDART].setActionCommand("verordnungsart");
            jcmb[cVERORDART].addActionListener(voArtActionListener);
            jpan.addLabel("Verordnungsart", cc.xy(1, ++rowCnt));   // 47
            jpan.add(jcmb[cVERORDART], cc.xy(3, rowCnt));

            jtf[cAKUTDATUM].setName("akut_datum");
            jpan.addLabel("Datum Akutereignis", cc.xy(5, rowCnt));
            jpan.add(jtf[cAKUTDATUM], cc.xy(7, rowCnt++));

            /********************/
            jcb[cHygienePausch] = new JRtaCheckBox("abrechnen");
            jcb[cHygienePausch].setOpaque(false);
            jcb[cHygienePausch].setToolTipText("nur zulässig bei Abrechnung zwischen 05.05.2020 und 31.03.2021");
            jpan.addLabel("Hygiene-Mehraufwand", cc.xy(1, ++rowCnt));  // 49
            if (neu) {
                jcb[cHygienePausch].setSelected(false);
            } else {
                boolean dummy = myRezept.getUseHygPausch();
                jcb[cHygienePausch].setSelected((myRezept.getUseHygPausch() ? true : false));
            }
            allowShortCut((Component) jcb[cHygienePausch], "hygPausch");
            jpan.add(jcb[cHygienePausch], cc.xy(3, rowCnt));

            jpan.addLabel("Angelegt von", cc.xy(5, rowCnt));
            jtf[cANGEL].addKeyListener(this);
            jpan.add(jtf[cANGEL], cc.xy(7, rowCnt++));

            jscr = JCompTools.getTransparentScrollPane(jpan.getPanel());
            jscr.getVerticalScrollBar()
                .setUnitIncrement(15);

            if (this.neu) {
                if (myRezept.isEmpty()) {
                    initRezeptNeu(myRezept); // McM:hier myRezept mit Pat-Daten, PG, ... initialisieren
                    this.holePreisGruppe(Reha.instance.patpanel.patDaten.get(68)
                                                                        .trim()); // setzt jtf[cPREISGR] u.
                                                                                  // this.preisgruppe
                    this.ladePreisliste(jcmb[cRKLASSE].getSelectedItem()
                                                      .toString()
                                                      .trim(),
                            preisgruppen[getPgIndex()]); // fuellt jcmb[cLEIST1..4] u.
                                                                              // jcmb[cBARCOD]
                } else { // myRezept enthaelt Daten
                    try {
                        String[] xartdbeh = new String[] { myRezept.getHMkurz(1), myRezept.getHMkurz(2),
                                myRezept.getHMkurz(3), myRezept.getHMkurz(4) };
                        initRezeptKopie(myRezept);
                        this.holePreisGruppe(myRezept.getKtraeger());
                        this.ladePreisliste(jcmb[cRKLASSE].getSelectedItem()
                                                          .toString()
                                                          .trim(),
                                preisgruppen[getPgIndex()]);
                        for (int i = 0; i < 4; i++) {
                            if (xartdbeh[i].equals("")) {
                                jcmb[cLEIST1 + i].setSelectedIndex(0);
                            } else {
                                jcmb[cLEIST1 + i].setSelectedVecIndex(1, xartdbeh[i]);
                            }
                        }
                        jcmb[cINDI].setSelectedItem(myRezept.getIndiSchluessel());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            } else {
                this.holePreisGruppe(myRezept.getKtraeger());
                this.ladePreisliste(jcmb[cRKLASSE].getSelectedItem()
                                                  .toString()
                                                  .trim(),
                        preisgruppen[getPgIndex()]);
            }
            verordnenderArzt.init(myRezept.getArztId());
            copyVecToForm();

            jscr.validate();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fehler in der Erstellung des Rezeptfensters\n" + ex.getMessage());
        }
        SaveChangeStatus();

        return jscr;
    }

    private void allowShortCut(Component thisComponent, String name) {
        thisComponent.setName(name);
        thisComponent.addKeyListener(this);
        thisComponent.addFocusListener(this);
    }

    private int getselectedRow() {
        return Reha.instance.patpanel.aktRezept.tabaktrez.getSelectedRow();
    }

    /**
     * RezeptDatum in Tabelle 'aktuelle Rezepte' uebernehmen
     *
     * @param datum
     */
    private void setRezDatInTable(String datum) {
        int row = getselectedRow();
        if (row >= 0) {
            AktuelleRezepte.tabaktrez.getModel()
                                     .setValueAt(datum, row, 2);
        }
    }

    /**
     * 'spaetester Beginn' in Tabelle uebernehmen
     *
     * @param datum
     */
    private void setLastDatInTable(String datum) {
        int row = getselectedRow();
        if (row >= 0) {
            AktuelleRezepte.tabaktrez.getModel()
                                     .setValueAt(datum, row, 4);
        }
    }

    private String chkLastBeginDat(String rezDat, String lastDat, String preisGroup, String aktDiszi) {
        if (lastDat.equals(".  .")) { // spaetester Beginn nicht angegeben? -> aus Preisgruppe holen
            // Preisgruppe holen
            int pg = Integer.parseInt(preisGroup) - 1;
            // Frist zwischen Rezeptdatum und erster Behandlung
            int frist = (Integer) ((Vector<?>) SystemPreislisten.hmFristen.get(aktDiszi)
                                                                          .get(0)).get(pg);
            // Kalendertage
            if ((Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(aktDiszi)
                                                                  .get(1)).get(pg)) {
                lastDat = DatFunk.sDatPlusTage(rezDat, frist);
            } else { // Werktage
                boolean mitsamstag = (Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(aktDiszi)
                                                                                       .get(4)).get(pg);
                lastDat = HMRCheck.hmrLetztesDatum(rezDat,
                        (Integer) ((Vector<?>) SystemPreislisten.hmFristen.get(aktDiszi)
                                                                          .get(0)).get(pg),
                        mitsamstag);
            }
        }
        return lastDat;
    }

    public int leistungTesten(int combo, int veczahl) {
        int retwert = 0;
        if (veczahl == -1 || veczahl == 0) {
            return retwert;
        }
        if (preisvec == null) {
            return 0;
        }
        for (int i = 0; i < preisvec.size(); i++) {
            if (Integer.parseInt((String) ((Vector<?>) preisvec.get(i)).get(preisvec.get(i)
                                                                                    .size()
                    - 1)) == veczahl) {
                return i + 1;
            }
        }
        return retwert;
    }

    public RezNeuanlage2020 getInstance() {
        return this;
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

    private boolean chkIcdIsValid(String string) {
        if (string.length() > 0) {
            String suchenach = macheIcdString(string);
            if (SqlInfo.holeEinzelFeld(
                    "select id from icd10 where schluessel1 like '" + suchenach + "%' LIMIT 1")
                       .equals("")) {
                int frage = JOptionPane.showConfirmDialog(null,
                        "<html><b>Der eingetragene ICD-10-Code ist falsch: <font color='#ff0000'>" + string
                                + "</font></b><br>" + "Wollen Sie jetzt das ICD-10-Tool starten?<br><br></html>",
                        "falscher ICD-10", JOptionPane.YES_NO_OPTION);
                if (frage == JOptionPane.YES_OPTION) {
                    new LadeProg(Path.Instance.getProghome()+"ICDSuche.jar"+" "+Reha.getAktIK());
                }
                return false;
            }            
        }
        return true;
    }

    private String chkIcdFormat(String string) {
        int posDot = string.indexOf(".");
        if ((string.length() > 3) && (posDot < 0)) {
            String tmp1 = string.substring(0, 3);
            String tmp2 = string.substring(3);
            return tmp1 + "." + tmp2;
        }
        return string;
    }

    private void setLeitSymCBox(int idx, boolean isEnabled) {
        jcb[idx].setEnabled(isEnabled);
        if (!isEnabled) {
            jcb[idx].setSelected(false);
        }
    }
    
    private void initLeitSymCBoxes() {
        Vector<String> lsymVec = new Vector<String>();
        Vector<Vector<String>> tmpVec = SqlInfo.holeFelder("select leitsyma,leitsymb,leitsymc,leitsymx from hmr_diagnosegruppe where diagnosegruppe ='"+jcmb[cINDI].getSelectedItem().toString()+"' LIMIT 1");
        if (tmpVec.size() > 0) {
            lsymVec = tmpVec.get(0);
        }
        for (int i = cLeitSymptA; i <= cLeitSymptX; i++) {
            if(lsymVec.size() > 0) {
                boolean cBoxIsActive = lsymVec.get(i-cLeitSymptA).equals("T");
                setLeitSymCBox (i, cBoxIsActive);
            }else if(lsymVec.size() == 0) { // Zahnarzt oder Keine DiagGr.
                setLeitSymCBox (i, false);
            }
        }
    }

    ActionListener cINDIActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if(cmd.equals("selDiagGrp")) {
                initLeitSymCBoxes();
            }
        }
    };

    ActionListener voArtActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(jcmb[cVERORDART].getSelectedIndex() == 1) {
                jtf[cAKUTDATUM].setEnabled(true);
            }else {
                jtf[cAKUTDATUM].setText("  .  .    ");
                jtf[cAKUTDATUM].setEnabled(false);
            }
        }
        
    };
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand()
             .equals("rezeptklasse") && klassenReady) {
            this.ladePreisliste(jcmb[cRKLASSE].getSelectedItem()
                                              .toString()
                                              .trim(),
                    preisgruppen[getPgIndex()]);
            this.fuelleIndis((String) jcmb[cRKLASSE].getSelectedItem());

            return;
        }
        /*********************/
        if (e.getActionCommand()
             .equals("speichern")) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        if (!anzahlTest()) {
                            return;
                        }
                        if (!komplettTest()) {
                            return;
                        }
                        if (getInstance().neu) {
                            if (!neuDateTest()) {
                                return;
                            }
                            copyFormToVec1stTime(myRezept);
                            myRezept.setNewRezNb(rezKlasse);
                            Reha.instance.patpanel.aktRezept.setzeRezeptNummerNeu(myRezept.getRezNb());
                        } else {
                            copyFormToVec(myRezept);
                        }
                        aufraeumen();
                        closeDialog();
                        // ?? automat. HMR-Check ??
                        myRezept.writeRez2DB();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            });
            return;
        }
        if (e.getActionCommand()
             .equals("abbrechen")) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    doAbbrechen();
                    return null;
                }
            }.execute();
            return;
        }
        if (e.getActionCommand()
             .equals("hmrcheck")) {
            System.out.println("Button hmrcheck");

            if (SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin)
                                                 .get(preisgruppen[getPgIndex()]) == 0) {
                this.hmrcheck.setEnabled(true);
                JOptionPane.showMessageDialog(null, "HMR-Check ist bei diesem Kostenträger nicht erforderlich");
                return;
            }
            doHmrCheck();
 
            return;
        }

        if (e.getActionCommand()
             .equals("Hausbesuche")) {
            if (jcb[cHAUSB].isSelected()) {
                // Hausbesuch gewählt
                if (Reha.instance.patpanel.patDaten.get(44)
                                                   .equals("T")) {
                    if (this.preisgruppe != 1 && (getPgIndex() <= 1)) {
                        jcb[cVOLLHB].setEnabled(true);
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            jcb[cHAUSB].requestFocusInWindow();
                        }
                    });
                } else {
                    jcb[cVOLLHB].setEnabled(false);
                    jcb[cVOLLHB].setSelected(true);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            jcb[cHAUSB].requestFocusInWindow();
                        }
                    });
                }
            } else {
                // Hausbesuch abgewählt
                jcb[cVOLLHB].setEnabled(false);
                jcb[cVOLLHB].setSelected(false);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jcb[cHAUSB].requestFocusInWindow();
                    }
                });
            }
            return;
        }

        /*********************/
        if (e.getActionCommand()
             .contains("leistung") && initReady) {
            int lang = e.getActionCommand()
                        .length();
            String test = (String) ((JRtaComboBox) e.getSource()).getSelectedItem();
            if (test == null) {
                return;
            }
            if (!test.equals("./.")) {
                String id = (String) ((JRtaComboBox) e.getSource()).getValue();
                Double preis = holePreisDouble(id, preisgruppe);
                if (preis <= 0.0) {
                    JOptionPane.showMessageDialog(null,
                            "Diese Position ist für die gewählte Preisgruppe ungültig\nBitte weisen Sie in der Preislisten-Bearbeitung der Position ein Kürzel zu");
                    ((JRtaComboBox) e.getSource()).setSelectedIndex(0);
                }
            }
            return;
        }
    }

    // prüft ob Heilmittel eingetragen worden sind
    private boolean anzahlTest() {
        int itest;
        int maxanzahl = 0, aktanzahl = 0;

        for (int i = 0; i < 4; i++) { // über alle 4 Leistungs- und Anzahl-Positionen rennen
            itest = jcmb[cLEIST1 + i].getSelectedIndex();
            if (itest > 0) {
                if (i < 3) {
                    try {
                        maxanzahl = maxanzahl + Integer.parseInt(jtf[cANZ1 + i].getText());
                    } catch (Exception ex) {
                    }
                } else {
                    try {
                        aktanzahl = Integer.parseInt(jtf[cANZ1 + i].getText());
                    } catch (Exception ex) {
                        aktanzahl = 0;
                    }
                    if (aktanzahl > maxanzahl) {
                        String cmd = "Sie haben mehr ergänzende Heilmittel als vorrangige eingegeben.\n"
                                + "Es sind maximal soviele möglich, wie die Summe der vorrangigen Heilmittel.";
                        JOptionPane.showMessageDialog(null, cmd);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /*
     * Test Rezeptdatum
     */
    private boolean neuDateTest() {
        long dattest = DatFunk.TageDifferenz(DatFunk.sHeute(), jtf[cREZDAT].getText()
                                                                           .trim());
        if ((dattest <= -364) || (dattest >= 364)) {
            int frage = JOptionPane.showConfirmDialog(null,
                    "<html><b>Das Rezeptdatum ist etwas kritisch....<br><br><font color='#ff0000'> " + "Rezeptdatum = "
                            + jtf[cREZDAT].getText()
                                          .trim()
                            + "</font></b><br>Das sind ab Heute " + Long.toString(dattest) + " Tage<br><br><br>"
                            + "Wollen Sie dieses Rezeptdatum tatsächlich abspeichern?",
                    "Bedenkliches Rezeptdatum", JOptionPane.YES_NO_OPTION);
            if (frage != JOptionPane.YES_OPTION) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jtf[cREZDAT].requestFocusInWindow();
                    }
                });
                return false;
            }
        }
        return true;
    }

    private void doHmrCheck() {
        int itest = 0;
        String indi = (String) jcmb[cINDI].getSelectedItem();
        if (indi.equals("") || indi.contains(noDiagGrpSelected)) {
            JOptionPane.showMessageDialog(null,
                    "<html><b>Keie Diagnosegruppe angegeben.<br>Die Angaben sind <font color='#ff0000'>nicht</font> gemäß den gültigen Heilmittelrichtlinien!</b></html>");
            return;
        }
        indi = indi.replace(" ", "");
        Vector<Integer> anzahlen = new Vector<Integer>();
        Vector<String> hmpositionen = new Vector<String>();

        for (int i = 0; i < 4; i++) {
            itest = jcmb[cLEIST1 + i].getSelectedIndex();
            if (itest > 0) {
                anzahlen.add(Integer.parseInt(jtf[cANZ1 + i].getText()));
                hmpositionen.add(preisvec.get(itest - 1)
                                         .get(2));
            }
        }

        if (jtf[cREZDAT].getText()
                        .trim()
                        .equals(".  .")) {
            JOptionPane.showMessageDialog(null, "Rezeptdatum nicht korrekt angegeben, HMR-Check nicht möglich");
            return;
        }
        if (hmpositionen.size() > 0) {
            String letztbeginn = jtf[cBEGINDAT].getText()
                                               .trim();
            if (letztbeginn.equals(".  .")) {
                // Preisgruppe holen
                int pg = Integer.parseInt(jtf[cPREISGR].getText()) - 1;
                // Frist zwischen Rezeptdatum und erster Behandlung
                int frist = (Integer) ((Vector<?>) SystemPreislisten.hmFristen.get(aktuelleDisziplin)
                                                                              .get(0)).get(pg);
                // Kalendertage
                if ((Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(aktuelleDisziplin)
                                                                      .get(1)).get(pg)) {
                    letztbeginn = DatFunk.sDatPlusTage(jtf[cREZDAT].getText()
                                                                   .trim(),
                            frist);
                } else { // Werktage
                    boolean mitsamstag = (Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(aktuelleDisziplin)
                                                                                           .get(4)).get(pg);
                    letztbeginn = HMRCheck.hmrLetztesDatum(jtf[cREZDAT].getText()
                                                                       .trim(),
                            (Integer) ((Vector<?>) SystemPreislisten.hmFristen.get(aktuelleDisziplin)
                                                                              .get(0)).get(pg),
                            mitsamstag);
                }
            }
            tmpRezept = new Rezeptvector();
            if (getInstance().neu) {
                initRezeptNeu(tmpRezept);
            } else {
                tmpRezept.setVec_rez(myRezept.getVec_rez());
            }
            copyFormToVec1stTime(tmpRezept);
            boolean checkok = new HMRCheck2020(tmpRezept, diszis.getCurrDisziFromActRK(), preisvec).check();
            if (checkok) {
                JOptionPane.showMessageDialog(null,
                        "<html><b>Das Rezept <font color='#ff0000'>entspricht</font> den geltenden Heilmittelrichtlinien</b></html>");
            } else {
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        jtf[cKTRAEG].requestFocusInWindow();
                        return null;
                    }
                }.execute();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Keine Behandlungspositionen angegeben, HMR-Check nicht möglich!!!");
        }

    }

    private boolean komplettTest() {
        if (jtf[cREZDAT].getText()
                        .trim()
                        .equals(".  .")) {
            JOptionPane.showMessageDialog(null,
                    "Ohne ein gültiges 'Rezeptdatum' kann ein Rezept nicht abgespeichert werden.");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jtf[cREZDAT].requestFocusInWindow();
                }
            });
            return false;
        }

        if (jtf[cKTRAEG].getText()
                        .trim()
                        .equals("")) {
            JOptionPane.showMessageDialog(null,
                    "Ohne die Angabe 'Kostenträger' kann ein Rezept nicht abgespeichert werden.");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jtf[cKTRAEG].requestFocusInWindow();
                }
            });
            return false;
        }
        if (jtf[cARZT].getText()
                      .trim()
                      .equals("")) {
            JOptionPane.showMessageDialog(null,
                    "Ohne die Angabe 'verordn. Arzt' kann ein Rezept nicht abgespeichert werden.");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jtf[cARZT].requestFocusInWindow();
                }
            });
            return false;
        }
        if (jtf[cDAUER].getText()
                       .trim()
                       .equals("")) {
            JOptionPane.showMessageDialog(null,
                    "Ohne die Angabe 'Behandlungsdauer' kann ein Rezept nicht abgespeichert werden.");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jtf[cDAUER].requestFocusInWindow();
                }
            });
            return false;
        }
        if (jtf[cANGEL].getText()
                       .trim()
                       .equals("")) {
            JOptionPane.showMessageDialog(null,
                    "Ohne die Angabe 'Angelegt von' kann ein Rezept nicht abgespeichert werden.");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jtf[cANGEL].requestFocusInWindow();
                }
            });
            return false;
        }
        if (SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin)
                                             .get(preisgruppen[getPgIndex()]) == 1) {
            if (jtf[cFREQ].getText()
                          .trim()
                          .equals("")) {
                JOptionPane.showMessageDialog(null,
                        "Ohne Angabe der 'Behandlungsfrequenz' kann ein GKV-Rezept nicht abgespeichert werden.");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jtf[cFREQ].requestFocusInWindow();
                    }
                });
                return false;
            }
          //*********************
            //Test nach Diagnosegruppe
            if(jcmb[cINDI].getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Ohne Angabe der 'Diagnosegruppe' kann ein GKV-Rezept nicht abgespeichert werden.");
                SwingUtilities.invokeLater(new Runnable(){
                       public  void run()
                       {
                            jcmb[cINDI].requestFocus();
                       }
                });
                return false;
            }
            // Leitsymptomatiktest
            if ((!jcb[this.cLeitSymptA].isSelected() && !jcb[this.cLeitSymptB].isSelected()
                    && !jcb[this.cLeitSymptC].isSelected() && !jcb[this.cLeitSymptX].isSelected())
                    && (!AktuelleRezepte.isDentist2020(jcmb[cINDI].getSelectedItem()
                                                              .toString()))) {
                JOptionPane.showMessageDialog(null,
                        "Achtung!\nNicht zulässige Rezeptanlage.\nEs wurde keine Leitsymptomatik angegeben.");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jcb[cLeitSymptA].requestFocus();
                    }
                });
                return false;
            }
            //ob dem Nachfolgenden nach den neuen HMR so ist muß erst noch ermittelt werden
            if(jcb[this.cLeitSymptX].isSelected() && (this.lsym_x_txt.getText().length() < 5) /*z.B. nur ein paar Zeilenumbrüche*/) {
                JOptionPane.showMessageDialog(null, "Achtung!\nNicht zulässige Rezeptanlage.\nEs wurde individuelle Leitsymptomatik angegeben\n"+
                            "aber kein Freitext eingetragen");
                SwingUtilities.invokeLater(new Runnable(){
                       public  void run() {
                            jcb[cLeitSymptX].requestFocus();
                       }
                });
                return false;
            }
            String tmpStr = jcmb[cINDI].getSelectedItem().toString();
            if(!AktuelleRezepte.isDentist2020(tmpStr)) {
                if(SqlInfo.holeFeld("select diagnosegruppe from hmr_diagnosegruppe where diagnosegruppe ='"+tmpStr+"' LIMIT 1" ).equals("")) {
                    JOptionPane.showMessageDialog(null, "Achtung!\nNicht zulässige Rezeptanlage.\nDiagnosegruppe konnte nicht ermittelt werden\nHMR-Check ist nicht möglich");
                    SwingUtilities.invokeLater(new Runnable(){
                           public  void run() {
                              jcmb[cINDI].requestFocus();
                           }
                    });
                    return false;
                }
            }
          //*********************
        }
        return true;
    }

    private void ladePreisliste(String typeOfVO, int preisgruppe) {
        try {
            String[] artdbeh = null;
            if (!this.neu && jcmb[cLEIST1].getItemCount() > 0) {
                artdbeh = new String[] { String.valueOf(jcmb[cLEIST1].getValueAt(1)),
                        String.valueOf(jcmb[cLEIST2].getValueAt(1)), String.valueOf(jcmb[cLEIST3].getValueAt(1)),
                        String.valueOf(jcmb[cLEIST4].getValueAt(1)) };
            }
            jcmb[cLEIST1].removeAllItems();
            jcmb[cLEIST2].removeAllItems();
            jcmb[cLEIST3].removeAllItems();
            jcmb[cLEIST4].removeAllItems();

            aktuelleDisziplin = diszis.getDisziKurzFromTypeOfVO(typeOfVO);
            rezKlasse = diszis.getRezClass(typeOfVO);

            preisvec = SystemPreislisten.hmPreise.get(aktuelleDisziplin)
                                                 .get(preisgruppe);


            if (artdbeh != null) {
                ladePreise(artdbeh);
            } else {
                ladePreise(null);
            }
            if (this.neu && SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin)
                                                             .get(preisgruppe) == 1) {
                if (aktuelleDisziplin.equals("Physio") || aktuelleDisziplin.equals("Massage")
                        || aktuelleDisziplin.equals("Ergo")) {
                    jcmb[cBARCOD].setSelectedItem("Muster 13/18");
                } else if (aktuelleDisziplin.equals("Logo")) {
                    jcmb[cBARCOD].setSelectedItem("Muster 14");
                } else if (aktuelleDisziplin.equals("Reha")) {
                    jcmb[cBARCOD].setSelectedItem("DIN A4 (REHA)");
                } else {
                    jcmb[cBARCOD].setSelectedItem("Muster 13/18");
                }
            } else if (this.neu && aktuelleDisziplin.equals("Reha")) {
                jcmb[cBARCOD].setSelectedItem("DIN A4 (REHA)");
            } else {
                if (this.neu) {
                    jcmb[cBARCOD].setSelectedItem("Muster 13/18");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void mkDiagGroups() {
        diagGroupsPhysio = new String[] { noDiagGrpSelected, "WS", "EX", "CS", "ZN", "PN", "AT", "GE", "LY", "SO1", "SO2", "SO3", "SO4", "SO5", 
                "CD1", "CD2", "ZNSZ", "CSZ", "LYZ1", "LYZ2", // Zahnarzt-VO
                noDiagGrpInVO };

        diagGroupsErgo = new String[] { noDiagGrpSelected, "SB1", "SB2", "SB3", "EN1", "EN2", "EN3", "PS1", "PS2", "PS3", "PS4", noDiagGrpInVO };

        diagGroupsLogo = new String[] { noDiagGrpSelected, "ST1", "ST2", "ST3", "ST4", "SP1", "SP2", "SP3", "SP4", "SP5", "SP6", "RE1", "RE2", "SF", "SC", 
                "SPZ", "SCZ", "OFZ",    // Zahnarzt-VO
                noDiagGrpInVO };
        
        diagGroupsPodo = new String[] { noDiagGrpSelected, "DF", "NF", "QF", noDiagGrpInVO };

    }
 
    /** Holt die passenden Inikationsschlüssel gemäß aktiver Disziplin**/       // <- stehen in 'aktuelleRezepte', werden aber nur 'hier' benutzt -> künftig lokal
    private void fuelleIndis(String typeOfVO) {                                 // weitere Definitionen in 'Historie' u. 'Dokumentation' - scheinbar unbenutzt
        try {
            jcmb[cINDI].removeActionListener(this);
            if (jcmb[cINDI].getItemCount() > 0) {
                jcmb[cINDI].removeAllItems();
            }
            String tmpItem = typeOfVO.toLowerCase();
            if (tmpItem.contains("reha") && (!tmpItem.startsWith("rehasport"))) {
                return;
            }
            int anz = 0;
            String[] indis = null;
            if (tmpItem.contains("physio") || tmpItem.contains("massage") || tmpItem.contains("rehasport")
                    || tmpItem.contains("funktions")) {
                anz = diagGroupsPhysio.length;
                indis = diagGroupsPhysio;
            } else if (tmpItem.contains("ergo")) {
                anz = diagGroupsErgo.length;
                indis = diagGroupsErgo;
            } else if (tmpItem.contains("logo")) {
                anz = diagGroupsLogo.length;
                indis = diagGroupsLogo;
            } else if (tmpItem.contains("podo")) {
                anz = diagGroupsPodo.length;
                indis = diagGroupsPodo;
            }
            for (int i = 0; i < anz; i++) {
                jcmb[cINDI].addItem(indis[i]);
            }
            jcmb[cINDI].addActionListener(cINDIActionListener);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fehler beim füllen der Inikat.schlüssel\n" + ex.getMessage());
        }

        return;
    }

    public void ladePreise(String[] artdbeh) {
        try {
            if (preisvec.size() <= 0) {
                JOptionPane.showMessageDialog(null,
                        "In der erforderlichen Preisliste sind noch keine Preise vorhanden!\nRezept kann nicht angelegt werden");
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "In der erforderlichen Preisliste sind noch keine Preise vorhanden!\nRezept kann nicht angelegt werden");
            return;
        }
        jcmb[cLEIST1].setDataVectorWithStartElement(preisvec, 0, 9, "./.");
        jcmb[cLEIST2].setDataVectorWithStartElement(preisvec, 0, 9, "./.");
        jcmb[cLEIST3].setDataVectorWithStartElement(preisvec, 0, 9, "./.");
        jcmb[cLEIST4].setDataVectorWithStartElement(preisvec, 0, 9, "./.");
        if (artdbeh != null) {
            for (int i = 0; i < 4; i++) {
                if (artdbeh[i].equals("")) {
                    jcmb[cLEIST1 + i].setSelectedIndex(0);
                } else {
                    jcmb[cLEIST1 + i].setSelectedVecIndex(1, artdbeh[i]);
                }
            }
        }
        return;
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        if (arg0.getKeyChar() == '?' && ((JComponent) arg0.getSource()).getName()
                                                                       .equals("arzt")) {
            String[] suchkrit = new String[] { jtf[cARZT].getText()
                                                         .replace("?", ""),
                    jtf[cARZTID].getText() };
            jtf[cARZT].setText(String.valueOf(suchkrit[0]));
            arztAuswahl(suchkrit);
        }
        if (arg0.getKeyChar() == '?' && ((JComponent) arg0.getSource()).getName()
                                                                       .equals("ktraeger")) {
            String[] suchkrit = new String[] { jtf[cKTRAEG].getText()
                                                           .replace("?", ""),
                    jtf[cKASID].getText() };
            jtf[cKTRAEG].setText(suchkrit[0]);
            kassenAuswahl(suchkrit);
        }
        if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
            doAbbrechen();
        }
        if (arg0.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrlIsPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        if (arg0.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrlIsPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    @Override
    public void focusGained(FocusEvent arg0) {
    }

    @Override
    public void focusLost(FocusEvent arg0) {
        if (((JComponent) arg0.getSource()).getName() != null) {
            String componentName = ((JComponent) arg0.getSource()).getName();
            boolean jumpForward = arg0.toString()
                                      .contains("cause=TRAVERSAL_FORWARD");
            if (componentName.equals("RezeptClass") || componentName.equals("ktraeger")) {
                if (ctrlIsPressed && jumpForward) {
                    eingabeRezDate.requestFocusInWindow();
                }
                return;
            }
            if (componentName.equals("rez_datum") || componentName.equals("lastdate")) {
                if (ctrlIsPressed && jumpForward) {
                    eingabeVerordnArt.requestFocusInWindow();
                }
                return;
            }
            if (componentName.equals("selArtDerVerordn") || componentName.equals("adrCheck")
                    || componentName.equals("hbCheck") || componentName.equals("hbVollCheck")) {
                if (ctrlIsPressed && jumpForward) {
                    eingabeVerordn1.requestFocusInWindow();
                }
                return;
            }
            if (componentName.equals("anzahl1") && neu) {
                String text = jtf[cANZ1].getText();
                jtf[cANZ2].setText(text);
                jtf[cANZ3].setText(text);
                jtf[cANZ4].setText(text);
                return;
            }
            if (componentName.contains("leistung") && jumpForward) {
                // ComboBox mit [TAB] verlassen ...
                String test = (String) ((JRtaComboBox) arg0.getSource()).getSelectedItem();
                if (test.equals("./.")) { // ... + kein Heilmittel ausgewählt -> zur Behandlungsfrequenz springen
                    eingabeBehFrequ.requestFocusInWindow();
                } else if (ctrlIsPressed) { // verlassen mit [STRG][TAB] bzw. [STRG][ENTER] springt auch zur
                                         // Behandlungsfrequenz
                        eingabeBehFrequ.requestFocusInWindow();
                }
                return;
            }
            if (componentName.equals("Diagnosegruppe") && jumpForward) {
                if (ctrlIsPressed) {
                    eingabeICD.requestFocusInWindow();
                }
                return;
            }
            if (componentName.equals("icd10")) {
                String text = jtf[cICD10].getText();
                text = chkIcdFormat(text);
                if (!chkIcdIsValid(text)) {  // Prüfung auf Gültigkeit
                    return;
                }
                jtf[cICD10].setText(text);
                if (ctrlIsPressed & jumpForward) {
                    eingabeDiag.requestFocusInWindow();
                }
                return; // chkIcdIsValid(String string)
            }
            if (componentName.equals("icd10_2")) {
                String text = jtf[cICD10_2].getText();
                text = chkIcdFormat(text);
                if (!chkIcdIsValid(text)) {  // Prüfung auf Gültigkeit
                    return;
                }
                jtf[cICD10_2].setText(text);
                return;
            }
        }
    }

    private void arztAuswahl(String[] suchenach) {
        jtf[cREZDAT].requestFocusInWindow();
        JRtaTextField tfArztNum = new JRtaTextField("", false);
        JRtaTextField lanr = new JRtaTextField("",false);
        ArztAuswahl awahl = new ArztAuswahl(null, "ArztAuswahl", suchenach,
                new JRtaTextField[] { jtf[cARZT], lanr, jtf[cARZTID] }, String.valueOf(jtf[cARZT].getText()
                                                                                                 .trim()));
        awahl.setModal(true);
        awahl.setLocationRelativeTo(this);
        awahl.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jtf[cREZDAT].requestFocusInWindow();
            }
        });
        try {
            verordnenderArzt = awahl.getArztRecord();
            jtf[cARZT].setText(verordnenderArzt.getNNameLanr());
            String aIdNeu = verordnenderArzt.getIdS();
            if (!Reha.instance.patpanel.patDaten.get(63)
                                                 .contains(("@" + aIdNeu + "@\n"))) {
                String aliste = Reha.instance.patpanel.patDaten.get(63) + "@" + aIdNeu + "@\n";
                Reha.instance.patpanel.patDaten.set(63, aliste + "@" + aIdNeu + "@\n");
                Reha.instance.patpanel.getLogic()
                                      .arztListeSpeichernString(aliste, false, Reha.instance.patpanel.aktPatID);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jtf[cREZDAT].requestFocusInWindow();
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Fehler beim Speichern der Arztliste!\n"
                            + "Bitte notieren Sie Patient, Rezeptnummer und den Arzt den Sie der\n"
                            + "Arztliste hinzufügen wollten und informieren Sie umgehend den Administrator.\n\nDanke");
        }
        awahl.dispose();
        awahl = null;

    }

    private void kassenAuswahl(String[] suchenach) {
        jtf[cARZT].requestFocusInWindow();
        KassenAuswahl kwahl = new KassenAuswahl(null, "KassenAuswahl", suchenach,
                new JRtaTextField[] { jtf[cKTRAEG], jtf[cPATID], jtf[cKASID] }, jtf[cKTRAEG].getText()
                                                                                            .trim());
        kwahl.setModal(true);
        kwahl.setLocationRelativeTo(this);
        kwahl.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (jtf[cKASID].getText()
                               .equals("")) {
                    String meldung = "Achtung - kann Preisgruppe nicht ermitteln!\n"
                            + "Das bedeutet diese Rezept kann später nicht abgerechnet werden!\n\n"
                            + "Und bedenken Sie bitte Ihr Kürzel wird dauerhaft diesem Rezept zugeordnet....";
                    JOptionPane.showMessageDialog(null, meldung);
                } else {
                    holePreisGruppe(jtf[cKASID].getText()
                                               .trim());
                    ladePreisliste(jcmb[cRKLASSE].getSelectedItem()
                                                 .toString()
                                                 .trim(),
                            preisgruppen[getPgIndex()]);
                    jtf[cARZT].requestFocusInWindow();
                }
            }
        });
        kwahl.dispose();
        kwahl = null;
    }

    private void holePreisGruppe(String idKtraeger) {
        try {
            Vector<Vector<String>> vec = null;
            if (SystemConfig.mitRs) {
                vec = SqlInfo.holeFelder(
                        "select preisgruppe,pgkg,pgma,pger,pglo,pgrh,pgpo,pgrs,pgft from kass_adr where id='" + idKtraeger
                                + "' LIMIT 1");
            } else {
                vec = SqlInfo.holeFelder(
                        "select preisgruppe,pgkg,pgma,pger,pglo,pgrh,pgpo from kass_adr where id='" + idKtraeger + "' LIMIT 1");
            }
            if (vec.size() > 0) {
                for (int i = 1; i < vec.get(0)
                                       .size(); i++) {
                    preisgruppen[i - 1] = Integer.parseInt(vec.get(0)
                                                              .get(i))
                            - 1;
                }
                preisgruppe = Integer.parseInt((String) vec.get(0)
                                                           .get(0))
                        - 1;
                jtf[cPREISGR].setText((String) vec.get(0)
                                                  .get(0));
            } else {
                JOptionPane.showMessageDialog(null,
                        "Achtung - kann Preisgruppe nicht ermitteln - Rezept kann später nicht abgerechnet werden!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Achtung - kann Preisgruppe nicht ermitteln - Rezept kann später nicht abgerechnet werden!\n"
                            + "Untersuchen Sie die Krankenkasse im Kassenstamm un weisen Sie dieser Kasse die entsprechend Preisgruppe zu");
        }
    }

    /**
     *
     * initialisiert ein Rezept mit Daten, die immer gesetzt werden müssen
     */
    private void initRezeptAll(Rezeptvector thisRezept) {
        if (thisRezept.getKtraeger()
                    .equals("")) { // eher ein Fall für check/speichern!
            JOptionPane.showMessageDialog(null,
                    "Achtung - kann Preisgruppe nicht ermitteln - Rezept kann später nicht abgerechnet werden!");
        }

        if (SystemConfig.AngelegtVonUser) {
            thisRezept.setAngelegtVon(Reha.aktUser);
        } else {
            thisRezept.setAngelegtVon("");
        }
        thisRezept.setAngelegtDatum(DatFunk.sDatInSQL(DatFunk.sHeute()));

        thisRezept.setLastEdit(Reha.aktUser);
        thisRezept.setLastEdDate(DatFunk.sDatInSQL(DatFunk.sHeute()));

        thisRezept.setHeimbew(Reha.instance.patpanel.patDaten.get(44)); // koennte sich geaendert haben
        thisRezept.setBefreit(Reha.instance.patpanel.patDaten.get(30)); // dito
        thisRezept.setGebuehrBezahlt(false); // kann noch nicht bezahlt sein (Rezeptgebühr)
        thisRezept.setGebuehrBetrag("0.00");
    }

    /**
     *
     * initialisiert ein leeres Rezept mit Daten aus dem aktuellen Patienten
     */
    private void initRezeptNeu(Rezeptvector thisRezept) {
        thisRezept.createEmptyVec();

        thisRezept.setKtrName(Reha.instance.patpanel.patDaten.get(13)); // Kasse
        thisRezept.setKtraeger(Reha.instance.patpanel.patDaten.get(68)); // id des Kassen-record

        initRezeptAll(thisRezept);

        thisRezept.setArzt(Reha.instance.patpanel.patDaten.get(25)); // Hausarzt als default
        thisRezept.setArztId(Reha.instance.patpanel.patDaten.get(67));
        thisRezept.setKm(Reha.instance.patpanel.patDaten.get(48));
        thisRezept.setPatIdS(Reha.instance.patpanel.patDaten.get(66));
        thisRezept.setPatIntern(Reha.instance.patpanel.patDaten.get(29));
        // Barcode
    }

    /**
     *
     * initialisiert ein kopiertes Rezept
     *  - aktualisiert Daten aus dem aktuellen Patienten,
     *  - löscht Daten, die nur für die Vorlage gelten (Behandlungen, Preise, Zuzahlung, ...)
     */
    private void initRezeptKopie(Rezeptvector thisRezept) {
        String kasseInVo = thisRezept.getKtrName();
        String kasseInPatStamm = Reha.instance.patpanel.patDaten.get(13);
        if (!kasseInVo.equals(kasseInPatStamm)) {
            // Kasse im Rezept stimmt nicht mit Kasse im Patientenstamm überein:
            // Pat. hat inzwischen Kasse gewechselt oder es ist ein BG- oder
            // Privatrezept
            if (askForKeepCurrent(kasseInVo, kasseInPatStamm) == JOptionPane.NO_OPTION) {
                thisRezept.setKtrName(kasseInPatStamm);
                thisRezept.setKtraeger(Reha.instance.patpanel.patDaten.get(68));
            }
        }
        
        initRezeptAll(thisRezept);

        thisRezept.setRezNb("");
        thisRezept.setRezeptDatum("");
        thisRezept.setTermine("");
        thisRezept.setZzStat("");
        thisRezept.setLastDate("");
    }

    private int askForKeepCurrent(String kasseInVO, String kassePatStamm) {
        String[] strOptions = { "Kasse der VO beibehalten", "Kasse aus Patientendaten verwenden" };
        return JOptionPane.showOptionDialog(null,
                "<html><b>Das Rezept enthält eine andere Kasse als die Stammdaten des Patienten: </b>\n"
                        + "\n     Kasse im kopierten Rezept:      " + kasseInVO + "\n     Kasse in den Patientendaten:  "
                        + kassePatStamm + "\n",
                "unterschiedliche Kassen gefunden", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                strOptions, strOptions[0]);
    }

    /**
     *
     * lädt die Daten aus der Rezept-Instanz myRezept in die Dialog-Felder des
     * Rezepts und setzt auch die ComboBoxen und CheckBoxen
     */
    private void copyVecToForm() {
        String test = StringTools.NullTest(myRezept.getKtrName());
        jtf[cKTRAEG].setText(test); // kasse
        test = StringTools.NullTest(myRezept.getKtraeger());
        jtf[cKASID].setText(test); // kid
        test = StringTools.NullTest(myRezept.getArzt());
        jtf[cARZT].setText(verordnenderArzt.getNNameLanr()); // arzt - LANR
        test = StringTools.NullTest(myRezept.getArztId());
        jtf[cARZTID].setText(test); // arztid
        test = StringTools.NullTest(myRezept.getRezeptDatum());
        if (!test.equals("")) {
            jtf[cREZDAT].setText(DatFunk.sDatInDeutsch(test));
        }
        test = StringTools.NullTest(myRezept.getLastDate());
        if (!test.equals("")) {
            jtf[cBEGINDAT].setText(DatFunk.sDatInDeutsch(test));
        }
        int itest = 0;
        jcb[cHAUSB].setSelected(myRezept.getHausbesuch());

        jcb[cVOLLHB].setSelected(myRezept.getHbVoll());

        jcb[cLeitSymptA].setSelected(myRezept.getLeitSymIsA());
        jcb[cLeitSymptB].setSelected(myRezept.getLeitSymIsB());
        jcb[cLeitSymptC].setSelected(myRezept.getLeitSymIsC());
        jcb[cLeitSymptX].setSelected(myRezept.getLeitSymIsX());
        lsym_x_txt.setText(myRezept.getLeitSymText());
/*        for (int i = cLeitSymptA; i <= cLeitSymptX; i++) {
            if (jcb[i].isSelected()) {
                resetLeitSymCBoxExcept(i);
                break;
            }
        }
 */
        jcb[cTBANGEF].setSelected(myRezept.getArztbericht());
        jcb[cdringBehBedarf].setSelected(myRezept.getDringlich());
        jtf[cANZ1].setText(myRezept.getAnzBehS(1));
        jtf[cANZ2].setText(myRezept.getAnzBehS(2));
        jtf[cANZ3].setText(myRezept.getAnzBehS(3));
        jtf[cANZ4].setText(myRezept.getAnzBehS(4));

        itest = StringTools.ZahlTest(myRezept.getArtDBehandl(1));
        jcmb[cLEIST1].setSelectedIndex(leistungTesten(0, itest));
        itest = StringTools.ZahlTest(myRezept.getArtDBehandl(2));
        jcmb[cLEIST2].setSelectedIndex(leistungTesten(1, itest));
        itest = StringTools.ZahlTest(myRezept.getArtDBehandl(3));
        jcmb[cLEIST3].setSelectedIndex(leistungTesten(2, itest));
        itest = StringTools.ZahlTest(myRezept.getArtDBehandl(4));
        jcmb[cLEIST4].setSelectedIndex(leistungTesten(3, itest));

        test = StringTools.NullTest(myRezept.getFrequenz());
        jtf[cFREQ].setText(test);
        test = StringTools.NullTest(myRezept.getDauer());
        jtf[cDAUER].setText(test);

        test = StringTools.NullTest(myRezept.getIndiSchluessel());
        jcmb[cINDI].setSelectedItem(test);

        itest = myRezept.getBarcodeform();
        if (itest >= 0) {
            jcmb[cBARCOD].setSelectedIndex(itest);
        } else {
            myRezept.setBarcodeform(jcmb[cBARCOD].getSelectedIndex()); // default wird in ladePreisliste() gesetzt
        }

        test = StringTools.NullTest(myRezept.getAngelegtVon());
        jtf[cANGEL].setText(test);
        if (!test.trim()
                 .equals("")) {
            jtf[cANGEL].setEnabled(false);
        }
        diagnose_txt.setText(StringTools.NullTest(myRezept.getDiagn()));
        lsym_x_txt.setText(StringTools.NullTest(myRezept.getLeitSymText()));
        thZielBefund_txt.setText(StringTools.NullTest(myRezept.getTherapieZiel()));

        if (!jtf[cKASID].getText()
                        .equals("")) {
            holePreisGruppe(jtf[cKASID].getText()
                                       .trim());
        } else {
            JOptionPane.showMessageDialog(null, "Ermittlung der Preisgruppen erforderlich");
        }

        jtf[cHEIMBEW].setText(Reha.instance.patpanel.patDaten.get(44)); // heimbewohn
        jtf[cBEFREIT].setText(Reha.instance.patpanel.patDaten.get(30)); // befreit
        jtf[cANZKM].setText(Reha.instance.patpanel.patDaten.get(48)); // kilometer
        jtf[cPATID].setText(myRezept.getPatIdS());
        jtf[cPATINT].setText(myRezept.getPatIntern());

        jtf[cICD10].setText(myRezept.getICD10());
        jtf[cICD10_2].setText(myRezept.getICD10_2());

        itest = myRezept.getFarbCode();
        if (itest >= 0) {
            jcmb[cFARBCOD].setSelectedItem((String) SystemConfig.vSysColsBedeut.get(itest));
        }

        itest = myRezept.getVoArtHmr2020();
        jcmb[cVERORDART].setSelectedIndex(itest);

        test = StringTools.NullTest(myRezept.getAkutDatum());
        if (!test.equals("")) {
            jtf[cAKUTDATUM].setText(DatFunk.sDatInDeutsch(test));
        }
        
    }

    /***********
     *
     * lädt die Daten aus den Dialog-Feldern des Rezepts erstmalig in die
     * Rezept-Instanz
     * @param thisRezept
     */
    private void copyFormToVec1stTime(Rezeptvector thisRezept) {
        thisRezept.setAnzHB(jtf[cANZ1].getText());
        copyFormToVec(thisRezept);
    }

    /***********
     *
     * lädt die Daten aus den Dialog-Feldern des Rezepts in die Rezept-Instanz
     * @param thisRezept
     */
    private void copyFormToVec(Rezeptvector thisRezept) {
        try {
            if (!komplettTest()) {
                return;
            }
            setCursor(Cursors.wartenCursor);
            String stest = "";
            int itest = -1;

            thisRezept.setKtrName(jtf[cKTRAEG].getText());
            thisRezept.setKtraeger(jtf[cKASID].getText());
            String[] arzt = (jtf[cARZT].getText()).split(" - ");
            thisRezept.setArzt(arzt[0]);   // LANR wieder ausblenden
            thisRezept.setArztId(jtf[cARZTID].getText());

            stest = jtf[cREZDAT].getText()
                                .trim();
            if (stest.equals(".  .")) {
                stest = DatFunk.sHeute();
            }
            boolean neuerpreis = RezTools.neuePreisNachRezeptdatumOderStichtag(aktuelleDisziplin, preisgruppe,
                    String.valueOf(stest), false, Reha.instance.patpanel.vecaktrez);
            thisRezept.setRezeptDatum(DatFunk.sDatInSQL(stest));
            setRezDatInTable(stest);
            thisRezept.setDringlich(jcb[cdringBehBedarf].isSelected());
            String stest2 = "";
            if (thisRezept.getDringlich()) {
                String rezDat = DatFunk.sDatInDeutsch(thisRezept.getRezeptDatum());
                stest2 = DatFunk.sDatPlusTage(rezDat, HMRCheck2020.getFristDringlich());
            } else {
                stest2 = chkLastBeginDat(stest, jtf[cBEGINDAT].getText()
                                                              .trim(),
                        jtf[cPREISGR].getText(), aktuelleDisziplin);
            }
            setLastDatInTable(stest2);
            thisRezept.setLastDate(DatFunk.sDatInSQL(stest2));
            thisRezept.setLastEdDate(DatFunk.sDatInSQL(DatFunk.sHeute()));
            thisRezept.setLastEdit(Reha.aktUser);
            thisRezept.setHausbesuch(jcb[cHAUSB].isSelected());
            if (thisRezept.getHausbesuch()) {
                String anzHB = String.valueOf(thisRezept.getAnzHB());
                if (!anzHB.equals(jtf[cANZ1].getText())) {
                    int frage = JOptionPane.showConfirmDialog(null, "Achtung!\n\nDie Anzahl Hausbesuche = " + anzHB
                            + "\n" + "Die Anzahl des ersten Heilmittels = " + jtf[cANZ1].getText() + "\n\n"
                            + "Soll die Anzahl Hausbesuche ebenfalls auf " + jtf[cANZ1].getText() + " gesetzt werden?",
                            "Benutzeranfrage", JOptionPane.YES_NO_OPTION);
                    if (frage == JOptionPane.YES_OPTION) {
                        thisRezept.setAnzHB(jtf[cANZ1].getText());
                    }
                }
            }
            thisRezept.setArztBericht(jcb[cTBANGEF].isSelected());
            thisRezept.setAnzBeh(1, jtf[cANZ1].getText());
            thisRezept.setAnzBeh(2, jtf[cANZ2].getText());
            thisRezept.setAnzBeh(3, jtf[cANZ3].getText());
            thisRezept.setAnzBeh(4, jtf[cANZ4].getText());

            for (int i = 0; i < 4; i++) {
                int idxVec = i + 1;
                itest = jcmb[cLEIST1 + i].getSelectedIndex();
                if (itest > 0) { // 0 ist der Leereintrag!
                    int idxPv = itest - 1;
                    thisRezept.setArtDBehandl(idxVec, preisvec.get(idxPv)
                                                            .get(9));
                    thisRezept.setPreis(idxVec, preisvec.get(idxPv)
                                                      .get(neuerpreis ? 3 : 4));
                    thisRezept.setHmPos(idxVec, preisvec.get(idxPv)
                                                      .get(2));
                    thisRezept.setHMkurz(idxVec, preisvec.get(idxPv)
                                                       .get(1));
                } else {
                    thisRezept.setArtDBehandl(idxVec, "0");
                    thisRezept.setPreis(idxVec, "0.00");
                    thisRezept.setHmPos(idxVec, "");
                    thisRezept.setHMkurz(idxVec, "");
                }
            }

            thisRezept.setFrequenz(jtf[cFREQ].getText());
            thisRezept.setDauer(jtf[cDAUER].getText());
            if (jcmb[cINDI].getSelectedIndex() > 0) {
                thisRezept.setIndiSchluessel((String) jcmb[cINDI].getSelectedItem());
            } else {
                thisRezept.setIndiSchluessel(noDiagGrpSelected);
            }

            thisRezept.setBarcodeform(jcmb[cBARCOD].getSelectedIndex());
            thisRezept.setAngelegtVon(jtf[cANGEL].getText());
            thisRezept.setPreisgruppe(jtf[cPREISGR].getText());

            if (jcmb[cFARBCOD].getSelectedIndex() > 0) {
                thisRezept.setFarbCode(13 + jcmb[cFARBCOD].getSelectedIndex());
            } else {
                thisRezept.setFarbCode(-1);
            }
            thisRezept.setVoArtHmr2020(jcmb[cVERORDART].getSelectedIndex());

            //// System.out.println("Speichern bestehendes Rezept -> Preisgruppe =
            //// "+jtf[cPREISGR].getText());
            Integer izuzahl = Integer.valueOf(jtf[cPREISGR].getText());
            String szzstatus = "";

            String unter18 = "F";
            for (int i = 0; i < 1; i++) {
                if (SystemPreislisten.hmZuzahlRegeln.get(aktuelleDisziplin)
                                                    .get(izuzahl - 1) <= 0) {
                    szzstatus = "0";
                    break;
                }
                if (aktuelleDisziplin.equals("Reha")) {
                    szzstatus = "0";
                    break;
                }
                if (aktuelleDisziplin.equals("Rsport") || aktuelleDisziplin.equals("Ftrain")) {
                    szzstatus = "0";
                    break;
                }
                //// System.out.println("ZuzahlStatus = Zuzahlung (zunächst) erforderlich, prüfe
                //// ob befreit oder unter 18");
                if (Reha.instance.patpanel.patDaten.get(30)
                                                   .equals("T")) {
                    // System.out.println("aktuelles Jahr ZuzahlStatus = Patient ist befreit");
                    if (thisRezept.getGebuehrBezahlt()) {
                        szzstatus = "1";
                    } else {

                        if (RezTools.mitJahresWechsel(thisRezept.getRezeptDatum())) {

                            String vorjahr = Reha.instance.patpanel.patDaten.get(69);
                            if (vorjahr.trim()
                                       .equals("")) {
                                // Nur einspringen wenn keine Vorjahrbefreiung vorliegt.
                                // Tabelle mit Einzelterminen auslesen ob Sätze vorhanden
                                // wenn Sätze = 0 und bereits im Befreiungszeitraum dann "0", ansonsten "2"
                                // Wenn Sätze > 0 dann ersten Satz auslesen Wenn Datum < Befreiung-ab dann "2"
                                // ansonsten "0"
                                if (Reha.instance.patpanel.aktRezept.tabaktterm.getRowCount() > 0) {
                                    // es sind bereits Tage verzeichnet.
                                    String ersterTag = Reha.instance.patpanel.aktRezept.tabaktterm.getValueAt(0, 0)
                                                                                                  .toString();
                                    try {
                                        if (DatFunk.TageDifferenz(
                                                DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(41)),
                                                ersterTag) >= 0) {
                                            // Behandlung liegt nach befr_ab
                                            szzstatus = "0";
                                        } else {
                                            // Behandlung liegt vor befr_ab
                                            szzstatus = "2";
                                        }
                                    } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(null,
                                                "Fehler:\nBefreit ab, im Patientenstamm nicht oder falsch eingetragen");
                                    }

                                } else {
                                    // es sind noch keine Sätze verzeichnet
                                    if (DatFunk.TageDifferenz(
                                            DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(41)),
                                            DatFunk.sHeute()) >= 0) {
                                        // Behandlung muß nach befr_ab liegen
                                        szzstatus = "0";
                                    } else {
                                        // Behandlung kann auch vor befr_ab liegen
                                        szzstatus = "2";
                                    }
                                }
                            } else {
                                szzstatus = "0";
                            }
                        } else {
                            szzstatus = "0";
                        }
                    }
                    break;
                }

                if (DatFunk.Unter18(DatFunk.sHeute(), DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)))) {
                    // System.out.println("ZuzahlStatus = Patient ist unter 18 also befreit...");
                    int aj = Integer.parseInt(SystemConfig.aktJahr) - 18;
                    String gebtag = DatFunk.sHeute()
                                           .substring(0, 6)
                            + Integer.toString(aj);
                    long tage = DatFunk.TageDifferenz(DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)),
                            gebtag);

                    // System.out.println("Differenz in Tagen = "+tage);
                    // System.out.println("Geburtstag = "+gebtag);

                    if (tage < 0 && tage >= -45) {
                        JOptionPane.showMessageDialog(null,
                                "Achtung es sind noch " + (tage * -1) + " Tage bis zur Volljährigkeit\n"
                                        + "Unter Umständen wechselt der Zuzahlungsstatus im Verlauf dieses Rezeptes");
                        szzstatus = "3";
                    } else {
                        szzstatus = "0";
                    }
                    unter18 = "T";
                    break;
                }
                /**********************/
                if (thisRezept.getGebuehrBezahlt() || (thisRezept.getGebuehrBetrag() > 0.00)) {
                    szzstatus = "1";
                } else {
                    // hier testen ob erster Behandlungstag bereits ab dem Befreiungszeitraum
                    szzstatus = "2";
                }
            }
            /******/

            String[] lzv = holeLFV("anamnese", "pat5", "pat_intern", jtf[cPATINT].getText(), rezKlasse.toUpperCase()
                                                                                                      .substring(0, 2));
            if (!lzv[0].equals("")) {
                if (!diagnose_txt.getText()
                        .contains(lzv[0])) {
                    int frage = JOptionPane.showConfirmDialog(null,
                            "Für den Patient ist eine Langfristverordnung eingetragen die diese Verordnung noch nicht einschließt.\n\n"
                                    + lzv[1] + "\n\nWollen Sie diesen Eintrag dieser Verordnung zuweisen?",
                            "Achtung wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
                    if (frage == JOptionPane.YES_OPTION) {
                        diagnose_txt.setText(diagnose_txt.getText() + "\n" + lzv[0]);
                    }
                }
            }
            /*****/

            thisRezept.setUnter18(unter18); // oben schon berechnet
            thisRezept.setZzStat(szzstatus);
            thisRezept.setDiagn(StringTools.Escaped(diagnose_txt.getText()));
            thisRezept.setLeitSymText(StringTools.Escaped(lsym_x_txt.getText()));
            thisRezept.setTherapieZiel(StringTools.Escaped(thZielBefund_txt.getText()));

            thisRezept.setvorJahrFrei(Reha.instance.patpanel.patDaten.get(69)); // (?) falls seit Rezeptanlage geaendert
                                                                              // (?) (nicht editierbar -> kann in's
                                                                              // 'initRezeptAll')
            thisRezept.setHeimbew(jtf[cHEIMBEW].getText()); // dito
            thisRezept.setHbVoll(jcb[cVOLLHB].isSelected() ? true : false); // dito

            thisRezept.setLeitSymIsA(jcb[cLeitSymptA].isSelected() ? true : false);
            thisRezept.setLeitSymIsB(jcb[cLeitSymptB].isSelected() ? true : false);
            thisRezept.setLeitSymIsC(jcb[cLeitSymptC].isSelected() ? true : false);
            thisRezept.setLeitSymIsX(jcb[cLeitSymptX].isSelected() ? true : false);

            thisRezept.setUseHygPausch(jcb[cHygienePausch].isSelected() ? true : false);

            stest = jtf[cANZKM].getText()
                               .trim(); // dito
            thisRezept.setKm(stest.equals("") ? "0.00" : stest);
            int rule = SystemPreislisten.hmZuzahlRegeln.get(aktuelleDisziplin)
                                                       .get(Integer.parseInt(jtf[cPREISGR].getText()) - 1);
            thisRezept.setZzRegel(rule);
            thisRezept.setICD10(jtf[cICD10].getText()
                                         .replace(" ", ""));
            thisRezept.setICD10_2(jtf[cICD10_2].getText()
                                             .replace(" ", ""));

            thisRezept.setVoArtHmr2020(jcmb[cVERORDART].getSelectedIndex());
            stest = jtf[cAKUTDATUM].getText()
                    .trim();
            if (!stest.equals(".  .")) {
                thisRezept.setAkutDatum(DatFunk.sDatInSQL(stest));
            }
            
            thisRezept.setIsHMR2020(true);
            setCursor(Cursors.normalCursor);
        } catch (Exception ex) {
            ex.printStackTrace();
            setCursor(Cursors.normalCursor);
            JOptionPane.showMessageDialog(null,
                    "Fehler beim Abspeichern dieses Rezeptes.\n"
                            + "Bitte notieren Sie den Namen des Patienten und die Rezeptnummer\n"
                            + "und informieren Sie umgehend den Administrator");
        }
    }

    /********************************/

    private Double holePreisDoubleX(String pos, int ipreisgruppe) {
        Double dbl = 0.0;
        for (int i = 0; i < preisvec.size(); i++) {
            if (this.preisvec.get(i)
                             .get(0)
                             .equals(pos)) {
                if (this.preisvec.get(i)
                                 .get(3)
                                 .equals("")) {
                    return dbl;
                }
                return Double.parseDouble(this.preisvec.get(i)
                                                       .get(3));
            }
        }
        return dbl;
    }

    private Double holePreisDouble(String id, int ipreisgruppe) {
        Double dbl = 0.0;
        for (int i = 0; i < preisvec.size(); i++) {
            if (this.preisvec.get(i)
                             .get(9)
                             .equals(id)) {
                if (this.preisvec.get(i)
                                 .get(1)
                                 .equals("")) {
                    return dbl;
                }
                return Double.parseDouble(this.preisvec.get(i)
                                                       .get(3));
            }
        }
        return dbl;
    }

    /*********************************/

    private String[] holePreis(int ivec, int ipreisgruppe) {
        if (ivec > 0) {
            int prid = Integer.valueOf((String) this.preisvec.get(ivec)
                                                             .get(this.preisvec.get(ivec)
                                                                               .size()
                                                                     - 1));
            Vector<?> xvec = ((Vector<?>) this.preisvec.get(ivec));
            return new String[] { (String) xvec.get(3), (String) xvec.get(2) };
        } else {
            return new String[] { "0.00", "" };
        }
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
                                + "Gültig ab: " + woerter[5] + "\n" + "Gültig bis: " + woerter[6] + "\n";
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

    public static String makeStacktraceToString(Exception ex) {
        String string = "";
        try {
            StackTraceElement[] se = ex.getStackTrace();
            for (int i = 0; i < se.length; i++) {
                string = string + se[i].toString() + "\n";
            }
        } catch (Exception ex2) {

        }
        return string;
    }

    private void doAbbrechen() {
        if ((Boolean) SystemConfig.hmRezeptDlgIni.get("RezAendAbbruchWarn")) {
            if (HasChanged() && askForCancelUsaved() == JOptionPane.NO_OPTION)
                return;
        }

        aufraeumen();
        closeDialog();
        
    }

    @Override
    public void rehaTPEventOccurred(RehaTPEvent evt) {
        try {
            if (evt.getDetails()[0] != null) {
                if (evt.getDetails()[0].equals(this.getName())) {
                    this.setVisible(false);
                    rtp.removeRehaTPEventListener((RehaTPEventListener) this);
                    rtp = null;
                    aufraeumen();
                }
            }
        } catch (NullPointerException ne) {
            JOptionPane.showMessageDialog(null, "Fehler beim abhängen des Listeners Rezept-Neuanlage\n"
                    + "Bitte informieren Sie den Administrator über diese Fehlermeldung");
        }
    }

    public void closeDialog() {
        Component comp = SwingUtilities.getRoot(this);
        ((Window) comp).dispose(); 
    }

    public void aufraeumen() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i < jtf.length; i++) {
                    ListenerTools.removeListeners(jtf[i]);
                }
                for (int i = 0; i < jcb.length; i++) {
                    ListenerTools.removeListeners(jcb[i]);
                }
                for (int i = 0; i < jcmb.length; i++) {
                    ListenerTools.removeListeners(jcmb[i]);
                }
                ListenerTools.removeListeners(diagnose_txt);
                ListenerTools.removeListeners(lsym_x_txt);
                ListenerTools.removeListeners(thZielBefund_txt);
                ListenerTools.removeListeners(getInstance());
                if (rtp != null) {
                    rtp.removeRehaTPEventListener((RehaTPEventListener) getInstance());
                    rtp = null;
                }
                return null;
            }
        }.execute();
    }

    private int getPgIndex() {
        return jcmb[cRKLASSE].getSelectedIndex();
    }
}