package dto;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

import core.Befreiung;
import core.Disziplin;
import core.Krankenkasse;
import core.Krankenversicherung;
import core.Patient;
import core.User;
import core.VersichertenStatus;
import hmv.Context;
import hmv.CoreTestDataFactory;
import hmv.Hmv;
import mandant.Mandant;
import sql.DatenquellenFactory;

public class HmvDtoTest {
    @Test
    public void testInsert() throws Exception {
        Mandant mandant = new Mandant("123456789", "testmandant");
        User user = new User("bob",16);
        Patient patient = CoreTestDataFactory.createPatientSimonLant();
        Context context= new Context(mandant , user, patient);
        Hmv hmv  = createTestHmv(context);
        HmvDto dto = new HmvDto(hmv);
        DatenquellenFactory dq = new DatenquellenFactory(mandant.ikDigitString());
       assertNotEquals(1, dto.insert(dq));
    }


    private Hmv createTestHmv(Context context) {
     Hmv hmv= new Hmv(context);
     hmv.disziplin = Disziplin.ER;
     hmv.setHmvNummer(0);
     hmv.kv = new Krankenversicherung(Optional.of(new Krankenkasse("999999998")), "versnr0815", VersichertenStatus.FAMILIE, Optional.ofNullable(null));
     hmv.arzt= CoreTestDataFactory .createArztEisenbart();
return hmv;
    }

    @Test
    public void testUpdate() throws Exception {
        Mandant mandant = new Mandant("123456789", "testmandant");
        User user = new User("bob",16);
        Patient patient = CoreTestDataFactory.createPatientSimonLant();
        Context context= new Context(mandant , user, patient);
        Hmv hmv  = createTestHmv(context);
        hmv.id=5;
        HmvDto dto = new HmvDto(hmv);
        DatenquellenFactory dq = new DatenquellenFactory(mandant.ikDigitString());
       assertEquals(1, dto.update(dq));
    }


}
