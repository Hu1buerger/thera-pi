package rezept;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Disziplin;

public class Rezept {
    private static final Logger logger = LoggerFactory.getLogger(Rezept.class);
    
    // "Erstverordnung", "Folgeverordnung", "Folgev. ausserhalb d.R."
    // TODO: Enum?
    public static final int REZART_ERSTVO = 0;
    public static final int REZART_FOLGEVO = 1;
    public static final int REZART_FOLGEVOADR = 2;

    Rezeptnummer rezNr;
    Disziplin disziplin;
    int id;
    int rezeptArt = -1; // erstverordn, VO oder Adr - why not as Enum?
    LocalDate rezDatum;

    int patIntern;
    int patId;

    int anzahl1;
    int anzahl2;
    int anzahl3;
    int anzahl4;
    int artDerBeh1;
    int artDerBeh2;
    int artDerBeh3;
    int artDerBeh4;

    boolean befr;
    Money rezGeb;
    boolean rezBez;

    String arzt;
    int arztId;
    int arztBetriebsnummer;
    String aerzte;

    Money preise1;
    Money preise2;
    Money preise3;
    Money preise4;

    LocalDate erfassungsDatum;
    
    String diagnose;
    
    boolean heimbewohn;
    LocalDate veraenderd;   // suspected dead meat
    int veraendera;         // suspected dead meat
    boolean logfrei1;
    boolean logfrei2;
    int numfrei1;
    int numfrei2;
    String charfrei1;
    String charfrei2;
    
    // TODO: Change to Behandlung-class
    String termine;
    // Behandlung termine;    
    
    String ktraeger;
    int kId = -1;
    
    int zzStatus = Zuzahlung.ZZSTATUS_NOTSET;
    int zzRegel = -1;
    
    LocalDate lastDate;
    int preisgruppe = -1;
    boolean begruendADR;
    boolean hausbes;
    int anzahlHb;
    boolean hbVoll;
    BigDecimal anzahlKM;
    
    String indikatSchl;
    String angelegtVon;
    String lastEditor;
    LocalDate lastEdDate;
    int barcodeform;
    String dauer;
    String pos1;
    String pos2;
    String pos3;
    String pos4;
    String frequenz;
    int berId = -1;
    boolean arztBericht;
    int farbcode = -1; // this is varChar in DB - but seems to be used (sometimes?) as int
    String rsplit;
    String jahrfrei; // vielleicht auch localdate
    boolean unter18;
    boolean abschluss;
    String kuerzel1;
    String kuerzel2;
    String kuerzel3;
    String kuerzel4;
    String kuerzel5;
    String kuerzel6;
    String icd10;
    String icd10_2;
    boolean pauschale;

    public static final int REZEPTART_ERSTVO = 0;
    public static final int REZEPTART_FOLGEVO = 1;
    public static final int REZEPTART_ADR = 2;
    
    // Auxiliary helpers
    public boolean isToBeInserted;
    
    public Rezept() {
        this.disziplin = Disziplin.INV;
        this.rezGeb = new Money();
        this.preise1 = new Money();
        this.preise2 = new Money();
        this.preise3 = new Money();
        this.preise4 = new Money();

    }
    
    /**
     * Copy Constructor - make a copy of a Rezept (copy as is e.g. no new RzNr!)
     * 
     * @param fromRez - source-Rezept to copy from
     */
    public Rezept(Rezept fromRez) {
        this.patIntern = fromRez.patIntern;
        this.disziplin = fromRez.disziplin;
        this.rezNr = fromRez.rezNr;
        this.rezDatum = fromRez.rezDatum;
        this.anzahl1 = fromRez.anzahl1;
        this.anzahl2 = fromRez.anzahl2;
        this.anzahl3 = fromRez.anzahl3;
        this.anzahl4 = fromRez.anzahl4;
        this.anzahlKM = fromRez.anzahlKM;
        this.artDerBeh1 = fromRez.artDerBeh1;
        this.artDerBeh2 = fromRez.artDerBeh2;
        this.artDerBeh3 = fromRez.artDerBeh3;
        this.artDerBeh4 = fromRez.artDerBeh4;
        this.befr = fromRez.befr;
        this.rezGeb = new Money(fromRez.rezGeb);
        
        this.rezBez = fromRez.rezBez;
        this.arzt = fromRez.arzt;
        this.arztId = fromRez.arztId;
        this.aerzte = fromRez.aerzte;
        this.preise1 = new Money(fromRez.preise1);
        this.preise2 = new Money(fromRez.preise2);
        this.preise3 = new Money(fromRez.preise3);
        this.preise4 = new Money(fromRez.preise4);
        this.erfassungsDatum = fromRez.erfassungsDatum;
        this.diagnose = fromRez.diagnose;
        this.heimbewohn = fromRez.heimbewohn;
        this.veraenderd = fromRez.veraenderd;
        this.veraendera = fromRez.veraendera;
        this.rezeptArt = fromRez.rezeptArt;
        this.logfrei1 = fromRez.logfrei1;
        this.logfrei2 = fromRez.logfrei2;
        this.numfrei1 = fromRez.numfrei1;
        this.numfrei2 = fromRez.numfrei2;
        this.charfrei1 = fromRez.charfrei1;
        this.charfrei2 = fromRez.charfrei2;
        this.termine = fromRez.termine;
        this.id = fromRez.id;
        this.ktraeger = fromRez.ktraeger;
        this.kId = fromRez.kId;
        this.patId = fromRez.patId;
        this.zzStatus = fromRez.zzStatus;
        this.zzRegel = fromRez.zzRegel;
        this.lastDate = fromRez.lastDate;
        this.preisgruppe = fromRez.preisgruppe;
        this.begruendADR = fromRez.begruendADR;
        this.hausbes = fromRez.hausbes;
        this.indikatSchl = fromRez.indikatSchl;
        this.angelegtVon = fromRez.angelegtVon;
        this.barcodeform = fromRez.barcodeform;
        this.dauer = fromRez.dauer;
        this.pos1 = fromRez.pos1;
        this.pos2 = fromRez.pos2;
        this.pos3 = fromRez.pos3;
        this.pos4 = fromRez.pos4;
        this.frequenz = fromRez.frequenz;
        this.lastEditor = fromRez.lastEditor;
        this.berId = fromRez.berId;
        this.arztBericht = fromRez.arztBericht;
        this.lastEdDate = fromRez.lastEdDate;
        this.farbcode = fromRez.farbcode;
        this.rsplit = fromRez.rsplit;
        this.jahrfrei = fromRez.jahrfrei;
        this.unter18 = fromRez.unter18;
        this.hbVoll = fromRez.hbVoll;
        this.abschluss = fromRez.abschluss;
        this.anzahlHb = fromRez.anzahlHb;
        this.kuerzel1 = fromRez.kuerzel1;
        this.kuerzel2 = fromRez.kuerzel2;
        this.kuerzel3 = fromRez.kuerzel3;
        this.kuerzel4 = fromRez.kuerzel4;
        this.kuerzel5 = fromRez.kuerzel5;
        this.kuerzel6 = fromRez.kuerzel6;
        this.icd10 = fromRez.icd10;
        this.icd10_2 = fromRez.icd10_2;
        this.pauschale = fromRez.pauschale;
    }
    
