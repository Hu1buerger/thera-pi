package patientenFenster.rezepte;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.therapi.reha.patient.AktuelleRezepte;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.Colors;
import CommonTools.DatFunk;
import CommonTools.DateTimeFormatters;
import CommonTools.JCompTools;
import CommonTools.JRtaCheckBox;
import CommonTools.JRtaComboBox;
import CommonTools.JRtaTextField;
import CommonTools.SqlInfo;
import CommonTools.StringTools;
import abrechnung.Disziplinen;
import commonData.ArztVec;
import core.Disziplin;
import environment.LadeProg;
import environment.Path;
import events.RehaTPEvent;
import events.RehaTPEventClass;
import events.RehaTPEventListener;
import gui.Cursors;
import hauptFenster.Reha;
import hmrCheck.HMRCheck;
import mandant.Mandant;
import patientenFenster.ArztAuswahl;
import patientenFenster.KassenAuswahl;
import rechteTools.Rechte;
import rezept.KrankenkasseAdr;
import rezept.KrankenkasseAdrDto;
import rezept.Money;
import rezept.Rezept;
import rezept.RezeptDto;
import rezept.Rezeptnummer;
import rezept.Zuzahlung;
import stammDatenTools.RezTools;
import systemEinstellungen.NummernKreis;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;
import systemTools.ListenerTools;

