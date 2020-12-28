package dto;

public class Hmr_diagnosegruppe{
    int id;
    String diagnosegruppe;
    String diagnosegruppe_beschreibung;
    double anzahl_vo;
    double max_behandlungen;
    java.util.Date gueltig_von;
    java.util.Date gueltig_bis;
    String ccid;

    @Override
    public String toString() {
        return "Hmr_diagnosegruppe [id=" + id + ", diagnosegruppe=" + diagnosegruppe + ", diagnosegruppe_beschreibung="
                + diagnosegruppe_beschreibung + ", anzahl_vo=" + anzahl_vo + ", max_behandlungen=" + max_behandlungen
                + ", gueltig_von=" + gueltig_von + ", gueltig_bis=" + gueltig_bis + ", ccid=" + ccid + "]";
    }



}
