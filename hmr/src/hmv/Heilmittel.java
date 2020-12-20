package hmv;

import core.Disziplin;

public class Heilmittel{
	public final String heilmittel;
	public final String heilmittel_beschreibung;
	public final String heilmittelposition;
	public final int Dauer_von;
	public final int Dauer_bis;
	public final boolean vorrangig;
	public final int Maximal;
	public	final  Disziplin hmr_disziplin;
	public Heilmittel(String heilmittel,
			String heilmittel_beschreibung, 
			String heilmittelposition, 
			int dauer_von, 
			int dauer_bis,
			boolean vorrangig, 
			int maximal, 
			Disziplin hmr_disziplin) {
		super();
		this.heilmittel = heilmittel;
		this.heilmittel_beschreibung = heilmittel_beschreibung;
		this.heilmittelposition = heilmittelposition;
		Dauer_von = dauer_von;
		Dauer_bis = dauer_bis;
		this.vorrangig = vorrangig;
		Maximal = maximal;
		this.hmr_disziplin = hmr_disziplin;
	}
	
	
}
