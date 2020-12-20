package org.therapi.hmv.entities;

import java.util.LinkedList;
import java.util.List;

import core.Disziplin;

public class Diagnosegruppe {
	final public String diagnosegruppe;
	final public String diagnosegruppe_beschreibung;
	final public String leitsymptomatik;
	final public String leitsymptomatik_beschreibung;
	final public Disziplin diszi;

	final static	public List<Diagnosegruppe> pool = new LinkedList<>();
	
	static public final void setContent(List<Diagnosegruppe> newContent) {
		pool.clear();
		pool.addAll(newContent);
	}
	
	public Diagnosegruppe(String diagnosegruppe, String diagnosegruppe_beschreibung, String leitsymptomatik,
			String leitsymptomatik_beschreibung, Disziplin diszi) {
		this.diagnosegruppe = diagnosegruppe;
		this.diagnosegruppe_beschreibung = diagnosegruppe_beschreibung;
		this.leitsymptomatik = leitsymptomatik;
		this.leitsymptomatik_beschreibung = leitsymptomatik_beschreibung;
		this.diszi = diszi;
	}

	public Diagnosegruppe(String diagnosegruppe2, String diagnosegruppe_beschreibung2, String leitsymptomatik2,
			String leitsymptomatik_beschreibung2, String string) {
		this(diagnosegruppe2,diagnosegruppe_beschreibung2,leitsymptomatik2,leitsymptomatik_beschreibung2, Disziplin.valueOf(string));
	}
	@Override
	public String toString() {
		return "Diagnosegruppe [diagnosegruppe=" + diagnosegruppe + ", diagnosegruppe_beschreibung="
				+ diagnosegruppe_beschreibung + ", leitsymptomatik=" + leitsymptomatik
				+ ", leitsymptomatik_beschreibung=" + leitsymptomatik_beschreibung + ", diszi=" + diszi + "]";
	}
}
