package org.therapi.hmv.dao;

import CommonTools.DateTimeFormatters;
import hmv.Hmv;

public class HmvDTO {
	private final int id;
	private final String disziplin;
	private final int nummer;
	private final int pat_id;
	private final int kk_id;
	private final String betriebs_nr;
	private final int arzt_id;
	private final String pat_vers_nummer;
	private final int pat_vers_status;
	private final String datum;
	private final String icd10_1;
	private final String icd10_2;
	private final String icd10_text;
	private  int leitsymptomatik;                //must be final
	private  String leitsymptomatik_text;        //must be final
	private  int frequenz_von;                   //must be final
	private  int frequent_bis;                   //must be final
	private  int dauer;                          //must be final
	private  int angelegt_von;                   //must be final
	
	
	public HmvDTO(Hmv hmv) {
		id = hmv.id;
		disziplin = hmv.disziplin.toString();
		nummer= hmv.nummer.ziffern;
		pat_id = hmv.patient().db_id;
		kk_id = hmv.kv.getKk().get().getId();
		betriebs_nr = hmv.arzt.getBsnr();
		arzt_id = hmv.arzt.getId();
		pat_vers_nummer = hmv.kv.getVersicherungsnummer();
		pat_vers_status=hmv.kv.getStatus().getNummer();
		datum =  hmv.ausstellungsdatum.format(DateTimeFormatters.yyyyMMddmitBindestrich);
		icd10_1 = hmv.diag.icd10_1.schluessel;
		icd10_2 = hmv.diag.icd10_2.schluessel;
		icd10_text=hmv.diag.text;
		//TODO:: rest of fields
		
		
		
	}

}
