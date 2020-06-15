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
 * Helper class to do DB related tasks on the table "kass_Adr" (KrankenkasseAdr?)
 * 
 * Originally designed to do "RezeptAbschluss" which needed some of these fields copied into a different table.
 * It will need
 * - a new home
 * - new methods 
 *      - retrieve full dataset         DONE
 *      - save current object to DB     DONE
 *      - some other stuff I currently can't think of
 *          - save as update or insert-into DONE
 *      
 */
public class KrankenkasseAdrDto {
    private static final Logger logger = LoggerFactory.getLogger(KrankenkasseAdrDto.class);
    
    private IK ik;
    private static final String dbName = "kass_adr";
    // FIXME: set the following value to something like "REZ_NR" or whatever makes the most sense...
    private static final String mainIdentifier="IK_KASSE";  // In this case, maybe ID would actually work as well...
    private static final String selectAllWhere = "select * from " + dbName + " where ";
    
    public KrankenkasseAdrDto(IK Ik) {
        ik = Ik;
    }
    
    /**
     * Originally created for RezeptAbschiessen, this method only get 2 fields from the DB:
     * IK_Kasse and IK_KostenT
     * TODO: needs renaming, since there are more than 2 IKs stored in a dataset
     * TODO: Maybe sort RezeptAbschliessen to handle an entire set of data
     * 
     * @param id INT id of Krankenkasse
     * @return Optional a KkAdr that only contains the two aforementioned IKs
     */
    public Optional<KrankenkasseAdr> getIKsById(int id) {
        String sql = "select IK_KASSE, IK_KOSTENT from " + dbName + " where id='" + id + "'";
        return retrieveFirst(sql);
    }
    
    public Optional<KrankenkasseAdr> getById(int id) {
        String sql = selectAllWhere + "id='" + id + "'";
        return retrieveFirst(sql);
    }
    
    /**
     * This will return a KrankenkasseAdr which only has the Preisgruppen fields set
     * It takes a boolean to indicate whether the Reha-PGs should be included
     * 
     * @param id    Int - the ID of the KK
     * @param mitRs Bool - do you also want the Reha-PGs?
     * 
     * @return      Optional - if result present will be a KrankenkasseAdr with PGs set
     */
    public Optional<KrankenkasseAdr> getAllePreisgruppenFelderById(int id, boolean mitRs) {
        String sql = "select preisgruppe,pgkg,pgma,pger,pglo,pgrh,pgpo";
        if (mitRs) {
            sql = sql.concat(",pgrs,pgft");
        }
        sql = sql.concat(" from " + dbName + " where id='" + id + "'");
 
        return retrieveFirst(sql);
    }
    
