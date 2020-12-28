package dto;

public class Hmr_disziplin{
    int id;
    private String disziplin_beschreibung;
    private String gueltig_von;
    private String gueltig_bis;
    private String ccid;

    public String getDisziplin_beschreibung(){
        return disziplin_beschreibung;
    }

    public void setDisziplin_beschreibung(String disziplin_beschreibung){
        this.disziplin_beschreibung=disziplin_beschreibung;
    }

    public String getGueltig_von(){
        return gueltig_von;
    }

    public void setGueltig_von(String gueltig_von){
        this.gueltig_von=gueltig_von;
    }

    public String getGueltig_bis(){
        return gueltig_bis;
    }

    public void setGueltig_bis(String gueltig_bis){
        this.gueltig_bis=gueltig_bis;
    }

    public String getCcid(){
        return ccid;
    }

    public void setCcid(String ccid){
        this.ccid=ccid;
    }
}
