package patientenFenster;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.LinearGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.MattePainter;

import abrechnung.Disziplinen;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.Colors;
import CommonTools.DatFunk;
import CommonTools.JCompTools;
import CommonTools.JRtaCheckBox;
import CommonTools.JRtaComboBox;
import CommonTools.JRtaTextField;
import CommonTools.SqlInfo;
import systemTools.ListenerTools;
import CommonTools.StringTools;
import Suchen.ICDrahmen;
import abrechnung.Disziplinen;
import environment.LadeProg;
import environment.Path;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import events.RehaTPEvent;
import events.RehaTPEventClass;
import events.RehaTPEventListener;
import gui.Cursors;
import hauptFenster.Reha;
import hmrCheck.HMRCheck;
import rechteTools.Rechte;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.SystemPreislisten;
import systemTools.ListenerTools;
import CommonTools.DatFunk;
import commonData.Rezept;

public class RezNeuanlage extends JXPanel implements ActionListener, KeyListener,FocusListener,RehaTPEventListener{

    /**
	 * McM '18: Umbau Struktur
	 * 	Konzept: 
	 * 		- Rezeptvector 'vec' wird ersetzt durch 'myRezept' (Instanz der Klasse 'Rezept'; Zugriff über get/set)
	 * 		- zu Beginn ist myRezept entweder leer (komplette Neuanlage) oder enthaelt Daten der Kopiervorlage
	 * 		- ein neues Rezept wird mit Daten aus dem Patienten-Record u. der gewählten Rezeptklasse initialisiert
	 * 		- ein kopiertes Rezept wird zuerst bereinigt (Behandlungen, Zuzahlung, ... entfernen) 
	 * 		- Eintragen der Daten in's Rezeptformular u. Auslesen aus demselben jeweils mit 1 zentralen Funktion
	 * 		- Schreiben der Rezeptdaten in die DB uebernimmt die entspr. Fkt der Klasse 'Rezept'
	 * 		- Fkt.:
	 * 			ladeZusatzDatenAlt/Neu() -> initRezept*()
	 * 			doSpeichernAlt/Neu -> copyFormToVec(), copyFormToVec1stTime()
	 * 
    */
    // Lemmi Doku: Das sind die Text-Eingabefgelder im Rezept
    public JRtaTextField[] jtf = {null,null,null,null,null,
                                null,null,null,null,null,
                                null,null,null,null,null,
                                null,null,null,null,null,
                                null,null,null,null,null,
                                null,null,null,null,null,
                                null,null};
    // Lemmi 20101231: Harte Index-Zahlen für "jtf" durch sprechende Konstanten ersetzt !
    final int cKTRAEG = 0;
    final int cARZT   = 1;
    final int cREZDAT = 2;
    final int cBEGINDAT = 3;
    final int cANZ1   = 4;        // ACHTUNG die Positionen cANZ1 bis cANZ4 müssen immer nacheinander definiert sein
    final int cANZ2   = 5;
    final int cANZ3   = 6;
    final int cANZ4   = 7;
    final int cFREQ   = 8;
    final int cDAUER  = 9;
    final int cANGEL   = 10;
    final int cKASID   = 11;
    final int cARZTID  = 12;
    final int cPREISGR = 13;
    final int cHEIMBEW = 14;
    final int cBEFREIT = 15;
    final int cPOS1    = 16;    // ACHTUNG die Positionen cPOS1 bis cPOS4 müssen immer nacheinander definiert sein
    final int cPOS2    = 17;
    final int cPOS3    = 18;
    final int cPOS4    = 19;
    final int cPREIS1    = 20;    // ACHTUNG die Positionen cPREIS1 bis cPREIS4 müssen immer nacheinander definiert sein
    final int cPREIS2    = 21;
    final int cPREIS3    = 22;
    final int cPREIS4    = 23;
    final int cANLAGDAT  = 24;
    final int cANZKM     = 25;
    final int cPATID     = 26;
    final int cPATINT    = 27;
    final int cZZSTAT    = 28;
    final int cHEIMBEWPATSTAM = 29;
    final int cICD10 = 30;
    final int cICD10_2 = 31;

    // Lemmi 20101231: Merken der Originalwerte der eingelesenen Textfelder, Combo- und Check-Boxen
    Vector<Object> originale = new Vector<Object>();

    public JRtaCheckBox[] jcb = {null,null,null,null};
    // Lemmi 20101231: Harte Index-Zahlen für "jcb" durch sprechende Konstanten ersetzt !
    final int cBEGRADR = 0;
    final int cHAUSB   = 1;
    final int cTBANGEF = 2;
    final int cVOLLHB  = 3;

    public JRtaComboBox[] jcmb = {null,null,null,null,null,null,null,null,null};
    // Lemmi 20101231: Harte Index-Zahlen für "jcmb" durch sprechende Konstanten ersetzt !
    final int cRKLASSE = 0;
    final int cVERORD  = 1;
    final int cLEIST1  = 2;    // ACHTUNG die Positionen cLEIST1 bis cLEIST4 müssen immer nacheinander definiert sein
    final int cLEIST2  = 3;
    final int cLEIST3  = 4;
    final int cLEIST4  = 5;
    final int cINDI    = 6;
    final int cBARCOD  = 7;
    final int cFARBCOD = 8;


    public JTextArea jta = null;

    public JButton speichern = null;
    public JButton abbrechen = null;
    public JButton hmrcheck = null;

    public boolean neu = false;
    public String feldname = "";

    // Lemmi 20110101: strKopiervorlage zugefügt. Kopieren des letzten Rezepts des selben Patienten bei Rezept-Neuanlage
    public String strKopiervorlage = "";

	public Vector<String> vec = null;  // Lemmi Doku: Das bekommt den 'vecaktrez' aus dem rufenden Programm (AktuelleRezepte)	// McM:loeschen (-> Rezept-Klasse)
    public Vector<Vector<String>> preisvec = null;
    private boolean klassenReady = false;
    private boolean initReady = false;
    private static final long serialVersionUID = 1L;
    private int preisgruppe = -1;
    public boolean feldergefuellt = false;
    private String nummer = null;
	private String rezKlasse = null;
    private String[] farbcodes = {null,null,null,null,null,null,null,null,null,null};

    private String aktuelleDisziplin = "";
    private int preisgruppen[] = {0,0,0,0,0,0,0,0};
    int[] comboid = {-1,-1,-1,-1};

    MattePainter mp = null;
    LinearGradientPaint p = null;
    private RehaTPEventClass rtp = null;

    JLabel kassenLab;
    JLabel arztLab;

    String[] strRezepklassenAktiv = null;

    // McM 16/11: Steuerung der Abkürzungen bei Rezepteingabe
    private boolean ctrlIsPressed = false;
    private Component eingabeRezDate = null;
    private Component eingabeBehFrequ = null;
    private Component eingabeVerordnArt = null;
    private Component eingabeVerordn1 = null;
    private Component eingabeICD = null;
    private Connection connection;

	private Rezept myRezept = null;
	private Disziplinen diszis = null;
	
	public RezNeuanlage(Vector<String> vec,boolean neu,String sfeldname,Connection connection){	// McM: sfeldname scheint unbenutzt; statt vec evtl rezNr übergeben?
        super();
        try{
            this.neu = neu;
            this.feldname = sfeldname;
			this.vec = vec;  // Lemmi 20110106  Wird auch fuer das Kopieren verwendet !!!!
			myRezept = new Rezept();
//			myRezept.init("KG18330");	// Bsp.
			myRezept.setVec_rez(vec);
            diszis = new Disziplinen();

            if( vec.size() > 0 && this.neu ) {
				aktuelleDisziplin = RezTools.putRezNrGetDisziplin(vec.get(1) );		//McM: putRezNrGetDisziplin nach Disziplinen versch.? 
            }

            setName("RezeptNeuanlage");
            rtp = new RehaTPEventClass();
            rtp.addRehaTPEventListener((RehaTPEventListener) this);

            addKeyListener(this);

            setLayout(new BorderLayout());
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            add(getDatenPanel(),BorderLayout.CENTER);
            add(getButtonPanel(),BorderLayout.SOUTH);
            setBackgroundPainter(Reha.instance.compoundPainter.get("RezNeuanlage"));
            validate();
            SwingUtilities.invokeLater(new Runnable(){
                    public  void run()
                    {
                        setzeFocus();
                    }
            });
            initReady = true;
            if(!neu){
                if(!Rechte.hatRecht(Rechte.Rezept_editvoll, false)){  // Lemmi Doku: Das sieht aus wie der Read-Only-Modus für das Rezept
                    for(int i = 0; i < jtf.length; i++){  // Lemmi Doku: alle Textfelder unbedienbar machen
                        if(jtf[i] != null){
                            jtf[i].setEnabled(false);
                        }
                    }
                    for(int i = 0; i < jcb.length;i++){  // Lemmi Doku: alle CheckBoxen unbedienbar machen
                        if(jcb[i] != null){
                            jcb[i].setEnabled(false);
                        }
                    }
                    for(int i = 0; i < jcmb.length;i++){ // Lemmi Doku: alle ComboBoxen unbedienbar machen
                        if(jcmb[i] != null){
                            jcmb[i].setEnabled(false);
                        }
                    }
                }
            }

        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Fehler im Konstruktor RezNeuanlage\n"+RezNeuanlage.makeStacktraceToString(ex));
        }

    }

