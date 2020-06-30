package org.therapi.reha.patient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.DateTimeFormatters;

public class RezeptHistTermTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RezeptHistTermTableModel.class);
    
    static final int HISTTERMTABCOL_BEHDAT = 0;
    static final int HISTTERMTABCOL_BEHDLER = 1;
    static final int HISTTERMTABCOL_TEXT = 2;
    static final int HISTTERMTABCOL_BEHART = 3;
    static final int HISTTERMTABCOL_SECRET = 4;
    private static final String[] columnNames = { "Beh.Datum", "Behandler", "Text", "Beh.Art", "" };
    
    private List<String> terminListe = null;
    private String[][] tableData;
    
    public RezeptHistTermTableModel(List<String> TerminListe) {
        logger.debug("Constr. TermListe=" + TerminListe.toString());
        terminListe = TerminListe;
        if (!TerminListe.isEmpty()) {
            tableData=getEinzelteile(TerminListe);
        } else {
            logger.debug("Passed in data was empty for constr.");
        }
    }
    
    public void updateTermine(String termine ) {
        if (termine == null) {
            terminListe.clear();
            return;
        }
        terminListe.clear();
        for(String termin : termine.split("\n")) {
            terminListe.add(termin);
        };
        
    }
    
    // 06.01.2020@VM@@54105@2020-01-06
    public Object getValueAt(int row, int col) {
        // String[] termEinzelteile = terminListe.get(row).split("@");
        if (terminListe == null)
            return null;
        String[] termEinzelteile = terminListe.get(row).split("@");
        // logger.debug("Got data: " + termEinzelteile);
        switch (col) {
            case HISTTERMTABCOL_BEHDAT:
                return LocalDate.parse(termEinzelteile[col], DateTimeFormatters.ddMMYYYYmitPunkt);
            case HISTTERMTABCOL_BEHDLER:
                return termEinzelteile[col];
            case HISTTERMTABCOL_TEXT:
                return termEinzelteile[col];
            case HISTTERMTABCOL_BEHART:
                return termEinzelteile[col];
            default:
                // Should never be reached, but - just in case:
                logger.error("Got columnindex " + col + " which could be outOfBounds");
                return LocalDate.parse(termEinzelteile[4], DateTimeFormatters.yyyyMMddmitBindestrich); // see what ya can do with this! :D
        }
    }
    
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    public int getRowCount() {
     // FIXME: Re-enable to see how often it gets called (WWAAYY TOO OFTEN!)
        // logger.debug("Term-GetRowCount=" + (terminListe == null ? 0 : terminListe.size()));
        return terminListe == null ? 0 : terminListe.size();
    }
    
    public int getColumnCount() {
        return columnNames.length;
    }
    
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case HISTTERMTABCOL_BEHDAT:
            return LocalDate.class;
        case HISTTERMTABCOL_BEHDLER:
            return String.class;
        case HISTTERMTABCOL_TEXT:
            return String.class;
        case HISTTERMTABCOL_BEHART:
            return String.class;
        default:
            //should never happen
            return super.getColumnClass(columnIndex);
        }
    }
    
    public void emptyTable() {
        terminListe.clear();
    }
    
    private String[][] getEinzelteile(List<String> TerminListe) {
        if (!TerminListe.isEmpty()) {
            ArrayList<String[]> tTD = new ArrayList<String[]>();
            for ( String termin : TerminListe) {
                String[] einzelteile = termin.split("@");
                tTD.add(einzelteile);
            }
            String[][] ret = (String[][]) tTD.toArray();
            return ret;
        } else {
            logger.debug("Passed in data was empty on getEinzelteile");
            return null;
        }

    }
}
/*
 * already in RezeptHistTableModel.java
 *
class DateCellRenderer extends DefaultTableCellRenderer {
    public DateCellRenderer() { super(); }

    @Override
    public void setValue(Object value) {
        setText((value == null) ? "" : ((LocalDate) value).format(DateTimeFormatters.ddMMYYYYmitPunkt));
    }
}
*/