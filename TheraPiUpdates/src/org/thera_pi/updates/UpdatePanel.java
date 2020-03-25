package org.thera_pi.updates;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.net.ftp.FTPFile;
import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.INIFile;
import environment.Path;
import sql.DatenquellenFactory;

public class UpdatePanel extends JXPanel {

    private static final Logger LOG = LoggerFactory.getLogger(UpdatePanel.class);
    private static final String PROGHOME = Path.Instance.getProghome();
    private static final String SECTION_THERA_PI_MANDANTEN = "TheraPiMandanten";

    private UpdateTableModel tabmod = null;
    private JXTable tab = null;

    JProgressBar pbar = null;

    private JTextArea ta = null;
    private UpdateDlg abrdlg;

    private static List<Vector<String>> updatefiles = new ArrayList<>();
    private static List<String[]> mandvec = new ArrayList<>();

    private FTPTools ftpt = null;

    private Image imgkeinupdate = new ImageIcon(PROGHOME + "icons/clean.png").getImage()
                                                                             .getScaledInstance(16, 16,
                                                                                     Image.SCALE_SMOOTH);
    private Image imgupdate = new ImageIcon(PROGHOME + "icons/application-exit.png").getImage()
                                                                                    .getScaledInstance(16, 16,
                                                                                            Image.SCALE_SMOOTH);
    private ImageIcon icokeinupdate;
    private ImageIcon icoupdate;
    FTPFile[] ffile = null;

    private JFrame jFrame;

    private int xaktrow;

    UpdatePanel(JFrame jFrame) {
        super();
        this.jFrame = jFrame;
        setLayout(new BorderLayout());
        add(getHeader(), BorderLayout.NORTH);
        add(getContent(), BorderLayout.CENTER);
    }

    void starteFTP() {
        doUpdateCheck();
    }

