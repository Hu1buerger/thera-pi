package abrechnung;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.therapi.reha.patient.AktuelleRezepte;
import org.therapi.reha.patient.Historie;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.star.uno.Exception;

import CommonTools.DatFunk;
import CommonTools.JCompTools;
import CommonTools.JRtaCheckBox;
import CommonTools.JRtaComboBox;
import CommonTools.JRtaTextField;
import CommonTools.SqlInfo;
import CommonTools.StringTools;
import CommonTools.ini.INIFile;
import CommonTools.ini.INITool;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.text.TextException;
import ag.ion.noa.NOAException;
import commonData.Rezeptvector;
import environment.Path;
import hauptFenster.Reha;
import hauptFenster.UIFSplitPane;
import jxTableTools.DblCellEditor;
import jxTableTools.DoubleTableCellRenderer;
import jxTableTools.MitteRenderer;
import jxTableTools.MyTableCheckBox;
import jxTableTools.MyTableComboBox;
import oOorgTools.OOTools;
import patientenFenster.KassenAuswahl;
import patientenFenster.PatUndVOsuchen;
import patientenFenster.RezNeuanlage;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;
import systemTools.AdressTools;
import systemTools.ListenerTools;

/**
 * @author Admin
 *
 */
public class AbrechnungRezept extends JXPanel implements HyperlinkListener, ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 8387184772704779192L;
    private AbrechnungGKV eltern;
    JToolBar tb = null;

    DecimalFormat dfx = new DecimalFormat("0.00");
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    String preisgruppe = "-1";
    String aktDisziplin = "";

    JButton[] tbbuts = { null, null, null, null };
    JComboBox tbcombo = null;
    JToggleButton tog = null;
    JLabel[] labs = { null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null };

    JLabel aktRezNum = null;
    Vector<Vector<Object>> vec_tabelle = new Vector<Vector<Object>>();
    Vector<Object> vecdummy = new Vector<Object>();

    Vector<Vector<String>> vectage = null;
    Rezeptvector aktRezept = new Rezeptvector();
    Vector<Vector<String>> vec_pat = null;
    Vector<Vector<String>> vec_term = null;
    Vector<Vector<String>> vec_hb = null;

    Vector<Vector<String>> vec_kuerzel = new Vector<Vector<String>>();
    Vector<String> kundid = new Vector<String>();

    Vector<String> vec_poskuerzel = new Vector<String>();
    Vector<String> vec_pospos = new Vector<String>();
    Vector<Integer> vec_posanzahl = new Vector<Integer>();

    JXTree treeRezept = null;

    public DefaultMutableTreeNode rootRezept;
    public DefaultMutableTreeNode rootRdaten;
    public DefaultMutableTreeNode rootPdaten;
    public DefaultMutableTreeNode rootAdaten;
    public DefaultMutableTreeNode rootTdaten;
    public DefaultMutableTreeNode rootGdaten;
    public DefaultMutableTreeNode rootStammdaten;
    public DefaultTreeModel treeModelRezept;
    final String plus = "+";
    final String EOL = "'\n";
    final String SOZ = "?";
    Vector<Vector<String>> preisvec = null;

    JRtaComboBox cmbkuerzel = null;
    JRtaComboBox cmbbreak = null;

    JComboBox cmbpreis = null;
    JCheckBox chkzuzahl = null;

    boolean patAktuellFrei = false;
    boolean patVorjahrFrei = false;
    boolean patU18 = false;
    String patFreiAb;
    String patFreiBis;
    boolean gebuehrBezahlt;
    Double gebuehrBetrag;
    boolean mitPauschale;

    private String zuZahlungsIndex = "";
    private String zuZahlungsPos = "";

    String disziplinIndex = "";
    String disziplinGruppe = "";
    int preisregelIndex = -1;

    int anzahlhb = 0;
    int anzahlposhb = 0;
    boolean hausbesuch = false;

    Double rezeptWert;
    Double zuzahlungWert;
    Double kmWert;
    int hbstrecke = -1;
    String hbkmpos = "";
    JXDatePicker datePick = new JXDatePicker();
    JXMonthView sv;

    public boolean rezeptSichtbar = false;

    public JXTable tageTbl = null;
    // public MyTageTableModel tageMod = new MyTageTableModel();
    MyTableComboBox mycomb;
    MyTableComboBox mycomb2;
    MyTableComboBox mycomb3;
    MyTableComboBox mycomb4;
    MyTableCheckBox mycheck;
    JRtaCheckBox check;

    StringBuffer buf1 = new StringBuffer();
    StringBuffer buf2 = new StringBuffer();
    StringBuffer buf3 = new StringBuffer();

    private UIFSplitPane jSplitOU = null;
    private String[] voArt = { "Erstverordnung", "Folgeverordnung", "Folgeverordn. außerhalb d. Regelf." };
    private String[] voIndex = { "01", "02", "10" };

    private String[] voBreak = { "", "K", "F", "T", "A" };
    // private String[] voPreis = {"akt. Tarif","alter Tarif"};
    JEditorPane htmlPane = null;
    JScrollPane scrHtml = null;

    private JXTTreeTableNode aktNode;
    private int aktRow;
    private JXTTreeTableNode root = null;
    private TageTreeTableModel demoTreeTableModel = null;
    private JXTreeTable jXTreeTable = null;
    private JXTTreeTableNode foo = null;
    // private JXMonthView mv;
    JDialog dlg;

    private int popUpX;
    private int popUpY;

    ActionListener tbaction = null;
    Rectangle rec = new Rectangle(0, 0, 0, 0);

    boolean rezeptFertig = false;

    public boolean mitTarifWechsel = false;
    public int neueTarifgruppe = -1;

    StringBuffer edibuf = new StringBuffer();
    StringBuffer htmlpos = new StringBuffer();
    StringBuffer htmlposbuf = new StringBuffer();
    String[] zzpflicht = { "keine gesetzliche Zuzahlung", "Zuzahlungsbefreit",
            "keine Zuzahlung trotz schriftlicher Zahlungsaufforderung", "Zuzahlungspflichtig",
            "Übergang zuzahlungspflichtig zu zuzahlungsfrei", "Übergang zuzahlungsfrei zu zuzahlungspflichtig" };

    boolean inworker = false;

    boolean notready = false;

    boolean tagedrucken = false;

    JRtaCheckBox cbtagedrucken = new JRtaCheckBox("Behandlungstage drucken");

    JRtaTextField[] aKasse = { new JRtaTextField("nix", false), new JRtaTextField("nix", false),
            new JRtaTextField("nix", false) };

    boolean ohneDrecksPauschale = false;

    boolean inParseHtml = false;
    private boolean vec_rez_valid = false; // flag, ob vec_rez gültig ist

    private TageTreeSize tts = null;
    private String currBerichtId = "";
    boolean kannAbhaken = false;
    private Connection connection;
    private static final Logger logger = LoggerFactory.getLogger(AbrechnungRezept.class);

    public AbrechnungRezept(AbrechnungGKV xeltern, Connection conn) {
        this.connection = conn;
        eltern = xeltern;
        tts = new TageTreeSize();
        setLayout(new BorderLayout());
        cmbkuerzel = new JRtaComboBox(vec_kuerzel, 0, 1);
        cmbkuerzel.setActionCommand("cmbkuerzel");
        cmbkuerzel.addActionListener(this);
        JXPanel tmp = getSplitPane();
        add(tmp, BorderLayout.CENTER);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jSplitOU.setDividerLocation(getHeight() - 200);
                if (SystemConfig.hmAbrechnung.get("keepTTSize")
                                             .equals("1")) { // TageTreeSize Werte in ini gespeichert (McM)
                    int maxBehTage = Integer.parseInt(SystemConfig.hmAbrechnung.get("maxBehTage"));
                    INIFile inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/",
                            "abrechnung.ini");
                    String section = "HMGKVRechnung";
                    for (int i = 1; i <= maxBehTage; i++) {
                        String key = "TTS_" + i;
                        if (inif.getStringProperty(section, key) != null) { // Eintrag in ini vorhanden?
                            tts.setDaysAndSize(i, inif.getIntegerProperty(section, key));
                            System.out.println("read TTS_" + i + " = " + inif.getIntegerProperty(section, key));
                        }
                    }
                }
            }
        });
        tmp.addComponentListener(windowResizeHandler);
    }

    private JXPanel getSplitPane() {
        JXPanel jpan = new JXPanel();
        jpan.setLayout(new BorderLayout());
        jSplitOU = UIFSplitPane.createStrippedSplitPane(JSplitPane.VERTICAL_SPLIT, getHTMLPanel(), getTageTree());
        jSplitOU.setDividerSize(7);
        jSplitOU.setDividerBorderVisible(true);
        jSplitOU.setName("BrowserSplitObenUnten");
        jSplitOU.setOneTouchExpandable(true);

        jSplitOU.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, splitPaneDiverChangeHandler);

        jpan.add(getToolbar(), BorderLayout.NORTH);
        jpan.add(jSplitOU, BorderLayout.CENTER);

        return jpan;
    }

     void keepDayTreeSize(UIFSplitPane sPane) {
        if (SystemConfig.hmAbrechnung.get("keepTTSize")
                                     .equals("0")) { // Fkt. abgeschaltet?
            return;
        }
        int yPos = sPane.getDividerLocation();
        int max = sPane.getHeight() - sPane.getDividerSize();
        if ((yPos > 0) && (yPos < max)) {
            tts.setTageTreeSize(yPos, max);
        }
    }

    /*
     * überwacht Veränderungen am Split-Panel Divider
     *
     * @author McM 1606
     */
     PropertyChangeListener splitPaneDiverChangeHandler = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (pce.getPropertyName() == JSplitPane.DIVIDER_LOCATION_PROPERTY) {
                 keepDayTreeSize(jSplitOU);
            }
    }
     };
    private JScrollPane getHTMLPanel() {
        htmlPane = new JEditorPane(/* initialURL */);
        htmlPane.setContentType("text/html");
        htmlPane.setEditable(false);
        htmlPane.setOpaque(false);
        htmlPane.addHyperlinkListener(this);
        parseHTML(null);
        scrHtml = JCompTools.getTransparentScrollPane(htmlPane);
        scrHtml.validate();
        return scrHtml;
    }

    public void setKuerzelVec(String xreznummer, String preisgr) {
        if (xreznummer.startsWith("KG")) {
            preisvec = RezTools.holePreisVector("KG", Integer.parseInt(preisgr.trim()) - 1);
            // disziplinIndex = "2";
            disziplinIndex = SystemConfig.hmHmPosIndex.get("KG");
            // disziplinGruppe = "22";
            disziplinGruppe = SystemConfig.hmHmPraefix.get("KG");
            preisregelIndex = 0;
            aktDisziplin = "Physio";
        } else if (xreznummer.startsWith("MA")) {
            preisvec = RezTools.holePreisVector("MA", Integer.parseInt(preisgr.trim()) - 1);
            // disziplinIndex = "1";
            disziplinIndex = SystemConfig.hmHmPosIndex.get("MA");
            // disziplinGruppe = "21";
            disziplinGruppe = SystemConfig.hmHmPraefix.get("MA");
            preisregelIndex = 1;
            aktDisziplin = "Massage";
        } else if (xreznummer.startsWith("ER")) {
            preisvec = RezTools.holePreisVector("ER", Integer.parseInt(preisgr.trim()) - 1);
            // disziplinIndex = "5";
            disziplinIndex = SystemConfig.hmHmPosIndex.get("ER");
            // disziplinGruppe = "26";
            disziplinGruppe = SystemConfig.hmHmPraefix.get("ER");
            preisregelIndex = 2;
            aktDisziplin = "Ergo";
        } else if (xreznummer.startsWith("LO")) {
            preisvec = RezTools.holePreisVector("LO", Integer.parseInt(preisgr.trim()) - 1);
            // disziplinIndex = "3";
            disziplinIndex = SystemConfig.hmHmPosIndex.get("LO");
            // disziplinGruppe = "23";
            disziplinGruppe = SystemConfig.hmHmPraefix.get("LO");
            preisregelIndex = 3;
            aktDisziplin = "Logo";
        } else if (xreznummer.startsWith("RH")) {
            preisvec = RezTools.holePreisVector("RH", Integer.parseInt(preisgr.trim()) - 1);
            // disziplinIndex = "8";
            disziplinIndex = SystemConfig.hmHmPosIndex.get("RH");
            // disziplinGruppe = "29";
            disziplinGruppe = SystemConfig.hmHmPraefix.get("RH");
            preisregelIndex = 4;
            aktDisziplin = "Reha";
        } else if (xreznummer.startsWith("PO")) {
            preisvec = RezTools.holePreisVector("PO", Integer.parseInt(preisgr.trim()) - 1);
            // disziplinIndex = "7";
            disziplinIndex = SystemConfig.hmHmPosIndex.get("PO");
            // disziplinGruppe = "71";
            disziplinGruppe = SystemConfig.hmHmPraefix.get("PO");
            preisregelIndex = 5;
            aktDisziplin = "Podo";
        } else if (xreznummer.startsWith("RS")) {
            preisvec = RezTools.holePreisVector("RS", Integer.parseInt(preisgr.trim()) - 1);
            // disziplinIndex = "7";
            disziplinIndex = SystemConfig.hmHmPosIndex.get("RS");
            // disziplinGruppe = "71";
            disziplinGruppe = SystemConfig.hmHmPraefix.get("RS");
            preisregelIndex = 6;
            aktDisziplin = "Rsport";
        } else if (xreznummer.startsWith("FT")) {
            preisvec = RezTools.holePreisVector("FT", Integer.parseInt(preisgr.trim()) - 1);
            // disziplinIndex = "7";
            disziplinIndex = SystemConfig.hmHmPosIndex.get("FT");
            // disziplinGruppe = "71";
            disziplinGruppe = SystemConfig.hmHmPraefix.get("FT");
            preisregelIndex = 7;
            aktDisziplin = "Ftrain";
        }
        vec_kuerzel.clear();
        int idpos = preisvec.get(0)
                            .size()
                - 1;
        for (int i = 0; i < preisvec.size(); i++) {
            kundid.clear();
            kundid.add(preisvec.get(i)
                               .get(1));
            kundid.add(preisvec.get(i)
                               .get(idpos));
            vec_kuerzel.add((Vector<String>) kundid.clone());
        }
        Comparator<Vector> comparator = new Comparator<Vector>() {

            public int compare(String s1, String s2) {
                String[] strings1 = s1.split("\\s");
                String[] strings2 = s2.split("\\s");
                return strings1[strings1.length - 1].compareTo(strings2[strings2.length - 1]);
            }

            @Override
            public int compare(Vector o1, Vector o2) {
                String s1 = (String) o1.get(0);
                String s2 = (String) o2.get(0);
                return s1.compareTo(s2);
            }
        };
        Collections.sort(vec_kuerzel, comparator);
        ////// System.out.println("Aus Funktion setKuerzelVec="+vec_kuerzel);
        mycomb2.setVector(vec_kuerzel, 0, 1);

    }

    public boolean getTageDrucken() {
        return this.tagedrucken;
    }

    public void setTageDrucken(boolean drucken) {
        this.tagedrucken = drucken;
    }

    public void sperreAbrechnung() {
        tbbuts[3].setEnabled(false);
    }

    public void erlaubeAbrechnung() {
        tbbuts[3].setEnabled(true);
    }

    public boolean setNewRez(String rez, boolean schonfertig, String aktDisziplin) {

        String preisgr = SqlInfo.holeEinzelFeld("select preisgruppe from verordn where rez_nr='" + rez + "' LIMIT 1");
        this.aktDisziplin = aktDisziplin;
        rezeptFertig = schonfertig;
        notready = false;
        if (!rezeptFertig) {
            jXTreeTable.setEditable(true);
            aktRezNum.setText(rez);
            setKuerzelVec(rez, preisgr);
            setWerte(rez);
            regleAbrechnungsModus(); // sucheRezept() läuft gar nicht? Nö, macht scheinbar setWerte()
            Reha.instance.progressStarten(false);
        } else {
            ////// System.out.println("Einlesen aus Edifact-Daten");
            jXTreeTable.setEditable(false);
            aktRezNum.setText(rez);
            setKuerzelVec(rez, preisgr);
            if (holeEDIFACT(rez)) {
                while (inworker) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                prepareTreeFromVector(true);
                doTreeRezeptWertermitteln();

                regleAbrechnungsModus();
                parseHTMLuniq(rez);

                doPositionenErmitteln();
            } else {
                JOptionPane.showMessageDialog(null, "Fehler im EDIFACT dieses Rezeptes");
                setRechtsAufNull();
            }

        }
        if (SystemConfig.hmAbrechnung.get("keepTTSize")
                                     .equals("1")) {
            // int allePositionen = vec_tabelle.size(); // alle Behandl. (x/Tag) + HB +
            // Arztber. (16160: 13)
            // int behTage = jXTreeTable.getRowCount(); // 16160: 6 (ok) -> hmap für divider
            // Positionierung (McM)
            int rows = jXTreeTable.getRowCount(); // akt Anz. Zeilen in TageTree
            // System.out.println("AbrechnungRezept->setNewRez: "+rows+" Zeilen");
            tts.setAnzTage(rows);
            jSplitOU.setDividerLocation(tts.getTageTreeSize(rows));
        }
        rezeptSichtbar = true;
        /*
         * }catch(Exception ex){ ex.printStackTrace(); JOptionPane.showMessageDialog(
         * null,"Fehler - Rezept kann nicht abgerechner werden"); return false; }
         */
        return true;
    }

    /******
     *
     *
     * @return
     */

    private JXPanel getTageTree() {
        JXPanel jpan = new JXPanel(new BorderLayout());
        FormLayout lay = new FormLayout("0dlu,0dlu,fill:0:grow(1.0),20dlu,0dlu",
                "0dlu,p,0dlu,fill:0:grow(0.5),2dlu,fill:0:grow(0.5),60dlu");
        jpan.setLayout(lay);
        CellConstraints cc = new CellConstraints();

        root = new JXTTreeTableNode("root", null, true);
        demoTreeTableModel = new TageTreeTableModel(root);
        Highlighter hl = HighlighterFactory.createAlternateStriping();

        jXTreeTable = new JXTreeTable(demoTreeTableModel);
        jXTreeTable.addHighlighter(hl);
        jXTreeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                if (evt.getButton() == 3) {
                    TreePath selpathss = jXTreeTable.getPathForLocation(evt.getX(), evt.getY());
                    // jXTreeTable.addHighlighter(HighlighterFactory.createAlternateStriping());
                    jXTreeTable.getTreeSelectionModel()
                               .setSelectionPath(selpathss);
                    ZeigePopupMenu(evt.getX(), evt.getY(), evt.getXOnScreen(), evt.getYOnScreen());
                } else {
                    jXTreeTable.setShowGrid(false);
                }
            }

        });

        jXTreeTable.setOpaque(true);
        jXTreeTable.setRootVisible(false);

        // ComboBox von Behandlungsart
        mycomb2 = new MyTableComboBox();
        ((JRtaComboBox) mycomb2.getComponent()).setActionCommand("kuerzel");
        ((JRtaComboBox) mycomb2.getComponent()).addActionListener(this);
        jXTreeTable.getColumnModel()
                   .getColumn(2)
                   .setCellEditor(mycomb2);
        // Anzahlspalte
        jXTreeTable.getColumnModel()
                   .getColumn(3)
                   .setCellEditor(new DblCellEditor());
        // Preisspalte
        // jXTreeTable.getColumnModel().getColumn(4).setCellEditor(new DblCellEditor());
        jXTreeTable.getColumnModel()
                   .getColumn(4)
                   .setCellRenderer(new DoubleTableCellRenderer());

        // Checkbox von Zuzahlung
        check = new JRtaCheckBox();
        check.setActionCommand("zuzahlung");
        // check.addActionListener(this);
        check.setOpaque(true);
        ActionListener alcheck = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                zuzahlCheck(check.isSelected()); ////// System.out.println("Wert von Check = "+check.isSelected());
            }
        };

        mycheck = new MyTableCheckBox(check, alcheck);
        // ((JRtaCheckBox)mycheck.getComponent()).setActionCommand("zuzahlung");
        // ((JRtaCheckBox)mycheck.getComponent()).addActionListener(this);
        jXTreeTable.getColumnModel()
                   .getColumn(5)
                   .setCellEditor(mycheck);
        // jXTreeTable.getColumnModel().getColumn(5).setCellRenderer(new
        // CheckRenderer(false));

        // Preis
        jXTreeTable.getColumnModel()
                   .getColumn(4)
                   .setCellEditor(new DblCellEditor());
        jXTreeTable.getColumnModel()
                   .getColumn(4)
                   .setCellRenderer(new DoubleTableCellRenderer());
        jXTreeTable.getColumnModel()
                   .getColumn(4)
                   .getCellEditor()
                   .addCellEditorListener(new CellEditorListener() {
                       @Override
                       public void editingStopped(ChangeEvent e) {
                           getVectorFromNodes();
                           doTreeRezeptWertermitteln();
//                doPositionenErmitteln();
                           /*
                            * while(inParseHtml){ try { Thread.sleep(25); } catch (InterruptedException ex)
                            * { ex.printStackTrace(); } } parseHTML(aktRezept.getRezNb());
                            */
                           parseHTMLuniq(aktRezept.getRezNb());
                       }

                       @Override
                       public void editingCanceled(ChangeEvent e) {
                       }

                   });

        // Zuzahlungsbetrag
        jXTreeTable.getColumnModel()
                   .getColumn(6)
                   .setCellEditor(new DblCellEditor());
        jXTreeTable.getColumnModel()
                   .getColumn(6)
                   .setCellRenderer(new DoubleTableCellRenderer());

        // Unterbrechungskennzeichen
        JRtaComboBox unterbrechung = new JRtaComboBox(voBreak);
        unterbrechung.setActionCommand("break");
        unterbrechung.addActionListener(this);
        MyTableComboBox combbreak = new MyTableComboBox(unterbrechung);
        jXTreeTable.getColumnModel()
                   .getColumn(7)
                   .setCellEditor(combbreak);
        jXTreeTable.getColumnModel()
                   .getColumn(7)
                   .setCellRenderer(new MitteRenderer());

        // Unterbrechungskennzeichen
        JRtaComboBox tarifart = new JRtaComboBox(new String[] { "aktuell", "alt" });
        tarifart.setActionCommand("akttarif");
        tarifart.addActionListener(this);
        MyTableComboBox combtarifakt = new MyTableComboBox(tarifart);
        jXTreeTable.getColumnModel()
                   .getColumn(8)
                   .setCellEditor(combtarifakt);
        jXTreeTable.getColumnModel()
                   .getColumn(8)
                   .setCellRenderer(new MitteRenderer());

        // sqldatum
        jXTreeTable.getColumnModel()
                   .getColumn(9)
                   .setMinWidth(0);
        jXTreeTable.getColumnModel()
                   .getColumn(9)
                   .setMaxWidth(0);
        jXTreeTable.getColumnModel()
                   .getColumn(10)
                   .setMinWidth(0);
        jXTreeTable.getColumnModel()
                   .getColumn(10)
                   .setMaxWidth(0);

        jXTreeTable.getColumn(0)
                   .setMinWidth(55);
        jXTreeTable.validate();
        // jXTreeTable.getColumn(1).setCellEditor(new MyTableStringDatePicker());
        // jXTreeTable.getColumn(0).setCellEditor(new MyDateCellEditor());
        jXTreeTable.setSortOrder(9, SortOrder.ASCENDING);
        // jXTreeTable.getSelectionModel().addListSelectionListener(new
        // AbrechnungListSelectionHandler());

        jXTreeTable.addTreeSelectionListener(new AbrechnungTreeSelectionListener());
        jXTreeTable.setSelectionMode(0);

        JScrollPane jscr = JCompTools.getTransparentScrollPane(jXTreeTable);
        jscr.validate();
        // jpan.add(jscr,cc.xywh(3,4,1,3));
        jpan.add(jscr, cc.xywh(1, 4, 5, 4));
        return jpan;

    }

    private void zuzahlCheck(boolean zuzahl) {
        int nodes;
        if (demoTreeTableModel.getPathToRoot(aktNode).length == 2 && ((nodes = aktNode.getChildCount()) > 0)) {
            String text = "Soll die Einstellung '" + (zuzahl ? "Zuzahlungspflichtig" : "keine Zuzahlung")
                    + "' auf den gesamten Knoten angewendet werden";
            int anfrage = JOptionPane.showConfirmDialog(null, text, "Achtung wichtige Benutzeranfrage",
                    JOptionPane.YES_NO_OPTION);
            if (anfrage == JOptionPane.YES_OPTION) {
                for (int i = 0; i < nodes; i++) {
                    ((JXTTreeTableNode) aktNode.getChildAt(i)).abr.zuzahlung = Boolean.valueOf(zuzahl);
                }
            }
            if (this.aktRow == 0) {
                this.mitPauschale = zuzahl;
            }
        } else {
            aktNode.abr.zuzahlung = Boolean.valueOf(zuzahl);
            ////// System.out.println(((JXTTreeTableNode)aktNode).abr.bezeichnung);
            ////// System.out.println( "Zuzahlung
            ////// ="+((JXTTreeTableNode)aktNode).abr.zuzahlung);
            if (this.aktRow == 0) {
                this.mitPauschale = zuzahl;
            }
        }
        jXTreeTable.repaint();
        // aktualisiereTree();
        doTreeRezeptWertermitteln();
        /*
         * while(inParseHtml){ try { Thread.sleep(25); } catch (InterruptedException ex)
         * { ex.printStackTrace(); } } parseHTML(aktRezept.getRezNb());
         */
        parseHTMLuniq(aktRezept.getRezNb());
    }

    public void ZeigePopupMenu(int x, int y, int x2, int y2) {
        JPopupMenu jPop = getTerminPopupMenu();
        popUpX = x2;
        popUpY = y2;
        jPop.show(jXTreeTable, x, y);
    }

    private JPopupMenu getTerminPopupMenu() {
        JPopupMenu jPopupMenu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Alle Knoten expandieren");
        item.setActionCommand("expandall");
        item.addActionListener(this);
        jPopupMenu.add(item);
        item = new JMenuItem("Alle Knoten schließen");
        item.setActionCommand("collapsall");
        item.addActionListener(this);
        jPopupMenu.add(item);
        jPopupMenu.addSeparator();
        item = new JMenuItem("neuen Tag einfügen");
        item.setActionCommand("tagneu");
        item.addActionListener(this);
        if (rezeptFertig) {
            item.setEnabled(false);
        }
        jPopupMenu.add(item);
        jPopupMenu.addSeparator();
        item = new JMenuItem("neue Behandlung einfügen");
        item.setActionCommand("behandlungneu");
        item.addActionListener(this);
        if (rezeptFertig) {
            item.setEnabled(false);
        }
        jPopupMenu.add(item);
        jPopupMenu.addSeparator();
        item = new JMenuItem("Behandlung löschen");
        item.setActionCommand("behandlungloeschen");
        item.addActionListener(this);
        if (rezeptFertig) {
            item.setEnabled(false);
        }
        jPopupMenu.add(item);

        return jPopupMenu;
    }

    private JXMonthView showView() {
        final JXMonthView mv = new JXMonthView();
        mv.addActionListener(this);
        mv.setName("picker2");
        mv.setTraversable(true);
        mv.setPreferredColumnCount(1);
        mv.setPreferredRowCount(1);
        mv.setShowingWeekNumber(true);
        return mv;
    }

    public void actionAbschluss() {
        if (!eltern.isRezeptSelected()) {
            JOptionPane.showMessageDialog(null, "Kein Rezept zum Auf-/Abschließen ausgewählt");
            return;
        }

        if (rezeptFertig) {
            jXTreeTable.setEditable(true);
            rezeptFertig = false;
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    SqlInfo.sqlAusfuehren("update fertige set ediok='F',edifact='' where rez_nr='"
                            + aktRezept.getRezNb() + "' LIMIT 1");
                    eltern.setKassenUmsatzNeu();
                    return null;
                }

            }.execute();
        } else {
            /*
             * hier muß die RgebKontrolle rein
             *
             */
            if (!kannAbhaken) {
                JOptionPane.showMessageDialog(null, "Rezeptgebühren nicht bezahlt und keine Rechnung erstellt!!!");
                return;
            }
            if (rezeptWert <= zuzahlungWert) {
                JOptionPane.showMessageDialog(null, "<html><b>Glückwunsch zum größten -> D E P P E N  (des Jahres "
                        + SystemConfig.aktJahr + ")</b></html>");
                return;
            }
            if (macheEDIFACT()) {
                jXTreeTable.setEditable(false);
                rezeptFertig = true;
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        SqlInfo.sqlAusfuehren(
                                "update fertige set ediok='T',edifact='" + StringTools.Escaped(edibuf.toString())
                                        + "' where rez_nr='" + aktRezept.getRezNb() + "' LIMIT 1");
                        eltern.setKassenUmsatzNeu();
                        return null;
                    }

                }.execute();
            } else {
                jXTreeTable.setEditable(false);
                rezeptFertig = false;
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        SqlInfo.sqlAusfuehren("update fertige set ediok='F',edifact='' where rez_nr='"
                                + aktRezept.getRezNb() + "' LIMIT 1");
                        return null;
                    }

                }.execute();
            }
            // hier den Edifact einbauen
        }
        eltern.setRezeptOk(rezeptFertig);
    }

    private JToolBar getToolbar() {
        JToolBar jtb = new JToolBar();
        jtb.setOpaque(false);
        jtb.setRollover(true);
        jtb.setBorder(null);

        tbaction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String cmd = arg0.getActionCommand();
                if (cmd.equals("abschliessen")) {
                    /*
                     * if(kannAbhaken){ actionAbschluss(); }else{
                     * JOptionPane.showMessageDialog(null, "Rezeptgebühren nicht bezahlt!!!"); }
                     */
                    actionAbschluss();
                }
                if (cmd.equals("scannen")) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                File f = new File(Path.Instance.getProghome() + "scanrun.bat");
                                if (!f.exists()) {
                                    JOptionPane.showMessageDialog(null, "Datei scanrun.bat existiert nicht");
                                } else {
                                    if (aktRezNum.getText()
                                                 .equals("")) {
                                        JOptionPane.showMessageDialog(null, "Kein Rezept zum Scannen ausgewählt");
                                        return;
                                    }
                                    Runtime.getRuntime()
                                           .exec(Path.Instance.getProghome() + "scanrun.bat " + aktRezNum.getText());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                }
                if (cmd.equals("taxieren")) {
                    doTaxieren();
                }
                if (cmd.equals("abrechnungstarten")) {
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            try {
                                tbbuts[3].setEnabled(false);
                                Reha.instance.progressStarten(true);
                                eltern.abrDlg = new AbrechnungDlg();
                                eltern.abrDlg.pack();
                                eltern.abrDlg.setLocationRelativeTo(eltern);
                                eltern.abrDlg.setzeLabel("starte Heilmittelabrechnung");
                                eltern.starteAbrechnung();
                            } catch (NullPointerException ex) {

                            }
                            tbbuts[3].setEnabled(true);
                            return null;
                        }
                    }.execute();
                }
                if (cmd.equals("302oderIV")) {
                    regleAbrechnungsModus();
                }

            }
        };
        JXPanel rezpan = new JXPanel(new BorderLayout());
        rezpan.setBorder(BorderFactory.createEtchedBorder(1));
        // rezpan.setPreferredSize(new Dimension(150,0));
        rezpan.setSize(100, 30);
        rezpan.setMaximumSize(new Dimension(150, 30));
        rezpan.setOpaque(false);
        aktRezNum = new JLabel();
        aktRezNum.setHorizontalAlignment(JLabel.CENTER);
        aktRezNum.setFont(new Font("Tahoma", Font.PLAIN, 15));
        aktRezNum.setForeground(Color.BLUE);
        rezpan.add(aktRezNum, BorderLayout.CENTER);
        jtb.add(rezpan);

        tbbuts[2] = new JButton();
        tbbuts[2].setIcon(SystemConfig.hmSysIcons.get("abschliessen"));
        tbbuts[2].setToolTipText("Rezept abschließen");
        tbbuts[2].setActionCommand("abschliessen");
        tbbuts[2].addActionListener(tbaction);
        jtb.add(tbbuts[2]);

        jtb.addSeparator(new Dimension(30, 0));

        tbbuts[0] = new JButton();
        tbbuts[0].setIcon(SystemConfig.hmSysIcons.get("print"));
        tbbuts[0].setToolTipText("Rezept taxieren");
        tbbuts[0].setActionCommand("taxieren");
        tbbuts[0].addActionListener(tbaction);
        jtb.add(tbbuts[0]);
        if (tbcombo == null) {
            tbcombo = new JComboBox(SystemConfig.vecTaxierung);
        }

        // tbcombo.setSize(new Dimension(75,26));
        tbcombo.setMaximumSize(new Dimension(100, 26));
        jtb.add(tbcombo);
        jtb.addSeparator(new Dimension(30, 0));

        tbbuts[1] = new JButton();
        tbbuts[1].setIcon(SystemConfig.hmSysIcons.get("scanner"));
        tbbuts[1].setToolTipText("Rezept scannen");
        tbbuts[1].setActionCommand("scannen");
        tbbuts[1].addActionListener(tbaction);
        jtb.add(tbbuts[1]);

        jtb.addSeparator(new Dimension(40, 0));

        tog = new JToggleButton();
        tog.setIcon(SystemConfig.hmSysIcons.get("abrdreizwei"));
        tog.setToolTipText("§ 302 oder IV Abrechnung");
        tog.setActionCommand("302oderIV");
        tog.addActionListener(tbaction);
        jtb.add(tog);

        jtb.addSeparator(new Dimension(40, 0));
        tbbuts[3] = new JButton();
        tbbuts[3].setIcon(SystemConfig.hmSysIcons.get("bombe"));
        tbbuts[3].setToolTipText("Die gewählte Kasse abrechnen");
        tbbuts[3].setActionCommand("abrechnungstarten");
        tbbuts[3].addActionListener(tbaction);
        jtb.add(tbbuts[3]);

        return jtb;
    }

    public void doTaxieren() {
        if (aktRezNum.getText()
                     .equals("")) {
            JOptionPane.showMessageDialog(null, "Kein Rezept zum Taxieren ausgewählt");
            return;
        }
        // Prüfen ob Rezept abgeschlossen ist. Wenn nicht zurück
        /*
         * String kilometerpos =
         * SystemConfig.vHBRegeln.get(Integer.parseInt(preisgruppe)-1).get(2).replace(
         * "x",disziplinIndex); String pauschalepos =
         * SystemConfig.vHBRegeln.get(Integer.parseInt(preisgruppe)-1).get(3).replace(
         * "x",disziplinIndex); String hauptziffer =
         * SystemConfig.vHBRegeln.get(Integer.parseInt(preisgruppe)-1).get(0).replace(
         * "x",disziplinIndex); String mehrereziffer =
         * SystemConfig.vHBRegeln.get(Integer.parseInt(preisgruppe)-1).get(1).replace(
         * "x",disziplinIndex);
         */
        String kilometerpos = SystemPreislisten.hmHBRegeln.get(aktDisziplin)
                                                          .get(Integer.parseInt(preisgruppe) - 1)
                                                          .get(2);
        String pauschalepos = SystemPreislisten.hmHBRegeln.get(aktDisziplin)
                                                          .get(Integer.parseInt(preisgruppe) - 1)
                                                          .get(3);
        String hauptziffer = SystemPreislisten.hmHBRegeln.get(aktDisziplin)
                                                         .get(Integer.parseInt(preisgruppe) - 1)
                                                         .get(0);
        String mehrereziffer = SystemPreislisten.hmHBRegeln.get(aktDisziplin)
                                                           .get(Integer.parseInt(preisgruppe) - 1)
                                                           .get(1);

        HashMap<String, String> taxWerte = new HashMap<String, String>();
        JXTTreeTableNode node;

        String km = "";

        boolean hb = false;

        boolean hbmit = false;

        boolean wgkm = false;

        boolean wgpausch = false;
        // boolean hbcecked = false;
        boolean kmchecked = false;

        // String preisid = "";
        String testepos = "";
        int hbanzahl = 0;
        String hbpos = "";
        int hbmitanzahl = 0;
        String hbmitpos = "";
        int wgpauschalanzahl = 0;
        String wgpauschpos = "";
        int wgkmanzahl = 0;
        int wgkmstrecke = 0;

        String wgkmpos = "";

        for (int i = 0; i < getNodeCount(); i++) {
            node = holeNode(i);
            testepos = RezTools.getPosFromID(node.abr.preisid, preisgruppe, preisvec);
            if (testepos.equals(hauptziffer)) {
                hbanzahl++;
                hbpos = testepos.toString();
                hb = true;
            } else if (testepos.equals(mehrereziffer)) {
                hbmitanzahl++;
                hbmitpos = testepos.toString();
                hbmit = true;
            } else if (testepos.equals(pauschalepos)) {
                wgpauschalanzahl++;
                wgpauschpos = testepos.toString();
                wgpausch = true;
            } else if (testepos.equals(kilometerpos) && (!kmchecked)) {
                // System.out.println("! kmchecked "+kilometerpos+"/"+kmchecked);
                wgkmpos = testepos.toString();
                wgkmstrecke = Integer.parseInt(Double.toString(node.abr.anzahl)
                                                     .replace(".0", ""));
                wgkmanzahl++;
                wgkm = true;
                kmchecked = true;
            } else if (testepos.contains(kilometerpos) && (kmchecked)) {
                // System.out.println("kmchecked "+kilometerpos+"/"+kmchecked);
                wgkmanzahl++;
            }
        }
        /*
         * ////System.out.println("Hausbesuche = "+hbpos + "/ "+hbanzahl);
         * ////System.out.println("Mehrere  = "+hbmitpos + "/ "+hbmitanzahl);
         * ////System.out.println("Pauschale WG = "+wgpauschpos+
         * " / "+wgpauschalanzahl); ////System.out.println("Kilometer WG = "+wgkmpos+
         * " / Strecke "+wgkmstrecke+ " / "+wgkmanzahl);
         * ////System.out.println(vec_pospos); ////System.out.println(vec_posanzahl);
         */
        for (int i = 1; i <= 18; i++) {
            taxWerte.put("<t" + i + ">", "");
        }
        taxWerte.put("<t1>", Reha.getAktIK());
        taxWerte.put("<t3>", dfx.format(rezeptWert));
        taxWerte.put("<t2>", dfx.format(zuzahlungWert));
        int taxpos = 4;
        String nohb = hbpos + hbmitpos + wgpauschpos + wgkmpos;
        for (int i = 0; i < vec_pospos.size(); i++) {
            if (!nohb.contains(vec_pospos.get(i))) {
                taxWerte.put("<t" + taxpos + ">", vec_pospos.get(i));
                taxWerte.put("<t" + (taxpos + 1) + ">", Integer.toString(vec_posanzahl.get(i)));
                taxpos += 2;
            } else if (hbpos.equals(vec_pospos.get(i))) {
                taxWerte.put("<t13>", hbpos);
                taxWerte.put("<t14>", Integer.toString(hbanzahl));
            } else if (hbmitpos.equals(vec_pospos.get(i))) {
                taxWerte.put("<t15>", hbmitpos);
                taxWerte.put("<t16>", Integer.toString(hbmitanzahl));
            } else if (wgpauschpos.equals(vec_pospos.get(i))) {
                taxWerte.put("<t10>", wgpauschpos);
                taxWerte.put("<t11>", Integer.toString(wgpauschalanzahl));
            } else if (wgkmpos.equals(vec_pospos.get(i))) {
                taxWerte.put("<t10>", wgkmpos);
                taxWerte.put("<t11>", Integer.toString(wgkmanzahl));
                taxWerte.put("<t12>", Integer.toString(wgkmstrecke));
            }
        }
        taxWerte.put("<t18>", aktRezNum.getText());

        try {
            String bcform = SqlInfo.holeEinzelFeld(
                    "select barcodeform from verordn where rez_nr='" + aktRezNum.getText()
                                                                                .trim()
                            + "' LIMIT 1");
            String formular = Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK() + "/"
                    + tbcombo.getSelectedItem()
                             .toString();
            OOTools.starteTaxierung(formular, taxWerte);
        } catch (OfficeApplicationException e) {
            e.printStackTrace();
        } catch (NOAException e) {
            e.printStackTrace();
        } catch (TextException e) {
            e.printStackTrace();
        }

    }

    private void regleAbrechnungsModus() {

        if (tog.isSelected()) {
            eltern.abrechnungsModus = eltern.ABR_MODE_IV;
            tog.setIcon(SystemConfig.hmSysIcons.get("abriv"));
            if (this.jXTreeTable.getRowCount() > 0) {
                String ivkasse = aktRezept.getKtraeger();
                macheHashMapIV(ivkasse);
                /*
                 * while(inParseHtml){ try { Thread.sleep(25); } catch (InterruptedException ex)
                 * { ex.printStackTrace(); } } parseHTML(aktRezept.getRezNb());
                 */
                parseHTMLuniq(aktRezept.getRezNb());
            }
            if (SystemConfig.certState > 0) {
                tbbuts[3].setEnabled(true);
            }
        } else {
            eltern.abrechnungsModus = eltern.ABR_MODE_302;
            this.setTageDrucken(false);
            tog.setIcon(SystemConfig.hmSysIcons.get("abrdreizwei"));
            // eltern.hmAlternativeKasse.clear();
            if (this.jXTreeTable.getRowCount() > 0) {
                int waitTimes = 20;
                int maxWait = waitTimes;
                while ((vec_rez_valid == false) && (maxWait > 0)) { // sucheRezept() ist noch nicht fertig...
                    try {
                        Thread.sleep(25);
                        maxWait--; // Abbruchbedingung, falls Suche nix liefert
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                if (maxWait == 0) {
                    System.out.println("AbrechnungRezept: sucheRezept() ohne Ergebnis");
                }
                /*
                 * if(maxWait < waitTimes) {
                 * System.out.println("AbrechnungRezept: maxWait: "+maxWait); }
                 */
                /*
                 * while(inParseHtml){ try { Thread.sleep(25); } catch (InterruptedException ex)
                 * { ex.printStackTrace(); } } parseHTML(aktRezept.getRezNb());
                 */
                parseHTMLuniq(aktRezept.getRezNb());
            }
            if (SystemConfig.certState > 0) {
                tbbuts[3].setEnabled(false);
            }
        }
    }

    private void macheHashMapIV(String id) {
        String cmd = "select kassen_nam1,kassen_nam2,strasse,plz,ort,id from kass_adr where id='" + id.trim()
                + "' LIMIT 1";
        Vector<Vector<String>> iv_vec = SqlInfo.holeFelder(cmd);
        eltern.hmAlternativeKasse.put("<Ivnam1>", iv_vec.get(0)
                                                        .get(0));
        eltern.hmAlternativeKasse.put("<Ivnam2>", iv_vec.get(0)
                                                        .get(1));
        eltern.hmAlternativeKasse.put("<Ivstrasse>", iv_vec.get(0)
                                                           .get(2));
        eltern.hmAlternativeKasse.put("<Ivplz>", iv_vec.get(0)
                                                       .get(3));
        eltern.hmAlternativeKasse.put("<Ivort>", iv_vec.get(0)
                                                       .get(4));
        eltern.hmAlternativeKasse.put("<Ivid>", iv_vec.get(0)
                                                      .get(5));

    }

    public void setRechtsAufNull() {
//        while( root.getChildCount() > 0){
//            demoTreeTableModel.removeNodeFromParent((MutableTreeTableNode) root.getChildAt(0));
//        }
        baumLoeschen();
        vec_tabelle.clear();
        String shtml = "<html></html";
        if (!htmlPane.getText()
                     .contains("p302.png")) {

            shtml = "<html><head>" + "<STYLE TYPE=\"text/css\">" + "<!--"
                    + "html, body{height:100%; width:100%; margin:0; padding:0;}"
                    + "#center {position:relative; top:50%; left:50%; margin:25px 0 0 75px;}" + "--->" + "</STYLE>"
                    + "</head>" + "<body>" +

                    "<div id=\"center\">" + "<img src=\"file:///" + Path.Instance.getProghome()
                    + "icons/p302.png\" align=\"center\">" + "</div>" +

                    "</body>" + "</html>";
            htmlPane.setText(shtml);
        }
        aktRezNum.setText("");

        rezeptSichtbar = false;
        return;

    }

    public void setHtmlText(String text) {
        htmlPane.setText(text);
    }

    private void setWerte(String rez_nr) {
        if (AbrechnungGKV.directCall) {
            AbrechnungGKV.directCall = false;
            return;
        }

        vec_rez_valid = Boolean.FALSE; // ungültig bis neu belegt
        sucheRezept(rez_nr);
        /*
         * String cmd =
         * "select * from verordn where rez_nr='"+rez_nr.trim()+"' LIMIT 1";
         * //////System.out.println("Kommando = "+cmd);
         * aktRezept.setVec_rez(SqlInfo.holeFelder(cmd));
         * //////System.out.println("RezeptVektor = "+vec_rez);
         * aktRezept.init(rez_nr.trim()); vec_rez_valid = Boolean.TRUE;
         */
        if (aktRezept.getVecSize() <= 0) {
            return;
        }
        /*
         * gebuehrBezahlt = aktRezept.getGebuehrBezahlt(); gebuehrBetrag =
         * aktRezept.getGebuehrBetrag(); // 0 1 2 3 4 5 String cmd =
         * "select  t1.n_name,t1.v_name,t1.geboren,t1.strasse,t1.plz,t1.ort,"+ // 6 7 8
         * 9 10 11 12 "t1.v_nummer,t1.kv_status,t1.kv_nummer,"+
         * "t1.befreit,t1.bef_ab,t1.bef_dat,t1.jahrfrei,"+ // 13 14 15 16
         * "t2.nachname,t2.bsnr,t2.arztnum,t3.kassen_nam1 from pat5 t1,arzt t2,kass_adr t3 where t1.pat_intern='"
         * + aktRezept.getPatIntern()+"' AND t2.id ='"+aktRezept.getArztId()
         * +"' AND t3.id='"+aktRezept.getKtraeger()+"' LIMIT 1";
         * ////System.out.println(cmd); vec_pat = SqlInfo.holeFelder(cmd);
         * ////System.out.println(vec_pat);
         */
        int barcodeform = 0;
        try {
            barcodeform = aktRezept.getBarcodeform();
        } catch (NullPointerException ex) {

        }
        this.tbcombo.setSelectedIndex(barcodeform <= 1 ? barcodeform : 0);
        /*
         * if(vec_pat.size() <= 0){ JOptionPane.showMessageDialog(null,
         * "Diesem Rezept ist eine unbrauchbare Kasse und/oder Arzt zugeordnet. Bitte korrigieren"
         * ); return; } if (vec_pat.get(0) .get(9) .equals("T")) { patAktuellFrei =
         * true; } else { patAktuellFrei = false; } if (!vec_pat.get(0) .get(12) .trim()
         * .equals("")) { patVorjahrFrei = true; } else { patVorjahrFrei = false; }
         * patFreiAb = vec_pat.get(0).get(10); patFreiBis = vec_pat.get(0).get(11);
         * patU18 = DatFunk.Unter18(DatFunk.sHeute(),
         * DatFunk.sDatInDeutsch(vec_pat.get(0).get(2)));
         */
        ermittleAbrechnungsfall(true);
        if (hausbesuch) {
            doHausbesuchKomplett();
        }
        ////// System.out.println("Nodes insgesamt ="+getNodeCount()+" VectorLänge =
        ////// "+vec_tabelle.size());
        // Hier den TheraPiebericht einbauen;
        doGebuehren();
        String therapiebericht = SystemPreislisten.hmBerichtRegeln.get(this.aktDisziplin)
                                                                  .get(Integer.parseInt(preisgruppe) - 1);
        if (therapiebericht != null) {
            if (!therapiebericht.equals("") && aktRezept.getArztbericht()) {
                String berichtid = aktRezept.getArztBerichtID();
                if (!AbrechnungGKV.directCall) {
                    showTbInfo(aktRezept.getArztBerichtID());
                    addTherapieBericht(therapiebericht);
                    if (berichtid.equals("")) {
                        setToClipboard(rez_nr.trim());
                    }
                } else {
                    AbrechnungGKV.directCall = false;
                    return;
                }
            }
        }
        
        if (aktRezept.getUseHygPausch()) {
            Disziplinen diszi = new Disziplinen();
            int currIdx = diszi.getIndex(this.aktDisziplin);
            String currPrefix = diszi.getPrefix(diszi.getIndex(this.aktDisziplin));
            String hmPosHygieneMehraufwand = currPrefix + "9944";
            addHygieneMehraufwand(hmPosHygieneMehraufwand);
        }
        this.getVectorFromNodes();
        doTarifWechselCheck();

        doPositionenErmitteln();
        doTreeRezeptWertermitteln();
        parseHTMLuniq(rez_nr);
        try {

            if (rezeptWert <= zuzahlungWert) {
                String deppenAnsage = "<html><font color=#FF0000><b>Achtung, Achtung, Achtung !</b></font><br><br>"
                        + "Der Rezeptwert ist <b>geringer oder gleich hoch</b> wie die Zuzahlung des Patienten.<br>"
                        + "Die Abrechnung dieses Rezeptes würde demzufolge zur Ablehnung der gesamten<br>"
                        + "Rechnung führen.<br>Insofern ist die Abrechnung dieses Rezeptes <b>nicht empfehlenswert!</b><br><br>"
                        + "Rezeptwert = " + dfx.format(rezeptWert) + "<br>" + "Rezeptgebühr = "
                        + dfx.format(zuzahlungWert) + "<br><br>"
                        + "Ihre Forderung an die Kasse wäre demzufolge: <font color=#FF0000><b>"
                        + dfx.format(rezeptWert - zuzahlungWert) + " EUR</b></font>" +

                        "<br><br></html>";
                JOptionPane.showMessageDialog(null, deppenAnsage);
            }
        } catch (NullPointerException ex) {

        }
    }

    /*
     * verhindert die mehrfache Anzeige der TB-Meldung im Fall 'Rezept abschließen
     * bei geöffnetem Abrechnungspanel' wenn 'AutoOKwenn302offen = 0' eingestellt
     * ist (workaround; eleganter wäre setWerte() nur einmal zu durchlaufen... )
     */
    private void showTbInfo(final String berichtId) {
        if (currBerichtId.equals(berichtId)) {
            return;
        }
        ; // Info fÃ¼r diesen Bericht wird bereits angezeigt
        currBerichtId = berichtId;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String dlgcmd = "<html>Für dieses Rezept wurde ein Therapiebericht angefordert!<br>"
                        + (berichtId.equals("")
                                ? "Es wurde aber <b><font color=#FF0000>kein</font> Therapiebericht erstellt</b>"
                                : "Der Therapiebericht wurde <b>bereits erstellt</b>")
                        + "<br><br>Position Therapiebericht wird an den letzten Behandlungstag angehängt</html>";
                JOptionPane.showMessageDialog(null, dlgcmd);
                currBerichtId = "";
            }
        });
    }

    private void setToClipboard(final String xcrez) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws java.lang.Exception {
                String cmd = "select rez_nr,Substring_index(Substring_index(Substring_index(Substring_index(termine, '\n',-4),'\n',1),'@',-4),'@',1 )as reststring "
                        + "from verordn where rez_nr = '" + xcrez + "' LIMIT 1";

                Vector<Vector<String>> xvec = SqlInfo.holeFelder(cmd);
                // System.out.println(xvec);
                if (xvec.size() > 0) {
                    Historie.copyToClipboard(xvec.get(0)
                                                 .get(0)
                            + "\t" + xvec.get(0)
                                         .get(1)
                            + "\n");
                }

                return null;
            }

        }.execute();

    }

    private void zurLetztenBehHinzufuegen(String position, boolean mitZuzahlung) {
        if (root.getChildCount() <= 0) {
            JOptionPane.showMessageDialog(null,
                    "Es sind keine Behandlungspositionen ermittelbar.\nWurden evtl. Positionen aus der Preisliste gelöscht?");
            return;
        }
        boolean zuZahlFrei = mitZuzahlung ? false : true;
        JXTTreeTableNode xnode = (JXTTreeTableNode) root.getChildAt(root.getChildCount() - 1);
        JXTTreeTableNode ynode = (JXTTreeTableNode) getBasicNodeFromChild(xnode);
        abrfallAnhaengen(xnode.getChildCount() - 1, ynode, ynode.abr.datum, position,
                Double.parseDouble("1.00"), zuZahlFrei);
    }
    
    private void addTherapieBericht(String berichtsposition) {
        zurLetztenBehHinzufuegen(berichtsposition, false);
    }

    private void addHygieneMehraufwand(String hmPosition) {
        zurLetztenBehHinzufuegen(hmPosition, false);
    }

    /*****************************************************************************************/
    private void ermittleAbrechnungsfall(boolean construct) {
        try {
            // vectage = RezTools.macheTerminVector(
            // aktRezept.getVecVec_rez().get(0).get(34)); // OK - den hier löschen!
            vectage = RezTools.macheTerminVector(aktRezept.getTermine());

            // Vector<Vector<String>> vecabrfaelle = new Vector<Vector<String>>();
            // Vector<String> vecabrfall = new Vector<String>();
            vec_tabelle.clear();
            vec_poskuerzel.clear();
            vec_posanzahl.clear();
            vec_pospos.clear();
            String[] behandlungen = null;
            //// System.out.println("Anzahl Tage = "+vectage.size());
            //// System.out.println("Anzahl Tage im einzelnen"+vectage.size());
            preisgruppe = aktRezept.getPreisgruppe();
            int anzahlbehandlungen = 0;
            /*
             * int[] ids = {Integer.parseInt(aktRezept.getVec_rez().get(0).get(8)),
             * Integer.parseInt(aktRezept.getVec_rez().get(0).get(9)),
             * Integer.parseInt(aktRezept.getVec_rez().get(0).get(10)),
             * Integer.parseInt(aktRezept.getVec_rez().get(0).get(11)) }; for(int i = 0; i <
             * 4; i++){ if(ids[i] > 0 ){
             */
            for (int i = 1; i <= 4; i++) {
                if (!aktRezept.getArtDBehandl(i)
                              .equals("0")) { // <- passt das?
                    anzahlbehandlungen++; // Anz. Heilmittel im Rezept
                } else {
                    break;
                }
            }
            //// System.out.println("******************Anzahl Behandlungen =
            //// "+anzahlbehandlungen);
            // int anzahlhb = 0;
            // int anzahlposhb = 0;
            // boolean hausbesuch = false;

            /*
             * if(aktRezept.getVec_rez().get(0).get(43).equals("T")){ hausbesuch= true;
             * if(RezTools.zweiPositionenBeiHB(aktDisziplin,preisgruppe)){ anzahlposhb = 2;
             * anzahlhb=Integer.valueOf(aktRezept.getVec_rez().get(0).get(64)); }else{
             * anzahlposhb = 1;
             * anzahlhb=Integer.valueOf(aktRezept.getVec_rez().get(0).get(64)); } } else {
             * hausbesuch = false; }
             */
            hausbesuch = aktRezept.getHausbesuch();
            anzahlhb = aktRezept.getAnzHB();
            if (RezTools.zweiPositionenBeiHB(aktDisziplin, preisgruppe)) {
                anzahlposhb = 2;
            } else {
                anzahlposhb = 1;
            }

            String splitvec = null;
            if (construct) {
//            while( root.getChildCount() > 0){
//                demoTreeTableModel.removeNodeFromParent((MutableTreeTableNode) root.getChildAt(0));
//            }
                baumLoeschen();
            }
            boolean toomuchhinweis = false;
            for (int i = 0; i < vectage.size(); i++) {
                splitvec = vectage.get(i)
                                  .get(3);
                behandlungen = splitvec.split(",");
                //// System.out.println(vectage.size());
                //// System.out.println(vectage.get(i).get(0));
                int anzahlBehandlungen = aktRezept.getAnzBeh(1);
                if (behandlungen.length > 0 && (!splitvec.trim()
                                                         .equals(""))) {
                    // Es stehen Behandlungsdaten im Terminblatt;
                    // Positionen = length+hbposanzahl;
                    //// System.out.println("Länge des Feldes = "+behandlungen.length);
                    // anzahlbehandlungen = behandlungen.length;
//                if((i+1) <= Integer.parseInt(aktRezept.getVec_rez().get(0).get(3))){
                    if ((i + 1) <= anzahlBehandlungen) {
                        //// System.out.println("in if");
                        constructTagVector(vectage.get(i)
                                                  .get(0),
                                behandlungen, behandlungen.length, anzahlhb, i, false);
                    } else { // if((i+1) > anzahlBehandlungen)
                        //// System.out.println("in else if");
                        constructTagVector(vectage.get(i)
                                                  .get(0),
                                behandlungen, behandlungen.length, anzahlhb, i, true);
                        toomuchhinweis = true;
                    }
                } else {
                    // Es sind keine Behandlungsformen im Terminblatt verzeichnet;
                    // in anzahlbehandlungen steht die tatsächliche Anzahl
                    //// System.out.println("Keine Behandlungen im Terminblatt =
                    // "+anzahlbehandlungen);
                    //// System.out.println("Anzahl-Hausbesuch keine Beh. im Terminblatt=
                    // "+anzahlhb);
                    //// System.out.println("Konstruiere Abrechnungsfall aus Rezeptangaben");
                    if ((i + 1) <= anzahlBehandlungen) {
                        constructTagVector(vectage.get(i)
                                                  .get(0),
                                null, anzahlbehandlungen, anzahlhb, i, false);
                    } else { // if((i+1) > anzahlBehandlungen)
                        constructTagVector(vectage.get(i)
                                                  .get(0),
                                null, anzahlbehandlungen, anzahlhb, i, true);
                        toomuchhinweis = true;
                    }

                }
            }
            if (toomuchhinweis) {
                //
                // JOptionPane.showMessageDialog(null,"Achtung - Sie rechnen mehr
                // Behandlungstage ab als im Rezept angegeben wurde!");
            }

            if (construct) {
                // Hier muß der Hinweis rein
                doFuelleTreeTable();
                aktNode = null;
                aktRow = -1;
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    /******************************
     *
     */
    private void doGebuehren() {
        zuZahlungsIndex = zzpflicht[3];
        zuZahlungsPos = "3";
        int nodes;
        //// System.out.println("Einsprung in doGebuehren");
        if ((nodes = getNodeCount()) <= 0) {
            //// System.out.println("NodeCount <= 0");
            return;
        }

        boolean amBeginnFrei = false;

        boolean amEndeFrei = false;

        boolean volleZuzahlung = true;

        boolean unter18 = false;

        boolean vollFrei = false;

        boolean teilFrei = false;
        mitPauschale = true;

        boolean jahresWechsel = false;
        zuZahlungsPos = "3";
        //// System.out.println("Einsprung 2 in doGebuehren");
        // if(SystemPreislisten.hmZuzahlRegeln.get(aktDisziplin).get(Integer.valueOf(preisgruppe)-1).equals("0")){
        if (SystemPreislisten.hmZuzahlRegeln.get(aktDisziplin)
                                            .get(Integer.valueOf(preisgruppe) - 1) <= 0) {
            //// System.out.println("keine Zuzahlung bei dieser Kasse");
            zuZahlungsIndex = zzpflicht[0];
            zuZahlungsPos = "0";
            mitPauschale = false;
            doTreeFreiAb(0, nodes, false);
            doTarifWechselCheck();
            arschGeigenCheck();
            return;
        }
        if (aktRezNum.getText()
                     .startsWith("RS")
                || aktRezNum.getText()
                            .startsWith("FT")) {
            zuZahlungsIndex = zzpflicht[0];
            zuZahlungsPos = "0";
            mitPauschale = false;
            doTreeFreiAb(0, nodes, false);
            doTarifWechselCheck();
            arschGeigenCheck();
            return;
        }
        if (patU18) {
            //// System.out.println("Patient unter 18");
            unter18 = true;
            vollFrei = true;
            amBeginnFrei = true;
            volleZuzahlung = false;
            mitPauschale = false;
            doTreeFreiAb(0, nodes, false);
            zuZahlungsIndex = zzpflicht[0];
            zuZahlungsPos = "0";

            doTarifWechselCheck();
            arschGeigenCheck();
            return;

        } else {
            Object[] u18 = RezTools.unter18Check(vec_tabelle, DatFunk.sDatInDeutsch(vec_pat.get(0)
                                                                                           .get(2)));
            //// System.out.println("Unter 18 = "+u18[0]+" / ab Position = "+u18[1]+"
            //// Splitting erforderlich = "+u18[2]);
            //// System.out.println("else von U 18");
            if (((Boolean) u18[0]) && (!(Boolean) u18[2])) {
                // Während der Kompletten Behandlung unter 18;
                //// System.out.println("Patient während Behandlung komplett U 18");
                unter18 = true;
                vollFrei = true;
                amBeginnFrei = true;
                volleZuzahlung = false;
                mitPauschale = false;
                doTreeFreiAb(0, nodes, false);
                zuZahlungsIndex = zzpflicht[0];
                zuZahlungsPos = "0";

                doTarifWechselCheck();
                arschGeigenCheck();
                return;

            } else if (((Boolean) u18[0]) && ((Boolean) u18[2])) {
                // Während der Behandlung 18 geworden, Rezept muß gesplittet werden;
                //// System.out.println("Patient während Behandlung 18 geworden");
                unter18 = true;
                teilFrei = true;
                amBeginnFrei = true;
                volleZuzahlung = false;
                mitPauschale = false;
                doTreeFreiAb(0, (Integer) u18[1], false);
                doTreeFreiAb((Integer) u18[1], nodes, true);
                zuZahlungsIndex = zzpflicht[5];
                zuZahlungsPos = "5";

                doTarifWechselCheck();
                arschGeigenCheck();
                return;
            }
        }

        Object[] newYear = RezTools.jahresWechselCheck(vec_tabelle, unter18);
        //// System.out.println("Jahreswechsel = "+newYear[0]+" / JahresWechsel ab
        //// Position = "+newYear[1]+" / Rezept vollständig im Vorjahr = "+newYear[2]);
        /*
         * int wechselfall = 0; int altfall = 0; int neufall = 0;
         *
         * for(int i = 0; i < newYear.length;i++){
         * //System.out.println("Werte von newYear = "+newYear[i]); }
         */
        if ((Boolean) newYear[0] && (Boolean) newYear[2]) {
            // Das Rezept ist komplett im Vorjahr abgearbeitet worden
            //// System.out.println("Altfall 0");
            if (patVorjahrFrei && (!unter18)) {
                // altfall = 1;
                //// System.out.println("Altfall 1");
                doTreeFreiAb(0, nodes, false);
                mitPauschale = false;
                zuZahlungsIndex = zzpflicht[1];
                zuZahlungsPos = "1";
            } else {
                // altfall = 2;
                //// System.out.println("Altfall 2");
                doTreeFreiAb(0, nodes, true);
                mitPauschale = true;
                zuZahlungsIndex = zzpflicht[3];
                zuZahlungsPos = "3";
            }

        } else if ((Boolean) newYear[0] && (!(Boolean) newYear[2])) {
            // Das Rezept ist erstreckt sich über den Jahreswechsel
            if (gebuehrBezahlt && !patVorjahrFrei) { // 1.Fall
                // die Gebühr wurde bereits bezahlt was soviel heißt wie es muß verrechnet
                // werden
                doTreeFreiAb(0, nodes, true);
                mitPauschale = true;
                // wechselfall = 1;
                zuZahlungsIndex = zzpflicht[3];
                zuZahlungsPos = "3";
            } else if (patVorjahrFrei && patAktuellFrei && (!gebuehrBezahlt)) { // 2.Fall
                // im Vorjahr befreit und jetzt schon wieder befreit und kein Vermerk bezahlt
                doTreeFreiAb(0, nodes, false);
                mitPauschale = false;
                // wechselfall = 2;
                zuZahlungsIndex = zzpflicht[1];
                zuZahlungsPos = "1";
            } else if ((patVorjahrFrei) && (!patAktuellFrei)) { // 3.Fall
                // im Vorjahr befreit und jetzt zuzahlungspflichtig
                doTreeFreiAb(0, (Integer) newYear[1], false);
                doTreeFreiAb((Integer) newYear[1], nodes, true);
                mitPauschale = false;
                // wechselfall = 3;
                zuZahlungsIndex = zzpflicht[5];
                zuZahlungsPos = "5";
            } else if ((!patVorjahrFrei) && (!patAktuellFrei) && (!unter18)) { // 4.Fall
                // weder im Vorjahr noch im aktuellen Jahr befreit und auch nicht unter 18
                doTreeFreiAb(0, nodes, true);
                mitPauschale = true;
                // wechselfall = 4;
                zuZahlungsIndex = zzpflicht[3];
                zuZahlungsPos = "3";
            } else if ((!patVorjahrFrei) && (patAktuellFrei) && (!unter18)) { // 5.Fall
                // war nicht im Vorjahr nicht befreit ist aber jetzt befreit
                doTreeFreiAb(0, (Integer) newYear[1], true);
                doTreeFreiAb((Integer) newYear[1], nodes, false);
                mitPauschale = true;
                // wechselfall = 5;
                zuZahlungsIndex = zzpflicht[4];
                zuZahlungsPos = "4";
            }
        } else if (!(Boolean) newYear[0]) {
            // Das Rezept wurde vollständig im aktuelle Jahr abgearbeitet
            if (patAktuellFrei && (!gebuehrBezahlt)) {
                doTreeFreiAb(0, nodes, false);
                mitPauschale = false;
                // neufall = 1;
                zuZahlungsIndex = zzpflicht[1];
                zuZahlungsPos = "1";
            } else if (patAktuellFrei && gebuehrBezahlt) {
                doTreeFreiAb(0, nodes, true);
                mitPauschale = true;
                // neufall = 2;
                zuZahlungsIndex = zzpflicht[3];
                zuZahlungsPos = "3";
            }
        }
        //// System.out.println("/*****************************************/");
        //// System.out.println("Im aktuellen Jahr befreit? = "+patAktuellFrei);
        //// System.out.println(" Befreit ab? = "+(DatFunk.datumOk(patFreiAb)==null ?
        //// "keine Angaben" : DatFunk.datumOk(patFreiAb)) );
        //// System.out.println(" Befreit bis? = "+(DatFunk.datumOk(patFreiBis)==null ?
        //// "keine Angaben" : DatFunk.datumOk(patFreiBis)) );
        //// System.out.println(" Im VorjahrJahr befreit? = "+patVorjahrFrei);
        //// System.out.println(" Patient ist unter 18? = "+patU18);
        //// System.out.println(" Rezeptgebühren bezahlt? = "+gebuehrBezahlt);
        //// System.out.println(" Rezeptgebühren Betrag? = "+gebuehrBetrag);
        //// System.out.println(" Mit 10 EUR Pauschale? = "+mitPauschale);
        //// System.out.println(" Konstellation nur alt? = "+altfall);
        //// System.out.println(" Konstellation wechsel? = "+wechselfall);
        //// System.out.println(" Konstellation nur neu? = "+neufall);
        //// System.out.println("/*****************************************/");
        /***** HausbesuchsCheck *****/

        ///// Jetzt der Tarifwchsel-Check
        // erst einlesen ab wann der Tarif gültig ist
        // dann testen ob Rezeptdatum nach diesem Datum liegt wenn ja sind Preise o.k.
        // wenn nein -> testen welche Anwendungsregel gilt und entsprechend in einer
        // for next Schleife die Preise anpassen!
        // bevor jetzt weitergemacht werden kann muß der Vector für die Behandlungen
        ///// erstellt werden!!!!!
        doTarifWechselCheck();
        arschGeigenCheck();

    }

    /*************************** TarifWechselCheck ********************************/
    private void doTarifWechselCheck() {
        int tarifgruppe = Integer.valueOf(preisgruppe) - 1;
        // String datum =
        // SystemConfig.vNeuePreiseAb.get(this.preisregelIndex).get(tarifgruppe);
        String datum = SystemPreislisten.hmNeuePreiseAb.get(aktDisziplin)
                                                       .get(tarifgruppe);
        if (datum.trim()
                 .equals("")) {
            return;
        }
        // int regel =
        // SystemConfig.vNeuePreiseRegel.get(this.preisregelIndex).get(tarifgruppe);
        int regel = SystemPreislisten.hmNeuePreiseRegel.get(aktDisziplin)
                                                       .get(tarifgruppe);
        String erster = getDatumErsterTag();
        String letzter = getDatumLetzterTag();

        ////// System.out.println(DatFunk.TageDifferenz(datum, getDatumErsterTag()));
        ////// System.out.println(DatFunk.TageDifferenz("31.12.2009", "31.12.2009"));

        if (regel == 1) {
            // erste Behandlung >= Stichtag alle zu neuem Tarif
            if (DatFunk.TageDifferenz(datum, erster) < 0) {
                // setze alle auf alten Tarif
                setTarif(true, false, "");
                return;
            }
        }
        if (regel == 2) {
            // Rezeptdatum >= Stichtag
            if (DatFunk.TageDifferenz(datum, DatFunk.sDatInDeutsch(aktRezept.getRezeptDatum())) < 0) {
                // setze alle auf alten Tarif
                setTarif(true, false, "");
                return;
            }
        }
        if (regel == 3) {
            // Beliebiger Tag innerhalb er Spanne
            // 06.03.2013 bislang war hier eine oder Verknüpfung was absolut verkehrt ist.
            // /st.
            if ((DatFunk.TageDifferenz(datum, erster) < 0) && (DatFunk.TageDifferenz(datum, letzter) < 0)) {
                // setze alle auf alten Tarif
                setTarif(true, false, "");
                return;
            }
        }
        if (regel == 4) {
            // es muß gesplittet werden
            setTarif(false, true, datum);
            return;
        }

    }

    private void setTarif(boolean allealt, boolean split, String splitdatum) {
        int count = getNodeCount();
        AbrFall abr;
        if (allealt) {
            for (int i = 0; i < count; i++) {
                abr = this.holeAbrFall(i);
                abr.alterpreis = "alt";
                abr.preis = Double.valueOf(RezTools.getPreisAltFromID(abr.preisid, preisgruppe, preisvec)
                                                   .replace(",", "."));
            }
            return;
        } else if (split) {
            for (int i = 0; i < count; i++) {
                abr = this.holeAbrFall(i);
                if (DatFunk.TageDifferenz(splitdatum, abr.datum) < 0) {
                    abr.alterpreis = "alt";
                    String preis = RezTools.getPreisAltFromID(abr.preisid, preisgruppe, preisvec)
                                           .replace(",", ".");
                    ////// System.out.println("Alter Preis = "+preis);
                    ////// System.out.println("PreisID = "+abr.preisid);
                    abr.preis = Double.valueOf(preis);
                }
            }
            return;
        }
    }

    private void arschGeigenCheck() {
        try {
            if (SystemConfig.vArschgeigenDaten.size() <= 0) {
                this.mitTarifWechsel = false;
                return;
            }
            this.mitTarifWechsel = false;
            for (int i = 0; i < SystemConfig.vArschgeigenDaten.size(); i++) {
                if (SystemConfig.vArschgeigenDaten.get(i)
                                                  .indexOf(eltern.getAktKTraeger()) >= 0) {
                    // System.out.println("ArschgeigenCheck - Tarifwechsel");
                    setTarifWechsel(i);
                    return;
                }
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        return;
    }

    private void setTarifWechsel(int welcher) {
        try {
            int tarifalt = (Integer) SystemConfig.hmArschgeigenModus.get("Tarifalt" + Integer.toString(welcher));
            int tarifneu = (Integer) SystemConfig.hmArschgeigenModus.get("Tarifneu" + Integer.toString(welcher));
            // Vector<Vector<String>> vecalt = (Vector<Vector<String>>)
            // RezTools.holePreisVector(aktRezNum.getText().substring(0,2),tarifalt ) ;
            Vector<Vector<String>> vecneu = RezTools.holePreisVector(aktRezNum.getText()
                                                                              .substring(0, 2),
                    tarifneu);
            int count = getNodeCount();
            String xpos = "";
            AbrFall abr;
            String preis;
            boolean alterpreis = false;
            for (int i = 0; i < count; i++) {
                abr = this.holeAbrFall(i);

                if (DatFunk.TageDifferenz(
                        (String) SystemConfig.hmArschgeigenModus.get("Stichtag" + Integer.toString(welcher)),
                        abr.datum) >= 0) {
                    // abr.alterpreis = "aktuell";
                    xpos = RezTools.getPosFromID(abr.preisid, preisgruppe, preisvec);
                    preis = RezTools.getPreisAktFromPos(xpos, Integer.toString(tarifneu), vecneu);
                    abr.preis = Double.valueOf(preis);
                    abr.tarifwechsel = true;
                    this.neueTarifgruppe = welcher;
                    this.mitTarifWechsel = true;
                    abr.tarifkennzeichen = SystemPreislisten.hmPreisBereich.get(aktDisziplin)
                                                                           .get(tarifneu);
                    // System.out.println("Arschgeigencheck - Tarif: "+abr.preis);
                } else {
                    alterpreis = true;
                }
                if (alterpreis && this.mitTarifWechsel) {
                    final int xwelcher = welcher;
                    final int xtarifalt = tarifalt;
                    final int xtarifneu = tarifneu;
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            String meldung = "<html><b>Achtung es hat ein Wechsel der Tarifgruppe statt gefunden</b><br><br>"
                                    + "Stichtag für den Wechsel ist der <b><font color=#FF0000>"
                                    + SystemConfig.hmArschgeigenModus.get("Stichtag" + Integer.toString(xwelcher))
                                    + "</font></b><br><br>" + "alte Tarifgruppe = <b><font color=#FF0000>"
                                    + SystemPreislisten.hmPreisGruppen.get(aktDisziplin)
                                                                      .get(xtarifalt)
                                    + "</font></b><br>" + "neue Tarifgruppe = <b><font color=#FF0000>"
                                    + SystemPreislisten.hmPreisGruppen.get(aktDisziplin)
                                                                      .get(xtarifneu)
                                    + "</font></b><br><br>";
                            JOptionPane.showMessageDialog(getInstance(), meldung);
                        }
                    });
                }
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

    }

    private AbrechnungRezept getInstance() {
        return this;
    }

    private String getDatumErsterTag() {
        return this.holeAbrFall(0).datum;
    }

    private String getDatumLetzterTag() {
        return this.holeAbrFall(getNodeCount() - 1).datum;
    }

    /***************************
     * Hausbesuchsgedönse
     ********************************/
    private void doHausbesuchKomplett() {

        int insgesamthb = anzahlhb;
        vec_hb = SqlInfo.holeFelder(
                "select heimbewohn,kilometer from pat5 where pat_intern='" + aktRezept.getPatIntern() + "' LIMIT 1");
        boolean vollepackung = aktRezept.getHbVoll();
        boolean heimbewohner = vec_hb.get(0)
                                     .get(0)
                                     .equals("T");
        double anzahlkm = 0.00;
        hbstrecke = -1;
        hbkmpos = "";

        int maxanzahl = root.getChildCount();
        try {
            anzahlkm = Double.parseDouble(vec_hb.get(0)
                                                .get(1)
                                                .replace(",", "."));
        } catch (NumberFormatException ex) {
            anzahlkm = Double.parseDouble("0.00");
        }

        /********* Jetzt geht's los ***********/
        ////// System.out.println("HB-Regeln = "+SystemConfig.vHBRegeln);
        if (maxanzahl < insgesamthb) {
            JOptionPane.showMessageDialog(null, "Achtung die Anzahl der Behandlungstage stimmt nicht mit der\n"
                    + "Angabe Anzahl Hausbesuche im Rezeptstamm überein");
        }
        ////// System.out.println("zugrundeLiegende Preisgruppe = "+preisgruppe);
        String tag = "";
        // int pos = 0;
        String position;
        boolean immerfrei = false;
        /*
         * String kilometerpos =
         * SystemConfig.vHBRegeln.get(Integer.parseInt(preisgruppe)-1).get(2).replace(
         * "x",disziplinIndex); String pauschalepos =
         * SystemConfig.vHBRegeln.get(Integer.parseInt(preisgruppe)-1).get(3).replace(
         * "x",disziplinIndex); String hauptziffer =
         * SystemConfig.vHBRegeln.get(Integer.parseInt(preisgruppe)-1).get(0).replace(
         * "x",disziplinIndex); String mehrereziffer =
         * SystemConfig.vHBRegeln.get(Integer.parseInt(preisgruppe)-1).get(1).replace(
         * "x",disziplinIndex);
         */
        String kilometerpos = SystemPreislisten.hmHBRegeln.get(aktDisziplin)
                                                          .get(Integer.parseInt(preisgruppe) - 1)
                                                          .get(2);
        String pauschalepos = SystemPreislisten.hmHBRegeln.get(aktDisziplin)
                                                          .get(Integer.parseInt(preisgruppe) - 1)
                                                          .get(3);
        String hauptziffer = SystemPreislisten.hmHBRegeln.get(aktDisziplin)
                                                         .get(Integer.parseInt(preisgruppe) - 1)
                                                         .get(0);
        String mehrereziffer = SystemPreislisten.hmHBRegeln.get(aktDisziplin)
                                                           .get(Integer.parseInt(preisgruppe) - 1)
                                                           .get(1);

        //// System.out.println("Anzahl Kilometer = "+anzahlkm);
        // for(int i = 0; i < maxanzahl;i++){
        for (int i = 0; i < insgesamthb; i++) {

            JXTTreeTableNode node = (JXTTreeTableNode) root.getChildAt(i);
            tag = node.abr.datum;
            ////// System.out.println("Behandlungstag "+tag+" HB hinzufügen");
            // Zunächst die Position HB-Einzeln oder HB-Mit hinzufügen
            if (vollepackung) {
                position = hauptziffer;
            } else {
                position = mehrereziffer;
            }
            if (heimbewohner) {
                immerfrei = SystemPreislisten.hmHBRegeln.get(aktDisziplin)
                                                        .get(Integer.parseInt(preisgruppe) - 1)
                                                        .get(4)
                                                        .equals("0");
            }
            //// System.out.println("Es ist ein Heimbewohner ="+heimbewohner+" HB-Ziffern
            //// sind immer frei="+immerfrei);
            // Die Hauptziffer anhängen
            // Parameter sind 1.Tag Basis Datum HM-Postion Anzahl immerfrei
            abrfallAnhaengen(i + 1, node, tag, position, Double.parseDouble("1.00"), immerfrei);

            // Jetzt untersuchen ob Wegegeld angehängt werden kann! (nur möglich wenn
            // hauptziffer abgerechnet wird.
            if ((vollepackung) && (this.anzahlposhb > 1)) {
                // Kilometer im Patientenstamm angegeben und es existiert eine Kilometerziffer
                boolean kilometerbesser = true;
                if ((!kilometerpos.trim()
                                  .equals(""))
                        && (!pauschalepos.trim()
                                         .equals(""))) {
                    kilometerbesser = RezTools.kmBesserAlsPauschale(pauschalepos, kilometerpos,
                            Double.valueOf(anzahlkm), Integer.parseInt(preisgruppe), aktDisziplin);
                }
                if (kilometerbesser && (!kilometerpos.trim()
                                                     .equals(""))) {
                    // if(anzahlkm > 7 && (!kilometerpos.trim().equals(""))){
                    if (anzahlkm <= 0) {
                        JOptionPane.showMessageDialog(null,
                                "Achtung Sie rechnen HB-Kilometer ab dem Pat wurden aber 0 km zugewiesen,\ndieses Rezept bitte aus der Abrechnung nehmen bis im Pat.Stamm die Kilometeranzahl korrigiert wurde");
                    }
                    abrfallAnhaengen(i + 1, node, tag, kilometerpos, anzahlkm, immerfrei);
                    hbstrecke = (int) Math.abs(anzahlkm);
                    hbkmpos = String.valueOf(kilometerpos);

                    // Kilometer im Patientenstamm auf 0 gesetzt und es existiert eine
                    // Pauschalenziffer
                } else if ((!kilometerbesser) && (!pauschalepos.trim()
                                                               .equals(""))) {
                    // }else if(anzahlkm <= 7 && (!pauschalepos.trim().equals(""))){
                    abrfallAnhaengen(i + 1, node, tag, pauschalepos, Double.parseDouble("1.00"), immerfrei);
                } else if ((anzahlkm <= 0) && (!kilometerpos.trim()
                                                            .equals(""))
                        && (pauschalepos.trim()
                                        .equals(""))) {
                    // }else if(anzahlkm <= 7 && (!kilometerpos.trim().equals("")) &&
                    // (!pauschalepos.trim().equals(""))){
                    JOptionPane.showMessageDialog(null, "Diese Kasse kann nur mit Kilometer abgerechnet werden.\n"
                            + "Die Angaben im Pateientenstamm lauten auf 0-Kilometer, bitte korrigieren");
                    abrfallAnhaengen(i + 1, node, tag, kilometerpos, Double.parseDouble("1.00"), immerfrei);
                }

            }
        }

    }

    private void doHausbesuchEinzeln(JXTTreeTableNode node, int basisindex) {
        vec_hb = SqlInfo.holeFelder(
                "select heimbewohn,kilometer from pat5 where pat_intern='" + aktRezept.getPatIntern() + "' LIMIT 1");
        int insgesamthb = anzahlhb;
        boolean vollepackung = aktRezept.getHbVoll();
        boolean heimbewohner = vec_hb.get(0)
                                     .get(0)
                                     .equals("T");
        double anzahlkm = 0.00;
        int maxanzahl = root.getChildCount();

        anzahlkm = Double.parseDouble(vec_hb.get(0)
                                            .get(1)
                                            .replace(",", "."));
        hbstrecke = -1;
        hbkmpos = "";
        /********* Jetzt geht's los ***********/
        ////// System.out.println("HB-Regeln = "+SystemConfig.vHBRegeln);
        if (maxanzahl < insgesamthb) {
            JOptionPane.showMessageDialog(null, "Achtung die Anzahl der Behandlungstage stimmt nicht mit der\n"
                    + "Angabe Anzahl Hausbesuche im Rezeptstamm überein");
        }
        ////// System.out.println("zugrundeLiegende Preisgruppe = "+preisgruppe);
        String tag = "";
        // int pos = 0;
        String position;
        boolean immerfrei = false;
        /*
         * String kilometerpos =
         * SystemConfig.vHBRegeln.get(Integer.parseInt(preisgruppe)-1).get(2).replace(
         * "x",disziplinIndex); String pauschalepos =
         * SystemConfig.vHBRegeln.get(Integer.parseInt(preisgruppe)-1).get(3).replace(
         * "x",disziplinIndex); String hauptziffer =
         * SystemConfig.vHBRegeln.get(Integer.parseInt(preisgruppe)-1).get(0).replace(
         * "x",disziplinIndex); String mehrereziffer =
         * SystemConfig.vHBRegeln.get(Integer.parseInt(preisgruppe)-1).get(1).replace(
         * "x",disziplinIndex);
         */
        String kilometerpos = SystemPreislisten.hmHBRegeln.get(aktDisziplin)
                                                          .get(Integer.parseInt(preisgruppe) - 1)
                                                          .get(2);
        String pauschalepos = SystemPreislisten.hmHBRegeln.get(aktDisziplin)
                                                          .get(Integer.parseInt(preisgruppe) - 1)
                                                          .get(3);
        String hauptziffer = SystemPreislisten.hmHBRegeln.get(aktDisziplin)
                                                         .get(Integer.parseInt(preisgruppe) - 1)
                                                         .get(0);
        String mehrereziffer = SystemPreislisten.hmHBRegeln.get(aktDisziplin)
                                                           .get(Integer.parseInt(preisgruppe) - 1)
                                                           .get(1);

        // System.out.println("Anzahl Kilometer = "+anzahlkm);

        // JXTTreeTableNode node = (JXTTreeTableNode)root.getChildAt(i);
        tag = node.abr.datum;
        //// System.out.println("Behandlungstag "+tag+" HB hinzufügen");
        // Zunächst die Position HB-Einzeln oder HB-Mit hinzufügen
        if (vollepackung) {
            position = hauptziffer;
        } else {
            position = mehrereziffer;
        }
        if (heimbewohner) {
            // immerfrei =
            // SystemConfig.vHBRegeln.get(Integer.parseInt(preisgruppe)-1).get(4).equals("0");
            immerfrei = SystemPreislisten.hmHBRegeln.get(aktDisziplin)
                                                    .get(Integer.parseInt(preisgruppe) - 1)
                                                    .get(4)
                                                    .equals("0");
        }
        //// System.out.println("Es ist ein Heimbewohner ="+heimbewohner+" HB-Ziffern
        //// sind immer frei="+immerfrei);
        // Die Hauptziffer anhängen
        // Parameter sind 1.Tag Basis Datum HM-Postion Anzahl immerfrei
        abrfallAnhaengen(basisindex + 1, node, tag, position, Double.parseDouble("1.00"), immerfrei);

        // Jetzt untersuchen ob Wegegeld angehängt werden kann! (nur möglich wenn
        // hauptziffer abgerechnet wird.
        if ((vollepackung) && (this.anzahlposhb > 1)) {
            // Kilometer im Patientenstamm angegeben und es existiert eine Kilometerziffer
            boolean kilometerbesser = true;
            if ((!kilometerpos.trim()
                              .equals(""))
                    && (!pauschalepos.trim()
                                     .equals(""))) {
                kilometerbesser = RezTools.kmBesserAlsPauschale(pauschalepos, kilometerpos, Double.valueOf(anzahlkm),
                        Integer.parseInt(preisgruppe), aktDisziplin);
            }
            if (kilometerbesser && (!kilometerpos.trim()
                                                 .equals(""))) {
                // if(anzahlkm > 7 && (!kilometerpos.trim().equals(""))){
                abrfallAnhaengen(basisindex + 1, node, tag, kilometerpos, anzahlkm, immerfrei);
                hbstrecke = (int) Math.abs(anzahlkm);
                hbkmpos = String.valueOf(kilometerpos);

                // System.out.println(hbstrecke+" / "+hbkmpos);
                // Kilometer im Patientenstamm auf 0 gesetzt und es existiert eine
                // Pauschalenziffer
            } else if ((!kilometerbesser) && (!pauschalepos.trim()
                                                           .equals(""))) {
                // }else if(anzahlkm <= 7 && (!pauschalepos.trim().equals(""))){
                abrfallAnhaengen(basisindex + 1, node, tag, pauschalepos, Double.parseDouble("1.00"), immerfrei);
            } else if ((anzahlkm <= 0) && (!kilometerpos.trim()
                                                        .equals(""))
                    && (pauschalepos.trim()
                                    .equals(""))) {
                // }else if(anzahlkm <= 7 && (!kilometerpos.trim().equals("")) &&
                // (!pauschalepos.trim().equals(""))){
                JOptionPane.showMessageDialog(null, "Diese Kasse kann nur mit Kilometer abgerechnet werden.\n"
                        + "Die Angaben im Pateientenstamm lauten auf 0-Kilometer, bitte korrigieren");
                abrfallAnhaengen(basisindex + 1, node, tag, kilometerpos, Double.parseDouble("1.00"), immerfrei);
            }
        }
    }

    /***************************
     * Hausbesuchsgedönse
     ********************************/
    private void doPositionenErmitteln() {
        vec_poskuerzel.clear();
        vec_posanzahl.clear();
        vec_pospos.clear();
        int lang = 0;
        if ((lang = vec_tabelle.size()) <= 0) {
            return;
        }
        // System.out.println(vec_tabelle);
        for (int i = 0; i < lang; i++) {
            if (!vec_poskuerzel.contains((vec_tabelle.get(i)
                                                     .get(1)))) {
                vec_poskuerzel.add(vec_tabelle.get(i)
                                              .get(1)
                                              .toString());
                vec_pospos.add(RezTools.getPosFromID(vec_tabelle.get(i)
                                                                .get(9)
                                                                .toString(),
                        preisgruppe, preisvec));
                vec_posanzahl.add(1);
            } else {
                int pos = vec_poskuerzel.indexOf(vec_tabelle.get(i)
                                                            .get(1));
                int anzahl = vec_posanzahl.get(pos);
                if (anzahl <= 0) {
                    JOptionPane.showMessageDialog(null,
                            "Achtung Sie rechnen eine Position mit Anzahl 0 ab,\ndieses Rezept bitte aus der Abrechnung nehmen bis das Problem behoben wurde");
                }
                vec_posanzahl.set(pos, anzahl + 1);
            }
        }
        /*
         * System.out.
         * println("****************nachfolgende Positionen wurden ermittelt****************"
         * ); System.out.println(vec_poskuerzel); System.out.println(vec_posanzahl);
         * System.out.println(vec_pospos);
         */
    }

    private void doTreeRezeptWertermitteln() {
        int lang = 0;
        rezeptWert = 0.00;
        zuzahlungWert = 0.00;

        BigDecimal dummy1 = BigDecimal.valueOf(Double.parseDouble("0.00"));
        BigDecimal dummy2 = BigDecimal.valueOf(Double.parseDouble("0.00"));

        BigDecimal ddummy1 = BigDecimal.valueOf(Double.parseDouble("0.00"));
        BigDecimal ddummy2 = BigDecimal.valueOf(Double.parseDouble("0.00"));

        if ((lang = this.getNodeCount()) <= 0) {
            return;
        }
        Object ob1 = null;
        Object ob2 = null;
        Object ob3 = null;
        boolean zuzahl = false;
        for (int i = 0; i < lang; i++) {
            AbrFall abr = this.holeAbrFall(i);
            ob1 = abr.anzahl;
            ob2 = abr.preis;

            zuzahl = abr.zuzahlung;
            ddummy1 = dummy1.add((BigDecimal.valueOf((Double) ob1)
                                            .multiply(BigDecimal.valueOf((Double) ob2))));
            if (zuzahl && (!abr.niezuzahl)) {
                // ob3 = this.rechneRezGebFromDouble(abr.preis);
                ob3 = (BigDecimal.valueOf((Double) ob1)
                                 .multiply(BigDecimal.valueOf(this.rechneRezGebFromDouble(abr.preis))));
                abr.rezgeb = ((BigDecimal) ob3).doubleValue();
                ddummy2 = dummy2.add(((BigDecimal) ob3));
            } else {

                abr.zuzahlung = false;
                abr.rezgeb = 0.00;
            }
            dummy1 = ddummy1;
            dummy2 = ddummy2;
        }
        if (mitPauschale) {
            ddummy2 = dummy2.add(BigDecimal.valueOf(Double.parseDouble("10.00")));
        }
        rezeptWert = ddummy1.doubleValue();
        zuzahlungWert = ddummy2.doubleValue();
        ////// System.out.println("Rezeptwert Gesamt = "+dfx.format(rezeptWert));
        ////// System.out.println("Zuzahlung Gesamt = "+dfx.format(zuzahlungWert));

    }

    private void doTreeFreiAb(int von, int bis, boolean pflichtig) {
        ////// System.out.println("Starte bei "+von);
        ////// System.out.println("Ende bei "+bis);
        ////// System.out.println("Setze Zuzahlungs-Flag="+pflichtig);
        for (int i = von; i < bis; i++) {
            AbrFall abr = holeAbrFall(i);
            if ((!pflichtig) || (abr.niezuzahl)) {
                abr.zuzahlung = pflichtig;
                abr.rezgeb = Double.parseDouble("0.00");
            } else {
                abr.zuzahlung = pflichtig;
                abr.rezgeb = rechneRezGebFromDouble(abr.preis);
            }
        }
    }

    /*******************************
     *
     */
    /*******************************/

    private void constructTagVector(String datum, String[] behandlungen, int anzahlbehandlungen, int anzahlhb, int tag,
            boolean toomuch) {
        try {
            String[] abrfall = new String[anzahlbehandlungen];
            String[] id = new String[anzahlbehandlungen];
            Object[] abrObject = { "", "" };
            if (behandlungen != null) {
                boolean posgefunden = true;
                String posnr = "";
                //// System.out.println("Anzahl behandlungen = "+anzahlbehandlungen);
                for (int i = 0; i < anzahlbehandlungen; i++) {
                    // abrfall[i] = RezTools.getKurzformFromPos(behandlungen[i].trim(), preisgruppe,
                    // preisvec);
                    //// System.out.println("Behandlung = "+behandlungen[i]);
                    abrObject = RezTools.getKurzformUndIDFromPos(behandlungen[i].trim(), preisgruppe, preisvec);
                    if (abrObject[0].toString()
                                    .equals("")) {
                        posgefunden = false;
                        posnr = behandlungen[i].trim();
                    }
                    abrfall[i] = abrObject[0].toString();
                    id[i] = abrObject[1].toString();
                    //// System.out.println(""+i+". Behandlung aus Terminblatt = "+abrfall[i]);
                    //// System.out.println(""+i+". id = "+id[i]);
                }
                if (!posgefunden) {
                    JOptionPane.showMessageDialog(null, "<html>Achtung eine der Rezeptpositionen z.B. <b>" + posnr
                            + "</b> konnte nicht gefunden werden.<br>Wurde evtl. eine Preislistenposition gelöscht oder verändert?</html>");
                }
            } else {

                for (int i = 0; i < anzahlbehandlungen; i++) {
                    abrfall[i] = RezTools.getKurzformFromID( // alt (weg, wenn neu ok)
                            aktRezept.getVecVec_rez()
                                     .get(0)
                                     .get(i + 8),
                            preisvec)
                                         .toString();
                    id[i] = aktRezept.getVecVec_rez()
                                     .get(0)
                                     .get(i + 8)
                                     .toString();

                    id[i] = aktRezept.getArtDBehandl(i + 1); // neu
                    abrfall[i] = RezTools.getKurzformFromID(id[i], preisvec);

                    //// System.out.println(""+i+". Behandlung aus RezeptTabelle = "+abrfall[i]);
                }
            }
            // Das ist ein Riesenscheiß
            //// System.out.println("anzahl Behandlungen
            // =*************************"+anzahlbehandlungen);
            int posanzahl = 0;
            int posanzahlbegleitend;
            for (int i = 0; i < anzahlbehandlungen; i++) {
                if (!abrfall[i].trim()
                               .equals("")) {
                    vecdummy.clear();
                    // Hier testen ob Anzahlen unterschiedlich sind
                    // posanzahl = Integer.parseInt(aktRezept.getVecVec_rez().get(0).get(3));
                    // //Original OK
                    posanzahl = aktRezept.getAnzBeh(1);
                    try {
                        // posanzahlbegleitend =
                        // Integer.parseInt(aktRezept.getVecVec_rez().get(0).get(3+i)); //Original OK
                        posanzahlbegleitend = aktRezept.getAnzBeh(1 + i);
                    } catch (NumberFormatException ex) {
                        posanzahlbegleitend = posanzahl;
                    }
                    if ((i + 1) <= posanzahl || (posanzahl == 1 && posanzahlbegleitend == 1)) {
                        // if((tag+1) <= posanzahl){ //Original
                        vecdummy.add(datum);
                        vecdummy.add(abrfall[i]);
                        /*
                         * //System.out.println("Abrechnungsfall = "+abrfall[i]);
                         * //System.out.println("******************");
                         */
                        vecdummy.add(Double.valueOf("1.00"));
                        // Preisuntersuchen ob alt oder neu....

                        // .....
                        vecdummy.add(Double.valueOf(RezTools.getPreisAktFromID(id[i], preisgruppe, preisvec)
                                                            .replace(",", ".")));
                        // untersuchen ob befreit
                        // .....
                        vecdummy.add(Boolean.valueOf(true));

                        vecdummy.add(Double.valueOf(rechneRezGeb(vecdummy.get(3)
                                                                         .toString()).replace(",", ".")));

                        vecdummy.add("");
                        vecdummy.add("aktuell");
                        vecdummy.add(DatFunk.sDatInSQL(datum));
                        vecdummy.add(id[i]);
                        vecdummy.add(Boolean.valueOf(false));

                        vec_tabelle.add((Vector<Object>) vecdummy.clone());
                        //// System.out.println(vecdummy);
                    } else if (toomuch) {
                        toomuch = true;
                        // System.out.println("***tomuch = true****");
                        vecdummy.add(datum);
                        vecdummy.add(abrfall[i]);
                        vecdummy.add(Double.valueOf("1.00"));
                        // Preisuntersuchen ob alt oder neu....

                        // .....
                        vecdummy.add(Double.valueOf(RezTools.getPreisAktFromID(id[i], preisgruppe, preisvec)
                                                            .replace(",", ".")));
                        // untersuchen ob befreit
                        // .....
                        vecdummy.add(Boolean.valueOf(true));

                        vecdummy.add(Double.valueOf(rechneRezGeb(vecdummy.get(3)
                                                                         .toString()).replace(",", ".")));

                        vecdummy.add("");
                        vecdummy.add("aktuell");
                        vecdummy.add(DatFunk.sDatInSQL(datum));
                        vecdummy.add(id[i]);
                        vecdummy.add(Boolean.valueOf(false));

                        vec_tabelle.add((Vector<Object>) vecdummy.clone());
                        //// System.out.println("in toomuch "+vecdummy);

                    }
                }
            }
            if (toomuch) {
                // JOptionPane.showMessageDialog(null,"Achtung - Sie rechnen mehr
                // Behandlungstage ab als im Rezept angegeben wurde!");
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

    }

    private AbrFall holeAbrFall(int zeile) {
        AbrFall abrFall = null;
        int rootAnzahl;
        int kindAnzahl;
        JXTTreeTableNode rootNode;
        JXTTreeTableNode childNode;
        rootAnzahl = root.getChildCount();
        if (rootAnzahl <= 0) {
            return abrFall;
        }
        int geprueft = 0;
        for (int i = 0; i < rootAnzahl; i++) {

            rootNode = (JXTTreeTableNode) root.getChildAt(i);
            ////// System.out.println("Anzahl Root-Knoten = "+rootAnzahl);
            if (rootNode.isLeaf()) {
                if (geprueft == zeile) {
                    return rootNode.abr;
                } else {
                    geprueft++;
                    continue;
                }

            } else if ((!rootNode.isLeaf()) && ((geprueft == zeile))) {
                return rootNode.abr;
            } else if (!rootNode.isLeaf()) {
                kindAnzahl = rootNode.getChildCount();
                ////// System.out.println("Anzahl Kind-Knoten = "+kindAnzahl);
                geprueft++;
                for (int i2 = 0; i2 < kindAnzahl; i2++) {

                    if (geprueft == zeile) {
                        childNode = (JXTTreeTableNode) rootNode.getChildAt(i2);
                        ////// System.out.println("Zeile gefunden geprueft wurden "+geprueft);
                        return childNode.abr;
                    } else {
                        childNode = (JXTTreeTableNode) rootNode.getChildAt(i2);
                        ////// System.out.println("Zeile überpüft="+geprueft+" - "+childNode.abr.datum+"
                        ////// - "+childNode.abr.bezeichnung);
                        geprueft++;
                    }

                }
            } else {
                ////// System.out.println("Keine Bedingung trifft zu="+geprueft);
                geprueft++;
            }
        }

        return abrFall;

    }

    private JXTTreeTableNode holeNode(int zeile) {

        JXTTreeTableNode node = null;
        int rootAnzahl;
        int kindAnzahl;
        JXTTreeTableNode rootNode;
        JXTTreeTableNode childNode;
        rootAnzahl = root.getChildCount();
        if (rootAnzahl <= 0) {
            return node;
        }
        int geprueft = 0;
        for (int i = 0; i < rootAnzahl; i++) {

            rootNode = (JXTTreeTableNode) root.getChildAt(i);
            ////// System.out.println("Anzahl Root-Knoten = "+rootAnzahl);
            if (rootNode.isLeaf()) {
                if (geprueft == zeile) {
                    return rootNode;
                } else {
                    geprueft++;
                    continue;
                }

            } else if ((!rootNode.isLeaf()) && ((geprueft == zeile))) {
                return rootNode;
            } else if (!rootNode.isLeaf()) {
                kindAnzahl = rootNode.getChildCount();
                ////// System.out.println("Anzahl Kind-Knoten = "+kindAnzahl);
                geprueft++;
                for (int i2 = 0; i2 < kindAnzahl; i2++) {

                    if (geprueft == zeile) {
                        childNode = (JXTTreeTableNode) rootNode.getChildAt(i2);
                        ////// System.out.println("Zeile gefunden geprueft wurden "+geprueft);
                        return childNode;
                    } else {
                        childNode = (JXTTreeTableNode) rootNode.getChildAt(i2);
                        ////// System.out.println("Zeile überpüft="+geprueft+" - "+childNode.abr.datum+"
                        ////// - "+childNode.abr.bezeichnung);
                        geprueft++;
                    }

                }
            } else {
                ////// System.out.println("Keine Bedingung trifft zu="+geprueft);
                geprueft++;
            }
        }

        return node;

    }

    private int getNodeCount() {
        int ret = 0;
        int rootAnzahl;
        int kindAnzahl;
        JXTTreeTableNode rootNode;
        // JXTTreeTableNode childNode;
        rootAnzahl = root.getChildCount();
        if (rootAnzahl <= 0) {
            return 0;
        }
        // int geprueft = 0;
        for (int i = 0; i < rootAnzahl; i++) {
            rootNode = (JXTTreeTableNode) root.getChildAt(i);
            ret += 1;
            if ((kindAnzahl = rootNode.getChildCount()) > 0) {
                ret += kindAnzahl;
            }
        }
        return ret;
    }

    /**************************/
    private String rechneRezGeb(String preis) {
        BigDecimal bi_rezgeb = BigDecimal.valueOf(Double.parseDouble(preis.replace(",", ".")));
        bi_rezgeb = bi_rezgeb.divide(BigDecimal.valueOf(Double.parseDouble("10.000")));
        bi_rezgeb = bi_rezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
        return dfx.format(bi_rezgeb)
                  .replace(".", ",");
    }

    private Double rechneRezGebFromDouble(Double preis) {
        BigDecimal bi_rezgeb = BigDecimal.valueOf(preis);
        bi_rezgeb = bi_rezgeb.divide(BigDecimal.valueOf(Double.parseDouble("10.000")));
        bi_rezgeb = bi_rezgeb.setScale(2, BigDecimal.ROUND_HALF_UP);
        return bi_rezgeb.doubleValue();
    }

    /*****************************************************************************************/

    private void doFuelleTreeTable() {
        AbrFall abr;
        if (vec_tabelle.size() <= 0) {
            return;
        }
        String testdatum = "";
        JXTTreeTableNode knoten = null;
        int tag = 0;
        for (int i = 0; i < vec_tabelle.size(); i++) {
            abr = new AbrFall(Integer.toString(tag) + ".Tag", (String) vec_tabelle.get(i)
                                                                                  .get(0),
                    (String) vec_tabelle.get(i)
                                        .get(1),
                    (Double) vec_tabelle.get(i)
                                        .get(2),
                    (Double) vec_tabelle.get(i)
                                        .get(3),
                    (Boolean) vec_tabelle.get(i)
                                         .get(4),
                    (Double) vec_tabelle.get(i)
                                        .get(5),
                    (String) vec_tabelle.get(i)
                                        .get(6),
                    (String) vec_tabelle.get(i)
                                        .get(7),
                    (String) vec_tabelle.get(i)
                                        .get(8),
                    (String) vec_tabelle.get(i)
                                        .get(9),
                    (Boolean) vec_tabelle.get(i)
                                         .get(10));
            if (!testdatum.trim()
                          .equals(abr.datum.trim())) {
                tag++;
                abr.unterbrechung = vectage.get(tag - 1)
                                           .get(2);
                abr.titel = Integer.toString(tag) + ".Tag";
                knoten = new JXTTreeTableNode(abr.datum, abr, true);
                demoTreeTableModel.insertNodeInto(knoten, root, root.getChildCount());
                testdatum = String.valueOf(abr.datum);
                ////// System.out.println(testdatum);
                continue;

            } else {
                abr.unterbrechung = vectage.get(tag - 1)
                                           .get(2);
                foo = new JXTTreeTableNode("", abr, true);
                demoTreeTableModel.insertNodeInto(foo, knoten, knoten.getChildCount());
                testdatum = String.valueOf(abr.datum);
                ////// System.out.println("In einzelner Funktion "+testdatum);
                continue;
            }
        }
    }

    class AbrFall {
        public String titel;
        public String datum;
        public String bezeichnung;
        public double anzahl = 0.0;
        public double preis = 0.00;
        public boolean zuzahlung = true;
        public double rezgeb = 0.00;
        public String unterbrechung = "";
        public String alterpreis = "";
        public String sqldatum = "";
        public String preisid = "";
        public boolean niezuzahl = false;
        public boolean tarifwechsel = false;
        public String tarifkennzeichen = "";

        // SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        public AbrFall(String titel, String datum, String bezeichnung, Double anzahl, Double preis, boolean zuzahlung,
                double rezgeb, String unterbrechung, String alterpreis, String sqldatum, String preisid,
                boolean niezuzahl) {
            this.titel = titel;
            this.datum = datum;
            this.bezeichnung = bezeichnung;
            this.anzahl = anzahl;
            this.preis = preis;
            this.zuzahlung = zuzahlung;
            this.rezgeb = rezgeb;
            this.unterbrechung = unterbrechung;
            this.sqldatum = sqldatum;
            this.alterpreis = alterpreis;
            this.preisid = preisid;
            this.niezuzahl = niezuzahl;
            this.tarifwechsel = false;
            this.tarifkennzeichen = "";
        }
    }

    /**
     * @param rez
     */
    public void parseHTMLuniq(String rez) {
        while (inParseHtml) {
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        parseHTML(rez.trim());
    }

    private void parseHTML(String rez_nr) {
        inParseHtml = true;
        try {
            if (rez_nr == null) {
                inParseHtml = false;
                return;
            }

            // String dummy="";
            // String text =
            buf1.setLength(0);
            buf1.trimToSize();

            buf1.append("<html><head>");
            buf1.append("<STYLE TYPE=\"text/css\">");
            buf1.append("<!--");
            buf1.append("A{text-decoration:none;background-color:transparent;border:none}");
            buf1.append("TD{font-family: Arial; font-size: 12pt; padding-left:5px;padding-right:30px}");
            buf1.append(".spalte1{color:#0000FF;}");
            buf1.append(".spalte2{color:#333333;}");
            buf1.append(".spalte2{color:#333333;}");
            buf1.append("--->");
            buf1.append("</STYLE>");
            buf1.append("</head>");
            buf1.append("<div style=margin-left:30px;>");
            buf1.append("<font face=\"Tahoma\"><style=margin-left=30px;>");
            buf1.append("<br>");
            buf1.append("<table>");
            /***** Rezept ****/
            /*******/
            buf1.append("<tr>");
            buf1.append("<th rowspan=\"4\"><a href=\"http://rezedit.de\"><img src='file:///"
                    + Path.Instance.getProghome() + "icons/Rezept.png' border=0></a></th>");
            buf1.append("<td class=\"spalte1\" align=\"right\">");
            buf1.append("Ausstellungsdatum");
            buf1.append("</td><td class=\"spalte2\" align=\"left\">");
            buf1.append(DatFunk.sDatInDeutsch(aktRezept.getRezeptDatum()));
            buf1.append("</td>");
            buf1.append("</tr>");
            /*******/
            buf1.append("<tr>");
            buf1.append("<td class=\"spalte1\" align=\"right\">");
            buf1.append("Verordnungsart");
            buf1.append("</td><td class=\"spalte2\" align=\"left\">");
//            buf1.append(voArt[Integer.parseInt(aktRezept.getVec_rez().get(0).get(27))]);
            buf1.append(voArt[aktRezept.getRezArt()]);
            buf1.append("</td>");
            buf1.append("</tr>");
            /*******/
            buf1.append("<tr>");
            buf1.append("<td class=\"spalte1\" align=\"right\">");
            buf1.append("Indikationsschlüssel / ICD-10");
            buf1.append("</td><td class=\"spalte2\" align=\"left\">");
            buf1.append((aktRezept.getIndiSchluessel()
                                  .startsWith("kein Indi")
                                          ? "<b><font color=#FF0000>" + aktRezept.getIndiSchluessel() + "</font></b>"
                                          : aktRezept.getIndiSchluessel())
                    + (aktRezept.getICD10()
                                .equals("") ? " / <b>n.a.</b>" : " / <b>" + aktRezept.getICD10() + "</b>")
                    + (aktRezept.getICD10_2()
                                .equals("") ? " " : "<b>, " + aktRezept.getICD10_2() + "</b>"));
            buf1.append("</td>");
            buf1.append("</tr>");
            /*******/
            boolean hb = (aktRezept.getHausbesuch());
            buf1.append("<tr>");
            buf1.append("<td class=\"spalte1\" align=\"right\">");
            buf1.append((hb ? "<b><font color=#FF0000>Hausbesuch</font></b>" : "Hausbesuch"));
            buf1.append("</td><td class=\"spalte2\" align=\"left\">");
            buf1.append((hb ? "<b><font color=#FF0000>JA</font></b>" : "NEIN"));
            buf1.append("</td>");
            buf1.append("</tr>");
            /***********************************************/
            buf1.append(getHTMLPositionen());
            /***********************************************/
            buf1.append("<tr>");
            buf1.append("<td>&nbsp;");
            buf1.append("</td>");
            buf1.append("</tr>");
            /******** Patient ********/
            buf1.append("<tr>");
            buf1.append("<th rowspan=\"5\" valign=\"top\"><a href=\"http://patedit.de\"><img src='file:///"
                    + Path.Instance.getProghome()
                    + "icons/kontact_contacts.png' width=52 height=52 border=0></a></th>");
            buf1.append("<td class=\"spalte1\" align=\"right\">");
            buf1.append("Patient");
            buf1.append("</td><td class=\"spalte2\" align=\"left\">");
            buf1.append(StringTools.EGross(vec_pat.get(0)
                                                  .get(0))
                    + ", ");
            buf1.append(StringTools.EGross(vec_pat.get(0)
                                                  .get(1))
                    + ", geb.am " + DatFunk.sDatInDeutsch(vec_pat.get(0)
                                                                 .get(2)));
            buf1.append("</td>");
            buf1.append("</tr>");
            /*******/
            buf1.append("<tr>");
            buf1.append("<td class=\"spalte1\" align=\"right\">");
            buf1.append("Adresse");
            buf1.append("</td><td class=\"spalte2\" align=\"left\">");
            buf1.append(StringTools.EGross(vec_pat.get(0)
                                                  .get(3))
                    + ", ");
            buf1.append(vec_pat.get(0)
                               .get(4)
                    + " ");
            buf1.append(StringTools.EGross(vec_pat.get(0)
                                                  .get(5)));
            buf1.append("</td>");
            buf1.append("</tr>");
            /*******/
            buf1.append("<tr>");
            buf1.append("<td class=\"spalte1\" align=\"right\">");
            buf1.append("Versicherten-Status");
            buf1.append("</td><td class=\"spalte2\" align=\"left\">");
            buf1.append(vec_pat.get(0)
                               .get(7));
            buf1.append("</td>");
            buf1.append("</tr>");
            /*******/
            buf1.append("<tr>");
            buf1.append("<td class=\"spalte1\" align=\"right\">");
            buf1.append("Mitgliedsnummer");
            buf1.append("</td><td class=\"spalte2\" align=\"left\">");
            buf1.append(vec_pat.get(0)
                               .get(6));
            buf1.append("</td>");
            buf1.append("</tr>");
            /*******/
            buf1.append("<tr>");
            buf1.append("<td class=\"spalte1\" align=\"right\">");
            buf1.append("Zuzahlungs-Status");
            buf1.append("</td><td class=\"spalte2\" id=\"zzpflicht\" align=\"left\">");
            buf1.append((zuZahlungsIndex.equals("Zuzahlungspflichtig") ? "" : "<b><font color=#FF0000>"));
            buf1.append(zuZahlungsIndex);
            buf1.append((zuZahlungsIndex.equals("Zuzahlungspflichtig") ? "" : "</font></b>"));
            buf1.append("</td>");
            buf1.append("</tr>");
            /*******/
            buf1.append("<tr>");
            buf1.append("<td>&nbsp;");
            buf1.append("</td>");
            buf1.append("</tr>");
            /******** Arzt ********/
            buf1.append("<tr>");
            buf1.append("<th rowspan=\"3\" valign=\"top\"><a href=\"http://arztedit.de\"><img src='file:///"
                    + Path.Instance.getProghome() + "icons/system-users.png' width=52 height=52 border=0></a></th>");
            buf1.append("<td class=\"spalte1\" align=\"right\">");
            buf1.append("verordnender Arzt");
            buf1.append("</td><td class=\"spalte2\" align=\"left\">");
            buf1.append(StringTools.EGross(vec_pat.get(0)
                                                  .get(13)));
            buf1.append("</td>");
            buf1.append("</tr>");
            /*******/
            buf1.append("<tr>");
            buf1.append("<td class=\"spalte1\" align=\"right\">");
            buf1.append("Betriebsstätte");
            buf1.append("</td><td class=\"spalte2\" align=\"left\">");
            buf1.append((vec_pat.get(0)
                                .get(14)
                                .trim()
                                .equals("") ? "999999999"
                                        : vec_pat.get(0)
                                                 .get(14)
                                                 .trim()));
            buf1.append("</td>");
            buf1.append("</tr>");
            /*******/
            buf1.append("<tr>");
            buf1.append("<td class=\"spalte1\" align=\"right\">");
            buf1.append("LANR");
            buf1.append("</td><td class=\"spalte2\" align=\"left\">");
            buf1.append((vec_pat.get(0)
                                .get(15)
                                .trim()
                                .equals("") ? "999999999"
                                        : vec_pat.get(0)
                                                 .get(15)
                                                 .trim()));
            buf1.append("</td>");
            buf1.append("</tr>");
            /*******/
            if (eltern.abrechnungsModus.equals(eltern.ABR_MODE_IV)) {
                buf1.append(getIVKassenAdresse());
                // this.htmlPane.add(this.cbtagedrucken);
            }
            // Double rezeptWert;
            // Double zuzahlungWert;
            buf1.append("</table>");
            buf1.append("</font>");
            buf1.append("</div>");
            buf1.append("</html>");
            this.htmlPane.setText(buf1.toString());
            ((JScrollPane) this.htmlPane.getParent()
                                        .getParent()).validate();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JViewport vp = ((JScrollPane) htmlPane.getParent()
                                                          .getParent()).getViewport();
                    vp.setViewPosition(new Point(0, 0));
                    ((JScrollPane) htmlPane.getParent()
                                           .getParent()).validate();
                }
            });
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler in der Aufbereitung des HTML-Textes\nFehlertext: " + ex.getLocalizedMessage());
        }
        inParseHtml = false;
    }

    private String getIVKassenAdresse() {
        buf3.setLength(0);
        buf3.trimToSize();
        /*******/
        buf1.append("<tr>");
        buf1.append("<td>&nbsp;");
        buf1.append("</td>");
        buf1.append("</tr>");

        buf3.append("<tr>");
        buf3.append("<th rowspan=\"4\" valign=\"top\"><a href=\"http://alternativekrankenkasse.de\"><img src='file:///"
                + Path.Instance.getProghome() + "icons/krankenkasse.png' width=52 height=52 border=0></a></th>");
        buf3.append("<td class=\"spalte1\" align=\"right\">");
        buf3.append("<b>Adresse für die</b>");
        buf3.append("</td><td class=\"spalte2\" align=\"left\">");
        buf3.append(eltern.hmAlternativeKasse.get("<Ivnam1>"));
        buf3.append("</td>");
        buf3.append("</tr>");

        buf3.append("<tr>");
        buf3.append("<td class=\"spalte1\" align=\"right\">");
        buf3.append("<b>IV-Rechnung</b>");
        buf3.append("</td><td class=\"spalte2\" align=\"left\">");
        buf3.append(eltern.hmAlternativeKasse.get("<Ivnam2>"));
        buf3.append("</td>");
        buf3.append("</tr>");

        buf3.append("<tr>");
        buf3.append("<td class=\"spalte1\" align=\"right\">");
        buf3.append("");
        buf3.append("</td><td class=\"spalte2\" align=\"left\">");
        buf3.append(eltern.hmAlternativeKasse.get("<Ivstrasse>"));
        buf3.append("</td>");
        buf3.append("</tr>");

        buf3.append("<tr>");
        buf3.append("<td class=\"spalte1\" align=\"right\">");
        buf3.append("");
        buf3.append("</td><td class=\"spalte2\" align=\"left\">");
        buf3.append(eltern.hmAlternativeKasse.get("<Ivplz>") + " " + eltern.hmAlternativeKasse.get("<Ivort>"));
        buf3.append("</td>");
        buf3.append("</tr>");

        buf3.append("<tr>");
        buf3.append("<th rowspan=\"4\" valign=\"top\"><a href=\"http://tagedrucken.de\"><img src='file:///"
                + Path.Instance.getProghome() + "icons/vcalendar.png' width=52 height=52 border=0></a></th>");
        buf3.append("<td class=\"spalte1\" align=\"right\">");
        buf3.append("<b>Behandlungstage</b>");
        buf3.append("</td><td class=\"spalte2\" align=\"left\">");
        buf3.append((this.tagedrucken ? "<b><font color=#FF0000>auf Rechnung drucken</font></b>" : "nicht ausdrucken"));
        buf3.append("</td>");
        buf3.append("</tr>");

        return buf3.toString();
    }

    private String getNoZuZahl(int variante, Vector<Vector<String>> vnozuz) {
        String sret = "";
        if (variante == 1) {
            sret = "<b><font color=#FF0000><a href=\"http://nozz.de\">" + dfx.format(zuzahlungWert)
                    + "<br>(nicht bar bezahlt und keine RGR erstellt!)</a></font></b>";
        } else if (variante == 2) {
            htmlposbuf.append("<b><font color=#FF0000>" + dfx.format(zuzahlungWert) + " " + vnozuz.get(0)
                                                                                                  .get(0)
                    + "<br>vom " + DatFunk.sDatInDeutsch(vnozuz.get(0)
                                                               .get(2))
                    + "<br>" + "noch offen: " + vnozuz.get(0)
                                                      .get(1)
                                                      .replace(".", ",")
                    + " EUR</font></b>");

        }
        return sret;
    }

    private String getHTMLPositionen() {

        htmlposbuf.setLength(0);
        htmlposbuf.trimToSize();

        for (int i = 0; i < vec_poskuerzel.size(); i++) {
            htmlposbuf.append("<tr><td>&nbsp;</td><td class=\"spalte1\" align=\"right\">");
            if (vec_pospos.get(i)
                          .trim()
                          .equals("")
                    || vec_poskuerzel.get(i)
                                     .equals("")) {
                htmlposbuf.append("<b><font color=#FF0000>Preislistenfehler!!!</font></b>");
                notready = true;
            } else {
                htmlposbuf.append(vec_pospos.get(i) + " - " + " <b>" + vec_poskuerzel.get(i) + "</b>");
            }
            htmlposbuf.append("</td><td class=\"spalte2\" align=\"left\">");

            htmlposbuf.append("<b>" + Integer.toString(vec_posanzahl.get(i)) + " x" + (vec_pospos.get(i)
                                                                                                 .equals(hbkmpos)
                                                                                                         ? " (" + Integer.toString(
                                                                                                                 hbstrecke)
                                                                                                                 + " km)"
                                                                                                         : "")
                    + "</b>");
            htmlposbuf.append("</td></tr>");
        }
        htmlposbuf.append("<tr>");
        htmlposbuf.append("<td>&nbsp;</td>");
        htmlposbuf.append("<td class=\"spalte1\" align=\"right\">");
        htmlposbuf.append("Rezeptwert");
        htmlposbuf.append("</td><td class=\"spalte2\" align=\"left\">");
        htmlposbuf.append("<b>" + dfx.format(rezeptWert) + "</b>");
        htmlposbuf.append("</td>");
        htmlposbuf.append("</tr>");
        htmlposbuf.append("<tr>");
        htmlposbuf.append("<td>&nbsp;</td>");
        htmlposbuf.append("<td class=\"spalte1\" align=\"right\">");
        htmlposbuf.append("Zuzahlung");
        htmlposbuf.append("</td><td class=\"spalte2\" align=\"left\">");
        // Hier muß überprüft werden ob Geld in der Kasse oder Rechnung geschrieben
        // wurde

        if (aktRezept.getGebuehrBezahlt()
                && (SqlInfo.holeEinzelFeld("select id from kasse where rez_nr ='" + aktRezept.getRezNb() + "' LIMIT 1")
                           .length() > 0)) {
            // Rezept auf bezahlt gesetzt und Geld in der Kasse
            htmlposbuf.append("<b>" + dfx.format(zuzahlungWert) + "</b>");
            kannAbhaken = true;
        } else if (zuzahlungWert <= 0) {
            htmlposbuf.append("<b>" + dfx.format(zuzahlungWert) + "</b>");
            kannAbhaken = true;
        } else {
            // ist eine Rechnung erstellt worden?
            Vector<Vector<String>> xrgaf = SqlInfo.holeFelder("select rnr,roffen,rdatum from rgaffaktura where reznr='"
                    + aktRezept.getRezNb() + "' and rnr like 'RGR-%' LIMIT 1");
            // System.out.println("select rnr,roffen,rdatum from rgaffaktura where
            // reznr='"+vec_rez.get(0).get(1)+"' and rnr like 'RGR-%' LIMIT 1");
            // System.out.println(xrgaf);
            if (xrgaf.size() <= 0) {
                // nein es wurde auch keine rechnung erstellt.
                htmlposbuf.append(getNoZuZahl(1, null));
                // Abrechnung trotz unbezahlter Rezeptgebühren und keine Rechnung erstellt?
                if (SystemConfig.hmAbrechnung.get("hmgkvfreigabeerzwingen")
                                             .equals("1")) {
                    kannAbhaken = true;
                } else {
                    kannAbhaken = false;
                }
            } else {
                kannAbhaken = true;
                if (xrgaf.get(0)
                         .get(1)
                         .equals("0.00")) {
                    // Rechnung erstellt und bereits bezahlt
                    htmlposbuf.append("<b>" + dfx.format(zuzahlungWert) + "</b> " + xrgaf.get(0)
                                                                                         .get(0)
                            + " vom " + DatFunk.sDatInDeutsch(xrgaf.get(0)
                                                                   .get(2))
                            + "<br>" + "bereits bezahlt");
                } else {
                    // Rechnung gestellt aber noch nicht bezahlt
                    htmlposbuf.append(getNoZuZahl(2, xrgaf));
                }
            }
        }

        // htmlposbuf.append((zuzahlungWert > 0 && vec_rez.get(0).get(14).equals("F") ?
        // getNoZuZahl() : "<b>"+dfx.format(zuzahlungWert)+"</b>"));
        htmlposbuf.append("</td>");
        htmlposbuf.append("</tr>");

        return htmlposbuf.toString();
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            //// System.out.println(event.getURL().toString());
            if (event.getURL()
                     .toString()
                     .contains("rezedit")) {

            }
            if (event.getURL()
                     .toString()
                     .contains("patedit")) {
                int anfrage = JOptionPane.showConfirmDialog(null,
                        "Soll das aktuelle Rezept wieder aufgeschlossen werden?", "Achtung wichtige Benutzeranfrage",
                        JOptionPane.YES_NO_OPTION);
                if (anfrage == JOptionPane.YES_OPTION) {
                    eltern.loescheKnoten();
                }
                PatUndVOsuchen.doPatSuchen(aktRezept.getPatIntern(), aktRezept.getRezNb(), this, this.connection);
            }
            if (event.getURL()
                     .toString()
                     .contains("nozz.de")) {
                PointerInfo info = MouseInfo.getPointerInfo();
                Point location = info.getLocation();
                doRezeptgebuehrRechnung(location);
            }
            if (event.getURL()
                     .toString()
                     .contains("alternativekrankenkasse")) {
                doNeueKasseFuerIV();
            }

            if (event.getURL()
                     .toString()
                     .contains("tagedrucken.de")) { // toggle Druck d. Behandlungstage
                /*
                 * if(!this.tagedrucken){ this.tagedrucken = true; while (inParseHtml) { try {
                 * Thread.sleep(25); } catch (InterruptedException ex) { ex.printStackTrace(); }
                 * } parseHTML(aktRezept.getRezNb()); htmlPaneScrollToEnd(); return; }
                 * this.tagedrucken = false; while (inParseHtml) { try { Thread.sleep(25); }
                 * catch (InterruptedException ex) { ex.printStackTrace(); } }
                 * parseHTML(aktRezept.getRezNb()); htmlPaneScrollToEnd(); return;
                 */
                if (this.tagedrucken) {
                    this.tagedrucken = false;
                } else {
                    this.tagedrucken = true;
                }
                parseHTMLuniq(aktRezept.getRezNb());
                htmlPaneScrollToEnd();
                return;
            }

        }
    }

    private void htmlPaneScrollToEnd() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JViewport viewport = scrHtml.getViewport();
                final JComponent view = (JComponent) viewport.getView();
                final int h = view.getHeight();
                view.scrollRectToVisible(new Rectangle(0, h, 1, h));
            }
        });
    }

    private void doNeueKasseFuerIV() {
        aKasse[0].setText(eltern.hmAlternativeKasse.get("<Ivnam1>")
                                                   .trim());
        aKasse[1].setText(eltern.hmAlternativeKasse.get("<Ivid>")
                                                   .trim());
        String[] suchegleichnach = { eltern.hmAlternativeKasse.get("<Ivnam1>")
                                                              .trim(),
                eltern.hmAlternativeKasse.get("<Ivid>")
                                         .trim() };

        // public KassenAuswahl(JXFrame owner, String name,String[]
        // suchegleichnach,JRtaTextField[] elterntf,String kassennum){
        KassenAuswahl kwahl = new KassenAuswahl(null, "ivkassensuche", suchegleichnach, aKasse,
                eltern.hmAlternativeKasse.get("<Ivid>")
                                         .trim());
        kwahl.pack();
        kwahl.setModal(true);
        kwahl.setVisible(true);
        //// System.out.println(aKasse[0].getText());
        //// System.out.println(aKasse[1].getText());
        //// System.out.println(aKasse[2].getText());

        if (!aKasse[2].getText()
                      .trim()
                      .equals(suchegleichnach[1])
                && !aKasse[2].getText()
                             .trim()
                             .equals("")) {
            //// System.out.println("Es wurde eine neue Kasse gewählt");
            macheHashMapIV(aKasse[2].getText()
                                    .trim());
            /*
             * while(inParseHtml){ try { Thread.sleep(25); } catch (InterruptedException ex)
             * { ex.printStackTrace(); } } parseHTML(aktRezept.getRezNb());
             */
            parseHTMLuniq(aktRezept.getRezNb());
        } else {
            //// System.out.println("kasse ist gleich geblieben");
        }
    }

    /*************************
     *
     */
    private void doRezeptgebuehrRechnung(Point location) {
        boolean buchen = true;
        Vector<Vector<String>> testvec = SqlInfo.holeFelder(
                "select reznr from rgaffaktura where reznr='" + aktRezNum.getText() + "' AND rnr LIKE 'RGR-%' LIMIT 1");

        if (testvec.size() > 0) {
            int anfrage = JOptionPane.showConfirmDialog(null,
                    "Für dieses Rezept wurde bereits eine Rezeptgebührrechnung angelegt!"
                            + "Wollen Sie eine Kopie erstellen?",
                    "Achtung wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
            if (anfrage != JOptionPane.YES_OPTION) {
                return;
            }
            buchen = false;
        }
        HashMap<String, String> hmRezgeb = new HashMap<String, String>();
        int rueckgabe = -1;
        String behandl = "";
        for (int i = 0; i < vec_poskuerzel.size(); i++) {
            // behandl=behandl+vec_posanzahl.get(i)+"*"+vec_poskuerzel.get(i).substring(0,2)+(i
            // < (vec_poskuerzel.size()-1) ? "," : "" );
            behandl = behandl + vec_posanzahl.get(i) + "*" + vec_poskuerzel.get(i)
                    + (i < (vec_poskuerzel.size() - 1) ? "," : "");
        }
        // anr=17,titel=18,nname=0,vname=1,strasse=3,plz=4,ort=5,abwadress=19
        // "anrede,titel,nachname,vorname,strasse,plz,ort"

        String cmd = "select abwadress,id from pat5 where pat_intern='" + aktRezept.getPatIntern() + "' LIMIT 1";
        Vector<Vector<String>> adrvec = SqlInfo.holeFelder(cmd);
        String[] adressParams = null;
        if (adrvec.get(0)
                  .get(0)
                  .equals("T")) {
            adressParams = holeAbweichendeAdresse(adrvec.get(0)
                                                        .get(1));
        } else {
            adressParams = getAdressParams(adrvec.get(0)
                                                 .get(1));
        }
        // System.out.println("Zuzhlungwert = "+zuzahlungWert);
        hmRezgeb.put("<rgreznum>", aktRezNum.getText());
        hmRezgeb.put("<rgbehandlung>", behandl);
        hmRezgeb.put("<rgdatum>", DatFunk.sDatInDeutsch(aktRezept.getRezeptDatum()));
        hmRezgeb.put("<rgbetrag>", dfx.format(zuzahlungWert));
        // hmRezgeb.put("<rgpauschale>","5,00");
        hmRezgeb.put("<rgpauschale>", SystemConfig.hmAbrechnung.get("rgrpauschale"));
        hmRezgeb.put("<rggesamt>", "0,00");
        hmRezgeb.put("<rganrede>", adressParams[0]);
        hmRezgeb.put("<rgname>", adressParams[1]);
        hmRezgeb.put("<rgstrasse>", adressParams[2]);
        hmRezgeb.put("<rgort>", adressParams[3]);
        hmRezgeb.put("<rgbanrede>", adressParams[4]);
        hmRezgeb.put("<rgpatintern>", aktRezept.getPatIntern());
        // Hier müssen noch Patienten Namen
        // hmRezgeb.put("<rgpatintern>",vec_rez.get(0).get(0));
        hmRezgeb.put("<rgpatnname>", StringTools.EGross(vec_pat.get(0)
                                                               .get(0)));
        hmRezgeb.put("<rgpatvname>", StringTools.EGross(vec_pat.get(0)
                                                               .get(1)));
        hmRezgeb.put("<rgpatgeboren>", DatFunk.sDatInDeutsch(vec_pat.get(0)
                                                                    .get(2)));
        RezeptGebuehrRechnung rgeb = new RezeptGebuehrRechnung(Reha.getThisFrame(), "Nachberechnung Rezeptgebühren",
                rueckgabe, hmRezgeb, buchen);
        rgeb.setSize(new Dimension(250, 300));
        rgeb.setLocation(location.x - 50, location.y - 50);
        rgeb.pack();
        rgeb.setVisible(true);
        /*
         * while(inParseHtml){ try { Thread.sleep(25); } catch (InterruptedException ex)
         * { ex.printStackTrace(); } } parseHTML(aktRezNum.getText());
         */
        parseHTMLuniq(aktRezept.getRezNb());
        // this.aktRezNum.getText()
        // Positionenvec_poskuerzel.get(i)
        // vec_posanzahl.get(i)
        // AusstellungsDatum DatFunk.sDatInDeutsch(vec_rez.get(0).get(2))
        // zuzahlung zuzahlungWert
        // Bearbeitungsgebühr

    }

    public String[] getAdressParams(String patid) {
        // anr=17,titel=18,nname=0,vname=1,strasse=3,plz=4,ort=5,abwadress=19
        // "anrede,titel,nachname,vorname,strasse,plz,ort"
        String cmd = "select anrede,titel,n_name,v_name,strasse,plz,ort from pat5 where id='" + patid + "' LIMIT 1";
        Vector<Vector<String>> abwvec = SqlInfo.holeFelder(cmd);
        Object[] obj = { abwvec.get(0)
                               .get(0),
                abwvec.get(0)
                      .get(1),
                abwvec.get(0)
                      .get(2),
                abwvec.get(0)
                      .get(3),
                abwvec.get(0)
                      .get(4),
                abwvec.get(0)
                      .get(5),
                abwvec.get(0)
                      .get(6) };
        return AdressTools.machePrivatAdresse(obj, true);
    }

    public String[] holeAbweichendeAdresse(String patid) {
        // "anrede,titel,nachname,vorname,strasse,plz,ort"
        String cmd = "select abwanrede,abwtitel,abwn_name,abwv_name,abwstrasse,abwplz,abwort from pat5 where id='"
                + patid + "' LIMIT 1";
        Vector<Vector<String>> abwvec = SqlInfo.holeFelder(cmd);
        Object[] obj = { abwvec.get(0)
                               .get(0),
                abwvec.get(0)
                      .get(1),
                abwvec.get(0)
                      .get(2),
                abwvec.get(0)
                      .get(3),
                abwvec.get(0)
                      .get(4),
                abwvec.get(0)
                      .get(5),
                abwvec.get(0)
                      .get(6) };
        return AdressTools.machePrivatAdresse(obj, true);
    }

    /*************************
     *
     */
    class AbrechnungListSelectionHandler implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();

            // int firstIndex = e.getFirstIndex();
            // int lastIndex = e.getLastIndex();
            boolean isAdjusting = e.getValueIsAdjusting();
            if (isAdjusting) {
                return;
            }
            // StringBuffer output = new StringBuffer();
            if (lsm.isSelectionEmpty()) {

            } else {
                ////// System.out.println(lsm);
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                        // final int ix = i;
                        new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {

                                // int row = jXTreeTable.getSelectedRow();
                                // int col = jXTreeTable.getSelectedColumn();
                                for (int i = 0; i < root.getChildCount(); i++) {
                                    JXTTreeTableNode node = (JXTTreeTableNode) root.getChildAt(i);
                                    int childs = node.getChildCount();
                                    for (int i2 = 0; i2 < childs; i2++) {
                                        // JXTTreeTableNode node2 = (JXTTreeTableNode) node.getChildAt(i2);
                                    }
                                }

                                return null;
                            }

                        }.execute();
                        break;
                    }
                }
            }
        }
    }

    class AbrechnungTreeSelectionListener implements TreeSelectionListener {
        boolean isUpdating = false;

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            if (!isUpdating) {
                /******/
                try {
                    isUpdating = true;
                    JXTreeTable tt = jXTreeTable;// (JXTreeTable) e.getSource();
                    TreeTableModel ttmodel = tt.getTreeTableModel();
                    TreePath[] selpaths = tt.getTreeSelectionModel()
                                            .getSelectionPaths();

                    if (selpaths != null) {
                        ArrayList<TreePath> selPathList = new ArrayList<TreePath>(Arrays.asList(selpaths));
                        int i = 1;
                        while (i <= selPathList.size()) {
                            // add all kiddies.
                            TreePath currPath = selPathList.get(i - 1);
                            Object currentObj = currPath.getLastPathComponent();
                            int childCnt = ttmodel.getChildCount(currentObj);
                            for (int j = 0; j < childCnt; j++) {
                                Object child = ttmodel.getChild(currentObj, j);
                                TreePath nuPath = currPath.pathByAddingChild(child);
                                if (!selPathList.contains(nuPath)) {
                                    selPathList.add(nuPath);
                                }
                            }
                            i++;
                        }
                        selpaths = selPathList.toArray(new TreePath[0]);

                        tt.getTreeSelectionModel()
                          .setSelectionPaths(selpaths);

                        TreePath tp = tt.getTreeSelectionModel()
                                        .getSelectionPath();
                        aktNode = (JXTTreeTableNode) tp.getLastPathComponent();// selpaths[selpaths.length-1].getLastPathComponent();
                        new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                int lang = getNodeCount();
                                aktRow = -1;
                                for (int i = 0; i < lang; i++) {
                                    if (aktNode == holeNode(i)) {
                                        aktRow = i;
                                        ////// System.out.println("Zeilennummer = = "+i);
                                        ////// System.out.println("Node selektiert = "+aktNode.abr.bezeichnung);
                                        ////// System.out.println("Behandlungsdatum selektiert = "+aktNode.abr.datum+" /
                                        ////// "+aktNode.abr.bezeichnung);
                                        break;
                                    }
                                }
                                return null;
                            }

                        }.execute();
                    }
                    /**********/
                } catch (NullPointerException ex) {
                }

                /**********/
            }
            isUpdating = false;

        }

    }

    /*************************
     *
     */
    private class TageTreeTableModel extends DefaultTreeTableModel {
        // SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        DecimalFormat dfx = new DecimalFormat("0.00");

        public TageTreeTableModel(JXTTreeTableNode jXTTreeTableNode) {
            super(jXTTreeTableNode);
        }

        @Override
        public Object getValueAt(Object node, int column) {
            JXTTreeTableNode jXTreeTableNode = (JXTTreeTableNode) node;

            AbrFall o = null;

            try {
                o = (AbrFall) jXTreeTableNode.getUserObject();
            } catch (ClassCastException cex) {
                return super.getValueAt(node, column);
            }

            switch (column) {
            case 0:
                return o.titel;
            case 1:
                return o.datum;
            case 2:
                return o.bezeichnung;
            case 3:
                return o.anzahl;
            case 4:
                return o.preis;
            case 5:
                return o.zuzahlung;
            case 6:
                return dfx.format(o.rezgeb);
            case 7:
                return o.unterbrechung;
            case 8:
                return o.alterpreis;
            case 9:
                return o.sqldatum;

            }
            return super.getValueAt(node, column);
        }

        @Override
        public void setValueAt(Object value, Object node, int column) {
            JXTTreeTableNode jXTreeTableNode = (JXTTreeTableNode) node;
            AbrFall o;

            try {
                o = (AbrFall) jXTreeTableNode.getUserObject();
            } catch (ClassCastException cex) {
                return;
            }
            switch (column) {
            case 0:
                o.titel = ((String) value);
                break;
            case 1:
                o.datum = ((String) value);
                o.sqldatum = DatFunk.sDatInSQL(((String) value));
                break;
            case 2:
                o.bezeichnung = ((String) value);
                break;
            case 3:
                o.anzahl = ((Double) value);
                break;
            case 4:
                o.preis = Double.parseDouble(dfx.format((value))
                                                .replaceAll(",", "."));
                o.rezgeb = ((o.zuzahlung) ? rechneRezGebFromDouble(o.preis) : (Double) 0.00);
                break;
            case 5:
                ////// System.out.println("in SetValue Zuzahlung="+value);
                o.zuzahlung = ((Boolean) value);
                o.rezgeb = (((Boolean) value) ? rechneRezGebFromDouble(o.preis) : (Double) 0.00);
                break;
            case 6:
                o.rezgeb = ((Double) value);
                break;
            case 7:
                o.unterbrechung = ((String) value);
                break;
            case 8:
                o.alterpreis = ((String) value);
                break;
            case 9:
                o.sqldatum = ((String) value);
                break;
            case 10:
                o.niezuzahl = ((Boolean) value);
                break;

            }
        }

        @Override
        public boolean isCellEditable(java.lang.Object node, int column) {
            switch (column) {
            case 0:
                return false;
            case 1:
                return false;
            case 2:
                return true;
            case 3:
                return true;
            case 4:
                return true;
            case 5:
                return true;
            case 6:
                return false;
            case 7:
                return true;
            case 8:
                return true;

            default:
                return false;
            }
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return Double.class;
            case 4:
                return Double.class;
            case 5:
                return Boolean.class;
            case 6:
                return Double.class;
            case 7:
                return String.class;
            case 8:
                return String.class;
            case 9:
                return String.class;
            default:
                return Object.class;
            }
        }

        @Override
        public int getColumnCount() {
            return 11;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
            case 0:
                return "Abr.Fall";
            case 1:
                return "Behandlungstag";
            case 2:
                return "Heilmittel";
            case 3:
                return "Anzahl";
            case 4:
                return "Preis";
            case 5:
                return "Zuzahlung";
            case 6:
                return "Rez.Gebühr";
            case 7:
                return "Unterbrech.";
            case 8:
                return "Akt.Tarif";
            case 9:
                return "sqldatum";
            default:
                return "Column " + (column + 1);
            }
        }
    }

    private static class JXTTreeTableNode extends DefaultMutableTreeTableNode {

        private boolean enabled = false;
        private AbrFall abr = null;

        public JXTTreeTableNode(String name, AbrFall abr, boolean enabled) {
            super(name);
            this.enabled = enabled;
            this.abr = abr;
            if (abr != null) {
                this.setUserObject(abr);
            }

        }

    }

    @Override
    public void actionPerformed(ActionEvent arg0) {

        String cmd = arg0.getActionCommand();
        if (cmd.equals("monthViewCommit")) {
            String tagNeu = sdf.format(((JXMonthView) arg0.getSource()).getSelectionDate());
            ListenerTools.removeListeners(dlg.getContentPane());
            dlg.setVisible(false);
            dlg.dispose();
            dlg = null;
            if (tagNeu != null) {
                doTagNeu2(tagNeu);
                aktualisiereTree();
            }

        }
        if (cmd.equals("kuerzel")) {
            if (aktRezept.getVec_rez() == null) {
                return;
            }
            final ActionEvent arg0X = arg0;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (aktRow < 0 || aktRezept.getVec_rez()
                                               .size() <= 0) {
                        return;
                    }
                    setEinzelPreis((String) ((JRtaComboBox) arg0X.getSource()).getValueAt(0),
                            (String) ((JRtaComboBox) arg0X.getSource()).getValueAt(1));
                    getVectorFromNodes();
                    doTreeRezeptWertermitteln();
                    doPositionenErmitteln();
                    /*
                     * while(inParseHtml){ try { Thread.sleep(25); } catch (InterruptedException ex)
                     * { ex.printStackTrace(); } } parseHTML(aktRezept.getRezNb());
                     */
                    parseHTMLuniq(aktRezept.getRezNb());
                }
            });
            return;
        }
        if (cmd.equals("break")) {
            final ActionEvent arg0X = arg0;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (aktRow < 0) {
                        return;
                    }
                    doBreak((String) ((JRtaComboBox) arg0X.getSource()).getSelectedItem());
                }
            });
            return;
        }
        if (cmd.equals("akttarif")) {
            final ActionEvent arg0X = arg0;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (aktRow < 0) {
                        return;
                    }
                    doAkttarif((String) ((JRtaComboBox) arg0X.getSource()).getSelectedItem());
                    doTreeRezeptWertermitteln();
                    /*
                     * while(inParseHtml){ try { Thread.sleep(25); } catch (InterruptedException ex)
                     * { ex.printStackTrace(); } } parseHTML(aktRezept.getRezNb());
                     */
                    parseHTMLuniq(aktRezept.getRezNb());
                }
            });
            return;
        }

        if (cmd.equals("zuzahlung")) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ////// System.out.println("Wert der CheckBox =
                    ////// "+((JRtaCheckBox)mycheck.getComponent()).isSelected()+ " Selektierte
                    ////// Tabellenzeile = ");
                }
            });

        }
        if (cmd.equals("expandall")) {
            jXTreeTable.expandAll();
        }
        if (cmd.equals("collapsall")) {
            jXTreeTable.collapseAll();
        }
        if (cmd.equals("tagneu")) {
            doShowView();

        }
        if (cmd.equals("behandlungneu")) {
            doBehandlungNeu();
            aktualisiereTree();
        }
        if (cmd.equals("behandlungloeschen")) {
            doBehandlungLoeschen();
            aktualisiereTree();
        }
    }

    private void doBreak(String unterbrechung) {
        aktNode.abr.unterbrechung = unterbrechung;
    }

    private void doAkttarif(String tarif) {
        // try{
        if (tarif.equals("alt")) {
            aktNode.abr.preis = Double.valueOf(RezTools.getPreisAltFromID(aktNode.abr.preisid, preisgruppe, preisvec)
                                                       .replace(",", "."));
        } else {
            aktNode.abr.preis = Double.valueOf(RezTools.getPreisAktFromID(aktNode.abr.preisid, preisgruppe, preisvec)
                                                       .replace(",", "."));
        }
        if (aktNode.abr.zuzahlung) {
            aktNode.abr.rezgeb = rechneRezGebFromDouble(aktNode.abr.preis);
        }
        /*
         * }catch(Exception ex){ if(aktNode != null){
         * demoTreeTableModel.setValueAt(Double.valueOf("0.00"), aktNode, 4); } }
         */
        jXTreeTable.repaint();
    }

    private void setEinzelPreis(String bez, String id) {
        try {
            if (aktNode.abr.alterpreis.equals("alt")) {
                aktNode.abr.preis = Double.valueOf(RezTools.getPreisAltFromID(id, preisgruppe, preisvec)
                                                           .replace(",", "."));
                aktNode.abr.preisid = id;
                aktNode.abr.bezeichnung = bez;
            } else {
                aktNode.abr.preis = Double.valueOf(RezTools.getPreisAktFromID(id, preisgruppe, preisvec)
                                                           .replace(",", "."));
                aktNode.abr.preisid = id;
                aktNode.abr.bezeichnung = bez;
            }
            if (aktNode.abr.zuzahlung) {
                aktNode.abr.rezgeb = rechneRezGebFromDouble(aktNode.abr.preis);
            }
            // Double preis = Double.valueOf(RezTools.getPreisAktFromID(id, preisgruppe,
            // preisvec).replace(",", "."));
            // demoTreeTableModel.setValueAt(preis, aktNode, 4);

        } catch (NullPointerException ex) {
            if (aktNode != null) {
                demoTreeTableModel.setValueAt(Double.valueOf("0.00"), aktNode, 4);
            }
        }

        jXTreeTable.repaint();
    }

    private void doShowView() {
        dlg = new JDialog();
        dlg.setModal(true);
        dlg.setPreferredSize(new Dimension(200, 200));
        dlg.setUndecorated(true);
        dlg.setLocation(popUpX, popUpY);
        final JDialog fdiag = dlg;
        dlg.setContentPane(showView());
        dlg.getContentPane()
           .addMouseMotionListener(new MouseMotionListener() {
               @Override
               public void mouseDragged(MouseEvent arg0) {
                   int x = arg0.getX();
                   int y = arg0.getY();
                   if (x <= 2 || y <= 2 || x >= (dlg.getWidth() - 2) || y >= (dlg.getHeight() - 2)) {
                       ListenerTools.removeListeners(fdiag.getContentPane());
                       fdiag.setVisible(false);
                       fdiag.dispose();
                   }
               }

               @Override
               public void mouseMoved(MouseEvent arg0) {
                   int x = arg0.getX();
                   int y = arg0.getY();
                   if (x <= 2 || y <= 2 || x >= (dlg.getWidth() - 2) || y >= (dlg.getHeight() - 2)) {
                       ListenerTools.removeListeners(fdiag.getContentPane());
                       fdiag.setVisible(false);
                       fdiag.dispose();
                   }
               }

           });
        dlg.pack();
        dlg.setVisible(true);
    }

    private void doTagNeu2(String tag) {
        // erst testen ob es dieses Datum schon gibt
        // String neudatum = DatFunk.sDatInSQL(tag);
        // JXTTreeTableNode node;
        int count = root.getChildCount();
        int einfuegenbei = count;
        if (count == 0) {
            demoTreeTableModel.insertNodeInto(macheTag(tag, einfuegenbei), root, 0);
            if (hausbesuch) {
                doHausbesuchEinzeln((JXTTreeTableNode) root.getChildAt(einfuegenbei), 0);
            }
            return;
        }
        for (int i = 0; i < count; i++) {
            // node = (JXTTreeTableNode) root.getChildAt(i);

            if (((JXTTreeTableNode) root.getChildAt(i)).abr.datum.equals(tag)) {
                JOptionPane.showMessageDialog(null, "Dieser Behandlungstag existiert bereits");
                return;

            } else if (DatFunk.TageDifferenz(((JXTTreeTableNode) root.getChildAt(i)).abr.datum, tag) < 0) {
                einfuegenbei = i;
                break;

            }
            ////// System.out.println(
            ////// DatFunk.TageDifferenz(((JXTTreeTableNode)root.getChildAt(i)).abr.datum,
            ////// tag) );

        }
        ////// System.out.println( "******>Neuer Tag wird eingefügt bei Index =
        ////// "+einfuegenbei);
        JXTTreeTableNode neuNode = null;
        try {
            if (einfuegenbei == count) {
                neuNode = macheTag(tag, einfuegenbei);
                // hier muß noch größer als max-Angabe im Rezept rein
                if (neuNode == null) {
                    return;
                }
                demoTreeTableModel.insertNodeInto(neuNode, root, count);
                if (hausbesuch) {
                    doHausbesuchEinzeln((JXTTreeTableNode) root.getChildAt(einfuegenbei), count);
                }
            } else {
                neuNode = macheTag(tag, einfuegenbei);
                // hier muß noch größer als max-Angabe im Rezept rein
                if (neuNode == null) {
                    return;
                }
                demoTreeTableModel.insertNodeInto(neuNode, root, einfuegenbei);
                if (hausbesuch) {
                    doHausbesuchEinzeln((JXTTreeTableNode) root.getChildAt(einfuegenbei), count);
                }
            }
            //// System.out.println(neuNode);
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(null, "Tag kann nicht eingefügt werden");
            //// System.out.println(neuNode);
            ex.printStackTrace();
        }

    }

    /*************************************************************************************************************/

    private void aktualisiereTree() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                getVectorFromNodes();
                doGebuehren();
                doTreeRezeptWertermitteln();
                doTitelRepair();
                jXTreeTable.repaint();
                doPositionenErmitteln();
                /*
                 * while(inParseHtml){ try { Thread.sleep(25); } catch (InterruptedException ex)
                 * { ex.printStackTrace(); } } parseHTML(aktRezept.getRezNb());
                 */
                parseHTMLuniq(aktRezept.getRezNb());
                return null;
            }
        }.execute();
    }

    private JXTTreeTableNode macheTag(String tag, int einfuegen) {
        JXTTreeTableNode node = null;
        // AbrFall fall= null;
        ////// System.out.println(vec_rez.get(0).get(34));
        String[] tage = aktRezept.getTermine()
                                 .split("\n");
        int zaehler = 0;
        String alletage = "";
        for (int i = 0; i < root.getChildCount() + 1; i++) {
            if (i == einfuegen) {
                alletage = alletage + tag + "@@@@" + DatFunk.sDatInSQL(tag) + "\n";
            } else {
                alletage = alletage + tage[zaehler] + "\n";
                zaehler++;
            }
        }
        ////// System.out.println(alletage);
        aktRezept.getVecVec_rez()
                 .get(0)
                 .set(34, alletage);
        /****** Entscheidender Funktionsaufruf ****************************/
        ermittleAbrechnungsfall(false);

        AbrFall abr = null;
        JXTTreeTableNode childnode = null;
        int neu = 0;
        try {
            for (int i = 0; i < vec_tabelle.size(); i++) {
                //// System.out.println(vec_tabelle.get(i).get(0)+" - Tage = "+tag);
                //// System.out.println(vec_tabelle.get(i).get(0)+" - Tage = "+tag);
                if (vec_tabelle.get(i)
                               .get(0)
                               .equals(tag)) {
                    if (neu == 0) {
                        abr = constuctAbrFall(i, einfuegen + 1);
                        node = new JXTTreeTableNode(abr.datum, abr, true);
                        neu++;
                    } else {
                        abr = constuctAbrFall(i, einfuegen + 1);
                        childnode = new JXTTreeTableNode(abr.datum, abr, true);
                        node.add(childnode);
                    }
                }
            }
            if (node == null) {
                //// System.out.println("Node = null Tage = -> "+tag);
                //// System.out.println("Größe von vec_tabelle = -> "+vec_tabelle.size());
                //// System.out.println("GetNodeCount() = -> "+getNodeCount());
                abr = constuctNewAbrFall(vec_tabelle.size() - 1, einfuegen + 1, tag);
                node = new JXTTreeTableNode(abr.datum, abr, true);
            }

        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(null, "Fehler bei der Erstellung des Behanlungstages");
            ex.printStackTrace();
        }
        //// System.out.println(node);
        // xx
        return node;
    }

    private AbrFall constuctNewAbrFall(int vecindex, int tag, String datum) {
        // public AbrFall(String titel,String datum,String bezeichnung,Double
        // anzahl,Double preis,
        // boolean zuzahlung,double rezgeb,String unterbrechung,String alterpreis,String
        // sqldatum,String preisid,boolean niezuzahl){

        AbrFall abr = new AbrFall(Integer.toString(tag) + ".Tag", datum, (String) vec_tabelle.get(vecindex)
                                                                                             .get(1),
                (Double) vec_tabelle.get(vecindex)
                                    .get(2),
                (Double) vec_tabelle.get(vecindex)
                                    .get(3),
                (Boolean) vec_tabelle.get(vecindex)
                                     .get(4),
                (Double) vec_tabelle.get(vecindex)
                                    .get(5),
                (String) vec_tabelle.get(vecindex)
                                    .get(6),
                (String) vec_tabelle.get(vecindex)
                                    .get(7),
                DatFunk.sDatInSQL(datum), (String) vec_tabelle.get(vecindex)
                                                              .get(9),
                (Boolean) vec_tabelle.get(vecindex)
                                     .get(10));
        return abr;
    }

    private AbrFall constuctAbrFall(int vecindex, int tag) {
        AbrFall abr = new AbrFall(Integer.toString(tag) + ".Tag", (String) vec_tabelle.get(vecindex)
                                                                                      .get(0),
                (String) vec_tabelle.get(vecindex)
                                    .get(1),
                (Double) vec_tabelle.get(vecindex)
                                    .get(2),
                (Double) vec_tabelle.get(vecindex)
                                    .get(3),
                (Boolean) vec_tabelle.get(vecindex)
                                     .get(4),
                (Double) vec_tabelle.get(vecindex)
                                    .get(5),
                (String) vec_tabelle.get(vecindex)
                                    .get(6),
                (String) vec_tabelle.get(vecindex)
                                    .get(7),
                (String) vec_tabelle.get(vecindex)
                                    .get(8),
                (String) vec_tabelle.get(vecindex)
                                    .get(9),
                (Boolean) vec_tabelle.get(vecindex)
                                     .get(10));
        return abr;
    }

    private JXTTreeTableNode constructNewBehandlung(JXTTreeTableNode node) {
        JXTTreeTableNode xnode = null;
        AbrFall abr = null;
        Vector<Object> tagdummy = new Vector<Object>();
        for (int i = 0; i < vec_tabelle.get(0)
                                       .size(); i++) {
            tagdummy.add(vec_tabelle.get(aktRow)
                                    .get(i));
        }
        vec_tabelle.insertElementAt((Vector<Object>) tagdummy.clone(), aktRow);
        abr = constuctAbrFall(aktRow, -1);
        xnode = new JXTTreeTableNode(abr.datum, abr, true);
        JXTTreeTableNode ynode = (JXTTreeTableNode) getBasicNodeFromChild(node);
        demoTreeTableModel.insertNodeInto(xnode, ynode, ynode.getChildCount());
        return xnode;
    }

    private void doTitelRepair() {
        int count = root.getChildCount();
        int nodes;
        int xtag = 1;
        String datum = null, dummydat = null;
        for (int i = 0; i < count; i++) {
            if (i == 0) {
                datum = ((JXTTreeTableNode) root.getChildAt(i)).abr.datum;
                dummydat = datum;
            }
            if (!datum.equals(dummydat)) {
                datum = ((JXTTreeTableNode) root.getChildAt(i)).abr.datum;
                dummydat = datum;
            }
            ((JXTTreeTableNode) root.getChildAt(i)).abr.titel = Integer.toString(xtag) + ".Tag";
            nodes = ((JXTTreeTableNode) root.getChildAt(i)).getChildCount();
            for (int i2 = 0; i2 < nodes; i2++) {
                ((JXTTreeTableNode) ((JXTTreeTableNode) root.getChildAt(i)).getChildAt(
                        i2)).abr.titel = Integer.toString(xtag) + ".Tag";
            }
            xtag++;

        }
        jXTreeTable.repaint();
    }

    private void doBehandlungNeu() {
        if (aktRow < 0) {
            JOptionPane.showMessageDialog(null, "Kein Behandlungstag ausgewählt für zusätzliches Heilmittel");
            return;
        }
        if (root.getChildCount() == 0) {
            JOptionPane.showMessageDialog(null,
                    "Es existiert kein Behandlungstag!\nWie bitteschön wollen Sie einem  nicht existierenden Behandlungstag\nein ergänzendes Heilmittel hinzufügen??");
            return;
        }
        // TreeTableNode[] nodes = demoTreeTableModel.getPathToRoot(aktNode);
        constructNewBehandlung(aktNode);
        doGebuehren();
        doTreeRezeptWertermitteln();
    }

    /**************************************************************/

    private void doBehandlungLoeschen() {
        if (getNodeCount() == 0 || holeAbrFall(aktRow) == null) {
            return;
        }

        TreeTableNode[] nodes = demoTreeTableModel.getPathToRoot(aktNode);
        String behandlung = aktNode.abr.bezeichnung;
        String datum = aktNode.abr.datum;
        String text = "";
        if (nodes.length == 3) {
            text = "Diese Behandlung wirklich löschen???\n\nBehandlung = " + behandlung + "\nDatum = " + datum + "\n";
        } else {
            text = "Sie löschen einen kompletten Behandlungstag(!!)\n\nBehandlungstag = " + datum + "\n";
        }

        int anfrage = JOptionPane.showConfirmDialog(null, text, "Achtung wichtige Benutzeranfrage",
                JOptionPane.YES_NO_OPTION);
        if (anfrage == JOptionPane.YES_OPTION) {
            demoTreeTableModel.removeNodeFromParent(aktNode);
        }
    }
    /*
     * private void loescheTree(){ int childs; while( (childs=root.getChildCount())
     * > 0){ demoTreeTableModel.removeNodeFromParent((MutableTreeTableNode)
     * root.getChildAt(0)); } }
     */

    private void getVectorFromNodes() {
        int lang = this.getNodeCount();

        ////// System.out.println("NodeCount nach löschen == "+this.getNodeCount());
        vec_tabelle.clear();
        AbrFall abr = null;

        for (int i = 0; i < lang; i++) {
            vecdummy.clear();
            abr = this.holeAbrFall(i);

            vecdummy.add(abr.datum);
            vecdummy.add(abr.bezeichnung);
            vecdummy.add(abr.anzahl);
            vecdummy.add(abr.preis);
            vecdummy.add((abr.niezuzahl ? false : abr.zuzahlung));
            vecdummy.add(abr.rezgeb);
            vecdummy.add(abr.unterbrechung);
            vecdummy.add(abr.alterpreis);
            vecdummy.add(abr.sqldatum);
            vecdummy.add(abr.preisid);
            vecdummy.add(abr.niezuzahl);
            vec_tabelle.add((Vector<Object>) vecdummy.clone());
        }
        sortiereVector(vec_tabelle, 0);

    }

    private void sortiereVector(Vector vec, int dimension) {
        final int xdimension = dimension;
        Comparator<Vector<String>> comparator = new Comparator<Vector<String>>() {
            @Override
            public int compare(Vector<String> o1, Vector<String> o2) {
                String s1 = DatFunk.sDatInSQL((String) o1.get(xdimension));
                String s2 = DatFunk.sDatInSQL((String) o2.get(xdimension));
                return s1.compareTo(s2);
            }
        };
        Collections.sort(vec, comparator);

    }

    private TreeTableNode getBasicNodeFromChild(TreeTableNode node) {
        TreeTableNode xnode;
        TreeTableNode ynode;
        TreeTableNode retnode = null;
        int anzahlBasics = root.getChildCount();
        int anzahlKinder = 0;
        for (int i = 0; i < anzahlBasics; i++) {
            xnode = root.getChildAt(i);
            if (xnode == node) {
                return root.getChildAt(i);
            } else {
                anzahlKinder = xnode.getChildCount();
                for (int i2 = 0; i2 < anzahlKinder; i2++) {
                    ynode = xnode.getChildAt(i2);
                    {
                        if (ynode == node) {
                            return xnode;
                        }
                    }
                }

            }
        }
        return retnode;
    }

    private void abrfallAnhaengen(int tagindex, JXTTreeTableNode node, String tag, String position, double anzahl,
            boolean immerfrei) {
        AbrFall abr;
//        public AbrFall(String titel,String datum,String bezeichnung,Double anzahl,Double preis,
//                boolean zuzahlung,double rezgeb,String unterbrechung,String alterpreis,String sqldatum,String preisid){

        String id = RezTools.getIDFromPos(position, preisgruppe, this.preisvec);
        Double preis = Double.parseDouble(RezTools.getPreisAktFromID(id, preisgruppe, this.preisvec)
                                                  .replace(",", "."));
        abr = new AbrFall(Integer.toString(tagindex) + ".Tag", tag, RezTools.getKurzformFromID(id, this.preisvec),
                anzahl, preis, immerfrei ? false : true, this.rechneRezGebFromDouble(preis), node.abr.unterbrechung,
                node.abr.alterpreis, node.abr.sqldatum, id, immerfrei);
        if (id.equals("-1")) {
            JOptionPane.showMessageDialog(null, "Fehler!!!\n\nDie HM-Position " + position
                    + " existiert in Ihrer Preisliste nicht!\n\nDas Rezept kann nicht abgerechnet werden.");
        }
        ////// System.out.println("niezuzahl ="+abr.niezuzahl+" Zuzahlungspflicht =
        ////// "+abr.zuzahlung);
        JXTTreeTableNode xnode = new JXTTreeTableNode("", abr, true);
        demoTreeTableModel.insertNodeInto(xnode, node, node.getChildCount());

    }

    private void prepareTreeFromVector(boolean zeigefertige) {
        AbrFall abr;
        if (vec_tabelle.size() <= 0) {
            return;
        }
        String testdatum = "";
        JXTTreeTableNode knoten = null;
        int tag = 0;
        for (int i = 0; i < vec_tabelle.size(); i++) {
            abr = new AbrFall(Integer.toString(tag) + ".Tag", (String) vec_tabelle.get(i)
                                                                                  .get(0),
                    (String) vec_tabelle.get(i)
                                        .get(1),
                    (Double) vec_tabelle.get(i)
                                        .get(2),
                    (Double) vec_tabelle.get(i)
                                        .get(3),
                    (Boolean) vec_tabelle.get(i)
                                         .get(4),
                    (Double) vec_tabelle.get(i)
                                        .get(5),
                    (String) vec_tabelle.get(i)
                                        .get(6),
                    (String) vec_tabelle.get(i)
                                        .get(7),
                    (String) vec_tabelle.get(i)
                                        .get(8),
                    (String) vec_tabelle.get(i)
                                        .get(9),
                    (Boolean) vec_tabelle.get(i)
                                         .get(10));
            if (!testdatum.trim()
                          .equals(abr.datum.trim())) {
                tag++;
                if (!zeigefertige) {
                    abr.unterbrechung = vectage.get(tag - 1)
                                               .get(2);
                }
                abr.titel = Integer.toString(tag) + ".Tag";
                knoten = new JXTTreeTableNode(abr.datum, abr, true);
                demoTreeTableModel.insertNodeInto(knoten, root, root.getChildCount());
                testdatum = String.valueOf(abr.datum);
                ////// System.out.println(testdatum);
                continue;

            } else {
                if (!zeigefertige) {
                    abr.unterbrechung = vectage.get(tag - 1)
                                               .get(2);
                }
                foo = new JXTTreeTableNode("", abr, true);
                demoTreeTableModel.insertNodeInto(foo, knoten, knoten.getChildCount());
                testdatum = String.valueOf(abr.datum);
                ////// System.out.println("In einzelner Funktion "+testdatum);
                continue;
            }
        }
        // jXTreeTable.expandAll();

    }




    /************************************************************************/

     boolean macheEDIFACT() {
        boolean ret = true;
        double gesamt = 0.00;
        double rez = 0.00;
        double pauschal = (mitPauschale ? 10.00 : 0.00);
        edibuf.setLength(0);
        edibuf.trimToSize();
        if (this.notready) {
            JOptionPane.showMessageDialog(null,
                    "Jetzt zeigt man Ihnen in fetter roter Schrift daß ein Preislistenfehler vorliegt,\nund Sie Armleuchter versuchen das Rezept trotzdem abzurechnen.\n\nLassen Sie den Alkohol weg - das schadet Ihnen!");
            return false;
        }
        String test = vec_pat.get(0)
                             .get(6);
        if ((test.trim()
                 .length() > 12)
                || (test.trim()
                        .length() == 0)) {
            // Versichertennummer falsch oder nicht angegeben
            JOptionPane.showMessageDialog(null, "Versichertennummer nicht angegeben oder falsch");
            return false;
        }
        edibuf.append("INV+" + test.trim() + plus);
        test = vec_pat.get(0)
                      .get(7);
        if (test.trim()
                .equals("")) {
            // Status nicht angegeben
            JOptionPane.showMessageDialog(null, "Status nicht angegeben oder falsch");
            return false;
        } else if (test.trim()
                       .length() > 5) {
            test = test.substring(0, 5);
        } else {
            test = test.substring(0, 1) + "0001";
        }
        if (test.trim()
                .length() != 5
                || (test.trim()
                        .indexOf(" ") >= 0)) {
            JOptionPane.showMessageDialog(null,
                    "Die Länge des Versichertenstatus ist falsch, oder es wurden Leerzeichen im Status angegeben!\nRezept kann nicht abgerechnet werden");
            return false;
        }
        edibuf.append(test.trim() + plus + plus);
        edibuf.append(aktRezept.getRezNb() + EOL);
        edibuf.append("NAD+" + hochKomma(vec_pat.get(0)
                                                .get(0)
                                                .trim())
                + plus);
        edibuf.append(hochKomma(vec_pat.get(0)
                                       .get(1)
                                       .trim())
                + plus);
        test = ediDatumFromSql(vec_pat.get(0)
                                      .get(2));
        if (test.length() == 0) {
            JOptionPane.showMessageDialog(null, "Geburtsdatum nicht angegeben");
            return false;
        }
        edibuf.append(test + plus);
        edibuf.append(hochKomma(vec_pat.get(0)
                                       .get(3)
                                       .trim())
                + plus);
        edibuf.append(hochKomma(vec_pat.get(0)
                                       .get(4)
                                       .trim())
                + plus);
        edibuf.append(hochKomma(vec_pat.get(0)
                                       .get(5)
                                       .trim())
                + EOL);
        JXTTreeTableNode node;
        for (int i = 0; i < getNodeCount(); i++) {
            node = holeNode(i);
            // Notwendig wg. BKK-Gesundheit Tarifwechsel
            if (!node.abr.tarifwechsel) {
                edibuf.append(
                        (disziplinGruppe.equals("61") || disziplinGruppe.equals("62") ? "ENF++" : "EHE+")
                                + disziplinGruppe + ":" + SystemPreislisten.hmPreisBereich.get(aktDisziplin)
                                                                                          .get(Integer.parseInt(
                                                                                                  preisgruppe) - 1)
                                + SystemPreislisten.hmPreisBesonderheit.get(aktDisziplin)
                                                                       .get(Integer.parseInt(preisgruppe) - 1)
                                + plus);
            } else {
                edibuf.append((disziplinGruppe.equals("61") || disziplinGruppe.equals("62") ? "ENF++" : "EHE+")
                        + disziplinGruppe + ":" + node.abr.tarifkennzeichen + "000" + plus);
            }
            edibuf.append(RezTools.getPosFromID(node.abr.preisid, preisgruppe, preisvec) + plus);
            edibuf.append(dfx.format(node.abr.anzahl) + plus);
            gesamt += BigDecimal.valueOf(node.abr.preis)
                                .multiply(BigDecimal.valueOf(node.abr.anzahl))
                                .doubleValue();
            edibuf.append(dfx.format(node.abr.preis) + plus);
            edibuf.append(ediDatumFromDeutsch(node.abr.datum));
            if (node.abr.rezgeb > 0) {
                rez += node.abr.rezgeb;
                //// System.out.println("1. Zuzahlmodus = "+(eltern.zuzahlModusDefault ?
                //// "Normal" : "Bayrisch"));
                if (eltern.zuzahlModusDefault) {
                    edibuf.append(plus + dfx.format(node.abr.rezgeb) + EOL);
                } else { // bayrischer Modus
                         // Einstieg1 für Kilometer
                         // Herr Lehmann: nächste Zeile muß freigeschaltet werden für Einzelkilometer
                    edibuf.append(plus + dfx.format(node.abr.rezgeb / node.abr.anzahl) + EOL);
                }

            } else {
                edibuf.append(EOL);
            }
            if ((!node.abr.unterbrechung.trim()
                                        .equals(""))
                    && (!node.abr.unterbrechung.trim()
                                               .equals("-"))) {
                edibuf.append("TXT+" + node.abr.unterbrechung.trim() + EOL);
            }

        }
        if (disziplinGruppe.equals("61") || disziplinGruppe.equals("62")) {
            edibuf.append("ZUV+");
            test = vec_pat.get(0)
                          .get(14)
                          .trim();
            if (test.length() != 9) {
                // Betriebsstätte
                test = "999999999";
            }
            if (!testeZahl(test)) {
                test = "999999999";
            }
            edibuf.append(test + plus);
            test = vec_pat.get(0)
                          .get(15)
                          .trim();
            if (test.length() != 9) {
                // LANR
                test = "999999999";
            }
            if (!testeZahl(test)) {
                test = "999999999";
            }
            edibuf.append(test + plus);
            edibuf.append(ediDatumFromSql(aktRezept.getRezeptDatum()) + plus);
            edibuf.append(zuZahlungsPos + EOL);

        } else {
            edibuf.append("ZHE+");
            test = vec_pat.get(0)
                          .get(14)
                          .trim();
            if (test.length() != 9) {
                // Betriebsstätte
                test = "999999999";
            }
            if (!testeZahl(test)) {
                test = "999999999";
            }
            edibuf.append(test + plus);
            test = vec_pat.get(0)
                          .get(15)
                          .trim();
            if (test.length() != 9) {
                // LANR
                test = "999999999";
            }
            if (!testeZahl(test)) {
                test = "999999999";
            }
            edibuf.append(test + plus);
            edibuf.append(ediDatumFromSql(aktRezept.getRezeptDatum()) + plus);
            edibuf.append(zuZahlungsPos + plus);
            test = aktRezept.getIndiSchluessel();
            if (test.startsWith("kein Indi")) {
                JOptionPane.showMessageDialog(null, "Kein Indikationsschlüssel angegeben");
                return false;
            } else if (test.equals("k.A.")) {
                test = "9999";
            }

            edibuf.append(test.replace(" ", "") + plus);
            /************************************************/
            /*
             * // McM vor dentist-test:
             * //edibuf.append(voIndex[Integer.parseInt(aktRezept.getVec_rez().get(0).get(27
             * ))]+EOL); // -> edibuf.append(voIndex[aktRezept.getRezArt()]+EOL);
             *
             * if(AktuelleRezepte.isDentist(test)){
             * edibuf.append(voIndex[aktRezept.getRezArt()]+plus); edibuf.append("1"+EOL);
             * System.out.println("Zahnarztverordnung"); }else{
             * edibuf.append(voIndex[aktRezept.getRezArt()]+EOL); }
             */
            edibuf.append(voIndex[aktRezept.getRezArt()]);
            if (AktuelleRezepte.isDentist(test)) {
                edibuf.append(plus + "1");
                System.out.println("Zahnarztverordnung");
            }
            edibuf.append(EOL);
        }

        // an dieser Stelle muß der ICD-10 eingebaut werden, sofern vorhanden
        // DIA+....
        if (aktRezept.getICD10()
                     .length() > 0) {
            edibuf.append("DIA+" + hochKomma(aktRezept.getICD10()) + EOL);
        }
        if (aktRezept.getICD10_2()
                     .length() > 0) {
            edibuf.append("DIA+" + hochKomma(aktRezept.getICD10_2()) + EOL);
        }

        // an dieser Stelle müssen Daten zur Bewilligung eingebaut werden sofern
        // vorhanden
        // SKZ+....

        // Ramsch mit der Genehmigung von LFV und Rehasport/Funktionstraining

        // String[] genehmigung = RezNeuanlage.holeLFV("diagnose", "verordn",
        // "rez_nr",aktRezept.getRezNb() ,
        // aktRezept.getVecVec_rez().get(0).get(1).substring(0,2).toUpperCase());
        String[] genehmigung = RezNeuanlage.holeLFV("diagnose", "verordn", "rez_nr", aktRezept.getRezNb(),
                aktRezept.getRezClass());
        String[] skz = { "", "", "", "", "", "", "" };
        if (disziplinGruppe.equals("61") || disziplinGruppe.equals("62")) {
            // String genehmigung = SqlInfo.holeEinzelFeld("select diagnose from verordn
            // wehere rez_nr = '"+vec_rez.get(0).get(1)+"' Limit 1").trim();
            if (!genehmigung[0].equals("")) {
                try {
                    skz = genehmigung[0].split(Pattern.quote("$$"));
                    if (skz[3].trim()
                              .equals("")) {
                        skz[3] = vec_pat.get(0)
                                        .get(6);
                    }
                    edibuf.append("SKZ" + plus + hochKomma(skz[3]) + plus + DatFunk.sDatInSQL(skz[4])
                                                                                   .replace("-", "")
                            + plus + (disziplinGruppe.equals("61") ? "H1" : "I1") + EOL);
                } catch (NullPointerException ex) {
                    edibuf.append("SKZ" + plus + hochKomma(vec_pat.get(0)
                                                                  .get(6))
                            + plus + ediDatumFromSql(aktRezept.getRezeptDatum()) + plus
                            + (disziplinGruppe.equals("61") ? "H1" : "I1") + EOL);
                    JOptionPane.showMessageDialog(null,
                            "Fehler im Segment Kostenzusage, Verordnung bitte keinesfalls abrechnen!!");
                } catch (ArrayIndexOutOfBoundsException aex) {
                    edibuf.append("SKZ" + plus + hochKomma(vec_pat.get(0)
                                                                  .get(6))
                            + plus + ediDatumFromSql(aktRezept.getRezeptDatum()) + plus
                            + (disziplinGruppe.equals("61") ? "H1" : "I1") + EOL);
                    JOptionPane.showMessageDialog(null,
                            "Fehler im Segment Kostenzusage, Verordnung bitte keinesfalls abrechnen!!");
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "Achtung für Rehasport und/oder Funktionstraining muß eine Genehmigung vermerkt sein.\nRezept bitte nicht abrechnen!!!!!");
            }
        } else {
            if (!genehmigung[0].equals("")) {
                try {
                    skz = genehmigung[0].split(Pattern.quote("$$"));
                    if (skz[3].trim()
                              .equals("")) {
                        skz[3] = vec_pat.get(0)
                                        .get(6);
                    }
                    // System.out.println("SKZ[4]="+skz[4]);
                    // System.out.println("SKZ[3]="+skz[3]);
                    // Prüfen will Kasse normale VOAdR genehmigen ja/nein?
                    boolean genehmigungADR = false;
                    try {
                        String saftladen = SqlInfo.holeEinzelFeld(
                                "select id from adrgenehmigung where ik = '" + eltern.ik_kasse + "' LIMIT 1");
                        if (!saftladen.isEmpty()) {
                            int anfrage = JOptionPane.showConfirmDialog(null, test,
                                    "Handelt es sich hier um eine Langfristverordnung außerhalb des Regelfalles?\n\nJa = Langfristverordnung außerhalb des Regelfalles\nNein = Einzelverordnung außerhalb des Regelfalles\n",
                                    JOptionPane.YES_NO_OPTION);
                            if (anfrage != JOptionPane.YES_OPTION) {
                                genehmigungADR = true;
                            }
                        }
                    } catch (NullPointerException ex) {
                    }
                    edibuf.append("SKZ+" + hochKomma(skz[3]) + plus + DatFunk.sDatInSQL(skz[4])
                                                                             .replace("-", "")
                            + plus + (genehmigungADR ? "B1" : "B2") + EOL);
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                    edibuf.append("SKZ" + plus + hochKomma(vec_pat.get(0)
                                                                  .get(6))
                            + plus + ediDatumFromSql(aktRezept.getRezeptDatum()) + plus + "B2" + EOL);
                    JOptionPane.showMessageDialog(null,
                            "Fehler im Segment Kostenzusage, Verordnung bitte keinesfalls abrechnen!!");
                } catch (ArrayIndexOutOfBoundsException aex) {
                    aex.printStackTrace();
                    edibuf.append("SKZ" + plus + hochKomma(vec_pat.get(0)
                                                                  .get(6))
                            + plus + ediDatumFromSql(aktRezept.getRezeptDatum()) + plus + "B2" + EOL);
                    JOptionPane.showMessageDialog(null,
                            "Fehler im Segment Kostenzusage, Verordnung bitte keinesfalls abrechnen!!");
                }
            }
        }
        edibuf.append("BES+");
        edibuf.append(dfx.format(gesamt) + plus);
        edibuf.append(dfx.format(rez + pauschal) + plus);
        edibuf.append(dfx.format(rez) + plus);
        edibuf.append(dfx.format(pauschal) + EOL);

        String kopfzeile = "PG=" + preisgruppe + ":PATINTERN=" + aktRezept.getPatIntern() + ":REZNUM="
                + aktRezept.getRezNb() + ":GESAMT=" + dfx.format(gesamt) + ":REZGEB=" + dfx.format(rez + pauschal)
                + ":REZANTEIL=" + dfx.format(rez) + ":REZPAUSCHL=" + dfx.format(pauschal) + ":KASSENID="
                + aktRezept.getKtraeger() + ":ARZTID=" + aktRezept.getArztId() + ":PATIENT=" + vec_pat.get(0)
                                                                                                      .get(0)
                + ", " + vec_pat.get(0)
                                .get(1)
                + ":STATUS=" + vec_pat.get(0)
                                      .get(7)
                + ":HB=" + hausbesuch + ":ZZINDEX=" + zuZahlungsIndex + "\n";

        /*
         * edibuf.append("BES+"); edibuf.append(dfx.format(gesamt)+plus);
         * edibuf.append(dfx.format(rez)+plus); edibuf.append(dfx.format(rez)+plus);
         * edibuf.append("0,00"+EOL);
         *
         * String kopfzeile =
         * "PG="+preisgruppe+":PATINTERN="+vec_rez.get(0).get(0).trim()+":REZNUM="+
         * vec_rez.get(0).get(1)+
         * ":GESAMT="+dfx.format(gesamt)+":REZGEB="+dfx.format(rez)+
         * ":REZANTEIL="+dfx.format(rez)+":REZPAUSCHL=0,00"+":KASSENID="+vec_rez.get(0).
         * get(37)+
         * ":ARZTID="+vec_rez.get(0).get(16)+":PATIENT="+vec_pat.get(0).get(0)+", "
         * +vec_pat.get(0).get(1)+":STATUS="+vec_pat.get(0).get(7)+":HB="+hausbesuch+
         * ":ZZINDEX="+zuZahlungsIndex+"\n";
         */

        edibuf.insert(0, vec_poskuerzel.toString() + "\n");
        edibuf.insert(0, vec_posanzahl.toString() + "\n");
        edibuf.insert(0, vec_pospos.toString() + "\n");
        edibuf.insert(0, kopfzeile);
        logger.debug(edibuf.toString());
        return ret;
    }

    private boolean testeZahl(String zahl) {
        String zahlen = "0123456789";
        for (int i = 0; i < zahl.length(); i++) {
            if (zahlen.indexOf(zahl.substring(i, i + 1)) < 0) {
                return false;
            }
        }
        return true;
    }

    private String ediDatumFromSql(String deutschDat) {
        if (deutschDat.trim()
                      .length() < 10) {
            return "";
        }
        return deutschDat.replace("-", "");
    }

    private String ediDatumFromDeutsch(String deutschDat) {
        if (deutschDat.trim()
                      .length() < 10) {
            return "";
        }
        return deutschDat.substring(6) + deutschDat.substring(3, 5) + deutschDat.substring(0, 2);
    }

    private String datumFromEdiDeutsch(String deutschDat) {
        return deutschDat.substring(6) + "." + deutschDat.substring(4, 6) + "." + deutschDat.substring(0, 4);
    }

    public String hochKomma(String string) {
        String str = string.replace("?", "??");
        str = string.replace("'", "?'");
        str = str.replace(":", "?:");
        str = str.replace("+", "?+");
        str = str.replace(",", "?,");
        return str;
    }

    /************************************************************************/

    private boolean holeEDIFACT(String rez_nr) {
        int zugabe = 0;
        boolean ret = true;
        edibuf.setLength(0);
        edibuf.trimToSize();

        edibuf.append(SqlInfo.holeFelder("select edifact from fertige where rez_nr='" + rez_nr + "'")
                             .get(0)
                             .get(0));

        if (edibuf.length() <= 0) {
            JOptionPane.showMessageDialog(null, "EDIFACT-Code kann nicht abgeholt werden");
        }

        String[] zeilen = edibuf.toString()
                                .split("\n");
        String[] positionen = zeilen[0].split(":");

        int basis = zeilen.length - 2;

        // basis = basis - countWords(edibuf.toString(),"DIA+");
        // basis = basis - countWords(edibuf.toString(),"SKZ+");

        basis = basis - countWordsFromRowStart(zeilen, "DIA+");
        basis = basis - countWordsFromRowStart(zeilen, "SKZ+");

        /*
         * System.out.println("Zeilenlänge insgesamt:"+zeilen.length);
         * System.out.println("Anzahl DIA+ Segmente:"+countWords(edibuf.toString(),
         * "DIA+"));
         * System.out.println("Anzahl SKZ+ Segmente:"+countWords(edibuf.toString(),
         * "SKZ+"));
         * System.out.println("Anzahl DIA+ Segmente:"+countWordsFromRowStart(zeilen,
         * "DIA+"));
         * System.out.println("Anzahl SKZ+ Segmente:"+countWordsFromRowStart(zeilen,
         * "SKZ+"));
         */

        // System.out.println("Basis = "+basis);

        if (zeilen[basis].split("\\+").length < 5) {
            JOptionPane.showMessageDialog(null, "Fehler in holeEDIFACT, falsche Länge im Segment ZHE");
            return false;
        }

        // System.out.println("Zeile-Basis = "+zeilen[basis]);

        zuZahlungsPos = zeilen[basis].replace("'", "")
                                     .split("\\+")[4];

        zuZahlungsIndex = zzpflicht[Integer.parseInt(zuZahlungsPos)];
        this.preisgruppe = positionen[0].split("=")[1];
        this.mitPauschale = (Double.parseDouble(zeilen[zeilen.length - 1].split("\\+")[4].replace(",", ".")
                                                                                         .replace("'", "")) > Double
                                                                                                                    .parseDouble(
                                                                                                                            "0.00") ? true
                                                                                                                                    : false);
        int lang = zeilen.length;

        final String xrez_nr = rez_nr;
        vec_rez_valid = Boolean.FALSE; // ungültig bis neu belegt
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                inworker = true;
                sucheRezept(xrez_nr);
                inworker = false;
                return null;
            }
        }.execute();
        vec_pospos.clear();
        macheVector(vec_pospos, zeilen[1], 0);
        vec_posanzahl.clear();
        macheVector(vec_posanzahl, zeilen[2], 1);
        vec_poskuerzel.clear();
        macheVector(vec_poskuerzel, zeilen[3], 0);

        baumLoeschen();
        vecdummy.clear();
        vec_tabelle.clear();
        String[] pos;
        String id;
        String datum;
        // String aktuell;
        for (int i = 4; i < lang; i++) {
            pos = zeilen[i].split("\\+");
            if (pos[0].equals("EHE") || pos[0].equals("ENF")) {
                if (pos[0].equals("ENF")) {
                    zugabe = 1;
                }
                datum = datumFromEdiDeutsch(pos[5 + zugabe]).replace("'", "");
                vecdummy.add(datum);
                id = RezTools.getIDFromPos(pos[2 + zugabe], preisgruppe, preisvec);
                vecdummy.add(RezTools.getKurzformFromID(id, preisvec));
                vecdummy.add(Double.valueOf(pos[3 + zugabe].replace(",", ".")));
                vecdummy.add(Double.valueOf(pos[4 + zugabe].replace(",", ".")));
                // Hier ganz wichtig die Multiplikation mit der Anzahl
                if (pos.length == (7 + zugabe)) {
                    vecdummy.add((boolean) Boolean.valueOf(true));
                    //// System.out.println("2. Zuzahlmodus = "+(eltern.zuzahlModusDefault ?
                    //// "Normal" : "Bayrisch"));
                    if (eltern.zuzahlModusDefault) {
                        vecdummy.add(Double.valueOf(pos[6 + zugabe].replace(",", ".")
                                                                   .replace("'", "")));
                    } else { // bayrischer Modus
                             // Herr Lehmann: nächste 2 Zeilen müssen freigeschaltet werden für
                             // Einzelkilometer
                        vecdummy.add(Double.valueOf(pos[6 + zugabe].replace(",", ".")
                                                                   .replace("'", ""))
                                * Double.valueOf(pos[3 + zugabe].replace(",", ".")));
                    }
                } else {
                    vecdummy.add((boolean) Boolean.valueOf(false));
                    vecdummy.add(Double.valueOf("0.00"));
                }
                if (i < (lang - 1)) {
                    if (zeilen[i + 1].split("\\+")[0].equals("TXT")) {
                        vecdummy.add(zeilen[i + 1].split("\\+")[1].replace("'", "")
                                                                  .replace("-", ""));
                    } else {
                        vecdummy.add("");
                    }
                } else {
                    vecdummy.add("");
                }
                if ((RezTools.getPreisAktFromID(id, preisgruppe, preisvec)
                             .trim()
                             .replace(".", ",")).equals(pos[4 + zugabe].trim())) {
                    vecdummy.add("aktuell");
                } else {
                    vecdummy.add("alt");
                }
                vecdummy.add(DatFunk.sDatInSQL(datum));
                vecdummy.add(id);
                vecdummy.add((boolean) Boolean.valueOf(false));
                vec_tabelle.add((Vector<Object>) vecdummy.clone());
                vecdummy.clear();
            }

        }

        return ret;
    }

    public static int countWordsFromRowStart(String[] zeilen, String word) {
        int count = 0;
        for (int i = 0; i < zeilen.length; i++) {
            if (zeilen[i].startsWith(word)) {
                count++;
            }
        }
        return count;
    }

    public static int countWords(String text, String word) {
        int count = 0;
        Pattern pat = Pattern.compile(Pattern.quote(word));
        Matcher m = pat.matcher(text);

        while (m.find()) {
            count++;
        }
        return count;
    }

    private void macheVector(Vector vec, String svec, int type) {
        // type 0 = String, type 1 = int;
        String ergebnis = svec.substring(1);
        ergebnis = ergebnis.substring(0, ergebnis.length() - 1);
        ////// System.out.println("Ergebnisstring = "+ergebnis);
        String[] teile = ergebnis.split(",");
        for (int i = 0; i < teile.length; i++) {
            vec.add((type == 0 ? teile[i].trim() : Integer.parseInt(teile[i].trim())));
        }
    }

    /************************/
    private void baumLoeschen() {
        while ((root.getChildCount()) > 0) {
            demoTreeTableModel.removeNodeFromParent((MutableTreeTableNode) root.getChildAt(0));
        }
    }

     void sucheRezept(String rez_nr) {

        aktRezept.init(rez_nr.trim());

        if (aktRezept.getVecSize() <= 0) {
            System.out.println("AbrechnungRezept->sucheRezept:  Abbruch vec_rez.size = 0");
            System.out.println("RezeptVektor = " + aktRezept.getVec_rez());
            vec_rez_valid = true;
            return;
        }
        gebuehrBezahlt = aktRezept.getGebuehrBezahlt();
        gebuehrBetrag = aktRezept.getGebuehrBetrag();
        // 0 1 2 3 4 5
        String cmd = "select  t1.n_name,t1.v_name,t1.geboren,t1.strasse,t1.plz,t1.ort," +
        // 6 7 8 9 10 11 12
                "t1.v_nummer,t1.kv_status,t1.kv_nummer," + "t1.befreit,t1.bef_ab,t1.bef_dat,t1.jahrfrei," +
                // 13 14 15 16
                "t2.nachname,t2.bsnr,t2.arztnum,t3.kassen_nam1 from pat5 t1,arzt t2,kass_adr t3 where t1.pat_intern='"
                + aktRezept.getPatIntern() + "' AND t2.id ='" + aktRezept.getArztId() + "' AND t3.id='"
                + aktRezept.getKtraeger() + "' LIMIT 1";
        //// System.out.println(cmd);
        vec_pat = SqlInfo.holeFelder(cmd);
        //// System.out.println(vec_pat);

        if (vec_pat.size() <= 0) {
            JOptionPane.showMessageDialog(null,
                    "Diesem Rezept ist eine unbrauchbare Kasse und/oder Arzt zugeordnet. Bitte korrigieren");
            vec_rez_valid = true;
            return;
        }

        if (vec_pat.get(0)
                   .get(9)
                   .equals("T")) {
            patAktuellFrei = true;
        } else {
            patAktuellFrei = false;
        }
        if (!vec_pat.get(0)
                    .get(12)
                    .trim()
                    .equals("")) {
            patVorjahrFrei = true;
        } else {
            patVorjahrFrei = false;
        }
        patFreiAb = vec_pat.get(0)
                           .get(10);
        patFreiBis = vec_pat.get(0)
                            .get(11);
        patU18 = DatFunk.Unter18(DatFunk.sHeute(), DatFunk.sDatInDeutsch(vec_pat.get(0)
                                                                                .get(2)));
        vec_rez_valid = true;
    }

    ComponentListener windowResizeHandler = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            keepDayTreeSize(jSplitOU);
            jSplitOU.setDividerLocation(tts.getCurrTageTreeSize());
        }

    };
    public void cleanUp() {
        // Aktionen beim Schließen des Abrechnungsfensters
        cmbkuerzel.removeActionListener(this);
        jSplitOU.removePropertyChangeListener(splitPaneDiverChangeHandler);
        htmlPane.removeHyperlinkListener(this);
        writeTTS2ini();
    }

    /*
     * TageTreeSize Werte in abrechnung.ini schreiben ? in eigene Klasse?
     *
     * @author McM
     */
    private void writeTTS2ini() {
        if (!SystemConfig.hmAbrechnung.get("TTSizeLocked")
                                      .equals("1")) {
            boolean mustsave = false;
            INIFile inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/",
                    "abrechnung.ini");
            String section = "HMGKVRechnung";
            for (Entry<Integer, Integer> e : tts.getHmTageTreeSize()
                                                .entrySet()) {
                int key = e.getKey();
                String val = e.getValue()
                              .toString();
                System.out.println("save TTS_" + key + " = " + val);
                if (tts.getTTSchanged(key)) {
                    inif.setStringProperty(section, "TTS_" + key, val, null);
                    mustsave = true;
                }
            }
            if (mustsave) {
                INITool.saveIni(inif);
            }
        }
    }
}

class MyDateCellEditor extends AbstractCellEditor implements TableCellEditor {
    // This is the component that will handle the editing of the cell value
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    JComponent component = new JXDatePicker();
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    // This method is called when a cell value is edited by the user.
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex,
            int vColIndex) {
        if (isSelected) {
            ((JXDatePicker) component).getEditor()
                                      .setEditable(false);
            // cell (and perhaps other cells) are selected }
            // Configure the component with the specified value
            // ((JXDatePicker)component).setDate((Date) table.getValueAt(rowIndex,vColIndex)
            // );
            ((JXDatePicker) component).setDate((Date) value);
            ((JXDatePicker) component).setVisible(true);
            // Return the configured component
            return component;
        } // This method is called when editing is completed.
        return null;

    }

    // 'value' is value contained in the cell located at (rowIndex, vColIndex)
    // It must return the new value to be stored in the cell.
    @Override
    public Object getCellEditorValue() {
        return ((JXDatePicker) component).getDate();
    }
}

class MyDate2CellEditor extends AbstractCellEditor implements TableCellEditor {
    /**
     *
     */
    private static final long serialVersionUID = 7964506030501479969L;

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return null;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }
}
