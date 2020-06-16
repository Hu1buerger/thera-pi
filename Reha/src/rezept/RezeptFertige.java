/**
 * Small class to deal with "fertige" Rezepte - a table in DB that holds a list of Rezepte that have been completed/locked.
 * 
 * TLDR; 
 * Each entry in that list is comprised of RzNr, RzID and some Krankenkassen-info (the institution that is going to be billed)
 * Every Rezept in this list must have its "Abschluss"-bool set to true in the main-Rezepte-DB (verordn). The opposite should also
 * hold true - if a Rezept (in main-Rezept-DB) has this bool set to false, it should not be listed under "fertige".
 */
package rezept;

import java.util.Objects;
import java.util.Optional;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mandant.IK;

/**
 * Class dealing with anything to do with "Rezepte Abschliessen"
 * It will create a "fertiges Rezept" by combining data from the passed in Rez with data from the krankenkasseAdr table.
 * The passed in Rezept will be updated & the dataset added or deleted from "fertige" table.
 * 
 *
 */
public class RezeptFertige {

    private static final Logger logger = LoggerFactory.getLogger(RezeptFertige.class);

    private IK ikKTraeger = IK.INVALIDIK;
    private IK ikKasse = IK.INVALIDIK;
    private String kassenName = "";
    private String rezNr = "";
    private int patientIntern;
    private String rezklasse = "";
    private core.Disziplin Disziplin; // TODO: change to disziplin-class/type
    private String idktraeger; // FIXME: This is dead meat in DB
    private String edifact = "";
    private boolean ediok = false;
    private int id;
    
    private int rezId;
    private int kid;
//    private Rezept rez;

    private static IK ik;

    public RezeptFertige() {
    }

    public RezeptFertige(Rezept rez, IK Ik) {
        ik = Ik;
        kassenName = rez.getKTraegerName();
        rezId = rez.getId();
        rezNr = rez.getRezNr();
        patientIntern = rez.getPatIntern();
        kid = rez.getkId();
        logger.debug("Rezklasse: " + rez.getRezClass());
        rezklasse=rez.getRezClass();
        // rez = Rez;
    }

    public RezeptFertige(IK Ik) {
        ik = Ik;
    }
    
    public boolean RezeptErledigt() {
        if ( rezNr == null || rezNr.isEmpty() || kid == 0 ) {
            logger.error("Need a proper Rezept to operate on - class not initialized with Rezept?");
            return false;
        }
        
        KrankenkasseAdrDto kkDto = new KrankenkasseAdrDto(ik);
        RezeptDto rDto = new RezeptDto(ik);
        RezeptFertigeDto rfDto = new RezeptFertigeDto(ik);
        Optional<KrankenkasseAdr> kka = kkDto.getIKsById(kid);
        if (kka.isPresent()) {
            ikKTraeger = kka.get()
                            .getIkKostenTraeger();
            ikKasse = kka.get()
                         .getIkKasse();
            logger.debug("kka: " + kka.toString());
        } else {
            logger.error("keine Krankenkasse gefunden fuer " + kid + " im Rezept: " + rezNr);
            return false;
        }

        /*
        kassenName = rez.getKTraegerName();
        rezNr = rez.getRezNr();
        patientIntern = rez.getPatIntern();
        Disziplin = rez.disziplin; // TODO: no getter yet / type-cast
        rez.setAbschluss(true); // Do we want to pass this back?
        */
        rfDto.saveToDB(this);
        // rDto.rezeptInDBSpeichern(rez);
        rDto.rezeptAbschluss(rezId, true);
        
        return true;
    }

    public boolean RezeptRevive() {
        RezeptFertigeDto rfDto = new RezeptFertigeDto(ik);
        RezeptDto rDto = new RezeptDto(ik);

        if ( rezId == 0 ) {
            logger.error("Need a proper Rezept to operate on - class not initialized with Rezept?");
            return false;
        }
        /*
        // rez.setAbschluss(false);
        if (!rfDto.deleteByRezNr(rez.getRezNr())) {
            logger.error("Problems deleting entry in fertige");
            return false;
        };
        */
        if (!rfDto.deleteByRezNr(rezNr)) {
            logger.error("Problems deleting entry in fertige");
            return false;
        };
        rDto.rezeptAbschluss(rezId, false);
        
        return true;
    }

    public IK getIkKTraeger() {
        return ikKTraeger;
    }

    public void setIkKTraeger(IK ikKTraeger) {
        this.ikKTraeger = ikKTraeger;
    }

    public IK getIkKasse() {
        return ikKasse;
    }

    public void setIkKasse(IK ikKasse) {
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

    // Default standard hashcode & equals omitting the ID field
    @Override
    public int hashCode() {
        return Objects.hash(Disziplin, edifact, ediok, idktraeger, ikKTraeger, ikKasse, kassenName, patientIntern,
                rezNr, rezklasse);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RezeptFertige other = (RezeptFertige) obj;
        return Disziplin == other.Disziplin && Objects.equals(edifact, other.edifact) && ediok == other.ediok
                && Objects.equals(idktraeger, other.idktraeger) && Objects.equals(ikKTraeger, other.ikKTraeger)
                && Objects.equals(ikKasse, other.ikKasse) && Objects.equals(kassenName, other.kassenName)
                && patientIntern == other.patientIntern && Objects.equals(rezNr, other.rezNr)
                && Objects.equals(rezklasse, other.rezklasse);
    }

    @Override
    public String toString() {
        return "RezeptFertige [ikKTraeger=" + ikKTraeger + ", ikKasse=" + ikKasse + ", kassenName=" + kassenName
                + ", rezNr=" + rezNr + ", patientIntern=" + patientIntern + ", rezklasse=" + rezklasse + ", Disziplin="
                + Disziplin + ", idktraeger=" + idktraeger + ", edifact=" + edifact + ", ediok=" + ediok + ", id=" + id + "]";
    }

}
