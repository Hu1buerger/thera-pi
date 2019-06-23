package dialoge;

import hauptFenster.AktiveFenster;
import hauptFenster.Reha;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jdesktop.swingx.JXPanel;
import org.therapi.reha.patient.AktuelleRezepte;

import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;
import CommonTools.DatFunk;
import CommonTools.INIFile;
import CommonTools.INITool;
import CommonTools.JCompTools;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.star.uno.Exception;

import commonData.Rezept;
import events.PatStammEvent;
import events.PatStammEventClass;


public class InfoDialogVOinArbeit extends InfoDialog implements HyperlinkListener{

	private static final long serialVersionUID = -440711655648177736L;

	private JLabel textlab;
	private JLabel bildlab;
	private String currKasse;
	static HashMap<String,String> hmVO = new HashMap<String,String>();
	Font font = new Font("Arial",Font.PLAIN,12);

	private Connection connection;

	public InfoDialogVOinArbeit(String arg1, Vector<Vector<String>> data, Connection connection) {
		super(arg1, data);
		this.connection = connection;
		
		activateListener();
		this.setContentPane(getVOinArbeitInfoContent(data));

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.addKeyListener(kl);		// erbt 'kl' von InfoDialog
		this.getContentPane().validate();
	}

	private Container getVOinArbeitInfoContent(Vector<Vector<String>> data) {
		JXPanel jpan = new JXPanel();
		jpan.addKeyListener(kl);
		//jpan.setPreferredSize(new Dimension(400,100));
		jpan.setBackground(Color.WHITE);
		jpan.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		FormLayout lay = new FormLayout("5dlu,fill:0:grow(0.5),p,fill:0:grow(0.5),5dlu",
				"5dlu,p,5dlu,p,p,35dlu,5dlu,2dlu,350dlu,5dlu");
		jpan.setLayout(lay);
		CellConstraints cc = new CellConstraints();
		bildlab = new JLabel(" ");
		bildlab.setIcon(SystemConfig.hmSysIcons.get("tporgklein"));
		jpan.add(bildlab,cc.xy(3, 2));
		htmlPane1 = new JEditorPane(/*initialURL*/);		// ~ oberer Bereich
        htmlPane1.setContentType("text/html");
        htmlPane1.setEditable(false);
        htmlPane1.setOpaque(false);
        htmlPane1.addKeyListener(kl);
        //htmlPane.addHyperlinkListener(this);
        scr1 = JCompTools.getTransparentScrollPane(htmlPane1);
        jpan.add(scr1,cc.xywh(2,4,3, 4));
        
		htmlPane2 = new JEditorPane(/*initialURL*/);		// ~ unterer Bereich
        htmlPane2.setContentType("text/html");
        htmlPane2.setEditable(false);
        htmlPane2.setOpaque(false);
        //htmlPane2.setOpaque(true);
        //htmlPane2.setBackground(Color.lightGray);
        htmlPane2.addKeyListener(kl);
        htmlPane2.addHyperlinkListener(this);
        scr2 = JCompTools.getTransparentScrollPane(htmlPane2);
        jpan.add(scr2,cc.xywh(2,8,3,2));		

        showVOinArbeit(data);
        if (data.size() > 0){
            showTableVO(data);
    	}
        
        scr1.validate();	
        scr2.validate();	
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
        	   public void run() { 
        		   scr2.getVerticalScrollBar().setValue(0);
        	   }
        	});
        jpan.revalidate();
        return jpan;
	}

	private void showVOinArbeit(Vector<Vector<String>> data) {
		String complete = ladehead();
		StringBuffer bdata = new StringBuffer();
		bdata.append("<span "+getSpanStyle("12",""));
		if (data.size() > 0){
			bdata.append("Noch "+data.size());
			if (data.size() > 1){
				bdata.append(" weitere Rezepte in Arbeit für </span>\n");
			}else{
				bdata.append(" weiteres Rezept in Arbeit für </span>\n");			
			}
		}else{
			bdata.append("Keine weiteren Rezepte in Arbeit für </span>\n");
		}
		bdata.append("<br><span "+getSpanStyle("14","")+this.arg1+":</span>\n");
		bdata.append("<span "+getSpanStyle("18","")+"&nbsp;</span>\n");
		complete = complete+bdata.toString()+ladeend();
		htmlPane1.setText(complete);
	}

	private void showTableVO(Vector<Vector<String>> data) {
		Rezept myRezept = new Rezept();
		StringBuffer tdata = new StringBuffer();
		hmVO.clear();
		String complete = ladehead();
		tdata.append("<table width='100%' border-collapse: collapse; padding:none;>\n"); 
		
		tdata.append("<tr class='head'"+getSpanStyle("16","")+"<td >Rezeptnummer</td><td  align=\"center\" colspan=2>Behandlungen\n");
			tdata.append("<table width='100%' >");		// subTabelle im Header (2 Spalten)
			tdata.append("<tr class='head' align=\"center\" "+getSpanStyle("14","")+"<td>erledigt</td><td>gesamt</td></tr>\n");
			tdata.append("</table>\n");
		tdata.append("</td></tr>\n");
		for( int i = 0; i < data.size();i++){
			String thisNb = data.get(i).get(0);
			myRezept.init(thisNb);
			hmVO.put(thisNb, myRezept.getPatIntern());
			int behandlungen = myRezept.getAnzBeh(1);
			Vector<String> termine = RezTools.holeEinzelTermineAusRezept(thisNb,myRezept.getTermine());
			int abgearbeitet = termine.size();
			tdata.append("<tr"+getSpanStyle("12","")+"\n");
			tdata.append("<td><a href=\"http://vo_suche."+thisNb+".de\">"+thisNb+"</a></td> \n");	
			tdata.append("<td align=\"center\" >"+abgearbeitet+"</td><td align=\"center\" >"+behandlungen+"</td></tr>\n");
		}
		tdata.append("</table>\n");

		complete = complete+tdata.toString()+ladeend();
		htmlPane2.setText(complete);
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent event) {
	    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
	    	String url = event.getURL().toString();
	    	if(url.contains("vo_suche")){
	    		String reznr = extractVO(url);
	    		System.out.println("öffne Hyperlink zu VO: "+reznr+" für PatId: "+hmVO.get(reznr));
	    		SucheNachAllem.doPatSuchen(hmVO.get(reznr), reznr, this, this.connection);
	    	}
	      }
	}
	private String extractVO(String url){
		// Bsp.: http://vo_suche.KG19239.de
		String ext = url.substring(16);
		return ext.replace(".de", "");
	}
}

