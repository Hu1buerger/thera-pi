package systemEinstellungen;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.INIFile;
import CommonTools.INITool;
import CommonTools.JRtaCheckBoxR;
import environment.Path;
import hauptFenster.Reha;
import opRgaf.OpRgAfIni;
import systemTools.ButtonTools;

// Lemmi 20101228 neue Klasse in der System-Inititalisierung
public class SysUtilOpMahnung extends JXPanel implements ActionListener, ItemListener {
    /**
     * 
     */
    private static final long serialVersionUID = 858117043130060154L;

    JRtaCheckBoxR ChkOP2BarKasse = null;
    JRtaCheckBoxR ChkVBon2BarKasse = null;
    JRtaCheckBoxR ChkVRhg2BarKasse = null;
    JCheckBox ChkLockOP = null;
    JCheckBox ChkLockOpRgAf = null;
    boolean dataValid = false, enableOP2BarKasse = false, enableVBon2BarKasse = false, enableVRhg2BarKasse = false,
            lockSettingsOP = false, lockSettingsOpRgAf = false, lockSettingsOP_ini = false, lockSettingsOpRgAf_ini = false;
    JButton abbruch = null;
    JButton speichern = null;

    private ActionListener al;
    private JLabel lab;

    public SysUtilOpMahnung(ImageIcon img) {
        super(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(15, 40, 20, 20));

        setBackgroundPainter(Reha.instance.compoundPainter.get("SystemInit"));

        initFields();
        add(getContent(), BorderLayout.CENTER);
        add(getKnopfPanel(), BorderLayout.SOUTH);
        initForm();
        return;
    }
    
    private void initForm() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                readFromIni();
                setFields();

