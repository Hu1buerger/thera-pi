package rezept;

import mandant.IK;

/**
 * Class implementing the KrankenkasseAdr object
 * 
 * Originally designed to do "RezeptAbschluss" which needed some of these fields copied into a different table.
 * It will need
 * - a new home
 * - new methods 
 *      - some other stuff I currently can't think of
 *      
 */
public class KrankenkasseAdr {

    String kuerzel;
    String PREISGRUPPE;
    String kassen_nam1;
    String kassen_nam2;
    String STRASSE;
    String PLZ;
    String ORT;
    String POSTFACH;
    String FAX;
    String TELEFON;
    String IK_NUM;
    String KV_NUMMER;
    String MATCHCODE;
    String KMEMO;
    String RECHNUNG;
    IK ikKasse;
    IK IK_PHYSIKA;
    IK IK_NUTZER;
    IK ikKostenTraeger;
    IK IK_KVKARTE;
    IK IK_PAPIER;
    String EMAIL1;
    String EMAIL2;
    String EMAIL3;
    int id;
    boolean HMRABRECHNUNG;
    String PGKG;
    String PGMA;
    String PGER;
    String PGLO;
    String PGRH;
    String PGPO;
    String pgrs;
    String pgft;
    
    public KrankenkasseAdr() {
    }
    
    @Override
    public String toString() {
        return "krankenkasseAdr [kuerzel=" + kuerzel + ", PREISGRUPPE=" + PREISGRUPPE + ", kassen_nam1=" + kassen_nam1
                + ", kassen_nam2=" + kassen_nam2 + ", STRASSE=" + STRASSE + ", PLZ=" + PLZ + ", ORT=" + ORT
                + ", POSTFACH=" + POSTFACH + ", FAX=" + FAX + ", TELEFON=" + TELEFON + ", IK_NUM=" + IK_NUM
                + ", KV_NUMMER=" + KV_NUMMER + ", MATCHCODE=" + MATCHCODE + ", KMEMO=" + KMEMO + ", RECHNUNG="
                + RECHNUNG + ", IK_KASSE=" + ikKasse + ", IK_PHYSIKA=" + IK_PHYSIKA + ", IK_NUTZER=" + IK_NUTZER
                + ", IK_KOSTENT=" + ikKostenTraeger + ", IK_KVKARTE=" + IK_KVKARTE + ", IK_PAPIER=" + IK_PAPIER + ", EMAIL1="
                + EMAIL1 + ", EMAIL2=" + EMAIL2 + ", EMAIL3=" + EMAIL3 + ", id=" + id + ", HMRABRECHNUNG="
                + HMRABRECHNUNG + ", PGKG=" + PGKG + ", PGMA=" + PGMA + ", PGER=" + PGER + ", PGLO=" + PGLO + ", PGRH="
                + PGRH + ", PGPO=" + PGPO + ", pgrs=" + pgrs + ", pgft=" + pgft + "]";
    }

    public String getKKuezel() {
        return kuerzel;
    }

    public void setKuerzel(String kUERZEL) {
        kuerzel = kUERZEL;
    }

    public String getPREISGRUPPE() {
        return PREISGRUPPE;
    }

    public void setPREISGRUPPE(String pREISGRUPPE) {
        PREISGRUPPE = pREISGRUPPE;
    }

    public String getKassen_nam1() {
        return kassen_nam1;
    }

    public void setKassen_nam1(String kassen_nam1) {
        this.kassen_nam1 = kassen_nam1;
    }

    public String getKassen_nam2() {
        return kassen_nam2;
    }

    public void setKassen_nam2(String kassen_nam2) {
        this.kassen_nam2 = kassen_nam2;
    }

    public String getSTRASSE() {
        return STRASSE;
    }

    public void setSTRASSE(String sTRASSE) {
        STRASSE = sTRASSE;
    }

    public String getPLZ() {
        return PLZ;
    }

    public void setPLZ(String pLZ) {
        PLZ = pLZ;
    }

    public String getORT() {
        return ORT;
    }

    public void setORT(String oRT) {
        ORT = oRT;
    }

    public String getPOSTFACH() {
        return POSTFACH;
    }

    public void setPOSTFACH(String pOSTFACH) {
        POSTFACH = pOSTFACH;
    }

