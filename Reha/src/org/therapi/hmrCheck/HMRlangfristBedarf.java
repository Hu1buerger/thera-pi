package org.therapi.hmrCheck;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class HMRlangfristBedarf {
    
    private File xml;
    private Document doc;
    private Element root;
    private Element voBedList;
    private String typeOfVoBedarf = null;
    
    private static String cKG = "I";    // <kapitel V="I" DN="Maßnahmen der Physiotherapie">
    private static String cPO = "II";   // <kapitel V="II" DN="Maßnahmen der Podologischen Therapie">
    private static String cLO = "III";  // <kapitel V="III" DN="Maßnahmen der Stimm-, Sprech-, Sprach- und Schlucktherapie">
    private static String cER = "IV";   // <kapitel V="IV" DN="Maßnahmen der Ergotherapie">
    private static String cEN = "V";    // <kapitel V="V" DN="Maßnahmen der Ernährungstherapie">
    static HashMap<String,String> hmKapitel = new HashMap<>();
    

    public HMRlangfristBedarf(File xml) {
        this.xml = xml;
        this.loadXML();
        hmKapitel.put( "Physio", cKG );
        hmKapitel.put( "Ergo", cER );
        hmKapitel.put( "Logo", cLO );
        hmKapitel.put( "Podo", cPO );
    }
    
    public String getKapitelFromHMap(String disziKurz) {
        return hmKapitel.get( disziKurz );
    }

    private void loadXML() {
        try {
            this.doc = new SAXBuilder().build(this.xml);
            this.root = doc.getRootElement();
            Element body =  this.root.getChild("body", this.root.getNamespace());
            Element sdhma = body.getChild("sdhma_stammdaten", body.getNamespace("sdhma_stammdaten"));
            this.voBedList = sdhma.getChild("verordnungsbedarf_liste", sdhma.getNamespace("verordnungsbedarf_liste"));
        } catch (JDOMException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    public boolean isBesVoBedOrLangfristBed(String ICD10, String diszi, String diagnosegruppe) {
        Element foundVoBedarf = null;
        for(Element voBedarf : this.voBedList.getChildren()) {
            Element currIcd = voBedarf.getChild("icd_code", voBedarf.getNamespace("icd_code"));
            if (currIcd == null) {
                continue;
            }
            Element hmlist = voBedarf.getChild("heilmittel_liste", voBedarf.getNamespace("heilmittel_liste"));
            Element hm = hmlist.getChild("heilmittel", hmlist.getNamespace("heilmittel"));
            Element bedarf = hm.getChild("anlage_heilmittelvereinbarung", hm.getNamespace("anlage_heilmittelvereinbarung"));
            if(currIcd.getAttribute("V").getValue().equals(ICD10)) {
                foundVoBedarf = voBedarf;
                break;
            }
        }
        if (foundVoBedarf != null) {
            Element hmlist = foundVoBedarf.getChild("heilmittel_liste", foundVoBedarf.getNamespace("heilmittel_liste"));
            Element hm = hmlist.getChild("heilmittel", hmlist.getNamespace("heilmittel"));
            Element bedarf = hm.getChild("anlage_heilmittelvereinbarung", hm.getNamespace("anlage_heilmittelvereinbarung"));
            typeOfVoBedarf = bedarf.getAttribute("V").getValue();
            Element kaplist = hm.getChild("kapitel_liste", hm.getNamespace("kapitel_liste"));
            Element kapitel = null;
            for(Element c : kaplist.getChildren()) {
                if(c.getAttribute("V").getValue().equals(diszi)) {
                    kapitel = c;
                }
            }
            if (kapitel != null) {
                Element dglist = kapitel.getChild("diagnosegruppe_liste", kapitel.getNamespace("diagnosegruppe_liste"));
                Element diagnosegr = null;
                for(Element c : dglist.getChildren()) {
                    if(c.getAttribute("V").getValue().equals(diagnosegruppe)) {
                        diagnosegr = c;
                    }
                }
                if (diagnosegr != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getTypeOfVoBedarf() {
        return (typeOfVoBedarf != null ? typeOfVoBedarf : "");
    }
    
}
