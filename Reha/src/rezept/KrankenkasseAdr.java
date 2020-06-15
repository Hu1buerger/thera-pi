package rezept;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(KrankenkasseAdr.class);

    String kuerzel;
    int preisgruppe;
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
    int pgKg;
    int pgMa;
    int pgEr;
    int pgLo;
    int pgRh;
    int pgPo;
    int pgRs;
    int pgFt;
    
    public KrankenkasseAdr() {
    }

    public String getKuerzel() {
        return kuerzel;
    }

    public void setKuerzel(String kuerzel) {
        this.kuerzel = kuerzel;
    }

    public int getPreisgruppe() {
        return preisgruppe;
    }

    public void setPreisgruppe(int preisgruppe) {
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

    public int getPgKg() {
        return pgKg;
    }

    public void setPgKg(int pgKg) {
        this.pgKg = pgKg;
    }

    public int getPgMa() {
        return pgMa;
    }

    public void setPgMa(int pgMa) {
        this.pgMa = pgMa;
    }

    public int getPgEr() {
        return pgEr;
    }

    public void setPgEr(int pgEr) {
        this.pgEr = pgEr;
    }

    public int getPgLo() {
        return pgLo;
    }

    public void setPgLo(int pgLo) {
        this.pgLo = pgLo;
    }

    public int getPgRh() {
        return pgRh;
    }

    public void setPgRh(int pgRh) {
        this.pgRh = pgRh;
    }

    public int getPgPo() {
        return pgPo;
    }

    public void setPgPo(int pgPo) {
        this.pgPo = pgPo;
    }

    public int getPgRs() {
        return pgRs;
    }

    public void setPgRs(int pgRs) {
        this.pgRs = pgRs;
    }

    public int getPgFt() {
        return pgFt;
    }

    public void setPgFt(int pgFt) {
        this.pgFt = pgFt;
    }

    // Standard hashcode & equals omitting the field ID to compare to different datasets
    @Override
    public int hashCode() {
        return Objects.hash(email1, email2, email3, fax, hmrabrechnung, ikKasse, ikKostent, ikKvKarte, ikNum,
                ikNutzer, ikPapier, ikPhysika, kMemo, kassenNam1, kassenNam2, kuerzel, kvNummer, matchcode, ort, pgEr,
                pgFt, pgKg, pgLo, pgMa, pgPo, pgRh, pgRs, plz, postfach, preisgruppe, rechnung, strasse, telefon);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KrankenkasseAdr other = (KrankenkasseAdr) obj;
        return Objects.equals(email1, other.email1) && Objects.equals(email2, other.email2)
                && Objects.equals(email3, other.email3) && Objects.equals(fax, other.fax)
                && hmrabrechnung == other.hmrabrechnung && Objects.equals(ikKasse, other.ikKasse)
                && Objects.equals(ikKostent, other.ikKostent) && Objects.equals(ikKvKarte, other.ikKvKarte)
                && Objects.equals(ikNum, other.ikNum) && Objects.equals(ikNutzer, other.ikNutzer)
                && Objects.equals(ikPapier, other.ikPapier) && Objects.equals(ikPhysika, other.ikPhysika)
                && Objects.equals(kMemo, other.kMemo) && Objects.equals(kassenNam1, other.kassenNam1)
                && Objects.equals(kassenNam2, other.kassenNam2) && Objects.equals(kuerzel, other.kuerzel)
                && Objects.equals(kvNummer, other.kvNummer) && Objects.equals(matchcode, other.matchcode)
                && Objects.equals(ort, other.ort) && pgEr == other.pgEr && pgFt == other.pgFt && pgKg == other.pgKg
                && pgLo == other.pgLo && pgMa == other.pgMa && pgPo == other.pgPo && pgRh == other.pgRh
                && pgRs == other.pgRs && Objects.equals(plz, other.plz) && Objects.equals(postfach, other.postfach)
                && preisgruppe == other.preisgruppe && Objects.equals(rechnung, other.rechnung)
                && Objects.equals(strasse, other.strasse) && Objects.equals(telefon, other.telefon);
    }

    @Override
    public String toString() {
        return "KrankenkasseAdr [kuerzel=" + kuerzel + ", preisgruppe=" + preisgruppe + ", kassenNam1=" + kassenNam1
                + ", kassenNam2=" + kassenNam2 + ", strasse=" + strasse + ", plz=" + plz + ", ort=" + ort
                + ", postfach=" + postfach + ", fax=" + fax + ", telefon=" + telefon + ", ikNum=" + ikNum
                + ", kvNummer=" + kvNummer + ", matchcode=" + matchcode + ", kMemo=" + kMemo + ", rechnung=" + rechnung
                + ", ikKasse=" + ikKasse + ", ikPhysika=" + ikPhysika + ", ikNutzer=" + ikNutzer + ", ikKostent="
                + ikKostent + ", ikKvKarte=" + ikKvKarte + ", ikPapier=" + ikPapier + ", email1=" + email1 + ", email2="
                + email2 + ", email3=" + email3 + ", id=" + id + ", hmrabrechnung=" + hmrabrechnung + ", pgKg=" + pgKg
                + ", pgMa=" + pgMa + ", pgEr=" + pgEr + ", pgLo=" + pgLo + ", pgRh=" + pgRh + ", pgPo=" + pgPo
                + ", pgRs=" + pgRs + ", pgFt=" + pgFt + "]";
    }
    
}
