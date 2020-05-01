package org.thera_pi.updater;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class VersionsSieb implements DateiSieb {

    private Version version;

    public VersionsSieb(Version currentVersion) {
        this.version = currentVersion;
    }

    @Override
    public List<File> select(List<File> filesList) {

        Predicate<? super File> predicate = new Predicate<File>() {

            @Override
            public boolean test(File f) {
                return UpdateFile.ful.matcher(f.getName()
                                               .toLowerCase())
                                     .find()
                        || UpdateFile.inc.matcher(f.getName()
                                                   .toLowerCase())
                                         .find();

            }
        };
        Map<Boolean, List<UpdateFile>> result = filesList.stream()
                                                         .filter(predicate)

                                                         .map(UpdateFile::new)
                                                         .collect(Collectors.<UpdateFile>partitioningBy(
                                                                 uf -> uf.isFull()));

        UpdateFile biggestFull = result.get(true)
                                       .stream()
                                       .max(Comparator.comparing(uf -> uf.to))
                                       .get();

        List<File> resultList= new LinkedList<>();
        if (biggestFull.to.compareTo(version) > 0) {

            UpdateFile resultFile = result.get(false)
                                          .stream()
                                          .filter(uf -> uf.from.equals(version))
                                          .findAny()
                                          .orElse(biggestFull);
            resultList.add(resultFile.file);
        }
        return resultList;
    }

}
