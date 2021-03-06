package fahrdienstExporter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Vector;

import com.hexiong.jdbf.DBFWriter;
import com.hexiong.jdbf.JDBFException;
import com.hexiong.jdbf.JDBField;

import CommonTools.DatFunk;
import CommonTools.ini.INIFile;
import CommonTools.ini.Settings;
import logging.Logging;

public class FahrdienstExporter {

    /**
     * @param args
     */
    public static String fertigdatei;
    public Vector<Object[]> termine = new Vector<Object[]>();
    Vector<String> dummy = new Vector<String>();

    public static void main(String[] args) {
        new Logging("fahrdienst");
        FahrdienstExporter application = new FahrdienstExporter();
        application.getInstance();

        String rohdatei;
        String datum = LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String fertigDateiName = "FPSort" + datum + ".dbf";
        if (args.length > 0) {
            Settings file = new INIFile(args[0]);
            rohdatei = file.getStringProperty("Verzeichnisse", "Fahrdienstrohdatei") + "FPSort.txt";
            fertigdatei = file.getStringProperty("Verzeichnisse", "Fahrdienstliste") + fertigDateiName;
        } else {
            rohdatei = "C:\\FPSort.txt";
            fertigdatei = "C:\\" + fertigDateiName;
        }

        application.getInstance()
                   .einlesen(rohdatei);
        application.getInstance()
                   .dbfProduce();
        System.out.println("Export beendet!");
        System.exit(0);

    }

    public FahrdienstExporter getInstance() {
        return this;
    }

    public void einlesen(String datei) {
        try {
            FileReader reader = new FileReader(datei);
            BufferedReader in = new BufferedReader(reader);
            String zeile = null;
            while ((zeile = in.readLine()) != null) {
                if (zeile.length() > 5)
                    macheVector(zeile);
            }
            in.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void macheVector(String zeile) {
        Object[] obj = { null, null, null, null, null, null };
        String[] szeile = zeile.split("°");
        if (szeile.length > 0) {
            for (int i = 0; i < szeile.length; i++) {
                if (i == 0) {
                    obj[i] = new Date(DatFunk.DatumsWert(DatFunk.sDatInDeutsch(szeile[i])));
                } else {
                    // obj[i] = szeile[i];
                    try {
                        obj[i] = new String(szeile[i].getBytes(), "ISO-8859-1");
                    } catch (UnsupportedEncodingException e) {

                        e.printStackTrace();
                    }
                }
            }
            termine.add(obj.clone());
        }
    }

    private void dbfProduce() {
        try {

            JDBField[] fields = { new JDBField("DATUM", 'D', 8, 0), new JDBField("UHRZEIT", 'C', 5, 0),
                    new JDBField("BEHANDLER", 'C', 25, 0), new JDBField("PAT_NAME", 'C', 80, 0),
                    new JDBField("PAT_NUMMER", 'C', 25, 0), new JDBField("DAUER", 'C', 3, 0) };
            DBFWriter dbfwriter = new DBFWriter(fertigdatei, fields);
            for (int i = 0; i < termine.size(); i++) {
                dbfwriter.addRecord(termine.get(i));
            }
            dbfwriter.close();

        } catch (JDBFException e) {
            e.printStackTrace();
        }

    }
}
