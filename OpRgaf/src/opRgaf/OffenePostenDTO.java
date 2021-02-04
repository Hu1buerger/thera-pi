package opRgaf;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mandant.IK;
import opRgaf.OffenePosten.Type;
import opRgaf.CommonTools.DateTimeFormatters;
import opRgaf.rezept.Money;
import opRgaf.rezept.Rezeptnummer;
import specs.ContractException;
import sql.DatenquellenFactory;

public class OffenePostenDTO {
    private static final Logger logger = LoggerFactory.getLogger(OffenePostenDTO.class);

    private static final String selectall = "  SELECT concat(t2.n_name, ', ',t2.v_name,', ',DATE_FORMAT(t2.geboren,'%d.%m.%Y'))as kennung,t1.rnr,t1.rdatum,t1.rgesamt, \n"
            + "           t1.roffen,t1.rgbetrag, t1.rpbetrag,t1.rbezdatum,t1.rmahndat1,t1.rmahndat2,t3.kassen_nam1,t1.reznr,t1.id,t1.pat_id \n"
            + "           FROM (SELECT v_nummer as rnr,v_datum as rdatum,v_betrag as rgesamt,v_offen as roffen,0 as rgbetrag, 0 as rpbetrag,\n"
            + "           v_bezahldatum as rbezdatum,mahndat1 as rmahndat1,mahndat2 as rmahndat2,'' as reznr,verklisteid as id,pat_id as pat_id \n"
            + "           FROM verkliste where v_nummer like 'VR-%' \n"
            + "            UNION SELECT rnr,rdatum,rgesamt,roffen,rgbetrag, rpbetrag,rbezdatum,rmahndat1,rmahndat2,reznr,id as id,pat_intern as pat_id \n"
            + "           FROM rgaffaktura ) t1 LEFT JOIN pat5 AS t2 ON (t1.pat_id = t2.pat_intern) LEFT JOIN kass_adr AS t3 ON ( t2.kassenid = t3.id )\n";

    private IK ik;

    public OffenePostenDTO(IK ik) {
        this.ik = ik;
    }

    private OffenePosten ofResultset(ResultSet rs) throws SQLException {
        OffenePosten ret = new OffenePosten();
        try {
            ret.kennung = new Kennung(rs.getString("kennung"));
        } catch (ContractException specs) {
            logger.error("Contract not met", specs);
            ret.kennung = new Kennung(rs.getString("kennung"), "", null);

        }
        ret.rgNr = rs.getString("rnr");
        ret.isStorniert = ret.rgNr != null && ret.rgNr.toLowerCase()
                                                      .contains("storno");
        ret.type = selectType(rs.getString("rnr"));

        LocalDate rdatum;
        try {
            rdatum = Date.valueOf(rs.getString("rDatum"))
                         .toLocalDate();
        } catch (Exception e) {
            rdatum = null;
        }
        ret.rgDatum = rdatum;
        ret.gesamtBetrag = Optional.ofNullable(rs.getString("rgesamt"))
                                   .map(Money::new)
                                   .orElse(new Money());
        ret.offen = Optional.ofNullable(rs.getString("roffen"))
                            .map(Money::new)
                            .orElse(new Money());
        ret.rgBetrag = Optional.ofNullable(rs.getString("rgbetrag"))
                               .map(Money::new)
                               .orElse(new Money());
        ret.bearbeitungsGebuehr = Optional.ofNullable(rs.getString("rpbetrag"))
                                          .map(Money::new)
                                          .orElse(new Money());
        ret.bezahltAm = Optional.ofNullable(rs.getDate("rbezdatum"))
                                .map(d -> d.toLocalDate())
                                .orElse(null);
        ret.mahnungEins = Optional.ofNullable(rs.getDate("rmahndat1"))
                                  .map(d -> d.toLocalDate())
                                  .orElse(null);
        ret.mahnungZwei = Optional.ofNullable(rs.getDate("rmahndat2"))
                                  .map(d -> d.toLocalDate())
                                  .orElse(null);
        ret.krankenKassenName = rs.getString("kassen_nam1");
        ret.rezNummer = new Rezeptnummer(rs.getString("reznr"));
        ret.tabellenId = rs.getInt("id");
        ret.patid = rs.getInt("pat_id");

        return ret;
    }

    private Type selectType(String rgNr) {
        if (rgNr == null)
            return Type.andere;
        if (rgNr.contains("RG")) {
            return Type.RGR;
        } else if (rgNr.contains("AF")) {
            return Type.AFR;
        } else if (rgNr.contains("VR")) {
            return Type.VR;
        } else {
            return Type.andere;
        }
    }

