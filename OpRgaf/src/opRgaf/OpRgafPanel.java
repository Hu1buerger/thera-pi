package opRgaf;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
import CommonTools.RgAfVkSelect;
import CommonTools.RgAfVk_IfCallBack;
import CommonTools.SqlInfo;
import CommonTools.StringTools;
import opRgaf.RehaIO.SocketClient;
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
import io.RehaIOMessages;
import office.OOService;

public class OpRgafPanel extends JXPanel implements TableModelListener, RgAfVk_IfCallBack {


    private static final long serialVersionUID = -7883557713071422132L;

    JRtaTextField suchen = null;
    JRtaTextField offen = null;
    JRtaTextField[] tfs = { null, null, null, null };
    JButton[] buts = { null, null, null };

    enum btIdx {
        ausbuchen,
        suchen,
        dummy
    };

    int btAusbuchen = btIdx.ausbuchen.ordinal();
    int btSuchen = btIdx.suchen.ordinal();
    JRtaComboBox combo = null;
    JXPanel content = null;
    KeyListener kl = null;
    ActionListener al = null;

    MyOpRgafTableModel tabmod = null;
    JXTable tab = null;
    Component kopieButton;
    JRtaCheckBox bar = null;
    private boolean barWasSelected = false;

    JButton kopie;

    BigDecimal gesamtOffen = BigDecimal.valueOf(Double.parseDouble("0.00"));
    DecimalFormat dcf = new DecimalFormat("###0.00");

    int ccount = -2;

    private HashMap<String, String> hmRezgeb = new HashMap<String, String>();
    final String stmtString =
            "SELECT concat(t2.n_name, ', ',t2.v_name,', ',DATE_FORMAT(t2.geboren,'%d.%m.%Y')),t1.rnr,t1.rdatum,t1.rgesamt,"
                    + "t1.roffen,t1.rpbetrag,t1.rbezdatum,t1.rmahndat1,t1.rmahndat2,t3.kassen_nam1,t1.reznr,t1.id,t1.pat_id "
                    + "FROM (SELECT v_nummer as rnr,v_datum as rdatum,v_betrag as rgesamt,v_offen as roffen,'' as rpbetrag,"
                    + "v_bezahldatum as rbezdatum,mahndat1 as rmahndat1,mahndat2 as rmahndat2,'' as reznr,verklisteid as id,pat_id as pat_id "
                    + "FROM verkliste where v_nummer like 'VR-%' "
                    + "UNION SELECT rnr,rdatum,rgesamt,roffen,rpbetrag,rbezdatum,rmahndat1,rmahndat2,reznr,id as id,pat_intern as pat_id "
                    + "FROM rgaffaktura ) t1 LEFT JOIN pat5 AS t2 ON (t1.pat_id = t2.pat_intern) LEFT JOIN kass_adr AS t3 ON ( t2.kassenid = t3.id )";

    String[] spalten = { "Name,Vorname,Geburtstag", "Rechn.Nr.", "Rechn.Datum", "Gesamtbetrag", "Offen", "Bearb.Gebühr",
            "bezahlt am", "1.Mahnung", "2.Mahnung", "Krankenkasse", "RezeptNr.", "id" };
    String[] colnamen = { "nix", "rnr", "rdatum", "rgesamt", "roffen", "rpbetrag", "rbezdatum", "rmahndat1",
            "rmahndat2", "nix", "nix", "id" };
    OpRgafTab eltern = null;

    class IdxCol { // Indices fuer sprechende Spaltenzugriffe
        static final short Name = 0, RNr = 1, RDat = 2, GBetr = 3, Offen = 4, BGeb = 5, bez = 6, mahn1 = 7, mahn2 = 8,
                kk = 9, RezNr = 10, id = 11;
    }

    private OpShowGesamt sumPan;
    private RgAfVkSelect selPan;

