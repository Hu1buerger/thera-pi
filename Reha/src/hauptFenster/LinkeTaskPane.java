package hauptFenster;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TooManyListenersException;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.jdesktop.swingx.plaf.windows.WindowsTaskPaneUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.therapi.reha.patient.AktuelleRezepte;

import CommonTools.SqlInfo;
import CommonTools.ini.INIFile;
import CommonTools.ini.Settings;
import ag.ion.bion.officelayer.text.ITextDocument;
import environment.Path;
import events.PatStammEvent;
import events.PatStammEventClass;
import systemEinstellungen.ImageRepository;
import systemEinstellungen.SystemConfig;
import terminKalender.TerminFenster;
import terminKalender.TerminFenster.Ansicht;
import umfeld.Betriebsumfeld;
import wecker.Wecker;

public class LinkeTaskPane extends JXPanel {

    private static final long serialVersionUID = 1L;
    private static JXTaskPaneContainer jxTPcontainer = null;
    public  JXTaskPane patientenstammPanel = null;
    public  JXTaskPane systemeinstellungpanel = null;
    public  JXTaskPane openofficePanel = null;
    public  JXTaskPane terminManagementPanel = null;
    public  JXTaskPane nuetzlichesPanel = null;
    public  JXTaskPane monatsuebersichtPanel = null;
    public  JXTaskPane userTaskPanel = null;
    private JXHyperlink oo1 = null;
    private JXHyperlink oo2 = null;
    public static boolean OOok = true;
    public static LinkeTaskPane thisClass = null;
    public static ITextDocument itestdocument = null;
    private ActionListener al;
    private String aktTag = "x";
    private String wahlTag = "y";
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    public static boolean mitUserTask = false;
    Logger logger = LoggerFactory.getLogger(LinkeTaskPane.class);
    private Connection conn;
    private ActionListener ltplistener;

    public LinkeTaskPane(Connection conn, ActionListener listener) {
        super();

        this.conn = conn;
        ltplistener = listener;
        mitUserTask = testUserTask();
        this.setBorder(null);
        this.setBackground(Color.WHITE);
        // this.eltern = Reha.instance;
        this.setPreferredSize(new Dimension(200, 500));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0D;
        gridBagConstraints.weighty = 1.0D;
        gridBagConstraints.insets = new Insets(5, 2, 5, 2);

        this.setLayout(new GridBagLayout());

        /**
         * Zuerst die Scrollpane generieren falls der sp�tere TaskPane-Container die
         * y-Dimension des Fensters �bersteigt.
         */
        JScrollPane jScrp = new JScrollPane();
        jScrp.setBorder(null);
        jScrp.setViewportBorder(null);
        jScrp.setBackground(Color.white);
        jScrp.setPreferredSize(new Dimension(180, 100));
        DropShadowBorder dropShadow = new DropShadowBorder(Color.BLACK, 10, 1, 5, false, true, true, true);
        jScrp.setBorder(dropShadow);
        jScrp.getVerticalScrollBar()
             .setUnitIncrement(15);
        /**
         * Jetz generieren wir den Taskpane-Container anschlie�end die TaskPanes
         */
        jxTPcontainer = new JXTaskPaneContainer();
        jxTPcontainer.setBackground(new Color(106, 130, 218));
        // jxTPcontainer.setPreferredSize(new Dimension(250,0));
        patientenstammPanel = getPatientenStamm();
        jxTPcontainer.add(patientenstammPanel);

        jxTPcontainer.add(getTerminKalender());

        jxTPcontainer.add(getOpenOfficeOrg());

        jxTPcontainer.add(getNuetzliches());

        jxTPcontainer.add(getSystemEinstellungen());
        jxTPcontainer.add(getMonatsUebersicht());

        if (mitUserTask) {
            jxTPcontainer.add((Component) getUserTasks());
        }
        /**
         * dann f�gen wir den TaskpaneContainer der ScrollPane hinzu
         */
        jScrp.setViewportView(jxTPcontainer);
        jScrp.setVisible(true);
        jScrp.revalidate();
        this.add(jScrp, gridBagConstraints);
        this.validate();
        thisClass = this;
    }

