package commonData;

import java.util.Optional;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.SqlInfo;
import CommonTools.StringTools;

public class Rezeptvector {
    private static final Logger LOGGER = LoggerFactory.getLogger(Rezeptvector.class);
    private Vector<Vector<String>> vecvec_rezepte;
    private Vector<String> vec_rezept;

    public Rezeptvector() {
        vecvec_rezepte = new Vector<>();
        vec_rezept = new Vector<>();
    }

    public boolean init(String rezNr) {
        String cmd = "select * from verordn where rez_nr='" + rezNr.trim() + "' LIMIT 1";
        this.vecvec_rezepte = SqlInfo.holeFelder(cmd);
        if (this.vecvec_rezepte.isEmpty()) {
            LOGGER.debug("RezeptVektor ist leer");
            this.vec_rezept = null;
            return false;
        }
        this.vec_rezept = this.vecvec_rezepte.get(0);
        return true;
    }

    public boolean createEmptyVec() {
        try {
            String cmd = "describe verordn";
            Vector<Vector<String>> spaltennamen = SqlInfo.holeFelder(cmd);
            for (int i = 0; i < spaltennamen.size(); i++) {
                this.vec_rezept.add(i, "");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean getBoolAt(int index) {
        return "T".equals(Optional.ofNullable(vec_rezept.get(index)).orElse("")
                              .trim());
    }

    private void setBoolAt(int index, boolean data) {
        this.vec_rezept.set(index, data ? "T" : "F");
    }

    private void setBoolAt(int index, String data) {
        if ("F".equalsIgnoreCase(data) || "T".equalsIgnoreCase(data)) {
            this.vec_rezept.set(index, data.toUpperCase());
        } else {
            LOGGER.debug("Fehler setBoolAt(String) idx: " + index + " val: " + data);
        }
    }

    private int getIntAt(int index) {
        return StringTools.ZahlTest(this.vec_rezept.get(index));
    }

    private void setIntAt(int index, int data) {
        this.vec_rezept.set(index, Integer.valueOf(data)
                                          .toString());
    }

    private void setIntAt(int index, String data) {
        this.vec_rezept.set(index, Integer.valueOf(data)
                                          .toString());
    }

    private String getStringAt(int index) {
        return Optional.ofNullable(vec_rezept.get(index)).orElse("")
                              .trim();
    }

    private void setStringAt(int index, String data) {
        this.vec_rezept.set(index, Optional.ofNullable(data).orElse("").trim());
    }

    public Vector<Vector<String>> getVecVec_rez() {
        return this.vecvec_rezepte;
    }
    /** Kompatibilitätsmodus Ende. */

    public Vector<String> getVec_rez() {
        return vec_rezept;
    }

    public void setVec_rez(Vector<String> vec_rez) {
        this.vec_rezept = vec_rez;
    }

    private int getVecSize() {
        return vec_rezept.size();
    }

    public boolean isEmpty() {
        return getVecSize() <= 0;
    }

    public String getPatIntern() {
        return getStringAt(0);
    }

    public void setPatIntern(String patInt) {
        setStringAt(0, patInt);
    }

    public String getRezNb() {
        return getStringAt(1);
    }

    public void setRezNb(String RezNb) {
        setStringAt(1, RezNb);
    }

    public String getRezClass() {
        return getStringAt(1).substring(0, 2)
                             .toUpperCase();
    }

    public String getRezeptDatum() {
        return getStringAt(2);
    }

    public void setRezeptDatum(String rezDat) {
        setStringAt(2, rezDat);
    }

    /**
     * Gibt Anzahl der Behandlungen für den angefragten Index zurück.
     *
     * @param index zu ermittelnde Position im Rezept(1..4)
     *
     * @return Anzahl der Behandlungen
     */
    public int getAnzBeh(int index) {
        return getIntAt(2 + index);
    }

    public String getAnzBehS(int index) {
        return getStringAt(2 + index);
    }

    public void setAnzBeh(int index, String data) {
        if (0 < index && index < 5) {
            setStringAt(2 + index, data);
        } else {
            LOGGER.debug("Indexfehler setAnzBeh: " + index);
        }
    }

    public String getKm() {
        return getStringAt(7);
    }

    public void setKm(String distance) {
        setStringAt(7, distance);
    }

    /**
     * Gibt Preislisten-ID der HM-Position des angefragten Index zurück.
     *
     * @param index zu ermittelnde Position im Rezept(1..4)
     *
     * @return ID der verordneten Behandlung in der Preisliste
     */
    public String getArtDBehandl(int index) {
        return getStringAt(7 + index);
    }

    public void setArtDBehandl(int index, String data) {
        if (0 < index && index < 5) {
            setStringAt(7 + index, data);
        } else {
            LOGGER.debug("Indexfehler setArtDBehandl: " + index);
        }
    }

    public Double getGebuehrBetrag() {
        return Double.parseDouble(vec_rezept.get(13));
    }

    public void setGebuehrBetrag(Double data) {
        setStringAt(13, data.toString());
    }

    public void setGebuehrBetrag(String data) {
        setStringAt(13, data);
    }

    public boolean getBefreit() {
        return getBoolAt(12);
    }

    public String getBefreitS() {
        return getStringAt(12);
    }

    public void setBefreit(String data) {
        setBoolAt(12, data);
    }

    public boolean getGebuehrBezahlt() {
        return getBoolAt(14);
    }

    public String getGebuehrBezahltS() {
        return getStringAt(14);
    }

    public void setGebuehrBezahlt(boolean data) {
        setBoolAt(14, data);
    }

    public String getArzt() {
        return getStringAt(15);
    }

    public void setArzt(String arzt) {
        setStringAt(15, arzt);
    }

    public String getArztId() {
        return getStringAt(16);
    }

    public void setArztId(String arztId) {
        setStringAt(16, arztId);
    }

    /**
     * Gibt Preis der Behandlung für den angefragten Index zurück.
     *
     * @param index zu ermittelnde Position im Rezept(1..4)
     *
     * @return Preis der Behandlung
     */
    private String getPreis(int index) {
        return getStringAt(17 + index);
    }

    public void setPreis(int index, String data) {
        if (0 < index && index < 5) {
            setStringAt(17 + index, data);
        } else {
            LOGGER.debug("Indexfehler setPreis: " + index);
        }
    }

    public String getAngelegtDatum() {
        return getStringAt(22);
    }

    public void setAngelegtDatum(String Dat) {
        setStringAt(22, Dat);
    }

    public String getDiagn() {
        return getStringAt(23);
    }

    public void setDiagn(String diag) {
        setStringAt(23, diag);
    }

    public boolean getHeimbew() {
        return getBoolAt(24);
    }

    public String getHeimbewS() {
        return getStringAt(24);
    }

    public void setHeimbew(boolean data) {
        setBoolAt(24, data);
    }

    public void setHeimbew(String data) {
        setBoolAt(24, data);
    }

    public int getRezArt() {
        return getIntAt(27);
    }

    public void setRezArt(int rezArt) {
        setIntAt(27, rezArt);
    }

    public String getTermine() {
        return getStringAt(34);
    }

    public void setTermine(String termine) {
        LOGGER.debug(termine);
        setStringAt(34, termine);
    }

    public String getId() {
        return getStringAt(35);
    }

    private void setId(int id) {
        setIntAt(35, id);
    }

    /**
     * @return Name der Kasse
     */
    public String getKtrName() {
        return getStringAt(36);
    }

    public void setKtrName(String ktrName) {
        setStringAt(36, ktrName);
    }

    /**
     * @return Kassen-Id
     */
    public String getKtraeger() {
        return getStringAt(37);
    }

    public void setKtraeger(String ktraeger) {
        setStringAt(37, ktraeger);
    }

    public String getPatIdS() {
        return getStringAt(38);
    }

    public void setPatIdS(String id) {
        setStringAt(38, id);
    }

    public String getZzStat() {
        return getStringAt(39);
    }

    public void setZzStat(String stat) {
        setStringAt(39, stat);
    }

    /**
     * @return spaetester Behandlungsbeginn
     */
    public String getLastDate() {
        return getStringAt(40);
    }

    public void setLastDate(String lastDat) {
        setStringAt(40, lastDat);
    }

    public String getPreisgruppe() {
        return getStringAt(41);
    }

    public void setPreisgruppe(String data) {
        setStringAt(41, data);
    }

    public boolean getBegrAdR() {
        return getBoolAt(42);
    }

    public String getBegrAdRS() {
        return getStringAt(42);
    }

    public void setBegrAdR(boolean data) {
        setBoolAt(42, data);
    }

    public boolean getHausbesuch() {
        return getBoolAt(43);
    }

    public String getHausbesuchS() {
        return getStringAt(43);
    }

    public void setHausbesuch(boolean data) {
        setStringAt(43, data ? "T" : "F");
    }

    public String getIndiSchluessel() {
        return getStringAt(44);
    }

    public void setIndiSchluessel(String data) {
        setStringAt(44, data);
    }

    public String getAngelegtVon() {
        return getStringAt(45);
    }

    public void setAngelegtVon(String user) {
        setStringAt(45, user);
    }

    public int getBarcodeform() {
        return getIntAt(46);
    }

    public void setBarcodeform(int data) {
        setStringAt(46, Integer.valueOf(data)
                               .toString());
    }

    public String getDauer() {
        return getStringAt(47);
    }

    public void setDauer(String data) {
        setStringAt(47, data);
    }

    /**
     * Gibt HM-Pos der Behandlung für den angefragten Index zurück.
     *
     * @param index zu ermittelnde Position im Rezept(1..4)
     *
     * @return HM-Pos der Behandlung
     */
    public String getHmPos(int index) {
        return getStringAt(47 + index);
    }

    public void setHmPos(int index, String data) {
        if (0 < index && index < 5) {
            setStringAt(47 + index, data);
        } else {
            LOGGER.debug("Indexfehler setHmPos: " + index);
        }
    }

    public String getFrequenz() {
        return getStringAt(52);
    }

    public void setFrequenz(String data) {
        setStringAt(52, data);
    }

    public String getLastEdit() {
        return getStringAt(53);
    }

    public void setLastEdit(String user) {
        setStringAt(53, user);
    }

    public String getArztBerichtID() {
        return getStringAt(54);
    }

    public boolean getArztbericht() {
        return getBoolAt(55);
    }

    public String getArztberichtS() {
        return getStringAt(55);
    }

    public void setArztBericht(boolean data) {
        setStringAt(55, data ? "T" : "F");
    }

    public String getLastEdDate() {
        return getStringAt(56);
    }

    public void setLastEdDate(String lastEdDat) {
        setStringAt(56, lastEdDat);
    }

    public int getFarbCode() {
        return getIntAt(57);
    }

    public void setFarbCode(int code) {
        setStringAt(57, Integer.valueOf(code)
                               .toString());
    }

    private String getvorJahrFrei() {
        return getStringAt(59);
    }

    public void setvorJahrFrei(String data) {
        setStringAt(59, data);
    }

    public boolean getUnter18() {
        return getBoolAt(60);
    }

    public String getUnter18S() {
        return getStringAt(60);
    }

    public void setUnter18(String data) {
        setBoolAt(60, data);
    }

    public void setUnter18(boolean data) {
        setBoolAt(60, data);
    }

    /**
     * Prüft, ob Hausbesuch voll abrechenbar ist.
     *
     * @return Flag; TRUE, wenn Hausbesuch voll abrechenbar ist.
     */
    public boolean getHbVoll() {
        return getBoolAt(61);
    }

    public String getHbVollS() {
        return getStringAt(61);
    }

    public void setHbVoll(boolean data) {
        setBoolAt(61, data);
    }

    public int getZzRegel() {
        return getIntAt(63);
    }

    public void setZzRegel(int rule) {
        setIntAt(63, rule);
    }

    public int getAnzHB() {
        return getIntAt(64);
    }
    public String getAnzHBS() {
        return getStringAt(64);
    }

    public void setAnzHB(String hb) {
        setIntAt(64, hb);
    }

    /**
     * Gibt HM-Kuerzel für den angefragten Index zurück.
     *
     * @param index zu ermittelnde Position im Rezept(1..4)
     *
     * @return HM-Kuerzel
     */
    public String getHMkurz(int index) {
        return getStringAt(64 + index);
    }

    public void setHMkurz(int index, String data) {
        if (0 < index && index < 7) { // es gibt 6 Kuerzel!
            setStringAt(64 + index, data);
        } else {
            LOGGER.debug("Indexfehler setHMkurz: " + index);
        }
    }

    public String getICD10() {
        return getStringAt(71);
    }

    public void setICD10(String data) {
        setStringAt(71, data);
    }

    public String getICD10_2() {
        return getStringAt(72);
    }

    public void setICD10_2(String data) {
        setStringAt(72, data);
    }
    
    public String getSymFrei() {
    	return getStringAt(78);
    }
    
    public String getDringlich() {
    	return getStringAt(79);
    }
    
    public String getSymA() {
    	return getStringAt(74);
    }
    public String getSymB() {
    	return getStringAt(75);
    }
    public String getSymC() {
    	return getStringAt(76);
    }
    public String getSymX() {
    	return getStringAt(77);
    }
    
    

    public boolean getUseHygPausch() {
        return getBoolAt(73);
    }
    public String getUseHygPauschS() {
        return getStringAt(73);
    }
    public void setUseHygPausch(boolean data) {
        setStringAt(73, data ? "T" : "F");
    }
    public void setNewRezNb(String rezClass) {
        int reznr = SqlInfo.erzeugeNummer(rezClass.toLowerCase());
        if (reznr < 0) {
            JOptionPane.showMessageDialog(null, "Schwerwiegender Fehler beim Bezug einer neuen Rezeptnummer!");
            return;
        }
        setRezNb(rezClass.toUpperCase() + Integer.valueOf(reznr));

        int rezidneu = SqlInfo.holeId("verordn", "diagnose");
        setId(rezidneu);
    }

    public void writeRez2DB() {
        StringBuilder cmd = new StringBuilder("update verordn set ");
        cmd.append("pat_intern='")
               .append(getPatIntern())
               .append("', ");
        cmd.append("rez_nr='")
               .append(getRezNb())
               .append("', ");
        cmd.append("rez_datum='")
               .append(getRezeptDatum())
               .append("', ");
        for (int i = 1; i < 5; i++) {
            String idx = Integer.valueOf(i)
                                .toString();
            cmd.append("anzahl")
                   .append(idx)
                   .append("='")
                   .append(getAnzBehS(i))
                   .append("', ");
            cmd.append("art_dbeh")
                   .append(idx)
                   .append("='")
                   .append(getArtDBehandl(i))
                   .append("', ");
            cmd.append("preise")
                   .append(idx)
                   .append("='")
                   .append(getPreis(i))
                   .append("', ");
            cmd.append("pos")
                   .append(idx)
                   .append("='")
                   .append(getHmPos(i))
                   .append("', ");
            cmd.append("kuerzel")
                   .append(idx)
                   .append("='")
                   .append(getHMkurz(i))
                   .append("', ");
        }
        for (int i = 5; i < 7; i++) {
            String idx = Integer.valueOf(i)
                                .toString();
            cmd.append("kuerzel")
                   .append(idx)
                   .append("='")
                   .append(getHMkurz(i))
                   .append("', ");
        }
        cmd.append("anzahlkm='")
               .append(getKm())
               .append("', ");
        cmd.append("befr='")
               .append(getBefreitS())
               .append("', ");
        // rez_geb
        cmd.append("rez_bez='")
               .append(getGebuehrBezahltS())
               .append("', ");
        cmd.append("arzt='")
               .append(getArzt())
               .append("', ");
        cmd.append("arztid='")
               .append(getArztId())
               .append("', ");
        // aerzte
        cmd.append("datum='")
               .append(getAngelegtDatum())
               .append("', ");
        cmd.append("diagnose='")
               .append(getDiagn())
               .append("', ");
        cmd.append("heimbewohn='")
               .append(getHeimbewS())
               .append("', ");
        // veraenderd, veraendera
        cmd.append("rezeptart='")
               .append(getRezArt())
               .append("', ");
        // logfrei1, logfrei2, numfrei1, numfrei2, charfrei1, charfrei2, termine, id
        cmd.append("ktraeger='")
               .append(getKtrName())
               .append("', ");
        cmd.append("kid='")
               .append(getKtraeger())
               .append("', ");
        cmd.append("patid='")
               .append(getPatIdS())
               .append("', ");
        cmd.append("zzstatus='")
               .append(getZzStat())
               .append("', ");
        cmd.append("lastdate='")
               .append(getLastDate())
               .append("', ");
        cmd.append("preisgruppe='")
               .append(getPreisgruppe())
               .append("', ");
        cmd.append("begruendadr='")
               .append(getBegrAdRS())
               .append("', ");
        cmd.append("hausbes='")
               .append(getHausbesuchS())
               .append("', ");
        cmd.append("indikatschl='")
               .append(getIndiSchluessel())
               .append("', ");
        cmd.append("angelegtvon='")
               .append(getAngelegtVon())
               .append("', ");
        cmd.append("barcodeform='")
               .append(getBarcodeform())
               .append("', ");
        cmd.append("dauer='")
               .append(getDauer())
               .append("', ");
        cmd.append("frequenz='")
               .append(getFrequenz())
               .append("', ");
        cmd.append("lastedit='")
               .append(getLastEdit())
               .append("', ");
        // berid
        cmd.append("arztbericht='")
               .append(getArztberichtS())
               .append("', ");
        cmd.append("lasteddate='")
               .append(getLastEdDate())
               .append("', ");
        cmd.append("farbcode='")
               .append(getFarbCode())
               .append("', ");
        // rsplit
        cmd.append("jahrfrei='")
               .append(getvorJahrFrei())
               .append("', ");
        cmd.append("unter18='")
               .append(getUnter18S())
               .append("', ");
        cmd.append("hbvoll='")
               .append(getHbVollS())
               .append("', ");
        // abschluss
        cmd.append("zzregel='")
               .append(getZzRegel())
               .append("', ");
        cmd.append("anzahlhb='")
               .append(getAnzHB())
               .append("', ");
        cmd.append("icd10='")
               .append(getICD10())
               .append("', ");
        cmd.append("icd10_2='")
               .append(getICD10_2())
               .append("', ");
        cmd.append("pauschale='")
               .append(getUseHygPauschS())
               .append("' ");

        cmd.append(" where id='")
               .append(getId())
               .append("' LIMIT 1");
        SqlInfo.sqlAusfuehren(cmd.toString());
    }

    public Vector<String> getAktuellesRezept() {
        return vec_rezept;
    }
}
