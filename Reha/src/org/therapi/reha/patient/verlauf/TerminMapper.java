package org.therapi.reha.patient.verlauf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TerminMapper {

    private static final DateTimeFormatter DDMYYYY = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private String terminString;
    private List<Termin> termine;
    private String rezept_nr;
    private int pat_id;

    public TerminMapper(String terminString, int pat_id, String rezeptNr) {
        this.terminString = terminString;
        this.pat_id = pat_id;
        rezept_nr = rezeptNr;
        this.termine = termine();

    }

    public List<Termin> termine() {
        if (terminString == null)
            return Collections.emptyList();

        String[] lines = terminString.split("\\n");

        ArrayList<Termin> liste = new ArrayList<Termin>();
        for (String line : lines) {
            liste.add(parse(line));
        }

        return liste;
    }

    private Termin parse(String line) {
        String[] data = line.split("@");
        return new Termin(LocalDate.parse(data[0], DDMYYYY), data[1], pat_id, rezept_nr);
    };

    String asString() {
        Termin termin = termine.get(0);
        return termin.date.format(DDMYYYY) + "@" + termin.behandler.toString() + "@@@";
    }
}
