package rezept;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mandant.IK;
import sql.DatenquellenFactory;

public class RezeptDto {
    private static final Logger logger = LoggerFactory.getLogger(RezeptDto.class);
    
    private static final String mainIdentifier="REZ_NR";
    private static final String aktRezDB = "verordn";
    private static final String rezDBLZA = "lza";
    private static final String SelectAllSql = "select * from " + aktRezDB
                                                + " union select * from " + rezDBLZA + " order by " + mainIdentifier + ";";
    private static final String selectAllFromRezDBWhere = "SELECT * from " + aktRezDB + " WHERE ";
    private static final String selectAllFromLzaDBWhere = "SELECT * from " + rezDBLZA + " WHERE ";

    private IK ik;

    public RezeptDto(IK Ik) {
        ik = Ik;
    }

    /**
     * Will return all rezepte as List from both aktuelle and LZA, ordered by RezNr
     * @return
     */
    List<Rezept> all() {
        String sql = SelectAllSql;
        return retrieveList(sql);
    }

    /**
     * Will return all rezepte from akuelle, ordered by RezNr as List
     * 
     * @return
     */
    List<Rezept> allfromVerordn(){
        final String sql = "select * from " + aktRezDB + " order by rez_nr";
        return retrieveList(sql);
    }

    /**
     * Will return a Rezept given the RezNr by checking both verordn and lza
     * @param rezeptNummer
     * @return
     */
    public Optional<Rezept> byRezeptNr(String rezeptNummer) {
        String sql = selectAllFromRezDBWhere + "REZ_NR='" + rezeptNummer + "'"
                + "UNION " + selectAllFromLzaDBWhere + "REZ_NR LIKE '" + rezeptNummer + "';";
        Rezept rezept = retrieveFirst(sql);

        return Optional.ofNullable(rezept);
    }

    /**
     * Will return a Rezept matching a given Rezept-Id by checking both verordn and lza
     * @param rezeptId
     * @return
     */
    public Optional<Rezept> byRezeptId(int rezeptId) {
        String sql = selectAllFromRezDBWhere + "ID = '" + rezeptId + "'"
                + "UNION " + selectAllFromLzaDBWhere + "ID = '" + rezeptId + "';";
        Rezept rezept = retrieveFirst(sql);

        return Optional.ofNullable(rezept);
    }

    /**
     * Will return a List of Rezepte from aktuelle Rezepte matching a given PatId (patIntern)
     * @param patientID
     * @return
     */
    public List<Rezept> getAktuelleRezepteByPatNr(int patientID) {
        String sql = selectAllFromRezDBWhere + "PAT_INTERN = '" + patientID + "'";

        return retrieveList(sql);
    }
    
    /**
     * Search LZA for Rezepte by PatIntern
     * @param patientID
     * @return
     */
    public List<Rezept> getHistorischeRezepteByPatNr(int patientID) {
        String sql = selectAllFromLzaDBWhere + "PAT_INTERN = '" + patientID + "'";

        return retrieveList(sql);
    }

    /**
     * Search LZA for a Rezept by RezNr
     * 
     * @param rezNr
     * @return
     */
    public Optional<Rezept> getHistorischesRezeptByRezNr(String rezNr) {
        String sql = selectAllFromLzaDBWhere + "REZ_NR = '" + rezNr + "'";
        Rezept rezept = retrieveFirst(sql);

        return Optional.ofNullable(rezept);
    }

    /**
     * Update the Termine-String of a Rezept identified by its RezId
     * @param Id
     * @param TerminListe
     */
    public void updateRezeptTermine(int Id, String TerminListe) {
        String sql = "UPDATE " + aktRezDB + " SET termine=" + quoteNonNull(TerminListe)
                    + " WHERE id ='" + Id + "' LIMIT 1";
        updateDataset(sql);
    }

    /**
     * Change the abschluss bool of a Rezept identified by RezId.
     * @param Id
     * @param status
     */
    void rezeptAbschluss(int Id, boolean status) {
        String sql = "UPDATE " + aktRezDB + " SET abschluss='" + ( status ? "T" : "F")
                    + "' WHERE id='" + Id + "' LIMIT 1";
        updateDataset(sql);
    }

