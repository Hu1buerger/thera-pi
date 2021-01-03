package hmv;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;

import core.Adresse;
import core.Arzt;
import core.Befreiung;
import core.Disziplin;
import core.Krankenkasse;
import core.Krankenversicherung;
import core.LANR;
import core.Patient;
import core.VersichertenStatus;
import mandant.IK;
import mandant.Mandant;

public class CoreTestDataFactory {

    public static Arzt createArztEisenbart() {
        return new ArztFactory().withNachname("Eisenbart")
                                          .withArztnummer(new LANR("081500000"))
                                          .withBsnr("000008150")
                                          .build();
    }

    public static Patient createPatientSimonLant() {
        Patient patient = new Patient(new Adresse("", "hohle gasse 5", "12345", "Baumburg"));
        patient.nachname = "Lant";
        patient.vorname = "Simon";
        Krankenkasse kk = new KrankenkasseFactory().withIk(new IK("999999999"))
                                                   .withName("donotpay")
                                                   .build();
        Befreiung befreit = new Befreiung(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31));

        patient.kv = new Krankenversicherung(Optional.of(kk), "0815", VersichertenStatus.RENTNER, befreit);

        patient.geburtstag = LocalDate.of(1904, 2, 29);

        patient.hauptarzt = Optional.of(CoreTestDataFactory.createArztEisenbart());
        return patient;
    }


    public static Context createContext() {
        EnumSet<Disziplin> disziplinen = EnumSet.of(Disziplin.ER, Disziplin.KG);
        return new Context(new Mandant("123456789", "testmandant"), new User("bob"), disziplinen, createPatientSimonLant());
    }

    static  Hmv createHmv(Context context) {
        Hmv hmvorig = new Hmv(context);
        hmvorig.ausstellungsdatum = LocalDate.of(2020,05,25);
        hmvorig.dringlich = Boolean.TRUE;

        hmvorig.diag = new Diagnose(new Icd10("43.1"), new Icd10("69"),"ab5",new Leitsymptomatik(Leitsymptomatik.X,"besonders gaga"));
        hmvorig.beh = new Behandlung();
        hmvorig.disziplin = Disziplin.ER;
        return hmvorig;
    }



}
