package org.therapi.reha.patient.verlauf;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import sql.DatenquellenFactory;
import umfeld.Betriebsumfeld;

public final class VerlaufDokumentationOpener extends WindowAdapter {

    private final Verlauf verlauf;

    public VerlaufDokumentationOpener(Verlauf verlauf) {
        this.verlauf = verlauf;
    }

    @Override
    public void windowClosed(WindowEvent e) {
        TextEditDialogue verlaufEditDialogue = new TextEditDialogue();
        verlaufEditDialogue.setTitle(verlauf.dayofDocumentation.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        String dokutext = verlaufEditDialogue.show(verlauf.text);
        if (!dokutext.equals(verlauf.text)) {
            verlauf.text = dokutext;
            new Verlaufgate(new DatenquellenFactory(Betriebsumfeld.getAktIK())).save(Arrays.asList(verlauf));
        }
    }
}
