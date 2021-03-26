package org.therapi.hmrCheck2021;

import java.time.LocalDate;

public class BVB {
	
	private int akutMonat, unteresAlter, oberesAlter;
	private String icd10_1, icd10_2, diagnosegruppe, diszi;
	private LocalDate akutEreignis;
	

	BVB(String diszi, String icd10_1, String icd10_2, String diagnosegruppe, int akutMonat, int unteresAlter, int oberesAlter) {
		this.diszi = diszi;
		this.icd10_1 = icd10_1;
		this.icd10_2 = icd10_2;
		this.diagnosegruppe = diagnosegruppe;
		this.akutMonat = akutMonat;
		this.oberesAlter = oberesAlter;
		this.unteresAlter = unteresAlter;
	}

	public String getDiszi() {
		return diszi;
	}

	public int getAkutMonat() {
		return akutMonat;
	}

	public String getIcd10_1() {
		return icd10_1;
	}

	public String getIcd10_2() {
		return icd10_2;
	}

	public String getDiagnosegruppe() {
		return diagnosegruppe;
	}

	public LocalDate getAkutEreignis() {
		return akutEreignis;
	}
	
	public int getUnteresAlter() {
		return unteresAlter;
	}

	public int getOberesAlter() {
		return oberesAlter;
	}
}