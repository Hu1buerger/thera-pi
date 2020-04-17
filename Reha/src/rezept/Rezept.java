package rezept;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.function.BiConsumer;

public class Rezept {
    int PAT_INTERN;
    String REZ_NR;
    LocalDate REZ_DATUM;
    int ANZAHL1;
    int ANZAHL2;
    int ANZAHL3;
    int ANZAHL4;
    BigDecimal ANZAHLKM;
    int ART_DBEH1;
    int ART_DBEH2;
    int ART_DBEH3;
    int ART_DBEH4;
    boolean BEFR;
    Money REZ_GEB;

    boolean REZ_BEZ;
    String ARZT;
    int ARZTID;
    String AERZTE;
    Money PREISE1;
    Money PREISE2;
    Money PREISE3;
    Money PREISE4;
    LocalDate DATUM;
    String DIAGNOSE;
    boolean HEIMBEWOHN;
    LocalDate VERAENDERD;
    int VERAENDERA;
    String REZEPTART; // erstverordn, VO oder Adr
    boolean LOGFREI1;
    boolean LOGFREI2;
    int NUMFREI1;
    int NUMFREI2;
    String CHARFREI1;
    String CHARFREI2;
    String TERMINE;
    int ID;
    String KTRAEGER;
    int KID;
    int PATID;
    int ZZSTATUS;
    LocalDate LASTDATE;
    int PREISGRUPPE;
    boolean BEGRUENDADR;
    boolean HAUSBES;
    String INDIKATSCHL;
    String ANGELEGTVON;
    int BARCODEFORM;
    String DAUER;
    String POS1;
    String POS2;
    String POS3;
    String POS4;
    String FREQUENZ;
    String LASTEDIT;
    int BERID;
    boolean ARZTBERICHT;
    LocalDate LASTEDDATE;
    String FARBCODE;
    String RSPLIT;
    String JAHRFREI; // vielleicht auch localdate
    boolean UNTER18;
    boolean HBVOLL;
    boolean ABSCHLUSS;
    int ZZREGEL = -1;
    int ANZAHLHB;
    String KUERZEL1;
    String KUERZEL2;
    String KUERZEL3;
    String KUERZEL4;
    String KUERZEL5;
    String KUERZEL6;
    String ICD10;
    String ICD10_2;

