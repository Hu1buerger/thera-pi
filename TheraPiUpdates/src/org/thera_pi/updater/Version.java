package org.thera_pi.updater;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;

public class Version implements Comparable<Version> {

    private final boolean IS_TESTVERSION = true;
    private final LocalDate releaseDate = LocalDate.of(2021, 01, 10);
    public final int major;
    public final int minor;
    public final int revision;

    public Version() {
        major = 1;
        minor = 1;
        revision = 12;
    }

    public boolean isTestVersion(){
        return IS_TESTVERSION;
    }

    Version(int major, int minor, int revision) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    public String number() {
        return String.format("%d.%d.%d", major, minor, revision);
    }


    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRevision() {
        return revision;
    }

    @Override
    public String toString() {
        return "Version [major=" + major + ", minor=" + minor + ", revision=" + revision + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, revision);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Version other = (Version) obj;
        return major == other.major && minor == other.minor && revision == other.revision;
    }

    private static final Comparator<Version> NATURAL_ORDER_COMPARATOR = Comparator.comparing(Version::getMajor)
                                                                                  .thenComparing(Version::getMinor)
                                                                                  .thenComparing(Version::getRevision);

    @Override
    public int compareTo(Version o) {
        return NATURAL_ORDER_COMPARATOR.compare(this, o);
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

}
