package rezept;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import CommonTools.SqlInfo;
import mandant.IK;
import sql.DatenquellenFactory;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemPreislisten;

public class RezeptTest {
    static IK ik = new IK("123456789");
    static Connection conn;

    static Rezept rez = new Rezept();

    @BeforeClass
    public static void initForAllTests() {
        try {
            conn = new DatenquellenFactory(ik.digitString()).createConnection();
        } catch (SQLException e) {
            fail("Need running DB connection for these tests");
        }

    }

    @Test
    public void rezFieldsToDbFieldsTest() {
        String stmt = "describe verordn";

        try {
            ResultSet rs = conn.createStatement()
                               .executeQuery(stmt);
            while (rs.next()) {
                try {
                    Field field = Rezept.class.getDeclaredField(rs.getString(1));
                    // System.out.println("Found field " + field + " from DB in Rezepte.");
                } catch (NoSuchFieldException e) {
                    fail("DB field " + rs.getString(1) + " is not in Rezept-fields");
                }
            }
        } catch (SQLException e) {
            fail("Need running DB connection for this test");
            System.out.println("Exception: " + e.getLocalizedMessage());
            e.printStackTrace();
        }

    }

    @Test
    public void reztoolsTest() throws Exception {
        List<Rezept> rez = new RezeptDto(ik).allfromVerordn();
        SqlInfo sqlinf = new SqlInfo();
        // Connection conn = new
        // DatenquellenFactory(ik.digitString()).createConnection();
        sqlinf.setConnection(conn);
        SystemPreislisten.ladepreise("Ergo", ik.digitString());
        rez = rez.parallelStream()
                 .filter(r -> r.REZ_NR != null)
                 .collect(Collectors.toList());
        for (Rezept rezept : rez) {
            if (rezept.REZ_NR != null)

                assertEquals(rezept.REZ_NR, rezept.positionenundanzahl()
                                                  .toString(),
                        RezTools.Y_holePosUndAnzahlAusRezept(rezept.REZ_NR)
                                .toString());
        }
    }

    @Test
    public void reztoolER1Test() throws Exception {
        SqlInfo sqlinf = new SqlInfo();
        // Connection conn = new
        // DatenquellenFactory(ik.digitString()).createConnection();
        sqlinf.setConnection(conn);
        SystemPreislisten.ladepreise("Ergo", ik.digitString());
        Optional<Rezept> rez = new RezeptDto(ik).byRezeptNr("ER1");

        Rezept rezept = rez.get();
        if (rezept.REZ_NR != null)
            assertEquals(rezept.REZ_NR, rezept.positionenundanzahl()
                                              .toString(),
                    RezTools.Y_holePosUndAnzahlAusRezept(rezept.REZ_NR)
                            .toString());
    }

    @Test
    public void reztoolER1424Test() throws Exception {
        SqlInfo sqlinf = new SqlInfo();
        // Connection conn = new
        // DatenquellenFactory(ik.digitString()).createConnection();
        sqlinf.setConnection(conn);
        SystemPreislisten.ladepreise("Ergo", ik.digitString());
        Optional<Rezept> rez = new RezeptDto(ik).byRezeptNr("ER1424");

        Rezept rezept = rez.get();
        if (rezept.REZ_NR != null)
            assertEquals(rezept.REZ_NR, rezept.positionenundanzahl()
                                              .toString(),
                    RezTools.Y_holePosUndAnzahlAusRezept(rezept.REZ_NR)
                            .toString());
    }
}
