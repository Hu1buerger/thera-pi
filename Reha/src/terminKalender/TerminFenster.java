package terminKalender;

import static java.awt.event.KeyEvent.*;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TooManyListenersException;
import java.util.Vector;

import javax.swing.*;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.therapi.reha.patient.RezepteAktuell;

import CommonTools.DatFunk;
import CommonTools.SqlInfo;
import CommonTools.ZeitFunk;
import dialoge.InfoDialogTerminInfo;
import environment.Path;
import events.PatStammEvent;
import events.PatStammEventClass;
import events.RehaTPEvent;
import events.RehaTPEventClass;
import events.RehaTPEventListener;
import generalSplash.RehaSplash;
import hauptFenster.AktiveFenster;
import hauptFenster.Reha;
import hilfsFenster.TerminEinpassen;
import hilfsFenster.TerminObenUntenAnschliessen;
import rechteTools.Rechte;
import rehaInternalFrame.JRehaInternal;
import stammDatenTools.RezTools;
import systemEinstellungen.BehandlerSet;
import systemEinstellungen.BehandlerSets;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.TKSettings;
import systemEinstellungen.config.Datenbank;
import systemTools.ListenerTools;

public class TerminFenster implements RehaTPEventListener, ActionListener, DropTargetListener {

    private static final int SPALTE_ANZ_BELGEGTE_BLOECKE = 301;
    private static final DateTimeFormatter ddmmyyy_hhmmss = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private JXPanel grundFlaeche;
    private JXPanel comboFlaeche;
    private JXPanel TerminFlaeche;
    JXPanel viewPanel;

    private TherapeutenTag[] oSpalten = new TherapeutenTag[7];
    private JComboBox<String>[] oCombo = new JComboBox[7];

    private JPopupMenu jPopupMenu;
    private JMenuItem Normalanzeige;
    private JMenuItem Wochenanzeige;
    private JMenuItem Patientsuchen;
    private JMenuItem Gruppezusammenfassen;
    private JMenuItem Gruppeloeschen;
    private JMenuItem Gruppekopieren;
    private JMenuItem Terminliste;
    private JMenuItem Gruppeeinfuegen;
    private JMenuItem Terminedespatsuchen;

    private JMenuItem Interminkalenderschreiben;
    private JMenuItem Copy;
    private JMenuItem Cut;
    private JMenuItem Paste;
    private JMenuItem Confirm;
    private JMenuItem Tagvor;
    private JMenuItem Tagzurueck;
    private JMenuItem Tagesdialog;
    private JMenuItem Behandlerset;
    private JMenuItem Tauschemitvorherigem;
    private JMenuItem Tauschemitnachfolger;
    private JMenuItem Telefonliste;

    private Zeitfenster zf;
    private DruckFenster df;
    private SchnellSuche sf;
    private MaskeInKalenderSchreiben mb;

    /** Welcher Kollege(Nr. ist in der jeweiligen Spalte */
    private int[] belegung = { -1, -1, -1, -1, -1, -1, -1 };
    /** Sichtbar nimmt die KollegenNr auf dessen Woche angezeigt wird. */
    private int wochenbelegung;
    /** Nimmt die KollegenNr auf dessen Maske erstellt/editiert wird. */
    private int maskenbelegung;
    /** Nimmt die KollegenNr auf dessen Maske erstellt/editiert wird. */
    private String[] sbelegung = { "./.", "./.", "./.", "./.", "./.", "./.", "./." };
    /** Welches Set soll dargestellt werden. */
 //   private int aktSet;

    /** Nimmt die Termindaten auf. */
    private Vector<Object> aSpaltenDaten = new Vector<>();
    /** Wird gebraucht zur Datenübergabe auf die Spalten. */
    private Vector vTerm = new Vector();
    private boolean updateverbot;

    private Point dragDaten = new Point(0, 0);
    /** Zur Positionsbestimmung Spalte, Block, aktiver Block etc. */
    private int[] aktiveSpalte = { 0, 0, 0, 0 };
    /** Zur Positionsbestimmung Spalte, Block, aktiver Block etc. */
    private int[] altaktiveSpalte = { -1, -1, -1, -1 };

    private String aktuellerTag;
    private String wocheAktuellerTag = "";
    private String wocheErster = "";
    private int wocheBehandler;
    private String swSetWahl = "./.";

    public enum Ansicht {
        NORMAL(),
        WOCHE(),
        MASKE();
    }

    Ansicht aktAnsicht;

    private Block terminangaben = new Block( "" , "" , ""  , "" ,
            "", ""  );
    private Block terminrueckgabe = new Block(  "" , ""  , "" , "",
            "", "" );

    private int focus[] = { 0, 0 };
    private boolean hasFocus;
    private RehaTPEventClass xEvent;

    private static String lockStatement = "";
    Statement privstmt;

    private int lockok;
    private String lockmessage = "";
    private String[] spaltenDatum = { null, null, null, null, null, null, null };

    private String[] datenSpeicher = { null, null, null, null, null };
    private int dialogRetInt;
    private String[] dialogRetData = { null, null };

    private boolean gruppierenAktiv;
    private int[] gruppierenBloecke = { -1, -1 };
    private int gruppierenBehandler;
    private int gruppierenSpalte;
    private int[] gruppierenClipBoard = { -1, -1, -1, -1 };
    private boolean gruppierenKopiert;

    private float fPixelProMinute;

    private ArrayList<String[]> terminVergabe = new ArrayList<>();

    public Thread db_Aktualisieren;

    String datGewaehlt;
    private boolean wartenAufReady;

    private boolean intagWahl;
    private boolean interminEdit;

    JLabel[] dragLab = { null, null, null, null, null, null, null };

    private static int DRAG_COPY;
    private static int DRAG_MOVE = 1;
    public static int DRAG_UNKNOWN = 2;
    private static int DRAG_NONE = -1;
    public static int DRAG_MODE = -1;
    private String DRAG_UHR = "";
    private String DRAG_PAT = "";
    private String DRAG_NUMMER = "";
    private boolean terminGedropt;

    private boolean terminBreak;
    private JRehaInternal eltern;

    private InfoDialogTerminInfo infoDlg;



    private  String[] dayname = { "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag",
            "Sonntag" };
    private  String[] dayshortname = { "Mo - ", "Di - ", "Mi - ", "Do - ", "Fr - ", "Sa - ", "So - " };
    private  String[] tooltip = { "", "", "", "", "", "", "" };
    private FinalGlassPane fgp;

    private Connection connection;
    final static private Logger logger = LoggerFactory.getLogger(TerminFenster.class);
    private BehandlerSet aktuellesSet = BehandlerSet.EMPTY;

    public TerminFenster(Connection connection) {
        this.connection = connection;
    }

