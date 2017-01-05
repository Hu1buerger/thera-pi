package opRgaf;

import java.util.HashMap;

import CommonTools.INIFile;
import CommonTools.INITool;
import CommonTools.OpCommon;



public class OpRgAfIni {
	public static HashMap<String,Object> mahnParam;
	public HashMap getMahnParameter;

	//INIFile inif;
	private String path2IniFile;
	private String path2TemplateFiles;
	private String iniFile;
	private boolean incRG = false, incAR = false, incVK = false, settingsLocked = false;
	private int vorauswahlSuchkriterium = -1;
	private String progHome, aktIK;
	
	/**
	 * liest Einträge aus 'oprgaf.ini' 
	 * @param args pfad, ik
	 */
	//args[0],"ini/",args[1],"/oprgaf.ini"
	public OpRgAfIni(String home, String subPath, String IK, String file) {
		progHome = home;
		aktIK = IK;
//		path2IniFile = progHome+subPath+aktIK+file;
//		inif = new INIFile (path2IniFile);
		this.path2IniFile = progHome+subPath+aktIK;
		this.path2TemplateFiles = progHome+"vorlagen/"+aktIK;
		INITool.init(path2IniFile+"/");
		this.iniFile = file;
		INIFile inif = INITool.openIni (path2IniFile,iniFile);		// wenn keine Direktzugriffe aus ext. Modulen (mehr) erfolgen -> iniFile nur noch lokal öffnen
		if ( inif.getStringProperty("offenePosten","lockSettings") != null ){
			settingsLocked = inif.getBooleanProperty("offenePosten", "lockSettings");
		}
		mahnParam = new HashMap<String,Object>();
		readLastSelectRgAfVk(inif);
		OpCommon.readMahnParamCommon(inif, mahnParam);
		readMahnParamRgAfVk(inif, mahnParam,path2TemplateFiles);
	}


	/**
	 * liest die zuletzt verwandten (Checkbox-)Einstellungen aus der oprgaf.ini
	 * ist keine Einstellung vorhanden, werden hier die Defaults gesetzt
	 */
	private void readLastSelectRgAfVk(INIFile inif){
		String section = "offenePosten";
		if ( inif.getStringProperty(section, "Rezeptgebuehren") != null ){					// Eintraege in ini vorhanden (alle oder keiner)
			incRG = inif.getBooleanProperty(section, "Rezeptgebuehren") ;
			incAR = inif.getBooleanProperty(section, "Ausfallrechnungen") ;
			incVK = inif.getBooleanProperty(section, "Verkaeufe");		
			vorauswahlSuchkriterium = inif.getIntegerProperty(section, "Suchkriterium");	
		}else{
			// Default-Werte setzen (Verhalten wie vor Erweiterung um Verkaufsrechnungen)
			incRG = true; 
			incAR = true;
			incVK = false;
			//int vorauswahlSuchkriterium =  Arrays.asList(args).indexOf("Noch offen >=");
			vorauswahlSuchkriterium = 0;
		}
	}

	
	
	/**
	 * liest RgAfVk-spezif. Mahnparameter aus der ini-Datei
	 * ist keine Einstellung vorhanden, werden hier die Defaults gesetzt
	 */
	private void readMahnParamRgAfVk(INIFile inif, HashMap<String,Object> mahnParam, String path2Templates){
		if ( inif.getStringProperty("General","DirAlteRechnungen") != null ){
			mahnParam.put("diralterechnungen", (String) inif.getStringProperty("General","DirAlteRechnungen") );
		}else{
			mahnParam.put("diralterechnungen", (String)progHome+"rechnung/" );
		}
		if ( inif.getStringProperty("General","WohinBuchen") != null ){
			mahnParam.put("inkasse", (String) inif.getStringProperty("General","WohinBuchen") );
		}else{
			mahnParam.put("inkasse", (String)"Bank" );
		}
		for(int i = 1; i <=4;i++){
			//addFormNb (inif,"General","FormularMahnung", "RGAFMahnung", i);
			OpCommon.addFormNb (inif,"General","FormularMahnung", "RGAFMahnung", i,mahnParam,path2Templates);
		}
	}
	
/*
	private void addFormNb (INIFile inif,String section, String entry, String defaultName, int lfdNb){
		if ( inif.getStringProperty(section,entry+lfdNb) != null ){
			mahnParam.put("formular"+lfdNb, getForm4aktIk(inif,section,entry+lfdNb));
		}else{
			mahnParam.put("formular"+lfdNb, (String) progHome+"vorlagen/"+aktIK+"/"+defaultName+lfdNb+".ott" );
		}		
	}
	
	private String getForm4aktIk(INIFile inif, String section, String entry){
		String forms = inif.getStringProperty(section,entry);
		if(forms.indexOf("/") > 0){
			forms = forms.substring(forms.lastIndexOf("/")+1);
		}
		return progHome+"vorlagen/"+aktIK+"/"+forms;
	}
*/
	
