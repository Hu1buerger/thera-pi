package rezept;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Behandlung implements Comparable<Behandlung> {

    LocalDate datum;
    String kollege;
    String keineahnung;
    String heilmittel;
    private DateTimeFormatter format =DateTimeFormatter.ofPattern("dd.MM.yyyy");

    Behandlung(String fromDB){
        String[] parts = fromDB.split("@");
        datum = LocalDate.parse(parts[0],format);
        kollege= parts[1];
        keineahnung = parts[2];
        heilmittel = parts[3];
    }

    public Behandlung(LocalDate of, String kollege, String string2, String heilmittel) {
        if(of == null ) {
            throw new IllegalArgumentException("Date musst not be null");
        }
        datum = of;
        this.kollege = kollege;
        keineahnung = string2;
        this.heilmittel = heilmittel;

    }

    @Override
    public String toString() {
        return "Termin [datum=" + datum + ", kollege=" + kollege + ", keineahnung=" + keineahnung + ", heilmittel="
                + heilmittel + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((datum == null) ? 0 : datum.hashCode());
        result = prime * result + ((heilmittel == null) ? 0 : heilmittel.hashCode());
        result = prime * result + ((keineahnung == null) ? 0 : keineahnung.hashCode());
        result = prime * result + ((kollege == null) ? 0 : kollege.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Behandlung other = (Behandlung) obj;
        if (datum == null) {
            if (other.datum != null)
                return false;
        } else if (!datum.equals(other.datum))
            return false;
        if (heilmittel == null) {
            if (other.heilmittel != null)
                return false;
        } else if (!heilmittel.equals(other.heilmittel))
            return false;
        if (keineahnung == null) {
            if (other.keineahnung != null)
                return false;
        } else if (!keineahnung.equals(other.keineahnung))
            return false;
        if (kollege == null) {
            if (other.kollege != null)
                return false;
        } else if (!kollege.equals(other.kollege))
            return false;
        return true;
    }

    @Override
    public int compareTo(Behandlung o) {
       return datum.compareTo(o.datum);
    }
}
