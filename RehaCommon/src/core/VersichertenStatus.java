package core;

import com.sun.star.lang.IllegalArgumentException;

public enum VersichertenStatus {
    VERSICHERTER(1),
    FAMILIE(3),
    RENTNER(5),
    INVALID(-1);



    private int nummer;

    VersichertenStatus(int i) {
        this.nummer = i;
    }

    public int getNummer() {
        return nummer;
    }

    /**Gibt den passenden Versichertenstatus zur Ziffer zurueck.
     *
     * @param ziffer
     * @return
     * @throws IllegalArgumentException wenn die ziffer nicht 1 3 oder 5 ist.
     */
   public static VersichertenStatus of(int ziffer) throws IllegalArgumentException {
        for (VersichertenStatus status : values()) {
            if (ziffer == status.nummer)
                return status;
        }
        return INVALID;
   }

}
