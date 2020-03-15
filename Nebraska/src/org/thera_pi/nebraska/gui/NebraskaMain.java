package org.thera_pi.nebraska.gui;

import java.awt.Dimension;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import CommonTools.INIFile;
import CommonTools.INITool;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationRuntime;
import crypt.Verschluesseln;

public class NebraskaMain {
    public static IOfficeApplication officeapplication;
    public static HashMap<String, String> hmZertifikat = new HashMap<String, String>();
    public static JFrame jf;
    public static NebraskaJTabbedPaneOrganizer jtbo;
    public static String OPENOFFICE_HOME = null;
    public static String OPENOFFICE_JARS = null;
    public static String progHome = null;
    public static Vector<Vector<String>> keyStoreParameter = new Vector<Vector<String>>();
    public static String therapiIK = null;

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if (System.getProperty("os.name")
                  .contains("Windows")) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (ClassNotFoundException e) {

                e.printStackTrace();
            } catch (InstantiationException e) {

                e.printStackTrace();
            } catch (IllegalAccessException e) {

                e.printStackTrace();
            } catch (UnsupportedLookAndFeelException e) {

                e.printStackTrace();
            }
        }
        progHome = System.getProperty("user.dir");
        String INI_FILE = "";
        if (System.getProperty("os.name")
                  .contains("Windows")) {
            INI_FILE = progHome + File.separator + "nebraska_windows.conf";
        } else if (System.getProperty("os.name")
                         .contains("Linux")) {
            INI_FILE = progHome + File.separator + "nebraska_linux.conf";
        } else if (System.getProperty("os.name")
                         .contains("String für MaxOSX????")) {
            INI_FILE = progHome + File.separator + "nebraska_mac.conf";
        }
        Verschluesseln man = Verschluesseln.getInstance();

        if (args.length > 0) {
            try {
                INIFile ini = new INIFile(INI_FILE);
                int anzahl = ini.getIntegerProperty("KeyStores", "KeyStoreAnzahl");
                Vector<String> dummy = new Vector<String>();
                boolean speichern = false;
                for (int i = 0; i < anzahl; i++) {

                    String pw = "";
                    pw = ini.getStringProperty("KeyStores", "KeyStorePw" + Integer.toString(i + 1));
                    if (pw.length() <= 6) {
                        ini.setStringProperty("KeyStores", "KeyStorePw" + Integer.toString(i + 1), man.encrypt(pw),
                                null);
                        pw = ini.getStringProperty("KeyStores", "KeyStoreKeyPw" + Integer.toString(i + 1));
                        ini.setStringProperty("KeyStores", "KeyStoreKeyPw" + Integer.toString(i + 1), man.encrypt(pw),
                                null);
                        speichern = true;
                    }

                    dummy.clear();
                    dummy.trimToSize();
                    dummy.add(ini.getStringProperty("KeyStores", "KeyStoreFile" + Integer.toString(i + 1)));
                    dummy.add(man.decrypt(ini.getStringProperty("KeyStores", "KeyStorePw" + Integer.toString(i + 1))));
                    dummy.add(ini.getStringProperty("KeyStores", "KeyStoreAlias" + Integer.toString(i + 1)));
                    dummy.add(
                            man.decrypt(ini.getStringProperty("KeyStores", "KeyStoreKeyPw" + Integer.toString(i + 1))));
                    keyStoreParameter.add((Vector<String>) dummy.clone());
                }
                if (speichern) {
                    ini.save();
                }

                ini = new INIFile(progHome + "/ini/" + args[0] + "/rehajava.ini");
                OPENOFFICE_HOME = ini.getStringProperty("OpenOffice.org", "OfficePfad");
                OPENOFFICE_JARS = ini.getStringProperty("OpenOffice.org", "OfficeNativePfad");

                therapiIK = String.valueOf(args[0]);
                INITool.init(progHome + "/ini/" + therapiIK + "/");
                System.out.println("Öffne mit Parameter " + therapiIK);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else {
            int frage = JOptionPane.showConfirmDialog(null,
                    "Falls Sie einen Zertifikatsrequest für die ITSG erzeugen wollen,\n"
                            + "muß Nebraska über Thera-Pi gestartet werden!\n\n"
                            + "Wollen Sie Nebraska trotzdem starten? (nicht empfohlen!!)",
                    "Wichtige Benutzerinfo", JOptionPane.YES_NO_OPTION);
            if (frage != JOptionPane.YES_OPTION) {
                System.exit(0);
            }
            INIFile ini = new INIFile(INI_FILE);
            int anzahl = ini.getIntegerProperty("KeyStores", "KeyStoreAnzahl");
            Vector<String> dummy = new Vector<String>();
            boolean speichern = false;
            for (int i = 0; i < anzahl; i++) {
                try {
                    String pw = "";
                    pw = ini.getStringProperty("KeyStores", "KeyStorePw" + Integer.toString(i + 1));
                    if (pw.length() <= 6) {
                        ini.setStringProperty("KeyStores", "KeyStorePw" + Integer.toString(i + 1), man.encrypt(pw),
                                null);
                        pw = ini.getStringProperty("KeyStores", "KeyStoreKeyPw" + Integer.toString(i + 1));
                        ini.setStringProperty("KeyStores", "KeyStoreKeyPw" + Integer.toString(i + 1), man.encrypt(pw),
                                null);
                        speichern = true;
                    }
                    if (ini.getStringProperty("KeyStores", "KeyStoreHash256" + Integer.toString(i + 1)) == null) {

                    }

                    dummy.clear();
                    dummy.trimToSize();
                    dummy.add(ini.getStringProperty("KeyStores", "KeyStoreFile" + Integer.toString(i + 1)));
                    dummy.add(man.decrypt(ini.getStringProperty("KeyStores", "KeyStorePw" + Integer.toString(i + 1))));
                    dummy.add(ini.getStringProperty("KeyStores", "KeyStoreAlias" + Integer.toString(i + 1)));
                    dummy.add(
                            man.decrypt(ini.getStringProperty("KeyStores", "KeyStoreKeyPw" + Integer.toString(i + 1))));
                    keyStoreParameter.add((Vector<String>) dummy.clone());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (speichern) {
                ini.save();
            }
            OPENOFFICE_HOME = ini.getStringProperty("Pfade", "OPENOFFICE_HOME");
            OPENOFFICE_JARS = ini.getStringProperty("Pfade", "OPENOFFICE_JARS");
            // ini.save();
        }

        NebraskaMain nebMain = new NebraskaMain();
        nebMain.getFrame();

    }

    private JFrame getFrame() throws Exception {
        jf = new JFrame();
        jf.setPreferredSize(new Dimension(1000, 775));
        jf.setTitle("Nebraska");
        jf.setLocation(200, 200);
        jtbo = new NebraskaJTabbedPaneOrganizer();
        jf.setContentPane(jtbo);
        jtbo.setHeader(0);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.pack();
        jtbo.erstTest();
        jf.setVisible(true);

        starteOfficeApplication();
        //new Constants();    // Werte scheinen unbenutzt
        return jf;
    }

    public static void starteOfficeApplication() throws Exception {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                final String OPEN_OFFICE_ORG_PATH = OPENOFFICE_HOME;
                final String OPEN_OFFICE_ORG_NATIVE_PATH = OPENOFFICE_JARS;
                Map<String, String> config = new HashMap<String, String>();
                config.put(IOfficeApplication.APPLICATION_HOME_KEY, OPEN_OFFICE_ORG_PATH);
                config.put(IOfficeApplication.APPLICATION_TYPE_KEY, IOfficeApplication.LOCAL_APPLICATION);
                System.setProperty(IOfficeApplication.NOA_NATIVE_LIB_PATH, OPEN_OFFICE_ORG_NATIVE_PATH);
                officeapplication = OfficeApplicationRuntime.getApplication(config);
                officeapplication.activate();
                System.out.println("Open-Office wurde gestartet");
                System.out.println("Open-Office-Typ: " + officeapplication.getApplicationType());
                return null;
            }
        }.execute();
    }

}