public class RezeptEditorGUI extends JXPanel implements FocusListener, RehaTPEventListener {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RezeptEditorGUI.class);
    
    // Keep:
    private JRtaTextField jtfKTRAEG;
    private JRtaTextField jtfARZT;
    private JRtaTextField jtfREZDAT;
    private JRtaTextField jtfBEGINDAT;
    private JRtaTextField jtfANZ1;
    private JRtaTextField jtfANZ2;
    private JRtaTextField jtfANZ3;
    private JRtaTextField jtfANZ4;
    private JRtaTextField jtfFREQ;
    private JRtaTextField jtfDAUER;
    private JRtaTextField jtfANGEL;
    private JRtaTextField jtfKASID;
    private JRtaTextField jtfARZTID;
    private JRtaTextField jtfPREISGR;
    private JRtaTextField jtfHEIMBEW;
    private JRtaTextField jtfBEFREIT;
    private JRtaTextField jtfPOS1;
    private JRtaTextField jtfPOS2;
    private JRtaTextField jtfPOS3;
    private JRtaTextField jtfPOS4;
    private JRtaTextField jtfPREIS1;
    private JRtaTextField jtfPREIS2;
    private JRtaTextField jtfPREIS3;
    private JRtaTextField jtfPREIS4;
    private JRtaTextField jtfANLAGDAT;
    private JRtaTextField jtfANZKM;
    private JRtaTextField jtfPATID;
    private JRtaTextField jtfPATINT;
    private JRtaTextField jtfZZSTAT;
    private JRtaTextField jtfHEIMBEWPATSTAM;
    private JRtaTextField jtfICD10_1;
    private JRtaTextField jtfICD10_2;
    private List<JRtaTextField> jtfAll = new ArrayList<JRtaTextField>();
    
    private JRtaComboBox jcmbRKLASSE;
    private JRtaComboBox jcmbVERORD;
    private JRtaComboBox jcmbLEIST1;
    private JRtaComboBox jcmbLEIST2;
    private JRtaComboBox jcmbLEIST3;
    private JRtaComboBox jcmbLEIST4;
    private JRtaComboBox jcmbINDI;
    private JRtaComboBox jcmbBARCOD;
    private JRtaComboBox jcmbFARBCOD;
    private List<JRtaComboBox> jcmbAll = new ArrayList<JRtaComboBox>();
    
    private JRtaCheckBox jcbBEGRADR;
    private JRtaCheckBox jcbHAUSB;
    private JRtaCheckBox jcbVOLLHB;
    private JRtaCheckBox jcbTBANGEF;
    private JRtaCheckBox jcbHygienePausch;
    private List<JRtaCheckBox> jcbAll = new ArrayList<JRtaCheckBox>();

    private JTextArea jta = null;
    private JTextArea jtaNotizen;
    
    public JButton speichern = null;
    public JButton abbrechen = null;
    public JButton hmrcheck = null;
    
    private JLabel labKassen;
    private JLabel labArzt;
    private JLabel labArztLANr;
    private JLabel labArztChanged;      // Wird beim Einlesen eines Rez. festgestellt,
                                        //  das der Arzt im Rez != verordnArzt.getNachname() ist,
                                        //  wird das hierrueber dem Benutzer kenntlich gemacht.
    private Mandant mand = null;
    private Rezept rez;
    
    // Copied from orig - prob. can stay
    private boolean ctrlIsPressed = false;
    private Component eingabeRezDate = null;
    private Component eingabeBehFrequ = null;
    private Component eingabeVerordnArt = null;
    private Component eingabeVerordn1 = null;
    private Component eingabeICD = null;
    private Component eingabeDiag = null;
    private RehaTPEventClass rtp = null;
    
    private int hashOfFormVals = 0;                         // Used to determine changes & alert upon abort
    private int preisgruppe = -1;
    
    // Copied from orig - should be replace/depr.
    private boolean neu = false;
    private final JDialog popupDialog = new JDialog();
    private ArztVec verordnenderArzt = null;
    String[] strRezepklassenAktiv = null;
    private Disziplinen diszis = null;
    private boolean klassenReady = false;
    private boolean initReady = false;
    private int preisgruppen[] = { 0, 0, 0, 0, 0, 0, 0, 0 };
    private String aktuelleDisziplin = "";
    public Vector<Vector<String>> preisvec = null;
    private ArrayList<String> farbcodes = new ArrayList<>();
    private String rezKlasse = null;
    
    public RezeptEditorGUI(Rezept Rez, Boolean Neu, Mandant Mand){
        super();
        
        rez=Rez;
        neu = Neu;
        mand = Mand;
        // FIXME: This is a Q&D to bring popups to front - at least it works for "own" popups,
        //   but unfort. not for popups created e.g. by HMRCheck
        // The main cause seems to be that directly after creating a new popup, the main Rezept-Editor-Window somehow
        //   regains focus, shoving the popup to back until you click somewhere on the editor...
        popupDialog.setAlwaysOnTop(true);
        
        try {
            // this.rez = rez; // Lemmi 20110106 Wird auch fuer das Kopieren verwendet !!!!
            /**
             * A Q&D to fix 'hiding popup windows' (on Mac?)
             * Just use it in e.g. showOptionDialog as "parent" to get the message to front
             * JOptionPane.showOptionDialog(popupDialog, ...) instead of NULL - or sort out "default" :D
             */
            popupDialog.setAlwaysOnTop(true);
            logger.debug("From Rez: " + rez.toString());
            // rez = new Rezept(rez);
            verordnenderArzt = new ArztVec();
            // TODO: sets the classmember in Rezeptvector-class for later operations
            diszis = new Disziplinen();

            // TODO: old code also checked vec-size 0-length - can we safely omit it?
            // No - currently if old-vec was NULL, now RezNr. and other members may be NULL
            logger.debug("RezNr in constr. is " + rez.getRezNr());
            if (this.neu && rez.getRezNr() != null) {
                logger.debug("Setting diszi to " + RezTools.getDisziplinFromRezNr(rez.getRezNr()));
                aktuelleDisziplin = RezTools.getDisziplinFromRezNr(rez.getRezNr()); 
            }
            
            setName("RezeptNeuanlage");
            rtp = new RehaTPEventClass();
            rtp.addRehaTPEventListener(this);

            addKeyListener(new KeyLauscher());

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
                                                                       // Read-Only-Modus fuer das Rezept
                    for (JRtaTextField jtf : jtfAll) {
                        jtf.setEnabled(false);
                    }
                    /*
                     * Do we still need null-check?
                    for (int i = 0; i < jtf.length; i++) { // Lemmi Doku: alle Textfelder unbedienbar machen
                        if (jtf[i] != null) {
                            jtf[i].setEnabled(false);
                        }
                    }
                    */
                    for (JRtaCheckBox jcb : jcbAll) {
                        jcb.setEnabled(false);
                    }
                    for(JRtaComboBox jcmb : jcmbAll) {
                        jcmb.setEnabled(false);
                    }
                }
            }
            logger.debug("Will hash this: " + rez.toString() + " and get: " + rez.hashCode());
            // Lets assume we have copied the rez-content to the form, if we now copy the form back
            // to the rez, we should have all fields initialized...
            copyFormToRez(rez, false);
            hashOfFormVals = rez.hashCode();

        } catch (Exception ex) {
            logger.error("Fehler im Konstruktor RezeptEditorGUI: " + ex.getLocalizedMessage());
            JOptionPane.showMessageDialog(popupDialog,
                    "Fehler im Konstruktor RezeptEditor: " + ex.getLocalizedMessage());
        }
    }

    public void setzeFocus() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (neu) {
                    int aid, kid;
                    boolean beenden = false;
                    String meldung = "";
                    kid = StringTools.ZahlTest(jtfKASID.getText());
                    aid = StringTools.ZahlTest(jtfARZTID.getText());
                    if (kid < 0 && aid < 0) {
                        beenden = true;
                        meldung = "Achtung - dem Patientenstamm ist keine verwertbare Krankenkasse\n"
                                + "sowie kein verwertbarer Arzt zugeordnet\n\n"
                                + "Gehen Sie im Patientenstamm auf ->\u00c4ndern/Editieren<- und ordnen Sie verwertaber Daten zu!";
                    } else if (kid >= 0 && aid < 0) {
                        beenden = true;
                        meldung = "Achtung - dem Patientenstamm ist kein verwertbarer Arzt zugeordnet\n\n"
                                + "Gehen Sie im Patientenstamm auf ->\u00c4ndern/Editieren<- und ordnen Sie verwertaber Daten zu!";
                    } else if (kid < 0 && aid >= 0) {
                        beenden = true;
                        meldung = "Achtung - dem Patientenstamm ist keine verwertbare Krankenkasse zugeordnet\n\n"
                                + "Gehen Sie im Patientenstamm auf ->\u00c4ndern/Editieren<- und ordnen Sie verwertaber Daten zu!";
                    }
                    if (beenden) {
                        JOptionPane.showMessageDialog(popupDialog, meldung);
                        aufraeumen();
                        ((JXDialog) getParent().getParent()
                                               .getParent()
                                               .getParent()
                                               .getParent()).dispose();
                    } else {
                        holePreisGruppe(Integer.parseInt(
                                            jtfKASID.getText().trim()));
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                jcmbRKLASSE.requestFocusInWindow();
                            }
                        });
                    }
                    // else bedeutet nicht neu - sondern aendern
                } else {
                    int aid, kid;
                    kid = StringTools.ZahlTest(jtfKASID.getText());
                    aid = StringTools.ZahlTest(jtfARZTID.getText());
                    if (kid < 0) {
                        jtfKASID.setText(Integer.toString(Reha.instance.patpanel.kid));
                        jtfKTRAEG.setText(Reha.instance.patpanel.patDaten.get(13));
                    }
                    if (aid < 0) {
                        jtfARZTID.setText(Integer.toString(Reha.instance.patpanel.aid));
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            jtfKTRAEG.requestFocusInWindow();
                        }
                    });
                }

            }
        });
    }
    
    private JScrollPane getDatenPanel() { //Xs: 1             2     3      4     5                   6     7      8
        FormLayout lay = new FormLayout("right:max(80dlu;p), 4dlu, 60dlu, 5dlu, right:max(60dlu;p), 4dlu, 60dlu, right:5dlu",
             // Ys: 1. 2. 3. 4.     5. 6     7 8      9 10    11  12    13 14    15 16    17   18   19  20   21 22    23 24    25
                "p, 5dlu, p, 5dlu,  p, 10dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 10dlu, p, 10dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, "
                        +
                        // 26  27  28    29 30    31  32    33   34    35  36    37 38   39 40    41    42
                        "10dlu, p, 10dlu, p, 2dlu, p, 2dlu,  p,  10dlu, p, 10dlu, p,10dlu,p,10dlu,30dlu,2dlu");

        CellConstraints cc = new CellConstraints();
        PanelBuilder jpan = new PanelBuilder(lay);
        jpan.setDefaultDialogBorder();
        jpan.getPanel()
            .setOpaque(false);
        JScrollPane jscr = null;

        try {
            // Lemmi 20101231: Harte Index-Zahlen fuer "jtf" durch sprechende Konstanten
            // ersetzt !
            jtfKTRAEG = new JRtaTextField("NIX", false); // kasse/kostentraeger
            jtfAll.add(jtfKTRAEG);
            jtfARZT = new JRtaTextField("NIX", false); // arzt
            jtfAll.add(jtfARZT);
            jtfREZDAT = new JRtaTextField("DATUM", true); // rezeptdatum
            jtfAll.add(jtfREZDAT);
            jtfBEGINDAT = new JRtaTextField("DATUM", true); // spaetester beginn
            jtfAll.add(jtfBEGINDAT);
            jtfANZ1 = new JRtaTextField("ZAHLEN", true); // Anzahl 1
            jtfAll.add(jtfANZ1);
            jtfANZ2 = new JRtaTextField("ZAHLEN", true); // Anzahl 2
            jtfAll.add(jtfANZ2);
            jtfANZ3 = new JRtaTextField("ZAHLEN", true); // Anzahl 3
            jtfAll.add(jtfANZ3);
            jtfANZ4 = new JRtaTextField("ZAHLEN", true); // Anzahl 4
            jtfAll.add(jtfANZ4);
            jtfFREQ = new JRtaTextField("GROSS", true); // Frequenz
            jtfAll.add(jtfFREQ);
            jtfDAUER = new JRtaTextField("ZAHLEN", true); // Dauer
            jtfAll.add(jtfDAUER);
            jtfANGEL = new JRtaTextField("GROSS", true); // angelegt von
            jtfAll.add(jtfANGEL);
            jtfKASID = new JRtaTextField("GROSS", false); // kassenid
            jtfAll.add(jtfKASID);
            jtfARZTID = new JRtaTextField("GROSS", false); // arztid
            jtfAll.add(jtfARZTID);
            jtfPREISGR = new JRtaTextField("GROSS", false); // preisgruppe
            jtfAll.add(jtfPREISGR);
            jtfHEIMBEW = new JRtaTextField("GROSS", false); // heimbewohner
            jtfAll.add(jtfHEIMBEW);
            jtfBEFREIT = new JRtaTextField("GROSS", false); // befreit
            jtfAll.add(jtfBEFREIT);
            jtfPOS1 = new JRtaTextField("", false); // POS1
            jtfAll.add(jtfPOS1);
            jtfPOS2 = new JRtaTextField("", false); // POS2
            jtfAll.add(jtfPOS2);
            jtfPOS3 = new JRtaTextField("", false); // POS3
            jtfAll.add(jtfPOS3);
            jtfPOS4 = new JRtaTextField("", false); // POS4
            jtfAll.add(jtfPOS4);
            jtfPREIS1 = new JRtaTextField("", false); // PREIS1
            jtfAll.add(jtfPREIS1);
            jtfPREIS2 = new JRtaTextField("", false); // PREIS2
            jtfAll.add(jtfPREIS2);
            jtfPREIS3 = new JRtaTextField("", false); // PREIS3
            jtfAll.add(jtfPREIS3);
            jtfPREIS4 = new JRtaTextField("", false); // PREIS4
            jtfAll.add(jtfPREIS4);
            jtfANLAGDAT = new JRtaTextField("DATUM", false); // ANLAGEDATUM
            jtfAll.add(jtfANLAGDAT);
            jtfANZKM = new JRtaTextField("", false); // KILOMETER
            jtfAll.add(jtfANZKM);
            jtfPATID = new JRtaTextField("", false); // id von Patient
            jtfAll.add(jtfPATID);
            jtfPATINT = new JRtaTextField("", false); // pat_intern von Patient
            jtfAll.add(jtfPATINT);
            jtfZZSTAT = new JRtaTextField("", false); // zzstatus
            jtfAll.add(jtfZZSTAT);
            jtfHEIMBEWPATSTAM = new JRtaTextField("", false); // Heimbewohner aus PatStamm
            jtfAll.add(jtfHEIMBEWPATSTAM);
            jtfICD10_1 = new JRtaTextField("GROSS", false); // 1. ICD10-Code
            // JRtaTextField TF_icd10_1 = jtfICD10_1;
            jtfAll.add(jtfICD10_1);
            // MouseListener icd10Text = new Icd10Listener();
            // TF_icd10_1.addMouseListener(icd10Text );
            jtfICD10_2 = new JRtaTextField("GROSS", false); // 2. ICD10-Code
            // JRtaTextField TF_icd10_2 = jtfICD10;
            jtfAll.add(jtfICD10_2);
            // TF_icd10_2.addMouseListener(icd10Text );
            jcmbRKLASSE = new JRtaComboBox();
            strRezepklassenAktiv = diszis.getActiveRK();
            jcmbRKLASSE = diszis.getComboBoxActiveRK();

            if (SystemConfig.AngelegtVonUser) {
                jtfANGEL.setText(Reha.aktUser);
                jtfANGEL.setEditable(false);
            }

            jpan.addLabel("Rezeptklasse ausw\u00e4hlen", cc.xy(1, 3));
            jpan.add(jcmbRKLASSE, cc.xyw(3, 3, 5));
            jcmbAll.add(jcmbRKLASSE);
            jcmbRKLASSE.setActionCommand("rezeptklasse");
            jcmbRKLASSE.addActionListener(e -> actionRezeptklasse());
            allowShortCut(jcmbRKLASSE, "RezeptClass");
            /********************/

            // Original code checked for empty vector - not sure how to do this now...
            // if (rez == null) {
            if (rez.getPatIntern() == 0) {
                jcmbRKLASSE.setSelectedItem(SystemConfig.initRezeptKlasse);                    
            } else {
                String rezClassInVO = rez.getRezClass();
                for (int i = 0; i < strRezepklassenAktiv.length; i++) {
                    if (strRezepklassenAktiv[i].equals(rezClassInVO)) {
                        jcmbRKLASSE.setSelectedIndex(i);
                    }
                }
            }
            if (!this.neu) {
                jcmbRKLASSE.setEnabled(false);
            }

            jpan.addSeparator("Rezeptkopf", cc.xyw(1, 5, 7));

            labKassen = new JLabel("Kostentr\u00e4ger");
            labKassen.setIcon(SystemConfig.hmSysIcons.get("kleinehilfe"));
            labKassen.setHorizontalTextPosition(JLabel.LEFT);
            labKassen.addMouseListener(new MouseAdapter() {
                // originally this used mousePressed - wanted to see if https://github.com/mzmine/mzmine2/issues/20 was
                // also applicable to us - seems to have made no difference...
                public void mouseClicked(MouseEvent ev) {
                    if (!Rechte.hatRecht(Rechte.Rezept_editvoll, false)) {
                        return;
                    }
                    if (jtfKTRAEG.getText()
                                    .trim()
                                    .startsWith("?")) {
                        jtfKTRAEG.requestFocusInWindow();
                    } else {
                        jtfKTRAEG.setText("?" + jtfKTRAEG.getText()
                                                               .trim());
                        jtfKTRAEG.requestFocusInWindow();
                    }
                    String[] suchkrit = new String[] { jtfKTRAEG.getText()
                                                                   .replace("?", ""),
                            jtfKASID.getText() };
                    jtfKTRAEG.setText(String.valueOf(suchkrit[0]));
                    kassenAuswahl(suchkrit);
                }
            });

            jtfKTRAEG.setName("ktraeger");
            jtfKTRAEG.addKeyListener(new KeyLauscher());
            allowShortCut(jtfKTRAEG, "ktraeger");
            jpan.add(labKassen, cc.xy(1, 7));
            jpan.add(jtfKTRAEG, cc.xy(3, 7));
            
            labArztLANr = new JLabel("123456789");
            labArzt = new JLabel("verordn. Arzt");
            labArzt.setIcon(SystemConfig.hmSysIcons.get("kleinehilfe"));
            labArzt.setHorizontalTextPosition(JLabel.LEFT);
            labArzt.addMouseListener(new MouseAdapter() {
                // originally this used mousePressed - wanted to see if https://github.com/mzmine/mzmine2/issues/20 was
                // also applicable to us - seems to have made no difference...
                public void mouseClicked(MouseEvent ev) {
                    if (!Rechte.hatRecht(Rechte.Rezept_editvoll, false)) {
                        return;
                    }
                    if (jtfARZT.getText()
                                  .trim()
                                  .startsWith("?")) {
                        jtfARZT.requestFocusInWindow();
                    } else {
                        jtfARZT.setText("?" + jtfARZT.getText()
                                                           .trim());
                        jtfARZT.requestFocusInWindow();
                    }
                    String[] suchkrit = new String[] { jtfARZT.getText()
                                                                 .replace("?", ""),
                            jtfARZTID.getText() };
                    jtfARZT.setText(String.valueOf(suchkrit[0]));
                    arztAuswahl(suchkrit);
                }
            });
            labArztChanged = new JLabel("");
            labArztChanged.setToolTipText("Der Eintrag hat sich nicht geaendert.");
            labArztChanged.setVisible(false);

            jtfARZT.setName("arzt");
            jtfARZT.addKeyListener(new KeyLauscher());
            
            jpan.addLabel("Arzt-LANr", cc.xy(5, 6));
            jpan.add(labArztLANr, cc.xy(7, 6));
            // jpan.addLabel(verordnenderArzt.getLANR(), cc.xy(7, 6));
            jpan.add(labArzt, cc.xy(5, 7));
            jpan.add(jtfARZT, cc.xy(7, 7));
            jpan.add(labArztChanged, cc.xy(8, 7));

            jtfREZDAT.setName("rez_datum");
            allowShortCut(jtfREZDAT, "rez_datum");
            jpan.addLabel("Rezeptdatum", cc.xy(1, 9));
            jpan.add(jtfREZDAT, cc.xy(3, 9));
            eingabeRezDate = jpan.add(jtfREZDAT, cc.xy(3, 9));

            allowShortCut(jtfBEGINDAT, "lastdate");
            jpan.addLabel("sp\u00e4tester Beh.Beginn", cc.xy(5, 9));
            jpan.add(jtfBEGINDAT, cc.xy(7, 9));

            jcmbVERORD = new JRtaComboBox(
                    new String[] { "Erstverordnung", "Folgeverordnung", "au\u00dferhalb des Regelfalles" });
            jcmbVERORD.setActionCommand("verordnungsart");
            jcmbVERORD.addActionListener(e -> actionVerordnungsArt());
            allowShortCut(jcmbVERORD, "selArtDerVerordn");
            jpan.addLabel("Art d. Verordn.", cc.xy(1, 11));
            eingabeVerordnArt = jpan.add(jcmbVERORD, cc.xy(3, 11));

            jcbBEGRADR = new JRtaCheckBox("vorhanden");
            jcbBEGRADR.setOpaque(false);
            jcbBEGRADR.setEnabled(false);
            allowShortCut(jcbBEGRADR, "adrCheck");
            jpan.addLabel("Begr\u00fcnd. f\u00fcr adR", cc.xy(5, 11));
            jpan.add(jcbBEGRADR, cc.xy(7, 11));
            jcbAll.add(jcbBEGRADR);

            jcbHAUSB = new JRtaCheckBox("Ja / Nein");
            jcbHAUSB.setOpaque(false);
            jcbHAUSB.setActionCommand("Hausbesuche");
            jcbHAUSB.addActionListener(e -> actionHausbesuche());
            allowShortCut(jcbHAUSB, "hbCheck");
            jpan.addLabel("Hausbesuch", cc.xy(1, 13));
            jpan.add(jcbHAUSB, cc.xy(3, 13));
            jcbAll.add(jcbHAUSB);

            jcbVOLLHB = new JRtaCheckBox("abrechnen");
            jcbVOLLHB.setOpaque(false);
            jcbVOLLHB.setToolTipText("Nur aktiv wenn Patient Heimbewohner und Hausbesuch angekreuzt");
            jpan.addLabel("volle HB-Geb\u00fchr", cc.xy(5, 13));
            if (neu) {
                jcbVOLLHB.setEnabled(false);
                jcbVOLLHB.setSelected(false);
            } else {
                if (Reha.instance.patpanel.patDaten.get(44)
                                                   .equals("T")) {
                    // Wenn Heimbewohner
                    if (rez.isHausBesuch()) {
                        jcbVOLLHB.setEnabled(true);
                        jcbVOLLHB.setSelected(rez.isHbVoll());
                    } else {
                        jcbVOLLHB.setEnabled(false);
                        jcbVOLLHB.setSelected(false);
                    }
                } else {
                    // Wenn kein(!!) Heimbewohner
                    if (rez.isHausBesuch()) {
                        jcbVOLLHB.setEnabled(false);
                        jcbVOLLHB.setSelected(true);
                    } else {
                        jcbVOLLHB.setEnabled(false);
                        jcbVOLLHB.setSelected(false);
                    }
                }
            }
            allowShortCut(jcbVOLLHB, "hbVollCheck");
            jpan.add(jcbVOLLHB, cc.xy(7, 13));
            jcbAll.add(jcbVOLLHB);

            jcbTBANGEF = new JRtaCheckBox("angefordert");
            jcbTBANGEF.setOpaque(false);
            jpan.addLabel("Therapiebericht", cc.xy(1, 15));
            jcbTBANGEF.addKeyListener(new KeyLauscher());
            jpan.add(jcbTBANGEF, cc.xy(3, 15));
            jcbAll.add(jcbTBANGEF);

            jcbHygienePausch = new JRtaCheckBox("abrechnen");
            jcbHygienePausch.setOpaque(false);
            jcbHygienePausch.setToolTipText("nur zul\u00e4ssig bei Abrechnung zwischen 05.05.2020 und 30.09.2020");
            jpan.addLabel("Hygiene-Mehraufwand", cc.xy(5, 15));
            if (neu) {
                jcbHygienePausch.setSelected(false);
            } else {
               
                jcbHygienePausch.setSelected((rez.usePauschale() ));
            }
            allowShortCut((Component) jcbHygienePausch, "hygPausch");
            jpan.add(jcbHygienePausch, cc.xy(7, 15));
            jcbAll.add(jcbHygienePausch);
            
            jpan.addSeparator("Verordnete Heilmittel", cc.xyw(1, 17, 7));

            jtfANZ1.setName("anzahl1");
            jtfANZ1.addFocusListener(this);
            jtfANZ1.addKeyListener(new KeyLauscher());
            jpan.addLabel("Anzahl / Heilmittel 1", cc.xy(1, 19));
            eingabeVerordn1 = jpan.add(jtfANZ1, cc.xy(3, 19));
            jcmbLEIST1 = new JRtaComboBox();
            jcmbLEIST1.setActionCommand("leistung1");
            jcmbLEIST1.addActionListener(e -> actionLeistung(e));
            allowShortCut(jcmbLEIST1, "leistung1");
            jpan.add(jcmbLEIST1, cc.xyw(5, 19, 3));
            jcmbAll.add(jcmbLEIST1);

            jpan.addLabel("Anzahl / Heilmittel 2", cc.xy(1, 21));
            jtfANZ2.addKeyListener(new KeyLauscher());
            jpan.add(jtfANZ2, cc.xy(3, 21));
            jcmbLEIST2 = new JRtaComboBox();
            jcmbLEIST2.setActionCommand("leistung2");
            jcmbLEIST2.addActionListener(e -> actionLeistung(e));
            allowShortCut(jcmbLEIST2, "leistung2");
            jpan.add(jcmbLEIST2, cc.xyw(5, 21, 3));
            jcmbAll.add(jcmbLEIST2);

            jpan.addLabel("Anzahl / Heilmittel 3", cc.xy(1, 23));
            jtfANZ3.addKeyListener(new KeyLauscher());
            jpan.add(jtfANZ3, cc.xy(3, 23));
            jcmbLEIST3 = new JRtaComboBox();
            jcmbLEIST3.setActionCommand("leistung3");
            jcmbLEIST3.addActionListener(e -> actionLeistung(e));
            allowShortCut(jcmbLEIST3, "leistung3");
            jpan.add(jcmbLEIST3, cc.xyw(5, 23, 3));
            jcmbAll.add(jcmbLEIST3);

            jpan.addLabel("Anzahl / Heilmittel 4", cc.xy(1, 25));
            jtfANZ4.addKeyListener(new KeyLauscher());
            jpan.add(jtfANZ4, cc.xy(3, 25));
            jcmbLEIST4 = new JRtaComboBox();
            jcmbLEIST4.setActionCommand("leistung4");
            jcmbLEIST4.setName("leistung4");
            jcmbLEIST4.addActionListener(e -> actionLeistung(e));
            jpan.add(jcmbLEIST4, cc.xyw(5, 25, 3));
            jcmbAll.add(jcmbLEIST4);

            jpan.addSeparator("Durchf\u00fchrungsbestimmungen", cc.xyw(1, 27, 7));

            jtfFREQ.addKeyListener(new KeyLauscher());
            jpan.addLabel("Behandlungsfrequenz", cc.xy(1, 29));
            eingabeBehFrequ = jpan.add(jtfFREQ, cc.xy(3, 29));

            jpan.addLabel("Dauer der Behandl. in Min.", cc.xy(5, 29));
            jtfDAUER.addKeyListener(new KeyLauscher());
            jpan.add(jtfDAUER, cc.xy(7, 29));

            jpan.addLabel("Indikationsschl\u00fcssel", cc.xy(1, 31));
            jcmbINDI = new JRtaComboBox();
            jcmbINDI.addKeyListener(new KeyLauscher());
            allowShortCut(jcmbINDI,"Indikationsschluessel");
            jpan.add(jcmbINDI, cc.xy(3, 31));
            jcmbAll.add(jcmbINDI);

            klassenReady = true;
            this.fuelleIndis((String) jcmbRKLASSE.getSelectedItem());

            jpan.addLabel("Barcode-Format", cc.xy(5, 31));
            jcmbBARCOD = new JRtaComboBox(SystemConfig.rezBarCodName);
            jcmbBARCOD.addKeyListener(new KeyLauscher());
            jpan.add(jcmbBARCOD, cc.xy(7, 31));
            jcmbAll.add(jcmbBARCOD);

            jpan.addLabel("FarbCode im TK", cc.xy(1, 33));
            jcmbFARBCOD = new JRtaComboBox();
            jcmbFARBCOD.addKeyListener(new KeyLauscher());
            macheFarbcodes();
            jpan.add(jcmbFARBCOD, cc.xy(3, 33));
            jcmbAll.add(jcmbFARBCOD);

            jpan.addLabel("Angelegt von", cc.xy(5, 33));
            jtfANGEL.addKeyListener(new KeyLauscher());
            jpan.add(jtfANGEL, cc.xy(7, 33));

            jpan.addSeparator("ICD-10 Codes", cc.xyw(1, 35, 7));
            jpan.addLabel("1. ICD-10-Code", cc.xy(1, 37));
            allowShortCut(jtfICD10_1, "icd10");
            eingabeICD = jpan.add(jtfICD10_1, cc.xy(3, 37));

            jpan.addLabel("2. ICD-10-Code", cc.xy(5, 37));
            allowShortCut(jtfICD10_2, "icd10_2");
            jpan.add(jtfICD10_2, cc.xy(7, 37));

            jpan.addSeparator("\u00c4rztliche Diagnose laut Rezept", cc.xyw(1, 39, 7));
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
            
            if (neu) {
                // TODO: find a better replacement for isEmpty-check
                // if (rez.isEmpty()) {
                if (rez.getPreisGruppe() == -1) {
                    logger.debug("Rezept was determinded to be empty");
                    rez = initRezeptNeu(); // McM:hier myRezept mit Pat-Daten, PG, ... initialisieren
                    holePreisGruppe(Integer.parseInt(Reha.instance.patpanel.patDaten.get(68)
                                                                        .trim())); // setzt jtfPREISGR u.
                                                                                  // this.preisgruppe
                    ladePreisliste(jcmbRKLASSE.getSelectedItem()
                                                      .toString()
                                                      .trim(),
                            preisgruppen[getPgIndex()]); // fuellt jcmb[cLEIST1..4] u.
                                                                              // jcmbBARCOD
                } else { // myRezept enthaelt Daten
                    logger.debug("Rezept was determined to contain data");
                    try {
                        // TODO: Check if we can safely use rez.getHMKX here - or if we even should at all
                        String[] xartdbeh = new String[] { rez.getHMKuerzel1(), rez.getHMKuerzel2(),
                                rez.getHMKuerzel3(), rez.getHMKuerzel4() };
                        JRtaComboBox[] jcmb = new JRtaComboBox[] { jcmbLEIST1, jcmbLEIST2, jcmbLEIST3, jcmbLEIST4 };
                        initRezeptKopie(rez);
                        this.holePreisGruppe(rez.getkId());
                        this.ladePreisliste(jcmbRKLASSE.getSelectedItem()
                                                          .toString()
                                                          .trim(),
                                preisgruppen[getPgIndex()]);
                        for (int i = 1; i <= 4; i++) {
                            if (xartdbeh[i].equals("")) {
                                jcmb[i].setSelectedIndex(0);
                            } else {
                                jcmb[i].setSelectedVecIndex(1, xartdbeh[i]);
                            }
                        }
                        jcmbINDI.setSelectedItem(rez.getIndikatSchl());
                    } catch (Exception ex) {
                        logger.debug("Trouble in editing (a copied?) Rezept - " + ex.getLocalizedMessage());
                    }

                }
            } else {
                logger.debug("Rezept didn't have new-bool set...");
                this.holePreisGruppe(rez.getkId());
                this.ladePreisliste(jcmbRKLASSE.getSelectedItem()
                                                  .toString()
                                                  .trim(),
                        preisgruppen[getPgIndex()]);
                this.fuelleIndis(jcmbRKLASSE.getSelectedItem()
                                               .toString()
                                               .trim());
            }
            verordnenderArzt.init(Integer.toString(rez.getArztId()));
            copyRezToForm();

            jscr.validate();
        } catch (Exception ex) {
            logger.error("Could not create Rezeptfenster");
            logger.error(ex.getLocalizedMessage());
            JOptionPane.showMessageDialog(popupDialog, "Fehler in der Erstellung des Rezeptfensters\n" + ex.getMessage());
        }

        // Lemmi 20101231: Merken der Originalwerte der eingelesenen Textfelder
        // logger.debug("Will hash this: " + rez.toString() + " to " + rez.hashCode());
        hashOfFormVals = rez.hashCode();

        return jscr;
    }
    
    public JXPanel getButtonPanel() {
        JXPanel jpan = JCompTools.getEmptyJXPanel();
        // RezNeuanlageAL aListener = new RezNeuanlageAL();
        jpan.addKeyListener(new KeyLauscher());
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
        speichern.addActionListener(e -> actionSpeichern());
        speichern.addKeyListener(new KeyLauscher());
        speichern.setMnemonic(KeyEvent.VK_S);
        jpan.add(speichern, cc.xy(2, 2));

        hmrcheck = new JButton("HMR-Check");
        hmrcheck.setActionCommand("hmrcheck");
        hmrcheck.addActionListener(e -> actionHmrCheck());
        hmrcheck.addKeyListener(new KeyLauscher());
        hmrcheck.setMnemonic(KeyEvent.VK_H);
        jpan.add(hmrcheck, cc.xy(4, 2));

        abbrechen = new JButton("abbrechen");
        abbrechen.setActionCommand("abbrechen");
        abbrechen.addActionListener(e -> actionAbbrechen());
        abbrechen.addKeyListener(new KeyLauscher());
        abbrechen.setMnemonic(KeyEvent.VK_A);
        jpan.add(abbrechen, cc.xy(6, 2));

        return jpan;
    }
    
    private void copyRezToForm() {
        // TODO: find a replacement for the following test:
        // String test = StringTools.NullTest(rez);
        jtfKTRAEG.setText(rez.getKTraegerName()); // kasse
        // test = StringTools.NullTest(rez.getKtraeger());
        jtfKASID.setText(Integer.toString(rez.getkId())); // kid
        // test = StringTools.NullTest(rez.getArzt());
        // jtfARZT.setText(verordnenderArzt.getNNameLanr()); // arzt - LANR
        jtfARZT.setText(verordnenderArzt.getNName());
        if (! rez.getArzt().equals(verordnenderArzt.getNName())) {
            labArztChanged.setText("!");
            labArztChanged.setVisible(true);
            labArztChanged.setToolTipText("Alter Wert: " + rez.getArzt());
            logger.info("Das \"Arzt\"-Feld wird bei Speicherung überschrieben. Alter Wert: " + rez.getArzt() 
                                                                            + " neuer Wert: " + verordnenderArzt.getNName());
        } else {
            labArztChanged.setText("");
            labArztChanged.setVisible(false);
            labArztChanged.setToolTipText("Allet jut");
        }
        
        labArztLANr.setText(verordnenderArzt.getLANR());
        // test = StringTools.NullTest(rez.getArztId());
        jtfARZTID.setText(String.valueOf(rez.getArztId())); // arztid
        // test = StringTools.NullTest(rez.getRezeptDatum());
        if (rez.getRezDatum() != null ) {
            jtfREZDAT.setText(rez.getRezDatum().format(DateTimeFormatters.ddMMYYYYmitPunkt));
        }
        // test = StringTools.NullTest(rez.getLastdate());
        if (rez.getLastDate() != null) {
            jtfBEGINDAT.setText(rez.getLastDate().format(DateTimeFormatters.ddMMYYYYmitPunkt));
        }
        int itest = rez.getRezeptArt();
        if (itest >= 0) {
            jcmbVERORD.setSelectedIndex(itest);
        }
        jcbBEGRADR.setSelected(rez.isBegruendADR());
        jcbHAUSB.setSelected(rez.isHausBesuch());

        jcbVOLLHB.setSelected(rez.isHbVoll());

        jcbTBANGEF.setSelected(rez.isArztBericht());
        
        jcbHygienePausch.setSelected(rez.usePauschale());

        jtfANZ1.setText(Integer.toString(rez.getBehAnzahl1()));
        jtfANZ2.setText(Integer.toString(rez.getBehAnzahl2()));
        jtfANZ3.setText(Integer.toString(rez.getBehAnzahl3()));
        jtfANZ4.setText(Integer.toString(rez.getBehAnzahl4()));

        // itest = rez.getArtDerBehandlung(1);
        jcmbLEIST1.setSelectedIndex(leistungTesten(0, rez.getArtDerBeh1()));
        // itest = rez.getArtDerBehandlung(2);
        jcmbLEIST2.setSelectedIndex(leistungTesten(1, rez.getArtDerBeh2()));
        // itest = StringTools.ZahlTest(rez.getArtDBehandl(3));
        jcmbLEIST3.setSelectedIndex(leistungTesten(2, rez.getArtDerBeh3()));
        // itest = StringTools.ZahlTest(rez.getArtDBehandl(4));
        jcmbLEIST4.setSelectedIndex(leistungTesten(3, rez.getArtDerBeh4()));

        String test = StringTools.NullTest(rez.getFrequenz());
        jtfFREQ.setText(test);
        test = StringTools.NullTest(rez.getDauer());
        jtfDAUER.setText(test);

        test = StringTools.NullTest(rez.getIndikatSchl());
        jcmbINDI.setSelectedItem(test);

        itest = rez.getBarcodeform();
        if (itest >= 0) {
            jcmbBARCOD.setSelectedIndex(itest);
        } else {
            rez.setBarcodeform(jcmbBARCOD.getSelectedIndex()); // default wird in ladePreisliste() gesetzt
        }

        test = StringTools.NullTest(rez.getAngelegtVon());
        jtfANGEL.setText(test);
        if (!test.trim()
                 .equals("")) {
            jtfANGEL.setEnabled(false);
        }
        jta.setText(StringTools.NullTest(rez.getDiagnose()));
        if (!jtfKASID.getText()
                        .equals("")) {
            holePreisGruppe(Integer.parseInt(
                                jtfKASID.getText().trim()));
        } else {
            JOptionPane.showMessageDialog(popupDialog, "Ermittlung der Preisgruppen erforderlich");
        }

        jtfHEIMBEW.setText(Reha.instance.patpanel.patDaten.get(44)); // heimbewohn
        jtfBEFREIT.setText(Reha.instance.patpanel.patDaten.get(30)); // befreit
        jtfANZKM.setText(Reha.instance.patpanel.patDaten.get(48)); // kilometer
        jtfPATID.setText(Integer.toString(rez.getPatId()));
        jtfPATINT.setText(Integer.toString(rez.getPatIntern()));

        // ICD-10
        jtfICD10_1.setText(rez.getIcd10());
        jtfICD10_2.setText(rez.getIcd10_2());

        itest = rez.getFarbcode();
        if (itest >= 0) {
            jcmbFARBCOD.setSelectedItem(SystemConfig.vSysColsBedeut.get(itest));
        }

    }

    /***********
     *
     * laedt die Daten aus den Dialog-Feldern des Rezepts erstmalig in die
     * Rezept-Instanz
     * @param thisRezept
     */
    private void copyFormToRez1stTime(Rezept thisRezept) {
        logger.debug("AnzahlHM1 = \"" + jtfANZ1.getText() + "\".");
        // TODO: Why? This will be set in copyFormToRez anyhow...
        thisRezept.setBehAnzahl1(Integer.parseInt(jtfANZ1.getText().trim().isEmpty() ? "0" : jtfANZ1.getText().trim()));
        copyFormToRez(thisRezept, true);
    }

    /***********
    *
    * laedt die Daten aus den Dialog-Feldern des Rezepts in die Rezept-Instanz
    *  und prueft dabei gewisse Mindestanforderungen und ergaenzt ggf. Felder
    *  
    * @param thisRezept will be filled with the data
    * @param withSanity If true do some sanity-checks & fill in some data
    */
    private void copyFormToRez(Rezept thisRezept, boolean withSanity) {
     // TODO: this method needs properly adjusting to Rezept-class
        try {
            logger.debug("Sanity check: " + withSanity);
            if (withSanity && !komplettTest()) {
                logger.error("Komplettest nicht bestanden...");
                return;
            }
            setCursor(Cursors.wartenCursor);
            String stest = "";
            int itest = -1;
 
            thisRezept.setKTraegerName(jtfKTRAEG.getText());
            thisRezept.setkId(Integer.parseInt(jtfKASID.getText()));
            logger.debug("Set KTraeger-Infos");
            // TODO: sort out what really needs to go in here...
            // If we already have 'verordnenderArzt, we actually should use that...
            thisRezept.setArzt((jtfARZT.getText().split(" - "))[0].trim());     // Correct "old" "Arzt - LANR" entries
            thisRezept.setArztId(Integer.parseInt(jtfARZTID.getText()));
            logger.debug("Set Arzt-Infos");
 
            stest = jtfREZDAT.getText()
                                .trim();
            LocalDate rezDat;
            if (stest.equals(".  .")) {
                stest = DatFunk.sHeute();
                rezDat = LocalDate.now();
            } else {
                rezDat = LocalDate.parse(jtfREZDAT.getText().trim(), DateTimeFormatters.dMYYYYmitPunkt);
            }
            
            boolean neuerpreis = RezTools.neuePreisNachRezeptdatumOderStichtag(aktuelleDisziplin, preisgruppe,
                    String.valueOf(stest), false, Reha.instance.patpanel.rezAktRez);
            if (withSanity) {
                    thisRezept.setRezDatum(rezDat);
                    setRezDatInTable(stest);
                    LocalDate stest2 = RezeptFensterTools.chkLastBeginDat(rezDat, jtfBEGINDAT.getText().trim(),
                                                                                   jtfPREISGR.getText(), aktuelleDisziplin);
                    setLastDatInTable(stest2);
                    thisRezept.setLastDate(stest2);
                    thisRezept.setLastEdDate(LocalDate.now());
                    thisRezept.setLastEditor(Reha.aktUser);
            }
            thisRezept.setRezeptArt(jcmbVERORD.getSelectedIndex());
            thisRezept.setBegruendADR(jcbBEGRADR.isSelected());
            thisRezept.setHausBesuch(jcbHAUSB.isSelected());
            if (thisRezept.isHausBesuch()) {
                String anzHB = String.valueOf(thisRezept.getAnzahlHb());
                if (!anzHB.equals(jtfANZ1.getText())) {
                    int frage = JOptionPane.showConfirmDialog(popupDialog, "Achtung!\n\n"
                            + "Die Anzahl Hausbesuche = " + anzHB + "\n" 
                            + "Die Anzahl des ersten Heilmittels = " + jtfANZ1.getText() + "\n\n"
                            + "Soll die Anzahl Hausbesuche ebenfalls auf " + jtfANZ1.getText() + " gesetzt werden?",
                            "Benutzeranfrage", JOptionPane.YES_NO_OPTION);
                    if (frage == JOptionPane.YES_OPTION) {
                        thisRezept.setAnzahlHb(Integer.parseInt(jtfANZ1.getText()));
                    }
                }
            }
            thisRezept.setArztBericht(jcbTBANGEF.isSelected());
            thisRezept.setBehAnzahl1(Integer.parseInt(jtfANZ1.getText()));
            thisRezept.setBehAnzahl2(Integer.parseInt(jtfANZ2.getText()));
            thisRezept.setBehAnzahl3(Integer.parseInt(jtfANZ3.getText()));
            thisRezept.setBehAnzahl4(Integer.parseInt(jtfANZ4.getText()));
 
            JRtaComboBox[] jcmb = new JRtaComboBox[] {jcmbLEIST1, jcmbLEIST2, jcmbLEIST3, jcmbLEIST4};
            for (int i = 0; i < 4; i++) {
                int idxVec = i + 1;
                itest = jcmb[i].getSelectedIndex();
                if (itest > 0) { // 0 ist der Leereintrag!
                    int idxPv = itest - 1;
                    thisRezept.setArtDerBeh(idxVec, Integer.parseInt(preisvec.get(idxPv)
                                                            .get(9)));
                    thisRezept.setPreis(idxVec,new Money(preisvec.get(idxPv)
                                                      .get(neuerpreis ? 3 : 4)));
                    thisRezept.setHMPos(idxVec, preisvec.get(idxPv)
                                                      .get(2));
                    thisRezept.setHMKuerzel(idxVec, preisvec.get(idxPv)
                                                       .get(1));
                } else {
                    thisRezept.setArtDerBeh(idxVec, 0);
                    thisRezept.setPreis(idxVec, new Money("0.00"));
                    thisRezept.setHMPos(idxVec, "");
                    thisRezept.setHMKuerzel(idxVec, "");
                }
            }
 
            thisRezept.setFrequenz(jtfFREQ.getText());
            thisRezept.setDauer(jtfDAUER.getText());
            if (jcmbINDI.getSelectedIndex() > 0) {
                thisRezept.setIndikatSchl((String) jcmbINDI.getSelectedItem());
            } else {
                // should this really be stored in DB - wouldn't an empty or null be better?
                thisRezept.setIndikatSchl("kein IndiSchl.");
            }
 
            thisRezept.setBarcodeform(jcmbBARCOD.getSelectedIndex());
            thisRezept.setAngelegtVon(jtfANGEL.getText());
            thisRezept.setPreisGruppe(Integer.parseInt(jtfPREISGR.getText()));
 
            if (jcmbFARBCOD.getSelectedIndex() > 0) {
                thisRezept.setFarbcode(13 + jcmbFARBCOD.getSelectedIndex());
            } else {
                thisRezept.setFarbcode(-1);
            }
            //// System.out.println("Speichern bestehendes Rezept -> Preisgruppe =
            //// "+jtfPREISGR.getText());
            Integer izuzahl = Integer.valueOf(jtfPREISGR.getText());
            int szzstatus = Zuzahlung.ZZSTATUS_NOTSET;
 
            boolean unter18 = false;
            for (int i = 0; i < 1; i++) {
                if (SystemPreislisten.hmZuzahlRegeln.get(aktuelleDisziplin)
                                                    .get(izuzahl - 1) <= 0) {
                    szzstatus = Zuzahlung.ZZSTATUS_BEFREIT;
                    break;
                }
                if (aktuelleDisziplin.equals("Reha")) {
                    szzstatus = Zuzahlung.ZZSTATUS_BEFREIT;
                    break;
                }
                if (aktuelleDisziplin.equals("Rsport") || aktuelleDisziplin.equals("Ftrain")) {
                    szzstatus = Zuzahlung.ZZSTATUS_BEFREIT;
                    break;
                }
                //// System.out.println("ZuzahlStatus = Zuzahlung (zunaechst) erforderlich, pruefe
                //// ob befreit oder unter 18");
                if (Reha.instance.patpanel.patDaten.get(30)
                                                   .equals("T")) {
                    // System.out.println("aktuelles Jahr ZuzahlStatus = Patient ist befreit");
                    if (thisRezept.isRezBez()) {
                        szzstatus = Zuzahlung.ZZSTATUS_OK;
                    } else {
 
                        // if (RezTools.mitJahresWechsel(thisRezept.getRezDatum())) {
                        if (thisRezept.getRezDatum().getYear() 
                                                    - Integer.parseInt(SystemConfig.aktJahr) <0) {
 
                            String vorjahr = Reha.instance.patpanel.patDaten.get(69);
                            if (vorjahr.trim()
                                       .equals("")) {
                                // Nur einspringen wenn keine Vorjahrbefreiung vorliegt.
                                // Tabelle mit Einzelterminen auslesen ob Saetze vorhanden
                                // wenn Saetze = 0 und bereits im Befreiungszeitraum dann "0", ansonsten "2"
                                // Wenn Saetze > 0 dann ersten Satz auslesen Wenn Datum < Befreiung-ab dann "2"
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
                                            szzstatus = Zuzahlung.ZZSTATUS_BEFREIT;
                                        } else {
                                            // Behandlung liegt vor befr_ab
                                            szzstatus = Zuzahlung.ZZSTATUS_NOTOK;
                                        }
                                    } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(popupDialog,
                                                "Fehler:\nBefreit ab, im Patientenstamm nicht oder falsch eingetragen");
                                    }
 
                                } else {
                                    // es sind noch keine Saetze verzeichnet
                                    if (DatFunk.TageDifferenz(
                                            DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(41)),
                                            DatFunk.sHeute()) >= 0) {
                                        // Behandlung muss nach befr_ab liegen
                                        szzstatus = Zuzahlung.ZZSTATUS_BEFREIT;
                                    } else {
                                        // Behandlung kann auch vor befr_ab liegen
                                        szzstatus = Zuzahlung.ZZSTATUS_NOTOK;
                                    }
                                }
                            } else {
                                szzstatus = Zuzahlung.ZZSTATUS_BEFREIT;
                            }
                        } else {
                            szzstatus = Zuzahlung.ZZSTATUS_BEFREIT;
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
                        JOptionPane.showMessageDialog(popupDialog,
                                "Achtung es sind noch " + (tage * -1) + " Tage bis zur Vollj\u00e4hrigkeit\n"
                                + "Unter Umst\u00e4nden wechselt der Zuzahlungsstatus im Verlauf dieses Rezeptes");
                        szzstatus = Zuzahlung.ZZSTATUS_BALD18;
                    } else {
                        szzstatus = Zuzahlung.ZZSTATUS_BEFREIT;
                    }
                    unter18 = true;
                    break;
                }
                /**********************/
                if (thisRezept.isRezBez() || (thisRezept.getRezGeb().isMoreThan(new Money("0.00")))) {
                    szzstatus = Zuzahlung.ZZSTATUS_OK;
                } else {
                    // hier testen ob erster Behandlungstag bereits ab dem Befreiungszeitraum
                    szzstatus = Zuzahlung.ZZSTATUS_NOTOK;
                }
            }
            /******/
 
            String[] lzv = RezeptFensterTools.holeLFV("anamnese", "pat5", "pat_intern", jtfPATINT.getText(), rezKlasse.toUpperCase()
                                                                                                      .substring(0, 2));
            if (!lzv[0].isEmpty()) {
                if (!jta.getText()
                        .contains(lzv[0])) {
                    int frage = JOptionPane.showConfirmDialog(popupDialog,
                            "F\u00fcr den Patient ist eine Langfristverordnung eingetragen die diese "
                            + "Verordnung noch nicht einschlie\u00dft.\n\n"
                            + lzv[1] + "\n\nWollen Sie diesen Eintrag dieser Verordnung zuweisen?",
                            "Achtung wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
                    if (frage == JOptionPane.YES_OPTION) {
                        jta.setText(jta.getText() + "\n" + lzv[0]);
                    }
                }
            }
            /*****/
 
            thisRezept.setUnter18(unter18); // oben schon ermittelt
            thisRezept.setZZStatus(szzstatus);
            thisRezept.setDiagnose(StringTools.Escaped(jta.getText()));
            thisRezept.setJahrfrei(Reha.instance.patpanel.patDaten.get(69)); // (?) falls seit Rezeptanlage geaendert
                                                                              // (?) (nicht editierbar -> kann in's
                                                                              // 'initRezeptAll')
            thisRezept.setPauschale(jcbHygienePausch.isSelected() ? true : false);
            // Why is this a text-field?
            thisRezept.setHeimbewohn("T".equals(jtfHEIMBEW.getText())); // dito
            
            thisRezept.setHbVoll(jcbVOLLHB.isSelected()); // dito
            stest = jtfANZKM.getText()
                               .trim(); // dito
            
            // TODO: Does setAnzahlKM need clearing of "TausenderTrennzeichen" et all...
            thisRezept.setAnzahlKM(new BigDecimal(stest.isEmpty() ? "0.00" : stest));  // A pre-filled Rezept (edit mode) will
                                                                                       // most likely have a value of "0.00"
                                                                                       // instead of just "0". To detect changes
                                                                                       // we should keep it that way
            int rule = SystemPreislisten.hmZuzahlRegeln.get(aktuelleDisziplin)
                                                       .get(Integer.parseInt(jtfPREISGR.getText()) - 1);
            thisRezept.setZZRegel(rule);
            thisRezept.setIcd10(jtfICD10_1.getText()
                                         .replace(" ", ""));
            thisRezept.setIcd10_2(jtfICD10_2.getText()
                                             .replace(" ", ""));
            setCursor(Cursors.normalCursor);
        } catch (Exception ex) {
            ex.printStackTrace();
            setCursor(Cursors.normalCursor);
            JOptionPane.showMessageDialog(popupDialog,
                    "Fehler beim Abspeichern dieses Rezeptes.\n"
                            + "Bitte notieren Sie den Namen des Patienten und die Rezeptnummer\n"
                            + "und informieren Sie umgehend den Administrator");
            logger.error("Problems copying contents of form to Rezept-Object");
        }
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

    /**
    *
    * initialisiert ein Rezept mit Daten, die immer gesetzt werden muessen
    */
    private void initRezeptAll(Rezept thisRezept) {
        logger.debug("In init-(4)all");
        if (!thisRezept.isKidSet() ) { // eher ein Fall fuer check/speichern!
            JOptionPane.showMessageDialog(popupDialog,
                    "Achtung - kann Preisgruppe nicht ermitteln - "
                    + "Rezept kann sp\u00e4ter nicht abgerechnet werden!");
            logger.error("Could not get Preisgruppe Rez will be invalid");
        }
 
        if (SystemConfig.AngelegtVonUser) {
            thisRezept.setAngelegtVon(Reha.aktUser);
        } else {
            thisRezept.setAngelegtVon("");
        }
        thisRezept.setErfassungsDatum(LocalDate.now());
 
        thisRezept.setLastEditor(Reha.aktUser);
        thisRezept.setLastEdDate(LocalDate.now());
 
        thisRezept.setHeimbewohn("T".equals(Reha.instance.patpanel.patDaten.get(44))); // koennte sich geaendert haben
        thisRezept.setBefr("T".equals(Reha.instance.patpanel.patDaten.get(30))); // dito
        thisRezept.setRezBez(false); // kann noch nicht bezahlt sein (Rezeptgebuehr)

    }

   /**
    *
    * initialisiert ein leeres Rezept mit Daten aus dem aktuellen Patienten
    */
    private Rezept initRezeptNeu() {
       Rezept thisRezept = new Rezept();

       logger.debug("Created empty Rez");
       thisRezept.setKTraegerName(Reha.instance.patpanel.patDaten.get(13)); // Kasse
       thisRezept.setkId(Integer.parseInt(Reha.instance.patpanel.patDaten.get(68))); // id des Kassen-record

       initRezeptAll(thisRezept);

       thisRezept.setArztId(Integer.parseInt(Reha.instance.patpanel.patDaten.get(67)));
       verordnenderArzt = new ArztVec();
       verordnenderArzt.init(thisRezept.getArztId());
       // thisRezept.setArzt(Reha.instance.patpanel.patDaten.get(25)); // Hausarzt als default
       thisRezept.setArzt(verordnenderArzt.getNName());
       // logger.debug("Hausarzt assumed");
       // TODO: check - AnzahlKM is a String in DB - what values/format can we expect here?
       // logger.debug("Want to set KM as \"" + Reha.instance.patpanel.patDaten.get(48).replace(".", "") + "\"");
       thisRezept.setAnzahlKM(new BigDecimal(
               Reha.instance.patpanel.patDaten.get(48).replace(".", "").isEmpty() ? 
                       "0.00" 
                       : Reha.instance.patpanel.patDaten.get(48).replace(".", "")));
       thisRezept.setPatId(Integer.parseInt(Reha.instance.patpanel.patDaten.get(66)));
       thisRezept.setPatIntern(Integer.parseInt(Reha.instance.patpanel.patDaten.get(29)));
       return thisRezept;
       // Barcode
    }

   /**
    *
    * initialisiert ein kopiertes Rezept
    *  - aktualisiert Daten aus dem aktuellen Patienten,
    *  - loescht Daten, die nur fuer die Vorlage gelten (Behandlungen, Preise, Zuzahlung, ...)
    */
    private void initRezeptKopie(Rezept thisRezept) {
       String kasseInVo = thisRezept.getKTraegerName();
       String kasseInPatStamm = Reha.instance.patpanel.patDaten.get(13);
       if (!kasseInVo.equals(kasseInPatStamm)) {
           // Kasse im Rezept stimmt nicht mit Kasse im Patientenstamm ueberein:
           // Pat. hat inzwischen Kasse gewechselt oder es ist ein BG- oder
           // Privatrezept
           if (askForKeepCurrent(kasseInVo, kasseInPatStamm) == JOptionPane.NO_OPTION) {
               thisRezept.setKTraegerName(kasseInPatStamm);
               thisRezept.setkId(Integer.parseInt(Reha.instance.patpanel.patDaten.get(68)));
           }
       }
       
       initRezeptAll(thisRezept);

       thisRezept.setRezNr("");
       thisRezept.setTermine("");
       // TODO: These were empty-String - check for lazy "isEmpty-then-set" in other methods
       // thisRezept.setRezDatum(LocalDate.now());
       // thisRezept.setZZStatus(Rezept.ZZSTATUS_NOTSET);
       // thisRezept.setLastDate("");
    }

    private int askForKeepCurrent(String kasseInVO, String kassePatStamm) {
       String[] strOptions = { "Kasse der VO beibehalten", "Kasse aus Patientendaten verwenden" };
       return JOptionPane.showOptionDialog(popupDialog,
               "<html><b>Das Rezept enth\u00e4lt eine andere Kasse als die Stammdaten des Patienten: </b>\n"
                       + "\n     Kasse im kopierten Rezept:      " + kasseInVO + "\n     Kasse in den Patientendaten:  "
                       + kassePatStamm + "\n",
               "unterschiedliche Kassen gefunden", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
               strOptions, strOptions[0]);
    }
   
    private void allowShortCut(Component thisComponent, String name) {
        if (thisComponent == null) {
            logger.debug("Component is null and name=" + name);
            return;            
        }
        if (name == null) {
            logger.debug("Name is null");
            return;            
        }
        thisComponent.setName(name);
        thisComponent.addKeyListener(new KeyLauscher());
        thisComponent.addFocusListener(this);
    }
       
    public void aufraeumen() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (JRtaTextField jtf : jtfAll) {
                    ListenerTools.removeListeners(jtf);
                }
                for (JRtaCheckBox jcb : jcbAll) {
                    ListenerTools.removeListeners(jcb);
                }
                for (JRtaComboBox jcmb : jcmbAll) {
                    ListenerTools.removeListeners(jcmb);
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
    
    // Lemmi 20101231: Standard-Abfrage nach Pruefung, ob sich Eintraege geaendert haben
    // fragt nach, ob wirklich ungesichert abgebrochen werden soll !
    public int askForCancelUsaved() {
        String[] strOptions = { "ja", "nein" }; // Defaultwert auf "nein" gesetzt !
        return JOptionPane.showOptionDialog(popupDialog,
                "Es wurden Rezept-Angaben ge\u00e4ndert!\nWollen sie die \u00c4nderung(en) wirklich verwerfen?",
                "Angaben wurden ge\u00e4ndert", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, strOptions,
                strOptions[1]);
    }
    
    // TODO: Find a way to rid ourselves of this:
    public RezeptEditorGUI getInstance() {
        return this;
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
                String text = jtfANZ1.getText();
                jtfANZ2.setText(text);
                jtfANZ3.setText(text);
                jtfANZ4.setText(text);
                return;
            }
            if (componentName.contains("leistung") && jumpForward) {
                // ComboBox mit [TAB] verlassen ...
                String test = (String) ((JRtaComboBox) arg0.getSource()).getSelectedItem();
                if (test.equals("./.")) { // ... + kein Heilmittel ausgewaehlt -> zur Behandlungsfrequenz springen
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
                String text = jtfICD10_1.getText();
                jtfICD10_1.setText(RezeptFensterTools.chkIcdFormat(text));
                if (ctrlIsPressed & jumpForward) {
                    eingabeDiag.requestFocusInWindow();
                }
                return;
            }
            if (componentName.equals("icd10_2")) {
                String text = jtfICD10_2.getText();
                jtfICD10_2.setText(RezeptFensterTools.chkIcdFormat(text));
                return;
            }
        }
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
            JOptionPane.showMessageDialog(popupDialog, "Fehler beim abh\u00e4ngen des Listeners Rezept-Neuanlage\n"
                    + "Bitte informieren Sie den Administrator \u00fcber diese Fehlermeldung");
        }
    }

    private void kassenAuswahl(String[] suchenach) {
        jtfARZT.requestFocusInWindow();
        KassenAuswahl kwahl = new KassenAuswahl(null, "KassenAuswahl", suchenach,
                new JRtaTextField[] { jtfKTRAEG, jtfPATID, jtfKASID }, jtfKTRAEG.getText()
                                                                                            .trim());
        kwahl.setModal(true);
        kwahl.setLocationRelativeTo(this);
        kwahl.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (jtfKASID.getText()
                               .equals("")) {
                    String meldung = "Achtung - kann Preisgruppe nicht ermitteln!\n"
                            + "Das bedeutet diese Rezept kann sp\u00e4ter nicht abgerechnet werden!\n\n"
                            + "Und bedenken Sie bitte Ihr K\u00fcrzel wird dauerhaft diesem Rezept zugeordnet....";
                    JOptionPane.showMessageDialog(popupDialog, meldung);
                } else {
                    holePreisGruppe(Integer.parseInt(jtfKASID.getText()
                                               .trim()));
                    ladePreisliste(jcmbRKLASSE.getSelectedItem()
                                                 .toString()
                                                 .trim(),
                            preisgruppen[getPgIndex()]);
                    jtfARZT.requestFocusInWindow();
                }
            }
        });
        kwahl.dispose();
        kwahl = null;
    }
    
    private void arztAuswahl(String[] suchenach) {
        jtfREZDAT.requestFocusInWindow();
        JRtaTextField tfArztNum = new JRtaTextField("", false);
        JRtaTextField lanr = new JRtaTextField("",false);
        ArztAuswahl awahl = new ArztAuswahl(null, "ArztAuswahl", suchenach,
                new JRtaTextField[] { jtfARZT, lanr, jtfARZTID }, String.valueOf(jtfARZT.getText()
                                                                                                 .trim()));
        awahl.setModal(true);
        awahl.setLocationRelativeTo(this);
        awahl.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jtfREZDAT.requestFocusInWindow();
            }
        });
        try {
            verordnenderArzt = awahl.getArztRecord();
            jtfARZT.setText(verordnenderArzt.getNNameLanr());
            labArztLANr.setText(verordnenderArzt.getLANR());
            String aIdNeu = verordnenderArzt.getIdS();
            if (!Reha.instance.patpanel.patDaten.get(63)
                                                 .contains(("@" + aIdNeu + "@\n"))) {
                String aliste = Reha.instance.patpanel.patDaten.get(63) + "@" + aIdNeu + "@\n";
                Reha.instance.patpanel.patDaten.set(63, aliste + "@" + aIdNeu + "@\n");
                Reha.instance.patpanel.getLogic()
                                      .arztListeSpeichernString(aliste, false, Reha.instance.patpanel.aktPatID);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jtfREZDAT.requestFocusInWindow();
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(popupDialog,
                    "Fehler beim Speichern der Arztliste!\n"
                        + "Bitte notieren Sie Patient, Rezeptnummer und den Arzt den Sie der\n"
                        + "Arztliste hinzuf\u00fcgen wollten und informieren Sie umgehend den Administrator.\n"
                        + "\nDanke");
        }
        awahl.dispose();
        awahl = null;

    }
    
    private void holePreisGruppe(int idKtraeger) {
        try {
            KrankenkasseAdrDto kkaDto = new KrankenkasseAdrDto(mand.ik());
            Optional<KrankenkasseAdr> kka = kkaDto.getAllePreisgruppenFelderById(idKtraeger, SystemConfig.mitRs);
            if (kka.isPresent()) {
                preisgruppen[0] = kka.get().getPgKg() - 1;
                preisgruppen[1] = kka.get().getPgMa() - 1;
                preisgruppen[2] = kka.get().getPgEr() - 1;
                preisgruppen[3] = kka.get().getPgLo() - 1;
                preisgruppen[4] = kka.get().getPgRh() - 1;
                preisgruppen[5] = kka.get().getPgPo() - 1;
                if (SystemConfig.mitRs) {
                    preisgruppen[6] = kka.get().getPgRs() - 1;
                    preisgruppen[7] = kka.get().getPgFt() - 1;
                }
                preisgruppe = kka.get().getPreisgruppe() - 1;
                jtfPREISGR.setText(String.valueOf(kka.get().getPreisgruppe()));
            } else {
                logger.error("Returned Preislisten were empty");
                JOptionPane.showMessageDialog(popupDialog,
                        "Achtung - kann Preisgruppe nicht ermitteln - "
                        + "Rezept kann sp\u00e4ter nicht abgerechnet werden!");
            }
            
        } catch (Exception ex) {
            logger.error("Exception caught whilst trying to get Preisliste", ex);
            JOptionPane.showMessageDialog(popupDialog,
                    "Achtung - kann Preisgruppe nicht ermitteln -"
                    + " Rezept kann sp\u00e4ter nicht abgerechnet werden!\n"
                    + "Untersuchen Sie die Krankenkasse im Kassenstamm un weisen "
                    + "Sie dieser Kasse die entsprechend Preisgruppe zu");
        }
    }

    private void ladePreisliste(String typeOfVO, int preisgruppe) {
        try {
            String[] artdbeh = null;
            if (!this.neu && jcmbLEIST1.getItemCount() > 0) {
                logger.debug("Not new & itemCount > 0");
                artdbeh = new String[] { String.valueOf(jcmbLEIST1.getValueAt(1)),
                        String.valueOf(jcmbLEIST2.getValueAt(1)), String.valueOf(jcmbLEIST3.getValueAt(1)),
                        String.valueOf(jcmbLEIST4.getValueAt(1)) };
            }
            jcmbLEIST1.removeAllItems();
            jcmbLEIST2.removeAllItems();
            jcmbLEIST3.removeAllItems();
            jcmbLEIST4.removeAllItems();

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
                    jcmbBARCOD.setSelectedItem("Muster 13/18");
                } else if (aktuelleDisziplin.equals("Logo")) {
                    jcmbBARCOD.setSelectedItem("Muster 14");
                } else if (aktuelleDisziplin.equals("Reha")) {
                    jcmbBARCOD.setSelectedItem("DIN A4 (REHA)");
                } else {
                    jcmbBARCOD.setSelectedItem("Muster 13/18");
                }
            } else if (this.neu && aktuelleDisziplin.equals("Reha")) {
                jcmbBARCOD.setSelectedItem("DIN A4 (REHA)");
            } else {
                if (this.neu) {
                    jcmbBARCOD.setSelectedItem("Muster 13/18");
                }
            }
        } catch (Exception ex) {
            logger.error("Ladepreisliste went bust: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

    }

    public void ladePreise(String[] artdbeh) {
        try {
            if (preisvec.size() <= 0) {
                JOptionPane.showMessageDialog(popupDialog,
                        "In der erforderlichen Preisliste sind noch keine Preise vorhanden!\n"
                        + "Rezept kann nicht angelegt werden");
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(popupDialog,
                    "In der erforderlichen Preisliste sind noch keine Preise vorhanden!\n"
                    + "Rezept kann nicht angelegt werden");
            return;
        }
        jcmbLEIST1.setDataVectorWithStartElement(preisvec, 0, 9, "./.");
        jcmbLEIST2.setDataVectorWithStartElement(preisvec, 0, 9, "./.");
        jcmbLEIST3.setDataVectorWithStartElement(preisvec, 0, 9, "./.");
        jcmbLEIST4.setDataVectorWithStartElement(preisvec, 0, 9, "./.");
        JRtaComboBox[] jcmb = new JRtaComboBox[] {jcmbLEIST1, jcmbLEIST2, jcmbLEIST3, jcmbLEIST4};
        if (artdbeh != null) {
            for (int i = 0; i < 4; i++) {
                if (artdbeh[i].equals("")) {
                    jcmb[i].setSelectedIndex(0);
                } else {
                    jcmb[i].setSelectedVecIndex(1, artdbeh[i]);
                }
            }
        }
        return;
    }
    
    private int getPgIndex() {
        return jcmbRKLASSE.getSelectedIndex();
    }
    
    /** Holt die passenden Inikationsschluessel gemaess aktiver Disziplin**/
    private void fuelleIndis(String typeOfVO) {
        try {
            if (jcmbINDI.getItemCount() > 0) {
                jcmbINDI.removeAllItems();
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
                jcmbINDI.addItem(indis[i]);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(popupDialog, "Fehler bei f\u00fcller Inikat.schl\u00fcssel\n" + ex.getMessage());

        }

        return;
    }

    public void macheFarbcodes() {
        try {
            farbcodes.add("kein Farbcode");
            jcmbFARBCOD.addItem(farbcodes.get(0));
            Vector<String> farbnamen = SystemConfig.vSysColsNamen;
            for (int i = 0; i < farbnamen.size(); i++) {
                if (farbnamen.get(i).startsWith("Col")) {
                String bedeutung = SystemConfig.vSysColsBedeut.get(i);
                farbcodes.add(bedeutung);

                jcmbFARBCOD.addItem(bedeutung);
                }
            }
            if (!this.neu) {
                
                int itest = rez.getFarbcode();
                
                logger.debug("rez: Farbcode=" + itest);
                if (itest >= 0) {
                    jcmbFARBCOD.setSelectedItem(SystemConfig.vSysColsBedeut.get(itest));
                } else {
                    jcmbFARBCOD.setSelectedIndex(0);
                }
            } else {
                jcmbFARBCOD.setSelectedIndex(0);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(popupDialog, "Fehler bei Farbcodes erstellen\n" + ex.getMessage());
        }

    }

    /**
     *  Lemmi Doku: prueft ob die Heilmittel ueberhaupt und in der korrekten
     *  Reihenfolge eingetragen worden sind
     */
    private boolean anzahlTest() {
        int itest;
        int maxanzahl = 0, aktanzahl = 0;

        JRtaComboBox[] jcmb = new JRtaComboBox[] {jcmbLEIST1, jcmbLEIST2, jcmbLEIST3, jcmbLEIST4 };
        JRtaTextField[]  jtf = new JRtaTextField[] {jtfANZ1, jtfANZ2, jtfANZ3, jtfANZ4};
        for (int i = 0; i < 4; i++) { // ueber alle 4 Leistungs- und Anzahl-Positionen rennen
            itest = jcmb[i].getSelectedIndex();
            if (itest > 0) {
                if (i == 0) { // die 1. Position besonders abfragen - diese muss existieren !
                    try {
                        maxanzahl = Integer.parseInt(jtf[i].getText());
                    } catch (NumberFormatException ex) {
                        maxanzahl = 0;
                    }
                } else {
                    try {
                        aktanzahl = Integer.parseInt(jtf[i].getText());
                    } catch (Exception ex) {
                        aktanzahl = 0;
                    }
                    if (aktanzahl > maxanzahl) {
                        String cmd = "Sie haben mehrere Heilmittel mit unterschiedlicher Anzahl eingegeben.\n"
                                + "Bitte geben Sie die Heilmittel so ein da\u00df das Heilmittel mit der"
                                + " gr\u00f6\u00dften Anzahl oben steht\n"
                                + "und dann (bezogen auf die Anzahl) in absteigender Reihgenfolge nach unten";
                        JOptionPane.showMessageDialog(popupDialog, cmd);
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
        long dattest = DatFunk.TageDifferenz(DatFunk.sHeute(), jtfREZDAT.getText()
                                                                           .trim());
        if ((dattest <= -364) || (dattest >= 364)) {
            int frage = JOptionPane.showConfirmDialog(popupDialog,
                    "<html><b>Das Rezeptdatum ist etwas kritisch....<br><br><font color='#ff0000'> " + "Rezeptdatum = "
                            + jtfREZDAT.getText().trim()
                            + "</font></b><br>Das sind ab Heute " + Long.toString(dattest) + " Tage<br><br><br>"
                            + "Wollen Sie dieses Rezeptdatum tats\u00e4chlich abspeichern?",
                    "Bedenkliches Rezeptdatum", JOptionPane.YES_NO_OPTION);
            if (frage != JOptionPane.YES_OPTION) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jtfREZDAT.requestFocusInWindow();
                    }
                });
                return false;
            }
        }
        return true;
    }
    
    /**
     * SanityChecks - Test the fields/filled out form for completeness <BR/>
     * - RezeptDatum<BR/>
     * - KostenTraeger<BR/>
     * - Arzt<BR/>
     * - Dauer (pro Behand.?)<BR/>
     * - Angelegt von?<BR/>
     * - Behandlungs Frequenz<BR/>
     * 
     * @return True if all tests pass - false if one fails on route
     */
    private boolean komplettTest() {
        if (jtfREZDAT.getText().trim().equals(".  .")) {
            JOptionPane.showMessageDialog(popupDialog,
                    "Ohne ein g\u00fcltiges 'Rezeptdatum' kann ein Rezept nicht abgespeichert werden.");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jtfREZDAT.requestFocusInWindow();
                }
            });
            return false;
        }

        if (jtfKTRAEG.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(popupDialog,
                    "Ohne die Angabe 'Kostentr\u00e4ger' kann ein Rezept nicht abgespeichert werden.");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jtfKTRAEG.requestFocusInWindow();
                }
            });
            return false;
        }
        
        if (jtfARZT.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(popupDialog,
                    "Ohne die Angabe 'verordn. Arzt' kann ein Rezept nicht abgespeichert werden.");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jtfARZT.requestFocusInWindow();
                }
            });
            return false;
        }
        
        if (jtfDAUER.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(popupDialog,
                    "Ohne die Angabe 'Behandlungsdauer' kann ein Rezept nicht abgespeichert werden.");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jtfDAUER.requestFocusInWindow();
                }
            });
            return false;
        }
        
        if (jtfANGEL.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(popupDialog,
                    "Ohne die Angabe 'Angelegt von' kann ein Rezept nicht abgespeichert werden.");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jtfANGEL.requestFocusInWindow();
                }
            });
            return false;
        }
        
        if (SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin)
                                             .get(preisgruppen[getPgIndex()]) == 1) {
            if (jtfFREQ.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(popupDialog,
                        "Ohne Angabe der 'Behandlungsfrequenz' kann ein GKV-Rezept nicht abgespeichert werden.");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jtfFREQ.requestFocusInWindow();
                    }
                });
                return false;
            }
        }
        
        // Apparently, no test returned false, so we're good to go:
        return true;
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
                        String meldung = "<html><b>Achtung!</b><br><br>Sie haben Verordnung au\u00dferhalb des"
                                + " Regelfalles gew\u00e4hlt!<br><br>Die Krankenkasse des Patienten"
                                + " besteht auf eine <br>"
                                + "<b>Genehmigung f\u00fcr Verordnungen au\u00dferhalb des Regelfalles"
                                + "</b><br><br></html>";
                        JOptionPane.showMessageDialog(popupDialog, meldung);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(popupDialog,
                            "Fehler!!!\n\nVermutlich haben Sie eines der letzten Updates verpa\u00dft.\n"
                            + "Fehlt zuf\u00e4llig die Tabelle adrgenehmigung?");
                    logger.error("Fehler in testeGenehmigung: " + ex.getLocalizedMessage());
                }
                return null;
            }
        }.execute();

    }

    
    /** 
     * Will do various sanity checks on the Rezept,<BR/>
     *  if they pass, a new "RezeptNummer" will be acquired<BR/>
     *  and the resulting Rezept stored in the database<BR/>
     */
    private void actionSpeichern() {
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
                       logger.debug("In neu - neuDate-Test ok");
                       copyFormToRez1stTime(rez);
                       // TODO: clean up neue-RezNr holen
                       Disziplin tmpDiszi = Disziplin.valueOf(rezKlasse);
                       if (tmpDiszi != Disziplin.INV) {
                           NummernKreis tmpRZN = new NummernKreis(mand.ik());
                           rez.setRezNr(new Rezeptnummer(tmpDiszi, tmpRZN.nextNumber(tmpDiszi))
                                                                                           .rezeptNummer());
                       } else {
                           logger.error("Couldn't find valid Disziplin to base new RezNr on");
                       }
                       // What's this ?? - better create a call-refresh or something...
                       Reha.instance.patpanel.aktRezept.setzeRezeptNummerNeu(rez.getRezNr());
                   } else {
                       copyFormToRez(rez, true);
                   }
                   closeDialog();
                   aufraeumen();
                   // ?? automat. HMR-Check ??
                   RezeptDto rDto = new RezeptDto(mand.ik());
                   if (!rDto.rezeptInDBSpeichern(rez)) {
                       String fehlerMeldung="<html><b>Achtung!</b><br>Fehler! Konnte Rezept (wahrscheinlich) nicht speichern!<BR/>"
                               + "Disziplin: " + Disziplin.valueOf(rezKlasse) + "<BR/>"
                               + "RezeptNummer: " + rez.getRezNr() + "<br><br></html>";
                       JOptionPane.showMessageDialog(popupDialog, fehlerMeldung);
                   };
                   
               } catch (Exception ex) {
                   logger.error("Couldn't save Rezept due to: " + ex.getLocalizedMessage());
                   String fehlerMeldung="<html><b>Achtung!</b><br>Fehler! Konnte Rezept (wahrscheinlich) nicht speichern!<BR/>"
                                       + "Disziplin: " + Disziplin.valueOf(rezKlasse) + "<BR/>"
                                       + "RezeptNummer: " + rez.getRezNr() + "<br><br></html>";
                   JOptionPane.showMessageDialog(popupDialog, fehlerMeldung);
               }

           }
       });
   }

    void actionAbbrechen() {
       // Lemmi 20101231: Verhinderung von Datenverlust bei unbeabsichtigtem Zumachen
       // des geaenderten Rezept-Dialoges
       /* 
        * Solche gravierenden Aenderungen der Programmlogik duerfen erst dann eingefuehrt
        * werden wenn sich der Benutzer auf einer System-Init-Seite entscheiden kann ob er
        * diese Funktionalitaet will oder nicht.
        * Wir im RTA wollen die Abfagerei definitiv nicht!
        * Wenn meine Damen einen Vorgang abbrechen wollen, dann wollen sie den Vorgang
        * abbrechen und nicht gefrag werden ob sie den Vorgang abbrechen wollen.
        * Steinhilber
        */
       // Lemmi 20110116: Gerne auch mit Steuer-Parameter
       SwingUtilities.invokeLater(new Runnable() {
           
           @Override
           public void run() {
               if ((Boolean) SystemConfig.hmRezeptDlgIni.get("RezAendAbbruchWarn")) {
                   Rezept tmpRez;
                   /*
                   if (neu) {
                       tmpRez = new Rezept();
                   } else {
                       tmpRez = new Rezept(rez);
                   }
                   */
                   tmpRez = new Rezept(rez);
                   copyFormToRez(tmpRez, false);
                   logger.debug("Will hash this: " + tmpRez.toString());
                   // logger.debug("And got: " + tmpRez.hashCode() + " whilst old val was: " + hashOfFormVals );
                   if ( hashOfFormVals != tmpRez.hashCode() && askForCancelUsaved() == JOptionPane.NO_OPTION)
                       return;
               }
               aufraeumen();
               closeDialog();
               }
           });
    }
   
    /**
     *
     */
    private void actionVerordnungsArt() {
        if (!klassenReady)
            return;
        if (jcmbVERORD.getSelectedIndex() == 2) {
            jcbBEGRADR.setEnabled(true);
            testeGenehmigung(jtfKASID.getText());
        } else {
            jcbBEGRADR.setSelected(false);
            jcbBEGRADR.setEnabled(false);
        }
    }

    /**
     * 
     */
    private void actionHmrCheck() {
       new SwingWorker<Void, Void>() {
           @Override
           protected Void doInBackground() throws Exception {
               try {
                   boolean icd10falsch = false;
                   int welcherIcd = 0;
                   String pruefIcd10Text = jtfICD10_1.getText().trim();
                   if ( pruefIcd10Text.length() > 0) {
                       if (!pruefeObIcd10GefundenWerdenKann(pruefIcd10Text)) {
                           icd10falsch = true;
                           welcherIcd = 1;
                       }
                       pruefIcd10Text = jtfICD10_2.getText().trim();
                       if (pruefIcd10Text.length() > 0) {
                           if (!pruefeObIcd10GefundenWerdenKann(pruefIcd10Text)) {
                               icd10falsch = true;
                               welcherIcd = 2;
                           }
                       }
                   } else {                                                             // min. der 1.ICD10 Code muss
                                                                                        // vorhanden sein
                       if (SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin)
                                                            .get(preisgruppen[getPgIndex()]) == 1) {
                           hmrcheck.setEnabled(true);
                           JOptionPane.showMessageDialog(popupDialog,
                                   "<html><b><font color='#ff0000'>Kein ICD-10 Code angegeben!</font></b></html>");

                       }
                   }
                   // Lets get the rest up and running first:
                   if (hmrPreChecks())
                       doHmrCheck();
                   
               } catch (Exception ex) {
                   logger.error("Problems in actionDoHMR-Check: " + ex.getLocalizedMessage());;
               }
               return null;
           }
       }.execute();
    }

    /**
     * 
     */
    private void actionHausbesuche() {
       if (jcbHAUSB.isSelected()) {
           // Hausbesuch gewaehlt
           if (Reha.instance.patpanel.patDaten.get(44)
                                              .equals("T")) {
               if (this.preisgruppe != 1 && (getPgIndex() <= 1)) {
                   jcbVOLLHB.setEnabled(true);
               }
               SwingUtilities.invokeLater(new Runnable() {
                   public void run() {
                       jcbHAUSB.requestFocusInWindow();
                   }
               });
           } else {
               jcbVOLLHB.setEnabled(false);
               jcbVOLLHB.setSelected(true);
               SwingUtilities.invokeLater(new Runnable() {
                   public void run() {
                       jcbHAUSB.requestFocusInWindow();
                   }
               });
           }
       } else {
           // Hausbesuch abgewaehlt
           jcbVOLLHB.setEnabled(false);
           jcbVOLLHB.setSelected(false);
           SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                   jcbHAUSB.requestFocusInWindow();
               }
           });
       }
    }

    /**
     * @param e
     */
    private void actionLeistung(ActionEvent e) {
        if (!initReady)
            return;
//        int lang = e.getActionCommand().length();
        String test = (String) ((JRtaComboBox) e.getSource()).getSelectedItem();
        if (test == null) {
            return;
        }
        if (!test.equals("./.")) {
            String id = (String) ((JRtaComboBox) e.getSource()).getValue();
            Double preis = holePreisDouble(id, preisgruppe);
            if (preis <= 0.0) {
                JOptionPane.showMessageDialog(popupDialog,
                        "Diese Position ist f\u00fcr die gew\u00e4hlte Preisgruppe ung\u00fcltig\n"
                        + "Bitte weisen Sie in der Preislisten-Bearbeitung der Position ein K\u00fcrzel zu");
                ((JRtaComboBox) e.getSource()).setSelectedIndex(0);
            }
        }
    }

    private void actionRezeptklasse() {
        if (!klassenReady)
            return;
        this.ladePreisliste(jcmbRKLASSE.getSelectedItem()
                                          .toString()
                                          .trim(),
                preisgruppen[getPgIndex()]);
        this.fuelleIndis((String) jcmbRKLASSE.getSelectedItem());
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

    
    // TODO: Find a better way for updating parent:
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
    private void setLastDatInTable(LocalDate datum) {
        int row = getselectedRow();
        if (row >= 0) {
            AktuelleRezepte.tabaktrez.getModel()
                                     .setValueAt(datum.format(DateTimeFormatters.ddMMYYYYmitPunkt), row, 4);
        }
    }

    
    private int getselectedRow() {
        return Reha.instance.patpanel.aktRezept.tabaktrez.getSelectedRow();
    }

    /**
     * Do some preliminary HMR-Checks to make sure we actually have enough data to do a HMR-Check
     * 
     * @return True if all checks pass - false otherwise
     */
    private boolean hmrPreChecks() {
        if (SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin).get(preisgruppen[getPgIndex()]) == 0) {
            this.hmrcheck.setEnabled(true);
            JOptionPane.showMessageDialog(popupDialog, "HMR-Check ist bei diesem Kostentr\u00e4ger nicht erforderlich");
            return false;
        }
        logger.debug("Need to do HMR-Check with this KT");
        String indi = (String) jcmbINDI.getSelectedItem();
        if (indi.isEmpty() || indi.contains("kein IndiSchl.")) {
            JOptionPane.showMessageDialog(popupDialog,
                    "<html><b>Kein Indikationsschl\u00fcssel angegeben.<br>"
                    + "Die Angaben sind <font color='#ff0000'>nicht</font> gem\u00e4\u00df "
                    + "den g\u00fcltigen Heilmittelrichtlinien!</b></html>");
            logger.error("Indi-Schluessel \"" + indi + "\" ist Muell.");
            return false;
        }
        if (jtfREZDAT.getText().trim()
                .equals(".  .")) {
            JOptionPane.showMessageDialog(popupDialog, "Rezeptdatum nicht korrekt angegeben HMR-Check nicht m\u00f6glich");
            logger.error("Rezdatum: \"" + jtfREZDAT.getText().trim() + "\" kann nicht verwertet werden.");
            return false;
        }
        
        boolean icd10falsch = false;
        int welcherIcd = 0;
        String pruefIcd10Text = jtfICD10_1.getText().trim();
        if ( pruefIcd10Text.length() > 0) {
            if (!pruefeObIcd10GefundenWerdenKann(pruefIcd10Text)) {
                icd10falsch = true;
                welcherIcd = 1;
            }
            pruefIcd10Text = jtfICD10_2.getText().trim();
            if (pruefIcd10Text.length() > 0) {
                if (!pruefeObIcd10GefundenWerdenKann(pruefIcd10Text)) {
                    if (!icd10falsch)
                        icd10falsch = true;
                    welcherIcd = 2;
                }
            }
        } else {                                                         // min. der 1. ICD10 Code muss
                                                                         // vorhanden sein, wenn f. akt. Diszi die pg==1
            if (SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin)
                                                 .get(preisgruppen[getPgIndex()]) == 1) {
                hmrcheck.setEnabled(true);
                JOptionPane.showMessageDialog(popupDialog,
                        "<html><b><font color='#ff0000'>Kein ICD-10 Code angegeben!</font></b></html>");
                icd10falsch = true;
            }
        }
        if (icd10falsch) {
            logger.debug("Fehlerhafte ICD10 Codes entdeckt: " + welcherIcd + ". ICD10-Tool starten?");
            waehleIcd10(welcherIcd);
            return false;
        }
        logger.debug("Pre-HMR-Checks went fine");
        return true;
    }
    
    private boolean pruefeObIcd10GefundenWerdenKann(String icd10) {
        String suchenach = RezeptFensterTools.macheIcdString(icd10);
        if (SqlInfo.holeEinzelFeld(
                "select id from icd10 where schluessel1 like '" + suchenach + "%' LIMIT 1")
                   .isEmpty()) {
            return false;
        }
        return true;
    }
    
    private void waehleIcd10(int welcherIcd) {
        int frage = JOptionPane.showConfirmDialog(popupDialog,
                "<html><b>Der eingetragene " + Integer.toString(welcherIcd)
                        + ". ICD-10-Code ist falsch: <font color='#ff0000'>" + (welcherIcd == 1 ? jtfICD10_1.getText()
                                                                                                          .trim()
                                : jtfICD10_2.getText()
                                               .trim())
                        + "</font></b><br>" + "HMR-Check nicht m\u00f6glich!<br><br>"
                        + "Wollen Sie jetzt das ICD-10-Tool starten?<br>"
                        + "(Auch nach einer Korrektur muss der HMR-Check erneut ausgef\u00fchrt werden)<br></html>",
                "falscher ICD-10", JOptionPane.YES_NO_OPTION);
        if (frage == JOptionPane.YES_OPTION) {
            new LadeProg(Path.Instance.getProghome() 
                    + "ICDSuche.jar" + " " 
                    + Path.Instance.getProghome() + " "
                    + mand.ikDigitString());
        }
        if (welcherIcd == 1) {
            jtfICD10_1.setText("");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jtfICD10_1.requestFocusInWindow();
                }
            });
        } else if (welcherIcd == 2) {
            jtfICD10_2.setText("");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jtfICD10_2.requestFocusInWindow();
                }
            });

        }
    }
    
    private void doHmrCheck() {
        
        // System.out.println(SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin).get(preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]));
        int itest = 0; // jcmb[cLEIST1].getSelectedIndex();
        String indi = (String) jcmbINDI.getSelectedItem();
        
        indi = indi.replace(" ", "");
        /*
         * As far as I can make out, all of the following is not really used sensibly
         * so, lets kill it
         *
        List<Integer> anzahlen = new ArrayList<Integer>();
        List<String> hmPositionen = new ArrayList<String>();
        
        logger.debug("Lets see:" + rez.positionenundanzahl().toString());
        Vector<ArrayList<?>> hmposAnzArt = rez.positionenundanzahl();
        // What do we do all this for? We only want to know .size() > 0...
        hmPositionen = hmposAnzArt.get(0).stream()
                .map(object -> Objects.toString(object, null))
                .collect(Collectors.toList());
        // What do we do the following for? It's never used hereafter...
        anzahlen = hmposAnzArt.get(1).stream()
                .map(object -> Integer.valueOf(Objects.toString(object, null)))
                .collect(Collectors.toList());

        logger.debug("anzahlen: " + anzahlen.toString());
        logger.debug("hmPositionen: " + hmPositionen.toString());
        
//        if (hmPositionen.size() > 0) {
 * 
 */
        if (rez.positionenundanzahl().get(0).size() > 0) {
            logger.debug("HMPos not empty");
            String letztbeginn = jtfBEGINDAT.getText()
                                               .trim();
            if (letztbeginn.equals(".  .")) {
                logger.debug("Letztmgl. Behandlungsbeg. leer...");
                // Preisgruppe holen
                int pg = Integer.parseInt(jtfPREISGR.getText()) - 1;
                // Frist zwischen Rezeptdatum und erster Behandlung
                int frist = (Integer) ((Vector<?>) SystemPreislisten.hmFristen.get(aktuelleDisziplin)
                                                                              .get(0)).get(pg);
                // Kalendertage
                if ((Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(aktuelleDisziplin)
                                                                      .get(1)).get(pg)) {
                    letztbeginn = DatFunk.sDatPlusTage(jtfREZDAT.getText()
                                                                   .trim(),
                            frist);
                    logger.debug("LetztBegin in somewhere is " + letztbeginn);
                } else { // Werktage
                    boolean mitsamstag = (Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(aktuelleDisziplin)
                                                                                           .get(4)).get(pg);
                    letztbeginn = HMRCheck.hmrLetztesDatum(jtfREZDAT.getText()
                                                                       .trim(),
                            (Integer) ((Vector<?>) SystemPreislisten.hmFristen.get(aktuelleDisziplin)
                                                                              .get(0)).get(pg),
                            mitsamstag);
                    logger.debug("LetztBegin in somewhere else is " + letztbeginn);
                }
            }
            // TODO: The following block could be sorted properly
            Rezept rezTmpRezept = new Rezept();
            if (getInstance().neu) {
                rezTmpRezept = initRezeptNeu();
            } else {
                rezTmpRezept = new Rezept(rez);
            }
            copyFormToRez1stTime(rezTmpRezept);
            boolean checkok = new HMRCheck(rezTmpRezept, diszis.getCurrDisziFromActRK(), preisvec).check();
            if (checkok) {
                JOptionPane.showMessageDialog(popupDialog,
                        "<html><b>Das Rezept <font color='#ff0000'>entspricht</font> "
                        + "den geltenden Heilmittelrichtlinien</b></html>");
            } else {
                logger.error("Rez not HMR-conform");
            }
        } else {
            JOptionPane.showMessageDialog(popupDialog, "Keine Behandlungspositionen angegeben, "
                                                + "HMR-Check nicht m\u00f6glich!!!");
        }

    }

    
    private final class Icd10Listener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            logger.debug("gotcha");
            super.mouseClicked(e);
        }


    }
    
    
    private class KeyLauscher extends KeyAdapter{
        @Override
        public void keyPressed(KeyEvent arg0) {
            if (arg0.getKeyChar() == '?' && ((JComponent) arg0.getSource()).getName()
                                                                           .equals("arzt")) {
                String[] suchkrit = new String[] { jtfARZT.getText()
                                                             .replace("?", ""),
                        jtfARZTID.getText() };
                jtfARZT.setText(String.valueOf(suchkrit[0]));
                arztAuswahl(suchkrit);
            }
            if (arg0.getKeyChar() == '?' && ((JComponent) arg0.getSource()).getName()
                                                                           .equals("ktraeger")) {
                String[] suchkrit = new String[] { jtfKTRAEG.getText()
                                                               .replace("?", ""),
                        jtfKASID.getText() };
                jtfKTRAEG.setText(suchkrit[0]);
                kassenAuswahl(suchkrit);
            }
            if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                actionAbbrechen();
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
    }
    
}
