package hmv;

import java.util.Objects;
import java.util.Optional;

public class Diagnose {

    final static String version = "HMR2020";
    final Icd10 icd10_1;
    final Icd10 icd10_2;
    final DG diagnosegruppe;
    final Leitsymptomatik leitsymptomatik; // [a-c|x]

    public Diagnose(Icd10 icd10_1, Icd10 icd10_2, DG dg, Leitsymptomatik leitsymptomatik) {
        super();
        this.icd10_1 = Optional.ofNullable(icd10_1).orElse(Icd10.empty());
        this.icd10_2 = Optional.ofNullable(icd10_2).orElse(Icd10.empty());
        this.diagnosegruppe = Optional.ofNullable(dg).orElse(DG.INVALID) ;
        this.leitsymptomatik = Optional.ofNullable(leitsymptomatik).orElse(Leitsymptomatik.empty());
    }


    @Override
    public String toString() {
        return "Diagnose [version=" + version + ", icd10_1=" + icd10_1 + ", icd10_2=" + icd10_2 + ", diagnosegruppe="
                + diagnosegruppe + ", leitsymptomatik=" + leitsymptomatik + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(diagnosegruppe, icd10_1, icd10_2, leitsymptomatik);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Diagnose))
            return false;
        Diagnose other = (Diagnose) obj;
        return Objects.equals(diagnosegruppe, other.diagnosegruppe) && Objects.equals(icd10_1, other.icd10_1)
                && Objects.equals(icd10_2, other.icd10_2) && Objects.equals(leitsymptomatik, other.leitsymptomatik);
    }

}
