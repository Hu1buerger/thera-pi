package rezept;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class Behandlung implements Comparable<Behandlung> {
    LocalDate datum;
    String kollege;
    String unterbrechungsbegruendung;
    /** Mehrere mit kommata getrennt. */
    List<String> heilmittel;
    private DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    Behandlung(String fromDB) {
        String[] parts = fromDB.split("@");
        datum = LocalDate.parse(parts[0], format);
        kollege = parts[1];
        unterbrechungsbegruendung = parts[2];
        heilmittel = Arrays.asList(parts[3].replace(" ", "")
                                           .split(","));
    }

    public Behandlung(LocalDate of, String kollege, String string2, String heilmittel) {
        if (of == null) {
            throw new IllegalArgumentException("Date musst not be null");
        }
        datum = of;
        this.kollege = kollege;
        unterbrechungsbegruendung = string2;
        this.heilmittel = Arrays.asList(heilmittel.replace(" ", "")
                                                  .split(","));
    }

    @Override
    public String toString() {
        return "Termin [datum=" + datum + ", kollege=" + kollege + ", unterbrechungsbegründung="
                + unterbrechungsbegruendung + ", heilmittel=" + heilmittel + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((datum == null) ? 0 : datum.hashCode());
        result = prime * result + ((heilmittel == null) ? 0 : heilmittel.hashCode());
        result = prime * result + ((unterbrechungsbegruendung == null) ? 0 : unterbrechungsbegruendung.hashCode());
        return prime * result + ((kollege == null) ? 0 : kollege.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Behandlung other = (Behandlung) obj;
        if (datum == null) {
            if (other.datum != null) {
                return false;
            }
        } else if (!datum.equals(other.datum)) {
            return false;
        }
        if (heilmittel == null) {
            if (other.heilmittel != null) {
                return false;
            }
        } else if (!heilmittel.equals(other.heilmittel)) {
            return false;
        }
        if (unterbrechungsbegruendung == null) {
            if (other.unterbrechungsbegruendung != null) {
                return false;
            }
        } else if (!unterbrechungsbegruendung.equals(other.unterbrechungsbegruendung)) {
            return false;
        }
        if (kollege == null) {
            if (other.kollege != null) {
                return false;
            }
        } else if (!kollege.equals(other.kollege)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Behandlung o) {
        return datum.compareTo(o.datum);
    }

    public List<String> erbrachteHeilmittel() {
        return heilmittel;
    }
}
