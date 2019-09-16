package abrechnung;

import java.util.ArrayList;
import java.util.Arrays;

import CommonTools.JRtaComboBox;
import systemEinstellungen.SystemConfig;

public class Disziplinen {
	private String[] typeOfVerordnung;
	private ArrayList<String> diszis;
	private String[] rezeptKlassen; 
	private String[] hmCodePrefix; 
	private JRtaComboBox cmbDiszi;
	private ArrayList<String> listTypeOfVO;
	private String[] typeOfActiveVerordnung;
	private String[] aktiveRezeptKlassen; 
	private JRtaComboBox cmbDisziActive;
	private ArrayList<String> listActiveTypeOfVO;

	/**
	 * verwaltet die enthaltenen Heilmittelsparten
	 */
	public Disziplinen() {
		// kleiner Überblick über das real existierende Chaos
		// aus HMRCheck
		//		String diszis[] =    {"2", "1", "5", "3", "8", "7"};	// auch in SysUtilTarifgruppen
		//		String diszikurz[] = {"KG","MA","ER","LO","RH","PO"}; 	// <- gibt es keine RH-Rezepte (? s.u.)
		
		// aus SysUtilTarifgruppen 			// <- McM: das ist die umfangreichste - als Basis nehmen?
		//		tabName = new String[]    {"kgtarif","matarif","ertarif","lotarif","rhtarif","potarif","rstarif","fttarif"}; 
		// 		dummydiszi = new String[] {"Physio","Massage","Ergo","Logo","Reha","Podo","Rsport","Ftrain"};

		// aus Rezeptneuanlage (+REHA-Verordnung, -Rehasport-Rezept, -Funktionstraining-Rezept)
		// String[] idiszi = {"Physio-Rezept","Massage/Lymphdrainage-Rezept","Ergotherapie-Rezept","Logopädie-Rezept","REHA-Verordnung","Podologie-Rezept"};


		diszis = new ArrayList<String>(Arrays.asList("Physio","Massage","Ergo","Logo","Podo","Rsport","Ftrain"));	// aus AktuelleRezepte
		rezeptKlassen = new String [] {"KG","MA","ER","LO","PO","RS","FT"};		// aus AbrechnungGKV (!)
		hmCodePrefix = new String [] {"2","1","5","3","7","8",""};				// passende Prefixe nach HMpositionsnummernverzeichnis    McM: "8" auch für "RH" u. "FT"?

		// erst eine Arraylist mit Auswahleinträgen erzeugen (<- die kann 'wachsen'; ein Array nicht), ...
		// verwendet in AbrechnungGKV(!)     McM: d.h. Reha-Verordnungen werden nicht elektron. abgerechnet?
		listTypeOfVO = new ArrayList<String>(Arrays.asList("Physio-Rezept","Massage/Lymphdrainage-Rezept","Ergotherapie-Rezept","Logopädie-Rezept","Podologie-Rezept"));
		if(SystemConfig.mitRs){
			listTypeOfVO.addAll(Arrays.asList("Rehasport-Rezept","Funktionstraining-Rezept"));
		}
		typeOfVerordnung = new String[ listTypeOfVO.size() ];		// ... daraus das Array fuer die ComboBox erstellen ...
		listTypeOfVO.toArray( typeOfVerordnung );					// ... und fuellen
		this.cmbDiszi = new JRtaComboBox(typeOfVerordnung);
		String initVal = SystemConfig.initRezeptKlasse;
		if (initVal.equals(null) || initVal.isEmpty()){
			initVal = SystemConfig.rezeptKlassenAktiv.get(0).get(0);
		}
		if (initVal.equals(null) || initVal.isEmpty()){
			initVal = listTypeOfVO.get(0);
		}
		cmbDiszi.setSelectedItem(initVal);						// default setzen

		// weitere ComboBox mit nur den aktiven Rezeptklassen erzeugen
		listActiveTypeOfVO = new ArrayList<String>();
		int aktiveKlassen = SystemConfig.rezeptKlassenAktiv.size();
		aktiveRezeptKlassen = new String[aktiveKlassen];
		for(int i = 0;i < aktiveKlassen;i++){
			listActiveTypeOfVO.add(SystemConfig.rezeptKlassenAktiv.get(i).get(0));	
			aktiveRezeptKlassen[i] = SystemConfig.rezeptKlassenAktiv.get(i).get(1); 
		}
		typeOfActiveVerordnung = new String[ aktiveKlassen ];
		listActiveTypeOfVO.toArray(typeOfActiveVerordnung);
		this.cmbDisziActive = new JRtaComboBox(typeOfActiveVerordnung);
		cmbDisziActive.setSelectedItem(SystemConfig.initRezeptKlasse);
	}

