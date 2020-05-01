package hauptFenster;

import static java.awt.event.KeyEvent.KEY_PRESSED;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_K;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_P;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.KeyEvent.VK_X;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;
import java.util.TooManyListenersException;
import java.util.Vector;

import javax.media.CannotRealizeException;
import javax.media.CaptureDeviceInfo;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.format.YUVFormat;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thera_pi.updates.TestForUpdates;
import org.therapi.reha.patient.PatientHauptPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;
import com.sun.star.uno.Exception;

import CommonTools.Colors;
import CommonTools.JRtaTextField;
import CommonTools.RehaEvent;
import CommonTools.RehaEventClass;
import CommonTools.RehaEventListener;
import CommonTools.SqlInfo;
import CommonTools.ini.INIFile;
import CommonTools.ini.INITool;
import abrechnung.AbrechnungGKV;
import abrechnung.AbrechnungReha;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import anmeldungUmsatz.Anmeldungen;
import anmeldungUmsatz.Umsaetze;
import arztFenster.ArztPanel;
import barKasse.Barkasse;
import benutzerVerwaltung.BenutzerRechte;
import dialoge.RehaSmartDialog;
import dta301.Dta301;
import entlassBerichte.EBerichtPanel;
import environment.LadeProg;
import environment.Path;
import geraeteInit.BarCodeScanner;
import gui.Cursors;
import io.RehaIOMessages;
import krankenKasse.KassenPanel;
import logging.Logging;
import mandant.Mandant;
import menus.TerminMenu;
import ocf.OcKVK;
import office.OOService;
import rechteTools.Rechte;
import rehaInternalFrame.JRehaInternal;
import rehaInternalFrame.OOODesktopManager;
import roogle.RoogleFenster;
import systemEinstellungen.ImageRepository;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemInit;
import systemTools.RehaPainters;
import systemTools.RezeptFahnder;
import systemTools.TestePatStamm;
import terminKalender.TerminFenster;
import therapi.updatehint.UpdatesMain;
import urlaubBeteiligung.Beteiligung;
import urlaubBeteiligung.Urlaub;
import verkauf.VerkaufTab;

public class Reha implements RehaEventListener {

    public PatientHauptPanel patpanel = null;
    public EBerichtPanel eberichtpanel = null;
    public KassenPanel kassenpanel = null;
    public ArztPanel arztpanel = null;
    public TerminFenster terminpanel = null;
    public RoogleFenster rooglepanel = null;
    public AbrechnungGKV abrechnungpanel = null;
    public Anmeldungen anmeldungenpanel = null;
    public Umsaetze umsaetzepanel = null;
    public Beteiligung beteiligungpanel = null;
    public Urlaub urlaubpanel = null;
    public VerkaufTab verkaufpanel = null;
    public Barkasse barkassenpanel = null;
    public AbrechnungReha rehaabrechnungpanel = null;
    public BenutzerRechte benutzerrechtepanel = null;
    public SystemInit systeminitpanel = null;
    public Dta301 dta301panel = null;

    public final int patiddiff = 5746;
    JXFrame jFrame = null;

    private JMenuBar jJMenuBar = null;
    private JMenu fileMenu = null;
    private JMenu stammMenu = null;
    private JMenu abrechnungMenu = null;
    private JMenu statistikMenu = null;
    private JMenu toolsMenu = null;
    private JMenu bueroMenu = null;
    private JMenu verkaufMenu = null;
    private JMenu urlaubMenu = null;
    private JMenu helpMenu = null;
    private JMenuItem exitMenuItem = null;
    JMenuItem aboutMenuItem = null;
    private JMenuItem aboutF2RescueMenuItem = null;
    public JXStatusBar jXStatusBar = null;
    private int dividerLocLR = 0;
    public JLabel shiftLabel = null;
    public static boolean dividerOk = false;
    public JLabel messageLabel = null;
    public JLabel dbLabel = null;
    public JXPanel versionbar = null;
    public JLabel mousePositionLabel = null;
    public JXPanel jxPinContainer = null;
    public JXPanel jxCopyContainer = null;
    public JLabel copyLabel = null;

    public JXPanel jxLinks = null;
    public JXPanel jxRechts = null;
    public JXPanel jxRechtsOben = null;
    public JXPanel jxRechtsUnten = null;
    public UIFSplitPane jSplitLR = null;
    public UIFSplitPane jSplitRechtsOU = null;
    public JXTitledPanel jxTitelOben = null;
    public JXTitledPanel jxTitelUnten = null;

    public JXPanel jInhaltOben = null;
    public JXPanel jInhaltUnten = null;
    public JXPanel jEventTargetOben = null;
    public JXPanel jEventTargetUnten = null;
    public JXPanel jContainerOben = null;
    public JXPanel jContainerUnten = null;
    public JXPanel jLeerOben = null;
    public JXPanel jLeerUnten = null;

    public boolean initok = false;
    public boolean splashok = false;

    public RehaSmartDialog splash = null;

    public Connection conn = null;
    public Connection hilfeConn = null;

    public static boolean DbOk = false;
    public static boolean HilfeDbOk = false;

    public static String progRechte = "0123";

    public final static String Titel = "Thera-\u03C0";

    public boolean KollegenOk = false;
    public static String aktLookAndFeel = "";
    public static SystemConfig sysConf = null;
    public static IOfficeApplication officeapplication;

    public static BarCodeScanner barcodeScanner = null;

    public static CompoundPainter[] RehaPainter = { null, null, null, null, null };
    public Vector<Object> aktiveFenster = new Vector<Object>();
    public final String NULL_DATE = "  .  .    ";
    public static String aktUser = "";
    public static String kalMin = "";
    public static String kalMax = "";
    public static String Titel2;
    public int vollsichtbar = 0;
    public JDesktopPane deskrechts = new JDesktopPane();
    public JDesktopPane[] desktops = { null, null, null, null };
    public JDesktopPane desktopUnten = new JDesktopPane();
    public JXPanel jpOben = null;
    public JXPanel jpUnten = null;

    public static boolean patientFirstStart = true;
    public static boolean terminFirstStart = true;
    public static boolean kassenFirstStart = true;
    public static boolean arztFirstStart = true;
    public static boolean iconsOk = false;
    public static ImageIcon rehaBackImg = null;
    public JLabel bunker = null;
    public JProgressBar Rehaprogress = null;
    public GradientPaint gp1 = new GradientPaint(0, 0, new Color(112, 141, 255), 0, 25, Color.WHITE, true);
    public GradientPaint gp2 = new GradientPaint(0, 0, new Color(112, 141, 120), 0, 25, Color.WHITE, true);
    public HashMap<String, CompoundPainter<Object>> compoundPainter = new HashMap<String, CompoundPainter<Object>>();
    /**************************/
    public JXPanel desktop = null;
    public ProgLoader progLoader = null;

    public static boolean demoversion = false;
    public static boolean vollbetrieb = true;

    public static Vector<Vector<Object>> timerVec = new Vector<Vector<Object>>();
    public static Timer fangoTimer = null;
    public static boolean timerLaeuft = false;
    public static boolean timerInBearbeitung = false;

    public static java.util.Timer nachrichtenTimer = null;
    public static boolean nachrichtenLaeuft = false;
    public static boolean nachrichtenInBearbeitung = false;

    public static boolean updatesBereit = false;
    public static boolean updatesChecken = true;
    public static int toolsDlgRueckgabe = -1;

    public RehaIOServer rehaIOServer = null;
    public static int xport = 6000;
    public static boolean isStarted = false;
    public static int divider1 = -1;
    public static int divider2 = -1;

    public static int zugabex = 20;
    public static int zugabey = 20;

    public OcKVK ocKVK = null;

    public CaptureDeviceInfo device = null;
    public MediaLocator ml = null;
    public Player player = null;

    public static JComponent dragDropComponent = null;

    public int lastSelectedPat = -1;
    public String lastSelectedValue = "";
    public int lastSelectedFloskel = -1;

    public SqlInfo sqlInfo = null;
    public static int nachladenDB = 0;
    public static int dbLoadError = 1;

    public static String lastCommId = "";
    public static String lastCommAction = "";
    public boolean lastCommActionConfirmed = true;

    public RehaCommServer rehaCommServer = null;
    public static boolean phoneOk = false;

    public static Vector<Vector<String>> vRGAFoffen;
    public static boolean bRGAFoffen;
    public static boolean bHatMerkmale;

    public static Vector<Vector<List<String>>> terminLookup = new Vector<Vector<List<String>>>();

    private final static Mandant nullMandant = new Mandant("000000000", "Übungs-Mandant");
    private Mandant mandant;
    private static String aktIK = nullMandant.ik();
    private static String aktMandant = nullMandant.name();

    public static Reha instance = new Reha(nullMandant);
    static JXFrame thisFrame;

    static Logger logger;
    private LinkeTaskPane linkeTaskPane;
    ActionListener ltplistener = new LinkeTaskPaneListener(this);

    public static JXFrame getThisFrame() {
        return thisFrame;
    }

    public static void setThisFrame(JXFrame thisFrame) {
        Reha.thisFrame = thisFrame;
    }

    public static String getAktIK() {
        return aktIK;
    }

    public static String getAktMandant() {
        return aktMandant;
    }

