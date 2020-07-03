package org.therapi.reha.patient;

import java.awt.Color;
import java.awt.Font;
import java.awt.dnd.DropTargetListener;
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
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.JCompTools;
import CommonTools.JRtaTextField;
import CommonTools.SqlInfo;
import dialoge.InfoDialogRGAFoffen;
import events.PatStammEvent;
import events.PatStammEventClass;
import events.PatStammEventListener;
import hauptFenster.Reha;
import hauptFenster.UIFSplitPane;
import mandant.Mandant;
import rehaInternalFrame.JPatientInternal;
import rezept.Rezept;

/**
 * @author juergen
 *
 */
public class PatientHauptPanel extends JXPanel {
    /**
     *
     */
    private static final long serialVersionUID = 36015777152668128L;

    // Logik-Klasse für PatientHauptPanel
    PatientHauptLogic patientLogic = null;

    // SuchenFenster
    public Object sucheComponent = null;

    // ToolBar-Controls & Listener
    public JButton[] jbut = { null, null, null, null, null };
    public JFormattedTextField tfsuchen;
    public JComboBox jcom;
    public ActionListener toolBarAction;
    public MouseListener toolBarMouse;
    public KeyListener toolBarKeys;
    public FocusListener toolBarFocus;
    public DropTargetListener dropTargetListener;

    // StammDaten-Controls & Listener
    public JPatTextField[] ptfield = new JPatTextField[15];
    public KeyListener stammDatenKeys;

    // MemoPanel-Controls & Listener
    public JTabbedPane memotab = null;
    public JButton[] memobut = { null, null, null, null, null, null };
    public JTextArea[] pmemo = { null, null };
    public ActionListener memoAction = null;
    public int inMemo = -1;

    // MultiFunctionPanel-Controls & Listener
    JTabbedPane multiTab = null;
    public RezepteAktuell aktRezept = null;
    public RezepteHistorisch historie = null;
    public TherapieBerichte berichte = null;
    public Dokumentation dokumentation = null;
    public Gutachten gutachten = null;
    public String[] tabTitel = { "aktuelle Rezepte", "Rezept-Historie", "Therapieberichte", "Dokumentation",
            "Gutachten", "Arzt & KK", "Plandaten" };
    
    // TODO: delete this JLabs member once RezeptDaten.java has been deleted...
    /**
     * @deprecated in 1.1.9 - use {@link org.therapi.reha.patient.RezepteAktuell#setRezeptDaten() Reha.instance.patpanel.aktRezept.setRezeptDaten()} instead
     * <BR/>
     * <BR/>0 RezNr?? - 1 hausbesuch - 2 angelegt - 3 kostentraeger - 4 arzt - 5 verornungsart - 6 begruendung - 7 arztbericht
     * 8 behandlung1 - 9 frequenz - 10 behandlung2 - 11  behandlung3 - 12  behandlung4 - 13  indikation - 14  Dauer
     * 15 LastEditor - 16 LastEditDate
     */
    @Deprecated
    public JLabel[] rezlabs = new JLabel[17];

    // Likewise
    @Deprecated
    public JTextArea rezdiag = null;

    public ImageIcon[] imgzuzahl = new ImageIcon[4];
    public ImageIcon[] imgrezstatus = new ImageIcon[2];
    public Vector<String> patDaten = new Vector<String>();
    // TODO: remove after Rezepte has been sorted
    @Deprecated
    public Vector<String> vecaktrez = null;
    public Rezept rezAktRez = null;
    // TODO: remove after Rezepte has been sorted
    @Deprecated
    public Vector<String> vecakthistor = null;
    public Rezept rezHistRez = null;

    // PatStamm-Event Listener == extrem wichtig
    private PatStammEventListener patientStammEventListener = null;
    private PatStammEventClass ptp = null;

    // Instanz-Variable für die einzelnen Panels
    public PatientToolBarPanel patToolBarPanel = null;
    private PatientStammDatenPanel stammDatenPanel = null;
    private PatientMemoPanel patMemoPanel = null;
    private PatientMultiFunctionPanel patMultiFunctionPanel = null;

    // Gemeinsam genutzte Variable
    public Font font = new Font("Courier New", Font.BOLD, 13);
    public Font fehler = new Font("Courier", Font.ITALIC, 13);
    public String aktPatID = "";
    public int autoPatid = -1;
    public int aid = -1;
    public int kid = -1;
    public boolean patDatenOk = false;
    public boolean rezDatenOk = false;
    public boolean historOk = false;
    public boolean berichtOk = false;
    public boolean dokuOk = false;
    public boolean gutachtenOk = false;

