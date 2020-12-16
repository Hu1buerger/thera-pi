package hmv;

import java.time.LocalDate;
import java.util.Objects;

import core.Arzt;
import core.Disziplin;
import core.Krankenversicherung;
import core.Patient;
import mandant.Mandant;

public class Hmv  {
    User angelegtvon;
    //mandant
    Mandant mandant;
    public Disziplin disziplin;
    //Patient
    Patient patient;
    Arzt arzt;
    Krankenversicherung kv;

    //HMV
    LocalDate ausstellungsdatum;

    Diagnose diag = new Diagnose(null, null,DG.INVALID, new Leitsymptomatik(DG.INVALID,"", ""));


    Behandlung beh;
    public Boolean dringlich;
    public HMVnr nummer;



    public Hmv(Context context) {
        mandant = context.mandant;
        patient = context.patient;
        arzt = patient.hauptarzt.orElse(null);
        kv = patient.kv;
        angelegtvon = context.user;

    }



    public Patient patient() {
        return patient;
    }



    @Override
    public String toString() {
        return "Hmv [angelegtvon=" + angelegtvon + ", mandant=" + mandant + ", disziplin=" + disziplin + ", patient="
                + patient + ", arzt=" + arzt + ", kv=" + kv + ", ausstellungsdatum=" + ausstellungsdatum + ", diag="
                + diag + ", beh=" + beh + ", dringlich=" + dringlich + "]";
    }



    @Override
    public int hashCode() {
        return Objects.hash(angelegtvon, arzt, ausstellungsdatum, beh, diag, disziplin, dringlich, kv, mandant,
                patient);
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Hmv))
            return false;
        Hmv other = (Hmv) obj;
        return Objects.equals(angelegtvon, other.angelegtvon) && Objects.equals(arzt, other.arzt)
                && Objects.equals(ausstellungsdatum, other.ausstellungsdatum) && Objects.equals(beh, other.beh)
                && Objects.equals(diag, other.diag) && disziplin == other.disziplin
                && Objects.equals(dringlich, other.dringlich) && Objects.equals(kv, other.kv)
                && Objects.equals(mandant, other.mandant) && Objects.equals(patient, other.patient);
    }



}
