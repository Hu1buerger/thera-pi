package CommonTools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class INIToolTest {

    @Test
    public void filesInIncontroleIniAreMeantForDatabase() throws Exception {

        String pfad = "./tests/resources/";
        INITool.init(pfad);

        assertEquals(2, INITool.anzahlInisInDB());
        assertTrue(INITool.inisInDb.contains("color.ini"));
        assertTrue(INITool.inisInDb.contains("james.ini"));
        assertFalse(INITool.inisInDb.contains("michnicht.ini"));

    }

}
