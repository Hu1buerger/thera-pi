package CommonTools;

import java.awt.Component;
import java.awt.event.ItemEvent;

/**
 * kleine Tabelle mit 3 Checkboxen zur Auswahl, ob RGR/AFR oder/und
 * Verkaufsrechnungen und/oder bar bezahlte Prvatrechnungen bei der Bearbeitung
 * berücksichtigt werden sollen
 *
 * @author McM
 *
 */
public class RgVkPrSelect extends Select3ChkBx {
    // private static final long serialVersionUID = -7830713581225774232L;
    private RgVkPr_IfCallBack callBackObjekt = null;

    /**
     * @param ask beschreibt Zweck der Auswahl
     */
    public RgVkPrSelect(String ask) {
        super(ask, "RGR/AFR", "Verkaufsrechnungen", "Privatrechnungen");
    }

    public boolean useRGR() {
        return (chkBxO.isSelected());
    }

    public void setRGR(boolean value) { // letzte Auswahl wiederherstellen
        chkBxO.setSelected(value);
    }

    public boolean useVKR() {
        return (chkBxM.isSelected());
    }

    public void setVKR(boolean value) {
        chkBxM.setSelected(value);
    }

    public boolean usePR() {
        return (chkBxU.isSelected());
    }

    public void setPR(boolean value) {
        chkBxU.setSelected(value);
    }



    public Component getPanel() {
        return checkBoxArea;
    }



    public void setCallBackObj(RgVkPr_IfCallBack callBackObj) // Referenz auf Klasse, die das Interface implementiert
    {
        callBackObjekt = callBackObj;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        // System.out.println(e.getStateChange() == ItemEvent.SELECTED ? "SELECTED" :
        // "DESELECTED");

        if (source == chkBxO) {
            // find out whether box was checked or unchecked.
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                // keine Rezeptgebühren berücksichtigen
                callBackObjekt.useRGAVR(false);
            } else {
                callBackObjekt.useRGAVR(true);
            }
        }
        if (source == chkBxM) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                // keine Ausfallrechnungen berücksichtigen
                callBackObjekt.useVKR(false);
            } else {
                callBackObjekt.useVKR(true);
            }
        }
        if (source == chkBxU) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                // keine Verkaufserlöse berücksichtigen
                callBackObjekt.usePR(false);
            } else {
                callBackObjekt.usePR(true);
            }
        }
    }

    private String sqlAddOr(String sstr, String field, String startsWith) {
        String tmp = sstr;
        if (tmp.length() > 0) {
            tmp = tmp + " OR ";
        }
        tmp = tmp + field + " like '" + startsWith + "%' ";

        return tmp;
    }




}
