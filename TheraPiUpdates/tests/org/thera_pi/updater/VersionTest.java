package org.thera_pi.updater;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class VersionTest {


    @Test
    public void versionenSortierenNachMajorMinorRevision() throws Exception {
        Version version100 = new Version(1,0,0);
        Version version101 = new Version(1,0,1);
        Version version201 = new Version(2,0,1);
        Version version110 = new Version(1,1,0);
        Version version000 = new Version(0,0,0);
        List<Version> automaticsorted = new LinkedList<>();
        automaticsorted.add(version100);
        automaticsorted.add(version101);
        automaticsorted.add(version201);
        automaticsorted.add(version110);
        automaticsorted.add(version000);
        List<Version> sorted = new LinkedList<>();
        sorted.add(version000);
        sorted.add(version100);
        sorted.add(version101);
        sorted.add(version110);
        sorted.add(version201);
        Collections.sort( automaticsorted);
        assertEquals(sorted, automaticsorted);



    }

}
