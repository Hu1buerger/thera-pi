package CommonTools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class OpShowGesamt {
	private JLabel valGesamtOffen;
	private JLabel valSuchOffen;
	private JLabel valSuchGesamt;
	private JLabel valAnzahlSaetze;
	private BigDecimal gesamtOffen;
	private BigDecimal suchOffen;
	private BigDecimal suchGesamt;
	private int records;
	private JPanel auswertung;
	
	DecimalFormat dcf = new DecimalFormat("###0.00");

	
	public OpShowGesamt() {
		auswertung = new JPanel();
		JLabel tmpLbl = new JLabel();
		
		FormLayout lay = new FormLayout(
		//       1    2      3    4        5        6    7
				"5dlu,145dlu,5dlu,100dlu:g,182dlu:g,5dlu,40dlu",	// xwerte,
		//       1    2 3    4 5    6 7    8 9
				"0dlu,p,3dlu,p,2dlu,p,2dlu,p,5dlu"					// ywerte
				);
		PanelBuilder builder = new PanelBuilder(lay);
		//PanelBuilder builder = new PanelBuilder(lay, new FormDebugPanel());		// debug mode

		builder.getPanel().setOpaque(false);
		CellConstraints cc = new CellConstraints();

		tmpLbl = builder.addLabel("Offene Posten gesamt:", cc.xy(2,2,CellConstraints.RIGHT,CellConstraints.DEFAULT));
		tmpLbl.setToolTipText("Summe OP in ausgewählten Rechnungsarten");
//		tmpLbl.setToolTipText("Summe OP in allen Rechnungsarten");
		valGesamtOffen = builder.addLabel("0,00", cc.xy(4,2,CellConstraints.LEFT,CellConstraints.DEFAULT));
		valGesamtOffen.setForeground(Color.RED);
		Font f = valGesamtOffen.getFont();
		valGesamtOffen.setFont(f.deriveFont(f.getStyle() | Font.BOLD));

		tmpLbl = builder.addLabel("Offene Posten der letzten Abfrage:", cc.xy(2,4,CellConstraints.RIGHT,CellConstraints.DEFAULT));
		tmpLbl.setToolTipText("Summe OP in den zuletzt gesuchten Rechnungen");
		valSuchOffen = builder.addLabel("0,00", cc.xy(4,4,CellConstraints.LEFT,CellConstraints.DEFAULT));
		valSuchOffen.setForeground(Color.RED);

		builder.addLabel("Summe Rechnunsbeträge der letzten Abfrage:", cc.xy(5,2,CellConstraints.RIGHT,CellConstraints.DEFAULT));
		valSuchGesamt = builder.addLabel("0,00", cc.xy(7,2,CellConstraints.LEFT,CellConstraints.DEFAULT));
		valSuchGesamt.setForeground(Color.BLUE);

		builder.addLabel("Anzahl Datensätze der letzten Abfrage:", cc.xy(5,4,CellConstraints.RIGHT,CellConstraints.DEFAULT));
		valAnzahlSaetze = builder.addLabel("0", cc.xy(7,4,CellConstraints.LEFT,CellConstraints.DEFAULT));
		valAnzahlSaetze.setForeground(Color.BLUE);

		auswertung.add(builder.getPanel());
	}
	
	public Component getPanel() {
		return auswertung;
	}
/*
	public void schreibeGesamtOffen(String betrag){
		valGesamtOffen.setText( betrag );
		auswertung.validate();
	}

	public void schreibeGesamtOffen(BigDecimal val){
		valGesamtOffen.setText( dcf.format(val) );
		auswertung.validate();
	}
*/
	public void schreibeGesamtOffen(){
		valGesamtOffen.setText( dcf.format(gesamtOffen) );
		auswertung.validate();
	}

	public BigDecimal getGesamtOffen(){
		return gesamtOffen;
	}

	public void setGesamtOffen(BigDecimal val){
		gesamtOffen = val;
	}

	public void substFromGesamtOffen(BigDecimal val){
		gesamtOffen = gesamtOffen.subtract(val);
		schreibeGesamtOffen();
	}

	public void ermittleGesamtOffen(boolean useRGR, boolean useAFR, boolean useVKR){
		gesamtOffen = BigDecimal.ZERO;
		if (useRGR) {
//		if (true) {
			String offen = SqlInfo.holeEinzelFeld("select sum(roffen) from rgaffaktura where roffen > '0.00' AND rnr LIKE 'RGR-%'");
			if (!offen.isEmpty()){
				gesamtOffen = gesamtOffen.add(BigDecimal.valueOf( Double.parseDouble(offen) ));				
			}
		}
		if (useAFR) {
//		if (true) {
			String offen = SqlInfo.holeEinzelFeld("select sum(roffen) from rgaffaktura where roffen > '0.00' AND rnr LIKE 'AFR-%'");
			boolean tmp = offen.isEmpty();
			int tst = offen.length();
			if (!offen.isEmpty()){
				gesamtOffen = gesamtOffen.add(BigDecimal.valueOf( Double.parseDouble(offen) ));				
			}
		}
		if (useVKR) {
//		if (true) {
			String offen = SqlInfo.holeEinzelFeld("select sum(v_offen) from verkliste where v_offen > '0.00' AND v_nummer LIKE 'VR-%'");
			if (!offen.isEmpty()){
				gesamtOffen = gesamtOffen.add(BigDecimal.valueOf( Double.parseDouble(offen) ));				
			}
		}
		schreibeGesamtOffen();
	}

	public void schreibeSuchOffen(){
		valSuchOffen.setText( dcf.format(suchOffen) );
		auswertung.validate();
	}

	public BigDecimal getSuchOffen(){
		return suchOffen;
	}

	public void setSuchOffen(BigDecimal val){
		suchOffen = val;
	}

	public void substFromSuchOffen(BigDecimal val){
		suchOffen = suchOffen.subtract(val);
		schreibeSuchOffen ();
	}

	public void delSuchOffen() {
		suchOffen = BigDecimal.ZERO;
		schreibeSuchOffen ();
	}

	public void schreibeSuchGesamt(){
		valSuchGesamt.setText( dcf.format(suchGesamt) );
		auswertung.validate();
	}

	public BigDecimal getSuchGesamt(){
		return suchGesamt;
	}

	public void setSuchGesamt(BigDecimal val){
		suchGesamt = val;
	}

	public void substFromSuchGesamt(BigDecimal val){
		suchGesamt.subtract(val);
		schreibeSuchGesamt();
	}

	public void delSuchGesamt() {
		suchGesamt = BigDecimal.ZERO;
		schreibeSuchGesamt();
	}

	public void schreibeAnzRec(){
		valAnzahlSaetze.setText(Integer.toString(records) );
		auswertung.validate();
	}

	public void incAnzRec(){
		records++;
	}

	public int getAnzRec(){
		return records;
	}

	public void setAnzRec(int val){
		records = val;
	}

	public void delAnzRec() {
		records = 0;
		schreibeAnzRec();
	}

}
