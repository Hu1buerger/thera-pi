package opRgaf;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.*;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextFieldService;
import ag.ion.bion.officelayer.text.TextException;
import ag.ion.noa.NOAException;
import environment.Path;
import io.RehaIOMessages;
import mandant.IK;
import office.OOService;
import opRgaf.RehaIO.SocketClient;
import sql.DatenquellenFactory;

class OpRgafPanel extends JXPanel implements TableModelListener, RgAfVk_IfCallBack, OPRGAFGui {
    private static final long serialVersionUID = -7883557713071422132L;

    private JRtaTextField suchen;

    private JButton suchenBtn;
    private JButton ausbuchenBtn;
    private JButton kopieButton;

    private JRtaComboBox combo;

    private KeyListener kl;

    private ActionListener al;

    private MyOpRgafTableModel tabmod;

    private JXTable tab;


    private JRtaCheckBox bar;
    private boolean barWasSelected;

    private DecimalFormat dcf = new DecimalFormat("###0.00");

    private HashMap<String, String> hmRezgeb = new HashMap<>();

    private final String stmtString = "SELECT concat(t2.n_name, ', ',t2.v_name,', ',DATE_FORMAT(t2.geboren,'%d.%m.%Y')),t1.rnr,t1.rdatum,t1.rgesamt,"
            + "t1.roffen,t1.rpbetrag,t1.rbezdatum,t1.rmahndat1,t1.rmahndat2,t3.kassen_nam1,t1.reznr,t1.id,t1.pat_id "
            + "FROM (SELECT v_nummer as rnr,v_datum as rdatum,v_betrag as rgesamt,v_offen as roffen,'' as rpbetrag,"
            + "v_bezahldatum as rbezdatum,mahndat1 as rmahndat1,mahndat2 as rmahndat2,'' as reznr,verklisteid as id,pat_id as pat_id "
            + "FROM verkliste where v_nummer like 'VR-%' "
            + "UNION SELECT rnr,rdatum,rgesamt,roffen,rpbetrag,rbezdatum,rmahndat1,rmahndat2,reznr,id as id,pat_intern as pat_id "
            + "FROM rgaffaktura ) t1 LEFT JOIN pat5 AS t2 ON (t1.pat_id = t2.pat_intern) LEFT JOIN kass_adr AS t3 ON ( t2.kassenid = t3.id )";

    private String[] spalten = { "Name,Vorname,Geburtstag", "Rechn.Nr.", "Rechn.Datum", "Gesamtbetrag", "Offen",
            "Bearb.Gebühr", "bezahlt am", "1.Mahnung", "2.Mahnung", "Krankenkasse", "RezeptNr.", "id" };

    private String[] colnamen = { "nix", "rnr", "rdatum", "rgesamt", "roffen", "rpbetrag", "rbezdatum", "rmahndat1",
            "rmahndat2", "nix", "nix", "id" };

    private class IdxCol {
        /** Indices fuer sprechende Spaltenzugriffe. */

        static final short Name = 0;
        static final short RNr = 1;
        static final short RDat = 2;
        static final short GBetr = 3;
        static final short Offen = 4;
        static final short BGeb = 5;
        static final short bez = 6;
        static final short mahn1 = 7;
        static final short mahn2 = 8;
        static final short kk = 9;
        static final short RezNr = 10;
        static final short id = 11;
    }



    private OpShowGesamt sumPan;
    private RgAfVkSelect selPan;

    private OpRgAfIni iniOpRgAf;

    private IK ik;

    private JRtaTextField geldeingangTf;

    private Logger logger = LoggerFactory.getLogger(OpRgafPanel.class);

    OpRgafPanel(OpRgafTab xeltern, OpRgaf opRgaf, IK ik) {

        this.iniOpRgAf = opRgaf.iniOpRgAf;
        this.ik = ik;
        startKeyListener();
        startActionListener();
        setLayout(new BorderLayout());
        add(getContent(), BorderLayout.CENTER);
        setzeFocus();
    }

