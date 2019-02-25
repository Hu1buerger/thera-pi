package CommonTools;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * kleine Tabelle mit 3 Checkboxen zur Auswahl, ob RGR, AFR, oder/und Verkaufsrechnungenm bei der Bearbeitung berücksichtigt werden sollen
 * 
 * @author McM
 *
 */
public class RgAfVkSelect extends Select3ChkBx{
	//private static final long serialVersionUID = -7883557713071422232L;
	public RgAfVk_IfCallBack callBackObjekt = null;

	/**
	 * @param ask beschreibt Zweck der Auswahl
	 */
	public RgAfVkSelect(String ask) {
		super(ask, "Rezeptgebührenrechnungen","Ausfallrechnungen", "Verkaufsrechnungen");
	}
	

	public boolean useRGR() {
		return (chkBxO.isSelected());
	}
	public void setRGR(boolean value){ 		// letzte Auswahl wiederherstellen
		chkBxO.setSelected(value);
	}
	
	public boolean useAFR() {
		return (chkBxM.isSelected());
	}
	public void setAFR(boolean value){
		chkBxM.setSelected(value);
	}
	
	public boolean useVKR() {
		return (chkBxU.isSelected());
	}
	public void setVKR(boolean value){
		chkBxU.setSelected(value);
	}
	
	public void setRGR_AFR_VKR(boolean valueR, boolean valueA, boolean valueV){
		setRGR(valueR);
		setAFR(valueA);
		setVKR(valueV);
	}
	
	public void disableVKR() {
		chkBxU.setEnabled(false);
	}

	public Component getPanel() {
		return checkBoxArea;
	}


	public void ask(String ask) {
		 askLabel.setText(ask);
		 checkBoxArea.validate();
	}

	 public void setCallBackObj (RgAfVk_IfCallBack callBackObj)			// Referenz auf Klasse, die das Interface implementiert 
	 {
		 callBackObjekt = callBackObj;
	 }


	@Override
	public void itemStateChanged( ItemEvent e ) {
	    Object source = e.getItemSelectable();
	    //    System.out.println(e.getStateChange() == ItemEvent.SELECTED ? "SELECTED" : "DESELECTED");

	    if (source == chkBxO) {
	        //find out whether box was checked or unchecked.
	        if (e.getStateChange() == ItemEvent.DESELECTED) {
	            //keine Rezeptgebühren berücksichtigen
		        callBackObjekt.useRGR(false);
	        }else{
		        callBackObjekt.useRGR(true);
	        }
	    }
	    if (source == chkBxM) {
	        if (e.getStateChange() == ItemEvent.DESELECTED) {
	            //keine Ausfallrechnungen berücksichtigen
		        callBackObjekt.useAFR(false);
	        }else{
		        callBackObjekt.useAFR(true);
	        }
	    }
	    if (source == chkBxU) {
	        if (e.getStateChange() == ItemEvent.DESELECTED) {
	        	// keine Verkaufserlöse berücksichtigen
		        callBackObjekt.useVKR(false);
	        }else{
		        callBackObjekt.useVKR(true);
	        }
	    }
	}

	private String sqlAddOr(String sstr, String field, String startsWith) {
		String tmp = sstr;
		if (tmp.length() > 0) {
			tmp = tmp + " OR " ;
		}
		tmp = tmp + field + " like '" + startsWith + "%' ";
	
		return tmp;
	}

	public String bills2search(String field) {
		String suche = "";
		if (useRGR()){
			suche = sqlAddOr(suche, field,"RGR-");
		}
		if (useAFR()){
			suche = sqlAddOr(suche, field,"AFR-");
		}
		if (useVKR()){
			suche = sqlAddOr(suche, field,"VR-");
		}
		if (useRGR() && useAFR() && useVKR()){
			// alle drei -> nix einschränken!
		}
		if (!useRGR() && !useAFR() && !useVKR()){
			// keine Tabelle -> leere Suche
			suche = field + " like ''";
		}
		return suche;
	}

}

