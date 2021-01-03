package BuildIniTable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class buildIniTableTest {

    public BuildIniTable buildIniTable = new BuildIniTable();
    public ProcessPanel procPanel;

    @Test
    public void testSetGetPfadzurmandini() {
        buildIniTable.setPfadzurmandini("test");
        assertEquals("test", buildIniTable.getPfadzurmandini());
    }

    /**
     * requires human input in test?????
     */
    //@Test
    public void testMandantTesten() {
        // Simple test only, since non-existing mandanten.ini will invoke file-chooser
        buildIniTable.setPfadzurmandini("../Files/tests/resources/ini/123456789/mandanten.ini");
        assertEquals("../Files/tests/resources/ini/123456789/mandanten.ini", buildIniTable.mandantTesten());
        //Whats the point in this test?
    }

    // ProgressPanel tests
    @Test
    public void testPPGetIniList() {
        /*
         * getIniList actually does 2 things: - it populates the PP Vector holding the
         * names of inis - it populates the check-box-table to choose inis from
         */
        BuildIniTable.thisClass = buildIniTable;
        BuildIniTable.thisClass.pfadzurini = "./tests/resources/ini";
        buildIniTable.anzahlmandanten = 1;
        buildIniTable.mandantIkvec.add("123456789");
        buildIniTable.mandantNamevec.add("Test Mandant 1");

        ProcessPanel pan = new ProcessPanel("UnitTest");
        procPanel = pan;
        pan.tabmod = pan.new MyIniTableModel();
        pan.inivec.clear();
        assertEquals(0, pan.inivec.size());
        pan.getIniList();
        assertTrue("Check size of ProgressPanel.inivec >0 after calling getIniList", pan.inivec.size() > 0); // Unit
                                                                                                             // test
        assertEquals("test.ini", pan.inivec.get(0)); // Integration test
    }
}