package patientenFenster;

import java.sql.Connection;

import hauptFenster.AktiveFenster;
import hauptFenster.Reha;

import javax.swing.JComponent;
import javax.swing.SwingWorker;

import org.therapi.reha.patient.AktuelleRezepte;

import com.sun.star.uno.Exception;

import events.PatStammEvent;
import events.PatStammEventClass;

public class PatUndVOsuchen{
	public static void doPatSuchen(String patint,String reznr,Object source, Connection connection){
		Connection connection1 =connection;
		String pat_int;
		pat_int = patint;
		JComponent patient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
		final String xreznr = reznr;
		if(patient == null){
			final String xpat_int = pat_int;
			final Object xsource = source;
			new SwingWorker<Void,Void>(){
				@Override
                protected Void doInBackground() throws Exception {
					JComponent xpatient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
					Reha.instance.progLoader.ProgPatientenVerwaltung(1,connection1);
					while( (xpatient == null) ){
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						xpatient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
					}
					while(  (!AktuelleRezepte.initOk) ){
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					String s1 = "#PATSUCHEN";
					String s2 = xpat_int;
					PatStammEvent pEvt = new PatStammEvent(xsource);
					pEvt.setPatStammEvent("PatSuchen");
					pEvt.setDetails(s1,s2,"#REZHOLEN-"+xreznr) ;
					PatStammEventClass.firePatStammEvent(pEvt);
					return null;
				}

			}.execute();
		}else{
			Reha.instance.progLoader.ProgPatientenVerwaltung(1,connection1);
			String s1 = "#PATSUCHEN";
			String s2 = pat_int;
			PatStammEvent pEvt = new PatStammEvent(source);
			pEvt.setPatStammEvent("PatSuchen");
			pEvt.setDetails(s1,s2,"#REZHOLEN-"+xreznr) ;
			PatStammEventClass.firePatStammEvent(pEvt);
		}
	}
}