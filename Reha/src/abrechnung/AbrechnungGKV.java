package abrechnung;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTree;
import org.thera_pi.nebraska.crypto.NebraskaCryptoException;
import org.thera_pi.nebraska.crypto.NebraskaEncryptor;
import org.thera_pi.nebraska.crypto.NebraskaFileException;
import org.thera_pi.nebraska.crypto.NebraskaKeystore;
import org.thera_pi.nebraska.crypto.NebraskaNotInitializedException;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.mysql.jdbc.PreparedStatement;

import CommonTools.DatFunk;
import CommonTools.JCompTools;
import CommonTools.JRtaCheckBox;
import CommonTools.JRtaComboBox;
import CommonTools.JRtaRadioButton;
import CommonTools.SqlInfo;
import CommonTools.StringTools;
import emailHandling.EmailSendenExtern;
import environment.Path;
import events.PatStammEvent;
import events.PatStammEventClass;
import events.PatStammEventListener;
import gui.Cursors;
import hauptFenster.AktiveFenster;
import hauptFenster.Reha;
import hauptFenster.UIFSplitPane;
import rehaInternalFrame.JAbrechnungInternal;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;

public class AbrechnungGKV extends JXPanel implements PatStammEventListener,ActionListener,TreeSelectionListener, MouseListener{
	/**
	 *
	 */
	private static final long serialVersionUID = -3580427603080353812L;
	private JAbrechnungInternal jry;
	private UIFSplitPane jSplitLR = null;

	final String plus = "+";
	final String EOL = "'"+System.getProperty("line.separator");
	final String SOZ = "?";
	public String abzurechnendeKassenID = "";
	String ik_kasse,ik_kostent,ik_nutzer,ik_physika,ik_papier,ik_email,ik_preisgruppe;
	String name_kostent;
	String aktEsol;
	String aktDfue;
	String aktRechnung;
	String aktDisziplin = "";
//	String[] diszis = {"KG","MA","ER","LO","PO","RS","FT"};		// McM: weg

	boolean annahmeAdresseOk = false;
	/*******Controls für die linke Seite*********/
	ButtonGroup bg = new ButtonGroup();
	JRtaRadioButton[] rbLinks = {null,null,null,null};
	JButton[] butLinks = {null,null,null,null};
	public JRtaComboBox cmbDiszi = null;
	JXTree treeKasse = null;
	File f;
	FileWriter fw;
	BufferedWriter bw;
	AbrechnungDlg abrDlg = null;

//	public DefaultMutableTreeNode rootKasse;
//	public DefaultTreeModel treeModelKasse;

	public JXTTreeNode rootKasse;
	public KassenTreeModel treeModelKasse;
	public JXTTreeNode aktuellerKnoten;
	public JXTTreeNode aktuellerKassenKnoten;
	public int kontrollierteRezepte;

	public StringBuffer positionenBuf = new StringBuffer();
	public StringBuffer unbBuf = new StringBuffer();
	public StringBuffer unzBuf = new StringBuffer();
	public StringBuffer gesamtBuf = new StringBuffer();
	public StringBuffer auftragsBuf = new StringBuffer();
	public StringBuffer buf = new StringBuffer();
	public StringBuffer htmlBuf = new StringBuffer();
	public StringBuffer rechnungBuf = new StringBuffer();
	public StringBuffer historieBuf = new StringBuffer();
	public int positionenAnzahl = 0;
	public String abrDateiName = "";
	JEditorPane htmlPane = null;

	Double[] preis00 = {0.00,0.00,0.00};
	Double[] preis11 = {0.00,0.00,0.00};
	Double[] preis31 = {0.00,0.00,0.00};
	Double[] preis51 = {0.00,0.00,0.00};

	Double[] kassenUmsatz = {0.00,0.00};
	DecimalFormat dfx = new DecimalFormat( "0.00" );

	Vector<String> existiertschon = new Vector<String>();
	Vector<String> customIconList = new Vector<String>();
	int toggleIcons;
	Vector<Vector<String>> kassenIKs = new Vector<Vector<String>>();
	/*******Controls für die rechte Seite*********/
	AbrechnungRezept abrRez = null;
	AbrechnungDrucken abrDruck = null;
	Vector<String> abgerechneteRezepte = new Vector<String>();
	Vector<String> abgerechnetePatienten = new Vector<String>();
	Vector<Vector<String>> preisVector = null;
	HashMap<String,String> hmAnnahme = null;
	HashMap<String,String> hmKostentraeger = new HashMap<String,String>();
	int abrechnungRezepte = 0;
	public String aktuellerPat = "";


	String rlistekasse;
	String rlisteesol;

	JRtaCheckBox soll302 = null;
	HashMap<String,String> hmAlternativeKasse = new HashMap<String,String>();
	JButton alternativeKK = null;

	public String abrechnungsModus = "abrechnung302";
	final String ABR_MODE_302 = "abrechnung302";
	final String ABR_MODE_IV = "abrechnungIV";
	public String SlgaVersion = null;
	public String SllaVersion = null;
	public boolean zuzahlModusDefault = true;

	public static String zertifikatVon = SystemConfig.hmAbrechnung.get("hmkeystoreusecertof");
	public static String originalTitel = "";
	public static boolean lOwnCert = (SystemConfig.hmAbrechnung.get("hmkeystoreusecertof").equals(SystemConfig.hmAbrechnung.get("hmkeystorealias")) ? true : false);
	
	final String sucheFertige = "SELECT t1.name1,t1.ikktraeger,t1.ikkasse,t1.id,t2.ik_papier FROM fertige AS t1 LEFT JOIN kass_adr AS t2 ON t1.ikkasse = t2.ik_kasse ";

	public static boolean directCall = false;

	public Disziplinen disziSelect = null;
	private Connection connection;

	public AbrechnungGKV(JAbrechnungInternal xjry, Connection connection){
		super();
		this.connection = connection;
		this.setJry(xjry);
		setLayout(new BorderLayout());
		if (disziSelect == null) {
			disziSelect = new Disziplinen();
		}
		cmbDiszi = disziSelect.getComboBox();

		jSplitLR =  UIFSplitPane.createStrippedSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        		getLeft(),
        		getRight(connection));
		jSplitLR.setDividerSize(7);
		jSplitLR.setDividerBorderVisible(true);
		jSplitLR.setName("BrowserSplitLinksRechts");
		jSplitLR.setOneTouchExpandable(true);
		jSplitLR.setDividerLocation(230);
		add(jSplitLR,BorderLayout.CENTER);
		mandantenCheck();
		SlgaVersion = "11";//( DatFunk.TageDifferenz("30.09.2013",DatFunk.sHeute()) <= 0 ? "08" : "09");
		SllaVersion = "11";//( DatFunk.TageDifferenz("30.09.2013",DatFunk.sHeute()) <= 0 ? "08" : "09");