	public JRtaComboBox getComboBox(){
		return this.cmbDiszi;
	}
	
	public String getCurrDiszi(){
		if (diszis.size() >= cmbDiszi.getSelectedIndex()){
			return diszis.get(cmbDiszi.getSelectedIndex());			
		}else{
			System.out.println("getCurrDiszi err: size "+diszis.size()+" vs idx "+cmbDiszi.getSelectedIndex());
			return diszis.get(0);	// use default ("Physio")
		}
	}

	public void setCurrDiszi(String currTypeOfVO) {
		this.cmbDiszi.setSelectedItem(currTypeOfVO);
	}

	public String getCurrRezClass(){
		if (rezeptKlassen.length >= cmbDiszi.getSelectedIndex()){
			return rezeptKlassen[cmbDiszi.getSelectedIndex()];			
		}else{
			System.out.println("getCurrDiszi err: size "+diszis.size()+" vs idx "+cmbDiszi.getSelectedIndex());
			return rezeptKlassen[0];	// use default ("KG")
		}
	}

	/**
	 * liefert Index der als Kurzbezeichnung (z.B. "Physio") uebergebenen Disziplin 
	 */
	public int getIndex(String Disziplin) {
		return diszis.indexOf(Disziplin);
	}
	
	public String[] getActiveRK(){
		return this.aktiveRezeptKlassen;
	}

	public JRtaComboBox getComboBoxActiveRK(){
		return this.cmbDisziActive;
	}

	public String getCurrDisziFromActRK(){
		String selectedRK = cmbDisziActive.getSelectedItem().toString();
		for (int i = 0; i < listTypeOfVO.size(); i++){
			if(selectedRK.equals(listTypeOfVO.get(i))){
				return diszis.get(i);
			}
		}
		System.out.println("getCurrDisziFromActRK err: not found: "+selectedRK);
		return diszis.get(0);	// use default ("Physio")
	}

	public void setCurrDisziActRK(String currTypeOfVO) {
		this.cmbDisziActive.setSelectedItem(currTypeOfVO);
	}

	/**
	 * liefert numer. Prefix des HM-Codes fuer die uebergebene Disziplin
	 */
	public String getPrefix(int Disziplin) {
		return hmCodePrefix[Disziplin];
	}

	/**
	 * liefert den Rezeptklasse-Prefix (z.B. "KG") fuer die uebergebene Disziplin 
	 */
	public String getRezClass(int Disziplin) {
		return rezeptKlassen[Disziplin];
	}

	public String getRezClass(String typeOfVO) {
		for (int i=0; i<listTypeOfVO.size(); i++){
			if (typeOfVO.equalsIgnoreCase(listTypeOfVO.get(i))){
				return rezeptKlassen[i];				
			}
		}
		System.out.println("getRezClass err: not found: "+typeOfVO+" set "+rezeptKlassen[0]+" as default");
		return rezeptKlassen[0];	// use default ("KG")
	}

	public String getDiszi(String typeOfVO){
		for (int i=0; i<listTypeOfVO.size(); i++){
			if (typeOfVO.equalsIgnoreCase(listTypeOfVO.get(i))){
				return diszis.get(i);				
			}
		}
		System.out.println("getDiszi err: not found: "+typeOfVO+" set "+diszis.get(0)+" as default");
		return diszis.get(0);	// use default ("Physio")
	}

}