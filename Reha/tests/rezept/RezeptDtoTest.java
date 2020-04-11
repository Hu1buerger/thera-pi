package rezept;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import mandant.IK;

public class RezeptDtoTest {

    @Test
    public void testalle() throws Exception {

        RezeptDto rezdto = new RezeptDto(new IK("123456789"));
        List<Rezept> rezepte = rezdto.all();
        assertFalse(rezepte.isEmpty());
    }

    @Test
    public void testeRZNrName() throws Exception {

        RezeptDto rezdto = new RezeptDto(new IK("123456789"));
        Optional<Rezept> rez = rezdto.byRezeptNr("ER1");
        assertTrue(rez.isPresent());
        assertEquals("ER1",rez.get().REZ_NR);

    }

}
