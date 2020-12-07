package terminKalender;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import CommonTools.DatFunk;
import CommonTools.ZeitFunk;
import dialoge.EmailDialog;
import environment.Path;
import gui.Cursors;
import hauptFenster.Reha;
import systemEinstellungen.SystemConfig;
import umfeld.Betriebsumfeld;

public class iCalRehaExporter {

    private String plandatei = "";
    private Vector<String> veczeilen = new Vector<String>();
    Vector<String> test = new Vector<String>();
    private String inhaber = "";
    private StringBuffer buf = new StringBuffer();

    public iCalRehaExporter() {
        if ((plandatei = oeffneDateiDialog()).equals("")) {
            JOptionPane.showMessageDialog(null, "Es wurde keine Plandatei ausgewählt");
        } else {
            if ((veczeilen = plandateiEinlesen()).size() <= 0) {
                JOptionPane.showMessageDialog(null, "Die Datei " + plandatei + " ist keine gültige Rehaplan-Datei");
            } else {
                inhaber = test.get(test.size() - 1);
                // Testen ob Vor und Nachname im Dateiname enthalten sind
                // ist dies nicht der Fall FrageDialog ob trotzdem ICS produziert werden soll
                // mit Vor- Und Nachname und Dateiname im Text
                try {
                    if ((!inhaber.contains(Reha.instance.patpanel.getPatDaten().get(2)))
                            || (!inhaber.contains(Reha.instance.patpanel.getPatDaten().get(3)))) {
                        String meldung = "<html><b>Achtung!</b><br>Sie versuchen dem Patient<b> -> "
                                + Reha.instance.patpanel.getPatDaten().get(2) + ", " + Reha.instance.patpanel.getPatDaten().get(3)
                                + " <- </b>, einen Rehaplan per Email zu senden<br>mit dem Dateiname: <b>" + inhaber
                                + " </b></html>";
                        int frage = JOptionPane.showConfirmDialog(null, meldung, "Wichtige Benutzeranfrage",
                                JOptionPane.YES_NO_OPTION);
                        if (frage != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }
                    erzeugeIcs();
                    String emailaddy = Reha.instance.patpanel.getPatDaten().get(50);

                    String recipient = emailaddy + ((Boolean) SystemConfig.hmIcalSettings.get("aufeigeneemail")
                            ? "," + SystemConfig.hmEmailExtern.get("SenderAdresse")
                            : "");

                    String[] aufDat = {
                            Path.Instance.getProghome() + "temp/" + Betriebsumfeld.getAktIK() + "/iCal-RehaTermine.ics",
                            "iCal-RehaTermine.ics" };
                    ArrayList<String[]> attachments = new ArrayList<String[]>();
                    attachments.add(aufDat);
                    String mailtext = SystemConfig.hmAdrPDaten.get("<Pbanrede>")
                            + ",\nwie gewünscht senden wir Ihnen hiermit Ihre Reha-Termine im RTA\n\nMit freundlichen Grüßen\nIhr Planungsteam im RTA ";

                    Reha.getThisFrame()
                        .setCursor(Cursors.wartenCursor);
                    EmailDialog emlDlg = new EmailDialog(Reha.getThisFrame(), "Ihre Reha-Termine als ICS Datei",
                            recipient, (String) SystemConfig.hmIcalSettings.get("betreff"), mailtext, attachments,
                            (Integer) SystemConfig.hmIcalSettings.get("postfach"),
                            (Boolean) SystemConfig.hmIcalSettings.get("direktsenden"));
                    emlDlg.setPreferredSize(new Dimension(575, 370));
                    emlDlg.setLocationRelativeTo(null);
                    // emlDlg.setLocation(pt.x-350,pt.y+100);
                    emlDlg.pack();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            // emlDlg.setTextCursor(0);
                        }
                    });

                    emlDlg.setVisible(true);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            // emlDlg.setTextCursor(0);
                        }
                    });

                } catch (Exception ex) {
                    Reha.getThisFrame()
                        .setCursor(Cursors.normalCursor);
                    JOptionPane.showMessageDialog(null, "Es ist ein Fehler beim ICS-Export aufgetreten");
                }

            }
        }
    }

    private boolean erzeugeIcs() {
        // {"08.10.2015",4,40,"12:00",720,15,"Blutdruckmessung
        // 3.OG",0,{89,239,39},{0,0,0},8}
        // macheRehaVevent(String datum, String start, String end, String titel, String
        // beschreibung,boolean warnen)
        buf.setLength(0);
        buf.trimToSize();
        buf.append(ICalGenerator.macheKopf());
        String[] parts = null;
        for (int i = 0; i < veczeilen.size(); i++) {
            try {
                parts = veczeilen.get(i)
                                 .split(",");
                buf.append(ICalGenerator.macheRehaVevent(DatFunk.sDatInSQL(parts[0].replace("{", "")
                                                                                   .replace("\"", ""))
                                                                .replace("-", ""),
                        parts[3].replace("\"", "")
                                .replace(":", "") + "00",
                        ZeitFunk.ZeitPlusMinuten(parts[3].replace("\"", ""), parts[5].replace("\"", ""))
                                .replace(":", "") + "00",
                        parts[3].replace("\"", "") + "-" + parts[6].replace("\"", ""), inhaber, false));
            } catch (Exception ex) {
                return false;
            }
        }
        buf.append(ICalGenerator.macheEnd());
        FileOutputStream outputFile;
        try {
            outputFile = new FileOutputStream(
                    Path.Instance.getProghome() + "temp/" + Betriebsumfeld.getAktIK() + "/iCal-RehaTermine.ics");
            OutputStreamWriter out = new OutputStreamWriter(outputFile, "UTF8");
            BufferedWriter bw = null;
            bw = new BufferedWriter(out);
            bw.write(buf.toString());
            bw.flush();
            bw.close();
            out.close();
            outputFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Vector<String> plandateiEinlesen() {
        Vector<String> vecz = new Vector<String>();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(plandatei));
            String zeile = null;
            while ((zeile = in.readLine()) != null) {
                if (zeile.startsWith("{\"")) {
                    vecz.add(zeile);
                }
                test.add(zeile);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        /*
         * System.out.println("Elemente = "+vecz.size()); for(int i = 0; i <
         * vecz.size();i++){ System.out.println("Element "+i+" = "+vecz.get(i)); }
         */
        return (Vector<String>) vecz.clone();
    }

    private String oeffneDateiDialog() {
        String sret = "";
        final JFileChooser chooser = new JFileChooser("Verzeichnis wählen");
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        final File file = new File((String) SystemConfig.hmIcalSettings.get("rehaplanverzeichnis"));

        chooser.setCurrentDirectory(file);

        chooser.setVisible(true);
        Reha.getThisFrame()
            .setCursor(Cursors.normalCursor);
        final int result = chooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File inputVerzFile = chooser.getSelectedFile();

            if (inputVerzFile.getName()
                             .trim()
                             .equals("")) {
                sret = "";
            } else {
                sret = inputVerzFile.getPath()
                                    .trim()
                                    .replace("\\", "/");
            }
        } else {
            sret = ""; // vorlagenname.setText(SystemConfig.oTerminListe.NameTemplate);
        }
        chooser.setVisible(false);

        return sret;
    }

}
