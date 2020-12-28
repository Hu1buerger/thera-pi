package hmv;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Behandlung {

    List<Heilmittel> heilmittel= new ArrayList<>(3) ;

    Heilmittel ergaenzend;

    boolean bericht;
    Hausbesuch hb;
    public int frequenzmin;
    public int frequenzmax;
    public int dauer;

    String therapiZiel;

    @Override
    public int hashCode() {
        return Objects.hash(bericht, ergaenzend, frequenzmax, frequenzmin, hb, heilmittel, therapiZiel);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Behandlung))
            return false;
        Behandlung other = (Behandlung) obj;
        return bericht == other.bericht && Objects.equals(ergaenzend, other.ergaenzend)
                && frequenzmax == other.frequenzmax && frequenzmin == other.frequenzmin && hb == other.hb
                && Objects.equals(heilmittel, other.heilmittel) && Objects.equals(therapiZiel, other.therapiZiel);
    }

    @Override
    public String toString() {
        return "Behandlung [heilmittel=" + heilmittel + ", ergaenzend=" + ergaenzend + ", bericht=" + bericht + ", hb="
                + hb + ", frequenzmin=" + frequenzmin + ", frequenzmax=" + frequenzmax + ", therapiZiel=" + therapiZiel
                + "]";
    }



}
