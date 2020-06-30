package rezept;

import stammDatenTools.ZuzahlTools;
import systemEinstellungen.SystemConfig;

public interface Zuzahlung {

    // TODO: use some get-/set-RezToStatus-way, or at least enum
    // TODO: seems like stammdatentools/ZuzahlTools also defines these (as enum)
    /**
     * Aforementioned stammdatentools/ZuzahlTools.java seems to be used to set the icons in Rezept-table
     * it's use (as of 05'2020):
     * <BR/>ZUZAHLFREI = 0
     * <BR/>ZUZAHLOK = 1
     * <BR/>ZUZAHLNICHTOK, -> icon zuzahlnichtok (no int to icon value ass.)
     * <BR/>ZUZAHLRGR = 2 - if RezNr in quest. found in rgaffaktura -> icon = ZUZAHLRGR, else ZUZAHLNICHTOK
     * <BR/>ZUZAHLNOTSET -> icon "kleinehilfe"
     * <BR/> 
     *  The icons are then resolved via icons.ini
     *  
     * <BR/>Also, in PatientHauptLogic.java, this is used:
     * <BR/>            ZuzahlTools.setZzIcons(); // soll peu-a-peu 'die da' ersetzen:
     * <BR/>            patientHauptPanel.imgzuzahl[0] = SystemConfig.hmSysIcons.get("zuzahlfrei");
     * <BR/>            patientHauptPanel.imgzuzahl[1] = SystemConfig.hmSysIcons.get("zuzahlok");
     * <BR/>            patientHauptPanel.imgzuzahl[2] = SystemConfig.hmSysIcons.get("zuzahlnichtok");
     * <BR/>            patientHauptPanel.imgzuzahl[3] = SystemConfig.hmSysIcons.get("kleinehilfe");
     */
    int ZZSTATUS_NOTSET = -1;
    int ZZSTATUS_BEFREIT = 0;
    int ZZSTATUS_OK = 1;
    int ZZSTATUS_NOTOK = 2;
    int ZZSTATUS_BALD18 = 3;

}