package rezept;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mandant.IK;
import sql.DatenquellenFactory;

public class RezeptDto {
    private IK ik;
    private static final Logger logger = LoggerFactory.getLogger(RezeptDto.class);
    private static final String aktRezDB = "verordn";
    private static final String rezDBLZA = "lza";
    private static final String SelectAllSql = "select * from " + aktRezDB 
                                                + " union select * from " + rezDBLZA + " order by rez_nr;";
    private static final String selectAllFromRezDBWhere = "SELECT * from " + aktRezDB + " WHERE ";
    private static final String selectAllFromLzaDBWhere = "SELECT * from " + rezDBLZA + " WHERE ";
    

    public RezeptDto(IK Ik) {
        ik = Ik;
    }
    
    List<Rezept> all() {
        String sql = SelectAllSql;
        return retrieveList(sql);
    }

    List<Rezept> allfromVerordn(){
        final String sql = "select * from " + aktRezDB + " order by rez_nr";
        return retrieveList(sql);
    }

    public Optional<Rezept> byRezeptNr(String rezeptNummer) {
        String sql = selectAllFromRezDBWhere + "REZ_NR LIKE '" + rezeptNummer + "'"
                + "UNION " + selectAllFromLzaDBWhere + "REZ_NR LIKE '" + rezeptNummer + "';";
        Rezept rezept = retrieveFirst(sql);

        return Optional.ofNullable(rezept);
    }
    
    public Optional<Rezept> byRezeptId(String rezeptId) {
        String sql = selectAllFromRezDBWhere + "ID = '" + rezeptId + "'"
                + "UNION " + selectAllFromLzaDBWhere + "ID = '" + rezeptId + "';";
        Rezept rezept = retrieveFirst(sql);

        return Optional.ofNullable(rezept);
    }

    public List<Rezept> getRezepteByPatNr(String patientID) {
        String sql = selectAllFromRezDBWhere + "PAT_INTERN = '" + patientID + "'"
                + "UNION " + selectAllFromLzaDBWhere + "PAT_INTERN = '" + patientID + "';";

        return retrieveList(sql);
    }

    public void updateRezeptTermine(String Id, String TerminListe) {
        String sql = "UPDATE " + aktRezDB + "SET termine='" + TerminListe
                    + "' WHERE id ='" + Id + "' LIMIT 1";
        updateDataset(sql);
    }
    
    public void rezeptAbschluss(String Id, boolean status) {
        String sql = "UPDATE " + aktRezDB + "SET abschluss='" + ( status ? "T" : "F")
                    + "' WHERE id='" + Id + "' LIMIT 1";
        updateDataset(sql);
    }
    
    public Rezept juengstesRezeptVonPatientInDiszi (String patIntern, String diszi) {
        // Suche neuestes Rezept inkl. der Disziplin
        
        String sql = "SELECT * FROM `lza` WHERE `PAT_INTERN` = " + patIntern + " AND rez_nr like '" + diszi
                + "%'" + " union " + "SELECT * FROM `verordn` WHERE `PAT_INTERN` = " + patIntern
                + " AND rez_nr like '" + diszi + "%'" + " ORDER BY rez_datum desc LIMIT 1";
        return  retrieveFirst(sql);

    }
    
    private void updateDataset(String sql) {
        Connection conn;
        try {
            conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement().execute(sql);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error("In updateDataset:");
            logger.error(e.getLocalizedMessage());
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
            return Collections.emptyList();
        }
    }

