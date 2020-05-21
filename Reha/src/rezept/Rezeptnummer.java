/**
 * 
 */
package rezept;

import java.util.Objects;

import core.Disziplin;

/**
 * @author keith
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
        if ( rezNr == null)
            return;
        String diszi2check = rezNr.substring(0, 2);
        for ( Disziplin d : Disziplin.values()) {
            if (diszi2check.equalsIgnoreCase(d.toString())) {
                disziplin = d;
                break;
            }
        };
        
        this.rezeptZiffern = Integer.parseInt(rezNr.substring(2));
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((disziplin == null) ? 0 : disziplin.hashCode());
        return prime * result + Integer.hashCode(rezeptZiffern);
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
