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
                // logger.debug("Checking: " + field + " in " + o);
                switch (field) {
                case "KUERZEL":
                    ret.setKuerzel(rs.getString(field));
                    break;
                case "PREISGRUPPE":
                    ret.setPreisgruppe(rs.getString(field));
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
                    ret.setPgKg(rs.getString(field));
                    break;
                case "PGMA":
                    ret.setPgMa(rs.getString(field));
                    break;
                case "PGER":
                    ret.setPgEr(rs.getString(field));
                    break;
                case "PGLO":
                    ret.setPgLo(rs.getString(field));
                    break;
                case "PGRH":
                    ret.setPgRh(rs.getString(field));
                    break;
                case "PGPO":
                    ret.setPgPo(rs.getString(field));
                    break;
                case "PGRS":
                    ret.setPgRs(rs.getString(field));
                    break;
                case "PGFT":
                    ret.setPgFt(rs.getString(field));
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

    public void saveToDB(KrankenkasseAdr kka) {
        String sql = "insert into " + dbName + " set "
            + "KUERZEL='" + kka.getKuerzel() + "',"
            + "PREISGRUPPE='" + kka.getPreisgruppe() + "',"
            + "KASSEN_NAM1='" + kka.getKassenNam1() + "',"
            + "KASSEN_NAM2='" + kka.getKassenNam2() + "',"
            + "STRASSE='" + kka.getStrasse() + "',"
            + "PLZ='" + kka.getPlz() + "',"
            + "ORT='" + kka.getOrt() + "',"
            + "POSTFACH='" + kka.getPostfach() + "',"
            + "FAX='" + kka.getFax() + "',"
            + "TELEFON='" + kka.getTelefon() + "',"
            + "IK_NUM='" + kka.getIkNum() + "',"
            + "KV_NUMMER='" + kka.getKvNummer() + "',"
            + "MATCHCODE='" + kka.getMatchcode() + "',"
            + "KMEMO='" + kka.getKMemo() + "',"
            + "RECHNUNG='" + kka.getRechnung() + "',"
            + "IK_KASSE='" + kka.getIkKasse() + "',"
            + "IK_PHYSIKA='" + kka.getIkPhysika() + "',"
            + "IK_NUTZER='" + kka.getIkNutzer() + "',"
            + "IK_KOSTENT='" + kka.getIkKostenTraeger() + "',"
            + "IK_KVKARTE='" + kka.getIkKvKarte() + "',"
            + "IK_PAPIER='" + kka.getIkPapier() + "',"
            + "EMAIL1='" + kka.getEmail1() + "',"
            + "EMAIL2='" + kka.getEmail2() + "',"
            + "EMAIL3='" + kka.getEmail3() + "',"
            + "ID='" + kka.getId() + "',"
            + "HMRABRECHNUNG='" + kka.getHmrAbrechnung() + "',"
            + "PGKG='" + kka.getPgKg() + "',"
            + "PGMA='" + kka.getPgMa() + "',"
            + "PGER='" + kka.getPgEr() + "',"
            + "PGLO='" + kka.getPgLo() + "',"
            + "PGRH='" + kka.getPgRh() + "',"
            + "PGPO='" + kka.getPgPo() + "',"
            + "PGRS='" + kka.getPgRs() + "',"
            + "PGFT='" + kka.getPgFt() + "'";
        try {
            Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement().execute(sql);
        } catch (SQLException e) {
            logger.error("Could not save Krankenkasse Adresse " + kka.toString() + " to Database", e);
        }
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