    public String getFAX() {
        return FAX;
    }

    public void setFAX(String fAX) {
        FAX = fAX;
    }

    public String getTELEFON() {
        return TELEFON;
    }

    public void setTELEFON(String tELEFON) {
        TELEFON = tELEFON;
    }

    public String getIK_NUM() {
        return IK_NUM;
    }

    public void setIK_NUM(String iK_NUM) {
        IK_NUM = iK_NUM;
    }

    public String getKV_NUMMER() {
        return KV_NUMMER;
    }

    public void setKV_NUMMER(String kV_NUMMER) {
        KV_NUMMER = kV_NUMMER;
    }

    public String getMATCHCODE() {
        return MATCHCODE;
    }

    public void setMATCHCODE(String mATCHCODE) {
        MATCHCODE = mATCHCODE;
    }

    public String getKMEMO() {
        return KMEMO;
    }

    public void setKMEMO(String kMEMO) {
        KMEMO = kMEMO;
    }

    public String getRECHNUNG() {
        return RECHNUNG;
    }

    public void setRECHNUNG(String rECHNUNG) {
        RECHNUNG = rECHNUNG;
    }

    public IK getIkKasse() {
        return ikKasse;
    }

    public void setIkKasse(IK iK_KASSE) {
        ikKasse = iK_KASSE;
    }

    public IK getIK_PHYSIKA() {
        return IK_PHYSIKA;
    }

    public void setIK_PHYSIKA(IK iK_PHYSIKA) {
        IK_PHYSIKA = iK_PHYSIKA;
    }

    public IK getIK_NUTZER() {
        return IK_NUTZER;
    }

    public void setIK_NUTZER(IK iK_NUTZER) {
        IK_NUTZER = iK_NUTZER;
    }

    public IK getIkKostenTraeger() {
        return ikKostenTraeger;
    }

    public void setIkKostenTraeger(IK iK_KOSTENT) {
        ikKostenTraeger = iK_KOSTENT;
    }

    public IK getIK_KVKARTE() {
        return IK_KVKARTE;
    }

    public void setIK_KVKARTE(IK iK_KVKARTE) {
        IK_KVKARTE = iK_KVKARTE;
    }

    public IK getIK_PAPIER() {
        return IK_PAPIER;
    }

    public void setIK_PAPIER(IK iK_PAPIER) {
        IK_PAPIER = iK_PAPIER;
    }

    public String getEMAIL1() {
        return EMAIL1;
    }

    public void setEMAIL1(String eMAIL1) {
        EMAIL1 = eMAIL1;
    }

    public String getEMAIL2() {
        return EMAIL2;
    }

    public void setEMAIL2(String eMAIL2) {
        EMAIL2 = eMAIL2;
    }

    public String getEMAIL3() {
        return EMAIL3;
    }

    public void setEMAIL3(String eMAIL3) {
        EMAIL3 = eMAIL3;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isHMRABRECHNUNG() {
        return HMRABRECHNUNG;
    }

    public void setHMRABRECHNUNG(boolean hMRABRECHNUNG) {
        HMRABRECHNUNG = hMRABRECHNUNG;
    }

    public String getPGKG() {
        return PGKG;
    }

    public void setPGKG(String pGKG) {
        PGKG = pGKG;
    }

    public String getPGMA() {
        return PGMA;
    }

    public void setPGMA(String pGMA) {
        PGMA = pGMA;
    }

    public String getPGER() {
        return PGER;
    }

    public void setPGER(String pGER) {
        PGER = pGER;
    }

    public String getPGLO() {
        return PGLO;
    }

    public void setPGLO(String pGLO) {
        PGLO = pGLO;
    }

    public String getPGRH() {
        return PGRH;
    }

    public void setPGRH(String pGRH) {
        PGRH = pGRH;
    }

    public String getPGPO() {
        return PGPO;
    }

    public void setPGPO(String pGPO) {
        PGPO = pGPO;
    }

    public String getPgrs() {
        return pgrs;
    }

    public void setPgrs(String pgrs) {
        this.pgrs = pgrs;
    }

    public String getPgft() {
        return pgft;
    }

    public void setPgft(String pgft) {
        this.pgft = pgft;
    }
    
}
