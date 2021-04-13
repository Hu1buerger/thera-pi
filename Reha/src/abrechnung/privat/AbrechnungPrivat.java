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
import java.util.Iterator;
import java.util.Map;
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

public class AbrechnungPrivat extends JXDialog {
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
    private JRtaComboBox jcmb;
    private JLabel[] labs = { null, null, null, null, null, null, null };
    private JLabel adr1;
    private JLabel adr2;
    private DecimalFormat dcf = new DecimalFormat("#########0.00");
    private ButtonGroup bg = new ButtonGroup();

    private String disziplin = "";
    private int aktGruppe;

    private Vector<Vector<String>> preisliste;

    private boolean hausBesuch;
    private boolean hbEinzeln;
    private Vector<String> originalPos = new Vector<>();
    private Vector<Integer> originalAnzahl = new Vector<>();
    private Vector<Double> einzelPreis = new Vector<>();
    private Vector<String> originalId = new Vector<>();
    private Vector<String> originalLangtext = new Vector<>();

    private Vector<BigDecimal> zeilenGesamt = new Vector<>();
    private BigDecimal rechnungGesamt = BigDecimal.ZERO;

    private HashMap<String, String> hmAdresse = new HashMap<>();
    private String aktRechnung = "";
    private ITextTable textTable;
    private ITextTable textEndbetrag;
    private ITextDocument textDocument;
    private int aktuellePosition;
    private int patKilometer;

    private StringBuffer writeBuf = new StringBuffer();
    private StringBuffer rechnungBuf = new StringBuffer();

    private int preisregel;
    private boolean wechselcheck;

    private int[] splitpreise = { 0, 0 };
    /** Alle alt, alle neu, splitten. */
    private boolean[] preisanwenden = { false, false, false };
    private Vector<Integer> hbvec = new Vector<>();
    private Vector<Integer> kmvec = new Vector<>();

    private String hatAbweichendeAdresse;

    private String patid;

    private Vector<String> vecaktrez;
    private Vector<String> patDaten;

    private String aktIk;

    private String privatRgFormular;

    private HashMap<String, String> hmAbrechnung;

    private JRtaRadioButton privatRechnungBtn;

    public AbrechnungPrivat(JXFrame owner, String titel, int preisgruppe) {
        this(owner, titel, preisgruppe, (JComponent) Reha.getThisFrame()
                                                                    .getGlassPane(), Reha.instance.patpanel.vecaktrez.get(1),
                SystemPreislisten.hmPreise.get(RezTools.getDisziplinFromRezNr(Reha.instance.patpanel.vecaktrez.get(1)))
                                          .get(preisgruppe - 1),
                Reha.instance.patpanel.patDaten.get(5),
                Reha.instance.patpanel.patDaten.get(66), Reha.instance.patpanel.vecaktrez,
                Reha.instance.patpanel.patDaten, Betriebsumfeld.getAktIK(), SystemConfig.hmAbrechnung.get("hmpriformular"),
                SystemConfig.hmAbrechnung, SystemPreislisten.hmPreisGruppen.get(StringTools.getDisziplin(Reha.instance.patpanel.vecaktrez.get(1))));
    }

