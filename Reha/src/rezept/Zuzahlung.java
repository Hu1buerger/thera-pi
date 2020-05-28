package rezept;

import stammDatenTools.ZuzahlTools;
import systemEinstellungen.SystemConfig;

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
     *  
     *  Also, in PatientHauptLogic.java, this is used:
     *              ZuzahlTools.setZzIcons(); // soll peu-a-peu 'die da' ersetzen:
     *              patientHauptPanel.imgzuzahl[0] = SystemConfig.hmSysIcons.get("zuzahlfrei");
     *              patientHauptPanel.imgzuzahl[1] = SystemConfig.hmSysIcons.get("zuzahlok");
     *              patientHauptPanel.imgzuzahl[2] = SystemConfig.hmSysIcons.get("zuzahlnichtok");
     *              patientHauptPanel.imgzuzahl[3] = SystemConfig.hmSysIcons.get("kleinehilfe");
     */
    int ZZSTATUS_NOTSET = -1;
    int ZZSTATUS_BEFREIT = 0;
    int ZZSTATUS_OK = 1;
    int ZZSTATUS_NOTOK = 2;
    int ZZSTATUS_BALD18 = 3;

}