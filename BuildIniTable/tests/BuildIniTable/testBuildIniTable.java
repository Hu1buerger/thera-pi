package BuildIniTable;

import org.junit.Test;
import static org.junit.Assert.*;
import BuildIniTable.ProcessPanel;
// For hand-coded test-setup:
import java.util.Vector;
// For test-ini driven tests:
import CommonTools.INIFile;
import java.io.File;

public class testBuildIniTable {

    public BuildIniTable buildIniTable = new BuildIniTable();
    public ProcessPanel procPanel;
    
    @Test
    public void testSetGetPfadzurmandini() {
	String dirName=".";
        File fileName = new File(dirName);
        File[] fileList = fileName.listFiles();
        
        for (File file: fileList) {
            
            System.out.println(file);
        }
        buildIniTable.setPfadzurmandini("test");
        assertEquals("test", buildIniTable.getPfadzurmandini());
    }
    
    @Test
    public void testMandIniExist() {
        assertEquals(true, BuildIniTable.mandIniExist("./tests/resources/ini/mandanten.ini"));
        assertEquals(false, BuildIniTable.mandIniExist("./tests/resources/ini/keinemand.ini"));
    }
    
    @Test
    public void testMandantTesten() {
        // Simple test only, since non-existing mandanten.ini will invoke file-chooser
        this.buildIniTable.setPfadzurmandini("./tests/resources/ini/mandanten.ini");
        assertEquals("./tests/resources/ini/mandanten.ini", this.buildIniTable.mandantTesten());
    }
    
    // ProgressPanel tests
    @Test
    public void testPPGetIniList() {
        /* 
         * getIniList actually does 2 things:
         *   - it populates the PP Vector holding the names of inis
         *   - it populates the check-box-table to choose inis from
         */
        this.buildIniTable.thisClass = this.buildIniTable;
        this.buildIniTable.thisClass.pfadzurini = "./tests/resources/ini";
        this.buildIniTable.anzahlmandanten = 1;
        this.buildIniTable.mandantIkvec.add("123456789");
        this.buildIniTable.mandantNamevec.add("Test Mandant 1");
        
        ProcessPanel pan = new ProcessPanel("UnitTest");
        this.procPanel = pan;
        pan.tabmod = pan.new MyIniTableModel();
        pan.inivec.clear();
        assertEquals(0, pan.inivec.size()); 
        pan.getIniList();
        assertTrue("Check size of ProgressPanel.inivec >0 after calling getIniList",
                             pan.inivec.size() > 0); // Unit test
        assertEquals("test.ini", pan.inivec.get(0)); // Integration test
    }
}
