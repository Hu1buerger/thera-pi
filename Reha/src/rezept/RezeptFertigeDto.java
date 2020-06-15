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

public class RezeptFertigeDto {

    private static final Logger logger = LoggerFactory.getLogger(RezeptFertigeDto.class);
    private static final String dbName = "fertige";
    private static final String selectAllWhere = "select * from " + dbName + " where ";
    private IK ik;
    
    public RezeptFertigeDto(IK Ik) {
        ik = Ik;
    }
    
    public Optional<RezeptFertige> getByRezNr(String rezNr) {
        String sql = selectAllWhere + "REZ_NR='" + rezNr + "'";
        
        return Optional.ofNullable(retrieveFirst(sql));
    }
    
    public RezeptFertige getByRezId(int rezId) {
        String sql = selectAllWhere + "ID='" + rezId + "'";
        
        return retrieveFirst(sql);
    }
    
    public void saveToDB(RezeptFertige fertiges) {
        // Entire Table is apparently set to "no-null" columns
        // Should be therefore prefer empty to NULL? Or let it go bust and find out where the NULL val came from?
        String sql = "insert into " + dbName + " set "
                + "IKKTRAEGER=" + (fertiges.getIkKTraeger() == null ? "NULL" : "'" + fertiges.getIkKTraeger().digitString() + "'" ) + ","
                + "IKKASSE=" + (fertiges.getIkKasse() == null ? "NULL" : "'" + fertiges.getIkKasse().digitString() + "'" ) + ","
                + "NAME1=" + quoteNonNull(fertiges.getKassenName()) + ","
                + "REZ_NR=" + quoteNonNull(fertiges.getRezNr()) + ","
                + "PAT_INTERN=" + fertiges.getPatientIntern() + ","
                + "REZKLASSE=" + quoteNonNull(fertiges.getRezklasse()) + ","
                + "IDKTRAEGER='',"           // Apparently this is dead meat - but has no default val in DB, so we need
                                             // to put something in there - non-null column
                                             // TODO: Apparently the current AlleTabellen.sql lacks an update that sets this
                                             //  field to allow NULL & defaults to NULL - once AlleTabellen.sql has been
                                             //  updated, we can safely revert to setting it to NULL
                + "EDIFACT=" + quoteNonNull(fertiges.getEdifact()) + ","
                + "EDIOK='" + fertiges.getEdiOk() + "'";
        try {
            Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement().execute(sql);
        } catch (SQLException e) {
            logger.error("Could not save fertiges Rezept " + fertiges.toString() + " to Database", e);
        }
    }
    
    public boolean deleteById(int id) {
        if ( id == 0) {
            logger.error("Need proper data to operate on");
            return false;
        }
        String sql="delete from " + dbName + " where id='" + id + "'";
        try {
            Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement().execute(sql);
        } catch (SQLException e) {
            logger.error("Could not Delete fertiges Rezept ID=" + id + " from Database", e);
            return false;
        }
        return true;
    }

    public boolean deleteByRezNr(String rezNr) {
        if ( rezNr.isEmpty()) {
            logger.error("Need proper data to operate on");
            return false;
        }
        String sql="delete from " + dbName + " where REZ_NR='" + rezNr + "'";
        try {
            Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement().execute(sql);
        } catch (SQLException e) {
            logger.error("Could not Delete fertiges Rezept ID=" + rezNr + " from Database", e);
            return false;
        }
        return true;
    }
    
    public boolean delete( RezeptFertige fertiges) {
        int rfId = fertiges.getId();
        if ( rfId == 0) {
            logger.error("Need proper data to operate on");
            return false;
        }
        String sql="delete from " + dbName + " where id='" + rfId + "'";
        try {
            Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement().execute(sql);
            // TODO: check if we actually did delete 1 element from DB
            // if (!rs) { logger.debug("Updatecount= " + gimme };
        } catch (SQLException e) {
            logger.error("Could not delete Rezept " + fertiges , e);
            return false;
        }
        return true;
    }

    private RezeptFertige retrieveFirst(String sql) {
        RezeptFertige rezFertig = null;
        try (Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();

                ResultSet rs = conn.createStatement()
                                  .executeQuery(sql)) {
            if (rs.next()) {
                // logger.debug("Next Got: " + rs.toString());
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
                // logger.debug("Checking: " + field + " in " + o);
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
                    if ( rs.getString(field) != null && !rs.getString(field).isEmpty()) {
                        logger.error("We thought this field was not in use - seems we found some data");
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

    /**
     * Little helper, since for the DB val='null' != val=NULL.
     * Should val==null be true, we return a string containing simply NULL,
     * otherwise we put val in single-quotes to tell DB it can treat val as string.
     * 
     * @param val
     * 
     * @return
     */
    private String quoteNonNull(Object val) {
        return (val == null ? "NULL" : "'" + val + "'");
    }

    
    //@Visible for Testing
    int countAlleEintraege() {
        String sql="select count(id) from " + dbName;
        int anzahl = 0;
        try (Connection conn = new DatenquellenFactory(ik.digitString())
                .createConnection();

            ResultSet rs = conn.createStatement()
                                                .executeQuery(sql)) {
                if (rs.next()) {
                    anzahl = rs.getInt(1);
                }
        } catch (SQLException e) {
            logger.error("could not count fertige Rezepte in " + dbName, e);
        }
        
        return anzahl;
    }
    
}
