package verkauf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.text.DecimalFormat;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.star.awt.Size;

import CommonTools.ButtonTools;
import CommonTools.JCompTools;
import CommonTools.JRtaRadioButton;
import CommonTools.JRtaTextField;
import CommonTools.OOTools;
import CommonTools.SqlInfo;
import CommonTools.StringTools;
import CommonTools.ini.INIFile;
import CommonTools.ini.INITool;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.internal.text.TextFieldService;
import ag.ion.bion.officelayer.internal.text.TextTableService;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextTable;
import ag.ion.bion.officelayer.text.TextException;
import ag.ion.noa.internal.printing.PrintProperties;
import ag.ion.noa.printing.IPrinter;
import environment.Path;
import hauptFenster.Reha;
import systemEinstellungen.SystemConfig;
import verkauf.model.Artikel;
import verkauf.model.ArtikelVerkauf;
import verkauf.model.Verkauf;

public class VerkaufGUI extends JXPanel {

    private static final long serialVersionUID = -6537113748627245247L;

    ArtikelSuchenDialog adlg = null;

    ActionListener al = null;
    KeyListener kl = null;
    FocusListener fl = null;
    JRtaTextField[] edits = { null, null, null, null, null, null, null, null, null };
    JRtaRadioButton[] rbuts = { null, null, null };
    ButtonGroup group = new ButtonGroup();
    /** neu, Bar, Rechnung, delete, edit, Formulare**/
    JButton[] buts = { null, null, null, null, null, null };
    public JXTable vktab = null;
    public DefaultTableModel vkmod = new DefaultTableModel();
    JScrollPane jscr = null;
    int lastcol = 7;
    JLabel einheitAnzahlLabel = null;
    /** 0="Artikel-ID", 1="Beschreibung", 2="Einzel-Preis", 3="Anzahl", 4="Rabatt",
     5="Gesamt-Preis", 6="MwSt.", 7="id" **/
    String[] column = { "Artikel-ID", "Beschreibung", "Einzel-Preis", "Anzahl", "Rabatt", "Gesamt-Preis", "MwSt.", "" };

    ArtikelVerkauf aktuellerArtikel = null;
    WechselgeldDialog wDialog = null;
    verkauf.model.Verkauf verkauf = null;
    DecimalFormat df = null;
    INIFile settings = null;
    VerkaufTab owner;
    boolean debug = false;
    Formulare forms = null;
    private String customForm = null;
    JLabel custFormName = new JLabel("");
    private boolean useBon = false;

    private Logger logger = LoggerFactory.getLogger(VerkaufGUI.class);

    private MwSTSatz satz = MwSTSatz.now();

    public VerkaufGUI(VerkaufTab owner) {
        super();

        this.owner = owner;
        this.activateListener();
        this.setLayout(new BorderLayout());
        this.setOpaque(false);
        this.add(this.getContent1(), BorderLayout.CENTER);
        verkauf = new Verkauf();
        df = new DecimalFormat("0.00");
        settings = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "verkauf.ini");

