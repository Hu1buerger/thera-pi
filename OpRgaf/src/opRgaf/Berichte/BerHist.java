package opRgaf.Berichte;

import java.time.LocalDate;
import java.util.Objects;

public class BerHist {

    String patIntern;
    int berichtId;
    String berichtTyp;
    String verfasser;
    String empfaenger;
    String berTitel;
    LocalDate erstellDat;
    LocalDate editDat;
    LocalDate versandDat;
    String dateiname;
    int empfId;
    int id;
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
    public String getBerichtTyp() {
        return berichtTyp;
    }
    public void setBerichtTyp(String berichtTyp) {
        this.berichtTyp = berichtTyp;
    }
    public String getVerfasser() {
        return verfasser;
    }
    public void setVerfasser(String verfasser) {
        this.verfasser = verfasser;
    }
    public String getEmpfaenger() {
        return empfaenger;
    }
    public void setEmpfaenger(String empfaenger) {
        this.empfaenger = empfaenger;
    }
    public String getBerTitel() {
        return berTitel;
    }
    public void setBerTitel(String berTitel) {
        this.berTitel = berTitel;
    }
    public LocalDate getErstellDat() {
        return erstellDat;
    }
    public void setErstellDat(LocalDate erstellDat) {
        this.erstellDat = erstellDat;
    }
    public LocalDate getEditDat() {
        return editDat;
    }
    public void setEditDat(LocalDate editDat) {
        this.editDat = editDat;
    }
    public LocalDate getVersandDat() {
        return versandDat;
    }
    public void setVersandDat(LocalDate versandDat) {
        this.versandDat = versandDat;
    }
    public String getDateiname() {
        return dateiname;
    }
    public void setDateiname(String dateiname) {
        this.dateiname = dateiname;
    }
    public int getEmpfId() {
        return empfId;
    }
    public void setEmpfId(int empfId) {
        this.empfId = empfId;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    // Standard Hashcode & equals omitting the field "id"
    @Override
    public int hashCode() {
        return Objects.hash(berTitel, berichtId, berichtTyp, dateiname, editDat, empfId, empfaenger, erstellDat,
                patIntern, verfasser, versandDat);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BerHist other = (BerHist) obj;
        return Objects.equals(berTitel, other.berTitel) && berichtId == other.berichtId
                && Objects.equals(berichtTyp, other.berichtTyp) && Objects.equals(dateiname, other.dateiname)
                && Objects.equals(editDat, other.editDat) && empfId == other.empfId
                && Objects.equals(empfaenger, other.empfaenger) && Objects.equals(erstellDat, other.erstellDat)
                && Objects.equals(patIntern, other.patIntern) && Objects.equals(verfasser, other.verfasser)
                && Objects.equals(versandDat, other.versandDat);
    }
    @Override
    public String toString() {
        return "BerHist [patIntern=" + patIntern + ", berichtId=" + berichtId + ", berichtTyp=" + berichtTyp
                + ", verfasser=" + verfasser + ", empfaenger=" + empfaenger + ", berTitel=" + berTitel + ", erstellDat="
                + erstellDat + ", editDat=" + editDat + ", versandDat=" + versandDat + ", dateiname=" + dateiname
                + ", empfId=" + empfId + ", id=" + id + "]";
    }
    
}