    public void macheFarbcodes(){
        try{
            farbcodes[0] = "kein Farbcode";
            jcmb[cFARBCOD].addItem(farbcodes[0]);

            for (int i = 0;i < 9;i++){
                farbcodes[i+1] = SystemConfig.vSysColsBedeut.get(i+14);
                jcmb[cFARBCOD].addItem(farbcodes[i+1]);
            }
            if(! this.neu){
				int itest = myRezept.getFarbCode();
                if(itest >= 0){
                    jcmb[cFARBCOD].setSelectedItem( (String)SystemConfig.vSysColsBedeut.get(itest) );
                }else{
                    jcmb[cFARBCOD].setSelectedIndex(0);
                }
            }else{
                jcmb[cFARBCOD].setSelectedIndex(0);
            }

        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,"Fehler bei Farbcodes erstellen\n"+ex.getMessage());
        }

    }
    public void setzeFocus(){
        SwingUtilities.invokeLater(new Runnable(){
                public  void run()
                {
                    if(neu){
                        int aid,kid;
                        boolean beenden = false;
                        String meldung = "";
                        kid = StringTools.ZahlTest(jtf[cKASID].getText());
                        aid = StringTools.ZahlTest(jtf[cARZTID].getText());
                        if(kid < 0 && aid < 0){
                            beenden = true;
                            meldung = "Achtung - dem Patientenstamm ist keine verwertbare Krankenkasse\n"+
                            "sowie kein verwertbarer Arzt zugeordnet\n\n"+
                            "Gehen Sie im Patientenstamm auf ->Ändern/Editieren<- und ordnen Sie verwertaber Daten zu!";
                        }else if(kid >= 0 && aid < 0){
                            beenden = true;
                            meldung = "Achtung - dem Patientenstamm ist kein verwertbarer Arzt zugeordnet\n\n"+
                            "Gehen Sie im Patientenstamm auf ->Ändern/Editieren<- und ordnen Sie verwertaber Daten zu!";
                        }else if(kid < 0 && aid >= 0){
                            beenden = true;
                            meldung = "Achtung - dem Patientenstamm ist keine verwertbare Krankenkasse zugeordnet\n\n"+
                            "Gehen Sie im Patientenstamm auf ->Ändern/Editieren<- und ordnen Sie verwertaber Daten zu!";
                        }
                        if(beenden){
                        JOptionPane.showMessageDialog(null,meldung);
                        aufraeumen();
                        ((JXDialog)getParent().getParent().getParent().getParent().getParent()).dispose();
                        }else{
                            holePreisGruppe(jtf[cKASID].getText().trim());
                            SwingUtilities.invokeLater(new Runnable(){
                                public  void run()
                                {
                                    jcmb[cRKLASSE].requestFocus();
                                }
                            });
                        }
	 			   // else bedeutet nicht neu - sondern aendern
                    }else{
                        int aid,kid;
                        //boolean beenden = false;
                        //String meldung = "";
                        kid = StringTools.ZahlTest(jtf[cKASID].getText());
                        aid = StringTools.ZahlTest(jtf[cARZTID].getText());
//		 			   if(kid < 0 && aid < 0){
//		 				   jtf[cKASID].setText(Integer.toString(Reha.instance.patpanel.kid));
//		 				   jtf[cARZTID].setText(Integer.toString(Reha.instance.patpanel.aid));
//		 				   jtf[cKTRAEG].setText(Reha.instance.patpanel.patDaten.get(13));
//		 			   }else if(kid >= 0 && aid < 0){
//		 				   jtf[cARZTID].setText(Integer.toString(Reha.instance.patpanel.aid));
//		 			   }else if(kid < 0 && aid >= 0){
//		 				   jtf[cKASID].setText(Integer.toString(Reha.instance.patpanel.kid));
//		 				   jtf[cKTRAEG].setText(Reha.instance.patpanel.patDaten.get(13));
//		 			   }else{
                            //System.out.println("*****************Keine Preisgruppen bezogen*******************");
                        //preisgruppen
                        //RezTools.holePreisVector(vec.get(1), Integer.parseInt(vec.get(41))-1);
                        //ladePreise();
//		 			   }
		 			   if(kid < 0){
		 				   jtf[cKASID].setText(Integer.toString(Reha.instance.patpanel.kid));
		 				   jtf[cKTRAEG].setText(Reha.instance.patpanel.patDaten.get(13));
                        }
		 			   if(aid < 0){
		 				   jtf[cARZTID].setText(Integer.toString(Reha.instance.patpanel.aid));
		 			   }

                        SwingUtilities.invokeLater(new Runnable(){
                                public  void run()
                                {
                                    jtf[cKTRAEG].requestFocus();
                                }
                        });
                    }

                }
        });
    }



    public JXPanel getButtonPanel(){
        JXPanel    jpan = JCompTools.getEmptyJXPanel();
        jpan.addKeyListener(this);
        jpan.setOpaque(false);
        FormLayout lay = new FormLayout(
                // 1                2          3             4      5               6          7
                "fill:0:grow(0.25),50dlu,fill:0:grow(0.25),50dlu,fill:0:grow(0.25),50dlu,fill:0:grow(0.25)",
                // 1  2  3
                "5dlu,p,5dlu");
        CellConstraints cc = new CellConstraints();
        jpan.setLayout(lay);
        speichern = new JButton("speichern");
        speichern.setActionCommand("speichern");
        speichern.addActionListener(this);
        speichern.addKeyListener(this);
        speichern.setMnemonic(KeyEvent.VK_S);
        jpan.add(speichern,cc.xy(2,2));

        hmrcheck = new JButton("HMR-Check");
        hmrcheck.setActionCommand("hmrcheck");
        hmrcheck.addActionListener(this);
        hmrcheck.addKeyListener(this);
        hmrcheck.setMnemonic(KeyEvent.VK_H);
        jpan.add(hmrcheck,cc.xy(4,2));

        abbrechen = new JButton("abbrechen");
        abbrechen.setActionCommand("abbrechen");
        abbrechen.addActionListener(this);
        abbrechen.addKeyListener(this);
        abbrechen.setMnemonic(KeyEvent.VK_A);
        jpan.add(abbrechen,cc.xy(6,2));

        return jpan;
    }

    /********************************************/

    // Lemmi 20101231: Merken der Originalwerte der eingelesenen Textfelder
    // ACHTUNG: Die Reihenfolge der Abfragen muß in SaveChangeStatus() und HasChanged() exakt identisch sein !
    private void SaveChangeStatus(){
        int i;
        originale.clear();  // vorherige Merkung wegwerfen

        // Alle Text-Eingabefelder
        for ( i = 0; i < jtf.length; i++ ) {
            //String strText = jtf[i].getText();
            originale.add( jtf[i].getText() );
        }

        // Das Feld mit "Ärztliche Diagnose"
        originale.add( jta.getText() );

        // alle ComboBoxen
        for ( i = 0; i < jcmb.length; i++ ) {
            originale.add( (Integer)jcmb[i].getSelectedIndex() );  // Art d. Verordn. etc.
        }

        // alle CheckBoxen
        for ( i = 0; i < jcb.length; i++ ) {
            originale.add( (Boolean)(jcb[i].isSelected() ) );  //
        }
    }

    // Lemmi 20101231: prüft, ob sich Einträge geändert haben
    // ACHTUNG: Die Reihenfolge der Abfragen muß in SaveChangeStatus() und HasChanged() exakt identisch sein !
    public Boolean HasChanged(){
        int i, idx = 0;

        // Alle Text-Eingabefelder
        for ( i = 0; i < jtf.length; i++) {
            if(! jtf[i].getText().equals( originale.get(idx++) ) )
                return true;
        }

        // Das Feld mit "Ärztliche Diagnose"
        if(! jta.getText().equals( originale.get(idx++) ) )       // Ärztliche Diagnose
            return true;

        // alle ComboBoxen
        for ( i = 0; i < jcmb.length; i++) {    // ComboBoxen
            if( jcmb[i].getSelectedIndex() != (Integer)originale.get(idx++) )    // Art d. Verordn. etc.
                return true;
        }

        // alle CheckBoxen
        for ( i = 0; i < jcb.length; i++) {        // CheckBoxen
            if( jcb[i].isSelected() != (Boolean)originale.get(idx++) )    // Begründung außer der Regel vorhanden ? .....
                return true;
        }

        return false;
    }

    // Lemmi 20101231: Stndard-Abfrage nach Prüfung, ob sich Einträge geändert haben
    // fragt nach, ob wirklich ungesichert abgebrochen werden soll !
    public int askForCancelUsaved(){
/*        if ( JOptionPane.NO_OPTION
        == JOptionPane.showConfirmDialog(null, "Es wurden Rezept-Anngaben geändert!\nWollen sie die Änderung(en) wirklich verwerfen?",
                                                "Angaben wurden geändert", JOptionPane.YES_NO_OPTION ) ) {
*/
        String[] strOptions = {"ja", "nein"};  // Defaultwert euf "nein" gesetzt !
        return JOptionPane.showOptionDialog(null, "Es wurden Rezept-Anngaben geändert!\nWollen sie die Änderung(en) wirklich verwerfen?",
                "Angaben wurden geändert",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                strOptions, strOptions[1] );
    }

    /**
    * @return
    */
    private JScrollPane getDatenPanel(){  //1                  2      3    4          5              6      7        8
        FormLayout lay = new FormLayout("right:max(80dlu;p), 4dlu, 60dlu, 5dlu, right:max(60dlu;p), 4dlu, 60dlu",
                //1.   2.   3.   4.   5.   6   7   8    9   10   11  12  13  14    15   16   17  18 19   20   21  22   23  24   25
                    "p, 10dlu, p, 5dlu,  p, 5dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 10dlu, p, 10dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, " +
        //26   27   28   29  30   31   32    33   34   35   36    37 38     39  40   41    42
        "10dlu, p, 10dlu, p, 2dlu, p, 2dlu,  p,  10dlu, p, 10dlu, p,10dlu,p,10dlu,30dlu,2dlu");


        CellConstraints cc = new CellConstraints();
        PanelBuilder jpan = new PanelBuilder(lay);
        jpan.setDefaultDialogBorder();
        jpan.getPanel().setOpaque(false);
        JScrollPane jscr = null;
        //String ywerte = "";

        try{
        // Lemmi 20101231: Harte Index-Zahlen für "jtf" durch sprechende Konstanten ersetzt !
        jtf[cKTRAEG] = new JRtaTextField("NIX",false); // kasse/kostenträger
        jtf[cARZT]   = new JRtaTextField("NIX",false); // arzt
        jtf[cREZDAT] = new JRtaTextField("DATUM",true); // rezeptdatum
        jtf[cBEGINDAT] = new JRtaTextField("DATUM",true); // spätester beginn
        jtf[cANZ1]   = new JRtaTextField("ZAHLEN",true); // Anzahl 1
        jtf[cANZ2]   = new JRtaTextField("ZAHLEN",true); // Anzahl 2
        jtf[cANZ3]   = new JRtaTextField("ZAHLEN",true); // Anzahl 3
        jtf[cANZ4]   = new JRtaTextField("ZAHLEN",true); // Anzahl 4
        jtf[cFREQ]   = new JRtaTextField("GROSS",true); // Frequenz
        jtf[cDAUER]  = new JRtaTextField("ZAHLEN",true); // Dauer
        jtf[cANGEL]  = new JRtaTextField("GROSS",true); // angelegt von
        jtf[cKASID]  = new JRtaTextField("GROSS",false); //kassenid
        jtf[cARZTID] = new JRtaTextField("GROSS",false); //arztid
        // ************ manches / nicht alles nachfolgende muss noch eingebaut werden.....
        jtf[cPREISGR] = new JRtaTextField("GROSS",false); //preisgruppe
        jtf[cHEIMBEW] = new JRtaTextField("GROSS",false); //heimbewohner
        jtf[cBEFREIT] = new JRtaTextField("GROSS",false); //befreit
        jtf[cPOS1] = new JRtaTextField("",false); //POS1
        jtf[cPOS2] = new JRtaTextField("",false); //POS2
        jtf[cPOS3] = new JRtaTextField("",false); //POS3
        jtf[cPOS4] = new JRtaTextField("",false); //POS4
        jtf[cPREIS1] = new JRtaTextField("",false); //PREIS1
        jtf[cPREIS2] = new JRtaTextField("",false); //PREIS2
        jtf[cPREIS3] = new JRtaTextField("",false); //PREIS3
        jtf[cPREIS4] = new JRtaTextField("",false); //PREIS4
        jtf[cANLAGDAT] = new JRtaTextField("DATUM",false); // ANLAGEDATUM
        jtf[cANZKM]    = new JRtaTextField("",false); // KILOMETER
        jtf[cPATID]    = new JRtaTextField("",false); //id von Patient
        jtf[cPATINT]   = new JRtaTextField("",false); //pat_intern von Patient
        jtf[cZZSTAT]   = new JRtaTextField("",false); //zzstatus
        jtf[cHEIMBEWPATSTAM] = new JRtaTextField("",false); //Heimbewohner aus PatStamm
        jtf[cICD10] = new JRtaTextField("GROSS",false); //1. ICD10-Code
        jtf[cICD10_2] = new JRtaTextField("GROSS",false); //2. ICD10-Code
        jcmb[cRKLASSE] = new JRtaComboBox();
        int lang = SystemConfig.rezeptKlassenAktiv.size();
//        strRezepklassenAktiv = new String[lang];
//        for(int i = 0;i < lang;i++){
//            jcmb[cRKLASSE].addItem(SystemConfig.rezeptKlassenAktiv.get(i).get(0));
//            // Lemmi 20110106: Belegung der Indices zur ComboBox für spätere Auswahlen:
//            strRezepklassenAktiv[i] = SystemConfig.rezeptKlassenAktiv.get(i).get(1);  // hier speichern wir die Kürzel für spätere Aktivitäten
//        }
        strRezepklassenAktiv = diszis.getActiveRK();
        jcmb[cRKLASSE]=diszis.getComboBoxActiveRK();

        if(SystemConfig.AngelegtVonUser) {
            jtf[cANGEL].setText(Reha.aktUser);
            jtf[cANGEL].setEditable(false);
        }

        jpan.addLabel("Rezeptklasse auswählen",cc.xy(1, 3));
        jpan.add(jcmb[cRKLASSE],cc.xyw(3, 3,5));
        jcmb[cRKLASSE].setActionCommand("rezeptklasse");
        jcmb[cRKLASSE].addActionListener(this);
        allowShortCut((Component)jcmb[cRKLASSE],"RezeptClass");
        /********************/

        if(this.neu){
            jcmb[cRKLASSE].setSelectedItem(SystemConfig.initRezeptKlasse);
        }else{
            for(int i = 0;i < lang;i++){
//				if(this.vec.get(1).substring(0,2).equals(SystemConfig.rezeptKlassenAktiv.get(i).get(1))){
				if(myRezept.getRezClass().equals(SystemConfig.rezeptKlassenAktiv.get(i).get(1))){
                    jcmb[cRKLASSE].setSelectedIndex(i);
                }
            }
            jcmb[cRKLASSE].setEnabled(false);
        }


        jpan.addSeparator("Rezeptkopf", cc.xyw(1,5,7));

        kassenLab = new JLabel("Kostenträger");
        kassenLab.setIcon(SystemConfig.hmSysIcons.get("kleinehilfe"));
        kassenLab.setHorizontalTextPosition(JLabel.LEFT);
        kassenLab.addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent ev){
                if(!Rechte.hatRecht(Rechte.Rezept_editvoll,false)){
                    return;
                }
                if(jtf[cKTRAEG].getText().trim().startsWith("?")){
                    jtf[cKTRAEG].requestFocus();
                }else{
                    jtf[cKTRAEG].setText("?"+jtf[cKTRAEG].getText().trim());
                    jtf[cKTRAEG].requestFocus();
                }
                String[] suchkrit = new String[] {jtf[cKTRAEG].getText().replace("?", ""),jtf[cKASID].getText()};
                jtf[cKTRAEG].setText(String.valueOf(suchkrit[0]));
                kassenAuswahl(suchkrit);
            }
        });

        jtf[cKTRAEG].setName("ktraeger");
        jtf[cKTRAEG].addKeyListener(this);
        allowShortCut((Component)jtf[cKTRAEG],"ktraeger");
        jpan.add(kassenLab,cc.xy(1,7));
        jpan.add(jtf[cKTRAEG],cc.xy(3,7));

        arztLab = new JLabel("verordn. Arzt");
        arztLab.setIcon(SystemConfig.hmSysIcons.get("kleinehilfe"));
        arztLab.setHorizontalTextPosition(JLabel.LEFT);
        arztLab.addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent ev){
                if(!Rechte.hatRecht(Rechte.Rezept_editvoll,false)){
                    return;
                }
                if(jtf[cARZT].getText().trim().startsWith("?")){
                    jtf[cARZT].requestFocus();
                }else{
                    jtf[cARZT].setText("?"+jtf[cARZT].getText().trim());
                    jtf[cARZT].requestFocus();
                }
                String[] suchkrit = new String[] {jtf[cARZT].getText().replace("?", ""),jtf[cARZTID].getText()};
                jtf[cARZT].setText(String.valueOf(suchkrit[0]));
                arztAuswahl(suchkrit);
            }
        });

        jtf[cARZT].setName("arzt");
        jtf[cARZT].addKeyListener(this);
        jpan.add(arztLab,cc.xy(5,7));
        jpan.add(jtf[cARZT],cc.xy(7,7));


        jtf[cREZDAT].setName("rez_datum");
        //jtf[cREZDAT].setName("rez_datum");
        allowShortCut((Component)jtf[cREZDAT],"rez_datum");
        jpan.addLabel("Rezeptdatum",cc.xy(1,9));
        jpan.add(jtf[cREZDAT],cc.xy(3,9));
        eingabeRezDate = jpan.add(jtf[cREZDAT],cc.xy(3,9));

        //jtf[cBEGINDAT].setName("lastdate");
        allowShortCut((Component)jtf[cBEGINDAT],"lastdate");
        jpan.addLabel("spätester Beh.Beginn",cc.xy(5,9));
        jpan.add(jtf[cBEGINDAT],cc.xy(7,9));

        jcmb[cVERORD] = new JRtaComboBox(new String[] {"Erstverordnung","Folgeverordnung", "außerhalb des Regelfalles"});
        jcmb[cVERORD].setActionCommand("verordnungsart");
        jcmb[cVERORD].addActionListener(this);
        allowShortCut((Component)jcmb[cVERORD],"selArtDerVerordn");
        jpan.addLabel("Art d. Verordn.",cc.xy(1, 11));
        eingabeVerordnArt = jpan.add(jcmb[cVERORD],cc.xy(3, 11));

        jcb[cBEGRADR] = new JRtaCheckBox("vorhanden");
        jcb[cBEGRADR].setOpaque(false);
        jcb[cBEGRADR].setEnabled(false);
        allowShortCut((Component)jcb[cBEGRADR],"adrCheck");
        jpan.addLabel("Begründ. für adR",cc.xy(5, 11));
        jpan.add(jcb[cBEGRADR],cc.xy(7, 11));

        jcb[cHAUSB] = new JRtaCheckBox("Ja / Nein");
        jcb[cHAUSB].setOpaque(false);
        jcb[cHAUSB].setActionCommand("Hausbesuche");
        jcb[cHAUSB].addActionListener(this);
        allowShortCut((Component)jcb[cHAUSB],"hbCheck");
        jpan.addLabel("Hausbesuch",cc.xy(1, 13));
        jpan.add(jcb[cHAUSB],cc.xy(3, 13));

        jcb[cVOLLHB] = new JRtaCheckBox("abrechnen");
        jcb[cVOLLHB].setOpaque(false);
        jcb[cVOLLHB].setToolTipText("Nur aktiv wenn Patient Heimbewohner und Hausbesuch angekreuzt");
        jpan.addLabel("volle HB-Gebühr",cc.xy(5,13));
        if(neu){
            jcb[cVOLLHB].setEnabled(false);
            jcb[cVOLLHB].setSelected(false);
        }else{
            if(Reha.instance.patpanel.patDaten.get(44).equals("T")){
                // Wenn Heimbewohner
				if(myRezept.getHausbesuch()){
                    jcb[cVOLLHB].setEnabled(true);
					jcb[cVOLLHB].setSelected( (myRezept.getHbVoll() ? true : false));
                }else{
                    jcb[cVOLLHB].setEnabled(false);
                    jcb[cVOLLHB].setSelected(false);
                }
            }else{
                // Wenn kein(!!) Heimbewohner
				if(myRezept.getHausbesuch()){
                    jcb[cVOLLHB].setEnabled(false);
                    jcb[cVOLLHB].setSelected(true);
                }else{
                    jcb[cVOLLHB].setEnabled(false);
                    jcb[cVOLLHB].setSelected(false);
                }
            }
        }
        allowShortCut((Component)jcb[cVOLLHB],"hbVollCheck");
        jpan.add(jcb[cVOLLHB],cc.xy(7,13));



        jcb[cTBANGEF] = new JRtaCheckBox("angefordert");
        jcb[cTBANGEF].setOpaque(false);
        jpan.addLabel("Therapiebericht",cc.xy(1, 15));
        jcb[cTBANGEF].addKeyListener(this);
        jpan.add(jcb[cTBANGEF],cc.xy(3, 15));

        jpan.addSeparator("Verordnete Heilmittel", cc.xyw(1,17,7));

        jtf[cANZ1].setName("anzahl1");
        jtf[cANZ1].addFocusListener(this);
        jtf[cANZ1].addKeyListener(this);
        jpan.addLabel("Anzahl / Heilmittel 1",cc.xy(1, 19));
        eingabeVerordn1  = jpan.add(jtf[cANZ1],cc.xy(3, 19));
        jcmb[cLEIST1] = new JRtaComboBox();
        jcmb[cLEIST1].setActionCommand("leistung1");
        jcmb[cLEIST1].addActionListener(this);
        allowShortCut((Component)jcmb[cLEIST1],"leistung1");
        jpan.add(jcmb[cLEIST1],cc.xyw(5, 19,3));

        jpan.addLabel("Anzahl / Heilmittel 2",cc.xy(1, 21));
        jtf[cANZ2].addKeyListener(this);
        jpan.add(jtf[cANZ2],cc.xy(3, 21));
        jcmb[cLEIST2] = new JRtaComboBox();
        jcmb[cLEIST2].setActionCommand("leistung2");
        jcmb[cLEIST2].addActionListener(this);
        allowShortCut((Component)jcmb[cLEIST2],"leistung2");
        jpan.add(jcmb[cLEIST2],cc.xyw(5, 21,3));

        jpan.addLabel("Anzahl / Heilmittel 3",cc.xy(1, 23));
        jtf[cANZ3].addKeyListener(this);
        jpan.add(jtf[cANZ3],cc.xy(3, 23));
        jcmb[cLEIST3] = new JRtaComboBox();
        jcmb[cLEIST3].setActionCommand("leistung3");
        jcmb[cLEIST3].addActionListener(this);
        allowShortCut((Component)jcmb[cLEIST3],"leistung3");
        jpan.add(jcmb[cLEIST3],cc.xyw(5, 23,3));

        jpan.addLabel("Anzahl / Heilmittel 4",cc.xy(1, 25));
        jtf[cANZ4].addKeyListener(this);
        jpan.add(jtf[cANZ4],cc.xy(3, 25));
        jcmb[cLEIST4] = new JRtaComboBox();
        jcmb[cLEIST4].setActionCommand("leistung4");
        jcmb[cLEIST4].setName("leistung4");
        jcmb[cLEIST4].addActionListener(this);
        jpan.add(jcmb[cLEIST4],cc.xyw(5, 25,3));

        jpan.addSeparator("Durchführungsbestimmungen", cc.xyw(1,27,7));

        jtf[cFREQ].addKeyListener(this);
        jpan.addLabel("Behandlungsfrequenz",cc.xy(1, 29));
        eingabeBehFrequ = jpan.add(jtf[cFREQ],cc.xy(3, 29));

        jpan.addLabel("Dauer der Behandl. in Min.",cc.xy(5, 29));
        jtf[cDAUER].addKeyListener(this);
        jpan.add(jtf[cDAUER],cc.xy(7, 29));


        jpan.addLabel("Indikationsschlüssel",cc.xy(1, 31));
        jcmb[cINDI] = new JRtaComboBox();
        jtf[cDAUER].addKeyListener(this);
        jpan.add(jcmb[cINDI],cc.xy(3, 31));

        klassenReady = true;
        fuelleIndis((String)jcmb[cRKLASSE].getSelectedItem());

        jpan.addLabel("Barcode-Format",cc.xy(5, 31));
        //jcmb[cBARCOD] = new JRtaComboBox(new String[] {"Muster 13/18","Muster 14","DIN A6-Format","DIN A4(BGE)","DIN A4 (REHA)"});
        jcmb[cBARCOD] = new JRtaComboBox(SystemConfig.rezBarCodName);
        jcmb[cBARCOD].addKeyListener(this);
        jpan.add(jcmb[cBARCOD],cc.xy(7, 31));

        jpan.addLabel("FarbCode im TK",cc.xy(1, 33));
        jcmb[cFARBCOD] = new JRtaComboBox();
        jcmb[cFARBCOD].addKeyListener(this);
        macheFarbcodes();
        /*
        new SwingWorker<Void,Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                macheFarbcodes();
                return null;
            }
        }.execute();
        */
        jpan.add(jcmb[cFARBCOD],cc.xy(3, 33));

        jpan.addLabel("Angelegt von",cc.xy(5, 33));
        jtf[cANGEL].addKeyListener(this);
        jpan.add(jtf[cANGEL],cc.xy(7, 33));

        jpan.addSeparator("ICD-10 Codes", cc.xyw(1,35,7));
        // hier der ICD-10 Code
        /********/
        jpan.addLabel("1. ICD-10-Code",cc.xy(1,37));
        jtf[cICD10].setName("icd10");
        jtf[cICD10].addKeyListener(this);
        //jtf[cICD10].addFocusListener(this);
        eingabeICD  = jpan.add(jtf[cICD10],cc.xy(3,37));

        jpan.addLabel("2. ICD-10-Code",cc.xy(5, 37));
        jtf[cICD10_2].setName("icd10_2");
        jpan.add(jtf[cICD10_2],cc.xy(7,37));

        jpan.addSeparator("Ärztliche Diagnose laut Rezept", cc.xyw(1,39,7));
        jta = new JTextArea();
        jta.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.5f)));
        jta.setFont(new Font("Courier",Font.PLAIN,11));
        jta.setLineWrap(true);
        jta.setName("notitzen");
        jta.setWrapStyleWord(true);
        jta.setEditable(true);
        jta.setBackground(Color.WHITE);
        jta.setForeground(Color.RED);
        JScrollPane span = JCompTools.getTransparentScrollPane(jta);
        span.setBorder(BorderFactory.createLineBorder(Colors.PiOrange.alpha(0.25f)));
        jpan.add(span,cc.xywh(1, 41,7,2));
        jscr = JCompTools.getTransparentScrollPane(jpan.getPanel());
        jscr.getVerticalScrollBar().setUnitIncrement(15);

        if(this.neu){
			if(myRezept.isEmpty()){
				initRezeptNeu();		// McM:hier myRezept mit Pat-Daten, PG, ... initialisieren
				this.holePreisGruppe(Reha.instance.patpanel.patDaten.get(68).trim());		// setzt jtf[cPREISGR] u. this.preisgruppe
				this.ladePreisliste(jcmb[cRKLASSE].getSelectedItem().toString().trim(), preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]);	// fuellt jcmb[cLEIST1..4] u. jcmb[cBARCOD]
                this.fuelleIndis(jcmb[cRKLASSE].getSelectedItem().toString().trim());
//				ladeZusatzDatenNeu();
			}else {	// myRezept enthaelt Daten
				// Lemmi 20110101: das muß auch als Voraussetzung für doKopiereLetztesRezeptDesPatienten gemacht werden
                try{
//					String[] xartdbeh = new String[] {String.valueOf(vec.get(65)),String.valueOf(vec.get(66)),String.valueOf(vec.get(67)),String.valueOf(vec.get(68))};
					String[] xartdbeh = new String[] {myRezept.getHMkurz(1), myRezept.getHMkurz(2), myRezept.getHMkurz(3), myRezept.getHMkurz(4)};
//					ladeZusatzDatenNeu();
//					doKopiereLetztesRezeptDesPatienten();  // hier drin wird auch "ladeZusatzDatenNeu()" aufgerufen
					initRezeptKopie();
					this.holePreisGruppe(myRezept.getKtraeger());
                    this.ladePreisliste(jcmb[cRKLASSE].getSelectedItem().toString().trim(), preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]);
                    this.fuelleIndis(jcmb[cRKLASSE].getSelectedItem().toString().trim());
                    for (int i = 0; i < 4; i++){
                        if(xartdbeh[i].equals("")){
                            jcmb[cLEIST1+i].setSelectedIndex(0);
                        }else{
                            jcmb[cLEIST1+i].setSelectedVecIndex(1, xartdbeh[i]);
                        }
                    }
					jcmb[cINDI].setSelectedItem(myRezept.getIndiSchluessel());
                }catch(Exception ex){
                    ex.printStackTrace();
                }

            }
        }else{
			this.holePreisGruppe(myRezept.getKtraeger());
            this.ladePreisliste(jcmb[cRKLASSE].getSelectedItem().toString().trim(), preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]);
            this.fuelleIndis(jcmb[cRKLASSE].getSelectedItem().toString().trim());