        forms = new Formulare();
        forms.holeFormulare(settings);
        useBon = settings.getBooleanProperty("Bon", "BonDruckErlaubt");
        buts[1].setEnabled(useBon);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setzeFocus();
            }
        });

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                forms.makeDialog();
            }
        });
    }

    private void setzeFocus() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (edits[0] != null) {
                    edits[0].requestFocus();
                }
            }
        });
    }

    private JXPanel getContent1() {
        JXPanel pan = new JXPanel();
        pan.setOpaque(false);
        /**************/
        // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17
        String xwerte = "5dlu, 55dlu, 5dlu, 55dlu, 5dlu, 55dlu, 5dlu, 55dlu:g, 5dlu, 55dlu, 5dlu, 55dlu, 5dlu, 78dlu, 5dlu";
        // 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17
        String ywerte = "5dlu, p, 1dlu, p, 10dlu, 150dlu:g, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu ";
        FormLayout lay = new FormLayout(xwerte, ywerte);
        CellConstraints cc = new CellConstraints();
        pan.setLayout(lay);

        /****** Legende ********/
        JLabel artikelidLbl = new JLabel("Artikel-ID");
        artikelidLbl.setIcon(SystemConfig.hmSysIcons.get("kleinehilfe"));
        artikelidLbl.addMouseListener(this.owner.ml);
        pan.add(artikelidLbl, cc.xy(2, 2));
        pan.add(new JLabel("Beschreibung"), cc.xyw(8, 2, 3));
        einheitAnzahlLabel = new JLabel("Anzahl / Einheit");
        pan.add(einheitAnzahlLabel, cc.xy(4, 2));
        pan.add(new JLabel("Gesamtpreis"), cc.xy(12, 2));
        JLabel rabatt = new JLabel("Rabatt");
        pan.add(rabatt, cc.xy(6, 2));

        /****** Edits und 2 Button ********/
        pan.add((edits[0] = new JRtaTextField("nix", true)), cc.xy(2, 4));
        edits[0].setName("artikelid");
        edits[0].addFocusListener(fl);
        edits[0].addKeyListener(kl);

        pan.add((edits[1] = new JRtaTextField("FL", true, "6.2", "RECHTS")), cc.xy(4, 4));
        edits[1].setName("anzahl");
        edits[1].setText("1,00");
        edits[1].addFocusListener(fl);
        edits[1].addKeyListener(kl);

        pan.add((edits[2] = new JRtaTextField("FL", true, "6.2", "RECHTS")), cc.xy(6, 4));
        edits[2].setText("0,00");
        edits[2].setName("artikelRabatt");
        edits[2].addFocusListener(fl);
        edits[2].addKeyListener(kl);

        pan.add((edits[3] = new JRtaTextField("nix", true)), cc.xyw(8, 4, 3));
        edits[3].setName("beschreibung");
        edits[3].addFocusListener(fl);
        edits[3].addKeyListener(kl);

        pan.add((edits[4] = new JRtaTextField("FL", true, "6.2", "RECHTS")), cc.xy(12, 4));
        edits[4].setName("preis");
        edits[4].setEditable(false);
        edits[4].addFocusListener(fl);
        edits[4].addKeyListener(kl);

        JToolBar jtb = new JToolBar();
        FormLayout lay2 = new FormLayout("p, 5dlu, p, 5dlu, p", "p");
        jtb.setLayout(lay2);
        jtb.setBorder(null);
        jtb.setOpaque(false);
        jtb.setRollover(true);

        jtb.add((buts[0] = new JButton()), cc.xy(1, 1));
        buts[0].setOpaque(false);
        buts[0].setIcon(SystemConfig.hmSysIcons.get("neu"));
        buts[0].setActionCommand("neu");
        buts[0].addActionListener(al);
        buts[0].setMnemonic(KeyEvent.VK_PLUS);

        jtb.add((buts[4] = new JButton()), cc.xy(3, 1));
        buts[4].setOpaque(false);
        buts[4].setIcon(SystemConfig.hmSysIcons.get("edit"));
        buts[4].setActionCommand("edit");
        buts[4].addActionListener(al);
        buts[4].setMnemonic(KeyEvent.VK_E);

        jtb.add((buts[3] = new JButton()), cc.xy(5, 1));
        buts[3].setOpaque(false);
        buts[3].setIcon(SystemConfig.hmSysIcons.get("delete"));
        buts[3].setActionCommand("delete");
        buts[3].addActionListener(al);
        buts[3].setMnemonic(KeyEvent.VK_MINUS);

        pan.add(jtb, cc.xy(14, 4));

        /****** Tabelle ********/
        vkmod.setColumnIdentifiers(column);
        vktab = new JXTable(vkmod);
        vktab.setEditable(false);
        vktab.getColumn(lastcol)
             .setMinWidth(0);
        vktab.getColumn(lastcol)
             .setMaxWidth(0);
        vktab.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                if (arg0.getClickCount() == 2) {
                    aktiviereFunktion(VerkaufTab.edit);
                }

            }

        });
        jscr = JCompTools.getTransparentScrollPane(vktab);
        jscr.validate();
        pan.add(jscr, cc.xyw(2, 6, 13, CellConstraints.FILL, CellConstraints.FILL));

        /****** Summe / Steuer / Rabatt ********/
       JLabel summeLbl = new JLabel("Summe:");
        pan.add(summeLbl, cc.xy(12, 8));
        JLabel MwStVermindert = new JLabel("MwSt. "
                + satz.verminderterSatz()
                + "%:");
        pan.add(MwStVermindert, cc.xy(12, 10));
        JLabel MwStVoll = new JLabel("MwSt. "
                + satz.vollerSatz()
                + "%");
        pan.add(MwStVoll, cc.xy(12, 12));

        pan.add((edits[6] = new JRtaTextField("FL", true, "6.2", "RECHTS")), cc.xy(14, 8));
        edits[6].setEditable(false);
        edits[6].setText("0,00");
        pan.add((edits[7] = new JRtaTextField("FL", true, "6.2", "RECHTS")), cc.xy(14, 10));
        edits[7].setEditable(false);
        edits[7].setText("0,00");
        pan.add((edits[8] = new JRtaTextField("FL", true, "6.2", "RECHTS")), cc.xy(14, 12));
        edits[8].setEditable(false);
        edits[8].setText("0,00");

        /****** Steuerbuttons ********/
        pan.add((buts[1] = ButtonTools.macheButton("Barzahlung", "bonEnde", al)), cc.xy(12, 14));
        buts[1].setMnemonic(KeyEvent.VK_B);
        pan.add((buts[2] = ButtonTools.macheButton("Rechnung", "rechnungEnde", al)), cc.xy(14, 14));
        buts[2].setMnemonic(KeyEvent.VK_N);
        /*************/

        /********* Adress-Quelle ************/
        rbuts[0] = new JRtaRadioButton("Adresse von aktuellem Patient beziehen");
        pan.add(rbuts[0], cc.xyw(2, 8, 5));
        rbuts[1] = new JRtaRadioButton("Adresse von aktueller Kasse beziehen");
        pan.add(rbuts[1], cc.xyw(2, 10, 5));
        rbuts[2] = new JRtaRadioButton("Adresse von Hand eingeben");
        pan.add(rbuts[2], cc.xyw(2, 12, 5));
        rbuts[0].setOpaque(false);
        rbuts[1].setOpaque(false);
        rbuts[2].setOpaque(false);
        group.add(rbuts[0]);
        group.add(rbuts[1]);
        group.add(rbuts[2]);
        rbuts[0].setSelected(true);
        /*************/

        // pan.add( (buts[5] = new JButton()),cc.xy(2, 12));
        pan.add((buts[5] = new JButton()), cc.xywh(9, 8, 2, 2));
        buts[5].setIcon(SystemConfig.hmSysIcons.get("print"));
        buts[5].setToolTipText("anderes Formular verwenden");
        buts[5].setActionCommand("verkFormulare");
        buts[5].addActionListener(al);
        buts[5].setOpaque(false);
        buts[5].setBorderPainted(false);
        buts[3].setMnemonic(KeyEvent.VK_F);

        MwStVoll = new JLabel("              Vorlage:");
        pan.add(MwStVoll, cc.xy(8, 10));
        selectStandardForm();
        pan.add(custFormName, cc.xyw(9, 10, 2));

        pan.validate();
        // edits[0].requestFocus();
        return pan;
    }

    public void aktiviereFunktion(int befehl) {
        if (befehl == VerkaufTab.neu) {
            if (aktuellerArtikel != null) {
                aktuellerArtikel.setAnzahl(Double.parseDouble(edits[1].getText()
                                                                      .replace(",", ".")));
                aktuellerArtikel.gewaehreRabatt(Double.parseDouble(edits[2].getText()
                                                                           .replace(",", ".")));
                aktuellerArtikel.setBeschreibung(edits[3].getText());
                aktuellerArtikel.setPreis(Double.parseDouble(edits[4].getText()
                                                                     .replace(",", "."))
                        / Double.parseDouble(edits[1].getText()
                                                     .replace(",", ".")));

                verkauf.fuegeArtikelHinzu(aktuellerArtikel);
                aktuellerArtikel = null;

                edits[6].setText(df.format(verkauf.getBetragBrutto()));
                edits[7].setText(df.format(verkauf.getBetrag7()));
                edits[8].setText(df.format(verkauf.getBetrag19()));

                setzeFelderzurueck();
                /*
                 * vkmod.setDataVector(verkauf.liefereTabDaten(), column);
                 * vktab.getColumn(lastcol).setMinWidth(0);
                 * vktab.getColumn(lastcol).setMaxWidth(0);
                 */
                String[][] werte = verkauf.liefereTabDaten();
                setzeTabellenWerte(werte);
                if (werte.length > 0) {
                    // vktab.setRowSelectionInterval(werte.length-1, werte.length-1);
                }
                edits[0].requestFocus();
                return;
            }
        } else if (befehl == VerkaufTab.edit) {
            if (vktab.getSelectedRow() >= 0) {
                try {
                    this.aktuellerArtikel = verkauf.lieferePosition(
                            Integer.parseInt((String) vkmod.getValueAt(vktab.getSelectedRow(), 7)));
                    String[][] werte = verkauf.liefereTabDaten();
                    setzeTabellenWerte(werte);
                    /*
                     * System.out.println(werte.length); vkmod.setDataVector(werte, column);
                     * vktab.getColumn(lastcol).setMinWidth(0);
                     * vktab.getColumn(lastcol).setMaxWidth(0);
                     */
                    edits[6].setText(df.format(verkauf.getBetragBrutto()));
                    edits[7].setText(df.format(verkauf.getBetrag7()));
                    edits[8].setText(df.format(verkauf.getBetrag19()));
                    edits[0].setText(this.aktuellerArtikel.getEan());
                    edits[3].setText(this.aktuellerArtikel.getBeschreibung());
                    edits[1].setText(String.valueOf(this.aktuellerArtikel.getAnzahl())
                                           .replace('.', ','));
                    edits[2].setText(String.valueOf(this.aktuellerArtikel.getRabatt())
                                           .replace('.', ','));
                    double zeilengesamt = this.aktuellerArtikel.getAnzahl() * this.aktuellerArtikel.getPreis()
                            - (this.aktuellerArtikel.getAnzahl() * this.aktuellerArtikel.getPreis() / 100
                                    * this.aktuellerArtikel.getRabatt());
                    edits[4].setText(df.format(zeilengesamt));

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(null, "Wen oder was willst du ändern?");
            }
        } else if (befehl == VerkaufTab.delete) {
            if (vkmod.getRowCount() > 0) {
                if (vktab.getSelectedRow() < 0) {
                    return;
                }
                int del1 = vktab.getSelectedRow();
                int del2 = vktab.convertRowIndexToModel(del1);
                // läuft auf Fehler wenn z.B. 3 oder mehr Artikel in der Tabelle
                // anschließend der mittlere gelöscht wird und dann der letzte
                // verkauf.loescheArtikel((Integer.parseInt((String)vkmod.getValueAt(vktab.getSelectedRow(),
                // 7))));
                verkauf.loescheArtikel(del2);
                String[][] werte = verkauf.liefereTabDaten();
                setzeTabellenWerte(werte);
                /*
                 * vkmod.setDataVector(verkauf.liefereTabDaten(), column);
                 * vktab.getColumn(lastcol).setMinWidth(0);
                 * vktab.getColumn(lastcol).setMaxWidth(0);
                 */
                edits[6].setText(df.format(verkauf.getBetragBrutto()));
                edits[7].setText(df.format(verkauf.getBetrag7()));
                edits[8].setText(df.format(verkauf.getBetrag19()));
                if (vkmod.getRowCount() >= (del1 + 1)) {
                    vktab.setRowSelectionInterval(del1, del1);
                } else if (vkmod.getRowCount() == 0) {
                    return;
                } else if (vkmod.getRowCount() >= del1) {
                    vktab.setRowSelectionInterval(del1 - 1, del1 - 1);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Wen oder was willst du löschen?");
            }
        } else if (befehl == VerkaufTab.rechnungEnde) {
            rechnungEnde();
        } else if (befehl == VerkaufTab.bonEnde) {
            bonEnde();
        } else if (befehl == VerkaufTab.suche) {
            UebergabeTool uebergabe = new UebergabeTool("");
            // wichtig !! erst mal eine InstanzVariable erzeugen
            // die dann z.B. für Focus setzen verwendet werden kann
            adlg = new ArtikelSuchenDialog(null, uebergabe, this.owner.holePosition(300, 400), ""); // neu
            SwingUtilities.invokeLater(new Runnable() { // neu
                @Override
                public void run() {
                    adlg.setzeFocus(); // neu
                }
            });
            adlg.setModal(true); // neu
            adlg.setVisible(true); // neu

            edits[0].requestFocus();
            edits[0].setText(uebergabe.getString());
            if (!edits[0].getText()
                         .equals("")) {
                edits[1].requestFocus();
            }
            adlg = null; // neu
        } else if (befehl == VerkaufTab.verkFormulare) {
            selectFormular();
        }
    }

    private void selectFormular() {
        Point pt = buts[5].getLocationOnScreen();
        pt.setLocation(pt.x, pt.y - 250);
        int iformular = forms.showDialog(pt);

        if (iformular < 0) {
            selectStandardForm();
        } else {
            selectCustomForm(iformular);
        }
    }

    private void selectStandardForm() {
        buts[1].setEnabled(useBon);
        buts[5].setBorderPainted(false);
        customForm = null;
        custFormName.setText("Bon/Rechnung");
        custFormName.setForeground(Color.BLACK);
        rbuts[0].setSelected(true);
    }

    private void selectCustomForm(int iformular) {
        buts[1].setEnabled(false); // Custom-Formulare nur 'auf Rechnung'
        buts[5].setBorderPainted(true);
        buts[2].requestFocus();
        customForm = forms.getFormular(iformular);
        custFormName.setText("<html><b>Custom</b></html>");
        custFormName.setForeground(Color.RED);
    }

    private void setzeTabellenWerte(String[][] tabDaten) {
        final String[][] tDaten = tabDaten;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                vkmod.setRowCount(0);
                for (int i = 0; i < tDaten.length; i++) {
                    if (debug) {
                        zeigeWerte(tDaten[i]);
                    }
                    vkmod.addRow(tDaten[i]);
                }
                vktab.repaint();
                return null;
            }
        }.execute();
    }

    private void zeigeWerte(String[] werte) {
        for (int i = 0; i < werte.length; i++) {
            System.out.println(werte[i]);
        }
    }

    private void activateListener() {
        this.al = this.owner.al;

        kl = new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_F9) {
                    edits[0].requestFocus();
                    verkauf.fuegeArtikelHinzu(aktuellerArtikel);
                    aktuellerArtikel = null;
                    setzeFelderzurueck();
                    edits[6].setText(df.format(verkauf.getBetragBrutto()));
                    edits[7].setText(df.format(verkauf.getBetragBrutto()));
                    edits[8].setText(df.format(verkauf.getBetragBrutto()));
                    String[][] werte = verkauf.liefereTabDaten();
                    setzeTabellenWerte(werte);

                }
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
                if (arg0.getKeyChar() == '?') {
                    arg0.consume();
                    ((JRtaTextField) arg0.getSource()).setText("");
                    // edits[0].setText("");
                    aktiviereFunktion(VerkaufTab.suche);
                }
            }
        };

        fl = new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent arg0) {
                try {
                    if (((JComponent) arg0.getSource()).getName()
                                                       .equals("artikelid")) {
                        String ean = edits[0].getText();
                        if (Artikel.artikelExistiert(ean)) {
                            aktuellerArtikel = new ArtikelVerkauf(ean);
                            edits[3].setText(aktuellerArtikel.getBeschreibung());
                            edits[4].setText(df.format(aktuellerArtikel.getPreis()));
                            einheitAnzahlLabel.setText("Anzahl / " + aktuellerArtikel.getEinheit());
                            // TODO: prüfen ob das Fenster grade Aktiv ist!; Rechnung vorschau?
                        } else {
                            edits[0].removeFocusListener(fl);
                            // edits[0].requestFocus();
                            edits[0].addFocusListener(fl);
                        }
                        return;
                    } else if (((JComponent) arg0.getSource()).getName()
                                                              .equals("anzahl")) {
                        if (aktuellerArtikel != null) {
                            aktuellerArtikel.setAnzahl(Double.parseDouble(edits[1].getText()
                                                                                  .replace(",", ".")));
                            edits[4].setText(df.format(aktuellerArtikel.getPreis() * aktuellerArtikel.getAnzahl()));
                        }
                        return;
                    } else if (((JComponent) arg0.getSource()).getName()
                                                              .equals("artikelRabatt")) {
                        if (aktuellerArtikel != null) {
                            aktuellerArtikel.gewaehreRabatt(Double.parseDouble(edits[2].getText()
                                                                                       .replace(",", ".")));
                            edits[4].setText(df.format(aktuellerArtikel.getPreis() * aktuellerArtikel.getAnzahl()));
                        }
                        return;
                    } else if (((JComponent) arg0.getSource()).getName()
                                                              .equals("beschreibung")) {
                        if (aktuellerArtikel != null) {
                            aktuellerArtikel.setBeschreibung(edits[3].getText());
                        }
                        return;
                    } else if (((JComponent) arg0.getSource()).getName()
                                                              .equals("preis")) {
                        if (aktuellerArtikel != null) {
                            aktuellerArtikel.setPreis(Double.parseDouble(edits[4].getText()
                                                                                 .replace(",", ".")));
                        }
                        return;
                    }
                } catch (Exception e) {
                    logger.error("something bad happened", e);
                }
            }

        };
    }

    private void bonEnde() {
        if (verkauf.getAnzahlPositionen() != 0) {

            Point position = buts[1].getLocationOnScreen();
            position.x = position.x + (buts[1].getWidth() / 2) - 100;
            position.y = position.y - 175;
            position.setLocation(position.x, position.y);

            wDialog = new WechselgeldDialog(null, position /* this.owner.holePosition(200, 150) */,
                    verkauf.getBetragBrutto());
            // nicht die feine englische...,
            // alternativ müßten die ganzen Focus- und KeyListener abgehängt werden
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        long eintritt = System.currentTimeMillis();
                        while (!wDialog.getTextFeld()
                                       .hasFocus()) {
                            wDialog.setzeFocus();
                            Thread.sleep(25);
                            if (System.currentTimeMillis() - eintritt > 5000) {
                                System.out.println("Zwangsausbruch aus bonEnde");
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
            }.execute();
            wDialog.setModal(true);
            wDialog.setVisible(true);

            if (!wDialog.processPayment()) {
                return; // Abbruch ist mit ESC oder rotem Button möglich
            }
            wDialog = null;

            String propSection = "Bon";
            String nummernkreis = "VB-" + SqlInfo.erzeugeNummer("vbon");

            IOfficeApplication application = Reha.officeapplication;
            try {
                IDocumentService service = application.getDocumentService();
                IDocumentDescriptor descriptor = new DocumentDescriptor();
                descriptor.setHidden(settings.getBooleanProperty(propSection, "SofortDrucken"));
                descriptor.setAsTemplate(true);

                String url = Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK() + "/"
                        + settings.getStringProperty(propSection, "Vorlage");
                ITextDocument doc = (ITextDocument) service.loadDocument(url, descriptor);
                if (settings.getBooleanProperty(propSection, "SeitenLaengeAendern")) {
                    Size page = (Size) doc.getPageService()
                                          .getPage(0)
                                          .getPageStyle()
                                          .getProperties()
                                          .getXPropertySet()
                                          .getPropertyValue("Size");
                    page.Height = page.Height + verkauf.getAnzahlPositionen()
                            * settings.getIntegerProperty(propSection, "ProArtikelSeitenLaenge");
                    doc.getPageService()
                       .getPage(0)
                       .getPageStyle()
                       .getProperties()
                       .getXPropertySet()
                       .setPropertyValue("Size", page);
                }

                TextFieldService feldservice = (TextFieldService) doc.getTextFieldService();
                ITextField[] felder = feldservice.getPlaceholderFields();
                for (int i = 0; i < felder.length; i++) {
                    if (felder[i].getDisplayText()
                                 .equals("<Rrabatt>")) {
                        felder[i].getTextRange()
                                 .setText(df.format(verkauf.getRabatt()));
                    } else if (felder[i].getDisplayText()
                                        .equals("<Rnummer>")) {
                        felder[i].getTextRange()
                                 .setText(nummernkreis);
                    } else if (felder[i].getDisplayText()
                                        .equals("<Rbrutto>")) {
                        felder[i].getTextRange()
                                 .setText(df.format(verkauf.getBetragBrutto()) + " €");
                    } else if (felder[i].getDisplayText()
                                        .equals("<Rmwst7>")) {
                        felder[i].getTextRange()
                                 .setText(df.format(verkauf.getBetrag7()) + " €");
                    } else if (felder[i].getDisplayText()
                                        .equals("<Rmwst19>")) {
                        felder[i].getTextRange()
                                 .setText(df.format(verkauf.getBetrag19()) + " €");
                    } else if (felder[i].getDisplayText()
                                        .equals("<Rnetto>")) {
                        felder[i].getTextRange()
                                 .setText(df.format(
                                         verkauf.getBetragBrutto() - verkauf.getBetrag19() - verkauf.getBetrag7())
                                         + " €");
                    }
                }
                TextTableService tservice = (TextTableService) doc.getTextTableService();

                ITextTable tabelle = tservice.getTextTable("Tabelle1");
                if (tabelle != null) {
                    fuelleTabelle(tabelle, propSection);
                }

                if (settings.getBooleanProperty(propSection, "SofortDrucken")) {
                    String druckername = settings.getStringProperty(propSection, "Drucker");
                    IPrinter drucker = null;
                    if (druckername == null) {
                        drucker = doc.getPrintService()
                                     .getActivePrinter();
                    } else {
                        drucker = doc.getPrintService()
                                     .createPrinter(druckername);
                    }
                    doc.getPrintService()
                       .setActivePrinter(drucker);
                    PrintProperties printprop = new PrintProperties((short) 1, null);
                    doc.getPrintService()
                       .print(printprop);
                    final ITextDocument xdoc = doc;
                    new SwingWorker<Void, Void>() {

                        @Override
                        protected Void doInBackground() throws Exception {
                            Thread.sleep(100);
                            xdoc.close();
                            Thread.sleep(100);
                            return null;
                        }

                    };
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Fehler: " + e.getMessage() + "!");
                e.printStackTrace();
            }
            schreibeUmsatzDaten(nummernkreis, 0.00, -100);
            verkauf.fuehreVerkaufdurch(-100, nummernkreis);
            verkauf = new Verkauf();
            String[][] werte = verkauf.liefereTabDaten();
            setzeTabellenWerte(werte);
            this.owner.aktiviereFunktion(VerkaufTab.reload);
            /*
             * vkmod.setDataVector(verkauf.liefereTabDaten(), column);
             */
            edits[0].requestFocus();
            edits[6].setText("0,00");
            edits[7].setText("0,00");
            edits[8].setText("0");
        } else {
            JOptionPane.showMessageDialog(this, "Keine Positionen zum Bonieren vorhanden!");
        }
    }

    private void rechnungEnde() {
        if (verkauf.getAnzahlPositionen() != 0) {
            if (((Reha.instance.patpanel != null) && (Reha.instance.patpanel.patDaten != null) && rbuts[0].isSelected())
                    || (Reha.instance.kassenpanel != null && rbuts[1].isSelected())
                    || rbuts[2].isSelected()) {
                String name = null, vorname = null, adresse = null, plz = null, ort = null, anrede = null;
                int patid = -1;
                if (rbuts[0].isSelected()) {
                    name = StringTools.EGross(Reha.instance.patpanel.patDaten.get(2));
                    vorname = StringTools.EGross(Reha.instance.patpanel.patDaten.get(3));
                    adresse = StringTools.EGross(Reha.instance.patpanel.patDaten.get(21));
                    plz = Reha.instance.patpanel.patDaten.get(23);
                    ort = StringTools.EGross(Reha.instance.patpanel.patDaten.get(24));
                    anrede = StringTools.EGross(Reha.instance.patpanel.patDaten.get(0));
                    patid = Integer.parseInt(Reha.instance.patpanel.patDaten.get(29));
                } else {
                    vorname = SystemConfig.hmAdrKDaten.get("<Kadr1>");
                    name = (!SystemConfig.hmAdrKDaten.get("<Kadr2>")
                                                     .trim()
                                                     .equals("") ? "\r" + SystemConfig.hmAdrKDaten.get("<Kadr2>") : "");
                    adresse = SystemConfig.hmAdrKDaten.get("<Kadr3>");
                    plz = SystemConfig.hmAdrKDaten.get("<Kadr4>");
                    ort = "";
                    anrede = "";
                }
                String propSection = "Rechnung";
                String nummernkreis = "VR-" + SqlInfo.erzeugeNummer("vrechnung");
                String url = null;

                IOfficeApplication application = Reha.officeapplication;
                boolean hideOfficeInBackground = settings.getBooleanProperty(propSection, "SofortDrucken");
                try {
                    IDocumentService service = application.getDocumentService();
                    IDocumentDescriptor descriptor = new DocumentDescriptor();
                    if (rbuts[2].isSelected()) {
                        hideOfficeInBackground = false; // Adressdaten müssen vor dem Druck eingegeben werden
                    }
                    descriptor.setHidden(hideOfficeInBackground);
                    descriptor.setAsTemplate(true);

                    if (customForm == null) {
                        url = Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK() + "/"
                                + settings.getStringProperty(propSection, "Vorlage");
                    } else {
                        url = Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK() + "/" + customForm;
                        customForm = null;
                    }
                    IDocument document = service.loadDocument(url, descriptor);
                    ITextDocument doc = (ITextDocument) document;

                    TextFieldService feldservice = (TextFieldService) doc.getTextFieldService();
                    ITextField[] felder = feldservice.getPlaceholderFields();
                    for (int i = 0; i < felder.length; i++) {
                        if (felder[i].getDisplayText()
                                     .equals("<Pname>")) {

                        } else if (felder[i].getDisplayText()
                                            .equals("<Pnname>")) {
                            felder[i].getTextRange()
                                     .setText(name);
                        } else if (felder[i].getDisplayText()
                                            .equals("<Pvname>")) {
                            felder[i].getTextRange()
                                     .setText(vorname);
                        } else if (felder[i].getDisplayText()
                                            .equals("<Panrede>")) {
                            if (anrede.equals("Herr")) {
                                felder[i].getTextRange()
                                         .setText("Sehr geehrter Herr " + name);
                            } else if (anrede.equals("")) {
                                felder[i].getTextRange()
                                         .setText("Sehr geehrte Damen und Herren");
                            } else {
                                felder[i].getTextRange()
                                         .setText("Sehr geehrte Frau " + name);
                            }
                        } else if (felder[i].getDisplayText()
                                            .equals("<Padr>")) {
                            felder[i].getTextRange()
                                     .setText(adresse);
                        } else if (felder[i].getDisplayText()
                                            .equals("<Pplz>")) {
                            felder[i].getTextRange()
                                     .setText(plz);
                        } else if (felder[i].getDisplayText()
                                            .equals("<Port>")) {
                            felder[i].getTextRange()
                                     .setText(ort);
                        } else if (felder[i].getDisplayText()
                                            .equals("<Rnetto>")) {
                            felder[i].getTextRange()
                                     .setText(df.format(
                                             verkauf.getBetragBrutto() - verkauf.getBetrag19() - verkauf.getBetrag7())
                                             + " €");
                        } else if (felder[i].getDisplayText()
                                            .equals("<Rmwst7>")) {
                            felder[i].getTextRange()
                                     .setText(df.format(verkauf.getBetrag7()) + " €");
                        } else if (felder[i].getDisplayText()
                                            .equals("<Rmwst19>")) {
                            felder[i].getTextRange()
                                     .setText(df.format(verkauf.getBetrag19()) + " €");
                        } else if (felder[i].getDisplayText()
                                            .equals("<Rbrutto>")) {
                            felder[i].getTextRange()
                                     .setText(df.format(verkauf.getBetragBrutto()) + " €");
                        } else if (felder[i].getDisplayText()
                                            .equals("<Rrabatt>")) {
                            felder[i].getTextRange()
                                     .setText(df.format(verkauf.getRabatt()));
                        } else if (felder[i].getDisplayText()
                                            .equals("<Rnummer>")) {
                            felder[i].getTextRange()
                                     .setText(nummernkreis);
                        }
                    }

                    TextTableService tservice = (TextTableService) doc.getTextTableService();

                    ITextTable tabelle = tservice.getTextTable("Tabelle1");
                    if (tabelle != null) {
                        fuelleTabelle(tabelle, propSection);
                    }

                    if (hideOfficeInBackground) {
                        String druckername = settings.getStringProperty(propSection, "Drucker");
                        IPrinter drucker = null;
                        if (druckername == null) {
                            drucker = doc.getPrintService()
                                         .getActivePrinter();
                        } else {
                            drucker = doc.getPrintService()
                                         .createPrinter(druckername);
                        }
                        doc.getPrintService()
                           .setActivePrinter(drucker);
                        short exemplare = settings.getIntegerProperty(propSection, "Exemplare")
                                                  .shortValue();
                        final PrintProperties printprop = new PrintProperties(exemplare, null);
                        doc.getPrintService()
                           .print(printprop);
                        final ITextDocument xdoc = doc;
                        new SwingWorker<Void, Void>() {

                            @Override
                            protected Void doInBackground() throws Exception {
                                Thread.sleep(100);
                                xdoc.close();
                                Thread.sleep(100);
                                return null;
                            }

                        };
                    } else {
//					OOTools.inDenVordergrund(doc);		// w7: öffnet Dokument meist im Hintergrund
                        OOTools.bringDocToFront(application.getDocumentService(), doc, descriptor, url);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Fehler: " + e.getMessage() + "!");
                    e.printStackTrace();
                }
                verkauf.fuehreVerkaufdurch(patid, nummernkreis);
                schreibeUmsatzDaten(nummernkreis, verkauf.getBetragBrutto(), patid);
                verkauf = new Verkauf();
                String[][] werte = verkauf.liefereTabDaten();
                setzeTabellenWerte(werte);
                /*
                 * vkmod.setDataVector(verkauf.liefereTabDaten(), column);
                 */
                edits[0].requestFocus();
                edits[6].setText("0,00");
                edits[7].setText("0,00");
                edits[8].setText("0");
                this.owner.aktiviereFunktion(VerkaufTab.reload);
                selectStandardForm();
            } else {
                JOptionPane.showMessageDialog(null,
                        "Bitte erst Patientenfenster bzw. Kassenfenster öffnen und Patienten bzw. Kasse auswählen!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Keine Rechnungspositionen vorhanden!");
        }
    }

    private void fuelleTabelle(ITextTable tabelle, String propSection) throws TextException {
        ArtikelVerkauf[] positionen = verkauf.liefereArtikel();
        for (int n = 0; n < positionen.length; n++) {
            tabelle.addRow(n + 1, 1);
            for (int m = 0; m < settings.getIntegerProperty(propSection, "Spaltenanzahl"); m++) {
                String spaltenname = settings.getStringProperty(propSection, "Spalte" + (m + 1));
                if (spaltenname.equals("ArtikelID")) {
                    tabelle.getCell(m, n + 1)
                           .getTextService()
                           .getText()
                           .setText(positionen[n].getEan());
                } else if (spaltenname.equals("MwSt")) {
                    tabelle.getCell(m, n + 1)
                           .getTextService()
                           .getText()
                           .setText(new DecimalFormat("0").format(positionen[n].hatVolleMwSt()?satz.vollerSatz():satz.verminderterSatz()));
                } else if (spaltenname.equals("Anzahl")) {
                    tabelle.getCell(m, n + 1)
                           .getTextService()
                           .getText()
                           .setText(df.format(positionen[n].getAnzahl()));
                } else if (spaltenname.equals("Beschreibung")) {
                    tabelle.getCell(m, n + 1)
                           .getTextService()
                           .getText()
                           .setText(positionen[n].getBeschreibung());
                } else if (spaltenname.equals("EinzelPreis")) {
                    tabelle.getCell(m, n + 1)
                           .getTextService()
                           .getText()
                           .setText(df.format(positionen[n].getPreis()) + " €");
                } else if (spaltenname.equals("GesamtPreis")) {
                    tabelle.getCell(m, n + 1)
                           .getTextService()
                           .getText()
                           .setText(df.format(positionen[n].getPreis() * positionen[n].getAnzahl()) + " €");
                } else if (spaltenname.equals("Rabatt")) {
                    tabelle.getCell(m, n + 1)
                           .getTextService()
                           .getText()
                           .setText(df.format(positionen[n].getRabatt()));
                } else if (spaltenname.equals("Bemerkung")) {
                    String inhalt = "";
                    if (positionen[n].getMwst() == 7) {
                        inhalt += "+ ";
                    }
                    if (positionen[n].getMwst() == 19) {
                        inhalt += "* ";
                    }
                    if (positionen[n].getRabatt() != 0) {
                        inhalt += "%";
                    }

                    tabelle.getCell(m, n + 1)
                           .getTextService()
                           .getText()
                           .setText(inhalt);
                } else if (spaltenname.equals("NettoPreis")) {
                    tabelle.getCell(m, n + 1)
                           .getTextService()
                           .getText()
                           .setText(df.format(positionen[n].getPreis() / (1 + (positionen[n].getMwst()))) + " €");
                }
            }
        }
    }

    private void setzeFelderzurueck() {
        edits[0].setText("");
        edits[1].setText("1,00");
        einheitAnzahlLabel.setText("Anzahl");
        edits[2].setText("0,00");
        edits[3].setText("");
        edits[4].setText("");
    }

    private void schreibeUmsatzDaten(String vnummer, double offen, int patid) {
        Date date = new Date(System.currentTimeMillis());
        String sql = "INSERT INTO verkliste (verklisteID, v_nummer, v_datum, v_betrag, v_mwst7, v_mwst19, v_offen, v_bezahldatum, mahndat1, mahndat2, mahndat3, mahnsperre, pat_id, user, ik) "
                + "VALUES (NULL, '" + vnummer + "', '" + date.toString() + "', '" + verkauf.getBetragBrutto() + "', '"
                + verkauf.getBetrag7() + "', '" + verkauf.getBetrag19() + "', '" + offen
                + "', NULL, NULL, NULL, NULL, '0', '" + patid + "', '" + Reha.aktUser + "', '" + Reha.getAktIK()
                + "');";
        SqlInfo.sqlAusfuehren(sql);
    }

    public void aufraeumen() {
        // hier sollten die Listener removed werden
        // anschließend die Listener genullt
    }

}