    // Bezug zum unterliegenden JInternalFrame
    JPatientInternal patientInternal = null;

    InfoDialogRGAFoffen infoDlg = null;

    private Connection connection;

    public PatientHauptPanel(String name, JPatientInternal internal, Connection connection) {
        super();

        this.connection = connection;
        setName(name);
        setDoubleBuffered(true);

        patientLogic = new PatientHauptLogic(this);
        patientInternal = internal;

        createPatStammListener();

        createActionListeners();
        createKeyListeners();
        createMouseListeners();
        createFocusListeners();

        setBackgroundPainter(Reha.instance.compoundPainter.get("getTabs2"));
        FormLayout lay = new FormLayout("0dlu,fill:0:grow(0.33),fill:0:grow(0.66)", "0dlu,p,fill:0:grow(1.0)");
        CellConstraints cc = new CellConstraints();
        setLayout(lay);

        add(getToolBarPatient(), cc.xyw(1, 2, 3));
        // add(constructSplitPaneLR(mand.getConnection()), cc.xyw(1, 3, 3));
        add(constructSplitPaneLR(), cc.xyw(1, 3, 3));
        setVisible(true);
        setzeFocus();
    }

    public PatientHauptLogic getLogic() {
        return patientLogic;
    }

    public PatientHauptPanel getInstance() {
        return this;
    }

    public JPatientInternal getInternal() {
        return patientInternal;
    }

    public void setInternalToNull() {
        patientInternal = null;
    }

