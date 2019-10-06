package hauptFenster;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyVetoException;
import java.sql.Connection;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.therapi.reha.patient.PatientHauptPanel;

import CommonTools.DatFunk;
import abrechnung.AbrechnungGKV;
import abrechnung.AbrechnungReha;
import anmeldungUmsatz.Anmeldungen;
import anmeldungUmsatz.Umsaetze;
import arztFenster.ArztPanel;
import barKasse.Barkasse;
import benutzerVerwaltung.BenutzerRechte;
import dialoge.PinPanel;
import dialoge.RehaSmartDialog;
import dta301.Dta301;
import entlassBerichte.EBerichtPanel;
import environment.Path;
import events.RehaTPEvent;
import gui.Cursors;
import hauptFenster.login.LoginSmartDialog;
import krankenKasse.KassenPanel;
import rechteTools.Rechte;
import rehaContainer.RehaTP;
import rehaInternalFrame.JAbrechnungInternal;
import rehaInternalFrame.JAnmeldungenInternal;
import rehaInternalFrame.JArztInternal;
import rehaInternalFrame.JBarkassenInternal;
import rehaInternalFrame.JBenutzerInternal;
import rehaInternalFrame.JBeteiligungInternal;
import rehaInternalFrame.JDta301Internal;
import rehaInternalFrame.JGutachtenInternal;
import rehaInternalFrame.JKasseInternal;
import rehaInternalFrame.JPatientInternal;
import rehaInternalFrame.JRehaInternal;
import rehaInternalFrame.JRehaabrechnungInternal;
import rehaInternalFrame.JSysteminitInternal;
import rehaInternalFrame.JTerminInternal;
import rehaInternalFrame.JUmsaetzeInternal;
import rehaInternalFrame.JUrlaubInternal;
import rehaInternalFrame.JVerkaufInternal;
import roogle.RoogleFenster;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemInit;
import systemTools.WinNum;
import terminKalender.TerminFenster;
import urlaubBeteiligung.Beteiligung;
import urlaubBeteiligung.Urlaub;
import verkauf.VerkaufTab;

public class ProgLoader {
    public JPatientInternal patjry = null;
    public JGutachtenInternal gutjry = null;
    public RoogleFenster roogleDlg = null;
    public JArztInternal arztjry = null;
    public JKasseInternal kassejry = null;
    public JTerminInternal terminjry = null;
    public JAbrechnungInternal abrechjry = null;
    public JAnmeldungenInternal anmeldungenjry = null;
    public JUmsaetzeInternal umsaetzejry = null;
    public JVerkaufInternal verkaufjry = null;
    public JBarkassenInternal barkassenjry = null;
    public JRehaabrechnungInternal rehaabrechnungjry = null;
    public JBeteiligungInternal beteiligungjry = null;
    public JUrlaubInternal urlaubjry = null;
    public JBenutzerInternal benutzerjry = null;
    public JSysteminitInternal systeminitjry = null;
    public JDta301Internal dta301jry = null;
//public static JTerminInternal tjry = null;
//public static JGutachtenInternal gjry = null;

    public ProgLoader() {

    }

    protected static RehaSmartDialog xsmart;
    private Connection connection;

    /************** Patient suchen (Test) **********************************/
    public static void ProgPatSuche(boolean setPos) {

    }

