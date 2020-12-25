package hmv;

import java.util.Objects;
import java.util.Optional;

public class Diagnose {

    final static String version = "HMR2020";
    final public Icd10 icd10_1;
    final public Icd10 icd10_2;
    final public String text;
    final public Leitsymptomatik leitsymptomatik; // [a-c|x]

    public Diagnose(Icd10 icd10_1, Icd10 icd10_2, String text, Leitsymptomatik leitsymptomatik) {
        super();
        this.icd10_1 = Optional.ofNullable(icd10_1).orElse(Icd10.empty());
        this.icd10_2 = Optional.ofNullable(icd10_2).orElse(Icd10.empty());
        this.text= Optional.ofNullable(text).orElse("");
        this.leitsymptomatik = Optional.ofNullable(leitsymptomatik).orElse(Leitsymptomatik.empty());
    }


  


	@Override
	public String toString() {
		return "Diagnose [icd10_1=" + icd10_1 + ", icd10_2=" + icd10_2 + ", leitsymptomatik=" + leitsymptomatik + "]";
	}

    @Override
	public int hashCode() {
		return Objects.hash(icd10_1, icd10_2, leitsymptomatik);
	}

    @Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Diagnose)) {
			return false;
		}
		Diagnose other = (Diagnose) obj;
		return Objects.equals(icd10_1, other.icd10_1) && Objects.equals(icd10_2, other.icd10_2)
				&& Objects.equals(leitsymptomatik, other.leitsymptomatik);
	}

}
