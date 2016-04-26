package CommonTools;

import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class OpCommon {

	/**
	 * liest allgemeine Mahnparameter aus einer ini-Datei (gültig für OP u. RgAfVk)
	 * ist keine Einstellung vorhanden, werden hier die Defaults gesetzt
	 */
	public static void readMahnParamCommon(INIFile inif, HashMap<String,Object> hmMahnPar){
		if ( inif.getIntegerProperty("General","TageBisMahnung1") != null ){				// Eintrag in ini vorhanden?
			hmMahnPar.put("frist1", (Integer) inif.getIntegerProperty("General","TageBisMahnung1") );
		}else{
			hmMahnPar.put("frist1", (Integer) 31 );
		}
		if ( inif.getIntegerProperty("General","TageBisMahnung1") != null ){
			hmMahnPar.put("frist2", (Integer) inif.getIntegerProperty("General","TageBisMahnung2") );
		}else{
			hmMahnPar.put("frist2", (Integer) 11 );
		}
		if ( inif.getIntegerProperty("General","TageBisMahnung3") != null ){
			hmMahnPar.put("frist3", (Integer) inif.getIntegerProperty("General","TageBisMahnung3") );
		}else{
			hmMahnPar.put("frist3", (Integer) 11 );
		}
		if ( inif.getIntegerProperty("General","EinzelMahnung") != null ){
			hmMahnPar.put("einzelmahnung", (Boolean) (inif.getIntegerProperty("General","EinzelMahnung").equals("1") ? Boolean.TRUE : Boolean.FALSE) );
		}else{
			hmMahnPar.put("einzelmahnung", Boolean.FALSE );
		}
		if ( inif.getStringProperty("General","MahnungDrucker") != null ){
			hmMahnPar.put("drucker", (String) inif.getStringProperty("General","MahnungDrucker") );
		}else{
			hmMahnPar.put("drucker", (String) "" );
		}
		if ( inif.getIntegerProperty("General","MahnungExemplare") != null ){
			hmMahnPar.put("exemplare", (Integer) inif.getIntegerProperty("General","MahnungExemplare") );
		}else{
			hmMahnPar.put("einzelmahnung", (Integer) 2 );
		}
		if ( inif.getStringProperty("General","InOfficeStarten") != null ){
			hmMahnPar.put("inofficestarten", (Boolean) (inif.getIntegerProperty("General","InOfficeStarten").equals("1") ? Boolean.TRUE : Boolean.FALSE) );
		}else{
			hmMahnPar.put("inofficestarten", Boolean.TRUE  );
		}
		if ( inif.getStringProperty("General","AuswahlErstAb") != null ){
			hmMahnPar.put("erstsuchenab", (String) inif.getStringProperty("General","AuswahlErstAb") );
		}else{
			hmMahnPar.put("erstsuchenab", (String) "2016-01-01" );
		}
	}

	/**
	 * liest (lfd.) nummerierten Namen der Mahnformulare aus einer ini-Datei (gültig für OP u. RgAfVk)
	 * ist keine Einstellung vorhanden, werden hier die Defaults gesetzt
	 */
	public static void addFormNb (INIFile inif,String section, String entry, String defaultName, int lfdNb, HashMap<String,Object> hmMahnPar, String path2Templates){
		String forms = inif.getStringProperty(section,entry+lfdNb);
		if ( forms != null ){
			if(forms.indexOf("/") > 0){
				forms = forms.substring(forms.lastIndexOf("/")+1);
			}
			hmMahnPar.put("formular"+lfdNb, path2Templates+"/"+forms);
		}else{
			hmMahnPar.put("formular"+lfdNb, (String) path2Templates+"/"+defaultName+lfdNb+".ott" );
		}		
	}

	/**
	 * Button-Panel für die Mahnfunktionen
	 */
	public static JPanel getMahnButtonPanel(ActionListener al){
		JButton[] mahnbuts = {null,null,null};
		FormLayout lay = new FormLayout(
		//       1                 2     3                 4     5                 6     7     
				"15dlu,65dlu,fill:0:grow(0.5),2dlu,fill:0:grow(0.5),15dlu,80dlu,15dlu",		// xwerte,
		//        1   2 3  
				"15dlu,p,15dlu"																// ywerte
				);
		PanelBuilder builder = new PanelBuilder(lay);
		//PanelBuilder builder = new PanelBuilder(lay, new FormDebugPanel());				// debug mode
		builder.getPanel().setOpaque(false);
		CellConstraints cc = new CellConstraints();

		int colCnt=3, rowCnt=2;

		builder.add((mahnbuts[1] = ButtonTools.macheButton(" << ", "vorheriger", al)),cc.xy(colCnt++,2));
		colCnt++;
		builder.add((mahnbuts[2] = ButtonTools.macheButton(" >> ", "naechster", al)),cc.xy(colCnt++,2));
		colCnt++;
		builder.add((mahnbuts[0] = ButtonTools.macheButton("Mahnung drucken", "mahnungstarten", al)),cc.xy(colCnt++,2));
		return builder.getPanel();
	}
}
