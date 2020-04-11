package sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DatenquellenFactory {
    private static final ConcurrentMap<String, Datenquelle> grube = new ConcurrentHashMap<>();
    private String ik;

    /**
     * @param ik = die 9 Ziffern des IK des Mandanten
     */
    public DatenquellenFactory(String ik) {
        this.ik = ik;
    }

    public Connection createConnection() throws SQLException {
        grube.computeIfAbsent(ik, k -> new Datenquelle(ik));
        return grube.get(ik)
                    .connection();
    }

}
