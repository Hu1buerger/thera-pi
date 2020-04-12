package sql;

import static org.junit.Assert.assertFalse;

import java.sql.SQLException;

import org.junit.Test;

import mandant.IK;

public class testDatenbankSql {

    @Test
    public void testDatenQuelleConstructor() throws SQLException {

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