    public JXPanel init(int setOben, Ansicht ansicht, JRehaInternal eltern, Connection connection) {
        this.eltern = eltern;
        this.aktAnsicht = ansicht;
        xEvent = new RehaTPEventClass();
        xEvent.addRehaTPEventListener(this);

        viewPanel = new JXPanel(new BorderLayout());

        viewPanel.setName(eltern.getName());

        grundFlaeche = getGrundFlaeche(connection);
        grundFlaeche.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusGained(java.awt.event.FocusEvent e) {

                holeFocus();
            }
        });

        viewPanel.add(grundFlaeche, BorderLayout.CENTER);
        viewPanel.setBackground(TKSettings.KalenderHintergrund);
        DropTarget dndt = new DropTarget();
        try {
            dndt.addDropTargetListener(this);
        } catch (TooManyListenersException e2) {

            e2.printStackTrace();
        }
        TerminFlaeche.setDropTarget(dndt);

        setCombos(connection);

        this.aktuellerTag = DatFunk.sHeute();
        if (this.aktAnsicht == Ansicht.MASKE) {
            String stmtmaske = "select from masken where behandler = '00BEHANDLER' ORDER BY art";
            maskenStatement(stmtmaske);
        } else {
            String sstmt = ansichtStatement(this.aktuellerTag, ansicht);
            macheStatement(sstmt, aktAnsicht == Ansicht.NORMAL ? KollegenListe.maxKalZeile : 7);
        }

        viewPanel.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                Reha.instance.terminpanel.eltern.feuereEvent(25554);
                Reha.instance.terminpanel.eltern.feuereEvent(25554);
                if (!Reha.instance.terminpanel.eltern.isSelected()) {
                    try {
                        Reha.instance.terminpanel.eltern.setSelected(true);
                    } catch (PropertyVetoException e1) {
                        e1.printStackTrace();
                    }
                }
                holeFocus();
            }
        });

        grundFlaeche.revalidate();
        viewPanel.revalidate();

        if (ansicht == Ansicht.WOCHE) {
            this.aktAnsicht = Ansicht.NORMAL;
            oCombo[0].setSelectedItem(TKSettings.KalenderStartWADefaultUser);
            setWochenanzeige();
        } else if (ansicht == Ansicht.NORMAL) {

            aktuellesSet = BehandlerSets.find(TKSettings.defaultBehandlerSet) ;
            if (aktuellesSet!=null) {
                for (int i = 0; i < 7; i++) {
                    oCombo[i].setSelectedItem(aktuellesSet.getMembers().get(i));
                }
            }
        }

        setzeStatement();

        if (TKSettings.KalenderZeitLabelZeigen) {
            fgp = new FinalGlassPane(eltern);
            eltern.setGlassPane(fgp);
        }
        getDatenVonExternInSpeicherNehmen();
        return viewPanel;

    }

    public void regleZeitLabel() {
        if (!TKSettings.KalenderZeitLabelZeigen && fgp != null) {
            fgp.setVisible(false);
            fgp = null;
        } else if (TKSettings.KalenderZeitLabelZeigen && fgp == null) {
            fgp = new FinalGlassPane(eltern);
            eltern.setGlassPane(fgp);
        }
    }

    public JXPanel getTerminFlaecheFromOutside() {
        return TerminFlaeche;
    }

    private void finalise() {
        vTerm.clear();
        vTerm = null;
        for (int i = 0; i < 7; i++) {

            oSpalten[i].removeListeners();
            oSpalten[i] = null;
            ListenerTools.removeListeners(oCombo[i].getParent());
            ListenerTools.removeListeners(oCombo[i]);
            oCombo[i] = null;
            if (viewPanel != null) {
                ListenerTools.removeListeners(viewPanel);
                viewPanel = null;
            }
            if (comboFlaeche != null) {
                ListenerTools.removeListeners(comboFlaeche);
                comboFlaeche = null;
            }
            if (TerminFlaeche != null) {
                ListenerTools.removeListeners(TerminFlaeche);
                TerminFlaeche = null;
            }
            if (grundFlaeche != null) {
                ListenerTools.removeListeners(grundFlaeche);
                grundFlaeche = null;
            }
        }
    }

    private void setzeStatement() {
        try {
            this.privstmt = Reha.instance.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            this.privstmt.setQueryTimeout(1);
        } catch (SQLException ex) {
            SqlInfo.loescheLocksMaschine();
        }
        if (TKSettings.UpdateIntervall > 0 && this.aktAnsicht != Ansicht.MASKE) {
            db_Aktualisieren = new Thread(new sperrTest());
            db_Aktualisieren.start();
        }
    }

    private JXPanel getGrundFlaeche(Connection connection) {
        if (grundFlaeche == null) {
            GridBagConstraints terminflaecheConstraints = new GridBagConstraints();
            terminflaecheConstraints.gridx = 0;
            terminflaecheConstraints.weighty = 150.0D;
            terminflaecheConstraints.weightx = 1.0D;
            terminflaecheConstraints.fill = GridBagConstraints.BOTH;
            terminflaecheConstraints.insets = new Insets(0, 0, 0, 0);
            terminflaecheConstraints.gridwidth = 0;
            terminflaecheConstraints.gridheight = 1;
            terminflaecheConstraints.anchor = GridBagConstraints.CENTER;
            terminflaecheConstraints.gridy = 1;
            terminflaecheConstraints.gridx = 0;

            GridBagConstraints ComboflaecheConstraints = new GridBagConstraints();
            ComboflaecheConstraints.gridx = 0;
            ComboflaecheConstraints.weighty = 1.0D;
            ComboflaecheConstraints.weightx = 1.0D;
            ComboflaecheConstraints.fill = GridBagConstraints.HORIZONTAL;
            ComboflaecheConstraints.insets = new Insets(0, 0, 0, 0);
            ComboflaecheConstraints.gridwidth = 0;
            ComboflaecheConstraints.gridheight = 1;
            ComboflaecheConstraints.anchor = GridBagConstraints.NORTH;
            ComboflaecheConstraints.gridy = 0;
            ComboflaecheConstraints.gridx = 0;

            grundFlaeche = new JXPanel();
            grundFlaeche.setDoubleBuffered(true);
            grundFlaeche.setBorder(null);
            grundFlaeche.setBackground(TKSettings.KalenderHintergrund);

            grundFlaeche.setLayout(new GridBagLayout());
            grundFlaeche.add(getComboFlaeche(), ComboflaecheConstraints);
            grundFlaeche.add(getTerminFlaeche(connection), terminflaecheConstraints);
        }
        return grundFlaeche;
    }

    public String getAktuellerTag() {
        return this.aktuellerTag;
    }

    private JXPanel getComboFlaeche() {
        if (comboFlaeche == null) {
            DropShadowBorder dropShadow = new DropShadowBorder(Color.BLACK, 5, 1, 3, true, true, false, true);
            comboFlaeche = new JXPanel();
            comboFlaeche.setBackground(TKSettings.KalenderHintergrund);
            GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(1);
            comboFlaeche.setLayout(gridLayout);
            comboFlaeche.setDoubleBuffered(true);
            comboFlaeche.setBorder(null);
            JXPanel cb = null;
            for (int i = 0; i < 7; i++) {
                cb = new JXPanel(new BorderLayout());
                cb.setBorder(dropShadow);
                cb.setBackground(TKSettings.KalenderHintergrund);
                oCombo[i] = new JComboBox();
                oCombo[i].setName("Combo" + i);
                cb.add(oCombo[i], BorderLayout.CENTER);
                cb.addMouseListener(new comboToolTip(i));
                comboFlaeche.add(cb);
            }
            comboFlaeche.revalidate();
        }
        return comboFlaeche;
    }

    /** Jetzt die Listener fuer die Combos installieren. */
    private void comboListenerInit(final int welche, Connection connection) {
        oCombo[welche].setPopupVisible(false);
        oCombo[welche].addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                for (int i = 0; i < 1; i++) {

                    if ((e.getKeyCode() == VK_PAGE_UP || e.getKeyCode() == VK_PAGE_DOWN)
                            && aktAnsicht != Ansicht.MASKE) {
                        e.consume();
                        // Neuer Tag soll gewählt werden
                        panelTastenAuswerten(e);
                        break;
                    }
                    if (e.getKeyCode() == VK_UP || e.getKeyCode() == VK_DOWN) {
                        if (!oCombo[welche].isPopupVisible()) {
                            oCombo[welche].setPopupVisible(false);
                        }
                        break;
                    }
                    if ((e.getKeyCode() == VK_F12) && aktAnsicht != Ansicht.MASKE) {
                        setAufruf(null);
                        aktiveSpalte[2] = welche;
                        oSpalten[welche].requestFocus();
                        break;
                    }
                    if (e.getKeyCode() == VK_ESCAPE) {
                        aktiveSpalte[2] = welche;
                        oSpalten[welche].requestFocus();
                        e.consume();
                        break;
                    }
                    if ((e.getKeyCode() == VK_L) && (e.isControlDown()) && aktAnsicht != Ansicht.MASKE) {
                        e.consume();
                        terminListe();
                        break;
                    }
                    if (e.getKeyCode() == VK_F11 && (!e.isShiftDown()) && aktAnsicht != Ansicht.MASKE) {
                        // F11 ohne Shift
                        schnellSuche(connection);
                        break;
                    }
                    if (e.getKeyCode() == VK_D && e.isControlDown()) {
                        // Terminplan drucken
                        DruckeViewPanel dvp = new DruckeViewPanel();
                        dvp.setPrintPanel(Reha.instance.terminpanel.viewPanel);
                        break;
                    }
                    if ((e.getKeyCode() == VK_W) && (e.isControlDown())) {
                        // Wochenansicht
                        setWochenanzeige();
                        break;
                    }
                    if ((e.getKeyCode() == VK_N) && (e.isControlDown())) {
                        // Normalansicht
                        setNormalanzeige();
                        break;
                    }
                    if (e.getKeyCode() == VK_P && e.isAltDown()) {
                        doPatSuchen(connection);
                    }

                }

            }

        });
        oCombo[welche].addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String matchcode = (String) oCombo[welche].getSelectedItem();
                int wahl = KollegenListe.suchen(matchcode);
                if (!oCombo[welche].isPopupVisible()) {
                    oCombo[welche].setPopupVisible(false);
                }
                if (TerminFenster.this.aktAnsicht == Ansicht.NORMAL) {
                    try {
                        belegung[welche] = KollegenListe.vKKollegen.get(wahl).getReihe() - 1;
                        oSpalten[welche].datenZeichnen(vTerm, belegung[welche]);
                        oSpalten[aktiveSpalte[2]].requestFocus();
                        if (welche == 0) {
                            wochenbelegung = KollegenListe.vKKollegen.get(wahl).getReihe();
                        }

                    } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                        SqlInfo.loescheLocksMaschine();
                    }
                } else if (TerminFenster.this.aktAnsicht == Ansicht.WOCHE) {
                    try {
                        if (welche == 0) {
                            wochenbelegung = KollegenListe.vKKollegen.get(wahl).getReihe();
                            if ("".equals(wocheErster)) {
                                ansichtStatement(aktuellerTag, aktAnsicht);
                            } else {
                                ansichtStatement(wocheErster, aktAnsicht);
                            }
                        }
                    } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                        SqlInfo.loescheLocksMaschine();
                    }

                } else if (TerminFenster.this.aktAnsicht == Ansicht.MASKE) {

                    maskenbelegung = KollegenListe.vKKollegen.get(wahl).getReihe();
                    String maskenbehandler = (maskenbelegung < 10 ? "0" + maskenbelegung + "BEHANDLER"
                            : Integer.toString(maskenbelegung) + "BEHANDLER");
                    String stmtmaske = "select * from masken where behandler = '" + maskenbehandler + "' ORDER BY art";
                    //// System.out.println(stmtmaske);
                    maskenStatement(stmtmaske);

                }
            }
        });
        oCombo[welche].addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                focusHandling(0, -1);
            }

            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                focusHandling(0, 1);
                try {
                    if (!Reha.instance.terminpanel.eltern.isActive) {
                        Reha.instance.terminpanel.eltern.feuereEvent(25554);
                    }
                } catch (Exception ex) {

                }
            }
        });
    }

    /** Die Comboboxen mit Werden füllen. */
    public void setCombosOutside() {
        int von = 0;
        int bis = KollegenListe.vKKollegen.size();
        for (int i = 0; i < 7; i++) {
            oCombo[i].removeAllItems();
        }
        for (von = 0; von < bis; von++) {
            oCombo[0].addItem(KollegenListe.vKKollegen.get(von).getMatchcode());
            oCombo[1].addItem(KollegenListe.vKKollegen.get(von).getMatchcode());
            oCombo[2].addItem(KollegenListe.vKKollegen.get(von).getMatchcode());
            oCombo[3].addItem(KollegenListe.vKKollegen.get(von).getMatchcode());
            oCombo[4].addItem(KollegenListe.vKKollegen.get(von).getMatchcode());
            oCombo[5].addItem(KollegenListe.vKKollegen.get(von).getMatchcode());
            oCombo[6].addItem(KollegenListe.vKKollegen.get(von).getMatchcode());
        }
        oCombo[0].setMaximumRowCount(35);
        oCombo[0].setSelectedItem("./.");
        oCombo[1].setMaximumRowCount(35);
        oCombo[1].setSelectedItem("./.");
        oCombo[2].setMaximumRowCount(35);
        oCombo[2].setSelectedItem("./.");
        oCombo[3].setMaximumRowCount(35);
        oCombo[3].setSelectedItem("./.");
        oCombo[4].setMaximumRowCount(35);
        oCombo[4].setSelectedItem("./.");
        oCombo[5].setMaximumRowCount(35);
        oCombo[5].setSelectedItem("./.");
        oCombo[6].setMaximumRowCount(35);
        oCombo[6].setSelectedItem("./.");
        if (aktAnsicht == Ansicht.MASKE) {
            oCombo[1].setEnabled(false);
            oCombo[2].setEnabled(false);
            oCombo[3].setEnabled(false);
            oCombo[4].setEnabled(false);
            oCombo[5].setEnabled(false);
            oCombo[6].setEnabled(false);
        }
    }

    public void setCombos(Connection connection) {
        int von = 0;
        int bis = KollegenListe.vKKollegen.size();

        // String cwert = null;
        for (von = 0; von < bis; von++) {
            // cwert = ParameterLaden.vKKollegen.get(von).Matchcode;
            oCombo[0].addItem(KollegenListe.vKKollegen.get(von).getMatchcode());
            oCombo[1].addItem(KollegenListe.vKKollegen.get(von).getMatchcode());
            oCombo[2].addItem(KollegenListe.vKKollegen.get(von).getMatchcode());
            oCombo[3].addItem(KollegenListe.vKKollegen.get(von).getMatchcode());
            oCombo[4].addItem(KollegenListe.vKKollegen.get(von).getMatchcode());
            oCombo[5].addItem(KollegenListe.vKKollegen.get(von).getMatchcode());
            oCombo[6].addItem(KollegenListe.vKKollegen.get(von).getMatchcode());
        }
        oCombo[0].setMaximumRowCount(35);
        oCombo[0].setSelectedItem("./.");
        oCombo[1].setMaximumRowCount(35);
        oCombo[1].setSelectedItem("./.");
        oCombo[2].setMaximumRowCount(35);
        oCombo[2].setSelectedItem("./.");
        oCombo[3].setMaximumRowCount(35);
        oCombo[3].setSelectedItem("./.");
        oCombo[4].setMaximumRowCount(35);
        oCombo[4].setSelectedItem("./.");
        oCombo[5].setMaximumRowCount(35);
        oCombo[5].setSelectedItem("./.");
        oCombo[6].setMaximumRowCount(35);
        oCombo[6].setSelectedItem("./.");
        if (this.aktAnsicht == Ansicht.MASKE) {
            oCombo[1].setEnabled(false);
            oCombo[2].setEnabled(false);
            oCombo[3].setEnabled(false);
            oCombo[4].setEnabled(false);
            oCombo[5].setEnabled(false);
            oCombo[6].setEnabled(false);
        }
        /* jetzt noch die Listener initialisieren */
        for (int i = 0; i < 7; i++) {
            comboListenerInit(i, connection);
        }
    }

    private JXPanel getTerminFlaeche(Connection connection) {
        if (TerminFlaeche == null) {
            TerminFlaeche = new JXPanel();
            TerminFlaeche.setBackground(TKSettings.KalenderHintergrund);
            GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(1);
            TerminFlaeche.setLayout(gridLayout);
            TerminFlaeche.setBorder(null);
            JXPanel cb = null;

            for (int i = 0; i < 7; i++) {
                cb = new JXPanel(new BorderLayout());
                cb.setBorder(null);
                cb.setBackground(TKSettings.KalenderHintergrund);
                String name = "Spalte" + i;
                oSpalten[i] = new TherapeutenTag(name,true,TKSettings.KalenderAlpha);


                dragLab[i] = new JLabel();
                dragLab[i].setName("draLab-" + i);
                dragLab[i].setForeground(SystemConfig.aktTkCol.get("aktBlock")[1]);
                dragLab[i].setBounds(0, 0, oSpalten[i].getWidth(), oSpalten[i].getHeight());
                dragLab[i].addMouseListener(new MouseAdapter() {

                    @Override
                    public void mousePressed(MouseEvent e) {
                        String[] sdaten = datenInDragSpeicherNehmen();
                        if (sdaten[0] == null) {
                            return;
                        }
                        if (!Rechte.hatRecht(Rechte.Kalender_termindragdrop, false)) {
                            return;
                        }
                        if (e.isAltDown()) {
                            DRAG_MODE = DRAG_MOVE;
                        } else if (e.isControlDown()) {
                            DRAG_MODE = DRAG_COPY;
                        } else {
                            DRAG_MODE = DRAG_NONE;
                        }
                        int behandler = -1;
                        int behandler1 = behandler;
                        switch (aktAnsicht) {
                        case NORMAL:
                            behandler1 = belegung[aktiveSpalte[2]];
                            break;
                        case WOCHE:
                            behandler1 = aktiveSpalte[2];
                            break;
                        case MASKE:
                            behandler1 = aktiveSpalte[2];
                            break;
                        }
                        behandler = behandler1;
                        if (behandler <= -1) {
                            return;
                        }
                        DRAG_PAT = ((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(0)).get(
                                aktiveSpalte[0])).replaceAll("\u00AE", "");
                        DRAG_NUMMER = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(1)).get(
                                aktiveSpalte[0]);
                        DRAG_UHR = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(2)).get(
                                aktiveSpalte[0]);
                        altaktiveSpalte = aktiveSpalte.clone();
                        // JLabel lab = new
                        // JLabel("TERMDATINTERN"+"°"+sdaten[0]+"°"+sdaten[1]+"°"+sdaten[3]+" Min.");
                        JLabel lab = new JLabel("TERMDATINTERN" + "°" + sdaten[0] + "°" + sdaten[1] + "°" + sdaten[3]
                                + " Min." + "°" + DRAG_UHR);
                        lab.setTransferHandler(new TransferHandler("text"));
                        TransferHandler th = lab.getTransferHandler();
                        th.exportAsDrag(lab, e, TransferHandler.COPY);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        JComponent c = (JComponent) e.getSource();
                        int v = Integer.parseInt(c.getName()
                                                  .split("-")[1]);
                        dragLab[v].setText("");
                        dragLab[v].setIcon(null);
                        oSpalten[v].repaint();
                    }
                });
                oSpalten[i].add(dragLab[i]);

                PanelListenerInit(i, connection);
                oSpalten[i].ListenerSetzen(i);
                cb.add(oSpalten[i].getDay(), BorderLayout.CENTER);
                TerminFlaeche.add(cb);
            }
            TerminFlaeche.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    // GroesseSetzen();
                    oSpalten[0].zeitSpanne();
                    oSpalten[1].zeitSpanne();
                    oSpalten[2].zeitSpanne();
                    oSpalten[3].zeitSpanne();
                    oSpalten[4].zeitSpanne();
                    oSpalten[5].zeitSpanne();
                    oSpalten[6].zeitSpanne();
                    grundFlaeche.revalidate();
                    int iMaxHoehe = TerminFlaeche.getHeight();
                    fPixelProMinute = iMaxHoehe;
                    fPixelProMinute = fPixelProMinute / 900;
                }
            });
            setTimeLine(TKSettings.KalenderTimeLineZeigen);
            TerminFlaeche.revalidate();
        }
        return TerminFlaeche;
    }

    public float getPanelPixelProMinute() {
        return oSpalten[0].getPixels();
    }

    public static void setTransparenz(float alf) {
        try {
            if (!(Reha.instance.terminpanel.oSpalten == null)) {
                for (int i = 0; i < 7; i++) {
                    Reha.instance.terminpanel.oSpalten[i].setTransparenz(alf);

                    Reha.instance.terminpanel.oCombo[i].getParent()
                                                       .setBackground(TKSettings.KalenderHintergrund);
                }
                Reha.instance.terminpanel.viewPanel.setBackground(TKSettings.KalenderHintergrund);
                Reha.instance.terminpanel.grundFlaeche.setBackground(TKSettings.KalenderHintergrund);
                Reha.instance.terminpanel.comboFlaeche.setBackground(TKSettings.KalenderHintergrund);
                Reha.instance.terminpanel.TerminFlaeche.setBackground(TKSettings.KalenderHintergrund);
                Reha.instance.terminpanel.viewPanel.validate();
                Reha.instance.terminpanel.viewPanel.repaint();
            }
        } catch (java.lang.NullPointerException n) {
            // fare niente - Terminkalender läuft nicht!!
        }
    }

    private void PanelListenerInit(final int tspalte, Connection connection) {
        oSpalten[tspalte].addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                for (int i = 0; i < 1; i++) {

                    if (e.getKeyCode() == VK_CONTROL) {
                        break;
                    }
                    if (e.getKeyCode() == VK_ALT) {
                        break;
                    }
                    if (e.getKeyCode() == VK_D && e.isControlDown()) {
                        DruckeViewPanel dvp = new DruckeViewPanel();
                        dvp.setPrintPanel(Reha.instance.terminpanel.viewPanel);
                        break;
                    }
                    if ((e.getKeyCode() == VK_F12) && (aktAnsicht != Ansicht.MASKE)) {
                        setAufruf(null);
                        oSpalten[tspalte].requestFocus();
                        break;
                    }
                    if ((e.getKeyCode() == KeyEvent.VK_INSERT && e.isShiftDown())
                            || (e.getKeyCode() == KeyEvent.VK_V && e.isControlDown())) {
                        // Daten in den Kalender schreiben (früher Aufruf über F3)
                        long zeit = System.currentTimeMillis();
                        boolean grobRaus = false;
                        while (wartenAufReady) {
                            try {
                                Thread.sleep(20);
                                if ((System.currentTimeMillis() - zeit) > 1500) {
                                    grobRaus = true;
                                    break;
                                }
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                        if (!grobRaus) {
                            wartenAufReady = true;
                            terminGedropt = false;
                            terminBreak = false;
                            datenAusSpeicherHolen();
                        } else {
                            wartenAufReady = false;
                            SqlInfo.loescheLocksMaschine();
                        }

                        gruppierenAktiv = false;
                        gruppierenBloecke[0] = -1;
                        gruppierenBloecke[1] = -1;
                        oSpalten[gruppierenSpalte].setInGruppierung(false);
                        oSpalten[tspalte].requestFocus();
                        break;
                    }

                    if (((e.getKeyCode() == KeyEvent.VK_INSERT) && (e.isControlDown()))
                            || ((e.getKeyCode() == KeyEvent.VK_C) && (e.isControlDown()))) {

                        int xaktBehandler = -1;
                        datenInSpeicherNehmen();
                        if (terminVergabe.size() > 0) {
                            terminVergabe.clear();
                        }
                        switch (aktAnsicht) {
                        case NORMAL:
                            xaktBehandler = belegung[aktiveSpalte[2]];
                            break;
                        case WOCHE:
                            xaktBehandler = aktiveSpalte[2];
                            break;
                        case MASKE:
                            xaktBehandler = aktiveSpalte[2];
                            break;
                        }
                        terminAufnehmen(xaktBehandler, aktiveSpalte[0]);
                        break;
                    }

                    if (e.getKeyCode() == VK_SHIFT) {
                        if (!Rechte.hatRecht(Rechte.Kalender_termingroup, false)) {
                            gruppierenAktiv = false;
                            oSpalten[tspalte].shiftGedrueckt(true);
                            oSpalten[gruppierenSpalte].setInGruppierung(false);
                            gruppierenBloecke[0] = -1;
                            gruppierenBloecke[1] = -1;
                            break;
                        }
                        if (!gruppierenAktiv) {
                            gruppierenAktiv = true;
                            gruppierenBloecke[0] = aktiveSpalte[0];
                            gruppierenBloecke[1] = aktiveSpalte[0];
                            gruppierenSpalte = aktiveSpalte[2];
                            gruppierenBehandler = (aktAnsicht == Ansicht.NORMAL ? belegung[aktiveSpalte[2]]
                                    : aktiveSpalte[2]);
                            gruppierenKopiert = false;
                            oSpalten[gruppierenSpalte].setInGruppierung(true);
                        }
                        oSpalten[tspalte].shiftGedrueckt(true);
                        break;
                    }
                    if (e.getKeyCode() == VK_ESCAPE) {
                        oCombo[tspalte].requestFocus();
                        e.consume();
                        break;
                    }
                    if ((e.getKeyCode() == VK_PAGE_UP || e.getKeyCode() == VK_UP || e.getKeyCode() == VK_PAGE_DOWN
                            || e.getKeyCode() == VK_DOWN || e.getKeyCode() == VK_ENTER || e.getKeyCode() == VK_LEFT
                            || e.getKeyCode() == VK_RIGHT) && (!e.isControlDown() && (!e.isAltDown()))
                            && (!e.isShiftDown())) {
                        // HauptAufgabe ist Weitergabe an Tastenauswerten
                        e.consume();
                        panelTastenAuswerten(e);
                        oSpalten[tspalte].requestFocus();
                        break;
                    }
                    if ((e.getKeyCode() == VK_UP || e.getKeyCode() == VK_DOWN)
                            && (!e.isControlDown() && (!e.isAltDown())) && (e.isShiftDown())) {
                        // HauptAufgabe ist Weitergabe und Tastenauswerten
                        // gruppierungMalen();
                        panelTastenAuswerten(e);
                        oSpalten[tspalte].requestFocus();
                        break;
                    }
                    if ((e.getKeyCode() == VK_W) && (e.isControlDown())) {
                        // Wochenansicht
                        setWochenanzeige();
                        break;
                    }
                    if ((e.getKeyCode() == VK_N) && (e.isControlDown())) {
                        // Normalansicht
                        setNormalanzeige();
                        break;
                    }
                    if (e.getKeyCode() == VK_P && e.isAltDown()) {
                        doPatSuchen(connection);
                    }
                    // ursprünglich an dieser Stelle
                    if ((e.getKeyCode() == VK_L) && (e.isControlDown())) {
                        /* Neu seit 10.10.2014 */
                        e.consume();
                        gruppierenAktiv = false;
                        gruppierenBloecke[0] = -1;
                        gruppierenBloecke[1] = -1;
                        oSpalten[gruppierenSpalte].setInGruppierung(false);
                        oSpalten[tspalte].requestFocus();
                        /* Ende Neu */
                        terminListe();
                        break;
                    }
                    if (e.getKeyCode() == VK_F1) {
                        // F1
                    }
                    if (e.getKeyCode() == VK_F2) {
                        // F2
                        long zeit = System.currentTimeMillis();
                        boolean grobRaus = false;
                        while (wartenAufReady) {
                            try {
                                Thread.sleep(20);
                                if ((System.currentTimeMillis() - zeit) > 1500) {
                                    grobRaus = true;
                                    break;
                                }
                            } catch (InterruptedException e1) {

                                e1.printStackTrace();
                            }
                        }
                        if (!grobRaus) {
                            wartenAufReady = true;
                            terminGedropt = false;
                            terminBreak = false;
                            datenAusSpeicherHolen();
                        } else {
                            SqlInfo.loescheLocksMaschine();
                            wartenAufReady = false;
                        }
                        gruppierenAktiv = false;
                        gruppierenBloecke[0] = -1;
                        gruppierenBloecke[1] = -1;
                        oSpalten[gruppierenSpalte].setInGruppierung(false);
                        oSpalten[tspalte].requestFocus();
                        break;
                    }
                    if (e.getKeyCode() == VK_F3) {
                        // F3 = Daten in Speicher nehmen
                        int xaktBehandler = -1;
                        datenInSpeicherNehmen();
                        if (terminVergabe.size() > 0) {
                            terminVergabe.clear();
                        }
                        int xaktBehandler1 = xaktBehandler;
                        switch (aktAnsicht) {
                        case NORMAL:
                            xaktBehandler1 = belegung[aktiveSpalte[2]];
                            break;
                        case WOCHE:
                            xaktBehandler1 = aktiveSpalte[2];
                            break;
                        case MASKE:
                            xaktBehandler1 = aktiveSpalte[2];
                            break;
                        }
                        xaktBehandler = xaktBehandler1;
                        terminAufnehmen(xaktBehandler, aktiveSpalte[0]);

                        break;
                    }
                    if ((e.getKeyCode() == VK_F7) && (e.isAltDown()) && (e.isShiftDown())) {
                        if (!Rechte.hatRecht(Rechte.Kalender_termindelete, true)) {
                            wartenAufReady = false;
                            gruppierenAktiv = false;
                            e.consume();
                            oSpalten[tspalte].requestFocus();
                            break;
                        }
                        blockSetzen(999);
                        e.consume();
                        oSpalten[tspalte].requestFocus();
                        break;
                    }
                    if (e.getKeyCode() == VK_F7) {
                        break;
                    }
                    if ((e.getKeyCode() == VK_F8) || (e.getKeyCode() == VK_DELETE && e.isShiftDown())
                            || (e.getKeyCode() == KeyEvent.VK_X && e.isControlDown() && (!e.isAltDown()))) {
                        // F8 / Shift-Entf / Strg-X
                        if ((!Rechte.hatRecht(Rechte.Kalender_termindelete, true))) {
                            // getAktTestTermin("name").equals(""))
                            wartenAufReady = false;
                            gruppierenAktiv = false;
                            e.consume();
                            oSpalten[tspalte].requestFocus();
                            break;
                        }
                        long zeit = System.currentTimeMillis();
                        boolean grobRaus = false;
                        while (wartenAufReady) {
                            try {
                                Thread.sleep(20);
                                if ((System.currentTimeMillis() - zeit) > 1500) {
                                    grobRaus = true;
                                    break;
                                }
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                        // System.out.println("GrobRaus = "+grobRaus);
                        if (!grobRaus) {

                            // int xaktBehandler = -1;
                            wartenAufReady = true;
                            testeObAusmustern();

                            /* Neu Anfang */
                            datenInSpeicherNehmen();
                            /*
                             * if(terminVergabe.size() > 0){ terminVergabe.clear(); } if(ansicht ==
                             * NORMAL_ANSICHT){ xaktBehandler = belegung[aktiveSpalte[2]]; }else if(ansicht
                             * == WOCHEN_ANSICHT){ xaktBehandler = aktiveSpalte[2]; }else if(ansicht ==
                             * MASKEN_ANSICHT){ xaktBehandler = aktiveSpalte[2]; }
                             * terminAufnehmen(xaktBehandler,aktiveSpalte[0]);
                             */
                            /* Neu Ende */
                            blockSetzen(11);

                        } else {
                            SqlInfo.loescheLocksMaschine();
                            wartenAufReady = false;
                        }

                        e.consume();
                        oSpalten[tspalte].requestFocus();
                        break;
                    }
                    if (e.getKeyCode() == VK_F9) {
                        // F9
                        if ((!Rechte.hatRecht(Rechte.Kalender_termindelete, false))) {
                            Rechte.hatRecht(Rechte.Kalender_termindelete, true);
                            wartenAufReady = false;
                            gruppierenAktiv = false;
                            e.consume();
                            oSpalten[tspalte].requestFocus();
                            break;

                        }
                        long zeit = System.currentTimeMillis();
                        boolean grobRaus = false;
                        while (wartenAufReady) {
                            try {
                                Thread.sleep(20);
                                if ((System.currentTimeMillis() - zeit) > 1500) {
                                    grobRaus = true;
                                    break;
                                }
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                        if (!grobRaus) {
                            wartenAufReady = true;
                            testeObAusmustern();
                            blockSetzen(10);
                        } else {
                            SqlInfo.loescheLocksMaschine();
                            wartenAufReady = false;
                        }
                        oSpalten[tspalte].requestFocus();

                        break;
                    }
                    // F11 + Shift
                    if (e.getKeyCode() == KeyEvent.VK_F11 && e.isShiftDown()) {
                        if (!Rechte.hatRecht(Rechte.Kalender_terminconfirm, true)) {
                            gruppeAusschalten();
                        } else {
                            terminBestaetigen(tspalte, false);
                            gruppeAusschalten();
                        }
                        oSpalten[tspalte].requestFocus();
                        break;
                    }
                    // F11 + Strg erzwingt in jeden Fall den Dialog
                    if (e.getKeyCode() == KeyEvent.VK_F11 && e.isControlDown()) {
                        if (!Rechte.hatRecht(Rechte.Kalender_terminconfirm, true)) {
                            gruppeAusschalten();
                        } else {
                            terminBestaetigen(tspalte, true);
                            gruppeAusschalten();
                        }
                        oSpalten[tspalte].requestFocus();
                        break;
                    }

                    if (e.getKeyCode() == KeyEvent.VK_F11 && (!e.isShiftDown()) && (!e.isControlDown())
                            && (!e.isAltDown())) {
                        // nur F11 ohne Shift etc.
                        schnellSuche(connection);
                        break;
                    }
                    if (e.isControlDown() && e.getKeyCode() == VK_O) {
                        // Termin nach oben
                        setUpdateVerbot(true);
                        tauscheTermin(-1);
                        setUpdateVerbot(false);
                        break;
                    }
                    if (e.isControlDown() && e.getKeyCode() == VK_U) {
                        // Termin nach unten
                        setUpdateVerbot(true);
                        tauscheTermin(1);
                        setUpdateVerbot(false);
                        break;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_F1 && (!e.isControlDown()) && (!e.isShiftDown())) {
                        if (infoDlg != null || aktiveSpalte[2] < 0 || aktiveSpalte[0] < 0) {
                            return;
                        }
                        String reznummer = "";
                        // TerminInfo aufrufen
                        if (aktAnsicht == Ansicht.NORMAL) {
                            reznummer = (String) ((Vector) ((ArrayList) vTerm.get(belegung[aktiveSpalte[2]])).get(
                                    1)).get(aktiveSpalte[0]);
                        } else if (aktAnsicht == Ansicht.WOCHE) {
                            reznummer = (String) ((Vector) ((ArrayList) vTerm.get(aktiveSpalte[2])).get(1)).get(
                                    aktiveSpalte[0]);
                        }
                        infoDlg = new InfoDialogTerminInfo(reznummer, null);
                        infoDlg.pack();
                        infoDlg.setLocationRelativeTo(TerminFlaeche);
                        infoDlg.setVisible(true);
                        infoDlg = null;
                        oSpalten[tspalte].requestFocus();

                        break;
                    }
                    oSpalten[tspalte].requestFocus();
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {

                if (e.getKeyCode() == VK_SHIFT) {
                    gruppierenAktiv = false;
                    gruppierenBloecke[0] = -1;
                    gruppierenBloecke[1] = -1;
                    oSpalten[gruppierenSpalte].setInGruppierung(false);
                    oSpalten[tspalte].shiftGedrueckt(false);
                } else {
                    oSpalten[tspalte].requestFocus();
                }
            }
        });

        oSpalten[tspalte].addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                e.setSource(this);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
            }

            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                dragDaten.y = e.getY();
                dragDaten.x = e.getX();
                if (fgp != null) {
                    if (!fgp.isVisible()) {
                        fgp.setVisible(true);
                    }
                    fgp.eventDispatched(e);
                }

                // fgp.setPoint(e.getPoint());
                // fgp.repaint();
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                for (int i = 0; i < 1; i++) {
                    if ((e.getClickCount() == 1) && (e.getButton() == java.awt.event.MouseEvent.BUTTON1)) {
                        KlickSetzen(oSpalten[tspalte], e);
                        oSpalten[tspalte].requestFocus();
                        dragLab[i].setVisible(true);
                        break;
                    }
                    if ((e.getClickCount() == 1) && (e.getButton() == java.awt.event.MouseEvent.BUTTON3)) {
                        if (!gruppierenAktiv) {
                            KlickSetzen(oSpalten[tspalte], e);
                            dragDaten.y = e.getY();
                            dragDaten.x = e.getX();
                            ZeigePopupMenu(e);
                            break;
                        } else {
                            dragDaten.y = e.getY();
                            dragDaten.x = e.getX();
                            ZeigePopupMenu(e);
                            oSpalten[tspalte].requestFocus();
                            break;
                        }
                    }
                    if ((e.getClickCount() == 2) && (e.getButton() == java.awt.event.MouseEvent.BUTTON1)) {
                        final java.awt.event.MouseEvent me = e;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                KlickSetzen(oSpalten[tspalte], me);
                            }
                        });
                        dragDaten.y = e.getY();
                        dragDaten.x = e.getX();
                        if (aktiveSpalte[0] >= 0) {
                            if (aktAnsicht == Ansicht.NORMAL) {
                                setLockStatement((belegung[tspalte] + 1 >= 10
                                        ? Integer.toString(belegung[tspalte] + 1) + "BEHANDLER"
                                        : "0" + (belegung[tspalte] + 1) + "BEHANDLER"), aktuellerTag);
                                new Thread(new LockRecord()).start();
                                long lockzeit = System.currentTimeMillis();
                                while (lockok == 0) {
                                    try {
                                        Thread.sleep(20);
                                        if (System.currentTimeMillis() - lockzeit > 1500) {
                                            break;
                                        }
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                if (lockok > 0) {
                                    setUpdateVerbot(true);
                                    Zeiteinstellen(e.getLocationOnScreen(), belegung[tspalte], aktiveSpalte[0]);
                                    oSpalten[tspalte].requestFocus();
                                    setUpdateVerbot(false);
                                } else {
                                    lockok = 0;
                                    sperreAnzeigen("");
                                    setUpdateVerbot(false);
                                    SqlInfo.loescheLocksMaschine();
                                }
                            } else if (aktAnsicht == Ansicht.WOCHE) { // WOCHEN_ANSICHT muß noch entwickelt werden!
                                if (aktiveSpalte[2] == 0) {

                                    setLockStatement(
                                            (wochenbelegung >= 10 ? Integer.toString(wochenbelegung) + "BEHANDLER"
                                                    : "0" + (wochenbelegung) + "BEHANDLER"),
                                            getWocheErster());
                                } else {
                                    setLockStatement(
                                            (wochenbelegung >= 10 ? Integer.toString(wochenbelegung) + "BEHANDLER"
                                                    : "0" + (wochenbelegung) + "BEHANDLER"),
                                            DatFunk.sDatPlusTage(getWocheErster(), aktiveSpalte[2]));
                                }
                                new Thread(new LockRecord()).start();
                                long lockzeit = System.currentTimeMillis();
                                while (lockok == 0) {
                                    try {
                                        Thread.sleep(20);
                                        if (System.currentTimeMillis() - lockzeit > 1500) {
                                            break;
                                        }
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                                if (lockok > 0) {
                                    setUpdateVerbot(true);
                                    Zeiteinstellen(e.getLocationOnScreen(), aktiveSpalte[2], aktiveSpalte[0]);
                                    oSpalten[tspalte].requestFocus();
                                    setUpdateVerbot(false);
                                } else {
                                    lockok = 0;
                                    sperreAnzeigen(
                                            "(e.getClickCount() == 2) && (e.getButton() == java.awt.event.MouseEvent.BUTTON1)");
                                    setUpdateVerbot(false);
                                    SqlInfo.loescheLocksMaschine();
                                }
                            } else if (aktAnsicht == Ansicht.MASKE) { // WOCHEN_ANSICHT mu� noch entwickelt werden!
                                //// System.out.println("Maskenansicht-Doppelklick");
                                lockok = 1;
                                Zeiteinstellen(e.getLocationOnScreen(), aktiveSpalte[2], aktiveSpalte[0]);
                                oSpalten[tspalte].requestFocus();
                                lockok = 0;
                            }
                        }
                        break;
                    }

                }

            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
            }
        });
        oSpalten[tspalte].addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                // Reha.instance.shiftLabel.setText("Spalte"+tspalte+" / Drag:X="+e.getX()+"
                // Y="+e.getY());
                if (fgp != null) {
                    if (!fgp.isVisible()) {
                        fgp.setVisible(true);
                    }
                    fgp.eventDispatched(e);
                }

            }

            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                dragDaten.y = e.getY();
                dragDaten.x = e.getX();
                if (fgp != null) {
                    if (!fgp.isVisible()) {
                        fgp.setVisible(true);
                    }
                    fgp.eventDispatched(e);
                }
            }
        });
        oSpalten[tspalte].addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                focusHandling(1, -1);

            }

            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                focusHandling(1, 1);
                try {
                    if (!Reha.instance.terminpanel.eltern.isActive) {
                        Reha.instance.terminpanel.eltern.feuereEvent(25554);
                    }
                } catch (Exception ex) {

                }
            }
        });

        oSpalten[tspalte].zeitInit((int) ZeitFunk.MinutenSeitMitternacht(TKSettings.KalenderUmfang[0]),
                (int) ZeitFunk.MinutenSeitMitternacht(TKSettings.KalenderUmfang[1]));

        return;
    }

    public String getWocheErster() {
        return this.wocheErster;
    }

    private void ZeigePopupMenu(MouseEvent me) {
        JPopupMenu jPop = getTerminPopupMenu();
        jPop.show(me.getComponent(), me.getX(), me.getY());
    }

    private JPopupMenu getTerminPopupMenu() {
        if (jPopupMenu == null) {
            jPopupMenu = new JPopupMenu();
            if (aktAnsicht != Ansicht.MASKE) {
                jPopupMenu.add(getTagvor());
                jPopupMenu.add(getTagzurueck());
                jPopupMenu.add(getTagesdialog());
                jPopupMenu.addSeparator();
                JMenu submenu = new JMenu("Termine tauschen");
                submenu.setIcon(SystemConfig.hmSysIcons.get("refresh"));
                submenu.add(getTauschemitvorherigem());
                submenu.add(getTauschemitnachfolger());
                jPopupMenu.add(submenu);
                jPopupMenu.addSeparator();
                jPopupMenu.add(getBehandlerset());
                jPopupMenu.addSeparator();
                jPopupMenu.add(getTerminedespatsuchen());
                jPopupMenu.addSeparator();
                submenu = new JMenu("Patient suchen / Telefonliste");
                submenu.setIcon(new ImageIcon(Path.Instance.getProghome() + "/icons/personen16.gif"));
                submenu.add(getPatientsuchen());
                submenu.add(getTelefonliste());
                jPopupMenu.add(submenu);
                jPopupMenu.addSeparator();
                jPopupMenu.add(getTerminliste());
                jPopupMenu.addSeparator();
                submenu = new JMenu("Termine gruppieren");
                submenu.setIcon(SystemConfig.hmSysIcons.get("att"));
                submenu.add(getGruppezusammenfassen());
                submenu.add(getGruppekopieren());
                submenu.add(getGruppeeinfuegen());
                submenu.add(getGruppeloeschen());
                jPopupMenu.add(submenu);
                jPopupMenu.addSeparator();
                submenu = new JMenu("Termin");
                submenu.setIcon(SystemConfig.hmSysIcons.get("termin"));
                submenu.add(getCopy());
                submenu.add(getCut());
                submenu.add(getPaste());
                submenu.add(getConfirm());
                if (TKSettings.KalenderLangesMenue) {
                    jPopupMenu.add(submenu);
                    jPopupMenu.addSeparator();
                }
                jPopupMenu.add(getNormalanzeige());
                jPopupMenu.add(getWochenanzeige());
            } else if (aktAnsicht == Ansicht.MASKE) {
                jPopupMenu.add(getGruppezusammenfassen());
                jPopupMenu.addSeparator();
                jPopupMenu.add(getKalenderschreiben());
            }
        }
        if (gruppierenAktiv) {
            if (aktAnsicht != Ansicht.MASKE) {
                Behandlerset.setEnabled(false);
                Tagvor.setEnabled(false);
                Tagzurueck.setEnabled(false);
                Tagesdialog.setEnabled(false);
                Gruppezusammenfassen.setEnabled(true);
                Gruppekopieren.setEnabled(true);
                Gruppeloeschen.setEnabled(true);
                Terminliste.setEnabled(false);
                Patientsuchen.setEnabled(false);
                Terminedespatsuchen.setEnabled(false);
                Wochenanzeige.setEnabled(false);
                Normalanzeige.setEnabled(false);
                Tauschemitvorherigem.setEnabled(false);
                Tauschemitnachfolger.setEnabled(false);
            } else if (aktAnsicht == Ansicht.MASKE) {
                Interminkalenderschreiben.setEnabled(false);
                Gruppezusammenfassen.setEnabled(true);
            }
            setGruppierenClipBoard();
        } else if (aktAnsicht != Ansicht.MASKE) {
            Tagvor.setEnabled(true);
            Tagzurueck.setEnabled(true);
            Tagesdialog.setEnabled(true);
            Gruppezusammenfassen.setEnabled(false);
            Gruppekopieren.setEnabled(false);
            Gruppeloeschen.setEnabled(false);
            Terminliste.setEnabled(true);
            Patientsuchen.setEnabled(true);
            Terminedespatsuchen.setEnabled(true);
            Tauschemitvorherigem.setEnabled(true);
            Tauschemitnachfolger.setEnabled(true);
            if (Reha.instance.copyLabel.getText()
                                       .length() == 0) {
                Paste.setEnabled(false);
            } else {
                Paste.setEnabled(true);
            }
            if (aktAnsicht == Ansicht.NORMAL) {
                Behandlerset.setEnabled(true);
                Wochenanzeige.setEnabled(true);
                Normalanzeige.setEnabled(false);
                Tagvor.setEnabled(true);
                Tagzurueck.setEnabled(true);
                Tagesdialog.setEnabled(true);
                Tagvor.setText("einen Tag vorwärts blättern");
                Tagzurueck.setText("einen Tag rückwärts blättern");
                if (((Rechte.hatRecht(Rechte.Kalender_terminconfirminpast, false)) || (this.getAktuellerTag()
                                                                                           .equals(DatFunk.sHeute())))
                        && (Reha.instance.copyLabel.getText()
                                                   .length() != 0)) {
                    Confirm.setEnabled(true);
                } else {
                    Confirm.setEnabled(false);
                }
            } else if (aktAnsicht == Ansicht.WOCHE) {
                Behandlerset.setEnabled(false);
                Wochenanzeige.setEnabled(false);
                Normalanzeige.setEnabled(true);
                Tagvor.setText("eine Woche vorwärts blättern");
                Tagzurueck.setText("eine Woche rückwärts blättern");
                Tagesdialog.setEnabled(false);
                Confirm.setEnabled(false);
            }
        } else if (aktAnsicht == Ansicht.MASKE) {
            Interminkalenderschreiben.setEnabled(true);
            Gruppezusammenfassen.setEnabled(false);
        }
        if (aktAnsicht != Ansicht.MASKE) {
            Gruppeeinfuegen.setEnabled((!gruppierenAktiv) && (gruppierenKopiert));
        }
        return jPopupMenu;
    }

    private JMenuItem getTauschemitvorherigem() {
        if (Tauschemitvorherigem == null) {
            Tauschemitvorherigem = new JMenuItem();
            Tauschemitvorherigem.setText("Termin mit Vorgängertermin tauschen");
            Tauschemitvorherigem.setToolTipText("Strg+O");
            Tauschemitvorherigem.setIcon(SystemConfig.hmSysIcons.get("upw"));
            Tauschemitvorherigem.setRolloverEnabled(true);
            Tauschemitvorherigem.setEnabled(true);
            Tauschemitvorherigem.addActionListener(this);
        }
        return Tauschemitvorherigem;
    }

    private JMenuItem getTauschemitnachfolger() {
        if (Tauschemitnachfolger == null) {
            Tauschemitnachfolger = new JMenuItem();
            Tauschemitnachfolger.setText("Termin mit Nachfolgetermin tauschen");
            Tauschemitnachfolger.setToolTipText("Strg+U");
            Tauschemitnachfolger.setIcon(SystemConfig.hmSysIcons.get("down"));
            Tauschemitnachfolger.setRolloverEnabled(true);
            Tauschemitnachfolger.setEnabled(true);
            Tauschemitnachfolger.addActionListener(this);
        }
        return Tauschemitnachfolger;
    }

    private JMenuItem getBehandlerset() {
        if (Behandlerset == null) {
            Behandlerset = new JMenuItem();
            Behandlerset.setText("Behandler-Set aufrufen");
            Behandlerset.setToolTipText("F12");
            Behandlerset.setRolloverEnabled(true);
            Behandlerset.setEnabled(true);
            Behandlerset.addActionListener(this);
        }
        return Behandlerset;
    }

    /** Neu. */
    private JMenuItem getCopy() {
        if (Copy == null) {
            Copy = new JMenuItem();
            Copy.setText("kopieren");
            Copy.setIcon(SystemConfig.hmSysIcons.get("copy"));
            Copy.setToolTipText("Strg-Einf");
            Copy.setRolloverEnabled(true);
            Copy.setEnabled(true);
            Copy.addActionListener(this);
        }
        return Copy;
    }

    private JMenuItem getCut() {
        if (Cut == null) {
            Cut = new JMenuItem();
            Cut.setText("ausschneiden");
            Cut.setIcon(SystemConfig.hmSysIcons.get("cut"));
            Cut.setToolTipText("Shift-Entf");
            Cut.setRolloverEnabled(true);
            Cut.setEnabled(true);
            Cut.addActionListener(this);
        }
        return Cut;
    }

    private JMenuItem getPaste() {
        if (Paste == null) {
            Paste = new JMenuItem();
            Paste.setText("einfügen");
            Paste.setIcon(SystemConfig.hmSysIcons.get("paste"));
            Paste.setToolTipText("Shift-Einf");
            Paste.setRolloverEnabled(true);
            Paste.setEnabled(true);
            Paste.addActionListener(this);
        }
        return Paste;
    }

    private JMenuItem getConfirm() {
        if (Confirm == null) {
            Confirm = new JMenuItem();
            Confirm.setText("bestätigen");
            Confirm.setIcon(SystemConfig.hmSysIcons.get("confirm"));
            Confirm.setToolTipText("Shift-F11");
            Confirm.setRolloverEnabled(true);
            Confirm.setEnabled(false);
            Confirm.addActionListener(this);
        }
        return Confirm;
    }

    /** Ende. */

    private JMenuItem getTagvor() {
        if (Tagvor == null) {
            Tagvor = new JMenuItem();
            Tagvor.setText("einen Tag vorwärts blättern");
            Tagvor.setIcon(SystemConfig.hmSysIcons.get("right"));
            Tagvor.setToolTipText("Bild-auf");
            Tagvor.setRolloverEnabled(true);
            Tagvor.setEnabled(true);
            Tagvor.addActionListener(this);
        }
        return Tagvor;
    }

    private JMenuItem getTagzurueck() {
        if (Tagzurueck == null) {
            Tagzurueck = new JMenuItem();
            Tagzurueck.setText("einen Tag rückwärts blättern");
            Tagzurueck.setIcon(SystemConfig.hmSysIcons.get("left"));
            Tagzurueck.setToolTipText("Bild-ab");
            Tagzurueck.setRolloverEnabled(true);
            Tagzurueck.setEnabled(true);
            Tagzurueck.addActionListener(this);
        }
        return Tagzurueck;
    }

    private JMenuItem getTagesdialog() {
        if (Tagesdialog == null) {
            Tagesdialog = new JMenuItem();
            Tagesdialog.setText("Datums-Dialog aufrufen");
            Tagesdialog.setIcon(SystemConfig.hmSysIcons.get("dayselect"));
            Tagesdialog.setRolloverEnabled(true);
            Tagesdialog.setEnabled(true);
            Tagesdialog.addActionListener(this);
        }
        return Tagesdialog;
    }

    private JMenuItem getKalenderschreiben() {
        if (Interminkalenderschreiben == null) {
            Interminkalenderschreiben = new JMenuItem();
            Interminkalenderschreiben.setText("Arbeitszeitdefinition in Terminkalender übertragen");
            Interminkalenderschreiben.setRolloverEnabled(true);
            Interminkalenderschreiben.setEnabled(true);
            Interminkalenderschreiben.addActionListener(this);
        }
        return Interminkalenderschreiben;
    }

    private JMenuItem getTerminliste() {
        if (Terminliste == null) {
            Terminliste = new JMenuItem();
            Terminliste.setText("Terminliste aufrufen");
            Terminliste.setRolloverEnabled(true);
            Terminliste.setEnabled(false);
            Terminliste.addActionListener(this);
        }
        return Terminliste;
    }

    private JMenuItem getGruppezusammenfassen() {
        if (Gruppezusammenfassen == null) {
            Gruppezusammenfassen = new JMenuItem();
            Gruppezusammenfassen.setText("Gruppierung zusammenfassen");
            Gruppezusammenfassen.setRolloverEnabled(true);
            Gruppezusammenfassen.setEnabled(false);
            Gruppezusammenfassen.addActionListener(this);
        }
        return Gruppezusammenfassen;
    }

    private JMenuItem getGruppeloeschen() {
        if (Gruppeloeschen == null) {
            Gruppeloeschen = new JMenuItem();
            Gruppeloeschen.setText("Gruppierung löschen");
            Gruppeloeschen.setRolloverEnabled(true);
            Gruppeloeschen.setEnabled(false);
            Gruppeloeschen.addActionListener(this);
        }
        return Gruppeloeschen;
    }

    private JMenuItem getGruppekopieren() {
        if (Gruppekopieren == null) {
            Gruppekopieren = new JMenuItem();
            Gruppekopieren.setText("Gruppierung kopieren");
            Gruppekopieren.setRolloverEnabled(true);
            Gruppekopieren.setEnabled(false);
            Gruppekopieren.addActionListener(this);
        }
        return Gruppekopieren;
    }

    private JMenuItem getGruppeeinfuegen() {
        if (Gruppeeinfuegen == null) {
            Gruppeeinfuegen = new JMenuItem();
            Gruppeeinfuegen.setText("Termingruppe einfügen");
            Gruppeeinfuegen.setRolloverEnabled(true);
            Gruppeeinfuegen.setEnabled(false);
            Gruppeeinfuegen.addActionListener(this);
        }
        return Gruppeeinfuegen;
    }

    private JMenuItem getPatientsuchen() {
        if (Patientsuchen == null) {
            Patientsuchen = new JMenuItem();
            Patientsuchen.setText("Patient suchen (über Rezept-Nummer)");
            Patientsuchen.setToolTipText("Alt-P");
            Patientsuchen.setIcon(SystemConfig.hmSysIcons.get("patsearch"));
            Patientsuchen.setActionCommand("PatRezSuchen");
            Patientsuchen.setRolloverEnabled(true);
            Patientsuchen.addActionListener(this);
        }
        return Patientsuchen;
    }

    private JMenuItem getTelefonliste() {
        if (Telefonliste == null) {
            Telefonliste = new JMenuItem();
            Telefonliste.setText("Telefonliste aller Patienten (über Rezept-Nummer)");
            Telefonliste.setIcon(SystemConfig.hmSysIcons.get("tellist"));
            Telefonliste.setActionCommand("TelefonListe");
            Telefonliste.setRolloverEnabled(true);
            Telefonliste.addActionListener(this);
        }
        return Telefonliste;
    }

    private JMenuItem getTerminedespatsuchen() {
        if (Terminedespatsuchen == null) {
            Terminedespatsuchen = new JMenuItem();
            Terminedespatsuchen.setText("Schnellsuche (Heute + 4 Tage)");
            Terminedespatsuchen.setIcon(SystemConfig.hmSysIcons.get("quicksearch"));
            Terminedespatsuchen.setRolloverEnabled(true);
            Terminedespatsuchen.addActionListener(this);
        }
        return Terminedespatsuchen;
    }

    private JMenuItem getNormalanzeige() {
        if (Normalanzeige == null) {
            Normalanzeige = new JMenuItem();
            Normalanzeige.setText("Normalanzeige (7 Kollegen gleicher Tag)");
            Normalanzeige.setToolTipText("Strg-N");
            Normalanzeige.setIcon(SystemConfig.hmSysIcons.get("day"));
            Normalanzeige.setRolloverEnabled(true);
            Normalanzeige.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setNormalanzeige();
                }
            });
        }
        return Normalanzeige;
    }

    private void setNormalanzeige() {
        try {
            if (aktAnsicht == Ansicht.NORMAL) {
                JOptionPane.showMessageDialog(null, "Sie sind bereits in der Normalanzeige....");
                return;
            }

            oCombo[0].setSelectedItem(sbelegung[0]);
            oCombo[0].setEnabled(true);
            oCombo[1].setSelectedItem(sbelegung[1]);
            oCombo[1].setEnabled(true);
            oCombo[2].setSelectedItem(sbelegung[2]);
            oCombo[2].setEnabled(true);
            oCombo[3].setSelectedItem(sbelegung[3]);
            oCombo[3].setEnabled(true);
            oCombo[4].setSelectedItem(sbelegung[4]);
            oCombo[4].setEnabled(true);
            oCombo[5].setSelectedItem(sbelegung[5]);
            oCombo[5].setEnabled(true);
            oCombo[6].setSelectedItem(sbelegung[6]);
            oCombo[6].setEnabled(true);

            this.aktAnsicht = Ansicht.NORMAL;

            try {
                showDaysInWeekView(Ansicht.NORMAL);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            aktiveSpalte[0] = 0;
            aktiveSpalte[1] = 0;
            aktiveSpalte[2] = 0;
            aktiveSpalte[3] = 0;
            for (int i = 0; i < 7; i++) {
                oSpalten[i].spalteDeaktivieren();
            }
            ansichtStatement(aktuellerTag, aktAnsicht);
            Normalanzeige.setEnabled(false);
            Wochenanzeige.setEnabled(true);
        } catch (Exception ex) {
        }
    }

    private JMenuItem getWochenanzeige() {
        if (Wochenanzeige == null) {
            Wochenanzeige = new JMenuItem();
            Wochenanzeige.setText("Wochenanzeige (1 Kollege 7 Tage)");
            Wochenanzeige.setToolTipText("Strg-W");
            Wochenanzeige.setIcon(SystemConfig.hmSysIcons.get("week"));
            Wochenanzeige.setRolloverEnabled(true);
            Wochenanzeige.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setWochenanzeige();
                }

            });
        }
        return Wochenanzeige;
    }

    private void setWochenanzeige() {
        if (aktAnsicht == Ansicht.WOCHE) {
            JOptionPane.showMessageDialog(null, "Sie sind bereits in der Wochenanzeige....");
            return;
        }
        sbelegung[0] = (String) oCombo[0].getSelectedItem();
        sbelegung[1] = (String) oCombo[1].getSelectedItem();
        sbelegung[2] = (String) oCombo[2].getSelectedItem();
        sbelegung[3] = (String) oCombo[3].getSelectedItem();
        sbelegung[4] = (String) oCombo[4].getSelectedItem();
        sbelegung[5] = (String) oCombo[5].getSelectedItem();
        sbelegung[6] = (String) oCombo[6].getSelectedItem();

        this.aktAnsicht = Ansicht.WOCHE;

        oCombo[0].setSelectedItem(oCombo[aktiveSpalte[2]].getSelectedItem());

        this.wocheErster = DatFunk.WocheErster(aktuellerTag);
        try {
            showDaysInWeekView(Ansicht.WOCHE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        oCombo[1].setSelectedIndex(0);
        oCombo[2].setSelectedIndex(0);
        oCombo[3].setSelectedIndex(0);
        oCombo[4].setSelectedIndex(0);
        oCombo[5].setSelectedIndex(0);
        oCombo[6].setSelectedIndex(0);

        oCombo[1].setEnabled(false);
        oCombo[2].setEnabled(false);
        oCombo[3].setEnabled(false);
        oCombo[4].setEnabled(false);
        oCombo[5].setEnabled(false);
        oCombo[6].setEnabled(false);

        aktiveSpalte[0] = 0;
        aktiveSpalte[1] = 0;
        aktiveSpalte[2] = 0;
        aktiveSpalte[3] = 0;
        for (int i = 0; i < 7; i++) {
            oSpalten[i].spalteDeaktivieren();
        }
        ansichtStatement(aktuellerTag, aktAnsicht);
        try {
            Normalanzeige.setEnabled(true);
            Wochenanzeige.setEnabled(false);
        } catch (NullPointerException ex) {

        }
    }

    private void showDaysInWeekView(Ansicht ansicht) {
        // System.out.println("ShowDays in Ansicht "+ansicht+" - WocheErster =
        // "+wocheErster);
        for (int i = 1; i < 7; i++) {
            if (!"./.".equals(oCombo[i].getItemAt(0)
                                       .toString())) {
                oCombo[i].removeItemAt(0);
                if (ansicht != Ansicht.WOCHE) {
                    continue;
                }
            }
            if (ansicht == Ansicht.WOCHE) {
                oCombo[i].insertItemAt(dayshortname[i] + DatFunk.sDatPlusTage(wocheErster, i), 0);
                oCombo[i].setSelectedIndex(0);
            }
        }
    }

    public int[] getGruppierenClipBoard() {
        return gruppierenClipBoard;
    }

    private void setGruppierenClipBoard() {
        gruppierenClipBoard[0] = gruppierenBloecke[0];
        gruppierenClipBoard[1] = gruppierenBloecke[1];
        gruppierenClipBoard[2] = gruppierenSpalte;
        gruppierenClipBoard[3] = gruppierenBehandler;
    }

    private void panelTastenAuswerten(KeyEvent e) {
        e.consume();
        int anz = -1;
        switch (e.getKeyCode()) {
        case VK_PAGE_UP: // Bild auf
            if (interminEdit) {
                return;
            }
            intagWahl = true;
            try {
                tagSprung(this.aktuellerTag, 1);
                if (aktiveSpalte[2] >= 0) {
                    oSpalten[aktiveSpalte[2]].setSpalteaktiv(false);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Fehler beim Aufruf des Datumdialog. Fehler =\n" + ex.getMessage());
            }
            intagWahl = false;
            break;
        case VK_PAGE_DOWN: // Bild ab
            if (interminEdit) {
                return;
            }
            intagWahl = true;
            try {
                tagSprung(this.aktuellerTag, -1);
                if (aktiveSpalte[2] >= 0) {
                    oSpalten[aktiveSpalte[2]].setSpalteaktiv(false);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Fehler beim Aufruf des Datumdialog. Fehler =\n" + ex.getMessage());
            }
            intagWahl = false;
            break;

        case VK_UP: // Pfeil auf
            if (!gruppierenAktiv) {
                try {
                    if (((aktiveSpalte[0] >= 0) && (aktiveSpalte[2] >= 0) && (belegung[aktiveSpalte[2]] >= 0))
                            || ((aktiveSpalte[0] >= 0) && (aktiveSpalte[2] >= 0) && (maskenbelegung >= 0))) {
                        if (aktAnsicht == Ansicht.NORMAL) {
                            anz = ((Vector<?>) ((ArrayList<?>) vTerm.get(belegung[aktiveSpalte[2]])).get(0)).size();
                        } else if (aktAnsicht == Ansicht.WOCHE) {
                            anz = ((Vector<?>) ((ArrayList<?>) vTerm.get(aktiveSpalte[2])).get(0)).size();
                        } else if (aktAnsicht == Ansicht.MASKE) {
                            anz = ((Vector<?>) ((ArrayList<?>) vTerm.get(aktiveSpalte[2])).get(0)).size();
                        }
                        if (anz > 1) {
                            if (aktiveSpalte[0] == 0) {
                                aktiveSpalte[0] = anz - 1;
                                aktiveSpalte[1] = anz - 1;
                            } else {
                                aktiveSpalte[0] = aktiveSpalte[0] - 1;
                                aktiveSpalte[1] = aktiveSpalte[0] - 1;
                            }
                            if (dragLab[aktiveSpalte[2]].getIcon() != null) {
                                dragLab[aktiveSpalte[2]].setIcon(null);
                                dragLab[aktiveSpalte[2]].setText("");
                                oSpalten[aktiveSpalte[2]].spalteDeaktivieren();
                            }
                            oSpalten[aktiveSpalte[2]].setSpalteaktiv(false);
                            oSpalten[aktiveSpalte[2]].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[0]);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    if (aktAnsicht == Ansicht.NORMAL) {
                        anz = ((Vector<?>) ((ArrayList<?>) vTerm.get(belegung[aktiveSpalte[2]])).get(0)).size();
                    } else if (aktAnsicht == Ansicht.WOCHE) {
                        anz = ((Vector<?>) ((ArrayList<?>) vTerm.get(aktiveSpalte[2])).get(0)).size();
                    } else if (aktAnsicht == Ansicht.MASKE) {
                        anz = ((Vector<?>) ((ArrayList<?>) vTerm.get(aktiveSpalte[2])).get(0)).size();
                    }
                    if ((gruppierenBloecke[1] > 0) && (anz > 0)) {
                        gruppierenBloecke[1] = gruppierenBloecke[1] - 1;
                        if (dragLab[aktiveSpalte[2]].getIcon() != null) {
                            dragLab[aktiveSpalte[2]].setIcon(null);
                            dragLab[aktiveSpalte[2]].setText("");
                            oSpalten[aktiveSpalte[2]].spalteDeaktivieren();
                        }
                        oSpalten[gruppierenSpalte].gruppierungZeichnen(gruppierenBloecke.clone());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            break;
        case VK_DOWN: // Pfeil ab
            if (!gruppierenAktiv) {
                try {
                    if (((aktiveSpalte[0] >= 0) && (aktiveSpalte[2] >= 0) && (belegung[aktiveSpalte[2]] >= 0))
                            || ((aktiveSpalte[0] >= 0) && (aktiveSpalte[2] >= 0) && (maskenbelegung >= 0))) {
                        if (aktAnsicht == Ansicht.NORMAL) {
                            try {
                                anz = ((Vector<?>) ((ArrayList<?>) vTerm.get(belegung[aktiveSpalte[2]])).get(0)).size();
                            } catch (java.lang.ArrayIndexOutOfBoundsException ob) {
                                // System.out.println("Spalte nicht belegt");
                            }
                        } else if (aktAnsicht == Ansicht.WOCHE) {
                            anz = ((Vector<?>) ((ArrayList<?>) vTerm.get(aktiveSpalte[2])).get(0)).size();
                        } else if (aktAnsicht == Ansicht.MASKE) {
                            anz = ((Vector<?>) ((ArrayList<?>) vTerm.get(aktiveSpalte[2])).get(0)).size();
                        }
                        if (anz > 1) {
                            if (aktiveSpalte[0] == anz - 1) {
                                aktiveSpalte[0] = 0;
                                aktiveSpalte[1] = 0;
                            } else {
                                aktiveSpalte[0] = aktiveSpalte[0] + 1;
                                aktiveSpalte[1] = aktiveSpalte[0] + 1;
                            }
                            if (dragLab[aktiveSpalte[2]].getIcon() != null) {
                                dragLab[aktiveSpalte[2]].setIcon(null);
                                dragLab[aktiveSpalte[2]].setText("");
                                oSpalten[aktiveSpalte[2]].spalteDeaktivieren();
                            }
                            oSpalten[aktiveSpalte[2]].setSpalteaktiv(false);
                            oSpalten[aktiveSpalte[2]].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[0]);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    if (belegung[aktiveSpalte[2]] == -1) {
                        return;
                    }
                    switch (aktAnsicht) {
                    case NORMAL:
                        anz = ((Vector<?>) ((ArrayList<?>) vTerm.get(belegung[aktiveSpalte[2]])).get(0)).size();
                        break;
                    case WOCHE:
                        anz = ((Vector<?>) ((ArrayList<?>) vTerm.get(aktiveSpalte[2])).get(0)).size();
                        break;
                    case MASKE:
                        anz = ((Vector<?>) ((ArrayList<?>) vTerm.get(aktiveSpalte[2])).get(0)).size();
                        break;
                    }
                    if ((gruppierenBloecke[1] < anz - 1) && anz > 0) {
                        gruppierenBloecke[1] = gruppierenBloecke[1] + 1;
                        if (dragLab[aktiveSpalte[2]].getIcon() != null) {
                            dragLab[aktiveSpalte[2]].setIcon(null);
                            dragLab[aktiveSpalte[2]].setText("");
                            oSpalten[aktiveSpalte[2]].spalteDeaktivieren();
                        }
                        oSpalten[gruppierenSpalte].gruppierungZeichnen(gruppierenBloecke.clone());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            break;
        case VK_LEFT: // Pfeil nach links
            if (aktiveSpalte[2] == 0) {
                oSpalten[aktiveSpalte[2]].spalteDeaktivieren();
                aktiveSpalte[2] = 6;
                aktiveSpalte[0] = 0;
                aktiveSpalte[1] = 0;
                oSpalten[aktiveSpalte[2]].requestFocus();
                oSpalten[aktiveSpalte[2]].setSpalteaktiv(false);
                oSpalten[aktiveSpalte[2]].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[1]);
            } else {
                oSpalten[aktiveSpalte[2]].spalteDeaktivieren();
                aktiveSpalte[2] = aktiveSpalte[2] - 1;
                aktiveSpalte[0] = 0;
                aktiveSpalte[1] = 0;
                oSpalten[aktiveSpalte[2]].requestFocus();
                oSpalten[aktiveSpalte[2]].setSpalteaktiv(false);
                oSpalten[aktiveSpalte[2]].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[1]);
            }
            break;

        case VK_RIGHT: // Pfeil nach rechts
            if (aktiveSpalte[2] == 6) {
                oSpalten[aktiveSpalte[2]].spalteDeaktivieren();
                aktiveSpalte[2] = 0;
                aktiveSpalte[0] = 0;
                aktiveSpalte[1] = 0;
                oSpalten[aktiveSpalte[2]].requestFocus();
                oSpalten[aktiveSpalte[2]].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[1]);
            } else {
                oSpalten[aktiveSpalte[2]].spalteDeaktivieren();
                aktiveSpalte[2] = aktiveSpalte[2] + 1;
                aktiveSpalte[0] = 0;
                aktiveSpalte[1] = 0;
                oSpalten[aktiveSpalte[2]].requestFocus();
                oSpalten[aktiveSpalte[2]].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[1]);
            }
            break;

        case VK_ENTER: // ReturnTaste
            if (intagWahl) {
                return;
            }
            interminEdit = true;
            int[] position;
            int x, y;
            Point pPosition = new Point();
            position = oSpalten[aktiveSpalte[2]].getPosition();
            pPosition = oSpalten[aktiveSpalte[2]].getLocationOnScreen();
            x = pPosition.x + position[0] + (oSpalten[aktiveSpalte[2]].getWidth() / 2);
            y = pPosition.y + position[1];
            if (aktAnsicht == Ansicht.NORMAL) {
                setLockStatement((belegung[aktiveSpalte[2]] + 1 >= 10
                        ? Integer.toString(belegung[aktiveSpalte[2]] + 1) + "BEHANDLER"
                        : "0" + (belegung[aktiveSpalte[2]] + 1) + "BEHANDLER"), aktuellerTag);
                new Thread(new LockRecord()).start();
                long lockzeit = System.currentTimeMillis();
                while (lockok == 0) {
                    try {
                        Thread.sleep(20);
                        if (System.currentTimeMillis() - lockzeit > 1500) {
                            break;
                        }
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                if (lockok > 0) {
                    if (intagWahl) {
                        return;
                    }
                    setUpdateVerbot(true);
                    Zeiteinstellen(new Point(x, y), belegung[aktiveSpalte[2]], aktiveSpalte[0]);
                    interminEdit = false;
                    oSpalten[aktiveSpalte[2]].setSpalteaktiv(false);
                    oSpalten[aktiveSpalte[2]].repaint();
                    oSpalten[aktiveSpalte[2]].requestFocus();
                    setUpdateVerbot(false);
                } else {
                    lockok = 0;
                    sperreAnzeigen("ansicht==NORMAL_ANSICHT - case Taste = 10");
                    setUpdateVerbot(false);
                    SqlInfo.loescheLocksMaschine();
                    interminEdit = false;
                }
            } else if (aktAnsicht == Ansicht.WOCHE) {
                if (aktiveSpalte[2] == 0) {
                    setLockStatement((wochenbelegung >= 10 ? Integer.toString(wochenbelegung) + "BEHANDLER"
                            : "0" + (wochenbelegung) + "BEHANDLER"), getWocheErster());
                } else {
                    setLockStatement(
                            (wochenbelegung >= 10 ? Integer.toString(wochenbelegung) + "BEHANDLER"
                                    : "0" + (wochenbelegung) + "BEHANDLER"),
                            DatFunk.sDatPlusTage(getWocheErster(), aktiveSpalte[2]));
                }
                new Thread(new LockRecord()).start();
                long lockzeit = System.currentTimeMillis();
                while (lockok == 0) {
                    try {
                        Thread.sleep(20);
                        if (System.currentTimeMillis() - lockzeit > 1500) {
                            break;
                        }
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                if (lockok > 0) {
                    if (intagWahl) {
                        return;
                    }
                    setUpdateVerbot(true);
                    Zeiteinstellen(new Point(x, y), aktiveSpalte[2], aktiveSpalte[0]);
                    interminEdit = false;
                    oSpalten[aktiveSpalte[2]].setSpalteaktiv(false);
                    oSpalten[aktiveSpalte[2]].requestFocus();
                    oSpalten[aktiveSpalte[2]].repaint();
                    setUpdateVerbot(false);
                } else {
                    lockok = 0;
                    sperreAnzeigen("ansicht==WOCHEN_ANSICHT - case Taste = 10");
                    setUpdateVerbot(false);
                    interminEdit = false;
                    SqlInfo.loescheLocksMaschine();
                }
            } else if (aktAnsicht == Ansicht.MASKE) { // WOCHEN_ANSICHT muß noch entwickelt werden!
                e.consume();
                lockok = 1;
                if (intagWahl) {
                    return;
                }
                Zeiteinstellen(new Point(x, y), aktiveSpalte[2], aktiveSpalte[0]);
                interminEdit = false;
                oSpalten[aktiveSpalte[2]].setSpalteaktiv(false);
                oSpalten[aktiveSpalte[2]].repaint();
                lockok = 0;
            }
            break;
        }
        return;
    }

    private Point positionErmitteln() {
        Point pPosition;
        int[] position;
        int x, y;
        position = oSpalten[aktiveSpalte[2]].getPosition();
        pPosition = oSpalten[aktiveSpalte[2]].getLocationOnScreen();
        x = pPosition.x + position[0] + oSpalten[aktiveSpalte[2]].getWidth() / 2;
        y = pPosition.y + position[1];
        return new Point(x, y);
    }

    private void Zeiteinstellen(Point position, int behandler, int block) {
        if (behandler < 0) {
            starteUnlock();
            return;
        }
        ArrayList<Vector<String>> terminangabe = (ArrayList<Vector<String>>) vTerm.get(behandler);
        terminangaben = new Block(terminangabe.get(0)
                                                                             .get(block),
        terminangabe.get(1)
                                                          .get(block),
        terminangabe.get(2)
                                                          .get(block),
        terminangabe.get(3)
                                                          .get(block),
        terminangabe.get(4)
                                                          .get(block),
        Integer.toString(block));

        /* Test der Berechtigungen */
        if (!rechteTest(terminangaben.getName0())) {
            starteUnlock();
            wartenAufReady = false;
            setUpdateVerbot(false);
            return;
        }

        if (lockok > 0) {
            this.zf = new Zeitfenster(this);
            int x, y;
            x = position.x;
            int xvp = this.viewPanel.getLocationOnScreen().x + this.viewPanel.getWidth();
            if ((x + zf.getWidth() + 10) > xvp) {
                x = x - zf.getWidth();
            }
            y = position.y;
            int yvp = this.viewPanel.getLocationOnScreen().y + this.viewPanel.getHeight();
            if (y + zf.getHeight() > yvp) {
                y = y - zf.getHeight();
            } else {
                // y=y-(zf.getHeight()/2);
            }
            zf.pack();
            zf.setLocation(x, y);
            zf.toFront();
            zf.setModal(true);
            zf.setVisible(true);
            boolean update = false;

            if (!this.terminrueckgabe.getStartzeit2().isEmpty() && !this.terminrueckgabe.getDauer3().isEmpty()) {
                for (int i = 0; i <= 4; i++) {
                    if (!this.terminangaben.equals(this.terminrueckgabe)) {
                        update = true;
                        break;
                    }
                }
            }
            if (update) {

                if (lockok > 0 && aktAnsicht == Ansicht.NORMAL) {
                    Tblock tbl = new Tblock();
                    spaltenDatumSetzen(true);
                    if ((tbl.TblockInit(this, this.terminrueckgabe, aktiveSpalte[2], aktiveSpalte[0],
                            belegung[aktiveSpalte[2]], vTerm, spaltenDatum, 0)) >= 0) {
                        setUpdateVerbot(true);
                        oSpalten[aktiveSpalte[2]].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[0]);
                        oSpalten[aktiveSpalte[2]].datenZeichnen(vTerm, belegung[aktiveSpalte[2]]);
                        setUpdateVerbot(false);
                    }
                } else if (lockok > 0 && aktAnsicht == Ansicht.WOCHE) {
                    Tblock tbl = new Tblock();
                    spaltenDatumSetzen(false);
                    if ((tbl.TblockInit(this, this.terminrueckgabe, aktiveSpalte[2], aktiveSpalte[0], aktiveSpalte[2],
                            vTerm, spaltenDatum, this.wocheBehandler)) >= 0) {
                        setUpdateVerbot(true);
                        oSpalten[aktiveSpalte[2]].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[0]);
                        oSpalten[aktiveSpalte[2]].datenZeichnen(vTerm, aktiveSpalte[2]);
                        setUpdateVerbot(false);
                    }
                } else if (lockok > 0 && aktAnsicht == Ansicht.MASKE) {
                    Tblock tbl = new Tblock();
                    spaltenDatumSetzen(true);
                    if ((tbl.TblockInit(this, this.terminrueckgabe, aktiveSpalte[2], aktiveSpalte[0], aktiveSpalte[2],
                            vTerm, spaltenDatum, maskenbelegung)) >= 0) {
                        setUpdateVerbot(true);
                        oSpalten[aktiveSpalte[2]].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[0]);
                        oSpalten[aktiveSpalte[2]].datenZeichnen(vTerm, aktiveSpalte[2]);
                        setUpdateVerbot(false);
                    }
                }
            } else {
                setUpdateVerbot(false);
                starteUnlock();
            }
            this.zf = null;
            if ((SystemConfig.logVTermine || SystemConfig.logAlleTermine) && update) {
                if (SystemConfig.logVTermine) {
                    if ((terminangaben.getName0().startsWith("V?") || terminrueckgabe.getName0().startsWith("V?"))) {

                        schreibeLog(terminangaben, terminrueckgabe);
                    }
                } else if (SystemConfig.logAlleTermine) {
                    schreibeLog(terminangaben, terminrueckgabe);
                }
            }
            /* V-Termine -Log Hier rein */
        } // von rlockok > 0
    }

    private static void schreibeLog(final Block alt, final Block neu) {
        new Thread() {

            @Override
            public void run() {
                String cmd = "insert into vlog set datum='" + LocalDateTime.now()
                                                                           .format(ddmmyyy_hhmmss)
                        + "', benutzer='" + Reha.aktUser + "', maschine='" + SystemConfig.dieseMaschine + "', vname='"
                        + alt.getName0() + "', " + "vreznr='" + alt.getRezeptnr1() + "', vdauer='" + alt.getDauer3() + "', vstart='" + alt.getStartzeit2()
                        + "', " + "vend='" + alt.getEndzeit4() + "', nname='" + neu.getName0() + "', nreznr='" + neu.getRezeptnr1() + "', ndauer='"
                        + neu.getDauer3() + "', " + "nstart='" + neu.getStartzeit2() + "', nend='" + neu.getEndzeit4() + "'";
                SqlInfo.sqlAusfuehren(cmd);
            }
        }.start();
    }

    private boolean rechteTest(String testtermin) {
        /* Test der Berechtigungen */
        boolean teil = Rechte.hatRecht(Rechte.Kalender_terminanlegenteil, false);
        boolean voll = Rechte.hatRecht(Rechte.Kalender_terminanlegenvoll, false);
        if (testtermin.trim()
                      .equals("")
                && !(teil || voll)) {
            Rechte.hatRecht(Rechte.Kalender_terminanlegenteil, true);
            return false;
        }
        if (!testtermin.trim()
                       .equals("")
                && !voll) {
            Rechte.hatRecht(Rechte.Kalender_terminanlegenvoll, true);
            return false;
        }
        return true;
    }

    private void spaltenDatumSetzen(boolean heute) {
        if (heute) {
            spaltenDatum[0] = this.aktuellerTag;
            spaltenDatum[1] = this.aktuellerTag;
            spaltenDatum[2] = this.aktuellerTag;
            spaltenDatum[3] = this.aktuellerTag;
            spaltenDatum[4] = this.aktuellerTag;
            spaltenDatum[5] = this.aktuellerTag;
            spaltenDatum[6] = this.aktuellerTag;
        } else {
            spaltenDatum[0] = this.wocheErster;
            spaltenDatum[1] = DatFunk.sDatPlusTage(this.wocheErster, 1);
            spaltenDatum[2] = DatFunk.sDatPlusTage(this.wocheErster, 2);
            spaltenDatum[3] = DatFunk.sDatPlusTage(this.wocheErster, 3);
            spaltenDatum[4] = DatFunk.sDatPlusTage(this.wocheErster, 4);
            spaltenDatum[5] = DatFunk.sDatPlusTage(this.wocheErster, 5);
            spaltenDatum[6] = DatFunk.sDatPlusTage(this.wocheErster, 6);
        }
    }

    public void setWerte(String[] srueck) {
        this.terminrueckgabe = new Block(srueck);
    }

    private void setzeRueckgabe() {
        int behandler = -1;
        int block = -1;
        switch (aktAnsicht) {
        case NORMAL:
            behandler = belegung[aktiveSpalte[2]];
            block = aktiveSpalte[0];
            break;
        case WOCHE:
            behandler = aktiveSpalte[2];
            block = aktiveSpalte[0];
            break;
        case MASKE:
            behandler = aktiveSpalte[2];
            block = aktiveSpalte[0];
            break;
        }
        try {
            ArrayList<?> behandlerVector = (ArrayList<?>) vTerm.get(behandler);
            terminrueckgabe = new Block( (String) ((Vector<?>) behandlerVector.get(0)).get(block),
            (String) ((Vector<?>) behandlerVector.get(1)).get(block),
            (String) ((Vector<?>) behandlerVector.get(2)).get(block),
            (String) ((Vector<?>) behandlerVector.get(3)).get(block),
            (String) ((Vector<?>) behandlerVector.get(4)).get(block),
            Integer.toString(block));
        } catch (ArrayIndexOutOfBoundsException ex) {
            terminrueckgabe = Block.EMPTYBLOCK;

            neuerBlockAktiv(((ArrayList) vTerm.get(behandler)).size());
        }
    }

    public Block getWerte() {
        return this.terminangaben;
    }

    public JXPanel getViewPanel() {
        return this.viewPanel;
    }

    public void setUpdateVerbot(boolean lwert) {
        this.updateverbot = lwert;
    }

    public boolean getUpdateVerbot() {
        return this.updateverbot;
    }

    int aktuellesSet() {
        return aktuellesSet.index;
    }

    void neuerBlockAktiv(int neuBlock) {
        aktiveSpalte[0] = neuBlock;
        aktiveSpalte[1] = neuBlock;
    }

    private void KlickSetzen(TherapeutenTag oSpalten2, MouseEvent e) {
        aktiveSpalte = oSpalten2.BlockTest(e.getX(), e.getY(), aktiveSpalte);
        if (aktiveSpalte[2] != aktiveSpalte[3]) {
            if (gruppierenAktiv) {
                gruppierenAktiv = false;
                oSpalten[gruppierenSpalte].setInGruppierung(false);
                oSpalten[gruppierenSpalte].repaint();
                gruppierenBloecke[0] = -1;
                gruppierenBloecke[1] = -1;
            }
            oSpalten2.blockGeklickt(aktiveSpalte[0]);
            oSpalten[aktiveSpalte[3]].blockGeklickt(-1);
        } else {
            if (gruppierenAktiv) {
                oSpalten[gruppierenSpalte].setInGruppierung(true);
                gruppierenBloecke[1] = aktiveSpalte[0];
                oSpalten[gruppierenSpalte].gruppierungZeichnen(gruppierenBloecke.clone());
                oSpalten[gruppierenSpalte].requestFocus();
            } else {
                oSpalten2.blockGeklickt(aktiveSpalte[0]);
            }
        }
        return;
    }

    public void setAufruf(Point p) {
        if (aktAnsicht == Ansicht.WOCHE) {
            JOptionPane.showMessageDialog(null,
                    "Aufruf Terminset ist nur in der Normalansicht möglich (und sinnvoll...)");
            return;
        }
        Point xpoint = null;
        SetWahl sw = null;
        if (p == null) {
            xpoint = this.viewPanel.getLocationOnScreen();
            setUpdateVerbot(true);
            setSwSetWahl("./.");
            sw = new SetWahl(this);
            xpoint.x = xpoint.x + (this.viewPanel.getWidth() / 2) - (sw.getWidth() / 2);
            xpoint.y = xpoint.y + (this.viewPanel.getHeight() / 2) - (sw.getHeight() / 2);

        } else {
            xpoint = p;
            setUpdateVerbot(true);
            setSwSetWahl("./.");
            sw = new SetWahl(this);
            xpoint.x = xpoint.x - (sw.getWidth() / 2);
            xpoint.y = xpoint.y - (sw.getHeight() / 2);
        }
        sw.setLocation(xpoint);

        sw.pack();
        sw.setVisible(true);
        setUpdateVerbot(false);
        if (!"./.".equals(sw.ret)) {
            aktuellesSet = BehandlerSets.find(sw.ret);
            String[] sSet = aktuellesSet.getMembers().toArray(new String[0]);
            oCombo[0].setSelectedItem(sSet[0]);
            oCombo[1].setSelectedItem(sSet[1]);
            oCombo[2].setSelectedItem(sSet[2]);
            oCombo[3].setSelectedItem(sSet[3]);
            oCombo[4].setSelectedItem(sSet[4]);
            oCombo[5].setSelectedItem(sSet[5]);
            oCombo[6].setSelectedItem(sSet[6]);
        }
    }

    private void SetzeLabel() {
        String ss = aktiveSpalte[0] + "," + aktiveSpalte[1] + "," + aktiveSpalte[2] + "," + aktiveSpalte[3];
        Reha.instance.messageLabel.setText(ss);
    }

    private void holeFocus() {
        oSpalten[aktiveSpalte[2]].requestFocus();
    }

    private void focusHandling(int panel, int plusminus) {
        focus[panel] = focus[panel] + plusminus;
        if (focus[0] == 0 && focus[1] == 0) {

            this.hasFocus = false;
        } else if (!this.hasFocus) {
            this.hasFocus = true;

        }
    }

    private String ansichtStatement(String stag, Ansicht ansicht) {
        String sstate = "";
        int behandler;
        String sletzter, serster, sbehandler;
        if (aktAnsicht == Ansicht.NORMAL) {
            if ("ADS".equals(new Datenbank().typ())) {
                sstate = "SELECT * FROM flexkc WHERE datum = '" + DatFunk.sDatInSQL(stag) + "'";
            } else {
                sstate = "SELECT * FROM flexkc WHERE datum = '" + DatFunk.sDatInSQL(stag) + "' LIMIT "
                        + KollegenListe.maxKalZeile;
            }
            macheStatement(sstate, aktAnsicht == Ansicht.NORMAL ? KollegenListe.maxKalZeile : 7);
            /* bislang aktiv */
            if (viewPanel.getParent() != null) {
                Reha.instance.terminpanel.eltern.setTitle(DatFunk.WochenTag(stag) + " " + stag + " -- KW: "
                        + DatFunk.KalenderWoche(stag) + " -- [Normalansicht]");
            }
            this.wocheAktuellerTag = "";
        } else if (aktAnsicht == Ansicht.WOCHE) {
            behandler = wochenbelegung;
            if (behandler == 0) {
                behandler = KollegenListe.vKKollegen.get(
                        KollegenListe.suchen((String) oCombo[0].getSelectedItem())).getReihe();
            }
            sbehandler = behandler < 10 ? "0" + behandler : "" + Integer.toString(behandler);
            serster = DatFunk.WocheErster(stag);
            this.wocheErster = serster;
            /* Nur zum Test */
            this.wocheBehandler = behandler;
            sletzter = DatFunk.WocheLetzter(stag);

            sstate = "SELECT * FROM flexkc WHERE datum >= '" + DatFunk.sDatInSQL(serster) + "'" + " AND datum <= '"
                    + DatFunk.sDatInSQL(sletzter) + "'" + " AND behandler = '" + sbehandler + "BEHANDLER'";
            macheStatement(sstate, aktAnsicht == Ansicht.NORMAL ? KollegenListe.maxKalZeile : 7);
            Reha.instance.terminpanel.eltern.setTitle(DatFunk.WochenTag(serster) + " " + serster + "  bis  "
                    + DatFunk.WochenTag(sletzter) + " " + sletzter + "-----Behandler:" + sbehandler + "-----KW:"
                    + DatFunk.KalenderWoche(serster) + " ----- [Wochenansicht]");
        }
        return sstate;
    }

    private void setDayForToolTip() {
        tooltip[0] = "<html>" + dayname[0] + "<br>" + DatFunk.sDatPlusTage(wocheErster, 0) + "</html>";
        tooltip[1] = "<html>" + dayname[1] + "<br>" + DatFunk.sDatPlusTage(wocheErster, 1) + "</html>";
        tooltip[2] = "<html>" + dayname[2] + "<br>" + DatFunk.sDatPlusTage(wocheErster, 2) + "</html>";
        tooltip[3] = "<html>" + dayname[3] + "<br>" + DatFunk.sDatPlusTage(wocheErster, 3) + "</html>";
        tooltip[4] = "<html>" + dayname[4] + "<br>" + DatFunk.sDatPlusTage(wocheErster, 4) + "</html>";
        tooltip[5] = "<html>" + dayname[5] + "<br>" + DatFunk.sDatPlusTage(wocheErster, 5) + "</html>";
        tooltip[6] = "<html>" + dayname[6] + "<br>" + DatFunk.sDatPlusTage(wocheErster, 6) + "</html>";
    }

    /***
     * Mache Statement.
     * @param behandlerMaxAnzahl TODO
     * @param ansicht            TODO
     */

    private void macheStatement(String sstmt, int behandlerMaxAnzahl) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = Reha.instance.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            try {
                rs = stmt.executeQuery(sstmt);
                int maxbehandler = behandlerMaxAnzahl;
                int maxblock = 0;
                int aktbehandler = 1;
                ArrayList<Object> aKalList = new ArrayList<Object>();
                aSpaltenDaten.clear();
                while ((rs.next()) && (aktbehandler <= maxbehandler)) {
                    Vector<String> v1 = new Vector<String>();
                    Vector<String> v2 = new Vector<String>();
                    Vector<String> v3 = new Vector<String>();
                    Vector<String> v4 = new Vector<String>();
                    Vector<String> v5 = new Vector<String>();
                    Vector<String> v6 = new Vector<String>();

                    /* in Spalte 301 steht die Anzahl der belegten Bl�cke */
                    int belegt = rs.getInt(SPALTE_ANZ_BELGEGTE_BLOECKE);
                    /* letzte zu durchsuchende Spalte festlegen */
                    int ende = (5 * belegt);
                    maxblock = maxblock + (ende + 5);


                        for (int i = 1; i < ende; i = i + 5) {
                            v1.addElement(rs.getString(i) != null ? rs.getString(i) : "");
                            v2.addElement(rs.getString(i + 1) != null ? rs.getString(i + 1) : "");
                            v3.addElement(rs.getString(i + 2));
                            v4.addElement(rs.getString(i + 3));
                            v5.addElement(rs.getString(i + 4));
                        }


                    v6.addElement(rs.getString(SPALTE_ANZ_BELGEGTE_BLOECKE)); // Anzahl
                    v6.addElement(rs.getString(302)); // Art
                    v6.addElement(rs.getString(303)); // Behandler
                    v6.addElement(rs.getString(304)); // MEMO
                    v6.addElement(rs.getString(305)); // Datum
                    v6.addElement(rs.getString(306)); // id

                    aKalList.add(v1);
                    aKalList.add(v2);
                    aKalList.add(v3);
                    aKalList.add(v4);
                    aKalList.add(v5);
                    aKalList.add(v6);

                    aSpaltenDaten.add(aKalList.clone());
                    aKalList.clear();
                    aktbehandler++;
                }
                aSpaltenDaten.add(aKalList.clone());
                // ge�ndert
                aKalList = null;
                if (maxblock > 0) {
                    datenZeichnen(aSpaltenDaten);
                }
            } catch (SQLException ex) {
                logger.error("einlesen der anzeigedaten", ex);

            }
        } catch (SQLException ex) {
            // System.out.println("Im Thread - Mache Statement");
            // System.out.println("von stmt -SQLState: " + ex.getSQLState());
            if ("08003".equals(ex.getSQLState())) {
                int nochmals = JOptionPane.showConfirmDialog(null,
                        "Die Datenbank konnte nicht gestartet werden, erneuter Versuch?", "Wichtige Benuterzinfo",
                        JOptionPane.YES_NO_OPTION);
                if (nochmals == JOptionPane.YES_OPTION) {
                    Reha.instance.ladenach();
                }
            } else {
                logger.error("einlesen der anzeigedaten", ex);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    rs = null;
                } catch (SQLException sqlEx) { // ignore }
                    rs = null;
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                    stmt = null;
                } catch (SQLException sqlEx) { // ignore }
                    stmt = null;
                }
            }
        }
    }

    private void maskenStatement(String sstmt) {
        Statement stmt = null;
        ResultSet rs = null;
        try {

            stmt = Reha.instance.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            try {
                rs = stmt.executeQuery(sstmt);
                int i = 0;
                int durchlauf = 0;
                int maxbehandler;
                if (aktAnsicht == Ansicht.NORMAL) {
                    maxbehandler = KollegenListe.vKKollegen.size();
                } else {
                    maxbehandler = 7;
                }
                int maxblock = 0;
                int aktbehandler = 1;
                ArrayList<Object> aKalList = new ArrayList<Object>();
                aSpaltenDaten.clear();
                while ((rs.next())) {
                    Vector<String> v1 = new Vector<String>();
                    Vector<String> v2 = new Vector<String>();
                    Vector<String> v3 = new Vector<String>();
                    Vector<String> v4 = new Vector<String>();
                    Vector<String> v5 = new Vector<String>();
                    Vector<String> v6 = new Vector<String>();

                    /* in Spalte 301 steht die Anzahl der belegten Bl�cke */
                    int belegt = rs.getInt(226);
                    /* letzte zu durchsuchende Spalte festlegen */
                    int ende = (5 * belegt);
                    maxblock = maxblock + (ende + 5);
                    durchlauf = 1;
                    int durchlauf1 = durchlauf;
                    int i1;
                    for (i1 = 1; i1 < ende; i1 = i1 + 5) {
                        v1.addElement(rs.getString(i1) != null ? rs.getString(i1) : "");
                        v2.addElement(rs.getString(i1 + 1) != null ? rs.getString(i1 + 1) : "");
                        v3.addElement(rs.getString(i1 + 2));
                        v4.addElement(rs.getString(i1 + 3));
                        v5.addElement(rs.getString(i1 + 4));
                        durchlauf1 = durchlauf1 + 1;

                    }
                    i = i1;

                    v6.addElement(rs.getString(226)); // Anzahl
                    v6.addElement(rs.getString(227)); // Art
                    v6.addElement(rs.getString(228)); // Behandler
                    v6.addElement(rs.getString(229)); // MEMO
                    v6.addElement(rs.getString(230)); // Datum

                    aKalList.add(v1.clone());
                    aKalList.add(v2.clone());
                    aKalList.add(v3.clone());
                    aKalList.add(v4.clone());
                    aKalList.add(v5.clone());
                    aKalList.add(v6.clone());
                    aSpaltenDaten.add(aKalList.clone());
                    aKalList.clear();
                    aktbehandler++;
                }

                if (maxblock > 0) {
                    datenZeichnen(aSpaltenDaten);
                    TerminFenster.rechneMaske();
                    if (aktAnsicht == Ansicht.MASKE) {
                        oSpalten[aktiveSpalte[2]].requestFocus(true);
                        oSpalten[aktiveSpalte[2]].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[0]);
                    }
                }
            } catch (SQLException ex) {

                System.out.println("von ResultSet SQLState: " + ex.getSQLState());
                System.out.println("von ResultSet ErrorCode: " + ex.getErrorCode());
                System.out.println("von ResultSet ErrorMessage: " + ex.getMessage());
            }

        } catch (SQLException ex) {
            System.out.println("von stmt -SQLState: " + ex.getSQLState());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    rs = null;
                } catch (SQLException sqlEx) { // ignore }
                    rs = null;
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                    stmt = null;
                } catch (SQLException sqlEx) { // ignore }
                    stmt = null;
                }
            }

        }
    }

    private void datenZeichnen(Vector<Object> vect) {
        vTerm = (Vector) vect.clone();
        vect = null;
        if (!vTerm.isEmpty()) {
            if (aktAnsicht == Ansicht.NORMAL) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        oSpalten[0].datenZeichnen(vTerm, belegung[0]);
                        oSpalten[1].datenZeichnen(vTerm, belegung[1]);
                        oSpalten[2].datenZeichnen(vTerm, belegung[2]);
                        oSpalten[3].datenZeichnen(vTerm, belegung[3]);
                        oSpalten[4].datenZeichnen(vTerm, belegung[4]);
                        oSpalten[5].datenZeichnen(vTerm, belegung[5]);
                        oSpalten[6].datenZeichnen(vTerm, belegung[6]);
                    }
                });
            } else if (aktAnsicht == Ansicht.WOCHE || aktAnsicht == Ansicht.MASKE) {
                new Thread(new KalZeichnen(oSpalten[0], vTerm, 0)).start();
                new Thread(new KalZeichnen(oSpalten[1], vTerm, 1)).start();
                new Thread(new KalZeichnen(oSpalten[2], vTerm, 2)).start();
                new Thread(new KalZeichnen(oSpalten[3], vTerm, 3)).start();
                new Thread(new KalZeichnen(oSpalten[4], vTerm, 4)).start();
                new Thread(new KalZeichnen(oSpalten[5], vTerm, 5)).start();
                new Thread(new KalZeichnen(oSpalten[6], vTerm, 6)).start();
            }
        }
    }

    @Override
    public void rehaTPEventOccurred(RehaTPEvent evt) {
        try {
            if (evt.getDetails()[0].equals(this.grundFlaeche.getParent()
                                                            .getName())) {
                if (evt.getDetails()[1] == "ROT") {
                    // String fname = evt.getDetails()[0];
                    xEvent.removeRehaTPEventListener(this);
                    if (TKSettings.UpdateIntervall > 0 || db_Aktualisieren != null) {
                        db_Aktualisieren.stop();
                    }
                    finalise();
                } else if (evt.getDetails()[2].equals("RequestFocus") || (evt.getDetails()[1] == "GRUEN")) {
                    oSpalten[aktiveSpalte[2]].requestFocus();
                } else if (evt.getRehaEvent()
                              .equals("ChangeLocation")) {
                    Integer.parseInt(evt.getDetails()[1]);
                }
            }
        } catch (NullPointerException ne) {
            // System.out.println(evt);
        }
    }

    private void setLockStatement(String sBehandler, String sDatum) {
        lockStatement = sBehandler + sDatum;
    }

    static String getLockStatement() {
        return lockStatement;
    }

    static void setLockSpalte(String spalte) {
    }

    static synchronized void setLockOk(int lock, String message) {
        Reha.instance.terminpanel.lockok = lock;
        Reha.instance.terminpanel.lockmessage = message;
    }

    public static TerminFenster getThisClass() {
        return Reha.instance.terminpanel;
    }

    private void sperreAnzeigen(String programmteil) {
        JOptionPane.showMessageDialog(null,
                "Diese Terminspalte ist derzeit gesperrt von Benutzer \n\n" + "---> " + lockmessage
                        + "\n\n und kann deshalb nicht veränder werden!\n\n"
                        + "Bitte informieren Sie den Administrator und notiern Sie zuvor -> " + programmteil);
    }

    static void starteUnlock() {
        new Thread(new UnlockRecord()).start();
    }

    /** Strg+Einfg. */
    public void setDatenVonExternInSpeicherNehmen(String[] daten) {
        datenSpeicher[0] = String.valueOf(daten[0]);
        datenSpeicher[1] = String.valueOf(daten[1]);
        datenSpeicher[3] = String.valueOf(daten[2]);
        Reha.instance.copyLabel.setText(datenSpeicher[0] + "°" + datenSpeicher[1] + "°" + datenSpeicher[3] + " Min.");
    }

    private void getDatenVonExternInSpeicherNehmen() {
        if (Reha.instance.bunker.getText()
                                .indexOf('°') >= 0) {
            try {
                String[] teilen = Reha.instance.bunker.getText()
                                                      .split("°");
                if (teilen.length <= 0) {
                    return;
                }
                teilen[3] = teilen[3].toUpperCase();
                teilen[3] = teilen[3].replaceAll(" MIN.", "");

                datenSpeicher[0] = teilen[1];
                datenSpeicher[1] = teilen[2];
                datenSpeicher[3] = teilen[3];
                Reha.instance.copyLabel.setText(
                        datenSpeicher[0] + "°" + datenSpeicher[1] + "°" + datenSpeicher[3] + " Min.");
                Reha.instance.shiftLabel.setText("bereit für F2= " + datenSpeicher[0] + "°" + datenSpeicher[1] + "°"
                        + datenSpeicher[3] + " Min.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Fehler bei der Vorbereitung des Datenspeichers");
            }
        }
    }

    private void datenInSpeicherNehmen() {
        int aktbehandler = -1;
        switch (this.aktAnsicht) {
        case NORMAL:
            aktbehandler = belegung[aktiveSpalte[2]];
            break;
        case WOCHE:
        case MASKE:
            aktbehandler = aktiveSpalte[2];
            break;
        }
        int aktblock = aktiveSpalte[0];
        if (aktbehandler == -1) {
            return;
        }
        datenSpeicher[0] = ((String) ((Vector) ((ArrayList) vTerm.get(aktbehandler)).get(0)).get(aktblock)).replaceAll(
                "\u00AE", "");
        datenSpeicher[1] = (String) ((Vector) ((ArrayList) vTerm.get(aktbehandler)).get(1)).get(aktblock);
        datenSpeicher[3] = (String) ((Vector) ((ArrayList) vTerm.get(aktbehandler)).get(3)).get(aktblock);

        Reha.instance.copyLabel.setText(datenSpeicher[0] + "°" + datenSpeicher[1] + "°" + datenSpeicher[3] + " Min.");
        Reha.instance.bunker.setText(
                "TERMDATEXT°" + datenSpeicher[0] + "°" + datenSpeicher[1] + "°" + datenSpeicher[3] + " Min.");
    }

    private String[] datenInDragSpeicherNehmen() {
        String[] srueck = { null, null, null, null, null };
        int aktbehandler = -1;
        switch (this.aktAnsicht) {
        case NORMAL:
            aktbehandler = belegung[aktiveSpalte[2]];
            break;
        case WOCHE:
            aktbehandler = aktiveSpalte[2];
            break;
        case MASKE:
            aktbehandler = aktiveSpalte[2];
            break;
        }
        int aktblock = aktiveSpalte[0];
        if (aktbehandler == -1) {
            return srueck;
        }
        try {
            srueck[0] = ((String) ((Vector) ((ArrayList) vTerm.get(aktbehandler)).get(0)).get(aktblock)).replaceAll(
                    "\u00AE", "");
            srueck[1] = (String) ((Vector) ((ArrayList) vTerm.get(aktbehandler)).get(1)).get(aktblock);
            srueck[3] = (String) ((Vector) ((ArrayList) vTerm.get(aktbehandler)).get(3)).get(aktblock);
            return srueck;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return new String[] { null, null, null, null, null };
        }
    }

    public void setDatenSpeicher(String[] speicher) {
        datenSpeicher[0] = speicher[0];
        datenSpeicher[1] = speicher[1];
        datenSpeicher[3] = speicher[3];
        Reha.instance.copyLabel.setText(datenSpeicher[0] + "°" + datenSpeicher[1] + "°" + datenSpeicher[3] + " Min.");
    }

    /** Shift+Einfg. */
    private void datenAusSpeicherHolen() {
        // System.out.println("Warten auf Ready = "+wartenAufReady);
        try {
            int aktbehandler = -1;
            int aktdauer;
            String aktstart;
            String aktend;
            String akttermdaten;
            setUpdateVerbot(true);//////

            gruppierenAktiv = false;
            gruppierenBloecke[0] = -1;
            gruppierenBloecke[1] = -1;
            oSpalten[gruppierenSpalte].setInGruppierung(false);

            if (datenSpeicher[0] == null) {
                //// System.out.println("datenSpeicher[0] hat den Wert null -> return");
                wartenAufReady = false;
                setUpdateVerbot(false);
                SqlInfo.loescheLocksMaschine();
                return;
            }
            // ****Hier rein die Abfrage ob die Druckerliste neu gestartet werden soll!
            int anzahl = terminVergabe.size();
            if (anzahl > 0) {
                if (!datenSpeicher[0].equals(terminVergabe.get(anzahl - 1)[8])) {
                    String confText = "Der Patient --> " + datenSpeicher[0]
                            + " <--ist jetzt NEU im internen Speicher.\n\n" + "BISLANG war der Patient --> "
                            + terminVergabe.get(anzahl - 1)[8] + " <-- im Speicher und damit in der Druckliste.\n"
                            + "Soll die bisherige Druckliste gelöscht werden und der Patient " + datenSpeicher[0]
                            + " übernommen werden?\n\n";
                    String meldungText = "Achtung!!! - wichtige Benutzeranfrage";
                    int abfrage = JOptionPane.showConfirmDialog(null, confText, meldungText, JOptionPane.YES_NO_OPTION);
                    if (abfrage == JOptionPane.YES_OPTION) {
                        terminVergabe.clear();
                    } else {
                        String[] internerSpeicher = { terminVergabe.get(anzahl - 1)[8],
                                terminVergabe.get(anzahl - 1)[9], "", terminVergabe.get(anzahl - 1)[4] };
                        setDatenSpeicher(internerSpeicher.clone());
                    }
                }
            }

            switch (this.aktAnsicht) {
            case NORMAL:
                aktbehandler = belegung[aktiveSpalte[2]];
                break;
            case WOCHE:
                aktbehandler = aktiveSpalte[2];
                break;
            case MASKE:
                aktbehandler = aktiveSpalte[2];
                break;
            }
            int aktblock = aktiveSpalte[0];
            aktdauer = Integer.parseInt(
                    (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(aktbehandler)).get(3)).get(aktblock));
            aktstart = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(aktbehandler)).get(2)).get(aktblock);
            aktend = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(aktbehandler)).get(4)).get(aktblock);
            akttermdaten = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(aktbehandler)).get(0)).get(aktblock);
            /* Test der Berechtigungen */
            if (!rechteTest(akttermdaten)) {
                wartenAufReady = false;
                setUpdateVerbot(false);
                SqlInfo.loescheLocksMaschine();
                return;
            }
            for (int i = 0; i < 1; i++) {
                if (aktdauer == Integer.parseInt(datenSpeicher[3])) {
                    datenSpeicher[2] = aktstart;
                    datenSpeicher[4] = aktend;
                    blockSetzen(1);
                    break;
                }
                if (aktdauer < Integer.parseInt(datenSpeicher[3])) {
                    dialogRetInt = 0;
                    Point p = null; // positionErmitteln();
                    if (!terminGedropt) {
                        p = positionErmitteln();
                    } else {
                        p = MouseInfo.getPointerInfo()
                                     .getLocation();
                        p.y = p.y + 4;
                    }
                    new TerminEinpassen(p.x, p.y);
                    switch (dialogRetInt) {
                    case 0:
                        terminBreak = true;
                        wartenAufReady = false;
                        break;
                    case 1:
                        datenSpeicher[2] = aktstart;
                        datenSpeicher[4] = aktend;
                        blockSetzen(4);
                        break;
                    case 2:
                        // in Nachfolgeblock kürzen
                        datenSpeicher[2] = aktstart;
                        datenSpeicher[4] = aktend;
                        int ende1, ende2;
                        int aktanzahl = ((Vector<?>) ((ArrayList<?>) vTerm.get(aktbehandler)).get(0)).size() - 1;
                        if (aktanzahl == aktblock) {
                            // ende1 =
                        } else { // pr�fen ob nachfolgender Block gekürzt werden kann oder ob zu klein
                            ende1 = (int) ZeitFunk.MinutenSeitMitternacht(
                                    (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(aktbehandler)).get(4)).get(
                                            aktblock + 1));
                            ende2 = (int) ZeitFunk.MinutenSeitMitternacht(aktstart)
                                    + Integer.parseInt(datenSpeicher[3]);

                            if (ende2 >= ende1) {
                                JOptionPane.showMessageDialog(null,
                                        "Der nachfolgende Block ist von kürzerer Dauer\n"
                                                + "als er für die von Ihnen gewünschte Operation sein müßte\n\n"
                                                + "Kopiert wird daher --> nix!");
                                wartenAufReady = false;

                            } else {
                                blockSetzen(6);
                            }
                        }
                        break;
                    }
                    break;
                }
                if (aktdauer > Integer.parseInt(datenSpeicher[3])) {
                    dialogRetInt = 0;

                    Point p = null;
                    if (!terminGedropt) {
                        p = positionErmitteln();
                    } else {
                        p = MouseInfo.getPointerInfo()
                                     .getLocation();
                        p.y = p.y + 4;
                    }
                    new TerminObenUntenAnschliessen(p.x, p.y);
                    // System.out.println("DialogretInt = "+dialogRetInt);

                    switch (dialogRetInt) {
                    case 0:
                        terminBreak = true;
                        wartenAufReady = false;
                        break;
                    case 1:
                        // Block oben anschliessen
                        datenSpeicher[2] = aktstart;
                        datenSpeicher[4] = aktend;
                        blockSetzen(2);
                        // o.k. blockObenAnschliessen();
                        break;

                    case 2:
                        // Block unten anschliessen
                        datenSpeicher[2] = aktstart;
                        datenSpeicher[4] = aktend;
                        blockSetzen(3);
                        break;
                    // blockUntenAnschliessen();

                    case 3:
                        // Block einpassen
                        datenSpeicher[2] = aktstart;
                        datenSpeicher[4] = aktend;
                        blockSetzen(4);
                        // blockAusdehnen();
                        break;

                    case 4:
                        int zeit1, zeit2, zeit3, zeit4;
                        datenSpeicher[2] = dialogRetData[0] + ":" + dialogRetData[1] + ":00";
                        zeit1 = (int) ZeitFunk.MinutenSeitMitternacht(datenSpeicher[2])
                                + Integer.parseInt(datenSpeicher[3]);
                        zeit2 = (int) ZeitFunk.MinutenSeitMitternacht(aktend);
                        zeit3 = (int) ZeitFunk.MinutenSeitMitternacht(datenSpeicher[2]);
                        zeit4 = (int) ZeitFunk.MinutenSeitMitternacht(aktstart);
                        if ((zeit1 < zeit2) && (zeit3 > zeit4)) {
                            //// System.out.println("case 4: blockSetzen(5) zeiten:"+zeit1+" / "+zeit2+" /
                            //// "+zeit3+" / "+zeit4);
                            blockSetzen(5);
                            break;
                        } else if ((zeit1 == zeit2) && (zeit3 > zeit4)) {
                            blockSetzen(3);
                            //// System.out.println("case 4: mu� unten andocken zeiten:"+zeit1+" / "+zeit2+"
                            //// / "+zeit3+" / "+zeit4);
                            break;
                        } else if ((zeit1 < zeit2) && (zeit3 == zeit4)) {
                            blockSetzen(2);
                            //// System.out.println("case 4: mu� oben andocken zeiten:"+zeit1+" / "+zeit2+"
                            //// / "+zeit3+" / "+zeit4);
                            break;
                        } else {
                            //// System.out.println("case 4: pa�t nicht zeiten:"+zeit1+" / "+zeit2+" /
                            //// "+zeit3+" / "+zeit4);
                            JOptionPane.showMessageDialog(null,
                                    "Die von Ihnen angegebene Startzeit " + datenSpeicher[2] + "\n"
                                            + " und die Dauer des Termines von " + datenSpeicher[3]
                                            + " Minuten, passt hinten und\n"
                                            + "vorne nicht. Entweder ergibt dies Startzeit eine Überschneidung mit \n"
                                            + "dem vorherigen oder mit dem nachfolgenden Termin\n\n"
                                            + "Kopiert wird daher --> nix!");
                            wartenAufReady = false;
                        }
                        break;

                    }
                    break;
                }
            }
            SqlInfo.loescheLocksMaschine();
            setUpdateVerbot(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            SqlInfo.loescheLocksMaschine();
            setUpdateVerbot(false);
            wartenAufReady = false;
        }
    }

    public int[] getAktiverBlock() {
        return aktiveSpalte;
    }

    public static void setDialogRet(int iret, String[] sret) {
        Reha.instance.terminpanel.dialogRetInt = iret;
        Reha.instance.terminpanel.dialogRetData[0] = sret[0];
        Reha.instance.terminpanel.dialogRetData[1] = sret[1];
    }

    /**
     * Nachfolgend das Blockhandling Übergabe = 1 Block passt genaut Übergabe = 2
     * Block oben anschließen Übergabe = 3 Block unten anschließen Übergabe = 4
     * Block ausdehnen Übergabe = 5 Startzeit wurde manuell festgelegt Übergabe = 6
     * Nachfolgenden Block kürzen Übergabe = 7 Vorblock kürzen Übergabe = 8
     * Gruppierten Terminblock zusammenfassen Übergabe = 9 Gruppierten Terminblock
     * löschen Übergabe = 10 Freitermin eintragen Übergabe = 11 Block löschen
     * Übergabe = 12 Block tauschen mit vorgänger (nach oben) Übergabe = 13 Block
     * tauschen mit nachfolger (nach unten).
     */
    private void blockSetzen(int wohin) {
        // System.out.println("Block-Handling: "+wohin);
        int gesperrt;
        gesperrt = lockVorbereiten();
        setzeRueckgabe();

        if (gesperrt < 0) {
            sperreAnzeigen("in blockSetzen - Parameter=" + wohin);
            SqlInfo.loescheLocksMaschine();
            return;
        } else {
            setUpdateVerbot(true);
            int rueck = -1;
            if (this.aktAnsicht == Ansicht.NORMAL) {
                spaltenDatumSetzen(true);
                BlockHandling bhd = new BlockHandling(wohin, vTerm, belegung[aktiveSpalte[2]], aktiveSpalte[2],
                        aktiveSpalte[0], spaltenDatum, 0, datenSpeicher);
                rueck = bhd.init();
            } else if (this.aktAnsicht == Ansicht.WOCHE) {
                spaltenDatumSetzen(false);
                BlockHandling bhd = new BlockHandling(wohin, vTerm, aktiveSpalte[2], aktiveSpalte[2], aktiveSpalte[0],
                        spaltenDatum, this.wocheBehandler, datenSpeicher);
                rueck = bhd.init();
            } else if (this.aktAnsicht == Ansicht.MASKE) {
                spaltenDatumSetzen(true);
                BlockHandling bhd = new BlockHandling(wohin, vTerm, aktiveSpalte[2], aktiveSpalte[2], aktiveSpalte[0],
                        spaltenDatum, maskenbelegung, datenSpeicher);
                rueck = bhd.init();
            }
            if (rueck >= 0) {
                if (this.aktAnsicht == Ansicht.NORMAL) {
                    // in Datenzeichnen
                    if (wohin == 8 || wohin == 9) { // Block komplett zusammenfassen
                        aktiveSpalte[0] = Math.min(gruppierenClipBoard[0], gruppierenClipBoard[1]);
                    }
                    int anzahl = ((Vector<?>) ((ArrayList<?>) vTerm.get(belegung[aktiveSpalte[2]])).get(0)).size();
                    if (aktiveSpalte[0] >= anzahl) {
                        aktiveSpalte[0] = anzahl - 1;
                    }
                    oSpalten[aktiveSpalte[2]].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[0]);
                    oSpalten[aktiveSpalte[2]].datenZeichnen(vTerm, belegung[aktiveSpalte[2]]);
                    oSpalten[aktiveSpalte[2]].repaint();
                } else if ((this.aktAnsicht == Ansicht.WOCHE) || (this.aktAnsicht == Ansicht.MASKE)) {
                    if (wohin == 8 || wohin == 9) { // Block komplett zusammenfassen
                        aktiveSpalte[0] = Math.min(gruppierenClipBoard[0], gruppierenClipBoard[1]);
                    }
                    int anzahl = ((Vector<?>) ((ArrayList<?>) vTerm.get(aktiveSpalte[2])).get(0)).size();
                    if (aktiveSpalte[0] >= anzahl) {
                        aktiveSpalte[0] = anzahl - 1;
                    }
                    oSpalten[aktiveSpalte[2]].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[0]);
                    oSpalten[aktiveSpalte[2]].datenZeichnen(vTerm, aktiveSpalte[2]);
                    oSpalten[aktiveSpalte[2]].repaint();
                }
            } else {
                starteUnlock();
                wartenAufReady = false;
                setUpdateVerbot(false);
            }
        }
        SqlInfo.loescheLocksMaschine();
        wartenAufReady = false;
        setUpdateVerbot(false);
    }

    public void setUpdateVector(Vector vTerm) {
        oSpalten[aktiveSpalte[2]].datenZeichnen(vTerm, aktiveSpalte[2]);
    }

    public Vector getDatenVector() {
        return vTerm;
    }

    static void rechneMaske() {
        String titel = "";
        double stunden = 0.00;
        String[] wochensicht = { "Mo=", "Di=", "Mi=", "Do=", "Fr=", "Sa=", "So=" };
        int anzahl;
        int minuten_tag = 0;
        double stunden_woche = 0.00;
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        for (int tage = 0; tage < 7; tage++) {
            anzahl = ((Vector<?>) ((ArrayList<?>) Reha.instance.terminpanel.vTerm.get(tage)).get(0)).size();
            minuten_tag = 0;
            for (int i = 0; i < anzahl; i++) {
                if (!((String) ((Vector<?>) ((ArrayList<?>) Reha.instance.terminpanel.vTerm.get(tage)).get(1)).get(
                        i)).trim()
                           .contains("@FREI")) {
                    // minuten_tag = minuten_tag + Integer.valueOf( ((String)
                    // ((Vector<?>)((ArrayList<?>)Reha.instance.terminpanel.vTerm.get(tage)).get(3)).get(i)).trim());
                    minuten_tag = minuten_tag + Integer.parseInt(
                            ((String) ((Vector<?>) ((ArrayList<?>) Reha.instance.terminpanel.vTerm.get(tage)).get(
                                    3)).get(i)).trim());
                }
            }
            stunden = 0.00 + minuten_tag;
            stunden_woche = stunden_woche + stunden;
            titel = titel + wochensicht[tage] + df.format(stunden / 60) + " - ";
        }
        titel = titel + "Wochenstunden: " + df.format(stunden_woche / 60);
        Reha.instance.terminpanel.eltern.setTitle(titel);
    }

    private int lockVorbereiten() {
        lockok = 0;
        if (aktAnsicht == Ansicht.NORMAL) {
            setLockStatement(
                    belegung[aktiveSpalte[2]] + 1 >= 10 ? Integer.toString(belegung[aktiveSpalte[2]] + 1) + "BEHANDLER"
                            : "0" + (belegung[aktiveSpalte[2]] + 1) + "BEHANDLER",
                    aktuellerTag);
        } else if (aktAnsicht == Ansicht.WOCHE) {
            if (aktiveSpalte[2] == 0) {
                setLockStatement(wochenbelegung >= 10 ? Integer.toString(wochenbelegung) + "BEHANDLER"
                        : "0" + (wochenbelegung) + "BEHANDLER", getWocheErster());
            } else {
                setLockStatement(
                        wochenbelegung >= 10 ? Integer.toString(wochenbelegung) + "BEHANDLER"
                                : "0" + (wochenbelegung) + "BEHANDLER",
                        DatFunk.sDatPlusTage(getWocheErster(), aktiveSpalte[2]));
            }
        }

        new Thread(new LockRecord()).start();
        long zeit = System.currentTimeMillis();
        while (lockok == 0) {
            try {
                Thread.sleep(20);
                if (System.currentTimeMillis() - zeit > 2500) {
                    JOptionPane.showMessageDialog(null,
                            "Fehler im Lock-Mechanismus -> Funktion LockVorbereiten(), bitte informieren Sie den Entwickler");
                    SqlInfo.loescheLocksMaschine();
                    lockok = -1;
                }
            } catch (InterruptedException e1) {
                JOptionPane.showMessageDialog(null, "Fehler im Modul lockVorbereiten");
                e1.printStackTrace();
                SqlInfo.loescheLocksMaschine();
            }
        }
        return lockok;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        for (int i = 0; i < 1; i++) { // ich waer so gern ein switch
            if (((AbstractButton) arg0.getSource()).getText() == "Gruppierung zusammenfassen") {
                blockSetzen(8);
                oSpalten[aktiveSpalte[2]].repaint();
                break;
            }
            if (((AbstractButton) arg0.getSource()).getText() == "Terminliste aufrufen") {
                terminListe();
                break;
            }
            if (((AbstractButton) arg0.getSource()).getText() == "Schnellsuche (Heute + 4 Tage)") {
                schnellSuche(connection);
                break;
            }
            if (((AbstractButton) arg0.getSource()).getText() == "einen Tag vorwärts blättern"
                    || ((AbstractButton) arg0.getSource()).getText() == "eine Woche vorwärts blättern") {
                setUpdateVerbot(true);
                tagBlaettern(1);
                setUpdateVerbot(false);
                oSpalten[aktiveSpalte[2]].requestFocus();
                break;
            }

            if (((AbstractButton) arg0.getSource()).getText() == "einen Tag rückwärts blättern"
                    || ((AbstractButton) arg0.getSource()).getText() == "eine Woche rückwärts blättern") {
                setUpdateVerbot(true);
                tagBlaettern(-1);
                setUpdateVerbot(false);
                oSpalten[aktiveSpalte[2]].requestFocus();
                break;
            }
            if (((AbstractButton) arg0.getSource()).getText() == "Behandler-Set aufrufen") {
                setUpdateVerbot(true);
                setAufruf(MouseInfo.getPointerInfo()
                                   .getLocation());
                setUpdateVerbot(false);
                oSpalten[aktiveSpalte[2]].requestFocus();
                break;
            }
            if (((AbstractButton) arg0.getSource()).getText() == "Datums-Dialog aufrufen") {
                setUpdateVerbot(true);
                tagSprung(this.aktuellerTag, 0);
                setUpdateVerbot(false);
                oSpalten[aktiveSpalte[2]].requestFocus();
                break;
            }
            if (((AbstractButton) arg0.getSource()).getText() == "Termin mit Vorgängertermin tauschen") {
                setUpdateVerbot(true);
                tauscheTermin(-1);
                setUpdateVerbot(false);
                oSpalten[aktiveSpalte[2]].requestFocus();
                break;
            }
            if (((AbstractButton) arg0.getSource()).getText() == "Termin mit Nachfolgetermin tauschen") {
                setUpdateVerbot(true);
                tauscheTermin(1);
                setUpdateVerbot(false);
                oSpalten[aktiveSpalte[2]].requestFocus();
                break;
            }
            if (((AbstractButton) arg0.getSource()).getText() == "Arbeitszeitdefinition in Terminkalender übertragen") {
                if (maskenbelegung < 1) {
                    JOptionPane.showMessageDialog(null,
                            "Um die AZ-Definition in den Terminkalender zu übertragen empfiehlt es sich erst auszuwählen\nwelche(!) Definition übertragen werden soll....");
                    return;
                }
                if (!Rechte.hatRecht(Rechte.Masken_uebertragen, true)) {
                    return;
                }
                setUpdateVerbot(true);
                mb = new MaskeInKalenderSchreiben(Reha.getThisFrame(), maskenbelegung, (Vector) vTerm.clone());
                mb.setSize(new Dimension(700, 430));
                mb.setLocation(new Point(250, 200));
                mb.setVisible(true);
                mb.setModal(true);
                setUpdateVerbot(false);
                oSpalten[aktiveSpalte[2]].requestFocus();
                break;
            }
            if (((AbstractButton) arg0.getSource()).getText() == "Patient suchen (über Rezept-Nummer)") {
                doPatSuchen(connection);
            }
            if (((AbstractButton) arg0.getSource()).getText() == "Telefonliste aller Patienten (über Rezept-Nummer)") {
                doTelefonListe();
            }
            if (((AbstractButton) arg0.getSource()).getText() == "kopieren") {
                int xaktBehandler = -1;
                datenInSpeicherNehmen();
                if (!terminVergabe.isEmpty()) {
                    terminVergabe.clear();
                }
                if (this.aktAnsicht == Ansicht.NORMAL) {
                    xaktBehandler = belegung[aktiveSpalte[2]];
                } else if ((this.aktAnsicht == Ansicht.WOCHE) || (this.aktAnsicht == Ansicht.MASKE)) {
                    xaktBehandler = aktiveSpalte[2];
                }
                terminAufnehmen(xaktBehandler, aktiveSpalte[0]);
                break;
            }
            if (((AbstractButton) arg0.getSource()).getText() == "ausschneiden") {
                if (!Rechte.hatRecht(Rechte.Kalender_termindelete, true)) {
                    wartenAufReady = false;
                    gruppierenAktiv = false;
                    break;
                }
                long zeit = System.currentTimeMillis();
                boolean grobRaus = false;
                while (wartenAufReady) {
                    try {
                        Thread.sleep(20);
                        if (System.currentTimeMillis() - zeit > 1500) {
                            grobRaus = true;
                            break;
                        }
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                if (!grobRaus) {
                    wartenAufReady = true;
                    testeObAusmustern();
                    blockSetzen(11);
                } else {
                    SqlInfo.loescheLocksMaschine();
                    wartenAufReady = false;
                }
                break;
            }
            if (((AbstractButton) arg0.getSource()).getText() == "einfügen") {
                long zeit = System.currentTimeMillis();
                boolean grobRaus = false;
                while (wartenAufReady) {
                    try {
                        Thread.sleep(20);
                        if (System.currentTimeMillis() - zeit > 1500) {
                            grobRaus = true;
                            break;
                        }
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                if (!grobRaus) {
                    wartenAufReady = true;
                    terminGedropt = false;
                    terminBreak = false;
                    datenAusSpeicherHolen();
                } else {
                    wartenAufReady = false;
                    SqlInfo.loescheLocksMaschine();
                }

                gruppierenAktiv = false;
                gruppierenBloecke[0] = -1;
                gruppierenBloecke[1] = -1;
                oSpalten[gruppierenSpalte].setInGruppierung(false);
                break;
            }
            if (((AbstractButton) arg0.getSource()).getText() == "bestätigen") {
                terminBestaetigen(1, false /*
                                            * TODO kennt tspalte nicht (ersatzweise 1); tspalte wird auch garnicht
                                            * gelesen ?!
                                            */);
                gruppeAusschalten();
                break;
            }
        }
    }

    private void doTelefonListe() {
        int xaktBehandler = 0;
        if (aktiveSpalte[0] < 0) {
            return;
        }
        if (this.aktAnsicht == Ansicht.NORMAL) {
            xaktBehandler = belegung[aktiveSpalte[2]];
        } else if (this.aktAnsicht == Ansicht.WOCHE) {
            xaktBehandler = aktiveSpalte[2];
        } else if (this.aktAnsicht == Ansicht.MASKE) {
            JOptionPane.showMessageDialog(null, "Patientenzuordnung in Definition der Wochenarbeitszeit nicht möglich");
            return;
        }
        if (xaktBehandler < 0) {
            return;
        }
        final int fxaktBehandler = xaktBehandler;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                new TelefonListe(vTerm.get(fxaktBehandler));
                return null;
            }
        }.execute();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                RehaSplash rspl = new RehaSplash(null,
                        "Telefonliste starten -  dieser Vorgang kann einige Sekunden dauern...");
                long zeit = System.currentTimeMillis();
                do {
                    Thread.sleep(20);
                    if (System.currentTimeMillis() - zeit > 3000) {
                        break;
                    }
                } while (true);
                rspl.dispose();
                return null;
            }
        }.execute();
    }

    private void doPatSuchen(Connection connection) {
        String pat_int;
        int xaktBehandler = 0;
        boolean inhistorie = false;
        if (aktiveSpalte[0] < 0) {
            return;
        }
        switch (aktAnsicht) {
        case NORMAL:
            xaktBehandler = belegung[aktiveSpalte[2]];
            break;
        case WOCHE:
            xaktBehandler = aktiveSpalte[2];
            break;
        case MASKE:
            JOptionPane.showMessageDialog(null, "Patientenzuordnung in Definition der Wochenarbeitszeit nicht möglich");
            return;
        }
        if (xaktBehandler < 0) {
            return;
        }
        String reznr = ((ArrayList<Vector<String>>) vTerm.get(xaktBehandler)).get(1)
                                                                             .get(aktiveSpalte[0]);
        int ind = reznr.indexOf('\\');
        if (ind >= 0) {
            reznr = reznr.substring(0, ind);
        }
        Vector vec = SqlInfo.holeSatz("verordn", "pat_intern", "rez_nr='" + reznr + "'", new ArrayList());
        if (vec.isEmpty()) {
            vec = SqlInfo.holeSatz("lza", "pat_intern", "rez_nr='" + reznr + "'", new ArrayList());
            if (!vec.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Achtung das Rezept ist bereits abgerechnet und befindet sich in der Historie");
                inhistorie = true;
            }
        }
        if (vec.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Rezept nicht gefunden!\nIst die eingetragene Rezeptnummer korrekt?");
            return;
        }

        vec = SqlInfo.holeSatz("pat5", "pat_intern", "pat_intern='" + vec.get(0) + "'", new ArrayList());
        if (vec.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Patient mit zugeordneter Rezeptnummer -> " + reznr + " <- wurde nicht gefunden");
            return;
        }
        pat_int = (String) vec.get(0);
        JComponent patient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
        final String xreznr = reznr;
        final boolean xinhistorie = inhistorie;
        if (patient == null) {
            final String xpat_int = pat_int;
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    JComponent xpatient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
                    Reha.instance.progLoader.ProgPatientenVerwaltung(1, connection);
                    long whilezeit = System.currentTimeMillis();
                    while (xpatient == null) {
                        Thread.sleep(20);
                        xpatient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
                        if (System.currentTimeMillis() - whilezeit > 2500) {
                            break;
                        }
                    }
                    whilezeit = System.currentTimeMillis();
                    while (!RezepteAktuell.initOk) {
                        Thread.sleep(20);
                        if (System.currentTimeMillis() - whilezeit > 2500) {
                            break;
                        }
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
            Reha.instance.progLoader.ProgPatientenVerwaltung(1, connection);
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

    private void tauscheTermin(int richtung) {
        if (!Rechte.hatRecht(Rechte.Kalender_terminanlegenvoll, true)) {
            return;
        }
        int behandler = -1, block = -1, blockmax;
        int behandler1 = behandler;
        switch (aktAnsicht) {
        case NORMAL:
            behandler1 = belegung[aktiveSpalte[2]];
            break;
        case WOCHE:
        case MASKE:
            behandler1 = aktiveSpalte[2];
            break;
        }
        behandler = behandler1;
        block = aktiveSpalte[0];
        blockmax = ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(0)).size();

        for (int i = 0; i < 1; i++) {
            if (richtung < 0) {
                // mit Vorgängertermin tauschen;
                if (block == 0) {
                    JOptionPane.showMessageDialog(null,
                            "Sie sind bereits auf dem ersten Termin und dieser hat in der Regel keinen Vorgänger....");
                    return;
                } else {
                    blockSetzen(12);
                    aktiveSpalte[0] = aktiveSpalte[0] - 1;
                    neuerBlockAktiv(aktiveSpalte[0]);
                    oSpalten[aktiveSpalte[2]].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[0]);
                    oSpalten[aktiveSpalte[2]].repaint();
                }
            } else // mit Nachfolgetermin tauschen
            if (block == (blockmax - 1)) {
                JOptionPane.showMessageDialog(null,
                        "Sie sind bereits auf dem letzten Termin und dieser hat in der Regel keinen Nachfolger....");
                return;
            } else {
                blockSetzen(13);
                aktiveSpalte[0] = aktiveSpalte[0] + 1;
                neuerBlockAktiv(aktiveSpalte[0]);
                oSpalten[aktiveSpalte[2]].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[0]);

                oSpalten[aktiveSpalte[2]].repaint();
            }
        }
    }

    public void springeAufDatum(String datum) {
        // tagBlaettern((int)DatFunk.TageDifferenz(this.aktuellerTag,datum));
        datGewaehlt = datum;
        if (aktAnsicht == Ansicht.WOCHE) {
            this.wocheAktuellerTag = DatFunk.WocheErster(datum);
            this.wocheErster = this.wocheAktuellerTag;
            setDayForToolTip();
            try {
                showDaysInWeekView(aktAnsicht);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        suchSchonMal();
        // tagSprung(datum,(int)DatFunk.TageDifferenz(this.aktuellerTag,datum));
    }

    private void tagSprung(String sprungdatum, int sprung) {
        datGewaehlt = null;

        if (aktAnsicht == Ansicht.NORMAL) {
            intagWahl = true;
            final String datwahl = sprung != 0 ? DatFunk.sDatPlusTage(this.aktuellerTag, sprung) : this.aktuellerTag;
            TagWahlNeu tagWahlNeu = new TagWahlNeu(Reha.getThisFrame(), null, datwahl);
            tagWahlNeu.setPreferredSize(new Dimension(240, 170));
            tagWahlNeu.getSmartTitledPanel()
                      .setPreferredSize(new Dimension(240, 170));
            tagWahlNeu.pack();
            tagWahlNeu.setLocationRelativeTo(viewPanel);
            tagWahlNeu.setVisible(true);
            tagWahlNeu.dispose();
            intagWahl = false;
            dragLab[aktiveSpalte[2]].setIcon(null);
            dragLab[aktiveSpalte[2]].setText("");
            tagWahlNeu = null;
        } else if (aktAnsicht == Ansicht.WOCHE) {
            if (this.wocheAktuellerTag.isEmpty()) {
                this.wocheAktuellerTag = this.aktuellerTag;
            }
            this.wocheAktuellerTag = DatFunk.sDatPlusTage(this.wocheAktuellerTag, 7 * sprung);
            this.wocheErster = DatFunk.WocheErster(this.wocheAktuellerTag);
            dragLab[aktiveSpalte[2]].setIcon(null);
            dragLab[aktiveSpalte[2]].setText("");
            ansichtStatement(this.wocheAktuellerTag, aktAnsicht);
            setDayForToolTip();
            try {
                showDaysInWeekView(aktAnsicht);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        SetzeLabel();
    }

    void suchSchonMal() {
        if (datGewaehlt != null && !datGewaehlt.equals(this.aktuellerTag)) {
            this.aktuellerTag = datGewaehlt;
            ansichtStatement(this.aktuellerTag, aktAnsicht);
        }
    }

    private void tagBlaettern(int richtung) {
        if (aktAnsicht == Ansicht.NORMAL)/* Normalansicht */ {
            this.aktuellerTag = DatFunk.sDatPlusTage(this.aktuellerTag, +richtung);
            ansichtStatement(this.aktuellerTag, aktAnsicht);
            this.oSpalten[0].requestFocus();
        } else if (aktAnsicht == Ansicht.WOCHE) {
            if (this.wocheAktuellerTag.isEmpty()) {
                this.aktuellerTag = DatFunk.sDatPlusTage(this.aktuellerTag, +richtung);
                this.wocheAktuellerTag = this.aktuellerTag;
            }
            this.wocheAktuellerTag = DatFunk.sDatPlusTage(this.wocheAktuellerTag, +(richtung * 7));
            this.wocheErster = DatFunk.WocheErster(this.wocheAktuellerTag);
            ansichtStatement(this.wocheAktuellerTag, aktAnsicht);
            setDayForToolTip();
            try {
                showDaysInWeekView(aktAnsicht);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void testeObAusmustern() {
        if (aktAnsicht == Ansicht.MASKE) {
            return;
        }

        int xaktBehandler = 0;
        String xBehandler = "";
        if (aktAnsicht == Ansicht.NORMAL) {
            xaktBehandler = Integer.parseInt(Integer.toString(belegung[aktiveSpalte[2]]));
            xBehandler = KollegenListe.getKollegenUeberReihe(xaktBehandler + 1);
        } else if (aktAnsicht == Ansicht.WOCHE) {
            xaktBehandler = Integer.parseInt(Integer.toString(aktiveSpalte[2]));
            xBehandler = KollegenListe.getKollegenUeberReihe(wocheBehandler);
        }
        int xblock = Integer.parseInt(Integer.toString(aktiveSpalte[0]));
        String nametext = ((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(0)).get(
                xblock)).replaceAll("\u00AE", "");
        String reztext = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(1)).get(xblock);
        String starttext = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(2)).get(xblock);
        String sdauer = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(3)).get(xblock);
        String stestdat = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(5)).get(4);
        terminAusmustern(stestdat + starttext, sdauer, xBehandler, nametext, reztext);
    }

    void terminAusmustern(String tagundstart, String dauer, String behandler, String name, String reznum) {
        for (int y = 0; y < terminVergabe.size(); y++) {
            if (terminVergabe.get(y)[3].trim()
                                       .equals(tagundstart.trim())
                    && terminVergabe.get(y)[4].trim()
                                              .equals(dauer.trim())
                    && terminVergabe.get(y)[5].trim()
                                              .equals(behandler.trim())
                    && terminVergabe.get(y)[8].trim()
                                              .equals(name.trim())
                    && terminVergabe.get(y)[9].trim()
                                              .equals(reznum.trim())) {
                terminVergabe.remove(y);
                break;
            }
        }
        if (!terminVergabe.isEmpty()) {
            Reha.instance.mousePositionLabel.setForeground(Color.RED);
            Reha.instance.mousePositionLabel.setText(
                    terminVergabe.size() + " * " + terminVergabe.get(0)[8] + " in Liste");
        } else {
            Reha.instance.mousePositionLabel.setForeground(Color.BLACK);
            Reha.instance.mousePositionLabel.setText("Druckliste = leer");
        }
    }

    void terminAufnehmen(int behandler, int block) {
        String[] sTerminVergabe = { null, null, null, null, null, null, null, null, null, null, null };
        String nametext = "";
        String reztext = "";
        String sdauer = "";
        int xaktBehandler = behandler;
        if (aktAnsicht == Ansicht.MASKE) {
            return;
        }
        if (terminVergabe.size() > 0) {
            int anzahl = terminVergabe.size();
            boolean gleiche = false;
            nametext = ((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(0)).get(block)).replaceAll(
                    "\u00AE", "");
            reztext = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(1)).get(block);
            String starttext = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(2)).get(block);
            sdauer = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(3)).get(block);
            String stestdat = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(5)).get(4);

            for (int i = 0; i < anzahl; i++) {
                if (terminVergabe.get(i)[8].equals(nametext) && terminVergabe.get(i)[9].equals(reztext)
                        && terminVergabe.get(i)[2].equals(starttext)
                        && terminVergabe.get(i)[3].equals(stestdat + starttext)) {
                    gleiche = true;
                    break;
                }
            }
            if (gleiche) {
                try {
                    Reha.instance.shiftLabel.setText(
                            "bereit für F2= " + nametext + "°" + reztext + "°" + sdauer + " Min.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Reha.instance.shiftLabel.setText("");
                }
                return;
            }
        }
        try {
            sTerminVergabe[8] = ((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(0)).get(
                    block)).replaceAll("\u00AE", "");
            sTerminVergabe[9] = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(1)).get(block);
            sTerminVergabe[2] = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(2)).get(block);
            sTerminVergabe[4] = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(3)).get(block);
            sTerminVergabe[3] = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(5)).get(4);

            if (aktAnsicht == Ansicht.NORMAL) {
                sTerminVergabe[5] = KollegenListe.getKollegenUeberReihe(xaktBehandler + 1);
                sTerminVergabe[6] = Integer.toString(behandler + 1);
            } else if (aktAnsicht == Ansicht.WOCHE) {
                sTerminVergabe[5] = KollegenListe.getKollegenUeberReihe(wocheBehandler);
                sTerminVergabe[6] = Integer.toString(wocheBehandler);
            }
            sTerminVergabe[10] = Integer.toString(behandler);
            sTerminVergabe[7] = Integer.toString(block);
            sTerminVergabe[1] = DatFunk.sDatInDeutsch(sTerminVergabe[3]);
            sTerminVergabe[0] = DatFunk.WochenTag(sTerminVergabe[1]);
            sTerminVergabe[3] = sTerminVergabe[3] + sTerminVergabe[2];
            terminVergabe.add(sTerminVergabe.clone());
            if (terminVergabe.size() > 0) {
                Reha.instance.mousePositionLabel.setForeground(Color.RED);
                Reha.instance.mousePositionLabel.setText(
                        Integer.toString(terminVergabe.size()) + " * " + terminVergabe.get(0)[8] + " in Liste");
            } else {
                Reha.instance.mousePositionLabel.setForeground(Color.BLACK);
                Reha.instance.mousePositionLabel.setText("Druckliste = leer");
            }
            try {
                Reha.instance.shiftLabel.setText("bereit für F2= " + sTerminVergabe[8] + "°" + sTerminVergabe[9] + "°"
                        + sTerminVergabe[4] + " Min.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            terminVergabe.clear();
            Reha.instance.mousePositionLabel.setForeground(Color.BLACK);
            Reha.instance.mousePositionLabel.setText("Druckliste = leer");
            Reha.instance.shiftLabel.setText("");
        }

        /*
         * for(int y = 0; y<terminVergabe.size();y++ ){
         * //System.out.println("*********************"); for(int i = 0;i <
         * sTerminVergabe.length;i++){ //System.out.println(terminVergabe.get(y)[i]); }
         * //System.out.println("*********************"); }
         */
    }

    public void setzeTerminAktuell(String adatum, String auhrzeit, String abehandler) {
        if (aktAnsicht == Ansicht.NORMAL) {
            boolean indarstellung = false;
            int setspalte = -1;
            for (int i = 0; i < 7; i++) {
                if (oCombo[i].getSelectedItem()
                             .toString()
                             .trim()
                             .equals(abehandler.trim())) {
                    indarstellung = true;
                    setspalte = i;
                    break;
                }
            }
            if (!indarstellung) {
                setspalte = 0;
                oCombo[setspalte].setSelectedItem(abehandler);
            }
            if (this.aktuellerTag != adatum) {
                tagBlaettern(Integer.parseInt(Long.toString(DatFunk.TageDifferenz(this.aktuellerTag, adatum))));
            }
            int azeile = KollegenListe.getDBZeile(oCombo[setspalte].getSelectedIndex()) - 1;
            int ablock = -1;
            int alang = ((Vector<?>) ((ArrayList<?>) vTerm.get(azeile)).get(2)).size();
            for (int i = 0; i < alang; i++) {
                if (((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(azeile)).get(2)).get(i)).trim()
                                                                                             .equals(auhrzeit.trim()
                                                                                                     + ":00")) {
                    ablock = i;
                    break;
                }
            }
            if (ablock >= 0) {
                if (aktiveSpalte[2] != setspalte) {
                    oSpalten[aktiveSpalte[2]].spalteDeaktivieren();
                    oSpalten[aktiveSpalte[2]].repaint();
                }
                aktiveSpalte[0] = ablock;
                aktiveSpalte[2] = setspalte;
                oSpalten[setspalte].blockGeklickt(ablock);
            }
        } // bis hierher Normal_Ansicht.
    }

    public void aktualisieren() {
        if (this.aktAnsicht == Ansicht.NORMAL) {
            /* String sstmt = */ansichtStatement(this.aktuellerTag, aktAnsicht);
        } else if (this.aktAnsicht == Ansicht.WOCHE) {
            if (this.wocheAktuellerTag.isEmpty()) {
                this.wocheAktuellerTag = this.aktuellerTag;
            }
            /* String sstmt = */ansichtStatement(this.wocheAktuellerTag, aktAnsicht);
        }
    }

    private void terminListe() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                df = new DruckFenster(Reha.getThisFrame(), terminVergabe);
                df.setSize(new Dimension(760, 480));
                df.setLocation(new Point(50, 150));
                df.setFocusTabelle();
                df.setVisible(true);
            }
        });
    }

    public TerminFenster getTerminFensterInstance() {
        return this;
    }

    int getAktiveSpalte(int index) {
        return aktiveSpalte[index];
    }

    private void schnellSuche(Connection connection) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    sf = new SchnellSuche(Reha.getThisFrame(), getTerminFensterInstance(), connection);
                    sf.setSize(new Dimension(720, 400));
                    sf.setLocation(new Point(250, 200));
                    sf.setVisible(true);
                } catch (Exception ex) {
                }
            }
        });
    }

    public void setAktiverBlock(int block) {
        aktiveSpalte[0] = block;
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        //// System.out.println(dte.getSource());
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        String mitgebracht = null;
        //// System.out.println("Es wurde gedroppt");

        if (TerminFenster.DRAG_MODE == TerminFenster.DRAG_NONE) {
            oSpalten[aktiveSpalte[2]].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[0]);
            dragLab[aktiveSpalte[2]].setIcon(null);
            dragLab[aktiveSpalte[2]].setText("");
            //// System.out.println("Drag_Mode == Drag_None");
            dtde.dropComplete(true);
            return;
        }

        //// System.out.println("Drag_Mode != Drag_None");

        try {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            terminGedropt = true;
            Transferable tr = dtde.getTransferable();
            DataFlavor[] flavors = tr.getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].getRepresentationClass()
                              .toString()
                              .equals("java.lang.String")) {
                    mitgebracht = tr.getTransferData(flavors[i])
                                    .toString();
                }
                mitgebracht = (String) tr.getTransferData(flavors[i]);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        dtde.dropComplete(true);

        int x = dtde.getLocation().x;
        int breit = TerminFlaeche.getWidth() / 7;
        for (int i = 0; i < 7; i++) {
            if ((x >= (i * breit)) && (x <= ((i * breit) + breit))) {
                oSpalten[i].BlockTestOhneAktivierung(dtde.getLocation().x - (i * breit), dtde.getLocation().y);

                aktiveSpalte = oSpalten[i].BlockTest(dtde.getLocation().x - (i * breit), dtde.getLocation().y,
                        aktiveSpalte);

                oSpalten[i].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[0]);

                int behandler = -1;
                switch (aktAnsicht) {
                case NORMAL:
                    behandler = belegung[i];
                    break;
                case WOCHE:
                    behandler = i;
                    break;
                case MASKE:
                    behandler = i;
                    break;
                }
                if (behandler <= -1) {
                    return;
                }

                String sname = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(0)).get(aktiveSpalte[0]);
                String sreznum = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(1)).get(
                        aktiveSpalte[0]);

                // Hier testen ob alter Block mit Daten gefüllt war
                if (!sname.equals("")) {
                    int frage = JOptionPane.showConfirmDialog(null,
                            "Wollen Sie den bisherigen Eintrag -> " + sname + " <- tatsächlich überschreiben?",
                            "Achtung wichtige Benutzeranfrage!!!", JOptionPane.YES_NO_OPTION);
                    if (frage == JOptionPane.NO_OPTION) {
                        break;
                    }
                }
                String[] teilen;
                //// System.out.println("D&DÜbergabe = "+mitgebracht);
                if (mitgebracht.indexOf("°") >= 0) {
                    teilen = mitgebracht.split("°");
                    if (!teilen[0].contains("TERMDAT")) {
                        // System.out.println("! teilen[0].contains('TERMDAT')");
                        return;
                    }

                    if ((altaktiveSpalte[0] == aktiveSpalte[0]) && (altaktiveSpalte[2] == aktiveSpalte[2])
                            && sname.equals(teilen[1]) && sreznum.equals(teilen[2])) {
                        // System.out.println("altaktiveSpalte[0]==aktiveSpalte[0]) &&
                        // (altaktiveSpalte[2]==aktiveSpalte[2])");
                        return;
                    }

                    teilen[3] = teilen[3].toUpperCase();
                    teilen[3] = teilen[3].replaceAll(" MIN.", "");

                    datenSpeicher[0] = teilen[1];
                    datenSpeicher[1] = teilen[2];
                    datenSpeicher[3] = teilen[3];
                    try {
                        terminBreak = false;
                        datenAusSpeicherHolen();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if (terminBreak) {
                        try {
                            oSpalten[altaktiveSpalte[2]].spalteDeaktivieren();
                            // System.out.println("oSpalten[altaktiveSpalte[2]].spalteDeaktivieren()");
                        } catch (Exception ex) {
                        }
                        return;
                    }
                    int[] spaltneu = aktiveSpalte.clone();
                    if (TerminFenster.DRAG_MODE == TerminFenster.DRAG_MOVE) {

                        long zeit = System.currentTimeMillis();
                        boolean grobRaus = false;
                        while (getUpdateVerbot()) {
                            try {
                                Thread.sleep(20);
                                if ((System.currentTimeMillis() - zeit) > 2500) {
                                    grobRaus = true;
                                    break;
                                }
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                        // System.out.println("Wert von Grobraus = "+grobRaus);
                        zeit = System.currentTimeMillis();
                        if (!grobRaus) {
                            try {
                                String sbeginnneu = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(
                                        2)).get(spaltneu[0]);
                                int spAktiv = aktiveSpalte[2];
                                aktiveSpalte = altaktiveSpalte.clone();
                                wartenAufReady = true;
                                grobRaus = false;
                                setUpdateVerbot(true);
                                /*
                                 * //xx while(getUpdateVerbot()){ try { Thread.sleep(20); if(
                                 * (System.currentTimeMillis()-zeit) > 2500){ grobRaus = true; break; } } catch
                                 * (InterruptedException e1) { e1.printStackTrace(); } }
                                 */
                                // System.out.println("Wert von Grobraus = "+grobRaus);
                                if (!grobRaus) {
                                    // Stufe 2 - o.k.
                                    if (altaktiveSpalte[2] == spAktiv) {

                                        ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(2)).get(
                                                altaktiveSpalte[0]);
                                        int lang = ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(0)).size();
                                        // Suche nach Uhrzeit -> "+sbeginn
                                        for (int i2 = 0; i2 < lang; i2++) {
                                            if (((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(2)).get(i2)
                                                                                                          .equals(DRAG_UHR)) {
                                                aktiveSpalte[0] = i2;
                                                aktiveSpalte[1] = i2;
                                                try {
                                                    String tagundstart = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(
                                                            behandler)).get(5)).get(4);
                                                    tagundstart = tagundstart
                                                            + ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(
                                                                    2)).get(i2);
                                                    String altdauer = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(
                                                            behandler)).get(3)).get(i2);
                                                    String altname = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(
                                                            behandler)).get(0)).get(i2);
                                                    String altrezept = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(
                                                            behandler)).get(1)).get(i2);
                                                    String altbehandler = "";
                                                    if (aktAnsicht == Ansicht.NORMAL) {
                                                        altbehandler = KollegenListe.getKollegenUeberReihe(
                                                                behandler + 1);
                                                    } else if (aktAnsicht == Ansicht.WOCHE) {
                                                        altbehandler = KollegenListe.getKollegenUeberReihe(
                                                                wocheBehandler);
                                                    }
                                                    terminAusmustern(tagundstart, altdauer, altbehandler, altname,
                                                            altrezept);

                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                }
                                                wartenAufReady = true;
                                                blockSetzen(11);
                                                break;
                                            }
                                        }
                                    } else {
                                        // System.out.println("Termin verschieben aber nicht in der selben Spalte");
                                        wartenAufReady = true;

                                        int ialtbehandler = 0;
                                        int i2 = altaktiveSpalte[2];
                                        int ibehandlung = altaktiveSpalte[0];
                                        switch (aktAnsicht) {
                                        case NORMAL:
                                            ialtbehandler = belegung[i2];
                                            break;
                                        case WOCHE:
                                            ialtbehandler = i2;
                                            break;
                                        case MASKE:
                                            ialtbehandler = i2;
                                            break;
                                        }

                                        String tagundstart = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(
                                                ialtbehandler)).get(5)).get(4);
                                        tagundstart = tagundstart
                                                + ((Vector<?>) ((ArrayList<?>) vTerm.get(ialtbehandler)).get(2)).get(
                                                        ibehandlung);
                                        String altdauer = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(
                                                ialtbehandler)).get(3)).get(ibehandlung);
                                        String altname = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(
                                                ialtbehandler)).get(0)).get(ibehandlung);
                                        String altrezept = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(
                                                ialtbehandler)).get(1)).get(ibehandlung);
                                        String altbehandler = "";
                                        if (aktAnsicht == Ansicht.NORMAL) {
                                            altbehandler = KollegenListe.getKollegenUeberReihe(ialtbehandler + 1);
                                        } else if (aktAnsicht == Ansicht.WOCHE) {
                                            altbehandler = KollegenListe.getKollegenUeberReihe(wocheBehandler);
                                        }
                                        terminAusmustern(tagundstart, altdauer, altbehandler, altname, altrezept);

                                        blockSetzen(11);
                                        aktiveSpalte = spaltneu.clone();
                                    }

                                    int lang = ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(0)).size();
                                    for (int i2 = 0; i2 < lang; i2++) {
                                        if (((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(2)).get(
                                                i2)).trim()
                                                    .equals(sbeginnneu.trim())) {
                                            aktiveSpalte[0] = i2;
                                            aktiveSpalte[1] = i2;
                                            break;
                                        }
                                    }
                                } else {
                                    aktiveSpalte = spaltneu.clone();
                                    wartenAufReady = false;
                                    SqlInfo.loescheLocksMaschine();
                                }
                                aktiveSpalte = spaltneu.clone();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                SqlInfo.loescheLocksMaschine();
                            }
                        } else {
                            aktiveSpalte = spaltneu.clone();
                            wartenAufReady = false;
                            SqlInfo.loescheLocksMaschine();
                        }
                    }
                    if ((spaltneu[2] != altaktiveSpalte[2]) && (altaktiveSpalte[2] >= 0)) {
                        oSpalten[altaktiveSpalte[2]].spalteDeaktivieren();
                    }

                }
                try {
                    for (int x2 = 0; x2 < 1; x2++) {
                        String name = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(0)).get(
                                aktiveSpalte[0] + 1);
                        String nummer = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(1)).get(
                                aktiveSpalte[0] + 1);
                        if (name.trim()
                                .equals(DRAG_PAT.trim()) && nummer.equals(DRAG_NUMMER.trim())) {
                            aktiveSpalte[0]++;
                            aktiveSpalte[1]++;
                            break;
                        }
                        name = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(0)).get(
                                aktiveSpalte[0] - 1);
                        nummer = (String) ((Vector<?>) ((ArrayList<?>) vTerm.get(behandler)).get(1)).get(
                                aktiveSpalte[0] - 1);
                        if (name.trim()
                                .equals(DRAG_PAT.trim()) && nummer.equals(DRAG_NUMMER.trim())) {
                            aktiveSpalte[0]--;
                            aktiveSpalte[1]--;
                            break;
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (altaktiveSpalte[2] >= 0) {
                    dragLab[aktiveSpalte[2]].setIcon(null);
                    dragLab[aktiveSpalte[2]].setText("");
                    dragLab[altaktiveSpalte[2]].setIcon(null);
                    dragLab[altaktiveSpalte[2]].setText("");
                }
                oSpalten[i].schwarzAbgleich(aktiveSpalte[0], aktiveSpalte[0]);
            } else {
                oSpalten[i].spalteDeaktivieren();
            }

        }
    }

    public static void setDragMode(int mode) {
        DRAG_MODE = mode;
    }

    void terminBestaetigen(int spalte, boolean forceDlg) {
        if ((Rechte.hatRecht(Rechte.Kalender_terminconfirminpast, false)) || (this.getAktuellerTag()
                                                                                  .equals(DatFunk.sHeute()))) {
            gruppeAusschalten();
        } else if (!Rechte.hatRecht(Rechte.Kalender_terminconfirminpast, true)) {
            gruppeAusschalten();
            return;
        }

        if (aktAnsicht == Ansicht.WOCHE) {
            // JOptionPane.showMessageDialog(null,"Behandlungsbestätigung ist nur für den
            // aktuellen Tag in der -> Normalansicht <- möglich");
            // gruppeAusschalten();
            if (!DatFunk.sHeute()
                        .equals(DatFunk.sDatPlusTage(wocheErster, aktiveSpalte[2]))) {
                if (!Rechte.hatRecht(Rechte.Kalender_terminconfirminpast, true)) {
                    gruppeAusschalten();
                    return;
                }
            }
            if (SystemConfig.isAndi) {
                gruppeAusschalten();
                return;
            }
        }

        if (aktiveSpalte[0] < 0) {
            gruppeAusschalten();
            return;
        }
        int xaktBehandler = 0;
        if (aktAnsicht == Ansicht.NORMAL) {
            xaktBehandler = belegung[aktiveSpalte[2]];
        } else if (aktAnsicht == Ansicht.WOCHE) {
            xaktBehandler = wochenbelegung - 1;
        } else if (aktAnsicht == Ansicht.MASKE) {
            JOptionPane.showMessageDialog(null, "Terminaufnahme in Definition der Wochenarbeitszeit nicht möglich");
            gruppeAusschalten();
            return;
        }
        if (xaktBehandler < 0) {
            gruppeAusschalten();
            return;
        }

        String sname, sreznum, sorigreznum, sbeginn, sende, sdatum;
        if (aktAnsicht == Ansicht.NORMAL) {
            sname = ((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(0)).get(aktiveSpalte[0]));
            sreznum = ((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(1)).get(aktiveSpalte[0]));
            sorigreznum = sreznum;
            sbeginn = ((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(2)).get(aktiveSpalte[0]));
            sende = ((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(4)).get(aktiveSpalte[0]));
            sdatum = ((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(xaktBehandler)).get(5)).get(4));
        } else {
            sname = ((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(aktiveSpalte[2])).get(0)).get(aktiveSpalte[0]));
            sreznum = ((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(aktiveSpalte[2])).get(1)).get(aktiveSpalte[0]));
            sorigreznum = sreznum;
            sbeginn = ((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(aktiveSpalte[2])).get(2)).get(aktiveSpalte[0]));
            sende = ((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(aktiveSpalte[2])).get(4)).get(aktiveSpalte[0]));
            sdatum = ((String) ((Vector<?>) ((ArrayList<?>) vTerm.get(aktiveSpalte[2])).get(5)).get(4));
        }

        int occur = sreznum.indexOf("\\");
        if (occur > -1) {
            sorigreznum = sreznum.replace("\\", "\\\\");
            sreznum = sreznum.substring(0, occur);
        }
        if (sreznum.length() <= 2) {
            JOptionPane.showMessageDialog(null, "Falsche oder nicht vorhandene Rezeptnummer");
            gruppeAusschalten();
            return;
        }

        final String swreznum = sreznum;
        final String sworigreznum = sorigreznum;
        final String swaltname = sname;
        final String swname = sname.replaceAll("\u00AE", "");
        final String swbeginn = sbeginn;
        final String swende = sende;
        final String swdatum = sdatum;

        final int swbehandler = xaktBehandler;

        final boolean xforceDlg = forceDlg;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                /*
                 * Vector<BestaetigungsDaten> hMPos= new Vector<BestaetigungsDaten>();
                 * hMPos.add(new BestaetigungsDaten(false, "./.", 0, 0,false,false));
                 * hMPos.add(new BestaetigungsDaten(false, "./.", 0, 0,false,false));
                 * hMPos.add(new BestaetigungsDaten(false, "./.", 0, 0,false,false));
                 * hMPos.add(new BestaetigungsDaten(false, "./.", 0, 0,false,false));
                 */
                Vector<String> vec = null;
                String copyright = "\u00AE";
                try {
                    // 0=termine,
                    // 1=pos1
                    // 2=pos2
                    // 3=pos3
                    // 4=pos4
                    // 5=hausbes
                    // 6=unter18
                    // 7=jahrfrei
                    // 8=pat_intern
                    // 9=preisgruppe
                    // 10=zzregel
                    // 11=anzahl1
                    // 12=anzahl2
                    // 13=anzahl3
                    // 14=anzahl4
                    // 15=preisgruppe
                    // die anzahlen 1-4 werden jetzt zusammenhängend ab index 11 abgerufen
                    vec = SqlInfo.holeSatz("verordn",
                            "termine,pos1,pos2,pos3,pos4,hausbes,unter18,jahrfrei,pat_intern,preisgruppe,zzregel,anzahl1,anzahl2,anzahl3,anzahl4,preisgruppe",
                            "rez_nr='" + swreznum + "'", Arrays.asList(new String[] {}));
                    if (vec.size() > 0) {
                        StringBuffer termbuf = new StringBuffer();
                        termbuf.append(vec.get(0));
                        if (termbuf.toString()
                                   .contains(swdatum)) {
                            JOptionPane.showMessageDialog(null,
                                    "Dieser Termin ist am " + DatFunk.sDatInDeutsch(swdatum) + " bereits erfasst");
                            gruppeAusschalten();
                            return null;
                        }
                        Object[] objTerm = RezTools.BehandlungenAnalysieren(swreznum, false, xforceDlg, false,
                                ((Vector<String>) vec.clone()), computeLocation(null, 240, 250, swbeginn, swende),
                                KollegenListe.getMatchCodeUeberDBZeile(swbehandler + 1),
                                DatFunk.sDatInDeutsch(swdatum));
                        if (objTerm == null) {
                            return null;
                        }
                        if ((Integer) objTerm[1] == RezTools.REZEPT_ABBRUCH) {
                            return null;
                        } else if ((Integer) objTerm[1] == RezTools.REZEPT_IST_BEREITS_VOLL) {
                            String anzahl = "??????"; // TodDo
                            String message = "<html><b><font size='5'>Auf dieses Rezept wurden bereits<font size='6' color='#ff0000'> "
                                    + anzahl + " </font>Behandlungen durchgeführt!"
                                    + "<br>Verordnete Menge ist<font size='6' color='#ff0000'> " + vec.get(11)
                                    + "</font><br>Das Rezept ist somit bereits voll und darf für aktuelle Behandlung nicht mehr<br>"
                                    + "verwendet werden!!!!<br><br>"
                                    + "Gescannte Rezeptnummer =<font size='6' color='#ff0000'> " + swreznum
                                    + "</font><br><br></html>";
                            JOptionPane.showMessageDialog(null, message);
                            return null;
                        } else {
                            termbuf.append((String) objTerm[0]);
                            if ((Integer) objTerm[1] == RezTools.REZEPT_IST_JETZ_VOLL) {
                                String message = "<html><b><font size='5'>Das Rezept ist jetzt voll"
                                        + "<br>Rezeptnummer = <font size='6' color='#ff0000'> " + swreznum
                                        + "</font><br>"
                                        + "<br>Bitte das Rezept zur Abrechnung vorbereiten.</font></b></html>";
                                JOptionPane.showMessageDialog(null, message);
                                try {
                                    RezTools.fuelleVolleTabelle(swreznum,
                                            KollegenListe.getMatchCodeUeberDBZeile(swbehandler + 1));
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(null, "Fehler beim Aufruf von 'fuelleVolleTabelle'");
                                }

                            }
                        }
                        boolean unter18 = (vec.get(6)
                                              .equals("T") ? true : false);
                        boolean vorjahrfrei = (vec.get(7)
                                                  .equals("") ? false : true);
                        if (!unter18 && !vorjahrfrei) { // =Normalfall
                            SqlInfo.aktualisiereSatz("verordn", "termine='" + termbuf.toString() + "'",
                                    "rez_nr='" + swreznum + "'");
                            // hier soundeffekt einbauen falls keine Rezeptgebühren bezahlt
                            if (SystemConfig.RezGebWarnung) {
                                RezTools.RezGebSignal(swreznum);
                            }
                        } else if (unter18 && !vorjahrfrei) {
                            /// Testen ob immer noch unter 18 ansonsten ZuZahlungsstatus ändern;
                            String geboren = DatFunk.sDatInDeutsch(
                                    SqlInfo.holePatFeld("geboren", "pat_intern='" + vec.get(8) + "'"));
                            //// System.out.println("Geboren = "+geboren);
                            if (DatFunk.Unter18(DatFunk.sDatInDeutsch(swdatum), geboren)) {
                                SqlInfo.aktualisiereSatz("verordn", "termine='" + termbuf.toString() + "'",
                                        "rez_nr='" + swreznum + "'");
                            } else {
                                SqlInfo.aktualisiereSatz("verordn",
                                        "termine='" + termbuf.toString() + "', zzstatus='2'",
                                        "rez_nr='" + swreznum + "'");
                                // hier soundeffekt einbauen falls keine Rezeptgebühren bezahlt
                                if (SystemConfig.RezGebWarnung) {
                                    RezTools.RezGebSignal(swreznum);
                                }
                            }

                        } else if (!unter18 && vorjahrfrei) {
                            String bef_pat = SqlInfo.holePatFeld("befreit", "pat_intern='" + vec.get(8) + "'");
                            String bezahlt = SqlInfo.holeRezFeld("rez_bez", "rez_nr='" + swreznum + "'");
                            if (!bef_pat.equals("T") && bezahlt.equals("F")) {
                                if (DatFunk.DatumsWert("31.12." + vec.get(7)) < DatFunk.DatumsWert(swdatum)) {
                                    SqlInfo.aktualisiereSatz("verordn",
                                            "termine='" + termbuf.toString() + "', zzstatus='2'",
                                            "rez_nr='" + swreznum + "'");
                                    // hier soundeffekt einbauen falls keine Rezeptgebühren bezahlt
                                    if (SystemConfig.RezGebWarnung) {
                                        RezTools.RezGebSignal(swreznum);
                                    }
                                } else {
                                    SqlInfo.aktualisiereSatz("verordn", "termine='" + termbuf.toString() + "'",
                                            "rez_nr='" + swreznum + "'");
                                }
                            } else {
                                SqlInfo.aktualisiereSatz("verordn", "termine='" + termbuf.toString() + "'",
                                        "rez_nr='" + swreznum + "'");
                            }
                        } else {
                            SqlInfo.aktualisiereSatz("verordn", "termine='" + termbuf.toString() + "'",
                                    "rez_nr='" + swreznum + "'");
                            // hier soundeffekt einbauen falls keine Rezeptgebühren bezahlt
                            if (SystemConfig.RezGebWarnung) {
                                RezTools.RezGebSignal(swreznum);
                            }
                        }

                        /* Datenbank beschreiben */
                        String sblock = Integer.toString(aktiveSpalte[0] + 1);
                        String toupdate = "T" + sblock + " = '" + copyright + swname + "'";
                        String towhere = "datum='" + swdatum + "' AND " + "behandler='"
                                + ((swbehandler + 1) < 10 ? "0" + Integer.toString(swbehandler + 1) + "BEHANDLER"
                                        : Integer.toString(swbehandler + 1) + "BEHANDLER")
                                + "' " + "AND TS" + sblock + "='" + swbeginn + "' AND T" + sblock + "='" + swaltname
                                + "' AND N" + sblock + "='" + sworigreznum + "'";

                        SqlInfo.aktualisiereSatz("flexkc", toupdate, towhere);
                        /* Ende Datenbank beschreiben */
                        /* hier müßte noch zwischen Wochen- und Normalansicht differenziert werden */
                        if (aktAnsicht == Ansicht.NORMAL) {
                            ((ArrayList<Vector<String>>) vTerm.get(swbehandler)).get(0)
                                                                                .set(aktiveSpalte[0],
                                                                                        copyright + swname);
                        } else {
                            ((ArrayList<Vector<String>>) vTerm.get(aktiveSpalte[2])).get(0)
                                                                                    .set(aktiveSpalte[0],
                                                                                            copyright + swname);
                        }

                        oSpalten[aktiveSpalte[2]].repaint();
                        JComponent patient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
                        if (patient != null) {
                            if (Reha.instance.patpanel.aktRezept.rezAngezeigt.equals(swreznum)) {
                                Reha.instance.patpanel.aktRezept.updateEinzelTermine(termbuf.toString());
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Dieses Rezept existiert nicht bzw. ist bereits abgerechnet!!");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    vec = null;
                }
                return null;
            }
        }.execute();
    }

    private Point computeLocation(Window win, int x, int y, String start, String ende) {
        int xwin, ywin;
        if (win == null) {
            xwin = x;
            ywin = y;
        } else {
            xwin = win.getWidth();
            ywin = win.getHeight();
        }

        Point p = oSpalten[aktiveSpalte[2]].getLocationOnScreen();
        try {
            float ypos = oSpalten[aktiveSpalte[2]].getFloatPixelProMinute()
                    * (Float.valueOf(ZeitFunk.MinutenSeitMitternacht(ende))
                            - Float.valueOf(ZeitFunk.MinutenSeitMitternacht(TKSettings.KalenderUmfang[0])));
            // Zuerst Y-testen
            if (Math.round(p.y + ypos + ywin) > p.y + oSpalten[aktiveSpalte[2]].getHeight()) {
                // Fenster würde nach unten ins Nirvana abtauchen
                ypos = oSpalten[aktiveSpalte[2]].getFloatPixelProMinute()
                        * (Float.valueOf(ZeitFunk.MinutenSeitMitternacht(start))
                                - Float.valueOf(ZeitFunk.MinutenSeitMitternacht(TKSettings.KalenderUmfang[0])));
                p.y = Math.round((p.y + ypos) - ywin);
            } else {
                p.y = Math.round(p.y + ypos);
            }
            // Jetzt X-testen damit nichts nach rechts abhaut
            if (p.x + xwin > viewPanel.getLocationOnScreen().x + viewPanel.getWidth()) {
                p.x = (viewPanel.getLocationOnScreen().x + viewPanel.getWidth()) - xwin;
            }
        } catch (Exception ex) {
            /* wird nicht ausgewertet */}

        return p;
    }

    private void gruppeAusschalten() {
        gruppierenAktiv = false;
        gruppierenBloecke[0] = -1;
        gruppierenBloecke[1] = -1;
        try {
            oSpalten[gruppierenSpalte].setInGruppierung(false);
            oSpalten[gruppierenSpalte].shiftGedrueckt(false);
        } catch (Exception ex) {
        }
    }

    private class comboToolTip extends MouseAdapter {
        private int welche = -1;

        private comboToolTip(int welche) {
            super();
            this.welche = welche;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (aktAnsicht == Ansicht.WOCHE) {
                setDayForToolTip();
                tooltip[welche] = "<html>" + dayname[welche] + "<br>" + DatFunk.sDatPlusTage(wocheErster, welche)
                        + "</html>";
                oCombo[welche].setToolTipText(tooltip[welche]);
            }

        }

    }

    public void setTimeLine(boolean zeigen) {
        for (int i = 0; i < 7; i++) {
            oSpalten[i].setShowTimeLine(zeigen);
        }
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        // TODO Auto-generated method stub

    }


    void setSwSetWahl(String setName) {
        this.swSetWahl = setName;
    }
}

