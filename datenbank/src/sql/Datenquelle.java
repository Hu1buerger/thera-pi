package sql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.ini4j.Ini;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import crypt.Verschluesseln;
import environment.Path;

public class Datenquelle {

    private String s_pfadZurRehaIni = "";
    private static final Verschluesseln DECODER = Verschluesseln.getInstance();
    private static final String DATEN_BANK = "DatenBank";
    MysqlDataSource dataSource;

    // @Visible for testing
    void setRehaIni(String pfadZurRehaIni) {
        this.s_pfadZurRehaIni = pfadZurRehaIni;
    }
    
    // @Visible for testing
    String getRehaIni() {
        return this.s_pfadZurRehaIni;
    }
    
    Datenquelle(String digitString) {
        this.s_pfadZurRehaIni = environment.Path.Instance.getProghome() + "ini/" + digitString + "/rehajava.ini";
        initialize();
    }

    private void initialize() {
        File f = new File(this.s_pfadZurRehaIni);
        Ini ini;
        try {
            ini = new Ini(f);
            ini.load();
        } catch (IOException e) {
            throw new IllegalArgumentException(f.getPath());
        }
        dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setUrl(ini.get(DATEN_BANK, "DBKontakt1"));
        dataSource.setUser(ini.get(DATEN_BANK, "DBBenutzer1"));
        String pw = DECODER.decrypt(ini.get(DATEN_BANK, "DBPasswort1"));
        dataSource.setPassword(pw);
    }

    public Connection connection() throws SQLException {
        return dataSource.getConnection();
    }

}
