package org.therapi.hmrCheck2021;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hauptFenster.Reha;

public class HMRCheck2021 {
	
	private String disziplin, diagnosegruppe, datum, hm1, hm2, hm3, hm4;
	private int arztid, anzahl1, anzahl2, anzahl3, anzahl4;
	
	public HMRCheck2021(String diszi, String diagnosegr, int arztid, String datum, 
			String hm1, String hm2, String hm3, String hm4, 
			int anzahl1, int anzahl2, int anzahl3, int anzahl4, int patid) {
		
		this.diagnosegruppe = diagnosegr;
		if(diszi.equals("Physio")) { this.disziplin = HmrCheck2021XML.cKG; }
		else if(diszi.equals("Ergo")) { this.disziplin = HmrCheck2021XML.cER; }
		else if(diszi.equals("Logo")) { this.disziplin = HmrCheck2021XML.cLO; }
		else if(diszi.equals("Podo")) { this.disziplin = HmrCheck2021XML.cPO; }
		
		this.hm1 = hm1;
		if(this.hm1.length()>2) {this.hm1 = "X"+this.hm1.substring(1, 5);}
		
		this.hm2 = hm2;
		if(this.hm2.length()>2) {this.hm2 = "X"+this.hm2.substring(1, 5);}
		
		this.hm3 = hm3;
		if(this.hm3.length()>2) {this.hm3 = "X"+this.hm3.substring(1, 5);}
		
		this.hm4 = hm4;
		if(this.hm4.length()>2) {this.hm4 = "X"+this.hm4.substring(1, 5);}
		
		this.anzahl1 = anzahl1;
		this.anzahl2 = anzahl1;
		this.anzahl3 = anzahl1;
		this.anzahl4 = anzahl1;
	}
	
	public void HMRCheck2021Exists(String reznr) {
		
	}
	
	public String isOkay() {
		
		String[] ign = {"X4002", "X0204", "X3011", "X3010", "X3008" };
		List<String> ignore = Arrays.asList(ign);
		String rueckgabe = "";
		int[] erlaubte = Reha.hmrXML.getAnzahl(this.disziplin, this.diagnosegruppe);
		ArrayList<String> hmPos = Reha.hmrXML.getErlaubteVorrangigeHM(this.disziplin, this.diagnosegruppe);
		ArrayList<String> hmErgPos = Reha.hmrXML.getErlaubteErgaenzendeHM(this.disziplin, this.diagnosegruppe);

		// sind aktuelle HM erlaubt
		if(!hmPos.contains(hm1) && !ignore.contains(hm1)) { rueckgabe = rueckgabe +" Heilmittel: "+hm1+" nicht als vorrangiges Heilmittel erlaubt \n"; }
		if(!hmPos.contains(hm2) && !hm2.equals("") && !ignore.contains(hm2)) { rueckgabe = rueckgabe +" Heilmittel: "+hm2+" nicht als vorrangiges Heilmittel erlaubt \n";  }
		if(!hmPos.contains(hm3) && !hm3.equals("")&& !ignore.contains(hm3)) { rueckgabe = rueckgabe +" Heilmittel: "+hm3+" nicht als vorrangiges Heilmittel erlaubt \n";  }
		if(!hmErgPos.contains(hm4) && !hm4.equals("") && !ignore.contains(hm4)) { rueckgabe = rueckgabe +" Heilmittel: "+hm4+" nicht als erg. Heilmittel erlaubt \n";  }
		// wie viele HM sind pro VO erlaubt
		int summe = 0;
		if(!hm1.equals("") && !ignore.contains(hm1)) {summe = summe + anzahl1;};
		if(!hm2.equals("") && !ignore.contains(hm2)) {summe = summe + anzahl2;};
		if(!hm3.equals("") && !ignore.contains(hm3)) {summe = summe + anzahl3;};
		if(erlaubte[HmrCheck2021XML.cVOMEN] < summe) { rueckgabe = rueckgabe +" Pro Verordnung sind nur "+erlaubte[HmrCheck2021XML.cVOMEN]+" Behandlungseinheiten erlaubt \n";  };
		// wie viele HM gab es schon im Verordnungsfall
		return rueckgabe;
	}
	
}
