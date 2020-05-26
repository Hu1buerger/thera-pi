package org.therapi.reha.patient;

import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import CommonTools.DateTimeFormatters;
import rezept.Rezept;

public class RezeptTableModel extends AbstractTableModel {
    private static final Logger logger = LoggerFactory.getLogger(RezeptTableModel.class);
    private static final int NR = 0;
    private static final int BEZAHLT = 1;
    private static final int REZ_DATUM = 2;
    private static final int ANGELEGT_AM = 3;
    private static final int SPAETESTER_BEGINN = 4;
    private static final int STATUS = 5;
    private static final int INDI_SCHLUESSEL = 6;
    Icon bezahlt = new ImageIcon("icons/euro_red.png");
    private List<Rezept> rezeptListe;

    public RezeptTableModel(List<Rezept> rezeptListe) {
        this.rezeptListe = rezeptListe;
    }



    @Override
    public String getColumnName(int column) {
        switch (column) {
        case NR:
            return "Rezept-Nummer";
        case BEZAHLT:
            return "bezahlt";
        case REZ_DATUM:
            return "Rez-Datum";
        case ANGELEGT_AM:
            return "angelegt am";
        case SPAETESTER_BEGINN:
            return "spät. Beginn";
        case STATUS:
            return "Status";
        case INDI_SCHLUESSEL:
            return "Indi.Schlüssel";
        default:
           return super.getColumnName(column);
        }
    }


    @Override
    public int getRowCount() {
        return rezeptListe.size();
    }

    @Override
    public int getColumnCount() {

        return 7;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Rezept rezept = rezeptListe.get(rowIndex);
        switch (columnIndex) {
        case NR:
            return rezept.getRezNr();
        case BEZAHLT:
            return bezahlt;
            //rezept.isRezBez();
        case REZ_DATUM:
            return rezept.getRezDatum().format(DateTimeFormatters.ddMMYYYYmitPunkt);
        case ANGELEGT_AM:
            return rezept.getErfassungsDatum().format(DateTimeFormatters.ddMMYYYYmitPunkt);
        case SPAETESTER_BEGINN:
            return rezept.getLastDate().format(DateTimeFormatters.ddMMYYYYmitPunkt);
        case STATUS:
            return rezept.getZZStatus();
        case INDI_SCHLUESSEL:
            return rezept.getIndikatSchl();
        default:
            //should never happen

            logger.error("unknown column requested: [column = " + columnIndex);
            return new Object();
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case NR:
            return String.class;
        case BEZAHLT:
            return Icon.class;
        case REZ_DATUM:
            return String.class;
        case ANGELEGT_AM:
            return String.class;
        case SPAETESTER_BEGINN:
            return String.class;
        case STATUS:
            return Integer.class;
        case INDI_SCHLUESSEL:
            return String.class;
        default:
            //should never happen
            return super.getColumnClass(columnIndex);
        }
    }

}
