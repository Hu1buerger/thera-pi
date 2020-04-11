package core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DisziplinTest {

    @Test
    public void testOfMedium() throws Exception {
        assertEquals(Disziplin.KG, Disziplin.ofMedium("physio"));
        assertEquals(Disziplin.MA, Disziplin.ofMedium("Massage"));
        assertEquals(Disziplin.ER, Disziplin.ofMedium("ERGO"));
        assertEquals(Disziplin.LO, Disziplin.ofMedium("Logo"));
        assertEquals(Disziplin.PO, Disziplin.ofMedium("Podo"));
        assertEquals(Disziplin.RS, Disziplin.ofMedium("Rsport"));
        assertEquals(Disziplin.FT, Disziplin.ofMedium("Ftrain"));

    }

}
