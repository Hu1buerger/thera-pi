/**
 * 
 */
package abrechnung;

import java.util.HashMap;
import java.util.Vector;

/**
 * merkt sich die Größeneinstellung des 'TageTree' Panel für versch. Anzahlen von Behandlungstagen
 * @author McM
 */
class TageTreeSize {
	private static HashMap<Integer,Integer> hmTageTreeSize = new HashMap<Integer,Integer>();
	private static HashMap<Integer,Integer> hmTageTreeIni = new HashMap<Integer,Integer>();		// keep start values from ini file
	private static int currNbDays;
	private static int windowSize = 100;
	private static boolean windowResized = false;

	public TageTreeSize (){
		TageTreeSize.currNbDays = -1;
	}

	public void setAnzTage (int days){
		if (days > 0){
			currNbDays = days;
		}
	}

	/*
	 * Hashmap-Eintrag anlegen/aktualisieren
	 * 
     * @param days Anzahl Tage
     * @param ysize Höhe des TageTree, wie in ini abgelegt (<0; Differenz zu Gesamthöhe des Split-Pane)
	 */
	public void setDaysAndSize (int days, int ySizeRel){
		if(days > 0){
			hmTageTreeSize.put(days,ySizeRel);
			hmTageTreeIni.put(days,ySizeRel);
		}
	}

	/*
	 * Größe des TageTree in HashMap eintragen
	 * (setzt voraus, dass Anz. der Tage bereits gesetzt ist)
	 * 
     * @param ysize Höhe des TageTree (>0, entspr. Pos. Unterkante des Split-Pane-Divider)
	 */
	public void setTageTreeSize (int ySize){
		if(currNbDays > 0){
			hmTageTreeSize.put(currNbDays,ySize-windowSize);
		}
	}

	/*
	 * Größe des TageTree in HashMap eintragen
	 *
	 * bei Größenänderung des umschließenden Fensters (-> divider meldet change), bleibt die Höhe des Tagetree unverändert
	 * 
     * @param ysize Höhe des TageTree (>0, entspr. Pos. Unterkante des Split-Pane-Divider)
     * @param max Höhe des umschließenden Panel (Split-Pane)
	 */
	public void setTageTreeSize (int ySize, int max){
		if(windowSize != max){		// Window-resize
			windowSize = max;
		}else{
			if(currNbDays > 0){		// noch kein Rezept markiert
				hmTageTreeSize.put(currNbDays,ySize - max);
			}			
		}
	}
	
	/*
	 * liefert Größe des TageTree für eine Anz. Behandlungstage
	 * ist  noch kein Wert hinterlegt, wird 1/4 der Höhe  des Split-Pane zurückgegeben
	 *
     * @param days Anz. der Behandlungstage
     * @return Abstand Oberkante des Tagetree von der Oberkante des umschließenden Split-Pane
	 */
	public int getTageTreeSize (int days){
		if (hmTageTreeSize.containsKey(days)){
			int tmp = hmTageTreeSize.get(days) + windowSize;
			if ((tmp > 0) && (tmp < windowSize)){
				return tmp;							
			}
		}
		return windowSize * 3/4;		// default
	}
	
	public int getCurrTageTreeSize (){
		return getTageTreeSize (currNbDays);							
	}
	
	public boolean getTTSchanged(int days){
		if (hmTageTreeSize.containsKey(days)){
			if (hmTageTreeIni.containsKey(days)){
				if (!hmTageTreeSize.get(days).equals(hmTageTreeIni.get(days)) ){
					return true;	// Wert geändert			
				}
				// Wert nicht geändert
			}else{					
				return true;		// in HMap, aber nicht in ini ( -> neu)			
			}
		}
		return false;				// Wert nicht geändert (bei nicht in HMap wird nicht erst gefragt)
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<Integer,Integer> getHmTageTreeSize(){
		return (HashMap<Integer, Integer>) hmTageTreeSize.clone();
	}
}
