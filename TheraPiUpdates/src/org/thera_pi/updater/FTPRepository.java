package org.thera_pi.updater;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thera_pi.updates.UpdateConfig;

public class FTPRepository implements UpdateRepository {
    FTPClient ftpClient;
    UpdateConfig updateConfig = new UpdateConfig();
    private static final Logger logger = LoggerFactory.getLogger(FTPRepository.class);

    public FTPRepository() throws SocketException, IOException {
        ftpClient = new FTPClient();

    }

    private void connect() throws SocketException, IOException {
        ftpClient.connect(updateConfig.getUpdateHost());

        ftpClient.login(updateConfig.getUpdateUser(), updateConfig.getUpdatePasswd());

        ftpClient.changeWorkingDirectory("." + updateConfig.getUpdateDir());
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();
    }

    @Override
    public List<File> filesList() {
        List<File> files = new LinkedList<>();
        try {
            connect();
            FTPFile[] ftpfiles = ftpClient.listFiles();

            for (FTPFile ftpFile : ftpfiles) {
                File remote = new File(ftpFile.getName());
                files.add(remote);
                remote.setLastModified(ftpFile.getTimestamp()
                                              .getTimeInMillis());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("bad things happen here", e);
        }

        return files;
    }

    @Override
    public int downloadFiles(List<File> neededList, Path path) {
        int count = 0;
        for (File file : neededList) {
            System.out.println(file.getName());
            count++;
        }
        return count;
    }

}
