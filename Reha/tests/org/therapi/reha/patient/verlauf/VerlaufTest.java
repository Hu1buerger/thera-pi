package org.therapi.reha.patient.verlauf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VerlaufTest {

    @Test
    public void copyConstructorreturnEqualVerlauf() throws Exception {
        Verlauf verl1 = new Verlauf();
        assertEquals(verl1, new Verlauf(verl1));
    }

}
