package org.thera_pi.updater;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemRepository implements UpdateRepository {

    private Path updatesLocation;
    private static final Logger logger = LoggerFactory.getLogger(FileSystemRepository.class);

    /**
     * @param location wo die Updatedateien herkommen
     */
    public FileSystemRepository(Path location) {
        this.updatesLocation = location;
    }

    @Override
    public List<File> filesList() {
        return Arrays.asList(updatesLocation.toFile()
                                            .listFiles());
    }

    @Override
    public int downloadFiles(List<File> neededList, Path path) {
        int anzahl = 0;
        if (path.toFile()
                .isDirectory()) {
            for (File file : neededList) {

                try {
                    Files.copy(file.toPath(), Paths.get(path.toString(), file.getName()),
                            StandardCopyOption.REPLACE_EXISTING);
                    anzahl++;
                } catch (IOException e) {
                    logger.error("bad things happen here", e);
                }

            }
        } else {
            logger.info(path.toString() + " ist kein Verzeichnis");
        }

        return anzahl;
    }

}
