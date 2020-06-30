package patientenFenster.rezepte;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;

import org.jdesktop.swingx.JXPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.therapi.reha.patient.RezeptDaten;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.DateTimeFormatters;
import CommonTools.JCompTools;
import CommonTools.JRtaLabel;
import CommonTools.JRtaTextField;
import CommonTools.StringTools;
import commonData.ArztVec;
import commonData.Rezeptvector;
import core.Disziplin;
import hauptFenster.Reha;
import mandant.IK;
import rezept.Rezept;
import rezept.RezeptDto;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;

/**
 * Class used to extract certain fields of a Rezept and display them as a sort of summary
 *  (in the PatientenFenster below the RezeptListe)
 * 
 */
public class RezeptDatenDarstellen extends JXPanel{
    static final long serialVersionUID = 1;
    private static final Logger logger = LoggerFactory.getLogger(RezeptDatenDarstellen.class);
    
    /**
     * 0 RezNr?? - 1 hausbesuch - 2 angelegt - 3 kostentraeger - 4 arzt - 5 verornungsart - 6 begruendung - 7 arztbericht
     * 8 behandlung1 - 9 frequenz - 10 behandlung2 - 11  behandlung3 - 12  behandlung4 - 13  indikation - 14  Dauer
     * 15 LastEditor - 16 LastEditDate
     */
    private JLabel[] rezlabs = new JLabel[17];
    
    private Rezept rez;
    private String rezNr;
    private IK ik;
    private boolean aktuelle;
    
    // Move to some better place:
    private JRtaLabel hblab = null;
    private ImageIcon hbimg = null;
    private ImageIcon hbimg2 = null;
    private JRtaTextField reznum = null;
    private JRtaTextField draghandler = null;
    private JTextArea rezdiag = null;
    private JScrollPane jscr = null;
    
    /**
     * Provide me an Ik, so that I can grab a Rezept from DB and extract information.
     * Provide the boolean Aktuelle so that I can choose whether you want current or
     * historical Rezepte.
     * 
     * @param RezNr     The RezeptNr of the Rezept to extract information from
     * @param Aktuelle  Is the rezept in aktuelle (verordn) or Historie (LZA)
     * @param Ik        The IK to use when getting the Rezept
     */
    public RezeptDatenDarstellen(String RezNr, boolean Aktuelle, IK Ik) {
        super();
        rezNr = RezNr;
        ik = Ik;
        aktuelle = Aktuelle;
        hbimg = SystemConfig.hmSysIcons.get("hausbesuch");
        hbimg2 = SystemConfig.hmSysIcons.get("hbmehrere");
        
        this.setOpaque(false);
        setBorder(null);
        setLayout(new BorderLayout());
        add(getDatenScrlPane(), BorderLayout.CENTER);
        validate();
    }

    /**
     * Call this method to update the panel with a different Rezept
     * 
     * @param RezNr     The RezeptNr used to retrieve the Rezept(-data)
     * @param Aktuelle  Is the Rezept in aktuelle (TRUE == verordn) or Hist. (LZA)?
     */
    public void updateDatenPanel(String RezNr, boolean Aktuelle) {
        rezNr = RezNr;
        aktuelle = Aktuelle;
        reznum.setText(rezNr);
        if (rezNr != null && !rezNr.isEmpty()) {
            logger.debug("Update on valid rezNr " + rezNr);
            Rezept rez = (aktuelle ? grabAktRez() : grabHistRez());
            if (rez == null) {
                logger.error("Couldn't find Rezept in " + RezNr + (Aktuelle ? "aktuelle " : "historische ") + "Rezepte.");
                return;
            }
            logger.debug("RezNr from rez: " + rez.getRezNr());
            fillInRezData(rez);
            activateRezNumField();
        } else {
            logger.debug("Update called on invalid RezNr: " + rezNr);
            deactivateRezNumField();
        }
        validate();
    }
        
