package org.therapi.reha.patient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.*;

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
import commonData.Rezeptvector;
import environment.Path;
import hauptFenster.Reha;
import rechteTools.Rechte;
import stammDatenTools.ArztTools;
import stammDatenTools.KasseTools;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;
import terminKalender.TerminFenster;

class RezeptDaten extends JXPanel implements ActionListener {
    private static final String FOLGEV_AUSSERHALB_D_R = "Folgev. außerhalb d.R.";
    private static final String FOLGEVERORDNUNG = "Folgeverordnung";
    private static final String ERSTVERORDNUNG = "Erstverordnung";
    /**
     *
     */
    private static final long serialVersionUID = -6994295488322966514L;
    private JRtaTextField reznum = null;
    private JRtaTextField draghandler = null;
    private ImageIcon hbimg = null;
    private ImageIcon hbimg2 = null;

    static boolean feddisch = false;
    private JPopupMenu jPopupMenu = null;

    private JMenuItem copyToBunker = null;
    private JRtaLabel hblab = null;

    private String[] rezart = { ERSTVERORDNUNG, FOLGEVERORDNUNG, FOLGEV_AUSSERHALB_D_R };

    private JLabel angelegtLbl;
    private JLabel kostentraegerLbl;
    private JLabel arztLbl;
    private JLabel verordnungsArtLbl;
    private JLabel begruendungLbl;
    private JLabel arztBerichtLbl;
    private JLabel behandlung1Lbl;
    private JLabel frequenzLbl;
    private JLabel behandlung2Lbl;
    private JLabel behandlung3Lbl;
    private JLabel behandlung4Lbl;
    private JLabel indikationLbl;
    private JLabel dauerLbl;

