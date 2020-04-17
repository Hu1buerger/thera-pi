package offenePosten;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jdesktop.swingworker.SwingWorker;

import CommonTools.INIFile;
import CommonTools.INITool;
import CommonTools.OpCommon;
import CommonTools.SqlInfo;
import RehaIO.RehaReverseServer;
import RehaIO.SocketClient;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import io.RehaIOMessages;
import logging.Logging;
import office.OOService;
import sql.DatenquellenFactory;

public class OffenePosten implements WindowListener {

    /**
     * @param args
     */
    public static boolean DbOk;
    JFrame jFrame;
    public static JFrame thisFrame = null;
    public Connection conn;



    public static String progHome = "C:/RehaVerwaltung/";
    public static String aktIK = "510841109";

    public static HashMap<String, Object> mahnParameter = new HashMap<String, Object>();

    public static HashMap<String, String> hmAbrechnung = new HashMap<String, String>();
    public static HashMap<String, String> hmFirmenDaten = null;
    public static HashMap<String, String> hmAdrPDaten = new HashMap<String, String>();


    public static boolean testcase = false;
    public SqlInfo sqlInfo;

    OffenepostenTab otab = null;


    public RehaReverseServer rehaReverseServer = null;
    public static int rehaReversePort = -1;


    private static String path2IniFile;
    private static String path2TemplateFiles;
    private static int vorauswahlSuchkriterium = -1;
    private static boolean settingsLocked = false;
    private static String iniFile;
    private static boolean erlaubeBarInKasse = false;
    private static boolean iniValuesValid = false;

    // @VisibleForTesting
    OffenePosten(String testIdent) {
        if ( !testIdent.contentEquals("JUnit")) {
            System.out.println("Attention! This method was created for Unit-testing and nothing else!");
            return;
        }
        setAktIK("123456789");
        System.out.println("OP-init, setting test mand to 123456789");
    }
    
    private OffenePosten() {
        
    }
    
    public static void main(String[] args) {
        new Logging("offeneposten");
       OffenePosten instance = new OffenePosten();
        instance.sqlInfo = new SqlInfo();
        System.out.println("OP main: " + instance);

        if (args.length > 0 || testcase) {
            if (!testcase) {
                System.out.println("hole daten aus INI-Datei " + args[0]);
                INIFile inif = new INIFile(args[0] + "ini/" + args[1] + "/rehajava.ini");

                 String officeProgrammPfad = inif.getStringProperty("OpenOffice.org", "OfficePfad") ;
                String officeNativePfad = inif.getStringProperty("OpenOffice.org", "OfficeNativePfad") ;
                try {
					new OOService().start(officeNativePfad, officeProgrammPfad);
				} catch (FileNotFoundException | OfficeApplicationException e) {
					e.printStackTrace();
				}
                
                progHome = args[0];
                aktIK = args[1];
                path2IniFile = progHome + "ini/" + aktIK + "/";
                path2TemplateFiles = progHome + "vorlagen/" + aktIK;
                INITool.init(path2IniFile);
                /*******************************************************/

                     instance.StarteDB();

                new SwingWorker<Void, Void>() {
                    @Override

                    protected Void doInBackground() throws java.lang.Exception {
                        while (!OffenePosten.DbOk) {
                            Thread.sleep(30);
                        }
                        iniFile = "offeneposten.ini";
                        INIFile oinif = INITool.openIni(path2IniFile, iniFile);
                        OpCommon.readMahnParamCommon(oinif, mahnParameter);

                        for (int i = 1; i <= 4; i++) {
                            OpCommon.addFormNb(oinif, "General", "FormularMahnung", "Mahnung", i, mahnParameter,
                                    path2TemplateFiles);
                        }

                        mahnParameter.put("diralterechnungen", oinif.getStringProperty("General", "DirAlteRechnungen"));


                        readLastSelection(oinif);
                        readBarAnKasse(oinif);
                        iniValuesValid = true; // werte für Anzeige OP sind jetzt gültig

                        AbrechnungParameter(progHome);
                        FirmenDaten(progHome);

                        return null;
                    }
                }.execute();
                if (args.length >= 3) {
                    rehaReversePort = Integer.parseInt(args[2]);
                }

            } else {
                mahnParameter.put("frist1", 31);
                mahnParameter.put("frist2", 11);
                mahnParameter.put("frist3", 11);
                mahnParameter.put("einzelmahnung", Boolean.TRUE);
                mahnParameter.put("drucker", "RICOH Aficio MP C2800 PS SW");
                mahnParameter.put("exemplare", 5);
                mahnParameter.put("inofficestarten", Boolean.TRUE);
                mahnParameter.put("erstsuchenab", "2009-01-01");
                mahnParameter.put("formular1", "2009-01-01");
                mahnParameter.put("formular2", "2009-01-01");
                mahnParameter.put("formular3", "2009-01-01");
                mahnParameter.put("formular4", "2009-01-01");
                mahnParameter.put("diralterechnungen", "l:/projekte/rta/dbf/rechnung/");
                AbrechnungParameter(progHome);
                FirmenDaten(progHome);

            }
            if (testcase) {
                System.out.println(mahnParameter);
                System.out.println("TestCase = " + testcase);
                AbrechnungParameter(progHome);
                FirmenDaten(progHome);

            }
            instance.getJFrame();
        } else {
            JOptionPane.showMessageDialog(null,
                    "Keine Datenbankparameter übergeben!\nReha-Statistik kann nicht gestartet werden");
        }

    }

