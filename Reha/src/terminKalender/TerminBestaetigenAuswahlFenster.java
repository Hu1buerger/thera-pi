package terminKalender;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.JRtaCheckBox;
import dialoge.PinPanel;
import dialoge.RehaSmartDialog;
import events.RehaTPEvent;
import events.RehaTPEventClass;
import hauptFenster.Reha;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemPreislisten;
import systemTools.ListenerTools;
import systemTools.WinNum;

//Drud 110418
//TODO 6. Anpassung des Umsatzbeteiligung-Moduls, um nur die tatsächlich geleisteten Heilmittel anzuzeigen

public class TerminBestaetigenAuswahlFenster extends RehaSmartDialog implements ItemListener {
    private static final long serialVersionUID = -2972115133247099975L;
    /**
    *
    */
    private String eigenName = null;
    private JXPanel jcc = null;
    private JXPanel jpan = null;
    private RehaTPEventClass rtp = null;
    private JRtaCheckBox[] btm = { null, null, null, null }; // Welche Position soll bestätigt werden?
    private JXLabel[] AnzTermine = { null, null, null, null }; // Bereits geleistete Therapien
    private JXLabel[] AnzRezept = { null, null, null, null }; // Max. Therapien lt. VO
    private JXLabel[] HMPosNr = { null, null, null, null }; // Positionsnummer
    private JXLabel[] SpaltenUeberschrift = { null, null, null, null };
    private JXButton okbut;
    private JXButton abbruchbut;
    private Vector<BestaetigungsDaten> hMPosLC = null;
    private int anzahlPos;

