package CommonTools;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class DatFunk {

    private static final DateTimeFormatter inFormatter = DateTimeFormatter.ofPattern("d.M.yyyy");
    private static final DateTimeFormatter outFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static DateTimeFormatter reverseHyphonFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String sDatInDeutsch(String sJavaDat) {
        if (sJavaDat == null) {
            return "  .  .    ";
        }

        String[] splittArray = sJavaDat.split("-");
        return splittArray[2] + "." + splittArray[1] + "." + splittArray[0];
    }

    public static String sDatInSQL(String sDeutschDat) {
        return LocalDate.parse(sDeutschDat, inFormatter)
                        .format(reverseHyphonFormatter);

    }

    public static String sHeute() {
        return LocalDate.now()
                        .format(outFormatter);

    }

    public static String sDatPlusTage(String datum, int Tage) {
        LocalDate datum1 = LocalDate.parse(datum, inFormatter);
        LocalDate later = datum1.plusDays(Tage);

        return later.format(outFormatter);
    }

    public static String WochenTag(String datum) {
        LocalDate datum1 = LocalDate.parse(datum, inFormatter);
        return datum1.getDayOfWeek()
                     .getDisplayName(TextStyle.FULL, Locale.GERMAN);

    }

    public static int TagDerWoche(String sdatum) {

        LocalDate datum = LocalDate.parse(sdatum, inFormatter);
        return datum.getDayOfWeek()
                    .getValue();

    }

    /** calculates the number of days between sdatum1 and sdatum2.
     * @param sdatum1 string of the form "d.M.yyyy"
     * @param sdatum2 string of the form "d.M.yyyy"
     * @return positiv number if sdatum1 is before sdatum2.<P>
     *  0 if sdatum1 and sdatum2 are the same date.<P>
     * negativ number, if sdatum1 is after sdatum2.
     *
     * @throws NumberformatException
     *
     */
    public static long TageDifferenz(String sdatum1, String sdatum2) {
        LocalDate datum1 = LocalDate.parse(sdatum1, inFormatter);
        LocalDate datum2 = LocalDate.parse(sdatum2, inFormatter);

        return ChronoUnit.DAYS.between(datum1, datum2);
    }

    public static String WocheErster(String sdatum) {
        LocalDate ld = LocalDate.parse(sdatum, inFormatter);
        return ld.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                 .format(outFormatter);
    }

    public static String WocheLetzter(String sdatum) {
        LocalDate ld = LocalDate.parse(sdatum, inFormatter);
        return ld.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                 .format(outFormatter);

    }

    public static int KalenderWoche(String sdatum) {
        LocalDate ld = LocalDate.parse(sdatum, inFormatter);
        return ld.get(WeekFields.of(Locale.GERMANY)
                                .weekOfYear());
    }

    /**
     * converts a given date into its long value (milliseconds since 01011970)
     *
     * if the date is separated with hyphons it needs to be in format yyyy-M-d
     *
     * @param sdatum
     * @return
     */
    public static long DatumsWert(String sdatum) {
        LocalDate ld;
        if (sdatum.contains(".")) {
            ld = LocalDate.parse(sdatum, inFormatter);

        } else {
            ld = LocalDate.parse(sdatum, reverseHyphonFormatter);
        }

        ZoneId zoneId = ZoneId.systemDefault();
        return ld.atStartOfDay(zoneId)
                 .toInstant()
                 .toEpochMilli();
    }

    public static String WertInDatum(long ldatum) {

        Instant inst;
        if (ldatum == 0) {
            inst = Instant.now();
        } else {
            inst = Instant.ofEpochMilli(ldatum);
        }
        return inst.atZone(ZoneId.systemDefault())
                   .toLocalDate()
                   .format(outFormatter);
    }

    public static boolean GeradeWoche(String sdatum) {
        return (KalenderWoche(sdatum) % 2) == 0;
    }

    public static boolean Unter18(String bezugdat, String geburtstag) {
        LocalDate geb = LocalDate.parse(geburtstag, outFormatter);
        LocalDate bezug = LocalDate.parse(bezugdat, outFormatter);
        return ChronoUnit.YEARS.between(geb, bezug) < 18;
    }

    public static boolean Schaltjahr(int jahr) {
        return Year.isLeap(jahr);

    }

    public static int JahreDifferenz(String sAktuellesJahr, String sInputJahr) {
        LocalDate aktuell = LocalDate.parse(sAktuellesJahr, outFormatter);
        LocalDate vergleich = LocalDate.parse(sInputJahr, outFormatter);

        return (int) ChronoUnit.YEARS.between(vergleich, aktuell);
    }

}
