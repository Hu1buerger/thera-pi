package dto;

public class Hmr_hm_diagnosegruppe{
    int id;
    private char heilmittelposition;
    private double vorrangig;
    private double ergaenzend;
    private double heilmittelpostion_ausschluss;
    private double max_menge_auf_vo;
    private double max_menge_je_vo_fall;
    private double max_menge_pro_jahr;
    private java.util.Date gueltig_von;
    private java.util.Date gueltig_bis;
    private String ccid;

    public char getHeilmittelposition(){
        return heilmittelposition;
    }

    public void setHeilmittelposition(char heilmittelposition){
        this.heilmittelposition=heilmittelposition;
    }

    public double getVorrangig(){
        return vorrangig;
    }

    public void setVorrangig(double vorrangig){
        this.vorrangig=vorrangig;
    }

    public double getErgaenzend(){
        return ergaenzend;
    }

    public void setErgaenzend(double ergaenzend){
        this.ergaenzend=ergaenzend;
    }

    public double getHeilmittelpostion_ausschluss(){
        return heilmittelpostion_ausschluss;
    }

    public void setHeilmittelpostion_ausschluss(double heilmittelpostion_ausschluss){
        this.heilmittelpostion_ausschluss=heilmittelpostion_ausschluss;
    }

    public double getMax_menge_auf_vo(){
        return max_menge_auf_vo;
    }

    public void setMax_menge_auf_vo(double max_menge_auf_vo){
        this.max_menge_auf_vo=max_menge_auf_vo;
    }

    public double getMax_menge_je_vo_fall(){
        return max_menge_je_vo_fall;
    }

    public void setMax_menge_je_vo_fall(double max_menge_je_vo_fall){
        this.max_menge_je_vo_fall=max_menge_je_vo_fall;
    }

    public double getMax_menge_pro_jahr(){
        return max_menge_pro_jahr;
    }

    public void setMax_menge_pro_jahr(double max_menge_pro_jahr){
        this.max_menge_pro_jahr=max_menge_pro_jahr;
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

    public String getCcid(){
        return ccid;
    }

    public void setCcid(String ccid){
        this.ccid=ccid;
    }
}
