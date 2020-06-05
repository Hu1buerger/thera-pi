package patientenFenster.rezepte;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.JRtaRadioButton;
import dialoge.PinPanel;
import dialoge.RehaSmartDialog;
import events.RehaTPEvent;
import events.RehaTPEventClass;
import hauptFenster.Reha;
import mandant.Mandant;
import sql.DatenquellenFactory;
import systemEinstellungen.SystemConfig;

public class RezNeuanlagePreDisziCheck extends RehaSmartDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RezNeuanlagePreDisziCheck.class);
    
    Mandant mand;
    JRtaRadioButton[] rbDiszi = { null, null, null, null };
    ButtonGroup bgroup = new ButtonGroup();

    private Point pt;
    private RehaTPEventClass rtp = null;
    private RezeptVorlageHintergrund rgb;

    private JButton btnUebernahme;
    private JButton btnAbbrechen;

    private String selectedDiszi = "";
    private List<String> gefDiszis;
    private String rezNr = "";
    private Boolean hasBeenFound = false;

    private int patIntern;

    public RezNeuanlagePreDisziCheck(Point Pt, int PatIntern, Mandant Mand) {
        super(null, "RezNeuanlagePreDisziCheck");
        
        patIntern = PatIntern;
        mand = Mand;
        pt = Pt;
        // gefDiszis = new ArrayList<String>();
        
    }

    public String findeDieRezNrZumKopieren() {
        // List<String> gefDiszis = new ArrayList<String>();
        // String selectedDiszi = "";
        String rezNr = "";
        
        gefDiszis = getAllDiszisFromPat(mand.ikDigitString());
        createPinPanel();
        
        // pruefe die Anzahl der gefundenen Ergebnisse und treffe voreilige
        // Entscheidungen !
        if (gefDiszis.size() < 2) {
            if (gefDiszis.size() == 1)
                selectedDiszi = gefDiszis.get(0);
            // mit der Disziplin suchen wir jetzt noch das konkrete letzte Rezept zu dieser
            // Disziplin
            rezNr = getRezNrByPatAndDiszi(selectedDiszi);
            hasBeenFound = true;
            this.dispose();
            //bHasSelfDisposed = true;
            return rezNr;
        }

        return "";
    }
    
    public String getRezNr() {
        return rezNr;
    }
    
    public Boolean hasSelected() {
        return hasBeenFound;
    }
        
    private void createPinPanel() {
        pinPanel = new PinPanel();
        pinPanel.setName("RezeptVorlage");
        pinPanel.getGruen()
                .setVisible(false);
        setPinPanel(pinPanel);
        getSmartTitledPanel().setTitle("Rezeptvorlage w\u00e4hlen");

        setSize(300, 270);
        setPreferredSize(new Dimension(300, 270));
        getSmartTitledPanel().setPreferredSize(new Dimension(300, 270));
        setPinPanel(pinPanel);
        rgb = new RezeptVorlageHintergrund();
        rgb.setLayout(new BorderLayout());

        // Das erzeugt den farbigen Fensterhintergrund mit Farbverlauf fuer den Dialog
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                rgb.setBackgroundPainter(Reha.instance.compoundPainter.get("RezNeuanlage"));
                return null;
            }
        }.execute();

        rgb.add(createJPanel(), BorderLayout.CENTER);

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

    }
    
    private JPanel createJPanel() { // 1 2 3 4 5 6 7 8
        FormLayout lay = new FormLayout("10dlu,fill:0:grow(0.50),50dlu,30dlu,5dlu,80dlu,fill:0:grow(0.50),10dlu",
                // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
                "15dlu,p,10dlu,p,2dlu,p,2dlu,p,2dlu,p, 10dlu, p,  20dlu, p ,20dlu");
        PanelBuilder pb = new PanelBuilder(lay);
        CellConstraints cc = new CellConstraints();

        pb.getPanel().setOpaque(false);

        pb.addLabel(
                "<html>Es existieren Rezepte in mehreren Disziplinen.<br><br>"
                        + "Bitte die Disziplin w\u00e4hlen, deren letztes Rezept Sie JETZT kopieren wollen</html>",
                cc.xyw(2, 2, 5));

        int iAnzAktiv = SystemConfig.rezeptKlassenAktiv.size();
        int iAnzVorh = gefDiszis.size();
        int iYpos = 4; // Start-Position der Radio-Buttons;
        Boolean bFirst = true;

        for (int iVorh = 0; iVorh < iAnzVorh; iVorh++) { // renne ueber alle gefunden Rezept-Disziplinen

            for (int iAktiv = 0; iAktiv < iAnzAktiv; iAktiv++) { // Gleiche ab gegen die aktuelle aktiven Disziplinen
                if (gefDiszis.get(iVorh)
                            .equals(SystemConfig.rezeptKlassenAktiv.get(iAktiv)
                                                                   .get(1))) {

                    // nur gefundene && aktive Disziplienen anzeigen
                    rbDiszi[0] = new JRtaRadioButton(SystemConfig.rezeptKlassenAktiv.get(iAktiv)
                                                                                    .get(1));
                    if (bFirst) {
                        rbDiszi[0].setSelected(true);
                        selectedDiszi = gefDiszis.get(iVorh);
                    }
                    rbDiszi[0].setName(SystemConfig.rezeptKlassenAktiv.get(iAktiv)
                                                                      .get(1));
                    rbDiszi[0].addActionListener(e -> actionSetSelectedDisziAndFindFittingRzNr(e));
                    bgroup.add(rbDiszi[0]);
                    pb.add(rbDiszi[0], cc.xy(4, iYpos));

                    // rechts noch die Langbeschreibung daneben fummeln
                    pb.addLabel(SystemConfig.rezeptKlassenAktiv.get(iAktiv)
                                                               .get(0),
                            cc.xy(6, iYpos));

                    iYpos += 2; // naechste Y-Position festlegen
                    bFirst = false;
                    break; // fuer das innere "for"
                }
            }
        }

        btnUebernahme = new JButton("kopieren");
        btnUebernahme.setActionCommand("kopieren");
        btnUebernahme.addActionListener(e -> actionProcessSelection());
        btnUebernahme.addKeyListener(this);
        pb.add(btnUebernahme, cc.xyw(3, 14, 2));

        btnAbbrechen = new JButton("abbrechen");
        btnAbbrechen.setActionCommand("abbrechen");
        btnAbbrechen.addActionListener(e -> actionAbbrechen());
        btnAbbrechen.addKeyListener(this);
        pb.add(btnAbbrechen, cc.xy(6, 14));

        pb.getPanel()
          .validate();
        return pb.getPanel();
    }

    /**
     * 
     */
    private List<String> getAllDiszisFromPat(String ik) {
        List<String> res = new ArrayList<String>();
        
        // Sort-unique on diszi (as substr. out of RzNr) from all Rezepte (akt. & hist.) of Patient (ident. by PatIntern)
        String sql = "SELECT DISTINCT SUBSTR(REZ_NR,1,2) as diszi from "
                + "(SELECT \"lza\", `PAT_INTERN`,`REZ_NR`, `REZ_DATUM` FROM `lza` lza WHERE `PAT_INTERN` = "
                + patIntern + " union "
                + "SELECT \"verordn\", `PAT_INTERN`,`REZ_NR`, `REZ_DATUM` FROM `verordn` ver WHERE `PAT_INTERN` = "
                + patIntern + ") uni";
        // ORDER BY REZ_NR asc, rez_datum desc

        try (Connection conn = new DatenquellenFactory(ik)
                                                    .createConnection();

                ResultSet rs = conn.createStatement()
                                    .executeQuery(sql)) {
            while (rs.next()) {
                res.add(rs.getString("diszi"));
            }
        } catch (SQLException e) {
            logger.error("could not retrieve Diszis from Database", e);
        }
        return res;
    }

    private String getRezNrByPatAndDiszi(String diszi) {
        String rezNr = "";
        String sql = "SELECT * FROM `lza` WHERE `PAT_INTERN` = " + patIntern + " AND rez_nr like '" + diszi
                + "%'" + " union " + "SELECT * FROM `verordn` WHERE `PAT_INTERN` = " + patIntern
                + " AND rez_nr like '" + diszi + "%'" + " ORDER BY rez_datum desc LIMIT 1";
        try (Connection conn = new DatenquellenFactory(mand.ikDigitString())
                .createConnection();

                ResultSet rs = conn.createStatement()
                                    .executeQuery(sql)) {
            if (rs.next()) {
                    rezNr = rs.getString("REZ_NR");
            }
        } catch (SQLException e) {
            logger.error("could not retrieve RezNr by patId and diszi from Database", e);
        }
    
        return rezNr;
    }
    
    private void actionProcessSelection() {
        rezNr = getRezNrByPatAndDiszi(selectedDiszi);
        hasBeenFound = true;
        this.dispose();
    }
    
    private void actionAbbrechen() {
        selectedDiszi = "";
        gefDiszis.clear();
        this.dispose();
    }
 
    private void actionSetSelectedDisziAndFindFittingRzNr(ActionEvent ae) {
        selectedDiszi = ae.getActionCommand();
        logger.debug("Found selected Diszi to be: " + selectedDiszi);
        rezNr = getRezNrByPatAndDiszi(selectedDiszi);
        hasBeenFound = true;
        // get actual RzNr somehow
        this.dispose();
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
                actionAbbrechen();
            }

            // System.out.println("Return Gedrueckt");
        }
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) { // 27 Abbruch mit der Tastatur
            // int iTest = KeyEvent.VK_ENTER;
            actionAbbrechen();
        }
    }
    
    @Override
    public void rehaTPEventOccurred(RehaTPEvent evt) {
        if (evt.getDetails()[0] != null &&  evt.getDetails()[0].equals(this.getName()))
            cleanExit();
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
        cleanExit();
    }

    private void cleanExit() {
        this.setVisible(false);
        if (rtp != null) {
            rtp.removeRehaTPEventListener(this);
            rtp = null;
        }
        pinPanel = null;
        dispose();
        super.dispose();        
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

