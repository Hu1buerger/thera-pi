package org.thera_pi.nebraska.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.jdesktop.swingx.JXPanel;
import org.thera_pi.nebraska.crypto.NebraskaCryptoException;
import org.thera_pi.nebraska.crypto.NebraskaFileException;
import org.thera_pi.nebraska.gui.utils.ButtonTools;
import org.thera_pi.nebraska.gui.utils.FileStatics;
import org.thera_pi.nebraska.gui.utils.JCompTools;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.JRtaTextField;

public class NebraskaToolPanel extends JXPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 6898372268192195180L;
    NebraskaJTabbedPaneOrganizer eltern = null;
    JXPanel content = null;
    JScrollPane jscr = null;
    CommonTools.JRtaTextField[] tf = { null, null, null, null, null, null, null, null };
    JButton[] buts = { null, null, null, null, null };
    ActionListener al = null;
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    JLabel dateilabel = null;
    String annahmekey;
    private String RECEIVER_KEY_FILE_2048 = "annahme-sha256.key";
    private String RECEIVER_KEY_FILE_4096 = "annahme-rsa4096.key";
    private String receiverKeyFile2use = RECEIVER_KEY_FILE_2048;

    public NebraskaToolPanel(NebraskaJTabbedPaneOrganizer xeltern) {
        super();
        eltern = xeltern;
        activateListener();
        setOpaque(false);
        JPanel jpan = new JPanel(new BorderLayout());
        jpan.setOpaque(false);
        jpan.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        FormLayout lay1 = new FormLayout("10dlu,fill:0:grow,10dlu",
                "10dlu,p,10dlu,p,10dlu,p,10dlu,p,10dlu,p,10dlu,p,10dlu,p,10dlu,p,5dlu");
        CellConstraints c1 = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(lay1);
        pb.getPanel()
          .setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pb.addSeparator("Zertifikate nach Gültikeitsdatum löschen (gültig bis)", c1.xyw(2, 2, 2));
        pb.add(getAbschnitt1(), c1.xyw(2, 4, 2));
        pb.addSeparator("Neue Zertifikate der Datenannahmestellen einlesen (Nebraska wählt den Dateinamen passend zur Länge des eigenen Schlüssels)",
                c1.xyw(2, 6, 2));
        pb.add(getAbschnitt2(), c1.xyw(2, 8, 2));

        pb.getPanel()
          .validate();
        jscr = JCompTools.getTransparentScrollPane(pb.getPanel());
        jscr.getVerticalScrollBar()
            .setUnitIncrement(15);
        jscr.validate();
        jpan.add(jscr, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(jpan, BorderLayout.CENTER);
        // add(getButtonPanel(),BorderLayout.SOUTH);

        validate();

    }

    /********** Zertifikate nach Datum löschen **************/
    private JPanel getAbschnitt1() {
        FormLayout lay2 = new FormLayout("62dlu,95dlu,5dlu,100dlu,20dlu,95dlu,5dlu,100dlu",
                "p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p");
        // FormLayout lay2 = new
        // FormLayout("62dlu,right:max(30dlu;p),5dlu,100dlu,90dlu,right:max(30;p),5dlu,100dlu","p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p");
        PanelBuilder a1 = new PanelBuilder(lay2);
        a1.getPanel()
          .setOpaque(false);
        CellConstraints c2 = new CellConstraints();
        JLabel lbl0 = new JLabel("lösche mit Datum gültig bis");
        a1.add(lbl0, c2.xy(2, 1));
        tf[0] = new JRtaTextField("DATUM", true);
        tf[0].setName("gueltigbis");
        a1.add(tf[0], c2.xy(4, 1, CellConstraints.DEFAULT, CellConstraints.FILL));

        buts[0] = ButtonTools.macheBut("Zertifikate löschen", "delcertsbydate", al);
        a1.add(buts[0], c2.xy(8, 1));

        a1.getPanel()
          .validate();
        return a1.getPanel();
    }

    /********** Neue Datenannahmestellen einlesen **************/
    private JPanel getAbschnitt2() {
        FormLayout lay2 = new FormLayout("62dlu,95dlu,5dlu,100dlu,20dlu,95dlu,5dlu,100dlu",
                "p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p");
        // FormLayout lay2 = new
        // FormLayout("62dlu,right:max(30dlu;p),5dlu,100dlu,90dlu,right:max(30;p),5dlu,100dlu","p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p,1dlu,p");
        PanelBuilder a1 = new PanelBuilder(lay2);
        try {
            a1.getPanel()
              .setOpaque(false);
            CellConstraints c2 = new CellConstraints();
            JLabel lbl0 = new JLabel("  Button betätigen zum");
            a1.add(lbl0, c2.xy(2, 1));
            buts[1] = ButtonTools.macheBut("Datei wählen", "selectannahme", al);
            a1.add(buts[1], c2.xy(4, 1));
            dateilabel = new JLabel("Keine Datei gewählt");
            dateilabel.setForeground(Color.RED);
            a1.add(dateilabel, c2.xy(6, 1));
            buts[2] = ButtonTools.macheBut("Zertifikate einlesen", "readannahme", al);
            a1.add(buts[2], c2.xy(8, 1));
            a1.getPanel()
              .validate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return a1.getPanel();
    }

    private void activateListener() {
        al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                if (cmd.equals("delcertsbydate")) {
                    delCertByDate();
                    return;
                } else if (cmd.equals("selectannahme")) {
                    selectAnnahme();
                    return;
                } else if (cmd.equals("readannahme")) {
                    readAnnahme();
                    return;
                }

            }

        };
    }

    private boolean chkMandantSelectionOK () {
        if (eltern.zertExplorer.jcombo.getSelectedItem()
                                      .toString()
                                      .trim()
                                      .length() != 11) {
            JOptionPane.showMessageDialog(null, "Bitte wählen Sie zuerst auf der Seite Zertifikate auswerten...\n"
                    + "\neinen gültigen Mandanten (IK) aus!");
            eltern.jtb.setSelectedIndex(0);
            return false;
        }
        return true;
    }

    private void readAnnahme() {
        if (!chkMandantSelectionOK()) {
            return;
        }
        if (!this.dateilabel.getText()
                            .equals("Datei = o.k.")) {
            JOptionPane.showMessageDialog(null, "Keine Datei gewählt oder gewählte Datei ist nicht verwendbar!");
            return;
        }
        try {
            eltern.zertExplorer.keystore.importReceiverCertificates(annahmekey.replace("\\", "/"));
            JOptionPane.showMessageDialog(null, "Zertifikate wurde erfolgreich eingelesen");
            eltern.zertExplorer.doKeystoreWahl();
        } catch (NebraskaFileException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Import der Zertifikate fehlgeschlagen");
        } catch (NebraskaCryptoException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Import der Zertifikate fehlgeschlagen");
        }
    }

    private void selectAnnahme() {
        if (!chkMandantSelectionOK()) {
            return;
        }
        String ikDir = eltern.zertExplorer.jcombo.getSelectedItem()
                                                 .toString()
                                                 .substring(2);
        String location = (this.eltern.zertAntrag.therapidir + "/keystore/" + ikDir);
        location = location.replace("\\", "/");

        // Dateinamen anhand des eig. Zertifikats bestimmen
        int keyLen = eltern.zertExplorer.keystore.getOwnCertLength();
        switch (keyLen) {
        case 2048:
            receiverKeyFile2use = RECEIVER_KEY_FILE_2048;
            break;
        case 4096:
            receiverKeyFile2use = RECEIVER_KEY_FILE_4096;
            break;
        default:
            System.out.println("NebraskaToolPanel.selectAnnahme(): unknown key lenght found: " + keyLen
                    + ", use default receiverKeyFile: " + receiverKeyFile2use);
        }

        System.out.println("NebraskaToolPanel receiverKeyFile2use: " + receiverKeyFile2use);
        annahmekey = (location + "/" + receiverKeyFile2use);
        File f = new File(annahmekey);
        if (!f.exists()) {
            JOptionPane.showMessageDialog(null, "Fehler: \nDie Datei " + receiverKeyFile2use
                    + "\nmuß sich im Verzeichnis " + location + " befinden!");
            this.dateilabel.setText("Datei = nicht o.k.!");
            dateilabel.setForeground(Color.RED);
            return;
        }

        System.out.println(annahmekey);
        this.dateilabel.setText("Datei = o.k.");
        dateilabel.setForeground(Color.BLUE);
    }

    /*******************
     * Löschen von Annahmestellen nach Datum***************
     * 
     * 
     * 
     */
    private void delCertByDate() {
        if (tf[0].getText()
                 .trim()
                 .length() != 10) {
            JOptionPane.showMessageDialog(null, "Kein gültiges Datum eingegeben");
            return;
        }
        try {
            if (eltern.zertExplorer.jcombo.getSelectedItem()
                                          .toString()
                                          .trim()
                                          .length() != 11) {
                JOptionPane.showMessageDialog(null, "Wählen Sie zuerst auf der Seite Zertifikate auswerten...\n"
                        + "\neinen gültigen Mandanten (IK) aus");
                eltern.jtb.setSelectedIndex(0);
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Wählen Sie zuerst auf der Seite Zertifikate auswerten...\n"
                    + "\neinen gültigen Mandanten (IK) aus");
            eltern.jtb.setSelectedIndex(0);
            return;
        }
        Vector<X509Certificate> certs = eltern.zertExplorer.keystore.getAllCerts();
        int todelete = 0;
        String[] dn = null;
        String ik;

        for (int i = 0; i < certs.size(); i++) {
            if (sdf.format(certs.get(i)
                                .getNotAfter())
                   .equals(tf[0].getText())) {
                todelete++;
            }
        }
        if (todelete == 0) {
            JOptionPane.showMessageDialog(null, "Keine Zertifikate mit diesen Gültigkeitsdatum vorhanden");
            return;
        }
        int antwort = JOptionPane.showConfirmDialog(null,
                "<html>Mit Datum <b>" + tf[0].getText() + "</b><br>" + "existieren insgesamt <b>"
                        + Integer.toString(todelete) + " Zertifikat(e)</b><br><br>"
                        + "Sollen diese Zertifikate wirklich löschen ???",
                "Achtung extrem wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
        int geloescht = 0;
        if (antwort == JOptionPane.YES_OPTION) {
            for (int i = 0; i < certs.size(); i++) {
                if (sdf.format(certs.get(i)
                                    .getNotAfter())
                       .equals(tf[0].getText())) {
                    dn = certs.get(i)
                              .getSubjectDN()
                              .toString()
                              .split(",");
                    if (dn.length == 5) {
                        ik = dn[3].split("=")[1];
                        if (eltern.zertExplorer.keystore.deleteCertByAlias(ik.substring(2))) {
                            geloescht++;
                        }
                    }

                }
            }
            if (geloescht > 0) {
                eltern.zertExplorer.keystore.keystoreSichern();
                eltern.zertExplorer.doKeystoreWahl();
                if (geloescht != todelete) {
                    JOptionPane.showMessageDialog(null,
                            "Achtung!\n" + "Von " + Integer.toString(todelete) + " Zertifikaten mit passendem Datum,\n"
                                    + "konnten lediglich " + Integer.toString(geloescht) + " gelöscht werden");
                    return;
                } else {

                    JOptionPane.showMessageDialog(null, "Löschen der Zertifikate wurde erfolgreich druchgeführt");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Löschen der Zertifikate fehlgeschlagen!");
            }
        }
        System.out.println("Gefunden = " + todelete);
        System.out.println("tatsächlich gelöscht = " + geloescht);
    }
    /*******************
     * E N D E Löschen von Annahmestellen nach Datum***************
     * 
     * 
     * 
     */

}
