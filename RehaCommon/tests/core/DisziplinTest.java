package core;

import static org.junit.Assert.*;

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



    @Test
    public void ofShortReturnsINVforIllegalArguments() throws Exception {
        assertSame(Disziplin.INV, Disziplin.ofShort(""));
        assertSame(Disziplin.INV, Disziplin.ofShort(null));
        assertSame(Disziplin.INV, Disziplin.ofShort("123"));

    }

    @Test
    public void ofShortReturnsDisziforValues() throws Exception {
        assertSame(Disziplin.KG, Disziplin.ofShort("KG"));
        assertSame(Disziplin.MA, Disziplin.ofShort("MA"));
        assertSame(Disziplin.ER, Disziplin.ofShort("ER"));
        assertSame(Disziplin.LO, Disziplin.ofShort("LO"));
        assertSame(Disziplin.PO, Disziplin.ofShort("PO"));
        assertSame(Disziplin.RS, Disziplin.ofShort("RS"));
        assertSame(Disziplin.FT, Disziplin.ofShort("FT"));
        assertSame(Disziplin.INV, Disziplin.ofShort("INV"));
        assertSame(Disziplin.COMMON, Disziplin.ofShort("COMMON"));


    }


}
