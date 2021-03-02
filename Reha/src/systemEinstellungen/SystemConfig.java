package systemEinstellungen;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.DatFunk;
import CommonTools.FireRehaError;
import CommonTools.SqlInfo;
import CommonTools.ZeitFunk;
import CommonTools.ini.INITool;
import CommonTools.ini.Settings;
import crypt.Verschluesseln;
import environment.Path;
import hauptFenster.Reha;
import socketClients.SMSClient;
import stammDatenTools.RezTools;
import systemEinstellungen.config.Datenbank;

public class SystemConfig {

    public static Vector<ArrayList<String>> vSystemKollegen;
    public static Vector<String> vComboKollegen;
    public static String aktJahr = "";
    public static String vorJahr = "";
    public static Vector<String> vKalenderSet;
    public static Vector<Object> vKalenderFarben;
    public static ArrayList<String> aHauptFenster;
    //
    public static Vector<Vector<Color[]>> hmDefaultCols;
    public static Vector<Vector<Color[]>> vSysColsObject;
    public static Vector<String> vSysColsNamen;
    public static Vector<String> vSysColsBedeut;
    public static Vector<String> vSysColsCode;
    public static Vector<String> vSysDefNamen;

    public static Vector vSysColDlg;
    public static HashMap<String, Color[]> aktTkCol;
    public static boolean[] RoogleTage = { false, false, false, false, false, false, false };
    public static int RoogleZeitraum;
    public static HashMap<String, String> RoogleZeiten = null;

    public static boolean[] taskPaneCollapsed = { false, false, true, true, false, true, false };
    public static boolean AngelegtVonUser = false;
    public static boolean RezGebWarnung = true;

    public static ArrayList<ArrayList<ArrayList<String[]>>> aRoogleGruppen;

    public static String OpenOfficePfad = null;
    public static String OpenOfficeNativePfad = null;
    private static Settings termkalini;
    private static Settings colini;
    public static java.net.InetAddress dieseMaschine = null;
    public static String dieseCallbackIP = null;
    public static String PDFformularPfad;
    public static String wissenURL = null;
    public static String homeDir = null;
    public static String homePageURL = null;
    public static int TerminUeberlappung = 1;
    public static TerminListe oTerminListe = null;
    public static GruppenEinlesen oGruppen = null;

    public static HashMap<String, String> hmEmailExtern;
    public static HashMap<String, String> hmEmailIntern;
    public static HashMap<String, String> hmVerzeichnisse = null;
    public static HashMap<String, String> hmFirmenDaten = null;

    public static HashMap<String, Vector> hmDBMandant = null;
    public static Vector<String[]> Mandanten = null;
    public static Vector<String[]> DBTypen = null;
    public static int AuswahlImmerZeigen;
    public static int DefaultMandant;

    public static Vector<ArrayList<String>> InetSeiten = null;
    public static String HilfeServer = null;
    public static boolean HilfeServerIstDatenServer;
    public static HashMap<String, String> hmHilfeServer;

    public static HashMap<String, String> hmAdrKDaten = null;
    public static HashMap<String, String> hmAdrADaten = null;
    public static HashMap<String, String> hmAdrPDaten = null;
    public static HashMap<String, String> hmAdrRDaten = null;
    public static HashMap<String, String> hmAdrBDaten = null;
    public static HashMap<String, String> hmAdrAFRDaten = null;
    public static HashMap<String, String> hmAdrHMRDaten = null;
    public static HashMap<String, String> hmEBerichtDaten = new HashMap<String, String>();
    public static HashMap<String, String> hmRgkDaten = new HashMap<String, String>();

    public static HashMap<String, Integer> hmContainer = null;

    public static Vector<String> vPatMerker = null;
    public static Vector<ImageIcon> vPatMerkerIcon = null;
    public static Vector<String> vPatMerkerIconFile = null;

    public static HashMap<String, String> hmKVKDaten = null;
    public static String sReaderName = null;
    public static String sReaderAktiv = null;
    public static String sReaderCtApiLib = null;
    public static String sReaderDeviceID = null;
    public static String sDokuScanner = null;
    public static String sBarcodeScanner = null;
    public static String sBarcodeAktiv = null;
    public static String sBarcodeCom = null;
    public static boolean RsFtOhneKalender = false;
    public static String[] arztGruppen = null;
    public static String[] rezeptKlassen = null;
    public static Vector<Vector<String>> rezeptKlassenAktiv = null;
    public static boolean mitRs = false;
    public static String initRezeptKlasse = null;
    public static String rezGebVorlageNeu = null;
    public static String rezGebVorlageAlt = null;
    public static String rezGebVorlageHB = null;
    public static boolean rezGebDirektDruck = false;
    public static String rezGebDrucker = null;
    public static String rezBarcodeDrucker = null;
    public static HashMap<String, String> hmDokuScanner = null;
    public static HashMap<String, String[]> hmGeraete = null;

    public static HashMap<String, String> hmFremdProgs = null;
    public static Vector<Vector<String>> vFremdProgs = null;
    public static HashMap<String, String> hmCompany = null;

    public static String[] rezBarCodName = null;
    public static Vector<String> rezBarCodForm = null;

    public static HashMap<String, Vector<String>> hmTherapBausteine = null;

    public static String[] berichttitel = { null, null, null, null };
    public static String thberichtdatei = "";
   final public static ImageRepository hmSysIcons = new ImageRepository();

    public static Vector<String> vGutachtenEmpfaenger;
    public static Vector<String> vGutachtenIK;
    public static Vector<String> vGutachtenAbsAdresse;
    public static Vector<String> vGutachtenArzt;
    public static Vector<String> vGutachtenDisplay;
    public static String sGutachtenOrt;

    public static HashMap<String, String> hmAbrechnung = new HashMap<String, String>();
    public static Vector<String> vecTaxierung = new Vector<String>();

    public static HashMap<String, Object> hmPatientenWerkzeugDlgIni = new HashMap<String, Object>();

    // Lemmi 20101223 Steuerparanmeter für den Patienten-Suchen-Dialog
    public static HashMap<String, Integer> hmPatientenSuchenDlgIni = new HashMap<String, Integer>();

    // Lemmi 20110116 Steuerparanmeter für den Rezept-Dialog
    public static HashMap<String, Object> hmRezeptDlgIni = new HashMap<String, Object>();

    // Lemmi 20101224 Steuerparanmeter für RGR und AFR Behandlung in OffenePosten
    // und Mahnungen
    public static HashMap<String, Integer> hmZusatzInOffenPostenIni = new HashMap<String, Integer>();

    public static HashMap<String, Object> hmTerminBestaetigen = new HashMap<String, Object>();

    public static boolean desktopHorizontal = true;

    public static String dta301InBox = null;
    public static String dta301OutBox = null;

    public static long timerdelay = 600000;
    public static boolean timerpopup = true;
    public static boolean timerprogressbar = true;

    public static Vector<String> vFeiertage;

    public static String sWebCamActive = null;
    public static int[] sWebCamSize = { 320, 240 };

    public static HashMap<String, Object> hmArschgeigenModus = new HashMap<String, Object>();
    public static Vector<Vector<String>> vArschgeigenDaten = new Vector<Vector<String>>();

    public static boolean logVTermine = false;
    public static boolean logAlleTermine = false;

    public static HashMap<String, String> hmHmPraefix = new HashMap<String, String>();
    public static HashMap<String, String> hmHmPosIndex = new HashMap<String, String>();

    public static Vector<Vector<String>> vOwnDokuTemplate = new Vector<Vector<String>>();
    public static HashMap<String, String> hmDokuSortMode = new HashMap<String, String>();

    public static HashMap<String, Object> hmIcalSettings = new HashMap<String, Object>();

    public static int certState = 0;
    final public static int certOK = 0;
    final public static int certWillExpire = 1;
    final public static int certIsExpired = 2;
    final public static int certNotFound = 3;
    public static boolean certHash256 = false;

    public static HashMap<String, String> hmSMS = new HashMap<String, String>();
    public static boolean activateSMS = false;
    public static boolean phoneAvailable = false;

    public static boolean behdatumTippen = false;
    public static boolean useStornieren = false;
    public static boolean isAndi = false;

    public static boolean fullSizePwDialog = false;

    public static Vector<Vector<String>> vUserTasks = new Vector<Vector<String>>();
    private static final Logger logger = LoggerFactory.getLogger(SystemConfig.class);

    public SystemConfig() {

    }

