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
    public void firstTest() {
        Optional<Rezept> orez = rDto.byRezeptNr("ER1");
        assertTrue("We need a Rezept to continue", orez.isPresent());
        Rezept rez = orez.orElse(null);
        RezeptFertige rf = new RezeptFertige(rez, new IK("123456789"));
    }
}
