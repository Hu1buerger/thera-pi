package org.therapi.reha.patient;

import java.awt.Color;
import java.awt.Font;
import java.awt.dnd.DropTargetListener;
import java.awt.event.*;
import java.sql.Connection;
import java.util.Vector;

import javax.swing.*;

import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.JCompTools;
import CommonTools.JRtaTextField;
import CommonTools.SqlInfo;
import CommonTools.StringTools;
import dialoge.InfoDialogRGAFoffen;
import events.PatStammEvent;
import events.PatStammEventClass;
import events.PatStammEventListener;
import hauptFenster.Reha;
import hauptFenster.UIFSplitPane;
import rehaInternalFrame.JPatientInternal;

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
    Object sucheComponent = null;

    // ToolBar-Controls & Listener
    JButton[] jbut = { null, null, null, null, null };
    JFormattedTextField tfsuchen;
    ActionListener toolBarAction;
    MouseListener toolBarMouse;
    KeyListener toolBarKeys;
    FocusListener toolBarFocus;
    DropTargetListener dropTargetListener;

    // StammDaten-Controls & Listener
    JPatTextField[] ptfield = new JPatTextField[15];

    // MemoPanel-Controls & Listener
    JTabbedPane memotab = null;

    ActionListener memoAction;
    int inMemo = -1;

    // MultiFunctionPanel-Controls & Listener
    JTabbedPane multiTab = null;
    public AktuelleRezepte aktRezept = null;
    public Historie historie = null;
    TherapieBerichte berichte = null;
    DokumentationPanel dokumentation = null;
    public Gutachten gutachten = null;
    String[] tabTitel = { "aktuelle Rezepte", "Rezept-Historie", "Therapieberichte", "Dokumentation", "Gutachten",
            "Arzt & KK", "Plandaten" };

    JTextArea rezdiag = null;

    ImageIcon[] imgzuzahl = new ImageIcon[4];
    ImageIcon[] imgrezstatus = new ImageIcon[2];
    public Vector<String> patDaten = new Vector<String>();
    public Vector<String> vecaktrez = null;
    public Vector<String> vecakthistor = null;

    // PatStamm-Event Listener == extrem wichtig
    private PatStammEventListener patientStammEventListener = null;
    private PatStammEventClass ptp = null;

    // Instanz-Variable für die einzelnen Panels
    public PatientToolBarPanel patToolBarPanel = null;
    private PatientStammDatenPanel stammDatenPanel = null;
    PatientMemoPanel patMemoPanel = null;
    private PatientMultiFunctionPanel patMultiFunctionPanel = null;

    // Gemeinsam genutzte Variable
    Font font = new Font("Courier New", Font.BOLD, 13);

    public String aktPatID = "";
    int dbPatid = -1;
    public int aid = -1;
    public int kid = -1;
    boolean patDatenOk = false;

    // Bezug zum unterliegenden JInternalFrame
    JPatientInternal patientInternal = null;

    private InfoDialogRGAFoffen infoDlg = null;

    public PatientHauptPanel(String name, JPatientInternal internal, Connection connection) {
        super();
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
        aktRezept = new AktuelleRezepte(this, connection);
        add(constructSplitPaneLR(connection), cc.xyw(1, 3, 3));
        setVisible(true);
        setzeFocus();
    }

    public PatientHauptLogic getLogic() {
        return patientLogic;
    }

    public JPatientInternal getInternal() {
        return patientInternal;
    }

    public void setInternalToNull() {
        patientInternal = null;
    }

    private UIFSplitPane constructSplitPaneLR(Connection connection) {
        UIFSplitPane jSplitLR = UIFSplitPane.createStrippedSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                getStammDatenPatient(connection), constructSplitPaneOU(connection));
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

    private UIFSplitPane constructSplitPaneOU(Connection connection) {

        UIFSplitPane jSplitRechtsOU = UIFSplitPane.createStrippedSplitPane(JSplitPane.VERTICAL_SPLIT, getMemosPatient(),
                getMultiFunctionTab(connection));
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

    private JScrollPane getStammDatenPatient(Connection connection) {
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

    private synchronized JScrollPane getMultiFunctionTab(Connection connection) {

        patMultiFunctionPanel = new PatientMultiFunctionPanel(this, connection);
        JScrollPane jscr = JCompTools.getTransparentScrollPane(patMultiFunctionPanel);
        jscr.validate();
        return jscr;
    }

    private synchronized JXPanel getToolBarPatient() {
        patToolBarPanel = new PatientToolBarPanel(this, patientLogic);
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

    void starteSuche() {
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

            memoAction = (e) -> patMemoPanel.doMemoAction(e,dbPatid);


    }

    /****************************************************/
    /**
     * Installiert die KeyListeners für alle drei Panels
     *
     */
    private void createKeyListeners() {
        // PateintToolBar
        toolBarKeys = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                patToolBarPanel.getLogic()
                               .reactOnKeyPressed(e);
            }

        };
    }

    private void createFocusListeners() {
        toolBarFocus = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                patToolBarPanel.getLogic()
                               .reactOnFocusGained(e);
            }
        };
    }

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
        if (getInternal() != null) {
            setInternalToNull();

        }
    }

    public void setzeFocus() {
        patientLogic.setzeFocus();
    }

    void holeWichtigeInfos(String xpatint) {
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
        dbPatid = -1;
        getStammDaten().htmlPane.setText("");
        aktRezept.setzeRezeptPanelAufNull(true);
        historie.setzeHistoriePanelAufNull(true);
        berichte.setzeBerichtPanelAufNull(true);
        dokumentation.setzeDokuPanelAufNull(true);
        gutachten.setzeGutachtenPanelAufNull(true);
        patMemoPanel.memoPanelAufNull();
    }

    void setPatdaten(Vector<String> patDaten) {
        if (patDaten.size() >= 71) {
            patMemoPanel.getPmemo()[0].setText(patDaten.get(65));

            patMemoPanel.getPmemo()[1].setText(patDaten.get(64));

            dbPatid = Integer.parseInt(patDaten.get(66));
            aid = StringTools.ZahlTest(patDaten.get(67));
            kid = StringTools.ZahlTest(patDaten.get(68));
            patDatenOk = true;
            getStammDaten().parseHTML(true);

        } else {
            JOptionPane.showMessageDialog(null, "Fehler beim Einlesen der Patientendaten");
            patDatenOk = false;
            getStammDaten().parseHTML(false);
        }
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
