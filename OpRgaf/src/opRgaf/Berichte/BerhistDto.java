package opRgaf.Berichte;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mandant.IK;
import sql.DatenquellenFactory;
    
public class BerhistDto {
    private static final Logger logger = LoggerFactory.getLogger(BerhistDto.class);
    
    private static final String dbName="berhist";
    private IK ik;
    
    public BerhistDto(IK Ik) {
        ik = Ik;
    }
    
    private Berhist ofResultset(ResultSet rs) {
        Berhist ret = new Berhist();
        
        ResultSetMetaData meta;
        try {
            meta = rs.getMetaData();
        } catch (SQLException e) {
            logger.error("Could not retrieve metaData", e);
            return null;
        }
        try {
            for(int o=1;o<=meta.getColumnCount();o++) {
                String field = meta.getColumnLabel(o).toUpperCase();
                // logger.debug("Checking: " + field + " in " + o);
                switch (field) {

                    case "PAT_INTERN":
                        ret.setPatIntern(rs.getString(field));
                        break;
                    case "BERICHTID":
                        ret.setBerichtId(rs.getInt(field));
                        break;
                    case "BERICHTTYP":
                        ret.setBerichtTyp(rs.getString(field));
                        break;
                    case "VERFASSER":
                        ret.setVerfasser(rs.getString(field));
                        break;
                    case "EMPFAENGER":
                        ret.setEmpfaenger(rs.getString(field));
                        break;
                    case "BERTITEL":
                        ret.setBerTitel(rs.getString(field));
                        break;
                    case "ERSTELLDAT":
                        ret.setErstellDat(rs.getDate(field) == null ? null : rs.getDate(field).toLocalDate());
                        break;
                    case "EDITDAT":
                        ret.setEditDat(rs.getDate(field) == null ? null : rs.getDate(field).toLocalDate());
                        break;
                    case "VERSANDDAT":
                        ret.setVersandDat(rs.getDate(field) == null ? null : rs.getDate(field).toLocalDate());
                        break;
                    case "DATEINAME":
                        ret.setDateiname(rs.getString(field));
                        break;
                    case "EMPFID":
                        ret.setEmpfId(rs.getInt(field));
                        break;
                    case "ID":
                        ret.setId(rs.getInt(field));
                        break;
                    default:
                        logger.error("Unhandled field in " + dbName + " found: " + meta.getColumnLabel(o) + " at pos: " + o);
                };
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error("Couldn't retrieve dataset in " + BerhistDto.class.getName());
            logger.error("Error: " + e.getLocalizedMessage());
        }
        
        return ret;
    }
    
    public void saveToDB(Berhist dataset) {
        String sql = "insert into " + dbName + " set "
                    + "PAT_INTERN=" + quoteNonNull(dataset.getPatIntern()) + ","
                    + "BERICHTID=" + dataset.getBerichtId() + ","
                    + "BERICHTTYP=" + quoteNonNull(dataset.getBerichtTyp()) + ","
                    + "VERFASSER=" + quoteNonNull(dataset.getVerfasser()) + ","
                    + "EMPFAENGER=" + quoteNonNull(dataset.getEmpfaenger()) + ","
                    + "BERTITEL=" + quoteNonNull(dataset.getBerTitel()) + ","
                    + "ERSTELLDAT=" + quoteNonNull(dataset.getErstellDat()) + ","
                    + "EDITDAT=" + quoteNonNull(dataset.getEditDat()) + ","
                    + "VERSANDDAT=" + quoteNonNull(dataset.getVersandDat()) + ","
                    + "DATEINAME=" + quoteNonNull(dataset.getDateiname()) + ","
                    + "EMPFID=" + dataset.getEmpfId();
        try {
            Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement().execute(sql);
        } catch (SQLException e) {
            logger.error("Could not save dataset " + dataset.toString( ) + " to Database, table " + dbName + ".", e);
        }
    }
    
    private String quoteNonNull(Object val) {
        return (val == null ? "NULL" : "'" + val + "'");
    }

}
