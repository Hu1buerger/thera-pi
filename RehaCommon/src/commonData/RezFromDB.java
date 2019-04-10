package commonData;

import java.util.Iterator;
import java.util.Vector;

import CommonTools.SqlInfo;

public class RezFromDB {

	private Vector <Vector<String>> vecInArbeit;
	private Vector <Vector<String>> vecFertige;
	private String aktKasse;

	/**
	 * initialisiert vecInArbeit mit allen Verordnungen zur uebergebenen Kasse, die sich noch nicht im LZA befinden.
	 * 
	 * @param kasse IK der zu pruefenden Kasse
	 * @return true, wenn VOs existieren 
	 */
	public boolean initActiveVO(String kasse) {
		aktKasse = kasse;
		String sucheAktiveVO = "SELECT t1.rez_nr FROM verordn AS t1 LEFT JOIN kass_adr AS t2 ON t1.kid = t2.id where t2.ik_kasse="+aktKasse;
		vecInArbeit = SqlInfo.holeFelder(sucheAktiveVO);
		if (vecInArbeit.size() > 0){
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * liefert noch in Arbeit befindliche Verordnungen zur uebergebenen Kasse.
	 * 
	 * @param kasse IK der zu pruefenden Kasse
	 * 
	 * @return Vektor mit den Rezeptnummern in Arbeit befindlicher Verordnungen
	 */
	public Vector<Vector<String>> getPendingVO (String kasse){
		if (!kasse.equals(this.aktKasse)){
			this.initActiveVO(kasse);
		}
		if (vecInArbeit.size() > 0){
			String sucheFertig = "SELECT rez_nr FROM fertige where ikkasse="+aktKasse;
			vecFertige = SqlInfo.holeFelder(sucheFertig);
	        assert (vecInArbeit.size()  > vecFertige.size()) :"more 'ready' than 'under processing' -something wrong here";	
	        Vector<Vector<String>> offen = vecInArbeit;
	        for( Iterator it = vecFertige.iterator(); it.hasNext(); )
	        {
	        	Object curr = it.next();
	        	if(offen.contains(curr)){
	        		offen.remove(curr);
	        	}
	        }
	        return offen;
		}
		return null;
	}
}
