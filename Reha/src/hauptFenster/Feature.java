package hauptFenster;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class Feature {
    private static Set<Feature> features = Collections.synchronizedSet(new HashSet<Feature>());
    public final String name;


    public Feature(String name) {
        super();
        this.name = name;
    }

    void enable() {
        features.add(this);
    }

    void disable() {
        features.remove(this);
    }

    boolean isEnabled() {
        return features.contains(this);
    }

    public static void init() {
        new Feature("physiotec").enable();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Feature other = (Feature) obj;
        return Objects.equals(name.toLowerCase(), other.name.toLowerCase());
    }

    @Override
    public String toString() {
        return "Feature [name=" + name + "]";
    }

}
