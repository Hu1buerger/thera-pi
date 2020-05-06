/**
 * 
 */
package offenePosten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import CommonTools.SqlInfo;
import sql.DatenquellenFactory;

/**
 *
 */
public class OffenePostenTest {

    static OffenePosten op = new OffenePosten("JUnit");
    static OffenepostenTab opTab = new OffenepostenTab("JUnit");
    static OffenepostenPanel opPan = new OffenepostenPanel("JUnit");
    static Connection conn;
    static SqlInfo sqlInfo;

    @BeforeClass
    public static void initForAllTests() {
        try {
            conn = new DatenquellenFactory("123456789").createConnection();
        } catch (SQLException e) {
            fail("Need running DB connection for this test");
        }
        sqlInfo = new SqlInfo();
        op.setProghome("./");
        op.setAktIK("123456789");
        sqlInfo.setConnection(conn);
        op.sqlInfo = sqlInfo;
        opPan.eltern = opTab;
        opPan.offenePosten = op;
    }

    @AfterClass
    public static void teardownForAllTests() {
        // Wipe table that was filled with test-entries:
        String stmt = "delete from rliste";
        try {
            conn.createStatement().execute(stmt);
            conn.close();
        } catch (SQLException e) {
            System.out.println("SQL-Error: " + e.getCause() + " with " + e.getLocalizedMessage());
            fail("Error in trying to tear-down the OP-Tests");
        }
        
    }
    
    @Test
    public void testOPPanelermittleGesamtOffen() {

        try {
            opPan.ermittleGesamtOffen();
        } catch (NullPointerException e) {
            e.printStackTrace();
            fail("Böser Code: ");
        }

        try {
            Statement batchStmt = conn.createStatement();
            conn.setAutoCommit(false);
            batchStmt.addBatch("delete from rliste");
            batchStmt.addBatch("insert into rliste (r_nummer, r_offen) values(1, 0.99)");
            batchStmt.addBatch("insert into rliste (r_nummer, r_offen) values(2, 0.02)");
            int[] rc = batchStmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println("SQL-Error: " + e.getCause() + " with " + e.getLocalizedMessage());
            System.out.println(e.toString());
            e.printStackTrace();
            fail("Need running DB connection for this test");
        }
        opPan.ermittleGesamtOffen();
        assertEquals(BigDecimal.valueOf(Double.parseDouble("1.01")), opPan.gesamtOffen);
    }
}
