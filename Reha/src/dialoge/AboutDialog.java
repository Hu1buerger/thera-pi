package dialoge;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.xml.bind.DatatypeConverter;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import environment.Path;
import hauptFenster.Version;

/**
 * This class shall give credit to where credit is due. Anyone is free to add
 * their name to it IF they contribute. Please do not remove names from here.
 *
 */
public class AboutDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    private String myTitle;
    public JScrollPane scroller;
    public JTextArea text;
   protected HashMap<String, String> md5Hashes = new HashMap<String, String>();





    AboutDialog currInstance = null;
    JFrame instJar = null;
    boolean processingMD5 = false;

    public AboutDialog(Frame parent, String title) {
        super(parent, title);
        myTitle = title;
        currInstance = this;

        setLayout(new BorderLayout());
        add(getHintergrundPanel(), BorderLayout.CENTER);

        setSize(350, 200);
        setLocationRelativeTo(null);

        try {
            calcMD5();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("calcMD5:  NoSuchAlgorithm");
        } catch (FileNotFoundException e) {
            System.out.println("calcMD5:  FileNotFound");
        } catch (IOException e) {
            System.out.println("calcMD5:  IOException");
        }

    }

    AboutDialog() {
        //for testing
    }

    /*
     * 'Hintergrund' des About-Dialogs
     */
    private JPanel getHintergrundPanel() {
        JPanel DialogFrame = new JPanel();

        FormLayout lay = new FormLayout(
                // 1 2 3
                "5dlu,90dlu:g,5dlu", // xwerte,
                // 1 2 3 4 5
                "5dlu,30dlu:g,10dlu,20dlu,5dlu" // ywerte
        );
        PanelBuilder builder = new PanelBuilder(lay);

        builder.getPanel()
               .setOpaque(false);
        CellConstraints cc = new CellConstraints();

        int colCnt = 2, rowCnt = 2;

        JPanel upper = createDialogTextArea();
        builder.add(upper, cc.xy(colCnt, rowCnt++)); // 2,2

        JPanel lower = getButtonRow();
        builder.add(lower, cc.xy(colCnt, ++rowCnt, CellConstraints.FILL, CellConstraints.FILL)); // 2,4
        builder.getPanel()
               .validate();
        DialogFrame = builder.getPanel();

        return DialogFrame;
    }

    protected JPanel createDialogTextArea() {
        JPanel dialogArea = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        ImageIcon icon = new ImageIcon(Path.Instance.getProghome() + "icons/Pi_1_0_64x64.png");
        JLabel imgLbl = new JLabel(icon);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 10, 5, 15);
        gbc.anchor = GridBagConstraints.NORTH;
        dialogArea.add(imgLbl, gbc);

        JLabel htmlPane = new JLabel(mkCreditsTxt().toString());

        scroller = new JScrollPane(htmlPane);
        scroller.setBorder(null);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0); // reset
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = GridBagConstraints.RELATIVE;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        dialogArea.add(scroller, gbc);

        return dialogArea;
    }

    /**
     * @return Text: Copyright (u. Credits)
     */
    private StringBuffer mkCreditsTxt() {
        StringBuffer strBuf = new StringBuffer();
        strBuf.setLength(0);
        strBuf.trimToSize();
        strBuf.append("<html>");
        strBuf.append("Thera-\u03C0 v" +new  Version().number() + " vom " + Version.aktuelleVersion.replace("-DB=", "")
                + "<br>nach einer Idee von Jürgen Steinhilber<br><br>");

        // insert credits here:
        /*
         * strBuf.append("Credits:<br>"); strBuf.append("Bodo Meissner (bomm)<br>");
         * strBuf.append("Ernst Lehmann (lemmi)<br>"); strBuf.append("?? (drud)<br>");
         * strBuf.append("JannyP(jannyp)<br>");
         */
        strBuf.append("</html>");
        return strBuf;
    }

    /*
     * Buttons des About-Dialogs
     */
    private JPanel getButtonRow() {
        JPanel buttonArea = new JPanel();

        FormLayout lay = new FormLayout(
                // 1 2 3
                "90dlu,20dlu:g,55dlu", // xwerte,
                // 1
                "15dlu" // ywerte
        );
        PanelBuilder builder = new PanelBuilder(lay);
        builder.getPanel()
               .setOpaque(false);
        CellConstraints cc = new CellConstraints();

        int colCnt = 1, rowCnt = 1;
        JButton details = new JButton("Installation Details");
        details.setName("instDetail");
        details.setActionCommand("instDetail");
        details.addActionListener(this);

        details.setMnemonic('d');
        details.setToolTipText("Zeige MD5-Hashes der inst. JARs");
        details = (JButton) builder.add(details, cc.xy(colCnt++, rowCnt));
        addEnterToSpaceReaction(details);
        okButton = new JButton("OK");
        okButton.setName("quit");
        okButton.setActionCommand("quit");
        okButton.addActionListener(this); // 1,1

        okButton = (JButton) builder.add(okButton, cc.xy(++colCnt, rowCnt)); // 3,1


        buttonArea.add(builder.getPanel());

        return buttonArea;
    }

    protected void addEnterToSpaceReaction(JButton details) {
        Object spaceMap = details.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0));

        details. getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),spaceMap);
    }

    public void setFocus() {
        okButton.requestFocus();
    }

    /**
     * @throws NoSuchAlgorithmException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void calcMD5() throws NoSuchAlgorithmException, FileNotFoundException, IOException {
   SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws java.lang.Exception {
                processingMD5 = true;

                File dir = new File(Path.Instance.getProghome());
                List<File> files =  Arrays.asList(dir.listFiles( (dir1, name) -> name.endsWith(".jar")));

                md5Hashes.clear();
                for (File file : files) {
                    String string = md5Hash(file);


                    md5Hashes.put(file.getName(), string);
                }
                processingMD5 = false;
                return null;
            }


        };
        worker.execute();
    }
    protected String md5Hash(File file) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");

        md.update(Files.readAllBytes(Paths.get(file.getPath())));
        byte[] digest = md.digest();
        return DatatypeConverter
          .printHexBinary(digest).toUpperCase();
    }


    /*
     * Panel mit html-Tabelle erzeugen
     */
    private JPanel showMD5() {
        JTextPane pane = new JTextPane();
        JPanel md5Table = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = GridBagConstraints.RELATIVE;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        JLabel htmlPane = new JLabel(mkMd5Table().toString());

        scroller = new JScrollPane(htmlPane);
        scroller.setBorder(null); // keine Umrandung
        scroller.getVerticalScrollBar()
                .setUnitIncrement(15);
        scroller.validate();
        // scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        md5Table.add(scroller, gbc); // scrollen fkt noch nicht!

        return md5Table;
    }

    /**
     * @return Text: HTML-Tabelle mit MD5-Hashes der inst. JARs erzeugen
     */
    private StringBuffer mkMd5Table() {
        int calcRows = 0, waitMax = 100;
        StringBuffer strBuf = new StringBuffer();

        strBuf.setLength(0);
        strBuf.trimToSize();
        while (processingMD5) {
            if (--waitMax == 0) {
                JOptionPane.showMessageDialog(null, "MD5 Calculation failed", "Error:", JOptionPane.WARNING_MESSAGE);
                return strBuf;
            }
        }
        strBuf.append("<html>");
        strBuf.append("<table>");
        strBuf.append("<tr><th>file</th><th>MD5</th><th>&nbsp;&nbsp;&nbsp;</th><th>file</th><th>MD5</th></tr>");
        List<String> jars = new ArrayList<String>(md5Hashes.keySet());
       Collections.sort(jars ,String.CASE_INSENSITIVE_ORDER);
        if ((jars.size() % 2) > 0) {
            jars.add("");
        }
        calcRows = jars.size() / 2;

        List<String> leftCol = jars.subList(0, calcRows);
        List<String> rightCol = jars.subList(calcRows, jars.size());
        for (String key : leftCol) { // Ausgabe sortiert, aufeinanderfolgende untereinander
            // System.out.println(i+": "+jars.get(i)+" "+jars.get(calcRows+i));
            strBuf.append("<tr><td>" + key + "</td><td>" + md5Hashes.get(key) + "</td><td></td>"); // erster Eintrag in
                                                                                                   // Zeile
            key = rightCol.get(leftCol.indexOf(key));
            if (!key.equals("")) {
                strBuf.append("<td>" + key + "</td><td>" + md5Hashes.get(key) + "</td></tr>"); // zweiter Eintrag in
                                                                                               // Zeile
            } else {
                strBuf.append("<td></td><td></td></tr>"); // mit Leerspalten auffüllen
            }
        }
        strBuf.append("</table>");
        strBuf.append("</html>");
        return strBuf;
    }

    /*
     * Fenster mit MD5-Hashes öffnen (+ About-Dialog schließen)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        System.out.println(cmd);
        if (cmd.equals("instDetail")) {
            if (!currInstance.equals(null)) {

                currInstance.dispose(); // eleganter wäre das Panel neu zu füllen - krieg' ich aber nicht hin :-(
                instJar = (new JFrame()); // deshalb: neuer Frame
                instJar.setTitle(myTitle + ": installierte JARs");
                instJar.setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.BOTH;
                gbc.insets = new Insets(10, 15, 10, 15); // top, left, bottom, right

                JPanel newContent = (showMD5());
                instJar.setContentPane(newContent);
                instJar.pack();
                instJar.setSize(30 + newContent.getWidth(), 40 + newContent.getHeight());
                instJar.addKeyListener(keyListener);

                instJar.setLocationRelativeTo(null);
                instJar.setVisible(true);

            }
            return;
        }
        if (cmd.equals("quit")) {
            doQuit();
            return;
        }
    }

    KeyListener keyListener = new KeyAdapter() {


    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_ESCAPE) {
            doQuit();
        }
        if (code == KeyEvent.VK_ENTER) {
          if (getFocusOwner() instanceof JButton) {
            ((JButton) getFocusOwner()).doClick();
        } }

    }
    };
    private JButton okButton;
    private void doQuit() {
        if (instJar != null) {
            instJar.dispose(); // Dialog schließen
        }
        if (currInstance != null) {
            currInstance.dispose();
        }
    }





} // end class panel
