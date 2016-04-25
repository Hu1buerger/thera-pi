package CommonTools;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

public class INITool {
	static String[] dbInis = null;
	
	public static void setDBInis(String[] xdbini){
		dbInis = xdbini;
	}
	public static String[] getDBInis(){
		return dbInis;
	}
	public static void init(String pfad){
		INIFile file = new INIFile(pfad+"inicontrol.ini");
		try{
			int anzahl = file.getIntegerProperty("INIinDB", "INIAnzahl");
			if(anzahl == 0){
				dbInis = new String[] {"nix"};
			}else{
				dbInis = new String[anzahl];
				for(int i = 0; i < dbInis.length;i++){
					dbInis[i] = String.valueOf(file.getStringProperty("INIinDB", "DBIni"+Integer.toString(i+1)));
				}
			}
		}catch(Exception ex){
			dbInis = new String[] {"nix"};
		}
	}
	/**
	 * liefert INIFile Objekt mit aktuell verwandter ini, unabhaengig ob die eine lokale Datei oder in der DB abgelegt ist
	 * @param path Pfad zu lokaler ini
	 * @param iniToOpen Name der ini-Datei
	 * @return INIFile Objekt
	 */
	public static INIFile openIni(String path,String iniToOpen){
		INIFile inif = null;
		try{
			if(Arrays.asList(dbInis).contains(iniToOpen)){
				InputStream stream = SqlInfo.liesIniAusTabelle(iniToOpen);
				inif = new INIFile(stream,iniToOpen);
			}else{
				inif = new INIFile(path+iniToOpen);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return inif;
	}
	/**
	 * liefert INIFile Objekt mit lokaler ini-Datei, falls die ini in der DB abgelegt ist, sonst null
	 * @param path Pfad zu lokaler ini
	 * @param iniToOpen Name der ini-Datei
	 * @return INIFile Objekt
	 */
	public static INIFile openIniFallback(String path,String iniToOpen){
		INIFile inif = null;
		try{
			if(Arrays.asList(dbInis).contains(iniToOpen)){
				inif = new INIFile(path+iniToOpen);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return inif;
	}
	/************************/
	public static boolean saveIni(INIFile iniToSave){
		boolean ret = false;
		try{
			if(Arrays.asList(dbInis).contains(iniToSave.getFileName())){
				SqlInfo.schreibeIniInTabelle(iniToSave.getFileName(),iniToSave.saveToStringBuffer().toString().getBytes());
				iniToSave.getInputStream().close();
				iniToSave = null;
			}else{
				iniToSave.save();
			}
			ret = true;
		}catch(Exception ex){
			ex.printStackTrace();
		}	
		return ret;
	}

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
			hmMahnPar.put("formular"+lfdNb, (String) path2Templates+defaultName+lfdNb+".ott" );
		}		
	}
	
}
