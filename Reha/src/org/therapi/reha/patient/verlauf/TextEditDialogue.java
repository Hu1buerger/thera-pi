package org.therapi.reha.patient.verlauf;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

class TextEditDialogue extends JDialog {

    private JButton save;
    private JButton abort;
    private JTextArea textArea;
    private String originalText;

    public TextEditDialogue() {

        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 400));
        setLayout(new BorderLayout(0, 0));

        JToolBar toolBar = new JToolBar();
        add(toolBar, BorderLayout.NORTH);

        save = saveButton();
        toolBar.add(save);

        toolBar.addSeparator(new Dimension(40, 0));

        abort = abortButton();
        toolBar.add(abort);

        textArea = textArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

    }

    TextEditDialogue(Point p) {
        this();
        setLocation(p);
    }

    private JTextArea textArea() {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        return area;
    }

    private boolean setInitialContent(String text) {
        this.originalText = text;
        boolean wasempty = textArea.getText()
                                   .isEmpty();
        textArea.setText(text);
        return wasempty;
    }

    private void abort() {
        textArea.setText(originalText);
    }

    private void save() {
        dispose();
    }

    private JButton abortButton() {
        JButton abort = new JButton(Icons.forName("stop"));
        abort.setToolTipText("abbrechen");

        abort.addActionListener(e -> abort());
        return abort;
    }

    private JButton saveButton() {
        JButton save = new JButton(Icons.forName("save"));
        save.setToolTipText("speichern und schlie\u00dfen");

        save.addActionListener(e -> save());
        return save;
    }

    String show(String text) {
        setInitialContent(text);
        setVisible(true);

        return textArea.getText();
    }

}