    public Reha(Mandant mandant) {
        this.mandant = mandant;
        instance = this;

    }

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        new Logging("reha");
        initializeLogging();
        Mandant mainMandant;
        String[] parameter = args;
        if (parameter.length > 0) {
            String[] split = parameter[0].split("@");
            mainMandant = new Mandant(split[0], split[1]);
        } else {
            INIFile inif = new INIFile(Path.Instance.getProghome() + "ini/mandanten.ini");
            int DefaultMandant = inif.getIntegerProperty("TheraPiMandanten", "DefaultMandant");
            mainMandant = new Mandant(inif.getStringProperty("TheraPiMandanten", "MAND-IK" + DefaultMandant),
                    inif.getStringProperty("TheraPiMandanten", "MAND-NAME" + DefaultMandant));
        }
        new Reha(mainMandant).startWithMandantSet();

    }

    public static void initializeLogging() {

        logger = LoggerFactory.getLogger(Reha.class);
    }

    public void startWithMandantSet() {

        aktIK = mandant.ik();
        aktMandant = mandant.name();

        String iniPath = Path.Instance.getProghome() + "ini/" + mandant.ik() + "/";

        INITool.init(iniPath);
        logger.info("Insgesamt sind " + INITool.anzahlInisInDB() + " INI-Dateien in der Tabelle inidatei abgelegt");

        Titel2 = "  -->  [Mandant: " + getAktMandant() + "]";

        Thread rehasockeThread = new Thread(new RehaSockServer(), "RehaSocketServer");
        rehasockeThread.start();

        try {
            logger.info("Starte RehaxSwing.jar");
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-Djava.net.preferIPv4Stack=true", "-jar",
                    Path.Instance.getProghome() + "RehaxSwing.jar");

            processBuilder.inheritIO()
                          .start();
            logger.info("RehaxSwing beendet");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            rehasockeThread.join(10000);
        } catch (InterruptedException e2) {
            logger.error("rehasocketthread could not be joined");
        }
        try {
            new Thread() {
                @Override
                public void run() {

                    new SocketClient().setzeInitStand("System-Icons laden");
                    while (!Reha.DbOk) {
                        try {
                            Thread.sleep(25);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    ImageRepository.SystemIconsInit();
                    iconsOk = true;
                    new SocketClient().setzeInitStand("System-Config initialisieren");
                }
            }.start();
        } catch (java.lang.Exception e) {

            logger.error("caught unexplainable Exception", e);
        }

        /*********/

        SystemConfig sysConf = new SystemConfig();

        setSystemConfig(sysConf);

        sysConf.SystemStart(Path.Instance.getProghome());

        sysConf.HauptFenster();
        sysConf.openoffice();

        sysConf.DatenBank();
        sysConf.phoneservice();
        try {
            aktLookAndFeel = SystemConfig.getLookAndFeel();
            UIManager.setLookAndFeel(aktLookAndFeel);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }
        /***********************/
        Color c = UIManager.getColor("Button.disabledForeground");
        if (c != null) {
            UIManager.put("Button.disabledText", new Color(112, 126, 106)/* original = Color.BLACK */);
        } else {
            UIManager.put("Button.disabledText", new Color(112, 126, 106)/* original = Color.BLACK */);
            UIManager.put("Button.disabledForeground", new Color(112, 126, 106)/* original = Color.BLACK */);
        }
        UIManager.put("ComboBox.disabledForeground", Color.RED);

        /***********************/

        javax.swing.plaf.FontUIResource fontUIDresource = new FontUIResource("Tahoma", Font.PLAIN, 11);
        UIDefaults defs = (UIDefaults) UIManager.getLookAndFeelDefaults()
                                                .clone();
        for (Object key : defs.keySet()) {
            if ("FormattedTextField.font".equals(key)) {
                UIManager.put(key, fontUIDresource);
            }
            if ("TextField.font".equals(key)) {
                UIManager.put(key, fontUIDresource);
            }
            if ("Label.font".equals(key)) {
                UIManager.put(key, fontUIDresource);
            }
            if ("Button.font".equals(key)) {
                UIManager.put(key, fontUIDresource);
            }
            if ("Table.font".equals(key)) {
                UIManager.put(key, fontUIDresource);
            }
            if ("ComboBox.font".equals(key)) {
                UIManager.put(key, fontUIDresource);
            }
        }
        searchForUpdates();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {

                    instance.sqlInfo = new SqlInfo();
                    instance.sqlInfo.setDieseMaschine(SystemConfig.dieseMaschine);
                    rehaBackImg = new ImageIcon(Path.Instance.getProghome() + "icons/therapieMT1.gif");

                    RehaEventClass rehaEvent = new RehaEventClass();
                    rehaEvent.addRehaEventListener(instance);
                    new Thread(new DatenbankStarten()).start();
                    instance.getJFrame();

                    Reha.getThisFrame()
                        .setIconImage(Toolkit.getDefaultToolkit()
                                             .getImage(Path.Instance.getProghome() + "icons/Pi_1_0.png"));

                    Reha.instance.doCompoundPainter();
                    Reha.instance.starteTimer();
                    if (SystemConfig.timerdelay > 0) {
                        Reha.instance.starteNachrichtenTimer();
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Reha.instance.rehaIOServer = new RehaIOServer(6000);
                                System.out.println("RehaIOServer wurde initialisiert");
                                SystemConfig.AktiviereLog();
                                try {
                                    if (SystemConfig.activateSMS) {
                                        Reha.instance.rehaCommServer = new RehaCommServer(
                                                Integer.parseInt(SystemConfig.hmSMS.get("COMM")));
                                    }
                                } catch (NullPointerException ex) {
                                    Reha.instance.rehaCommServer = null;
                                }
                            } catch (NullPointerException ex) {
                                System.out.println("RehaCommServer = null");
                            }
                        }
                    });
                } catch (NullPointerException ex) {
                    logger.error("Fehler beim Systemstart", ex);
                }

            }
        });
    }

    public void setzeInitEnde() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws java.lang.Exception {
                new SocketClient().setzeInitStand("INITENDE");
                return null;
            }
        }.execute();
    }

    private void saveAktuelleFensterAnordnung() {
        try {
            INIFile inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/",
                    "rehajava.ini");
            SystemConfig.UpdateIni(inif, "HauptFenster", "Divider1", jSplitLR.getDividerLocation(), null);
            SystemConfig.UpdateIni(inif, "HauptFenster", "Divider2", jSplitRechtsOU.getDividerLocation(), null);
            SystemConfig.UpdateIni(inif, "HauptFenster", "TP1Offen",
                    linkeTaskPane.patientenstammPanel.isCollapsed() ? "1" : "0", null);
            SystemConfig.UpdateIni(inif, "HauptFenster", "TP2Offen",
                    linkeTaskPane.terminManagementPanel.isCollapsed() ? "1" : "0", null);
            SystemConfig.UpdateIni(inif, "HauptFenster", "TP3Offen",
                    linkeTaskPane.openofficePanel.isCollapsed() ? "1" : "0", null);
            SystemConfig.UpdateIni(inif, "HauptFenster", "TP4Offen",
                    linkeTaskPane.nuetzlichesPanel.isCollapsed() ? "1" : "0", null);
            SystemConfig.UpdateIni(inif, "HauptFenster", "TP5Offen",
                    linkeTaskPane.systemeinstellungpanel.isCollapsed() ? "1" : "0", null);
            SystemConfig.UpdateIni(inif, "HauptFenster", "TP6Offen",
                    linkeTaskPane.monatsuebersichtPanel.isCollapsed() ? "1" : "0", null);
            if (LinkeTaskPane.mitUserTask) {
                SystemConfig.UpdateIni(inif, "HauptFenster", "TP7Offen",
                        linkeTaskPane.userTaskPanel.isCollapsed() ? "1" : "0", null);
            }
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(null, "Fehler beim Speichern der aktuellen Fensteranordnung!");
        }
    }

    public void beendeSofort() {
        doCloseEverything();
        System.exit(0);
    }

    private void doCloseEverything() {
        this.jFrame.removeWindowListener(windowlistener);
        if (Reha.instance.conn != null) {
            try {
                Reha.instance.conn.close();
                System.out.println("Datenbankverbindung geschlossen");
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        if (Reha.barcodeScanner != null) {
            try {
                BarCodeScanner.serialPort.close();
                Reha.barcodeScanner = null;
                System.out.println("Serielle-Schnittstelle geschlossen");
            } catch (NullPointerException ex) {

            }
        }
        if (Reha.timerLaeuft) {
            Reha.fangoTimer.stop();
            Reha.timerLaeuft = false;
        }
        if (Reha.nachrichtenTimer != null) {
            Reha.nachrichtenTimer.cancel();
            Reha.nachrichtenLaeuft = false;
            Reha.nachrichtenTimer = null;
        }
        if (rehaIOServer != null) {
            try {
                rehaIOServer.serv.close();
                System.out.println("RehaIO-SocketServer geschlossen");
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        if (rehaCommServer != null) {
            try {
                rehaCommServer.serv.close();
                System.out.println("RehaComm-SocketServer geschlossen");
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        }
        if (SystemConfig.sReaderAktiv.equals("1") && Reha.instance.ocKVK != null) {
            try {
                System.out.println("Card-Terminal deaktiviert");
            } catch (NullPointerException ex) {

            }
        }
        saveAktuelleFensterAnordnung();

    }

    private void doCompoundPainter() {
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    CompoundPainter<Object> cp = null;
                    MattePainter mp = null;
                    LinearGradientPaint p = null;
                    /*****************/
                    Point2D start = new Point2D.Float(0, 0);
                    Point2D end = new Point2D.Float(960, 100);
                    float[] dist = { 0.0f, 0.75f };
                    Color[] colors = { Color.WHITE, Colors.PiOrange.alpha(0.25f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("PatNeuanlage", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(0, 100);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, new Color(231, 120, 23) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("SuchePanel", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(0, 15);// vorher 45
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Colors.PiOrange.alpha(0.5f), Color.WHITE };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("ButtonPanel", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(0, 40);
                    dist = new float[] { 0.0f, 1.00f };
                    colors = new Color[] { Colors.PiOrange.alpha(0.5f), Color.WHITE };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("StammDatenPanel", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(0, 100);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Colors.PiOrange.alpha(0.70f), Color.WHITE };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("AnredePanel", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(0, 150);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.PiOrange.alpha(0.5f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("HauptPanel", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(0, 150);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.PiOrange.alpha(0.5f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("FliessText", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(0, 150);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.PiOrange.alpha(0.5f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("getTabs", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(0, 450);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Colors.PiOrange.alpha(0.25f), Color.WHITE };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("getTabs2", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(350, 290);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.Yellow.alpha(0.05f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("RezeptGebuehren", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(400, 550);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.Gray.alpha(0.15f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("EBerichtPanel", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(600, 350);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.Yellow.alpha(0.25f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("ArztBericht", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(600, 750);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.Yellow.alpha(0.05f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("RezNeuanlage", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(300, 100);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.Gray.alpha(0.05f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("ScannerUtil", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(0, 400);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.TaskPaneBlau.alpha(0.45f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("ArztAuswahl", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(0, 400);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.Green.alpha(0.45f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("KassenAuswahl", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(900, 100);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.PiOrange.alpha(0.25f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("KVKRohDaten", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(600, 550);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.TaskPaneBlau.alpha(0.45f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("ArztPanel", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(400, 100);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.Blue.alpha(0.15f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("ArztNeuanlage", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(400, 100);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.Green.alpha(0.25f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("KasseNeuanlage", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(600, 550);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.Green.alpha(0.5f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("KassenPanel", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(200, 120);
                    dist = new float[] { 0.0f, 0.5f };
                    colors = new Color[] { Colors.TaskPaneBlau.alpha(1.0f), Color.WHITE };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("SuchenSeite", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(300, 270);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.Gray.alpha(0.15f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("GutachtenWahl", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(900, 100);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Color.WHITE, Colors.Yellow.alpha(0.05f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("VorBerichte", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(0, 600);
                    dist = new float[] { 0.0f, 0.75f };
                    colors = new Color[] { Colors.Yellow.alpha(0.15f), Color.WHITE };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("TextBlock", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(200, 120);
                    dist = new float[] { 0.0f, 0.5f };
                    colors = new Color[] { Colors.TaskPaneBlau.alpha(1.0f), Color.WHITE };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("TagWahlNeu", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(390, 180);
                    dist = new float[] { 0.0f, 0.5f };
                    colors = new Color[] { Colors.TaskPaneBlau.alpha(1.0f), Color.WHITE };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("Zeitfenster", cp);
                    /*****************/
                    start = new Point2D.Float(0, 0);
                    end = new Point2D.Float(400, 500);
                    dist = new float[] { 0.0f, 0.5f };
                    colors = new Color[] { Color.WHITE, Colors.Gray.alpha(0.15f) };
                    p = new LinearGradientPaint(start, end, dist, colors);
                    mp = new MattePainter(p);
                    cp = new CompoundPainter<Object>(mp);
                    Reha.instance.compoundPainter.put("SystemInit", cp);

                    /*****************/
                    progLoader = new ProgLoader();

                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
                return null;
            }

        }.execute();

    }

    /***************************************/
    private void starteNachrichtenTimer() {
        Reha.nachrichtenTimer = new java.util.Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!nachrichtenInBearbeitung) {
                    // nur wenn das Nachrichtentool nich läuft
                    if (!RehaIOServer.rehaMailIsActive) {
                        nachrichtenInBearbeitung = true;
                        /**************/
                        if ((!Reha.aktUser.equals("")) && (checkForMails()) && (Reha.officeapplication != null)) {
                            nachrichtenRegeln();
                        }
                        /*************/
                    }
                    nachrichtenInBearbeitung = false;
                }
            }
        };
        // start des Timers:
        Reha.nachrichtenTimer.scheduleAtFixedRate(task, SystemConfig.timerdelay, SystemConfig.timerdelay);
    }

    public static void nachrichtenRegeln() {
        // System.out.println(Reha.aktUser);
        boolean newmail = checkForMails();
        if ((!Reha.aktUser.trim()
                          .startsWith("Therapeut"))
                && RehaIOServer.rehaMailIsActive && newmail) {
            new ReverseSocket().setzeRehaNachricht(RehaIOServer.rehaMailreversePort,
                    "Reha#" + RehaIOMessages.MUST_CHANGEUSER + "#" + Reha.aktUser);
            new ReverseSocket().setzeRehaNachricht(RehaIOServer.rehaMailreversePort,
                    "Reha#" + RehaIOMessages.MUST_GOTOFRONT);
        } else if ((!Reha.aktUser.trim()
                                 .startsWith("Therapeut"))
                && RehaIOServer.rehaMailIsActive && (!newmail)) {
            new ReverseSocket().setzeRehaNachricht(RehaIOServer.rehaMailreversePort,
                    "Reha#" + RehaIOMessages.MUST_CHANGEUSER + "#" + Reha.aktUser);
        } else {
            if ((!Reha.aktUser.trim()
                              .startsWith("Therapeut"))
                    && Reha.checkForMails()) {
                if (Reha.isStarted) {
                    new LadeProg(Path.Instance.getProghome() + "RehaMail.jar" + " " + Path.Instance.getProghome() + " "
                            + Reha.getAktIK() + " " + Reha.xport + " " + Reha.aktUser.replace(" ", "#"));
                }
            }
        }
    }

    public static boolean checkForMails() {
        if (!SqlInfo.holeEinzelFeld(
                "select gelesen from pimail where empfaenger_person ='" + Reha.aktUser + "' and gelesen='F' LIMIT 1")
                    .trim()
                    .equals("")) {
            return true;
        }
        return false;
    }

    /***************************************/
    public void aktiviereNaechsten(int welchen) {
        JInternalFrame[] frame = desktops[welchen].getAllFrames();
        if (frame.length > 0) {
            for (int i = 0; i < frame.length; i++) {
                //// System.out.println("InternalFrames übrig = "+frame[i].getTitle());
                ((JRehaInternal) frame[i]).toFront();
                ((JRehaInternal) frame[i]).setActive(true);
                ((JRehaInternal) frame[i]).getContent()
                                          .requestFocus();
                if (i == 0) {
                    break;
                }
            }
        } else {
            if (welchen == 0) {
                frame = desktops[1].getAllFrames();
                for (int i = 0; i < frame.length; i++) {
                    ((JRehaInternal) frame[i]).toFront();
                    ((JRehaInternal) frame[i]).setActive(true);
                    ((JRehaInternal) frame[i]).getContent()
                                              .requestFocus();
                    Reha.containerHandling(1);
                    if (i == 0) {
                        break;
                    }
                }
            } else {
                frame = desktops[0].getAllFrames();
                for (int i = 0; i < frame.length; i++) {
                    ((JRehaInternal) frame[i]).toFront();
                    ((JRehaInternal) frame[i]).setActive(true);
                    ((JRehaInternal) frame[i]).getContent()
                                              .requestFocus();
                    Reha.containerHandling(0);
                    if (i == 0) {
                        break;
                    }
                }
            }
        }

    }

    private JXFrame getJFrame() {
        if (jFrame == null) {
            jFrame = new RehaFrame(Titel, Titel2);

            sqlInfo.setFrame(jFrame);
            // thisClass = this;
            jFrame.addWindowListener(windowlistener);
            jFrame.addComponentListener(componentListener);

            JDesktopPane desk = new JDesktopPane();
            desk.setName("desk");
            desk.setOpaque(false);
            jFrame.setContentPane(desk);

            /*******/

            jFrame.setJMenuBar(getJJMenuBar());
            jFrame.setStatusBar(getJXStatusBar());
            shiftLabel.setText("Prog-User ok!");
            Reha.RehaPainter[0] = RehaPainters.getBlauPainter();
            Reha.RehaPainter[1] = RehaPainters.getSchwarzGradientPainter();
            Reha.RehaPainter[2] = RehaPainters.getBlauGradientPainter();

            /**
             * Zuerste die Panels für die linke und rechte Seite erstellen, dann die
             * Splitpane generieren und die Panels L+R übergeben
             *
             */
            jxLinks = new JXPanel(new BorderLayout());
            jxLinks.setDoubleBuffered(true);
            jxLinks.setName("LinkesGrundpanel");
            jxLinks.setBorder(null);
            jxLinks.setBackground(Color.WHITE);

            jxRechts = new JXPanel(new BorderLayout());
            jxRechts.setDoubleBuffered(true);
            jxRechts.setName("RechtesGrundpanel");
            jxRechts.setBackground(Color.WHITE);
            jxRechts.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));

            jSplitLR = UIFSplitPane.createStrippedSplitPane(JSplitPane.HORIZONTAL_SPLIT, jxRechts, jxLinks);
            jSplitLR.setBackground(Color.WHITE);
            jSplitLR.setDividerSize(7);
            jSplitLR.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    dividerLocLR = jSplitLR.getDividerLocation();
                }
            });

            jSplitLR.setDividerBorderVisible(false);
            jSplitLR.setName("GrundSplitLinksRechts");
            jSplitLR.setOneTouchExpandable(true);
            jSplitLR.setDividerLocation(Toolkit.getDefaultToolkit()
                                               .getScreenSize().width
                    - 250);
            ((BasicSplitPaneUI) jSplitLR.getUI()).getDivider()
                                                 .setBackground(Color.WHITE);

            desktop = new JXPanel(new BorderLayout());
            desktop.add(jSplitLR, BorderLayout.CENTER);
            desktop.setSize(2500, 2500);

            jFrame.getContentPane()
                  .add(desktop);
            jFrame.getContentPane()
                  .addComponentListener(componentListener);

            /********* den BackgroundPainter basteln *********/
            Point2D start = new Point2D.Float(0, 0);
            Point2D end = new Point2D.Float(800, 500);
            float[] dist = { 0.2f, 0.7f, 1.0f };
            Color[] colors = { Colors.TaskPaneBlau.alpha(1.0f), Color.WHITE, Colors.TaskPaneBlau.alpha(1.0f) };
            LinearGradientPaint p = new LinearGradientPaint(start, end, dist, colors);
            MattePainter mp = new MattePainter(p);

            DropShadowBorder dropShadow = new DropShadowBorder(Color.BLACK, 10, 1, 5, false, true, true, true);

            /**
             * Jetzt die Panels für die rechte Seite oben und unten erstellen, dann die
             * Splitpane generieren und die Panels O+U übergeben.
             */
            jxRechtsOben = new JXPanel(new BorderLayout());
            jxRechtsOben.setDoubleBuffered(true);
            jxRechtsOben.setPreferredSize(new Dimension(0, 250));
            jxRechtsOben.setName("RechtsOben");
            jxRechtsOben.setBorder(null);
            jxRechtsOben.setBackground(Color.WHITE);
            JXPanel jp2 = new JXPanel(new BorderLayout());
            jp2.setBackground(Color.WHITE);
            jp2.setBorder(dropShadow);
            // ***

            jpOben = new JXPanel(new BorderLayout());
            jpOben.setBorder(null);
            jpOben.setBackgroundPainter(new CompoundPainter(mp));
            jpOben.setName("PanelOben");
            jpOben.addComponentListener(componentListener);

            desktops[0] = new Hintergrund(Reha.rehaBackImg);
            desktops[0].setName("DesktopOben");
            desktops[0].setOpaque(false);
            desktops[0].setSize(2000, 2000);
            desktops[0].setDesktopManager(new OOODesktopManager(0));
            desktops[0].addComponentListener(componentListener);

            jpOben.add(desktops[0]);

            jp2.add(jpOben, BorderLayout.CENTER);
            jxRechtsOben.add(jp2, BorderLayout.CENTER);
            jxRechtsOben.validate();
            jxRechtsOben.updateUI();

            /*********************/
            jxRechtsUnten = new JXPanel(new BorderLayout());
            jxRechtsUnten.setDoubleBuffered(true);
            jxRechtsUnten.setPreferredSize(new Dimension(0, 250));
            jxRechtsUnten.setName("RechtsUnten");
            jxRechtsUnten.setBorder(null);
            jxRechtsUnten.setBackground(Color.WHITE);

            jp2 = new JXPanel(new BorderLayout());
            jp2.setBackground(Color.WHITE);
            jp2.setBorder(dropShadow);
            jp2.addComponentListener(componentListener);

            jpUnten = new JXPanel(new BorderLayout());
            jpUnten.setBorder(null);
            jpUnten.setBackgroundPainter(new CompoundPainter(mp));
            jpUnten.setName("PanelUnten");
            jpUnten.addComponentListener(componentListener);

            desktops[1] = new Hintergrund(Reha.rehaBackImg);
            desktops[1].setName("DesktopUnten");
            desktops[1].setOpaque(false);
            desktops[1].setSize(2000, 2000);
            desktops[1].setDesktopManager(new OOODesktopManager(1));
            desktops[1].addComponentListener(componentListener);

            jpUnten.add(desktops[1]);
            jp2.add(jpUnten, BorderLayout.CENTER);
            jxRechtsUnten.add(jp2, BorderLayout.CENTER);
            jxRechtsUnten.validate();
            jxRechtsUnten.updateUI();
            /********************************/

            if (SystemConfig.desktopHorizontal) {
                jSplitRechtsOU = UIFSplitPane.createStrippedSplitPane(JSplitPane.VERTICAL_SPLIT, jxRechtsOben,
                        jxRechtsUnten);
            } else {
                jSplitRechtsOU = UIFSplitPane.createStrippedSplitPane(JSplitPane.HORIZONTAL_SPLIT, jxRechtsOben,
                        jxRechtsUnten);

            }

            jSplitRechtsOU.setDividerBorderVisible(false);
            jSplitRechtsOU.setDividerSize(7);
            ((BasicSplitPaneUI) jSplitRechtsOU.getUI()).getDivider()
                                                       .setBackground(Color.WHITE);

            jSplitRechtsOU.setBackground(Color.WHITE);
            jSplitRechtsOU.setName("RechtsSplitObenUnten");
            jSplitRechtsOU.setOneTouchExpandable(true);
            jxRechts.add(jSplitRechtsOU, BorderLayout.CENTER); // bislang o.k.

            jxRechts.addComponentListener(componentListener);
            jxRechts.validate();

            /**
             * Jetzt erstellen wir die TaskPanes der linken Seite
             */
            while ((!Reha.iconsOk)) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            linkeTaskPane = new LinkeTaskPane(conn, ltplistener);
            jxLinks.add(linkeTaskPane, BorderLayout.CENTER);
            jxLinks.validate();
            jFrame.getContentPane()
                  .validate();

        }

        setThisFrame(jFrame);

        jxLinks.setAlpha(1.0f);
        jxRechts.setAlpha(1.0f);

        jFrame.setExtendedState(JXFrame.MAXIMIZED_BOTH);

        setKeyboardActions();
        AktiveFenster.Init();

        return jFrame;
    }

    private void searchForUpdates() {
        Thread updater = new Thread("Udpater") {
            @Override
            public void run() {
                UpdatesMain.main(new String[0]);
            }
        };
        updater.start();

    }

    public static void setSystemConfig(SystemConfig sysConf) {
        Reha.sysConf = sysConf;
    }

    private JXStatusBar getJXStatusBar() {
        if (jXStatusBar == null) {
            UIManager.put("Separator.foreground", new Color(231, 120, 23));

            jXStatusBar = new JXStatusBar();

            jXStatusBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
            jXStatusBar.putClientProperty(Options.NO_CONTENT_BORDER_KEY, Boolean.TRUE);
            jXStatusBar.putClientProperty(Options.HI_RES_GRAY_FILTER_ENABLED_KEY, Boolean.FALSE);

            jXStatusBar.setPreferredSize(new Dimension(1280, 30));
            jXStatusBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            jXStatusBar.setLayout(new BorderLayout());

            FormLayout sblay = new FormLayout(
                    "10dlu,fill:0:grow(0.16),4dlu,fill:0:grow(0.16),4dlu,fill:0:grow(0.16),4dlu,fill:0:grow(0.16),4dlu,fill:0:grow(0.16),4dlu,fill:0:grow(0.16),10dlu",
                    "fill:0:grow(0.5),18px,fill:0:grow(0.5)");
            CellConstraints sbcc = new CellConstraints();
            JXPanel sbkomplett = new JXPanel();
            sbkomplett.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
            sbkomplett.setOpaque(false);
            sbkomplett.setLayout(sblay);

            /************* 1 Container *****************************/
            JXPanel bar = new JXPanel(new BorderLayout());
            bar.setOpaque(false);
            bar.setBorder(BorderFactory.createLoweredBevelBorder());
            JXPanel versionbar = new JXPanel(new BorderLayout());
            versionbar.setOpaque(false);
            versionbar.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
            dbLabel = new JLabel(" ");
            dbLabel.setVerticalAlignment(JLabel.CENTER);
            dbLabel.setHorizontalAlignment(JLabel.LEFT);
            versionbar.add(dbLabel);
            bar.add(versionbar);
            sbkomplett.add(bar, sbcc.xy(2, 2));

            /************* 2 Container *****************************/

            FlowLayout flay = new FlowLayout(FlowLayout.LEFT);
            flay.setVgap(1);
            jxPinContainer = new JXPanel(flay);
            jxPinContainer.setBorder(null);
            jxPinContainer.setOpaque(false);
            bar = new JXPanel(new BorderLayout());
            bar.setOpaque(false);
            bar.setBorder(BorderFactory.createLoweredBevelBorder());
            JXPanel bar2 = new JXPanel(new BorderLayout());
            bar2.setOpaque(false);
            bar2.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
            messageLabel = new JLabel("starte OpenOffice.org");
            messageLabel.setForeground(Color.RED);
            messageLabel.setVerticalAlignment(SwingConstants.CENTER);
            messageLabel.setHorizontalAlignment(SwingConstants.LEFT);
            jxPinContainer.add(messageLabel);
            bar2.add(jxPinContainer);
            bar.add(bar2);
            sbkomplett.add(bar, sbcc.xy(4, 2));

            /************** 3 Container ****************************/

            bar = new JXPanel(new BorderLayout());
            bar.setOpaque(false);
            bar.setBorder(BorderFactory.createLoweredBevelBorder());
            bar2 = new JXPanel(new BorderLayout());
            bar2.setOpaque(false);
            bar2.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
            mousePositionLabel = new javax.swing.JLabel("Druckliste = leer");
            mousePositionLabel.setHorizontalAlignment(SwingConstants.LEFT);
            mousePositionLabel.setVerticalAlignment(SwingConstants.CENTER);
            bar2.add(mousePositionLabel);
            bar.add(bar2);
            sbkomplett.add(bar, sbcc.xy(6, 2));

            /************** 4 Container ****************************/

            bar = new JXPanel(new BorderLayout());
            bar.setOpaque(false);
            bar.setBorder(BorderFactory.createLoweredBevelBorder());
            bar2 = new JXPanel(new BorderLayout());
            bar2.setOpaque(false);
            bar2.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
            Rehaprogress = new JProgressBar();
            Rehaprogress.setOpaque(false);
            Rehaprogress.setIndeterminate(true);
            Rehaprogress.setForeground(Color.RED);
            Rehaprogress.setBorder(null);
            Rehaprogress.setBorderPainted(false);

            bar2.add(Rehaprogress);
            bar.add(bar2);
            sbkomplett.add(bar, sbcc.xy(8, 2));

            /*************** 5 Container ***************************/

            bar = new JXPanel(new BorderLayout());
            bar.setOpaque(false);
            bar.setBorder(BorderFactory.createLoweredBevelBorder());
            bar2 = new JXPanel(new BorderLayout());
            bar2.setOpaque(false);
            bar2.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

            shiftLabel = new JLabel("Standard User");
            shiftLabel.setForeground(Color.RED);
            shiftLabel.setVerticalAlignment(SwingConstants.CENTER);
            shiftLabel.setHorizontalAlignment(SwingConstants.LEFT);
            shiftLabel.setForeground(Color.RED);
            bar2.add(shiftLabel, BorderLayout.WEST);
            bar.add(bar2);
            sbkomplett.add(bar, sbcc.xy(10, 2));

            /******************************************/

            bar = new JXPanel(new BorderLayout());
            bar.setOpaque(false);
            bar.setBorder(BorderFactory.createLoweredBevelBorder());
            bar2 = new JXPanel(new BorderLayout());
            bar2.setOpaque(false);
            bar2.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
            copyLabel = new JLabel("");
            copyLabel.setHorizontalAlignment(SwingConstants.LEFT);
            copyLabel.setVerticalAlignment(SwingConstants.CENTER);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    while (!iconsOk) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    copyLabel.setIcon(SystemConfig.hmSysIcons.get("bunker"));
                    return null;
                }
            }.execute();
            DropTarget dndt = new DropTarget();
            DropTargetListener dropTargetListener = new DropTargetAdapter() {

                @Override
                public void drop(DropTargetDropEvent e) {
                    String mitgebracht = "";
                    try {
                        Transferable tr = e.getTransferable();

                        if (Path.Instance.isLinux()) {
                            if (Reha.dragDropComponent instanceof JRtaTextField) {
                                mitgebracht = ((JRtaTextField) Reha.dragDropComponent).getText();
                            }
                        } else {
                            DataFlavor[] flavors = tr.getTransferDataFlavors();
                            for (int i = 0; i < flavors.length; i++) {
                                mitgebracht = String.valueOf(tr.getTransferData(flavors[i])
                                                               .toString());
                            }
                        }

                        if (mitgebracht.indexOf("°") >= 0) {
                            String[] labs = mitgebracht.split("°");
                            if (labs[0].contains("TERMDAT")) {
                                copyLabel.setText(labs[1] + "°" + labs[2] + "°" + labs[3]);
                                bunker.setText("TERMDATEXT°" + copyLabel.getText());
                                e.dropComplete(true);
                                return;
                            } else if (labs[0].contains("PATDAT")) {
                                copyLabel.setText("");
                                bunker.setText("");
                                e.dropComplete(true);
                            } else {
                                copyLabel.setText("");
                                bunker.setText("");
                                e.dropComplete(true);
                                return;
                            }
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                    e.dropComplete(true);
                }
            };
            try {
                dndt.addDropTargetListener(dropTargetListener);
            } catch (TooManyListenersException e1) {
                e1.printStackTrace();
            }
            copyLabel.setDropTarget(dndt);

            final String propertyName = "text";
            bunker = new JLabel();
            bunker.setName("bunker");
            bunker.setTransferHandler(new TransferHandler(propertyName));
            copyLabel.setTransferHandler(new TransferHandler(propertyName));
            copyLabel.setName("copyLabel");
            /*********************/
            if (Path.Instance.isLinux()) {
                DragGestureListener dragGestureListener = new DragGestureListener() {
                    @Override
                    public void dragGestureRecognized(DragGestureEvent e) {
                        StringSelection selection = new StringSelection(copyLabel.getText());
                        Reha.dragDropComponent = bunker;
                        if (!Rechte.hatRecht(Rechte.Kalender_termindragdrop, false)) {
                            return;
                        }
                        JComponent comp = copyLabel;
                        if (((JLabel) comp).getText()
                                           .equals("")) {
                            return;
                        }
                        if (bunker.getText()
                                  .startsWith("TERMDAT")) {
                            TerminFenster.setDragMode(0);
                        }
                        e.startDrag(null, selection, null);
                    }
                };

                DragSource dragSource = new DragSource();

                dragSource.createDefaultDragGestureRecognizer(copyLabel, DnDConstants.ACTION_COPY, dragGestureListener);
            }

            copyLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent evt) {
                    if (!Rechte.hatRecht(Rechte.Kalender_termindragdrop, false)) {
                        return;
                    }
                    JComponent comp = (JComponent) evt.getSource();
                    if (((JLabel) comp).getText()
                                       .equals("")) {
                        return;
                    }
                    if (bunker.getText()
                              .startsWith("TERMDAT")) {
                        TerminFenster.setDragMode(0);
                    }
                    TransferHandler th = bunker.getTransferHandler();
                    th.exportAsDrag(bunker, evt, TransferHandler.COPY);
                }
            });
            bar2.add(copyLabel);
            bar.add(bar2);
            sbkomplett.add(bar, sbcc.xy(12, 2));
            sbkomplett.validate();
            jXStatusBar.add(sbkomplett, BorderLayout.CENTER);
            jXStatusBar.validate();
            jXStatusBar.setVisible(true);

        }
        return jXStatusBar;
    }

    public void progressStarten(boolean starten) {
        final boolean xstarten = starten;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                new Thread() {
                    @Override
                    public void run() {
                        Rehaprogress.setIndeterminate(xstarten);
                    }
                }.start();
                return null;
            }
        }.execute();
    }

    private JMenuBar getJJMenuBar() {

        if (jJMenuBar == null) {
            jJMenuBar = new JMenuBar();
            jJMenuBar.setFont(new Font("Dialog", Font.PLAIN, 12));
            jJMenuBar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
            jJMenuBar.add(getFileMenu());
            jJMenuBar.add(getstammMenu());
            jJMenuBar.add(new TerminMenu().getJMenu());
            jJMenuBar.add(getabrechnungMenu());
            jJMenuBar.add(geturlaubMenu());
            jJMenuBar.add(getverkaufMenu());
            jJMenuBar.add(getstatistikMenu());
            jJMenuBar.add(getbueroMenu());
            jJMenuBar.add(gettoolsMenu());
            jJMenuBar.add(getHelpMenu());
        }
        return jJMenuBar;
    }

    private JMenu getFileMenu() {
        if (fileMenu == null) {
            fileMenu = new JMenu();
            fileMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
            fileMenu.setText("Datei");
            fileMenu.add(getExitMenuItem());
        }
        return fileMenu;
    }

    private JMenu getstammMenu() {
        if (stammMenu == null) {
            stammMenu = new JMenu();
            stammMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
            stammMenu.setText("Stammdaten");
            JMenuItem men = new JMenuItem("Patienten Rezepte etc.");
            men.setActionCommand("patient");
            men.setAccelerator(KeyStroke.getKeyStroke(VK_P, Event.CTRL_MASK, false));
            men.setMnemonic(VK_P);
            men.addActionListener(actionListener);
            stammMenu.add(men);
            stammMenu.addSeparator();
            men = new JMenuItem("Krankenkassen");
            men.setActionCommand("kasse");
            men.setAccelerator(KeyStroke.getKeyStroke(VK_K, Event.CTRL_MASK, false));
            men.setMnemonic(VK_K);
            men.addActionListener(actionListener);
            stammMenu.add(men);
            stammMenu.addSeparator();
            men = new JMenuItem("Ärzte");
            men.setActionCommand("arzt");
            men.setAccelerator(KeyStroke.getKeyStroke(VK_A, Event.CTRL_MASK, false));
            men.setMnemonic(VK_A);
            men.addActionListener(actionListener);
            stammMenu.add(men);

        }
        return stammMenu;
    }

    private JMenu getabrechnungMenu() {
        if (abrechnungMenu == null) {
            abrechnungMenu = new JMenu();
            abrechnungMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
            abrechnungMenu.setText("Abrechnung");
            JMenuItem men = new JMenuItem("Heilmittel-Abrechnung nach §302 SGB V");
            men.setActionCommand("hmabrechnung");
            men.addActionListener(actionListener);
            abrechnungMenu.add(men);
            abrechnungMenu.addSeparator();
            men = new JMenuItem("Reha-Abrechnung");
            men.setActionCommand("rehaabrechnung");
            men.addActionListener(actionListener);
            abrechnungMenu.add(men);
            abrechnungMenu.addSeparator();
            men = new JMenuItem("Barkasse abrechnen");
            men.setActionCommand("barkasse");
            men.addActionListener(actionListener);
            abrechnungMenu.add(men);
            abrechnungMenu.addSeparator();
            men = new JMenuItem("Anmeldezahlen ermitteln");
            men.setActionCommand("anmeldezahlen");
            men.addActionListener(actionListener);
            abrechnungMenu.add(men);
            men = new JMenuItem("Tagesumsätze ermitteln");
            men.setActionCommand("tagesumsatz");
            men.addActionListener(actionListener);
            abrechnungMenu.add(men);
            abrechnungMenu.addSeparator();
            men = new JMenuItem("Offene Posten / Mahnwesen");
            men.setActionCommand("offeneposten");
            men.addActionListener(actionListener);
            abrechnungMenu.add(men);
            abrechnungMenu.addSeparator();
            men = new JMenuItem("OP Rezeptgebühr-/Ausfall-/Verkaufsrechnung");
            men.setActionCommand("rgaffaktura");
            men.addActionListener(actionListener);
            abrechnungMenu.add(men);
        }
        return abrechnungMenu;
    }

    private JMenu getstatistikMenu() {
        if (statistikMenu == null) {
            statistikMenu = new JMenu();
            statistikMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
            statistikMenu.setText("Statistiken");
            JMenuItem men = new JMenuItem("LVA/BfA Statistik");
            men.setActionCommand("lvastatistik");
            men.addActionListener(actionListener);
            statistikMenu.add(men);
        }
        return statistikMenu;
    }

    private JMenu getbueroMenu() {
        if (bueroMenu == null) {
            bueroMenu = new JMenu();
            bueroMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
            bueroMenu.setText("Büroprogramme");
        }
        return bueroMenu;
    }

    private JMenu gettoolsMenu() {
        if (toolsMenu == null) {
            toolsMenu = new JMenu();
            toolsMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
            toolsMenu.setText("Tools");
            JMenuItem men = new JMenuItem("Rezeptnummer suchen");
            men.setActionCommand("rezeptfahnder");
            men.addActionListener(actionListener);
            toolsMenu.add(men);
            toolsMenu.addSeparator();
            men = new JMenuItem("Kassenbuch erstellen");
            men.setActionCommand("kassenbuch");
            men.addActionListener(actionListener);
            toolsMenu.add(men);
            toolsMenu.addSeparator();
            men = new JMenuItem("Geburtstagsbriefe erstellen");
            men.setActionCommand("geburtstagsbriefe");
            men.addActionListener(actionListener);
            toolsMenu.add(men);
            toolsMenu.addSeparator();
            men = new JMenuItem("Sql-Modul");
            men.setActionCommand("sqlmodul");
            men.addActionListener(actionListener);
            toolsMenu.add(men);
            /*
             * men = new JMenuItem("INI-Editor"); men.setActionCommand("iniedit");
             * men.addActionListener(this); toolsMenu.add(men); toolsMenu.addSeparator();
             */
            men = new JMenuItem("§301 Reha Fall-Steuerung");
            men.setActionCommand("fallsteuerung");
            men.addActionListener(actionListener);
            toolsMenu.add(men);
            toolsMenu.addSeparator();
            men = new JMenuItem("Work-Flow Manager");
            men.setActionCommand("workflow");
            men.addActionListener(actionListener);
            toolsMenu.add(men);
            toolsMenu.addSeparator();
            men = new JMenuItem("Heilmittelrichtlinien-Tool");
            men.setActionCommand("hmrsearch");
            men.addActionListener(actionListener);
            toolsMenu.add(men);
            men = new JMenuItem("Thera-Pi OCR-Modul");
            men.setActionCommand("ocr");
            men.addActionListener(actionListener);
            toolsMenu.add(men);

        }
        return toolsMenu;
    }

    private JMenu getverkaufMenu() {
        if (verkaufMenu == null) {
            verkaufMenu = new JMenu();
            verkaufMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
            verkaufMenu.setText("Verkauf");
            JMenuItem men = new JMenuItem("Verkaufsmodul starten");
            men.setActionCommand("verkauf");
            men.addActionListener(actionListener);
            verkaufMenu.add(men);
        }
        return verkaufMenu;
    }

    private JMenu geturlaubMenu() {
        if (urlaubMenu == null) {
            urlaubMenu = new JMenu();
            urlaubMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
            urlaubMenu.setText("Urlaub/Überstunden");
            JMenuItem men = new JMenuItem("Urlaub-/Überstunden verwalten");
            men.setActionCommand("urlaub");
            men.addActionListener(actionListener);
            urlaubMenu.add(men);
            urlaubMenu.addSeparator();
            men = new JMenuItem("Umsatzbeteiligung ermitteln");
            men.setActionCommand("umsatzbeteiligung");
            men.addActionListener(actionListener);
            urlaubMenu.add(men);
        }
        return urlaubMenu;
    }

    private JMenu getHelpMenu() {
        if (helpMenu == null) {
            helpMenu = new JMenu();
            helpMenu.setFont(new Font("Dialog", Font.PLAIN, 12));
            helpMenu.setText("Hilfe");
            helpMenu.add(getF2RescueMenuItem());
            helpMenu.addSeparator();
            helpMenu.add(getAboutMenuItem());
        }
        return helpMenu;
    }

    private JMenuItem getExitMenuItem() {
        if (exitMenuItem == null) {
            exitMenuItem = new JMenuItem();
            exitMenuItem.setText("Thera-Pi beenden");
            exitMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    askCloseOrRestart();
                }
            });
        }
        return exitMenuItem;
    }

    protected void askCloseOrRestart() {
        Runtime r = Runtime.getRuntime();
        r.gc();
        switch (JOptionPane.showOptionDialog(null, "thera-\u03C0 wirklich schließen?", "Bitte bestätigen",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new String[] { "Ja", "Nein", "Restart" }, "Ja")) {
        case JOptionPane.YES_OPTION: // schließen
            if (Reha.DbOk && (Reha.instance.conn != null)) {
                Date zeit = new Date();
                String stx = "Insert into eingeloggt set comp='" + SystemConfig.dieseMaschine + "', zeit='"
                        + zeit.toString() + "', einaus='aus'";
                SqlInfo.sqlAusfuehren(stx);
            }
            beendeSofort();
            break;
        case JOptionPane.CANCEL_OPTION: // restart
            doCloseEverything();
            try {
                Runtime.getRuntime()
                       .exec("java -jar " + Path.Instance.getProghome() + "TheraPi.jar"); // restart einleiten
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
            break;
        }
    }

    private JMenuItem getAboutMenuItem() {
        if (aboutMenuItem == null) {
            aboutMenuItem = new JMenuItem();
            aboutMenuItem.setText("über Thera-\u03C0");
            aboutMenuItem.setActionCommand("ueberTheraPi");
            aboutMenuItem.addActionListener(actionListener);
        }
        return aboutMenuItem;
    }

    private JMenuItem getF2RescueMenuItem() {
        if (aboutF2RescueMenuItem == null) {
            aboutF2RescueMenuItem = new JMenuItem();
            aboutF2RescueMenuItem.setText("F2 - Rettungsanker");
            aboutF2RescueMenuItem.setActionCommand("f2Rescue");
            aboutF2RescueMenuItem.addActionListener(actionListener);
        }
        return aboutF2RescueMenuItem;
    }

    public void setzeUi(String sUI, JScrollPane panel) {
        try {
            SystemConfig.UpdateIni("rehajava.ini", "HauptFenster", "LookAndFeel", sUI);
            UIManager.setLookAndFeel((aktLookAndFeel = sUI));
            SwingUtilities.updateComponentTreeUI(getThisFrame());
            SwingUtilities.updateComponentTreeUI(this.jxRechtsOben);
            SwingUtilities.updateComponentTreeUI(this.jxRechtsUnten);
            SwingUtilities.updateComponentTreeUI(this.jSplitLR);
            SwingUtilities.updateComponentTreeUI(this.jxLinks);
            SwingUtilities.updateComponentTreeUI(this.jxRechts);
            linkeTaskPane.UpdateUI();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }
    }

    public static void starteOfficeApplication() {
        try {
            new OOService().start(SystemConfig.OpenOfficeNativePfad, SystemConfig.OpenOfficePfad);

            officeapplication = new OOService().getOfficeapplication();
            Reha.instance.Rehaprogress.setIndeterminate(false);
        } catch (OfficeApplicationException | FileNotFoundException e) {
            e.printStackTrace();
            Reha.instance.messageLabel = new JLabel("OO.org nicht verfügbar!!!");
        }

    }

    private void setKeyboardActions() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof KeyEvent) {
                    KeyEvent keyEvent = (KeyEvent) event;
                    if (progRechte.equals("")) {
                        return;
                    }
                    if (keyEvent.isAltDown() && keyEvent.getID() == KeyEvent.KEY_PRESSED
                            && keyEvent.getKeyCode() == VK_X) {
                    }
                    if (keyEvent.isControlDown() && keyEvent.getID() == KeyEvent.KEY_PRESSED
                            && keyEvent.getKeyCode() == VK_P) {
                        new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                Reha.getThisFrame()
                                    .setCursor(Cursors.wartenCursor);
                                Reha.getThisFrame()
                                    .setCursor(Cursors.normalCursor);
                                return null;
                            }
                        }.execute();
                    }
                    if (keyEvent.isAltDown() && keyEvent.getID() == KEY_PRESSED && keyEvent.getKeyCode() == VK_R) {
                        new RezeptFahnder(true, conn);
                        return;

                    }
                    if (keyEvent.isAltDown() && keyEvent.getID() == KEY_PRESSED && keyEvent.getKeyCode() == VK_X) {
                        Reha.aktUser = "";
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (RehaIOServer.rehaMailIsActive) {
                                    new ReverseSocket().setzeRehaNachricht(RehaIOServer.rehaMailreversePort,
                                            "Reha#" + RehaIOMessages.MUST_RESET);
                                }
                            }
                        });
                        ProgLoader.PasswortDialog();
                    }

                    if (keyEvent.isControlDown() && keyEvent.getID() == KEY_PRESSED && keyEvent.getKeyCode() == VK_T) {
                        JComponent termin = AktiveFenster.getFensterAlle("TerminFenster");
                        if (termin == null) {
                            //
                        } else {
                            // ProgLoader.ProgTerminFenster(0,0);//
                        }
                    }
                    if (keyEvent.isControlDown() && keyEvent.getID() == KEY_PRESSED && keyEvent.getKeyCode() == VK_O) {

                    }
                    if (keyEvent.isControlDown() && keyEvent.getID() == KEY_PRESSED && keyEvent.getKeyCode() == VK_K) {
                        // JComponent kasse = AktiveFenster.getFensterAlle("KrankenKasse");

                    }
                    if (keyEvent.isControlDown() && keyEvent.getID() == KeyEvent.KEY_PRESSED
                            && keyEvent.getKeyCode() == VK_A) {
                        // JComponent arzt = AktiveFenster.getFensterAlle("ArztVerwaltung");

                        Reha.getThisFrame()
                            .setCursor(Cursors.wartenCursor);
                        Reha.instance.progLoader.ArztFenster(0, TestePatStamm.PatStammArztID());
                        Reha.getThisFrame()
                            .setCursor(Cursors.normalCursor);
                    }

                    if (keyEvent.isControlDown() && keyEvent.getID() == KeyEvent.KEY_PRESSED
                            && keyEvent.getKeyCode() == VK_LEFT) {
                        setDivider(1);
                        keyEvent.consume();
                    }
                    if (keyEvent.isControlDown() && keyEvent.getID() == KeyEvent.KEY_PRESSED
                            && keyEvent.getKeyCode() == VK_RIGHT) {
                        setDivider(2);
                        keyEvent.consume();
                    }
                    if (keyEvent.isControlDown() && keyEvent.getID() == KeyEvent.KEY_PRESSED
                            && keyEvent.getKeyCode() == VK_UP) {
                        setDivider(3);
                        keyEvent.consume();
                    }
                    if (keyEvent.isControlDown() && keyEvent.getID() == KeyEvent.KEY_PRESSED
                            && keyEvent.getKeyCode() == VK_DOWN) {
                        setDivider(4);
                        keyEvent.consume();
                    }
                }
            }
        }, AWTEvent.KEY_EVENT_MASK);

    }

    public void setVertDivider(final int variante) {
        // System.out.println("Variante = "+variante);
        // Diese Funktion wäre etwas für Michael Schuett
        /*
         * Im Grunde wuerde es genuegen wenn Strg+Pfeil-Links/oder Rechts gedrueckt
         * wird, die Arbeitsflächen entweder hälftig oder voll sichtbar darzustellen den
         * Rest muesste man dann einfach mit der Maus herstellen.
         *
         */
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // links
                // System.out.println("Variante = "+variante);
                // System.out.println("Vollsichtbar = "+vollsichtbar);
                if (variante == 1) {

                    if (desktops[0].getWidth() <= 25) {
                        jSplitRechtsOU.setDividerLocation(0);
                        vollsichtbar = 1;
                        return;
                    } else if (desktops[0].getWidth() > 25) {
                        jSplitRechtsOU.setDividerLocation((jSplitRechtsOU.getDividerLocation() - 25));
                        vollsichtbar = -1;
                        return;
                    }

                    // rechts
                } else if (variante == 2) {
                    if (desktops[1].getWidth() <= 25) {
                        jSplitRechtsOU.setDividerLocation(jSplitRechtsOU.getWidth() - 7);
                        vollsichtbar = 0;
                        return;
                    } else {
                        jSplitRechtsOU.setDividerLocation((jSplitRechtsOU.getDividerLocation() + 25));
                        vollsichtbar = -1;
                        return;
                    }
                } else if (variante == 5) {
                    jSplitRechtsOU.setDividerLocation(jSplitRechtsOU.getWidth() - 7);
                } else if (variante == 6) {

                    jSplitRechtsOU.setDividerLocation(0);

                }
                vollsichtbar = -1;

            }
        });
    }

    public void setDivider(int variante) {
        final int xvariante = variante;
        // System.out.println("Variante = "+variante);
        // System.out.println("Vollsichtbar = "+vollsichtbar);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!SystemConfig.desktopHorizontal) {
                    setVertDivider(xvariante);
                    return;
                }
                switch (xvariante) {
                case 1:
                    if (jSplitLR.getDividerLocation() > 250) {
                        jSplitLR.setDividerLocation(dividerLocLR - 10);
                    } else {
                        if (dividerLocLR - 10 < 0) {
                            jSplitLR.setDividerLocation(0);
                        } else {
                            jSplitLR.setDividerLocation(dividerLocLR - 10);
                        }
                    }
                    break;
                case 2:
                    if (jSplitLR.getDividerLocation() < 250) {
                        jSplitLR.setDividerLocation(dividerLocLR + 10);
                    } else {
                        if (dividerLocLR + 10 > getThisFrame().getRootPane()
                                                              .getWidth()
                                - 7) {
                            jSplitLR.setDividerLocation(getThisFrame().getRootPane()
                                                                      .getWidth()
                                    - 7);
                        } else {
                            jSplitLR.setDividerLocation(dividerLocLR + 10);
                        }
                    }
                    break;
                case 3:
                    // nach oben
                    if (jSplitRechtsOU.getDividerLocation() > (getThisFrame().getRootPane()
                                                                             .getHeight()
                            / 2) - 3) {
                        jSplitRechtsOU.setDividerLocation((jxLinks.getHeight() / 2) - 3);
                        vollsichtbar = -1;
                    } else {
                        jSplitRechtsOU.setDividerLocation(0);
                        vollsichtbar = 1;
                    }
                    break;
                case 4:
                    // nach unten
                    if (jSplitRechtsOU.getDividerLocation() < (jxLinks.getHeight() / 2) - 3) {
                        jSplitRechtsOU.setDividerLocation((jxLinks.getHeight() / 2) - 3);
                        vollsichtbar = -1;
                    } else {
                        jSplitRechtsOU.setDividerLocation(getThisFrame().getRootPane()
                                                                        .getHeight()
                                - 7);
                        vollsichtbar = 0;
                    }
                    break;
                case 5:
                    // oben Vollbild
                    vollsichtbar = 0;
                    jSplitRechtsOU.setDividerLocation(getThisFrame().getRootPane()
                                                                    .getHeight()
                            - 7);
                    break;
                case 6:
                    // unten Vollbild
                    vollsichtbar = 1;
                    jSplitRechtsOU.setDividerLocation(0);
                    break;
                case 7:
                    vollsichtbar = 1;
                    jSplitRechtsOU.setDividerLocation(0);
                    break;
                }
            }
        });

    }

    public int getSichtbar() {
        /*
         * System.out.println("\n\nDivider-Location = "+jSplitRechtsOU.
         * getDividerLocation());
         * System.out.println("Divider-Ok          = "+Reha.dividerOk);
         * System.out.println("Höhe der RootPane = "+thisFrame.getRootPane().getHeight()
         * +"\n"); System.out.println("Höhe von Desktop[0] = "+desktops[0].getHeight());
         * System.out.println("Höhe von Desktop[1] = "+desktops[1].getHeight());
         * System.out.println("Breite von Desktop[0] = "+desktops[0].getWidth());
         * System.out.println("Breite von Desktop[1] = "+desktops[1].getWidth());
         */
        if (SystemConfig.desktopHorizontal) {
            if (desktops[0].getHeight() <= 10) {
                return 1;
            } else if (desktops[1].getHeight() <= 10) {
                return 0;
            }
        } else {
            /*
             * if(desktops[0].getWidth() <= 10){ return 1; }else if(desktops[1].getWidth()
             * <= 10){ return 0; }
             */
        }
        return -1;
    }

    ComponentListener componentListener = new ComponentAdapter() {

        @Override
        public void componentResized(ComponentEvent arg0) {
            // Größe einstellen
            try {
                if (((JComponent) arg0.getSource()).getName() != null) {
                    if (((JComponent) arg0.getSource()).getName()
                                                       .equals("PanelOben")) {
                        desktops[0].setBounds(0, 0, Reha.instance.jpOben.getWidth(), Reha.instance.jpOben.getHeight());
                    }
                    if (((JComponent) arg0.getSource()).getName()
                                                       .equals("PanelUnten")) {
                        desktops[1].setBounds(0, 0, Reha.instance.jpUnten.getWidth(),
                                Reha.instance.jpUnten.getHeight());
                    }
                    JInternalFrame[] frm = Reha.instance.desktops[0].getAllFrames();
                    for (int i = 0; i < frm.length; i++) {
                        if (((JRehaInternal) frm[i]).getImmerGross()) {
                            frm[i].setBounds(2, 2, Reha.instance.jpOben.getWidth() - 2,
                                    Reha.instance.jpOben.getHeight() - 2);
                        }
                        ((JRehaInternal) frm[i]).setCompOrder(i);
                        ((JRehaInternal) frm[i]).setzeIcon();
                    }
                    frm = Reha.instance.desktops[1].getAllFrames();
                    for (int i = 0; i < frm.length; i++) {
                        if (((JRehaInternal) frm[i]).getImmerGross()) {
                            frm[i].setBounds(2, 2, Reha.instance.jpUnten.getWidth() - 2,
                                    Reha.instance.jpUnten.getHeight() - 2);
                        }
                        ((JRehaInternal) frm[i]).setCompOrder(i);
                        ((JRehaInternal) frm[i]).setzeIcon();
                    }

                }
            } catch (java.lang.ClassCastException cex) {

            }
            jSplitLR.validate();
            desktop.setBounds(0, 0, getThisFrame().getContentPane()
                                                  .getWidth(),
                    getThisFrame().getContentPane()
                                  .getHeight());
            desktop.validate();
            jFrame.getContentPane()
                  .validate();

        }
    };

    @Override
    public void rehaEventOccurred(RehaEvent evt) {
        // System.out.println("Event angekommen - Event="+evt.getRehaEvent());
        if (evt.getRehaEvent()
               .equals("PatSuchen")) {
        }
        if (evt.getRehaEvent()
               .equals(RehaEvent.ERROR_EVENT)) {
            String sclass = (evt.getSource() == null ? "NULL"
                    : evt.getSource()
                         .getClass()
                         .toString());
            String module = evt.getDetails()[0];
            String errortext = evt.getDetails()[1];
            JOptionPane.showMessageDialog(null, "Es ist ein Fehler aufgetreten!\n\nKlasse=" + sclass + "\n\nModul="
                    + module + "\n\nFehlertext=" + errortext);
        }
    }

    public void ladenach() {
        int nochmals = JOptionPane.showConfirmDialog(null,
                "Die Datenbank konnte nicht gestartet werden, erneuter Versuch?", "Wichtige Benuterzinfo",
                JOptionPane.YES_NO_OPTION);
        if (nochmals == JOptionPane.YES_OPTION) {
            new Thread(new DbNachladen()).start();
        }
    }

    public void setzeInitStand(String stand) {
        new SocketClient().setzeInitStand(stand);
    }

    WindowListener windowlistener = new WindowAdapter() {

        @Override
        public void windowActivated(WindowEvent arg0) {
            desktop.setBounds(0, 0, getThisFrame().getContentPane()
                                                  .getWidth(),
                    getThisFrame().getContentPane()
                                  .getHeight());
        }

        @Override
        public void windowClosed(WindowEvent arg0) {
            if (Reha.barcodeScanner != null) {
                BarCodeScanner.serialPort.close();
                System.out.println("Serielle Schnittstelle wurde geschlossen");
            }
        }

        @Override
        public void windowClosing(WindowEvent arg0) {
            askCloseOrRestart();
        }

    };

    /***************************/
    public void starteTimer() {
        Reha.fangoTimer = new Timer(60000, actionListener);
        Reha.fangoTimer.setActionCommand("testeFango");
        Reha.fangoTimer.start();
        Reha.timerLaeuft = true;
    }

    public static void testeStrictMode() {
        try {
            String cmd = "show variables like 'sql_mode%'";
            Vector<Vector<String>> vecfeld = SqlInfo.holeFelder(cmd);
            System.out.println("sql_mode=" + vecfeld.get(0)
                                                    .get(1)
                                                    .trim());
            if (!vecfeld.get(0)
                        .get(1)
                        .trim()
                        .equals("")) {
                String meldung = "Achtung der MySql-Server wird im Modus: " + vecfeld.get(0)
                                                                                     .get(1)
                                                                                     .trim()
                        + " betrieben!\n" + "In diesem Modus kann Thera-Pi nicht fehlerfrei betrieben werden.\n\n"
                        + "Beenden Sie Thera-Pi und stellen Sie in der Datei my.ini (Linux=my.cnf) den Wert sql_mode='' ein\n"
                        + "Die Datei befindet sich in dem Verzeichnis indem der MySql-Server installiert wurde";
                JOptionPane.showMessageDialog(null, meldung);
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public static void testeMaxAllowed() {
        try {
            String cmd = "show variables like 'max_allowed_packet%'";
            Vector<Vector<String>> vecfeld = SqlInfo.holeFelder(cmd);

            int dfeld = (Integer.valueOf(vecfeld.get(0)
                                                .get(1))
                    / 1024) / 1024;
            System.out.println("max_allowed_packet=" + Integer.toString(dfeld) + " MB");
            if (dfeld < 16) {
                String meldung = "Achtung die MySql-Server Einstellung 'max_allowed_packet' ist bei Ihnen auf "
                        + Integer.toString(dfeld) + " MB eingestellt\n"
                        + "Dieser Wert ist möglicherweise zu niedrig wenn Sie größere Dokumentationen scannen wollen.\n\n"
                        + "Wir empfehlen Ihnen einen Wert von >= 32MB.\nEingestellt wird dieser Wert in der Datei my.ini (Linux=my.cnf)\n"
                        + "Diese Datei befindet sich in dem Verzeichnis indem der MySql-Server installiert wurde\n";
                JOptionPane.showMessageDialog(null, meldung);
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public static void testeNummernKreis() {
        String cmd = "select mandant from nummern LIMIT 1";
        Vector<Vector<String>> vecnummern = SqlInfo.holeFelder(cmd);
        if (vecnummern.size() <= 0) {
            cmd = "insert into nummern set pat='1',kg='1',ma='1',er='1',"
                    + "lo='1',rh='1',rnr='1',esol='1',bericht='1',afrnr='1',rgrnr='1',doku='1'," + "dfue='1',mandant='"
                    + Reha.getAktIK() + "'";
            // System.out.println(cmd);
            SqlInfo.sqlAusfuehren(cmd);
        }
    }

    ActionListener actionListener = new MenuActionListener(this);

    public void activateWebCam() {

        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws java.lang.Exception {

                try {
                    try {

                        Class c = Class.forName("javax.media.Manager");
                    } catch (ClassNotFoundException e) {
                        SystemConfig.sWebCamActive = "0";
                        JOptionPane.showMessageDialog(null, "Java Media Framework (JMF) ist nicht installiert"
                                + "\nWebCam kann nicht gestartet werden");

                    }

                    Vector<CaptureDeviceInfo> deviceList = javax.media.cdm.CaptureDeviceManager.getDeviceList(
                            new YUVFormat());
                    if (deviceList == null) {
                        JOptionPane.showMessageDialog(null, "Keine WebCam verfügbar!!");
                        SystemConfig.sWebCamActive = "0";
                        return null;
                    }
                    device = deviceList.firstElement();
                    ml = device.getLocator();
                    Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, new Boolean(true));
                    player = Manager.createRealizedPlayer(ml);
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                    SystemConfig.sWebCamActive = "0";
                } catch (NoPlayerException e) {
                    e.printStackTrace();
                    SystemConfig.sWebCamActive = "0";
                } catch (CannotRealizeException e) {
                    e.printStackTrace();
                    SystemConfig.sWebCamActive = "0";
                } catch (IOException e) {
                    e.printStackTrace();
                    SystemConfig.sWebCamActive = "0";
                }
                System.out.println("Web-Cam erfolgreich gestartet");
                return null;

            }
        }.execute();
    }

    public static void containerHandling(int cont) {
        if (instance.vollsichtbar == -1 || (!SystemConfig.desktopHorizontal)) {
            // System.out.println("Location =
            // "+Reha.instance.jSplitRechtsOU.getDividerLocation());
            // System.out.println("Width = "+Reha.instance.jSplitRechtsOU.getWidth());
            if (cont == 0) {
                if (instance.jSplitRechtsOU.getDividerLocation() == 0) {
                    instance.setDivider(5);
                }
            } else if (cont == 1) {
                if (instance.jSplitRechtsOU.getDividerLocation() == instance.jSplitRechtsOU.getWidth() - 7) {
                    instance.setDivider(6);
                }
            }

            return;
        }
        if ((instance.vollsichtbar == 1 && cont == 1) || (instance.vollsichtbar == 0 && cont == 0)) {
            return;
        }
        if (instance.vollsichtbar == 0 && cont == 1) {
            instance.setDivider(6);
            return;
        }
        if (instance.vollsichtbar == 1 && cont == 0) {
            instance.setDivider(5);
            return;
        }

    }

    ComponentListener getComponentListener() {
        return componentListener;
    }

}