    /**
     * Update RezGebuehr related fields in DB:
     * <BR/> - ZZStatus
     * <BR/> - rezGeb
     * <BR/> - rezBez
     * <BR/> - befr
     */
    public void updateRezeptGebuehrenParameter(int ZZStatus, Money rezGeb, boolean rezBez, boolean befreit, String rezNr) {
        String sql = "UPDATE " + aktRezDB + " SET "
                                                + "ZZSTATUS=" + ZZStatus + ", "
                                                + "REZ_GEB=" + rezGeb + ", "
                                                + "REZ_BEZ='" + (rezBez ? "T" : "F") + "', "
                                                + "BEFR='" + (befreit ? "T" : "F") + "' "
                                                + "WHERE REZ_NR='" + rezNr + "' LIMIT 1";

        // sql = String.format(sql, ZZStatus, rezGeb, (rezBez ? "T" : "F"), rezNr);
        
        updateDataset(sql);
    }
    
    /**
     * Search both aktuel & lza for Rezepte by Patient & Diszi, order rez_datum descending and return 1st
     * @param patIntern
     * @param diszi
     * @return
     */
    public Rezept juengstesRezeptVonPatientInDiszi (int patIntern, String diszi) {
        // Suche neuestes Rezept inkl. der Disziplin

        String sql = "SELECT * FROM `lza` WHERE `PAT_INTERN` = " + patIntern + " AND rez_nr like '" + diszi
                + "%'" + " union " + "SELECT * FROM `verordn` WHERE `PAT_INTERN` = " + patIntern
                + " AND rez_nr like '" + diszi + "%'" + " ORDER BY rez_datum desc LIMIT 1";
        return  retrieveFirst(sql);

    }

    /**
     * Originally for doubletten-Test <BR/> Will search for Rezepte in akt. Rezepte (bool aktuelle == true)  or in 
     * lza (aktuelle == false) by patIntern excluding a specific Rezept
     * <BR/>currently returns a list of Rezpete that only have the fields
     *  rez_datum,rez_nr and termine filled - need to mull over whether we want
     *  <BR/>- fully populated Rezepte(-List)
     *  <BR/>- A dedicated ArrayList [rezNr, rezDatum, Termine] or whatnot (key/val pairs may be awkward)
     *  <BR/>- Could drop the 'exclude this rezNr' and sort that at the callers end (and thereby possibly use standard
     *  'getByPatId()' altogether, obsoleting this method...
     */
    // TODO: change RezNr to proper type
    public List<Rezept> holeDatumUndTermineNachPatientExclRezNr(int patID, String rezNr, boolean aktuelle, LocalDate lastRezDate) {
        String sql= "SELECT REZ_DATUM, REZ_NR, TERMINE from " 
                        + ( aktuelle ? aktRezDB : aktRezDB )
                        + " WHERE PAT_INTERN=" + patID + " and rez_nr !='" + rezNr + "'"
                        + ( aktuelle ? ";" : " and REZ_DATUM >= " + lastRezDate + ";");
        
        return retrieveList(sql);
    }
    
    /**
     * Delete a Rezept from aktuelle by RezNr
     * @param rezNr
     */
    public void deleteByRezNr(String rezNr) {
        if (rezNr == null || rezNr.isEmpty()) {
            // TODO: throw something at caller...
            logger.error("RezNr " + rezNr + " on delete was not usable...");
        }
        String sql = "delete from " + aktRezDB + " where REZ_NR='" + rezNr + "' limit 1";
        updateDataset(sql);
    }
    
