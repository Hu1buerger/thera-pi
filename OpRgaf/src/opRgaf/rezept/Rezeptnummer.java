/**
 *
 */
package opRgaf.rezept;

import java.util.Objects;

import core.Disziplin;

/**
 * Small class to provide RezeptNr magic
 *
 */
public class Rezeptnummer {

    private Disziplin disziplin;
    private int rezeptZiffern;

    public Rezeptnummer() {
        disziplin = Disziplin.INV;
    }

    /**
     * PRE: expects a String in the format of e.g. "ER101"
     * POST: class members diszi and rezNr are set
     *
     * @param rezNr
     */
    public Rezeptnummer(String rezNr) {
        // TODO: handle malformed rezNr
        // - disziplin can't be found - set INV?
        // - parseInt screws up
        disziplin = Disziplin.INV;
        if ( rezNr == null || rezNr == "" )
            return;
        String diszi2check = rezNr.replaceAll("[0-9]", "");
        disziplin = ofShort(diszi2check);
        String rezNrDigits = rezNr.replaceAll("[a-zA-Z]", "");

        this.rezeptZiffern = Integer.parseInt(rezNrDigits);
    }


    public static Disziplin ofShort(String value) {
        try {
            return Disziplin.valueOf(value);
        } catch (Exception e) {
            return Disziplin.INV;
        }
    }


    // constructor only passing in INT -> take from sysconfig def. diszi or set inv?

    public Rezeptnummer(Disziplin disziplin, int rezeptZiffern) {
        // super();
        this.disziplin = disziplin;
        this.rezeptZiffern = rezeptZiffern;
    }

    public Disziplin disziplin() {
        return disziplin;
    }

    public int rezeptZiffern() {
        return rezeptZiffern;
    }

    public String rezeptNummer() {
        // TODO: if diszi == (COMMON || INV) => boing!
        return disziplin + Integer.toString(rezeptZiffern);
    }
    @Override
    public String toString() {
        return "Rezeptnummer [disziplin=" + disziplin + ", rezeptZiffern=" + rezeptZiffern + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(disziplin, rezeptZiffern);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Rezeptnummer other = (Rezeptnummer) obj;
        return disziplin == other.disziplin && rezeptZiffern == other.rezeptZiffern;
    }


}