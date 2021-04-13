package CommonTools;

import static gui.Cursors.normalCursor;
import static gui.Cursors.wartenCursor;

import java.io.InputStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlInfo {
    static Logger logger = LoggerFactory.getLogger(SqlInfo.class);
    private static JFrame frame;
    static Connection conn;
    private static InetAddress dieseMaschine;

    public SqlInfo(JFrame frame, Connection conn) {
        SqlInfo.frame = frame;
        SqlInfo.conn = conn;
        dieseMaschine = null;
    }

    public SqlInfo() {
        frame = null;
        conn = null;
        dieseMaschine = null;
    }

    public void setFrame(JFrame frame) {
        SqlInfo.frame = frame;
    }

    public void setConnection(Connection conn) {
        SqlInfo.conn = conn;
    }

    public void setDieseMaschine(InetAddress dieseMaschine) {
        if (dieseMaschine == null) return;
        SqlInfo.dieseMaschine = dieseMaschine;
    }

    public JFrame getFrame() {
        return frame;
    }

    public Connection getConnection() {
        return conn;
    }

    public InetAddress getDieseMaschine() {
        return dieseMaschine;
    }

    public static void loescheLocksMaschine() {
        if (dieseMaschine == null) {
            logger.info("dieseMaschine not set");
            return;
        }
        int stelle = dieseMaschine.toString()
                                  .indexOf('/');
        String maschine = dieseMaschine.toString()
                                       .substring(0, stelle);
        sqlAusfuehren("delete from flexlock where maschine like '%" + maschine + "%'");
    }

    public static boolean gibtsSchon(String sstmt) {
        boolean gibtsschon = false;
        imbusy();
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt)) {

            if (rs.next()) {
                gibtsschon = true;
            }

        } catch (SQLException ev) {
            logger.error("something bad happens here", ev);
        }
        imdone();
        return gibtsschon;
    }

    private static void imdone() {
        if (frame != null) {
            frame.setCursor(normalCursor);
        }
    }

    private static void imbusy() {
        if (frame != null) {
            frame.setCursor(wartenCursor);
        }
    }

    public static int holeId(String tabelle, String feld) {
        int retid = -1;
        String sstmt1 = "insert into " + tabelle + " set " + feld + " = '" + dieseMaschine + "'";
        String sstmt2 = "select id from " + tabelle + " where " + feld + " = '" + dieseMaschine + "'";
        imbusy();
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            stmt.execute(sstmt1);
            ResultSet rs = stmt.executeQuery(sstmt2);
            if (rs.next()) {
                retid = rs.getInt("id");
            }
            rs.close();
        } catch (SQLException ev) {
            logger.error("setzen der maschinen ID", ev);
        }
        imdone();
        return retid;
    }

    public static int holeIdSimple(String tabelle, String befehl) {
        int retid = -1;
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            stmt.execute(befehl);
            ResultSet rs = stmt.executeQuery("select max(id) from " + tabelle);
            if (rs.next()) {
                retid = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("holesimpleID", e);
        }
        return retid;
    }

    public static Vector<String> holeSatz(String string, String string2, String string3) {
        return holeSatz(string, string2, string3,Collections.<String>emptyList());
    }

    public static Vector<String> holeSatz(String tabelle, String felder, String kriterium, List<?> ausschliessen) {
        String sstmt = "select " + felder + " from " + tabelle + " where " + kriterium + " LIMIT 1";
        Vector<String> retvec = new Vector<>();

        imbusy();
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt)) {
            int nichtlesen = ausschliessen.size();
            if (rs.next()) {
                ResultSetMetaData rsMetaData = rs.getMetaData();
                int numberOfColumns = rsMetaData.getColumnCount() + 1;
                for (int i = 1; i < numberOfColumns; i++) {
                    if (nichtlesen > 0) {
                        if (!ausschliessen.contains(rsMetaData.getColumnName(i))) {
                            retvec.add(rs.getString(i) == null ? "" : rs.getString(i));
                        }
                    } else {
                        retvec.add(rs.getString(i) == null ? "" : rs.getString(i));
                    }
                }
            }
        } catch (SQLException ev) {
            logger.error("something bad happens here", ev);
        }
        imdone();

        return retvec;
    }

    public static Vector<String> holeFeldNamen(String tabelle, boolean ausnahmen, List<?> lausnahmen) {
        Vector<String> vec = new Vector<>();
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery("describe " + tabelle)) {
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
            logger.error("something bad happens here", e);
        }
        return vec;
    }

    static Vector<String> holeFeldForUpdate(String tabelle, String feld, String kriterium) {
        Vector<String> retvec = new Vector<>();
        String sstmt = "select " + feld + " from " + tabelle + kriterium;

        imbusy();
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt)) {
            if (rs.next()) {
                retvec.add(rs.getString(1) == null ? "" : rs.getString(1));
                retvec.add(rs.getString(2) == null ? "" : rs.getString(2));
            }
        } catch (SQLException ev) {
            logger.error("something bad happens here", ev);
        }
        imdone();
        return retvec;
    }

    public static Vector<Vector<String>> holeSaetze(String tabelle, String felder, String kriterium,
            List<String> ausschliessen) {
        Vector<String> retvec = new Vector<>();
        Vector<Vector<String>> retkomplett = new Vector<>();
        ResultSetMetaData rsMetaData = null;
        String sstmt = "select " + felder + " from " + tabelle + " where " + kriterium;
        imbusy();
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt)) {
            int nichtlesen = ausschliessen.size();
            while (rs.next()) {
                try {
                    retvec.clear();
                    rsMetaData = rs.getMetaData();
                    int numberOfColumns = rsMetaData.getColumnCount() + 1;
                    for (int i = 1; i < numberOfColumns; i++) {
                        if (nichtlesen > 0) {
                            if (!ausschliessen.contains(rsMetaData.getColumnName(i))) {
                                retvec.add(rs.getString(i) == null ? "" : rs.getString(i));
                            }
                        } else {
                            retvec.add(rs.getString(i) == null ? "" : rs.getString(i));
                        }
                    }
                    retkomplett.add((Vector<String>) ((Vector<?>) retvec.clone()));
                } catch (Exception ex) {
                    logger.error("something bad happens here", ex);
                }
            }
            retvec.clear();
            retvec = null;
        } catch (SQLException ev) {
            logger.error("something bad happens here", ev);
        }
        imdone();
        return retkomplett;
    }

    public static String macheWhereKlausel(String praefix, String test, String[] suchein) {
        // paraefix = wenn der eine fixe Bedinung vorangestellt wird z.B.
        // "(name='steinhilber') AND " bzw. "" fals keine notwendig
        // test = der suchbegriff bzw. die durch Leerzeichen getrennte suchbegriffe
        // suchein[] sind die spalten bzw. die spalte die durchsucht werden soll
        // werden mehrere suchbegriffe eingegeben, bezogen auf die Begriffe -> AND-Suche
        // innerhalb der spalten, bezogen auf die Spalten -> OR-Suche
        String ret = praefix;
        String cmd = test;
        // zunaechst versuchen dass immer nur ein Leerzeichen zwischen den Begriffen
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
            return ret + ") ";
        }

        ret = ret + "( ";
        for (int i = 0; i < split.length; i++) {
            if (!"".equals(split[i])) {
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
        return ret + ") ";
    }

    private static String toRTF(String toConvert) {
        return toConvert.replace("Ö", "\\\\\\\\\\'d6")
                        .replace("ö", "\\\\\\\\\\'f6")
                        .replace("Ä", "\\\\\\\\\\'c4")
                        .replace("ä", "\\\\\\\\\\'e4")
                        .replace("Ü", "\\\\\\\\\\'dc")
                        .replace("ü", "\\\\\\\\\\'fc")
                        .replace("ß", "\\\\\\\\\\'df");
    }

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
            return ret + ") ";
        }
        ret = ret + "( ";
        for (int i = 0; i < split.length; i++) {
            if (!"".equals(split[i])) {
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
        return ret + ") ";
    }

    public static int erzeugeNummer(String nummer) {
        int reznr = -1;
        /* Zunächst eine neue Rezeptnummer holen */
        Vector<String> numvec = null;
        try {
            conn.setAutoCommit(false);

            numvec = holeFeldForUpdate("nummern", nummer + ",id", " FOR UPDATE");
        } catch (SQLException e) {
            logger.error("something bad happens here", e);
        }
        if (!numvec.isEmpty()) {
            try {
                reznr = Integer.parseInt(numvec.get(0));
                String cmd = "update nummern set " + nummer + "='" + (reznr + 1) + "' where id='" + numvec.get(1) + "'";
                sqlAusfuehren(cmd);
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

    public static int zaehleSaetze(String tabelle, String bedingung) {
        int retid = -1;

        String sstmt1 = "select count(*) from " + tabelle + " where " + bedingung;
        imbusy();
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt1)) {
            if (rs.next()) {
                retid = rs.getInt(1);
            }
        } catch (SQLException ev) {
            logger.error("something bad happens here", ev);
        }
        imdone();
        return retid;
    }

    public static void aktualisiereSatz(String tabelle, String sets, String kriterium) {
        String sstmt = "update " + tabelle + " set " + sets + " where " + kriterium + " LIMIT 1";
        imbusy();
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            stmt.execute(sstmt);
        } catch (SQLException ev) {
            logger.error("something bad happens here", ev);
        }
        imdone();
    }

    public static void aktualisiereSaetze(String tabelle, String sets, String kriterium) {
        String sstmt = "update " + tabelle + " set " + sets + " where " + kriterium;
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            imbusy();
            stmt.execute(sstmt);
            imdone();
        } catch (SQLException ev) {
            logger.error("something bad happens here", ev);
        }
    }

    public static String holePatFeld(String feld, String kriterium) {
        String ret = "";

        String sstmt = "select " + feld + " from pat5 where " + kriterium + " LIMIT 1";
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt)) {
            imbusy();

            if (rs.next()) {
                ret = rs.getString(feld) == null ? "" : rs.getString(feld);
            }
            imdone();
        } catch (SQLException ev) {
            logger.error("something bad happens here", ev);
        }
        return ret;
    }

    /**
     * @param xstmt must be a valid sql statement
     * @return first value of sqlresult
     */
    public static String holeEinzelFeld(final String xstmt) {
        String ret = "";
        imbusy();
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(xstmt)) {
            if (rs.next() && rs.getString(1) != null) {
                ret = rs.getString(1)
                        .trim();
            }
        } catch (SQLException ev) {
            logger.error("something bad happens here", ev);
        }
        imdone();
        return ret;
    }

    public static Vector<Vector<String>> holeFelder(final String xstmt) {
        imbusy();
        Vector<Vector<String>> retkomplett = new Vector<>();
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(xstmt)) {
            int numberOfColumns = rs.getMetaData()
                                    .getColumnCount()
                    + 1;
            while (rs.next()) {
                Vector<String> vec = new Vector<>();
                for (int i = 1; i < numberOfColumns; i++) {
                    vec.add(Optional.ofNullable(rs.getString(i))
                                    .orElse(""));
                }
                retkomplett.add(vec);
            }
        } catch (SQLException ev) {
            logger.error("something bad happens here", ev);
        }
        imdone();
        return retkomplett;
    }

    public static String holeRezFeld(String feld, String kriterium) {
        String ret = "";

        imbusy();
        String sstmt = "select " + feld + " from verordn where " + kriterium + " LIMIT 1";
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt)) {
            if (rs.next()) {
                ret = Optional.ofNullable(rs.getString(feld))
                              .orElse("");
            }
        } catch (SQLException ev) {
            logger.error("something bad happens here", ev);
        }
        imdone();
        return ret;
    }

    public static Vector<String> holeFeld(String sstmt) {
        Vector<String> vecret = new Vector<>();

        imbusy();
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt)) {
            while (rs.next()) {
                String ret = Optional.ofNullable(rs.getString(1))
                                     .orElse("");
                vecret.add(ret);
            }
        } catch (SQLException ev) {
            logger.error("something bad happens here", ev);
        }
        imdone();
        return vecret;
    }

    public static boolean sqlAusfuehren(String sstmt) {
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            stmt.execute(sstmt);
            return true;
        } catch (SQLException ev) {
            logger.error("something bad happens here", ev);
            JOptionPane.showMessageDialog(null, "Fehler bei der Ausführung des Statements\nMethode:sqlAusfuehren("
                    + sstmt + ")\n\nBitte informieren Sie sofort den Administrator!!!");
            return false;
        }
    }

    public static InputStream holeStream(String tabelle, String feld, String kriterium) {
        InputStream is = null;
        String sstmt = "select " + feld + " from " + tabelle + " where " + kriterium + " LIMIT 1";

        imbusy();
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt)) {
            if (rs.next()) {
                is = rs.getBinaryStream(1);
            }
        } catch (SQLException ev) {
            logger.error("something bad happens here", ev);
        }
        imdone();
        return is;
    }

    public static boolean transferRowToAnotherDB(String sourcedb, String targetdb, String dbfield, String argument,
            boolean ausnahmen, List<?> lausnahmen) {
        boolean ret = false;
        StringBuilder transferBuf = new StringBuilder();
        StringBuilder insertBuf = new StringBuilder();
        Vector<String> feldNamen = holeFeldNamen(sourcedb, ausnahmen, lausnahmen);
        transferBuf.append("select ");
        int rezeptFelder = 0;
        for (int i = 0; i < feldNamen.size(); i++) {
            if (i > 0) {
                transferBuf.append(",")
                           .append(feldNamen.get(i));
            } else {
                transferBuf.append(feldNamen.get(i));
            }
        }
        transferBuf.append(" from ")
                   .append(sourcedb)
                   .append(" where ")
                   .append(dbfield)
                   .append("='")
                   .append(argument)
                   .append("' LIMIT 1");

        Vector<Vector<String>> vec = holeFelder(transferBuf.toString());

        if (vec.isEmpty()) {
            return false;
        }
        try {
            rezeptFelder = vec.get(0)
                              .size();
            insertBuf.append("insert into ")
                     .append(targetdb)
                     .append(" set ");
            for (int i = 0; i < rezeptFelder; i++) {
                if (!"".equals(vec.get(0)
                                  .get(i))) {
                    if (i > 0) {
                        insertBuf.append(",")
                                 .append(feldNamen.get(i))
                                 .append("='")
                                 .append(StringTools.Escaped(vec.get(0)
                                                                .get(i)))
                                 .append("'");
                    } else {
                        insertBuf.append(feldNamen.get(i))
                                 .append("='")
                                 .append(StringTools.Escaped(vec.get(0)
                                                                .get(i)))
                                 .append("'");
                    }
                }
            }
            sqlAusfuehren(insertBuf.toString());
            return true;
        } catch (Exception ex) {
            logger.error("something bad happens here", ex);
        }
        return ret;
    }

    public static InputStream liesIniAusTabelle(String inifilename) {
        InputStream retStream = null;
        String sqlInidatei = "select inhalt from inidatei where dateiname='" + inifilename + "' LIMIT 1";

        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sqlInidatei)) {
            if (rs.next()) {
                retStream = rs.getBinaryStream(1);
            }
        } catch (SQLException e) {
            logger.error("something bad happens here", e);
        }
        return retStream;
    }

    public static boolean schreibeIniInTabelle(String inifilename, byte[] buf) {
        String sqlString;
        String einzelfeld = holeEinzelFeld(
                "select dateiname from inidatei where dateiname='" + inifilename + "' LIMIT 1");
        if (einzelfeld.isEmpty()) {
            sqlString = "insert into inidatei set dateiname = ? , inhalt = ?";
        } else {
            sqlString = "update inidatei set dateiname = ? , inhalt = ? where dateiname = '" + inifilename + "'";
        }

        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                PreparedStatement ps = conn.prepareStatement(sqlString)) {
            ps.setString(1, inifilename);
            ps.setBytes(2, buf);
            ps.execute();
        } catch (Exception ex) {
            logger.error("something bad happens here", ex);
        }

        return true;
    }


}
