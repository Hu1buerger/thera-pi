package commonData;

import java.util.Vector;

import javax.swing.JOptionPane;

import CommonTools.SqlInfo;
import CommonTools.StringTools;
import CommonTools.DatFunk;

public class Rezept {
	private Vector<Vector<String>> vecvec_rezepte;
	private Vector<String> vec_rezept;

	public Rezept() {
		vecvec_rezepte = new Vector<Vector<String>>();
		vec_rezept = new Vector<String>();
	}

	public boolean init(String rezNr) {
		String cmd = "select * from verordn where rez_nr='"+rezNr.trim()+"' LIMIT 1";
		//System.out.println("Kommando = "+cmd);
		this.vecvec_rezepte = SqlInfo.holeFelder(cmd);
		if(this.vecvec_rezepte.size()<=0){
			System.out.println("RezeptVektor ist leer");
			this.vec_rezept = null;
			return Boolean.FALSE;
		}
		setTo1stVec_rez(this.vecvec_rezepte);
		return Boolean.TRUE;
	}

	public boolean createEmptyVec() {
		try {
			String cmd = "describe verordn";
			this.vecvec_rezepte = SqlInfo.holeFelder(cmd);
			for (int i = 0; i < this.vecvec_rezepte.size(); i++){
				this.vec_rezept.add(i, "");
			}
			return Boolean.TRUE;
		} catch (Exception e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
	}

	public Rezept getInstance(){
		return this;
	}

	private void setTo1stVec_rez(Vector<Vector<String>> vecvec_rez) {
		this.vec_rezept = this.vecvec_rezepte.get(0);
		//System.out.println("RezeptVektor = "+this.vec_rezept);
	}

	private boolean getBoolAt(int index){
		return this.vec_rezept.get(index).trim().equals("T");
	}

	private void setBoolAt(int index, boolean data){
		this.vec_rezept.set(index, (Boolean.valueOf(data) ? "T" : "F"));
	}
	private void setBoolAt(int index, String data){
		if (data.equalsIgnoreCase("F") || data.equalsIgnoreCase("T")){
			this.vec_rezept.set(index, data.toUpperCase());						
		}else{
			System.out.println("Fehler setBoolAt(String) idx: "+index+" val: "+data);			
		}
	}

	private int getIntAt(int index){
//		return Integer.parseInt(this.vec_rezept.get(index));
		return StringTools.ZahlTest(this.vec_rezept.get(index));
	}
	private void setIntAt(int index, int data){
		this.vec_rezept.set(index, Integer.valueOf(data).toString());
	}
	private void setIntAt(int index, String data){
		this.vec_rezept.set(index, Integer.valueOf(data).toString());
	}
	
	private String getStringAt(int index){
		return this.vec_rezept.get(index).trim();
	}
	private void setStringAt(int index, String data) {
		this.vec_rezept.set(index, data.trim());
	}


	/*
	 * Kompatibilitätsmodus
	 */
	public void setVecVec_rez(Vector<Vector<String>> vecvec_rez) {
		this.vecvec_rezepte = vecvec_rez;
		setTo1stVec_rez(this.vecvec_rezepte);
	}

	public Vector<Vector<String>> getVecVec_rez() {
		return this.vecvec_rezepte;
	}
	// Kompatibilitätsmodus Ende

	public Vector<String> getVec_rez() {
		return vec_rezept;
	}

	public void setVec_rez(Vector<String> vec_rez) {
		this.vec_rezept = vec_rez;
	}
	
	public int getVecSize() {
		return vec_rezept.size();
	}

	public boolean isEmpty(){
		if (getVecSize() <= 0){
			return true;
		}else{
			return false;			
		}
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
		return getStringAt(1).substring(0,2).toUpperCase();
	}

	public String getRezeptDatum() {
		return getStringAt(2);
	}
	public void setRezeptDatum(String rezDat) {
		setStringAt(2,rezDat);
	}

	/**
	 * Gibt Anzahl der Behandlungen für den angefragten Index zurück.
	 * 
	 * @param index zu ermittelnde Position im Rezept(1..4)
	 * 
	 * @return Anzahl der Behandlungen
	 */
	public int getAnzBeh(int index) {
		return getIntAt(2+index);
	}
	public String getAnzBehS(int index) {
		return getStringAt(2+index);
	}
	public void setAnzBeh(int index, String data) {
		if (0 < index && index < 5){
			setStringAt(2+index, data);		
		}else{
			System.out.println("Indexfehler setAnzBeh: "+index);
		}
	}

	public String getKm() {
		return getStringAt(7);
	}
	public void setKm(String distance) {
		setStringAt(7,distance);
	}

	/**
	 * Gibt Preislisten-ID der HM-Position des angefragten Index zurück.
	 * 
	 * @param index zu ermittelnde Position im Rezept(1..4)
	 * 
	 * @return ID der verordneten Behandlung in der Preisliste
	 */
	public String getArtDBehandl(int index) {
		return getStringAt(7+index);
	}
	public void setArtDBehandl(int index, String data) {
		if (0 < index && index < 5){
			setStringAt(7+index, data);
		}else{
			System.out.println("Indexfehler setArtDBehandl: "+index);
		}
	}

	public Double getGebuehrBetrag(){
		return Double.parseDouble(vec_rezept.get(13));
	}
	public void setGebuehrBetrag(Double data){
		setStringAt(13,data.toString());
	}
	public void setGebuehrBetrag(String data) {
		setStringAt(13,data);
	}
	
	public boolean getBefreit(){
		return getBoolAt (12);
	}
	public String getBefreitS(){
		return getStringAt(12);
	}
	public void setBefreit(String data){
		setBoolAt (12, data);
	}

	public boolean getGebuehrBezahlt(){
		return getBoolAt (14);
	}
	public String getGebuehrBezahltS(){
		return getStringAt (14);
	}
	public void setGebuehrBezahlt(boolean data){
		setBoolAt (14, data);
	}

	public String getArzt() {
		return getStringAt(15);
	}

	public void setArzt(String arzt) {
		setStringAt(15,arzt);
	}

	public String getArztId() {
		return getStringAt(16);
	}

	public void setArztId(String arztId) {
		setStringAt(16,arztId);
	}

	/**
	 * Gibt Preis der Behandlung für den angefragten Index zurück.
	 * 
	 * @param index zu ermittelnde Position im Rezept(1..4)
	 * 
	 * @return Preis der Behandlung
	 */
	public String getPreis(int index) {
		return getStringAt(17+index);
	}
	public void setPreis(int index, String data) {
		if (0 < index && index < 5){
			setStringAt(17+index, data);
		}else{
			System.out.println("Indexfehler setPreis: "+index);
		}
	}

	public String getAngelegtDatum() {
		return getStringAt(22);
	}
	public void setAngelegtDatum(String Dat) {
		setStringAt(22,Dat);
	}

	public String getDiagn() {
		return getStringAt(23);
	}
	public void setDiagn(String diag) {
		setStringAt(23, diag);
	}

	public boolean getHeimbew(){
		return getBoolAt (24);
	}
	public String getHeimbewS(){
		return getStringAt (24);
	}
	public void setHeimbew(boolean data){
		setBoolAt (24, data);
	}
	public void setHeimbew(String data){
		setBoolAt (24, data);
	}

	public int getRezArt() {
		return getIntAt(27);
	}
	public void setRezArt(int rezArt) {
		setIntAt(27,rezArt);
	}

	public String getTermine() {
		return getStringAt(34);
	}
	public void setTermine(String termine) {
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
		setStringAt(36,ktrName);
	}
	
	/**
	 * @return Kassen-Id	
	 */
	public String getKtraeger() {
		return getStringAt(37);
	}
	public void setKtraeger(String ktraeger) {
		setStringAt(37,ktraeger);
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
		setStringAt(40,lastDat);
	}

	public String getPreisgruppe() {
		return getStringAt(41);
	}
	public void setPreisgruppe(String data) {
		setStringAt(41, data);
	}
	
	public boolean getBegrAdR(){
		return getBoolAt (42);
	}
	public String getBegrAdRS(){
		return getStringAt(42);
	}
	public void setBegrAdR(boolean data){
		setBoolAt (42, data);
	}

	public boolean getHausbesuch(){
		return getBoolAt (43);
	}
	public String getHausbesuchS(){
		return getStringAt (43);
	}
	public void setHausbesuch(boolean data) {
		setStringAt(43, (data ? "T" : "F"));
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
		setStringAt(46, Integer.valueOf(data).toString());
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
		return getStringAt(47+index);
	}
	public void setHmPos(int index, String data) {
		if (0 < index && index < 5){
			setStringAt(47+index, data);
		}else{
			System.out.println("Indexfehler setHmPos: "+index);
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
		setStringAt(53,user);
	}
		
	public String getArztBerichtID() {
		return getStringAt(54);
	}

	public boolean getArztbericht(){
		return getBoolAt (55);
	}
	public String getArztberichtS(){
		return getStringAt(55);
	}
	public void setArztBericht(boolean data) {
		setStringAt(55, (data ? "T" : "F"));
	}
		
	public String getLastEdDate() {
		return getStringAt(56);
	}
	public void setLastEdDate(String lastEdDat) {
		setStringAt(56,lastEdDat);
	}

	public int getFarbCode() {
		return getIntAt(57);
	}
	public void setFarbCode(int code) {
		setStringAt(57, Integer.valueOf(code).toString());
	}

	public String getvorJahrFrei() {
		return getStringAt(59);
	}
	public void setvorJahrFrei(String data) {
		setStringAt(59, data);
	}

	public boolean getUnter18(){
		return getBoolAt (60);
	}
	public String getUnter18S(){
		return getStringAt (60);
	}
	public void setUnter18(String data){
		setBoolAt (60, data);
	}
	public void setUnter18(boolean data){
		setBoolAt (60, data);
	}

	/**
	 * prüft, ob Hausbesuch voll abrechenbar ist.
	 * 
	 * @return Flag; TRUE, wenn Hausbesuch voll abrechenbar ist.
	 */
	public boolean getHbVoll(){
		return getBoolAt (61);
	}
	public String getHbVollS(){
		return getStringAt (61);
	}
	public void setHbVoll(boolean data){
		setBoolAt (61, data);
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
		return getStringAt(64+index);
	}
	public void setHMkurz(int index, String data) {
		if (0 < index && index < 7){		// es gibt 6 Kuerzel!
			setStringAt(64+index, data);
		}else{
			System.out.println("Indexfehler setHMkurz: "+index);
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

	public void setNewRezNb(String rezClass) {
		int reznr = SqlInfo.erzeugeNummer(rezClass.toLowerCase()
				);
		if(reznr < 0){
			JOptionPane.showMessageDialog(null,"Schwerwiegender Fehler beim Bezug einer neuen Rezeptnummer!");
			return;
		}
		this.setRezNb(rezClass.toUpperCase()+Integer.valueOf(reznr).toString());

		int rezidneu = SqlInfo.holeId("verordn", "diagnose");
		this.setId(rezidneu);
	}

	public void writeRez2DB(){
		StringBuffer cmd = new StringBuffer("update verordn set ");
		cmd.append("pat_intern='"+getPatIntern()+"', "); 
		cmd.append("rez_nr='"+getRezNb()+"', ");
		cmd.append("rez_datum='"+getRezeptDatum()+"', ");
		for (int i=1; i<5; i++){
			String idx = Integer.valueOf(i).toString();
			cmd.append("anzahl"+idx+"='"+getAnzBehS(i)+"', ");
			cmd.append("art_dbeh"+idx+"='"+getArtDBehandl(i)+"', ");
			cmd.append("preise"+idx+"='"+getPreis(i)+"', ");
			cmd.append("pos"+idx+"='"+getHmPos(i)+"', ");
			cmd.append("kuerzel"+idx+"='"+getHMkurz(i)+"', ");
		}
		for (int i=5; i<7; i++){
			String idx = Integer.valueOf(i).toString();
			cmd.append("kuerzel"+idx+"='"+getHMkurz(i)+"', ");
		}
		cmd.append("anzahlkm='"+getKm()+"', ");
		cmd.append("befr='"+getBefreitS()+"', ");
		// rez_geb
		cmd.append("rez_bez='"+getGebuehrBezahltS()+"', ");
		cmd.append("arzt='"+getArzt()+"', ");
		cmd.append("arztid='"+getArztId()+"', ");
		// aerzte
		cmd.append("datum='"+getAngelegtDatum()+"', ");
		cmd.append("diagnose='"+getDiagn()+"', ");
		cmd.append("heimbewohn='"+getHeimbewS()+"', ");
		// veraenderd, veraendera
		cmd.append("rezeptart='"+getRezArt()+"', ");
		// logfrei1, logfrei2, numfrei1, numfrei2, charfrei1, charfrei2, termine, id
		cmd.append("ktraeger='"+getKtrName()+"', ");
		cmd.append("kid='"+getKtraeger()+"', ");
		cmd.append("patid='"+getPatIdS()+"', ");
		cmd.append("zzstatus='"+getZzStat()+"', ");
		cmd.append("lastdate='"+getLastDate()+"', ");
		cmd.append("preisgruppe='"+getPreisgruppe()+"', ");
		cmd.append("begruendadr='"+getBegrAdRS()+"', ");
		cmd.append("hausbes='"+getHausbesuchS()+"', ");
		cmd.append("indikatschl='"+getIndiSchluessel()+"', ");
		cmd.append("angelegtvon='"+getAngelegtVon()+"', ");
		cmd.append("barcodeform='"+getBarcodeform()+"', ");
		cmd.append("dauer='"+getDauer()+"', ");
		cmd.append("frequenz='"+getFrequenz()+"', ");
		cmd.append("lastedit='"+getLastEdit()+"', ");
		// berid
		cmd.append("arztbericht='"+getArztberichtS()+"', ");
		cmd.append("lasteddate='"+getLastEdDate()+"', ");
		cmd.append("farbcode='"+getFarbCode()+"', ");
		// rsplit
		cmd.append("jahrfrei='"+getvorJahrFrei()+"', ");
		cmd.append("unter18='"+getUnter18S()+"', ");
		cmd.append("hbvoll='"+getHbVollS()+"', ");
		// abschluss
		cmd.append("zzregel='"+getZzRegel()+"', ");
		cmd.append("anzahlhb='"+getAnzHB()+"', ");
		cmd.append("icd10='"+getICD10()+"', ");
		cmd.append("icd10_2='"+getICD10_2()+"' ");
		cmd.append(", rsplit='rez2db' ");	// -> debug-Hilfe
		
		cmd.append(" where id='"+getId()+"' LIMIT 1");
		SqlInfo.sqlAusfuehren(cmd.toString());
	}
}