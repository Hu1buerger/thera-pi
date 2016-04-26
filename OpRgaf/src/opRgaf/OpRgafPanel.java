package opRgaf;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.ButtonTools;
import CommonTools.DatFunk;
import CommonTools.DateTableCellEditor;
import CommonTools.DblCellEditor;
import CommonTools.DoubleTableCellRenderer;
import CommonTools.JCompTools;
import CommonTools.JRtaCheckBox;
import CommonTools.JRtaComboBox;
import CommonTools.JRtaTextField;
import CommonTools.MitteRenderer;
import CommonTools.RgAfVkSelect;
import CommonTools.RgAfVk_IfCallBack;
import CommonTools.SqlInfo;
import CommonTools.StringTools;
import RehaIO.SocketClient;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextFieldService;
import ag.ion.bion.officelayer.text.TextException;
import ag.ion.noa.NOAException;
import io.RehaIOMessages;

import com.jgoodies.forms.builder.PanelBuilder;

public class OpRgafPanel extends JXPanel implements TableModelListener, RgAfVk_IfCallBack {

	/**
	 *
	 */
	private static final long serialVersionUID = -7883557713071422132L;

	JRtaTextField suchen = null;
	JRtaTextField offen = null;
	JRtaTextField[] tfs = {null,null,null,null};
	JButton[] buts = {null,null,null};
	enum btIdx {ausbuchen,suchen,dummy};
	int btAusbuchen = btIdx.ausbuchen.ordinal();
	int btSuchen = btIdx.suchen.ordinal();
	JRtaComboBox combo = null;
	JXPanel content = null;
	KeyListener kl = null;
	ActionListener al = null;

	MyOpRgafTableModel tabmod = null;
	JXTable tab = null;
	JLabel summeOffen;
	JLabel summeRechnung;
	JLabel summeGesamtOffen;
	Component kopieButton;
	JRtaCheckBox bar = null;
//	JRtaCheckBox ChkRGR = null;
//	JRtaCheckBox ChkAFR = null;
//	JRtaCheckBox ChkVKR = null;
	
	JButton kopie;

	BigDecimal gesamtOffen = BigDecimal.valueOf(Double.parseDouble("0.00"));
	BigDecimal suchOffen = BigDecimal.valueOf(Double.parseDouble("0.00"));
	BigDecimal suchGesamt = BigDecimal.valueOf(Double.parseDouble("0.00"));

	DecimalFormat dcf = new DecimalFormat("###0.00");

	int ccount = -2;

	private HashMap<String,String> hmRezgeb = new HashMap<String,String>();
	final String stmtString = 
/*
		"select concat(t2.n_name, ', ',t2.v_name,', ',DATE_FORMAT(geboren,'%d.%m.%Y'))," +
		"t1.rnr,t1.rdatum,t1.rgesamt,t1.roffen,t1.rpbetrag,t1.rbezdatum,t1.rmahndat1,t1.rmahndat2,t3.kassen_nam1,t1.reznr,t1.id "+
		"from rgaffaktura as t1 inner join pat5 as t2 on (t1.pat_intern = t2.pat_intern) "+
		"left join kass_adr as t3 ON ( t2.kassenid = t3.id )";
*/
		"SELECT concat(t2.n_name, ', ',t2.v_name,', ',DATE_FORMAT(t2.geboren,'%d.%m.%Y')),t1.rnr,t1.rdatum,t1.rgesamt," +
		"t1.roffen,t1.rpbetrag,t1.rbezdatum,t1.rmahndat1,t1.rmahndat2,t3.kassen_nam1,t1.reznr,t1.id,t1.pat_id " +
		"FROM (SELECT v_nummer as rnr,v_datum as rdatum,v_betrag as rgesamt,v_offen as roffen,'' as rpbetrag," +
		"v_bezahldatum as rbezdatum,mahndat1 as rmahndat1,mahndat2 as rmahndat2,'' as reznr,verklisteid as id,pat_id as pat_id " +
		"FROM verkliste where v_nummer like 'VR-%' " +
		"UNION SELECT rnr,rdatum,rgesamt,roffen,rpbetrag,rbezdatum,rmahndat1,rmahndat2,reznr,id as id,pat_intern as pat_id " +
		"FROM rgaffaktura ) t1 LEFT JOIN pat5 AS t2 ON (t1.pat_id = t2.pat_intern) LEFT JOIN kass_adr AS t3 ON ( t2.kassenid = t3.id )";

	int gefunden;
	String[] spalten = {"Name,Vorname,Geburtstag","Rechn.Nr.","Rechn.Datum","Gesamtbetrag","Offen","Bearb.Gebühr","bezahlt am","1.Mahnung","2.Mahnung","Krankenkasse","RezeptNr.","id"};
	String[] colnamen ={"nix","rnr","rdatum","rgesamt","roffen","rpbetrag","rbezdatum","rmahndat1","rmahndat2","nix","nix","id"};
	OpRgafTab eltern = null;

	private RgAfVkSelect selPan;
	private RgAfVkGesamt sumPan;

