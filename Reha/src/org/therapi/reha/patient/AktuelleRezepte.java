package org.therapi.reha.patient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.event.TableColumnModelExtListener;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.IconValues;
import org.jdesktop.swingx.renderer.MappedValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.Colors;
import CommonTools.DatFunk;
import CommonTools.DateTableCellEditor;
import CommonTools.DateTimeFormatters;
import CommonTools.ExUndHop;
import CommonTools.JCompTools;
import CommonTools.JRtaTextField;
import CommonTools.SqlInfo;
import CommonTools.StringTools;
import CommonTools.ini.INIFile;
import CommonTools.ini.INITool;
import Suchen.ICDrahmen;
import abrechnung.AbrechnungPrivat;
import abrechnung.AbrechnungRezept;
import abrechnung.Disziplinen;
import abrechnung.RezeptGebuehrRechnung;
import commonData.Rezeptvector;
import core.Disziplin;
import dialoge.InfoDialog;
import dialoge.InfoDialogTerminInfo;
import dialoge.PinPanel;
import dialoge.RehaSmartDialog;
import dialoge.ToolsDialog;
import environment.Path;
import events.RehaTPEvent;
import events.RehaTPEventClass;
import gui.Cursors;
import hauptFenster.AktiveFenster;
import hauptFenster.Reha;
import hmrCheck.HMRCheck;
import jxTableTools.MyTableStringDatePicker;
import jxTableTools.TableTool;
import krankenKasse.KassenFormulare;
import mandant.Mandant;
import oOorgTools.OOTools;
import patientenFenster.KeinRezept;
// import patientenFenster.rezepte.RezNeuanlage;
import patientenFenster.rezepte.RezNeuanlagePreDisziCheck;
import patientenFenster.rezepte.RezTest;
import patientenFenster.rezepte.RezTestPanel;
import patientenFenster.rezepte.RezeptEditorGUI;
import patientenFenster.rezepte.RezeptGebuehren;
import patientenFenster.rezepte.RezeptFensterTools;
import rechteTools.Rechte;
import rezept.Money;
import rezept.Rezept;
import rezept.RezeptDto;
import rezept.RezeptFertige;
import rezept.Zuzahlung;
import stammDatenTools.KasseTools;
import stammDatenTools.RezTools;
import stammDatenTools.ZuzahlTools;
import stammDatenTools.ZuzahlTools.ZZStat;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;
import systemTools.IconListRenderer;
import systemTools.ListenerTools;

