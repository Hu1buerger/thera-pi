package rehaHMK;

import CommonTools.Colors;
import CommonTools.SqlInfo;
import CommonTools.ini.INIFile;
import RehaIO.RehaReverseServer;
import RehaIO.SocketClient;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import environment.Path;
import environment.SWTJarLoader;
import io.RehaIOMessages;
import logging.Logging;
import office.OOService;
import sql.DatenquellenFactory;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;

public class RehaHMK implements WindowListener {
  public static boolean DbOk;
  
  JFrame jFrame;
  
  public static JFrame thisFrame = null;
  
  public Connection conn;
  public static RehaHMK thisClass;
  public SqlInfo sqlInfo = null;
  
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
  
  public static int xport = -1;
  public static boolean xportOk = false;
  public RehaReverseServer rehaReverseServer = null;
  public static int rehaReversePort = 6000;

  public static String officeProgrammPfad = "C:/Program Files (x86)/OpenOffice.org 3";
  public static String officeNativePfad = "C:/RehaVerwaltung/Libraries/lib/openofficeorg/";

  public static String aktIK = "510841109";
  public static CompoundPainter<Object> cp = null;
  public static CompoundPainter<Object> cparzt = null;
  public static CompoundPainter<Object> cpscanner = null;
  public static boolean testcase = false;

  public static HashMap<String, Integer> pgReferenz = new HashMap<String, Integer>();
  public static HashMap<String, ImageIcon> icons = new HashMap<String, ImageIcon>();
  public static HashMap<String, String> hmAdrADaten = new HashMap<String, String>();
  public static IOfficeApplication officeapplication;

  public static String[] arztGruppen = null;
  public static String hmkURL = null;
  public static String aktUser = "unbekannt";
  private static String proghome;
  
  public static void main(String[] args) {
      new Logging("hmk");
      try {
          SwingUtilities.invokeAndWait(new SWTJarLoader());
      } catch (InvocationTargetException | InterruptedException e1) {
          e1.printStackTrace();
      }
      RehaHMK application = new RehaHMK();

      application.getInstance();
      application.getInstance().sqlInfo = new SqlInfo();
    if (args.length > 0 || testcase) {
      if (!testcase) {
        System.out.println("hole daten aus INI-Datei " + args[0]);
        INIFile inif = new INIFile(args[0] + "ini/" + args[1] + "/rehajava.ini");

        officeProgrammPfad = inif.getStringProperty("OpenOffice.org", "OfficePfad");
        officeNativePfad = inif.getStringProperty("OpenOffice.org", "OfficeNativePfad");
        
        proghome = Path.Instance.getProghome();
        aktIK = args[1];
        
        INIFile hmrinif = new INIFile(proghome + "ini/" + RehaHMK.aktIK + "/hmrmodul.ini");
        hmkURL = hmrinif.getStringProperty("HMRModul", "HMKUrl");
        
        if (args.length >= 3)
          rehaReversePort = Integer.parseInt(args[2]); 
        
        if (args.length >= 4) {
            if (args[3].equals("info")) {
                String meldung = "Pfad zur rehajava.ini = " + args[0] + "ini/" + args[1] + "/rehajava.ini\n"
                        + "Aktuell verwendetes IK = " + aktIK + "\n"
                        + "Pfad zu OpenOffice = " + officeProgrammPfad + "\n" + "Pfad zur NativeView.dll = "
                        + officeNativePfad + "\n";
                JOptionPane.showMessageDialog(null, meldung);
            }
        }
      } 

      cp = null;
      MattePainter mp = null;
      LinearGradientPaint p = null;
      Point2D start = new Point2D.Float(0.0F, 0.0F);
      Point2D end = new Point2D.Float(150.0F, 800.0F);
      float[] dist = { 0.0F, 0.75F };
      Color[] colors = { Color.WHITE, Color.LIGHT_GRAY };
      p = new LinearGradientPaint(start, end, dist, colors);
      mp = new MattePainter(p);
      cp = new CompoundPainter(new Painter[] { (Painter)mp });
      start = new Point2D.Float(0.0F, 0.0F);
      end = new Point2D.Float(0.0F, 400.0F);
      dist = new float[] { 0.0F, 0.75F };
      colors = new Color[] { Color.WHITE, Colors.TaskPaneBlau.alpha(0.45F) };
      p = new LinearGradientPaint(start, end, dist, colors);
      mp = new MattePainter(p);
      cparzt = new CompoundPainter(new Painter[] { (Painter)mp });
      start = new Point2D.Float(0.0F, 0.0F);
      end = new Point2D.Float(0.0F, 40.0F);
      dist = new float[] { 0.0F, 1.0F };
      colors = new Color[] { Colors.PiOrange.alpha(0.5F), Color.WHITE };
      p = new LinearGradientPaint(start, end, dist, colors);
      mp = new MattePainter(p);
      cpscanner = new CompoundPainter(new Painter[] { (Painter)mp });
      
      NativeInterface.initialize();
      JWebBrowser.useXULRunnerRuntime();
      NativeInterface.open();
      new Thread(new Runnable() {
          @Override
          public void run() {
              if (!NativeInterface.isEventPumpRunning())
                  NativeInterface.runEventPump();
          }
      }).start();

      final RehaHMK xapplication = application;
      (new SwingWorker<Void, Void>() {
          protected Void doInBackground() throws Exception {
            xapplication.starteDB();
            long zeit = System.currentTimeMillis();
            while (!RehaHMK.DbOk) {
              try {
                Thread.sleep(20L);
                if (System.currentTimeMillis() - zeit > 10000L)
                  break; 
              } catch (InterruptedException e) {
                e.printStackTrace();
              } 
            } 
            if (!RehaHMK.DbOk) {
              JOptionPane.showMessageDialog(null, "Datenbank konnte nicht geöffnet werden!\nReha-Sql kann nicht gestartet werden");
              System.exit(0);
            } 
            try {
              RehaHMK.starteOfficeApplication();
              RehaHMK.ArztGruppenInit();
            } catch (Exception ex) {
              ex.printStackTrace();
            } 
            return null;
          }
        }).execute();
      fuelleReferenz();
      application.getJFrame();  
    } else {
      JOptionPane.showMessageDialog(null, "Keine Datenbankparameter übergeben\nReha-Sql kann nicht gestartet werden");
      System.exit(0);
    } 
  }
  
