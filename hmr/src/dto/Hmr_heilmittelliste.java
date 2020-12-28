package dto;

public class Hmr_heilmittelliste{
    private char kurzbezeichnung;
    private char langbezeichnung;
    private double dauer_von;
    private double dauer_bis;
    private double vorrangig;
    private double max_vo_fall;
    private java.util.Date gueltig_von;
    private java.util.Date gueltig_bis;
    private char region;
    private char rvo;
    private char vdek;
    private char lkk;
    private String ccid;

    public char getKurzbezeichnung(){
        return kurzbezeichnung;
    }

    public void setKurzbezeichnung(char kurzbezeichnung){
        this.kurzbezeichnung=kurzbezeichnung;
    }

    public char getLangbezeichnung(){
        return langbezeichnung;
    }

    public void setLangbezeichnung(char langbezeichnung){
        this.langbezeichnung=langbezeichnung;
    }

    public double getDauer_von(){
        return dauer_von;
    }

    public void setDauer_von(double dauer_von){
        this.dauer_von=dauer_von;
    }

    public double getDauer_bis(){
        return dauer_bis;
    }

    public void setDauer_bis(double dauer_bis){
        this.dauer_bis=dauer_bis;
    }

    public double getVorrangig(){
        return vorrangig;
    }

    public void setVorrangig(double vorrangig){
        this.vorrangig=vorrangig;
    }

    public double getMax_vo_fall(){
        return max_vo_fall;
    }

    public void setMax_vo_fall(double max_vo_fall){
        this.max_vo_fall=max_vo_fall;
    }

    public java.util.Date getGueltig_von(){
        return gueltig_von;
    }

    public void setGueltig_von(java.util.Date gueltig_von){
        this.gueltig_von=gueltig_von;
    }

    public java.util.Date getGueltig_bis(){
        return gueltig_bis;
    }

    public void setGueltig_bis(java.util.Date gueltig_bis){
        this.gueltig_bis=gueltig_bis;
    }

    public char getRegion(){
        return region;
    }

    public void setRegion(char region){
        this.region=region;
    }

    public char getRvo(){
        return rvo;
    }

    public void setRvo(char rvo){
        this.rvo=rvo;
    }

    public char getVdek(){
        return vdek;
    }

    public void setVdek(char vdek){
        this.vdek=vdek;
    }

    public char getLkk(){
        return lkk;
    }

    public void setLkk(char lkk){
        this.lkk=lkk;
    }

    public String getCcid(){
        return ccid;
    }

    public void setCcid(String ccid){
        this.ccid=ccid;
    }
}