package systemEinstellungen;

import java.awt.Color;
import java.util.ArrayList;

public final class TKSettings {

    /**
     * nachfolgende static's sind notwendig für den Einsatz des Terminkalenders
     */
    public static ArrayList<ArrayList<ArrayList<String[]>>> aTerminKalender;
    public static String KalenderStartNADefaultSet = "./.";
    public static int AnzahlKollegen;
    public static Color KalenderHintergrund = null;
    public static boolean KalenderBarcode = false;
    public static boolean KalenderLangesMenue = false;
    public static boolean KalenderStartWochenAnsicht = false;
    public static String KalenderStartWADefaultUser = "./.";
    public static boolean KalenderZeitLabelZeigen = false;
    public static boolean KalenderWochenTagZeigen = false;
    public static boolean KalenderTimeLineZeigen = false;
    public static String[] KalenderUmfang = { null, null };
    public static long[] KalenderMilli = { 0, 0 };
    public static int UpdateIntervall;
    public static float KalenderAlpha = 0.0f;

}
