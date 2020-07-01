package patientenFenster.rezepte;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.jdesktop.swingx.JXPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.therapi.reha.patient.RezeptDaten;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.DateTimeFormatters;
import CommonTools.ExUndHop;
import CommonTools.JCompTools;
import CommonTools.JRtaLabel;
import CommonTools.JRtaTextField;
import CommonTools.StringTools;
import commonData.ArztVec;
import commonData.Rezeptvector;
import core.Disziplin;
import hauptFenster.Reha;
import mandant.IK;
import rechteTools.Rechte;
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
    
    /** "angelegt", "kostentraeger", "arzt", "verordnungsart", "begruendung", "arztbericht", "behandlung1",
            "frequenz", "behandlung2", "behandlung3", "behandlung4", "indikation", "dauer", "lastEditor", "lastEditDate" **/
    // These are used as keys in the HM
    String[] fieldNames = { "angelegt", "kostentraeger", "arzt", "verordnungsart", "begruendung", "arztbericht", "behandlung1",
            "frequenz", "behandlung2", "behandlung3", "behandlung4", "indikation", "dauer", "lastEditor", "lastEditDate" };
    private HashMap<String, JLabel> rezFields = new HashMap<>();
    
    private Rezept rez;
    private String rezNr;
    private IK ik;
    private boolean aktuelle;
    
    // Move to some better place:
    private JRtaLabel hblab = null;         // replaces rezlabs[1] - JRtaTextfield doesn't offer 'alternateText' which is
                                            // used for HB when visiting 1 location with multiple Patienten (in aktuelle)...
    private ImageIcon hbimg = null;
    private ImageIcon hbimg2 = null;
    private JRtaTextField reznum = null;    // replaces rezlabs[0] - it's not clear if we can offer all the features we want
                                            // to do with RezNum on an ArrayElement...
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
    // TODO: maybe migrate away from RezNr & Ik and drop in a full Rezept...
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
    // TODO: maybe migrate away from RezNr & Ik and drop in a full Rezept...
    public void updateDatenPanel(String RezNr, boolean Aktuelle) {
        rezNr = RezNr;
        aktuelle = Aktuelle;
        reznum.setText(rezNr);
        if (rezNr != null && !rezNr.isEmpty()) {
            logger.debug("Update on valid rezNr " + rezNr);
            rez = (aktuelle ? grabAktRez() : grabHistRez());
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

        hblab = new JRtaLabel(" ");
        hblab.setHorizontalTextPosition(JLabel.LEFT);
        hblab.setName("hausbesuch");
        hblab.setIcon(hbimg);
        if (aktuelle)
            activateHBAnzahlEditable();

        // All piggies are equal...
        for (String field : fieldNames ) {
            JLabel lab = new JLabel(" ");
            lab.setName(field);
            rezFields.put(field, lab);
        }
        // But some are more equal... ;)
        rezFields.get("begruendung").setForeground(Color.RED);
        rezFields.get("begruendung").setVisible(false);
        
        
        rezFields.get("arztbericht").setVisible(false);
        
        rezFields.get("behandlung1").setFont(fontbehandlung);
        rezFields.get("behandlung2").setFont(fontbehandlung);
        rezFields.get("behandlung3").setFont(fontbehandlung);
        rezFields.get("behandlung4").setFont(fontbehandlung);
        rezFields.get("frequenz").setFont(fontbehandlung);
        rezFields.get("indikation").setFont(fontbehandlung);
        rezFields.get("dauer").setFont(fontbehandlung);
        
        rezFields.get("lastEditor").setForeground(Color.GRAY);
        rezFields.get("lastEditDate").setForeground(Color.GRAY);
                
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
        jpan.add(hblab, cc.xy(3, 1));
        // jpan.add(rezlabs[2], cc.xy(5, 1));
        jpan.add(rezFields.get("angelegt"), cc.xy(5, 1));

        jpan.addSeparator("", cc.xyw(1, 3, 5));

        //jpan.add(rezlabs[3], cc.xy(1, 5));
        jpan.add(rezFields.get("kostentraeger"), cc.xy(1, 5));
        jpan.add(rezFields.get("arzt"), cc.xy(5, 5));

        jpan.add(rezFields.get("verordnungsart"), cc.xy(1, 7));
        jpan.add(rezFields.get("begruendung"), cc.xy(3, 7));
        jpan.add(rezFields.get("arztbericht"), cc.xy(5, 7));

        jpan.addSeparator("", cc.xyw(1, 9, 5));

        jpan.add(rezFields.get("behandlung1"), cc.xy(1, 11));
        jpan.add(rezFields.get("frequenz"), cc.xy(3, 11));
        jpan.add(rezFields.get("dauer"), cc.xy(5, 11));
        jpan.add(rezFields.get("behandlung2"), cc.xy(1, 13));
        jpan.add(rezFields.get("behandlung3"), cc.xy(1, 15));
        jpan.add(rezFields.get("behandlung4"), cc.xy(1, 17));

        jpan.addSeparator("", cc.xyw(1, 19, 5));

        jpan.add(rezFields.get("indikation"), cc.xy(1, 21));
        // rezlabs[14] added between 9 & 10...
                
        JScrollPane jscrdiag = JCompTools.getTransparentScrollPane(rezdiag);
        jscrdiag.validate();
        jpan.add(jscrdiag, cc.xywh(3, 21, 3, 4));

        jpan.addSeparator("", cc.xyw(1, 25, 5));
        
        JLabel labLastEdit = new JLabel("Zuletzt bearbeitet durch:");
        labLastEdit.setForeground(Color.GRAY);
        jpan.add(labLastEdit, cc.xy(1, 26));
        jpan.add(rezFields.get("lastEditor"), cc.xy(3, 26));
        jpan.add(rezFields.get("lastEditDate"), cc.xy(5, 26));

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
                        + "\u00b0" + reznum.getText() + "\u00b0" + rezFields.get("dauer").getText());
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
    
    private void activateHBAnzahlEditable() {
        hblab.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {

                if ((arg0.getSource() instanceof JLabel) && (arg0.getClickCount() == 2)) {
                    if (!Rechte.hatRecht(Rechte.Rezept_editvoll, true)) {
                        return;
                    }
                    int anzhb = Reha.instance.patpanel.rezAktRez.getAnzahlHb();
                    Object ret = JOptionPane.showInputDialog(null, "Geben Sie bitte die neue Anzahl f\u00fcr Hausbesuch ein",
                            anzhb);
                    if (ret == null) {
                        return;
                    }
                    int neueHBAnzahl = Integer.valueOf(ret.toString());
                    if ( neueHBAnzahl != anzhb ) {
                        hblab.setText(((String) ret).trim() + " *");
                        new ExUndHop().setzeStatement("update verordn set anzahlhb=" + neueHBAnzahl + " "
                                + "where rez_nr='" + rez.getRezNr() + "' LIMIT 1");
                     // TODO: Fire Update-event
                        Reha.instance.patpanel.rezAktRez.setAnzahlHb(neueHBAnzahl);
                    }
                }
            }
        });
    }
    
    private JScrollPane getDatenScrPane() {
        JScrollPane pane = new JScrollPane();
        Component table = null;
        JScrollPane pane1 =new JScrollPane(table);
        
        return pane;
    }
    
    private void fillInRezData(Rezept rez) {
        // Empty defaults:
        rezFields.get("arzt").setText("Kein Arzt");
        rezFields.get("begruendung").setText("<HTML><font color=red>Begr\u00fcndung fehlt</font><html>");
        rezFields.get("arztbericht").setText("<HTML><font color=red>Therapiebericht fehlt</font><html>");
        rezFields.get("frequenz").setText("<HTML><font color=red>??? / Wo.</font><html>");
        rezFields.get("indikation").setText("");
        rezFields.get("dauer").setText("<HTML><font color=red>??? Min.</font><html>");
        
        reznum.setText(rez.getRezNr());
        
        // Historie (currently) uses simplified HB icon display
        if (aktuelle)
            setHbIconsAndText();
        // Aktuelle likes coloured RezNums:
        if (aktuelle)
            setRezNumColour(rez);
        
        rezFields.get("angelegt").setText("angelegt von: " + rez.getAngelegtVon());
        rezFields.get("lastEditor").setText(rez.getLastEditor());
        rezFields.get("lastEditDate").setText("am: " + (rez.getLastEdDate() == null ? "???" 
                                                : rez.getLastEdDate().format(DateTimeFormatters.ddMMYYYYmitPunkt)));

        rezFields.get("kostentraeger").setForeground((rez.getkId() >= 0 ? Color.BLACK : Color.RED));
        rezFields.get("kostentraeger").setText(rez.getKTraegerName());
        
        ArztVec verordnenderArzt = new ArztVec();
        if (verordnenderArzt.init(rez.getArztId())) {
            rezFields.get("arzt").setForeground(Color.BLACK);
            rezFields.get("arzt").setText( verordnenderArzt.getNNameLanr() );
            
        } else {
            rezFields.get("arzt").setForeground(Color.RED);
        }
        if (rez.isArztBericht()) {
            rezFields.get("arztbericht").setVisible(true);
            if ( rez.getBerId() > 0) {
                rezFields.get("arztbericht").setForeground(Color.BLACK);
                rezFields.get("arztbericht").setText("Therapiebericht o.k.");
            } 
        }
        
        int rezArtImRezept = rez.getRezeptArt();
        if (rezArtImRezept >= 0) {
            String[] rezArt = { "Erstverordnung", "Folgeverordnung", "Folgev. au\u00dferhalb d.R." };
            rezFields.get("verordnungsart").setText(rezArt[rezArtImRezept]);
            if (rezArtImRezept == Rezept.REZART_FOLGEVOADR) {
                rezFields.get("begruendung").setVisible(true);
                if (rez.isBegruendADR()) {
                    rezFields.get("begruendung").setForeground(Color.BLACK);
                    rezFields.get("begruendung").setText("Begr\u00fcndung o.k.");
                }
            }
        }
        
        
        // Beautified HM-Pos String:
        rezFields.get("behandlung1").setText(showHM(rez,1));
        rezFields.get("behandlung2").setText(showHM(rez,2));
        rezFields.get("behandlung3").setText(showHM(rez,3));
        rezFields.get("behandlung4").setText(showHM(rez,4));
        
        if ( rez.getFrequenz() == null || rez.getFrequenz().isEmpty()) {
            rezFields.get("frequenz").setForeground(Color.RED);
            rezFields.get("frequenz").setText("??? / Wo.");
        } else {
            rezFields.get("frequenz").setForeground(Color.BLACK);
            rezFields.get("frequenz").setText(rez.getFrequenz() + " / Wo.");
        }
        
        
        String indiSchl = rez.getIndikatSchl();
        if (!rez.getRezNr().startsWith("RH")) {
            if (indiSchl.isEmpty() || "kein IndiSchl.".equals(indiSchl)) {
                rezFields.get("indikation").setForeground(Color.RED);
                rezFields.get("indikation").setText("??? " + indiSchl);
                
            } else {
                rezFields.get("indikation").setForeground(Color.BLACK);
                rezFields.get("indikation").setText(indiSchl);
            }
        }

        if (rez.getDauer() != null && !rez.getDauer().isEmpty()) {
            rezFields.get("dauer").setForeground(Color.BLACK);
            rezFields.get("dauer").setText(rez.getDauer() + " Min.");
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
                
    }

    /**
     * @param rez
     */
    private void setRezNumColour(Rezept rez) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int farbcode = rez.getFarbcode();
                if (farbcode > 0) {
                    reznum.setText(rez.getRezNr());
                    reznum.setForeground((SystemConfig.vSysColsObject.get(0)
                                                                     .get(farbcode)[0]));
                    reznum.repaint();
                }
            }
        });
    }
    
    private String showHM(Rezept rez, int idxHM) {
        Vector<Vector<String>> preisvec = null;
        String retVal = "----";
        String diszi = Disziplin.ofShort(rez.getRezClass()).medium;
        int prgruppe = rez.getPreisGruppe() - 1;
        
        if (rez.getHMPos(idxHM) == null)
            return retVal;
        
        try {
            preisvec = SystemPreislisten.hmPreise.get(diszi)
                                                 .get(prgruppe);
        } catch (Exception ex) {
            logger.error("Konnte PG in setRezeptDaten von " + rez.getRezNr() + " via " + diszi + " nicht holen.");
            JOptionPane.showMessageDialog(null,
                    "Achtung Fehler beim Bezug der Preislisteninformation!\nKlasse: RezeptDaten");
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
                        if (!rez.getRezNr().startsWith("RH") && aktuelle) {
                            retVal = retVal + " (" + rez.getHMPos(idxHM) + ")";
                        }
                    }
                }
            }
        }
        return retVal;
    }
    
    private void setHbIconsAndText() {
        logger.debug("In setHB for: " + rez.getRezNr());
        String diszi = Disziplin.ofShort(rez.getRezClass()).medium;
        int prgruppe = rez.getPreisGruppe() - 1;
        if (rez.isHausBesuch()) {
            // Not sure this sufficiently qualifies - should take isHeimbewohner() into account as well?
            // boolean einzeln = rez.isHbVoll();
            // hblab.setText(StringTools.NullTest(vecDieseVO.getAnzHBS())+" *");
            hblab.setText(rez.getAnzahlHb() + " *");
            // hblab.setIcon((einzeln.equals("T") ? hbimg : hbimg2));
            hblab.setIcon((rez.isHbVoll() ? hbimg : hbimg2));
            hblab.setAlternateText(
                    "<html>" + (rez.isHbVoll()
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
