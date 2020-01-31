package org.therapi.reha.patient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.ExUndHop;
import CommonTools.JCompTools;
import CommonTools.JRtaLabel;
import CommonTools.JRtaTextField;
import CommonTools.StringTools;
import commonData.ArztVec;
import commonData.Rezept;
import environment.Path;
import hauptFenster.Reha;
import rechteTools.Rechte;
import stammDatenTools.ArztTools;
import stammDatenTools.KasseTools;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;
import terminKalender.TerminFenster;

public class RezeptDaten extends JXPanel implements ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = -6994295488322966514L;
    // public JLabel[] rezlabs = {null,null,null,null,null,null,null,null,null};
    public JRtaTextField reznum = null;
    public JRtaTextField draghandler = null;
    public ImageIcon hbimg = null;
    public ImageIcon hbimg2 = null;
    public Vector<String> vecaktrez = null;
    public static boolean feddisch = false;
    private JPopupMenu jPopupMenu = null;

    private JMenuItem copyToBunker = null;
    private JRtaLabel hblab = null;

    public String[] rezart = { "Erstverordnung", "Folgeverordnung", "Folgev. außerhalb d.R." };

    public RezeptDaten(PatientHauptPanel eltern) {
        super();
        this.setOpaque(false);
        setBorder(null);
        setLayout(new BorderLayout());
        add(getDatenPanel(eltern), BorderLayout.CENTER);
        hbimg = SystemConfig.hmSysIcons.get("hausbesuch");
        hbimg2 = SystemConfig.hmSysIcons.get("hbmehrere");
        hblab.setHorizontalTextPosition(JLabel.LEFT);
        hblab.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {

                if ((arg0.getSource() instanceof JLabel) && (arg0.getClickCount() == 2)) {
                    if (!Rechte.hatRecht(Rechte.Rezept_editvoll, true)) {
                        return;
                    }
                    String anzhb = StringTools.NullTest(Reha.instance.patpanel.vecaktrez.get(64))
                                              .trim();
                    Object ret = JOptionPane.showInputDialog(null, "Geben Sie bitte die neue Anzahl für Hausbesuch ein",
                            anzhb);
                    if (ret == null) {
                        return;
                    }
                    if (!((String) ret).trim()
                                       .equals(anzhb)) {
                        hblab.setText(((String) ret).trim() + " *");
                        new ExUndHop().setzeStatement("update verordn set anzahlhb='" + ((String) ret).trim() + "' "
                                + "where rez_nr='" + reznum.getText() + "' LIMIT 1");
                        Reha.instance.patpanel.vecaktrez.set(64, ((String) ret).trim());
                    }
                }
            }
        });

    }

    public void setRezeptDaten(String reznummer, String sid) {
        RezeptDaten.feddisch = false;
        boolean reha = false;
        ArztVec verordnenderArzt = new ArztVec();
        Rezept dieseVO = new Rezept();
        // reznum.setText(reznummer);
        // reznum.repaint();
        final String xreznummer = reznummer;
        try {
            dieseVO.setVec_rez(Reha.instance.patpanel.vecaktrez);
            verordnenderArzt.init(dieseVO.getArztId());
            
            String diszi = RezTools.putRezNrGetDisziplin(dieseVO.getRezClass());
            int prgruppe = 0;
            if (prgruppe < 0) {
                prgruppe = 0;
            }
            try {
                prgruppe = Integer.parseInt(dieseVO.getPreisgruppe())-1;
            } catch (Exception ex) {
            }

            String stest = StringTools.NullTest(dieseVO.getHausbesuchS());
            String einzeln = StringTools.NullTest(dieseVO.getHbVollS());
            if (stest.equals("T")) {
                hblab.setText(StringTools.NullTest(dieseVO.getAnzHBS())+" *");
                hblab.setIcon((einzeln.equals("T") ? hbimg : hbimg2));
                hblab.setAlternateText(
                        "<html>" + (einzeln.equals("T")
                                ? "Hausbesuch einzeln (Privatwohnung/-haus)<br>Positionsnummer: "
                                        + SystemPreislisten.hmHBRegeln.get(diszi)
                                                                      .get(prgruppe)
                                                                      .get(0)
                                :

                                "Hausbesuch in einer sozialen Gemeinschaft (mehrere)<br>Positionsnummer: "
                                        + SystemPreislisten.hmHBRegeln.get(diszi)
                                                                      .get(prgruppe)
                                                                      .get(1))
                                + "</html>");

            } else {
                hblab.setText(null);
                hblab.setIcon(null);
            }

            Reha.instance.patpanel.rezlabs[2].setText("angelegt von: "+dieseVO.getAngelegtVon());
            if(StringTools.ZahlTest( dieseVO.getKtraeger()) >= 0 ){
                Reha.instance.patpanel.rezlabs[3].setForeground(Color.BLACK);
            } else {
                Reha.instance.patpanel.rezlabs[3].setForeground(Color.RED);
            }
            Reha.instance.patpanel.rezlabs[3].setText(StringTools.NullTest(dieseVO.getKtrName()));

            if(StringTools.ZahlTest( dieseVO.getArztId()) >= 0 ){
                Reha.instance.patpanel.rezlabs[4].setForeground(Color.BLACK);
            } else {
                Reha.instance.patpanel.rezlabs[4].setForeground(Color.RED);
            }
            Reha.instance.patpanel.rezlabs[4].setText(StringTools.NullTest(verordnenderArzt.getNNameLanr()));

            int test = dieseVO.getRezArt();
            if (test >= 0) {
                Reha.instance.patpanel.rezlabs[5].setText(rezart[test]);
                if (test == 2) {
                    stest = StringTools.NullTest(dieseVO.getBegrAdRS());
                    if (stest.equals("T")) {
                        Reha.instance.patpanel.rezlabs[6].setForeground(Color.BLACK);
                        Reha.instance.patpanel.rezlabs[6].setText("Begründung o.k.");
                    } else {
                        Reha.instance.patpanel.rezlabs[6].setForeground(Color.RED);
                        Reha.instance.patpanel.rezlabs[6].setText("Begründung fehlt");
                    }
                } else {
                    Reha.instance.patpanel.rezlabs[6].setText(" ");
                }
            } else {
                Reha.instance.patpanel.rezlabs[5].setText(" ");
                Reha.instance.patpanel.rezlabs[6].setText(" ");
            }
            stest = StringTools.NullTest(dieseVO.getArztberichtS());
            if (stest.equals("T")) {
                test = StringTools.ZahlTest(dieseVO.getArztBerichtID());
                if (test >= 0) {
                    Reha.instance.patpanel.rezlabs[7].setForeground(Color.BLACK);
                    Reha.instance.patpanel.rezlabs[7].setText("Therapiebericht o.k.");
                } else {
                    Reha.instance.patpanel.rezlabs[7].setForeground(Color.RED);
                    Reha.instance.patpanel.rezlabs[7].setText("Therapiebericht fehlt");
                }

            } else {
                Reha.instance.patpanel.rezlabs[7].setText(" ");
            }
            Vector<Vector<String>> preisvec = null;

            try {
                preisvec = SystemPreislisten.hmPreise.get(diszi)
                                                     .get(prgruppe);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Achtung Fehler beim Bezug der Preislisteninformation!\nKlasse: RezeptDaten");
                RezeptDaten.feddisch = true;
                return;
            }

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    int farbcode = dieseVO.getFarbCode();
                    if (farbcode > 0) {
                        reznum.setText(xreznummer);
                        reznum.setForeground((SystemConfig.vSysColsObject.get(0)
                                                                         .get(farbcode)[0]));
                        reznum.repaint();
                    } else {
                        reznum.setText(xreznummer);
                        reznum.setForeground(Color.BLUE);
                        reznum.repaint();
                    }
                }
            });
            reha = dieseVO.getRezNb().startsWith("RH");
            stest = StringTools.NullTest(dieseVO.getFrequenz());

            Reha.instance.patpanel.rezlabs[8].setText(
                    leistungTesten(0, preisvec, StringTools.ZahlTest(dieseVO.getArtDBehandl(1)))
                            + (reha ? "" : "  " + (dieseVO.getHmPos(1)
                                                          .equals("") ? "" : " (" + dieseVO.getHmPos(1) + ")")));

            if (stest.equals("")) {
                Reha.instance.patpanel.rezlabs[9].setForeground(Color.RED);
                Reha.instance.patpanel.rezlabs[9].setText(stest + "??? / Wo.");
            } else {
                Reha.instance.patpanel.rezlabs[9].setForeground(Color.BLACK);
                Reha.instance.patpanel.rezlabs[9].setText(stest + " / Wo.");
            }

            Reha.instance.patpanel.rezlabs[10].setText(
                    leistungTesten(0, preisvec, StringTools.ZahlTest(dieseVO.getArtDBehandl(2)))
                            + (reha ? "" : "  " + (dieseVO.getHmPos(2)
                                                          .equals("") ? "" : " (" + dieseVO.getHmPos(2) + ")")));
            Reha.instance.patpanel.rezlabs[11].setText(
                    leistungTesten(0, preisvec, StringTools.ZahlTest(dieseVO.getArtDBehandl(3)))
                            + (reha ? "" : "  " + (dieseVO.getHmPos(3)
                                                          .equals("") ? "" : " (" + dieseVO.getHmPos(3) + ")")));
            Reha.instance.patpanel.rezlabs[12].setText(
                    leistungTesten(0, preisvec, StringTools.ZahlTest(dieseVO.getArtDBehandl(4)))
                            + (reha ? "" : "  " + (dieseVO.getHmPos(4)
                                                          .equals("") ? "" : " (" + dieseVO.getHmPos(4) + ")")));
            stest = StringTools.NullTest(dieseVO.getIndiSchluessel());
            if ((stest.equals("") || stest.equals("kein IndiSchl."))) {
                if (!reha) {
                    Reha.instance.patpanel.rezlabs[13].setForeground(Color.RED);
                    Reha.instance.patpanel.rezlabs[13].setText("??? " + stest);
                } else {
                    Reha.instance.patpanel.rezlabs[13].setText("");
                }
            } else {
                if (!reha) {
                    Reha.instance.patpanel.rezlabs[13].setForeground(Color.BLACK);
                    Reha.instance.patpanel.rezlabs[13].setText(stest);
                } else {
                    Reha.instance.patpanel.rezlabs[13].setText("");
                }
            }

            stest = StringTools.NullTest(dieseVO.getDauer());
            if (stest.equals("")) {
                Reha.instance.patpanel.rezlabs[14].setForeground(Color.RED);
                Reha.instance.patpanel.rezlabs[14].setText("??? Min.");
            } else {
                Reha.instance.patpanel.rezlabs[14].setForeground(Color.BLACK);
                Reha.instance.patpanel.rezlabs[14].setText(stest + " Min.");
            }

            stest = StringTools.NullTest(dieseVO.getICD10())
                               .trim();
            if (stest.length() > 0) {
                stest = "1.ICD-10: " + stest;
                String stestIcd2 = StringTools.NullTest(dieseVO.getICD10_2())
                                           .trim();
                stest = stest + (stestIcd2.length() > 0 ? "  -  2.ICD-10: " + stestIcd2 : "");
                Reha.instance.patpanel.rezdiag.setText(stest + "\n" + StringTools.NullTest(dieseVO.getDiagn()));
            } else {
                Reha.instance.patpanel.rezdiag.setText(StringTools.NullTest(dieseVO.getDiagn()));
            }

            int zzbild = 0;
            try {
                zzbild = Integer.parseInt(dieseVO.getZzStat());
            } catch (Exception ex) {
                zzbild = 0;
                ex.printStackTrace();
            }
            int row = AktuelleRezepte.tabaktrez.getSelectedRow();
            if (row >= 0) {
                if (AktuelleRezepte.dtblm.getValueAt(row, 1) != Reha.instance.patpanel.imgzuzahl[zzbild]) {
                    org.therapi.reha.patient.AktuelleRezepte.setZuzahlImage(zzbild);
                }
            }

            try {
                RezTools.constructVirginHMap();
                ArztTools.constructArztHMap(dieseVO.getArztId());
                KasseTools.constructKasseHMap(dieseVO.getKtraeger());
                RezeptDaten.feddisch = true;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Fehler in ConstructHashMap (Modul:RezeptDaten)\nBitte verständigen Sie den Administrator und notieren Sie zuvor die Fehlermeldung");
                RezeptDaten.feddisch = true;
            }
            /*
             * new SwingWorker<Void,Void>(){
             * 
             * @Override protected Void doInBackground() throws Exception { try{
             * RezTools.constructVirginHMap();
             * ArztTools.constructArztHMap((String)Reha.instance.patpanel.vecaktrez.get(16))
             * ;
             * KasseTools.constructKasseHMap((String)Reha.instance.patpanel.vecaktrez.get(37
             * )); RezeptDaten.feddisch = true; }catch(Exception ex){
             * JOptionPane.showMessageDialog(null,
             * "Fehler in ConstructHashMap (Modul:RezeptDaten)\nBitte verständigen Sie den Administrator und notieren Sie zuvor die Fehlermeldung"
             * ); RezeptDaten.feddisch = true; } return null; } }.execute();
             */
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public String leistungTesten(int leistung, Vector<Vector<String>> preisevec, int veczahl) {
        String retwert = "----";
        if (veczahl == -1 || veczahl == 0) {
            return retwert;
        }
        for (int i = 0; i < preisevec.size(); i++) {
            if (Integer.valueOf((String) ((Vector) preisevec.get(i)).get(preisevec.get(i)
                                                                                  .size()
                    - 1)) == veczahl) {
                return StringTools.NullTest(Reha.instance.patpanel.vecaktrez.get(leistung + 3)) + "  *  "
                        + (String) ((Vector) preisevec.get(i)).get(1);
            }
        }

        return retwert;
    }

    public JScrollPane getDatenPanel(PatientHauptPanel eltern) {
        JScrollPane jscr = null;
        FormLayout lay = new FormLayout("fill:0:grow(0.33),2px,fill:0:grow(0.33),2px,fill:0:grow(0.33)",
                // FormLayout lay = new FormLayout("p,fill:0:grow(0.50),p,fill:0:grow(0.50),p",
                // 1.Sep 2.Sep 3.Sep
                // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25
                "p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p, 1dlu,p,1dlu,p,1dlu,p,1dlu,p,38dlu,1px");
        CellConstraints cc = new CellConstraints();
        PanelBuilder jpan = new PanelBuilder(lay);
        jpan.getPanel()
            .setOpaque(false);
        Font fontreznr = new Font("Tahoma", Font.BOLD, 16);
        Font fontbehandlung = new Font("Tahoma", Font.BOLD, 11);
        reznum = new JRtaTextField("GROSS", true);
        reznum.setText("  ");
        reznum.setFont(fontreznr);
        reznum.setForeground(Color.BLUE);
        reznum.setOpaque(false);
        reznum.setEditable(false);
        reznum.setBorder(null);
        reznum.setDragEnabled(true);

        reznum.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != java.awt.event.MouseEvent.BUTTON3) {
                    int farbcode = StringTools.ZahlTest(Reha.instance.patpanel.vecaktrez.get(57));
                    TerminFenster.DRAG_MODE = TerminFenster.DRAG_UNKNOWN;
                    draghandler.setText("TERMDATEXT" + "°" + Reha.instance.patpanel.patDaten.get(0)
                                                                                            .substring(0, 1)
                            + "-" + Reha.instance.patpanel.patDaten.get(2) + ","
                            + Reha.instance.patpanel.patDaten.get(3) + "°" + String.valueOf(reznum.getText())
                            + (farbcode > 0 ? (String) SystemConfig.vSysColsCode.get(farbcode) : "") + "°"
                            + Reha.instance.patpanel.rezlabs[14].getText());
                    JComponent c = draghandler;
                    TransferHandler th = c.getTransferHandler();
                    th.exportAsDrag(c, e, TransferHandler.COPY); // TransferHandler.COPY
                    if (Path.Instance.isLinux()) {
                        Reha.dragDropComponent = draghandler;
                    }

                } else {
                    ZeigePopupMenu(e);
                }
            }
        });
        draghandler = new JRtaTextField("GROSS", true);
        draghandler.setTransferHandler(new TransferHandler("text"));
        /*
         * Reha.instance.patpanel.rezlabs[0] = new JLabel();
         * Reha.instance.patpanel.rezlabs[0].setFont(fontreznr);
         * Reha.instance.patpanel.rezlabs[0].setName("rezeptnummer");
         * Reha.instance.patpanel.rezlabs[0].setForeground(Color.BLUE);
         * Reha.instance.patpanel.rezlabs[0].setText("KG57606");
         */

        hblab = new JRtaLabel(" ");
        hblab.setName("hausbesuch");
        hblab.setIcon(hbimg);
        /*
         * eltern.rezlabs[1] = new JRtaLabel(" ");
         * eltern.rezlabs[1].setName("hausbesuch"); eltern.rezlabs[1].setIcon(hbimg);
         */
        eltern.rezlabs[2] = new JLabel(" ");
        eltern.rezlabs[2].setName("angelegt");

        eltern.rezlabs[3] = new JLabel(" ");
        eltern.rezlabs[3].setName("kostentraeger");

        eltern.rezlabs[4] = new JLabel(" ");
        eltern.rezlabs[4].setName("arzt");

        eltern.rezlabs[5] = new JLabel(" ");
        eltern.rezlabs[5].setName("verornungsart");

        eltern.rezlabs[6] = new JLabel(" ");
        eltern.rezlabs[6].setName("begruendung");
        eltern.rezlabs[6].setForeground(Color.RED);

        eltern.rezlabs[7] = new JLabel(" ");
        eltern.rezlabs[7].setName("arztbericht");

        eltern.rezlabs[8] = new JLabel("");
        eltern.rezlabs[8].setName("behandlung1");
        eltern.rezlabs[8].setFont(fontbehandlung);
        eltern.rezlabs[9] = new JLabel(" ");
        eltern.rezlabs[9].setName("frequenz");
        eltern.rezlabs[9].setFont(fontbehandlung);

        eltern.rezlabs[10] = new JLabel(" ");
        eltern.rezlabs[10].setName("behandlung2");
        eltern.rezlabs[10].setFont(fontbehandlung);
        eltern.rezlabs[11] = new JLabel(" ");
        eltern.rezlabs[11].setName("behandlung3");
        eltern.rezlabs[11].setFont(fontbehandlung);
        eltern.rezlabs[12] = new JLabel(" ");
        eltern.rezlabs[12].setName("behandlung4");
        eltern.rezlabs[12].setFont(fontbehandlung);

        eltern.rezlabs[13] = new JLabel(" ");
        eltern.rezlabs[13].setName("indikation");
        eltern.rezlabs[13].setFont(fontbehandlung);

        eltern.rezlabs[14] = new JLabel(" ");
        eltern.rezlabs[14].setName("Dauer");
        eltern.rezlabs[14].setFont(fontbehandlung);

        eltern.rezdiag = new JTextArea("");
        eltern.rezdiag.setOpaque(false);
        eltern.rezdiag.setFont(new Font("Courier", Font.PLAIN, 11));
        eltern.rezdiag.setForeground(Color.BLUE);
        eltern.rezdiag.setLineWrap(true);
        eltern.rezdiag.setName("notitzen");
        eltern.rezdiag.setWrapStyleWord(true);
        eltern.rezdiag.setEditable(false);

        jpan.add(reznum, cc.xy(1, 1));
        // jpan.add(Reha.instance.patpanel.rezlabs[0],cc.xy(1, 1));

        jpan.add(hblab, cc.xy(3, 1));
        // jpan.add(eltern.rezlabs[1],cc.xy(3, 1));

        jpan.add(eltern.rezlabs[2], cc.xy(5, 1));

        jpan.addSeparator("", cc.xyw(1, 3, 5));

        jpan.add(eltern.rezlabs[3], cc.xy(1, 5));
        jpan.add(eltern.rezlabs[4], cc.xy(5, 5));

        jpan.add(eltern.rezlabs[5], cc.xy(1, 7));
        jpan.add(eltern.rezlabs[6], cc.xy(3, 7));
        jpan.add(eltern.rezlabs[7], cc.xy(5, 7));

        jpan.addSeparator("", cc.xyw(1, 9, 5));

        jpan.add(eltern.rezlabs[8], cc.xy(1, 11));
        jpan.add(eltern.rezlabs[9], cc.xy(3, 11));
        jpan.add(eltern.rezlabs[14], cc.xy(5, 11));
        jpan.add(eltern.rezlabs[10], cc.xy(1, 13));
        jpan.add(eltern.rezlabs[11], cc.xy(1, 15));
        jpan.add(eltern.rezlabs[12], cc.xy(1, 17));

        jpan.addSeparator("", cc.xyw(1, 19, 5));

        jpan.add(eltern.rezlabs[13], cc.xy(1, 21));
        // jpan.add(Reha.instance.patpanel.rezlabs[14],cc.xy(1, 23));
        JXPanel diagpan = new JXPanel(new BorderLayout());
        diagpan.setOpaque(false);
        JScrollPane jscrdiag = JCompTools.getTransparentScrollPane(eltern.rezdiag);
        jscrdiag.validate();
        diagpan.add(jscrdiag, BorderLayout.CENTER);

        // jpan.add(jscrdiag,cc.xywh(3, 21,3,4));
        jpan.add(diagpan, cc.xywh(3, 21, 3, 4, CellConstraints.FILL, CellConstraints.FILL));

        jscr = JCompTools.getTransparentScrollPane(jpan.getPanel());
        jscr.getVerticalScrollBar()
            .setUnitIncrement(15);
        jscr.validate();
        return jscr;
    }

    private void ZeigePopupMenu(java.awt.event.MouseEvent me) {
        JPopupMenu jPop = getTerminPopupMenu();
        jPop.show(me.getComponent(), me.getX(), me.getY());
    }

    private JPopupMenu getTerminPopupMenu() {
        if (jPopupMenu == null) {
            jPopupMenu = new JPopupMenu();
            jPopupMenu.add(copyToBunker());
        }
        return jPopupMenu;
    }

    private JMenuItem copyToBunker() {
        if (copyToBunker == null) {
            copyToBunker = new JMenuItem();
            copyToBunker.setText("Rezept kopieren");
            copyToBunker.setIcon(SystemConfig.hmSysIcons.get("bunker"));
            copyToBunker.setRolloverEnabled(true);
            copyToBunker.setEnabled(true);
            copyToBunker.addActionListener(this);
        }
        return copyToBunker;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand()
             .equals("Rezept kopieren")) {
            int farbcode = StringTools.ZahlTest(Reha.instance.patpanel.vecaktrez.get(57));
            TerminFenster.DRAG_MODE = TerminFenster.DRAG_UNKNOWN;
            String dragText = Reha.instance.patpanel.patDaten.get(0)
                                                             .substring(0, 1)
                    + "-" + Reha.instance.patpanel.patDaten.get(2) + "," + Reha.instance.patpanel.patDaten.get(3) + "°"
                    + reznum.getText() + (farbcode > 0 ? (String) SystemConfig.vSysColsCode.get(farbcode) : "") + "°"
                    + Reha.instance.patpanel.rezlabs[14].getText();
            Reha.instance.copyLabel.setText(String.valueOf(dragText));
            Reha.instance.bunker.setText("TERMDATEXT" + "°" + String.valueOf(dragText));
            String[] daten = { (Reha.instance.patpanel.patDaten.get(0)
                                                               .startsWith("F") ? "F-" : "H-")
                    + Reha.instance.patpanel.patDaten.get(2) + "," + Reha.instance.patpanel.patDaten.get(3),
                    Reha.instance.patpanel.vecaktrez.get(1)
                            + (farbcode > 0 ? (String) SystemConfig.vSysColsCode.get(farbcode) : ""),
                    Reha.instance.patpanel.vecaktrez.get(47) };

            if (Reha.instance.terminpanel != null) {
                Reha.instance.terminpanel.setDatenVonExternInSpeicherNehmen(daten.clone());
                Reha.instance.shiftLabel.setText(
                        "bereit für F2= " + daten[0] + "°" + daten[1] + "°" + daten[2] + " Min.");
            } else {
                Reha.instance.shiftLabel.setText(" ");
            }
        }

    }
}
