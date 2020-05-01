package org.thera_pi.updater;

import static org.junit.Assert.*;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class VersionsSiebTest {


@Test
public void testName() throws Exception {

    List<File> list = new LinkedList<>();
    list.add(new File("TheraPi_1_1_3_1_1_4.exe"));
    list.add(new File("TheraPi_1_1_3_jars.exe"));
    list.add(new File("TheraPi_1_1_4.exe"));
    list.add(new File("TheraPi_1_1_4_1_1_5.exe"));
    list.add(new File("TheraPi_1_1_5.exe"));

    VersionsSieb sieb112 = new VersionsSieb(new Version(1,1,2));
    List<File> list112 = new LinkedList<>();
    list112.add(new File("TheraPi_1_1_5.exe"));
    assertEquals( list112,  sieb112.select(list));


    VersionsSieb sieb114 = new VersionsSieb(new Version(1,1,4));
    List<File> list114 = new LinkedList<>();
    list114.add(new File("TheraPi_1_1_4_1_1_5.exe"));
    assertEquals( list114,  sieb114.select(list));

    VersionsSieb sieb234 = new VersionsSieb(new Version(2,3,4));
    List<File> list234 = new LinkedList<>();
    assertEquals( list234,  sieb234.select(list));


}



}
