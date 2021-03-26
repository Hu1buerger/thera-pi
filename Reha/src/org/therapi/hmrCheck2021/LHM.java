package org.therapi.hmrCheck2021;

public class LHM {
	
	private int unteresAlter, oberesAlter;
	private String icd10, diagnosegruppe, diszi;
	
	LHM(String diszi, String icd10, String diagnosegruppe, int unteresAlter, int oberesAlter) {
		this.diszi = diszi;
		this.icd10 = icd10;
		this.diagnosegruppe = diagnosegruppe;
		this.unteresAlter = unteresAlter;
		this.oberesAlter = oberesAlter;
	}

	public String getDiszi() {
		return diszi;
	}

	public String getIcd10() {
		return icd10;
	}

	public String getDiagnosegruppe() {
		return diagnosegruppe;
	}
	
	public int getUnteresAlter() {
		return unteresAlter;
	}

	public int getOberesAlter() {
		return oberesAlter;
	}

}
