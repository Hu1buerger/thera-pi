package rezept;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mandant.IK;
import sql.DatenquellenFactory;

public class krankenkasseAdrDto {
    private static final Logger logger = LoggerFactory.getLogger(krankenkasseAdrDto.class);
    
    private IK ik;
    private static final String dbName = "kass_adr";
    
    public krankenkasseAdrDto(IK Ik) {
        ik = Ik;
    }
    
    public krankenkasseAdr getIKsById(int id) {
        String sql = "select IK_KASSE, IK_KOSTENT from " + dbName + " where id='" + id + "'";
        return retrieveFirst(sql);
    }
    
    private krankenkasseAdr retrieveFirst(String sql) {
        krankenkasseAdr kkAdr = null;
        try (Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();

                ResultSet rs = conn.createStatement()
                                  .executeQuery(sql)) {
            if (rs.next()) {
                logger.debug("Next Got: " + rs.toString());
                kkAdr = ofResultset(rs, rs.getMetaData());
            }
        } catch (SQLException e) {
            logger.error("Could not retrieve KrankenkasseAdr from Database", e);
        }
        return kkAdr;
    }
    
    private krankenkasseAdr ofResultset(ResultSet rs, ResultSetMetaData meta) {
        krankenkasseAdr ret = new krankenkasseAdr();

        try {
            for(int o=1;o<=meta.getColumnCount();o++) {
                String field = meta.getColumnLabel(o).toUpperCase();
                logger.debug("Checking: " + field + " in " + o);
                switch (field) {
                case "KUERZEL":
                    ret.setKuerzel(rs.getString(field));
                    break;
                case "IK_KASSE":
                    ret.setIkKasse(rs.getInt(field));
                    break;
                case "IK_KOSTENT":
                    ret.setIkKostenTraeger(rs.getInt(field));
                    break;
                default:
                    logger.error("Unhandled field in KKassenAdr found: " + meta.getColumnLabel(o) + " at pos: " + o);
                };
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error("Couldn't retrieve dataset in KrankenkasseAdr");
            logger.error("Error: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        
        return ret;
    }
    
}