    public List<OffenePosten> all() throws SQLException {

        List<OffenePosten> rgafFakturaListe = new LinkedList<OffenePosten>();
        try (Connection con = new DatenquellenFactory(ik.digitString()).createConnection();) {
            ResultSet rs = con.createStatement()
                              .executeQuery(selectall);
            while (rs.next()) {

                try {
                    rgafFakturaListe.add(ofResultset(rs));
                } catch (Exception e) {
                   logger.error("Fehler beim Einlesen der offenen Posten bei ID: " + rs.getString("id"), e);
                }

            }

            return rgafFakturaListe;
        }

    }

    boolean updatePayment(OffenePosten op) {
        if (op.type == Type.VR) {
            return updateVr(op);
        } else {
            return updateRGR(op);
        }
    }

    private boolean updateRGR(OffenePosten op) {

        String updateRGRSQL = generatePayment(op);

        try (Connection con = new DatenquellenFactory(ik.digitString()).createConnection();
                Statement stmt = con.createStatement()) {
            stmt.executeUpdate(updateRGRSQL, Statement.RETURN_GENERATED_KEYS);

        } catch (SQLException e) {
            logger.error("coud not save OffenPosten " + op, e);
            return false;
        }
        return true;

    }

    private String einklammern(String value) {
        return value == null ? null : "'" + value + "'";
    }

    String generatePayment(OffenePosten op) {

        String sql = new StringBuilder().append("UPDATE rgaffaktura")
                                        .append(" SET rgesamt= ")
                                        .append(op.gesamtBetrag.toPlainString())
                                        .append(" , roffen = ")
                                        .append(op.offen.toPlainString())
                                        .append(" , rgbetrag =")
                                        .append(op.rgBetrag)
                                        .append(" , rpbetrag =")
                                        .append(op.bearbeitungsGebuehr)
                                        .append(" , rdatum = ")
                                        .append(einklammern(op.rgDatum != null
                                                ? op.rgDatum.format(DateTimeFormatters.yyyyMMddmitBindestrich)
                                                : null))

                                        .append(" , rbezdatum = ")
                                        .append(einklammern(op.bezahltAm != null
                                                ? op.bezahltAm.format(DateTimeFormatters.yyyyMMddmitBindestrich)
                                                : null))

                                        .append(" , rmahndat1 = ")
                                        .append(einklammern(op.mahnungEins != null
                                                ? op.mahnungEins.format(DateTimeFormatters.yyyyMMddmitBindestrich)
                                                : null))

                                        .append(" , rmahndat2 = ")
                                        .append(einklammern(op.mahnungZwei != null
                                                ? op.mahnungZwei.format(DateTimeFormatters.yyyyMMddmitBindestrich)
                                                : null))
                                        .append(" where id = ")
                                        .append(op.tabellenId)
                                        .toString();
        return sql;
    }

    private boolean updateVr(OffenePosten op) {


            String updateRGRSQL = generateVRPayment(op);

            try (Connection con = new DatenquellenFactory(ik.digitString()).createConnection();
                    Statement stmt = con.createStatement()) {
                stmt.executeUpdate(updateRGRSQL, Statement.RETURN_GENERATED_KEYS);

            } catch (SQLException e) {
                logger.error("coud not save OffenPosten " + op, e);
                return false;
            }
            return true;

        }

    private String generateVRPayment(OffenePosten op) {

        String sql = new StringBuilder().append("UPDATE verkliste")
        .append(" SET v_betrag= ")
        .append(op.gesamtBetrag.toPlainString())
        .append(" , v_offen = ")
        .append(op.offen.toPlainString())
        .append(" , v_betrag =")
        .append(op.rgBetrag)

        .append(" , v_datum = ")
        .append(einklammern(op.rgDatum != null
                ? op.rgDatum.format(DateTimeFormatters.yyyyMMddmitBindestrich)
                : null))

        .append(" , v_bezahldatum = ")
        .append(einklammern(op.bezahltAm != null
                ? op.bezahltAm.format(DateTimeFormatters.yyyyMMddmitBindestrich)
                : null))

        .append(" , mahndat1 = ")
        .append(einklammern(op.mahnungEins != null
                ? op.mahnungEins.format(DateTimeFormatters.yyyyMMddmitBindestrich)
                : null))

        .append(" , mahndat2 = ")
        .append(einklammern(op.mahnungZwei != null
                ? op.mahnungZwei.format(DateTimeFormatters.yyyyMMddmitBindestrich)
                : null))
        .append(" where verklisteID = ")
        .append(op.tabellenId)
        .toString();
        return sql;
    }


}
