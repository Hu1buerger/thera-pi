package org.therapi.reha.patient.verlauf;

import java.time.LocalDate;
import java.util.Objects;

public class Termin {
    LocalDate date;
    int patid;

    public Termin(LocalDate date, String behandler, int PatID) {
        super();
        this.patid = PatID;
        this.date = date;
        this.behandler = behandler;
    }

    String behandler;

    @Override
    public int hashCode() {
        return Objects.hash(behandler, date);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Termin other = (Termin) obj;
        return Objects.equals(behandler, other.behandler) && Objects.equals(date, other.date);
    }

    @Override
    public String toString() {
        return "Termin [date=" + date + ", behandler=" + behandler + "]";
    }

}
