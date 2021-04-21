package org.therapi.hmrCheck2021;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.therapi.reha.patient.AktuelleRezepte;

import CommonTools.SqlInfo;
import hauptFenster.Reha;
import stammDatenTools.RezTools;

public class HMRCheck2021 {
	
	private String disziplin, diagnosegruppe, hm1, hm2, hm3, hm4, icd10_1, icd10_2, voArt, rezNR;
	private LocalDate rezDatum, akutDatum;
	private int anzahl1, anzahl2, anzahl3, anzahl4, alter, behBeginn, frequenz;

	
	public HMRCheck2021(String diszi, String diagnosegr,
			String icd10_1, String icd10_2, String vorArt, String rezNR,
			String hm1, String hm2, String hm3, String hm4, 
			int anzahl1, int anzahl2, int anzahl3, int anzahl4, int alter,
			int behBeginn, LocalDate akutDatum, LocalDate rezDatum, int frequenz) {
		
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
		this.anzahl2 = anzahl2;
		this.anzahl3 = anzahl3;
		this.anzahl4 = anzahl4;
		
		this.frequenz = frequenz;
		
		this.behBeginn = behBeginn;
		this.alter = alter;
		
		this.akutDatum = akutDatum;
		this.rezDatum =  rezDatum;
		
		this.voArt = vorArt;
		this.icd10_1 = icd10_1;
		this.icd10_2 = icd10_2;
		
		this.rezNR = rezNR;
		
	}
	
	public void HMRCheck2021Exists(String reznr) {
		
	}
	
