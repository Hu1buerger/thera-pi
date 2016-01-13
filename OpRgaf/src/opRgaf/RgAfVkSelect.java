package opRgaf;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXPanel;

import CommonTools.JRtaCheckBox;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * kleine Tabelle mit 3 Checkboxen zur Auswahl, ob RGR, AFR, oder/und Verkaufsrechnungenm bei der Bearbeitung berücksichtigt werden sollen
 * 
 * @author McM
 *
 */
public class RgAfVkSelect implements ItemListener{
	private JLabel askLabel;
	private JPanel checkBoxArea;
	private JRtaCheckBox chkRGR = null, chkAFR = null, chkVKR = null;
	private JXPanel ownedBy;
	IfCbxCallBack callBackObjekt = null;
	
	//private static final long serialVersionUID = -7883557713071422232L;

	/**
	 * Sign and encrypt data from input file and write result to output file.
	 *  
	 * @param ask beschreibt Zweck der Auswahl
	 * @return 
	 * @return 
	 */
	public RgAfVkSelect(String ask) {
		checkBoxArea = new JPanel();
		askLabel = new JLabel(); 

		FormLayout lay = new FormLayout(
		//       1    2     3    4 5
				"2dlu,30dlu,5dlu,p,2dlu",	// xwerte,
		//       1 2 3
				"p,p,p"						// ywerte
				);
		PanelBuilder builder = new PanelBuilder(lay);
		//PanelBuilder builder = new PanelBuilder(lay, new FormDebugPanel());		// debug mode
		builder.getPanel().setOpaque(false);
		CellConstraints cc = new CellConstraints();
		
		askLabel = builder.addLabel(ask, cc.xy(2,2));

		chkRGR = new JRtaCheckBox ("Rezeptgebührenrechnungen");
		chkRGR.setName("includeRGR");
		chkAFR = new JRtaCheckBox ("Ausfallrechnungen");
		chkAFR.setName("includeAFR");
		chkVKR = new JRtaCheckBox ("Verkaufsrechnungen");
		chkVKR.setName("includeVKR");

		chkRGR = (JRtaCheckBox) builder.add(chkRGR,cc.xy(4,1));
		chkAFR = (JRtaCheckBox) builder.add(chkAFR,cc.xy(4,2));
		chkVKR = (JRtaCheckBox) builder.add(chkVKR,cc.xy(4,3));
		
/*
 		// letzte Auswahl wiederherstellen (Bsp.) - muß 'ne Methode werden
 		if(OpRgaf.mahnParameter.get("inkasse").equals("Kasse")){
			rgr.setSelected(true);
		}
		// stattdessen erstmal bisher. Zustand als default:
 */
		chkRGR.setSelected(true);
		chkAFR.setSelected(true);

		chkRGR.addItemListener(this);			// Listener registrieren (setzt 'implements ... ItemListener' in class definition voraus)
		chkAFR.addItemListener(this);
		chkVKR.addItemListener(this);		

		builder.setOpaque(true);
		checkBoxArea.add(builder.getPanel());
	}
	

	public boolean useRGR() {
		return (chkRGR.isSelected());
	}
	
	public boolean useAFR() {
		return (chkAFR.isSelected());
	}
	
	public boolean useVKR() {
		return (chkVKR.isSelected());
	}
	
	public Component getPanel() {
		return checkBoxArea;
	}


	public void ask(String ask) {
		 askLabel.setText(ask);
		 checkBoxArea.validate();
	}

	 void setCallBackObj (IfCbxCallBack callBackObj)			// Referenz auf Klasse, die das Interface implementiert 
	 {
		 callBackObjekt = callBackObj;
	 }
	 
	@Override
	public void itemStateChanged( ItemEvent e ) {
	    Object source = e.getItemSelectable();
	    //    System.out.println(e.getStateChange() == ItemEvent.SELECTED ? "SELECTED" : "DESELECTED");

	    if (source == chkRGR) {
	        //find out whether box was checked or unchecked.
	        if (e.getStateChange() == ItemEvent.DESELECTED) {
	            //keine Rezeptgebühren berücksichtigen
		        callBackObjekt.useRGR(false);
	        }else{
		        callBackObjekt.useRGR(true);
	        }
	    }
	    if (source == chkAFR) {
	        if (e.getStateChange() == ItemEvent.DESELECTED) {
	            //keine Ausfallrechnungen berücksichtigen
		        callBackObjekt.useAFR(false);
	        }else{
		        callBackObjekt.useAFR(true);
	        }
	    }
	    if (source == chkVKR) {
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
