package abrechnung.privat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTitledPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.therapi.reha.patient.AktuelleRezepte;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.AdressTools;
import CommonTools.DatFunk;
import CommonTools.JRtaComboBox;
import CommonTools.JRtaRadioButton;
import CommonTools.SqlInfo;
import CommonTools.StringTools;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextFieldService;
import ag.ion.bion.officelayer.text.ITextTable;
import ag.ion.bion.officelayer.text.TextException;
import dialoge.DragWin;
import dialoge.PinPanel;
import environment.Path;
import events.PatStammEvent;
import events.PatStammEventClass;
import events.RehaTPEventClass;
import hauptFenster.Reha;
import jxTableTools.TableTool;
import office.OOService;
import office.OOTools;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;
import umfeld.Betriebsumfeld;

import static abrechnung.privat.PreisanwendenStrategie.*;

public class AbrechnungPrivat extends JXDialog {
    private final class AbrechnungHausbesuch {
        final boolean mitHausBesuch;
        final boolean einzelnAbrechenbar;

        public AbrechnungHausbesuch(boolean mitHausBesuch, boolean einzelnabrechenbar) {
            this.mitHausBesuch = mitHausBesuch;
            this.einzelnAbrechenbar = einzelnabrechenbar;
        }
    }

    private static final int PREISGRUPPE_BG = 4;
    private static final int OK = 0;
    private static final int ABBRECHEN = -1;
    public static final int KORREKTUR = -2;

    public int rueckgabe;

    private final String rezeptNummer;

    private static final long serialVersionUID = 1036517682792665034L;

    private JXTitledPanel jtp;
    private MouseAdapter mymouse;
    private PinPanel pinPanel;
    private JXPanel content;
    private RehaTPEventClass rtp;
    private int preisgruppe;
    private JRtaComboBox preisgruppenfuerDisziCmbBox;
    private JLabel[] labs = { null, null, null, null, null, null, null };
    private JLabel adr1;
    private JLabel adr2;
    private DecimalFormat dcf = new DecimalFormat("#########0.00");
    private ButtonGroup bg = new ButtonGroup();

    private String disziplin = "";
    private int aktGruppe;

    private Vector<Vector<String>> preisliste;

    private Vector<String> originalPos = new Vector<>();
    private Vector<Integer> originalAnzahl = new Vector<>();
    private Vector<Double> einzelPreis = new Vector<>();
    private Vector<String> originalId = new Vector<>();
    private Vector<String> originalLangtext = new Vector<>();

    private Vector<BigDecimal> zeilenGesamt = new Vector<>();
    private BigDecimal rechnungGesamt = BigDecimal.ZERO;

    private HashMap<String, String> hmAdresse = new HashMap<>();
    private String aktRechnung = "";

    private int aktuellePosition;
    private int patKilometer;

    private StringBuffer writeBuf = new StringBuffer();
    private StringBuffer rechnungBuf = new StringBuffer();

    private int preisregel;
    private boolean wechselcheck;

    private int[] splitpreise = { 0, 0 };
    /**
     * contains information on splitting price strategy. array of size 3.
     * <p>
     * [0] all old prices
     * <p>
     * [1] all new prices
     * <p>
     * [2] splitting must be applied.
     */
    private boolean[] preisanwenden = { false, false, false };
    private PreisanwendenStrategie preisstrategie = PreisanwendenStrategie.examine(preisanwenden);

    private Vector<Integer> hbvec = new Vector<>();
    private Vector<Integer> kmvec = new Vector<>();

    private String hatAbweichendeAdresse;

    private String patid;

    private Rezept aktuellesRezept;
    private Vector<String> patDaten;

    private String aktIk;

    private String privatRgFormular;

    private HashMap<String, String> hmAbrechnung;

