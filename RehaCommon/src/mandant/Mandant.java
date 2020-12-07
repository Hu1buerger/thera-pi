package mandant;

import java.util.EnumSet;
import java.util.Objects;

import core.Disziplin;

public class Mandant {

    private IK ik;
    private String name;
    public final static Mandant nullMandant = new Mandant("000000000", "Übungs-Mandant");
    private EnumSet<Disziplin> aktiveDisziplinen = EnumSet.noneOf(Disziplin.class);

    public Mandant(String ik, String name) {
        this.ik = new IK(ik);
        this.name = name;
    }

    public String name() {
        return name;
    }

    public String ikDigitString() {
        return ik.digitString();
    }

    @Override
    public String toString() {

        return name + " - IK" + ik.digitString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ik, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Mandant other = (Mandant) obj;
        return Objects.equals(ik, other.ik) && Objects.equals(name, other.name);
    }

    public IK ik() {
        return ik;
    }

    public void disziplinen(EnumSet<Disziplin> aktiveDisziplinen) {
        this.aktiveDisziplinen = aktiveDisziplinen;
    }

    public EnumSet<Disziplin> disziplinen() {
        return aktiveDisziplinen;
    }

}
