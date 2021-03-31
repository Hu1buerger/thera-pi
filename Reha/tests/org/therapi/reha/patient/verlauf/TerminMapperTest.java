package org.therapi.reha.patient.verlauf;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TerminMapperTest {

    @Test
    public void testTerminMapper() throws Exception {
        String behandler = "Katrin Ehring";
        String terminString = "20.02.2019@" + behandler + "@@54105@2019-02-20\n27.02.2019@Katrin Ehring@@54105@2019-02-27"
                + "\n"
                + "06.03.2019@Katrin Ehring@@54105@2019-03-06"
                  + "\n"
                + "13.03.2019@Katrin Ehring@@54105@2019-03-13";

        int pat_id = 5;
        String rezeptNr = "123";
        TerminMapper mapper = new TerminMapper(terminString, pat_id, rezeptNr);

        List<Termin> terminListe = mapper.termine();
        assertEquals(4, terminListe.size());
        List<Termin> expectedTerminListe = Arrays.asList(new Termin(LocalDate.of(2019, 2, 20),behandler, pat_id, rezeptNr),
                new Termin(LocalDate.of(2019, 2, 27),behandler, pat_id, rezeptNr),new Termin(LocalDate.of(2019,3,6),behandler, pat_id, rezeptNr),new Termin(LocalDate.of(2019, 3,13),behandler, pat_id, rezeptNr));
        assertEquals(expectedTerminListe, terminListe);
    }

}
