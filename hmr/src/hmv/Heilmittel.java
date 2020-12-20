package hmv;

import java.util.Objects;

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
	@Override
	public String toString() {
		return "Heilmittel [heilmittel=" + heilmittel + ", heilmittel_beschreibung=" + heilmittel_beschreibung
				+ ", heilmittelposition=" + heilmittelposition + ", hmr_disziplin=" + hmr_disziplin + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(Dauer_bis, Dauer_von, Maximal, heilmittel, heilmittel_beschreibung, heilmittelposition,
				hmr_disziplin, vorrangig);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Heilmittel)) {
			return false;
		}
		Heilmittel other = (Heilmittel) obj;
		return Dauer_bis == other.Dauer_bis && Dauer_von == other.Dauer_von && Maximal == other.Maximal
				&& Objects.equals(heilmittel, other.heilmittel)
				&& Objects.equals(heilmittel_beschreibung, other.heilmittel_beschreibung)
				&& Objects.equals(heilmittelposition, other.heilmittelposition) && hmr_disziplin == other.hmr_disziplin
				&& vorrangig == other.vorrangig;
	}
	
	
}
