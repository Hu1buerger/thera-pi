package rezept;

public interface Zuzahlung {

    // TODO: use some get-/set-RezToStatus-way, or at least enum
    // TODO: seems like stammdatentools/ZuzahlTools also defines these (as enum)
    int ZZSTATUS_NOTSET = -1;
    int ZZSTATUS_BEFREIT = 0;
    int ZZSTATUS_OK = 1;
    int ZZSTATUS_NOTOK = 2;
    int ZZSTATUS_BALD18 = 3;

}