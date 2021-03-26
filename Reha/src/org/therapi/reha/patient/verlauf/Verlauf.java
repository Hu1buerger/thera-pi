package org.therapi.reha.patient.verlauf;

import java.time.LocalDate;


public class Verlauf {

    public int patientID = 0;
    public String therapist = "";
    public String documentator = "";
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

        this.documentedDay = orig.documentedDay;
        this.dayofDocumentation = orig.dayofDocumentation;
        this.text = orig.text;
    }

    @Override
    public String toString() {
        return "Verlauf [patientID=" + patientID + ", therapist=" + therapist + ", documentator=" + documentator
                + ", documentedDay=" + documentedDay + ", dayofDocumentation=" + dayofDocumentation + ", text=" + text
                + "]";
    }

    public Verlauf(Termin termin, String aktUser) {
        
        this.documentator = aktUser;
        this.patientID = termin.patid;
        this.documentedDay = termin.date;
        this.therapist = termin.behandler;
    }

}
