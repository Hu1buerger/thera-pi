package org.therapi.hmv.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.therapi.hmv.entities.Diagnosegruppe;

import core.Disziplin;
import mandant.IK;
import sql.DatenquellenFactory;

public class DiagnosegruppeDao {
	private static final String SQL = "SELECT diagnosegruppe,diagnosegruppe_beschreibung AS dgbeschreibung,leitsymptomatik,leitsymptomatik_beschreibung AS lsbeschreibung,  kuerzel AS diszi FROM hmr_diagnosegruppe JOIN hmr_disziplin ON kennung = hmr_tarif " ;
	private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosegruppeDao.class);
	private DatenquellenFactory DQ;

	public DiagnosegruppeDao(IK ik) {
		DQ = new DatenquellenFactory(ik.digitString());
	}

	public List<Diagnosegruppe> all(){
		try(ResultSet rs = DQ.createConnection().createStatement().executeQuery(SQL)) {
			List<Diagnosegruppe> all = new LinkedList<>();
			while(rs.next()) {
				all.add(byResultset(rs));
			}
				return all;
			
			
		} catch (SQLException e) {
			LOGGER.debug("error loading all Diagnosegruppen",e);
			return java.util.Collections.emptyList();
		}
		
		
	}

	
	
	private Diagnosegruppe byResultset(ResultSet rs) throws SQLException {
		
		
		String    diagnosegruppe               = rs.getString("diagnosegruppe");   
		String    diagnosegruppe_beschreibung  = rs.getString("dgbeschreibung"); 
		String    leitsymptomatik              = rs.getString("leitsymptomatik"); 
		String    leitsymptomatik_beschreibung = rs.getString("lsbeschreibung"); 
		Disziplin diszi                        = Disziplin.valueOf( rs.getString("diszi"));
		return new Diagnosegruppe(diagnosegruppe,diagnosegruppe_beschreibung,leitsymptomatik,leitsymptomatik_beschreibung,diszi);
	}
	
}
