package rezept;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mandant.IK;
import sql.DatenquellenFactory;

public class RezeptFertigeDto {

    private static final Logger logger = LoggerFactory.getLogger(RezeptFertigeDto.class);
    private static final String dbName = "fertige";
    private static final String selectAllWhere = "select * from " + dbName + " where ";
    private IK ik;
    
    public RezeptFertigeDto(IK Ik) {
        ik = Ik;
    }
    
    public RezeptFertige getByRezNr(String rezNr) {
        String sql = selectAllWhere + "REZ_NR='" + rezNr + "'";
        
        return retrieveFirst(sql);
    }
    
    public void saveToDB(RezeptFertige fertiges) {
        String sql = "insert into " + dbName + " set "
                + "IKKTRAEGER='" + fertiges.getIkKTraeger() + "',"
                + "IKKASSE='" + fertiges.getIkKasse() + "',"
                + "NAME1='" + fertiges.getKassenName() + "',"
                + "REZ_NR='" + fertiges.getRezNr() + "',"
                + "PAT_INTERN='" + fertiges.getPatientIntern() + "',"
                + "REZKLASSE='" + fertiges.getRezklasse() + "',"
//                + "IDKTRAEGER='" + fertiges.getIdKTraeger() + "',"
                + "EDIFACT='" + fertiges.getEdifact() + "',"
                + "EDIOK='" + fertiges.getEdiOk() + "'";
        try {
            Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement().execute(sql);
        } catch (SQLException e) {
            logger.error("Could not save fertiges Rezept " + fertiges.toString() + " to Database", e);
        }
    }
    
    public void deleteById(int id) {
        String sql="delete from " + dbName + " where id='" + id + "'";
        try {
            Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement().execute(sql);
        } catch (SQLException e) {
            logger.error("Could not Delete fertiges Rezept ID=" + id + " from Database", e);
        }
    }
    
    public void delete( RezeptFertige fertiges) {
        String sql="delete from " + dbName + " where id='" + fertiges.getId() + "'";
        try {
            Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement().execute(sql);
        } catch (SQLException e) {
            logger.error("Could not delete Rezept " + fertiges , e);
        }
    }

    private RezeptFertige retrieveFirst(String sql) {
        RezeptFertige rezFertig = null;
        try (Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();

                ResultSet rs = conn.createStatement()
                                  .executeQuery(sql)) {
            if (rs.next()) {
                logger.debug("Next Got: " + rs.toString());
                rezFertig = ofResultset(rs);
            }
        } catch (SQLException e) {
            logger.error("Could not retrieve fertiges Rezept from Database", e);
        }
        return rezFertig;
    }
    
    private RezeptFertige ofResultset(ResultSet rs) {
        RezeptFertige ret = new RezeptFertige();
        
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
                logger.debug("Checking: " + field + " in " + o);
                switch (field) {
                case "IKKTRAEGER":
                    ret.setIkKTraeger(evaluateIK(rs.getString(field)));
                    break;
                case "IKKASSE":
                    ret.setIkKasse(evaluateIK(rs.getString(field)));
                    break;
                case "NAME1":
                    ret.setKassenName(rs.getString(field));
                    break;
                case "REZ_NR":
                    ret.setRezNr(rs.getString(field));
                    break;
                case "PAT_INTERN":
                    ret.setPatientIntern(rs.getInt(field));
                    break;
                case "REZKLASSE":
                    ret.setRezklasse(rs.getString(field));
                    break;
                case "IDKTRAEGER":
                    // This field seems to be dead meat.
                    if ( rs.getString(field) != null ) {
                        logger.error("This should have been null - seems we found some data");
                        logger.error("Field idktrager contained: \"" + rs.getString(field) + "\"");
                    };
                    break;
                case "EDIFACT":
                    ret.setEdifact(rs.getString(field));
                    break;
                case "EDIOK":
                    ret.setEdiOk(rs.getBoolean(field));
                    break;
                case "ID":
                    ret.setId(rs.getInt(field));
                    break;
                default:
                    logger.error("Unhandled field in fertige found: " + meta.getColumnLabel(o) + " at pos: " + o);
                };
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error("Couldn't retrieve dataset in fertige Rezepte");
            logger.error("Error: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        
        return ret;
    }

    /**
     * Checks if a passed-in String could be cast to an IK object, if not INVALIDIK is returned
     * 
     * @param ik as String
     * @return IK either new otherwise INVALIDIK (if param was NULL or empty)
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
