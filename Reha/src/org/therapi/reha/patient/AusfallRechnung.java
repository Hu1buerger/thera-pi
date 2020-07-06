package org.therapi.reha.patient;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import oOorgTools.OOTools;
import rezept.Money;
import systemEinstellungen.SystemConfig;
import systemTools.LeistungTools;

public class AusfallRechnung extends RehaSmartDialog {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(AusfallRechnung.class);

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
        // Lets make sure ESC-key works from anywhere..
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ESCAPE"), "escGedrueckt");
        getRootPane().getActionMap().put("escGedrueckt", escaped);
        // Pressing Enter will complete the dialog
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ENTER"), "enterGedrueckt");
        getRootPane().getActionMap().put("enterGedrueckt", tueEs);

    }

    /****************************************************/

    private JPanel getGebuehren() { // 1 2 3 4 5 6 7
        FormLayout lay = new FormLayout("10dlu,fill:0:grow(0.50),right:80dlu,10dlu,80dlu,fill:0:grow(0.50),10dlu",
                // 1...2..3....4..5...6..7...8..9...10.11....12...13.....14..15
                "15dlu,p,10dlu,p,2dlu,p,2dlu,p,2dlu,p, 10dlu, p,  20dlu, p ,20dlu");
        PanelBuilder pb = new PanelBuilder(lay);
        CellConstraints cc = new CellConstraints();

        pb.getPanel()
          .setOpaque(false);

        pb.addLabel("Bitte die Positionen ausw\u00e4hlen die Sie berechnen wollen", cc.xyw(2, 2, 4));

        pb.addLabel("Heilmittel 1", cc.xy(3, 4));
     // TODO: delete me once Rezepte have been sorted
        String lab = Reha.instance.patpanel.vecaktrez.get(48);
        logger.debug("Rez: lab=" + lab);
        lab = Reha.instance.patpanel.rezAktRez.getHMPos1();
        logger.debug("Vec: lab=" + lab);
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
     // TODO: delete me once Rezepte have been sorted
        lab = Reha.instance.patpanel.vecaktrez.get(49);
        logger.debug("Rez: lab=" + lab);
        lab = Reha.instance.patpanel.rezAktRez.getHMPos2();
        logger.debug("Vec: lab=" + lab);
        leistung[1] = new JRtaCheckBox((lab.equals("") ? "----" : lab));
        leistung[1].setOpaque(false);
        if (lab.isEmpty()) {
            leistung[1].setSelected(false);
            leistung[1].setEnabled(false);
        }
        pb.add(leistung[1], cc.xyw(5, 6, 2));

        pb.addLabel("Heilmittel 3", cc.xy(3, 8));
     // TODO: delete me once Rezepte have been sorted
        lab = Reha.instance.patpanel.vecaktrez.get(50);
        logger.debug("Rez: lab=" + lab);
        lab = Reha.instance.patpanel.rezAktRez.getHMPos3();
        logger.debug("Vec: lab=" + lab);
        leistung[2] = new JRtaCheckBox((lab.equals("") ? "----" : lab));
        leistung[2].setOpaque(false);
        if (lab.isEmpty()) {
            leistung[2].setSelected(false);
            leistung[2].setEnabled(false);
        }
        pb.add(leistung[2], cc.xyw(5, 8, 2));

        pb.addLabel("Heilmittel 4", cc.xy(3, 10));
     // TODO: delete me once Rezepte have been sorted
        lab = Reha.instance.patpanel.vecaktrez.get(51);
        logger.debug("Rez: lab=" + lab);
        lab = Reha.instance.patpanel.rezAktRez.getHMPos4();
        logger.debug("Vec: lab=" + lab);
        leistung[3] = new JRtaCheckBox((lab.equals("") ? "----" : lab));
        leistung[3].setOpaque(false);
        if (lab.isEmpty()) {
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
        uebernahme.addActionListener(e -> actionUebernahme(e));
        // uebernahme.addKeyListener(this);
        uebernahme.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("ENTER"), "enterGedrueckt");
        uebernahme.getActionMap().put("enterGedrueckt", tueEs);
        pb.add(uebernahme, cc.xy(3, 14));

        abbrechen = new JButton("abbrechen");
        abbrechen.setActionCommand("abbrechen");
        abbrechen.addActionListener(e -> actionExit());
        // abbrechen.addKeyListener(this);
        abbrechen.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke("ENTER"), "enterGedrueckt");
        abbrechen.getActionMap().put("enterGedrueckt", escaped);
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

    /*
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
             * /
        }
        if (arg0.getActionCommand()
                .equals("abbrechen")) {
            this.dispose();
        }

    }
    */

    private AusfallRechnung getInstance() {
        return this;
    }

    private void doBuchen() {
        StringBuffer buf = new StringBuffer();
        buf.append("insert into rgaffaktura set ");
        buf.append("rnr='" + afrNummer + "', ");
     // TODO: delete me once Rezepte have been sorted
        // buf.append("reznr='" + Reha.instance.patpanel.vecaktrez.get(1) + "', ");
        // buf.append("pat_intern='" + Reha.instance.patpanel.vecaktrez.get(0) + "', ");
        buf.append("reznr='" + Reha.instance.patpanel.rezAktRez.getRezNr() + "', ");
        buf.append("pat_intern='" + String.valueOf(Reha.instance.patpanel.rezAktRez.getPatIntern()) + "', ");
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
        sb.append(DatFunk.sHeute() + " - unentschuldigt oder zu sp\u00e4t abgesagt - Rechnung!! - Rechnung-Nr.: "
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
        Double dGesamt = new Double(0.00);
        Money gesamt = new Money("0");
        int preisgruppe = 0;
        DecimalFormat df = new DecimalFormat("0.00");

        for (int i = 0; i < 4; i++) {
            mappos = "<AFRposition" + (i + 1) + ">";
            mappreis = "<AFRpreis" + (i + 1) + ">";
            mapkurz = "<AFRkurz" + (i + 1) + ">";
            maplang = "<AFRlang" + (i + 1) + ">";
            if (leistung[i].isSelected()) {
             // TODO: delete me once Rezepte have been sorted
                Double dPreis = new Double(Reha.instance.patpanel.vecaktrez.get(18 + i));
                logger.debug("Vec: preis=" + dPreis);
                Money preis = Reha.instance.patpanel.rezAktRez.getPreis(i+1);
                String s = preis.toString();
                SystemConfig.hmAdrAFRDaten.put(mappos, leistung[i].getText());
                SystemConfig.hmAdrAFRDaten.put(mappreis, s);
                dGesamt = dGesamt + dPreis;
                gesamt.add(preis);
                logger.debug("Vec: gesamt= " + String.valueOf(dGesamt));
                logger.debug("Rez: gesamt= " + gesamt.toString());

             // TODO: delete me once Rezepte have been sorted
                spos = Reha.instance.patpanel.vecaktrez.get(8 + i);
                logger.debug("Vec: spos= " + spos);
                // Q&D - should rather do proper to-int conversion of the code
                spos = String.valueOf(Reha.instance.patpanel.rezAktRez.getArtDerBehandlung(i+1));
                logger.debug("Rez: spos= " + spos);
                sart = Reha.instance.patpanel.vecaktrez.get(1);
                logger.debug("Vec: sart= " + sart);
                sart = Reha.instance.patpanel.rezAktRez.getRezNr();
                logger.debug("Rez: sart= " + sart);
                sart = sart.substring(0, 2);
             // TODO: delete me once Rezepte have been sorted
                preisgruppe = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(41)) - 1;
                logger.debug("Vec: Preisgruppe=" + preisgruppe);
                preisgruppe = Reha.instance.patpanel.rezAktRez.getPreisGruppe() -1;
                logger.debug("Rez: Preisgruppe=" + preisgruppe);
                
                inpos = LeistungTools.getLeistung(sart, spos, preisgruppe);
                
                SystemConfig.hmAdrAFRDaten.put(maplang, inpos[0]);
                SystemConfig.hmAdrAFRDaten.put(mapkurz, inpos[1]);
                //// System.out.println(inpos[0]);
                //// System.out.println(inpos[1]);

            } else {
             // TODO: delete me once Rezepte have been sorted
                spos = Reha.instance.patpanel.vecaktrez.get(8 + i);
                logger.debug("Vec: spos= " + spos);
                // Q&D - should rather do proper to-int conversion of the code
                spos = String.valueOf(Reha.instance.patpanel.rezAktRez.getArtDerBehandlung(i+1));
                logger.debug("Rez: spos= " + spos);
                sart = Reha.instance.patpanel.vecaktrez.get(1);
                logger.debug("Vec: sart= " + sart);
                sart = Reha.instance.patpanel.rezAktRez.getRezNr();
                logger.debug("Rez: sart= " + sart);
                sart = sart.substring(0, 2);
             // TODO: delete me once Rezepte have been sorted
                preisgruppe = Integer.parseInt(Reha.instance.patpanel.vecaktrez.get(41)) - 1;
                logger.debug("Vec: Preisgruppe=" + preisgruppe);
                preisgruppe = Reha.instance.patpanel.rezAktRez.getPreisGruppe() -1;
                logger.debug("Rez: Preisgruppe=" + preisgruppe);
                
                inpos = LeistungTools.getLeistung(sart, spos, preisgruppe);

                SystemConfig.hmAdrAFRDaten.put(mappos, leistung[i].getText());
                SystemConfig.hmAdrAFRDaten.put(mappreis, "0,00");
                SystemConfig.hmAdrAFRDaten.put(maplang, (!inpos[0].equals("") ? inpos[0] : "----"));
                SystemConfig.hmAdrAFRDaten.put(mapkurz, (!inpos[1].equals("") ? inpos[1] : "----"));

            }

        }
        SystemConfig.hmAdrAFRDaten.put("<AFRgesamt>", df.format(dGesamt));
        /// Hier muss noch die Rechnungsnummer bezogen und eingetragen werden
        afrNummer = "AFR-" + Integer.toString(SqlInfo.erzeugeNummer("afrnr"));
        SystemConfig.hmAdrAFRDaten.put("<AFRnummer>", afrNummer);
    }

    /*
    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            event.consume();
            if (((JComponent) event.getSource()).getName()
                                                .equals("uebernahme")) {
                // doUebernahme();  // Why was this disabled? It's still in actioncommands...
            }
            if (((JComponent) event.getSource()).getName()
                                                .equals("abbrechen")) {
                this.dispose();
            }

            // System.out.println("Return Gedrueckt");
        }
        /*
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            this.dispose();
        }
        * /
    }
    */

    public static void starteAusfallRechnung(String url) {
        IDocumentService documentService = null;
        // System.out.println("Starte Datei -> "+url);
        if (!Reha.officeapplication.isActive()) {
            Reha.starteOfficeApplication();
        }
        try {
            documentService = Reha.officeapplication.getDocumentService();
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
    
    /*
     * Actions
     */
    /**
     * Used when the ESC-Key is pressed somewhere
     */
    Action escaped = new AbstractAction() {
        static final long serialVersionUID = 1L;
        @Override
        public void actionPerformed(ActionEvent arg0) {
            actionExit();
        }
    };
    Action tueEs = new AbstractAction() {
        static final long serialVersionUID = 1L;
        @Override
        public void actionPerformed(ActionEvent arg0) {
            actionUebernahme();
        }
    };
    private void actionExit() {
        this.dispose();
    }
    // From original Keylistener
    private void actionUebernahme() {
        // doUebernahme();
    }
    // From original ActionListener
    private void actionUebernahme(ActionEvent arg0) {
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
