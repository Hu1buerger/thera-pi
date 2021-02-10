package org.therapi.reha.patient.verlauf;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;

public class VerlaufTableModel extends AbstractTableModel {

    List<Verlauf> verlaeufe = new ArrayList<Verlauf>();

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public int getRowCount() {
        return verlaeufe.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Verlauf rowValue = rowValue(rowIndex);
        switch (columnIndex) {
        case 0:
            return rowValue.documentedDay;
        case 1:
            return rowValue.text;
        case 2:
            return rowValue.therapist;
        case 3:
            return rowValue.documentator;

        default:
            return "";
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Verlauf rowValue = rowValue(rowIndex);

        switch (columnIndex) {

        case 0:
            rowValue.documentedDay = (LocalDate) aValue;
            break;
        case 1:
            rowValue.text = (String) aValue;
            break;
        case 2:
            rowValue.therapist = (String) aValue;
            break;
        case 3:
            rowValue.documentator = (String) aValue;
            break;

        default:
            ;
        }
    }

    Verlauf rowValue(int rowIndex) {
        return verlaeufe.get(rowIndex);
    }

    public int addRow(Verlauf verlauf) {
        int size = verlaeufe.size();
        verlaeufe.add(verlauf);

        fireTableRowsInserted(size, size);
        return size;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0:
            return LocalDate.class;
        case 1:
            return String.class;
        case 2:
            return String.class;
        case 3:
            return String.class;

        default:
            return String.class;
        }
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public String getColumnName(int column) {
        return getColumnNames()[column];
    }

    private String[] columnNames = { "BehandlungsDatum", "Verlauf", "Behandler", "Dokumentierer" };

    public void setData(List<Verlauf> verlaufliste) {
        verlaeufe = new ArrayList<>();
        verlaeufe.addAll(verlaufliste);
        fireTableDataChanged();

    }

    /**
     * retrieves a deep copy of underlying data.
     *
     * @return copied data
     */
    public List<Verlauf> verlaeufe() {

        return verlaeufe.stream()
                        .map(Verlauf::new)
                        .collect(Collectors.toList());
    }

}
