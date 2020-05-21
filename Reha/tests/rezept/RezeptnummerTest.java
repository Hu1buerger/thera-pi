package rezept;
import static org.junit.Assert.*;
// needs junit 4.13... import static org.junit.Assert.assertThrows;

import org.junit.Test;

import core.Disziplin;

public class RezeptnummerTest {

    @Test
    public void RezeptnummerByStringConstructorTest() {
        Rezeptnummer rezNr = new Rezeptnummer("ER101");
        assertEquals("Expecting ER as String after calling String constr.", "ER", rezNr.disziplin().toString());
        assertEquals("Expecting ER as Disziplin after calling String constr.",
                Disziplin.ER, rezNr.disziplin());
        assertEquals("Expecting 101 as int after calling String constr.", 101, rezNr.rezeptZiffern());
    }
    
    @Test
    public void RezeptnummerByMalformedStringConstructorTest() {
        Rezeptnummer rezNr = new Rezeptnummer("BER101");
       // assertThrows();
        assertEquals("Expecting INV as Disziplin after calling String-constr. with invalid rezNr",
                Disziplin.INV, rezNr.disziplin());
    }
    
    @Test
    public void RezeptnummerByEmptyConstructorTest() {
        Rezeptnummer rezNr = new Rezeptnummer();
        assertEquals("Expecting INV as Disziplin after calling empty constr.", Disziplin.INV, rezNr.disziplin());
        assertEquals("Expecting 0 as int in rezNr digits", 0, rezNr.rezeptZiffern());
    }
    
    @Test
    public void RezeptnummerByDisziAndIntConstructorTest() {
        Rezeptnummer rezNr = new Rezeptnummer(Disziplin.ER, 101);
        assertEquals("Expecting ER101 as String after calling Disziplin + int constr.",
                "ER101", rezNr.rezeptNummer());
    }
}
