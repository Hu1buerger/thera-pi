package org.therapi.reha.patient;

// Lemmi 20101212: zusätzliche Imports: Die letzte benutzte Suchart aus der INI-Datei holen und setzen
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.util.Arrays;
import java.util.TooManyListenersException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import systemEinstellungen.SystemConfig;

public class PatientToolBarPanel extends JXPanel {
    /**
     *
     */
    private static final long serialVersionUID = 8491959397526727602L;
    PatientHauptPanel patientHauptPanel = null;
    PatientToolBarLogic patToolLogic = null;
    public JLabel sucheLabel = null;
    private String kriterium[] = { "Nachname Vorname", "Telefon", "Notizen", "Patienten-ID",
            "volle, nicht abgeschlossene Rezepte", "abgebrochene Rezepte (> 21 Tage inaktiv)", "Patienten mit aktuellen Rezepten" };

    private String toolTipTexte[] = {
            "<html>Sucheingabe (Anfangsbuchstaben von):<br><br>Nachname[Leerzeichen]Vorname<br>&nbsp;&nbsp;oder<br>Nachname<br>&nbsp;&nbsp;oder<br>[Leerzeichen]Vorname</html>",
            "enthaltene Ziffernfolge eingeben",
            "enthaltene Buchstabenfolge eingeben",
            "ID eingeben",
            "kein Filter m\u00f6glich",
            "kein Filter m\u00f6glich",
            "kein Filter m\u00f6glich" };
    public JComboBox suchKrteriumCbBox;

    public String getKritAsString(int idx) {
        return kriterium[idx];
    }

    public int getNnVnIdx() {
        return Arrays.asList(kriterium)
                     .indexOf("Nachname Vorname");
    }

    public int getPatIdIdx() {
        return Arrays.asList(kriterium)
                     .indexOf("Patienten-ID");
    }

    public int getTelIdx() {
        return Arrays.asList(kriterium)
                     .indexOf("Telefon");
    }

    public int getNoteIdx() {
        return Arrays.asList(kriterium)
                     .indexOf("Notizen");
    }

    public int getVolleVoIdx() {
        return Arrays.asList(kriterium)
                     .indexOf("volle, nicht abgeschlossene Rezepte");
    }

    public int getAbgebrVoIdx() {
        return Arrays.asList(kriterium)
                     .indexOf("abgebrochene Rezepte (> 21 Tage inaktiv)");
    }

    public int getAktRezIdx() {
        return Arrays.asList(kriterium)
                     .indexOf("Patienten mit aktuellen Rezepten");
    }

    public boolean getSucheOhneEingabe(int idxKriterium) {
        if (idxKriterium == getVolleVoIdx() || idxKriterium == getAbgebrVoIdx() || idxKriterium == getAktRezIdx()) {
            return true;
        }
        return false;
    }

    public String getToolTipText(int idx) {
        if (idx < toolTipTexte.length) {
            return toolTipTexte[idx];
        }
        return null;
    }


    public PatientToolBarPanel(PatientHauptPanel patHauptPanel, PatientHauptLogic patientHauptLogic) {
        super();
        setOpaque(false);
        this.patientHauptPanel = patHauptPanel;
        patToolLogic = new PatientToolBarLogic(patHauptPanel, this,patientHauptLogic);
        setBorder(BorderFactory.createLineBorder(Color.WHITE));
        FormLayout lay = new FormLayout(
                "3dlu,right:max(35dlu;p),3dlu,p,45dlu,fill:0:grow(0.10),0dlu ,right:max(39dlu;p),3dlu, p,45dlu,7dlu," +
                // 2-teSpalte (13) 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28
                        "right:max(39dlu;p),3dlu,p,90,fill:0:grow(0.60),0dlu,7dlu,right:max(39dlu;p),3dlu,p,40dlu,2dlu,p,50dlu,fill:0:grow(0.30),5dlu,10dlu",
                // 1 2 3 4 5 6 7 8 9 10 11
                "fill:0:grow(0.50),p,fill:0:grow(0.50)");
        CellConstraints cc = new CellConstraints();
        setLayout(lay);
        JLabel lbl = new JLabel("Kriterium:");
        add(lbl, cc.xy(2, 2));

        suchKrteriumCbBox = new JComboBox(kriterium);

         int suchart = SystemConfig.hmPatientenSuchenDlgIni.get("suchart");
        suchKrteriumCbBox.setSelectedIndex(suchart);
        String toolTipText = getToolTipText(suchart);

        suchKrteriumCbBox.setBackground(new Color(247, 209, 176));
        add(suchKrteriumCbBox, cc.xyw(4, 2, 8));
        suchKrteriumCbBox.addActionListener(patientHauptPanel.toolBarAction);
        suchKrteriumCbBox.addKeyListener(patientHauptPanel.toolBarKeys);
        suchKrteriumCbBox.setName("Suchkriterium");

        sucheLabel = new JLabel("finde Pat. -->");
        sucheLabel.setName("Suchen");
        sucheLabel.setIcon(SystemConfig.hmSysIcons.get("find"));
        sucheLabel.addMouseListener(patientHauptPanel.toolBarMouse);
        sucheLabel.addFocusListener(patientHauptPanel.toolBarFocus);

        patientHauptPanel.dropTargetListener = new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent e) {
                if (!patientHauptPanel.tfsuchen.getText()
                                               .equals("")) {
                    patientHauptPanel.tfsuchen.setText("");
                }
            }


