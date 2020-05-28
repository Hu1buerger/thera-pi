package rezept;

public interface Zuzahlung {

    // TODO: use some get-/set-RezToStatus-way, or at least enum
    // TODO: seems like stammdatentools/ZuzahlTools also defines these (as enum)
    /**
     * Aforementioned stammdatentools/ZuzahlTools.java seems to be used to set the icons in Rezept-table
     * it's use (as of 05'2020):
     *  ZUZAHLFREI = 0
     *  ZUZAHLOK = 1
     *  ZUZAHLNICHTOK, -> icon zuzahlnichtok (no int to icon value ass.)
     *  ZUZAHLRGR = 2 - if RezNr in quest. found in rgaffaktura -> icon = ZUZAHLRGR, else ZUZAHLNICHTOK
     *  ZUZAHLNOTSET -> icon "kleinehilfe"
     *  
     *  The icons are then resolved via icons.ini
     */
    int ZZSTATUS_NOTSET = -1;
    int ZZSTATUS_BEFREIT = 0;
    int ZZSTATUS_OK = 1;
    int ZZSTATUS_NOTOK = 2;
    int ZZSTATUS_BALD18 = 3;

}