    /************** Terminkalender Echtfunktion ****************************/
    public void ProgTerminFenster(int setPos, int ansicht) {
        if (!Reha.DbOk) {
            return;
        }
        if (ansicht == 2) {
            if (!Rechte.hatRecht(Rechte.Masken_erstellen, true)) {
                return;
            }
        }
        JComponent termin = AktiveFenster.getFensterAlle("TerminFenster");
        if (termin != null) {
            if (ansicht == 2) {
                JOptionPane.showMessageDialog(null,
                        "Um die Wochenarbeitszeit zu starten,\nschließen Sie bitte zunächst den Terminkalender");
            }
            ////// System.out.println("Der Terminkalender befindet sich in Container
            ////// "+((JTerminInternal)termin).getDesktop());
            containerHandling(((JTerminInternal) termin).getDesktop());
            ((JTerminInternal) termin).aktiviereDiesenFrame(((JTerminInternal) termin).getName());
            if (((JTerminInternal) termin).isIcon()) {
                try {
                    ((JTerminInternal) termin).setIcon(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        final int xansicht = ansicht;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String name = "TerminFenster" + WinNum.NeueNummer();

                int containerNr = SystemConfig.hmContainer.get("Kalender");
                containerHandling(containerNr);
                LinkeTaskPane.thisClass.setCursor(Cursors.wartenCursor);
                terminjry = null;
                if (xansicht != 2) {
                    String stag = DatFunk.sHeute();
                    String titel = DatFunk.WochenTag(stag) + " " + stag + " -- KW: " + DatFunk.KalenderWoche(stag)
                            + " -- [Normalansicht]";
                    terminjry = new JTerminInternal(titel,
                            new ImageIcon(Path.Instance.getProghome() + "icons/calendar.png"), containerNr);
                } else {
                    terminjry = new JTerminInternal("Terminkalender - " + DatFunk.sHeute(),
                            new ImageIcon(Path.Instance.getProghome() + "icons/calendar.png"), containerNr);
                }
                terminjry.setName(name);
                ((JRehaInternal) terminjry).setImmerGross(
                        (SystemConfig.hmContainer.get("KalenderOpti") == 1 ? true : false));
                Reha.instance.terminpanel = new TerminFenster(connection);
                terminjry.setContent(Reha.instance.terminpanel.init(containerNr, xansicht, terminjry, connection));
                if (SystemConfig.hmContainer.get("KalenderOpti") == 1) {
                    terminjry.setLocation(new Point(0, 0));
                    // Reha.instance.jpOben muss noch ersetzt werden durch
                    // Reha.instance.desktops[containerNr]
                    // terminjry.setSize(new
                    // Dimension(Reha.instance.jpOben.getWidth(),Reha.instance.jpOben.getHeight()));
                    // terminjry.setPreferredSize(new
                    // Dimension(Reha.instance.jpOben.getWidth(),Reha.instance.jpOben.getHeight()));
                    terminjry.setSize(new Dimension(Reha.instance.desktops[containerNr].getWidth(),
                            Reha.instance.desktops[containerNr].getHeight()));
                    terminjry.setPreferredSize(new Dimension(Reha.instance.desktops[containerNr].getWidth(),
                            Reha.instance.desktops[containerNr].getHeight()));

                } else {
                    terminjry.setLocation(new Point(SystemConfig.hmContainer.get("KalenderLocationX"),
                            SystemConfig.hmContainer.get("KalenderLocationY")));
                    terminjry.setSize(new Dimension(SystemConfig.hmContainer.get("KalenderDimensionX"),
                            SystemConfig.hmContainer.get("KalenderDimensionY")));
                    terminjry.setPreferredSize(new Dimension(SystemConfig.hmContainer.get("KalenderDimensionX"),
                            SystemConfig.hmContainer.get("KalenderDimensionY")));
                }
                JTerminInternal.inIniSave = false;
                terminjry.pack();
                terminjry.setVisible(true);
                Reha.instance.desktops[containerNr].add(terminjry);
                LinkeTaskPane.thisClass.setCursor(Cursors.normalCursor);
                AktiveFenster.setNeuesFenster(name, terminjry, containerNr, Reha.instance.terminpanel.getViewPanel());
                terminjry.aktiviereDiesenFrame(terminjry.getName());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Reha.instance.terminpanel.getViewPanel()
                                                 .requestFocus();
                    }
                });
            }
        });

    }

    public void loescheTermine() {
        terminjry = null;
        Reha.instance.terminpanel = null;
    }

    /************** Roogle Echtfunktion ***********************/
    public void ProgRoogleFenster(int setPos, String droptext) {
        final String xdroptext = droptext;

        new Thread() {
            @Override
            public void run() {
                if (!Rechte.hatRecht(Rechte.Rugl_open, true)) {
                    return;
                }
                Reha.getThisFrame()
                    .setCursor(Cursors.wartenCursor);
                roogleDlg = new RoogleFenster(Reha.getThisFrame(), xdroptext);
                roogleDlg.setSize(940, 680);
                roogleDlg.setPreferredSize(new Dimension(940, 680));
                roogleDlg.setLocationRelativeTo(null);
                roogleDlg.pack();
                roogleDlg.setVisible(true);
                Reha.getThisFrame()
                    .setCursor(Cursors.normalCursor);
            }
        }.start();
    }

    public void loescheRoogle() {
        roogleDlg = null;
    }

    /************** Krankenkassenverwaltung Echtfunktion ***********************/
    public void KassenFenster(int setPos, String kid) {
        if (!Reha.DbOk) {
            return;
        }
        JComponent kasse = AktiveFenster.getFensterAlle("KrankenKasse");
        if (kasse != null) {
            containerHandling(((JKasseInternal) kasse).getDesktop());
            ((JKasseInternal) kasse).aktiviereDiesenFrame(((JKasseInternal) kasse).getName());
            ((JKasseInternal) kasse).starteKasseID(kid);
            if (((JKasseInternal) kasse).isIcon()) {
                try {
                    ((JKasseInternal) kasse).setIcon(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);
        String name = "KrankenKasse" + WinNum.NeueNummer();
        int containerNr = SystemConfig.hmContainer.get("Kasse");
        containerHandling(containerNr);
        kassejry = new JKasseInternal("thera-\u03C0 Krankenkassen-Verwaltung ",
                SystemConfig.hmSysIcons.get("kassenstamm"), containerNr);
        AktiveFenster.setNeuesFenster(name, kassejry, containerNr, kassejry.getContentPane());
        kassejry.setName(name);
        kassejry.setSize(new Dimension(650, 500));
        kassejry.setPreferredSize(new Dimension(650, 500));
        Reha.instance.kassenpanel = new KassenPanel(kassejry, kid);
        kassejry.setContent(Reha.instance.kassenpanel);
        kassejry.addComponentListener(Reha.instance);
        int comps = Reha.instance.desktops[containerNr].getComponentCount();
        kassejry.setLocation(comps * 10, comps * 10);
        kassejry.pack();
        kassejry.setVisible(true);
        Reha.instance.desktops[containerNr].add(kassejry);
        ((JRehaInternal) kassejry).setImmerGross((SystemConfig.hmContainer.get("KasseOpti") > 0 ? true : false));
        //// System.out.println("Anzahl Fenster =
        //// "+Reha.instance.desktops[containerNr].getComponentCount());
        kassejry.aktiviereDiesenFrame(kassejry.getName());
        Reha.getThisFrame()
            .setCursor(Cursors.normalCursor);
        Reha.instance.kassenpanel.setzeFocus();
    }

    public void loescheKasse() {
        kassejry = null;
        Reha.instance.kassenpanel = null;
    }

    /************** Ärzteverwaltung Echtfunktion ***********************/
    public void ArztFenster(int setPos, String aid) {
        if (!Reha.DbOk) {
            return;
        }
        JComponent arzt = AktiveFenster.getFensterAlle("ArztVerwaltung");
        if (arzt != null) {
            containerHandling(((JArztInternal) arzt).getDesktop());
            ((JArztInternal) arzt).aktiviereDiesenFrame(((JArztInternal) arzt).getName());
            ((JArztInternal) arzt).starteArztID(aid);
            if (((JArztInternal) arzt).isIcon()) {
                try {
                    ((JArztInternal) arzt).setIcon(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);
        String name = "ArztVerwaltung" + WinNum.NeueNummer();
        int containerNr = SystemConfig.hmContainer.get("Arzt");
        containerHandling(containerNr);
        // arztjry = new JArztInternal("thera-\u03C0 Ärzte-Verwaltung || F3 = Daten in
        // Zwischenablage | F2 = Daten aus
        // Zwischenablage",SystemConfig.hmSysIcons.get("arztstamm"),containerNr) ;
        arztjry = new JArztInternal("thera-\u03C0 Ärzte-Verwaltung", SystemConfig.hmSysIcons.get("arztstamm"),
                containerNr);
        AktiveFenster.setNeuesFenster(name, arztjry, containerNr, arztjry.getContentPane());
        arztjry.setName(name);
        arztjry.setSize(new Dimension(650, 500));
        arztjry.setPreferredSize(new Dimension(650, 500));
        Reha.instance.arztpanel = new ArztPanel(arztjry, aid);
        arztjry.setContent(Reha.instance.arztpanel);
        arztjry.addComponentListener(Reha.instance);
        int comps = Reha.instance.desktops[containerNr].getComponentCount();
        arztjry.setLocation(comps * 10, comps * 10);
        arztjry.pack();
        arztjry.setVisible(true);
        Reha.instance.desktops[containerNr].add(arztjry);
        ((JRehaInternal) arztjry).setImmerGross((SystemConfig.hmContainer.get("ArztOpti") > 0 ? true : false));
        //// System.out.println("Anzahl Fenster =
        //// "+Reha.instance.desktops[containerNr].getComponentCount());
        arztjry.aktiviereDiesenFrame(arztjry.getName());
        Reha.getThisFrame()
            .setCursor(Cursors.normalCursor);
        Reha.instance.arztpanel.setzeFocus();

    }

    public void loescheArzt() {
        arztjry = null;
        Reha.instance.arztpanel = null;
    }

    /************** Gutachten Echtfunktion ***********************/
    public void GutachenFenster(int setPos, String pat_intern, int berichtid, String berichttyp, boolean neu,
            String empfaenger, int uebernahmeid) {
        if (!Reha.DbOk) {
            return;
        }
        if (!Rechte.hatRecht(Rechte.Gutachten_anlegen, true)) {
            return;
        }
        JComponent gutachten = AktiveFenster.getFensterAlle("GutachtenFenster");
        if (gutachten != null) {
            containerHandling(((JGutachtenInternal) gutachten).getDesktop());
            ((JGutachtenInternal) gutachten).aktiviereDiesenFrame(((JGutachtenInternal) gutachten).getName());

            if (((JGutachtenInternal) gutachten).isIcon()) {
                try {
                    ((JGutachtenInternal) gutachten).setIcon(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);
        String name = "GutachtenFenster" + WinNum.NeueNummer();
        int containerNr = SystemConfig.hmContainer.get("Arzt");
        containerHandling(containerNr);
        gutjry = new JGutachtenInternal("thera-\u03C0 Gutachten ", SystemConfig.hmSysIcons.get("drvlogo"), containerNr);
        AktiveFenster.setNeuesFenster(name, gutjry, containerNr, gutjry.getContentPane());
        gutjry.setName(name);
        gutjry.setSize(new Dimension(900, Reha.instance.desktops[containerNr].getHeight() - 20));
        gutjry.setPreferredSize(new Dimension(900, Reha.instance.desktops[containerNr].getHeight() - 20));
        Reha.instance.eberichtpanel = new EBerichtPanel(gutjry, pat_intern, berichtid, berichttyp, neu, empfaenger,
                uebernahmeid, connection);
        gutjry.setContent(Reha.instance.eberichtpanel);
        gutjry.addComponentListener(Reha.instance);
        int comps = Reha.instance.desktops[containerNr].getComponentCount();
        gutjry.setLocation(comps * 10, comps * 10);
        gutjry.pack();
        gutjry.setVisible(true);
        Reha.instance.desktops[containerNr].add(gutjry);
        gutjry.aktiviereDiesenFrame(gutjry.getName());
        Reha.getThisFrame()
            .setCursor(Cursors.normalCursor);
    }

    public void loescheGutachten() {
        gutjry = null;
        Reha.instance.eberichtpanel = null;
    }

    /**
     * @param connection
     ****************************************/
    public void AbrechnungFenster(int setPos, Connection connection) {
        if (!Reha.DbOk) {
            return;
        }
        if (!Rechte.hatRecht(Rechte.Funktion_kassenabrechnung, true)) {
            return;
        }
        JComponent abrech1 = AktiveFenster.getFensterAlle("Abrechnung");
        if (abrech1 != null) {
            //// System.out.println("InternalFrame Kassenabrechnung bereits geöffnet");
            containerHandling(((JAbrechnungInternal) abrech1).getDesktop());
            ((JAbrechnungInternal) abrech1).aktiviereDiesenFrame(((JAbrechnungInternal) abrech1).getName());
            if (((JAbrechnungInternal) abrech1).isIcon()) {
                try {
                    ((JAbrechnungInternal) abrech1).setIcon(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        // neuer Titel eingebaut
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);
        String name = "Abrechnung" + WinNum.NeueNummer();
        int containerNr = setPos;
        containerHandling(containerNr);
        abrechjry = new JAbrechnungInternal("thera-\u03C0  - Kassen-Abrechnung nach §302 ",
                SystemConfig.hmSysIcons.get("bomb24"), 1);
        AktiveFenster.setNeuesFenster(name, abrechjry, 1, abrechjry.getContentPane());
        abrechjry.setName(name);
        abrechjry.setSize(new Dimension(850, 700));
        abrechjry.setPreferredSize(new Dimension(850, 700));
        Reha.instance.abrechnungpanel = new AbrechnungGKV(abrechjry, connection);
        abrechjry.setContent(Reha.instance.abrechnungpanel);
        abrechjry.addComponentListener(Reha.instance);
        int comps = Reha.instance.desktops[containerNr].getComponentCount();
        abrechjry.setLocation(comps * 15, comps * 15);
        abrechjry.pack();
        abrechjry.setVisible(true);
        Reha.instance.desktops[containerNr].add(abrechjry);
        // ((JRehaInternal)abrechjry).setImmerGross(
        // (SystemConfig.hmContainer.get("ArztOpti") > 0 ? true : false));
        abrechjry.aktiviereDiesenFrame(abrechjry.getName());
        Reha.getThisFrame()
            .setCursor(Cursors.normalCursor);

    }

    public void loescheAbrechnung() {
        abrechjry = null;
        Reha.instance.abrechnungpanel = null;
    }

    /**********************
     * Neuanmeldungen
     * 
     * @param connection
     ****************************/
    public void AnmeldungenFenster(int setPos, String sparam, Connection connection) {
        this.connection = connection;
        if (!Reha.DbOk) {
            return;
        }
        if (!Rechte.hatRecht(Rechte.Funktion_neuanmeldungen, true)) {
            return;
        }
        JComponent anmeld = AktiveFenster.getFensterAlle("Anmeldungen");
        if (anmeld != null) {
            //// System.out.println("InternalFrame Anmeldungen bereits geöffnet");
            containerHandling(((JAnmeldungenInternal) anmeld).getDesktop());
            ((JAnmeldungenInternal) anmeld).aktiviereDiesenFrame(((JAnmeldungenInternal) anmeld).getName());
            if (((JAnmeldungenInternal) anmeld).isIcon()) {
                try {
                    ((JAnmeldungenInternal) anmeld).setIcon(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);
        String name = "Anmeldungen" + WinNum.NeueNummer();
        // int containerNr = SystemConfig.hmContainer.get("Arzt");
        int containerNr = setPos;
        containerHandling(containerNr);
        anmeldungenjry = new JAnmeldungenInternal("thera-\u03C0  - Ermittlung des Anmeldevolumens ",
                SystemConfig.hmSysIcons.get("arztstamm"), 1);
        AktiveFenster.setNeuesFenster(name, anmeldungenjry, 1, anmeldungenjry.getContentPane());
        anmeldungenjry.setName(name);
        anmeldungenjry.setSize(new Dimension(570, 500));
        anmeldungenjry.setPreferredSize(new Dimension(570, 500));
        Reha.instance.anmeldungenpanel = new Anmeldungen(anmeldungenjry, connection);
        anmeldungenjry.setContent(Reha.instance.anmeldungenpanel);
        anmeldungenjry.addComponentListener(Reha.instance);
        int comps = Reha.instance.desktops[containerNr].getComponentCount();
        anmeldungenjry.setLocation(comps * 15, comps * 15);
        anmeldungenjry.pack();
        anmeldungenjry.setVisible(true);
        Reha.instance.desktops[containerNr].add(anmeldungenjry);
        anmeldungenjry.aktiviereDiesenFrame(anmeldungenjry.getName());
        Reha.getThisFrame()
            .setCursor(Cursors.normalCursor);

    }

    public void loescheAnmeldungen() {
        anmeldungenjry = null;
        Reha.instance.anmeldungenpanel = null;
    }

    /*****************************
     * Umsätze von bis
     *********************************/
    public void UmsatzFenster(int setPos, String sparam) {
        if (!Reha.DbOk) {
            return;
        }
        if (!Rechte.hatRecht(Rechte.Funktion_umsatzvonbis, true)) {
            return;
        }
        JComponent umsatz = AktiveFenster.getFensterAlle("Umsaetze");
        if (umsatz != null) {
            //// System.out.println("InternalFrame Anmeldungen bereits geöffnet");
            containerHandling(((JUmsaetzeInternal) umsatz).getDesktop());
            ((JUmsaetzeInternal) umsatz).aktiviereDiesenFrame(((JUmsaetzeInternal) umsatz).getName());
            if (((JUmsaetzeInternal) umsatz).isIcon()) {
                try {
                    ((JUmsaetzeInternal) umsatz).setIcon(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);
        String name = "Umsaetze" + WinNum.NeueNummer();
        int containerNr = setPos;
        containerHandling(containerNr);
        umsaetzejry = new JUmsaetzeInternal("thera-\u03C0  - Ermittlung der realisierten Umsätze ",
                SystemConfig.hmSysIcons.get("arztstamm"), 1);
        AktiveFenster.setNeuesFenster(name, umsaetzejry, 1, umsaetzejry.getContentPane());
        umsaetzejry.setName(name);
        umsaetzejry.setSize(new Dimension(500, 150));
        umsaetzejry.setPreferredSize(new Dimension(500, 150));
        Reha.instance.umsaetzepanel = new Umsaetze(umsaetzejry);
        umsaetzejry.setContent(Reha.instance.umsaetzepanel);
        umsaetzejry.addComponentListener(Reha.instance);
        int comps = Reha.instance.desktops[containerNr].getComponentCount();
        umsaetzejry.setLocation(comps * 15, comps * 15);
        umsaetzejry.pack();
        umsaetzejry.setVisible(true);
        Reha.instance.desktops[containerNr].add(umsaetzejry);
        umsaetzejry.aktiviereDiesenFrame(umsaetzejry.getName());
        Reha.getThisFrame()
            .setCursor(Cursors.normalCursor);
    }

    public void loescheUmsaetze() {
        umsaetzejry = null;
        Reha.instance.umsaetzepanel = null;
    }

    /***************************
     * Verkäufe in der Praxis
     *********************************/
    public void VerkaufFenster(int setPos, String sparam) {
        if (!Reha.DbOk) {
            return;
        }
        if (!Rechte.hatRecht(Rechte.Sonstiges_verkaufsmodul, true)) {
            return;
        }
        JComponent vk = AktiveFenster.getFensterAlle("Verkauf");
        if (vk != null) {
            //// System.out.println("InternalFrame Anmeldungen bereits geöffnet");
            containerHandling(((JVerkaufInternal) vk).getDesktop());
            ((JVerkaufInternal) vk).aktiviereDiesenFrame(((JVerkaufInternal) vk).getName());
            if (((JVerkaufInternal) vk).isIcon()) {
                try {
                    ((JVerkaufInternal) vk).setIcon(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);
        String name = "Verkauf" + WinNum.NeueNummer();
        int containerNr = setPos;
        containerHandling(containerNr);
        verkaufjry = new JVerkaufInternal("thera-\u03C0  - Verkäufe tätigen ", SystemConfig.hmSysIcons.get("arztstamm"),
                1);
        AktiveFenster.setNeuesFenster(name, verkaufjry, 1, verkaufjry.getContentPane());
        verkaufjry.setName(name);
        verkaufjry.setSize(new Dimension(700, 600));
        verkaufjry.setPreferredSize(new Dimension(700, 600));
        Reha.instance.verkaufpanel = new VerkaufTab(verkaufjry);
        verkaufjry.setContent(Reha.instance.verkaufpanel);
        verkaufjry.addComponentListener(Reha.instance);
        int comps = Reha.instance.desktops[containerNr].getComponentCount();
        verkaufjry.setLocation(comps * 15, comps * 15);
        verkaufjry.pack();
        verkaufjry.setVisible(true);
        Reha.instance.desktops[containerNr].add(verkaufjry);
        verkaufjry.aktiviereDiesenFrame(verkaufjry.getName());
        Reha.getThisFrame()
            .setCursor(Cursors.normalCursor);
    }

    public void loescheVerkauf() {
        verkaufjry = null;
        Reha.instance.verkaufpanel = null;
    }

    /***************************
     * Fallsteuerung nach § 301
     *********************************/
    public void Dta301Fenster(int setPos, String sparam) {
        if (!Reha.DbOk) {
            return;
        }
        if (!Rechte.hatRecht(Rechte.Sonstiges_Reha301, true)) {
            return;
        }
        JComponent vk = AktiveFenster.getFensterAlle("DTA301");
        if (vk != null) {
            Reha.instance.dta301panel.aktualisieren(sparam);
            // Reha.instance.dta301panel.aktualisieren(Reha.instance.patpanel.vecaktrez.get(1));
            //// System.out.println("InternalFrame Anmeldungen bereits geöffnet");
            containerHandling(((JDta301Internal) vk).getDesktop());
            ((JDta301Internal) vk).aktiviereDiesenFrame(((JDta301Internal) vk).getName());
            if (((JDta301Internal) vk).isIcon()) {
                try {
                    ((JDta301Internal) vk).setIcon(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);
        String name = "DTA301" + WinNum.NeueNummer();
        int containerNr = setPos;
        containerHandling(containerNr);
        dta301jry = new JDta301Internal("thera-\u03C0  - Fallsteuerung nach §301 ",
                SystemConfig.hmSysIcons.get("arztstamm"), 1);
        AktiveFenster.setNeuesFenster(name, dta301jry, 1, dta301jry.getContentPane());
        dta301jry.setName(name);
        dta301jry.setSize(new Dimension(780, 500));
        dta301jry.setPreferredSize(new Dimension(820, 600));
        Reha.instance.dta301panel = new Dta301(dta301jry, sparam);
        dta301jry.setContent(Reha.instance.dta301panel);
        dta301jry.addComponentListener(Reha.instance);
        // int comps = Reha.instance.desktops[containerNr].getComponentCount();
        // dta301jry.setLocation(comps*15, comps*15);
        dta301jry.setLocation(150, 200);
        dta301jry.pack();
        dta301jry.setVisible(true);
        Reha.instance.desktops[containerNr].add(dta301jry);
        dta301jry.aktiviereDiesenFrame(dta301jry.getName());
        Reha.getThisFrame()
            .setCursor(Cursors.normalCursor);
    }

    public void loescheDta301() {
        dta301jry = null;
        Reha.instance.dta301panel = null;
    }

    /*********************** Barkasse abrechnen *************************/
    public void BarkassenFenster(int setPos, String sparam) {
        if (!Reha.DbOk) {
            return;
        }
        if (!Rechte.hatRecht(Rechte.Funktion_barkasse, true)) {
            return;
        }
        JComponent bk = AktiveFenster.getFensterAlle("Barkasse");
        if (bk != null) {
            //// System.out.println("InternalFrame Anmeldungen bereits geöffnet");
            containerHandling(((JBarkassenInternal) bk).getDesktop());
            ((JBarkassenInternal) bk).aktiviereDiesenFrame(((JBarkassenInternal) bk).getName());
            if (((JBarkassenInternal) bk).isIcon()) {
                try {
                    ((JBarkassenInternal) bk).setIcon(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);
        String name = "Barkasse" + WinNum.NeueNummer();
        //// System.out.println("Neues Barkassenfenster = "+name);
        int containerNr = setPos;
        containerHandling(containerNr);
        // barkassenjry = new JBarkassenInternal("thera-\u03C0 - Barkasse abrechnen
        // ",SystemConfig.hmSysIcons.get("arztstamm"),1) ;
        barkassenjry = new JBarkassenInternal("thera-\u03C0  - Barkasse abrechnen ",
                SystemConfig.hmSysIcons.get("BarKasse"), 1);
        AktiveFenster.setNeuesFenster(name, barkassenjry, 1, barkassenjry.getContentPane());
        barkassenjry.setName(name);
        barkassenjry.setSize(new Dimension(500, 430));
        barkassenjry.setPreferredSize(new Dimension(500, 430));
        Reha.instance.barkassenpanel = new Barkasse(barkassenjry);
        barkassenjry.setContent(Reha.instance.barkassenpanel);
        barkassenjry.addComponentListener(Reha.instance);
        int comps = Reha.instance.desktops[containerNr].getComponentCount();
        barkassenjry.setLocation(comps * 15, comps * 15);
        barkassenjry.pack();
        barkassenjry.setVisible(true);
        Reha.instance.desktops[containerNr].add(barkassenjry);
        barkassenjry.aktiviereDiesenFrame(barkassenjry.getName());
        Reha.getThisFrame()
            .setCursor(Cursors.normalCursor);
    }

    public void loescheBarkasse() {
        barkassenjry = null;
        Reha.instance.barkassenpanel = null;
    }

    /************************* Rehaabrechnungen *************************/
    public void RehaabrechnungFenster(int setPos, String sparam) {
        if (!Reha.DbOk) {
            return;
        }
        if (!Rechte.hatRecht(Rechte.Funktion_rehaabrechnung, true)) {
            return;
        }
        JComponent rab = AktiveFenster.getFensterAlle("Rehaabrechnung");
        if (rab != null) {
            containerHandling(((JRehaabrechnungInternal) rab).getDesktop());
            ((JRehaabrechnungInternal) rab).aktiviereDiesenFrame(((JRehaabrechnungInternal) rab).getName());
            if (((JRehaabrechnungInternal) rab).isIcon()) {
                try {
                    ((JRehaabrechnungInternal) rab).setIcon(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);
        String name = "Rehaabrechnung" + WinNum.NeueNummer();
        int containerNr = setPos;
        containerHandling(containerNr);
        rehaabrechnungjry = new JRehaabrechnungInternal("thera-\u03C0  - ganztägig ambulante Reha abrechnen ",
                SystemConfig.hmSysIcons.get("arztstamm"), 1);
        AktiveFenster.setNeuesFenster(name, rehaabrechnungjry, 1, rehaabrechnungjry.getContentPane());
        rehaabrechnungjry.setName(name);
        rehaabrechnungjry.setSize(new Dimension(500, 430));
        rehaabrechnungjry.setPreferredSize(new Dimension(500, 430));
        Reha.instance.rehaabrechnungpanel = new AbrechnungReha(rehaabrechnungjry);
        rehaabrechnungjry.setContent(Reha.instance.rehaabrechnungpanel);
        rehaabrechnungjry.addComponentListener(Reha.instance);
        int comps = Reha.instance.desktops[containerNr].getComponentCount();
        rehaabrechnungjry.setLocation(comps * 25, comps * 25);
        rehaabrechnungjry.pack();
        rehaabrechnungjry.setVisible(true);
        Reha.instance.desktops[containerNr].add(rehaabrechnungjry);
        rehaabrechnungjry.aktiviereDiesenFrame(rehaabrechnungjry.getName());
        Reha.getThisFrame()
            .setCursor(Cursors.normalCursor);
    }

    public void loescheRehaabrechnung() {
        rehaabrechnungjry = null;
        Reha.instance.rehaabrechnungpanel = null;
    }

    /***********************************************************/
    public void BeteiligungFenster(int setPos, String sparam) {
        if (!Reha.DbOk) {
            return;
        }
        if (!Rechte.hatRecht(Rechte.Funktion_mitarbeiterbeteiligung, true)) {
            return;
        }
        JComponent beteiligung = AktiveFenster.getFensterAlle("Beteiligung");
        if (beteiligung != null) {
            //// System.out.println("InternalFrame Anmeldungen bereits geöffnet");
            containerHandling(((JBeteiligungInternal) beteiligung).getDesktop());
            ((JBeteiligungInternal) beteiligung).aktiviereDiesenFrame(((JBeteiligungInternal) beteiligung).getName());
            if (((JBeteiligungInternal) beteiligung).isIcon()) {
                try {
                    ((JBeteiligungInternal) beteiligung).setIcon(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);
        String name = "Beteiligung" + WinNum.NeueNummer();
        int containerNr = setPos;
        containerHandling(containerNr);
        beteiligungjry = new JBeteiligungInternal("thera-\u03C0  - Ermittlung der Umsatzbeteiligungen ",
                SystemConfig.hmSysIcons.get("arztstamm"), 1);
        AktiveFenster.setNeuesFenster(name, beteiligungjry, 1, beteiligungjry.getContentPane());
        beteiligungjry.setName(name);
        beteiligungjry.setSize(new Dimension(500, 500));
        Reha.instance.beteiligungpanel = new Beteiligung(beteiligungjry);
        beteiligungjry.setContent(Reha.instance.beteiligungpanel);
        beteiligungjry.addComponentListener(Reha.instance);
        int comps = Reha.instance.desktops[containerNr].getComponentCount();
        beteiligungjry.setLocation(comps * 15, comps * 15);
        beteiligungjry.pack();
        beteiligungjry.setVisible(true);
        Reha.instance.desktops[containerNr].add(beteiligungjry);
        beteiligungjry.aktiviereDiesenFrame(beteiligungjry.getName());
        Reha.getThisFrame()
            .setCursor(Cursors.normalCursor);
    }

    public void loescheBeteiligung() {
        beteiligungjry = null;
        Reha.instance.beteiligungpanel = null;
    }

    /***************************
     * Verkäufe in der Praxis
     *********************************/
    public void BenutzerrechteFenster(int setPos, String sparam) {
        if (!Reha.DbOk) {
            return;
        }
        if (!Rechte.hatRecht(Rechte.BenutzerDialog_open, true)) {
            return;
        }
        JComponent benutzer = AktiveFenster.getFensterAlle("Benutzerrechte");
        if (benutzer != null) {
            //// System.out.println("InternalFrame Anmeldungen bereits geöffnet");
            containerHandling(((JBenutzerInternal) benutzer).getDesktop());
            ((JBenutzerInternal) benutzer).aktiviereDiesenFrame(((JBenutzerInternal) benutzer).getName());
            if (((JBenutzerInternal) benutzer).isIcon()) {
                try {
                    ((JBenutzerInternal) benutzer).setIcon(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);
        String name = "Benutzerrechte" + WinNum.NeueNummer();
        int containerNr = setPos;
        containerHandling(containerNr);
        benutzerjry = new JBenutzerInternal("thera-\u03C0  - Benutzer- und Rechteverwaltung ",
                SystemConfig.hmSysIcons.get("arztstamm"), 1);
        AktiveFenster.setNeuesFenster(name, benutzerjry, 1, benutzerjry.getContentPane());
        benutzerjry.setName(name);
        benutzerjry.setSize(new Dimension(800, 500));
        benutzerjry.setPreferredSize(new Dimension(800, 500));
        Reha.instance.benutzerrechtepanel = new BenutzerRechte(benutzerjry);
        benutzerjry.setContent(Reha.instance.benutzerrechtepanel);
        benutzerjry.addComponentListener(Reha.instance);
        int comps = Reha.instance.desktops[containerNr].getComponentCount();
        benutzerjry.setLocation(comps * 15, comps * 15);
        benutzerjry.pack();
        benutzerjry.setVisible(true);
        Reha.instance.desktops[containerNr].add(benutzerjry);
        benutzerjry.aktiviereDiesenFrame(benutzerjry.getName());
        Reha.getThisFrame()
            .setCursor(Cursors.normalCursor);
    }

    public void loescheBenutzerrechte() {
        benutzerjry = null;
        Reha.instance.benutzerrechtepanel = null;
    }

    /***********************************************************/
    public void UrlaubFenster(int setPos, String sparam) {
        if (!Reha.DbOk) {
            return;
        }
        JComponent urlaub = AktiveFenster.getFensterAlle("Urlaub");
        if (urlaub != null) {
            //// System.out.println("InternalFrame Anmeldungen bereits geöffnet");
            containerHandling(((JUrlaubInternal) urlaub).getDesktop());
            ((JUrlaubInternal) urlaub).aktiviereDiesenFrame(((JUrlaubInternal) urlaub).getName());
            if (((JUrlaubInternal) urlaub).isIcon()) {
                try {
                    ((JUrlaubInternal) urlaub).setIcon(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);
        String name = "Urlaub" + WinNum.NeueNummer();
        int containerNr = setPos;
        containerHandling(containerNr);
        urlaubjry = new JUrlaubInternal("thera-\u03C0  - Bearbeitung von Urlaub und Überstunden ",
                SystemConfig.hmSysIcons.get("arztstamm"), 1);
        AktiveFenster.setNeuesFenster(name, urlaubjry, 1, urlaubjry.getContentPane());
        urlaubjry.setName(name);
        urlaubjry.setSize(new Dimension(500, 500));
        urlaubjry.setPreferredSize(new Dimension(500, 500));
        Reha.instance.urlaubpanel = new Urlaub(urlaubjry);
        urlaubjry.setContent(Reha.instance.urlaubpanel);
        urlaubjry.addComponentListener(Reha.instance);
        int comps = Reha.instance.desktops[containerNr].getComponentCount();
        urlaubjry.setLocation(comps * 15, comps * 15);
        urlaubjry.pack();
        urlaubjry.setVisible(true);
        Reha.instance.desktops[containerNr].add(urlaubjry);
        urlaubjry.aktiviereDiesenFrame(urlaubjry.getName());
        Reha.getThisFrame()
            .setCursor(Cursors.normalCursor);
    }

    public void loescheUrlaub() {
        urlaubjry = null;
        Reha.instance.urlaubpanel = null;
    }
    /***************************
     * Verkäufe in der Praxis
     *********************************/
    /*
     * public static void InternalGut2(){ JInternalFrame iframe = new
     * JInternalFrame(); iframe.setSize(900,650); iframe.setResizable(true);
     * iframe.setIconifiable(true); iframe.setClosable(true);
     * Reha.instance.desktops[1].add(iframe); OOIFTest oif = new OOIFTest();
     * iframe.getContentPane().add(oif); iframe.setVisible(true); iframe.toFront();
     * try { iframe.setSelected(true); } catch (PropertyVetoException e) {
     * 
     * e.printStackTrace(); }
     * 
     * 
     * }
     */

    /**************
     * Patientenverwaltung Echtfunktion
     * 
     * @param connection
     ***********************/
    public void ProgPatientenVerwaltung(int setPos, Connection connection) {
        if (!Reha.DbOk) {
            Reha.instance.progressStarten(false);
            return;
        }
        try {
            JComponent patient = AktiveFenster.getFensterAlle("PatientenVerwaltung");
            if (patient != null) {
                containerHandling(((JPatientInternal) patient).getDesktop());
                ((JPatientInternal) patient).aktiviereDiesenFrame(((JPatientInternal) patient).getName());
                if (((JPatientInternal) patient).isIcon()) {
                    try {
                        ((JPatientInternal) patient).setIcon(false);
                    } catch (PropertyVetoException e) {
                        e.printStackTrace();
                    }
                }
                Reha.instance.progressStarten(false);
                ((JPatientInternal) patient).setzeSuche();
                return;
            }

            Reha.instance.progressStarten(true);
            LinkeTaskPane.thisClass.setCursor(Cursors.wartenCursor);
            String name = "PatientenVerwaltung" + WinNum.NeueNummer();
            int containerNr = SystemConfig.hmContainer.get("Patient");
            containerHandling(containerNr);
            patjry = new JPatientInternal(
                    "thera-\u03C0 Patientenverwaltung " + Reha.instance.desktops[1].getComponentCount() + 1,
                    SystemConfig.hmSysIcons.get("patstamm"), containerNr);
            /***************************/
            // Hier muß anstelle der Hartcodierung 0 oder 1 (3. Parameter) die Variable
            // containerNr erscheinen
            /***************************/
            AktiveFenster.setNeuesFenster(name, patjry, containerNr, patjry.getContentPane());
            patjry.setName(name);

            /***************************/
//Definition der Größe und der Position - Anfang
            /***************************/
            if (SystemConfig.hmContainer.get("PatientOpti") == 1) {
                patjry.setLocation(new Point(0, 0));
                patjry.setSize(new Dimension(Reha.instance.desktops[containerNr].getWidth(),
                        Reha.instance.desktops[containerNr].getHeight()));
                patjry.setPreferredSize(new Dimension(Reha.instance.desktops[containerNr].getWidth(),
                        Reha.instance.desktops[containerNr].getHeight()));

            } else {
                int xloc = SystemConfig.hmContainer.get("PatientLocationX");
                int yloc = SystemConfig.hmContainer.get("PatientLocationY");
                int xsize = SystemConfig.hmContainer.get("PatientDimensionX");
                int ysize = SystemConfig.hmContainer.get("PatientDimensionY");
                patjry.setLocation(new Point(xloc, yloc));
                // Wenn size <= 0 dann die Originalgröße
                patjry.setSize(new Dimension((xsize <= 0 ? 900 : xsize), (ysize <= 0 ? 650 : ysize)));
                patjry.setPreferredSize(new Dimension((xsize <= 0 ? 900 : xsize), (ysize <= 0 ? 650 : ysize)));
            }
            /***************************/
            // Definition der Größe und der Position - Ende
            /***************************/
            Reha.instance.patpanel = new PatientHauptPanel(name, patjry, connection);
            patjry.setContent(Reha.instance.patpanel);
            patjry.addComponentListener(Reha.instance);
            patjry.pack();
            patjry.setVisible(true);
            Reha.instance.desktops[containerNr].add(patjry);
            /***************************/
            // Definition ob immer auf maximale Größe getrimmt wird oder nicht
            /***************************/
            ((JRehaInternal) patjry).setImmerGross((SystemConfig.hmContainer.get("PatientOpti") == 1 ? true : false));
            LinkeTaskPane.thisClass.setCursor(Cursors.normalCursor);
            patjry.aktiviereDiesenFrame(patjry.getName());
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    patjry.setzeSuche();
                }
            });
            Reha.instance.progressStarten(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return;

    }

    public void loeschePatient() {
        patjry = null;
        Reha.instance.patpanel = null;
        // Reha.instance.PATINSTANCE = null;
    }

    /************** Passwortverwaltung Echtfunktion *************************/
    public static void PasswortDialog() {
        long zeit = System.currentTimeMillis();
        while (Reha.getThisFrame() == null) {
            try {
                Thread.sleep(25);
                if (System.currentTimeMillis() - zeit > 15000) {
                    System.exit(0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);
        String name = "PasswortDialog" + WinNum.NeueNummer();

        RehaSmartDialog rSmart = new LoginSmartDialog(null, name, SystemConfig.fullSizePwDialog);

        rSmart.setVisible(true);
        rSmart.toFront();

        Reha.getThisFrame()
            .setCursor(new Cursor((Cursor.DEFAULT_CURSOR)));
        if (Reha.progRechte.equals("")) {
            System.exit(0);
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (Reha.isStarted) {
                    Reha.nachrichtenRegeln();
                }
            }
        });

    }

    /************** System-Initialisierung *********************/
    public void SystemInit(int setPos, String sparam) {
        if (!Reha.DbOk) {
            return;
        }
        JComponent sysinit = AktiveFenster.getFensterAlle("SystemInit");
        if (sysinit != null) {
            //// System.out.println("InternalFrame SystemInit bereits geöffnet");
            containerHandling(((JSysteminitInternal) sysinit).getDesktop());
            ((JSysteminitInternal) sysinit).aktiviereDiesenFrame(((JSysteminitInternal) sysinit).getName());
            if (((JSysteminitInternal) sysinit).isIcon()) {
                try {
                    ((JSysteminitInternal) sysinit).setIcon(false);
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        Reha.getThisFrame()
            .setCursor(Cursors.wartenCursor);
        String name = "SystemInit" + WinNum.NeueNummer();
        int containerNr = setPos;
        containerHandling(containerNr);
        systeminitjry = new JSysteminitInternal("thera-\u03C0  - System-Initialisierung und Einstellungen ",
                SystemConfig.hmSysIcons.get("arztstamm"), 1);
        AktiveFenster.setNeuesFenster(name, systeminitjry, 1, systeminitjry.getContentPane());
        systeminitjry.setName(name);
        systeminitjry.setSize(new Dimension(850, 620));
        systeminitjry.setPreferredSize(new Dimension(850, 620));
        Reha.instance.systeminitpanel = new SystemInit(systeminitjry);
        systeminitjry.setContent(Reha.instance.systeminitpanel);
        systeminitjry.addComponentListener(Reha.instance);
        int comps = Reha.instance.desktops[containerNr].getComponentCount();
        systeminitjry.setLocation(comps * 15, comps * 15);
        systeminitjry.pack();
        systeminitjry.setVisible(true);
        Reha.instance.desktops[containerNr].add(systeminitjry);
        systeminitjry.aktiviereDiesenFrame(systeminitjry.getName());
        Reha.getThisFrame()
            .setCursor(Cursors.normalCursor);
    }

    public void loescheSysteminit() {
        systeminitjry = null;
        Reha.instance.systeminitpanel = null;
    }

    /*********************************************************************************/
    /*
     * public static void SystemInitialisierung(){ SwingUtilities.invokeLater(new
     * Runnable(){ public void run() { SystemUtil sysUtil = new SystemUtil(null);
     * //SystemUtil sysUtil = new SystemUtil(Reha.thisFrame);
     * sysUtil.setSize(850,620); //roogle.setLocationRelativeTo(null);
     * sysUtil.setLocationRelativeTo(null); sysUtil.setVisible(true); } }); }
     */

    public static void containerBelegen(int setPos, RehaTP jtp) {
        if (setPos == 1) {
            if (Reha.instance.jLeerOben != null) {
                Reha.instance.jLeerOben.setVisible(false);
                Reha.instance.jInhaltOben = jtp;
                Reha.instance.jContainerOben.add(Reha.instance.jInhaltOben, BorderLayout.CENTER);
                Reha.instance.jContainerOben.validate();
                Reha.instance.jContainerOben.remove(Reha.instance.jLeerOben);
                Reha.instance.jLeerOben = null;
            } else {
                RehaSmartDialog rsm = new RehaSmartDialog(Reha.getThisFrame(), jtp.getContentContainer()
                                                                                  .getName());
                PinPanel pinPanel = new PinPanel();
                pinPanel.setName(jtp.getName());
                rsm.setPinPanel(pinPanel);
                rsm.setName(jtp.getName());
                rsm.setLocation(300, 300);
                rsm.setContentPanel(jtp.getContentContainer());
                rsm.setVisible(true);
            }

        } else if (setPos == 2) {
            if (Reha.instance.jLeerUnten != null) {
                Reha.instance.jLeerUnten.setVisible(false);
                Reha.instance.jInhaltUnten = jtp;
                Reha.instance.jContainerUnten.add(Reha.instance.jInhaltUnten, BorderLayout.CENTER);
                Reha.instance.jContainerUnten.validate();
                Reha.instance.jContainerUnten.remove(Reha.instance.jLeerUnten);
                Reha.instance.jLeerUnten = null;
            } else {
                RehaSmartDialog rsm = new RehaSmartDialog(Reha.getThisFrame(), jtp.getContentContainer()
                                                                                  .getName());
                PinPanel pinPanel = new PinPanel();
                pinPanel.setName(jtp.getName());
                rsm.setPinPanel(pinPanel);
                rsm.setName(jtp.getName());
                rsm.setLocation(300, 300);
                rsm.setContentPanel(jtp.getContentContainer());
                rsm.setVisible(true);
            }
        } else if (setPos == 0) {
            RehaSmartDialog rsm = new RehaSmartDialog(Reha.getThisFrame(), jtp.getContentContainer()
                                                                              .getName());
            PinPanel pinPanel = new PinPanel();
            pinPanel.setName(jtp.getName());
            //// System.out.println("jtp.getName() = "+jtp.getName());
            jtp.setStandort(jtp.getName(), 0);
            rsm.setPinPanel(pinPanel);
            rsm.setName(jtp.getName());
            rsm.setLocationRelativeTo(null);
            rsm.setContentPanel(jtp.getContentContainer());
            rsm.setVisible(true);
        }

    }

    public void RehaTPEventOccurred(RehaTPEvent evt) {

        //// System.out.println("ProgLoader Systemausl�ser"+evt.getSource());
        //// System.out.println("ProgLoader Event getDetails[0]: =
        //// "+evt.getDetails()[0]);
        //// System.out.println("ProgLoader Event getDetails[1]: =
        //// "+evt.getDetails()[1]);
        //// System.out.println(((JXTitledPanel)
        //// evt.getSource()).getContentContainer().getName());
    }

    public static int PosTest(int pos) {
        if ((pos == 1) && (Reha.instance.jLeerOben == null)) {
            return 0;
        }
        if ((pos == 2) && (Reha.instance.jLeerUnten == null)) {
            //// System.out.println("pos = "+pos);
            return 0;
        }
        return pos;
    }

    public static void containerHandling(int cont) {
        if (Reha.instance.vollsichtbar == -1 || (!SystemConfig.desktopHorizontal)) {
            // System.out.println("Location =
            // "+Reha.instance.jSplitRechtsOU.getDividerLocation());
            // System.out.println("Width = "+Reha.instance.jSplitRechtsOU.getWidth());
            if (cont == 0) {
                if (Reha.instance.jSplitRechtsOU.getDividerLocation() == 0) {
                    Reha.instance.setDivider(5);
                }
            } else if (cont == 1) {
                if (Reha.instance.jSplitRechtsOU.getDividerLocation() == Reha.instance.jSplitRechtsOU.getWidth() - 7) {
                    Reha.instance.setDivider(6);
                }
            }

            return;
        }
        if ((Reha.instance.vollsichtbar == 1 && cont == 1) || (Reha.instance.vollsichtbar == 0 && cont == 0)) {
            return;
        }
        if (Reha.instance.vollsichtbar == 0 && cont == 1) {
            Reha.instance.setDivider(6);
            return;
        }
        if (Reha.instance.vollsichtbar == 1 && cont == 0) {
            Reha.instance.setDivider(5);
            return;
        }

    }

}