    public TerminBestaetigenAuswahlFenster(JXFrame owner, String name, Vector<BestaetigungsDaten> hMPos, String reznum,
            int preisgruppe) {
        super(owner, "Eltern-TermBest" + WinNum.NeueNummer());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        hMPosLC = hMPos;
        anzahlPos = hMPosLC.size();

        for (int i = 0; i < 4; i++) {
            btm[i] = new JRtaCheckBox("");
            if (i < anzahlPos) {
                btm[i].setName(Integer.toString(i));
                HMPosNr[i] = new JXLabel("HMPos");
                AnzTermine[i] = new JXLabel("geleistet_Menge");
                AnzRezept[i] = new JXLabel("VO_Menge");

                btm[i].setEnabled((hMPosLC.get(i).anzBBT < hMPosLC.get(i).vOMenge) ? true : false);

                HMPosNr[i].setText(
                        RezTools.getKurzformFromPos(hMPosLC.get(i).hMPosNr, Integer.toString(preisgruppe - 1),
                                SystemPreislisten.hmPreise.get(RezTools.getDisziplinFromRezNr(reznum))
                                                          .get(preisgruppe - 1)));
                // HMPosNr[i].setText(hMPosLC.get(i).hMPosNr);
                AnzTermine[i].setText(Integer.toString(hMPosLC.get(i).anzBBT));
                AnzRezept[i].setText(Integer.toString(hMPosLC.get(i).vOMenge));

                btm[i].addItemListener(this);
                btm[i].setSelected(hMPosLC.get(i).best); // TODO TerminFenster hat bereits eine Vorauswahl getroffen
                                                         // (z.B. Doppelbehandlungen)!
                btm[i].addKeyListener(this);
                btm[i].setSelected((hMPosLC.get(i).anzBBT < hMPosLC.get(i).vOMenge) ? true : false);
            }
        }
        SpaltenUeberschrift[0] = new JXLabel("bestätigen>");
        SpaltenUeberschrift[1] = new JXLabel("Heilmittel");
        SpaltenUeberschrift[2] = new JXLabel("geleistet");
        SpaltenUeberschrift[3] = new JXLabel("VO-Menge");

        setPreferredSize(new Dimension(240, 250));

        eigenName = "TermBest" + WinNum.NeueNummer();
        this.setName(eigenName);
        getSmartTitledPanel().setPreferredSize(new Dimension(240, 220));
        getSmartTitledPanel().setName("Eltern-" + eigenName);
        this.getParent()
            .setName("Eltern-" + eigenName);

        this.setUndecorated(true);
        this.addWindowListener(this);
        this.addKeyListener(this);

        jcc = new JXPanel(new BorderLayout());
        jcc.setDoubleBuffered(true);
        jcc.setName(eigenName);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                jcc.setBackgroundPainter(Reha.instance.compoundPainter.get("TerminBestaetigenAuswahlFenster"));
                return null;
            }
        }.execute();

        jcc.setBorder(null);
        jcc.addKeyListener(this);

        this.setContentPanel(jcc);

        getSmartTitledPanel().setTitle("Leistung bestätigen");
        getSmartTitledPanel().getContentContainer()
                             .setName(eigenName);
        getSmartTitledPanel().addKeyListener(this);
        getSmartTitledPanel().validate();
        PinPanel pinPanel = new PinPanel();
        pinPanel.getGruen()
                .setVisible(false);
        pinPanel.setName(eigenName);
        pinPanel.setzeName(eigenName);
        pinPanel.addKeyListener(this);

        setPinPanel(pinPanel);

        rtp = new RehaTPEventClass();
        rtp.addRehaTPEventListener(this);
        jcc.add(getTerminBest(jpan = new JXPanel()), BorderLayout.CENTER);
        jcc.add(getButtonBest(), BorderLayout.SOUTH);
        jpan.validate();
        jcc.validate();

        // this.setAlwaysOnTop(true); //gefährlich in Java, außer in begründeten
        // Ausnahmefenstern eigentlich nur anzuwenden bei NON-Modalen Fenstern
        this.setModal(true);
        validate();
        getRootPane().setDefaultButton(okbut);

    }

    private JXPanel getTerminBest(JXPanel jp) {
        // 1 2 3 4 5 6 7 8 9 10 11 12 13 14
        FormLayout lay = new FormLayout("6px,center:p,6px,right:p,6px,right:p,6px,right:p,6px,66px,6px,p,6px,p",
                // 1. 2. 3. 4. 5. 6. 7. 8. 9 10 11 12 13 14 15 16
                "6px, p, 6dlu:g, p ,6dlu,p,6dlu,p,6dlu,p,6dlu:g"); /* ,6dlu,p,6dlu,p,6dlu,p */
        jp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jp.setBackground(Color.WHITE);
        // jp.setOpaque(false); // mit weiß ist es passend zu den anderen Terminkalender
        // Optionsfenster
        jp.setLayout(lay);
        CellConstraints cc = new CellConstraints();
        /*
        */
        /***/
        for (int k = 0; k < 4; k++) {
            jp.add(SpaltenUeberschrift[k], cc.xy(2 + 2 * k, 2));
            if (k < anzahlPos) {
                jp.add(btm[k], cc.xy(2, 4 + 2 * k));
                jp.add(HMPosNr[k], cc.xy(4, 4 + 2 * k));
                jp.add(AnzTermine[k], cc.xy(6, 4 + 2 * k));
                jp.add(AnzRezept[k], cc.xy(8, 4 + 2 * k));
            }
        }

        jp.addKeyListener(this);
        jp.validate();
        return jp;
    }

    private JXPanel getButtonBest() {
        String xwert = "fill:0:grow(0.5),50dlu,10dlu,50dlu,fill:0:grow(0.5)";
        String ywert = "5dlu,p,5dlu";
        FormLayout lay = new FormLayout(xwert, ywert);
        CellConstraints cc = new CellConstraints();
        JXPanel pan = new JXPanel();
        pan.setLayout(lay);
        okbut = new JXButton(oKAction);
        okbut.setActionCommand("ok");

        abbruchbut = new JXButton(cancelAction);
        abbruchbut.setActionCommand("abbruch");
        abbruchbut.getInputMap(JComponent.WHEN_FOCUSED)
                  .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "abbruch");
        abbruchbut.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                  .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "abbruch");
        abbruchbut.getActionMap()
                  .put("abbruch", cancelAction);

        pan.add(okbut, cc.xy(2, 2));
        pan.add(abbruchbut, cc.xy(4, 2));
        pan.validate();

        return pan;
    }

    private Action oKAction = new AbstractAction("ok") {

        @Override
        public void actionPerformed(ActionEvent e) {
            zurueck();

        }
    };
    private Action cancelAction = new AbstractAction("abbrechen") {

        @Override
        public void actionPerformed(ActionEvent e) {
            reset();

        }
    };

    private void zurueck() {
        int counter = 0;
        for (int i = 0; i < btm.length; i++) {
            counter += (btm[i].isSelected() ? 1 : 0);
        }
        if (counter != 0) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < anzahlPos; i++) {
                        hMPosLC.get(i).best = btm[i].isSelected();
                    }
                    RezTools.DIALOG_WERT = RezTools.DIALOG_OK;
                    setVisible(false);
                    dispose();
                }
            });
        } else {
            JOptionPane.showMessageDialog(null, "Sie haben noch keine Heilmittelposition ausgewählt!");
        }
    }

    private void reset() {
        for (int i = 0; i < btm.length; i++) {
            if (i < anzahlPos) {
                hMPosLC.get(i).best = false;
            }
        }
        RezTools.DIALOG_WERT = RezTools.DIALOG_ABBRUCH;
        setVisible(false);
        this.dispose();
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
        if (okbut != null) {
            ListenerTools.removeListeners(okbut);
            okbut = null;
        }
        if (abbruchbut != null) {
            ListenerTools.removeListeners(abbruchbut);
            abbruchbut = null;
        }
        ListenerTools.removeListeners(this);
        if (jcc != null) {
            ListenerTools.removeListeners(jcc);
            jcc = null;
        }
    }

    @Override
    public void rehaTPEventOccurred(RehaTPEvent evt) {
        String ss = this.getName();
        try {
            if (evt.getDetails()[0].equals(ss) && evt.getDetails()[1] == "ROT") {
                this.dispose();
                rtp.removeRehaTPEventListener(this);
                rtp = null;
            }
        } catch (NullPointerException ne) {
            // ignore
        }
    }

    @Override
    public void itemStateChanged(ItemEvent arg0) {
        int chkBoxNr = -1;
        try { // was ist wenn eine Componente ItemChanged feuert, deren Name sich nicht zu
              // einem Integer parsen lässt?
              // das ist dann Murks, bzw. wirft zurecht eine Exception, deshalb arbeite ich
              // für solche Aufgaben wesentlich
              // lieber mit dem ActioListener
            chkBoxNr = Integer.parseInt(((JComponent) arg0.getSource()).getName());
        } catch (Exception Ex) {
            System.out.println(Ex);
        }
        if (arg0.getStateChange() == ItemEvent.SELECTED) {
            AnzTermine[chkBoxNr].setText(Integer.toString(Integer.parseInt(AnzTermine[chkBoxNr].getText()) + 1));
            AnzTermine[chkBoxNr].setForeground(Color.BLUE);
        } else {
            AnzTermine[chkBoxNr].setText(Integer.toString(Integer.parseInt(AnzTermine[chkBoxNr].getText()) - 1));
            AnzTermine[chkBoxNr].setForeground(Color.BLACK);
        }
        validate();
    }

}