	public OpRgafPanel(OpRgafTab xeltern){
		super();
		this.eltern = xeltern;
		startKeyListener();
		startActionListener();
		setLayout(new BorderLayout());
		add(getContent(),BorderLayout.CENTER);
		SwingUtilities.invokeLater(new Runnable(){
			@Override
            public void run(){
				setzeFocus();
			}
		});

	}
	public void setzeFocus(){
		SwingUtilities.invokeLater(new Runnable(){
			@Override
            public void run(){
				suchen.requestFocus();
			}
		});
	}

	private JPanel getContent(){
		FormLayout lay = new FormLayout(
		//        1     2     3    4     5     6 7    8       9     10    11   12    13   14    15   16    17   18    19
				 "10dlu,50dlu,2dlu,90dlu,10dlu,p,2dlu,50dlu:g,10dlu,50dlu,5dlu,50dlu,5dlu,50dlu,5dlu,50dlu,2dlu,50dlu,10dlu",	// xwerte,
		//        1     2 3     4        5    6 7     8    9 10   11
				 "15dlu,p,15dlu,160dlu:g,8dlu,p,10dlu,2dlu,p,8dlu,0dlu"															// ywerte
				);
		PanelBuilder builder = new PanelBuilder(lay);
		//PanelBuilder builder = new PanelBuilder(lay, new FormDebugPanel());		// debug mode
		builder.getPanel().setOpaque(false);
		CellConstraints cc = new CellConstraints();

		int colCnt=2, rowCnt=2;
		
		builder.addLabel("Suchkriterium", cc.xy(colCnt++, rowCnt));			// 2,2

		String[] args = {"Rechnungsnummer =","Rechnungsnummer enthält",
				"Rechnungsbetrag =","Rechnungsbetrag >=","Rechnungsbetrag <=",
				"Noch offen =","Noch offen >=","Noch offen <=",
				"Pat. Nachname beginnt mit",
				"Rezeptnummer =",
				"Rechnungsdatum =","Rechnungsdatum >=","Rechnungsdatum <=",
				"Krankenkasse enthält"};

		int vorauswahl =  OpRgaf.iniOpRgAf.getVorauswahl(args.length);
		combo = new JRtaComboBox(args);
		combo.setSelectedIndex( vorauswahl ); 
		builder.add(combo, cc.xy(++colCnt,rowCnt));							// 4,2

		++colCnt;
		builder.addLabel("finde:", cc.xy(++colCnt, rowCnt));				// 6,2
		
		++colCnt;
		suchen = new JRtaTextField("nix",true);
		suchen.setName("suchen");
		suchen.addKeyListener(kl);
		builder.add(suchen,cc.xy(++colCnt, rowCnt,CellConstraints.FILL,CellConstraints.DEFAULT));	// 8,2
		
		// Auswahl RGR/AFR/Verkauf
		colCnt += 2;
		selPan = new RgAfVkSelect("suche in  ");							// Subpanel mit Checkboxen anlegen
		//selPan.ask("Tabellen:");
		selPan.setCallBackObj(this);										// callBack registrieren
		initSelection();
		
		builder.add(selPan.getPanel(),cc.xywh(++colCnt, rowCnt-1,5,3,CellConstraints.LEFT,CellConstraints.DEFAULT));	//10..15,1..3
		// Ende Auswahl

		buts[btSuchen] = ButtonTools.macheButton("suchen", "suchen", al);
		buts[btSuchen].setMnemonic('s');
		builder.add(buts[btSuchen],cc.xy(18,rowCnt));

//**********************
		while(!OpRgaf.DbOk){

		}
		tabmod = new MyOpRgafTableModel();
		/*
		Vector<Vector<String>> felder = SqlInfo.holeFelder("describe rgaffaktura");
		String[] spalten = new String[felder.size()];
		for(int i= 0; i < felder.size();i++){
			spalten[i] = felder.get(i).get(0);
		}
		*/

		tabmod.setColumnIdentifiers(spalten);
		tab = new JXTable(tabmod);
		tab.setHorizontalScrollEnabled(true);


		//tab.getColumn(1).setCellEditor();
		DateTableCellEditor tble = new DateTableCellEditor();
		tab.getColumn(1).setCellRenderer(new MitteRenderer());

		tab.getColumn(2).setCellEditor(tble);

		tab.getColumn(3).setCellRenderer(new DoubleTableCellRenderer());
		tab.getColumn(3).setCellEditor(new DblCellEditor());

		tab.getColumn(4).setCellRenderer(new DoubleTableCellRenderer());
		tab.getColumn(4).setCellEditor(new DblCellEditor());

		tab.getColumn(5).setCellRenderer(new DoubleTableCellRenderer());
		tab.getColumn(5).setCellEditor(new DblCellEditor());

		tab.getColumn(6).setCellEditor(tble);
		tab.getColumn(7).setCellEditor(tble);
		tab.getColumn(8).setCellEditor(tble);
		tab.getColumn(10).setMinWidth(80);
		tab.getColumn(11).setMaxWidth(50);
		tab.getSelectionModel().addListSelectionListener( new OPListSelectionHandler());
		tab.setHighlighters(HighlighterFactory.createSimpleStriping(HighlighterFactory.CLASSIC_LINE_PRINTER));



		JScrollPane jscr = JCompTools.getTransparentScrollPane(tab);
		rowCnt +=2;
		builder.add(jscr,cc.xyw(2,rowCnt,17));		// 2,4
//**********************

		rowCnt +=2;									// 6
		colCnt = 4;
		kopieButton = builder.add(ButtonTools.macheButton("Rechnungskopie", "kopie", al),cc.xy(colCnt,rowCnt));		// 4,6
		colCnt = 12;
		builder.addLabel("Geldeingang:", cc.xy(colCnt, rowCnt,CellConstraints.RIGHT,CellConstraints.TOP));										// 12,6

		++colCnt;
		tfs[0] = new JRtaTextField("F",true,"6.2","");
		tfs[0].setHorizontalAlignment(SwingConstants.RIGHT);
		tfs[0].setText("0,00");
		tfs[0].setName("offen");
		tfs[0].addKeyListener(kl);
		builder.add(tfs[0],cc.xy(++colCnt,rowCnt));														// 14,6

		++colCnt;
		bar = (JRtaCheckBox) builder.add(new JRtaCheckBox("bar in Kasse"), cc.xy(++colCnt,rowCnt));
		if(OpRgaf.iniOpRgAf.getWohinBuchen().equals("Kasse")){
			bar.setSelected(true);			
		}

		
		buts[btAusbuchen] = (JButton) builder.add(ButtonTools.macheButton("ausbuchen", "ausbuchen", al),cc.xy(18,6));
//**********************

		rowCnt +=2;
		colCnt = 1;
		builder.add(new JSeparator(SwingConstants.HORIZONTAL), cc.xyw(colCnt,rowCnt++,19));

		sumPan = new RgAfVkGesamt();
		builder.add(sumPan.getPanel(),cc.xyw(colCnt, rowCnt,2,CellConstraints.LEFT,CellConstraints.TOP));	//2,2

		calcGesamtOffen();
		
		return builder.getPanel();
	}
	/**
	 * letzte Checkbox-Auswahl wiederherstellen
	 */
	public void initSelection() {
		selPan.setRGR(OpRgaf.iniOpRgAf.getIncRG());	
		selPan.setAFR(OpRgaf.iniOpRgAf.getIncAR());
		selPan.setVKR(OpRgaf.iniOpRgAf.getIncVK());
		if (!selPan.useRGR() && !selPan.useAFR() && !selPan.useVKR()){
			selPan.setRGR(Boolean.TRUE);	// einer sollte immer ausgewählt sein 
		}
	}
	
