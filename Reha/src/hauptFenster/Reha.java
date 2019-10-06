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
import java.awt.Component;
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
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import javax.smartcardio.CardException;
import javax.sql.DataSource;
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
import CommonTools.DatFunk;
import CommonTools.ExUndHop;
import CommonTools.FileTools;
import CommonTools.FireRehaError;
import CommonTools.INIFile;
import CommonTools.INITool;
import CommonTools.JRtaTextField;
import CommonTools.RehaEvent;
import CommonTools.RehaEventClass;
import CommonTools.RehaEventListener;
import CommonTools.SqlInfo;
import CommonTools.StartOOApplication;
import abrechnung.AbrechnungGKV;
import abrechnung.AbrechnungReha;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.text.TextException;
import anmeldungUmsatz.Anmeldungen;
import anmeldungUmsatz.Umsaetze;
import arztFenster.ArztPanel;
import barKasse.Barkasse;
import benutzerVerwaltung.BenutzerRechte;
import dialoge.AboutDialog;
import dialoge.RehaSmartDialog;
import dta301.Dta301;
import egk.EgkReader;
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
import oOorgTools.OOTools;
import ocf.OcKVK;
import rechteTools.Rechte;
import rehaInternalFrame.JRehaInternal;
import rehaInternalFrame.OOODesktopManager;
import roogle.RoogleFenster;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemInit;
import systemEinstellungen.SystemPreislisten;
import systemTools.RehaPainters;
import systemTools.RezeptFahnder;
import systemTools.TestePatStamm;
import terminKalender.ParameterLaden;
import terminKalender.TerminFenster;
import urlaubBeteiligung.Beteiligung;
import urlaubBeteiligung.Urlaub;
import verkauf.VerkaufTab;
import wecker.Wecker;

