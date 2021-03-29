package org.therapi.reha.patient.verlauf;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sql.DatenquellenFactory;

public class TerminGate {
    private static final String REZEPT_NR_SPALTE = "REZ_NR";
    private static final String TERMIN_SPALTE = "TERMINE";
    Logger logger = LoggerFactory.getLogger(TerminGate.class);
    private DatenquellenFactory ds;

    public TerminGate(DatenquellenFactory ds2) {
        this.ds = ds2;

    }

    List<Termin> findByPatientId(int pat_id) {
        List<Termin> liste = new LinkedList<Termin>();
        String findSQL = "Select * from verordn WHERE patId = ";

        try (Statement statement = ds.createConnection()
                                     .createStatement();
                ResultSet rs = statement.executeQuery(findSQL + pat_id);) {

            while (rs.next()) {

                String termine = rs.getString(TERMIN_SPALTE);
                String rezeptNr = rs.getString(REZEPT_NR_SPALTE);
                liste.addAll(new TerminMapper(termine, pat_id, rezeptNr).termine());

            }
        } catch (SQLException e) {
            logger.error("trying to find Patient ID " + pat_id, e);
        }
        return liste;
    }

}