    RezeptDaten(PatientHauptPanel eltern) {
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

    void setRezeptDaten(String reznummer, String sid) {
        RezeptDaten.feddisch = false;
        boolean reha = false;
        ArztVec verordnenderArzt = new ArztVec();
        Rezeptvector dieseVO = new Rezeptvector();
        final String xreznummer = reznummer;
        try {
            dieseVO.setVec_rez(Reha.instance.patpanel.vecaktrez);
            verordnenderArzt.init(dieseVO.getArztId());

            String diszi = RezTools.getDisziplinFromRezNr(dieseVO.getRezClass());
            int prgruppe = 0;
            try {
                prgruppe = Integer.parseInt(dieseVO.getPreisgruppe()) - 1;
            } catch (Exception ex) {
            }

            String stest = StringTools.NullTest(dieseVO.getHausbesuchS());
            String einzeln = StringTools.NullTest(dieseVO.getHbVollS());
            if (stest.equals("T")) {
                hblab.setText(StringTools.NullTest(dieseVO.getAnzHBS()) + " *");
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

            angelegtLbl.setText("angelegt von: " + dieseVO.getAngelegtVon());
            if (StringTools.ZahlTest(dieseVO.getKtraeger()) >= 0) {
                kostentraegerLbl.setForeground(Color.BLACK);
            } else {
                kostentraegerLbl.setForeground(Color.RED);
            }
            kostentraegerLbl.setText(StringTools.NullTest(dieseVO.getKtrName()));

            if (StringTools.ZahlTest(dieseVO.getArztId()) >= 0) {
                arztLbl.setForeground(Color.BLACK);
            } else {
                arztLbl.setForeground(Color.RED);
            }
            arztLbl.setText(StringTools.NullTest(verordnenderArzt.getNNameLanr()));

            int test = dieseVO.getRezArt();
            if (test >= 0) {
                verordnungsArtLbl.setText(rezart[test]);
                if (test == 2) {
                    stest = StringTools.NullTest(dieseVO.getBegrAdRS());
                    if (stest.equals("T")) {
                        begruendungLbl.setForeground(Color.BLACK);
                        begruendungLbl.setText("Begründung o.k.");
                    } else {
                        begruendungLbl.setForeground(Color.RED);
                        begruendungLbl.setText("Begründung fehlt");
                    }
                } else {
                    begruendungLbl.setText(" ");
                }
            } else {
                verordnungsArtLbl.setText(" ");
                begruendungLbl.setText(" ");
            }
            stest = StringTools.NullTest(dieseVO.getArztberichtS());
            if (stest.equals("T")) {
                test = StringTools.ZahlTest(dieseVO.getArztBerichtID());
                if (test >= 0) {
                    arztBerichtLbl.setForeground(Color.BLACK);
                    arztBerichtLbl.setText("Therapiebericht o.k.");
                } else {
                    arztBerichtLbl.setForeground(Color.RED);
                    arztBerichtLbl.setText("Therapiebericht fehlt");
                }

            } else {
                arztBerichtLbl.setText(" ");
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
            reha = dieseVO.getRezNb()
                          .startsWith("RH");
            stest = StringTools.NullTest(dieseVO.getFrequenz());
            final int INDEX_HEILMITTEL_1 = 1;
            final int INDEX_HEILMITTEL_2 = 2;
            final int INDEX_HEILMITTEL_3 = 3;
            final int INDEX_HEILMITTEL_4 = 4;

            String anzeigeHMPosition1 = showHM(dieseVO, preisvec, INDEX_HEILMITTEL_1);
            behandlung1Lbl.setText(anzeigeHMPosition1);

            if (stest.equals("")) {
                frequenzLbl.setForeground(Color.RED);
                frequenzLbl.setText(stest + "??? / Wo.");
            } else {
                frequenzLbl.setForeground(Color.BLACK);
                frequenzLbl.setText(stest + " / Wo.");
            }

            String anzeigeHMPosition2 = showHM(dieseVO, preisvec, INDEX_HEILMITTEL_2);
            behandlung2Lbl.setText(anzeigeHMPosition2);
            String anzeigeHMPosition3 = showHM(dieseVO, preisvec, INDEX_HEILMITTEL_3);
            behandlung3Lbl.setText(anzeigeHMPosition3);
            String anzeigeHMPosition4 = showHM(dieseVO, preisvec, INDEX_HEILMITTEL_4);
            behandlung4Lbl.setText(anzeigeHMPosition4);

            stest = StringTools.NullTest(dieseVO.getIndiSchluessel());
            if ((stest.equals("") || stest.equals("kein IndiSchl."))) {
                if (!reha) {
                    indikationLbl.setForeground(Color.RED);
                    indikationLbl.setText("??? " + stest);
                } else {
                    indikationLbl.setText("");
                }
            } else {
                if (!reha) {
                    indikationLbl.setForeground(Color.BLACK);
                    indikationLbl.setText(stest);
                } else {
                    indikationLbl.setText("");
                }
            }

            stest = StringTools.NullTest(dieseVO.getDauer());
            if (stest.equals("")) {
                dauerLbl.setForeground(Color.RED);
                dauerLbl.setText("??? Min.");
            } else {
                dauerLbl.setForeground(Color.BLACK);
                dauerLbl.setText(stest + " Min.");
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
            setzeZuzahlungsbild(zzbild);

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
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void setzeZuzahlungsbild(int zzbild) {
        int row = AktuelleRezepte.tabelleaktrez.getSelectedRow();
        if (row >= 0) {
            if (AktuelleRezepte.dtblm.getValueAt(row, 1) != Reha.instance.patpanel.imgzuzahl[zzbild]) {
                org.therapi.reha.patient.AktuelleRezepte.setZuzahlImage(zzbild);
            }
        }
    }

    /**
     * @param dieseVO  - Rezept-record
     * @param preisvec - Preisliste
     * @param idxHM    - Index des HM im Rezept
     * @return String 'anz * kuerzel (HM-Position)' oder Platzhalter '----'
     */
    private String showHM(Rezeptvector dieseVO, Vector<Vector<String>> preisvec, int idxHM) {
        String retVal = "----";
        // indices for preisvec/priceListEntry access
        final int KUERZEL = 1;
        final int ID = 9;

        if (!dieseVO.getHmPos(idxHM)
                    .equals("")) {
            int idOfPricelistEntry = StringTools.ZahlTest(dieseVO.getArtDBehandl(idxHM));

            if (idOfPricelistEntry > 0) {
                for (int i = 0; i < preisvec.size(); i++) {
                    String priceListEntry[] = new String[preisvec.get(i)
                                                                 .size()];
                    preisvec.get(i)
                            .toArray(priceListEntry);
                    int thisID = Integer.valueOf(priceListEntry[ID]);
                    if (thisID == idOfPricelistEntry) {
                        retVal = StringTools.NullTest(dieseVO.getAnzBehS(idxHM)) + "  *  " + priceListEntry[KUERZEL];
                        if (!dieseVO.getRezNb()
                                    .startsWith("RH")) {
                            retVal = retVal + " (" + dieseVO.getHmPos(idxHM) + ")";
                        }
                    }
                }
            }
        }
        return retVal;
    }

    private JScrollPane getDatenPanel(PatientHauptPanel eltern) {
        JScrollPane jscr = null;
        FormLayout lay = new FormLayout("fill:0:grow(0.33),2px,fill:0:grow(0.33),2px,fill:0:grow(0.33)",
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
                    draghandler.setText("TERMDATEXT" + "°" + Reha.instance.patpanel.getPatDaten().get(0)
                                                                                            .substring(0, 1)
                            + "-" + Reha.instance.patpanel.getPatDaten().get(2) + ","
                            + Reha.instance.patpanel.getPatDaten().get(3) + "°" + String.valueOf(reznum.getText())
                            + (farbcode > 0 ? (String) SystemConfig.vSysColsCode.get(farbcode) : "") + "°"
                            + dauerLbl.getText());
                    JComponent c = draghandler;
                    TransferHandler th = c.getTransferHandler();
                    th.exportAsDrag(c, e, TransferHandler.COPY);
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

        hblab = new JRtaLabel(" ");
        hblab.setName("hausbesuch");
        hblab.setIcon(hbimg);

        angelegtLbl = new JLabel(" ");
        angelegtLbl.setName("angelegt");

        kostentraegerLbl = new JLabel(" ");
        kostentraegerLbl.setName("kostentraeger");

        arztLbl = new JLabel(" ");
        arztLbl.setName("arzt");

        verordnungsArtLbl = new JLabel(" ");
        verordnungsArtLbl.setName("verornungsart");

        begruendungLbl = new JLabel(" ");
        begruendungLbl.setName("begruendung");
        begruendungLbl.setForeground(Color.RED);

        arztBerichtLbl = new JLabel(" ");
        arztBerichtLbl.setName("arztbericht");

        behandlung1Lbl = new JLabel("");
        behandlung1Lbl.setName("behandlung1");
        behandlung1Lbl.setFont(fontbehandlung);
        frequenzLbl = new JLabel(" ");
        frequenzLbl.setName("frequenz");
        frequenzLbl.setFont(fontbehandlung);

        behandlung2Lbl = new JLabel(" ");
        behandlung2Lbl.setName("behandlung2");
        behandlung2Lbl.setFont(fontbehandlung);
        behandlung3Lbl = new JLabel(" ");
        behandlung3Lbl.setName("behandlung3");
        behandlung3Lbl.setFont(fontbehandlung);
        behandlung4Lbl = new JLabel(" ");
        behandlung4Lbl.setName("behandlung4");
        behandlung4Lbl.setFont(fontbehandlung);

        indikationLbl = new JLabel(" ");
        indikationLbl.setName("indikation");
        indikationLbl.setFont(fontbehandlung);

        dauerLbl = new JLabel(" ");
        dauerLbl.setName("Dauer");
        dauerLbl.setFont(fontbehandlung);

        eltern.rezdiag = new JTextArea("");
        eltern.rezdiag.setOpaque(false);
        eltern.rezdiag.setFont(new Font("Courier", Font.PLAIN, 11));
        eltern.rezdiag.setForeground(Color.BLUE);
        eltern.rezdiag.setLineWrap(true);
        eltern.rezdiag.setName("notitzen");
        eltern.rezdiag.setWrapStyleWord(true);
        eltern.rezdiag.setEditable(false);

        jpan.add(reznum, cc.xy(1, 1));

        jpan.add(hblab, cc.xy(3, 1));

        jpan.add(angelegtLbl, cc.xy(5, 1));

        jpan.addSeparator("", cc.xyw(1, 3, 5));

        jpan.add(kostentraegerLbl, cc.xy(1, 5));
        jpan.add(arztLbl, cc.xy(5, 5));

        jpan.add(verordnungsArtLbl, cc.xy(1, 7));
        jpan.add(begruendungLbl, cc.xy(3, 7));
        jpan.add(arztBerichtLbl, cc.xy(5, 7));

        jpan.addSeparator("", cc.xyw(1, 9, 5));

        jpan.add(behandlung1Lbl, cc.xy(1, 11));
        jpan.add(frequenzLbl, cc.xy(3, 11));
        jpan.add(dauerLbl, cc.xy(5, 11));
        jpan.add(behandlung2Lbl, cc.xy(1, 13));
        jpan.add(behandlung3Lbl, cc.xy(1, 15));
        jpan.add(behandlung4Lbl, cc.xy(1, 17));

        jpan.addSeparator("", cc.xyw(1, 19, 5));

        jpan.add(indikationLbl, cc.xy(1, 21));
        JXPanel diagpan = new JXPanel(new BorderLayout());
        diagpan.setOpaque(false);
        JScrollPane jscrdiag = JCompTools.getTransparentScrollPane(eltern.rezdiag);
        jscrdiag.validate();
        diagpan.add(jscrdiag, BorderLayout.CENTER);

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
            String dragText = Reha.instance.patpanel.getPatDaten().get(0)
                                                             .substring(0, 1)
                    + "-" + Reha.instance.patpanel.getPatDaten().get(2) + "," + Reha.instance.patpanel.getPatDaten().get(3) + "°"
                    + reznum.getText() + (farbcode > 0 ? (String) SystemConfig.vSysColsCode.get(farbcode) : "") + "°"
                    + dauerLbl.getText();
            Reha.instance.copyLabel.setText(String.valueOf(dragText));
            Reha.instance.bunker.setText("TERMDATEXT" + "°" + String.valueOf(dragText));
            String[] daten = { (Reha.instance.patpanel.getPatDaten().get(0)
                                                               .startsWith("F") ? "F-" : "H-")
                    + Reha.instance.patpanel.getPatDaten().get(2) + "," + Reha.instance.patpanel.getPatDaten().get(3),
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

    public JLabel getTherapieBerichtlabel() {
        return arztBerichtLbl;
    }
}
