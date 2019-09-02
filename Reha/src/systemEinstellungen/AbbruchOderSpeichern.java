package systemEinstellungen;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import CommonTools.ButtonTools;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class AbbruchOderSpeichern {
	private JButton btSpeichern, btAbbruch;
	private ActionListener al;
	private JPanel footerArea;
	private SysInitCommon_If ownedBy;


	public AbbruchOderSpeichern(SysInitCommon_If owner){
		this.ownedBy = owner;
		activateListener();
		btAbbruch = ButtonTools.macheButton("abbrechen", "abbrechen", al);
		btSpeichern = ButtonTools.macheButton("speichern", "speichern", al);
									//      1.     2.                   3.     4.     5.     6.    7.
		FormLayout jpanlay = new FormLayout("p:g, right:max(150dlu;p), 60dlu, 60dlu, 4dlu, 60dlu, 20dlu",
       //1.    2. 3.   4.   
		"10dlu,p, 10dlu, p");
		
		PanelBuilder jpan = new PanelBuilder(jpanlay);
		jpan.getPanel().setOpaque(false);		
		CellConstraints jpancc = new CellConstraints();
		
		jpan.addSeparator("", jpancc.xyw(1,2,6));
		jpan.add(btAbbruch, jpancc.xy(4,4));
		jpan.add(btSpeichern, jpancc.xy(6,4));
		jpan.addLabel("Änderungen übernehmen?", jpancc.xy(2,4));
		
		jpan.getPanel().validate();

		footerArea = jpan.getPanel();
	}
	
	private void activateListener() {
		al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(arg0.getActionCommand().equals("speichern")) {
					ownedBy.Speichern();
				} else 	if(arg0.getActionCommand().equals("abbrechen")){
					ownedBy.Abbruch();
					return;
				}
			}
		};
	}

	public JPanel getPanel() {
		return footerArea;
	}

}
