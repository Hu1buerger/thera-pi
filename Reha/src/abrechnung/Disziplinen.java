package abrechnung;

import java.util.ArrayList;
import java.util.Arrays;

import CommonTools.JRtaComboBox;
import systemEinstellungen.SystemConfig;

public class Disziplinen {
    private String[] typeOfVerordnung;
    private ArrayList<String> diszis;
    private String[] rezeptKlassen;
    private String[] hmCodePrefix;
    private JRtaComboBox cmbDiszi;
    private ArrayList<String> listTypeOfVO;
    private String[] typeOfActiveVerordnung;
    private String[] aktiveRezeptKlassen;
    private JRtaComboBox cmbDisziActive;
    private ArrayList<String> listActiveTypeOfVO;

    /**
     * verwaltet die enthaltenen Heilmittelsparten
     */
    public Disziplinen() {
        // kleiner Überblick über das real existierende Chaos
        // aus HMRCheck
        //      String diszis[] =    {"2", "1", "5", "3", "8", "7"};    // auch in SysUtilTarifgruppen
        //      String diszikurz[] = {"KG","MA","ER","LO","RH","PO"};   // <- gibt es keine RH-Rezepte (? s.u.)
        
        // aus SysUtilTarifgruppen          // <- McM: das ist die umfangreichste - als Basis nehmen?
        //      tabName = new String[]    {"kgtarif","matarif","ertarif","lotarif","rhtarif","potarif","rstarif","fttarif"}; 
        //      dummydiszi = new String[] {"Physio","Massage","Ergo","Logo","Reha","Podo","Rsport","Ftrain"};

        // aus Rezeptneuanlage (+REHA-Verordnung, -Rehasport-Rezept, -Funktionstraining-Rezept)
        // String[] idiszi = {"Physio-Rezept","Massage/Lymphdrainage-Rezept","Ergotherapie-Rezept","Logopädie-Rezept","REHA-Verordnung","Podologie-Rezept"};

        diszis = new ArrayList<String>(Arrays.asList("Physio","Massage","Ergo","Logo","Podo","Rsport","Ftrain"));   // aus AktuelleRezepte
        rezeptKlassen = new String [] {"KG","MA","ER","LO","PO","RS","FT"};     // aus AbrechnungGKV (!)
        hmCodePrefix = new String [] {"2","1","5","3","7","8",""};              // passende Prefixe nach HMpositionsnummernverzeichnis
                                                                                // McM: wird "8" auch für "RH" u. "FT" verwandt?

        // erst eine Arraylist mit Auswahleinträgen erzeugen (<- die kann 'wachsen'; ein
        // Array nicht), ...
        // verwendet in AbrechnungGKV(!) McM: d.h. Reha-Verordnungen werden nicht
        // elektron. abgerechnet?
        listTypeOfVO = new ArrayList<String>(Arrays.asList("Physio-Rezept", "Massage/Lymphdrainage-Rezept",
                "Ergotherapie-Rezept", "Logopädie-Rezept", "Podologie-Rezept"));
        if (SystemConfig.mitRs) {
            listTypeOfVO.addAll(Arrays.asList("Rehasport-Rezept", "Funktionstraining-Rezept"));
        }
        typeOfVerordnung = new String[listTypeOfVO.size()]; // ... daraus das Array fuer die ComboBox erstellen ...
        listTypeOfVO.toArray(typeOfVerordnung); // ... und fuellen
        this.cmbDiszi = new JRtaComboBox(typeOfVerordnung);
        String initVal = SystemConfig.initRezeptKlasse;
        if (initVal.equals(null) || initVal.isEmpty()) {
            initVal = SystemConfig.rezeptKlassenAktiv.get(0)
                                                     .get(0);
        }
        if (initVal.equals(null) || initVal.isEmpty()) {
            initVal = listTypeOfVO.get(0);
        }
        cmbDiszi.setSelectedItem(initVal); // default setzen

        // weitere ComboBox mit nur den aktiven Rezeptklassen erzeugen
        listActiveTypeOfVO = new ArrayList<String>();
        int aktiveKlassen = SystemConfig.rezeptKlassenAktiv.size();
        aktiveRezeptKlassen = new String[aktiveKlassen];
        for (int i = 0; i < aktiveKlassen; i++) {
            listActiveTypeOfVO.add(SystemConfig.rezeptKlassenAktiv.get(i)
                                                                  .get(0));
            aktiveRezeptKlassen[i] = SystemConfig.rezeptKlassenAktiv.get(i)
                                                                    .get(1);
        }
        typeOfActiveVerordnung = new String[aktiveKlassen];
        listActiveTypeOfVO.toArray(typeOfActiveVerordnung);
        this.cmbDisziActive = new JRtaComboBox(typeOfActiveVerordnung);
        cmbDisziActive.setSelectedItem(SystemConfig.initRezeptKlasse);
    }

