package verkauf;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.JRtaTextField;
import dialoge.PinPanel;
import dialoge.RehaSmartDialog;
import events.RehaTPEvent;
import events.RehaTPEventClass;


public class WechselgeldDialog extends RehaSmartDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JXPanel pane = null;
	JXLabel summeLab = null, rest = null;
	JRtaTextField gegebenFeld = null;
	double summe, gegeben = 0;
	ActionListener al = null;
	FocusListener fl = null;
	KeyListener kl = null;
	DecimalFormat df = new DecimalFormat("0.00");
	
	RehaTPEventClass rtp = null;
	
	PinPanel pinPanel = null;
	boolean gotCash = false;
	
	
	public WechselgeldDialog(Frame owner, Point position, double summe) {
		super(null,"WechselGeld");
		this.summe = summe;
		this.activateListener();
		this.setSize(200, 150);
		this.setLocation(position);
		
		pinPanel = new PinPanel();
		pinPanel.getGruen().setVisible(false);
		pinPanel.setName("WechselGeld");
		setPinPanel(pinPanel);
		
		getSmartTitledPanel().setContentContainer(getJContentPane());
		getSmartTitledPanel().getContentContainer().setName("WechselGeld");
		getSmartTitledPanel().setTitle("Wechselgeld");
		this.setName("WechselGeld");
		validate();
	}
	public JRtaTextField getTextFeld(){
		return gegebenFeld;
	}
	public void setzeFocus(){
		SwingUtilities.invokeLater(new Runnable(){
			@Override
            public void run(){
				gegebenFeld.requestFocus();
			}
		});
	}
	
	private JPanel getJContentPane() {
		try{
		pane = new JXPanel();
		pane.setBorder(new EtchedBorder(Color.WHITE, Color.GRAY));
		
		//			      1     2      3     4        5
		String xwerte = "5dlu, 40dlu, 5dlu, 60dlu:g, 5dlu";
		//              1   2    3   4     5  6    7   8    9
		String ywerte ="p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu:g, p, 5dlu ";
		
		FormLayout lay = new FormLayout(xwerte, ywerte);
		CellConstraints cc = new CellConstraints();
		
		pane.setLayout(lay);
		pane.setBackground(Color.WHITE);
		
		JXLabel lab = new JXLabel("Summe");
		pane.add(lab, cc.xy(2, 3));
		
		lab = new JXLabel("gegeben:");
		pane.add(lab, cc.xy(2, 5));
		
		lab = new JXLabel("Rückgeld:");
		pane.add(lab, cc.xy(2, 7));
		
		summeLab = new JXLabel(df.format(summe).replace(".", ","));
		pane.add(summeLab, cc.xy(4, 3));
		
		rest = new JXLabel("0,00");
		pane.add(rest, cc.xy(4, 7));
		
		gegebenFeld = new JRtaTextField("FL",true,"6.2","RECHTS");
		gegebenFeld.setText(df.format(summe));
		gegebenFeld.addKeyListener(kl);
		pane.add(gegebenFeld, cc.xy(4, 5));
		
		JXButton close = new JXButton("Bon drucken & buchen");
		close.setActionCommand("printNbook");
		close.addActionListener(al);
		pane.add(close, cc.xyw(2, 9, 3));
	
		pane.validate();
		}catch(Exception ex){
			ex.printStackTrace();
		}

		return pane;		
	}

	private void activateListener() {
		fl = new FocusListener() {

			@Override
			public void focusGained(FocusEvent arg0) {

			}

			@Override
			public void focusLost(FocusEvent arg0) {
			}
			
		};
		
		kl = new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					continuePayment();
					return;
				}else if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
					endPayment();		// Abbruch mit ESC
					return;
				} 
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				String dummyparse = (gegebenFeld.getText().trim().equals("") ? "0.00" : gegebenFeld.getText().replace(",", "."));
				double uebrig = Double.parseDouble(dummyparse) - summe;		
				rest.setText(df.format(uebrig));				
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				//bei Typed ist das TextFeld noch nicht gefüllt
			}
			
		};
		
		al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(arg0.getActionCommand().equals("printNbook")) {
					continuePayment();
				}
				
			}
			
		};
		
		rtp = new RehaTPEventClass();
		rtp.addRehaTPEventListener(this);
	}
	
	private void endPayment() {
		this.setVisible(false);
		rtp.removeRehaTPEventListener(this);
		rtp = null;
		pinPanel = null;
		this.dispose();
	}
	
	private void continuePayment() {
		gotCash = true;
		endPayment();
	}
	
	public boolean processPayment(){
		return gotCash;
	}
	
	public void rehaTPEventOccurred(RehaTPEvent evt) {
		try{
			if(evt.getDetails()[0].equals("WechselGeld")){
				endPayment();
/*				this.setVisible(false);	
				rtp.removeRehaTPEventListener((RehaTPEventListener) this);
				rtp = null;
				pinPanel = null;
				this.dispose();
 */
			}
		}catch(NullPointerException ne){
		}
	}			

}
