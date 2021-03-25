package rehaSql;

import static CommonTools.StringLiterals.C_REHA_VERWALTUNG;
import static CommonTools.StringLiterals.RTA_IK;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jdesktop.swingworker.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.SqlInfo;
import CommonTools.ini.INITool;
import rehaSql.RehaIO.RehaReverseServer;
import rehaSql.RehaIO.SocketClient;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ch.qos.logback.classic.util.ContextInitializer;
import io.RehaIOMessages;
import office.OOTools;
import sql.DatenquellenFactory;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

public class RehaSql implements WindowListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RehaSql.class);
    public static boolean DbOk;
    public static JFrame thisFrame = null;
    public Connection conn;
    static RehaSql thisClass;

    public static Optional<IOfficeApplication> officeapplication;

    static String progHome = C_REHA_VERWALTUNG;
    static String aktIK = RTA_IK;

    private static int xport = -1;
    private RehaReverseServer rehaReverseServer = null;
    static int rehaReversePort = -1;
    private SqlInfo sqlInfo = null;
    static boolean hasEditRights = true;

    public static void main(String[] args) {
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "./logs/conf/rehasql.xml");
        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();

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

        RehaSql application = new RehaSql();
        application.getInstance();
        application.getInstance().sqlInfo = new SqlInfo();

        start(path, aktik, application);

    }

    private static void start(String path, final String aktik, RehaSql application) {
        path = OOTools.exchangebackslashes(path);
        System.out.println("hole daten aus INI-Datei " + path);

        officeapplication = OOTools.initOffice(path, aktik);


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
        jFrame.setTitle("Thera-Pi  Sql-Modul  [IK: " + aktIK + "]  - Äußerste Vorsicht ist geboten!!!");
        jFrame.setIconImage(Toolkit.getDefaultToolkit()
                                   .getImage(System.getProperty("user.dir") + File.separator + "icons" + File.separator
                                           + "SQL-Modul.png"));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        jFrame.getContentPane()
              .add(new RehaSqlTab());
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

        try {
            RehaSql.thisClass.conn = new DatenquellenFactory(aktIK).createConnection();
            RehaSql.thisClass.sqlInfo.setConnection(RehaSql.thisClass.conn);
            RehaSql.DbOk = true;
            LOGGER.info("Datenbankkontakt hergestellt");
        } catch (final SQLException ex) {
            LOGGER.error("datenbank konnte nicht gestartet werden",ex);
            RehaSql.DbOk = false;
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