    private Rezept ofResultset(ResultSet rs) throws SQLException {
        Rezept rez = new Rezept();
        rez.patIntern = rs.getInt("PAT_INTERN");
        rez.rezNr = new Rezeptnummer(rs.getString("REZ_NR"));
        rez.rezDatum =

                rs.getDate("REZ_DATUM") == null ? null
                        : rs.getDate("REZ_DATUM")
                            .toLocalDate();
        rez.anzahl1 = rs.getInt("ANZAHL1");
        rez.anzahl2 = rs.getInt("ANZAHL2");
        rez.anzahl3 = rs.getInt("ANZAHL3");
        rez.anzahl4 = rs.getInt("ANZAHL4");
        rez.anzahlKM = new BigDecimal(rs.getString("ANZAHLKM"));
        rez.artDerBeh1 = rs.getInt("ART_DBEH1");
        rez.artDerBeh2 = rs.getInt("ART_DBEH2");
        rez.artDerBeh3 = rs.getInt("ART_DBEH3");
        rez.artDerBeh4 = rs.getInt("ART_DBEH4");
        rez.befr = "T".equals(Optional.ofNullable(rs.getString("BEFR"))
                           .orElse(""));
        rez.rezGeb = new Money(rs.getString("REZ_GEB"));
        rez.rezBez = "T".equals(Optional.ofNullable(rs.getString("REZ_BEZ"))
                              .orElse(""));
        rez.arzt = rs.getString("ARZT");
        rez.arztId = rs.getInt("ARZTID");
        rez.aerzte = rs.getString("AERZTE");
        rez.preise1 = new Money(rs.getString("PREISE1"));
        rez.preise2 = new Money(rs.getString("PREISE2"));
        rez.preise3 = new Money(rs.getString("PREISE3"));
        rez.preise4 = new Money(rs.getString("PREISE4"));
        rez.erfassungsDatum = rs.getDate("DATUM") == null ? null
                : rs.getDate("DATUM")
                    .toLocalDate();
        rez.diagnose = rs.getString("DIAGNOSE");
        rez.heimbewohn = "T".equals(Optional.ofNullable(rs.getString("HEIMBEWOHN"))
                                 .orElse(""));
        rez.veraenderd = rs.getDate("VERAENDERD") == null ? null
                : rs.getDate("VERAENDERD")
                    .toLocalDate();
        rez.veraendera = rs.getInt("VERAENDERA");
        rez.rezeptArt = rs.getInt("REZEPTART");
        rez.logfrei1 = "T".equals(Optional.ofNullable(rs.getString("LOGFREI1"))
                               .orElse(""));
        rez.logfrei2 = "T".equals(Optional.ofNullable(rs.getString("LOGFREI2"))
                               .orElse(""));
        rez.numfrei1 = rs.getInt("NUMFREI1");
        rez.numfrei2 = rs.getInt("NUMFREI2");
        rez.charfrei1 = rs.getString("CHARFREI1");
        rez.charfrei2 = rs.getString("CHARFREI2");
        rez.termine = rs.getString("TERMINE");
        rez.id = rs.getInt("ID");
        rez.ktraeger = rs.getString("KTRAEGER");
        rez.kId = rs.getInt("KID");
        rez.patId = rs.getInt("PATID");
        rez.zzStatus = rs.getInt("ZZSTATUS");
        rez.lastDate = rs.getDate("LASTDATE") == null ? null
                : rs.getDate("LASTDATE")
                    .toLocalDate();
        rez.preisgruppe = rs.getInt("PREISGRUPPE");
        rez.begruendADR = "T".equals(Optional.ofNullable(rs.getString("BEGRUENDADR"))
                                  .orElse(""));
        rez.hausbes = "T".equals(Optional.ofNullable(rs.getString("HAUSBES"))
                              .orElse(""));
        rez.indikatSchl = rs.getString("INDIKATSCHL");
        rez.angelegtVon = rs.getString("ANGELEGTVON");
        rez.barcodeform = rs.getInt("BARCODEFORM");
        rez.dauer = rs.getString("DAUER");
        rez.pos1 = rs.getString("POS1");
        rez.pos2 = rs.getString("POS2");
        rez.pos3 = rs.getString("POS3");
        rez.pos4 = rs.getString("POS4");
        rez.frequenz = rs.getString("FREQUENZ");
        rez.lastEditor = rs.getString("LASTEDIT");
        rez.berId = rs.getInt("BERID");
        rez.arztBericht = "T".equals(Optional.ofNullable(rs.getString("ARZTBERICHT"))
                                  .orElse(""));
        rez.lastEdDate = rs.getDate("LASTEDDATE") == null ? null
                : rs.getDate("LASTEDDATE")
                    .toLocalDate();
        rez.farbcode = Integer.parseInt(rs.getString("FARBCODE"));
        rez.rsplit = rs.getString("RSPLIT");
        rez.jahrfrei = rs.getString("JAHRFREI");
        rez.unter18 = "T".equals(Optional.ofNullable(rs.getString("UNTER18"))
                              .orElse(""));
        rez.hbVoll = "T".equals(Optional.ofNullable(rs.getString("HBVOLL"))
                             .orElse(""));
        rez.abschluss = "T".equals(Optional.ofNullable(rs.getString("ABSCHLUSS"))
                                .orElse(""));
        rez.zzRegel = rs.getInt("ZZREGEL");
        rez.anzahlHb = rs.getInt("ANZAHLHB");
        rez.kuerzel1 = rs.getString("KUERZEL1");
        rez.kuerzel2 = rs.getString("KUERZEL2");
        rez.kuerzel3 = rs.getString("KUERZEL3");
        rez.kuerzel4 = rs.getString("KUERZEL4");
        rez.kuerzel5 = rs.getString("KUERZEL5");
        rez.kuerzel6 = rs.getString("KUERZEL6");
        rez.icd10 = rs.getString("ICD10");
        rez.icd10_2 = rs.getString("ICD10_2");
        rez.pauschale = "T".equals(Optional.ofNullable(rs.getString("PAUSCHALE"))
                                .orElse(""));

        return rez;
    }
    