    @Override
    public String toString() {
        return "Rezept [PAT_INTERN=" + PAT_INTERN + ", REZ_NR=" + REZ_NR + ", REZ_DATUM=" + REZ_DATUM + ", ANZAHL1="
                + ANZAHL1 + ", ANZAHL2=" + ANZAHL2 + ", ANZAHL3=" + ANZAHL3 + ", ANZAHL4=" + ANZAHL4 + ", ANZAHLKM="
                + ANZAHLKM + ", ART_DBEH1=" + ART_DBEH1 + ", ART_DBEH2=" + ART_DBEH2 + ", ART_DBEH3=" + ART_DBEH3
                + ", ART_DBEH4=" + ART_DBEH4 + ", BEFR=" + BEFR + ", REZ_GEB=" + REZ_GEB + ", REZ_BEZ=" + REZ_BEZ
                + ", ARZT=" + ARZT + ", ARZTID=" + ARZTID + ", AERZTE=" + AERZTE + ", PREISE1=" + PREISE1 + ", PREISE2="
                + PREISE2 + ", PREISE3=" + PREISE3 + ", PREISE4=" + PREISE4 + ", DATUM=" + DATUM + ", DIAGNOSE="
                + DIAGNOSE + ", HEIMBEWOHN=" + HEIMBEWOHN + ", VERAENDERD=" + VERAENDERD + ", VERAENDERA=" + VERAENDERA
                + ", REZEPTART=" + REZEPTART + ", LOGFREI1=" + LOGFREI1 + ", LOGFREI2=" + LOGFREI2 + ", NUMFREI1="
                + NUMFREI1 + ", NUMFREI2=" + NUMFREI2 + ", CHARFREI1=" + CHARFREI1 + ", CHARFREI2=" + CHARFREI2
                + ", TERMINE=" + TERMINE + ", ID=" + ID + ", KTRAEGER=" + KTRAEGER + ", KID=" + KID + ", PATID=" + PATID
                + ", ZZSTATUS=" + ZZSTATUS + ", LASTDATE=" + LASTDATE + ", PREISGRUPPE=" + PREISGRUPPE
                + ", BEGRUENDADR=" + BEGRUENDADR + ", HAUSBES=" + HAUSBES + ", INDIKATSCHL=" + INDIKATSCHL
                + ", ANGELEGTVON=" + ANGELEGTVON + ", BARCODEFORM=" + BARCODEFORM + ", DAUER=" + DAUER + ", POS1="
                + POS1 + ", POS2=" + POS2 + ", POS3=" + POS3 + ", POS4=" + POS4 + ", FREQUENZ=" + FREQUENZ
                + ", LASTEDIT=" + LASTEDIT + ", BERID=" + BERID + ", ARZTBERICHT=" + ARZTBERICHT + ", LASTEDDATE="
                + LASTEDDATE + ", FARBCODE=" + FARBCODE + ", RSPLIT=" + RSPLIT + ", JAHRFREI=" + JAHRFREI + ", UNTER18="
                + UNTER18 + ", HBVOLL=" + HBVOLL + ", ABSCHLUSS=" + ABSCHLUSS + ", ZZREGEL=" + ZZREGEL + ", ANZAHLHB="
                + ANZAHLHB + ", KUERZEL1=" + KUERZEL1 + ", KUERZEL2=" + KUERZEL2 + ", KUERZEL3=" + KUERZEL3
                + ", KUERZEL4=" + KUERZEL4 + ", KUERZEL5=" + KUERZEL5 + ", KUERZEL6=" + KUERZEL6 + ", ICD10=" + ICD10
                + ", ICD10_2=" + ICD10_2 + "]";
    }

    public Vector<ArrayList<?>> positionenundanzahl() {

        Map<String, Heilmittel> heilm = new LinkedHashMap<>();

        Vector<ArrayList<?>> vec = new Vector<>();
        ArrayList<String> arrayPos = new ArrayList<String>();
        ArrayList<Integer> anzahl = new ArrayList<Integer>();
        ArrayList<Integer> artderBeh = new ArrayList<>();
        if (POS1 != null && !"".equals(POS1)) {
            heilm.put(POS1, new Heilmittel(POS1, ANZAHL1, ART_DBEH1));

        }
        if (POS2 != null && !"".equals(POS2)) {
            if (heilm.containsKey(POS2)) {
                heilm.get(POS2).Anzahl += ANZAHL2;
            } else {
                heilm.put(POS2, new Heilmittel(POS2, ANZAHL2, ART_DBEH2));
            }
        }
        if (POS3 != null && !"".equals(POS3)) {
            if (heilm.containsKey(POS3)) {
                heilm.get(POS3).Anzahl += ANZAHL3;
            } else {
                heilm.put(POS3, new Heilmittel(POS3, ANZAHL3, ART_DBEH3));
            }

        }
        if (POS4 != null && !"".equals(POS4)) {
            if (heilm.containsKey(POS4)) {
                heilm.get(POS4).Anzahl += ANZAHL3;
            } else {
                heilm.put(POS4, new Heilmittel(POS4, ANZAHL4, ART_DBEH4));
            }

        }

        BiConsumer<? super String, ? super Heilmittel> action = new BiConsumer<String, Heilmittel>() {

            @Override
            public void accept(String t, Heilmittel u) {
                arrayPos.add(u.hmNummer);
                anzahl.add(u.Anzahl);
                artderBeh.add(u.artderBehandlung);

            }
        };
        heilm.forEach(action);

        vec.add(arrayPos);
        vec.add(anzahl);
        vec.add(artderBeh);
        return vec;
    }

}
