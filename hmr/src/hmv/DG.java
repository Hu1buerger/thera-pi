package hmv;

import java.util.Objects;

public class DG {

    public static final DG INVALID = new DG("Invalid","invalid");
    public final String gruppe;
    final String beschreibung;

    public DG(String gruppe, String beschreibung) {
        super();
        this.gruppe = gruppe;
        this.beschreibung = beschreibung;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beschreibung, gruppe);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof DG))
            return false;
        DG other = (DG) obj;
        return Objects.equals(beschreibung, other.beschreibung) && Objects.equals(gruppe, other.gruppe);
    }

    @Override
    public String toString() {
        return "DG [gruppe=" + gruppe + ", beschreibung=" + beschreibung + "]";
    }


}
