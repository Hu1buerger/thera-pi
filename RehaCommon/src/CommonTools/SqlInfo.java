package CommonTools;

import java.io.InputStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlInfo.class);
    static Connection conn = null;
    private static InetAddress dieseMaschine;

    public SqlInfo(JFrame frame, Connection conn, InetAddress dieseMaschine) {
        SqlInfo.conn = conn;
        SqlInfo.dieseMaschine = dieseMaschine;
    }

    public SqlInfo(JFrame frame, Connection conn) {
        SqlInfo.conn = conn;
        SqlInfo.dieseMaschine = null;
    }

    public SqlInfo() {
        SqlInfo.conn = null;
        SqlInfo.dieseMaschine = null;
    }

    public void setFrame(JFrame frame) {
    }

    public void setConnection(Connection conn) {
        SqlInfo.conn = conn;
    }

    public void setDieseMaschine(InetAddress dieseMaschine) {
        SqlInfo.dieseMaschine = dieseMaschine;
    }

    public JFrame getFrame() {
        return null;
    }

    public Connection getConnection() {
        return SqlInfo.conn;
    }

    public InetAddress getDieseMaschine() {
        return SqlInfo.dieseMaschine;
    }

    /***********************************/
    public static void loescheLocksMaschine() {
        int stelle = dieseMaschine.toString()
                                  .indexOf("/");
        String maschine = dieseMaschine.toString()
                                       .substring(0, stelle);
        SqlInfo.sqlAusfuehren("delete from flexlock where maschine like '%" + maschine + "%'");
    }

    public static boolean gibtsSchon(String sstmt) {
        boolean gibtsschon = false;

        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery(sstmt);

            if (rs.next()) {
                gibtsschon = true;
            }

        } catch (SQLException ev) {
            // System.out.println("SQLException: " + ev.getMessage());
            // System.out.println("SQLState: " + ev.getSQLState());
            // System.out.println("VendorError: " + ev.getErrorCode());
        }
        return gibtsschon;
    }

    /**
     * Deprecated use {@link ResultSet} getGeneratedKeys() .
     * 
     * @param tabelle
     * @param feld
     * @return
     */
    @Deprecated
    public static int holeId(String tabelle, String feld) {
        int retid = -1;

        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = null;

            String createID = "insert into " + tabelle + " set " + feld + " = '" + dieseMaschine + "'";
            stmt.execute(createID);
            String selectID = "select id from " + tabelle + " where " + feld + " = '" + dieseMaschine + "'";
            rs = stmt.executeQuery(selectID);
            if (rs.next()) {
                retid = rs.getInt("id");
            }
        } catch (SQLException ev) {
            // System.out.println("SQLException: " + ev.getMessage());
            // System.out.println("SQLState: " + ev.getSQLState());
            // System.out.println("VendorError: " + ev.getErrorCode());
        }
        return retid;
    }

    /**
     * 
     * Deprecated use {@link ResultSet} getGeneratedKeys() .
     **/
    @Deprecated
    public static int holeIdSimple(String tabelle, String befehl) {
        int retid = -1;

        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);) {
            stmt.execute(befehl);
            try (ResultSet rs = stmt.executeQuery("select max(id) from " + tabelle);) {
                if (rs.next()) {
                    retid = rs.getInt(1);
                }

            } catch (SQLException e) {

                e.printStackTrace();
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        return retid;
    }

    /*******************************/

    public static Vector<String> holeSatz(String tabelle, String felder, String kriterium, List<?> ausschliessen) {
        Vector<String> retvec = new Vector<String>();

        String sstmt = "select " + felder + " from " + tabelle + " where " + kriterium + " LIMIT 1";
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt);) {

            int nichtlesen = ausschliessen.size();
            if (rs.next()) {
                ResultSetMetaData rsMetaData = rs.getMetaData();
                int numberOfColumns = rsMetaData.getColumnCount() + 1;
                for (int i = 1; i < numberOfColumns; i++) {
                    if (nichtlesen > 0) {
                        if (!ausschliessen.contains(rsMetaData.getColumnName(i))) {
                            retvec.add((rs.getString(i) == null ? "" : rs.getString(i)));
                        }
                    } else {
                        retvec.add((rs.getString(i) == null ? "" : rs.getString(i)));
                    }
                }
            }
        } catch (SQLException ev) {
            LOGGER.error(sstmt, ev);
        }
        return retvec;
    }

    public static Vector<String> holeSatzLimit(String tabelle, String felder, String kriterium, int[] limit,
            List<?> ausschliessen) {
        String sstmt = "select " + felder + " from " + tabelle + " " + kriterium + " LIMIT "
                + Integer.toString(limit[0]) + "," + Integer.toString(limit[1]) + "";
        Vector<String> retvec = new Vector<String>();
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt);) {

            int nichtlesen = ausschliessen.size();
            if (rs.next()) {
                ResultSetMetaData rsMetaData = rs.getMetaData();
                int numberOfColumns = rsMetaData.getColumnCount() + 1;
                for (int i = 1; i < numberOfColumns; i++) {
                    if (nichtlesen > 0) {
                        if (!ausschliessen.contains(rsMetaData.getColumnName(i))) {
                            retvec.add((rs.getString(i) == null ? "" : rs.getString(i)));
                        }
                    } else {
                        retvec.add((rs.getString(i) == null ? "" : rs.getString(i)));
                    }
                }
            }
        } catch (SQLException ev) {
            LOGGER.error(sstmt, ev);
        }
        return retvec;
    }

    /*****************************************/
    public static Vector<String> holeFeldNamen(String tabelle, boolean ausnahmen, List<?> lausnahmen) {
        Vector<String> vec = new Vector<String>();
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery("describe " + tabelle);
            while (rs.next()) {
                if (ausnahmen) {
                    if (!lausnahmen.contains(rs.getString(1)
                                               .toLowerCase())) {
                        vec.add(rs.getString(1)
                                  .toLowerCase());
                    }
                } else {
                    vec.add(rs.getString(1)
                              .toLowerCase());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vec;
    }

    public static Vector<String> holeFeldForUpdate(String tabelle, String feld, String kriterium) {
        Vector<String> retvec = new Vector<String>();
        String sstmt = "select " + feld + " from " + tabelle + kriterium;

        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery(sstmt);
            if (rs.next()) {
                retvec.add((rs.getString(1) == null ? "" : rs.getString(1)));
                retvec.add((rs.getString(2) == null ? "" : rs.getString(2)));
            }
        } catch (SQLException ev) {
            LOGGER.error(sstmt, ev);
        }
        return retvec;
    }

    /*******************************/

    public static Vector<Vector<String>> holeSaetze(String tabelle, String felder, String kriterium,
            List<String> ausschliessen) {
        Vector<Vector<String>> retkomplett = new Vector<Vector<String>>();
        ResultSetMetaData rsMetaData = null;
        String sstmt = "select " + felder + " from " + tabelle + " where " + kriterium;
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt);) {

            int nichtlesen = ausschliessen.size();
            while (rs.next()) {
                try {
                    Vector<String> retvec = new Vector<String>();
                    rsMetaData = rs.getMetaData();
                    int numberOfColumns = rsMetaData.getColumnCount() + 1;
                    for (int i = 1; i < numberOfColumns; i++) {
                        if (nichtlesen > 0) {
                            if (!ausschliessen.contains(rsMetaData.getColumnName(i))) {
                                retvec.add((rs.getString(i) == null ? "" : rs.getString(i)));
                            }
                        } else {
                            retvec.add((rs.getString(i) == null ? "" : rs.getString(i)));
                        }
                    }
                    retkomplett.add(retvec);
                } catch (Exception ex) {
                    LOGGER.error(sstmt, ex);
                }
            }
        } catch (SQLException ev) {
            LOGGER.error(sstmt, ev);
        }
        return retkomplett;
    }

    /*****************************************/
    public static String macheWhereKlausel(String praefix, String test, String[] suchein) {
        // paraefix = wenn der eine fixe Bedinung vorangestellt wird z.B.
        // "(name='steinhilber') AND " bzw. "" fals keine notwendig
        // test = der suchbegriff bzw. die durch Leerzeichen getrennte suchbegriffe
        // suchein[] sind die spalten bzw. die spalte die durchsucht werden soll
        // werden mehrere suchbegriffe eingegeben, bezogen auf die Begriffe -> AND-Suche
        // innerhalb der spalten, bezogen auf die Spalten -> OR-Suche
        String ret = praefix;
        String cmd = test;
        // zun�chst versuchen da� immer nur ein Leerzeichen zwischen den Begriffen
        // existiert
        cmd = cmd.replaceAll("   ", " ");
        cmd = cmd.replaceAll("  ", " ");
        // wer jetzt immer noch Leerzeichen in der Suchbedingung hat ist selbst schuld
        // daß er nix finder!!!
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

    /***********************************/
    private static String toRTF(String toConvert) {
        String convertet = "";
        convertet = toConvert.replace("Ö", "\\\\\\\\\\'d6")
                             .replace("ö", "\\\\\\\\\\'f6");
        convertet = convertet.replace("Ä", "\\\\\\\\\\'c4")
                             .replace("ä", "\\\\\\\\\\'e4");
        convertet = convertet.replace("Ü", "\\\\\\\\\\'dc")
                             .replace("ü", "\\\\\\\\\\'fc");
        convertet = convertet.replace("ß", "\\\\\\\\\\'df");
        return String.valueOf(convertet);
    }

    /***********************************/
    public static String macheWhereKlauselRTF(String praefix, String test, String[] suchein) {
        String ret = praefix;
        String cmd = test;
        cmd = cmd.replaceAll("   ", " ");
        cmd = cmd.replaceAll("  ", " ");
        String[] felder = suchein;
        String[] split = cmd.split(" ");
        if (split.length == 1) {
            ret = ret + " (";
            for (int i = 0; i < felder.length; i++) {
                if (i == 0) {
                    ret = ret + felder[i] + " like '%" + cmd + "%'";
                } else {
                    ret = ret + felder[i] + " like '%" + toRTF(cmd) + "%'";
                }

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
                    if (i2 == 0) {
                        ret = ret + felder[i2] + " like '%" + split[i] + "%'";
                    } else {
                        ret = ret + felder[i2] + " like '%" + toRTF(split[i]) + "%'";
                    }

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

    public static int erzeugeNummer(String nummer) {
        int reznr = -1;
        /****** Zunächst eine neue Rezeptnummer holen ******/
        Vector<String> numvec = null;
        try {
            conn.setAutoCommit(false);
            numvec = SqlInfo.holeFeldForUpdate("nummern", nummer + ",id", " FOR UPDATE");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (numvec.size() > 0) {
            try {
                reznr = Integer.parseInt(numvec.get(0));
                String cmd = "update nummern set " + nummer + "='" + (reznr + 1) + "' where id='" + numvec.get(1) + "'";
                SqlInfo.sqlAusfuehren(cmd);
            } catch (Exception ex) {
                reznr = -1;
            }
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {

                e.printStackTrace();
            }
        } else {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException e) {

                e.printStackTrace();
            }

        }
        numvec = null;
        return reznr;

    }

    public static int erzeugeNummerMitMax(String nummer, int max) {
        int reznr = -1;
        /****** Zunächst eine neue Rezeptnummer holen ******/
        Vector<String> numvec = new Vector<String>();
        try {
            conn.setAutoCommit(false);
            numvec = SqlInfo.holeFeldForUpdate("nummern", nummer + ",id", " FOR UPDATE");
        } catch (SQLException e) {

            e.printStackTrace();
        }
        if (numvec.size() > 0) {
            reznr = Integer.parseInt(numvec.get(0));
            if ((reznr + 1) > max) {
                reznr = 1;
            }
            String cmd = "update nummern set " + nummer + "='" + (reznr + 1) + "' where id='" + numvec.get(1) + "'";
            new ExUndHop().setzeStatement(cmd);
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {

                e.printStackTrace();
            }
        } else {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException e) {

                e.printStackTrace();
            }

        }
        return reznr;

    }

    /*******************************************/
    public static int zaehleSaetze(String tabelle, String bedingung) {
        int retid = -1;

        String sstmt = "select count(*) from " + tabelle + " where " + bedingung;
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt);) {
            if (rs.next()) {
                retid = rs.getInt(1);
            }
        } catch (SQLException ev) {
            LOGGER.error(sstmt, ev);
        }
        return retid;
    }

    /*******************************/
    public static void aktualisiereSatz(String tabelle, String sets, String kriterium) {

        String sstmt = "update " + tabelle + " set " + sets + " where " + kriterium + " LIMIT 1";
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);) {
            stmt.execute(sstmt);
        } catch (SQLException ev) {
            LOGGER.error(sstmt, ev);
        }
        return;
    }

    /*******************************/
    public static void aktualisiereSaetze(String tabelle, String sets, String kriterium) {

        String sstmt = "update " + tabelle + " set " + sets + " where " + kriterium;
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);) {

            stmt.execute(sstmt);
        } catch (SQLException ev) {
            LOGGER.error(sstmt, ev);
        }
    }

    public static String holePatFeld(String feld, String kriterium) {
        String ret = "";

        String sstmt = "select " + feld + " from pat5 where " + kriterium + " LIMIT 1";
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt);) {
            if (rs.next()) {
                ret = (rs.getString(feld) == null ? "" : rs.getString(feld));
            }
        } catch (SQLException ev) {
            LOGGER.error(sstmt, ev);
        }
        return ret;
    }

    /*****************************************/
    public static String holeEinzelFeld(String sstmt) {
        String ret = "";
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt)) {
            if (rs.next()) {
                ret = (rs.getString(1) == null ? "" : rs.getString(1)).trim();
            }
            return ret;
        } catch (SQLException ex) {
            LOGGER.error(sstmt, ex);
        }
        return ret;
    }

    /*****************************************/

    /*****************************************/

    public static Vector<Vector<String>> holeFelder(String sstmt) {
        Vector<Vector<String>> retkomplett = new Vector<Vector<String>>();
        int numberOfColumns = 0;
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt);) {
            while (rs.next()) {
                Vector<String> retvec = new Vector<String>();
                ResultSetMetaData rsMetaData = rs.getMetaData();
                numberOfColumns = rsMetaData.getColumnCount() + 1;
                for (int i = 1; i < numberOfColumns; i++) {
                    retvec.add((rs.getString(i) == null ? "" : rs.getString(i)));

                }
                retkomplett.add(retvec);
            }
        } catch (SQLException ev) {
            LOGGER.error(sstmt, ev);
        }

        return retkomplett;
    }

    /*****************************************/

    /*****************************************/
    public static String holeRezFeld(String feld, String kriterium) {

        String ret = "";

        String sstmt = "select " + feld + " from verordn where " + kriterium + " LIMIT 1";
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt);) {

            if (rs.next()) {
                ret = (rs.getString(feld) == null ? "" : rs.getString(feld));
            }
        } catch (SQLException ev) {
            LOGGER.error(sstmt, ev);
        }
        return ret;
    }

    public static Vector<String> holeFeld(String sstmt) {

        Vector<String> vecret = new Vector<String>();
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt);) {
            String ret = "";

            while (rs.next()) {
                ret = (rs.getString(1) == null ? "" : rs.getString(1));
                vecret.add(String.valueOf(ret));
            }
        } catch (SQLException ev) {
            LOGGER.error("Could not execute: " + sstmt, ev);
        }
        return vecret;
    }

    /*****************************************/
    public static boolean sqlAusfuehren(String sstmt) {

        boolean ret = true;
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);) {

            stmt.execute(sstmt);
        } catch (SQLException ev) {
            ev.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fehler bei der Ausführung des Statements\nMethode:sqlAusfuehren("
                    + sstmt + ")\n\nBitte informieren Sie sofort den Administrator!!!");
            ret = false;
        }
        return ret;
    }

    public static InputStream holeStream(String tabelle, String feld, String kriterium) {
        InputStream is = null;
        String sstmt = "select " + feld + " from " + tabelle + " where " + kriterium + " LIMIT 1";

        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt);) {
            if (rs.next()) {
                is = rs.getBinaryStream(1);
            }
        } catch (SQLException ev) {
            LOGGER.error(sstmt, ev);
        }
        return is;
    }

    public static boolean transferRowToAnotherDB(String sourcedb, String targetdb, String dbfield, String argument,
            boolean ausnahmen, List<?> lausnahmen) {

        boolean ret = false;
        StringBuffer transferBuf = new StringBuffer();
        StringBuffer insertBuf = new StringBuffer();
        Vector<String> feldNamen = SqlInfo.holeFeldNamen(sourcedb, ausnahmen, lausnahmen);
        transferBuf.append("select ");
        int rezeptFelder = 0;
        for (int i = 0; i < feldNamen.size(); i++) {
            if (i > 0) {
                transferBuf.append("," + feldNamen.get(i));
            } else {
                transferBuf.append(feldNamen.get(i));
            }
        }
        transferBuf.append(" from " + sourcedb + " where " + dbfield + "='" + argument + "' LIMIT 1");
        Vector<Vector<String>> vec = SqlInfo.holeFelder(transferBuf.toString());

        if (vec.size() <= 0) {
            return false;
        }
        try {
            rezeptFelder = vec.get(0)
                              .size();
            insertBuf.append("insert into " + targetdb + " set ");
            for (int i = 0; i < rezeptFelder; i++) {
                if (!vec.get(0)
                        .get(i)
                        .equals("")) {
                    if (i > 0) {
                        insertBuf.append("," + feldNamen.get(i) + "='" + StringTools.Escaped(vec.get(0)
                                                                                                .get(i))
                                + "'");
                    } else {
                        insertBuf.append(feldNamen.get(i) + "='" + StringTools.Escaped(vec.get(0)
                                                                                          .get(i))
                                + "'");
                    }
                }
            }
            SqlInfo.sqlAusfuehren(insertBuf.toString());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public static String liesIniAusTabelle(String inifilename) {
        String result = null;

        final String preparedStatementString = "SELECT inhalt FROM inidatei WHERE dateiname=? LIMIT 1";
        try (PreparedStatement preparedStatement = conn.prepareStatement(preparedStatementString,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setString(1, inifilename);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    result = rs.getString("inhalt");
                }
            }
        } catch (SQLException e) {
            LOGGER.error(inifilename, e);
        }
        return result;
    }

    public static boolean schreibeIniInTabelle(String inifilename, byte[] buf) {

        boolean result;
        String insertOrUpdateStatementString = determineIfInsertOrUpdateMustPerformed(inifilename);
        try (PreparedStatement ps = conn.prepareStatement(insertOrUpdateStatementString,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            ps.setString(2, inifilename);
            ps.setBytes(1, buf);
            ps.execute();
            result = true;
        } catch (SQLException ex) {
            LOGGER.error("Exception during performing sql statement " + insertOrUpdateStatementString
                    + " with following values. dateiname=" + inifilename + ", inhalt=" + buf, ex);
            result = false;
        } catch (Exception ex) {
            LOGGER.error("Exception during check if update or insert is needed!", ex);
            result = false;
        }
        return result;
    }

    private static String determineIfInsertOrUpdateMustPerformed(String inifilename) {
        final String existsEntryStatementString = "select dateiname from inidatei where dateiname='" + inifilename
                + "' LIMIT 1";
        String insertOrUpdateStatementString;
        try {
            if (SqlInfo.holeEinzelFeld(existsEntryStatementString)
                       .isEmpty()) {
                insertOrUpdateStatementString = "insert into inidatei set inhalt = ?, dateiname = ?";
            } else {
                insertOrUpdateStatementString = "update inidatei set inhalt = ? where dateiname = ?";
            }
        } catch (Exception ex) {
            throw new RuntimeException("Exception during performing sql statement " + existsEntryStatementString, ex);
        }
        return insertOrUpdateStatementString;
    }

}