	public void setIncRG(boolean value){
		incRG = value;
	}
	public boolean getIncRG(){
		return incRG;
	}
	public void setIncAR(boolean value){
		incAR = value;
	}
	public boolean getIncAR(){
		return incAR;
	}
	public void setIncVK(boolean value){
		incVK = value;
	}
	public boolean getIncVK(){
		return incVK;
	}

	public void setVorauswahl(int value){
		vorauswahlSuchkriterium = value;
	}
	public int getVorauswahl(int max){
		int maxWait = 20;
		int waitTimes = maxWait;
		while((vorauswahlSuchkriterium < 0) && (waitTimes-- > 0)){		// lesen aus ini ist noch nicht fertig...
			try {
				Thread.sleep(25);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		if(waitTimes == 0) { 
			System.out.println("OpRgaf getVorauswahl: " + 0 +"(Abbruch ini-read)");
			return 0; }
		//System.out.println("OpRgaf getVorauswahl: " + vorauswahlSuchkriterium +"(" + max + ")");
		return vorauswahlSuchkriterium < max ? vorauswahlSuchkriterium : 0;
}
	public HashMap<String,Object> getMahnParameter (){
		return mahnParam;
	}
	public String getWohinBuchen (){
		return (String) mahnParam.get("inkasse");
	}
	public String getFormNb (int lfdNb){
		return (String) mahnParam.get("formular"+lfdNb);
	}
	public int getFrist(int lfdNb){
		return (Integer) mahnParam.get("frist"+lfdNb);
	}
	public String getDrucker (){
		return (String) mahnParam.get("drucker");
	}


	/**
	 * schreibt die zuletzt verwandten Checkbox-Einstellungen (falls geändert) in die oprgaf.ini
	 */
	public void saveLastSelection(){
		INIFile inif = INITool.openIni (path2IniFile,iniFile);
		String section = "offenePosten", comment = null;
		if ( ! settingsLocked ){																	// ini-Einträge  dürfen aktualisiert werden
			if ( (incRG != inif.getBooleanProperty(section, "Rezeptgebuehren") ) || 
					( incAR != inif.getBooleanProperty(section, "Ausfallrechnungen") ) ||
					( incVK != inif.getBooleanProperty(section, "Verkaeufe") ) ||
					(vorauswahlSuchkriterium != inif.getIntegerProperty(section, "Suchkriterium") )
				){

				if ( inif.getStringProperty("offenePosten", "Rezeptgebuehren") == null ){			// Eintrag in ini noch nicht vorhanden
					comment = "offenePosten RgAfVk beruecksichtigt";
				}else{comment = null;}
				inif.setBooleanProperty("offenePosten", "Rezeptgebuehren", incRG, comment);
				inif.setBooleanProperty("offenePosten", "Ausfallrechnungen", incAR, null);
				inif.setBooleanProperty("offenePosten", "Verkaeufe", incVK, null);

				if ( inif.getStringProperty("offenePosten", "Suchkriterium") == null ){
					comment = "zuletzt gesucht";
				}else{comment = null;}
				inif.setIntegerProperty("offenePosten", "Suchkriterium", vorauswahlSuchkriterium, comment);

				if ( inif.getStringProperty("offenePosten", "lockSettings") == null ){
					inif.setBooleanProperty("offenePosten", "lockSettings",false, "Aktualisieren der Eintraege gesperrt");
				}
				INITool.saveIni(inif);
			}
			
		}
	}


}
