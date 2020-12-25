package hmv;

import java.util.Objects;
import java.util.function.Supplier;

public class Icd10 {
   static private final Icd10 EMPTY = new Icd10("");
   public final  String schluessel;
    String titelzeile;
    String beschreibung;

    public Icd10(String schluessel) {
        this.schluessel = schluessel;
        // TODO Auto-generated constructor stub
    }

    @Override
    public int hashCode() {
        return Objects.hash(beschreibung, schluessel, titelzeile);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Icd10))
            return false;
        Icd10 other = (Icd10) obj;
        return Objects.equals(beschreibung, other.beschreibung) && Objects.equals(schluessel, other.schluessel)
                && Objects.equals(titelzeile, other.titelzeile);
    }

    @Override
    public String toString() {
        return "Icd10 [schluessel=" + schluessel + ", titelzeile=" + titelzeile + ", beschreibung=" + beschreibung
                + "]";
    }

    public static Icd10 empty() {
        return EMPTY;
    }



}
