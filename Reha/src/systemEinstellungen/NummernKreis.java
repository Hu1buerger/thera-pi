package systemEinstellungen;

import static core.Disziplin.ER;
import static core.Disziplin.KG;
import static core.Disziplin.LO;
import static core.Disziplin.MA;
import static core.Disziplin.PO;
import static core.Disziplin.RH;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;

import core.Disziplin;
import mandant.IK;
import specs.Contracts;
import sql.DatenquellenFactory;

public class NummernKreis {
    EnumSet<Disziplin> supportedDiszis = EnumSet.of(KG, MA, ER, LO, PO, RH);

    private DatenquellenFactory dq;

    private IK ik;

    public NummernKreis(IK ik) {
        this.ik = ik;
        dq = new DatenquellenFactory(ik.digitString());
    };

    int nextNumber(Disziplin d) throws SQLException {
        Contracts.require(supportedDiszis.contains(d),
                "Die Disziplin " + d + " wird nicht über die Nummern Tabelle gesteuert");

        String query = "Select id," + d + " FROM nummern where mandant LIKE " +ik.digitString();
        try (Connection con = dq.createConnection();
                Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(query)) {
            rs.next();
            int nextnumber = rs.getInt(d.toString());

            rs.updateInt(d.toString(), nextnumber + 1);
            rs.updateRow();
            return nextnumber;

        } finally {
        }
    }
}
