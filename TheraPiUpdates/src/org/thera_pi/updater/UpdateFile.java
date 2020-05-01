package org.thera_pi.updater;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateFile {
    final File file;
    Version from;
    Version to;
    private boolean full;
    public boolean isFull() {
        return full;
    }

    public static final Pattern inc = Pattern.compile("therapi_([\\d]+_[\\d]+_[\\d]+)_([\\d]+_[\\d]+_[\\d]+).[zip|exe]");
    public static final Pattern ful = Pattern.compile("therapi_([\\d]+_[\\d]+_[\\d]+).[zip|exe]");

    public UpdateFile(File file) {
        this.file = file;
        extractVersions();

    }

    void extractVersions() {

        Matcher incremental = inc.matcher(file.getName().toLowerCase());
        Matcher full = ful.matcher(file.getName().toLowerCase());
        if (incremental.find()) {

            String[] fromsplit = incremental.group(1)
                                            .split("_");
            from = new Version(Integer.parseInt(fromsplit[0]), Integer.parseInt(fromsplit[1]),
                    Integer.parseInt(fromsplit[2]));
            String[] toSplit = incremental.group(2)
                                          .split("_");
            to = new Version(Integer.parseInt(toSplit[0]), Integer.parseInt(toSplit[1]), Integer.parseInt(toSplit[2]));
        } else if (full.find()) {
            this.full = true;
            String[] toSplit = full.group(1)
                                          .split("_");
            from = new Version(1,0,0);
            to = new Version(Integer.parseInt(toSplit[0]), Integer.parseInt(toSplit[1]), Integer.parseInt(toSplit[2]));
        }

        else {
            throw new IllegalArgumentException(file.getName());
        }
    }

    @Override
    public String toString() {
        return "UpdateFile [file=" + file + ", from=" + from + ", to=" + to + "]";
    }


}
