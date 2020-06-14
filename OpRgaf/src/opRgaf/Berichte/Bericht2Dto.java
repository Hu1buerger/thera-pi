package opRgaf.Berichte;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mandant.IK;
import sql.DatenquellenFactory;
    
public class Bericht2Dto {
    private static final Logger logger = LoggerFactory.getLogger(Bericht2Dto.class);
    
    private static final String dbName="bericht2";
    private IK ik;
    
    public Bericht2Dto(IK Ik) {
        ik = Ik;
    }

    private Bericht2 ofResultset(ResultSet rs) {
        Bericht2 ret = new Bericht2();
        
        ResultSetMetaData meta;
        try {
            meta = rs.getMetaData();
        } catch (SQLException e) {
            logger.error("Could not retrieve metaData", e);
            return null;
        }
        try {
            for(int o=1;o<=meta.getColumnCount();o++) {
                String field = meta.getColumnLabel(o).toUpperCase();
                // logger.debug("Checking: " + field + " in " + o);
                switch (field) {
                    case "PAT_INTERN":
                        ret.setPatIntern(rs.getString(field));
                        break;
                    case "BERICHTID":
                        ret.setBerichtId(rs.getInt(field));
                        break;
                    case "VNUMMER":
                        ret.setvNummer(rs.getString(field));
                        break;
                    case "NAMEVOR":
                        ret.setNameVor(rs.getString(field));
                        break;
                    case "GEBOREN":
                        ret.setGeboren(rs.getDate(field) == null ? null : rs.getDate(field).toLocalDate());
                        break;
                    case "STRASSE":
                        ret.setStrasse(rs.getString(field));
                        break;
                    case "PLZ":
                        ret.setPlz(rs.getString(field));
                        break;
                    case "ORT":
                        ret.setOrt(rs.getString(field));
                        break;
                    case "VNAMEVO":
                        ret.setvNameVo(rs.getString(field));
                        break;
                    case "MSNR":
                        ret.setMsnr(rs.getString(field));
                        break;
                    case "BNR":
                        ret.setBnr(rs.getString(field));
                        break;
                    case "AUFDAT1":
                        ret.setAufDat1(rs.getDate(field) == null ? null : rs.getDate(field).toLocalDate());
                        break;
                    case "ENTDAT1":
                        ret.setEntDat1(rs.getDate(field) == null ? null : rs.getDate(field).toLocalDate());
                        break;
                    case "AUFDAT2":
                        ret.setAufDat2(rs.getDate(field) == null ? null : rs.getDate(field).toLocalDate());
                        break;
                    case "ENTDAT2":
                        ret.setEntDat2(rs.getDate(field) == null ? null : rs.getDate(field).toLocalDate());
                        break;
                    case "AUFDAT3":
                        ret.setAufDat3(rs.getDate(field) == null ? null : rs.getDate(field).toLocalDate());
                        break;
                    case "ENTDAT3":
                        ret.setEntDat3(rs.getDate(field) == null ? null : rs.getDate(field).toLocalDate());
                        break;
                    case "ENTFORM":
                        ret.setEntform(rs.getString(field));
                        break;
                    case "ARBFAE":
                        ret.setArbfae(rs.getString(field));
                        break;
                    case "DIAG1":
                        ret.setDiag1(rs.getString(field));
                        break;
                    case "F_74":
                        ret.setF74(rs.getString(field));
                        break;
                    case "F_79":
                        ret.setF79(rs.getString(field));
                        break;
                    case "F_80":
                        ret.setF80(rs.getString(field));
                        break;
                    case "F_81":
                        ret.setF81(rs.getString(field));
                        break;
                    case "DIAG2":
                        ret.setDiag2(rs.getString(field));
                        break;
                    case "F_82":
                        ret.setF82(rs.getString(field));
                        break;
                    case "F_87":
                        ret.setF87(rs.getString(field));
                        break;
                    case "F_88":
                        ret.setF88(rs.getString(field));
                        break;
                    case "F_89":
                        ret.setF89(rs.getString(field));
                        break;
                    case "DIAG3":
                        ret.setDiag3(rs.getString(field));
                        break;
                    case "F_90":
                        ret.setF90(rs.getString(field));
                        break;
                    case "F_95":
                        ret.setF95(rs.getString(field));
                        break;
                    case "F_96":
                        ret.setF96(rs.getString(field));
                        break;
                    case "F_97":
                        ret.setF97(rs.getString(field));
                        break;
                    case "DIAG4":
                        ret.setDiag4(rs.getString(field));
                        break;
                    case "F_98":
                        ret.setF98(rs.getString(field));
                        break;
                    case "F_103":
                        ret.setF103(rs.getString(field));
                        break;
                    case "F_104":
                        ret.setF104(rs.getString(field));
                        break;
                    case "F_105":
                        ret.setF105(rs.getString(field));
                        break;
                    case "DIAG5":
                        ret.setDiag5(rs.getString(field));
                        break;
                    case "F_106":
                        ret.setF106(rs.getString(field));
                        break;
                    case "F_111":
                        ret.setF111(rs.getString(field));
                        break;
                    case "F_112":
                        ret.setF112(rs.getString(field));
                        break;
                    case "F_113":
                        ret.setF113(rs.getString(field));
                        break;
                    case "F_114":
                        ret.setF114(rs.getString(field));
                        break;
                    case "F_117":
                        ret.setF117(rs.getString(field));
                        break;
                    case "F_120":
                        ret.setF120(rs.getString(field));
                        break;
                    case "F_123":
                        ret.setF123(rs.getString(field));
                        break;
                    case "F_124":
                        ret.setF124(rs.getString(field));
                        break;
                    case "F_125":
                        ret.setF125(rs.getString(field));
                        break;
                    case "F_126":
                        ret.setF126(rs.getString(field));
                        break;
                    case "F_127":
                        ret.setF127(rs.getString(field));
                        break;
                    case "F_128":
                        ret.setF128(rs.getString(field));
                        break;
                    case "F_129":
                        ret.setF129(rs.getString(field));
                        break;
                    case "F_130":
                        ret.setF130(rs.getString(field));
                        break;
                    case "F_131":
                        ret.setF131(rs.getString(field));
                        break;
                    case "F_132":
                        ret.setF132(rs.getString(field));
                        break;
                    case "F_133":
                        ret.setF133(rs.getString(field));
                        break;
                    case "F_134":
                        ret.setF134(rs.getString(field));
                        break;
                    case "F_135":
                        ret.setF135(rs.getString(field));
                        break;
                    case "F_136":
                        ret.setF136(rs.getString(field));
                        break;
                    case "F_137":
                        ret.setF137(rs.getString(field));
                        break;
                    case "F_138":
                        ret.setF138(rs.getString(field));
                        break;
                    case "F_139":
                        ret.setF139(rs.getString(field));
                        break;
                    case "F_140":
                        ret.setF140(rs.getString(field));
                        break;
                    case "F_141":
                        ret.setF141(rs.getString(field));
                        break;
                    case "ERLAEUT":
                        ret.setErlaeut(rs.getString(field));
                        break;
                    case "LMEDIKAT":
                        ret.setLmedikat(rs.getString(field));
                        break;
                    case "TAET":
                        ret.setTaet(rs.getString(field));
                        break;
                    case "BKS":
                        ret.setBks(rs.getString(field));
                        break;
                    case "F_153":
                        ret.setF153(rs.getString(field));
                        break;
                    case "F_154":
                        ret.setF154(rs.getString(field));
                        break;
                    case "F_156":
                        ret.setF156(rs.getString(field));
                        break;
                    case "F_157":
                        ret.setF157(rs.getString(field));
                        break;
                    case "F_158":
                        ret.setF158(rs.getString(field));
                        break;
                    case "F_159":
                        ret.setF159(rs.getString(field));
                        break;
                    case "F_160":
                        ret.setF160(rs.getString(field));
                        break;
                    case "F_161":
                        ret.setF161(rs.getString(field));
                        break;
                    case "F_162":
                        ret.setF162(rs.getString(field));
                        break;
                    case "F_163":
                        ret.setF163(rs.getString(field));
                        break;
                    case "F_164":
                        ret.setF164(rs.getString(field));
                        break;
                    case "F_165":
                        ret.setF165(rs.getString(field));
                        break;
                    case "F_166":
                        ret.setF166(rs.getString(field));
                        break;
                    case "F_167":
                        ret.setF167(rs.getString(field));
                        break;
                    case "F_168":
                        ret.setF168(rs.getString(field));
                        break;
                    case "F_169":
                        ret.setF169(rs.getString(field));
                        break;
                    case "F_170":
                        ret.setF170(rs.getString(field));
                        break;
                    case "F_171":
                        ret.setF171(rs.getString(field));
                        break;
                    case "F_172":
                        ret.setF172(rs.getString(field));
                        break;
                    case "F_173":
                        ret.setF173(rs.getString(field));
                        break;
                    case "F_174":
                        ret.setF174(rs.getString(field));
                        break;
                    case "F_175":
                        ret.setF175(rs.getString(field));
                        break;
                    case "F_176":
                        ret.setF176(rs.getString(field));
                        break;
                    case "F_177":
                        ret.setF177(rs.getString(field));
                        break;
                    case "LEISTBI":
                        ret.setLeistbi(rs.getString(field));
                        break;
                    case "F_178":
                        ret.setF178(rs.getString(field));
                        break;
                    case "F_179":
                        ret.setF179(rs.getString(field));
                        break;
                    case "F_181":
                        ret.setF181(rs.getString(field));
                        break;
                    case "TERLEUT":
                        ret.setTerleut(rs.getString(field));
                        break;
                    case "FREITEXT":
                        ret.setFreitext(rs.getString(field));
                        break;
                    case "TMA1":
                        ret.setTma1(rs.getInt(field));
                        break;
                    case "TMA2":
                        ret.setTma2(rs.getInt(field));
                        break;
                    case "TMA3":
                        ret.setTma3(rs.getInt(field));
                        break;
                    case "TMA4":
                        ret.setTma4(rs.getInt(field));
                        break;
                    case "TMA5":
                        ret.setTma5(rs.getInt(field));
                        break;
                    case "TMA6":
                        ret.setTma6(rs.getInt(field));
                        break;
                    case "TMA7":
                        ret.setTma7(rs.getInt(field));
                        break;
                    case "TMA8":
                        ret.setTma8(rs.getInt(field));
                        break;
                    case "TMA9":
                        ret.setTma9(rs.getInt(field));
                        break;
                    case "TMA10":
                        ret.setTma10(rs.getInt(field));
                        break;
                    case "TMA11":
                        ret.setTma11(rs.getInt(field));
                        break;
                    case "TMA12":
                        ret.setTma12(rs.getInt(field));
                        break;
                    case "TMA13":
                        ret.setTma13(rs.getInt(field));
                        break;
                    case "TMA14":
                        ret.setTma14(rs.getInt(field));
                        break;
                    case "TMA15":
                        ret.setTma15(rs.getInt(field));
                        break;
                    case "TMA16":
                        ret.setTma16(rs.getInt(field));
                        break;
                    case "TMA17":
                        ret.setTma17(rs.getInt(field));
                        break;
                    case "TMA18":
                        ret.setTma18(rs.getInt(field));
                        break;
                    case "TMA19":
                        ret.setTma19(rs.getInt(field));
                        break;
                    case "TMA20":
                        ret.setTma20(rs.getInt(field));
                        break;
                    case "TMA21":
                        ret.setTma21(rs.getInt(field));
                        break;
                    case "TMA22":
                        ret.setTma22(rs.getInt(field));
                        break;
                    case "TMA23":
                        ret.setTma23(rs.getInt(field));
                        break;
                    case "TMA24":
                        ret.setTma24(rs.getInt(field));
                        break;
                    case "TMA25":
                        ret.setTma25(rs.getInt(field));
                        break;
                    case "TAZ1":
                        ret.setTaz1(rs.getString(field));
                        break;
                    case "TAZ2":
                        ret.setTaz2(rs.getString(field));
                        break;
                    case "TAZ3":
                        ret.setTaz3(rs.getString(field));
                        break;
                    case "TAZ4":
                        ret.setTaz4(rs.getString(field));
                        break;
                    case "TAZ5":
                        ret.setTaz5(rs.getString(field));
                        break;
                    case "TAZ6":
                        ret.setTaz6(rs.getString(field));
                        break;
                    case "TAZ7":
                        ret.setTaz7(rs.getString(field));
                        break;
                    case "TAZ8":
                        ret.setTaz8(rs.getString(field));
                        break;
                    case "TAZ9":
                        ret.setTaz9(rs.getString(field));
                        break;
                    case "TAZ10":
                        ret.setTaz10(rs.getString(field));
                        break;
                    case "TAZ11":
                        ret.setTaz11(rs.getString(field));
                        break;
                    case "TAZ12":
                        ret.setTaz12(rs.getString(field));
                        break;
                    case "TAZ13":
                        ret.setTaz13(rs.getString(field));
                        break;
                    case "TAZ14":
                        ret.setTaz14(rs.getString(field));
                        break;
                    case "TAZ15":
                        ret.setTaz15(rs.getString(field));
                        break;
                    case "TAZ16":
                        ret.setTaz16(rs.getString(field));
                        break;
                    case "TAZ17":
                        ret.setTaz17(rs.getString(field));
                        break;
                    case "TAZ18":
                        ret.setTaz18(rs.getString(field));
                        break;
                    case "TAZ19":
                        ret.setTaz19(rs.getString(field));
                        break;
                    case "TAZ20":
                        ret.setTaz20(rs.getString(field));
                        break;
                    case "TAZ21":
                        ret.setTaz21(rs.getString(field));
                        break;
                    case "TAZ22":
                        ret.setTaz22(rs.getString(field));
                        break;
                    case "TAZ23":
                        ret.setTaz23(rs.getString(field));
                        break;
                    case "TAZ24":
                        ret.setTaz24(rs.getString(field));
                        break;
                    case "TAZ25":
                        ret.setTaz25(rs.getString(field));
                        break;
                    case "LSEITE1":
                        ret.setLseite1(rs.getString(field) == null ? null : "T".equals(rs.getString(field)));
                        break;
                    case "LSEITE3":
                        ret.setLseite3(rs.getString(field) == null ? null : "T".equals(rs.getString(field)));
                        break;
                    case "LSEITE4":
                        ret.setLseite4(rs.getString(field) == null ? null : "T".equals(rs.getString(field)));
                        break;
                    case "AIGR":
                        ret.setAigr(rs.getString(field));
                        break;
                    case "ABTEILUNG":
                        ret.setAbteilung(rs.getString(field));
                        break;
                    case "DMP":
                        ret.setDmp(rs.getString(field));
                        break;
                    case "UNTDAT":
                        ret.setUntDat(rs.getDate(field) == null ? null : rs.getDate(field).toLocalDate());
                        break;
                    case "ID":
                        ret.setId(rs.getInt(field));
                        break;
                    case "ARZT1":
                        ret.setArzt1(rs.getString(field));
                        break;
                    case "ARZT2":
                        ret.setArzt2(rs.getString(field));
                        break;
                    case "ARZT3":
                        ret.setArzt3(rs.getString(field));
                        break;
                    case "DIAG6":
                        ret.setDiag6(rs.getString(field));
                        break;
                default:
                    logger.error("Unhandled field in " + dbName + " found: " + meta.getColumnLabel(o) + " at pos: " + o);
                };
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error("Couldn't retrieve dataset in "  + Bericht2Dto.class.getName());
            logger.error("Error: " + e.getLocalizedMessage());
        }
        
        return ret;
    }

    public void saveToDB(Bericht2 dataset) {
        String sql = "insert into " + dbName + " set "
                        + "PAT_INTERN=" + quoteNonNull(dataset.getPatIntern()) + ","
                        + "BERICHTID=" + dataset.getBerichtId() + ","
                        + "VNUMMER=" + quoteNonNull(dataset.getvNummer()) + ","
                        + "NAMEVOR=" + quoteNonNull(dataset.getNameVor()) + ","
                        + "GEBOREN=" + quoteNonNull(dataset.getGeboren()) + ","
                        + "STRASSE=" + quoteNonNull(dataset.getStrasse()) + ","
                        + "PLZ=" + quoteNonNull(dataset.getPlz()) + ","
                        + "ORT=" + quoteNonNull(dataset.getOrt()) + ","
                        + "VNAMEVO=" + quoteNonNull(dataset.getvNameVo()) + ","
                        + "MSNR=" + quoteNonNull(dataset.getMsnr()) + ","
                        + "BNR=" + quoteNonNull(dataset.getBnr()) + ","
                        + "AUFDAT1=" + quoteNonNull(dataset.getAufDat1()) + ","
                        + "ENTDAT1=" + quoteNonNull(dataset.getEntDat1()) + ","
                        + "AUFDAT2=" + quoteNonNull(dataset.getAufDat2()) + ","
                        + "ENTDAT2=" + quoteNonNull(dataset.getEntDat2()) + ","
                        + "AUFDAT3=" + quoteNonNull(dataset.getAufDat3()) + ","
                        + "ENTDAT3=" + quoteNonNull(dataset.getEntDat3()) + ","
                        + "ENTFORM=" + quoteNonNull(dataset.getEntform()) + ","
                        + "ARBFAE=" + quoteNonNull(dataset.getArbfae()) + ","
                        + "DIAG1=" + quoteNonNull(dataset.getDiag1()) + ","
                        + "F_74=" + quoteNonNull(dataset.getF74()) + ","
                        + "F_79=" + quoteNonNull(dataset.getF79()) + ","
                        + "F_80=" + quoteNonNull(dataset.getF80()) + ","
                        + "F_81=" + quoteNonNull(dataset.getF81()) + ","
                        + "DIAG2=" + quoteNonNull(dataset.getDiag2()) + ","
                        + "F_82=" + quoteNonNull(dataset.getF82()) + ","
                        + "F_87=" + quoteNonNull(dataset.getF87()) + ","
                        + "F_88=" + quoteNonNull(dataset.getF88()) + ","
                        + "F_89=" + quoteNonNull(dataset.getF89()) + ","
                        + "DIAG3=" + quoteNonNull(dataset.getDiag3()) + ","
                        + "F_90=" + quoteNonNull(dataset.getF90()) + ","
                        + "F_95=" + quoteNonNull(dataset.getF95()) + ","
                        + "F_96=" + quoteNonNull(dataset.getF96()) + ","
                        + "F_97=" + quoteNonNull(dataset.getF97()) + ","
                        + "DIAG4=" + quoteNonNull(dataset.getDiag4()) + ","
                        + "F_98=" + quoteNonNull(dataset.getF98()) + ","
                        + "F_103=" + quoteNonNull(dataset.getF103()) + ","
                        + "F_104=" + quoteNonNull(dataset.getF104()) + ","
                        + "F_105=" + quoteNonNull(dataset.getF105()) + ","
                        + "DIAG5=" + quoteNonNull(dataset.getDiag5()) + ","
                        + "F_106=" + quoteNonNull(dataset.getF106()) + ","
                        + "F_111=" + quoteNonNull(dataset.getF111()) + ","
                        + "F_112=" + quoteNonNull(dataset.getF112()) + ","
                        + "F_113=" + quoteNonNull(dataset.getF113()) + ","
                        + "F_114=" + quoteNonNull(dataset.getF114()) + ","
                        + "F_117=" + quoteNonNull(dataset.getF117()) + ","
                        + "F_120=" + quoteNonNull(dataset.getF120()) + ","
                        + "F_123=" + quoteNonNull(dataset.getF123()) + ","
                        + "F_124=" + quoteNonNull(dataset.getF124()) + ","
                        + "F_125=" + quoteNonNull(dataset.getF125()) + ","
                        + "F_126=" + quoteNonNull(dataset.getF126()) + ","
                        + "F_127=" + quoteNonNull(dataset.getF127()) + ","
                        + "F_128=" + quoteNonNull(dataset.getF128()) + ","
                        + "F_129=" + quoteNonNull(dataset.getF129()) + ","
                        + "F_130=" + quoteNonNull(dataset.getF130()) + ","
                        + "F_131=" + quoteNonNull(dataset.getF131()) + ","
                        + "F_132=" + quoteNonNull(dataset.getF132()) + ","
                        + "F_133=" + quoteNonNull(dataset.getF133()) + ","
                        + "F_134=" + quoteNonNull(dataset.getF134()) + ","
                        + "F_135=" + quoteNonNull(dataset.getF135()) + ","
                        + "F_136=" + quoteNonNull(dataset.getF136()) + ","
                        + "F_137=" + quoteNonNull(dataset.getF137()) + ","
                        + "F_138=" + quoteNonNull(dataset.getF138()) + ","
                        + "F_139=" + quoteNonNull(dataset.getF139()) + ","
                        + "F_140=" + quoteNonNull(dataset.getF140()) + ","
                        + "F_141=" + quoteNonNull(dataset.getF141()) + ","
                        + "ERLAEUT=" + quoteNonNull(dataset.getErlaeut()) + ","
                        + "LMEDIKAT=" + quoteNonNull(dataset.getLmedikat()) + ","
                        + "TAET=" + quoteNonNull(dataset.getTaet()) + ","
                        + "BKS=" + quoteNonNull(dataset.getBks()) + ","
                        + "F_153=" + quoteNonNull(dataset.getF153()) + ","
                        + "F_154=" + quoteNonNull(dataset.getF154()) + ","
                        + "F_156=" + quoteNonNull(dataset.getF156()) + ","
                        + "F_157=" + quoteNonNull(dataset.getF157()) + ","
                        + "F_158=" + quoteNonNull(dataset.getF158()) + ","
                        + "F_159=" + quoteNonNull(dataset.getF159()) + ","
                        + "F_160=" + quoteNonNull(dataset.getF160()) + ","
                        + "F_161=" + quoteNonNull(dataset.getF161()) + ","
                        + "F_162=" + quoteNonNull(dataset.getF162()) + ","
                        + "F_163=" + quoteNonNull(dataset.getF163()) + ","
                        + "F_164=" + quoteNonNull(dataset.getF164()) + ","
                        + "F_165=" + quoteNonNull(dataset.getF165()) + ","
                        + "F_166=" + quoteNonNull(dataset.getF166()) + ","
                        + "F_167=" + quoteNonNull(dataset.getF167()) + ","
                        + "F_168=" + quoteNonNull(dataset.getF168()) + ","
                        + "F_169=" + quoteNonNull(dataset.getF169()) + ","
                        + "F_170=" + quoteNonNull(dataset.getF170()) + ","
                        + "F_171=" + quoteNonNull(dataset.getF171()) + ","
                        + "F_172=" + quoteNonNull(dataset.getF172()) + ","
                        + "F_173=" + quoteNonNull(dataset.getF173()) + ","
                        + "F_174=" + quoteNonNull(dataset.getF174()) + ","
                        + "F_175=" + quoteNonNull(dataset.getF175()) + ","
                        + "F_176=" + quoteNonNull(dataset.getF176()) + ","
                        + "F_177=" + quoteNonNull(dataset.getF177()) + ","
                        + "LEISTBI=" + quoteNonNull(dataset.getLeistbi()) + ","
                        + "F_178=" + quoteNonNull(dataset.getF178()) + ","
                        + "F_179=" + quoteNonNull(dataset.getF179()) + ","
                        + "F_181=" + quoteNonNull(dataset.getF181()) + ","
                        + "TERLEUT=" + quoteNonNull(dataset.getTerleut()) + ","
                        + "FREITEXT=" + quoteNonNull(dataset.getFreitext()) + ","
                        + "TMA1=" + dataset.getTma1() + ","
                        + "TMA2=" + dataset.getTma2() + ","
                        + "TMA3=" + dataset.getTma3() + ","
                        + "TMA4=" + dataset.getTma4() + ","
                        + "TMA5=" + dataset.getTma5() + ","
                        + "TMA6=" + dataset.getTma6() + ","
                        + "TMA7=" + dataset.getTma7() + ","
                        + "TMA8=" + dataset.getTma8() + ","
                        + "TMA9=" + dataset.getTma9() + ","
                        + "TMA10=" + dataset.getTma10() + ","
                        + "TMA11=" + dataset.getTma11() + ","
                        + "TMA12=" + dataset.getTma12() + ","
                        + "TMA13=" + dataset.getTma13() + ","
                        + "TMA14=" + dataset.getTma14() + ","
                        + "TMA15=" + dataset.getTma15() + ","
                        + "TMA16=" + dataset.getTma16() + ","
                        + "TMA17=" + dataset.getTma17() + ","
                        + "TMA18=" + dataset.getTma18() + ","
                        + "TMA19=" + dataset.getTma19() + ","
                        + "TMA20=" + dataset.getTma20() + ","
                        + "TMA21=" + dataset.getTma21() + ","
                        + "TMA22=" + dataset.getTma22() + ","
                        + "TMA23=" + dataset.getTma23() + ","
                        + "TMA24=" + dataset.getTma24() + ","
                        + "TMA25=" + dataset.getTma25() + ","
                        + "TAZ1=" + quoteNonNull(dataset.getTaz1()) + ","
                        + "TAZ2=" + quoteNonNull(dataset.getTaz2()) + ","
                        + "TAZ3=" + quoteNonNull(dataset.getTaz3()) + ","
                        + "TAZ4=" + quoteNonNull(dataset.getTaz4()) + ","
                        + "TAZ5=" + quoteNonNull(dataset.getTaz5()) + ","
                        + "TAZ6=" + quoteNonNull(dataset.getTaz6()) + ","
                        + "TAZ7=" + quoteNonNull(dataset.getTaz7()) + ","
                        + "TAZ8=" + quoteNonNull(dataset.getTaz8()) + ","
                        + "TAZ9=" + quoteNonNull(dataset.getTaz9()) + ","
                        + "TAZ10=" + quoteNonNull(dataset.getTaz10()) + ","
                        + "TAZ11=" + quoteNonNull(dataset.getTaz11()) + ","
                        + "TAZ12=" + quoteNonNull(dataset.getTaz12()) + ","
                        + "TAZ13=" + quoteNonNull(dataset.getTaz13()) + ","
                        + "TAZ14=" + quoteNonNull(dataset.getTaz14()) + ","
                        + "TAZ15=" + quoteNonNull(dataset.getTaz15()) + ","
                        + "TAZ16=" + quoteNonNull(dataset.getTaz16()) + ","
                        + "TAZ17=" + quoteNonNull(dataset.getTaz17()) + ","
                        + "TAZ18=" + quoteNonNull(dataset.getTaz18()) + ","
                        + "TAZ19=" + quoteNonNull(dataset.getTaz19()) + ","
                        + "TAZ20=" + quoteNonNull(dataset.getTaz20()) + ","
                        + "TAZ21=" + quoteNonNull(dataset.getTaz21()) + ","
                        + "TAZ22=" + quoteNonNull(dataset.getTaz22()) + ","
                        + "TAZ23=" + quoteNonNull(dataset.getTaz23()) + ","
                        + "TAZ24=" + quoteNonNull(dataset.getTaz24()) + ","
                        + "TAZ25=" + quoteNonNull(dataset.getTaz25()) + ","
                        + "LSEITE1=" + quoteNonNull(dataset.getLseite1()) + ","
                        + "LSEITE3=" + quoteNonNull(dataset.getLseite3()) + ","
                        + "LSEITE4=" + quoteNonNull(dataset.getLseite4()) + ","
                        + "AIGR=" + quoteNonNull(dataset.getAigr()) + ","
                        + "ABTEILUNG=" + quoteNonNull(dataset.getAbteilung()) + ","
                        + "DMP=" + quoteNonNull(dataset.getDmp()) + ","
                        + "UNTDAT=" + quoteNonNull(dataset.getUntDat()) + ","
                        + "ARZT1=" + quoteNonNull(dataset.getArzt1()) + ","
                        + "ARZT2=" + quoteNonNull(dataset.getArzt2()) + ","
                        + "ARZT3=" + quoteNonNull(dataset.getArzt3()) + ","
                        + "DIAG6=" + quoteNonNull(dataset.getDiag6());
        try {
            Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement().execute(sql);
        } catch (SQLException e) {
            logger.error("Could not save dataset " + dataset.toString( ) + " to Database, table " + dbName + ".", e);
        }
    }
    
    private String quoteNonNull(Object val) {
        return (val == null ? "NULL" : "'" + val + "'");
    }

    
}