    /**
     * Takes an SQL-Statement as String and executes it<BR/> (<B>this is NOT! a query!</B>).<BR/>
     * SQL statements can be e.g. <BR/>"update verordn set termine='' where rez_nr='ER1'"<BR/>
     * In theory even <BR/>"alter table ..."<BR/> should be possible...<BR/>
     * @param sql - the SQL statement as String
     */
    private void updateDataset(String sql) {
        Connection conn;
        try {
            conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement().execute(sql);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error("In updateDataset:");
            logger.error(e.getLocalizedMessage());
            logger.error("SQL-Statement was: '" + sql + "'");
        }
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
            logger.error("SQL-Statement was: \"" + sql + "\"");
        }
        return Optional.ofNullable(kkAdr);
    }
    
    private KrankenkasseAdr ofResultset(ResultSet rs, ResultSetMetaData meta) {
        KrankenkasseAdr ret = new KrankenkasseAdr();

        try {
            for(int o=1;o<=meta.getColumnCount();o++) {
                String field = meta.getColumnLabel(o).toUpperCase();
                // logger.debug("Checking: " + field + " in " + o);
                switch (field) {
                case "KUERZEL":
                    ret.setKuerzel(rs.getString(field));
                    break;
                case "PREISGRUPPE":
                    ret.setPreisgruppe(rs.getInt(field));
                    break;
                case "KASSEN_NAM1":
                    ret.setKassenNam1(rs.getString(field));
                    break;
                case "KASSEN_NAM2":
                    ret.setKassenNam2(rs.getString(field));
                    break;
                case "STRASSE":
                    ret.setStrasse(rs.getString(field));
                    break;
                case "PLZ":
                    ret.setPlz(rs.getString(field));
                    break;
                case "ORT":
                    ret.setOrt(rs.getString(field));
                    break;
                case "POSTFACH":
                    ret.setPostfach(rs.getString(field));
                    break;
                case "FAX":
                    ret.setFax(rs.getString(field));
                    break;
                case "TELEFON":
                    ret.setTelefon(rs.getString(field));
                    break;
                case "IK_NUM":
                    ret.setIkNum(rs.getString(field));
                    break;
                case "KV_NUMMER":
                    ret.setKvNummer(rs.getString(field));
                    break;
                case "MATCHCODE":
                    ret.setMatchcode(rs.getString(field));
                    break;
                case "KMEMO":
                    ret.setKMemo(rs.getString(field));
                    break;
                case "RECHNUNG":
                    ret.setRechnung(rs.getString(field));
                    break;
                case "IK_KASSE":
                    ret.setIkKasse(evaluateIK(rs.getString(field)));
                    break;
                case "IK_PHYSIKA":
                    ret.setIkPhysika(evaluateIK(rs.getString(field)));
                    break;
                case "IK_NUTZER":
                    ret.setIkNutzer(evaluateIK(rs.getString(field)));
                    break;
                case "IK_KOSTENT":
                    ret.setIkKostenTraeger(evaluateIK(rs.getString(field)));
                    break;
                case "IK_KVKARTE":
                    ret.setIkKvKarte(evaluateIK(rs.getString(field)));
                    break;
                case "IK_PAPIER":
                    ret.setIkPapier(evaluateIK(rs.getString(field)));
                    break;
                case "EMAIL1":
                    ret.setEmail1(rs.getString(field));
                    break;
                case "EMAIL2":
                    ret.setEmail2(rs.getString(field));
                    break;
                case "EMAIL3":
                    ret.setEmail3(rs.getString(field));
                    break;
                case "ID":
                    ret.setId(rs.getInt(field));
                    break;
                case "HMRABRECHNUNG":
                    ret.setHmrAbrechnung(rs.getString(field).trim().equals("T") ? true : false);
                    break;
                case "PGKG":
                    ret.setPgKg(rs.getInt(field));
                    break;
                case "PGMA":
                    ret.setPgMa(rs.getInt(field));
                    break;
                case "PGER":
                    ret.setPgEr(rs.getInt(field));
                    break;
                case "PGLO":
                    ret.setPgLo(rs.getInt(field));
                    break;
                case "PGRH":
                    ret.setPgRh(rs.getInt(field));
                    break;
                case "PGPO":
                    ret.setPgPo(rs.getInt(field));
                    break;
                case "PGRS":
                    ret.setPgRs(rs.getInt(field));
                    break;
                case "PGFT":
                    ret.setPgFt(rs.getInt(field));
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

    public boolean saveToDB(KrankenkasseAdr dataset) {
        logger.debug("Got this DS in saveToDB: " + dataset.toString());
        // FIXME: set appropriate getter to match mainIdentifier
        String sql="select id from " + dbName + " where " + mainIdentifier + "='" + dataset.getIkKasse().digitString() + "'";
        boolean isNew = false;
        
        try (Connection conn = new DatenquellenFactory(ik.digitString())
                .createConnection()) {
            // FIXME: set appropriate getter to match mainIdentifier
            if ( dataset.getIkKasse() != null && dataset.getIkKasse().isValid()) {
            
                ResultSet rs = conn.createStatement()
                        .executeQuery(sql);
                if (rs.next()) {
                    isNew = false;
                    // FIXME: set appropriate getter to match mainIdentifier
                    logger.debug("Kass_adr will " + dataset.getIkKasse() + " be updated");
                } else {
                    isNew = true;
                    // FIXME: set appropriate getter to match mainIdentifier
                    logger.debug("Kass_adr will " + dataset.getIkKasse() + " be added.");
                }
            } else {
                logger.error("Given " + mainIdentifier + " was empty or Null - this shouldn't happen - get " + mainIdentifier + " before saving it");
                return false;
            }
            if (isNew) {
                sql="insert into " + dbName + " ";
            } else {
                sql="update " + dbName + " ";
            }
            sql = sql.concat(createFullDataset(dataset));
            if (!isNew)
                // FIXME: set appropriate getter to match mainIdentifier
                sql = sql.concat(" WHERE " + mainIdentifier + "='" + dataset.getIkKasse().digitString() + "' LIMIT 1");
            logger.debug("Will run this sql-command: \"" + sql + "\"");
            updateDataset(sql);
        } catch (SQLException e) {
            // FIXME: set appropriate getter to match mainIdentifier
            logger.error("Could not save Kass_adr " + dataset.getIkKasse() + " to Database", e);
            logger.error("SQL-Statement: " + sql);
            return false;
        }
        return true;

    }
        
    private String createFullDataset(KrankenkasseAdr dataset) {
        logger.debug("Got this DS: " + dataset.toString());
        String sql = "set "
                    + "KUERZEL=" + quoteNonNull(dataset.getKuerzel()) + ","
                    + "PREISGRUPPE=" + dataset.getPreisgruppe() + ","
                    + "KASSEN_NAM1=" + quoteNonNull(dataset.getKassenNam1()) + ","
                    + "KASSEN_NAM2=" + quoteNonNull(dataset.getKassenNam2()) + ","
                    + "STRASSE=" + quoteNonNull(dataset.getStrasse()) + ","
                    + "PLZ=" + quoteNonNull(dataset.getPlz()) + ","
                    + "ORT=" + quoteNonNull(dataset.getOrt()) + ","
                    + "POSTFACH=" + quoteNonNull(dataset.getPostfach()) + ","
                    + "FAX=" + quoteNonNull(dataset.getFax()) + ","
                    + "TELEFON=" + quoteNonNull(dataset.getTelefon()) + ","
                    + "IK_NUM=" + quoteNonNull(dataset.getIkNum()) + ","
                    + "KV_NUMMER=" + quoteNonNull(dataset.getKvNummer()) + ","
                    + "MATCHCODE=" + quoteNonNull(dataset.getMatchcode()) + ","
                    + "KMEMO=" + quoteNonNull(dataset.getKMemo()) + ","
                    + "RECHNUNG=" + quoteNonNull(dataset.getRechnung()) + ","
                    + "IK_KASSE=" + ( dataset.getIkKasse() == null ? "NULL" : "'" + dataset.getIkKasse().digitString() + "'" ) + ","
                    + "IK_PHYSIKA=" + ( dataset.getIkPhysika() == null ? "NULL" : "'" + dataset.getIkPhysika().digitString() + "'" ) + ","
                    + "IK_NUTZER=" +  ( dataset.getIkNutzer() == null ? "NULL" : "'" + dataset.getIkNutzer().digitString() + "'" ) + ","
                    + "IK_KOSTENT=" + ( dataset.getIkKostenTraeger() == null ? "NULL" : "'" + dataset.getIkKostenTraeger().digitString() + "'" ) + ","
                    + "IK_KVKARTE=" + ( dataset.getIkKvKarte() == null ? "NULL" : "'" + dataset.getIkKvKarte().digitString() + "'" ) + ","
                    + "IK_PAPIER=" +  ( dataset.getIkPapier() == null ? "NULL" : "'" + dataset.getIkPapier().digitString() + "'" ) + ","
                    + "EMAIL1=" + quoteNonNull(dataset.getEmail1()) + ","
                    + "EMAIL2=" + quoteNonNull(dataset.getEmail2()) + ","
                    + "EMAIL3=" + quoteNonNull(dataset.getEmail3()) + ","
                    + "HMRABRECHNUNG=" + quoteNonNull(dataset.getHmrAbrechnung()) + ","   // DB wants this field non-null
                                                                                          //  so, we shouldn't need to put
                                                                                          // an unquoted null - but if we
                                                                                          // do ntl, we'll have a fail-fast
                    + "PGKG=" + quoteNonNull(dataset.getPgKg()) + ","     // These are all int in class, but String in
                    + "PGMA=" + quoteNonNull(dataset.getPgMa()) + ","     // DB - we'll quote them to store them as
                    + "PGER=" + quoteNonNull(dataset.getPgEr()) + ","     // strings....
                    + "PGLO=" + quoteNonNull(dataset.getPgLo()) + ","
                    + "PGRH=" + quoteNonNull(dataset.getPgRh()) + ","
                    + "PGPO=" + quoteNonNull(dataset.getPgPo()) + ","
                    + "PGRS=" + quoteNonNull(dataset.getPgRs()) + ","
                    + "PGFT=" + quoteNonNull(dataset.getPgFt()) ;
        return sql;
    }
    
    /**
     * Checks if a passed-in String could be cast to an IK object, if not INVALIDIK is returned
     * 
     * @param ik as String
     * @return IK either new or INVALIDIK (if param was NULL or empty)
     */
    private IK evaluateIK(String ik) {
        IK temp;
        
        if (ik == null) {
            temp= null;
        } else if (ik.isEmpty()) {
            temp= IK.INVALIDIK;         // This is bad when trying to save a DS via ik.digits()...
        } else {
            temp= new IK(ik);
        }
        return temp;
    }
    
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
            logger.error("could not count entries in Database " + dbName, e);
        }
        
        return anzahl;
    }
    
}