    /**
     * Liefert die Anzahl der gespeicherten Termine in einem Rezept
     * 
     * @return
     */
    public int AnzahlTermineInRezept() {
        if (getTermine() == null || getTermine().isEmpty())
            return 0;
        return getTermine().split("\n").length;
    }
    
    @Override
    public String toString() {
        return "Rezept [PAT_INTERN=" + patIntern + ", REZ_NR=" + rezNr + ", disziplin=" + disziplin + ", REZ_datum=" + rezDatum + ", anzahl1="
                + anzahl1 + ", anzahl2=" + anzahl2 + ", anzahl3=" + anzahl3 + ", anzahl4=" + anzahl4 + ", anzahlkm="
                + anzahlKM + ", art_dbeh1=" + artDerBeh1 + ", art_dbeh2=" + artDerBeh2 + ", art_dbeh3=" + artDerBeh3
                + ", art_dbeh4=" + artDerBeh4 + ", befr=" + befr + ", rezGeb=" + rezGeb + ", rezBez=" + rezBez
                + ", arzt=" + arzt + ", arztid=" + arztId + ", arztBetriebsnummer=" + arztBetriebsnummer + ", aerzte=" + aerzte + ", preise1=" + preise1 + ", preise2="
                + preise2 + ", preise3=" + preise3 + ", preise4=" + preise4 + ", erfassungsDatum=" + erfassungsDatum + ", diagnose="
                + diagnose + ", heimbewohn=" + heimbewohn + ", veraenderd=" + veraenderd + ", veraendera=" + veraendera
                + ", rezeptart=" + rezeptArt + ", logfrei1=" + logfrei1 + ", logfrei2=" + logfrei2 + ", numfrei1="
                + numfrei1 + ", numfrei2=" + numfrei2 + ", charfrei1=" + charfrei1 + ", charfrei2=" + charfrei2
                + ", termine=" + termine + ", id=" + id + ", ktraeger=" + ktraeger + ", Kid=" + kId + ", PATid=" + patId
                + ", zzstatus=" + zzStatus + ", zzregel=" + zzRegel + ", lastdate=" + lastDate + ", preisgruppe=" + preisgruppe
                + ", begruendadr=" + begruendADR + ", hausbes=" + hausbes + ", indikatschl=" + indikatSchl
                + ", angelegtvon=" + angelegtVon + ", barcodeform=" + barcodeform + ", dauer=" + dauer + ", pos1="
                + pos1 + ", pos2=" + pos2 + ", pos3=" + pos3 + ", pos4=" + pos4 + ", frequenz=" + frequenz
                + ", lastEditor=" + lastEditor + ", BERid=" + berId + ", arztBERICHT=" + arztBericht + ", lasteddate="
                + lastEdDate + ", farbcode=" + farbcode + ", rsplit=" + rsplit + ", jahrfrei=" + jahrfrei + ", unter18="
                + unter18 + ", hbvoll=" + hbVoll + ", abschluss=" + abschluss + ", anzahlhb="
                + anzahlHb + ", kuerzel1=" + kuerzel1 + ", kuerzel2=" + kuerzel2 + ", kuerzel3=" + kuerzel3
                + ", kuerzel4=" + kuerzel4 + ", kuerzel5=" + kuerzel5 + ", kuerzel6=" + kuerzel6 + ", icd10=" + icd10
                + ", icd10_2=" + icd10_2 + ", useHygenePauschale= " + pauschale + "]";
    }

