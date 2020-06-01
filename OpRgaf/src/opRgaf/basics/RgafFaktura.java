package opRgaf.basics;

import java.time.LocalDate;

import mandant.IK;

public class RgafFaktura {
    String rNr;
    String rezNr;
    String patIntern;
    Money rGesamt;
    Money rOffen;
    Money rGBetrag;
    Money rPBetrag;
    LocalDate rDatum;
    LocalDate rBezDatum;
    LocalDate rMahndat1;
    LocalDate rMahndat2;
    int id;
    IK ik;                  // This is the IK in the DB, not the current mandant.ik()!!

    public RgafFaktura() {
        
    }

    public String getrNr() {
        return rNr;
    }

    public void setrNr(String rNr) {
        this.rNr = rNr;
    }

    public String getRezNr() {
        return rezNr;
    }

    public void setRezNr(String rezNr) {
        this.rezNr = rezNr;
    }

    public String getPatIntern() {
        return patIntern;
    }

    public void setPatIntern(String patIntern) {
        this.patIntern = patIntern;
    }

    public Money getrGesamt() {
        return rGesamt;
    }

    public void setrGesamt(Money rGesamt) {
        this.rGesamt = rGesamt;
    }

    public Money getrOffen() {
        return rOffen;
    }

    public void setrOffen(Money rOffen) {
        this.rOffen = rOffen;
    }

    public Money getrGBetrag() {
        return rGBetrag;
    }

    public void setrGBetrag(Money rGBetrag) {
        this.rGBetrag = rGBetrag;
    }

    public Money getrPBetrag() {
        return rPBetrag;
    }

    public void setrPBetrag(Money rPBetrag) {
        this.rPBetrag = rPBetrag;
    }

    public LocalDate getrDatum() {
        return rDatum;
    }

    public void setrDatum(LocalDate rDatum) {
        this.rDatum = rDatum;
    }

    public LocalDate getrBezDatum() {
        return rBezDatum;
    }

    public void setrBezDatum(LocalDate rBezDatum) {
        this.rBezDatum = rBezDatum;
    }

    public LocalDate getrMahndat1() {
        return rMahndat1;
    }

    public void setrMahndat1(LocalDate rMahndat1) {
        this.rMahndat1 = rMahndat1;
    }

    public LocalDate getrMahndat2() {
        return rMahndat2;
    }

    public void setrMahndat2(LocalDate rMahndat2) {
        this.rMahndat2 = rMahndat2;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public IK getIk() {
        return ik;
    }

    public void setIk(IK ik) {
        this.ik = ik;
    }

}
