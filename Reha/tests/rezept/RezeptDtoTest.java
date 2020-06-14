package rezept;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import mandant.IK;

public class RezeptDtoTest {

    static RezeptDto rezDto;
    
    @BeforeClass
    public static void initForAllTests() {
        rezDto = new RezeptDto(new IK("123456789"));
    }
    
    @Test
    public void holeAlle() throws Exception {
        List<Rezept> rezepte = rezDto.all();
        assertFalse(rezepte.isEmpty());
    }

    @Test
    public void rzNrName() throws Exception {
        Optional<Rezept> rez = rezDto.byRezeptNr("ER1");
        assertTrue(rez.isPresent());
        assertEquals(new Rezeptnummer("ER1"),rez.get().rezNr);

    }
    
    @Test
    public void rezAvailable() {
        assertTrue(rezDto.countAlleEintraege() > 0);
    }
    
    @Test
    public void saveEmptyRezept() {
        Rezept rez = new Rezept();
        int anzRezB4Test = rezDto.countAlleEintraege();
        assertFalse(rezDto.rezeptInDBSpeichern(rez));
        assertTrue(anzRezB4Test == rezDto.countAlleEintraege());
    }

}