    AbrechnungPrivat(JXFrame owner, String titel, int preisgruppe, JComponent glasspane, String rezeptNr,
            Vector<Vector<String>> preisliste, String hatAbweichendeAdresse, String patientenDbID, Vector<String> aktuellesRezeptVector,
            Vector<String> aktuellerPatientDaten, String aktIk, String privatRgFormular,
            HashMap<String, String> hmAbrechnung,  Vector<String> preisgruppenFuerDiszi) {
        super(owner, glasspane);
        this.hmAbrechnung = hmAbrechnung;
        this.privatRgFormular = privatRgFormular;
        patDaten = aktuellerPatientDaten;
        vecaktrez = aktuellesRezeptVector;
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

        jcmb = new JRtaComboBox(preisgruppenFuerDiszi);
        jcmb.setSelectedIndex(this.preisgruppe - 1);
        this.aktGruppe = this.preisgruppe - 1;
        jcmb.setActionCommand("neuertarif");
        jcmb.addActionListener(neuerTarifAL);
        pan.add(jcmb, cc.xy(3, 8));
        privatRechnungBtn = new JRtaRadioButton("Formular für Privatrechnung verwenden");
        privatRechnungBtn.addChangeListener(cl);
        pan.add(privatRechnungBtn, cc.xy(3, 10));
        JRtaRadioButton kostentraegerBtn = new JRtaRadioButton("Formular für Kostenträger Rechnung verwenden");
        kostentraegerBtn.addChangeListener(cl);
        pan.add(kostentraegerBtn, cc.xy(3, 12));
        bg.add(privatRechnungBtn);
        bg.add(kostentraegerBtn);

        if (preisgruppe == 4) {
            kostentraegerBtn.setSelected(true);
            regleBGE();
        } else {
            privatRechnungBtn.setSelected(true);
            reglePrivat();
        }

        if (!"0".equals(vecaktrez.get(8))) {
            labs[0] = new JLabel();
            labs[0].setForeground(Color.BLUE);
            pan.add(labs[0], cc.xy(3, 14));
        }
        if (!"0".equals(vecaktrez.get(9))) {
            labs[1] = new JLabel();
            labs[1].setForeground(Color.BLUE);
            pan.add(labs[1], cc.xy(3, 16));
        }
        if (!"0".equals(vecaktrez.get(10))) {
            labs[2] = new JLabel();
            labs[2].setForeground(Color.BLUE);
            pan.add(labs[2], cc.xy(3, 18));
        }
        if (!"0".equals(vecaktrez.get(11))) {
            labs[3] = new JLabel();
            labs[3].setForeground(Color.BLUE);
            pan.add(labs[3], cc.xy(3, 20));
        }
        // Mit Hausbesuch
        if ("T".equals(vecaktrez.get(43))) {
            // Hausbesuch voll (Einzeln) abrechnen
            hausBesuch = true;
            if ("T".equals(vecaktrez.get(61))) {
                hbEinzeln = true;
            }
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

        JButton okBtn = macheBut("Ok", "ok");
        pan.add(okBtn, cc.xy(3, 3));
        okBtn.addKeyListener(kl);

        JButton korrekturBtn = macheBut("Korrektur", "korrektur");
        pan.add(korrekturBtn, cc.xy(5, 3));
        korrekturBtn.addKeyListener(kl);

        JButton abbrechnenBtn = macheBut("abbrechen", "abbrechen");
        pan.add(abbrechnenBtn, cc.xy(7, 3));
        abbrechnenBtn.addKeyListener(kl);

        return pan;
    }

    private JButton macheBut(String titel, String cmd) {
        JButton but = new JButton(titel);
        but.setName(cmd);
        but.setActionCommand(cmd);
        but.addActionListener(al);
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

            if (!hatPatientAbweichendeAdresse()) {
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
            } else {
                String[] adressParams = AdressTools.holeAbweichendeAdresse(patDaten.get(66));
                hmAdresse.put("<pri1>", adressParams[0]);
                hmAdresse.put("<pri2>", adressParams[1]);
                hmAdresse.put("<pri3>", adressParams[2]);
                hmAdresse.put("<pri4>", adressParams[3]);
                hmAdresse.put("<pri5>", adressParams[4]);
            }
            aktRechnung = Integer.toString(SqlInfo.erzeugeNummer("rnr"));
            hmAdresse.put("<pri6>", aktRechnung);

            System.out.println(Path.Instance.getProghome() + "vorlagen\\" + aktIk + "\\" + privatRgFormular);
            starteDokument(Path.Instance.getProghome() + "vorlagen\\" + aktIk + "\\" + privatRgFormular);
            starteErsetzen();
            startePositionen();

            starteDrucken();

            if (Reha.vollbetrieb) {
                doFaktura("privat");

                doOffenePosten("privat");

                doUebertrag();
            }
            doTabelle();
        } catch (Exception e) {
            e.printStackTrace();
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

            starteDokument(Path.Instance.getProghome() + "vorlagen/" + Betriebsumfeld.getAktIK() + "/"
                    + SystemConfig.hmAbrechnung.get("hmbgeformular"));
            starteErsetzen();
            startePositionen();

            starteDrucken();

            if (Reha.vollbetrieb) {
                doFaktura("bge");

                doOffenePosten("bge");

                doUebertrag();
            }
            doTabelle();
        } catch (Exception e) {
            e.printStackTrace();
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
                    .append(vecaktrez.get(0))
                    .append("', ");
            writeBuf.append("kassid='")
                    .append(vecaktrez.get(37))
                    .append("', ");
            writeBuf.append("arztid='")
                    .append(vecaktrez.get(16))
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
                   .append(vecaktrez.get(37))
                   .append("',");
        rechnungBuf.append("pat_intern='")
                   .append(vecaktrez.get(0))
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
            if ("T".equals(vecaktrez.get(62)
                                    .trim())) {
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
        preisliste = SystemPreislisten.hmPreise.get(this.disziplin)
                                               .get(this.aktGruppe);
        try {
            preisdatum = SystemPreislisten.hmNeuePreiseAb.get(this.disziplin)
                                                         .get(this.aktGruppe);
            if (preisdatum ==null || "".equals(preisdatum)) {
                preisregel = 0;
                wechselcheck = false;
            } else {
                preisregel = SystemPreislisten.hmNeuePreiseRegel.get(this.disziplin)
                                                                .get(this.aktGruppe);
            }
            tage = RezTools.holeEinzelTermineAusRezept(null, vecaktrez.get(34));
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
                } else {
                    preisanwenden[0] = false;
                    preisanwenden[1] = true;
                }
                preisanwenden[2] = false;
            } else if (preisregel == 2 && wechselcheck) {
                // Rezeptdatum
                if (DatFunk.TageDifferenz(preisdatum, DatFunk.sDatInDeutsch(vecaktrez.get(2))) < 0) {
                    preisanwenden[0] = true;
                    preisanwenden[1] = false;
                } else {
                    preisanwenden[0] = false;
                    preisanwenden[1] = true;
                }
                preisanwenden[2] = false;
            } else if (preisregel == 3 && wechselcheck) {
                // beliebige Behandlung
                preisanwenden[0] = true;
                preisanwenden[1] = false;
                preisanwenden[2] = false;
                for (int i = 0; i < tage.size(); i++) {
                    if (DatFunk.TageDifferenz(preisdatum, tage.get(i)) >= 0) {
                        preisanwenden[0] = false;
                        preisanwenden[1] = true;
                        preisanwenden[2] = false;
                        break;
                    }
                }
            } else if (preisregel == 4 && wechselcheck) {
                int max = Integer.parseInt(vecaktrez.get(3));
                // splitten
                preisanwenden[0] = false;
                preisanwenden[1] = false;
                preisanwenden[2] = true;
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
                } else if (splitpreise[1] == max) {
                    preisanwenden[0] = false;
                    preisanwenden[1] = true;
                    preisanwenden[2] = false;
                } else if ((splitpreise[0] != 0 && splitpreise[1] != 0) || wechselcheck) {
                    doNeuerTarifMitSplitting();
                    if (hausBesuch) {
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
                    } else {
                        preisanwenden[0] = false;
                        preisanwenden[1] = true;
                    }
                    preisanwenden[2] = false;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            wechselcheck = false;
            preisregel = 0;
        }

        // Änderungen in Preis
        Integer aktanzahl = (Integer) RezTools.holeTermineAnzahlUndLetzter(vecaktrez.get(34))[0];
        if (!"0".equals(vecaktrez.get(8))) {
            anzahl = vecaktrez.get(3);
            if (Integer.parseInt(anzahl) > 1) {
                anzahl = Integer.toString(aktanzahl);
            }

            originalPos.add(vecaktrez.get(48));
            originalId.add(vecaktrez.get(8));
            originalAnzahl.add(Integer.parseInt(anzahl));
            originalLangtext.add(RezTools.getLangtextFromID(vecaktrez.get(8), "", preisliste)
                                         .replace("30Min.", "")
                                         .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(vecaktrez.get(8), preisliste);
            if (preisanwenden[0]) {
                preis = RezTools.getPreisAltFromID(vecaktrez.get(8), "", preisliste);
            } else {
                preis = RezTools.getPreisAktFromID(vecaktrez.get(8), "", preisliste);
            }

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preis));
                labs[0].setText(anzahl + " * " + pos + " (Einzelpreis = " + preis + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[0].setText(anzahl + " * " + pos + " (Einzelpreis = 0.00)");
            }
        }
        if (!"0".equals(vecaktrez.get(9))) {
            anzahl = vecaktrez.get(4);
            if (Integer.parseInt(anzahl) > 1) {
                anzahl = Integer.toString(aktanzahl);
            }

            originalPos.add(vecaktrez.get(49));
            originalId.add(vecaktrez.get(9));
            originalAnzahl.add(Integer.parseInt(anzahl));
            originalLangtext.add(RezTools.getLangtextFromID(vecaktrez.get(9), "", preisliste)
                                         .replace("30Min.", "")
                                         .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(vecaktrez.get(9), preisliste);

            if (preisanwenden[0]) {
                preis = RezTools.getPreisAltFromID(vecaktrez.get(9), "", preisliste);
            } else {
                preis = RezTools.getPreisAktFromID(vecaktrez.get(9), "", preisliste);
            }
            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preis));
                labs[1].setText(anzahl + " * " + pos + " (Einzelpreis = " + preis + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[1].setText(anzahl + " * " + pos + " (Einzelpreis = 0.00)");
            }
        }
        if (!"0".equals(vecaktrez.get(10))) {
            anzahl = vecaktrez.get(5);
            if (Integer.parseInt(anzahl) > 1) {
                anzahl = Integer.toString(aktanzahl);
            }

            originalPos.add(vecaktrez.get(50));
            originalId.add(vecaktrez.get(10));
            originalAnzahl.add(Integer.parseInt(anzahl));
            originalLangtext.add(RezTools.getLangtextFromID(vecaktrez.get(10), "", preisliste)
                                         .replace("30Min.", "")
                                         .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(vecaktrez.get(10), preisliste);

            if (preisanwenden[0]) {
                preis = RezTools.getPreisAltFromID(vecaktrez.get(10), "", preisliste);
            } else {
                preis = RezTools.getPreisAktFromID(vecaktrez.get(10), "", preisliste);
            }

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preis));
                labs[2].setText(anzahl + " * " + pos + " (Einzelpreis = " + preis + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[2].setText(anzahl + " * " + pos + " (Einzelpreis = 0.00)");
            }
        }
        if (!"0".equals(vecaktrez.get(11))) {
            anzahl = vecaktrez.get(6);
            if (Integer.parseInt(anzahl) > 1) {
                anzahl = Integer.toString(aktanzahl);
            }

            originalPos.add(vecaktrez.get(51));
            originalId.add(vecaktrez.get(11));
            originalAnzahl.add(Integer.parseInt(anzahl));
            originalLangtext.add(RezTools.getLangtextFromID(vecaktrez.get(11), "", preisliste)
                                         .replace("30Min.", "")
                                         .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(vecaktrez.get(11), preisliste);

            if (preisanwenden[0]) {
                preis = RezTools.getPreisAltFromID(vecaktrez.get(11), "", preisliste);
            } else {
                preis = RezTools.getPreisAktFromID(vecaktrez.get(11), "", preisliste);
            }

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preis));
                labs[3].setText(anzahl + " * " + pos + " (Einzelpreis = " + preis + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[3].setText(anzahl + " * " + pos + " (Einzelpreis = 0.00)");
            }
        }
        if (hausBesuch) {
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
        this.aktGruppe = jcmb.getSelectedIndex();
        labs[5].setText("");
        /* Hausbesuch voll abrechnen */
        int hbanzahl = (Integer) RezTools.holeTermineAnzahlUndLetzter(vecaktrez.get(34))[0];

        if (this.hbEinzeln) {
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
        int testanzahl = Integer.parseInt(vecaktrez.get(3));
        if (testanzahl != splitpreise[0] + splitpreise[1]) {
            JOptionPane.showMessageDialog(null,
                    "Die Anwendungsregel dieser Tarifgruppe ist Splitting!!!\nBei dieser Regel müssen die Behandlungstage mit der Anzahl der Behandlungen im Rezept übereinstimmen!");
            return;
        }
        // Änderungen in Preis und ggfls. in Anzahl
        // Benötigt werde: Anzahlen auf dem Rezept, Anzahl alter Preis, Anzahl neuer
        // Preis.
        if (!"0".equals(vecaktrez.get(8))) {
            originalPos.add(vecaktrez.get(48));
            originalId.add(vecaktrez.get(8));
            // jetzt Anzahlen für alter Preis
            originalAnzahl.add(splitpreise[0]);
            originalLangtext.add(RezTools.getLangtextFromID(vecaktrez.get(8), "", preisliste)
                                         .replace("30Min.", "")
                                         .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(vecaktrez.get(8), preisliste);
            anzahlAlt = Integer.toString(splitpreise[0]);

            preisAlt = RezTools.getPreisAltFromID(vecaktrez.get(8), "", preisliste);

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preisAlt));
                labs[0].setText(anzahlAlt + " * " + pos + " (Einzelpreis = " + preisAlt + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[0].setText(anzahlAlt + " * " + pos + " (Einzelpreis = 0.00)");
            }
            // jetzt Anzahlen für neuer Preis
            originalPos.add(vecaktrez.get(48));
            originalId.add(vecaktrez.get(8));
            originalAnzahl.add(splitpreise[1]);
            originalLangtext.add(RezTools.getLangtextFromID(vecaktrez.get(8), "", preisliste)
                                         .replace("30Min.", "")
                                         .replace("45Min.", ""));
            pos = RezTools.getKurzformFromID(vecaktrez.get(8), preisliste);
            anzahlNeu = Integer.toString(splitpreise[1]);
            preisNeu = RezTools.getPreisAktFromID(vecaktrez.get(8), "", preisliste);

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preisNeu));
                labs[0].setText(
                        labs[0].getText() + " / " + anzahlNeu + " * " + pos + " (Einzelpreis = " + preisNeu + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[0].setText(labs[0].getText() + " / " + anzahlNeu + " * " + pos + " (Einzelpreis = 0.00)");
            }
        }
        if (!"0".equals(vecaktrez.get(9))) {
            originalPos.add(vecaktrez.get(49));
            originalId.add(vecaktrez.get(9));
            test = Integer.parseInt(vecaktrez.get(4));
            originalAnzahl.add(splitpreise[0] > test ? test : splitpreise[0]);
            originalLangtext.add(RezTools.getLangtextFromID(vecaktrez.get(9), "", preisliste)
                                         .replace("30Min.", "")
                                         .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(vecaktrez.get(9), preisliste);
            anzahlAlt = Integer.toString(splitpreise[0] > test ? test : splitpreise[0]);
            preisAlt = RezTools.getPreisAltFromID(vecaktrez.get(9), "", preisliste);

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preisAlt));
                labs[1].setText(anzahlAlt + " * " + pos + " (Einzelpreis = " + preisAlt + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[1].setText(anzahlAlt + " * " + pos + " (Einzelpreis = 0.00)");
            }
            // nur wenn die angegebene Anzahl < ist als Anzahl Tage im Rezeptblatt
            if (splitpreise[0] < test) {
                originalPos.add(vecaktrez.get(49));
                originalId.add(vecaktrez.get(9));

                originalAnzahl.add(test - splitpreise[0]);
                originalLangtext.add(RezTools.getLangtextFromID(vecaktrez.get(9), "", preisliste)
                                             .replace("30Min.", "")
                                             .replace("45Min.", ""));
                pos = RezTools.getKurzformFromID(vecaktrez.get(9), preisliste);
                anzahlNeu = Integer.toString(test - splitpreise[0]);
                preisNeu = RezTools.getPreisAktFromID(vecaktrez.get(9), "", preisliste);

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
        if (!"0".equals(vecaktrez.get(10))) {
            originalPos.add(vecaktrez.get(50));
            originalId.add(vecaktrez.get(10));
            test = Integer.parseInt(vecaktrez.get(5));
            originalAnzahl.add(splitpreise[0] > test ? test : splitpreise[0]);
            originalLangtext.add(RezTools.getLangtextFromID(vecaktrez.get(10), "", preisliste)
                                         .replace("30Min.", "")
                                         .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(vecaktrez.get(10), preisliste);
            test = Integer.parseInt(vecaktrez.get(5));
            anzahlAlt = Integer.toString(splitpreise[0] > test ? test : splitpreise[0]);
            preisAlt = RezTools.getPreisAltFromID(vecaktrez.get(10), "", preisliste);

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preisAlt));
                labs[2].setText(anzahlAlt + " * " + pos + " (Einzelpreis = " + preisAlt + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[2].setText(anzahlAlt + " * " + pos + " (Einzelpreis = 0.00)");
            }
            if (splitpreise[0] < test) {
                originalPos.add(vecaktrez.get(50));
                originalId.add(vecaktrez.get(10));
                originalAnzahl.add(test - splitpreise[0]);
                originalLangtext.add(RezTools.getLangtextFromID(vecaktrez.get(10), "", preisliste)
                                             .replace("30Min.", "")
                                             .replace("45Min.", ""));
                anzahlNeu = Integer.toString(test - splitpreise[0]);
                preisNeu = RezTools.getPreisAktFromID(vecaktrez.get(10), "", preisliste);
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
        if (!"0".equals(vecaktrez.get(11))) {
            originalPos.add(vecaktrez.get(51));
            originalId.add(vecaktrez.get(11));
            test = Integer.parseInt(vecaktrez.get(6));
            originalAnzahl.add(splitpreise[0] > test ? test : splitpreise[0]);
            originalLangtext.add(RezTools.getLangtextFromID(vecaktrez.get(11), "", preisliste)
                                         .replace("30Min.", "")
                                         .replace("45Min.", ""));

            pos = RezTools.getKurzformFromID(vecaktrez.get(11), preisliste);
            anzahlAlt = Integer.toString(splitpreise[0] > test ? test : splitpreise[0]);
            preisAlt = RezTools.getPreisAltFromID(vecaktrez.get(11), "", preisliste);

            if (!"".equals(pos.trim())) {
                einzelPreis.add(Double.parseDouble(preisAlt));
                labs[3].setText(anzahlAlt + " * " + pos + " (Einzelpreis = " + preisAlt + ")");
            } else {
                JOptionPane.showMessageDialog(null, "Die Rezeptpositionen sind in dieser preisgruppe nicht vorhanden");
                labs[3].setText(anzahlAlt + " * " + pos + " (Einzelpreis = 0.00)");
            }
            if (splitpreise[0] < test) {
                originalPos.add(vecaktrez.get(51));
                originalId.add(vecaktrez.get(11));
                originalAnzahl.add(test - splitpreise[0]);
                originalLangtext.add(RezTools.getLangtextFromID(vecaktrez.get(11), "", preisliste)
                                             .replace("30Min.", "")
                                             .replace("45Min.", ""));
                anzahlNeu = Integer.toString(test - splitpreise[0]);
                preisNeu = RezTools.getPreisAktFromID(vecaktrez.get(11), "", preisliste);
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
        this.aktGruppe = jcmb.getSelectedIndex();
        labs[5].setText("");

        /* Hausbesuch voll abrechnen */
        int hbanzahl = Integer.parseInt(vecaktrez.get(64));
        int althb = -1;
        int neuhb = -1;
        String preisAlt = "";
        String preisNeu = "";

        if (this.hbEinzeln) {
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

    private void doKorrektur() {
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
                                        .trim().isEmpty() ? " " : hmAdresse.get("<pri1>"));
        adr2.setText(hmAdresse.get("<pri2>")
                                        .trim().isEmpty() ? " " : hmAdresse.get("<pri2>"));
    }

    private ActionListener neuerTarifAL = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            aktGruppe = jcmb.getSelectedIndex();
            doNeuerTarif();

        }
    };

    private ActionListener al = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            String cmd = arg0.getActionCommand();
            if ("privatadresse".equals(cmd)) {
                reglePrivat();
                return;
            }
            if ("kassendresse".equals(cmd)) {
                regleBGE();
                return;
            }
            if ("neuertarif".equals(cmd)) {
                aktGruppe = jcmb.getSelectedIndex();
                doNeuerTarif();
                return;
            }
            if ("korrektur".equals(cmd)) {
                rueckgabe = KORREKTUR;
                // doKorrektur();
                fensterSchliessen();
                return;
            }
            if ("abbrechen".equals(cmd)) {
                rueckgabe = ABBRECHEN;
                fensterSchliessen();
            }
            if ("ok".equals(cmd)) {
                rueckgabe = OK;
                doRgRechnungPrepare();
            }
        }
    };

    private KeyListener kl = new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent arg0) {
            if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                rueckgabe = ABBRECHEN;
                fensterSchliessen();
                return;
            }
            if (arg0.getKeyCode() == KeyEvent.VK_ENTER && (JComponent) arg0.getSource() instanceof JButton) {
                if ("abbrechen".equals(((JComponent) arg0.getSource()).getName())) {
                    rueckgabe = ABBRECHEN;
                    fensterSchliessen();
                } else if ("korrektur".equals(((JComponent) arg0.getSource()).getName())) {
                    doKorrektur();
                } else if ("ok".equals(((JComponent) arg0.getSource()).getName())) {
                    rueckgabe = OK;
                    doRgRechnungPrepare();
                }
            }
        }

          };

    private void fensterSchliessen() {
        setVisible(false);
        dispose();
    }

    private void starteDokument(String url) throws Exception {
        IDocumentService documentService;
        documentService = new OOService().getOfficeapplication()
                                         .getDocumentService();
        IDocumentDescriptor docdescript = new DocumentDescriptor();
        docdescript.setHidden(true);
        docdescript.setAsTemplate(true);
        IDocument document;
        document = documentService.loadDocument(url, docdescript);
        textDocument = (ITextDocument) document;
        if (privatRechnungBtn.isSelected()) {
            OOTools.druckerSetzen(textDocument, SystemConfig.hmAbrechnung.get("hmpridrucker"));
        } else {
            OOTools.druckerSetzen(textDocument, SystemConfig.hmAbrechnung.get("hmbgedrucker"));
        }
        textTable = textDocument.getTextTableService()
                                .getTextTable("Tabelle1");
        textEndbetrag = textDocument.getTextTableService()
                                    .getTextTable("Tabelle2");
    }

    private void starteErsetzen() {
        ITextFieldService textFieldService = textDocument.getTextFieldService();
        ITextField[] placeholders = null;
        try {
            placeholders = textFieldService.getPlaceholderFields();
        } catch (TextException e) {
            e.printStackTrace();
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
                Set<?> entries = SystemConfig.hmAdrRDaten.entrySet();
                Iterator<?> it = entries.iterator();
                while (it.hasNext()) {
                    Map.Entry<?, ?> entry = (Map.Entry<?, ?>) it.next();
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

    private void startePositionen() throws TextException {
        aktuellePosition++;
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

    private synchronized void starteDrucken() {
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
