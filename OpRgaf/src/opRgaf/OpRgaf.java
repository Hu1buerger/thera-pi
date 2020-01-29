package opRgaf;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import CommonTools.INIFile;
import CommonTools.SqlInfo;
import CommonTools.StartOOApplication;
import RehaIO.RehaReverseServer;
import RehaIO.SocketClient;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import environment.Path;
import io.RehaIOMessages;
import logging.Logging;
import sql.DatenquellenFactory;

public class OpRgaf implements WindowListener {

    /**
     * @param args
     */
    public final Cursor wartenCursor = new Cursor(Cursor.WAIT_CURSOR);
    public final Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    public final Cursor kreuzCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
    public final Cursor cmove = new Cursor(Cursor.MOVE_CURSOR); // @jve:decl-index=0:
    public final Cursor cnsize = new Cursor(Cursor.N_RESIZE_CURSOR); // @jve:decl-index=0:
    public final Cursor cnwsize = new Cursor(Cursor.NW_RESIZE_CURSOR); // @jve:decl-index=0:
    public final Cursor cnesize = new Cursor(Cursor.NE_RESIZE_CURSOR); // @jve:decl-index=0:
    public final Cursor cswsize = new Cursor(Cursor.SW_RESIZE_CURSOR); // @jve:decl-index=0:
    public final Cursor cwsize = new Cursor(Cursor.W_RESIZE_CURSOR); // @jve:decl-index=0:
    public final Cursor csesize = new Cursor(Cursor.SE_RESIZE_CURSOR); // @jve:decl-index=0:
    public final Cursor cssize = new Cursor(Cursor.S_RESIZE_CURSOR); // @jve:decl-index=0:
    public final Cursor cesize = new Cursor(Cursor.E_RESIZE_CURSOR); // @jve:decl-index=0:
    public final Cursor cdefault = new Cursor(Cursor.DEFAULT_CURSOR); // @jve:decl-index=0:

    public static boolean DbOk;
    JFrame jFrame;
    public static JFrame thisFrame = null;
    public Connection conn;
    public static OpRgaf thisClass;

    public static IOfficeApplication officeapplication;

    public String dieseMaschine = null;


    public static String officeProgrammPfad = "C:/Program Files (x86)/LibreOffice 3";
    public static String officeNativePfad = "C:/RehaVerwaltung/Libraries/lib/openofficeorg/";

    public static String aktIK = "510841109";

    public static HashMap<String, String> hmAbrechnung = new HashMap<String, String>();
    public static HashMap<String, String> hmFirmenDaten = null;
    public static HashMap<String, String> hmAdrPDaten = new HashMap<String, String>();

    public static boolean testcase = false;
    public OpRgafTab otab = null;

    public static int xport = -1;
    public static boolean xportOk = false;
    public RehaReverseServer rehaReverseServer = null;
    public static int rehaReversePort = -1;
    public SqlInfo sqlInfo;
    public static OpRgAfIni iniOpRgAf;
    static String proghome;

    public static void main(String[] args) {
        new Logging("oprgaf");
        OpRgaf application = new OpRgaf();
        application.getInstance();
        application.getInstance().sqlInfo = new SqlInfo();
        if (args.length > 0 || testcase) {
            proghome = Path.Instance.getProghome();
            if (!testcase) {
                System.out.println("hole daten aus INI-Datei " + args[0]);
                INIFile inif = new INIFile(args[0] + "ini/" + args[1] + "/rehajava.ini");

                inif = new INIFile(args[0] + "ini/" + args[1] + "/rehajava.ini");
                officeProgrammPfad = inif.getStringProperty("OpenOffice.org", "OfficePfad");
                officeNativePfad = inif.getStringProperty("OpenOffice.org", "OfficeNativePfad");

                aktIK = args[1];

                iniOpRgAf = new OpRgAfIni(args[0], "ini/", args[1], "oprgaf.ini");
                AbrechnungParameter(proghome);
                FirmenDaten(proghome);
                if (args.length >= 3) {
                    rehaReversePort = Integer.parseInt(args[2]);
                }
            } else {
                iniOpRgAf = new OpRgAfIni(proghome, "ini/", aktIK, "oprgaf.ini");
                AbrechnungParameter(proghome);
                FirmenDaten(proghome);
            }
            if (testcase) {
                System.out.println(iniOpRgAf.getMahnParameter());
                System.out.println("TestCase = " + testcase);
                AbrechnungParameter(proghome);
                FirmenDaten(proghome);

            }
            final OpRgaf xOpRgaf = application;

            application.StarteDB();

            application.getJFrame();
        } else {
            JOptionPane.showMessageDialog(null,
                    "Keine Datenbankparameter übergeben!\nReha-Statistik kann nicht gestartet werden");
            System.exit(0);
        }

    }

