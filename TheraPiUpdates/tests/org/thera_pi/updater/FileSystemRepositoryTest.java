package org.thera_pi.updater;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileSystemRepositoryTest {

    private static final File TEST_IN_FILE = new File("./tests/org/thera_pi/updater/filesin/therapi_1_0_0_1_1_3.zip");
    private static final File TEST_OUT_FILE = new File("./tests/org/thera_pi/updater/filesout/therapi_1_0_0_1_1_3.zip");

    @BeforeClass
    public static void vorbereiten() throws IOException {
        if (!Files.exists(TEST_IN_FILE.toPath()))
            Files.createFile(TEST_IN_FILE.toPath());

    }

    @AfterClass
    public static void aufraeumen() throws IOException {
        Files.deleteIfExists(TEST_IN_FILE.toPath());
        Files.deleteIfExists(TEST_OUT_FILE.toPath());

    }

    @Test
    public void listFilesFindsAllFiles() throws Exception {
        FileSystemRepository filerep = new FileSystemRepository(
                new File("./tests/org/thera_pi/updater/filesin").toPath());
        String[] filesList = filerep.filesList()
                                    .stream()
                                    .map(f -> f.getName())
                                    .collect(Collectors.toList())
                                    .toArray(new String[0]);
        String[] expected = { ".gitkeep", "therapi_1_0_0_1_1_3.zip" };
        assertArrayEquals(expected, filesList);
    }

    @Test
    public void neededFilesAreCopiedToSpecifiedLocation() throws Exception {
        FileSystemRepository filerep = new FileSystemRepository(
                new File("./tests/org/thera_pi/updater/filesin").toPath());

        List<File> neededList = filerep.filesList();
        assertEquals(2, filerep.downloadFiles(neededList, new File("./tests/org/thera_pi/updater/filesout/").toPath()));

    }

}
