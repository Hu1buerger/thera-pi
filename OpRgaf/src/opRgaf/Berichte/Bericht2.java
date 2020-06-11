package opRgaf.Berichte;

import java.time.LocalDate;
import java.util.Objects;

public class Bericht2 {

    String patIntern;
    int berichtId;
    String vNummer;
    String nameVor;
    LocalDate geboren;
    String strasse;
    String plz;
    String ort;
    String vNameVO;
    String msNr;
    String bNr;
    LocalDate aufDat1;
    LocalDate aufDat2;
    LocalDate aufDat3;
    LocalDate entDat1;
    LocalDate entDat2;
    LocalDate entDat3;
    String entform;
    String arbfae;
    String diag1;
    String diag2;
    String diag3;
    String diag4;
    String diag5;
    String diag6;
    String erlaeut;
    String lmedikat;
    String taet;
    String bks;
    String leistbi;
    String terleut;
    String freitext;
    boolean lSeite1;
    boolean lSeite3;
    boolean lSeite4;
    String aigr;
    String abteilung;
    String dmp;
    LocalDate untdat;
    int id;
    String arzt1;
    String arzt2;
    String arzt3;
    String f74;
    String f79;
    String f80;
    String f81;
    String f82;
    String f87;
    String f88;
    String f89;
    String f90;
    String f95;
    String f96;
    String f97;
    String f98;
    String f103;
    String f104;
    String f105;
    String f106;
    String f111;
    String f112;
    String f113;
    String f114;
    String f117;
    String f120;
    String f123;
    String f124;
    String f125;
    String f126;
    String f127;
    String f128;
    String f129;
    String f130;
    String f131;
    String f132;
    String f133;
    String f134;
    String f135;
    String f136;
    String f137;
    String f138;
    String f139;
    String f140;
    String f141;
    String f153;
    String f154;
    String f156;
    String f157;
    String f158;
    String f159;
    String f160;
    String f161;
    String f162;
    String f163;
    String f164;
    String f165;
    String f166;
    String f167;
    String f168;
    String f169;
    String f170;
    String f171;
    String f172;
    String f173;
    String f174;
    String f175;
    String f176;
    String f177;
    String f178;
    String f179;
    String f181;
    int tma1;
    int tma2;
    int tma3;
    int tma4;
    int tma5;
    int tma6;
    int tma7;
    int tma8;
    int tma9;
    int tma10;
    int tma11;
    int tma12;
    int tma13;
    int tma14;
    int tma15;
    int tma16;
    int tma17;
    int tma18;
    int tma19;
    int tma20;
    int tma21;
    int tma22;
    int tma23;
    int tma24;
    int tma25;
    String taz1;
    String taz2;
    String taz3;
    String taz4;
    String taz5;
    String taz6;
    String taz7;
    String taz8;
    String taz9;
    String taz10;
    String taz11;
    String taz12;
    String taz13;
    String taz14;
    String taz15;
    String taz16;
    String taz17;
    String taz18;
    String taz19;
    String taz20;
    String taz21;
    String taz22;
    String taz23;
    String taz24;
    String taz25;    
    
