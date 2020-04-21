package rehaSql;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sheet.XSheetCellCursor;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheets;
import com.sun.star.uno.UnoRuntime;

import CommonTools.ButtonTools;
import CommonTools.DatFunk;
import CommonTools.DblCellEditor;
import CommonTools.DoubleTableCellRenderer;
import CommonTools.JCompTools;
import CommonTools.JRtaComboBox;
import CommonTools.OOTools;
import CommonTools.SqlInfo;
import CommonTools.ini.INIFile;
import CommonTools.ini.INITool;
import rehaSql.RehaIO.SocketClient;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.spreadsheet.ISpreadsheetDocument;
import ag.ion.noa.NOAException;
import io.RehaIOMessages;

public class RehaSqlPanel extends JXPanel implements ListSelectionListener, TableModelListener {

    /**
     * 
     */
    private static final long serialVersionUID = -5545910505665721828L;

    private JXPanel content = null;

    private JRtaComboBox chbstatement = null;
    private final JButton[] buts = { null, null, null, null };

    private ActionListener al = null;

    private SqlTableModel tabmod = null;
    private JXTable tab = null;

    private DefaultTableModel alletablemod = null;
    private JXTable alletabletab = null;

    private JTextArea textArea = null;
    private JLabel labgefunden = null;

    // Vector<Vector<String>> feldNamen = null;

    private final Vector<String> colName = new Vector<String>();
    private final Vector<String> colClassName = new Vector<String>();
    private final Vector<Integer> colType = new Vector<Integer>();
    private final Vector<Boolean> colAutoinc = new Vector<Boolean>();
    private final Vector<String> colTypeName = new Vector<String>();
    private final Vector<Integer> colVisible = new Vector<Integer>();

    private final Vector<Vector<String>> vecStatements = new Vector<Vector<String>>();

    private int autoIncCol = -1;
    private boolean isUpdateable = false;
    private String aktuelleTabelle = "";

    private JScrollPane jscr = null;

    private final CommonTools.DateTableCellEditor tabDateEditor = new CommonTools.DateTableCellEditor();
    private final DateTableCellRenderer tabDateRenderer = new DateTableCellRenderer();

    private final DblCellEditor tabDoubleEditor = new DblCellEditor();
    private final DoubleTableCellRenderer tabDoubleRenderer = new DoubleTableCellRenderer();

    private final IntTableCellEditor tabIntegerEditor = new IntTableCellEditor();
    private final IntTableCellRenderer tabIntegerRenderer = new IntTableCellRenderer();

    private JTextArea sqlstatement = null;

    private final DecimalFormat dcf = new DecimalFormat("##########0.00");
    private final SimpleDateFormat datumsFormat = new SimpleDateFormat("dd.MM.yyyy"); // Konv.

    private ISpreadsheetDocument spreadsheetDocument = null;
    private XSheetCellCursor cellCursor = null;
    private String sheetName = null;

    private long starttime = 0;

