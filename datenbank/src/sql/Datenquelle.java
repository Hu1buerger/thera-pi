package sql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.ini4j.Ini;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import crypt.Verschluesseln;

public class Datenquelle {

    private static final Verschluesseln DECODER = Verschluesseln.getInstance();
    private static final String DATEN_BANK = "DatenBank";
    MysqlDataSource dataSource;

    /**
     * Datenquelle liest fuer den angegebenen Mandant die Datenverbindung, die in
     * rehajava.ini unter DB1 angegeben ist.
     *
     * @param digitString = die 9 Ziffern des IK des Mandanten
     */
    Datenquelle(String digitString) {
        initialize(digitString);

    }

    private void initialize(String digitString) {
        File datei = new File(environment.Path.Instance.getProghome() + "ini" + File.separator
                                                                + digitString + File.separator
                                                                + "rehajava.ini");
        Ini ini;
        try {
            ini = new Ini(datei);
            ini.load();
        } catch (IOException e) {
            throw new IllegalArgumentException(datei.getPath());
        }
        dataSource = new MysqlConnectionPoolDataSource();
        // If enabled, serverside sql_mode='' is ignored clientside.
        dataSource.setJdbcCompliantTruncation(false);
        dataSource.setUrl(ini.get(DATEN_BANK, "DBKontakt1"));
        dataSource.setUser(ini.get(DATEN_BANK, "DBBenutzer1"));
        String pw = DECODER.decrypt(ini.get(DATEN_BANK, "DBPasswort1"));
        dataSource.setPassword(pw);
    }

    public Connection connection() throws SQLException {
        return dataSource.getConnection();
    }

}