    public JRtaComboBox getComboBox() {
        return this.cmbDiszi;
    }

    public String getCurrDisziKurz() {
        if (diszis.size() >= cmbDiszi.getSelectedIndex()) {
            return diszis.get(cmbDiszi.getSelectedIndex());
        } else {
            System.out.println("getCurrDiszi err: size " + diszis.size() + " vs idx " + cmbDiszi.getSelectedIndex());
            return diszis.get(0); // use default ("Physio")
        }
    }

    public int getCurrDisziIdx() {
        return this.getIndex(getCurrDisziKurz());
    }

    public void setCurrTypeOfVO(String currTypeOfVO) {
        this.cmbDiszi.setSelectedItem(currTypeOfVO);
    }

    /**
     * liefert die aktuell ausgewählte Rezeptklasse
     */
    public String getCurrRezClass() {
        if (rezeptKlassen.length >= cmbDiszi.getSelectedIndex()) {
            return rezeptKlassen[cmbDiszi.getSelectedIndex()];
        } else {
            System.out.println("getCurrDiszi err: size " + diszis.size() + " vs idx " + cmbDiszi.getSelectedIndex());
            return rezeptKlassen[0]; // use default ("KG")
        }
    }
    
    public boolean currIsPhysio() {
        return this.getCurrDisziKurz().equals("Physio");
    }
    public boolean currIsMassage() {
        return this.getCurrDisziKurz().equals("Massage");
    }
    public boolean currIsErgo() {
        return this.getCurrDisziKurz().equals("Ergo");
    }
    public boolean currIsLogo() {
        return this.getCurrDisziKurz().equals("Logo");
    }
    public boolean currIsPodo() {
        return this.getCurrDisziKurz().equals("Podo");
    }
    public boolean currIsRsport() {
        return this.getCurrDisziKurz().equals("Rsport");
    }
    public boolean currIsFtrain() {
        return this.getCurrDisziKurz().equals("Ftrain");
    }

    /**
     * liefert Index der als Kurzbezeichnung (z.B. "Physio") uebergebenen Disziplin
     */
    public int getIndex(String Disziplin) {
        return diszis.indexOf(Disziplin);
    }

    /**
     * liefert String-Feld der aktiven Rezeptklassen (z.B. [KG, PO])
     */
    public String[] getActiveRK() {
        return this.aktiveRezeptKlassen;
    }

    /**
     * liefert ComboBox, die Verordnungen der aktiven HM-Sparten auflistet
     */
    public JRtaComboBox getComboBoxActiveRK() {
        return this.cmbDisziActive;
    }

    public String getCurrDisziFromActRK() {
        return cmbDisziActive.getSelectedItem()
                             .toString();
    }

    public void setCurrDisziActRK(String currTypeOfVO) {
        this.cmbDisziActive.setSelectedItem(currTypeOfVO);
    }

    /**
     * liefert numer. Prefix des HM-Codes fuer die uebergebene Disziplin
     */
    public String getPrefix(int Disziplin) {
        return hmCodePrefix[Disziplin];
    }

    /**
     * liefert den Rezeptklasse-Prefix (z.B. "KG") fuer die uebergebene Disziplin
     */
    public String getRezClass(int Disziplin) {
        return rezeptKlassen[Disziplin];
    }

    /**
     * liefert Kurzbezeichnung (z.B. "Physio") der uebergebenen Disziplin
     */
    public String getDisziKurz(int Disziplin) {
        return diszis.get(Disziplin);
    }

    /**
     * liefert Kurzbezeichnung (z.B. "Physio") zur uebergebenen Rezeptklasse (z.B. "KG")
     */
    public String getDisziKurzFromRK(String rk) {
        ArrayList<String> rKl = new ArrayList<String>(Arrays.asList(rezeptKlassen));
        return getDisziKurz(rKl.indexOf(rk));
    }

    /**
     * liefert Kurzbezeichnung (z.B. "Physio") zum uebergebenen VO-Typ (z.B. "Physio-Rezept")
     */
    public String getDisziKurzFromTypeOfVO(String typeOfVO) {
        for (int i = 0; i < listTypeOfVO.size(); i++) {
            if (typeOfVO.equalsIgnoreCase(listTypeOfVO.get(i))) {
                return diszis.get(i);
            }
        }
        System.out.println("getDiszi err: not found: " + typeOfVO + " set " + diszis.get(0) + " as default");
        return diszis.get(0); // use default ("Physio")
    }

    public String getRezClass(String typeOfVO) {
        for (int i = 0; i < listTypeOfVO.size(); i++) {
            if (typeOfVO.equalsIgnoreCase(listTypeOfVO.get(i))) {
                return rezeptKlassen[i];
            }
        }
        System.out.println("getRezClass err: not found: " + typeOfVO + " set " + rezeptKlassen[0] + " as default");
        return rezeptKlassen[0]; // use default ("KG")
    }

}