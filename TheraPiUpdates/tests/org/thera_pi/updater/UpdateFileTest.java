package org.thera_pi.updater;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;


public class UpdateFileTest {


    @Test
    public void versionsForIncrementalAreExtracted() throws Exception {
        File file = new File("therapi_1_0_0_1_1_1.exe");
        UpdateFile ufile = new UpdateFile(file );
        assertEquals(new Version(1,0,0),ufile.from);
        assertEquals(new Version(1,1,1),ufile.to);

        File file1 = new File("therapi_12_3_123_14_4_6.zip");
        UpdateFile ufile1 = new UpdateFile(file1 );
        assertEquals(new Version(12,3,123),ufile1.from);
        assertEquals(new Version(14,4,6),ufile1.to);

    }


    @Test
    public void versionForFullAreExtracted() throws Exception {
        File file = new File("therapi_1_1_5.zip");
        UpdateFile ufile = new UpdateFile(file );
        assertEquals(new Version(1,0,0),ufile.from);
        assertEquals(new Version(1,1,5),ufile.to);
    }
}