    // @VisibleForTesting
    String getAktIK() {
        return aktIK;
    }
    
    // @VisibleForTesting
    void setAktIK(String ik2set ) {
        aktIK = ik2set;
    }
    
    // @VisibleForTesting
    String getProghome() {
        return progHome;
    }
    
    // @VisibleForTesting
    void setProghome(String ph2set) {
        progHome = ph2set;
    }
    
    /********************/

    public JFrame getJFrame() {
        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        jFrame = new JFrame() {


            private static final long serialVersionUID = 1L;

            @Override
            public void setVisible(final boolean shallBeVisible) {

                if (getState() != JFrame.NORMAL) {
                    setState(JFrame.NORMAL);
                }

                
                if (!shallBeVisible || !isVisible()) {
                    super.setVisible(shallBeVisible);
                }

                if (shallBeVisible) {
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
            rehaReverseServer = new RehaReverseServer(7000, jFrame);
        } catch (Exception ex) {
            rehaReverseServer = null;
            ex.printStackTrace();
        }
        sqlInfo.setFrame(jFrame);
        jFrame.addWindowListener(this);
        jFrame.setSize(1000, 750);
        String string =  "dbIpAndName"; // XXX:fixme name
        jFrame.setTitle(
                "Thera-Pi  Offene-Posten / Mahnwesen  [IK: " + aktIK + "] " + "[Server-IP: " + string + "]");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        otab = new OffenepostenTab(this);
        otab.setHeader(0);

        jFrame.getContentPane()
              .add(otab);
        jFrame.setIconImage(Toolkit.getDefaultToolkit()
                                   .getImage(System.getProperty("user.dir") + File.separator + "icons" + File.separator
                                           + "hauptbuch_I.jpg"));
        jFrame.setVisible(true);
        thisFrame = jFrame;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                otab.setFirstFocus();
            }
        });

        try {
            new SocketClient().setzeRehaNachricht(OffenePosten.rehaReversePort,
                    "AppName#OffenePosten#" + rehaReverseServer.getPort());
            new SocketClient().setzeRehaNachricht(OffenePosten.rehaReversePort,
                    "OffenePosten#" + RehaIOMessages.IS_STARTET);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Fehler in der Socketkommunikation");
            ex.printStackTrace();
        }