    RehaSqlPanel() {
        super();
        setLayout(new BorderLayout());
        activateActionListener();
        add(getContent(), BorderLayout.CENTER);

        validate();
        content.validate();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                long zeit = System.currentTimeMillis();
                while (!RehaSql.DbOk) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        if (System.currentTimeMillis() - zeit > 10000) {
                            System.exit(0);
                        }
                        e.printStackTrace();
                    }
                }
                chbstatement.addActionListener(al);
                setzeFocus(sqlstatement);
                return null;
            }

        }.execute();
    }

    private JXPanel getContent() {
        String xwerte = "fill:0:grow(0.2),fill:0:grow(0.8)";
        String ywerte = "0dlu,fill:0:grow(1.0),0dlu";

        FormLayout lay = new FormLayout(xwerte, ywerte);
        CellConstraints cc = new CellConstraints();
        content = new JXPanel();
        content.setLayout(lay);

        content.add(getSqlPanel(), cc.xy(2, 2, CellConstraints.FILL, CellConstraints.FILL));
        content.add(getAlleTabellenPanel(), cc.xy(1, 2, CellConstraints.FILL, CellConstraints.FILL));
        content.revalidate();
        return content;
    }

    private JXPanel getAlleTabellenPanel() {
        String xwerte = "5dlu,fill:0:grow(1.0),5dlu";
        String ywerte = "5dlu,fill:0:grow(1.0),5dlu";
        FormLayout lay = new FormLayout(xwerte, ywerte);
        CellConstraints cc = new CellConstraints();
        JXPanel jpan = new JXPanel();
        jpan.setOpaque(false);
        jpan.setLayout(lay);

        alletablemod = new DefaultTableModel();
        alletablemod.setColumnIdentifiers(new String[] { "Alle-Tabellen" });
        alletabletab = new JXTable(alletablemod);
        alletabletab.setEditable(false);
        alletabletab.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    if (!RehaSql.isReadOnly()) {
                        sqlstatement.setText("describe " + alletabletab.getValueAt(alletabletab.getSelectedRow(), 0)
                                                                       .toString());
                        doStatementAuswerten();
                    }
                }
            }
        });

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                long zeit = System.currentTimeMillis();
                while (!RehaSql.DbOk) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        if (System.currentTimeMillis() - zeit > 10000) {
                            System.exit(0);
                        }
                        e.printStackTrace();
                    }
                }
                Vector<Vector<String>> vec = SqlInfo.holeFelder("show tables");
                if (!RehaSql.isReadOnly()) {
                    for (Vector<String> strings : vec) {
                        alletablemod.addRow(strings);
                    }
                    if (!vec.isEmpty()) {
                        alletabletab.setRowSelectionInterval(0, 0);
                    }
                }
                return null;
            }
        }.execute();
        JScrollPane alletbljscr = JCompTools.getTransparentScrollPane(alletabletab);
        alletbljscr.validate();
        jpan.add(alletbljscr, cc.xy(2, 2, CellConstraints.FILL, CellConstraints.FILL));
        jpan.validate();
        return jpan;
    }

    private JXPanel getSqlPanel() {
        // 1 2 3 4 5 6 7
        String xwerte = "5dlu,40dlu,2dlu,fill:0:grow(1.0),2dlu,60dlu,5dlu";
        // 1 2 3 4 5 6 7 8 9 10
        // String ywerte =
        // "5dlu,p,2dlu,fill:0:grow(0.5),2dlu,p,2dlu,p,2dlu,fill:0:grow(0.5),5dlu";
        // 1 2 3 4 5 6 7 8 9 10 11 12 13 14
        String ywerte = "5dlu,p,70dlu,5dlu,p,5dlu,fill:100dlu:grow(0.7),2dlu,p,2dlu,p,2dlu,fill:100dlu:grow(0.3),5dlu";
        FormLayout lay = new FormLayout(xwerte, ywerte);
        CellConstraints cc = new CellConstraints();
        JXPanel jpan = new JXPanel();
        jpan.setOpaque(false);
        jpan.setLayout(lay);
        JLabel lab = new JLabel("Statement eingeben oder vorgefertigtes Statement auswählen:");
        lab.setForeground(Color.BLUE);
        jpan.add(lab, cc.xyw(2, 2, 3));

        sqlstatement = new JTextArea();
        sqlstatement.setFont(new Font("Courier", Font.PLAIN, 12));
        sqlstatement.setLineWrap(true);
        sqlstatement.setWrapStyleWord(true);
        sqlstatement.setForeground(Color.BLUE);
        sqlstatement.setOpaque(false);
        sqlstatement.setName("sqlstatement");
        sqlstatement.setFont(new Font("Courier New", Font.PLAIN, 12));

        if (RehaSql.isReadOnly()) {
            sqlstatement.setEditable(false);
        }
        JScrollPane scrstmt = JCompTools.getTransparentScrollPane(sqlstatement);
        scrstmt.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        scrstmt.validate();
        jpan.add(scrstmt, cc.xyw(2, 3, 5, CellConstraints.FILL, CellConstraints.FILL));

        lab = new JLabel("Statement ausführen (execute):");
        lab.setForeground(Color.RED);
        jpan.add(lab, cc.xyw(2, 5, 3));
        JButton jbExecute = ButtonTools.macheButton("execute", "exekutieren", al);
        buts[0] = jbExecute;
        buts[0].setMnemonic('e');
        buts[0].setForeground(Color.RED);
        jpan.add(buts[0], cc.xy(6, 5));

        tabmod = new SqlTableModel();
        tab = new JXTable(tabmod);
        tab.setColumnControlVisible(true);
        tab.setHorizontalScrollEnabled(true);
        tab.getSelectionModel()
           .addListSelectionListener(new BillListSelectionHandler());

        tab.setHighlighters(HighlighterFactory.createSimpleStriping(HighlighterFactory.CLASSIC_LINE_PRINTER));
        tabmod.addTableModelListener(this);

        jscr = JCompTools.getTransparentScrollPane(tab);
        jscr.validate();
        jpan.add(jscr, cc.xyw(2, 7, 5, CellConstraints.DEFAULT, CellConstraints.FILL));

        labgefunden = new JLabel("noch keine Abfageergebnisse");
        labgefunden.setForeground(Color.BLUE);
        jpan.add(labgefunden, cc.xyw(2, 9, 5));

        // Hier mit y-Wert = 8 die Funktionsleiste einbauen
        jpan.add(getFunktionsPanel(), cc.xyw(2, 11, 5, CellConstraints.FILL, CellConstraints.FILL));

        textArea = new JTextArea();
        textArea.setFont(new Font("Courier", Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(Color.LIGHT_GRAY);
        textArea.setForeground(Color.BLUE);
        // ta.setEnabled(false);
        // ta.setDisabledTextColor(Color.BLUE);
        JScrollPane span = JCompTools.getTransparentScrollPane(textArea);
        span.setBackground(Color.WHITE);
        span.validate();
        jpan.add(span, cc.xyw(2, 13, 5, CellConstraints.DEFAULT, CellConstraints.FILL));

        jpan.validate();
        return jpan;

    }

    private void setzeFocus(JComponent comp) {
        final JComponent xcomp = comp;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                xcomp.requestFocus();
            }
        });
    }

    private JXPanel getFunktionsPanel() {
        // 1.export in OO-Calc
        // Speichern des SQL-Befehles in der SQL-INI und Platzhalterhandling
        // Auswahl ob Ergebnis in Tabelle oder in TextArea geworfen wird
        // Listbox mit den vorhandenen Sql-Befehlen
        // 1 2 3 4 5 6 7 8 9 10
        String xwerte = "0dlu,160dlu,2dlu,80dlu,2dlu,80dlu,fill:0:grow(1.0),40dlu,2dlu,40dlu";
        // 1 2 3 4 5 6 7 8 9 10
        // String ywerte =
        // "5dlu,p,2dlu,fill:0:grow(0.5),2dlu,p,2dlu,p,2dlu,fill:0:grow(0.5),5dlu";
        // 1 2 3
        String ywerte = "5dlu,p,5dlu";
        FormLayout lay = new FormLayout(xwerte, ywerte);
        CellConstraints cc = new CellConstraints();
        JXPanel jpan = new JXPanel();
        jpan.setOpaque(false);
        jpan.setLayout(lay);
        jpan.add(buts[0] = ButtonTools.macheButton("Export in OO-Calc", "exportcalc", al), cc.xy(6, 2));
        chbstatement = new JRtaComboBox();
        chbstatement.setActionCommand("statementliste");

        jpan.add(chbstatement, cc.xy(2, 2));
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                doFillStatementBox();
                return null;
            }
        }.execute();
        jpan.add(buts[1] = ButtonTools.macheButton("Sql ausführen", "executestmt", al), cc.xy(4, 2));
        jpan.add(buts[2] = ButtonTools.macheButton("neu", "neuersatz", al), cc.xy(8, 2));
        jpan.add(buts[3] = ButtonTools.macheButton("löschen", "loeschensatz", al), cc.xy(10, 2));
        jpan.validate();
        return jpan;

    }

    private void doFillStatementBox() {
        while (!RehaSql.DbOk) {
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            String iniFile = (RehaSql.isReadOnly() ? "sqlmodulro.ini" : "sqlmodul.ini");
            INIFile inif = INITool.openIni(RehaSql.progHome + "ini/" + RehaSql.aktIK + "/", iniFile);
            Vector<String> vecstmts = new Vector<String>();
            int anzahl = inif.getIntegerProperty("SqlStatements", "StatementsAnzahl");
            for (int i = 0; i < anzahl; i++) {
                vecstmts.clear();
                vecstmts.add(inif.getStringProperty("SqlStatements", "StatementTitel" + (i + 1)));
                vecstmts.add(inif.getStringProperty("SqlStatements", "Statement" + (i + 1)));
                vecStatements.add(((Vector<String>) vecstmts.clone()));
            }
            chbstatement.setDataVectorWithStartElement(vecStatements, 0, 1, "./.");
            chbstatement.setSelectedItem("./.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fehler beim Einlesen der INI-Datei");
        }
    }

    private void benachrichtigeReha(final int row) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                int spalten = tabmod.getColumnCount();
                int relativerow = tab.convertRowIndexToModel(row);
                String pat = "", rez = "";
                for (int i = 0; i < spalten; i++) {
                    if (tabmod.getColumnName(i)
                              .trim()
                              .toLowerCase()
                              .equals("rez_nr")
                            || tabmod.getColumnName(i)
                                     .trim()
                                     .equals("reznr")) {
                        rez = tabmod.getValueAt(relativerow, i)
                                    .toString();
                    } else if (tabmod.getColumnName(i)
                                     .trim()
                                     .toLowerCase()
                                     .equals("pat_intern")) {
                        pat = tabmod.getValueAt(relativerow, i)
                                    .toString();
                    }
                }
                String xnachricht;
                if (pat.equals("")) {
                    if (rez.equals("")) {
                        return null;
                    } else {
                        xnachricht = "RehaSql#" + RehaIOMessages.MUST_REZFIND + "#" + rez;
                    }
                } else {
                    if (rez.equals("")) {
                        xnachricht = "RehaSql#" + RehaIOMessages.MUST_PATFIND + "#" + pat;

                    } else {
                        xnachricht = "RehaSql#" + RehaIOMessages.MUST_PATANDREZFIND + "#" + pat + "#" + rez;

                    }
                }
                new SocketClient().setzeRehaNachricht(RehaSql.rehaReversePort, xnachricht);
                return null;
            }

        }.execute();
    }

    class BillListSelectionHandler implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            boolean isAdjusting = e.getValueIsAdjusting();
            if (isAdjusting) {
                return;
            }
            if (!lsm.isSelectionEmpty()) {
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                        benachrichtigeReha(i);
                        break;
                    }
                }
            }
        }
    }

    private void activateActionListener() {
        al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String cmd = arg0.getActionCommand();
                if (cmd.equals("exekutieren")) {
                    try {
                        doStatementAuswerten();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (cmd.equals("exportcalc")) {
                    try {
                        starteExport();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (cmd.equals("statementliste")) {
                    try {
                        if (chbstatement.getSelectedIndex() <= 0) {
                            sqlstatement.setText("");
                            tabmod.setRowCount(0);
                            tab.validate();
                            return;
                        }
                        doMacheStatement();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (cmd.equals("executestmt")) {
                    try {
                        if (chbstatement.getSelectedIndex() <= 0) {
                            sqlstatement.setText("");
                            return;
                        }
                        doStatementAuswerten();
                        // doMacheStatement();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (cmd.equals("zeileloeschen")) {
                    try {
                        // doZeileLoeschen();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (cmd.equals("indbspeichern")) {
                    try {
                        // doInDbSpeichern();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (cmd.equals("abbrechen")) {
                    try {
                        // doAbbrechen();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (cmd.equals("loeschensatz")) {
                    try {
                        doLoeschenSatz();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

        };

    }

    private void doLoeschenSatz() {
        if (!isUpdateable || aktuelleTabelle.trim()
                                            .equals("")) {
            return;
        }
        int row = tab.getSelectedRow();
        if (row < 0) {
            return;
        }
        int frage = JOptionPane.showConfirmDialog(null, "Wollen Sie den Satz mit " + colName.get(autoIncCol) + " "
                + tabmod.getValueAt(row, autoIncCol) + " wirklich löschen?", "Achtung!!!!", JOptionPane.YES_NO_OPTION);
        if (frage != JOptionPane.YES_OPTION) {
            return;
        }
        row = tab.convertRowIndexToModel(row);
        String cmd = "delete from " + aktuelleTabelle + " where " + colName.get(autoIncCol) + " = '"
                + tabmod.getValueAt(row, autoIncCol) + "' LIMIT 1";
        SqlInfo.sqlAusfuehren(cmd);
        tabmod.removeRow(row);
    }

    private void doMacheStatement() {
        int ind = this.chbstatement.getSelectedIndex();
        if (ind <= 0) {
            JOptionPane.showMessageDialog(null, "Kein vorbereitetes Statement ausgewählt");
            return;
        }
        String prepstatement = vecStatements.get(ind - 1)
                                            .get(1);
        if (prepstatement.contains("^") || prepstatement.contains("where")) {
            prepstatement = testeAufPlatzhalter(prepstatement);
            System.out.println("prepstatement=" + prepstatement);
            if (prepstatement.equals("")) {
                JOptionPane.showMessageDialog(null, "Fehler in der Eingabe");
                return;
            }
        }
        sqlstatement.setText(prepstatement);
        buts[1].requestFocus();
    }

    private String testeAufPlatzhalter(String text) {
        String sret = "";

        String stext = text;
        int start;

        String dummy;

        boolean noendfound;
        while ((start = stext.indexOf("^")) >= 0) {
            noendfound = true;
            for (int i = 1; i < 350; i++) {
                if (stext.substring(start + i, start + (i + 1))
                         .equals("^")) {
                    dummy = stext.substring(start, start + (i + 1));
                    String sanweisung = dummy.replace("^", "");
                    String ret = JOptionPane.showInputDialog(null,
                            "<html>Bitte Wert eingeben für: --\u003E<b> " + sanweisung + " </b> &nbsp; </html>",
                            "Platzhalter gefunden", 1);
                    if (ret == null) {
                        return "";

                    } else {

                        if (ret.trim()
                               .length() == 10
                                && ret.trim()
                                      .indexOf(".") == 2
                                && ret.trim()
                                      .lastIndexOf(".") == 5) {

                            try {
                                ret = DatFunk.sDatInSQL(ret);
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null, "Fehler in der Konvertierung des Datums");
                            }

                        }
                        sret = stext.replace(dummy, ret.trim());
                        stext = sret;
                    }
                    noendfound = false;
                    break;
                }
            }
            if (noendfound) {
                JOptionPane.showMessageDialog(null, "Der Baustein ist fehlerhaft, eine Übernahme deshalb nicht möglich"
                        + "\n\nVermutete Ursache des Fehlers: es wurde ein Start-/Endezeichen '^' für Variable vergessen\n");
                return "";
            }
        }

        return (sret.equals("") ? text : sret);
    }

    @Override
    public void valueChanged(ListSelectionEvent arg0) {

    }

    @Override
    public void tableChanged(TableModelEvent arg0) {
        if (arg0.getType() == TableModelEvent.INSERT) {
            return;
        }
        if (arg0.getType() == TableModelEvent.UPDATE) {
            try {
                int col = arg0.getColumn();
                int row = arg0.getFirstRow();
                // String colname = tabmod.getColumnName(col).toString();
                String value = "";
                // String id =
                // Integer.toString((Integer)tabmod.getValueAt(row,tabmod.getColumnCount()-1));

                if (tabmod.getColumnClass(col) == Boolean.class) {
                    value = (tabmod.getValueAt(row, col) == Boolean.FALSE ? "F" : "T");
                } else if (tabmod.getColumnClass(col) == Date.class) {
                    value = tabmod.getValueAt(row, col)
                                  .toString();
                    if (value.trim()
                             .length() == 10) {
                        if (value.indexOf(".") == 2) {
                            value = DatFunk.sDatInSQL(value);
                        }
                    } else {
                        value = "null";
                    }
                } else if (tabmod.getColumnClass(col) == Double.class) {
                    value = dcf.format(tabmod.getValueAt(row, col))
                               .replace(",", ".");
                } else if (tabmod.getColumnClass(col) == Integer.class) {
                    value = Integer.toString((Integer) tabmod.getValueAt(row, col));
                } else if (tabmod.getColumnClass(col) == String.class) {
                    value = tabmod.getValueAt(row, col)
                                  .toString();
                }
                if (autoIncCol < 0) {
                    JOptionPane.showMessageDialog(null,
                            "Keine auto_inc Spalte gefunden, Wert wird nicht in die Datenbank geschrieben");
                    return;
                } else {
                    if (isUpdateable) {
                        String cmd = "update " + aktuelleTabelle + " set " + colName.get(col) + " = '" + value
                                + "' where " + colName.get(autoIncCol) + " = '" + tabmod.getValueAt(row, autoIncCol)
                                + "' LIMIT 1";
                        SqlInfo.sqlAusfuehren(cmd);
                        // System.out.println(cmd);
                    } else {
                        String message = "der vorangegangene SELECT-Befehl bezog sich auf mehr als eine Tabelle.\n"
                                + "Solche Abfrageergebnisse können nicht direkt UPDATED werden.\n"
                                + "Lösung: mehrere SELECT Abfragen auf jeweils eine Tabelle bezogen";
                        JOptionPane.showMessageDialog(null, message);
                    }

                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Fehler in der Dateneingbe");
            }

        }

    }

    private void doExecuteOnly() {
        // ToDo auf delete und update testen und wenn ja auf Limit 1 testen und wenn
        // nein SuperUser-Passwort anfordern
        textArea.setText("Ihr Statement: [" + sqlstatement.getText()
                                                          .trim()
                + "]\n" + textArea.getText());
        try (Statement stmt = RehaSql.thisClass.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE)) {
            stmt.execute(sqlstatement.getText());
            tabmod.setRowCount(0);
            tab.validate();
        } catch (SQLException e) {
            textArea.setText(e.getMessage() + "\n" + textArea.getText());
        }

    }

    private void doExecuteStatement() {

        // ToDo auf delete und update testen und wenn ja auf Limit 1 testen und wenn
        // nein SuperUser-Passwort anfordern
        if (sqlstatement.getText()
                        .toLowerCase()
                        .contains("delete")
                || sqlstatement.getText()
                               .toLowerCase()
                               .contains("update")) {
            if (!sqlstatement.getText()
                             .toLowerCase()
                             .contains("limit")) {
                // TODO hier Super-User-Paswort abfragen
            }
        }
        textArea.setText("Ihr Statement: [" + sqlstatement.getText()
                                                          .trim()
                + "]\n" + textArea.getText());
        Statement stmt = null;
        try {

            stmt = RehaSql.thisClass.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(sqlstatement.getText());
            tabmod.setRowCount(0);
            tab.validate();
        } catch (SQLException e) {
            textArea.setText(e.getMessage() + "\n" + textArea.getText());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { // ignore }
                    stmt = null;
                }
            }
        }
    }

    private void doOnlySuperUserExecuteStatement() {
        Statement stmt = null;
        // ToDo SuperUser-Passwort anfordern
        try {
            textArea.setText("Ihr Statement: [" + sqlstatement.getText()
                                                              .trim()
                    + "]\n" + textArea.getText());
            stmt = RehaSql.thisClass.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(sqlstatement.getText());
            tabmod.setRowCount(0);
            tab.validate();
        } catch (SQLException e) {
            textArea.setText(e.getMessage() + "\n" + textArea.getText());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { // ignore }
                    stmt = null;
                }
            }
        }
    }

    private void doStatementAuswerten() {
        starttime = System.currentTimeMillis();
        if (sqlstatement.getText()
                        .trim()
                        .equals("")) {
            return;
        }
        if (sqlstatement.getText()
                        .trim()
                        .toLowerCase()
                        .startsWith("drop")) {
            doOnlySuperUserExecuteStatement();
            return;
        }
        if (sqlstatement.getText()
                        .trim()
                        .toLowerCase()
                        .startsWith("alter table")
                || sqlstatement.getText()
                               .trim()
                               .toLowerCase()
                               .startsWith("truncate table")
                || sqlstatement.getText()
                               .trim()
                               .toLowerCase()
                               .startsWith("create table")) {
            doExecuteOnly();
            return;

        }
        if (sqlstatement.getText()
                        .trim()
                        .toLowerCase()
                        .startsWith("update")
                || sqlstatement.getText()
                               .trim()
                               .toLowerCase()
                               .startsWith("insert")
                || sqlstatement.getText()
                               .trim()
                               .toLowerCase()
                               .startsWith("delete")) {
            doExecuteStatement();
            return;
        }

        autoIncCol = -1;
        tabmod.removeTableModelListener(this);
        tabmod.setRowCount(0);
        colName.clear();
        colType.clear();
        colAutoinc.clear();
        colClassName.clear();
        colTypeName.clear();
        isUpdateable = true;
        aktuelleTabelle = "";

        Statement stmt = null;
        ResultSet rs = null;

        ResultSetMetaData md;

        try {
            textArea.setText("Ihr Statement: [" + sqlstatement.getText()
                                                              .trim()
                    + "]\n" + textArea.getText());
            stmt = RehaSql.thisClass.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery(sqlstatement.getText()
                                               .trim());

            md = rs.getMetaData();

            int cols = md.getColumnCount();
            String table0 = null;
            for (int i = 0; i < cols; i++) {

                colName.add(md.getColumnName(i + 1));
                colType.add(md.getColumnType(i + 1));
                colClassName.add(md.getColumnClassName(i + 1));
                try {
                    if (md.getColumnType(i + 1) == 1 && md.getColumnDisplaySize(i + 1) == 1) {
                        // dann wird enum('T','F') also logisch vermutet und
                        // per Hand BOOLEAN eingetragen. Stimmt natürlich nicht wenn in der
                        // Tabellendefinition tatsächlich Spaltenbreite 1 und typ CHAR eingetragen ist.
                        // - Leider
                        colTypeName.add("BOOLEAN");
                    } else {
                        colTypeName.add(md.getColumnTypeName(i + 1));
                    }
                } catch (Exception ex) {
                    colTypeName.add("VARCHAR");
                }
                try {
                    colAutoinc.add(md.isAutoIncrement(i + 1));
                    if (md.isAutoIncrement(i + 1)) {
                        autoIncCol = i;
                    }
                } catch (Exception ex) {
                    colAutoinc.add(false);
                }
                if (i == 0) {
                    table0 = md.getTableName(i + 1);
                    aktuelleTabelle = String.valueOf(table0);
                } else {
                    try {
                        if (!table0.equals(md.getTableName(i + 1))) {
                            isUpdateable = false;
                        }
                    } catch (Exception ex) {

                    }
                }

            }
            tabmod.setColumnIdentifiers(colName);

            Vector<Object> vec = new Vector<Object>();
            int durchlauf = 0;
            int lang = colName.size();
            // Saudummerweise entspricht der Rückgabewert von getColumnTypeName() oder
            // getColumnType() nicht der Abfrag von describe tabelle
            // so werden alle Integer-Typen unter INT zusammengefaßt
            // Longtext, Mediumtext, Varchar = alles VARCHAR
            // CHAR kann sowohl ein einzelnes Zeichen als auch enum('T','F') also boolean
            // sein...
            // eigentlich ein Riesenmist!
            while (rs.next()) {
                vec.clear();

                for (int i = 0; i < lang; i++) {
                    try {
                        // System.out.println(i+" Durchlauf: "+colTypeName.get(i));
                        if (colTypeName.get(i)
                                       .contains("VARCHAR")) {
                            vec.add((rs.getString(i + 1) == null ? "" : rs.getString(i + 1)));
                        } else if (colTypeName.get(i)
                                              .equals("BOOLEAN")) {
                            vec.add((rs.getString(i + 1) == null ? Boolean.FALSE
                                    : (rs.getString(i + 1)
                                         .equals("T") ? Boolean.TRUE : Boolean.FALSE)));
                        } else if (colTypeName.get(i)
                                              .contains("DECIMAL")) {
                            if (rs.getBigDecimal(i + 1) == null) {
                                vec.add(Double.parseDouble("0.00"));
                            } else {
                                vec.add(rs.getBigDecimal(i + 1)
                                          .doubleValue());
                            }
                        } else if (colTypeName.get(i)
                                              .toUpperCase()
                                              .startsWith("TINYINT")) {
                            vec.add(rs.getInt(i + 1));
                        } else if (colTypeName.get(i)
                                              .toUpperCase()
                                              .startsWith("SMALLINT")) {
                            vec.add(rs.getInt(i + 1));
                        } else if (colTypeName.get(i)
                                              .startsWith("BIGINT")) {
                            vec.add(rs.getLong(i + 1));
                        } else if (colTypeName.get(i)
                                              .startsWith("INT")) {
                            vec.add(rs.getInt(i + 1));
                        } else if (colTypeName.get(i)
                                              .contains("DATE")) {
                            if (!(rs.getString(i + 1) == null)) {
                                vec.add(rs.getDate(i + 1));
                            } else {
                                vec.add(null);
                            }
                        } else {
                            vec.add((rs.getString(i + 1) == null ? "" : rs.getString(i + 1)));
                        }
                    } catch (Exception ex) {
                        vec.add(null);
                        ex.printStackTrace();
                    }
                }

                tabmod.addRow((Vector<?>) vec.clone());
                if (durchlauf > 200) {
                    try {
                        tab.validate();
                        tab.repaint();
                        Thread.sleep(100);
                        durchlauf = 0;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                durchlauf++;
            }
            for (int i = 0; i < lang; i++) {
                if (colTypeName.get(i)
                               .contains("DATE")) {
                    tab.getColumn(i)
                       .setCellEditor(tabDateEditor);
                    tab.getColumn(i)
                       .setCellRenderer(tabDateRenderer);

                } else if (colTypeName.get(i)
                                      .contains("DECIMAL")) {
                    tab.getColumn(i)
                       .setCellEditor(tabDoubleEditor);
                    tab.getColumn(i)
                       .setCellRenderer(tabDoubleRenderer);
                } else if (colTypeName.get(i)
                                      .contains("INT")) {
                    tab.getColumn(i)
                       .setCellEditor(tabIntegerEditor);
                    tab.getColumn(i)
                       .setCellRenderer(tabIntegerRenderer);
                }
            }
            tab.validate();
            tab.repaint();
            if (tab.getRowCount() > 0) {
                tab.setRowSelectionInterval(0, 0);
            }
            jscr.validate();
            doSetAbfrageErgebnis();

        } catch (SQLException e) {
            textArea.setText(e.getMessage() + "\n" + textArea.getText());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { // ignore }
                    rs = null;
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { // ignore }
                    stmt = null;
                }
            }
        }
        tabmod.addTableModelListener(this);
    }

    private void doSetAbfrageErgebnis() {
        labgefunden.setText("Abfrageergebnis: Datensätze = " + tab.getRowCount() + " / Spalten = "
                + tab.getColumnCount() + " - " + (System.currentTimeMillis() - starttime) + " Millisekunden");
    }

    class SqlTableModel extends DefaultTableModel {
        /**
        * 
        */
        private static final long serialVersionUID = 1L;

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (colTypeName.get(columnIndex)
                           .contains("VARCHAR")) {
                return String.class;
            } else if (colTypeName.get(columnIndex)
                                  .equals("BOOLEAN")) {
                return Boolean.class;
            } else if (colTypeName.get(columnIndex)
                                  .contains("DECIMAL")) {
                return Double.class;
            } else if (colTypeName.get(columnIndex)
                                  .toUpperCase()
                                  .contains("TINYINT")) {
                return Integer.class;
            } else if (colTypeName.get(columnIndex)
                                  .toUpperCase()
                                  .contains("INT")) {
                return Integer.class;
            } else if (colTypeName.get(columnIndex)
                                  .contains("DATE")) {
                return Date.class;
            } else if (colTypeName.get(columnIndex)
                                  .toUpperCase()
                                  .contains("LONGTEXT")) {
                return String.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return !colAutoinc.get(col);
        }
    }

    private void starteExport() {
        int rowcount;
        if ((rowcount = tab.getRowCount()) <= 0) {
            JOptionPane.showMessageDialog(null, "Kein Abfrageergebnis für OO.org-Export vorhanden");
            return;
        }
        int colcount = tab.getColumnCount(false);
        colVisible.clear();
        for (int i = 0; i < colcount; i++) {
            colVisible.add(tab.convertColumnIndexToModel(i));
        }
        try {
            starteCalc();
            for (int i = 0; i < colVisible.size(); i++) {
                OOTools.doCellFontBold(cellCursor, i, 0);
                OOTools.doCellValue(cellCursor, i, 0, colName.get(colVisible.get(i)));
                if (tabmod.getColumnClass(colVisible.get(i))
                          .toString()
                          .contains("java.lang.Double")) {
                    OOTools.doColNumberFormat(spreadsheetDocument, sheetName, i, i, 2);
                }
            }
            Object obj;
            int relativerow;
            for (int i = 0; i < rowcount; i++) {
                for (int y = 0; y < colVisible.size(); y++) {
                    relativerow = tab.convertRowIndexToModel(i);
                    obj = tabmod.getValueAt(relativerow, colVisible.get(y));
                    // System.out.println(y+" Durchlauf = "+ obj.getClass());
                    if (obj != null) {
                        if (obj instanceof Double) {
                            OOTools.doCellValue(cellCursor, y, i + 1, obj);
                        } else if (obj instanceof Integer) {
                            OOTools.doCellValue(cellCursor, y, i + 1, obj);
                        } else if (obj instanceof Boolean) {
                            OOTools.doCellValue(cellCursor, y, i + 1, obj == Boolean.TRUE ? "ja" : "nein");
                        } else if (obj instanceof Date) {
                            try {
                                if (datumsFormat.format(obj)
                                                .length() == 10) {
                                    OOTools.doCellFormula(cellCursor, y, i + 1,
                                            OOTools.doOODate(datumsFormat.format(obj)));
                                    OOTools.doCellDateFormatGerman(spreadsheetDocument, cellCursor, y, i + 1, true);
                                }
                            } catch (Exception ex) {
                                OOTools.doCellValue(cellCursor, y, i + 1, obj.toString());
                            }
                        } else if (obj instanceof Long) {
                            OOTools.doCellValue(cellCursor, y, i + 1, obj);
                        } else if (obj instanceof Short) {
                            OOTools.doCellValue(cellCursor, y, i + 1, obj);
                        } else {
                            OOTools.doCellValue(cellCursor, y, i + 1, obj);
                        }
                    }
                }
            }
        } catch (NoSuchElementException | IndexOutOfBoundsException | NOAException | OfficeApplicationException
                | IllegalArgumentException | PropertyVetoException | UnknownPropertyException
                | WrappedTargetException e) {
            e.printStackTrace();
        }
    }

    private void starteCalc() throws OfficeApplicationException, NOAException, NoSuchElementException,
            WrappedTargetException, UnknownPropertyException, PropertyVetoException, IllegalArgumentException {
        if (!RehaSql.officeapplication.isActive()) {
            RehaSql.starteOfficeApplication();
        }
        IDocumentService documentService = RehaSql.officeapplication.getDocumentService();
        IDocumentDescriptor docdescript = new DocumentDescriptor();

        docdescript.setAsTemplate(true);
        IDocument document = documentService.constructNewDocument(IDocument.CALC, docdescript);
        spreadsheetDocument = (ISpreadsheetDocument) document;
        OOTools.setzePapierFormatCalc(spreadsheetDocument, 21000, 29700);
        OOTools.setzeRaenderCalc(spreadsheetDocument, 1000, 1000, 1000, 1000);
        sheetName = "Tabelle1";
        XSpreadsheets spreadsheets = spreadsheetDocument.getSpreadsheetDocument()
                                                        .getSheets();
        XSpreadsheet spreadsheet1 = UnoRuntime.queryInterface(XSpreadsheet.class, spreadsheets.getByName(sheetName));
        cellCursor = spreadsheet1.createCursor();
    }
}
