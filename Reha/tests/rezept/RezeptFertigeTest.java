package rezept;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import mandant.IK;

public class RezeptFertigeTest {

    private static RezeptFertigeDto rfDto;
    private static RezeptDto rDto;
    
    @BeforeClass
    public static void initForAllTests() {
        rfDto = new RezeptFertigeDto(new IK("123456789"));
        rDto = new RezeptDto(new IK("123456789"));
    }

    @Test
    public void invalidRezept() {
        Rezept rez = new Rezept();
        RezeptFertige rf = new RezeptFertige(rez, new IK("123456789"));
        int anzahlFertige = rfDto.countAlleEintraege();
        assertFalse("Dieses Rezept sollte sich nicht abschliessen lassen", rf.RezeptErledigt());
        // Not sure if the following is legal, since tests may run in parallel?
        assertTrue("Die Anzahl sollte sich nicht geaendert haben", anzahlFertige == rfDto.countAlleEintraege());
        assertFalse("Und erst recht nicht aufschliessen lassen", rf.RezeptRevive());
        assertTrue("Die Anzahl sollte sich nicht geaendert haben", anzahlFertige == rfDto.countAlleEintraege());
    }
    
    @Test
    public void rezeptAbUnAufschliessenTest() {
        Optional<Rezept> orez = rDto.byRezeptNr("ER1");
        assertTrue("We need a Rezept to continue", orez.isPresent());
        Rezept rez = orez.orElse(null);
        assertFalse("Rezept sollte noch nicht abgeschlossen sein",rez.isAbschluss());
        Optional<RezeptFertige> oRF = rfDto.getByRezNr("ER1");
        assertFalse("ER1 sollte nun in abgeschlossene gefunden werden", oRF.isPresent());
        RezeptFertige rf = new RezeptFertige(rez, new IK("123456789"));
        rf.RezeptErledigt();
        rez = rDto.byRezeptNr("ER1").get();
        assertTrue("Rezept sollte jetzt im abgeschlossenem Zustand in DB gespeichert sein", rez.isAbschluss());
        oRF = rfDto.getByRezNr("ER1");
        assertTrue("ER1 sollte nun in abgeschlossene gefunden werden", oRF.isPresent());
        rf.RezeptRevive();
        rez = rDto.byRezeptNr("ER1").get();
        assertFalse("Rezept sollte jetzt im aufgeschlossenem Zustand in DB gespeichert sein", rez.isAbschluss());
        
    }
}