	public String isOkay() {
		
		String[] ign = {"X4002", "X0204", "X3011", "X3010", "X3008" };
		List<String> ignore = Arrays.asList(ign);
		String rueckgabe = "";
		
		// Prüfung ob ICD10-Codes gültig sind
		if(this.icd10_1.equals("")) {
			rueckgabe = rueckgabe + " Kein 1. ICD-10 angegeben \n";
		} else {
			String sqlICD1 = "SELECT * FROM icd10 WHERE schluessel1 LIKE '"+ this.icd10_1 +"'";
			Vector<Vector<String>> icd101exists = SqlInfo.holeFelder(sqlICD1);
			if(icd101exists.size() == 0) {
				rueckgabe = rueckgabe + " 1. ICD-10-Schlüssel "+this.icd10_1+" existiert nicht \n";
			}
		}
		
		if(!this.icd10_2.equals("")) {
			String sqlICD2 = "SELECT * FROM icd10 WHERE schluessel1 LIKE '"+ this.icd10_2 +"'";
			Vector<Vector<String>> icd102exists = SqlInfo.holeFelder(sqlICD2);
			if(icd102exists.size() == 0) {
				rueckgabe = rueckgabe + " 2. ICD-10-Schlüssel "+this.icd10_2+" existiert nicht \n";
			}
		}
		
		
		int summe = 0;
		int[] erlaubte = null;
		if(!AktuelleRezepte.isDentist(this.diagnosegruppe)) {
			erlaubte = Reha.hmrXML.getAnzahl(this.disziplin, this.diagnosegruppe);
			ArrayList<String> hmPos = Reha.hmrXML.getErlaubteVorrangigeHM(this.disziplin, this.diagnosegruppe);
			ArrayList<String> hmErgPos = Reha.hmrXML.getErlaubteErgaenzendeHM(this.disziplin, this.diagnosegruppe);

			// sind aktuelle HM erlaubt
			if(!hmPos.contains(hm1) && !ignore.contains(hm1)) { rueckgabe = rueckgabe +" Heilmittel: "+hm1+" nicht als vorrangiges Heilmittel erlaubt \n"; }
			if(!hmPos.contains(hm2) && !hm2.equals("") && !ignore.contains(hm2)) { rueckgabe = rueckgabe +" Heilmittel: "+hm2+" nicht als vorrangiges Heilmittel erlaubt \n";  }
			if(!hmPos.contains(hm3) && !hm3.equals("")&& !ignore.contains(hm3)) { rueckgabe = rueckgabe +" Heilmittel: "+hm3+" nicht als vorrangiges Heilmittel erlaubt \n";  }
			if(!hmErgPos.contains(hm4) && !hm4.equals("") && !ignore.contains(hm4)) { rueckgabe = rueckgabe +" Heilmittel: "+hm4+" nicht als erg. Heilmittel erlaubt \n";  }
			// wie viele HM sind pro VO erlaubt
			
			if(!hm1.equals("") && !ignore.contains(hm1)) {summe = summe + anzahl1;};
			if(!hm2.equals("") && !ignore.contains(hm2)) {summe = summe + anzahl2;};
			if(!hm3.equals("") && !ignore.contains(hm3)) {summe = summe + anzahl3;};			
		} else {
			rueckgabe = rueckgabe + " Zahnarzt-Verordnung erkannt \n";
		}

		
		// merker für Einheiten Prüfung
		boolean einheitenPrüfen = true;
		
		//liegt ein bvb / lhm vor?
		BVBlhmCheck checker = new BVBlhmCheck();
		String bvblhm = checker.LHMbvbKurzCheck(this.diagnosegruppe, this.icd10_1, this.icd10_2, this.alter, this.rezDatum, this.akutDatum);
		
		if(bvblhm != null) {
			if(bvblhm.equals("!DAT!")) {
				rueckgabe = rueckgabe + " Es liegt zwar ein BVB vor, aber das Akutereignis liegt zu lange in der Vergangenheit \n";
			}
			
			double sum = summe;
			double freq = frequenz;
			
			double wochen = Math.ceil(sum / freq);
			
			if(wochen > 12) {
				rueckgabe = rueckgabe + " 12 Wochenfrist bei BVB / LHM passt nicht \n";
			}
		}
		
		if(bvblhm == null) {
			bvblhm = "gar nichts";
		}
				
		// LHM ausgewählt, aber liegt auch einer vor?
		if(voArt.contains("Langfrist"))  {
			if(!bvblhm.equals("LHM")) {
				rueckgabe = rueckgabe + " Als Verordnungsart ist Langfrist-VO ausgewählt, aber es liegt keiner vor \n"
						+ "    Patientenindividuelle Genehmigung? \n";
			} else {
				einheitenPrüfen = false;
			}
		}
		
		// BVB ausgewählt, aber liegt auch einer vor?
		if(voArt.contains("Bes.Vo"))  {
			if(!bvblhm.equals("BVB")) {
				rueckgabe = rueckgabe + " Als Verordnungsart ist ein Besonderer Verordnungsbedarf ausgewählt, aber es liegt keiner vor \n";
			} else {
				einheitenPrüfen = false;
			}
		}
		
		if(einheitenPrüfen && !AktuelleRezepte.isDentist(this.diagnosegruppe))  {
			if(erlaubte[HmrCheck2021XML.cVOMEN] < summe) { rueckgabe = rueckgabe +" Pro Verordnung sind nur "+erlaubte[HmrCheck2021XML.cVOMEN]+" Behandlungseinheiten erlaubt \n";  };
		}
		
		
		if(anzahl4 > summe && !hm4.equals("")) { rueckgabe = rueckgabe + " Es sind mehr ergänzende Einheiten als vorrangige Einheiten verordnet \n"; };
		
		// behandlungsbeginn / erste behandlung prüfen
		LocalDate erstDatum = LocalDate.now();
		if(!rezNR.equals("")) {
			String erstbehandlung = RezTools.holeEinzelTermineAusRezept(this.rezNR, "")
	                .get(0);
			int akutJahr = Integer.parseInt(erstbehandlung.split("\\.")[2]);
			int akutMonat = Integer.parseInt(erstbehandlung.split("\\.")[1]);
			int akutTag = Integer.parseInt(erstbehandlung.split("\\.")[0]);
			
			erstDatum = LocalDate.of(akutJahr, akutMonat, akutTag);
		}
		
		if(erstDatum.isBefore(rezDatum) && this.rezNR.equals(""))  {
			rueckgabe = rueckgabe + " Zukunftsmodus an: Das Rezeptdatum liegt vor dem aktuellem Datum \n";
		} else if(erstDatum.isBefore(rezDatum)) {
			rueckgabe = rueckgabe + " Die 1. Behandlung hat vor dem Rezeptdatum stattgefunden \n";
		} else {
			LocalDate spätBegin = rezDatum.plusDays(this.behBeginn);
			if(spätBegin.isBefore(erstDatum)) {
				rueckgabe = rueckgabe + " Die 1. Behandlung liegt mehr als " + this.behBeginn + " Tage hinter dem Ausstellungsdatum \n";
			}
		}
		
		
		
		return rueckgabe;
	}
	
	
}
