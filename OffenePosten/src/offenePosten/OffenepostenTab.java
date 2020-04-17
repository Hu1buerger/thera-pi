package offenePosten;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.JXPanel;

import com.jgoodies.looks.windows.WindowsTabbedPaneUI;

import CommonTools.DatFunk;
import rehaBillEdit.RehaBillPanel;

public class OffenepostenTab extends JXPanel implements ChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -6012301447745950357L;

    private Vector<String> vectitel = new Vector<String>();
    private Vector<String> vecdescript = new Vector<String>();
    private Vector<ImageIcon> vecimg = new Vector<ImageIcon>();

    public JTabbedPane jtb;
    public JXHeader jxh;

    OffenepostenPanel oppanel = null;
    OffenepostenMahnungen omahnpanel = null;
    OffenepostenEinstellungen oeinstellungpanel = null;
    RehaBillPanel rehaBillPanel = null;

    // @VisibleForTesting
    OffenepostenTab(String testIdent) {
        if ( !testIdent.contentEquals("JUnit")) {
            System.out.println("Attention! This method was created for Unit-testing and nothing else!");
            return;
        }
    }

    public OffenepostenTab(OffenePosten offenePosten) {
        super();
        setOpaque(false);
        setLayout(new BorderLayout());
        jtb = new JTabbedPane();
        jtb.setUI(new WindowsTabbedPaneUI());

        oppanel = new OffenepostenPanel(this, offenePosten);
        jtb.addTab("Rechnungen ausbuchen", oppanel);

        rehaBillPanel = new RehaBillPanel(this,offenePosten.conn);
        jtb.addTab("Rechn. korrigieren / - Kopie", rehaBillPanel);

        omahnpanel = new OffenepostenMahnungen(this , offenePosten);
        jtb.addTab("Mahnungen erstellen", omahnpanel);

        oeinstellungpanel = new OffenepostenEinstellungen(this);
        jtb.addTab("Einstellungen", oeinstellungpanel);

        jtb.addChangeListener(this);
        doHeader();
        jxh = new JXHeader();
        ((JLabel) jxh.getComponent(1)).setVerticalAlignment(JLabel.NORTH);
        add(jxh, BorderLayout.NORTH);
        add(jtb, BorderLayout.CENTER);

        jxh.validate();
        jtb.validate();
        validate();

    }

    public void setHeader(int header) {
        jxh.setTitle(vectitel.get(header));
        jxh.setDescription(vecdescript.get(header));
        jxh.setIcon(vecimg.get(header));
        jxh.validate();
    }

    private void doHeader() {
        ImageIcon ico;
        String ss = System.getProperty("user.dir") + File.separator + "icons" + File.separator + "hauptbuch.jpg";
        ico = new ImageIcon(ss);
        vectitel.add("Bezahlte Rechnungen ausbuchen / Teilzahlungen buchen");
        vecdescript.add(
                "<html>Hier haben Sie die Möglichkeit Rechnungen nach verschiedenen Kriterien zu suchen.<br><br>"
                        + "Wenn Sie die Rechnung, die Sie suchen, gefunden haben und die Rechnung <b>vollständig bezahlt</b> wurde,<br>"
                        + "genügt es völlig über <b>Alt+A</b> den Vorgang <b>Ausbuchen</b> zu aktivieren.<br><br>"
                        + "Wurde lediglich eine <b>Teilzahlung</b> geleistet, muß diese zuvor im Textfeld <b>Geldeingang</b> eingetragen werden.</html>");
        vecimg.add(ico);

        vectitel.add("Rechnung korrigieren / Kopie erstellen");
        vecdescript.add("Hier können Sie die Details der gewählten Rechnung einsehen,\n"
                + "Rechnungsdaten korrigieren (bei Abrechnungen nach §302 nur eingeschränkt)\n"
                + "und Kopien der Rechnung drucken.");
        vecimg.add(ico);

        vectitel.add("Mahnwesen");
        vecdescript.add("<html>Hier erzeugen Sie Mahnungen für noch nicht bezahlte Rechnungen.<br><br>"
                + "Button <b>[los..]</b> listet die Rechnungen, bei denen noch ein Betrag offen ist und<br>"
                + "die in der eingestellten Mahnstufe noch nicht gemahnt wurden.</html>");
        ss = System.getProperty("user.dir") + File.separator + "icons" + File.separator + "Mahnung.png";
        ico = new ImageIcon(ss);
        vecimg.add(ico);

        vectitel.add("Voreinstellungen für das Mahnwesen");
        vecdescript.add("Hier sind die Grundeinstellungen\n" + "der Mahnfunktion zu finden.");
        ss = System.getProperty("user.dir") + File.separator + "icons" + File.separator + "einstellungen.jpg";
        ico = new ImageIcon(ss);
        vecimg.add(ico);
        /*
         * vectitel.add("Rezeptgebührrechungen / Ausfallrechnungen");
         * vecdescript.add("....Experimentierpanal von Bodo und Jürgen.\n" +
         * "Hier werden die Funktionen die später Nebraska zu dem machen was Nebraske ist\n"
         * + "entwickelt und getestet"); vecimg.add(ico);
         */

    }

    @Override
    public void stateChanged(ChangeEvent arg0) {
        JTabbedPane pane = (JTabbedPane) arg0.getSource();
        int sel = pane.getSelectedIndex();
        try {
            if (sel == 0) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        oppanel.setzeFocus();
                    }
                });

            } else if (sel == 1) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        rehaBillPanel.setzeFocus();
                    }
                });

            }
        } catch (Exception ex) {

        }
        jxh.setTitle(vectitel.get(sel));
        jxh.setDescription(vecdescript.get(sel));
        jxh.setIcon(vecimg.get(sel));
    }

    public void setFirstFocus() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                oppanel.setzeFocus();
            }
        });
    }

    public void setOnBillPanel(String suchkrit) {
        rehaBillPanel.setOnBillPanel(suchkrit);
    }

    public String getNotBefore() {
        try {
            return DatFunk.sDatInSQL(oeinstellungpanel.tfs[4].getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Fehler beim Bezug des Startdatums, nehme 01.01.1995");
        }
        return "1995-01-01";
    }

    public int getFrist(int frist) {
        if (frist == 1) {
            try {
                return Integer.parseInt(oeinstellungpanel.tfs[0].getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Fehler beim Bezug der Frist Tage für Mahnstufe 1, nehme 31 Tage");
            }
            return 31;
        }
        if (frist == 2) {
            try {
                return Integer.parseInt(oeinstellungpanel.tfs[1].getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Fehler beim Bezug der Frist Tage für Mahnstufe 1, nehme 11 Tage");
            }
            return 11;
        }
        if (frist == 3) {
            try {
                return Integer.parseInt(oeinstellungpanel.tfs[2].getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Fehler beim Bezug der Frist Tage für Mahnstufe 3, nehme 11 Tage");
            }
            return 11;
        }

        return -1;
    }

    public void refreshData() {
        oppanel.refreshData();
    }

}
