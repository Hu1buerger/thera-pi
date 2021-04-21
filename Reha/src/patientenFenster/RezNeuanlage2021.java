package patientenFenster;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.LinearGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.MattePainter;
import org.therapi.hmrCheck2021.BVBlhmCheck;
import org.therapi.hmrCheck2021.HMRCheck2021;
import org.therapi.reha.patient.AktuelleRezepte;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.Colors;
import CommonTools.DatFunk;
import CommonTools.JCompTools;
import CommonTools.JRtaCheckBox;
import CommonTools.JRtaComboBox;
import CommonTools.JRtaTextField;
import CommonTools.SqlInfo;
import CommonTools.StringTools;
import events.RehaTPEvent;
import events.RehaTPEventClass;
import events.RehaTPEventListener;
import gui.Cursors;
import hauptFenster.Reha;
import hmrCheck.HMRCheck;
import rechteTools.Rechte;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;
import systemTools.ListenerTools;
import umfeld.Betriebsumfeld;

public class RezNeuanlage2021 extends JXPanel implements ActionListener, KeyListener,FocusListener,RehaTPEventListener{

	/**
	 * 
	 */
	// Lemmi Doku: Das sind die Text-Eingabefgelder im Rezept
	public JRtaTextField[] jtf = {null,null,null,null,null,
								  null,null,null,null,null,
								  null,null,null,null,null,
								  null,null,null,null,null,
								  null,null,null,null,null,
								  null,null,null,null,null,
								  null,null,null};
	// Lemmi 20101231: Harte Index-Zahlen für "jtf" durch sprechende Konstanten ersetzt ! 
	final int cKTRAEG = 0;
	final int cARZT   = 1;
	final int cREZDAT = 2;
	final int cBEGINDAT = 3;
	final int cANZ1   = 4;		// ACHTUNG die Positionen cANZ1 bis cANZ4 müssen immer nacheinander definiert sein
	final int cANZ2   = 5;
	final int cANZ3   = 6;
	final int cANZ4   = 7;
	final int cFREQ   = 8;
	final int cDAUER  = 9;
	final int cANGEL   = 10;
	final int cKASID   = 11;
	final int cARZTID  = 12;
	final int cPREISGR = 13;
	final int cHEIMBEW = 14;
	final int cBEFREIT = 15;
	final int cPOS1    = 16;	// ACHTUNG die Positionen cPOS1 bis cPOS4 müssen immer nacheinander definiert sein
	final int cPOS2    = 17;
	final int cPOS3    = 18;
	final int cPOS4    = 19;
	final int cPREIS1    = 20;	// ACHTUNG die Positionen cPREIS1 bis cPREIS4 müssen immer nacheinander definiert sein
	final int cPREIS2    = 21;
	final int cPREIS3    = 22;
	final int cPREIS4    = 23;
	final int cANLAGDAT  = 24;
	final int cANZKM     = 25;
	final int cPATID     = 26;
	final int cPATINT    = 27;
	final int cZZSTAT    = 28;
	final int cHEIMBEWPATSTAM = 29;
	final int cICD10 = 30;
	final int cICD10_2 = 31; 
	final int cAKUTDATUM = 32;
	
	// Lemmi 20101231: Merken der Originalwerte der eingelesenen Textfelder, Combo- und Check-Boxen
	Vector<Object> originale = new Vector<Object>();

	public JRtaCheckBox[] jcb = {null,null,null,null,null,null,null,null,null};
	// Lemmi 20101231: Harte Index-Zahlen für "jcb" durch sprechende Konstanten ersetzt ! 
	final int cBEGRADR = 0;
	final int cKURZFRIST = 0;
	final int cHAUSB   = 1;
	final int cTBANGEF = 2;
	final int cVOLLHB  = 3;
	final int cLEITSA = 4;
	final int cLEITSB = 5;
	final int cLEITSC = 6;
	final int cLEITSX = 7;
	final int cDRINGLICH = 8;
	
	public JRtaComboBox[] jcmb =  {null,null,null,null,null,null,null,null,null,null};
	public JRtaComboBox jcmbIK;
	// Lemmi 20101231: Harte Index-Zahlen für "jcmb" durch sprechende Konstanten ersetzt ! 
	final int cRKLASSE = 0;
	final int cVERORD  = 1;
	final int cLEIST1  = 2;	// ACHTUNG die Positionen cLEIST1 bis cLEIST4 müssen immer nacheinander definiert sein
	final int cLEIST2  = 3;
	final int cLEIST3  = 4;
	final int cLEIST4  = 5;
	final int cINDI    = 6;
	final int cBARCOD  = 7;
	final int cFARBCOD = 8;
	final int cBEDARF  = 9;

	
	public JRtaTextArea jta = null;
	public JTextArea leitsymta = null;
	public JTextArea therapzielta = null;
	
	public JButton speichern = null;
	public JButton abbrechen = null;
	public JButton hmrcheck = null;
	
	public boolean neu = false;
	public String feldname = "";
	
	// Lemmi 20110101: strKopiervorlage zugefügt. Kopieren des letzten Rezepts des selben Patienten bei Rezept-Neuanlage
	//boolean bCtrlPressed = false;
	public String strKopiervorlage = "";
	
	public Vector<String> vec = null;  // Lemmi Doku: Das bekommt den 'vecaktrez' aus dem rufenden Programm (AktuelleRezepte)
	public Vector<Vector<String>> preisvec = null;
	private boolean klassenReady = false;
	private boolean initReady = false;
	private static final long serialVersionUID = 1L;
	private int preisgruppe = -1;
	public boolean feldergefuellt = false;
	private String nummer = null;
	private String[] farbcodes = new String[(SystemConfig.vSysColsBedeut.size()-14)] ;////{null,null,null,null,null,null,null,null,null,null};
	private int anzahlFarbcodes = (SystemConfig.vSysColsBedeut.size()-15);
	//private String[] heilmittel = {"KG","MA","ER","LO","RH"};
	
	private String aktuelleDisziplin = "";
	private int preisgruppen[] = {0,0,0,0,0,0,0,0};
	int[] comboid = {-1,-1,-1,-1};
	
	MattePainter mp = null;
	LinearGradientPaint p = null;
	private RehaTPEventClass rtp = null;
	
	private PanelBuilder datenPanel;
	
	JLabel kassenLab;
	JLabel arztLab;
	
	// Lemmi 20110106: Lieber Hr. Steinhilber: Diese Funktion an andere Stelle verlegt, weil Architekturänderung
//	String rezToCopy = null;
	
	String[] strRezepklassenAktiv = null;
	

