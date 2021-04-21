package org.therapi.hmrCheck2021;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Vector;

import CommonTools.SqlInfo;

public class BVBlhmCheck {
	
	public String LHMbvbKurzCheck(String diag, String icd10_1, String icd10_2, int alter, LocalDate rezDatum, LocalDate akutDatum) {
		String rueckgabe = null;
		if(icd10_2.equals("")) { icd10_2 = "na";}
		
		String sql = "SELECT art, akutMonat FROM hmr_bvblhm WHERE "
				+ "diagnosegruppe LIKE '"+diag+"' AND "
				+ "icd10_1 LIKE '"+icd10_1+"' AND "
				+ "icd10_2 LIKE '"+icd10_2+"' AND "
				+ "(oberesAlter >= "+alter+" OR unteresAlter <= "+alter+");";
		
		Vector<Vector<String>> result = SqlInfo.holeFelder(sql);
		
		
		if(result.size() == 1) {
			String art = result.get(0).get(0);
			int akutMonat = Integer.parseInt(result.get(0).get(1));
			if(akutMonat == -1 ) {
				return art;
			} else {
				long monthsBetween = ChronoUnit.MONTHS.between(akutDatum, rezDatum);
				if(monthsBetween > akutMonat) {
					return "!DAT!";
				} else {
					return art;
				}
			}
		}
		
		return rueckgabe;
	}
	
	public String LHMbvbCheck(String diag, String icd10_1, String icd10_2, int alter) {
		String rueckgabe = null;
		
		String sql = "SELECT * FROM hmr_bvblhm WHERE "
				+ "diagnosegruppe LIKE '"+diag+"' AND "
				+ "icd10_1 LIKE '"+icd10_1+"' AND "
				+ "icd10_2 LIKE '"+icd10_2+"';";
		
		
		
		
		Vector<Vector<String>> result = SqlInfo.holeFelder(sql);
		if(result.size() == 1) {
			Vector<String>  row = result.get(0);
			String art = row.get(2);
			int untereGrenze = Integer.parseInt(row.get(8));
			int obereGrenze = Integer.parseInt(row.get(7));
			int akut = Integer.parseInt(row.get(6));
			if(alter <= obereGrenze && alter >= untereGrenze && akut == -1) {
				return art;
			} else if(alter <= obereGrenze && alter >= untereGrenze && akut != -1) {
				return art+"+DAT!";				
			}
		}
		
		String icd10_1_abfrage = icd10_1, icd10_2_abfrage = icd10_2;
		if(icd10_1.length() > 2) {
			icd10_1_abfrage = icd10_1.substring(0, 3);
		}
		icd10_1_abfrage = icd10_1_abfrage+"%";
		
		if(!icd10_2.equals("na") && icd10_2.length() > 2) {
			icd10_2_abfrage = icd10_2.substring(0, 3);
			icd10_2_abfrage = icd10_2_abfrage+"%";
		} else {
			icd10_2_abfrage = "egal";
		}
		

		sql = "SELECT hmr_bvblhm.art"
				+ "     , hmr_bvblhm.icd10_1"
				+ "	  , hmr_bvblhm.icd10_2"
				+ "	  , hmr_bvblhm.akutMonat"
				+ "	  , hmr_bvblhm.oberesAlter"
				+ "	  , hmr_bvblhm.unteresAlter"
				+ "	  , hmr_bvblhm.icd10_1_titel"
				+ "	  , hmr_bvblhm.icd10_2_titel"
				+ "   FROM hmr_bvblhm"
				+ "  WHERE (hmr_bvblhm.icd10_1 LIKE '"+icd10_1_abfrage+"' OR hmr_bvblhm.icd10_2 LIKE '"+icd10_1_abfrage+"' OR " 
				+ "				hmr_bvblhm.icd10_1 LIKE '"+icd10_2_abfrage+"' OR hmr_bvblhm.icd10_2 LIKE '"+icd10_2_abfrage+"')"
				+ "   AND diagnosegruppe LIKE '"+diag+"'"
				+ "   AND (oberesAlter >= "+alter+" "
				+ "   AND unteresAlter <= "+alter+");";
		
		
		
		result = SqlInfo.holeFelder(sql);
		if(result.size() >= 1) {
			StringBuffer rWert = new StringBuffer();
			rWert.append("<html>");
			rWert.append("<h1>Es liegt zwar kein BVB / LHM vor.<br />");
			rWert.append("Aber vielleicht ist eine Anpassung auf folgende Kombination denkbar:<br /></h1>");
			for(Vector<String> pos : result) {
				
				String art = pos.get(0); 
				String icd10_1_result = pos.get(1);
				String icd10_2_result = pos.get(2);
				String akutMonat = pos.get(3);
				String oberesAlter = pos.get(4);
				String unteresAlter = pos.get(5);
				String icd10_1_titel = pos.get(6);
				String icd10_2_titel = pos.get(7);
				boolean marker = false;
				if((icd10_1_result.equals(icd10_1) || icd10_2_result.equals(icd10_2)) && !icd10_2_result.equals("na")) {
					rWert.append("<b><font color=\"red\">");
					marker = true;
				}
				rWert.append("Art: "+art+" ");
				rWert.append("1. ICD10: ");
				rWert.append(icd10_1_result);
				rWert.append(" (");
				rWert.append(icd10_1_titel);
				rWert.append(") \n");
				rWert.append("   2. ICD10: ");
				String icd2 = pos.get(4);
				if(icd10_2_result.equals("na")) {
					icd2 = "nicht notwendig";
				} else {
					rWert.append(icd10_2_result);
					rWert.append(" (");
					rWert.append(icd10_2_titel);
					rWert.append(")");
				}
				
				if(!akutMonat.equals("-1")) {
					rWert.append("\n   Akutereignis innerhalb der letzten "+akutMonat+" Monate.");
				}
				
				if(!oberesAlter.equals("200") || !unteresAlter.equals("0")) {
					rWert.append("\n    Alter muss zwischen "+unteresAlter+" und "+oberesAlter+" liegen.");
				}
				
				if(marker) {
					rWert.append("</b></font>");
				}
				rWert.append("<br />");
			}
			return rWert.toString();
		}
		
		
		return rueckgabe;
	}

}