    public void setzeFocus() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                suchen.requestFocus();
            }
        });
    }

    private JPanel getContent() {
        // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19
        String xwerte = "10dlu,50dlu,2dlu,90dlu,10dlu,p,2dlu,70dlu:g,40dlu,5dlu,50dlu,5dlu,50dlu,5dlu,50dlu,5dlu,50dlu,10dlu,p";
        // 1 2 3 4 5 6 7 8 9 10 11
        String ywerte = "15dlu,p,15dlu,160dlu:g,8dlu,p,10dlu,2dlu,p,8dlu,0dlu";
        FormLayout lay = new FormLayout(xwerte, ywerte);
        PanelBuilder builder = new PanelBuilder(lay);

        builder.getPanel()
               .setOpaque(false);
        CellConstraints cc = new CellConstraints();

        int colCnt = 2, rowCnt = 2;

        builder.addLabel("Suchkriterium", cc.xy(colCnt++, rowCnt)); // 2,2

        String[] args = { "Rechnungsnummer =", "Rechnungsnummer enthält", "Rechnungsbetrag =", "Rechnungsbetrag >=",
                "Rechnungsbetrag <=", "Noch offen =", "Noch offen >=", "Noch offen <=", "Pat. Nachname beginnt mit",
                "Rezeptnummer =", "Rechnungsdatum =", "Rechnungsdatum >=", "Rechnungsdatum <=",
                "Krankenkasse enthält" };

        int vorauswahl = iniOpRgAf.getVorauswahl(args.length);
        combo = new JRtaComboBox(args);
        combo.setSelectedIndex(vorauswahl);
        builder.add(combo, cc.xy(++colCnt, rowCnt)); // 4,2

        ++colCnt;
        builder.addLabel("finde:", cc.xy(++colCnt, rowCnt)); // 6,2

        ++colCnt;
        suchen = new JRtaTextField("nix", true);
        suchen.setName("suchen");
        suchen.addKeyListener(kl);
        builder.add(suchen, cc.xy(++colCnt, rowCnt, CellConstraints.FILL, CellConstraints.DEFAULT)); // 8,2

        // Auswahl RGR/AFR/Verkauf
        colCnt += 2;
        selPan = new RgAfVkSelect("suche in  "); // Subpanel mit Checkboxen anlegen
        selPan.setCallBackObj(this); // callBack registrieren
        initSelection();

        builder.add(selPan.getPanel(),
                cc.xywh(++colCnt, rowCnt - 1, 5, 3, CellConstraints.LEFT, CellConstraints.DEFAULT)); // 10..15,1..3
        // Ende Auswahl

        suchenBtn = ButtonTools.macheButton("suchen", "suchen", al);
        suchenBtn.setMnemonic(KeyEvent.VK_S);
        builder.add(suchenBtn, cc.xy(17, rowCnt));

        tabmod = new MyOpRgafTableModel();
        tabmod.setColumnIdentifiers(spalten);
        tab = new JXTable(tabmod);
        tab.setHorizontalScrollEnabled(true);

        DateTableCellEditor tble = new DateTableCellEditor();
        tab.getColumn(1)
           .setCellRenderer(new MitteRenderer());

        tab.getColumn(2)
           .setCellEditor(tble);

        tab.getColumn(3)
           .setCellRenderer(new DoubleTableCellRenderer());
        tab.getColumn(3)
           .setCellEditor(new DblCellEditor());

        tab.getColumn(4)
           .setCellRenderer(new DoubleTableCellRenderer());
        tab.getColumn(4)
           .setCellEditor(new DblCellEditor());

        tab.getColumn(5)
           .setCellRenderer(new DoubleTableCellRenderer());
        tab.getColumn(5)
           .setCellEditor(new DblCellEditor());

        tab.getColumn(6)
           .setCellEditor(tble);
        tab.getColumn(7)
           .setCellEditor(tble);
        tab.getColumn(8)
           .setCellEditor(tble);
        tab.getColumn(10)
           .setMinWidth(80);
        tab.getColumn(11)
           .setMaxWidth(50);
        tab.getSelectionModel()
           .addListSelectionListener(new OPListSelectionHandler());
        tab.setHighlighters(HighlighterFactory.createSimpleStriping(HighlighterFactory.CLASSIC_LINE_PRINTER));

        JScrollPane jscr = JCompTools.getTransparentScrollPane(tab);
        rowCnt += 2;
        builder.add(jscr, cc.xyw(2, rowCnt, 17)); // 2,4
//**********************

        rowCnt += 2; // 6
        colCnt = 4;
        kopieButton = ButtonTools.macheButton("Rechnungskopie", "kopie", al);
         builder.add(kopieButton, cc.xy(colCnt, rowCnt)); // 4,6
        colCnt = 11;
        builder.addLabel("Geldeingang:", cc.xy(colCnt, rowCnt, CellConstraints.RIGHT, CellConstraints.TOP)); // 12,6

        ++colCnt;
        geldeingangTf = new JRtaTextField("F", true, "6.2", "");
        geldeingangTf.setHorizontalAlignment(SwingConstants.RIGHT);
        geldeingangTf.setText("0,00");
        geldeingangTf.setName("offen");
        geldeingangTf.addKeyListener(kl);
        builder.add(geldeingangTf, cc.xy(++colCnt, rowCnt)); // 14,6

        ++colCnt;
        bar = (JRtaCheckBox) builder.add(new JRtaCheckBox("bar in Kasse"), cc.xy(++colCnt, rowCnt));
        if ("Kasse".equals(iniOpRgAf.getWohinBuchen())) {
            bar.setSelected(true);
        }

        ausbuchenBtn = ButtonTools.macheButton("ausbuchen", "ausbuchen", al);
        builder.add(ausbuchenBtn, cc.xy(17, 6));
        ausbuchenBtn.setMnemonic(KeyEvent.VK_A);
//**********************

        rowCnt += 2;
        colCnt = 1;
        builder.add(new JSeparator(SwingConstants.HORIZONTAL), cc.xyw(colCnt, rowCnt++, 19));

        sumPan = new OpShowGesamt();
        builder.add(sumPan.getPanel(), cc.xyw(colCnt, rowCnt, 2, CellConstraints.LEFT, CellConstraints.TOP)); // 2,2

        calcGesamtOffen();

        return builder.getPanel();
    }

    /** Letzte Checkbox-Auswahl wiederherstellen. */
    void initSelection() {
        selPan.setRGR(iniOpRgAf.getIncRG());
        selPan.setAFR(iniOpRgAf.getIncAR());
        selPan.setVKR(iniOpRgAf.getIncVK());
        if (!selPan.useRGR() && !selPan.useAFR() && !selPan.useVKR()) {
            selPan.setRGR(Boolean.TRUE); // einer sollte immer ausgewählt sein
        }
    }

    private void calcGesamtOffen() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                sumPan.ermittleGesamtOffen(selPan.useRGR(), selPan.useAFR(), selPan.useVKR());
                return null;
            }

        }.execute();
    }

    private void startKeyListener() {
        kl = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    arg0.consume();
                    if ("suchen".equals(((JComponent) arg0.getSource()).getName())) {
                        sucheEinleiten();
                    } else if ("offen".equals(((JComponent) arg0.getSource()).getName())) {
                        setzeFocus();
                    }
                }
            }
        };
    }

    private void startActionListener() {
        al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String cmd = arg0.getActionCommand();
                if ("ausbuchen".equals(cmd)) {
                    tabmod.removeTableModelListener(OpRgafPanel.this);
                    doAusbuchen();
                    tabmod.addTableModelListener(OpRgafPanel.this);
                    setzeFocus();
                    return;
                }
                if ("kopie".equals(cmd)) {
                    doKopie();
                    setzeFocus();
                    return;
                }
                if ("suchen".equals(cmd)) {
                    sucheEinleiten();
                }
            }
        };
    }

    private void doKopie() {
        if (tabmod.getRowCount() <= 0) {
            return;
        }
        final String rnr = tab.getValueAt(tab.getSelectedRow(), 1)
                              .toString();
        if (rnr.startsWith("AFR")) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        String id = tab.getValueAt(tab.getSelectedRow(), 11)
                                       .toString();
                        String rez_nr = getFieldFromRgaffAtId ("reznr", id);
                        String pat_intern = getFieldFromRgaffAtId ("pat_intern", id);
                        String rdatum = getFieldFromRgaffAtId ("rdatum", id);
                        AusfallRechnung ausfall = new AusfallRechnung(pat_intern, rez_nr,
                                rnr, rdatum, OpRgafPanel.this.ik);
                        ausfall.setModal(true);
                        ausfall.setLocationRelativeTo(null);
                        ausfall.toFront();
                        ausfall.setVisible(true);
                        ausfall = null;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
            }.execute();
            return;
        }
        if (rnr.startsWith("RGR")) {
            doRezeptgebKopie();
        }
    }

    private void setzeBezahlBetrag(final int i) {
        geldeingangTf.setText(dcf.format(tabmod.getValueAt(tab.convertRowIndexToModel(i), IdxCol.Offen)));
    }

    private void sucheEinleiten() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    setzeFocus();
                    tabmod.removeTableModelListener(OpRgafPanel.this);
                    doSuchen();
                    schreibeAbfrage();
                    tabmod.addTableModelListener(OpRgafPanel.this);
                    suchen.setEnabled(true);
                    ausbuchenBtn.setEnabled(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Fehler beim einlesen der Datensätze");
                    setzeFocus();
                    suchen.setEnabled(true);
                    ausbuchenBtn.setEnabled(true);
                }
                setzeFocus();
                return null;
            }
        }.execute();
    }

    private void doAusbuchen() {
        int row = tab.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Keine Rechnung zum Ausbuchen ausgewählt");
            return;
        }
        BigDecimal nochoffen = BigDecimal.valueOf(
                (Double) tabmod.getValueAt(tab.convertRowIndexToModel(row), IdxCol.Offen));
        BigDecimal eingang = BigDecimal.valueOf(Double.parseDouble(geldeingangTf.getText()
                                                                                .replace(",", ".")));
        BigDecimal restbetrag = nochoffen.subtract(eingang);

        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Keine Rechnung zum Ausbuchen ausgewählt");
            return;
        }

        if (nochoffen.compareTo(BigDecimal.ZERO) == 0) {
            JOptionPane.showMessageDialog(null, "Diese Rechnung ist bereits auf bezahlt gesetzt");
            return;
        }

        sumPan.substFromGesamtOffen(eingang);
        sumPan.substFromSuchOffen(eingang);

        String cmd = "";
        String rgaf_reznum = tabmod.getValueAt(tab.convertRowIndexToModel(row), IdxCol.RezNr)
                                   .toString();
        String rgaf_rechnum = tabmod.getValueAt(tab.convertRowIndexToModel(row), IdxCol.RNr)
                                    .toString();

        if (bar.isSelected()) {
            String ktext = rgaf_rechnum + "," + tabmod.getValueAt(tab.convertRowIndexToModel(row), IdxCol.Name);
            // Name, Vorname, Geburtstag (soweit 35 Zeichen reichen)
            if (ktext.length() > 35) {
                ktext = ktext.substring(0, 34);
            }
            cmd = "insert into kasse set einnahme='" + dcf.format(eingang)
                                                          .replace(",", ".")
                    + "', datum='" + DatFunk.sDatInSQL(DatFunk.sHeute()) + "', ktext='" + ktext + "'," + "rez_nr='"
                    + rgaf_reznum + "'";
            SqlInfo.sqlAusfuehren(cmd);
        }
        tabmod.setValueAt(new Date(), tab.convertRowIndexToModel(row), IdxCol.bez);
        tabmod.setValueAt(restbetrag.doubleValue(), tab.convertRowIndexToModel(row), IdxCol.Offen);

        if (rgaf_rechnum.startsWith("RGR-")) { // Rezept bezahlt setzen
            SqlInfo.sqlAusfuehren(
                    "update verordn set zzstatus='1', rez_bez='T' where rez_nr = '" + rgaf_reznum + "' LIMIT 1"); // zz:
                                                                                                                  // 1-ok
            SqlInfo.sqlAusfuehren(
                    "update lza set zzstatus='1', rez_bez='T' where rez_nr = '" + rgaf_reznum + "' LIMIT 1");
        }

        int id = (Integer) tabmod.getValueAt(tab.convertRowIndexToModel(row), IdxCol.id);
        if (rgaf_rechnum.startsWith("RGR-") || rgaf_rechnum.startsWith("AFR-")) { // aus rgaffaktura ausbuchen
            cmd = "update rgaffaktura set roffen='" + dcf.format(restbetrag)
                                                         .replace(",", ".")
                    + "', rbezdatum='" + DatFunk.sDatInSQL(DatFunk.sHeute()) + "' where id ='" + id + "' LIMIT 1";
        }
        if (rgaf_rechnum.startsWith("VR-")) { // aus verkliste ausbuchen
            cmd = "update verkliste set v_offen='" + dcf.format(restbetrag)
                                                        .replace(",", ".")
                    + "', v_bezahldatum='" + DatFunk.sDatInSQL(DatFunk.sHeute()) + "' where verklisteID ='" + id
                    + "' LIMIT 1";
        }
        SqlInfo.sqlAusfuehren(cmd);
        schreibeAbfrage();
        geldeingangTf.setText("0,00");
    }

    private void schreibeAbfrage() {
        sumPan.schreibeGesamtOffen();
        sumPan.schreibeSuchOffen();
        sumPan.schreibeSuchGesamt();
        sumPan.schreibeAnzRec();
    }

    public void sucheRezept(String rezept) { // Einstieg für RehaReverseServer (z.B. RGR-Kopie aus Historie)
        suchen.setText(rezept);
        combo.setSelectedItem("Rezeptnummer =");
        boolean useRGR = selPan.useRGR(); // Checkbox-Einstellung merken
        boolean useAFR = selPan.useAFR();
        boolean useVKR = selPan.useVKR();
        selPan.setRGR_AFR_VKR(true, false, false); // wird immer eine RGR gesucht?
        doSuchen();
        selPan.setRGR_AFR_VKR(useRGR, useRGR, useVKR); // Checkbox-Einstellung wiederherstellen
    }

    private void doSuchen() {
        if ("".equals(suchen.getText()
                            .trim())) {
            return;
        }
        int suchart = combo.getSelectedIndex();
        iniOpRgAf.setVorauswahl(suchart); // Auswahl merken
        combo.getItemAt(combo.getSelectedIndex())
             .toString();

        String cmd = "";
        String tmpStr = selPan.bills2search("rnr");
        String whereToSearch = " WHERE ";
        String searchStr = suchen.getText()
                                 .trim();
        String searchStrNumVal = searchStr.replace(",", ".");
        if (!tmpStr.isEmpty()) {
            whereToSearch = whereToSearch + " ( " + tmpStr + " ) AND ";
        }

        try {
            switch (suchart) {
            case 0:
                cmd = stmtString + " where rnr ='" + searchStr + "'";
                break;
            case 1: // Rechnungsnummer enthält
                if (searchStr.contains("sto") || searchStr.contains("tor") || searchStr.contains("orn")
                        || searchStr.contains("rno")) {
                    whereToSearch = sucheStornierte(whereToSearch);
                }
                cmd = stmtString + whereToSearch + " rnr like'%" + searchStr + "%' order by t1.id";
                break;
            case 2: // Rechnungsbetrag =
                cmd = stmtString + whereToSearch + " rgesamt ='" + searchStrNumVal + "' order by t1.id";
                break;
            case 3: // >=
                cmd = stmtString + whereToSearch + " rgesamt >='" + searchStrNumVal + "' order by t1.id";
                break;
            case 4: // <=
                cmd = stmtString + whereToSearch + " rgesamt <='" + searchStrNumVal + "' order by t1.id";
                break;
            case 5: // Noch offen =
                cmd = stmtString + whereToSearch + " roffen ='" + searchStrNumVal + "' order by t1.id";
                break;
            case 6: // >=
                cmd = stmtString + whereToSearch + " t1.roffen >='" + searchStrNumVal + "' order by t1.id";
                break;
            case 7: // <=
                cmd = stmtString + whereToSearch + " roffen <='" + searchStrNumVal + "' order by t1.id";
                break;
            case 8: // Nachname beginnt mit
                cmd = stmtString + whereToSearch + " t2.n_name like'" + searchStr + "%' order by t1.id";
                break;
            case 9: // Rezeptnummer =
                cmd = stmtString + whereToSearch + " t1.reznr ='" + searchStr + "'";
                break;
            case 10: // Rechnungsdatum =
                cmd = stmtString + whereToSearch + " rdatum ='" + DatFunk.sDatInSQL(searchStr) + "'";
                break;
            case 11: // >=
                cmd = stmtString + whereToSearch + " rdatum >='" + DatFunk.sDatInSQL(searchStr) + "'";
                break;
            case 12: // <=
                cmd = stmtString + whereToSearch + " rdatum <='" + DatFunk.sDatInSQL(searchStr) + "'";
                break;
            case 13: // Krankenkasse enthält
                cmd = stmtString + whereToSearch + " t3.kassen_nam1 like'%" + searchStr + "%'";
                break;
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
        }

        if (!"".equals(cmd)) {
            ausbuchenBtn.setEnabled(false);
            suchen.setEnabled(false);
            try {
                starteSuche(cmd);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Korrekte Auflistung des Suchergebnisses fehlgeschlagen");
            }

            suchen.setEnabled(true);
            ausbuchenBtn.setEnabled(true);
            setzeFocus();
        }
    }

    private String sucheStornierte(String whereToSearch) {
        String tmp = whereToSearch;
        if (whereToSearch.contains("RGR")) {
            tmp = tmp.replace("RGR", "storno_RGR");
        }
        if (whereToSearch.contains("AFR")) {
            tmp = tmp.replace("AFR", "storno_AFR");
        }
        if (whereToSearch.contains("VR")) {
            tmp = tmp.replace("VR", "storno_VR");
        }
        return tmp;
    }

    private class MyOpRgafTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1L;

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
            case 0:
            case 1:
            case 9:
            case 10:
                return String.class;
            case 2:
            case 6:
            case 7:
            case 8:
                return Date.class;
            case 3:
            case 4:
            case 5:
                return Double.class;
            case 11:
                return Integer.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col > 1 && col < 9) {
                return true;
            }
            return false;
        }
    }

    private void starteSuche(String sstmt) {
        tabmod.setRowCount(0);
        tab.validate();

        try (Connection connection = new DatenquellenFactory(ik.digitString()).createConnection();
                Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt);) {
            Vector<Object> vec = new Vector<Object>();
            int durchlauf = 0;
            sumPan.delSuchGesamt();
            sumPan.delSuchOffen();
            sumPan.delAnzRec();
            calcGesamtOffen();
            ResultSetMetaData rsMetaData = null;
            while (rs.next()) {
                vec.clear();
                rsMetaData = rs.getMetaData();
                int numberOfColumns = rsMetaData.getColumnCount() + 1;
                for (int i = 1; i < numberOfColumns; i++) {
                    String colClassName = rsMetaData.getColumnClassName(i)
                                                    .toString();
                    if (colClassName.equals("java.lang.String")) {
                        vec.add((rs.getString(i) == null ? "" : rs.getString(i)));
                    } else if (colClassName.equals("java.math.BigDecimal")) {
                        vec.add(rs.getBigDecimal(i)
                                  .doubleValue());
                    } else if (colClassName.equals("java.sql.Date")) {
                        try {
                            vec.add(rs.getDate(i));
                        } catch (SQLException e) {
                            vec.add(null);
                        }
                    } else if (colClassName.equals("java.lang.Integer")) {
                        vec.add(rs.getInt(i));
                    } else if (colClassName.equals("[B")) {
                        try {
                            vec.add(rs.getString(i));
                        } catch (SQLException e) {
                            vec.add(null);
                        }
                    } else {
                        logger.error("unexpected ColumnClassName '" + colClassName + "' at column " + i);                        
                    }
                }

                sumPan.setSuchGesamt(sumPan.getSuchGesamt()
                                           .add(rs.getBigDecimal(4)));
                sumPan.setSuchOffen(sumPan.getSuchOffen()
                                          .add(rs.getBigDecimal(5)));
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
                sumPan.incAnzRec();
            }

            tab.validate();
            tab.repaint();
            if (tab.getRowCount() > 0) {
                tab.setRowSelectionInterval(0, 0);
                adjustColumns();
            }
        } catch (SQLException ev) {
            logger.error("Datenbankfehler bei der Suche", ev);
        }
    }

    private void adjustColumns() {
        /* ausgewaehlte Spalten dem Inhalt anpassen */
        int columns2adjust[] = { 0, 4, 7, 8, 10 }; // Name,Vorname,Geburtstag, Offen, 1.Mahnung, 2.Mahnung, RezeptNr.
        for (int col : columns2adjust) {
            tab.packColumn(col, 5);
        }
    }

    private class OPListSelectionHandler implements ListSelectionListener {
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
                        setzeBezahlBetrag(i);
                        String id = tab.getValueAt(i, 11)
                                       .toString();
                        String rnr = tab.getValueAt(i, 1)
                                        .toString();
                        String rez_nr = getFieldFromRgaffAtId ("reznr", id);
                        String pat_intern = getFieldFromRgaffAtId ("pat_intern", id);
                        new SocketClient().setzeRehaNachricht(OpRgaf.rehaReversePort,
                                "OpRgaf#" + RehaIOMessages.MUST_PATANDREZFIND + "#" + pat_intern + "#" + rez_nr);

                        if (rnr.startsWith("VR-")) { // test ob VR -> bar ausbuchen enabled/disabled
                            if (!iniOpRgAf.getVrCashAllowed()) {
                                bar.setEnabled(false);
                                bar.setToolTipText("not allowed for VR (see System-Init)");
                                if (bar.isSelected()) { // falls 'bar in Kasse' gewählt war -> merken
                                    bar.setSelected(false);
                                    barWasSelected = true;
                                }
                            }
                        } else {
                            bar.setEnabled(true);
                            bar.setToolTipText("");
                            if (barWasSelected) { // Status 'bar in Kasse' wieder herstellen
                                bar.setSelected(true);
                                barWasSelected = false;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    private String getFieldFromRgaffAtId (String field, String id) {
        String data = SqlInfo.holeEinzelFeld(
                "select " + field + " from rgaffaktura where id='" + id + "' LIMIT 1");
        return data;
    }


    @Override
    public void tableChanged(TableModelEvent arg0) {
        if (arg0.getType() == TableModelEvent.INSERT) {
            logger.info("Insert");
            return;
        }
        if (arg0.getType() == TableModelEvent.UPDATE) {
            try {
                int col = arg0.getColumn();
                int row = arg0.getFirstRow();
                String colname = colnamen[col];
                String value = "";
                String id = Integer.toString((Integer) tabmod.getValueAt(row, 11));
                if (tabmod.getColumnClass(col) == Boolean.class) {
                    value = tabmod.getValueAt(row, col) == Boolean.FALSE ? "F" : "T";
                } else if (tabmod.getColumnClass(col) == Date.class) {
                    if (tabmod.getValueAt(row, col) == null) {
                        value = "1900-01-01";
                    } else {
                        String test = tabmod.getValueAt(row, col)
                                            .toString();
                        if (".  .".equals(test.trim())) {
                            value = null;
                        } else if (test.contains(".")) {
                            value = DatFunk.sDatInSQL(test);
                            if (value.equals("    -  -  ")) {
                                value = null;
                            }
                        } else if (test.equals("    -  -  ")) {
                            value = null;
                        } else {
                            value = test;
                        }
                    }
                } else if (tabmod.getColumnClass(col) == Double.class) {
                    value = dcf.format(tabmod.getValueAt(row, col))
                               .replace(",", ".");
                } else if (tabmod.getColumnClass(col) == String.class) {
                    value = tabmod.getValueAt(row, col)
                                  .toString();
                }
                String rnr = (String) tabmod.getValueAt(row, 1);
                if (rnr.startsWith("VR-")) { // test ob VR -> Änderung in 'verkliste' schreiben
                    HashMap<String, String> hmMap2VerkListe = new HashMap<>();
                    hmMap2VerkListe.put("rbezdatum", "v_bezahldatum"); // der Fluch der verbogenen Spaltennamen
                    hmMap2VerkListe.put("roffen", "v_offen");
                    hmMap2VerkListe.put("rgesamt", "v_betrag");
                    if (hmMap2VerkListe.containsKey(colname)) { // Ändern dieser Spalte ist erlaubt
                        String cmd = "update verkliste set " + hmMap2VerkListe.get(colname) + " ="
                                + (value != null ? "'" + value + "'" : "null") + " where verklisteID='" + id
                                + "' LIMIT 1";
                        logger.info(cmd);
                        SqlInfo.sqlAusfuehren(cmd);
                    } else {
                        new SwingWorker<Void, Void>() {
                            /** Andere 'rückgängig' machen (= Suche neu ausführen). */
                            @Override // eleganter wäre nur das geänderte Feld neu einzulesen ...
                            protected Void doInBackground() throws Exception {
                                try {
                                    doSuchen();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                return null;
                            }
                        }.execute();

                        JOptionPane.showMessageDialog(null, "Diese Änderung ist in Verkaufsrechnungen nicht möglich!");
                    }
                } else {
                    String cmd = "update rgaffaktura set " + colname + "="
                            + (value != null ? "'" + value + "'" : "null") + " where id='" + id + "' LIMIT 1";
                    SqlInfo.sqlAusfuehren(cmd);
                    geldeingangTf.setText(dcf.format(tabmod.getValueAt(tab.convertRowIndexToModel(row), IdxCol.Offen)));
                }
            } catch (Exception ex) {
                logger.error("Fehler in der Dateneingabe", ex);
                JOptionPane.showMessageDialog(null, "Fehler in der Dateneingabe");
            }
        }
    }

    private void doRezeptgebKopie() {
        if (tabmod.getRowCount() <= 0) {
            return;
        }
        String db = "";
        String id = tab.getValueAt(tab.getSelectedRow(), 11)
                       .toString();
        String rgnr = tab.getValueAt(tab.getSelectedRow(), 1)
                         .toString();
        String rez_nr = getFieldFromRgaffAtId ("reznr", id);
        String pat_intern = getFieldFromRgaffAtId ("pat_intern", id);
        String rdatum = getFieldFromRgaffAtId ("rdatum", id);
        String rezgeb = getFieldFromRgaffAtId ("rgbetrag", id);
        String pauschale = getFieldFromRgaffAtId ("rpbetrag", id);
        String gesamt = getFieldFromRgaffAtId ("rgesamt", id);
        logger.info("RezeptGebKopie = "+rez_nr);
        new InitHashMaps();

        String test = SqlInfo.holeEinzelFeld("select id from verordn where rez_nr = '" + rez_nr + "' LIMIT 1");
        Vector<String> vecaktrez = null;
        if ("".equals(test)) {
            test = SqlInfo.holeEinzelFeld("select id from lza where rez_nr = '" + rez_nr + "' LIMIT 1");
            if (!"".equals(test)) {
                vecaktrez = SqlInfo.holeSatz("lza",
                        " anzahl1,kuerzel1,kuerzel2," + "kuerzel3,kuerzel4,kuerzel5,kuerzel6 ", "id='" + test + "'",
                        Arrays.asList(new String[] {}));
                db = "lza";
            }
        } else {
            vecaktrez = SqlInfo.holeSatz("verordn",
                    " anzahl1,kuerzel1,kuerzel2," + "kuerzel3,kuerzel4,kuerzel5,kuerzel6 ", "id='" + test + "'",
                    Arrays.asList(new String[] {}));
            db = "verordn";
        }
        if (vecaktrez != null) {
            String behandlungen = vecaktrez.get(0) + "*" + (!"".equals(vecaktrez.get(1)
                                                                                .trim()) ? vecaktrez.get(1) : "")
                    + (!"".equals(vecaktrez.get(2)
                                           .trim()) ? "," + vecaktrez.get(2) : "")
                    + (!"".equals(vecaktrez.get(3)
                                           .trim()) ? "," + vecaktrez.get(3) : "")
                    + (!"".equals(vecaktrez.get(4)
                                           .trim()) ? "," + vecaktrez.get(4) : "")
                    + (!"".equals(vecaktrez.get(5)
                                           .trim()) ? "," + vecaktrez.get(5) : "")
                    + (!"".equals(vecaktrez.get(6)
                                           .trim()) ? "," + vecaktrez.get(6) : "");

            String cmd = "select abwadress,id from pat5 where pat_intern='" + pat_intern + "' LIMIT 1";
            Vector<Vector<String>> adrvec = SqlInfo.holeFelder(cmd);
            String[] adressParams = null;
            String patid = adrvec.get(0)
                                 .get(1);
            PatientenAdressen patientenAdressen = new PatientenAdressen(patid);
            if ("T".equals(adrvec.get(0)
                                 .get(0))) {
                adressParams = patientenAdressen.holeAbweichendeAdresse(patid);
            } else {
                adressParams = patientenAdressen.getAdressParams(patid);
            }

            hmRezgeb.put("<rgreznum>", rez_nr);
            hmRezgeb.put("<rgbehandlung>", behandlungen);
            hmRezgeb.put("<rgdatum>", DatFunk.sDatInDeutsch(
                    SqlInfo.holeEinzelFeld("select rez_datum from " + db + " where rez_nr='" + rez_nr + "' LIMIT 1")));
            hmRezgeb.put("<rgbetrag>", rezgeb.replace(".", ","));
            hmRezgeb.put("<rgpauschale>", pauschale.replace(".", ","));
            hmRezgeb.put("<rggesamt>", gesamt.replace(".", ","));
            hmRezgeb.put("<rganrede>", adressParams[0]);
            hmRezgeb.put("<rgname>", adressParams[1]);
            hmRezgeb.put("<rgstrasse>", adressParams[2]);
            hmRezgeb.put("<rgort>", adressParams[3]);
            hmRezgeb.put("<rgbanrede>", adressParams[4]);
            hmRezgeb.put("<rgorigdatum>", DatFunk.sDatInDeutsch(rdatum));
            hmRezgeb.put("<rgnr>", rgnr);

            hmRezgeb.put("<rgpatnname>", StringTools.EGross(
                    SqlInfo.holeEinzelFeld("select n_name from pat5 where pat_intern='" + pat_intern + "' LIMIT 1")));
            hmRezgeb.put("<rgpatvname>", StringTools.EGross(
                    SqlInfo.holeEinzelFeld("select v_name from pat5 where pat_intern='" + pat_intern + "' LIMIT 1")));
            hmRezgeb.put("<rgpatgeboren>", DatFunk.sDatInDeutsch(
                    SqlInfo.holeEinzelFeld("select geboren from pat5 where pat_intern='" + pat_intern + "' LIMIT 1")));

            // System.out.println(hmRezgeb);
            String url = Path.Instance.getProghome() + "vorlagen/" + ik.digitString()
                    + "/RezeptgebuehrRechnung.ott.Kopie.ott";
            try {
                officeStarten(url);
            } catch (OfficeApplicationException | NOAException e) {
                e.printStackTrace();
            } catch (TextException e) {
                e.printStackTrace();
            }
        } else {
            logger.error("das Rezept: " + rez_nr + " konnte nicht gefunden werden, daher kann keine Rechnungskopie erstellt werden. ");
        }
    }

    private void officeStarten(String url) throws OfficeApplicationException, NOAException, TextException {
        IDocumentService documentService;
        //// System.out.println("Starte Datei -> "+url);

        documentService = new OOService().getOfficeapplication()
                                         .getDocumentService();

        IDocumentDescriptor docdescript = new DocumentDescriptor();
        docdescript.setHidden(true);
        docdescript.setAsTemplate(true);
        IDocument document;

        document = documentService.loadDocument(url, docdescript);
        ITextDocument textDocument = (ITextDocument) document;
        // OOTools.druckerSetzen(textDocument,
        // SystemConfig.hmAbrechnung.get("hmgkvrechnungdrucker"));
        ITextFieldService textFieldService = textDocument.getTextFieldService();
        ITextField[] placeholders;

        placeholders = textFieldService.getPlaceholderFields();
        String placeholderDisplayText = "";

        for (int i = 0; i < placeholders.length; i++) {
            placeholderDisplayText = placeholders[i].getDisplayText()
                                                    .toLowerCase();
            Set<Entry<String, String>> entries = hmRezgeb.entrySet();
            Iterator<Entry<String, String>> it = entries.iterator();
            while (it.hasNext()) {
                Entry<String, String> entry = it.next();
                if (((String) entry.getKey()).toLowerCase()
                                             .equals(placeholderDisplayText)) {
                    placeholders[i].getTextRange()
                                   .setText((String) entry.getValue());

                    break;
                }
            }
        }
        textDocument.getFrame()
                    .getXFrame()
                    .getContainerWindow()
                    .setVisible(true);
    }

    @Override
    public void useRGR(boolean rgr) {
        iniOpRgAf.setIncRG(rgr);
        calcGesamtOffen();
    }

    @Override
    public void useAFR(boolean afr) {
        iniOpRgAf.setIncAR(afr);
        calcGesamtOffen();
    }

    @Override
    public void useVKR(boolean vkr) {
        iniOpRgAf.setIncVK(vkr);
        calcGesamtOffen();
    }
}