//			copyVecToForm();
        }
		copyVecToForm();		// McM: hier sollte copyVecToForm() in beiden Faellen stattfinden

        jscr.validate();
        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,"Fehler in der Erstellung des Rezeptfensters\n"+ex.getMessage());
        }

        // Lemmi 20101231: Merken der Originalwerte der eingelesenen Textfelder
        SaveChangeStatus();

        return jscr;
    }

    private void allowShortCut(Component thisComponent, String name) {
        thisComponent.setName(name);
        thisComponent.addKeyListener(this);
        thisComponent.addFocusListener(this);
    }

	private int getselectedRow(){
		return Reha.instance.patpanel.aktRezept.tabaktrez.getSelectedRow();
	}
	/**
	 * RezeptDatum in Tabelle 'aktuelle Rezepte' uebernehmen
	 * @param datum
	 */
	private void setRezDatInTable(String datum){
		int row = getselectedRow(); 
		if(row >= 0){
			Reha.instance.patpanel.aktRezept.tabaktrez.getModel().setValueAt(datum, row, 2);
		}
	}
	/**
	 * 'spaetester Beginn' in Tabelle uebernehmen
	 * @param datum
	 */
	private void setLastDatInTable(String datum){
		int row = getselectedRow(); 
		if(row >= 0){
			Reha.instance.patpanel.aktRezept.tabaktrez.getModel().setValueAt(datum, row, 4);
		}
	}
	private String chkLastBeginDat (String rezDat, String lastDat, String preisGroup, String aktDiszi){
		if(lastDat.equals(".  .")){		// spaetester Beginn nicht angegeben? -> aus Preisgruppe holen
			//Preisgruppe holen
			int pg = Integer.parseInt(preisGroup)-1;
			//Frist zwischen Rezeptdatum und erster Behandlung
			int frist = (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(aktDiszi).get(0)).get(pg);
			//Kalendertage
			if((Boolean) ((Vector<?>)SystemPreislisten.hmFristen.get(aktDiszi).get(1)).get(pg)){
				lastDat = DatFunk.sDatPlusTage(rezDat, frist);					
			}else{ //Werktage
				boolean mitsamstag = (Boolean)((Vector<?>)SystemPreislisten.hmFristen.get(aktDiszi).get(4)).get(pg);
				lastDat = HMRCheck.hmrLetztesDatum(rezDat, (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(aktDiszi).get(0)).get(pg),mitsamstag );
			}
		}
		return lastDat;
	}

    public int leistungTesten(int combo,int veczahl){
        int retwert = 0;
        if(veczahl==-1 || veczahl==0){
            return retwert;
        }
        if(preisvec==null){
            return 0;
        }
        for(int i = 0;i<preisvec.size();i++){
            if( Integer.parseInt((String) ((Vector<?>)preisvec.get(i)).get(preisvec.get(i).size()-1)) == veczahl ){
                return i+1;
            }
        }
        return retwert;
    }
    public RezNeuanlage getInstance(){
        return this;
    }
    public static String macheIcdString(String string){
        String String1 = string.trim().substring(0,1).toUpperCase();
        String String2 = string.trim().substring(1).toUpperCase().replace(" ", "").replace("*", "").replace("!", "").replace("+","").replace("R", "").replace("L","").replace("B","").replace("G","").replace("V","").replace("Z","");;
        return String1+String2;

    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("rezeptklasse") && klassenReady){
            //System.out.println(jcmb[cRKLASSE].getSelectedItem().toString().trim());
            //System.out.println(preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]);
            this.ladePreisliste(jcmb[cRKLASSE].getSelectedItem().toString().trim(), preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]);
            this.fuelleIndis((String)jcmb[cRKLASSE].getSelectedItem());

            return;
        }
        /*********************/
        if(e.getActionCommand().equals("verordnungsart") && klassenReady){
            if(jcmb[cVERORD].getSelectedIndex()==2){
                jcb[cBEGRADR].setEnabled(true);
                testeGenehmigung(jtf[cKASID].getText());
            }else{
                jcb[cBEGRADR].setSelected(false);
                jcb[cBEGRADR].setEnabled(false);
            }
            return;
        }
        /*********************/
        if(e.getActionCommand().equals("speichern") ){
            SwingUtilities.invokeLater(new Runnable(){
                    public  void run()
                    {
                        try{
                            if(! anzahlTest()){
                                return;
                            }
							if(!komplettTest()){
								////System.out.println("Komplett-Test fehlgeschlagen");
								return;
							}
                            if(getInstance().neu){
							if(!neuDateTest()){
								return;
							}
							copyFormToVec1stTime();
							doSpeichernNeu();						// -> erzeugt neue rezNb
							myRezept.setNewRezNb(rezKlasse);		// -> dito
							Reha.instance.patpanel.aktRezept.setzeRezeptNummerNeu(myRezept.getRezNb());
                            }else{
								copyFormToVec();
                                doSpeichernAlt();
                            }
						closeDialog();
						aufraeumen();
						// ?? automat. HMR-Check ??
						myRezept.writeRez2DB();
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }

                    }
            });
            return;
        }
        if(e.getActionCommand().equals("abbrechen") ){
            new SwingWorker<Void,Void>(){
                @Override
                protected Void doInBackground() throws Exception {
                    doAbbrechen();
                    return null;
                }
            }.execute();
            return;
        }
        if(e.getActionCommand().equals("hmrcheck") ){
            new SwingWorker<Void,Void>(){
                @Override
                protected Void doInBackground() throws Exception {
                    try{
                        boolean icd10falsch = false;
                        int welcherIcd = 0;
                        if(jtf[cICD10].getText().trim().length() > 0){
                            String suchenach = macheIcdString(jtf[cICD10].getText());
                            if(SqlInfo.holeEinzelFeld("select id from icd10 where schluessel1 like '"+suchenach+"%' LIMIT 1").equals("")){
                                icd10falsch = true;
                                welcherIcd = 1;
                            }
                            if(jtf[cICD10_2].getText().trim().length() > 0){
                                suchenach = macheIcdString(jtf[cICD10_2].getText());
                                if(SqlInfo.holeEinzelFeld("select id from icd10 where schluessel1 like '"+suchenach+"%' LIMIT 1").equals("")){
                                    icd10falsch = true;
                                    welcherIcd = 2;
                                }
                            }
                        }else{
                            if( SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin).get(preisgruppen[jcmb[cRKLASSE].getSelectedIndex()])==1  ){
                                hmrcheck.setEnabled(true);
                                JOptionPane.showMessageDialog(null, "<html><b><font color='#ff0000'>Kein ICD-10 Code angegeben!</font></b></html>");

                            }
                        }
                        doHmrCheck(icd10falsch,welcherIcd);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                    return null;
                }
            }.execute();
            return;
        }

        if(e.getActionCommand().equals("Hausbesuche") ){
            if(jcb[cHAUSB].isSelected()){
                // Hausbesuch gewählt
                if(Reha.instance.patpanel.patDaten.get(44).equals("T")){
                    //System.out.println("aktuelle Preisgruppe = "+preisgruppe);
                    if(this.preisgruppe!=1 && (jcmb[cRKLASSE].getSelectedIndex() <= 1) ){
                        jcb[cVOLLHB].setEnabled(true);
                    }
                    SwingUtilities.invokeLater(new Runnable(){
                            public  void run()
                            {
                                jcb[cHAUSB].requestFocus();
                            }
                    });
                }else{
                    jcb[cVOLLHB].setEnabled(false);
                    jcb[cVOLLHB].setSelected(true);
                    SwingUtilities.invokeLater(new Runnable(){
                            public  void run()
                            {
                                jcb[cHAUSB].requestFocus();
                            }
                    });
                }
            }else{
                // Haubesuch abgewählt
                jcb[cVOLLHB].setEnabled(false);
                jcb[cVOLLHB].setSelected(false);
                SwingUtilities.invokeLater(new Runnable(){
                        public  void run()
                        {
                            jcb[cHAUSB].requestFocus();
                        }
                });
            }
            return;
        }

        /*********************/
        if(e.getActionCommand().contains("leistung") && initReady){
            int lang = e.getActionCommand().length();
            doRechnen( Integer.valueOf( e.getActionCommand().substring(lang-1 ) ) );
            String test = (String)((JRtaComboBox)e.getSource()).getSelectedItem();
            if(test==null){
                return;
            }
            if(! test.equals("./.")){
                String id = (String)((JRtaComboBox)e.getSource()).getValue();
                Double preis = holePreisDouble(id,preisgruppe);
                if(preis <= 0.0){
                    JOptionPane.showMessageDialog(null,"Diese Position ist für die gewählte Preisgruppe ungültig\nBitte weisen Sie in der Preislisten-Bearbeitung der Position ein Kürzel zu");
                    ((JRtaComboBox)e.getSource()).setSelectedIndex(0);
                }
            }
            return;
        }
    }

    private void testeGenehmigung(final String kassenid){
        new SwingWorker<Void,Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                try{
                    //System.out.println("kassenID = "+kassenid);
                    String test = SqlInfo.holeEinzelFeld("select id from adrgenehmigung where ik = (select ik_kostent from kass_adr where id = '"+kassenid+"') LIMIT 1" );
                    if(!test.isEmpty()){
                        String meldung = "<html><b>Achtung!</b><br><br>Sie haben Verordnung außerhalb des Regelfalles gewählt!<br><br>Die Krankenkasse des Patienten besteht auf eine <br>"+
                                "<b>Genehmigung für Verordnungen außerhalb des Regelfalles</b><br><br></html>";
                        JOptionPane.showMessageDialog(null,meldung);
                    }
                    //System.out.println("Rückgabe von test = "+test);
                }catch(Exception ex){
                    JOptionPane.showMessageDialog(null,"Fehler!!!\n\nVermutlich haben Sie eines der letzten Updates verpaßt.\nFehlt zufällig die Tabelle adrgenehmigung?");
                    ex.printStackTrace();
                }
                return null;
            }
        }.execute();

    }
    // Lemmi 20101231: Harte Index-Zahlen für "jcmb" und "jtf" durch sprechende Konstanten ersetzt !
    // Lemmi Doku: prüft ob die Heilmittel überhaupt und in der korrekten Reihenfolge eingetragen worden sind
    private boolean anzahlTest(){
        int itest;
        int maxanzahl=0, aktanzahl=0;

        for(int i = 0; i < 4; i++) {  // über alle 4 Leistungs- und Anzahl-Positionen rennen
            itest = jcmb[cLEIST1 + i].getSelectedIndex();
            if(itest > 0){
                if(i == 0 ){  // die 1. Position besonders abfragen - diese muß existieren !
                    try{
                        maxanzahl = Integer.parseInt(jtf[cANZ1 + i].getText());
                    }catch(Exception ex){
                        maxanzahl = 0;
                    }
                }else{
                    try{
                        aktanzahl = Integer.parseInt(jtf[cANZ1 + i].getText());
                    }catch(Exception ex){
                        aktanzahl = 0;
                    }
                    if(aktanzahl > maxanzahl){
                        String cmd = "Sie haben mehrere Heilmittel mit unterschiedlicher Anzahl eingegeben.\n"+
                        "Bitte geben Sie die Heilmittel so ein daß das Heilmittel mit der größten Anzahl oben steht\n"+
                        "und dann (bezogen auf die Anzahl) in absteigender Reihgenfolge nach unten";
                        JOptionPane.showMessageDialog(null,cmd);
                        return false;
                    }
                }
            }
        }
        return true;
    }

	/*
	 * Test Rezeptdatum (aus doSpeichernNeu())
	 */
	private boolean neuDateTest(){
		long dattest = DatFunk.TageDifferenz(DatFunk.sHeute(),jtf[cREZDAT].getText().trim() );
		//long min = -364;
		//long max = 364;
		if( (dattest <= -364) || (dattest >= 364) ){
			int frage = JOptionPane.showConfirmDialog(null, "<html><b>Das Rezeptdatum ist etwas kritisch....<br><br><font color='#ff0000'> "+
					"Rezeptdatum = "+jtf[cREZDAT].getText().trim()+"</font></b><br>Das sind ab Heute "+Long.toString(dattest)+" Tage<br><br><br>"+
					"Wollen Sie dieses Rezeptdatum tatsächlich abspeichern?", "Bedenkliches Rezeptdatum",JOptionPane.YES_NO_OPTION);
			if(frage!=JOptionPane.YES_OPTION){
				 SwingUtilities.invokeLater(new Runnable(){
				 	   public  void run()
				 	   {
							jtf[cREZDAT].requestFocus();
				 	   }
				});	   		
				return false;
			}
		}			
		return true;
	}
	
    private void doRechnen(int comb){
        //unbelegt
    }

