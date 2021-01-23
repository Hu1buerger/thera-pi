package commonData;

import java.util.ArrayList;
import java.util.Arrays;

public class VerordnungsArten {
    public static final String ERST_VO = "Erstverordnung";
    public static final String FOLGE_VO = "Folgeverordnung";
    public static final String FOLGE_VO_A_D_R = "Folgev. außerhalb d.R.";
    public static final String STANDARD_VO = "Standard-VO";
    public static final String BES_VO_BEDARF = "Bes.VO Bedarf";
    public static final String LANGFRIST_VO = "Langfrist-VO";
    public static final String BLANKO_VO = "Blanko-VO";
    public static final String ENTLASS_MNGMNT = "Entlassmanagement";

    private ArrayList<String> listTypesOfVO = null;
    private static String[] rezArten = null;
    private static int startVo2020 = 0;

    public VerordnungsArten () {
        listTypesOfVO = new ArrayList<String>();
        listTypesOfVO.addAll (Arrays.asList( ERST_VO, FOLGE_VO, FOLGE_VO_A_D_R, STANDARD_VO, BES_VO_BEDARF, LANGFRIST_VO, BLANKO_VO, ENTLASS_MNGMNT ));
        rezArten = new String[listTypesOfVO.size()]; 
        listTypesOfVO.toArray(rezArten);
        startVo2020 = listTypesOfVO.indexOf(STANDARD_VO);
    }
    
    
    public String[] getAll () {
        return rezArten;
    }

    public String[] getHmrOld () {
        String[] voArten = new String[startVo2020];
        System.arraycopy(rezArten, 0, voArten, 0, startVo2020);
        return voArten;
    }

    public String[] getHmr2020 () {
        int len = rezArten.length - startVo2020;
        String[] vo2020 = new String[len];
        System.arraycopy(rezArten, startVo2020, vo2020, 0, len);
        return vo2020;
    }
    public static String getTypeOfVo (int idxArtDerVo) {
            return (idxArtDerVo < rezArten.length ? rezArten[idxArtDerVo] : "");
    }
}
