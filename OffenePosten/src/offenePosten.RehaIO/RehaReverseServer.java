package offenePosten.RehaIO;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jdesktop.swingworker.SwingWorker;

import io.RehaIOMessages;

public class RehaReverseServer extends SwingWorker<Void, Void> {
    public ServerSocket serv = null;
    StringBuffer sb = new StringBuffer();
    InputStream input = null;

    private int xport;
    private JFrame opFrame;


    public RehaReverseServer(int x,JFrame opFrame) {
        xport = x;
        this.opFrame = opFrame;
        execute();
    }

    public String getPort() {
        return String.valueOf(serv.getLocalPort());
    }

    private void doReha(String op) {
        if (op.split("#")[1].equals(RehaIOMessages.MUST_GOTOFRONT)) {
            opFrame.setVisible(true);
        }
    }

    @Override
    protected Void doInBackground() throws Exception {

        while (xport < 7050) {
            try {
                serv = new ServerSocket(xport);
                break;
            } catch (Exception e) {
                // System.out.println("In Exception währen der Portsuche - 1");
                if (serv != null) {
                    try {
                        serv.close();
                    } catch (IOException e1) {
                        // System.out.println("In Exception währen der Portsuche - 2");
                        e1.printStackTrace();
                    }
                    serv = null;
                }
                xport++;
            }
        }
        if (xport == 7050) {
            JOptionPane.showMessageDialog(null, "Fehler bei der Initialisierung des IO-Servers (kein Port frei!)");
            xport = -1;
            serv = null;
            return null;
        }
        Socket client = null;

        while (true) {
            try {
                client = serv.accept();
            } catch (SocketException se) {
                // se.printStackTrace();
                return null;
            }
            sb.setLength(0);
            sb.trimToSize();
            input = client.getInputStream();
            // output = client.getOutputStream();
            int byteStream;
            // String test = "";
            try {
                while ((byteStream = input.read()) > -1) {
                    char b = (char) byteStream;
                    sb.append(b);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("In Exception währen der while input.read()-Schleife");
            }
            /***************************/

            if (sb.toString()
                  .startsWith("Reha#")) {
                doReha(sb.toString());
            }
        }


    }



}
