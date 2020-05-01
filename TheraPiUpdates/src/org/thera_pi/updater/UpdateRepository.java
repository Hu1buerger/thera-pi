package org.thera_pi.updater;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface UpdateRepository {

    List<File> filesList();

    int downloadFiles(List<File> neededList, Path path);

}
