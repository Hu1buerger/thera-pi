package mandant;

import java.util.Objects;

public class Mandant {

    private IK ik;
    private String name;

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
    
    public IK ik() {
        return ik;
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

}
