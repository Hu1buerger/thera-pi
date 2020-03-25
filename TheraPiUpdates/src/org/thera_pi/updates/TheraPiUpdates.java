package org.thera_pi.updates;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.*;

import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.INIFile;
import environment.Path;

public class TheraPiUpdates extends WindowAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(TheraPiUpdates.class);
    private static final String PROGHOME = Path.Instance.getProghome();
    private static final String LIB_SIG_JAR = "Libraries/lib/ocf/sig.jar";
    private static boolean showpwdlg = true;
    private static boolean macNotMatch = true;
    private static boolean dbok = false;
    private JFrame jFrame = null;
    private UpdatePanel updatePanel;
    private UpdateTab updateTab;
    private String email;
    private String pw;
    static boolean updateallowed = false;
    static Image imgtporg;
    static List<String> userdaten;
    static Connection conn = null;
    static boolean starteTheraPi = false;
    static String strMACAdr = "";

    public static void main(String[] args) {
        if (args.length > 0) {
            starteTheraPi = true;
        }

        if (UpdateConfig.getInstance()
                        .isUseActiveMode()) {

            LOG.debug("FTP-Modus = ActiveMode");
        } else {
            LOG.debug("FTP-Modus = PassiveMode");
        }

        LOG.debug("program home: {}", PROGHOME);

        TheraPiUpdates application = new TheraPiUpdates();

        application.createJFrame();
        if (TheraPiUpdates.showpwdlg) {
            application.getPwDialog();
        } else {
            boolean test = application.testeZugang();
            if (test) {
                TheraPiUpdates.updateallowed = true;
                application.starteFTP();
            } else {
                TheraPiUpdates.updateallowed = false;
                JOptionPane.showMessageDialog(null,
                        "Sie haben zwar eine Zugangsdatei in Ihrem System integriert, die Zugangsdaten sind allderdings falsch");
                File ftest = new File(PROGHOME + LIB_SIG_JAR);
                if (ftest.exists()) {
                    ftest.delete();
                }
                if (starteTheraPi) {
                    try {
                        Runtime.getRuntime()
                               .exec("java -jar " + PROGHOME + "TheraPi.jar");
                    } catch (IOException ex) {
                        LOG.error("Exception :" + ex.getMessage(), ex);
                    }
                    System.exit(0);
                }
                application.jFrame.dispose();
                System.exit(0);
            }
        }

    }

    private void starteFTP() {
        if (UpdateConfig.getInstance()
                        .isDeveloperMode()) {
            updateTab.starteFTP();
        } else {
            updatePanel.starteFTP();
        }
    }

    private boolean testeZugang() {
        String cmd = "select id from regtpuser where email='" + this.email + "' and pw='" + this.pw + "' LIMIT 1";
        List<String> testvec = SqlInfo.holeFeld(conn, cmd);
        return !testvec.isEmpty();
    }

    private void createJFrame() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException
                | InstantiationException e) {
            LOG.error("Exception: " + e.getMessage(), e);
        }

        new DatenbankStarten().run();
        long warten = System.currentTimeMillis();
        while (!dbok && (System.currentTimeMillis() - warten < 10000)) {
            try {
                Thread.sleep(75);
            } catch (InterruptedException e) {
                LOG.error("Exception: ", e);
            }
        }
        if (!dbok) {
            JOptionPane.showMessageDialog(null,
                    "Kann die Update-Datenbank nicht starten, Update-Explorer wird beendet");
        }
        jFrame = new JFrame();
        jFrame.setUndecorated(true);
        jFrame.addWindowListener(this);
        Dimension ssize = Toolkit.getDefaultToolkit()
                                 .getScreenSize();
        jFrame.setTitle("Thera-Pi  Update-Explorer");
        jFrame.setSize(Math.max(ssize.width * 3 / 4, 800), Math.max(ssize.height / 2, 600));
        jFrame.setPreferredSize(new Dimension(Math.max(ssize.width * 3 / 4, 800), Math.max(ssize.height / 2, 600)));
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        try {
            TheraPiUpdates.strMACAdr = TheraPiUpdates.getMacAddress();
        } catch (Exception ex) {
            LOG.error("Exception: ", ex);
        }
        File fi = new File(PROGHOME + "ini/tpupdateneu.ini");
        if (fi.exists()) {
            LOG.debug("Datei existiert = {}", fi.getName());
        } else {
            LOG.debug("Datei existiert nicht");
        }
        // ************** Testen ob die sig.jar noch altes Format hat *************/
        File ftest = new File(PROGHOME + LIB_SIG_JAR);
        if (ftest.exists()) {
            try (FileReader fileReader = new FileReader(PROGHOME + LIB_SIG_JAR);
                    BufferedReader in = new BufferedReader(fileReader)) {
                String test = in.readLine()
                                .trim();
                if (!test.startsWith("[Updates]")) {
                    ftest.delete();
                }
            } catch (IOException e) {
                LOG.error("Exception: ", e);
            }

        }

        File f = new File(PROGHOME + LIB_SIG_JAR);
        if (!f.exists()) {
            LOG.debug("sig nicht vorhanden");
            TheraPiUpdates.showpwdlg = true;
        } else {
            try {
                INIFile inif = new INIFile(PROGHOME + LIB_SIG_JAR);

                String macAdr = inif.getStringProperty("Updates", "1");
                this.email = inif.getStringProperty("Updates", "2");
                this.pw = inif.getStringProperty("Updates", "3");
                LOG.debug("Client = {}", macAdr);

                if (!macAdr.equals(strMACAdr)) {
                    JOptionPane.showMessageDialog(null,
                            "Der Updatekanal wurde für diesen Rechner noch nicht freigeschaltet");
                    TheraPiUpdates.showpwdlg = true;
                    TheraPiUpdates.macNotMatch = true;
                } else {
                    TheraPiUpdates.showpwdlg = false;
                }
                TheraPiUpdates.imgtporg = new ImageIcon(PROGHOME + "icons/TPorgKlein.png").getImage()
                                                                                          .getScaledInstance(246, 35,
                                                                                                  Image.SCALE_SMOOTH);

            } catch (Exception e) {
                LOG.error("Exception: ", e);
                JOptionPane.showMessageDialog(null, "Es ist ein Fehler aufgetreten\nFehlertext:\n" + e.getMessage());
                jFrame.dispose();
                System.exit(0);
            }
        }

        if (UpdateConfig.getInstance()
                        .isDeveloperMode()) {
            updateTab = new UpdateTab(jFrame);
            jFrame.getContentPane().add(updateTab);
        } else {
            updatePanel = new UpdatePanel(jFrame);
            jFrame.getContentPane().add(updatePanel);
        }
        jFrame.pack();
        jFrame.setVisible(true);
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
        if (conn != null) {
            try {
                conn.close();
                LOG.debug("Verbindung geschlossen in Closed");
            } catch (SQLException e) {
                LOG.error("Exception: ", e);
            }
        }
        System.exit(0);
    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        if (conn != null) {
            try {
                conn.close();
                LOG.debug("Verbindung geschlossen in Closing");
            } catch (SQLException e) {
                LOG.error("Exception: ", e);
            }
        }
        System.exit(0);
    }

    static final class DatenbankStarten implements Runnable {
        @Override
        public void run() {
            try {
                Class.forName("de.root1.jpmdbc.Driver");
            } catch (final Exception e) {
                JOptionPane.showMessageDialog(null, "Fehler beim Laden des Datenbanktreibers für Preislisten-Server");
                return;
            }
            try {
                Properties connProperties = new Properties();
                connProperties.setProperty("user", "dbo486621783");
                connProperties.setProperty("password", "neuerupdateexplorer");
                connProperties.setProperty("host", "db486621783.db.1and1.com");
                connProperties.setProperty("port", "3306");
                connProperties.setProperty("compression", "false");
                connProperties.setProperty("NO_DRIVER_INFO", "1");
                TheraPiUpdates.conn = DriverManager.getConnection(
                        "jdbc:jpmdbc:http://www.thera-pi.org/jpmdbc.php?db486621783", connProperties);

                TheraPiUpdates.dbok = true;

                LOG.debug("Kontakt zur Update-Datenbank hergestellt");
            } catch (final SQLException ex) {
                LOG.error("Exception: ", ex);
                JOptionPane.showMessageDialog(null,
                        "Fehler: Datenbankkontakt zum Update-Server konnte nicht hergestellt werden.");
            }
        }
    }

    private void getPwDialog() {
        final JXPanel pan = new JXPanel();
        JXDialog dlg = new JXDialog(jFrame, pan);
        final JTextField field1 = new JTextField();
        final JPasswordField field2 = new JPasswordField();
        final JButton[] buts = { null, null };

        FormLayout lay = new FormLayout("10dlu:g,125dlu,5dlu,125dlu,10dlu:g", "15dlu:g,p,5dlu,p,15dlu,p,15dlu:g");
        CellConstraints cc = new CellConstraints();
        pan.setPreferredSize(new Dimension(420, 150));
        pan.setLayout(lay);
        pan.add(new JLabel("Bitte Ihre Emailadresse eingeben:"), cc.xy(2, 2));
        pan.add(field1, cc.xy(4, 2));
        pan.add(new JLabel("Bitte Passwort eingeben:"), cc.xy(2, 4));
        pan.add(field2, cc.xy(4, 4));

        buts[0] = new JButton("senden");
        buts[0].addActionListener(e -> {
            if (field1.getText()
                      .trim()
                      .equals("")) {
                JOptionPane.showMessageDialog(null, "Emailadresse muß angegeben werden");
                return;
            }
            String password = new String(field2.getPassword());
            if (password.trim()
                  .equals("")) {
                JOptionPane.showMessageDialog(null, "Passwort muß angegeben werden");
                field2.requestFocus();
                return;
            }
            boolean knownUser = sucheUser(field1.getText().trim(),password.trim());
            LOG.debug("Benutzer is bekannt: {}", knownUser);
            if (!knownUser) {
                JOptionPane.showMessageDialog(null,
                        "Emailadresse und/oder Passwort sind nicht registriert,\noder passen nicht zusammen!");
                field1.requestFocus();
                return;
            }
            LOG.debug("MacNotMatch = {}", macNotMatch);
            if (macNotMatch) {
                try {
                    INIFile inif = new INIFile(PROGHOME + LIB_SIG_JAR);
                    inif.setStringProperty("Updates", "1", strMACAdr.trim(), null);
                    inif.setStringProperty("Updates", "2", field1.getText(), null);
                    inif.setStringProperty("Updates", "3", password, null);
                    inif.save();
                } catch (Exception ex) {
                    LOG.error("Exception: ", ex);
                }

            }
            TheraPiUpdates.updateallowed = true;
            if (UpdateConfig.getInstance()
                            .isDeveloperMode()) {
                updateTab.starteFTP();
            } else {
                updatePanel.starteFTP();
            }

            dlg.dispose();
        });
        buts[1] = new JButton("abbrechen");
        buts[1].addActionListener(e -> {
            if (starteTheraPi) {
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
        KeyListener kl = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    e.consume();
                }
            }
        };
        field1.addKeyListener(kl);
        field2.addKeyListener(kl);
        pan.add(buts[1], cc.xy(2, 6));
        pan.add(buts[0], cc.xy(4, 6));
        pan.validate();
        dlg.setPreferredSize(new Dimension(550, 200));
        dlg.setTitle("Zugang zum Thera-Pi Update-Explorer");
        dlg.validate();
        dlg.pack();
        dlg.setLocationRelativeTo(jFrame);
        dlg.setVisible(true);
        field1.requestFocus();
    }

    private boolean sucheUser(String email, String pw) {
        try {
            TheraPiUpdates.userdaten = SqlInfo.holeSatz(TheraPiUpdates.conn, "regtpuser", " * ",
                    "email='" + email + "' and pw='" + pw + "'");
            if (!userdaten.isEmpty()) {
                return true;
            }
        } catch (Exception ex) {
            LOG.error("Exception: ", ex);
        }
        return false;
    }

    private static String getMacAddress() {

        StringBuilder result = new StringBuilder();
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                byte[] hardwareAddress = ni.getHardwareAddress();
                if (hardwareAddress != null) {
                    for (byte address : hardwareAddress) {
                        result.append(String.format("%02X", address));
                    }
                    if (result.length() > 0 && !ni.isLoopback()) {
                        return result.toString();
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Exception: ", e);
        }
        return result.toString();
    }
}
