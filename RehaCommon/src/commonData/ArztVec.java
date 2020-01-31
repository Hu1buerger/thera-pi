package commonData;

import java.util.Vector;

import CommonTools.SqlInfo;
import CommonTools.StringTools;

public class ArztVec {
    private Vector<Vector<String>> vecvec_aerzte;
    private Vector<String> vec_arzt;    

    // Indices für Zugriffe auf vec_arzt
    final int ANREDE = 0, TITEL = 1, NACHNAME = 2, VORNAME = 3, STRASSE = 4, PLZ = 5, ORT = 6, FACHARZT = 7,
            TELEFON = 8, FAX = 9, MATCHCODE = 10, LANR = 11, KLINIK = 12, MTEXT = 13, EMAIL1 = 14, 
            EMAIL2 = 15, ID = 16, BSNR = 17;

    public ArztVec() {
        vecvec_aerzte = new Vector<Vector<String>>();
        vec_arzt = new Vector<String>();
    }
    
    public boolean init(int idInDB) {
        String cmd = "select * from arzt where id='" + idInDB + "' LIMIT 1";
        return getRecord (cmd);
    }
    
    public boolean init(String idInDb) {
        String cmd = "select * from arzt where id ='" + idInDb + "' LIMIT 1";
        return getRecord (cmd);
    }

    private boolean getRecord (String cmd) {
        this.vecvec_aerzte = SqlInfo.holeFelder(cmd);
        if(this.vecvec_aerzte.size()<=0){
            // RezeptVektor ist leer
            this.vec_arzt = null;
            return Boolean.FALSE;
        }
        setTo1stVec_arzt(this.vecvec_aerzte);
        return Boolean.TRUE;
    }

