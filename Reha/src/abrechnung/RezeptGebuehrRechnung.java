package abrechnung;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTitledPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.therapi.updater.Version;
import org.therapi.reha.patient.AktuelleRezepte;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.DatFunk;
import CommonTools.JRtaTextField;
import CommonTools.SqlInfo;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.DocumentException;
import ag.ion.bion.officelayer.document.IDocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextFieldService;
import ag.ion.bion.officelayer.text.ITextTable;
import ag.ion.bion.officelayer.text.TextException;
import ag.ion.noa.NOAException;
import ag.ion.noa.internal.printing.PrintProperties;
import dialoge.DragWin;
import dialoge.PinPanel;
import environment.Path;
import events.RehaTPEvent;
import events.RehaTPEventClass;
import events.RehaTPEventListener;
import gui.Cursors;
import hauptFenster.Reha;
import office.OOService;
import office.OOTools;
import stammDatenTools.ZuzahlTools.ZZStat;
import systemEinstellungen.SystemConfig;
import umfeld.Betriebsumfeld;

public class RezeptGebuehrRechnung extends JXDialog implements ActionListener, KeyListener, RehaTPEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RezeptGebuehrRechnung.class);
    private static final long serialVersionUID = 7491942845791659861L;
    private JXTitledPanel jtp;
    private MouseAdapter mymouse;
    private PinPanel pinPanel;
    private JXPanel content;
    private RehaTPEventClass rtp;

    private JRtaTextField rezNRTxtField;
    private JRtaTextField rgDatumTxtField;
    private JRtaTextField rgBetragTextField;
    private JRtaTextField rgPauschaleTxtField;
    private JRtaTextField rgBehandlungTextfield;
    private Map<String, String> hmRezgeb = new HashMap<>();
    private DecimalFormat dcf = new DecimalFormat("#########0.00");

    private boolean sollGebuchtwerden;

    public RezeptGebuehrRechnung(JXFrame owner, String titel, int rueckgabe, Map<String, String> hmRezgeb,
            boolean auchbuchen) {
        this(owner, titel, rueckgabe, hmRezgeb, auchbuchen, Reha.getThisFrame()
                                                                .getGlassPane());
        start(); // XXX: don't start things in constructor
    }

    public RezeptGebuehrRechnung(JXFrame owner, String titel, int rueckgabe, Map<String, String> hmRezgeb,
            boolean auchbuchen, Component glassPane) {
        super(owner, (JComponent) glassPane);
        setUndecorated(true);
        setName("RezgebDlg");
        this.hmRezgeb = hmRezgeb;
        try {
            RGRData data = new RGRData(hmRezgeb);
            LOGGER.debug(data.toString());
        } catch (Exception e) {
            if (new Version().isTestVersion()) {
                Set<Entry<String, String>> entries = hmRezgeb.entrySet();
                    for (Entry<String, String> entry : entries) {
                        LOGGER.error("key:" +entry.getKey() + " value:" +entry.getValue());
                    }

            }
            LOGGER.error("couldn't set data for rgr" , e);
        }

        this.sollGebuchtwerden = auchbuchen;
        this.jtp = new JXTitledPanel();
        this.jtp.setName("RezgebDlg");
        this.mymouse = new DragWin(this);
        this.jtp.addMouseListener(mymouse);
        this.jtp.addMouseMotionListener(mymouse);
        this.jtp.setContentContainer(getContent());
        this.jtp.setTitleForeground(Color.WHITE);
        this.jtp.setTitle(titel);
        this.pinPanel = new PinPanel();
        this.pinPanel.getGruen()
                     .setVisible(false);
        this.pinPanel.setName("RezgebDlg");
        this.jtp.setRightDecoration(this.pinPanel);
        setContentPane(jtp);
        setModal(true);
        setResizable(false);
        this.rtp = new RehaTPEventClass();
        this.rtp.addRehaTPEventListener(this);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    }

    public void start() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (sollGebuchtwerden) {
                    setzeFelderMitBuchung();
                } else {
                    setzeFelderOhneBuchung();
                }
            }
        });

        setzeFocusaufRgPauschaleTF();

    }

    private void setzeFocusaufRgPauschaleTF() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                rgPauschaleTxtField.requestFocus();
            }
        });
    }

    private void setzeFelderMitBuchung() {

        rezNRTxtField.setText(hmRezgeb.get("<rgreznum>"));
        rgDatumTxtField.setText(hmRezgeb.get("<rgdatum>"));
        rgBetragTextField.setText(hmRezgeb.get("<rgbetrag>"));
        rgPauschaleTxtField.setText(hmRezgeb.get("<rgpauschale>"));
        rgBehandlungTextfield.setText(hmRezgeb.get("<rgbehandlung>"));
    }

    private void setzeFelderOhneBuchung() {
        try {
            Vector<Vector<String>> vec = holeausFaktura();
            if (vec.isEmpty()) {
                showUserMessage("Diese Rezeptgebührrechnung ist nicht in der Tabelle rgaffaktura erfaßt");
                return;
            }

            rezNRTxtField.setText(vec.get(0)
                                     .get(0));
            rgDatumTxtField.setText(DatFunk.sDatInDeutsch(vec.get(0)
                                                             .get(1)));
            rgBetragTextField.setText(vec.get(0)
                                         .get(2));
            rgPauschaleTxtField.setText(vec.get(0)
                                           .get(3));
            rgBehandlungTextfield.setText(hmRezgeb.get("<rgbehandlung>"));
        } catch (Exception ex) {
            LOGGER.error("Fehler beim Bezug der Daten fuer RGR" + hmRezgeb.get("<rgreznum>"), ex);
            showUserMessage("Fehler beim Bezug der Daten für Rezeptgebührrechnung");
        }
    }

    private Vector<Vector<String>> holeausFaktura() {
        String rezeptNummer = hmRezgeb.get("<rgreznum>");
        String cmd = "select reznr,rdatum,rgbetrag,rpbetrag from rgaffaktura where reznr='" + rezeptNummer
                + "' and rnr like 'RGR-%' LIMIT 1";
        return SqlInfo.holeFelder(cmd);
    }

    protected void showUserMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    private JXPanel getContent() {
        content = new JXPanel(new BorderLayout());
        content.add(getFields(), BorderLayout.CENTER);
        content.add(getButtons(), BorderLayout.SOUTH);
        content.addKeyListener(this);
        return content;
    }

    private JXPanel getFields() {
        JXPanel pan = new JXPanel();
        // 1 2 3 4 5 6 7
        FormLayout lay = new FormLayout("5dlu,fill:0:grow(0.5),right:max(40dlu;p),5dlu,60dlu,fill:0:grow(0.5),5dlu",
                // 1 2 3 4 5 6 7 8 9 10 11 12 13
                "5dlu,fill:0:grow(0.5),p,3dlu,p,3dlu,p,3dlu,p,3dlu, p,fill:0:grow(0.5),5dlu");
        pan.setLayout(lay);
        CellConstraints cc = new CellConstraints();
        pan.setOpaque(false);

        pan.add(new JLabel("Rezeptnummer"), cc.xy(3, 3));
        rezNRTxtField = new JRtaTextField("GROSS", true);
        pan.add(rezNRTxtField, cc.xy(5, 3));
        pan.add(new JLabel("Rezeptdatum"), cc.xy(3, 5));
        rgDatumTxtField = new JRtaTextField("DATUM", true);
        pan.add(rgDatumTxtField, cc.xy(5, 5));
        JLabel rezeptgebLbl = new JLabel("Rezeptgebühr");
        rezeptgebLbl.setForeground(Color.RED);
        pan.add(rezeptgebLbl, cc.xy(3, 7));
        rgBetragTextField = new JRtaTextField("FL", true, "6.2", "RECHTS");
        rgBetragTextField.setupFormat(2);
        rgBetragTextField.setDValueFromS("0,00");
        pan.add(rgBetragTextField, cc.xy(5, 7));
        JLabel bearbeitungsGebLbl = new JLabel("Bearbeitungsgebühr");
        bearbeitungsGebLbl.setForeground(Color.RED);
        pan.add(bearbeitungsGebLbl, cc.xy(3, 9));
        rgPauschaleTxtField = new JRtaTextField("FL", true, "6.2", "RECHTS");
        rgPauschaleTxtField.setDValueFromS("0,00");
        rgPauschaleTxtField.setupFormat(2);
        pan.add(rgPauschaleTxtField, cc.xy(5, 9));
        pan.add(new JLabel("Behandlungen"), cc.xy(3, 11));
        rgBehandlungTextfield = new JRtaTextField("NIX", true);
        pan.add(rgBehandlungTextfield, cc.xy(5, 11));
        return pan;
    }

    private JXPanel getButtons() {
        JXPanel pan = new JXPanel();
        pan.setOpaque(false);
        FormLayout lay = new FormLayout("5dlu,fill:0:grow(0.5),50dlu,10dlu,50dlu,fill:0:grow(0.5),5dlu",
                // 1 2 3 4 5 6 7 8 9 10 11 12
                "5dlu,fill:0:grow(0.5),p,fill:0:grow(0.5),5dlu");
        pan.setLayout(lay);
        CellConstraints cc = new CellConstraints();
        JButton okButton = macheBut("Ok", "ok", e -> doRgRechnungPrepare());
        pan.add(okButton, cc.xy(3, 3));
        JButton abbrechenButton = macheBut("abbrechen", "abbrechen", e -> fensterSchliessen("dieses"));
        pan.add(abbrechenButton, cc.xy(5, 3));
        return pan;
    }

    private JButton macheBut(String titel, String cmd, ActionListener listener) {
        JButton but = new JButton(titel);
        but.setName(cmd);
        but.setActionCommand(cmd);
        but.addActionListener(listener);
        return but;
    }

    private void doRgRechnungPrepare() {
        double rezgeb = Double.parseDouble(rgBetragTextField.getText()
                                                            .replace(",", "."))
                + Double.parseDouble(rgPauschaleTxtField.getText()
                                                        .replace(",", "."));
        hmRezgeb.put("<rggesamt>", dcf.format(rezgeb));
        hmRezgeb.put("<rgbetrag>", dcf.format(Double.parseDouble(rgBetragTextField.getText()
                                                                                  .replace(",", "."))));
        hmRezgeb.put("<rgpauschale>", dcf.format(Double.parseDouble(rgPauschaleTxtField.getText()
                                                                                       .replace(",", "."))));
        if (sollGebuchtwerden) {
            hmRezgeb.put("<rgnr>", "RGR-" + SqlInfo.erzeugeNummer("rgrnr"));
        } else {
            hmRezgeb.put("<rgnr>", SqlInfo.holeEinzelFeld("select rnr from rgaffaktura where reznr='"
                    + hmRezgeb.get("<rgreznum>") + "' and rnr like 'RGR-%' LIMIT 1"));
        }
        hmRezgeb.put("<rgbehandlung>", rgBehandlungTextfield.getText()
                                                            .trim());
        String url = Path.Instance.getProghome() + "vorlagen/" + Betriebsumfeld.getAktIK() + "/RezeptgebuehrRechnung.ott";
        try {
            officeStarten(url);
        } catch (OfficeApplicationException | NOAException | TextException | DocumentException e) {
            e.printStackTrace();
        }
        if (sollGebuchtwerden) {
            buchungStarten();
            setzeZuhzahlungsimageInAktuelleRezepte();
        } else {
            buchungUpdaten();
        }
        fensterSchliessen("dieses");
    }

    protected void setzeZuhzahlungsimageInAktuelleRezepte() {
        AktuelleRezepte.setZuzahlImageActRow(ZZStat.ZUZAHLRGR, hmRezgeb.get("<rgreznum>"));
    }

    private void buchungStarten() {
        String sqlCommand = createInsertStmt();
        sqlausfuehren(sqlCommand);
    }

    private String createInsertStmt() {
        StringBuilder buf = new StringBuilder();
        buf.append("insert into rgaffaktura set ");
        buf.append("rnr='")
           .append(hmRezgeb.get("<rgnr>"))
           .append("', ");
        buf.append("reznr='")
           .append(hmRezgeb.get("<rgreznum>"))
           .append("', ");
        buf.append("pat_intern='")
           .append(hmRezgeb.get("<rgpatintern>"))
           .append("', ");
        buf.append("rgesamt='")
           .append(hmRezgeb.get("<rggesamt>")
                           .replace(",", "."))
           .append("', ");
        buf.append("roffen='")
           .append(hmRezgeb.get("<rggesamt>")
                           .replace(",", "."))
           .append("', ");
        buf.append("rgbetrag='")
           .append(hmRezgeb.get("<rgbetrag>")
                           .replace(",", "."))
           .append("', ");
        buf.append("rpbetrag='")
           .append(hmRezgeb.get("<rgpauschale>")
                           .replace(",", "."))
           .append("', ");
        buf.append("rdatum='")
           .append(DatFunk.sDatInSQL(DatFunk.sHeute()))
           .append("',");
        buf.append("ik='")
           .append(Betriebsumfeld.getAktIK())
           .append("'");
        return buf.toString();
    }

    private void buchungUpdaten() {
        String sqlCommand = createUpdateStmt();
        sqlausfuehren(sqlCommand);
    }

    private String createUpdateStmt() {
        StringBuilder buf = new StringBuilder();
        buf.append("update rgaffaktura set ");
        buf.append("rnr='")
           .append(hmRezgeb.get("<rgnr>"))
           .append("', ");
        buf.append("reznr='")
           .append(hmRezgeb.get("<rgreznum>"))
           .append("', ");
        buf.append("pat_intern='")
           .append(hmRezgeb.get("<rgpatintern>"))
           .append("', ");
        buf.append("rgesamt='")
           .append(hmRezgeb.get("<rggesamt>")
                           .replace(",", "."))
           .append("', ");
        buf.append("roffen='")
           .append(hmRezgeb.get("<rggesamt>")
                           .replace(",", "."))
           .append("', ");
        buf.append("rgbetrag='")
           .append(hmRezgeb.get("<rgbetrag>")
                           .replace(",", "."))
           .append("', ");
        buf.append("rpbetrag='")
           .append(hmRezgeb.get("<rgpauschale>")
                           .replace(",", "."))
           .append("', ");
        buf.append("rdatum='")
           .append(DatFunk.sDatInSQL(DatFunk.sHeute()))
           .append("',");
        buf.append("ik='")
           .append(Betriebsumfeld.getAktIK())
           .append("'");
        buf.append(" where rnr='")
           .append(hmRezgeb.get("<rgnr>"))
           .append("' LIMIT 1");
        return buf.toString();
    }

    protected void sqlausfuehren(String buf) {
        SqlInfo.sqlAusfuehren(buf);
    }

    private synchronized void officeStarten(String url)
            throws OfficeApplicationException, NOAException, TextException, DocumentException {
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);

        IDocumentService documentService = new OOService().getOfficeapplication().getDocumentService();
        IDocumentDescriptor docdescript = new DocumentDescriptor();
        docdescript.setHidden(true);
        docdescript.setAsTemplate(true);
        final ITextDocument textDocument = (ITextDocument) documentService.loadDocument(url, docdescript);
        /**********************/
        OOTools.druckerSetzen(textDocument, SystemConfig.hmAbrechnung.get("hmgkvrechnungdrucker"));
        /**********************/
        ITextFieldService textFieldService = textDocument.getTextFieldService();
        ITextField[] placeholders = textFieldService.getPlaceholderFields();
        String placeholderDisplayText = "";

        Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(hmRezgeb);

        for (ITextField placeHolder : placeholders) {
            placeholderDisplayText = placeHolder.getDisplayText()
                                                .toLowerCase();
            placeHolder.getTextRange()
                       .setText(map.get(placeholderDisplayText));
        }
        try {
            ITextTable[] textTables = textDocument.getTextTableService()
                                                  .getTextTables();
            for (ITextTable iTextTable : textTables) {
                if ("Tabelle-RGR".equals(iTextTable.getName())) {
                    ITextTable textTable = textDocument.getTextTableService()
                                                       .getTextTable("Tabelle-RGR");
                    int anzpos = Integer.parseInt(hmRezgeb.get("<rganzpos>"));
                    for (int position = 1; position <= anzpos; position++) {
                        setCellText(textTable, 0, position, "rglangtext");
                        setCellText(textTable, 1, position, "rganzahl");
                        setCellText(textTable, 2, position, "rggesamt");

                        textTable.addRow(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Fehler in DetailTabelle: ", e);
        }
        boolean immerInOfficeOeffnen = "1".equals(SystemConfig.hmAbrechnung.get("hmallinoffice"));
        if (immerInOfficeOeffnen) {
            textDocument.getFrame()
                        .getXFrame()
                        .getContainerWindow()
                        .setVisible(true);
        } else {
            PrintProperties printprop = new PrintProperties(anzahlKopien());
            textDocument.getPrintService()
                        .print(printprop);

            if (textDocument.isOpen()) {
                textDocument.close();
            }
        }
    }

    private void setCellText(ITextTable textTable, int column, int position, String string) throws TextException {
        textTable.getCell(column, position)
                 .getTextService()
                 .getText()
                 .setText(hmRezgeb.get("<" + string + position + ">"));
    }

    private short anzahlKopien() {
        String anzahlKopien = SystemConfig.hmAbrechnung.get("rgrdruckanzahl");
        int exemplare = 2;
        if (anzahlKopien != null) {
            exemplare = Integer.parseInt(anzahlKopien);
        }
        return (short) exemplare;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        String cmd = arg0.getActionCommand();
        if ("abbrechen".equals(cmd)) {
            fensterSchliessen("dieses");
        } else {
            doRgRechnungPrepare();
        }
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
            fensterSchliessen("dieses");
            return;
        }
        if (arg0.getKeyCode() == KeyEvent.VK_ENTER && (JComponent) arg0.getSource() instanceof JButton) {
            if ("abbrechen".equals(((JComponent) arg0.getSource()).getName())) {
                fensterSchliessen("dieses");
            } else if ("ok".equals(((JComponent) arg0.getSource()).getName())) {
                doRgRechnungPrepare();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    @Override
    public void rehaTPEventOccurred(RehaTPEvent evt) {
        fensterSchliessen("dieses");
    }

    private void fensterSchliessen(String welches) {
        this.jtp.removeMouseListener(this.mymouse);
        this.jtp.removeMouseMotionListener(this.mymouse);
        this.content.removeKeyListener(this);
        this.mymouse = null;
        if (this.rtp != null) {
            this.rtp.removeRehaTPEventListener(this);
            this.rtp = null;
        }
        this.pinPanel = null;
        setVisible(false);
        dispose();
    }
}