    /********************/

    public JFrame getJFrame() {
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
        thisClass = this;
        jFrame = new JFrame() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void setVisible(final boolean visible) {

                if (getState() != JFrame.NORMAL) {
                    setState(JFrame.NORMAL);
                }

                if (visible) {
                    // setDisposed(false);
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

        new InitHashMaps();

        try {
            rehaReverseServer = new RehaReverseServer(7000);
        } catch (Exception ex) {
            rehaReverseServer = null;
        }
        sqlInfo.setFrame(jFrame);
        jFrame.addWindowListener(this);
        jFrame.setSize(1000, 675);
        jFrame.setTitle("Thera-Pi  Rezeptgebühr-/Ausfall-/Verkaufsrechnungen ausbuchen u. Mahnwesen  [IK: " + aktIK
                + "] " + "[Server-IP: " + "dbIpAndName" + "]"); //FIXME: dpipandname
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        otab = new OpRgafTab();
        otab.setHeader(0);
        otab.setFirstFocus();

        jFrame.getContentPane()
              .add(otab);

        jFrame.setIconImage(Toolkit.getDefaultToolkit()
                                   .getImage(System.getProperty("user.dir") + File.separator + "icons" + File.separator
                                           + "Guldiner_I.png"));
        jFrame.setVisible(true);
        thisFrame = jFrame;
        try {
            new SocketClient().setzeRehaNachricht(OpRgaf.rehaReversePort,
                    "AppName#OpRgaf#" + Integer.toString(OpRgaf.xport));
            new SocketClient().setzeRehaNachricht(OpRgaf.rehaReversePort, "OpRgaf#" + RehaIOMessages.IS_STARTET);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Fehler in der Socketkommunikation");
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                otab.opRgafPanel.setzeFocus();
            }
        });
        return jFrame;
    }

    /********************/

    public OpRgaf getInstance() {
        thisClass = this;
        return this;
    }


    /*******************/