    public boolean createEmptyVec() {
        try {
            String cmd = "describe arzt";
            this.vecvec_aerzte = SqlInfo.holeFelder(cmd);
            for (int i = 0; i < this.vecvec_aerzte.size(); i++){
                this.vec_arzt.add(i, "");
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
    }

    public ArztVec getInstance(){
        return this;
    }

    private void setTo1stVec_arzt(Vector<Vector<String>> vecvec_aerzte) {
        this.vec_arzt = this.vecvec_aerzte.get(0);
    }

    private int getIntAt(int index){
        return StringTools.ZahlTest(this.vec_arzt.get(index));
    }
    private void setIntAt(int index, int data){
        this.vec_arzt.set(index, Integer.valueOf(data).toString());
    }
    private void setIntAt(int index, String data){
        this.vec_arzt.set(index, Integer.valueOf(data).toString());
    }
    
    private String getStringAt(int index){
        return this.vec_arzt.get(index).trim();
    }
    private void setStringAt(int index, String data) {
        this.vec_arzt.set(index, data.trim());
    }

    /*
     * Kompatibilitätsmodus
     */
    public void setvecvec_aerzte(Vector<Vector<String>> vecvec_tmp) {
        this.vecvec_aerzte = vecvec_tmp;
        setTo1stVec_arzt(this.vecvec_aerzte);
    }

    public Vector<Vector<String>> getVecVec_aerzte() {
        return this.vecvec_aerzte;
    }
    // Kompatibilitätsmodus Ende

    public Vector<String> getVec_arzt() {
        return vec_arzt;
    }

    public void setVec_arzt(Vector<String> vec_tmp) {
        this.vec_arzt = vec_tmp;
    }
    
    public int getVecSize() {
        return vec_arzt.size();
    }

    public boolean isEmpty(){
        if (getVecSize() <= 0){
            return true;
        }else{
            return false;           
        }
    }

    public String getAnrd() {
        return getStringAt(ANREDE);
    }
    public void setAnrd(String data) {
        setStringAt(ANREDE, data);
    }

    public String getTitel() {
        return getStringAt(TITEL);
    }
    public void setTitel(String data) {
        setStringAt(TITEL, data);
    }

    public String getNName() {
        return getStringAt(NACHNAME);
    }
    public void setNName(String data) {
        setStringAt(NACHNAME, data);
    }

    public String getVName() {
        return getStringAt(VORNAME);
    }
    public void setVName(String data) {
        setStringAt(VORNAME, data);
    }

    public String getStr() {
        return getStringAt(STRASSE);
    }
    public void setStr(String data) {
        setStringAt(STRASSE, data);
    }

    public String getPlz() {
        return getStringAt(PLZ);
    }
    public void setPlz(String data) {
        setStringAt(PLZ, data);
    }

    public String getOrt() {
        return getStringAt(ORT);
    }
    public void setOrt(String data) {
        setStringAt(ORT, data);
    }

    public String getFach() {
        return getStringAt(FACHARZT);
    }
    public void setFach(String data) {
        setStringAt(FACHARZT, data);
    }

    public String getTel() {
        return getStringAt(TELEFON);
    }
    public void setTel(String data) {
        setStringAt(TELEFON, data);
    }

    public String getFax() {
        return getStringAt(FAX);
    }
    public void setFax(String data) {
        setStringAt(FAX, data);
    }

    public String getMatch() {
        return getStringAt(MATCHCODE);
    }
    public void setMatch(String data) {
        setStringAt(MATCHCODE, data);
    }

    public String getLANR() {
        return getStringAt(LANR);
    }
    public void setLANR(String data) {
        setStringAt(LANR, data);
    }

    public String getKlinik() {
        return getStringAt(KLINIK);
    }
    public void setKlinik(String data) {
        setStringAt(KLINIK, data);
    }

    public String getMTxt() {   // Notiz in Ärzte-Übersicht
        return getStringAt(MTEXT);
    }
    public void setMTxt(String data) {
        setStringAt(MTEXT, data);
    }

    public String getEMail() {
        return getStringAt(EMAIL1);
    }
    public void setEMail(String data) {
        setStringAt(EMAIL1, data);
    }

    public String getEMail2() {
        return getStringAt(EMAIL2);
    }
    public void setEMail2(String data) {
        setStringAt(EMAIL2, data);
    }

    public String getBSNR() {
        return getStringAt(BSNR);
    }
    public void setBSNR(String data) {
        setStringAt(BSNR, data);
    }

    public int getId() {
        return getIntAt(ID);
    }
    public String getIdS() {
        return getStringAt(ID);
    }
    public void setId(int data) {
        setIntAt(ID, data);
    }

    public String getNNameLanr() {
        if (getLANR().length() > 0 ) {
            return (getNName() + " - " + getLANR());            
        } else {
            return getNName();
        }
    }

    public void writeArzt2DB(){
        StringBuffer cmd = new StringBuffer();
        cmd.append("anrede='" + getAnrd() + "', "); 
        cmd.append("titel='" + getTitel() + "', ");
        cmd.append("nachname='" + getNName() + "', ");
        cmd.append("vorname='" + getVName() + "', ");
        cmd.append("strasse='" + getStr() + "', ");
        cmd.append("plz='" + getPlz() + "', ");
        cmd.append("ort='" + getOrt() + "', ");
        cmd.append("facharzt='" + getFach() + "', ");
        cmd.append("telefon='" + getTel() + "', ");
        cmd.append("fax='" + getFax() + "', ");
        cmd.append("matchcode='" + getMatch() + "', ");
        cmd.append("arztnum='" + getLANR() + "', ");
        cmd.append("klinik='" + getKlinik() + "', ");
        cmd.append("mtext='" + getMTxt() + "', ");
        cmd.append("email1='" + getEMail() + "', ");
        cmd.append("email2='" + getEMail2() + "', ");
        cmd.append("bsnr='" + getBSNR() + "'");
        if (getId() < 0) {
            cmd.insert(0, "insert into arzt set "); // insert new
        } else {
            cmd.insert(0, "update arzt set ");  // update existing
            cmd.append(" where id = " + getId());
        }
        SqlInfo.sqlAusfuehren(cmd.toString());
        int tmp = Integer.valueOf(SqlInfo.holeEinzelFeld(new String ("select max(id) from arzt")));
        setId(tmp);
    }
}