/** Ende Klasse. */

class KalZeichnen implements Runnable {
    private TherapeutenTag kPanel = null;
    private int belegung;
    private Vector vTerm = null;

    public KalZeichnen(TherapeutenTag oSpalten, Vector vTerm, int belegung) {
        this.kPanel = oSpalten;
        this.vTerm = vTerm;
        this.belegung = belegung;
    }

    @Override
    public void run() {
        this.kPanel.datenZeichnen(vTerm, belegung);
    }
}

class LockRecord implements Runnable {

    private Statement sState;
    private ResultSet rs;
    private String threadStmt = "";

    private void SatzSperren() {
        TerminFenster.setLockOk(0, "");
        this.sState = TerminFenster.getThisClass().privstmt;

        try {
            threadStmt = "select * from flexlock where sperre = '" + TerminFenster.getLockStatement() + "' LIMIT 1";
            rs = this.sState.executeQuery(threadStmt);
            if (!rs.next()) {
                new Thread(new SetLock()).start();
                TerminFenster.setLockOk(1, "");
                TerminFenster.setLockSpalte(TerminFenster.getLockStatement());
                Reha.instance.messageLabel.setText("Lock erfolgreich == 1");
            } else {
                TerminFenster.setLockOk(-1, rs.getString("maschine"));
                Reha.instance.messageLabel.setText("Lock misslungen");
                Reha.instance.messageLabel.setText("Lock misslungen == -1");
            }
        } catch (SQLException ex) {
            // System.out.println("von ResultSet SQLState: " + ex.getSQLState());
            // System.out.println("von ResultSet ErrorCode: " + ex.getErrorCode ());
            // System.out.println("von ResultSet ErrorMessage: " + ex.getMessage ());

            TerminFenster.setLockOk(-1, " Durch Fehler in SQL-Statement:" + ex.getMessage());
            Reha.instance.messageLabel.setText("Lock misslungen");
        }
    }