    // Standard hashcode & equals omitting the "id" field
    @Override
    public int hashCode() {
        return Objects.hash(abteilung, aigr, arbfae, arzt1, arzt2, arzt3, aufDat1, aufDat2, aufDat3, bNr, berichtId,
                bks, diag1, diag2, diag3, diag4, diag5, diag6, dmp, entDat1, entDat2, entDat3, entform, erlaeut, f103,
                f104, f105, f106, f111, f112, f113, f114, f117, f120, f123, f124, f125, f126, f127, f128, f129, f130,
                f131, f132, f133, f134, f135, f136, f137, f138, f139, f140, f141, f153, f154, f156, f157, f158, f159,
                f160, f161, f162, f163, f164, f165, f166, f167, f168, f169, f170, f171, f172, f173, f174, f175, f176,
                f177, f178, f179, f181, f74, f79, f80, f81, f82, f87, f88, f89, f90, f95, f96, f97, f98, freitext,
                geboren, lSeite1, lSeite3, lSeite4, leistbi, lmedikat, msNr, nameVor, ort, patIntern, plz, strasse,
                taet, taz1, taz10, taz11, taz12, taz13, taz14, taz15, taz16, taz17, taz18, taz19, taz2, taz20, taz21,
                taz22, taz23, taz24, taz25, taz3, taz4, taz5, taz6, taz7, taz8, taz9, terleut, tma1, tma10, tma11,
                tma12, tma13, tma14, tma15, tma16, tma17, tma18, tma19, tma2, tma20, tma21, tma22, tma23, tma24, tma25,
                tma3, tma4, tma5, tma6, tma7, tma8, tma9, untdat, vNameVO, vNummer);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Bericht2 other = (Bericht2) obj;
        return Objects.equals(abteilung, other.abteilung) && Objects.equals(aigr, other.aigr)
                && Objects.equals(arbfae, other.arbfae) && Objects.equals(arzt1, other.arzt1)
                && Objects.equals(arzt2, other.arzt2) && Objects.equals(arzt3, other.arzt3)
                && Objects.equals(aufDat1, other.aufDat1) && Objects.equals(aufDat2, other.aufDat2)
                && Objects.equals(aufDat3, other.aufDat3) && Objects.equals(bNr, other.bNr)
                && berichtId == other.berichtId && Objects.equals(bks, other.bks) && Objects.equals(diag1, other.diag1)
                && Objects.equals(diag2, other.diag2) && Objects.equals(diag3, other.diag3)
                && Objects.equals(diag4, other.diag4) && Objects.equals(diag5, other.diag5)
                && Objects.equals(diag6, other.diag6) && Objects.equals(dmp, other.dmp)
                && Objects.equals(entDat1, other.entDat1) && Objects.equals(entDat2, other.entDat2)
                && Objects.equals(entDat3, other.entDat3) && Objects.equals(entform, other.entform)
                && Objects.equals(erlaeut, other.erlaeut) && Objects.equals(f103, other.f103)
                && Objects.equals(f104, other.f104) && Objects.equals(f105, other.f105)
                && Objects.equals(f106, other.f106) && Objects.equals(f111, other.f111)
                && Objects.equals(f112, other.f112) && Objects.equals(f113, other.f113)
                && Objects.equals(f114, other.f114) && Objects.equals(f117, other.f117)
                && Objects.equals(f120, other.f120) && Objects.equals(f123, other.f123)
                && Objects.equals(f124, other.f124) && Objects.equals(f125, other.f125)
                && Objects.equals(f126, other.f126) && Objects.equals(f127, other.f127)
                && Objects.equals(f128, other.f128) && Objects.equals(f129, other.f129)
                && Objects.equals(f130, other.f130) && Objects.equals(f131, other.f131)
                && Objects.equals(f132, other.f132) && Objects.equals(f133, other.f133)
                && Objects.equals(f134, other.f134) && Objects.equals(f135, other.f135)
                && Objects.equals(f136, other.f136) && Objects.equals(f137, other.f137)
                && Objects.equals(f138, other.f138) && Objects.equals(f139, other.f139)
                && Objects.equals(f140, other.f140) && Objects.equals(f141, other.f141)
                && Objects.equals(f153, other.f153) && Objects.equals(f154, other.f154)
                && Objects.equals(f156, other.f156) && Objects.equals(f157, other.f157)
                && Objects.equals(f158, other.f158) && Objects.equals(f159, other.f159)
                && Objects.equals(f160, other.f160) && Objects.equals(f161, other.f161)
                && Objects.equals(f162, other.f162) && Objects.equals(f163, other.f163)
                && Objects.equals(f164, other.f164) && Objects.equals(f165, other.f165)
                && Objects.equals(f166, other.f166) && Objects.equals(f167, other.f167)
                && Objects.equals(f168, other.f168) && Objects.equals(f169, other.f169)
                && Objects.equals(f170, other.f170) && Objects.equals(f171, other.f171)
                && Objects.equals(f172, other.f172) && Objects.equals(f173, other.f173)
                && Objects.equals(f174, other.f174) && Objects.equals(f175, other.f175)
                && Objects.equals(f176, other.f176) && Objects.equals(f177, other.f177)
                && Objects.equals(f178, other.f178) && Objects.equals(f179, other.f179)
                && Objects.equals(f181, other.f181) && Objects.equals(f74, other.f74) && Objects.equals(f79, other.f79)
                && Objects.equals(f80, other.f80) && Objects.equals(f81, other.f81) && Objects.equals(f82, other.f82)
                && Objects.equals(f87, other.f87) && Objects.equals(f88, other.f88) && Objects.equals(f89, other.f89)
                && Objects.equals(f90, other.f90) && Objects.equals(f95, other.f95) && Objects.equals(f96, other.f96)
                && Objects.equals(f97, other.f97) && Objects.equals(f98, other.f98)
                && Objects.equals(freitext, other.freitext) && Objects.equals(geboren, other.geboren)
                && lSeite1 == other.lSeite1 && lSeite3 == other.lSeite3 && lSeite4 == other.lSeite4
                && Objects.equals(leistbi, other.leistbi) && Objects.equals(lmedikat, other.lmedikat)
                && Objects.equals(msNr, other.msNr) && Objects.equals(nameVor, other.nameVor)
                && Objects.equals(ort, other.ort) && Objects.equals(patIntern, other.patIntern)
                && Objects.equals(plz, other.plz) && Objects.equals(strasse, other.strasse)
                && Objects.equals(taet, other.taet) && Objects.equals(taz1, other.taz1)
                && Objects.equals(taz10, other.taz10) && Objects.equals(taz11, other.taz11)
                && Objects.equals(taz12, other.taz12) && Objects.equals(taz13, other.taz13)
                && Objects.equals(taz14, other.taz14) && Objects.equals(taz15, other.taz15)
                && Objects.equals(taz16, other.taz16) && Objects.equals(taz17, other.taz17)
                && Objects.equals(taz18, other.taz18) && Objects.equals(taz19, other.taz19)
                && Objects.equals(taz2, other.taz2) && Objects.equals(taz20, other.taz20)
                && Objects.equals(taz21, other.taz21) && Objects.equals(taz22, other.taz22)
                && Objects.equals(taz23, other.taz23) && Objects.equals(taz24, other.taz24)
                && Objects.equals(taz25, other.taz25) && Objects.equals(taz3, other.taz3)
                && Objects.equals(taz4, other.taz4) && Objects.equals(taz5, other.taz5)
                && Objects.equals(taz6, other.taz6) && Objects.equals(taz7, other.taz7)
                && Objects.equals(taz8, other.taz8) && Objects.equals(taz9, other.taz9)
                && Objects.equals(terleut, other.terleut) && tma1 == other.tma1 && tma10 == other.tma10
                && tma11 == other.tma11 && tma12 == other.tma12 && tma13 == other.tma13 && tma14 == other.tma14
                && tma15 == other.tma15 && tma16 == other.tma16 && tma17 == other.tma17 && tma18 == other.tma18
                && tma19 == other.tma19 && tma2 == other.tma2 && tma20 == other.tma20 && tma21 == other.tma21
                && tma22 == other.tma22 && tma23 == other.tma23 && tma24 == other.tma24 && tma25 == other.tma25
                && tma3 == other.tma3 && tma4 == other.tma4 && tma5 == other.tma5 && tma6 == other.tma6
                && tma7 == other.tma7 && tma8 == other.tma8 && tma9 == other.tma9
                && Objects.equals(untdat, other.untdat) && Objects.equals(vNameVO, other.vNameVO)
                && Objects.equals(vNummer, other.vNummer);
    }
    // Public standard getter/setters:
    public String getPatIntern() {
        return patIntern;
    }
    public void setPatIntern(String patIntern) {
        this.patIntern = patIntern;
    }
    public int getBerichtId() {
        return berichtId;
    }
    public void setBerichtId(int berichtId) {
        this.berichtId = berichtId;
    }
    public String getvNummer() {
        return vNummer;
    }
    public void setvNummer(String vNummer) {
        this.vNummer = vNummer;
    }
    public String getNameVor() {
        return nameVor;
    }
    public void setNameVor(String nameVor) {
        this.nameVor = nameVor;
    }
    public LocalDate getGeboren() {
        return geboren;
    }
    public void setGeboren(LocalDate geboren) {
        this.geboren = geboren;
    }
    public String getStrasse() {
        return strasse;
    }
    public void setStrasse(String strasse) {
        this.strasse = strasse;
    }
    public String getPlz() {
        return plz;
    }
    public void setPlz(String plz) {
        this.plz = plz;
    }
    public String getOrt() {
        return ort;
    }
    public void setOrt(String ort) {
        this.ort = ort;
    }
    public String getvNameVO() {
        return vNameVO;
    }
    public void setvNameVO(String vNameVO) {
        this.vNameVO = vNameVO;
    }
    public String getMsNr() {
        return msNr;
    }
    public void setMsNr(String msNr) {
        this.msNr = msNr;
    }
    public String getbNr() {
        return bNr;
    }
    public void setbNr(String bNr) {
        this.bNr = bNr;
    }
    public LocalDate getAufDat1() {
        return aufDat1;
    }
    public void setAufDat1(LocalDate aufDat1) {
        this.aufDat1 = aufDat1;
    }
    public LocalDate getEntDat1() {
        return entDat1;
    }
    public void setEntDat1(LocalDate entDat1) {
        this.entDat1 = entDat1;
    }
    public LocalDate getAufDat2() {
        return aufDat2;
    }
    public void setAufDat2(LocalDate aufDat2) {
        this.aufDat2 = aufDat2;
    }
    public LocalDate getEntDat2() {
        return entDat2;
    }
    public void setEntDat2(LocalDate entDat2) {
        this.entDat2 = entDat2;
    }
    public LocalDate getAufDat3() {
        return aufDat3;
    }
    public void setAufDat3(LocalDate aufDat3) {
        this.aufDat3 = aufDat3;
    }
    public LocalDate getEntDat3() {
        return entDat3;
    }
    public void setEntDat3(LocalDate entDat3) {
        this.entDat3 = entDat3;
    }
    public String getEntform() {
        return entform;
    }
    public void setEntform(String entform) {
        this.entform = entform;
    }
    public String getArbfae() {
        return arbfae;
    }
    public void setArbfae(String arbfae) {
        this.arbfae = arbfae;
    }
    public String getDiag1() {
        return diag1;
    }
    public void setDiag1(String diag1) {
        this.diag1 = diag1;
    }
    public String getF74() {
        return f74;
    }
    public void setF74(String f74) {
        this.f74 = f74;
    }
    public String getF79() {
        return f79;
    }
    public void setF79(String f79) {
        this.f79 = f79;
    }
    public String getF80() {
        return f80;
    }
    public void setF80(String f80) {
        this.f80 = f80;
    }
    public String getF81() {
        return f81;
    }
    public void setF81(String f81) {
        this.f81 = f81;
    }
    public String getDiag2() {
        return diag2;
    }
    public void setDiag2(String diag2) {
        this.diag2 = diag2;
    }
    public String getF82() {
        return f82;
    }
    public void setF82(String f82) {
        this.f82 = f82;
    }
    public String getF87() {
        return f87;
    }
    public void setF87(String f87) {
        this.f87 = f87;
    }
    public String getF88() {
        return f88;
    }
    public void setF88(String f88) {
        this.f88 = f88;
    }
    public String getF89() {
        return f89;
    }
    public void setF89(String f89) {
        this.f89 = f89;
    }
    public String getDiag3() {
        return diag3;
    }
    public void setDiag3(String diag3) {
        this.diag3 = diag3;
    }
    public String getF90() {
        return f90;
    }
    public void setF90(String f90) {
        this.f90 = f90;
    }
    public String getF95() {
        return f95;
    }
    public void setF95(String f95) {
        this.f95 = f95;
    }
    public String getF96() {
        return f96;
    }
    public void setF96(String f96) {
        this.f96 = f96;
    }
    public String getF97() {
        return f97;
    }
    public void setF97(String f97) {
        this.f97 = f97;
    }
    public String getDiag4() {
        return diag4;
    }
    public void setDiag4(String diag4) {
        this.diag4 = diag4;
    }
    public String getF98() {
        return f98;
    }
    public void setF98(String f98) {
        this.f98 = f98;
    }
    public String getF103() {
        return f103;
    }
    public void setF103(String f103) {
        this.f103 = f103;
    }
    public String getF104() {
        return f104;
    }
    public void setF104(String f104) {
        this.f104 = f104;
    }
    public String getF105() {
        return f105;
    }
    public void setF105(String f105) {
        this.f105 = f105;
    }
    public String getDiag5() {
        return diag5;
    }
    public void setDiag5(String diag5) {
        this.diag5 = diag5;
    }
    public String getF106() {
        return f106;
    }
    public void setF106(String f106) {
        this.f106 = f106;
    }
    public String getF111() {
        return f111;
    }
    public void setF111(String f111) {
        this.f111 = f111;
    }
    public String getF112() {
        return f112;
    }
    public void setF112(String f112) {
        this.f112 = f112;
    }
    public String getF113() {
        return f113;
    }
    public void setF113(String f113) {
        this.f113 = f113;
    }
    public String getF114() {
        return f114;
    }
    public void setF114(String f114) {
        this.f114 = f114;
    }
    public String getF117() {
        return f117;
    }
    public void setF117(String f117) {
        this.f117 = f117;
    }
    public String getF120() {
        return f120;
    }
    public void setF120(String f120) {
        this.f120 = f120;
    }
    public String getF123() {
        return f123;
    }
    public void setF123(String f123) {
        this.f123 = f123;
    }
    public String getF124() {
        return f124;
    }
    public void setF124(String f124) {
        this.f124 = f124;
    }
    public String getF125() {
        return f125;
    }
    public void setF125(String f125) {
        this.f125 = f125;
    }
    public String getF126() {
        return f126;
    }
    public void setF126(String f126) {
        this.f126 = f126;
    }
    public String getF127() {
        return f127;
    }
    public void setF127(String f127) {
        this.f127 = f127;
    }
    public String getF128() {
        return f128;
    }
    public void setF128(String f128) {
        this.f128 = f128;
    }
    public String getF129() {
        return f129;
    }
    public void setF129(String f129) {
        this.f129 = f129;
    }
    public String getF130() {
        return f130;
    }
    public void setF130(String f130) {
        this.f130 = f130;
    }
    public String getF131() {
        return f131;
    }
    public void setF131(String f131) {
        this.f131 = f131;
    }
    public String getF132() {
        return f132;
    }
    public void setF132(String f132) {
        this.f132 = f132;
    }
    public String getF133() {
        return f133;
    }
    public void setF133(String f133) {
        this.f133 = f133;
    }
    public String getF134() {
        return f134;
    }
    public void setF134(String f134) {
        this.f134 = f134;
    }
    public String getF135() {
        return f135;
    }
    public void setF135(String f135) {
        this.f135 = f135;
    }
    public String getF136() {
        return f136;
    }
    public void setF136(String f136) {
        this.f136 = f136;
    }
    public String getF137() {
        return f137;
    }
    public void setF137(String f137) {
        this.f137 = f137;
    }
    public String getF138() {
        return f138;
    }
    public void setF138(String f138) {
        this.f138 = f138;
    }
    public String getF139() {
        return f139;
    }
    public void setF139(String f139) {
        this.f139 = f139;
    }
    public String getF140() {
        return f140;
    }
    public void setF140(String f140) {
        this.f140 = f140;
    }
    public String getF141() {
        return f141;
    }
    public void setF141(String f141) {
        this.f141 = f141;
    }
    public String getErlaeut() {
        return erlaeut;
    }
    public void setErlaeut(String erlaeut) {
        this.erlaeut = erlaeut;
    }
    public String getLmedikat() {
        return lmedikat;
    }
    public void setLmedikat(String lmedikat) {
        this.lmedikat = lmedikat;
    }
    public String getTaet() {
        return taet;
    }
    public void setTaet(String taet) {
        this.taet = taet;
    }
    public String getBks() {
        return bks;
    }
    public void setBks(String bks) {
        this.bks = bks;
    }
    public String getF153() {
        return f153;
    }
    public void setF153(String f153) {
        this.f153 = f153;
    }
    public String getF154() {
        return f154;
    }
    public void setF154(String f154) {
        this.f154 = f154;
    }
    public String getF156() {
        return f156;
    }
    public void setF156(String f156) {
        this.f156 = f156;
    }
    public String getF157() {
        return f157;
    }
    public void setF157(String f157) {
        this.f157 = f157;
    }
    public String getF158() {
        return f158;
    }
    public void setF158(String f158) {
        this.f158 = f158;
    }
    public String getF159() {
        return f159;
    }
    public void setF159(String f159) {
        this.f159 = f159;
    }
    public String getF160() {
        return f160;
    }
    public void setF160(String f160) {
        this.f160 = f160;
    }
    public String getF161() {
        return f161;
    }
    public void setF161(String f161) {
        this.f161 = f161;
    }
    public String getF162() {
        return f162;
    }
    public void setF162(String f162) {
        this.f162 = f162;
    }
    public String getF163() {
        return f163;
    }
    public void setF163(String f163) {
        this.f163 = f163;
    }
    public String getF164() {
        return f164;
    }
    public void setF164(String f164) {
        this.f164 = f164;
    }
    public String getF165() {
        return f165;
    }
    public void setF165(String f165) {
        this.f165 = f165;
    }
    public String getF166() {
        return f166;
    }
    public void setF166(String f166) {
        this.f166 = f166;
    }
    public String getF167() {
        return f167;
    }
    public void setF167(String f167) {
        this.f167 = f167;
    }
    public String getF168() {
        return f168;
    }
    public void setF168(String f168) {
        this.f168 = f168;
    }
    public String getF169() {
        return f169;
    }
    public void setF169(String f169) {
        this.f169 = f169;
    }
    public String getF170() {
        return f170;
    }
    public void setF170(String f170) {
        this.f170 = f170;
    }
    public String getF171() {
        return f171;
    }
    public void setF171(String f171) {
        this.f171 = f171;
    }
    public String getF172() {
        return f172;
    }
    public void setF172(String f172) {
        this.f172 = f172;
    }
    public String getF173() {
        return f173;
    }
    public void setF173(String f173) {
        this.f173 = f173;
    }
    public String getF174() {
        return f174;
    }
    public void setF174(String f174) {
        this.f174 = f174;
    }
    public String getF175() {
        return f175;
    }
    public void setF175(String f175) {
        this.f175 = f175;
    }
    public String getF176() {
        return f176;
    }
    public void setF176(String f176) {
        this.f176 = f176;
    }
    public String getF177() {
        return f177;
    }
    public void setF177(String f177) {
        this.f177 = f177;
    }
    public String getLeistbi() {
        return leistbi;
    }
    public void setLeistbi(String leistbi) {
        this.leistbi = leistbi;
    }
    public String getF178() {
        return f178;
    }
    public void setF178(String f178) {
        this.f178 = f178;
    }
    public String getF179() {
        return f179;
    }
    public void setF179(String f179) {
        this.f179 = f179;
    }
    public String getF181() {
        return f181;
    }
    public void setF181(String f181) {
        this.f181 = f181;
    }
    public String getTerleut() {
        return terleut;
    }
    public void setTerleut(String terleut) {
        this.terleut = terleut;
    }
    public String getFreitext() {
        return freitext;
    }
    public void setFreitext(String freitext) {
        this.freitext = freitext;
    }
    public int getTma1() {
        return tma1;
    }
    public void setTma1(int tma1) {
        this.tma1 = tma1;
    }
    public int getTma2() {
        return tma2;
    }
    public void setTma2(int tma2) {
        this.tma2 = tma2;
    }
    public int getTma3() {
        return tma3;
    }
    public void setTma3(int tma3) {
        this.tma3 = tma3;
    }
    public int getTma4() {
        return tma4;
    }
    public void setTma4(int tma4) {
        this.tma4 = tma4;
    }
    public int getTma5() {
        return tma5;
    }
    public void setTma5(int tma5) {
        this.tma5 = tma5;
    }
    public int getTma6() {
        return tma6;
    }
    public void setTma6(int tma6) {
        this.tma6 = tma6;
    }
    public int getTma7() {
        return tma7;
    }
    public void setTma7(int tma7) {
        this.tma7 = tma7;
    }
    public int getTma8() {
        return tma8;
    }
    public void setTma8(int tma8) {
        this.tma8 = tma8;
    }
    public int getTma9() {
        return tma9;
    }
    public void setTma9(int tma9) {
        this.tma9 = tma9;
    }
    public int getTma10() {
        return tma10;
    }
    public void setTma10(int tma10) {
        this.tma10 = tma10;
    }
    public int getTma11() {
        return tma11;
    }
    public void setTma11(int tma11) {
        this.tma11 = tma11;
    }
    public int getTma12() {
        return tma12;
    }
    public void setTma12(int tma12) {
        this.tma12 = tma12;
    }
    public int getTma13() {
        return tma13;
    }
    public void setTma13(int tma13) {
        this.tma13 = tma13;
    }
    public int getTma14() {
        return tma14;
    }
    public void setTma14(int tma14) {
        this.tma14 = tma14;
    }
    public int getTma15() {
        return tma15;
    }
    public void setTma15(int tma15) {
        this.tma15 = tma15;
    }
    public int getTma16() {
        return tma16;
    }
    public void setTma16(int tma16) {
        this.tma16 = tma16;
    }
    public int getTma17() {
        return tma17;
    }
    public void setTma17(int tma17) {
        this.tma17 = tma17;
    }
    public int getTma18() {
        return tma18;
    }
    public void setTma18(int tma18) {
        this.tma18 = tma18;
    }
    public int getTma19() {
        return tma19;
    }
    public void setTma19(int tma19) {
        this.tma19 = tma19;
    }
    public int getTma20() {
        return tma20;
    }
    public void setTma20(int tma20) {
        this.tma20 = tma20;
    }
    public int getTma21() {
        return tma21;
    }
    public void setTma21(int tma21) {
        this.tma21 = tma21;
    }
    public int getTma22() {
        return tma22;
    }
    public void setTma22(int tma22) {
        this.tma22 = tma22;
    }
    public int getTma23() {
        return tma23;
    }
    public void setTma23(int tma23) {
        this.tma23 = tma23;
    }
    public int getTma24() {
        return tma24;
    }
    public void setTma24(int tma24) {
        this.tma24 = tma24;
    }
    public int getTma25() {
        return tma25;
    }
    public void setTma25(int tma25) {
        this.tma25 = tma25;
    }
    public String getTaz1() {
        return taz1;
    }
    public void setTaz1(String taz1) {
        this.taz1 = taz1;
    }
    public String getTaz2() {
        return taz2;
    }
    public void setTaz2(String taz2) {
        this.taz2 = taz2;
    }
    public String getTaz3() {
        return taz3;
    }
    public void setTaz3(String taz3) {
        this.taz3 = taz3;
    }
    public String getTaz4() {
        return taz4;
    }
    public void setTaz4(String taz4) {
        this.taz4 = taz4;
    }
    public String getTaz5() {
        return taz5;
    }
    public void setTaz5(String taz5) {
        this.taz5 = taz5;
    }
    public String getTaz6() {
        return taz6;
    }
    public void setTaz6(String taz6) {
        this.taz6 = taz6;
    }
    public String getTaz7() {
        return taz7;
    }
    public void setTaz7(String taz7) {
        this.taz7 = taz7;
    }
    public String getTaz8() {
        return taz8;
    }
    public void setTaz8(String taz8) {
        this.taz8 = taz8;
    }
    public String getTaz9() {
        return taz9;
    }
    public void setTaz9(String taz9) {
        this.taz9 = taz9;
    }
    public String getTaz10() {
        return taz10;
    }
    public void setTaz10(String taz10) {
        this.taz10 = taz10;
    }
    public String getTaz11() {
        return taz11;
    }
    public void setTaz11(String taz11) {
        this.taz11 = taz11;
    }
    public String getTaz12() {
        return taz12;
    }
    public void setTaz12(String taz12) {
        this.taz12 = taz12;
    }
    public String getTaz13() {
        return taz13;
    }
    public void setTaz13(String taz13) {
        this.taz13 = taz13;
    }
    public String getTaz14() {
        return taz14;
    }
    public void setTaz14(String taz14) {
        this.taz14 = taz14;
    }
    public String getTaz15() {
        return taz15;
    }
    public void setTaz15(String taz15) {
        this.taz15 = taz15;
    }
    public String getTaz16() {
        return taz16;
    }
    public void setTaz16(String taz16) {
        this.taz16 = taz16;
    }
    public String getTaz17() {
        return taz17;
    }
    public void setTaz17(String taz17) {
        this.taz17 = taz17;
    }
    public String getTaz18() {
        return taz18;
    }
    public void setTaz18(String taz18) {
        this.taz18 = taz18;
    }
    public String getTaz19() {
        return taz19;
    }
    public void setTaz19(String taz19) {
        this.taz19 = taz19;
    }
    public String getTaz20() {
        return taz20;
    }
    public void setTaz20(String taz20) {
        this.taz20 = taz20;
    }
    public String getTaz21() {
        return taz21;
    }
    public void setTaz21(String taz21) {
        this.taz21 = taz21;
    }
    public String getTaz22() {
        return taz22;
    }
    public void setTaz22(String taz22) {
        this.taz22 = taz22;
    }
    public String getTaz23() {
        return taz23;
    }
    public void setTaz23(String taz23) {
        this.taz23 = taz23;
    }
    public String getTaz24() {
        return taz24;
    }
    public void setTaz24(String taz24) {
        this.taz24 = taz24;
    }
    public String getTaz25() {
        return taz25;
    }
    public void setTaz25(String taz25) {
        this.taz25 = taz25;
    }
    public boolean islSeite1() {
        return lSeite1;
    }
    public String getlSeite1() {
        return islSeite1() ? "T" : "F";
    }
    public void setlSeite1(boolean lSeite1) {
        this.lSeite1 = lSeite1;
    }
    public boolean islSeite3() {
        return lSeite3;
    }
    public String getlSeite3() {
        return islSeite3() ? "T" : "F";
    }

