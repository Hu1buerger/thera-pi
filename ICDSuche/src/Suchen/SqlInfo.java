package Suchen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.Vector;

class SqlInfo {
    private Connection conn;

    public SqlInfo(Connection con) {
        this.conn = con;
    }

    Vector<Vector<String>> holeFelder(String xstmt) {

        Statement stmt = null;
        ResultSet rs = null;

        Vector<Vector<String>> retkomplett = new Vector<Vector<String>>();
        try {
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException e) {

            e.printStackTrace();
        }
        try {

            String sstmt = xstmt;
            rs = stmt.executeQuery(sstmt);

            while (rs.next()) {
                Vector<String> retvec = new Vector<String>();
                ResultSetMetaData rsMetaData = rs.getMetaData();
                int numberOfColumns = rsMetaData.getColumnCount() + 1;
                for (int i = 1; i < numberOfColumns; i++) {
                    retvec.add((rs.getString(i) == null ? "" : rs.getString(i)));

                }
                retkomplett.add(retvec);
            }

        } catch (SQLException ev) {
            System.out.println("SQLException: " + ev.getMessage());
            System.out.println("SQLState: " + ev.getSQLState());
            System.out.println("VendorError: " + ev.getErrorCode());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { // ignore }
                    rs = null;
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { // ignore }
                    stmt = null;
                }
            }
        }
        return retkomplett;
    }

    Vector<Vector<String>> suchICD(String suchtext, SearchType searchType, int ilimit) {

        SearchParameter param = new SearchParameter(searchType, suchtext, ilimit);

        String slimit = (param.limit() > 0 ? "LIMIT " + Integer.toString(param.limit()) : "");
        String where;
        switch (searchType) {
        case ICD:

            where = SqlInfo.macheWhereKlausel(null, suchtext, new String[] { "schluessel2", "schluessel1" });
            break;
        case TEXT:
            where = SqlInfo.macheWhereKlausel(suchtext, "icdtext");
            break;
        default:
            where = "";
        }

        String cmd = "select schluessel1,schluessel2,titelzeile,id,icdtext from icd10 where " + where + slimit;
        return holeFelder(cmd);
    }

    static String macheWhereKlausel(String praefix, String test, String[] suchein) {
        // paraefix = wenn der eine fixe Bedinung vorangestellt wird z.B.
        // "(name='steinhilber') AND " bzw. "" fals keine notwendig
        // test = der suchbegriff bzw. die durch Leerzeichen getrennte suchbegriffe
        // suchein[] sind die spalten bzw. die spalte die durchsucht werden soll
        // werden mehrere suchbegriffe eingegeben, bezogen auf die Begriffe -> AND-Suche
        // innerhalb der spalten, bezogen auf die Spalten -> OR-Suche
        String ret = Optional.ofNullable(praefix)
                             .orElse("");
        String cmd = test.trim();

        cmd = new String(cmd.replaceAll("\\s+", " "));

        /*
         * String[] felder = suchein; String[] split = cmd.split(" ");
         * if(split.length==1){ ret = ret +" ("; for(int i = 0; i < felder.length;i++){
         * ret = ret+felder[i]+" like '%"+cmd+"%'"; if(i < felder.length-1){ ret = ret+
         * " OR "; } } ret = ret +") "; return ret; }
         *
         *
         * ret = ret +"( "; for(int i = 0; i < split.length;i++){ if(!
         * split[i].equals("")){ ret = ret +" ("; for(int i2 = 0; i2 <
         * felder.length;i2++){ ret = ret+felder[i2]+" like '%"+split[i]+"%'"; if(i2 <
         * felder.length-1){ ret = ret+ " OR "; } } ret = ret +") "; if(i <
         * split.length-1){ ret = ret+ " AND "; } }
         *
         * } ret = ret +") "; return ret;
         */
        String[] felder = suchein;
        String[] split = cmd.split(" ");
        if (split.length == 1) {
            ret = ret + " (";
            for (int i = 0; i < felder.length; i++) {
                ret = ret + felder[i] + " like '%" + cmd + "%'";
                if (i < felder.length - 1) {
                    ret = ret + " OR ";
                }
            }
            ret = ret + ") ";
            return ret;
        }

        ret = ret + "( ";
        for (int i = 0; i < split.length; i++) {
            if (!split[i].equals("")) {
                ret = ret + " (";
                for (int i2 = 0; i2 < felder.length; i2++) {
                    ret = ret + felder[i2] + " like '%" + split[i] + "%'";
                    if (i2 < felder.length - 1) {
                        ret = ret + " OR ";
                    }
                }
                ret = ret + ") ";
                if (i < split.length - 1) {
                    ret = ret + " AND ";
                }
            }

        }
        ret = ret + ") ";
        return ret;

    }

    public static String macheWhereKlausel(String test, String feld) {

        String cmd = test.trim();

        cmd = new String(cmd.replaceAll("\\s+", " "));

        String[] split = cmd.split(" ");

        String ret = "";
        for (int i = 0; i < split.length; i++) {
            if (!split[i].equals("")) {
                ret = ret + " (";

                ret = ret + feld + " like '%" + split[i] + "%'";

                ret = ret + ") ";
                if (i < split.length - 1) {
                    ret = ret + " AND ";
                }
            }

        }

        if (split.length > 1)
            ret = "( " + ret + ") ";
        return ret;

    }

}
