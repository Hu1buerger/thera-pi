package org.therapi.reha.patient;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.IconValues;
import org.jdesktop.swingx.renderer.MappedValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.Colors;
import CommonTools.DatFunk;
import CommonTools.DateTimeFormatters;
import CommonTools.JCompTools;
import CommonTools.SqlInfo;
import dialoge.ToolsDialog;
import environment.Path;
import gui.Cursors;
import hauptFenster.Reha;
import jxTableTools.DateTableCellEditor;
import jxTableTools.TableTool;
import mandant.IK;
import oOorgTools.OOTools;
import patientenFenster.HistorDaten;
import patientenFenster.KeinRezept;
import patientenFenster.rezepte.RezeptDatenDarstellen;
import patientenFenster.rezepte.RezeptFensterTools;
import rechteTools.Rechte;
import rezept.Rezept;
import rezept.RezeptDto;
import stammDatenTools.RezTools;
import stammDatenTools.ZuzahlTools.ZZStat;
import systemEinstellungen.SystemConfig;
import systemTools.IconListRenderer;

/**
 * Replaces the historie.java and uses the Rezept-class
 *
 */
public class RezepteHistorisch extends JXPanel implements ActionListener {
    private static final long serialVersionUID = -7023226994175632749L;
    private static final Logger logger = LoggerFactory.getLogger(RezepteHistorisch.class);
    
    IK ik;
    JXPanel leerPanel = null;
    public HistorPanel vollPanel = null;
    JXPanel wechselPanel = null;
    public JLabel anzahlTermine = null;
    public JLabel anzahlHistorie = null;
    public String aktPanel = "";
    public JXTable tabHistRezepte = null;
    public JXTable tabHistTerm = null;
    public RezeptHistTableModel tblmodHistRez;
    public RezeptHistTermTableModel tblmodHistTerm;
    public TableCellEditor tbl = null;
    public boolean rezneugefunden = false;
    public boolean neuDlgOffen = false;
    public String[] indphysio = null;
    public String[] indergo = null;
    public String[] indlogo = null;
    public RezeptDatenDarstellen jpan1 = null;
    
    private JButton btnArztBericht;
    private JButton btnTools;
    private List<JButton> allUsedTBButtons;
    private JButton btnNeu;
    private JButton btnSort;
    private JButton btnDel;
    private List<JButton> allUsedTerminTBButtons;

    public JButton[] histbut = { null, null, null, null };
    public static boolean inRezeptDaten = false;

    // int idInTable = 7;

    /**
     * Replaces the old Historie.java, now with IK param and Rezept-class
     * @param Ik
     */
    public RezepteHistorisch(IK Ik) {
        super();
        ik = Ik;
        setOpaque(false);
        setLayout(new BorderLayout());
        /******** zuerst das Leere Panel basteln **************/
        leerPanel = new KeinRezept("noch keine Rezepte in der Historie f\u00fcr diesen Patient");
        leerPanel.setName("leerpanel");
        leerPanel.setOpaque(false);

        /******** dann das volle **************/
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

        allesrein.add(getToolbar(), cc.xy(2, 2));
        // allesrein.add(getTabelle(),cc.xy(2, 4));
        allesrein.add(wechselPanel, cc.xy(2, 6));

        add(JCompTools.getTransparentScrollPane(allesrein), BorderLayout.CENTER);
        validate();

        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    vollPanel = new HistorPanel();
                    FormLayout vplay = new FormLayout("fill:0:grow(0.75),5dlu,fill:0:grow(0.25),5dlu",
                            "13dlu,53dlu,5dlu,fill:0:grow(1.00),0dlu");
                    CellConstraints vpcc = new CellConstraints();
                    vollPanel.setLayout(vplay);
                    vollPanel.setOpaque(false);
                    vollPanel.setBorder(null);

                    Font font = new Font("Tahome", Font.PLAIN, 11);
                    anzahlHistorie = new JLabel("Anzahl Rezepte in Historie: 0");
                    anzahlHistorie.setFont(font);
                    vollPanel.add(anzahlHistorie, vpcc.xy(1, 1));

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

                    jpan1 = new RezeptDatenDarstellen(null, false, ik);
                    vollPanel.add(jpan1, vpcc.xyw(1, 4, 1));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Fehler im Modul Historie");
                }

