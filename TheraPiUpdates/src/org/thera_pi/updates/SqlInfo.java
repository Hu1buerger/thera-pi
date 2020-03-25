package org.thera_pi.updates;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlInfo {

    private static final Logger LOG = LoggerFactory.getLogger(SqlInfo.class);

    /**
     * private CTor to avoid creating an instance of a class with only static
     * methods.
     */
    private SqlInfo() {
        // nothing to do here
    }

    public static void sqlAusfuehren(Connection conn, String sstmt) throws SQLException {
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            stmt.execute(sstmt);
        }
    }

    public static List<String> holeSatz(Connection conn, String tabelle, String felder, String kriterium) {
        List<String> retvec = new ArrayList<>();
        String sstmt = "select " + felder + " from " + tabelle + " where " + kriterium;
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt)) {
            if (rs.next()) {
                ResultSetMetaData rsMetaData = rs.getMetaData();
                int numberOfColumns = rsMetaData.getColumnCount() + 1;
                for (int i = 1; i < numberOfColumns; i++) {
                    retvec.add((rs.getString(i) == null ? "" : rs.getString(i)));
                }
            }
        } catch (SQLException ev) {
            LOG.error("SQLException: {}", ev.getMessage());
            LOG.error("SQLState: {}", ev.getSQLState());
            LOG.error("VendorError: {}", ev.getErrorCode(), ev);
        }
        return retvec;
    }

    static List<String> holeFeld(Connection conn, String sstmt) {
        String ret;
        List<String> result = new ArrayList<>();
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(sstmt)) {
            while (rs.next()) {
                ret = (rs.getString(1) == null ? "" : rs.getString(1));
                result.add(String.valueOf(ret));
            }
        } catch (SQLException ev) {
            LOG.error("SQLException: {}", ev.getMessage());
            LOG.error("SQLState: {}", ev.getSQLState());
            LOG.error("VendorError: {}", ev.getErrorCode(), ev);
        }
        return result;
    }
}
