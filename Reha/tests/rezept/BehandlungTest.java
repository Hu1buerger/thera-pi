package rezept;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.*;

public class BehandlungTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void nullDateThrowsIAE() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Date musst not be null");

        new Behandlung(null, null, null, null);

    }

    @Test
    public void contructorfromdbstring() throws Exception {

        Behandlung t = new Behandlung("13.03.2019@kollege@weißnich@54105@2019-03-13");
        Behandlung exp = new Behandlung(LocalDate.of(2019, 3, 13), "kollege", "weißnich", "54105");
        assertEquals(exp, t);
    }

    @Test
    public void behandlungIsOrderedByDate() throws Exception {

        Behandlung behandl_13_3_19 = new Behandlung(LocalDate.of(2019, 3, 13), "kollege", "weißnich", "54105");
        Behandlung behandl_12_3_19 = new Behandlung(LocalDate.of(2019, 3, 12), "kollege", "weißnich", "54105");
        assertThat(behandl_13_3_19, greaterThan(behandl_12_3_19));
        assertThat(behandl_13_3_19, comparesEqualTo(behandl_13_3_19));
        assertThat(behandl_12_3_19, lessThan(behandl_13_3_19));
    }

}