    private JRtaRadioButton privatRechnungBtn;
    private AbrechnungHausbesuch abrechnungHausbesuch;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbrechnungPrivat.class);

    public AbrechnungPrivat(JXFrame owner, String titel, int preisgruppe) {
        this(owner, titel, preisgruppe, (JComponent) Reha.getThisFrame()
                                                         .getGlassPane(),
                Reha.instance.patpanel.vecaktrez.get(1),
                SystemPreislisten.hmPreise.get(RezTools.getDisziplinFromRezNr(Reha.instance.patpanel.vecaktrez.get(1)))
                                          .get(preisgruppe - 1),
                Reha.instance.patpanel.patDaten.get(5), Reha.instance.patpanel.patDaten.get(66),
                Reha.instance.patpanel.vecaktrez, Reha.instance.patpanel.patDaten, Betriebsumfeld.getAktIK(),
                SystemConfig.hmAbrechnung.get("hmpriformular"), SystemConfig.hmAbrechnung,
                SystemPreislisten.hmPreisGruppen.get(
                        StringTools.getDisziplin(Reha.instance.patpanel.vecaktrez.get(1))));
    }

    AbrechnungPrivat(JXFrame owner, String titel, int preisgruppe, JComponent glasspane, String rezeptNr,
            Vector<Vector<String>> preisliste, String hatAbweichendeAdresse, String patientenDbID,
            Vector<String> aktuellesRezeptVector, Vector<String> aktuellerPatientDaten, String aktIk,
            String privatRgFormular, HashMap<String, String> hmAbrechnung, Vector<String> preisgruppenFuerDiszi) {
        super(owner, glasspane);
        this.hmAbrechnung = hmAbrechnung;
        this.privatRgFormular = privatRgFormular;
        patDaten = aktuellerPatientDaten;
        aktuellesRezept = new Rezept(aktuellesRezeptVector);
        this.hatAbweichendeAdresse = hatAbweichendeAdresse;
        patid = patientenDbID;
        this.rezeptNummer = rezeptNr;
        this.aktIk = aktIk;
        disziplin = RezTools.getDisziplinFromRezNr(rezeptNr);
        this.preisliste = preisliste;
        this.preisgruppe = preisgruppe;
        setUndecorated(true);
        setName("Privatrechnung");
        jtp = new JXTitledPanel();
        jtp.setName("Privatrechnung");
        mymouse = new DragWin(this);
        jtp.addMouseListener(mymouse);
        jtp.addMouseMotionListener(mymouse);
        jtp.setContentContainer(getContent(preisgruppenFuerDiszi));
        jtp.setTitleForeground(Color.WHITE);
        jtp.setTitle(titel);
        pinPanel = new PinPanel();
        pinPanel.getGruen()
                .setVisible(false);
        pinPanel.setName("Privatrechnung");
        jtp.setRightDecoration(this.pinPanel);
        setContentPane(jtp);
        setResizable(false);
        rtp = new RehaTPEventClass();
        rtp.addRehaTPEventListener(e -> fensterSchliessen());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private JXPanel getContent(Vector<String> preisgruppenFuerDiszi) {
        content = new JXPanel(new BorderLayout());
        content.add(getFields(preisgruppenFuerDiszi), BorderLayout.CENTER);
        content.add(getButtons(), BorderLayout.SOUTH);
        content.addKeyListener(kl);
        return content;
    }

    private JXPanel getFields(Vector<String> preisgruppenFuerDiszi) {
        JXPanel pan = new JXPanel();
        // 1 2 3 4 5 6 7
        FormLayout lay = new FormLayout("20dlu,fill:0:grow(0.5),p,fill:0:grow(0.5),20dlu",
                // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26
                "20dlu,p,2dlu,p,10dlu,p,2dlu,p,10dlu,p,3dlu,p,5dlu, p,1dlu,p,1dlu,p,1dlu,p ,1dlu,p,1dlu,p,1dlu,p, fill:0:grow(0.5),5dlu");
        pan.setLayout(lay);
        CellConstraints cc = new CellConstraints();
        pan.setOpaque(false);
        JLabel lab = new JLabel("Abrechnung Rezeptnummer: " + rezeptNummer);
        lab.setForeground(Color.BLUE);
        pan.add(lab, cc.xy(3, 1, CellConstraints.DEFAULT, CellConstraints.CENTER));
        adr1 = new JLabel(" ");
        adr1.setForeground(Color.BLUE);
        pan.add(adr1, cc.xy(3, 2));
        adr2 = new JLabel(" ");
        adr2.setForeground(Color.BLUE);
        pan.add(adr2, cc.xy(3, 4));

        lab = new JLabel("Preisgruppe wählen:");
        pan.add(lab, cc.xy(3, 6));

        this.aktGruppe = this.preisgruppe - 1;

        preisgruppenfuerDisziCmbBox = new JRtaComboBox(preisgruppenFuerDiszi);
        preisgruppenfuerDisziCmbBox.setSelectedIndex(this.aktGruppe);
        preisgruppenfuerDisziCmbBox.setActionCommand("neuertarif");
        preisgruppenfuerDisziCmbBox.addActionListener(neuerTarifAL);
        pan.add(preisgruppenfuerDisziCmbBox, cc.xy(3, 8));
        privatRechnungBtn = new JRtaRadioButton("Formular für Privatrechnung verwenden");
        privatRechnungBtn.addChangeListener(cl);
        pan.add(privatRechnungBtn, cc.xy(3, 10));
        JRtaRadioButton kostentraegerBtn = new JRtaRadioButton("Formular für Kostenträger Rechnung verwenden");
        kostentraegerBtn.addChangeListener(cl);
        pan.add(kostentraegerBtn, cc.xy(3, 12));
        bg.add(privatRechnungBtn);
        bg.add(kostentraegerBtn);

        if (preisgruppe == PREISGRUPPE_BG) {
            kostentraegerBtn.setSelected(true);
            regleBGE();
        } else {
            privatRechnungBtn.setSelected(true);
            reglePrivat();
        }

        if (!"0".equals(aktuellesRezept.aktuellesRezept_8_artderbeh1())) {
            labs[0] = new JLabel();
            labs[0].setForeground(Color.BLUE);
            pan.add(labs[0], cc.xy(3, 14));
        }
        if (!"0".equals(aktuellesRezept.aktuellesRezept_9_artderbeh2())) {
            labs[1] = new JLabel();
            labs[1].setForeground(Color.BLUE);
            pan.add(labs[1], cc.xy(3, 16));
        }
        if (!"0".equals(aktuellesRezept.aktuellesRezept_10_artderbeh3())) {
            labs[2] = new JLabel();
            labs[2].setForeground(Color.BLUE);
            pan.add(labs[2], cc.xy(3, 18));
        }
        if (!"0".equals(aktuellesRezept.aktuellesRezept_11_artderbeh4())) {
            labs[3] = new JLabel();
            labs[3].setForeground(Color.BLUE);
            pan.add(labs[3], cc.xy(3, 20));
        }
        // Mit Hausbesuch

        abrechnungHausbesuch = new AbrechnungHausbesuch("T".equals(aktuellesRezept.aktuellesRezept_43_hausbesuch()),
                "T".equals(aktuellesRezept.aktuellesRezept_61_einzelnabrechenbar()));

        if (abrechnungHausbesuch.mitHausBesuch) {

            labs[4] = new JLabel();
            labs[4].setForeground(Color.RED);
            pan.add(labs[4], cc.xy(3, 22));
            labs[5] = new JLabel();
            labs[5].setForeground(Color.RED);
            pan.add(labs[5], cc.xy(3, 24));
        }
        labs[6] = new JLabel();
        labs[6].setForeground(Color.BLUE);
        pan.add(labs[6], cc.xy(3, 26));

        doNeuerTarif();
        pan.validate();
        return pan;
    }

    private JXPanel getButtons() {
        JXPanel pan = new JXPanel();
        pan.setOpaque(false);// 1 2 3 4 5 6 7
        FormLayout lay = new FormLayout("5dlu,fill:0:grow(0.5),50dlu,10dlu,50dlu,10dlu,50dlu,fill:0:grow(0.5),5dlu",
                // 1 2 3 4 5 6 7 8 9 10 11 12
                "5dlu,fill:0:grow(0.5),p,fill:0:grow(0.5),5dlu");
        pan.setLayout(lay);

        CellConstraints cc = new CellConstraints();

        JButton okBtn = macheBut("Ok", "ok", al);
        pan.add(okBtn, cc.xy(3, 3));
        okBtn.addKeyListener(kl);

        JButton korrekturBtn = macheBut("Korrektur", "korrektur", al);
        pan.add(korrekturBtn, cc.xy(5, 3));
        korrekturBtn.addKeyListener(kl);

        JButton abbrechnenBtn = macheBut("abbrechen", "abbrechen", al);
        pan.add(abbrechnenBtn, cc.xy(7, 3));
        abbrechnenBtn.addKeyListener(kl);

        return pan;
    }

    private JButton macheBut(String titel, String cmd, ActionListener actionListener) {
        JButton but = new JButton(titel);
        but.setName(cmd);
        but.setActionCommand(cmd);
        but.addActionListener(actionListener);
        return but;
    }

    private void doRgRechnungPrepare() {
        if (privatRechnungBtn.isSelected()) {
            doPrivat();
        } else {
            doBGE();
        }
        posteAktualisierung(patDaten.get(29));
        fensterSchliessen();
    }

    private void posteAktualisierung(final String patid) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                PatStammEvent pEvt = new PatStammEvent(this);
                pEvt.setPatStammEvent("PatSuchen");
                pEvt.setDetails("#PATSUCHEN", patid, "");
                PatStammEventClass.firePatStammEvent(pEvt);
                return null;
            }

        }.execute();
    }

    private void holePrivat() {
        if (hatPatientAbweichendeAdresse()) {
            String[] adressParams = AdressTools.holeAbweichendeAdresse(patid);
            hmAdresse.put("<pri1>", adressParams[0]);
            hmAdresse.put("<pri2>", adressParams[1]);
            hmAdresse.put("<pri3>", adressParams[2]);
            hmAdresse.put("<pri4>", adressParams[3]);
            hmAdresse.put("<pri5>", adressParams[4]);
        } else {
            hmAdresse.put("<pri1>", SystemConfig.hmAdrPDaten.get("<Panrede>"));
            hmAdresse.put("<pri2>", SystemConfig.hmAdrPDaten.get("<Padr1>"));
            hmAdresse.put("<pri3>", SystemConfig.hmAdrPDaten.get("<Padr2>"));
            hmAdresse.put("<pri4>", SystemConfig.hmAdrPDaten.get("<Padr3>"));
            hmAdresse.put("<pri5>", SystemConfig.hmAdrPDaten.get("<Pbanrede>"));
        }
    }

    private boolean hatPatientAbweichendeAdresse() {
        return "T".equals(hatAbweichendeAdresse);
    }

    private void holeBGE() {
        hmAdresse.put("<pri1>", SystemConfig.hmAdrKDaten.get("<Kadr1>"));
        hmAdresse.put("<pri2>", SystemConfig.hmAdrKDaten.get("<Kadr2>"));
        hmAdresse.put("<pri3>", SystemConfig.hmAdrKDaten.get("<Kadr3>"));
        hmAdresse.put("<pri4>", SystemConfig.hmAdrKDaten.get("<Kadr4>"));
        hmAdresse.put("<pri5>", "Sehr geehrte Damen und Herren");
    }

    private void doPrivat() {
        try {
            Thread.sleep(50);

            if (hatPatientAbweichendeAdresse()) {
                String[] adressParams = AdressTools.holeAbweichendeAdresse(patDaten.get(66));
                hmAdresse.put("<pri1>", adressParams[0]);
                hmAdresse.put("<pri2>", adressParams[1]);
                hmAdresse.put("<pri3>", adressParams[2]);
                hmAdresse.put("<pri4>", adressParams[3]);
                hmAdresse.put("<pri5>", adressParams[4]);
            } else {
                hmAdresse.put("<pri1>", SystemConfig.hmAdrPDaten.get("<Panrede>"));
                hmAdresse.put("<pri2>", SystemConfig.hmAdrPDaten.get("<Padr1>"));
                hmAdresse.put("<pri3>", SystemConfig.hmAdrPDaten.get("<Padr2>"));
                hmAdresse.put("<pri4>", SystemConfig.hmAdrPDaten.get("<Padr3>"));
                hmAdresse.put("<pri5>", SystemConfig.hmAdrPDaten.get("<Pbanrede>"));

                if (!hmAdresse.get("<pri2>")
                              .contains(StringTools.EGross(StringTools.EscapedDouble(patDaten.get(2))))
                        || !hmAdresse.get("<pri2>")
                                     .contains(StringTools.EGross(StringTools.EscapedDouble(patDaten.get(3))))) {
                    String meldung = "Fehler!!!! aktuelle Patientendaten - soll = "
                            + StringTools.EGross(StringTools.EscapedDouble(patDaten.get(3))) + " "
                            + StringTools.EGross(StringTools.EscapedDouble(patDaten.get(2))) + "\n" + "Istdaten sind\n"
                            + hmAdresse.get("<pri1>") + "\n" + hmAdresse.get("<pri2>") + "\n" + hmAdresse.get("<pri3>")
                            + "\n" + hmAdresse.get("<pri4>") + "\n" + hmAdresse.get("<pri5>");
                    JOptionPane.showMessageDialog(null, meldung);
                    return;
                }
            }
            aktRechnung = Integer.toString(SqlInfo.erzeugeNummer("rnr"));
            hmAdresse.put("<pri6>", aktRechnung);

            LOGGER.info(Path.Instance.getProghome() + "vorlagen\\" + aktIk + "\\" + privatRgFormular);
            ITextDocument textDocument = starteDokument(
                    Path.Instance.getProghome() + "vorlagen\\" + aktIk + "\\" + privatRgFormular);
            starteErsetzen(textDocument);
            startePositionen(textDocument);

            starteDrucken(textDocument);

            if (Reha.vollbetrieb) {
                doFaktura("privat");

                doOffenePosten("privat");

                doUebertrag();
            }
            doTabelle();
        } catch (Exception e) {
            LOGGER.error("Something bad happens here", e);
        }
    }

    private void doBGE() {
        try {
            Thread.sleep(50);
            hmAdresse.put("<pri1>", SystemConfig.hmAdrKDaten.get("<Kadr1>"));
            hmAdresse.put("<pri2>", SystemConfig.hmAdrKDaten.get("<Kadr2>"));
            hmAdresse.put("<pri3>", SystemConfig.hmAdrKDaten.get("<Kadr3>"));
            hmAdresse.put("<pri4>", SystemConfig.hmAdrKDaten.get("<Kadr4>"));
            hmAdresse.put("<pri5>", "Sehr geehrte Damen und Herren");
            aktRechnung = Integer.toString(SqlInfo.erzeugeNummer("rnr"));
            hmAdresse.put("<pri6>", aktRechnung);

            ITextDocument textDocument = starteDokument(Path.Instance.getProghome() + "vorlagen/"
                    + Betriebsumfeld.getAktIK() + "/" + SystemConfig.hmAbrechnung.get("hmbgeformular"));
            starteErsetzen(textDocument);
            startePositionen(textDocument);

            starteDrucken(textDocument);

            if (Reha.vollbetrieb) {
                doFaktura("bge");

                doOffenePosten("bge");

                doUebertrag();
            }
            doTabelle();
        } catch (Exception e) {
            LOGGER.error("Something bad happens here", e);
        }
    }

    private void doFaktura(String kostentraeger) {
        // Hier die Sätze in die faktura-datenbank schreiben

        String plz = "";
        String ort = "";
        int hbpos = ABBRECHEN;
        int wgpos = ABBRECHEN;
        int diff = originalPos.size() - originalId.size();
        if (diff == 2 && !preisanwenden[2]) {
            hbpos = originalId.size() + 1;
            wgpos = originalId.size() + 2;
        } else if (diff == 1 && !preisanwenden[2]) {
            hbpos = originalId.size() + 1;
        }
        try {
            int idummy = hmAdresse.get("<pri4>")
                                  .indexOf(' ');
            plz = hmAdresse.get("<pri4>")
                           .substring(0, idummy)
                           .trim();
            ort = hmAdresse.get("<pri4>")
                           .substring(idummy)
                           .trim();
        } catch (Exception ex) {
        }
        for (int i = 0; i < originalPos.size(); i++) {
            writeBuf.setLength(0);
            writeBuf.trimToSize();
            if (i == 0) {
                writeBuf.append("insert into faktura set kassen_nam='")
                        .append(StringTools.EscapedDouble(hmAdresse.get("<pri1>")))
                        .append("', ");
                writeBuf.append("kassen_na2='")
                        .append(StringTools.EscapedDouble(hmAdresse.get("<pri2>")))
                        .append("', ");
                writeBuf.append("strasse='")
                        .append(StringTools.EscapedDouble(hmAdresse.get("<pri3>")))
                        .append("', ");
                writeBuf.append("plz='")
                        .append(plz)
                        .append("', ort='")
                        .append(ort)
                        .append("', ");
                writeBuf.append("name='")
                        .append(StringTools.EscapedDouble(patDaten.get(2) + ", " + patDaten.get(3)))
                        .append("', ");
            } else {
                writeBuf.append("insert into faktura set ");
            }
            writeBuf.append("lfnr='")
                    .append(i)
                    .append("', ");
            if (i == (hbpos - 1) || hbvec.indexOf(i) >= 0) {
                // Hausbesuch
                writeBuf.append("pos_int='")
                        .append(RezTools.getIDFromPos(originalPos.get(i), "", preisliste))
                        .append("', ");
                writeBuf.append("anzahl='")
                        .append(originalAnzahl.get(i))
                        .append("', ");
                writeBuf.append("anzahltage='")
                        .append(originalAnzahl.get(i))
                        .append("', ");
            } else if (i == (wgpos - 1) || kmvec.indexOf(i) >= 0) {
                // Weggebühren Kilometer und Pauschale differenzieren
                writeBuf.append("pos_int='")
                        .append(RezTools.getIDFromPos(originalPos.get(i), "", preisliste))
                        .append("', ");
                writeBuf.append("anzahl='")
                        .append(originalAnzahl.get(i))
                        .append("', ");
                if (patKilometer > 0) {
                    String tage = Integer.toString(originalAnzahl.get(i) / patKilometer);
                    writeBuf.append("anzahltage='")
                            .append(tage)
                            .append("', ");
                    writeBuf.append("kilometer='")
                            .append(dcf.format(Double.parseDouble(Integer.toString(patKilometer)))
                                       .replace(",", "."))
                            .append("', ");
                } else {
                    writeBuf.append("anzahltage='")
                            .append(originalAnzahl.get(i))
                            .append("', ");
                }
            } else {
                try {
                    writeBuf.append("pos_int='")
                            .append(originalId.get(i))
                            .append("', ");
                    writeBuf.append("anzahl='")
                            .append(originalAnzahl.get(i))
                            .append("', ");
                    writeBuf.append("anzahltage='")
                            .append(originalAnzahl.get(i))
                            .append("', ");
                } catch (Exception ex) {
                    System.out.println("\n******************Fehler*****************");
                    System.out.println("Durchlauf = " + i);
                    System.out.println("Vectorsize originalId = " + originalId.size());
                    System.out.println("Vectorsize originalAnzahl = " + originalAnzahl.size());
                    System.out.println("Vectorsize anzahltage = " + originalAnzahl.size());
                    System.out.println("******************Fehler*****************");
                }
            }
            writeBuf.append("pos_kas='")
                    .append(originalPos.get(i))
                    .append("', ");
            writeBuf.append("kuerzel='")
                    .append(RezTools.getKurzformFromPos(originalPos.get(i), "", preisliste))
                    .append("', ");
            writeBuf.append("preis='")
                    .append(dcf.format(einzelPreis.get(i))
                               .replace(",", "."))
                    .append("', ");
            writeBuf.append("gesamt='")
                    .append(dcf.format(zeilenGesamt.get(i)
                                                   .doubleValue())
                               .replace(",", "."))
                    .append("', ");
            writeBuf.append("zuzahl='F', ");
            writeBuf.append("zzbetrag='0.00', ");
            writeBuf.append("netto='")
                    .append(dcf.format(zeilenGesamt.get(i)
                                                   .doubleValue())
                               .replace(",", "."))
                    .append("', ");
            writeBuf.append("rez_nr='")
                    .append(rezeptNummer)
                    .append("', ");
            writeBuf.append("rezeptart='")
                    .append("privat".equals(kostentraeger) ? "1" : "2")
                    .append("', ");
            writeBuf.append("rnummer='")
                    .append(aktRechnung)
                    .append("', ");
            writeBuf.append("pat_intern='")
                    .append(aktuellesRezept.aktuellesRezept_0_patintern())
                    .append("', ");
            writeBuf.append("kassid='")
                    .append(aktuellesRezept.aktuellesRezept_37_kassenId())
                    .append("', ");
            writeBuf.append("arztid='")
                    .append(aktuellesRezept.aktuellesRezept_16_arztID())
                    .append("', ");
            writeBuf.append("disziplin='")
                    .append(rezeptNummer.trim(), 0, 2)
                    .append("', ");
            writeBuf.append("rdatum='")
                    .append(DatFunk.sDatInSQL(DatFunk.sHeute()))
                    .append("',");
            writeBuf.append("ik='")
                    .append(Betriebsumfeld.getAktIK())
                    .append("'");
            SqlInfo.sqlAusfuehren(writeBuf.toString());
        }
    }

    private void doOffenePosten(String kostentraeger) {
        rechnungBuf.setLength(0);
        rechnungBuf.trimToSize();
        rechnungBuf.append("insert into rliste set ");
        rechnungBuf.append("r_nummer='")
                   .append(aktRechnung)
                   .append("', ");
        rechnungBuf.append("r_datum='")
                   .append(DatFunk.sDatInSQL(DatFunk.sHeute()))
                   .append("', ");
        if ("privat".equals(kostentraeger)) {
            rechnungBuf.append("r_kasse='")
                       .append(StringTools.EscapedDouble(patDaten.get(2) + ", " + patDaten.get(3)))
                       .append("', ");
        } else {
            rechnungBuf.append("r_kasse='")
                       .append(StringTools.EscapedDouble(hmAdresse.get("<pri1>")))
                       .append("', ");
            String rname = StringTools.EscapedDouble(
                    patDaten.get(2) + "," + patDaten.get(3) + "," + DatFunk.sDatInDeutsch(patDaten.get(4)));
            rechnungBuf.append("r_name='")
                       .append(rname)
                       .append("', ");
        }
        rechnungBuf.append("r_klasse='")
                   .append(rezeptNummer.trim(), 0, 2)
                   .append("', ");
        rechnungBuf.append("r_betrag='")
                   .append(dcf.format(rechnungGesamt.doubleValue())
                              .replace(",", "."))
                   .append("', ");
        rechnungBuf.append("r_offen='")
                   .append(dcf.format(rechnungGesamt.doubleValue())
                              .replace(",", "."))
                   .append("', ");
        rechnungBuf.append("r_zuzahl='0.00', ");
        rechnungBuf.append("ikktraeger='")
                   .append(aktuellesRezept.aktuellesRezept_37_kassenId())
                   .append("',");
        rechnungBuf.append("pat_intern='")
                   .append(aktuellesRezept.aktuellesRezept_0_patintern())
                   .append("',");
        rechnungBuf.append("ik='")
                   .append(Betriebsumfeld.getAktIK())
                   .append("'");
        SqlInfo.sqlAusfuehren(rechnungBuf.toString());
    }


    protected void doUebertrag() {
        String rez_nr = rezeptNummer;
        boolean wasSuccessfullyMoved = SqlInfo.transferRowToAnotherDB("verordn", "lza", "rez_nr", rez_nr, true,
                Arrays.asList(new String[] { "id" }));
        if (wasSuccessfullyMoved) {
            if ("T".equals(aktuellesRezept.aktuellesRezept_62_abschluss())) {
                SqlInfo.sqlAusfuehren("delete from fertige where rez_nr='" + rez_nr + "' LIMIT 1");
            }
            SqlInfo.sqlAusfuehren("delete from verordn where rez_nr='" + rez_nr + "'");
            Reha.instance.patpanel.historie.holeRezepte(patDaten.get(29), "");
            SqlInfo.sqlAusfuehren("delete from volle where rez_nr='" + rez_nr + "'");
        }
    }

    private void doTabelle() {
        SwingUtilities.invokeLater(() -> {
            int row = AktuelleRezepte.tabelleaktrez.getSelectedRow();
            if (row >= 0) {
                TableTool.loescheRowAusModel(AktuelleRezepte.tabelleaktrez, row);
                AktuelleRezepte.tabelleaktrez.repaint();
                Reha.instance.patpanel.aktRezept.setzeKarteiLasche();
            }
        });
    }

    private void doNeuerTarif() {
        String pos = "";
        String preis = "";
        String anzahl = "";
        einzelPreis.clear();
        originalPos.clear();
        originalAnzahl.clear();
        originalId.clear();
        originalLangtext.clear();
        zeilenGesamt.clear();
        rechnungGesamt = new BigDecimal("0.00");
        patKilometer = 0;
        hbvec.clear();
        kmvec.clear();

        /* Hier der Test auf Preisumstellung */
        Vector<String> tage = null;
        String preisdatum = null;
        // Anzahl alter Preis Anzahl neuer Preis
        splitpreise[0] = 0;
        splitpreise[1] = 0;
        // alle alt, alle neu, splitten
        preisanwenden[0] = false;
        preisanwenden[1] = true;
        preisanwenden[2] = false;

        preisstrategie = PreisanwendenStrategie.alleNeu;
        preisliste = SystemPreislisten.hmPreise.get(this.disziplin)
                                               .get(this.aktGruppe);
        try {
            preisdatum = SystemPreislisten.hmNeuePreiseAb.get(this.disziplin)
                                                         .get(this.aktGruppe);
            if (preisdatum == null || "".equals(preisdatum)) {
                preisregel = 0;
                wechselcheck = false;
            } else {
                preisregel = SystemPreislisten.hmNeuePreiseRegel.get(this.disziplin)
                                                                .get(this.aktGruppe);
            }
            tage = RezTools.holeEinzelTermineAusRezept(null, aktuellesRezept.aktuellesRezept_34_termine());
            if (tage.isEmpty() || preisregel == 0) {
                wechselcheck = false;
            } else {
                wechselcheck = true;
            }
            // Regel anwenden
            if (preisregel == 1 && wechselcheck) {
                // Behandlungsbeginn
                if (DatFunk.TageDifferenz(preisdatum, tage.get(0)) < 0) {
                    preisanwenden[0] = true;
                    preisanwenden[1] = false;
                    preisanwenden[2] = false;
                    preisstrategie = alleAlt;
                } else {
                    preisanwenden[0] = false;
                    preisanwenden[1] = true;
                    preisanwenden[2] = false;
                    preisstrategie = alleNeu;
                }
            } else if (preisregel == 2 && wechselcheck) {
                // Rezeptdatum
                if (DatFunk.TageDifferenz(preisdatum,
                        DatFunk.sDatInDeutsch(aktuellesRezept.aktuellesRezept_2_Rezeptdatum())) < 0) {
                    preisanwenden[0] = true;
                    preisanwenden[1] = false;
                    preisanwenden[2] = false;
                    preisstrategie = alleAlt;
                } else {
                    preisanwenden[0] = false;
                    preisanwenden[1] = true;
                    preisanwenden[2] = false;
                    preisstrategie = alleNeu;
                }
            } else if (preisregel == 3 && wechselcheck) {
                // beliebige Behandlung
                preisanwenden[0] = true;
                preisanwenden[1] = false;
                preisanwenden[2] = false;
                preisstrategie = alleAlt;
                for (int i = 0; i < tage.size(); i++) {
                    if (DatFunk.TageDifferenz(preisdatum, tage.get(i)) >= 0) {
                        preisanwenden[0] = false;
                        preisanwenden[1] = true;
                        preisanwenden[2] = false;
                        preisstrategie = alleNeu;
                        break;
                    }
                }
            } else if (preisregel == 4 && wechselcheck) {
                int max = Integer.parseInt(aktuellesRezept.aktuellesRezept_3_anzahl1());
                // splitten
                preisanwenden[0] = false;
                preisanwenden[1] = false;
                preisanwenden[2] = true;
                preisstrategie = splitten;
                for (int i = 0; i < tage.size(); i++) {
                    if (DatFunk.TageDifferenz(preisdatum, tage.get(i)) < 0) {
                        splitpreise[0] += 1;
                    } else {
                        splitpreise[1] += 1;
                    }
                }
                if (splitpreise[0] == max) {
                    preisanwenden[0] = true;
                    preisanwenden[1] = false;
                    preisanwenden[2] = false;
                    preisstrategie = alleAlt;
                } else if (splitpreise[1] == max) {
                    preisanwenden[0] = false;
                    preisanwenden[1] = true;
                    preisanwenden[2] = false;
                    preisstrategie = alleNeu;
                } else if ((splitpreise[0] != 0 && splitpreise[1] != 0) || wechselcheck) {
                    doNeuerTarifMitSplitting();
                    if (abrechnungHausbesuch.mitHausBesuch) {
                        analysiereHausbesuchMitSplitting();
                    }
                    for (int i = 0; i < originalAnzahl.size(); i++) {
                        BigDecimal zeilengesamt = BigDecimal.valueOf(einzelPreis.get(i))
                                                            .multiply(BigDecimal.valueOf(Double.valueOf(
                                                                    Integer.toString(originalAnzahl.get(i)))));
                        zeilenGesamt.add(BigDecimal.valueOf(zeilengesamt.doubleValue()));
                        rechnungGesamt = rechnungGesamt.add(BigDecimal.valueOf(zeilengesamt.doubleValue()));
                    }
                    try {
                        labs[6].setText("Rezeptwert = " + dcf.format(rechnungGesamt.doubleValue()) + " EUR");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return;
                } else {
                    if (splitpreise[0] > 0) {
                        preisanwenden[0] = true;
                        preisanwenden[1] = false;
                        preisanwenden[2] = false;
                        preisstrategie = alleAlt;
                    } else {
                        preisanwenden[0] = false;
                        preisanwenden[1] = true;
                        preisanwenden[2] = false;
                        preisstrategie = alleNeu;
                    }

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            wechselcheck = false;
            preisregel = 0;
        }

        // Änderungen in Preis
        Integer aktanzahl = (Integer) RezTools.holeTermineAnzahlUndLetzter(
                aktuellesRezept.aktuellesRezept_34_termine())[0];
        if (!"0".equals(aktuellesRezept.aktuellesRezept_8_artderbeh1())) {
            anzahl = aktuellesRezept.aktuellesRezept_3_anzahl1();
            if (Integer.parseInt(anzahl) > 1) {
                anzahl = Integer.toString(aktanzahl);
            }

            originalPos.add(aktuellesRezept.aktuellesRezept_48_pos1());
            originalId.add(aktuellesRezept.aktuellesRezept_8_artderbeh1());
            originalAnzahl.add(Integer.parseInt(anzahl));
            originalLangtext.add(
                    RezTools.getLangtextFromID(aktuellesRezept.aktuellesRezept_8_artderbeh1(), "", preisliste)
                            .replace("30Min.", "")
                            .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(aktuellesRezept.aktuellesRezept_8_artderbeh1(), preisliste);
            if (preisanwenden[0]) {
                preis = RezTools.getPreisAltFromID(aktuellesRezept.aktuellesRezept_8_artderbeh1(), "", preisliste);
            } else {
                preis = RezTools.getPreisAktFromID(aktuellesRezept.aktuellesRezept_8_artderbeh1(), "", preisliste);
            }

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preis));
                labs[0].setText(anzahl + " * " + pos + " (Einzelpreis = " + preis + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[0].setText(anzahl + " * " + pos + " (Einzelpreis = 0.00)");
            }
        }
        if (!"0".equals(aktuellesRezept.aktuellesRezept_9_artderbeh2())) {
            anzahl = aktuellesRezept.aktuellesRezept_4_anzahl2();
            if (Integer.parseInt(anzahl) > 1) {
                anzahl = Integer.toString(aktanzahl);
            }

            originalPos.add(aktuellesRezept.aktuellesRezept_49_pos2());
            originalId.add(aktuellesRezept.aktuellesRezept_9_artderbeh2());
            originalAnzahl.add(Integer.parseInt(anzahl));
            originalLangtext.add(
                    RezTools.getLangtextFromID(aktuellesRezept.aktuellesRezept_9_artderbeh2(), "", preisliste)
                            .replace("30Min.", "")
                            .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(aktuellesRezept.aktuellesRezept_9_artderbeh2(), preisliste);

            if (preisanwenden[0]) {
                preis = RezTools.getPreisAltFromID(aktuellesRezept.aktuellesRezept_9_artderbeh2(), "", preisliste);
            } else {
                preis = RezTools.getPreisAktFromID(aktuellesRezept.aktuellesRezept_9_artderbeh2(), "", preisliste);
            }
            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preis));
                labs[1].setText(anzahl + " * " + pos + " (Einzelpreis = " + preis + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[1].setText(anzahl + " * " + pos + " (Einzelpreis = 0.00)");
            }
        }
        if (!"0".equals(aktuellesRezept.aktuellesRezept_10_artderbeh3())) {
            anzahl = aktuellesRezept.aktuellesRezept_5_anzahl3();
            if (Integer.parseInt(anzahl) > 1) {
                anzahl = Integer.toString(aktanzahl);
            }

            originalPos.add(aktuellesRezept.aktuellesRezept_50_pos3());
            originalId.add(aktuellesRezept.aktuellesRezept_10_artderbeh3());
            originalAnzahl.add(Integer.parseInt(anzahl));
            originalLangtext.add(
                    RezTools.getLangtextFromID(aktuellesRezept.aktuellesRezept_10_artderbeh3(), "", preisliste)
                            .replace("30Min.", "")
                            .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(aktuellesRezept.aktuellesRezept_10_artderbeh3(), preisliste);

            if (preisanwenden[0]) {
                preis = RezTools.getPreisAltFromID(aktuellesRezept.aktuellesRezept_10_artderbeh3(), "", preisliste);
            } else {
                preis = RezTools.getPreisAktFromID(aktuellesRezept.aktuellesRezept_10_artderbeh3(), "", preisliste);
            }

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preis));
                labs[2].setText(anzahl + " * " + pos + " (Einzelpreis = " + preis + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[2].setText(anzahl + " * " + pos + " (Einzelpreis = 0.00)");
            }
        }
        if (!"0".equals(aktuellesRezept.aktuellesRezept_11_artderbeh4())) {
            anzahl = aktuellesRezept.aktuellesRezept_6_Anzahl4();
            if (Integer.parseInt(anzahl) > 1) {
                anzahl = Integer.toString(aktanzahl);
            }

            originalPos.add(aktuellesRezept.aktuellesRezept_51_pos4());
            originalId.add(aktuellesRezept.aktuellesRezept_11_artderbeh4());
            originalAnzahl.add(Integer.parseInt(anzahl));
            originalLangtext.add(
                    RezTools.getLangtextFromID(aktuellesRezept.aktuellesRezept_11_artderbeh4(), "", preisliste)
                            .replace("30Min.", "")
                            .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(aktuellesRezept.aktuellesRezept_11_artderbeh4(), preisliste);

            if (preisanwenden[0]) {
                preis = RezTools.getPreisAltFromID(aktuellesRezept.aktuellesRezept_11_artderbeh4(), "", preisliste);
            } else {
                preis = RezTools.getPreisAktFromID(aktuellesRezept.aktuellesRezept_11_artderbeh4(), "", preisliste);
            }

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preis));
                labs[3].setText(anzahl + " * " + pos + " (Einzelpreis = " + preis + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[3].setText(anzahl + " * " + pos + " (Einzelpreis = 0.00)");
            }
        }
        if (abrechnungHausbesuch.mitHausBesuch) {
            analysiereHausbesuch();
        }

        for (int i = 0; i < originalAnzahl.size(); i++) {
            BigDecimal zeilengesamt = BigDecimal.valueOf(einzelPreis.get(i))
                                                .multiply(BigDecimal.valueOf(
                                                        Double.valueOf(Integer.toString(originalAnzahl.get(i)))));
            zeilenGesamt.add(BigDecimal.valueOf(zeilengesamt.doubleValue()));
            rechnungGesamt = rechnungGesamt.add(BigDecimal.valueOf(zeilengesamt.doubleValue()));
        }
        try {
            labs[6].setText("Rezeptwert = " + dcf.format(rechnungGesamt.doubleValue()) + " EUR");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void analysiereHausbesuch() {
        this.aktGruppe = preisgruppenfuerDisziCmbBox.getSelectedIndex();
        labs[5].setText("");
        /* Hausbesuch voll abrechnen */
        int hbanzahl = (Integer) RezTools.holeTermineAnzahlUndLetzter(aktuellesRezept.aktuellesRezept_34_termine())[0];

        if (abrechnungHausbesuch.einzelnAbrechenbar) {
            String preis = "";
            String pos = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                     .get(this.aktGruppe)
                                                     .get(0);
            if (preisanwenden[0]) {
                preis = RezTools.getPreisAltFromPosNeu(pos, "", preisliste);
            } else {
                preis = RezTools.getPreisAktFromPos(pos, "", preisliste);
            }

            originalAnzahl.add(hbanzahl);
            originalPos.add(pos);
            einzelPreis.add(Double.parseDouble(preis));
            originalLangtext.add("Hausbesuchspauschale");
            labs[4].setText(hbanzahl + " * " + pos + " (Einzelpreis = " + preis + ")");
            patKilometer = StringTools.ZahlTest(patDaten.get(48));
            if (patKilometer <= 0) {
                // Keine Kilometer Im Patientenstamm hinterlegt
                if ("".equals((pos = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                                 .get(this.aktGruppe)
                                                                 .get(3)).trim())) {
                    // Wegegeldpauschale ist nicht vorgesehen und Kilometer sind null - ganz schön
                    // blöd....
                    JOptionPane.showMessageDialog(null,
                            "Im Patientenstamm sind keine Kilometer hinterlegt und eine pauschale\n"
                                    + "Wegegeldberechnung ist für diese Tarifgruppe nicht vorgesehen.\nWegegeld wird nicht abgerechnet!");
                } else {
                    if (preisanwenden[0]) {
                        preis = RezTools.getPreisAltFromPosNeu(pos, "", preisliste);
                    } else {
                        preis = RezTools.getPreisAktFromPos(pos, "", preisliste);
                    }
                    originalAnzahl.add(hbanzahl);
                    originalPos.add(pos);
                    einzelPreis.add(Double.parseDouble(preis));
                    originalLangtext.add("Wegegeldpauschale");
                    labs[5].setText(hbanzahl + " * " + pos + " (Einzelpreis = " + preis + ")");
                }
            } else /*
                    * es wurden zwar Kilometer angegeben aber diese Preisgruppe kennt keine
                    * Wegegebühr
                    */
            if ("".equals((pos = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                             .get(this.aktGruppe)
                                                             .get(2)).trim())) {
                JOptionPane.showMessageDialog(null,
                        "Im Patientenstamm sind zwar " + patKilometer
                                + " Kilometer hinterlegt aber Wegegeldberechnung\n"
                                + "ist für diese Tarifgruppe nicht vorgesehen.\nWegegeld wird nicht aberechnet!");
            } else {
                if (preisanwenden[0]) {
                    preis = RezTools.getPreisAltFromPosNeu(pos, "", preisliste);
                } else {
                    preis = RezTools.getPreisAktFromPos(pos, "", preisliste);
                }
                originalAnzahl.add(hbanzahl * patKilometer);
                originalPos.add(pos);
                einzelPreis.add(Double.parseDouble(preis));
                originalLangtext.add("Wegegeld / km");
                labs[5].setText(hbanzahl * patKilometer + " * " + pos + " (Einzelpreis = " + preis + ")");
            }
        } else { /* Hausbesuch mehrere abrechnen */
            String pos = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                     .get(this.aktGruppe)
                                                     .get(1);
            if ("".equals(pos.trim())) {
                JOptionPane.showMessageDialog(null,
                        "In dieser Tarifgruppe ist die Ziffer Hausbesuche - mehrere Patienten - nicht vorgeshen!\n");
            } else {
                String preis = "";
                if (preisanwenden[0]) {
                    preis = RezTools.getPreisAltFromPosNeu(pos, "", preisliste);
                } else {
                    preis = RezTools.getPreisAktFromPos(pos, "", preisliste);
                }
                originalAnzahl.add(hbanzahl);
                originalPos.add(pos);
                einzelPreis.add(Double.parseDouble(preis));
                originalLangtext.add("Hausbesuchspauschale (mehrere Patienten)");
                labs[5].setText(hbanzahl + " * " + pos + " (Einzelpreis = " + preis + ")");
            }
        }
    }

    private void doNeuerTarifMitSplitting() {
        // System.out.println("Disziplin = "+this.disziplin);
        // System.out.println("AktGruppe = "+this.aktGruppe);
        // System.out.println("stelle neuen Tarif ein....");

        String pos = "";
        einzelPreis.clear();
        originalPos.clear();
        originalAnzahl.clear();
        originalId.clear();
        originalLangtext.clear();
        zeilenGesamt.clear();
        rechnungGesamt = BigDecimal.valueOf(Double.parseDouble("0.00"));
        patKilometer = 0;
        String anzahlAlt = "", anzahlNeu = "";
        String preisAlt = "", preisNeu = "";
        int test = 0;
        int testanzahl = Integer.parseInt(aktuellesRezept.aktuellesRezept_3_anzahl1());
        if (testanzahl != splitpreise[0] + splitpreise[1]) {
            JOptionPane.showMessageDialog(null,
                    "Die Anwendungsregel dieser Tarifgruppe ist Splitting!!!\nBei dieser Regel müssen die Behandlungstage mit der Anzahl der Behandlungen im Rezept übereinstimmen!");
            return;
        }
        // Änderungen in Preis und ggfls. in Anzahl
        // Benötigt werde: Anzahlen auf dem Rezept, Anzahl alter Preis, Anzahl neuer
        // Preis.
        if (!"0".equals(aktuellesRezept.aktuellesRezept_8_artderbeh1())) {
            originalPos.add(aktuellesRezept.aktuellesRezept_48_pos1());
            originalId.add(aktuellesRezept.aktuellesRezept_8_artderbeh1());
            // jetzt Anzahlen für alter Preis
            originalAnzahl.add(splitpreise[0]);
            originalLangtext.add(
                    RezTools.getLangtextFromID(aktuellesRezept.aktuellesRezept_8_artderbeh1(), "", preisliste)
                            .replace("30Min.", "")
                            .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(aktuellesRezept.aktuellesRezept_8_artderbeh1(), preisliste);
            anzahlAlt = Integer.toString(splitpreise[0]);

            preisAlt = RezTools.getPreisAltFromID(aktuellesRezept.aktuellesRezept_8_artderbeh1(), "", preisliste);

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preisAlt));
                labs[0].setText(anzahlAlt + " * " + pos + " (Einzelpreis = " + preisAlt + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[0].setText(anzahlAlt + " * " + pos + " (Einzelpreis = 0.00)");
            }
            // jetzt Anzahlen für neuer Preis
            originalPos.add(aktuellesRezept.aktuellesRezept_48_pos1());
            originalId.add(aktuellesRezept.aktuellesRezept_8_artderbeh1());
            originalAnzahl.add(splitpreise[1]);
            originalLangtext.add(
                    RezTools.getLangtextFromID(aktuellesRezept.aktuellesRezept_8_artderbeh1(), "", preisliste)
                            .replace("30Min.", "")
                            .replace("45Min.", ""));
            pos = RezTools.getKurzformFromID(aktuellesRezept.aktuellesRezept_8_artderbeh1(), preisliste);
            anzahlNeu = Integer.toString(splitpreise[1]);
            preisNeu = RezTools.getPreisAktFromID(aktuellesRezept.aktuellesRezept_8_artderbeh1(), "", preisliste);

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preisNeu));
                labs[0].setText(
                        labs[0].getText() + " / " + anzahlNeu + " * " + pos + " (Einzelpreis = " + preisNeu + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[0].setText(labs[0].getText() + " / " + anzahlNeu + " * " + pos + " (Einzelpreis = 0.00)");
            }
        }
        if (!"0".equals(aktuellesRezept.aktuellesRezept_9_artderbeh2())) {
            originalPos.add(aktuellesRezept.aktuellesRezept_49_pos2());
            originalId.add(aktuellesRezept.aktuellesRezept_9_artderbeh2());
            test = Integer.parseInt(aktuellesRezept.aktuellesRezept_4_anzahl2());
            originalAnzahl.add(splitpreise[0] > test ? test : splitpreise[0]);
            originalLangtext.add(
                    RezTools.getLangtextFromID(aktuellesRezept.aktuellesRezept_9_artderbeh2(), "", preisliste)
                            .replace("30Min.", "")
                            .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(aktuellesRezept.aktuellesRezept_9_artderbeh2(), preisliste);
            anzahlAlt = Integer.toString(splitpreise[0] > test ? test : splitpreise[0]);
            preisAlt = RezTools.getPreisAltFromID(aktuellesRezept.aktuellesRezept_9_artderbeh2(), "", preisliste);

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preisAlt));
                labs[1].setText(anzahlAlt + " * " + pos + " (Einzelpreis = " + preisAlt + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[1].setText(anzahlAlt + " * " + pos + " (Einzelpreis = 0.00)");
            }
            // nur wenn die angegebene Anzahl < ist als Anzahl Tage im Rezeptblatt
            if (splitpreise[0] < test) {
                originalPos.add(aktuellesRezept.aktuellesRezept_49_pos2());
                originalId.add(aktuellesRezept.aktuellesRezept_9_artderbeh2());

                originalAnzahl.add(test - splitpreise[0]);
                originalLangtext.add(
                        RezTools.getLangtextFromID(aktuellesRezept.aktuellesRezept_9_artderbeh2(), "", preisliste)
                                .replace("30Min.", "")
                                .replace("45Min.", ""));
                pos = RezTools.getKurzformFromID(aktuellesRezept.aktuellesRezept_9_artderbeh2(), preisliste);
                anzahlNeu = Integer.toString(test - splitpreise[0]);
                preisNeu = RezTools.getPreisAktFromID(aktuellesRezept.aktuellesRezept_9_artderbeh2(), "", preisliste);

                if (!"".equals(pos.trim())) {
                    einzelPreis.add(Double.parseDouble(preisNeu));
                    labs[1].setText(
                            labs[1].getText() + " / " + anzahlNeu + " * " + pos + " (Einzelpreis = " + preisNeu + ")");
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                    labs[1].setText(labs[1].getText() + "/ " + anzahlNeu + " * " + pos + " (Einzelpreis = 0.00)");
                }
            }
        }
        if (!"0".equals(aktuellesRezept.aktuellesRezept_10_artderbeh3())) {
            originalPos.add(aktuellesRezept.aktuellesRezept_50_pos3());
            originalId.add(aktuellesRezept.aktuellesRezept_10_artderbeh3());
            test = Integer.parseInt(aktuellesRezept.aktuellesRezept_5_anzahl3());
            originalAnzahl.add(splitpreise[0] > test ? test : splitpreise[0]);
            originalLangtext.add(
                    RezTools.getLangtextFromID(aktuellesRezept.aktuellesRezept_10_artderbeh3(), "", preisliste)
                            .replace("30Min.", "")
                            .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(aktuellesRezept.aktuellesRezept_10_artderbeh3(), preisliste);
            test = Integer.parseInt(aktuellesRezept.aktuellesRezept_5_anzahl3());
            anzahlAlt = Integer.toString(splitpreise[0] > test ? test : splitpreise[0]);
            preisAlt = RezTools.getPreisAltFromID(aktuellesRezept.aktuellesRezept_10_artderbeh3(), "", preisliste);

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preisAlt));
                labs[2].setText(anzahlAlt + " * " + pos + " (Einzelpreis = " + preisAlt + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[2].setText(anzahlAlt + " * " + pos + " (Einzelpreis = 0.00)");
            }
            if (splitpreise[0] < test) {
                originalPos.add(aktuellesRezept.aktuellesRezept_50_pos3());
                originalId.add(aktuellesRezept.aktuellesRezept_10_artderbeh3());
                originalAnzahl.add(test - splitpreise[0]);
                originalLangtext.add(
                        RezTools.getLangtextFromID(aktuellesRezept.aktuellesRezept_10_artderbeh3(), "", preisliste)
                                .replace("30Min.", "")
                                .replace("45Min.", ""));
                anzahlNeu = Integer.toString(test - splitpreise[0]);
                preisNeu = RezTools.getPreisAktFromID(aktuellesRezept.aktuellesRezept_10_artderbeh3(), "", preisliste);
                if (!"".equals(pos.trim())) {
                    einzelPreis.add(Double.parseDouble(preisNeu));
                    labs[2].setText(
                            labs[2].getText() + " / " + anzahlNeu + " * " + pos + " (Einzelpreis = " + preisNeu + ")");
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                    labs[2].setText(labs[2].getText() + " / " + anzahlNeu + " * " + pos + " (Einzelpreis = 0.00)");
                }
            }
        }
        if (!"0".equals(aktuellesRezept.aktuellesRezept_11_artderbeh4())) {
            originalPos.add(aktuellesRezept.aktuellesRezept_51_pos4());
            originalId.add(aktuellesRezept.aktuellesRezept_11_artderbeh4());
            test = Integer.parseInt(aktuellesRezept.aktuellesRezept_6_Anzahl4());
            originalAnzahl.add(splitpreise[0] > test ? test : splitpreise[0]);
            originalLangtext.add(
                    RezTools.getLangtextFromID(aktuellesRezept.aktuellesRezept_11_artderbeh4(), "", preisliste)
                            .replace("30Min.", "")
                            .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(aktuellesRezept.aktuellesRezept_11_artderbeh4(), preisliste);
            anzahlAlt = Integer.toString(splitpreise[0] > test ? test : splitpreise[0]);
            preisAlt = RezTools.getPreisAltFromID(aktuellesRezept.aktuellesRezept_11_artderbeh4(), "", preisliste);

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preisAlt));
                labs[3].setText(anzahlAlt + " * " + pos + " (Einzelpreis = " + preisAlt + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[3].setText(anzahlAlt + " * " + pos + " (Einzelpreis = 0.00)");
            }
            if (splitpreise[0] < test) {
                originalPos.add(aktuellesRezept.aktuellesRezept_51_pos4());
                originalId.add(aktuellesRezept.aktuellesRezept_11_artderbeh4());
                originalAnzahl.add(test - splitpreise[0]);
                originalLangtext.add(
                        RezTools.getLangtextFromID(aktuellesRezept.aktuellesRezept_11_artderbeh4(), "", preisliste)
                                .replace("30Min.", "")
                                .replace("45Min.", ""));
                anzahlNeu = Integer.toString(test - splitpreise[0]);
                preisNeu = RezTools.getPreisAktFromID(aktuellesRezept.aktuellesRezept_11_artderbeh4(), "", preisliste);
                if (!"".equals(pos.trim())) {
                    einzelPreis.add(Double.parseDouble(preisNeu));
                    labs[3].setText(
                            labs[3].getText() + " / " + anzahlNeu + " * " + pos + " (Einzelpreis = " + preisNeu + ")");
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                    labs[3].setText(labs[3].getText() + " / " + anzahlNeu + " * " + pos + " (Einzelpreis = 0.00)");
                }
            }
        }
    }

    private void analysiereHausbesuchMitSplitting() {
        this.aktGruppe = preisgruppenfuerDisziCmbBox.getSelectedIndex();
        labs[5].setText("");

        /* Hausbesuch voll abrechnen */
        int hbanzahl = Integer.parseInt(aktuellesRezept.aktuellesRezept_64_hbAnzahl());
        int althb = -1;
        int neuhb = -1;
        String preisAlt = "";
        String preisNeu = "";

        if (abrechnungHausbesuch.einzelnAbrechenbar) {
            althb = splitpreise[0] > hbanzahl ? hbanzahl : splitpreise[0];
            String pos = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                     .get(this.aktGruppe)
                                                     .get(0);
            preisAlt = RezTools.getPreisAltFromPosNeu(pos, "", preisliste);
            originalAnzahl.add(splitpreise[0] > hbanzahl ? hbanzahl : splitpreise[0]);
            originalPos.add(pos);
            einzelPreis.add(Double.parseDouble(preisAlt));
            originalLangtext.add("Hausbesuchspauschale");
            labs[4].setText(althb + " * " + pos + " (Einzelpreis = " + preisAlt + ")");
            hbvec.add(originalPos.size() - 1);
            if (splitpreise[0] < hbanzahl) {
                neuhb = hbanzahl - splitpreise[0];
                originalAnzahl.add(neuhb);
                originalPos.add(pos);
                preisNeu = RezTools.getPreisAktFromPos(pos, "", preisliste);
                einzelPreis.add(Double.parseDouble(preisNeu));
                originalLangtext.add("Hausbesuchspauschale");
                labs[4].setText(labs[4].getText() + " / " + neuhb + " * " + pos + " (Einzelpreis = " + preisNeu + ")");
                hbvec.add(originalPos.size() - 1);
            }

            patKilometer = StringTools.ZahlTest(patDaten.get(48));

            if (patKilometer <= 0) {
                // Keine Kilometer Im Patientenstamm hinterlegt
                if ("".equals((pos = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                                 .get(this.aktGruppe)
                                                                 .get(3)).trim())) {
                    // Wegegeldpauschale ist nicht vorgesehen und Kilometer sind null - ganz schön
                    // blöd....
                    JOptionPane.showMessageDialog(null,
                            "Im Patientenstamm sind keine Kilometer hinterlegt und eine pauschale\n"
                                    + "Wegegeldberechnung ist für diese Tarifgruppe nicht vorgesehen.\nWegegeld wird nicht abgerechnet!");
                } else {
                    preisAlt = RezTools.getPreisAltFromPosNeu(pos, "", preisliste);
                    originalAnzahl.add(althb);
                    originalPos.add(pos);
                    einzelPreis.add(Double.parseDouble(preisAlt));
                    originalLangtext.add("Wegegeldpauschale");
                    labs[5].setText(althb + " * " + pos + " (Einzelpreis = " + preisAlt + ")");
                    kmvec.add(originalPos.size() - 1);
                    if (splitpreise[0] < hbanzahl) {
                        neuhb = hbanzahl - splitpreise[0];
                        originalAnzahl.add(neuhb);
                        originalPos.add(pos);
                        preisNeu = RezTools.getPreisAktFromPos(pos, "", preisliste);
                        einzelPreis.add(Double.parseDouble(preisNeu));
                        originalLangtext.add("Wegegeldpauschale");
                        labs[5].setText(
                                labs[5].getText() + " / " + neuhb + " * " + pos + " (Einzelpreis = " + preisNeu + ")");
                        kmvec.add(originalPos.size() - 1);
                    }
                }
            } else /*
                    * es wurden zwar Kilometer angegeben aber diese Preisgruppe kennt keine
                    * Wegegebühr
                    */
            if ("".equals((pos = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                             .get(this.aktGruppe)
                                                             .get(2)).trim())) {
                JOptionPane.showMessageDialog(null,
                        "Im Patientenstamm sind zwar " + patKilometer
                                + " Kilometer hinterlegt aber Wegegeldberechnung\n"
                                + "ist für diese Tarifgruppe nicht vorgesehen.\nWegegeld wird nicht aberechnet!");
            } else {
                preisAlt = RezTools.getPreisAltFromPosNeu(pos, "", preisliste);
                originalAnzahl.add(althb * patKilometer);
                originalPos.add(pos);
                einzelPreis.add(Double.parseDouble(preisAlt));
                originalLangtext.add("Wegegeld / km");
                labs[5].setText(althb * patKilometer + " * " + pos + " (Einzelpreis = " + preisAlt + ")");
                kmvec.add(originalPos.size() - 1);
                if (splitpreise[0] < hbanzahl) {
                    neuhb = hbanzahl - splitpreise[0];
                    originalAnzahl.add(neuhb * patKilometer);
                    originalPos.add(pos);
                    preisNeu = RezTools.getPreisAktFromPos(pos, "", preisliste);
                    einzelPreis.add(Double.parseDouble(preisNeu));
                    originalLangtext.add("Wegegeld / km");
                    labs[5].setText(labs[5].getText() + " / " + neuhb * patKilometer + " * " + pos + " (Einzelpreis = "
                            + preisNeu + ")");
                    kmvec.add(originalPos.size() - 1);
                }
            }
        } else { /* Hausbesuch mehrere abrechnen */
            String pos = SystemPreislisten.hmHBRegeln.get(disziplin)
                                                     .get(this.aktGruppe)
                                                     .get(1);
            if ("".equals(pos.trim())) {
                JOptionPane.showMessageDialog(null,
                        "In dieser Tarifgruppe ist die Ziffer Hausbesuche - mehrere Patienten - nicht vorgeshen!\n");
            } else {
                althb = splitpreise[0] > hbanzahl ? hbanzahl : splitpreise[0];
                preisAlt = RezTools.getPreisAltFromPosNeu(pos, "", preisliste);
                originalAnzahl.add(althb);
                originalPos.add(pos);
                einzelPreis.add(Double.parseDouble(preisAlt));
                originalLangtext.add("Hausbesuchspauschale (mehrere Patienten)");
                labs[5].setText(althb + " * " + pos + " (Einzelpreis = " + preisAlt + ")");
                hbvec.add(originalPos.size() - 1);
                if (splitpreise[0] < hbanzahl) {
                    neuhb = hbanzahl - splitpreise[0];
                    originalAnzahl.add(neuhb);
                    originalPos.add(pos);
                    preisNeu = RezTools.getPreisAktFromPos(pos, "", preisliste);
                    einzelPreis.add(Double.parseDouble(preisNeu));
                    originalLangtext.add("Hausbesuchspauschale (mehrere Patienten)");
                    labs[5].setText(
                            labs[5].getText() + " / " + neuhb + " * " + pos + " (Einzelpreis = " + preisNeu + ")");
                    hbvec.add(originalPos.size() - 1);
                }
            }
        }
    }

    private void korrekturReaktion() {
        rueckgabe = KORREKTUR;
        fensterSchliessen();
    }

    private void regleBGE() {
        holeBGE();
        adr1.setText("".equals(hmAdresse.get("<pri1>")
                                        .trim()) ? " " : hmAdresse.get("<pri1>"));
        adr2.setText("".equals(hmAdresse.get("<pri2>")
                                        .trim()) ? " " : hmAdresse.get("<pri2>"));
    }

    private void reglePrivat() {
        holePrivat();
        adr1.setText(hmAdresse.get("<pri1>")
                              .trim()
                              .isEmpty() ? " " : hmAdresse.get("<pri1>"));
        adr2.setText(hmAdresse.get("<pri2>")
                              .trim()
                              .isEmpty() ? " " : hmAdresse.get("<pri2>"));
    }

    private ActionListener neuerTarifAL = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            aktGruppe = preisgruppenfuerDisziCmbBox.getSelectedIndex();
            doNeuerTarif();

        }
    };

    private ActionListener al = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            String cmd = arg0.getActionCommand();
            if (cmd != null) {
                switch (cmd) {

                case "privatadresse":
                    reglePrivat();
                    break;
                case "kassendresse":
                    regleBGE();
                    break;
                case "neuertarif":
                    aktGruppe = preisgruppenfuerDisziCmbBox.getSelectedIndex();
                    doNeuerTarif();
                    break;
                case "korrektur":
                    korrekturReaktion();
                    break;
                case "abbrechen":
                    abbrechenReaktion();
                    break;
                case "ok":
                    okReaktion();
                    break;
                }
            }
        }
    };

    private KeyListener kl = new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent arg0) {
            if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                abbrechenReaktion();
                return;
            }
            if (arg0.getKeyCode() == KeyEvent.VK_ENTER && (JComponent) arg0.getSource() instanceof JButton) {
                if ("abbrechen".equals(((JComponent) arg0.getSource()).getName())) {
                    abbrechenReaktion();
                } else if ("korrektur".equals(((JComponent) arg0.getSource()).getName())) {
                    AbrechnungPrivat.this.korrekturReaktion();
                } else if ("ok".equals(((JComponent) arg0.getSource()).getName())) {
                    okReaktion();
                }
            }
        }

    };

    private void okReaktion() {
        rueckgabe = OK;
        doRgRechnungPrepare();
    }

    private void abbrechenReaktion() {
        rueckgabe = ABBRECHEN;
        fensterSchliessen();
    }

    private void fensterSchliessen() {
        setVisible(false);

    }

    private ITextDocument starteDokument(String url) throws Exception {
        IDocumentService documentService = new OOService().getOfficeapplication()
                                                          .getDocumentService();
        IDocumentDescriptor docdescript = new DocumentDescriptor();
        docdescript.setHidden(true);
        docdescript.setAsTemplate(true);
        IDocument document = documentService.loadDocument(url, docdescript);
        ITextDocument textDocument = (ITextDocument) document;
        if (privatRechnungBtn.isSelected()) {
            OOTools.druckerSetzen(textDocument, SystemConfig.hmAbrechnung.get("hmpridrucker"));
        } else {
            OOTools.druckerSetzen(textDocument, SystemConfig.hmAbrechnung.get("hmbgedrucker"));
        }

        return textDocument;
    }

    private void starteErsetzen(ITextDocument textDocument) {
        ITextFieldService textFieldService = textDocument.getTextFieldService();
        ITextField[] placeholders = null;
        try {
            placeholders = textFieldService.getPlaceholderFields();
        } catch (TextException e) {
            LOGGER.error("Something bad happens here", e);
        }

        for (ITextField placeholder : placeholders) {
            switch (placeholder.getDisplayText()
                               .toLowerCase()) {
            case "<pri1>":
                placeholder.getTextRange()
                           .setText(hmAdresse.get("<pri1>"));
                break;
            case "<pri2>":
                placeholder.getTextRange()
                           .setText(hmAdresse.get("<pri2>"));
                break;
            case "<pri3>":
                placeholder.getTextRange()
                           .setText(hmAdresse.get("<pri3>"));
                break;
            case "<pri4>":
                placeholder.getTextRange()
                           .setText(hmAdresse.get("<pri4>"));
                break;
            case "<pri5>":
                placeholder.getTextRange()
                           .setText(hmAdresse.get("<pri5>"));
                break;
            case "<pri6>":
                placeholder.getTextRange()
                           .setText(hmAdresse.get("<pri6>"));
                break;
            case "<pnname>":
                placeholder.getTextRange()
                           .setText(SystemConfig.hmAdrPDaten.get("<Pnname>"));
                break;
            case "<pvname>":
                placeholder.getTextRange()
                           .setText(SystemConfig.hmAdrPDaten.get("<Pvname>"));
                break;
            case "<pgeboren>":
                placeholder.getTextRange()
                           .setText(SystemConfig.hmAdrPDaten.get("<Pgeboren>"));
                break;
            case "<panrede>":
                placeholder.getTextRange()
                           .setText(SystemConfig.hmAdrPDaten.get("<Panrede>"));
                break;
            default:
                Set<Entry<String, String>> entries = SystemConfig.hmAdrRDaten.entrySet();

                for (Entry<String, String> entry : entries) {
                    try {
                        if (((String) entry.getKey()).equalsIgnoreCase(placeholder.getDisplayText())) {
                            placeholder.getTextRange()
                                       .setText((String) entry.getValue());
                            break;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        break;
                    }
                }
                break;
            }
        }
    }

    private void startePositionen(ITextDocument textDocument) throws TextException {
        aktuellePosition++;
        ITextTable textTable = textDocument.getTextTableService()
                                           .getTextTable("Tabelle1");
        ITextTable textEndbetrag = textDocument.getTextTableService()
                                               .getTextTable("Tabelle2");
        for (int i = 0; i < originalAnzahl.size(); i++) {
            textTable.getCell(0, aktuellePosition)
                     .getTextService()
                     .getText()
                     .setText(originalLangtext.get(i));
            textTable.getCell(1, aktuellePosition)
                     .getTextService()
                     .getText()
                     .setText(Integer.toString(originalAnzahl.get(i)));
            textTable.getCell(2, aktuellePosition)
                     .getTextService()
                     .getText()
                     .setText(dcf.format(einzelPreis.get(i)));

            textTable.getCell(3, aktuellePosition)
                     .getTextService()
                     .getText()
                     .setText(dcf.format(zeilenGesamt.get(i)
                                                     .doubleValue()));
            textTable.addRow(1);
            aktuellePosition++;
        }
        textEndbetrag.getCell(1, 0)
                     .getTextService()
                     .getText()
                     .setText(dcf.format(rechnungGesamt.doubleValue()) + " EUR");
    }

    private synchronized void starteDrucken(ITextDocument textDocument) {
        if ("1".equals(hmAbrechnung.get("hmallinoffice"))) {
            textDocument.getFrame()
                        .getXFrame()
                        .getContainerWindow()
                        .setVisible(true);
        } else {
            int exemplare = 0;
            if (privatRechnungBtn.isSelected()) {
                exemplare = Integer.parseInt(hmAbrechnung.get("hmpriexemplare"));
            } else {
                exemplare = Integer.parseInt(hmAbrechnung.get("hmbgeexemplare"));
            }
            OOTools.printAndClose(textDocument, exemplare);
        }
    }

    public int showAndWait(Point pt) {
        pt.move(-75, 30);
        pack();
        setModal(true);
        setVisible(true);
        return rueckgabe;
    }

    private ChangeListener cl = new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent arg0) {
            if (privatRechnungBtn.isSelected()) {
                reglePrivat();
            } else {
                regleBGE();
            }
        }
    };

}
