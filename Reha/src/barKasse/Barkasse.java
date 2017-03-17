package barKasse;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.DatFunk;
import CommonTools.INIFile;
import CommonTools.INITool;
import CommonTools.JRtaTextField;
import CommonTools.SqlInfo;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.DocumentException;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextTable;
import ag.ion.bion.officelayer.text.TextException;
import ag.ion.noa.NOAException;
import environment.Path;
import hauptFenster.Reha;
import rehaInternalFrame.JBarkassenInternal;
import ag.ion.noa.internal.printing.PrintProperties;
import ag.ion.noa.printing.IPrinter;

import com.jgoodies.forms.debug.FormDebugPanel;

import CommonTools.OOTools;
import systemTools.ButtonTools;

public class Barkasse extends JXPanel implements ItemListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -720717301520114866L;
	JXPanel content = null;
	JRtaTextField[] tfs = {null,null};
	JRtaTextField[] tfzahlen = {null,null,null,null,null,null};
	JButton[] buts = {null,null,null};
	JLabel lab = null;
	JLabel einlab = null;
	JLabel auslab = null;
	
	JLabel kasselab = null;
	JLabel barlab = null;
	JLabel differenzlab = null;

	JCheckBox ChkRG = null;
	JCheckBox ChkVerk = null;
	boolean incRG = false, incVerk = false, incPR = false, hideOfficeInBackground = false;

	ActionListener al = null;
	KeyListener kl = null;
	ItemListener il = null;
	
	DecimalFormat dcf = new DecimalFormat("#######0.00");
	
	public Barkasse(JBarkassenInternal bki){
		super();
		this.makeListeners();
		readLastSelection();
		this.add(getContent(),BorderLayout.CENTER);
		for(int i=0;i<5;i++){
			tfzahlen[i].addKeyListener(kl);
		}
		SwingUtilities.invokeLater(new Runnable(){
			@Override
            public void run(){
				setzeFocus();
			}
		});
	}
	private void setzeFocus(){
		SwingUtilities.invokeLater(new Runnable(){
			@Override
            public void run(){
				tfs[0].requestFocus();
			}
		});
	}
	private JXPanel getContent(){//       1               2    3     4     5    6    7     8     9     10    11    12
		FormLayout lay = new FormLayout("fill:0:grow(0.5),5dlu,22dlu,64dlu,5dlu,9dlu,20dlu,60dlu,11dlu,14dlu,60dlu,5dlu,fill:0:grow(0.5)",
           // 1    2  3   4  5    6  7   8 9   10  11 12  13 14 15  16 17  18 19  20 21  22 23  24  25 26  27 28  29 30
			"10dlu,p,3dlu,p,10dlu,p,2dlu,p,5dlu,p,2dlu,p,2dlu,p,2dlu,p,2dlu,p,5dlu,p,2dlu,p,2dlu,p,2dlu,p,2dlu,p,2dlu,p");
		CellConstraints cc = new CellConstraints();
		int rowCnt=2;
		ChkRG = new JCheckBox();
		if (incRG) {
			ChkRG.setSelected(true);
		}
		ChkRG.addItemListener(this);			// Listener registrieren (setzt 'implements ... ItemListener' in class definition voraus)
		ChkVerk = new JCheckBox();
		if (incVerk) {
			ChkVerk.setSelected(true);
		}
		ChkVerk.addItemListener(this);

		content = new JXPanel();
		// content = new FormDebugPanel(); <- tut nicht! - wie geht das?

		content.setLayout(lay);
		lab = new JLabel("Erfassungszeitraum");
		content.add(lab,cc.xyw(3,rowCnt++,5));
		
		lab = new JLabel("von...");
		content.add(lab,cc.xy(3,++rowCnt));
		tfs[0] = new JRtaTextField("DATUM",false);
		tfs[0].setText(DatFunk.sHeute());
		content.add(tfs[0],cc.xyw(4,rowCnt,2));
		
		lab = new JLabel("bis...");
		content.add(lab,cc.xy(7,rowCnt));
		tfs[1] = new JRtaTextField("DATUM",false);
		tfs[1].setText(DatFunk.sHeute());
		content.add(tfs[1],cc.xyw(8,rowCnt++,2));
		
		lab = new JLabel("berücksichtige Einnahmen aus");
		content.add(lab,cc.xyw(3,++rowCnt,5));
		rowCnt++;
		lab = new JLabel("Rezeptgebühren");
		content.add(lab,cc.xyw(3,++rowCnt,3));
		// checkbox Gebühen (checked)
		content.add(ChkRG, cc.xy(6, rowCnt));
		lab = new JLabel("Verkauf");
		content.add(lab,cc.xy(8,rowCnt));
		// checkbox Verkauf (unchecked)
		content.add(ChkVerk, cc.xy(9, rowCnt));

		content.add((buts[0]=ButtonTools.macheButton("ermitteln", "ermitteln", al)),cc.xy(11,rowCnt++));
		
		lab = new JLabel("ermittelte Einnahmen");
		lab.setForeground(Color.BLUE);
		content.add(lab,cc.xyw(4,++rowCnt,3,CellConstraints.RIGHT,CellConstraints.DEFAULT));
		
		einlab = new JLabel("0,00");
		einlab.setForeground(Color.BLUE);
		content.add(einlab,cc.xyw(8,rowCnt++,2,CellConstraints.RIGHT,CellConstraints.DEFAULT));

		lab = new JLabel("ermittelte Ausgaben");
		lab.setForeground(Color.BLUE);
		content.add(lab,cc.xyw(4,++rowCnt,3,CellConstraints.RIGHT,CellConstraints.DEFAULT));
		
		auslab = new JLabel("0,00");
		auslab.setForeground(Color.BLUE);
		content.add(auslab,cc.xyw(8,rowCnt++,2,CellConstraints.RIGHT,CellConstraints.DEFAULT));

		//
		lab = new JLabel("Anfangsbestand");
		lab.setForeground(Color.BLUE);
		content.add(lab,cc.xyw(4,++rowCnt,3,CellConstraints.RIGHT,CellConstraints.DEFAULT));
		tfzahlen[0] = new JRtaTextField("FL",true,"6.2","RECHTS");
		tfzahlen[0].setText("0,00");
		content.add(tfzahlen[0],cc.xyw(8,rowCnt++,2));

		lab = new JLabel("zusätzliche Einnahmen");
		lab.setForeground(Color.BLUE);
		content.add(lab,cc.xyw(4,++rowCnt,3,CellConstraints.RIGHT,CellConstraints.DEFAULT));
		tfzahlen[1] = new JRtaTextField("FL",true,"6.2","RECHTS");
		tfzahlen[1].setText("0,00");
		content.add(tfzahlen[1],cc.xyw(8,rowCnt++,2));
		
		lab = new JLabel("zusätzliche Ausgaben");
		lab.setForeground(Color.BLUE);
		content.add(lab,cc.xyw(4,++rowCnt,3,CellConstraints.RIGHT,CellConstraints.DEFAULT));
		tfzahlen[2] = new JRtaTextField("FL",true,"6.2","RECHTS");
		tfzahlen[2].setText("0,00");
		content.add(tfzahlen[2],cc.xyw(8,rowCnt++,2));

		lab = new JLabel("Wert der Scheckeinnahmen");
		lab.setForeground(Color.BLUE);
		content.add(lab,cc.xyw(4,++rowCnt,3,CellConstraints.RIGHT,CellConstraints.DEFAULT));
		tfzahlen[3] = new JRtaTextField("FL",true,"6.2","RECHTS");
		tfzahlen[3].setText("0,00");
		content.add(tfzahlen[3],cc.xyw(8,rowCnt++,2));
		
		lab = new JLabel("gezählter Bargeldbestand (Ist)");
		lab.setForeground(Color.BLUE);
		content.add(lab,cc.xyw(4,++rowCnt,3,CellConstraints.RIGHT,CellConstraints.DEFAULT));
		tfzahlen[4] = new JRtaTextField("FL",true,"6.2","RECHTS");
		tfzahlen[4].setText("0,00");
		content.add(tfzahlen[4],cc.xyw(8,rowCnt++,2));

		lab = new JLabel("Kassenbestand Soll");
		lab.setForeground(Color.BLUE);
		content.add(lab,cc.xyw(4,++rowCnt,3,CellConstraints.RIGHT,CellConstraints.DEFAULT));
		kasselab = new JLabel("0,00");
		kasselab.setForeground(Color.RED);
		content.add(kasselab,cc.xyw(8,rowCnt++,2,CellConstraints.RIGHT,CellConstraints.DEFAULT));

		lab = new JLabel("Bargeldbestand Soll");
		lab.setForeground(Color.BLUE);
		content.add(lab,cc.xyw(4,++rowCnt,3,CellConstraints.RIGHT,CellConstraints.DEFAULT));
		barlab = new JLabel("0,00");
		barlab.setForeground(Color.RED);
		content.add(barlab,cc.xyw(8,rowCnt++,2,CellConstraints.RIGHT,CellConstraints.DEFAULT));
		
		lab = new JLabel("Differenz im Bargeldbestand!!!");
		lab.setForeground(Color.RED);
		content.add(lab,cc.xyw(4,++rowCnt,3,CellConstraints.RIGHT,CellConstraints.DEFAULT));
		differenzlab = new JLabel("0,00");
		differenzlab.setForeground(Color.RED);
		content.add(differenzlab,cc.xyw(8,rowCnt++,2,CellConstraints.RIGHT,CellConstraints.DEFAULT));

		
		content.add((buts[1]=ButtonTools.macheButton("drucken", "drucken", al)),cc.xy(11,++rowCnt));
		
		return content;
	}
	private void makeListeners(){
		al = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String cmd = arg0.getActionCommand();
				if(cmd.equals("ermitteln")){
					doErmitteln();
					return;
				}
				if(cmd.equals("drucken")){
					new SwingWorker<Void,Void>(){
						@Override
						protected Void doInBackground() throws Exception {
							doDrucken();
							return null;
						}
					}.execute();
					return;
				}
			}
		};
		kl = new KeyListener(){
			@Override
			public void keyPressed(KeyEvent arg0) {
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				doRechnen();
			}
			@Override
			public void keyTyped(KeyEvent arg0) {
			}
		};
	}


	private void doErmitteln(){
		String dat1,dat2; 
		double einnahmen=0, ausgaben=0; 
		try{
			dat1 = DatFunk.sDatInSQL(tfs[0].getText());
			dat2 = DatFunk.sDatInSQL(tfs[1].getText());
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null, "Die Angaben von...bis... sind nicht korrekt");
			return;
		}

		if (ChkRG.isSelected()){	// zuerst die Rezeptgebühren ermitteln ...
			Vector<Vector<String>> vec = SqlInfo.holeFelder("select sum(einnahme),sum(ausgabe) from kasse where datum>='"+dat1+"' AND datum<='"+dat2+"'");
			if(vec.get(0).get(0).trim() != ""){
				einnahmen = einnahmen + Double.parseDouble(vec.get(0).get(0).trim());
			}
			if(vec.get(0).get(1).trim() != ""){
				ausgaben = ausgaben + Double.parseDouble(vec.get(0).get(1).trim());
			}
		}
		if (ChkVerk.isSelected()){	// ... dann die Verkaufserlöse
			Vector<Vector<String>> vec = SqlInfo.holeFelder("select sum(v_betrag) from verkliste where v_datum>='"+dat1+"' AND v_datum<='"+dat2+"' AND v_nummer like 'VB-%'");
			if(vec.get(0).get(0).trim() != ""){
				einnahmen = einnahmen + Double.parseDouble(vec.get(0).get(0).trim());
			}
		}

		if(einnahmen == 0){
			einlab.setText("0,00");
		}else{
			einlab.setText(dcf.format(einnahmen));	
		}
		if(ausgaben == 0){
			auslab.setText("0,00");
		}else{
			auslab.setText(dcf.format(ausgaben));	
		}
		doRechnen();
		
	}
	private void doDrucken() throws DocumentException{
		try {
			starteOO();
		} catch (OfficeApplicationException e) {
			e.printStackTrace();
		} catch (NOAException e) {
			e.printStackTrace();
		} catch (TextException e) {
			e.printStackTrace();
		}
	}
	private void doRechnen(){
		BigDecimal kassenbestand = BigDecimal.valueOf(textToDouble(einlab.getText() ));
		kassenbestand = kassenbestand.subtract(BigDecimal.valueOf(textToDouble(auslab.getText() )));
		kassenbestand = kassenbestand.add(BigDecimal.valueOf(textToDouble(tfzahlen[0].getText() )));
		kassenbestand = kassenbestand.add(BigDecimal.valueOf(textToDouble(tfzahlen[1].getText() )));
		kassenbestand = kassenbestand.subtract(BigDecimal.valueOf(textToDouble(tfzahlen[2].getText() )));
		kasselab.setText(doubleToText(kassenbestand.doubleValue()));
		
		BigDecimal scheck = BigDecimal.valueOf( textToDouble(tfzahlen[3].getText() ));
		barlab.setText(doubleToText(kassenbestand.subtract(scheck).doubleValue()));
		
		BigDecimal gezaehlt = BigDecimal.valueOf( textToDouble(tfzahlen[4].getText() ));
		
		differenzlab.setText(doubleToText(gezaehlt.subtract(BigDecimal.valueOf(textToDouble(barlab.getText()))).doubleValue()) );
	}
	private String doubleToText(Double dbl){
		return dcf.format(dbl).replace(".", ",");
	}
	private Double textToDouble(String dbl){
		if(dbl.trim().equals("")){
			return 0.00;
		}
		return Double.parseDouble(dbl.replace(",", "."));
	}
	private void starteOO() throws OfficeApplicationException, NOAException, TextException, DocumentException{
		IDocumentService documentService = null;
		String url = null;
		if(!Reha.officeapplication.isActive()){
			Reha.starteOfficeApplication();
		}
		documentService = Reha.officeapplication.getDocumentService();
		IDocument document = null;

		DocumentDescriptor docdescript = new DocumentDescriptor();
		docdescript.setAsTemplate(true);
		//docdescript.setHidden(true);
		url = Path.Instance.getProghome()+"vorlagen/"+Reha.getAktIK()+"/Barkasse.ott";
		document = documentService.loadDocument(url,docdescript);

		docdescript.setHidden(hideOfficeInBackground);
		//ITextTable[] tbl = null;
		ITextDocument textDocument = (ITextDocument)document;
		//tbl = textDocument.getTextTableService().getTextTables();
		ITextTable textTable = null;
		textTable = textDocument.getTextTableService().getTextTable("Tabelle1");
		textTable.getCell(2,0).getTextService().getText().setText(einlab.getText()+" €");
		textTable.getCell(2,1).getTextService().getText().setText(auslab.getText()+" €");
		textTable.getCell(2,2).getTextService().getText().setText(tfzahlen[0].getText()+" €");
		textTable.getCell(2,3).getTextService().getText().setText(tfzahlen[1].getText()+" €");
		textTable.getCell(2,4).getTextService().getText().setText(tfzahlen[2].getText()+" €");
		textTable.getCell(2,5).getTextService().getText().setText(tfzahlen[3].getText()+" €");
		textTable.getCell(2,6).getTextService().getText().setText(tfzahlen[4].getText()+" €");
		textTable.getCell(2,7).getTextService().getText().setText(kasselab.getText()+" €");
		textTable.getCell(2,8).getTextService().getText().setText(barlab.getText()+" €");
		textTable.getCell(2,9).getTextService().getText().setText(differenzlab.getText()+" €");

		//document.getFrame().getXFrame().getContainerWindow().setVisible(true);
		if(hideOfficeInBackground) {
/*
 * keine Drucker-settings in ini/Systeminitialisierung -> Einstellungen aus Vorlage verwenden
 */
//			String druckername = settings.getStringProperty(propSection, "Drucker");
			IPrinter drucker = null;
//			if(druckername == null) {
				drucker = textDocument.getPrintService().getActivePrinter();
//			} else {
//				drucker = textDocument.getPrintService().createPrinter(druckername);
//			}
//			textDocument.getPrintService().setActivePrinter(drucker);
//			short exemplare = settings.getIntegerProperty(propSection, "Exemplare").shortValue();
//			final PrintProperties printprop = new PrintProperties(exemplare, null);
				final PrintProperties printprop = new PrintProperties((short) 1, null);
				textDocument.getPrintService().print(printprop);
			final ITextDocument xdoc = textDocument;
			new SwingWorker<Void,Void>() {

				@Override
				protected Void doInBackground() throws Exception {
					Thread.sleep(100);
					xdoc.close();
					Thread.sleep(100);
					return null;
				}
				
			};
		}else{
			OOTools.bringDocToFront(documentService, textDocument, docdescript, url);
		}

	}
	public void doAufraeumen(){
		buts[0].removeActionListener(al);
		buts[1].removeActionListener(al);
		al = null;
		tfs[0].listenerLoeschen();
		tfs[1].listenerLoeschen();
		for(int i = 0; i < 5; i++){
			tfzahlen[i].removeKeyListener(kl);
			tfzahlen[i].listenerLoeschen();
		}
		kl = null;
		saveLastSelection();
	}
	@Override
	public void itemStateChanged(ItemEvent e) {
	    Object source = e.getItemSelectable();
	    //    System.out.println(e.getStateChange() == ItemEvent.SELECTED ? "SELECTED" : "DESELECTED");

	    if (source == ChkRG) {
	        //find out whether box was checked or unchecked.
	        if (e.getStateChange() == ItemEvent.DESELECTED) {
	            //keine Gebühren berücksichtigen
	        }else{
	        }
	    }
	    if (source == ChkVerk) {
	        if (e.getStateChange() == ItemEvent.DESELECTED) {
	        	// keine Verkaufserlöse berücksichtigen
	        }else{
	        }
	    }
	}	
	/**
	 * liest die zuletzt verwandten Checkbox-Einstellungen aus der bedienung.ini
	 * ist keine Einstellung vorhanden, wird ein Default gesetzt
	 */
	private void readLastSelection(){
		INIFile inif = INITool.openIni(Path.Instance.getProghome()+"ini/"+Reha.getAktIK()+"/", "bedienung.ini");
		if ( inif.getStringProperty("BarKasse", "Rezeptgebuehren") != null ){					// Eintraege in ini vorhanden
			incRG = inif.getBooleanProperty("BarKasse", "Rezeptgebuehren") ;
			incVerk = inif.getBooleanProperty("BarKasse", "Verkaeufe");			
		}else{
			// Default-Werte setzen (Verhalten wie vor Erweiterung um Verkäufe)
			incRG = true;
			incVerk = false;
		}
		if ( inif.getStringProperty("BarKasse", "SofortDrucken") != null ){						// unabhäng. (später hinzugekommen)
			hideOfficeInBackground = inif.getBooleanProperty("BarKasse", "SofortDrucken") ;
		}else{
			hideOfficeInBackground = false;
		}
	}
	/**
	 * schreibt die zuletzt verwandten Checkbox-Einstellungen (falls geändert) in die bedienung.ini
	 */
	private void saveLastSelection(){
		if ( ( ChkRG.isSelected() != incRG ) || ( ChkVerk.isSelected() != incVerk ) ){
			INIFile inif = INITool.openIni(Path.Instance.getProghome()+"ini/"+Reha.getAktIK()+"/", "bedienung.ini");
			inif.setBooleanProperty("BarKasse", "Rezeptgebuehren", ChkRG.isSelected(), "Abrechnung Barkasse beruecksichtigt");
			inif.setBooleanProperty("BarKasse", "Verkaeufe", ChkVerk.isSelected(), null);
			INITool.saveIni(inif);
		}
	}
}
