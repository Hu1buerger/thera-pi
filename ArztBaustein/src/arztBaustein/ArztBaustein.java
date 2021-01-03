package arztBaustein;

import CommonTools.Monitor;
import CommonTools.ini.INIFile;
import CommonTools.ini.Settings;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import gui.LaF;
import logging.Logging;
import mandant.IK;
import office.OOService;
import org.jdesktop.swingx.JXFrame;
import sql.DatenquellenFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;

public class ArztBaustein {
    private IOfficeApplication officeapplication;
    Connection conn;

    JXFrame jFrame;

    private ArztBausteinPanel arztbausteinpanel;

    private ArztBaustein(Connection connection, IOfficeApplication officeapplication) {
        conn = connection;
        this.officeapplication = officeapplication;
    }

    public static void main(String[] args) throws SQLException {
        new Logging("arztbaustein");
        String proghome = args[0];
        IK ik = new IK(args[1]);

        String OpenOfficePfad = "C:/Program Files (x86)/OpenOffice.org 3";
        String OpenOfficeNativePfad = "C:/RehaVerwaltung/Libraries/lib/openofficeorg";

        if (args.length > 0) {
            System.out.println("hole daten aus INI-Datei " + proghome);
            Settings ini = new INIFile(proghome + "ini/" + ik.digitString() + "/rehajava.ini");

            OpenOfficePfad = ini.getStringProperty("OpenOffice.org", "OfficePfad");
            OpenOfficeNativePfad = ini.getStringProperty("OpenOffice.org", "OfficeNativePfad");
        }

        IOfficeApplication officeapp = starteOfficeApplication(OpenOfficePfad, OpenOfficeNativePfad);

        start(ik, officeapp);
    }

    public static void start(IK ik, IOfficeApplication officeapplication) throws SQLException {
        Connection connection = new DatenquellenFactory(ik.digitString()).createConnection();
        ArztBaustein arztbaustein = new ArztBaustein(connection, officeapplication);
        arztbaustein.getJFrame();
    }

    public JXFrame getJFrame() {
        if (jFrame == null) {
            jFrame = new JXFrame();
            Monitor monitor = new Monitor() {

                @Override
                public void statusChange(Object status) {
                    SwingUtilities.invokeLater(() -> {
                        if (status == Monitor.START) {
                            jFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        } else {
                            jFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                    });
                }
            };

            jFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            LaF.setPlastic();

            jFrame.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent arg0) {
                    closeconnection();
                }
            });
            jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            jFrame.setPreferredSize(new Dimension(1024, 800));

            jFrame.setTitle("Bausteine für ärztlichen Entlassbericht anlegen / ändern");

            jFrame.getContentPane()
                    .setPreferredSize(new Dimension(1024, 800));
            jFrame.getContentPane()
                    .setLayout(new GridLayout());
            jFrame.getContentPane()
                    .add(arztbausteinpanel = new ArztBausteinPanel(this.conn, this.officeapplication, monitor));
            WindowListener wl = new ArztBausteinWindowlistener(arztbausteinpanel);
            jFrame.addWindowListener(wl);
            jFrame.setVisible(true);

            jFrame.pack();
        }
        return jFrame;
    }

    private void closeconnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static IOfficeApplication starteOfficeApplication(String ooPath, String ooNativePath) {
        IOfficeApplication application = null;
        try {
            new OOService().start();
            application = new OOService().getOfficeapplication();
            System.out.println("OpenOffice ist gestartet und Active =" + application.isActive());
        } catch (OfficeApplicationException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return application;
    }
}
