package opRgaf;

import java.util.HashMap;

import CommonTools.INIFile;
import CommonTools.INITool;



public class OpRgAfIni {
	//INIFile inif;
	String path2IniFile;
	boolean incRG = false, incAR = false, incVK = false, settingsLocked = false;
	int vorauswahlSuchkriterium, suchkriteriumSaved = 0;
	public static HashMap<String,Object> mahnParam;
	String progHome, aktIK;
	public HashMap getMahnParameter;

	
	/**
	 * öffnet 'oprgaf.ini'
	 * @param args pfad, ik
	 */
	//args[0],"ini/",args[1],"/oprgaf.ini"
	public OpRgAfIni(String home, String subPath, String IK, String file) {
		progHome = home;
		aktIK = IK;
		path2IniFile = progHome+subPath+aktIK+file;
		INIFile inif = new INIFile (path2IniFile);		// wenn keine Direktzugriffe aus ext. Modulen (mehr) erfolgen -> iniFile nur noch lokal öffnen
		//inif = new INIFile (path2IniFile);
		if ( inif.getStringProperty("offenePosten","lockSettings") != null ){
			settingsLocked = inif.getBooleanProperty("offenePosten", "lockSettings");
		}
		mahnParam = new HashMap<String,Object>();
		readLastSelection();
		readMahnParam();
	}

	/**
	 * 
	 * @return liefert Instanz mit akt. Werten der 'oprgaf.ini' zurück
	 */
	/*
	public INIFile getOpRgAfIni (){
		return inif;
	}
	*/
	
	/**
	 * liest die zuletzt verwandten (Checkbox-)Einstellungen aus der oprgaf.ini
	 * ist keine Einstellung vorhanden, werden hier die Defaults gesetzt
	 */
	private void readLastSelection(){
		String section = "offenePosten";
		INIFile inif = new INIFile (path2IniFile);
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
	 * liest die Mahnparameter aus der oprgaf.ini
	 * ist keine Einstellung vorhanden, werden hier die Defaults gesetzt
	 */
	private void readMahnParam(){
		INIFile inif = new INIFile (path2IniFile);
		if ( inif.getIntegerProperty("General","TageBisMahnung1") != null ){				// Eintrag in ini vorhanden?
			mahnParam.put("frist1", (Integer) inif.getIntegerProperty("General","TageBisMahnung1") );
		}else{
			mahnParam.put("frist1", (Integer) 31 );
		}
		if ( inif.getIntegerProperty("General","TageBisMahnung1") != null ){
			mahnParam.put("frist2", (Integer) inif.getIntegerProperty("General","TageBisMahnung2") );
		}else{
			mahnParam.put("frist2", (Integer) 11 );
		}
		if ( inif.getIntegerProperty("General","TageBisMahnung3") != null ){
			mahnParam.put("frist3", (Integer) inif.getIntegerProperty("General","TageBisMahnung3") );
		}else{
			mahnParam.put("frist3", (Integer) 11 );
		}
		if ( inif.getIntegerProperty("General","EinzelMahnung") != null ){
			mahnParam.put("einzelmahnung", (Boolean) (inif.getIntegerProperty("General","EinzelMahnung").equals("1") ? Boolean.TRUE : Boolean.FALSE) );
		}else{
			mahnParam.put("einzelmahnung", Boolean.FALSE );
		}
		if ( inif.getStringProperty("General","MahnungDrucker") != null ){
			mahnParam.put("drucker", (String) inif.getStringProperty("General","MahnungDrucker") );
		}else{
			mahnParam.put("drucker", (String) "" );
		}
		if ( inif.getIntegerProperty("General","MahnungExemplare") != null ){
			mahnParam.put("exemplare", (Integer) inif.getIntegerProperty("General","MahnungExemplare") );
		}else{
			mahnParam.put("einzelmahnung", (Integer) 2 );
		}
		if ( inif.getStringProperty("General","InOfficeStarten") != null ){
			mahnParam.put("inofficestarten", (Boolean) (inif.getIntegerProperty("General","InOfficeStarten").equals("1") ? Boolean.TRUE : Boolean.FALSE) );
		}else{
			mahnParam.put("inofficestarten", Boolean.TRUE  );
		}
		if ( inif.getStringProperty("General","AuswahlErstAb") != null ){
			mahnParam.put("erstsuchenab", (String) inif.getStringProperty("General","AuswahlErstAb") );
		}else{
			mahnParam.put("erstsuchenab", (String) "2016-01-01" );
		}
		addFormNb (inif,"General","FormularMahnung", "RGAFMahnung",1);
		addFormNb (inif,"General","FormularMahnung", "RGAFMahnung",2);
		addFormNb (inif,"General","FormularMahnung", "RGAFMahnung",3);
		addFormNb (inif,"General","FormularMahnung", "RGAFMahnung",4);
		/*
		System.out.println(mahnParameter.get("formular1"));
		System.out.println(mahnParameter.get("formular2"));
		System.out.println(mahnParameter.get("formular3"));
		System.out.println(mahnParameter.get("formular4"));
		*/
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
	}
	
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
	public int getVorauswahl(){
		return vorauswahlSuchkriterium;
	}
	

	public HashMap<String,Object> getMahnParameter (){
		return mahnParam;
	}
	
	public String getWohinBuchen (){
		return (String) mahnParam.get("inkasse");
	}

	
	
	
	
	/**
	 * schreibt die zuletzt verwandten Checkbox-Einstellungen (falls geändert) in die oprgaf.ini
	 */
	public void saveLastSelection(){
		INIFile inif = new INIFile (path2IniFile);
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
				inif.setBooleanProperty("offenePosten", "Ausfallrechnungen", incVK, null);
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
