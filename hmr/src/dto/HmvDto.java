package dto;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import hmv.Hmv;
import sql.DatenquellenFactory;

public class HmvDto {
    int id;
    String disziplin;
    private int nummer;
    private int pat_id;
    private int kk_id;
    private String betriebs_id;
    private int arzt_id;
    private String pat_vers_nummer;
    private int pat_vers_status;
    private java.sql.Date datum;
    private String icd10_1;
    private String icd10_2;
    private String icd10_text;
    private String diagnosegruppe;
    private String leitsymptomatik;
    private String leitsymptomatik_text;
    private int frequenz_von;
    private int frequent_bis;
    private int dauer;
    private int angelegt_von;
    private java.sql.Date angelegt_am;
    private static final String SQL_INSERT = "INSERT INTO hmv ("
            + "disziplin,nummer,pat_id,kk_id,betriebs_id,arzt_id,pat_vers_nummer,pat_vers_status,datum,icd10_1,icd10_2,icd10_text,diagnosegruppe,leitsymptomatik,leitsymptomatik_text,frequenz_von,frequent_bis,dauer,angelegt_von,angelegt_am) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    int insert(DatenquellenFactory dq) throws SQLException {

        Connection conn = dq.createConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);

        preparedStatement.setString(1, disziplin);
        preparedStatement.setInt(2, nummer);
        preparedStatement.setInt(3, pat_id);
        preparedStatement.setInt(4, kk_id);
        preparedStatement.setString(5, betriebs_id);
        preparedStatement.setInt(6, arzt_id);
        preparedStatement.setString(7, pat_vers_nummer);
        preparedStatement.setInt(8, pat_vers_status);
        preparedStatement.setDate(9, datum);
        preparedStatement.setString(10, icd10_1);
        preparedStatement.setString(11, icd10_2);
        preparedStatement.setString(12, icd10_text);
        preparedStatement.setString(13, diagnosegruppe);
        preparedStatement.setString(14, leitsymptomatik);
        preparedStatement.setString(15, leitsymptomatik_text);
        preparedStatement.setInt(16, frequenz_von);
        preparedStatement.setInt(17, frequent_bis);
        preparedStatement.setInt(18, dauer);
        preparedStatement.setInt(19, angelegt_von);
        preparedStatement.setDate(20, Date.valueOf(LocalDate.now()));

        preparedStatement.executeUpdate();
        ResultSet rs = preparedStatement.getGeneratedKeys();
        if (rs.next()) {
            int rid = rs.getInt(1);
            return rid;
        }
        return -1;
    }
    private static final String SQL_UPDATE = "UPDATE hmv SET "
            + "disziplin= ? ,nummer= ? ,pat_id= ? ,kk_id= ? ,betriebs_id= ? ,"
            + "arzt_id= ? ,pat_vers_nummer= ? ,pat_vers_status= ? ,datum= ? ,icd10_1= ? ,"
            + "icd10_2= ? ,icd10_text= ? ,diagnosegruppe= ? ,leitsymptomatik= ? ,leitsymptomatik_text= ? ,"
            + "frequenz_von= ? ,frequent_bis= ? ,dauer= ? ,angelegt_von= ? ,angelegt_am = ? "
            + "WHERE id= ?";



    int update(DatenquellenFactory dq) throws SQLException {

        Connection conn = dq.createConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(SQL_UPDATE, Statement.RETURN_GENERATED_KEYS);

        preparedStatement.setString(1, disziplin);
        preparedStatement.setInt(2, nummer);
        preparedStatement.setInt(3, pat_id);
        preparedStatement.setInt(4, kk_id);
        preparedStatement.setString(5, betriebs_id);
        preparedStatement.setInt(6, arzt_id);
        preparedStatement.setString(7, pat_vers_nummer);
        preparedStatement.setInt(8, pat_vers_status);
        preparedStatement.setDate(9, datum);
        preparedStatement.setString(10, icd10_1);
        preparedStatement.setString(11, icd10_2);
        preparedStatement.setString(12, icd10_text);
        preparedStatement.setString(13, diagnosegruppe);
        preparedStatement.setString(14, leitsymptomatik);
        preparedStatement.setString(15, leitsymptomatik_text);
        preparedStatement.setInt(16, frequenz_von);
        preparedStatement.setInt(17, frequent_bis);
        preparedStatement.setInt(18, dauer);
        preparedStatement.setInt(19, angelegt_von);
        preparedStatement.setDate(20, angelegt_am);

        preparedStatement.setInt(21, id);



        return    preparedStatement.executeUpdate();
    }

    public HmvDto(Hmv hmv) {

        id = hmv.id;
        disziplin = hmv.disziplin.toString();
        nummer = hmv.nummer.ziffern;
        pat_id = hmv.patient.db_id;
        kk_id = hmv.kv.getKk()
                      .get()
                      .getId();
        betriebs_id = hmv.arzt.getBsnr();
        arzt_id = hmv.arzt.getId();
        pat_vers_nummer = hmv.kv.getVersicherungsnummer();
        pat_vers_status = hmv.kv.getStatus()
                                .getNummer();
        datum = Date.valueOf(hmv.ausstellungsdatum);
        icd10_1 = hmv.diag.icd10_1.schluessel();
        icd10_2 = hmv.diag.icd10_2.schluessel();
        icd10_text = hmv.diag.text;
        diagnosegruppe = hmv.diag.leitsymptomatik.diagnosegruppe.gruppe;
        leitsymptomatik = hmv.diag.leitsymptomatik.kennung;
        leitsymptomatik_text = hmv.diag.leitsymptomatik.text;
        frequenz_von = hmv.beh.frequenzmin;
        frequent_bis = hmv.beh.frequenzmax;
        dauer = hmv.beh.dauer;
        angelegt_von = hmv.angelegtvon.dbId;
        angelegt_am =  Date.valueOf(hmv.angelegt_am);

        System.out.println(hmv);
        System.out.println(this);
    }

    @Override
    public String toString() {
        return "HmvDto [id=" + id + ", disziplin=" + disziplin + ", nummer=" + nummer + ", pat_id=" + pat_id
                + ", kk_id=" + kk_id + ", betriebs_id=" + betriebs_id + ", arzt_id=" + arzt_id + ", pat_vers_nummer="
                + pat_vers_nummer + ", pat_vers_status=" + pat_vers_status + ", datum=" + datum + ", icd10_1=" + icd10_1
                + ", icd10_2=" + icd10_2 + ", icd10_text=" + icd10_text + ", diagnosegruppe=" + diagnosegruppe
                + ", leitsymptomatik=" + leitsymptomatik + ", leitsymptomatik_text=" + leitsymptomatik_text
                + ", frequenz_von=" + frequenz_von + ", frequent_bis=" + frequent_bis + ", dauer=" + dauer
                + ", angelegt_von=" + angelegt_von + "]";
    }

}
