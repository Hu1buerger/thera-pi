package rehaSql;

import static CommonTools.StringLiterals.C_PROGRAMME_OPEN_OFFICE_ORG_3;
import static CommonTools.StringLiterals.C_REHA_VERWALTUNG;
import static CommonTools.StringLiterals.C_REHA_VERWALTUNG_LIBRARIES_LIB_OPENOFFICEORG;
import static CommonTools.StringLiterals.DATEN_BANK_SECTION;
import static CommonTools.StringLiterals.JDBC_MYSQL_192_168_2_3_3306_RTADATEN;
import static CommonTools.StringLiterals.RTACURIE;
import static CommonTools.StringLiterals.RTAUSER;
import static CommonTools.StringLiterals.RTA_IK;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jdesktop.swingworker.SwingWorker;

import CommonTools.INIFile;
import CommonTools.INITool;
import CommonTools.SqlInfo;
import CommonTools.StartOOApplication;
import CommonTools.Verschluesseln;
import RehaIO.RehaReverseServer;
import RehaIO.SocketClient;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import io.RehaIOMessages;
import logging.Logging;

public class RehaSql implements WindowListener {

    
    public static boolean DbOk;
    public static JFrame thisFrame = null;
    public Connection conn;
    static RehaSql thisClass;

    public static IOfficeApplication officeapplication;

    private static String dbIpAndName = JDBC_MYSQL_192_168_2_3_3306_RTADATEN;
    private static String dbUser = RTAUSER;
    private static String dbPassword = RTACURIE;
    private static String officeProgrammPfad = C_PROGRAMME_OPEN_OFFICE_ORG_3;
    private static String officeNativePfad = C_REHA_VERWALTUNG_LIBRARIES_LIB_OPENOFFICEORG;
    static String progHome = C_REHA_VERWALTUNG;
    static String aktIK = RTA_IK;

   
    private static int xport = -1;
    private RehaReverseServer rehaReverseServer = null;
    static int rehaReversePort = -1;
    private SqlInfo sqlInfo = null;
    static boolean hasEditRights =true;

    public static void main(String[] args) {
        boolean argumentsGiven = args.length > 0;
        if (!argumentsGiven) {
            JOptionPane.showMessageDialog(null,
                    "Keine Datenbankparameter übergeben!\nReha-Sql kann nicht gestartet werden");
            return;
        }
        
        final String path = args[0];
        final String aktik = args[1];
        final String port = args[2];
        if (args.length >= 3) {

            rehaReversePort = Integer.parseInt(port);
        }
        if (args.length >= 4) {
            String isFull = args[3];
            hasEditRights = "full".equals(isFull);

        }

        new Logging("rehasql");

        RehaSql application = new RehaSql();
        application.getInstance();
        application.getInstance().sqlInfo = new SqlInfo();
        
        if (argumentsGiven ) {
            

                start(path, aktik, application);
        } else {
   
        }

    }

