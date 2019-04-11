package dialoge;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXPanel;

import systemEinstellungen.SystemConfig;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


public class InfoDialog extends JDialog implements WindowListener{
	/**
	 *
	 */
	private static final long serialVersionUID = -2903001967749371315L;
	private JLabel textlab;
	private JLabel bildlab;
	protected KeyListener kl;
	Font font = new Font("Arial",Font.PLAIN,12);
	
	protected String arg1 = null;

	private String infoArt = null;
	JEditorPane htmlPane1 = null;
	JEditorPane htmlPane2 = null;

	Vector <Vector<String>> vecResult = null;
	Vector<String> tage = null;
	Vector<Vector<String>> tageplus = null;
	boolean historie = false;
	boolean notfound = false;
	String endhinweis = "";
	JScrollPane scr1 = null;
	JScrollPane scr2 = null;

	String last12Wo = null;

	String disziplin = "";
	int tagebreak = 0;

	DecimalFormat df = new DecimalFormat( "0.00" );

	boolean muststop = false;

	boolean isF1Released = false;

	JButton[] buts = {null,null,null};
	
//	public InfoDialog(String arg1,String infoArt,Vector<Vector<String>> data) {
	public InfoDialog(String arg1,Vector<Vector<String>> data) {
		super();
		/*
		System.out.println(arg1);
		System.out.println(infoArt);
		System.out.println(data);
		*/
		setUndecorated(true);
		setModal(true);
		this.arg1 = arg1;
//		this.infoArt = infoArt;
		activateListener();
		this.setLayout(new BorderLayout());
//		if(this.infoArt.equals("terminInfo")){
//			this.setContentPane(getTerminInfoContent());
//		}else if(this.infoArt.equals("offenRGAF")){
//			this.setContentPane(getOffeneRechnungenInfoContent(data));
//		}else{	
			this.setContentPane(getContent());	
//		}
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.addKeyListener(kl);
		//validate();
		this.getContentPane().validate();
	}

	void activateListener(){
		kl = new KeyListener(){
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_F1 && (!e.isControlDown()) && (!e.isShiftDown())){
					dispose();
				}
			}
		};
	}
	public JXPanel getContent(){
	JXPanel jpan = new JXPanel();
	jpan.addKeyListener(kl);
	jpan.setPreferredSize(new Dimension(400,100));
	jpan.setBackground(Color.WHITE);
	jpan.setBorder(BorderFactory.createLineBorder(Color.BLACK));

	FormLayout lay = new FormLayout("fill:0:grow(0.5),p,fill:0:grow(0.5)",
			"fill:0:grow(0.25),p,15dlu,p,fill:0:grow(0.75)");
	jpan.setLayout(lay);
	CellConstraints cc = new CellConstraints();
	bildlab = new JLabel(" ");
	bildlab.setIcon(SystemConfig.hmSysIcons.get("tporgklein"));
	jpan.add(bildlab,cc.xy(2, 2));
	textlab = new JLabel(" ");
	textlab.setFont(font);
	textlab.setForeground(Color.BLUE);
	jpan.add(textlab,cc.xy(2, 4,CellConstraints.CENTER,CellConstraints.CENTER));
	jpan.validate();
	return jpan;

	}
	public void setzeLabel(String labelText){
		textlab.setText(labelText);
		textlab.getParent().validate();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void windowOpened(WindowEvent e) {
	}
	@Override
	public void windowClosing(WindowEvent e) {
	}
	@Override
	public void windowClosed(WindowEvent e) {
	}
	@Override
	public void windowIconified(WindowEvent e) {
	}
	@Override
	public void windowDeiconified(WindowEvent e) {
	}
	@Override
	public void windowActivated(WindowEvent e) {
	}
	@Override
	public void windowDeactivated(WindowEvent e) {
	}
	
	
	public static String macheNummer(String string){
		if(string.indexOf("\\") >= 0){
			return string.substring(0,string.indexOf("\\"));
		}
		return string;
	}
	protected String getSpanStyle(String pix,String color){
		return "style='font-family: Arial, Helvetica, sans-serif; font-size: "+pix+"px;"+(color.length()>0 ? "color: "+color+";" : "")+ " '>";
	}
	final StringBuffer  bufhead = new StringBuffer();
	final StringBuffer  bufend = new StringBuffer();
	public String ladehead(){
		bufhead.append("<html>\n<head>\n");
		bufhead.append("<STYLE TYPE=\"text/css\">\n");
		bufhead.append("<!--\n");
		bufhead.append("A{text-decoration:none;background-color:transparent;border:none}\n");
		bufhead.append("A.even{text-decoration:underline;color: #000000; background-color:transparent;border:none}\n");
		bufhead.append("A.odd{text-decoration:underline;color: #FFFFFF;background-color:transparent;border:none}\n");
		bufhead.append("TD{font-family: Arial; font-size: 12pt; vertical-align: top;white-space: nowrap;}\n");
		bufhead.append("TD.inhalt {font-family: Arial, Helvetica, sans-serif; font-size: 20px;background-color: #7356AC;color: #FFFFFF;}\n");
		bufhead.append("TD.inhaltinfo {font-family: Arial, Helvetica, sans-serif; font-size: 20px;background-color: #DACFE7; color: #1E0F87;}\n");
		bufhead.append("TD.headline1 {font-family: Arial, Helvetica, sans-serif; font-size: 14px; background-color: #EADFF7; color: #000000;}\n");
		bufhead.append("TD.headline2 {font-family: Arial, Helvetica, sans-serif; font-size: 14px; background-color: #DACFE7; color: #000000;}\n");
		bufhead.append("TD.headline3 {font-family: Arial, Helvetica, sans-serif; font-size: 14px; background-color: #7356AC; color: #FFFFFF;}\n");
		bufhead.append("TD.headline4 {font-family: Arial, Helvetica, sans-serif; font-size: 14px; background-color: #1E0F87; color: #FFFFFF;}\n");
		bufhead.append("TD.itemeven {font-family: Arial, Helvetica, sans-serif; font-size: 14px; background-color: #E6E6E6; color: #000000;}\n");
		bufhead.append("TD.itemodd {font-family: Arial, Helvetica, sans-serif; font-size: 14px; background-color: #737373; color: #F0F0F0;}\n");
		bufhead.append("TD.itemkleineven {font-family: Arial, Helvetica, sans-serif; font-size: 9px; background-color: #E6E6E6; color: #000000;}\n");
		bufhead.append("TD.itemkleinodd {font-family: Arial, Helvetica, sans-serif; font-size: 9px; background-color: #737373; color: #F0F0F0;}\n");
		bufhead.append("TD.header {font-family: Arial, Helvetica, sans-serif; font-size: 14px; background-color: #1E0F87; color: #FFFFFF;}\n");
		bufhead.append("UL {font-family: Arial, Helvetica, sans-serif; font-size: 9px;}\n");
		bufhead.append("UL.paeb { margin-top: 0px; margin-bottom: 0px; }\n");
		bufhead.append("H1 {font-family: Arial, Helvetica, sans-serif; font-size: 12px; background-color: #1E0F87; color: #FFFFFF;}\n");
		bufhead.append("--->\n");
		bufhead.append("</STYLE>\n");
		bufhead.append("</head>\n");
		bufhead.append("<body>\n");
		//bufhead.append("<div style=margin-left:30px;>");
		//bufhead.append("<font face=\"Tahoma\"><style=margin-left=30px;>");
		return bufhead.toString();
	}

	public String ladeend(){
		bufend.append("</body>\n</html>\n");
		return bufend.toString();
	}
	
}

