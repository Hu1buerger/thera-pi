package org.thera_pi.nebraska.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXPanel;
import org.thera_pi.nebraska.crypto.NebraskaCryptoException;
import org.thera_pi.nebraska.crypto.NebraskaFileException;
import org.thera_pi.nebraska.crypto.NebraskaKeystore;
import org.thera_pi.nebraska.gui.utils.FileStatics;
import org.thera_pi.nebraska.gui.utils.JCompTools;
import org.thera_pi.nebraska.gui.utils.JRtaCheckBox;
import org.thera_pi.nebraska.gui.utils.JRtaRadioButton;
import org.thera_pi.nebraska.gui.utils.pdf.PDFDrucker;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import CommonTools.DatFunk;
import CommonTools.INIFile;
import CommonTools.JRtaTextField;

public class NebraskaZertAntrag extends JXPanel implements ListSelectionListener, ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -8078665856424061730L;
    JRtaTextField[] tf = { null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null }; // 'Bemerkungen' fallen weg ...
            null, null };
    JRtaRadioButton[] jrb = { null, null, null, null, null, null, null };

    ButtonGroup bg1 = new ButtonGroup();
    ButtonGroup bg2 = new ButtonGroup();

    JRtaCheckBox[] jcb = { null };
    JButton[] jbut = { null, null, null, null, null };

    String[][] resultRadio1 = { { "Ja", "Nein", "Nein" }, { "Nein", "Ja", "Nein" }, { "Nein", "Nein", "Ja" } };
    String[][] resultRadio2 = { { "Ja", "Nein" }, { "Nein", "Ja" } };
    HashMap<String, String> hmPdf = new HashMap<String, String>();

    Vector<Component> newPolicy = new Vector<Component>();
    MyStammFocusTraversalPolicy myPolicy;
    FocusListener fl;
    JScrollPane jscr;

    boolean antragprint = false;
    boolean importiert = false;
    String therapidir = java.lang.System.getProperty("user.dir");
    public NebraskaJTabbedPaneOrganizer elternTab;

    public NebraskaZertAntrag(NebraskaJTabbedPaneOrganizer xelternTab) {
        super();
        this.elternTab = xelternTab;
        setOpaque(false);
        JPanel jpan = new JPanel(new BorderLayout());
        jpan.setOpaque(false);
        jpan.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        FormLayout lay1 = new FormLayout("10dlu,fill:0:grow,10dlu",
                "10dlu,p,10dlu,p,10dlu,p,10dlu,p,10dlu,p,10dlu,p,10dlu,p,10dlu,p,5dlu");
        CellConstraints c1 = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(lay1);
        //PanelBuilder pb = new PanelBuilder(lay1, new FormDebugPanel());     // debug mode
        pb.getPanel()
          .setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pb.addSeparator("Stammdaten", c1.xyw(2, 2, 1));
        pb.add(getAbschnitt1(), c1.xyw(2, 4, 2));
        pb.addSeparator("Freiwillige Angaben zur Software, sowie wichtige Angaben zur Datenübermittlung",
                c1.xyw(2, 6, 1));
        pb.add(getAbschnitt2(), c1.xy(2, 8));
        pb.addSeparator("Rechnungsanschrift (nur notwendig sofern abweichend von der Adresse des Antragstellers)",
                c1.xyw(2, 10, 1));
        pb.add(getAbschnitt3(), c1.xy(2, 12));
//        pb.addSeparator("Bemerkungen (sofern erforderlich bzw. gewünscht)", c1.xyw(2, 14, 2));
//        pb.add(getAbschnitt4(), c1.xy(2, 16));

        pb.getPanel()
          .validate();
        jscr = JCompTools.getTransparentScrollPane(pb.getPanel());
        jscr.getVerticalScrollBar()
            .setUnitIncrement(15);
        jscr.validate();
        jpan.add(jscr, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(jpan, BorderLayout.CENTER);
        add(getButtonPanel(), BorderLayout.SOUTH);
        validate();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                fl = new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent arg0) {
                        if (((JComponent) arg0.getSource()) instanceof JRtaTextField) {
                            ((JRtaTextField) ((JComponent) arg0.getSource())).setSelectionStart(0);
                            ((JRtaTextField) ((JComponent) arg0.getSource())).setSelectionEnd(0);
                            ((JRtaTextField) ((JComponent) arg0.getSource())).setCaretPosition(0);
                        }

                        int y = ((JComponent) arg0.getSource()).getBounds().y
                                + ((JComponent) arg0.getSource()).getParent()
                                                                 .getBounds().y
                                + 15;
                        Rectangle rec1 = ((JComponent) arg0.getSource()).getBounds();
                        Rectangle rec2 = jscr.getViewport()
                                             .getViewRect();
                        jscr.getViewport()
                            .getViewRect();
                        JViewport vp = jscr.getViewport();
                        if (y > (rec2.y + rec2.height)) {
                            vp.setViewPosition(new Point(rec1.x, (rec2.y + rec2.height) - rec1.height));
                        }
                        if (y < (rec2.y)) {
                            vp.setViewPosition(new Point(rec1.x, rec1.y));
                        }
                        jscr.validate();
                    }

                    @Override
                    public void focusLost(FocusEvent arg0) {
                        if (((JComponent) arg0.getSource()) instanceof JRtaTextField) {
                            ((JRtaTextField) ((JComponent) arg0.getSource())).setCaretPosition(0);
                        }
                    }
                };
                for (int i = 0; i < 10; i++) {
                    tf[i].addFocusListener(fl);
                    newPolicy.add(tf[i]);
                }
                for (int i = 0; i < 2; i++) {
                    jrb[i].addFocusListener(fl);
                    newPolicy.add(jrb[i]);
                }
                for (int i = 10; i < 12; i++) {
                    tf[i].addFocusListener(fl);
                    newPolicy.add(tf[i]);
                }
                for (int i = 2; i < 5; i++) {
                    jrb[i].addFocusListener(fl);
                    newPolicy.add(jrb[i]);
                }
                newPolicy.add(tf[12]);
                tf[12].addFocusListener(fl);
                newPolicy.add(jcb[0]);
                jcb[0].addFocusListener(fl);
                for (int i = 13; i < 21; i++) {
                    tf[i].addFocusListener(fl);
                    newPolicy.add(tf[i]);
                }
                myPolicy = new MyStammFocusTraversalPolicy(newPolicy);
                setFocusCycleRoot(true);
                setFocusTraversalPolicy(myPolicy);
                return null;
            }
        }.execute();
    }

    public void setzeFocus() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tf[0].requestFocus();
            }
        });
    }

    /******
     * 
     * 
     * 
     */
    private void doRequestMachen() {
        NebraskaRequestDlg nrdlg = new NebraskaRequestDlg(this, importiert, therapidir);
        nrdlg.setPreferredSize(new Dimension(800, 400));
        nrdlg.pack();
        nrdlg.setLocationRelativeTo(null);
        nrdlg.setVisible(true);
    }

    private void doReplyEinlesen() {
        NebraskaReplyEinlesen nrdlg = new NebraskaReplyEinlesen(this);
        nrdlg.setPreferredSize(new Dimension(800, 400));
        nrdlg.pack();
        nrdlg.setLocationRelativeTo(null);
        nrdlg.setVisible(true);

    }

    /******
     * 
     * 
     * 
     */
    public String getIK() {
        return tf[0].getText()
                    .trim();
    }

    public String getInstitution() {
        return tf[1].getText()
                    .trim();
    }

    public String getPerson() {
        return tf[2].getText()
                    .trim();
    }

    private JPanel getButtonPanel() {
        FormLayout lay2 = new FormLayout(
                "10dlu,fill:0:grow(0.2),5dlu,fill:0:grow(0.2),5dlu,fill:0:grow(0.2),5dlu,fill:0:grow(0.2),5dlu,fill:0:grow(0.2),5dlu",
                "2dlu,10dlu,p,10dlu");
        PanelBuilder a1 = new PanelBuilder(lay2);
        CellConstraints c1 = new CellConstraints();
        a1.getPanel()
          .setOpaque(false);
        a1.addSeparator("");
        a1.add((jbut[0] = macheBut("Datenimport aus Thera-Pi", "therapiimport")), c1.xy(2, 3));
        a1.add((jbut[1] = macheBut("Zert-Antrag drucken", "antragprint")), c1.xy(4, 3));
        a1.add((jbut[2] = macheBut("Zert-Request erzeugen", "requestmachen")), c1.xy(6, 3));
        a1.add((jbut[3] = macheBut("Zert-Reply einlesen", "replyeinlesen")), c1.xy(8, 3));
        a1.add((jbut[4] = macheBut("zusätzl. IK einlesen f. §301", "neuesIkEinlesen")), c1.xy(10, 3));
        jbut[1].setEnabled(false);
        jbut[2].setEnabled(false);
        jbut[3].setEnabled(false);
        jbut[4].setEnabled(false);
        a1.getPanel()
          .validate();
        return a1.getPanel();
    }

    public JButton macheBut(String titel, String cmd) {
        JButton but = new JButton(titel);
        but.setActionCommand(cmd);
        but.addActionListener(this);
        return but;
    }

    private JPanel getAbschnitt1() {
        FormLayout lay2 = new FormLayout(
                // "62dlu,right:max(30dlu;p),5dlu,100dlu,90dlu,right:max(30;p),5dlu,100dlu","p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p");
                "62dlu,right:max(30dlu;p),5dlu,130dlu,60dlu,right:max(30;p),5dlu,100dlu",
                "p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p");
        PanelBuilder a1 = new PanelBuilder(lay2);
        a1.getPanel()
          .setOpaque(false);
        CellConstraints c2 = new CellConstraints();

        JLabel lbl0 = new JLabel("Institutionskennzeichen (IK)");
        a1.add(lbl0, c2.xy(2, 1));
        tf[0] = new JRtaTextField("GROSS", true);
        tf[0].setName("IK");
        a1.add(tf[0], c2.xy(4, 1));
        JLabel lbl1 = new JLabel("Betriebsnummer");
        a1.add(lbl1, c2.xy(6, 1));
        tf[6] = new JRtaTextField("Text", true);
        tf[6].setName("Betriebsnummer");
        tf[6].setEditable(false);
        a1.add(tf[6], c2.xy(8, 1));

        JLabel lbl2 = new JLabel("Name/Firma des Antragstellers");
        a1.add(lbl2, c2.xy(2, 3));
        tf[1] = new JRtaTextField("Text", true);
        tf[1].setName("NameAntragsteller");
        a1.add(tf[1], c2.xy(4, 3));
        JLabel lbl3 = new JLabel("verantwortlicher Ansprechpartner");
        a1.add(lbl3, c2.xy(2, 5));
        tf[2] = new JRtaTextField("Text", true);
        tf[2].setName("Ansprechpartner");
        a1.add(tf[2], c2.xy(4, 5));
        JLabel lbl4 = new JLabel("Straße");
        a1.add(lbl4, c2.xy(2, 7));
        tf[3] = new JRtaTextField("Text", true);
        tf[3].setName("Strasse");
        a1.add(tf[3], c2.xy(4, 7));
        JLabel lbl5 = new JLabel("PLZ");
        a1.add(lbl5, c2.xy(2, 9));
        tf[4] = new JRtaTextField("ZAHLEN", true);
        tf[4].setName("PLZ");
        a1.add(tf[4], c2.xy(4, 9));
        JLabel lbl6 = new JLabel("Ort");
        a1.add(lbl6, c2.xy(2, 11));
        tf[5] = new JRtaTextField("Text", true);
        tf[5].setName("Ort");
        a1.add(tf[5], c2.xy(4, 11));

        JLabel lbl7 = new JLabel("Telefon-Nr.");
        a1.add(lbl7, c2.xy(6, 3));
        tf[7] = new JRtaTextField("Text", true);
        tf[7].setName("Telefon");
        a1.add(tf[7], c2.xy(8, 3));
        JLabel lbl8 = new JLabel("Telefax-Nr.");
        a1.add(lbl8, c2.xy(6, 5));
        tf[8] = new JRtaTextField("Text", true);
        tf[8].setName("Telefax");
        a1.add(tf[8], c2.xy(8, 5));
        JLabel lbl9 = new JLabel("E-Mail-Adresse");
        a1.add(lbl9, c2.xy(6, 7));
        tf[9] = new JRtaTextField("Text", true);
        tf[9].setName("E-Mail");
        a1.add(tf[9], c2.xy(8, 7));

//        jrb[0] = new JRtaRadioButton("Zertifizierungsantwort an diese E-Mail-Adresse");
//        // jrb[0].setActionCommand("");
//        jrb[0].setSelected(false);
//        jrb[0].addActionListener(this);
//        jrb[0].setName("Kontrollkaestchen1");
//        a1.add(jrb[0], c2.xyw(6, 9, 3));
        jcb[0] =  new JRtaCheckBox("Zertifizierungsantwort an diese E-Mail-Adresse");
        jcb[0].setSelected(true );
        jcb[0].addActionListener(this);
        jcb[0].setName("Kontrollkaestchen1");
        a1.add(jcb[0],c2.xyw(6,9,3));

//      jrb[1] =  new JRtaRadioButton("Zertifizierungsantwort per Diskette");
//      //jrb[0].setActionCommand("");
//      jrb[1].setSelected(false);
//      jrb[1].addActionListener(this);
//      jrb[1].setName("Kontrollkaestchen2");
//      a1.add(jrb[1],c2.xyw(6,11,3));
//      bg1.add(jrb[0]);
//      bg1.add(jrb[1]);
        return a1.getPanel();
    }

    private JPanel getAbschnitt2() {
        FormLayout lay3 = new FormLayout("right:max(30dlu;p),5dlu,350dlu,p",
                "p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p");
        PanelBuilder a2 = new PanelBuilder(lay3);
        a2.getPanel()
          .setOpaque(false);
        CellConstraints c3 = new CellConstraints();

        JLabel lbl10 = new JLabel("Mit welchem Softwarehaus arbeiten Sie zusammen?");
        a2.add(lbl10, c3.xy(1, 1));
        tf[10] = new JRtaTextField("Text", true);
        tf[10].setName("Softwarehaus");
        a2.add(tf[10], c3.xy(3, 1));
        JLabel lbl11 = new JLabel("Welche Fachanwendung setzen sie ein?");
        a2.add(lbl11, c3.xy(1, 3));
        tf[11] = new JRtaTextField("Text", true);
        tf[11].setName("Fachanwendung");
        a2.add(tf[11], c3.xy(3, 3));

        JLabel lbl17 = new JLabel("Wie wird der Request der ITSG übermittelt?");
        a2.add(lbl17, c3.xy(1, 5));

        jrb[2] = new JRtaRadioButton("über die Online-Schnittstelle des Trust Centers (OSTC)");
        // jrb[2].setActionCommand("");
        jrb[2].setSelected(false);
        jrb[2].addActionListener(this);
        jrb[2].setName("Kontrollkaestchen3");
        a2.add(jrb[2], c3.xy(3, 5));

        jrb[3] = new JRtaRadioButton("per E-Mail,(crq@itsg-trust.de)");
        // jrb[3].setActionCommand("");
        jrb[3].setSelected(true);
        jrb[3].addActionListener(this);
        jrb[3].setName("Kontrollkaestchen4");
        a2.add(jrb[3], c3.xy(3, 7));

//        jrb[4] = new JRtaRadioButton("per Diskette (ITSG- Trust Center, Postfach 12 30, 49702 Meppen)");
//        // jrb[0].setActionCommand("");
//        jrb[4].setSelected(false);
//        jrb[4].addActionListener(this);
//        jrb[4].setName("Kontrollkaestchen5");
//        a2.add(jrb[4], c3.xy(3, 9));
        bg2.add(jrb[2]);
        bg2.add(jrb[3]);
//        bg2.add(jrb[4]);

        JLabel lbl12 = new JLabel("(optional, max. 12 Zeichen) Das Kundenkennwort ist?");
        a2.add(lbl12, c3.xy(1, 11));
        tf[12] = new JRtaTextField("Text", true);
        tf[12].setName("Kundenkennwort");
        a2.add(tf[12], c3.xy(3, 11));

//        jcb[0] = new JRtaCheckBox(
//                "Eine Sperrung der Zertifikates soll auch ohne Angabe eines Kundenkennwortes möglich sein.");
//        // jrb[0].setActionCommand("");
//        jcb[0].setSelected(false);
//        jcb[0].addActionListener(this);
//        jcb[0].setName("Kontrollkaestchen6");
//        a2.add(jcb[0], c3.xy(3, 13));
        return a2.getPanel();
    }

    private JPanel getAbschnitt3() {
        FormLayout lay4 = new FormLayout("98dlu,right:max(30dlu;p),5dlu,130dlu", "p,1dlu,p,1dlu,p,1dlu,p");
        PanelBuilder a3 = new PanelBuilder(lay4);
        a3.getPanel()
          .setOpaque(false);
        CellConstraints c4 = new CellConstraints();

        JLabel lbl13 = new JLabel("Name/Firma");
        a3.add(lbl13, c4.xy(2, 1));
        tf[13] = new JRtaTextField("Text", true);
        tf[13].setName("RechnungName");
        a3.add(tf[13], c4.xy(4, 1));
        JLabel lbl14 = new JLabel("Straße oder Postfach");
        a3.add(lbl14, c4.xy(2, 3));
        tf[14] = new JRtaTextField("Text", true);
        tf[14].setName("RechnungStrasse");
        a3.add(tf[14], c4.xy(4, 3));
        JLabel lbl15 = new JLabel("PLZ");
        a3.add(lbl15, c4.xy(2, 5));
        tf[15] = new JRtaTextField("ZAHLEN", true);
        tf[15].setName("RechnungPLZ");
        a3.add(tf[15], c4.xy(4, 5));
        JLabel lbl16 = new JLabel("Ort");
        a3.add(lbl16, c4.xy(2, 7));
        tf[16] = new JRtaTextField("Text", true);
        tf[16].setName("RechnungOrt");
        a3.add(tf[16], c4.xy(4, 7));
        return a3.getPanel();
    }