                return null;
            }

        }.execute();
        validate();
    }

    private JPanel getKnopfPanel() {
        abbruch = ButtonTools.macheButton("abbrechen", "abbrechen", al);
        speichern = ButtonTools.macheButton("speichern", "speicher", al);

        // 1. 2. 3. 4. 5.
        FormLayout jpanlay = new FormLayout("right:max(150dlu;p), 60dlu, 60dlu, 4dlu, 60dlu",
                // 1. 2. 3.
                " p, 10dlu, p");

        PanelBuilder jpan = new PanelBuilder(jpanlay);
        jpan.getPanel()
            .setOpaque(false);
        CellConstraints jpancc = new CellConstraints();

        jpan.addSeparator("", jpancc.xyw(1, 1, 5));
        jpan.add(abbruch, jpancc.xy(3, 3));
        jpan.add(speichern, jpancc.xy(5, 3));
        jpan.addLabel("Änderungen übernehmen?", jpancc.xy(1, 3));

        jpan.getPanel()
            .validate();
        return jpan.getPanel();
    }

    private void initFields() {
        ChkLockOP = new JCheckBox();
        ChkLockOP.addItemListener(this);
        ChkOP2BarKasse = new JRtaCheckBoxR("Ausbuchen von (Privat-)Rechnungen an Barkasse erlauben");
        ChkOP2BarKasse.addItemListener(this);

        ChkLockOpRgAf = new JCheckBox();
        ChkLockOpRgAf.addItemListener(this);
        ChkVBon2BarKasse = new JRtaCheckBoxR("Verkäufe gegen Barzahlung (Bondruck) in Barkasse buchen");
        ChkVBon2BarKasse.addItemListener(this);
        ChkVRhg2BarKasse = new JRtaCheckBoxR("Ausbuchen von Verkaufsrechnungen an Barkasse erlauben");
        ChkVRhg2BarKasse.addItemListener(this);

        activateListener();
    }

    private void setFields() {
        ChkLockOP.setSelected(lockSettingsOP);
        ChkOP2BarKasse.showLocked(enableOP2BarKasse, lockSettingsOP_ini);

        ChkLockOpRgAf.setSelected(lockSettingsOpRgAf);
        ChkVRhg2BarKasse.showLocked(enableVRhg2BarKasse, lockSettingsOpRgAf);

        ChkVBon2BarKasse.showLocked(enableVBon2BarKasse, true); // besser in SysUtilVerkauf!
        //ChkVBon2BarKasse.setSelected(enableVBon2BarKasse);

    }

    private JPanel getContent() {
        // 1 2 3 4 5 6 7 8
        String xwerte = "15dlu, 3dlu, 60dlu, 5dlu, 40dlu:g, 9dlu, 3dlu, 15dlu";
        // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20
        String ywerte = "5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p,"
                // 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40
                + "5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p,"
                // 41 42 43 44 45 46 47 48 49
                + "5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu";
        FormLayout lay = new FormLayout(xwerte, ywerte);

        PanelBuilder builder = new PanelBuilder(lay);
        // PanelBuilder builder = new PanelBuilder(lay, new FormDebugPanel()); // debug
        // mode
        builder.getPanel()
               .setOpaque(false);

        CellConstraints cc = new CellConstraints();

        int colLeft = 2, colRight = 5, rowCnt = 2;

        builder.addSeparator("Privat- und Kassenabrechnung (Offene Posten / Mahnwesen)", cc.xyw(1, rowCnt++, 7));
        lab = new JLabel("Voreinstellung für OP-Suche u. nachfolgende Einträge gegen Ändern gesperrt");
        builder.add(lab, cc.xyw(colLeft, ++rowCnt, 5));
        builder.add(ChkLockOP, cc.xy(6, rowCnt++));
        ChkLockOP.setToolTipText("Wenn nicht gesperrt, wird das zuletzt verwandte Suchkriterium gespeichert.");

        rowCnt++; // 6
        builder.add(ChkOP2BarKasse, cc.xyw(colRight, rowCnt++, 2));

        rowCnt += 3; // 10
        builder.addSeparator("OP Rezeptgebühr-/Ausfall-/Verkaufsrechnung", cc.xyw(1, rowCnt++, 7));
        lab = new JLabel("Voreinstellung für OpRgAf-Suche u. nachfolgende Einträge gegen Ändern gesperrt");
        builder.add(lab, cc.xyw(colLeft, ++rowCnt, 5));
        builder.add(ChkLockOpRgAf, cc.xy(6, rowCnt++));
        ChkLockOpRgAf.setToolTipText("<html>Wenn nicht gesperrt, werden das zuletzt verwandte Suchkriterium <br>"
                + " und die selektierten Rechnungsarten gespeichert.</html>");

        rowCnt++; // 14
        builder.add(ChkVRhg2BarKasse, cc.xyw(colRight, rowCnt++, 2));

        rowCnt += 3; // 18
        builder.addSeparator("Verkaufs-Bon", cc.xyw(1, rowCnt++, 7));

        rowCnt++; // 20
        builder.add(ChkVBon2BarKasse, cc.xyw(colRight, rowCnt++, 2));

        return builder.getPanel();
    }

    private void activateListener() {
        al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (arg0.getActionCommand()
                        .equals("speicher")) {
                    doSpeichern();
                    initForm();
                } else if (arg0.getActionCommand()
                               .equals("abbrechen")) {
                    SystemInit.abbrechen();
                    return;
                }
            }
        };
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        // TODO Auto-generated method stub
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();

        if (source == ChkOP2BarKasse) {
            enableOP2BarKasse = (e.getStateChange() == ItemEvent.SELECTED ? true : false);
        }
        if (source == ChkVBon2BarKasse) {
            enableVBon2BarKasse = (e.getStateChange() == ItemEvent.SELECTED ? true : false);
        }
        if (source == ChkVRhg2BarKasse) {
            enableVRhg2BarKasse = (e.getStateChange() == ItemEvent.SELECTED ? true : false);
        }
        if (source == ChkLockOP) {
            lockSettingsOP = (e.getStateChange() == ItemEvent.SELECTED ? true : false);
        }
        if (source == ChkLockOpRgAf) {
            lockSettingsOpRgAf = (e.getStateChange() == ItemEvent.SELECTED ? true : false);
        }
        setFields();
        validate();
    }

    private void readFromIni() { // noch umstellen auf iniOP-Klasse! (analog iniOpRgAf)
        INIFile iniOP = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/",
                "offeneposten.ini");
        OpRgAfIni iniOpRgAf = new OpRgAfIni(Path.Instance.getProghome(), "ini/", Reha.getAktIK(), "oprgaf.ini");

        String section = "offenePosten";
        if (iniOP.getStringProperty(section, "lockSettings") != null) { // Eintrag in ini vorhanden?
            lockSettingsOP_ini = lockSettingsOP = iniOP.getBooleanProperty(section, "lockSettings");
        } else {
            lockSettingsOP_ini = lockSettingsOP = true; // Default-Wert setzen
        }
        enableOP2BarKasse = iniOP.getBooleanProperty(section, "erlaubeBarzahlung");

        enableVBon2BarKasse = iniOpRgAf.getVbCashAllowed();
        enableVRhg2BarKasse = iniOpRgAf.getVrCashAllowed();
        lockSettingsOpRgAf_ini = lockSettingsOpRgAf = iniOpRgAf.getSettingsLocked();
    }

    private void doSpeichern() { // noch umstellen auf iniOP-Klasse! (analog iniOpRgAf)
        String section = "offenePosten";
        boolean saveChanges = false;

        try {
            INIFile iniOP = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/",
                    "offeneposten.ini");

            if (enableOP2BarKasse != iniOP.getBooleanProperty(section, "erlaubeBarzahlung")) {
                iniOP.setBooleanProperty(section, "erlaubeBarzahlung", enableOP2BarKasse,
                        "Rechnungen duerfen in Barkasse gebucht werden");
                saveChanges = true;
            }

            if (lockSettingsOP != lockSettingsOP_ini) {
                iniOP.setBooleanProperty(section, "lockSettings", lockSettingsOP,
                        "Aktualisieren der Eintraege gesperrt");
                saveChanges = true;
            }
            if (saveChanges) {
                INITool.saveIni(iniOP);
                JOptionPane.showMessageDialog(null, "Änderungen in offeneposten.ini gespeichert.");
            }
        } catch (HeadlessException e) {
            JOptionPane.showMessageDialog(null, "Fehler beim speichern der Konfiguration in offeneposten.ini!!!");
        }

        try {
            saveChanges = false;
            OpRgAfIni iniOpRgAf = new OpRgAfIni(Path.Instance.getProghome(), "ini/", Reha.getAktIK(), "oprgaf.ini");

            iniOpRgAf.setVbCashAllowed(enableVBon2BarKasse);
            iniOpRgAf.setVrCashAllowed(enableVRhg2BarKasse);
            if (iniOpRgAf.saveLastCashSettings()) {
                saveChanges = true;
            }

            if (lockSettingsOpRgAf != lockSettingsOpRgAf_ini) {
                iniOpRgAf.setSettingsLocked(lockSettingsOpRgAf);
                if (iniOpRgAf.saveLockSettings()) {
                    saveChanges = true;
                }
            }
            if (saveChanges) {
                JOptionPane.showMessageDialog(null, "Änderungen in oprgaf.ini gespeichert.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Fehler beim speichern der Konfiguration in oprgaf.ini!!!");
        }

    }
}
