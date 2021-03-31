package org.therapi.reha.patient.verlauf;

import java.time.LocalDate;
import java.util.Objects;

public class Verlauf {

    public int patientID = 0;
    public String therapist = "";
    public String documentator = "";
    public String rezeptNr = "";
    public LocalDate documentedDay = LocalDate.now();
    public LocalDate dayofDocumentation = LocalDate.now();
    public String text = "";
    public int id;

    public Verlauf() {

    }

    public Verlauf(Verlauf orig) {
        super();
        this.id = orig.id;
        this.patientID = orig.patientID;
        this.therapist = orig.therapist;
        this.documentator = orig.documentator;
        this.rezeptNr = orig.rezeptNr;
        this.documentedDay = orig.documentedDay;
        this.dayofDocumentation = orig.dayofDocumentation;
        this.text = orig.text;
    }

    @Override
    public String toString() {
        return "Verlauf [patientID=" + patientID + ", therapist=" + therapist + ", documentator=" + documentator
                + ", rezeptNr=" + rezeptNr + ", documentedDay=" + documentedDay + ", dayofDocumentation="
                + dayofDocumentation + ", text=" + text + ", id=" + id + "]";
    }

    public Verlauf(Termin termin, String aktUser) {

        this.documentator = aktUser;
        this.patientID = termin.patid;
        this.documentedDay = termin.date;
        this.therapist = termin.behandler;
        this.rezeptNr = termin.rezept_nr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayofDocumentation, documentator, documentedDay, id, patientID, rezeptNr, text, therapist);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Verlauf))
            return false;
        Verlauf other = (Verlauf) obj;
        return Objects.equals(dayofDocumentation, other.dayofDocumentation)
                && Objects.equals(documentator, other.documentator)
                && Objects.equals(documentedDay, other.documentedDay) && id == other.id && patientID == other.patientID
                && rezeptNr == other.rezeptNr && Objects.equals(text, other.text)
                && Objects.equals(therapist, other.therapist);
    }

}