                return null;
            }

        }.execute();

    }

    public JScrollPane getTermine() {
        List<String> terminListe = new ArrayList<String>();

        tblmodHistTerm = new RezeptHistTermTableModel(terminListe);
        tblmodHistTerm.addTableModelListener(e -> actionTableModelChange(e));
        /*
        String[] column = { "Beh.Datum", "Behandler", "Text", "Beh.Art", "" };
        dtermm.setColumnIdentifiers(column);
        */
        tabHistTerm = new JXTable(tblmodHistTerm);
        tabHistTerm.setHighlighters(HighlighterFactory.createSimpleStriping(Colors.PiOrange.alpha(0.25f)));
        tabHistTerm.setDoubleBuffered(true);
        tabHistTerm.addPropertyChangeListener(e -> actionTablePropertyChange(e));
        tabHistTerm.setEditable(false);
        tabHistTerm.setSortable(false);
        tabHistTerm.setSelectionMode(0);
        tabHistTerm.setHorizontalScrollEnabled(true);
        tbl = new DateTableCellEditor();
        tabHistTerm.getColumnModel()
                  .getColumn(RezeptHistTermTableModel.HISTTERMTABCOL_BEHDAT)
                  .setCellEditor(tbl);
        tabHistTerm.getColumn(RezeptHistTermTableModel.HISTTERMTABCOL_BEHDAT)
                  .setMinWidth(60);
        tabHistTerm.getColumn(RezeptHistTermTableModel.HISTTERMTABCOL_BEHDAT)
                  .setCellRenderer(new DateCellRenderer());
        tabHistTerm.getColumn(RezeptHistTermTableModel.HISTTERMTABCOL_BEHDLER)
                  .setMinWidth(60);
        tabHistTerm.getColumn(RezeptHistTermTableModel.HISTTERMTABCOL_TEXT)
                  .setMinWidth(40);
        tabHistTerm.getColumn(RezeptHistTermTableModel.HISTTERMTABCOL_BEHART)
                  .setMinWidth(40);
        tabHistTerm.getColumn(RezeptHistTermTableModel.HISTTERMTABCOL_SECRET)
                  .setMinWidth(0);
        tabHistTerm.getColumn(RezeptHistTermTableModel.HISTTERMTABCOL_SECRET)
                  .setMaxWidth(0);
        tabHistTerm.setOpaque(true);
        tabHistTerm.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                }
                if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                }
            }

        });
        tabHistTerm.validate();
        tabHistTerm.setName("AktTerm");
        JScrollPane termscr = JCompTools.getTransparentScrollPane(tabHistTerm);
        termscr.getVerticalScrollBar()
               .setUnitIncrement(15);
        return termscr;
    }

    public JXPanel getTabelle() {
        JXPanel dummypan = new JXPanel(new BorderLayout());
        dummypan.setOpaque(false);
        dummypan.setBorder(null);
        tblmodHistRez = new RezeptHistTableModel();
        // String[] column = { "Rezept-Nr.", "bezahlt", "Rez-Datum", "angelegt am", "sp\u00e4t.Beginn", "Pat-Nr.", "Indi.Schl.",
        //        "ID" };
        // dtblm.setColumnIdentifiers(column);
        tabHistRezepte = new JXTable(tblmodHistRez);
        tabHistRezepte.setHighlighters(HighlighterFactory.createSimpleStriping(Colors.PiOrange.alpha(0.25f)));
        tabHistRezepte.setDoubleBuffered(true);
        tabHistRezepte.setEditable(false);
        tabHistRezepte.setSortable(false);
        tabHistRezepte.getColumn(RezeptHistTableModel.HISTREZTABCOL_NR)
                   .setMaxWidth(75);
        TableCellRenderer renderer = new DefaultTableRenderer(new MappedValue(StringValues.EMPTY, IconValues.ICON),
                JLabel.CENTER);
        tabHistRezepte.getColumn(RezeptHistTableModel.HISTREZTABCOL_BEZAHLT)
                   .setCellRenderer(renderer);
        tabHistRezepte.getColumn(RezeptHistTableModel.HISTREZTABCOL_BEZAHLT)
                   .setMaxWidth(45);
        tabHistRezepte.getColumn(RezeptHistTableModel.HISTREZTABCOL_REZDAT)
                   .setMaxWidth(75);
        tabHistRezepte.getColumn(RezeptHistTableModel.HISTREZTABCOL_REZDAT)
                   .setCellRenderer(new DateCellRenderer());
        tabHistRezepte.getColumn(RezeptHistTableModel.HISTREZTABCOL_ANGELEGTAM)
                   .setMaxWidth(75);
        tabHistRezepte.getColumn(RezeptHistTableModel.HISTREZTABCOL_ANGELEGTAM)
                   .setCellRenderer(new DateCellRenderer());
        tabHistRezepte.getColumn(RezeptHistTableModel.HISTREZTABCOL_SPAETESTERBEGINN)
                   .setCellRenderer(new DateCellRenderer());
        // Don't use this unprepared - it will screw up the index - invisible columns don't count towards # of cols...
        // tabhistorie.getColumnExt(RezeptHistTableModel.HISTREZTABCOL_PATNR).setVisible(false);
        
        tabHistRezepte.getColumn(RezeptHistTableModel.HISTREZTABCOL_PATNR)
                   .setMinWidth(0);
        tabHistRezepte.getColumn(RezeptHistTableModel.HISTREZTABCOL_PATNR)
                   .setMaxWidth(0);
        // Don't use this unprepared - it will screw up the index - invisible columns don't count towards # of cols...
        // tabhistorie.getColumnExt(RezeptHistTableModel.HISTREZTABCOL_ID).setVisible(false);
        tabHistRezepte.getColumn(RezeptHistTableModel.HISTREZTABCOL_ID)
                   .setMinWidth(0);
        tabHistRezepte.getColumn(RezeptHistTableModel.HISTREZTABCOL_ID)
                   .setMaxWidth(0);
        tabHistRezepte.validate();
        tabHistRezepte.setName("AktRez");
        tabHistRezepte.setSelectionMode(0);
        tabHistRezepte.getSelectionModel()
                   .addListSelectionListener(new HistorRezepteListSelectionHandler());
        tabHistRezepte.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                long zeit = System.currentTimeMillis();
                if (arg0.getClickCount() == 2) {
                    while (inRezeptDaten) {
                        try {
                            Thread.sleep(20);
                            if ((System.currentTimeMillis() - zeit) > 2000) {
                                return;
                            }

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        tabHistRezepte.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    arg0.consume();
                }
                if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    arg0.consume();
                }
            }

        });
        dummypan.setPreferredSize(new Dimension(0, 100));
        JScrollPane aktrezscr = JCompTools.getTransparentScrollPane(tabHistRezepte);
        aktrezscr.getVerticalScrollBar()
                 .setUnitIncrement(15);
        dummypan.add(aktrezscr, BorderLayout.CENTER);
        dummypan.validate();
        return dummypan;
    }

    public void setzeHistoriePanelAufNull(boolean aufnull) {
        if (aufnull) {
            if (aktPanel.equals("vollPanel")) {
                wechselPanel.remove(vollPanel);
                wechselPanel.add(leerPanel);
                aktPanel = "leerPanel";
                for (JButton btn : allUsedTBButtons) {
                    btn.setEnabled(false);
                }
            }
        } else {
            if (aktPanel.equals("leerPanel")) {
                wechselPanel.remove(leerPanel);
                wechselPanel.add(vollPanel);
                aktPanel = "vollPanel";
                for (JButton btn : allUsedTBButtons) {
                    btn.setEnabled(true);
                }
                /*
                for (int i = 0; i < 4; i++) {
                    try {
                        histbut[i].setEnabled(true);
                    } catch (Exception ex) {

                    }
                }
                */
            }
        }
    }

    // Toolbars:
    /**
     * Creates a toolbar with buttons related to Rezepte
     * @return A populate JToolBar (Arztbericht + Tools)
     */
    public JToolBar getToolbar() {
        JToolBar jtb = new JToolBar();
        allUsedTBButtons = new LinkedList<JButton>();

        jtb.setOpaque(false);
        jtb.setRollover(true);
        jtb.setBorder(null);
        jtb.setOpaque(false);

        btnArztBericht = new JButton();
        btnArztBericht.setIcon(SystemConfig.hmSysIcons.get("arztbericht"));
        btnArztBericht.setToolTipText("Nachtr\u00e4glich Arztbericht Rezept erstellen");
        btnArztBericht.setActionCommand("arztbericht");
        btnArztBericht.addActionListener(e -> actionArztBericht(e));
        jtb.add(btnArztBericht);
        { allUsedTBButtons.add(btnArztBericht); }

        jtb.addSeparator(new Dimension(30, 0));

        btnTools = new JButton();
        btnTools.setIcon(SystemConfig.hmSysIcons.get("tools"));
        btnTools.setToolTipText("Werkzeugkiste f\u00fcr die Historie");
        btnTools.setActionCommand("werkzeuge");
        btnTools.addActionListener(e -> actionTools(e));
        jtb.add(btnTools);
        { allUsedTBButtons.add(btnTools); }
        
        for ( JButton btn : allUsedTBButtons) {
            btn.setEnabled(false);
        }
        return jtb;
    }

    /**
     * Creates a toolbar related to the Termine of a Rezept
     * @return A populate JToolBar with buttons to perform actions on Termine
     */
    public JToolBar getTerminToolbar() {
        allUsedTerminTBButtons = new LinkedList<JButton>();
        JToolBar jtb = new JToolBar();
        
        btnNeu = new JButton();
        btnNeu.setIcon(SystemConfig.hmSysIcons.get("neu"));
        btnNeu.setToolTipText("Neuen Termin eintragen");
        btnNeu.setActionCommand("terminplus");
        btnNeu.addActionListener(e -> actionTerminNeu(e));
        btnNeu.setEnabled(false);
        jtb.add(btnNeu);
        { allUsedTerminTBButtons.add(btnNeu); }
        
        btnDel = new JButton();
        btnDel.setIcon(SystemConfig.hmSysIcons.get("delete"));
        btnDel.setToolTipText("Termin l\u00f6schen");
        btnDel.setActionCommand("terminminus");
        btnDel.addActionListener(e -> actionTerminDel(e));
        btnDel.setEnabled(false);
        jtb.add(btnDel);
        { allUsedTerminTBButtons.add(btnDel); }
        
        btnSort = new JButton();
        btnSort.setIcon(SystemConfig.hmSysIcons.get("sort"));
        btnSort.setToolTipText("Termine nach Datum sortieren");
        btnSort.setActionCommand("terminsortieren");
        btnSort.addActionListener(e -> actionTerminSort(e));
        jtb.add(btnSort);
        { allUsedTerminTBButtons.add(btnSort); }

        return jtb;

    }

    /*
     * Seems like dead meat...
     *
    public void macheTabelle(Vector<String> vec) {
        if (vec.size() > 0) {
            dtblm.addRow(vec);
        } else {
            dtblm.setRowCount(0);
            tabhistorie.validate();
        }

    }
    */

    private void holeEinzelTermine(int row, Rezept Rez) {
        Rezept rez = null;
        if (Rez == null) {
            /*
            xvec = SqlInfo.holeSatz("lza", "termine", "id='" + tabhistorie.getValueAt(row, RezeptHistTableModel.HISTREZTABCOL_ID) + "'",
                    Arrays.asList(new String[] {}));
            */
            rez = new RezeptDto(ik).getHistorischesRezeptByRezNr(
                        (String) tabHistRezepte.getValueAt(row, RezeptHistTableModel.HISTREZTABCOL_NR))
                                                                                    .orElse(null);
        } else {
            rez = new Rezept(Rez);
        }
        if (rez == null)
            return;

        String terms = rez.getTermine();
        if (terms == null || terms.equals("")) {
            tblmodHistTerm.emptyTable();
            tabHistTerm.validate();
            anzahlTermine.setText("Anzahl Termine: 0");
            return;
        }
        
        String[] tlines = terms.split("\n");
        int lines = tlines.length;
        /*
        dtermm.emptyTable();
        for (String line : tlines) {
            String[] termEinzelteile = line.split("@");
        }
        for (int i = 0; i < lines; i++) {
            Vector<String> tvec = new Vector<String>();
            String[] terdat = tlines[i].split("@");
            int ieinzel = terdat.length;
            for (int y = 0; y < ieinzel; y++) {
                if (y == 0) {
                    tvec.add((terdat[y].trim()
                                       .equals("") ? "  .  .    " : terdat[y]));
                } else {
                    tvec.add(terdat[y]);
                }
            }
            dtermm.addRow( tvec);
        }
        */
        //FIXME: need to get the data displayed in table
        tblmodHistTerm.updateTermine(terms);
        anzahlTermine.setText("Anzahl Termine: " + lines);

    }

    /******************
     *
     *
     */
    public void doRechneAlles() {
        double gesamtHistor = 0.00; // new Double(0.00);
        double gesamtAkt = 0.00; // new Double(0.00);
        gesamtHistor = doRechneHistorie("lza");
        gesamtAkt = doRechneHistorie("verordn");
        double gesamtumsatz = gesamtHistor + gesamtAkt;
        DecimalFormat dfx = new DecimalFormat("0.00");
        String msg = "<html><font font-family='Courier New'>Gesamtumsatz von Patient --> "
                + Reha.instance.patpanel.patDaten.get(2) + ", " + Reha.instance.patpanel.patDaten.get(3)
                + "&nbsp;&nbsp;&nbsp;&nbsp;" + "<br><br>Historie&nbsp;=&nbsp;" + dfx.format(gesamtHistor) + " EUR"
                + "<br>Aktuell&nbsp;&nbsp;=&nbsp;" + dfx.format(gesamtAkt) + " EUR"
                + "<br><br><p><b>Gesamt = <font align='center' color='#FF0000'>" + dfx.format(gesamtumsatz)
                + " EUR </font></b></p><br><br></font>";

        JOptionPane optionPane = new JOptionPane();
        optionPane.setMessage(msg);
        optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
        String xtitel = "";
        if (gesamtumsatz < 1000.00) {
            xtitel = "k\u00f6nnte besser sein...";
        } else if (gesamtumsatz > 1000.00 && gesamtumsatz < 2000.00) {
            xtitel = "geht doch...";
        } else if (gesamtumsatz > 2000.00) {
            xtitel = "'Sternle-Patient' bitte warmhalten...";
        }
        JDialog dialog = optionPane.createDialog(null, xtitel);
        dialog.setVisible(true);

    }

    public double doRechneHistorie(String db) {
        int rows = tabHistRezepte.getRowCount();
        String felder = "anzahl1,anzahl2,anzahl3,anzahl3,preise1,preise2,preise3,preise4,hausbes";
        Double gesamtumsatz = new Double(0.00);
        if (db.equals("lza")) {
            if (rows <= 0) {
                return new Double(0.00);
            }
            String suchrezid = null;
            String suchrez = null;
            for (int i = 0; i < rows; i++) {
                suchrezid = String.valueOf(tabHistRezepte.getValueAt(i, RezeptHistTableModel.HISTREZTABCOL_ID));
                suchrez = (String) tabHistRezepte.getValueAt(i, RezeptHistTableModel.HISTREZTABCOL_NR);
                Vector<String> vec = null;
                /*********************/
                vec = SqlInfo.holeSatz(db, felder, "id='" + suchrezid + "'", Arrays.asList(new String[] {}));
                if (vec.get(8)
                       .equals("T")) {
                    vec = SqlInfo.holeFeld("select sum(gesamt) from faktura where rez_nr = '" + suchrez + "'");
                    if (!vec.get(0)
                            .equals("")) {
                        gesamtumsatz = gesamtumsatz + Double.parseDouble(vec.get(0));
                        // System.out.println("-1- "+vec);
                    } else {
                        vec = SqlInfo.holeSatz(db, felder, "id='" + suchrezid + "'", Arrays.asList(new String[] {}));
                        if (vec.size() > 0) {
                            BigDecimal preispos = BigDecimal.valueOf(new Double(0.00));
                            for (int anz = 0; anz < 4; anz++) {
                                preispos = BigDecimal.valueOf(new Double(vec.get(anz + 4)))
                                                     .multiply(BigDecimal.valueOf(new Double(vec.get(anz))));
                                gesamtumsatz = gesamtumsatz + preispos.doubleValue();
                            }
                        }
                    }
                } else {
                    vec = SqlInfo.holeSatz(db, felder, "id='" + suchrezid + "'", Arrays.asList(new String[] {}));
                    if (vec.size() > 0) {
                        BigDecimal preispos = BigDecimal.valueOf(new Double(0.00));
                        for (int anz = 0; anz < 4; anz++) {
                            preispos = BigDecimal.valueOf(new Double(vec.get(anz + 4)))
                                                 .multiply(BigDecimal.valueOf(new Double(vec.get(anz))));
                            gesamtumsatz = gesamtumsatz + preispos.doubleValue();
                        }
                    }
                }
                /*********************/
                /*
                 * vec =
                 * SqlInfo.holeFeld("select sum(gesamt) from faktura where rez_nr = '"+suchrez+
                 * "'");
                 *
                 * if(! vec.get(0).equals("")){ gesamtumsatz =
                 * gesamtumsatz+Double.parseDouble(vec.get(0)); System.out.println("-1- "+vec);
                 * }else{ vec = SqlInfo.holeSatz(db, felder, "id='"+suchrezid+"'",
                 * Arrays.asList(new String[] {})); if(vec.size() > 0){ BigDecimal preispos =
                 * BigDecimal.valueOf(new Double(0.00)); for(int anz = 0;anz <4;anz++){ preispos
                 * = BigDecimal.valueOf(new Double((String)vec.get(anz+4))).multiply(
                 * BigDecimal.valueOf(new Double((String)vec.get(anz)))) ; gesamtumsatz =
                 * gesamtumsatz+preispos.doubleValue(); } } }
                 */
                /*********************/
            }
        } else {
            String cmd = "pat_intern='" + Reha.instance.patpanel.aktPatID + "'";
            Vector<Vector<String>> vec = SqlInfo.holeSaetze(db, "id,rez_nr", cmd, Arrays.asList(new String[] {}));
            rows = vec.size();
            String suchrezid = null;
            String suchrez = null;
            Object[] hbscheiss = { null, null, null };
            Vector<String> vhbscheiss = null;
            for (int i = 0; i < rows; i++) {
                suchrezid = (String) ((Vector<?>) vec.get(i)).get(0);// (String)tabhistorie.getValueAt(i,6);
                suchrez = (String) ((Vector<?>) vec.get(i)).get(1);
                Vector<String> vec2 = SqlInfo.holeSatz(db, felder, "id='" + suchrezid + "'",
                        Arrays.asList(new String[] {}));
                if (vec2.size() > 0) {
                    BigDecimal preispos = BigDecimal.valueOf(new Double(0.00));
                    for (int anz = 0; anz < 4; anz++) {
                        preispos = BigDecimal.valueOf(new Double(vec2.get(anz + 4)))
                                             .multiply(BigDecimal.valueOf(new Double(vec2.get(anz))));
                        gesamtumsatz = gesamtumsatz + preispos.doubleValue();
                    }
                }
                try {
                    if (vec2.get(8)
                            .equals("T")) {
                        vhbscheiss = SqlInfo.holeSatz("verordn", " * ", "rez_nr = '" + suchrez + "'",
                                Arrays.asList(new String[] {}));

                        hbscheiss = RezTools.ermittleHBwert(vhbscheiss);
                        gesamtumsatz = gesamtumsatz + ((Double) hbscheiss[0] != null ? (Double) hbscheiss[0] : 0.00);
                        gesamtumsatz = gesamtumsatz + ((Double) hbscheiss[1] != null ? (Double) hbscheiss[1] : 0.00);
                        // System.out.println(suchrez+ " -
                        // "+hbscheiss[0]+"/"+hbscheiss[1]+"/"+hbscheiss[2]);

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
        return gesamtumsatz;
    }

    /******************
     *
     *
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        String cmd = arg0.getActionCommand();
        /*
        if (cmd.equals("arztbericht")) {
            if (!Rechte.hatRecht(Rechte.Historie_thbericht, true)) {
                return;
            }
            if (aktPanel.equals("leerPanel")) {
                JOptionPane.showMessageDialog(null, "Ich sag jetzt nix....\n\n"
                        + "....au\u00dfer - und f\u00fcr welches der nicht vorhandenen Rezepte in der Historie wollen Sie einen Therapiebericht erstellen....");
                return;
            }
            boolean neuber = true;
            int berid = 0;
            String xreznr;
            String xverfasser = "";
            int currow = tabHistRezepte.getSelectedRow();
            if (currow >= 0) {
                xreznr = (String) tabHistRezepte.getValueAt(currow, 0);
            } else {
                xreznr = "";
            }

            int iexistiert = Reha.instance.patpanel.berichte.berichtExistiert(xreznr);
            if (iexistiert > 0) {
                xverfasser = Reha.instance.patpanel.berichte.holeVerfasser();
                neuber = false;
                berid = iexistiert;
                String meldung = "<html>F\u00fcr das Historienrezept <b>" + xreznr
                        + "</b> existiert bereits ein Bericht.<br>\nVorhandener Bericht wird jetzt ge\u00f6ffnet";
                JOptionPane.showMessageDialog(null, meldung);
            }

            final boolean xneuber = neuber;
            final String xxreznr = xreznr;
            final int xberid = berid;
            final int xcurrow = currow;
            final String xxverfasser = xverfasser;
            ArztBericht ab = new ArztBericht(null, "arztberichterstellen", xneuber, xxreznr, xberid, 1, xxverfasser, "",
                    xcurrow);
            ab.setModal(true);
            ab.setLocationRelativeTo(null);
            ab.setVisible(true);
            ab = null;
            return;
        } else
        */
        if (cmd.equals("historinfo")) {
            return;
        // can't find an Object defining this string...
        } else if (cmd.equals("historumsatz")) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    doRechneAlles();
                    return null;
                }
            }.execute();
            return;
        // Likewise - no ref. in entire code...
        } else if (cmd.equals("historprinttage")) {
            return;
        }
        if (cmd.equals("werkzeuge")) {
            new ToolsDlgHistorie("", btnTools.getLocationOnScreen());
        }

    }

    void setRezeptDaten() {
        int row = this.tabHistRezepte.getSelectedRow();
        if (row >= 0) {
            final int xrow = row;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String reznr = (String) tabHistRezepte.getValueAt(xrow, RezeptHistTableModel.HISTREZTABCOL_NR);
                    String id = String.valueOf(tabHistRezepte.getValueAt(xrow, RezeptHistTableModel.HISTREZTABCOL_ID));
                    // jpan1.setRezeptDaten(reznr, id);
                    logger.debug("Calling rezDataUpdate with " + reznr + " could use " + id);
                    jpan1.updateDatenPanel(reznr, false);
                }
            });

        }
    }

    private String macheHtmlTitel(int anz, String titel) {
        String ret = titel + " - " + Integer.toString(anz);
        return ret;
    }

    /*************************************************/

    public void holeRezepte(String patint, String rez_nr) {
        final String xpatint = patint;
        final String xrez_nr = rez_nr;
        /*
        Vector<Vector<String>> vec = SqlInfo.holeSaetze("lza",
                "rez_nr,zzstatus,DATE_FORMAT(rez_datum,'%d.%m.%Y') AS drez_datum,DATE_FORMAT(datum,'%d.%m.%Y') AS datum,"
                        + "DATE_FORMAT(lastdate,'%d.%m.%Y') AS datum,pat_intern,indikatschl,id",
                "pat_intern='" + xpatint + "' ORDER BY rez_datum DESC", Arrays.asList(new String[] {}));
        */

        List<Rezept> rezepte = new RezeptDto(ik).getHistorischeRezepteByPatNr(Integer.parseInt(xpatint));
        int anz = rezepte.size();
        ZZStat iconKey;
        tblmodHistRez.emptyTable();
        for (Rezept rez : rezepte) {
            if(rez == null)     // shouldn't need this on empty list...
                break;
            int zzBild = rez.getZZStatus();
            final String testreznum = rez.getRezNr();
            iconKey = stammDatenTools.ZuzahlTools.getIconKey(zzBild, testreznum);
            tblmodHistRez.addRow(rez);
            // FIXME: Disabled until I can figure out a nice way for this:
            // dtblm.setValueAt(stammDatenTools.ZuzahlTools.getZzIcon(iconKey), i, 1);
            
        }
        
        try {
            Reha.instance.patpanel.getTab()
                                  .setTitleAt(1, macheHtmlTitel(anz, "Rezept-Historie"));
        } catch (Exception extiming) {
            System.out.println("Timingprobleme beim setzen des Reitertitels - Reiter: Historie");
        }
        if (anz > 0) {
            new Thread() {
                @Override
                public void run() {
                    holeEinzelTermine(0, null);
                }
            }.start();
            setzeHistoriePanelAufNull(false);
            if (xrez_nr.length() > 0) {
                int row = 0;
                rezneugefunden = true;
                for (int ii = 0; ii < anz; ii++) {
                    if (tabHistRezepte.getValueAt(ii, 0)
                                   .equals(xrez_nr)) {
                        row = ii;
                        break;
                    }

                }
                tabHistRezepte.setRowSelectionInterval(row, row);
                // jpan1.setRezeptDaten((String) tabHistRezepte.getValueAt(row, 0),
                //        String.valueOf(tabHistRezepte.getValueAt(row, RezeptHistTableModel.HISTREZTABCOL_ID)));
                String reznr = (String) tabHistRezepte.getValueAt(row, RezeptHistTableModel.HISTREZTABCOL_NR);
                String id = String.valueOf(tabHistRezepte.getValueAt(row, RezeptHistTableModel.HISTREZTABCOL_ID));
                logger.debug("Calling rezDataUpdate with " + reznr + " could use " + id);
                jpan1.updateDatenPanel((String) tabHistRezepte.getValueAt(row, RezeptHistTableModel.HISTREZTABCOL_NR), false);
                tabHistRezepte.scrollRowToVisible(row);
                holeEinzelTermine(row, null);
            } else {
                rezneugefunden = true;
                tabHistRezepte.setRowSelectionInterval(0, 0);
                // jpan1.setRezeptDaten((String) tabHistRezepte.getValueAt(0, 0),
                //        String.valueOf(tabHistRezepte.getValueAt(0, RezeptHistTableModel.HISTREZTABCOL_ID)));
                String reznr = (String) tabHistRezepte.getValueAt(0, RezeptHistTableModel.HISTREZTABCOL_NR);
                String id = String.valueOf(tabHistRezepte.getValueAt(0, RezeptHistTableModel.HISTREZTABCOL_ID));
                logger.debug("Calling rezDataUpdate with " + reznr + " could use " + id);
                jpan1.updateDatenPanel((String) tabHistRezepte.getValueAt(0, RezeptHistTableModel.HISTREZTABCOL_NR), false);
            }
            anzahlHistorie.setText("Anzahl Rezepte in Historie: " + anz);
            wechselPanel.revalidate();
            wechselPanel.repaint();
        } else {
            setzeHistoriePanelAufNull(true);
            anzahlHistorie.setText("Anzahl Rezepte in Historie: " + anz);
            wechselPanel.revalidate();
            wechselPanel.repaint();
            // dtblm.setRowCount(0);
            tblmodHistRez.emptyTable();
            tblmodHistTerm.emptyTable();
        }
    }

    private void doUebertrag() {
        int row = tabHistRezepte.getSelectedRow();
        if (row >= 0) {
            try {
                int mod = tabHistRezepte.convertRowIndexToModel(row);
                String rez_nr = tblmodHistRez.getValueAt(mod, RezeptHistTableModel.HISTREZTABCOL_NR)
                                     .toString()
                                     .trim();
                // SqlInfo.transferRowToAnotherDB("lza", "verordn", "rez_nr", rez_nr, true,
                //        Arrays.asList(new String[] { "id" }));
                // String xcmd = "update verordn set abschluss='F' where rez_nr='" + rez_nr + "' LIMIT 1";
                //SqlInfo.sqlAusfuehren(xcmd);
                // SqlInfo.sqlAusfuehren("delete from lza where rez_nr='" + rez_nr + "'");
                try {
                    RezeptDto rDto = new RezeptDto(ik);
                    Rezept rez = rDto.getHistorischesRezeptByRezNr(rez_nr).get();
                    rez.setAbschluss(false);
                    // TODO: if we didn't alter lza to allow null-lastdate, the following can stay
                    if (rez.getLastDate().equals(RezeptFensterTools.calcLatestStartDate(rez)))
                        rez.setLastDate(null);
                    rDto.rezeptInDBSpeichern(rez);
                    rDto.deleteHistorieByRezNr(rez_nr);
                } catch(Exception e) {
                    logger.error("Could not transfer Rezept " + rez_nr + " from hist to aktuelle db");
                    // TODO: msg to user?
                }
                // TableTool.loescheRowAusModel(tabHistRezepte, row);
                tblmodHistRez.removeRow(row);
                tblmodHistTerm.emptyTable();
                String htmlstring = "<html><b><font color='#ff0000'>Achtung!!!!</font><br>"
                        + "Wenn Sie das Rezept lediglich zur Ansicht in die aktuelle Rezepte transferieren<br>"
                        + "sollten Sie die zugeh\u00f6rigen Fakturadaten <font color='#ff0000'>nicht l\u00f6schen.</font><br><br>"
                        + "Wenn Sie das Rezept jedoch <u>neu abrechnen</u> wollen, sollten Sie<br>"
                        + "die Fakturadaten <font color='#ff0000'>unbedingt l\u00f6schen</font>.<br><br>"
                        + "Wollen Sie die Fakturadaten jetzt l\u00f6schen?</b></html>";
                int anfrage = JOptionPane.showConfirmDialog(null, htmlstring, "Achtung wichtige Benutzeranfrage",
                        JOptionPane.YES_NO_OPTION);
                if (anfrage == JOptionPane.YES_OPTION) {
                    SqlInfo.sqlAusfuehren("delete from faktura where rez_nr='" + rez_nr + "'");
                    SqlInfo.sqlAusfuehren("delete from fertige where rez_nr='" + rez_nr + "'");
                    if (Reha.instance.abrechnungpanel != null) {
                        /*
                         * String[] diszis = null; if(SystemConfig.mitRs){ diszis = new String[]
                         * {"Physio","Massage","Ergo","Logo","Podo","Rsport","Ftrain"}; }else{ diszis =
                         * new String[] {"Physio","Massage","Ergo","Logo","Podo"}; }
                         *
                         * String aktDisziplin =
                         * diszis[Reha.instance.abrechnungpanel.cmbDiszi.getSelectedIndex()];
                         */
                        String aktDisziplin = Reha.instance.abrechnungpanel.disziSelect.getCurrDisziKurz();
                        if (RezTools.getDisziplinFromRezNr(rez_nr)
                                    .equals(aktDisziplin)) {
                            Reha.instance.abrechnungpanel.einlesenErneuern(rez_nr);
                        }
                    }
                }
                // new sqlTools.ExUndHop().setzeStatement("delete from faktura where
                // rez_nr='"+rez_nr+"'");
                setzeKarteiLasche();
                Reha.instance.patpanel.aktRezept.holeRezepte(Reha.instance.patpanel.patDaten.get(29), "");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null,
                    "Kein Historien-Rezept f\u00fcr den \u00dcbertrag in aktuelle Rezepte ausgew\u00e4hlt!");
        }

    }

    public void setzeKarteiLasche() {
        if (tabHistRezepte.getRowCount() == 0) {
            holeRezepte(Reha.instance.patpanel.patDaten.get(29), "");
            Reha.instance.patpanel.multiTab.setTitleAt(1, macheHtmlTitel(tabHistRezepte.getRowCount(), "Rezept-Historie"));
        } else {
            Reha.instance.patpanel.multiTab.setTitleAt(1, macheHtmlTitel(tabHistRezepte.getRowCount(), "Rezept-Historie"));
        }
    }

    private void doTageDrucken() {
        int akt = this.tabHistRezepte.getSelectedRow();
        if (akt < 0) {
            JOptionPane.showMessageDialog(null, "Kein Historien-Rezept f\u00fcr \u00dcbertrag in Clipboard ausgew\u00e4hlt");
            return;
        }
        String stage = "Rezeptnummer: " + tabHistRezepte.getValueAt(akt, RezeptHistTableModel.HISTREZTABCOL_NR)
                                                     .toString()
                + " - Rezeptdatum: " + ((LocalDate) tabHistRezepte.getValueAt(akt, RezeptHistTableModel.HISTREZTABCOL_REZDAT)
                                                  ).format(DateTimeFormatters.dMYYYYmitPunkt)
                + "\n";
        int tage = tblmodHistTerm.getRowCount();

        for (int i = 0; i < tage; i++) {
            stage = stage + Integer.toString(i + 1) + "\t" + ((LocalDate) tblmodHistTerm
                                            .getValueAt(i, RezeptHistTermTableModel.HISTTERMTABCOL_BEHDAT))
                                                                                .format(DateTimeFormatters.ddMMYYYYmitPunkt)
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
        int row = tabHistRezepte.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Kein Rezept f\u00fcr Fallsteuerung ausgew\u00e4hlt");
            return;
        }
        Reha.instance.progLoader.Dta301Fenster(1, tabHistRezepte.getValueAt(row, 0)
                                                             .toString());
    }

    private void doRgebKopie() {
        if (!Rechte.hatRecht(Rechte.Rezept_gebuehren, true)) {
            return;
        }
        int row = tabHistRezepte.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Kein Rezept f\u00fcr Rezeptgeb\u00fchr-Kopie ausgew\u00e4hlt");
            return;
        }
        // testen ob in kasse also bezahlt, dann die Werte holen
        // wenn nicht bezahlt testen ob OpRgaf l\u00e4uft falls nicht starten
        // dann \u00fcber sockComm die Rechnung suchen und drucken
        // evtl. OpRgaf wieder beenden
        String sreznum = tabHistRezepte.getValueAt(row, 0)
                                    .toString();
        String srezdat = tabHistRezepte.getValueAt(row, 2)
                                    .toString();
        String einnahme = SqlInfo.holeEinzelFeld(
                "select einnahme from kasse where rez_nr = '" + sreznum + "' or ktext like '%" + sreznum + "%'LIMIT 1");
        if (einnahme.equals("")) {
            einnahme = SqlInfo.holeEinzelFeld("select reznr from rgaffaktura where reznr = '" + sreznum + "' LIMIT 1");
        } else {
            // Kopie der Gebuehrenquittung
            SystemConfig.hmRgkDaten.put("<Rgknummer>", String.valueOf(sreznum));
            SystemConfig.hmRgkDaten.put("<Rgkdatum>", srezdat);
            SystemConfig.hmRgkDaten.put("<Rgkbetrag>", String.valueOf(einnahme.replace(".", ",")));
            String bezdatum = SqlInfo.holeEinzelFeld("select datum from kasse where rez_nr = '" + sreznum
                    + "' or ktext like '%" + sreznum + "%'LIMIT 1");
            SystemConfig.hmRgkDaten.put("<Rgkbezahldatum>", String.valueOf(DatFunk.sDatInDeutsch(bezdatum)));
            OOTools.starteRGKopie(
                    Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK() + "/Rezeptgebuehr.ott.Kopie.ott",
                    SystemConfig.rezGebDrucker);
            return;
        }
        if (einnahme.equals("")) {
            JOptionPane.showMessageDialog(null,
                    "Das Rezept wurde weder bar bezahlt noch existiert eine Rezeptgeb\u00fchrrechnung!!");
            return;
        } else {
            RgrKopie kopie = new RgrKopie(sreznum);
        }
    }

    public static String getActiveRezNr() {
        if (Reha.instance.patpanel.historie.tabHistRezepte.getSelectedRow() < 0) {
            return null;
        }
        return Reha.instance.patpanel.historie.tabHistRezepte.getValueAt(
                Reha.instance.patpanel.historie.tabHistRezepte.getSelectedRow(), 0)
                                                          .toString();
    }

    private void doKopieToNew() {
        try {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        if ((getActiveRezNr()) == null) {
                            return null;
                        }
                        Reha.instance.patpanel.aktRezept.neuanlageRezept(true, "", RezepteAktuell.REZEPTKOPIERE_HISTORIENREZEPT);
                        Reha.instance.patpanel.multiTab.setSelectedIndex(0);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }

            }.execute();
        } catch (Exception ex) {

        }
    }

    /***********************/
    // Actions
    
    public void actionListSelection(ListSelectionEvent e) {
        if (rezneugefunden) {
            rezneugefunden = false;
            return;
        }
        
        if (e.getValueIsAdjusting()) {
            return;
        }
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (lsm.isSelectionEmpty()) {

        } else {
            int minIndex = lsm.getMinSelectionIndex();
            int maxIndex = lsm.getMaxSelectionIndex();
            for (int i = minIndex; i <= maxIndex; i++) {
                if (lsm.isSelectedIndex(i)) {
                    final int ix = i;

                    new SwingWorker<Void, Void>() {

                        @Override
                        protected Void doInBackground() throws Exception {
                            try {
                                inRezeptDaten = true;
                                setCursor(Cursors.wartenCursor);
                                /*
                                holeEinzelTermine(ix, null);
                                jpan1.setRezeptDaten((String) tabhistorie.getValueAt(ix, 0),
                                        (String) tabhistorie.getValueAt(ix, RezeptHistTableModel.HISTREZTABCOL_ID));
                                */
                                setCursor(Cursors.normalCursor);
                                inRezeptDaten = false;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                setCursor(Cursors.normalCursor);
                            }

                            return null;
                        }

                    }.execute();

                    break;
                }
            }
        }
    }
    
    private void actionTableModelChange(TableModelEvent arg0) {
        // Emtpy stub for now...
    }
    private void actionTablePropertyChange(PropertyChangeEvent arg0) {
        // Empty stub for now...
    }
    // Rezept-Related actions:
    private void actionArztBericht(ActionEvent e) {
        if (!Rechte.hatRecht(Rechte.Historie_thbericht, true)) {
            return;
        }
        if (aktPanel.equals("leerPanel")) {
            JOptionPane.showMessageDialog(null, "Ich sag jetzt nix....\n\n"
                    + "....au\u00dfer - und f\u00fcr welches der nicht vorhandenen Rezepte in der Historie wollen Sie einen Therapiebericht erstellen....");
            return;
        }
        boolean neuber = true;
        int berid = 0;
        String xreznr;
        String xverfasser = "";
        int currow = tabHistRezepte.getSelectedRow();
        if (currow >= 0) {
            xreznr = (String) tabHistRezepte.getValueAt(currow, RezeptHistTableModel.HISTREZTABCOL_NR);
        } else {
            xreznr = "";
        }

        int iexistiert = Reha.instance.patpanel.berichte.berichtExistiert(xreznr);
        if (iexistiert > 0) {
            xverfasser = Reha.instance.patpanel.berichte.holeVerfasser();
            neuber = false;
            berid = iexistiert;
            String meldung = "<html>F\u00fcr das Historienrezept <b>" + xreznr
                    + "</b> existiert bereits ein Bericht.<br>\nVorhandener Bericht wird jetzt ge\u00f6ffnet";
            JOptionPane.showMessageDialog(null, meldung);
        }

        final boolean xneuber = neuber;
        final String xxreznr = xreznr;
        final int xberid = berid;
        final int xcurrow = currow;
        final String xxverfasser = xverfasser;
        ArztBericht ab = new ArztBericht(null, "arztberichterstellen", xneuber, xxreznr, xberid, 1, xxverfasser, "",
                xcurrow);
        ab.setModal(true);
        ab.setLocationRelativeTo(null);
        ab.setVisible(true);
        ab = null;
        return;
    }
    private void actionTools(ActionEvent e) {
        new ToolsDlgHistorie("", btnTools.getLocationOnScreen());
    }
    
    // TerminToolbarActions: (are they supposed to work at all?)
    private void actionTerminNeu(ActionEvent ae) {
        // seems like these are not used in Historische Rezepte...
    }
    private void actionTerminSort(ActionEvent ae) {
        // seems like these are not used in Historische Rezepte...
    }
    private void actionTerminDel(ActionEvent ae) {
        // seems like these are not used in Historische Rezepte...
    }



    /*************************************************/
    // Inner Helper-Classes:
    class HistorRezepteListSelectionHandler implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (rezneugefunden) {
                rezneugefunden = false;
                return;
            }
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            boolean isAdjusting = e.getValueIsAdjusting();
            if (isAdjusting) {
                return;
            }
            if (lsm.isSelectionEmpty()) {

            } else {
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                        final int ix = i;

                        new SwingWorker<Void, Void>() {

                            @Override
                            protected Void doInBackground() throws Exception {
                                try {
                                    inRezeptDaten = true;
                                    setCursor(Cursors.wartenCursor);
                                    holeEinzelTermine(ix, null);
                                    // jpan1.setRezeptDaten((String) tabHistRezepte.getValueAt(ix, 0),
                                    //         String.valueOf(tabHistRezepte.getValueAt(ix, RezeptHistTableModel.HISTREZTABCOL_ID)));
                                    String reznr = (String) tabHistRezepte.getValueAt(ix, RezeptHistTableModel.HISTREZTABCOL_NR);
                                    String id = String.valueOf(tabHistRezepte.getValueAt(ix, RezeptHistTableModel.HISTREZTABCOL_ID));
                                    logger.debug("Calling rezDataUpdate with " + reznr + " could use " + id);
                                    jpan1.updateDatenPanel((String) tabHistRezepte.getValueAt(ix, RezeptHistTableModel.HISTREZTABCOL_NR), false);
                                    setCursor(Cursors.normalCursor);
                                    inRezeptDaten = false;
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    setCursor(Cursors.normalCursor);
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
    
    class HistorPanel extends JXPanel {
        /**
         *
         */
        private static final long serialVersionUID = -1044284924785143054L;
        ImageIcon hgicon;
        int icx, icy;
        AlphaComposite xac1 = null;
        AlphaComposite xac2 = null;

        HistorPanel() {
            super();
            setOpaque(false);
            /*
             * hgicon = SystemConfig.hmSysIcons.get("historie"); icx =
             * hgicon.getIconWidth()/2; icy = hgicon.getIconHeight()/2; xac1 =
             * AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.075f); xac2 =
             * AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);
             */
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;

            if (hgicon != null) {
                // g2d.setComposite(this.xac1);
                // g2d.drawImage(hgicon.getImage(), (getWidth()/3)-(icx+20) ,
                // (getHeight()/2)-(icy-40),null);
                // g2d.setComposite(this.xac2);
            }
        }
    }

    class ToolsDlgHistorie {
        public ToolsDlgHistorie(String command, Point pt) {

            Map<Object, ImageIcon> icons = new HashMap<Object, ImageIcon>();
            icons.put("Gesamtumsatz dieses Patienten", SystemConfig.hmSysIcons.get("euro"));
            icons.put("Behandlungstage in Clipboard", SystemConfig.hmSysIcons.get("einzeltage"));
            icons.put("Transfer in aktuelle Rezepte", SystemConfig.hmSysIcons.get("undo"));
            icons.put("Rezeptgeb\u00fchrquittung (Kopie)", SystemConfig.hmSysIcons.get("rezeptgebuehr"));
            icons.put("Daten in neues Rezept kopieren", SystemConfig.hmSysIcons.get("neu"));

            icons.put("\u00a7301 Reha-Fallsteuerung", SystemConfig.hmSysIcons.get("abrdreieins"));
            // create a list with some test data
            JList list = new JList(new Object[] { "Gesamtumsatz dieses Patienten", "Behandlungstage in Clipboard",
                    "Transfer in aktuelle Rezepte", "Rezeptgeb\u00fchrquittung (Kopie)", "Daten in neues Rezept kopieren",
                    "\u00a7301 Reha-Fallsteuerung" });
            list.setCellRenderer(new IconListRenderer(icons));
            Reha.toolsDlgRueckgabe = -1;
            ToolsDialog tDlg = new ToolsDialog(Reha.getThisFrame(), "Werkzeuge: Historie", list);
            tDlg.setPreferredSize(new Dimension(225,
                    200 + ((Boolean) SystemConfig.hmPatientenWerkzeugDlgIni.get("ToolsDlgShowButton") ? 25 : 0)));
            tDlg.setLocation(pt.x - 70, pt.y + 30);
            tDlg.pack();
            tDlg.setModal(true);
            tDlg.activateListener();
            tDlg.setVisible(true);
            switch (Reha.toolsDlgRueckgabe) {
            case 0:
                if (!Rechte.hatRecht(Rechte.Historie_gesamtumsatz, true)) {
                    return;
                }
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        doRechneAlles();
                        return null;
                    }
                }.execute();
                break;
            case 1:
                if (!Rechte.hatRecht(Rechte.Historie_tagedrucken, true)) {
                    return;
                }
                doTageDrucken();
                break;
            case 2:
                if (!Rechte.hatRecht(Rechte.Sonstiges_rezepttransfer, true)) {
                    return;
                }
                int anfrage = JOptionPane.showConfirmDialog(null,
                        "Das ausgew\u00e4hlte Rezept wirklich zur\u00fcck in den aktuellen Rezeptstamm transferieren?",
                        "Achtung wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
                if (anfrage == JOptionPane.YES_OPTION) {
                    doUebertrag();
                }
                break;
            case 3:
                doRgebKopie();
                break;
            case 4:
                doKopieToNew();
                break;
            case 5:
                do301FallSteuerung();
                break;

            }
            tDlg = null;
        }
    }

}

/*************************************/
/*************************************/

/*
class MyHistorieTableModel extends DefaultTableModel {
    /**
    *
    * /
    private static final long serialVersionUID = 1L;

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 1) {
            return JLabel.class;
        } else {
            return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
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

class MyHistorTermTableModel extends DefaultTableModel {
    /**
    *
    * /
    private static final long serialVersionUID = 1L;

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return String.class;
        } else {
            return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col == 0) {
            return true;
        } else if (col == 1) {
            return true;
        } else if (col == 2) {
            return true;
        } else if (col == 11) {
            return true;
        } else {
            return false;
        }
    }
}
*/
