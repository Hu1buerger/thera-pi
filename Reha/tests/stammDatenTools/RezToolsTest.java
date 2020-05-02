package stammDatenTools;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RezToolsTest {

    @Test
    public void testPutRezNrGetDisziplin() throws Exception {


        String anytext="anytext";
            assertEquals( "Physio", RezTools.getDisziplinFromRezNr("KG" + anytext));
            assertEquals( "Massage", RezTools.getDisziplinFromRezNr("MA" + anytext));
            assertEquals( "Ergo", RezTools.getDisziplinFromRezNr("ER" + anytext));
            assertEquals( "Logo", RezTools.getDisziplinFromRezNr("LO" + anytext));
            assertEquals( "Reha", RezTools.getDisziplinFromRezNr("RH" + anytext));
            assertEquals( "Podo", RezTools.getDisziplinFromRezNr("PO" + anytext));
            assertEquals( "Rsport", RezTools.getDisziplinFromRezNr("RS" + anytext));
            assertEquals( "Ftrain", RezTools.getDisziplinFromRezNr("FT" + anytext));

    }

}
