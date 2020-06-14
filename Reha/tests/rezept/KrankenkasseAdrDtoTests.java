package rezept;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import mandant.IK;

public class KrankenkasseAdrDtoTests {
    static KrankenkasseAdrDto kkaDto;
    
    @BeforeClass
    public static void initForAllTests() {
        kkaDto = new KrankenkasseAdrDto(new IK("123456789"));
    }
    
    @Test
    public void getByIdTests() {
        Optional<KrankenkasseAdr> kka = kkaDto.getById(18);
        assertTrue("We should have found at least 1 KK-adr", kka.isPresent());
        kka = kkaDto.getById(99);
        assertFalse("We should have found no KK-adr", kka.isPresent());
    }

    @Test
    public void getIKsByIdTest() {
        Optional<KrankenkasseAdr> okka = kkaDto.getIKsById(18);
        assertTrue("We should have found at least 1 KK-adr", okka.isPresent());
        KrankenkasseAdr kka = okka.orElse(null);
        assertTrue("The IK-Kasse should be set", kka.getIkKasse() != null);
        assertTrue("The IK-Kostentraeger should be set", kka.getIkKostenTraeger() != null);
        assertTrue("We shouldn't have any other vals set", kka.getKuerzel() == null);
    }

    @Test
    public void getAllePreisgruppenFelderByIdTest() {
        boolean mitRs=false;
        Optional<KrankenkasseAdr> okka = kkaDto.getAllePreisgruppenFelderById(18, mitRs);
        assertTrue("We should have found at least 1 KK-adr", okka.isPresent());
        KrankenkasseAdr kka = okka.orElse(null);
        assertTrue("Expected Ergo Preisgruppe to be >0 - got " + kka.getPgEr(), kka.getPgEr() >0);
        assertTrue("Expected Physio Preisgruppe to be >0 - got " + kka.getPgKg(), kka.getPgKg() >0);
        assertTrue("Expected Logopaedie Preisgruppe to be >0 - got " + kka.getPgLo(), kka.getPgLo() >0);
        assertTrue("Expected Massage Preisgruppe to be >0 - got " + kka.getPgMa(), kka.getPgMa() >0);
        assertTrue("Expected Podologie Preisgruppe to be >0 - got " + kka.getPgPo(), kka.getPgPo() >0);
        assertTrue("Expected Reha Preisgruppe to be >0 - got " + kka.getPgRh(), kka.getPgRh() >0);
        assertTrue("Expected Preisgruppe to be >0 - got " + kka.getPreisgruppe(), kka.getPreisgruppe() >0);
        assertTrue("IK-Kasse should not have been filled with a value", kka.getIkKasse() == null);
    }
}
