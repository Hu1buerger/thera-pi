package BuildIniTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.mysql.jdbc.PreparedStatement;

import CommonTools.ButtonTools;
import CommonTools.INIFile;
import CommonTools.JCompTools;
import CommonTools.JRtaCheckBox;
import CommonTools.SqlInfo;
import sql.DatenquellenFactory;

public class ProcessPanel extends JXPanel {
    private static Logger logger = LoggerFactory.getLogger(ProcessPanel.class);
    private static final long serialVersionUID = 2467154459698276827L;

    public JRtaCheckBox[] check = new JRtaCheckBox[10];
    public JButton[] buts = new JButton[3];
    public JTextArea area = null;

    public JXTable tab = null;
    private static final int JXTAB_ISSET = 0;
    private static final int JXTAB_ININAME = 1;

    public MyIniTableModel tabmod = null;
    public Vector<String> inivec = new Vector<String>();

    ActionListener al = null;

    /**
     * This constructor was created to have an empty, clean constructor for
     * (unit-)testing. Should you need it otherwise, replace this comment and remove
     * the restriction.
     *
     * @param testString *Should* be UnitTest
     */
    ProcessPanel(String testString) {
        if (!testString.contentEquals("UnitTest")) {
            logger.error("This ProcessPanel-constructor was intended for testing only");
        }
    }

