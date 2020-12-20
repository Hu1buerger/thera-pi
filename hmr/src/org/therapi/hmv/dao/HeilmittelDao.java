package org.therapi.hmv.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.therapi.hmv.entities.Diagnosegruppe;

import core.Disziplin;
import hmv.Heilmittel;
import mandant.IK;
import sql.DatenquellenFactory;

public class HeilmittelDao {
	private static final String SQL = "SELECT heilmittel,heilmittel_beschreibung,heilmittelposition ,dauer_von,dauer_bis, vorrangig,Maximal,kuerzel AS diszi FROM hmr_heilmittel JOIN hmr_disziplin ON kennung = hmr_heilmittel.hmr_disziplin  " ;
	private static final Logger LOGGER = LoggerFactory.getLogger(HeilmittelDao.class);
	private DatenquellenFactory DQ;

	public HeilmittelDao(IK ik) {
		DQ = new DatenquellenFactory(ik.digitString());
	}

	public List<Heilmittel> all(){
		try(ResultSet rs = DQ.createConnection().createStatement().executeQuery(SQL)) {
			List<Heilmittel> all = new LinkedList<>();
			while(rs.next()) {
				all.add(byResultset(rs));
			}
				return all;
			
			
		} catch (SQLException e) {
			LOGGER.debug("error loading all Diagnosegruppen",e);
			return java.util.Collections.emptyList();
		}
		
		
	}

	
	
	private Heilmittel byResultset(ResultSet rs) throws SQLException {
		
		
		String heilmittel                     = rs.getString("heilmittel");  
		String heilmittel_beschreibung       = rs.getString("heilmittel_beschreibung");  
		String hmposition                        = rs.getString("heilmittelposition"); 
		int dauer_von                         = rs.getInt("dauer_von");  
		int dauer_bis                         = rs.getInt("dauer_bis");  
		boolean vorrangig                         = rs.getBoolean("vorrangig"); 
		int max                         = rs.getInt("Maximal");  
		Disziplin diszi                        = Disziplin.valueOf( rs.getString("diszi"));
		return new Heilmittel(heilmittel, 
				heilmittel_beschreibung,
				hmposition,
				dauer_von,
				dauer_bis,
				vorrangig, 
				max,
				diszi);
	}
	
}
