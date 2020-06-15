package rezept;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import mandant.IK;

public class RezeptFertigeDtoTest {
    private static RezeptFertigeDto rfDto;
    private static RezeptDto rDto;
    
    @BeforeClass
    public static void initForAllTests() {
        rfDto = new RezeptFertigeDto(new IK("123456789"));
        rDto = new RezeptDto(new IK("123456789"));
    }

    @Test
    public void fertigesRezeptSpeichernUndLoeschenTest() {
        Optional<Rezept> orez = rDto.byRezeptNr("ER1");
        assertTrue("We need a Rezept to continue", orez.isPresent());
        Rezept rez = orez.orElse(null);
        RezeptFertige rf = new RezeptFertige(new IK("123456789"));
        rf.setRezNr("ER1");
        int anzahlFertigeRez = rfDto.countAlleEintraege();
        rfDto.saveToDB(rf);
        assertTrue("Es muessen mehr fertige Rezepte als vorher in DB sein", anzahlFertigeRez < rfDto.countAlleEintraege());
        rf = rfDto.getByRezNr("ER1");
        assertTrue("Rezept sollte gefunden werden", rf != null);
        rfDto.deleteByRezNr("ER1");
        assertTrue("Es muessen wieder gleich viele Rezepte sein", anzahlFertigeRez == rfDto.countAlleEintraege());
        rf = rfDto.getByRezNr("ER1");
        assertTrue("ER1 darf nicht mehr gefunden werden", rf == null);
    }
    
}
