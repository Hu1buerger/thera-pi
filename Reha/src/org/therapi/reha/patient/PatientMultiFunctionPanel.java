package org.therapi.reha.patient;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.sql.Connection;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;

import org.jdesktop.swingx.JXPanel;
import org.therapi.reha.patient.verlauf.VerlaufModul;

import com.jgoodies.looks.windows.WindowsTabbedPaneUI;

import hauptFenster.Reha;
import mandant.IK;
import umfeld.Betriebsumfeld;

public class PatientMultiFunctionPanel extends JXPanel {

    private static final long serialVersionUID = -1284209871875228012L;
    PatientHauptPanel patientHauptPanel = null;
    private VerlaufModul verlaufModul;

    public PatientMultiFunctionPanel(PatientHauptPanel patHauptPanel, Connection connection) {
        super();
        setLayout(new BorderLayout());
        setOpaque(false);
        this.patientHauptPanel = patHauptPanel;
        add(getTabs(connection), BorderLayout.CENTER);
    }

    public void fireAufraeumen() {
        patientHauptPanel = null;
    }

    private synchronized JXPanel getTabs(Connection connection) {
        JXPanel rechts = new JXPanel(new BorderLayout());
        rechts.setOpaque(false);
        rechts.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        JTabbedPane jTabbedPane = new JTabbedPane();
        patientHauptPanel.multiTab = jTabbedPane;


        try {
            patientHauptPanel.multiTab.setUI(new WindowsTabbedPaneUI());
        } catch (Exception ex) {

        }

        JXPanel tabpan = new JXPanel(new BorderLayout());
        tabpan.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tabpan.setOpaque(true);
        tabpan.setBackgroundPainter(Reha.instance.compoundPainter.get("getTabs2"));

        tabpan.add(patientHauptPanel.aktRezept);
        patientHauptPanel.multiTab.addTab(patientHauptPanel.tabTitel[0] + " - 0", tabpan);
        patientHauptPanel.multiTab.setMnemonicAt(0, KeyEvent.VK_A);

        patientHauptPanel.historie = new Historie();
        patientHauptPanel.multiTab.addTab(patientHauptPanel.tabTitel[1] + " - 0", patientHauptPanel.historie);
        patientHauptPanel.multiTab.setMnemonicAt(1, KeyEvent.VK_H);

        patientHauptPanel.berichte = new TherapieBerichte();
        patientHauptPanel.multiTab.addTab(patientHauptPanel.tabTitel[2] + " - 0", patientHauptPanel.berichte);
        patientHauptPanel.multiTab.setMnemonicAt(2, KeyEvent.VK_T);

        patientHauptPanel.dokumentation = new DokumentationPanel();
        patientHauptPanel.multiTab.addTab(patientHauptPanel.tabTitel[3] + " - 0", patientHauptPanel.dokumentation);
        patientHauptPanel.multiTab.setMnemonicAt(3, KeyEvent.VK_D);

        patientHauptPanel.gutachten = new Gutachten();
        patientHauptPanel.multiTab.addTab(patientHauptPanel.tabTitel[4] + " - 0", patientHauptPanel.gutachten);
        patientHauptPanel.multiTab.setMnemonicAt(4, KeyEvent.VK_G);

        verlaufModul = new VerlaufModul(new IK(Betriebsumfeld.getAktIK()));
        jTabbedPane.add("Verlauf", verlaufModul.component());

        rechts.add(patientHauptPanel.multiTab, BorderLayout.CENTER);
        rechts.revalidate();
        return rechts;
    }

    public AktuelleRezepte getAktRez() {
        return patientHauptPanel.aktRezept;
    }

    public VerlaufModul verlaufModul() {
        return verlaufModul;
    }

}
