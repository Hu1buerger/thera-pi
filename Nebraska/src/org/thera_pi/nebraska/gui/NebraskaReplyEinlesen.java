package org.thera_pi.nebraska.gui;

import java.awt.Color;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.security.cert.X509Certificate;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.jdesktop.swingx.JXPanel;
import org.thera_pi.nebraska.crypto.NebraskaCryptoException;
import org.thera_pi.nebraska.crypto.NebraskaFileException;
import org.thera_pi.nebraska.crypto.NebraskaKeystore;
import org.thera_pi.nebraska.crypto.NebraskaNotInitializedException;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import org.thera_pi.nebraska.gui.utils.ButtonTools;
import org.thera_pi.nebraska.gui.utils.FileStatics;

public class NebraskaReplyEinlesen extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = 4158060069360452844L;
    private ActionListener al = null;
    private JXPanel content = null;
    private JLabel keystorelab;
    private JLabel replyfile;
    private JButton[] actionbuts = { null, null };
    private JButton[] buts = { null, null };
    private NebraskaZertAntrag eltern;
    private String RECEIVER_KEY_FILE = "annahme-rsa4096.key";
    private String receiverKeyFile2use = RECEIVER_KEY_FILE;

    public NebraskaReplyEinlesen(NebraskaZertAntrag xeltern) {
        super();
        this.activateListeners();
        this.setTitle("Zert-Reqly einlesen ");
        add(getContent());
        this.setModal(true);
        this.eltern = xeltern;
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    }

    private JXPanel getContent() {
        content = new JXPanel();
        // 1 2 3 4 5 6 7
        FormLayout lay = new FormLayout("fill:0:grow(0.5),60dlu,10dlu,200dlu,10dlu,60dlu,fill:0:grow(0.5)",
                // 1 2 3 4 5 6 7 8
                "0dlu,fill:0:grow(0.5),p,15dlu,p,15dlu,p,fill:0:grow(0.5)");
        CellConstraints cc = new CellConstraints();
        content.setLayout(lay);
        JLabel lab = new JLabel("Keystore:");
        content.add(lab, cc.xy(2, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        keystorelab = new JLabel("kein Keystore ausgewählt....");
        keystorelab.setForeground(Color.BLUE);
        content.add(keystorelab, cc.xy(4, 3));
        actionbuts[0] = ButtonTools.macheBut("auswählen", "keystorewahl", al);
        content.add(actionbuts[0], cc.xy(6, 3));

        lab = new JLabel("CertificateReply:");
        content.add(lab, cc.xy(2, 5, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        replyfile = new JLabel("keine Reply-Datei ausgewählt....");
        replyfile.setForeground(Color.BLUE);
        content.add(replyfile, cc.xy(4, 5));
        actionbuts[1] = ButtonTools.macheBut("auswählen", "replywahl", al);
        content.add(actionbuts[1], cc.xy(6, 5));

        JXPanel jpan = new JXPanel();
        FormLayout lay2 = new FormLayout("fill:0:grow(0.33),60dlu,fill:0:grow(0.33),60dlu,fill:0:grow(0.33)",
                "10dlu,p,10dlu");
        CellConstraints cc2 = new CellConstraints();
        jpan.setLayout(lay2);
        buts[0] = ButtonTools.macheBut("einlesen", "einlesen", al);
        jpan.add(buts[0], cc2.xy(2, 2));
        buts[1] = ButtonTools.macheBut("abbrechen", "abbrechen", al);
        jpan.add(buts[1], cc2.xy(4, 2));
        jpan.validate();
        content.add(jpan, cc.xyw(1, 7, 7));
        content.validate();
        return content;

    }

    private void activateListeners() {
        al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String cmd = arg0.getActionCommand();
                if (cmd.equals("keystorewahl")) {
                    doKeystorewahl();
                    return;
                }
                if (cmd.equals("replywahl")) {
                    doReplywahl();
                    return;
                }
                if (cmd.equals("einlesen")) {
                    System.out.println("Rufe einlesen auf");
                    doEinlesen(keystorelab.getText()
                                          .trim(),
                            replyfile.getText()
                                     .trim());
                    return;
                }
                if (cmd.equals("abbrechen")) {
                    setVisible(false);
                    dispose();
                    return;
                }

            }
        };
    }

    private void doKeystorewahl() {
        String keystore = this.eltern.therapidir + "/keystore/" + eltern.getIK() + "/" + eltern.getIK() + ".p12";
        System.out.println(keystore);
        File f = new File(keystore);
        if (!f.exists()) {
            keystore = FileStatics.dirChooser(this.eltern.therapidir + "/keystore/", "Keystore auswählen");
            System.out.println("Keystore = " + keystore);
            if (keystore.trim()
                        .equals("")) {
                keystorelab.setText("kein Keystore ausgewählt....");
            } else {
                keystorelab.setText(keystore);
            }
        } else {
            keystorelab.setText(keystore);
        }
    }

    private void doReplywahl() {
        String ik = eltern.getIK();
        String replykurz = ik.substring(0, 8) + ".";
        String location = (this.eltern.therapidir + "/keystore/"  + ik);
        File folderToScan = new File(location); 
        String replyFileName = "";

        File[] listOfFiles = folderToScan.listFiles();
        List foundFiles = new List();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                replyFileName = listOfFiles[i].getName();
                if (replyFileName.startsWith(replykurz)         // findet sowohl 12345678.p7c
                        && replyFileName.endsWith(".p7c")) {    // als auch 12345678.654321.p7c (neu)
                    foundFiles.add(replyFileName);
                    System.out.println("Replyfile = " + replyFileName);
                }
            }
        }
        if (foundFiles.getItemCount() > 1) {
            JOptionPane.showMessageDialog(null,
                    "Fehler:  \nEs darf sich nur eine Zertifikatrequest-Antwortdatei (*.p7c) \nim Verzeichnis "
                            + location + " befinden!");
            return;
        }
        File f = new File(location + "/" + receiverKeyFile2use);
        if ((foundFiles.getItemCount() == 0) || (!f.exists())) {
            JOptionPane.showMessageDialog(null, 
                    "Fehler: \nDie Zertifikatrequest-Antwortdatei (*.p7c) und(!) die Datei " + receiverKeyFile2use
                            + "\nmüssen sich im Verzeichnis " + location + " befinden!");
            return;
        }
        replyfile.setText(location + "/" + foundFiles.getItem(0));
    }

    /*
     * @param keystoreFileName name of key store file
     * 
     * @param keystorePassword password for key store file
     * 
     * @param keyPassword password for private key
     * 
     * @param IK institution ID
     * 
     * @throws NebraskaCryptoException on cryptography related errors
     * 
     * @throws NebraskaFileException on I/O related errors
     *
     */
    private void doEinlesen(String keystorefile, String requestfile) {

        /*
         * if( keystorefile.startsWith("kein") || requestfile.trim().startsWith("kein")
         * ) { JOptionPane.showMessageDialog(null,
         * "Keystore und Reply-Datei müssen angegeben werden!!!!"); return; }
         */
        if (keystorelab.getText()
                       .toLowerCase()
                       .startsWith("keine keystore")) {
            JOptionPane.showMessageDialog(null,
                    "Oh Herr schmeiß Hirn ra....\n\nSie haben zwar keinen Keystore angegeben\nwollen aber schon mal einen ITSG-Reply einlesen?\n\nEin Tip lassen Sie das Zeugs weg das Sie einnehmen,\nwie immer es auch heißen mag!");
            setVisible(false);
            dispose();
            return;
        }
        if (replyfile.getText()
                     .toLowerCase()
                     .startsWith("keine reply-datei")) {
            JOptionPane.showMessageDialog(null,
                    "Oh Herr schmeiß Hirn ra....\n\nSie haben zwar keinen Reply der ITSG ausgewählte\nwollen diesen aber schon mal einlesen?\n\nEin Tip lassen Sie das Zeugs weg das Sie einnehmen,\nwie immer es auch heißen mag!");
            setVisible(false);
            dispose();
            return;
        }
        String kfile = keystorelab.getText()
                                  .replace("\\", "/");
        // "C:/RehaVerwaltung/keystore/540840108/540840108.p12";

        // Object ret = JOptionPane.showInputDialog(null, "Geben Sie bitte das
        // (6-stellige) Passwort für den gewählten KeyStore ein, Default-Passwort =
        // 123456", "");
        String ret = "123456";
        for (int i = 0; i < NebraskaMain.keyStoreParameter.size(); i++) {
            if (NebraskaMain.keyStoreParameter.get(i)
                                              .get(2)
                                              .trim()
                                              .equals("IK" + eltern.getIK())) {
                if (!NebraskaMain.keyStoreParameter.get(i)
                                                   .get(1)
                                                   .trim()
                                                   .equals(ret)) {
                    ret = NebraskaMain.keyStoreParameter.get(i)
                                                        .get(1)
                                                        .trim();
                    System.out.println("nehme alternatives Passwort = " + ret);
                }
            }
        }

        try {
            NebraskaZertExplorer explorer = eltern.elternTab.getZertExplorer();
            int index = explorer.jcombo.getSelectedIndex();
            NebraskaKeystore nebraskastore = new NebraskaKeystore(kfile, ret, "abc", "IK" + eltern.getIK(),
                    eltern.getInstitution(), eltern.getPerson());
            System.out.println("KeyStore initialisiert " + kfile);
            nebraskastore.deleteAllCerts();
            /*
             * int frage =
             * JOptionPane.showConfirmDialog(null,"Alle Zerts löschen und dann zurück"
             * ,"Anfrage", JOptionPane.YES_NO_OPTION); if(frage==JOptionPane.YES_OPTION){
             * return; }
             */
            Vector<X509Certificate> certs = nebraskastore.getAllCerts();
            for (int i = 0; i < certs.size(); i++) {
                System.out.println("Zert " + i + " = " + certs.get(i)
                                                              .getIssuerDN());
            }
            System.out.println("IK des Keystoreinhaber = " + nebraskastore.getIK());
            System.out.println("fertig");

            String privkeyfile = FileStatics.dirChooser(
                    this.eltern.therapidir + "/keystore/" + eltern.getIK() + "/privkeys/", "PrivateKey-File auswählen");
            if (privkeyfile != null) {
                System.out.println("Datei = " + privkeyfile.replace("\\", "/"));
            } else {
                System.out.println("Datei = null");
            }
            if (privkeyfile == null || privkeyfile.equals("")) {
                JOptionPane.showMessageDialog(null,
                        "Ohne die Datei für den privaten Schlüssel kann der Reply nicht eingelesen werden!");
                setVisible(false);
                dispose();
                return;
            }
            nebraskastore.importKeyPair(privkeyfile.replace("\\", "/"));
            /*
             * frage =
             * JOptionPane.showConfirmDialog(null,"Privaten Schlüssel generieren und zurück"
             * ,"Anfrage", JOptionPane.YES_NO_OPTION); if(frage==JOptionPane.YES_OPTION){
             * return; }
             */
            nebraskastore.importCertificateReply(requestfile);
            System.out.println("fertig mit dem Reply-Import");
            nebraskastore.importReceiverCertificates(
                    this.eltern.therapidir + "/keystore/" + eltern.getIK() + "/" + receiverKeyFile2use);
            System.out.println("fertig mit Import der Datenannahmestellen");
            JOptionPane.showMessageDialog(null,
                    "<html>Glückwunsch - die Zertifikatsdatenbank wurde erfolgreich erstellt"
                            + "<br>Viel Spaß bei der Kassenabrechnung und vor allem -> <b>gute Geschäfte</b>!!!!</html>");
            explorer.jcombo.setSelectedIndex(index);
        } catch (NebraskaCryptoException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Erstellung der Zertifikatsdatenbank - CryptoException\nAbrechnung nach § 302 nicht möglich!!");
        } catch (NebraskaFileException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Erstellung der Zertifikatsdatenbank - FileException\nAbrechnung nach § 302 nicht möglich!!");
        } catch (NebraskaNotInitializedException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Erstellung der Zertifikatsdatenbank - NotInitializedException\nAbrechnung nach § 302 nicht möglich!!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setVisible(false);
        dispose();
    }

}