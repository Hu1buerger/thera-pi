package patientenFenster.rezepte;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.DateTimeFormatters;
import CommonTools.JRtaLabel;
import CommonTools.StringTools;
import commonData.ArztVec;
import core.Disziplin;
import hauptFenster.Reha;
import mandant.IK;
import rezept.Rezept;
import rezept.RezeptDto;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;

public class RezeptDatenDarstellen {
    private static final Logger logger = LoggerFactory.getLogger(RezeptDatenDarstellen.class);
    
    /**
     * 0 RezNr?? - 1 hausbesuch - 2 angelegt - 3 kostentraeger - 4 arzt - 5 verornungsart - 6 begruendung - 7 arztbericht
     * 8 behandlung1 - 9 frequenz - 10 behandlung2 - 11  behandlung3 - 12  behandlung4 - 13  indikation - 14  Dauer
     * 15 LastEditor - 16 LastEditDate
     */
    private JLabel[] rezlabs = new JLabel[17];
    
    private Rezept rez;
    private String rezNr;private IK ik;
    private boolean aktuelle;
    
    // Move to some better place:
    private JRtaLabel hblab = null;
    public ImageIcon hbimg = null;
    public ImageIcon hbimg2 = null;
    
    public RezeptDatenDarstellen(String RezNr, boolean Aktuelle, IK Ik) {
        rezNr = RezNr;
        ik = Ik;
        aktuelle = Aktuelle;
        hbimg = SystemConfig.hmSysIcons.get("hausbesuch");
        hbimg2 = SystemConfig.hmSysIcons.get("hbmehrere");
    }

    private JXPanel createPanel() {
        JXPanel pan = new JXPanel();
        pan.setOpaque(false);
        pan.setBorder(null);
        pan.setLayout(new BorderLayout());
        
        rez = (aktuelle ? grabAktRez() : grabHistRez());
        if (rez == null)
            return pan;
        
        return pan;
    }
    
    private JScrollPane getDatenScrPane() {
        JScrollPane pane = new JScrollPane();
        Component table = null;
        JScrollPane pane1 =new JScrollPane(table);
        
        return pane;
    }
    
    private void fillInRezData() {
        // Empty defaults:
        rezlabs[4].setText("Kein Arzt");
        rezlabs[5].setText(" ");
        rezlabs[6].setText(" ");
        rezlabs[7].setText(" ");
        
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
            if ( rez.getArztBericht() != null && !rez.getArztBericht().isEmpty()) {
                rezlabs[7].setForeground(Color.BLACK);
                rezlabs[7].setText("Therapiebericht o.k.");
            } else {
                rezlabs[7].setForeground(Color.RED);
                rezlabs[7].setText("Therapiebericht fehlt");
            }
        }
        
        
        rezlabs[15].setText(rez.getLastEditor());
        rezlabs[16].setText("am: " + rez.getLastEdDate().format(DateTimeFormatters.ddMMYYYYmitPunkt));
        
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
