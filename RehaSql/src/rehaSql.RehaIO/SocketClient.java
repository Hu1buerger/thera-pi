package rehaSql.RehaIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketClient {
    private String nachricht = "";
    private int port = -1;

    public void setzeRehaNachricht(int xport, String xnachricht) {
        this.nachricht = xnachricht;
        this.port = xport;
        run();
    }

    private void run() {
        serverStarten();
    }

    private void serverStarten() {
        try {
            Socket client = new Socket("localhost", this.port);
            OutputStream output = client.getOutputStream();
            InputStream input = client.getInputStream();

            byte[] bytes = this.nachricht.getBytes();

            output.write(bytes);
            output.flush();
            int zahl = input.available();
            if (zahl > 0) {
                byte[] lesen = new byte[zahl];
                input.read(lesen);
            }

            client.close();
            input.close();
            output.close();
        } catch (NullPointerException | IOException ex) {
            ex.printStackTrace();
        }
    }
}
