package dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sql.DatenquellenFactory;

public class hmr_Diagnosegruppe_dao {
    public hmr_Diagnosegruppe_dao(DatenquellenFactory dQ) {
        super();
        DQ = dQ;
    }
    private DatenquellenFactory DQ;
    private static final Logger LOGGER = LoggerFactory.getLogger(Hmr_diagnosegruppe.class);
    private static final String SQL = "Select "+
            "id," +
            "diagnosegruppe," +
            "diagnosegruppe_beschreibung," +
            "anzahl_vo," +
            "max_behandlungen," +
            "gueltig_von," +
            "gueltig_bis," +
            "ccid from hmr_diagnosegruppe;"



            ;

    public    List<Hmr_diagnosegruppe> all (){

        try (ResultSet rs = DQ.createConnection().createStatement().executeQuery(SQL)) {
            List<Hmr_diagnosegruppe> all = new LinkedList<>();
            while (rs.next()) {
                try {
                all.add(byResultset(rs));
                } catch (Exception inner){
                    LOGGER.error("fetching line from hmr_diagnose", inner);
                }
            }
            return all;

        } catch (SQLException e) {
            LOGGER.debug("error loading all Diagnosegruppen", e);
            return java.util.Collections.emptyList();
        }

    }
    private Hmr_diagnosegruppe byResultset(ResultSet rs) throws SQLException {
        Hmr_diagnosegruppe diagnosegruppe = new Hmr_diagnosegruppe();
       diagnosegruppe.id    = rs.getInt("id");
       diagnosegruppe.diagnosegruppe= rs.getString("diagnosegruppe");
       diagnosegruppe.diagnosegruppe_beschreibung= rs.getString("diagnosegruppe_beschreibung");
       diagnosegruppe.anzahl_vo= rs.getInt("anzahl_vo");
       diagnosegruppe.max_behandlungen= rs.getInt("max_behandlungen");
       diagnosegruppe.gueltig_von= rs.getDate("gueltig_von");
       diagnosegruppe.gueltig_bis= rs.getDate("gueltig_bis");
       diagnosegruppe.ccid= rs.getString("ccid");
        return diagnosegruppe;
    }



}
