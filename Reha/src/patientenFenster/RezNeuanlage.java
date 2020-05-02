package patientenFenster;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.LinearGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.MattePainter;
import org.therapi.reha.patient.AktuelleRezepte;

import com.jgoodies.forms.builder.PanelBuilder;
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
import abrechnung.Disziplinen;
import commonData.ArztVec;
import commonData.Rezeptvector;
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

public class RezNeuanlage extends JXPanel implements ActionListener, KeyListener, FocusListener, RehaTPEventListener {

    private final class Icd10Listener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            System.out.println("gotcha");
            super.mouseClicked(e);
        }


    }

    /**
     * McM '18: Umbau Struktur
     *     Konzept:
     *         - Rezeptvector 'vec' wird ersetzt durch 'myRezept' (Instanz der Klasse 'Rezept'; Zugriff über get/set)
     *         - zu Beginn ist myRezept entweder leer (komplette Neuanlage) oder enthaelt Daten der Kopiervorlage
     *         - ein neues Rezept wird mit Daten aus dem Patienten-Record u. der gewählten Rezeptklasse initialisiert
     *         - ein kopiertes Rezept wird zuerst bereinigt (Behandlungen, Zuzahlung, ... entfernen)
     *         - Eintragen der Daten in's Rezeptformular u. Auslesen aus demselben jeweils mit 1 zentralen Funktion
     *         - Schreiben der Rezeptdaten in die DB uebernimmt die entspr. Fkt der Klasse 'Rezept'
     *         - Fkt.:
     *             ladeZusatzDatenAlt/Neu() -> initRezept*()
     *             doSpeichernAlt/Neu -> copyFormToVec(), copyFormToVec1stTime()
     *
     */
    // Lemmi Doku: Das sind die Text-Eingabefgelder im Rezept
    public JRtaTextField[] jtf = new JRtaTextField[32];
    // Lemmi 20101231: Harte Index-Zahlen für "jtf" durch sprechende Konstanten
    // ersetzt !
    final int cKTRAEG = 0;
    final int cARZT = 1;
    final int cREZDAT = 2;
    final int cBEGINDAT = 3;
    final int cANZ1 = 4; // Lemmi 20101231: ACHTUNG
                         // die Positionen cANZ1 bis cANZ4 müssen immer
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
    final int cPOS1 = 16; // Lemmi 20101231: ACHTUNG
                          // die Positionen cPOS1 bis cPOS4 müssen immer
                          // nacheinander definiert sein
    final int cPOS2 = 17;
    final int cPOS3 = 18;
    final int cPOS4 = 19;
    final int cPREIS1 = 20; // Lemmi 20101231: ACHTUNG
                            // die Positionen cPREIS1 bis cPREIS4 müssen immer
                            // nacheinander definiert sein
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

    // Lemmi 20101231: Merken der Originalwerte der eingelesenen Textfelder, Combo-
    // und Check-Boxen
    Vector<Object> originale = new Vector<Object>();

    public JRtaCheckBox[] jcb = { null, null, null, null };
    // Lemmi 20101231: Harte Index-Zahlen für "jcb" durch sprechende Konstanten
    // ersetzt !
    final int cBEGRADR = 0;
    final int cHAUSB = 1;
    final int cTBANGEF = 2;
    final int cVOLLHB = 3;

    public JRtaComboBox[] jcmb = { null, null, null, null, null, null, null, null, null };

    final int cRKLASSE = 0;
    final int cVERORD = 1;
    final int cLEIST1 = 2; // Lemmi 20101231: ACHTUNG
                           // die Positionen cLEIST1 bis cLEIST4 müssen immer
                           // nacheinander definiert sein
    final int cLEIST2 = 3;
    final int cLEIST3 = 4;
    final int cLEIST4 = 5;
    final int cINDI = 6;
    final int cBARCOD = 7;
    final int cFARBCOD = 8;

    public JTextArea jta = null;

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

    public RezNeuanlage(Vector<String> vec, boolean neu, Connection connection) { 
        super();
        try {
            this.neu = neu;
            this.vec = vec; // Lemmi 20110106 Wird auch fuer das Kopieren verwendet !!!!
            myRezept = new Rezeptvector();
            verordnenderArzt = new ArztVec();
            myRezept.setVec_rez(vec);
            diszis = new Disziplinen();

            if (vec.size() > 0 && this.neu) {
                aktuelleDisziplin = RezTools.getDisziplinFromRezNr(vec.get(1)); 
            }

            setName("RezeptNeuanlage");
            rtp = new RehaTPEventClass();
            rtp.addRehaTPEventListener(this);

            addKeyListener(this);

            setLayout(new BorderLayout());
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            add(getDatenPanel(), BorderLayout.CENTER);
            add(getButtonPanel(), BorderLayout.SOUTH);
            setBackgroundPainter(Reha.instance.compoundPainter.get("RezNeuanlage"));
            validate();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setzeFocus();
                }
            });
            initReady = true;
            if (!neu) {
                if (!Rechte.hatRecht(Rechte.Rezept_editvoll, false)) { // Lemmi Doku: Das sieht aus wie der
                                                                       // Read-Only-Modus für das Rezept
                    for (int i = 0; i < jtf.length; i++) { // Lemmi Doku: alle Textfelder unbedienbar machen
                        if (jtf[i] != null) {
                            jtf[i].setEnabled(false);
                        }
                    }
                    for (int i = 0; i < jcb.length; i++) { // Lemmi Doku: alle CheckBoxen unbedienbar machen
                        if (jcb[i] != null) {
                            jcb[i].setEnabled(false);
                        }
                    }
                    for (int i = 0; i < jcmb.length; i++) { // Lemmi Doku: alle ComboBoxen unbedienbar machen
                        if (jcmb[i] != null) {
                            jcmb[i].setEnabled(false);
                        }
                    }
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler im Konstruktor RezNeuanlage\n" + RezNeuanlage.makeStacktraceToString(ex));
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
                    jcmb[cFARBCOD].setSelectedItem(SystemConfig.vSysColsBedeut.get(itest));
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

    // Lemmi 20101231: Merken der Originalwerte der eingelesenen Textfelder
    // ACHTUNG: Die Reihenfolge der Abfragen muß in SaveChangeStatus() und
    // HasChanged() exakt identisch sein !
    private void SaveChangeStatus() {
        int i;
        originale.clear(); // vorherige Merkung wegwerfen

        // Alle Text-Eingabefelder
        for (i = 0; i < jtf.length; i++) {
            // String strText = jtf[i].getText();
            originale.add(jtf[i].getText());
        }

        // Das Feld mit "Ärztliche Diagnose"
        originale.add(jta.getText());

        // alle ComboBoxen
        for (i = 0; i < jcmb.length; i++) {
            originale.add(jcmb[i].getSelectedIndex()); // Art d. Verordn. etc.
        }

        // alle CheckBoxen
        for (i = 0; i < jcb.length; i++) {
            originale.add((jcb[i].isSelected())); //
        }
    }

    // Lemmi 20101231: prüft, ob sich Einträge geändert haben
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
        if (!jta.getText()
                .equals(originale.get(idx++))) // Ärztliche Diagnose
            return true;

        // alle ComboBoxen
        for (i = 0; i < jcmb.length; i++) { // ComboBoxen
            if (jcmb[i].getSelectedIndex() != (Integer) originale.get(idx++)) // Art d. Verordn. etc.
                return true;
        }

        // alle CheckBoxen
        for (i = 0; i < jcb.length; i++) { // CheckBoxen
            if (jcb[i].isSelected() != (Boolean) originale.get(idx++)) // Begründung außer der Regel vorhanden ? .....
                return true;
        }

        return false;
    }

    // Lemmi 20101231: Standard-Abfrage nach Prüfung, ob sich Einträge geändert haben
    // fragt nach, ob wirklich ungesichert abgebrochen werden soll !
    public int askForCancelUsaved() {
        String[] strOptions = { "ja", "nein" }; // Defaultwert auf "nein" gesetzt !
        return JOptionPane.showOptionDialog(null,
                "Es wurden Rezept-Angaben geändert!\nWollen sie die Änderung(en) wirklich verwerfen?",
                "Angaben wurden geändert", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, strOptions,
                strOptions[1]);
    }

    /**
     * @return
     */
    private JScrollPane getDatenPanel() { // 1 2 3 4 5 6 7 8
        FormLayout lay = new FormLayout("right:max(80dlu;p), 4dlu, 60dlu, 5dlu, right:max(60dlu;p), 4dlu, 60dlu",
                // 1. 2. 3. 4. 5. 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25
                "p, 10dlu, p, 5dlu,  p, 5dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 10dlu, p, 10dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, "
                        +
                        // 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42
                        "10dlu, p, 10dlu, p, 2dlu, p, 2dlu,  p,  10dlu, p, 10dlu, p,10dlu,p,10dlu,30dlu,2dlu");

        CellConstraints cc = new CellConstraints();
        PanelBuilder jpan = new PanelBuilder(lay);
        jpan.setDefaultDialogBorder();
        jpan.getPanel()
            .setOpaque(false);
        JScrollPane jscr = null;

        try {
            // Lemmi 20101231: Harte Index-Zahlen für "jtf" durch sprechende Konstanten
            // ersetzt !
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
            JRtaTextField TF_icd10_1 = jtf[cICD10];
            MouseListener icd10Text = new Icd10Listener();
            TF_icd10_1.addMouseListener(icd10Text );
            jtf[cICD10_2] = new JRtaTextField("GROSS", false); // 2. ICD10-Code
            JRtaTextField TF_icd10_2 = jtf[cICD10];
            TF_icd10_2.addMouseListener(icd10Text );
            jcmb[cRKLASSE] = new JRtaComboBox();
            strRezepklassenAktiv = diszis.getActiveRK();
            jcmb[cRKLASSE] = diszis.getComboBoxActiveRK();

            if (SystemConfig.AngelegtVonUser) {
                jtf[cANGEL].setText(Reha.aktUser);
                jtf[cANGEL].setEditable(false);
            }

            jpan.addLabel("Rezeptklasse auswählen", cc.xy(1, 3));
            jpan.add(jcmb[cRKLASSE], cc.xyw(3, 3, 5));
            jcmb[cRKLASSE].setActionCommand("rezeptklasse");
            jcmb[cRKLASSE].addActionListener(this);
            allowShortCut(jcmb[cRKLASSE], "RezeptClass");
            /********************/

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

            jpan.addSeparator("Rezeptkopf", cc.xyw(1, 5, 7));

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
            allowShortCut(jtf[cKTRAEG], "ktraeger");
            jpan.add(kassenLab, cc.xy(1, 7));
            jpan.add(jtf[cKTRAEG], cc.xy(3, 7));

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
            jpan.add(arztLab, cc.xy(5, 7));
            jpan.add(jtf[cARZT], cc.xy(7, 7));

            jtf[cREZDAT].setName("rez_datum");
            allowShortCut(jtf[cREZDAT], "rez_datum");
            jpan.addLabel("Rezeptdatum", cc.xy(1, 9));
            jpan.add(jtf[cREZDAT], cc.xy(3, 9));
            eingabeRezDate = jpan.add(jtf[cREZDAT], cc.xy(3, 9));

            allowShortCut(jtf[cBEGINDAT], "lastdate");
            jpan.addLabel("spätester Beh.Beginn", cc.xy(5, 9));
            jpan.add(jtf[cBEGINDAT], cc.xy(7, 9));

            jcmb[cVERORD] = new JRtaComboBox(
                    new String[] { "Erstverordnung", "Folgeverordnung", "außerhalb des Regelfalles" });
            jcmb[cVERORD].setActionCommand("verordnungsart");
            jcmb[cVERORD].addActionListener(this);
            allowShortCut(jcmb[cVERORD], "selArtDerVerordn");
            jpan.addLabel("Art d. Verordn.", cc.xy(1, 11));
            eingabeVerordnArt = jpan.add(jcmb[cVERORD], cc.xy(3, 11));

            jcb[cBEGRADR] = new JRtaCheckBox("vorhanden");
            jcb[cBEGRADR].setOpaque(false);
            jcb[cBEGRADR].setEnabled(false);
            allowShortCut(jcb[cBEGRADR], "adrCheck");
            jpan.addLabel("Begründ. für adR", cc.xy(5, 11));
            jpan.add(jcb[cBEGRADR], cc.xy(7, 11));

            jcb[cHAUSB] = new JRtaCheckBox("Ja / Nein");
            jcb[cHAUSB].setOpaque(false);
            jcb[cHAUSB].setActionCommand("Hausbesuche");
            jcb[cHAUSB].addActionListener(this);
            allowShortCut(jcb[cHAUSB], "hbCheck");
            jpan.addLabel("Hausbesuch", cc.xy(1, 13));
            jpan.add(jcb[cHAUSB], cc.xy(3, 13));

            jcb[cVOLLHB] = new JRtaCheckBox("abrechnen");
            jcb[cVOLLHB].setOpaque(false);
            jcb[cVOLLHB].setToolTipText("Nur aktiv wenn Patient Heimbewohner und Hausbesuch angekreuzt");
            jpan.addLabel("volle HB-Gebühr", cc.xy(5, 13));
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
            allowShortCut(jcb[cVOLLHB], "hbVollCheck");
            jpan.add(jcb[cVOLLHB], cc.xy(7, 13));

            jcb[cTBANGEF] = new JRtaCheckBox("angefordert");
            jcb[cTBANGEF].setOpaque(false);
            jpan.addLabel("Therapiebericht", cc.xy(1, 15));
            jcb[cTBANGEF].addKeyListener(this);
            jpan.add(jcb[cTBANGEF], cc.xy(3, 15));

            jpan.addSeparator("Verordnete Heilmittel", cc.xyw(1, 17, 7));

            jtf[cANZ1].setName("anzahl1");
            jtf[cANZ1].addFocusListener(this);
            jtf[cANZ1].addKeyListener(this);
            jpan.addLabel("Anzahl / Heilmittel 1", cc.xy(1, 19));
            eingabeVerordn1 = jpan.add(jtf[cANZ1], cc.xy(3, 19));
            jcmb[cLEIST1] = new JRtaComboBox();
            jcmb[cLEIST1].setActionCommand("leistung1");
            jcmb[cLEIST1].addActionListener(this);
            allowShortCut(jcmb[cLEIST1], "leistung1");
            jpan.add(jcmb[cLEIST1], cc.xyw(5, 19, 3));

            jpan.addLabel("Anzahl / Heilmittel 2", cc.xy(1, 21));
            jtf[cANZ2].addKeyListener(this);
            jpan.add(jtf[cANZ2], cc.xy(3, 21));
            jcmb[cLEIST2] = new JRtaComboBox();
            jcmb[cLEIST2].setActionCommand("leistung2");
            jcmb[cLEIST2].addActionListener(this);
            allowShortCut(jcmb[cLEIST2], "leistung2");
            jpan.add(jcmb[cLEIST2], cc.xyw(5, 21, 3));

            jpan.addLabel("Anzahl / Heilmittel 3", cc.xy(1, 23));
            jtf[cANZ3].addKeyListener(this);
            jpan.add(jtf[cANZ3], cc.xy(3, 23));
            jcmb[cLEIST3] = new JRtaComboBox();
            jcmb[cLEIST3].setActionCommand("leistung3");
            jcmb[cLEIST3].addActionListener(this);
            allowShortCut(jcmb[cLEIST3], "leistung3");
            jpan.add(jcmb[cLEIST3], cc.xyw(5, 23, 3));

            jpan.addLabel("Anzahl / Heilmittel 4", cc.xy(1, 25));
            jtf[cANZ4].addKeyListener(this);
            jpan.add(jtf[cANZ4], cc.xy(3, 25));
            jcmb[cLEIST4] = new JRtaComboBox();
            jcmb[cLEIST4].setActionCommand("leistung4");
            jcmb[cLEIST4].setName("leistung4");
            jcmb[cLEIST4].addActionListener(this);
            jpan.add(jcmb[cLEIST4], cc.xyw(5, 25, 3));

            jpan.addSeparator("Durchführungsbestimmungen", cc.xyw(1, 27, 7));

            jtf[cFREQ].addKeyListener(this);
            jpan.addLabel("Behandlungsfrequenz", cc.xy(1, 29));
            eingabeBehFrequ = jpan.add(jtf[cFREQ], cc.xy(3, 29));

            jpan.addLabel("Dauer der Behandl. in Min.", cc.xy(5, 29));
            jtf[cDAUER].addKeyListener(this);
            jpan.add(jtf[cDAUER], cc.xy(7, 29));

            jpan.addLabel("Indikationsschlüssel", cc.xy(1, 31));
            jcmb[cINDI] = new JRtaComboBox();
            jcmb[cINDI].addKeyListener(this);
            allowShortCut(jcmb[cINDI],"Indikationsschluessel");
            jpan.add(jcmb[cINDI], cc.xy(3, 31));

            klassenReady = true;
            this.fuelleIndis((String) jcmb[cRKLASSE].getSelectedItem());

            jpan.addLabel("Barcode-Format", cc.xy(5, 31));
            jcmb[cBARCOD] = new JRtaComboBox(SystemConfig.rezBarCodName);
            jcmb[cBARCOD].addKeyListener(this);
            jpan.add(jcmb[cBARCOD], cc.xy(7, 31));

            jpan.addLabel("FarbCode im TK", cc.xy(1, 33));
            jcmb[cFARBCOD] = new JRtaComboBox();
            jcmb[cFARBCOD].addKeyListener(this);
            macheFarbcodes();

            jpan.add(jcmb[cFARBCOD], cc.xy(3, 33));

            jpan.addLabel("Angelegt von", cc.xy(5, 33));
            jtf[cANGEL].addKeyListener(this);
            jpan.add(jtf[cANGEL], cc.xy(7, 33));

            jpan.addSeparator("ICD-10 Codes", cc.xyw(1, 35, 7));
            jpan.addLabel("1. ICD-10-Code", cc.xy(1, 37));
            allowShortCut(jtf[cICD10], "icd10");
            eingabeICD = jpan.add(jtf[cICD10], cc.xy(3, 37));

            jpan.addLabel("2. ICD-10-Code", cc.xy(5, 37));
            allowShortCut(jtf[cICD10_2], "icd10_2");
            jpan.add(jtf[cICD10_2], cc.xy(7, 37));

            jpan.addSeparator("Ärztliche Diagnose laut Rezept", cc.xyw(1, 39, 7));
            jta = new JTextArea();
            jta.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.5f)));
            jta.setFont(new Font("Courier", Font.PLAIN, 11));
            jta.setLineWrap(true);
            jta.setName("notitzen");
            jta.setWrapStyleWord(true);
            jta.setEditable(true);
            jta.setBackground(Color.WHITE);
            jta.setForeground(Color.RED);
            eingabeDiag = jta;
            JScrollPane span = JCompTools.getTransparentScrollPane(jta);
            span.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.25f)));
            jpan.add(span, cc.xywh(1, 41, 7, 2));
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
                this.fuelleIndis(jcmb[cRKLASSE].getSelectedItem()
                                               .toString()
                                               .trim());
            }
            verordnenderArzt.init(myRezept.getArztId());
            copyVecToForm();

            jscr.validate();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fehler in der Erstellung des Rezeptfensters\n" + ex.getMessage());
        }

        // Lemmi 20101231: Merken der Originalwerte der eingelesenen Textfelder
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

    public RezNeuanlage getInstance() {
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

    private String chkIcdFormat(String string) {
        int posDot = string.indexOf(".");
        if ((string.length() > 3) && (posDot < 0)) {
            String tmp1 = string.substring(0, 3);
            String tmp2 = string.substring(3);
            return tmp1 + "." + tmp2;
        }
        return string;
    }

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
             .equals("verordnungsart") && klassenReady) {
            if (jcmb[cVERORD].getSelectedIndex() == 2) {
                jcb[cBEGRADR].setEnabled(true);
                testeGenehmigung(jtf[cKASID].getText());
            } else {
                jcb[cBEGRADR].setSelected(false);
                jcb[cBEGRADR].setEnabled(false);
            }
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
                        closeDialog();
                        aufraeumen();
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
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        boolean icd10falsch = false;
                        int welcherIcd = 0;
                        if (jtf[cICD10].getText()
                                       .trim()
                                       .length() > 0) {
                            String suchenach = macheIcdString(jtf[cICD10].getText());
                            if (SqlInfo.holeEinzelFeld(
                                    "select id from icd10 where schluessel1 like '" + suchenach + "%' LIMIT 1")
                                       .equals("")) {
                                icd10falsch = true;
                                welcherIcd = 1;
                            }
                            if (jtf[cICD10_2].getText()
                                             .trim()
                                             .length() > 0) {
                                suchenach = macheIcdString(jtf[cICD10_2].getText());
                                if (SqlInfo.holeEinzelFeld(
                                        "select id from icd10 where schluessel1 like '" + suchenach + "%' LIMIT 1")
                                           .equals("")) {
                                    icd10falsch = true;
                                    welcherIcd = 2;
                                }
                            }
                        } else {
                            if (SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin)
                                                                 .get(preisgruppen[getPgIndex()]) == 1) {
                                hmrcheck.setEnabled(true);
                                JOptionPane.showMessageDialog(null,
                                        "<html><b><font color='#ff0000'>Kein ICD-10 Code angegeben!</font></b></html>");

                            }
                        }
                        doHmrCheck(icd10falsch, welcherIcd);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
            }.execute();
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

    private void testeGenehmigung(final String kassenid) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    String test = SqlInfo.holeEinzelFeld(
                            "select id from adrgenehmigung where ik = (select ik_kostent from kass_adr where id = '"
                                    + kassenid + "') LIMIT 1");
                    if (!test.isEmpty()) {
                        String meldung = "<html><b>Achtung!</b><br><br>Sie haben Verordnung außerhalb des Regelfalles gewählt!<br><br>Die Krankenkasse des Patienten besteht auf eine <br>"
                                + "<b>Genehmigung für Verordnungen außerhalb des Regelfalles</b><br><br></html>";
                        JOptionPane.showMessageDialog(null, meldung);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                            "Fehler!!!\n\nVermutlich haben Sie eines der letzten Updates verpaßt.\nFehlt zufällig die Tabelle adrgenehmigung?");
                    ex.printStackTrace();
                }
                return null;
            }
        }.execute();

    }

    // Lemmi Doku: prüft ob die Heilmittel überhaupt und in der korrekten
    // Reihenfolge eingetragen worden sind
    private boolean anzahlTest() {
        int itest;
        int maxanzahl = 0, aktanzahl = 0;

        for (int i = 0; i < 4; i++) { // über alle 4 Leistungs- und Anzahl-Positionen rennen
            itest = jcmb[cLEIST1 + i].getSelectedIndex();
            if (itest > 0) {
                if (i == 0) { // die 1. Position besonders abfragen - diese muß existieren !
                    try {
                        maxanzahl = Integer.parseInt(jtf[cANZ1 + i].getText());
                    } catch (Exception ex) {
                        maxanzahl = 0;
                    }
                } else {
                    try {
                        aktanzahl = Integer.parseInt(jtf[cANZ1 + i].getText());
                    } catch (Exception ex) {
                        aktanzahl = 0;
                    }
                    if (aktanzahl > maxanzahl) {
                        String cmd = "Sie haben mehrere Heilmittel mit unterschiedlicher Anzahl eingegeben.\n"
                                + "Bitte geben Sie die Heilmittel so ein daß das Heilmittel mit der größten Anzahl oben steht\n"
                                + "und dann (bezogen auf die Anzahl) in absteigender Reihgenfolge nach unten";
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

    private void doHmrCheck(boolean icd10falsch, int welcher) {
        if (SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin)
                                             .get(preisgruppen[getPgIndex()]) == 0) {
            this.hmrcheck.setEnabled(true);
            JOptionPane.showMessageDialog(null, "HMR-Check ist bei diesem Kostenträger nicht erforderlich");
            return;
        }
        // System.out.println(SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin).get(preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]));
        int itest = 0; // jcmb[cLEIST1].getSelectedIndex();
        String indi = (String) jcmb[cINDI].getSelectedItem();
        if (indi.equals("") || indi.contains("kein IndiSchl.")) {
            JOptionPane.showMessageDialog(null,
                    "<html><b>Kein Indikationsschlüssel angegeben.<br>Die Angaben sind <font color='#ff0000'>nicht</font> gemäß den gültigen Heilmittelrichtlinien!</b></html>");
            return;
        }
        indi = indi.replace(" ", "");
        Vector<Integer> anzahlen = new Vector<Integer>();
        Vector<String> hmpositionen = new Vector<String>();

        for (int i = 0; i < 4; i++) { // Lemmi Doku: Nacheinander alle 4 Leistungen abfragen und Anzahlen addieren
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
            JOptionPane.showMessageDialog(null, "Rezeptdatum nicht korrekt angegeben HMR-Check nicht möglich");
            return;
        }
        if (icd10falsch) {
            int frage = JOptionPane.showConfirmDialog(null,
                    "<html><b>Der eingetragene " + Integer.toString(welcher)
                            + ". ICD-10-Code ist falsch: <font color='#ff0000'>" + (welcher == 1 ? jtf[cICD10].getText()
                                                                                                              .trim()
                                    : jtf[cICD10_2].getText()
                                                   .trim())
                            + "</font></b><br>" + "HMR-Check nicht möglich!<br><br>"
                            + "Wollen Sie jetzt das ICD-10-Tool starten?<br><br></html>",
                    "falscher ICD-10", JOptionPane.YES_NO_OPTION);
            if (frage == JOptionPane.YES_OPTION) {
                new LadeProg(Path.Instance.getProghome()+"ICDSuche.jar"+" "+Path.Instance.getProghome()+" "+Reha.getAktIK());
            }
            if (welcher == 1) {
                jtf[cICD10].setText("");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jtf[cICD10].requestFocusInWindow();
                    }
                });
            } else if (welcher == 2) {
                jtf[cICD10_2].setText("");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jtf[cICD10_2].requestFocusInWindow();
                    }
                });

            }
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
            boolean checkok = new HMRCheck(tmpRezept, diszis.getCurrDisziFromActRK(), preisvec).check();
            if (checkok) {
                JOptionPane.showMessageDialog(null,
                        "<html><b>Das Rezept <font color='#ff0000'>entspricht</font> den geltenden Heilmittelrichtlinien</b></html>");
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

    /** Holt die passenden Inikationsschlüssel gemäß aktiver Disziplin**/
    private void fuelleIndis(String typeOfVO) {
        try {
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
                anz = Reha.instance.patpanel.aktRezept.indphysio.length;
                indis = Reha.instance.patpanel.aktRezept.indphysio;
            } else if (tmpItem.contains("ergo")) {
                anz = Reha.instance.patpanel.aktRezept.indergo.length;
                indis = Reha.instance.patpanel.aktRezept.indergo;
            } else if (tmpItem.contains("logo")) {
                anz = Reha.instance.patpanel.aktRezept.indlogo.length;
                indis = Reha.instance.patpanel.aktRezept.indlogo;
            } else if (tmpItem.contains("podo")) {
                anz = Reha.instance.patpanel.aktRezept.indpodo.length;
                indis = Reha.instance.patpanel.aktRezept.indpodo;
            }
            for (int i = 0; i < anz; i++) {
                jcmb[cINDI].addItem(indis[i]);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fehler bei füller Inikat.schlüssel\n" + ex.getMessage());

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
            if (componentName.equals("Indikationsschluessel") && jumpForward) {
                if (ctrlIsPressed) {
                    eingabeICD.requestFocusInWindow();
                }
                return;
            }
            if (componentName.equals("icd10")) {
                String text = jtf[cICD10].getText();
                jtf[cICD10].setText(chkIcdFormat(text));
                if (ctrlIsPressed & jumpForward) {
                    eingabeDiag.requestFocusInWindow();
                }
                return;
            }
            if (componentName.equals("icd10_2")) {
                String text = jtf[cICD10_2].getText();
                jtf[cICD10_2].setText(chkIcdFormat(text));
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
                preisgruppe = Integer.parseInt(vec.get(0)
                                                           .get(0))
                        - 1;
                jtf[cPREISGR].setText(vec.get(0)
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
        int itest = myRezept.getRezArt();
        if (itest >= 0) {
            jcmb[cVERORD].setSelectedIndex(itest);
        }
        jcb[cBEGRADR].setSelected(myRezept.getBegrAdR());
        jcb[cHAUSB].setSelected(myRezept.getHausbesuch());

        jcb[cVOLLHB].setSelected(myRezept.getHbVoll());

        jcb[cTBANGEF].setSelected(myRezept.getArztbericht());
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
        jta.setText(StringTools.NullTest(myRezept.getDiagn()));
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

        // ICD-10
        jtf[cICD10].setText(myRezept.getICD10());
        jtf[cICD10_2].setText(myRezept.getICD10_2());

        itest = myRezept.getFarbCode();
        if (itest >= 0) {
            jcmb[cFARBCOD].setSelectedItem(SystemConfig.vSysColsBedeut.get(itest));
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
            String stest2 = chkLastBeginDat(stest, jtf[cBEGINDAT].getText()
                                                                 .trim(),
                    jtf[cPREISGR].getText(), aktuelleDisziplin);
            setLastDatInTable(stest2);
            thisRezept.setLastDate(DatFunk.sDatInSQL(stest2));
            thisRezept.setLastEdDate(DatFunk.sDatInSQL(DatFunk.sHeute()));
            thisRezept.setLastEdit(Reha.aktUser);
            thisRezept.setRezArt(jcmb[cVERORD].getSelectedIndex());
            thisRezept.setBegrAdR(jcb[cBEGRADR].isSelected());
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
                thisRezept.setIndiSchluessel("kein IndiSchl.");
            }

            thisRezept.setBarcodeform(jcmb[cBARCOD].getSelectedIndex());
            thisRezept.setAngelegtVon(jtf[cANGEL].getText());
            thisRezept.setPreisgruppe(jtf[cPREISGR].getText());

            if (jcmb[cFARBCOD].getSelectedIndex() > 0) {
                thisRezept.setFarbCode(13 + jcmb[cFARBCOD].getSelectedIndex());
            } else {
                thisRezept.setFarbCode(-1);
            }
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
                if (!jta.getText()
                        .contains(lzv[0])) {
                    int frage = JOptionPane.showConfirmDialog(null,
                            "Für den Patient ist eine Langfristverordnung eingetragen die diese Verordnung noch nicht einschließt.\n\n"
                                    + lzv[1] + "\n\nWollen Sie diesen Eintrag dieser Verordnung zuweisen?",
                            "Achtung wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
                    if (frage == JOptionPane.YES_OPTION) {
                        jta.setText(jta.getText() + "\n" + lzv[0]);
                    }
                }
            }
            /*****/

            thisRezept.setUnter18(unter18); // oben schon berechnet
            thisRezept.setZzStat(szzstatus);
            thisRezept.setDiagn(StringTools.Escaped(jta.getText()));
            thisRezept.setvorJahrFrei(Reha.instance.patpanel.patDaten.get(69)); // (?) falls seit Rezeptanlage geaendert
                                                                              // (?) (nicht editierbar -> kann in's
                                                                              // 'initRezeptAll')
            thisRezept.setHeimbew(jtf[cHEIMBEW].getText()); // dito
            thisRezept.setHbVoll(jcb[cVOLLHB].isSelected() ? true : false); // dito
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
            int prid = Integer.valueOf(this.preisvec.get(ivec)
                                                             .get(this.preisvec.get(ivec)
                                                                               .size()
                                                                     - 1));
            Vector<?> xvec = (this.preisvec.get(ivec));
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
        // Lemmi 20101231: Verhinderung von Datenverlust bei unbeabsichtigtem Zumachen
        // des geänderten Rezept-Dialoges
        // Solche gravierenden Änderungen der Programmlogik dürfen erst dann eingeführt
        // werden
        // wenn sich der Benutzer auf einer System-Init-Seite entscheiden kann ob er
        // diese
        // Funktionalität will oder nicht
        // Wir im RTA wollen die Abfagerei definitiv nicht!
        // Wenn meine Damen einen Vorgang abbrechen wollen, dann wollen sie den Vorgang
        // abbrechen
        // und nicht gefrag werden ob sie den Vorgang abbrechen wollen.
        // Steinhilber
        // Lemmi 20110116: Gerne auch mit Steuer-Parameter
        if ((Boolean) SystemConfig.hmRezeptDlgIni.get("RezAendAbbruchWarn")) {
            if (HasChanged() && askForCancelUsaved() == JOptionPane.NO_OPTION)
                return;
        }

        aufraeumen();
        ((JXDialog) this.getParent()
                        .getParent()
                        .getParent()
                        .getParent()
                        .getParent()).dispose();
    }

    @Override
    public void rehaTPEventOccurred(RehaTPEvent evt) {
        try {
            if (evt.getDetails()[0] != null) {
                if (evt.getDetails()[0].equals(this.getName())) {
                    this.setVisible(false);
                    rtp.removeRehaTPEventListener(this);
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
        ((JXDialog) this.getParent()
                        .getParent()
                        .getParent()
                        .getParent()
                        .getParent()).setVisible(false);
        ((JXDialog) this.getParent()
                        .getParent()
                        .getParent()
                        .getParent()
                        .getParent()).dispose();
        setCursor(Cursors.normalCursor);
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
                ListenerTools.removeListeners(jta);
                ListenerTools.removeListeners(getInstance());
                if (rtp != null) {
                    rtp.removeRehaTPEventListener(getInstance());
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
