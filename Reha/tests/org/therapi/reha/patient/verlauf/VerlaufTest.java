package org.therapi.reha.patient.verlauf;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Test;

public class VerlaufTest {

    @Test
    public void copyConstructorreturnEqualVerlauf() throws Exception {
        Verlauf verl1 = new Verlauf();
        assertEquals(verl1, new Verlauf(verl1));
    }

    @Test
    public void testVerlaufTerminString() throws Exception {
        String rezept_nr = "rezeptnr";
        Verlauf verlauf = new Verlauf(new Termin(LocalDate.of(2020, 2, 1), "behandler", 123, rezept_nr), "aktuser");
        assertEquals(rezept_nr, verlauf.rezeptNr);

    }

}