	private void calcGesamtOffen() {
		new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				sumPan.ermittleGesamtOffen(selPan.useRGR(), selPan.useAFR(), selPan.useVKR());
				return null;
			}

		}.execute();
	}
	
	private OpRgafPanel getInstance(){
		return this;
	}

	private void startKeyListener(){
		kl = new KeyListener(){
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode()==10){
					arg0.consume();
					if( ((JComponent)arg0.getSource()).getName().equals("suchen")){
						sucheEinleiten();
						return;
					}else if( ((JComponent)arg0.getSource()).getName().equals("offen") ){
						setzeFocus();
					}
				}

			}
			@Override
			public void keyReleased(KeyEvent arg0) {


			}
			@Override
			public void keyTyped(KeyEvent arg0) {


			}

		};
	}
	private void startActionListener(){
		al = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String cmd = arg0.getActionCommand();
				if(cmd.equals("ausbuchen")){
					tabmod.removeTableModelListener(getInstance());
					doAusbuchen();
					tabmod.addTableModelListener(getInstance());
					setzeFocus();
					return;
				}
				if(cmd.equals("kopie")){
					doKopie();
					setzeFocus();
					return;
				}
				if(cmd.equals("suchen")){
					sucheEinleiten();
					return;
				}

			}
		};
	}
	private void doKopie(){
		if(tabmod.getRowCount() <= 0){
			return;
		}
		final String rnr = tab.getValueAt(tab.getSelectedRow(), 1).toString();
		if(rnr.startsWith("AFR")){

				new SwingWorker<Void,Void>(){
					@Override
					protected Void doInBackground() throws Exception {

						try{
						//System.out.println("in Ausfallrechnung");
						//(Point pt, String pat_intern,String rez_nr,String rnummer,String rdatum){
						String id = tab.getValueAt(tab.getSelectedRow(), 11).toString();
						String rez_nr = SqlInfo.holeEinzelFeld("select reznr from rgaffaktura where id='"+id+"' LIMIT 1");
						String pat_intern = SqlInfo.holeEinzelFeld("select pat_intern from rgaffaktura where id='"+id+"' LIMIT 1");
						String rdatum = SqlInfo.holeEinzelFeld("select rdatum from rgaffaktura where id='"+id+"' LIMIT 1");
						AusfallRechnung ausfall = new AusfallRechnung(kopieButton.getLocationOnScreen(),pat_intern,
								rez_nr,rnr,rdatum);
						ausfall.setModal(true);
						ausfall.setLocationRelativeTo(null);
						ausfall.toFront();
						ausfall.setVisible(true);
						ausfall = null;
						}catch(Exception ex){
							ex.printStackTrace();
						}
						return null;
					}
				}.execute();
				return;
		}
		if(rnr.startsWith("RGR")){
			doRezeptgebKopie();
		}
	}
	private void setzeBezahlBetrag(final int i){
		tfs[0].setText(dcf.format(tabmod.getValueAt(tab.convertRowIndexToModel(i), 4)));
	}

	private void sucheEinleiten(){
		new SwingWorker<Void,Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				try{
					OpRgaf.thisFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
					setzeFocus();
					tabmod.removeTableModelListener(getInstance());
					doSuchen();
					schreibeAbfrage();
					tabmod.addTableModelListener(getInstance());
					suchen.setEnabled(true);
					buts[btAusbuchen].setEnabled(true);

				}catch(Exception ex){
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null,"Fehler beim Einlesen der Datensätze");
					OpRgaf.thisFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					setzeFocus();
					suchen.setEnabled(true);
					buts[btAusbuchen].setEnabled(true);
				}
				OpRgaf.thisFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				setzeFocus();
				return null;
			}
		}.execute();
	}
	private void doAusbuchen(){
		int row = tab.getSelectedRow();
		if(row < 0){
			JOptionPane.showMessageDialog(null, "Keine Rechnung zum Ausbuchen ausgewählt");
			return;
		}
		BigDecimal nochoffen = BigDecimal.valueOf((Double)tabmod.getValueAt(tab.convertRowIndexToModel(row), 4));
		BigDecimal eingang = BigDecimal.valueOf(Double.parseDouble(tfs[0].getText().replace(",", ".")) );
		BigDecimal restbetrag = nochoffen.subtract(eingang);

		if(row < 0){
			JOptionPane.showMessageDialog(null, "Keine Rechnung zum Ausbuchen ausgewählt");
			return;
		}
		if(nochoffen.equals(BigDecimal.valueOf(Double.parseDouble("0.0")))){
			JOptionPane.showMessageDialog(null,"Diese Rechnung ist bereits auf bezahlt gesetzt");
			return;
		}

		suchOffen = suchOffen.subtract(eingang );
		gesamtOffen = gesamtOffen.subtract(eingang);

		String cmd = "";
		//einen größeren Schwachsinn für den praktische Einsatz kann man sich schwerlich vorstellen.
		//Der logische Wert muß über die CheckBox bar.isSelected() bestimmt werden alles andere ist völlig Scheiße
		//ich habe mich dafür bereits mehrfach(!!) in den Arsch gebissen
		//if(OpRgaf.mahnParameter.get("inkasse").equals("Kasse")){
		String rgaf_reznum = tabmod.getValueAt(tab.convertRowIndexToModel(row), 10).toString();
		String rgaf_rechnum = tabmod.getValueAt(tab.convertRowIndexToModel(row), 1).toString();
		if(bar.isSelected()){
			//BigDecimal einnahme = BigDecimal.valueOf((Double)tabmod.getValueAt(tab.convertRowIndexToModel(row), 4));
			//einnahme = einnahme.subtract(BigDecimal.valueOf(Double.parseDouble(tfs[0].getText().replace(",", ".")) ));
			cmd = "insert into kasse set einnahme='"+dcf.format(eingang).replace(",", ".")+"', datum='"+
			DatFunk.sDatInSQL(DatFunk.sHeute())+"', ktext='"+
			rgaf_rechnum+","+
			tabmod.getValueAt(tab.convertRowIndexToModel(row), 0)+"',"+
			"rez_nr='"+rgaf_reznum+"'";
			//System.out.println(cmd);
			SqlInfo.sqlAusfuehren(cmd);
		}
		tabmod.setValueAt(new Date(), tab.convertRowIndexToModel(row), 6);
		tabmod.setValueAt(restbetrag.doubleValue(), tab.convertRowIndexToModel(row), 4);

		if(rgaf_rechnum.startsWith("RGR-")){										// Rezept bezahlt setzen
			SqlInfo.sqlAusfuehren("update verordn set zzstatus='1', rez_bez='T' where rez_nr = '"+rgaf_reznum+"' LIMIT 1");	// zz: 1-ok
			SqlInfo.sqlAusfuehren("update lza set zzstatus='1', rez_bez='T' where rez_nr = '"+rgaf_reznum+"' LIMIT 1");
		}


		int id = (Integer) tabmod.getValueAt(tab.convertRowIndexToModel(row), 11);
		cmd = "update rgaffaktura set roffen='"+dcf.format(restbetrag).replace(",", ".")+"', rbezdatum='"+
		DatFunk.sDatInSQL(DatFunk.sHeute())+"' where id ='"+Integer.toString(id)+"' LIMIT 1";
		/*
		if(!OpRgaf.testcase){
			SqlInfo.sqlAusfuehren(cmd);
		}
		*/
		//hier rein die Kasseneinnahme
		SqlInfo.sqlAusfuehren(cmd);
		schreibeAbfrage();
		tfs[0].setText("0,00");
	}