    private static void start(final String path, final String aktik, RehaSql application) {
        System.out.println("hole daten aus INI-Datei " + path);

        INIFile inif = new INIFile(path + "ini/" + aktik + "/rehajava.ini");
        dbIpAndName = inif.getStringProperty(DATEN_BANK_SECTION, "DBKontakt1");
        dbUser = inif.getStringProperty(DATEN_BANK_SECTION, "DBBenutzer1");
        String pw = inif.getStringProperty(DATEN_BANK_SECTION, "DBPasswort1");
        String decrypted;
        if (pw != null) {
            Verschluesseln man = Verschluesseln.getInstance();
            decrypted = man.decrypt(pw);
        } else {
            decrypted = "";
        }
        dbPassword = decrypted;
        inif = new INIFile(path + "ini/" + aktik + "/rehajava.ini");
        officeProgrammPfad = inif.getStringProperty("OpenOffice.org", "OfficePfad");
        officeNativePfad = inif.getStringProperty("OpenOffice.org", "OfficeNativePfad");
        progHome = path;
        aktIK = aktik;
        INITool.init(progHome + "ini/" + aktIK + "/");

         

         final RehaSql xapplication = application;
         new SwingWorker<Void, Void>() {
        @Override
        protected Void doInBackground() {
            xapplication.starteDB();
            long zeit = System.currentTimeMillis();
            while (!DbOk) {
                try {
                    Thread.sleep(20);
                    if (System.currentTimeMillis() - zeit > 10000) {
                        System.exit(0);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!DbOk) {
                JOptionPane.showMessageDialog(null,
                        "Datenbank konnte nicht geöffnet werden!\nReha-Sql kann nicht gestartet werden");
            }
            RehaSql.starteOfficeApplication();
            return null;
        }

         }.execute();
         application.getJFrame();
    }

    /********************/

    private void getJFrame() {
        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException
                | InstantiationException e) {
            e.printStackTrace();
        }
        thisClass = this;

        JFrame jFrame = new JFrame() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void setVisible(final boolean visible) {
                if (getState() != JFrame.NORMAL) {
                    setState(JFrame.NORMAL);
                }

                if (!visible || !isVisible()) {
                    super.setVisible(visible);
                }
                if (visible) {
                    int state = super.getExtendedState();
                    state &= ~JFrame.ICONIFIED;
                    super.setExtendedState(state);
                    super.setAlwaysOnTop(true);
                    super.toFront();
                    super.requestFocus();
                    super.setAlwaysOnTop(false);
                }
            }

            @Override
            public void toFront() {
                super.setVisible(true);
                int state = super.getExtendedState();
                state &= ~JFrame.ICONIFIED;
                super.setExtendedState(state);
                super.setAlwaysOnTop(true);
                super.toFront();
                super.requestFocus();
                super.setAlwaysOnTop(false);
            }
        };
        try {
            rehaReverseServer = new RehaReverseServer(7000);
        } catch (Exception ex) {
            rehaReverseServer = null;
        }
        sqlInfo.setFrame(jFrame);
        jFrame.addWindowListener(this);
        jFrame.setSize(1000, 700);
        jFrame.setTitle("Thera-Pi  Sql-Modul  [IK: " + aktIK + "] " + "[Server-IP: " + dbIpAndName
                + "] - Äußerste Vorsicht ist geboten!!!");
		jFrame.setIconImage( Toolkit.getDefaultToolkit().getImage( System.getProperty("user.dir")+File.separator+"icons"+File.separator+"SQL-Modul.png" ) );
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        jFrame.getContentPane().add(new RehaSqlTab());
        jFrame.setVisible(true);
        thisFrame = jFrame;
        try {
            new SocketClient().setzeRehaNachricht(RehaSql.rehaReversePort, "AppName#RehaSql#" + RehaSql.getXport());
            new SocketClient().setzeRehaNachricht(RehaSql.rehaReversePort, "RehaSql#" + RehaIOMessages.IS_STARTET);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Fehler in der Socketkommunikation");
        }
    }

    /********************/

    private RehaSql getInstance() {
        thisClass = this;
        return this;
    }

    /*******************/

    private void starteDB() {

        DatenbankStarten dbstart = new DatenbankStarten();
        dbstart.run();

    }

    /**********************************************************
     * 
     */
    final class DatenbankStarten implements Runnable {
        private void StarteDB() {
            final String sDB = "SQL";
            if (RehaSql.thisClass.conn != null) {
                try {
                    RehaSql.thisClass.conn.close();
                } catch (final SQLException e) {
                }
            }
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
            } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
                e.printStackTrace();
                System.out.println(sDB + "Treiberfehler: " + e.getMessage());
                RehaSql.DbOk = false;
                return;
            }
            try {
                RehaSql.thisClass.conn = DriverManager.getConnection(
                        dbIpAndName + "?jdbcCompliantTruncation=false&zeroDateTimeBehavior=convertToNull", dbUser,
                        dbPassword);
                RehaSql.thisClass.sqlInfo.setConnection(RehaSql.thisClass.conn);
                RehaSql.DbOk = true;
                System.out.println("Datenbankkontakt hergestellt");
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
                RehaSql.DbOk = false;

            }
        }

        @Override
        public void run() {
            StarteDB();
        }

    }

    @Override
    public void windowActivated(WindowEvent arg0) {
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
        if (RehaSql.thisClass.conn != null) {
            try {
                RehaSql.thisClass.conn.close();
                System.out.println("Datenbankverbindung wurde geschlossen");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        if (RehaSql.thisClass.conn != null) {
            try {
                RehaSql.thisClass.conn.close();
                System.out.println("Datenbankverbindung wurde geschlossen");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (RehaSql.thisClass.rehaReverseServer != null) {
            try {
                new SocketClient().setzeRehaNachricht(RehaSql.rehaReversePort, "RehaSql#" + RehaIOMessages.IS_FINISHED);
                rehaReverseServer.serv.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        System.exit(0);
    }

    @Override
    public void windowDeactivated(WindowEvent arg0) {
    }

    @Override
    public void windowDeiconified(WindowEvent arg0) {
    }

    @Override
    public void windowIconified(WindowEvent arg0) {
    }

    @Override
    public void windowOpened(WindowEvent arg0) {
    }

    /***************************/

    public static void starteOfficeApplication() {
        try {
            officeapplication = new StartOOApplication(RehaSql.officeProgrammPfad, RehaSql.officeNativePfad)
                    .start(false);
            System.out.println("OpenOffice ist gestartet und Active =" + officeapplication.isActive());
        } catch (OfficeApplicationException e1) {
            e1.printStackTrace();
        }
    }

    static boolean isReadOnly() {
        return !hasEditRights;
    }

    public static int getXport() {
        return xport;
    }

    public static void setXport(int xport) {
        RehaSql.xport = xport;
    }

}