		new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				SystemConfig.certState = checkCert(zertifikatVon/*"IK"+Reha.aktIK*/);
				if(SystemConfig.certState > 0){
					abrRez.tbbuts[3].setEnabled(false);
				}
				//System.out.println(SystemConfig.hmAbrechnung);
				//System.out.println("CertState = "+SystemConfig.certState);
				return null;
			}
		}.execute();
		originalTitel = this.jry.getTitel();
		setEncryptTitle();
		//cmbDiszi.setSelectedItem(SystemConfig.initRezeptKlasse);
		disziSelect.setCurrDiszi(SystemConfig.initRezeptKlasse);
		jry.setAbrRezInstance(abrRez);							    // JAbrechnungInternal mitteilen, welche Instanz cleanup() enthält
	}
	public void setEncryptTitle(){
		this.jry.setzeTitel(originalTitel+ " [Abrechnung für IK: "+Reha.getAktIK()+" - Zertifikat von IK: "+zertifikatVon.replace("IK","")+"]");
		this.jry.repaint();
	}
	public static int checkCert(String alias){
		try{
			String keystore = SystemConfig.hmAbrechnung.get("hmkeystorefile");
			NebraskaKeystore store = new NebraskaKeystore(keystore, SystemConfig.hmAbrechnung.get("hmkeystorepw"),"123456", SystemConfig.hmAbrechnung.get("hmkeystoreusecertof").replace("IK", ""));
			Vector<X509Certificate> certs = store.getAllCerts();
			String[] dn = null;
			String ik;
			long tage;
			for(int i = 0; i < certs.size();i++){
				dn=certs.get(i).getSubjectDN().toString().split(",");
				if(dn.length==5){
					ik = dn[3].split("=")[1];
					if(ik.equals(alias)){
						String verfall = certs.get(i).getNotAfter().toLocaleString().split(" ")[0].trim();
						tage = DatFunk.TageDifferenz(DatFunk.sHeute(),verfall);
						if( tage <= 0){
							JOptionPane.showMessageDialog(null,"Ihr Zertifikat ist abgelaufen.\nEine Verschlüsselung mit diesem Zertifikat ist nicht mehr möglich");
							return SystemConfig.certIsExpired;
						}else if(tage <= 30){
							JOptionPane.showMessageDialog(null,"Achtung!!!\nIhr Zertifikat läuft in "+Long.toString(tage)+" Tage(n) ab.\nBitte rechtzeitig neues Zertifikat beantragen");
						}
						return SystemConfig.certOK;
					}else{
						String verfall = certs.get(i).getNotAfter().toLocaleString().split(" ")[0].trim();
						tage = DatFunk.TageDifferenz(DatFunk.sHeute(),verfall);
						if( tage <= 0){
							JOptionPane.showMessageDialog(null,"Mindestens ein Zertifikat im Keystore ist abgelaufen.\nVerschlüsselung und damit die 302-er Abrechnung wird daher gesperrt");
							return SystemConfig.certIsExpired;
						}
					}
				}
			}
		}catch(Exception ex){
			return SystemConfig.certNotFound;
		}
		return SystemConfig.certNotFound;
	}
	/**********
<<<<<<< HEAD
	 *
	 *
	 *
=======
	 * 
>>>>>>> fd1bd68... source cleanup
	 * Linke Seite
	 */
	private void mandantenCheck(){
		if(SystemConfig.hmFirmenDaten.get("Steuernummer").trim().equals("")){
			String msg = "Achtung(!!!!!!) für diesen Mandant wurde keine Steuernummer angegeben!\n\n"+
			"Eine Abrechnung ohne Steuernummer wird von der GKV nicht(!!!!) akzeptiert\n\"+" +
			"Schließen Sie die Abrechnung starten Sie die System-Initialisierung -> Mandanten Datenbank -> Firmenangaben...\n"+
			"und tragen Sie hier im Feld Steuernummer die vom Finanzamt vergebene Steuernummer ein.";
			JOptionPane.showMessageDialog(null,msg);
		}
		if(SystemConfig.hmFirmenDaten.get("Ikbezeichnung").trim().equals("")){
			String msg = "Achtung(!!!!!!) für diesen Mandant wurde keine Firmenbezeichnung angegeben!\n\n"+
			"Schließen Sie die Abrechnung starten Sie die System-Initialisierung -> Mandanten Datenbank -> Firmenangaben...\n"+
			"und tragen Sie hier im Feld 'Mandanten-Bezeichnung' eine Kurzfassung Ihres Firmennamens ein (max 30 Zeichen)";
			JOptionPane.showMessageDialog(null,msg);
		}
		if(SystemConfig.hmFirmenDaten.get("Ikbezeichnung").length() > 30){
			String msg = "Achtung(!!!!!!) für diesen Mandant wurde eine zu lange Firmenbezeichnung angegeben.\n\n"+
			"Schließen Sie die Abrechnung starten Sie die System-Initialisierung -> Mandanten Datenbank -> Firmenangaben...\n"+
			"und tragen Sie hier im Feld 'Mandanten-Bezeichnung' eine Kurzfassung Ihres Firmennamens ein (max 30 Zeichen)";
			JOptionPane.showMessageDialog(null,msg);
		}
	}
	private JScrollPane getLeft(){
		FormLayout lay = new FormLayout("5dlu,fill:0:grow(1.0),5dlu",
				//1   2  3   4  5    6  7    8  9      10             11
				"5dlu,p,5dlu,p,15dlu,p,20dlu,p,15dlu,fill:0:grow(1.0),5dlu");
		PanelBuilder pb = new PanelBuilder(lay);
		CellConstraints cc = new CellConstraints();
		pb.getPanel().setBackground(Color.WHITE);
		//pb.add(getIVPanel(),cc.xy(2,1));
		pb.addLabel("Heilmittel auswählen",cc.xy(2,2));
//		if(SystemConfig.mitRs){
//			cmbDiszi = new JRtaComboBox(new String[] {"Physio-Rezept","Massage/Lymphdrainage-Rezept","Ergotherapie-Rezept","Logopädie-Rezept","Podologie-Rezept","Rehasport-Rezept","Funktionstraining-Rezept"});
//		}else{
//			cmbDiszi = new JRtaComboBox(new String[] {"Physio-Rezept","Massage/Lymphdrainage-Rezept","Ergotherapie-Rezept","Logopädie-Rezept","Podologie-Rezept"});
//		}

		cmbDiszi.setActionCommand("einlesen");
//		cmbDiszi.setSelectedItem(SystemConfig.initRezeptKlasse);
		pb.add(cmbDiszi,cc.xy(2,4));

		rootKasse = new JXTTreeNode(new KnotenObjekt("Abrechnung für Kasse...","",false,"",""),true);
		treeModelKasse = new KassenTreeModel(rootKasse);

		treeKasse = new JXTree(treeModelKasse);
		treeKasse.setModel(treeModelKasse);
		treeKasse.setName("kassentree");
		treeKasse.getSelectionModel().addTreeSelectionListener(this);
		treeKasse.setCellRenderer(new MyRenderer(SystemConfig.hmSysIcons.get("zuzahlok")));
		treeKasse.addMouseListener(this);
		JScrollPane jscrk = JCompTools.getTransparentScrollPane(treeKasse);
		jscrk.validate();
		pb.add(jscrk,cc.xy(2, 6));
		new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				Reha.getThisFrame().setCursor(Cursors.wartenCursor);
				return null;
			}

		}.execute();

		//doEinlesen(null);

		htmlPane = new JEditorPane(/*initialURL*/);
        htmlPane.setContentType("text/html");
        htmlPane.setEditable(false);
        htmlPane.setOpaque(false);
		jscrk = JCompTools.getTransparentScrollPane(htmlPane);
		jscrk.validate();
		pb.add(jscrk,cc.xy(2,10));

		pb.getPanel().validate();

		JScrollPane jscr = JCompTools.getTransparentScrollPane(pb.getPanel());
		jscr.validate();
		cmbDiszi.addActionListener(this);


		//cmbDiszi.setSelectedIndex(cmbDiszi.getSelectedIndex());
		return jscr;
	}
	private JXPanel getRight(Connection connection){
		this.abrRez = new AbrechnungRezept(this, connection);
		this.abrRez.setRechtsAufNull();
		return abrRez;
	}
	public boolean getTageDrucken(){
		return this.abrRez.getTageDrucken();
	}
	@Override
	public void patStammEventOccurred(PatStammEvent evt) {

	}

	public void setJry(JAbrechnungInternal jry) {
		this.jry = jry;
	}

	public JAbrechnungInternal getJry() {
		return jry;
	}
	public JXTTreeNode getaktuellerKassenKnoten(){
		return aktuellerKassenKnoten;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		if(cmd.equals("einlesen")){
			//rootKasse.removeAllChildren();
//			aktDisziplin = getCurrDiszi();
			aktDisziplin = disziSelect.getCurrDiszi();
			//System.out.println("aktDisziplin = "+aktDisziplin);
			if(abrRez.rezeptSichtbar){
				abrRez.setRechtsAufNull();
	    		aktuellerPat = "";
			}
			this.jry.setzeTitel(originalTitel+ " [Abrechnung für IK: "+Reha.getAktIK()+" - Zertifikat von IK: "+zertifikatVon.replace("IK","")+"] [Disziplin: "+aktDisziplin+"]");
			doEinlesen(null,null);
			//setPreisVec(cmbDiszi.getSelectedIndex());
		}
		if(cmd.equals("alternativeadresse")){

		}
	}

	public void einlesenErneuern(String neueReznr){
		directCall = false;
//		aktDisziplin = getCurrDiszi();
		aktDisziplin = disziSelect.getCurrDiszi();
		//abrRez.setKuerzelVec(reznr[cmbDiszi.getSelectedIndex()]);
		if(abrRez.rezeptSichtbar){
			abrRez.setRechtsAufNull();
    		aktuellerPat = "";
		}
		//System.out.println("aktDisziplin");
		//System.out.println(RezTools.putRezNrGetDisziplin(neueReznr));
		if(neueReznr != null){						// Rezept zum Baum hinzufügen
			if( ! aktDisziplin.equals(RezTools.putRezNrGetDisziplin(neueReznr)) ){
				doEinlesen(null,neueReznr);			// andere Disziplin -> Kassenbaum neu aufbauen
			}else{
				directCall = true;					// in Baum der akt. Disziplin einsortieren
				//treeKasse.getSelectionModel().removeTreeSelectionListener(this);
				final int xindex = doEinlesenEinzeln(neueReznr);
				SwingUtilities.invokeLater(new Runnable(){
					@Override
                    public void run(){
						try{
							//System.out.println("rechne Kasse neu");
							treeKasse.clearSelection();
							treeKasse.setSelectionInterval(xindex, xindex);
						}catch(Exception ex){
							ex.printStackTrace();
						}
					}
				});
				//treeKasse.getSelectionModel().addTreeSelectionListener(this);
			}
		}else{										// Rezept aus Baum entfernen
			doEinlesen(null,neueReznr);
		}

	}

	public int doEinlesenEinzeln(String neueReznr){
		//String cmd = "select name1,ikktraeger,ikkasse,id from fertige where rez_nr='"+neueReznr+"' Limit 1";
		// das Gleiche, mit Papierannahmestelle:
		String cmd = sucheFertige + "WHERE rez_nr='"+neueReznr+"' Limit 1";
		Vector <Vector<String>> vecKassen = SqlInfo.holeFelder(cmd);
		treeKasse.setEnabled(true);
		String kas = vecKassen.get(0).get(0).trim().toUpperCase();
		String ktraeger = vecKassen.get(0).get(1).trim();
		String ikkasse = vecKassen.get(0).get(2).trim();
		String ikpapier = vecKassen.get(0).get(4).trim();
		int usesSameIkPapier = 0;
		int aeste = rootKasse.getChildCount();
		int aktuellerAst = 0;
		boolean neuerKnoten = true;
		JXTTreeNode node = null;
		int treeindex = 0;
		for(int i = 0; i < aeste; i++){
			if(((JXTTreeNode)rootKasse.getChildAt(i)).knotenObjekt.ktraeger.equals(ktraeger)){
				neuerKnoten = false;
				node = ((JXTTreeNode)rootKasse.getChildAt(i));
				aktuellerAst = i;
				break;
			} else if(((JXTTreeNode)rootKasse.getChildAt(i)).knotenObjekt.ikpapier.equals(ikpapier)){
				usesSameIkPapier = i; 
			}
		}
		if(neuerKnoten){
			if (usesSameIkPapier != 0){
				node = astEinhaengen(kas,ktraeger,ikkasse,ikpapier,++usesSameIkPapier);
			}else{
				node = astAnhaengen(kas,ktraeger,ikkasse,ikpapier);
			}
		}
		
		cmd = "select rez_nr,pat_intern,ediok,ikkasse from fertige where rez_nr='"+neueReznr+"' Limit 1";
		vecKassen = SqlInfo.holeFelder(cmd);
		JXTTreeNode meinitem = null;
		for(int i = 0;i<vecKassen.size();i++){
			//System.out.println("Durchlaufen: "+i+" von "+vecKassen.size());
			try{
				cmd = "select n_name from pat5 where pat_intern='"+
				vecKassen.get(i).get(1)+"' LIMIT 1";

				String name = SqlInfo.holeFelder(cmd).get(0).get(0);
				cmd = "select preisgruppe from verordn where rez_nr='"+vecKassen.get(i).get(0)+"' LIMIT 1";
				////System.out.println(cmd);
				String preisgr = SqlInfo.holeEinzelFeld(cmd);
				////System.out.println("Preisgruppe="+preisgr);

				KnotenObjekt rezeptknoten = new KnotenObjekt(vecKassen.get(i).get(0)+"-"+name,
						vecKassen.get(i).get(0),
						(vecKassen.get(i).get(2).equals("T")? true : false),
						vecKassen.get(i).get(3),
						preisgr);
				rezeptknoten.ktraeger = ktraeger;
				rezeptknoten.pat_intern = vecKassen.get(i).get(1);
				meinitem = new JXTTreeNode(rezeptknoten,true);

				treeModelKasse.insertNodeInto(meinitem,node,node.getChildCount());
				treeKasse.validate();


				aeste = rootKasse.getChildCount();
				treeKasse.updateUI();
				treeKasse.expandPath(new TreePath(node.getPath() ) );
				treeKasse.scrollPathToVisible(new TreePath(meinitem.getPath() ));
				for(int i2 = 0; i2 < aeste; i2++){
					if(treeKasse.isCollapsed(  new TreePath(((JXTTreeNode)rootKasse.getChildAt(i2)).getPath() ) ) ){
						//System.out.println("geschlossen "+i2+" - "+((JXTTreeNode)rootKasse.getChildAt(i2)).knotenObjekt.titel);
						treeindex += 1;
					}else{
						//System.out.println("expanded "+i2+" - "+((JXTTreeNode)rootKasse.getChildAt(i2)).knotenObjekt.titel);
						treeindex += ((JXTTreeNode)rootKasse.getChildAt(i2)).getChildCount()+1;
					}
					if(((JXTTreeNode)rootKasse.getChildAt(i2)).knotenObjekt.ktraeger.equals(ktraeger)){
						break;
					}
				}

				treeKasse.expandPath(new TreePath(node));
				/*
				System.out.println("****** TreeIndex: "+treeindex);
				System.out.println("Root-Childs "+rootKasse.getChildCount());
				System.out.println("GetIndex "+rootKasse.getIndex(meinitem));
				System.out.println("IndexOfChild = "+treeModelKasse.getIndexOfChild(node,meinitem));
				*/

				treeKasse.setSelectionPath(new TreePath(meinitem));
				treeKasse.setSelectionInterval(treeindex, treeindex);
				if(treeKasse.getSelectionPath() != null){
					if (! SystemConfig.hmAbrechnung.get("autoOk302").equals("0")) {
						//System.out.println("autoOk302: markiere Rezept "+neueReznr+" fertig zur Abrechnung");
						SwingUtilities.invokeLater(new Runnable(){
							@Override
                            public void run(){
								abrRez.actionAbschluss();
							}
						});
					}
				}


			}catch(Exception ex){
				ex.printStackTrace();
			}

		}
		return treeindex;
	}

	/*********
	 * Einlesen der abrechnungsdaten
	 */
	public void doEinlesen(JXTTreeNode aktKassenNode,String neueReznr ){
		directCall = false;
		//final JXTTreeNode xaktKassenNode = aktKassenNode;
		new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				try{
					new SwingWorker<Void,Void>(){
						@Override
						protected Void doInBackground() throws Exception {
							Reha.instance.progressStarten(true);
							return null;
						}
					}.execute();
					existiertschon.clear();
					customIconList.clear();
//					String dsz = diszis[cmbDiszi.getSelectedIndex()];		// McM: kann weg (OK)
					String dsz = disziSelect.getCurrRezClass();

					//String cmd = "select name1,ikktraeger,ikkasse,id from fertige where rezklasse='"+dsz+"' ORDER BY ikktraeger , id";
					// das Gleiche, sortiert nach Papierannahmestellen:
					String cmd = sucheFertige + "WHERE rezklasse='"+dsz+"' GROUP by ikktraeger ORDER BY t2.ik_papier, t1.name1, t1.ikktraeger, t1.id";

					Vector <Vector<String>> vecKassen = SqlInfo.holeFelder(cmd);

					kassenBaumLoeschen();
					if(vecKassen.size() <= 0){
						Reha.instance.progressStarten(false);
						return null;
					}
					treeKasse.setEnabled(true);
					String kas = vecKassen.get(0).get(0).trim().toUpperCase();
					String ktraeger = vecKassen.get(0).get(1).trim();
					String ikkasse = vecKassen.get(0).get(2).trim();
					String ikpapier = vecKassen.get(0).get(4).trim();
					existiertschon.add(ktraeger);
//					//customIconList.add(ktraeger); neee, der erste bleibt 'original'
//					toggleIcons = 0;
//					KeepIkPap myIkPap = new KeepIkPap(ikpapier);

					int aeste = 0;
					KnotenObjekt newNode = astAnhaengen(kas,ktraeger,ikkasse,ikpapier).getObject();
					rezepteAnhaengen(aeste);
					/*
					System.out.println(ktraeger);
					System.out.println(((JXTTreeNode)rootKasse.getChildAt(aeste)).knotenObjekt.titel);
					System.out.println(((JXTTreeNode)rootKasse.getChildAt(aeste)).knotenObjekt.rez_num);
					*/
					aeste++;

					for(int i = 0; i < vecKassen.size();i++){
						ktraeger = vecKassen.get(i).get(1).trim();
						if(! existiertschon.contains(ktraeger)){
							kas = vecKassen.get(i).get(0).trim().toUpperCase();
							//ktraeger = vecKassen.get(i).get(1);
							ikkasse = vecKassen.get(i).get(2);
							ikpapier = vecKassen.get(i).get(4).trim();
							existiertschon.add(ktraeger);
							astAnhaengen(kas,ktraeger,ikkasse,ikpapier);
							rezepteAnhaengen(aeste);
							aeste++;

//							treeKasse.repaint();
						}
					}
					kassenIconsNeuAnzeigen();

					treeKasse.validate();
					treeKasse.setRootVisible(true);

					treeKasse.expandRow(0);

					treeKasse.repaint();
				}catch(Exception ex){
					ex.printStackTrace();
				}
				Reha.getThisFrame().setCursor(Cursors.cdefault);
				Reha.instance.progressStarten(false);
				return null;
			}
		}.execute();
	}
	private void rezepteAnhaengen(int knoten){
		String ktraeger = ((JXTTreeNode)rootKasse.getChildAt(knoten)).knotenObjekt.ktraeger;
//		String dsz = diszis[cmbDiszi.getSelectedIndex()];		// McM: kann weg (OK)
		String dsz = disziSelect.getCurrRezClass();
		String cmd = "select rez_nr,pat_intern,ediok,ikkasse from fertige where rezklasse='"+dsz+"' AND ikktraeger='"+
		ktraeger+"' ORDER BY id,pat_intern";

		Vector <Vector<String>> vecRezepte = SqlInfo.holeFelder(cmd);

		JXTTreeNode node = (JXTTreeNode) rootKasse.getChildAt(knoten);
		//JXTTreeNode treeitem = null;

		JXTTreeNode meinitem = null;
		for(int i = 0;i<vecRezepte.size();i++){
			try{
				cmd = "select n_name from pat5 where pat_intern='"+
				vecRezepte.get(i).get(1)+"' LIMIT 1";

				String name = SqlInfo.holeFelder(cmd).get(0).get(0);
				cmd = "select preisgruppe from verordn where rez_nr='"+vecRezepte.get(i).get(0)+"' LIMIT 1";;
				////System.out.println(cmd);
				String preisgr = SqlInfo.holeEinzelFeld(cmd);
				////System.out.println("Preisgruppe="+preisgr);

				KnotenObjekt rezeptknoten = new KnotenObjekt(vecRezepte.get(i).get(0)+"-"+name,
						vecRezepte.get(i).get(0),
						(vecRezepte.get(i).get(2).equals("T")? true : false),
						vecRezepte.get(i).get(3),
						preisgr);
				rezeptknoten.ktraeger = ktraeger;
				rezeptknoten.pat_intern = vecRezepte.get(i).get(1);
				meinitem = new JXTTreeNode(rezeptknoten,true);

				treeModelKasse.insertNodeInto(meinitem,node,node.getChildCount());
				treeKasse.validate();
			}catch(Exception ex){

			}
		}

	}
	private JXTTreeNode astEinhaengen(String ast,String ktraeger,String ikkasse, String ikpapier, int usesSameIkPap){
		KnotenObjekt knoten = new KnotenObjekt(ast,"",false,"","");
		knoten.ktraeger = ktraeger;
		knoten.ikkasse = ikkasse;
		knoten.setIkPap(ikpapier);
		JXTTreeNode node = new JXTTreeNode(knoten,true);
		treeModelKasse.insertNodeInto(node, rootKasse, usesSameIkPap);
		treeKasse.validate();
		return (node);
	}
	private JXTTreeNode astAnhaengen(String ast,String ktraeger,String ikkasse, String ikpapier){
/*		KnotenObjekt knoten = new KnotenObjekt(ast,"",false,"","");
		knoten.ktraeger = ktraeger;
		knoten.ikkasse = ikkasse;
		knoten.setIkPap(ikpapier);
		JXTTreeNode node = new JXTTreeNode(knoten,true);
		treeModelKasse.insertNodeInto(node, rootKasse, rootKasse.getChildCount());
		treeKasse.validate();
		return (node);
 */
		return (astEinhaengen(ast,ktraeger,ikkasse,ikpapier, rootKasse.getChildCount()));

	}

	private void kassenBaumLoeschen(){
		try{

		while( (rootKasse.getChildCount()) > 0){
			treeModelKasse.removeNodeFromParent((JXTTreeNode) ((JXTTreeNode) treeModelKasse.getRoot()).getChildAt(0));
		}
		treeKasse.validate();
		treeKasse.repaint();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	/*******************************************/
	private void doKassenTreeAuswerten(KnotenObjekt node){
			//Rezept ausgewählt
			setCursor(Cursors.wartenCursor);
			Reha.instance.progressStarten(true);
			try{
				if(! this.abrRez.setNewRez(node.rez_num,node.fertig,aktDisziplin) ){
					Reha.instance.progressStarten(false);
					setCursor(Cursors.normalCursor);
					JOptionPane.showMessageDialog(null,"Rezept konnte nicht ausgewertet werden");
					return;
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
			////System.out.println("Rezept "+node.rez_num+" fertig eingestellt");
			setCursor(Cursors.normalCursor);
			SwingUtilities.invokeLater(new Runnable(){
				@Override
                public void run(){
					Reha.instance.progressStarten(false);
				}
			});


			return;

	}
	/*******************************************/	
	public void loescheKnoten(){
		//rezept aus fertige löschen
		//Verschluß des Rezeptes aufheben
		//Knoten löschen
		//wenn einziger Knoten den kassenKnoten löschen
		String rez_nr = this.aktuellerKnoten.knotenObjekt.rez_num;
		String cmd = "delete from fertige where rez_nr='"+rez_nr+"' LIMIT 1";
		SqlInfo.sqlAusfuehren(cmd);
		cmd = "update verordn set abschluss='F' where rez_nr='"+rez_nr+"' LIMIT 1";
		SqlInfo.sqlAusfuehren(cmd);
		try{
			treeModelKasse.removeNodeFromParent(this.aktuellerKnoten);
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null, "Kritische Situation bei Aktion aufschließen des Rezeptes");
		}
		this.aktuellerKassenKnoten.getNextNode();
//		int anzahlrez = this.aktuellerKassenKnoten.getChildCount();
		if(!removeKassenNode(this.aktuellerKassenKnoten)){
			this.rechneKasse(this.aktuellerKassenKnoten);
		}
/*		
		else{
			treeModelKasse.removeNodeFromParent(this.aktuellerKassenKnoten);
		}
 */
		this.abrRez.setRechtsAufNull();
	}
	private boolean removeKassenNode(JXTTreeNode aktKassNode) {
		if (aktKassNode.getChildCount() > 0) {
			return Boolean.FALSE;
		}
		JXTTreeNode nodeWithSameIK = sameIkPap(aktKassNode);
		JXTTreeNode prevKNode = getPrevKassenKnoten(aktKassNode);
		JXTTreeNode nextKNode = getNextKassenKnoten(aktKassNode);
		treeModelKasse.removeNodeFromParent(aktKassNode);
		if(nodeWithSameIK != null) {										// Nachfolger o. Vorgänger hat gleiches IKpapier -> icons können bleiben
			aktKassNode = nodeWithSameIK;									// ... wird aktueller Knoten
			int aktNodeIdx = 1+ treeModelKasse.getIndexOfChild(rootKasse, aktKassNode);
			//treeKasse.setSelectionInterval(aktNodeIdx, aktNodeIdx);		// KEINEN neuen akt. Knoten selektieren (Anzeige des gerade aufgeschlossenen Rezeptes im Patientenfenster)
			//treeKasse.validate();
			treeKasse.repaint();											// Anzeige aktualisieren
		} else if (!prevKNode.equals(null) && !nextKNode.equals(null)) {
			//doEinlesen(null,null);											// Vorgänger u. Nachfolger haben gleiche Farbe -> Tree neu aufbauen
			kassenIconsNeuAnzeigen();										// neu anzeigen - ist schneller als neu aufbauen
		}
		return Boolean.TRUE;
	}
	private JXTTreeNode sameIkPap (JXTTreeNode aktNode){
		/* Testkram ...
		JXTTreeNode prevKassenNode, nextKassenNode;
		String tmpK = aktNode.getObject().titel;	//ok
		String tmpIK = ((KnotenObjekt)aktNode.getUserObject()).ikkasse;	// auch
		System.out.println("Akt Node = "+tmpK+" "+tmpIK);
		prevKassenNode = (JXTTreeNode) aktNode.getPreviousSibling();
		if(prevKassenNode != null) {
			tmpK = (prevKassenNode.getObject().titel);
			tmpIK = ((KnotenObjekt)prevKassenNode.getUserObject()).ikkasse;
			System.out.println("Prev Node = "+tmpK+" "+tmpIK);			
		} else {
			System.out.println("Prev Node = empty ");						
		}
		nextKassenNode =(JXTTreeNode)aktNode.getNextSibling(); 
		if(nextKassenNode != null) {
			tmpK = nextKassenNode.getObject().titel;
			tmpIK = ((KnotenObjekt)nextKassenNode.getUserObject()).ikkasse;
			System.out.println("Next Node = "+tmpK+" "+tmpIK);
		} else {
			System.out.println("Next Node = empty ");						
		}
		// ... Ende Testkram */

		JXTTreeNode prevKNode = getPrevKassenKnoten(aktNode);
		JXTTreeNode nextKNode = getNextKassenKnoten(aktNode);
		if (nextKNode != null) {
			if (nextKNode.knotenObjekt.getIkPap().equals(aktNode.knotenObjekt.getIkPap())) {	// nächster Knoten hat gleiches IKpapier
				return nextKNode;
			}
		}
		if (prevKNode != null) {
			if (prevKNode.knotenObjekt.getIkPap().equals(aktNode.knotenObjekt.getIkPap())) {	// Vorläuferknoten hat gleiches IKpapier
				return prevKNode;
			}
		}
		return null;
	}
	private JXTTreeNode getPrevKassenKnoten (JXTTreeNode aktNode){
		return (JXTTreeNode) aktNode.getPreviousSibling();
	}
	private JXTTreeNode getNextKassenKnoten (JXTTreeNode aktNode){
		return (JXTTreeNode) aktNode.getNextSibling();
	}
	private void kassenIconsNeuAnzeigen(){
		JXTTreeNode rootNode = (JXTTreeNode) treeModelKasse.getRoot();
		JXTTreeNode aktKasse = (JXTTreeNode) rootNode.getChildAt(0);
		KnotenObjekt knAktKasse = aktKasse.getObject();
		
		if (customIconList.contains(knAktKasse.ktraeger)){	
			toggleIcons = 1;
		}else{
			toggleIcons = 0;
		}
		customIconList.clear();
		
		KeepIkPap myIkPap = new KeepIkPap(knAktKasse.ikpapier);
		while (aktKasse != null) {
			knAktKasse = aktKasse.getObject();
			if (myIkPap.newIkPap(knAktKasse.ikpapier)){
				// Hintergrund- oder Icon-Farbe ändern
				toggleIcons = (++toggleIcons)&1;
				// System.out.println("Wechsel IK-Papier: "+knAktKasse.ikpapier+" Hintergrund oder Icon-Farbe ändern @: "+ knAktKasse.titel);
			};
			if (toggleIcons == 1 ){
				customIconList.add(knAktKasse.ktraeger);								
			}
			aktKasse = (JXTTreeNode) aktKasse.getNextSibling();
		}

		treeKasse.validate();
		treeKasse.repaint();
	}
	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		//TreePath path = arg0.getNewLeadSelectionPath();
    	TreePath tp =  treeKasse.getSelectionPath();
		kontrollierteRezepte = 0;
    	if(tp==null){

    		return;
    	}
    	JXTTreeNode node = (JXTTreeNode) tp.getLastPathComponent();
    	String rez_nr = node.knotenObjekt.rez_num;
    	if(!rez_nr.trim().equals("")){
    		aktuellerKnoten = node;
    		doKassenTreeAuswerten(node.knotenObjekt);
    		aktuellerPat = node.knotenObjekt.pat_intern;
    		aktuellerKassenKnoten =(JXTTreeNode) aktuellerKnoten.getParent();
    		int pgr = -1;
    		if(! node.knotenObjekt.preisgruppe.trim().equals("")){
    			////System.out.println("Aktuelle Disziplin = "+getDiszis()+" / Aktuelle Preisgruppe = "+pgr);
    			pgr = Integer.parseInt(node.knotenObjekt.preisgruppe.trim());
    			zuzahlModusDefault = (SystemPreislisten.hmZuzahlModus.get(disziSelect.getCurrDiszi()).get(pgr-1)==1 ? true : false);
    		}
    		if(pgr < 0){
    			JOptionPane.showMessageDialog(null,"Achtung Preisgruppe kann nicht ermittelt werden!\nBitte dieses Rezept nicht abrechnen!");
    		}
    		////System.out.println("Preisguppe = "+Integer.toString(pgr)+"\nZuhahlmodus = "+(zuzahlModusDefault ? "Normal" : "Bayrisch"));

    	}else{
    		abrRez.setRechtsAufNull();
    		aktuellerKnoten = node;
    		if(aktuellerKnoten.getParent() != null){
    			if(((JXTTreeNode)aktuellerKnoten.getParent()).isRoot()){
    	    		aktuellerKassenKnoten =(aktuellerKnoten);
    	    		//////System.out.println("Aktueller Knoten ist Root");
    			}else{
    				//////System.out.println("Aktueller Knoten ungleich Root");
    			}
        		//////System.out.println("Pfad zu Parent = "+new TreePath(aktuellerKnoten.getParent()).toString());
    		}else{
    			aktuellerKassenKnoten = null;
    		}
    		aktuellerPat = "";

    	}
		kassenUmsatz[0] = 0.00;
		kassenUmsatz[1] = 0.00;
    	if(aktuellerKassenKnoten != null){
    		rechneKasse(aktuellerKassenKnoten);
    	}

	}
	public void setKassenUmsatzNeu(){
		kassenUmsatz[0] = 0.00;
		kassenUmsatz[1] = 0.00;
    	if(aktuellerKassenKnoten != null){
    		rechneKasse(aktuellerKassenKnoten);
    	}
	}
	public String getAbrechnungKasse(){
		////System.out.println(((JXTTreeNode)aktuellerKnoten).knotenObjekt.ktraeger);
		return aktuellerKnoten.knotenObjekt.ktraeger;
	}
	public void rechneKasse(JXTTreeNode aktKasse){
		kontrollierteRezepte = 0;
			final JXTTreeNode xaktKasse = aktKasse;
        	new SwingWorker<Void,Void>(){
				@Override
				protected Void doInBackground() throws Exception {
					try{
						int lang = xaktKasse.getChildCount();
						Reha.instance.progressStarten(true);
						getInstance().setCursor(Cursors.wartenCursor);
						for(int i = 0; i < lang;i++){
							if( ((JXTTreeNode)xaktKasse.getChildAt(i)).knotenObjekt.fertig ){
								kontrollierteRezepte++;
								holeUmsaetze(((JXTTreeNode)xaktKasse.getChildAt(i)).knotenObjekt.rez_num);
							}
						}
						setHtmlLinksUnten(lang,kontrollierteRezepte);
						Reha.instance.progressStarten(false);
						getInstance().setCursor(Cursors.cdefault);
					}catch(Exception ex){
						ex.printStackTrace();
					}
					return null;
				}
        	}.execute();
	}
	public void setHtmlLinksUnten(int gesamt,int gut){
		htmlBuf.setLength(0);
		htmlBuf.trimToSize();
		htmlBuf.append("<html><head>");
		htmlBuf.append("<STYLE TYPE=\"text/css\">");
		htmlBuf.append("<!--");
		htmlBuf.append("A{text-decoration:none;background-color:transparent;border:none}");
		htmlBuf.append("TD{font-family: Tahoma; font-size: 11pt; padding-left:5px;padding-right:30px}");
		htmlBuf.append(".spalte1{color:#0000FF;}");
		htmlBuf.append(".spalte2{color:#FF0000;}");
		htmlBuf.append(".spalte3{color:#333333;}");
		htmlBuf.append("--->");
		htmlBuf.append("</STYLE>");
		htmlBuf.append("</head>");
		htmlBuf.append("<div style=margin-left:0px;>");
		htmlBuf.append("<font face=\"Tahoma\"><style=margin-left=0px;>");
		htmlBuf.append("<br>");
		htmlBuf.append("<table>");
		htmlBuf.append("<tr><td>fertige Rezepte:</td>");
		htmlBuf.append("<td class=\"spalte1\" align=\"right\"><b>"+gesamt+"</b></td></tr>");
		htmlBuf.append("<tr><td>abrechnungsfähig:</td>");
		htmlBuf.append((gesamt != gut ? "<td class=\"spalte2\" align=\"right\">" : "<td class=\"spalte1\" align=\"right\">" )+
					"<b>"+gut+"</b></td></tr>");
		htmlBuf.append("<tr><td>Umsatz:</td>");
		htmlBuf.append("<td class=\"spalte1\" align=\"right\">"+dfx.format(kassenUmsatz[0])+"</td></tr>");
		htmlBuf.append("<tr><td>enth. Rezeptgeb.: </td>");
		htmlBuf.append("<td class=\"spalte1\" align=\"right\">"+dfx.format(kassenUmsatz[1])+"</td></tr>");
		htmlBuf.append("</table>");
		htmlBuf.append("</div>");
		htmlBuf.append("</html>");
		htmlPane.setText(htmlBuf.toString());
	}
	public void holeUmsaetze(String rez_nr){
		buf.setLength(0);
		buf.trimToSize();
		try{
			buf.append(SqlInfo.holeFelder("select edifact from fertige where rez_nr='"+rez_nr+"'").get(0).get(0));
		}catch(Exception ex){}
		if(buf.length()<=0){
			JOptionPane.showMessageDialog(null,"Kassenumsatz für Rezept + "+rez_nr+" kann nicht abgeholt werden. Modul holeUmsatz() (Edifact)");
		}
		//////System.out.println(buf.toString());
		String[] zeilen = buf.toString().split("\n");
		String[] positionen = zeilen[0].split(":");
		//PG=1:PATINTERN=16961:REZNUM=KG57747:GESAMT=102,30:REZGEB=20,26:REZANTEIL=10,26:REZPAUSCHL=10,00:KASSENID=116
		kassenUmsatz[0] = kassenUmsatz[0]+ Double.valueOf(positionen[3].split("=")[1].replace(",", "."));
		kassenUmsatz[1] = kassenUmsatz[1]+ Double.valueOf(positionen[4].split("=")[1].replace(",", "."));
	}
	/**************************************************/

	public void starteAbrechnung(){
		try{
			hmKostentraeger.clear();
			if(!Reha.officeapplication.isActive()){
				try{
					Reha.starteOfficeApplication();
					if(!Reha.officeapplication.isActive()){
						doDlgAbort();
						JOptionPane.showMessageDialog(null, "Das OpenOffice-System reagiert nicht korrekt!\nAbrechnung wird nicht gestartet");
						return;
					}
				}catch(Exception ex){
					doDlgAbort();
					JOptionPane.showMessageDialog(null, "Das OpenOffice-System reagiert nicht korrekt!\nAbrechnung wird nicht gestartet");
					return;

				}
			}
		if(aktuellerKassenKnoten==null){
			abrDlg.setVisible(false);
			abrDlg.dispose();
			abrDlg = null;
			Reha.instance.progressStarten(false);
			JOptionPane.showMessageDialog(null, "Keine Kasse für die Abrechnung ausgewählt!");
			return;
		}
		if(kontrollierteRezepte <=0){
			abrDlg.setVisible(false);
			abrDlg.dispose();
			abrDlg = null;
			Reha.instance.progressStarten(false);
			JOptionPane.showMessageDialog(null, "Für die ausgewählte Kasse sind keine Rezepte zur Abrechnung freigegeben!");
			return;

		}

		abgerechneteRezepte.clear();
		abgerechnetePatienten.clear();
		abrechnungRezepte = 0;
		preis00 = setzePreiseAufNull(preis00);
		preis11 = setzePreiseAufNull(preis11);
		preis31 = setzePreiseAufNull(preis31);
		preis51 = setzePreiseAufNull(preis51);
		positionenBuf.setLength(0);positionenBuf.trimToSize();
		unbBuf.setLength(0);unbBuf.trimToSize();
		unzBuf.setLength(0);unzBuf.trimToSize();
		gesamtBuf.setLength(0);gesamtBuf.trimToSize();
		auftragsBuf.setLength(0);auftragsBuf.trimToSize();
		positionenAnzahl = 0;
		abrDateiName = "";
		annahmeAdresseOk = false;
		/**********************************/

		abzurechnendeKassenID = getAktKTraeger();
		String preisgr = getPreisgruppenKuerzel(aktDisziplin);
		String cmd = "select ik_kasse,ik_kostent,ik_nutzer,ik_physika,ik_papier,"+preisgr+" from kass_adr where ik_kasse='"+abzurechnendeKassenID+"' LIMIT 1";
		kassenIKs.clear();
		kassenIKs = SqlInfo.holeFelder(cmd);
		////System.out.println(cmd);
		if(kassenIKs.size()<=0){
			Reha.instance.progressStarten(false);
			abrDlg.setVisible(false);
			abrDlg.dispose();
			abrDlg = null;
			JOptionPane.showMessageDialog(null, "Fehler - Daten der Krankenkasse konnten nicht ermittelt werden");
			//SqlInfo.sqlAusfuehren("update nummern set rnr='"+aktRechnung+"' where mandant='"+Reha.aktIK+"' LIMIT 1"); // McM 16-05: aktRechnung ist noch gar nicht initialisiert
			return;
		}
		/*
		public String abrechnungsModus = "abrechnung302";
		final String ABR_MODE_302 = "abrechnung302";
		final String ABR_MODE_IV = "abrechnungIV";
		*/

		ik_kasse = kassenIKs.get(0).get(0);
		ik_kostent = kassenIKs.get(0).get(1);
		ik_nutzer = kassenIKs.get(0).get(2);
		ik_physika = kassenIKs.get(0).get(3);
		ik_papier = kassenIKs.get(0).get(4);
		ik_email = SqlInfo.holeEinzelFeld("select email from ktraeger where ikkasse='"+ik_physika+"' LIMIT 1");
		/*
		//System.out.println("  kasse = "+ik_kasse);
		//System.out.println("kostent = "+ik_kostent);
		//System.out.println(" nutzer = "+ik_nutzer);
		//System.out.println("physika = "+ik_physika);
		//System.out.println("  email = "+ik_email);
		*/

		if(abrechnungsModus.equals(ABR_MODE_302)){
			if(ik_email.equals("")){
				ik_email = SqlInfo.holeEinzelFeld("select email1 from kass_adr where ik_kasse='"+ik_physika+"' LIMIT 1");
			}
			if(ik_email.equals("")){
				ik_email = SqlInfo.holeEinzelFeld("select email from ktraeger where ikkasse='"+ik_kasse+"' LIMIT 1");
			}
			if(ik_email.equals("")){
				ik_email = SqlInfo.holeEinzelFeld("select email from ktraeger where ikkasse='"+ik_kostent+"' LIMIT 1");
			}
			if(ik_email.equals("")){
				ik_email = SqlInfo.holeEinzelFeld("select email from ktraeger where ikkasse='"+ik_physika+"' LIMIT 1");
			}

			if(ik_email.trim().equals("")){
				JOptionPane.showMessageDialog(null,"Emailadresse ist weder der Datenannahmestelle noch dem Kostenträger\noder der Kasse selbst zugeordnet."+
				"\n\n Bitte geben Sie die Emailadresse von Hand ein");
				Object ret = JOptionPane.showInputDialog(null, "Geben Sie bitte die Emailadresse der Datenannahmestelle ein","");
				if(ret == null){
					Reha.instance.progressStarten(false);
					abrDlg.setVisible(false);
					abrDlg.dispose();
					abrDlg = null;
					JOptionPane.showMessageDialog(null, "Fehler - keine Emailadresse eingegeben");
					//SqlInfo.sqlAusfuehren("update nummern set rnr='"+aktRechnung+"' where mandant='"+Reha.aktIK+"' LIMIT 1");	// McM 16-05: aktRechnung ist noch gar nicht initialisiert
					return;
				}
				ik_email = String.valueOf(ret);
			}
		}
		//preisVector = RezTools.holePreisVector(diszis[cmbDiszi.getSelectedIndex()],Integer.parseInt(kassenIKs.get(0).get(5))-1);		// McM: kann weg (OK)
		preisVector = RezTools.holePreisVector(disziSelect.getCurrRezClass(),Integer.parseInt(kassenIKs.get(0).get(5))-1);
		name_kostent = holeNameKostentraeger();

		String test = "";
		if(abrechnungsModus.equals(ABR_MODE_302)){
			//TODO: Einbau Test ob Zertifikat abgelaufen
			test = "IK der Krankenkasse: "+ik_kasse+"\n"+
			"IK des Kostenträgers: "+ik_kostent+"\n"+
			"IK des Nutzer mit Entschlüsselungsbefungnis: "+ik_nutzer+"\n"+
			"IK der Datenannahmestelle: "+ik_physika+"\n"+
			"IK der Papierannahmestelle: "+ik_papier+"\n"+
			"Emailadresse der Datenannahmestelle: "+ik_email+"\n"+
			"Name des Kostenträgers: "+name_kostent;

			/************************************************/
			hmKostentraeger.put("name1",String.valueOf(name_kostent));
			/************************************************/
		}else{
			test = "IK der Krankenkasse: "+ik_kasse+"\n"+
			"Keine Abrechnung nach § 302!!!\n\n"+
			"Rechnungsanschrift:\n"+
			hmAlternativeKasse.get("<Ivnam1>")+"\n"+
			hmAlternativeKasse.get("<Ivnam2>")+"\n"+
			hmAlternativeKasse.get("<Ivstrasse>")+"\n"+
			hmAlternativeKasse.get("<Ivplz>")+" "+
			hmAlternativeKasse.get("<Ivort>");
			//hmAlternativeKasse.get("<Ivid>");
			hmKostentraeger.put("name1",String.valueOf(hmAlternativeKasse.get("<Ivnam1>")));
		}
		int anfrage = JOptionPane.showConfirmDialog(null,test , "Die Abrechnung mit diesen Parametern starten?", JOptionPane.YES_NO_OPTION);
		if(anfrage != JOptionPane.YES_OPTION){
			try{
				doDlgAbort();
			}catch(Exception ex){
			}
			return;
		}
		/***********hier erst die Nummer erzeugen **************/
		aktRechnung = Integer.toString(SqlInfo.erzeugeNummer("rnr"));
		if(abrechnungsModus.equals(ABR_MODE_302)){
			aktEsol = StringTools.fuelleMitZeichen(Integer.toString(SqlInfo.erzeugeNummerMitMax("esol", 999)), "0", true, 3);
		}
	    /************************************************/
		hmKostentraeger.put("aktesol",String.valueOf(aktEsol));
		/************************************************/
		aktDfue = StringTools.fuelleMitZeichen(Integer.toString(SqlInfo.erzeugeNummerMitMax("dfue", 99999)), "0", true, 5);
		if(aktRechnung.equals("-1")){
			Reha.instance.progressStarten(false);
			abrDlg.setVisible(false);
			abrDlg.dispose();
			abrDlg = null;
			JOptionPane.showMessageDialog(null, "Fehler - Rechnungsnummer kann nicht bezogen werden");
			return;
		}
		/*****************************************/
		if(abrechnungsModus.equals(ABR_MODE_302)){
			if(ik_email.trim().equals("")){
				JOptionPane.showMessageDialog(null, "Dieser Kasse ist keine Emailadresse zugewiesen\n"+
						"Abrechnung nach §302 ist nicht möglich!");
				cancelRechnung(aktRechnung);
				return;
			}
			if(ik_papier.trim().equals("")){
				JOptionPane.showMessageDialog(null, "Dieser Kasse ist keine Papierannahmestelle zugewiesen\n"+
				"Abrechnung nach §302 ist nicht möglich!");
				cancelRechnung(aktRechnung);
				return;
			}
			if(ik_nutzer.trim().equals("")){
				JOptionPane.showMessageDialog(null, "Dieser Kasse ist kein Nutzer mit Entschlüsselungsbefugnis zugewiesen\n"+
				"Abrechnung nach §302 ist nicht möglich!");
				cancelRechnung(aktRechnung);
				return;
			}
			if(ik_physika.trim().equals("")){
				JOptionPane.showMessageDialog(null, "Dieser Kasse ist keine Empfänger der Abrechnungsdaten zugewiesen\n"+
				"Abrechnung nach §302 ist nicht möglich!");
				cancelRechnung(aktRechnung);
				return;
			}
			hmAnnahme = holeAdresseAnnahmestelle(true);
			annahmeAdresseOk = true;
		}else{
			hmAnnahme = holeAdresseAnnahmestelle(false);
			annahmeAdresseOk = true;
		}

		/********
		 *
		 *
		 */
		abrDlg.setVisible(true);

		holeEdifact();
		macheKopfDaten();
		macheEndeDaten();
		/********
		 *
		 *
		 */
		gesamtBuf.append(unbBuf.toString());
		gesamtBuf.append(positionenBuf.toString());
		gesamtBuf.append(unzBuf.toString());
		abrDlg.setzeLabel("übertrage EDIFACT in Datenbank");

		if(Reha.vollbetrieb && abrechnungsModus.equals(ABR_MODE_302)){
			PreparedStatement ps = null;
			try {
				ps = (PreparedStatement) Reha.instance.conn.prepareStatement(
				    "insert into edifact (r_nummer, r_datum,r_edifact) VALUES (?,?,?)");
			    ps.setString(1, aktRechnung);
			    ps.setString(2, DatFunk.sDatInSQL(DatFunk.sHeute()));
			    ps.setBytes(3, gesamtBuf.toString().getBytes());
			    ps.executeUpdate();
			    ps.close();
			    ps = null;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			finally{
				if(ps != null){
					try {
						ps.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					ps = null;
				}
			}
		}

		if(abrechnungsModus.equals(ABR_MODE_302)){
			try {

				f = new File(Path.Instance.getProghome()+"edifact/"+Reha.getAktIK()+"/"+"esol0"+aktEsol+".org");
				fw = new FileWriter(f);
			    bw = new BufferedWriter(fw);
			    bw.write(gesamtBuf.toString());
			    bw.flush();

			    bw.close();
			    fw.close();


				abrDlg.setzeLabel("Rechnungsdatei verschlüsseln");
			    int originalSize = Integer.parseInt(Long.toString(f.length()));
			    int encryptedSize = originalSize;
			    String skeystore = SystemConfig.hmAbrechnung.get("hmkeystorefile");
				File fkeystore = new File(skeystore);
				if(! fkeystore.exists()){
					abrDlg.setzeLabel("Rechnungsdatei verschlüsseln - fehlgeschlagen!!!");
					String message = "<html>Auf Ihrem System ist keine (ITSG) Zertifikatsdatenbank vorhanden.<br>"+
					"Eine Verschlüsselung gemäß §302 SGB V kann daher nicht durchgeführt werden.<br><br>"+
					"Melden Sie sich im Forum <a href='http://www.thera-pi.org'>www.Thera-Pi.org</a> und fragen Sie nach dem<br>Verschlüsseler <b>'Nebraska'</b></html>";
					Reha.instance.progressStarten(false);
					JOptionPane.showMessageDialog(null, message);

				}else{

				    encryptedSize = doVerschluesseln(aktEsol+".org");

				}

			    if(encryptedSize < 0){
			    	JOptionPane.showMessageDialog(null, "Es ist ein Fehler in der Verschlüsselung aufgetreten!");
					Reha.instance.progressStarten(false);
					abrDlg.setVisible(false);
					abrDlg.dispose();
					abrDlg = null;
			    	return;
			    }

			    doAuftragsDatei(originalSize,encryptedSize);


				f = new File(Path.Instance.getProghome()+"edifact/"+Reha.getAktIK()+"/"+"esol0"+aktEsol+".auf");
				fw = new FileWriter(f);
			    bw = new BufferedWriter(fw);
			    bw.write(auftragsBuf.toString());
			    bw.close();
			    fw.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
			abrDlg.setzeLabel("erstelle Email an: "+ik_email);


			try{
				if("1".equals(SystemConfig.hmAbrechnung.get("hmaskforemail"))){
					int frage = JOptionPane.showConfirmDialog(null,"Wollen Sie die Abrechnungsdatei esol0"+aktEsol+" an die Adresse\n"+
							"--> "+ik_email+" <-- versenden?","Benutzeranfrage",JOptionPane.YES_NO_OPTION);
					if(frage==JOptionPane.YES_OPTION){
						doEmail();
					}
				}else{
					doEmail();
				}

				/*
				String meldung = "Die Dateien "+"esol0"+aktEsol+".auf und "+"esol0"+aktEsol+" wurden erfolgreich\n"+
				"an die Adresse "+ik_email+" versandt.\n";
				JOptionPane.showMessageDialog(null, meldung);
				*/

			}catch(Exception ex){
				String meldung = "Die Dateien "+"esol0"+aktEsol+".auf und "+"esol0"+aktEsol+" sollten an die"+
				"Adresse "+ik_email+" gesendet werden.\n\n"+
				"Versand ist fehlgeschlagen, bitte von Hand erneut senden";
				JOptionPane.showMessageDialog(null, meldung);
			}

		}

		/*********************************************/

		abrDlg.setzeLabel("übertrage Rezepte in Historie");
		if(Reha.vollbetrieb){
			doUebertragen();
			abrDlg.setzeLabel("organisiere Abrechnungsprogramm");
		}
		loescheFertigeRezepteAusKassenNode();
		Reha.instance.progressStarten(false);

		abrDlg.setVisible(false);
		abrDlg.dispose();
		abrDlg = null;
		}catch(Exception ex){
			if(abrDlg != null){
				Reha.instance.progressStarten(false);
				abrDlg.setVisible(false);
				abrDlg.dispose();
				abrDlg = null;
			}
			JOptionPane.showMessageDialog(null,"Fehler beim Abrechnungsvorgang:\n"+ex.getMessage());
			ex.printStackTrace();
		}
		this.abrRez.setRechtsAufNull();
	}
	/**
	 * Abbruch Rechnungserstellung; bereits erzeugte Rechnungsnummer 'zurückgeben'
	 * (McM 16-05)
	 */
	private void cancelRechnung(String aktRnr) {
		if (aktRnr != null){
				SqlInfo.sqlAusfuehren("update nummern set rnr='"+aktRnr+"' where mandant='"+Reha.getAktIK()+"' LIMIT 1");
			}
		doDlgAbort();
	}

	/********************************************************************/
	private void doDlgAbort(){
		if(abrDlg != null){
			Reha.instance.progressStarten(false);
			abrDlg.setVisible(false);
			abrDlg.dispose();
			abrDlg = null;
		}
	}
	private void doEmail(){
		try{
			////System.out.println("Erstelle Emailparameter.....");
			String smtphost = SystemConfig.hmEmailExtern.get("SmtpHost");
			//String pophost = SystemConfig.hmEmailExtern.get("Pop3Host");
			String authent = SystemConfig.hmEmailExtern.get("SmtpAuth");
			String benutzer = SystemConfig.hmEmailExtern.get("Username") ;
			String pass1 = SystemConfig.hmEmailExtern.get("Password");
			String sender = SystemConfig.hmEmailExtern.get("SenderAdresse");
			String secure = SystemConfig.hmEmailExtern.get("SmtpSecure");
			String useport = SystemConfig.hmEmailExtern.get("SmtpPort");
			//String recipient = "m.schuchmann@rta.de"+","+SystemConfig.hmEmailExtern.get("SenderAdresse");
			String recipient = ik_email+","+SystemConfig.hmEmailExtern.get("SenderAdresse");
			String text = "";
			boolean authx = (authent.equals("0") ? false : true);
			boolean bestaetigen = false;
			String[] encodedDat = {Path.Instance.getProghome()+"edifact/"+Reha.getAktIK()+"/"+"esol0"+aktEsol,"esol0"+aktEsol};
			String[] aufDat = {Path.Instance.getProghome()+"edifact/"+Reha.getAktIK()+"/"+"esol0"+aktEsol+".auf","esol0"+aktEsol+".auf"};
			ArrayList<String[]> attachments = new ArrayList<String[]>();
			attachments.add(encodedDat);
			attachments.add(aufDat);
			EmailSendenExtern oMail = new EmailSendenExtern();
			try{
				////System.out.println("Starte Emailversand.....");
				oMail.sendMail(smtphost, benutzer, pass1, sender, recipient, zertifikatVon.replace("IK", "")/*Reha.aktIK*/, text,attachments,authx,bestaetigen,secure,useport);
				oMail = null;
				////System.out.println("Emailversand beendet.....");

			}catch(Exception e){
				e.printStackTrace( );
				JOptionPane.showMessageDialog(null, "Emailversand fehlgeschlagen\n\n"+
	        			"Mögliche Ursachen:\n"+
	        			"- falsche Angaben zu Ihrem Emailpostfach und/oder dem Provider\n"+
	        			"- Sie haben keinen Kontakt zum Internet"+"\n\nFehlertext:"+e.getLocalizedMessage());
			}
		}catch(Exception ex){
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, "Emailversand fehlgeschlagen\n\n"+
	        			"Mögliche Ursachen:\n"+
	        			"- falsche Angaben zu Ihrem Emailpostfach und/oder dem Provider\n"+
	        			"- Sie haben keinen Kontakt zum Internet - Fehlermeldung:\n\n"+ex.getLocalizedMessage());
		}
	}
	/********************************************************************/
	private void loescheFertigeRezepteAusKassenNode(){
		try{
			int lang = aktuellerKassenKnoten.getChildCount();
			JXTTreeNode node;
			for(int i = (lang-1); i >= 0;i--){
				node = (JXTTreeNode) aktuellerKassenKnoten.getChildAt(i);
				if(node.knotenObjekt.fertig){
					//////System.out.println("Lösche KindKnoten an "+i);
					treeModelKasse.removeNodeFromParent(node);
				}
			}
			removeKassenNode(this.aktuellerKassenKnoten);
/*
			if(aktuellerKassenKnoten.getChildCount() <= 0){
				JXTTreeNode newKNode = sameIkPap(this.aktuellerKassenKnoten);
				treeModelKasse.removeNodeFromParent(aktuellerKassenKnoten);
				if(newKNode != null) {
					doEinlesen(null,newKNode.getObject().rez_num);
				}
			}
 */
			treeKasse.validate();
			this.treeKasse.repaint();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	/***************************************************************/
	private void doUebertragen(){
		try{
		String aktiverPatient = "";
		JComponent patient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
		if(patient != null){
			aktiverPatient = Reha.instance.patpanel.aktPatID;
		}

		Vector<String> feldNamen = SqlInfo.holeFeldNamen("verordn",true,Arrays.asList(new String[] {"id"}) );
		////System.out.println(feldNamen);

		rechnungBuf.setLength(0);
		rechnungBuf.trimToSize();
		rechnungBuf.append("select ");

		int rezepte = 0;
		int rezeptFelder = 0;
		for(int i = 0; i < feldNamen.size();i++){
			if(i > 0){
				rechnungBuf.append(","+feldNamen.get(i));
			}else{
				rechnungBuf.append(feldNamen.get(i));
			}
		}
		rechnungBuf.append(" from verordn where rez_nr='");
		Vector<Vector<String>> vec = null;
		rezepte = abgerechneteRezepte.size();
		for(int i2 = 0; i2 < rezepte;i2++){
			abrDlg.setzeLabel("übertrage Rezepte in Historie, übertrage Rezept: "+abgerechneteRezepte.get(i2));
			vec = SqlInfo.holeFelder(rechnungBuf.toString()+abgerechneteRezepte.get(i2)+"'");
			rezeptFelder = vec.get(0).size();
			historieBuf.setLength(0);
			historieBuf.trimToSize();
			historieBuf.append("insert into lza set ");
			for(int i3 = 0; i3 < rezeptFelder;i3++){
				if(!vec.get(0).get(i3).equals("")){
					if(i3 > 0){
						historieBuf.append(","+feldNamen.get(i3)+"='"+StringTools.Escaped(vec.get(0).get(i3))+"'");
					}else{
						historieBuf.append(feldNamen.get(i3)+"='"+StringTools.Escaped(vec.get(0).get(i3))+"'");
					}
				}
			}
			//////System.out.println(historieBuf.toString());
			//////System.out.println("Übertrage Rezept "+abgerechneteRezepte.get(i2)+" in Langzeitarchiv = Historie");

			SqlInfo.sqlAusfuehren(historieBuf.toString());


			/***
<<<<<<< HEAD
			 *
			 *
			 * In der Echtfunktion muß das Löschen in der rezept-Datenbank eingeschaltet werden
			 * und das sofortige Löschen aus der Historie auscheschaltet werden
			 *
			 *
=======
			 * 
			 * In der Echtfunktion muß das Löschen in der rezept-Datenbank eingeschaltet werden
			 * und das sofortige Löschen aus der Historie auscheschaltet werden
			 *  
>>>>>>> fd1bd68... source cleanup
			 */

			//SqlInfo.sqlAusfuehren("delete from lza where rez_nr='"+abgerechneteRezepte.get(i2)+"' LIMIT 1");
			String delrez = String.valueOf(abgerechneteRezepte.get(i2));
			SqlInfo.sqlAusfuehren("delete from fertige where rez_nr='"+delrez+"' LIMIT 1");
			SqlInfo.sqlAusfuehren("delete from verordn where rez_nr='"+delrez+"' LIMIT 1");
			SqlInfo.sqlAusfuehren("delete from volle where rez_nr='"+delrez+"'");
			if(aktiverPatient.equals(abgerechnetePatienten.get(i2)) ){
				posteAktualisierung(aktiverPatient.toString());
				//Reha.instance.patpanel.aktRezept.setzeKarteiLasche();
			}


		}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	private void posteAktualisierung(String patid){
		final String xpatid = patid;
		new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				String s1 = String.valueOf("#PATSUCHEN");
				String s2 = xpatid;
				PatStammEvent pEvt = new PatStammEvent(getInstance());
				pEvt.setPatStammEvent("PatSuchen");
				pEvt.setDetails(s1,s2,"") ;
				PatStammEventClass.firePatStammEvent(pEvt);
				return null;
			}

		}.execute();
	}
	/***************************************************************/
	private int doVerschluesseln(String datei){
		try {
			String keystore = SystemConfig.hmAbrechnung.get("hmkeystorefile");

			NebraskaKeystore store = new NebraskaKeystore(keystore, SystemConfig.hmAbrechnung.get("hmkeystorepw"),"123456", zertifikatVon.replace("IK", ""));

			NebraskaEncryptor encryptor = store.getEncryptor(ik_nutzer);
			String inFile = Path.Instance.getProghome()+"edifact/"+Reha.getAktIK()+"/"+"esol0"+aktEsol+".org";
			long size = encryptor.encrypt(inFile, inFile.replace(".org", ""));
			return Integer.parseInt(Long.toString(size));
		} catch (NebraskaCryptoException e) {
			e.printStackTrace();
		} catch (NebraskaFileException e) {
			e.printStackTrace();
		} catch (NebraskaNotInitializedException e) {
			e.printStackTrace();
		}
		return -1;
	}
	/***************************************************************/
	private void doAuftragsDatei(int originalSize,int encryptedSize){
		auftragsBuf.append("500000"+"01"+"00000348"+"000");
		auftragsBuf.append("ESOL0"+aktEsol);
		auftragsBuf.append("     ");
		auftragsBuf.append(StringTools.fuelleMitZeichen(zertifikatVon.replace("IK", "")/*Reha.aktIK*/, " ", false, 15));
		auftragsBuf.append(StringTools.fuelleMitZeichen(zertifikatVon.replace("IK", "")/*Reha.aktIK*/, " ", false, 15));
		auftragsBuf.append(StringTools.fuelleMitZeichen(ik_nutzer, " ", false, 15));
		auftragsBuf.append(StringTools.fuelleMitZeichen(ik_physika, " ", false, 15));
		auftragsBuf.append("000000");
		auftragsBuf.append("000000");
		auftragsBuf.append(abrDateiName);
		auftragsBuf.append(getEdiDatumFromDeutsch(DatFunk.sHeute())+getEdiTimeString(true));
		auftragsBuf.append(StringTools.fuelleMitZeichen("0", "0", false, 14));
		auftragsBuf.append(StringTools.fuelleMitZeichen("0", "0", false, 14));
		auftragsBuf.append(StringTools.fuelleMitZeichen("0", "0", false, 14));
		auftragsBuf.append("000000");
		auftragsBuf.append("0");
		auftragsBuf.append(StringTools.fuelleMitZeichen(Integer.toString(originalSize), "0", true, 12) );
		auftragsBuf.append(StringTools.fuelleMitZeichen(Integer.toString(encryptedSize), "0", true, 12) );
		auftragsBuf.append("I800");
		auftragsBuf.append("0303");
		auftragsBuf.append("   ");
		auftragsBuf.append(StringTools.fuelleMitZeichen("0", "0", true, 5) );
		auftragsBuf.append(StringTools.fuelleMitZeichen("0", "0", true, 8) );
		auftragsBuf.append("0");
		auftragsBuf.append("00");
		auftragsBuf.append("0");
		auftragsBuf.append(StringTools.fuelleMitZeichen("0", "0", true, 10) );
		auftragsBuf.append(StringTools.fuelleMitZeichen("0", "0", true, 6) );
		auftragsBuf.append(StringTools.fuelleMitZeichen(" ", " ", true, 28) );
		auftragsBuf.append(StringTools.fuelleMitZeichen(" ", " ", true, 44) );
		auftragsBuf.append(StringTools.fuelleMitZeichen(" ", " ", true, 30) );
		rlisteesol = String.valueOf(aktEsol); //aktEsol.toString();
		rlistekasse = String.valueOf(getAbrechnungKasse());
	}
	/*************************************************/
	private void macheEndeDaten(){
		String zeilenzahl = StringTools.fuelleMitZeichen(Integer.toString(positionenAnzahl+4), "0", true, 6);
		unzBuf.append("UNT"+plus+zeilenzahl+plus+"00002"+EOL);
		unzBuf.append("UNZ"+plus+"000002"+plus+aktDfue+EOL);
	}

	/***************************************************************/

	private void macheKopfDaten(){
		//aktRechnung = Integer.toString(SqlInfo.erzeugeNummer("rnr"));
		new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				//hmAnnahme = holeAdresseAnnahmestelle();
				if(!annahmeAdresseOk){
					long zeit = System.currentTimeMillis();
					while(!annahmeAdresseOk){
						Thread.sleep(50);
						if(System.currentTimeMillis()-zeit > 5000){
							JOptionPane.showMessageDialog(null,"Adresse der Annahmestelle konnte nicht ermittelt werden");
							break;
						}
					}
				}

				if(abrDruck != null){
					abrDruck.setIKundRnr(ik_papier, aktRechnung,hmAnnahme);
					abrDruck = null;
				}else{
					JOptionPane.showMessageDialog(null, "Fehler im Rechnungsdruck - Fehler = abrDruck==null");
				}
				try{
					rezepteUebertragen();
					rechnungAnlegen();
				}catch(Exception ex){
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null, "Fehler im Modul Rezept übertragen und Rechnung anlegen\n"+ex.getMessage());
				}
				if(abrechnungsModus.equals(ABR_MODE_302)){
					try{
						Thread.sleep(100);
						new BegleitzettelDrucken(getInstance(),abrechnungRezepte,ik_kostent,name_kostent,hmAnnahme, aktRechnung,Path.Instance.getProghome()+"vorlagen/"+Reha.getAktIK()+"/"+SystemConfig.hmAbrechnung.get("hmgkvbegleitzettel"));
					}catch(Exception ex){
						JOptionPane.showMessageDialog(null, "Fehler im Modul BegleitzettlDrucken - Fehler-Exception: ex\n"+ex.getMessage());
					}
				}
				return null;
			}
		}.execute();
		//////System.out.println(aktEsol + "  - "+aktDfue);
		String sgruppe = null;
		if(this.aktDisziplin.equals("Rsport")){
			sgruppe = "H";
		}else if(this.aktDisziplin.equals("Ftrain")){
			sgruppe = "I";
		}else{
			sgruppe = "B";
		}

		unbBuf.append("UNB+UNOC:3+"+zertifikatVon.replace("IK", "")/*Reha.aktIK*/+plus+ik_nutzer+plus);
		unbBuf.append(getEdiDatumFromDeutsch(DatFunk.sHeute())+":"+getEdiTimeString(false)+plus);
		unbBuf.append(aktDfue+plus+sgruppe+plus);
		abrDateiName = "SL"+zertifikatVon.replace("IK", "").substring(2,8)/*Reha.aktIK.substring(2,8)*/+"S"+getEdiMonat();
		unbBuf.append(abrDateiName+plus);
		unbBuf.append("2"+EOL);
		//unbBuf.append(aktDfue+plus+"B"+plus+"SL"+Reha.aktIK.substring(2,8)+"S"+getEdiMonat()+plus+"2"+EOL);

		unbBuf.append("UNH+00001+SLGA:"+SlgaVersion+":0:0"+EOL);
		unbBuf.append("FKT+01"+plus+plus+Reha.getAktIK()+plus+ik_kostent+plus+ik_kasse+plus+zertifikatVon.replace("IK", "")/*Reha.aktIK*/+EOL);
		unbBuf.append("REC"+plus+aktRechnung+":0"+plus+getEdiDatumFromDeutsch(DatFunk.sHeute())+plus+(lOwnCert ? "1" : "2")+EOL);
		unbBuf.append("UST"+plus+SystemConfig.hmFirmenDaten.get("Steuernummer")+plus+"J"+EOL);
		unbBuf.append("GES"+plus+"00"+plus+dfx.format(preis00[0])+plus+dfx.format(preis00[1])+plus+dfx.format(preis00[2])+EOL);
		unbBuf.append("GES"+plus+"11"+plus+dfx.format(preis11[0])+plus+dfx.format(preis11[1])+plus+dfx.format(preis11[2])+EOL);
		unbBuf.append("GES"+plus+"31"+plus+dfx.format(preis31[0])+plus+dfx.format(preis31[1])+plus+dfx.format(preis31[2])+EOL);
		unbBuf.append("GES"+plus+"51"+plus+dfx.format(preis51[0])+plus+dfx.format(preis51[1])+plus+dfx.format(preis51[2])+EOL);
		unbBuf.append("NAM"+plus+
				(abrRez.hochKomma(SystemConfig.hmFirmenDaten.get("Ikbezeichnung")).length() > 30 ?
						abrRez.hochKomma(SystemConfig.hmFirmenDaten.get("Ikbezeichnung")).substring(0,30) :
							abrRez.hochKomma(SystemConfig.hmFirmenDaten.get("Ikbezeichnung"))	)+plus+
				abrRez.hochKomma(SystemConfig.hmFirmenDaten.get("Anrede")).trim()+" "+
				(abrRez.hochKomma(SystemConfig.hmFirmenDaten.get("Nachname")).trim().length() > 25 ?
						abrRez.hochKomma(SystemConfig.hmFirmenDaten.get("Nachname")).trim().substring(0,25) :
							abrRez.hochKomma(SystemConfig.hmFirmenDaten.get("Nachname")).trim()	)+
				plus+SystemConfig.hmFirmenDaten.get("Telefon")+EOL);
		unbBuf.append("UNT+000010+00001"+EOL);
		unbBuf.append("UNH+00002+SLLA:"+SllaVersion+":0:0"+EOL);
		unbBuf.append("FKT+01"+plus+plus+Reha.getAktIK()+plus+ik_kostent+plus+ik_kasse+EOL);
		unbBuf.append("REC"+plus+aktRechnung+":0"+plus+getEdiDatumFromDeutsch(DatFunk.sHeute())+plus+(lOwnCert ? "1" : "2")+EOL);
		getEdiTimeString(false);
	}

	/***************************************************************/

	AbrechnungGKV getInstance(){
		return this;
	}
	
	public AbrechnungRezept getInstanceAbrechnungRezept (){
		return abrRez;
	}
	
	/***************************************************************/

	private String holeNameKostentraeger(){
		Vector<Vector<String>> vec = SqlInfo.holeFelder("select name1 from ktraeger where ikkasse ='"+ik_kostent+"' LIMIT 1");
		if(vec.size()==0){
			return "";
		}
		return vec.get(0).get(0);
	}
	/*
	private void holeAlleKostentraegerDaten(){
		abzurechnendeKassenID = getAktKTraeger();
		String preisgr = getPreisgruppenKuerzel(aktDisziplin);
		String cmd = "select ik_kasse,ik_kostent,ik_nutzer,ik_physika,ik_papier,"+preisgr+" from kass_adr where ik_kasse='"+abzurechnendeKassenID+"' LIMIT 1";
		kassenIKs.clear();
		kassenIKs = SqlInfo.holeFelder(cmd);
		////System.out.println(cmd);
		if(kassenIKs.size()<=0){
			Reha.instance.progressStarten(false);
			abrDlg.setVisible(false);
			abrDlg.dispose();
			abrDlg = null;
			JOptionPane.showMessageDialog(null, "Fehler - Daten der Krankenkasse konnten nicht ermittelt werden");
			return;
		}
		ik_kasse = kassenIKs.get(0).get(0);
		ik_kostent = kassenIKs.get(0).get(1);
		ik_nutzer = kassenIKs.get(0).get(2);
		ik_physika = kassenIKs.get(0).get(3);
		ik_papier = kassenIKs.get(0).get(4);
		ik_email = SqlInfo.holeEinzelFeld("select email from ktraeger where ikkasse='"+ik_physika+"' LIMIT 1");
		preisVector = RezTools.holePreisVector(diszis[cmbDiszi.getSelectedIndex()],Integer.parseInt(kassenIKs.get(0).get(5))-1);
		name_kostent = holeNameKostentraeger();
		String test = "IK der Krankenkasse: "+ik_kasse+"\n"+
		"IK des Kostenträgers: "+ik_kostent+"\n"+
		"IK des Nutzer mit Entschlüsselungsbefungnis: "+ik_nutzer+"\n"+
		"IK der Datenannahmestelle: "+ik_physika+"\n"+
		"IK der Papierannahmestelle: "+ik_papier+"\n"+
		"Emailadresse der Datenannahmestelle: "+ik_email+"\n"+
		"Name des Kostenträgers: "+name_kostent;
	}
	*/
	/***************************************************************/

	private HashMap<String,String> holeAdresseAnnahmestelle(boolean nach302){
		HashMap<String,String> hmAdresse = new HashMap<String,String>();
		String[] hmKeys = {"<gkv1>","<gkv2>","<gkv3>","<gkv4>","<gkv5>","<gkv6>"};
		if(nach302){
			Vector<Vector<String>> vec = SqlInfo.holeFelder("select kassen_nam1,kassen_nam2,strasse,plz,ort from kass_adr where ik_kasse ='"+ik_papier+"' LIMIT 1");
			if(vec.size()==0){
				vec = SqlInfo.holeFelder("select name1,name2,adresse3,adresse1,adresse2 from ktraeger where ikkasse ='"+ik_papier+"' LIMIT 1");
				if(vec.size()==0){
					for(int i = 0; i < hmKeys.length-1;i++){
						hmAdresse.put(hmKeys[i], "");
					}
					hmAdresse.put(hmKeys[5],aktRechnung);
					JOptionPane.showMessageDialog(null,"Achtung Daten für die Papierannahmestelle konnt nicht ermittelt werden (vermutlich wieder mal AOK....)!\n\nBitte die Daten von Hand auf den Ausdrucken eintragen");
					return hmAdresse;
				}
			}
			hmAdresse.put(hmKeys[0],vec.get(0).get(0) );
			hmAdresse.put(hmKeys[1],vec.get(0).get(1) );
			hmAdresse.put(hmKeys[2],vec.get(0).get(2) );
			hmAdresse.put(hmKeys[3],vec.get(0).get(3)+" "+vec.get(0).get(4));
			hmAdresse.put(hmKeys[4],"");
			hmAdresse.put(hmKeys[5],aktRechnung);
			return hmAdresse;
		}else{
			hmAdresse.put(hmKeys[0],hmAlternativeKasse.get("<Ivnam1>") );
			hmAdresse.put(hmKeys[1],hmAlternativeKasse.get("<Ivnam2>") );
			hmAdresse.put(hmKeys[2],hmAlternativeKasse.get("<Ivstrasse>") );
			hmAdresse.put(hmKeys[3],hmAlternativeKasse.get("<Ivplz>")+" "+hmAlternativeKasse.get("<Ivort>"));
			hmAdresse.put(hmKeys[4],"");
			hmAdresse.put(hmKeys[5],aktRechnung);
		}
		return hmAdresse;
	}

	/***************************************************************/

	private Double[] setzePreiseAufNull(Double[] preis){
		preis[0] = 0.00;
		preis[1] = 0.00;
		preis[2] = 0.00;
		return preis;
	}
	private String getEdiMonat(){
		String tag = DatFunk.sHeute();
		return tag.substring(3,5);
	}
	private String getEdiDatumFromDeutsch(String deutschDat){
		if(deutschDat.trim().length()<10){
			return "";
		}
		return deutschDat.substring(6)+deutschDat.substring(3,5)+deutschDat.substring(0,2);
	}
	private String getEdiTimeString(boolean mitsekunden){
		Date date = new Date();
		String[] datesplit = date.toString().split(" ");
		//////System.out.println(date.toString());
		if(mitsekunden){
			//////System.out.println("Zeit mit Sekunden"+datesplit[3].substring(0,2)+datesplit[3].substring(3,5)+datesplit[3].substring(6,8));
			return datesplit[3].substring(0,2)+datesplit[3].substring(3,5)+datesplit[3].substring(6,8);
		}
		return datesplit[3].substring(0,2)+datesplit[3].substring(3,5);
	}


	/***************************************************************/

	private void holeEdifact(){
		try {
			if(SystemConfig.hmAbrechnung.get("hmgkvrauchdrucken").equals("1")){
				abrDruck = new AbrechnungDrucken(this,Path.Instance.getProghome()+
						"vorlagen/"+
						Reha.getAktIK()+
						"/"+
						SystemConfig.hmAbrechnung.get("hmgkvformular"));
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Fehler im Modul Rechnungdruck in AbrechnungGKV.java");
			abrDruck = null;
			e.printStackTrace();
		}
		int lang = aktuellerKassenKnoten.getChildCount();
		JXTTreeNode node;
		Vector<Vector<String>> vec;
		for(int i = 0; i < lang;i++){
			node = (JXTTreeNode) aktuellerKassenKnoten.getChildAt(i);
			if(node.knotenObjekt.fertig){
				vec = SqlInfo.holeFelder("select edifact from fertige where rez_nr='"+node.knotenObjekt.rez_num+"'");
				//abrDlg.setzeLabel("Edifact-Daten holen von Rezept:"+(String) node.knotenObjekt.rez_num);
				try{
					if(!annahmeAdresseOk){
						long zeit = System.currentTimeMillis();
						while(!annahmeAdresseOk){
							Thread.sleep(50);
							if(System.currentTimeMillis()-zeit > 5000){
								JOptionPane.showMessageDialog(null,"Adresse der Annahmestelle konnte nicht ermittelt werden");
								break;
							}
						}
					}
					Thread.sleep(75);
					//abzurechnendeKassenID = holeAbrechnungsKasse(vec.get(0).get(0));
					abgerechneteRezepte.add(node.knotenObjekt.rez_num);
					abgerechnetePatienten.add(node.knotenObjekt.pat_intern);
					//hier den Edifact-Code analysieren und die Rechnungsdatei erstellen;
					try{
						analysierenEdifact(vec.get(0).get(0),node.knotenObjekt.rez_num);
					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(null,"Unbekannter Fehler bei Edifact analysierenEdifact()\n"+ex.getLocalizedMessage());

					}
					try{
						anhaengenEdifact(vec.get(0).get(0));
					}catch(Exception ex){
						JOptionPane.showMessageDialog(null,"Unbekannter Fehler bei Edifact anhaengenEdifact()\n"+ex.getLocalizedMessage());
					}

				}catch(Exception ex){
					JOptionPane.showMessageDialog(null,"Unbekannter Fehler bei Edifact anhängen");
				}
			}
		}
		if(abgerechneteRezepte.size() > 0){
			/**************Hier den offenen Posten anlegen***************/
			abrDlg.setzeLabel("Offene Posten anlegen für Rechnung Nr.: "+aktRechnung );
			////System.out.println("  abgerechnete Rezepte = "+abgerechneteRezepte);
			////System.out.println("abgerechnete Patienten = "+abgerechnetePatienten);
			////System.out.println("abger. Bruttovolumen   = "+preis00[1]);
			////System.out.println("  abger. Rezeptanteil  = "+preis00[2]);
			////System.out.println("  abger. Nettovolumen  = "+preis00[0]);
			////System.out.println("Name der abger. Kasse  = "+name_kostent);
			////System.out.println("       IK-Kostenträger = "+ik_kostent);
			//////System.out.println("             Disziplin = "+diszis[cmbDiszi.getSelectedIndex()]);		// McM: weg
			////System.out.println("             Disziplin = "+disziSelect.getCurrRezClass());
			////System.out.println("          Rechnung Nr. = "+aktRechnung);
			if(Reha.vollbetrieb){
				anlegenOP();
			}

		}
	}

	/***************************************************************/

	private void anlegenOP(){
		/************************************************/
		rechnungBuf.setLength(0);
		rechnungBuf.trimToSize();
		rechnungBuf.append("insert into rliste set ");
		rechnungBuf.append("r_nummer='"+aktRechnung+"', ");
		rechnungBuf.append("r_datum='"+DatFunk.sDatInSQL(DatFunk.sHeute())+"', ");
		rechnungBuf.append("r_kasse='"+hmKostentraeger.get("name1")+", "+"esol0"+hmKostentraeger.get("aktesol")+"', ");
//		rechnungBuf.append("r_klasse='"+diszis[cmbDiszi.getSelectedIndex()]+"', ");		// McM: weg
		rechnungBuf.append("r_klasse='"+disziSelect.getCurrRezClass()+"', ");
		rechnungBuf.append("r_betrag='"+dfx.format(preis00[0]).replace(",", ".")+"', ");
		rechnungBuf.append("r_offen='"+dfx.format(preis00[0]).replace(",", ".")+"', ");
		rechnungBuf.append("r_zuzahl='"+dfx.format(preis00[2]).replace(",", ".")+"', ");
		rechnungBuf.append("ikktraeger='"+ik_kostent+"',");
		rechnungBuf.append("ik='"+Reha.getAktIK()+"'");
		SqlInfo.sqlAusfuehren(rechnungBuf.toString());
	}

	/***************************************************************/

	private void analysierenEdifact(String edifact,String rez_num){
		//////System.out.println(edifact);
		Vector<String> position = new Vector<String>();
		Vector<BigDecimal>anzahl = new Vector<BigDecimal>();
		Vector<BigDecimal>preis = new Vector<BigDecimal>();
		Vector<BigDecimal>einzelpreis = new Vector<BigDecimal>();
		Vector<BigDecimal>einzelzuzahlung = new Vector<BigDecimal>();
		Vector<BigDecimal>rezgeb = new Vector<BigDecimal>();
		Vector<BigDecimal>abrtage = new Vector<BigDecimal>();
		BigDecimal bdAnzahl = null;
		BigDecimal einzelPreisTest = null;
		//BigDecimal zuzahlTest = null;
		String[] zeilen = edifact.split("\n");
		boolean preisUmstellung = false;
		boolean zuzahlUmstellung = false;
		String[] woerter;
		String dummy;
		int pos = 0;
		int zugabe = 0;
		for(int i = 0; i < zeilen.length;i++){
			if(zeilen[i].startsWith("EHE+") || zeilen[i].startsWith("ENF++") ){
				if(zeilen[i].startsWith("ENF++")){
					zugabe = 1;
				}
				woerter = zeilen[i].split("\\+");
				if(!position.contains(woerter[2+zugabe])){
					position.add(woerter[2+zugabe]);
					bdAnzahl = BigDecimal.valueOf(Double.valueOf(woerter[3+zugabe].replace(",", ".")));
					anzahl.add(bdAnzahl);
					abrtage.add(BigDecimal.valueOf(Double.valueOf("1.00")));
					preis.add(BigDecimal.valueOf(Double.valueOf(woerter[4+zugabe].replace(",", "."))).multiply(
							bdAnzahl ));
					if(woerter.length==(7+zugabe)){
						//Einstieg2 für Kilometer
						dummy = woerter[6+zugabe].replace("'", "").replace(",", ".");
						if(zuzahlModusDefault){
							rezgeb.add(BigDecimal.valueOf(Double.valueOf(dummy)));
							einzelzuzahlung.add(BigDecimal.valueOf(Double.valueOf(dummy)));
						}else{
							/*
							System.out.println("Dummy = "+dummy);
							System.out.println("Muliplikator = "+bdAnzahl);
							*/
							//Herr Lehmann: nächste Zeile muß freigeschaltet werden für Einzelkilometer
							rezgeb.add(BigDecimal.valueOf(Double.valueOf(dummy)).multiply(bdAnzahl));
							//Herr Lehmann: nächste Zeile muß freigeschaltet werden für Einzelkilometer
							einzelzuzahlung.add(BigDecimal.valueOf(Double.valueOf(dummy)).multiply(bdAnzahl));
						}
					}else{
						rezgeb.add(BigDecimal.valueOf(Double.valueOf("0.00")));
						einzelzuzahlung.add(BigDecimal.valueOf(Double.valueOf("0.00")));
					}

					einzelpreis.add(BigDecimal.valueOf(Double.valueOf(woerter[4+zugabe].replace(",", "."))));

				}else{
					pos = position.indexOf(woerter[2+zugabe]);
					einzelPreisTest = BigDecimal.valueOf(Double.valueOf(woerter[4+zugabe].replace(",", ".")));
					if(!einzelPreisTest.equals(einzelpreis.get(pos))){
						preisUmstellung = true;
					}
					bdAnzahl = BigDecimal.valueOf(Double.valueOf(woerter[3+zugabe].replace(",", ".")));
					anzahl.set(pos, anzahl.get(pos).add(BigDecimal.valueOf(Double.valueOf(woerter[3+zugabe].replace(",", ".")))));
					preis.set(pos, preis.get(pos).add(BigDecimal.valueOf(Double.valueOf(woerter[4+zugabe].replace(",", "."))).multiply(
							bdAnzahl)));
					abrtage.set(pos,abrtage.get(pos).add(BigDecimal.valueOf(Double.valueOf("1.00"))));
					if(woerter.length==(7+zugabe)){
						//Einstieg3 für Kilometer
						dummy = woerter[6+zugabe].replace("'", "").replace(",", ".");
						if(zuzahlModusDefault){
							rezgeb.set(pos,rezgeb.get(pos).add(BigDecimal.valueOf(Double.valueOf(dummy))));
							if(! BigDecimal.valueOf(Double.valueOf(dummy)).equals(einzelzuzahlung.get(pos))){
								zuzahlUmstellung = true;
							}
						}else{
							//Herr Lehmann: nächste Zeile muß freigeschaltet werden für Einzelkilometer
							/*
							System.out.println("Dummy = "+dummy);
							System.out.println("Muliplikator (falsch) = "+anzahl.get(pos));
							System.out.println("Muliplikator (richtig) = "+bdAnzahl);
							*/
							rezgeb.set(pos,rezgeb.get(pos).add(BigDecimal.valueOf(Double.valueOf(dummy)).multiply(bdAnzahl)));
							//Herr Lehmann: nächste Zeile muß freigeschaltet werden für Einzelkilometer
							if(! BigDecimal.valueOf(Double.valueOf(dummy)).multiply(bdAnzahl).equals(einzelzuzahlung.get(pos))){
								zuzahlUmstellung = true;
							}
							/*
							rezgeb.set(pos,rezgeb.get(pos).add(BigDecimal.valueOf(Double.valueOf(dummy)).multiply(anzahl.get(pos))));
							if(! BigDecimal.valueOf(Double.valueOf(dummy)).multiply(bdAnzahl).equals(einzelzuzahlung.get(pos))){
								zuzahlUmstellung = true;
							}
							*/
						}
					}else{
						//System.out.println("Keine Zuzahlung Dummy = ");
						//System.out.println("Keine Zuzahlung Muliplikator = "+anzahl.get(pos));
						rezgeb.set(pos,rezgeb.get(pos).add(BigDecimal.valueOf(Double.valueOf("0.00"))));
						if(!BigDecimal.valueOf(Double.valueOf("0.00")).equals(einzelzuzahlung.get(pos))){
							//////System.out.println("Einzelzuzahlung = "+einzelzuzahlung.get(pos));
							//////System.out.println("Vergleichswert = 0.00 ");
							zuzahlUmstellung = true;
							//System.out.println("Umstellung der Zuzahlung = "+zuzahlUmstellung);
						}
					}
				}
			}
			try {
				Thread.sleep(15);
			} catch (InterruptedException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,"Fehler in Thread.sleep(25) analysierenEdifact\n"+e.getMessage());
			}
		}
		String[] splits = zeilen[0].split(":");

		try {
			abrechnungRezepte++;
			if(abrDruck != null){
				abrDruck.setDaten(splits[9].split("=")[1],
						splits[10].split("=")[1],
						splits[2].split("=")[1],
						position,
						anzahl,
						abrtage,
						einzelpreis,
						preis,
						rezgeb,
						(splits[6].split("=")[1].equals("10,00") ? true : false));
			}
			/*
			////System.out.println("         Rezept Nr. ="+abrechnungRezepte+" ********Abrechnungsposition Anfang********");
			////System.out.println("               Name = "+splits[9].split("=")[1]);
			////System.out.println("             Status = "+splits[10].split("=")[1]);
			////System.out.println("          RezeptNr. = "+splits[2].split("=")[1]);
			////System.out.println("  Positionen Vector = "+position);
			////System.out.println("      Anzahl Vector = "+anzahl);
			////System.out.println("    Abr.tage Vector = "+abrtage);
			////System.out.println(" Einzelpreis Vector = "+einzelpreis);
			////System.out.println(" Kummulierte Preise = "+preis);
			////System.out.println(" Kummulierte Zuzahl.= "+rezgeb);
			////System.out.println(" Zuzahlungen einzel = "+einzelzuzahlung);
			////System.out.println("      mit Pauschale = "+(splits[6].split("=")[1].equals("10,00") ? true : false));
			////System.out.println("mit Preisumstellung = "+preisUmstellung);
			////System.out.println("  mit Zuzahlwechsel = "+zuzahlUmstellung);
			////System.out.println("bislang abgerechnet = "+abgerechneteRezepte);
			////System.out.println("   Rechnungsadresse = "+hmAnnahme);
			////System.out.println("Rezept Nr. ="+abrechnungRezepte+" ********Abrechnungsposition Ende********");
			*/
			/////////////////Hier die Sätze in der Rechnungsdatei anlegen///////////////
			if(Reha.vollbetrieb){
				schreibeInRechnungDB(
						splits,
						position,
						anzahl,
						abrtage,
						einzelpreis,
						preis,
						rezgeb,
						preisUmstellung,
						zuzahlUmstellung);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***************************************************************/

	private void schreibeInRechnungDB(
			String[] kopf,
			Vector<String> positionen,
			Vector<BigDecimal>anzahl,
			Vector<BigDecimal>abrtage,
			Vector<BigDecimal>einzelpreis,
			Vector<BigDecimal>preis,
			Vector<BigDecimal>rezgeb,
			boolean preisUmstellung,
			boolean zuzahlUmstellung){
			try{
				if(hmAnnahme.get("<gkv1>").trim().equals("")){
					holeAdresseAnnahmestelle(true);
				}
			}catch(Exception ex){
				holeAdresseAnnahmestelle(true);
			}
			abrDlg.setzeLabel("Rechnungssatz erstellen für Rezept: "+kopf[2].split("=")[1]);
			String cmdKopf = "insert into faktura set ";
			for(int i = 0; i< positionen.size();i++){
				rechnungBuf.setLength(0);
				rechnungBuf.trimToSize();
				rechnungBuf.append(cmdKopf);
				if(i==0){
					rechnungBuf.append("kassen_nam='"+hmAnnahme.get("<gkv1>")+"', ");
					rechnungBuf.append("kassen_na2='"+hmAnnahme.get("<gkv2>")+"', ");
					rechnungBuf.append("strasse='"+hmAnnahme.get("<gkv3>")+"', ");
					try{
						rechnungBuf.append("plz='"+hmAnnahme.get("<gkv4>").split(" ")[0]+"', ");
						rechnungBuf.append("ort='"+hmAnnahme.get("<gkv4>").split(" ")[1]+"', ");
						rechnungBuf.append("name='"+kopf[9].split("=")[1]+"', ");
					}catch(Exception ex){
						JOptionPane.showMessageDialog(null, "Fehler in der Adressaufbereitung - Tabelle=Faktura");
						ex.printStackTrace();
					}
				}
				rechnungBuf.append("lfnr='"+Integer.toString(i)+"', ");
				rechnungBuf.append("status='"+ kopf[10].split("=")[1]+"', ");
				rechnungBuf.append("pos_kas='"+ positionen.get(i) +"', ");
				rechnungBuf.append("pos_int='"+ RezTools.getIDFromPos(positionen.get(i),kopf[0].split("=")[1] ,preisVector) +"', ");
				rechnungBuf.append("anzahl='"+ Integer.toString(anzahl.get(i).intValue()) +"', ");
				rechnungBuf.append("anzahltage='"+ Integer.toString(abrtage.get(i).intValue()) +"', ");
				rechnungBuf.append("preis='"+  dfx.format(einzelpreis.get(i).doubleValue()).replace(",", ".")  +"', ");
				rechnungBuf.append("gesamt='"+  dfx.format(preis.get(i).doubleValue()).replace(",", ".")  +"', ");
				rechnungBuf.append("zzbetrag='"+  dfx.format(rezgeb.get(i).doubleValue()).replace(",", ".")  +"', ");
				rechnungBuf.append("netto='"+  dfx.format((preis.get(i).subtract(rezgeb.get(i))).doubleValue()).replace(",", ".")  +"', ");
				rechnungBuf.append("pauschale='"+  kopf[6].split("=")[1].replace(",", ".") +"', ");
				rechnungBuf.append("rez_nr='"+   kopf[2].split("=")[1] +"', ");
				if(! anzahl.get(i).equals(abrtage.get(i))){
					rechnungBuf.append("kilometer='"+  dfx.format((anzahl.get(i).divide(abrtage.get(i)).doubleValue())).replace(",",".")  +"', ");
				}
				rechnungBuf.append("rezeptart='0', ");
				rechnungBuf.append("pat_intern='"+kopf[1].split("=")[1]+"', ");
				rechnungBuf.append("rnummer='"+  aktRechnung +"', ");
				rechnungBuf.append("kassid='"+kopf[7].split("=")[1]+"', ");
				rechnungBuf.append("arztid='"+kopf[8].split("=")[1]+"', ");
				rechnungBuf.append("zzindex='"+  kopf[12].split("=")[1] +"', ");
				rechnungBuf.append("preisdiff='"+  (preisUmstellung ? "T" : "F") +"', ");
				rechnungBuf.append("zuzahldiff='"+  (zuzahlUmstellung ? "T" : "F") +"', ");
				rechnungBuf.append("disziplin='"+  kopf[2].split("=")[1].subSequence(0, 2) +"', ");
				rechnungBuf.append("rdatum='"+  DatFunk.sDatInSQL(DatFunk.sHeute()) +"',");
				rechnungBuf.append("ik='"+  Reha.getAktIK() +"'");
				SqlInfo.sqlAusfuehren(rechnungBuf.toString());
			}

	}

	/***************************************************************/
	/*
	private String holeAbrechnungsKasse(String edifact){
		String[] komplett = edifact.split("\n");
		String[] zeile1 = komplett[0].split(":");
		return zeile1[7].split("=")[1];
	}
	*/
	/*************************************************/
	private void anhaengenEdifact(String string){
		String[] edi = string.split("\n");
		//String[] preise = edi[0].split(":");
		String status = "";
		try{
			status = edi[4].split("\\+")[2];
		}catch(Exception ex){
			status = "10001";
		}
		for(int i = 4; i < edi.length;i++){
			positionenBuf.append(edi[i]+System.getProperty("line.separator") );
			positionenAnzahl++;
		}
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(status.startsWith("1")){
			preis11 = addierePreise(preis11,edi[edi.length-1]);
		}else if(status.startsWith("3")){
			preis31 = addierePreise(preis31,edi[edi.length-1]);
		}else if(status.startsWith("5")){
			preis51 = addierePreise(preis51,edi[edi.length-1]);
		}
	}
	/*************************************************/
	private Double[] addierePreise(Double[] preis,String zeile){
		String[] zahlen = zeile.split("\\+");
		Double brutto = Double.parseDouble(zahlen[1].replace(",", "."));
		Double zuzahl  = Double.parseDouble(zahlen[2].replace(",", "."));
		preis[1] = preis[1]+ brutto;
		preis[2] = preis[2]+ zuzahl;
		preis[0] = preis[0] + (brutto-zuzahl);
		preis00[0] = preis00[0] + (brutto-zuzahl);
		preis00[1] = preis00[1] + (brutto);
		preis00[2] = preis00[2] + (zuzahl);
		return preis;
	}
	/*************************************************/
	public boolean isRezeptSelected(){
    	if(treeKasse.getSelectionCount()<=0){
    		return false;
    	}
    	TreePath path = treeKasse.getSelectionPath();
    	return	(path.getPathCount()>=3);
	}
	String getAktKTraeger(){
		TreePath path = treeKasse.getSelectionPath();
		JXTTreeNode node = (JXTTreeNode) path.getLastPathComponent();
		return ((KnotenObjekt)node.getUserObject()).ikkasse;
	}
	public void setRezeptOk(boolean ok){

    	treeKasse.getSelectionCount();
    	TreePath path = treeKasse.getSelectionPath();
    	JXTTreeNode node = (JXTTreeNode) path.getLastPathComponent();
    	((KnotenObjekt)node.getUserObject()).fertig = ok;
    	if(ok){
    		kontrollierteRezepte++;
    	}else{
    		kontrollierteRezepte--;
    	}
    	treeKasse.repaint();
	}
	public String getPreisgruppenKuerzel(String disziplin){		// nach 'Disziplinen' verschieben?
		if(disziplin.equals("Physio")){
			return "pgkg";
		}else if(disziplin.equals("Massage")){
			return "pgma";
		}else if(disziplin.equals("Ergo")){
			return "pger";
		}else if(disziplin.equals("Logo")){
			return "pglo";
		}else if(disziplin.equals("Reha")){
			return "pgrh";
		}else if(disziplin.equals("Podo")){
			return "pgpo";
		}else if(disziplin.equals("Rsport")){
			return "pgrs";
		}else if(disziplin.equals("Ftrain")){
			return "pgft";
		}else{
			return "pgkg";
		}
	}
	/***************************************/

	private static class JXTTreeNode extends DefaultMutableTreeNode {
    	/**
		 *
		 */
		private static final long serialVersionUID = 2195590211796817012L;

		public boolean enabled = false;
    	private KnotenObjekt knotenObjekt = null;
    	public JXTTreeNode(KnotenObjekt obj,boolean enabled){
    		super();
    		this.enabled = enabled;
   			this.knotenObjekt = obj;
   			if(obj != null){
   				this.setUserObject(obj);
   			}
    	}
    	/*
		public boolean isEnabled() {
			return enabled;
		}
		*/

		public KnotenObjekt getObject(){
			return knotenObjekt;
		}
    }

	/***************************************/
	class KnotenObjekt{
		public String titel;
		public boolean fertig;
		public String rez_num;
		public String ktraeger;
		public String pat_intern;
		public String entschluessel;
		public String ikkasse;
		public String preisgruppe;
		public String ohnepauschale;
		public boolean langfrist;
		public String langfristaz;
		private String ikpapier;
		
		public KnotenObjekt(String titel,String rez_num,boolean fertig,String ikkasse,String preisgruppe){
			this.titel = titel;
			this.fertig = fertig;
			this.rez_num = rez_num;
			this.ikkasse = ikkasse;
			this.preisgruppe = preisgruppe;
		}
		public void setIkPap (String ikpapier){
			this.ikpapier = ikpapier;
		}
		public String getIkPap (){
			return this.ikpapier;
		}
	}
	/*************************************/
	private class KassenTreeModel extends DefaultTreeModel {
		 /**
		 *
		 */
		private static final long serialVersionUID = 6391618556224740611L;
		public KassenTreeModel(JXTTreeNode node) {
	            super(node);
	        }

		public Object getValueAt(Object node, int column) {
			 JXTTreeNode jXnode = (JXTTreeNode) node;

	        	KnotenObjekt  o = null;
	        	o =  (KnotenObjekt) jXnode.getUserObject();
	        	switch (column) {
            	case 0:
            		return o.titel;
            	case 1:
            		return o.fertig;

	        	}
	        	return jXnode.getObject().titel;
		 }

		public int getColumnCount() {
	            return 3;
	        }

		public void setValueAt(Object value, Object node, int column){
	    	  JXTTreeNode jXnode = (JXTTreeNode) node;
	    	  KnotenObjekt  o;
	    	  o = jXnode.getObject();
	    	  switch (column) {
	            case 0:
					o.titel =((String) value) ;
					break;
	            case 1:
					o.fertig =((Boolean) value) ;
	            	break;
	    	  }
	      }

		public Class<?> getColumnClass(int column) {
	            switch (column) {
	            case 0:
	                return String.class;
	            case 1:
	                return Boolean.class;
	            }
	            return Object.class;
	      }
	}
	/*****************************************/
	private class KeepIkPap  {
		/**
		 * Vergleich, ob sich IK_Papier (bei Wechsel der Kasse) geändert hat
		 */
		String lastIkPap;

		public KeepIkPap(String ik) {
			lastIkPap = ik;
		}

		public boolean newIkPap(String ik) {
			if (lastIkPap.equals(ik)){
				return false;
			}else{
				lastIkPap = ik;
				return true;
			}
		}
	}

	/*****************************************/
	private class MyRenderer extends DefaultTreeCellRenderer {
		/**
		 *
		 */
		private static final long serialVersionUID = 2333990367290526356L;
		Icon fertigIcon;

		public MyRenderer(Icon icon) {
		fertigIcon = icon;
		}

		@Override
        public Component getTreeCellRendererComponent(
		JTree tree,
		Object value,
		boolean sel,
		boolean expanded,
		boolean leaf,
		int row,
		boolean hasFocus) {

		super.getTreeCellRendererComponent(
		tree, value, sel,
		expanded, leaf, row,
		hasFocus);
		KnotenObjekt o = ((JXTTreeNode)value).knotenObjekt;
		if (leaf && istFertig(value)) {
			setIcon(fertigIcon);
			this.setText(o.titel);
			setToolTipText("Verordnung "+o.rez_num+" kann dirket abgerechnet werden.");
		} else {
			setToolTipText(null);
			this.setText(o.titel);
		}
		if (!leaf){
			// set Icon according to (change of) ik_papier;
			if (customIconList.contains(o.ktraeger)){
				setIcon(getDisabledIcon());
			}else{
				setIcon(getIcon());
			}
		}
		return this;
		}

	}
	protected boolean istFertig(Object value) {
		DefaultMutableTreeNode node =
		(DefaultMutableTreeNode)value;
		KnotenObjekt fertig =
		(KnotenObjekt)(node.getUserObject());
		boolean istfertig = fertig.fertig;
		if(istfertig){
			return true;
		}
		return false;
	}
	private void rezepteUebertragen(){

	}
	private void rechnungAnlegen(){

	}
	@Override
	public void mouseClicked(MouseEvent e) {

	}
	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton()==3){					// Rechtsklick auf Rezept im Tree
			TreePath tp =  treeKasse.getSelectionPath();

			//kontrollierteRezepte = 0;
			if(tp==null){
				return;
			}
			JXTTreeNode node = (JXTTreeNode) tp.getLastPathComponent();
			String rez_nr = node.knotenObjekt.rez_num;
			if(!rez_nr.trim().equals("")){
				if(node.knotenObjekt.fertig){
					String msg = "<html>Achtung Sie editieren im Anschluß den EDIFACT-Code!<br>"+
							"Wenn Sie den Code in unzulässiger Weise manipulieren<br>wird <b>der gesamte Abrechnungslauf unbrauchbar</b><br><br>"+
							"<b>Rufen Sie diese Funktion nur dann auf wenn Sie genau wissen was Sie tun!!!</b><br><br>"+
							"Soll die Funktion jetzt aufgerufen werden?<br></html>";
					int frage = JOptionPane.showConfirmDialog(null,msg,"Achtung wichtige Benutzeranfrage",JOptionPane.YES_NO_OPTION);
					if(frage != JOptionPane.YES_OPTION){
						return;
					}
					PointerInfo info = MouseInfo.getPointerInfo();
		    	    Point location = info.getLocation();
					EditEdifact editEdifact = new EditEdifact(Reha.getThisFrame(),"EDIFACT - editieren",rez_nr.trim());
					editEdifact.getContentPane().setPreferredSize(new Dimension(600,500));
					editEdifact.setLocation(e.getXOnScreen()-50,e.getYOnScreen()-50);
					//editEdifact.setLocation(location.x-50,location.y-50);
					editEdifact.pack();
					editEdifact.setVisible(true);
					this.abrRez.setNewRez(rez_nr,node.knotenObjekt.fertig,aktDisziplin);

		    		//doKassenTreeAuswerten(aktuellerKnoten.knotenObjekt);

				}else{
					JOptionPane.showMessageDialog(null,"Abrechnungsdaten im Edifact-Format kann nur\nbei bereits markierten Rezepten manipuliert werden!");
				}

			}
		}

	}
	@Override
	public void mouseReleased(MouseEvent e) {

	}
	@Override
	public void mouseEntered(MouseEvent e) {

	}
	@Override
	public void mouseExited(MouseEvent e) {

	}

}
