package systemEinstellungen;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.ButtonTools;

public class AddOrRemove {
	private JButton btAdd, btRemove;
	private ActionListener al;
	private JPanel vorlagenArea;
	private SysInitCommon_If ownedBy;
	private int instanceNb;
	private boolean formok;

	public AddOrRemove(SysInitCommon_If owner, int instance) {
		this.ownedBy = owner;
		this.instanceNb = instance;
		activateListener();
		btAdd = ButtonTools.macheButton("hinzufügen", "btCmdAdd", al);
		btRemove = ButtonTools.macheButton("entfernen", "btCmdRemove", al);
		//                                    1.   2.                   3.       4.    5. 
		FormLayout jpanlay = new FormLayout("p, right:max(130dlu;p):g, 4dlu, 60dlu, p",	// x
											//1.  2.     3.    4.   5.  
											" p, 11dlu, 1dlu, 2dlu, 11dlu, 1dlu,p");				// y 

		PanelBuilder builder = new PanelBuilder(jpanlay);
		builder.getPanel().setOpaque(false);		
		CellConstraints cc = new CellConstraints();

		builder.addLabel("aus Liste", cc.xy(2, 2, CellConstraints.RIGHT, CellConstraints.BOTTOM));
		builder.add(btRemove, cc.xywh(4, 2, 2, 2));
		builder.addLabel("zu Liste", cc.xy(2, 5, CellConstraints.RIGHT, CellConstraints.BOTTOM));
		builder.add(btAdd, cc.xywh(4,5,2,2));
		
		builder.getPanel().validate();

		vorlagenArea = builder.getPanel();
	}

	private void activateListener() {
		al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(arg0.getActionCommand().equals("btCmdAdd")) {
					ownedBy.AddEntry(instanceNb);
				} else 	if(arg0.getActionCommand().equals("btCmdRemove")){
					ownedBy.RemoveEntry(instanceNb);
					return;
				}
			}
		};
	}

	public JPanel getPanel() {
		return vorlagenArea;
	}
}