public class Reha implements FocusListener, ComponentListener, ContainerListener, MouseListener, MouseMotionListener,
        KeyListener, RehaEventListener, WindowListener, WindowStateListener, ActionListener {

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
    private JXFrame jFrame = null;

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
    private JMenuItem aboutMenuItem = null;
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

    public boolean isLibreOffice = false;
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
    private static JXFrame thisFrame;

    private RehaSettings settings;
    private DataSource dataSource;
    private static Logger logger;

    public static JXFrame getThisFrame() {
        return thisFrame;
    }

    public static void setThisFrame(JXFrame thisFrame) {
        Reha.thisFrame = thisFrame;
    }

    /*
     * Einschalten für Geschwindigkeitstests zusätzlich in der Terminkalender.java
     * die Zeilen 2670, 2671,2714,2928,2929 public static int datecounts = 0; public
     * static long startmillis = 0;
     */

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

    public void start() {

        startWithMandantSet();

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
        new Reha(mainMandant).start();

    }

    public static void initializeLogging() {

        logger = LoggerFactory.getLogger(Reha.class);
    }

    private void startWithMandantSet() {

        aktIK = mandant.ik();
        aktMandant = mandant.name();

        String iniPath = Path.Instance.getProghome() + "ini/" + mandant.ik() + "/";
        try {
            settings = new RehaSettings(mandant);
        } catch (IOException e) {
            logger.error("RehaSettings could not be created",e);
        }

        INITool.init(iniPath);
        logger.info("Insgesamt sind " + Integer.toString(INITool.getDBInis().length)
                + " INI-Dateien in der Tabelle inidatei abgelegt");

        Titel2 = "  -->  [Mandant: " + getAktMandant() + "]";
        // System.out.println(Titel2);
        /**************************/
        Thread rehasockeThread = new Thread(new RehaSockServer(), "RehaSocketServer");
        rehasockeThread.start();
        /**************************/
        new Thread() {
            @Override
            public void run() {
                Process process;
                try {
                    System.out.println("Starte RehaxSwing.jar");
                    process = new ProcessBuilder("java", "-Djava.net.preferIPv4Stack=true", "-jar",
                            Path.Instance.getProghome() + "RehaxSwing.jar").start();
                    InputStream is = process.getInputStream();

                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);

                    while ((br.readLine()) != null) {
                        System.out.println(br.readLine());
                    }
                    is.close();
                    isr.close();
                    br.close();
                    System.out.println("RehaxSwing beendet");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        try {
            rehasockeThread.join(5000);
        } catch (InterruptedException e2) {
            logger.error("rehasocketthread could not be joined");
        }

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
                SystemConfig.SystemIconsInit();
                iconsOk = true;
                new SocketClient().setzeInitStand("System-Config initialisieren");
            }
        }.start();

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
        for (Iterator ii = new HashSet(defs.keySet()).iterator(); ii.hasNext();) {
            Object key = ii.next();
            if (key.equals("FormattedTextField.font")) {
                UIManager.put(key, fontUIDresource);
            }
            if (key.equals("TextField.font")) {
                UIManager.put(key, fontUIDresource);
            }
            if (key.equals("Label.font")) {
                UIManager.put(key, fontUIDresource);
            }
            if (key.equals("Button.font")) {
                UIManager.put(key, fontUIDresource);
            }
            if (key.equals("Table.font")) {
                UIManager.put(key, fontUIDresource);
            }
            if (key.equals("ComboBox.font")) {
                UIManager.put(key, fontUIDresource);
            }
        }
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
                    ex.printStackTrace();
                    System.out.println("Fehler beim Systemstart");
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
            SystemConfig.UpdateIni(inif, "HauptFenster", "TP1Offen", LinkeTaskPane.tp1.isCollapsed() ? "1" : "0", null);
            SystemConfig.UpdateIni(inif, "HauptFenster", "TP2Offen", LinkeTaskPane.tp4.isCollapsed() ? "1" : "0", null);
            SystemConfig.UpdateIni(inif, "HauptFenster", "TP3Offen", LinkeTaskPane.tp3.isCollapsed() ? "1" : "0", null);
            SystemConfig.UpdateIni(inif, "HauptFenster", "TP4Offen", LinkeTaskPane.tp5.isCollapsed() ? "1" : "0", null);
            SystemConfig.UpdateIni(inif, "HauptFenster", "TP5Offen", LinkeTaskPane.tp2.isCollapsed() ? "1" : "0", null);
            SystemConfig.UpdateIni(inif, "HauptFenster", "TP6Offen", LinkeTaskPane.tp6.isCollapsed() ? "1" : "0", null);
            if (LinkeTaskPane.mitUserTask) {
                SystemConfig.UpdateIni(inif, "HauptFenster", "TP7Offen", LinkeTaskPane.tp7.isCollapsed() ? "1" : "0",
                        null);
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
        this.jFrame.removeWindowListener(this);
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
                    ProgLoader.containerHandling(1);
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
                    ProgLoader.containerHandling(0);
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
            jFrame.addWindowListener(this);
            jFrame.addWindowStateListener(this);
            jFrame.addComponentListener(this);
            jFrame.addContainerListener(this);

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
                  .addComponentListener(this);

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
            jpOben.addComponentListener(this);

            desktops[0] = new Hintergrund(Reha.rehaBackImg);
            desktops[0].setName("DesktopOben");
            desktops[0].setOpaque(false);
            desktops[0].setSize(2000, 2000);
            desktops[0].setDesktopManager(new OOODesktopManager(0));
            desktops[0].addFocusListener(this);
            desktops[0].addMouseListener(this);
            desktops[0].addMouseMotionListener(this);
            desktops[0].addComponentListener(this);
            desktops[0].addContainerListener(this);

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
            jp2.addComponentListener(this);

            jpUnten = new JXPanel(new BorderLayout());
            jpUnten.setBorder(null);
            jpUnten.setBackgroundPainter(new CompoundPainter(mp));
            jpUnten.setName("PanelUnten");
            jpUnten.addComponentListener(this);

            desktops[1] = new Hintergrund(Reha.rehaBackImg);
            desktops[1].setName("DesktopUnten");
            desktops[1].setOpaque(false);
            desktops[1].setSize(2000, 2000);
            desktops[1].setDesktopManager(new OOODesktopManager(1));
            desktops[1].addFocusListener(this);
            desktops[1].addMouseListener(this);
            desktops[1].addMouseMotionListener(this);
            desktops[1].addComponentListener(this);
            desktops[1].addContainerListener(this);

            // desktops[1].add(new WorkFlow("WorkFlow",null,1));

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
            jSplitRechtsOU.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent arg0) {
                    // dividerLocOU = jSplitRechtsOU.getDividerLocation();
                }
            });

            jSplitRechtsOU.setDividerBorderVisible(false);
            jSplitRechtsOU.setDividerSize(7);
            ((BasicSplitPaneUI) jSplitRechtsOU.getUI()).getDivider()
                                                       .setBackground(Color.WHITE);

            jSplitRechtsOU.setBackground(Color.WHITE);
            jSplitRechtsOU.setName("RechtsSplitObenUnten");
            jSplitRechtsOU.setOneTouchExpandable(true);
            jxRechts.add(jSplitRechtsOU, BorderLayout.CENTER); // bislang o.k.

            jxRechts.addComponentListener(this);
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
            jxLinks.add(new LinkeTaskPane(conn), BorderLayout.CENTER);
            jxLinks.validate();
            jFrame.getContentPane()
                  .validate();
            /*
            new  Thread(){
                public void run(){
                    while((!Reha.iconsOk) && (!Reha.DbOk)){
                        try {
                            Thread.sleep(25);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    jxLinks.add(new LinkeTaskPane(),BorderLayout.CENTER);
                    jxLinks.validate();
                    jFrame.getContentPane().validate();
                }
            }.start();
             */
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws java.lang.Exception {
                    try {
                        INIFile updateini = null;
                        File f = new File(Path.Instance.getProghome() + "ini/tpupdateneu.ini");
                        if (f.exists()) {
                            updateini = INITool.openIni(Path.Instance.getProghome() + "ini/", "tpupdateneu.ini");
                        } else {
                            updateini = INITool.openIni(Path.Instance.getProghome() + "ini/", "tpupdate.ini");
                        }
                        try {
                            if (updateini.getStringProperty("TheraPiUpdates", "ProxyIP") != null
                                    && updateini.getStringProperty("TheraPiUpdates", "ProxyPort") != null
                                    && updateini.getStringProperty("TheraPiUpdates", "NoProxy") != null
                                    && updateini.getStringProperty("TheraPiUpdates", "ProxyIP")
                                                .equals("")
                                    && updateini.getStringProperty("TheraPiUpdates", "ProxyPort")
                                                .equals("")
                                    && updateini.getStringProperty("TheraPiUpdates", "NoProxy")
                                                .equals("")) {
                                System.setProperty("http.proxyHost",
                                        updateini.getStringProperty("TheraPiUpdates", "ProxyIP"));
                                System.setProperty("http.proxyPort",
                                        updateini.getStringProperty("TheraPiUpdates", "ProxyPort"));
                                System.setProperty("http.nonProxyHosts",
                                        updateini.getStringProperty("TheraPiUpdates", "NoProxy"));
                                System.setProperty("ftp.proxyHost",
                                        updateini.getStringProperty("TheraPiUpdates", "ProxyIP"));
                                System.setProperty("ftp.proxyPort",
                                        updateini.getStringProperty("TheraPiUpdates", "ProxyPort"));
                                System.setProperty("ftp.nonProxyHosts",
                                        updateini.getStringProperty("TheraPiUpdates", "NoProxy"));
                            }
                        } catch (NullPointerException ex) {
                            ex.printStackTrace();
                        }
                        try {
                            Reha.updatesChecken = (updateini.getIntegerProperty("TheraPiUpdates", "UpdateChecken") > 0
                                    ? true
                                    : false);
                            System.out.println("System soll nach Updates suchen = " + Reha.updatesChecken);
                        } catch (NullPointerException ex) {
                            Reha.updatesChecken = true;
                        }
                        if (!Reha.updatesChecken) {
                            return null;
                        }
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    TestForUpdates tfupd = null;
                                    tfupd = new TestForUpdates();

                                    Reha.updatesBereit = tfupd.doFtpTest();

                                    if (Reha.updatesBereit) {
                                        JOptionPane.showMessageDialog(null,
                                                "<html><b><font color='aa0000'>Es existieren Updates für Thera-Pi 1.0.</font></b><br><br>Bitte gehen Sie auf die Seite<br><br><b>System-Initialisierung -> 'Software-Updateservice'</b></html>");
                                    }
                                } catch (NullPointerException ex) {
                                    System.out.println("Fehler bei der Updatesuche");
                                    ex.printStackTrace();
                                }
                            }
                        }.start();

                    } catch (NullPointerException ex) {
                        StackTraceElement[] element = ex.getStackTrace();
                        String cmd = "";
                        for (int i = 0; i < element.length; i++) {
                            cmd = cmd + element[i] + "\n";
                        }
                        JOptionPane.showMessageDialog(null,
                                "Suche nach Updates fehlgeschlagen!\nIst die Internetverbindung o.k.");
                    }
                    return null;
                }

            }.execute();
        }

        setThisFrame(jFrame);

        jxLinks.setAlpha(1.0f);
        jxRechts.setAlpha(1.0f);

        // jxLinks.setAlpha(0.3f);
        // jxRechts.setAlpha(0.3f);

        // new Thread(new DatenbankStarten()).start();

        jFrame.setExtendedState(JXFrame.MAXIMIZED_BOTH);

        setKeyboardActions();
        setFocusWatcher();

        AktiveFenster.Init();

        /*
         * rehaEvent.addRehaEventListener(new RehaEventListener() {
         *
         * @Override public void RehaEventOccurred(RehaEvent evt) {
         * //System.out.println("Event getSource: = "+evt.getSource());
         * //System.out.println("Event Nachricht: = "+ evt.getRehaEvent()); } });
         */

        return jFrame;
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
            // JLabel lab = new JLabel("Benutzer: Admin");
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
            DropTargetListener dropTargetListener = new DropTargetListener() {
                @Override
                public void dragEnter(DropTargetDragEvent e) {
                }

                @Override
                public void dragExit(DropTargetEvent e) {
                }

                @Override
                public void dragOver(DropTargetDragEvent e) {
                }

                @Override
                public void drop(DropTargetDropEvent e) {
                    String mitgebracht = "";
                    try {
                        Transferable tr = e.getTransferable();
                        // Ursprüngliche Routine
                        /*
                         * DataFlavor[] flavors = tr.getTransferDataFlavors(); for (int i = 0; i <
                         * flavors.length; i++){ mitgebracht = String.valueOf((String)
                         * tr.getTransferData(flavors[i]).toString()); }
                         */
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

                @Override
                public void dropActionChanged(DropTargetDragEvent e) {
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
                        // if(Reha.osVersion.contains("Linux")){
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
                        // }
                        e.startDrag(null, selection, null);
                    }
                };

                DragSource dragSource = new DragSource();

                dragSource.createDefaultDragGestureRecognizer(copyLabel, DnDConstants.ACTION_COPY, dragGestureListener);
            }
            /*********************/

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
            men.addActionListener(this);
            stammMenu.add(men);
            stammMenu.addSeparator();
            men = new JMenuItem("Krankenkassen");
            men.setActionCommand("kasse");
            men.setAccelerator(KeyStroke.getKeyStroke(VK_K, Event.CTRL_MASK, false));
            men.setMnemonic(VK_K);
            men.addActionListener(this);
            stammMenu.add(men);
            stammMenu.addSeparator();
            men = new JMenuItem("Ärzte");
            men.setActionCommand("arzt");
            men.setAccelerator(KeyStroke.getKeyStroke(VK_A, Event.CTRL_MASK, false));
            men.setMnemonic(VK_A);
            men.addActionListener(this);
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
            men.addActionListener(this);
            abrechnungMenu.add(men);
            abrechnungMenu.addSeparator();
            men = new JMenuItem("Reha-Abrechnung");
            men.setActionCommand("rehaabrechnung");
            men.addActionListener(this);
            abrechnungMenu.add(men);
            abrechnungMenu.addSeparator();
            men = new JMenuItem("Barkasse abrechnen");
            men.setActionCommand("barkasse");
            men.addActionListener(this);
            abrechnungMenu.add(men);
            abrechnungMenu.addSeparator();
            men = new JMenuItem("Anmeldezahlen ermitteln");
            men.setActionCommand("anmeldezahlen");
            men.addActionListener(this);
            abrechnungMenu.add(men);
            men = new JMenuItem("Tagesumsätze ermitteln");
            men.setActionCommand("tagesumsatz");
            men.addActionListener(this);
            abrechnungMenu.add(men);
            abrechnungMenu.addSeparator();
            men = new JMenuItem("Offene Posten / Mahnwesen");
            men.setActionCommand("offeneposten");
            men.addActionListener(this);
            abrechnungMenu.add(men);
            abrechnungMenu.addSeparator();
            men = new JMenuItem("OP Rezeptgebühr-/Ausfall-/Verkaufsrechnung");
            men.setActionCommand("rgaffaktura");
            men.addActionListener(this);
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
            men.addActionListener(this);
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
            men.addActionListener(this);
            toolsMenu.add(men);
            toolsMenu.addSeparator();
            men = new JMenuItem("Kassenbuch erstellen");
            men.setActionCommand("kassenbuch");
            men.addActionListener(this);
            toolsMenu.add(men);
            toolsMenu.addSeparator();
            men = new JMenuItem("Geburtstagsbriefe erstellen");
            men.setActionCommand("geburtstagsbriefe");
            men.addActionListener(this);
            toolsMenu.add(men);
            toolsMenu.addSeparator();
            men = new JMenuItem("Sql-Modul");
            men.setActionCommand("sqlmodul");
            men.addActionListener(this);
            toolsMenu.add(men);
            /*
             * men = new JMenuItem("INI-Editor"); men.setActionCommand("iniedit");
             * men.addActionListener(this); toolsMenu.add(men); toolsMenu.addSeparator();
             */
            men = new JMenuItem("§301 Reha Fall-Steuerung");
            men.setActionCommand("fallsteuerung");
            men.addActionListener(this);
            toolsMenu.add(men);
            toolsMenu.addSeparator();
            men = new JMenuItem("Work-Flow Manager");
            men.setActionCommand("workflow");
            men.addActionListener(this);
            toolsMenu.add(men);
            toolsMenu.addSeparator();
            men = new JMenuItem("Heilmittelrichtlinien-Tool");
            men.setActionCommand("hmrsearch");
            men.addActionListener(this);
            toolsMenu.add(men);
            men = new JMenuItem("Thera-Pi OCR-Modul");
            men.setActionCommand("ocr");
            men.addActionListener(this);
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
            men.addActionListener(this);
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
            men.addActionListener(this);
            urlaubMenu.add(men);
            urlaubMenu.addSeparator();
            men = new JMenuItem("Umsatzbeteiligung ermitteln");
            men.setActionCommand("umsatzbeteiligung");
            men.addActionListener(this);
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
            aboutMenuItem.addActionListener(this);
        }
        return aboutMenuItem;
    }

    private JMenuItem getF2RescueMenuItem() {
        if (aboutF2RescueMenuItem == null) {
            aboutF2RescueMenuItem = new JMenuItem();
            aboutF2RescueMenuItem.setText("F2 - Rettungsanker");
            aboutF2RescueMenuItem.setActionCommand("f2Rescue");
            aboutF2RescueMenuItem.addActionListener(this);
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
            LinkeTaskPane.UpdateUI();
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
            officeapplication = new StartOOApplication(SystemConfig.OpenOfficePfad,
                    SystemConfig.OpenOfficeNativePfad).start(false);
            System.out.println("OpenOffice ist gestartet und aktiv = " + officeapplication.isActive());
            Reha.instance.Rehaprogress.setIndeterminate(false);
        } catch (OfficeApplicationException e) {
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
                int i;
                for (i = 0; i < 1; i++) {
                    // links
                    if (xvariante == 1) {
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
                    }
                    // rechts
                    if (xvariante == 2) {
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
                    }
                    if (xvariante == 3) {
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
                    }
                    if (xvariante == 4) {
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
                    }
                    if (xvariante == 5) {
                        // oben Vollbild
                        vollsichtbar = 0;
                        jSplitRechtsOU.setDividerLocation(getThisFrame().getRootPane()
                                                                        .getHeight()
                                - 7);
                        break;
                    }
                    if (xvariante == 6) {
                        // unten Vollbild
                        vollsichtbar = 1;
                        jSplitRechtsOU.setDividerLocation(0);
                        break;
                    }
                    if (xvariante == 7) {
                        vollsichtbar = 1;
                        jSplitRechtsOU.setDividerLocation(0);
                        break;
                    }
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

    public void setFocusWatcher() {
        long mask = AWTEvent.FOCUS_EVENT_MASK;
        /*
         * AWTEvent.ACTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK |
         * AWTEvent.FOCUS_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK |
         * AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.TEXT_EVENT_MASK |
         * AWTEvent.WINDOW_EVENT_MASK | AWTEvent.WINDOW_FOCUS_EVENT_MASK |
         * AWTEvent.WINDOW_STATE_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK;
         */
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.addAWTEventListener(new AWTEventListener() {

            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof FocusEvent) {
                    /*
                     * FocusEvent focusEvent = (FocusEvent) event;
                     * System.out.println("\n*************************************************");
                     * System.out.println("***Klasse = "+ ((FocusEvent)
                     * event).getComponent().getClass().toString());
                     * System.out.println("***Name = "+ ((FocusEvent)
                     * event).getComponent().getName()); System.out.println("***hat Focus = "
                     * +((FocusEvent) event).getComponent().hasFocus());
                     * System.out.println("***Name des Focused Windows = "+KeyboardFocusManager.
                     * getCurrentKeyboardFocusManager().getActiveWindow().getName());
                     * System.out.println("*************************************************");
                     */
                }
            }
        }, mask);
    }

    @Override
    public void componentHidden(ComponentEvent arg0) {
    }

    @Override
    public void componentMoved(ComponentEvent arg0) {
    }

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
                    desktops[1].setBounds(0, 0, Reha.instance.jpUnten.getWidth(), Reha.instance.jpUnten.getHeight());
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

    /************ Motion Event ******************/

    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

    /************ Motion für DragEvent ******************/
    @Override
    public void mouseDragged(MouseEvent arg0) {
    }

    @Override
    public void mouseMoved(MouseEvent arg0) {
    }

    /************ KeyListener *************************/
    @Override
    public void keyPressed(KeyEvent arg0) {
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

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

    static Component WerHatFocus() {
        final Component focusOwner = null;
        // focusOwner = FocusManager.getCurrentManager.getFocusedWindow();
        return focusOwner;
    }

    public void ladenach() {
        int nochmals = JOptionPane.showConfirmDialog(null,
                "Die Datenbank konnte nicht gestartet werden, erneuter Versuch?", "Wichtige Benuterzinfo",
                JOptionPane.YES_NO_OPTION);
        if (nochmals == JOptionPane.YES_OPTION) {
            new Thread(new DbNachladen()).start();
        }
    }

    public void addSbContainer(String simage, String sname, JComponent jcomponent) {
    }

    public void setzeInitStand(String stand) {
        new SocketClient().setzeInitStand(stand);
    }

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

    @Override
    public void windowStateChanged(WindowEvent arg0) {
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent arg0) {
    }

    @Override
    public void componentAdded(ContainerEvent arg0) {
    }

    @Override
    public void componentRemoved(ContainerEvent arg0) {
    }

    /***************************/
    public void starteTimer() {
        Reha.fangoTimer = new Timer(60000, this);
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

    /********** Actions **********/
    @Override
    public void actionPerformed(ActionEvent arg0) {
        String cmd = arg0.getActionCommand();
        switch (cmd) {
        case "ueberTheraPi":
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws java.lang.Exception {
                    AboutDialog aboutFenster = new AboutDialog(jFrame, aboutMenuItem.getText());
                    aboutFenster.setVisible(true);
                    aboutFenster.setFocus();
                    return null;
                }
            }.execute();
            break;
        case "f2Rescue":
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws java.lang.Exception {
                    if (Reha.terminLookup.size() <= 0) {
                        JOptionPane.showMessageDialog(null, "Bislang sind noch keine F3 / F2-Termine aufgezeichnet");
                        return null;
                    }
                    try {
                        new F2RettungsAnker();
                    } catch (TextException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }.execute();
            break;
        case "testeFango":
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws java.lang.Exception {
                    Wecker.testeWecker();
                    return null;
                }
            }.execute();
            break;
        case "patient":
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    progLoader.ProgPatientenVerwaltung(1, conn);
                    Reha.instance.progressStarten(false);
                    return null;
                }
            }.execute();
            return;
        case "kasse":
            Reha.instance.progLoader.KassenFenster(0, TestePatStamm.PatStammKasseID());
            return;
        case "arzt":
            Reha.instance.progLoader.ArztFenster(0, TestePatStamm.PatStammArztID());
            return;
        case "hmabrechnung":
            try {
                if (SystemConfig.hmEmailExtern.get("SenderAdresse") == null
                        || SystemConfig.hmEmailExtern.get("SenderAdresse")
                                                     .trim()
                                                     .equals("")) {
                    JOptionPane.showMessageDialog(null,
                            "<html>Bevor zum ersten Mal mit der GKV abgerechnet wird<br><br><b>muß(!) der Emailaccount in der System-Init konfiguriert werden.</b><br><br></html>");
                    return;
                }
            } catch (NullPointerException ex) {
                JOptionPane.showMessageDialog(null,
                        "<html>Bevor zum ersten Mal mit der GKV abgerechnet wird<br><br><b>muß(!) der Emailaccount in der System-Init konfiguriert werden.</b><br><br></html>");
                return;
            }
            Reha.instance.progLoader.AbrechnungFenster(1, conn);
            return;
        case "rehaabrechnung":
            Reha.instance.progLoader.RehaabrechnungFenster(1, "");
            return;
        case "barkasse":
            Reha.instance.progLoader.BarkassenFenster(1, "");
            return;
        case "anmeldezahlen":
            Reha.instance.progLoader.AnmeldungenFenster(1, "", conn);
            return;
        case "tagesumsatz":
            Reha.instance.progLoader.UmsatzFenster(1, "");
            return;
        case "verkauf":
            Reha.instance.progLoader.VerkaufFenster(1, "");
            return;
        case "urlaub":
            if (!Rechte.hatRecht(Rechte.Funktion_urlaubueberstunden, true)) {
                return;
            }
            new LadeProg(Path.Instance.getProghome() + "RehaUrlaub.jar" + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK());
            return;
        case "umsatzbeteiligung":
            Reha.instance.progLoader.BeteiligungFenster(1, "");
            return;
        case "lvastatistik":
            new LadeProg(Path.Instance.getProghome() + "RehaStatistik.jar" + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK());
            return;
        case "offeneposten":
            if (!Rechte.hatRecht(Rechte.Funktion_offeneposten, true)) {
                return;
            }
            if (!RehaIOServer.offenePostenIsActive) {
                new LadeProg(Path.Instance.getProghome() + "OffenePosten.jar" + " " + Path.Instance.getProghome() + " "
                        + Reha.getAktIK() + " " + Reha.xport);
            } else {
                new ReverseSocket().setzeRehaNachricht(RehaIOServer.offenePostenreversePort,
                        "Reha#" + RehaIOMessages.MUST_GOTOFRONT);
            }
            return;
        case "rezeptfahnder":
            new RezeptFahnder(true, conn);
            return;
        case "rgaffaktura":
            if (!Rechte.hatRecht(Rechte.Funktion_barkasse, false)) {
                JOptionPane.showMessageDialog(null, "Keine Berechtigung -> Funktion Ausbuchen RGAF-Faktura");
                return;
            }
            if (!RehaIOServer.rgAfIsActive) {
                new LadeProg(Path.Instance.getProghome() + "OpRgaf.jar" + " " + Path.Instance.getProghome() + " "
                        + Reha.getAktIK() + " " + Reha.xport);
            } else {
                new ReverseSocket().setzeRehaNachricht(RehaIOServer.rgAfreversePort,
                        "Reha#" + RehaIOMessages.MUST_GOTOFRONT);
            }
            return;
        case "kassenbuch":
            if (!Rechte.hatRecht(Rechte.Funktion_kassenbuch, true)) {
                return;
            }
            new LadeProg(Path.Instance.getProghome() + "RehaKassenbuch.jar" + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK());
            return;
        case "geburtstagsbriefe":
            if (!Rechte.hatRecht(Rechte.Sonstiges_geburtstagsbriefe, true)) {
                return;
            }
            new LadeProg(Path.Instance.getProghome() + "GBriefe.jar" + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK());
            return;
        case "sqlmodul":
            if (!Rechte.hatRecht(Rechte.Sonstiges_sqlmodul, true)) {
                return;
            }
            if (!RehaIOServer.rehaSqlIsActive) {
                new LadeProg(Path.Instance.getProghome() + "RehaSql.jar" + " " + Path.Instance.getProghome() + " "
                        + Reha.getAktIK() + " " + String.valueOf(Integer.toString(Reha.xport))
                        + (!Rechte.hatRecht(Rechte.BenutzerSuper_user, false) ? " readonly" : " full"));
            } else {
                new ReverseSocket().setzeRehaNachricht(RehaIOServer.rehaSqlreversePort,
                        "Reha#" + RehaIOMessages.MUST_GOTOFRONT);
            }
            return;
        case "fallsteuerung":
            if (!Rechte.hatRecht(Rechte.Sonstiges_Reha301, true)) {
                return;
            }
            if (RehaIOServer.reha301IsActive) {
                JOptionPane.showMessageDialog(null, "Das 301-er Modul läuft bereits");
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new ReverseSocket().setzeRehaNachricht(RehaIOServer.reha301reversePort,
                                "Reha301#" + RehaIOMessages.MUST_GOTOFRONT);
                    }
                });
                return;
            }
            new LadeProg(Path.Instance.getProghome() + "Reha301.jar " + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK() + " " + String.valueOf(Integer.toString(Reha.xport)));
            return;
        case "workflow":
            if (!Rechte.hatRecht(Rechte.Sonstiges_Reha301, true)) {
                return;
            }
            if (RehaIOServer.rehaWorkFlowIsActive) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new ReverseSocket().setzeRehaNachricht(RehaIOServer.rehaWorkFlowreversePort,
                                "ZeigeFrame#" + RehaIOMessages.MUST_GOTOFRONT);
                    }
                });
                return;
            }
            new LadeProg(Path.Instance.getProghome() + "WorkFlow.jar " + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK() + " " + String.valueOf(Integer.toString(Reha.xport)));
            return;
        case "hmrsearch":
            System.out.println("isActive = " + RehaIOServer.rehaHMKIsActive);
            if (RehaIOServer.rehaHMKIsActive) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String searchrez = (Reha.instance.patpanel != null
                                ? " " + Reha.instance.patpanel.vecaktrez.get(1)
                                : "");
                        new ReverseSocket().setzeRehaNachricht(RehaIOServer.rehaHMKreversePort,
                                "Reha#" + RehaIOMessages.MUST_GOTOFRONT);
                        if (!searchrez.isEmpty()) {
                            new ReverseSocket().setzeRehaNachricht(RehaIOServer.rehaHMKreversePort,
                                    "Reha#" + RehaIOMessages.MUST_REZFIND + "#" + searchrez);
                        }
                    }
                });
                return;
            }
            new LadeProg(Path.Instance.getProghome() + "RehaHMK.jar " + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK() + " " + String.valueOf(Integer.toString(Reha.xport))
                    + (Reha.instance.patpanel != null ? " " + Reha.instance.patpanel.vecaktrez.get(1) : ""));
            // System.out.println("Übergebe Rezeptnummer:
            // "+SystemConfig.hmAdrRDaten.get("Rnummer"));
            // Reha.thisFrame.setCursor(Reha.instance.wartenCursor);
            return;
        case "iniedit":
            if (!Rechte.hatRecht(Rechte.Sonstiges_sqlmodul, true)) {
                return;
            }
            new LadeProg(Path.Instance.getProghome() + "RehaIniedit.jar " + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK());
            return;
        case "ocr":
            new LadeProg(Path.Instance.getProghome() + "RehaOCR.jar " + " " + Path.Instance.getProghome() + " "
                    + Reha.getAktIK() + " " + String.valueOf(Integer.toString(Reha.xport)));
            return;
        }

    }

    /****************/
    /***************/
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

    /*********************************************/
}

