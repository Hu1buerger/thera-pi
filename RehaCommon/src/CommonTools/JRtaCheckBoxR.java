package CommonTools;

import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

public class JRtaCheckBoxR extends JCheckBox {

	/**
	 * Checkbox mit 'Text links von der Checkbox' und 'enabled'-Darstellung in Abhängigkeit von einem lock
	 */
	private static final long serialVersionUID = 4707201282342924271L;

	public JRtaCheckBoxR(){
		super();
		//addItemListener(this);
		setOpaque(false);
		setHorizontalTextPosition(SwingConstants.LEFT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setIconTextGap(10);
	}
	public JRtaCheckBoxR(String ss){
		super(ss);
		//addItemListener(this);
		setOpaque(false);
		setHorizontalTextPosition(SwingConstants.LEFT);
		setHorizontalAlignment(SwingConstants.RIGHT);
		setIconTextGap(10);
	}

	public void showLocked(boolean state, boolean lock) {
		this.setEnabled(true);
		this.setSelected(state);
		this.setEnabled(lock == Boolean.TRUE ? false : true);
	}
}
