package update;

import java.io.OutputStream;
import java.io.PrintWriter;

class NullWriter extends PrintWriter {

    private static OutputStream outstream = new OutputStream()     {         @Override         public void write(int b)         {         }     };;

    public NullWriter() {
        super(outstream);
    }

}