    public Vector<ArrayList<?>> positionenundanzahl() {

        Map<String, Heilmittel> heilm = new LinkedHashMap<>();

        Vector<ArrayList<?>> vec = new Vector<>();
        ArrayList<String> arrayPos = new ArrayList<String>();
        ArrayList<Integer> anzahl = new ArrayList<Integer>();
        ArrayList<Integer> artderBeh = new ArrayList<>();
        berechneGesamtHeilmittel(heilm, pos1, anzahl1, artDerBeh1);
        berechneGesamtHeilmittel(heilm, pos2, anzahl2, artDerBeh2);
        berechneGesamtHeilmittel(heilm, pos3, anzahl3, artDerBeh3);
        berechneGesamtHeilmittel(heilm, pos4, anzahl4, artDerBeh4);



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

    private void berechneGesamtHeilmittel(Map<String, Heilmittel> heilm, String positionsnummer  , int menge,
            int artDerBehandlung) {
        if (positionsnummer != null && !"".equals(positionsnummer)) {

            if (heilm.containsKey(positionsnummer)) {
                heilm.get(positionsnummer).Anzahl += menge;
            } else {
                heilm.put(positionsnummer, new Heilmittel(positionsnummer, menge, artDerBehandlung));
            }

        }
    }
    
    /**
     * Returns the first 2 chars of RezNr, expecting Rezepte to be of the format e.g. "ER101"
     * 
     * @return String e.g. "ER"
     */
    public String getRezClass() {
        // This will only work if rezNr != null
        // TODO: adjust to disziplin-class - maybe return INVALID on null?
        return (getRezNr() == null ? null : getRezNr().substring(0, 2)
                                                         .toUpperCase());
    }
    
    /**
     * Return the ArtDBehX where X is passed in int i
     * @param i - The index at which to retrieve ADB
     * @return the int at index i
     */
    public int getArtDerBehandlung(int i) {
        switch (i) {
        case 1:
            return getArtDerBeh1();
        case 2:
            return getArtDerBeh2();
        case 3:
            return getArtDerBeh3();
        case 4:
            return getArtDerBeh4();
        default:
            logger.error("Index " + i + " ist ungueltig. - only 1-4 are impl. so far");
            return -1;
        
        }
    }

    /**
     * Returns an array of ints containing all 4 fields "anzahlN" in order
     * 
     * @return int[] of anzahl1-4
     */
    public int[] getAnzahlAlle() {
        return new int[] { anzahl1, anzahl2, anzahl3, anzahl4};
    }
    

    /**
     * Public standard getter/setters
     *  if member is of type boolean there are 3 possible types:
     * 
     *  1. isField() - will return a bool
     *  2. getField() - will return a String repr. of the bool in the form of "T"/"F"
     *  3. setField() - set field via bool
     */
    
    /**
     * @return the rezNr
     */
    public String getRezNr() {
        // TODO: Nasty things happen here if Diszi is invalid (or NULL!)
        return ( rezNr == null ? null : rezNr.rezeptNummer());
    }

    /**
     * @return the iD
     */
    public int getId() {
        return id;
    }

    /**
     * @param the iD
     */
    public void setId(int ID) {
        id = ID;
    }
    
    /**
     * @return the rezeptArt
     */
    public int getRezeptArt() {
        return rezeptArt;
    }

    /**
     * @param rezeptArt the rezeptArt to set
     */
    public void setRezeptArt(int rezeptArt) {
        this.rezeptArt = rezeptArt;
    }

    /**
     * @param rezNr the rezNr to set
     */
    public void setRezNr(String rezNr) {
        this.rezNr = new Rezeptnummer(rezNr);
    }

    /**
     * @return the rezDatum
     */
    public LocalDate getRezDatum() {
        return rezDatum;
    }

    /**
     * set the rezDatum
     */
    public void setRezDatum(LocalDate Datum) {
        rezDatum = Datum;
    }
    
    /**
     * set the rezDatum
     *
    // TODO: make sure we get & set Datum in correct format.
    public void setRezDatum(String Datum) {
        rezDatum = LocalDate.parse(Datum);
    }
    */
    
    /**
     * @return the patIntern
     */
    public int getPatIntern() {
        return patIntern;
    }

    /**
     * Set the patIntern
     */
    public void setPatIntern(int internId) {
        patIntern = internId;
    }

    
    /**
     * @return the patId
     */
    public int getPatId() {
        return patId;
    }

    /**
     * Set the patId
     */
    public void setPatId(int patID) {
        patId = patID;
    }

    
    /**
     * @return the anzahl1
     */
    public int getBehAnzahl1() {
        return anzahl1;
    }

    /**
     * @param anzahl1 the anzahl1 to set
     */
    public void setBehAnzahl1(int anzahl1) {
        this.anzahl1 = anzahl1;
    }

    /**
     * @return the anzahl2
     */
    public int getBehAnzahl2() {
        return anzahl2;
    }

    /**
     * @param anzahl2 the anzahl2 to set
     */
    public void setBehAnzahl2(int anzahl2) {
        this.anzahl2 = anzahl2;
    }

    /**
     * @return the anzahl3
     */
    public int getBehAnzahl3() {
        return anzahl3;
    }

    /**
     * @param anzahl3 the anzahl3 to set
     */
    public void setBehAnzahl3(int anzahl3) {
        this.anzahl3 = anzahl3;
    }

    /**
     * @return the anzahl4
     */
    public int getBehAnzahl4() {
        return anzahl4;
    }

    /**
     * @param anzahl4 the anzahl4 to set
     */
    public void setBehAnzahl4(int anzahl4) {
        this.anzahl4 = anzahl4;
    }

    /**
     * Takes an index indicating which BehandlungsAnzahl to retrieve.
     * Will return -1 on invalid index (0 < idx < 5)
     * 
     * @param idx
     * @return
     */
    public int getBehAnzahl(int idx) {
        switch (idx) {
            case 1:
                return getBehAnzahl1();
            case 2:
                return getBehAnzahl2();
            case 3:
                return getBehAnzahl3();
            case 4:
                return getBehAnzahl4();
            default:
                logger.error("Invalid index for getBehAnzahl received. "
                             + "Only index 1-4 is currently implemented. Requested: " + idx);
                return -1;
        }
    }
    
    /**
     * Returns an array of ints containing all 4 fields "artDerBehN" in order
     * 
     * @return int[] of artDerBeh1-4
     */
    public int[] getArtDerBehAlle() {
        return new int[] {artDerBeh1, artDerBeh2, artDerBeh3, artDerBeh4 };
    }
    
    /**
     * @return the artDerBeh1
     */
    public int getArtDerBeh1() {
        return artDerBeh1;
    }

    /**
     * @param artDerBeh1 the artDerBeh1 to set
     */
    public void setArtDerBeh1(int artDerBeh1) {
        this.artDerBeh1 = artDerBeh1;
    }

    /**
     * @return the artDerBeh2
     */
    public int getArtDerBeh2() {
        return artDerBeh2;
    }

    /**
     * @param artDerBeh2 the artDerBeh2 to set
     */
    public void setArtDerBeh2(int artDerBeh2) {
        this.artDerBeh2 = artDerBeh2;
    }

    /**
     * @return the artDerBeh3
     */
    public int getArtDerBeh3() {
        return artDerBeh3;
    }

    /**
     * @param artDerBeh3 the artDerBeh3 to set
     */
    public void setArtDerBeh3(int artDerBeh3) {
        this.artDerBeh3 = artDerBeh3;
    }

    /**
     * @return the artDerBeh4
     */
    public int getArtDerBeh4() {
        return artDerBeh4;
    }

    /**
     * @param artDerBeh4 the artDerBeh4 to set
     */
    public void setArtDerBeh4(int artDerBeh4) {
        this.artDerBeh4 = artDerBeh4;
    }

    /**
     * Sets 1 of the 4 ArtDerBehandlungen by index to value
     * 
     * @param idx Which field to set (ArtDerBehan[1-4])
     * @param value The value to set the field to
     */
    public void setArtDerBeh(int idx, int value) {
        switch (idx) {
            case 1:
                setArtDerBeh1(value);
                break;
            case 2:
                setArtDerBeh2(value);
                break;
            case 3:
                setArtDerBeh3(value);
                break;
            case 4:
                setArtDerBeh4(value);
                break;
            default:
                logger.error("Index " + idx + " ist ungueltig. Can only set ArtDerBehandlungen 1-4");
                
        }
    }
    
    /**
     * @return the anzahlKM
     */
    public BigDecimal getAnzahlKM() {
        return anzahlKM;
    }

    /**
     * Set the Entfernung für HB in KiloMetern
     * 
     * @param BigDecimal for KM
     */
    public void setAnzahlKM(BigDecimal km) {
        anzahlKM = km;
    }

    /**
     * @return the befr
     */
    public boolean isBefr() {
        return befr;
    }

    /**
     * Get befr bool as String "T"/"F". To use the bool directly, call isBefr()
     * 
     * @return befr as String
     */
    public String getBefr() {
        return isBefr() ? "T" : "F";
    }
    
    /**
     * @param befr the befr to set
     */
    public void setBefr(boolean befr) {
        this.befr = befr;
    }

    /**
     * @return the RezeptGebuehr (amount in Euro)
     */
    public Money getRezGeb() {
        return rezGeb;
    }

    /**
     * @param rezGeb the rezGeb to set
     */
    public void setRezGeb(Money rezGeb) {
        this.rezGeb = rezGeb;
    }

    /**
     * Rezept(Gebuehr) bezahlt? To get a String "T"/"F" use getRezBez
     * @return the rezBez
     */
    public boolean isRezBez() {
        return rezBez;
    }

    /**
     * Returns a String "T"/"F" depending on bool rezBez. To use the bool directly, call isRezBez()
     * 
     * @return String "T"/"F" if rezBez
     */
    public String getRezBez() {
        if (isRezBez())
            return "T";
        else
            return "F";
    }
    
    /**
     * Rezept(Gebuehr) bezahlt?
     * @param rezBez the rezBez to set
     */
    public void setRezBez(boolean rezBez) {
        this.rezBez = rezBez;
    }

    /**
     * @return the arzt
     */
    public String getArzt() {
        return arzt;
    }

    /**
     * Set the Arzt(-Name?) as String
     * @param the ArztId as String
     */
    public void setArzt(String Arzt) {
        arzt = Arzt;
    }
    
    /**
     * @return the ArztId
     */
    public int getArztId() {
        return arztId;
    }

    /**
     * Set the Arzt-ID as int
     * @param the ArztId as int
     */
    public void setArztId(int ArztId) {
        arztId = ArztId;
    }
    
    /**
     * @return the aerzte
     */
    public String getAerzte() {
        return aerzte;
    }

    /**
     * @param the aerzte
     */
    public void setAerzte(String Aerzte) {
        aerzte = Aerzte;
    }

    public int getArztBetriebsnummer() {
        return arztBetriebsnummer;
    }
    
    public void setArztBetriebsnummer(int aBN) {
        arztBetriebsnummer = aBN;
    }
    
    /**
     * @return the preise1
     */
    public Money getPreise1() {
        return preise1;
    }

    /**
     * Set the preise1
     */
    public void setPreise1(Money preis) {
        preise1 = preis;
    }

    
    /**
     * @return the preise2
     */
    public Money getPreise2() {
        return preise2;
    }
    
    /**
     * Set the preise2
     */
    public void setPreise2(Money preis) {
        preise2 = preis;
    }


    /**
     * @return the preise3
     */
    public Money getPreise3() {
        return preise3;
    }
    
    /**
     * Set the preise3
     */
    public void setPreise3(Money preis) {
        preise3 = preis;
    }

    

    /**
     * @return the preise4
     */
    public Money getPreise4() {
        return preise4;
    }

    /**
     * Set the preise4
     */
    public void setPreise4(Money preis) {
        preise4= preis;
    }

    /**
     * Gets 1 of the 4 Preise by index
     * 
     * @param idx Which field to get (preise[1-4])
     */
    public Money getPreis(int idx) {
        switch (idx) {
            case 1:
                return getPreise1();
            case 2:
                return getPreise2();
            case 3:
                return getPreise3();
            case 4:
                return getPreise4();
            default:
                logger.error("Can only get Preise 1-4. You tried: " + idx);
                return new Money("0");
        }
    }
    
    /**
     * Sets 1 of the 4 Preise by index to value
     * 
     * @param idx Which field to set (preise[1-4])
     * @param value The value to set the field to
     */
    public void setPreis(int idx, Money value) {
        switch (idx) {
            case 1:
                setPreise1(value);
                break;
            case 2:
                setPreise2(value);
                break;
            case 3:
                setPreise3(value);
                break;
            case 4:
                setPreise4(value);
                break;
            default:
                logger.error("Can only set Preise 1-4. You tried: " + idx);
        }
    }

    /**
     * @return the diagnose
     */
    public String getDiagnose() {
        return diagnose;
    }

    /**
     * Set the diagnose
     */
    public void setDiagnose(String diag) {
        diagnose = diag;
    }
    
    
    /**
     * @return the heimbewohn
     */
    public boolean isHeimbewohn() {
        return heimbewohn;
    }

    /**
     * Returns the bool as "T"/"F" String
     * @return "T"/"F" as String
     */
    public String getHeimbewohn() {
        return isHeimbewohn() ? "T" : "F";
    }
    
    /**
     * Set the bool heimbewohn
     * 
     */
    public void setHeimbewohn(boolean bewohntHeim) {
        heimbewohn = bewohntHeim;
    }

    
    /**
     * @return the veraenderd
     */
    public LocalDate getVeraenderD() {
        return veraenderd;
    }
    
    public void setVeraenderD(LocalDate date) {
        veraenderd = date;
    }

    /**
     * @return the veraendera
     */
    public int getVeraenderA() {
        return veraendera;
    }

    /**
     * @param the veraendera
     */
    public void setVeraenderA(int VeraenderA) {
        veraendera = VeraenderA;
    }
    
    /**
     * @return the logfrei1
     */
    public boolean isLogfrei1() {
        return logfrei1;
    }

    /**
     * Returns the bool as "T"/"F" String
     * @return "T"/"F" as String
     */
    public String getLogfrei1() {
        return isLogfrei1() ? "T" : "F";
    }
    
    /**
     * Set the logfrei1
     */
    public void setLogfrei1(boolean LogFrei) {
        logfrei1 = LogFrei;
    }
    
    /**
     * @return the logfrei2
     */
    public boolean isLogfrei2() {
        return logfrei2;
    }

    /**
     * Returns the bool as "T"/"F" String
     * @return "T"/"F" as String
     */
    public String getLogfrei2() {
        return isLogfrei2() ? "T" : "F";
    }
    
    /**
     * Set the logfrei2
     */
    public void setLogfrei2(boolean LogFrei) {
        logfrei2 = LogFrei;
    }
    
    /**
     * @return the numfrei1
     */
    public int getNumfrei1() {
        return numfrei1;
    }

    /**
     * @param the numfrei1
     */
    public void setNumfrei1(int Numfrei) {
        numfrei1 = Numfrei;
    }
    
    /**
     * @return the numfrei2
     */
    public int getNumfrei2() {
        return numfrei2;
    }
    
    /**
     * @param the numfrei1
     */
    public void setNumfrei2(int Numfrei) {
        numfrei2 = Numfrei;
    }

    /**
     * @return the charfrei1
     */
    public String getCharfrei1() {
        return charfrei1;
    }
    
    /**
     * @param the charfrei1
     */
    public void setCharfrei1(String Charfrei) {
        charfrei1 = Charfrei;
    }

    /**
     * @return the charfrei2
     */
    public String getCharfrei2() {
        return charfrei2;
    }

    /**
     * @param the charfrei2
     */
    public void setCharfrei2(String Charfrei) {
        charfrei2 = Charfrei;
    }
    
    /**
     * @return the Erfassungsdatum
     */
    public LocalDate getErfassungsDatum() {
        return erfassungsDatum;
    }

    /**
     * Set the Erfassungsdatum
     */
    public void setErfassungsDatum(LocalDate datum) {
        erfassungsDatum = datum;
    }

    
    /**
     * @return the termine as is in DB (one String w/ LFs)
     */
    public String getTermine() {
        return termine;
    }

    /**
     * Set the termine-field as one String w/ CR/LF
     * 
     * @param termine the termine to set
     */
    public void setTermine(String termine) {
        this.termine = termine;
    }

    /**
     * @return the ktraeger
     */
    public String getKTraegerName() {
        return ktraeger;
    }

    /**
     * Set the ktraeger
     * 
     * @param String - name of the KostenTraeger
     */
    public void setKTraegerName(String KTraeger) {
        ktraeger = KTraeger;
    }
    
    /**
     * Retrieve the KostenTraegerID
     * 
     * @return the kId
     */
    public int getkId() {
        return kId;
    }

    /**
     * Set the KostenTraegerID
     * 
     * @param kId the kId to set
     */
    public void setkId(int kId) {
        this.kId = kId;
    }

    /**
     * Checks if the KassenID was previously set (somehow, somewhere :D )...
     * @return
     */
    public boolean isKidSet() {
        return kId != -1;
    }
    /**
     * @return the lASTDATE
     */
    public LocalDate getLastDate() {
        return lastDate;
    }

    /**
     * Set the LastDate (Spaetester Beh. Beginn??)
     */
    public void setLastDate(LocalDate datum) {
        lastDate = datum;
    }

    
    /**
     * @return the PreisGruppe
     */
    public int getPreisGruppe() {
        return preisgruppe;
    }

    /**
     * Set the PreisGruppe
     */
    public void setPreisGruppe(int pg) {
        preisgruppe = pg;
    }

    
    /**
     * @return the begruendADR
     */
    public boolean isBegruendADR() {
        return begruendADR;
    }

    /**
     * Returns the bool as "T"/"F" String
     * @return "T"/"F" as String
     */
    public String getBegruendADR() {
        return isBegruendADR() ? "T" : "F";
    }

    /**
     * Set the begruendADR
     */
    public void setBegruendADR(boolean begruendetADR) {
        begruendADR = begruendetADR;
    }

    
    /**
     * @return the hausBes bool. To get a String "T"/"F" use getHausBesuch()
     */
    public boolean isHausBesuch() {
        return hausbes;
    }
    
    /**
     * Returns a String "T"/"F" depending on value of hausBes. To use the bool directly, call isHausBesuch()
     * @return
     */
    public String getHausBesuch() {
        if (isHausBesuch())
            return "T";
        else
            return "F";
    }

    /**
     * Set the hausBesuch bool
     */
    public void setHausBesuch(boolean hb) {
        hausbes = hb;
    }
    
    /**
     * @return the iNDIKATSCHL
     */
    public String getIndikatSchl() {
        return indikatSchl;
    }

    /**
     * Set the IndikationsSchluessel
     */
    public void setIndikatSchl(String indiSchl) {
        indikatSchl = indiSchl;
    }

    
    /**
     * @return the angelegtVon
     */
    public String getAngelegtVon() {
        return angelegtVon;
    }

    /**
     * Set the angelegtVon
     */
    public void setAngelegtVon(String angelegtVon) {
        this.angelegtVon = angelegtVon;
    }

    
    /**
     * @return the lastEdDate
     */
    public LocalDate getLastEdDate() {
        return lastEdDate;
    }

    /**
     * Set the lastEdDate
     */
    public void setLastEdDate(LocalDate datum) {
        lastEdDate = datum;
    }
    
    /**
     * @return the barcodeform
     */
    public int getBarcodeform() {
        return barcodeform;
    }

    /**
     * @param barcodeform the barcodeform to set
     */
    public void setBarcodeform(int barcodeform) {
        this.barcodeform = barcodeform;
    }

    /**
     * @return the dauer
     */
    public String getDauer() {
        return dauer;
    }

    /**
     * Set the dauer
     */
    public void setDauer(String Dauer) {
        dauer = Dauer;
    }
    
    
    /**
     * @return the frequenz
     */
    public String getFrequenz() {
        return frequenz;
    }

    /**
     * Set the Behandlungs Frequenz
     */
    public void setFrequenz(String bHz) {
        frequenz = bHz;
    }

    
    /**
     * @return the matchcode of lastEditor (last edited by user)
     * 
     */
    public String getLastEditor() {
        return lastEditor;
    }

    /**
     * Set the matchcode of lastEditor (last edited by user)
     * 
     */
    public void setLastEditor(String matchCode) {
        lastEditor = matchCode;
    }

    
    /**
     * @return the berId
     */
    public int getBerId() {
        return berId;
    }

    /**
     * Set the berId
     */
    public void setBerId(int BerichtId) {
        berId = BerichtId;
    }

    /**
     * @return the arztBericht
     */
    public boolean isArztBericht() {
        return arztBericht;
    }

    /**
     * Returns the bool as "T"/"F" String
     * @return "T"/"F" as String
     */
    public String getArztBericht() {
        return isArztBericht() ? "T" : "F";
    }
 
    /**
     * Set the arztBericht
     */
    public void setArztBericht(boolean ab) {
        arztBericht = ab;
    }

    
    /**
     * @return the Abschluss as bool
     */
    public boolean isAbschluss() {
        return abschluss;
    }

    public String getAbschluss() {
        return isAbschluss() ? "T" : "F";
    }
    
    /**
     * Set the Abschluss as bool
     */
    public void setAbschluss(boolean Abschluss) {
        abschluss = Abschluss;
    }
    
    /**
     * @return the zZSTATUS
     */
    public int getZZStatus() {
        return zzStatus;
    }

    
    /**
     * @param zzStatus the zzStatus to set
     */
    public void setZZStatus(int zzStatus) {
        this.zzStatus = zzStatus;
    }

    /**
     * @return the zzRegel
     */
    public int getZZRegel() {
        return zzRegel;
    }

    /**
     * Set the zzRegel
     */
    public void setZZRegel(int zzR) {
        zzRegel = zzR;
    }

    
    /**
     * @return the pos1
     */
    public String getHMPos1() {
        return pos1;
    }

    /**
     * @param pos1 the pos1 to set
     */
    public void setHMPos1(String pos1) {
        this.pos1 = pos1.trim ();
    }

    /**
     * @return the pos2
     */
    public String getHMPos2() {
        return pos2;
    }

    /**
     * @param pos2 the pos2 to set
     */
    public void setHMPos2(String pos2) {
        this.pos2 = pos2.trim ();
    }

    /**
     * @return the pos3
     */
    public String getHMPos3() {
        return pos3;
    }

    /**
     * @param pos3 the pos3 to set
     */
    public void setHMPos3(String pos3) {
        this.pos3 = pos3.trim ();
    }

    /**
     * @return the pos4
     */
    public String getHMPos4() {
        return pos4;
    }

    /**
     * @param pos4 the pos4 to set
     */
    public void setHMPos4(String pos4) {
        this.pos4 = pos4.trim ();
    }

    /**
     * Get the HMPosX at idx
     * @param idx the HMPosX to retrieve
     * @return
     */
    public String getHMPos(int idx) {
        switch (idx) {
            case 1:
                return getHMPos1();
            case 2:
                return getHMPos2();
            case 3:
                return getHMPos3();
            case 4:
                return getHMPos4();
            default:
                logger.error("Index " + idx + " ist ungueltig. Es sind nur HM-Positionen 1-4 setzbar");
                return "";
        }
    }
    
    /**
     * Set the desired HMPos via an index. This will set HMPosX, where X=idx to value
     * @param idx   the HMPos to set
     * @param value the value to set it to
     */
    public void setHMPos(int idx, String value) {
        switch (idx) {
            case 1:
                setHMPos1(value);
                break;
            case 2:
                setHMPos2(value);
                break;
            case 3:
                setHMPos3(value);
                break;
            case 4:
                setHMPos4(value);
                break;
            default:
                logger.error("Index " + idx + " ist ungueltig. Es sind nur HM-Positionen 1-4 setzbar");
        }
    }
    
    /**
     * @return the farbcode
     */
    public int getFarbcode() {
        return farbcode;
    }

    /**
     * Set the farbcode
     */
    public void setFarbcode(int fc) {
        farbcode = fc;
    }
    
    /**
     * @return the rsplit
     */
    public String getRSplit() {
        return rsplit;
    }

    /**
     * @param the rsplit
     */
    public void setRSplit(String RSplit) {
        rsplit = RSplit;
    }
    
    /**
     * @return the jahrfrei (=VorJahrFrei?)
     */
    public String getJahrfrei() {
        return jahrfrei;
    }

    /**
     * Set the jahrFrei
     */
    public void setJahrfrei(String vjf) {
        jahrfrei = vjf;
    }

    
    /**
     * @return the unter18
     */
    public boolean isUnter18() {
        return unter18;
    }

    /**
     * return the bool unter18 as "T"/"F" String
     */
    public String getUnter18() {
        if (isUnter18())
            return "T";
        else
            return "F";
    }
    /**
     * @param unter18 the unter18 to set
     */
    public void setUnter18(boolean unter18) {
        this.unter18 = unter18;
    }

    /**
     * @return the hbVoll
     */
    public boolean isHbVoll() {
        return hbVoll;
    }

    /**
     * Returns the bool as "T"/"F" String
     * @return "T"/"F" as String
     */
    public String getHbVoll() {
        return isHbVoll() ? "T" : "F";
    }

    /**
     * @return the hbVoll
     */
    public void setHbVoll(boolean voll) {
        hbVoll = voll;
    }

    /**
     * @return the anzahlHb
     */
    public int getAnzahlHb() {
        return anzahlHb;
    }
    
    /**
     * set the anzahlHausBesuche
     * 
     * @param int
     */
    public void setAnzahlHb(int Anzahl) {
        anzahlHb= Anzahl;
    }

    /**
     * @return the kuerzel1
     */
    public String getHMKuerzel1() {
        return kuerzel1;
    }

    /**
     * @param kuerzel1 the kuerzel1 to set
     */
    public void setHMKuerzel1(String kuerzel1) {
        this.kuerzel1 = kuerzel1;
    }

    /**
     * @return the kuerzel2
     */
    public String getHMKuerzel2() {
        return kuerzel2;
    }

    /**
     * @param kuerzel2 the kuerzel2 to set
     */
    public void setHMKuerzel2(String kuerzel2) {
        this.kuerzel2 = kuerzel2;
    }

    /**
     * @return the kuerzel3
     */
    public String getHMKuerzel3() {
        return kuerzel3;
    }

    /**
     * @param kuerzel3 the kuerzel3 to set
     */
    public void setHMKuerzel3(String kuerzel3) {
        this.kuerzel3 = kuerzel3;
    }

    /**
     * @return the kuerzel4
     */
    public String getHMKuerzel4() {
        return kuerzel4;
    }

    /**
     * @param kuerzel4 the kuerzel4 to set
     */
    public void setHMKuerzel4(String kuerzel4) {
        this.kuerzel4 = kuerzel4;
    }
    
    /**
     * @param kuerzel5 the kuerzel5 to set
     */
    public void setHMKuerzel5(String kuerzel5) {
        this.kuerzel5 = kuerzel5;
    }

    /**
     * @return the kuerzel5
     */
    public String getHMKuerzel5() {
        return kuerzel5;
    }

    /**
     * @return the kuerzel6
     */
    public String getHMKuerzel6() {
        return kuerzel6;
    }
    
    /**
     * @param kuerzel6 the kuerzel6 to set
     */
    public void setHMKuerzel6(String kuerzel6) {
        this.kuerzel6 = kuerzel6;
    }
    
    /**
     * Get 1 of the 6 (HM-?)KuerzelX
     *  
     * @param idx - the Kuerzel to set
     */
    public String getHMKuerzel(int idx){
        switch (idx) {
            
            case 1:
                return getHMKuerzel1();
            case 2:
                return getHMKuerzel2();
            case 3:
                return getHMKuerzel3();
            case 4:
                return getHMKuerzel4();
            case 5:
                return getHMKuerzel5();
            case 6:
                return getHMKuerzel6();
            default:
                logger.error("Index " + idx + " ist ungueltig. Nur (HM-)Kuerzel 1-6.");
                return "";
        }
    }
    
    /**
     * Set 1 of the 6 (HM-?)KuerzelX to value
     *  
     * @param idx - the Kuerzel to set
     * @param value the value to set it to
     */
    public void setHMKuerzel(int idx, String value){
        switch (idx) {
            
            case 1:
                setHMKuerzel1(value);
                break;
            case 2:
                setHMKuerzel2(value);
                break;
            case 3:
                setHMKuerzel3(value);
                break;
            case 4:
                setHMKuerzel4(value);
                break;
            case 5:
                setHMKuerzel5(value);
                break;
            case 6:
                setHMKuerzel6(value);
                break;
            default:
                logger.error("Index " + idx + " ist ungueltig. Nur 1-6 sind Kuerzel");
        }
    }
   
    /**
     * @return the icd10
     */
    public String getIcd10() {
        return icd10;
    }

    /**
     * @param icd10 the icd10 to set
     */
    public void setIcd10(String icd10) {
        this.icd10 = icd10;
    }

    /**
     * @return the icd10_2
     */
    public String getIcd10_2() {
        return icd10_2;
    }

    /**
     * @param icd10_2 the icd10_2 to set
     */
    public void setIcd10_2(String icd10_2) {
        this.icd10_2 = icd10_2;
    }

    /**
     * @return the pauschale
     */
    public boolean usePauschale() {
        return pauschale;
    }

    public String getPauschale() {
        if (usePauschale())
            return "T";
        else
            return "F";
    }
    
    public void setPauschale(boolean pausch) {
        pauschale = pausch;
    }

    /**
     *  I took lastEdDate and lastEditor out of hashcode, since this would always lead to a changed
     *   Rezept on check "Do we have changes we want to save before abort" - I would suggest using equals if those vals
     *   should be included or, rename this meth & create new default
     *  Also removed veraendera & veraenderd since apparently they ain't being used
     */
    @Override
    public int hashCode() {
        return Objects.hash(abschluss, aerzte, angelegtVon, anzahl1, anzahl2, anzahl3, anzahl4, anzahlHb, anzahlKM,
                artDerBeh1, artDerBeh2, artDerBeh3, artDerBeh4, arzt, arztBericht, arztId, barcodeform, befr,
                begruendADR, berId, charfrei1, charfrei2, dauer, diagnose, disziplin, erfassungsDatum, farbcode,
                frequenz, hausbes, hbVoll, heimbewohn, icd10, icd10_2, id, indikatSchl, isToBeInserted, jahrfrei, kId,
                ktraeger, kuerzel1, kuerzel2, kuerzel3, kuerzel4, kuerzel5, kuerzel6, lastDate,
                logfrei1, logfrei2, numfrei1, numfrei2, patId, patIntern, pauschale, pos1, pos2, pos3, pos4, preise1,
                preise2, preise3, preise4, preisgruppe, rezBez, rezDatum, rezGeb, rezNr, rezeptArt, rsplit, termine,
                unter18, zzRegel, zzStatus);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Rezept other = (Rezept) obj;
        return abschluss == other.abschluss && Objects.equals(aerzte, other.aerzte)
                && Objects.equals(angelegtVon, other.angelegtVon) && anzahl1 == other.anzahl1
                && anzahl2 == other.anzahl2 && anzahl3 == other.anzahl3 && anzahl4 == other.anzahl4
                && anzahlHb == other.anzahlHb && Objects.equals(anzahlKM, other.anzahlKM)
                && artDerBeh1 == other.artDerBeh1 && artDerBeh2 == other.artDerBeh2 && artDerBeh3 == other.artDerBeh3
                && artDerBeh4 == other.artDerBeh4 && Objects.equals(arzt, other.arzt)
                && arztBericht == other.arztBericht && arztId == other.arztId && barcodeform == other.barcodeform
                && befr == other.befr && begruendADR == other.begruendADR && berId == other.berId
                && Objects.equals(charfrei1, other.charfrei1) && Objects.equals(charfrei2, other.charfrei2)
                && Objects.equals(dauer, other.dauer) && Objects.equals(diagnose, other.diagnose)
                && disziplin == other.disziplin && Objects.equals(erfassungsDatum, other.erfassungsDatum)
                && farbcode == other.farbcode && Objects.equals(frequenz, other.frequenz) && hausbes == other.hausbes
                && hbVoll == other.hbVoll && heimbewohn == other.heimbewohn && Objects.equals(icd10, other.icd10)
                && Objects.equals(icd10_2, other.icd10_2) && id == other.id
                && Objects.equals(indikatSchl, other.indikatSchl) && isToBeInserted == other.isToBeInserted
                && Objects.equals(jahrfrei, other.jahrfrei) && kId == other.kId
                && Objects.equals(ktraeger, other.ktraeger) && Objects.equals(kuerzel1, other.kuerzel1)
                && Objects.equals(kuerzel2, other.kuerzel2) && Objects.equals(kuerzel3, other.kuerzel3)
                && Objects.equals(kuerzel4, other.kuerzel4) && Objects.equals(kuerzel5, other.kuerzel5)
                && Objects.equals(kuerzel6, other.kuerzel6) && Objects.equals(lastDate, other.lastDate)
                && Objects.equals(lastEdDate, other.lastEdDate) && Objects.equals(lastEditor, other.lastEditor)
                && logfrei1 == other.logfrei1 && logfrei2 == other.logfrei2 && numfrei1 == other.numfrei1
                && numfrei2 == other.numfrei2 && patId == other.patId && patIntern == other.patIntern
                && pauschale == other.pauschale && Objects.equals(pos1, other.pos1) && Objects.equals(pos2, other.pos2)
                && Objects.equals(pos3, other.pos3) && Objects.equals(pos4, other.pos4)
                && Objects.equals(preise1, other.preise1) && Objects.equals(preise2, other.preise2)
                && Objects.equals(preise3, other.preise3) && Objects.equals(preise4, other.preise4)
                && preisgruppe == other.preisgruppe && rezBez == other.rezBez
                && Objects.equals(rezDatum, other.rezDatum) && Objects.equals(rezGeb, other.rezGeb)
                && Objects.equals(rezNr, other.rezNr) && rezeptArt == other.rezeptArt
                && Objects.equals(rsplit, other.rsplit) && Objects.equals(termine, other.termine)
                && unter18 == other.unter18 && veraendera == other.veraendera
                && Objects.equals(veraenderd, other.veraenderd) && zzRegel == other.zzRegel
                && zzStatus == other.zzStatus;
    }

}
