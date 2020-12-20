package org.therapi.hmv.dao;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.therapi.hmv.entities.Diagnosegruppe;

import mandant.IK;

public class DiagnosegruppeDaoTest {

	@Test
	public void allReturnsEmptyListWhenThingsGoWrong() {
		DiagnosegruppeDao dgdao = new DiagnosegruppeDao(new IK("123456789"));
		assertEquals(236, dgdao.all().size());
	}

	private List<Diagnosegruppe> data() {
		List<Diagnosegruppe> liste = new LinkedList<>();
		liste.add(new Diagnosegruppe("WS","Wirbelsäulenerkrankung","a","Schädigung der Bewegungssegmente","MA"                                                                                               ) );
		liste.add(new Diagnosegruppe("WS","Wirbelsäulenerkrankung","b","Schädigung/Störung der Muskelfunktion","KG"                                                                                          ) );
		liste.add(new Diagnosegruppe("WS","Wirbelsäulenerkrankung","x","patientenindividuelle Symptomatik","KG"                                                                                              ) );
		liste.add(new Diagnosegruppe("EX","Erkrankung der Extremitäten und des Beckens","a","Schädigung/Störung der Gelenkfunktion","KG"                                                                     ) );
		liste.add(new Diagnosegruppe("EX","Erkrankung der Extremitäten und des Beckens","b","Schädigung/Störung der Muskelfunktion","KG"                                                                     ) );
		liste.add(new Diagnosegruppe("EX","Erkrankung der Extremitäten und des Beckens","x","patientenindividuelle Symptomatik","KG"                                                                         ) );
		liste.add(new Diagnosegruppe("CS","Chronifiziertes Schmerzsyndrom","a","chronische Schmerzen","KG"                                                                                                   ) );
		liste.add(new Diagnosegruppe("CS","Chronifiziertes Schmerzsyndrom","x","patientenindividuelle Symptomatik","KG"                                                                                      ) );
		liste.add(new Diagnosegruppe("ZN","ZNS-Erkrankungen einschließlich des Rückenmarks/Neuromuskuläre Erkrankungen","a","Schädigung/Störung der Bewegungs- und Sinnesfunktion","KG"                      ) );
		liste.add(new Diagnosegruppe("ZN","ZNS-Erkrankungen einschließlich des Rückenmarks/Neuromuskuläre Erkrankungen","b","Schädigung/Störung der Muskelfunktion","KG"                                     ) );
		liste.add(new Diagnosegruppe("DF","diabetische Neuropathie mit oder ohne Angiopathie","a","Hyperkatose (schmerzhaft oder schmerzlos)","PO"                                                           ) );
		liste.add(new Diagnosegruppe("PN","Perophere Nervenläsionen Muskelerkrankungen","a","Schädigung/Störung der Bewegungsfunktion","KG"                                                                  ) );
		liste.add(new Diagnosegruppe("PN","Perophere Nervenläsionen Muskelerkrankungen","b","Schädigung/Störung der Muskelfunktion","KG"                                                                     ) );
		liste.add(new Diagnosegruppe("PN","Perophere Nervenläsionen Muskelerkrankungen","x","patientenindividuelle Symptomatik","KG"                                                                         ) );
		liste.add(new Diagnosegruppe("AT","Störung der Atmung","x","patientenindividuelle Symptomatik","KG"                                                                                                  ) );
		liste.add(new Diagnosegruppe("AT","Störung der Atmung","a","Schädigung/Störung der Atmungsfunktion","KG"                                                                                             ) );
		liste.add(new Diagnosegruppe("AT","Störung der Atmung","b","Schädigung/Störung der Atemmuskulatur (einschließlich Zwerchfell und Atemhilfsmuskulatur)","KG"                                           ) );
		liste.add(new Diagnosegruppe("GE","Arterielle Gefäßerkrankungen (bei konventioneller Behandlung, nach interventioneller/operativer Behandlung)","a","Schmerzen der Extremitäten","KG"                ) );
		liste.add(new Diagnosegruppe("GE","Arterielle Gefäßerkrankungen (bei konventioneller Behandlung, nach interventioneller/operativer Behandlung)","x","patientenindividuelle Symptomatik","KG"         ) );
		liste.add(new Diagnosegruppe("GE","Arterielle Gefäßerkrankungen (bei konventioneller Behandlung, nach interventioneller/operativer Behandlung)","b","Schädigung/Störung der Muskelfunktion","KG"     ) );
		liste.add(new Diagnosegruppe("LY","Lymphabflusstörungen","a","Schädigung der Lymphgefäße, Lymphknoten, Kapiliaren","KG"                                                                              ) );	// TODO Auto-generated method stub
		liste.add(new Diagnosegruppe("EN1","ZNS-Erkrankungen (Gehirn) Entwicklungsstörungen","a","Schädigung der Bewegungsfunktionen","ER"));
		liste.add(new Diagnosegruppe("EN1","ZNS-Erkrankungen (Gehirn) Entwicklungsstörungen","b","Schädigung der Sinnesfunktionen","ER"));
		liste.add(new Diagnosegruppe("EN1","ZNS-Erkrankungen (Gehirn) Entwicklungsstörungen","c","Schädigung der mentalen Funktionen","ER"));
		liste.add(new Diagnosegruppe("EN1","ZNS-Erkrankungen (Gehirn) Entwicklungsstörungen","x","patientenindividuelle Symptomatik]","ER"));
		liste.add(new Diagnosegruppe("EN2","ZNS-Erkrankungen (Rückenmark)/ Neuromuskuläre Erkrankungen","a","Schädigung der Bewegungsfunktionen","ER"));
		liste.add(new Diagnosegruppe("EN2","ZNS-Erkrankungen (Rückenmark)/ Neuromuskuläre Erkrankungen","b","Schädigung der Sinnesfunktionen","ER"));
		liste.add(new Diagnosegruppe("EN2","ZNS-Erkrankungen (Rückenmark)/ Neuromuskuläre Erkrankungen","c","Schädigung der mentalen Funktionen","ER"));
		return liste;
	}

}