    public ProcessPanel() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(500, 500));
        activateListener();
        add((Component) mainpanel(), "Center");
        add((Component) mainpanel());
        validate();
        logger.debug(BuildIniTable.thisClass.mandantIkvec.toString());
        logger.debug(BuildIniTable.thisClass.mandantNamevec.toString());
    }

    /**
     * This method creates the main GUI panel, creating checkboxes for Mandanten
     * found in mandanten.ini.
     *
     * @return a JXPanel
     */
    private JXPanel mainpanel() {
        JXPanel pan = new JXPanel();
        pan.setBackground(Color.WHITE);
        String x = "10dlu,125dlu,5dlu,p:g,10dlu";
        String y = "10dlu,p,20dlu,";
        for (int i = 0; i < BuildIniTable.thisClass.anzahlmandanten; i++)
            y = String.valueOf(y) + "p,2dlu,";

        y = String.valueOf(y) + "p,2dlu,40dlu,p,5dlu,fill:0:grow(1.00),5dlu";
        FormLayout lay = new FormLayout(x, y);
        CellConstraints cc = new CellConstraints();
        pan.setLayout((LayoutManager) lay);
        JLabel lab = new JLabel("<html><font size=+1><font color=#0000FF>Bitte kreuzen "
                + "Sie links<u> die Mandanten</u> an, für die Sie die DB-Tabelle <b>"
                + "'inidatei'</b> erzeugen wollen.&nbsp;&nbsp;In der Liste rechts sind "
                + "bereits INI-Dateien für die Aufnahme in die DB-Tabelle markiert. "
                + "Sie können zusätzliche INI-Dateien markieren (wird jedoch ausdrücklich "
                + "nicht empfohlen /st.)</font></font></html>");
        pan.add(lab, cc.xyw(2, 2, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        int lastY = 0;
        int j;
        for (j = 0; j < BuildIniTable.thisClass.anzahlmandanten; j++) {
            this.check[j] = new JRtaCheckBox(String.valueOf(BuildIniTable.thisClass.mandantIkvec.get(j)) + " - "
                    + (String) BuildIniTable.thisClass.mandantNamevec.get(j));

            lastY = 3 + j * 2 + 1;
            pan.add((Component) this.check[j], cc.xy(2, lastY));
        }
        this.check[j] = new JRtaCheckBox("Tabelle(n) vorher löschen");
        lastY = 3 + j * 2 + 1;
        pan.add((Component) this.check[j], cc.xy(2, lastY));
        this.buts[0] = ButtonTools.macheButton("Tabelle erzeugen", "erzeugen", this.al);
        pan.add(this.buts[0], cc.xy(2, lastY + 3));
        this.tabmod = new MyIniTableModel();
        this.tabmod.setColumnIdentifiers((Object[]) new String[] { "in Tabelle", "INI-Datei" });
        this.tab = new JXTable(this.tabmod);
        JScrollPane tabscr = JCompTools.getTransparentScrollPane((Component) this.tab);
        tabscr.validate();
        pan.add(tabscr, cc.xywh(4, 4, 1, lastY, CellConstraints.FILL, CellConstraints.FILL));
        this.area = new JTextArea();
        this.area.setFont(new Font("Courier", 0, 12));
        this.area.setLineWrap(true);
        this.area.setName("logbuch");
        this.area.setWrapStyleWord(true);
        this.area.setEditable(false);
        this.area.setBackground(Color.WHITE);
        this.area.setForeground(Color.BLACK);
        JScrollPane span = JCompTools.getTransparentScrollPane(this.area);
        span.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        span.validate();
        pan.add(span, cc.xyw(2, lastY + 5, 4, CellConstraints.FILL, CellConstraints.FILL));
        pan.validate();
        getIniList();
        return pan;
    }

    /**
     * Populate the ini-file-vector with ini-file, excluding some select files
     */
    // @VisibleForTesting
    void getIniList() {
        if (BuildIniTable.thisClass.anzahlmandanten <= 0)
            return;
        File dir = new File(String.valueOf(BuildIniTable.thisClass.pfadzurini) + "/"
                + (String) BuildIniTable.thisClass.mandantIkvec.get(0) + "/");
        File[] contents = dir.listFiles();
        this.inivec.clear();
        // FIXME: if dir is non-exist, we get NPE
        for (int i = 0; i < contents.length; i++) {
            if (contents[i].getName()
                           .endsWith(".ini")
                    && !contents[i].getName()
                                   .equals("rehajava.ini")
                    && !contents[i].getName()
                                   .equals("inicontrol.ini")
                    && !contents[i].getName()
                                   .equals("firmen.ini"))
                this.inivec.add(contents[i].getName());
        }
        Comparator<String> comparator = new Comparator<String>() {
            public int compare(String o1, String o2) {
                String s1 = o1;
                String s2 = o2;
                return s1.compareTo(s2);
            }
        };
        Collections.sort(this.inivec, comparator);
        Vector<Object> dummy = new Vector();
        boolean logwert = false;
        List<String> listini = Arrays.asList(BuildIniTable.thisClass.inis);
        for (int j = 0; j < this.inivec.size(); j++) {
            dummy.clear();
            if (listini.contains(this.inivec.get(j))) {
                logwert = true;
            } else {
                logwert = false;
            }
            dummy.add(Boolean.valueOf(logwert));
            dummy.add(this.inivec.get(j));
            this.tabmod.addRow((Vector) dummy.clone());
        }
    }

    /**
     * This method does the bulk of work in this class. - Check/create/overwrite
     * inicontrol.ini - Create DB connection - Check/drop/create/update inidatei
     * table - Call schreibeIniInTabelle
     *
     * Where appropriate those actions are performed for each mandant from
     * mandanten.ini
     *
     */
    private void startAction() {
        Connection dbConnection = null;
        setTextArea("Starte Logbuch!");
        INIFile dummyini = null;
        INIFile inicontrol = null;
        String kopf = "INIinDB";
        String sanzahl = "INIAnzahl";
        int anzahl = 0;
        boolean overwrite = true;
        boolean tableDrop = false;

        if (this.check[BuildIniTable.thisClass.anzahlmandanten].isSelected()) {
            tableDrop = true;
        }

        for (int i = 0; i < BuildIniTable.thisClass.anzahlmandanten; i++) {
            String mandIk = (String) BuildIniTable.thisClass.mandantIkvec.get(i);
            try {
                Thread.sleep(1000L);
                overwrite = true;
                anzahl = 0;
                if (this.check[i].isSelected()) {
                    File testfile = new File(
                            String.valueOf(BuildIniTable.thisClass.pfadzurini) + "/" + mandIk + "/inicontrol.ini");
                    if (testfile.exists()) {
                        int frage = JOptionPane.showConfirmDialog(null,
                                "Für diesen Mandanten " + "existiert bereits eine 'inicontrol.ini'\n"
                                        + "Wollen Sie diese Datei mit der aktuellen Auswahl überschreiben?",
                                "Achtung wichtige Benutzeranfrage", 0);
                        if (frage == 1)
                            overwrite = false;
                    }
                    if (overwrite) {
                        inicontrol = new INIFile(
                                String.valueOf(BuildIniTable.thisClass.pfadzurini) + mandIk + "/inicontrol.ini");
                        inicontrol.addSection(kopf, null);
                        inicontrol.setStringProperty(kopf, sanzahl, "0", null);
                    }
                    setTextArea("\n\nErmittle Datenbankparameter für Mandant: " + mandIk);

                    dbConnection = new DatenquellenFactory(mandIk).createConnection();
                    if (dbConnection != null) {
                        setTextArea("Datenbankkontakt hergestellt");
                        Thread.sleep(500L);
                        setTextArea("Überprüfe ob Tabelle inidatei bereits existiert");
                        Thread.sleep(500L);
                        Vector<Vector<String>> testvec = SqlInfo.holeFelder("show table status " + "like 'inidatei'");
                        if (testvec.size() <= 0) {
                            setTextArea("Tabelle inidatei existiert nicht");
                            Thread.sleep(500L);
                            setTextArea("Erzeuge Tabelle");
                            Thread.sleep(500L);
                            SqlInfo.sqlAusfuehren(createIniTableStmt());
                            Thread.sleep(500L);
                        } else {
                            setTextArea("Tabelle inidatei existiert bereits");
                            Thread.sleep(500L);
                            if (tableDrop) {
                                setTextArea("Tabelle wird gelöscht");
                                if (SqlInfo.sqlAusfuehren("DROP TABLE inidatei")) {
                                    setTextArea("Tabelle erfolgreich gelöscht");
                                    Thread.sleep(500L);
                                    SqlInfo.sqlAusfuehren(createIniTableStmt());
                                    setTextArea("Tabelle neu angelegt");
                                    Thread.sleep(500L);
                                } else {
                                    setTextArea("Fehler beim löschen der Tabelle - "
                                            + "bitte von Hand ausführen und Programm neu starten");
                                    // FIXME: Needs changing in case this is integrated into Thera-pi
                                    System.exit(1);
                                }
                            }
                        }
                        int fehler = 0;
                        for (int i2 = 0; i2 < this.tab.getRowCount(); i2++) {
                            String iniName = this.tab.getValueAt(i2, ProcessPanel.JXTAB_ININAME)
                                                     .toString();
                            try {
                                if (this.tab.getValueAt(i2, ProcessPanel.JXTAB_ISSET) == Boolean.TRUE) {

                                    setTextArea("Schreibe INI in Tabelle -> " + iniName);
                                    Thread.sleep(500L);
                                    dummyini = new INIFile(String.valueOf(BuildIniTable.thisClass.pfadzurini) + "/"
                                            + (String) BuildIniTable.thisClass.mandantIkvec.get(i) + "/" + iniName);
                                    schreibeIniInTabelle(iniName, dummyini.saveToStringBuffer()
                                                                          .toString()
                                                                          .getBytes());
                                    setTextArea("Datensatz für " + iniName + " erfolgreich erzeugt");
                                    anzahl++;
                                    if (overwrite)
                                        inicontrol.setStringProperty(kopf, "DBIni" + Integer.toString(anzahl), iniName,
                                                null);
                                    Thread.sleep(500L);
                                }
                            } catch (Exception ex) {
                                fehler++;
                                setTextArea("Fehler bei der Erstellung des Datensatzes ----> " + iniName);
                            }
                        }
                        if (overwrite) {
                            inicontrol.setStringProperty(kopf, sanzahl, Integer.toString(anzahl), null);
                            inicontrol.save();
                            setTextArea("Erstelle inicontrol.ini\n");
                        }
                        setTextArea("\nUmsetzung der Inidateien in die Tabelle --> inidatei <-- " + "mit "
                                + Integer.toString(fehler) + " Fehlern beendet\n");
                    }
                }
            } catch (Exception ex) {
                setTextArea("Fehler!!!!!!");
                setTextArea(ex.getMessage());
            }
        }
    }

    /**
     * Method to display strings in a textbox, e.g. to let the user know about
     * progress or problems. It will append CR and update the 'cursor' position
     * within the textbox.
     *
     * @param text String The text to put into the textbox.
     */
    private void setTextArea(String text) {
        this.area.setText(String.valueOf(this.area.getText()) + text + "\n");
        this.area.setCaretPosition(this.area.getText()
                                            .length());
    }

    /**
     * Calls 'startAction' in a background thread
     */
    private void activateListener() {
        this.al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String cmd = e.getActionCommand();
                    if (cmd.equals("erzeugen")) {
                        new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                startAction();
                                return null;
                            }
                        }.execute();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        };
    }

    /**
     * Helper that returns the table-create string needed to create the 'inidatei'
     * table containing the appropriate fields and charset + engine settings.
     *
     * @return String Holds the statement needed to create the table in an SQL
     *         database.
     */
    public static String createIniTableStmt() {
        StringBuffer buf = new StringBuffer();
        buf.append("CREATE TABLE IF NOT EXISTS inidatei (");
        buf.append("dateiname varchar(250) DEFAULT NULL,");
        buf.append("inhalt text,");
        buf.append("id int(11) NOT NULL AUTO_INCREMENT,");
        buf.append("PRIMARY KEY (id)");
        buf.append(") ENGINE=InnoDB  COLLATE utf8_general_ci AUTO_INCREMENT=1");
        return buf.toString();
    }

    /**
     * Will write an ini-file into the table 'inidatei' of the database. If an entry
     * in the table already exists, it will be updated, otherwise inserted.
     *
     * @param inifile String of ini-file name - used to fill field 'dateiname'
     * @param buf     The content to be written into the field 'inhalt'
     * @return bool *Should* return false on failure or true upon success
     */
    public static boolean schreibeIniInTabelle(String inifile, byte[] buf) {
        boolean ret = false;
        try {
            Statement stmt = null;
            ResultSet rs = null;
            PreparedStatement ps = null;
            try {
                stmt = BuildIniTable.thisClass.conn.createStatement(1005, 1008);
                String select = null;
                if (SqlInfo.holeEinzelFeld("select dateiname from inidatei where dateiname='" + inifile + "' LIMIT 1")
                           .equals("")) {
                    select = "insert into inidatei set dateiname = ? , inhalt = ?";
                } else {
                    select = "update inidatei set dateiname = ? , inhalt = ? where dateiname = '" + inifile + "'";
                }
                ps = (PreparedStatement) BuildIniTable.thisClass.conn.prepareStatement(select);
                ps.setString(1, inifile);
                ps.setBytes(2, buf);
                ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (rs != null)
                    try {
                        rs.close();
                    } catch (SQLException sqlEx) {
                        rs = null;
                    }
                if (stmt != null)
                    try {
                        stmt.close();
                    } catch (SQLException sqlEx) {
                        stmt = null;
                    }
                if (ps != null)
                    ps.close();
            }
            ret = true;
        } catch (Exception exception) {
        }
        return ret;
    }

    /**
     * Helper class for working with Swing-Tables
     *
     */
    class MyIniTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1L;

        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0)
                return Boolean.class;
            return String.class;
        }

        public boolean isCellEditable(int row, int col) {
            if (col == 0)
                return true;
            return false;
        }
    }
}
