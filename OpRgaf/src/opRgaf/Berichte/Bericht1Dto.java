package opRgaf.Berichte;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mandant.IK;
import sql.DatenquellenFactory;

public class Bericht1Dto {

    private Logger logger = LoggerFactory.getLogger(Bericht1Dto.class);

    private static final String dbName = "bericht1";

    private IK ik;

    private Bericht1 ofResultset(ResultSet rs) {
        Bericht1 ret = new Bericht1();

        ResultSetMetaData meta;
        try {
            meta = rs.getMetaData();
        } catch (SQLException e) {
            logger.error("Could not retrieve metaData", e);
            return null;
        }
        try {
            for (int o = 1; o <= meta.getColumnCount(); o++) {
                String field = meta.getColumnLabel(o)
                                   .toUpperCase();
                // logger.debug("Checking: " + field + " in " + o);
                switch (field) {

                case "PAT_INTERN":
                    ret.setPatIntern(rs.getString(field));
                    break;
                case "BERICHTID":
                    ret.setBerichtId(rs.getInt(field));
                    break;
                case "ARZT_NUM":
                    ret.setArztNum(rs.getString(field));
                    break;
                case "ERSTELLDAT":
                    ret.setErstellDat(rs.getDate(field) == null ? null : rs.getDate(field).toLocalDate());
                    break;
                case "VERSANDART":
                    ret.setVersandArt(rs.getString(field));
                    break;
                case "VERSANDDAT":
                    ret.setVersandDat(rs.getDate(field) == null ? null : rs.getDate(field).toLocalDate());
                    break;
                case "BERTYP":
                    ret.setBerTyp(rs.getString(field));
                    break;
                case "BERSTAND":
                    ret.setBerStand(rs.getString(field));
                    break;
                case "BERBESO":
                    ret.setBerBeso(rs.getString(field));
                    break;
                case "BERPROG":
                    ret.setBerProg(rs.getString(field));
                    break;
                case "BERVORS":
                    ret.setBerVors(rs.getString(field));
                    break;
                case "DIAGNOSE":
                    ret.setDiagnose(rs.getString(field));
                    break;
                case "KRBILD":
                    ret.setKrBild(rs.getString(field));
                    break;
                case "VERFASSER":
                    ret.setVerfasser(rs.getString(field));
                    break;
                case "REZ_DATUM":
                    ret.setRezDatum(rs.getDate(field) == null ? null : rs.getDate(field).toLocalDate());
                    break;
                default:
                    logger.error("Unhandled field in bericht1 found: " + meta.getColumnLabel(o) + " at pos: " + o);
                }
                ;
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error("Couldn't retrieve dataset in Bericht1");
            logger.error("Error: " + e.getLocalizedMessage());
        }

        return ret;
    }

    public void saveToDB(Bericht1 dataset) {
        String sql = "insert into " + dbName + " set " + "PAT_INTERN='" + dataset.getPatIntern() + "'," + "BERICHTID='"
                + dataset.getBerichtId() + "'," + "ARZT_NUM='" + dataset.getArztNum() + "'," + "ERSTELLDAT='"
                + dataset.getErstellDat() + "'," + "VERSANDART='" + dataset.getVersandArt() + "'," + "VERSANDDAT='"
                + dataset.getVersandDat() + "'," + "BERTYP='" + dataset.getBerTyp() + "'," + "BERSTAND='"
                + dataset.getBerStand() + "'," + "BERBESO='" + dataset.getBerBeso() + "'," + "BERPROG='"
                + dataset.getBerProg() + "'," + "BERVORS='" + dataset.getBerVors() + "'," + "DIAGNOSE='"
                + dataset.getDiagnose() + "'," + "KRBILD='" + dataset.getKrBild() + "'," + "VERFASSER='"
                + dataset.getVerfasser() + "'," + "REZ_DATUM='" + dataset.getRezDatum() + "'";
        try {
            Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement()
                             .execute(sql);
        } catch (SQLException e) {
            logger.error("Could not save dataset " + dataset.toString() + " to Database, table bericht1", e);
        }
    }

}
