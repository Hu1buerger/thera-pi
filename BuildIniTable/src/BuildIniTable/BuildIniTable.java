package BuildIniTable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.INIFile;
import CommonTools.SqlInfo;
import environment.Path;

/**
 * (Currently) Standalone tool um *.ini Dateien in die Datenbank zu übertragen
 */
public class BuildIniTable {
    private static final Logger logger = LoggerFactory.getLogger(BuildIniTable.class);

    private WindowListener windowListener = new WindowAdapter() {

        public void windowClosing(WindowEvent e) {
            if (BuildIniTable.this.conn != null) {
                try {
                    BuildIniTable.this.conn.close();
                    logger.info("Connection geschlossen");
                } catch (SQLException whatever) {
                    logger.error("I'm dying but still", whatever);
                }
            }
        }

    };
    public static BuildIniTable thisClass;

    SqlInfo sqlInfo;
    JFrame jFrame;
    Vector<String> mandantIkvec = new Vector<>();
    Vector<String> mandantNamevec = new Vector<>();

    Connection conn;

    public boolean DbOk;
    public String pfadzurmandini;
    public String pfadzurini;
    public int anzahlmandanten;
    public String[] inis = new String[] { "preisgruppen.ini", "terminkalender.ini", "gruppen.ini", "icons.ini",
            "fristen.ini", "color.ini", "dta301.ini", "gutachten.ini", "ktraeger.ini", "sqlmodul.ini",
            "thbericht.ini" };

    /** @VisibleForTesting */
    public BuildIniTable() {
        this.pfadzurmandini = Path.Instance.getProghome() + "ini/mandanten.ini";
        this.pfadzurini = this.pfadzurmandini.replace("mandanten.ini", "");
    }

    /** @VisibleForTesting */
    void setPfadzurmandini(String pfadZurMandIni) {
        this.pfadzurmandini = pfadZurMandIni;
    }

    /** @VisibleForTesting */
    String getPfadzurmandini() {
        return this.pfadzurmandini;
    }

    public static void main(String[] args) {
        BuildIniTable application = new BuildIniTable();
        application.sqlInfo = new SqlInfo();
        application.getJFrame();
    }

    public JFrame getJFrame() {
        setlookandfeel();
        datensicherungoderende();

        thisClass = this;

        pfadzurmandini = mandantTesten();

        pfadzurini = pfadzurmandini.replace("mandanten.ini", "");
        INIFile ini = new INIFile(pfadzurmandini);
        anzahlmandanten = Integer.parseInt(ini.getStringProperty("TheraPiMandanten", "AnzahlMandanten"));
        for (int i = 0; i < anzahlmandanten; i++) {
            mandantIkvec.add(ini.getStringProperty("TheraPiMandanten", "MAND-IK" + (i + 1)));
            mandantNamevec.add(ini.getStringProperty("TheraPiMandanten", "MAND-NAME" + (i + 1)));
        }
        jFrame = new JFrame();
        sqlInfo.setFrame(this.jFrame);
        jFrame.addWindowListener(windowListener);
        jFrame.setSize(600, 600);
        jFrame.setPreferredSize(new Dimension(600, 600));
        jFrame.setTitle("Thera-Pi  INI-Tabelle(n) erzeugen");
        jFrame.setDefaultCloseOperation(3);
        jFrame.setLocationRelativeTo((Component) null);
        ProcessPanel pan = new ProcessPanel();
        jFrame.getContentPane()
              .setLayout(new BorderLayout());
        jFrame.getContentPane()
              .add((Component) pan, "Center");
        jFrame.setVisible(true);
        return this.jFrame;
    }

    private void datensicherungoderende() {
        int frage = JOptionPane.showConfirmDialog(null,
                "Haben Sie von Ihren Datenbanken " + "eine Sicherungskopie erstellt?",
                "Achtung wichtige Benutzeranfrage", 0);
        if (frage != 0) {
            System.exit(0);
        }
    }

    private void setlookandfeel() {
        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            logger.debug("LaF not set", e);
        }
    }

    /**
     * Will test whether mandanten.ini exists in default progHome/ini/ directory. If
     * not, the user will be presented a file-browser to choose one. Calls
     * system.exit(0). Should the former also fail (this should be reconsidered -
     * exit status 0 often indicates OK - <> 0 should be error).
     *
     * @return String - path to and including mandanten.ini
     */
    // @VisisbleForTesting
    String mandantTesten() {
        String mandini = this.pfadzurmandini;
        logger.debug("Vorhandener mand-ini-Eintrag: " + mandini);
        if (!new File(mandini).exists()) {
            return frageBenutzerNachAlternativerMandantenIni(mandini);
        }
        return mandini;
    }

    private String frageBenutzerNachAlternativerMandantenIni(String mandini) {
        JOptionPane.showMessageDialog(null,
                "Das System kann die mandanten.ini nicht "
                        + "finden!\nBitte navigieren Sie in das Verzeichnis in dem sich die\n"
                        + "mandanten.ini befindet und wählen Sie die mandanten.ini aus!");
        String sret = dateiDialog(mandini);
        if (!sret.endsWith("mandanten.ini")) {
            JOptionPane.showMessageDialog(null,
                    "Sie haben die falsche(!!!) Datei ausgewählt, " + "das Programm wird beendet!");
            // ToDo: find elegant way to exit, if this is integrated into Thera-pi, we kill
            // all
            System.exit(0);
        }
        return sret;
    }

    private String dateiDialog(String pfad) {
        JFileChooser chooser = new JFileChooser("Verzeichnis wählen");
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileFilter(new FileFilter() {

            @Override
            public String getDescription() {
                return "mandantenini";
            }

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                System.out.println(f.getName());
                return "mandanten.ini".equals(f.getName()
                                               .toLowerCase());
            }
        });
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        final File file = new File(pfad);

        chooser.setCurrentDirectory(file);

        chooser.setVisible(true);
        String sret;
        int result = chooser.showOpenDialog((Component) null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File inputVerzFile = chooser.getSelectedFile();
            String inputVerzStr = inputVerzFile.getPath();
            if ("".equals(inputVerzFile.getName()
                                       .trim())) {
                sret = "";
            } else {
                sret = inputVerzStr.trim()
                                   .replace("\\", "/");
            }
        } else {
            sret = "";
        }
        chooser.setVisible(false);
        return sret;
    }
}