            @Override
            public void drop(DropTargetDropEvent e) {
                try {
                    patientHauptPanel.patientLogic.starteSuche();
                } catch (Throwable t) {
                    t.printStackTrace();
                    System.out.println("Fehler***************1********");
                }
                e.dropComplete(true);
            }

        };
        add(sucheLabel, cc.xy(13, 2));
        patientHauptPanel.tfsuchen = new JFormattedTextField();
        patientHauptPanel.tfsuchen.setFont(new Font("Tahoma", Font.BOLD, 11));
        patientHauptPanel.tfsuchen.setOpaque(false);
        patientHauptPanel.tfsuchen.setForeground(new Color(136, 136, 136));
        patientHauptPanel.tfsuchen.setName("suchenach");
        patientHauptPanel.tfsuchen.addKeyListener(patientHauptPanel.toolBarKeys);
        patientHauptPanel.tfsuchen.addFocusListener(patientHauptPanel.toolBarFocus);
        patientHauptPanel.tfsuchen.setToolTipText(toolTipText);
        try {
            patientHauptPanel.tfsuchen.getDropTarget()
                                      .addDropTargetListener(patientHauptPanel.dropTargetListener);
        } catch (TooManyListenersException e1) {
            e1.printStackTrace();
        }
        add(patientHauptPanel.tfsuchen, cc.xyw(15, 2, 3));

        JToolBar jtb = new JToolBar();
        jtb.setRollover(true);
        jtb.setBorder(null);
        jtb.setOpaque(false);

        patientHauptPanel.jbut[0] = new JButton();
        patientHauptPanel.jbut[0].setIcon(SystemConfig.hmSysIcons.get("neu"));
        patientHauptPanel.jbut[0].setToolTipText("neuen Patient anlegen (Alt+N)");
        patientHauptPanel.jbut[0].setActionCommand("neu");
        patientHauptPanel.jbut[0].addActionListener(patientHauptPanel.toolBarAction);
        jtb.add(patientHauptPanel.jbut[0]);

        patientHauptPanel.jbut[1] = new JButton();
        patientHauptPanel.jbut[1].setIcon(SystemConfig.hmSysIcons.get("edit"));
        patientHauptPanel.jbut[1].setToolTipText("aktuellen Patient ändern/editieren (Alt+E)");
        patientHauptPanel.jbut[1].setActionCommand("edit");
        patientHauptPanel.jbut[1].addActionListener(patientHauptPanel.toolBarAction);
        jtb.add(patientHauptPanel.jbut[1]);

        patientHauptPanel.jbut[2] = new JButton();
        patientHauptPanel.jbut[2].setIcon(SystemConfig.hmSysIcons.get("delete"));
        patientHauptPanel.jbut[2].setToolTipText("Patient löschen (Alt+L)");
        patientHauptPanel.jbut[2].setActionCommand("delete");
        patientHauptPanel.jbut[2].addActionListener(patientHauptPanel.toolBarAction);
        jtb.add(patientHauptPanel.jbut[2]);

        jtb.addSeparator(new Dimension(30, 0));

        patientHauptPanel.jbut[3] = new JButton();
        patientHauptPanel.jbut[3].setIcon(SystemConfig.hmSysIcons.get("print"));
        patientHauptPanel.jbut[3].setToolTipText("Brief/Formular für Patient erstellen (Alt+B)");
        patientHauptPanel.jbut[3].setActionCommand("formulare");
        patientHauptPanel.jbut[3].addActionListener(patientHauptPanel.toolBarAction);
        jtb.add(patientHauptPanel.jbut[3]);

        jtb.addSeparator(new Dimension(30, 0));

        patientHauptPanel.jbut[4] = new JButton();
        patientHauptPanel.jbut[4].setIcon(SystemConfig.hmSysIcons.get("tools"));
        patientHauptPanel.jbut[4].setToolTipText("Werkzeugkiste für aktuellen Patient");
        patientHauptPanel.jbut[4].setActionCommand("werkzeuge");
        patientHauptPanel.jbut[4].addActionListener(patientHauptPanel.toolBarAction);
        jtb.add(patientHauptPanel.jbut[4]);

        add(jtb, cc.xyw(20, 2, 8));
    }

    public PatientToolBarLogic getLogic() {
        return patToolLogic;
    }

}
