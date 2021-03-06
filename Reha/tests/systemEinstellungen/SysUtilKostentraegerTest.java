package systemEinstellungen;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SysUtilKostentraegerTest {

    @Test
    public void testDateiNameCheckServerNewer() {
        /*
         * Caller of dateiNameCheck(g, i) currently checks prior to calling dateiNameCheck whether the
         * filenames are equal, so (currently) there are no safeguards in method dateiNameCheck that
         * check for differences in Kasse(ntyp) or exactly same files (dateiNameCheck only considers
         * the dates and version parts of filename).
         */



        SysUtilKostentraeger sukt = new SysUtilKostentraeger("Testing");

        String GKVFile="AO05Q220.ke0";
        String IniFile="AO05Q120.ke0";
        assertTrue("GKV hat neueres Quartal", sukt.dateiNameCheck(GKVFile, IniFile));


        GKVFile="AO05Q121.ke0";
        IniFile="AO05Q320.ke0";
        assertTrue("GKV hat neueres Jahr, aber frueheres Quartal", sukt.dateiNameCheck(GKVFile, IniFile));
        assertFalse("Lokal hat neueres Jahr, aber frueheres Quartal", sukt.dateiNameCheck(IniFile, GKVFile));

        GKVFile="AO05Q220.ke0";
        IniFile="AO05Q120.ke1";
        assertTrue("GKV hat kleinere Version im naechsten Quartal", sukt.dateiNameCheck(GKVFile, IniFile));

        GKVFile="AO05Q220.ke1";
        IniFile="AO05Q220.ke0";
        assertTrue("GKV hat hoehere Version im gleichem Quartal", sukt.dateiNameCheck(GKVFile, IniFile));
    }

    public void testDateiNameCheckLocalNewer() {
        /*
         * Caller of dateiNameCheck(g, i) currently checks prior to calling dateiNameCheck whether the
         * filenames are equal, so (currently) there are no safeguards in method dateiNameCheck that
         * check for differences in Kasse(ntyp) or exactly same files (dateiNameCheck only considers
         * the dates and version parts of filename).
         */
        SysUtilKostentraeger sukt = new SysUtilKostentraeger("Testing");

        String GKVFile="AO05Q120.ke0";
        String IniFile="AO05Q220.ke0";
        assertTrue("Lokal hat neueres Quartal", sukt.dateiNameCheck(GKVFile, IniFile));

        GKVFile="AO05Q120.ke1";
        IniFile="AO05Q220.ke0";
        assertTrue("Lokal hat kleinere Version im naechsten Quartal", sukt.dateiNameCheck(GKVFile, IniFile));

        GKVFile="AO05Q220.ke0";
        IniFile="AO05Q220.ke1";
        assertTrue("Lokal hat hoehere Version im gleichem Quartal", sukt.dateiNameCheck(GKVFile, IniFile));
    }
}
