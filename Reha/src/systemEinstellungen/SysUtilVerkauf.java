package systemEinstellungen;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterJob;
import java.io.File;

import javax.print.PrintService;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


import CommonTools.ButtonTools;
import CommonTools.INIFile;
import CommonTools.INITool;
import CommonTools.JRtaCheckBox;
import CommonTools.JRtaTextField;
import environment.Path;
import hauptFenster.Reha;
import CommonTools.JCompTools;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SysUtilVerkauf extends JXPanel implements SysInitCommon_If {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String[] druckernamen, spaltenNamen = { "ArtikelID", "Beschreibung", "Anzahl", "EinzelPreis", "GesamtPreis",
            "MwSt", "Rabatt", "Bemerkung", "Nettopreis" };
    private Integer[] spaltenAnzahl = { 1, 2, 3, 4, 5, 6 };

    private JComboBox rechnungSpalte1, rechnungSpalte2, rechnungSpalte3, rechnungSpalte4, rechnungSpalte5,
            rechnungSpalte6, bonSpalte1, bonSpalte2, bonSpalte3, bonSpalte4, bonSpalte5, bonSpalte6, rechnungDrucker,
            bonDrucker, rechnungSpalten, bonSpalten;

    private JButton rechnungVorlageB, bonVorlageB;

    private JRtaCheckBox bonAnpassen, sofortDrucken, bonEnabled;

    private JRtaTextField rechnungVorlage, bonVorlage, rechnungExemplare, bonSeitenlaenge;

    private INIFile inif;

    private ActionListener al;

    SysUtilVorlagen vorlagen = null;
    

    SysUtilVerkauf() {
        super(new BorderLayout());
        // super(new GridLayout(1,1));
        this.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 0));
        activateListener();

        this.setOpaque(false);

        JScrollPane jscr = new JScrollPane();
        jscr.setBorder(null);
        jscr.setOpaque(false);
        jscr.getViewport()
            .setOpaque(false);
        jscr.getVerticalScrollBar()
            .setUnitIncrement(15);
        jscr.setViewportView(getContent());
        jscr.validate();

        //inif = INITool.openIni(Path.Instance.getProghome()+"ini/"+Reha.getAktIK()+"/", "verkauf.ini");
        inif = vorlagen.getInif();

        //add(getContent(),BorderLayout.CENTER);
        ladeEinstellungen();
        this.add(jscr,BorderLayout.CENTER);
//      this.add(getKnopfPanel(),BorderLayout.SOUTH);
        AbbruchOderSpeichern footer = new AbbruchOderSpeichern(this);
        this.add(footer.getPanel(),BorderLayout.SOUTH);
        System.out.println(getWidth()+"/"+getHeight());
    }