    public static void stoppeDB() {
        try {
            OpRgaf.thisClass.conn.close();
            OpRgaf.thisClass.conn = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void StarteDB() {
        final OpRgaf obj = OpRgaf.thisClass;


        try {

            obj.conn = new DatenquellenFactory(aktIK).createConnection();
            OpRgaf.thisClass.sqlInfo.setConnection(obj.conn);
            OpRgaf.DbOk = true;
            System.out.println("Datenbankkontakt hergestellt");
        } catch (final SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            OpRgaf.DbOk = false;

        }
        return;
    }


    @Override
    public void windowActivated(WindowEvent arg0) {
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
        if (OpRgaf.thisClass.conn != null) {
            try {
                OpRgaf.thisClass.conn.close();
                System.out.println("Datenbankverbindung wurde geschlossen");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        iniOpRgAf.saveLastSelection();
        if (OpRgaf.thisClass.conn != null) {
            try {
                OpRgaf.thisClass.conn.close();
                System.out.println("Datenbankverbindung wurde geschlossen");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (OpRgaf.thisClass.rehaReverseServer != null) {
            try {
                new SocketClient().setzeRehaNachricht(OpRgaf.rehaReversePort, "OpRgaf#" + RehaIOMessages.IS_FINISHED);
                rehaReverseServer.serv.close();
                System.out.println("ReverseServer geschlossen");
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

    public static void AbrechnungParameter(String proghome) {
        hmAbrechnung.clear();
        /******** Heilmittelabrechnung ********/
        INIFile inif = new INIFile(proghome + "ini/" + aktIK + "/abrechnung.ini");
        hmAbrechnung.put("hmgkvformular", inif.getStringProperty("HMGKVRechnung", "Rformular"));
        hmAbrechnung.put("hmgkvrechnungdrucker", inif.getStringProperty("HMGKVRechnung", "Rdrucker"));
        hmAbrechnung.put("hmgkvtaxierdrucker", inif.getStringProperty("HMGKVRechnung", "Tdrucker"));
        hmAbrechnung.put("hmgkvbegleitzettel", inif.getStringProperty("HMGKVRechnung", "Begleitzettel"));
        hmAbrechnung.put("hmgkvrauchdrucken", inif.getStringProperty("HMGKVRechnung", "Rauchdrucken"));
        hmAbrechnung.put("hmgkvrexemplare", inif.getStringProperty("HMGKVRechnung", "Rexemplare"));

        hmAbrechnung.put("hmpriformular", inif.getStringProperty("HMPRIRechnung", "Pformular"));
        hmAbrechnung.put("hmpridrucker", inif.getStringProperty("HMPRIRechnung", "Pdrucker"));
        hmAbrechnung.put("hmpriexemplare", inif.getStringProperty("HMPRIRechnung", "Pexemplare"));

        hmAbrechnung.put("hmbgeformular", inif.getStringProperty("HMBGERechnung", "Bformular"));
        hmAbrechnung.put("hmbgedrucker", inif.getStringProperty("HMBGERechnung", "Bdrucker"));
        hmAbrechnung.put("hmbgeexemplare", inif.getStringProperty("HMBGERechnung", "Bexemplare"));
        /******** Rehaabrechnung ********/
        hmAbrechnung.put("rehagkvformular", inif.getStringProperty("RehaGKVRechnung", "RehaGKVformular"));
        hmAbrechnung.put("rehagkvdrucker", inif.getStringProperty("RehaGKVRechnung", "RehaGKVdrucker"));
        hmAbrechnung.put("rehagkvexemplare", inif.getStringProperty("RehaGKVRechnung", "RehaGKVexemplare"));
        hmAbrechnung.put("rehagkvik", inif.getStringProperty("RehaGKVRechnung", "RehaGKVik"));

        hmAbrechnung.put("rehadrvformular", inif.getStringProperty("RehaDRVRechnung", "RehaDRVformular"));
        hmAbrechnung.put("rehadrvdrucker", inif.getStringProperty("RehaDRVRechnung", "RehaDRVdrucker"));
        hmAbrechnung.put("rehadrvexemplare", inif.getStringProperty("RehaDRVRechnung", "RehaDRVexemplare"));
        hmAbrechnung.put("rehadrvik", inif.getStringProperty("RehaDRVRechnung", "RehaDRVik"));

        hmAbrechnung.put("rehapriformular", inif.getStringProperty("RehaPRIRechnung", "RehaPRIformular"));
        hmAbrechnung.put("rehapridrucker", inif.getStringProperty("RehaPRIRechnung", "RehaPRIdrucker"));
        hmAbrechnung.put("rehapriexemplare", inif.getStringProperty("RehaPRIRechnung", "RehaPRIexemplare"));
        hmAbrechnung.put("rehapriik", inif.getStringProperty("RehaPRIRechnung", "RehaPRIik"));

        hmAbrechnung.put("hmallinoffice", inif.getStringProperty("GemeinsameParameter", "InOfficeStarten"));
    }

    /***************************/
    public static void FirmenDaten(String proghome) {
        String[] stitel = { "Ik", "Ikbezeichnung", "Firma1", "Firma2", "Anrede", "Nachname", "Vorname", "Strasse",
                "Plz", "Ort", "Telefon", "Telefax", "Email", "Internet", "Bank", "Blz", "Kto", "Steuernummer", "Hrb",
                "Logodatei", "Zusatz1", "Zusatz2", "Zusatz3", "Zusatz4", "Bundesland" };
        hmFirmenDaten = new HashMap<String, String>();
        INIFile inif = new INIFile(proghome + "ini/" + OpRgaf.aktIK + "/firmen.ini");
        for (int i = 0; i < stitel.length; i++) {
            hmFirmenDaten.put(stitel[i], inif.getStringProperty("Firma", stitel[i]));
        }
    }

    /***************************/

    public static void starteOfficeApplication() {
        try {
            officeapplication = new StartOOApplication(OpRgaf.officeProgrammPfad, OpRgaf.officeNativePfad).start(false);
            System.out.println("OpenOffice ist gestartet und Active =" + officeapplication.isActive());
        } catch (OfficeApplicationException e1) {
            e1.printStackTrace();
        }
    }

}