    @Override
    public void run() {
        SatzSperren();
    }
}

class UnlockRecord implements Runnable {

    private void SatzEntsperren() {
        SqlInfo.sqlAusfuehren("delete from flexlock where maschine like '%" + SystemConfig.dieseMaschine + "%'");
    }

    @Override
    public void run() {
        SatzEntsperren();
    }
}

class SetLock implements Runnable {
    private String threadStmt = "";
    private Statement sState = null;

    private void LockSetzen() {
        threadStmt = "insert into flexlock set sperre = '" + TerminFenster.getLockStatement() + "' , maschine = '"
                + SystemConfig.dieseMaschine + "', zeit='" + Long.toString(System.currentTimeMillis()) + "'";
        try {
            this.sState = TerminFenster.getThisClass().privstmt;

            this.sState.execute(threadStmt);
            this.sState.execute("COMMIT");

        } catch (SQLException ex) {
            SqlInfo.sqlAusfuehren("delete from flexlock where maschine like '%" + SystemConfig.dieseMaschine + "%'");
            Reha.instance.messageLabel.setText("Entsperren misslungen");
            TerminFenster.setLockOk(-1, " Durch Fehler in SQL-Statement:" + ex.getMessage());
        }
    }

    @Override
    public void run() {
        LockSetzen();
    }
}

