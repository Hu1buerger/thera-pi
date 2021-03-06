package opRgaf;

import static opRgaf.OffenePostenTableModel.GESAMTBETRAG;
import static opRgaf.OffenePostenTableModel.KENNUNG;
import static opRgaf.OffenePostenTableModel.KRANKENKASSENNAME;
import static opRgaf.OffenePostenTableModel.OFFEN;
import static opRgaf.OffenePostenTableModel.REZNUMMER;
import static opRgaf.OffenePostenTableModel.RGDATUM;
import static opRgaf.OffenePostenTableModel.RGNR;

import javax.swing.JComboBox;
final class OffenePostenComboBox extends JComboBox<CBModel> {


    static final CBModel REZNUMMER_ENTHAELT = new CBModel("Rezeptnummer enth\u00e4lt", new OffenePostenTextFilter(REZNUMMER));

    public OffenePostenComboBox(int vorauswahl) {
        addItem(new CBModel("Rechnungsnummer enth\u00e4lt", new OffenePostenTextFilter(RGNR)));
        addItem(new CBModel("Rechnungsdatum =", new OffenePostenDatumFilter(RGDATUM,Strategy.gleich)));
        addItem(new CBModel("Rechnungsdatum >=", new OffenePostenDatumFilter(RGDATUM,Strategy.groesserOderGleich)));
        addItem(new CBModel("Rechnungsdatum <=", new OffenePostenDatumFilter(RGDATUM,Strategy.kleinerOderGleich)));

        addItem(new CBModel("offen =", new OffenePostenMoneyFilter(OFFEN,Strategy.gleich)));
        addItem(new CBModel("offen >=", new OffenePostenMoneyFilter(OFFEN,Strategy.groesserOderGleich)));
        addItem(new CBModel("offen <=", new OffenePostenMoneyFilter(OFFEN,Strategy.kleinerOderGleich)));
        addItem(new CBModel("Rechnungsbetrag", new OffenePostenMoneyFilter(GESAMTBETRAG,Strategy.gleich)));
        addItem(new CBModel("Rechnungsbetrag >=", new OffenePostenMoneyFilter(GESAMTBETRAG,Strategy.groesserOderGleich)));
        addItem(new CBModel("Rechnungsbetrag <=", new OffenePostenMoneyFilter(GESAMTBETRAG,Strategy.kleinerOderGleich)));
        addItem(REZNUMMER_ENTHAELT);
        addItem(new CBModel("Name enth\u00e4lt", new OffenePostenTextFilter(KENNUNG)));
        addItem(new CBModel("Krankenkasse enth\u00e4lt", new OffenePostenTextFilter(KRANKENKASSENNAME)));
        setSelectedIndex(vorauswahl< getItemCount()?vorauswahl:0);
    }
}
final class CBModel {
    String anzeigeText="";
    OffenePostenAbstractRowFilter filter;

    public CBModel(String anzeige, OffenePostenAbstractRowFilter filter) {
        anzeigeText = anzeige;
        this.filter = filter;
    }


    @Override
    public String toString() {
        return anzeigeText;
    }
}