//    private JPanel getAbschnitt4() {    // kann weg
//        FormLayout lay5 = new FormLayout("164dlu,5dlu,350dlu", "p,1dlu,p,1dlu,p,1dlu,p");
//        PanelBuilder a4 = new PanelBuilder(lay5);
//        a4.getPanel()
//          .setOpaque(false);
//        CellConstraints c5 = new CellConstraints();
//
//        tf[17] = new JRtaTextField("Text", true);
//        tf[17].setName("Bemerkung1");
//        a4.add(tf[17], c5.xy(3, 1));
//        tf[18] = new JRtaTextField("Text", true);
//        tf[18].setName("Bemerkung2");
//        a4.add(tf[18], c5.xy(3, 3));
//        tf[19] = new JRtaTextField("Text", true);
//        tf[19].setName("Bemerkung3");
//        a4.add(tf[19], c5.xy(3, 5));
//        tf[20] = new JRtaTextField("Text", true);
//        tf[20].setName("Bemerkung4");
//        a4.add(tf[20], c5.xy(3, 7));
//        return a4.getPanel();
//    }

    @Override
    public void valueChanged(ListSelectionEvent arg0) {

    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        String cmd = arg0.getActionCommand();

        if (cmd.equals("therapiimport")) {
            doTheraPiImport();
            return;
        }
        if (cmd.equals("antragprint")) {
            doAntragPrint();
            antragprint = true;
            return;
        }
        if (cmd.equals("requestmachen")) {
            if (!antragprint) {
                JOptionPane.showMessageDialog(null, "Bitte drucken Sie im ersten Schritt den Zertifizierungsantrag");
                // return;
            }
            doRequestMachen();
            return;
        }
        if (cmd.equals("replyeinlesen")) {
            doReplyEinlesen();
            return;
        }
        if (cmd.equals("replyeinlesen")) {
            doReplyEinlesen();
            return;
        }
        if (cmd.equals("neuesIkEinlesen")) {
            try {
                String kfile = "";
                String pw = "";
                String aliasIk = "";
                String institution = "";
                String person = "";
                NebraskaZertExplorer explorer = elternTab.getZertExplorer();
                int index = explorer.jcombo.getSelectedIndex();
                if (index == 0) {
                    return;
                } else {
                    System.out.println("Vector-Element-0 = " + NebraskaMain.keyStoreParameter.get(index - 1)
                                                                                             .get(0));
                    System.out.println("Vector-Element-1 = " + NebraskaMain.keyStoreParameter.get(index - 1)
                                                                                             .get(1));
                    System.out.println("Vector-Element-2 = " + NebraskaMain.keyStoreParameter.get(index - 1)
                                                                                             .get(2));
                    System.out.println("Vector-Element-3 = " + NebraskaMain.keyStoreParameter.get(index - 1)
                                                                                             .get(3));
                    System.out.println("getInstitution() = " + getInstitution());
                    System.out.println("getPerson()      = " + getPerson());
                    kfile = NebraskaMain.keyStoreParameter.get(index - 1)
                                                          .get(0);
                    pw = NebraskaMain.keyStoreParameter.get(index - 1)
                                                       .get(1);
                    aliasIk = NebraskaMain.keyStoreParameter.get(index - 1)
                                                            .get(2);
                    institution = getInstitution();
                    person = getPerson();
                }
                try {

                    Object ret = JOptionPane.showInputDialog(null,
                            "Geben Sie bitte das (9-stellige) IK der neuen  Annahmestelle ein", "");
                    if (ret == null) {
                        return;
                    }
                    if (ret.equals("")) {
                        return;
                    }
                    NebraskaKeystore nebraskastore = new NebraskaKeystore(kfile, pw, "abc", aliasIk, institution,
                            person);
                    String pfad = this.therapidir + "keystore";
                    String source = FileStatics.dirChooser(pfad, "p7c.Datei auswählen");
                    if (!source.equals("")) {
                        pfad = String.valueOf(source.replace("\\", "/"));
                    } else {
                        return;
                    }

                    nebraskastore.importNewCertFromReply(pfad, (String) ret);
                    explorer.jcombo.setSelectedIndex(index);

                } catch (NebraskaCryptoException e) {
                    e.printStackTrace();
                } catch (NebraskaFileException e) {
                    e.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

    }

    private void doTheraPiImport() {
        try {
            String ik = "";
            String ikPfad = "";
            String pfad = java.lang.System.getProperty("user.dir") + "/ini/mandanten.ini";
            File f = new File(pfad);
            if (!f.exists()) {
                pfad = FileStatics.dirChooser(java.lang.System.getProperty("user.dir") + "/ini/",
                        "Bitte wählen Sie die Datei mandanten.ini aus Ihrer Thera-Pi Installation");
            }
            System.out.println(pfad);

            if (pfad.equals("")) {
                return;
            }
            if (!pfad.endsWith("mandanten.ini")) {
                JOptionPane.showMessageDialog(null, "Das ausgewählte Verzeichnis ist keine Thera-Pi mandanten.ini,\n"
                        + "Oder das Thera-Pi Verzeichnis ist inkonsistent.\n\nDatenimport fehlgeschlagen!!");
                return;
            }
            pfad = pfad.replace("\\", "/");
            System.out.println("Pfad = " + pfad);
            therapidir = pfad.substring(0, pfad.lastIndexOf("/"));
            therapidir = therapidir.substring(0, therapidir.lastIndexOf("/"));
            System.out.println("TherapiDir = " + therapidir);
            INIFile inif = new INIFile(pfad);
            int mandantenAnzahl = inif.getIntegerProperty("TheraPiMandanten", "AnzahlMandanten");
            if (mandantenAnzahl == 1) {
                ik = inif.getStringProperty("TheraPiMandanten", "MAND-IK" + Integer.toString(mandantenAnzahl));
                ikPfad = therapidir + File.separator + "ini" + File.separator + ik + File.separator + "firmen.ini";
                mandantLesen(ikPfad);
                importiert = true;
                jbut[1].setEnabled(true);
                jbut[2].setEnabled(true);
                jbut[3].setEnabled(true);
                jbut[4].setEnabled(true);
            } else if (mandantenAnzahl > 1) {
                String[] mandanten = new String[mandantenAnzahl];
                for (int i = 0; i < mandantenAnzahl; i++) {
                    mandanten[i] = inif.getStringProperty("TheraPiMandanten", "MAND-IK" + Integer.toString(i + 1));
                }
                String retwert = (String) JOptionPane.showInputDialog(null,
                        "Bitte wählen Sie den entsprechenden Mandanten aus",
                        "Sie haben mehrere Mandaten in Ihrem System installiert", JOptionPane.QUESTION_MESSAGE, null,
                        mandanten, mandanten[0]);
                if (retwert == null) {
                    importiert = false;
                    therapidir = "";
                    return;
                }
                try {
                    for (int x = 0; x < NebraskaMain.keyStoreParameter.size(); x++) {
                        if (NebraskaMain.keyStoreParameter.get(x)
                                                          .get(2)
                                                          .equals("IK" + retwert.trim())) {
                            elternTab.zertExplorer.jcombo.setSelectedItem("IK" + retwert);
                            break;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                ik = mandanten[Arrays.asList(mandanten)
                                     .indexOf(retwert)];
                // ikPfad = (pfad.replace("mandanten.ini/",
                // "")+File.separator+"ini"+File.separator+ik+File.separator+"firmen.ini").replace("\\",
                // "/");
                ikPfad = therapidir + "/ini/" + ik + "/firmen.ini";
                System.out.println("Pfad zu der Firmendatei = " + ikPfad);
                mandantLesen(ikPfad);
                importiert = true;
                jbut[1].setEnabled(true);
                jbut[2].setEnabled(true);
                jbut[3].setEnabled(true);
                jbut[4].setEnabled(true);
            } else if (mandantenAnzahl == 1) {
                jbut[1].setEnabled(true);
                jbut[2].setEnabled(true);
                jbut[3].setEnabled(true);
                jbut[4].setEnabled(true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        tf[0].requestFocus();
    }

    private void mandantLesen(String ikPfad) {
        INIFile inif = new INIFile(ikPfad);
        System.out.println(ersetzteUnerlaubtes(inif.getStringProperty("Firma", "Ik")));
        tf[0].setText(ersetzteUnerlaubtes(inif.getStringProperty("Firma", "Ik")));
        tf[1].setText(ersetzteUnerlaubtes(inif.getStringProperty("Firma", "Firma1")));
        tf[1].setCaretPosition(0);
        tf[2].setText((ersetzteUnerlaubtes(inif.getStringProperty("Firma", "Vorname")) + " "
                + ersetzteUnerlaubtes(inif.getStringProperty("Firma", "Nachname"))));
        tf[3].setText(ersetzteUnerlaubtes(inif.getStringProperty("Firma", "Strasse")));
        tf[4].setText(ersetzteUnerlaubtes(inif.getStringProperty("Firma", "Plz")));
        tf[5].setText(ersetzteUnerlaubtes(inif.getStringProperty("Firma", "Ort")));
        tf[7].setText(ersetzteUnerlaubtes(inif.getStringProperty("Firma", "Telefon")));
        tf[8].setText(ersetzteUnerlaubtes(inif.getStringProperty("Firma", "Telefax")));
        tf[9].setText(ersetzteUnerlaubtes(inif.getStringProperty("Firma", "Email")));
//        jrb[0].setSelected(true);
        jcb[0].setSelected(true);
        tf[10].setText("./. entfällt");
        tf[11].setText("Thera-Pi / Nebraska");
        jrb[3].setSelected(true);
    }

    private String ersetzteUnerlaubtes(String instring) {
        if (instring == null) {
            return "";
        }
        String result = instring;
        result = result.replaceAll("ä", "ae")
                       .replaceAll("ö", "oe")
                       .replaceAll("ü", "ue");
        result = result.replaceAll("Ä", "AE")
                       .replaceAll("Ö", "OE")
                       .replaceAll("Ü", "UE");
        result = result.replaceAll("ß", "ss")
                       .replaceAll("&", "und");
        return result;

    }

    private void doAntragPrint() {
        hmPdf.clear();
        if ((tf[0].getText()
                  .trim()
                  .equals(""))
                || (tf[0].getText()
                         .trim()
                         .length() != 9)) {
            JOptionPane.showMessageDialog(null, "Die Angabe einer korrekten(!) IK wäre nicht schlecht");
            return;
        }
        if (tf[12].getText()
                  .trim()
                  .length() > 12) {
            JOptionPane.showMessageDialog(null, "Das Kundenkennwort darf maximal 12 Zeichen lang sein!");
            return;
        }
        for (int i = 0; i < tf.length; i++) {
            hmPdf.put(tf[i].getName(), tf[i].getText()
                                            .trim());
        }
        int antwort = -1;
//        if (jrb[0].isSelected() || jrb[1].isSelected()) {   // jetzt: jcb[0]
//            if (jrb[0].isSelected()) {
//                antwort = 0;
//            } else {
//                antwort = 1;
//            }
//            hmPdf.put(jrb[0].getName(), resultRadio2[antwort][0]);
//            hmPdf.put(jrb[1].getName(), resultRadio2[antwort][1]);
//        } else {
//            JOptionPane.showMessageDialog(null,
//                    "Bitte geben Sie an wie Sie die Zertifizierungsantwort erhalten wollen");
//            return;
//        }
        if (jcb[0].isSelected()) {
            antwort = 0;
        } else {
            antwort = 1;
        }
//        if (jrb[2].isSelected() || jrb[3].isSelected() || jrb[4].isSelected()) {
        if (jrb[2].isSelected() || jrb[3].isSelected()) {
            if (jrb[2].isSelected()) {
                antwort = 0;
            } else if (jrb[3].isSelected()) {
                antwort = 1;
            } else {
                antwort = 2;
            }
            hmPdf.put(jrb[2].getName(), resultRadio1[antwort][0]);
            hmPdf.put(jrb[3].getName(), resultRadio1[antwort][1]);
//            hmPdf.put(jrb[4].getName(), resultRadio1[antwort][2]);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Bitte geben Sie an wie Sie die Datei (Zertifikatsanforderung) der ITSG übermitteln wollen");
            return;
        }
        if (jcb[0].isSelected()) {
            hmPdf.put(jcb[0].getName(), "Ja");
        } else {
            hmPdf.put(jcb[0].getName(), "Nein");
        }
        hmPdf.put("Datum", DatFunk.sHeute());
        try {
            doPdfFuellen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doPdfFuellen() throws Exception {
        String outFile = null;
        String vorlage = therapidir + File.separator + "defaults/vorlagen/" + "Zertifizierungsantrag.pdf";
        // System.out.println("ZertAntrag vorlage: "+vorlage);
        File f = new File(vorlage);

        if (!f.exists()) {
            vorlage = FileStatics.dirChooser(therapidir + File.separator + "defaults/vorlagen/",
                    "Bitte wählen Sie die Vorlage aus (Standard = Zertifizierungsantrag.pdf)");
            if (vorlage.equals("")) {
                JOptionPane.showMessageDialog(null, "Na dann eben nicht");
                return;
            }
        }
        try {
            PdfReader reader = new PdfReader(vorlage);
            outFile = vorlage.replace(".pdf", "_") + DatFunk.sHeute() + ".pdf";
            FileOutputStream out = new FileOutputStream(outFile);
            PdfStamper stamper = new PdfStamper(reader, out);
            AcroFields form = stamper.getAcroFields();
            Map fieldMap = form.getFields();
            Set keys = fieldMap.keySet();
            String fieldName = null, hmFieldName = null;
            System.out.println("ZertAntrag fields");
            for (Iterator it = keys.iterator(); it.hasNext();) {
                fieldName = (String) it.next();
                System.out.println(fieldName);
                System.out.println(fieldName);
                if (fieldName.contains("[0]")) { // Formular aus 2019: (topmostSubform[0].Page1[0].IK[0])
                    hmFieldName = fieldName.replaceAll("\\[0\\]", "");
                    if (hmFieldName.contains(".")) {
                        hmFieldName = hmFieldName.substring(hmFieldName.lastIndexOf(".") + 1, hmFieldName.length());
                    }
                } else {
                    hmFieldName = fieldName; // Formular ist das 2008er (IK)
                }
                try {
                    form.setField(fieldName, hmPdf.get(hmFieldName));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            stamper.setFormFlattening(true);
            stamper.close();
            reader.close();
            out.close();

            String text = "Der Antrag wurde generiert und in der Datei -> " + outFile + " <- gespeichert\n\n"
                    + "Soll der Antrag jetzt auf Ihrem Standarddrucker gedruckt werden?";
            int frage = JOptionPane.showConfirmDialog(null, text, "Achtung wichtige Benutzeranfrage",
                    JOptionPane.YES_NO_OPTION);
            if (frage == JOptionPane.YES_OPTION) {
                PDFDrucker.setup(outFile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Keine Vorlagendatei, oder Datei beschädigt");
        }
    }

    class MyStammFocusTraversalPolicy extends FocusTraversalPolicy {
        Vector<Component> order;

        public MyStammFocusTraversalPolicy(Vector<Component> order) {
            this.order = new Vector<Component>(order.size());
            this.order.addAll(order);
        }

        @Override
        public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
            int idx = (order.indexOf(aComponent) + 1) % order.size();
            return order.get(idx);
        }

        @Override
        public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
            int idx = order.indexOf(aComponent) - 1;
            if (idx < 0) {
                idx = order.size() - 1;
            }
            return order.get(idx);
        }

        @Override
        public Component getDefaultComponent(Container focusCycleRoot) {
            return order.get(0);
        }

        @Override
        public Component getLastComponent(Container focusCycleRoot) {
            return order.lastElement();
        }

        @Override
        public Component getFirstComponent(Container focusCycleRoot) {
            return order.get(0);
        }
    }

}
