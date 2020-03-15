package org.thera_pi.nebraska.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.pkcs.CertificationRequestInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.JXPanel;
import org.thera_pi.nebraska.crypto.NebraskaConstants;
import org.thera_pi.nebraska.crypto.NebraskaCryptoException;
import org.thera_pi.nebraska.crypto.NebraskaFileException;
import org.thera_pi.nebraska.crypto.NebraskaKeystore;
import org.thera_pi.nebraska.crypto.NebraskaNotInitializedException;
import org.thera_pi.nebraska.crypto.NebraskaUtil;
import org.thera_pi.nebraska.gui.utils.BCStatics2;
import org.thera_pi.nebraska.gui.utils.ButtonTools;
import org.thera_pi.nebraska.gui.utils.FileStatics;
import org.thera_pi.nebraska.gui.utils.JRtaComboBox;
import org.thera_pi.nebraska.gui.utils.MultiLineLabel;
import org.thera_pi.nebraska.gui.utils.OOorgTools;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.DatFunk;
import CommonTools.INIFile;
import CommonTools.JRtaTextField;
import crypt.Verschluesseln;

public class NebraskaRequestDlg extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 6958116962234351720L;

    private JRtaTextField pw = null;
    private JButton[] buts = { null, null, null, null };
    private ActionListener al = null;
    private NebraskaZertAntrag zertantrag = null;
    private JXPanel content = null;
    private JLabel[] labs = { null, null, null };
    private MultiLineLabel[] mlabs = { null, null, null, null };
    private String pathtokeystoredir = null;
    private String pathtoprivkeydir = null;
    private String ik = null;
    private String institution = null;
    private String person = null;
    NebraskaKeystore keystore = null;

	public static HashMap<String, String> hmZertifikat = new HashMap<String, String>();

    public NebraskaRequestDlg(NebraskaZertAntrag zertantrag, boolean importiert, String therapidir) {
        super();
        this.activateListeners();
        this.setTitle("4 Schritte zum Zertifikatsrequest");

        this.zertantrag = zertantrag;
        this.ik = zertantrag.getIK();
        this.institution = zertantrag.getInstitution();
        this.person = zertantrag.getPerson();

        if (this.ik.equals("") || this.institution.equals("") || this.person.equals("")) {
            JOptionPane.showMessageDialog(null, "IK, Antragsteller und Ansprechpartner dürfen nicht leer sein");
            this.dispose();
            return;
        }

        this.content = this.contentPanel();

        if (importiert) {
            System.out.println("nehme getImportPanel()");
            pathtokeystoredir = (therapidir + File.separator + "keystore" + File.separator + ik).replace("\\", "/");
            pathtoprivkeydir = (pathtokeystoredir + File.separator + "privkeys").replace("\\", "/");
            this.content.add(getImportPanel(importiert, therapidir, ik), BorderLayout.CENTER);
        } else {
            System.out.println("nehme getStandAlonePanel()");
            this.content.add(getStandAlonePanel(), BorderLayout.CENTER);
        }
        this.setContentPane(this.content);
        this.getContentPane()
            .setPreferredSize(new Dimension(800, 400));
        this.getContentPane()
            .validate();
    }

    /*****
     * Für Benutzer von Thera-Pi, die die Mandantendaten zuvor importiert haben
     * 
     * @param importiert
     * @param therapidir
     * @param ik
     * @return
     */

    private JXPanel getImportPanel(boolean importiert, String therapidir, String ik) {
        // 1 2 3 4 5 6 7 8 9
        FormLayout lay = new FormLayout("0dlu,fill:0:grow(0.5),p,10dlu,p,5dlu,50dlu,fill:0:grow(0.5),0dlu",
                // 1 2 3 4 5 6 7 8 9
                "fill:0:grow(0.5),p,15dlu,p,15dlu,p,15dlu,p,fill:0:grow(0.5)");
        CellConstraints cc = new CellConstraints();
        JXPanel jpan = new JXPanel();
        jpan.setLayout(lay);
        mlabs[0] = new MultiLineLabel(
                "1. Verzeichnis der Zertifikatsdatenbank\n(Bei Import aus Thera-Pi fest vorgegeben)", 0, 0);
        mlabs[0].setAlignment(MultiLineLabel.RIGHT);
        jpan.add(mlabs[0], cc.xy(3, 2, CellConstraints.RIGHT, CellConstraints.CENTER));
        labs[0] = new JLabel(pathtokeystoredir);
        labs[0].setForeground(Color.RED);
        jpan.add(labs[0], cc.xy(5, 2));
        jpan.add((buts[0] = ButtonTools.macheBut("ändern", "edit", al)), cc.xy(7, 2));
        buts[0].setEnabled(false);
        mlabs[1] = new MultiLineLabel(
                "2. Passwort für die Zertifikatsdatenbank eingeben (max.6 Zeichen!)\n(Sinniger Weise ist diese Passwort fest vorgegeben!!!)",
                0, 0);
        mlabs[1].setAlignment(MultiLineLabel.RIGHT);
        jpan.add(mlabs[1], cc.xy(3, 4, CellConstraints.RIGHT, CellConstraints.CENTER));
        pw = new JRtaTextField("nix", true);
        pw.setText("123456");
        pw.setEditable(false);
        jpan.add(pw, cc.xy(5, 4));
        jpan.add((buts[1] = ButtonTools.macheBut("fixieren", "fixit", al)), cc.xy(7, 4));

        mlabs[2] = new MultiLineLabel("3. Sodele.... jetzt das geheime Schlüsselpaar erzeugen und speichern", 0, 0);
        mlabs[2].setEnabled(false);
        jpan.add(mlabs[2], cc.xyw(3, 6, 3, CellConstraints.RIGHT, CellConstraints.CENTER));
        jpan.add((buts[2] = ButtonTools.macheBut("und los...", "generatekeypair", al)), cc.xy(7, 6));
        buts[2].setEnabled(false);

        mlabs[3] = new MultiLineLabel(
                "4. Abschließend den Zertifikats-Request für die ITSG erzeugen\nund fertig ist die Laube", 0, 0);
        mlabs[3].setAlignment(MultiLineLabel.RIGHT);
        jpan.add(mlabs[3], cc.xyw(3, 8, 3, CellConstraints.RIGHT, CellConstraints.CENTER));
        jpan.add((buts[3] = ButtonTools.macheBut("und los...", "generaterequest", al)), cc.xy(7, 8));
        buts[3].setEnabled(false);
        jpan.validate();
        return jpan;
    }

    private JXPanel getStandAlonePanel() {
        JXPanel jpan = new JXPanel();
        jpan.add(new JLabel(
                "Nebraska StandAlone-Panel noch nicht entwickelt!\n\nSie müssen die Mandantendaten zuvor aus Thera-Pi importieren!"));
        return jpan;
    }

    private JXPanel contentPanel() {
        JXPanel pan = new JXPanel(new BorderLayout());
        JXHeader jxhead = new JXHeader("Zertifikats-Request erzeugen",
                "Gehen Sie einfach Schritt für Schritt durch die 4 angegebenen Punkte - nur keine Panik, Sie haben Zeit...\n\n"
                        + "Vergessen Sie bitte nicht Ihr gewähltes Passwort zu notieren und an sicherer Stelle zu verwahren!",
                null);
        pan.add(jxhead, BorderLayout.NORTH);
        return pan;
    }

    private void activateListeners() {
        al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String cmd = arg0.getActionCommand();
                if (cmd.equals("fixit")) {
                    doFixIt();
                    return;
                }
                if (cmd.equals("generatekeypair")) {
                    JOptionPane.showMessageDialog(null, "Schlüssel generieren benötigt ein paar Sekunden.\nBitte um etwas Geduld ...");
                    setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    doGenerateKeys();
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    return;
                }
                if (cmd.equals("generaterequest")) {
                    doGenerateRequest();
                    String meldung = "Der Zertifikatsrequest wurde erfolgreich erzeugt!\n\n"
                            + "Sie können nun dieses Fenster schließen, den Begleitzettel ausdrucken und dann die Datei\n\n"
                            + zertantrag.therapidir + "/keystore/" + ik + "/" + ik
                            + ".p10\n\nals Emailanhang an die ITSG senden\n" + "";
                    JOptionPane.showMessageDialog(null, meldung);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                RunAjax("http://www.thera-pi.org/html/updates.php", "updated.txt",
                                        "ZertRequest-erstellt-für-" + ik + "-" + institution.replace(" ", "_") + "-"
                                                + person.replace(" ", "_"));
                            } catch (Exception ex) {
                            }
                        }
                    });

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                macheConfig();
                            } catch (Exception ex) {
                            }
                        }
                    });
                    return;
                }
            }

        };
    }

    private void macheConfig() {
        String osVersion = System.getProperty("os.name");
        String confdatei = null;
        if (osVersion.contains("Linux")) {
            confdatei = this.zertantrag.therapidir + "/nebraska_linux.conf";
        } else if (osVersion.contains("Windows")) {
            confdatei = this.zertantrag.therapidir + "/nebraska_windows.conf";
        } else if (osVersion.contains("Mac OS X")) {
            confdatei = "keine Ahnung..... /st.";
        }
        erstelleConfig(confdatei);
    }

    private void erstelleConfig(String configfile) {
        INIFile ifile = new INIFile(configfile);
        boolean neuerEintrag = false;
        int eintragBei = -1;
        if (NebraskaMain.keyStoreParameter.size() <= 0) {
            neuerEintrag = true;
        } else {
            for (int i = 0; i < NebraskaMain.keyStoreParameter.size(); i++) {
                if (ifile.getStringProperty("KeyStores", "KeyStoreAlias" + Integer.toString(i + 1))
                         .equals("IK" + this.ik)) {
                    neuerEintrag = false;
                    eintragBei = i + 1;
                    break;
                }
                neuerEintrag = true;
            }
        }
        Vector<String> dummy = new Vector<String>();
        /*
         * dummy.add( ini.getStringProperty("KeyStores",
         * "KeyStoreFile"+Integer.toString(i+1)) ); dummy.add(
         * man.decrypt(ini.getStringProperty("KeyStores",
         * "KeyStorePw"+Integer.toString(i+1))) ); dummy.add(
         * ini.getStringProperty("KeyStores", "KeyStoreAlias"+Integer.toString(i+1)) );
         * dummy.add( man.decrypt(ini.getStringProperty("KeyStores",
         * "KeyStoreKeyPw"+Integer.toString(i+1))) ); keyStoreParameter.add(
         * (Vector<String>) dummy.clone() );
         */
        int inipos = -1;
        if (neuerEintrag && eintragBei < 0) { // es gibt noch keinen Eintrag
            inipos = 1;
            dummy.add(this.zertantrag.therapidir + "/keystore/" + this.ik + "/" + this.ik + ".p12");
            dummy.add(pw.getText());
            dummy.add("IK" + this.ik);
            dummy.add("abc");
            NebraskaMain.keyStoreParameter.add((Vector<String>) dummy.clone());
        } else if (neuerEintrag && NebraskaMain.keyStoreParameter.size() > 0) {// es gibt schon Einträge aber nicht den
                                                                               // aktuellen
            inipos = NebraskaMain.keyStoreParameter.size() + 1;
            dummy.add(this.zertantrag.therapidir + "/keystore/" + this.ik + "/" + this.ik + ".p12");
            dummy.add(pw.getText());
            dummy.add("IK" + this.ik);
            dummy.add("abc");
            NebraskaMain.keyStoreParameter.add((Vector<String>) dummy.clone());
        } else if (!neuerEintrag && eintragBei > 0) {
            inipos = Integer.valueOf(eintragBei);
            dummy.add(this.zertantrag.therapidir + "/keystore/" + this.ik + "/" + this.ik + ".p12");
            dummy.add(pw.getText());
            dummy.add("IK" + this.ik);
            dummy.add("abc");
            NebraskaMain.keyStoreParameter.set(inipos - 1, (Vector<String>) dummy.clone());
        }
        Verschluesseln man = Verschluesseln.getInstance();

        ifile.setIntegerProperty("KeyStores", "KeyStoreAnzahl", NebraskaMain.keyStoreParameter.size(), null);
        ifile.setStringProperty("KeyStores", "KeyStoreFile" + Integer.toString(inipos),
                this.zertantrag.therapidir + "/keystore/" + this.ik + "/" + this.ik + ".p12", null);
        ifile.setStringProperty("KeyStores", "KeyStorePw" + Integer.toString(inipos), man.encrypt(pw.getText()
                                                                                                    .trim()),
                null);
        ifile.setStringProperty("KeyStores", "KeyStoreAlias" + Integer.toString(inipos), "IK" + this.ik, null);
        ifile.setStringProperty("KeyStores", "KeyStoreKeyPw" + Integer.toString(inipos), man.encrypt("abc"), null);
        ifile.save();
        // Jetzt in die Ini-Schreiben
        JRtaComboBox cmb = this.zertantrag.elternTab.zertExplorer.jcombo;
        cmb.setDataVectorWithStartElement(NebraskaMain.keyStoreParameter, 2, 2, "./.");
        cmb.setSelectedVecIndex(2, "IK" + this.ik);
    }

    private void doFixIt() {
        if (pw.getText()
              .trim()
              .equals("")) {
            JOptionPane.showMessageDialog(null, "Passwort darf nicht leer sein!");
            return;
        }
        mlabs[0].setEnabled(false);
        mlabs[1].setEnabled(false);
        buts[1].setEnabled(false);
        pw.setEditable(false);
        mlabs[2].setEnabled(true);
        buts[2].setEnabled(true);

    }

    private void doGenerateKeys() {
        mlabs[2].setEnabled(false);
        buts[2].setEnabled(false);
        String privkeyfile = "privkey" + DatFunk.sDatInSQL(DatFunk.sHeute())
                                                .replace("-", "");
        if (pw.getText()
              .trim()
              .length() > 6) {
            JOptionPane.showMessageDialog(null,
                    "Also wer nicht bis auf 6 zählen kann, der sollte tunlichst die Finger von der Verschlüsselung lassen....");
            return;
        }
        try {
            System.out.println(this.ik);
            System.out.println(this.institution);
            keystore = new NebraskaKeystore(this.pathtokeystoredir + File.separator + this.ik + ".p12", pw.getText()
                                                                                                          .trim(),
                    "", this.ik, this.institution, this.person);
            X509Certificate keyCert = keystore.getKeyCert();
            if (keyCert != null) {
                String[] teile = keyCert.getSubjectDN()
                                        .toString()
                                        .split(",");
                for (int i = 0; i < teile.length; i++) {
                    if (teile[i].equals("OU=IK" + this.ik)) {
                        int antwort = JOptionPane.showConfirmDialog(null,
                                "Dieser Schlüssel existiert bereits, wollen Sie einen neuen Schlüssel beantragen?",
                                "Achtung extrem wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
                        if (antwort == JOptionPane.YES_OPTION) {
                            keystore = new NebraskaKeystore(
                                    this.pathtokeystoredir + File.separator + this.ik + "_neu.p12", pw.getText()
                                                                                                      .trim(),
                                    "", this.ik, this.institution, this.person);
                        } else {
                            JOptionPane.showMessageDialog(null, "Der Keystore kann nicht verändert werden");
                            this.dispose();
                            return;
                        }

                    }
                }
            }
            keystore.setCrqSignatureAlgorithm(NebraskaConstants.CRQ_SIGNATURE_ALGORITHM_DEFAULT);
            // ToDo:
            // for RSASSA-PSS set PSS-parameters too
            // McM 2020-02: default settings work - no action neccessary for now
            keystore.generateKeyPairAndSaveToFile(true, privkeyfile, pathtoprivkeydir);
            // keystore.generateKeyPairAndSaveToFile(false, privkeyfile, pathtoprivkeydir);
        } catch (NebraskaCryptoException e) {
            JOptionPane.showMessageDialog(null,
                    "Fehler, es existiert bereits ein gültiges Schlüsselpaar, überschreiben ist nicht erlaubt!"
                            + "\nWenn Sie ein neues Schlüsselpaar erzeugen wollen löschen Sie alle Dateien im Verzeichnis "
                            + this.pathtokeystoredir);
            e.printStackTrace();
        } catch (NebraskaFileException e) {
            e.printStackTrace();
        } catch (NebraskaNotInitializedException e) {
            e.printStackTrace();
        }
        mlabs[3].setEnabled(true);
        buts[3].setEnabled(true);
    }

    private void doGenerateRequest() {
        mlabs[3].setEnabled(false);
        buts[3].setEnabled(false);
        NebraskaRequestDlg.hmZertifikat.clear();
        StringBuffer md5Buf = new StringBuffer();
        try {
            OutputStream out = new FileOutputStream(this.pathtokeystoredir + File.separator + this.ik + ".p10");
            keystore.createCertificateRequest(out, md5Buf);
            out.flush();
            out.close();

            NebraskaRequestDlg.hmZertifikat.put("<Ikpraxis>", "IK" + keystore.getIK());
            NebraskaRequestDlg.hmZertifikat.put("<Issuerc>", "C=DE");
            NebraskaRequestDlg.hmZertifikat.put("<Issuero>", "O=" + NebraskaConstants.X500_PRINCIPAL_ORGANIZATION);
            NebraskaRequestDlg.hmZertifikat.put("<Subjectc>", "C=DE");
            NebraskaRequestDlg.hmZertifikat.put("<Subjecto>", "O=" + NebraskaConstants.X500_PRINCIPAL_ORGANIZATION);
            NebraskaRequestDlg.hmZertifikat.put("<Subjectou1>", "OU=" + keystore.getCompanyName());
            NebraskaRequestDlg.hmZertifikat.put("<Subjectou2>", "OU=" + "IK" + keystore.getIK());
            NebraskaRequestDlg.hmZertifikat.put("<Subjectcn>", "CN=" + keystore.getCEO());
            NebraskaRequestDlg.hmZertifikat.put("<Algorithm>", NebraskaUtil.decodeHashAlgorithm(keystore.getCertSignatureAlgorithm())); 
            // Nebraska.hmZertifikat.put("<Md5publickey>",md5Buf.toString().replace(":", "
            // "));
            PKCS10CertificationRequest request = keystore.getCertificateRequest();
            request.verify(request.getPublicKey(), NebraskaConstants.SECURITY_PROVIDER);
            CertificationRequestInfo info = request.getCertificationRequestInfo();
            ASN1Object asno = (ASN1Object) info.getDERObject()
                                               .toASN1Object();
            ASN1Sequence aseq = ASN1Sequence.getInstance(asno);
            SubjectPublicKeyInfo spub = null;
            for (int i = 0; i < aseq.size(); i++) {
                System.out.println("Objec Nr." + i + " aus der ASN1-Struktur = " + aseq.getObjectAt(i));
                if (aseq.getObjectAt(i) instanceof SubjectPublicKeyInfo) {
                    spub = (SubjectPublicKeyInfo) aseq.getObjectAt(i);
                    // System.out.println("Public Key des Requests = "+spub.getPublicKeyData());
                    // System.out.println("SHA1-Hash aus dem PubKey des Requests =
                    // "+BCStatics2.getSHA1fromByte(spub.getPublicKeyData().getEncoded()));
                }
            }

            // String sha1 = BCStatics2.getSHA1fromByte(spub.getPublicKeyData().getBytes());
            String hash = BCStatics2.getSHA256fromByte(spub.getPublicKeyData()
                                                           .getBytes());
            NebraskaRequestDlg.hmZertifikat.put("<Sha1publickey>", BCStatics2.macheHexDump(hash, 20, " "));

            String md5 = BCStatics2.getMD5fromByte(spub.getPublicKeyData()
                                                       .getBytes());
            NebraskaRequestDlg.hmZertifikat.put("<Md5publickey>", BCStatics2.macheHexDump(md5, 20, " "));

            // sha1 = BCStatics2.getSHA1fromByte(request.getEncoded());
            hash = BCStatics2.getSHA256fromByte(request.getEncoded());
            NebraskaRequestDlg.hmZertifikat.put("<Sha1certificate>", BCStatics2.macheHexDump(hash, 20, " "));

            md5 = BCStatics2.getMD5fromByte(request.getEncoded());
            NebraskaRequestDlg.hmZertifikat.put("<Md5certificate>", BCStatics2.macheHexDump(md5, 20, " "));

            java.security.interfaces.RSAPublicKey pub = (java.security.interfaces.RSAPublicKey) request.getPublicKey();
            String hexstring = new BigInteger(pub.getModulus()
                                                 .toByteArray()).toString(16);
            // System.out.println("Hexstring = "+hexstring);
            String modulus = BCStatics2.macheHexDump(hexstring, 20, " ");
            NebraskaRequestDlg.hmZertifikat.put("<Modulus>", modulus);

            hexstring = new BigInteger(pub.getPublicExponent()
                                          .toByteArray()).toString(16);
            NebraskaRequestDlg.hmZertifikat.put("<Exponent>", (hexstring.length() == 5 ? "0" + hexstring : hexstring));
            String vorlage = this.zertantrag.therapidir + "/defaults/vorlagen/ZertBegleitzettel-SHA256.ott";
            File f = new File(vorlage);
            if (!f.exists()) {
                vorlage = FileStatics.dirChooser(this.zertantrag.therapidir + "/defaults/vorlagen/",
                        "Bitte wählen Sie die Vorlage auswählen (Standard = ZertBegleitzettel-SHA256.ott)");
                if (!vorlage.equals("")) {
                    OOorgTools.starteStandardFormular(vorlage, null);
                }
            } else {
                OOorgTools.starteStandardFormular(vorlage, null);
            }
            System.out.println(md5Buf);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NebraskaCryptoException e) {
            e.printStackTrace();
        } catch (NebraskaFileException e) {
            e.printStackTrace();
        } catch (NebraskaNotInitializedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void RunAjax(String partUrl, String indatei, String testdatei) {
        InetAddress dieseMaschine = null;
        try {
            dieseMaschine = java.net.InetAddress.getLocalHost();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        String url = null;
        if (!dieseMaschine.toString()
                          .contains("192.168.2.2")) {
            url = partUrl + "?indatei=" + indatei + "&tester=" + dieseMaschine.toString() + "&datei=" + testdatei;
        }
        try {
            URL tester = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) tester.openConnection();
            httpURLConnection.setAllowUserInteraction(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.getResponseMessage();
            // System.out.println(httpURLConnection.getResponseMessage());
            httpURLConnection.setRequestProperty("Accept", "true");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
