package theraPi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXPanel;

import CommonTools.INIFile;
import environment.Path;

public class TheraPi {

    static JDialog jDiag = null;
    JXPanel contentPanel = null;
    static JLabel standDerDingelbl = null;
    static boolean socketoffen = false;
    public static String proghome;
    public static int AnzahlMandanten;
    public static int AuswahlImmerZeigen;
    public static int DefaultMandant;
    public static int LetzterMandant;
    public static String StartMandant;
    public static Vector<String[]> mandvec = new Vector<String[]>();
    public static Vector<Vector<String>> updatefiles = new Vector<Vector<String>>();

    public static void main(String[] args) {
        setLookAndFeel();

        proghome = Path.Instance.getProghome();
        System.out.println("Programmverzeichnis = " + proghome);
        INIFile inif = new INIFile(proghome + "ini/mandanten.ini");
        int AnzahlMandanten = inif.getIntegerProperty("TheraPiMandanten", "AnzahlMandanten");
        int AuswahlImmerZeigen = inif.getIntegerProperty("TheraPiMandanten", "AuswahlImmerZeigen");
        int DefaultMandant = inif.getIntegerProperty("TheraPiMandanten", "DefaultMandant");
        if (AuswahlImmerZeigen == 0) {
            String s1 = inif.getStringProperty("TheraPiMandanten", "MAND-IK" + DefaultMandant);
            String s2 = inif.getStringProperty("TheraPiMandanten", "MAND-NAME" + DefaultMandant);

            String[] mand = { null, null };
            mand[0] = s1;
            mand[1] = s2;
            mandvec.add(mand);
            StartMandant = s1 + '@' + s2;
            RehaStarter rst = new RehaStarter();
            rst.execute();
            try {
                int i = rst.get();
                if (i == 1) {
                    Thread.sleep(10000);
                    System.out.println("Rückgabewert = 1");
                    System.exit(0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {

            for (int i = 0; i < AnzahlMandanten; i++) {
                String[] mand = { null, null };
                mand[0] = new String(inif.getStringProperty("TheraPiMandanten", "MAND-IK" + (i + 1)));
                mand[1] = new String(inif.getStringProperty("TheraPiMandanten", "MAND-NAME" + (i + 1)));
                mandvec.add(mand);
            }
            TheraPi application = new TheraPi();
            jDiag = application.getDialog();

            jDiag.validate();
            jDiag.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            jDiag.setVisible(true);
        }

    }

    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
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

    private JDialog getDialog() {
        contentPanel = new JXPanel(new BorderLayout());
        contentPanel.setPreferredSize(new Dimension(400, 300));
        contentPanel.add(new SplashInhalt(), BorderLayout.CENTER);
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        JXPanel textPanel = new JXPanel(new BorderLayout());
        textPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        textPanel.setBackground(Color.WHITE);
        textPanel.setPreferredSize(new Dimension(0, 15));
        standDerDingelbl = new JLabel("OpenSource-Projekt Reha-xSwing", JLabel.CENTER);
        standDerDingelbl.setFont(new Font("Arial", 8, 10));
        textPanel.add(standDerDingelbl, BorderLayout.CENTER);
        contentPanel.add(textPanel, BorderLayout.SOUTH);
        contentPanel.validate();

        JDialog xDiag = new JDialog();
        xDiag.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        xDiag.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        xDiag.setUndecorated(true);
        xDiag.setContentPane(contentPanel);
        xDiag.setSize(450, 200);
        xDiag.setLocationRelativeTo(null);
        xDiag.validate();
        xDiag.pack();
        return xDiag;
    }
}

class RehaStarter extends SwingWorker<Integer, Void> {

    @Override
    protected Integer doInBackground() throws Exception {
        try {

            String programm = TheraPi.proghome + "Reha.jar";
            System.out.println("In TheraPi.jar Programmstart = " + programm);
            String mandik = TheraPi.StartMandant.split("@")[0];
            INIFile minif = new INIFile(TheraPi.proghome + "ini/" + mandik + "/rehajava.ini");
            // FIXME: memsize of JVM should not be hard coded
            String memsizemin = "-Xms128m ";
            String memsizemax = "-Xmx256m ";
            String dummy = minif.getStringProperty("SystemIntern", "MinMemSize");
            if (dummy != null) {
                memsizemin = "-Xms" + dummy + " ";
            } else {
                minif.setStringProperty("SystemIntern", "MinMemSize", "128m", null);
                minif.save();
            }
            dummy = minif.getStringProperty("SystemIntern", "MaxMemSize");
            if (dummy != null) {
                memsizemax = "-Xmx" + String.valueOf(dummy) + " ";
            } else {
                minif.setStringProperty("SystemIntern", "MaxMemSize", "256m", null);
                minif.save();
            }
            String start = new String("java -jar -Djava.net.preferIPv4Stack=true " + memsizemin + memsizemax
                    + TheraPi.proghome + "Reha.jar " + TheraPi.StartMandant);

            System.out.println("Kommando ist " + start);
            Runtime.getRuntime().exec(start);
            System.out.println("Reha gestartet");
            System.out.println(start);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 1;

    }
}
