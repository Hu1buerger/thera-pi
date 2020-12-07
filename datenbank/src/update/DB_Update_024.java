package update;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;

import sql.DatenquellenFactory;

public class DB_Update_024 extends Update {



    @Override
    protected boolean postCondition(DatenquellenFactory dq) {

        return !tableStructureIsOk(dq);
    }

    @Override
    protected void execute(DatenquellenFactory dq) {

        try (Connection conn = dq.createConnection(); Statement statement = conn.createStatement();) {
            statement.addBatch("INSERT IGNORE INTO `hmrcheck`\n"
                    + " (`indischluessel`, `gesamt`, `maxrezept`, `vorrangig`, `maxvorrangig`, `ergaenzend`, `maxergaenzend`, `id`)\n"
                    + " VALUES\n"
                    + " ('NF', '40', '6', '8001@8002@8003@8004@8005@8006', '6@6@6@6@6@6', NULL, NULL, NULL),\n"
                    + " ('QF', '40', '6', '8001@8002@8003@8004@8005@8006', '6@6@6@6@6@6', NULL, NULL, NULL);");
            int[] result = statement.executeBatch();

            System.out.println( "DB_Update_024 returned: " + Arrays.toString(result ));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean preCondition(DatenquellenFactory dq) {
       return !tableStructureIsOk(dq);
    }

    private boolean tableStructureIsOk(DatenquellenFactory dq) {

        boolean nfIsThere = false;
        boolean qfIsThere = false;
        try (Connection conn = dq.createConnection();
                Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery("select * from hmrcheck where\n"
                        + "    indischluessel = 'NF' or indischluessel = 'QF';")) {
            String currKey = null;
            while (rs.next()) {
                currKey = rs.getString("INDISCHLUESSEL");
                nfIsThere = nfIsThere || (currKey.equals("NF"));
                qfIsThere = qfIsThere || (currKey.equals("QF"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nfIsThere && qfIsThere;

    }
}
