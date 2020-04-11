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
    private static final String SelectAllSql = "select * from verordn union select * from lza order by rez_nr;";
    public RezeptDto(IK ik) {
        this.ik = ik;
    }

    List<Rezept> all() {
        String sql = SelectAllSql;
        return retrieveList(sql);
    }

    private List<Rezept> retrieveList(String sql) {
        List<Rezept> rezeptLste = new LinkedList<>();
        try (Connection con = new DatenquellenFactory(ik.digitString())
                                                       .createConnection()) {
            ResultSet rs = con.createStatement()
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

    public Optional<Rezept> byRezeptNr(String rezeptNummer) {
        String sql = "SELECT * FROM verordn WHERE REZ_NR LIKE '" + rezeptNummer + "'"
                + "UNION SELECT * FROM lza WHERE REZ_NR LIKE '" + rezeptNummer + "';";
        Rezept rezept = retrieveFirst(sql);

        return Optional.ofNullable(rezept);
    }

    private Rezept retrieveFirst(String sql) {
        Rezept rezept = null;
        try (Connection con = new DatenquellenFactory(ik.digitString())
                                                       .createConnection();

                ResultSet rs = con.createStatement()
                                  .executeQuery(sql)) {
            if (rs.next()) {
                rezept = ofResultset(rs);
            }
        } catch (SQLException e) {
            logger.error("could not retrieve Rezept from Database", e);
        }
        return rezept;
    }

    private Rezept ofResultset(ResultSet rs) throws SQLException {
        Rezept rez = new Rezept();
        rez.PAT_INTERN = rs.getInt("PAT_INTERN");
        rez.REZ_NR = rs.getString("REZ_NR");
        rez.REZ_DATUM =

                rs.getDate("REZ_DATUM") == null ? null
                        : rs.getDate("REZ_DATUM")
                            .toLocalDate();
        rez.ANZAHL1 = rs.getInt("ANZAHL1");
        rez.ANZAHL2 = rs.getInt("ANZAHL2");
        rez.ANZAHL3 = rs.getInt("ANZAHL3");
        rez.ANZAHL4 = rs.getInt("ANZAHL4");
        rez.ANZAHLKM = new BigDecimal(rs.getString("ANZAHLKM"));
        rez.ART_DBEH1 = rs.getInt("ART_DBEH1");
        rez.ART_DBEH2 = rs.getInt("ART_DBEH2");
        rez.ART_DBEH3 = rs.getInt("ART_DBEH3");
        rez.ART_DBEH4 = rs.getInt("ART_DBEH4");
        rez.BEFR = "T".equals(Optional.ofNullable(rs.getString("BEFR"))
                           .orElse(""));
        rez.REZ_GEB = new Money(rs.getString("REZ_GEB"));
        rez.REZ_BEZ = "T".equals(Optional.ofNullable(rs.getString("REZ_BEZ"))
                              .orElse(""));
        rez.ARZT = rs.getString("ARZT");
        rez.ARZTID = rs.getInt("ARZTID");
        rez.AERZTE = rs.getString("AERZTE");
        rez.PREISE1 = new Money(rs.getString("PREISE1"));
        rez.PREISE2 = new Money(rs.getString("PREISE2"));
        rez.PREISE3 = new Money(rs.getString("PREISE3"));
        rez.PREISE4 = new Money(rs.getString("PREISE4"));
        rez.DATUM = rs.getDate("DATUM") == null ? null
                : rs.getDate("DATUM")
                    .toLocalDate();
        rez.DIAGNOSE = rs.getString("DIAGNOSE");
        rez.HEIMBEWOHN = "T".equals(Optional.ofNullable(rs.getString("HEIMBEWOHN"))
                                 .orElse(""));
        rez.VERAENDERD = rs.getDate("VERAENDERD") == null ? null
                : rs.getDate("VERAENDERD")
                    .toLocalDate();
        rez.VERAENDERA = rs.getInt("VERAENDERA");
        rez.REZEPTART = rs.getString("REZEPTART");
        rez.LOGFREI1 = "T".equals(Optional.ofNullable(rs.getString("LOGFREI1"))
                               .orElse(""));
        rez.LOGFREI2 = "T".equals(Optional.ofNullable(rs.getString("LOGFREI2"))
                               .orElse(""));
        rez.NUMFREI1 = rs.getInt("NUMFREI1");
        rez.NUMFREI2 = rs.getInt("NUMFREI2");
        rez.CHARFREI1 = rs.getString("CHARFREI1");
        rez.CHARFREI2 = rs.getString("CHARFREI2");
        rez.TERMINE = rs.getString("TERMINE");
        rez.ID = rs.getInt("ID");
        rez.KTRAEGER = rs.getString("KTRAEGER");
        rez.KID = rs.getInt("KID");
        rez.PATID = rs.getInt("PATID");
        rez.ZZSTATUS = rs.getInt("ZZSTATUS");
        rez.LASTDATE = rs.getDate("LASTDATE") == null ? null
                : rs.getDate("LASTDATE")
                    .toLocalDate();
        rez.PREISGRUPPE = rs.getInt("PREISGRUPPE");
        rez.BEGRUENDADR = "T".equals(Optional.ofNullable(rs.getString("BEGRUENDADR"))
                                  .orElse(""));
        rez.HAUSBES = "T".equals(Optional.ofNullable(rs.getString("HAUSBES"))
                              .orElse(""));
        rez.INDIKATSCHL = rs.getString("INDIKATSCHL");
        rez.ANGELEGTVON = rs.getString("ANGELEGTVON");
        rez.BARCODEFORM = rs.getInt("BARCODEFORM");
        rez.DAUER = rs.getString("DAUER");
        rez.POS1 = rs.getString("POS1");
        rez.POS2 = rs.getString("POS2");
        rez.POS3 = rs.getString("POS3");
        rez.POS4 = rs.getString("POS4");
        rez.FREQUENZ = rs.getString("FREQUENZ");
        rez.LASTEDIT = rs.getString("LASTEDIT");
        rez.BERID = rs.getInt("BERID");
        rez.ARZTBERICHT = "T".equals(Optional.ofNullable(rs.getString("ARZTBERICHT"))
                                  .orElse(""));
        rez.LASTEDDATE = rs.getDate("LASTEDDATE") == null ? null
                : rs.getDate("LASTEDDATE")
                    .toLocalDate();
        rez.FARBCODE = rs.getString("FARBCODE");
        rez.RSPLIT = rs.getString("RSPLIT");
        rez.JAHRFREI = rs.getString("JAHRFREI");
        rez.UNTER18 = "T".equals(Optional.ofNullable(rs.getString("UNTER18"))
                              .orElse(""));
        rez.HBVOLL = "T".equals(Optional.ofNullable(rs.getString("HBVOLL"))
                             .orElse(""));
        rez.ABSCHLUSS = "T".equals(Optional.ofNullable(rs.getString("ABSCHLUSS"))
                                .orElse(""));
        rez.ZZREGEL = rs.getInt("ZZREGEL");
        rez.ANZAHLHB = rs.getInt("ANZAHLHB");
        rez.KUERZEL1 = rs.getString("KUERZEL1");
        rez.KUERZEL2 = rs.getString("KUERZEL2");
        rez.KUERZEL3 = rs.getString("KUERZEL3");
        rez.KUERZEL4 = rs.getString("KUERZEL4");
        rez.KUERZEL5 = rs.getString("KUERZEL5");
        rez.KUERZEL6 = rs.getString("KUERZEL6");
        rez.ICD10 = rs.getString("ICD10");
        rez.ICD10_2 = rs.getString("ICD10_2");

        return rez;
    }
}
