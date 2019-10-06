package offenePosten;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
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

import CommonTools.ButtonTools;
import CommonTools.DatFunk;
import CommonTools.DateTableCellEditor;
import CommonTools.DblCellEditor;
import CommonTools.DoubleTableCellRenderer;
import CommonTools.JCompTools;
import CommonTools.JRtaCheckBox;
import CommonTools.JRtaComboBox;
import CommonTools.JRtaTextField;
import CommonTools.MitteRenderer;
import CommonTools.OpShowGesamt;
import CommonTools.SqlInfo;
import RehaIO.SocketClient;
import io.RehaIOMessages;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class OffenepostenPanel extends JXPanel implements TableModelListener {

    /**
     * 
     */
    private static final long serialVersionUID = -7883557713071422132L;

    JRtaTextField suchen = null;
    JRtaTextField offen = null;
    JRtaTextField[] tfs = { null, null, null, null };
    JButton[] buts = { null, null, null };
    enum btIdx {ausbuchen,suchen,dummy};
    int btAusbuchen = btIdx.ausbuchen.ordinal();
    int btSuchen = btIdx.suchen.ordinal();
    JRtaComboBox combo = null;
    JXPanel content = null;
    KeyListener kl = null;
    ActionListener al = null;

    MyOffenePostenTableModel tabmod = null;
    JXTable tab = null;
/*    JLabel summeOffen;
    JLabel summeRechnung;
    JLabel summeGesamtOffen;
    JLabel anzahlSaetze;
*/    
    BigDecimal gesamtOffen = BigDecimal.valueOf(Double.parseDouble("0.00"));
    BigDecimal suchOffen = BigDecimal.valueOf(Double.parseDouble("0.00"));
    BigDecimal suchGesamt = BigDecimal.valueOf(Double.parseDouble("0.00"));
    DecimalFormat dcf = new DecimalFormat("###0.00");
    int gefunden;
    String[] spalten = { "Rechn.Nr.", "R.Datum", "Kasse", "Name/Notiz", "Sparte", "Gesamtbetrag", "Offen", "bezahlt am",
            "Zuzahlung", "1.Mahnung", "2.Mahnung", "3.Mahnung", "Mahnsperre", "Pat.Nr.", "IK Kostentr.", "id", "IK" };
    String[] colnamen = { "r_nummer", "r_datum", "r_kasse", "r_name", "r_klasse", "r_betrag", "r_offen", "r_bezdatum",
            "r_zuzahl", "r_mahndat1", "r_mahndat2", "r_mahndat3", "mahnsperr", "pat_intern", "ikktraeger", "id", "ik" };
    OffenepostenTab eltern = null;

    private OpShowGesamt sumPan;

    private JRtaCheckBox bar = null;
    
    public OffenepostenPanel(OffenepostenTab xeltern) {
        super();
        this.eltern = xeltern;
        startKeyListener();
        startActionListener();
        setLayout(new BorderLayout());
        add(getContent(), BorderLayout.CENTER);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setzeFocus();
            }
        });

    }

    public void setzeFocus() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                suchen.requestFocus();
            }
        });
    }

    private JPanel getContent(){
        FormLayout lay = new FormLayout(
        // 1    2     3    4     5    6  7    8       9     10   11    12   13    14   15    16   17    18   19
        "10dlu,50dlu,2dlu,90dlu,10dlu,p,2dlu,70dlu:g,40dlu,5dlu,50dlu,5dlu,50dlu,5dlu,50dlu,5dlu,50dlu,10dlu,p",    // xwerte,
        //  1   2  3     4       5   6  7     8   9  10   11
         "10dlu,p,5dlu,160dlu:g,8dlu,p,10dlu,2dlu,p,0dlu,0dlu"                                                        // ywerte
        );
        PanelBuilder builder = new PanelBuilder(lay);
        //PanelBuilder builder = new PanelBuilder(lay, new FormDebugPanel());        // debug mode
        builder.getPanel().setOpaque(false);
        CellConstraints cc = new CellConstraints();

        int colCnt=2, rowCnt=2;
        
        builder.addLabel("Suchkriterium", cc.xy(colCnt++, rowCnt));            // 2,2
        
        String[] args = {"Rechnungsnummer =","Rechnungsnummer >=","Rechnungsnummer <=",
                "Rechnungsbetrag =","Rechnungsbetrag >=","Rechnungsbetrag <=",
                "Noch offen =","Noch offen >=","Noch offen <=",
                "Name enthält",
                "Rezeptnummer =",
                "Rechnungsdatum =","Rechnungsdatum >=","Rechnungsdatum <=",
                "Krankenkasse enthält",};
        
        int vorauswahl = OffenePosten.getVorauswahl(args.length);
        combo = new JRtaComboBox(args);
        combo.setSelectedIndex( vorauswahl ); 
        builder.add(combo, cc.xy(++colCnt,rowCnt));                            // 4,2

        ++colCnt;
        builder.addLabel("finde:", cc.xy(++colCnt, rowCnt));                // 6,2
        ++colCnt;
        suchen = new JRtaTextField("nix",true);
        suchen.setName("suchen");
        suchen.addKeyListener(kl);
        builder.add(suchen,cc.xy(++colCnt, rowCnt,CellConstraints.FILL,CellConstraints.DEFAULT));    // 8,2

        buts[btSuchen] = ButtonTools.macheButton("suchen", "suchen", al);
        buts[btSuchen].setMnemonic('s');
        colCnt +=9;
        builder.add(buts[btSuchen],cc.xy(colCnt,rowCnt));                    // 17,2

        while (!OffenePosten.DbOk) {

        }
        tabmod = new MyOffenePostenTableModel();

        tabmod.setColumnIdentifiers(spalten);
        tab = new JXTable(tabmod);
        tab.setHorizontalScrollEnabled(true);
        tab.getColumn(0)
           .setCellRenderer(new MitteRenderer());

        // tab.getColumn(1).setCellEditor();
        DateTableCellEditor tble = new DateTableCellEditor();
        tab.getColumn(4)
           .setCellRenderer(new MitteRenderer());
        tab.getColumn(5)
           .setCellRenderer(new DoubleTableCellRenderer());
        tab.getColumn(6)
           .setCellRenderer(new DoubleTableCellRenderer());
        tab.getColumn(8)
           .setCellRenderer(new DoubleTableCellRenderer());
        tab.getColumn(5)
           .setCellEditor(new DblCellEditor());
        tab.getColumn(6)
           .setCellEditor(new DblCellEditor());
        tab.getColumn(8)
           .setCellEditor(new DblCellEditor());

        tab.getColumn(1)
           .setCellEditor(tble);
        tab.getColumn(7)
           .setCellEditor(tble);
        tab.getColumn(9)
           .setCellEditor(tble);
        tab.getColumn(10)
           .setCellEditor(tble);
        tab.getColumn(11)
           .setCellEditor(tble);

        tab.getSelectionModel()
           .addListSelectionListener(new OPListSelectionHandler());
        tab.setHighlighters(HighlighterFactory.createSimpleStriping(HighlighterFactory.CLASSIC_LINE_PRINTER));

        JScrollPane jscr = JCompTools.getTransparentScrollPane(tab);
        rowCnt +=2;
        builder.add(jscr,cc.xyw(2,rowCnt++,17));        // 2,4

        colCnt = 11;
        //colCnt = 13;    // ohne 'bar in Kasse'
        builder.addLabel("Geldeingang:", cc.xy(colCnt++, ++rowCnt,CellConstraints.RIGHT,CellConstraints.TOP));    // 12,6

        tfs[0] = new JRtaTextField("F",true,"6.2","");
        tfs[0].setHorizontalAlignment(SwingConstants.RIGHT);
        tfs[0].setText("0,00");
        tfs[0].setName("offen");
        tfs[0].addKeyListener(kl);
        //content.add(tfs[0],cc.xy(14,2));
        builder.add(tfs[0],cc.xy(++colCnt,rowCnt));                                                                // 14,6

        ++colCnt;
        bar  = (JRtaCheckBox) builder.add(new JRtaCheckBox("bar in Kasse"), cc.xy(++colCnt,rowCnt));
        bar.setEnabled(OffenePosten.getBarAusbuchenErlaubt());
        if(!bar.isEnabled()){
            bar.setToolTipText("disabled (see System-Init)");
            }

        colCnt +=2;
        buts[btAusbuchen] = (JButton) builder.add(ButtonTools.macheButton("ausbuchen", "ausbuchen", al),cc.xy(colCnt,rowCnt));
        buts[btAusbuchen].setMnemonic('a');
        
        rowCnt +=2;
        colCnt = 1;
        builder.add(new JSeparator(SwingConstants.HORIZONTAL), cc.xyw(colCnt,rowCnt++,17));
        
        sumPan = new OpShowGesamt();
        builder.add(sumPan.getPanel(),cc.xyw(colCnt, rowCnt,2,CellConstraints.LEFT,CellConstraints.TOP));    //2,2

        ermittleGesamtOffen();
        
        return builder.getPanel();        
    }

    private OffenepostenPanel getInstance() {
        return this;
    }

    private void startKeyListener() {
        kl = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    arg0.consume();
                    if (((JComponent) arg0.getSource()).getName()
                                                       .equals("suchen")) {
                        sucheEinleiten();
                        return;
                    } else if (((JComponent) arg0.getSource()).getName()
                                                              .equals("offen")) {
                        setzeFocus();
                    }
                }

            }

            @Override
            public void keyReleased(KeyEvent arg0) {

            }

            @Override
            public void keyTyped(KeyEvent arg0) {

            }

        };
    }

    private void startActionListener() {
        al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String cmd = arg0.getActionCommand();
                if (cmd.equals("ausbuchen")) {
                    tabmod.removeTableModelListener(getInstance());
                    doAusbuchen();
                    tabmod.addTableModelListener(getInstance());
                    setzeFocus();
                }
                if (cmd.equals("suchen")) {
                    sucheEinleiten();
                }
            }

        };
    }

    public void refreshData() {
        sucheEinleiten();
    }

    private void sucheEinleiten() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    OffenePosten.thisFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    setzeFocus();
                    tabmod.removeTableModelListener(getInstance());
                    doSuchen();
                    schreibeAbfrage();
                    tabmod.addTableModelListener(getInstance());
                    suchen.setEnabled(true);
                    buts[0].setEnabled(true);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Fehler beim einlesen der Datensätze");
                    OffenePosten.thisFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    setzeFocus();
                    suchen.setEnabled(true);
                    buts[0].setEnabled(true);
                }
                OffenePosten.thisFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                setzeFocus();
                return null;
            }
        }.execute();
    }

    private void setzeBezahlBetrag(final int i) {
        tfs[0].setText(dcf.format(tabmod.getValueAt(tab.convertRowIndexToModel(i), 6)));
    }

    private void doAusbuchen() {

        int row = tab.getSelectedRow();
        String sTmp = tfs[0].getText();
        int iDot = sTmp.indexOf('.'), iComma = sTmp.indexOf(',');
        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Keine Rechnung zum Ausbuchen ausgewählt");
            return;
        }
        BigDecimal nochoffen = BigDecimal.valueOf((Double) tabmod.getValueAt(tab.convertRowIndexToModel(row), 6));
        if ((iDot > -1) && (iComma > -1) && iDot < iComma) { // Eintrag enthält 1000er-Trennung (z.B. Kopie aus Browser)
            sTmp = sTmp.replace(".", "");
        }
        BigDecimal eingang = BigDecimal.valueOf(Double.parseDouble(sTmp.replace(",", ".")));
        BigDecimal restbetrag = nochoffen.subtract(eingang);

        if (nochoffen.equals(BigDecimal.valueOf(Double.parseDouble("0.0")))) {
            JOptionPane.showMessageDialog(null, "Diese Rechnung ist bereits auf bezahlt gesetzt");
            return;
        }
        suchOffen = suchOffen.subtract(eingang);
        gesamtOffen = gesamtOffen.subtract(eingang);

        if(bar.isSelected()){            // Buchung in Bar-Kasse
            String op_rechnum = tabmod.getValueAt(tab.convertRowIndexToModel(row), 0).toString();
            String op_patId = tabmod.getValueAt(tab.convertRowIndexToModel(row), 13).toString();
            if(op_patId.equals("")){                // wenn leer war's 'ne Rechnung an 'ne Kasse
                JOptionPane.showMessageDialog(null,"Die Kasse hat bar gezahlt? - Glaub' ich nicht! \nAusbuchen abgebrochen!");
                bar.setSelected(false);
                return;                
            }
            String op_patient = tabmod.getValueAt(tab.convertRowIndexToModel(row), 2).toString();         // bei Privatrechng steht hier 'Name,Vorname'
            String op_sparte = tabmod.getValueAt(tab.convertRowIndexToModel(row), 4).toString();
            String op_reznum = SqlInfo.holeEinzelFeld("select rez_nr from faktura where rnummer='"+op_rechnum+"' LIMIT 1");

            String cmd = "insert into kasse set einnahme='"+dcf.format(eingang).replace(",", ".")+"', "
                    + "datum='"+ DatFunk.sDatInSQL(DatFunk.sHeute())+"', " 
                    + "ktext='R-"+ op_sparte + " " + op_rechnum+","+ op_patient +"', rez_nr='"+op_reznum+"'";
            //System.out.println(cmd);
            SqlInfo.sqlAusfuehren(cmd);

            bar.setSelected(false);
        }
        
        tabmod.setValueAt(restbetrag.doubleValue(), tab.convertRowIndexToModel(row), 6);
        tabmod.setValueAt(new Date(), tab.convertRowIndexToModel(row), 7);

        int id = (Integer) tabmod.getValueAt(tab.convertRowIndexToModel(row), 15);
        String cmd = "update rliste set r_offen='" + Double.toString(restbetrag.doubleValue())
                                                           .replace(",", ".")
                + "', r_bezdatum='" + DatFunk.sDatInSQL(DatFunk.sHeute()) + "' where id ='" + Integer.toString(id)
                + "' LIMIT 1";

        if (!OffenePosten.testcase) {
            SqlInfo.sqlAusfuehren(cmd);
        }
        schreibeAbfrage();
        tfs[0].setText("0,00");
    }

    private void ermittleGesamtOffen() {
        Vector<Vector<String>> offen = SqlInfo.holeFelder("select sum(r_offen) from rliste where r_offen > '0.00'");
        gesamtOffen = BigDecimal.valueOf(Double.parseDouble(offen.get(0)
                                                                 .get(0)));
        schreibeGesamtOffen();
    }

    private void schreibeAbfrage() {
        schreibeGesamtOffen();
        sumPan.setSuchOffen(suchOffen);
        sumPan.schreibeSuchOffen();
        sumPan.setSuchGesamt(suchGesamt);
        sumPan.schreibeSuchGesamt();
        sumPan.setAnzRec(gefunden);
        sumPan.schreibeAnzRec();
    }

    private void schreibeGesamtOffen() {
        sumPan.setGesamtOffen(gesamtOffen);
        sumPan.schreibeGesamtOffen();
    }

    private void doSuchen() {
        String searchStr = suchen.getText()
                                 .trim();
        String searchStrNumVal = searchStr.replace(",", ".");
        if (searchStr.equals("")) {
            return;
        }
        int suchart = combo.getSelectedIndex();
        OffenePosten.setVorauswahl(suchart); // Auswahl merken
        String cmd = "";
        try {
            switch (suchart) {
            case 0: // Rechnungsnummer =
                cmd = "select * from rliste where r_nummer ='" + searchStr + "'";
                break;
            case 1: // >=
                cmd = "select * from rliste where r_nummer >='" + searchStr + "'";
                break;
            case 2: // <=
                cmd = "select * from rliste where r_nummer <='" + searchStr + "'";
                break;
            case 3: // Rechnungsbetrag =
                cmd = "select * from rliste where r_betrag ='" + searchStrNumVal + "'";
                break;
            case 4: // >=
                cmd = "select * from rliste where r_betrag >='" + searchStrNumVal + "'";
                break;
            case 5: // <=
                cmd = "select * from rliste where r_betrag <='" + searchStrNumVal + "'";
                break;
            case 6: // offen =
                cmd = "select * from rliste where r_offen ='" + searchStrNumVal + "'";
                break;
            case 7: // offen >=
                cmd = "select * from rliste where r_offen >='" + searchStrNumVal + "'";
                break;
            case 8: // offen <=
                cmd = "select * from rliste where r_offen <='" + searchStrNumVal + "'";
                break;
            case 9: // Name enthält
                cmd = "select * from rliste where r_name like'%" + searchStr + "%'";
                break;
            case 10: // Rezeptnummer =
                cmd = "select * from rliste where r_nummer = (select rnummer from faktura where rez_nr = '" + searchStr
                        + "'  LIMIT 1)";
                break;
            case 11: // Rechnungsdatum =
                cmd = "select * from rliste where r_datum ='" + DatFunk.sDatInSQL(searchStr) + "'";
                break;
            case 12: // Rechnungsdatum >=
                cmd = "select * from rliste where r_datum >='" + DatFunk.sDatInSQL(searchStr) + "'";
                break;
            case 13: // Rechnungsdatum <=
                cmd = "select * from rliste where r_datum <='" + DatFunk.sDatInSQL(searchStr) + "'";
                break;
            case 14: // Krankenkasse enthält
                cmd = "select * from rliste where r_kasse like'%" + suchen.getText() + "%'";
                break;

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!cmd.equals("")) {
            buts[0].setEnabled(false);
            suchen.setEnabled(false);
            try {
                starteSuche(cmd);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Korrekte Auflistung des Suchergebnisses fehlgeschlagen");
            }
            OffenePosten.thisFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

            suchen.setEnabled(true);
            buts[0].setEnabled(true);
            setzeFocus();
        }

    }

    public void benachrichtigeReha(final int i) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String patintern = tab.getValueAt(i, 13)
                                      .toString()
                                      .trim();
                if (!patintern.equals("")) {
                    new SocketClient().setzeRehaNachricht(OffenePosten.rehaReversePort,
                            "OffenePosten#" + RehaIOMessages.MUST_PATFIND + "#" + patintern);
                }
            }
        });
    }

    public void benachrichtigeBillPanel(final int i) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                eltern.setOnBillPanel(tab.getValueAt(i, 0)
                                         .toString()
                                         .trim());
            }
        });
    }

    class MyOffenePostenTableModel extends DefaultTableModel {
        /**
        * 
        */
        private static final long serialVersionUID = 1L;

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
            case 0:
            case 15:
                return Integer.class;
            case 1:
            case 7:
            case 9:
            case 10:
            case 11:
                return Date.class;
            case 2:
            case 3:
            case 4:
            case 13:
            case 14:
                return String.class;
            case 5:
            case 6:
            case 8:
                return Double.class;
            case 12:
                return Boolean.class;

            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {

            if (col < 15) {
                return true;
            }
            return false;
        }

    }

    private void starteSuche(String sstmt) {
        tabmod.setRowCount(0);
        tab.validate();
        tab.repaint();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = OffenePosten.thisClass.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException e) {

            e.printStackTrace();
        }
        try {
            rs = stmt.executeQuery(sstmt);
            Vector<Object> vec = new Vector<Object>();
            int durchlauf = 0;
            suchOffen = BigDecimal.valueOf(Double.parseDouble("0.00"));
            suchGesamt = BigDecimal.valueOf(Double.parseDouble("0.00"));
            gefunden = 0;
            while (rs.next()) {
                vec.clear();
                vec.add(rs.getInt(1)); // r_nummer
                vec.add(rs.getDate(2)); // r_datum
                vec.add((rs.getString(3) == null ? "" : rs.getString(3)));// r_kasse
                vec.add((rs.getString(4) == null ? "" : rs.getString(4)));// r_name
                vec.add((rs.getString(5) == null ? "" : rs.getString(5)));// r_klasse
                vec.add(rs.getBigDecimal(6)
                          .doubleValue());// r_betrag
                vec.add(rs.getBigDecimal(7)
                          .doubleValue());// r_offen
                vec.add(rs.getDate(8));// r_bezdatum
                vec.add(rs.getBigDecimal(9)
                          .doubleValue());// r_zuzahl
                vec.add(rs.getDate(10));// mahndat1
                vec.add(rs.getDate(11));// mahndat2
                vec.add(rs.getDate(12));// mahndat3
                vec.add((rs.getString(13) == null ? Boolean.FALSE
                        : (rs.getString(13)
                             .equals("T") ? Boolean.TRUE : Boolean.FALSE)));// mahnsperr
                vec.add((rs.getString(14) == null ? "" : rs.getString(14)));// pat_intern
                vec.add((rs.getString(15) == null ? "" : rs.getString(15)));// ikktraeger
                vec.add(rs.getInt(16));// id

                suchOffen = suchOffen.add(rs.getBigDecimal(7));
                suchGesamt = suchGesamt.add(rs.getBigDecimal(6));
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
                gefunden++;
            }

            tab.validate();
            tab.repaint();
            if (tab.getRowCount() > 0) {
                tab.setRowSelectionInterval(0, 0);
                adjustColumns();
            }

        } catch (SQLException ev) {
            System.out.println("SQLException: " + ev.getMessage());
            System.out.println("SQLState: " + ev.getSQLState());
            System.out.println("VendorError: " + ev.getErrorCode());
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

    }

    private void adjustColumns() {
        /*
         * ausgewaehlte Spalten dem Inhalt anpassen
         */
        int columns2adjust[]={0, 2, 4, 6, 8};    // Rechn.Nr., Kasse, Sparte, Offen, Zuzahlung
        for(int col:columns2adjust){
            tab.packColumn(col, 5);
        }
    }
    /*****************************************************/
    class OPListSelectionHandler implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
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
                        benachrichtigeReha(i);
                        benachrichtigeBillPanel(i);
                        setzeBezahlBetrag(i);
                        break;
                    }
                }
            }

        }
    }

    @Override
    public void tableChanged(TableModelEvent arg0) {
        if (arg0.getType() == TableModelEvent.INSERT) {
            System.out.println("Insert");
            return;
        }
        if (arg0.getType() == TableModelEvent.UPDATE) {
            try {
                int col = arg0.getColumn();
                int row = arg0.getFirstRow();
                // String colname = tabmod.getColumnName(col).toString();
                String colname = colnamen[col].toString();
                String value = "";
                String id = Integer.toString((Integer) tabmod.getValueAt(row, 15));
                if (tabmod.getColumnClass(col) == Boolean.class) {
                    value = (tabmod.getValueAt(row, col) == Boolean.FALSE ? "F" : "T");
                } else if (tabmod.getColumnClass(col) == Date.class) {
                    if (tabmod.getValueAt(row, col) == null) {
                        value = "1900-01-01";
                    } else {
                        String test = tabmod.getValueAt(row, col)
                                            .toString();
                        if (test.contains(".")) {
                            if (test.trim()
                                    .length() == 10) {
                                value = DatFunk.sDatInSQL(test);
                            } else {
                                value = null;
                            }
                        } else if (test.trim()
                                       .equals("-  -")) {
                            value = null;
                        } else {
                            value = test;
                        }
                    }
                    // value = tabmod.getValueAt(row,col).toString();
                } else if (tabmod.getColumnClass(col) == Double.class) {
                    value = dcf.format(tabmod.getValueAt(row, col))
                               .replace(",", ".");
                } else if (tabmod.getColumnClass(col) == Integer.class) {
                    value = Integer.toString((Integer) tabmod.getValueAt(row, 15));
                } else if (tabmod.getColumnClass(col) == String.class) {
                    value = tabmod.getValueAt(row, col)
                                  .toString();
                }
                String cmd = "update rliste set " + colname + "=" + (value != null ? "'" + value + "'" : "null")
                        + " where id='" + id + "' LIMIT 1";
                // System.out.println(cmd);
                SqlInfo.sqlAusfuehren(cmd);
                tfs[0].setText(dcf.format(tabmod.getValueAt(tab.convertRowIndexToModel(row), 6)));

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Fehler in der Dateneingbe");
            }
            return;
        }

    }

}
