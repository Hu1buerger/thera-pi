package org.therapi.reha.patient.verlauf;

import java.time.LocalDate;
import java.util.Objects;

public class Termin {
    LocalDate date;
    int patid;
    private String rezept_nr;

    public Termin(LocalDate date, String behandler, int PatID, String rezept_nr) {
        super();
        this.patid = PatID;
        this.date = date;
        this.behandler = behandler;
        this.rezept_nr = rezept_nr;
    }

    String behandler;

    @Override
    public int hashCode() {
        return Objects.hash(behandler, date, patid, rezept_nr);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Termin))
            return false;
        Termin other = (Termin) obj;
        return Objects.equals(behandler, other.behandler) && Objects.equals(date, other.date) && patid == other.patid
                && rezept_nr == other.rezept_nr;
    }

    @Override
    public String toString() {
        return "Termin [date=" + date + ", rezept_id=" + rezept_nr + ", behandler=" + behandler + "]";
    }

}
