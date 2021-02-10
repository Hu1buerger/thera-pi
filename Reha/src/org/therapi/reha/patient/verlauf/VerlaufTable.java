package org.therapi.reha.patient.verlauf;

import java.awt.Color;

import org.jdesktop.swingx.JXTable;

final class VerlaufTable extends JXTable {

    public VerlaufTable(VerlaufTableModel dataModel) {
        setOpaque(false);
        setModel(dataModel);
        setAutoCreateRowSorter(true);
        getRowSorter().toggleSortOrder(0);
        setBackground(Color.WHITE);
        getTableHeader().setBackground(Color.WHITE);
        getColumn("BehandlungsDatum").setPreferredWidth(80);
        getColumn("Verlauf").setPreferredWidth(400);

        getColumn("Behandler").setPreferredWidth(80);
        getColumn("Dokumentierer").setPreferredWidth(80);
    }
}