    private UIFSplitPane constructSplitPaneLR() {
        UIFSplitPane jSplitLR = UIFSplitPane.createStrippedSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                getStammDatenPatient(), constructSplitPaneOU());
        jSplitLR.setOpaque(false);
        jSplitLR.setDividerSize(7);
        jSplitLR.setDividerBorderVisible(true);
        jSplitLR.setName("PatGrundSplitLinksRechts");
        jSplitLR.setOneTouchExpandable(true);
        jSplitLR.setDividerColor(Color.LIGHT_GRAY);
        jSplitLR.setDividerLocation(200);
        jSplitLR.validate();
        return jSplitLR;
    }

    private UIFSplitPane constructSplitPaneOU() {
        UIFSplitPane jSplitRechtsOU = UIFSplitPane.createStrippedSplitPane(JSplitPane.VERTICAL_SPLIT, getMemosPatient(),
                getMultiFunctionTab());
        jSplitRechtsOU.setOpaque(false);
        jSplitRechtsOU.setDividerSize(7);
        jSplitRechtsOU.setDividerBorderVisible(true);
        jSplitRechtsOU.setName("PatGrundSplitRechteSeiteObenUnten");
        jSplitRechtsOU.setOneTouchExpandable(true);
        jSplitRechtsOU.setDividerColor(Color.LIGHT_GRAY);
        jSplitRechtsOU.setDividerLocation(175);
        jSplitRechtsOU.validate();
        return jSplitRechtsOU;
    }

    private JScrollPane getStammDatenPatient() {
        stammDatenPanel = new PatientStammDatenPanel(this, connection);
        JScrollPane jscr = JCompTools.getTransparentScrollPane(stammDatenPanel);
        jscr.validate();
        JScrollPane jscr2 = JCompTools.getTransparent2ScrollPane(jscr);
        jscr2.validate();
        return jscr2;
    }

    private JScrollPane getMemosPatient() {
        patMemoPanel = new PatientMemoPanel(this);
        JScrollPane jscr = JCompTools.getTransparentScrollPane(patMemoPanel);
        jscr.validate();
        return jscr;
    }

    private synchronized JScrollPane getMultiFunctionTab() {
        patMultiFunctionPanel = new PatientMultiFunctionPanel(this, connection);
        JScrollPane jscr = JCompTools.getTransparentScrollPane(patMultiFunctionPanel);
        jscr.validate();
        return jscr;
    }

    private synchronized JXPanel getToolBarPatient() {
        patToolBarPanel = new PatientToolBarPanel(this);
        return patToolBarPanel;

    }

    public JTabbedPane getTab() {
        return multiTab;
    }

    public PatientStammDatenPanel getStammDaten() {
        return stammDatenPanel;
    }

    public PatientMemoPanel getMemo() {
        return patMemoPanel;
    }

    public PatientMultiFunctionPanel getMultiFuncPanel() {
        return patMultiFunctionPanel;

    }

    public PatientToolBarPanel getToolBar() {
        return patToolBarPanel;
    }

    public void starteSuche() {
        patientLogic.starteSuche();
    }

    /*****************
     * Dieser EventListener handled alle wesentlichen Funktionen inklusive der
     * CloseWindow-Methode
     *************/
    private void createPatStammListener() {
        patientStammEventListener = new PatStammEventListener() {
            @Override
            public void patStammEventOccurred(PatStammEvent evt) {
                patientLogic.patStammEventOccurred(evt);
            }
        };
        this.ptp = new PatStammEventClass();
        this.ptp.addPatStammEventListener(patientStammEventListener);

    }

    /****************************************************/
    /**
     * Installiert die ActionListeners für alle drei Panels
     *
     */
    private void createActionListeners() {
        toolBarAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                patToolBarPanel.getLogic()
                               .reactOnAction(arg0);
            }
        };
        memoAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                patMemoPanel.doMemoAction(arg0);
            }
        };

    }

    /****************************************************/
    /**
     * Installiert die KeyListeners für alle drei Panels
     *
     */
    private void createKeyListeners() {
        // PateintToolBar
        toolBarKeys = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                patToolBarPanel.getLogic()
                               .reactOnKeyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
            }
        };
    }

    private void createFocusListeners() {
        toolBarFocus = new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                patToolBarPanel.getLogic()
                               .reactOnFocusGained(e);
            }

            @Override
            public void focusLost(FocusEvent e) {
            }

        };
    }

    /****************************************************/
    /**
     * Installiert die MouseListeners für alle drei Panels
     *
     */

    private void createMouseListeners() {
        toolBarMouse = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                patToolBarPanel.getLogic()
                               .reactOnMouseClicked(arg0);
            }
        };

    }

    /****************************************************/
    /**
     *
     * Aufräumarbeiten zuerst die Listener entfernen
     *
     */
    public void allesAufraeumen() {
        stammDatenPanel.fireAufraeumen();
        patToolBarPanel.getLogic()
                       .fireAufraeumen();
        patMemoPanel.fireAufraeumen();
        patMultiFunctionPanel.fireAufraeumen();
        this.ptp.removePatStammEventListener(patientStammEventListener);
        ptp = null;
        patientLogic.fireAufraeumen();
    }

    public void setzeFocus() {
        patientLogic.setzeFocus();
    }

    public void holeWichtigeInfos(String xpatint) {
        String stmt = "select t1.rdatum,t1.rnr,t1.roffen,t1.pat_intern from rgaffaktura as t1 "
                + "join pat5 as t2 on (t1.pat_intern=t2.pat_intern) " + "where t1.roffen > '0' and t1.pat_intern = '"
                + xpatint + "' and NOT t1.rnr like 'sto%'" + "order by t1.rdatum";
        Vector<Vector<String>> vecoffen = SqlInfo.holeFelder(stmt);
        if (vecoffen.size() > 0 || Reha.bHatMerkmale) {
            try {
                infoDlg = new InfoDialogRGAFoffen(xpatint, vecoffen);
                infoDlg.pack();
                infoDlg.setLocationRelativeTo(this);
                infoDlg.setVisible(true);
                infoDlg = null;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        tfsuchen.requestFocus();
                    }
                });
            } catch (Exception ex) {
                if (infoDlg != null) {
                    infoDlg.dispose();
                    infoDlg = null;
                }
            }

        }
    }

    void allesAufNull() {

        aktPatID = "";
        autoPatid = -1;
        getStammDaten().htmlPane.setText("");
        aktRezept.setzeRezeptPanelAufNull(true);
        historie.setzeHistoriePanelAufNull(true);
        berichte.setzeBerichtPanelAufNull(true);
        dokumentation.setzeDokuPanelAufNull(true);
        gutachten.setzeGutachtenPanelAufNull(true);
        pmemo[0].setText("");
        pmemo[1].setText("");
    }
}

/*********** Inner-Class JPatTextField *************/
class JPatTextField extends JRtaTextField {
    /**
     *
     */
    private static final long serialVersionUID = 2904164740273664807L;

    public JPatTextField(String type, boolean selectWhenFocus) {
        super(type, selectWhenFocus);
        setOpaque(false);
        setEditable(false);
        setBorder(null);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                if (arg0.getClickCount() == 2 && arg0.getButton() == 1) {
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            String s1 = "#KORRIGIEREN";
                            String s2 = getName();
                            PatStammEvent pEvt = new PatStammEvent(this);
                            pEvt.setPatStammEvent("PatSuchen");
                            pEvt.setDetails(s1, s2, "");
                            PatStammEventClass.firePatStammEvent(pEvt);
                            return null;
                        }
                    }.execute();
                }
            }
        });
    }
}