    private void doUpdateCheck() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    icokeinupdate = new ImageIcon(imgkeinupdate);
                    icoupdate = new ImageIcon(imgupdate);
                    doHoleUpdateConf();
                    updateCheck(PROGHOME + "update.files");
                    doFtpTest();
                } catch (Exception ex) {
                    LOG.error("Exception: " + ex.getMessage(), ex);
                }
                return null;
            }
        }.execute();
    }

    private JXHeader getHeader() {
        JXHeader head = new JXHeader();
        String titel = "<html><font size='5'><font color='e77817'>Thera-Pi</font>&nbsp;&nbsp; Update-Explorer (Vers. 2016-01)</font></html>";
        head.setTitle(titel);
        String description = "<html>Ein rotes <img src='file:///" + PROGHOME
                + "icons/application-exit.png' width='16' height='16' align=\"bottom\">"
                + "signalisiert, daß die angezeigte Datei <b>neuer</b> ist als die Datei die sich auf Ihrem Rechner befindet.<br>"
                + "Wenn Sie in der Tabelle einen Doppelklick auf einer dieser Dateien ausführen, kopieren Sie diese Datei in Ihre Thera-Pi-Installation."
                + "<b><font color='aa0000'><br>Achtung:</font></b><br>Wenn INI-Dateien zum Update angeboten werden überschreiben Sie evtl. bestehende individuelle INI-Dateien. Bitte "
                + "machen Sie in diesem Fall vor dem Update eine <b>Sicherungskopie Ihres 'INI-Verzeichnisses'</b></html>";
        head.setDescription(description);
        head.setIcon(new ImageIcon(PROGHOME + "icons/TPorg.png"));
        return head;
    }

    private JXPanel getContent() {
        JXPanel jpan = new JXPanel();
        String xwerte = "5dlu,p:g,5dlu";
        // 1 2 3 4 5 6 7 8 9 10
        String ywerte = "5dlu,0dlu,0dlu,100dlu,5dlu,p,2dlu,0dlu:g,5dlu,p,5dlu";
        FormLayout lay = new FormLayout(xwerte, ywerte);
        CellConstraints cc = new CellConstraints();
        jpan.setLayout(lay);
        tabmod = new UpdateTableModel();
        tabmod.setColumnIdentifiers(new String[] { "Update-Datei", "Dateidatum/Uhrzeit", "Größe in Bytes", "aktuell" });

        tab = new JXTable(tabmod);
        tab.setSortable(false);
        tab.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    if (!TheraPiUpdates.updateallowed) {
                        JOptionPane.showMessageDialog(null,
                                "Keine gültigen Zugangsdaten eingegeben!\nUpdates können nicht heruntergeladen werden!");
                        return;
                    }
                    testeObUpdate(tab.getSelectedRow());
                }
            }
        });
        UpdateListSelectionHandler updateListener = new UpdateListSelectionHandler();
        tab.getSelectionModel()
           .addListSelectionListener(updateListener);
        JScrollPane jscr = JCompTools.getTransparentScrollPane(tab);
        jscr.validate();
        jpan.add(jscr, cc.xy(2, 4));
        pbar = new JProgressBar();
        pbar.setStringPainted(false);
        jpan.add(pbar, cc.xy(2, 6));

        ta = new JTextArea();
        ta.setFont(new Font("Courier", Font.PLAIN, 12));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        ta.setBackground(Color.WHITE);
        ta.setForeground(Color.BLUE);
        jscr = JCompTools.getTransparentScrollPane(ta);
        jscr.validate();
        jpan.add(jscr, cc.xy(2, 8, CellConstraints.FILL, CellConstraints.FILL));

        JButton but = new JButton("Update-Explorer beenden");
        but.addActionListener(arg0 -> {
            if (TheraPiUpdates.starteTheraPi) {
                int anfrage = JOptionPane.showConfirmDialog(null, "Wollen Sie Thera-Pi 1.0 jetzt starten?",
                        "Thera-Pi starten?", JOptionPane.YES_NO_OPTION);
                if (anfrage == JOptionPane.YES_OPTION) {
                    try {
                        Runtime.getRuntime()
                               .exec("java -jar " + PROGHOME + "TheraPi.jar");
                    } catch (IOException ex) {
                        LOG.error("Exception: ", ex);
                    }
                }
            }
            System.exit(0);
        });
        jpan.add(but, cc.xy(2, 10, CellConstraints.RIGHT, CellConstraints.BOTTOM));
        jpan.validate();

        return jpan;
    }

    private void testeObUpdate(int row) {
        String upddatei = tab.getValueAt(row, 0)
                             .toString();
        long gross = Long.parseLong(tab.getValueAt(row, 2)
                                       .toString());
        if (tab.getValueAt(row, 3)
               .equals(this.icokeinupdate)) {
            JOptionPane.showMessageDialog(null,
                    "Die Datei --> " + upddatei + " <-- ist bereits auf dem neuesten Stand. Update nicht erforderlich");
            return;
        }
        for (int i = 0; i < updatefiles.size(); i++) {
            if (updatefiles.get(i)
                           .get(0)
                           .equals(upddatei)) {

                if (upddatei.equals("TabellenUpdate.sql")) {
                    int anfrage = JOptionPane.showConfirmDialog(null,
                            "Soll der Tabellen-Update jetzt durchgeführt werden?", "Achtung wichtige Benutzeranfrage",
                            JOptionPane.YES_NO_OPTION);
                    if (anfrage == JOptionPane.YES_OPTION) {
                        doTabellenUpdate();
                        tabmod.setValueAt(icokeinupdate, row, 3);
                    }
                    break;
                }

                if (upddatei.equals("ProgrammAusfuehren.sql")) {
                    int anfrage = JOptionPane.showConfirmDialog(null,
                            "Soll das im Change-Log aufgeführte Programm jetzt gestartet werden?",
                            "Achtung wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
                    if (anfrage == JOptionPane.YES_OPTION) {
                        doProgExecute();
                        tabmod.setValueAt(icokeinupdate, row, 3);
                    }
                    break;
                }

                String cmd = "<html>Wollen Sie die Update-Datei <b>" + updatefiles.get(i)
                                                                                  .get(0)
                        + "</b> nach<br>" + "<b>" + updatefiles.get(i)
                                                               .get(1)
                        + "</b> kopieren</html>";
                int anfrage = JOptionPane.showConfirmDialog(null, cmd, "Achtung wichtige Benutzeranfrage",
                        JOptionPane.YES_NO_OPTION);
                if (anfrage == JOptionPane.YES_OPTION) {
                    pbar.setStringPainted(true);
                    pbar.getParent()
                        .validate();
                    final int ix = i;
                    final String xupdate = upddatei;
                    final long xgross = gross;
                    final int xrow = row;

                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() {

                            try {
                                pbar.setValue(0);
                                String nurvz = updatefiles.get(ix)
                                                          .get(1)
                                                          .substring(0, updatefiles.get(ix)
                                                                                   .get(1)
                                                                                   .lastIndexOf("/")
                                                                  + 1);
                                LOG.debug(updatefiles.get(ix)
                                                     .get(1));
                                LOG.debug(updatefiles.get(ix)
                                                     .get(0));
                                LOG.debug(nurvz);
                                try {
                                    if (updatefiles.get(ix)
                                                   .get(1)
                                                   .endsWith(".ini")) {
                                        File f = new File(updatefiles.get(ix)
                                                                     .get(1));
                                        f.renameTo(new File(updatefiles.get(ix)
                                                                       .get(1)
                                                                       .replace(".ini", ".bak")));
                                    }
                                } catch (Exception ex) {
                                    LOG.error("Fehler beim Umbenennen der Datei " + updatefiles.get(ix)
                                                                                               .get(1),
                                            ex);
                                    JOptionPane.showMessageDialog(null,
                                            "Fehler beim Umbenennen der Datei " + updatefiles.get(ix)
                                                                                             .get(1));
                                }
                                LOG.debug("hole Datei -> {} Schleifendurchlauf -> {}", updatefiles.get(ix)
                                                                                                  .get(1),
                                        ix);
                                xaktrow = xrow;
                                try {
                                    ftpt = new FTPTools();
                                    ftpt.holeDatei(xupdate, nurvz, true, getInstance(), xgross);
                                } catch (Exception ex) {
                                    String message = "Bezug der Datei " + xupdate
                                            + " fehlgeschlagen!\nBitte starten Sie einen neuen Versuch";
                                    LOG.error(message, ex);
                                    JOptionPane.showMessageDialog(null, message);
                                    ftpt = null;
                                    pbar.setValue(0);
                                    return null;
                                }
                                ftpt = null;
                                SwingUtilities.invokeLater(() -> {

                                    try {
                                        if (TheraPiUpdates.userdaten != null) {
                                            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
                                                    "yyyy-MM-dd");
                                            LocalDateTime currentTime = LocalDateTime.now();
                                            String stmt = "insert into updated set ik='"
                                                    + TheraPiUpdates.userdaten.get(0) + "', datei='" + xupdate
                                                    + "', userid='" + TheraPiUpdates.userdaten.get(6) + "', datum='"
                                                    + dateTimeFormatter.format(currentTime) + "', " + "mac='"
                                                    + TheraPiUpdates.strMACAdr + "'";
                                            try {
                                                SqlInfo.sqlAusfuehren(TheraPiUpdates.conn, stmt);
                                            } catch (SQLException ex) {
                                                LOG.error("Exception: ", ex);
                                            }
                                        }
                                    } catch (Exception ex) {
                                        LOG.error(
                                                "Bezug der Datei " + xupdate
                                                        + " fehlgeschlagen!\nBitte starten Sie einen neuen Versuch",
                                                ex);
                                    }
                                });
                            } catch (Exception ex) {
                                LOG.error("Bezug der Datei " + xupdate
                                        + " fehlgeschlagen!\nBitte starten Sie einen neuen Versuch", ex);
                            }
                            return null;
                        }
                    }.execute();
                }
            }
        }
    }

    void setDoneIcon() {
        tabmod.setValueAt(icokeinupdate, xaktrow, 3);
    }

    private UpdatePanel getInstance() {
        return this;
    }

    private void doHoleUpdateConf() {
        try {
            ftpt = new FTPTools();
            boolean geklappt = ftpt.holeDatei("update.files", PROGHOME, false, getInstance(), -1);
            if (!geklappt) {
                JOptionPane.showMessageDialog(null, "Fehler beim Bezug der Steuerdatei update.files");
            }
            ftpt = null;
        } catch (Exception ex) {
            LOG.error("Exception: ", ex);
        }
    }

    private void doFtpTest() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    jFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    ftpt = new FTPTools();

                    ffile = ftpt.holeDatNamen();
                    Vector<Object> vec = new Vector<>();
                    for (FTPFile ftpFile : ffile) {
                        if ((!ftpFile.getName()
                                     .trim()
                                     .equals("."))
                                && (!ftpFile.getName()
                                            .trim()
                                            .equals(".."))
                                && (!ftpFile.getName()
                                            .startsWith("update."))) {
                            SimpleDateFormat datumsFormat = new SimpleDateFormat("dd.MM.yyyy  HH:mm:ss"); // Konv.
                            vec.clear();
                            vec.add(ftpFile.getName());
                            vec.add(datumsFormat.format(ftpFile.getTimestamp()
                                                               .getTime()
                                                               .getTime()));
                            vec.add(Long.toString(ftpFile.getSize()));
                            if (mussUpdaten(ftpFile.getName()
                                                   .trim(),
                                    ftpFile.getTimestamp()
                                           .getTime()
                                           .getTime())) {
                                vec.add(icoupdate);
                                tabmod.addRow((Vector<?>) vec.clone());
                            }
                        }
                    }
                    if (tabmod.getRowCount() > 0) {
                        tab.setRowSelectionInterval(0, 0);
                    } else {
                        JOptionPane.showMessageDialog(null, "Ihre Thera-Pi-Installation ist auf dem aktuellen Stand.");
                    }
                    LOG.debug("\n*********************************");
                    LOG.debug("   Insgesamt getestete Files: {}", ffile.length);
                    LOG.debug("Davon muessen updated werden: {}", tabmod.getRowCount());
                    LOG.debug("*********************************\n");
                    ftpt = null;
                    jFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                } catch (Exception ex) {
                    LOG.error("Exception: ", ex);
                    ftpt = null;
                }
                return null;
            }

        }.execute();

    }

    private boolean mussUpdaten(String datei, Long datum) {
        try {
            for (Vector<String> updatefile : updatefiles) {
                if (updatefile.get(0)
                              .equals(datei)) {
                    File f = new File(updatefile.get(1));
                    if (!f.exists()) {
                        testeDirectory(f);
                        return true;
                    }
                    LOG.debug("\nDatei: -----> {}\n\tDateidatum lokal: {}}\n\tDateidatum Updateserver: {}", f.getName(),
                            usingDateFormatterWithTimeZone(f.lastModified()), usingDateFormatterWithTimeZone(datum));
                    if (f.lastModified() < datum) {
                        LOG.debug("lokale Datei ist aelter -> muss updated werden!!!!!\n****************");
                        return true;
                    } else {
                        LOG.debug("lokale Datei ist juenger -> darf nicht updated werden\n****************");
                    }
                }
            }
        } catch (Exception ex) {
            LOG.error("Exception: ", ex);
        }
        return false;
    }

    private void testeDirectory(File f) {
        String abspfad = f.getAbsolutePath()
                          .replace("\\", "/");
        String dir = abspfad.substring(0, abspfad.lastIndexOf("/"));
        File d = new File(dir);
        if (!d.exists()) {
            LOG.debug("Erstelle Verzeichnis {}", d.getAbsolutePath());
            try {
                d.mkdir();
            } catch (Exception ex) {
                String message = "Verzeichnis " + d.getAbsolutePath() + " konnte nicht erstellt werden\n"
                        + "Bitte erstellen die das Verzeichnis von Hand und starten danach den Update-Explorer erneut";
                LOG.error(message, ex);
                JOptionPane.showMessageDialog(null, message);
            }
        }

    }

    private String usingDateFormatterWithTimeZone(long input) {
        ZonedDateTime gmt = Instant.ofEpochMilli(input)
                                   .atZone(TimeZone.getTimeZone("GMT")
                                                   .toZoneId());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss z");
        return dateTimeFormatter.format(gmt);
    }

    // ************************************************************************
    private static void updateCheck(String xupdatefile) {
        INIFile inif = new INIFile(PROGHOME + "ini/mandanten.ini");
        int anzahlMandanten = inif.getIntegerProperty(SECTION_THERA_PI_MANDANTEN, "AnzahlMandanten");
        for (int i = 0; i < anzahlMandanten; i++) {
            String[] mand = { null, null };
            mand[0] = inif.getStringProperty(SECTION_THERA_PI_MANDANTEN, "MAND-IK" + (i + 1));
            mand[1] = inif.getStringProperty(SECTION_THERA_PI_MANDANTEN, "MAND-NAME" + (i + 1));
            mandvec.add(mand);
        }

        try (FileReader reader = new FileReader(xupdatefile); BufferedReader in = new BufferedReader(reader)) {
            Vector<String> dummy = new Vector<>();
            String[] sourceAndTarget;
            Vector<Object> targetvec = new Vector<>();
            String zeile;
            while ((zeile = in.readLine()) != null) {
                if (!zeile.startsWith("#") && zeile.length() > 5) {
                    sourceAndTarget = zeile.split("@");
                    if (sourceAndTarget.length == 2) {
                        String updatedir = "";
                        if (sourceAndTarget[1].contains("%proghome%")) {
                            dummy.clear();
                            dummy.add(updatedir + sourceAndTarget[0].trim());
                            dummy.add(sourceAndTarget[1].trim()
                                                        .replace("%proghome%", PROGHOME)
                                                        .replace("//", "/"));
                            if (!targetvec.contains(dummy.get(1))) {
                                targetvec.add(dummy.get(1));
                                updatefiles.add(new Vector<>(dummy));
                            }
                        } else if (sourceAndTarget[1].contains("%userdir%")) {
                            String home = sourceAndTarget[1].trim()
                                                            .replace("%userdir%", PROGHOME)
                                                            .replace("//", "/");

                            for (String[] strings : mandvec) {
                                dummy.clear();
                                dummy.add(updatedir + sourceAndTarget[0].trim());
                                dummy.add(home.replace("%mandantik%", strings[0]));
                                if (!targetvec.contains(dummy.get(1))) {
                                    updatefiles.add(new Vector<>(dummy));
                                }
                                targetvec.add(home.replace("%mandantik%", strings[0]));
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            String message = "Fehler beim Bezug der Update-Informationen.\nBitte informieren Sie den Administrator umgehend";
            LOG.error(message, ex);
            JOptionPane.showMessageDialog(null, message);
        }
    }

    /*******************************************************/
    private void doProgExecute() {
        ftpt = new FTPTools();
        long xgross = Long.parseLong(tab.getValueAt(tab.getSelectedRow(), 2)
                                        .toString());
        ftpt.holeDatei("ProgrammAusfuehren.sql", PROGHOME, true, getInstance(), xgross);
        ftpt = null;
        pbar.setValue(0);
        File file = new File(PROGHOME + "ProgrammAusfuehren.sql");

        List<String> vecstmt = new ArrayList<>();
        try (FileReader freader = new FileReader(file); LineNumberReader lnreader = new LineNumberReader(freader)) {
            String line;
            while ((line = lnreader.readLine()) != null) {
                if (!line.trim()
                         .equals("")) {
                    vecstmt.add(line);
                }
                LOG.debug("Statement = {}", line);
            }
        } catch (IOException ex) {
            LOG.error("Exception: ", ex);
        }
        if (!vecstmt.isEmpty()) {
            String cmd = vecstmt.get(0)
                                .replace("@proghome/", PROGHOME);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Runtime.getRuntime()
                           .exec(cmd);
                    return null;
                }
            }.execute();
        } else {
            JOptionPane.showMessageDialog(null, "Keine Programm zur Ausführung gefunden");
        }

    }

    public static String Escaped(String string) {
        String escaped = string.replaceAll("'", "\\\\'");
        escaped = escaped.replaceAll("\"", "\\\\\"");
        return escaped;
    }

    private void doTabellenUpdate() {
        String ik;
        ftpt = new FTPTools();
        long xgross = Long.parseLong(tab.getValueAt(tab.getSelectedRow(), 2)
                                        .toString());
        ftpt.holeDatei("TabellenUpdate.sql", PROGHOME, true, getInstance(), xgross);
        ftpt = null;
        pbar.setValue(0);
        File file = new File(PROGHOME + "TabellenUpdate.sql");
        Vector<String> vecstmt = new Vector<>();
        try (FileReader freader = new FileReader(file); LineNumberReader lnreader = new LineNumberReader(freader)) {

            String line;
            while ((line = lnreader.readLine()) != null) {
                if (!line.trim()
                         .equals("")) {
                    vecstmt.add(line);
                }
                LOG.debug("Statement = {}", line);
            }
        } catch (IOException ex) {
            LOG.error("Exception: ", ex);
        }
        if (!vecstmt.isEmpty()) {
            SwingUtilities.invokeLater(() -> jFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR)));

            for (String[] strings : mandvec) {
                ik = strings[0];
                int frage = JOptionPane.showConfirmDialog(null,
                        "Wollen Sie die Tabellen der Datenbank für IK ->" + ik + " jetzt anpassen",
                        "Achtung wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
                if (frage == JOptionPane.YES_OPTION) {
                    try {

                        Connection conn = new DatenquellenFactory(ik).createConnection();
                        for (String s : vecstmt) {
                            try {
                                LOG.debug("Execute = {}", s);
                                SqlInfo.sqlAusfuehren(conn, s);
                            } catch (Exception e) {
                                LOG.error("Exception: " + e.getMessage(), e);
                            }
                        }

                        conn.close();
                    } catch (Exception ex) {
                        String message = "Fehler beim öffnen der Datenbank\nBetroffene IK: " + ik + "\nFehlertext: "
                                + (ex.getMessage() == null ? "nicht verfügbar" : ex.getMessage());
                        LOG.error(message, ex);
                        JOptionPane.showMessageDialog(null, message);
                    }
                }
            }
            SwingUtilities.invokeLater(() -> jFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)));

        } else {
            JOptionPane.showMessageDialog(null, "Keine Statements für Tabellen-Update gefunden");
        }
    }

    static class UpdateTableModel extends DefaultTableModel {

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 3) {
                return ImageIcon.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {

            return false;
        }

    }

    private boolean testeObLogVorhanden(String datei) {
        for (FTPFile ftpFile : ffile) {
            if (ftpFile.getName()
                       .equals(datei + ".log")) {
                return true;
            }
        }
        return false;
    }

    private String holeLogText(String logDatei) {
        FTPTools ftp = new FTPTools();
        return ftp.holeLogDateiSilent(logDatei);
    }

    class UpdateListSelectionHandler implements ListSelectionListener {

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
                        final int ix = i;

                        new SwingWorker<Void, Void>() {

                            @Override
                            protected Void doInBackground() {
                                try {
                                    int modi = tab.convertRowIndexToModel(ix);
                                    pbar.setValue(0);
                                    if (testeObLogVorhanden(tabmod.getValueAt(modi, 0)
                                                                  .toString())) {
                                        jFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                                        final int imodi = modi;
                                        new SwingWorker<Void, Void>() {

                                            @Override
                                            protected Void doInBackground() {
                                                abrdlg = new UpdateDlg();
                                                abrdlg.setzeLabel("<html>Beziehe Log-Datei für "
                                                        + tabmod.getValueAt(imodi, 0)
                                                                .toString()
                                                        + "<br><br>Bitte warten....<br></html>");
                                                abrdlg.setModal(true);
                                                abrdlg.setAlwaysOnTop(true);

                                                abrdlg.pack();
                                                abrdlg.setLocationRelativeTo(null);
                                                abrdlg.setVisible(true);
                                                return null;
                                            }

                                        }.execute();

                                        try {
                                            ta.setForeground(Color.BLUE);
                                            ta.setText(holeLogText(tabmod.getValueAt(modi, 0)
                                                                         .toString()
                                                    + ".log"));
                                            if (ta.getText()
                                                  .equals("Fehler beim Bezug der Log-Datei, ftpClient == nicht connected\nBitte starten Sie einen neuen Versuch")) {
                                                tab.clearSelection();
                                                tab.setRowSelectionInterval(modi, modi);
                                            }
                                        } catch (Exception ex) {
                                            LOG.error("Fehler beim Bezug der Log-Datei", ex);
                                            ta.setText("Fehler beim Bezug der Log-Datei");
                                        }
                                        jFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                                    } else {
                                        ta.setForeground(Color.RED);
                                        ta.setText("Für diese Update-Datei ist kein ChangeLog verfügbar");

                                    }
                                } catch (Exception ex) {
                                    LOG.error("Exception", ex);
                                }
                                if (abrdlg != null) {
                                    abrdlg.setVisible(false);
                                    abrdlg = null;
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
}