/**************
 *
 * Thread zum Start der Datenbank
 *
 * @author admin
 *
 */

final class DatenbankStarten implements Runnable {
    Logger logger = LoggerFactory.getLogger(DatenbankStarten.class);

    private void StarteDB() {
        final Reha obj = Reha.instance;

        final String sDB = "SQL";
        if (obj.conn != null) {
            try {
                obj.conn.close();
            } catch (final SQLException e) {
            }
        }
        try {
            if (sDB == "SQL") {
                new SocketClient().setzeInitStand("Datenbanktreiber installieren");
                Class.forName(SystemConfig.vDatenBank.get(0)
                                                     .get(0))
                     .newInstance();
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (sDB == "SQL") {

                // obj.conn = (Connection)
                // DriverManager.getConnection("jdbc:mysql://194.168.1.8:3306/dbf","entwickler","entwickler");
                new SocketClient().setzeInitStand("Datenbank initialisieren und öffnen");
                obj.conn = DriverManager.getConnection(SystemConfig.vDatenBank.get(0)
                                                                              .get(1)
                        + "?jdbcCompliantTruncation=false&zeroDateTimeBehavior=convertToNull&autoReconnect=true",
                        SystemConfig.vDatenBank.get(0)
                                               .get(3),
                        SystemConfig.vDatenBank.get(0)
                                               .get(4));
            }
            int nurmaschine = SystemConfig.dieseMaschine.toString()
                                                        .lastIndexOf("/");
            obj.sqlInfo.setConnection(obj.conn);
            new ExUndHop().setzeStatement("delete from flexlock where maschine like '%"
                    + SystemConfig.dieseMaschine.toString()
                                                .substring(0, nurmaschine)
                    + "%'");
            // geht leider nicht, erfordert root-Rechte
            // SqlInfo.sqlAusfuehren("SET GLOBAL sql_mode = ''");
            // sql_mode ging zwar mit SET SESSION, aber dann haben wir max_allowed... immer
            // noch nicht gelöst.
            // SqlInfo.sqlAusfuehren("SET GLOBAL max_allowed_packet = 32*1024*1024");
            String db = SystemConfig.vDatenBank.get(0)
                                               .get(1)
                                               .replace("jdbc:mysql://", "");
            db = db.substring(0, db.indexOf("/"));
            final String xdb = db;
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws java.lang.Exception {
                    try {
                        while (Reha.getThisFrame() == null || Reha.getThisFrame()
                                                                  .getStatusBar() == null
                                || Reha.instance.dbLabel == null) {
                            Thread.sleep(25);
                        }
                        Reha.instance.dbLabel.setText(Version.aktuelleVersion + xdb);
                    } catch (NullPointerException ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }

            }.execute();

            Reha.DbOk = true;
            try {
                Reha.testeNummernKreis();
                Reha.testeStrictMode();
                Reha.testeMaxAllowed();

            } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                ex.printStackTrace();
            }

        } catch (final SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            Reha.DbOk = false;
            Reha.nachladenDB = -1;
            System.out.println("Fehler bei der Initialisierung der Datenbank");
            new FireRehaError(RehaEvent.ERROR_EVENT, "Datenbankfehler!",
                    new String[] { "Datenabankfehler, Fehlertext:", ex.getMessage() });
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {

                e1.printStackTrace();

            }

            if (Reha.nachladenDB == JOptionPane.YES_OPTION) {
                new Thread(new DbNachladen()).start();
            } else {
                // new FireRehaError(this,"Datenbankfehler",new String[] {"Fehlertext:","Die
                // Datenbank kann nicht gestartet werden"});
                new SocketClient().setzeInitStand(
                        "Fehler!!!! Datenbank kann nicht gestartet werden - Thera-Pi wird beendet");

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new SocketClient().setzeInitStand("INITENDE");

                System.exit(0);

            }
            return;
        }
        return;
    }

