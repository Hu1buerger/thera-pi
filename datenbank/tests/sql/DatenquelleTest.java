package sql;

import mandant.IK;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertFalse;

public class DatenquelleTest {

    //TODO: Fail at this path from IK is wrong because of PATH is cyka
    //@Test
    public void constructor() throws SQLException {

        IK ik = new IK("123456789");
        Datenquelle dq = new Datenquelle(ik.digitString());
        assertFalse(dq.connection()
                .isClosed());
        dq.connection()
                .close();
        assertFalse(dq.connection()
                .isClosed());

    }

}