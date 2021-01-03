package core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mandant.Mandant;

public class Feature {
    private static final String FEATURESFILENAME = ".features";
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

    public boolean isEnabled() {
        return features.contains(this);
    }

    private static final Logger logger = LoggerFactory.getLogger(Feature.class);

    /**
     * searches for feature file and inits.
     *
     * @param mandant
     */
    public static void init(Mandant mandant) {
        StringBuilder pathStringBuilder = new StringBuilder();
        pathStringBuilder.append(environment.Path.Instance.getProghome());
        pathStringBuilder.append(File.separator);
        pathStringBuilder.append("ini");
        pathStringBuilder.append(File.separator);
        pathStringBuilder.append(mandant.ikDigitString());
        pathStringBuilder.append(File.separator);
        pathStringBuilder.append(FEATURESFILENAME);
        String pathname = pathStringBuilder.toString();
        init(new File(pathname));
    }

    /**
     * enables one feature for each line in file.
     *
     * @param file to be read from
     */
    public static void init(File file) {
        if (file.exists()) {
            try {
                List<String> featuresNames = Files.readAllLines(file.toPath());
                featuresNames.stream()
                             .map(Feature::new)
                             .forEach(Feature::enable);
            } catch (IOException e) {
                logger.error("cannot read all lines from existing file", e);
            }
        }

        if(!features.isEmpty()) logger.debug("starting with features:");
        for (Feature feature : features) {
            logger.debug(feature.name);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
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
