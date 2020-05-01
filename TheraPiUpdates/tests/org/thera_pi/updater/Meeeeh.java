package org.thera_pi.updater;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Meeeeh {

    private static Logger logger = LoggerFactory.getLogger(Meeeeh.class);

    public static void main(String[] args) {
        String htttpaddr = "https://www.thera-pi-software.de/Updates/TheraPiCommon.jar";

        try {
            URL url = new URL(htttpaddr);
            URLConnection connection = url.openConnection();
            Instant instant = Instant.ofEpochMilli(connection.getLastModified());
            System.out.println(LocalDateTime.ofInstant(instant, TimeZone.getDefault()
                                                                        .toZoneId()));
            System.out.println(connection.getHeaderFields());
            System.out.println(url.getFile());
            File fout = new File(
                    environment.Path.Instance.getProghome() + new File(url.getFile()).getName() + LocalDate.now()
                                                                                                           .toString());
            java.nio.file.Files.copy(url.openStream(), fout.toPath(), StandardCopyOption.REPLACE_EXISTING);
            fout.setLastModified(connection.getLastModified());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
