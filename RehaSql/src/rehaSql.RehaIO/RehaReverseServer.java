package rehaSql.RehaIO;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JOptionPane;

import org.jdesktop.swingworker.SwingWorker;

import io.RehaIOMessages;
import rehaSql.RehaSql;

public class RehaReverseServer extends SwingWorker<Void, Void> {
    public ServerSocket serv = null;
    private final StringBuffer sb = new StringBuffer();

    public RehaReverseServer(int x) {
        RehaSql.setXport(x);
        execute();
    }

    private void doReha(String op) {
        if (op.split("#")[1].equals(RehaIOMessages.MUST_GOTOFRONT)) {
            RehaSql.thisFrame.setVisible(true);
        }

    }

    @Override
    protected Void doInBackground() throws Exception {

        while (RehaSql.getXport() < 7050) {
            try {
                serv = new ServerSocket(RehaSql.getXport());
                break;
            } catch (Exception e) {

                if (serv != null) {
                    try {
                        serv.close();
                    } catch (IOException e1) {

                        e1.printStackTrace();
                    }
                    serv = null;
                }
                RehaSql.setXport(RehaSql.getXport() + 1);
            }
        }
        if (RehaSql.getXport() == 7050) {
            JOptionPane.showMessageDialog(null, "Fehler bei der Initialisierung des IO-Servers (kein Port frei!)");
            RehaSql.setXport(-1);
            serv = null;
            return null;
        }
        Socket client;

        while (true) {
            try {
                client = serv.accept();
            } catch (SocketException se) {

                return null;
            }
            sb.setLength(0);
            sb.trimToSize();
            InputStream input = client.getInputStream();

            int byteStream;

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

//			return null;

    }

}
