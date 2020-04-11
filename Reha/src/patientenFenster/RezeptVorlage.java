package patientenFenster;
// Lemmi 20110101: Kopieren des letzten Rezepts des selben Patienten bei Rezept-Neuanlage

// dazu neue Klasse mit Auswahlfenster angelegt !

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.JRtaRadioButton;
import dialoge.PinPanel;
import dialoge.RehaSmartDialog;
import events.RehaTPEvent;
import events.RehaTPEventClass;
import hauptFenster.Reha;
import systemEinstellungen.SystemConfig;

public class RezeptVorlage extends RehaSmartDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    JRtaRadioButton[] rbDiszi = { null, null, null, null };
    ButtonGroup bgroup = new ButtonGroup();

    private RehaTPEventClass rtp = null;
    private RezeptVorlageHintergrund rgb;

    public JButton uebernahme;
    public JButton abbrechen;

    public String strSelectedDiszi = "";
    Vector<String> vecDiszi = new Vector<String>();
    public Vector<String> vecResult = new Vector<String>();
    public boolean bHasSelfDisposed = false;

    public RezeptVorlage(Point pt) {
        super(null, "RezeptVorlage");

        // Ermittlung der Rezept-Daten zu diesem Patienten
        String strPatIntern = Reha.instance.patpanel.vecaktrez.get(0);

        String cmd = "SELECT DISTINCT SUBSTR(REZ_NR,1,2) as diszi from "
                + "(SELECT \"lza\", `PAT_INTERN`,`REZ_NR`, `REZ_DATUM` FROM `lza` lza WHERE `PAT_INTERN` = "
                + strPatIntern + " union "
                + "SELECT \"verordn\", `PAT_INTERN`,`REZ_NR`, `REZ_DATUM` FROM `verordn` ver WHERE `PAT_INTERN` = "
                + strPatIntern + ") uni";
        // ORDER BY REZ_NR asc, rez_datum desc
        starteSuche(cmd, "diszi"); // das füllt den Member-Vektor "vecDiszi"

        pinPanel = new PinPanel();
        pinPanel.setName("RezeptVorlage");
        pinPanel.getGruen()
                .setVisible(false);
        setPinPanel(pinPanel);
        getSmartTitledPanel().setTitle("Rezeptvorlage wählen");

        setSize(300, 270);
        setPreferredSize(new Dimension(300, 270));
        getSmartTitledPanel().setPreferredSize(new Dimension(300, 270));
        setPinPanel(pinPanel);
        rgb = new RezeptVorlageHintergrund();
        rgb.setLayout(new BorderLayout());

        // Das erzeugt den farbigen Fensterhintergrund mit Farbverlauf für den Dialog
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                rgb.setBackgroundPainter(Reha.instance.compoundPainter.get("RezNeuanlage"));
                return null;
            }
        }.execute();

        rgb.add(getVorlage(), BorderLayout.CENTER);

        getSmartTitledPanel().setContentContainer(rgb);
        getSmartTitledPanel().getContentContainer()
                             .setName("RezeptVorlage");
        setName("RezeptVorlage");
        setModal(true);
        Point lpt = new Point(pt.x - 150, pt.y + 30);
        setLocation(lpt);

        rtp = new RehaTPEventClass();
        rtp.addRehaTPEventListener(this);

        pack();

        // prüfe die Anzahl der gefundenen Ergebnisse und treffe voreilige
        // Entscheidungen !
        if (vecDiszi.size() < 2) {
            if (vecDiszi.size() == 1)
                strSelectedDiszi = vecDiszi.get(0);
            // mit der Disziplin such wir jetzt noch das konkrete letzte Rezept zu dieser
            // Disziplin
            starteSucheVorlage(Reha.instance.patpanel.vecaktrez.get(0), strSelectedDiszi);
            this.dispose();
            bHasSelfDisposed = true;
            return;
        }

    }

    /****************************************************/

    // ermittelt die ganz konkrete Vorlage für die Kopieraktion
    private void starteSucheVorlage(String strPatIntern, String strDiszi) {

        // Suche neuestes Rezept inkl. der vorab bestimmten Disziplin
        String cmd = "SELECT * FROM `lza` WHERE `PAT_INTERN` = " + strPatIntern + " AND rez_nr like \"" + strDiszi
                + "%\"" + " union " + "SELECT * FROM `verordn` WHERE `PAT_INTERN` = " + strPatIntern
                + " AND rez_nr like \"" + strDiszi + "%\"" + " ORDER BY rez_datum desc LIMIT 1";

        starteSuche(cmd, "vorlage"); // das füllt den Member-Vektor "vecResult"
    }

    private void starteSuche(String sstmt, String strMode) {
        Statement stmt = null;
        ResultSet rs = null;
        vecDiszi.clear();

        try {
            stmt = Reha.instance.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException e) {

            e.printStackTrace();
        }
        try {
            rs = stmt.executeQuery(sstmt);

            if (strMode.equals("diszi"))
                while (rs.next()) {
                    vecDiszi.add(rs.getString("diszi")); // das schaut nur die Disziplinen aus gefundenen Rezepten an
                } // end if while
            else if (strMode.equals("vorlage")) { // das schaut konkrete Rezepte an
                vecResult.clear();
                ResultSetMetaData rsMetaData = rs.getMetaData();
                int numberOfColumns = rsMetaData.getColumnCount() + 1;
                while (rs.next())
                    for (int i = 1; i < numberOfColumns; i++) {
                        vecResult.add((rs.getString(i) == null ? "" : rs.getString(i)));
                    }
            }

        } catch (SQLException ev) {
            System.out.println("SQLException: " + ev.getMessage());
            System.out.println("SQLState: " + ev.getSQLState());
            System.out.println("VendorError: " + ev.getErrorCode());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { // ignore }
                    rs = null;
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { // ignore }
                    stmt = null;
                }
            }
        }
    }

    private JPanel getVorlage() { // 1 2 3 4 5 6 7 8
        FormLayout lay = new FormLayout("10dlu,fill:0:grow(0.50),50dlu,30dlu,5dlu,80dlu,fill:0:grow(0.50),10dlu",
                // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
                "15dlu,p,10dlu,p,2dlu,p,2dlu,p,2dlu,p, 10dlu, p,  20dlu, p ,20dlu");
        PanelBuilder pb = new PanelBuilder(lay);
        CellConstraints cc = new CellConstraints();

        pb.getPanel()
          .setOpaque(false);

        pb.addLabel(
                "<html>Es existieren Rezepte in mehreren Disziplinen.<br><br>"
                        + "Bitte die Disziplin wählen, deren letztes Rezept Sie JETZT kopieren wollen</html>",
                cc.xyw(2, 2, 5));

        int iAnzAktiv = SystemConfig.rezeptKlassenAktiv.size();
        int iAnzVorh = vecDiszi.size();
        int iYpos = 4; // Start-Position der Radio-Buttons;
        Boolean bFirst = true;

        for (int iVorh = 0; iVorh < iAnzVorh; iVorh++) { // renne über alle gefunden Rezept-Disziplinen

            for (int iAktiv = 0; iAktiv < iAnzAktiv; iAktiv++) { // Gleiche ab gegen die aktuelle aktiven Disziplinen
                if (vecDiszi.get(iVorh)
                            .equals(SystemConfig.rezeptKlassenAktiv.get(iAktiv)
                                                                   .get(1))) {

                    // nur gefundene && aktive Disziplienen anzeigen
                    rbDiszi[0] = new JRtaRadioButton(SystemConfig.rezeptKlassenAktiv.get(iAktiv)
                                                                                    .get(1));
                    if (bFirst) {
                        rbDiszi[0].setSelected(true);
                        strSelectedDiszi = vecDiszi.get(iVorh);
                    }
                    rbDiszi[0].setName(SystemConfig.rezeptKlassenAktiv.get(iAktiv)
                                                                      .get(1));
                    rbDiszi[0].addActionListener(this);
                    bgroup.add(rbDiszi[0]);
                    pb.add(rbDiszi[0], cc.xy(4, iYpos));

                    // rechts noch die Langbeschreibung daneben fummeln
                    pb.addLabel(SystemConfig.rezeptKlassenAktiv.get(iAktiv)
                                                               .get(0),
                            cc.xy(6, iYpos));

                    iYpos += 2; // nächste Y-Position festlegen
                    bFirst = false;
                    break; // für das innere "for"
                }
            }
        }

        uebernahme = new JButton("kopieren");
        uebernahme.setActionCommand("kopieren");
        uebernahme.addActionListener(this);
        uebernahme.addKeyListener(this);
        pb.add(uebernahme, cc.xyw(3, 14, 2));

        abbrechen = new JButton("abbrechen");
        abbrechen.setActionCommand("abbrechen");
        abbrechen.addActionListener(this);
        abbrechen.addKeyListener(this);
        pb.add(abbrechen, cc.xy(6, 14));

        pb.getPanel()
          .validate();
        return pb.getPanel();
    }

    /****************************************************/

    @Override
    public void rehaTPEventOccurred(RehaTPEvent evt) {

        try {
            if (evt.getDetails()[0] != null) {
                if (evt.getDetails()[0].equals(this.getName())) {
                    this.setVisible(false);
                    rtp.removeRehaTPEventListener(this);
                    rtp = null;
                    this.dispose();
                    super.dispose();
                }
            }
        } catch (NullPointerException ne) {
            ne.printStackTrace();
        }
    }

    @Override
    public void windowClosed(WindowEvent arg0) {

        if (rtp != null) {
            this.setVisible(false);
            rtp.removeRehaTPEventListener(this);
            rtp = null;
            pinPanel = null;
            dispose();
            super.dispose();
        }
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {

        if (arg0.getActionCommand()
                .equals("kopieren")) {
            // hier wird vecResult gefüllt
            starteSucheVorlage(Reha.instance.patpanel.vecaktrez.get(0), strSelectedDiszi);
            this.dispose();
        }
        if (vecDiszi.contains(arg0.getActionCommand())) { // Wenn eine der gefundenen Disziplinen angewählt worden ist
            strSelectedDiszi = arg0.getActionCommand();
            // hier wird vecResult gefüllt
            starteSucheVorlage(Reha.instance.patpanel.vecaktrez.get(0), strSelectedDiszi);
            this.dispose();
        }
        if (arg0.getActionCommand()
                .equals("abbrechen")) {
            strSelectedDiszi = "";
            vecResult.clear();
            this.dispose();
        }

    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == 10) {
            event.consume();

            if (((JComponent) event.getSource()).getName()
                                                .equals("uebernahme")) {
                // doUebernahme();
            }
            if (((JComponent) event.getSource()).getName()
                                                .equals("abbrechen")) {
                this.dispose();
            }

            // System.out.println("Return Gedrückt");
        }
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) { // 27 Abbruch mit der Tastatur
            // int iTest = KeyEvent.VK_ENTER;
            this.dispose();
        }
    }


}

class RezeptVorlageHintergrund extends JXPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ImageIcon hgicon;
    int icx, icy;
    AlphaComposite xac1 = null;
    AlphaComposite xac2 = null;

    public RezeptVorlageHintergrund() {
        super();
    }
}