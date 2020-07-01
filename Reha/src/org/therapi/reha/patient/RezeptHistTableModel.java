package org.therapi.reha.patient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.DateTimeFormatters;
import patientenFenster.rezepte.RezeptFensterTools;
import rezept.Rezept;
import stammDatenTools.ZuzahlTools.ZZStat;

public class RezeptHistTableModel extends AbstractTableModel {
    static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RezeptHistTableModel.class);
    static final int HISTREZTABCOL_NR = 0;
    static final int HISTREZTABCOL_BEZAHLT = 1;
    static final int HISTREZTABCOL_REZDAT = 2;
    static final int HISTREZTABCOL_ANGELEGTAM = 3;
    static final int HISTREZTABCOL_SPAETESTERBEGINN = 4;
    static final int HISTREZTABCOL_PATNR = 5;
    static final int HISTREZTABCOL_INDISCHL = 6;
    static final int HISTREZTABCOL_ID=7;
    
    private static final String[] columnNames = { "Rezept-Nr.", "bezahlt", "Rez-Datum", "angelegt am",
                                                  "sp\u00e4t.Beginn", "Pat-Nr.", "Indi.Schl.", "ID" };
    
    Icon bezahlt = new ImageIcon("icons/euro_red.png");
    private List<Rezept> rezeptListe;

    public RezeptHistTableModel(List<Rezept> RezeptListe) {
        logger.debug("Constr. RezListe=" + RezeptListe.toString());
        rezeptListe = (RezeptListe == null ? new ArrayList<Rezept>() : RezeptListe);
    }

    public RezeptHistTableModel() {
        // Empty until I can figure out what is needed...
        rezeptListe = new ArrayList<Rezept>();
    }
    
    public void emptyTable() {
        if (rezeptListe != null ) {
            rezeptListe.clear();
        } else {
            logger.debug("rezeptListe was null - nothing to empty...");
        }
    }

    public void addRow(Rezept rez) {
        logger.debug("Adding hist rez: " + rez);
        if (rezeptListe == null) {
            rezeptListe = new ArrayList<Rezept>();
        }
        rezeptListe.add(rez);
    }
    
    public void removeRow(int row) {
        rezeptListe.remove(row);
    }
    
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public int getRowCount() {
        // FIXME: Re-enable to see how often it gets called (WWAAYY TOO OFTEN!)
        // logger.debug("GetRowCount=" + (rezeptListe == null ? 0 : rezeptListe.size()));
        for (Rezept rez : rezeptListe) {
            // logger.debug("RezNummer: " + rez.getRezNr());
        }
        return rezeptListe == null ? 0 : rezeptListe.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Rezept rezept = rezeptListe.get(rowIndex);
        switch (columnIndex) {
            case HISTREZTABCOL_NR:
                return rezept.getRezNr();
            case HISTREZTABCOL_BEZAHLT:
                logger.debug("Icon from " + rezept.getZZStatus() + " " + rezept.getRezNr());
                ZZStat iconKey = stammDatenTools.ZuzahlTools.getIconKey(rezept.getZZStatus(), rezept.getRezNr());
                return stammDatenTools.ZuzahlTools.getZzIcon(iconKey);
                //rezept.isRezBez(); // Would we ever have a non-paid Rezept in Historie?
            case HISTREZTABCOL_REZDAT:
                return rezept.getRezDatum();
            case HISTREZTABCOL_ANGELEGTAM:
                return rezept.getErfassungsDatum();
            case HISTREZTABCOL_SPAETESTERBEGINN:
                return RezeptFensterTools.calcLatestStartDate(rezept);
            case HISTREZTABCOL_PATNR:
                return rezept.getPatIntern();
            case HISTREZTABCOL_INDISCHL:
                return rezept.getIndikatSchl();
            case HISTREZTABCOL_ID:
                return rezept.getId();
            default:
                //should never happen
    
                logger.error("unknown column requested: [column = " + columnIndex + " ]");
                return new Object();
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case HISTREZTABCOL_NR:
            return String.class;
        case HISTREZTABCOL_BEZAHLT:
            return JLabel.class;
        case HISTREZTABCOL_REZDAT:
            return LocalDate.class;
        case HISTREZTABCOL_ANGELEGTAM:
            return LocalDate.class;
        case HISTREZTABCOL_SPAETESTERBEGINN:
            return LocalDate.class;
        case HISTREZTABCOL_PATNR:
            return int.class;
        case HISTREZTABCOL_INDISCHL:
            return String.class;
        case HISTREZTABCOL_ID:
            return int.class;
        default:
            //should never happen
            return super.getColumnClass(columnIndex);
        }
    }

}

class DateCellRenderer extends DefaultTableCellRenderer {
    static final long serialVersionUID = 1L;

    public DateCellRenderer() { super(); setHorizontalAlignment(JLabel.CENTER);}

    @Override
    public void setValue(Object value) {
        setText((value == null) ? "" : ((LocalDate) value).format(DateTimeFormatters.ddMMYYYYmitPunkt));
    }
}