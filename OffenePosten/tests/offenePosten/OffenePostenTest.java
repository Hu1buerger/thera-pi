/**
 * 
 */
package offenePosten;

import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

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
        op.sqlInfo = sqlInfo;
        opPan.eltern = opTab;
        opPan.offenePosten = op;
        try {
            opPan.ermittleGesamtOffen();
        } catch (NullPointerException e) {
            e.printStackTrace();
            fail("Böser Code: ");
        }
        
    }
}