    /**
     * Delete a Rezept from Historie by RezNr
     * @param rezNr
     */
    public void deleteHistorieByRezNr(String rezNr) {
        if (rezNr == null || rezNr.isEmpty()) {
            // TODO: throw something at caller...
            logger.error("RezNr " + rezNr + " on delete from lza was not usable...");
        }
        String sql = "delete from " + rezDBLZA + " where REZ_NR='" + rezNr + "' limit 1";
        updateDataset(sql);
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

    private Rezept retrieveFirst(String sql) {
        Rezept rezept = null;
        try (Connection conn = new DatenquellenFactory(ik.digitString())
                                                       .createConnection();

                ResultSet rs = conn.createStatement()
                                  .executeQuery(sql)) {
            if (rs.next()) {
                rezept = ofResultset(rs);
            }
        } catch (SQLException e) {
            logger.error("could not retrieve Rezept from Database", e);
        }
        return rezept;
    }

    private List<Rezept> retrieveList(String sql) {
        List<Rezept> rezeptLste = new LinkedList<>();
        try (Connection conn = new DatenquellenFactory(ik.digitString())
                                                       .createConnection()) {
            ResultSet rs = conn.createStatement()
                              .executeQuery(sql);
            while (rs.next()) {
                rezeptLste.add(ofResultset(rs));
            }

            return rezeptLste;
        } catch (SQLException e) {
            logger.error("could not retrieve Rezepte from Database", e);
            logger.error("Statement: \"" + sql + "\"");
            return Collections.emptyList();
        }
    }

    /**
     * Transfers a resultset into a Rezept-Object & returns it
     * 
     * @param rs - the ResultSet from a db-query containing 1 Rezept
     * @return a Rezept-Object populated with data from the Resultset
     * @throws SQLException
     */
    private Rezept ofResultset(ResultSet rs) throws SQLException {
        Rezept rez = new Rezept();
        
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
                        rez.setPatIntern(rs.getInt(field));
                        break;
                    case "REZ_NR":
                        rez.setRezNr(rs.getString(field));
                        break;
                    case "REZ_DATUM":
                        rez.setRezDatum((rs.getDate(field)== null ? null : rs.getDate(field).toLocalDate()));
                        break;
                    case "ANZAHL1":
                        rez.setBehAnzahl1(rs.getInt(field));
                        break;
                    case "ANZAHL2":
                        rez.setBehAnzahl2(rs.getInt(field));
                        break;
                    case "ANZAHL3":
                        rez.setBehAnzahl3(rs.getInt(field));
                        break;
                    case "ANZAHL4":
                        rez.setBehAnzahl4(rs.getInt(field));
                        break;
                    case "ANZAHLKM":
                        // TODO: do we need to set "0.00" on empty/null/0?
                        rez.setAnzahlKM(rs.getBigDecimal(field));
                        break; 
                    case "ART_DBEH1":
                        rez.setArtDerBeh1(rs.getInt(field));
                        break;
                    case "ART_DBEH2":
                        rez.setArtDerBeh2(rs.getInt(field));
                        break;
                    case "ART_DBEH3":
                        rez.setArtDerBeh3(rs.getInt(field));
                        break;
                    case "ART_DBEH4":
                        rez.setArtDerBeh4(rs.getInt(field));
                        break;
                    case "BEFR":        // Feld (stand 2020.06) darf nicht null sein
                        rez.setBefr("T".equals(rs.getString(field)));
                        break;
                    case "REZ_GEB":
                        rez.setRezGeb(new Money(rs.getString(field)));
                        break;
                    case "REZ_BEZ":        // Feld (stand 2020.06) darf nicht null sein
                        rez.setRezBez("T".equals(rs.getString(field)));
                        break;
                    case "ARZT":
                        rez.setArzt(rs.getString(field));
                        break;
                    case "ARZTID":
                        rez.setArztId(rs.getInt(field));
                        break;
                    case "AERZTE":
                        rez.setAerzte(rs.getString(field));
                        break;
                    case "PREISE1":
                        rez.setPreise1(new Money(rs.getString(field)));
                        break;
                    case "PREISE2":
                        rez.setPreise2(new Money(rs.getString(field)));
                        break;
                    case "PREISE3":
                        rez.setPreise3(new Money(rs.getString(field)));
                        break;
                    case "PREISE4":
                        rez.setPreise4(new Money(rs.getString(field)));
                        break;
                    case "DATUM":
                        rez.setErfassungsDatum((rs.getDate(field)== null ? null : rs.getDate(field).toLocalDate()));
                        break;
                    case "DIAGNOSE":
                        rez.setDiagnose(rs.getString(field));
                        break;
                    case "HEIMBEWOHN":        // Feld (stand 2020.06) darf nicht null sein
                        rez.setRezBez("T".equals(rs.getString(field)));
                        break;
                    case "VERAENDERD":
                        rez.setVeraenderD((rs.getDate(field)== null ? null : rs.getDate(field).toLocalDate()));
                        break;
                    case "VERAENDERA":
                        rez.setVeraenderA(rs.getInt(field));
                        break;
                    case "REZEPTART":
                        rez.setRezeptArt(rs.getInt(field));
                        break;
                    case "LOGFREI1":        // Feld (stand 2020.06) darf nicht null sein
                        rez.setLogfrei1("T".equals(rs.getString(field)));
                        break;
                    case "LOGFREI2":        // Feld (stand 2020.06) darf nicht null sein
                        rez.setLogfrei2("T".equals(rs.getString(field)));
                        break;
                    case "NUMFREI1":
                        rez.setNumfrei1(rs.getInt(field));
                        break;
                    case "NUMFREI2":
                        rez.setNumfrei2(rs.getInt(field));
                        break;
                    case "CHARFREI1":
                        rez.setCharfrei1(rs.getString(field));
                        break;
                    case "CHARFREI2":
                        rez.setCharfrei2(rs.getString(field));
                        break;
                    //TODO List<Termin>
                    case "TERMINE":
                        rez.setTermine(rs.getString(field));
                        break;
                    case "ID":
                        rez.setId(rs.getInt(field));
                        break;
                    case "KTRAEGER":
                        rez.setKTraegerName(rs.getString(field));
                        break;
                    case "KID":
                        rez.setkId(rs.getInt(field));
                        break;
                    case "PATID":
                        rez.setPatId(rs.getInt(field));
                        break;
                    case "ZZSTATUS":
                        rez.setZZStatus(rs.getInt(field));
                        break;
                    case "LASTDATE":
                        rez.setLastDate((rs.getDate(field)== null ? null : rs.getDate(field).toLocalDate()));
                        break;
                    case "PREISGRUPPE":
                        rez.setPreisGruppe(rs.getInt(field));
                        break;
                    case "BEGRUENDADR":        // Feld (stand 2020.06) darf nicht null sein
                        rez.setBegruendADR("T".equals(rs.getString(field)));
                        break;
                    case "HAUSBES":        // Feld (stand 2020.06) darf nicht null sein
                        rez.setHausBesuch("T".equals(rs.getString(field)));
                        break;
                    case "INDIKATSCHL":
                        rez.setIndikatSchl(rs.getString(field));
                        break;
                    case "ANGELEGTVON":
                        rez.setAngelegtVon(rs.getString(field));
                        break;
                    case "BARCODEFORM":
                        rez.setBarcodeform(rs.getInt(field));
                        break;
                    case "DAUER":
                        rez.setDauer(rs.getString(field));
                        break;
                    case "POS1":
                        rez.setHMPos1(Optional.ofNullable( rs.getString(field)).orElse(""));
                        break;
                    case "POS2":
                        rez.setHMPos2(Optional.ofNullable( rs.getString(field)).orElse(""));
                        break;
                    case "POS3":
                        rez.setHMPos3(Optional.ofNullable( rs.getString(field)).orElse(""));
                        break;
                    case "POS4":
                        rez.setHMPos4(Optional.ofNullable( rs.getString(field)).orElse(""));
                        break;
                    case "FREQUENZ":
                        rez.setFrequenz(rs.getString(field));
                        break;
                    case "LASTEDIT":
                        rez.setLastEditor(rs.getString(field));
                        break;
                    case "BERID":
                        rez.setBerId(rs.getInt(field));
                        break;
                    case "ARZTBERICHT":        // Feld (stand 2020.06) darf nicht null sein
                        rez.setArztBericht("T".equals(rs.getString(field)));
                        break;
                    case "LASTEDDATE":
                        rez.setLastEdDate((rs.getDate(field)== null ? null : rs.getDate(field).toLocalDate()));
                        break;
                    case "RSPLIT":
                        rez.setRSplit(rs.getString(field));
                        break;
                    case "JAHRFREI":
                        rez.setJahrfrei(rs.getString(field));
                        break;
                    case "UNTER18":        // Feld (stand 2020.06) darf nicht null sein
                        rez.setUnter18("T".equals(rs.getString(field)));
                        break;
                    case "HBVOLL":        // Feld (stand 2020.06) darf nicht null sein
                        rez.setHbVoll("T".equals(rs.getString(field)));
                        break;
                    case "ABSCHLUSS":        // Feld (stand 2020.06) darf nicht null sein
                        rez.setAbschluss("T".equals(rs.getString(field)));
                        break;
                    case "ZZREGEL":
                        rez.setZZRegel(rs.getInt(field));
                        break;
                    case "ANZAHLHB":
                        rez.setAnzahlHb(rs.getInt(field));
                        break;
                    case "KUERZEL1":
                        rez.setHMKuerzel1(rs.getString(field));
                        break;
                    case "KUERZEL2":
                        rez.setHMKuerzel2(rs.getString(field));
                        break;
                    case "KUERZEL3":
                        rez.setHMKuerzel3(rs.getString(field));
                        break;
                    case "KUERZEL4":
                        rez.setHMKuerzel4(rs.getString(field));
                        break;
                    case "KUERZEL5":
                        rez.setHMKuerzel5(rs.getString(field));
                        break;
                    case "KUERZEL6":
                        rez.setHMKuerzel6(rs.getString(field));
                        break;
                    case "ICD10":
                        rez.setIcd10(rs.getString(field));
                        break;
                    case "ICD10_2":
                        rez.setIcd10_2(rs.getString(field));
                        break;
                    case "PAUSCHALE":        // Feld (stand 2020.06) darf nicht null sein
                        rez.setPauschale("T".equals(rs.getString(field)));
                        break;
                    case "FARBCODE":
                        // Why?
                        rez.setFarbcode(parseFarbcodeFromDBToint(rs));
                        break;
                    default:
                        logger.error("Unhandled field in Rezepte-DB found: " + meta.getColumnLabel(o) + " at pos: " + o);
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error("Couldn't retrieve dataset in RezepteDto");
            logger.error("Error: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        
        return rez;
    }

    private int parseFarbcodeFromDBToint(ResultSet rs) throws SQLException {
        try {
            return Integer.parseInt(rs.getString("FARBCODE"));
        }catch (Exception e){
            return -1;
        }
    }
    
    /**
     * Will (try) to save a Rezept to the "historische rezepte" table. Will check whether Rezept(-Nr) already
     *  exists and if so, update otherwise insert.
     * <BR/> Contains a Q&D to fix no-null on lastdate in lza - this should either be removed (if alter lza table)
     *  or inserted on caller side, with proper "werktage/kalendartage" switch and Fristen taken from diszi...
     * 
     * @param rez - the Rezept to save
     * 
     * @return true if no error detected otherwise false
     */
    public boolean rezeptInHistSpeichern(Rezept rez) {
        // TODO: create either copy ("insert into lza select...") or create own insert, since we should never have to update...
        if (rez.getLastDate() == null) {
            rez.setLastDate(rez.getRezDatum().plusDays(14)); // Q&D - either lza needs to handle this or
                                                             // caller should have fixed it properly before calling us
        }
        return rezeptInDBSpeichern(rez, false);
    };

    /**
     * Will (try) to save a Rezept to the "aktuelle rezepte" table. Will check whether Rezept(-Nr) already exists and if so, update
     *   otherwise insert.
     *   
     * @param rez - the Rezept to save
     * 
     * @return true if no error detected otherwise false
     */
    public boolean rezeptInDBSpeichern(Rezept rez) {
        return rezeptInDBSpeichern(rez, true);
    }
    
    private boolean rezeptInDBSpeichern(Rezept rez, boolean aktuelle) {
        String dbName = (aktuelle ? aktRezDB : rezDBLZA);
        String sql="select id from " + dbName + " where " + mainIdentifier + "='" + rez.getRezNr() + "'";
        boolean isNew = false;
        
        try (Connection conn = new DatenquellenFactory(ik.digitString())
                .createConnection()) {
            if ( rez.getRezNr() != null && !rez.getRezNr().isEmpty()) {
            
                ResultSet rs = conn.createStatement()
                        .executeQuery(sql);
                if (rs.next()) {
                    isNew = false;
                    logger.debug("Rezept " + rez.getRezNr() + " will be updated");
                } else {
                    isNew = true;
                    logger.debug("Rezept " + rez.getRezNr() + " will be added.");
                }
            } else {
                logger.error("Given RezNr was empty or Null - this shouldn't happen - get RezNr before saving it");
                return false;
            }
            if (isNew) {
                sql="insert into " + dbName + " ";
            } else {
                sql="update " + dbName + " ";
            }
            sql = sql.concat(createFullDataset(rez));
            if (!isNew)
                sql = sql.concat(" WHERE REZ_NR='" + rez.getRezNr() + "' LIMIT 1");
            updateDataset(sql);
        } catch (SQLException e) {
            logger.error("Could not save Rezept " + rez.getRezNr() + " to Database", e);
            return false;
        }
        return true;
        
    }
    
    /**
     * This will simply return a string setting all fields for an entry in verordn.
     * The data to be set is taken from a Rezept-Object.
     * 
     * @param rez - the Rezept who's values shall be used to create the set statement
     * 
     * @return a String containing all fields with the values from the Rezept
     */
    private String createFullDataset(Rezept rez) {
        String sql = "set "
                + "REZ_NR='" + rez.getRezNr() + "', "
 //               + "ID='" + rez.getId() + "', "                  // Do we really want this? on insert it should create one,
                                                                  //   on update it shouldn't change...
                + "REZEPTART=" + rez.getRezeptArt() + ", "
                + "REZ_DATUM='" + rez.getRezDatum() + "', "
                + "PAT_INTERN=" + rez.getPatIntern() + ", "
                + "PATID=" + rez.getPatId() + ", "
                + "ANZAHL1=" + rez.getBehAnzahl1() + ", "
                + "ANZAHL2=" + rez.getBehAnzahl2() + ", "
                + "ANZAHL3=" + rez.getBehAnzahl3() + ", "
                + "ANZAHL4=" + rez.getBehAnzahl4() + ", "
                + "ANZAHLKM=" + rez.getAnzahlKM() + ", "
                + "ART_DBEH1=" + rez.getArtDerBeh1() + ", "
                + "ART_DBEH2=" + rez.getArtDerBeh2() + ", "
                + "ART_DBEH3=" + rez.getArtDerBeh3() + ", "
                + "ART_DBEH4=" + rez.getArtDerBeh4() + ", "
                + "BEFR='" + rez.getBefr() + "', "
                + "REZ_GEB=" + rez.getRezGeb() + ", "
                + "REZ_BEZ='" + rez.getRezBez() + "', "
                + "ARZT='" + rez.getArzt() + "', "
                + "ARZTID=" + rez.getArztId() + ", "
                + "AERZTE=" + quoteNonNull(rez.getAerzte()) + ", "
                + "PREISE1=" + rez.getPreise1() + ", "
                + "PREISE2=" + rez.getPreise2() + ", "
                + "PREISE3=" + rez.getPreise3() + ", "
                + "PREISE4=" + rez.getPreise4() + ", "
                + "DATUM='" + rez.getErfassungsDatum() + "', "
                + "DIAGNOSE='" + rez.getDiagnose() + "', "
                + "HEIMBEWOHN='" + rez.getHeimbewohn() + "', "
                + "VERAENDERD=" + quoteNonNull(rez.getVeraenderD()) + ", "
                + "VERAENDERA='" + rez.getVeraenderA() + "', "
                + "LOGFREI1='" + rez.getLogfrei1() + "', "
                + "LOGFREI2='" + rez.getLogfrei2() + "', "
                + "NUMFREI1=" + rez.getNumfrei1() + ", "
                + "NUMFREI2=" + rez.getNumfrei2() + ", "
                + "CHARFREI1=" + quoteNonNull(rez.getCharfrei1()) + ", "
                + "CHARFREI2=" + quoteNonNull(rez.getCharfrei2()) + ", "
                + "TERMINE=" + quoteNonNull(rez.getTermine()) + ", "
                + "KTRAEGER='" + rez.getKTraegerName() + "', "
                + "KID=" + rez.getkId() + ", "
                + "ZZSTATUS=" + rez.getZZStatus() + ", "
                + "LASTDATE=" + quoteNonNull(rez.getLastDate()) + ", "
                + "PREISGRUPPE=" + rez.getPreisGruppe() + ", "
                + "BEGRUENDADR='" + rez.getBegruendADR() + "', "
                + "HAUSBES='" + rez.getHausBesuch() + "', "
                + "INDIKATSCHL='" + rez.getIndikatSchl() + "', "
                + "ANGELEGTVON='" + rez.getAngelegtVon() + "', "
                + "LASTEDDATE=" + quoteNonNull(rez.getLastEdDate()) + ", "
                + "BARCODEFORM=" + rez.getBarcodeform() + ", "
                + "DAUER='" + rez.getDauer() + "', "
                + "POS1='" + rez.getHMPos1() + "', "
                + "POS2='" + rez.getHMPos2() + "', "
                + "POS3='" + rez.getHMPos3() + "', "
                + "POS4='" + rez.getHMPos4() + "', "
                + "FREQUENZ='" + rez.getFrequenz() + "', "
                + "LASTEDIT='" + rez.getLastEditor() + "', "
                + "BERID=" + rez.getBerId() + ", "
                + "ARZTBERICHT='" + rez.getArztBericht() + "', "
                + "FARBCODE='" + rez.getFarbcode() + "', "
                + "RSPLIT=" + quoteNonNull(rez.getRSplit()) + ", "
                + "JAHRFREI='" + rez.getJahrfrei() + "', "
                + "UNTER18='" + rez.getUnter18() + "', "
                + "HBVOLL='" + rez.getHbVoll() + "', "
                + "ABSCHLUSS='" + rez.getAbschluss() + "', "
                + "ZZREGEL=" + rez.getZZRegel() + ", "
                + "ANZAHLHB='" + rez.getAnzahlHb() + "', "
                + "KUERZEL1='" + rez.getHMKuerzel1() + "', "
                + "KUERZEL2='" + rez.getHMKuerzel2() + "', "
                + "KUERZEL3='" + rez.getHMKuerzel3() + "', "
                + "KUERZEL4='" + rez.getHMKuerzel4() + "', "
                + "KUERZEL5=" + quoteNonNull(rez.getHMKuerzel5()) + ", "
                + "KUERZEL6=" + quoteNonNull(rez.getHMKuerzel6()) + ", "
                + "ICD10='" + rez.getIcd10() + "', "
                + "ICD10_2=" + quoteNonNull(rez.getIcd10_2()) + ", "
                + "PAUSCHALE='" + rez.getPauschale() + "' ";
        return sql;
        // logger.debug("Ran SQL command: ");
        // logger.debug(sql);
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
        String sql="select count(id) from " + aktRezDB;
        int anzahl = 0;
        try (Connection conn = new DatenquellenFactory(ik.digitString())
                .createConnection();

            ResultSet rs = conn.createStatement()
                                                .executeQuery(sql)) {
                if (rs.next()) {
                    anzahl = rs.getInt(1);
                }
        } catch (SQLException e) {
            logger.error("could not count Rezepte in Database", e);
        }
        
        return anzahl;
    }
}