    /**
     * Task-Pane f�r den Patientenstamm erstellen
     *
     * @return
     */

    private JXTaskPane getPatientenStamm() {
        Image img = null;
        JXTaskPane tp1 = new JXTaskPane();
        UIManager.put("TaskPane.titleBackgroundGradientStart", Color.WHITE);
        UIManager.put("TaskPane.titleBackgroundGradientEnd", new Color(200, 212, 247));
        UIManager.put("TaskPane.background", new Color(214, 223, 247));
        UIManager.put("TaskPane.foreground", Color.BLUE);
        UIManager.put("TaskPane.useGradient", Boolean.TRUE);
        WindowsTaskPaneUI wui = new WindowsTaskPaneUI();
        tp1.setUI(wui);
        tp1.setTitle("Stammdaten");
        JXHyperlink jxLink = new JXHyperlink();
        jxLink.setText("Patienten und Rezepte");
        jxLink.setToolTipText("Strg+P = Patienten-/Rezeptstamm starten");
        img = new ImageIcon(Path.Instance.getProghome() + "icons/kontact_contacts.png").getImage()
                                                                                       .getScaledInstance(24, 24,
                                                                                               Image.SCALE_SMOOTH);
        jxLink.setIcon(new ImageIcon(img));
        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
        jxLink.addActionListener(ltplistener);
        jxLink.setEnabled(true);
        DropTarget dndt = new DropTarget();
        DropTargetListener dropTargetListener = new DropTargetAdapter() {

            @Override
            public void drop(DropTargetDropEvent e) {
                String mitgebracht = "";
                try {
                    Transferable tr = e.getTransferable();
                    DataFlavor[] flavors = tr.getTransferDataFlavors();
                    for (int i = 0; i < flavors.length; i++) {
                        mitgebracht = (String) tr.getTransferData(flavors[i]);
                    }
                    if (mitgebracht.indexOf("°") >= 0) {
                        if (!mitgebracht.split("°")[0].contains("TERMDAT")) {
                            return;
                        }
                        doPatientDrop(mitgebracht.split("°")[2].trim());
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
        jxLink.setDropTarget(dndt);

        tp1.add(jxLink);
        jxLink = new JXHyperlink();
        jxLink.setText("Ärzte");
        jxLink.setActionCommand("Arztstamm");
        jxLink.setToolTipText("Strg+A = Arztstamm starten");
        img = new ImageIcon(Path.Instance.getProghome() + "icons/system-users.png").getImage()
                                                                                   .getScaledInstance(24, 24,
                                                                                           Image.SCALE_SMOOTH);
        jxLink.setIcon(new ImageIcon(img));
        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
        jxLink.addActionListener(ltplistener);
        tp1.add(jxLink);
        jxLink = new JXHyperlink();
        jxLink.setText("Krankenkassen");
        jxLink.setToolTipText("Strg+K = Kassenstamm starten");
        img = new ImageIcon(Path.Instance.getProghome() + "icons/krankenkasse.png").getImage()
                                                                                   .getScaledInstance(24, 24,
                                                                                           Image.SCALE_SMOOTH);
        jxLink.setIcon(new ImageIcon(img));
        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
        jxLink.addActionListener(ltplistener);
        tp1.add(jxLink);
        tp1.setCollapsed(SystemConfig.taskPaneCollapsed[0]);
        return tp1;
    }

    private JXTaskPane getTerminKalender() {
        Image img = null;
        terminManagementPanel = new JXTaskPane();
        UIManager.put("TaskPane.titleBackgroundGradientStart", Color.WHITE);
        UIManager.put("TaskPane.titleBackgroundGradientEnd", new Color(200, 212, 247));
        UIManager.put("TaskPane.background", new Color(214, 223, 247));
        UIManager.put("TaskPane.useGradient", Boolean.TRUE);
        WindowsTaskPaneUI wui = new WindowsTaskPaneUI();
        terminManagementPanel.setUI(wui);
        terminManagementPanel.setTitle("Termin-Management");
        terminManagementPanel.setIcon(new ImageIcon(Path.Instance.getProghome() + "icons/table_mode.png"));
        JXHyperlink jxLink = new JXHyperlink();
        jxLink.setText("Terminkalender starten");
        jxLink.setToolTipText("Strg+T = Terminkalender starten");
        img = new ImageIcon(Path.Instance.getProghome() + "icons/evolution-addressbook.png").getImage()
                                                                                            .getScaledInstance(24, 24,
                                                                                                    Image.SCALE_SMOOTH);
        jxLink.setIcon(new ImageIcon(img));
        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
        jxLink.addActionListener(ltplistener);
        terminManagementPanel.add(jxLink);
        jxLink = new JXHyperlink();
        DropTarget dndt = new DropTarget();
        DropTargetListener dropTargetListener = new DropTargetAdapter() {

            @Override
            public void drop(DropTargetDropEvent e) {
                String mitgebracht = "";
                try {
                    Transferable tr = e.getTransferable();
                    DataFlavor[] flavors = tr.getTransferDataFlavors();
                    for (int i = 0; i < flavors.length; i++) {
                        mitgebracht = (String) tr.getTransferData(flavors[i]);
                    }
                    if (mitgebracht.indexOf("°") >= 0) {
                        if (!mitgebracht.split("°")[0].contains("TERMDAT")) {
                            return;
                        }
                        Reha.instance.progLoader.ProgRoogleFenster(0, mitgebracht);
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
        jxLink.setDropTarget(dndt);
        jxLink.setName("Rugl");
        String srugl = "<html><font color='#000000'>[</font><font color='#0000ff'>R</font><font color='#ff0000'>u</font>"
                + "<font color='#00ffff'><b>:</b></font><font color='#0000ff'>g</font><font color='#00ff00'>l</font>"
                + "<font color='#000000'>]</font>&nbsp;- Die Terminsuchmaschine";
        jxLink.setText(srugl);
        jxLink.setToolTipText("Strg+R = [Ru:gl] starten");
        img = new ImageIcon(Path.Instance.getProghome() + "icons/orca.png").getImage()
                                                                           .getScaledInstance(24, 24,
                                                                                   Image.SCALE_SMOOTH);
        jxLink.setIcon(new ImageIcon(img));
        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
        jxLink.setActionCommand("[Ru:gl] - Die Terminsuchmaschine");
        jxLink.addActionListener(ltplistener);
        terminManagementPanel.add(jxLink);
        jxLink = new JXHyperlink();
        jxLink.setText("Wochenarbeitszeiten definieren");
        img = new ImageIcon(Path.Instance.getProghome() + "icons/alacarte.png").getImage()
                                                                               .getScaledInstance(24, 24,
                                                                                       Image.SCALE_SMOOTH);
        jxLink.setIcon(new ImageIcon(img));
        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
        jxLink.addActionListener(ltplistener);
        terminManagementPanel.add(jxLink);
        jxLink = new JXHyperlink();
        jxLink.setText("Akutliste - kurzfristige Termine");
        jxLink.setActionCommand("Akutliste");
        img = new ImageIcon(Path.Instance.getProghome() + "icons/vcalendar.png").getImage()
                                                                                .getScaledInstance(24, 24,
                                                                                        Image.SCALE_SMOOTH);
        jxLink.setIcon(new ImageIcon(img));
        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
        jxLink.addActionListener(ltplistener);
        terminManagementPanel.add(jxLink);

        jxLink = new JXHyperlink();
        jxLink.setText("Thera-\u03C0" + " Erinnerungs-System");
        img = new ImageIcon(Path.Instance.getProghome() + "icons/chronometer.png").getImage()
                                                                                  .getScaledInstance(24, 24,
                                                                                          Image.SCALE_SMOOTH);
        jxLink.setIcon(new ImageIcon(img));
        jxLink.setActionCommand("neuerwecker");
        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
        jxLink.addActionListener(ltplistener);
        dndt = new DropTarget();
        dropTargetListener = new DropTargetAdapter() {

            @Override
            public void drop(DropTargetDropEvent e) {
                String mitgebracht = "";
                try {
                    Transferable tr = e.getTransferable();
                    DataFlavor[] flavors = tr.getTransferDataFlavors();
                    for (int i = 0; i < flavors.length; i++) {
                        mitgebracht = (String) tr.getTransferData(flavors[i]);
                    }
                    if (mitgebracht.indexOf("°") >= 0) {
                        if (!mitgebracht.split("°")[0].contains("TERMDAT")) {
                            return;
                        }
                        doWeckerDrop(mitgebracht);
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
        jxLink.setDropTarget(dndt);
        jxLink.setName("Rugl");

        terminManagementPanel.add(jxLink);
        terminManagementPanel.setCollapsed(SystemConfig.taskPaneCollapsed[1]);
        return terminManagementPanel;
    }

    private JXTaskPane getOpenOfficeOrg() {
        openofficePanel = new JXTaskPane();
        UIManager.put("TaskPane.titleBackgroundGradientStart", Color.WHITE);
        UIManager.put("TaskPane.titleBackgroundGradientEnd", new Color(200, 212, 247));
        UIManager.put("TaskPane.background", new Color(214, 223, 247));
        UIManager.put("TaskPane.useGradient", Boolean.TRUE);
        WindowsTaskPaneUI wui = new WindowsTaskPaneUI();
        openofficePanel.setUI(wui);
        openofficePanel.setTitle("OpenOffice.org");
        openofficePanel.setIcon(SystemConfig.hmSysIcons.get("openoffice"));
        oo1 = new JXHyperlink();
        oo1.setText("OpenOffice-Writer");
        oo1.setClickedColor(new Color(0, 0x33, 0xFF));
        oo1.setIcon(SystemConfig.hmSysIcons.get("ooowriter"));
        oo1.addActionListener(ltplistener);
        openofficePanel.add(oo1);
        oo2 = new JXHyperlink();
        oo2.setIcon(SystemConfig.hmSysIcons.get("ooocalc"));
        oo2.setText("OpenOffice-Calc");
        oo2.setClickedColor(new Color(0, 0x33, 0xFF));
        oo2.addActionListener(ltplistener);
        openofficePanel.add(oo2);
        oo2 = new JXHyperlink();
        oo2.setIcon(SystemConfig.hmSysIcons.get("oooimpress"));
        oo2.setText("OpenOffice-Impress");
        oo2.setClickedColor(new Color(0, 0x33, 0xFF));
        oo2.addActionListener(ltplistener);
        openofficePanel.add(oo2);
        openofficePanel.setCollapsed(SystemConfig.taskPaneCollapsed[2]);
        return openofficePanel;
    }

    private JXTaskPane getNuetzliches() {
        Image img = null;
        nuetzlichesPanel = new JXTaskPane();
        UIManager.put("TaskPane.titleBackgroundGradientStart", Color.WHITE);
        UIManager.put("TaskPane.titleBackgroundGradientEnd", new Color(200, 212, 247));
        UIManager.put("TaskPane.background", new Color(214, 223, 247));
        UIManager.put("TaskPane.useGradient", Boolean.TRUE);
        WindowsTaskPaneUI wui = new WindowsTaskPaneUI();
        nuetzlichesPanel.setUI(wui);

        nuetzlichesPanel.setTitle("Nützliches...");
        JXHyperlink jxLink = new JXHyperlink();
        jxLink.setText("Thera-PI - Nachrichten");
        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
        img = new ImageIcon(Path.Instance.getProghome() + "icons/emblem-mail.png").getImage()
                                                                                  .getScaledInstance(24, 24,
                                                                                          Image.SCALE_SMOOTH);
        jxLink.setIcon(new ImageIcon(img));
        jxLink.addActionListener(ltplistener);
        nuetzlichesPanel.add(jxLink);
//        jxLink = new JXHyperlink();
//        jxLink.setText("Thera-PI - Browser");
//        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
//        img = new ImageIcon(Path.Instance.getProghome() + "icons/home.gif").getImage()
//                                                                           .getScaledInstance(24, 24,
//                                                                                   Image.SCALE_SMOOTH);
//        jxLink.setIcon(new ImageIcon(img));
//        jxLink.addActionListener(ltplistener);
//        nuetzlichesPanel.add(jxLink);
//        jxLink = new JXHyperlink();
//        jxLink.setText("piTool - ScreenShots");
//        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
//        img = new ImageIcon(Path.Instance.getProghome() + "icons/camera_unmount.png").getImage()
//                                                                                     .getScaledInstance(24, 24,
//                                                                                             Image.SCALE_SMOOTH);
//        jxLink.setIcon(new ImageIcon(img));
//        jxLink.setActionCommand("piTool");
//        jxLink.addActionListener(ltplistener);
//        nuetzlichesPanel.add(jxLink);
//        jxLink = new JXHyperlink();
//        jxLink.setText("piHelp - Hifetextgenerator");
//        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
//        img = new ImageIcon(Path.Instance.getProghome() + "icons/fragezeichenklein.png").getImage()
//                                                                                        .getScaledInstance(24, 24,
//                                                                                                Image.SCALE_SMOOTH);
//        jxLink.setIcon(new ImageIcon(img));
//        jxLink.setActionCommand("piHelp");
//        jxLink.addActionListener(ltplistener);
//        nuetzlichesPanel.add(jxLink);
        jxLink = new JXHyperlink();
        jxLink.setText("Textbausteine - Therapiebericht");
        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
        img = new ImageIcon(Path.Instance.getProghome() + "icons/abiword.png").getImage()
                                                                              .getScaledInstance(24, 24,
                                                                                      Image.SCALE_SMOOTH);
        jxLink.setIcon(new ImageIcon(img));
        jxLink.setActionCommand("piTextb");
        jxLink.addActionListener(ltplistener);
        nuetzlichesPanel.add(jxLink);

        jxLink = new JXHyperlink();
        jxLink.setText("Textbausteine - Gutachten");
        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
        img = new ImageIcon(Path.Instance.getProghome() + "icons/abiword.png").getImage()
                                                                              .getScaledInstance(24, 24,
                                                                                      Image.SCALE_SMOOTH);
        jxLink.setIcon(new ImageIcon(img));
        jxLink.setActionCommand("piArztTextb");
        jxLink.addActionListener(ltplistener);
        nuetzlichesPanel.add(jxLink);

        jxLink = new JXHyperlink();
        jxLink.setText("ICD-10 Recherche");
        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
        img = new ImageIcon(Path.Instance.getProghome() + "icons/mag.png").getImage()
                                                                          .getScaledInstance(24, 24,
                                                                                  Image.SCALE_SMOOTH);
        jxLink.setIcon(new ImageIcon(img));
        jxLink.setActionCommand("piIcd10");
        jxLink.addActionListener(ltplistener);
        nuetzlichesPanel.add(jxLink);
        JXHyperlink paypal = createPaypalLink();
        nuetzlichesPanel.add(paypal);

        File f = new File(Path.Instance.getProghome() + "QMHandbuch.jar");
        if (f.exists()) {
            jxLink = new JXHyperlink();
            jxLink.setText("QM-Handbuch");
            jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
            img = new ImageIcon(Path.Instance.getProghome() + "icons/abiword.png").getImage()
                                                                                  .getScaledInstance(24, 24,
                                                                                          Image.SCALE_SMOOTH);
            jxLink.setIcon(new ImageIcon(img));
            jxLink.setActionCommand("piQM");
            jxLink.addActionListener(ltplistener);
            nuetzlichesPanel.add(jxLink);
        }
        f = new File(Path.Instance.getProghome() + "QMAuswertung.jar");
        if (f.exists()) {
            jxLink = new JXHyperlink();
            jxLink.setText("QM-Auswertungen");
            jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
            img = new ImageIcon(Path.Instance.getProghome() + "icons/abiword.png").getImage()
                                                                                  .getScaledInstance(24, 24,
                                                                                          Image.SCALE_SMOOTH);
            jxLink.setIcon(new ImageIcon(img));
            jxLink.setActionCommand("piAW");
            jxLink.addActionListener(ltplistener);
            nuetzlichesPanel.add(jxLink);
        }

        nuetzlichesPanel.setCollapsed(SystemConfig.taskPaneCollapsed[3]);
        return nuetzlichesPanel;
    }

    private JXHyperlink createPaypalLink() {

        JXHyperlink paypal = new JXHyperlink();
        paypal.setText("Thera-Pi unterstützen");
        paypal.setClickedColor(new Color(0, 0x33, 0xFF));
        paypal.setIcon(ImageRepository.paypalIcon());
        paypal.setActionCommand("piIcd10");
        paypal.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    open(new URI(
                            "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=BDMYY86QLM9XG&source=url"));
                } catch (URISyntaxException e1) {
                    logger.error("url zu paypal fehlerhaft", e1);
                }

            }
        });
        return paypal;
    }

    private JXTaskPane getSystemEinstellungen() {
        Image img = null;
        systemeinstellungpanel = new JXTaskPane();
        UIManager.put("TaskPane.titleBackgroundGradientStart", Color.WHITE);
        UIManager.put("TaskPane.titleBackgroundGradientEnd", new Color(200, 212, 247));
        UIManager.put("TaskPane.background", new Color(214, 223, 247));
        UIManager.put("TaskPane.useGradient", Boolean.TRUE);
        WindowsTaskPaneUI wui = new WindowsTaskPaneUI();
        systemeinstellungpanel.setUI(wui);

        systemeinstellungpanel.setTitle("Systemeinstellungen");
        systemeinstellungpanel.setIcon(new ImageIcon(Path.Instance.getProghome() + "icons/pdf.gif"));
        JXHyperlink jxLink = new JXHyperlink();
        jxLink.setText("Benutzerverwaltung");
        img = new ImageIcon(Path.Instance.getProghome() + "icons/contact-new.png").getImage()
                                                                                  .getScaledInstance(24, 24,
                                                                                          Image.SCALE_SMOOTH);
        jxLink.setIcon(new ImageIcon(img));
        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
        jxLink.addActionListener(ltplistener);
        jxLink.setEnabled(true);
        systemeinstellungpanel.add(jxLink);
        jxLink = new JXHyperlink();
        jxLink.setText("System Initialisierung");
        img = new ImageIcon(Path.Instance.getProghome() + "icons/galternatives.png").getImage()
                                                                                    .getScaledInstance(24, 24,
                                                                                            Image.SCALE_SMOOTH);
        jxLink.setIcon(new ImageIcon(img));
        jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
        jxLink.addActionListener(ltplistener);
        systemeinstellungpanel.add(jxLink);
        systemeinstellungpanel.setCollapsed(SystemConfig.taskPaneCollapsed[4]);
        return systemeinstellungpanel;
    }

    private JXTaskPane getMonatsUebersicht() {
        monatsuebersichtPanel = new JXTaskPane();
        UIManager.put("TaskPane.titleBackgroundGradientStart", Color.WHITE);
        UIManager.put("TaskPane.titleBackgroundGradientEnd", new Color(200, 212, 247));
        UIManager.put("TaskPane.background", new Color(214, 223, 247));
        UIManager.put("TaskPane.useGradient", Boolean.TRUE);
        WindowsTaskPaneUI wui = new WindowsTaskPaneUI();
        monatsuebersichtPanel.setUI(wui);
        monatsuebersichtPanel.setTitle("Monatsübersicht");
        final JXMonthView monthView = new JXMonthView();
        monthView.setPreferredColumnCount(1);
        monthView.setPreferredRowCount(1);
        monthView.setTraversable(true);
        monthView.setShowingWeekNumber(true);
        al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (TerminFenster.getThisClass() != null) {
                    Date dat = monthView.getSelectionDate();
                    if (dat == null) {
                        return;
                    }
                    wahlTag = sdf.format(monthView.getSelectionDate());
                    if (wahlTag.equals(aktTag)) {
                        return;
                    }
                    aktTag = wahlTag;
                    Reha.instance.progLoader.ProgTerminFenster(Ansicht.NORMAL);
                    TerminFenster.getThisClass()
                                 .springeAufDatum(aktTag);
                } else {
                    Date dat = monthView.getSelectionDate();
                    if (dat == null) {
                        return;
                    }
                    wahlTag = sdf.format(monthView.getSelectionDate());
                    if (wahlTag.equals(aktTag)) {
                        return;
                    }
                    aktTag = wahlTag;
                    Reha.instance.progLoader.ProgTerminFenster(Ansicht.NORMAL);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            TerminFenster.getThisClass()
                                         .springeAufDatum(aktTag);

                        }
                    });
                }

            }
        };
        monthView.addActionListener(al);
        monatsuebersichtPanel.add(monthView);
        monatsuebersichtPanel.setCollapsed(true);
        monatsuebersichtPanel.setCollapsed(SystemConfig.taskPaneCollapsed[5]);
        return monatsuebersichtPanel;
    }

    private JXTaskPane getUserTasks() {
        userTaskPanel = new JXTaskPane();
        UIManager.put("TaskPane.titleBackgroundGradientStart", Color.WHITE);
        UIManager.put("TaskPane.titleBackgroundGradientEnd", new Color(200, 212, 247));
        UIManager.put("TaskPane.background", new Color(214, 223, 247));
        UIManager.put("TaskPane.useGradient", Boolean.TRUE);
        WindowsTaskPaneUI wui = new WindowsTaskPaneUI();
        userTaskPanel.setUI(wui);
        userTaskPanel.setTitle("Benutzer-Tasks");
        Image img = null;
        for (int i = 0; i < SystemConfig.vUserTasks.size(); i++) {
            JXHyperlink jxLink = new JXHyperlink();
            jxLink.setText(SystemConfig.vUserTasks.get(i)
                                                  .get(0));
            img = new ImageIcon(SystemConfig.vUserTasks.get(i)
                                                       .get(1)).getImage()
                                                               .getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            jxLink.setIcon(new ImageIcon(img));
            jxLink.setActionCommand("UserTask-" + Integer.toString(i));
            jxLink.addActionListener(ltplistener);
            jxLink.setClickedColor(new Color(0, 0x33, 0xFF));
            userTaskPanel.add(jxLink);
        }
        userTaskPanel.setCollapsed(SystemConfig.taskPaneCollapsed[6]);
        return userTaskPanel;
    }

    public void UpdateUI() {
        jxTPcontainer.updateUI();
        patientenstammPanel.updateUI();
        systemeinstellungpanel.updateUI();
        openofficePanel.updateUI();
        terminManagementPanel.updateUI();
        nuetzlichesPanel.updateUI();
        monatsuebersichtPanel.updateUI();
    }
 private boolean testUserTask() {
        File f = new File(Path.Instance.getProghome() + "ini/" + Betriebsumfeld.getAktIK() + "/usertask.ini");
        if (!f.exists()) {
            return false;
        }
        try {
            Settings utask = new INIFile(Path.Instance.getProghome() + "ini/" + Betriebsumfeld.getAktIK() + "/usertask.ini");
            int tasks = Integer.parseInt(utask.getStringProperty("UserTasks", "AnzahlUserTasks"));
            if (tasks == 0) {
                return false;
            }

            for (int i = 0; i < tasks; i++) {
                Vector<String> dummy = new Vector<String>();
                dummy.add(utask.getStringProperty("UserTasks", "UserTaskTitel" + Integer.toString(i + 1)));
                dummy.add(utask.getStringProperty("UserTasks", "UserIcon" + Integer.toString(i + 1)));
                dummy.add(utask.getStringProperty("UserTasks", "UserTaskExecute" + Integer.toString(i + 1)));
                SystemConfig.vUserTasks.add(dummy);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public void MaskenErstellen() {
        String sstmt = "";
        String behandler = "";
        Statement stmt = null;
        try {
            stmt = Reha.instance.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            for (int i = 1; i < 61; i++) {
                for (int t = 1; t < 8; t++) {
                    behandler = (i < 10 ? "0" + i + "BEHANDLER" : Integer.toString(i) + "BEHANDLER");
                    sstmt = "insert into masken set behandler='" + behandler + "' , art = '" + t
                            + "' ,belegt='1', N1='@FREI', TS1='07:00:00', TD1='900', TE1='22:00:00'";
                    try {
                        stmt.execute(sstmt);
                    } catch (SQLException e) {

                        e.printStackTrace();
                    }
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {

                    e.printStackTrace();
                }
            }
        }
    }

    private void doWeckerDrop(String drops) {
        Wecker wecker = new Wecker(drops);
        wecker.pack();
        wecker.setVisible(true);
        wecker = null;
    }

    private void doPatientDrop(String rez_nr) {
        String pat_int = "";
        String reznr = rez_nr;
        boolean inhistorie = false;
        int ind = reznr.indexOf("\\");
        if (ind >= 0) {
            reznr = reznr.substring(0, ind);
        }

        Vector<String> vec = SqlInfo.holeSatz("verordn", "pat_intern", "rez_nr='" + reznr + "'",
                new ArrayList<String>());
        if (vec.size() == 0) {
            vec = SqlInfo.holeSatz("lza", "pat_intern", "rez_nr='" + reznr + "'", new ArrayList<String>());
            if (vec.size() == 0) {
                JOptionPane.showMessageDialog(null,
                        "Rezept weder im aktuellen Rezeptstamm noch in der Historie vorhanden!\nIst die eingetragene Rezeptnummer korrekt?");
                return;
            } else {
                JOptionPane.showMessageDialog(null,
                        "Rezept ist bereits abgerechnet und somit in der Historie des Patienten!");
                inhistorie = true;
            }
        }
        vec = SqlInfo.holeSatz("pat5", "pat_intern", "pat_intern='" + vec.get(0) + "'", new ArrayList<String>());
        if (vec.size() == 0) {
            JOptionPane.showMessageDialog(null,
                    "Patient mit zugeordneter Rezeptnummer -> " + reznr + " <- wurde nicht gefunden");
            return;
        }
        pat_int = vec.get(0);
        JComponent patient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
        final String xreznr = reznr;
        final boolean xinhistorie = inhistorie;
        if (patient == null) {
            final String xpat_int = pat_int;
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    JComponent xpatient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
                    Reha.instance.progLoader.ProgPatientenVerwaltung(1, conn);
                    while ((xpatient == null)) {
                        Thread.sleep(20);
                        xpatient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
                    }
                    while ((!AktuelleRezepte.initOk)) {
                        Thread.sleep(20);
                    }

                    String s1 = "#PATSUCHEN";
                    String s2 = xpat_int;
                    PatStammEvent pEvt = new PatStammEvent(Reha.instance.terminpanel);
                    pEvt.setPatStammEvent("PatSuchen");
                    pEvt.setDetails(s1, s2, "#REZHOLEN-" + xreznr);
                    PatStammEventClass.firePatStammEvent(pEvt);
                    if (xinhistorie) {
                        Reha.instance.patpanel.getTab()
                                              .setSelectedIndex(1);
                    } else {
                        Reha.instance.patpanel.getTab()
                                              .setSelectedIndex(0);
                    }

                    return null;
                }

            }.execute();
        } else {
            Reha.instance.progLoader.ProgPatientenVerwaltung(1, conn);
            String s1 = "#PATSUCHEN";
            String s2 = pat_int;
            PatStammEvent pEvt = new PatStammEvent(Reha.instance.terminpanel);
            pEvt.setPatStammEvent("PatSuchen");
            pEvt.setDetails(s1, s2, "#REZHOLEN-" + xreznr);
            PatStammEventClass.firePatStammEvent(pEvt);
            if (xinhistorie) {
                Reha.instance.patpanel.getTab()
                                      .setSelectedIndex(1);
            } else {
                Reha.instance.patpanel.getTab()
                                      .setSelectedIndex(0);
            }

        }
    }

    private static void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(uri);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Failed to launch the link, your computer is likely misconfigured.",
                        "Cannot Launch Link", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Java is not able to launch links on your computer.",
                    "Cannot Launch Link", JOptionPane.WARNING_MESSAGE);
        }
    }
}