    @Override
    public void run() {
        int i = 0;
        while (!Reha.instance.splashok) {
            i = i + 1;
            if (i > 10) {
                break;
            }
            try {
                Thread.sleep(300);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        new SocketClient().setzeInitStand("Datenbank starten");
        StarteDB();
        if (Reha.DbOk) {
            Date zeit = new Date();
            String stx = "Insert into eingeloggt set comp='" + SystemConfig.dieseMaschine + "', zeit='"
                    + zeit.toString() + "', einaus='ein'";
            new ExUndHop().setzeStatement(stx);
            try {
                Thread.sleep(50);

                new SocketClient().setzeInitStand("Datenbank ok");

                ParameterLaden.Init();
                new SocketClient().setzeInitStand("Systemparameter laden");

                Reha.sysConf.SystemInit(3);

                ParameterLaden.Passwort();
                new SocketClient().setzeInitStand("Systemparameter ok");

                new SocketClient().setzeInitStand("Native Interface ok");

                Reha.sysConf.SystemInit(4);

                new SocketClient().setzeInitStand("Emailparameter");

                Reha.sysConf.SystemInit(6);

                new SocketClient().setzeInitStand("Roogle-Gruppen ok!");

                Reha.sysConf.SystemInit(7);

                new SocketClient().setzeInitStand("Verzeichnisse");

                new SocketClient().setzeInitStand("Mandanten-Daten einlesen");

                Reha.sysConf.SystemInit(11);

                Reha.sysConf.SystemInit(9);

                Thread.sleep(50);

                new SocketClient().setzeInitStand("HashMaps initialisieren");

                SystemConfig.HashMapsVorbereiten();

                Thread.sleep(50);

                new SocketClient().setzeInitStand("Desktop konfigurieren");

                SystemConfig.DesktopLesen();

                Thread.sleep(50);

                new SocketClient().setzeInitStand("Patientenstamm init");

                SystemConfig.PatientLesen();

                Thread.sleep(50);

                new SocketClient().setzeInitStand("Gerätetreiber initialiseieren");

                SystemConfig.GeraeteInit();

                Thread.sleep(50);

                new SocketClient().setzeInitStand("Arztgruppen einlesen");

                SystemConfig.ArztGruppenInit();

                Thread.sleep(50);

                new SocketClient().setzeInitStand("Rezeptparameter einlesen");

                SystemConfig.RezeptInit();

                new SocketClient().setzeInitStand("Bausteine für Therapie-Berichte laden");

                SystemConfig.TherapBausteinInit();

                // SystemConfig.compTest();

                new SocketClient().setzeInitStand("Fremdprogramme überprüfen");

                SystemConfig.FremdProgs();

                new SocketClient().setzeInitStand("Geräteliste erstellen");

                SystemConfig.GeraeteListe();

                SystemConfig.CompanyInit();

                FileTools.deleteAllFiles(new File(SystemConfig.hmVerzeichnisse.get("Temp")));
                if (SystemConfig.sBarcodeAktiv.equals("1")) {
                    try {
                        Reha.barcodeScanner = new BarCodeScanner(SystemConfig.sBarcodeCom);
                    } catch (Exception e) {
                        //// System.out.println("Barcode-Scanner konnte nicht installiert werden");
                    } catch (java.lang.Exception e) {
                        e.printStackTrace();
                    }
                }
                new SocketClient().setzeInitStand("Firmendaten einlesen");

                Vector<Vector<String>> vec = SqlInfo.holeFelder("select min(datum),max(datum) from flexkc");

                try {
                    Reha.kalMin = DatFunk.sDatInDeutsch((vec.get(0)
                                                            .get(0)));
                    Reha.kalMax = DatFunk.sDatInDeutsch((vec.get(0)
                                                            .get(1)));
                } catch (java.lang.Exception e) {
                    logger.info("kalmin und kalmax nicht gesetzt", e);
                }

                SystemConfig.FirmenDaten();

                new SocketClient().setzeInitStand("Gutachten Parameter einlesen");

                SystemConfig.GutachtenInit();

                SystemConfig.AbrechnungParameter();

                SystemConfig.BedienungIni_ReadFromIni();

                SystemConfig.OffenePostenIni_ReadFromIni();

                SystemConfig.JahresUmstellung();

                SystemConfig.Feiertage();

                // notwendig bis alle Überhangsrezepte der BKK-Gesundheit abgearbeitet sind.
                SystemConfig.ArschGeigenTest();

                SystemConfig.EigeneDokuvorlagenLesen();

                SystemConfig.IcalSettings();

                new Thread(new PreisListenLaden()).start();

                if (SystemConfig.sWebCamActive.equals("1")) {
                    Reha.instance.activateWebCam();
                }

            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (NullPointerException e2) {
                e2.printStackTrace();
            }
        } else {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws java.lang.Exception {
                    new SocketClient().setzeInitStand("INITENDE");
                    return null;
                }
            }.execute();

        }
    }
}

final class DbNachladen implements Runnable {
    @Override
    public void run() {
        final String sDB = "SQL";
        final Reha obj = Reha.instance;
        if (Reha.instance.conn != null) {
            try {
                Reha.instance.conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            if (sDB == "SQL") {
                new SocketClient().setzeInitStand("Datenbank initialisieren und öffnen");
                obj.conn = DriverManager.getConnection(SystemConfig.vDatenBank.get(0)
                                                                              .get(1)
                        + "?jdbcCompliantTruncation=false",
                        SystemConfig.vDatenBank.get(0)
                                               .get(3),
                        SystemConfig.vDatenBank.get(0)
                                               .get(4));
            }
            int nurmaschine = SystemConfig.dieseMaschine.toString()
                                                        .lastIndexOf("/");
            new ExUndHop().setzeStatement("delete from flexlock where maschine like '%"
                    + SystemConfig.dieseMaschine.toString()
                                                .substring(0, nurmaschine)
                    + "%'");
            if (obj.dbLabel != null) {
                String db = SystemConfig.vDatenBank.get(0)
                                                   .get(1)
                                                   .replace("jdbc:mysql://", "");
                db = db.substring(0, db.indexOf("/"));
                obj.dbLabel.setText(Version.aktuelleVersion + db);
            }
            obj.sqlInfo.setConnection(obj.conn);
            Reha.DbOk = true;

        } catch (final SQLException ex) {
            Reha.DbOk = false;
            Reha.nachladenDB = -1;
            Reha.nachladenDB = JOptionPane.showConfirmDialog(Reha.getThisFrame(),
                    "Die Datenbank konnte nicht gestartet werden, erneuter Versuch?", "Wichtige Benuterzinfo",
                    JOptionPane.YES_NO_OPTION);

            while (Reha.nachladenDB < 0) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (Reha.nachladenDB == JOptionPane.YES_OPTION) {
                new Thread(new DbNachladen()).start();
            }
            return;
        }
        return;
    }
}

final class ErsterLogin implements Runnable {
    private void Login() {
        new Thread() {
            @Override
            public void run() {
                Reha.starteOfficeApplication();
                OOTools.ooOrgAnmelden();
            }
        }.start();
        ProgLoader.PasswortDialog();
    }

    @Override
    public void run() {
        Login();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Reha.getThisFrame()
                    .setMinimumSize(new Dimension(800, 600));
                Reha.getThisFrame()
                    .setPreferredSize(new Dimension(800, 600));
                Reha.getThisFrame()
                    .setExtendedState(JXFrame.MAXIMIZED_BOTH);
                Reha.getThisFrame()
                    .setVisible(true);
                INIFile inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/",
                        "rehajava.ini");
                if (inif.getIntegerProperty("HauptFenster", "Divider1") != null) {
                    Reha.instance.jSplitLR.setDividerLocation(
                            (Reha.divider1 = inif.getIntegerProperty("HauptFenster", "Divider1")));
                    Reha.instance.jSplitRechtsOU.setDividerLocation(
                            (Reha.divider2 = inif.getIntegerProperty("HauptFenster", "Divider2")));
                    // System.out.println("Divider gesetzt");
                    // System.out.println("Divider 1 = "+inif.getIntegerProperty("HauptFenster",
                    // "Divider1"));
                    // System.out.println("Divider 2 = "+inif.getIntegerProperty("HauptFenster",
                    // "Divider2")+"\n\n");
                    Reha.dividerOk = true;
                    // Hier mußt noch eine funktion getSichtbar() entwickelt werden
                    // diese ersetzt die nächste Zeile
                    // System.out.println("Sichtbar Variante = "+Reha.instance.getSichtbar());
                } else {
                    // System.out.println("Divider-Angaben sind noch null");
                    Reha.instance.setDivider(5);
                }

                Reha.getThisFrame()
                    .getRootPane()
                    .validate();
                Reha.isStarted = true;

                Reha.getThisFrame()
                    .setVisible(true);

                if (Reha.dividerOk) {
                    Reha.instance.vollsichtbar = Reha.instance.getSichtbar();
                    if (!SystemConfig.desktopHorizontal) {
                        Reha.instance.jSplitRechtsOU.setDividerLocation((Reha.divider2));
                    }
                    // System.out.println("Wert für Vollsichtbar = "+Reha.instance.vollsichtbar);
                }

                // Reha.thisFrame.pack();
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        try {

                            // SCR335
                            // ctpcsc31kv

                            if (SystemConfig.sReaderAktiv.equals("1")) {
                                try {

                                    System.out.println("Aktiviere Reader: " + SystemConfig.sReaderName + "\n"
                                            + "CT-API Bibliothek: " + SystemConfig.sReaderCtApiLib);

                                    Reha.instance.ocKVK = new OcKVK();

                                    EgkReader target = new EgkReader(SystemConfig.sReaderName);
                                    target.addCardListener(Reha.instance.ocKVK);
                                    Thread egk= new Thread(
                                     target);

                                    egk.setDaemon(true);
                                    egk.setName("EGK");
                                    egk.start();

                                } catch (CardException e) {
                                    disableReader("Fehlerstufe rc = -2 oder -4  = Karte wird nicht unterstützt\n"
                                            + e.getMessage());
                                } catch (ClassNotFoundException e) {
                                    disableReader("Fehlerstufe rc = -1 = CT-API läßt sich nicht initialisieren\n"
                                            + e.getMessage());
                                } catch (java.lang.Exception e) {
                                    if (e.getMessage()
                                         .contains("property file")) {
                                        disableReader("Anderweitiger Fehler\n"
                                                + "Die Datei opencard.properties befindet sich nicht im Java-Verzeichnis ../lib."
                                                + "Das Kartenlesegerät kann nicht verwendet werden.");
                                    } else {
                                        disableReader("Anderweitiger Fehler\n" + e.getMessage());
                                    }
                                }


                            }
                        } catch (NullPointerException ex) {
                            ex.printStackTrace();
                        }
                        return null;
                    }
                }.execute();
            }
        });
    }

    private void disableReader(String error) {
        SystemConfig.sReaderAktiv = "0";
        Reha.instance.ocKVK = null;
        JOptionPane.showMessageDialog(null, error);
    }
}

