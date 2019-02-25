package CommonTools;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public abstract class Select3ChkBx implements ItemListener{

	public JLabel askLabel;
	public JPanel checkBoxArea;
	public JXPanel ownedBy;
	public JRtaCheckBox chkBxO = null, chkBxM = null, chkBxU = null;

	/**
	 * @param ask - beschreibt Zweck der Auswahl
	 * @param chkBxOLabel - Label der 1. checkBox
	 * @param chkBxMLabel - Label der 2. checkBox
	 * @param chkBxULabel - Label der 3. checkBox
	 */
	public Select3ChkBx (String ask, String chkBxOLabel,
			String chkBxMLabel, String chkBxULabel) {
		checkBoxArea = new JPanel();
		askLabel = new JLabel(); 

		FormLayout lay = new FormLayout(
		//       1    2 3    4 5
				"2dlu,p,5dlu,p,2dlu",	// xwerte,
		//       1 2 3
				"p,p,p"					// ywerte
				);
		PanelBuilder builder = new PanelBuilder(lay);
		//PanelBuilder builder = new PanelBuilder(lay, new FormDebugPanel());		// debug mode
		builder.getPanel().setOpaque(false);
		CellConstraints cc = new CellConstraints();
		
		askLabel = builder.addLabel(ask, cc.xy(2,2));

		chkBxO = new JRtaCheckBox (chkBxOLabel);
		chkBxO.setName("includeRGR");
		chkBxM = new JRtaCheckBox (chkBxMLabel);
		chkBxM.setName("includeAFR");
		chkBxU = new JRtaCheckBox (chkBxULabel);
		chkBxU.setName("includeVKR");

		chkBxO = (JRtaCheckBox) builder.add(chkBxO,cc.xy(4,1));
		chkBxM = (JRtaCheckBox) builder.add(chkBxM,cc.xy(4,2));
		chkBxU = (JRtaCheckBox) builder.add(chkBxU,cc.xy(4,3));

		chkBxO.addItemListener(this);							// Listener registrieren (setzt 'implements ... ItemListener' in class definition voraus)
		chkBxM.addItemListener(this);
		chkBxU.addItemListener(this);		

		builder.setOpaque(true);
		checkBoxArea.add(builder.getPanel());
	}

}