class SucheNachAllem{
	public static void doPatSuchen(String patint,String reznr,Object source, Connection connection){
		String pat_int;
		pat_int = patint;
		JComponent patient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
		final String xreznr = reznr;
		if(patient == null){
			final String xpat_int = pat_int;
			final Object xsource = source;
			new SwingWorker<Void,Void>(){
				protected Void doInBackground() throws Exception {
					JComponent xpatient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
					Reha.instance.progLoader.ProgPatientenVerwaltung(1,connection);
					while( (xpatient == null) ){
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						xpatient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
					}
					while(  (!AktuelleRezepte.initOk) ){
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					String s1 = "#PATSUCHEN";
					String s2 = (String) xpat_int;
					PatStammEvent pEvt = new PatStammEvent(xsource);
					pEvt.setPatStammEvent("PatSuchen");
					pEvt.setDetails(s1,s2,"#REZHOLEN-"+xreznr) ;
					PatStammEventClass.firePatStammEvent(pEvt);
					return null;
				}
				
			}.execute();
		}else{
			Reha.instance.progLoader.ProgPatientenVerwaltung(1,connection);
			String s1 = "#PATSUCHEN";
			String s2 = (String) pat_int;
			PatStammEvent pEvt = new PatStammEvent(source);
			pEvt.setPatStammEvent("PatSuchen");
			pEvt.setDetails(s1,s2,"#REZHOLEN-"+xreznr) ;
			PatStammEventClass.firePatStammEvent(pEvt);

		}
	}
	
}