//	private void mustHmrCheck(){
//		if( SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin).get(preisgruppen[jcmb[cRKLASSE].getSelectedIndex()])==1  ){
//			this.hmrcheck.setEnabled(false);
//		}
//	}
    private void doHmrCheck(boolean icd10falsch, int welcher ){
        if( SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin).get(preisgruppen[jcmb[cRKLASSE].getSelectedIndex()])==0  ){
            this.hmrcheck.setEnabled(true);
            JOptionPane.showMessageDialog(null, "HMR-Check ist bei diesem Kostenträger nicht erforderlich");
            return;
        }
        //System.out.println(SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin).get(preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]));
        int itest = 0; //jcmb[cLEIST1].getSelectedIndex();
        String indi = (String)jcmb[cINDI].getSelectedItem();
        if(indi.equals("") || indi.contains("kein IndiSchl.")){
            JOptionPane.showMessageDialog(null, "<html><b>Kein Indikationsschlüssel angegeben.<br>Die Angaben sind <font color='#ff0000'>nicht</font> gemäß den gültigen Heilmittelrichtlinien!</b></html>");
            return;
        }
        indi = indi.replace(" ", "");
        Vector<Integer> anzahlen = new Vector<Integer>();
        Vector<String> hmpositionen = new Vector<String>();

        // Lemmi 20101231: Harte Index-Zahlen für "jcmb" und "jtf" durch sprechende Konstanten ersetzt !
        for(int i = 0; i < 4; i++ ) {   // Lemmi Doku: Nacheinander alle 4  Leistungen abfragen und Anzahlen addieren
            itest = jcmb[cLEIST1 + i].getSelectedIndex();
            if(itest > 0){
                anzahlen.add( Integer.parseInt(jtf[cANZ1 + i].getText()) );
                hmpositionen.add(preisvec.get(itest-1).get(2));
            }
        }

        if(jtf[cREZDAT].getText().trim().equals(".  .")){
            JOptionPane.showMessageDialog(null, "Rezeptdatum nicht korrekt angegeben HMR-Check nicht möglich");
            return;
        }
        if(icd10falsch){
            int frage = JOptionPane.showConfirmDialog(null, "<html><b>Der eingetragene "+Integer.toString(welcher)+". ICD-10-Code ist falsch: <font color='#ff0000'>"+
                    (welcher==1 ? jtf[cICD10].getText().trim() : jtf[cICD10_2].getText().trim())+"</font></b><br>"+
                    "HMR-Check nicht möglich!<br><br>"+
                    "Wollen Sie jetzt das ICD-10-Tool starten?<br><br></html>", "falscher ICD-10",JOptionPane.YES_NO_OPTION);
            if(frage==JOptionPane.YES_OPTION){
                SwingUtilities.invokeLater(new ICDrahmen(connection));
            }
            if(welcher==1){
                jtf[cICD10].setText("");
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        jtf[cICD10].requestFocus();
                    }
                });
            }else if(welcher==2){
                jtf[cICD10_2].setText("");
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        jtf[cICD10_2].requestFocus();
                    }
                });

            }
            return;
        }
        if(hmpositionen.size() > 0){
//            String[] idiszi = {"Physio-Rezept","Massage/Lymphdrainage-Rezept",
//                    "Ergotherapie-Rezept","Logopädie-Rezept","REHA-Verordnung","Podologie-Rezept"};  // Lemmi Fehler: Wa ist die Podologie ? Warum müssen diese "Standard-Strings immer neu aufgeführt werden? (genau EINAML an zentraler Stelle reicht! dt. für die 2-Buchstaben-Kürzel !
            String letztbeginn =  jtf[cBEGINDAT].getText().trim();
            if(letztbeginn.equals(".  .")){
                //Preisgruppe holen
                int pg = Integer.parseInt(jtf[cPREISGR].getText())-1;
                //Frist zwischen Rezeptdatum und erster Behandlung
                int frist = (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(0)).get(pg);
                //Kalendertage
                if((Boolean) ((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(1)).get(pg)){
                    letztbeginn = DatFunk.sDatPlusTage(jtf[cREZDAT].getText().trim(), frist);
                }else{ //Werktage
                    boolean mitsamstag = (Boolean)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(4)).get(pg);
                    letztbeginn = HMRCheck.hmrLetztesDatum(jtf[cREZDAT].getText().trim(), (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(0)).get(pg),mitsamstag );
                }
            }
            boolean checkok = new HMRCheck(
                    indi,
//                    Arrays.asList(idiszi).indexOf((String)jcmb[cRKLASSE].getSelectedItem().toString()),
                    diszis.getIndex(diszis.getCurrDisziFromActRK()),
                    anzahlen,
                    hmpositionen,
                    preisgruppen[jcmb[cRKLASSE].getSelectedIndex()],
                    preisvec,
                    jcmb[cVERORD].getSelectedIndex(),
//					(this.neu ? "" : this.vec.get(1).trim()),
					(this.neu ? "" : myRezept.getRezNb()),
                    jtf[cREZDAT].getText().trim(),
                    letztbeginn
                    ).check();
            if(checkok){
                JOptionPane.showMessageDialog(null, "<html><b>Das Rezept <font color='#ff0000'>entspricht</font> den geltenden Heilmittelrichtlinien</b></html>");
            }
        }else{
            JOptionPane.showMessageDialog(null, "Keine Behandlungspositionen angegeben, HMR-Check nicht möglich!!!");
        }

    }


    private boolean komplettTest(){
        if(jtf[cREZDAT].getText().trim().equals(".  .")){
            JOptionPane.showMessageDialog(null, "Ohne ein gültiges 'Rezeptdatum' kann ein Rezept nicht abgespeichert werden.");
            SwingUtilities.invokeLater(new Runnable(){
                    public  void run()
                    {
                        jtf[cREZDAT].requestFocus();
                    }
            });
            return false;
        }

        if(jtf[cKTRAEG].getText().trim().equals("")){
            JOptionPane.showMessageDialog(null, "Ohne die Angabe 'Kostenträger' kann ein Rezept nicht abgespeichert werden.");
            SwingUtilities.invokeLater(new Runnable(){
                    public  void run()
                    {
                        jtf[cKTRAEG].requestFocus();
                    }
            });
            return false;
        }
        if(jtf[cARZT].getText().trim().equals("")){
            JOptionPane.showMessageDialog(null, "Ohne die Angabe 'verordn. Arzt' kann ein Rezept nicht abgespeichert werden.");
            SwingUtilities.invokeLater(new Runnable(){
                    public  void run()
                    {
                        jtf[cARZT].requestFocus();
                    }
            });
            return false;
        }
        if(jtf[cDAUER].getText().trim().equals("")){
            JOptionPane.showMessageDialog(null, "Ohne die Angabe 'Behandlungsdauer' kann ein Rezept nicht abgespeichert werden.");
            SwingUtilities.invokeLater(new Runnable(){
                    public  void run()
                    {
                        jtf[cDAUER].requestFocus();
                    }
            });
            return false;
        }
        if(jtf[cANGEL].getText().trim().equals("")){
            JOptionPane.showMessageDialog(null, "Ohne die Angabe 'Angelegt von' kann ein Rezept nicht abgespeichert werden.");
            SwingUtilities.invokeLater(new Runnable(){
                    public  void run()
                    {
                        jtf[cANGEL].requestFocus();
                    }
            });
            return false;
        }
        if( SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin).get(preisgruppen[jcmb[cRKLASSE].getSelectedIndex()])==1  ){
            if(jtf[cFREQ].getText().trim().equals("")){
                JOptionPane.showMessageDialog(null, "Ohne Angabe der 'Behandlungsfrequenz' kann ein GKV-Rezept nicht abgespeichert werden.");
                SwingUtilities.invokeLater(new Runnable(){
                        public  void run()
                        {
                            jtf[cFREQ].requestFocus();
                        }
                });
                return false;
            }
        }
        return true;
    }
    private void ladePreisliste(String item,int preisgruppe){
        try{
            String[] artdbeh=null;
            if(!this.neu && jcmb[cLEIST1].getItemCount()>0){
                artdbeh = new String[]{
                        String.valueOf(jcmb[cLEIST1].getValueAt(1)),String.valueOf(jcmb[cLEIST2].getValueAt(1)),
                        String.valueOf(jcmb[cLEIST3].getValueAt(1)),String.valueOf(jcmb[cLEIST4].getValueAt(1))};
            }
            jcmb[cLEIST1].removeAllItems();
            jcmb[cLEIST2].removeAllItems();
            jcmb[cLEIST3].removeAllItems();
            jcmb[cLEIST4].removeAllItems();

//		if(item.toLowerCase().contains("physio") ){		// item: Physio-Rezept
//			aktuelleDisziplin = "Physio";
//			rezKlasse = "kg";
//		}else if(item.toLowerCase().contains("massage")){
//			aktuelleDisziplin = "Massage";
//			rezKlasse = "ma";
//		}else if(item.toLowerCase().contains("ergo")){
//			aktuelleDisziplin = "Ergo";
//			rezKlasse = "er";
//		}else if(item.toLowerCase().contains("logo")){
//			aktuelleDisziplin = "Logo";
//			rezKlasse = "lo";
//		}else if(item.toLowerCase().contains("rehasport")){
//			aktuelleDisziplin = "Rsport";
//			rezKlasse = "rs";
//		}else if(item.toLowerCase().contains("funktions")){
//			aktuelleDisziplin = "Ftrain";
//			rezKlasse = "ft";
//		}else if(item.toLowerCase().contains("reha") && (!item.toLowerCase().contains("rehasport")) ){
//			aktuelleDisziplin = "Reha";
//			rezKlasse = "rh";
//		}else if(item.toLowerCase().contains("podo")){
//			aktuelleDisziplin = "Podo";
//			rezKlasse = "po";
//		}
		aktuelleDisziplin = diszis.getDiszi(item);
		rezKlasse = diszis.getRezClass(item);	//ist jetzt Uppercase!

            preisvec = SystemPreislisten.hmPreise.get(aktuelleDisziplin).get(preisgruppe);

            //System.out.println("Aktuelle Disziplin = "+aktuelleDisziplin);
            //System.out.println("Preisvektor  = "+preisvec);

            if(artdbeh!=null){
                ladePreise(artdbeh);
            }else{
                ladePreise(null);
            }
            if(this.neu && SystemPreislisten.hmHMRAbrechnung.get(aktuelleDisziplin).get(preisgruppe) == 1){
                if(aktuelleDisziplin.equals("Physio") || aktuelleDisziplin.equals("Massage") || aktuelleDisziplin.equals("Ergo")){
                    jcmb[cBARCOD].setSelectedItem("Muster 13/18");
                }else if(aktuelleDisziplin.equals("Logo")){
                    jcmb[cBARCOD].setSelectedItem("Muster 14");
                }else if(aktuelleDisziplin.equals("Reha")){
                    jcmb[cBARCOD].setSelectedItem("DIN A4 (REHA)");
                }else{
                    jcmb[cBARCOD].setSelectedItem("Muster 13/18");
                }
            }else if(this.neu && aktuelleDisziplin.equals("Reha")){
                jcmb[cBARCOD].setSelectedItem("DIN A4 (REHA)");
            }else{
                if(this.neu){
                    jcmb[cBARCOD].setSelectedItem("Muster 13/18");
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }

    // Lemmi-Doku: Holt die passenden Inikationsschlüssel gemäß aktiver Disziplin
    private void fuelleIndis(String item){
        try{
            if(jcmb[cINDI].getItemCount() > 0){
                jcmb[cINDI].removeAllItems();
            }
            if(item.toLowerCase().contains("reha") && (!item.toLowerCase().startsWith("rehasport")) ){
                return;
            }
            int anz = 0;
            String[] indis = null;
            if(    item.toLowerCase().contains("physio")
                || item.toLowerCase().contains("massage")
                || item.toLowerCase().contains("rehasport")
                || item.toLowerCase().contains("funktions")){
                anz = Reha.instance.patpanel.aktRezept.indphysio.length;
                indis = Reha.instance.patpanel.aktRezept.indphysio;
            }else if(item.toLowerCase().contains("ergo")){
                anz = Reha.instance.patpanel.aktRezept.indergo.length;
                indis = Reha.instance.patpanel.aktRezept.indergo;
            }else if(item.toLowerCase().contains("logo")){
                anz = Reha.instance.patpanel.aktRezept.indlogo.length;
                indis = Reha.instance.patpanel.aktRezept.indlogo;
            }else if(item.toLowerCase().contains("podo")){
                anz = Reha.instance.patpanel.aktRezept.indpodo.length;
                indis = Reha.instance.patpanel.aktRezept.indpodo;
            }
            for(int i = 0; i < anz; i++){
                jcmb[cINDI].addItem(indis[i]);
            }

        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,"Fehler bei füller Inikat.schlüssel\n"+ex.getMessage());

        }

        return;
    }

    public void ladePreise(String[] artdbeh){
        try{
            if(preisvec.size()<=0){
                JOptionPane.showMessageDialog(null,"In der erforderlichen Preisliste sind noch keine Preise vorhanden!\nRezept kann nicht angelegt werden");
                return;
            }
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"In der erforderlichen Preisliste sind noch keine Preise vorhanden!\nRezept kann nicht angelegt werden");
            return;
        }
        jcmb[cLEIST1].setDataVectorWithStartElement(preisvec,0,9,"./.");
        jcmb[cLEIST2].setDataVectorWithStartElement(preisvec,0,9,"./.");
        jcmb[cLEIST3].setDataVectorWithStartElement(preisvec,0,9,"./.");
        jcmb[cLEIST4].setDataVectorWithStartElement(preisvec,0,9,"./.");
        if(artdbeh != null){
            for (int i = 0; i < 4; i++){
                if(artdbeh[i].equals("")){
                    jcmb[cLEIST1+i].setSelectedIndex(0);
                }else{
                    jcmb[cLEIST1+i].setSelectedVecIndex(1, artdbeh[i]);
                }
            }
        }
        return;
    }


//    static int x = 1;

    @Override
    public void keyPressed(KeyEvent arg0) {
        if(arg0.getKeyChar()=='?' && ((JComponent)arg0.getSource()).getName().equals("arzt")){
            String[] suchkrit = new String[] {jtf[cARZT].getText().replace("?", ""),jtf[cARZTID].getText()};
            jtf[cARZT].setText(String.valueOf(suchkrit[0]));
            arztAuswahl(suchkrit);
        }
        if(arg0.getKeyChar()=='?' && ((JComponent)arg0.getSource()).getName().equals("ktraeger")){
            String[] suchkrit = new String[] {jtf[cKTRAEG].getText().replace("?", ""),jtf[cKASID].getText()};
            jtf[cKTRAEG].setText(suchkrit[0]);
            kassenAuswahl(suchkrit);
        }
        if(arg0.getKeyCode()==KeyEvent.VK_ESCAPE){
            doAbbrechen();
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        if(arg0.getKeyCode() == KeyEvent.VK_CONTROL){
            //System.out.println("CTRL released");
            ctrlIsPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    @Override
    public void focusGained(FocusEvent arg0) {
    }

    @Override
    public void focusLost(FocusEvent arg0) {
        if( ((JComponent)arg0.getSource()).getName() != null){
            String componentName = ((JComponent)arg0.getSource()).getName();
            boolean jumpForward = arg0.toString().contains("cause=TRAVERSAL_FORWARD");
            //String evt = arg0.toString();
            //System.out.println("Focus lost: "+ componentName + " (" + evt + ")");
            //System.out.println("Focus lost: "+ componentName);
            if( componentName.equals("RezeptClass")  || componentName.equals("ktraeger")){
                if (ctrlIsPressed && jumpForward){
                    // jtf[cREZDAT].requestFocus();
                    eingabeRezDate.requestFocus();        // zur Eingabe Rezeptdatum springen
                }
                return;
            }
            if( componentName.equals("rez_datum") || componentName.equals("lastdate")){
                if (ctrlIsPressed && jumpForward){
                    eingabeVerordnArt.requestFocus();    // zur Auswahl 'Art d. Verordn.' springen
                }
                return;
            }
            if( componentName.equals("selArtDerVerordn") ||
                    componentName.equals("adrCheck") ||
                    componentName.equals("hbCheck") ||
                    componentName.equals("hbVollCheck")
                    ){
                if (ctrlIsPressed && jumpForward){ eingabeVerordn1.requestFocus(); }    // zur Eingabe der ersten Verordnung springen
                return;
            }
            if( componentName.equals("anzahl1") && neu ){
                String text = jtf[cANZ1].getText();
                jtf[cANZ2].setText(text);
                jtf[cANZ3].setText(text);
                jtf[cANZ4].setText(text);
                return;
            }
            if( componentName.contains("leistung") && jumpForward){
                // ComboBox mit [TAB] verlassen ...
                String test = (String)((JRtaComboBox)arg0.getSource()).getSelectedItem();
                if(test.equals("./.")){        // ... + kein Heilmittel ausgewählt -> zur Behandlungsfrequenz springen
                    eingabeBehFrequ.requestFocus();
                } else {
                    if (ctrlIsPressed){        // schöner/schneller: [STRG][TAB] bzw. [STRG][ENTER] springt zur Behandlungsfrequenz
                        eingabeBehFrequ.requestFocus();
                    }
                }
                return;
            }
            if( componentName.equals("Indikationsschluessel") && jumpForward){
                if (ctrlIsPressed){ eingabeICD.requestFocus(); }    // zur Eingabe ICD10-Code springen
                return;
            }
        }
    }

    private void arztAuswahl(String[] suchenach){
        jtf[cREZDAT].requestFocus();
        JRtaTextField tfArztNum = new JRtaTextField("",false);
        // einbauen A-Name +" - " +LANR;
        ArztAuswahl awahl = new ArztAuswahl(null,"ArztAuswahl",suchenach,new JRtaTextField[] {jtf[cARZT],new JRtaTextField("",false),jtf[cARZTID]},String.valueOf(jtf[cARZT].getText().trim()));
        awahl.setModal(true);
        awahl.setLocationRelativeTo(this);
        awahl.setVisible(true);
        SwingUtilities.invokeLater(new Runnable(){
                public  void run(){
                    jtf[cREZDAT].requestFocus();
                }
        });
        try{
            String aneu = "";
            if(! Reha.instance.patpanel.patDaten.get(63).contains( ("@"+(aneu = jtf[cARZTID].getText().trim())+"@\n")) ){
                String aliste = Reha.instance.patpanel.patDaten.get(63)+ "@"+aneu+"@\n";
                Reha.instance.patpanel.patDaten.set(63,aliste+ "@"+aneu+"@\n");
                Reha.instance.patpanel.getLogic().arztListeSpeichernString(aliste,false,Reha.instance.patpanel.aktPatID);
                SwingUtilities.invokeLater(new Runnable(){
                        public  void run(){
                            jtf[cREZDAT].requestFocus();
                        }
                });

                /*
                String msg = "Dieser Arzt ist bislang nicht in der Arztliste dieses Patienten.\n"+
                "Soll dieser Arzt der Ärzteliste des Patienten zugeordnet werden?";
                int frage = JOptionPane.showConfirmDialog(null,msg,"Wichtige Benutzeranfrage",JOptionPane.YES_NO_OPTION);
                if(frage == JOptionPane.YES_OPTION){
					String aliste = Reha.instance.patpanel.patDaten.get(63)+ "@"+aneu+"@\n";
					Reha.instance.patpanel.patDaten.set(63,aliste+ "@"+aneu+"@\n");
					Reha.instance.patpanel.getLogic().arztListeSpeichernString(aliste,false,Reha.instance.patpanel.aktPatID);
                    SwingUtilities.invokeLater(new Runnable(){
                            public  void run(){
                                jtf[REZDAT].requestFocus();
                            }
                    });
                }else{
                    SwingUtilities.invokeLater(new Runnable(){
                            public  void run(){
                                jtf[REZDAT].requestFocus();
                            }
                    });
                }
                */
            }
        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fehler beim Speichern der Arztliste!\n"+
                    "Bitte notieren Sie Patient, Rezeptnummer und den Arzt den Sie der\n"+
                    "Arztliste hinzufügen wollten und informieren Sie umgehend den Administrator.\n\nDanke");
        }
        awahl.dispose();
        awahl = null;

    }
    private void kassenAuswahl(String[] suchenach){
        jtf[cARZT].requestFocus();
        KassenAuswahl kwahl = new KassenAuswahl(null,"KassenAuswahl",suchenach,new JRtaTextField[] {jtf[cKTRAEG],jtf[cPATID],jtf[cKASID]},jtf[cKTRAEG].getText().trim());
        kwahl.setModal(true);
        kwahl.setLocationRelativeTo(this);
        kwahl.setVisible(true);
        SwingUtilities.invokeLater(new Runnable(){
                public  void run(){
                    if(jtf[cKASID].getText().equals("")){
                        String meldung = "Achtung - kann Preisgruppe nicht ermitteln!\n"+
                        "Das bedeutet diese Rezept kann später nicht abgerechnet werden!\n\n"+
                        "Und bedenken Sie bitte Ihr Kürzel wird dauerhaft diesem Rezept zugeordnet....";
                        JOptionPane.showMessageDialog(null,meldung);
                    }else{
                            holePreisGruppe(jtf[cKASID].getText().trim());
                        ladePreisliste(jcmb[cRKLASSE].getSelectedItem().toString().trim(), preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]);
                        jtf[cARZT].requestFocus();
                    }
                }
        });
        kwahl.dispose();
        kwahl = null;
    }

/*	private void holePreisGruppeMitWorker(String id){
        final String xid = id;
        new SwingWorker<Void,Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                try{

                    Vector<Vector<String>> vec = null;
                    if(SystemConfig.mitRs){
                        vec = SqlInfo.holeFelder("select preisgruppe,pgkg,pgma,pger,pglo,pgrh,pgpo,pgrs,pgft from kass_adr where id='"+xid+"' LIMIT 1");
                    }else{
                        vec = SqlInfo.holeFelder("select preisgruppe,pgkg,pgma,pger,pglo,pgrh,pgpo from kass_adr where id='"+xid+"' LIMIT 1");
                    }
                //System.out.println(vec);
                if(vec.size()>0){
                    for(int i = 1; i < vec.get(0).size();i++){
                        preisgruppen[i-1] = Integer.parseInt(vec.get(0).get(i))-1;
                    }
                    preisgruppe = Integer.parseInt((String)vec.get(0).get(0))-1;
                    jtf[cPREISGR].setText((String)vec.get(0).get(0));
                    ladePreisliste(jcmb[cRKLASSE].getSelectedItem().toString().trim(), preisgruppen[jcmb[cRKLASSE].getSelectedIndex()]);
                    fuelleIndis(jcmb[cRKLASSE].getSelectedItem().toString().trim());
                    ladeZusatzDatenNeu();
                }else{
                    JOptionPane.showMessageDialog(null,"Achtung - kann Preisgruppe nicht ermitteln - Rezept kann später nicht abgerechnet werden!");
                }
                }catch(Exception ex){
                    ex.printStackTrace();
                }
                return null;
            }

        }.execute();
	} */
    private void holePreisGruppe(String id){
        try{
            Vector<Vector<String>> vec = null;
            if(SystemConfig.mitRs){
                vec = SqlInfo.holeFelder("select preisgruppe,pgkg,pgma,pger,pglo,pgrh,pgpo,pgrs,pgft from kass_adr where id='"+id+"' LIMIT 1");
            }else{
                vec = SqlInfo.holeFelder("select preisgruppe,pgkg,pgma,pger,pglo,pgrh,pgpo from kass_adr where id='"+id+"' LIMIT 1");
            }
        //Vector<Vector<String>> vec = SqlInfo.holeFelder("select preisgruppe,pgkg,pgma,pger,pglo,pgrh,pgpo from kass_adr where id='"+id+"' LIMIT 1");
        //System.out.println(vec);
        if(vec.size()>0){
            for(int i = 1; i < vec.get(0).size();i++){
                preisgruppen[i-1] = Integer.parseInt(vec.get(0).get(i))-1;
            }
            preisgruppe = Integer.parseInt((String)vec.get(0).get(0))-1;
            jtf[cPREISGR].setText((String)vec.get(0).get(0));
        }else{
            JOptionPane.showMessageDialog(null,"Achtung - kann Preisgruppe nicht ermitteln - Rezept kann später nicht abgerechnet werden!");
        }
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null,"Achtung - kann Preisgruppe nicht ermitteln - Rezept kann später nicht abgerechnet werden!\n"+
                    "Untersuchen Sie die Krankenkasse im Kassenstamm un weisen Sie dieser Kasse die entsprechend Preisgruppe zu");
        }
    }

    /***********
    *
    *
    */
    // Lemmi Doku: holt Daten aus dem aktuellen Patienten und trägt sie im Rezept ein
	private void ladeZusatzDatenNeu(){		// McM: obsolet nach Verwendung Rezept-Klasse (+ initRezept*())
        //String tests = "";
		if(myRezept.isEmpty()){
            jtf[cKTRAEG].setText(Reha.instance.patpanel.patDaten.get(13));
            jtf[cKASID].setText(Reha.instance.patpanel.patDaten.get(68)); //kassenid
		}else{		// uebernimmt Kostentraeger aus Kopiervorlage. Waere generelle Uebernahme aus Pat.-Daten nicht sinnvoller (Kassenwechsel)?
//			jtf[cKTRAEG].setText(vec.get(36));
			jtf[cKTRAEG].setText(myRezept.getKtrName());				// McM: steht da schon drin (da in vec enthalten)
//			jtf[cKASID].setText(vec.get(37)); //kassenid						
			jtf[cKASID].setText(myRezept.getKtraeger()); //kassenid		// McM: dito			
        }

        if(jtf[cKASID].getText().trim().equals("")){
			JOptionPane.showMessageDialog(null,"Achtung - kann Preisgruppe nicht ermitteln - Rezept kann später nicht abgerechnet werden!");		// McM: eher ein Fall für check/speichern!
        }
        jtf[cARZT].setText(Reha.instance.patpanel.patDaten.get(25)+ " - "+Reha.instance.patpanel.patDaten.get(26));
        // einbauen A-Name +" - " +LANR;
		jtf[cARZTID].setText(Reha.instance.patpanel.patDaten.get(67)); //arztid		// McM: wozu ändern? Es ist 'ne Rezeptkopie 							
		//tests = Reha.instance.patpanel.patDaten.get(31);		// bef_dat = Datum der Befreiung
        jtf[cHEIMBEW].setText(Reha.instance.patpanel.patDaten.get(44)); //heimbewohn
        jtf[cBEFREIT].setText(Reha.instance.patpanel.patDaten.get(30)); //befreit
        jtf[cANZKM].setText(Reha.instance.patpanel.patDaten.get(48)); //kilometer
        jtf[cPATID].setText(Reha.instance.patpanel.patDaten.get(66)); //id von Patient
        jtf[cPATINT].setText(Reha.instance.patpanel.patDaten.get(29)); //pat_intern von Patient
    }

	/**
    *
	 * initialisiert ein Rezept mit Daten, die immer gesetzt werden müssen
	 */
	private void initRezeptAll(){
		myRezept.setKtrName(Reha.instance.patpanel.patDaten.get(13));		// Kasse koennte sich seit Kopiervorlage geaendert haben
		myRezept.setKtraeger(Reha.instance.patpanel.patDaten.get(68));
		
		if(myRezept.getKtraeger().equals("")){ 								// eher ein Fall für check/speichern!
			JOptionPane.showMessageDialog(null,"Achtung - kann Preisgruppe nicht ermitteln - Rezept kann später nicht abgerechnet werden!");
		}

		if(SystemConfig.AngelegtVonUser){
			myRezept.setAngelegtVon(Reha.aktUser);
		}else{
			myRezept.setAngelegtVon("");
		}
		myRezept.setAngelegtDatum(DatFunk.sDatInSQL(DatFunk.sHeute()));

		myRezept.setLastEdit(Reha.aktUser);
		myRezept.setLastEdDate(DatFunk.sDatInSQL(DatFunk.sHeute()));

		myRezept.setHeimbew(Reha.instance.patpanel.patDaten.get(44));		// koennte sich geaendert haben
		myRezept.setBefreit(Reha.instance.patpanel.patDaten.get(30));		// dito
		myRezept.setGebuehrBezahlt(false);									// kann noch nicht bezahlt sein (Rezeptgebühr)
		myRezept.setGebuehrBetrag("0.00");
	}

	/**
	 * 
	 * initialisiert ein leeres Rezept mit Daten aus dem aktuellen Patienten
	 */
	private void initRezeptNeu(){
		myRezept.createEmptyVec();
		initRezeptAll();

		myRezept.setArzt(Reha.instance.patpanel.patDaten.get(25)+ " - "+Reha.instance.patpanel.patDaten.get(26));		// Hausarzt als default
		myRezept.setArztId(Reha.instance.patpanel.patDaten.get(67));
		myRezept.setKm(Reha.instance.patpanel.patDaten.get(48));
		myRezept.setPatIdS(Reha.instance.patpanel.patDaten.get(66));
		myRezept.setPatIntern(Reha.instance.patpanel.patDaten.get(29));
		// Barcode
	}
	
	/**
	 * 
	 * initialisiert ein kopiertes Rezept
	 * 	- aktualisiert Daten aus dem aktuellen Patienten, 
	 * 	- löscht Daten, die nur für die Vorlage gelten (Behandlungen, Preise, Zuzahlung, ...)
	 */
	private void initRezeptKopie(){
		initRezeptAll();
		
//		myRezept.setArzt(Reha.instance.patpanel.patDaten.get(25)+ " - "+Reha.instance.patpanel.patDaten.get(26));		// 'ne Rezeptkopie hat gute Chancen, vom gleichen Arzt zu stammen
//		myRezept.setArztId(Reha.instance.patpanel.patDaten.get(67));

		// war Lemmis 'Löschen der auf jeden Fall "falsch weil alt" Komponenten'
		myRezept.setRezNb("");			//	vec.set(cVAR_REZNR, "");
		myRezept.setRezeptDatum("");	//	vec.set(cVAR_REZDATUM, "");
		myRezept.setTermine("");		//	vec.set(cVAR_TERMINE, "");
		myRezept.setZzStat("");			//	vec.set(cVAR_ZZSTAT, "");
		myRezept.setLastDate("");		//	vec.set(cVAR_LASTDAT, "");
	}
	
	/**
    *
	 * lädt die Daten aus der Rezept-Instanz myRezept in die Dialog-Felder des Rezepts
	 * und setzt auch die ComboBoxen und CheckBoxen
    */
	private void copyVecToForm(){
		String test = StringTools.NullTest(myRezept.getKtrName());
        jtf[cKTRAEG].setText(test); //kasse
		test = StringTools.NullTest(myRezept.getKtraeger());
        jtf[cKASID].setText(test);  //kid
		test = StringTools.NullTest(myRezept.getArzt());
        jtf[cARZT].setText(test); //arzt
		test = StringTools.NullTest(myRezept.getArztId());
        jtf[cARZTID].setText(test); //arztid
		test = StringTools.NullTest(myRezept.getRezeptDatum());
        if(!test.equals("")){
            jtf[cREZDAT].setText(DatFunk.sDatInDeutsch(test));
        }
		test = StringTools.NullTest(myRezept.getLastDate());
        if(!test.equals("")){
            jtf[cBEGINDAT].setText(DatFunk.sDatInDeutsch(test));
        }
//		int itest = StringTools.ZahlTest(this.vec.get(27));
		int itest = myRezept.getRezArt();
        if(itest >=0){
            jcmb[cVERORD].setSelectedIndex(itest);
        }
//		test = StringTools.NullTest(this.vec.get(42));
		jcb[cBEGRADR].setSelected(myRezept.getBegrAdR());
//		test = StringTools.NullTest(this.vec.get(43));
		jcb[cHAUSB].setSelected(myRezept.getHausbesuch());

//		test = StringTools.NullTest(this.vec.get(61));
		jcb[cVOLLHB].setSelected(myRezept.getHbVoll());
		
//		test = StringTools.NullTest(this.vec.get(55));
		jcb[cTBANGEF].setSelected(myRezept.getArztbericht());
//		test = StringTools.NullTest(this.vec.get(3)); 
		jtf[cANZ1].setText(myRezept.getAnzBehS(1));
//		test = StringTools.NullTest(this.vec.get(4));
		jtf[cANZ2].setText(myRezept.getAnzBehS(2));
//		test = StringTools.NullTest(this.vec.get(5));
		jtf[cANZ3].setText(myRezept.getAnzBehS(3));
//		test = StringTools.NullTest(this.vec.get(6));
		jtf[cANZ4].setText(myRezept.getAnzBehS(4));
		
//		itest = StringTools.ZahlTest(this.vec.get(8));
		itest = StringTools.ZahlTest(myRezept.getArtDBehandl(1));
        jcmb[cLEIST1].setSelectedIndex(leistungTesten(0,itest));
		itest = StringTools.ZahlTest(myRezept.getArtDBehandl(2));
        jcmb[cLEIST2].setSelectedIndex(leistungTesten(1,itest));
		itest = StringTools.ZahlTest(myRezept.getArtDBehandl(3));
        jcmb[cLEIST3].setSelectedIndex(leistungTesten(2,itest));
		itest = StringTools.ZahlTest(myRezept.getArtDBehandl(4));
        jcmb[cLEIST4].setSelectedIndex(leistungTesten(3,itest));

		test = StringTools.NullTest(myRezept.getFrequenz());
        jtf[cFREQ].setText(test);
		test = StringTools.NullTest(myRezept.getDauer());
        jtf[cDAUER].setText(test);

		test = StringTools.NullTest(myRezept.getIndiSchluessel());
        jcmb[cINDI].setSelectedItem(test);

//		itest = StringTools.ZahlTest(this.vec.get(46));
		itest = myRezept.getBarcodeform();
		if (itest >= 0){
        jcmb[cBARCOD].setSelectedIndex(itest);
		}else{
			myRezept.setBarcodeform(jcmb[cBARCOD].getSelectedIndex());		// default wird in ladePreisliste() gesetzt				
		}

		test = StringTools.NullTest(myRezept.getAngelegtVon());
        jtf[cANGEL].setText(test);
        if(!test.trim().equals("")){
            jtf[cANGEL].setEnabled(false);
        }
		jta.setText( StringTools.NullTest(myRezept.getDiagn()) );
        if(!jtf[cKASID].getText().equals("")){
            holePreisGruppe(jtf[cKASID].getText().trim());
        }else{
            JOptionPane.showMessageDialog(null, "Ermittlung der Preisgruppen erforderlich");
        }

        jtf[cHEIMBEW].setText(Reha.instance.patpanel.patDaten.get(44)); //heimbewohn
        jtf[cBEFREIT].setText(Reha.instance.patpanel.patDaten.get(30)); //befreit
        jtf[cANZKM].setText(Reha.instance.patpanel.patDaten.get(48)); //kilometer
//		jtf[cPATID].setText(this.vec.get(38)); //id von Patient
		jtf[cPATID].setText(myRezept.getPatIdS());
		jtf[cPATINT].setText(myRezept.getPatIntern());

        //ICD-10
		jtf[cICD10].setText(myRezept.getICD10());
		jtf[cICD10_2].setText(myRezept.getICD10_2());

		itest = myRezept.getFarbCode();
        if(itest >= 0){
            jcmb[cFARBCOD].setSelectedItem( (String)SystemConfig.vSysColsBedeut.get(itest) );
        }

    }

	/***********
	 * 
	 * lädt die Daten aus den Dialog-Feldern des Rezepts erstmalig in die Rezept-Instanz (ex doSpeichernNeu)
	 */
	private void copyFormToVec1stTime(){
		myRezept.setAnzHB(jtf[cANZ1].getText());
		copyFormToVec();
	}
	
	/***********
	 * 
	 * lädt die Daten aus den Dialog-Feldern des Rezepts in die Rezept-Instanz (ex doSpeichernAlt)
	 */
	private void copyFormToVec(){
		try{
			if(!komplettTest()){
				return;
			}
			setCursor(Cursors.wartenCursor);
			String stest = "";
			int itest = -1;
			StringBuffer sbuf = new StringBuffer();		// kann am Ende weg (speichern uebernimmt myRezept.writeRez2DB()) 
			
//			sbuf.append("update verordn set ktraeger='"+jtf[cKTRAEG].getText()+"', ");
			myRezept.setKtrName(jtf[cKTRAEG].getText());
//			sbuf.append("kid='"+jtf[cKASID].getText()+"', ");
			myRezept.setKtraeger(jtf[cKASID].getText());
//			sbuf.append("arzt='"+jtf[cARZT].getText()+"', ");
			myRezept.setArzt(jtf[cARZT].getText());
//			sbuf.append("arztid='"+jtf[cARZTID].getText()+"', ");
			myRezept.setArztId(jtf[cARZTID].getText());

			stest = jtf[cREZDAT].getText().trim();
			if(stest.equals(".  .")){
				stest = DatFunk.sHeute();
			}
			boolean neuerpreis = RezTools.neuePreisNachRezeptdatumOderStichtag(aktuelleDisziplin, preisgruppe, String.valueOf(stest),false,Reha.instance.patpanel.vecaktrez);
//			sbuf.append("rez_datum='"+DatFunk.sDatInSQL(stest)+"', ");
			myRezept.setRezeptDatum(DatFunk.sDatInSQL(stest));
//			int row = Reha.instance.patpanel.aktRezept.tabaktrez.getSelectedRow();
//			if(row >= 0){
//				Reha.instance.patpanel.aktRezept.tabaktrez.getModel().setValueAt(stest, row, 2);	// RezeptDatum in Tabelle uebernehmen
//			}
			setRezDatInTable(stest);
//			String stest2 = jtf[cBEGINDAT].getText().trim();
//			if(stest2.equals(".  .")){		// spaetester Beginn nicht angegeben? -> aus Preisgruppe holen
//				//Preisgruppe holen
//				int pg = Integer.parseInt(jtf[cPREISGR].getText())-1;
//				//Frist zwischen Rezeptdatum und erster Behandlung
//				int frist = (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(0)).get(pg);
//				//Kalendertage
//				if((Boolean) ((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(1)).get(pg)){
//					stest2 = DatFunk.sDatPlusTage(stest, frist);					
//				}else{ //Werktage
//					boolean mitsamstag = (Boolean)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(4)).get(pg);
//					stest2 = HMRCheck.hmrLetztesDatum(stest, (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(0)).get(pg),mitsamstag );
//				}
//			}
//			if(row >= 0){
//				Reha.instance.patpanel.aktRezept.tabaktrez.getModel().setValueAt(stest2, row, 4);	// 'spaetester Beginn' in Tabelle uebernehmen
//			}
			String stest2 = chkLastBeginDat (stest, jtf[cBEGINDAT].getText().trim(), jtf[cPREISGR].getText(), aktuelleDisziplin);
			setLastDatInTable(stest2);
//			sbuf.append("lastdate='"+DatFunk.sDatInSQL(stest2)+"', ");
			myRezept.setLastDate(DatFunk.sDatInSQL(stest2));
//			sbuf.append("lasteddate='"+DatFunk.sDatInSQL(DatFunk.sHeute())+"', ");
			myRezept.setLastEdDate(DatFunk.sDatInSQL(DatFunk.sHeute()));
//			sbuf.append("lastedit='"+Reha.aktUser+"', ");
			myRezept.setLastEdit(Reha.aktUser);
//			sbuf.append("rezeptart='"+Integer.valueOf(jcmb[cVERORD].getSelectedIndex()).toString()+"', ");	// ?? cast int->String->int ?? 
			myRezept.setRezArt(jcmb[cVERORD].getSelectedIndex());
//			sbuf.append("begruendadr='"+(jcb[cBEGRADR].isSelected() ? "T" : "F")+"', ");
			myRezept.setBegrAdR(jcb[cBEGRADR].isSelected());
//			sbuf.append("hausbes='"+(jcb[cHAUSB].isSelected() ? "T" : "F")+"', ");
			myRezept.setHausbesuch(jcb[cHAUSB].isSelected());
			if(myRezept.getHausbesuch()){
				String anzHB = String.valueOf(myRezept.getAnzHB());
				if(!anzHB.equals(jtf[cANZ1].getText())){
					int frage = JOptionPane.showConfirmDialog(null,"Achtung!\n\nDie Anzahl Hausbesuche = "+anzHB+"\n"+
							"Die Anzahl des ersten Heilmittels = "+jtf[cANZ1].getText()+"\n\n"+
							"Soll die Anzahl Hausbesuche ebenfalls auf "+jtf[cANZ1].getText()+" gesetzt werden?","Benutzeranfrage",JOptionPane.YES_NO_OPTION);
					if(frage == JOptionPane.YES_OPTION){
//						sbuf.append("anzahlhb='"+jtf[cANZ1].getText()+"', ");	
						myRezept.setAnzHB(jtf[cANZ1].getText());
					}
				}
			}
//			sbuf.append("arztbericht='"+(jcb[cTBANGEF].isSelected() ? "T" : "F")+"', ");
			myRezept.setArztBericht(jcb[cTBANGEF].isSelected());
//			sbuf.append("anzahl1='"+jtf[cANZ1].getText()+"', ");
			myRezept.setAnzBeh(1,jtf[cANZ1].getText());
//			sbuf.append("anzahl2='"+jtf[cANZ2].getText()+"', ");
			myRezept.setAnzBeh(2,jtf[cANZ2].getText());
//			sbuf.append("anzahl3='"+jtf[cANZ3].getText()+"', ");
			myRezept.setAnzBeh(3,jtf[cANZ3].getText());
//			sbuf.append("anzahl4='"+jtf[cANZ4].getText()+"', ");
			myRezept.setAnzBeh(4,jtf[cANZ4].getText());

//			itest = jcmb[cLEIST1].getSelectedIndex();
//			if(itest > 0){
//				sbuf.append("art_dbeh1='"+preisvec.get(itest-1).get(9)+"', ");
//				sbuf.append("preise1='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
//				sbuf.append("pos1='"+preisvec.get(itest-1).get(2)+"', ");
//				sbuf.append("kuerzel1='"+preisvec.get(itest-1).get(1)+"', ");
//			}else{
//				sbuf.append("art_dbeh1='0', ");
//				sbuf.append("preise1='0.00', ");
//				sbuf.append("pos1='', ");
//				sbuf.append("kuerzel1='', ");
//			}
			for(int i = 0; i < 4;i++){
				int idxVec = i+1;
				itest = jcmb[cLEIST1+i].getSelectedIndex();
				if(itest > 0){		// 0 ist der Leereintrag!
					int idxPv = itest-1;
//					sbuf.append("art_dbeh1='"+preisvec.get(itest-1).get(9)+"', ");
					myRezept.setArtDBehandl(idxVec, preisvec.get(idxPv).get(9));
//					sbuf.append("preise1='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
					myRezept.setPreis(idxVec, preisvec.get(idxPv).get(neuerpreis ? 3 : 4));
//					sbuf.append("pos1='"+preisvec.get(itest-1).get(2)+"', ");
					myRezept.setHmPos(idxVec, preisvec.get(idxPv).get(2));
//					sbuf.append("kuerzel1='"+preisvec.get(itest-1).get(1)+"', ");
					myRezept.setHMkurz(idxVec, preisvec.get(idxPv).get(1));
				}else{
//					sbuf.append("art_dbeh1='0', ");
					myRezept.setArtDBehandl(idxVec, "0");
//					sbuf.append("preise1='0.00', ");
					myRezept.setPreis(idxVec, "0.00");
//					sbuf.append("pos1='', ");
					myRezept.setHmPos(idxVec, "");
//					sbuf.append("kuerzel1='', ");
					myRezept.setHMkurz(idxVec, "");
				}
			}

//			sbuf.append("frequenz='"+jtf[cFREQ].getText()+"', ");
			myRezept.setFrequenz(jtf[cFREQ].getText());
//			sbuf.append("dauer='"+jtf[cDAUER].getText()+"', ");
			myRezept.setDauer(jtf[cDAUER].getText());
			if(jcmb[cINDI].getSelectedIndex() > 0){
//				sbuf.append("indikatschl='"+(String)jcmb[cINDI].getSelectedItem()+"', ");
				myRezept.setIndiSchluessel((String)jcmb[cINDI].getSelectedItem());
			}else{
//				sbuf.append("indikatschl='"+"kein IndiSchl."+"', ");
				myRezept.setIndiSchluessel("kein IndiSchl.");
			}

//			sbuf.append("barcodeform='"+Integer.valueOf(jcmb[cBARCOD].getSelectedIndex()).toString()+"', ");
			myRezept.setBarcodeform(jcmb[cBARCOD].getSelectedIndex());
//			sbuf.append("angelegtvon='"+jtf[cANGEL].getText()+"', ");
			myRezept.setAngelegtVon(jtf[cANGEL].getText());
//			sbuf.append("preisgruppe='"+jtf[cPREISGR].getText()+"', ");
			myRezept.setPreisgruppe(jtf[cPREISGR].getText());
			
			if(jcmb[cFARBCOD].getSelectedIndex() > 0){
				// Lemmi Frage: was bedeutet "14+" in der folgenden Zeile:
//				sbuf.append("farbcode='"+Integer.valueOf(14+jcmb[cFARBCOD].getSelectedIndex()-1).toString()+"', ");	
				myRezept.setFarbCode(13+jcmb[cFARBCOD].getSelectedIndex());
			}else{
//				sbuf.append("farbcode='-1', ");
				myRezept.setFarbCode(-1);
			}
			////System.out.println("Speichern bestehendes Rezept -> Preisgruppe = "+jtf[cPREISGR].getText());
			Integer izuzahl = Integer.valueOf(jtf[cPREISGR].getText());
			String szzstatus = "";
			
			String unter18 = "F";
			for(int i = 0; i < 1;i++){
				if(SystemPreislisten.hmZuzahlRegeln.get(aktuelleDisziplin).get(izuzahl-1) <= 0){
					szzstatus = "0";
					break;
				}
				if(aktuelleDisziplin.equals("Reha")){ 
					szzstatus = "0";
					break;
				}
				if(aktuelleDisziplin.equals("Rsport") || aktuelleDisziplin.equals("Ftrain")){
					szzstatus = "0";
					break;
				}
				////System.out.println("ZuzahlStatus = Zuzahlung (zunächst) erforderlich, prüfe ob befreit oder unter 18");
				if(Reha.instance.patpanel.patDaten.get(30).equals("T")){
					//System.out.println("aktuelles Jahr ZuzahlStatus = Patient ist befreit");
					if(myRezept.getGebuehrBezahlt()){
						szzstatus = "1";
					}else{
						
						if(RezTools.mitJahresWechsel(myRezept.getRezeptDatum())){
							
							String vorjahr = Reha.instance.patpanel.patDaten.get(69); 
							if(vorjahr.trim().equals("")){
								//Nur einspringen wenn keine Vorjahrbefreiung vorliegt.
								//Tabelle mit Einzelterminen auslesen ob Sätze vorhanden
								//wenn Sätze = 0 und bereits im Befreiungszeitraum dann "0", ansonsten "2" 
								//Wenn Sätze > 0 dann ersten Satz auslesen Wenn Datum < Befreiung-ab dann "2" ansonsten "0" 
								if(Reha.instance.patpanel.aktRezept.tabaktterm.getRowCount() > 0){
									// es sind bereits Tage verzeichnet.
									String ersterTag = Reha.instance.patpanel.aktRezept.tabaktterm.getValueAt(0, 0).toString();
									try{
										if(DatFunk.TageDifferenz(DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(41)), ersterTag) >= 0){
											//Behandlung liegt nach befr_ab
											szzstatus = "0";
										}else{
											//Behandlung liegt vor befr_ab
											szzstatus = "2";
										}
									}catch(Exception ex){
										JOptionPane.showMessageDialog(null,"Fehler:\nBefreit ab, im Patientenstamm nicht oder falsch eingetragen");
									}
									
								}else{
									//es sind noch keine Sätze verzeichnet
									if(DatFunk.TageDifferenz(DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(41)), DatFunk.sHeute()) >= 0){
										//Behandlung muß nach befr_ab liegen
										szzstatus = "0";
									}else{
										//Behandlung kann auch vor befr_ab liegen
										szzstatus = "2";
									}
								}
							}else{
								szzstatus = "0";
							}
						}else{
							szzstatus = "0";
						}
					}
					break;
				}
				
				if(DatFunk.Unter18(DatFunk.sHeute(), DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)))){
					//System.out.println("ZuzahlStatus = Patient ist unter 18 also befreit...");
					int aj = Integer.parseInt(SystemConfig.aktJahr)-18;
					String gebtag = DatFunk.sHeute().substring(0,6)+Integer.toString(aj);
					long tage = DatFunk.TageDifferenz(DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)) ,gebtag);

					//System.out.println("Differenz in Tagen = "+tage);
					//System.out.println("Geburtstag = "+gebtag);
					
					if(tage < 0 && tage >= -45){
						JOptionPane.showMessageDialog(null ,"Achtung es sind noch "+(tage*-1)+" Tage bis zur Volljährigkeit\n"+
								"Unter Umständen wechselt der Zuzahlungsstatus im Verlauf dieses Rezeptes");
						szzstatus = "3";
					}else{
						szzstatus = "0";
					}
					//szzstatus = "0";
					unter18 = "T";
					break;
				}
				/**********************/
				if(myRezept.getGebuehrBezahlt() || (myRezept.getGebuehrBetrag() > 0.00) ){
					szzstatus = "1";
				}else{
					// hier testen ob erster Behandlungstag bereits ab dem Befreiungszeitraum
					szzstatus = "2";				
				}
			}
			/******/
			
			String[] lzv= holeLFV("anamnese", "pat5", "pat_intern", jtf[cPATINT].getText(), rezKlasse.toUpperCase().substring(0,2));
			if(!  lzv[0].equals("") ){
				if(!jta.getText().contains(lzv[0]) ){
					int frage = JOptionPane.showConfirmDialog(null, "Für den Patient ist eine Langfristverordnung eingetragen die diese Verordnung noch nicht einschließt.\n\n"+lzv[1]+
							"\n\nWollen Sie diesen Eintrag dieser Verordnung zuweisen?",
							"Achtung wichtige Benutzeranfrage",JOptionPane.YES_NO_OPTION);
					if(frage==JOptionPane.YES_OPTION){
						jta.setText(jta.getText()+"\n"+lzv[0]);
					}
				}
			}
			/*****/
			
