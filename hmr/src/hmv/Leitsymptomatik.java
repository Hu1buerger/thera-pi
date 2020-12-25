package hmv;

import java.util.Objects;

import specs.Contracts;

public class Leitsymptomatik {
    final static String A = "A";
    final static String B = "B";
    final static String C = "C";
    final static String X = "X";
    private static final Leitsymptomatik INVALID = new Leitsymptomatik(DG.INVALID,"invalid", "no value");
   public final DG diagnosegruppe;
   public final String kennung ;// [a-c|x]
   public final String text;



    public Leitsymptomatik(DG diagnosegruppe, String kennung, String langtext) {
        Contracts.require(diagnosegruppe != null, "diagnosegruppe must not be null");
        Contracts.require(kennung != null, "kennung must not be null");
        Contracts.require(langtext != null, "langtext must not be null");
        this.diagnosegruppe = diagnosegruppe;
        this.kennung = kennung.toUpperCase();
        this.text = langtext;
    }
    @Override
    public int hashCode() {
        return Objects.hash(diagnosegruppe, kennung, text);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Leitsymptomatik))
            return false;
        Leitsymptomatik other = (Leitsymptomatik) obj;
        return Objects.equals(diagnosegruppe, other.diagnosegruppe) && Objects.equals(kennung, other.kennung)
                && Objects.equals(text, other.text);
    }
    @Override
    public String toString() {
        return "Leitsymptomatik [kennung=" + kennung + ", text=" + text + "]";
    }
    public static Leitsymptomatik empty() {
        return INVALID;
    }
}
