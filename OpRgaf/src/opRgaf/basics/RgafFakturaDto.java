package opRgaf.basics;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mandant.IK;
import mandant.Mandant;
import sql.DatenquellenFactory;

public class RgafFakturaDto {
    private Logger logger = LoggerFactory.getLogger(RgafFakturaDto.class);

    private static final String dbName = "RgafFaktura";
    private Mandant mandant;
    private IK ik;

    public RgafFakturaDto(Mandant mand) {
        mandant = mand;
        ik = mandant.ik();
    }

    private RgafFaktura ofResultset(ResultSet rs) {
        RgafFaktura ret = new RgafFaktura();

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
                logger.debug("Checking: " + field + " in " + o);
                switch (field) {

                case "RNR":
                    ret.setrNr(rs.getString(field));
                    break;
                case "REZNR":
                    ret.setRezNr(rs.getString(field));
                    break;
                case "PAT_INTERN":
                    ret.setPatIntern(rs.getString(field));
                    break;
                case "RGESAMT":
                    ret.setRGesamt(rs.getString(field));
                    break;
                case "ROFFEN":
                    ret.setROffen(rs.getString(field));
                    break;
                case "RGBETRAG":
                    ret.setRGBetrag(rs.getString(field));
                    break;
                case "RPBETRAG":
                    ret.setRPBetrag(rs.getString(field));
                    break;
                case "RDATUM":
                    ret.setrDatum(LocalDate.parse(rs.getString(field)));
                    break;
                case "RBEZDATUM":
                    ret.setrBezDatum(LocalDate.parse(rs.getString(field)));
                    break;
                case "RMAHNDAT1":
                    ret.setrMahndat1(LocalDate.parse(rs.getString(field)));
                    break;
                case "RMAHNDAT2":
                    ret.setrMahndat1(LocalDate.parse(rs.getString(field)));
                    break;
                case "ID":
                    ret.setId(rs.getInt(field));
                    break;
                case "IK":
                    ret.setIk(new IK(rs.getString(field)));
                    break;
                default:
                    logger.error("Unhandled field in rgaffaktura found: " + meta.getColumnLabel(o) + " at pos: " + o);
                }
                ;
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error("Couldn't retrieve dataset in rgaffaktura");
            logger.error("Error: " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        return ret;
    }

    public void saveToDB(RgafFaktura rgaff) {
        String sql = "insert into " + dbName + " set " + "RNR='" + rgaff.getrNr() + "'," + "REZNR='" + rgaff.getRezNr()
                + "'," + "PAT_INTERN='" + rgaff.getPatIntern() + "'," + "RGESAMT='" + rgaff.getrGesamt() + "',"
                + "ROFFEN='" + rgaff.getrOffen() + "'," + "RGBETRAG='" + rgaff.getrGBetrag() + "'," + "RPBETRAG='"
                + rgaff.getrPBetrag() + "'," + "RDATUM='" + rgaff.getrDatum() + "'," + "RBEZDATUM='"
                + rgaff.getrBezDatum() + "'," + "RMAHNDAT1='" + rgaff.getrMahndat1() + "'," + "RMAHNDAT2='"
                + rgaff.getrMahndat2() + "'," + "ID='" + rgaff.getId() + "'," + "IK='" + rgaff.getIk() + "'";
        try {
            Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement()
                             .execute(sql);
        } catch (SQLException e) {
            logger.error("Could not save to RgafFaktura " + rgaff.toString() + " to Database", e);
        }
    }

}
