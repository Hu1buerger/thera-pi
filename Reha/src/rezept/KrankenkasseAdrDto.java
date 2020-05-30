package rezept;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mandant.IK;
import sql.DatenquellenFactory;

/**
 * Helper class to do DB related tasks on the table "krankenkasseAdr"
 * 
 * Originally designed to do "RezeptAbschluss" which needed some of these fields copied into a different table.
 * It will need
 * - a new home
 * - new methods 
 *      - retrieve full dataset
 *      - save current okbject to DB
 *      - some other stuff I currently can't think of
 *      
 */
public class KrankenkasseAdrDto {
    private static final Logger logger = LoggerFactory.getLogger(KrankenkasseAdrDto.class);
    
    private IK ik;
    private static final String dbName = "kass_adr";
    
    public KrankenkasseAdrDto(IK Ik) {
        ik = Ik;
    }
    
    public Optional<KrankenkasseAdr> getIKsById(int id) {
        String sql = "select IK_KASSE, IK_KOSTENT from " + dbName + " where id='" + id + "'";
        return retrieveFirst(sql);
    }
    
    private Optional<KrankenkasseAdr> retrieveFirst(String sql) {
        KrankenkasseAdr kkAdr = null;
        try (Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();

                ResultSet rs = conn.createStatement()
                                  .executeQuery(sql)) {
            if (rs.next()) {
                kkAdr = ofResultset(rs, rs.getMetaData());
            }
        } catch (SQLException e) {
            logger.error("Could not retrieve KrankenkasseAdr from Database", e);
        }
        return Optional.ofNullable(kkAdr);
    }
    
    private KrankenkasseAdr ofResultset(ResultSet rs, ResultSetMetaData meta) {
        KrankenkasseAdr ret = new KrankenkasseAdr();

        try {
            for(int o=1;o<=meta.getColumnCount();o++) {
                String field = meta.getColumnLabel(o).toUpperCase();
                logger.debug("Checking: " + field + " in " + o);
                switch (field) {
                case "KUERZEL":
                    ret.setKuerzel(rs.getString(field));
                    break;
                case "IK_KASSE":
                    ret.setIkKasse(evaluateIK(rs.getString(field)));
                    break;
                case "IK_KOSTENT":
                    ret.setIkKostenTraeger(evaluateIK(rs.getString(field)));
                    break;
                default:
                    logger.error("Unhandled field in KKassenAdr found: " + meta.getColumnLabel(o) + " at pos: " + o);
                };
            }
        } catch (SQLException e) {
            logger.error("Couldn't retrieve dataset in KrankenkasseAdr", e);
        }
        
        return ret;
    }

    /**
     * Checks if a passed-in String could be cast to an IK object, if not INVALIDIK is returned
     * 
     * @param ik as String
     * @return IK either new or INVALIDIK (if param was NULL or empty)
     */
    private IK evaluateIK(String ik) {
        IK temp;
        
        if (ik == null || ik.isEmpty()) {
             temp= IK.INVALIDIK;
            
        } else {
            temp= new IK(ik);
        }
        return temp;
    }
    
}
