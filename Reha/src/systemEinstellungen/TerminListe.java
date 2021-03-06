package systemEinstellungen;

import java.util.Vector;

import environment.Path;
import umfeld.Betriebsumfeld;

public class TerminListe {
    public int AnzahlTerminTabellen;
    public int AnzahlSpaltenProTabellen;
    public Vector<String> NamenSpalten = new Vector<String>();

    public String PatNamenPlatzhalter;
    // public String[] NameTabelle = {null,null,null,null,null,null};
    public int AnzahlTermineProTabelle;
    public String NameTemplate;
    public String NameTerminDrucker;
    public String iniName = "terminliste.ini";
    public String iniPfad = Path.Instance.getProghome() + "ini/" + Betriebsumfeld.getAktIK() + "/";
    public int PatNameDrucken;
    public int MitUeberschrift;
    public boolean DirektDruck;
    public boolean EndlosDruck = false;

    public TerminListe init() {
        AnzahlTerminTabellen = Integer.valueOf(
                RWJedeIni.leseIniDatei(iniPfad, iniName, "TerminListe1", "AnzahlTabellen"));
        AnzahlSpaltenProTabellen = Integer.valueOf(
                RWJedeIni.leseIniDatei(iniPfad, iniName, "TerminListe1", "AnzahlSpaltenProTabellen"));
        for (int i = 0; i < AnzahlSpaltenProTabellen; i++) {
            NamenSpalten.add(RWJedeIni.leseIniDatei(iniPfad, iniName, "TerminListe1", "InhaltSpalte" + (i + 1)));
        }
        AnzahlTermineProTabelle = Integer.valueOf(
                RWJedeIni.leseIniDatei(iniPfad, iniName, "TerminListe1", "AnzahlTermineProTabelle"));
        NameTemplate = RWJedeIni.leseIniDatei(iniPfad, iniName, "TerminListe1", "NameTemplate");
        NameTerminDrucker = RWJedeIni.leseIniDatei(iniPfad, iniName, "TerminListe1", "NameTerminDrucker");
        PatNameDrucken = Integer.valueOf(RWJedeIni.leseIniDatei(iniPfad, iniName, "TerminListe1", "PatNameDrucken"));
        PatNamenPlatzhalter = RWJedeIni.leseIniDatei(iniPfad, iniName, "TerminListe1", "PatNamePlatzhalter");
        MitUeberschrift = Integer.valueOf(
                RWJedeIni.leseIniDatei(iniPfad, iniName, "TerminListe1", "MitSpaltenUeberschrift"));
        DirektDruck = (RWJedeIni.leseIniDatei(iniPfad, iniName, "TerminListe1", "DirektDruck")
                                .trim()
                                .equals("0") ? false : true);
        try {
            if (RWJedeIni.leseIniDatei(iniPfad, iniName, "TerminListe1", "EndlosDruck") == null) {
                RWJedeIni.schreibeIniDatei(iniPfad, iniName, "TerminListe1", "EndlosDruck", "0");
                EndlosDruck = false;
            } else {
                EndlosDruck = (RWJedeIni.leseIniDatei(iniPfad, iniName, "TerminListe1", "EndlosDruck")
                                        .trim()
                                        .equals("0") ? false : true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // System.out.println(AnzahlTerminTabellen);
        //// System.out.println(NameTabelle);
        // System.out.println(AnzahlTermineProTabelle);
        // System.out.println(NameTemplate);
        // System.out.println(NameTerminDrucker);
        return this;
    }

}