	public static boolean isNeu = false;
	public static boolean neueHmr = false;
	public static boolean hmrBeachten = false; 
	
	
	// Lemmi 20110101: bCtrlPressed zugefügt. Kopieren des letzten Rezepts des selben Patienten bei Rezept-Neuanlage
	public RezNeuanlage2021(Vector<String> vec,boolean neu,String sfeldname){
//	public RezNeuanlage(Vector<String> vec,boolean neu,String sfeldname, boolean bCtrlPressed){
		super();
		try{
			this.neu = neu;
			this.feldname = sfeldname;
			this.vec = vec;  // Lemmi 20110106  Wird auch für das Kopieren verwendet !!!!
//			this.bCtrlPressed = bCtrlPressed;
			
			if( vec.size() > 0 && this.neu ) {
				// Lemmi 20110106: Lieber Hr. Steinhilber: Diese Funktion an andere Stelle verlegt, weil Architekturänderung
				//rezToCopy = AktuelleRezepte.getActiveRezNr();
				//aktuelleDisziplin = RezTools.putRezNrGetDisziplin(rezToCopy);
				aktuelleDisziplin = RezTools.getDisziplinFromRezNr(vec.get(1));
			}
			
			/*
			if((Reha.neueHMRinkraft && this.neu)) {
				neueHmr = true;	
			}else if(Reha.neueHMRinkraft && (!this.neu)) {
				//prüfen ob Rezeptdatum < 01.01.2021
				neueHmr = DatFunk.TageDifferenz(vec.get(2), Reha.neueHMRab) >= 0;
			}
			*/ 
			
			setName("RezeptNeuanlage");
			rtp = new RehaTPEventClass();
			rtp.addRehaTPEventListener((RehaTPEventListener) this);


			addKeyListener(this);
			
			setLayout(new BorderLayout());
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
			add(getDatenPanel(),BorderLayout.CENTER);
			add(getButtonPanel(),BorderLayout.SOUTH);
			setBackgroundPainter(Reha.instance.compoundPainter.get("RezNeuanlage"));
			validate();
			SwingUtilities.invokeLater(new Runnable(){
			 	   public  void run()
			 	   {
			 			setzeFocus();		 		   
			 	   }
			});	
			initReady = true;
			if(!neu){
				if(!Rechte.hatRecht(Rechte.Rezept_editvoll, false)){  // Lemmi Doku: Das sieht aus wie der Read-Only-Modus für das Rezept
					for(int i = 0; i < jtf.length; i++){  // Lemmi Doku: alle Textfelder unbedienbar machen
						if(jtf[i] != null){
							jtf[i].setEnabled(false);
						}
					}
					for(int i = 0; i < jcb.length;i++){  // Lemmi Doku: alle CheckBoxen unbedienbar machen
						if(jcb[i] != null){
							jcb[i].setEnabled(false);
						}
					}
					for(int i = 0; i < jcmb.length;i++){ // Lemmi Doku: alle ComboBoxen unbedienbar machen
						if(jcmb[i] != null){
							jcmb[i].setEnabled(false);
						}
					}
					/*
					new SwingWorker<Void,Void>(){
						@Override
						protected Void doInBackground() throws Exception { 
							for(int i = 0; i < jtf.length; i++){  // Lemmi Doku: alle Textfelder unbedienbar machen
								if(jtf[i] != null){
									jtf[i].setEnabled(false);
								}
							}
							for(int i = 0; i < jcb.length;i++){  // Lemmi Doku: alle CheckBoxen unbedienbar machen
								if(jcb[i] != null){
									jcb[i].setEnabled(false);
								}
							}
							for(int i = 0; i < jcmb.length;i++){ // Lemmi Doku: alle ComboBoxen unbedienbar machen
								if(jcmb[i] != null){
									jcmb[i].setEnabled(false);
								}
							}
							return null;
						}
						
					}.execute(); */
				}
			}
			
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null,"Fehler im Konstruktor RezNeuanlage\n"+RezNeuanlage.makeStacktraceToString(ex));
		}

	}

	public void macheFarbcodes(){
		try{
			farbcodes[0] = "kein Farbcode";
			jcmb[cFARBCOD].addItem(farbcodes[0]);
			for( int i = 0; i < anzahlFarbcodes;i++){
			//for (int i = 0;i < 9;i++){
				farbcodes[i+1] = SystemConfig.vSysColsBedeut.get(i+14);
				jcmb[cFARBCOD].addItem(farbcodes[i+1]);
			}
			if(! this.neu){
				int itest = StringTools.ZahlTest(this.vec.get(57));
				if(itest >= 0){
					jcmb[cFARBCOD].setSelectedItem( (String)SystemConfig.vSysColsBedeut.get(itest) );			
				}else{
					jcmb[cFARBCOD].setSelectedIndex(0);
				}
			}else{
				jcmb[cFARBCOD].setSelectedIndex(0);			
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null,"Fehler bei Farbcodes erstellen\n"+ex.getMessage());
		}
		
	}
	public void setzeFocus(){
		SwingUtilities.invokeLater(new Runnable(){
		 	   public  void run()
		 	   {
		 		   if(neu){
		 			   int aid,kid;
		 			   boolean beenden = false;
		 			   String meldung = "";
		 			   kid = StringTools.ZahlTest(jtf[cKASID].getText());
		 			   aid = StringTools.ZahlTest(jtf[cARZTID].getText());
		 			   if(kid < 0 && aid < 0){
		 				   beenden = true;
		 				   meldung = "Achtung - dem Patientenstamm ist keine verwertbare Krankenkasse\n"+
		 				    "sowie kein verwertbarer Arzt zugeordnet\n\n"+
		 				    "Gehen Sie im Patientenstamm auf ->Ändern/Editieren<- und ordnen Sie verwertaber Daten zu!";
		 			   }else if(kid >= 0 && aid < 0){
		 				   beenden = true;
		 				   meldung = "Achtung - dem Patientenstamm ist kein verwertbarer Arzt zugeordnet\n\n"+
		 				    "Gehen Sie im Patientenstamm auf ->Ändern/Editieren<- und ordnen Sie verwertaber Daten zu!";
		 			   }else if(kid < 0 && aid >= 0){
		 				   beenden = true;
		 				   meldung = "Achtung - dem Patientenstamm ist keine verwertbare Krankenkasse zugeordnet\n\n"+
		 				    "Gehen Sie im Patientenstamm auf ->Ändern/Editieren<- und ordnen Sie verwertaber Daten zu!";
		 			   }
		 			   if(beenden){
		 				  JOptionPane.showMessageDialog(null,meldung); 
		 				  aufraeumen();
		 				  ((JXDialog)getParent().getParent().getParent().getParent().getParent()).dispose();
		 			   }else{
			 			   holePreisGruppe(jtf[cKASID].getText().trim());
				 			  SwingUtilities.invokeLater(new Runnable(){
				 				  public  void run()
				 				  {
				 					  jcmb[cRKLASSE].requestFocus();
				 				  }
				 			  });	   		
		 			   }
	 			   // else bedeutet nicht neu - sondern ändern
		 		   }else{
		 			   int aid,kid;
		 			   //boolean beenden = false;
		 			   //String meldung = "";
		 			   kid = StringTools.ZahlTest(jtf[cKASID].getText());
		 			   aid = StringTools.ZahlTest(jtf[cARZTID].getText());
		 			   if(kid < 0 && aid < 0){
		 				   jtf[cKASID].setText(Integer.toString(Reha.instance.patpanel.kid));
		 				   jtf[cARZTID].setText(Integer.toString(Reha.instance.patpanel.aid));
		 				   jtf[cKTRAEG].setText(Reha.instance.patpanel.patDaten.get(13));
		 			   }else if(kid >= 0 && aid < 0){
		 				   jtf[cARZTID].setText(Integer.toString(Reha.instance.patpanel.aid));
		 			   }else if(kid < 0 && aid >= 0){
		 				   jtf[cKASID].setText(Integer.toString(Reha.instance.patpanel.kid));
		 				   jtf[cKTRAEG].setText(Reha.instance.patpanel.patDaten.get(13));
		 			   }else{
		 				   //System.out.println("*****************Keine Preisgruppen bezogen*******************");
		 				  //preisgruppen
		 				  //RezTools.holePreisVector(vec.get(1), Integer.parseInt(vec.get(41))-1);
		 				  //ladePreise();
		 			   }
		 			   SwingUtilities.invokeLater(new Runnable(){
			 			 	   public  void run()
			 			 	   {
			 			 		   jtf[cKTRAEG].requestFocus();
			 			 	   }
			 			});	   		
		 		   }
		 		   		 		   
		 	   }
		});	   		
	}
	
	
	
	public JXPanel getButtonPanel(){
		JXPanel	jpan = JCompTools.getEmptyJXPanel();
		jpan.addKeyListener(this);
		jpan.setOpaque(false);
		FormLayout lay = new FormLayout(
		        // 1                2          3             4      5               6          7 
				"fill:0:grow(0.25),50dlu,fill:0:grow(0.25),50dlu,fill:0:grow(0.25),50dlu,fill:0:grow(0.25)",
				// 1  2  3  
				"5dlu,p,5dlu");
		CellConstraints cc = new CellConstraints();
		jpan.setLayout(lay);
		speichern = new JButton("speichern");
		speichern.setActionCommand("speichern");
		speichern.addActionListener(this);
		speichern.addKeyListener(this);
		speichern.setMnemonic(KeyEvent.VK_S);
		speichern.setName("btnSpeichern");
		jpan.add(speichern,cc.xy(2,2));
		
		hmrcheck = new JButton("HMR-Check");
		hmrcheck.setActionCommand("hmrcheck");
		hmrcheck.addActionListener(this);
		hmrcheck.addKeyListener(this);
		hmrcheck.setMnemonic(KeyEvent.VK_H);
		jpan.add(hmrcheck,cc.xy(4,2));

		abbrechen = new JButton("abbrechen");
		abbrechen.setActionCommand("abbrechen");
		abbrechen.addActionListener(this);
		abbrechen.addKeyListener(this);
		abbrechen.setMnemonic(KeyEvent.VK_A);		
		jpan.add(abbrechen,cc.xy(6,2));

		return jpan;
	}	
	
	/********************************************/

	// Lemmi 20101231: Merken der Originalwerte der eingelesenen Textfelder
	// ACHTUNG: Die Reihenfolge der Abfragen muß in SaveChangeStatus() und HasChanged() exakt identisch sein ! 
	private void SaveChangeStatus(){
		int i;
		originale.clear();  // vorherige Merkung wegwerfen

		// Alle Text-Eingabefelder
		for ( i = 0; i < jtf.length; i++ ) {
			originale.add( jtf[i].getText() );
		}
		
		// Das Feld mit "Ärztliche Diagnose"
		originale.add( jta.getText() );
		
		// alle ComboBoxen
		for ( i = 0; i < jcmb.length; i++ ) {
			originale.add( (Integer)jcmb[i].getSelectedIndex() );  // Art d. Verordn. etc.
		}
		
		// alle CheckBoxen
		for ( i = 0; i < jcb.length; i++ ) {
			originale.add( (Boolean)(jcb[i].isSelected() ) );  // 
		}
	}
	
	// Lemmi 20101231: prüft, ob sich Einträge geändert haben
	// ACHTUNG: Die Reihenfolge der Abfragen muß in SaveChangeStatus() und HasChanged() exakt identisch sein ! 
	public Boolean HasChanged(){
		int i, idx = 0;
		
		// Alle Text-Eingabefelder
		for ( i = 0; i < jtf.length; i++) {
			if(! jtf[i].getText().equals( originale.get(idx++) ) )
				return true;
		}
		
		// Das Feld mit "Ärztliche Diagnose"
		if(! jta.getText().equals( originale.get(idx++) ) )	   // Ärztliche Diagnose
			return true;
		
		// alle ComboBoxen
		for ( i = 0; i < jcmb.length; i++) {	// ComboBoxen 
			if( jcmb[i].getSelectedIndex() != (Integer)originale.get(idx++) )	// Art d. Verordn. etc.
				return true;
		}		
		
		// alle CheckBoxen
		for ( i = 0; i < jcb.length; i++) {		// CheckBoxen
			if( jcb[i].isSelected() != (Boolean)originale.get(idx++) )	// Begründung außer der Regel vorhanden ? .....
				return true;
		}		

		return false;
	}

	// Lemmi 20101231: Stndard-Abfrage nach Prüfung, ob sich Einträge geändert haben
	// fragt nach, ob wirklich ungesichert abgebrochen werden soll !
	public int askForCancelUsaved(){
/*		if ( JOptionPane.NO_OPTION 
		 == JOptionPane.showConfirmDialog(null, "Es wurden Rezept-Anngaben geändert!\nWollen sie die Änderung(en) wirklich verwerfen?",
				 								"Angaben wurden geändert", JOptionPane.YES_NO_OPTION ) ) {
*/
		String[] strOptions = {"ja", "nein"};  // Defaultwert euf "nein" gesetzt !
		return JOptionPane.showOptionDialog(null, "Es wurden Rezept-Anngaben geändert!\nWollen sie die Änderung(en) wirklich verwerfen?",
				 "Angaben wurden geändert", 
				 JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				 strOptions, strOptions[1] );
	 }
	private JScrollPane getDatenPanel2021(){  //1             2      3    4          5              6      7        8
		FormLayout lay = new FormLayout("right:max(80dlu;p), 4dlu, 60dlu, 5dlu, right:max(60dlu;p), 4dlu, 60dlu",
			       //1.   2.   3.   4.   5.   6   7   8    9   10   11  12  13  14    15   16   17  18 19   20   21  22   23  24   25  
					"p, 10dlu, p, 5dlu,  p, 5dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 10dlu, p, 10dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, " +
		//26   27   28   29  30   31   32    33   34   35   36    37 38	 39  40   41	42		
		"10dlu, p, 10dlu, p, 2dlu, p, 2dlu,  p,  10dlu, p, 10dlu, p,10dlu,p,10dlu,30dlu,2dlu");
					

		CellConstraints cc = new CellConstraints();
		PanelBuilder jpan = new PanelBuilder(lay);
		jpan.setDefaultDialogBorder();
		jpan.getPanel().setOpaque(false);
		JScrollPane jscr = null;
		
		return jscr;
		
	}
	
	private JScrollPane getDatenPanel(){  //1                  2      3    4          5              6      7        8       
		FormLayout lay = new FormLayout("right:max(80dlu;p), 4dlu, 60dlu, 5dlu, right:max(80dlu;p), 4dlu, 60dlu",
			       //1.   2.   3.   4.   5.   6   7   8    9   10   11  12  13  14    15   16   17  18   19   20   21  22   23  24   25  
					"p, 10dlu, p, 5dlu,  p, 5dlu, p, 2dlu, p, 0dlu, p, 0dlu, p, 0dlu, p,  5dlu, p, 5dlu, p, 2dlu,  p, 2dlu, p, 2dlu, p, " +
		//26   27   28   29  30    31   32  33  34   35   36    37  38	  39    40   41  42   43  44  45  46   47  48	49  50  51  52  53  ,"+ 
		"2dlu, p, 10dlu,0dlu,5dlu,0dlu, p, 5dlu, p, 2dlu,  p,  2dlu, p,   8dlu, p, 10dlu, p,10dlu, p,2dlu, p, 2dlu, p, 2dlu, p,2dlu, p, 12dlu, "+
		//54  55  56    57   58 59  60  61 62  63   64
		"  p,2dlu,10dlu,0dlu,p,5dlu,p, 2dlu,p,2dlu,p");
					
		int xzugabe = 0;
		int yzugabe = 2;
		CellConstraints cc = new CellConstraints();
		PanelBuilder jpan = new PanelBuilder(lay);
		jpan.setDefaultDialogBorder();
		jpan.getPanel().setOpaque(false);
		this.datenPanel = jpan;
		JScrollPane jscr = null;
		//String ywerte = "";
	
		try{
		// Lemmi 20101231: Harte Index-Zahlen für "jtf" durch sprechende Konstanten ersetzt ! 
		jtf[cKTRAEG] = new JRtaTextField("NIX",false); // kasse/kostenträger
		jtf[cARZT]   = new JRtaTextField("NIX",false); // arzt
		jtf[cREZDAT] = new JRtaTextField("DATUM",true); // rezeptdatum
		jtf[cBEGINDAT] = new JRtaTextField("DATUM",true); // spätester beginn
		jtf[cANZ1]   = new JRtaTextField("ZAHLEN",true); // Anzahl 1
		jtf[cANZ2]   = new JRtaTextField("ZAHLEN",true); // Anzahl 2
		jtf[cANZ3]   = new JRtaTextField("ZAHLEN",true); // Anzahl 3
		jtf[cANZ4]   = new JRtaTextField("ZAHLEN",true); // Anzahl 4
		jtf[cFREQ]   = new JRtaTextField("GROSS",true); // Frequenz		
		jtf[cDAUER]  = new JRtaTextField("ZAHLEN",true); // Dauer		
		jtf[cANGEL]  = new JRtaTextField("GROSS",true); // angelegt von
		jtf[cKASID]  = new JRtaTextField("GROSS",false); //kassenid
		jtf[cARZTID] = new JRtaTextField("GROSS",false); //arztid
		// ************ manches / nicht alles nachfolgende muss noch eingebaut werden.....
		jtf[cPREISGR] = new JRtaTextField("GROSS",false); //preisgruppe
		jtf[cHEIMBEW] = new JRtaTextField("GROSS",false); //heimbewohner
		jtf[cBEFREIT] = new JRtaTextField("GROSS",false); //befreit
		jtf[cPOS1] = new JRtaTextField("",false); //POS1		
		jtf[cPOS2] = new JRtaTextField("",false); //POS2
		jtf[cPOS3] = new JRtaTextField("",false); //POS3
		jtf[cPOS4] = new JRtaTextField("",false); //POS4
		jtf[cPREIS1] = new JRtaTextField("",false); //PREIS1
		jtf[cPREIS2] = new JRtaTextField("",false); //PREIS2
		jtf[cPREIS3] = new JRtaTextField("",false); //PREIS3
		jtf[cPREIS4] = new JRtaTextField("",false); //PREIS4
		jtf[cANLAGDAT] = new JRtaTextField("DATUM",false); // ANLAGEDATUM
		jtf[cANZKM]    = new JRtaTextField("",false); // KILOMETER
		jtf[cPATID]    = new JRtaTextField("",false); //id von Patient
		jtf[cPATINT]   = new JRtaTextField("",false); //pat_intern von Patient
		jtf[cZZSTAT]   = new JRtaTextField("",false); //zzstatus
		jtf[cHEIMBEWPATSTAM] = new JRtaTextField("",false); //Heimbewohner aus PatStamm
		jtf[cICD10] = new JRtaTextField("GROSS",false); //1. ICD10-Code
		jtf[cICD10_2] = new JRtaTextField("GROSS",false); //2. ICD10-Code
		jtf[cAKUTDATUM] = new JRtaTextField("DATUM",true); // spätester beginn
		jcmb[cRKLASSE] = new JRtaComboBox();
		jcmb[cVERORD] = new JRtaComboBox(new String[] {"Standard","Bes.VoBedarf","Langfrist-VO","Blanko-VO","Zahnarzt","Entlassmanagement"});
		jcmb[cVERORD].setSelectedIndex(0);
		
		
		jcb[cBEGRADR] = new JRtaCheckBox("vorhanden");
		int lang = SystemConfig.rezeptKlassenAktiv.size();
		strRezepklassenAktiv = new String[lang];
		for(int i = 0;i < lang;i++){
			jcmb[cRKLASSE].addItem(SystemConfig.rezeptKlassenAktiv.get(i).get(0));	
			// Lemmi 20110106: Belegung der Indices zur ComboBox für spätere Auswahlen:
			strRezepklassenAktiv[i] = SystemConfig.rezeptKlassenAktiv.get(i).get(1);  // hier speichern wir die Kürzel für spätere Aktivitäten
		}
		if(SystemConfig.AngelegtVonUser) {
			jtf[cANGEL].setText(Reha.aktUser);
			jtf[cANGEL].setEditable(false);
		}

		jpan.addLabel("Rezeptklasse auswählen",cc.xy(1, 3));
		jpan.add(jcmb[cRKLASSE],cc.xyw(3, 3,5));
		jcmb[cRKLASSE].setActionCommand("rezeptklasse");
		jcmb[cRKLASSE].addActionListener(this);
		/********************/
		
		if(this.neu){
			jcmb[cRKLASSE].setSelectedItem(SystemConfig.initRezeptKlasse);
		}else{
			for(int i = 0;i < lang;i++){
				if(this.vec.get(1).substring(0,2).equals(SystemConfig.rezeptKlassenAktiv.get(i).get(1))){
					jcmb[cRKLASSE].setSelectedIndex(i);
				}
			}
			jcmb[cRKLASSE].setEnabled(false);
		}			

		
		jpan.addSeparator("Rezeptkopf", cc.xyw(1,5,7));

		kassenLab = new JLabel("Kostenträger");
		kassenLab.setIcon(SystemConfig.hmSysIcons.get("kleinehilfe"));
		kassenLab.setHorizontalTextPosition(JLabel.LEFT);
		kassenLab.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent ev){
				if(!Rechte.hatRecht(Rechte.Rezept_editvoll,false)){
					return;
				}
				if(jtf[cKTRAEG].getText().trim().startsWith("?")){
					jtf[cKTRAEG].requestFocus();
				}else{
					jtf[cKTRAEG].setText("?"+jtf[cKTRAEG].getText().trim());
					jtf[cKTRAEG].requestFocus();
				}
				String[] suchkrit = new String[] {jtf[cKTRAEG].getText().replace("?", ""),jtf[cKASID].getText()};
				jtf[cKTRAEG].setText(String.valueOf(suchkrit[0]));
				kassenAuswahl(suchkrit);
			}
		});
	
		jtf[cKTRAEG].setName("ktraeger");
		jtf[cKTRAEG].addKeyListener(this);
		jpan.add(kassenLab,cc.xy(1,7));
		jpan.add(jtf[cKTRAEG],cc.xy(3,7));
		
		arztLab = new JLabel("verordn. Arzt");
		arztLab.setIcon(SystemConfig.hmSysIcons.get("kleinehilfe"));
		arztLab.setHorizontalTextPosition(JLabel.LEFT);
		arztLab.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent ev){
				if(!Rechte.hatRecht(Rechte.Rezept_editvoll,false)){
					return;
				}
				if(jtf[cARZT].getText().trim().startsWith("?")){
					jtf[cARZT].requestFocus();
				}else{
					jtf[cARZT].setText("?"+jtf[cARZT].getText().trim());
					jtf[cARZT].requestFocus();
				}
				String[] suchkrit = new String[] {jtf[cARZT].getText().replace("?", ""),jtf[cARZTID].getText()};
				jtf[cARZT].setText(String.valueOf(suchkrit[0]));
				arztAuswahl(suchkrit);
			}
		});

		jtf[cARZT].setName("arzt");
		jtf[cARZT].addKeyListener(this);		
		jpan.add(arztLab,cc.xy(5,7));
		jpan.add(jtf[cARZT],cc.xy(7,7));
		

		jtf[cREZDAT].setName("rez_datum");
		jtf[cREZDAT].addKeyListener(this);
		jpan.addLabel("Rezeptdatum",cc.xy(1,9));
		jpan.add(jtf[cREZDAT],cc.xy(3,9));
		
		jtf[cBEGINDAT].setName("lastdate");
		jpan.addLabel("spätester Beh.Beginn",cc.xy(5,9));
		jpan.add(jtf[cBEGINDAT],cc.xy(7,9));
		
		
		jpan.addSeparator("Behandlungsrelevante Diagnosen", cc.xyw(1,17,7));
		// hier der ICD-10 Code
		/********/
		jpan.addLabel("1. ICD-10-Code",cc.xy(1,19));
		jtf[cICD10].setName("icd10");
		jtf[cICD10].addKeyListener(this);
		jtf[cICD10].addFocusListener(this);
		jpan.add(jtf[cICD10],cc.xy(3,19));

		jpan.addLabel("2. ICD-10-Code",cc.xy(1, 21));
		jtf[cICD10_2].setName("icd10_2");
		jtf[cICD10_2].addKeyListener(this);
		jtf[cICD10_2].addFocusListener(this);
		jpan.add(jtf[cICD10_2],cc.xy(3,21));
		
		//damit der Klasse das Nachfolge-Objekt bekannt gemacht werden kann
		jcmb[cINDI] = new JRtaComboBox();
		jcmb[cINDI].setActionCommand("diaggruppe");
		//jcmb[cINDI].addActionListener(this);
		jta = new JRtaTextArea(jtf[cICD10_2],jcmb[cINDI]);
		//jta = new JTextArea();
		jta.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.5f)));
		jta.setFont(new Font("Courier",Font.PLAIN,11));
		jta.setLineWrap(true);
		jta.setName("notitzen");
		jta.setWrapStyleWord(true);
		jta.setEditable(true);
		jta.setBackground(Color.WHITE);
		jta.setForeground(Color.RED);
		//jta.addKeyListener(this);
		JScrollPane span = JCompTools.getTransparentScrollPane(jta);
		span.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.25f)));
		jpan.add(span,cc.xywh(5, 19,3,3,CellConstraints.FILL,CellConstraints.FILL));

		/*****************************/
		jpan.addLabel("Diagnosegruppe",cc.xy(1, 23));
		//jcmb[cINDI] = new JRtaComboBox();
		jpan.add(jcmb[cINDI],cc.xy(3, 23));
		
		
		                                //  1  2   3   4  5  6   7  8     9
		FormLayout dumlay = new FormLayout("p,2dlu,p,2dlu,p,2dlu,p,10dlu,p:g","p");
		PanelBuilder dumpan = new PanelBuilder(dumlay);
		dumpan.getPanel().setOpaque(false);
		CellConstraints dumcc = new CellConstraints();
		dumpan.addLabel("Leitsymptom.", dumcc.xy(1,1));
		
		jcb[cLEITSA] = new JRtaCheckBox("a");jcb[cLEITSA].setOpaque(false);jcb[cLEITSA].setEnabled(false);
		jcb[cLEITSB] = new JRtaCheckBox("b");jcb[cLEITSB].setOpaque(false);jcb[cLEITSB].setEnabled(false);
		jcb[cLEITSC] = new JRtaCheckBox("c");jcb[cLEITSC].setOpaque(false);jcb[cLEITSC].setEnabled(false);
		jcb[cLEITSX] = new JRtaCheckBox("indiv.");jcb[cLEITSX].setOpaque(false);jcb[cLEITSX].setEnabled(false);
		
		dumpan.add(jcb[cLEITSA],cc.xy(3, 1));
		dumpan.add(jcb[cLEITSB],cc.xy(5, 1));
		dumpan.add(jcb[cLEITSC],cc.xy(7, 1));
		dumpan.add(jcb[cLEITSX],cc.xy(9, 1));
		dumpan.getPanel().validate();
		jpan.add(dumpan.getPanel(),cc.xyw(5, 23, 3,CellConstraints.FILL,CellConstraints.FILL));
		
		
		leitsymta = new JTextArea(); 
		leitsymta.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.5f)));
		leitsymta.setFont(new Font("Courier",Font.PLAIN,11));
		leitsymta.setLineWrap(true);
		//////////////////////////////////////////Hier muß noch der Tabellennamen rein
		//jpan.addLabel("  ",cc.xy(1, 25));
		//jpan.addLabel("  ",cc.xy(1, 27));
		leitsymta.setName("leitsymt");
		leitsymta.setWrapStyleWord(true);
		leitsymta.setEditable(true);
		leitsymta.setBackground(Color.WHITE);
		leitsymta.setForeground(Color.RED);
		leitsymta.addKeyListener(this);
		jpan.addLabel("Freitext indiv. Leitsympt.",cc.xy(1, 25));
		JScrollPane span2 = JCompTools.getTransparentScrollPane(leitsymta);
		span2.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.25f)));
		span2.validate();
		jpan.add(span2,cc.xywh(3, 25,5,4,CellConstraints.FILL,CellConstraints.FILL));
		
		jpan.addSeparator("Verordnete Heilmittel", cc.xyw(1,30+yzugabe,7));
		
		jpan.addLabel("Heilmittel 1 / Anzahl",cc.xy(1, 32+yzugabe));
		
		jcmb[cLEIST1] = new JRtaComboBox();
		jcmb[cLEIST1].setName("leistung1");
		jcmb[cLEIST1].setActionCommand("leistung1");
		jcmb[cLEIST1].addActionListener(this);
		jpan.add(jcmb[cLEIST1],cc.xyw(3, 32+yzugabe,3));
		jtf[cANZ1].setName("anzahl1");
		jtf[cANZ1].addFocusListener(this);
		jpan.add(jtf[cANZ1],cc.xy(7, 32+yzugabe));
		
		jpan.addLabel("Heilmittel 2 / Anzahl",cc.xy(1, 34+yzugabe));

		jcmb[cLEIST2] = new JRtaComboBox();
		jcmb[cLEIST2].setName("leistung2");
		jcmb[cLEIST2].setActionCommand("leistung2");
		jcmb[cLEIST2].addActionListener(this);
		jpan.add(jcmb[cLEIST2],cc.xyw(3, 34+yzugabe,3));
		jtf[cANZ2].setName("anzahl2");
		jtf[cANZ2].addFocusListener(this);
		jpan.add(jtf[cANZ2],cc.xy(7, 34+yzugabe));
		
		jpan.addLabel("Heilmittel 3 / Anzahl",cc.xy(1, 36+yzugabe));

		jcmb[cLEIST3] = new JRtaComboBox();
		jcmb[cLEIST3].setName("leistung3");
		jcmb[cLEIST3].setActionCommand("leistung3");
		jcmb[cLEIST3].addActionListener(this);
		jpan.add(jcmb[cLEIST3],cc.xyw(3, 36+yzugabe,3));
		jtf[cANZ3].setName("anzahl3");
		jtf[cANZ3].addFocusListener(this);
		jpan.add(jtf[cANZ3],cc.xy(7, 36+yzugabe));
		
		jpan.addLabel("ergänzendes Heilmittel / Anzahl",cc.xy(1, 38+yzugabe));

		jcmb[cLEIST4] = new JRtaComboBox();
		jcmb[cLEIST4].setName("leistung4");
		jcmb[cLEIST4].setActionCommand("leistung4");
		jcmb[cLEIST4].addActionListener(this);
		jpan.add(jcmb[cLEIST4],cc.xyw(3, 38+yzugabe,3));
		jtf[cANZ4].setName("anzahl4");
		jtf[cANZ4].addFocusListener(this);
		jpan.add(jtf[cANZ4],cc.xy(7, 38+yzugabe));


		jpan.addSeparator("Ergänzende Angaben", cc.xyw(1,40+yzugabe,7));
		
		jcb[cTBANGEF] = new JRtaCheckBox("angefordert");
		jcb[cTBANGEF].setOpaque(false);
		jpan.addLabel("Therapiebericht",cc.xy(1, 42+yzugabe));
		jpan.add(jcb[cTBANGEF],cc.xy(3, 42+yzugabe));
		
		/*****Hausbesuchsgedöndse******/
		
		jcb[cHAUSB] = new JRtaCheckBox("Ja / Nein");
		jcb[cHAUSB].setOpaque(false);
		jcb[cHAUSB].setActionCommand("Hausbesuche");
		jcb[cHAUSB].addActionListener(this);
		jpan.addLabel("Hausbesuch",cc.xy(1, 44+yzugabe));
		jpan.add(jcb[cHAUSB],cc.xy(3, 44+yzugabe));

		jcb[cVOLLHB] = new JRtaCheckBox("abrechnen");
		jcb[cVOLLHB].setOpaque(false);
		jcb[cVOLLHB].setToolTipText("Nur aktiv wenn Patient Heimbewohner und Hausbesuch angekreuzt");
		jpan.addLabel("volle HB-Gebühr",cc.xy(5,44+yzugabe));
		if(neu){
			jcb[cVOLLHB].setEnabled(false);
			jcb[cVOLLHB].setSelected(false);
		}else{
			if(Reha.instance.patpanel.patDaten.get(44).equals("T")){
				// Wenn Heimbewohner
				if(this.vec.get(43).equals("T")){
					jcb[cVOLLHB].setEnabled(true);
					jcb[cVOLLHB].setSelected( (this.vec.get(61).equals("T") ? true : false));
				}else{
					jcb[cVOLLHB].setEnabled(false);
					jcb[cVOLLHB].setSelected(false);
				}
			}else{
				// Wenn kein(!!) Heimbewohner
				if(this.vec.get(43).equals("T")){
					jcb[cVOLLHB].setEnabled(false);
					jcb[cVOLLHB].setSelected(true);
				}else{
					jcb[cVOLLHB].setEnabled(false);
					jcb[cVOLLHB].setSelected(false);
				}
			}
		}
		jpan.add(jcb[cVOLLHB],cc.xy(7,44+yzugabe));
		
		jpan.addLabel("Behandlungsfrequenz",cc.xy(1, 46+yzugabe));		
		jpan.add(jtf[cFREQ],cc.xy(3, 46+yzugabe));	
		
		jtf[cFREQ].addFocusListener(this);
		jtf[cFREQ].setName("frequenz");

		jpan.addLabel("Dauer der Behandl. in Min.",cc.xy(5, 46+yzugabe));
		jpan.add(jtf[cDAUER],cc.xy(7, 46+yzugabe));

		jcb[cDRINGLICH] = new JRtaCheckBox("Dringlicher Behandlungsbedarf (innerhalb von 14 Tagen)");
		jcb[cDRINGLICH].setName("dringlich");
		jcb[cDRINGLICH].setActionCommand("dringlich");
		jcb[cDRINGLICH].addActionListener(this);
		jpan.add(jcb[cDRINGLICH],cc.xyw(3,48+yzugabe,5));
		

		
		therapzielta = new JTextArea(); 
		therapzielta.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.5f)));
		therapzielta.setFont(new Font("Courier",Font.PLAIN,11));
		therapzielta.setLineWrap(true);
		//////////////////////////////////////////Hier muß noch der Tabellennamen rein
		//jpan.addLabel("  ",cc.xy(1, 25));
		//jpan.addLabel("  ",cc.xy(1, 27));
		therapzielta.setName("therapziel");
		therapzielta.setWrapStyleWord(true);
		therapzielta.setEditable(true);
		therapzielta.setBackground(Color.WHITE);
		therapzielta.setForeground(Color.RED);
		therapzielta.addKeyListener(this);
		jpan.addLabel("Therapieziele / Befunde",cc.xy(1, 50+yzugabe));
		JScrollPane span3 = JCompTools.getTransparentScrollPane(therapzielta);
		span3.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.25f)));
		span3.validate();
		jpan.add(span3,cc.xywh(3, 50+yzugabe,5,3,CellConstraints.FILL,CellConstraints.FILL));

		jpan.addSeparator("Interne Daten", cc.xyw(1,58,7));
		
		jpan.addLabel("FarbCode im TK",cc.xy(1, 60));
		jcmb[cFARBCOD] = new JRtaComboBox();
		macheFarbcodes();
		jpan.add(jcmb[cFARBCOD],cc.xy(3, 60));

		jpan.addLabel("Barcode-Format",cc.xy(5, 60));
		jcmb[cBARCOD] = new JRtaComboBox(SystemConfig.rezBarCodName);
		jpan.add(jcmb[cBARCOD],cc.xy(7, 60));
		
		jpan.addLabel("Verordnungsart",cc.xy(1, 62));
		jcmb[cBEDARF] = new JRtaComboBox(new String[] {"Standard","Bes.VoBedarf","Langfrist-VO","Blanko-VO","Entlassmanagement"});
		jpan.add(jcmb[cBEDARF],cc.xy(3, 62));
		jcmb[cBEDARF].setActionCommand("besondererbedarf");
		jcmb[cBEDARF].addActionListener(this);
		
		jtf[cAKUTDATUM].setName("veraenderd");
		jpan.addLabel("Datum Akutereignis",cc.xy(5, 62));
		jpan.add(jtf[cAKUTDATUM],cc.xy(7, 62));
		jtf[cAKUTDATUM].setEnabled(false);
		
		jpan.addLabel("Angelegt von",cc.xy(5, 64));
		jpan.add(jtf[cANGEL],cc.xy(7, 64));
		jtf[cANGEL].setName("angelegtvon");
		
		Vector<Vector<String>> iks = SqlInfo.holeFelder("SELECT * FROM mandant");
		String[] mandanten = new String[iks.size()];
		int n = 0;
		for(Vector<String> v : iks) {
			String mandant = v.get(0) + " - " + v.get(1);
			mandanten[n] = mandant;
			n++;
		}
		
		jcmbIK = new JRtaComboBox(mandanten);
		String manSet = Betriebsumfeld.getAktIK() +" - "+ Betriebsumfeld.getAktMandant();
		jcmbIK.setSelectedItem(manSet);
		jpan.addLabel("Praxis:", cc.xy(1, 64));
		jpan.add(jcmbIK, cc.xy(3, 64));
		
		jpan.getPanel().validate();
		jscr = JCompTools.getTransparentScrollPane(jpan.getPanel());
		jscr.getVerticalScrollBar().setUnitIncrement(15);
		
		
		klassenReady = true;
		
		fuelleIndis((String)jcmb[cRKLASSE].getSelectedItem());	
		
		if(this.neu){
			if(this.neu && vec.size() <= 0){
				this.holePreisGruppe(Reha.instance.patpanel.patDaten.get(68).trim());
				this.ladePreisliste(jcmb[cRKLASSE].getSelectedItem().toString().trim(), preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]);
				this.fuelleIndis(jcmb[cRKLASSE].getSelectedItem().toString().trim());
				ladeZusatzDatenNeu();				
			}else if ( this.neu && vec.size() > 0 ){
				// Lemmi 20110101: bCtrlPressed zugefügt. Kopieren des letzten Rezepts des selben Patienten bei Rezept-Neuanlage
			  // das muß auch als Voraussetzung für doKopiereLetztesRezeptDesPatienten gemacht werden
				try{
					String sindi = String.valueOf(vec.get(44));
					String xkasse = String.valueOf(vec.get(37));
					String[] xartdbeh = new String[] {String.valueOf(vec.get(65)),String.valueOf(vec.get(66)),String.valueOf(vec.get(67)),String.valueOf(vec.get(68))};
					ladeZusatzDatenNeu();
					doKopiereLetztesRezeptDesPatienten();  // hier drin wird auch "ladeZusatzDatenNeu()" aufgerufen
					this.holePreisGruppe(xkasse);
					this.ladePreisliste(jcmb[cRKLASSE].getSelectedItem().toString().trim(), preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]);
					this.fuelleIndis(jcmb[cRKLASSE].getSelectedItem().toString().trim());
					for (int i = 0; i < 4; i++){
						if(xartdbeh[i].equals("")){
							jcmb[cLEIST1+i].setSelectedIndex(0);
						}else{
							jcmb[cLEIST1+i].setSelectedVecIndex(1, xartdbeh[i]);
						}
					}
					jcmb[cINDI].setSelectedItem(sindi);
				}catch(Exception ex){
					ex.printStackTrace();
				}

			}
		}else{
			this.holePreisGruppe(this.vec.get(37));
			this.ladePreisliste(jcmb[cRKLASSE].getSelectedItem().toString().trim(), preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]);
			this.fuelleIndis(jcmb[cRKLASSE].getSelectedItem().toString().trim());
			ladeZusatzDatenAlt();
		}
		

		jscr.validate();
		}catch(Exception ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null,"Fehler in der Erstellung des Rezeptfensters\n"+ex.getMessage());
		}

		// Lemmi 20101231: Merken der Originalwerte der eingelesenen Textfelder
		SaveChangeStatus();
		hmrBeachten = hmrCheckErforderlich();
		//System.out.println(hmrCheckErforderlich());
		return jscr;
	}
	

	
	
	public int leistungTesten(int combo,int veczahl){
		int retwert = 0;
		if(veczahl==-1 || veczahl==0){
			return retwert;
		}
		if(preisvec==null){
			return 0;
		}
		for(int i = 0;i<preisvec.size();i++){
			if( Integer.parseInt((String) ((Vector<?>)preisvec.get(i)).get(preisvec.get(i).size()-1)) == veczahl ){
				return i+1;
			}
		}
		return retwert;
	}
	public RezNeuanlage2021 getInstance(){
		return this;
	}
	public static String macheIcdString(String string) {
		
        String String1 = string.trim()
                               .substring(0, 1)
                               .toUpperCase();
        String String2 = string.trim()
                               .substring(1)
                               .toUpperCase()
                               .replace(" ", "")
                               .replace("*", "")
                               .replace("!", "")
                               .replace("+", "")
                               .replace("R", "")
                               .replace("L", "")
                               .replace("B", "")
                               .replace("G", "")
                               .replace("V", "")
                               .replace("Z", "");
        ;
        return String1 + String2;

    }
	
	private String chkIcdFormat(String string) {
		if(string.length() > 0) {
			int posDot = string.indexOf(".");
	        string = macheIcdString(string);
	        if ((string.length() > 3) && (posDot < 0)) {
	            String tmp1 = string.substring(0, 3);
	            String tmp2 = string.substring(3);
	            return tmp1 + "." + tmp2;
	        }
		}
        
        return string;
    }
	
	private void icdTextInDiagnose() {
		String icd1 = jtf[cICD10].getText();
		String icd2 = jtf[cICD10_2].getText();		
		//prüfen ob icd bereits in Diagnose
		String diagtext = jta.getText();
		diagtext.replace("\n", "");
		if(diagtext.contains("@@")) {
			String diag[] = diagtext.split("@@");
			if(diag.length > 1) {
				diagtext = diag[2];
				diagtext = diagtext.substring(1, diagtext.length());
			}
		}
		
		String sql1 = "SELECT titelzeile FROM icd10 WHERE schluessel1 LIKE '"+icd1+"';";
		String sql2 = "SELECT titelzeile FROM icd10 WHERE schluessel1 LIKE '"+icd2+"';";
		String icdText1 = SqlInfo.holeEinzelFeld(sql1);
		String icdText2 = SqlInfo.holeEinzelFeld(sql2);
		String textFeld = "@@ ICD10 1: "+icdText1+"\nICD10 2: "+icdText2+"@@\n"+diagtext;
		jta.setText(textFeld);
		//
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		//System.out.println(e.getActionCommand());
		if(e.getActionCommand().equals("rezeptklasse") && klassenReady){
			//System.out.println(jcmb[cRKLASSE].getSelectedItem().toString().trim());
			//System.out.println(preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]);
			this.ladePreisliste(jcmb[cRKLASSE].getSelectedItem().toString().trim(), preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]);			
			this.fuelleIndis((String)jcmb[cRKLASSE].getSelectedItem());
			hmrBeachten = hmrCheckErforderlich();
			return;
		}
		/*********************/		
		//System.out.println("2 - "+e.getActionCommand());
		if(e.getActionCommand().equals("verordnungsart") && klassenReady){
			if(jcmb[cVERORD].getSelectedIndex()==2){
				jcb[cBEGRADR].setEnabled(true);
				testeGenehmigung(jtf[cKASID].getText());
			}else{
				jcb[cBEGRADR].setSelected(false);
				jcb[cBEGRADR].setEnabled(false);				
			}
			return;
		}
		/*********************/		
		//System.out.println("3 - "+e.getActionCommand());
		if(e.getActionCommand().equals("speichern") ){
			doSpeichern();
	
			return;
		}
		/*********************/
		//System.out.println("4 - "+e.getActionCommand());
		if(e.getActionCommand().equals("abbrechen") ){
			new SwingWorker<Void,Void>(){
				@Override
				protected Void doInBackground() throws Exception {
					doAbbrechen();
					return null;
				}
			}.execute();
			return;
		}
		
		/*********************/
		//System.out.println("5 - "+e.getActionCommand());
		if(e.getActionCommand().equals("hmrcheck") ){
			if(komplettTest()) {
				doHmrCheck();
			}
			return;
		}
		
		/*********************/
		//System.out.println("6 - "+e.getActionCommand());
		if(e.getActionCommand().equals("Hausbesuche") ){
			if(jcb[cHAUSB].isSelected()){
				// Hausbesuch gewählt
				if(Reha.instance.patpanel.patDaten.get(44).equals("T")){
					//System.out.println("aktuelle Preisgruppe = "+preisgruppe);
					if(this.preisgruppe!=1 && (jcmb[cRKLASSE].getSelectedIndex() <= 1) ){
						jcb[cVOLLHB].setEnabled(true);						
					}
					SwingUtilities.invokeLater(new Runnable(){
					 	   public  void run()
					 	   {
								jcb[cHAUSB].requestFocus();		 		   
					 	   }
					});	
				}else{
					jcb[cVOLLHB].setEnabled(false);
					jcb[cVOLLHB].setSelected(true);
					SwingUtilities.invokeLater(new Runnable(){
					 	   public  void run()
					 	   {
								jcb[cHAUSB].requestFocus();		 		   
					 	   }
					});	
				}
			}else{
				// Haubesuch abgewählt
				jcb[cVOLLHB].setEnabled(false);
				jcb[cVOLLHB].setSelected(false);
				SwingUtilities.invokeLater(new Runnable(){
				 	   public  void run()
				 	   {
							jcb[cHAUSB].requestFocus();		 		   
				 	   }
				});	
			}
			return;
		}

		/*********************/	
		//System.out.println("7 - "+e.getActionCommand());
		if(e.getActionCommand().contains("leistung") && initReady){
			int lang = e.getActionCommand().length();
			doRechnen( Integer.valueOf( e.getActionCommand().substring(lang-1 ) ) );
			String test = (String)((JRtaComboBox)e.getSource()).getSelectedItem();
			if(test==null){
				return;
			}
			if(! test.equals("./.")){
				String id = (String)((JRtaComboBox)e.getSource()).getValue();
				Double preis = holePreisDouble(id,preisgruppe);
				if(preis <= 0.0){
					JOptionPane.showMessageDialog(null,"Diese Position ist für die gewählte Preisgruppe ungültig\nBitte weisen Sie in der Preislisten-Bearbeitung der Position ein Kürzel zu");
					((JRtaComboBox)e.getSource()).setSelectedIndex(0);
				}
			}
			return; 
		}
		
		/*********************/
		//System.out.println("8 - "+e.getActionCommand());
		if(e.getActionCommand().contains("dringlich")) {
			if(jtf[this.cREZDAT].getText().trim().length()==10) {
				if(!jcb[this.cDRINGLICH].isSelected()) {
					jtf[this.cBEGINDAT].setText(DatFunk.sDatPlusTage(jtf[this.cREZDAT].getText(), 28));
				}else {
					jtf[this.cBEGINDAT].setText(DatFunk.sDatPlusTage(jtf[this.cREZDAT].getText(), 14));
					JOptionPane.showMessageDialog(null, "Achtung!!!\nSpätester Behandlungsbeginn reduziert sich auf den "+jtf[this.cBEGINDAT].getText() );
				}
			}
			return;
		}
		
		/*********************/
		//System.out.println("9 - "+e.getActionCommand());
		if(e.getActionCommand().contains("diaggruppe")) {
			Vector<Vector<String>> vec = SqlInfo.holeFelder("select leitsyma,leitsymb,leitsymc,leitsymx from hmr_diagnosegruppe where diagnosegruppe ='"+jcmb[cINDI].getSelectedItem().toString()+"' LIMIT 1");
			if(vec.size() > 0) {
				jcb[this.cLEITSA].setEnabled(vec.get(0).get(0).equals("T"));jcb[this.cLEITSA].setSelected( (vec.get(0).get(0).equals("F") ? false : jcb[this.cLEITSA].isSelected() ) );
				jcb[this.cLEITSB].setEnabled(vec.get(0).get(1).equals("T"));jcb[this.cLEITSB].setSelected( (vec.get(0).get(0).equals("F") ? false : jcb[this.cLEITSB].isSelected() ) );
				jcb[this.cLEITSC].setEnabled(vec.get(0).get(2).equals("T"));jcb[this.cLEITSC].setSelected( (vec.get(0).get(0).equals("F") ? false : jcb[this.cLEITSC].isSelected() ) );
				jcb[this.cLEITSX].setEnabled(vec.get(0).get(3).equals("T"));jcb[this.cLEITSX].setSelected( (vec.get(0).get(0).equals("F") ? false : jcb[this.cLEITSX].isSelected() ) );
			}else if(vec.size() == 0) { // Zahnarzt oder Keine DiagGr.
				jcb[this.cLEITSA].setSelected(false);jcb[this.cLEITSA].setEnabled(false);
				jcb[this.cLEITSB].setSelected(false);jcb[this.cLEITSB].setEnabled(false);
				jcb[this.cLEITSC].setSelected(false);jcb[this.cLEITSC].setEnabled(false);
				jcb[this.cLEITSX].setSelected(false);jcb[this.cLEITSX].setEnabled(false);
			}
			return;
		}
		
		/*********************/
		//System.out.println("10 - "+e.getActionCommand());
		if(e.getActionCommand().contains("besondererbedarf")) {
			//System.out.println("gewählt = "+jcmb[cBEDARF].getSelectedIndex());
			if(jcmb[cBEDARF].getSelectedIndex() == 1) {
				jtf[cAKUTDATUM].setEnabled(true);
			}else {
				//System.out.println("gewählt -2 = "+jcmb[cBEDARF].getSelectedIndex());
				jtf[cAKUTDATUM].setText("  .  .    ");
				jtf[cAKUTDATUM].setEnabled(false);
			}
		}
	}
	
	private void testeGenehmigung(final String kassenid){
		new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				try{
					//System.out.println("kassenID = "+kassenid);
					String test = SqlInfo.holeEinzelFeld("select id from adrgenehmigung where ik = (select ik_kostent from kass_adr where id = '"+kassenid+"') LIMIT 1" );
					if(!test.isEmpty()){
						String meldung = "<html><b>Achtung!</b><br><br>Sie haben Verordnung außerhalb des Regelfalles gewählt!<br><br>Die Krankenkasse des Patienten besteht auf eine <br>"+
								"<b>Genehmigung für Verordnungen außerhalb des Regelfalles</b><br><br></html>";
						JOptionPane.showMessageDialog(null,meldung);
					}				
					//System.out.println("Rückgabe von test = "+test);
				}catch(Exception ex){
					JOptionPane.showMessageDialog(null,"Fehler!!!\n\nVermutlich haben Sie eines der letzten Updates verpaßt.\nFehlt zufällig die Tabelle adrgenehmigung?");
					ex.printStackTrace();
				}
				return null;
			}
		}.execute();
		
	}
	/* Lemmi 20101231: diese Routine ersetzt durch nachfolgenden Routine, weil diese Routine ein "Scheck" gegen die Zukunft ist!
	private boolean anzahlTest(){
		int itest;
		int maxanzahl=0,aktanzahl=0;
		for(int i = 2; i < 6;i++){
			itest = jcmb[i].getSelectedIndex();
			if(itest > 0){
				if(i == 2 ){
					try{
						maxanzahl = Integer.parseInt(jtf[i+2].getText());
					}catch(Exception ex){
						maxanzahl = 0;
					}
				}else{
					try{
						aktanzahl = Integer.parseInt(jtf[i+2].getText());
					}catch(Exception ex){
						aktanzahl = 0;
					}
					if(aktanzahl > maxanzahl){
						String cmd = "Sie haben mehrere Heilmittel mit unterschiedlicher Anzahl eingegeben.\n"+
						"Bitte geben Sie die Heilmittel so ein daß das Heilmittel mit der größten Anzahl oben steht\n"+
						"und dann (bezogen auf die Anzahl) in absteigender Reihgenfolge nach unten"; 
						JOptionPane.showMessageDialog(null,cmd);
						return false;
					}
				}
			}
		}
		return true;
	}
*/	
	// Lemmi 20101231: Harte Index-Zahlen für "jcmb" und "jtf" durch sprechende Konstanten ersetzt !
	// Lemmi Doku: prüft ob die Heilmittel überhaupt und in der korrekten Reihenfolge eingetragen worden sind
	private boolean anzahlTest(){
		int itest;
		int maxanzahl=0, aktanzahl=0;
		
		for(int i = 0; i < 4; i++) {  // über alle 4 Leistungs- und Anzahl-Positionen rennen
			itest = jcmb[cLEIST1 + i].getSelectedIndex();
			if(itest > 0){
				if(i == 0 ){  // die 1. Position besonders abfragen - diese muß existieren !
					try{
						maxanzahl = Integer.parseInt(jtf[cANZ1 + i].getText());
					}catch(Exception ex){
						maxanzahl = 0;
					}
				}else{
					try{
						aktanzahl = Integer.parseInt(jtf[cANZ1 + i].getText());
					}catch(Exception ex){
						aktanzahl = 0;
					}
					if(aktanzahl > maxanzahl){
						String cmd = "Sie haben mehrere Heilmittel mit unterschiedlicher Anzahl eingegeben.\n"+
						"Bitte geben Sie die Heilmittel so ein daß das Heilmittel mit der größten Anzahl oben steht\n"+
						"und dann (bezogen auf die Anzahl) in absteigender Reihgenfolge nach unten"; 
						JOptionPane.showMessageDialog(null,cmd);
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private void doRechnen(int comb){
		//unbelegt
	}
	private boolean hmrCheckErforderlich() {
		boolean notwendig = true;
		if(SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin).get(preisgruppen[jcmb[cRKLASSE].getSelectedIndex()])==0) {
			notwendig = false;
		}
		//System.out.println("HMRCheck notwendig = "+notwendig);
		return notwendig;
	}
	@SuppressWarnings("unused")
	private void mustHmrCheck(){
		if( SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin).get(preisgruppen[jcmb[cRKLASSE].getSelectedIndex()])==1  ){
			this.hmrcheck.setEnabled(false);
		}
	}
	private void doHmrCheck(){
		
		
		
		if( SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin).get(preisgruppen[jcmb[cRKLASSE].getSelectedIndex()])==0  ){
			this.hmrcheck.setEnabled(true);
			JOptionPane.showMessageDialog(null, "HMR-Check ist bei diesem Kostenträger nicht erforderlich");
			return;
		} else {			
						
			String diagnosegruppe = this.jcmb[cINDI].getSelectedItem().toString();
			
			
			
//			Alter berechnen
			int gebJahr = Integer.parseInt(Reha.instance.patpanel.patDaten.get(4).split("-")[0]);
			int gebMon = Integer.parseInt(Reha.instance.patpanel.patDaten.get(4).split("-")[1]);
			int gebDay = Integer.parseInt(Reha.instance.patpanel.patDaten.get(4).split("-")[2]);
			
			int aktJahr = LocalDate.now().getYear();
			int aktMon = LocalDate.now().getMonthValue();
			int aktDay = LocalDate.now().getDayOfMonth();
			
			
			int alter = aktJahr - gebJahr;
			
			if(!(aktMon >= gebMon && aktDay >= gebDay)) {
				alter--;
			}
			int rezJahr = Integer.parseInt(this.jtf[cREZDAT].getText().split("\\.")[2]);
			int rezMonat = Integer.parseInt(this.jtf[cREZDAT].getText().split("\\.")[1]);
			int rezTag = Integer.parseInt(this.jtf[cREZDAT].getText().split("\\.")[0]);
			
			LocalDate rezDatum = LocalDate.of(rezJahr, rezMonat, rezTag);
			
			
			LocalDate akutDatum = null;;
			
			if(!this.jtf[cAKUTDATUM].getText().contains(" ")) {
				int akutJahr = Integer.parseInt(this.jtf[cAKUTDATUM].getText().split("\\.")[2]);
				int akutMonat = Integer.parseInt(this.jtf[cAKUTDATUM].getText().split("\\.")[1]);
				int akutTag = Integer.parseInt(this.jtf[cAKUTDATUM].getText().split("\\.")[0]);
				
				akutDatum = LocalDate.of(akutJahr, akutMonat, akutTag);
			}
			
			
			int summe = 0;
			
			String hm1 = "";
			String hm2 = "";
			String hm3 = "";
			String hm4 = "";
			
			int itest = jcmb[cLEIST1].getSelectedIndex();
			if(itest > 0){
				hm1 = preisvec.get(itest-1).get(2);
				summe = summe + Integer.parseInt(jtf[cANZ1].getText());
			}
			itest = jcmb[cLEIST2].getSelectedIndex();
			if(itest > 0){
				hm2 = preisvec.get(itest-1).get(2);
				summe = summe + Integer.parseInt(jtf[cANZ2].getText());
			}
			itest = jcmb[cLEIST3].getSelectedIndex();
			if(itest > 0){
				hm3 = preisvec.get(itest-1).get(2);
				summe = summe + Integer.parseInt(jtf[cANZ3].getText());
			}
			itest = jcmb[cLEIST4].getSelectedIndex();
			if(itest > 0){
				hm4 = preisvec.get(itest-1).get(2);
			}
			int frequenz = 0;
			if(jtf[cFREQ].getText().contains("-")) {
				frequenz = Integer.parseInt(jtf[cFREQ].getText().split("-")[1]);
			} else {
				frequenz = Integer.parseInt(jtf[cFREQ].getText());
			}
			
			
			String msg = "";
			if(!jtf[cANZ1].getText().equals("") && !jtf[cANZ1].getText().equals("") &&
					!jtf[cANZ1].getText().equals("") && !jtf[cANZ1].getText().equals("")) {
				int behBeginn = 28;
				if(jcb[cDRINGLICH].isSelected()) {
					behBeginn = 14;
				}
				
				String voart = this.jcmb[cBEDARF].getSelectedItem().toString();
				
				String reznrhmr = "";
				if(!this.neu) {
					reznrhmr =  this.vec.get(1);
				}
				
				HMRCheck2021 hmr = new HMRCheck2021(aktuelleDisziplin, diagnosegruppe,
						jtf[cICD10].getText(), jtf[cICD10_2].getText(), voart, reznrhmr, 
						hm1, hm2, hm3, hm4,
						Integer.valueOf(jtf[cANZ1].getText()), Integer.valueOf(jtf[cANZ2].getText()),
						Integer.valueOf(jtf[cANZ3].getText()), Integer.valueOf(jtf[cANZ4].getText()),
						alter, behBeginn, akutDatum, rezDatum, frequenz);
				msg = hmr.isOkay();
			}
			
			if(!msg.equals("")) {
				JOptionPane.showMessageDialog(null, msg, "HMR-Fehler", JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null, "Tadaa - alles schick!", "HMR Okay", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	private void doBVBlhmCheck() {
		BVBlhmCheck checker = new BVBlhmCheck();
		String icd10_2 = "na";
		if(!jtf[cICD10_2].getText().equals("")) {
			icd10_2 = jtf[cICD10_2].getText();
		}
		int gebJahr = Integer.parseInt(Reha.instance.patpanel.patDaten.get(4).split("-")[0]);
		int gebMon = Integer.parseInt(Reha.instance.patpanel.patDaten.get(4).split("-")[1]);
		int gebDay = Integer.parseInt(Reha.instance.patpanel.patDaten.get(4).split("-")[2]);
		
		int aktJahr = LocalDate.now().getYear();
		int aktMon = LocalDate.now().getMonthValue();
		int aktDay = LocalDate.now().getDayOfMonth();
		
		
		int alter = aktJahr - gebJahr;
		
		if(!(aktMon >= gebMon && aktDay >= gebDay)) {
			alter--;
		}
		
		String bvblhm = checker.LHMbvbCheck((String)jcmb[cINDI].getSelectedItem(), jtf[cICD10].getText(), icd10_2, alter);
		if(bvblhm != null) {
			if(bvblhm.equals("LHM")) {
				String text = "Es wurde ein Langfristiger Heilmittelbedarf erkannt. \n"
						+ "Soll die Rezeptart geangepasst werden?";
				int answer = JOptionPane.showConfirmDialog(null, text, "LHM?", JOptionPane.YES_NO_OPTION);
				if(answer == JOptionPane.YES_OPTION) {
					jcmb[cBEDARF].setSelectedIndex(2);	
				}
			} else if(bvblhm.equals("BVB")) {
				String text = "Es wurde ein besonderer Verordnungsbedarf erkannt. \n"
						+ "Soll die Rezeptart geangepasst werden?";
				int answer = JOptionPane.showConfirmDialog(null, text, "BVB?", JOptionPane.YES_NO_OPTION);
				if(answer == JOptionPane.YES_OPTION) {
					jcmb[cBEDARF].setSelectedIndex(1);
				}
			} else if(bvblhm.equals("BVB+DAT!")) {
				String text = "Es wurde ein besonderer Verordnungsbedarf erkannt. \n"
						+ "Allerdings ist die Angabe eines Akut Ereignisses erforderlich. \n"
						+ "Ist ein Akut Ereigniss angegeben?";
				int answer = JOptionPane.showConfirmDialog(null, text, "BVB?", JOptionPane.YES_NO_OPTION);
				if(answer == JOptionPane.YES_OPTION) {
					jcmb[cBEDARF].setSelectedIndex(1);
					jtf[cAKUTDATUM].requestFocus();
				}
			} else {
				JLabel pane = new JLabel();
				pane.setText(bvblhm);
				JOptionPane.showConfirmDialog(null, new JScrollPane(pane), "Kontakt zum Arzt?", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	
	private boolean komplettTest(){
		if(jtf[cREZDAT].getText().trim().equals(".  .")){
			JOptionPane.showMessageDialog(null, "Ohne ein gültiges 'Rezeptdatum' kann ein Rezept nicht abgespeichert werden.");
			 SwingUtilities.invokeLater(new Runnable(){
			 	   public  void run()
			 	   {
						jtf[cREZDAT].requestFocus();
			 	   }
			});	   		
			return false;
		}
		
		if(jtf[cKTRAEG].getText().trim().equals("")){
			JOptionPane.showMessageDialog(null, "Ohne die Angabe 'Kostenträger' kann ein Rezept nicht abgespeichert werden.");
			 SwingUtilities.invokeLater(new Runnable(){
			 	   public  void run()
			 	   {
						jtf[cKTRAEG].requestFocus();
			 	   }
			});	   		
			return false;
		}
		if(jtf[cARZT].getText().trim().equals("")){
			JOptionPane.showMessageDialog(null, "Ohne die Angabe 'verordn. Arzt' kann ein Rezept nicht abgespeichert werden.");
			 SwingUtilities.invokeLater(new Runnable(){
			 	   public  void run()
			 	   {
						jtf[cARZT].requestFocus();
			 	   }
			});	   		
			return false;
		}
		if(jtf[cDAUER].getText().trim().equals("")){
			JOptionPane.showMessageDialog(null, "Ohne die Angabe 'Behandlungsdauer' kann ein Rezept nicht abgespeichert werden.");
			 SwingUtilities.invokeLater(new Runnable(){
			 	   public  void run()
			 	   {
						jtf[cDAUER].requestFocus();
			 	   }
			});	   		
			return false;
		}
		if(jtf[cANGEL].getText().trim().equals("")){
			JOptionPane.showMessageDialog(null, "Ohne die Angabe 'Angelegt von' kann ein Rezept nicht abgespeichert werden.");
			 SwingUtilities.invokeLater(new Runnable(){
			 	   public  void run()
			 	   {
						jtf[cANGEL].requestFocus();
			 	   }
			});	   		
			return false;
		}
		if( SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin).get(preisgruppen[jcmb[cRKLASSE].getSelectedIndex()])==1  ){
			if(jtf[cFREQ].getText().trim().equals("")){
				JOptionPane.showMessageDialog(null, "Ohne Angabe der 'Behandlungsfrequenz' kann ein GKV-Rezept nicht abgespeichert werden.");
				SwingUtilities.invokeLater(new Runnable(){
				 	   public  void run()
				 	   {
							jtf[cFREQ].requestFocus();
				 	   }
				});	   		
				return false;
			}
			//Test nach Diagnosegruppe
			if(jcmb[cINDI].getSelectedIndex() == 0) {
				JOptionPane.showMessageDialog(null, "Ohne Angabe der 'Diagnosegruppe' kann ein GKV-Rezept nicht abgespeichert werden.");
				SwingUtilities.invokeLater(new Runnable(){
				 	   public  void run()
				 	   {
							jcmb[cINDI].requestFocus();
				 	   }
				});
				return false;
			}
			//Leitsymptomatiktest
			if( (!jcb[this.cLEITSA].isSelected() && !jcb[this.cLEITSB].isSelected() && !jcb[this.cLEITSC].isSelected() && !jcb[this.cLEITSX].isSelected()) && (!AktuelleRezepte.isDentist(jcmb[cINDI].getSelectedItem().toString())) ) {
				JOptionPane.showMessageDialog(null, "Achtung!\nNicht zulässige Rezeptanlage.\nEs wurde keine Leitsymptomatik angegeben.");
				SwingUtilities.invokeLater(new Runnable(){
				 	   public  void run()
				 	   {
							jcb[cLEITSA].requestFocus();
				 	   }
				});
				return false;
			}
			//ob dem Nachfolgenden nach den neuen HMR so ist muß erst noch ermittelt werden
			if(jcb[this.cLEITSX].isSelected() && (this.leitsymta.getText().length() < 5) /*z.B. nur ein paar Zeilenumbrüche*/) {
				JOptionPane.showMessageDialog(null, "Achtung!\nNicht zulässige Rezeptanlage.\nEs wurde individuelle Leitsymptomatik angegeben\n"+
							"aber kein Freitext eingetragen");
				SwingUtilities.invokeLater(new Runnable(){
				 	   public  void run()
				 	   {
							jcb[cLEITSX].requestFocus();
				 	   }
				});
				return false;
			}
			if(!AktuelleRezepte.isDentist(jcmb[cINDI].getSelectedItem().toString())) {
				if(SqlInfo.holeFeld("select diagnosegruppe from hmr_diagnosegruppe where diagnosegruppe ='"+jcmb[cINDI].getSelectedItem().toString()+"' LIMIT 1" ).equals("")) {
					JOptionPane.showMessageDialog(null, "Achtung!\nNicht zulässige Rezeptanlage.\nDiagnosegruppe konnte nicht ermittelt werden\nHMR-Check ist nicht möglich");
					SwingUtilities.invokeLater(new Runnable(){
					 	   public  void run()
					 	   {
					 		  jcmb[cINDI].requestFocus();
					 	   }
					});
					return false;
				}
			}
			

		}
		return true;
	}
	private void ladePreisliste(String item,int preisgruppe){
		try{
			String[] artdbeh=null;
			if(!this.neu && jcmb[cLEIST1].getItemCount()>0){
				artdbeh = new String[]{
						String.valueOf(jcmb[cLEIST1].getValueAt(1)),String.valueOf(jcmb[cLEIST2].getValueAt(1)),
						String.valueOf(jcmb[cLEIST3].getValueAt(1)),String.valueOf(jcmb[cLEIST4].getValueAt(1))};
			}
			jcmb[cLEIST1].removeAllItems();
			jcmb[cLEIST2].removeAllItems();
			jcmb[cLEIST3].removeAllItems();
			jcmb[cLEIST4].removeAllItems();
			
			if(item.toLowerCase().contains("physio") ){
				aktuelleDisziplin = "Physio";
				nummer = "kg";
			}else if(item.toLowerCase().contains("massage")){
				aktuelleDisziplin = "Massage";
				nummer = "ma";
			}else if(item.toLowerCase().contains("ergo")){
				aktuelleDisziplin = "Ergo";
				nummer = "er";
			}else if(item.toLowerCase().contains("logo")){
				aktuelleDisziplin = "Logo";
				nummer = "lo";
			}else if(item.toLowerCase().contains("rehasport")){
				aktuelleDisziplin = "Rsport";
				nummer = "rs";
			}else if(item.toLowerCase().contains("funktions")){
				aktuelleDisziplin = "Ftrain";
				nummer = "ft";
			}else if(item.toLowerCase().contains("reha") && (!item.toLowerCase().contains("rehasport")) ){
				aktuelleDisziplin = "Reha";
				nummer = "rh";
			}else if(item.toLowerCase().contains("podo")){
				aktuelleDisziplin = "Podo";
				nummer = "po";
			}
			
			preisvec = SystemPreislisten.hmPreise.get(aktuelleDisziplin).get(preisgruppe);
			
			//System.out.println("Aktuelle Disziplin = "+aktuelleDisziplin);
			//System.out.println("Preisvektor  = "+preisvec);
			
			if(artdbeh!=null){
				ladePreise(artdbeh);	
			}else{
				ladePreise(null);
			}
			if(this.neu && SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin).get(preisgruppe) == 1){
				if(aktuelleDisziplin.equals("Physio") || aktuelleDisziplin.equals("Massage") || aktuelleDisziplin.equals("Ergo")){
					jcmb[cBARCOD].setSelectedItem("Muster 13");
				}else if(aktuelleDisziplin.equals("Logo")){
					jcmb[cBARCOD].setSelectedItem("Muster 13");
				}else if(aktuelleDisziplin.equals("Reha")){
					jcmb[cBARCOD].setSelectedItem("DIN A4 (REHA)");
				}else{
					jcmb[cBARCOD].setSelectedItem("Muster 13");
				}
			}else if(this.neu && aktuelleDisziplin.equals("Reha")){
				jcmb[cBARCOD].setSelectedItem("DIN A4 (REHA)");
			}else{
				if(this.neu){
					jcmb[cBARCOD].setSelectedItem("Muster 13");
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

	// Lemmi-Doku: Holt die passenden Inikationsschlüssel gemäß aktiver Disziplin
	private void fuelleIndis(String item){
		try{
			jcmb[cINDI].removeActionListener(this);
			if(jcmb[cINDI].getItemCount() > 0){
				jcmb[cINDI].removeAllItems();
			}
			if(item.toLowerCase().contains("reha") && (!item.toLowerCase().startsWith("rehasport")) ){
				return;
			}
			int anz = 0;
			String[] indis = null;
			if(    item.toLowerCase().contains("physio") 
				|| item.toLowerCase().contains("massage")
				|| item.toLowerCase().contains("rehasport")
				|| item.toLowerCase().contains("funktions")){
				anz = Reha.instance.patpanel.aktRezept.diagphysio.length;
				indis = Reha.instance.patpanel.aktRezept.diagphysio; 
			}else if(item.toLowerCase().contains("ergo")){
				anz = Reha.instance.patpanel.aktRezept.diagergo.length;
				indis = Reha.instance.patpanel.aktRezept.diagergo; 
			}else if(item.toLowerCase().contains("logo")){
				anz = Reha.instance.patpanel.aktRezept.diaglogo.length;
				indis = Reha.instance.patpanel.aktRezept.diaglogo; 
			}else if(item.toLowerCase().contains("podo")){
				anz = Reha.instance.patpanel.aktRezept.diagpodo.length;
				indis = Reha.instance.patpanel.aktRezept.diagpodo; 
			}
			for(int i = 0; i < anz; i++){
				jcmb[cINDI].addItem(indis[i]);
			}
			jcmb[cINDI].addActionListener(this);
			
		}catch(Exception ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null,"Fehler bei füller Inikat.schlüssel\n"+ex.getMessage());
					
		}
		
		return;
	}

	public void ladePreise(String[] artdbeh){
		try{
			if(preisvec.size()<=0){
				JOptionPane.showMessageDialog(null,"In der erforderlichen Preisliste sind noch keine Preise vorhanden!\nRezept kann nicht angelegt werden");
				return;
			}
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null,"In der erforderlichen Preisliste sind noch keine Preise vorhanden!\nRezept kann nicht angelegt werden");
			return;
		}
		jcmb[cLEIST1].setDataVectorWithStartElement(preisvec,0,9,"./.");
		jcmb[cLEIST2].setDataVectorWithStartElement(preisvec,0,9,"./.");
		jcmb[cLEIST3].setDataVectorWithStartElement(preisvec,0,9,"./.");
		jcmb[cLEIST4].setDataVectorWithStartElement(preisvec,0,9,"./.");
		if(artdbeh != null){ 
			for (int i = 0; i < 4; i++){
				if(artdbeh[i].equals("")){
					// Lemmi 20110116 ersetzt durch Zeile unten drunter. 
					//Alt:  jcmb[i+2].setSelectedIndex(0);
					jcmb[cLEIST1+i].setSelectedIndex(0);
				}else{
					// Lemmi 20110116 ersetzt durch Zeile unten drunter. 
					//Alt: jcmb[i+2].setSelectedVecIndex(1, artdbeh[i]);
					jcmb[cLEIST1+i].setSelectedVecIndex(1, artdbeh[i]);
				}
			}
		}
		return;		
	}
	
	
	
	
//	static int x = 1;
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyChar()=='?' && ((JComponent)arg0.getSource()).getName().equals("arzt")){
			String[] suchkrit = new String[] {jtf[cARZT].getText().replace("?", ""),jtf[cARZTID].getText()};
			jtf[cARZT].setText(String.valueOf(suchkrit[0]));
			arztAuswahl(suchkrit);
			return;
		}
		if(arg0.getKeyChar()=='?' && ((JComponent)arg0.getSource()).getName().equals("ktraeger")){
			String[] suchkrit = new String[] {jtf[cKTRAEG].getText().replace("?", ""),jtf[cKASID].getText()};
			jtf[cKTRAEG].setText(suchkrit[0]);
			kassenAuswahl(suchkrit);
			return;
		}
		if(arg0.getKeyCode()==27){  // Lemmi Doku: Taste "ESC" gedrückt: besser wäre die Abfrage nach "KeyEvent.VK_ESCAPE"
			doAbbrechen();
			return;
		}
		
		
		if( (arg0.getKeyCode()==KeyEvent.VK_ENTER || arg0.getKeyCode()==KeyEvent.VK_TAB)  && (
				((JComponent)arg0.getSource()).getName().equals("rez_datum")
				 ) 	){
			if(jtf[this.cREZDAT].getText().trim().length()==10) {
				if(!jcb[this.cDRINGLICH].isSelected()) {
					jtf[this.cBEGINDAT].setText(DatFunk.sDatPlusTage(jtf[this.cREZDAT].getText(), 28));
				}else {
					jtf[this.cBEGINDAT].setText(DatFunk.sDatPlusTage(jtf[this.cREZDAT].getText(), 14));
				}
			}
			return;
		}
		if( (arg0.getKeyCode()==KeyEvent.VK_ENTER)  && (
				((JComponent)arg0.getSource()).getName().equals("btnSpeichern")
				 ) 	){
			doSpeichern();
			return;
		}
		if( (arg0.getKeyCode()==KeyEvent.VK_ENTER || arg0.getKeyCode()==KeyEvent.VK_TAB)  && (
				((JComponent)arg0.getSource()).getName().equals("icd10")
				 ) 	){
			arg0.consume();
			if(arg0.isShiftDown()) {
				jtf[this.cBEGINDAT].requestFocus();
				return;
			}
			jtf[this.cICD10_2].requestFocus();
			return;
		}
		if( (arg0.getKeyCode()==KeyEvent.VK_ENTER || arg0.getKeyCode()==KeyEvent.VK_TAB)  && (
				((JComponent)arg0.getSource()).getName().equals("icd10_2")
				 ) 	){
			arg0.consume();
			if(arg0.isShiftDown()) {
				jtf[this.cICD10].requestFocus();
				return;
			}
			jta.requestFocus();
			return;
		}
		/*
		if( (arg0.getKeyCode()==KeyEvent.VK_ENTER || arg0.getKeyCode()==KeyEvent.VK_TAB) && (arg0.isControlDown() || arg0.isShiftDown()) && (
				 ((JComponent)arg0.getSource()).getName().equals("notitzen")
				 ) 	){
			if(arg0.isShiftDown()) {
				arg0.consume();
				jtf[this.cICD10_2].requestFocus();
			}else {
				arg0.consume();
				jcmb[this.cINDI].requestFocus();
			}
			return;
		}
		
		if( (this.neu) && (arg0.getKeyCode()==KeyEvent.VK_ENTER || arg0.getKeyCode()==KeyEvent.VK_TAB) &&  (
				 ((JComponent)arg0.getSource()).getName().equals("notitzen") )) {
			 if(jta.getText().length() <= 2) {
				 arg0.consume();
				 jta.setText("");
				 jcmb[this.cINDI].requestFocus();
			 }
			 return;
		}
		*/
		if((arg0.getKeyCode()==KeyEvent.VK_ENTER || arg0.getKeyCode()==KeyEvent.VK_TAB) && (arg0.isControlDown() || arg0.isShiftDown()) && (
				 ((JComponent)arg0.getSource()).getName().equals("leitsymt")
				 ) 	){
			if(arg0.isShiftDown()) {
				arg0.consume();
				jcb[this.cLEITSX].requestFocus();
			}else {
				arg0.consume();
				jcmb[this.cLEIST1].requestFocus();
			}
			return;
		}
		if( (this.neu) && (arg0.getKeyCode()==KeyEvent.VK_ENTER || arg0.getKeyCode()==KeyEvent.VK_TAB) &&  (
				 ((JComponent)arg0.getSource()).getName().equals("leitsymt") )) {
			if(leitsymta.getText().length() <= 2) {
				 arg0.consume();
				 leitsymta.setText("");
				 jcmb[this.cLEIST1].requestFocus();
			 }
			return;
		}
		
		if((arg0.getKeyCode()==KeyEvent.VK_ENTER || arg0.getKeyCode()==KeyEvent.VK_TAB) && (arg0.isControlDown() || arg0.isShiftDown()) && (
				 ((JComponent)arg0.getSource()).getName().equals("therapziel")
				 ) 	){
			if(arg0.isShiftDown()) {
				arg0.consume();
				jcb[this.cDRINGLICH].requestFocus();
			}else {
				arg0.consume();
				jcmb[this.cFARBCOD].requestFocus();
			}
			return;
		}
		if( (this.neu) && (arg0.getKeyCode()==KeyEvent.VK_ENTER || arg0.getKeyCode()==KeyEvent.VK_TAB) &&  (
				 ((JComponent)arg0.getSource()).getName().equals("therapziel") )) {
			if(therapzielta.getText().length() <= 2) {
				 arg0.consume();
				 therapzielta.setText("");
				 jcmb[this.cFARBCOD].requestFocus();
			 }
			return;
		}
		

		/* Lemmi Experimental
		if(arg0.getKeyCode()==KeyEvent.VK_S && arg0.isControlDown()){  // Lemmi Doku: Taste "Strg+S" gedrückt: besser wäre die Abfrage nach "KeyEvent.VK_ESCAPE"
			System.out.println("ausgelöst " + x++ );
			jcb[1].se
			ansehen: Favoriten: key map bei jta,, actiomap bei jcmb und jcb
		}
		else
			System.out.println("irgendwas" );
		*/	

		
/* Lemmi 20110101: Kopieren des letzten Rezepts des selben Patienten bei Rezept-Neuanlage
		if(arg0.getKeyCode() == KeyEvent.VK_CONTROL){  // Lemmi Doku: Taste "Ctrl" gedrückt
			if( this.neu )  // nur dann, wenn ein neues Rezept angelegt werden soll !
				doKopiereletztesRezeptDesPatienten();
		}
*/		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public void focusGained(FocusEvent arg0) {
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		if( ((JComponent)arg0.getSource()).getName() != null){
			if( ((JComponent)arg0.getSource()).getName().equals("rez_datum") ){
				return;
			}
			if( ((JComponent)arg0.getSource()).getName().equals("anzahl1") && neu ){
				String text = jtf[cANZ1].getText();
				jtf[cANZ2].setText(text);
				jtf[cANZ3].setText(text);
				jtf[cANZ4].setText(text);				
				return;
			}else if(((JComponent)arg0.getSource()).getName().equals("rez_datum")) {
				if(jtf[this.cREZDAT].getText().trim().length()==10) {
					if(!jcb[this.cDRINGLICH].isSelected()) {
						jtf[this.cBEGINDAT].setText(DatFunk.sDatPlusTage(jtf[this.cREZDAT].getText(), 28));
					}else {
						jtf[this.cBEGINDAT].setText(DatFunk.sDatPlusTage(jtf[this.cREZDAT].getText(), 14));
					}
				}
				
			}
			if (((JComponent)arg0.getSource()).getName().equals("icd10")) {
                String text = jtf[cICD10].getText();
                String icd10 = chkIcdFormat(text);
                jtf[cICD10].setText(icd10);
                icdTextInDiagnose();
                return;
            }
            if (((JComponent)arg0.getSource()).getName().equals("icd10_2")) {
                String text = jtf[cICD10_2].getText();
                String icd10 = chkIcdFormat(text);
                jtf[cICD10_2].setText(icd10);
                icdTextInDiagnose();
                return;
            }
            if (((JComponent)arg0.getSource()).getName().equals("frequenz")) {
                doBVBlhmCheck();
                return;
            }
            
			//Die ICD-10 Prüfung ist im HMR-Check wohl besser aufgehoben
			/*
			if( ((JComponent)arg0.getSource()).getName().equals("icd10") ){
				new SwingWorker<Void,Void>(){
					@Override
					protected Void doInBackground() throws Exception {
						if(SqlInfo.holeEinzelFeld("select id from icd10 where schluessel1 like '"+jtf[cICD10].getText().trim()+"%' LIMIT 1").equals("")){
							int frage = JOptionPane.showConfirmDialog(null, "<html>Achtung!!<br><br>Der ICD-10 Code <b>"+jtf[cICD10].getText().trim()+
									"</b> existiert nicht!<br>"+
									"Wollen Sie jetzt das ICD-10-Tool starten?<br><br></html>", "falscher ICD-10",JOptionPane.YES_NO_OPTION);
							if(frage==JOptionPane.YES_OPTION){
								new LadeProg(Reha.proghome+"ICDSuche.jar"+" "+Reha.proghome+" "+Reha.aktIK);
							}
						}
						return null;
					}
				}.execute();
				return;
			}
			*/
		}
	}
	
	private void arztAuswahl(String[] suchenach){
		jtf[cREZDAT].requestFocus();
		JRtaTextField tfArztNum = new JRtaTextField("",false);
		// einbauen A-Name +" - " +LANR;
		ArztAuswahl awahl = new ArztAuswahl(null,"ArztAuswahl",suchenach,new JRtaTextField[] {jtf[cARZT],new JRtaTextField("",false),jtf[cARZTID]},String.valueOf(jtf[cARZT].getText().trim()));
		awahl.setModal(true);
		awahl.setLocationRelativeTo(this);
		awahl.setVisible(true);
		SwingUtilities.invokeLater(new Runnable(){
		 	   public  void run(){
		 			jtf[cREZDAT].requestFocus();
		 	   }
		});
		try{
			String aneu = "";
			if(! Reha.instance.patpanel.patDaten.get(63).contains( ("@"+(aneu = jtf[cARZTID].getText().trim())+"@\n")) ){
				String aliste = Reha.instance.patpanel.patDaten.get(63)+ "@"+aneu+"@\n";
				Reha.instance.patpanel.patDaten.set(63,aliste+ "@"+aneu+"@\n");
				Reha.instance.patpanel.getLogic().arztListeSpeichernString(aliste,false,Reha.instance.patpanel.aktPatID);
				SwingUtilities.invokeLater(new Runnable(){
				 	   public  void run(){
				 			jtf[cREZDAT].requestFocus();
				 	   }
				});
				
				/*
				String msg = "Dieser Arzt ist bislang nicht in der Arztliste dieses Patienten.\n"+
				"Soll dieser Arzt der Ärzteliste des Patienten zugeordnet werden?";
				int frage = JOptionPane.showConfirmDialog(null,msg,"Wichtige Benutzeranfrage",JOptionPane.YES_NO_OPTION);
				if(frage == JOptionPane.YES_OPTION){
					String aliste = Reha.instance.patpanel.patDaten.get(63)+ "@"+aneu+"@\n";
					Reha.instance.patpanel.patDaten.set(63,aliste+ "@"+aneu+"@\n");
					Reha.instance.patpanel.getLogic().arztListeSpeichernString(aliste,false,Reha.instance.patpanel.aktPatID);
					SwingUtilities.invokeLater(new Runnable(){
					 	   public  void run(){
					 			jtf[REZDAT].requestFocus();
					 	   }
					});
				}else{
					SwingUtilities.invokeLater(new Runnable(){
					 	   public  void run(){
					 			jtf[REZDAT].requestFocus();
					 	   }
					});
				}
				*/
			}
		}catch(Exception ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Fehler beim Speichern der Arztliste!\n"+
					"Bitte notieren Sie Patient, Rezeptnummer und den Arzt den Sie der\n"+
					"Arztliste hinzufügen wollten und informieren Sie umgehend den Administrator.\n\nDanke");
		}
		awahl.dispose();
		awahl = null;

	}
	private void kassenAuswahl(String[] suchenach){
		jtf[cARZT].requestFocus();
		KassenAuswahl kwahl = new KassenAuswahl(null,"KassenAuswahl",suchenach,new JRtaTextField[] {jtf[cKTRAEG],jtf[cPATID],jtf[cKASID]},jtf[cKTRAEG].getText().trim());
		kwahl.setModal(true);
		kwahl.setLocationRelativeTo(this);
		kwahl.setVisible(true);
		SwingUtilities.invokeLater(new Runnable(){
		 	   public  void run(){
		 		   if(jtf[cKASID].getText().equals("")){
		 			   String meldung = "Achtung - kann Preisgruppe nicht ermitteln!\n"+
		 			   "Das bedeutet diese Rezept kann später nicht abgerechnet werden!\n\n"+
		 			   "Und bedenken Sie bitte Ihr Kürzel wird dauerhaft diesem Rezept zugeordnet....";
			 			  JOptionPane.showMessageDialog(null,meldung);		 			   
		 		   }else{
			 		   	holePreisGruppe(jtf[cKASID].getText().trim());
						ladePreisliste(jcmb[cRKLASSE].getSelectedItem().toString().trim(), preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]);
						hmrBeachten = hmrCheckErforderlich();
						jtf[cARZT].requestFocus();
		 		   }
		 	   }
		});
		kwahl.dispose();
		kwahl = null;
	}
	
	@SuppressWarnings("unused")
	private void holePreisGruppeMitWorker(String id){
		final String xid = id;
		new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				try{
					
					Vector<Vector<String>> vec = null;
					if(SystemConfig.mitRs){
						vec = SqlInfo.holeFelder("select preisgruppe,pgkg,pgma,pger,pglo,pgrh,pgpo,pgrs,pgft from kass_adr where id='"+xid+"' LIMIT 1");						
					}else{
						vec = SqlInfo.holeFelder("select preisgruppe,pgkg,pgma,pger,pglo,pgrh,pgpo from kass_adr where id='"+xid+"' LIMIT 1");
					}
				//System.out.println(vec);
				if(vec.size()>0){
					for(int i = 1; i < vec.get(0).size();i++){
						preisgruppen[i-1] = Integer.parseInt(vec.get(0).get(i))-1;
					}
					preisgruppe = Integer.parseInt((String)vec.get(0).get(0))-1;
					jtf[cPREISGR].setText((String)vec.get(0).get(0));
					ladePreisliste(jcmb[cRKLASSE].getSelectedItem().toString().trim(), preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]);
					fuelleIndis(jcmb[cRKLASSE].getSelectedItem().toString().trim());
					ladeZusatzDatenNeu();
				}else{
					JOptionPane.showMessageDialog(null,"Achtung - kann Preisgruppe nicht ermitteln - Rezept kann später nicht abgerechnet werden!");
				}
				}catch(Exception ex){
					ex.printStackTrace();
				}
				return null;
			}
			
		}.execute();
	}
	private void holePreisGruppe(String id){
		try{
			Vector<Vector<String>> vec = null;
			if(SystemConfig.mitRs){
				vec = SqlInfo.holeFelder("select preisgruppe,pgkg,pgma,pger,pglo,pgrh,pgpo,pgrs,pgft from kass_adr where id='"+id+"' LIMIT 1");						
			}else{
				vec = SqlInfo.holeFelder("select preisgruppe,pgkg,pgma,pger,pglo,pgrh,pgpo from kass_adr where id='"+id+"' LIMIT 1");
			}
		//Vector<Vector<String>> vec = SqlInfo.holeFelder("select preisgruppe,pgkg,pgma,pger,pglo,pgrh,pgpo from kass_adr where id='"+id+"' LIMIT 1");
		//System.out.println(vec);
		if(vec.size()>0){
			for(int i = 1; i < vec.get(0).size();i++){
				preisgruppen[i-1] = Integer.parseInt(vec.get(0).get(i))-1;
			}
			preisgruppe = Integer.parseInt((String)vec.get(0).get(0))-1;
			jtf[cPREISGR].setText((String)vec.get(0).get(0));
		}else{
			JOptionPane.showMessageDialog(null,"Achtung - kann Preisgruppe nicht ermitteln - Rezept kann später nicht abgerechnet werden!");
		}
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null,"Achtung - kann Preisgruppe nicht ermitteln - Rezept kann später nicht abgerechnet werden!\n"+
					"Untersuchen Sie die Krankenkasse im Kassenstamm un weisen Sie dieser Kasse die entsprechend Preisgruppe zu");
		}
	}
	
	/***********
	 * 
	 * 
	 */
	// Lemmi Doku: holt Daten aus dem aktuellen Patienten und trägt sie im Rezept ein
	private void ladeZusatzDatenNeu(){
		//String tests = "";
		if(vec.size() <= 0){
			jtf[cKTRAEG].setText(Reha.instance.patpanel.patDaten.get(13));
			jtf[cKASID].setText(Reha.instance.patpanel.patDaten.get(68)); //kassenid			
		}else{
			jtf[cKTRAEG].setText(vec.get(36));
			jtf[cKASID].setText(vec.get(37)); //kassenid						
		}

		if(jtf[cKASID].getText().trim().equals("")){
			JOptionPane.showMessageDialog(null,"Achtung - kann Preisgruppe nicht ermitteln - Rezept kann später nicht abgerechnet werden!");
		}
		jtf[cARZT].setText(Reha.instance.patpanel.patDaten.get(25)+ " - "+Reha.instance.patpanel.patDaten.get(26));
		// einbauen A-Name +" - " +LANR;
		jtf[cARZTID].setText(Reha.instance.patpanel.patDaten.get(67)); //arztid					
		//tests = Reha.instance.patpanel.patDaten.get(31);		// bef_dat = Datum der Befreiung
		jtf[cHEIMBEW].setText(Reha.instance.patpanel.patDaten.get(44)); //heimbewohn
		jtf[cBEFREIT].setText(Reha.instance.patpanel.patDaten.get(30)); //befreit
		jtf[cANZKM].setText(Reha.instance.patpanel.patDaten.get(48)); //kilometer
		jtf[cPATID].setText(Reha.instance.patpanel.patDaten.get(66)); //id von Patient
		jtf[cPATINT].setText(Reha.instance.patpanel.patDaten.get(29)); //pat_intern von Patient
		jcmb[cBEDARF].setSelectedIndex(0);
	}
	
	/***********
	 * 
	 * 
	 */
	// Lemmi Doku: lädt die Daten aus dem übergebenen Rezept-Vektor in die Dialog-Felder des Rezepts
	// 		       und setzt auch dei ComboBoxen und CheckBoxen
	private void ladeZusatzDatenAlt(){
		String test = StringTools.NullTest(this.vec.get(36));
		jtf[cKTRAEG].setText(test); //kasse
		test = StringTools.NullTest(this.vec.get(37));
		jtf[cKASID].setText(test);  //kid
		test = StringTools.NullTest(this.vec.get(15));
		jtf[cARZT].setText(test); //arzt
		test = StringTools.NullTest(this.vec.get(16));
		jtf[cARZTID].setText(test); //arztid
		test = StringTools.NullTest(this.vec.get(2));
		if(!test.equals("")){
			jtf[cREZDAT].setText(DatFunk.sDatInDeutsch(test));
		}
		test = StringTools.NullTest(this.vec.get(40));
		if(!test.equals("")){
			jtf[cBEGINDAT].setText(DatFunk.sDatInDeutsch(test));
		}
		int itest = StringTools.ZahlTest(this.vec.get(27));
		if(itest >=3){
			jcmb[cVERORD].setSelectedIndex(itest-3);
		}
		test = StringTools.NullTest(this.vec.get(42));
		jcb[cBEGRADR].setSelected((test.equals("T")?true:false));
		test = StringTools.NullTest(this.vec.get(43));
		jcb[cHAUSB].setSelected((test.equals("T")?true:false));

		test = StringTools.NullTest(this.vec.get(61));
		jcb[cVOLLHB].setSelected((test.equals("T")?true:false));
		
		test = StringTools.NullTest(this.vec.get(55));
		jcb[cTBANGEF].setSelected((test.equals("T")?true:false));
		test = StringTools.NullTest(this.vec.get(3));
		jtf[cANZ1].setText(test);
		test = StringTools.NullTest(this.vec.get(4));
		jtf[cANZ2].setText(test);
		test = StringTools.NullTest(this.vec.get(5));
		jtf[cANZ3].setText(test);
		test = StringTools.NullTest(this.vec.get(6));
		jtf[cANZ4].setText(test);
		
		itest = StringTools.ZahlTest(this.vec.get(8));
		jcmb[cLEIST1].setSelectedIndex(leistungTesten(0,itest));
		itest = StringTools.ZahlTest(this.vec.get(9));
		jcmb[cLEIST2].setSelectedIndex(leistungTesten(1,itest));
		itest = StringTools.ZahlTest(this.vec.get(10));
		jcmb[cLEIST3].setSelectedIndex(leistungTesten(2,itest));
		itest = StringTools.ZahlTest(this.vec.get(11));
		jcmb[cLEIST4].setSelectedIndex(leistungTesten(3,itest));
		
		test = StringTools.NullTest(this.vec.get(52));
		jtf[cFREQ].setText(test);
		test = StringTools.NullTest(this.vec.get(47));
		jtf[cDAUER].setText(test);
		
		test = StringTools.NullTest(this.vec.get(44));
		jcmb[cINDI].setSelectedItem(test);
		
		itest = StringTools.ZahlTest(this.vec.get(46));
		jcmb[cBARCOD].setSelectedIndex(itest);
		
		test = StringTools.NullTest(this.vec.get(45));
		jtf[cANGEL].setText(test);
		if(!test.trim().equals("")){
			jtf[cANGEL].setEnabled(false);				
		}
		jta.setText( StringTools.NullTest(this.vec.get(23)) );
		if(!jtf[cKASID].getText().equals("")){
			holePreisGruppe(jtf[cKASID].getText().trim());				
		}else{
			JOptionPane.showMessageDialog(null, "Ermittlung der Preisgruppen erforderlich");				
		}
		
		jtf[cHEIMBEW].setText(Reha.instance.patpanel.patDaten.get(44)); //heimbewohn
		jtf[cBEFREIT].setText(Reha.instance.patpanel.patDaten.get(30)); //befreit
		jtf[cANZKM].setText(Reha.instance.patpanel.patDaten.get(48)); //kilometer
		jtf[cPATID].setText(this.vec.get(38)); //id von Patient
		jtf[cPATINT].setText(this.vec.get(0)); //pat_intern von Patient
		
		//ICD-10
		jtf[cICD10].setText(this.vec.get(71));
		jtf[cICD10_2].setText(this.vec.get(72));
		
		itest = StringTools.ZahlTest(this.vec.get(57));
		if(itest >= 0){
			jcmb[cFARBCOD].setSelectedItem( (String)SystemConfig.vSysColsBedeut.get(itest) );			
		}
		
		test = StringTools.NullTest(this.vec.get(74));
		jcb[cLEITSA].setSelected((test.equals("T")?true:false));
		test = StringTools.NullTest(this.vec.get(75));
		jcb[cLEITSB].setSelected((test.equals("T")?true:false));
		test = StringTools.NullTest(this.vec.get(76));
		jcb[cLEITSC].setSelected((test.equals("T")?true:false));
		test = StringTools.NullTest(this.vec.get(77));
		jcb[cLEITSX].setSelected((test.equals("T")?true:false));
		
		leitsymta.setText(StringTools.NullTest(this.vec.get(78)));
		
		test = StringTools.NullTest(this.vec.get(79));
		jcb[cDRINGLICH].setSelected((test.equals("T")?true:false));
		
		therapzielta.setText(StringTools.NullTest(this.vec.get(80)));
		
		itest = StringTools.ZahlTest(this.vec.get(27)); //Feld rezeptart
		if(itest >=3) {
			jcmb[this.cBEDARF].setSelectedIndex(itest-3);
		}
		
		if( this.vec.get(AktuelleRezepte.cAKUTEREIGNIS).length()==10){
			jtf[cAKUTDATUM].setText(DatFunk.sDatInDeutsch(this.vec.get(AktuelleRezepte.cAKUTEREIGNIS)));	
		}
		
		String mand = SqlInfo.holeEinzelFeld("SELECT praxisname FROM mandant WHERE ik = '"+this.vec.get(82)+"';");
		this.jcmbIK.setSelectedItem(this.vec.get(82) + " - " + mand);   
		
	}
	
	/********************************/
	@SuppressWarnings("unused")
	private Double holePreisDoubleX(String pos,int ipreisgruppe){
		Double dbl = 0.0;
		for(int i = 0; i < preisvec.size();i++){
			if(this.preisvec.get(i).get(0).equals(pos)){
				if(this.preisvec.get(i).get(3).equals("")){
					return dbl;
				}
				return Double.parseDouble(this.preisvec.get(i).get(3));
			}
		}
		return dbl;
	}
	private Double holePreisDouble(String id,int ipreisgruppe){
		Double dbl = 0.0;
		for(int i = 0; i < preisvec.size();i++){
			if(this.preisvec.get(i).get(9).equals(id)){
				if(this.preisvec.get(i).get(1).equals("")){
					return dbl;
				}
				return Double.parseDouble(this.preisvec.get(i).get(3));
			}
		}
		return dbl;
	}

	/*********************************/
	@SuppressWarnings("unused")
	private String[] holePreis(int ivec,int ipreisgruppe){
		if(ivec > 0){
			int prid = Integer.valueOf((String) this.preisvec.get(ivec).get(this.preisvec.get(ivec).size()-1));
			Vector<?> xvec = ((Vector<?>)this.preisvec.get(ivec));
			return new String[] {(String)xvec.get(3),(String)xvec.get(2)};
		}else{
			return new String[] {"0.00",""};
		}
	}
	/***********
	 * 
	 * 
	 */
	/**************************************/
	
	private void doSpeichern() {
		SwingUtilities.invokeLater(new Runnable(){
		 	   public  void run()
		 	   {
					try{
						/*
						if(! anzahlTest()){
							return;
						}
						*/
						if(getInstance().neu){
							doSpeichernNeu();
						}else{
							doSpeichernAlt();
						}
					}catch(Exception ex){
						ex.printStackTrace();
					}
					 		   
		 	   }
		});	
	}
	private void doSpeichernAlt(){
		try{
			if(!komplettTest()){
				return;
			}
			setCursor(Cursors.wartenCursor);
			String stest = "";
			int itest = -1;
			StringBuffer sbuf = new StringBuffer();
			sbuf.append("update verordn set ktraeger='"+jtf[cKTRAEG].getText()+"', ");
			sbuf.append("kid='"+jtf[cKASID].getText()+"', ");
			sbuf.append("arzt='"+jtf[cARZT].getText()+"', ");
			sbuf.append("arztid='"+jtf[cARZTID].getText()+"', ");
			stest = jtf[cREZDAT].getText().trim();
			if(stest.equals(".  .")){
				stest = DatFunk.sHeute();
			}
			boolean neuerpreis = RezTools.neuePreisNachRezeptdatumOderStichtag(aktuelleDisziplin, preisgruppe, String.valueOf(stest),false,Reha.instance.patpanel.vecaktrez);
			sbuf.append("rez_datum='"+DatFunk.sDatInSQL(stest)+"', ");
			int row = Reha.instance.patpanel.aktRezept.tabelleaktrez.getSelectedRow();
			if(row >= 0){
				Reha.instance.patpanel.aktRezept.tabelleaktrez.getModel().setValueAt(stest, row, 2);	
			}
			String stest2 = jtf[cBEGINDAT].getText().trim();
			if(stest2.equals(".  .")){
				//Preisgruppe holen
				int pg = Integer.parseInt(jtf[cPREISGR].getText())-1;
				//Frist zwischen Rezeptdatum und erster Behandlung
				int frist = (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(0)).get(pg);
				//Kalendertage
				if((Boolean) ((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(1)).get(pg)){
					stest2 = DatFunk.sDatPlusTage(stest, frist);					
				}else{ //Werktage
					boolean mitsamstag = (Boolean)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(4)).get(pg);
					stest2 = HMRCheck.hmrLetztesDatum(stest, (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(0)).get(pg),mitsamstag );
				}
			}
			if(row >= 0){
				Reha.instance.patpanel.aktRezept.tabelleaktrez.getModel().setValueAt(stest2, row, 4);	
			}
			sbuf.append("lastdate='"+DatFunk.sDatInSQL(stest2)+"', ");
			sbuf.append("lasteddate='"+DatFunk.sDatInSQL(DatFunk.sHeute())+"', ");
			sbuf.append("lastedit='"+Reha.aktUser+"', ");
			Integer rezeptart = Integer.valueOf(jcmb[cBEDARF].getSelectedIndex()) + 3;
			sbuf.append("rezeptart='"+rezeptart.toString()+"', ");
			sbuf.append("begruendadr='"+(jcb[cBEGRADR].isSelected() ? "T" : "F")+"', ");
			sbuf.append("hausbes='"+(jcb[cHAUSB].isSelected() ? "T" : "F")+"', ");
			if(jcb[cHAUSB].isSelected()){
				if(!this.vec.get(64).equals(jtf[cANZ1].getText())){
					int frage = JOptionPane.showConfirmDialog(null,"Achtung!\n\nDie Anzahl Hausbesuche = "+this.vec.get(64)+"\n"+
							"Die Anzahl des ersten Heilmittels = "+jtf[cANZ1].getText()+"\n\n"+
							"Soll die Anzahl Hausbesuche ebenfalls auf "+jtf[cANZ1].getText()+" gesetzt werden?","Benutzeranfrage",JOptionPane.YES_NO_OPTION);
					if(frage == JOptionPane.YES_OPTION){
						sbuf.append("anzahlhb='"+jtf[cANZ1].getText()+"', ");		
					}
				}
			}
			sbuf.append("arztbericht='"+(jcb[cTBANGEF].isSelected() ? "T" : "F")+"', ");
			sbuf.append("anzahl1='"+jtf[cANZ1].getText()+"', ");		
			sbuf.append("anzahl2='"+jtf[cANZ2].getText()+"', ");
			sbuf.append("anzahl3='"+jtf[cANZ3].getText()+"', ");
			sbuf.append("anzahl4='"+jtf[cANZ4].getText()+"', ");
			itest = jcmb[cLEIST1].getSelectedIndex();
			
			if(itest > 0){
				sbuf.append("art_dbeh1='"+preisvec.get(itest-1).get(9)+"', ");
				sbuf.append("preise1='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
				sbuf.append("pos1='"+preisvec.get(itest-1).get(2)+"', ");
				sbuf.append("kuerzel1='"+preisvec.get(itest-1).get(1)+"', ");
				
			}else{
				sbuf.append("art_dbeh1='0', ");
				sbuf.append("preise1='0.00', ");
				sbuf.append("pos1='', ");
				sbuf.append("kuerzel1='', ");

			}
			itest = jcmb[cLEIST2].getSelectedIndex();
			if(itest > 0){
				sbuf.append("art_dbeh2='"+preisvec.get(itest-1).get(9)+"', ");
				sbuf.append("preise2='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
				sbuf.append("pos2='"+preisvec.get(itest-1).get(2)+"', ");
				sbuf.append("kuerzel2='"+preisvec.get(itest-1).get(1)+"', ");
			}else{
				sbuf.append("art_dbeh2='0', ");
				sbuf.append("preise2='0.00', ");
				sbuf.append("pos2='', ");
				sbuf.append("kuerzel2='', ");
			}
			itest = jcmb[cLEIST3].getSelectedIndex();
			if(itest > 0){
				sbuf.append("art_dbeh3='"+preisvec.get(itest-1).get(9)+"', ");
				sbuf.append("preise3='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
				sbuf.append("pos3='"+preisvec.get(itest-1).get(2)+"', ");
				sbuf.append("kuerzel3='"+preisvec.get(itest-1).get(1)+"', ");
			}else{
				sbuf.append("art_dbeh3='0', ");
				sbuf.append("preise3='0.00', ");
				sbuf.append("pos3='', ");
				sbuf.append("kuerzel3='', ");
			}
			itest = jcmb[cLEIST4].getSelectedIndex();
			if(itest > 0){
				sbuf.append("art_dbeh4='"+preisvec.get(itest-1).get(9)+"', ");
				sbuf.append("preise4='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
				sbuf.append("pos4='"+preisvec.get(itest-1).get(2)+"', ");
				sbuf.append("kuerzel4='"+preisvec.get(itest-1).get(1)+"', ");
			}else{
				sbuf.append("art_dbeh4='0', ");
				sbuf.append("preise4='0.00', ");
				sbuf.append("pos4='', ");
				sbuf.append("kuerzel4='', ");
			}
			sbuf.append("frequenz='"+jtf[cFREQ].getText()+"', ");
			sbuf.append("dauer='"+jtf[cDAUER].getText()+"', ");
			if(jcmb[cINDI].getSelectedIndex() > 0){
				sbuf.append("indikatschl='"+(String)jcmb[cINDI].getSelectedItem()+/**Leitsymt einbauen**/"', ");			
			}else{
				sbuf.append("indikatschl='"+"keine DiagGr."+"', ");			
			}
			sbuf.append("barcodeform='"+Integer.valueOf(jcmb[cBARCOD].getSelectedIndex()).toString()+"', ");
			sbuf.append("angelegtvon='"+jtf[cANGEL].getText()+"', ");
			sbuf.append("preisgruppe='"+jtf[cPREISGR].getText()+"', ");
			
			if(jcmb[cFARBCOD].getSelectedIndex() > 0){
				// Lemmi Frage: was bedeutet "14+" in der folgenden Zeile:
				sbuf.append("farbcode='"+Integer.valueOf(14+jcmb[cFARBCOD].getSelectedIndex()-1).toString()+"', ");	
			}else{
				sbuf.append("farbcode='-1', ");
			}
			/*********************2021-Änderungen*************************/
			sbuf.append("leitsyma='"+(jcb[this.cLEITSA].isSelected() ? "T" : "F")+"', ");
			sbuf.append("leitsymb='"+(jcb[this.cLEITSB].isSelected() ? "T" : "F")+"', ");
			sbuf.append("leitsymc='"+(jcb[this.cLEITSC].isSelected() ? "T" : "F")+"', ");
			sbuf.append("leitsymx='"+(jcb[this.cLEITSX].isSelected() ? "T" : "F")+"', ");
			sbuf.append("leitsymtext='"+leitsymta.getText()+"', ");
			sbuf.append("dringlich='"+(jcb[this.cDRINGLICH].isSelected() ? "T" : "F")+"', ");
			sbuf.append("therapziel='"+therapzielta.getText()+"', ");
			String akutDatum = jtf[cAKUTDATUM].getText();
			if(akutDatum.length()==10 && !akutDatum.equals("  .  .    ")) {
				sbuf.append("veraenderd='"+DatFunk.sDatInSQL(jtf[cAKUTDATUM].getText())+"' ,");
			}else {
				sbuf.append("veraenderd=null ,");
			}
			////System.out.println("Speichern bestehendes Rezept -> Preisgruppe = "+jtf[cPREISGR].getText());
			Integer izuzahl = Integer.valueOf(jtf[cPREISGR].getText());
			String szzstatus = "";
			@SuppressWarnings("unused")
			String unter18 = "F";
			for(int i = 0; i < 1;i++){
				if(SystemPreislisten.hmZuzahlRegeln.get(aktuelleDisziplin).get(izuzahl-1) <= 0){
					szzstatus = "0";
					break;
				}
				if(aktuelleDisziplin.equals("Reha")){
					szzstatus = "0";
					break;
				}
				if(aktuelleDisziplin.equals("Rsport") || aktuelleDisziplin.equals("Ftrain")){
					szzstatus = "0";
					break;
				}
				////System.out.println("ZuzahlStatus = Zuzahlung (zunächst) erforderlich, prüfe ob befreit oder unter 18");
				if(Reha.instance.patpanel.patDaten.get(30).equals("T")){
					//System.out.println("aktuelles Jahr ZuzahlStatus = Patient ist befreit");
					if(this.vec.get(14).equals("T")){
						szzstatus = "1";
					}else{
						
						if(RezTools.mitJahresWechsel(DatFunk.sDatInDeutsch(this.vec.get(2)))){
							
							String vorjahr = Reha.instance.patpanel.patDaten.get(69); 
							if(vorjahr.trim().equals("")){
								//Nur einspringen wenn keine Vorjahrbefreiung vorliegt.
								//Tabelle mit Einzelterminen auslesen ob Sätze vorhanden
								//wenn Sätze = 0 und bereits im Befreiungszeitraum dann "0", ansonsten "2" 
								//Wenn Sätze > 0 dann ersten Satz auslesen Wenn Datum < Befreiung-ab dann "2" ansonsten "0" 
								if(Reha.instance.patpanel.aktRezept.tabaktterm.getRowCount() > 0){
									// es sind bereits Tage verzeichnet.
									String ersterTag = Reha.instance.patpanel.aktRezept.tabaktterm.getValueAt(0, 0).toString();
									try{
										if(DatFunk.TageDifferenz(DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(41)), ersterTag) >= 0){
											//Behandlung liegt nach befr_ab
											szzstatus = "0";
										}else{
											//Behandlung liegt vor befr_ab
											szzstatus = "2";
										}
									}catch(Exception ex){
										JOptionPane.showMessageDialog(null,"Fehler:\nBefreit ab, im Patientenstamm nicht oder falsch eingetragen");
									}
									
								}else{
									//es sind noch keine Sätze verzeichnet
									if(DatFunk.TageDifferenz(DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(41)), DatFunk.sHeute()) >= 0){
										//Behandlung muß nach befr_ab liegen
										szzstatus = "0";
									}else{
										//Behandlung kann auch vor befr_ab liegen
										szzstatus = "2";
									}
								}
								/*
								if(this.vec.get(34).indexOf(vorjahr)>=0){
									szzstatus = "2";
								}else{
									szzstatus = "0";
								}
								*/
							}else{
								szzstatus = "0";
							}
						}else{
							szzstatus = "0";
						}

						//Im Patientenstamm liegt eine aktuelle befreiung vor  
						//testen ob sich das Rezept über den Jahreswechsel erstreckt
						//wenn ja war er damals auch befreit, wenn ja Status == 0
						//wenn nein Status == 2 == nicht befreit und nicht bezahlt
						//szzstatus = "0";
						/*
						if(Reha.instance.patpanel.aktRezept.tabaktterm.getRowCount() > 0){
							String ersterTag = Reha.instance.patpanel.aktRezept.tabaktterm.getValueAt(0, 0).toString();
							if(DatFunk.TageDifferenz(Reha.instance.patpanel.patDaten.get(41), ersterTag) >= 0){
								
							}
						}else{
							//noch keine Behandlung
							if(DatFunk.TageDifferenz(Reha.instance.patpanel.patDaten.get(41), DatFunk.sHeute()) >= 0){
								System.out.println("Noch keine Behandlung vermerkt aber bereits im Befr.Zeitraum angekommen");
								System.out.println(DatFunk.TageDifferenz(Reha.instance.patpanel.patDaten.get(41), DatFunk.sHeute()));
								szzstatus = "0";
							}
							
						}
						*/ 
					}
					break;
				}
				
				if(DatFunk.Unter18(DatFunk.sHeute(), DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)))){
					//System.out.println("ZuzahlStatus = Patient ist unter 18 also befreit...");
					int aj = Integer.parseInt(SystemConfig.aktJahr)-18;
					String gebtag = DatFunk.sHeute().substring(0,6)+Integer.toString(aj);
					long tage = DatFunk.TageDifferenz(DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)) ,gebtag);

					//System.out.println("Differenz in Tagen = "+tage);
					//System.out.println("Geburtstag = "+gebtag);
					
					if(tage < 0 && tage >= -45){
						JOptionPane.showMessageDialog(null ,"Achtung es sind noch "+(tage*-1)+" Tage bis zur Volljährigkeit\n"+
								"Unter Umständen wechselt der Zuzahlungsstatus im Verlauf dieses Rezeptes");
						szzstatus = "3";
					}else{
						szzstatus = "0";
					}
					//szzstatus = "0";
					unter18 = "T";
					break;
				}
				/**********************/
				if(this.vec.get(14).equals("T") || 
						(new Double((String)this.vec.get(13)) > 0.00) ){
					szzstatus = "1";
				}else{
					// hier testen ob erster Behandlungstag bereits ab dem Befreiungszeitraum
					szzstatus = "2";				
				}
			}
			/******/
			
			String[] lzv= holeLFV("anamnese", "pat5", "pat_intern", jtf[cPATINT].getText(), nummer.toUpperCase().substring(0,2));
			if(!  lzv[0].equals("") ){
				if(!jta.getText().contains(lzv[0]) ){
					int frage = JOptionPane.showConfirmDialog(null, "Für den Patient ist eine Langfristverordnung eingetragen die diese Verordnung noch nicht einschließt.\n\n"+lzv[1]+
							"\n\nWollen Sie diesen Eintrag dieser Verordnung zuweisen?",
							"Achtung wichtige Benutzeranfrage",JOptionPane.YES_NO_OPTION);
					if(frage==JOptionPane.YES_OPTION){
						jta.setText(jta.getText()+"\n"+lzv[0]);
					}
				}
			}
			/*****/
			
			sbuf.append("unter18='"+((DatFunk.Unter18(DatFunk.sHeute(), DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)))) ? "T', " : "F', "));
			sbuf.append("zzstatus='"+szzstatus+"', ");
			//int leistung;
			//String[] str;
			sbuf.append("diagnose='"+StringTools.Escaped(jta.getText())+"', ");
			sbuf.append("jahrfrei='"+Reha.instance.patpanel.patDaten.get(69)+"', ");
			sbuf.append("heimbewohn='"+jtf[cHEIMBEW].getText()+"', ");
			sbuf.append("hbvoll='"+(jcb[cVOLLHB].isSelected() ? "T" : "F")+"', ");
			sbuf.append("anzahlkm='"+(jtf[cANZKM].getText().trim().equals("") ? "0.00" : jtf[cANZKM].getText().trim())+"', ");
			sbuf.append("zzregel='"+SystemPreislisten.hmZuzahlRegeln.get(aktuelleDisziplin).get(Integer.parseInt(jtf[cPREISGR].getText())-1 )+"', ");
			sbuf.append("icd10='"+jtf[cICD10].getText().trim().replace(" ", "")+"', ");
			sbuf.append("icd10_2='"+jtf[cICD10_2].getText().trim().replace(" ", "")+"', ");
			
			String ik = (String) this.jcmbIK.getSelectedItem();
			ik = ik.split(" - ")[0];
			sbuf.append("ik='"+ik+"', ");
			sbuf.append("pauschale = 'T' ");
			sbuf.append(" where id='"+this.vec.get(35)+"' LIMIT 1");

			SqlInfo.sqlAusfuehren(sbuf.toString());
			((JXDialog)this.getParent().getParent().getParent().getParent().getParent()).setVisible(false);
			aufraeumen();
			((JXDialog)this.getParent().getParent().getParent().getParent().getParent()).dispose();
			//System.out.println("Rezept wurde mit Preisgruppe "+jtf[cPREISGR].getText()+" gespeichert");
			setCursor(Cursors.cdefault);
			//System.out.println(sbuf.toString());
		}catch(Exception ex){
			ex.printStackTrace();
			setCursor(Cursors.cdefault);
			JOptionPane.showMessageDialog(null, "Fehler beim Abspeichern dieses Rezeptes.\n"+
					"Bitte notieren Sie den Namen des Patienten und die Rezeptnummer\n"+
					"und informieren Sie umgehend den Administrator");
		}
		
	}
	/**************************************/
	/**************************************/
	public static String[] holeLFV(String hole_feld, String db,String where_feld, String suchen,String voart){
		String cmd = "select "+hole_feld+" from "+db+" where "+where_feld+"='"+suchen+"' LIMIT 1";
		String anamnese = SqlInfo.holeEinzelFeld(cmd);
		String[] retstring = {"",""};
		if(anamnese.indexOf("$$LFV$$"+voart.toUpperCase()+"$$") >= 0){
			String[] zeilen = anamnese.split("\n");
			for(int i = 0; i < zeilen.length;i++){
				if(zeilen[i].startsWith("$$LFV$$"+voart.toUpperCase()+"$$")){
					String[] woerter = zeilen[i].split(Pattern.quote("$$"));
					try{
						retstring[1] = "LangfristVerordnung: "+woerter[1]+"\n"+
								"Disziplin: "+woerter[2]+"\n"+
								"Aktenzeichen: "+woerter[3]+"\n"+
								"Genehmigungsdatum: "+woerter[4]+"\n"+
								"Gültig ab: "+woerter[5]+"\n"+
								"Gültig bis: "+woerter[6]+"\n";
						retstring[0] = String.valueOf(zeilen[i]);
					}catch(Exception ex){
						ex.printStackTrace();
					}
					return retstring;
				}
			}
			
		}
		return retstring;
	}
	/**************************************/
	/**************************************/
	/**************************************/
	private void doSpeichernNeu(){
		try{
			int reznr = -1;
			if(!komplettTest()){
				////System.out.println("Komplett-Test fehlgeschlagen");
				return;
			}
			long dattest = DatFunk.TageDifferenz(DatFunk.sHeute(),jtf[cREZDAT].getText().trim() );
			//long min = -364;
			//long max = 364;
			if( (dattest <= -364) || (dattest >= 364) ){
				int frage = JOptionPane.showConfirmDialog(null, "<html><b>Das Rezeptdatum ist etwas kritisch....<br><br><font color='#ff0000'> "+
						"Rezeptdatum = "+jtf[cREZDAT].getText().trim()+"</font></b><br>Das sind ab Heute "+Long.toString(dattest)+" Tage<br><br><br>"+
						"Wollen Sie dieses Rezeptdatum tatsächlich abspeichern?", "Bedenkliches Rezeptdatum",JOptionPane.YES_NO_OPTION);
				if(frage!=JOptionPane.NO_OPTION){
					 SwingUtilities.invokeLater(new Runnable(){
					 	   public  void run()
					 	   {
								jtf[cREZDAT].requestFocus();
					 	   }
					});	   		
					return;
				}
				
			}
			if(jcb[this.cLEITSX].isSelected() && leitsymta.getText().trim().equals("")) {
				int frage = JOptionPane.showConfirmDialog(null, "<html><b>Leichte Ungereimtheiten....<br><br> "+
						"Sie haben 'individuelle Leitsmptomatik' angekreuzt.<br><font color='#ff0000'>Im vorgesehenen Textfeld ist aber kein Text angegeben</font></b><br><br>"+
						"Wollen Sie dieses Rezept tatsächlich so abspeichern?", "Eventuell falsche Angaben...",JOptionPane.YES_NO_OPTION);
				if(frage!=JOptionPane.NO_OPTION){
					final int check = this.cLEITSX; 
					 SwingUtilities.invokeLater(new Runnable(){
					 	   public  void run()
					 	   {
								jcb[check].requestFocus();
					 	   }
					});
					return;
				}
			}
			setCursor(Cursors.wartenCursor);
			String stest = "";
			int itest = -1;
			StringBuffer sbuf = new StringBuffer();
			//System.out.println("Nummer = "+nummer);
			reznr = SqlInfo.erzeugeNummer(nummer);
			if(reznr < 0){
				JOptionPane.showMessageDialog(null,"Schwerwiegender Fehler beim Bezug einer neuen Rezeptnummer!");
				setCursor(Cursors.cdefault);
				return;
			}
			int rezidneu = SqlInfo.holeId("verordn", "diagnose");
			sbuf.append("update verordn set rez_nr='"+nummer.toUpperCase()+
					Integer.valueOf(reznr).toString()+"', ");
			sbuf.append("pat_intern='"+jtf[cPATINT].getText()+"', ");
			sbuf.append("patid='"+jtf[cPATID].getText()+"', ");
			sbuf.append("ktraeger='"+jtf[cKTRAEG].getText()+"', ");
			sbuf.append("kid='"+jtf[cKASID].getText()+"', ");
			sbuf.append("arzt='"+jtf[cARZT].getText()+"', ");
			sbuf.append("arztid='"+jtf[cARZTID].getText()+"', ");
			stest = DatFunk.sHeute();
			sbuf.append("datum='"+DatFunk.sDatInSQL(stest)+"', ");
			
			String akutEreignis = jtf[cAKUTDATUM].getText();
			if(akutEreignis.length()==10 && !akutEreignis.equals("  .  .    ")) {
				sbuf.append("veraenderd='"+DatFunk.sDatInSQL(jtf[cAKUTDATUM].getText())+"' ,");
			}
			stest = jtf[cREZDAT].getText().trim();
			if(stest.equals(".  .")){
				stest = DatFunk.sHeute();
			}
			boolean neuerpreis = RezTools.neuePreisNachRezeptdatumOderStichtag(aktuelleDisziplin, preisgruppe, String.valueOf(stest),true,null);
			//Zunächst ermitteln welche Fristen und ob Kalender oder Werktage gelten
			//Dann das Rezeptdatum übergeben, Rückgabewert ist spätester Beginn. 
			sbuf.append("rez_datum='"+DatFunk.sDatInSQL(stest)+"', ");
			String stest2 = jtf[cBEGINDAT].getText().trim();
			if(stest2.equals(".  .")){  //muß noch auf die neuen Regeln 2021 angepaßt werden
				//Preisgruppe holen
				int pg = Integer.parseInt(jtf[cPREISGR].getText())-1;
				//Frist zwischen Rezeptdatum und erster Behandlung holen
				int frist = (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(0)).get(pg);
				//Kalendertage
				if((Boolean) ((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(1)).get(pg)){
					stest2 = DatFunk.sDatPlusTage(stest, frist);					
				}else{ //Werktage
					boolean mitsamstag = (Boolean)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(4)).get(pg);
					stest2 = HMRCheck.hmrLetztesDatum(stest, (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(0)).get(pg),mitsamstag );
				}
			}
			sbuf.append("lastdate='"+DatFunk.sDatInSQL(stest2)+"', ");
			sbuf.append("lasteddate='"+DatFunk.sDatInSQL(DatFunk.sHeute())+"', ");
			sbuf.append("lastedit='"+Reha.aktUser+"', ");
			Integer rezeptart = Integer.valueOf(jcmb[cBEDARF].getSelectedIndex()) + 3 ;
			sbuf.append("rezeptart='"+rezeptart.toString()+"', ");
			sbuf.append("begruendadr='"+(jcb[cBEGRADR].isSelected() ? "T" : "F")+"', ");
			sbuf.append("hausbes='"+(jcb[cHAUSB].isSelected() ? "T" : "F")+"', ");
			sbuf.append("arztbericht='"+(jcb[cTBANGEF].isSelected() ? "T" : "F")+"', ");
			sbuf.append("anzahl1='"+jtf[cANZ1].getText()+"', ");		
			sbuf.append("anzahl2='"+jtf[cANZ2].getText()+"', ");
			sbuf.append("anzahl3='"+jtf[cANZ3].getText()+"', ");
			sbuf.append("anzahl4='"+jtf[cANZ4].getText()+"', ");
			sbuf.append("anzahlhb='"+jtf[cANZ1].getText()+"', ");
			itest = jcmb[cLEIST1].getSelectedIndex();
			if(itest > 0){
				sbuf.append("art_dbeh1='"+preisvec.get(itest-1).get(9)+"', ");
				sbuf.append("preise1='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
				sbuf.append("pos1='"+preisvec.get(itest-1).get(2)+"', ");
				sbuf.append("kuerzel1='"+preisvec.get(itest-1).get(1)+"', ");
			}else{
				sbuf.append("art_dbeh1='0', ");
			}
			itest = jcmb[cLEIST2].getSelectedIndex();
			if(itest > 0){
				sbuf.append("art_dbeh2='"+preisvec.get(itest-1).get(9)+"', ");
				sbuf.append("preise2='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
				sbuf.append("pos2='"+preisvec.get(itest-1).get(2)+"', ");
				sbuf.append("kuerzel2='"+preisvec.get(itest-1).get(1)+"', ");
			}else{
				sbuf.append("art_dbeh2='0', ");
			}
			itest = jcmb[cLEIST3].getSelectedIndex();
			if(itest > 0){
				sbuf.append("art_dbeh3='"+preisvec.get(itest-1).get(9)+"', ");
				sbuf.append("preise3='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
				sbuf.append("pos3='"+preisvec.get(itest-1).get(2)+"', ");
				sbuf.append("kuerzel3='"+preisvec.get(itest-1).get(1)+"', ");
			}else{
				sbuf.append("art_dbeh3='0', ");
			}
			itest = jcmb[cLEIST4].getSelectedIndex();
			if(itest > 0){
				sbuf.append("art_dbeh4='"+preisvec.get(itest-1).get(9)+"', ");
				sbuf.append("preise4='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
				sbuf.append("pos4='"+preisvec.get(itest-1).get(2)+"', ");
				sbuf.append("kuerzel4='"+preisvec.get(itest-1).get(1)+"', ");
			}else{
				sbuf.append("art_dbeh4='0', ");
			}
			sbuf.append("frequenz='"+jtf[cFREQ].getText()+"', ");
			sbuf.append("dauer='"+jtf[cDAUER].getText()+"', ");
			if(jcmb[cINDI].getSelectedIndex() > 0){
				sbuf.append("indikatschl='"+(String)jcmb[cINDI].getSelectedItem()+/**Leitsymt einbauen**/"', ");			
			}else{
				sbuf.append("indikatschl='"+"keine DiagGr."+"', ");			
			}
			sbuf.append("barcodeform='"+Integer.toString(jcmb[cBARCOD].getSelectedIndex())+"', ");
			sbuf.append("angelegtvon='"+jtf[cANGEL].getText()+"', ");
			sbuf.append("preisgruppe='"+jtf[cPREISGR].getText()+"', ");
			if(jcmb[cFARBCOD].getSelectedIndex() > 0){
				sbuf.append("farbcode='"+Integer.toString(14+jcmb[cFARBCOD].getSelectedIndex()-1).toString()+"', ");	
			}else{
				sbuf.append("farbcode='-1', ");
			}
			/*********************2021-Änderungen*************************/
			sbuf.append("leitsyma='"+(jcb[this.cLEITSA].isSelected() ? "T" : "F")+"', ");
			sbuf.append("leitsymb='"+(jcb[this.cLEITSB].isSelected() ? "T" : "F")+"', ");
			sbuf.append("leitsymc='"+(jcb[this.cLEITSC].isSelected() ? "T" : "F")+"', ");
			sbuf.append("leitsymx='"+(jcb[this.cLEITSX].isSelected() ? "T" : "F")+"', ");
			sbuf.append("leitsymtext='"+leitsymta.getText()+"', ");
			sbuf.append("dringlich='"+(jcb[this.cDRINGLICH].isSelected() ? "T" : "F")+"', ");
			sbuf.append("therapziel='"+therapzielta.getText()+"', ");
			sbuf.append("hmr2021='T', ");
			
	/*******************************************/		
			Integer izuzahl = Integer.valueOf(jtf[cPREISGR].getText());
			String unter18 = "F";
			String szzstatus = "";
			for(int i = 0; i < 1;i++){
				//if(SystemConfig.vZuzahlRegeln.get(izuzahl-1) <= 0){
				if(SystemPreislisten.hmZuzahlRegeln.get(aktuelleDisziplin).get(izuzahl-1) <= 0){	
					//System.out.println("1. ZuzahlStatus = Zuzahlung nicht erforderlich");
					szzstatus = "0";
					break;
				}
				if(nummer.equalsIgnoreCase("rh")){
					szzstatus = "0";
					break;
				}
				if(nummer.equalsIgnoreCase("rs") || nummer.equalsIgnoreCase("ft")){
					szzstatus = "0";
					break;
				}				
				////System.out.println("ZuzahlStatus = Zuzahlung (zunï¿½chst) erforderlich, prï¿½fe ob befreit oder unter 18");
				if(Reha.instance.patpanel.patDaten.get(30).equals("T")){
					//System.out.println("2. ZuzahlStatus = Patient ist befreit");
					szzstatus = "0";				
					break;
				}
				if(DatFunk.Unter18(DatFunk.sHeute(), DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)))){
					////System.out.println("ZuzahlStatus = Patient ist unter 18 also befreit...");
					String gebtag = DatFunk.sHeute().substring(0,6)+Integer.valueOf(Integer.valueOf(SystemConfig.aktJahr)-18).toString();
					long tage = DatFunk.TageDifferenz(DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)) ,gebtag);
					//System.out.println("Differenz in Tagen = "+tage);
					//System.out.println("Geburtstag = "+gebtag);
					if(tage < 0 && tage >= -45){
						JOptionPane.showMessageDialog(null ,"Achtung es sind noch "+(tage*-1)+" Tage bis zur Volljährigkeit\n"+
								"Unter Umständen wechselt der Zuzahlungsstatus im Verlauf dieses Rezeptes");
						szzstatus = "3";
					}else{
						szzstatus = "0";
					}
					unter18 = "T";
					break;
				}
				////System.out.println("Normale Zuzahlung -> status noch nicht bezahlt");
				szzstatus = "2";				
			}
			String[] lzv= holeLFV("anamnese", "pat5", "pat_intern", jtf[cPATINT].getText(), nummer.toUpperCase().substring(0,2));
			if(!  lzv[0].equals("") ){
				if(!jta.getText().contains(lzv[0]) ){
					int frage = JOptionPane.showConfirmDialog(null, "Für den Patient ist eine Langfristverordnung eingetragen.\n\n"+lzv[1]+
							"\n\nWollen Sie diesen Eintrag dieser Verordnung zuweisen?",
							"Achtung wichtige Benutzeranfrage",JOptionPane.YES_NO_OPTION);
					if(frage==JOptionPane.YES_OPTION){
						jta.setText(jta.getText()+"\n"+lzv[0]);
					}
				}
			}
			sbuf.append("zzstatus='"+szzstatus+"', ");
			sbuf.append("diagnose='"+StringTools.Escaped(jta.getText())+"', ");
			sbuf.append("unter18='"+unter18+"', ");
			sbuf.append("jahrfrei='"+Reha.instance.patpanel.patDaten.get(69)+"', ");
			sbuf.append("heimbewohn='"+jtf[cHEIMBEW].getText()+"', ");
			sbuf.append("hbvoll='"+(jcb[cVOLLHB].isSelected() ? "T" : "F")+"', ");
			sbuf.append("anzahlkm='"+(jtf[cANZKM].getText().trim().equals("") ? "0.00" : jtf[cANZKM].getText().trim())+"', ");		
			sbuf.append("befr='"+Reha.instance.patpanel.patDaten.get(30)+"', ");
			sbuf.append("zzregel='"+SystemPreislisten.hmZuzahlRegeln.get(aktuelleDisziplin).get(Integer.valueOf(jtf[cPREISGR].getText())-1 )+"',");
			sbuf.append("icd10='"+jtf[cICD10].getText().trim().replace(" ", "")+"', ");
			sbuf.append("icd10_2='"+jtf[cICD10_2].getText().trim().replace(" ", "")+"', ");
			
			String ik = (String) this.jcmbIK.getSelectedItem();
			ik = ik.split(" - ")[0];
			sbuf.append("ik='"+ik+"', ");
			sbuf.append("pauschale = 'T' ");
			sbuf.append("where id='"+Integer.toString(rezidneu)+"'  LIMIT 1");
			SqlInfo.sqlAusfuehren(sbuf.toString());
			//System.out.println("Rezept wurde mit Preisgruppe "+jtf[cPREISGR].getText()+" gespeichert");
			Reha.instance.patpanel.aktRezept.setzeRezeptNummerNeu(nummer.toUpperCase()+Integer.toString(reznr));
			//Reha.instance.patpanel.aktRezept.holeRezepte(jtf[cPATINT].getText(),nummer.toUpperCase()+Integer.toString(reznr));
			((JXDialog)this.getParent().getParent().getParent().getParent().getParent()).setVisible(false);
			aufraeumen();
			((JXDialog)this.getParent().getParent().getParent().getParent().getParent()).dispose();
			setCursor(Cursors.cdefault);
		}catch(Exception ex){
			ex.printStackTrace();
			setCursor(Cursors.cdefault);
			JOptionPane.showMessageDialog(null, "Fehler beim Abspeichern dieses Rezeptes.\n"+
					"Bitte notieren Sie den Namen des Patienten und die Rezeptnummer\n"+
					"und informieren Sie umgehend den Administrator\n"+makeStacktraceToString(ex));
		}
		
	}
	public static String makeStacktraceToString(Exception ex){
		String string = "";
		try{
			StackTraceElement[] se = ex.getStackTrace();
			for(int i = 0; i < se.length; i++){
				string = string+se[i].toString()+"\n";
			}
		}catch(Exception ex2){
			
		}
		return string;
	}
	
	// Lemmi 20110101: Kopieren des letzten Rezepts des selben Patienten bei Rezept-Neuanlage
	@SuppressWarnings("unused")
	private void doKopiereLetztesRezeptDesPatienten() {
		/** KONZEPT
		 holle alle Rezepte aus den Tabellen "verordn" und "lza" zum aktuellen Patienten sortiert und finde das neueste als erstes in der Liste
		 falls es Rezepte zu mehreren Disziplinen gibt, müßte man hier noch interaktiv abfragen, welches gemeint sein soll (nicht eingebaut)
		 dann hole die Daten aus dem alten Rezept in einen Vektor analog vekcaktrez und schiebe sie in das Rezept via ladeZusatzDatenAlt()
		 lösche alle Felder aus dem Vektor, die im akt. rezept gar nicht sein können (zB cREZDAT)
		 dann setzte nochmal neue Daten drüber ladeZusatzDatenNeu()
		 **/

		// Lemmi 20110106: Lieber Hr. Steinhilber: Diese Funktion an andere Stelle verlegt, weil Architekturänderung
		//if(rezToCopy == null){return;}
		
		// Definition der Inices für den Vektor "vecaktrez"  
		// Lemmi Todo: DAS MUSS VOLLSTÄNDIG GEMACHT UND AN ZENTRALE STELLE VERSCHOBEN WERDEN !!!
		final int cVAR_PATID = 0;
		final int cVAR_REZNR = 1;
		final int cVAR_REZDATUM = 2;
		final int cVAR_ANZAHL1 = 3;
		final int cVAR_ANZAHL2 = 4;
		final int cVAR_ANZAHL3 = 5;
		final int cVAR_ANZAHL4 = 6;
		final int cVAR_ANZAHLKM = 7;
		final int cVAR_ARTDBEH1 = 8;  // Art der Behandlung
		final int cVAR_ARTDBEH2 = 9;
		final int cVAR_ARTDBEH3 = 10;
		final int cVAR_ARTDBEH4 = 11;
		final int cVAR_BEFREIT = 12;  // BEFR
		final int cVAR_REZGEBUEHR = 13;
		final int cVAR_BEZAHLT = 14;  // REZ_BEZ
		final int cVAR_ARZTNAM = 15;
		final int cVAR_ARZTID = 16;
		final int cVAR_AERZTE = 17;
		final int cVAR_PREIS1 = 18;
		final int cVAR_PREIS2 = 19;
		final int cVAR_PREIS3 = 20;
		final int cVAR_PREIS4 = 21;
		final int cVAR_DATANGEL = 22;
		final int cVAR_DIAGNOSE = 23;
		
		final int cVAR_TERMINE = 34;
		
		final int cVAR_ZZSTAT = 39;
		final int cVAR_LASTDAT = 40;  // spätester Behandlungsbginn
		final int cVAR_PREISGR = 41;
		final int cVAR_BEGRUENDADR = 42;
		final int cVAR_HAUSBES = 43;
		final int cVAR_INDI = 44;
		final int cVAR_ANGEL = 45;
		final int cVAR_BARCOD = 46;  // BARCODEFORM
		final int cVAR_DAUER = 47;
		final int cVAR_POS1 = 48;
		final int cVAR_POS2 = 49;
		final int cVAR_POS3 = 50;
		final int cVAR_POS4 = 51;
		final int cVAR_FREQ = 52;
		final int cVAR_LASTEDIT = 53;
		final int cVAR_BERID = 54;
		final int cVAR_ARZTBER = 55;
		final int cVAR_LASTEDDATE = 56;
		final int cVAR_FARBCOD = 57;
		final int cVAR_RSPLIT = 58;
		final int cVAR_JAHRFREI = 59;

		final int cVAR_HBVOLL = 61;
		
		final int cVAR_ICD10 = 71;
		final int cVAR_ICD10_2 = 72;
		//Funktion ist immer noch suboptimal, da der Kostenträger des Rezeptes noch nicht übernommen wird. 
		
		//String strPat_Intern = jtf[cPATINT].getText();

		// Lemmi 20110106: Lieber Hr. Steinhilber: Diese Funktion an andere Stelle verlegt, weil Architekturänderung
		//vec = ((Vector<String>)SqlInfo.holeSatz( "verordn", " * ", "REZ_NR = '"+rezToCopy+"'", Arrays.asList(new String[] {}) ));


		// für die Rückmeldung zum Setezen der Dailogüberschrift
		strKopiervorlage = "";
		
		if ( vec.size() > 0 ) {   // nur wenn etwas gefunden werden konnte !

			// Titel des Dialogs individualisieren für die Rückmeldung zum Setezen der Dailogüberschrift
			strKopiervorlage = vec.get(cVAR_REZNR);

			// Lemmi 20110106: Lieber Hr. Steinhilber: Das fkt. nicht, weil jcmb nicht den hier angebenen Inhalt besitzt !
///			jcmb[cRKLASSE].setSelectedIndex( Arrays.asList(new String[] {"KG","MA","ER","LO","RH","PO"}).indexOf(rezToCopy.substring(0,2))  );
			jcmb[cRKLASSE].setSelectedIndex( Arrays.asList(strRezepklassenAktiv).indexOf(strKopiervorlage.substring(0,2)) );
			
	
			// Löschen der auf jeden Fall "falsch weil alt" Komponenten
			vec.set(cVAR_REZNR, "");
			vec.set(cVAR_REZDATUM, "");
			vec.set(cVAR_TERMINE, "");
			vec.set(cVAR_ZZSTAT, "");
			vec.set(cVAR_LASTDAT, "");
			
			if(SystemConfig.AngelegtVonUser){
				vec.set(cVAR_ANGEL, Reha.aktUser);
			}else{
				vec.set(cVAR_ANGEL, "");				
			}
			vec.set(cVAR_LASTEDIT, "");
			
			vec.set(cVAR_BEFREIT, "");

			vec.set(cVAR_BEZAHLT, "F" );    // Das kann noch nicht bezahlt sein (Rezeptgebühr)
			
			jtf[cKTRAEG].setText(vec.get(36)); //ktraeger
			jtf[cKASID].setText(vec.get(37)); //kassenid
			preisgruppe = Integer.parseInt(vec.get(41));
			
			ladeZusatzDatenAlt();  // Eintragen von vec in die Dialog-Felder
			
			ladeZusatzDatenNeu();  // Hier nochmals die neuen Daten ermitteln - schließlich haben wir ein neues Rezept !

			//jtf[cKTRAEG].setText(vec.get(36)); //ktraeger
			//jtf[cKASID].setText(vec.get(37)); //kassenid
			jtf[cARZT].setText(vec.get(15)); //arzt
			jtf[cARZTID].setText(vec.get(16)); //arztid
			
			jtf[cICD10].setText(vec.get(71)); //icd10
			jtf[cICD10_2].setText(vec.get(72)); //icd10_2
			
			//preisgruppe = Integer.parseInt(vec.get(41));
			
			// Lemmi 20110106: Lieber Hr. Steinhilber: Das fkt. nicht, weil hier nicht alle Disziplinen aktiv sein müssen !
			//erneuter Aufruf damit die korrekte Preisgruppe genommen wird GKV vs. BGE etc.
///			jcmb[cRKLASSE].setSelectedIndex( Arrays.asList(new String[] {"KG","MA","ER","LO","RH","PO"}).indexOf(rezToCopy.substring(0,2))  );
			// Lemmi 20110116: die ganze Zeile ist überflüssig, weil das vorab alles schon korrekt gesetzt worden ist !
			//                 wenn man die Zeile benutzt wird zudem der gewählte INDI-Schlüssel wieder gelöscht !
//			jcmb[cRKLASSE].setSelectedIndex( Arrays.asList(strRezepklassenAktiv).indexOf(strKopiervorlage.substring(0,2)) );
			
			
			/* Lemmi 20110116: Lieber Hr. Steinhilber: Die Leistungen werden bereits durch den obigen Aufruf von "ladeZusatzDatenAlt()" gesetzt.
			 * Lemmi Frage: warum sollte das hier nochmals gemacht werden?
			if(!vec.get(cVAR_ARTDBEH1).equals("0")){
				jcmb[cLEIST1].setSelectedVecIndex(9, vec.get(cVAR_ARTDBEH1));//art_dbeh1	
			}
			if(!vec.get(cVAR_ARTDBEH2).equals("0")){
				jcmb[cLEIST2].setSelectedVecIndex(9, vec.get(cVAR_ARTDBEH2));//art_dbeh2	
			}
			if(!vec.get(cVAR_ARTDBEH3).equals("0")){
				jcmb[cLEIST3].setSelectedVecIndex(9, vec.get(cVAR_ARTDBEH3));//art_dbeh3	
			}
			if(!vec.get(cVAR_ARTDBEH4).equals("0")){
				jcmb[cLEIST4].setSelectedVecIndex(9, vec.get(cVAR_ARTDBEH4));//art_dbeh4	
			}
			*/
			
			// vec wieder löschen - er hat seinen Transport-Dienst für das Kopieren geleistet
			vec.clear();
		}
	}  // end of doKopiereLetztesRezeptDesPatienten()
	
	private void doAbbrechen(){
		// Lemmi 20101231: Verhinderung von Datenverlust bei unbeabsichtigtem Zumachen des geänderten Rezept-Dialoges
		//Solche gravierenden Änderungen der Programmlogik dürfen erst dann eingeführt werden
		//wenn sich der Benutzer auf einer System-Init-Seite entscheiden kann ob er diese 
		//Funktionalität will oder nicht
		//Wir im RTA wollen die Abfagerei definitiv nicht!
		//Wenn meine Damen einen Vorgang abbrechen wollen, dann wollen sie den Vorgang abbrechen
		//und nicht gefrag werden ob sie den Vorgang abbrechen wollen.
		//Steinhilber
		// Lemmi 20110116: Gerne auch mit Steuer-Parameter
		if( (Boolean)SystemConfig.hmRezeptDlgIni.get("RezAendAbbruchWarn")) {
			if ( HasChanged() && askForCancelUsaved() == JOptionPane.NO_OPTION )
				return;
		}
			
		aufraeumen();
		((JXDialog)this.getParent().getParent().getParent().getParent().getParent()).dispose();		
	}

	@Override
	public void rehaTPEventOccurred(RehaTPEvent evt) {
		try{
			if(evt.getDetails()[0] != null){
				if(evt.getDetails()[0].equals(this.getName())){
					this.setVisible(false);
					rtp.removeRehaTPEventListener((RehaTPEventListener) this);
					rtp = null;
					aufraeumen();
				}
			}
		}catch(NullPointerException ne){
			JOptionPane.showMessageDialog(null, "Fehler beim abhängen des Listeners Rezept-Neuanlage\n"+
					"Bitte informieren Sie den Administrator über diese Fehlermeldung");
		}
	}	
	public void aufraeumen(){
		new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				for(int i = 0; i < jtf.length;i++){
					ListenerTools.removeListeners(jtf[i]);
				}
				for(int i = 0; i < jcb.length;i++){
					ListenerTools.removeListeners(jcb[i]);
				}
				for(int i = 0; i < jcmb.length;i++){
					ListenerTools.removeListeners(jcmb[i]);
				}
				ListenerTools.removeListeners(jta);
				ListenerTools.removeListeners(getInstance());
				if(rtp != null){
					rtp.removeRehaTPEventListener((RehaTPEventListener) getInstance());
					rtp = null;
				}
				return null;
			}
		}.execute();
	}
	
	private class JRtaTextArea extends JTextArea{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		KeyListener kl;
		JComponent back;
		JComponent forward;
		JRtaTextArea(JComponent backComponent,JComponent forwardComponent ){
			
			super();
			
			back = backComponent;
			forward = forwardComponent;
					
			kl = new KeyListener() {
				@Override
				public void keyPressed(KeyEvent e) {
					if( (e.getKeyCode()==KeyEvent.VK_ENTER || e.getKeyCode()==KeyEvent.VK_TAB || e.getKeyCode()==KeyEvent.VK_DOWN)    
							&& getInstance().getText().length() <= 2) {
						e.consume();
						((JComponent)forward).requestFocus();
						return;
					}else if( (e.getKeyCode()==KeyEvent.VK_ENTER || e.getKeyCode()==KeyEvent.VK_TAB || e.getKeyCode()==KeyEvent.VK_DOWN)
						&& e.isControlDown()) {
						e.consume();
						((JComponent)forward).requestFocus();
						return;
					}else if( (e.getKeyCode()==KeyEvent.VK_ENTER || e.getKeyCode()==KeyEvent.VK_TAB || e.getKeyCode()==KeyEvent.VK_UP)
							&& e.isShiftDown()) {
						e.consume();
						((JComponent)back).requestFocus();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
					// TODO Auto-generated method stub
				}
				@Override
				public void keyTyped(KeyEvent e) {
					// TODO Auto-generated method stub
				}
				
			};
			
			addKeyListener(kl);
		}
		
		public JRtaTextArea getInstance() {
			return this;
		}
		
	}
}