    public void setlSeite3(boolean lSeite3) {
        this.lSeite3 = lSeite3;
    }
    public boolean islSeite4() {
        return lSeite4;
    }
    public String getlSeite4() {
        return islSeite4() ? "T" : "F";
    }
    public void setlSeite4(boolean lSeite4) {
        this.lSeite4 = lSeite4;
    }
    public String getAigr() {
        return aigr;
    }
    public void setAigr(String aigr) {
        this.aigr = aigr;
    }
    public String getAbteilung() {
        return abteilung;
    }
    public void setAbteilung(String abteilung) {
        this.abteilung = abteilung;
    }
    public String getDmp() {
        return dmp;
    }
    public void setDmp(String dmp) {
        this.dmp = dmp;
    }
    public LocalDate getUntdat() {
        return untdat;
    }
    public void setUntdat(LocalDate untdat) {
        this.untdat = untdat;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getArzt1() {
        return arzt1;
    }
    public void setArzt1(String arzt1) {
        this.arzt1 = arzt1;
    }
    public String getArzt2() {
        return arzt2;
    }
    public void setArzt2(String arzt2) {
        this.arzt2 = arzt2;
    }
    public String getArzt3() {
        return arzt3;
    }
    public void setArzt3(String arzt3) {
        this.arzt3 = arzt3;
    }
    public String getDiag6() {
        return diag6;
    }
    public void setDiag6(String diag6) {
        this.diag6 = diag6;
    }    
    
}
