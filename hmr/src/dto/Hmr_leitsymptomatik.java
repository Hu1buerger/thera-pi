package dto;

public class Hmr_leitsymptomatik{
    int id;
    private char leitsymptomatik;
    private String leitsymptomatik_beschreibung;
    private java.util.Date gueltig_von;
    private java.util.Date gueltig_bis;
    private String ccid;

    public char getLeitsymptomatik(){
        return leitsymptomatik;
    }

    public void setLeitsymptomatik(char leitsymptomatik){
        this.leitsymptomatik=leitsymptomatik;
    }

    public String getLeitsymptomatik_beschreibung(){
        return leitsymptomatik_beschreibung;
    }

    public void setLeitsymptomatik_beschreibung(String leitsymptomatik_beschreibung){
        this.leitsymptomatik_beschreibung=leitsymptomatik_beschreibung;
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
