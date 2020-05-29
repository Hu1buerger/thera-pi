/**
 * Small class to deal with "fertige" Rezepte - a table in DB that holds a list of Rezepte that have been completed/locked.
 * 
 * TLDR; 
 * Each entry in that list is comprised of RzNr, RzID and some Krankenkassen-info (the institution that is going to be billed)
 * Every Rezept in this list must have its "Abschluss"-bool set to true in the main-Rezepte-DB (verordn). The opposite should also
 * hold true - if a Rezept (in main-Rezept-DB) has this bool set to false, it should not be listed under "fertige".
 */
package rezept;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.SqlInfo;
import hauptFenster.Reha;
import mandant.IK;
import sql.DatenquellenFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 */
public class RezeptFertige {

    private static final Logger logger = LoggerFactory.getLogger(RezeptFertige.class);
    
    private int ikKTraeger;
    private int ikKasse; // TODO: check this
    private String kassenName;
    private String rezNr;
    private int patientIntern;
    private String rezklasse;
    private core.Disziplin Disziplin; // TODO: change to disziplin-class/type
    private String idktraeger;
    private String edifact;
    private boolean ediok;
    private int id;
    
    private static IK ik;
    
    public RezeptFertige() {
        // Lets give 'em buggers some defaults:
        kassenName = "";
        rezNr = "";
        rezklasse = "";
        idktraeger = "";
        edifact = "";
        ediok = false;
       
    }
    
    public RezeptFertige(Rezept rez, IK Ik) {
        ik = Ik;
        this.rezNr = rez.getRezNr();
        this.Disziplin = rez.disziplin; // TODO: no getter yet / type-cast
        this.patientIntern = rez.getPatIntern();
    }

    public RezeptFertige(IK Ik) {
        ik = Ik;
    }

    public void RezeptErledigt(Rezept rez) {
        krankenkasseAdrDto kkDto = new krankenkasseAdrDto(ik);
        RezeptDto rDto = new RezeptDto(ik);
        RezeptFertigeDto rfDto = new RezeptFertigeDto(ik);
        krankenkasseAdr kka = new krankenkasseAdr();
        // RezeptFertige fRez = new RezeptFertige();
        
        kka = kkDto.getIKsById(rez.getkId());
        
        ikKTraeger = kka.getIkKostenTraeger();
        ikKasse = kka.getIkKasse();
        
        kassenName = rez.getKTraegerName();
        rezNr = rez.getRezNr();
        patientIntern = rez.getPatIntern();
        Disziplin = rez.disziplin; // TODO: no getter yet / type-cast
        rez.setAbschluss(true);
        rfDto.saveToDB(this);
        rDto.rezeptInDBSpeichern(rez);
        
    }
 
    public void RezeptRevive(Rezept rez) {
        RezeptFertigeDto rfDto = new RezeptFertigeDto(ik);
        
        rez.setAbschluss(false);
        rfDto.deleteById(rez.getId());
    }
 
    /*
     *             String cmd = "insert into fertige set ikktraeger='" + ikkost + "', ikkasse='" + ikkass + "', " + "name1='"
                    + kname + "', rez_nr='" + rnr + "', pat_intern='" + patint + "', rezklasse='" + rnr.substring(0, 2)
                    + "'";
                    
                    Vector<Vector<String>> kdat = SqlInfo.holeFelder("select ik_kasse,ik_kostent from kass_adr where id='"
                    + Reha.instance.patpanel.rezAktRez.getkId() + "' LIMIT 1");
                    ikkass = kdat.get(0).get(0); // kass_adr.ik_kasse
                    ikkost = kdat.get(0).get(1); // kass_adr.ik_kostent
     */
    
    public static void main(String[] args) {
        logger.debug("arg0= " + args[0]);
        logger.debug("arg1= " + args[1]);
        ik = new IK(args[0]);
        String Id = args[1];
        krankenkasseAdr kkadr = new krankenkasseAdr();
        krankenkasseAdrDto kkadrDto = new krankenkasseAdrDto(ik);
        kkadr = kkadrDto.getIKsById(Integer.valueOf(Id));
        logger.debug("Got: " + kkadr.toString());
    }

    public int getIkKTraeger() {
        return ikKTraeger;
    }

    public void setIkKTraeger(int ikKTraeger) {
        this.ikKTraeger = ikKTraeger;
    }

    public int getIkKasse() {
        return ikKasse;
    }

    public void setIkKasse(int ikKasse) {
        this.ikKasse = ikKasse;
    }

    public String getKassenName() {
        return kassenName;
    }

    public void setKassenName(String kassenName) {
        this.kassenName = kassenName;
    }

    public String getRezNr() {
        return rezNr;
    }

    public void setRezNr(String rezNr) {
        this.rezNr = rezNr;
    }

    public int getPatientIntern() {
        return patientIntern;
    }

    public void setPatientIntern(int patientIntern) {
        this.patientIntern = patientIntern;
    }

    public String getRezklasse() {
        return rezklasse;
    }

    public void setRezklasse(String rezklasse) {
        this.rezklasse = rezklasse;
    }

    public core.Disziplin getDisziplin() {
        return Disziplin;
    }

    public void setDisziplin(core.Disziplin disziplin) {
        Disziplin = disziplin;
    }

    public String getIdKTraeger() {
        return idktraeger;
    }

    public void setIdKTraeger(String idktraeger) {
        this.idktraeger = idktraeger;
    }

    public String getEdifact() {
        return edifact;
    }

    public void setEdifact(String edifact) {
        this.edifact = edifact;
    }

    public boolean isEdiOk() {
        return ediok;
    }
    
    public String getEdiOk() {
        return isEdiOk() ? "T" : "F";
    }

    public void setEdiOk(boolean ediok) {
        this.ediok = ediok;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
}
