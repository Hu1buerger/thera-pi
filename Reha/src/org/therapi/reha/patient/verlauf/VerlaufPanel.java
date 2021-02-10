package org.therapi.reha.patient.verlauf;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jdesktop.swingx.JXPanel;

import hauptFenster.CompoundPainterMap;

class VerlaufPanel extends JXPanel {
    private JTable table;
    private VerlaufTableModel dataModel;
    private VerlaufToolbar toolbar;

    public VerlaufPanel() {

        setLayout(new BorderLayout(0, 0));
        setBackgroundPainter(new CompoundPainterMap().forName("getTabs2"));
        toolbar = new VerlaufToolbar();

        add(toolbar, BorderLayout.NORTH);

        dataModel = new VerlaufTableModel();
        table = new VerlaufTable(dataModel);

        MouseListener listener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2)
                    return;
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1)
                    return;

                showEditDialog(selectedRow, e.getLocationOnScreen());

            }

            private void showEditDialog(int selectedRow, Point locationOnScreen) {
                Verlauf verlauf = dataModel.rowValue(table.convertRowIndexToModel(selectedRow));
                TextEditDialogue verlaufEditDialogue = new TextEditDialogue(locationOnScreen);
                verlaufEditDialogue.setTitle(
                        verlauf.dayofDocumentation.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                verlauf.text = verlaufEditDialogue.show(verlauf.text);

                dataModel.fireTableRowsUpdated(selectedRow, selectedRow);
            }
        };
        table.addMouseListener(listener);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

    }

    public List<Verlauf> verlaeufe() {
        return dataModel.verlaeufe();

    }

    public void setVerlaufListe(List<Verlauf> verlaufliste) {
        dataModel.setData(verlaufliste);
    }

    void addSaveActionListener(ActionListener listener) {
        toolbar.addSaveActionListener(listener);
    }

    void addAbortActionListener(ActionListener listener) {
        toolbar.addAbortActionListener(listener);
    }

}
