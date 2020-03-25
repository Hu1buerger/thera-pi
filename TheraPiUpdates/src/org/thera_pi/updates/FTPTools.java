package org.thera_pi.updates;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.swing.*;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FTPTools {
    private static final Logger LOG = LoggerFactory.getLogger(FTPTools.class);

    private FTPClient ftpClient = null;
    private static final int BUFFER_SIZE = 1024 * 8;
    private FTPFile[] files;
    private UpdateConfig updateConfig;

    FTPTools() {
        try {
            updateConfig = UpdateConfig.getInstance();
            ftpClient = new FTPClient();
        } catch (Exception ex) {
            LOG.error("Exception: ", ex);
        }
    }

    FTPFile[] holeDatNamen() {
        try {
            if (files != null) {
                return files;
            }
            ftpClient.connect(updateConfig.getUpdateHost());

            ftpClient.login(updateConfig.getUpdateUser(), updateConfig.getUpdatePasswd());

            ftpClient.changeWorkingDirectory("." + updateConfig.getUpdateDir());

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);

            if (updateConfig.isUseActiveMode()) {
                ftpClient.enterLocalActiveMode();
            } else {
                ftpClient.enterLocalPassiveMode();
            }

            files = ftpClient.listFiles();

        } catch (IOException ex) {
            LOG.error("Exception: ", ex);
        }
        return files;
    }

    boolean holeDatei(String datfern, String vznah, boolean doprogress, final UpdatePanel eltern, long groesse) {
        try {
            if (ftpClient == null) {
                return false;
            }
            if (!ftpClient.isConnected()) {
                if (!nurConnect()) {
                    return false;
                }

            }

            if (updateConfig.isUseActiveMode()) {
                ftpClient.enterLocalActiveMode();
            } else {
                ftpClient.enterLocalPassiveMode();
            }

            ftpClient.setUseEPSVwithIPv4(true);

            try (InputStream uis = ftpClient.retrieveFileStream(datfern);
                    FileOutputStream fos = new FileOutputStream(vznah + /* "test/"+ */datfern)) {
                if (files == null) {
                    files = ftpClient.listFiles();
                } else {
                    LOG.debug("files bereits eingelesen");
                }

                // Untersuchen ob Datei vorhanden

                long max = -1;
                if (groesse < 0) {
                    for (FTPFile file : files) {
                        if (file.getName()
                                .equals(datfern)) {
                            max = file.getSize();
                            LOG.debug(file.getName());
                        }
                    }
                } else {
                    max = groesse;
                }
                if (max < 0) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                    return false;
                }

                ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);

                int n;
                byte[] buf = new byte[BUFFER_SIZE];

                if (doprogress) {
                    eltern.pbar.setMinimum(0);
                    eltern.pbar.setMaximum(Integer.parseInt(Long.toString(max)));
                }

                while ((n = uis.read(buf, 0, buf.length)) > 0) {
                    fos.write(buf, 0, n);
                    if (doprogress) {
                        final int xgesamt = n;
                        SwingUtilities.invokeLater(() -> eltern.pbar.setValue(xgesamt));

                    }
                }
                if (doprogress) {
                    SwingUtilities.invokeLater(eltern::setDoneIcon);
                }

                LOG.debug("Datei {} wurde erfolgreich übertragen", datfern);
                fos.flush();
            } catch (IOException ex) {
                String message = "Bezug der Datei " + datfern
                        + " fehlgeschlagen!\nBitte starten Sie einen neuen Versuch";
                JOptionPane.showMessageDialog(null, message);
                LOG.error(message, ex);
                return false;
            }
        } catch (Exception ex) {
            String message = "Bezug der Datei " + datfern + " fehlgeschlagen!\nBitte starten Sie einen neuen Versuch";
            JOptionPane.showMessageDialog(null, message);
            LOG.error(message, ex);
            return false;
        }

        return true;
    }

    /*****************************************************/
    String holeLogDateiSilent(String datfern) {
        try {
            if (ftpClient == null) {
                return "Fehler beim Bezug der Log-Datei, ftpClient == null";
            }
            if (!ftpClient.isConnected()) {
                if (!nurConnect()) {
                    return "Fehler beim Bezug der Log-Datei, ftpClient == nicht connected\nBitte starten Sie einen neuen Versuch";
                }
            }
            if (updateConfig.isUseActiveMode()) {
                ftpClient.enterLocalActiveMode();
            } else {
                ftpClient.enterLocalPassiveMode();
            }
            ftpClient.setUseEPSVwithIPv4(true);
            ftpClient.getReplyStrings();

            try (InputStream uis = ftpClient.retrieveFileStream(datfern)) {
                String ret = convertStreamToString(uis);
                ftpClient.getReplyStrings();
                ftpClient.logout();
                ftpClient.disconnect();
                return ret;
            } catch (Exception ex) {
                LOG.error("Exception: ", ex);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler beim Bezug der Updatebeschreibung, bitte versuchen Sie es erneut");
            LOG.error("Fehler beim Bezug der Updatebeschreibung, bitte versuchen Sie es erneut", ex);
        }
        return "Fehler beim Bezug der Log-Datei";
    }

    void ftpTransferString(String datfern, String string, JProgressBar jprog) {
        if (ftpClient == null) {
            LOG.debug("ftpClient = null");
            return;
        }
        if (!ftpClient.isConnected() && !nurConnect()) {
            return;
        }

        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (IOException ex) {
            LOG.error("Exception: ", ex);
        }

        if (updateConfig.isUseActiveMode()) {
            ftpClient.enterLocalActiveMode();
        } else {
            ftpClient.enterLocalPassiveMode();
        }

        try (InputStream ins = convertStringToStream(string); OutputStream fos = ftpClient.storeFileStream(datfern)) {
            ftpClient.deleteFile(datfern);

            if (updateConfig.isUseActiveMode()) {
                ftpClient.enterLocalActiveMode();
            } else {
                ftpClient.enterLocalPassiveMode();
            }

            ftpClient.setSendBufferSize(1024 * 8);

            byte[] buf = new byte[1024 * 8];

            int gesamt = 0;

            boolean progresszeigen = false;
            final JProgressBar xprog = jprog;
            final int xgross = string.length();
            if (jprog != null) {
                SwingUtilities.invokeLater(() -> {
                    xprog.setStringPainted(true);
                    xprog.setMinimum(0);
                    xprog.setMaximum(xgross);
                    xprog.repaint();
                });
                progresszeigen = true;
            }

            int n;
            while ((n = ins.read(buf, 0, buf.length)) > 0) {
                try {
                    gesamt += n;
                    fos.write(buf, 0, n);

                    if (progresszeigen) {
                        final int xgesamt = gesamt;
                        SwingUtilities.invokeLater(() -> {
                            xprog.setValue(xgesamt);
                            xprog.repaint();
                        });
                    }

                } catch (Exception ex) {
                    LOG.error("Exception: ", ex);
                }

            }
            LOG.debug("Datei {} auf Server geschrieben mit {} Bytes", datfern, gesamt);

            fos.flush();
            if (!ftpClient.completePendingCommand()) {
                ftpClient.logout();
                ftpClient.disconnect();
                String message = "Die Puffer des belämmerten FTP's konnten nicht vollständig geschrieben werden!!!!(Ich krieg die Krise)";
                JOptionPane.showMessageDialog(null, message);
                System.err.println(message);
            }
            if (progresszeigen) {
                SwingUtilities.invokeLater(() -> {
                    xprog.setValue(0);
                    xprog.repaint();
                });
            }
        } catch (IOException ex) {
            LOG.error("Exception: ", ex);
        }
    }

    /********************************************************/

    private static String convertStreamToString(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line)
                  .append("\n");
            }
        } catch (Exception ex) {
            LOG.error("Exception: ", ex);
        }
        return sb.toString();
    }

    private static InputStream convertStringToStream(String string) {
        return new ByteArrayInputStream(string.getBytes());
    }

    private boolean nurConnect() {
        try {
            if (ftpClient != null && !ftpClient.isConnected()) {
                ftpClient.connect(updateConfig.getUpdateHost());

                ftpClient.login(updateConfig.getUpdateUser(), updateConfig.getUpdatePasswd());

                ftpClient.changeWorkingDirectory("." + updateConfig.getUpdateDir());
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            } else {
                if (ftpClient == null) {
                    LOG.debug("ftpClient == null");
                } else if (ftpClient.isConnected()) {
                    LOG.debug("ftpClient == bereits connected");
                }
            }

        } catch (IOException ex) {
            LOG.error("Exception: ", ex);
            return false;
        }
        return true;
    }

    void holeDateiSilent(String datfern, String vznah) {
        try {
            if (ftpClient == null) {
                return;
            }
            if (!ftpClient.isConnected() && !nurConnect()) {
                return;
            }
            if (updateConfig.isUseActiveMode()) {
                ftpClient.enterLocalActiveMode();
            } else {
                ftpClient.enterLocalPassiveMode();
            }
        } catch (Exception ex) {
            LOG.error("Exception: ", ex);
        }

        try (InputStream uis = ftpClient.retrieveFileStream(datfern);
                FileOutputStream fos = new FileOutputStream(vznah + datfern)) {

            files = ftpClient.listFiles();
            // Untersuchen ob Datei vorhanden

            ftpClient.getReplyString();
            int n;
            byte[] buf = new byte[BUFFER_SIZE];
            int gesamt = 0;

            while ((n = uis.read(buf, 0, buf.length)) > 0) {
                gesamt = gesamt + n;
                fos.write(buf, 0, n);
            }

            fos.flush();
            ftpClient.getReplyString();
            ftpClient.logout();
            ftpClient.getReplyString();
            ftpClient.disconnect();
        } catch (IOException ex) {
            LOG.error("Exception: ", ex);
        }

    }

    /********************************************************/
    void ftpTransferDatei(String datfern, String quelldat, int groesse, JProgressBar jprog) {

        if (ftpClient == null) {
            LOG.debug("ftpClient = null");
            return;
        }
        if (!ftpClient.isConnected()) {
            LOG.debug("nicht connected");
            if (!nurConnect()) {
                return;
            }
            LOG.debug("connected");

        }

        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
        } catch (IOException ex) {
            LOG.error("Exception: ", ex);
        }

        if (updateConfig.isUseActiveMode()) {
            ftpClient.enterLocalActiveMode();
        } else {
            ftpClient.enterLocalPassiveMode();
        }

        File src = new File(quelldat);
        try (InputStream ins = new FileInputStream(src); OutputStream fos = ftpClient.storeFileStream(datfern);) {
            ftpClient.deleteFile(datfern);
            if (updateConfig.isUseActiveMode()) {
                ftpClient.enterLocalActiveMode();
            } else {
                ftpClient.enterLocalPassiveMode();
            }

            ftpClient.setSendBufferSize(1024 * 8);

            if (!FTPReply.isPositiveIntermediate(ftpClient.getReplyCode())) {
                LOG.debug("Datei = {}", quelldat);
            }

            int n;
            byte[] buf = new byte[1024 * 8];

            int gesamt = 0;

            boolean progresszeigen = false;
            final JProgressBar xprog = jprog;
            final long xgross = groesse;
            if (jprog != null) {
                SwingUtilities.invokeLater(() -> {
                    xprog.setStringPainted(true);
                    xprog.setMinimum(0);
                    xprog.setMaximum((int) xgross);
                    xprog.repaint();
                });
                progresszeigen = true;
            }

            while ((n = ins.read(buf, 0, buf.length)) > 0) {
                try {
                    gesamt = gesamt + n;
                    fos.write(buf, 0, n);

                    if (progresszeigen) {
                        final int xgesamt = gesamt;
                        SwingUtilities.invokeLater(() -> {
                            xprog.setValue(xgesamt);
                            xprog.repaint();
                        });
                    }
                } catch (Exception ex) {
                    LOG.error("Exception: " + ex.getMessage(), ex);
                }

            }
            LOG.debug("Datei {} auf Server geschrieben mit {} Bytes", datfern, gesamt);

            fos.flush();
            if (!ftpClient.completePendingCommand()) {
                ftpClient.logout();
                ftpClient.disconnect();
                JOptionPane.showMessageDialog(null,
                        "Die Puffer des belämmerten FTP's konnten nicht vollständig geschrieben werden!!!!(Ich krieg die Krise)");
                System.err.println(
                        "Die Puffer des belämmerten FTP's konnten nicht vollständig geschrieben werden!!!!(Ich krieg die Krise)");
            }
            if (progresszeigen) {
                SwingUtilities.invokeLater(() -> {
                    xprog.setValue(0);
                    xprog.repaint();
                });
            }

            ftpClient.logout();
            ftpClient.disconnect();

        } catch (IOException ex) {
            LOG.error("Exception: ", ex);
        }

    }

    void connectTest() {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
            } catch (Exception ex) {
                LOG.error("Exception: ", ex);
            }
            try {
                ftpClient.disconnect();
            } catch (Exception ex) {
                LOG.error("Exception: ", ex);

            }
        }
    }
}