    public void rezeptInDBSpeichern(Rezept rez) {
        String sql = "update " + rezDBLZA + " set "
                + "REZ_NR='" + rez.getRezNr() + "', "
                + "ID='" + rez.getId() + "', "
                + "REZEPTART='" + rez.getRezeptArt() + "', "
                + "REZ_DATUM='" + rez.getRezDatum() + "', "
                + "PAT_INTERN='" + rez.getPatIntern() + "', "
                + "PATID='" + rez.getPatId() + "', "
                + "ANZAHL1='" + rez.getBehAnzahl1() + "', "
                + "ANZAHL2='" + rez.getBehAnzahl2() + "', "
                + "ANZAHL3='" + rez.getBehAnzahl3() + "', "
                + "ANZAHL4='" + rez.getBehAnzahl4() + "', "
                + "ANZAHLKM='" + rez.getAnzahlKM() + "', "
                + "ART_DBEH1='" + rez.getArtDerBeh1() + "', "
                + "ART_DBEH2='" + rez.getArtDerBeh2() + "', "
                + "ART_DBEH3='" + rez.getArtDerBeh3() + "', "
                + "ART_DBEH4='" + rez.getArtDerBeh4() + "', "
                + "BEFR='" + rez.getBefr() + "', "
                + "REZ_GEB='" + rez.getRezGeb() + "', "
                + "REZ_BEZ='" + rez.getRezBez() + "', "
                + "ARZT='" + rez.getArzt() + "', "
                + "ARZTID='" + rez.getArztId() + "', "
                + "AERZTE='" + rez.getAerzte() + "', "
                + "PREISE1='" + rez.getPreise1() + "', "
                + "PREISE2='" + rez.getPreise2() + "', "
                + "PREISE3='" + rez.getPreise3() + "', "
                + "PREISE4='" + rez.getPreise4() + "', "
                + "DATUM='" + rez.getErfassungsDatum() + "', "
                + "DIAGNOSE='" + rez.getDiagnose() + "', "
                + "HEIMBEWOHN='" + rez.getHeimbewohn() + "', "
                + "VERAENDERD='" + rez.getVeraenderd() + "', "
                + "VERAENDERA='" + rez.getVeraendera() + "', "
                + "LOGFREI1='" + rez.getLogfrei1() + "', "
                + "LOGFREI2='" + rez.getLogfrei2() + "', "
                + "NUMFREI1='" + rez.getNumfrei1() + "', "
                + "NUMFREI2='" + rez.getNumfrei2() + "', "
                + "CHARFREI1='" + rez.getCharfrei1() + "', "
                + "CHARFREI2='" + rez.getCharfrei2() + "', "
                + "TERMINE='" + rez.getTermine() + "', "
                + "KTRAEGER='" + rez.getKTraegerName() + "', "
                + "KID='" + rez.getkId() + "', "
                + "ZZSTATUS='" + rez.getZZStatus() + "', "
                + "LASTDATE='" + rez.getLastDate() + "', "
                + "PREISGRUPPE='" + rez.getPreisGruppe() + "', "
                + "BEGRUENDADR='" + rez.getBegruendADR() + "', "
                + "HAUSBES='" + rez.getHausBesuch() + "', "
                + "INDIKATSCHL='" + rez.getIndikatSchl() + "', "
                + "ANGELEGTVON='" + rez.getAngelegtVon() + "', "
                + "LASTEDDATE='" + rez.getLastEdDate() + "', "
                + "BARCODEFORM='" + rez.getBarcodeform() + "', "
                + "DAUER='" + rez.getDauer() + "', "
                + "POS1='" + rez.getPos1() + "', "
                + "POS2='" + rez.getPos2() + "', "
                + "POS3='" + rez.getPos3() + "', "
                + "POS4='" + rez.getPos4() + "', "
                + "FREQUENZ='" + rez.getFrequenz() + "', "
                + "LASTEDIT='" + rez.getLastEditor() + "', "
                + "BERID='" + rez.getBerId() + "', "
                + "ARZTBERICHT='" + rez.getArztBericht() + "', "
                + "FARBCODE='" + rez.getFarbcode() + "', "
                + "RSPLIT='" + rez.getRSplit() + "', "
                + "JAHRFREI='" + rez.getJahrfrei() + "', "
                + "UNTER18='" + rez.getUnter18() + "', "
                + "HBVOLL='" + rez.getHbVoll() + "', "
                + "ABSCHLUSS='" + rez.getAbschluss() + "', "
                + "ZZREGEL='" + rez.getZZRegel() + "', "
                + "ANZAHLHB='" + rez.getAnzahlHb() + "', "
                + "KUERZEL1='" + rez.getKuerzel1() + "', "
                + "KUERZEL2='" + rez.getKuerzel2() + "', "
                + "KUERZEL3='" + rez.getKuerzel3() + "', "
                + "KUERZEL4='" + rez.getKuerzel4() + "', "
                + "KUERZEL5='" + rez.getKuerzel5() + "', "
                + "KUERZEL6='" + rez.getKuerzel6() + "', "
                + "ICD10='" + rez.getIcd10() + "', "
                + "ICD10_2='" + rez.getIcd10_2() + "', "
                + "PAUSCHALE='" + rez.getPauschale() + "', "
         + "WHERE id='" + rez.getId() + "' LIMIT 1";
        updateDataset(sql);
    }
}
