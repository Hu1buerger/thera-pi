package opRgaf.Berichte;

import java.time.LocalDate;
import java.util.Objects;

public class Bericht1 {
    String patIntern;
    int berichtId;
    String arztNum;
    LocalDate erstellDat;
    String versandArt;
    LocalDate versandDat;
    String berTyp;
    String berStand;
    String berBeso;
    String berProg;
    String berVors;
    String diagnose;
    String krBild;
    String verfasser;
    LocalDate rezDatum;
    int id;
    
    // Public std. getter/setters
    public String getPatIntern() {
        return patIntern;
    }
    public void setPatIntern(String patIntern) {
        this.patIntern = patIntern;
    }
    public int getBerichtId() {
        return berichtId;
    }
    public void setBerichtId(int berichtId) {
        this.berichtId = berichtId;
    }
    public String getArztNum() {
        return arztNum;
    }
    public void setArztNum(String arztNum) {
        this.arztNum = arztNum;
    }
    public LocalDate getErstellDat() {
        return erstellDat;
    }
    public void setErstellDat(LocalDate erstellDat) {
        this.erstellDat = erstellDat;
    }
    public String getVersandArt() {
        return versandArt;
    }
    public void setVersandArt(String versandArt) {
        this.versandArt = versandArt;
    }
    public LocalDate getVersandDat() {
        return versandDat;
    }
    public void setVersandDat(LocalDate versandDat) {
        this.versandDat = versandDat;
    }
    public String getBerTyp() {
        return berTyp;
    }
    public void setBerTyp(String berTyp) {
        this.berTyp = berTyp;
    }
    public String getBerStand() {
        return berStand;
    }
    public void setBerStand(String berStand) {
        this.berStand = berStand;
    }
    public String getBerBeso() {
        return berBeso;
    }
    public void setBerBeso(String berBeso) {
        this.berBeso = berBeso;
    }
    public String getBerProg() {
        return berProg;
    }
    public void setBerProg(String berProg) {
        this.berProg = berProg;
    }
    public String getBerVors() {
        return berVors;
    }
    public void setBerVors(String berVors) {
        this.berVors = berVors;
    }
    public String getDiagnose() {
        return diagnose;
    }
    public void setDiagnose(String diagnose) {
        this.diagnose = diagnose;
    }
    public String getKrBild() {
        return krBild;
    }
    public void setKrBild(String krBild) {
        this.krBild = krBild;
    }
    public String getVerfasser() {
        return verfasser;
    }
    public void setVerfasser(String verfasser) {
        this.verfasser = verfasser;
    }
    public LocalDate getRezDatum() {
        return rezDatum;
    }
    public void setRezDatum(LocalDate rezDatum) {
        this.rezDatum = rezDatum;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return "Bericht1 [patIntern=" + patIntern + ", berichtId=" + berichtId + ", arztNum=" + arztNum
                + ", erstellDat=" + erstellDat + ", versandArt=" + versandArt + ", versandDat=" + versandDat
                + ", berTyp=" + berTyp + ", berStand=" + berStand + ", berBeso=" + berBeso + ", berProg=" + berProg
                + ", berVors=" + berVors + ", diagnose=" + diagnose + ", krBild=" + krBild + ", verfasser=" + verfasser
                + ", rezDatum=" + rezDatum + ", id=" + id + "]";
    }
    
    // Hashcode & equals do not include "id" - hope that's ok...
    @Override
    public int hashCode() {
        return Objects.hash(arztNum, berBeso, berProg, berStand, berTyp, berVors, berichtId, diagnose, erstellDat,
                krBild, patIntern, rezDatum, verfasser, versandArt, versandDat);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Bericht1 other = (Bericht1) obj;
        return Objects.equals(arztNum, other.arztNum) && Objects.equals(berBeso, other.berBeso)
                && Objects.equals(berProg, other.berProg) && Objects.equals(berStand, other.berStand)
                && Objects.equals(berTyp, other.berTyp) && Objects.equals(berVors, other.berVors)
                && berichtId == other.berichtId && Objects.equals(diagnose, other.diagnose)
                && Objects.equals(erstellDat, other.erstellDat) && Objects.equals(krBild, other.krBild)
                && Objects.equals(patIntern, other.patIntern) && Objects.equals(rezDatum, other.rezDatum)
                && Objects.equals(verfasser, other.verfasser) && Objects.equals(versandArt, other.versandArt)
                && Objects.equals(versandDat, other.versandDat);
    }

}
