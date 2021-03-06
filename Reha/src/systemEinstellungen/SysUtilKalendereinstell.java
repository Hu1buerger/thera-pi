package systemEinstellungen;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.ExUndHop;
import CommonTools.JRtaTextField;
import CommonTools.ini.INITool;
import CommonTools.ini.Settings;
import environment.Path;
import hauptFenster.Reha;
import umfeld.Betriebsumfeld;

public class SysUtilKalendereinstell extends JXPanel implements ActionListener, FocusListener {

    JButton knopf1 = null;
    JButton knopf2 = null;
    JButton knopf3 = null;
    JComboBox<String> refresh = null;
    static JProgressBar Fortschritt = null;
    JRtaTextField STD1 = null;
    JRtaTextField MIN1 = null;
    JRtaTextField STD2 = null;
    JRtaTextField MIN2 = null;
    JCheckBox scan = null;
    JCheckBox langmenu = null;
    JCheckBox zeitzeigen = null;
    JCheckBox timelinezeigen = null;

    private boolean kalNeuAnfang = false;
    private boolean kalNeuEnde = false;

    public SysUtilKalendereinstell() {
        super(new GridLayout(1, 1));
        // System.out.println("Aufruf SysUtilKalendereinstell");
        this.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 20));
        /****/
        setBackgroundPainter(Reha.instance.compoundPainter.get("SystemInit"));
        /****/
        add(getVorlagenSeite());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                STD1.requestFocus();
            }
        });

        return;
    }

    /**************
     * Beginn der Methode f�r die Objekterstellung und -platzierung
     *********/
    private JPanel getVorlagenSeite() {

        knopf2 = new JButton("abbrechen");
        knopf2.setPreferredSize(new Dimension(70, 20));
        knopf2.addActionListener(this);
        knopf2.setActionCommand("abbruch");
        knopf1 = new JButton("speichern");
        knopf1.setPreferredSize(new Dimension(70, 20));
        knopf1.addActionListener(this);
        knopf1.setActionCommand("speichern");
        knopf3 = new JButton("entsperren");
        knopf3.addActionListener(this);
        knopf3.setActionCommand("unlock");

        Fortschritt = new JProgressBar();

        String[] refreshtakt = { "Einzel-PC", "LAN", "DSL" };
        refresh = new JComboBox<String>(refreshtakt);

        scan = new JCheckBox();
        scan.setSelected(TKSettings.KalenderBarcode);
        langmenu = new JCheckBox();
        langmenu.setSelected(TKSettings.KalenderLangesMenue);
        zeitzeigen = new JCheckBox();
        zeitzeigen.setSelected(TKSettings.KalenderZeitLabelZeigen);
        timelinezeigen = new JCheckBox();
        timelinezeigen.setSelected(TKSettings.KalenderTimeLineZeigen);

        STD1 = new JRtaTextField("STUNDEN", true);
        STD1.setText(TKSettings.KalenderUmfang[0].substring(0, 2));
        STD2 = new JRtaTextField("STUNDEN", true);
        STD2.setText(TKSettings.KalenderUmfang[1].substring(0, 2));
        MIN1 = new JRtaTextField("MINUTEN", true);
        MIN1.setText(TKSettings.KalenderUmfang[0].substring(3, 5));
        MIN1.setName("MIN1");
        // MIN1.addKeyListener(this);
        MIN1.addFocusListener(this);
        MIN2 = new JRtaTextField("MINUTEN", true);
        MIN2.setText(TKSettings.KalenderUmfang[1].substring(3, 5));
        MIN2.setName("MIN2");
        // MIN2.addKeyListener(this);
        MIN2.addFocusListener(this);

        // 1. 2. 3. 4. 5. 6. 7. 8. 9.
        FormLayout lay = new FormLayout("left:max(120dlu;p), 20dlu, 15dlu, 3dlu, 4dlu, 3dlu, 15dlu",
                // 1. 2. 3. 4. 5. 6. 7. 8. 9. 10. 11. 12. 13. 14. 15. 16. 17. 18. 19. 20. 21.
                // 22. 23. 24. 25.
                "p, 2dlu, p, 10dlu, p, 10dlu, p, 10dlu, p, 10dlu, p, 10dlu, p, 10dlu, p, 10dlu, p, 10dlu, p, 15dlu, p, 2dlu, p, 10dlu, p, 10dlu, p");

        PanelBuilder builder = new PanelBuilder(lay);
        builder.setDefaultDialogBorder();
        builder.getPanel()
               .setOpaque(false);
        CellConstraints cc = new CellConstraints();

        builder.addLabel("Tagesbeginn im Kalender", cc.xy(1, 1));
        builder.add(STD1, cc.xy(3, 1));
        builder.addLabel(":", cc.xy(5, 1));
        builder.add(MIN1, cc.xy(7, 1));
        builder.addLabel("Tagesende im Kalender", cc.xy(1, 3));
        builder.add(STD2, cc.xy(3, 3));
        builder.addLabel(":", cc.xy(5, 3));
        builder.add(MIN2, cc.xy(7, 3));
        builder.addLabel("Refresh-Takt", cc.xy(1, 5));
        builder.add(refresh, cc.xyw(3, 5, 5));
        builder.addLabel("Barcodescanner für Behandlungsbestätigungen", cc.xy(1, 7));
        builder.add(scan, cc.xy(7, 7, CellConstraints.RIGHT, CellConstraints.BOTTOM));
        builder.addLabel("Langes Menü anzeigen", cc.xy(1, 9));
        builder.add(langmenu, cc.xy(7, 9, CellConstraints.RIGHT, CellConstraints.BOTTOM));
        builder.addLabel("Uhrzeit an Mausposition anzeigen", cc.xy(1, 11));
        builder.add(zeitzeigen, cc.xy(7, 11, CellConstraints.RIGHT, CellConstraints.BOTTOM));
        builder.addLabel("Linie für aktuelle Uhrzeit anzeigen", cc.xy(1, 13));
        builder.add(timelinezeigen, cc.xy(7, 13, CellConstraints.RIGHT, CellConstraints.BOTTOM));

        builder.addSeparator("", cc.xyw(1, 15, 7));
        builder.addLabel("Abbruch ohne Übernahme", cc.xy(1, 17));
        builder.add(knopf2, cc.xyw(3, 17, 5));
        builder.addLabel("Parameter übernehmen", cc.xy(1, 19));
        builder.add(knopf1, cc.xyw(3, 19, 5));
        builder.addLabel("Fortschritt beim Verändern der Datenbank",
                cc.xy(1, 23, CellConstraints.LEFT, CellConstraints.BOTTOM));
        builder.add(Fortschritt, cc.xyw(1, 23, 7));
        builder.addSeparator("", cc.xyw(1, 25, 7));
        builder.addLabel("gesperrte Spalten freigeben", cc.xy(1, 27));
        builder.add(knopf3, cc.xyw(3, 27, 5));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                STD1.requestFocus();
            }
        });

        return builder.getPanel();
    }



    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand()
             .equals("speichern")) {
            if (kalNeuAnfang || kalNeuEnde) {
                JOptionPane.showMessageDialog(null,
                        "Die Funktion Kalenderzeiten verändern, wird während der Softwarentwicklung nicht aufgerufen!");
            }
            try {
                Settings ini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Betriebsumfeld.getAktIK() + "/",
                        "terminkalender.ini");
                ini.setStringProperty("Kalender", "KalenderBarcode", (scan.isSelected() ? "1" : "0"), null);
                INITool.saveIni(ini);

                ini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Betriebsumfeld.getAktIK() + "/", "kalender.ini");
                ini.setStringProperty("Kalender", "LangesMenue", (langmenu.isSelected() ? "1" : "0"), null);
                ini.setStringProperty("Kalender", "ZeitLabelZeigen", (zeitzeigen.isSelected() ? "1" : "0"), null);
                ini.setStringProperty("Kalender", "ZeitLinieZeigen", (timelinezeigen.isSelected() ? "1" : "0"), null);
                INITool.saveIni(ini);
                TKSettings.KalenderBarcode = scan.isSelected();
                TKSettings.KalenderLangesMenue = langmenu.isSelected();
                TKSettings.KalenderZeitLabelZeigen = Boolean.valueOf(zeitzeigen.isSelected());
                TKSettings.KalenderTimeLineZeigen = Boolean.valueOf(timelinezeigen.isSelected());
                if (Reha.instance.terminpanel != null) {
                    try {
                        Reha.instance.terminpanel.regleZeitLabel();
                        Reha.instance.terminpanel.setTimeLine(TKSettings.KalenderTimeLineZeigen);
                        Reha.instance.terminpanel.getViewPanel()
                                                 .repaint();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                JOptionPane.showMessageDialog(null, "Konfiguration wurde erfolgreich gespeichert");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Speichern der Konfiguration fehlgeschlagen");
            }

        }
        if (e.getActionCommand()
             .equals("abbruch")) {
            SystemInit.abbrechen();
            // SystemUtil.thisClass.parameterScroll.requestFocus();
        }
        if (e.getActionCommand()
             .equals("unlock")) {
            String cmd = "delete from flexlock";
            new ExUndHop().setzeStatement(cmd);
            JOptionPane.showMessageDialog(null, "Sämtliche Sperren der Terminspalten wurden aufgehoben");
        }

    }

    @Override
    public void focusLost(FocusEvent arg0) {

        if (((JComponent) arg0.getSource()).getName() != null) {
            if (((JComponent) arg0.getSource()).getName()
                                               .equals("MIN1")) {
                String s1, s2, s3;
                s1 = STD1.getText()
                         .trim();
                s2 = MIN1.getText()
                         .trim();
                s3 = s1 + ":" + s2 + ":00";
                if (!s3.equals(TKSettings.KalenderUmfang[0])) {
                    JOptionPane.showMessageDialog(null, "Sie haben die Kalenderanfangszeit verändert.\n\n"
                            + "Für die Neuorganisation des Terminkalenders können Sie schon mal einige Kannen Kaffee kochen!");
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            STD2.requestFocus();
                        }
                    });
                    kalNeuAnfang = true;
                } else {
                    kalNeuAnfang = false;
                }
            }
            if (((JComponent) arg0.getSource()).getName()
                                               .equals("MIN2")) {
                String s1, s2, s3;
                s1 = STD2.getText()
                         .trim();
                s2 = MIN2.getText()
                         .trim();
                s3 = s1 + ":" + s2 + ":00";
                if (!s3.equals(TKSettings.KalenderUmfang[1])) {
                    JOptionPane.showMessageDialog(null, "Sie haben die Kalenderendzeit verändert.\n\n"
                            + "Für die Neuorganisation des Terminkalenders können Sie schon mal einige Kannen Kaffee kochen!");
                    kalNeuEnde = true;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            refresh.requestFocus();
                        }
                    });
                } else {
                    kalNeuEnde = false;
                }
            }
        }

    }

    @Override
    public void focusGained(FocusEvent arg0) {

    }

}