//			sbuf.append("unter18='"+((DatFunk.Unter18(DatFunk.sHeute(), DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)))) ? "T', " : "F', "));
			myRezept.setUnter18(unter18);	// oben schon berechnet
//			sbuf.append("zzstatus='"+szzstatus+"', ");
			myRezept.setZzStat(szzstatus);
			//int leistung;
			//String[] str;
//			sbuf.append("diagnose='"+StringTools.Escaped(jta.getText())+"', ");
			myRezept.setDiagn(StringTools.Escaped(jta.getText()));
//			sbuf.append("jahrfrei='"+Reha.instance.patpanel.patDaten.get(69)+"', ");
			myRezept.setvorJahrFrei(Reha.instance.patpanel.patDaten.get(69));		// (?) falls seit Rezeptanlage geaendert (?) (nicht editierbar -> kann in's 'initRezeptAll')
//			sbuf.append("heimbewohn='"+jtf[cHEIMBEW].getText()+"', ");
			myRezept.setHeimbew(jtf[cHEIMBEW].getText());							// dito
//			sbuf.append("hbvoll='"+(jcb[cVOLLHB].isSelected() ? "T" : "F")+"', ");
			myRezept.setHbVoll(jcb[cVOLLHB].isSelected() ? true : false);			// dito
//			sbuf.append("anzahlkm='"+(jtf[cANZKM].getText().trim().equals("") ? "0.00" : jtf[cANZKM].getText().trim())+"', ");
			stest = jtf[cANZKM].getText().trim();									// dito
			myRezept.setKm(stest.equals("") ? "0.00" : stest);
