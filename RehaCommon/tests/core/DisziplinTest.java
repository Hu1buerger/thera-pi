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

    @Test
    public void testOfKennung() throws Exception {
        assertEquals(Disziplin.MA, Disziplin.ofKennung("1"));
        assertEquals(Disziplin.KG, Disziplin.ofKennung("2"));
        assertEquals(Disziplin.LO, Disziplin.ofKennung("3"));
        assertEquals(Disziplin.ST, Disziplin.ofKennung("4"));
        assertEquals(Disziplin.ER, Disziplin.ofKennung("5"));
        assertEquals(Disziplin.KK, Disziplin.ofKennung("6"));
        assertEquals(Disziplin.PO, Disziplin.ofKennung("7"));
        assertEquals(Disziplin.ET, Disziplin.ofKennung("A"));
        assertEquals(Disziplin.RH, Disziplin.ofKennung("R"));
        assertEquals(Disziplin.ET, Disziplin.ofKennung("a"));
        assertEquals(Disziplin.RH, Disziplin.ofKennung("r"));

   // ist das rehasport?     assertEquals(Disziplin.PT, Disziplin.ofKennung("8"));
}}