    public void SystemStart(String homedir) {
        termkalini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "rehajava.ini");
        PDFformularPfad = termkalini.getStringProperty("Formulare", "PDFFormularPfad");
        try {
            dieseMaschine = java.net.InetAddress.getLocalHost();
        } catch (java.net.UnknownHostException uhe) {

        }

    }

    public void SystemInit(int i) {
        switch (i) {
        case 1:

            break;
        case 2:

            break;
        case 3:
            TerminKalender();
            break;
        case 4:
            EmailParameter();
            break;
        case 5:
            // EmailParameter();
            break;
        case 6:
            Verzeichnisse();
            break;
        case 7:
            RoogleGruppen();
            break;
//        case 8:
//            NurSets();
//            break;
        case 9:
            try {
                TKFarben();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            break;
        case 10:
            GruppenLesen();
            break;
        case 11:
            MandantenEinlesen();
            break;

        }
        return;
    }

    public void DatenBank() {
        try {
            termkalini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "rehajava.ini");
            new Datenbank().datenbankEinstellungeneinlesen(termkalini);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der rehajava.ini, Methode: Datenbank!\nFehlertext: "
                            + ex.getMessage());
            ex.printStackTrace();
        }
        /*********************************/

        return;
    }

    public void phoneservice() {
        try {
            File f = new File(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "phoneservice.ini");
            if (f.exists()) {
                Settings phservice = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/",
                        "phoneservice.ini");
                if (phservice.getStringProperty("SMSDienste", "SmartPhoneSms") != null) {
                    try {
                        hmSMS.put("SMS", phservice.getStringProperty("SMSDienste", "SmartPhoneSms"));
                        hmSMS.put("DIAL", phservice.getStringProperty("SMSDienste", "SmartPhoneDialer"));
                        hmSMS.put("IP", phservice.getStringProperty("SMSDienste", "SmartPhoneIP"));
                        hmSMS.put("NAME", phservice.getStringProperty("SMSDienste", "SmartPhoneName"));
                        hmSMS.put("PORT", phservice.getStringProperty("SMSDienste", "SmartPhonePort"));
                        hmSMS.put("COMM", phservice.getStringProperty("SMSDienste", "TheraPiSMSPort"));
                        activateSMS = true;
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    boolean loopback = false;
                                    boolean sitelocal = false;
                                    boolean reachable = false;

                                    Enumeration<NetworkInterface> netInter = NetworkInterface.getNetworkInterfaces();

                                    while (netInter.hasMoreElements()) {
                                        NetworkInterface ni = netInter.nextElement();
                                        if (ni != null) {

                                            for (InetAddress iaddress : Collections.list(ni.getInetAddresses())) {
                                                loopback = iaddress.isLoopbackAddress();
                                                sitelocal = iaddress.isSiteLocalAddress();
                                                reachable = InetAddress.getByName(iaddress.getHostAddress())
                                                                       .isReachable(2000);
                                                if (!loopback && sitelocal && reachable) {
                                                    dieseCallbackIP = iaddress.getHostAddress();
                                                    if (dieseCallbackIP.toString()
                                                                       .contains(hmSMS.get("IP")
                                                                                      .substring(0, hmSMS.get("IP")
                                                                                                         .lastIndexOf(
                                                                                                                 ".")))) {
                                                        logger.info("Callback-IP: " + dieseCallbackIP);
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }

                                } catch (SocketException e) {
                                    e.printStackTrace();
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    new SMSClient().setzeNachricht("PHONE-CONNECTABLE");
                                } catch (Exception ex) {

                                }

                                return;

                            }
                        }.start();

                        logger.info(hmSMS.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    logger.info("Dienste Autodialer/SMS nicht verfügbar");
                }

            } else {
                logger.info("SmartPhone-Service ist nicht installiert");
            }
            /********************************************************/
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der phoneserver.ini!\nFehlertext: " + ex.getMessage());
        }
    }

    public void HauptFenster() {
        try {
            termkalini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "rehajava.ini");
            boolean mustsave = false;
            aHauptFenster = new ArrayList<String>();
            aHauptFenster.add(termkalini.getStringProperty("HauptFenster", "Hintergrundbild"));
            aHauptFenster.add(termkalini.getStringProperty("HauptFenster", "Bildgroesse"));
            aHauptFenster.add(termkalini.getStringProperty("HauptFenster", "FensterFarbeRGB"));
            aHauptFenster.add(termkalini.getStringProperty("HauptFenster", "FensterTitel"));
            aHauptFenster.add(termkalini.getStringProperty("HauptFenster", "LookAndFeel"));

            if (termkalini.getStringProperty("HauptFenster", "HorizontalTeilen") == null) {
                termkalini.setStringProperty("HauptFenster", "HorizontalTeilen", "1", null);
                mustsave = true;

            } else {
                desktopHorizontal = (termkalini.getIntegerProperty("HauptFenster", "HorizontalTeilen") == 1 ? true : false);
            }
            if (termkalini.getStringProperty("HauptFenster", "TP1Offen") == null) {
                termkalini.setStringProperty("HauptFenster", "TP1Offen", "0", null);
                termkalini.setStringProperty("HauptFenster", "TP2Offen", "0", null);
                termkalini.setStringProperty("HauptFenster", "TP3Offen", "1", null);
                termkalini.setStringProperty("HauptFenster", "TP4Offen", "1", null);
                termkalini.setStringProperty("HauptFenster", "TP5Offen", "0", null);
                termkalini.setStringProperty("HauptFenster", "TP6Offen", "1", null);
                termkalini.setStringProperty("HauptFenster", "TP7Offen", "1", null);
                mustsave = true;
            } else {
                if (termkalini.getStringProperty("HauptFenster", "TP7Offen") == null) {
                    termkalini.setStringProperty("HauptFenster", "TP7Offen", "1", null);
                    mustsave = true;
                }
                for (int i = 1; i < 8; i++) {
                    taskPaneCollapsed[i
                            - 1] = (termkalini.getStringProperty("HauptFenster", "TP" + Integer.toString(i) + "Offen")
                                       .equals("1") ? true : false);
                }
            }
            if (mustsave) {
                INITool.saveIni(termkalini);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der rehajava.ini, Mehode:HauptFenster!\nFehlertext: "
                            + ex.getMessage());
            ex.printStackTrace();
        }
        return;
    }

    public void openoffice() {
        OpenOfficePfad = termkalini.getStringProperty("OpenOffice.org", "OfficePfad");
        if (!new File(OpenOfficePfad).exists()) {
            String meldung = "Es konnte keine gültige OpenOffice-Installation entdeckt werden\n"
                    + "Bislang zeigt der Pfad auf OO.org auf " + OpenOfficePfad + "\n\n"
                    + "Öffnen Sie bitte in Thera-Pi die System-Initialisierung und\n"
                    + "gehen Sie dann auf die Seite -> sonsige Einstellungen -> Fremdprogramme.\n\n"
                    + "Stellen Sie dann bitte auf dieser Seite den Pfad zu OpenOffice.org ein\n\n"
                    + "Auf der selben Seite stellen Sie dann bitte Ihren PDF-Reader ein\n\n"
                    + "Abschließend beenden Sie bitte Thera-Pi und starten Sie dann Thera-Pi erneut\n\n"
                    + "Everything should then be fine - wie der Schwabe zu sagen pflegt!";
            JOptionPane.showMessageDialog(null, meldung);
        }
        OpenOfficeNativePfad = termkalini.getStringProperty("OpenOffice.org", "OfficeNativePfad");

        termkalini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "nachrichten.ini");
        timerdelay = termkalini.getLongProperty("RehaNachrichten", "NachrichtenTimer");
        timerpopup = (termkalini.getIntegerProperty("RehaNachrichten", "NachrichtenPopUp") <= 0 ? false : true);
        timerprogressbar = (termkalini.getIntegerProperty("RehaNachrichten", "NachrichtenProgressbar") <= 0 ? false : true);

        wissenURL = termkalini.getStringProperty("WWW-Services", "RTA-Wissen");
        homePageURL = termkalini.getStringProperty("WWW-Services", "HomePage");
        homeDir = Path.Instance.getProghome();
    }

    private void TerminKalender() {
        try {
            Settings termkalini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/",
                    "terminkalender.ini");
           BehandlerSets.laden(termkalini);
            TKSettings.KalenderUmfang[0] = String.valueOf(termkalini.getStringProperty("Kalender", "KalenderStart"));
            TKSettings.KalenderUmfang[1] = String.valueOf(termkalini.getStringProperty("Kalender", "KalenderEnde"));
            TKSettings.KalenderMilli[0] = ZeitFunk.MinutenSeitMitternacht(TKSettings.KalenderUmfang[0]);
            TKSettings.KalenderMilli[1] = ZeitFunk.MinutenSeitMitternacht(TKSettings.KalenderUmfang[1]);
            TKSettings.KalenderBarcode = (termkalini.getStringProperty("Kalender", "KalenderBarcode")
                                         .trim()
                                         .equals("0") ? false : true);
            TKSettings.UpdateIntervall = Integer.valueOf(
                    String.valueOf(termkalini.getStringProperty("Kalender", "KalenderTimer")));
            String s = String.valueOf(termkalini.getStringProperty("Kalender", "KalenderHintergrundRGB"));
            String[] ss = s.split(",");
            TKSettings.KalenderHintergrund = new Color(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]), Integer.parseInt(ss[2]));
            TKSettings.KalenderAlpha = new Float(
                    String.valueOf(termkalini.getStringProperty("Kalender", "KalenderHintergrundAlpha")));
            oTerminListe = new TerminListe().init();
            Reha.instance.setzeInitStand("Gruppendefinition einlesen");
            GruppenLesen();
            try {
                termkalini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "kalender.ini");
                TKSettings.KalenderLangesMenue = (termkalini.getStringProperty("Kalender", "LangesMenue")
                                          .trim()
                                          .equals("0") ? false : true);
                TKSettings.KalenderStartWochenAnsicht = (termkalini.getStringProperty("Kalender", "StartWochenAnsicht")
                                                 .trim()
                                                 .equals("0") ? false : true);
                TKSettings.KalenderStartWADefaultUser = (termkalini.getStringProperty("Kalender", "AnsichtDefault")
                                                 .split("@")[0]);
                TKSettings.defaultBehandlerSet = (termkalini.getStringProperty("Kalender", "AnsichtDefault")
                                                .split("@")[1]);
                TKSettings.KalenderZeitLabelZeigen = (termkalini.getStringProperty("Kalender", "ZeitLabelZeigen")
                                              .trim()
                                              .equals("0") ? false : true);
                if (termkalini.getStringProperty("Kalender", "ZeitLinieZeigen") == null) {
                    termkalini.setStringProperty("Kalender", "ZeitLinieZeigen", "0", null);
                    INITool.saveIni(termkalini);
                    TKSettings.KalenderTimeLineZeigen = false;
                } else {
                    TKSettings.KalenderTimeLineZeigen = (termkalini.getStringProperty("Kalender", "ZeitLinieZeigen")
                                                 .trim()
                                                 .equals("0") ? false : true);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Fehler bei der Verarbeitung der kalender.ini, Mehode:TerminKalender!\nFehlertext: "
                                + ex.getMessage());
                ex.printStackTrace();
                new FireRehaError(this, "SystemConfig.Terminkalender()",
                        new String[] { "kalender.ini einlesen", ex.getMessage() });

            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der terminkalender.ini, Mehode:TerminKalender!\nFehlertext: "
                            + ex.getMessage());
        }

        return;
    }


    public static void RoogleGruppen() {
        try {
            Settings roogleini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/",
                    "terminkalender.ini");
            aRoogleGruppen = new ArrayList<ArrayList<ArrayList<String[]>>>();
            int lesen, i;
            lesen = Integer.parseInt(
                    String.valueOf(roogleini.getStringProperty("RoogleEinstellungen", "RoogleAnzahlGruppen")));
            ArrayList<String> aList1 = new ArrayList<String>();
            ArrayList<String[]> aList2 = new ArrayList<String[]>();
            ArrayList<ArrayList<ArrayList<String[]>>> aList3 = new ArrayList<ArrayList<ArrayList<String[]>>>();

            for (i = 1; i <= lesen; i++) {
                aList1.add(String.valueOf(roogleini.getStringProperty("RoogleEinstellungen", "RoogleNameGruppen" + i)));
                aList2.add(String.valueOf(roogleini.getStringProperty("RoogleEinstellungen", "RoogleFelderGruppen" + i))
                                 .split(","));
                aList3.add((ArrayList) aList1.clone());
                aList3.add((ArrayList) aList2.clone());
                aRoogleGruppen.add((ArrayList) aList3.clone());
                aList1.clear();
                aList2.clear();
                aList3.clear();
            }
            for (i = 0; i < 7; i++) {
                RoogleTage[i] = (roogleini.getStringProperty("RoogleEinstellungen", "Tag" + (i + 1))
                                          .trim()
                                          .equals("0") ? false : true);
            }
            RoogleZeitraum = Integer.valueOf(roogleini.getStringProperty("RoogleEinstellungen", "Zeitraum"));
            RoogleZeiten = new HashMap<String, String>();
            RoogleZeiten.put("KG", roogleini.getStringProperty("RoogleEinstellungen", "KG"));
            RoogleZeiten.put("MA", roogleini.getStringProperty("RoogleEinstellungen", "MA"));
            RoogleZeiten.put("ER", roogleini.getStringProperty("RoogleEinstellungen", "ER"));
            RoogleZeiten.put("LO", roogleini.getStringProperty("RoogleEinstellungen", "LO"));
            RoogleZeiten.put("SP", roogleini.getStringProperty("RoogleEinstellungen", "SP"));
        } catch (Exception es) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der terminkalender.ini, Mehode:RoogleGruppen!\nFehlertext: "
                            + es.getMessage());
        }
    }

    private void EmailParameter() {
        try {
            boolean mustsave = false;
            Settings emailini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/",
                    "email.ini");
            hmEmailExtern = new HashMap<String, String>();
            hmEmailExtern.put("SmtpHost", emailini.getStringProperty("EmailExtern", "SmtpHost"));
            hmEmailExtern.put("SmtpAuth", emailini.getStringProperty("EmailExtern", "SmtpAuth"));
            hmEmailExtern.put("Pop3Host", emailini.getStringProperty("EmailExtern", "Pop3Host"));
            hmEmailExtern.put("Username", emailini.getStringProperty("EmailExtern", "Username"));
            String pw = emailini.getStringProperty("EmailExtern", "Password");
            Verschluesseln man = Verschluesseln.getInstance();
            String decrypted = man.decrypt(pw);
            hmEmailExtern.put("Password", decrypted);
            hmEmailExtern.put("SenderAdresse", emailini.getStringProperty("EmailExtern", "SenderAdresse"));
            hmEmailExtern.put("Bestaetigen", emailini.getStringProperty("EmailExtern", "EmpfangBestaetigen"));
            if (emailini.getStringProperty("EmailExtern", "SmtpSecure") == null) {
                hmEmailExtern.put("SmtpSecure", "keine");
                emailini.setStringProperty("EmailExtern", "SmtpSecure", "keine", null);
                mustsave = true;
            } else {
                hmEmailExtern.put("SmtpSecure", emailini.getStringProperty("EmailExtern", "SmtpSecure"));
            }
            if (emailini.getStringProperty("EmailExtern", "SmtpPort") == null) {
                if (hmEmailExtern.get("SmtpSecure")
                                 .equals("keine")) {
                    hmEmailExtern.put("SmtpPort", "25");
                    emailini.setStringProperty("EmailExtern", "SmtpPort", "25", null);
                } else if (hmEmailExtern.get("SmtpSecure")
                                        .equals("TLS/STARTTLS")) {
                    hmEmailExtern.put("SmtpPort", "587");
                    emailini.setStringProperty("EmailExtern", "SmtpPort", "587", null);
                } else if (hmEmailExtern.get("SmtpSecure")
                                        .equals("SSL")) {
                    hmEmailExtern.put("SmtpPort", "465");
                    emailini.setStringProperty("EmailExtern", "SmtpPort", "465", null);
                }
                mustsave = true;
            } else {
                hmEmailExtern.put("SmtpPort", emailini.getStringProperty("EmailExtern", "SmtpPort"));
            }
            /********************/
            hmEmailIntern = new HashMap<String, String>();
            hmEmailIntern.put("SmtpHost", emailini.getStringProperty("EmailIntern", "SmtpHost"));
            hmEmailIntern.put("SmtpAuth", emailini.getStringProperty("EmailIntern", "SmtpAuth"));
            hmEmailIntern.put("Pop3Host", emailini.getStringProperty("EmailIntern", "Pop3Host"));
            hmEmailIntern.put("Username", emailini.getStringProperty("EmailIntern", "Username"));
            pw = emailini.getStringProperty("EmailIntern", "Password");
            man = Verschluesseln.getInstance();
            decrypted = man.decrypt(pw);
            hmEmailIntern.put("Password", decrypted);
            hmEmailIntern.put("SenderAdresse", emailini.getStringProperty("EmailIntern", "SenderAdresse"));
            hmEmailIntern.put("Bestaetigen", emailini.getStringProperty("EmailIntern", "EmpfangBestaetigen"));

            if (emailini.getStringProperty("EmailIntern", "SmtpSecure") == null) {
                hmEmailIntern.put("SmtpSecure", "keine");
                emailini.setStringProperty("EmailIntern", "SmtpSecure", "keine", null);
                mustsave = true;
            } else {
                hmEmailIntern.put("SmtpSecure", emailini.getStringProperty("EmailIntern", "SmtpSecure"));
            }
            if (emailini.getStringProperty("EmailIntern", "SmtpPort") == null) {
                if (hmEmailIntern.get("SmtpSecure")
                                 .equals("keine")) {
                    hmEmailIntern.put("SmtpPort", "25");
                    emailini.setStringProperty("EmailIntern", "SmtpPort", "25", null);
                } else if (hmEmailIntern.get("SmtpSecure")
                                        .equals("TLS/STARTTLS")) {
                    hmEmailIntern.put("SmtpPort", "587");
                    emailini.setStringProperty("EmailIntern", "SmtpPort", "587", null);
                } else if (hmEmailIntern.get("SmtpSecure")
                                        .equals("SSL")) {
                    hmEmailIntern.put("SmtpPort", "465");
                    emailini.setStringProperty("EmailIntern", "SmtpPort", "465", null);
                }
                mustsave = true;
            } else {
                hmEmailIntern.put("SmtpPort", emailini.getStringProperty("EmailIntern", "SmtpPort"));
            }
            if (mustsave) {
                INITool.saveIni(emailini);
            }

            if (new File(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/dta301.ini").exists()) {
                Settings dtaini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/",
                        "dta301.ini");
                dta301InBox = dtaini.getStringProperty("DatenPfade301", "inbox");
                dta301OutBox = dtaini.getStringProperty("DatenPfade301", "outbox");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der email.ini, Mehode:EmailParameter!\nFehlertext: "
                            + ex.getMessage());
        }

    }

    public static void IcalSettings() {
        try {
            Settings icalini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/",
                    "icalendar.ini");
            hmIcalSettings.put("warnen", icalini.getStringProperty("ICalendar", "Warnen")
                                                .equals("0") ? false : true);
            hmIcalSettings.put("warnzeitpunkt", icalini.getStringProperty("ICalendar", "Warnzeitpunkt"));
            hmIcalSettings.put("organisatorname", icalini.getStringProperty("ICalendar", "Organisatorname"));
            hmIcalSettings.put("organisatoremail", icalini.getStringProperty("ICalendar", "Organisatoremail"));
            hmIcalSettings.put("praefix", icalini.getStringProperty("ICalendar", "Praefixvortermin"));
            hmIcalSettings.put("aufeigeneemail", icalini.getStringProperty("ICalendar", "Aufeigeneemail")
                                                        .equals("0") ? false : true);
            hmIcalSettings.put("praefixbeireha", icalini.getStringProperty("ICalendar", "Praefixbeireha")
                                                        .equals("0") ? false : true);
            hmIcalSettings.put("warnenbeireha", icalini.getStringProperty("ICalendar", "Warnenbeireha")
                                                       .equals("0") ? false : true);
            hmIcalSettings.put("rehaplanverzeichnis", icalini.getStringProperty("ICalendar", "Rehaplanverzeichnis"));
            if (icalini.getStringProperty("ICalendar", "Direktsenden") == null) {
                hmIcalSettings.put("direktsenden", false);
            } else {
                hmIcalSettings.put("direktsenden", icalini.getStringProperty("ICalendar", "Direktsenden")
                                                          .equals("0") ? false : true);
            }
            if (icalini.getStringProperty("ICalendar", "Postfach") == null) {
                hmIcalSettings.put("postfach", 0);
            } else {
                hmIcalSettings.put("postfach", Integer.parseInt(icalini.getStringProperty("ICalendar", "Postfach")));
            }
            if (icalini.getStringProperty("ICalendar", "Pdfbeilegen") == null) {
                hmIcalSettings.put("pdfbeilegen", false);
            } else {
                hmIcalSettings.put("pdfbeilegen", icalini.getStringProperty("ICalendar", "Pdfbeilegen")
                                                         .equals("0") ? false : true);
            }
            int zeilen = Integer.parseInt(icalini.getStringProperty("Terminbeschreibung", "TextzeilenAnzahl"));
            String beschreibung = "";
            for (int i = 0; i < zeilen; i++) {
                beschreibung = beschreibung
                        + icalini.getStringProperty("Terminbeschreibung", "Textzeile" + Integer.toString(i + 1))
                        + (i < (zeilen - 1) ? "\n" : "");
            }
            hmIcalSettings.put("beschreibung", String.valueOf(beschreibung));
            zeilen = Integer.parseInt(icalini.getStringProperty("Emailtext", "TextzeilenAnzahl"));
            beschreibung = "";
            for (int i = 0; i < zeilen; i++) {
                beschreibung = beschreibung
                        + icalini.getStringProperty("Emailtext", "Textzeile" + Integer.toString(i + 1))
                        + (i < (zeilen - 1) ? "\n" : "");
            }
            hmIcalSettings.put("emailtext", String.valueOf(beschreibung));
            hmIcalSettings.put("betreff", icalini.getStringProperty("Emailtext", "Betreff"));
            // ICalGenerator.setUtcTime("20151022T113000");
            // ICalGenerator.setUtcTime("20151029T113000");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der icalendar.ini, Mehode:ICalSettingns!\nFehlertext: "
                            + ex.getMessage());
        }
    }

    private void Verzeichnisse() {
        try {
            termkalini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "rehajava.ini");
            hmVerzeichnisse = new HashMap<String, String>();
            hmVerzeichnisse.put("Programmverzeichnis", String.valueOf(Path.Instance.getProghome()));
            hmVerzeichnisse.put("Vorlagen",
                    String.valueOf(Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK()));
            hmVerzeichnisse.put("Icons", String.valueOf(Path.Instance.getProghome() + "icons"));
            hmVerzeichnisse.put("Temp", String.valueOf(Path.Instance.getProghome() + "temp/" + Reha.getAktIK()));
            hmVerzeichnisse.put("Ini", String.valueOf(Path.Instance.getProghome() + "ini/" + Reha.getAktIK()));
            hmVerzeichnisse.put("Rehaplaner", termkalini.getStringProperty("Verzeichnisse", "Rehaplaner"));
            hmVerzeichnisse.put("Fahrdienstliste", termkalini.getStringProperty("Verzeichnisse", "Fahrdienstliste"));
            hmVerzeichnisse.put("Fahrdienstrohdatei", termkalini.getStringProperty("Verzeichnisse", "Fahrdienstrohdatei"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der rehajava.ini, Mehode:Verzeichnisse!\nFehlertext: "
                            + ex.getMessage());
        }
    }

    private void TKFarben() {
        try {
            if (colini == null) {
                colini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "color.ini");
            }
            int anz = Integer.valueOf(String.valueOf(colini.getStringProperty("Terminkalender", "FarbenAnzahl")));
            vSysColsNamen = new Vector<String>();
            vSysColsBedeut = new Vector<String>();
            vSysColsCode = new Vector<String>();
            // ArrayList<String> colnames = new ArrayList<String>();
            for (int i = 0; i < anz; i++) {
                vSysColsNamen.add(String.valueOf(colini.getStringProperty("Terminkalender", "FarbenNamen" + (i + 1))));
                vSysColsBedeut.add(
                        String.valueOf(colini.getStringProperty("Terminkalender", "FarbenBedeutung" + (i + 1))));
                vSysColsCode.add(String.valueOf(colini.getStringProperty("Terminkalender", "FarbenCode" + (i + 1))));
                // colnames.add(String.valueOf(colini.getStringProperty("Terminkalender","FarbenNamen")));
            }
            int def = Integer.valueOf(String.valueOf(colini.getStringProperty("Terminkalender", "FarbenDefaults")));
            vSysDefNamen = new Vector<String>();
            // ArrayList<String> defnames = new ArrayList<String>();
            for (int i = 0; i < def; i++) {
                vSysDefNamen.add(
                        String.valueOf(colini.getStringProperty("Terminkalender", "FarbenDefaultNamen" + (i + 1))));
                // defnames.add(String.valueOf(colini.getStringProperty("Terminkalender","FarbenDefaultNamen")));
            }


            vSysColsObject = new Vector<Vector<Color[]>>();

            // Zuerst die Userfarben
            Vector<Color[]> colv = new Vector<Color[]>();
            for (int j = 0; j < anz; j++) {

                String[] farb = String.valueOf(colini.getStringProperty("UserFarben", vSysColsNamen.get(j)))
                                      .split(",");
                Color[] farbe = new Color[2];
                farbe[0] = new Color(Integer.valueOf(farb[0]), Integer.valueOf(farb[1]), Integer.valueOf(farb[2]));
                farbe[1] = new Color(Integer.valueOf(farb[3]), Integer.valueOf(farb[4]), Integer.valueOf(farb[5]));
                colv.add(farbe);
            }
            vSysColsObject.add((Vector<Color[]>) colv.clone());

            for (int i = 0; i < def; i++) {
                // Anzahl der Sets
                colv = new Vector<Color[]>();
                for (int j = 0; j < anz; j++) {
                    String[] farb = String.valueOf(colini.getStringProperty(vSysDefNamen.get(i), vSysColsNamen.get(j)))
                                          .split(",");
                    Color[] farbe = new Color[2];
                    farbe[0] = new Color(Integer.valueOf(farb[0]), Integer.valueOf(farb[1]), Integer.valueOf(farb[2]));
                    farbe[1] = new Color(Integer.valueOf(farb[3]), Integer.valueOf(farb[4]), Integer.valueOf(farb[5]));
                    colv.add(farbe);
                }
                vSysColsObject.add((Vector<Color[]>) colv.clone());
            }

            JLabel BeispielDummi = new JLabel("so sieht's aus");
            int i, lang;
            lang = SystemConfig.vSysColsNamen.size();
            vSysColDlg = new Vector();
            for (i = 0; i < lang; i++) {
                Vector ovec = new Vector();
                ovec.add(SystemConfig.vSysColsCode.get(i));
                ovec.add(SystemConfig.vSysColsBedeut.get(i));
                ovec.add(SystemConfig.vSysColsObject.get(0)
                                                    .get(i)[0]);
                ovec.add(SystemConfig.vSysColsObject.get(0)
                                                    .get(i)[1]);
                ovec.add(BeispielDummi);
                vSysColDlg.add(ovec.clone());
            }
            aktTkCol = new HashMap<String, Color[]>();
            for (i = 0; i < lang; i++) {
                aktTkCol.put(vSysColsNamen.get(i), new Color[] { SystemConfig.vSysColsObject.get(0)
                                                                                            .get(i)[0],
                        SystemConfig.vSysColsObject.get(0)
                                                   .get(i)[1] });

            }
            TKSettings.KalenderHintergrund = aktTkCol.get("AusserAZ")[0];
        } catch (Exception ex) {
            logger .error("color ini konnte nicht gelesen werden ", ex);
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der color.ini, Mehode:TKFarben!\nFehlertext: " + ex.getMessage());
        }

    }

    public static String getLookAndFeel() {
        return aHauptFenster.get(4);
    }

    public static void MandantenEinlesen() {
        try {
            Settings inif = INITool.openIni(Path.Instance.getProghome() + "ini/", "mandanten.ini");
            int AnzahlMandanten = inif.getIntegerProperty("TheraPiMandanten", "AnzahlMandanten");
            AuswahlImmerZeigen = inif.getIntegerProperty("TheraPiMandanten", "AuswahlImmerZeigen");
            DefaultMandant = inif.getIntegerProperty("TheraPiMandanten", "DefaultMandant");
            // int LetzterMandant = inif.getIntegerProperty("TheraPiMandanten",
            // "LetzterMandant");
            Mandanten = new Vector<String[]>();
            for (int i = 0; i < AnzahlMandanten; i++) {
                String[] mand = { null, null };
                mand[0] = String.valueOf(inif.getStringProperty("TheraPiMandanten", "MAND-IK" + (i + 1)));
                mand[1] = String.valueOf(inif.getStringProperty("TheraPiMandanten", "MAND-NAME" + (i + 1)));
                Mandanten.add(mand.clone());
            }
            hmDBMandant = new HashMap<String, Vector>();
            for (int i = 0; i < AnzahlMandanten; i++) {
                Settings minif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Mandanten.get(i)[0] + "/",
                        "rehajava.ini");
                Vector<String> mandantDB = new Vector<String>();
                mandantDB.add(minif.getStringProperty("DatenBank", "DBType1"));
                mandantDB.add(minif.getStringProperty("DatenBank", "DBTreiber1"));
                mandantDB.add(minif.getStringProperty("DatenBank", "DBServer1"));
                mandantDB.add(minif.getStringProperty("DatenBank", "DBPort1"));
                mandantDB.add(minif.getStringProperty("DatenBank", "DBName1"));
                mandantDB.add(minif.getStringProperty("DatenBank", "DBBenutzer1"));
                String pw = minif.getStringProperty("DatenBank", "DBPasswort1");
                String decrypted = null;
                if (pw != null) {
                    Verschluesseln man = Verschluesseln.getInstance();
                    decrypted = man.decrypt(pw);
                } else {
                    decrypted = String.valueOf("");
                }
                mandantDB.add(String.valueOf(decrypted));
                hmDBMandant.put(Mandanten.get(i)[1], (Vector<String>) mandantDB.clone());
            }

            Settings dbtini = INITool.openIni(Path.Instance.getProghome() + "ini/", "dbtypen.ini");
            int itypen = dbtini.getIntegerProperty("Datenbanktypen", "TypenAnzahl");
            DBTypen = new Vector<String[]>();
            String[] typen = new String[] { null, null, null };
            for (int i = 0; i < itypen; i++) {
                typen[0] = dbtini.getStringProperty("Datenbanktypen", "Typ" + (i + 1) + "Typ");
                typen[1] = dbtini.getStringProperty("Datenbanktypen", "Typ" + (i + 1) + "Treiber");
                typen[2] = dbtini.getStringProperty("Datenbanktypen", "Typ" + (i + 1) + "Port");
                DBTypen.add(typen.clone());
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der mandanten.ini, Mehode:MandantenEinlesen!\nFehlertext: "
                            + ex.getMessage());
        }
    }

    public static void GleicheDBMandanten(String sik) {

    }

    public static void InetSeitenEinlesen() {
        try {
            Settings inif = INITool.openIni(Path.Instance.getProghome() + "ini/", "rehabrowser.ini");
            int seitenanzahl = inif.getIntegerProperty("RehaBrowser", "SeitenAnzahl");
            InetSeiten = new Vector<ArrayList<String>>();
            ArrayList<String> seite = null;
            for (int i = 0; i < seitenanzahl; i++) {
                seite = new ArrayList<String>();
                seite.add(inif.getStringProperty("RehaBrowser", "SeitenName" + (i + 1)));
                seite.add(inif.getStringProperty("RehaBrowser", "SeitenIcon" + (i + 1)));
                seite.add(inif.getStringProperty("RehaBrowser", "SeitenAdresse" + (i + 1)));
                InetSeiten.add(seite);
            }
            HilfeServer = inif.getStringProperty("TheraPiHilfe", "HilfeServer");
            HilfeServerIstDatenServer = (inif.getIntegerProperty("TheraPiHilfe", "HilfeDBIstDatenDB") > 0 ? true
                    : false);
            if (!HilfeServerIstDatenServer) {
                hmHilfeServer = new HashMap<String, String>();
                hmHilfeServer.put("HilfeDBTreiber", inif.getStringProperty("TheraPiHilfe", "HilfeDBTreiber"));
                hmHilfeServer.put("HilfeDBLogin", inif.getStringProperty("TheraPiHilfe", "HilfeDBLogin"));
                hmHilfeServer.put("HilfeDBUser", inif.getStringProperty("TheraPiHilfe", "HilfeDBUser"));
                String pw = String.valueOf(inif.getStringProperty("TheraPiHilfe", "HilfeDBPassword"));
                Verschluesseln man = Verschluesseln.getInstance();
                String decrypted = man.decrypt(pw);
                hmHilfeServer.put("HilfeDBPassword", decrypted);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der rehabrowser.ini, Mehode:InetSeitenEinlesen!\nFehlertext: "
                            + ex.getMessage());
        }
    }

    public static void UpdateIni(String inidatei, String gruppe, String element, String wert) {
        Settings updateini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", inidatei);
        updateini.setStringProperty(gruppe, element, wert, null);
        INITool.saveIni(updateini);
    }

    public static void UpdateIni(Settings inidatei, String gruppe, String element, Object wert, String hinweis) {
        if (wert instanceof java.lang.String) {
            inidatei.setStringProperty(gruppe, element, (String) wert, hinweis);
        } else if (wert instanceof java.lang.Integer) {
            inidatei.setIntegerProperty(gruppe, element, (Integer) wert, hinweis);
        }
        INITool.saveIni(inidatei);
    }

    public static void GruppenLesen() {
        oGruppen = new GruppenEinlesen().init();
    }

    public static void HashMapsVorbereiten() {
        /********************/
        hmAdrKDaten = new HashMap<String, String>();
        List<String> lAdrKDaten = Arrays.asList(new String[] { "<Kadr1>", "<Kadr2>", "<Kadr3>", "<Kadr4>", "<Kadr5>",
                "<Ktel>", "<Kfax>", "<Kemail>", "<Kid>" });
        for (int i = 0; i < lAdrKDaten.size(); i++) {
            hmAdrKDaten.put(lAdrKDaten.get(i), "");
        }
        /********************/
        hmAdrADaten = new HashMap<String, String>();
        List<String> lAdrADaten = Arrays.asList(new String[] { "<Aadr1>", "<Aadr2>", "<Aadr3>", "<Aadr4>", "<Aadr5>",
                "<Atel>", "<Afax>", "<Aemail>", "<Aid>", "<Aihrer>", "<Apatientin>", "<Adie>" });
        for (int i = 0; i < lAdrADaten.size(); i++) {
            hmAdrADaten.put(lAdrADaten.get(i), "");
        }
        /********************/
        hmAdrPDaten = new HashMap<String, String>();
        List<String> lAdrPDaten = Arrays.asList(
                new String[] { "<Padr1>", "<Padr2>", "<Padr3>", "<Padr4>", "<Padr5>", "<Pgeboren>", "<Panrede>",
                        "<Pnname>", "<Pvname>", "<Pbanrede>", "<Ptelp>", "<Ptelg>", "<Ptelmob>", "<Pfax>", "<Pemail>",
                        "<Ptitel>", "<Pihrem>", "<Pihnen>", "<Pid>", "<Palter>", "<Pzigsten>", "<Pvnummer>" });
        for (int i = 0; i < lAdrPDaten.size(); i++) {
            hmAdrPDaten.put(lAdrPDaten.get(i), "");
        }
        /********************/
        hmAdrRDaten = new HashMap<String, String>();
        List<String> lAdrRDaten = Arrays.asList(new String[] { "<Rpatid>", "<Rnummer>", "<Rdatum>", "<Rposition1>",
                "<Rposition2>", "<Rposition3>", "<Rposition4>", "<Rpreis1>", "<Rpreis2>", "<Rpreis3>", "<Rpreis4>",
                "<Rproz1>", "<Rproz2>", "<Rproz3>", "<Rproz4>", "<Rgesamt1>", "<Rgesamt2>", "<Rgesamt3>", "<Rgesamt4>",
                "<Rpauschale>", "<Rendbetrag>", "<Ranzahl1>", "<Ranzahl2>", "<Ranzahl3>", "<Ranzahl4>", "<Rerstdat>",
                "<Rletztdat>", "<Rid>", "<Rtage>", "<Rkuerzel1>", "<Rkuerzel2>", "<Rkuerzel3>", "<Rkuerzel4>",
                "<Rlangtext1>", "<Rlangtext2>", "<Rlangtext3>", "<Rlangtext4>", "<Rbarcode>", "<Systemik>", "<Rwert>",
                "<Rhbpos>", "<Rwegpos>", "<Rhbpreis>", "<Rwegpreis>", "<Rhbproz>", "<Rwegproz>", "<Rhbanzahl>",
                "<Rweganzahl>", "<Rhbgesamt>", "<Rweggesamt>", "<Rwegkm>", "<Rtage>" });
        for (int i = 0; i < lAdrRDaten.size(); i++) {
            hmAdrRDaten.put(lAdrRDaten.get(i), "");
        }
        /********************/
        hmAdrBDaten = new HashMap<String, String>();
        List<String> lAdrBDaten = Arrays.asList(new String[] { "<Badr1>", "<Badr2>", "<Badr3>", "<Badr4>", "<Badr5>",
                "<Bbanrede>", "<Bihrenpat>", "<Bdisziplin>", "<Bdiagnose>", "<Breznr>", "<Brezdatum>", "<Bblock1>",
                "<Bblock2>", "<Bblock3>", "<Bblock4>", "<Btitel1>", "<Btitel2>", "<Btitel3>", "<Btitel4>", "<Bnname>",
                "<Bvnname>", "<Bgeboren>", "<Btherapeut>", "<Berstdat>", "<Bletztdat>", "<Banzahl1>", "<Banzahl2>",
                "<Banzahl3>", "<Banzahl4>", "<Blang1>", "<Blang2>", "<Blang3>", "<Blang4>", "<Barztfax>",
                "<Barztemail>" });
        for (int i = 0; i < lAdrBDaten.size(); i++) {
            hmAdrBDaten.put(lAdrBDaten.get(i), "");
        }
        /********************/
        hmAdrAFRDaten = new HashMap<String, String>();
        List<String> lAdrAFRDaten = Arrays.asList(new String[] { "<AFRposition1>", "<AFRposition2>", "<AFRposition3>",
                "<AFRposition4>", "<AFRpreis1>", "<AFRpreis2>", "<AFRpreis3>", "<AFRpreis4>", "<AFRgesamt>",
                "<AFRnummer>", "<AFRkurz1>", "<AFRkurz2>", "<AFRkurz3>", "<AFRkurz4>", "<AFRlang1>", "<AFRlang2>",
                "<AFRlang3>", "<AFRlang4>" });
        for (int i = 0; i < lAdrAFRDaten.size(); i++) {
            hmAdrAFRDaten.put(lAdrAFRDaten.get(i), "");
        }
        /********************/
        hmAdrHMRDaten = new HashMap<String, String>();

    }

    public static void DesktopLesen() {
        hmContainer = new HashMap<String, Integer>();
        String[] fenster = { "Kasse", "Patient", "Kalender", "Arzt", "Gutachten", "Abrechnung" };
        String[] files = { "kasse.ini", "patient.ini", "kalender.ini", "arzt.ini", "gutachten.ini", "abrechnung.ini" };
        Settings inif = null;
        boolean mustupdate = false;
        for (int i = 0; i < fenster.length; i++) {
            try {
                mustupdate = false;
                inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", files[i]);
                // desktopPane 0 oder 1
                if (inif.getIntegerProperty("Container", "StarteIn") == null) {
                    inif.setIntegerProperty("Container", "StarteIn", 1, null);
                    mustupdate = true;
                }
                hmContainer.put(fenster[i], inif.getIntegerProperty("Container", "StarteIn"));
                // immer auf maximale Größe
                if (inif.getIntegerProperty("Container", "ImmerOptimieren") == null) {
                    inif.setIntegerProperty("Container", "ImmerOptimieren", (fenster[i].equals("Kalender") ? 1 : 0),
                            null);
                    mustupdate = true;
                }
                hmContainer.put(fenster[i] + "Opti", inif.getIntegerProperty("Container", "ImmerOptimieren"));
                // X-Position
                if (inif.getIntegerProperty("Container", "ZeigeAnPositionX") == null) {
                    inif.setIntegerProperty("Container", "ZeigeAnPositionX", 5, null);
                    mustupdate = true;
                    if (fenster[i].equals("Kalender")) {
                        hmContainer.put(fenster[i] + "Opti", 1);
                    }
                }
                hmContainer.put(fenster[i] + "LocationX", inif.getIntegerProperty("Container", "ZeigeAnPositionX"));
                // Y-Position
                if (inif.getIntegerProperty("Container", "ZeigeAnPositionY") == null) {
                    inif.setIntegerProperty("Container", "ZeigeAnPositionY", 5, null);
                    mustupdate = true;
                }
                hmContainer.put(fenster[i] + "LocationY", inif.getIntegerProperty("Container", "ZeigeAnPositionY"));
                // X-Größe
                if (inif.getIntegerProperty("Container", "DimensionX") == null) {
                    inif.setIntegerProperty("Container", "DimensionX", -1, null);
                    mustupdate = true;
                }
                hmContainer.put(fenster[i] + "DimensionX", inif.getIntegerProperty("Container", "DimensionX"));
                // Y-Größe
                if (inif.getIntegerProperty("Container", "DimensionY") == null) {
                    inif.setIntegerProperty("Container", "DimensionY", -1, null);
                    mustupdate = true;
                }
                hmContainer.put(fenster[i] + "DimensionY", inif.getIntegerProperty("Container", "DimensionY"));
                if (mustupdate) {
                    INITool.saveIni(inif);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Fehler bei der Verarbeitung von INI-Dateien , Mehode:DesktopLesen!\nFehlertext: "
                                + ex.getMessage());
            }
        }
    }

    public static void PatientLesen() {
        try {
            vPatMerker = new Vector<String>();
            vPatMerkerIcon = new Vector<ImageIcon>();
            vPatMerkerIconFile = new Vector<String>();
            Settings inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "patient.ini");
            for (int i = 1; i < 7; i++) {
                vPatMerker.add(inif.getStringProperty("Kriterien", "Krit" + i));
                String simg = inif.getStringProperty("Kriterien", "Image" + i);
                if (simg == null || simg.equals("")) {
                    vPatMerkerIcon.add(null);
                    vPatMerkerIconFile.add(null);
                } else {
                    vPatMerkerIcon.add(new ImageIcon(Path.Instance.getProghome() + "icons/" + simg));
                    vPatMerkerIconFile.add(Path.Instance.getProghome() + "icons/" + simg);
                }
            }
        } catch (Exception es) {
            es.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Auswertung der Patientenkriterien (patient.ini).\nFehlertext: " + es.getMessage());
        }
    }

    public static void GeraeteInit() {
        Settings inif = null;
        try {
            inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "geraete.ini");
            boolean mustsave = false;
            if (inif.getIntegerProperty("KartenLeser", "KartenLeserAktivieren") > 0) {
                sReaderName = inif.getStringProperty("KartenLeser", "KartenLeserName");
                sReaderAktiv = "1";

                if (inif.getStringProperty("KartenLeser", "KartenLeserCTAPILib") == null) {
                    inif.setStringProperty("KartenLeser", "KartenLeserCTAPILib", "ctpcsc31kv", null);
                    mustsave = true;
                }
                if (inif.getStringProperty("KartenLeser", "KartenLeserDeviceID") == null) {
                    inif.setStringProperty("KartenLeser", "KartenLeserDeviceID", "0", null);
                    mustsave = true;
                }

                sReaderCtApiLib = inif.getStringProperty("KartenLeser", "KartenLeserCTAPILib");
                sReaderDeviceID = inif.getStringProperty("KartenLeser", "KartenLeserDeviceID");
                hmKVKDaten = new HashMap<String, String>();
                hmKVKDaten.put("Krankekasse", "");
                hmKVKDaten.put("Kassennummer", "");
                hmKVKDaten.put("Kartennummer", "");
                hmKVKDaten.put("Versichertennummer", "");
                hmKVKDaten.put("Status", "");
                hmKVKDaten.put("Statusext", "");
                hmKVKDaten.put("Vorname", "");
                hmKVKDaten.put("Nachname", "");
                hmKVKDaten.put("Geboren", "");
                hmKVKDaten.put("Strasse", "");
                hmKVKDaten.put("Land", "");
                hmKVKDaten.put("Plz", "");
                hmKVKDaten.put("Ort", "");
                hmKVKDaten.put("Gueltigkeit", "");
                hmKVKDaten.put("Checksumme", "");
                hmKVKDaten.put("Fehlercode", "");
                hmKVKDaten.put("Fehlertext", "");
                hmKVKDaten.put("Anrede", "");

            } else {
                sReaderName = "";
                sReaderAktiv = "0";
                sReaderCtApiLib = inif.getStringProperty("KartenLeser", "KartenLeserCTAPILib");
            }
            if (inif.getIntegerProperty("BarcodeScanner", "BarcodeScannerAktivieren") > 0) {
                try {
                    sBarcodeScanner = inif.getStringProperty("BarcodeScanner", "BarcodeScannerName");
                    sBarcodeAktiv = inif.getStringProperty("BarcodeScanner", "BarcodeScannerAktivieren");
                    sBarcodeCom = inif.getStringProperty("BarcodeScanner", "BarcodeScannerAnschluss");
                    if (inif.getStringProperty("BarcodeScanner", "RsFtOhneKalender") != null) {
                        RsFtOhneKalender = (inif.getIntegerProperty("BarcodeScanner", "RsFtOhneKalender") == 0 ? false
                                : true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                sBarcodeScanner = "";
                sBarcodeAktiv = "0";
                sBarcodeCom = inif.getStringProperty("BarcodeScanner", "BarcodeScannerAnschluss");
            }
            hmDokuScanner = new HashMap<String, String>();
            if (inif.getIntegerProperty("DokumentenScanner", "DokumentenScannerAktivieren") > 0) {
                sDokuScanner = inif.getStringProperty("DokumentenScanner", "DokumentenScannerName");
                hmDokuScanner.put("aktivieren", "1");
                hmDokuScanner.put("aufloesung",
                        inif.getStringProperty("DokumentenScanner", "DokumentenScannerAufloesung"));
                hmDokuScanner.put("farben", inif.getStringProperty("DokumentenScanner", "DokumentenScannerFarben"));
                hmDokuScanner.put("seiten", inif.getStringProperty("DokumentenScanner", "DokumentenScannerSeiten"));
                hmDokuScanner.put("dialog", inif.getStringProperty("DokumentenScanner", "DokumentenScannerDialog"));
            } else {
                sDokuScanner = "Scanner nicht aktiviert!";
                hmDokuScanner.put("aktivieren", "0");
                hmDokuScanner.put("aufloesung", "---");
                hmDokuScanner.put("farben", "---");
                hmDokuScanner.put("seiten", "---");
                hmDokuScanner.put("dialog", "---");
            }

            if (mustsave) {
                INITool.saveIni(inif);
            }
            logger.info("RsFtOhneKalender = " + RsFtOhneKalender);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der geraete.ini, Mehode:GeraeteInit!\nFehlertext: " + ex.getMessage());
        }

    }

    public static void ArztGruppenInit() {
        try {
            Settings inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "arzt.ini");
            int ags;
            if ((ags = inif.getIntegerProperty("ArztGruppen", "AnzahlGruppen")) > 0) {
                arztGruppen = new String[ags];
                for (int i = 0; i < ags; i++) {
                    arztGruppen[i] = inif.getStringProperty("ArztGruppen", "Gruppe" + Integer.valueOf(i + 1)
                                                                                             .toString());
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der arzt.ini, Mehode:ArztGruppenInit!\nFehlertext: "
                            + ex.getMessage());
        }
    }

    public static void RezeptInit() {
        try {
            Settings inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "rezept.ini");
            boolean mustsave = false;
            // public static String[] rezeptKlassen = null;
            initRezeptKlasse = inif.getStringProperty("RezeptKlassen", "InitKlasse");
            int rezeptKlassenAnzahl = Integer.parseInt(inif.getStringProperty("RezeptKlassen", "KlassenAnzahl"));
            rezeptKlassen = new String[rezeptKlassenAnzahl];
            rezeptKlassenAktiv = new Vector<Vector<String>>();

            int aktiv;
            for (int i = 0; i < rezeptKlassenAnzahl; i++) {
                Vector<String> vec = new Vector<String>();
                try {
                    rezeptKlassen[i] = inif.getStringProperty("RezeptKlassen", "Klasse" + Integer.valueOf(i + 1)
                                                                                                 .toString());
                    if (rezeptKlassen[i].startsWith("Rehasport")) {
                        mitRs = true;
                    }
                    aktiv = inif.getIntegerProperty("RezeptKlassen", "KlasseAktiv" + Integer.valueOf(i + 1)
                                                                                            .toString());
                    if (aktiv > 0) {
                        vec.clear();
                        vec.add(String.valueOf(rezeptKlassen[i]));
                        vec.add(inif.getStringProperty("RezeptKlassen", "KlasseKurz" + Integer.valueOf(i + 1)
                                                                                              .toString()));
                        rezeptKlassenAktiv.add( vec);
                    }
                } catch (Exception ex) {
                    logger.info("Fehler bei Rezeptklasse " + i,ex);
                }
            }
            rezGebDrucker = inif.getStringProperty("DruckOptionen", "RezGebDrucker");
            rezGebVorlageNeu = Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK() + "/"
                    + inif.getStringProperty("Vorlagen", "RezGebVorlageNeu");
            rezGebVorlageAlt = Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK() + "/"
                    + inif.getStringProperty("Vorlagen", "RezGebVorlageAlt");
            rezGebVorlageHB = Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK() + "/"
                    + inif.getStringProperty("Vorlagen", "RezGebVorlageHB");
            rezGebDirektDruck = (inif.getIntegerProperty("DruckOptionen", "DirektDruck") <= 0 ? false : true);
            rezBarcodeDrucker = inif.getStringProperty("DruckOptionen", "BarCodeDrucker");
            rezeptKlassenAnzahl = inif.getIntegerProperty("BarcodeForm", "BarcodeFormAnzahl");
            if (rezeptKlassenAnzahl > 0) {
                rezBarCodName = new String[rezeptKlassenAnzahl];
                rezBarCodForm = new Vector<String>();
                for (int i = 0; i < rezeptKlassenAnzahl; i++) {
                    rezBarCodName[i] = inif.getStringProperty("BarcodeForm", "FormName" + (i + 1));
                    rezBarCodForm.add(inif.getStringProperty("BarcodeForm", "FormVorlage" + (i + 1)));
                }
            } else {
                rezBarCodName = new String[] { null };
                rezBarCodForm = new Vector<String>();
            }
            String dummy = inif.getStringProperty("Sonstiges", "AngelegtVonUser");
            if (dummy == null) {
                inif.setStringProperty("Sonstiges", "AngelegtVonUser", "0", null);
                mustsave = true;
            } else {
                AngelegtVonUser = (inif.getStringProperty("Sonstiges", "AngelegtVonUser")
                                       .equals("0") ? false : true);
            }
            dummy = inif.getStringProperty("Sonstiges", "RezGebWarnung");
            if (dummy == null) {
                inif.setStringProperty("Sonstiges", "RezGebWarnung", "1", null);
                mustsave = true;
            } else {
                RezGebWarnung = (inif.getStringProperty("Sonstiges", "RezGebWarnung")
                                     .equals("0") ? false : true);
            }
            dummy = inif.getStringProperty("Sonstiges", "BehDatumTippen");
            if (dummy != null) {
                behdatumTippen = (inif.getStringProperty("Sonstiges", "BehDatumTippen")
                                      .equals("0") ? false : true);
            }

            dummy = inif.getStringProperty("Sonstiges", "StornoStattLoeschen");
            if (dummy == null) { // Eintrag noch nicht vorhanden?
                inif.setStringProperty("Sonstiges", "StornoStattLoeschen", "0", null); // default wie gehabt: löschen
                mustsave = true;
            } else {
                useStornieren = (inif.getStringProperty("Sonstiges", "StornoStattLoeschen")
                                     .equals("0") ? false : true);
            }

            String[] hmPraefixArt = { "KG", "MA", "ER", "LO", "RH", "PO", "RS", "FT" };
            String[] hmPraefixZahl = { "22", "21", "26", "23", "67", "71", "61", "62" };
            String[] hmIndexZahl = { "2", "1", "5", "3", "6", "7", "6", "7" };
            // hmHmPraefix

            if (inif.getStringProperty("HMRPraefix", "KG") == null
                    || inif.getStringProperty("HMRPosindex", "KG") == null) {
                for (int i = 0; i < hmPraefixArt.length; i++) {
                    inif.setStringProperty("HMRPraefix", hmPraefixArt[i], hmPraefixZahl[i], null);
                    inif.setStringProperty("HMRPosindex", hmPraefixArt[i], hmIndexZahl[i], null);
                    hmHmPraefix.put(hmPraefixArt[i], hmPraefixZahl[i]);
                    hmHmPosIndex.put(hmPraefixArt[i], hmIndexZahl[i]);
                }
                mustsave = true;
            } else {
                for (int i = 0; i < hmPraefixArt.length; i++) {
                    hmHmPraefix.put(hmPraefixArt[i], inif.getStringProperty("HMRPraefix", hmPraefixArt[i]));
                    hmHmPosIndex.put(hmPraefixArt[i], inif.getStringProperty("HMRPosindex", hmPraefixArt[i]));
                }
            }
            if (mustsave) {
                INITool.saveIni(inif);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der rezept.ini, Mehode:RezeptInit!\nFehlertext: " + ex.getMessage());
        }

    }

    public static void TherapBausteinInit() {
        Settings inif = null;
        try {
            inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "thbericht.ini");
            hmTherapBausteine = new HashMap<String, Vector<String>>();
            int lang = rezeptKlassenAktiv.size();
            Vector<String> vec = new Vector<String>();
            for (int i = 0; i < lang; i++) {
                vec.clear();
                String prop = "AnzahlThemen_" + RezTools.getDisziplinFromRezNr(rezeptKlassenAktiv.get(i)
                                                                                                .get(1));
                int lang2 = inif.getIntegerProperty("Textbausteine", prop);
                String prop2 = RezTools.getDisziplinFromRezNr(rezeptKlassenAktiv.get(i)
                                                                               .get(1));
                for (int i2 = 0; i2 < lang2; i2++) {
                    vec.add(inif.getStringProperty(prop2, "Thema" + (i2 + 1)));
                }
                hmTherapBausteine.put(prop2, (Vector<String>) vec.clone());
            }
            for (int i = 0; i < 4; i++) {
                berichttitel[i] = inif.getStringProperty("Bericht", "Block" + (i + 1));
            }
            thberichtdatei = Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK() + "/"
                    + inif.getStringProperty("Datei", "BerichtsDatei");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der thbericht.ini, Mehode:TherapBausteinInit!\nFehlertext: "
                            + ex.getMessage());
        }

    }

    public static void FirmenDaten() {
        Settings inif = null;
        try {
            String[] stitel = { "Ik", "Ikbezeichnung", "Firma1", "Firma2", "Anrede", "Nachname", "Vorname", "Strasse",
                    "Plz", "Ort", "Telefon", "Telefax", "Email", "Internet", "Bank", "Blz", "Kto", "Steuernummer",
                    "Hrb", "Logodatei", "Zusatz1", "Zusatz2", "Zusatz3", "Zusatz4", "Bundesland" };
            hmFirmenDaten = new HashMap<String, String>();
            inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "firmen.ini");
            for (int i = 0; i < stitel.length; i++) {
                hmFirmenDaten.put(stitel[i], inif.getStringProperty("Firma", stitel[i]));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der firmen.ini, Mehode:FirmenDaten!\nFehlertext: " + ex.getMessage());
        }
    }

    // Lemmi 20101224 Steuerparanmeter für RGR und AFR in OffenPosten und Mahnungen,
    // zentral einlesen
    public static void OffenePostenIni_ReadFromIni() {
        Settings inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/",
                "offeneposten.ini");

        // Voreinstellung von Defaultwerten
        hmZusatzInOffenPostenIni.put("RGRinOPverwaltung", 0);
        hmZusatzInOffenPostenIni.put("AFRinOPverwaltung", 0);

        try {
            if (inif.getStringProperty("ZusaetzlicheRechnungen", "RGRinOPverwaltung") != null) // Prüfung auf Existenz
                hmZusatzInOffenPostenIni.put("RGRinOPverwaltung",
                        inif.getIntegerProperty("ZusaetzlicheRechnungen", "RGRinOPverwaltung"));
            if (inif.getStringProperty("ZusaetzlicheRechnungen", "AFRinOPverwaltung") != null) // Prüfung auf Existenz
                hmZusatzInOffenPostenIni.put("AFRinOPverwaltung",
                        inif.getIntegerProperty("ZusaetzlicheRechnungen", "AFRinOPverwaltung"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Die Datei 'offeneposten.ini' zur aktuellen IK-Nummer kann nicht gelesen werden.");
        }
    }

    public static void EigeneDokuvorlagenLesen() {
        Settings inif = null;
        try {
            inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "eigenedoku.ini");
            String dokus = null;
            vOwnDokuTemplate.clear();
            hmDokuSortMode.clear();
            boolean mustsave = false;
            // Prüfung auf Existenz
            if ((dokus = inif.getStringProperty("EigeneDokus", "DokuAnzahl")) == null) {
                inif.setStringProperty("EigeneDokus", "DokuAnzahl", "0", null);
                inif.setStringProperty("EigeneDokus", "DokuText1", "", null);
                inif.setStringProperty("EigeneDokus", "DokuDatei1", "", null);
                mustsave = true;
            } else {
                Vector<String> vdummy = new Vector<String>();
                for (int i = 0; i < Integer.parseInt(dokus); i++) {
                    vdummy.clear();
                    vdummy.add(inif.getStringProperty("EigeneDokus", "DokuText" + Integer.toString(i + 1)));
                    vdummy.add(inif.getStringProperty("EigeneDokus", "DokuDatei" + Integer.toString(i + 1)));
                    vOwnDokuTemplate.add((Vector<String>) vdummy.clone());
                }
            }
            if ((dokus = inif.getStringProperty("EigeneDokus", "SortByDate")) == null) {
                inif.setStringProperty("EigeneDokus", "SortByDate", "0", null);
                inif.setStringProperty("EigeneDokus", "SortAsc", "0", null);
                hmDokuSortMode.put("sortmode", "0");
                hmDokuSortMode.put("sortasc", "0");

                mustsave = true;
            } else {
                hmDokuSortMode.put("sortmode", inif.getStringProperty("EigeneDokus", "SortByDate"));
                hmDokuSortMode.put("sortasc", inif.getStringProperty("EigeneDokus", "SortAsc"));
            }
            if (mustsave) {
                INITool.saveIni(inif);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der eigenedoku.ini, Mehode:EigeneDokuvorlagenLesen!\nFehlertext: "
                            + ex.getMessage());
        }

    }

    // Lemmi 20101223 Steuerparanmeter für den Patienten-Suchen-Dialog aus der INI
    // zentral einlesen
    public static void BedienungIni_ReadFromIni() {
        Settings inif = null;
        try {
            boolean mustsave = false;
            inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "bedienung.ini");

//            if ( inif.IsFileLoaded() )
//                int x = 5;
            // funktioniert auch mit Defaultwerten, wenn die INI-Datei bis dato NICHT
            // existiert. In BedienungIni_WriteToIni()
            // wird dann eine komplette INI angelegt !

            // Voreinstellung von Defaultwerten
            hmPatientenWerkzeugDlgIni.put("ToolsDlgClickCount", 2);
            if (inif.getStringProperty("Bedienung", "WerkzeugaufrufMausklicks") != null) // Prüfung auf Existenz
                hmPatientenWerkzeugDlgIni.put("ToolsDlgClickCount",
                        inif.getIntegerProperty("Bedienung", "WerkzeugaufrufMausklicks"));

            hmPatientenWerkzeugDlgIni.put("ToolsDlgShowButton", false);
            if (inif.getStringProperty("Bedienung", "WerkzeugaufrufButtonZeigen") != null) // Prüfung auf Existenz
                hmPatientenWerkzeugDlgIni.put("ToolsDlgShowButton",
                        inif.getIntegerProperty("Bedienung", "WerkzeugaufrufButtonZeigen") == 1 ? true : false);
            hmRezeptDlgIni.put("RezAendAbbruchWarn", false);
            if (inif.getStringProperty("Rezept", "RezeptAenderungAbbruchWarnung") != null) // Prüfung auf Existenz
                hmRezeptDlgIni.put("RezAendAbbruchWarn",
                        inif.getIntegerProperty("Rezept", "RezeptAenderungAbbruchWarnung") == 1 ? true : false);

            /// Zeigt den Terminbestätigungsdialog wenn erforderlich, siehe Erweiterung von
            /// Drud
            // Ist der Wert false wird der Dialog nie gezeigt
            // mit Strg+F11 anstatt Shift+F11 kann die Anzeige des Dialoges
            // aber unabhängig von dieser Einstellung erzwungen werden /st.
            if (inif.getStringProperty("Termine", "HMDialogZeigen") != null) {
                hmTerminBestaetigen.put("dlgzeigen",
                        (inif.getIntegerProperty("Termine", "HMDialogZeigen") == 1 ? true : false));
            } else {
                hmTerminBestaetigen.put("dlgzeigen", false);
                inif.setStringProperty("Termine", "HMDialogZeigen", "0", null);
                mustsave = true;
            }
            // nur wenn unterschiedliche Anzahlen im Rezept vermerkt und auch nur solange
            // bis es nichts mehr zu zeigen gibt, sprich die Heilmittel mit der geringeren
            // Anzahl
            // bereits bis zur Maximalanzahl bestätigt wurden
            // die dahinterliegende Funktion ist noch nicht implementiert
            // Persönlicher Wunsch von mir: Drud's Job /st.
            if (inif.getStringProperty("Termine", "HMDialogDiffZeigen") != null) {
                hmTerminBestaetigen.put("dlgdiffzeigen",
                        (inif.getIntegerProperty("Termine", "HMDialogDiffZeigen") == 1 ? true : false));
            } else {
                hmTerminBestaetigen.put("dlgdiffzeigen", false);
                inif.setStringProperty("Termine", "HMDialogDiffZeigen", "0", null);
                mustsave = true;
            }
            if (inif.getStringProperty("PwDialog", "PWVollbild") != null) {
                fullSizePwDialog = (inif.getIntegerProperty("PwDialog", "PWVollbild") == 1 ? true : false);

            } else {
                fullSizePwDialog = false;
                inif.setStringProperty("PwDialog", "PWVollbild", "0", null);
                mustsave = true;
            }

            // Voreinstellung von Defaultwerten
            hmPatientenSuchenDlgIni.put("suchart", 0);
            hmPatientenSuchenDlgIni.put("fensterbreite", 300);
            hmPatientenSuchenDlgIni.put("fensterhoehe", 400);
            try {
                if (inif.getStringProperty("PatientenSuche", "Suchart") != null) // Prüfung auf Existenz
                    hmPatientenSuchenDlgIni.put("suchart", inif.getIntegerProperty("PatientenSuche", "Suchart"));
                if (inif.getStringProperty("PatientenSuche", "SuchFensterBreite") != null) // Prüfung auf Existenz
                    hmPatientenSuchenDlgIni.put("fensterbreite",
                            inif.getIntegerProperty("PatientenSuche", "SuchFensterBreite"));
                if (inif.getStringProperty("PatientenSuche", "SuchFensterHoehe") != null) // Prüfung auf Existenz
                    hmPatientenSuchenDlgIni.put("fensterhoehe",
                            inif.getIntegerProperty("PatientenSuche", "SuchFensterHoehe"));
                if (inif.getStringProperty("PatientenSuche", "erweiterteUmlautSuche") != null) {
                    hmPatientenSuchenDlgIni.put("erweiterteUmlautSuche",
                            inif.getIntegerProperty("PatientenSuche", "erweiterteUmlautSuche"));
                } else {
                    inif.setIntegerProperty("PatientenSuche", "erweiterteUmlautSuche", 0,
                            " berücksichtigt bei dt. Umlauten versch. Schreibweisen");
                    hmPatientenSuchenDlgIni.put("erweiterteUmlautSuche", 0); // kein Eintrag -> Voreinstellung:
                                                                             // abgeschaltet
                    mustsave = true;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Die Datei 'bedienung.ini' zur aktuellen IK-Nummer kann nicht gelesen werden.");
            }
            if (mustsave) {
                INITool.saveIni(inif);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Fehler bei der Verarbeitung der bedienung.ini, Mehode:BedienungIni_ReadFromIni!\nFehlertext: "
                            + ex.getMessage());
        }
    }

    // Lemmi 20101223 Steuerparanmeter für den Patienten-Suchen-Dialog in die INI
    // schreiben
    public static void BedienungIni_WriteToIni() {
        Settings inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "bedienung.ini");

        // Sofern alle Parameter hier wieder in die INI geschrieben werden, legt das
        // eine komplette INI an !

        inif.setIntegerProperty("Bedienung", "WerkzeugaufrufMausklicks",
                Integer.parseInt(hmPatientenWerkzeugDlgIni.get("ToolsDlgClickCount")
                                                          .toString()),
                " Anzahl Klicks für Werkzeugaufruf");
        inif.setIntegerProperty("Bedienung", "WerkzeugaufrufButtonZeigen",
                (Boolean) hmPatientenWerkzeugDlgIni.get("ToolsDlgShowButton") ? 1 : 0,
                " Zusatzknopf im Werkzeugdialog");

        inif.setIntegerProperty("PatientenSuche", "SuchFensterBreite", hmPatientenSuchenDlgIni.get("fensterbreite"),
                " letzte Breite des Suchfensters");
        inif.setIntegerProperty("PatientenSuche", "SuchFensterHoehe", hmPatientenSuchenDlgIni.get("fensterhoehe"),
                " letzte Höhe des Suchfensters");
        inif.setIntegerProperty("PatientenSuche", "Suchart", hmPatientenSuchenDlgIni.get("suchart"),
                " letzte angewählte Suchart Suchfensters");
        inif.setIntegerProperty("PatientenSuche", "erweiterteUmlautSuche",
                hmPatientenSuchenDlgIni.get("erweiterteUmlautSuche"),
                " berücksichtigt bei dt. Umlauten versch. Schreibweisen");

        // Lemmi 20110116: Abfrage Abbruch bei Rezeptänderungen mit Warnung
        inif.setIntegerProperty("Rezept", "RezeptAenderungAbbruchWarnung",
                (Boolean) hmRezeptDlgIni.get("RezAendAbbruchWarn") ? 1 : 0,
                " Abfrage Abbruch bei Rezeptänderungen mit Warnung");
        inif.setIntegerProperty("Termine", "HMDialogZeigen", (Boolean) hmTerminBestaetigen.get("dlgzeigen") ? 1 : 0,
                null);
        inif.setIntegerProperty("Termine", "HMDialogZeigen", (Boolean) hmTerminBestaetigen.get("dlgzeigen") ? 1 : 0,
                null);
        inif.setIntegerProperty("Termine", "HMDialogDiffZeigen",
                (Boolean) hmTerminBestaetigen.get("dlgdiffzeigen") ? 1 : 0, null);

        // Daten wegschreiben
        INITool.saveIni(inif);
        termkalini = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "rehajava.ini");
        termkalini.setStringProperty("HauptFenster", "HorizontalTeilen", (SystemConfig.desktopHorizontal ? "1" : "0"), null);
        INITool.saveIni(termkalini);

        /*
         * // Wegschreiben der INI-Parameter in die Datenbank TESTHALBER
         * inidb.WritePropInteger("bedienung.ini", "Bedienung",
         * "WerkzeugaufrufMausklicks",
         * Integer.parseInt(hmPatientenWerkzeugDlgIni.get("ToolsDlgClickCount").toString
         * ()), "Anzahl Klicks für Werkzeugaufruf");
         * inidb.WritePropInteger("bedienung.ini", "Bedienung",
         * "WerkzeugaufrufButtonZeigen",
         * (Boolean)hmPatientenWerkzeugDlgIni.get("ToolsDlgShowButton") ? 1 : 0,
         * "Zusatzknopf im Werkzeugdialog");
         *
         * inidb.WritePropInteger("bedienung.ini", "PatientenSuche",
         * "SuchFensterBreite", hmPatientenSuchenDlgIni.get("fensterbreite"),
         * "letzte Breite des Suchfensters"); inidb.WritePropInteger("bedienung.ini",
         * "PatientenSuche", "SuchFensterHoehe",
         * hmPatientenSuchenDlgIni.get("fensterhoehe"),
         * " letzte Höhe des Suchfensters"); inidb.WritePropInteger("bedienung.ini",
         * "PatientenSuche", "Suchart", hmPatientenSuchenDlgIni.get("suchart"),
         * " letzte angewählte Suchart im Suchfenster");
         */
    }

    public static void FremdProgs() {
        Settings inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "fremdprog.ini");
        vFremdProgs = new Vector<Vector<String>>();
        Vector<String> progs = new Vector<String>();
        int anzahl = inif.getIntegerProperty("FremdProgramme", "FremdProgrammeAnzahl");
        for (int i = 0; i < anzahl; i++) {
            progs.clear();
            progs.add(inif.getStringProperty("FremdProgramme", "FremdProgrammName" + (i + 1)));
            progs.add(inif.getStringProperty("FremdProgramme", "FremdProgrammPfad" + (i + 1)));
            vFremdProgs.add((Vector<String>) progs.clone());
        }
        hmFremdProgs = new HashMap<String, String>();
        anzahl = inif.getIntegerProperty("FestProg", "FestProgAnzahl");
        for (int i = 0; i < anzahl; i++) {
            hmFremdProgs.put(inif.getStringProperty("FestProg", "FestProgName" + (i + 1)),
                    inif.getStringProperty("FestProg", "FestProgPfad" + (i + 1)));
        }

    }

    public static void GeraeteListe() {
        hmGeraete = new HashMap<String, String[]>();
        Settings inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "geraete.ini");

        int anzahl = inif.getIntegerProperty("KartenLeserListe", "LeserAnzahl");
        String[] string = new String[anzahl];
        String[] string2 = new String[anzahl];
        boolean mustsave = false;
        for (int i = 0; i < anzahl; i++) {
            string[i] = inif.getStringProperty("KartenLeserListe", "Leser" + (i + 1));
            if (inif.getStringProperty("KartenLeserListe", "CTAPILib" + (i + 1)) == null) {
                mustsave = true;
                inif.setStringProperty("KartenLeserListe", "CTAPILib" + (i + 1), "ctpcsc31kv", null);
            }
            string2[i] = inif.getStringProperty("KartenLeserListe", "CTAPILib" + (i + 1));
        }
        sWebCamActive = inif.getStringProperty("WebCam", "WebCamActive");
        if (sWebCamActive == null) {
            mustsave = true;
            sWebCamActive = "0";
            inif.setStringProperty("WebCam", "WebCamActive", "0", null);
        }
        String dummy = inif.getStringProperty("WebCam", "WebCamX");
        if (dummy == null) {
            mustsave = true;
            inif.setIntegerProperty("WebCam", "WebCamX", sWebCamSize[0], null);
            inif.setIntegerProperty("WebCam", "WebCamY", sWebCamSize[1], null);
        }
        // sWebCamActive

        sWebCamSize[0] = inif.getIntegerProperty("WebCam", "WebCamX");
        sWebCamSize[1] = inif.getIntegerProperty("WebCam", "WebCamY");

        hmGeraete.put("Kartenleser", string.clone());
        hmGeraete.put("CTApi", string2.clone());

        anzahl = inif.getIntegerProperty("BarcodeScannerListe", "ScannerAnzahl");
        string = new String[anzahl];
        for (int i = 0; i < anzahl; i++) {
            string[i] = inif.getStringProperty("BarcodeScannerListe", "Scanner" + (i + 1));
        }
        hmGeraete.put("Barcode", string.clone());

        anzahl = inif.getIntegerProperty("ECKartenLeserListe", "ECLeserAnzahl");
        string = new String[anzahl];
        for (int i = 0; i < anzahl; i++) {
            string[i] = inif.getStringProperty("ECKartenLeserListe", "ECLeser" + (i + 1));
        }
        hmGeraete.put("ECKarte", string.clone());
        string = new String[4];
        for (int i = 1; i < 11; i++) {
            string[0] = inif.getStringProperty("COM" + i, "BaudRate");
            string[1] = inif.getStringProperty("COM" + i, "Bits");
            string[2] = inif.getStringProperty("COM" + i, "Parity");
            string[3] = inif.getStringProperty("COM" + i, "StopBit");
            hmGeraete.put("COM" + i, string.clone());
        }
        if (mustsave) {
            INITool.saveIni(inif);
        }

    }

    public static void CompanyInit() {
        hmCompany = new HashMap<String, String>();
        Settings inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "company.ini");
        hmCompany.put("name", inif.getStringProperty("Company", "CompanyName"));
        hmCompany.put("enable", inif.getStringProperty("Company", "DeliverEnable"));
        hmCompany.put("event", inif.getStringProperty("Company", "DeliverEvent"));
        hmCompany.put("ip", inif.getStringProperty("Company", "DeliverIP"));
        hmCompany.put("port", inif.getStringProperty("Company", "DeliverPort"));
        hmCompany.put("mail", inif.getStringProperty("Company", "DeliverMail"));
        hmCompany.put("adress", inif.getStringProperty("Company", "DeliverAdress"));
    }

    public static void GutachtenInit() {
        vGutachtenEmpfaenger = new Vector<String>();
        vGutachtenIK = new Vector<String>();
        vGutachtenArzt = new Vector<String>();
        vGutachtenDisplay = new Vector<String>();
        Settings inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "gutachten.ini");
        int anzahl = inif.getIntegerProperty("GutachtenEmpfaenger", "AnzahlEmpfaenger");
        for (int i = 0; i < anzahl; i++) {
            vGutachtenEmpfaenger.add(inif.getStringProperty("GutachtenEmpfaenger", "Empfaenger" + (i + 1)));
            vGutachtenIK.add(inif.getStringProperty("IKAbsender", "AnsenderIK" + (i + 1)));
        }
        vGutachtenAbsAdresse = new Vector<String>();
        for (int i = 0; i < 5; i++) {
            vGutachtenAbsAdresse.add(inif.getStringProperty("AbsenderAdresse", "AbsenderZeile" + (i + 1)));
        }
        sGutachtenOrt = inif.getStringProperty("AbsenderDaten", "Ort");
        anzahl = inif.getIntegerProperty("Arzt", "ArztAnzahl");
        for (int i = 0; i < anzahl; i++) {
            vGutachtenArzt.add(inif.getStringProperty("Arzt", "Arzt" + (i + 1)));
        }
        vGutachtenDisplay.add(inif.getStringProperty("AbsenderDisplayAdresse", "Display1"));
        vGutachtenDisplay.add(inif.getStringProperty("AbsenderDisplayAdresse", "Display2"));
        vGutachtenDisplay.add(inif.getStringProperty("AbsenderDisplayAdresse", "Display3"));
    }

    public static void AbrechnungParameter() {
        boolean mustsave = false;
        hmAbrechnung.clear();
        /******** Heilmittelabrechnung ********/
        Settings inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "abrechnung.ini");
        hmAbrechnung.put("hmgkvformular", inif.getStringProperty("HMGKVRechnung", "Rformular"));
        hmAbrechnung.put("hmgkvrechnungdrucker", inif.getStringProperty("HMGKVRechnung", "Rdrucker"));
        hmAbrechnung.put("hmgkvtaxierdrucker", inif.getStringProperty("HMGKVRechnung", "Tdrucker"));
        hmAbrechnung.put("hmgkvbegleitzettel", inif.getStringProperty("HMGKVRechnung", "Begleitzettel"));
        hmAbrechnung.put("hmgkvrauchdrucken", inif.getStringProperty("HMGKVRechnung", "Rauchdrucken"));
        hmAbrechnung.put("hmgkvrexemplare", inif.getStringProperty("HMGKVRechnung", "Rexemplare"));
        if (inif.getStringProperty("HMGKVRechnung", "FreigabeErzwingen") == null) {
            hmAbrechnung.put("hmgkvfreigabeerzwingen", "0");
            inif.setStringProperty("HMGKVRechnung", "FreigabeErzwingen", "0", null);
            mustsave = true;
        } else {
            if (inif.getStringProperty("HMGKVRechnung", "FreigabeErzwingen")
                    .trim()
                    .equals("0")) {
                hmAbrechnung.put("hmgkvfreigabeerzwingen", "0");

            } else if (inif.getStringProperty("HMGKVRechnung", "FreigabeErzwingen")
                           .trim()
                           .equals("1")) {
                hmAbrechnung.put("hmgkvfreigabeerzwingen", "1");
            } else {
                hmAbrechnung.put("hmgkvfreigabeerzwingen", "0");
            }
        }
        hmAbrechnung.put("hmgkvfreigabeerzwingen", inif.getStringProperty("HMGKVRechnung", "FreigabeErzwingen"));

        hmAbrechnung.put("hmpriformular", inif.getStringProperty("HMPRIRechnung", "Pformular"));
        hmAbrechnung.put("hmpridrucker", inif.getStringProperty("HMPRIRechnung", "Pdrucker"));
        hmAbrechnung.put("hmpriexemplare", inif.getStringProperty("HMPRIRechnung", "Pexemplare"));

        hmAbrechnung.put("hmbgeformular", inif.getStringProperty("HMBGERechnung", "Bformular"));
        hmAbrechnung.put("hmbgedrucker", inif.getStringProperty("HMBGERechnung", "Bdrucker"));
        hmAbrechnung.put("hmbgeexemplare", inif.getStringProperty("HMBGERechnung", "Bexemplare"));
        /******** Rehaabrechnung ********/
        hmAbrechnung.put("rehagkvformular", inif.getStringProperty("RehaGKVRechnung", "RehaGKVformular"));
        hmAbrechnung.put("rehagkvdrucker", inif.getStringProperty("RehaGKVRechnung", "RehaGKVdrucker"));
        hmAbrechnung.put("rehagkvexemplare", inif.getStringProperty("RehaGKVRechnung", "RehaGKVexemplare"));
        hmAbrechnung.put("rehagkvik", inif.getStringProperty("RehaGKVRechnung", "RehaGKVik"));

        hmAbrechnung.put("rehadrvformular", inif.getStringProperty("RehaDRVRechnung", "RehaDRVformular"));
        hmAbrechnung.put("rehadrvdrucker", inif.getStringProperty("RehaDRVRechnung", "RehaDRVdrucker"));
        hmAbrechnung.put("rehadrvexemplare", inif.getStringProperty("RehaDRVRechnung", "RehaDRVexemplare"));
        hmAbrechnung.put("rehadrvik", inif.getStringProperty("RehaDRVRechnung", "RehaDRVik"));

        hmAbrechnung.put("rehapriformular", inif.getStringProperty("RehaPRIRechnung", "RehaPRIformular"));
        hmAbrechnung.put("rehapridrucker", inif.getStringProperty("RehaPRIRechnung", "RehaPRIdrucker"));
        hmAbrechnung.put("rehapriexemplare", inif.getStringProperty("RehaPRIRechnung", "RehaPRIexemplare"));
        hmAbrechnung.put("rehapriik", inif.getStringProperty("RehaPRIRechnung", "RehaPRIik"));

        hmAbrechnung.put("hmallinoffice", inif.getStringProperty("GemeinsameParameter", "InOfficeStarten"));

        String section = "HMGKVRechnung";
        if (inif.getStringProperty("HMGKVRechnung", "AutoOKwenn302offen") == null) { // kein Eintrag in ini -> default
                                                                                     // anlegen
            inif.setStringProperty(section, "AutoOKwenn302offen", "0",
                    "Rezept bekommt automatisch Haekchen wenn beim Abschliessen auch das 302-er Panel offen ist");
            mustsave = true;
        }
        hmAbrechnung.put("autoOk302", inif.getStringProperty(section, "AutoOKwenn302offen"));

        if (inif.getStringProperty(section, "keepTageTreeSize") == null) { // kein Eintrag in ini -> default anlegen
            inif.setIntegerProperty(section, "keepTageTreeSize", 1,
                    "Groesse des Fensters fuer versch. Anz. Behandlungstage merken");
            inif.setIntegerProperty(section, "maxTage", 24, "maximale Anz. Behandlungstage");
            inif.setIntegerProperty(section, "lockSettings", 0, "Aktualisieren der Eintraege gesperrt");
            mustsave = true;
        }
        hmAbrechnung.put("keepTTSize", inif.getStringProperty(section, "keepTageTreeSize"));
        hmAbrechnung.put("TTSizeLocked", inif.getStringProperty(section, "lockSettings"));
        hmAbrechnung.put("maxBehTage", inif.getStringProperty(section, "maxTage"));

        if (inif.getStringProperty(section, "usePrinterFromTemplate") == null) { // kein Eintrag in ini -> default
                                                                                 // setzen
            hmAbrechnung.put("hmusePrinterFromTemplate", "0");
        } else {
            hmAbrechnung.put("hmusePrinterFromTemplate", inif.getStringProperty(section, "usePrinterFromTemplate"));
        }
        String sask = inif.getStringProperty("GemeinsameParameter", "FragenVorEmail");
        if (sask == null) {
            logger.info("Erstelle Parameter 'FrageVorEmail'");
            inif.setStringProperty("GemeinsameParameter", "FragenVorEmail", "1", null);
            mustsave = true;
        }
        hmAbrechnung.put("hmaskforemail", inif.getStringProperty("GemeinsameParameter", "FragenVorEmail"));

        if (inif.getStringProperty("RGRParameter", "RGRPauschale") == null) {
            hmAbrechnung.put("rgrpauschale", "5,00");
            inif.setStringProperty("RGRParameter", "RGRPauschale", "5,00", "Voreinstellung Bearbeitungsgebühr");
            mustsave = true;
        } else {
            hmAbrechnung.put("rgrpauschale", inif.getStringProperty("RGRParameter", "RGRPauschale"));
        }
        if (inif.getStringProperty("RGRParameter", "RGRexemplare") == null) {
            hmAbrechnung.put("rgrdruckanzahl", "2");
            inif.setStringProperty("RGRParameter", "RGRexemplare", "2",
                    "Voreinstellung Anzahl Ausdrucke RGR-Rechnungen");
            mustsave = true;
        } else {
            hmAbrechnung.put("rgrdruckanzahl", inif.getStringProperty("RGRParameter", "RGRexemplare"));
        }
        /* } */
        sask = inif.getStringProperty("GKVTaxierung", "AnzahlVorlagen");
        if (sask == null) {
            logger.info("Erstelle Parameter 'AnzahlVorlagen'");
            inif.setStringProperty("GKVTaxierung", "AnzahlVorlagen", "0", null);
            inif.setStringProperty("GKVTaxierung", "Vorlage1", "", null);
            mustsave = true;
        }
        vecTaxierung.clear();
        vecTaxierung.add("TaxierungA5.ott");
        vecTaxierung.add("TaxierungA4.ott");
        try {
            int ownTemplate = Integer.parseInt(inif.getStringProperty("GKVTaxierung", "AnzahlVorlagen"));
            for (int i = 0; i < ownTemplate; i++) {
                vecTaxierung.add(inif.getStringProperty("GKVTaxierung", "Vorlage" + Integer.toString(i + 1)));
            }
        } catch (Exception ex) {

        }

        if (mustsave) {
            INITool.saveIni(inif);
        }
        // sask = inif.getStringProperty("GemeinsameParameter", "ZuzahlmodusNormal");

        String INI_FILE = "";
        if (System.getProperty("os.name")
                  .contains("Windows")) {
            INI_FILE = "nebraska_windows.conf";
        } else if (System.getProperty("os.name")
                         .contains("Linux")) {
            INI_FILE = "nebraska_linux.conf";
        } else if (System.getProperty("os.name")
                         .contains("String für MaxOSX????")) {
            INI_FILE = "nebraska_mac.conf";
        }
        Verschluesseln man = Verschluesseln.getInstance();
        try {
            inif = INITool.openIni(Path.Instance.getProghome(), INI_FILE);
            String pw = null;
            String decrypted = null;
            hmAbrechnung.put("hmkeystorepw", "");
            int anzahl = inif.getIntegerProperty("KeyStores", "KeyStoreAnzahl");
            for (int i = 0; i < anzahl; i++) {
                if (inif.getStringProperty("KeyStores", "KeyStoreAlias" + Integer.toString(i + 1))
                        .trim()
                        .equals("IK" + Reha.getAktIK())) {
                    pw = inif.getStringProperty("KeyStores", "KeyStorePw" + Integer.toString(i + 1));
                    decrypted = man.decrypt(pw);
                    hmAbrechnung.put("hmkeystorepw", decrypted);
                    hmAbrechnung.put("hmkeystorefile",
                            inif.getStringProperty("KeyStores", "KeyStoreFile" + Integer.toString(i + 1)));
                    hmAbrechnung.put("hmkeystorealias",
                            inif.getStringProperty("KeyStores", "KeyStoreAlias" + Integer.toString(i + 1)));
                    /***********************/
                    // KeyStoreUseCert1
                    /*
                     * if(hmFirmenDaten.get("Firma1").toLowerCase().contains("andi") &&
                     * hmFirmenDaten.get("Ort").toLowerCase().contains("hamburg")){
                     * hmAbrechnung.put("hmkeystoreusecertof", "IK"+Reha.aktIK); isAndi = true;
                     * }else{
                     */
                    if (inif.getStringProperty("KeyStores", "KeyStoreUseCertOf" + Integer.toString(i + 1)) == null) {
                        hmAbrechnung.put("hmkeystoreusecertof", "IK" + Reha.getAktIK());
                    } else {
                        hmAbrechnung.put("hmkeystoreusecertof",
                                inif.getStringProperty("KeyStores", "KeyStoreUseCertOf" + Integer.toString(i + 1)));
                    }
                    /* } */
                    break;
                }
            }
            if (hmAbrechnung.get("hmkeystoreusecertof") == null) {
                hmAbrechnung.put("hmkeystoreusecertof", "Owner nicht vorhanden");
                hmAbrechnung.put("hmkeystorealias", "Alias nicht vorhanden");
            }
            logger.info("Alias=" + hmAbrechnung.get("hmkeystorealias"));
            logger.info("Owner=" + hmAbrechnung.get("hmkeystoreusecertof"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Zertifikatsdatenbank nicht vorhanden oder fehlerhaft.\nAbrechnung nach § 302 kann nicht durchgeführt werden.");
            hmAbrechnung.put("hmkeystoreusecertof", "Alias nicht vorhanden");
            hmAbrechnung.put("hmkeystorealias", "Owner nicht vorhanden");
            SystemConfig.certState = SystemConfig.certNotFound;
            ex.printStackTrace();
        }
    }

    public static void AktiviereLog() {
        Settings inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "rehajava.ini");
        boolean mustsave = false;
        String dummy = inif.getStringProperty("SystemIntern", "VLog");
        if (dummy == null) {
            if (Reha.getAktMandant()
                    .startsWith("RTA")) {
                inif.setStringProperty("SystemIntern", "VLog", "1", null);
                logVTermine = true;
                mustsave = true;
            } else {
                inif.setStringProperty("SystemIntern", "VLog", "0", null);
                logVTermine = false;
                mustsave = true;
            }
        } else {
            logVTermine = (inif.getStringProperty("SystemIntern", "VLog")
                               .trim()
                               .equals("0") ? false : true);
        }
        dummy = inif.getStringProperty("SystemIntern", "ALog");
        if (dummy == null) {
            if (Reha.getAktMandant()
                    .startsWith("RTA")) {
                inif.setStringProperty("SystemIntern", "ALog", "1", null);
                logAlleTermine = true;
                mustsave = true;
            } else {
                inif.setStringProperty("SystemIntern", "ALog", "0", null);
                logAlleTermine = false;
                mustsave = true;
            }
        } else {
            logAlleTermine = (inif.getStringProperty("SystemIntern", "ALog")
                                  .trim()
                                  .equals("0") ? false : true);
        }
        if (mustsave) {
            INITool.saveIni(inif);
        }

    }

    public static void JahresUmstellung() {
        Settings inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/", "rehajava.ini");
        aktJahr = inif.getStringProperty("SystemIntern", "AktJahr");
        String jahrHeute = DatFunk.sHeute()
                                  .substring(6);
        if (!aktJahr.equals(jahrHeute)) {
            /*
             * JOptionPane.showMessageDialog(null,
             * "Wichtiger Hinweis!!!!!\n\nDer letzte Programmstart war im Kalenderjahr -->"
             * +aktJahr+"\n"+
             * "Bitte fragen Sie den Administrator ob alle Befreiungen des Jahes "
             * +aktJahr+" zurückgesetzt wurden\n"+
             * "Beginnen Sie erst dann mit der Arbeit wenn sichergestellt ist daß alle Jahresabschlußarbeiten erledigt worden sind!!!!"
             * ); //System.out.println("Aktuelles Jahr wurde veränder auf "+jahrHeute);
             */
            aktJahr = String.valueOf(jahrHeute);

            inif.setStringProperty("SystemIntern", "AktJahr", jahrHeute, null);
            INITool.saveIni(inif);
        } else {
            // System.out.println("Aktuelles Jahr ist o.k.: "+jahrHeute);
        }
        vorJahr = Integer.valueOf(Integer.valueOf(aktJahr) - 1)
                         .toString();
        String umstellung = SqlInfo.holeEinzelFeld("select altesjahr from jahresabschluss LIMIT 1");
        if ((Integer.parseInt(jahrHeute) - Integer.parseInt(umstellung)) > 1) {
            String htmlstring = "<html><b><font color='#ff0000'>Achtung !</font><br><br>Die Umstellung der Rezeptgebührbefreiungen aus<br>"
                    + "dem <font color='#ff0000'>Kalenderjahr " + vorJahr + "</font> "
                    + "wurde noch nicht durchgeführt.<br><br>Rezeptebühren und Kassenabrechnung können deshalb fehlerhaft sein.<br><br>"
                    + "Bitte informieren Sie den Systemadministrator umgehend<br><br>"
                    + "Sollten Sie die Berechtigung für die Umstellung haben, <font color='#ff0000'>stellen Sie bitte selbst um:</font><br>"
                    + "System-Initialisierung -> sonstige Einstellungen -> Befreiungen zurücksetzen/Jahreswechsel</b></html>";
            JOptionPane.showMessageDialog(null, htmlstring);
        }
    }

    public static void ArschGeigenTest() {
        // public static HashMap<String,Object> hmArschgeigenModus = new
        // HashMap<String,Object>();
        // public static Vector<Vector<String>> vArschgeigenDaten = new
        // Vector<Vector<String>>();
        if (new File(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/arschgeigen.ini").exists()) {
            Settings inif = INITool.openIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK() + "/",
                    "arschgeigen.ini");
            int anzahlag = inif.getIntegerProperty("Arschgeigen", "AnzahlArschgeigen");
            int anzahliks;
            Vector<String> vDummy = new Vector<String>();
            for (int i = 0; i < anzahlag; i++) {
                vDummy.clear();
                anzahliks = inif.getIntegerProperty("Arschgeigen" + Integer.toString(i + 1), "ArschgeigenAnzahl");
                for (int i2 = 0; i2 < anzahliks; i2++) {
                    vDummy.add(inif.getStringProperty("Arschgeigen" + Integer.toString(i + 1),
                            "ArschgeigenIK" + Integer.toString(i2 + 1)));
                }
                /*
                 * ArschgeigenModus=4 ArschgeigenStichtag=01.01.2012 ArschgeigenTarifgruppeAlt =
                 * 8 ArschgeigenTarifgruppeNeu = 2
                 */
                vArschgeigenDaten.add((Vector<String>) vDummy.clone());
                hmArschgeigenModus.put("Modus" + Integer.toString(i),
                        inif.getIntegerProperty("Arschgeigen" + Integer.toString(i + 1), "ArschgeigenModus"));
                hmArschgeigenModus.put("Stichtag" + Integer.toString(i),
                        inif.getStringProperty("Arschgeigen" + Integer.toString(i + 1), "ArschgeigenStichtag"));
                hmArschgeigenModus.put("Tarifalt" + Integer.toString(i),
                        inif.getIntegerProperty("Arschgeigen" + Integer.toString(i + 1), "ArschgeigenTarifgruppeAlt")
                                - 1);
                hmArschgeigenModus.put("Tarifneu" + Integer.toString(i),
                        inif.getIntegerProperty("Arschgeigen" + Integer.toString(i + 1), "ArschgeigenTarifgruppeNeu")
                                - 1);
            }

        }
    }

    /**
     * checkt, ob Schlüssel iniKey in der ini-Datei enthalten ist, liefert dessen
     * int-Wert oder 0
     *
     * @param ini2use zu prüfende ini-Datei
     * @param iniSect zu prüfende Sektion in der Datei
     * @param iniKey  gesuchter Schlüssel
     * @return Wert
     */
    static int testIntIni(Settings ini2use, String iniSect, String iniKey) {
        int xscale = 0;
        try {
            xscale = ini2use.getIntegerProperty(iniSect, iniKey);
        } catch (Exception ex) {
            //ignore
        }
        return xscale;
    }

    public static void Feiertage() {
        vFeiertage = SqlInfo.holeFeld(
                "select datsql from feiertage where jahr >= '" + aktJahr + "' AND " + "buland <> ''");
    }


    /*******************************************************************************/
    public static boolean searchExtended() {
        if (hmPatientenSuchenDlgIni.get("erweiterteUmlautSuche") == 0) {
            return false;
        }
        return true;
    }

}

/*****************************************/
