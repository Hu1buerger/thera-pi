/**
 * 
 */
package offenePosten;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.mysql.jdbc.PreparedStatement;

import sql.DatenquellenFactory;
import CommonTools.SqlInfo;

/**
 *
 */
public class OffenePostenTest {
    
    OffenePosten op = new OffenePosten("JUnit");
    OffenepostenTab opTab = new OffenepostenTab("JUnit");
    OffenepostenPanel  opPan = new OffenepostenPanel("JUnit");
    String aktIK = "123456789";
    public Connection conn;
    public SqlInfo sqlInfo;

    @Test
    public void testOPPanelermittleGesamtOffen() {
        try {
            conn = new DatenquellenFactory(aktIK).createConnection();
        } catch (SQLException e) {
            fail("Need running DB connection for this test");
        }
        sqlInfo = new SqlInfo();
        op.setProghome("./");
        sqlInfo.setConnection(conn);
        op.sqlInfo = sqlInfo;
        opPan.eltern = opTab;
        opPan.offenePosten = op;
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
            batchStmt.addBatch("insert into rliste (r_nummer, r_offen) values(2, 0.01)");
            int[] rc = batchStmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println("SQL-Error: " + e.getCause() + "with " + e.getLocalizedMessage());
            System.out.println(e.toString());
            e.printStackTrace();
            fail("Need running DB connection for this test");
        }
        Vector<Vector<String>> Ergebnis = SqlInfo.holeFelder("select * from rliste");
        System.out.println("Got Outer0: " + Ergebnis.get(0));
        System.out.println("Got Inner0: " + Ergebnis.get(0).get(0));
        opPan.ermittleGesamtOffen();
        assertEquals("1.00", opPan.gesamtOffen);
    }
}
