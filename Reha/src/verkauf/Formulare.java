package verkauf;

import java.awt.Point;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.jdesktop.swingworker.SwingWorker;

import CommonTools.JRtaTextField;
import CommonTools.ini.Settings;
import hauptFenster.Reha;
import krankenKasse.KassenFormulare;

public class Formulare {

    Vector<String> fListe = new Vector<String>();
    Vector<String> formular = new Vector<String>();
    private boolean listIsValid = false;
    private JRtaTextField formularid = new JRtaTextField("NIX", false);
    private KassenFormulare kf = null;
    Settings myInif = null;
    private String nbOfEntriesLabel = "FormulareAnzahl";
    private String entryTextLabel = "FormularText";
    private String entryNameLabel = "FormularName";
    private String sectionLabel =  "Formulare";



    public Formulare() {
    }

    public void holeFormulare() {
        if (myInif != null) {
            holeFormulare(myInif);
        }
    }

    public void holeFormulare(Settings inif) {
        final Settings tmpInif = inif;
        listIsValid = false;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                int anzForms = 0;
                if (tmpInif.getStringProperty(sectionLabel, nbOfEntriesLabel) != null) { // erst prüfen,ob überhaupt
                                                                                         // Einträge vorhanden sind
                    anzForms = tmpInif.getIntegerProperty(sectionLabel, nbOfEntriesLabel);
                    for (int i = 1; i <= anzForms; i++) {
                        fListe.add(tmpInif.getStringProperty(sectionLabel, entryTextLabel + i));
                        formular.add(tmpInif.getStringProperty(sectionLabel, entryNameLabel + i));
                    }
                }
                listIsValid = true;
                return null;
            }

        }.execute();

    }



    public void setLabels(String sectionLabel, String nbOfEntriesLabel, String entryTextLabel, String entryNameLabel) {
        this.nbOfEntriesLabel = nbOfEntriesLabel;
        this.entryNameLabel = entryNameLabel;
        this.entryTextLabel = entryTextLabel;
        this.sectionLabel = sectionLabel;
    }

    public void setLabels(String sectionLabel, String nbOfEntriesLabel, String entryBase) {
        setLabels(sectionLabel, nbOfEntriesLabel, entryBase + "Text", entryBase + "Name");
    }

    public Vector<String> getListe() {
        if (!listIsValid) {
            long zeit = System.currentTimeMillis();
            while (!listIsValid) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (System.currentTimeMillis() - zeit > 5000) {
                    JOptionPane.showMessageDialog(null, "Formulare einlesen abgebrochen", "Error:",
                            JOptionPane.WARNING_MESSAGE);
                    break;
                }
            }
        }
        return fListe;
    }

    public String getFormular(int listEntry) {
        if (listEntry < 0) {
            return null;
        } else {
            return formular.get(listEntry);
        }
    }

    public void makeDialog() {
        kf = new KassenFormulare(Reha.getThisFrame(), getListe(), formularid);
    }

    public int showDialog(Point pt) {
        kf.setLocation(pt.x - 100, pt.y + 32);
        kf.setModal(true);
        kf.setVisible(true);
        if (!formularid.getText()
                       .equals("")) {
            return (Integer.valueOf(formularid.getText()));
        }
        return -1;
    }

}