public class AktuelleRezepte extends JXPanel implements ListSelectionListener, TableModelListener,
        TableColumnModelExtListener, PropertyChangeListener, ActionListener {

    private static final long serialVersionUID = 5440388431022834348L;
    private static final Logger logger = LoggerFactory.getLogger(AktuelleRezepte.class);
    JXPanel leerPanel = null;
    JXPanel vollPanel = null;
    JXPanel wechselPanel = null;
    public JLabel anzahlTermine = null;
    public JLabel anzahlRezepte = null;
    public String aktPanel = "";
    public static JXTable tabaktrez = null;
    public JXTable tabaktterm = null;
    public static MyAktRezeptTableModel dtblm;
    public MyTermTableModel dtermm;
    public TableCellEditor tbl = null;
    public boolean rezneugefunden = false;
    public boolean neuDlgOffen = false;
    public String[] indphysio = null;
    public String[] indergo = null;
    public String[] indlogo = null;
    public String[] indpodo = null;
    public RezeptDaten rezDatenPanel = null;
    private JButton btnNeu;
    private JButton btnEdit;
    private JButton btnDel;
    private JButton btnTools;
    private JButton btnArztBericht;
    private JButton btnPrint;
    private List<JButton> allUsedTBButtons;
    // public JButton[] aktrbut = { null, null, null, null, null, null, null, null, null };
    public boolean suchePatUeberRez = false;
    public String rezAngezeigt = "";
    public static boolean inRezeptDaten = false;
    public static boolean inEinzelTermine = false;
    public static boolean initOk = false;
    public JLabel dummyLabel = null;
    private JRtaTextField formularid = new JRtaTextField("NIX", false);
    Vector<String> titel = new Vector<String>();
    Vector<String> formular = new Vector<String>();
    Vector<String> aktTerminBuffer = new Vector<String>();
    RezeptDto rDto = null;
    Rezept aktuelAngezeigtesRezept;   // This should be equal to Reha.instance.patpanel.rezAktRez
    List<Rezept> listAktuelleRez;
    int aktuellAngezeigt = -1;
    int iformular = -1;

    // int MyAktRezeptTableModel.AKTREZTABMODELCOL_ID = 8;
    // int termineInTable = 9;
    
    private static final int REZEPTKOPIERE_NIX = 0;
    private static final int REZEPTKOPIERE_LETZTES = 1;
    private static final int REZEPTKOPIERE_GEWAEHLTES = 2;
    // This bugger is also used in Historie.java...
    static final int REZEPTKOPIERE_HISTORIENREZEPT = 3;


    AbrechnungRezept abrRez = null;

    InfoDialogTerminInfo infoDlg = null;
    String sRezNumNeu = "";
    private Mandant mand;
    private Connection connection;

    public AktuelleRezepte(PatientHauptPanel eltern, Connection connection) {
        
        this.connection = connection;
        mand = Reha.instance.mandant();
        
        rDto = new RezeptDto(mand.ik());

        setOpaque(false);
        setBorder(null);
        setLayout(new BorderLayout());

        leerPanel = new KeinRezept("Keine Rezepte angelegt f\u00fcr diesen Patient");
        leerPanel.setName("leerpanel");
        leerPanel.setOpaque(false);

        JXPanel allesrein = new JXPanel(new BorderLayout());
        allesrein.setOpaque(false);
        allesrein.setBorder(null);

        FormLayout lay = new FormLayout("5dlu,fill:0:grow(1.00),0dlu", "0dlu,p,2dlu,p,2dlu,fill:0:grow(1.00),5dlu");
        CellConstraints cc = new CellConstraints();
        allesrein.setLayout(lay);

        wechselPanel = new JXPanel(new BorderLayout());
        wechselPanel.setOpaque(false);
        wechselPanel.setBorder(null);

        wechselPanel.add(leerPanel, BorderLayout.CENTER);

        aktPanel = "leerPanel";

        allesrein.add(createToolbar(), cc.xy(2, 2));
        allesrein.add(wechselPanel, cc.xy(2, 6));

        add(JCompTools.getTransparentScrollPane(allesrein), BorderLayout.CENTER);
        validate();
        final PatientHauptPanel xeltern = eltern;
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    vollPanel = new JXPanel();
                    // Lemmi 20110105: Layout etwas dynamischer gestaltet
                    FormLayout vplay = new FormLayout("fill:0:grow(0.75),5dlu,fill:0:grow(0.25),5dlu",
                            "13dlu,53dlu,5dlu,fill:0:grow(1.00),0dlu");
                    // Das soll nicht "dynamische" gestaltet werden sondern genau so belassen werden
                    // wie es ist! Ansonsten muss bei den meisten Diagnosen gescrollt werden
                    // und genau das ist Murks in einer View die einem einen schnellen
                    // Gesamtueberblick verschaffen soll!
                    // Steinhilber

                    CellConstraints vpcc = new CellConstraints();
                    vollPanel.setLayout(vplay);
                    vollPanel.setOpaque(false);
                    vollPanel.setBorder(null);

                    Font font = new Font("Tahome", Font.PLAIN, 11);
                    anzahlRezepte = new JLabel("Anzahl Rezepte: 0");
                    anzahlRezepte.setFont(font);
                    vollPanel.add(anzahlRezepte, vpcc.xy(1, 1));
                    vollPanel.add(getTabelle(), vpcc.xywh(1, 2, 1, 1));
                    anzahlTermine = new JLabel("Anzahl Termine: 0");
                    anzahlTermine.setFont(font);
                    anzahlTermine.setOpaque(false);
                    vollPanel.add(anzahlTermine, vpcc.xywh(3, 1, 1, 1));

                    JXPanel dummy = new JXPanel();
                    dummy.setOpaque(false);
                    FormLayout dumlay = new FormLayout(
                            "fill:0:grow(0.25),p,fill:0:grow(0.25),p,fill:0:grow(0.25),p,fill:0:grow(0.25)",
                            "fill:0:grow(1.00),2dlu,p,2dlu");
                    CellConstraints dumcc = new CellConstraints();
                    dummy.setLayout(dumlay);
                    vollPanel.add(dummy, vpcc.xywh(3, 2, 1, 3));

                    dummy.add(getTermine(), dumcc.xyw(1, 1, 7));
                    dummy.add(getTerminToolbar(), dumcc.xyw(1, 3, 7));

                    rezDatenPanel = new RezeptDaten(xeltern);
                    vollPanel.add(rezDatenPanel, vpcc.xyw(1, 4, 1));
                    indiSchluessel();
                    initOk = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    initOk = true;
                }
                return null;
            }

        }.execute();
        new Thread() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        holeFormulare();
                        return;
                    }
                });
            }
        }.start();

    }
    
    /**
     * Creates & returns a "dummy" Panel
     * 
     * @return
     */
    public JXPanel getDatenpanel() {
        FormLayout datenlay = new FormLayout("", "");
        PanelBuilder builder = new PanelBuilder(datenlay);
        builder.getPanel()
               .setOpaque(false);
        JXPanel dumm = new JXPanel(new BorderLayout());
        dumm.setOpaque(false);
        dumm.setBorder(null);
        dumm.add(builder.getPanel(), BorderLayout.CENTER);
        return dumm;
    }

    /**
     * Creates a toolbar and returns it. The toolbar will be populated with buttons/icons
     *   for creating/editing/deleting/... Rezepte
     *   
     * @return a ready built swing-toolbar (JToolbar)
     */
    public JToolBar createToolbar() {
        allUsedTBButtons = new LinkedList<JButton>();
        
        JToolBar jtb = new JToolBar();
        jtb.setOpaque(false);
        jtb.setRollover(true);
        jtb.setBorder(null);
        jtb.setOpaque(false);

        initToolbarBtns();
        
        btnNeu.addActionListener(e -> actionRezNeu(e));
        jtb.add(btnNeu);
        { allUsedTBButtons.add(btnNeu); }
        
        btnEdit.addActionListener(e -> actionRezEdit());
        jtb.add(btnEdit);
        { allUsedTBButtons.add(btnEdit); }
        
        btnDel.addActionListener(e -> actionRezDelete());
        jtb.add(btnDel);
        { allUsedTBButtons.add(btnDel); }
        
        jtb.addSeparator(new Dimension(30, 0));

        btnPrint.addActionListener(e -> actionRezeptBrief());
        jtb.add(btnPrint);
        { allUsedTBButtons.add(btnPrint); }

        btnArztBericht.addActionListener(e -> actionArztBericht());
        jtb.add(btnArztBericht);
        { allUsedTBButtons.add(btnArztBericht); }
        
        jtb.addSeparator(new Dimension(30, 0));

        btnTools.addActionListener(e -> actionWerkzeuge());
        jtb.add(btnTools);
        { allUsedTBButtons.add(btnTools); }

        setzteAlleBtnsStatus(false);
        
        return jtb;
    }
    

    /**
     * Set all buttons to a passed-in status (true/false)
     * 
     * @param btnStatus
     */
    void setzteAlleBtnsStatus(boolean btnStatus) {
        /*
        logger.debug("Setting all buttons to " + btnStatus + " by hand.");
        
        btnNeu.setEnabled(btnStatus);
        btnEdit.setEnabled(btnStatus);
        btnDel.setEnabled(btnStatus);
        btnPrint.setEnabled(btnStatus);
        btnArztBericht.setEnabled(btnStatus);
        btnTools.setEnabled(btnStatus);
        */
        for ( JButton btn : allUsedTBButtons) {
            btn.setEnabled(btnStatus);
        }
    }

    /**
     * Create all buttons used in the JToolbar & initialize them
     * 
     */
    private void initToolbarBtns() {
        // "Neu" Button
        btnNeu = new JButton();
        btnNeu.setName("neu");
        btnNeu.setIcon(SystemConfig.hmSysIcons.get("neu"));
        btnNeu.setToolTipText("<html>neues Rezept anlegen<br><br>"
                + "Halten sie gleichzeitig Die Taste <b><font color='#0000ff'>Shift</font></b> gedr\u00fcckt,<br>"
                + "wird das aktuell unterlegte bzw. <font color='#0000ff'>aktive Rezept</font> das Patienten kopiert!<br><br>"
                + "Halten sie gleichzeitig Die Taste <b><font color='#0000ff'>Strg</font></b> gedr\u00fcckt,"
                + "<br>wird <font color='#0000ff'>das j\u00fcngste Rezept</font> das Patienten kopiert!<br><br></html>");
        btnNeu.setActionCommand("rezneu");
        // allBtns[0] = btnNeu;
        
        // "Edit" Button
        btnEdit = new JButton();
        btnEdit.setName("edit");
        btnEdit.setIcon(SystemConfig.hmSysIcons.get("edit"));
        btnEdit.setToolTipText("aktuelles Rezept \u00e4ndern/editieren");
        btnEdit.setActionCommand("rezedit");
        
        // "Loeschen" Button
        btnDel = new JButton();
        btnDel.setIcon(SystemConfig.hmSysIcons.get("delete"));
        btnDel.setToolTipText("aktuelles Rezept l\u00f6schen");
        btnDel.setActionCommand("rezdelete");
        
        // "Tools" Button
        btnTools = new JButton();
        btnTools.setIcon(SystemConfig.hmSysIcons.get("tools"));
        btnTools.setToolTipText("Werkzeugkiste f\u00fcr aktuelle Rezepte");
        btnTools.setActionCommand("werkzeuge");
        
        // "ArtzBericht" Button
        btnArztBericht = new JButton();
        btnArztBericht.setIcon(SystemConfig.hmSysIcons.get("arztbericht"));
        btnArztBericht.setToolTipText("Arztbericht erstellen/\u00e4ndern");
        btnArztBericht.setActionCommand("arztbericht");
        
        // "Drucken" Button
        btnPrint = new JButton();
        btnPrint.setIcon(SystemConfig.hmSysIcons.get("print"));
        btnPrint.setToolTipText("Rezeptbezogenen Brief/Formular erstellen");
        btnPrint.setActionCommand("rezeptbrief");
    }

    
    // Lemmi Doku: Liste mit den aktuellen Rezepten
    public JXPanel getTabelle() {
        JXPanel dummypan = new JXPanel(new BorderLayout());
        dummypan.setOpaque(false);
        dummypan.setBorder(null);
        dtblm = new MyAktRezeptTableModel();
        String[] column = { "Rezept-Nr.", "bezahlt", "Rez-Datum", "angelegt am", "sp\u00e4t.Beginn", "Status", "Pat-Nr.",
                "Indi.Schl.", "" };
        dtblm.setColumnIdentifiers(column);
        tabaktrez = new JXTable(dtblm);
        tabaktrez.setHighlighters(HighlighterFactory.createSimpleStriping(Colors.PiOrange.alpha(0.25f)));
        tabaktrez.setDoubleBuffered(true);
        tabaktrez.setEditable(false);
        tabaktrez.setSortable(false);

        TableCellRenderer renderer = new DefaultTableRenderer(new MappedValue(StringValues.EMPTY, IconValues.ICON),
                JLabel.CENTER);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        tabaktrez.getColumn(MyAktRezeptTableModel.AKTREZTABMODELCOL_BEZICON)
                 .setMaxWidth(45);
        tabaktrez.getColumn(MyAktRezeptTableModel.AKTREZTABMODELCOL_ANGELEGTDATUM) 
                 .setMaxWidth(75); // Angelegt am
        tabaktrez.getColumn(MyAktRezeptTableModel.AKTREZTABMODELCOL_REZSTATUS)
                 .setMaxWidth(45); // Status

        tabaktrez.getColumn(MyAktRezeptTableModel.AKTREZTABMODELCOL_PATINTERN)
                 .setMinWidth(0); // Pat-Nr.
        tabaktrez.getColumn(MyAktRezeptTableModel.AKTREZTABMODELCOL_PATINTERN)
                 .setMaxWidth(0);

        tabaktrez.getColumn(MyAktRezeptTableModel.AKTREZTABMODELCOL_ID)
                 .setMinWidth(0); // verordn->id
        tabaktrez.getColumn(MyAktRezeptTableModel.AKTREZTABMODELCOL_ID)
                 .setMaxWidth(0);
        for (int i = 0; i < column.length; i++) {
            switch (i) {
            case MyAktRezeptTableModel.AKTREZTABMODELCOL_BEZICON: // Bez. Icons
            case MyAktRezeptTableModel.AKTREZTABMODELCOL_REZSTATUS: // Status
                tabaktrez.getColumn(i)
                         .setCellRenderer(renderer);
                break;
            default: // Text
                tabaktrez.getColumn(i)
                         .setCellRenderer(centerRenderer);
            }
        }
        tabaktrez.validate();
        tabaktrez.setName("AktRez");
        tabaktrez.setSelectionMode(0);

        tabaktrez.getSelectionModel()
                 .addListSelectionListener(new RezepteListSelectionHandler());
        tabaktrez.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                if (arg0.getClickCount() == 2 && arg0.getButton() == 1) {
                    long zeit = System.currentTimeMillis();
                    while (!RezeptDaten.feddisch) {
                        try {
                            Thread.sleep(20);
                            if (System.currentTimeMillis() - zeit > 5000) {
                                JOptionPane.showMessageDialog(null, "Fehler beim Bezug der Rezeptdaten");
                                logger.error("Error after dbl-click on Rezept - took too long to process data (>5s)");
                                return;
                            }
                        } catch (InterruptedException e) {
                            JOptionPane.showMessageDialog(null,
                                    "Fehler beim Bezug der Rezeptdaten\n Bitte Administrator verst\u00e4ndigen (Exception)\n\n"
                                            + e.getMessage());
                            logger.error("Exception after dbl-click on processing data in getTabelle");
                            logger.error(e.getLocalizedMessage());
                            e.printStackTrace();
                        }
                    }
                    if (rezGeschlossenMitWarnung()) {
                        return;
                    }
                    neuanlageRezept(false, "", REZEPTKOPIERE_NIX);
                }
                if (arg0.getClickCount() == 1 && arg0.getButton() == 3) {
                    if (Rechte.hatRecht(Rechte.Funktion_rezgebstatusedit, false)) {
                        Point point = arg0.getPoint();
                        int row = tabaktrez.rowAtPoint(point);
                        if (row < 0) {
                            return;
                        }
                        tabaktrez.columnAtPoint(point);
                        tabaktrez.setRowSelectionInterval(row, row);
                        ZeigePopupMenu(arg0);
                    }
                }
            }
        });
        tabaktrez.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    arg0.consume();
                    if (rezGeschlossenMitWarnung()) {
                        return;
                    }
                    neuanlageRezept(false, "", REZEPTKOPIERE_NIX);
                }
                if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    arg0.consume();
                }
                if (arg0.getKeyCode() == KeyEvent.VK_A && arg0.isControlDown()) {
                    arg0.consume();
                }
                if (arg0.getKeyCode() == KeyEvent.VK_F1) {
                    if (infoDlg != null) {
                        return;
                    }
                    int row = tabaktrez.getSelectedRow();
                    if (row < 0) {
                        return;
                    }
                    String reznummer = InfoDialog.macheNummer(tabaktrez.getValueAt(row, 0)
                                                                       .toString());
                    if (reznummer.equals("")) {
                        return;
                    }
                    infoDlg = new InfoDialogTerminInfo(reznummer, null);
                    infoDlg.pack();
                    infoDlg.setLocationRelativeTo(null);
                    infoDlg.setVisible(true);
                    infoDlg = null;

                }

            }

        });
        dummypan.setPreferredSize(new Dimension(0, 100));
        JScrollPane aktrezscr = JCompTools.getTransparentScrollPane(tabaktrez);
        aktrezscr.getVerticalScrollBar()
                 .setUnitIncrement(15);
        dummypan.add(aktrezscr, BorderLayout.CENTER);
        dummypan.validate();
        return dummypan;
    }

    public void setzeRezeptPanelAufNull(boolean aufnull) {
        // DONE adjust to btnXXXX
        if (aufnull) {
            if (aktPanel.equals("vollPanel")) {
                wechselPanel.remove(vollPanel);
                wechselPanel.add(leerPanel);
                aktPanel = "leerPanel";
                logger.debug("Setze Neu auf true");
                // Originally the code set Neu to true and then iterated over array to set all to false
                //  not sure if the array reflected the current status...
                setzteAlleBtnsStatus(false);
                btnNeu.setEnabled(true);
            } else {
                logger.debug("In else setze Neu auf true");
                btnNeu.setEnabled(true);
            }

        } else {
            if (aktPanel.equals("leerPanel")) {
                wechselPanel.remove(leerPanel);
                wechselPanel.add(vollPanel);
                aktPanel = "vollPanel";
                setzteAlleBtnsStatus(true);
                
            }
        }
    }
    
    private void ZeigePopupMenu(java.awt.event.MouseEvent me) {
        JPopupMenu jPop = getTerminPopupMenu();

        jPop.show(me.getComponent(), me.getX(), me.getY());
    }

    private void ZeigePopupMenu2(java.awt.event.MouseEvent me) {
        JPopupMenu jPop = getBehandlungsartLoeschenMenu();
        jPop.show(me.getComponent(), me.getX(), me.getY());
    }

    // Lemmi Doku: RMT Menue in "aktuelle Rezepte" zur Einstellung des
    // Zuzahlungsstatus
    private JPopupMenu getTerminPopupMenu() {
        JPopupMenu jPopupMenu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Zuzahlungsstatus auf befreit setzen",
                new ImageIcon(Path.Instance.getProghome() + "icons/frei.png"));
        item.setActionCommand("statusfrei");
        item.addActionListener(e -> actionStatusFrei());
        jPopupMenu.add(item); // McM 2016-01 keine Auswirkung auf Abrechnung; RTA intern benutzt fuer
                              // verschieben in die Historie ohne Abrechnung (Rezept-split)
                              // ?? sollte Abrechnung den gesetzten Status verwenden?
        item = new JMenuItem("... auf bereits bezahlt setzen",
                new ImageIcon(Path.Instance.getProghome() + "icons/Haken_klein.gif"));
        item.setActionCommand("statusbezahlt");
        item.addActionListener(e -> actionStatusBezahlt());
        jPopupMenu.add(item); // McM 2016-01 keine Auswirkung auf Abrechnung; RTA intern benutzt
        item = new JMenuItem("... auf nicht bezahlt setzen",
                new ImageIcon(Path.Instance.getProghome() + "icons/Kreuz.png"));
        item.setActionCommand("statusnichtbezahlt");
        item.addActionListener(e -> actionStatusNichtBezahlt());
        jPopupMenu.add(item);

        jPopupMenu.addSeparator();

        // Lemmi 201110106: Knopf zum Kopieren des aktiven Rezeptes zugefuegt
        item = new JMenuItem("Angew\u00e4hltes Rezept kopieren",
                new ImageIcon(Path.Instance.getProghome() + "icons/plus_button_gn_klein.png"));
        item.setActionCommand("KopiereAngewaehltes");
        item.addActionListener(e -> actionKopiereAngewaehltes());
        jPopupMenu.add(item);

        // Lemmi 201110113: Knopf zum Kopieren des juengsten Rezeptes zugefuegt
        item = new JMenuItem("J\u00fcngstes Rezept kopieren",
                new ImageIcon(Path.Instance.getProghome() + "icons/plus_button_bl_klein.png"));
        item.setActionCommand("KopiereLetztes");
        item.addActionListener(e -> actionKopiereLetztes());
        jPopupMenu.add(item);

        jPopupMenu.addSeparator();

        item = new JMenuItem("Angew\u00e4hltes Rezept aufteilen",
                new ImageIcon(Path.Instance.getProghome() + "icons/split.png"));
        item.setActionCommand("RezeptTeilen");
        item.addActionListener(e -> actionRezeptTeilen());
        jPopupMenu.add(item);

        return jPopupMenu;
    }

    private JPopupMenu getBehandlungsartLoeschenMenu() {
        JPopupMenu jPopupMenu = new JPopupMenu();
        JMenuItem item = new JMenuItem("alle im Rezept gespeicherten Behandlungsarten l\u00f6schen");
        item.setActionCommand("deletebehandlungen");
        item.addActionListener(e -> actionDeleteBehandlungen());
        jPopupMenu.add(item);

        item = new JMenuItem("alle Behandlungsarten den Rezeptdaten angleichen");
        item.setActionCommand("angleichenbehandlungen");
        item.addActionListener(e -> actionAngleichenBehandlungen());
        jPopupMenu.add(item);

        jPopupMenu.addSeparator();

        // vvv Lemmi 20110105: aktuellen Behandler auf alle leeren Behandler kopieren
        item = new JMenuItem("gew\u00e4hlten Behandler in alle leeren Behandler-Felder kopieren");
        item.setActionCommand("behandlerkopieren");
        // aktuell gewaehlte Zeile finden - mit Sicherung, wenn keine angewaehlt worden
        // ist !
        int iPos = tabaktterm.getSelectedRow();
        if (iPos < 0 || iPos >= tabaktterm.getRowCount() || tabaktterm.getStringAt(tabaktterm.getSelectedRow(), 1)
                                                                      .isEmpty())
            item.setEnabled(false);
        item.addActionListener(e -> actionBehandlerKopieren());
        jPopupMenu.add(item);

        return jPopupMenu;
    }

    public JToolBar getTerminToolbar() {
        ActionsTermine alt = new ActionsTermine();
        JToolBar jtb = new JToolBar();
        jtb.setOpaque(false);
        jtb.setRollover(true);
        jtb.setBorder(null);
        jtb.setOpaque(false);

        JButton jbut = new JButton();
        jbut.setIcon(SystemConfig.hmSysIcons.get("neu"));
        jbut.setToolTipText("Neuen Termin eintragen");
//        jbut.setActionCommand("terminplus");
        jbut.addActionListener(e -> alt.actionTerminPlus());
        jtb.add(jbut);
        jbut = new JButton();
        jbut.setIcon(SystemConfig.hmSysIcons.get("delete"));
        jbut.setToolTipText("Termin l\u00f6schen");
//        jbut.setActionCommand("terminminus");
        jbut.addActionListener(e -> alt.actionTerminMinus());
        jtb.add(jbut);
        jtb.addSeparator(new Dimension(40, 0));
        jbut = new JButton();
        jbut.setIcon(SystemConfig.hmSysIcons.get("sort"));

//        jbut.setActionCommand("terminsortieren");
        jbut.addActionListener(e -> alt.actionTermineSortieren());
        jbut.setToolTipText("Termine nach Datum sortieren");
        jtb.add(jbut);
        return jtb;
    }

    // Lemmi-Doku: Liste mit den Terminen am aktuellen Rezept
    public JScrollPane getTermine() {

        dtermm = new MyTermTableModel();
        dtermm.addTableModelListener(this);
        String[] column = { "Beh.Datum", "Behandler", "Text", "Beh.Art", "" };
        dtermm.setColumnIdentifiers(column);
        if (SystemConfig.behdatumTippen) {
            tabaktterm = new JXTable(dtermm);
        } else {

            tabaktterm = new JXTable(dtermm) {
                /**
                 *
                 */

                private static final long serialVersionUID = 1L;

                @Override
                public boolean editCellAt(int row, int column, EventObject e) {

                    if (e == null) {
                        return false;
                    }
                    if (e instanceof MouseEvent) {
                        MouseEvent mouseEvent = (MouseEvent) e;
                        if (mouseEvent.getClickCount() > 1) {
                            return super.editCellAt(row, column, e);
                        }
                    } else if (e instanceof ActionEvent) {
                        ActionEvent aktionEvent = (ActionEvent) e;
                        if (aktionEvent.getActionCommand()
                                       .toString()
                                       .length() == 1) {
                            return super.editCellAt(row, column, e);
                        }
                    } else if (e instanceof KeyEvent) {
                        KeyEvent keyEvent = (KeyEvent) e;
                        if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER && !keyEvent.isControlDown()) {
                            if (super.editCellAt(row, column, e)) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }
                    return false;

                }
            };
        }

        tabaktterm.setHighlighters(HighlighterFactory.createSimpleStriping(Colors.PiOrange.alpha(0.25f)));
        tabaktterm.setDoubleBuffered(true);
        tabaktterm.addPropertyChangeListener(this);
        tabaktterm.setEditable(true);
        tabaktterm.setSortable(false);
        tabaktterm.setSelectionMode(0);
        tabaktterm.setHorizontalScrollEnabled(true);

        if (SystemConfig.behdatumTippen) {
            tbl = new DateTableCellEditor();
            tabaktterm.getColumnModel()
                      .getColumn(0)
                      .setCellEditor(tbl);

        } else {
            MyTableStringDatePicker pic = new MyTableStringDatePicker();
            tabaktterm.getColumnModel()
                      .getColumn(0)
                      .setCellEditor(pic);
        }

        tabaktterm.getColumn(0)
                  .setMinWidth(40);
        tabaktterm.getColumn(0)
                  .setMaxWidth(80);
        tabaktterm.getColumn(1)
                  .setMaxWidth(80);
        tabaktterm.getColumn(1)
                  .setMinWidth(60);
        tabaktterm.getColumn(2)
                  .setMinWidth(40);
        tabaktterm.getColumn(3)
                  .setMinWidth(40);
        tabaktterm.getColumn(4)
                  .setMinWidth(0);
        tabaktterm.getColumn(4)
                  .setMaxWidth(0);
        tabaktterm.setOpaque(true);

        if (SystemConfig.behdatumTippen) {
        } else {
            tabaktterm.setAutoStartEditOnKeyStroke(false);
        }

        tabaktterm.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                arg0.consume();
                final MouseEvent xarg0 = arg0;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (xarg0.getButton() == 1) {
                            int row = tabaktterm.getSelectedRow();
                            int col = tabaktterm.getSelectedColumn();
                            if (row >= 0) {
                                tabaktterm.setRowSelectionInterval(row, row);
                                tabaktterm.setColumnSelectionInterval(col, col);
                            }
                        }
                    }
                });
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                arg0.consume();
            }

            @Override
            public void mouseClicked(MouseEvent arg0) {
                arg0.consume();
                if (arg0.getClickCount() == 2) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            int row = tabaktterm.getSelectedRow();
                            int col = tabaktterm.getSelectedColumn();
                            if (row >= 0) {
                                startCellEditing(tabaktterm, row, col);
                            }
                        }
                    });
                    return;
                }
                if (arg0.getButton() == 3) {
                    if (!Rechte.hatRecht(Rechte.Sonstiges_rezeptbehandlungsartloeschen, false)) {
                        return;
                    }
                    ZeigePopupMenu2(arg0);
                }
            }
        });
        tabaktterm.validate();
        tabaktterm.setName("AktTerm");
        JScrollPane termscr = JCompTools.getTransparentScrollPane(tabaktterm);
        termscr.getVerticalScrollBar()
               .setUnitIncrement(15);
        return termscr;
    }

    private void startCellEditing(JXTable table, int row, int col) {
        final int xrows = row;
        final int xcols = col;
        final JXTable xtable = table;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                xtable.editCellAt(xrows, xcols);
            }
        });
    }

    private String macheHtmlTitel(int anz, String titel) {

        String ret = titel + " - " + Integer.toString(anz);
        return ret;
    }

    public void setzeRezeptNummerNeu(String nummer) {
        this.sRezNumNeu = nummer;
    }

    public void holeRezepte(String patint, String rez_nr) {
        final String xpatint = patint;
        final String xrez_nr = rez_nr;
        
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    aktTerminBuffer.clear();
                    aktTerminBuffer.trimToSize();
                    
                    listAktuelleRez = new LinkedList<>(rDto.getAktuelleRezepteByPatNr(Integer.parseInt(xpatint)));
                    // TODO: remove once done with Rezepte
                    Vector<Vector<String>> vec = SqlInfo.holeSaetze("verordn",
                            "rez_nr,zzstatus,"
                                    + "DATE_FORMAT(rez_datum,'%d.%m.%Y') AS drez_datum,"
                                    + "DATE_FORMAT(datum,'%d.%m.%Y') AS datum,"
                                    + "DATE_FORMAT(lastdate,'%d.%m.%Y') AS datum,"
                                    + "abschluss,pat_intern,indikatschl,id,termine",
                            "pat_intern='" + xpatint + "' ORDER BY rez_datum", Arrays.asList(new String[] {}));
                    int anz = listAktuelleRez.size();
                    for (int i = 0; i < anz; i++) {
                        if (i == 0) {
                            dtblm.setRowCount(0);
                        }
                        // TODO: remove once Rezepte has been sorted
                        // Comment the following line to disable vec-based termine to be added
                        // aktTerminBuffer.add(String.valueOf(vec.get(i).get(MyAktRezeptTableModel.AKTREZTABMODELCOL_TERMINE)));
                        aktTerminBuffer.add(listAktuelleRez.get(i).getTermine());
                        // TODO: irgendwo lief mir ein ZZStatus wo 3 == bald18 bedeutet?
                        int iZuZahlStat = 3, rezstatus = 0;
                        ZZStat iconKey;
                        if (((Vector) vec.get(i)).get(MyAktRezeptTableModel.AKTREZTABMODELCOL_BEZICON) == null) { // McM: zzstatus leer heisst 'befreit'??
                            iZuZahlStat = 0; // ?? nicht besser 'not set' ??
                            logger.debug("ZZStatus not set (def. NULL)");
                        } else if (!((Vector) vec.get(i)).get(MyAktRezeptTableModel.AKTREZTABMODELCOL_BEZICON)
                                                         .equals("")) {
                            logger.debug("ZZStatus is not empty");
                            iZuZahlStat = Integer.parseInt(((Vector) vec.get(i))
                                                                .get(MyAktRezeptTableModel.AKTREZTABMODELCOL_BEZICON)
                                                                    .toString());
                            logger.debug("Set iZuZahlStat to " + iZuZahlStat);
                        }
                        /* for an int this ain't gonna work/necc.
                        if (lDtoRezepte.get(i).getZZSTATUS() == null ) {
                            iZuZahlStat = 0; // ?? nicht besser 'not set' ??
                            logger.debug("ZZStatus not set (def. NULL)");
                        } else 
                        
                        if (!lDtoRezepte.get(i).getZZSTATUS().equals("")) {
                            logger.debug("ZZStatus is not empty");
                            */
                        logger.debug("iZuZahlStat from Vec: " + iZuZahlStat);
                        iZuZahlStat = listAktuelleRez.get(i).getZZStatus();
                        logger.debug("iZuZahlStat from Rez: " + iZuZahlStat);
                        //}
                        final String testreznum = String.valueOf(vec.get(i)
                                                                    .get(MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr));
                        logger.debug("testreznum from Vec: " + testreznum);
                        logger.debug("testreznum from rezDto: " + listAktuelleRez.get(i).getRezNr());
                        iconKey = ZuzahlTools.getIconKey(iZuZahlStat, testreznum);

                        if (((Vector) vec.get(i)).get(MyAktRezeptTableModel.AKTREZTABMODELCOL_REZSTATUS)
                                                 .equals("T")) {
                            rezstatus = 1;
                        }
                        logger.debug("rezStatus from vec:" + rezstatus);
                        if(listAktuelleRez.get(i).isAbschluss())
                            rezstatus = 1; // Enum?
                        logger.debug("rezStatus from Rez:" + rezstatus);

                        
                        // Comment following block to disable vec-based Rezepte to be shown
                        /*
                        dtblm.addRow(vec.get(i)); // Rezept in Tabelle eintragen
                        // Icons in akt. Zeile setzen
                        dtblm.setValueAt(ZuzahlTools.getZzIcon(iconKey), i,
                                                                MyAktRezeptTableModel.AKTREZTABMODELCOL_BEZICON);
                        dtblm.setValueAt(Reha.instance.patpanel.imgrezstatus[rezstatus], i,
                                                                MyAktRezeptTableModel.AKTREZTABMODELCOL_REZSTATUS);
                        */
                        // Using Rezepte, we collected all vals, so here's the selection for the panel:
                        Object[] fields = new Object[]{
                                listAktuelleRez.get(i).getRezNr(),
                                listAktuelleRez.get(i).getZZStatus(),
                                DatFunk.sDatInDeutsch(listAktuelleRez.get(i).getRezDatum().toString()),
                                DatFunk.sDatInDeutsch(listAktuelleRez.get(i).getErfassungsDatum().toString()),
                                DatFunk.sDatInDeutsch(listAktuelleRez.get(i).getLastDate().toString()),
                                listAktuelleRez.get(i).isAbschluss(),
                                listAktuelleRez.get(i).getPatIntern(),
                                listAktuelleRez.get(i).getIndikatSchl(),
                                listAktuelleRez.get(i).getId(),
                                listAktuelleRez.get(i).getTermine()
                        };
                        // And now add them: 
                        dtblm.addRow((Object[]) fields);

                        // Icons in akt. Zeile setzen
                        dtblm.setValueAt(ZuzahlTools.getZzIcon(iconKey), i,
                                                        MyAktRezeptTableModel.AKTREZTABMODELCOL_BEZICON);
                        dtblm.setValueAt(Reha.instance.patpanel.imgrezstatus[rezstatus], i,
                                                        MyAktRezeptTableModel.AKTREZTABMODELCOL_REZSTATUS);

                        // TODO: remove once sorted rezepte
                        if (vec.get(i)
                               .get(MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr)
                               .startsWith("RH") && Reha.instance.dta301panel != null) {
                            new SwingWorker<Void, Void>() {
                                @Override
                                protected Void doInBackground() throws Exception {
                                    Reha.instance.dta301panel.aktualisieren(testreznum);
                                    return null;
                                }
                            }.execute();
                        }
                        if (listAktuelleRez.get(i).getRezNr().startsWith("RH") && Reha.instance.dta301panel != null) {
                            new SwingWorker<Void, Void>() {
                                @Override
                                protected Void doInBackground() throws Exception {
                                    Reha.instance.dta301panel.aktualisieren(testreznum);
                                    return null;
                                }
                            }.execute();
                        }

                        // TODO: Check for Rezepte-vec usage
                        if (i == 0) {
                            if (suchePatUeberRez) {
                                suchePatUeberRez = false;
                            }
                        }
                    }
                    /************** Bis hierher hat man die Saetze eingelesen ********************/
                    try {
                        Reha.instance.patpanel.multiTab.setTitleAt(0, macheHtmlTitel(anz, "aktuelle Rezepte"));
                    } catch (Exception ex) {
                        logger.error("Timingprobleme beim setzen des Reitertitels - Reiter: aktuelle Rezepte");
                    }
                    int row = 0;
                    if (anz > 0) {
                        setzeRezeptPanelAufNull(false);
                        if (xrez_nr.length() > 0) {
                            row = 0;
                            rezneugefunden = true;
                            for (int ii = 0; ii < anz; ii++) {
                                if (tabaktrez.getValueAt(ii, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr)
                                             .equals(xrez_nr)) {
                                    row = ii;
                                    break;
                                }

                            }
                            // TODO: remove me once Rezepte has been sorted
                            Reha.instance.patpanel.vecaktrez = (SqlInfo.holeSatz("verordn", " * ",
                                    "id = '" + String.valueOf(tabaktrez.getValueAt(row, MyAktRezeptTableModel.AKTREZTABMODELCOL_ID)) + "'",
                                    Arrays.asList(new String[] {})));
                            logger.debug("vecaktrez from Vec: " + Reha.instance.patpanel.vecaktrez.toString());
                            aktuelAngezeigtesRezept = listAktuelleRez.get(row);
                            Reha.instance.patpanel.rezAktRez = aktuelAngezeigtesRezept;
                            logger.debug("rezAktrez from Rez: " + Reha.instance.patpanel.rezAktRez.toString());
                            // TODO: revisit once Rezepte has been sorted
                            rezDatenPanel.setRezeptDaten((String) tabaktrez.getValueAt(row, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr),
                                    String.valueOf(tabaktrez.getValueAt(row, MyAktRezeptTableModel.AKTREZTABMODELCOL_ID)));
                        } else {
                            rezneugefunden = true;
                            // TODO: remove me once Rezepte has been sorted
                            Reha.instance.patpanel.vecaktrez = (SqlInfo.holeSatz("verordn", " * ",
                                    "id = '" + String.valueOf(tabaktrez.getValueAt(row, MyAktRezeptTableModel.AKTREZTABMODELCOL_ID)) + "'",
                                    Arrays.asList(new String[] {})));
                            logger.debug("vecaktrez from Vec: " + Reha.instance.patpanel.vecaktrez.toString());
                            aktuelAngezeigtesRezept = listAktuelleRez.get(row);
                            Reha.instance.patpanel.rezAktRez = aktuelAngezeigtesRezept;
                            logger.debug("vecaktrez from Rez: " + Reha.instance.patpanel.rezAktRez.toString());
                            rezDatenPanel.setRezeptDaten((String) tabaktrez.getValueAt(0, 0),
                                    String.valueOf(tabaktrez.getValueAt(0, MyAktRezeptTableModel.AKTREZTABMODELCOL_ID)));
                        }

                        try {
                            if (!inEinzelTermine) {
                                inEinzelTermine = true;
                                try {
                                    if (aktTerminBuffer.size() > row) {
                                        holeEinzelTermineAusRezept("", aktTerminBuffer.get(row));
                                    } else {
                                        termineInTabelle(null);
                                    }

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                aktuellAngezeigt = row;
                                tabaktrez.setRowSelectionInterval(row, row);
                                tabaktrez.scrollRowToVisible(row);
                                rezAngezeigt = tabaktrez.getValueAt(row, 0)
                                                        .toString()
                                                        .trim();
                                inEinzelTermine = false;
                            }

                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Fehler in holeEinzelTermine-2");
                            JOptionPane.showMessageDialog(null, ex.getMessage());
                            inEinzelTermine = false;
                        }

                        anzahlRezepte.setText("Anzahl Rezepte: " + anz);
                        wechselPanel.revalidate();
                        wechselPanel.repaint();

                    } else {
                        setzeRezeptPanelAufNull(true);
                        rezAngezeigt = "";
                        anzahlRezepte.setText("Anzahl Rezepte: " + anz);
                        wechselPanel.revalidate();
                        wechselPanel.repaint();
                        dtblm.setRowCount(0);
                        dtermm.setRowCount(0);
                        aktuellAngezeigt = -1;
                        // TODO: delete me once Rezepte have been sorted & checked new Rez() is safe to use here
                        if (Reha.instance.patpanel.vecaktrez != null) {
                            // replaced .clear by " = new Vec<>();
                            Reha.instance.patpanel.vecaktrez = new Vector<String>();
                        }
                        if (aktuelAngezeigtesRezept != null) {
                            aktuelAngezeigtesRezept = new Rezept();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Fehler in der Funktion holeRezepte()\n" + ex.getMessage());
                    inEinzelTermine = false;
                }
                return null;
            }

        }.execute();

    }
    
    public void setzeKarteiLasche() {
        if (tabaktrez.getRowCount() == 0) {
            holeRezepte(Reha.instance.patpanel.patDaten.get(29), "");
            Reha.instance.patpanel.multiTab.setTitleAt(0, macheHtmlTitel(tabaktrez.getRowCount(), "aktuelle Rezepte"));
        } else {
            Reha.instance.patpanel.multiTab.setTitleAt(0, macheHtmlTitel(tabaktrez.getRowCount(), "aktuelle Rezepte"));
        }
    }

    // TODO: Remove once Rezepte has been sorted
    public void aktualisiereVector(String rid) {
        String[] strg = {};
        Reha.instance.patpanel.vecaktrez = (SqlInfo.holeSatz("verordn", " * ", "id = '" + rid + "'",
                Arrays.asList(strg)));
        setRezeptDaten();
    }

    public void aktualisiereRezAktRez(int Id) {
        aktuelAngezeigtesRezept = rDto.byRezeptId(Id).get();
        // TODO: check if we also need to update Reha.instance.patpanel.rezAktRez
        setRezeptDaten();
    }
    
    public void setRezeptDaten() {
        int row = tabaktrez.getSelectedRow();
        if (row >= 0) {
            String reznr = (String) tabaktrez.getValueAt(row, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr);
            rezAngezeigt = reznr;
            String id = String.valueOf(tabaktrez.getValueAt(row, MyAktRezeptTableModel.AKTREZTABMODELCOL_ID));
            rezDatenPanel.setRezeptDaten(reznr, id);
        }
    }

    public void updateEinzelTermine(String einzel) {
        String[] tlines = einzel.split("\n");
        int lines = tlines.length;
        Vector<String> tvec = new Vector<String>();
        dtermm.setRowCount(0);
        String[] terdat = null;
        for (int i = 0; i < lines; i++) {
            terdat = tlines[i].split("@");
            int ieinzel = terdat.length;
            tvec.clear();
            for (int y = 0; y < ieinzel; y++) {
                if (y == 0) {
                    tvec.add(String.valueOf((terdat[y].trim()
                                                      .equals("") ? "  .  .    " : terdat[y])));
                    if (i == 0) {
                        SystemConfig.hmAdrRDaten.put("<Rerstdat>", (terdat[y].trim()
                                                                             .equals("") ? "  .  .    " : terdat[y]));
                    }
                } else {
                    tvec.add(terdat[y]);
                }
            }
            dtermm.addRow((Vector<?>) tvec.clone());
        }
        tabaktterm.validate();
        tabaktterm.repaint();
        anzahlTermine.setText("Anzahl Termine: " + lines);
        if (lines > 0) {
            tabaktterm.setRowSelectionInterval(lines - 1, lines - 1);
        }
        SystemConfig.hmAdrRDaten.put("<Rletztdat>", (terdat[0].trim()
                                                              .equals("") ? "  .  .    " : terdat[0]));
        SystemConfig.hmAdrRDaten.put("<Ranzahltage>", Integer.toString(lines));

    }

    public void setzeBild(int satz, int icon) {
        dtblm.setValueAt(Reha.instance.patpanel.imgzuzahl[icon], satz, MyAktRezeptTableModel.AKTREZTABMODELCOL_BEZICON);
        tabaktrez.validate();
        tabaktrez.repaint();
    }

    private void termineAufNull() {
        dtermm.setRowCount(0);
        tabaktterm.validate();
        anzahlTermine.setText("Anzahl Termine: 0");
        SystemConfig.hmAdrRDaten.put("<Rletztdat>", "");
        SystemConfig.hmAdrRDaten.put("<Rerstdat>", "");
        SystemConfig.hmAdrRDaten.put("<Ranzahltage>", "0");
    }

    /**
     * Collects the termine from a given RzNr - it's possible we can do w/o the SQL, since we already get all data
     * for a Rezept and only display a selection thereof in aktRez-tab. The Vector based code only had a limited number
     * of fields selected at this point in time
     * 
     * @param xreznr
     * @param termine
     */
    public void holeEinzelTermineAusRezept(String xreznr, String termine) {
        try {
            Vector<String> xvec = null;
            Vector<Vector<String>> retvec = new Vector<Vector<String>>();
            String terms = null;
            if (termine == null) {
                xvec = SqlInfo.holeSatz("verordn", "termine,pat_intern", "rez_nr='" + xreznr + "'",
                        Arrays.asList(new String[] {}));
                if (xvec.size() == 0) {
                    termineAufNull();
                    return;
                } else {
                    terms = xvec.get(0);
                }
            } else {
                terms = termine;
            }
            if (terms == null) {
                termineAufNull();
                return;
            }
            if (terms.equals("")) {
                termineAufNull();
                return;
            }
            String[] tlines = terms.split("\n");
            int lines = tlines.length;
            Vector<String> tvec = new Vector<String>();
            String stage = "";

            for (int i = 0; i < lines; i++) {
                tvec.clear();
                String[] terdat = tlines[i].split("@");
                int ieinzel = terdat.length;
                for (int y = 0; y < ieinzel; y++) {
                    if (y == 0) {
                        tvec.add(String.valueOf((terdat[y].trim()
                                                          .equals("") ? "  .  .    " : terdat[y])));
                        stage = stage + (i > 0 ? ", " : "") + (terdat[y].trim()
                                                                        .equals("") ? "  .  .    " : terdat[y]);
                    } else {
                        tvec.add(String.valueOf(terdat[y]));
                    }
                }
                retvec.add((Vector<String>) tvec.clone());

            }
            SystemConfig.hmAdrRDaten.put("<Rtage>", String.valueOf(stage));
            Comparator<Vector> comparator = new Comparator<Vector>() {
                @Override
                public int compare(Vector o1, Vector o2) {
                    String s1 = (String) o1.get(4);
                    String s2 = (String) o2.get(4);
                    return s1.compareTo(s2);
                }
            };
            Collections.sort(retvec, comparator);
            termineInTabelle((Vector) retvec.clone());
        } catch (Exception ex) {
            ex.printStackTrace();
            termineInTabelle(null);
        }
    }

    private void termineInTabelle(Vector<Vector<String>> terms) {
        dtermm.setRowCount(0);
        if (terms != null) {
            for (int i = 0; i < terms.size(); i++) {
                if (i == 0) {
                    SystemConfig.hmAdrRDaten.put("<Rerstdat>", (terms.get(i)
                                                                     .get(0)
                                                                     .equals("") ? "  .  .    "
                                                                             : String.valueOf(terms.get(i)
                                                                                                   .get(0))));
                }
                dtermm.addRow(terms.get(i));
            }
            SystemConfig.hmAdrRDaten.put("<Rletztdat>", (terms.get(terms.size() - 1)
                                                              .get(0)
                                                              .equals("") ? "  .  .    "
                                                                      : String.valueOf(terms.get(terms.size() - 1)
                                                                                            .get(0))));
            tabaktterm.validate();
            anzahlTermine.setText("Anzahl Termine: " + terms.size());
            SystemConfig.hmAdrRDaten.put("<Ranzahltage>", Integer.toString(terms.size()));
        } else {
            SystemConfig.hmAdrRDaten.put("<Rletztdat>", "  .  .    ");
            tabaktterm.validate();
            anzahlTermine.setText("Anzahl Termine: " + "0");
            SystemConfig.hmAdrRDaten.put("<Ranzahltage>", "0");
        }
    }

    private void holeEinzelTermineAktuell(int row, Vector<String> vvec, String aufruf) {
        inEinzelTermine = true;
        Vector<String> xvec = null;
        if (vvec == null) {
            xvec = SqlInfo.holeSatz("verordn", "termine", "id='" + tabaktrez.getValueAt(row, MyAktRezeptTableModel.AKTREZTABMODELCOL_ID) + "'",
                    Arrays.asList(new String[] {}));
        } else {
            xvec = vvec;
        }

        String terms = xvec.get(0);
        if (terms == null || terms.isEmpty()) {
            dtermm.setRowCount(0);
            tabaktterm.validate();
            anzahlTermine.setText("Anzahl Termine: 0");
            SystemConfig.hmAdrRDaten.put("<Rletztdat>", "");
            SystemConfig.hmAdrRDaten.put("<Rerstdat>", "");
            SystemConfig.hmAdrRDaten.put("<Ranzahltage>", "0");
            inEinzelTermine = false;
            return;
        }
        String[] tlines = terms.split("\n");
        int lines = tlines.length;

        Vector tvec = new Vector();
        dtermm.setRowCount(0);
        String[] terdat = null;
        for (int i = 0; i < lines; i++) {
            terdat = tlines[i].split("@");
            int ieinzel = terdat.length;
            tvec.clear();
            for (int y = 0; y < ieinzel; y++) {
                if (y == 0) {
                    tvec.add(String.valueOf((terdat[y].trim()
                                                      .equals("") ? "  .  .    " : terdat[y])));
                    if (i == 0) {
                        SystemConfig.hmAdrRDaten.put("<Rerstdat>", String.valueOf((terdat[y].trim()
                                                                                            .equals("") ? "  .  .    "
                                                                                                    : terdat[y])));
                    }
                } else {
                    tvec.add(String.valueOf(terdat[y]));
                }
            }
            dtermm.addRow((Vector<?>) tvec.clone());
        }
        tabaktterm.validate();
        tabaktterm.repaint();
        anzahlTermine.setText("Anzahl Terimine: " + lines);
        if (lines > 0) {
        }
        SystemConfig.hmAdrRDaten.put("<Rletztdat>", (terdat[0].trim()
                                                              .equals("") ? "  .  .    " : terdat[0]));
        SystemConfig.hmAdrRDaten.put("<Ranzahltage>", Integer.toString(lines));
        inEinzelTermine = false;
    }

    public void termineSpeichern() {
        int reihen = dtermm.getRowCount();
        StringBuffer sb = new StringBuffer();
        String sdat = "";
        for (int i = 0; i < reihen; i++) {
            sdat = (dtermm.getValueAt(i, 0) != null ? ((String) dtermm.getValueAt(i, 0)).trim() : ".  .");
            if (i == 0) {
                SystemConfig.hmAdrRDaten.put("<Rerstdat>", sdat);
            }
            if (i == (reihen - 1)) {
                SystemConfig.hmAdrRDaten.put("<Rletztdat>", sdat);
            }

            dtermm.setValueAt((sdat.equals(".  .") ? " " : DatFunk.sDatInSQL(sdat)), i, 4);
            sb.append((sdat.equals(".  .") ? "  .  .    @" : sdat) + "@");
            sb.append((dtermm.getValueAt(i, 1) != null ? ((String) dtermm.getValueAt(i, 1)).trim() : "") + "@");
            sb.append((dtermm.getValueAt(i, 2) != null ? ((String) dtermm.getValueAt(i, 2)).trim() : "") + "@");
            sb.append((dtermm.getValueAt(i, 3) != null ? ((String) dtermm.getValueAt(i, 3)).trim() : "") + "@");
            sb.append((dtermm.getValueAt(i, 4) != null ? ((String) dtermm.getValueAt(i, 4)).trim() : "") + "\n");
        }
        SystemConfig.hmAdrRDaten.put("<Ranzahltage>", Integer.toString(reihen));
        // TODO: Change to Rezept-DTO
        // SqlInfo.aktualisiereSatz("verordn", "termine='" + sb.toString() + "'", "id='"
        //          + (int) tabaktrez.getValueAt(tabaktrez.getSelectedRow(), MyAktRezeptTableModel.AKTREZTABMODELCOL_ID) + "'");
        int rezId = (int) tabaktrez.getValueAt(tabaktrez.getSelectedRow(), MyAktRezeptTableModel.AKTREZTABMODELCOL_ID);
        rDto.updateRezeptTermine(rezId, sb.toString());
        
        // TODO: Remove me once Rezepte has been sorted
        Reha.instance.patpanel.vecaktrez.set(34, sb.toString());
        aktuelAngezeigtesRezept.setTermine(sb.toString());
        if (aktuellAngezeigt >= 0) {
            try {
                aktTerminBuffer.set(aktuellAngezeigt, sb.toString());
            } catch (Exception ex) {
                logger.error("Error in updating aktuellAngezeigt terminliste");
                logger.error(ex.getLocalizedMessage());
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
    }

    @Override
    public void tableChanged(TableModelEvent arg0) {
        int col = arg0.getColumn();
        int type = arg0.getType();

        if ((col >= 0 && col < 4 && type == TableModelEvent.UPDATE)) {
            final int xcol = col;
            new Thread() {
                @Override
                public void run() {
                    termineSpeichern();
                }
            }.start();

        }

    }

    private void starteTests() {
        new Thread() {
            @Override
            public void run() {
                Rezept rez = aktuelAngezeigtesRezept;
                // TODO: change to rez
                if (rez.isUnter18()) {
                    logger.debug("Rez: is under 18");
                }
                if (Reha.instance.patpanel.vecaktrez.get(60)
                                                    .equals("T")) {
                    logger.debug("Vec: is under 18");
                    Vector<String> tage = new Vector<String>();
                    Vector<?> v = dtermm.getDataVector();
                    for (int i = 0; i < v.size(); i++) {
                        tage.add((String) ((Vector<?>) v.get(i)).get(0));
                    }
                    ZuzahlTools.unter18TestDirekt(tage, true, false);
                }
                // Pat-Daten Jahrfrei
                if (!Reha.instance.patpanel.patDaten.get(69)
                                                    .equals("")) {
                    ZuzahlTools.jahresWechselTest(Reha.instance.patpanel.vecaktrez.get(1), true, false);
                }
            }
        }.start();
    }

    @Override
    public void valueChanged(ListSelectionEvent arg0) {
        if ((arg0.getFirstIndex()) >= 0) {
            if (!arg0.getValueIsAdjusting()) {

            }
        }

    }

    class RezepteListSelectionHandler implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (rezneugefunden) {
                rezneugefunden = false;
                return;
            }
            if (!RezeptDaten.feddisch) {
                return;
            }
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();

            if (e.getValueIsAdjusting()) {
                return;
            }
            if (lsm.isSelectionEmpty()) {

            } else {
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                        RezeptDaten.feddisch = false;
                        final int ix = i;
                        datenHolenUndEinstellen();
                        break;
                    }
                }
            }
        }
    }

    public boolean datenHolenUndEinstellen() {
        try {
            if (suchePatUeberRez) {
                suchePatUeberRez = false;
                return false;
            }
            int ix = tabaktrez.getSelectedRow();
            setCursor(Cursors.wartenCursor);
            if (!inEinzelTermine) {
                try {
                    inEinzelTermine = true;

                    try {
                        holeEinzelTermineAusRezept("", aktTerminBuffer.get(ix));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    aktuellAngezeigt = tabaktrez.getSelectedRow();
                    inEinzelTermine = false;

                } catch (Exception ex) {
                    inEinzelTermine = false;
                }
            }
            // TODO: delete me once Rezepte has been sorted
            Reha.instance.patpanel.vecaktrez = (SqlInfo.holeSatz("verordn", " * ",
                    "id = '" + String.valueOf(tabaktrez.getValueAt(ix, MyAktRezeptTableModel.AKTREZTABMODELCOL_ID))
                       + "'", Arrays.asList(new String[] {})));
            aktuelAngezeigtesRezept = rDto.byRezeptId(Integer.parseInt(String.valueOf(
                                                                    tabaktrez.getValueAt(ix,
                                                                            MyAktRezeptTableModel.AKTREZTABMODELCOL_ID)))).get();
            Reha.instance.patpanel.rezAktRez = aktuelAngezeigtesRezept;
            // Huh??
            Reha.instance.patpanel.aktRezept.rezAngezeigt = (String) tabaktrez.getValueAt(ix,
                                                                      MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr);

            rezDatenPanel.setRezeptDaten(String.valueOf(tabaktrez.getValueAt(ix,
                                                                      MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr)),
                    String.valueOf(tabaktrez.getValueAt(ix, MyAktRezeptTableModel.AKTREZTABMODELCOL_ID)));
            setCursor(Cursors.normalCursor);
            final String testreznum = tabaktrez.getValueAt(ix, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr)
                                               .toString();

            try {
                if ((testreznum.startsWith("RH")) && (Reha.instance.dta301panel != null)) {
                    Reha.instance.dta301panel.aktualisieren(testreznum);
                }
            } catch (Exception ex) {
                logger.error("In datenHolenUndEinstellen: Check for RH & dta301Panel failed");
                logger.error(ex.getLocalizedMessage());
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            logger.error("In datenHolenUndEinstellen: general failure");
            logger.error(ex.getLocalizedMessage());
            setCursor(Cursors.normalCursor);
            ex.printStackTrace();
            inEinzelTermine = false;
        }
        return true;
    }

    public static boolean isDentist(String sindi) {
        String[] aindi = { "CD1 a", "CD1 b", "CD1 c", "CD1 d", "CD2 a", "CD2 b", "CD2 c", "CD2 d", "ZNSZ", "CSZ a",
                "CSZ b", "CSZ c", "LYZ1", "LYZ2", "SPZ", "SCZ", "OFZ" };
        return Arrays.asList(aindi)
                     .indexOf(sindi) >= 0;
    }

    public void indiSchluessel() {
        indphysio = new String[] { "kein IndiSchl.", "AT1 a", "AT1 b", "AT1 c", "AT2 a", "AT2 b", "AT2 c", "AT3 a",
                "AT3 b", "AT3 c", "CS a", "CS b", "EX1 a", "EX1 b", "EX1 c", "EX2 a", "EX2 b", "EX2 c", "EX2 d",
                "EX3 a", "EX3 b", "EX3 c", "EX3 d", "EX4 a", "GE a", "LY1 a", "LY1 b", "LY2 a", "LY3 a", "PN a", "PN b",
                "PN c", "SO1 a", "SO2 a", "SO3 a", "SO4 a", "SO5 a", "WS1 a", "WS1 b", "WS1 c", "WS1 d", "WS1 e",
                "WS2 a", "WS2 b", "WS2 c", "WS2 d", "WS2 e", "WS2 f", "WS2 g", "ZN1 a", "ZN1 b", "ZN1 c", "ZN2 a",
                "ZN2 b", "ZN2 c", "CD1 a", "CD1 b", "CD1 c", "CD1 d", "CD2 a", "CD2 b", "CD2 c", "CD2 d", "ZNSZ",
                "CSZ a", "CSZ b", "CSZ c", "LYZ1", "LYZ2", "k.A." };

        indergo = new String[] { "kein IndiSchl.", "EN1", "EN2", "EN3", "EN4", "SB1", "SB2", "SB3", "SB4", "SB5", "SB6",
                "SB7", "PS1", "PS2", "PS3", "PS4", "PS5", "k.A." };
        indlogo = new String[] { "kein IndiSchl.", "RE1", "RE2", "SC1", "SC2", "SF", "SP1", "SP2", "SP3", "SP4", "SP5",
                "SP6", "ST1", "ST2", "ST3", "ST4", "SPZ", "SCZ", "OFZ", "k.A." };
        indpodo = new String[] { "kein IndiSchl.", "DFa", "DFb", "DFc", "k.A." };

    }

    @Override
    public void columnPropertyChange(PropertyChangeEvent arg0) {
    }

    @Override
    public void columnAdded(TableColumnModelEvent arg0) {

    }

    @Override
    public void columnMarginChanged(ChangeEvent arg0) {

    }

    @Override
    public void columnMoved(TableColumnModelEvent arg0) {

    }

    @Override
    public void columnRemoved(TableColumnModelEvent arg0) {

    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent arg0) {

    }

    public void holeFormulare() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                INIFile inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/",
                        "rezept.ini");
                int forms = inif.getIntegerProperty("Formulare", "RezeptFormulareAnzahl");
                for (int i = 1; i <= forms; i++) {
                    titel.add(inif.getStringProperty("Formulare", "RFormularText" + i));
                    formular.add(inif.getStringProperty("Formulare", "RFormularName" + i));
                }
                return null;
            }

        }.execute();

    }

    @Override
    public void actionPerformed(ActionEvent arg0) {

        String cmd = arg0.getActionCommand();

        for (int i = 0; i < 1; i++) {
            /*
            if (cmd.equals("terminplus")) {
                actionTerminPlus();
                break;               
            }
            *
            if (cmd.equals("terminminus")) {
                actionTerminMinus();
                break;
            }
            *
            if (cmd.equals("terminsortieren")) {
                actionTermineSortieren();
                break;
            }
            *
            if (cmd.equals("rezneu")) {
                actionRezNeu(arg0);

                break;
            }
            *
            if (cmd.equals("rezedit")) {
                actionRezEdit();
                break;
            }
            *
            if (cmd.equals("rezdelete")) {
                actionRezDelete();

            }
            */
            /**
             * Can't find where this one is defined - will leave it active for now...
             */
            if (cmd.equals("rezeptgebuehr")) {
                logger.error("Who fired me & where? " + arg0.toString());
                actionRezGebuehr();
            }
            // dito
            if (cmd.equals("barcode")) {
                logger.error("Who fired me & where? " + arg0.toString());
                actionBarcode();
            }
            /*
            if (cmd.equals("arztbericht")) {
                actionArztBericht();
            }
            */
            if (cmd.equals("ausfallrechnung")) {
                logger.error("Who fired me & where? " + arg0.toString());
                actionAusfallRechnung();
            }
            /*
            if (cmd.equals("statusfrei")) {
                actionStatusFrei();
            }
            *
            if (cmd.equals("statusbezahlt")) {
                actionStatusBezahlt();
            }
            *
            if (cmd.equals("statusnichtbezahlt")) {
                actionStatusNichtBezahlt();
            }
            *
            if (cmd.equals("KopiereAngewaehltes")) {
                actionKopiereAngewaehltes();
            }
            if (cmd.equals("KopiereLetztes")) {
                actionKopiereLetztes();
            }
            *
            if (cmd.equals("rezeptbrief")) {
                actionRezeptBrief();
            }
            */
            if (cmd.equals("rezeptabschliessen")) {
                logger.error("Who fired me & where? " + arg0.toString());
                actionRezeptAbschliessen();
            }
            /*
            if (cmd.equals("werkzeuge")) {
                actionWerkzeuge();
            }
            *
            if (cmd.equals("deletebehandlungen")) {
                actionDeleteBehandlungen();
            }
            *
            if (cmd.equals("angleichenbehandlungen")) {
                actionAngleichenBehandlungen();
            }
            *
            if (cmd.equals("behandlerkopieren")) {
                actionBehandlerKopieren();
            }
            *
            if (cmd.equals("RezeptTeilen")) {
                actionRezeptTeilen();
            }
            */
        }
    }

    /**
     * 
     */
    private void actionRezeptTeilen() {
        JOptionPane.showMessageDialog(null,
                "<html>Diese Funktion ist noch nicht implementiert.<br><br>Bitte wenden Sie sich "
                        + "im Forum unter www.Thera-Pi.org an Teilnehmern <b>letzter3</b>!<br>Das w\u00e4re n\u00e4mlich seine "
                        + "Lieblingsfunktion - so es sie g\u00e4be....<br><br><html>");
    }

    /**
     * nimmt den Behandler aus der aktuell markierten Zeile und kopiert ihn auf alle
     * leeren Behandlerfelder
     */
    private void actionBehandlerKopieren() {
        if (this.tabaktterm.getRowCount() <= 0) {
            return;
        }

        // aktuell gewaehlte Zeile finden - mit Sicherung, wenn keine angewaehlt worden
        // ist !
        int iPos = tabaktterm.getSelectedRow();
        if (iPos < 0 || iPos >= tabaktterm.getRowCount())
            return;

        // Behandler aus aktuell angewaehler Zeile holen
        String strBehandler = tabaktterm.getStringAt(tabaktterm.getSelectedRow(), 1);
        if (!strBehandler.isEmpty()) {
            for (int i = 0; i < tabaktterm.getRowCount(); i++) {
                if (tabaktterm.getStringAt(i, 1)
                              .isEmpty()) // nur wenn der Behandler leer ist eintragen.
                    tabaktterm.setValueAt(strBehandler, i, 1);
            }
            termineSpeichern();
        }
    }

    /**
     * 
     */
    private void actionAngleichenBehandlungen() {
        if (this.tabaktterm.getRowCount() <= 0) {
            return;
        }
        Rezept rez = aktuelAngezeigtesRezept;
        Vector<Vector<String>> vec = RezTools.macheTerminVector(this.aktTerminBuffer.get(aktuellAngezeigt));
        dtermm.setRowCount(0);
        // TODO: check after change to Rezepte-class
        for (int i = 0; i < vec.size(); i++) {
            // POS1(48)-4(51):
            vec.get(i)
               .set(3, (rez.getHMPos1())
                       + (rez.getHMPos2().isEmpty() ? "" : "," + rez.getHMPos2())
                       + (rez.getHMPos3().isEmpty() ? "" : "," + rez.getHMPos3())
                       + (rez.getHMPos4().trim().isEmpty() ? "" : "," + rez.getHMPos4()));
            dtermm.addRow(vec.get(i));
        }
        termineSpeichern();
    }

    /**
     * 
     */
    private void actionDeleteBehandlungen() {
        if (this.tabaktterm.getRowCount() <= 0) {
            return;
        }
        Vector<Vector<String>> vec = RezTools.macheTerminVector(this.aktTerminBuffer.get(aktuellAngezeigt));
        dtermm.setRowCount(0);
        for (int i = 0; i < vec.size(); i++) {
            vec.get(i)
               .set(3, "");
            dtermm.addRow(vec.get(i));
        }
        termineSpeichern();
    }

    /**
     * 
     */
    private void actionWerkzeuge() {
        new ToolsDlgAktuelleRezepte("", btnTools.getLocationOnScreen());
    }

    /**
     * 
     */
    private void actionRezeptAbschliessen() {
        rezeptAbschliessen();
    }

    /**
     * 
     */
    private void actionRezeptBrief() {
        int row = tabaktrez.getSelectedRow();
        if (row >= 0) {
            iformular = -1;
            KassenFormulare kf = new KassenFormulare(Reha.getThisFrame(), titel, formularid);
            Point pt = btnPrint.getLocationOnScreen();
            kf.setLocation(pt.x - 100, pt.y + 32);
            kf.setModal(true);
            kf.setVisible(true);
            if (!formularid.getText()
                           .equals("")) {
                iformular = Integer.valueOf(formularid.getText());
            }
            kf = null;
            if (iformular >= 0) {
                new SwingWorker<Void, Void>() {

                    @Override
                    protected Void doInBackground() throws Exception {
                        RezTools.constructRawHMap();
                        OOTools.starteStandardFormular(Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK() + "/"
                                + formular.get(iformular), null);
                        return null;
                    }
                }.execute();

            }
        } else {
            iformular = -1;
        }
    }

    /**
     * 
     */
    private void actionKopiereLetztes() {
        neuanlageRezept(true, "", REZEPTKOPIERE_LETZTES);
    }

    /**
     * 
     */
    private void actionKopiereAngewaehltes() {
        neuanlageRezept(true, "", REZEPTKOPIERE_GEWAEHLTES);
    }

    /**
     * 
     */
    private void actionStatusNichtBezahlt() {
        if (!Rechte.hatRecht(Rechte.Rezept_editvoll, true)) {
            return;
        }
        if (rezGeschlossenMitWarnung()) {
            return;
        }
        if (rezBefreitMitWarnung()) {
            return;
        }
        int currow = tabaktrez.getSelectedRow();
        String xreznr;
        if (currow >= 0) {
            xreznr = (String) tabaktrez.getValueAt(currow, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr);
            // TODO: delete me once Rezepte have been sorted
            doVectorAktualisieren(new int[] { 13, 14, 39 }, new String[] { "0.00", "F", "2" }); // rez_geb,
                                                                                                // rez_bez,zzstatus
                                                                                                // (zuzahlnichtok)
            // The old way - change values in pat-haupt-rez
            // TODO: A better way: trigger re-read dataset for rezNr
            aktuelAngezeigtesRezept.setRezGeb(new Money("0.00"));
            aktuelAngezeigtesRezept.setRezBez(false);
            aktuelAngezeigtesRezept.setZZStatus(Zuzahlung.ZZSTATUS_NOTOK);
            
            String xcmd = "update verordn set zzstatus='" + Zuzahlung.ZZSTATUS_NOTOK + "', rez_geb='0.00',rez_bez='F' "
                                                                              + "where rez_nr='" + xreznr + "' LIMIT 1";
            // SqlInfo.sqlAusfuehren(xcmd);
            // TODO: insert save-to-db
            rDto.updateRezeptGebuehrenParameter(Zuzahlung.ZZSTATUS_NOTOK,
                                                new Money("0.00"),
                                                false,
                                                aktuelAngezeigtesRezept.isBefr(),
                                                xreznr);

            if (SystemConfig.useStornieren) {
                if (stammDatenTools.ZuzahlTools.existsRGR(xreznr)) {
                    /**
                     * McM: stellt in Tabelle rgaffaktura 'storno_' vor Rechnungsnummer u. haengt 'S'
                     * an Rezeptnummer an, dadurch wird record bei der Suche nach
                     * Rechnungs-/Rezeptnummer nicht mehr gefunden <roffen> wird nicht 0 gesetzt,
                     * falls schon eine Teilzahlung gebucht wurde o.ae. - in OP taucht er deshalb
                     * noch auf
                     */
                    xcmd = "UPDATE rgaffaktura SET rnr=CONCAT('storno_',rnr), reznr=CONCAT(reznr,'S') where reznr='"
                            + xreznr + "' AND rnr like 'RGR-%' LIMIT 1";
                    SqlInfo.sqlAusfuehren(xcmd); // storniert RGR in 'rgaffaktura'
                    // McM: storno auch in 'kasse' (falls RGR schon als 'bar bezahlt' verbucht
                    // wurde)
                    // auf einnahme = 0 u. 'storno_RGR...' aendern (da Kassenabrechnung nach 'RGR-%'
                    // sucht)
                    if (stammDatenTools.ZuzahlTools.existsRgrBarInKasse(xreznr)) {
                        // TODO ?? user & IK auf den stornierenden aendern?
                        xcmd = "UPDATE kasse SET einnahme='0.00', ktext=CONCAT('storno_',ktext) where rez_nr='"
                                + xreznr + "' AND ktext like 'RGR-%' LIMIT 1";
                        SqlInfo.sqlAusfuehren(xcmd); // storniert RGR in 'kasse'
                    }
                } else {
                    xcmd = "UPDATE kasse SET einnahme='0.00', ktext=CONCAT('storno_',ktext) where rez_nr='"
                            + xreznr + "' AND ktext not like 'storno%' LIMIT 1";
                    SqlInfo.sqlAusfuehren(xcmd); // storniert Bar-Zuzahlung in 'kasse'
                }
            } else { // Ursprungs-Variante (Steinhilber)
                if (stammDatenTools.ZuzahlTools.existsRGR(xreznr)) {
                    SqlInfo.sqlAusfuehren("delete from rgaffaktura where reznr='" + xreznr
                            + "' and rnr like 'RGR-%' LIMIT 1"); // loescht RGR
                }
                SqlInfo.sqlAusfuehren("delete from kasse where rez_nr='" + xreznr + "' LIMIT 1"); // loescht
                                                                                                  // Bar-Zuzahlung
                                                                                                  // _und_ bar
                                                                                                  // bez. RGR
            }

            // ZZ-Icon in akt. Zeile setzen
            setZuzahlImageActRow(ZZStat.ZUZAHLNICHTOK, xreznr);
        }
    }

    /**
     * 
     */
    private void actionStatusBezahlt() {
        if (!Rechte.hatRecht(Rechte.Rezept_editvoll, true)) {
            return;
        }
        if (rezGeschlossenMitWarnung()) {
            return;
        }
        int currow = tabaktrez.getSelectedRow();
        String xreznr = null;
        if (currow >= 0) {
            xreznr = (String) tabaktrez.getValueAt(currow, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr);
            String xcmd = "update verordn set zzstatus='" + Zuzahlung.ZZSTATUS_OK + "', befr='F',rez_bez='T' where rez_nr='"
                    + xreznr + "' LIMIT 1";
            // SqlInfo.sqlAusfuehren(xcmd);
            dtblm.setValueAt(Reha.instance.patpanel.imgzuzahl[1], currow, 1);
            tabaktrez.validate();
            // TODO: delete me once Rezepte have been sorted
            doVectorAktualisieren(new int[] { 12, 14, 39 }, new String[] { "F", "T", "1" }); // befr, rez_bez,
                                                                                             // zzstatus
                                                                                             // (zuzahlok)
            
            // TODO: A better way: trigger re-read dataset for rezNr
            rDto.updateRezeptGebuehrenParameter(Zuzahlung.ZZSTATUS_OK,
                                                aktuelAngezeigtesRezept.getRezGeb(),
                                                true,
                                                false,
                                                xreznr);
            aktuelAngezeigtesRezept.setBefr(false);
            aktuelAngezeigtesRezept.setRezBez(true);
            aktuelAngezeigtesRezept.setZZStatus(Zuzahlung.ZZSTATUS_OK);
        }
    }

    /**
     * 
     */
    private void actionStatusFrei() {
        if (!Rechte.hatRecht(Rechte.Rezept_editvoll, true)) {
            return;
        }
        if (rezGeschlossenMitWarnung()) {
            return;
        }
        int currow = tabaktrez.getSelectedRow();
        String xreznr;
        if (currow >= 0) {
            xreznr = (String) tabaktrez.getValueAt(currow, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr);
            // TODO: move this sql-stmt to Rezepte-class
            String xcmd = "update verordn set zzstatus='" + 0 + "', befr='T',rez_bez='F' where rez_nr='"
                    + xreznr + "' LIMIT 1";
            SqlInfo.sqlAusfuehren(xcmd);
            dtblm.setValueAt(Reha.instance.patpanel.imgzuzahl[0], currow, MyAktRezeptTableModel.AKTREZTABMODELCOL_BEZICON);
            tabaktrez.validate();
            // TODO: modify to Rezepte-class:
            doVectorAktualisieren(new int[] { 12, 14, 39 }, new String[] { "T", "F", "0" }); // befr, rez_bez,
                                                                                             // zzstatus
                                                                                             // (befreit)
            // The old way - change values in pat-haupt-rez
            // TODO: A better way: trigger re-read dataset for rezNr
            aktuelAngezeigtesRezept.setBefr(true);
            aktuelAngezeigtesRezept.setRezBez(false);
            aktuelAngezeigtesRezept.setZZStatus(Zuzahlung.ZZSTATUS_BEFREIT);
            
            SqlInfo.sqlAusfuehren("delete from kasse where rez_nr='" + xreznr + "' LIMIT 1");
        }
    }

    /**
     * 
     */
    private void actionAusfallRechnung() {
        if (!Rechte.hatRecht(Rechte.Rezept_ausfallrechnung, true)) {
            return;
        }
        ausfallRechnung();
    }

    /**
     * 
     */
    private void actionArztBericht() {
        if (!Rechte.hatRecht(Rechte.Rezept_thbericht, true)) {
            return;
        }
        // hier muss noch getestet werden:
        // 1 ist es eine Neuanlage oder soll ein bestehender Ber. editiert werden
        // 2 ist ein Ber. ueberhaupt angefordert
        // 3 gibt es einen Rezeptbezug oder nicht
        if (aktPanel.equals("leerPanel")) {
            JOptionPane.showMessageDialog(null, "Ich sag jetz nix....\n\n"
                    + "....au\u00dfer - und f\u00fcr welches der nicht vorhandenen Rezepte wollen Sie einen Therapiebericht erstellen....");
            return;
        }

        boolean neuber = true;
        int berid = 0;
        String xreznr;
        String xverfasser = "";
        int currow = tabaktrez.getSelectedRow();
        if (currow >= 0) {
            xreznr = (String) tabaktrez.getValueAt(currow, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr);
        } else {
            xreznr = "";
        }
        int iexistiert = Reha.instance.patpanel.berichte.berichtExistiert(xreznr);
        if (iexistiert > 0) {
            xverfasser = Reha.instance.patpanel.berichte.holeVerfasser();
            neuber = false;
            berid = iexistiert;
            String meldung = "<html>F\u00fcr das Rezept <b>" + xreznr
                    + "</b> existiert bereits ein Bericht.<br>Vorhandener Bericht wird jetzt ge\u00f6ffnet</html>";
            JOptionPane.showMessageDialog(null, meldung);
        }
        final boolean xneuber = neuber;
        final String xxreznr = xreznr;
        final int xberid = berid;
        final int xcurrow = currow;
        final String xxverfasser = xverfasser;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    ArztBericht ab = new ArztBericht(null, "arztberichterstellen", xneuber, xxreznr, xberid, 0,
                            xxverfasser, "", xcurrow);
                    ab.setModal(true);
                    ab.setLocationRelativeTo(null);
                    ab.toFront();
                    ab.setVisible(true);
                    ab = null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
                return null;
            }

        }.execute();
    }

    /**
     * 
     */
    private void actionBarcode() {
        doBarcode();
    }

    /**
     * 
     */
    private void actionRezGebuehr() {
        if (!Rechte.hatRecht(Rechte.Rezept_gebuehren, true)) {
            return;
        }
        rezeptGebuehr();
    }

    /**
     * 
     */
    private void actionRezDelete() {
        if (!Rechte.hatRecht(Rechte.Rezept_delete, true)) {
            return;
        }
        if (aktPanel.equals("leerPanel")) {
            JOptionPane.showMessageDialog(null, "Oh Herr la\u00df halten...\n\n"
                    + "....und welches der nicht vorhandenen Rezepte m\u00f6chten Sie bittesch\u00f6n l\u00f6schen....");
            return;
        }
        if (rezGeschlossenMitWarnung()) {
            return;
        }
        int currow = tabaktrez.getSelectedRow();
        if (currow == -1) {
            JOptionPane.showMessageDialog(null, "Kein Rezept zum -> l\u00f6schen <- ausgew\u00e4hlt");
            return;
        }
        String reznr = (String) tabaktrez.getValueAt(currow, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr);
        int rezid = (int) tabaktrez.getValueAt(currow, MyAktRezeptTableModel.AKTREZTABMODELCOL_ID);
        int frage = JOptionPane.showConfirmDialog(null, "Wollen Sie das Rezept " + reznr + " wirklich l\u00f6schen?",
                "Wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
        if (frage == JOptionPane.NO_OPTION) {
            return;
        }
        // TODO: Move these sql-statement to RezeptDto:
        String sqlcmd = "delete from verordn where id='" + rezid + "'";
        SqlInfo.sqlAusfuehren(sqlcmd);
        sqlcmd = "delete from fertige where id='" + rezid + "'";
        new ExUndHop().setzeStatement(sqlcmd);
        RezTools.loescheRezAusVolleTabelle(reznr);
        aktTerminBuffer.remove(currow);

        currow = TableTool.loescheRow(tabaktrez, Integer.valueOf(currow));
        int uebrig = tabaktrez.getRowCount();

        anzahlRezepte.setText("Anzahl Rezepte: " + Integer.toString(uebrig));
        Reha.instance.patpanel.multiTab.setTitleAt(0, macheHtmlTitel(uebrig, "aktuelle Rezepte"));
        if (uebrig <= 0) {
            holeRezepte(Reha.instance.patpanel.patDaten.get(29), "");
        } else {
        }
    }

    /**
     * 
     */
    private void actionRezEdit() {
        if (aktPanel.equals("leerPanel")) {
            JOptionPane.showMessageDialog(null, "Oh Herr la\u00df halten...\n\n"
                    + "....und welches der nicht vorhandenen Rezepte m\u00f6chten Sie bittesch\u00f6n \u00e4ndern....");
            return;
        }
        if (rezGeschlossenMitWarnung()) {
            return;
        }
        neuanlageRezept(false, "", REZEPTKOPIERE_NIX);
    }

    /**
     * @param ae
     */
    private void actionRezNeu(ActionEvent ae) {
        if (!Rechte.hatRecht(Rechte.Rezept_anlegen, true)) {
            return;
        }
        if (Reha.instance.patpanel.autoPatid <= 0) {
            JOptionPane.showMessageDialog(null, "Oh Herr la\u00df halten...\n\n"
                    + "....und f\u00fcr welchen Patienten wollen Sie ein neues Rezept anlegen....");
            return;
        }
        boolean bCtrlPressed = ((ae.getModifiers() & KeyEvent.CTRL_MASK) == KeyEvent.CTRL_MASK);
        boolean bShiftPressed = ((ae.getModifiers() & KeyEvent.SHIFT_MASK) == KeyEvent.SHIFT_MASK);
        boolean bAltPressed = ((ae.getModifiers() & KeyEvent.ALT_MASK) == KeyEvent.ALT_MASK);
        // TODO: kopierModus inits w/ zero - should that maybe be "new" i.e. no copy?
        int kopierModus = REZEPTKOPIERE_NIX;
        if (bCtrlPressed)
            kopierModus = REZEPTKOPIERE_LETZTES;
        else if (bShiftPressed)
            kopierModus = REZEPTKOPIERE_GEWAEHLTES;
        else if (bAltPressed)
            kopierModus = REZEPTKOPIERE_HISTORIENREZEPT;
        neuanlageRezept(true, "", kopierModus);
    }
    
    private class ActionsTermine {
        /**
         * 
         */
        private void actionTermineSortieren() {
            if (rezGeschlossenMitWarnung()) {
                return;
            }
            int row = tabaktterm.getRowCount();
            if (row > 1) {
    
                Vector<Vector<String>> vec = (Vector<Vector<String>>) dtermm.getDataVector()
                                                                            .clone();
    
                Comparator<Vector<String>> comparator = new Comparator<Vector<String>>() {
                    @Override
                    public int compare(Vector<String> o1, Vector<String> o2) {
                        String s1 = o1.get(4);
                        String s2 = o2.get(4);
                        return s1.compareTo(s2);
                    }
                };
                Collections.sort(vec, comparator);
                dtermm.setRowCount(0);
                for (int y = 0; y < vec.size(); y++) {
                    dtermm.addRow(vec.get(y));
                }
                tabaktterm.validate();
                new Thread() {
                    @Override
                    public void run() {
                        termineSpeichern();
                        fuelleTage();
                    }
                }.start();
            }
        }
    
        /**
         * 
         */
        private void actionTerminMinus() {
            if (rezGeschlossenMitWarnung()) {
                return;
            }
            int row = tabaktterm.getSelectedRow();
            if (row >= 0) {
                dtermm.removeRow(row);
                tabaktterm.validate();
                if (tabaktterm.getRowCount() > 0) {
                    tabaktterm.setRowSelectionInterval(tabaktterm.getRowCount() - 1, tabaktterm.getRowCount() - 1);
                }
                anzahlTermine.setText("Anzahl Termine: " + tabaktterm.getRowCount());
    
                new Thread() {
                    @Override
                    public void run() {
                        termineSpeichern();
                        starteTests();
                    }
                }.start();
    
            }
        }
    
        private void actionTerminPlus() {
            if (rezGeschlossenMitWarnung()) {
                return;
            }
            try {
                // TODO: delete me once Rezepte has been sorted
                Object[] objTerm;
                objTerm = RezTools.BehandlungenAnalysieren(Reha.instance.patpanel.vecaktrez.get(1), false,
                        false, false, null, null, null, DatFunk.sHeute()); // hier noch ein Point Object uebergeben
                logger.debug("Vec: objTerm = " + objTerm);
                objTerm = RezTools.BehandlungenAnalysieren(aktuelAngezeigtesRezept.getRezNr(), false,
                        false, false, null, null, null, DatFunk.sHeute()); // hier noch ein Point Object uebergeben
                logger.debug("Rez: objTerm = " + objTerm);
    
                if (objTerm == null) {
                    return;
                }
    
                if ((Integer) objTerm[1] == RezTools.REZEPT_IST_BEREITS_VOLL) {
                    logger.error("Rezept ist bereits voll");
                } else if ((Integer) objTerm[1] == RezTools.REZEPT_ABBRUCH) {
                    logger.error("Rezept ist abgebrochen");
                    return;
                } else {
                    Vector<String> vec = new Vector<String>();
                    vec.add(DatFunk.sHeute());
                    vec.add("");
                    vec.add("");
                    vec.add(((String) objTerm[0]).split("@")[3]);
                    dtermm.addRow((Vector<String>) vec.clone());
                    termineSpeichern();
                    starteTests();
                    if ((Integer) objTerm[1] == RezTools.REZEPT_IST_JETZ_VOLL) {
                        try {
                            // TODO: adjusted to Rezepte-class - check if ok
                            RezTools.fuelleVolleTabelle((aktuelAngezeigtesRezept.getRezNr()), Reha.aktUser);
                        } catch (Exception ex) {
                            logger.debug("Fehler beim Aufruf von 'fuelleVolleTabelle'");
                            JOptionPane.showMessageDialog(null, "Fehler beim Aufruf von 'fuelleVolleTabelle'");
                        }
                    }
                }
                tabaktterm.validate();
                int tanzahl = tabaktterm.getRowCount();
                anzahlTermine.setText("Anzahl Terimine: " + Integer.toString(tanzahl));
                if (tanzahl > 0) {
                    tabaktterm.setRowSelectionInterval(tanzahl - 1, tanzahl - 1);
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        tabaktterm.scrollRowToVisible(tabaktterm.getRowCount());
                    }
    
                });
                tabaktterm.validate();
                tabaktterm.repaint();
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    
        }
    }

    public static String getActiveRezNr() {
        int row = AktuelleRezepte.tabaktrez.getSelectedRow();
        if (row >= 0) {
            return AktuelleRezepte.tabaktrez.getValueAt(row, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr)
                                            .toString();
        }
        return null;
    }

    // TODO: delete me once Rezepte have been sorted - ihhh! this is called ext. by RezeptGebuehren - the two methods hereafter simulate old way
    public void doVectorAktualisieren(int[] elemente, String[] werte) {
        for (int i = 0; i < elemente.length; i++) {
            Reha.instance.patpanel.vecaktrez.set(elemente[i], werte[i]);
        }
    }
    public void setAktuellesRezeptBezahlt(boolean status) {
        aktuelAngezeigtesRezept.setRezBez(status);
    }
    public void setAktRezZZStatus(int status) {
        aktuelAngezeigtesRezept.setZZStatus(status);
    }

    private void rezeptAbschliessen() {
        try {
            if (this.neuDlgOffen) {
                return;
            }
            Rezept rez = aktuelAngezeigtesRezept;
            int pghmr;
            // TODO: delete me after Rezepte has been sorted
            pghmr = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(41));
            logger.debug("Vec: pghmr=" + pghmr);
            pghmr = aktuelAngezeigtesRezept.getPreisGruppe();
            logger.debug("Rez: pghmr=" + pghmr);
            String disziplin = StringTools.getDisziplin(Reha.instance.patpanel.vecaktrez.get(1));
            logger.debug("Vec: diszi=" + disziplin);
            // TODO: change to new RezeptNummern & Disziplin classes
            disziplin = Disziplin.ofShort(rez.getRezClass()).medium;
            logger.debug("Rez: diszi=" + disziplin);
            if (SystemPreislisten.hmHMRAbrechnung.get(disziplin)
                                                 .get(pghmr - 1) < 1) {
                String meldung = "Die Tarifgruppe dieser Verordnung unterliegt nicht den Heilmittelrichtlinien.\n\n"
                        + "Abschlie\u00dfen des Rezeptes ist nicht erforderlich";
                JOptionPane.showMessageDialog(null, meldung);
                return;
            }
            doAbschlussTest(rez);
            if (Reha.instance.abrechnungpanel != null) {

                int currow = tabaktrez.getSelectedRow();
                if (currow < 0) {
                    return;
                }
                if (dtblm.getValueAt(currow, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZSTATUS) == null) { // kein Status-Icon (mehr) gesetzt
                    Reha.instance.abrechnungpanel.einlesenErneuern(null);
                } else {
                    String aktDisziplin = Reha.instance.abrechnungpanel.disziSelect.getCurrDisziKurz();
                    // TODO: done changed to Rezepte
                    if (RezTools.getDisziplinFromRezNr(rez.getRezNr())
                                                                .equals(aktDisziplin)) {
                        // Rezept gehoert zu der Sparte, die gerade im Abrechnungspanel geoeffnet ist
                        Reha.instance.abrechnungpanel.einlesenErneuern(rez.getRezNr());
                    } else {
                        Reha.instance.abrechnungpanel.einlesenErneuern(null);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void ausfallRechnung() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                AusfallRechnung ausfall = new AusfallRechnung(btnTools.getLocationOnScreen());
                ausfall.setModal(true);
                ausfall.toFront();
                ausfall.setVisible(true);
                ausfall = null;
                return null;
            }
        }.execute();
    }

    private void rezeptGebuehr() {
        if (aktPanel.equals("leerPanel")) {
            JOptionPane.showMessageDialog(null, "Ich sag jetz nix....\n\n"
                    + "....au\u00dfer - und von welchem der nicht vorhandenen Rezepte wollen Sie Rezeptgeb\u00fchren kassieren....");
            return;
        }
        int currow = tabaktrez.getSelectedRow();
        if (currow == -1) {
            JOptionPane.showMessageDialog(null, "Kein Rezept zum -> kassieren <- ausgew\u00e4hlt");
            return;
        }
        doRezeptGebuehr(btnTools.getLocationOnScreen());
    }

    private void doAbschlussTest(Rezept rez) {
        int currow = tabaktrez.getSelectedRow();
        if (currow < 0) {
            return;
        }
        // if (dtblm.getValueAt(currow, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZSTATUS) == null) {
        if (!rez.isAbschluss()) {
            // derzeit offen also abschliessen
            if (!Rechte.hatRecht(Rechte.Rezept_lock, true)) {
                return;
            }

            // Is this guaranteed the same as Reha.instance.patpanel.rezAktRez? If so, we can skip this and use
            //  passed in rez
            // f.i. lets see who shouts...
            // Rezept rezToTest = rDto.byRezeptNr((String) tabaktrez.getValueAt(currow, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr)).get();
            int anzterm = rez.AnzahlTermineInRezept();
            if (anzterm <= 0) {
                return;
            }
            /*
            String vgldat1 = (String) tabaktrez.getValueAt(currow, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZDATUM);
            logger.debug("vgldat1 in tabakrez: " + vgldat1);
            LocalDate rezDat = rez.getRezDatum();
            vgldat1 = rez.getRezDatum().format(DateTimeFormatters.ddMMYYYYmitPunkt);
            logger.debug("vgldat1 in rez: " + vgldat1 + " as localdate: " + rezDat);
            String vgldat2 = (String) dtermm.getValueAt(0, 0);
            logger.debug("vgldat2 in tabakrez: " + vgldat2);
            LocalDate ersterBehTermin = LocalDate.parse(
                                                (rez.getTermine().split("\n")[0]).split("@")[0], // Von 1.Zeile alles vor '@'
                                                DateTimeFormatters.ddMMYYYYmitPunkt);
            logger.debug("vlgdat2 as localdate: " + ersterBehTermin);
            */
            // String vgldat3 = (String) tabaktrez.getValueAt(currow, MyAktRezeptTableModel.AKTREZTABMODELCOL_SPAETBEHBEG);
            // logger.debug("vlgdat3 in tabakrez: " + vgldat3);
            // LocalDate spaetesterAnfang = rez.getLastDate();
            // logger.debug("vlgdat3 as localdate: " + spaetesterAnfang);
            String vglreznum = tabaktrez.getValueAt(currow, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr)
                                        .toString();
            logger.debug("vglRezNum in tabakrez: " + vglreznum);
            vglreznum = rez.getRezNr();
            logger.debug("vglRezNum in rez: " + vglreznum);

            // TODO: delete me after Rezepte have been sorted
            int dummypeisgruppe = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(41)) - 1;
            logger.debug("Vec: dummyPG=" + dummypeisgruppe);
            dummypeisgruppe = rez.getPreisGruppe() - 1;
            logger.debug("Rez: dummyPG=" + dummypeisgruppe);

            // TODO: this whole block can be moved to own method/class (aktuelleRezepteChecks.java?)
            //  passing patId & rezNr as params
            /*********************/
            // First block: various Patientenstammdaten checks
            if (!pruefePatDatenVorRezAbschluss(rez))
                return;

            // Ok, now we've got enough valid Patientenstammdaten to bill a Rezept
            
            /*********************/
            // Next Block, are the Termine within legal range of HMV?
            
            // TODO: Change to new RezeptNummern & Diszi-class
            String diszi = RezTools.getDisziplinFromRezNr(rez.getRezNr());
            logger.debug("Diszi from rezTools: " + diszi);
            logger.debug("Diszi from rez: " + rez.getRezClass());

            if (!doTageTest(rez)) {
                logger.debug("TageTest with Rezept determined not HMR-conform");
                return;
            }
            /*
            if (!doTageTest(vgldat3, vgldat2, anzterm, diszi, preisgruppe - 1)) {
                return;
            }
            */

            /*********************/
            // Next Block, are Termine of this Rezept also listed in other Rezepte?
            // RezepteTools rezTools = new RezepteTools(mand.ik());
            // this originally used (way) above rezToTezt
            List<String[]> doubletten = RezeptFensterTools.doDoublettenTest(rez, mand.ik());
            if (doubletten.size() > 0) {
                String msg = "<html><b><font color='#ff0000'>Achtung!</font><br><br>"
                        + "Ein oder mehrere Behandlungstage wurden in anderen Rezepten entdeckt/abgerechnet</b><br><br>";
                for (String[] doublette : doubletten) {
                    msg = msg + "Behandlungstag: " + doublette[1]
                            + " - enthalten in Rezept: " + doublette[0]
                            + " - Standort: " + doublette[2]
                            + "<br>";
                }
                msg = msg + "<br><br>Wollen Sie das Rezept trotzdem abschlie\u00dfen?</html>";
                int frage = JOptionPane.showConfirmDialog(null, msg, "Behandlungsdaten in anderen Rezepten erfa\u00dft",
                        JOptionPane.YES_NO_OPTION);
                if (frage != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            /*********************/
            // Next Block, check IndikationsSchluessel & associated ICD10
            int idtest = 0;
            String indi = rez.getIndikatSchl();
            if (indi.equals("") || indi.contains("kein IndiSchl.")) {
                JOptionPane.showMessageDialog(null,
                        "<html><b>Kein Indikationsschl\u00fcssel angegeben.<br>Die Angaben sind "
                        + "<font color='#ff0000'>nicht</font> gem\u00e4\u00df den g\u00fcltigen"
                        + " Heilmittelrichtlinien!</b></html>");
                return;
            }
            if (rez.getIcd10().trim().length() > 0) {
                // fuer die Suche alles entfernen das nicht in der icd10-Tabelle aufgefuehrt sein
                // kann
                String suchenach = RezeptFensterTools.macheIcdString(rez.getIcd10());
                // TODO: put following SQL statement in some Dto-class
                if (SqlInfo.holeEinzelFeld("select id from icd10 where schluessel1 like '" + suchenach + "%' LIMIT 1")
                           .equals("")) {
                    int frage = JOptionPane.showConfirmDialog(null,
                            "<html><b>Der eingetragene 1. ICD-10-Code ist falsch: <font color='#ff0000'>"
                                    + rez.getIcd10().trim()
                                    + "</font></b><br>" + "HMR-Check nicht m\u00f6glich!<br><br>"
                                    + "Wollen Sie jetzt das ICD-10-Tool starten?<br><br></html>",
                            "falscher ICD-10", JOptionPane.YES_NO_OPTION);
                    if (frage == JOptionPane.YES_OPTION) {

                        SwingUtilities.invokeLater(new ICDrahmen(connection));
                    }
                    return;

                }
                if (rez.getIcd10_2().trim().length() > 0) {
                    suchenach = RezeptFensterTools.macheIcdString(rez.getIcd10_2());
                    if (SqlInfo.holeEinzelFeld(
                            "select id from icd10 where schluessel1 like '" + suchenach + "%' LIMIT 1")
                               .equals("")) {
                        int frage = JOptionPane.showConfirmDialog(null,
                                "<html><b>Der eingetragene 2. ICD-10-Code ist falsch: <font color='#ff0000'>"
                                        + rez.getIcd10_2().trim()
                                        + "</font></b><br>" + "HMR-Check nicht m\u00f6glich!<br><br>"
                                        + "Wollen Sie jetzt das ICD-10-Tool starten?<br><br></html>",
                                "falscher ICD-10", JOptionPane.YES_NO_OPTION);
                        if (frage == JOptionPane.YES_OPTION) {
                            SwingUtilities.invokeLater(new ICDrahmen(connection));
                        }
                        return;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "<html><b><font color='#ff0000'>Kein ICD-10 Code angegeben!</font></b></html>");
            }

            indi = indi.replace(" ", "");
            Vector<Integer> anzahlen = new Vector<Integer>();
            Vector<String> hmpositionen = new Vector<String>();

            int preisgruppe = rez.getPreisGruppe();
            
            String position = "";

            Disziplinen disziSelect = new Disziplinen();

            // TODO: Delete me when Rezepteumbau has been completed
            // get(6+2) = ArtDerBeh1 - 6+5 ArtDerBeh4
            for (int i = 2; i <= 5; i++) {
                try {
                    idtest = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(6 + i));
                } catch (Exception ex) {
                    idtest = 0;
                }
                logger.debug("Vec: idtest (ArtDerBeh" + (i - 1) + ") =" +idtest);
                idtest = rez.getArtDerBehandlung(i - 1);
                logger.debug("Rez: idtest (ArtDerBeh" + (i - 1) + ") =" + idtest);
                if (idtest > 0) {
                    try {
                        // anzahlen.add(Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(1 + i)));
                        anzahlen.add(rez.getBehAnzahl(i - 1));
                    } catch (Exception ex) {
                        // My guess is this was supposed to be done if parseInt barfed at emptyString?
                        // In this case we shouldn't need the entire try-catch block, since getBehAnz in Rezepte
                        // now returns an int - and this should be "0" if not set previously anyhow...
                        // Any other exceptions should probably not lead to continuation of the program
                        anzahlen.add(0);
                    }
                    try {
                        // TODO: caller and called should be adjusted to new Disziplin-class and possibly caller
                        //   handle int as input
                        position = RezTools.getPosFromID(Integer.toString(idtest), Integer.toString(preisgruppe),
                                SystemPreislisten.hmPreise.get(diszi)
                                                          .get(preisgruppe - 1));
                        hmpositionen.add(position);
                    } catch (Exception ex) {
                        hmpositionen.add("");
                    }

                }
            }
            logger.debug("After Vec:");
            logger.debug("Anzahlen: " + anzahlen.toString() + " hmpositionen=" + hmpositionen.toString());
            
            // Lets replace above with new Rezept-class:
            anzahlen.clear();
            hmpositionen.clear();
            int[] artDerBehandlungen = rez.getArtDerBehAlle();
            int[] behandlungenAnzahle = rez.getAnzahlAlle();
            
            for ( int i=0; i<artDerBehandlungen.length;i++) {
                idtest = artDerBehandlungen[i];
                if( idtest > 0) {
                    anzahlen.add(behandlungenAnzahle[i]);
                    int tmp = preisgruppe - 1;
                    position = RezTools.getPosFromID(Integer.toString(idtest),
                                                     Integer.toString(preisgruppe),
                                                     SystemPreislisten.hmPreise.get(diszi).get(tmp));
                    hmpositionen.add(position);
                }
            }
            logger.debug("After Rez:");
            logger.debug("Anzahlen: " + anzahlen.toString() + " hmpositionen=" + hmpositionen.toString());
            
            if (hmpositionen.size() > 0) {
                boolean checkok = new HMRCheck(indi,
                                               disziSelect.getIndex(diszi), 
                                               anzahlen, 
                                               hmpositionen, 
                                               preisgruppe - 1,
                                               SystemPreislisten.hmPreise.get(diszi).get(preisgruppe - 1),
                                               rez.getRezeptArt(),
                                               rez.getRezNr(),
                                               DatFunk.sDatInDeutsch(rez.getRezDatum().toString()),
                                               DatFunk.sDatInDeutsch(rez.getLastDate().toString()))
                                        .check();
                if (!checkok) {
                    int anfrage = JOptionPane.showConfirmDialog(null,
                            "Das Rezept entspricht nicht den geltenden Heilmittelrichtlinien\n"
                            + "Wollen Sie diesen Rezept trotzdem abschlie\u00dfen?",
                            "Achtung wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
                    if (anfrage != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "Keine Behandlungspositionen angegeben, HMR-Check nicht m\u00f6glich!!!");
                return;
            }
            /*********************/
            /********************************************************************************/
            // Icon Rezepstatus -> abgeschlossen:
            dtblm.setValueAt(Reha.instance.patpanel.imgrezstatus[1], currow,
                                                                MyAktRezeptTableModel.AKTREZTABMODELCOL_REZSTATUS);
            doAbschliessen(rez);
            // TODO: delete me once Rezepteumbau has been completed
            String xcmd = "update verordn set abschluss='T' where id='" + Reha.instance.patpanel.vecaktrez.get(35)
                    + "' LIMIT 1";
            // SqlInfo.sqlAusfuehren(xcmd);
            // TODO: remove me once Rezepte Umbau has been completed
            Reha.instance.patpanel.vecaktrez.set(62, "T");
            aktuelAngezeigtesRezept.setAbschluss(true);
            // TODO: move the following SQL-Stmt into some Dto-class
            Vector<Vector<String>> kdat = SqlInfo.holeFelder("select ik_kasse,ik_kostent from kass_adr where id='"
                    + rez.getkId() + "' LIMIT 1");
            String ikkass = "", ikkost = "", kname = "", rnr = "", patintS = "";
            int patint = -1;
            if (kdat.size() > 0) {
                ikkass = kdat.get(0)
                             .get(0);
                ikkost = kdat.get(0)
                             .get(1);
            } else {
                ikkass = "";
                ikkost = "";
            }
            // TODO: delete me lots of once Rezepteumbau has been completed
            kname = Reha.instance.patpanel.vecaktrez.get(36);
            logger.debug("Vec: kname=" +kname);
            kname = rez.getKTraegerName();
            logger.debug("Rez: kname=" + kname);
            patintS = Reha.instance.patpanel.vecaktrez.get(0);
            logger.debug("Vec: patint=" + patintS);
            patint = rez.getPatIntern();
            logger.debug("Rez: patint=" + patint);
            rnr = Reha.instance.patpanel.vecaktrez.get(1);
            // TODO: move the following SQL statement to some dto (RezepteDto?)  DONE - landed in rezeptFertigeDto
            // TODO: change rnr.substring to new Rezeptnummern-class + Disziplin
            String cmd = "insert into fertige set ikktraeger='" + ikkost + "', ikkasse='" + ikkass + "', " + "name1='"
                    + kname + "', rez_nr='" + rnr + "', pat_intern='" + patint + "', rezklasse='" + rnr.substring(0, 2)
                    + "'";
            // SqlInfo.sqlAusfuehren(cmd);
        } else {
            if (!Rechte.hatRecht(Rechte.Rezept_unlock, true)) {
                return;
            }
            // bereits abgeschlossen muss geoeffnet werden
            dtblm.setValueAt(Reha.instance.patpanel.imgrezstatus[0], currow, 
                                                                MyAktRezeptTableModel.AKTREZTABMODELCOL_REZSTATUS);
            // TODO: delete me once Rezepteumbau has been completed
            String xcmd = "update verordn set abschluss='F' where id='" + Reha.instance.patpanel.vecaktrez.get(35)
                    + "' LIMIT 1";
            // TODO: delete me once RezepteUmbau has been completed
            Reha.instance.patpanel.vecaktrez.set(62, "F");
            doAufschliessen(rez);
            // Reha.instance.patpanel.rezAktRez.setAbschluss(false);
            // rDto.rezeptAbschluss(Reha.instance.patpanel.rezAktRez.getId(), false);
            // SqlInfo.sqlAusfuehren(xcmd);
            // TODO: delete me once RezepteUmbau has been completed
            String rnr = Reha.instance.patpanel.vecaktrez.get(1);
            logger.debug("Vec: rnr=" + rnr);
            rnr = rez.getRezNr();
            logger.debug("Rez: rnr=" + rnr);
            // TODO: move the following SQL statement to some dto (RezepteDto?) DONE - landed in rezeptFertigeDto
            String cmd = "delete from fertige where rez_nr='" + rnr + "' LIMIT 1";
            // SqlInfo.sqlAusfuehren(cmd);
            JComponent abrech1 = AktiveFenster.getFensterAlle("Abrechnung-1");
            if (abrech1 != null) {
                Reha.instance.abrechnungpanel.doEinlesen(Reha.instance.abrechnungpanel.getaktuellerKassenKnoten(),
                        null);
            }
        }
    }

    /**
     * Checks the dates for too many days between Rezept-Datum & Behandlungsbeginn and/or too many days between 2 Termine
     * 
     * @return true if test passed
     */
    private boolean doTageTest(Rezept rez) {
        List<LocalDate> termine = new ArrayList<LocalDate>();
        String disziplin;
        int preisgruppe;
        List<String> kommentare = new ArrayList<String>();
        String ret;
        for (String termin : rez.getTermine().split("\n")) {
            String[] t = termin.split("@"); // 0=dtsch-dat, 1=behandler, 2=kommentar, 3=HM, 4=sql-dat
            termine.add(LocalDate.parse(t[0], DateTimeFormatters.ddMMYYYYmitPunkt));
            kommentare.add(t[2]);
        }
        
        disziplin = Disziplin.ofShort(rez.getRezClass()).medium;
        preisgruppe = rez.getPreisGruppe() - 1; // I swear, I'll kill s/o for this one of these days...
        // Frist zwischen RezDat (bzw. spaetester BehBeginn) und tatsaechlichem BehBeginn
        int fristbeginn = (Integer) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                            .get(0)).get(preisgruppe);
        // Frist zwischen den Behjandlungen
        int fristbreak = (Integer) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                           .get(2)).get(preisgruppe);
        if (fristbreak > 14) {      // Magic number - Astrid, this will need checking on new HMR
            if (!disziplin.equals("Podo")) {
                fristbreak = 14;    // Magic number - Astrid, this will need checking on new HMR
            }
        }
        // Beginn-Berechnung nach Kalendertagen
        boolean ktagebeginn = (Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                                .get(1)).get(preisgruppe);
        // Unterbrechung-Berechnung nach Kalendertagen
        boolean ktagebreak = (Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                               .get(3)).get(preisgruppe);
        // Beginnfrist: Samstag als Werktag werten (wirk nur bei Werktagregel)
        boolean beginnsamstag = (Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                                  .get(4)).get(preisgruppe);
        // Unterbrechungsfrist: Samstag als Werktag werten (wirk nur bei Werktagregel)
        boolean breaksamstag = (Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                                 .get(5)).get(preisgruppe);
        long utage = 0;
        logger.debug("Frist zum BehBeg: " + fristbeginn);
        // This wasn't part of original code:
        // The lazy way (assumes correctly set LastDate):
        if ( termine.get(0).isAfter(rez.getLastDate()) ) {
            
            int frage = JOptionPane.showConfirmDialog(null,
                    "<html>Der erste Termin ist nach sp&auml;testem Behandlungsdatum."
                    + "<BR/>M&ouml;chten Sie das Rezept trotzdem abschliessen?</html>",
                    "Wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
            if (frage == JOptionPane.NO_OPTION) {
                return false;
            }
        }
        // The complicated way (works only on Rezept-Datum & 1. Termin + Frist from Systempreislisten.hmFristen):
        // It's possible that lastDate is not (re-)set when a Rezept is edited (RezDatum changed)?
        // But, to spare user of pot. 2x same question, we'll else'it here:
        else if ( (utage =  ChronoUnit.DAYS.between( rez.getRezDatum(), termine.get(0) ) ) > fristbeginn ) {
            
            int frage = JOptionPane.showConfirmDialog(null,
                    "<html>Der erste Termin ist<BR/>" + utage + "<BR/>nach Rezeptdatum."
                    + "<BR/>M&ouml;chten Sie das Rezept trotzdem abschliessen?</html>",
                    "Wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
            if (frage == JOptionPane.NO_OPTION) {
                return false;
            }
        }
        for(int i=1; i<termine.size(); i++) {
            if(termine.get(i-1).equals(termine.get(i))) {
                JOptionPane.showMessageDialog(null,
                        "Zwei identische Behandlungstage sind nicht zul\u00e4ssig - Abschlu\u00df des Rezeptes fehlgeschlagen");
                return false;
            }
            if( termine.get(i).isBefore(termine.get(i - 1))) {
                JOptionPane.showMessageDialog(null,
                        "Bitte sortieren Sie zuerst die Behandlungstage - Abschlu\u00df des Rezeptes fehlgeschlagen");
                return false;
            }
            
            // PRE: Der Unterbrechungsgrund ist in dem "zu spaetem" Termin, 
            if (ktagebreak) {
                if (!"RSFT".contains(disziplin)) {
                    if ( ( (utage = ChronoUnit.DAYS.between(termine.get(i-1), termine.get(i) ) ) > fristbreak)
                                    && (kommentare.get(i).trim().isEmpty())) {
                        ret = rezUnterbrechung(true, "", i + 1, Long.toString(utage));// Unterbrechungsgrund
                        if (ret.isEmpty()) {
                            return false;
                        } else {
                            dtermm.setValueAt(ret, i, 2);
                        }
                    }
                }
            } else {
                if (!"RSFT".contains(disziplin)) {
                    if ((utage = HMRCheck.hmrTageDifferenz(
                                            termine.get(i-1).format(DateTimeFormatters.ddMMYYYYmitPunkt),
                                            termine.get(i).format(DateTimeFormatters.ddMMYYYYmitPunkt),
                                            fristbreak,
                                            breaksamstag)) > 0
                            && kommentare.get(i).trim().isEmpty()) {
                        ret = rezUnterbrechung(true, "", i + 1, Long.toString(utage));// Unterbrechungsgrund-Eingabe
                        if (ret.isEmpty()) {
                            return false;
                        } else {
                            // FIXME: this needs to properly add the comment to the rez, and then re-read it...
                            RezeptFensterTools.updateKommentarInTermin(rez, i, ret, mand.ik());
                            // TODO: reread/-paint dterm
                            // Original code as Q&D for above todo - caution! a table-change-listener may re-write to Rez&DB
                            // dtermm.setValueAt(ret, i, 2);
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Adds/changes a comment of a Termin in the Termine-String of a Rezept.
     * <BR/>Alters the passed in Rezept and also updates the database with new Termine
     * <BR/>
     * <BR/>TODO: think about transferring this to Rezept/-Dto...
     *  
     * @param rez           Rezept to be altered
     * @param welcherTermin Which of the termine should be altered. 0=first Termin
     * @param kommentar     The kommentar (Unterbrech-Begr.?) to be set
     **/
    private void updateKommentarInTermin(Rezept rez, int welcherTermin, String kommentar) {
        String[] termine = rez.getTermine().split("\n");
        String[] eintrag = termine[welcherTermin].split("@");
        eintrag[2] = kommentar;
        String neuerEintrag = eintrag[0];
        for (int i=1; i<eintrag.length; i++)
            neuerEintrag.concat("@" + eintrag[i]);
        termine[welcherTermin] = neuerEintrag;
        String neueTermine = termine[0];
        for (int i=1; i<termine.length; i++)
            neueTermine.concat("\n" + termine[i]);
        rDto.updateRezeptTermine(rez.getId(), neueTermine);
        rez.setTermine(neueTermine);
    }
    
    /**
     * Checks the dates for too many days between Rezept-Datum & Behandlungsbeginn and/or too many days between 2 Termine
     *  Huh?? Where's the test on "Rez-Dat vs Behandlungsbeginn" gone?
     * @param latestdat
     * @param starttag
     * @param tageanzahl
     * @param disziplin
     * @param preisgruppe
     * @return
     **/
    // TODO: To properly use the new rezept-class the strings *tag should be changed to LocalDate & disziplin to class disziplin
    // TODO: Move this entire check to some other class like AktuelleRezepteChecks.java or whatnot
    private boolean doTageTest(String latestdat, String starttag, int tageanzahl, String disziplin, int preisgruppe) {
        String vglalt;
        String vglneu;
        String kommentar;
        String ret;
        // Frist zwischen RezDat (bzw. spaetester BehBeginn) und tatsaechlichem BehBeginn
        int fristbeginn = (Integer) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                            .get(0)).get(preisgruppe);
        // Frist zwischen den Behjandlungen
        int fristbreak = (Integer) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                           .get(2)).get(preisgruppe);

        if (fristbreak > 14) {      // Magic number - Astrid, this will need checking on new HMR
            if (!disziplin.equals("Podo")) {
                fristbreak = 14;    // Magic number - Astrid, this will need checking on new HMR
            }
        }
        // Beginn-Berechnung nach Kalendertagen
        boolean ktagebeginn = (Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                                .get(1)).get(preisgruppe);
        // Unterbrechung-Berechnung nach Kalendertagen
        boolean ktagebreak = (Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                               .get(3)).get(preisgruppe);
        // Beginnfrist: Samstag als Werktag werten (wirk nur bei Werktagregel)
        boolean beginnsamstag = (Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                                  .get(4)).get(preisgruppe);
        // Unterbrechungsfrist: Samstag als Werktag werten (wirk nur bei Werktagregel)
        boolean breaksamstag = (Boolean) ((Vector<?>) SystemPreislisten.hmFristen.get(disziplin)
                                                                                 .get(5)).get(preisgruppe);
        for (int i = 0; i < tageanzahl; i++) {
            if (i > 0) {
                vglalt = (String) dtermm.getValueAt(i - 1, 0);
                vglneu = (String) dtermm.getValueAt(i, 0);
                if (vglalt.equals(vglneu)) {
                    JOptionPane.showMessageDialog(null,
                            "Zwei identische Behandlungstage sind nicht zul\u00e4ssig - Abschlu\u00df des Rezeptes fehlgeschlagen");
                    return false;
                }
                if (DatFunk.TageDifferenz(vglalt, vglneu) < 0) {
                    JOptionPane.showMessageDialog(null,
                            "Bitte sortieren Sie zuerst die Behandlungstage - Abschlu\u00df des Rezeptes fehlgeschlagen");
                    return false;
                }

                kommentar = (String) dtermm.getValueAt(i, 2);
                long utage = 0;
                // Wenn nach Kalendertagen ermittelt werden soll
                if (ktagebreak) {
                    if (!"RSFT".contains(aktuelAngezeigtesRezept.getRezNr()
                                                                         .substring(0, 2))) {
                        if (((utage = DatFunk.TageDifferenz(vglalt, vglneu)) > fristbreak) && (kommentar.trim()
                                                                                                        .equals(""))) {
                            ret = rezUnterbrechung(true, "", i + 1, Long.toString(utage));// Unterbrechungsgrund
                            if (ret.equals("")) {
                                return false;
                            } else {
                                dtermm.setValueAt(ret, i, 2);
                            }
                        }
                    }
                } else {
                    if (!"RSFT".contains(aktuelAngezeigtesRezept.getRezNr()
                                                                         .substring(0, 2))) {
                        if ((utage = HMRCheck.hmrTageDifferenz(vglalt, vglneu, fristbreak, breaksamstag)) > 0
                                && kommentar.trim()
                                            .equals("")) {
                            ret = rezUnterbrechung(true, "", i + 1, Long.toString(utage));// Unterbrechungsgrund
                            if (ret.equals("")) {
                                return false;
                            } else {
                                dtermm.setValueAt(ret, i, 2);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean pruefePatDatenVorRezAbschluss(Rezept rez) {
        if (Reha.instance.patpanel.patDaten.get(23).trim().length() != 5) {
            JOptionPane.showMessageDialog(null, "Die im Patientenstamm zugewiesene Postleitzahl ist fehlerhaft");
            return false;
        }
        if (Reha.instance.patpanel.patDaten.get(14).trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                                          "Die im Patientenstamm zugewiesene Krankenkasse hat keine Kassennummer");
            return false;
        }
        if (Reha.instance.patpanel.patDaten.get(15).trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Der Mitgliedsstatus fehlt im Patientenstamm, bitte eintragen");
            return false;
        }
        if ("135".indexOf(Reha.instance.patpanel.patDaten.get(15)
                                                            .substring(0, 1)) < 0) {
        JOptionPane.showMessageDialog(null, "Der im Patientenstamm vermerkte Mitgliedsstatus ist ung\u00fcltig\n\n"
                                            + "Fehlerhafter Status = " + Reha.instance.patpanel.patDaten.get(15) + "\n");
            return false;
        }
        if (Reha.instance.patpanel.patDaten.get(16).trim().isEmpty()) {
        JOptionPane.showMessageDialog(null,
                                      "Die Krankenkassen-Mitgliedsnummer fehlt im Patientenstamm, bitte eintragen");
            return false;
        }
        if (!Reha.instance.patpanel.patDaten.get(68).trim()
                                                    .equals(Integer.toString(rez.getkId()))) {
            JOptionPane.showMessageDialog(null,
                                          "ID der Krankenkasse im Patientenstamm pa\u00dft nicht zu der ID der Krankenkasse im Rezept");
            return false;
        }
        return true;
    }
    
    private boolean rezGeschlossenMitWarnung() {
        if (aktuelAngezeigtesRezept.isAbschluss()) {
            JOptionPane.showMessageDialog(null,
                    "Das Rezept ist bereits abgeschlossen\n\u00c4nderungen sind nur noch durch berechtigte Personen m\u00f6glich");
            return true;
        } else {
            return false;
        }
    }

    private boolean rezBefreitMitWarnung() {
        if (aktuelAngezeigtesRezept.isBefr()) {
            JOptionPane.showMessageDialog(null, "Das Rezept ist zuzahlungsbefreit!");
            return true;
        } else {
            return false;
        }
    }

    private void privatRechnung() {
        try {
            // Preisgruppe ermitteln
            int preisgruppe = 0;
            // TODO: Original code used field as string - check constructKasseHMap if it could deal with an int
            KasseTools.constructKasseHMap(Integer.toString(aktuelAngezeigtesRezept.getkId()));
            // TODO: the original code retrieved a string & parsed it - should that have gone bust
            //   (due to pg==null?) an error was displayed - what is an error now? pg=0?
            preisgruppe = aktuelAngezeigtesRezept.getPreisGruppe();
            
            Point pt = btnTools.getLocationOnScreen();
            pt.x = pt.x - 75;
            pt.y = pt.y + 30;
            AbrechnungPrivat abrechnungPrivat = new AbrechnungPrivat(Reha.getThisFrame(),
                    "Privat-/BG-/Nachsorge-Rechnung erstellen", -1, preisgruppe);
            abrechnungPrivat.setLocation(pt);
            abrechnungPrivat.pack();
            abrechnungPrivat.setModal(true);
            abrechnungPrivat.setVisible(true);
            int rueckgabe = abrechnungPrivat.rueckgabe;
            abrechnungPrivat = null;
            if (rueckgabe == -2) {
                neuanlageRezept(false, "", REZEPTKOPIERE_NIX);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Funktion privatRechnung(), Exception = " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    private void doAbschliessen(Rezept rez) {
        // RezeptFertigeDto rfDto = new RezeptFertigeDto(mandant.ik());
        RezeptFertige rf = new RezeptFertige(rez, mand.ik());
        
        if (!rf.RezeptErledigt()) {
            logger.error("Konnte Rezept nicht abschliesen");
            JOptionPane.showMessageDialog(null, "Es ist ein Fehler aufgetreten\n"
                    + "Konnte Rezept\n" + rez.getRezNr().toString() + "\n"
                    + "nicht abschliessen\n");
        };

    }

    private void doAufschliessen(Rezept rez) {
        RezeptFertige rf = new RezeptFertige(rez, mand.ik());
        
        if (!rf.RezeptRevive()) {
            logger.error("Konnte Rezept nicht aufschliesen");
            JOptionPane.showMessageDialog(null, "Es ist ein Fehler aufgetreten\n"
                    + "Konnte Rezept\n" + rez.getRezNr().toString() + "\n"
                    + "nicht aufschliessen\n");
        };

    }

    /*****************************************************/
    class MyTermClass {
        String ddatum;
        String behandler;
        String stext;
        String sart;
        String qdatum;

        public MyTermClass(String s1, String s2, String s3, String s4, String s5) {
            ddatum = s1;
            behandler = s2;
            stext = s3;
            sart = s4;
            qdatum = (s5 == null ? " " : s5);
        }

        public String getDDatum() {
            return ddatum;
        }

        public String getBehandler() {
            return behandler;
        }

        public String getStext() {
            return stext;
        }

        public String getSArt() {
            return sart;
        }

        public String getQDatum() {
            return qdatum;
        }
    }

    class MyTermTableModel extends DefaultTableModel {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return String.class;
            }
            else {
                return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            // Note that the data/cell address is constant,
            // no matter where the cell appears onscreen.
            if (aktuelAngezeigtesRezept.isAbschluss()) {
                return false;
            }
            if (col == 0) {
                return true;
            } else if (col == 1) {
                return true;
            } else if (col == 2) {
                return true;
            } else if (col == 3) {
                return true;
            } else if (col == 11) {
                return true;
            } else {
                return false;
            }
        }
    }

    class MyAktRezeptTableModel extends DefaultTableModel {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        
        public static final int AKTREZTABMODELCOL_REZNr = 0;
        public static final int AKTREZTABMODELCOL_BEZICON = 1; // zzstatus
        public static final int AKTREZTABMODELCOL_REZDATUM = 2;
        public static final int AKTREZTABMODELCOL_ANGELEGTDATUM = 3;
        public static final int AKTREZTABMODELCOL_SPAETBEHBEG = 4;
        public static final int AKTREZTABMODELCOL_REZSTATUS = 5; // abschluss
        public static final int AKTREZTABMODELCOL_PATINTERN = 6;
        public static final int AKTREZTABMODELCOL_INDIKATSCHL = 7;
        public static final int AKTREZTABMODELCOL_ID = 8;
        public static final int AKTREZTABMODELCOL_TERMINE = 9;
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 1 || columnIndex == 5) {
                return JLabel.class;
            } else {
                return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            // Note that the data/cell address is constant,
            // no matter where the cell appears onscreen.

            if (col == 0) {
                return true;
            } else if (col == 3) {
                return true;
            } else if (col == 7) {
                return true;
            } else if (col == 11) {
                return true;
            } else {
                return false;
            }
        }

    }

    public void doRezeptGebuehr(Point pt) { // Lemmi Doku: Bares Kassieren der Rezeptgebuehr
        boolean bereitsbezahlt = false;

        // vvv Lemmi 20101218: Pruefung, ob es eine RGR-RECHNUNG bereits gibt, falls ja,
        // geht hier gar nix !
        String reznr = aktuelAngezeigtesRezept.getRezNr();

        if (ZuzahlTools.existsRGR(reznr)) {
            JOptionPane.showMessageDialog(null,
                    "<html>" + ZuzahlTools.rgrOK(reznr) + "<br>"
                            + "Eine Barzahlungs-Quittung kann nicht mehr erstellt werden.</html>",
                    "Bar-Quittung nicht mehr m\u00f6glich", JOptionPane.WARNING_MESSAGE, null);
            return;
        }

        // erst pruefen ob Zuzahlstatus = 0 (befreit) o. u18, wenn ja zurueck;
        // dann pruefen ob bereits bezahlt wenn ja fragen ob Kopie erstellt werden soll;
        if (aktuelAngezeigtesRezept.getZZStatus() == Zuzahlung.ZZSTATUS_BEFREIT) {
            JOptionPane.showMessageDialog(null, "Zuzahlung nicht erforderlich!");
            return;
        }
        if (DatFunk.Unter18(DatFunk.sHeute(), DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)))) {
            JOptionPane.showMessageDialog(null,
                    "Stand heute ist der Patient noch nicht Vollj\u00e4hrig - "
                    + "Zuzahlung deshalb (bislang) noch nicht erforderlich");
            return;
        }

        if (ZuzahlTools.bereitsBezahlt(reznr)) {
            int frage = JOptionPane.showConfirmDialog(null,
                    "<html>Zuzahlung f\u00fcr Rezept <b>" + reznr
                            + "</b> bereits in bar geleistet!<br><br> Wollen Sie eine Kopie erstellen?</html>",
                    "Wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
            if (frage == JOptionPane.NO_OPTION) {
                return;
            }
            bereitsbezahlt = true;
        }
        resetHmAdrRData();
        RezTools.testeRezGebArt(false, false, aktuelAngezeigtesRezept.getRezNr(),
                aktuelAngezeigtesRezept.getTermine());
        new RezeptGebuehren(this, bereitsbezahlt, false, pt);
    }

    private void resetHmAdrRData() {
        SystemConfig.hmAdrRDaten.put("<Rhbpos>", "----");
        SystemConfig.hmAdrRDaten.put("<Rhbpreis>", "0,00");
        SystemConfig.hmAdrRDaten.put("<Rhbproz>", "0,00");
        SystemConfig.hmAdrRDaten.put("<Rhbgesamt>", "0,00");
        SystemConfig.hmAdrRDaten.put("<Rwegpos>", "----");
        SystemConfig.hmAdrRDaten.put("<Rwegpreis>", "0,00");
        SystemConfig.hmAdrRDaten.put("<Rwegproz>", "0,00");
        SystemConfig.hmAdrRDaten.put("<Rweggesamt>", "0,00");
        SystemConfig.hmAdrRDaten.put("<Rendbetrag>", "0,00");
        SystemConfig.hmAdrRDaten.put("<Rwert>", "0,00");
    }

    // TODO: Adjust to new Rezeptnummern-class
    public static void setZuzahlImageActRow(ZZStat key, String reznr) {
        try {
            if (tabaktrez == null) {
                return;
            }
            int row = tabaktrez.getSelectedRow();
            if (row >= 0) {
                if (dtblm.getValueAt(row, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr)
                         .toString()
                         .equals(reznr)) {
                    dtblm.setValueAt(ZuzahlTools.getZzIcon(key), row, MyAktRezeptTableModel.AKTREZTABMODELCOL_BEZICON);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Achtung kann Icon f\u00fcr korrekte Zuzahlung nicht setzen.\n"
                            + "Bitte notieren Sie den Namen des Patienten und die Rezeptnummer und verst\u00e4ndigen\n"
                            + "Sie den Administrator");
        }
    }

    public static void setZuzahlImage(int imageno) {
        String rezNr = tabaktrez.getValueAt(tabaktrez.getSelectedRow(), MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr)
                                .toString();
        ZZStat iconKey = ZuzahlTools.getIconKey(imageno, rezNr);
        setZuzahlImageActRow(iconKey, rezNr);
    }

    private void doBarcode() {
        resetHmAdrRData();
        RezTools.testeRezGebArt(true, false, aktuelAngezeigtesRezept.getRezNr(),
                aktuelAngezeigtesRezept.getTermine());
        SystemConfig.hmAdrRDaten.put("<Bcik>", Reha.getAktIK());
        // ?? The following converted a string to a string...
        String bcreznr = aktuelAngezeigtesRezept.getRezNr();
        if (bcreznr.startsWith("RS") || bcreznr.startsWith("FT")) {
            if (bcreznr.length() < 6) {
                bcreznr = StringTools.fuelleMitZeichen(bcreznr, "_", false, 6);
            }
        }
        SystemConfig.hmAdrRDaten.put("<Bcode>", "*" + bcreznr + "*");
        int iurl = aktuelAngezeigtesRezept.getBarcodeform();
        String url = SystemConfig.rezBarCodForm.get((iurl < 0 ? 0 : iurl));
        SystemConfig.hmAdrRDaten.put("<Bzu>",
                StringTools.fuelleMitZeichen(SystemConfig.hmAdrRDaten.get("<Rendbetrag>"), " ", true, 5));
        SystemConfig.hmAdrRDaten.put("<Bges>",
                StringTools.fuelleMitZeichen(SystemConfig.hmAdrRDaten.get("<Rwert>"), " ", true, 6));
        SystemConfig.hmAdrRDaten.put("<Bnr>", SystemConfig.hmAdrRDaten.get("<Rnummer>"));
        SystemConfig.hmAdrRDaten.put("<Buser>", Reha.aktUser);
        // TODO: could hmAdrDaten take an int as 2nd param?
        SystemConfig.hmAdrRDaten.put("<Rpatid>", Integer.toString(aktuelAngezeigtesRezept.getPatIntern()));
        OOTools.starteBacrodeFormular(Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK() + "/" + url,
                SystemConfig.rezBarcodeDrucker);

    }

    public String rezUnterbrechung(boolean lneu, String feldname, int behandlung, String utage) {
        if (neuDlgOffen) {
            return "";
        }
        try {
            neuDlgOffen = true;
            String ret;
            RezTest rezTest = new RezTest();
            PinPanel pinPanel = new PinPanel();
            pinPanel.setName("RezeptTest");
            pinPanel.getGruen()
                    .setVisible(false);
            rezTest.setSize(300, 200);
            rezTest.setPreferredSize(new Dimension(300, 200));
            rezTest.getSmartTitledPanel()
                   .setPreferredSize(new Dimension(250, 200));
            rezTest.setPinPanel(pinPanel);
            RezTestPanel testPan = new RezTestPanel((dummyLabel = new JLabel()));
            rezTest.getSmartTitledPanel()
                   .setContentContainer(testPan);
            rezTest.getSmartTitledPanel()
                   .setTitle("Unterbr. bei der " + behandlung + ". Behandlung - " + utage + " Tage");
            rezTest.setName("RezeptTest");
            rezTest.setModal(true);
            Point pt = tabaktterm.getLocationOnScreen();
            pt.x = pt.x - 300;
            pt.y = pt.y - 15;
            rezTest.setLocation(pt);
            rezTest.pack();
            rezTest.setVisible(true);
            rezTest.dispose();
            ret = String.valueOf(dummyLabel.getText());
            testPan.dummylab = null;
            testPan = null;
            rezTest = null;
            neuDlgOffen = false;
            return ret;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    // Lemmi 20110101: bCtrlPressed zugefuegt. Kopieren des letzten Rezepts des
    // selben Patienten bei Rezept-Neuanlage
    public void neuanlageRezept(boolean lneu, String feldname, int kopierModus) {
        try {
            if (Reha.instance.patpanel.aid < 0 || Reha.instance.patpanel.kid < 0) {
                String meldung = "Hausarzt und/oder Krankenkasse im Patientenstamm sind nicht verwertbar.\n"
                        + "Die jeweils ung\u00fcltigen Angaben sind -> kursiv <- dargestellt.\n\n"
                        + "Bitte korrigieren Sie die entsprechenden Angaben";
                JOptionPane.showMessageDialog(null, meldung);
                return;
            }
            if (neuDlgOffen) {
                JOptionPane.showMessageDialog(null, "neuDlgOffen hat den wert true");
                return;
            }
            try {
                neuDlgOffen = true;
                RezNeuDlg neuRez = new RezNeuDlg();
                PinPanel pinPanel = new PinPanel();
                pinPanel.setName("RezeptEditor");
                pinPanel.getGruen()
                        .setVisible(false);
                if (lneu) {
                    neuRez.getSmartTitledPanel()
                          .setTitle("Rezept Neuanlage");
                }
                neuRez.setSize(500, 800);
                neuRez.setPreferredSize(new Dimension(490 + Reha.zugabex, 690 + Reha.zugabey));
                neuRez.getSmartTitledPanel()
                      .setPreferredSize(new Dimension(490, 800));
                neuRez.setPinPanel(pinPanel);
                   
                if (lneu) {
                    Rezept rezKopierVorlage;
                    if ( kopierModus == REZEPTKOPIERE_NIX) {
                        // echte NeuAnlage
                        rezKopierVorlage = new Rezept();
                    } else {
                        // TODO: This whole block assumes "Rezept" is a vector - checks for size and alike
                        //   need to be sorted to new Rezept-class
                        // Vector<String> vecKopierVorlage = vecNeuesRezeptVonKopie(kopierModus);
                        rezKopierVorlage = rezNeuesRezeptVonKopie(kopierModus);
                        if (rezKopierVorlage.getPatIntern() == 0) { /**
                                                                    * TODO: getRezNr() has trouble with empty Rezept...
                                                                    * TODO: ganzes Rezept kann nicht NULL sein, aber Bausteine darin
                                                                    * z.Bsp. RezNr...
                                                                    *  If user pressed "Abbrechen" in Pre-Diszi-Selection dialog,
                                                                    *  we have no Rezept-Data
                                                                    *   - do we want to proceed with "Rezept Anlegen", 
                                                                    *     or cancel the whole operation?
                                                                    *  I.e. why would user press 'cancel' in Diszi-selection?
                                                                    *     Wrong patient? Then get out all the way...
                                                                    *  Get out completely seems the most sensible, since
                                                                    *    - if we had a choice on Diszis, and cancel the choice,
                                                                    *      the following Rezept-Neuanlage will expect vals
                                                                    *       in e.g. Diszi, RezNr we can't deliver - better let
                                                                    *       go via "proper" new Rezept path...
                                                                    */
                            logger.error("Null-Rezept abgefangen. Bitte neu...");
                            neuDlgOffen = false;
                            return;
                        }
                    }
                    String tmpRzNr = rezKopierVorlage.getRezNr();  // I think the RezEditor messes up the "Vorlage" upon init
                                                                   // We'll stash this away to be able to set a nice title
                    // This used to pass a copy to RezNeuanlageGUI - lets see if we still need to do that...
                    RezeptEditorGUI rezNeuAn = new RezeptEditorGUI(rezKopierVorlage, lneu, mand);
                    neuRez.getSmartTitledPanel()
                          .setContentContainer(rezNeuAn);
                    // TODO: If above leads to exit, we won't need NULL check here anymore...
                    if (rezKopierVorlage.getPatIntern() == 0) // A Q&D until we get RezNr == NULL sorted
                        neuRez.getSmartTitledPanel()
                              .setTitle("Rezept Neuanlage");
                    else // Lemmi 20110101: Kopieren des letzten Rezepts des selben Patienten bei
                         // Rezept-Neuanlage
                        neuRez.getSmartTitledPanel()
                              .setTitle("Rezept Neuanlage als Kopie von --> " + tmpRzNr + " <--");


                } else { // Lemmi Doku: Hier wird ein existierendes Rezept mittels Doppelklick geoeffnet:
                    neuRez.getSmartTitledPanel()
                          .setContentContainer(
                                  new RezeptEditorGUI(aktuelAngezeigtesRezept, lneu, mand));
                    
                    neuRez.getSmartTitledPanel()
                          .setTitle("editieren Rezept ---> " + aktuelAngezeigtesRezept.getRezNr());
                }
                neuRez.getSmartTitledPanel()
                      .getContentContainer()
                      .setName("RezeptEditor");
                neuRez.setName("RezeptEditor");
                neuRez.setModal(true);
                neuRez.setLocationRelativeTo(null);
                neuRez.pack();
                neuRez.setVisible(true);

                neuRez.dispose();
                neuRez = null;
                pinPanel = null;
                if (!lneu) {
                    if (tabaktrez.getRowCount() > 0) {
                        try {
                            RezeptDaten.feddisch = false;
                            // TODO: Remove once Rezepte has been sorted
                            aktualisiereVector(String.valueOf(tabaktrez.getValueAt(tabaktrez.getSelectedRow()
                                                                                                 , MyAktRezeptTableModel.AKTREZTABMODELCOL_ID)));
                            // TODO: find a nicer way to get the tab-content as int (can we define column as int?)
                            aktualisiereRezAktRez(Integer.parseInt(String.valueOf(
                                                tabaktrez.getValueAt(tabaktrez.getSelectedRow(), MyAktRezeptTableModel.AKTREZTABMODELCOL_ID))));

                            // falls typ des zzstatus (@idx 39) im vecaktrez auf typ ZZStat umgestellt wird,
                            // oder get-Methoden erstellt werden,
                            // sind die beiden Hilfsvariablen obsolet:
                            // int iZzStat = Reha.instance.patpanel.rezAktRez.getZZStatus();
                            String sRezNr = aktuelAngezeigtesRezept.getRezNr();

                            ZZStat iconKey = ZuzahlTools.getIconKey(aktuelAngezeigtesRezept.getZZStatus(),
                                                                                                            sRezNr);
                            setZuzahlImageActRow(iconKey, sRezNr);

                            // IndiSchluessel
                            dtblm.setValueAt(aktuelAngezeigtesRezept.getIndikatSchl(),
                                                                    tabaktrez.getSelectedRow(),
                                                                MyAktRezeptTableModel.AKTREZTABMODELCOL_INDIKATSCHL);
                            tabaktrez.validate();
                            tabaktrez.repaint();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null,
                                    "Fehler in der Darstellung eines abgespeicherten Rezeptes");
                            logger.error("In neuanlageRezept - Rez-akt. failed: " + ex.getLocalizedMessage());
                            logger.error(ex.getStackTrace().toString());
                        }
                    }
                } else {
                    if (aktPanel.equals("leerPanel")) {
                        try {
                            holeRezepte(Reha.instance.patpanel.patDaten.get(29), "");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Fehler in holeRezepte\n" + ex.getMessage());
                        }
                    } else {
                        try {
                            holeRezepte(Reha.instance.patpanel.patDaten.get(29), this.sRezNumNeu);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Fehler in holeRezepte\n" + ex.getMessage());
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Fehler beim \u00d6ffnen des Rezeptfensters");
            }
            neuDlgOffen = false;

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Fehler bei der Rezeptneuanlage\n" + ex.getMessage()
                                                                                       .toString());
        }

    }

    /**
     * Erstelle ein neues Rezept aufgrund einer Kopie (e.Rez.) aus entweder<BR/>
     *   - letztem Rezept<BR/>
     *   - ausgewaehltem Rezept<BR/>
     *   - der Historie<BR/>
     *   
     * @param kopierModus
     */
    private Rezept rezNeuesRezeptVonKopie(int kopierModus) {

        Rezept vorlage = new Rezept();
        String rezNrToCopy = "";
        
        switch (kopierModus) {
            case REZEPTKOPIERE_LETZTES:
                RezNeuanlagePreDisziCheck preAnlangeDisziWahl = new RezNeuanlagePreDisziCheck(
                        btnNeu.getLocationOnScreen(),
                        aktuelAngezeigtesRezept.getPatIntern(),
                        mand);
                // Sollte der Patient nur 1 Diszi bei uns in Anspruch genommen haben, so
                //  kriegen wir hier die zu kopierende RezNr:
                rezNrToCopy = preAnlangeDisziWahl.findeDieRezNrZumKopieren();
                // Ansonsten muss der Nutzer waehlen von welcher Diszi er denn das letzte Rezept
                //   kopieren moechte:
                if ( rezNrToCopy.isEmpty() && !preAnlangeDisziWahl.hasSelected()) {
                    preAnlangeDisziWahl.setModal(true);
                    preAnlangeDisziWahl.toFront();
                    preAnlangeDisziWahl.setVisible(true);
                    rezNrToCopy = preAnlangeDisziWahl.getRezNr();
                }
                logger.debug("RezNr.: " + rezNrToCopy);
                if (preAnlangeDisziWahl.isDisplayable() )
                                preAnlangeDisziWahl.dispose();
                vorlage = rDto.byRezeptNr(rezNrToCopy).orElse(new Rezept());  
                logger.debug("Vorlage is now: " + vorlage.toString());
                break;
            case REZEPTKOPIERE_GEWAEHLTES:           // Vorschlag von J. Steinhilber integriert: Kopiere
                                                     // das angewaehlte Rezept
                rezNrToCopy = AktuelleRezepte.getActiveRezNr();
                vorlage = rDto.byRezeptNr(rezNrToCopy).orElse(new Rezept());
                break;
                
            case REZEPTKOPIERE_HISTORIENREZEPT:
                
                rezNrToCopy = null;
                if ((rezNrToCopy = Historie.getActiveRezNr()) != null) {
                    vorlage = rDto.getHistorischesRezeptByRezNr(rezNrToCopy).orElse(new Rezept());
    
                } else {
                    JOptionPane.showMessageDialog(null, "Kein Rezept in der Historie ausgew\u00e4hlt");
                }
                break;

            default:
                logger.error("Rez - Kopiermodus " + kopierModus + " nicht implementiert.");
        }
        
        return vorlage;
    }
    
    public Vector<String> getModelTermine() {
        return (Vector<String>) dtermm.getDataVector()
                                      .clone();
    }

    private void doUebertrag() {
        int row = tabaktrez.getSelectedRow();
        if (row >= 0) {
            try {
                int mod = tabaktrez.convertRowIndexToModel(row);
                String rez_nr = dtblm.getValueAt(mod, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr)
                                     .toString()
                                     .trim();
                SqlInfo.transferRowToAnotherDB("verordn", "lza", "rez_nr", rez_nr, true,
                        Arrays.asList(new String[] { "id" }));
                SqlInfo.sqlAusfuehren("delete from verordn where rez_nr='" + rez_nr + "'");
                Reha.instance.patpanel.aktRezept.holeRezepte(Reha.instance.patpanel.patDaten.get(29), "");
                final String xrez_nr = String.valueOf(rez_nr);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Reha.instance.patpanel.historie.holeRezepte(Reha.instance.patpanel.patDaten.get(29), "");
                        SqlInfo.sqlAusfuehren("delete from fertige where rez_nr='" + xrez_nr + "'");
                        RezTools.loescheRezAusVolleTabelle(xrez_nr);
                        if (Reha.instance.abrechnungpanel != null) {
                            String aktDisziplin = Reha.instance.abrechnungpanel.disziSelect.getCurrDisziKurz();
                            if (RezTools.getDisziplinFromRezNr(xrez_nr)
                                        .equals(aktDisziplin)) {
                                Reha.instance.abrechnungpanel.einlesenErneuern(null);
                            }
                        }

                    }
                });
                setzeKarteiLasche();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showConfirmDialog(null, "Fehler in der Funktion AktuelleRezepte -> doUebertrag()");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Kein aktuelles Rezept f\u00fcr den \u00dcbertrag in die Historie ausgew\u00e4hlt!");
        }

    }

    private void fuelleTage() {
        int akt = tabaktrez.getSelectedRow();
        if (akt < 0) {
            return;
        }
        String stage = "";
        int tage = this.dtermm.getRowCount();

        for (int i = 0; i < tage; i++) {
            stage = stage + (i > 0 ? ", " : "") + dtermm.getValueAt(i, 0)
                                                        .toString();
        }
        SystemConfig.hmAdrRDaten.put("<Rtage>", String.valueOf(stage));
    }

    private void doTageDrucken() {
        int akt = tabaktrez.getSelectedRow();
        if (akt < 0) {
            JOptionPane.showMessageDialog(null, "Kein aktuelles Rezept f\u00fcr \u00dcbertrag in Clipboard ausgew\u00e4hlt");
            return;
        }
        String stage = "Rezeptnummer: " + tabaktrez.getValueAt(akt, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZNr)
                                                   .toString()
                + " - Rezeptdatum: " + tabaktrez.getValueAt(akt, MyAktRezeptTableModel.AKTREZTABMODELCOL_REZDATUM)
                                                .toString()
                + "\n";
        int tage = this.dtermm.getRowCount();

        for (int i = 0; i < tage; i++) {
            stage = stage + Integer.toString(i + 1) + "\t" + dtermm.getValueAt(i, 0)
                                                                   .toString()
                    + "\n";
        }
        copyToClipboard(stage);
    }

    public static void copyToClipboard(String s) {
        Toolkit.getDefaultToolkit()
               .getSystemClipboard()
               .setContents(new StringSelection(s), null);
    }

    private void do301FallSteuerung() {
        if (!Rechte.hatRecht(Rechte.Sonstiges_Reha301, true)) {
            return;
        }
        int row = tabaktrez.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Kein Rezept f\u00fcr Fallsteuerung ausgew\u00e4hlt");
            return;
        }

        Reha.instance.progLoader.Dta301Fenster(1, aktuelAngezeigtesRezept.getRezNr());
        // Hier der Aufruf der Fallsteuerungs .JAR
    }

    // Lemmi 20101218: kopiert aus AbrechnungRezept.java und die
    // Datenherkunfts-Variablen veraendert bzw. angepasst.
    private void doRezeptgebuehrRechnung(Point location) {
        boolean buchen = true;
        DecimalFormat dfx = new DecimalFormat("0.00");
        Rezeptvector currVO = new Rezeptvector();
        Rezept rezCurrVO = new Rezept(aktuelAngezeigtesRezept);
        currVO.setVec_rez(Reha.instance.patpanel.vecaktrez);
        
        String sRezNr = currVO.getRezNb();
        logger.debug("Vec: sRezNr=" + sRezNr);
        sRezNr = rezCurrVO.getRezNr();
        logger.debug("Rez: sRezNr=" + sRezNr);
        if (ZuzahlTools.existsRGR(sRezNr)) {
            int anfrage = JOptionPane.showConfirmDialog(null,
                    "<html>" + ZuzahlTools.rgrOK(sRezNr) + "<br><br>" + "Wollen Sie eine Kopie erstellen?</html>",
                    "Achtung wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
            if (anfrage != JOptionPane.YES_OPTION) {
                return;
            }
            RgrKopie kopie = new RgrKopie(sRezNr);
            return;
        } else {
            // vvv Pruefungen aus der Bar-Quittung auch hier !
            // if (currVO.getZzStat().equals(String.valueOf(ZZStat.ZUZAHLFREI))) {
            // TODO: switch the following to ZZStat enum?
            if(rezCurrVO.getZZStatus() == Zuzahlung.ZZSTATUS_BEFREIT) {
                JOptionPane.showMessageDialog(null, "Zuzahlung nicht erforderlich!");
                return;
            }
            if (DatFunk.Unter18(DatFunk.sHeute(), DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)))) {
                JOptionPane.showMessageDialog(null,
                        "Stand heute ist der Patient noch nicht Vollj\u00e4hrig"
                      + " - Zuzahlung deshalb (bislang) noch nicht erforderlich");
                return;
            }
            if (ZuzahlTools.existsBarQuittung(sRezNr)) {
                JOptionPane.showMessageDialog(null,
                        "<html>Zuzahlung f\u00fcr Rezept  <b>" + sRezNr + "</b>  wurde bereits in bar geleistet.<br>"
                                + "Eine Rezeptgeb\u00fchren-Rechnung kann deshalb nicht mehr erstellt werden.</html>",
                        "Rezeptgeb\u00fchren-Rechnung nicht mehr m\u00f6glich", JOptionPane.WARNING_MESSAGE, null);
                return;
            }

        }

        HashMap<String, String> hmRezgeb = new HashMap<String, String>();
        int rueckgabe = -1;
        int i;
        String behandl = "";
        String strZuzahlung = "0.00";

        resetHmAdrRData();
        String termine = currVO.getTermine();
        logger.debug("Vec: termine=" + termine);
        termine = rezCurrVO.getTermine();
        logger.debug("Rez: termine=" + termine);
        RezTools.testeRezGebArt(false, false, sRezNr, termine);

        // String mit den Anzahlen und HM-Kuerzeln erzeugen
        for (i = 1; i < 5; i++) {
            String hmKurz = currVO.getHMkurz(i);
            logger.debug("Rez: hmKurz=" + hmKurz);
            hmKurz = rezCurrVO.getHMKuerzel(i);
            logger.debug("Vec: hmKurz=" + hmKurz);
            String sAktAnzBehandlg = currVO.getAnzBehS(i);
            logger.debug("Rez: hmKurz=" + sAktAnzBehandlg);
            int aktAnzBehandlg = rezCurrVO.getBehAnzahl(i);
            logger.debug("Vec: hmKurz=" + aktAnzBehandlg);
            if ((hmKurz != null) && hmKurz.length() > 0) {
                behandl += ((behandl.length() > 0) ? ", " : "") + aktAnzBehandlg + " * " + hmKurz;
            }
        }

        strZuzahlung = SystemConfig.hmAdrRDaten.get("<Rendbetrag>");

        String cmd = "select abwadress,id from pat5 where pat_intern='" + rezCurrVO.getPatIntern() + "' LIMIT 1";
        Vector<Vector<String>> adrvec = SqlInfo.holeFelder(cmd);
        String[] adressParams = null;

        abrRez = new AbrechnungRezept(null, connection);
        if (adrvec.get(0)
                  .get(0)
                  .equals("T")) {
            adressParams = abrRez.holeAbweichendeAdresse(adrvec.get(0)
                                                               .get(1));
        } else {
            adressParams = abrRez.getAdressParams(adrvec.get(0)
                                                        .get(1));
        }
        hmRezgeb.put("<rgreznum>", sRezNr);

        hmRezgeb.put("<rgbehandlung>", behandl);

        // hmRezgeb.put("<rgdatum>", DatFunk.sDatInDeutsch(currVO.getRezeptDatum()));
        hmRezgeb.put("<rgdatum>", rezCurrVO.getRezDatum().format(DateTimeFormatter.ofPattern("d.M.yyyy")));

        hmRezgeb.put("<rgbetrag>", strZuzahlung);
        hmRezgeb.put("<rgpauschale>", SystemConfig.hmAbrechnung.get("rgrpauschale"));
        hmRezgeb.put("<rggesamt>", "0,00");
        hmRezgeb.put("<rganrede>", adressParams[0]);
        hmRezgeb.put("<rgname>", adressParams[1]);
        hmRezgeb.put("<rgstrasse>", adressParams[2]);
        hmRezgeb.put("<rgort>", adressParams[3]);
        hmRezgeb.put("<rgbanrede>", adressParams[4]);

        hmRezgeb.put("<rgpatintern>", Integer.toString(rezCurrVO.getPatIntern()));
        
        hmRezgeb.put("<rgpatnname>", SystemConfig.hmAdrPDaten.get("<Pnname>"));
        hmRezgeb.put("<rgpatvname>", SystemConfig.hmAdrPDaten.get("<Pvname>"));
        hmRezgeb.put("<rgpatgeboren>", SystemConfig.hmAdrPDaten.get("<Pgeboren>"));

        RezeptGebuehrRechnung rgeb = new RezeptGebuehrRechnung(Reha.getThisFrame(), "Nachberechnung Rezeptgeb\u00fchren",
                rueckgabe, hmRezgeb, buchen);
        rgeb.setSize(new Dimension(250, 300));
        rgeb.setLocation(location.x - 50, location.y - 50);
        rgeb.pack();
        rgeb.setVisible(true);
    }

    /**********************************************/

    class ToolsDlgAktuelleRezepte {
        public ToolsDlgAktuelleRezepte(String command, Point pt) {
            Map<Object, ImageIcon> icons = new HashMap<Object, ImageIcon>();
            icons.put("Rezeptgeb\u00fchren kassieren", SystemConfig.hmSysIcons.get("rezeptgebuehr"));
            // Lemmi 20101218: angehaengt Rezeptgebuehr-Rechnung aus dem Rezept heraus
            // erzeugen
            // icons.put("Rezeptgebuehr-Rechnung erstellen",
            // SystemConfig.hmSysIcons.get("privatrechnung"));
            // McM: 'thematisch einsortiert' u. mit eigenem Icon (match mit Anzeige in
            // Rezeptliste):
            icons.put("Rezeptgeb\u00fchr-Rechnung erstellen", SystemConfig.hmSysIcons.get("rezeptgebuehrrechnung"));
            icons.put("BarCode auf Rezept drucken", SystemConfig.hmSysIcons.get("barcode"));
            icons.put("Ausfallrechnung drucken", SystemConfig.hmSysIcons.get("ausfallrechnung"));
            icons.put("Rezept ab-/aufschlie\u00dfen", SystemConfig.hmSysIcons.get("statusset"));
            icons.put("Privat-/BG-/Nachsorge-Rechnung erstellen", SystemConfig.hmSysIcons.get("privatrechnung"));
            icons.put("Behandlungstage in Clipboard", SystemConfig.hmSysIcons.get("einzeltage"));
            icons.put("Transfer in Historie", SystemConfig.hmSysIcons.get("redo"));
            icons.put("\u00a7301 Reha-Fallsteuerung", SystemConfig.hmSysIcons.get("abrdreieins"));

            // create a list with some test data
            JList list = new JList(new Object[] { "Rezeptgeb\u00fchren kassieren", "Rezeptgeb\u00fchr-Rechnung erstellen",
                    "BarCode auf Rezept drucken", "Ausfallrechnung drucken", "Rezept ab-/aufschlie\u00dfen",
                    "Privat-/BG-/Nachsorge-Rechnung erstellen", "Behandlungstage in Clipboard", "Transfer in Historie",
                    "\u00a7301 Reha-Fallsteuerung" });
            list.setCellRenderer(new IconListRenderer(icons));
            Reha.toolsDlgRueckgabe = -1;
            ToolsDialog tDlg = new ToolsDialog(Reha.getThisFrame(), "Werkzeuge: aktuelle Rezepte", list);
            tDlg.setPreferredSize(new Dimension(275, (255 + 28) + // Lemmi: Breite, H\u00f6he des Werkzeug-Dialogs
                    ((Boolean) SystemConfig.hmPatientenWerkzeugDlgIni.get("ToolsDlgShowButton") ? 25 : 0)));
            tDlg.setLocation(pt.x - 70, pt.y + 30);
            tDlg.pack();
            tDlg.setModal(true);
            tDlg.activateListener();
            tDlg.setVisible(true);
            if (Reha.toolsDlgRueckgabe > -1) {
                if (Reha.toolsDlgRueckgabe == 0) {
                    tDlg = null;
                    if (!Rechte.hatRecht(Rechte.Rezept_gebuehren, true)) {
                        return;
                    }
                    RezTools.constructRawHMap();
                    rezeptGebuehr();
                    return;
                }
                // Lemmi 20101218: neuer if Block: Rezeptgebuehr-Rechnung aus dem Rezept heraus
                // erzeugen
                else if (Reha.toolsDlgRueckgabe == 1) {
                    tDlg = null;
                    if (!Rechte.hatRecht(Rechte.Rezept_gebuehren, true)) {
                        return;
                    }
                    PointerInfo info = MouseInfo.getPointerInfo();
                    Point location = info.getLocation();
                    doRezeptgebuehrRechnung(location);

                    return;
                } else if (Reha.toolsDlgRueckgabe == 2) {
                    tDlg = null;
                    if (!Rechte.hatRecht(Rechte.Rezept_gebuehren, true)) {
                        return;
                    }
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            doBarcode();
                            return null;
                        }
                    }.execute();

                    return;
                } else if (Reha.toolsDlgRueckgabe == 3) {
                    tDlg = null;
                    if (!Rechte.hatRecht(Rechte.Rezept_ausfallrechnung, true)) {
                        return;
                    }
                    ausfallRechnung();
                    return;
                } else if (Reha.toolsDlgRueckgabe == 4) {
                    tDlg = null;
                    rezeptAbschliessen();
                    return;
                } else if (Reha.toolsDlgRueckgabe == 5) {
                    tDlg = null;
                    if (!Rechte.hatRecht(Rechte.Rezept_privatrechnung, true)) {
                        return;
                    }
                    try {
                        fuelleTage();
                        privatRechnung();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    }
                    return;
                } else if (Reha.toolsDlgRueckgabe == 6) {
                    tDlg = null;
                    doTageDrucken();
                    return;
                } else if (Reha.toolsDlgRueckgabe == 7) {
                    tDlg = null;
                    if (!Rechte.hatRecht(Rechte.Sonstiges_rezepttransfer, true)) {
                        return;
                    }
                    int anfrage = JOptionPane.showConfirmDialog(null,
                            "Das ausgew\u00e4hlte Rezept wirklich in die Historie transferieren?",
                            "Achtung wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
                    if (anfrage == JOptionPane.YES_OPTION) {
                        doUebertrag();
                    }
                    return;
                } else if (Reha.toolsDlgRueckgabe == 8) {
                    do301FallSteuerung();
                }
            }

            tDlg = null;
        }
    }

    class rezToolbarButtons {
        
        public rezToolbarButtons() {
            initToolbarBtns();
        }
        
    }


}

class RezNeuDlg extends RehaSmartDialog {
    /**
     *
     */
    private static final long serialVersionUID = -7104716962577408414L;
    private static final Logger logger = LoggerFactory.getLogger(RehaSmartDialog.class);
    private RehaTPEventClass rtp = null;

    public RezNeuDlg() {
        super(null, "RezeptEditor");
        this.setName("RezeptEditor");
        rtp = new RehaTPEventClass();
        rtp.addRehaTPEventListener(this);

    }

    @Override
    public void rehaTPEventOccurred(RehaTPEvent evt) {
        try {
            if (evt.getDetails()[0] != null) {
                logger.debug("rehaTPEvent: " + evt.toString());
                logger.debug("This-name: " + this.getName() + " evt-name: " + evt.getDetails()[0].toString());
                for ( String detail : evt.getDetails()) {
                    logger.debug("Detail: " + detail);
                }
                if (evt.getDetails()[0].equals(this.getName())) {
                    this.setVisible(false);
                    this.dispose();
                    rtp.removeRehaTPEventListener(this);
                    rtp = null;
                    ListenerTools.removeListeners(this);
                    super.dispose();
                }
            } else {
            }
        } catch (NullPointerException ne) {
            ne.printStackTrace();
        } catch (Exception ex) {

        }
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
        if (rtp != null) {
            this.setVisible(false);
            rtp.removeRehaTPEventListener(this);
            rtp = null;
            dispose();
            ListenerTools.removeListeners(this);
            super.dispose();
        }
    }

}