    public static void ArztGruppenInit() {
        INIFile inif = new INIFile(proghome + "ini/" + aktIK + "/arzt.ini");
        int ags;
        if ((ags = inif.getIntegerProperty("ArztGruppen", "AnzahlGruppen")
                       .intValue()) > 0) {
            arztGruppen = new String[ags];
            for (int i = 0; i < ags; i++)
                arztGruppen[i] = inif.getStringProperty("ArztGruppen", "Gruppe" + Integer.valueOf(i + 1)
                                                                                         .toString());
        }
    }
  
  public static void fuelleReferenz() {
    String[] diszis = { "Physio", "Massage", "Ergo", "Logo", "Podo" };  // <- ToDo: umstellen!
    INIFile inif = new INIFile(proghome + "ini/" + aktIK + "/pgreferenz.ini");
    for (int i = 0; i < diszis.length; i++)
      pgReferenz.put(diszis[i], inif.getIntegerProperty("HMR_ReferenzPreisGruppe", diszis[i])); 
    icons.put("browser", new ImageIcon(proghome + "icons/internet-web-browser.png"));
    icons.put("key", new ImageIcon(proghome + "icons/entry_pk.gif"));
    icons.put("lupe", new ImageIcon(proghome + "icons/mag.png"));
    icons.put("erde", new ImageIcon(proghome + "icons/earth.gif"));
    icons.put("inaktiv", new ImageIcon(proghome + "icons/inaktiv.png"));
    icons.put("green", new ImageIcon(proghome + "icons/green.png"));
    icons.put("rot", new ImageIcon(proghome + "icons/red.png"));
    Image ico = (new ImageIcon(proghome + "icons/blitz.png")).getImage().getScaledInstance(26, 26, Image.SCALE_SMOOTH);
    icons.put("blitz", new ImageIcon(ico));
    icons.put("strauss", new ImageIcon(proghome + "icons/strauss_150.png"));
  }
  
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
    this.jFrame = new JFrame() {
        private static final long serialVersionUID = 1L;
        
        public void setVisible(boolean visible) {
          if (getState() != 0)
            setState(0); 
          if (!visible || !isVisible())
            super.setVisible(visible); 
          if (visible) {
            int state = getExtendedState();
            state &= ~JFrame.ICONIFIED;
            setExtendedState(state);
            setAlwaysOnTop(true);
            super.toFront();
            requestFocus();
            setAlwaysOnTop(false);
          } 
        }
        
        public void toFront() {
          super.setVisible(true);
          int state = getExtendedState();
          state &= ~JFrame.ICONIFIED;
          setExtendedState(state);
          setAlwaysOnTop(true);
          super.toFront();
          requestFocus();
          setAlwaysOnTop(false);
        }
      };
    try {
      this.rehaReverseServer = new RehaReverseServer(7000);
    } catch (Exception ex) {
      this.rehaReverseServer = null;
    } 
    this.sqlInfo.setFrame(this.jFrame);
    this.jFrame.addWindowListener(this);
    this.jFrame.setSize(1020, 675);
    this.jFrame.setTitle("Thera-Pi  Heilmittelkatalog  [IK: " + aktIK + "] " + "[Server-IP: " + "dbIpAndName" + "]"); //FIXME: info dpipandname
    this.jFrame.setDefaultCloseOperation(3);
    this.jFrame.setLocationRelativeTo((Component)null);
    this.jFrame.getContentPane().add((Component)new RehaHMKTab());
    this.jFrame.setVisible(true);
    thisFrame = this.jFrame;
    try {
      (new SocketClient()).setzeRehaNachricht(rehaReversePort, "AppName#RehaHMK#" + Integer.toString(xport));
      (new SocketClient()).setzeRehaNachricht(rehaReversePort, "RehaHMK#" + RehaIOMessages.IS_STARTET);
      SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              (new SocketClient()).setzeRehaNachricht(RehaHMK.rehaReversePort, "RehaHMK#" + RehaIOMessages.NEED_AKTUSER);
            }
          });
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(null, "Fehler in der Socketkommunikation");
    } 
    return this.jFrame;
  }
  
  public RehaHMK getInstance() {
    thisClass = this;
    return this;
  }
  
  public void windowOpened(WindowEvent e) {}
  
  public void windowClosing(WindowEvent e) {
    if (thisClass.conn != null)
      try {
        thisClass.conn.close();
        System.out.println("Datenbankverbindung wurde geschlossen");
      } catch (SQLException ex) {
        ex.printStackTrace();
      }  
    if (thisClass.rehaReverseServer != null)
      try {
        (new SocketClient()).setzeRehaNachricht(rehaReversePort, "RehaHMK#" + RehaIOMessages.IS_FINISHED);
        this.rehaReverseServer.serv.close();
      } catch (Exception ex) {
        ex.printStackTrace();
      }  
    System.exit(0);
  }
  
  public void windowClosed(WindowEvent e) {}
  
  public void windowIconified(WindowEvent e) {}
  
  public void windowDeiconified(WindowEvent e) {}
  
  public void windowActivated(WindowEvent e) {}
  
  public void windowDeactivated(WindowEvent e) {}
  
  public void starteDB() {
    DatenbankStarten dbstart = new DatenbankStarten();
    dbstart.run();
  }
  
  public static void stoppeDB() {
    try {
      thisClass.conn.close();
      thisClass.conn = null;
    } catch (SQLException e) {
      e.printStackTrace();
    } 
  }
  
  final class DatenbankStarten implements Runnable {
      private void StarteDB() {
          final RehaHMK obj = RehaHMK.thisClass;


          try {

              obj.conn = new DatenquellenFactory(aktIK).createConnection();
              RehaHMK.thisClass.sqlInfo.setConnection(obj.conn);
              RehaHMK.DbOk = true;
              System.out.println("Datenbankkontakt hergestellt");
          } catch (final SQLException ex) {
              JOptionPane.showMessageDialog(null,
                      "Fehler im Datenbankkontakt\n für IK " + aktIK);
              System.out.println("SQLException: " + ex.getMessage());
              System.out.println("SQLState: " + ex.getSQLState());
              System.out.println("VendorError: " + ex.getErrorCode());
              RehaHMK.DbOk = false;

          }
          return;
      }

    @Override
    public void run() {
        StarteDB();
    }
    
  }
  
  public static void starteOfficeApplication() {
    File f = new File(officeProgrammPfad);
    boolean exists = f.isDirectory();
    if (!exists)
      JOptionPane.showMessageDialog(null, "Fehler!!!!!\n\nDer von Ihnen verwendete OpenOffice-Pfad lautet:\n" + 
          officeProgrammPfad + "\n\n" + 
          "Dieser Pfad existiert: NEIN"); 
    try {
    	new OOService().start(officeNativePfad, officeProgrammPfad);
      officeapplication = new OOService().getOfficeapplication();
    } catch (OfficeApplicationException e1) {
      e1.printStackTrace();
    } catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
  }
}
