package org.thera_pi.updater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class HTTPRepositoryTest {

    @Test
    public void allFilesonIndexPageAreParsed() throws Exception {
        HTTPRepository repo = new HTTPRepository(new URL("file://resources/index.html"));

        List<File> list = new LinkedList<>();
        list.add(new File("TheraPi_1_1_3_1_1_4.exe"));
        list.add(new File("TheraPi_1_1_3_jars.exe"));
        list.add(new File("TheraPi_1_1_4.exe"));
        list.add(new File("TheraPi_1_1_4_1_1_5.exe"));
        list.add(new File("TheraPi_1_1_5.exe"));
        File inputFile = new File("tests/resources/index.html");
        assertTrue(inputFile.exists());
        List<String> lines = Files.readAllLines(inputFile.toPath());
        assertNotEquals(0, lines.size());
        assertEquals(list, repo.extractFilesFromResponse(lines));
    }


}
