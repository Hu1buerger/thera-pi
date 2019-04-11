package dialoge;

import hauptFenster.Reha;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXPanel;

import systemEinstellungen.SystemConfig;
import CommonTools.DatFunk;
import CommonTools.JCompTools;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class InfoDialogRGAFoffen extends InfoDialog {

	private static final long serialVersionUID = -4100786817400796515L;
	private JLabel textlab;
	private JLabel bildlab;
	Font font = new Font("Arial",Font.PLAIN,12);

	public InfoDialogRGAFoffen(String arg1, Vector<Vector<String>> data) {
//		super(arg1, "offenRGAF", data);
		super(arg1, data);
		
		activateListener();
		this.setContentPane(getOffeneRechnungenInfoContent(data));

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.addKeyListener(kl);		// erbt 'kl' von InfoDialog
		this.getContentPane().validate();
	}

	public JXPanel getOffeneRechnungenInfoContent(Vector<Vector<String>> vdata){
		JXPanel jpan = new JXPanel();
		jpan.addKeyListener(kl);
		//jpan.setPreferredSize(new Dimension(400,100));
		jpan.setBackground(Color.WHITE);
		jpan.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		FormLayout lay = new FormLayout("5dlu,fill:0:grow(0.5),p,fill:0:grow(0.5),5dlu",
				"5dlu,p,5dlu,p,p,250dlu,5dlu,2dlu,150dlu:g,5dlu");
		jpan.setLayout(lay);
		CellConstraints cc = new CellConstraints();
		bildlab = new JLabel(" ");
		bildlab.setIcon(SystemConfig.hmSysIcons.get("tporgklein"));
		jpan.add(bildlab,cc.xy(3, 2));
		htmlPane1 = new JEditorPane(/*initialURL*/);
        htmlPane1.setContentType("text/html");
        htmlPane1.setEditable(false);
        htmlPane1.setOpaque(false);
        htmlPane1.addKeyListener(kl);
        //htmlPane.addHyperlinkListener(this);
        scr1 = JCompTools.getTransparentScrollPane(htmlPane1);
        scr1.validate();	
        jpan.add(scr1,cc.xywh(2,4,3, 4));

		htmlPane2 = new JEditorPane(/*initialURL*/);
        htmlPane2.setContentType("text/html");
        htmlPane2.setEditable(false);
        htmlPane2.setOpaque(false);
        htmlPane2.addKeyListener(kl);
        scr2 = JCompTools.getTransparentScrollPane(htmlPane2);
        scr2.validate();	
        jpan.add(scr2,cc.xywh(2,8,3,2));		

		holeOffeneRechnungen(vdata);
		scr1.validate();
		scr2.validate();
		jpan.revalidate();
		return jpan;
	}

	/***************************************************/
	public void holeOffeneRechnungen(Vector<Vector<String>> data){
		String complete = ladehead();
		StringBuffer bdata = new StringBuffer();
		bdata.append("<span "+getSpanStyle("14","")+"Offene RGR-/AFR-Rechnungen</span><br>\n");
		bdata.append("<table width='100%'>\n"); 
		Double gesamt = 0.00;
		//String stmt = "select t1.rdatum,t1.rnr,t1.roffen,t1.pat_intern from rgaffaktura as t1 join pat5 as t2 on (t1.pat_intern=t2.pat_intern) where t1.roffen > '0' and t1.pat_intern = '"+xpatint+"' order by t1.rdatum";
		for( int i = 0; i < data.size();i++){
			bdata.append("<tr>\n");
			bdata.append("<td>"+Integer.toString(i+1)+".</td>\n");
			bdata.append("<td>\n");
			bdata.append(DatFunk.sDatInDeutsch(data.get(i).get(0)));
			bdata.append("</td>\n");
			bdata.append("<td>\n");
			bdata.append(data.get(i).get(1));
			bdata.append("</td>\n");
			bdata.append("<td>\n");
			bdata.append(data.get(i).get(2).replace(".", ",")+" EUR");
			bdata.append("</td>\n");
			bdata.append("</tr>\n");
			gesamt = gesamt+Double.parseDouble(data.get(i).get(2));
		}
		bdata.append("<tr>\n");
		bdata.append("<td>\n");
		bdata.append("&nbsp;");
		bdata.append("</td>\n");
		bdata.append("<td>\n");
		bdata.append("<b>Summe</b>");
		bdata.append("</td>\n");
		bdata.append("<td>\n");
		bdata.append("&nbsp;");
		bdata.append("</td>\n");
		bdata.append("<td>\n");
		bdata.append("<b>"+df.format( gesamt)+"</b> EUR");
		bdata.append("</td>\n");
		bdata.append("</tr>\n");
		
		bdata.append("</table>\n");
		complete = complete+bdata.toString()+ladeend();
		htmlPane1.setText(complete);
		
		bdata.setLength(0);
		bdata.trimToSize();
		complete = "";
		complete = ladehead();
		bdata.append("<span "+getSpanStyle("14","")+"Merkmale für diesen Patient</span><br>\n");
		int durchlauf = 0;
		for(int i = 62; i > 56;i--){
			if(Reha.thisClass.patpanel.patDaten.get(i).equals("T")){
				/*
				vPatMerker.add(inif.getStringProperty("Kriterien", "Krit"+i));
				String simg = inif.getStringProperty("Kriterien", "Image"+i);
				if(simg.equals("")){
					vPatMerkerIcon.add(null);
				}else{
					vPatMerkerIcon.add(new ImageIcon(Reha.proghome+"icons/"+simg));
				}*/
				if(SystemConfig.vPatMerkerIconFile.get(durchlauf) != null){
					bdata.append("<img src='file:///"+SystemConfig.vPatMerkerIconFile.get(durchlauf)+"'>&nbsp;&nbsp;");	
				}
				
				bdata.append("<span "+getSpanStyle("12","#FF0000")+SystemConfig.vPatMerker.get(durchlauf)+"</span><br>\n");
				
				
			}
			durchlauf++;
		}
		complete = complete+bdata.toString()+ladeend();
		htmlPane2.setText(complete);
		 
	}
	

}
