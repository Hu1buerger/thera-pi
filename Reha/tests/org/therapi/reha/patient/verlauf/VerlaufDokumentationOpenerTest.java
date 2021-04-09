package org.therapi.reha.patient.verlauf;

import org.junit.Ignore;
import org.junit.Test;
import org.therapi.reha.patient.verlauf.Verlauf;

import org.therapi.reha.patient.verlauf.VerlaufDokumentationOpener;

public class VerlaufDokumentationOpenerTest {

    @Test
    @Ignore
    public void testVerlaufDokumentationOpener() throws Exception {
       Verlauf verlauf = new Verlauf();
      VerlaufDokumentationOpener opener = new VerlaufDokumentationOpener(verlauf);
      opener.windowClosed(null);
    }

}