//	private void ermittleGesamtOffen(){  ==> RgAfVkGesamt

	private void schreibeAbfrage(){
		sumPan.schreibeGesamtOffen();
		sumPan.setSuchOffen(suchOffen);
		sumPan.schreibeSuchOffen();
		sumPan.setSuchGesamt(suchGesamt);
		sumPan.schreibeSuchGesamt();
		sumPan.setAnzRec(gefunden);
		sumPan.schreibeAnzRec();
	}

	public void sucheRezept(String rezept){		// <- nutzt das jemand?
		suchen.setText(rezept);
		//combo.setSelectedIndex(8);
		combo.setSelectedItem("Rezeptnummer =");
		doSuchen();
	}

	private void doSuchen(){
		if(suchen.getText().trim().equals("")){

			return;
		}
		int suchart = combo.getSelectedIndex();
		OpRgaf.iniOpRgAf.setVorauswahl(suchart);		// Auswahl merken
		//String suchVal = combo.getItemAt(combo.getSelectedIndex()).toString();
		//System.out.println("OpRgafPanel-doSuchen-suche: " +'"' + suchVal + '"' + "(" + suchart + ")");  // s. String[] args
		String cmd = "";
		String tmpStr = selPan.bills2search("rnr");
		String whereToSearch = " WHERE ";
		if(tmpStr.length() > 0){
			whereToSearch = whereToSearch + " ( " + tmpStr + " ) AND ";
		}

		try{
//			switch(suchVal){		// <- funktioniert erst ab Java 1.7
			switch(suchart){
			case 0:
				cmd = stmtString+" where rnr ='"+suchen.getText().trim()+"'";
				break;
			case 1:					// Rechnungsnummer enthält
				cmd = stmtString+ whereToSearch + " rnr like'%"+suchen.getText().trim()+"%' order by t1.id";
				break;
			case 2:					// Rechnungsbetrag =
				cmd = stmtString+ whereToSearch + " rgesamt ='"+suchen.getText().trim().replace(",", ".")+"' order by t1.id";
				break;
			case 3:					//  >=
				cmd = stmtString+ whereToSearch + " rgesamt >='"+suchen.getText().trim().replace(",", ".")+"' order by t1.id";
				break;
			case 4:					//  <=
				cmd = stmtString+ whereToSearch + " rgesamt <='"+suchen.getText().trim().replace(",", ".")+"' order by t1.id";
				break;
			case 5:					// Noch offen =
				cmd = stmtString+ whereToSearch + " roffen ='"+suchen.getText().trim().replace(",", ".")+"' order by t1.id";
				break;
			case 6:					//  >=
				cmd = stmtString+ whereToSearch + " t1.roffen >='"+suchen.getText().trim().replace(",", ".")+"' order by t1.id";
				break;
			case 7:					//  <=
				cmd = stmtString+ whereToSearch + " roffen <='"+suchen.getText().trim().replace(",", ".")+"' order by t1.id";
				break;
			case 8:					// Nachname beginnt mit
				cmd = stmtString+ whereToSearch + " t2.n_name like'"+suchen.getText().trim()+"%' order by t1.id";
				break;
			case 9:					// Rezeptnummer =
				cmd = stmtString+ whereToSearch + " t1.reznr ='"+suchen.getText().trim()+"'";
				break;
			case 10:				// Rechnungsdatum =
				cmd = stmtString+ whereToSearch + " rdatum ='"+DatFunk.sDatInSQL(suchen.getText().trim())+"'";
				break;
			case 11:				//  >=
				cmd = stmtString+ whereToSearch + " rdatum >='"+DatFunk.sDatInSQL(suchen.getText().trim())+"'";
				break;
			case 12:				//  <=
				cmd = stmtString+ whereToSearch + " rdatum <='"+DatFunk.sDatInSQL(suchen.getText().trim())+"'";
				break;
			case 13:				// Krankenkasse enthält
				cmd = stmtString+ whereToSearch + " t3.kassen_nam1 like'%"+suchen.getText().trim()+"%'";
				break;
			}
		}catch(Exception ex){
			//ex.printStackTrace();
		}

		if(!cmd.equals("")){
			buts[btAusbuchen].setEnabled(false);
			suchen.setEnabled(false);
			try{
			starteSuche(cmd);
			}catch(Exception ex){
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, "Korrekte Auflistung des Suchergebnisses fehlgeschlagen");
			}
			OpRgaf.thisFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

			suchen.setEnabled(true);
			buts[btAusbuchen].setEnabled(true);
			setzeFocus();
		}

	}


	class MyOpRgafTableModel extends DefaultTableModel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
        public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex){
			case 0:
			case 1:
			case 9:
			case 10:
				return String.class;
			case 2:
			case 6:
			case 7:
			case 8:
				return Date.class;
			case 3:
			case 4:
			case 5:
				return Double.class;
			case 11:
				return Integer.class;
			}
		   return String.class;
	    }

		@Override
        public boolean isCellEditable(int row, int col) {

			if(col > 1 && col < 9){
				return true;
			}
			return false;
		}

	}

	private void starteSuche(String sstmt){
		tabmod.setRowCount(0);
		tab.validate();
		//tab.repaint();
		Statement stmt = null;
		ResultSet rs = null;


		try {
			stmt =  OpRgaf.thisClass.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
			            ResultSet.CONCUR_UPDATABLE );
		} catch (SQLException e) {

			e.printStackTrace();
		}
		try{
			
			rs = stmt.executeQuery(sstmt);
			Vector<Object> vec = new Vector<Object>();
			int durchlauf = 0;
			sumPan.delSuchGesamt();
			sumPan.delSuchOffen();
			sumPan.delAnzRec();
			suchGesamt = sumPan.getSuchGesamt();
			suchOffen = sumPan.getSuchOffen();
			gefunden = sumPan.getAnzRec();
			ResultSetMetaData rsMetaData = null;
			while(rs.next()){
				vec.clear();
				rsMetaData = rs.getMetaData() ;
				int numberOfColumns = rsMetaData.getColumnCount()+1;
				 for(int i = 1 ; i < numberOfColumns;i++){
					 if(rsMetaData.getColumnClassName(i).toString().equals("java.lang.String")){
						 vec.add( (rs.getString(i)==null ? "" : rs.getString(i)) );
					 }else if(rsMetaData.getColumnClassName(i).toString().equals("java.math.BigDecimal")){
						 vec.add( rs.getBigDecimal(i).doubleValue() );
					 }else if(rsMetaData.getColumnClassName(i).toString().equals("java.sql.Date")){
						 vec.add( rs.getDate(i) );
					 }else if(rsMetaData.getColumnClassName(i).toString().equals("java.lang.Integer")){
						 vec.add( rs.getInt(i) );
					 }
					 //vec.add( (rs.getString(i)==null ? "" : rs.getString(i)) );//r_klasse
					 //System.out.println(rsMetaData.getColumnClassName(i));
				 }

				suchOffen = suchOffen.add(rs.getBigDecimal(5));
				suchGesamt = suchGesamt.add(rs.getBigDecimal(4));
				tabmod.addRow( (Vector<?>) vec.clone());
				if(durchlauf>200){
					try {
						tab.validate();
						tab.repaint();
						Thread.sleep(100);
						durchlauf = 0;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				durchlauf++;
				gefunden++;
			}

			tab.validate();
			tab.repaint();
			if(tab.getRowCount() > 0){
				tab.setRowSelectionInterval(0, 0);
			}

		}catch(SQLException ev){
			System.out.println("SQLException: " + ev.getMessage());
			System.out.println("SQLState: " + ev.getSQLState());
			System.out.println("VendorError: " + ev.getErrorCode());
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
	}
	/*****************************************************/
	class OPListSelectionHandler implements ListSelectionListener {

	    @Override
        public void valueChanged(ListSelectionEvent e) {
	        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
	        boolean isAdjusting = e.getValueIsAdjusting();
	        if(isAdjusting){
	        	return;
	        }
	        if (lsm.isSelectionEmpty()) {

	        } else {
	            int minIndex = lsm.getMinSelectionIndex();
	            int maxIndex = lsm.getMaxSelectionIndex();
	            for (int i = minIndex; i <= maxIndex; i++) {
	                if (lsm.isSelectedIndex(i)) {
	                	setzeBezahlBetrag(i);
	                	String id = tab.getValueAt(i, 11).toString();
						String rez_nr = SqlInfo.holeEinzelFeld("select reznr from rgaffaktura where id='"+id+"' LIMIT 1");
						String pat_intern = SqlInfo.holeEinzelFeld("select pat_intern from rgaffaktura where id='"+id+"' LIMIT 1");
						new SocketClient().setzeRehaNachricht(OpRgaf.rehaReversePort,"OpRgaf#"+
								RehaIOMessages.MUST_PATANDREZFIND+"#"+pat_intern+"#"+rez_nr);
	                	//System.out.println("Satz "+i);

	                    break;
	                }
	            }
	        }

	    }
	}


	@Override
	public void tableChanged(TableModelEvent arg0) {
		if(arg0.getType() == TableModelEvent.INSERT){
			System.out.println("Insert");
			return;
		}
		if(arg0.getType() == TableModelEvent.UPDATE){
			try{
				int col = arg0.getColumn();
				int row = arg0.getFirstRow();
				String colname = colnamen[col].toString();
				String value = "";
				String id = Integer.toString((Integer)tabmod.getValueAt(row,11));
				if( tabmod.getColumnClass(col) == Boolean.class){
					value = (tabmod.getValueAt(row,col) == Boolean.FALSE ? "F" : "T");
				}else if(tabmod.getColumnClass(col) == Date.class){
					if(tabmod.getValueAt(row,col)==null ){
						value =  "1900-01-01";
					}else{
						String test = tabmod.getValueAt(row,col).toString();
						if(test.contains(".")){
							value = DatFunk.sDatInSQL(test);
							if(value.equals("    -  -  ")){
								value = null;
							}
						}else{
							if(test.equals("    -  -  ")){
								value = null;
							}else{
								value = test;
							}

						}
					}
				}else if(tabmod.getColumnClass(col) == Double.class){
					value = dcf.format(tabmod.getValueAt(row,col)).replace(",",".");
				}else if(tabmod.getColumnClass(col) == String.class){
					value = tabmod.getValueAt(row,col).toString();
				}
				String cmd = "update rgaffaktura set "+colname+"="+(value != null ? "'"+value+"'" : "null")+" where id='"+id+"' LIMIT 1";
				//System.out.println(cmd);
				SqlInfo.sqlAusfuehren(cmd);
				tfs[0].setText(dcf.format(tabmod.getValueAt(tab.convertRowIndexToModel(row), 4)));

			}catch(Exception ex){
				System.out.println(ex);
				JOptionPane.showMessageDialog(null,"Fehler in der Dateneingbe");
			}
			return;
		}
	}

	private void doRezeptgebKopie(){
		if(tabmod.getRowCount() <= 0){return;}
		String db = "";
		String id = tab.getValueAt(tab.getSelectedRow(), 11).toString();
		String rgnr = tab.getValueAt(tab.getSelectedRow(), 1).toString();
		String rez_nr = SqlInfo.holeEinzelFeld("select reznr from rgaffaktura where id='"+id+"' LIMIT 1");
		String pat_intern = SqlInfo.holeEinzelFeld("select pat_intern from rgaffaktura where id='"+id+"' LIMIT 1");
		String rdatum = SqlInfo.holeEinzelFeld("select rdatum from rgaffaktura where id='"+id+"' LIMIT 1");
		String rezgeb = SqlInfo.holeEinzelFeld("select rgbetrag from rgaffaktura where id='"+id+"' LIMIT 1");
		String pauschale = SqlInfo.holeEinzelFeld("select rpbetrag from rgaffaktura where id='"+id+"' LIMIT 1");
		String gesamt = SqlInfo.holeEinzelFeld("select rgesamt from rgaffaktura where id='"+id+"' LIMIT 1");
		System.out.println("Rezeptnummer = "+rez_nr);
		new InitHashMaps();
		/*
		Vector<String> patDaten = SqlInfo.holeSatz("pat5", " * ", "pat_intern='"+pat_intern+"'", Arrays.asList(new String[] {}));
		InitHashMaps.constructPatHMap(patDaten);
		*/
		String test = SqlInfo.holeEinzelFeld("select id from verordn where rez_nr = '"+rez_nr+"' LIMIT 1");
		Vector<String> vecaktrez = null;
		if(test.equals("")){
			test = SqlInfo.holeEinzelFeld("select id from lza where rez_nr = '"+rez_nr+"' LIMIT 1");
			if(test.equals("")){
				//this.dispose();
				//return;
			}else{
				vecaktrez = SqlInfo.holeSatz("lza", " anzahl1,kuerzel1,kuerzel2,"+
						"kuerzel3,kuerzel4,kuerzel5,kuerzel6 ", "id='"+test+"'", Arrays.asList(new String[] {}));
				db = "lza";
			}
		}else{
			vecaktrez = SqlInfo.holeSatz("verordn", " anzahl1,kuerzel1,kuerzel2,"+
					"kuerzel3,kuerzel4,kuerzel5,kuerzel6 ", "id='"+test+"'", Arrays.asList(new String[] {}));
			db = "verordn";

		}
		String behandlungen = vecaktrez.get(0)+"*"+
			(! vecaktrez.get(1).trim().equals("") ? "" +vecaktrez.get(1) : "") +
			(! vecaktrez.get(2).trim().equals("") ? "," +vecaktrez.get(2) : "") +
			(! vecaktrez.get(3).trim().equals("") ? "," +vecaktrez.get(3) : "") +
			(! vecaktrez.get(4).trim().equals("") ? "," +vecaktrez.get(4) : "") +
			(! vecaktrez.get(5).trim().equals("") ? "," +vecaktrez.get(5) : "") +
			(! vecaktrez.get(6).trim().equals("") ? "," +vecaktrez.get(6) : "");

		String cmd = "select abwadress,id from pat5 where pat_intern='"+pat_intern+"' LIMIT 1";
		Vector<Vector<String>> adrvec = SqlInfo.holeFelder(cmd);
		String[] adressParams = null;
		if(adrvec.get(0).get(0).equals("T")){
			adressParams = holeAbweichendeAdresse(adrvec.get(0).get(1));
		}else{
			adressParams = getAdressParams(adrvec.get(0).get(1));
		}

		hmRezgeb.put("<rgreznum>",rez_nr);
		hmRezgeb.put("<rgbehandlung>",behandlungen);
		hmRezgeb.put("<rgdatum>",DatFunk.sDatInDeutsch(
				SqlInfo.holeEinzelFeld("select rez_datum from "+db+" where rez_nr='"+rez_nr+"' LIMIT 1") ));
		hmRezgeb.put("<rgbetrag>",rezgeb.replace(".", ","));
		hmRezgeb.put("<rgpauschale>",pauschale.replace(".", ","));
		hmRezgeb.put("<rggesamt>",gesamt.replace(".", ","));
		hmRezgeb.put("<rganrede>",adressParams[0]);
		hmRezgeb.put("<rgname>",adressParams[1]);
		hmRezgeb.put("<rgstrasse>",adressParams[2]);
		hmRezgeb.put("<rgort>",adressParams[3]);
		hmRezgeb.put("<rgbanrede>",adressParams[4]);
		hmRezgeb.put("<rgorigdatum>", DatFunk.sDatInDeutsch(rdatum));
		hmRezgeb.put("<rgnr>", rgnr);

		hmRezgeb.put("<rgpatnname>", StringTools.EGross(SqlInfo.holeEinzelFeld("select n_name from pat5 where pat_intern='"+pat_intern+"' LIMIT 1") ));
		hmRezgeb.put("<rgpatvname>", StringTools.EGross(SqlInfo.holeEinzelFeld("select v_name from pat5 where pat_intern='"+pat_intern+"' LIMIT 1") ));
		hmRezgeb.put("<rgpatgeboren>", DatFunk.sDatInDeutsch(SqlInfo.holeEinzelFeld("select geboren from pat5 where pat_intern='"+pat_intern+"' LIMIT 1")) );

		//System.out.println(hmRezgeb);
		String url = OpRgaf.progHome+"vorlagen/"+OpRgaf.aktIK+"/RezeptgebuehrRechnung.ott.Kopie.ott";
		try {
			officeStarten(url);
		} catch (OfficeApplicationException e) {
			e.printStackTrace();
		} catch (NOAException e) {
			e.printStackTrace();
		} catch (TextException e) {
			e.printStackTrace();
		}

	}

	public String[] getAdressParams(String patid){
		//anr=17,titel=18,nname=0,vname=1,strasse=3,plz=4,ort=5,abwadress=19
		//"anrede,titel,nachname,vorname,strasse,plz,ort"
		String cmd = "select anrede,titel,n_name,v_name,strasse,plz,ort from pat5 where id='"+
		patid+"' LIMIT 1";
		Vector<Vector<String>> abwvec = SqlInfo.holeFelder(cmd);
		Object[] obj = { abwvec.get(0).get(0),abwvec.get(0).get(1),abwvec.get(0).get(2),
			abwvec.get(0).get(3),abwvec.get(0).get(4),abwvec.get(0).get(5),
			abwvec.get(0).get(6)
			};
		return AdressTools.machePrivatAdresse(obj,true);
	}

	public String[] holeAbweichendeAdresse(String patid){
		//"anrede,titel,nachname,vorname,strasse,plz,ort"
		String cmd = "select abwanrede,abwtitel,abwn_name,abwv_name,abwstrasse,abwplz,abwort from pat5 where id='"+
			patid+"' LIMIT 1";
		Vector<Vector<String>> abwvec = SqlInfo.holeFelder(cmd);
		Object[] obj = { abwvec.get(0).get(0),abwvec.get(0).get(1),abwvec.get(0).get(2),
				abwvec.get(0).get(3),abwvec.get(0).get(4),abwvec.get(0).get(5),
				abwvec.get(0).get(6)
				};
		return AdressTools.machePrivatAdresse(obj,true);
	}

	private void officeStarten(String url) throws OfficeApplicationException, NOAException, TextException{
		IDocumentService documentService = null;
		OpRgaf.thisFrame.setCursor(OpRgaf.thisClass.wartenCursor);
		////System.out.println("Starte Datei -> "+url);
		if(!OpRgaf.officeapplication.isActive()){
			OpRgaf.starteOfficeApplication();
		}

		documentService = OpRgaf.officeapplication.getDocumentService();

        IDocumentDescriptor docdescript = new DocumentDescriptor();
       	docdescript.setHidden(true);
        docdescript.setAsTemplate(true);
		IDocument document = null;

		document = documentService.loadDocument(url,docdescript);
		ITextDocument textDocument = (ITextDocument)document;
		/**********************/
		//OOTools.druckerSetzen(textDocument, SystemConfig.hmAbrechnung.get("hmgkvrechnungdrucker"));
		/**********************/
		ITextFieldService textFieldService = textDocument.getTextFieldService();
		ITextField[] placeholders = null;

		placeholders = textFieldService.getPlaceholderFields();
		String placeholderDisplayText = "";

		for (int i = 0; i < placeholders.length; i++) {
			placeholderDisplayText = placeholders[i].getDisplayText().toLowerCase();
			Set<?> entries = hmRezgeb.entrySet();
		    Iterator<?> it = entries.iterator();
			    while (it.hasNext()) {

				Map.Entry entry = (Map.Entry) it.next();
			      if(((String)entry.getKey()).toLowerCase().equals(placeholderDisplayText)){
			    	  try{

			    	  }catch(RuntimeException ex){
			    		  //System.out.println("Fehler bei "+placeholderDisplayText);
			    	  }
			    	  placeholders[i].getTextRange().setText(((String)entry.getValue()));

			    	  break;
			      }
			    }
		}
		//if(SystemConfig.hmAbrechnung.get("hmallinoffice").equals("1")){
			textDocument.getFrame().getXFrame().getContainerWindow().setVisible(true);
		/*
		}else{
			PrintProperties printprop = new PrintProperties ((short) 2 ,null);
			textDocument.getPrintService().print(printprop);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			textDocument.close();
			textDocument = null;
		}
		*/

			OpRgaf.thisFrame.setCursor(OpRgaf.thisClass.normalCursor);
	}
	@Override
	public void useRGR(boolean rgr) {
		OpRgaf.iniOpRgAf.setIncRG(rgr);
		calcGesamtOffen();
	}
	@Override
	public void useAFR(boolean afr) {
		OpRgaf.iniOpRgAf.setIncAR(afr);
		calcGesamtOffen();
	}
	@Override
	public void useVKR(boolean vkr) {
		OpRgaf.iniOpRgAf.setIncVK(vkr);
		calcGesamtOffen();
	}

}