    private JScrollPane getDatenScrlPane() {
        JScrollPane jscr = null;
                                     //        1            2         3           4        5
        FormLayout lay = new FormLayout("fill:0:grow(0.33),2px,fill:0:grow(0.33),2px,fill:0:grow(0.33)",
                // FormLayout lay = new FormLayout("p,fill:0:grow(0.50),p,fill:0:grow(0.50),p",
                //      1.Sep                2.Sep                              3.Sep
              // 1  2   3  4   5  6   7  8   9  10 11 12  13  14 15  16  17 18  19 20  21 22  23  24    25  26 27
              //"p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,5dlu,p,1dlu,p,1dlu,p,1dlu,p,5dlu,p,5dlu,p,1dlu,p,20dlu:g,1px,p,1dlu");
                "p,2dlu,p,2dlu,p,1dlu,p,2dlu,p,2dlu,p,1dlu,p,1dlu,p,1dlu,p,2dlu,p,2dlu,p,1dlu,p,26dlu,1px,p,1dlu");
        //      "p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,38dlu,1px,p,1dlu");
        CellConstraints cc = new CellConstraints();
        PanelBuilder jpan = new PanelBuilder(lay);
        jpan.getPanel()
            .setOpaque(false);
        Font fontbehandlung = new Font("Tahoma", Font.BOLD, 11);
        Font fontreznr = new Font("Tahoma", Font.BOLD, 16);

        reznum = new JRtaTextField("GROSS", true);
        reznum.setText("  ");
        reznum.setFont(fontreznr);
        reznum.setForeground(Color.BLUE);
        reznum.setOpaque(false);
        reznum.setEditable(false);
        reznum.setBorder(null);
        /*
         * rezlabs[0] = new JLabel(); rezlabs[0].setFont(fontreznr);
         * rezlabs[0].setName("rezeptnummer"); rezlabs[0].setForeground(Color.BLUE);
         * rezlabs[0].setText("KG57606");
         */

        rezlabs[1] = new JLabel(" ");
        rezlabs[1].setHorizontalTextPosition(JLabel.LEFT);
        rezlabs[1].setName("hausbesuch");
        rezlabs[1].setIcon(hbimg);

        rezlabs[2] = new JLabel(" ");
        rezlabs[2].setName("angelegt");

        rezlabs[3] = new JLabel(" ");
        rezlabs[3].setName("kostentraeger");

        rezlabs[4] = new JLabel(" ");
        rezlabs[4].setName("arzt");

        rezlabs[5] = new JLabel(" ");
        rezlabs[5].setName("verornungsart");

        rezlabs[6] = new JLabel(" ");
        rezlabs[6].setName("begruendung");
        rezlabs[6].setForeground(Color.RED);

        rezlabs[7] = new JLabel(" ");
        rezlabs[7].setName("arztbericht");
        rezlabs[7].setVisible(false);

        rezlabs[8] = new JLabel("");
        rezlabs[8].setName("behandlung1");
        rezlabs[8].setFont(fontbehandlung);
        rezlabs[9] = new JLabel(" ");
        rezlabs[9].setName("frequenz");
        rezlabs[9].setFont(fontbehandlung);

        rezlabs[10] = new JLabel(" ");
        rezlabs[10].setName("behandlung2");
        rezlabs[10].setFont(fontbehandlung);
        rezlabs[11] = new JLabel(" ");
        rezlabs[11].setName("behandlung3");
        rezlabs[11].setFont(fontbehandlung);
        rezlabs[12] = new JLabel(" ");
        rezlabs[12].setName("behandlung4");
        rezlabs[12].setFont(fontbehandlung);

        rezlabs[13] = new JLabel(" ");
        rezlabs[13].setName("indikation");
        rezlabs[13].setFont(fontbehandlung);

        rezlabs[14] = new JLabel(" ");
        rezlabs[14].setName("Dauer");
        rezlabs[14].setFont(fontbehandlung);

        rezlabs[15] = new JLabel(" ");
        rezlabs[15].setName("lasteditor");
        rezlabs[15].setForeground(Color.GRAY);
        rezlabs[16] = new JLabel(" ");
        rezlabs[16].setName("lasteditdate");
        rezlabs[16].setForeground(Color.GRAY);
        
        rezdiag = new JTextArea("");
        rezdiag.setOpaque(false);
        rezdiag.setFont(new Font("Courier", Font.PLAIN, 11));
        rezdiag.setForeground(Color.BLUE);
        rezdiag.setLineWrap(true);
        rezdiag.setName("notitzen");
        rezdiag.setWrapStyleWord(true);
        rezdiag.setEditable(false);

        jpan.add(reznum, cc.xy(1, 1));
        // jpan.add(rezlabs[0],cc.xy(1, 1));
        jpan.add(rezlabs[1], cc.xy(3, 1));
        jpan.add(rezlabs[2], cc.xy(5, 1));

        jpan.addSeparator("", cc.xyw(1, 3, 5));

        jpan.add(rezlabs[3], cc.xy(1, 5));
        jpan.add(rezlabs[4], cc.xy(5, 5));

        jpan.add(rezlabs[5], cc.xy(1, 7));
        jpan.add(rezlabs[6], cc.xy(3, 7));
        jpan.add(rezlabs[7], cc.xy(5, 7));

        jpan.addSeparator("", cc.xyw(1, 9, 5));

        jpan.add(rezlabs[8], cc.xy(1, 11));
        jpan.add(rezlabs[9], cc.xy(3, 11));
        jpan.add(rezlabs[14], cc.xy(5, 11));
        jpan.add(rezlabs[10], cc.xy(1, 13));
        jpan.add(rezlabs[11], cc.xy(1, 15));
        jpan.add(rezlabs[12], cc.xy(1, 17));

        jpan.addSeparator("", cc.xyw(1, 19, 5));

        jpan.add(rezlabs[13], cc.xy(1, 21));
        // rezlabs[14] added between 9 & 10...
                
        JScrollPane jscrdiag = JCompTools.getTransparentScrollPane(rezdiag);
        jscrdiag.validate();
        jpan.add(jscrdiag, cc.xywh(3, 21, 3, 4));

        jpan.addSeparator("", cc.xyw(1, 25, 5));
        
        JLabel labLastEdit = new JLabel("Zuletzt bearbeitet durch:");
        labLastEdit.setForeground(Color.GRAY);
        jpan.add(labLastEdit, cc.xy(1, 26));
        jpan.add(rezlabs[15], cc.xy(3, 26));
        jpan.add(rezlabs[16], cc.xy(5, 26));

        jscr = JCompTools.getTransparentScrollPane(jpan.getPanel());
        jscr.getVerticalScrollBar()
            .setUnitIncrement(15);
        jscr.validate();
        return jscr;
    }

