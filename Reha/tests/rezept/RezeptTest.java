package rezept;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import CommonTools.SqlInfo;
import mandant.IK;
import sql.DatenquellenFactory;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemPreislisten;

public class RezeptTest {
    IK ik = new IK("123456789");
    @Test
    public void reztools() throws Exception {
        List<Rezept> rez = new RezeptDto(ik).allfromVerordn();
        SqlInfo sqlinf = new SqlInfo();
        Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
        sqlinf.setConnection(conn);
        SystemPreislisten.ladepreise("Ergo", ik.digitString());
        rez = rez.parallelStream()
                 .filter(r -> r.REZ_NR != null)
                 .collect(Collectors.toList());
        for (Rezept rezept : rez) {
            if (rezept.REZ_NR != null)

                assertEquals(rezept.REZ_NR,rezept.positionenundanzahl().toString(), RezTools.Y_holePosUndAnzahlAusRezept(rezept.REZ_NR).toString());
        }
    }

    @Test
    public void reztoolER1() throws Exception {
        SqlInfo sqlinf = new SqlInfo();
        Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
        sqlinf.setConnection(conn);
        SystemPreislisten.ladepreise("Ergo", ik.digitString());
        Optional<Rezept> rez = new RezeptDto(ik).byRezeptNr("ER1");

        Rezept rezept = rez.get();
        if (rezept.REZ_NR != null)
            assertEquals(rezept.REZ_NR,rezept.positionenundanzahl().toString(), RezTools.Y_holePosUndAnzahlAusRezept(rezept.REZ_NR).toString());
    }
    @Test
    public void reztoolER1424() throws Exception {
        SqlInfo sqlinf = new SqlInfo();
        Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
        sqlinf.setConnection(conn);
        SystemPreislisten.ladepreise("Ergo", ik.digitString());
        Optional<Rezept> rez = new RezeptDto(ik).byRezeptNr("ER1424");

        Rezept rezept = rez.get();
        if (rezept.REZ_NR != null)
            assertEquals(rezept.REZ_NR,rezept.positionenundanzahl().toString(), RezTools.Y_holePosUndAnzahlAusRezept(rezept.REZ_NR).toString());
    }
}
