package org.therapi.hmrCheck2021;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import hauptFenster.Reha;
import sqlTools.SqlInfo;





public class BVBCheck2021TableCreate {
	
	private File xml;
	private Document doc;
	private Element root;
	private Element verordnungsbedarfliste;
	
	private ArrayList<BVB> bvbs;
	private ArrayList<LHM> lhms;
	
	public static String cKG = "I";
	public static String cPO = "II";
	public static String cLO = "III";
	public static String cER = "IV";
	public static String cEN = "V";
	
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
	
	public BVBCheck2021TableCreate(File xml) {
		this.xml = xml;
		this.loadXML();
		
		this.bvbs = new ArrayList<BVB>();
		this.lhms = new ArrayList<LHM>();
		
		this.loadEntry();
		SqlInfo.sqlAusfuehren("UPDATE hmr_bvblhm\r\n"
    			+ "     , icd10 \r\n"
    			+ "    SET hmr_bvblhm.icd10_1_titel = icd10.titelzeile\r\n"
    			+ " WHERE hmr_bvblhm.icd10_1 = icd10.schluessel1;");
    	SqlInfo.sqlAusfuehren("UPDATE hmr_bvblhm\r\n"
    			+ "     , icd10 \r\n"
    			+ "    SET hmr_bvblhm.icd10_2_titel = icd10.titelzeile\r\n"
    			+ " WHERE hmr_bvblhm.icd10_2 = icd10.schluessel1;");
		System.out.println("BVBS: " + this.bvbs.size() + " LHMs: "+ this.lhms.size());
	}
	
	private void loadXML() {
		try {
			this.doc = new SAXBuilder().build(this.xml);
			this.root = doc.getRootElement();
			Element body =  this.root.getChild("body", this.root.getNamespace());
			Element sdhma = body.getChild("sdhma_stammdaten", body.getNamespace("sdhma_stammdaten"));
			this.verordnungsbedarfliste = sdhma.getChild("verordnungsbedarf_liste", sdhma.getNamespace("verordnungsbedarfliste"));
			
			
			
		} catch (JDOMException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void loadEntry() {
		for(Element verordnungsbedarf : this.verordnungsbedarfliste.getChildren()) {
			Element icd = verordnungsbedarf.getChild("icd_code", verordnungsbedarf.getNamespace());
			if(icd != null) {
				 String icd10_1 = icd.getAttributeValue("V");
				 Element heilmittel_liste = verordnungsbedarf.getChild("heilmittel_liste", verordnungsbedarf.getNamespace());
				 Element heilmittel = heilmittel_liste.getChild("heilmittel", heilmittel_liste.getNamespace());
				 String art = heilmittel.getChild("anlage_heilmittelvereinbarung", heilmittel.getNamespace()).getAttributeValue("V");
				 
				 Element icd_2 = heilmittel.getChild("sekundaercode", heilmittel.getNamespace());
				 String icd10_2 = "na";
				 if(icd_2 != null) { icd10_2 = icd_2.getAttributeValue("V");};
				 
				 Element zeitraum_akutereignis = heilmittel.getChild("zeitraum_akutereignis", heilmittel.getNamespace());
				 int zeitraumMonat = -1;
				 if(zeitraum_akutereignis != null) {
					 if(zeitraum_akutereignis.getAttributeValue("U").equals("Jahr")) { zeitraumMonat = 12;}
					 if(zeitraum_akutereignis.getAttributeValue("U").equals("Monat")) { zeitraumMonat = Integer.parseInt(zeitraum_akutereignis.getAttributeValue("V"));}
				 }
				 
				 Element untere_altersgrenze = heilmittel.getChild("untere_altersgrenze", heilmittel.getNamespace());
				 int unteresAlter = 0;
				 if(untere_altersgrenze != null) { unteresAlter = Integer.parseInt(untere_altersgrenze.getAttributeValue("V"));}
				 
				 Element obere_altersgrenze = heilmittel.getChild("obere_altersgrenze", heilmittel.getNamespace());
				 int oberesAlter = 200;
				 if(obere_altersgrenze != null) { oberesAlter = Integer.parseInt(obere_altersgrenze.getAttributeValue("V"));}
				 
				 Element kapitel_liste = heilmittel.getChild("kapitel_liste", heilmittel.getNamespace());
				 
				 for(Element kapitel : kapitel_liste.getChildren()) {
					 String diszi = kapitel.getAttributeValue("V");
					 Element diagnosegruppe_liste = kapitel.getChild("diagnosegruppe_liste", kapitel.getNamespace());
					 for(Element diagnosegruppe : diagnosegruppe_liste.getChildren()) {
						 String diaggruppe = diagnosegruppe.getAttributeValue("V");
						 String sql = null;
						 if(art.equals("BVB")) {
							sql = "INSERT INTO hmr_bvblhm SET "
									+ "diszi = '"+diszi+"', "
									+ "art = 'BVB', "
									+ "icd10_1 = '"+icd10_1+"', "
									+ "icd10_2 = '"+icd10_2+"', "
									+ "diagnosegruppe = '"+diaggruppe+"', "
									+ "akutMonat = "+zeitraumMonat+", "
									+ "oberesAlter = "+oberesAlter+", "
									+ "unteresAlter = "+unteresAlter+";";
							
						 } else if (art.equals("LHM")) {
							sql = "INSERT INTO hmr_bvblhm SET "
										+ "diszi = '"+diszi+"', "
										+ "art = 'LHM', "
										+ "icd10_1 = '"+icd10_1+"', "
										+ "icd10_2 = '"+icd10_2+"', "
										+ "diagnosegruppe = '"+diaggruppe+"', "
										+ "akutMonat = "+zeitraumMonat+", "
										+ "oberesAlter = "+oberesAlter+", "
										+ "unteresAlter = "+unteresAlter+";";
							
						 }
						 try {
							Reha.instance.conn.createStatement().execute(sql);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					 }
				 }
			}
		}
	}

}
