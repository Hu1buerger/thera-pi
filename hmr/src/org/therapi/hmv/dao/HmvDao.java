package org.therapi.hmv.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Disziplin;
import hmv.Hmv;
import mandant.IK;
import sql.DatenquellenFactory;

public class HmvDao {
	private final DatenquellenFactory DQ;
	private final IK ik;

	public HmvDao(IK ik) {
		this.ik = ik;
		DQ = new DatenquellenFactory(ik.digitString());
	}
	private static final Logger LOGGER = LoggerFactory.getLogger(HmvDao.class);
	
	
	boolean save(Hmv hmv) {
			  String sql;
		        if (hmv.isNew()) {
		        	hmv.setHmvNummer(nextHmvNumber(hmv.disziplin));
		            sql = generateInsertSQL(hmv);

		        } else {
		            sql = generateUpdateSQL(hmv);
		        }

		        try (Connection con = new DatenquellenFactory(ik.digitString()).createConnection();
		                Statement stmt = con.createStatement()) {
		            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		            ResultSet rs = stmt.getGeneratedKeys();
		            if (rs.next()) {
		                hmv.id = rs.getInt(1);
		            }
		            if (rs.getWarnings() != null) {
		                LOGGER.debug(String.valueOf(rs.getWarnings()));
		            }
		        } catch (SQLException e) {
		            LOGGER.error("coud not save Mitarbeiter " + hmv, e);
		            return false;
		        }
		        return true;
		
		

	}
	
	
	
	
	
    private String generateInsertSQL(Hmv hmv) {
        String sqlStart = "INSERT INTO kollegen2 ( ANREDE, VORNAME , NACHNAME, STRASSE , PLZ , ORT , TELEFON1 , TELFON2, GEBOREN , matchcode , ZTEXT , KAL_TEIL , PERS_NR , ASTUNDEN, NICHT_ZEIG , ABTEILUNG , DEFTAKT , KALZEILE ) VALUES (";
    return "notyetdone";
    }

    private String generateUpdateSQL(Hmv hmv) {
      return "notyetdone";
    }

    private String einklammern(Date value) {
        return value == null ? null : "'" + String.valueOf(value) + "'";
    }

    private Integer einklammern(int invalue) {

        return invalue == 0 ? null : Integer.valueOf(invalue);
    }

    private String einklammern(String value) {
        return value == null ? null : "'" + value + "'";
    }





	public int nextHmvNumber(Disziplin diszi) {
		String column = diszi.toString().toLowerCase();
		String updateSql = "UPDATE nummern set " + column + "= "+ column +"+1 where mandant like '"
				+ ik.digitString() + "'";
		String selectSql = "select " + column + " from nummern  where mandant like '" + ik.digitString() + "'";
		try (Connection conn = DQ.createConnection()) {
			conn.setAutoCommit(false);
			Statement stmt = conn.createStatement();
			stmt.execute(updateSql);
			ResultSet rs =	stmt.executeQuery(selectSql);
			
			conn.commit();
			conn.setAutoCommit(true);
			if (rs.next())
				return rs.getInt(column);
		} catch (SQLException e) {
			
			LOGGER.debug("update reznummern fehlgeschlagen", e);
		}
		return -1;
	

	}
}
