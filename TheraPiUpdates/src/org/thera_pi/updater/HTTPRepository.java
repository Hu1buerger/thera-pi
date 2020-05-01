package org.thera_pi.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPRepository implements UpdateRepository {

    Logger logger = LoggerFactory.getLogger(HTTPRepository.class);
    private URL url;

    public HTTPRepository(URL url) {
        this.url = url;
    }

    public HTTPRepository() {
        try {
            url = new URL("https://www.thera-pi-software.de/Updates/");
        } catch (MalformedURLException e) {
           //cannot happen
        }
    }

    @Override
    public List<File> filesList() {
        List<File> files = new LinkedList<>();
        try {

            URLConnection therapiCon = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(therapiCon.getInputStream()));
            String inputLine;
            List<String> response = new LinkedList<>();
            while ((inputLine = in.readLine()) != null)
                response.add(inputLine);
            in.close();
            files = extractFilesFromResponse(response);
        } catch (IOException e) {
            logger.error("could not connect to update site", e);
        }
        return files;
    }

    List<File> extractFilesFromResponse(List<String> list) {


        List<File> result = list.stream()
                                .filter(s -> s.contains("TheraPi_"))
                                .map(s->s.substring(s.indexOf("TheraPi"),(s.contains("exe")?s.indexOf("exe"):s.indexOf("zip"))+3))
                                .map(s -> new File(s))
                                .collect(Collectors.toList());
        return result;

    }

    @Override
    public int downloadFiles(List<File> neededList, Path path) {
        // TODO Auto-generated method stub
        return 0;
    }

}
