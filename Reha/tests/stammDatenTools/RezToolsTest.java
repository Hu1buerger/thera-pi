package stammDatenTools;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RezToolsTest {

    @Test
    public void testPutRezNrGetDisziplin() throws Exception {


        String anytext="anytext";
            assertEquals( "Physio", RezTools.putRezNrGetDisziplin("KG" + anytext));
            assertEquals( "Massage", RezTools.putRezNrGetDisziplin("MA" + anytext));
            assertEquals( "Ergo", RezTools.putRezNrGetDisziplin("ER" + anytext));
            assertEquals( "Logo", RezTools.putRezNrGetDisziplin("LO" + anytext));
            assertEquals( "Reha", RezTools.putRezNrGetDisziplin("RH" + anytext));
            assertEquals( "Podo", RezTools.putRezNrGetDisziplin("PO" + anytext));
            assertEquals( "Rsport", RezTools.putRezNrGetDisziplin("RS" + anytext));
            assertEquals( "Ftrain", RezTools.putRezNrGetDisziplin("FT" + anytext));

    }

}
