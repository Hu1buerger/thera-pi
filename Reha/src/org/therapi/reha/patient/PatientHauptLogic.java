package org.therapi.reha.patient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXPanel;

import CommonTools.DatFunk;
import CommonTools.INIFile;
import CommonTools.INITool;
import CommonTools.JRtaTextField;
import CommonTools.SqlInfo;
import CommonTools.StringTools;
import dialoge.PinPanel;
import dialoge.RehaSmartDialog;
import environment.Path;
import events.PatStammEvent;
import events.RehaTPEvent;
import events.RehaTPEventClass;
import hauptFenster.Reha;
import hauptFenster.SuchenDialog;
import krankenKasse.KassenFormulare;
import oOorgTools.OOTools;
import patientenFenster.PatNeuanlage;
import rechteTools.Rechte;
import socketClients.SMSClient;
import stammDatenTools.ArztTools;
import stammDatenTools.KasseTools;
import stammDatenTools.PatTools;
import stammDatenTools.ZuzahlTools;
import systemEinstellungen.SystemConfig;

public class PatientHauptLogic {

	public PatientHauptPanel patientHauptPanel = null;
	private String lastseek = "";
	public boolean neuDlgOffen = false;
	private Vector<String> titel = new Vector<String>();
	private Vector<String> formular = new Vector<String>();
	private int iformular;
	private JRtaTextField formularid = new JRtaTextField("NIX",false);
	private PatNeuDlg neuPat;
	public PatNeuanlage pneu;
	public int lastrow = -1;
	List<String> teles = Arrays.asList("TELEFONP","TELEFONG","TELEFONM");
	List<String> vecpos = Arrays.asList("18","19","20");
	String sdialnr = "";
	String xfeldname = "";
	int idialnr = -1;
	public PatientHauptLogic(PatientHauptPanel patHauptPanel){

		this.patientHauptPanel = patHauptPanel;
		new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				try{
					ZuzahlTools.setZzIcons(); // soll peu-a-peu 'die da' ersetzen:
					patientHauptPanel.imgzuzahl[0] = SystemConfig.hmSysIcons.get("zuzahlfrei");
					patientHauptPanel.imgzuzahl[1] = SystemConfig.hmSysIcons.get("zuzahlok");
					patientHauptPanel.imgzuzahl[2] = SystemConfig.hmSysIcons.get("zuzahlnichtok");
					patientHauptPanel.imgzuzahl[3] = SystemConfig.hmSysIcons.get("kleinehilfe");

					//imgrezstatus[0] = SystemConfig.hmSysIcons.get("statusoffen");
					patientHauptPanel.imgrezstatus[0] = null;
					patientHauptPanel.imgrezstatus[1] = SystemConfig.hmSysIcons.get("statuszu");

				}catch(Exception ex){
					ex.printStackTrace();
				}
				return null;
			}
		}.execute();
		SwingUtilities.invokeLater(new Runnable(){
			@Override
            public synchronized void run(){
				KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_MASK);
				patientHauptPanel.getInstance().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "doSuchen");
				patientHauptPanel.getActionMap().put("doSuchen", new PatientAction());
				stroke = KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK);
				patientHauptPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "doNeu");
				patientHauptPanel.getActionMap().put("doNeu", new PatientAction());
				stroke = KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.ALT_MASK);
				patientHauptPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "doEdit");
				patientHauptPanel.getActionMap().put("doEdit", new PatientAction());
				stroke = KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.ALT_MASK);
				patientHauptPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "doDelete");
				patientHauptPanel.getActionMap().put("doDelete", new PatientAction());
				stroke = KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.ALT_MASK);
				patientHauptPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, "doFormulare");
				patientHauptPanel.getActionMap().put("doFormulare", new PatientAction());
				holeFormulare();
				if(Reha.instance.terminpanel != null){
			    	//TerminFenster.thisClass.setUpdateVerbot(true);
			    }

	   	  	}
		});

	}
	public void fireAufraeumen(){
		formularid.listenerLoeschen();
		formularid = null;
		patientHauptPanel = null;
	}

	/***
	 * SuchenDialog aufrufen
	 */
	public void starteSuche(){
		String sstmt = "", cmd = "", strAnzahl = "", strStamm = "";
		Statement stmt = null;
		ResultSet rs = null;

		if(patientHauptPanel.tfsuchen.getText().trim().equals("")){  // Wenn keine Suchvorgaben eingetippt worden sind:

			// Lemmi 20101212: Anzahl der zu erwartenden Datensätze eingebaut
			int suchart = patientHauptPanel.jcom.getSelectedIndex();
			//if(suchart==6){
			if(suchart==patientHauptPanel.patToolBarPanel.getAktRezIdx()){
				/* - lädt eh ALLE Patienten mit akt. Rezept -> brauch 'mer also auch nich meckern...
				//sstmt = "SELECT COUNT(*) from verordn";			// err: listet Anz. der akt. Rezepte
				sstmt = "SELECT COUNT(*) FROM (SELECT * FROM verordn GROUP BY pat_intern) AS t1";	// ok: Anz. der Pat. mit akt. Rezept
				strStamm = "aktiven Rezeptstamm";
				*/
			}else{
				sstmt = "SELECT COUNT(*) from pat5";
				strStamm = "Patientenstamm";

				// Lemmi 20101212: Anzahl der zu erwartenden Datensätze ermitteln via DB-Abfrage
				try {
					stmt =  Reha.instance.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
		                    ResultSet.CONCUR_UPDATABLE );
					try{
						rs = stmt.executeQuery(sstmt);
						rs.absolute(1); // moves the cursor to the 1st row of rs
						strAnzahl = rs.getString(1);
					}catch(SQLException ev){
					}
				}catch(SQLException ex) {
				}

				finally {
					if (rs != null) {
						try {
							rs.close();
						} catch (SQLException sqlEx) { // ignore }
							rs = null;
						}
					}
					if (stmt != null) {
						try {
							stmt.close();
						} catch (SQLException sqlEx) { // ignore }
							stmt = null;
						}
					}
				}

				// Lemmi 20101212: Erstellung der Meldung mit Anzahl der Datensätze
				cmd = "<html>Sie haben <b>kein</b> Suchkriterium eingegeben.<br>"+
				"Das bedeutet Sie laden den <b>kompletten " + strStamm + "!!!</b><br><br>"+
				"Es sind <b>" + strAnzahl + " </b>Datensätze zu erwarten.<br>"+
				"<b>Wollen Sie das wirklich?</b>";

				// Abfrage-Dialog anzeigen und Ergebnis der User-Eingabe abfragen
				int anfrage = JOptionPane.showConfirmDialog(null, cmd,"Achtung wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
				if(anfrage == JOptionPane.NO_OPTION){
					return;
				}
			}
		}
		if (patientHauptPanel.sucheComponent != null){
			Point thispoint = patientHauptPanel.tfsuchen.getLocationOnScreen();
			((SuchenDialog) patientHauptPanel.sucheComponent).setLocation(thispoint.x-60, thispoint.y+25);

			if(! patientHauptPanel.tfsuchen.getText().trim().equals(lastseek.trim())){
				((SuchenDialog) patientHauptPanel.sucheComponent).suchDasDing(patientHauptPanel.tfsuchen.getText());
				lastseek = patientHauptPanel.tfsuchen.getText().trim();
				((SuchenDialog) patientHauptPanel.sucheComponent).setVisible(true);
				this.lastrow = -1;
			}else{
				lastrow = -1;
				((SuchenDialog) patientHauptPanel.sucheComponent).setVisible(true);
			}

		}else{
			//System.out.println("Lastseek = "+lastseek.trim()+" / aktuelle Suche = "+patientHauptPanel.tfsuchen.getText().trim());
			if(! patientHauptPanel.tfsuchen.getText().trim().equals(lastseek.trim())){
				this.setLastRow(-1);
			}
			patientHauptPanel.sucheComponent = new SuchenDialog(null,Reha.instance.patpanel,
					patientHauptPanel.tfsuchen.getText(),patientHauptPanel.jcom.getSelectedIndex(),this);
			Point thispoint = patientHauptPanel.tfsuchen.getLocationOnScreen();
			((SuchenDialog) patientHauptPanel.sucheComponent).setLocation(thispoint.x-60, thispoint.y+25);
			((SuchenDialog) patientHauptPanel.sucheComponent).setVisible(true);
			lastseek = patientHauptPanel.tfsuchen.getText().trim();
		}
	}
	public void setLastRow(int row){
		this.lastrow = row;
	}
	public int getLastRow(){
		return this.lastrow;
	}

	class PatientAction extends AbstractAction {
	    /**
		 *
		 */
		private static final long serialVersionUID = 6195808468235028392L;

		@Override
        public void actionPerformed(ActionEvent e) {
	        ////System.out.println("Patient Action = "+e.getActionCommand());
	        ////System.out.println(e);
	        if(e.getActionCommand().equals("f")){
	        	 Reha.instance.patpanel.tfsuchen.requestFocus();
	        }
	        if(e.getActionCommand().equals("n")){
	        	patNeu();
	        }
	        if(e.getActionCommand().equals("e")){
	        	patEdit();

	        }
	        if(e.getActionCommand().equals("l")){
	       		patDelete();
	        }
	        if(e.getActionCommand().equals("b")){
	        	patStarteFormulare();
	        }
	        if(e.getActionCommand().equals("waehlen")){
	        	dialOut(sdialnr);
	        }
	        if(e.getActionCommand().equals("korrigieren")){
	        	neuanlagePatient(false, xfeldname);
	        }
	    }
	}

	public void patNeu(){
		if(!Rechte.hatRecht(Rechte.Patient_anlegen, true)){
			return;
		}
		neuanlagePatient(true,"");
	}
	public void patEdit(){
		if(Rechte.hatRecht(Rechte.Patient_editteil, false) ||
				Rechte.hatRecht(Rechte.Patient_editvoll, false)){
			if(!patientHauptPanel.aktPatID.equals("")){
				neuanlagePatient(false,"");
			}else{
				JOptionPane.showMessageDialog(null, "Welchen Patient bitteschön wollen Sie editieren?");
				setzeFocus();
				return;
			}
		}else{
			Rechte.hatRecht(Rechte.Patient_editvoll, true);
		}
	}
	public void editFeld(String feldname) {
		//List<String> teles = Arrays.asList("TELEFONP","TELEFONG","TELEFONM");
		//List<String> vecpos = Arrays.asList("18","19","20");
		//System.out.println(feldname);
		this.xfeldname="";
		this.idialnr = -1;
		this.sdialnr = "";
		if(Rechte.hatRecht(Rechte.Patient_editteil, false) ||
				Rechte.hatRecht(Rechte.Patient_editvoll, false)){
			if(! patientHauptPanel.aktPatID.equals("")){
				//hier die Dialexistenz
				if(SystemConfig.activateSMS && (teles.contains(feldname) )){
					//hier die Dialberechtigung
					if(SystemConfig.hmSMS.get("DIAL").equals("1")){
						//nur wenn berechtigt Popup zeigen
						//Hierher Popupaufruf
						this.idialnr = Integer.parseInt(vecpos.get(teles.indexOf(feldname)));
						if(! (this.sdialnr=patientHauptPanel.patDaten.get(this.idialnr)).equals("")){
							this.xfeldname = feldname;
							new SwingWorker<Void,Void>(){
								@Override
								protected Void doInBackground()
										throws Exception {
									ZeigePopupMenu(sdialnr,MouseInfo.getPointerInfo().getLocation());
									return null;
								}

							}.execute();
						}else{
							neuanlagePatient(false, feldname);
						}
					}else{
						neuanlagePatient(false, feldname);
					}
				}else{
					neuanlagePatient(false, feldname);
				}

			}
		}else{
			Rechte.hatRecht(Rechte.Patient_editvoll, true);
		}
	}

	public void dialOut(String number){
		String b = ":++:";
		final StringBuffer buf = new StringBuffer();
		Reha.lastCommId = Long.toString(System.currentTimeMillis());
		Reha.lastCommAction = "DIAL-OUT:TO:"+number+":";
		buf.append(Reha.lastCommId);
		buf.append(b);
		buf.append(SystemConfig.dieseMaschine);
		buf.append(b);
		buf.append("DIAL-OUT");
		buf.append(b);
		buf.append("---");
		buf.append(b);
		buf.append(number);
		new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				try{
					new SMSClient().setzeNachricht(buf.toString());
				}catch(Exception ex){
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null,"<html><b>Kontakt mit "+SystemConfig.hmSMS.get("NAME")+" konnte nicht aufgebaut werden</b></html>");
				}
				return null;
			}
		}.execute();
	}

	private void ZeigePopupMenu(String nummer,Point p){
		JPopupMenu jPop = getPhonePopupMenu(nummer);
		jPop.show( Reha.getThisFrame(), (int)p.getX()+25, (int)p.getY() );
	}

	private JPopupMenu getPhonePopupMenu(String nummer){
		JPopupMenu jPopupMenu = new JPopupMenu();

		JMenuItem item = new JMenuItem("Telefonat -> "+nummer+" <- starten");
		item.setActionCommand("waehlen");
		item.addActionListener(new PatientAction());
		jPopupMenu.add(item);
		jPopupMenu.addSeparator();

		item = new JMenuItem("Patientenstamm editieren");
		item.setActionCommand("korrigieren");
		item.addActionListener(new PatientAction());
		jPopupMenu.add(item);

		return jPopupMenu;
	}
	public void patDelete(){
		if(! Rechte.hatRecht(Rechte.Patient_delete, true)){
			return;
		}
		if(!patientHauptPanel.aktPatID.equals("")){
			//String spat = patientHauptPanel.ptfield[2].getText().trim()+", "+patientHauptPanel.ptfield[3].getText().trim()+", geb.am "+patientHauptPanel.ptfield[4].getText().trim();
			//String spat = "Wollen Sie den aktuellen Patient wirklich löschen?";
	    	int frage = JOptionPane.showConfirmDialog(null, "Wollen Sie den aktuellen Patient wirklich löschen??", "Achtung wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
	    	if(frage != JOptionPane.YES_OPTION){
	    		return;
	    	}
	    	final String spatid = String.valueOf(patientHauptPanel.aktPatID);
	    	new Thread(){
	    		@Override
                public void run(){
	    			String stmt = "delete from pat5 where pat_intern='"+spatid+"'";
	    			SqlInfo.sqlAusfuehren(stmt);
	    			stmt = "delete from patbild where pat_intern='"+spatid+"' LIMIT 1";
	    			SqlInfo.sqlAusfuehren(stmt);
	    			stmt = "delete from verordn where pat_intern='"+spatid+"'";
	    			SqlInfo.sqlAusfuehren(stmt);
	    			stmt = "delete from lza where pat_intern='"+spatid+"'";
	    			SqlInfo.sqlAusfuehren(stmt);
	    			stmt = "delete from doku1 where pat_intern='"+spatid+"'";
	    			SqlInfo.sqlAusfuehren(stmt);
	    			stmt = "delete from bericht1 where pat_intern='"+spatid+"'";
	    			SqlInfo.sqlAusfuehren(stmt);
	    			stmt = "delete from bericht2 where pat_intern='"+spatid+"'";
	    			SqlInfo.sqlAusfuehren(stmt);
	    			stmt = "delete from bericht2ktl where pat_intern='"+spatid+"'";
	    			SqlInfo.sqlAusfuehren(stmt);
	    			stmt = "delete from berhist where pat_intern='"+spatid+"'";
	    			SqlInfo.sqlAusfuehren(stmt);
	    		}
	    	}.start();
			allesAufNull();
			setzeFocus();
		}else{
			JOptionPane.showMessageDialog(null, "Welchen Patient bitteschön wollen Sie löschen?");
			setzeFocus();
			return;
		}
	}
	private void allesAufNull(){
		/******************************************************************************/
		// erst die sichtbaren Edits löschen
		/*
		for(int i = 0; i <15;i++){
			patientHauptPanel.ptfield[i].setText("");
		}
		*/
		// aktPatID zurücksetzten dann ist in weiteres löschen nicht mehr möglich
		patientHauptPanel.aktPatID = "";
		patientHauptPanel.autoPatid = -1;
		// jetzt das RezeptPanel KeinRezept anhängen
		patientHauptPanel.getStammDaten().htmlPane.setText("");
		patientHauptPanel.aktRezept.setzeRezeptPanelAufNull(true);
		patientHauptPanel.historie.setzeHistoriePanelAufNull(true);
		patientHauptPanel.berichte.setzeBerichtPanelAufNull(true);
		patientHauptPanel.dokumentation.setzeDokuPanelAufNull(true);
		patientHauptPanel.gutachten.setzeGutachtenPanelAufNull(true);
		// dann die Icons löschen
		patientHauptPanel.pmemo[0].setText("");
		patientHauptPanel.pmemo[1].setText("");
	}
	public void patStarteFormulare(){
			new SwingWorker<Void,Void>(){
				@Override
				protected Void doInBackground() throws Exception {
					iformular = -1;
					KassenFormulare kf = new KassenFormulare(Reha.getThisFrame(),titel,formularid);
					Point pt = patientHauptPanel.jbut[3].getLocationOnScreen();
					kf.setLocation(pt.x-100,pt.y+25);
					kf.setModal(true);
					final KassenFormulare xkf = kf;
					SwingUtilities.invokeLater(new Runnable(){
						@Override
						public void run() {
							xkf.aufErsteElement();
						}

					});
					kf.setVisible(true);

					new SwingWorker<Void,Void>(){
						@Override
						protected Void doInBackground() throws Exception {
							if(! formularid.getText().equals("") ){
								iformular = Integer.valueOf(formularid.getText());
								if(iformular >=0){
									String sdatei = formular.get(iformular);
									OOTools.starteStandardFormular(Path.Instance.getProghome()+"vorlagen/"+Reha.getAktIK()+"/"+sdatei,null);
								}
							}
							return null;
						}
					}.execute();
					kf=null;
					return null;
				}

			}.execute();
		}


	public void holeFormulare(){
		new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				INIFile inif = INITool.openIni(Path.Instance.getProghome()+"ini/"+Reha.getAktIK()+"/", "patient.ini");
				int forms = inif.getIntegerProperty("Formulare", "PatientFormulareAnzahl");
				for(int i = 1; i <= forms; i++){
					titel.add(inif.getStringProperty("Formulare","PFormularText"+i));
					formular.add(inif.getStringProperty("Formulare","PFormularName"+i));
				}
				return null;
			}
		}.execute();
	}


	public void neuanlagePatient(boolean lneu,String feldname){
		if(neuDlgOffen){
			return;
		}
		neuDlgOffen = true;
		neuPat = new PatNeuDlg();
		PinPanel pinPanel = new PinPanel();
		pinPanel.setName("PatientenNeuanlage");
		pinPanel.getGruen().setVisible(false);
		neuPat.setPinPanel(pinPanel);
		if(lneu){
			neuPat.getSmartTitledPanel().setTitle("Patient Neuanlage");
		}else{
			neuPat.getSmartTitledPanel().setTitle("Patient ändern");
			//neuPat.getSmartTitledPanel().setTitle("editieren ---> "+ptfield[2].getText().trim()+", "+ptfield[3].getText().trim()+", geboren am: "+ptfield[4].getText().trim());
		}
		neuPat.setSize(960,600);
		neuPat.setPreferredSize(new Dimension(960,600));
		neuPat.getSmartTitledPanel().setPreferredSize(new Dimension (960,600));
		neuPat.setPinPanel(pinPanel);
		pneu = new PatNeuanlage(null,lneu,feldname);
		neuPat.getSmartTitledPanel().setContentContainer(pneu);
		neuPat.getSmartTitledPanel().getContentContainer().setName("PatientenNeuanlage");
	    neuPat.setName("PatientenNeuanlage");
		neuPat.setLocationRelativeTo(null);
		neuPat.setTitle("Patienten Neuanlage");
		neuPat.pack();
		neuPat.setModal(true);
		//neuPat.setAlwaysOnTop(true);
		neuPat.setVisible(true);
		//neuPat.dispose();
		neuPat = null;
		pneu = null;
	    pinPanel = null;
		//System.out.println("Pat Neu/ändern ist disposed");
		neuDlgOffen = false;


	}
	class PatNeuDlg extends RehaSmartDialog{
		/**
		 *
		 */
		private static final long serialVersionUID = -7706110755275876905L;
		private RehaTPEventClass rtp = null;
		public PatNeuDlg(){
			super(null,"PatientenNeuanlage");
			this.setName("PatientenNeuanlage");
			rtp = new RehaTPEventClass();
			rtp.addRehaTPEventListener(this);
		}
		@Override
        public void rehaTPEventOccurred(RehaTPEvent evt) {
			try{
				if(evt.getDetails()[0] != null){
					if(evt.getDetails()[0].equals(this.getName())){
						this.setVisible(false);
						rtp.removeRehaTPEventListener(this);
						rtp = null;
						this.dispose();
						super.dispose();
						//System.out.println("****************Patient Neu/ändern -> Listener entfernt**************");
					}
				}
			}catch(NullPointerException ne){
				//System.out.println("In PatNeuanlage" +evt);
			}
		}
		@Override
        public void windowClosed(WindowEvent arg0) {
			if(rtp != null){
				this.setVisible(false);
				rtp.removeRehaTPEventListener(this);
				rtp = null;
				dispose();
				super.dispose();
				//System.out.println("****************Patient Neu/ändern -> Listener entfernt (Closed)**********");
			}
		}


	}

	class PatFotoDlg extends RehaSmartDialog{
		/**
		 *
		 */
		private static final long serialVersionUID = -7706110755275876905L;
		private RehaTPEventClass rtp = null;
		public PatFotoDlg(){
			super(null,"PatientenFoto");
			this.setName("PatientenFoto");
			rtp = new RehaTPEventClass();
			rtp.addRehaTPEventListener(this);
		}
		@Override
        public void rehaTPEventOccurred(RehaTPEvent evt) {
			try{
				if(evt.getDetails()[0] != null){
					if(evt.getDetails()[0].equals(this.getName())){
						this.setVisible(false);
						rtp.removeRehaTPEventListener(this);
						rtp = null;
						this.dispose();
						super.dispose();
					}
				}
			}catch(NullPointerException ne){

			}
		}
		@Override
        public void windowClosed(WindowEvent arg0) {
			if(rtp != null){
				this.setVisible(false);
				rtp.removeRehaTPEventListener(this);
				rtp = null;
				dispose();
				super.dispose();
			}
		}
	}
	public void doPatFoto(){
		BufferedImage buf = PatNeuanlage.holePatBild(Reha.instance.patpanel.aktPatID);
		if(buf==null){
			JOptionPane.showMessageDialog(patientHauptPanel.getToolBar(), "Kein Patientenfoto verfügbar!");
			return;
		}
		PatFotoDlg foto = new PatFotoDlg();
		PinPanel pinPanel = new PinPanel();
		pinPanel.setName("PatientenFoto");
		pinPanel.getGruen().setVisible(false);
		foto.setPinPanel(pinPanel);
		foto.setPinPanel(pinPanel);
		JXPanel pan = new JXPanel(new BorderLayout());

		JLabel lab = new JLabel();
		lab.setIcon(new ImageIcon(buf));
		pan.add(lab);
		pan.setPreferredSize(new Dimension(buf.getWidth(),buf.getHeight()));
		foto.getSmartTitledPanel().setContentContainer(pan);
		foto.getSmartTitledPanel().getContentContainer().setName("PatientenFoto");
		foto.getSmartTitledPanel().setTitle("Patienten-Foto");
		foto.setName("PatientenFoto");
		foto.setLocation(patientHauptPanel.getStammDaten().getLocationOnScreen().x+10,
				patientHauptPanel.getStammDaten().getLocationOnScreen().y+25);
		foto.pack();
		foto.setModal(true);
		//neuPat.setAlwaysOnTop(true);
		foto.setVisible(true);
		buf = null;
		foto = null;


	}


	public void arztListeSpeichernVector(Vector<?> vec, boolean inNeu, String xpatintern){
		String aliste = "";
		for(int i = 0;i < vec.size();i++){
			aliste = aliste+"@"+((String)((Vector<?>)vec.get(i)).get(5))+"@\n";
		}
		String sets = "aerzte='"+aliste+"'";
		SqlInfo.aktualisiereSaetze("pat5",sets , "pat_intern='"+xpatintern+"'");
		//System.out.println("Sets = "+sets +" pat_Intern = "+xpatintern);
		if(Reha.instance.patpanel.aktPatID.equals(xpatintern)){
			//System.out.println("Länge des patDaten.Arrays = "+Reha.instance.patpanel.patDaten.size());
			Reha.instance.patpanel.patDaten.set(63,aliste);
		}
	}
	public void arztListeSpeichernString(String aliste,boolean inNeu,String xpatintern){
		String sets = "aerzte='"+aliste+"'";
		SqlInfo.aktualisiereSaetze("pat5",sets , "pat_intern='"+xpatintern+"'");
		//System.out.println("Sets = "+sets +" pat_Intern = "+xpatintern);
		if(Reha.instance.patpanel.aktPatID.equals(xpatintern)){
			Reha.instance.patpanel.patDaten.set(63,aliste);
		}
	}


	public void patStammEventOccurred(PatStammEvent evt) {
		////System.out.println("Event im Neuen PatStamm = "+evt);
		////System.out.println("Detail 0 = "+evt.getDetails()[0]);
		////System.out.println("Detail 1 = "+evt.getDetails()[1]);
		if(evt.getDetails()[0].equals("#PATSUCHEN")){
			final String xpatint = evt.getDetails()[1].trim();
			final String xrez = evt.getDetails()[2].trim();
			patientHauptPanel.aktPatID = xpatint;
			// Anzeigedaten holen
			//System.out.println("In Thera-Pi PatID="+xpatint+" Rezeptnummer="+xrez);
			new Thread(){
				@Override
                public void run(){
					new SwingWorker<Void,Void>(){
						@Override
						protected Void doInBackground() throws Exception {
							try{
								datenHolen(xpatint);
							}catch(Exception ex){
								ex.printStackTrace();
							}

							SwingUtilities.invokeLater(new Runnable(){
							 	   @Override
                                public  void run()
							 	   {
							 		   try{
								 		   String titel = "Patient: "+Reha.instance.patpanel.patDaten.get(2)+", "+
						 					Reha.instance.patpanel.patDaten.get(3)+" geboren am: "+
						 					DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4))+" - "+
						 					"Patienten-ID: "+Reha.instance.patpanel.patDaten.get(29);
								 		   	Reha.instance.patpanel.patientInternal.setzeTitel(titel);
								 		   	macheAlleHashMaps();
							 		   }catch(Exception ex){
							 			   ex.printStackTrace();
							 			   JOptionPane.showMessageDialog(null, "Fehler bei der Aufbereitung der Adressenplatzhalter");
							 		   }
							 	   }
							});
							return null;
						}
					}.execute();
				}
			}.start();
			// kmplette Patdaten holen
			//new Thread(){
				//public void run(){
				//}
			//}.start();
			// Rezeptdaten holen
			new Thread(){
				@Override
                public void run(){
					new SwingWorker<Void,Void>(){
						@Override
						protected Void doInBackground() throws Exception {
							long zeit = System.currentTimeMillis();
							String rezNb = xrez;
							while(! Reha.instance.patpanel.patDatenOk){
								Thread.sleep(20);
								if(System.currentTimeMillis()-zeit > 10000){
									JOptionPane.showMessageDialog(null, "Fehler beim Bezug der Rezeptdaten");
									return null;
								}
							}
							if(xrez.contains("#REZHOLEN-")){
								patientHauptPanel.aktRezept.suchePatUeberRez = true;
								rezNb = xrez.split("#REZHOLEN-")[1].trim();
							}
							patientHauptPanel.aktRezept.holeRezepte(xpatint,rezNb);
							return null;
						}
					}.execute();
				}
			}.start();
			// Historie holen
			new Thread(){
				@Override
                public void run(){
					new SwingWorker<Void,Void>(){
						@Override
						protected Void doInBackground() throws Exception {
							String rezNb = xrez;
							if(xrez.contains("#REZHOLEN-")){
								rezNb = xrez.split("#REZHOLEN-")[1].trim();
							}
							try{
								patientHauptPanel.historie.holeRezepte(xpatint,rezNb);
							}catch(Exception ex){
								//ex.printStackTrace();
							}
							return null;
						}
					}.execute();
				}
			}.start();
			// Berichte holen
			new Thread(){
				@Override
                public void run(){
					new SwingWorker<Void,Void>(){
						@Override
						protected Void doInBackground() throws Exception {
							try{
								patientHauptPanel.berichte.holeBerichte(xpatint,"");
							}catch(Exception ex){
								//ex.printStackTrace();
							}
							return null;
						}
					}.execute();
				}
			}.start();
			// Dokumentation holen
			new Thread(){
				@Override
                public void run(){
					new SwingWorker<Void,Void>(){
						@Override
						protected Void doInBackground() throws Exception {
							try{
								patientHauptPanel.dokumentation.holeDokus(xpatint,"");
							}catch(Exception ex){
								//ex.printStackTrace();
							}
							return null;
						}
					}.execute();
				}
			}.start();
			// Gutachten (E-Berichte) holen
			new Thread(){
				@Override
                public void run(){
					new SwingWorker<Void,Void>(){
						@Override
						protected Void doInBackground() throws Exception {
							try{
								patientHauptPanel.gutachten.holeGutachten(xpatint,"");
							}catch(Exception ex){
								//ex.printStackTrace();
							}
							return null;
						}
					}.execute();
				}
			}.start();
			/*
			//auf offene RGR-Prüfen
			new Thread(){
				public void run(){
					new SwingWorker<Void,Void>(){
						@Override
						protected Void doInBackground() throws Exception {
							try{
								patientHauptPanel.holeWichtigeInfos(xpatint,"");
							}catch(Exception ex){
								ex.printStackTrace();
							}
							return null;
						}
					}.execute();
				}
			}.start();
			*/
			/*
			int i = patientHauptPanel.multiTab.getTabCount();
			for(int y = 0;y < i;y++){
				////System.out.println("Tabtitel von "+y+" = "+jtab.getTitleAt(y));
			}
			*/
		}
		if(evt.getDetails()[0].equals("#SUCHENBEENDEN")){
			if(((SuchenDialog) patientHauptPanel.sucheComponent) != null){
				((SuchenDialog) patientHauptPanel.sucheComponent).dispose();
				patientHauptPanel.sucheComponent = null;
				new SwingWorker<Void,Void>(){
					@Override
					protected Void doInBackground() throws Exception {
						Runtime r = Runtime.getRuntime();
					    r.gc();
						return null;
					}

				}.execute();
			}
		}
		if(evt.getDetails()[0].equals("#PATEDIT")){
			//neuanlagePatient(false,"");
		}
		if(evt.getDetails()[0].equals("#CLOSING")){
			if(patientHauptPanel.sucheComponent != null){
				//patientHauptPanel.ptp.removePatStammEventListener((PatStammEventListener) this);
				((SuchenDialog) patientHauptPanel.sucheComponent).dispose();
			}
		}
		if(evt.getDetails()[0].equals("#FOCUSIEREN")){
			setzeFocus();
			patientHauptPanel.tfsuchen.requestFocus();
		}
		/*
		if(evt.getDetails()[0].equals("#KORRIGIEREN")){
			if(this.neuDlgOffen){return;}
			final String feld = (String)evt.getDetails()[1];
			new SwingWorker<Void,Void>(){
				@Override
				protected Void doInBackground() throws Exception {
					////System.out.println("Korrigieren->"+evx.getSource());
					editFeld(feld);
					return null;
				}
			}.execute();
		}*/

	}
	public void setzeFocus(){
		SwingUtilities.invokeLater(new Runnable(){
		 	   @Override
            public  void run(){
			 		  if(! patientHauptPanel.tfsuchen.hasFocus()){
			 			  if(! patientHauptPanel.patientInternal.getActive()){
			 				 patientHauptPanel.patientInternal.activateInternal();
			 				  SwingUtilities.invokeLater(new Runnable(){
			 					  @Override
                                public  void run(){
			 						 patientHauptPanel.tfsuchen.requestFocus();
			 					  }
			 				  });
		 				  }else{
		 					 SwingUtilities.invokeLater(new Runnable(){
								  @Override
                                public  void run(){
									  patientHauptPanel.tfsuchen.requestFocus();
								  }
		 					 });
		 				  }
					  }else{
						  SwingUtilities.invokeLater(new Runnable(){
							  @Override
                            public  void run(){
								  patientHauptPanel.tfsuchen.requestFocus();
							  }
						  });
					  }
			 	   }
		});
	}

	public void datenHolen(String patint){
		try{
			Reha.instance.patpanel.patDatenOk = false;
			Reha.instance.patpanel.patDaten = SqlInfo.holeSatz("pat5"," * ", "PAT_INTERN ='"+patint+"'", Arrays.asList(new String[] {}) );
//			String stmt = "select id from rgaffaktura where roffen > '0' and pat_intern = '"+patint+"' LIMIT 1";
			String stmt = "select id from rgaffaktura where roffen > '0' and rnr not like 'storno%' and pat_intern = '"+patint+"' LIMIT 1";
			
			if(SqlInfo.holeFelder(stmt).size() > 0){
				Reha.bRGAFoffen = true;
			}else{
				Reha.bRGAFoffen = false;
			}

			//System.out.println("Größe der Daten = "+Reha.instance.patpanel.patDaten.size());
			if(Reha.instance.patpanel.patDaten.size() >= 71){
				if( (Reha.instance.patpanel.patDaten.get(57)+Reha.instance.patpanel.patDaten.get(58)+Reha.instance.patpanel.patDaten.get(59)+
						Reha.instance.patpanel.patDaten.get(60)+Reha.instance.patpanel.patDaten.get(61)+Reha.instance.patpanel.patDaten.get(62)).contains("T")){
					Reha.bHatMerkmale=true;
				}else{
					Reha.bHatMerkmale=false;
				}

				if(Reha.instance.patpanel.patDaten.get(65).equals("")){
					Reha.instance.patpanel.pmemo[0].setText("");
				}else{
					Reha.instance.patpanel.pmemo[0].setText(Reha.instance.patpanel.patDaten.get(65));
				}
				if(Reha.instance.patpanel.patDaten.get(64).equals("")){
					Reha.instance.patpanel.pmemo[1].setText("");
				}else{
					Reha.instance.patpanel.pmemo[1].setText(Reha.instance.patpanel.patDaten.get(64));
				}
				Reha.instance.patpanel.autoPatid = Integer.parseInt(Reha.instance.patpanel.patDaten.get(66));
				Reha.instance.patpanel.aid = StringTools.ZahlTest(Reha.instance.patpanel.patDaten.get(67));
				Reha.instance.patpanel.kid = StringTools.ZahlTest(Reha.instance.patpanel.patDaten.get(68));
				Reha.instance.patpanel.patDatenOk = true;
				Reha.instance.patpanel.getStammDaten().parseHTML(true);

			}else{
				JOptionPane.showMessageDialog(null, "Fehler beim Einlesen der Patientendaten");
				Reha.instance.patpanel.patDatenOk = false;
				Reha.instance.patpanel.getStammDaten().parseHTML(false);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			Reha.instance.patpanel.patDatenOk = true;
		}

	}
	private void macheAlleHashMaps(){
		new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				try{
					PatTools.constructPatHMap();
				}catch(Exception ex){
					JOptionPane.showMessageDialog(null, "Fehler bei PatTools.constructPatHMap()");
				}
				try{
					ArztTools.constructArztHMap("");
				}catch(Exception ex){
					JOptionPane.showMessageDialog(null, "Fehler bei	ArztTools.constructArztHMap('')");
				}
				try{
					KasseTools.constructKasseHMap("");
				}catch(Exception ex){
					JOptionPane.showMessageDialog(null, "Fehler bei	KasseTools.constructKasseHMap('')");
				}
				if(((SuchenDialog) patientHauptPanel.sucheComponent) != null){
					((SuchenDialog) patientHauptPanel.sucheComponent).dispose();
					patientHauptPanel.sucheComponent = null;
				}
				return null;
			}
		}.execute();
	}


}