    private void activateRezNumField() {
        
        reznum.setDragEnabled(true);
        reznum.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                draghandler.setText(Reha.instance.patpanel.patDaten.get(0)
                                                                   .substring(0, 1)
                        + "-" + Reha.instance.patpanel.patDaten.get(2) + "," + Reha.instance.patpanel.patDaten.get(3)
                        + "\u00b0" + reznum.getText() + "\u00b0" + rezlabs[14].getText());
                JComponent c = draghandler;
                TransferHandler th = c.getTransferHandler();
                th.exportAsDrag(c, e, TransferHandler.COPY); // TransferHandler.COPY
            }
        });
        draghandler = new JRtaTextField("GROSS", true);
        draghandler.setTransferHandler(new TransferHandler("text"));

    }
    
    private void deactivateRezNumField() {
        if (reznum.getMouseListeners() != null) {
            for (MouseListener ml : reznum.getMouseListeners()) {
                reznum.removeMouseListener(ml);
            }
        }
        reznum.setDragEnabled(false);
    }
    
    private JScrollPane getDatenScrPane() {
        JScrollPane pane = new JScrollPane();
        Component table = null;
        JScrollPane pane1 =new JScrollPane(table);
        
        return pane;
    }
    
    private void fillInRezData(Rezept rez) {
        // Empty defaults:
        logger.debug("Entering fillInData on RezNr: " + rez.getRezNr());
        rezlabs[4].setText("Kein Arzt");
        rezlabs[5].setText(" ");
        rezlabs[6].setText(" ");
        // rezlabs[7].setText(" ");
        rezlabs[7].setText("<HTML><font color=red>Therapiebericht fehlt</font><html>");
        rezlabs[9].setText("<HTML><font color=red>??? / Wo.</font><html>");
        rezlabs[13].setText("");
        rezlabs[14].setText("<HTML><font color=red>??? Min.</font><html>");
        
        rezlabs[2].setText("angelegt von: " + rez.getAngelegtVon());

        rezlabs[3].setForeground((rez.getkId() >= 0 ? Color.BLACK : Color.RED));
        rezlabs[3].setText(rez.getKTraegerName());
        
        ArztVec verordnenderArzt = new ArztVec();
        if (verordnenderArzt.init(rez.getArztId())) {
            rezlabs[4].setForeground(Color.BLACK);
            rezlabs[4].setText( verordnenderArzt.getNNameLanr() );
            
        } else {
            rezlabs[4].setForeground(Color.RED);
        }
        
        int rezArtImRezept = rez.getRezeptArt();
        if (rezArtImRezept >= 0) {
            String[] rezArt = { "Erstverordnung", "Folgeverordnung", "Folgev. au\u00dferhalb d.R." };
            rezlabs[5].setText(rezArt[rezArtImRezept]);
            if (rezArtImRezept == Rezept.REZART_FOLGEVOADR) {
                if (rez.isBegruendADR()) {
                    rezlabs[6].setForeground(Color.BLACK);
                    rezlabs[6].setText("Begr\u00fcndung o.k.");
                } else {
                    rezlabs[6].setForeground(Color.RED);
                    rezlabs[6].setText("Begr\u00fcndung fehlt");
                }
            }
        }
        
        if (rez.isArztBericht()) {
            rezlabs[7].setVisible(true);
            if ( rez.getBerId() > 0) {
                rezlabs[7].setForeground(Color.BLACK);
                rezlabs[7].setText("Therapiebericht o.k.");
            } 
            /* else {
                rezlabs[7].setForeground(Color.RED);
                rezlabs[7].setText("Therapiebericht fehlt");
            } */
        }
        
        // Beautified HM-Pos1 String:
        rezlabs[8].setText(showHM(rez,1));
        
        if ( rez.getFrequenz() == null || rez.getFrequenz().isEmpty()) {
            rezlabs[9].setForeground(Color.RED);
            rezlabs[9].setText("??? / Wo.");
        } else {
            rezlabs[9].setForeground(Color.BLACK);
            rezlabs[9].setText(rez.getFrequenz() + " / Wo.");
        }
        
        // Beautified HM-Pos2-4 String:
        rezlabs[10].setText(showHM(rez,2));
        rezlabs[11].setText(showHM(rez,3));
        rezlabs[12].setText(showHM(rez,4));
        
        String indiSchl = rez.getIndikatSchl();
        if (!rez.getRezNr().startsWith("RH")) {
            if (indiSchl.isEmpty() || "kein IndiSchl.".equals(indiSchl)) {
                    rezlabs[13].setForeground(Color.RED);
                    rezlabs[13].setText("??? " + indiSchl);
                
            } else {
                    rezlabs[13].setForeground(Color.BLACK);
                    rezlabs[13].setText(indiSchl);
            }
        }

        if (!rez.getDauer().isEmpty()) {
            rezlabs[14].setForeground(Color.BLACK);
            rezlabs[14].setText(rez.getDauer() + " Min.");
        }
        
        String icd101 = rez.getIcd10();
        if ( icd101 != null && !icd101.trim().isEmpty()) {
            String diagText = "1.ICD-10: " + icd101;
            String icd102 = rez.getIcd10_2();
            if ( icd102 != null && !icd102.trim().isEmpty()) {
                diagText = diagText.concat("  -  2.ICD-10: " + icd102);
            }
            rezdiag.setText(diagText + "\n" + rez.getDiagnose());
        } else {
            rezdiag.setText(rez.getDiagnose());
        }
        
        rezlabs[15].setText(rez.getLastEditor());
        rezlabs[16].setText("am: " + rez.getLastEdDate().format(DateTimeFormatters.ddMMYYYYmitPunkt));
        
    }
    
    private String showHM(Rezept rez, int idxHM) {
        Vector<Vector<String>> preisvec = null;
        String retVal = "----";
        String diszi = Disziplin.ofShort(rez.getRezClass()).medium;
        int prgruppe = rez.getPreisGruppe() - 1;
        
        try {
            preisvec = SystemPreislisten.hmPreise.get(diszi)
                                                 .get(prgruppe);
        } catch (Exception ex) {
            logger.error("Konnte PG in setRezeptDaten von " + rez.getRezNr() + " via " + diszi + " nicht holen.");
            JOptionPane.showMessageDialog(null,
                    "Achtung Fehler beim Bezug der Preislisteninformation!\nKlasse: RezeptDaten");
            RezeptDaten.feddisch = true;
            return retVal;
        }
        final int KUERZEL = 1;
        final int ID = 9; 
        
        if (!rez.getHMPos(idxHM).isEmpty()) {
            int idOfPricelistEntry = rez.getArtDerBehandlung(idxHM);
            
            if (idOfPricelistEntry > 0) {
                for (int i = 0; i < preisvec.size(); i++) {
                    String priceListEntry[] = new String[preisvec.get(i)
                                                                 .size()];
                    preisvec.get(i).toArray(priceListEntry);
                    int thisID = Integer.valueOf(priceListEntry[ID]);
                    if (thisID == idOfPricelistEntry) {
                        retVal = rez.getBehAnzahl(idxHM) + "  *  "
                                + priceListEntry[KUERZEL];
                        if (!rez.getRezNr()
                                    .startsWith("RH")) {
                            retVal = retVal + " (" + rez.getHMPos(idxHM) + ")";
                        }
                    }
                }
            }
        }
        return retVal;
    }
    
    private void setHbIconsAndText() {
        String diszi = Disziplin.ofShort(rez.getRezClass()).medium;
        int prgruppe = rez.getPreisGruppe() - 1;
        if (rez.isHausBesuch()) {
            boolean einzeln = rez.isHbVoll();
            // hblab.setText(StringTools.NullTest(vecDieseVO.getAnzHBS())+" *");
            hblab.setText(rez.getAnzahlHb() + " *");
            // hblab.setIcon((einzeln.equals("T") ? hbimg : hbimg2));
            hblab.setIcon((einzeln ? hbimg : hbimg2));
            hblab.setAlternateText(
                    "<html>" + (einzeln
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
    }
    
    private Rezept grabAktRez() {
        return (new RezeptDto(ik).byRezeptNr(rezNr)).orElse(null);
    }
    
    private Rezept grabHistRez() {
        return (new RezeptDto(ik).getHistorischesRezeptByRezNr(rezNr)).orElse(null);
    }
    
}
