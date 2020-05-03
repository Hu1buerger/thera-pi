package rehaWissen;

import java.awt.Cursor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import environment.SWTJarLoader;
import rtaWissen.BrowserFenster;

public class RehaWissen {
    static JFrame jDiag = null;
    JXPanel contentPanel = null;
    static JLabel standDerDingelbl = null;
    public static RehaWissen thisClass = null;
    static boolean socketoffen = false;
    public boolean HilfeDbOk;
    public Connection hilfeConn = null;
    private static  Logger logger = LoggerFactory.getLogger(RehaWissen.class);

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeAndWait(new SWTJarLoader());
        } catch (InvocationTargetException | InterruptedException e1) {
            e1.printStackTrace();
        }
        RehaWissen application = new RehaWissen();

        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
        } catch (ClassNotFoundException e) {

            e.printStackTrace();
        } catch (InstantiationException e) {

            e.printStackTrace();
        } catch (IllegalAccessException e) {

            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {

            e.printStackTrace();
        }
        SystemConfig.InetSeitenEinlesen();
        NativeInterface.initialize();
        JWebBrowser.useXULRunnerRuntime();
        NativeInterface.open();
        new Thread(()-> NativeInterface.runEventPump() ,"eventpump");

        final RehaWissen xapplication = application;

                jDiag = xapplication.getDialog();

                jDiag.validate();
                jDiag.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                jDiag.pack();
                jDiag.setExtendedState(JXFrame.MAXIMIZED_BOTH);
                jDiag.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                jDiag.setVisible(true);


    }

    private JFrame getDialog() {
        thisClass = this;
        new HilfeDatenbankStarten().StarteDB();
        long zeit = System.currentTimeMillis();
        while (!HilfeDbOk) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
            if ((System.currentTimeMillis() - zeit) > 2000) {
                break;
            }
        }
        if (!HilfeDbOk) {
            logger .error("HilfeDatenbank startet nicht");
            // System.exit(0);
        }
        return new BrowserFenster(null, "RehaWissen");
    }
}

final class HilfeDatenbankStarten implements Runnable {

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
            connProperties.setProperty("user", "dbo336243054");
            connProperties.setProperty("password", "allepreise");
            connProperties.setProperty("host", "db2614.1und1.de");
            connProperties.setProperty("port", "3306");
            connProperties.setProperty("compression", "false");
            connProperties.setProperty("NO_DRIVER_INFO", "1");
            RehaWissen.thisClass.hilfeConn = DriverManager.getConnection(
                    "jdbc:jpmdbc:http://www.thera-pi.org/jpmdbc.php?db336243054", connProperties);

            // Date zeit = new Date();
            // String stx = "Insert into eingeloggt set comp='"+"Preis-Listen
            // "+java.net.InetAddress.getLocalHost()+": import"+"',
            // zeit='"+zeit.toString()+"', einaus='ein';";
            // sqlAusfuehren(stx);

        } catch (final SQLException ex) {
            System.out.println("SQLException-1: " + ex.getMessage());
            System.out.println("SQLState-1: " + ex.getSQLState());
            System.out.println("VendorError-1: " + ex.getErrorCode());
            JOptionPane.showMessageDialog(null,
                    "Fehler: Datenbankkontakt zum Preislisten-Server konnte nicht hergestellt werden.");
            return;
        }
        return;
        /*
         * if (obj.hilfeConn != null){ try{ obj.hilfeConn.close();} catch(final
         * SQLException e){} } try{
         *
         * Class.forName(SystemConfig.hmHilfeServer.get("HilfeDBTreiber")).newInstance()
         * ; RehaWissen.thisClass.HilfeDbOk = true; } catch ( final Exception e ){
         * System.out.println(sDB+"-Treiberfehler: " + e.getMessage());
         * RehaWissen.thisClass.HilfeDbOk = false; return ; } try {
         * RehaWissen.thisClass.hilfeConn = (Connection)
         * DriverManager.getConnection(SystemConfig.hmHilfeServer.get("HilfeDBLogin"),
         * SystemConfig.hmHilfeServer.get("HilfeDBUser"),SystemConfig.hmHilfeServer.get(
         * "HilfeDBPassword")); } catch (final SQLException ex) {
         * System.out.println("SQLException: " + ex.getMessage());
         * System.out.println("SQLState: " + ex.getSQLState());
         * System.out.println("VendorError: " + ex.getErrorCode());
         * RehaWissen.thisClass.HilfeDbOk = false; return; }
         * System.out.println("HilfeServer wurde - gestartet"); return;
         */
    }

    public void StarteDB() {
        run();
    }
}
