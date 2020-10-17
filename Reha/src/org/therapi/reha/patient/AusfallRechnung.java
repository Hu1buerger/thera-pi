package org.therapi.reha.patient;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.DatFunk;
import CommonTools.JRtaCheckBox;
import CommonTools.SqlInfo;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextFieldService;
import ag.ion.bion.officelayer.text.TextException;
import ag.ion.noa.NOAException;
import dialoge.PinPanel;
import dialoge.RehaSmartDialog;
import environment.Path;
import events.RehaTPEvent;
import events.RehaTPEventClass;
import hauptFenster.Reha;
import office.OOService;
import office.OOTools;
import systemEinstellungen.SystemConfig;
import systemTools.LeistungTools;

public class AusfallRechnung extends RehaSmartDialog implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public JRtaCheckBox[] leistung = { null, null, null, null, null };

    private RehaTPEventClass rtp = null;
    private AusfallRechnungHintergrund rgb;

    public JButton uebernahme;
    public JButton abbrechen;
    public String afrNummer;

    public AusfallRechnung(Point pt) {
        super(null, "AusfallRechnung");

        pinPanel = new PinPanel();
        pinPanel.setName("AusfallRechnung");
        pinPanel.getGruen()
                .setVisible(false);
        setPinPanel(pinPanel);
        getSmartTitledPanel().setTitle("Ausfallrechnung erstellen");

        setSize(300, 270);
        setPreferredSize(new Dimension(300, 270));
        getSmartTitledPanel().setPreferredSize(new Dimension(300, 270));
        setPinPanel(pinPanel);
        rgb = new AusfallRechnungHintergrund();
        rgb.setLayout(new BorderLayout());

        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {

                rgb.setBackgroundPainter(Reha.instance.compoundPainter.get("RezeptGebuehren"));
                return null;
            }

        }.execute();
        rgb.add(getGebuehren(), BorderLayout.CENTER);

        getSmartTitledPanel().setContentContainer(rgb);
        getSmartTitledPanel().getContentContainer()
                             .setName("AusfallRechnung");
        setName("AusfallRechnung");
        setModal(true);
        // Point lpt = new Point(pt.x-125,pt.y+30);
        Point lpt = new Point(pt.x - 150, pt.y + 30);
        setLocation(lpt);

        rtp = new RehaTPEventClass();
        rtp.addRehaTPEventListener(this);

        pack();

    }

    /****************************************************/

    private JPanel getGebuehren() { // 1 2 3 4 5 6 7
        FormLayout lay = new FormLayout("10dlu,fill:0:grow(0.50),right:80dlu,10dlu,80dlu,fill:0:grow(0.50),10dlu",
                // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
                "15dlu,p,10dlu,p,2dlu,p,2dlu,p,2dlu,p, 10dlu, p,  20dlu, p ,20dlu");
        PanelBuilder pb = new PanelBuilder(lay);
        CellConstraints cc = new CellConstraints();

        pb.getPanel()
          .setOpaque(false);

        pb.addLabel("Bitte die Positionen auswählen die Sie berechnen wollen", cc.xyw(2, 2, 4));

        pb.addLabel("Heilmittel 1", cc.xy(3, 4));
        String lab = Reha.instance.patpanel.vecaktrez.get(48);
        leistung[0] = new JRtaCheckBox((lab.equals("") ? "----" : lab));
        leistung[0].setOpaque(false);
        if (!lab.equals("")) {
            leistung[0].setSelected(true);
        } else {
            leistung[0].setSelected(false);
            leistung[0].setEnabled(false);
        }
        pb.add(leistung[0], cc.xyw(5, 4, 2));

        pb.addLabel("Heilmittel 2", cc.xy(3, 6));
        lab = Reha.instance.patpanel.vecaktrez.get(49);
        leistung[1] = new JRtaCheckBox((lab.equals("") ? "----" : lab));
        leistung[1].setOpaque(false);
        if (!lab.equals("")) {

        } else {
            leistung[1].setSelected(false);
            leistung[1].setEnabled(false);
        }
        pb.add(leistung[1], cc.xyw(5, 6, 2));

        pb.addLabel("Heilmittel 3", cc.xy(3, 8));
        lab = Reha.instance.patpanel.vecaktrez.get(50);
        leistung[2] = new JRtaCheckBox((lab.equals("") ? "----" : lab));
        leistung[2].setOpaque(false);
        if (!lab.equals("")) {

        } else {
            leistung[2].setSelected(false);
            leistung[2].setEnabled(false);
        }
        pb.add(leistung[2], cc.xyw(5, 8, 2));

        pb.addLabel("Heilmittel 4", cc.xy(3, 10));
        lab = Reha.instance.patpanel.vecaktrez.get(51);
        leistung[3] = new JRtaCheckBox((lab.equals("") ? "----" : lab));
        leistung[3].setOpaque(false);
        if (!lab.equals("")) {

        } else {
            leistung[3].setSelected(false);
            leistung[3].setEnabled(false);
        }
        pb.add(leistung[3], cc.xyw(5, 10, 2));

        pb.addLabel("Eintragen in Memo", cc.xy(3, 12));
        leistung[4] = new JRtaCheckBox("Fehldaten");
        leistung[4].setOpaque(false);
        leistung[4].setSelected(true);
        pb.add(leistung[4], cc.xyw(5, 12, 2));

        uebernahme = new JButton("drucken & buchen");
        uebernahme.setActionCommand("uebernahme");
        uebernahme.addActionListener(this);
        uebernahme.addKeyListener(this);
        pb.add(uebernahme, cc.xy(3, 14));

        abbrechen = new JButton("abbrechen");
        abbrechen.setActionCommand("abbrechen");
        abbrechen.addActionListener(this);
        abbrechen.addKeyListener(this);
        pb.add(abbrechen, cc.xy(5, 14));

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
                    // System.out.println("****************Ausfallrechnung -> Listener
                    // entfernt**************");
                }
            }
        } catch (NullPointerException ne) {
            // System.out.println("In PatNeuanlage" +evt);
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
            // System.out.println("****************Ausfallrechnung -> Listener entfernt
            // (Closed)**********");
        }

    }

    @Override
    public void actionPerformed(ActionEvent arg0) {

        if (arg0.getActionCommand()
                .equals("uebernahme")) {
            macheAFRHmap();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        starteAusfallRechnung(
                                Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK() + "/AusfallRechnung.ott");
                        doBuchen();
                        if (leistung[4].isSelected()) {
                            macheMemoEintrag();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Fehler bei der Erstellung der Ausfallrechnung");
                    }
                    getInstance().dispose();
                    return null;
                }
            }.execute();
            /*
             * new SwingWorker<Void,Void>(){
             *
             * @Override protected Void doInBackground() throws Exception {
             * if(leistung[4].isSelected()){ macheMemoEintrag(); } return null; }
             * }.execute(); this.dispose();
             */
        }
        if (arg0.getActionCommand()
                .equals("abbrechen")) {
            this.dispose();
        }

    }

    private AusfallRechnung getInstance() {
        return this;
    }

    private void doBuchen() {
        StringBuffer buf = new StringBuffer();
        buf.append("insert into rgaffaktura set ");
        buf.append("rnr='" + afrNummer + "', ");
        buf.append("reznr='" + Reha.instance.patpanel.vecaktrez.get(1) + "', ");
        buf.append("pat_intern='" + Reha.instance.patpanel.vecaktrez.get(0) + "', ");
        buf.append("rgesamt='" + SystemConfig.hmAdrAFRDaten.get("<AFRgesamt>")
                                                           .replace(",", ".")
                + "', ");
        buf.append("roffen='" + SystemConfig.hmAdrAFRDaten.get("<AFRgesamt>")
                                                          .replace(",", ".")
                + "', ");
        buf.append("rdatum='" + DatFunk.sDatInSQL(DatFunk.sHeute()) + "',");
        buf.append("ik='" + Reha.getAktIK() + "'");
        SqlInfo.sqlAusfuehren(buf.toString());
    }

    private void macheMemoEintrag() {
        StringBuffer sb = new StringBuffer();
        sb.append(DatFunk.sHeute() + " - unentschuldigt oder zu spät abgesagt - Rechnung!! - Rechnung-Nr.: "
                + SystemConfig.hmAdrAFRDaten.get("<AFRnummer>") + " - erstellt von: " + Reha.aktUser + "\n");
        sb.append(Reha.instance.patpanel.pmemo[1].getText());
        Reha.instance.patpanel.pmemo[1].setText(sb.toString());
        String cmd = "update pat5 set pat_text='" + sb.toString() + "' where pat_intern = '"
                + Reha.instance.patpanel.aktPatID + "'";
        SqlInfo.sqlAusfuehren(cmd);
    }

    private void macheAFRHmap() {
        String mappos = "";
        String mappreis = "";
        String mapkurz = "";
        String maplang = "";
        String[] inpos = { null, null };
        String spos = "";
        String sart = "";
        Double gesamt = new Double(0.00);
        int preisgruppe = 0;
        DecimalFormat df = new DecimalFormat("0.00");

        for (int i = 0; i < 4; i++) {
            mappos = "<AFRposition" + (i + 1) + ">";
            mappreis = "<AFRpreis" + (i + 1) + ">";
            mapkurz = "<AFRkurz" + (i + 1) + ">";
            maplang = "<AFRlang" + (i + 1) + ">";
            if (leistung[i].isSelected()) {
                Double preis = new Double(Reha.instance.patpanel.vecaktrez.get(18 + i));
                String s = df.format(preis);
                SystemConfig.hmAdrAFRDaten.put(mappos, leistung[i].getText());
                SystemConfig.hmAdrAFRDaten.put(mappreis, s);
                gesamt = gesamt + preis;

                spos = Reha.instance.patpanel.vecaktrez.get(8 + i);
                sart = Reha.instance.patpanel.vecaktrez.get(1);
                sart = sart.substring(0, 2);
                preisgruppe = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(41)) - 1;
                inpos = LeistungTools.getLeistung(sart, spos, preisgruppe);
                SystemConfig.hmAdrAFRDaten.put(maplang, inpos[0]);
                SystemConfig.hmAdrAFRDaten.put(mapkurz, inpos[1]);
                //// System.out.println(inpos[0]);
                //// System.out.println(inpos[1]);

            } else {
                spos = Reha.instance.patpanel.vecaktrez.get(8 + i);
                sart = Reha.instance.patpanel.vecaktrez.get(1);
                sart = sart.substring(0, 2);
                preisgruppe = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(41)) - 1;
                inpos = LeistungTools.getLeistung(sart, spos, preisgruppe);

                SystemConfig.hmAdrAFRDaten.put(mappos, leistung[i].getText());
                SystemConfig.hmAdrAFRDaten.put(mappreis, "0,00");
                SystemConfig.hmAdrAFRDaten.put(maplang, (!inpos[0].equals("") ? inpos[0] : "----"));
                SystemConfig.hmAdrAFRDaten.put(mapkurz, (!inpos[1].equals("") ? inpos[1] : "----"));

            }

        }
        SystemConfig.hmAdrAFRDaten.put("<AFRgesamt>", df.format(gesamt));
        /// Hier muß noch die Rechnungsnummer bezogen und eingetragen werden
        afrNummer = "AFR-" + Integer.toString(SqlInfo.erzeugeNummer("afrnr"));
        SystemConfig.hmAdrAFRDaten.put("<AFRnummer>", afrNummer);
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
        if (event.getKeyCode() == 27) {
            this.dispose();
        }
    }

    public static void starteAusfallRechnung(String url) {
        IDocumentService documentService = null;
        // System.out.println("Starte Datei -> "+url);
        if (!new OOService().getOfficeapplication().isActive()) {
            Reha.starteOfficeApplication();
        }
        try {
            documentService = new OOService().getOfficeapplication().getDocumentService();
        } catch (OfficeApplicationException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Fehler im OpenOffice-System - Ausfallrechnung kann nicht erstellt werden");
            return;
        }
        IDocumentDescriptor docdescript = new DocumentDescriptor();
        docdescript.setHidden(false);
        docdescript.setAsTemplate(true);
        IDocument document = null;
        // ITextTable[] tbl = null;
        try {
            document = documentService.loadDocument(url, docdescript);
        } catch (NOAException e) {

            e.printStackTrace();
        }
        ITextDocument textDocument = (ITextDocument) document;
        ITextFieldService textFieldService = textDocument.getTextFieldService();
        ITextField[] placeholders = null;
        try {
            placeholders = textFieldService.getPlaceholderFields();
        } catch (TextException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < placeholders.length; i++) {
            boolean schonersetzt = false;
            String placeholderDisplayText = placeholders[i].getDisplayText()
                                                           .toLowerCase();
            /*****************/
            Set<?> entries = SystemConfig.hmAdrPDaten.entrySet();
            Iterator<?> it = entries.iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = ((Map.Entry<String, String>) it.next());
                if (entry.getKey()
                         .toLowerCase()
                         .equals(placeholderDisplayText)) {
                    placeholders[i].getTextRange()
                                   .setText((entry.getValue()));
                    schonersetzt = true;
                    break;
                }
            }
            /*****************/
            entries = SystemConfig.hmAdrAFRDaten.entrySet();
            it = entries.iterator();
            while (it.hasNext() && (!schonersetzt)) {
                Map.Entry entry = (Map.Entry) it.next();
                if (((String) entry.getKey()).toLowerCase()
                                             .equals(placeholderDisplayText)) {
                    placeholders[i].getTextRange()
                                   .setText(((String) entry.getValue()));
                    schonersetzt = true;
                    break;
                }
            }
            if (!schonersetzt) {
                OOTools.loescheLeerenPlatzhalter(textDocument, placeholders[i]);
            }
            /*****************/
        }

    }

}

class AusfallRechnungHintergrund extends JXPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    ImageIcon hgicon;
    int icx, icy;
    AlphaComposite xac1 = null;
    AlphaComposite xac2 = null;

    public AusfallRechnungHintergrund() {
        super();

    }
}