        return jFrame;
    }

    private void StarteDB() {


            try {

                conn = new DatenquellenFactory(aktIK).createConnection();
                sqlInfo.setConnection(conn);
                OffenePosten.DbOk = true;
                System.out.println("Datenbankkontakt hergestellt");
            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
                OffenePosten.DbOk = false;

            }
        }







    /*****************************************************************
     *
     */

    @Override
    public void windowActivated(WindowEvent arg0) {
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Datenbankverbindung wurde geschlossen");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (rehaReverseServer != null) {
            new SocketClient().setzeRehaNachricht(OffenePosten.rehaReversePort,
                    "OffenePosten#" + RehaIOMessages.IS_FINISHED);
            try {
                rehaReverseServer.serv.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void windowClosing(WindowEvent arg0) {
        saveLastSelection();
        if (conn != null) {
            try {
                conn.close();
                System.out.println("OP: Datenbankverbindung wurde geschlossen");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (rehaReverseServer != null) {
            new SocketClient().setzeRehaNachricht(OffenePosten.rehaReversePort,
                    "OffenePosten#" + RehaIOMessages.IS_FINISHED);
            try {
                rehaReverseServer.serv.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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

        INIFile inif = INITool.openIni(proghome + "ini/" + aktIK + "/", "abrechnung.ini");
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
        INIFile inif = INITool.openIni(proghome + "ini/" + OffenePosten.aktIK + "/", "firmen.ini");
        for (int i = 0; i < stitel.length; i++) {
            hmFirmenDaten.put(stitel[i], inif.getStringProperty("Firma", stitel[i]));
        }
    }

    /***************************/

  

    public static void setVorauswahl(int value) {
        vorauswahlSuchkriterium = value;
    }

    public static int getVorauswahl(int max) {
        int maxWait = 20;
        int waitTimes = maxWait;
        while ((vorauswahlSuchkriterium < 0) && (waitTimes-- > 0)) { // lesen aus ini ist noch nicht fertig...
            try {
                Thread.sleep(25);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        if (waitTimes == 0) {
            System.out.println("OP getVorauswahl: " + 0 + "(Abbruch ini-read)");
            return 0;
        }
        // System.out.println("OP getVorauswahl: " + vorauswahlSuchkriterium +"(" + max
        // + ")");
        return vorauswahlSuchkriterium < max ? vorauswahlSuchkriterium : 0;
    }

    private static void readBarAnKasse(INIFile inif) {
        erlaubeBarInKasse = false;
        if (inif.getStringProperty("offenePosten", "erlaubeBarzahlung") != null) {
            erlaubeBarInKasse = inif.getBooleanProperty("offenePosten", "erlaubeBarzahlung");
        }
    }

    public static boolean getBarAusbuchenErlaubt() {
        int maxWait = 20;
        int waitTimes = maxWait;
        while ((!iniValuesValid) && (waitTimes-- > 0)) { // lesen aus ini ist noch nicht fertig...
            try {
                Thread.sleep(25);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        if (waitTimes == 0) {
            System.out.println("OP erlaubeBarInKasse: " + erlaubeBarInKasse + "(Abbruch ini-read)");
        }
        return erlaubeBarInKasse;
    }

    /**
     * liest die zuletzt verwandten (Such-)Einstellungen aus der ini-Datei ist keine
     * Einstellung vorhanden, werden hier die Defaults gesetzt
     */
    private static void readLastSelection(INIFile inif) {
        String section = "offenePosten";
        if (inif.getStringProperty(section, "Suchkriterium") != null) { // Eintrag in ini vorhanden?
            setVorauswahl(inif.getIntegerProperty(section, "Suchkriterium"));
        } else {
            setVorauswahl(0); // Default-Wert setzen
        }
        if (inif.getStringProperty(section, "lockSettings") != null) {
            settingsLocked = inif.getBooleanProperty(section, "lockSettings");
        } else {
            settingsLocked = true;
        }
        System.out.println("OP readLastSel.: " + vorauswahlSuchkriterium);
    }

    /**
     * schreibt die zuletzt verwandten Such-Einstellungen (falls geändert) in die
     * ini-Datei
     */
    public void saveLastSelection() {
        INIFile inif = INITool.openIni(path2IniFile, iniFile);
        String section = "offenePosten", comment = null;
        boolean saveChanges = false;
        if (!settingsLocked) { // ini-Einträge dürfen aktualisiert werden
            if (inif.getStringProperty(section, "lockSettings") == null) {
                inif.setBooleanProperty(section, "lockSettings", true, "Aktualisieren der Eintraege gesperrt");
                saveChanges = true;
            }

            comment = "zuletzt gesucht";
            if (inif.getStringProperty(section, "Suchkriterium") == null) {
                inif.setIntegerProperty(section, "Suchkriterium", vorauswahlSuchkriterium, comment);
                saveChanges = true;
            } else {
                if (vorauswahlSuchkriterium != inif.getIntegerProperty(section, "Suchkriterium")) {
                    inif.setIntegerProperty(section, "Suchkriterium", vorauswahlSuchkriterium, comment);
                    saveChanges = true;
                }
            }
            if (inif.getStringProperty(section, "erlaubeBarzahlung") == null) { // default setzen - Ändern nur über
                                                                                // Systemeinstellungen
                comment = "ermoeglicht Barzahlung von Rechnungen in Barkasse zu buchen";
                inif.setBooleanProperty(section, "erlaubeBarzahlung", false, comment);
                saveChanges = true;
            }

            if (saveChanges) {
                INITool.saveIni(inif);
            }
        }
    }
}