//			sbuf.append("zzregel='"+SystemPreislisten.hmZuzahlRegeln.get(aktuelleDisziplin).get(Integer.parseInt(jtf[cPREISGR].getText())-1 )+"', "); 
			int rule = SystemPreislisten.hmZuzahlRegeln.get(aktuelleDisziplin).get(Integer.parseInt(jtf[cPREISGR].getText())-1 );
			myRezept.setZzRegel(rule);
//			sbuf.append("icd10='"+jtf[cICD10].getText().trim().replace(" ", "")+"', ");
			myRezept.setICD10(jtf[cICD10].getText().replace(" ", ""));
//			sbuf.append("icd10_2='"+jtf[cICD10_2].getText().trim().replace(" ", "")+"' ");
			myRezept.setICD10_2(jtf[cICD10_2].getText().replace(" ", ""));
//			sbuf.append(" where id='"+myRezept.getId()+"' LIMIT 1");

//			SqlInfo.sqlAusfuehren(sbuf.toString());
//			((JXDialog)this.getParent().getParent().getParent().getParent().getParent()).setVisible(false);
//			aufraeumen();
//			((JXDialog)this.getParent().getParent().getParent().getParent().getParent()).dispose();
//			//System.out.println("Rezept wurde mit Preisgruppe "+jtf[cPREISGR].getText()+" gespeichert");
//			setCursor(Reha.instance.cdefault);
//			//System.out.println(sbuf.toString());
		}catch(Exception ex){
			ex.printStackTrace();
			setCursor(Cursors.normalCursor);
			JOptionPane.showMessageDialog(null, "Fehler beim Abspeichern dieses Rezeptes.\n"+
					"Bitte notieren Sie den Namen des Patienten und die Rezeptnummer\n"+
					"und informieren Sie umgehend den Administrator");
		}
	}
    /********************************/

    private Double holePreisDoubleX(String pos,int ipreisgruppe){
        Double dbl = 0.0;
        for(int i = 0; i < preisvec.size();i++){
            if(this.preisvec.get(i).get(0).equals(pos)){
                if(this.preisvec.get(i).get(3).equals("")){
                    return dbl;
                }
                return Double.parseDouble(this.preisvec.get(i).get(3));
            }
        }
        return dbl;
    }
    private Double holePreisDouble(String id,int ipreisgruppe){
        Double dbl = 0.0;
        for(int i = 0; i < preisvec.size();i++){
            if(this.preisvec.get(i).get(9).equals(id)){
                if(this.preisvec.get(i).get(1).equals("")){
                    return dbl;
                }
                return Double.parseDouble(this.preisvec.get(i).get(3));
            }
        }
        return dbl;
    }

    /*********************************/

    private String[] holePreis(int ivec,int ipreisgruppe){
        if(ivec > 0){
            int prid = Integer.valueOf((String) this.preisvec.get(ivec).get(this.preisvec.get(ivec).size()-1));
            Vector<?> xvec = ((Vector<?>)this.preisvec.get(ivec));
            return new String[] {(String)xvec.get(3),(String)xvec.get(2)};
        }else{
            return new String[] {"0.00",""};
        }
    }
    /***********
    *
    *
    */
    /**************************************/
    private void doSpeichernAlt(){
        try{
			/* -> actionPerformed()
//			if(!komplettTest()){
//				return;
//			}
 *  -> actionPerformed() end
 */
			setCursor(Cursors.wartenCursor);		// -> copyFormToVec
			String stest = "";		// -> copyFormToVec
			int itest = -1;		// -> copyFormToVec
			StringBuffer sbuf = new StringBuffer();		// -> copyFormToVec (spA)
			sbuf.append("update verordn set ktraeger='"+jtf[cKTRAEG].getText()+"', ");	// -> copyFormToVec (spA)
			sbuf.append("kid='"+jtf[cKASID].getText()+"', ");		// -> copyFormToVec (spA)
			sbuf.append("arzt='"+jtf[cARZT].getText()+"', ");		// -> copyFormToVec (spA)
			sbuf.append("arztid='"+jtf[cARZTID].getText()+"', ");		// -> copyFormToVec (spA)
            stest = jtf[cREZDAT].getText().trim();
            if(stest.equals(".  .")){
                stest = DatFunk.sHeute();
            }
            boolean neuerpreis = RezTools.neuePreisNachRezeptdatumOderStichtag(aktuelleDisziplin, preisgruppe, String.valueOf(stest),false,Reha.instance.patpanel.vecaktrez);
			sbuf.append("rez_datum='"+DatFunk.sDatInSQL(stest)+"', ");		// -> copyFormToVec (spA)
//			int row = Reha.instance.patpanel.aktRezept.tabaktrez.getSelectedRow();
//			if(row >= 0){
//				Reha.instance.patpanel.aktRezept.tabaktrez.getModel().setValueAt(stest, row, 2);	
//			}
			setRezDatInTable(stest);
//			String stest2 = jtf[cBEGINDAT].getText().trim();
//			if(stest2.equals(".  .")){
//				//Preisgruppe holen
//				int pg = Integer.parseInt(jtf[cPREISGR].getText())-1;
//				//Frist zwischen Rezeptdatum und erster Behandlung
//				int frist = (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(0)).get(pg);
//				//Kalendertage
//				if((Boolean) ((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(1)).get(pg)){
//					stest2 = DatFunk.sDatPlusTage(stest, frist);					
//				}else{ //Werktage
//					boolean mitsamstag = (Boolean)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(4)).get(pg);
//					stest2 = HMRCheck.hmrLetztesDatum(stest, (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(0)).get(pg),mitsamstag );
//				}
//			}
//			if(row >= 0){
//				Reha.instance.patpanel.aktRezept.tabaktrez.getModel().setValueAt(stest2, row, 4);	
//			}
			String stest2 = chkLastBeginDat (stest, jtf[cBEGINDAT].getText().trim(), jtf[cPREISGR].getText(), aktuelleDisziplin);
			setLastDatInTable(stest2);
			sbuf.append("lastdate='"+DatFunk.sDatInSQL(stest2)+"', ");		// -> copyFormToVec (spA)
			sbuf.append("lasteddate='"+DatFunk.sDatInSQL(DatFunk.sHeute())+"', ");		// -> copyFormToVec (spA)
			sbuf.append("lastedit='"+Reha.aktUser+"', ");		// -> copyFormToVec (spA)
			sbuf.append("rezeptart='"+Integer.valueOf(jcmb[cVERORD].getSelectedIndex()).toString()+"', ");		// -> copyFormToVec (spA)
			sbuf.append("begruendadr='"+(jcb[cBEGRADR].isSelected() ? "T" : "F")+"', ");		// -> copyFormToVec (spA)
			sbuf.append("hausbes='"+(jcb[cHAUSB].isSelected() ? "T" : "F")+"', ");		// -> copyFormToVec (spA)
			if(jcb[cHAUSB].isSelected()){		// -> copyFormToVec (spA)
				String anzHB = String.valueOf(myRezept.getAnzHB());
				if(!anzHB.equals(jtf[cANZ1].getText())){
					int frage = JOptionPane.showConfirmDialog(null,"Achtung!\n\nDie Anzahl Hausbesuche = "+anzHB+"\n"+
                            "Die Anzahl des ersten Heilmittels = "+jtf[cANZ1].getText()+"\n\n"+
                            "Soll die Anzahl Hausbesuche ebenfalls auf "+jtf[cANZ1].getText()+" gesetzt werden?","Benutzeranfrage",JOptionPane.YES_NO_OPTION);
                    if(frage == JOptionPane.YES_OPTION){
                        sbuf.append("anzahlhb='"+jtf[cANZ1].getText()+"', ");
                    }
                }
            }
			sbuf.append("arztbericht='"+(jcb[cTBANGEF].isSelected() ? "T" : "F")+"', ");		// -> copyFormToVec (spA)
			sbuf.append("anzahl1='"+jtf[cANZ1].getText()+"', ");		// -> copyFormToVec (spA)
			sbuf.append("anzahl2='"+jtf[cANZ2].getText()+"', ");		// -> copyFormToVec (spA)
			sbuf.append("anzahl3='"+jtf[cANZ3].getText()+"', ");		// -> copyFormToVec (spA)
			sbuf.append("anzahl4='"+jtf[cANZ4].getText()+"', ");		// -> copyFormToVec (spA)
			itest = jcmb[cLEIST1].getSelectedIndex();		// -> copyFormToVec (spA)

			if(itest > 0){		// -> copyFormToVec (spA)
                sbuf.append("art_dbeh1='"+preisvec.get(itest-1).get(9)+"', ");
                sbuf.append("preise1='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
                sbuf.append("pos1='"+preisvec.get(itest-1).get(2)+"', ");
                sbuf.append("kuerzel1='"+preisvec.get(itest-1).get(1)+"', ");

            }else{
                sbuf.append("art_dbeh1='0', ");
                sbuf.append("preise1='0.00', ");
                sbuf.append("pos1='', ");
                sbuf.append("kuerzel1='', ");

            }
			itest = jcmb[cLEIST2].getSelectedIndex();		// -> copyFormToVec (spA)
			if(itest > 0){		// -> copyFormToVec (spA)
                sbuf.append("art_dbeh2='"+preisvec.get(itest-1).get(9)+"', ");
                sbuf.append("preise2='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
                sbuf.append("pos2='"+preisvec.get(itest-1).get(2)+"', ");
                sbuf.append("kuerzel2='"+preisvec.get(itest-1).get(1)+"', ");
            }else{
                sbuf.append("art_dbeh2='0', ");
                sbuf.append("preise2='0.00', ");
                sbuf.append("pos2='', ");
                sbuf.append("kuerzel2='', ");
            }
			itest = jcmb[cLEIST3].getSelectedIndex();		// -> copyFormToVec (spA)
			if(itest > 0){		// -> copyFormToVec (spA)
                sbuf.append("art_dbeh3='"+preisvec.get(itest-1).get(9)+"', ");
                sbuf.append("preise3='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
                sbuf.append("pos3='"+preisvec.get(itest-1).get(2)+"', ");
                sbuf.append("kuerzel3='"+preisvec.get(itest-1).get(1)+"', ");
            }else{
                sbuf.append("art_dbeh3='0', ");
                sbuf.append("preise3='0.00', ");
                sbuf.append("pos3='', ");
                sbuf.append("kuerzel3='', ");
            }
			itest = jcmb[cLEIST4].getSelectedIndex();		// -> copyFormToVec (spA)
			if(itest > 0){		// -> copyFormToVec (spA)
                sbuf.append("art_dbeh4='"+preisvec.get(itest-1).get(9)+"', ");
                sbuf.append("preise4='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
                sbuf.append("pos4='"+preisvec.get(itest-1).get(2)+"', ");
                sbuf.append("kuerzel4='"+preisvec.get(itest-1).get(1)+"', ");
            }else{
                sbuf.append("art_dbeh4='0', ");
                sbuf.append("preise4='0.00', ");
                sbuf.append("pos4='', ");
                sbuf.append("kuerzel4='', ");
            }
			sbuf.append("frequenz='"+jtf[cFREQ].getText()+"', ");		// -> copyFormToVec (spA)
			sbuf.append("dauer='"+jtf[cDAUER].getText()+"', ");		// -> copyFormToVec (spA)
			if(jcmb[cINDI].getSelectedIndex() > 0){		// -> copyFormToVec (spA)
                sbuf.append("indikatschl='"+(String)jcmb[cINDI].getSelectedItem()+"', ");
            }else{
                sbuf.append("indikatschl='"+"kein IndiSchl."+"', ");
            }
			sbuf.append("barcodeform='"+Integer.valueOf(jcmb[cBARCOD].getSelectedIndex()).toString()+"', ");		// -> copyFormToVec (spA)
			sbuf.append("angelegtvon='"+jtf[cANGEL].getText()+"', ");		// -> copyFormToVec (spA)
			sbuf.append("preisgruppe='"+jtf[cPREISGR].getText()+"', ");		// -> copyFormToVec (spA)

			if(jcmb[cFARBCOD].getSelectedIndex() > 0){		// -> copyFormToVec (spA)
                // Lemmi Frage: was bedeutet "14+" in der folgenden Zeile:
                sbuf.append("farbcode='"+Integer.valueOf(14+jcmb[cFARBCOD].getSelectedIndex()-1).toString()+"', ");
            }else{
                sbuf.append("farbcode='-1', ");
            }
            ////System.out.println("Speichern bestehendes Rezept -> Preisgruppe = "+jtf[cPREISGR].getText());
			Integer izuzahl = Integer.valueOf(jtf[cPREISGR].getText());		// -> copyFormToVec (spA)
			String szzstatus = "";		// -> copyFormToVec (spA)

			String unter18 = "F";		// -> copyFormToVec (spA)
			for(int i = 0; i < 1;i++){		// -> copyFormToVec (spA)
                if(SystemPreislisten.hmZuzahlRegeln.get(aktuelleDisziplin).get(izuzahl-1) <= 0){
                    szzstatus = "0";
                    break;
                }
                if(aktuelleDisziplin.equals("Reha")){
                    szzstatus = "0";
                    break;
                }
                if(aktuelleDisziplin.equals("Rsport") || aktuelleDisziplin.equals("Ftrain")){
                    szzstatus = "0";
                    break;
                }
                ////System.out.println("ZuzahlStatus = Zuzahlung (zunächst) erforderlich, prüfe ob befreit oder unter 18");
                if(Reha.instance.patpanel.patDaten.get(30).equals("T")){
                    //System.out.println("aktuelles Jahr ZuzahlStatus = Patient ist befreit");
					if(myRezept.getGebuehrBezahlt()){
                        szzstatus = "1";
                    }else{

						if(RezTools.mitJahresWechsel(DatFunk.sDatInDeutsch(myRezept.getRezeptDatum()))){

                            String vorjahr = Reha.instance.patpanel.patDaten.get(69);
                            if(vorjahr.trim().equals("")){
                                //Nur einspringen wenn keine Vorjahrbefreiung vorliegt.
                                //Tabelle mit Einzelterminen auslesen ob Sätze vorhanden
                                //wenn Sätze = 0 und bereits im Befreiungszeitraum dann "0", ansonsten "2"
                                //Wenn Sätze > 0 dann ersten Satz auslesen Wenn Datum < Befreiung-ab dann "2" ansonsten "0"
                                if(Reha.instance.patpanel.aktRezept.tabaktterm.getRowCount() > 0){
                                    // es sind bereits Tage verzeichnet.
                                    String ersterTag = Reha.instance.patpanel.aktRezept.tabaktterm.getValueAt(0, 0).toString();
                                    try{
                                        if(DatFunk.TageDifferenz(DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(41)), ersterTag) >= 0){
                                            //Behandlung liegt nach befr_ab
                                            szzstatus = "0";
                                        }else{
                                            //Behandlung liegt vor befr_ab
                                            szzstatus = "2";
                                        }
                                    }catch(Exception ex){
                                        JOptionPane.showMessageDialog(null,"Fehler:\nBefreit ab, im Patientenstamm nicht oder falsch eingetragen");
                                    }

                                }else{
                                    //es sind noch keine Sätze verzeichnet
                                    if(DatFunk.TageDifferenz(DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(41)), DatFunk.sHeute()) >= 0){
                                        //Behandlung muß nach befr_ab liegen
                                        szzstatus = "0";
                                    }else{
                                        //Behandlung kann auch vor befr_ab liegen
                                        szzstatus = "2";
                                    }
                                }
                                /*
                                if(this.vec.get(34).indexOf(vorjahr)>=0){
                                    szzstatus = "2";
                                }else{
                                    szzstatus = "0";
                                }
                                */
                            }else{
                                szzstatus = "0";
                            }
                        }else{
                            szzstatus = "0";
                        }

                        //Im Patientenstamm liegt eine aktuelle befreiung vor
                        //testen ob sich das Rezept über den Jahreswechsel erstreckt
                        //wenn ja war er damals auch befreit, wenn ja Status == 0
                        //wenn nein Status == 2 == nicht befreit und nicht bezahlt
                        //szzstatus = "0";
                        /*
						if(Reha.instance.patpanel.aktRezept.tabaktterm.getRowCount() > 0){
							String ersterTag = Reha.instance.patpanel.aktRezept.tabaktterm.getValueAt(0, 0).toString();
							if(DatFunk.TageDifferenz(Reha.instance.patpanel.patDaten.get(41), ersterTag) >= 0){

                            }
                        }else{
                            //noch keine Behandlung
							if(DatFunk.TageDifferenz(Reha.instance.patpanel.patDaten.get(41), DatFunk.sHeute()) >= 0){
                                System.out.println("Noch keine Behandlung vermerkt aber bereits im Befr.Zeitraum angekommen");
								System.out.println(DatFunk.TageDifferenz(Reha.instance.patpanel.patDaten.get(41), DatFunk.sHeute()));
                                szzstatus = "0";
                            }

                        }
                        */
                    }
                    break;
                }

                if(DatFunk.Unter18(DatFunk.sHeute(), DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)))){
                    //System.out.println("ZuzahlStatus = Patient ist unter 18 also befreit...");
                    int aj = Integer.parseInt(SystemConfig.aktJahr)-18;
                    String gebtag = DatFunk.sHeute().substring(0,6)+Integer.toString(aj);
                    long tage = DatFunk.TageDifferenz(DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)) ,gebtag);

                    //System.out.println("Differenz in Tagen = "+tage);
                    //System.out.println("Geburtstag = "+gebtag);

                    if(tage < 0 && tage >= -45){
                        JOptionPane.showMessageDialog(null ,"Achtung es sind noch "+(tage*-1)+" Tage bis zur Volljährigkeit\n"+
                                "Unter Umständen wechselt der Zuzahlungsstatus im Verlauf dieses Rezeptes");
                        szzstatus = "3";
                    }else{
                        szzstatus = "0";
                    }
                    //szzstatus = "0";
                    unter18 = "T";
                    break;
                }
                /**********************/
				if(myRezept.getGebuehrBezahlt() || (myRezept.getGebuehrBetrag() > 0.00) ){
                    szzstatus = "1";
                }else{
                    // hier testen ob erster Behandlungstag bereits ab dem Befreiungszeitraum
                    szzstatus = "2";
                }
            }
            /******/

			String[] lzv= holeLFV("anamnese", "pat5", "pat_intern", jtf[cPATINT].getText(), rezKlasse.toUpperCase().substring(0,2));
            if(!  lzv[0].equals("") ){
                if(!jta.getText().contains(lzv[0]) ){
                    int frage = JOptionPane.showConfirmDialog(null, "Für den Patient ist eine Langfristverordnung eingetragen die diese Verordnung noch nicht einschließt.\n\n"+lzv[1]+
                            "\n\nWollen Sie diesen Eintrag dieser Verordnung zuweisen?",
                            "Achtung wichtige Benutzeranfrage",JOptionPane.YES_NO_OPTION);
                    if(frage==JOptionPane.YES_OPTION){
                        jta.setText(jta.getText()+"\n"+lzv[0]);
                    }
                }
            }
            /*****/

			sbuf.append("unter18='"+((DatFunk.Unter18(DatFunk.sHeute(), DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)))) ? "T', " : "F', "));		// -> copyFormToVec (spA)
			sbuf.append("zzstatus='"+szzstatus+"', ");		// -> copyFormToVec (spA)
            //int leistung;
            //String[] str;
			sbuf.append("diagnose='"+StringTools.Escaped(jta.getText())+"', ");		// -> copyFormToVec (spA)
			sbuf.append("jahrfrei='"+Reha.instance.patpanel.patDaten.get(69)+"', ");		// -> copyFormToVec (spA)
			sbuf.append("heimbewohn='"+jtf[cHEIMBEW].getText()+"', ");		// -> copyFormToVec (spA)
			sbuf.append("hbvoll='"+(jcb[cVOLLHB].isSelected() ? "T" : "F")+"', ");		// -> copyFormToVec (spA)
			sbuf.append("anzahlkm='"+(jtf[cANZKM].getText().trim().equals("") ? "0.00" : jtf[cANZKM].getText().trim())+"', ");		// -> copyFormToVec (spA)
			sbuf.append("zzregel='"+SystemPreislisten.hmZuzahlRegeln.get(aktuelleDisziplin).get(Integer.parseInt(jtf[cPREISGR].getText())-1 )+"', ");		// -> copyFormToVec (spA)
			sbuf.append("icd10='"+jtf[cICD10].getText().trim().replace(" ", "")+"', ");		// -> copyFormToVec (spA)
			sbuf.append("icd10_2='"+jtf[cICD10_2].getText().trim().replace(" ", "")+"' ");		// -> copyFormToVec (spA)
			sbuf.append(", rsplit='dSpAlt' ");	// -> debug-Hilfe
			sbuf.append(" where id='"+myRezept.getId()+"' LIMIT 1");

			SqlInfo.sqlAusfuehren(sbuf.toString());		// debug-Hilfe: doSpeichernAlt
            //System.out.println("Rezept wurde mit Preisgruppe "+jtf[cPREISGR].getText()+" gespeichert");
			setCursor(Cursors.normalCursor);
            //System.out.println(sbuf.toString());
