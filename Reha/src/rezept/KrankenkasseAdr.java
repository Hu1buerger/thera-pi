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
    String preisgruppe;
    String kassenNam1;
    String kassenNam2;
    String strasse;
    String plz;
    String ort;
    String postfach;
    String fax;
    String telefon;
    String ikNum;
    String kvNummer;
    String matchcode;
    String kMemo;
    String rechnung;
    IK ikKasse;
    IK ikPhysika;
    IK ikNutzer;
    IK ikKostent;
    IK ikKvKarte;
    IK ikPapier;
    String email1;
    String email2;
    String email3;
    int id;
    boolean hmrabrechnung;
    String pgKg;
    String pgMa;
    String pgEr;
    String pgLo;
    String pgRh;
    String pgPo;
    String pgRs;
    String pgFt;
    
    public KrankenkasseAdr() {
    }

    public String getKuerzel() {
        return kuerzel;
    }

    public void setKuerzel(String kuerzel) {
        this.kuerzel = kuerzel;
    }

    public String getPreisgruppe() {
        return preisgruppe;
    }

    public void setPreisgruppe(String preisgruppe) {
        this.preisgruppe = preisgruppe;
    }

    public String getKassenNam1() {
        return kassenNam1;
    }

    public void setKassenNam1(String kassenNam1) {
        this.kassenNam1 = kassenNam1;
    }

    public String getKassenNam2() {
        return kassenNam2;
    }

    public void setKassenNam2(String kassenNam2) {
        this.kassenNam2 = kassenNam2;
    }

    public String getStrasse() {
        return strasse;
    }

    public void setStrasse(String strasse) {
        this.strasse = strasse;
    }

    public String getPlz() {
        return plz;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public String getOrt() {
        return ort;
    }

    public void setOrt(String ort) {
        this.ort = ort;
    }

    public String getPostfach() {
        return postfach;
    }

    public void setPostfach(String postfach) {
        this.postfach = postfach;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getIkNum() {
        return ikNum;
    }

    public void setIkNum(String ikNum) {
        this.ikNum = ikNum;
    }

    public String getKvNummer() {
        return kvNummer;
    }

    public void setKvNummer(String kvNummer) {
        this.kvNummer = kvNummer;
    }

    public String getMatchcode() {
        return matchcode;
    }

    public void setMatchcode(String matchcode) {
        this.matchcode = matchcode;
    }

    public String getKMemo() {
        return kMemo;
    }

    public void setKMemo(String kMemo) {
        this.kMemo = kMemo;
    }

    public String getRechnung() {
        return rechnung;
    }

    public void setRechnung(String rechnung) {
        this.rechnung = rechnung;
    }

    public IK getIkKasse() {
        return ikKasse;
    }

    public void setIkKasse(IK ikKasse) {
        this.ikKasse = ikKasse;
    }

    public IK getIkPhysika() {
        return ikPhysika;
    }

    public void setIkPhysika(IK ikPhysika) {
        this.ikPhysika = ikPhysika;
    }

    public IK getIkNutzer() {
        return ikNutzer;
    }

    public void setIkNutzer(IK ikNutzer) {
        this.ikNutzer = ikNutzer;
    }

    public IK getIkKostenTraeger() {
        return ikKostent;
    }

    public void setIkKostenTraeger(IK ikKostent) {
        this.ikKostent = ikKostent;
    }

    public IK getIkKvKarte() {
        return ikKvKarte;
    }

    public void setIkKvKarte(IK ikKvKarte) {
        this.ikKvKarte = ikKvKarte;
    }

    public IK getIkPapier() {
        return ikPapier;
    }

    public void setIkPapier(IK ikPapier) {
        this.ikPapier = ikPapier;
    }

    public String getEmail1() {
        return email1;
    }

    public void setEmail1(String email1) {
        this.email1 = email1;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(String email2) {
        this.email2 = email2;
    }

    public String getEmail3() {
        return email3;
    }

    public void setEmail3(String email3) {
        this.email3 = email3;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isHmrAbrechnung() {
        return hmrabrechnung;
    }
    
    public String getHmrAbrechnung() {
        return ( isHmrAbrechnung() ? "T" : "F");
    }

    public void setHmrAbrechnung(boolean hmrabrechnung) {
        this.hmrabrechnung = hmrabrechnung;
    }

    public String getPgKg() {
        return pgKg;
    }

    public void setPgKg(String pgKg) {
        this.pgKg = pgKg;
    }

    public String getPgMa() {
        return pgMa;
    }

    public void setPgMa(String pgMa) {
        this.pgMa = pgMa;
    }

    public String getPgEr() {
        return pgEr;
    }

    public void setPgEr(String pgEr) {
        this.pgEr = pgEr;
    }

    public String getPgLo() {
        return pgLo;
    }

    public void setPgLo(String pgLo) {
        this.pgLo = pgLo;
    }

    public String getPgRh() {
        return pgRh;
    }

    public void setPgRh(String pgRh) {
        this.pgRh = pgRh;
    }

    public String getPgPo() {
        return pgPo;
    }

    public void setPgPo(String pgPo) {
        this.pgPo = pgPo;
    }

    public String getPgRs() {
        return pgRs;
    }

    public void setPgRs(String pgRs) {
        this.pgRs = pgRs;
    }

    public String getPgFt() {
        return pgFt;
    }

    public void setPgFt(String pgFt) {
        this.pgFt = pgFt;
    }
    

    
}
