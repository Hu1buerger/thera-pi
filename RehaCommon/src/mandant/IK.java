package mandant;

import java.util.Objects;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Das IK ist ein eindeutiges Merkmal für die Abrechnung medizinischer und
 * rehabilitativer Leistungen mit den Traegern der Sozialversicherung
 * (Krankenkassen, Berufsgenossen- schaften, Unfallkassen, Rentenversicherung,
 * Bundesagentur fuer Arbeit).
 * <p>
 *
 * Das IK besteht aus den Buchstaben "IK" driekt gefolgt von 9 Ziffern
 * <p>
 *
 * Ziffer
 * <p>
 * 1-2 Klassifikation
 * <p>
 * 3-4 Regionalbereich
 * <p>
 * 5-8 Seriennummer
 * <p>
 * 9 Pruefziffer (aus den Stellen 3 bis 8)
 * <p>
 *
 * Klassifikation:
 * <p>
 * Die Stellen 1 und 2 bezeichnen die Art der Institution oder die
 * Personengruppe.
 * <p>
 *
 * Regionalbereich:
 * <p>
 * Die Stellen 3 und 4 bezeichnen den Regionalbereich.
 * <p>
 *
 * Seriennummer:
 * <p>
 * Die Stellen 5 bis 8 enthalten die Seriennummer. Die Seriennummern sind
 * grundsaetzlich frei verwendbar, sofern nicht Seriennummern- Kontingente
 * festgelegt worden sind.
 * <p>
 *
 * Pruefziffer:
 * <p>
 * Die Stelle 9 enthält die aus den Stellen 3 bis 8 errechnete Pruefziffer (also
 * ohne Einbeziehung der Klassifikation). Die Berechnung erfolgt nach dem
 * Modulo-10-Verfahren von rechts beginnend mit der Gewich- tung 1.2.1.2.1.2.
 * <p>
 * Quelle :arge-ik.de
 **/
public class IK {
    private static final Pattern IK_FORMAT = Pattern.compile("\\d{9}");
    public static final IK INVALIDIK = new IK("INVALID") {

        @Override void logIfInvalid(String ik) {

        }

        @Override
                    public boolean isValid() {

                        return false;
                    }
    };
    private final String ik_ziffern;
    private final String wellFormed;
    private Logger logger = LoggerFactory.getLogger(IK.class);

    /**
     *
     * @param ik ein 9-stelliger String der ausschliesslich aus Ziffern besteht.
     */
    public IK(String ik) {

        logIfInvalid(ik);
        this.ik_ziffern = ik;
        this.wellFormed = "IK" + ik;

    }

    void logIfInvalid(String ik) {
        if (!IK_FORMAT.matcher(ik)
                      .matches()) {
            logger.error(ik + " is not a valid IK");
        }
    }

    public String digitString() {
        return ik_ziffern;
    }

    public String wellFormed() { // NO_UCD (unused code)
        return wellFormed;
    }

    /**
     *
     * @return true wenn die Nummer das Modulo-10-Verfahren besteht.
     */
    public boolean isValid() {
        char[] digitschar = ik_ziffern.toCharArray();
        int sum = 0;
        for (int i = 3; i <= 8; i++) {
            int numericValue = Character.getNumericValue(digitschar[i - 1]);
            int locSum = numericValue + numericValue * (i % 2);
            sum += locSum > 9 ? locSum - 9 : locSum;
        }
        return sum % 10 == Character.getNumericValue(digitschar[8]);
    }

    @Override
    public String toString() {
        return "IK [ik=" + ik_ziffern + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(ik_ziffern);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IK other = (IK) obj;
        return Objects.equals(ik_ziffern, other.ik_ziffern);
    }

}
