package hauptFenster;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.uno.Exception;

import CommonTools.DatFunk;
import CommonTools.ExUndHop;
import CommonTools.FileTools;
import CommonTools.FireRehaError;
import CommonTools.RehaEvent;
import CommonTools.SqlInfo;
import geraeteInit.BarCodeScanner;
import systemEinstellungen.SystemConfig;
import systemEinstellungen.config.Datenbank;
import terminKalender.ParameterLaden;

/**************
 *
 * Thread zum Start der Datenbank
 *
 * @author admin
 *
 */

final class DatenbankStarten implements Runnable {
    Logger logger = LoggerFactory.getLogger(DatenbankStarten.class);

    private void StarteDB() {

        if (Reha.instance.conn != null) {
            try {
                Reha.instance.conn.close();
            } catch (final SQLException e) {
            }
        }
        final String sDB = "SQL";
        Datenbank datenbank = new Datenbank();
        try {
            if (sDB == "SQL") {

                new SocketClient().setzeInitStand("Datenbanktreiber installieren");
                Class.forName(datenbank. treiber())
                .newInstance();
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (sDB == "SQL") {

                new SocketClient().setzeInitStand("Datenbank initialisieren und öffnen");
                Reha.instance.conn = DriverManager.getConnection(datenbank.jdbcDB()
                        + "?jdbcCompliantTruncation=false&zeroDateTimeBehavior=convertToNull&autoReconnect=true",
                        datenbank. user(),
                        datenbank. password());
            }
            int nurmaschine = SystemConfig.dieseMaschine.toString()
                                                        .lastIndexOf("/");
            Reha.instance.sqlInfo.setConnection(Reha.instance.conn);
            new ExUndHop().setzeStatement("delete from flexlock where maschine like '%"
                    + SystemConfig.dieseMaschine.toString()
                                                .substring(0, nurmaschine)
                    + "%'");

            String db =datenbank. jdbcDB()
                                               .replace("jdbc:mysql://", "");
            db = db.substring(0, db.indexOf("/"));
            final String xdb = db;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {

                    try {

                        Reha.instance.dbLabel.setText(Version.aktuelleVersion + xdb);
                    } catch (NullPointerException ex) {
                        ex.printStackTrace();
                    }

                }

            });

            Reha.DbOk = true;
            try {
                Reha.testeNummernKreis();
                Reha.testeStrictMode();
                Reha.testeMaxAllowed();
                Reha.testeVoTables();

            } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                ex.printStackTrace();
            }

        } catch (final SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            Reha.DbOk = false;
            Reha.nachladenDB = -1;
            System.out.println("Fehler bei der Initialisierung der Datenbank");
            new FireRehaError(RehaEvent.ERROR_EVENT, "Datenbankfehler!",
                    new String[] { "Datenabankfehler, Fehlertext:", ex.getMessage() });
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {

                e1.printStackTrace();

            }

            if (Reha.nachladenDB == JOptionPane.YES_OPTION) {
                new Thread(new DbNachladen()).start();
            } else {
                // new FireRehaError(this,"Datenbankfehler",new String[] {"Fehlertext:","Die
                // Datenbank kann nicht gestartet werden"});
                new SocketClient().setzeInitStand(
                        "Fehler!!!! Datenbank kann nicht gestartet werden - Thera-Pi wird beendet");

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new SocketClient().setzeInitStand("INITENDE");

                System.exit(0);

            }
            return;
        }
        return;
    }






    @Override
    public void run() {
        int i = 0;
        while (!Reha.instance.splashok) {
            i = i + 1;
            if (i > 10) {
                break;
            }
            try {
                Thread.sleep(300);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        new SocketClient().setzeInitStand("Datenbank starten");
        StarteDB();
        if (Reha.DbOk) {
            Date zeit = new Date();
            String stx = "Insert into eingeloggt set comp='" + SystemConfig.dieseMaschine + "', zeit='"
                    + zeit.toString() + "', einaus='ein'";
            new ExUndHop().setzeStatement(stx);
            try {
                Thread.sleep(50);

                new SocketClient().setzeInitStand("Datenbank ok");

                ParameterLaden.Init();
                new SocketClient().setzeInitStand("Systemparameter laden");

                Reha.sysConf.SystemInit(3);

                ParameterLaden.Passwort();
                new SocketClient().setzeInitStand("Systemparameter ok");

                new SocketClient().setzeInitStand("Native Interface ok");

                Reha.sysConf.SystemInit(4);

                new SocketClient().setzeInitStand("Emailparameter");

                Reha.sysConf.SystemInit(6);

                new SocketClient().setzeInitStand("Roogle-Gruppen ok!");

                Reha.sysConf.SystemInit(7);

                new SocketClient().setzeInitStand("Verzeichnisse");

                new SocketClient().setzeInitStand("Mandanten-Daten einlesen");

                Reha.sysConf.SystemInit(11);

                Reha.sysConf.SystemInit(9);

                Thread.sleep(50);

                new SocketClient().setzeInitStand("HashMaps initialisieren");

                SystemConfig.HashMapsVorbereiten();

                Thread.sleep(50);

                new SocketClient().setzeInitStand("Desktop konfigurieren");

                SystemConfig.DesktopLesen();

                Thread.sleep(50);

                new SocketClient().setzeInitStand("Patientenstamm init");

                SystemConfig.PatientLesen();

                Thread.sleep(50);

                new SocketClient().setzeInitStand("Gerätetreiber initialiseieren");

                SystemConfig.GeraeteInit();

                Thread.sleep(50);

                new SocketClient().setzeInitStand("Arztgruppen einlesen");

                SystemConfig.ArztGruppenInit();

                Thread.sleep(50);

                new SocketClient().setzeInitStand("Rezeptparameter einlesen");

                SystemConfig.RezeptInit();

                new SocketClient().setzeInitStand("Bausteine für Therapie-Berichte laden");

                SystemConfig.TherapBausteinInit();

                // SystemConfig.compTest();

                new SocketClient().setzeInitStand("Fremdprogramme überprüfen");

                SystemConfig.FremdProgs();

                new SocketClient().setzeInitStand("Geräteliste erstellen");

                SystemConfig.GeraeteListe();

                SystemConfig.CompanyInit();

                FileTools.deleteAllFiles(new File(SystemConfig.hmVerzeichnisse.get("Temp")));
                if (SystemConfig.sBarcodeAktiv.equals("1")) {
                    try {
                        Reha.barcodeScanner = new BarCodeScanner(SystemConfig.sBarcodeCom);
                    } catch (Exception e) {
                        //// System.out.println("Barcode-Scanner konnte nicht installiert werden");
                    } catch (java.lang.Exception e) {
                        e.printStackTrace();
                    }
                }
                new SocketClient().setzeInitStand("Firmendaten einlesen");

                Vector<Vector<String>> vec = SqlInfo.holeFelder("select min(datum),max(datum) from flexkc");

                try {
                    Reha.kalMin = DatFunk.sDatInDeutsch((vec.get(0)
                                                            .get(0)));
                    Reha.kalMax = DatFunk.sDatInDeutsch((vec.get(0)
                                                            .get(1)));
                } catch (java.lang.Exception e) {
                    logger.info("kalmin und kalmax nicht gesetzt", e);
                }

                SystemConfig.FirmenDaten();

                new SocketClient().setzeInitStand("Gutachten Parameter einlesen");

                SystemConfig.GutachtenInit();

                SystemConfig.AbrechnungParameter();

                SystemConfig.BedienungIni_ReadFromIni();

                SystemConfig.OffenePostenIni_ReadFromIni();

                SystemConfig.JahresUmstellung();

                SystemConfig.Feiertage();

                // notwendig bis alle Überhangsrezepte der BKK-Gesundheit abgearbeitet sind.
                SystemConfig.ArschGeigenTest();

                SystemConfig.EigeneDokuvorlagenLesen();

                SystemConfig.IcalSettings();

                new Thread(new PreisListenLaden()).start();

                if (SystemConfig.sWebCamActive.equals("1")) {
                    Reha.instance.activateWebCam();
                }

            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (NullPointerException e2) {
                e2.printStackTrace();
            }
        } else {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws java.lang.Exception {
                    new SocketClient().setzeInitStand("INITENDE");
                    return null;
                }
            }.execute();

        }
    }
}
