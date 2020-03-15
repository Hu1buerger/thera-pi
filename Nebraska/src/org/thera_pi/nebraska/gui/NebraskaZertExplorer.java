package org.thera_pi.nebraska.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.thera_pi.nebraska.crypto.NebraskaCryptoException;
import org.thera_pi.nebraska.crypto.NebraskaDecryptor;
import org.thera_pi.nebraska.crypto.NebraskaEncryptor;
import org.thera_pi.nebraska.crypto.NebraskaFileException;
import org.thera_pi.nebraska.crypto.NebraskaKeystore;
import org.thera_pi.nebraska.crypto.NebraskaNotInitializedException;
import org.thera_pi.nebraska.crypto.NebraskaUtil;
import org.thera_pi.nebraska.gui.utils.ButtonTools;
import org.thera_pi.nebraska.gui.utils.FileStatics;
import org.thera_pi.nebraska.gui.utils.JRtaCheckBox;
import org.thera_pi.nebraska.gui.utils.JRtaComboBox;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.JRtaTextField;

import org.thera_pi.nebraska.gui.utils.JCompTools;

public class NebraskaZertExplorer extends JXPanel implements ListSelectionListener, ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -7368787318044651022L;
    public static X509Certificate[] annahmeCerts = null;
    JXPanel content = null;
    public MyCertTableModel tabmod = new MyCertTableModel();
    public JXTable tab;
    public JButton[] buts = { null, null, null, null };
    public JRtaTextField[] tfs = { null, null, null };
    public NebraskaKeystore keystore = null;
    public JRtaComboBox jcombo = null;
    public JRtaCheckBox check = null;
    public String pfad = System.getProperty("user.dir") + File.separator + "keystore" + File.separator;

    public NebraskaZertExplorer() {
        super();
        setLayout(new BorderLayout());
        add(getContent(), BorderLayout.CENTER);
    }

    private JXPanel getContent() {
        content = new JXPanel();// 1 2 3 4 5 6
        FormLayout lay = new FormLayout("10dlu,80dlu,5dlu,100dlu,5dlu,fill:0:grow(1.0),10dlu",
                // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15
                "10dlu,p,5dlu,150dlu,25dlu,p,5dlu,p,15dlu,p,2dlu,p,5dlu,p,fill:0:grow(0.5)");
        CellConstraints cc = new CellConstraints();
        content.setLayout(lay);
        content.add(new JLabel("Keystore auswählen"), cc.xy(2, 2));
        jcombo = new JRtaComboBox();
        if (NebraskaMain.keyStoreParameter.size() > 0) {
            jcombo.setDataVectorWithStartElement(NebraskaMain.keyStoreParameter, 2, 2, "./.");
        }
        jcombo.setActionCommand("keystorewahl");
        jcombo.addActionListener(this);

        content.add(jcombo, cc.xyw(4, 2, 2));

        tabmod = new MyCertTableModel();
        tabmod.setColumnIdentifiers(
                new String[] { "Alias", "Zert oder Key?", "Zert. Inhaber", "Ansprechpartner", "gültig bis" });
        tab = new JXTable(tabmod);
        tab.getColumn(0)
           .setMaxWidth(100);
        tab.getColumn(0)
           .setMinWidth(100);
        tab.getColumn(1)
           .setMaxWidth(60);
        tab.getColumn(4)
           .setMaxWidth(150);
        tab.getColumn(4)
           .setMinWidth(150);
        tab.validate();
        tab.setSelectionMode(0);
        tab.getSelectionModel()
           .addListSelectionListener(new ZertListSelectionHandler());

        JScrollPane jscr = JCompTools.getTransparentScrollPane(tab);
        jscr.validate();
        content.add(jscr, cc.xyw(2, 4, 5));

        content.add(new JLabel("Quelldatei auswählen"), cc.xy(2, 6));
        content.add((buts[0] = ButtonTools.macheBut("Quelle auswählen", "source", this)), cc.xyw(4, 6, 2));
        tfs[0] = new JRtaTextField("nix", true);
        content.add(tfs[0], cc.xy(6, 6));

        content.add(new JLabel("Zieldatei auswählen"), cc.xy(2, 8));
        content.add((buts[1] = ButtonTools.macheBut("Ziel auswählen", "target", this)), cc.xyw(4, 8, 2));
        tfs[1] = new JRtaTextField("nix", true);
        content.add(tfs[1], cc.xy(6, 8));

        check = new JRtaCheckBox("zusätzl. auf eigenes IK verschl.");
        content.add(check, cc.xy(4, 10));

        content.add((buts[2] = ButtonTools.macheBut("Verschlüsselung", "encoding", this)), cc.xyw(4, 12, 2));
        content.add((buts[3] = ButtonTools.macheBut("Entschlüsselung", "decoding", this)), cc.xyw(4, 14, 2));
        if (jcombo.getItemCount() > 0 && NebraskaMain.therapiIK != null) {
            jcombo.setSelectedVecIndex(2, "IK" + NebraskaMain.therapiIK);
        }
        if (tab.getRowCount() > 0) {
            System.out.println("Anzahl Zerts = " + tab.getRowCount());
            tab.setRowSelectionInterval(0, 0);
            inspectZert(0);
        }

        return content;
    }

    public void doFuelleTabelle() throws Exception {
        // Hier werden die beiden Tabellen mit den Angaben zu enthaltenen Zertifikaten
        // gefüllt
        // Hier die Enumeration durch die Aliases und dann die Tabellen füllen
    }

    @Override
    public void valueChanged(ListSelectionEvent arg0) {

    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        String cmd = arg0.getActionCommand();
        if (cmd.equals("los")) {
            doVerEntschluesseln();
        }
        if (cmd.equals("keystorewahl")) {
            doKeystoreWahl();
        }
        if (cmd.equals("source")) {
            new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    String source = FileStatics.dirChooser(pfad, "Quelldatei auswählen");
                    if (!source.equals("")) {
                        pfad = String.valueOf(source);
                        tfs[0].setText(source);
                        tfs[1].setText(source);
                    }
                    return null;
                }
            }.execute();
        }
        if (cmd.equals("target")) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    String source = FileStatics.dirChooser(pfad, "Zieldatei auswählen");
                    if (!source.equals("")) {
                        pfad = String.valueOf(source);
                        tfs[1].setText(source);
                    }
                    return null;
                }
            }.execute();

        }
        if (cmd.equals("encoding")) {
            try {
                doEncoding();
            } catch (NebraskaCryptoException e) {
                JOptionPane.showMessageDialog(null, "Verschlüsselung fehlgeschlagen");
                e.printStackTrace();
            } catch (NebraskaNotInitializedException e) {
                JOptionPane.showMessageDialog(null, "Verschlüsselung fehlgeschlagen");
                e.printStackTrace();
            } catch (NebraskaFileException e) {
                JOptionPane.showMessageDialog(null, "Verschlüsselung fehlgeschlagen");
                e.printStackTrace();
            }
        }
        if (cmd.equals("decoding")) {
            try {
                doDecoding();
            } catch (NebraskaCryptoException e) {
                JOptionPane.showMessageDialog(null, "Entschlüsselung fehlgeschlagen");
                e.printStackTrace();
            } catch (NebraskaNotInitializedException e) {
                JOptionPane.showMessageDialog(null, "Entschlüsselung fehlgeschlagen");
                e.printStackTrace();
            } catch (NebraskaFileException e) {
                JOptionPane.showMessageDialog(null, "Entschlüsselung fehlgeschlagen");
                e.printStackTrace();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Entschlüsselung fehlgeschlagen");
                e.printStackTrace();
            }
        }

    }

    public void doKeystoreWahl() {
        tabmod.setRowCount(0);
        int index = jcombo.getSelectedIndex();
        if (index <= 0) {
            tab.validate();
            return;
        } else {
            try {
                NebraskaMain.jf.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                keystore = new NebraskaKeystore(NebraskaMain.keyStoreParameter.get(index - 1)
                                                                              .get(0),
                        NebraskaMain.keyStoreParameter.get(index - 1)
                                                      .get(1),
                        NebraskaMain.keyStoreParameter.get(index - 1)
                                                      .get(3),
                        NebraskaMain.keyStoreParameter.get(index - 1)
                                                      .get(2));
                zertifikateHolen();
                /*
                 * try { keystore.exportKey("C:/privkey",
                 * NebraskaMain.keyStoreParameter.get(index-1).get(1)); } catch
                 * (NebraskaNotInitializedException e) {
                 *
                 * e.printStackTrace(); }
                 */

            } catch (NebraskaCryptoException e) {
                keystore = null;
                tab.validate();
                NebraskaMain.jf.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                e.printStackTrace();
            } catch (NebraskaFileException e) {
                keystore = null;
                tab.validate();
                NebraskaMain.jf.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                e.printStackTrace();
            }
        }
    }

    public void inspectZert(int cert) {
        int row = tab.getSelectedRow();
        if (row < 0 || tabmod.getRowCount() <= 0) {
            return;
        }
        String ik = tab.getValueAt(row, 0)
                       .toString();
        String ikStoreOwner = jcombo.getSelectedItem()
                                    .toString()
                                    .trim();
        String ikSelectedRow = null;
        if (tabmod.getRowCount() > 0) {
            Vector<X509Certificate> certs = keystore.getAllCerts();
            for (int i = 0; i < certs.size(); i++) {
                try {
                    String hashAlgorithm = NebraskaUtil.decodeHashAlgorithm(certs.get(i)
                                                                                 .getSigAlgName()
                                                                                 .toString());
                    ikSelectedRow = certs.get(i)
                                         .getSubjectDN()
                                         .toString()
                                         .split(",")[3].split("=")[1];
                    if (ik.equals(ikSelectedRow)) {
                        if (ik.equals(ikStoreOwner)) {
                            showHash(ik, hashAlgorithm, true);
                        } else {
                            showHash(ik, hashAlgorithm, false);
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }

    }

    private void showHash(String ik, String hashAlgorithm, boolean ownCert) {
        String show = "Hash-Algorithmus ";
        if (ownCert) {
            show = (show + "eigenes Zertifikat:  ");
        } else {
            show = (show + "Zertifikat von " + ik + ":  ");
        }
        ((NebraskaJTabbedPaneOrganizer) this.getParent()
                .getParent()).setAlgText(show + hashAlgorithm);
    }

    public void erstTest() {
        if (tabmod.getRowCount() > 0) {
            tab.setRowSelectionInterval(0, 0);
            inspectZert(0);
        }
    }

    private void zertifikateHolen() {
        // keystore.deleteAllCerts();

        Vector<X509Certificate> certs = keystore.getAllCerts();
        // System.out.println(keystore.getKeyCert().getSubjectDN());
        Vector<Object> vec = new Vector<Object>();
        ImageIcon imgkey = new ImageIcon(System.getProperty("user.dir") + File.separator + "icons/entry_pk.gif");
        ImageIcon imgcert = new ImageIcon(
                System.getProperty("user.dir") + File.separator + "icons/certificate_node.gif");
        String[] dn = null;
        String ik;

        boolean ownCertOk = false;
        for (int i = 0; i < certs.size(); i++) {
            vec.clear();
            dn = certs.get(i)
                      .getSubjectDN()
                      .toString()
                      .split(",");
            /*
             *
             * for(int i2 = 0; i2 < dn.length; i2++){
             * System.out.println("Zertifikat "+(i+1)+"  Bestandteil "+(i2+1)+" = "+dn[i2]);
             * }
             */
            try {
                if (dn.length == 5) {

                    ik = dn[3].split("=")[1];
                    if (!ik.equals(jcombo.getSelectedItem()
                                         .toString()
                                         .trim())) {
                        vec.add(ik);
                        vec.add(imgcert);
                        vec.add(dn[2].split("=")[1]);
                        vec.add(dn[4].split("=")[1]);
                        vec.add(certs.get(i)
                                     .getNotAfter()
                                     .toLocaleString());

                        tabmod.addRow((Vector<?>) vec.clone());
                    } else {
                        if (!ownCertOk) {
                            vec.add(ik);
                            vec.add(imgkey);
                            vec.add(dn[2].split("=")[1]);
                            vec.add(dn[4].split("=")[1]);
                            vec.add(certs.get(i)
                                         .getNotAfter()
                                         .toLocaleString());
                            tabmod.addRow((Vector<?>) vec.clone());
                            ownCertOk = true;
                            try {
                                String hashAlgorithm = NebraskaUtil.decodeHashAlgorithm(certs.get(i)
                                                                                             .getSigAlgName()
                                                                                             .toString());
                                showHash(ik, hashAlgorithm, true);
                            } catch (Exception ex) {
                            }
                        }
                    }

                }
            } catch (Exception ex) {

            }
        }
        if (tab.getRowCount() > 0) {
            tab.setRowSelectionInterval(0, 0);
        }
        vec.clear();
        NebraskaMain.jf.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    private void doEncoding() throws NebraskaCryptoException, NebraskaNotInitializedException, NebraskaFileException {
        int row = tab.getSelectedRow();
        String receiverIK = null;
        if (row >= 0) {
            receiverIK = tab.getValueAt(row, 0)
                            .toString();
        }
        NebraskaEncryptor encrypt = keystore.getEncryptor(receiverIK);
        if (check.isSelected()) {
            encrypt.setEncryptToSelf(true);
        }
        File f = new File(tfs[0].getText());
        long sizedec = f.length();
        long sizeenc = encrypt.encrypt(tfs[0].getText(), tfs[1].getText());
        JOptionPane.showMessageDialog(null, "Verschlüsselung erfolgreich!\n\n" + "Größe Quelldatei = "
                + Long.toString(sizedec) + " Bytes\n" + "Größe Zieldatei = " + Long.toString(sizeenc) + " Bytes\n\n");

    }

    private void doDecoding()
            throws NebraskaCryptoException, NebraskaNotInitializedException, NebraskaFileException, IOException {
        NebraskaDecryptor decrypt = keystore.getDecryptor();
        FileInputStream fin = new FileInputStream(tfs[0].getText()
                                                        .replace("\\", "/"));
        FileOutputStream fout = new FileOutputStream(tfs[1].getText()
                                                           .replace("\\", "/"));
        decrypt.decrypt(fin, fout);
        fin.close();
        fout.flush();
        fout.close();
        JOptionPane.showMessageDialog(null, "Datei " + tfs[0].getText() + " wurde erfolgreich entschlüsselt");
    }

    private void doVerEntschluesseln() {
        try {
            NebraskaKeystore nebraskastore = new NebraskaKeystore("C:/RehaVerwaltung/keystore/510841109/510841109.p12",
                    "123456", "abc", "IK510841109");
            System.out.println("Fingerprint des PublicKey = " + nebraskastore.getPublicKeyMD5());
            NebraskaEncryptor nebraskaencrypt = nebraskastore.getEncryptor("IK109900019");
            nebraskaencrypt.setEncryptToSelf(true);
            nebraskaencrypt.encrypt("C:/esol0237.org", "C:/esol0237.enc");
            NebraskaDecryptor nebraskadecrypt = nebraskastore.getDecryptor();
            FileInputStream fin = new FileInputStream("C:/esol0237.enc");
            FileOutputStream fout = new FileOutputStream("C:/esol0237.plain");
            nebraskadecrypt.decrypt(fin, fout);
            fin.close();
            fout.flush();
            fout.close();
        } catch (NebraskaCryptoException e) {

            e.printStackTrace();
        } catch (NebraskaFileException e) {
            e.printStackTrace();
        } catch (NebraskaNotInitializedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    class MyCertTableModel extends DefaultTableModel {
        /**
        *
        */
        private static final long serialVersionUID = 1L;

        @Override
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 1) {
                return ImageIcon.class;
            } else if (columnIndex == 4) {
                return String.class;
            } else {
                return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    /*****************************************************************************/
    class ZertListSelectionHandler implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();

            // int firstIndex = e.getFirstIndex();
            // int lastIndex = e.getLastIndex();
            boolean isAdjusting = e.getValueIsAdjusting();
            if (isAdjusting) {
                return;
            }
            // StringBuffer output = new StringBuffer();
            if (lsm.isSelectionEmpty()) {

            } else {
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
                // System.out.println("Zertifikat 0: "+minIndex);
                // System.out.println("Zertifikat ausgewähl: "+maxIndex);
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                        // System.out.println("Zertifikat ausgewähl: "+i);
                        inspectZert(i);
                        break;
                    }
                }
            }

        }
    }

}
