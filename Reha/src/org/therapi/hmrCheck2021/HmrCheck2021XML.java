package org.therapi.hmrCheck2021;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;





public class HmrCheck2021XML {
	
	private File xml;
	private Document doc;
	private Element root;
	private Element sdhm;
	
	public static String cKG = "I. Maßnahmen der Physiotherapie";
	public static String cPO = "II. Maßnahmen der Podologischen Therapie";
	public static String cLO = "III. Maßnahmen der Stimm-, Sprech-, Sprach- und Schlucktherapie";
	public static String cER = "IV. Maßnahmen der Ergotherapie";
	public static String cEN = "V. Maßnahmen der Ern�hrungstherapie";
	
	/*
	 * 0 = orientierende Menge.
	 * 1 = VO Menge.
	 * 2 = standard Menge
	 * 3 = MassageMenge
	 * 4 = hoechstalter.
	 * 5 = behandlungsmenge hoechstalter.
	 * 6 = hoechtsmenge icd.
	 */
	
	public static int cORIMEN = 0;
	public static int cVOMEN = 1;
	public static int cSTDMEN = 2;
	public static int cMASMEN = 3;
	public static int cHOECHSTALTER = 4;
	public static int cMENHOECHSTALTER = 5;
	public static int cMENICD = 6;
	
	public HmrCheck2021XML(File xml) {
		this.xml = xml;
		this.loadXML();
	}
	