final class PreisListenLaden implements Runnable {
    private void Einlesen() {

        try {

            while (Reha.instance == null || Reha.instance.jxLinks == null || Reha.instance.jxRechts == null) {
                long zeit = System.currentTimeMillis();
                try {
                    Thread.sleep(50);
                    if (System.currentTimeMillis() - zeit > 20000) {
                        JOptionPane.showMessageDialog(null, "Fehler beim Starten des Systems ");
                        System.exit(0);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (isAktiv("Physio")) {
                new SocketClient().setzeInitStand("Preisliste Physio einlesen");
                SystemPreislisten.ladePreise("Physio");
            }
            if (isAktiv("Massage")) {
                new SocketClient().setzeInitStand("Preisliste Massage einlesen");
                SystemPreislisten.ladePreise("Massage");
            }
            if (isAktiv("Ergo")) {
                new SocketClient().setzeInitStand("Preisliste Ergo einlesen");
                SystemPreislisten.ladePreise("Ergo");
            }
            if (isAktiv("Logo")) {
                new SocketClient().setzeInitStand("Preisliste Logo einlesen");
                SystemPreislisten.ladePreise("Logo");
            }
            if (isAktiv("Reha")) {
                new SocketClient().setzeInitStand("Preisliste Reha einlesen");
                SystemPreislisten.ladePreise("Reha");
            }
            if (isAktiv("Podo")) {
                new SocketClient().setzeInitStand("Preisliste Podologie einlesen");
                SystemPreislisten.ladePreise("Podo");
            }
            if (SystemConfig.mitRs) {
                if (isAktiv("Rsport")) {
                    new SocketClient().setzeInitStand("Preisliste Rehasport einlesen");
                    SystemPreislisten.ladePreise("Rsport");
                }
                if (isAktiv("Ftrain")) {
                    new SocketClient().setzeInitStand("Preisliste Funktionstraining einlesen");
                    SystemPreislisten.ladePreise("Ftrain");
                }
            }

            SystemPreislisten.ladePreise("Common");

            System.out.println("Preislisten einlesen abgeschlossen");
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        try {
            new SocketClient().setzeInitStand("System-Init abgeschlossen!");
            Reha.instance.setzeInitEnde();
            Reha.instance.initok = true;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

    }

    public boolean isAktiv(String disziplin) {

        for (int i = 0; i < SystemConfig.rezeptKlassenAktiv.size(); i++) {
            if (SystemConfig.rezeptKlassenAktiv.get(i)
                                               .get(0)
                                               .toLowerCase()
                                               .startsWith(disziplin.toLowerCase())
                    || (disziplin.equals("Rsport") && SystemConfig.rezeptKlassenAktiv.get(i)
                                                                                     .get(0)
                                                                                     .toLowerCase()
                                                                                     .startsWith("rehasport"))
                    || (disziplin.equals("Ftrain") && SystemConfig.rezeptKlassenAktiv.get(i)
                                                                                     .get(0)
                                                                                     .toLowerCase()
                                                                                     .startsWith("funktionstrai"))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        Einlesen();
        int i = 0;
        while (!Reha.instance.initok) {
            i = i + 1;
            if (i > 10) {
                break;
            }
            try {
                Thread.sleep(100);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        new Thread(new ErsterLogin()).start();
    }

}

class SocketClient {
    String stand = "";
    Socket server = null;

    public void setzeInitStand(String stand) {
        this.stand = stand;
        run();
    }

    public void run() {
        serverStarten();
    }

    private void serverStarten() {
        try {
            this.server = new Socket("localhost", 1234);
            OutputStream output = server.getOutputStream();
            InputStream input = server.getInputStream();

            byte[] bytes = this.stand.getBytes();

            output.write(bytes);
            output.flush();
            int zahl = input.available();
            if (zahl > 0) {
                byte[] lesen = new byte[zahl];
                input.read(lesen);
            }

            server.close();
            input.close();
            output.close();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*******************************************/
final class HilfeDatenbankStarten implements Runnable {

    void StarteDB() {

    }

    @Override
    public void run() {
        final Reha obj = Reha.instance;

        // final String sDB = "SQL";
        if (obj.hilfeConn != null) {
            try {
                obj.hilfeConn.close();
            } catch (final SQLException e) {
            }
        }
        try {
            Class.forName(SystemConfig.hmHilfeServer.get("HilfeDBTreiber"))
                 .newInstance();
            Reha.HilfeDbOk = true;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Reha.instance.hilfeConn = DriverManager.getConnection(SystemConfig.hmHilfeServer.get("HilfeDBLogin"),
                    SystemConfig.hmHilfeServer.get("HilfeDBUser"), SystemConfig.hmHilfeServer.get("HilfeDBPassword"));
        } catch (final SQLException ex) {
            Reha.HilfeDbOk = false;
            return;
        }
        return;
    }
}