//			((JXDialog)this.getParent().getParent().getParent().getParent().getParent()).setVisible(false);
		//	closeDialog();
		//	aufraeumen();
//			((JXDialog)this.getParent().getParent().getParent().getParent().getParent()).dispose();
//			setCursor(Reha.instance.cdefault);
        }catch(Exception ex){
            ex.printStackTrace();
			setCursor(Cursors.normalCursor);
            JOptionPane.showMessageDialog(null, "Fehler beim Abspeichern dieses Rezeptes.\n"+
                    "Bitte notieren Sie den Namen des Patienten und die Rezeptnummer\n"+
                    "und informieren Sie umgehend den Administrator");
        }

    }

	/**
	 * 
	 * Test, ob eine Langfristverordnung vorliegt
	 */
    public static String[] holeLFV(String hole_feld, String db,String where_feld, String suchen,String voart){
        String cmd = "select "+hole_feld+" from "+db+" where "+where_feld+"='"+suchen+"' LIMIT 1";
        String anamnese = SqlInfo.holeEinzelFeld(cmd);
        String[] retstring = {"",""};
        if(anamnese.indexOf("$$LFV$$"+voart.toUpperCase()+"$$") >= 0){
            String[] zeilen = anamnese.split("\n");
            for(int i = 0; i < zeilen.length;i++){
                if(zeilen[i].startsWith("$$LFV$$"+voart.toUpperCase()+"$$")){
                    String[] woerter = zeilen[i].split(Pattern.quote("$$"));
                    try{
                        retstring[1] = "LangfristVerordnung: "+woerter[1]+"\n"+
                                "Disziplin: "+woerter[2]+"\n"+
                                "Aktenzeichen: "+woerter[3]+"\n"+
                                "Genehmigungsdatum: "+woerter[4]+"\n"+
                                "Gültig ab: "+woerter[5]+"\n"+
                                "Gültig bis: "+woerter[6]+"\n";
                        retstring[0] = String.valueOf(zeilen[i]);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                    return retstring;
                }
            }

        }
        return retstring;
    }
    /**************************************/
    private void doSpeichernNeu(){
        try{
            int reznr = -1;
			/* -> actionPerformed()
//			if(!komplettTest()){
//				return;
//			}

//			long dattest = DatFunk.TageDifferenz(DatFunk.sHeute(),jtf[cREZDAT].getText().trim() );
//			//long min = -364;
//			//long max = 364;
//			if( (dattest <= -364) || (dattest >= 364) ){
//				int frage = JOptionPane.showConfirmDialog(null, "<html><b>Das Rezeptdatum ist etwas kritisch....<br><br><font color='#ff0000'> "+
//						"Rezeptdatum = "+jtf[cREZDAT].getText().trim()+"</font></b><br>Das sind ab Heute "+Long.toString(dattest)+" Tage<br><br><br>"+
//						"Wollen Sie dieses Rezeptdatum tatsächlich abspeichern?", "Bedenkliches Rezeptdatum",JOptionPane.YES_NO_OPTION);
//				if(frage!=JOptionPane.YES_OPTION){
//					 SwingUtilities.invokeLater(new Runnable(){
//					 	   public  void run()
//					 	   {
//								jtf[cREZDAT].requestFocus();
//					 	   }
//					});	   		
//					return;
//				}
//			}			
 *  -> actionPerformed() end
 */
			setCursor(Cursors.wartenCursor);		// -> copyFormToVec
			String stest = "";		// -> copyFormToVec
			int itest = -1;		// -> copyFormToVec
			StringBuffer sbuf = new StringBuffer();		// -> copyFormToVec (spN)
            //System.out.println("Nummer = "+nummer);
// -> Rezept.setNewRezNb()
			reznr = SqlInfo.erzeugeNummer(rezKlasse.toLowerCase());
            if(reznr < 0){
                JOptionPane.showMessageDialog(null,"Schwerwiegender Fehler beim Bezug einer neuen Rezeptnummer!");
				setCursor(Cursors.normalCursor); 
                return;
            }
            int rezidneu = SqlInfo.holeId("verordn", "diagnose");
// -> Rezept.setNewRezNb() End
			sbuf.append("update verordn set ");		// -> copyFormToVec (spN)
			sbuf.append("rez_nr='"+rezKlasse.toUpperCase()+Integer.valueOf(reznr).toString()+"', ");		// -> Rezept.setNewRezNb()
			sbuf.append("pat_intern='"+jtf[cPATINT].getText()+"', ");		// -> initRezeptNeu()
			sbuf.append("patid='"+jtf[cPATID].getText()+"', ");		// -> initRezeptNeu()
			sbuf.append("ktraeger='"+jtf[cKTRAEG].getText()+"', ");		// -> copyFormToVec (spN)
			sbuf.append("kid='"+jtf[cKASID].getText()+"', ");		// -> copyFormToVec (spN)
			sbuf.append("arzt='"+jtf[cARZT].getText()+"', ");		// -> copyFormToVec (spN)
			sbuf.append("arztid='"+jtf[cARZTID].getText()+"', ");		// -> copyFormToVec (spN)
			stest = DatFunk.sHeute();
			sbuf.append("datum='"+DatFunk.sDatInSQL(stest)+"', ");		// -> initRezeptAll() (angelegtDatum)
            stest = DatFunk.sHeute();
            sbuf.append("datum='"+DatFunk.sDatInSQL(stest)+"', ");
            stest = jtf[cREZDAT].getText().trim();
            if(stest.equals(".  .")){
                stest = DatFunk.sHeute();
            }

            boolean neuerpreis = RezTools.neuePreisNachRezeptdatumOderStichtag(aktuelleDisziplin, preisgruppe, String.valueOf(stest),true,null);
            //Zunächst ermitteln welche Fristen und ob Kalender oder Werktage gelten
            //Dann das Rezeptdatum übergeben, Rückgabewert ist spätester Beginn.
			sbuf.append("rez_datum='"+DatFunk.sDatInSQL(stest)+"', ");		// -> copyFormToVec (spN)
//			String stest2 = jtf[cBEGINDAT].getText().trim();
//			if(stest2.equals(".  .")){
//				//Preisgruppe holen
//				int pg = Integer.parseInt(jtf[cPREISGR].getText())-1;
//				//Frist zwischen Rezeptdatum und erster Behandlung holen
//				int frist = (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(0)).get(pg);
//				//Kalendertage
//				if((Boolean) ((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(1)).get(pg)){
//					stest2 = DatFunk.sDatPlusTage(stest, frist);					
//				}else{ //Werktage
//					boolean mitsamstag = (Boolean)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(4)).get(pg);
//					stest2 = HMRCheck.hmrLetztesDatum(stest, (Integer)((Vector<?>)SystemPreislisten.hmFristen.get(aktuelleDisziplin).get(0)).get(pg),mitsamstag );
//				}
//			}
			String stest2 = chkLastBeginDat (stest, jtf[cBEGINDAT].getText().trim(), jtf[cPREISGR].getText(), aktuelleDisziplin);
			sbuf.append("lastdate='"+DatFunk.sDatInSQL(stest2)+"', ");		// -> copyFormToVec (spN)
			sbuf.append("lasteddate='"+DatFunk.sDatInSQL(DatFunk.sHeute())+"', ");		// -> copyFormToVec (spN)
			sbuf.append("lastedit='"+Reha.aktUser+"', ");		// -> copyFormToVec (spN)
			sbuf.append("rezeptart='"+Integer.valueOf(jcmb[cVERORD].getSelectedIndex()).toString()+"', ");		// -> copyFormToVec (spN)
			sbuf.append("begruendadr='"+(jcb[cBEGRADR].isSelected() ? "T" : "F")+"', ");		// -> copyFormToVec (spN)
			sbuf.append("hausbes='"+(jcb[cHAUSB].isSelected() ? "T" : "F")+"', ");		// -> copyFormToVec (spN)
			sbuf.append("arztbericht='"+(jcb[cTBANGEF].isSelected() ? "T" : "F")+"', ");		// -> copyFormToVec (spN)
			sbuf.append("anzahl1='"+jtf[cANZ1].getText()+"', ");		// -> copyFormToVec (spN)
			sbuf.append("anzahl2='"+jtf[cANZ2].getText()+"', ");		// -> copyFormToVec (spN)
			sbuf.append("anzahl3='"+jtf[cANZ3].getText()+"', ");		// -> copyFormToVec (spN)
			sbuf.append("anzahl4='"+jtf[cANZ4].getText()+"', ");		// -> copyFormToVec (spN)
			sbuf.append("anzahlhb='"+jtf[cANZ1].getText()+"', ");		// -> copyFormToVec1stTime (spN)
			itest = jcmb[cLEIST1].getSelectedIndex();		// -> copyFormToVec (spN)
			if(itest > 0){		// -> copyFormToVec (spN)
                sbuf.append("art_dbeh1='"+preisvec.get(itest-1).get(9)+"', ");
                sbuf.append("preise1='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
                sbuf.append("pos1='"+preisvec.get(itest-1).get(2)+"', ");
                sbuf.append("kuerzel1='"+preisvec.get(itest-1).get(1)+"', ");
            }else{
                sbuf.append("art_dbeh1='0', ");
            }
			itest = jcmb[cLEIST2].getSelectedIndex();		// -> copyFormToVec (spN)
			if(itest > 0){		// -> copyFormToVec (spN)
                sbuf.append("art_dbeh2='"+preisvec.get(itest-1).get(9)+"', ");
                sbuf.append("preise2='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
                sbuf.append("pos2='"+preisvec.get(itest-1).get(2)+"', ");
                sbuf.append("kuerzel2='"+preisvec.get(itest-1).get(1)+"', ");
			}else{		// -> copyFormToVec (spN)
                sbuf.append("art_dbeh2='0', ");
            }
			itest = jcmb[cLEIST3].getSelectedIndex();		// -> copyFormToVec (spN)
			if(itest > 0){		// -> copyFormToVec (spN)
                sbuf.append("art_dbeh3='"+preisvec.get(itest-1).get(9)+"', ");
                sbuf.append("preise3='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
                sbuf.append("pos3='"+preisvec.get(itest-1).get(2)+"', ");
                sbuf.append("kuerzel3='"+preisvec.get(itest-1).get(1)+"', ");
            }else{
                sbuf.append("art_dbeh3='0', ");
            }
			itest = jcmb[cLEIST4].getSelectedIndex();		// -> copyFormToVec (spN)
			if(itest > 0){		// -> copyFormToVec (spN)
                sbuf.append("art_dbeh4='"+preisvec.get(itest-1).get(9)+"', ");
                sbuf.append("preise4='"+preisvec.get(itest-1).get((neuerpreis ? 3 : 4))+"', ");
                sbuf.append("pos4='"+preisvec.get(itest-1).get(2)+"', ");
                sbuf.append("kuerzel4='"+preisvec.get(itest-1).get(1)+"', ");
            }else{
                sbuf.append("art_dbeh4='0', ");
            }
			sbuf.append("frequenz='"+jtf[cFREQ].getText()+"', ");		// -> copyFormToVec (spN)
			sbuf.append("dauer='"+jtf[cDAUER].getText()+"', ");		// -> copyFormToVec (spN)
			if(jcmb[cINDI].getSelectedIndex() > 0){		// -> copyFormToVec (spN)
                sbuf.append("indikatschl='"+(String)jcmb[cINDI].getSelectedItem()+"', ");
            }else{
                sbuf.append("indikatschl='"+"kein IndiSchl."+"', ");
            }
			sbuf.append("barcodeform='"+Integer.toString(jcmb[cBARCOD].getSelectedIndex())+"', ");		// -> copyFormToVec (spN)
			sbuf.append("angelegtvon='"+jtf[cANGEL].getText()+"', ");		// -> copyFormToVec (spN)
			sbuf.append("preisgruppe='"+jtf[cPREISGR].getText()+"', ");		// -> copyFormToVec (spN)
			if(jcmb[cFARBCOD].getSelectedIndex() > 0){		// -> copyFormToVec (spN)
                sbuf.append("farbcode='"+Integer.toString(14+jcmb[cFARBCOD].getSelectedIndex()-1).toString()+"', ");
            }else{
                sbuf.append("farbcode='-1', ");
            }

    /*******************************************/
			Integer izuzahl = Integer.valueOf(jtf[cPREISGR].getText());		// -> copyFormToVec (spN)
			String unter18 = "F";		// -> copyFormToVec (spN)
			String szzstatus = "";		// -> copyFormToVec (spN)
            for(int i = 0; i < 1;i++){
                //if(SystemConfig.vZuzahlRegeln.get(izuzahl-1) <= 0){
                if(SystemPreislisten.hmZuzahlRegeln.get(aktuelleDisziplin).get(izuzahl-1) <= 0){
                    //System.out.println("1. ZuzahlStatus = Zuzahlung nicht erforderlich");
                    szzstatus = "0";
                    break;
                }
				if(rezKlasse.equalsIgnoreCase("rh")){
                    szzstatus = "0";
                    break;
                }
				if(rezKlasse.equalsIgnoreCase("rs") || rezKlasse.equalsIgnoreCase("ft")){
                    szzstatus = "0";
                    break;
                }
                ////System.out.println("ZuzahlStatus = Zuzahlung (zunï¿½chst) erforderlich, prï¿½fe ob befreit oder unter 18");
                if(Reha.instance.patpanel.patDaten.get(30).equals("T")){
                    //System.out.println("2. ZuzahlStatus = Patient ist befreit");
                    szzstatus = "0";
                    break;
                }
                if(DatFunk.Unter18(DatFunk.sHeute(), DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)))){
                    ////System.out.println("ZuzahlStatus = Patient ist unter 18 also befreit...");
                    String gebtag = DatFunk.sHeute().substring(0,6)+Integer.valueOf(Integer.valueOf(SystemConfig.aktJahr)-18).toString();
                    long tage = DatFunk.TageDifferenz(DatFunk.sDatInDeutsch(Reha.instance.patpanel.patDaten.get(4)) ,gebtag);
                    //System.out.println("Differenz in Tagen = "+tage);
                    //System.out.println("Geburtstag = "+gebtag);
                    if(tage < 0 && tage >= -45){
                        JOptionPane.showMessageDialog(null ,"Achtung es sind noch "+(tage*-1)+" Tage bis zur Volljährigkeit\n"+
                                "Unter Umständen wechselt der Zuzahlungsstatus im Verlauf dieses Rezeptes");
                        szzstatus = "3";
                    }else{
                        szzstatus = "0";
                    }
                    unter18 = "T";
                    break;
                }
                ////System.out.println("Normale Zuzahlung -> status noch nicht bezahlt");
                szzstatus = "2";
            }
			String[] lzv= holeLFV("anamnese", "pat5", "pat_intern", jtf[cPATINT].getText(), rezKlasse.toUpperCase().substring(0,2));
            if(!  lzv[0].equals("") ){
                if(!jta.getText().contains(lzv[0]) ){
                    int frage = JOptionPane.showConfirmDialog(null, "Für den Patient ist eine Langfristverordnung eingetragen.\n\n"+lzv[1]+
                            "\n\nWollen Sie diesen Eintrag dieser Verordnung zuweisen?",
                            "Achtung wichtige Benutzeranfrage",JOptionPane.YES_NO_OPTION);
                    if(frage==JOptionPane.YES_OPTION){
                        jta.setText(jta.getText()+"\n"+lzv[0]);
                    }
                }
            }
			sbuf.append("zzstatus='"+szzstatus+"', ");		// -> copyFormToVec (spN)
			sbuf.append("diagnose='"+StringTools.Escaped(jta.getText())+"', ");		// -> copyFormToVec (spN)
			sbuf.append("unter18='"+unter18+"', ");		// -> copyFormToVec (spN)
			sbuf.append("jahrfrei='"+Reha.instance.patpanel.patDaten.get(69)+"', ");		// -> copyFormToVec (spN)
			sbuf.append("heimbewohn='"+jtf[cHEIMBEW].getText()+"', ");		// -> copyFormToVec (spN)
			sbuf.append("hbvoll='"+(jcb[cVOLLHB].isSelected() ? "T" : "F")+"', ");		// -> copyFormToVec (spN)
			sbuf.append("anzahlkm='"+(jtf[cANZKM].getText().trim().equals("") ? "0.00" : jtf[cANZKM].getText().trim())+"', ");		// -> copyFormToVec (spN)
			sbuf.append("befr='"+Reha.instance.patpanel.patDaten.get(30)+"', ");		// -> initRezeptAll()
			sbuf.append("zzregel='"+SystemPreislisten.hmZuzahlRegeln.get(aktuelleDisziplin).get(Integer.valueOf(jtf[cPREISGR].getText())-1 )+"',");		// -> copyFormToVec (spN)
			sbuf.append("icd10='"+jtf[cICD10].getText().trim().replace(" ", "")+"', ");		// -> copyFormToVec (spN)
			sbuf.append("icd10_2='"+jtf[cICD10_2].getText().trim().replace(" ", "")+"' ");		// -> copyFormToVec (spN)
			sbuf.append(", rsplit='dSpNeu' ");	// -> debug-Hilfe
            sbuf.append("where id='"+Integer.toString(rezidneu)+"'  LIMIT 1");
			SqlInfo.sqlAusfuehren(sbuf.toString());		// debug-Hilfe: doSpeichernNeu
            //System.out.println("Rezept wurde mit Preisgruppe "+jtf[cPREISGR].getText()+" gespeichert");
			Reha.instance.patpanel.aktRezept.setzeRezeptNummerNeu(rezKlasse.toUpperCase()+Integer.toString(reznr));
			//Reha.instance.patpanel.aktRezept.holeRezepte(jtf[cPATINT].getText(),nummer.toUpperCase()+Integer.toString(reznr));
//			((JXDialog)this.getParent().getParent().getParent().getParent().getParent()).setVisible(false);
		//	closeDialog();
		//	aufraeumen();
//			((JXDialog)this.getParent().getParent().getParent().getParent().getParent()).dispose();
//			setCursor(Reha.instance.cdefault);
        }catch(Exception ex){
            ex.printStackTrace();
			setCursor(Cursors.normalCursor);
            JOptionPane.showMessageDialog(null, "Fehler beim Abspeichern dieses Rezeptes.\n"+
                    "Bitte notieren Sie den Namen des Patienten und die Rezeptnummer\n"+
                    "und informieren Sie umgehend den Administrator\n"+makeStacktraceToString(ex));
        }

    }
    public static String makeStacktraceToString(Exception ex){
        String string = "";
        try{
            StackTraceElement[] se = ex.getStackTrace();
            for(int i = 0; i < se.length; i++){
                string = string+se[i].toString()+"\n";
            }
        }catch(Exception ex2){

        }
        return string;
    }

    // Lemmi 20110101: Kopieren des letzten Rezepts des selben Patienten bei Rezept-Neuanlage

	private void doKopiereLetztesRezeptDesPatienten() {		// McM: obsolet nach Verwendung Rezept-Klasse (ladeZusatzDatenAlt/neu dito)
        /** KONZEPT
        holle alle Rezepte aus den Tabellen "verordn" und "lza" zum aktuellen Patienten sortiert und finde das neueste als erstes in der Liste
        falls es Rezepte zu mehreren Disziplinen gibt, müßte man hier noch interaktiv abfragen, welches gemeint sein soll (nicht eingebaut)
        dann hole die Daten aus dem alten Rezept in einen Vektor analog vekcaktrez und schiebe sie in das Rezept via ladeZusatzDatenAlt()
        lösche alle Felder aus dem Vektor, die im akt. rezept gar nicht sein können (zB cREZDAT)
        dann setzte nochmal neue Daten drüber ladeZusatzDatenNeu()
        **/

        // Definition der Inices für den Vektor "vecaktrez"
        // Lemmi Todo: DAS MUSS VOLLSTÄNDIG GEMACHT UND AN ZENTRALE STELLE VERSCHOBEN WERDEN !!!
        final int cVAR_PATID = 0;
        final int cVAR_REZNR = 1;
        final int cVAR_REZDATUM = 2;
        final int cVAR_ANZAHL1 = 3;
        final int cVAR_ANZAHL2 = 4;
        final int cVAR_ANZAHL3 = 5;
        final int cVAR_ANZAHL4 = 6;
        final int cVAR_ANZAHLKM = 7;
        final int cVAR_ARTDBEH1 = 8;  // Art der Behandlung
        final int cVAR_ARTDBEH2 = 9;
        final int cVAR_ARTDBEH3 = 10;
        final int cVAR_ARTDBEH4 = 11;
        final int cVAR_BEFREIT = 12;  // BEFR
        final int cVAR_REZGEBUEHR = 13;
        final int cVAR_BEZAHLT = 14;  // REZ_BEZ
        final int cVAR_ARZTNAM = 15;
        final int cVAR_ARZTID = 16;
        final int cVAR_AERZTE = 17;
        final int cVAR_PREIS1 = 18;
        final int cVAR_PREIS2 = 19;
        final int cVAR_PREIS3 = 20;
        final int cVAR_PREIS4 = 21;
        final int cVAR_DATANGEL = 22;
        final int cVAR_DIAGNOSE = 23;

        final int cVAR_TERMINE = 34;

        final int cVAR_ZZSTAT = 39;
        final int cVAR_LASTDAT = 40;  // spätester Behandlungsbginn
        final int cVAR_PREISGR = 41;
        final int cVAR_BEGRUENDADR = 42;
        final int cVAR_HAUSBES = 43;
        final int cVAR_INDI = 44;
        final int cVAR_ANGEL = 45;
        final int cVAR_BARCOD = 46;  // BARCODEFORM
        final int cVAR_DAUER = 47;
        final int cVAR_POS1 = 48;
        final int cVAR_POS2 = 49;
        final int cVAR_POS3 = 50;
        final int cVAR_POS4 = 51;
        final int cVAR_FREQ = 52;
        final int cVAR_LASTEDIT = 53;
        final int cVAR_BERID = 54;
        final int cVAR_ARZTBER = 55;
        final int cVAR_LASTEDDATE = 56;
        final int cVAR_FARBCOD = 57;
        final int cVAR_RSPLIT = 58;
        final int cVAR_JAHRFREI = 59;

        final int cVAR_HBVOLL = 61;

        final int cVAR_ICD10 = 71;
        final int cVAR_ICD10_2 = 72;
        //Funktion ist immer noch suboptimal, da der Kostenträger des Rezeptes noch nicht übernommen wird.

        //String strPat_Intern = jtf[cPATINT].getText();

        // für die Rückmeldung zum Setezen der Dailogüberschrift
        strKopiervorlage = "";

        if ( vec.size() > 0 ) {   // nur wenn etwas gefunden werden konnte !

            // Titel des Dialogs individualisieren für die Rückmeldung zum Setezen der Dailogüberschrift
            strKopiervorlage = vec.get(cVAR_REZNR);

            jcmb[cRKLASSE].setSelectedIndex( Arrays.asList(strRezepklassenAktiv).indexOf(strKopiervorlage.substring(0,2)) );


            // Löschen der auf jeden Fall "falsch weil alt" Komponenten
            vec.set(cVAR_REZNR, "");
            vec.set(cVAR_REZDATUM, "");
            vec.set(cVAR_TERMINE, "");
            vec.set(cVAR_ZZSTAT, "");
            vec.set(cVAR_LASTDAT, "");

            if(SystemConfig.AngelegtVonUser){
                vec.set(cVAR_ANGEL, Reha.aktUser);
            }else{
                vec.set(cVAR_ANGEL, "");
            }
            vec.set(cVAR_LASTEDIT, "");

            vec.set(cVAR_BEFREIT, "");

            vec.set(cVAR_BEZAHLT, "F" );    // Das kann noch nicht bezahlt sein (Rezeptgebühr)

			jtf[cKTRAEG].setText(vec.get(36)); //ktraeger	McM: kommt eh aus RezeptClass
			jtf[cKASID].setText(vec.get(37)); //kassenid		-"-
			preisgruppe = Integer.parseInt(vec.get(41));	//	-"-

			copyVecToForm();  // Eintragen von vec in die Dialog-Felder

            ladeZusatzDatenNeu();  // Hier nochmals die neuen Daten ermitteln - schließlich haben wir ein neues Rezept !

            //jtf[cKTRAEG].setText(vec.get(36)); //ktraeger
            //jtf[cKASID].setText(vec.get(37)); //kassenid
            jtf[cARZT].setText(vec.get(15)); //arzt
            jtf[cARZTID].setText(vec.get(16)); //arztid

            jtf[cICD10].setText(vec.get(71)); //icd10
            jtf[cICD10_2].setText(vec.get(72)); //icd10_2

            //preisgruppe = Integer.parseInt(vec.get(41));

            // Lemmi 20110106: Lieber Hr. Steinhilber: Das fkt. nicht, weil hier nicht alle Disziplinen aktiv sein müssen !
            //erneuter Aufruf damit die korrekte Preisgruppe genommen wird GKV vs. BGE etc.
///            jcmb[cRKLASSE].setSelectedIndex( Arrays.asList(new String[] {"KG","MA","ER","LO","RH","PO"}).indexOf(rezToCopy.substring(0,2))  );
            // Lemmi 20110116: die ganze Zeile ist überflüssig, weil das vorab alles schon korrekt gesetzt worden ist !
            //                 wenn man die Zeile benutzt wird zudem der gewählte INDI-Schlüssel wieder gelöscht !
//            jcmb[cRKLASSE].setSelectedIndex( Arrays.asList(strRezepklassenAktiv).indexOf(strKopiervorlage.substring(0,2)) );


            /* Lemmi 20110116: Lieber Hr. Steinhilber: Die Leistungen werden bereits durch den obigen Aufruf von "ladeZusatzDatenAlt()" gesetzt.
            * Lemmi Frage: warum sollte das hier nochmals gemacht werden?
            if(!vec.get(cVAR_ARTDBEH1).equals("0")){
                jcmb[cLEIST1].setSelectedVecIndex(9, vec.get(cVAR_ARTDBEH1));//art_dbeh1
            }
            if(!vec.get(cVAR_ARTDBEH2).equals("0")){
                jcmb[cLEIST2].setSelectedVecIndex(9, vec.get(cVAR_ARTDBEH2));//art_dbeh2
            }
            if(!vec.get(cVAR_ARTDBEH3).equals("0")){
                jcmb[cLEIST3].setSelectedVecIndex(9, vec.get(cVAR_ARTDBEH3));//art_dbeh3
            }
            if(!vec.get(cVAR_ARTDBEH4).equals("0")){
                jcmb[cLEIST4].setSelectedVecIndex(9, vec.get(cVAR_ARTDBEH4));//art_dbeh4
            }
            */

            // vec wieder löschen - er hat seinen Transport-Dienst für das Kopieren geleistet
            vec.clear();
        }
    }  // end of doKopiereLetztesRezeptDesPatienten()

    private void doAbbrechen(){
        // Lemmi 20101231: Verhinderung von Datenverlust bei unbeabsichtigtem Zumachen des geänderten Rezept-Dialoges
        //Solche gravierenden Änderungen der Programmlogik dürfen erst dann eingeführt werden
        //wenn sich der Benutzer auf einer System-Init-Seite entscheiden kann ob er diese
        //Funktionalität will oder nicht
        //Wir im RTA wollen die Abfagerei definitiv nicht!
        //Wenn meine Damen einen Vorgang abbrechen wollen, dann wollen sie den Vorgang abbrechen
        //und nicht gefrag werden ob sie den Vorgang abbrechen wollen.
        //Steinhilber
        // Lemmi 20110116: Gerne auch mit Steuer-Parameter
        if( (Boolean)SystemConfig.hmRezeptDlgIni.get("RezAendAbbruchWarn")) {
            if ( HasChanged() && askForCancelUsaved() == JOptionPane.NO_OPTION )
                return;
        }

        aufraeumen();
        ((JXDialog)this.getParent().getParent().getParent().getParent().getParent()).dispose();
    }

    @Override
    public void rehaTPEventOccurred(RehaTPEvent evt) {
        try{
            if(evt.getDetails()[0] != null){
                if(evt.getDetails()[0].equals(this.getName())){
                    this.setVisible(false);
                    rtp.removeRehaTPEventListener((RehaTPEventListener) this);
                    rtp = null;
                    aufraeumen();
                }
            }
        }catch(NullPointerException ne){
            JOptionPane.showMessageDialog(null, "Fehler beim abhängen des Listeners Rezept-Neuanlage\n"+
                    "Bitte informieren Sie den Administrator über diese Fehlermeldung");
        }
    }
	public void closeDialog(){
		((JXDialog)this.getParent().getParent().getParent().getParent().getParent()).setVisible(false);
		((JXDialog)this.getParent().getParent().getParent().getParent().getParent()).dispose();
		setCursor(Cursors.normalCursor);
	}
    public void aufraeumen(){
        new SwingWorker<Void,Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                for(int i = 0; i < jtf.length;i++){
                    ListenerTools.removeListeners(jtf[i]);
                }
                for(int i = 0; i < jcb.length;i++){
                    ListenerTools.removeListeners(jcb[i]);
                }
                for(int i = 0; i < jcmb.length;i++){
                    ListenerTools.removeListeners(jcmb[i]);
                }
                ListenerTools.removeListeners(jta);
                ListenerTools.removeListeners(getInstance());
                if(rtp != null){
                    rtp.removeRehaTPEventListener((RehaTPEventListener) getInstance());
                    rtp = null;
                }
                return null;
            }
        }.execute();
    }
}