    public OpRgafPanel(OpRgafTab xeltern) {
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

    private JPanel getContent() {
        //                  1     2    3     4     5  6   7     8       9   10    11   12    13   14    15   16    17    18 19
        String xwerte = "10dlu,50dlu,2dlu,90dlu,10dlu,p,2dlu,70dlu:g,40dlu,5dlu,50dlu,5dlu,50dlu,5dlu,50dlu,5dlu,50dlu,10dlu,p";
        //                  1  2    3      4      5  6    7    8  9  10   11
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

        int vorauswahl = OpRgaf.iniOpRgAf.getVorauswahl(args.length);
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

        buts[btSuchen] = ButtonTools.macheButton("suchen", "suchen", al);
        buts[btSuchen].setMnemonic(KeyEvent.VK_S);
        builder.add(buts[btSuchen], cc.xy(17, rowCnt));

//**********************
        while (!OpRgaf.DbOk) {

        }
        tabmod = new MyOpRgafTableModel();
        if (chkSalesTableStruct()) {
            return builder.getPanel();
        }

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
        kopieButton = builder.add(ButtonTools.macheButton("Rechnungskopie", "kopie", al), cc.xy(colCnt, rowCnt)); // 4,6
        colCnt = 11;
        builder.addLabel("Geldeingang:", cc.xy(colCnt, rowCnt, CellConstraints.RIGHT, CellConstraints.TOP)); // 12,6

        ++colCnt;
        tfs[0] = new JRtaTextField("F", true, "6.2", "");
        tfs[0].setHorizontalAlignment(SwingConstants.RIGHT);
        tfs[0].setText("0,00");
        tfs[0].setName("offen");
        tfs[0].addKeyListener(kl);
        builder.add(tfs[0], cc.xy(++colCnt, rowCnt)); // 14,6

        ++colCnt;
        bar = (JRtaCheckBox) builder.add(new JRtaCheckBox("bar in Kasse"), cc.xy(++colCnt, rowCnt));
        if (OpRgaf.iniOpRgAf.getWohinBuchen()
                            .equals("Kasse")) {
            bar.setSelected(true);
        }

        buts[btAusbuchen] = (JButton) builder.add(ButtonTools.macheButton("ausbuchen", "ausbuchen", al), cc.xy(17, 6));
        buts[btAusbuchen].setMnemonic(KeyEvent.VK_A);
//**********************

        rowCnt += 2;
        colCnt = 1;
        builder.add(new JSeparator(SwingConstants.HORIZONTAL), cc.xyw(colCnt, rowCnt++, 19));

        sumPan = new OpShowGesamt();
        builder.add(sumPan.getPanel(), cc.xyw(colCnt, rowCnt, 2, CellConstraints.LEFT, CellConstraints.TOP)); // 2,2

        calcGesamtOffen();

        return builder.getPanel();
    }

    private boolean chkSalesTableStruct() {
        HashMap<String, String> hmMapNumFields = new HashMap<String, String>();
        hmMapNumFields.put("v_offen", "verkliste");
        hmMapNumFields.put("v_betrag", "verkliste");
        hmMapNumFields.put("v_mwst7", "verkliste");
        hmMapNumFields.put("v_mwst19", "verkliste");
        hmMapNumFields.put("art_einzelpreis", "verkfaktura");
        hmMapNumFields.put("art_mwst", "verkfaktura");
        hmMapNumFields.put("anzahl", "verkfaktura");
        hmMapNumFields.put("preis", "verkartikel");
        hmMapNumFields.put("mwst", "verkartikel");
        hmMapNumFields.put("lagerstand", "verkartikel");
        hmMapNumFields.put("einkaufspreis", "verkartikel");
        HashMap types = new HashMap();
        Vector<Vector<String>> felder = SqlInfo.holeFelder("describe verkliste");
        String[] cols = new String[felder.size()];
        for (int i = 0; i < felder.size(); i++) {
            cols[i] = felder.get(i)
                            .get(0);
            types.put(cols[i], felder.get(i)
                                     .get(1));
        }
        felder = SqlInfo.holeFelder("describe verkartikel");
        cols = new String[felder.size()];
        for (int i = 0; i < felder.size(); i++) {
            cols[i] = felder.get(i)
                            .get(0);
            types.put(cols[i], felder.get(i)
                                     .get(1));
        }
        felder = SqlInfo.holeFelder("describe verkfaktura");
        cols = new String[felder.size()];
        for (int i = 0; i < felder.size(); i++) {
            cols[i] = felder.get(i)
                            .get(0);
            types.put(cols[i], felder.get(i)
                                     .get(1));
        }

        Set keys = hmMapNumFields.keySet();
        for (Iterator it = keys.iterator(); it.hasNext();) {
            String key = (String) it.next();
            System.out.println("key: " + key + "  ist vom Typ: " + types.get(key).toString());
            if (!types.get(key)
                    .toString()
                    .contains("decimal(10,2)")) {
                JOptionPane.showMessageDialog(null, "Struktur der Tabelle '" + hmMapNumFields.get(key) + "' ist nicht aktuell. \nBitte wenden Sie sich an den Support!");
                return true;
            }
        }

        return false;
    }

    /**
     * letzte Checkbox-Auswahl wiederherstellen
     */
    public void initSelection() {
        selPan.setRGR(OpRgaf.iniOpRgAf.getIncRG());
        selPan.setAFR(OpRgaf.iniOpRgAf.getIncAR());
        selPan.setVKR(OpRgaf.iniOpRgAf.getIncVK());
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

    private OpRgafPanel getInstance() {
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
                    return;
                }
                if (cmd.equals("kopie")) {
                    doKopie();
                    setzeFocus();
                    return;
                }
                if (cmd.equals("suchen")) {
                    sucheEinleiten();
                    return;
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
                        // System.out.println("in Ausfallrechnung");
                        String id = tab.getValueAt(tab.getSelectedRow(), 11)
                                       .toString();
                        String rez_nr = SqlInfo.holeEinzelFeld(
                                "select reznr from rgaffaktura where id='" + id + "' LIMIT 1");
                        String pat_intern = SqlInfo.holeEinzelFeld(
                                "select pat_intern from rgaffaktura where id='" + id + "' LIMIT 1");
                        String rdatum = SqlInfo.holeEinzelFeld(
                                "select rdatum from rgaffaktura where id='" + id + "' LIMIT 1");
                        AusfallRechnung ausfall = new AusfallRechnung(kopieButton.getLocationOnScreen(), pat_intern,
                                rez_nr, rnr, rdatum);
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
        tfs[0].setText(dcf.format(tabmod.getValueAt(tab.convertRowIndexToModel(i), IdxCol.Offen)));
    }

    private void sucheEinleiten() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    setzeFocus();
                    tabmod.removeTableModelListener(getInstance());
                    doSuchen();
                    schreibeAbfrage();
                    tabmod.addTableModelListener(getInstance());
                    suchen.setEnabled(true);
                    buts[btAusbuchen].setEnabled(true);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Fehler beim einlesen der Datensätze");
                    setzeFocus();
                    suchen.setEnabled(true);
                    buts[btAusbuchen].setEnabled(true);
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
        BigDecimal eingang = BigDecimal.valueOf(Double.parseDouble(tfs[0].getText()
                                                                         .replace(",", ".")));
        BigDecimal restbetrag = nochoffen.subtract(eingang);

        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Keine Rechnung zum Ausbuchen ausgewählt");
            return;
        }

        if (nochoffen.equals(BigDecimal.valueOf(Double.parseDouble("0.0")))) {
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
            if (ktext.length() > 35) { ktext = ktext.substring(0, 34);}
            cmd = "insert into kasse set einnahme='" + dcf.format(eingang)
                                                          .replace(",", ".")
                    + "', datum='" + DatFunk.sDatInSQL(DatFunk.sHeute()) + "', ktext='" + ktext + "'," + 
                    "rez_nr='" + rgaf_reznum + "'";
            // System.out.println(cmd);
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
                    + "', rbezdatum='" + DatFunk.sDatInSQL(DatFunk.sHeute()) + "' where id ='" + Integer.toString(id)
                    + "' LIMIT 1";
        }
        if (rgaf_rechnum.startsWith("VR-")) { // aus verkliste ausbuchen
            cmd = "update verkliste set v_offen='" + dcf.format(restbetrag)
                                                        .replace(",", ".")
                    + "', v_bezahldatum='" + DatFunk.sDatInSQL(DatFunk.sHeute()) + "' where verklisteID ='"
                    + Integer.toString(id) + "' LIMIT 1";
        }
        SqlInfo.sqlAusfuehren(cmd);
        schreibeAbfrage();
        tfs[0].setText("0,00");
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
        if (suchen.getText()
                  .trim()
                  .equals("")) {

            return;
        }
        int suchart = combo.getSelectedIndex();
        OpRgaf.iniOpRgAf.setVorauswahl(suchart); // Auswahl merken
        String suchVal = combo.getItemAt(combo.getSelectedIndex()).toString();
        // System.out.println("OpRgafPanel-doSuchen-suche: " +'"' + suchVal + '"' + "("
        // + suchart + ")"); // s. String[] args
        String cmd = "";
        String tmpStr = selPan.bills2search("rnr");
        String whereToSearch = " WHERE ";
        String searchStr = suchen.getText()
                                 .trim();
        String searchStrNumVal = searchStr.replace(",", ".");
        if (tmpStr.length() > 0) {
            whereToSearch = whereToSearch + " ( " + tmpStr + " ) AND ";
        }

        try {
//            switch(suchVal){        // <- funktioniert erst ab Java 1.7
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

        if (!cmd.equals("")) {
            buts[btAusbuchen].setEnabled(false);
            suchen.setEnabled(false);
            // System.out.println("suche nach: "+'"'+cmd+'"');
            try {
                starteSuche(cmd);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Korrekte Auflistung des Suchergebnisses fehlgeschlagen");
            }

            suchen.setEnabled(true);
            buts[btAusbuchen].setEnabled(true);
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

    class MyOpRgafTableModel extends DefaultTableModel {
        /**
        *
        */
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
        // tab.repaint();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = OpRgaf.thisClass.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {

            rs = stmt.executeQuery(sstmt);
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
                    if (rsMetaData.getColumnClassName(i)
                                  .toString()
                                  .equals("java.lang.String")) {
                        vec.add((rs.getString(i) == null ? "" : rs.getString(i)));
                    } else if (rsMetaData.getColumnClassName(i)
                                         .toString()
                                         .equals("java.math.BigDecimal")) {
                        vec.add(rs.getBigDecimal(i)
                                  .doubleValue());
                    } else if (rsMetaData.getColumnClassName(i)
                                         .toString()
                                         .equals("java.sql.Date")) {
                        vec.add(rs.getDate(i));
                    } else if (rsMetaData.getColumnClassName(i)
                                         .toString()
                                         .equals("java.lang.Integer")) {
                        vec.add(rs.getInt(i));
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
        int columns2adjust[] = { 0, 4, 7, 8, 10 }; // Name,Vorname,Geburtstag, Offen, 1.Mahnung, 2.Mahnung, RezeptNr.
        for (int col : columns2adjust) {
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
                        setzeBezahlBetrag(i);
                        String id = tab.getValueAt(i, 11)
                                       .toString();
                        String rnr = tab.getValueAt(i, 1)
                                        .toString();
                        String rez_nr = SqlInfo.holeEinzelFeld(
                                "select reznr from rgaffaktura where id='" + id + "' LIMIT 1");
                        String pat_intern = SqlInfo.holeEinzelFeld(
                                "select pat_intern from rgaffaktura where id='" + id + "' LIMIT 1");
                        new SocketClient().setzeRehaNachricht(OpRgaf.rehaReversePort,
                                "OpRgaf#" + RehaIOMessages.MUST_PATANDREZFIND + "#" + pat_intern + "#" + rez_nr);
                        // System.out.println("Satz "+i);

                        if (rnr.startsWith("VR-")) { // test ob VR -> bar ausbuchen enabled/disabled
                            if (!OpRgaf.iniOpRgAf.getVrCashAllowed()) {
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
                String colname = colnamen[col].toString();
                String value = "";
                String id = Integer.toString((Integer) tabmod.getValueAt(row, 11));
                if (tabmod.getColumnClass(col) == Boolean.class) {
                    value = (tabmod.getValueAt(row, col) == Boolean.FALSE ? "F" : "T");
                } else if (tabmod.getColumnClass(col) == Date.class) {
                    if (tabmod.getValueAt(row, col) == null) {
                        value = "1900-01-01";
                    } else {
                        String test = tabmod.getValueAt(row, col)
                                            .toString();
                        if (test.contains(".")) {
                            value = DatFunk.sDatInSQL(test);
                            if (value.equals("    -  -  ")) {
                                value = null;
                            }
                        } else {
                            if (test.equals("    -  -  ")) {
                                value = null;
                            } else {
                                value = test;
                            }

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
                    HashMap<String, String> hmMap2VerkListe = new HashMap<String, String>();
                    hmMap2VerkListe.put("rbezdatum", "v_bezahldatum");  // der Fluch der verbogenen Spaltennamen
                    hmMap2VerkListe.put("roffen", "v_offen");
                    hmMap2VerkListe.put("rgesamt", "v_betrag");
                    if (hmMap2VerkListe.containsKey(colname)) { // Ändern dieser Spalte ist erlaubt
                        String cmd = "update verkliste set " + hmMap2VerkListe.get(colname) + " ="
                                + (value != null ? "'" + value + "'" : "null") + " where verklisteID='" + id
                                + "' LIMIT 1";
                        System.out.println(cmd);
                        SqlInfo.sqlAusfuehren(cmd);
                    } else {
                        new SwingWorker<Void, Void>() { // andere 'rückgängig' machen (= Suche neu ausführen)
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
                    // System.out.println(cmd);
                    SqlInfo.sqlAusfuehren(cmd);
                    tfs[0].setText(
                            dcf.format(tabmod.getValueAt(tab.convertRowIndexToModel(row), IdxCol.Offen)));
                }

            } catch (Exception ex) {
                System.out.println(ex);
                JOptionPane.showMessageDialog(null, "Fehler in der Dateneingabe");
            }
            return;
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
        String rez_nr = SqlInfo.holeEinzelFeld("select reznr from rgaffaktura where id='" + id + "' LIMIT 1");
        String pat_intern = SqlInfo.holeEinzelFeld("select pat_intern from rgaffaktura where id='" + id + "' LIMIT 1");
        String rdatum = SqlInfo.holeEinzelFeld("select rdatum from rgaffaktura where id='" + id + "' LIMIT 1");
        String rezgeb = SqlInfo.holeEinzelFeld("select rgbetrag from rgaffaktura where id='" + id + "' LIMIT 1");
        String pauschale = SqlInfo.holeEinzelFeld("select rpbetrag from rgaffaktura where id='" + id + "' LIMIT 1");
        String gesamt = SqlInfo.holeEinzelFeld("select rgesamt from rgaffaktura where id='" + id + "' LIMIT 1");
        // System.out.println("Rezeptnummer = "+rez_nr);
        new InitHashMaps();

        String test = SqlInfo.holeEinzelFeld("select id from verordn where rez_nr = '" + rez_nr + "' LIMIT 1");
        Vector<String> vecaktrez = null;
        if (test.equals("")) {
            test = SqlInfo.holeEinzelFeld("select id from lza where rez_nr = '" + rez_nr + "' LIMIT 1");
            if (test.equals("")) {
            } else {
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
        String behandlungen = vecaktrez.get(0) + "*" + (!vecaktrez.get(1)
                                                                  .trim()
                                                                  .equals("") ? "" + vecaktrez.get(1) : "")
                + (!vecaktrez.get(2)
                             .trim()
                             .equals("") ? "," + vecaktrez.get(2) : "")
                + (!vecaktrez.get(3)
                             .trim()
                             .equals("") ? "," + vecaktrez.get(3) : "")
                + (!vecaktrez.get(4)
                             .trim()
                             .equals("") ? "," + vecaktrez.get(4) : "")
                + (!vecaktrez.get(5)
                             .trim()
                             .equals("") ? "," + vecaktrez.get(5) : "")
                + (!vecaktrez.get(6)
                             .trim()
                             .equals("") ? "," + vecaktrez.get(6) : "");

        String cmd = "select abwadress,id from pat5 where pat_intern='" + pat_intern + "' LIMIT 1";
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
        String url = OpRgaf.proghome + "vorlagen/" + OpRgaf.aktIK + "/RezeptgebuehrRechnung.ott.Kopie.ott";
        try {
            officeStarten(url);
        } catch (OfficeApplicationException e) {
            e.printStackTrace();
        } catch (NOAException e) {
            e.printStackTrace();
        } catch (TextException e) {
            e.printStackTrace();
        }

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

    private void officeStarten(String url) throws OfficeApplicationException, NOAException, TextException {
        IDocumentService documentService = null;
        //// System.out.println("Starte Datei -> "+url);
      

        documentService =   new OOService().getOfficeapplication().getDocumentService();

        IDocumentDescriptor docdescript = new DocumentDescriptor();
        docdescript.setHidden(true);
        docdescript.setAsTemplate(true);
        IDocument document = null;

        document = documentService.loadDocument(url, docdescript);
        ITextDocument textDocument = (ITextDocument) document;
        /**********************/
        // OOTools.druckerSetzen(textDocument,
        // SystemConfig.hmAbrechnung.get("hmgkvrechnungdrucker"));
        /**********************/
        ITextFieldService textFieldService = textDocument.getTextFieldService();
        ITextField[] placeholders = null;

        placeholders = textFieldService.getPlaceholderFields();
        String placeholderDisplayText = "";

        for (int i = 0; i < placeholders.length; i++) {
            placeholderDisplayText = placeholders[i].getDisplayText()
                                                    .toLowerCase();
            Set<?> entries = hmRezgeb.entrySet();
            Iterator<?> it = entries.iterator();
            while (it.hasNext()) {

                Map.Entry entry = (Map.Entry) it.next();
                if (((String) entry.getKey()).toLowerCase()
                                             .equals(placeholderDisplayText)) {
                    try {

                    } catch (RuntimeException ex) {
                        // System.out.println("Fehler bei "+placeholderDisplayText);
                    }
                    placeholders[i].getTextRange()
                                   .setText(((String) entry.getValue()));

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
        OpRgaf.iniOpRgAf.setIncRG(rgr);
        calcGesamtOffen();
    }

    @Override
    public void useAFR(boolean afr) {
        OpRgaf.iniOpRgAf.setIncAR(afr);
        calcGesamtOffen();
    }

    @Override
    public void useVKR(boolean vkr) {
        OpRgaf.iniOpRgAf.setIncVK(vkr);
        calcGesamtOffen();
    }

}