	private void loadXML() {
		try {
			this.doc = new SAXBuilder().build(this.xml);
			this.root = doc.getRootElement();
			Element body =  this.root.getChild("body", this.root.getNamespace());
			this.sdhm = body.getChild("sdhm_stammdaten", body.getNamespace("sdhm_stammdaten"));
		} catch (JDOMException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public ArrayList<String> getErlaubteVorrangigeHM(String diszi, String diagnosegruppe) {
		ArrayList<String> rueckgabe = new ArrayList<String>();
		Element kapitel = null;
		for(Element c : this.sdhm.getChildren()) {
			if(c.getAttribute("V").getValue().equals(diszi)) {
				kapitel = c;
			}
		}
		Element diagnosegr = null;
		for(Element c : kapitel.getChildren()) {
			if(c.getAttributeValue("V").equals(diagnosegruppe)) {
				diagnosegr = c;
			}
		}
		Element e = diagnosegr.getChild("heilmittelverordnung", diagnosegr.getNamespace())
				.getChild("vorrangiges_heilmittel_liste", diagnosegr.getNamespace());
		for(Element c : e.getChildren()) {
			String positionsnr = c.getChild("positionsnr_liste", c.getNamespace()).getChild("positionsnr", c.getNamespace()).getAttributeValue("V");
			rueckgabe.add(positionsnr);
		}
		return rueckgabe;
	}
	
	public ArrayList<String> getErlaubteErgaenzendeHM(String diszi, String diagnosegruppe) {
		ArrayList<String> rueckgabe = new ArrayList<String>();
		Element kapitel = null;
		for(Element c : this.sdhm.getChildren()) {
			if(c.getAttribute("V").getValue().equals(diszi)) {
				kapitel = c;
			}
		}
		Element diagnosegr = null;
		for(Element c : kapitel.getChildren()) {
			if(c.getAttributeValue("V").equals(diagnosegruppe)) {
				diagnosegr = c;
			}
		}
		Element e = diagnosegr.getChild("heilmittelverordnung", diagnosegr.getNamespace())
				.getChild("ergaenzendes_heilmittel_liste", diagnosegr.getNamespace());
		if(e != null) {
			for(Element c : e.getChildren()) {
				String positionsnr = c.getChild("positionsnr_liste", c.getNamespace()).getChild("positionsnr", c.getNamespace()).getAttributeValue("V");
				rueckgabe.add(positionsnr);
			}
		}
		
		return rueckgabe;
	}
	
	
	public int[] getAnzahl(String diszi, String diagnosegruppe) {
		int rueckgabe[] = {0, 0, 0, 0, 0, 0, 0};
		Element kapitel = null;
		for(Element c : this.sdhm.getChildren()) {
			if(c.getAttribute("V").getValue().equals(diszi)) {
				kapitel = c;
			}
		}
		Element diagnosegr = null;
		for(Element c : kapitel.getChildren()) {
			if(c.getAttributeValue("V").equals(diagnosegruppe)) {
				diagnosegr = c;
			}
		}
		Element e = diagnosegr.getChild("heilmittelverordnung", diagnosegr.getNamespace())
				.getChild("verordnungsmenge", diagnosegr.getNamespace());
		
		rueckgabe[cVOMEN] = Integer.valueOf(e.getChild("hoechstmenge_verordnung", e.getNamespace()).getAttributeValue("V"));
		rueckgabe[cORIMEN] = Integer.valueOf(e.getChild("orientierende_behandlungsmenge", e.getNamespace()).getAttributeValue("V"));
		
		//ICD-10 VO-Menge puffern
		Element icd = e.getChild("orientierende_behandlungsmenge", e.getNamespace()).getChild("orientierende_behandlungsmenge_icd_code", 
				e.getNamespace());
		if(icd != null) {
			rueckgabe[cMENICD] = Integer.valueOf(icd.getAttributeValue("V"));
		}
		
		//Altersabh�ngige H�chstmenge mit alter
		Element altersAbhaengige = e.getChild("orientierende_behandlungsmenge", e.getNamespace()).getChild("orientierende_behandlungsmenge_hoechstalter", 
				e.getNamespace());
		if(altersAbhaengige != null) {
			rueckgabe[cMENHOECHSTALTER] = Integer.valueOf(altersAbhaengige.getAttributeValue("V"));
		}
		altersAbhaengige = e.getChild("orientierende_behandlungsmenge", e.getNamespace()).getChild("hoechstalter_jahre", 
				e.getNamespace());
		if(altersAbhaengige != null) {
			rueckgabe[cHOECHSTALTER] = Integer.valueOf(altersAbhaengige.getAttributeValue("V"));
		}
		
		// standard
		Element standard = e.getChild("orientierende_behandlungsmenge_standardisiert", e.getNamespace());
		if(standard != null) {
			rueckgabe[cSTDMEN] = Integer.valueOf(standard.getAttributeValue("V"));
		}
		
		// massage
		Element massage = e.getChild("orientierende_behandlungsmenge_massage", e.getNamespace());
		if(standard != null) {
			rueckgabe[cMASMEN] = Integer.valueOf(massage.getAttributeValue("V"));
		}
		
		return rueckgabe;
	}
	
	public ArrayList<String> getICDCodes(String diszi, String diagnosegruppe) {
		ArrayList<String> rueckgabe = new ArrayList<String>();
		
		Element kapitel = null;
		for(Element c : this.sdhm.getChildren()) {
			if(c.getAttribute("V").getValue().equals(diszi)) {
				kapitel = c;
			}
		}
		Element diagnosegr = null;
		for(Element c : kapitel.getChildren()) {
			if(c.getAttributeValue("V").equals(diagnosegruppe)) {
				diagnosegr = c;
			}
		}
		Element voMenge = diagnosegr.getChild("heilmittelverordnung", diagnosegr.getNamespace())
				.getChild("verordnungsmenge", diagnosegr.getNamespace())
				.getChild("orientierende_behandlungsmenge", diagnosegr.getNamespace());
		
		Element icdListe = voMenge.getChild("icd_code_liste", voMenge.getNamespace());
		
		if(icdListe != null) {
			List<Element> icdCodes = icdListe.getChildren("icd_code", voMenge.getNamespace());
			for(Element icd : icdCodes) {
				System.out.println(icd.getAttributeValue("V"));
				rueckgabe.add(icd.getAttributeValue("V"));
			}
		}
		
		return rueckgabe;
	}


}