/*  
    private JPanel getKnopfPanel(){
        abbruch = ButtonTools.macheButton("abbrechen", "abbrechen", al);
        speichern = ButtonTools.macheButton("speichern", "speicher", al);
                                    //      1.                      2.    3.    4.     5.     6.    7.      8.     9.
        FormLayout jpanlay = new FormLayout("right:max(150dlu;p), 60dlu:g, 60dlu, 4dlu, 60dlu, 20dlu",
       //1.    2. 3.   4.   5.   6.     7.    8. 9.  10.  11. 12. 13.  14.  15. 16.  17. 18.  19.   20.    21.   22.   23.
                "10dlu,p, 10dlu, p");

        PanelBuilder jpan = new PanelBuilder(jpanlay);
        jpan.getPanel()
            .setOpaque(false);
        CellConstraints jpancc = new CellConstraints();

        jpan.addSeparator("", jpancc.xyw(1, 2, 5));
        jpan.add(abbruch, jpancc.xy(3, 4));
        jpan.add(speichern, jpancc.xy(5, 4));
        jpan.addLabel("Änderungen übernehmen?", jpancc.xy(1, 4));

        jpan.getPanel()
            .validate();
        return jpan.getPanel();
    }
 */    

    /**
     * @return
     */
    private JPanel getContent() {

        //                 1      2     3      4     5        6
        String xwerte = "15dlu, 3dlu, 80dlu, 5dlu, 40dlu:g, 15dlu";
        //                 1   2    3   4    5   6    7   8    9  10   11  12   13  14   15  16   17  18   19  20
        String ywerte = "5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p," +
        // ""     21  22   23  24   25  26   27  28   29  30   31  32   33  34   35  36   37  38   39  40
                "5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p," +
        //        41  42   43   44  45  46  47   48   49  50   51  52   53
                "5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu";
        FormLayout lay = new FormLayout(xwerte, ywerte);
        CellConstraints cc = new CellConstraints();
        int rowCnt=2;
        PanelBuilder pane = new PanelBuilder(lay);
        //PanelBuilder pane = new PanelBuilder(lay, new FormDebugPanel());        // debug mode
        pane.setDefaultDialogBorder();
        pane.getPanel()
            .setOpaque(false);

        PrintService[] printers = PrinterJob.lookupPrintServices();     // Druckerliste
        druckernamen = new String[printers.length];
        for (int i = 0; i < printers.length; i++) {
            druckernamen[i] = printers[i].getName();
        }

        
        vorlagen = new SysUtilVorlagen(this);
        vorlagen.setVPfad(Path.Instance.getProghome()+"vorlagen/"+Reha.getAktIK());
        vorlagen.setIni(Path.Instance.getProghome()+"ini/"+Reha.getAktIK(), "verkauf.ini");
        vorlagen.setLabels("Formulare","FormulareAnzahl","Formular");
        vorlagen.activateEditing();
        
        pane.add(vorlagen.getPanel(), cc.xyw(1, rowCnt++, 6));        // 1,2
        
        pane.addSeparator("Rechnungsdruck", cc.xyw(1,++rowCnt,5));    // 1,4
        rowCnt++;

        JXLabel lab = new JXLabel("Sofort drucken?");
        pane.add(lab, cc.xy(3, ++rowCnt));                            // 3,6
        String toolTip = "Ausnahme: 'Adresseingabe von Hand' wird im Verkaufsmodul gewählt";
        lab.setToolTipText(toolTip);
        
        sofortDrucken = new JRtaCheckBox();
        pane.add(sofortDrucken, cc.xy(5, rowCnt++));                // 5,6
        sofortDrucken.setToolTipText(toolTip);

        lab = new JXLabel("Drucker:");
        pane.add(lab, cc.xy(3, ++rowCnt));                            // 3,8
        
        rechnungDrucker = new JComboBox(druckernamen);
        pane.add(rechnungDrucker, cc.xy(5, rowCnt++));                // 5,8

        lab = new JXLabel("Vorlage:");
        pane.add(lab, cc.xy(3, ++rowCnt));                            // 3,10

        rechnungVorlage = new JRtaTextField("nix", false);
        rechnungVorlage.setLayout(new BorderLayout());
        rechnungVorlage.add(rechnungVorlageB = new JXButton("auswählen"), BorderLayout.EAST);
        rechnungVorlageB.setActionCommand("vorlageRechnung");
        rechnungVorlageB.addActionListener(al);
        pane.add(rechnungVorlage, cc.xy(5, rowCnt++));              // 5,10

        lab = new JXLabel("Anzahl Spalten:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        rechnungSpalten = new JComboBox(spaltenAnzahl);
        pane.add(rechnungSpalten, cc.xy(5, rowCnt++));

        lab = new JXLabel("Spalte 1:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        rechnungSpalte1 = new JComboBox(spaltenNamen);
        pane.add(rechnungSpalte1, cc.xy(5, rowCnt++));

        lab = new JXLabel("Spalte 2:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        rechnungSpalte2 = new JComboBox(spaltenNamen);
        pane.add(rechnungSpalte2, cc.xy(5, rowCnt++));

        lab = new JXLabel("Spalte 3:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        rechnungSpalte3 = new JComboBox(spaltenNamen);
        pane.add(rechnungSpalte3, cc.xy(5, rowCnt++));

        lab = new JXLabel("Spalte 4:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        rechnungSpalte4 = new JComboBox(spaltenNamen);
        pane.add(rechnungSpalte4, cc.xy(5, rowCnt++));

        lab = new JXLabel("Spalte 5:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        rechnungSpalte5 = new JComboBox(spaltenNamen);
        pane.add(rechnungSpalte5, cc.xy(5, rowCnt++));

        lab = new JXLabel("Spalte 6:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        rechnungSpalte6 = new JComboBox(spaltenNamen);
        pane.add(rechnungSpalte6, cc.xy(5, rowCnt++));

        lab = new JXLabel("Exemplare:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        rechnungExemplare = new JRtaTextField("ZAHLEN", false);
        pane.add(rechnungExemplare, cc.xy(5, rowCnt++));


        rowCnt ++;
        pane.addSeparator("Bondruck", cc.xyw(1,rowCnt++,5));
        
        lab = new JXLabel("Bondruck erlaubt?");
        pane.add(lab, cc.xy(3, ++rowCnt));
        toolTip = "wenn nicht können nur Rechnungen erstellt werden";
        lab.setToolTipText(toolTip);

        bonEnabled = new JRtaCheckBox();
        pane.add(bonEnabled, cc.xy(5, rowCnt++));
        bonEnabled.setToolTipText(toolTip);

        lab = new JXLabel("Drucker:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        bonDrucker = new JComboBox(druckernamen);
        pane.add(bonDrucker, cc.xy(5, rowCnt++));

        lab = new JXLabel("Vorlage:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        bonVorlage = new JRtaTextField("nix", false);
        bonVorlage.setLayout(new BorderLayout());
        bonVorlage.add(bonVorlageB = new JXButton("auswählen"), BorderLayout.EAST);
        bonVorlageB.setActionCommand("vorlageBon");
        bonVorlageB.addActionListener(al);
        pane.add(bonVorlage, cc.xy(5, rowCnt++));

        lab = new JXLabel("Anzahl Spalten:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        bonSpalten = new JComboBox(spaltenAnzahl);
        pane.add(bonSpalten, cc.xy(5, rowCnt++));

        lab = new JXLabel("Spalte 1:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        bonSpalte1 = new JComboBox(spaltenNamen);
        pane.add(bonSpalte1, cc.xy(5, rowCnt++));

        lab = new JXLabel("Spalte 2:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        bonSpalte2 = new JComboBox(spaltenNamen);
        pane.add(bonSpalte2, cc.xy(5, rowCnt++));

        lab = new JXLabel("Spalte 3:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        bonSpalte3 = new JComboBox(spaltenNamen);
        pane.add(bonSpalte3, cc.xy(5, rowCnt++));

        lab = new JXLabel("Spalte 4:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        bonSpalte4 = new JComboBox(spaltenNamen);
        pane.add(bonSpalte4, cc.xy(5, rowCnt++));

        lab = new JXLabel("Spalte 5:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        bonSpalte5 = new JComboBox(spaltenNamen);
        pane.add(bonSpalte5, cc.xy(5, rowCnt++));

        lab = new JXLabel("Spalte 6:");
        pane.add(lab, cc.xy(3, ++rowCnt));

        bonSpalte6 = new JComboBox(spaltenNamen);
        pane.add(bonSpalte6, cc.xy(5, rowCnt++));

        lab = new JXLabel("Seitenlänge anpassen?");
        pane.add(lab, cc.xy(3, ++rowCnt));

        bonAnpassen = new JRtaCheckBox();
        pane.add(bonAnpassen, cc.xy(5, rowCnt++));

        lab = new JXLabel("Seitenlänge pro Artikel:");
        pane.add(lab, cc.xy(3, ++rowCnt));
        
        bonSeitenlaenge = new JRtaTextField("nix", false);
        bonSeitenlaenge.setLayout(new BorderLayout());
        bonSeitenlaenge.add(new JXLabel("mm * 100"), BorderLayout.EAST);
        pane.add(bonSeitenlaenge, cc.xy(5, rowCnt++));

        pane.getPanel()
            .validate();

        return pane.getPanel();
    }

    private String dateiWaehlen() {
        String returnstmt = "";
        JFileChooser explorer = new JFileChooser();
        explorer.setDialogType(JFileChooser.OPEN_DIALOG);
        explorer.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        explorer.setCurrentDirectory(new File(Path.Instance.getProghome() + "/vorlagen"));

        explorer.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName()
                                           .toLowerCase()
                                           .endsWith(".ott");
            }

            @Override
            public String getDescription() {
                return "OpenOffice.org Vorlagen";
            }
        });

        explorer.setVisible(true);

        int state = explorer.showOpenDialog(null);

        if (state == JFileChooser.APPROVE_OPTION) {
            returnstmt = explorer.getSelectedFile()
                                 .getName();
        } else {

        }

        return returnstmt;
    }

    private void ladeEinstellungen() {
        bonAnpassen.setSelected(inif.getBooleanProperty("Bon", "SeitenLaengeAendern"));
        sofortDrucken.setSelected(inif.getBooleanProperty("Bon", "SofortDrucken"));
        bonEnabled.setSelected(inif.getBooleanProperty("Bon", "BonDruckErlaubt"));

        rechnungSpalte1.setSelectedItem(inif.getStringProperty("Rechnung", "Spalte1"));
        rechnungSpalte2.setSelectedItem(inif.getStringProperty("Rechnung", "Spalte2"));
        rechnungSpalte3.setSelectedItem(inif.getStringProperty("Rechnung", "Spalte3"));
        rechnungSpalte4.setSelectedItem(inif.getStringProperty("Rechnung", "Spalte4"));
        rechnungSpalte5.setSelectedItem(inif.getStringProperty("Rechnung", "Spalte5"));
        rechnungSpalte6.setSelectedItem(inif.getStringProperty("Rechnung", "Spalte6"));

        bonSpalte1.setSelectedItem(inif.getStringProperty("Bon", "Spalte1"));
        bonSpalte2.setSelectedItem(inif.getStringProperty("Bon", "Spalte2"));
        bonSpalte3.setSelectedItem(inif.getStringProperty("Bon", "Spalte3"));
        bonSpalte4.setSelectedItem(inif.getStringProperty("Bon", "Spalte4"));
        bonSpalte5.setSelectedItem(inif.getStringProperty("Bon", "Spalte5"));
        bonSpalte6.setSelectedItem(inif.getStringProperty("Bon", "Spalte6"));

        rechnungDrucker.setSelectedItem(inif.getStringProperty("Rechnung", "Drucker"));
        bonDrucker.setSelectedItem(inif.getStringProperty("Bon", "Drucker"));

        rechnungSpalten.setSelectedItem(inif.getIntegerProperty("Rechnung", "Spaltenanzahl"));
        bonSpalten.setSelectedItem(inif.getIntegerProperty("Bon", "Spaltenanzahl"));

        rechnungVorlage.setText(inif.getStringProperty("Rechnung", "Vorlage"));
        bonVorlage.setText(inif.getStringProperty("Bon", "Vorlage"));

        rechnungExemplare.setText(inif.getStringProperty("Rechnung", "Exemplare"));

        bonSeitenlaenge.setText(inif.getStringProperty("Bon", "ProArtikelSeitenLaenge"));

        vorlagen.readFromIni();
    }

    private void speicherEinstellungen() {
        try {
            inif.setBooleanProperty("Bon", "SeitenLaengeAendern", bonAnpassen.isSelected(), null);
            inif.setBooleanProperty("Bon", "BonDruckErlaubt", bonEnabled.isSelected(), null);

            inif.setBooleanProperty("Bon", "SofortDrucken", sofortDrucken.isSelected(), null);
            inif.setBooleanProperty("Rechnung", "SofortDrucken", sofortDrucken.isSelected(), null);

            inif.setStringProperty("Rechnung", "Spalte1", (String) rechnungSpalte1.getSelectedItem(), null);
            inif.setStringProperty("Rechnung", "Spalte2", (String) rechnungSpalte2.getSelectedItem(), null);
            inif.setStringProperty("Rechnung", "Spalte3", (String) rechnungSpalte3.getSelectedItem(), null);
            inif.setStringProperty("Rechnung", "Spalte4", (String) rechnungSpalte4.getSelectedItem(), null);
            inif.setStringProperty("Rechnung", "Spalte5", (String) rechnungSpalte5.getSelectedItem(), null);
            inif.setStringProperty("Rechnung", "Spalte6", (String) rechnungSpalte6.getSelectedItem(), null);

            inif.setStringProperty("Bon", "Spalte1", (String) bonSpalte1.getSelectedItem(), null);
            inif.setStringProperty("Bon", "Spalte2", (String) bonSpalte2.getSelectedItem(), null);
            inif.setStringProperty("Bon", "Spalte3", (String) bonSpalte3.getSelectedItem(), null);
            inif.setStringProperty("Bon", "Spalte4", (String) bonSpalte4.getSelectedItem(), null);
            inif.setStringProperty("Bon", "Spalte5", (String) bonSpalte5.getSelectedItem(), null);
            inif.setStringProperty("Bon", "Spalte6", (String) bonSpalte6.getSelectedItem(), null);

            inif.setStringProperty("Rechnung", "Drucker", (String) rechnungDrucker.getSelectedItem(), null);
            inif.setStringProperty("Bon", "Drucker", (String) bonDrucker.getSelectedItem(), null);

            inif.setStringProperty("Rechnung", "Spaltenanzahl", String.valueOf(rechnungSpalten.getSelectedItem()),
                    null);
            inif.setStringProperty("Bon", "Spaltenanzahl", String.valueOf(bonSpalten.getSelectedItem()), null);

            inif.setStringProperty("Rechnung", "Vorlage", rechnungVorlage.getText(), null);
            inif.setStringProperty("Bon", "Voralge", bonVorlage.getText(), null);

            inif.setStringProperty("Rechnung", "Exemplare", rechnungExemplare.getText(), null);

            inif.setStringProperty("Bon", "ProArtikelSeitenLaenge", bonSeitenlaenge.getText(), null);

            boolean formok = vorlagen.saveToIni();

            INITool.saveIni(inif);
            JOptionPane.showMessageDialog(null, "Konfiguration erfolgreich in verkauf.ini gespeichert.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Fehler beim speichern der Konfiguration in verkauf.ini!!!");
        }

    }

    private void activateListener() {
        al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(arg0.getActionCommand()
                        .equals("vorlageRechnung")) {
                    String sdummy = dateiWaehlen();
                    rechnungVorlage.setText((sdummy.equals("") ? rechnungVorlage.getText() : sdummy));
                } else if (arg0.getActionCommand()
                               .equals("vorlageBon")) {
                    String sdummy = dateiWaehlen();
                    bonVorlage.setText((sdummy.equals("") ? bonVorlage.getText() : sdummy));
                }
            }
        };
    }

    @Override
    public void Abbruch() {
                    SystemInit.abbrechen();
                }

    @Override
    public void Speichern() {
        speicherEinstellungen();
    }

    @Override
    public void AddEntry(int instanceNb) {
        // TODO Auto-generated method stub
        
            }

    @Override
    public void RemoveEntry(int instanceNb) {
        // TODO Auto-generated method stub
        
    }
}
