package org.therapi.reha.patient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.DateTimeFormatters;
import patientenFenster.rezepte.RezeptFensterTools;
import rezept.Rezept;

public class RezeptHistTableModel extends AbstractTableModel {
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
    
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public int getRowCount() {
        logger.debug("GetRowCount=" + (rezeptListe == null ? 0 : rezeptListe.size()));
        for (Rezept rez : rezeptListe) {
            // logger.debug("RezNummer: " + rez.getRezNr());
        }
        return rezeptListe == null ? 0 : rezeptListe.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    /* override this in rezhisttable.java
    public Object getValueAt(int rowIndex, int columnIndex) {
        Rezept rezept = rezeptListe.get(rowIndex);
        String colName = columnNames[columnIndex];
        switch (colName) {
        case "Rezept-Nr.":
            return rezept.getRezNr();
        case "bezahlt":
            return bezahlt;
            //rezept.isRezBez(); // Would we ever have a non-paid Rezept in Historie?
        case "Rez-Datum":
            return rezept.getRezDatum();
        case "angelegt am":
            return rezept.getErfassungsDatum().format(DateTimeFormatters.ddMMYYYYmitPunkt);
        case "sp\u00e4t.Beginn":
            return RezeptFensterTools.calcLatestStartDate(rezept);
        case "Pat-Nr.":
            return rezept.getPatIntern();
        case "Indi.Schl.":
            return rezept.getIndikatSchl();
        case "ID":
            return rezept.getId();
        default:
            //should never happen

            logger.error("unknown column requested: [column = " + columnIndex + " ]");
            return new Object();
        }
    }
 */   
    public Object getValueAt(int rowIndex, int columnIndex) {
        Rezept rezept = rezeptListe.get(rowIndex);
        switch (columnIndex) {
            case HISTREZTABCOL_NR:
                return rezept.getRezNr();
            case HISTREZTABCOL_BEZAHLT:
                return bezahlt;
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
            return Icon.class;
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
    public DateCellRenderer() { super(); }

    @Override
    public void setValue(Object value) {
        setText((value == null) ? "" : ((LocalDate) value).format(DateTimeFormatters.ddMMYYYYmitPunkt));
    }
}