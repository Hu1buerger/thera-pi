package rezept;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.util.LinkedList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import specs.ContractException;

public class BehandlungTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void nullDateThrowsIAE() throws Exception {
        thrown.expect(ContractException.class);
        thrown.expectMessage("Date musst not be null");

        new Behandlung(null, null, null, null);
    }

    @Test
    public void contructorfromdbstring() throws Exception {
        Behandlung t = new Behandlung("13.03.2019@kollege@weißnich@54105@2019-03-13");
        Behandlung exp = new Behandlung(LocalDate.of(2019, 3, 13), "kollege", "weißnich", "54105",LocalDate.of(2019, 3, 13));
        assertEquals(exp.toString(), t.toString());
    }

    @Test
    public void mehrereHeilmittelIneinerBehandlung() throws Exception {
        Behandlung t = new Behandlung("29.09.2016@@@54102,54002@2016-09-29");
        Behandlung bMitLeerzeichen = new Behandlung("29.09.2016@@@54102, 54002@2016-09-29");
        LinkedList<String> hmList = new LinkedList<>();
        hmList.add("54102");
        hmList.add("54002");
        assertEquals(hmList, t.erbrachteHeilmittel());
        assertEquals(hmList, bMitLeerzeichen.erbrachteHeilmittel());
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
