package speichern;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import hmv.HMVnr;
import hmv.Hmv;

public class HMVFiles implements HmvSaver {

    @Override
    public boolean save(Hmv hmv) {
        try {
            serialize(filenameOf(hmv), hmv.toString());

            System.out.println(deSerialize(filenameOf(hmv)));
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String filenameOf(Hmv hmv) {
        hmv.nummer = new HMVnr(hmv.disziplin, 123);
        return hmv.nummer.diszi.toString() + hmv.nummer.ziffern;
    }

    public static void serialize(String outFile, Object serializableObject) throws IOException {
        FileOutputStream fos = new FileOutputStream(outFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(serializableObject);
        oos.flush();
        oos.close();
    }

    public static Object deSerialize(String serilizedObject)
            throws FileNotFoundException, IOException, ClassNotFoundException {

        FileInputStream fis = new FileInputStream(serilizedObject);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object readObject = ois.readObject();
        ois.close();
        return readObject;
    }
}
