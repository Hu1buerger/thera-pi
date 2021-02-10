package org.therapi.reha.patient.verlauf;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JToolBar;

class VerlaufToolbar extends JToolBar {
    private JButton save;
    private JButton abort;

    VerlaufToolbar() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());

        save = saveButton();
        add(save);

        addSeparator(new Dimension(40, 0));

        abort = abortButton();
        add(abort);
    }

    private JButton abortButton() {
        JButton abort = new JButton(Icons.forName("stop"));
        abort.setBorder(BorderFactory.createEmptyBorder());
        abort.setToolTipText("bearbeiten abbrechen");

        return abort;
    }

    private JButton saveButton() {
        JButton save = new JButton(Icons.forName("save"));
        save.setToolTipText("speichern");
        save.setBorder(BorderFactory.createEmptyBorder());

        return save;
    }

    void addSaveActionListener(ActionListener listener) {
        save.addActionListener(listener);
    }

    void addAbortActionListener(ActionListener listener) {
        abort.addActionListener(listener);
    }
}