final class sperrTest extends Thread {
    private int gelesen;

    @Override
    public void run() {
        do {
            if (gelesen > 1) { // beim ersten Aufruf liegen bereits aktuelle Daten vor
                if (TerminFenster.getThisClass() == null) {
                    break;
                }
                if (!TerminFenster.getThisClass()
                                  .getUpdateVerbot()) {
                    try {
                        // Reha.instance.shiftLabel.setText("in Update...");
                        TerminFenster.getThisClass()
                                     .setUpdateVerbot(true);
                        TerminFenster.getThisClass()
                                     .aktualisieren();
                        TerminFenster.getThisClass()
                                     .setUpdateVerbot(false);
                        // Reha.instance.shiftLabel.setText("Update ok.");
                        gelesen++;
                    } catch (Exception ex) {
                        SqlInfo.loescheLocksMaschine();
                        // SqlInfo.sqlAusfuehren("delete from flexlock where maschine like
                        // '%"+SystemConfig.dieseMaschine+"%'");
                        break;
                    }
                    // ********>Toolkit.getDefaultToolkit().beep();
                } else {
                    // TestFenster.LabelSetzen(2,"DB-Aktualisierungsverbot");
                    gelesen++;
                    // Toolkit.getDefaultToolkit().beep();
                }
                if (gelesen > 10000) {
                    gelesen = 2;
                }
            } else {
                gelesen++;
            }

            try {
                Thread.sleep(TKSettings.UpdateIntervall);
            } catch (InterruptedException e) {
            }
        } while (true);
    }
}
