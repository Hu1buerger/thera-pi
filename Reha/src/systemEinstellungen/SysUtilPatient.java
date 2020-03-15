package systemEinstellungen;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.INIFile;
import CommonTools.INITool;
import CommonTools.JRtaTextField;
import environment.Path;
import gui.Cursors;
import hauptFenster.Reha;

public class SysUtilPatient extends JXPanel
        implements KeyListener, ActionListener, CellEditorListener, SysInitCommon_If {

    JButton[] button = { null, null, null, null, null, null, null, null, null, null, null };
    JRtaTextField vorlage = null;
    JRtaTextField[] krit = { null, null, null, null, null, null };
    JRtaTextField[] icon = { null, null, null, null, null, null };
    // JXTable vorlagen = null;
    // MyDefaultTableModel defvorlagen = new MyDefaultTableModel();
    JRadioButton oben = null;
    JRadioButton unten = null;
    JCheckBox optimize = null;
    ButtonGroup bgroup = new ButtonGroup();
    JLabel datLabel = null;
    JLabel[] kritlab = { null, null, null, null, null, null };
    SysUtilVorlagen vorlagen = null;
    boolean formok = true;

    public SysUtilPatient() {
        super(new BorderLayout());
        // System.out.println("Aufruf SysUtilPatient");
        this.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 20));
        /****/
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                setBackgroundPainter(Reha.instance.compoundPainter.get("SystemInit"));
                return null;
            }

        }.execute();

        JScrollPane jscr = new JScrollPane();
        jscr.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        jscr.setOpaque(false);
        jscr.getViewport()
            .setOpaque(false);
        jscr.setViewportView(getVorlagenSeite());
        jscr.getVerticalScrollBar()
            .setUnitIncrement(15);
        jscr.validate();

        add(jscr, BorderLayout.CENTER);
//      add(getKnopfPanel(),BorderLayout.SOUTH);
        AbbruchOderSpeichern footer = new AbbruchOderSpeichern(this);
        this.add(footer.getPanel(), BorderLayout.SOUTH);
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                fuelleMitWerten();
                return null;
            }
        }.execute();

        return;
    }
    /*
     * private JPanel getKnopfPanel() {
     * 
     * button[9] = new JButton("abbrechen");
     * button[9].setActionCommand("abbrechen"); button[9].addActionListener(this);
     * button[10] = new JButton("speichern");
     * button[10].setActionCommand("speichern"); button[10].addActionListener(this);
     * 
     * // 1. 2. 3. 4. 5. 6. 7. 8. 9. FormLayout jpanlay = new
     * FormLayout("right:max(126dlu;p), 60dlu, 40dlu, 4dlu, 40dlu", // 1. 2. 3. 4.
     * 5. 6. 7. 8. 9. 10. 11. 12. 13. 14. 15. 16. 17. 18. 19. 20. 21. // 22. 23.
     * "p, 10dlu, p");
     * 
     * PanelBuilder jpan = new PanelBuilder(jpanlay); jpan.getPanel()
     * .setOpaque(false); CellConstraints jpancc = new CellConstraints();
     * 
     * jpan.addSeparator("", jpancc.xyw(1, 1, 5)); jpan.add(button[9], jpancc.xy(3,
     * 3)); jpan.add(button[10], jpancc.xy(5, 3));
     * jpan.addLabel("Änderungen übernehmen?", jpancc.xy(1, 3));
     * 
     * return jpan.getPanel(); }
     */

    private void fuelleMitWerten() {
        if (!formok) {
            return;
        }
        // hmContainer.put("Patient", inif.getIntegerProperty("Container", "StarteIn"));
        // hmContainer.put("PatientOpti", inif.getIntegerProperty("Container",
        // "ImmerOptimieren"));
        if (SystemConfig.hmContainer.get("Patient") == 0) {
            oben.setSelected(true);
        } else {
            unten.setSelected(true);
        }
        if (SystemConfig.hmContainer.get("PatientOpti") == 0) {
            optimize.setSelected(false);
        } else {
            optimize.setSelected(true);
        }

        vorlagen.readFromIni();
        INIFile inif = vorlagen.getInif();
        /*
         * INIFile inif = INITool.openIni(Path.Instance.getProghome() + "ini/" +
         * Reha.getAktIK() + "/", "patient.ini"); int forms =
         * inif.getIntegerProperty("Formulare", "PatientFormulareAnzahl");
         * Vector<String> vec = new Vector<String>(); for (int i = 1; i <= forms; i++) {
         * vec.clear(); vec.add(inif.getStringProperty("Formulare", "PFormularText" +
         * i)); vec.add(inif.getStringProperty("Formulare", "PFormularName" + i));
         * defvorlagen.addRow((Vector) vec.clone()); } if (defvorlagen.getRowCount() >
         * 0) { vorlagen.setRowSelectionInterval(0, 0); } vorlagen.validate();
         * 
         * for (int i = 0; i < 9; i++) { button[i].addActionListener(this); }
         */
        for (int i = 0; i < 6; i++) {
            krit[i].setText(SystemConfig.vPatMerker.get(i));
            String sico = "";
            if (SystemConfig.vPatMerkerIcon.get(i) == null) {
                sico = "";
            } else {
                sico = inif.getStringProperty("Kriterien", "Image" + (i + 1)); // Name aus .ini lesen
                                                                               // (vPatMerkerIconFile enthält Pfad)
                kritlab[i].setIcon(SystemConfig.vPatMerkerIcon.get(i));
            }
            icon[i].setText(sico);
            icon[i].setEditable(false);
        }
        for (int i = 0; i < 9; i++) {
            button[i].addActionListener(this);
        }
    }

    /**************
     * Beginn der Methode f�r die Objekterstellung und -platzierung
     *********/
    private JPanel getVorlagenSeite() {

        oben = new JRadioButton();
        bgroup.add(oben);
        unten = new JRadioButton();
        bgroup.add(unten);
        optimize = new JCheckBox();
        /*
         * defvorlagen.setColumnIdentifiers(new String[] { "Titel der Vorlage",
         * "Vorlagendatei" }); vorlagen = new JXTable(defvorlagen);
         * vorlagen.addMouseListener(new MouseAdapter() {
         * 
         * @Override public void mouseClicked(MouseEvent arg0) {
         * 
         * if (arg0.getClickCount() == 2 && arg0.getButton() == 1) { int row =
         * vorlagen.getSelectedRow(); row = vorlagen.convertRowIndexToModel(row); int
         * col = vorlagen.getSelectedColumn(); if (col == 1) {
         * setCursor(Cursors.wartenCursor); String svorlage =
         * dateiDialog(Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK()); if
         * (svorlage.equals("")) { return; } defvorlagen.setValueAt(svorlage, row, col);
         * vorlagen.validate(); } } } });
         * 
         * // vorlagen.setDefaultEditor(new Object().getClass(), new MyEditor());
         * vorlagen.getColumn(0) .setCellEditor(new TitelEditor());
         * vorlagen.getColumn(0) .getCellEditor() .addCellEditorListener(this); vorlage
         * = new JRtaTextField("NIX", true); button[6] = new JButton("entfernen");
         * button[6].setActionCommand("entfernen");
         */
        vorlagen = new SysUtilVorlagen(this);
        vorlagen.setVPfad(Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK());
        vorlagen.setIni(Path.Instance.getProghome() + "ini/" + Reha.getAktIK(), "patient.ini");
        vorlagen.setLabels("Formulare", "PatientFormulareAnzahl", "PFormular");
        vorlagen.activateEditing();

//        button[7] = new JButton("auswählen");
//        button[7].setActionCommand("vorlagenwahl");
//        button[8] = new JButton("hinzufügen");
//        button[8].setActionCommand("vorlagenneu");

        krit[0] = new JRtaTextField("", true);
        krit[1] = new JRtaTextField("", true);
        krit[2] = new JRtaTextField("", true);
        krit[3] = new JRtaTextField("", true);
        krit[4] = new JRtaTextField("", true);
        krit[5] = new JRtaTextField("", true);
        button[0] = new JButton("auswählen");
        button[0].setActionCommand("iwahl0");
        button[1] = new JButton("auswählen");
        button[1].setActionCommand("iwahl1");
        button[2] = new JButton("auswählen");
        button[2].setActionCommand("iwahl2");
        button[3] = new JButton("auswählen");
        button[3].setActionCommand("iwahl3");
        button[4] = new JButton("auswählen");
        button[4].setActionCommand("iwahl4");
        button[5] = new JButton("auswählen");
        button[5].setActionCommand("iwahl5");
        icon[0] = new JRtaTextField("", true);
        icon[1] = new JRtaTextField("", true);
        icon[2] = new JRtaTextField("", true);
        icon[3] = new JRtaTextField("", true);
        icon[4] = new JRtaTextField("", true);
        icon[5] = new JRtaTextField("", true);
        kritlab[0] = new JLabel("1. Icon");
        kritlab[1] = new JLabel("2. Icon");
        kritlab[2] = new JLabel("3. Icon");
        kritlab[3] = new JLabel("4. Icon");
        kritlab[4] = new JLabel("5. Icon");
        kritlab[5] = new JLabel("6. Icon");
        // 1. 2. 3. 4. 5. 6. 7. 8. 9.
        // FormLayout lay = new FormLayout("right:max(120dlu;p), 20dlu, 40dlu, 40dlu,
        // 4dlu, 40dlu,0dlu",
        // 1. 2. 3. 4. 5. 6. 7.
        FormLayout lay = new FormLayout("right:max(120dlu;p), 15dlu, 40dlu:g, 4dlu, 45dlu, 4dlu, 10dlu, 15dlu",
                // 1. 2. 3. 4. 5. 6. 7. 8. 9. 10. 11. 12. 13. 14. 15. 16. 17. 18. 19.
                "p, 2dlu, p, 10dlu, p, 10dlu, p, 2dlu, p, 10dlu, p, 2dlu , p, 5dlu, p, 2dlu, p, 5dlu,p, 2dlu, p, "
                        // 20. 21. 22. 23. 24 25 26 27 28 29 30 31 32
                        + "5dlu, p, 2dlu, p, 5dlu, p, 2dlu, p, 5dlu, p, 2dlu, p, 10dlu");
        PanelBuilder builder = new PanelBuilder(lay);
        // PanelBuilder builder = new PanelBuilder(lay, new FormDebugPanel()); // debug
        // mode

        builder.setDefaultDialogBorder();
        builder.getPanel()
               .setOpaque(false);
        CellConstraints cc = new CellConstraints();
        int rowCnt = 1;

        builder.addLabel("Fenster startet im ...", cc.xy(1, rowCnt, CellConstraints.LEFT, CellConstraints.BOTTOM));
        builder.addLabel((SystemConfig.desktopHorizontal ? "oberen" : "linken") + " Container",
                cc.xyw(4, rowCnt, 2, CellConstraints.RIGHT, CellConstraints.BOTTOM));
        builder.add(oben, cc.xy(7, rowCnt++, CellConstraints.RIGHT, CellConstraints.BOTTOM));
        builder.addLabel((SystemConfig.desktopHorizontal ? "unteren" : "rechten") + " Container",
                cc.xyw(4, ++rowCnt, 2, CellConstraints.RIGHT, CellConstraints.BOTTOM));
        builder.add(unten, cc.xy(7, rowCnt++, CellConstraints.RIGHT, CellConstraints.BOTTOM));
        builder.addLabel("Fenstergröße automatisch optimieren", cc.xy(1, ++rowCnt));
        builder.add(optimize, cc.xy(7, rowCnt++, CellConstraints.RIGHT, CellConstraints.BOTTOM));

        builder.add(vorlagen.getPanel(), cc.xyw(1, ++rowCnt, 8));
        /*
         * builder.addSeparator("Vorlagen-Verwaltung", cc.xyw(1,7,7)); JScrollPane jscr
         * = JCompTools.getTransparentScrollPane(vorlagen); jscr.validate();
         * builder.add(jscr, cc.xyw(1,9,7)); builder.addLabel("aus Liste entfernen",
         * cc.xy(1, 11)); builder.add(button[6], cc.xyw(5, 11, 3));
         * //builder.addLabel("Titel der neuen Vorlagen", cc.xy(1, 13));
         * //builder.add(vorlage, cc.xyw(3,13,4)); //builder.addLabel("Datei ausw�hlen",
         * cc.xy(1, 15)); datLabel = new JLabel(); //
         * datLabel.setForeground(Color.BLUE); // builder.add(datLabel, cc.xyw(3, 15,
         * 2)); // builder.add(button[7], cc.xy(6, 15));
         * builder.addLabel("neue Vorlagendatei hinzufügen", cc.xy(1, 17));
         * builder.add(button[8], cc.xyw(5, 17, 3));
         */
        rowCnt = 9;
        builder.addSeparator("Kriteriendefinitionen / Icons", cc.xyw(1, rowCnt++, 7));
        builder.addLabel("1. Kriterium", cc.xy(1, ++rowCnt));
        builder.add(krit[0], cc.xyw(3, rowCnt++, 5));
        builder.add(kritlab[0], cc.xy(1, ++rowCnt));
        builder.add(icon[0], cc.xy(3, rowCnt));
        builder.add(button[0], cc.xyw(5, rowCnt++, 3));

        builder.addLabel("2. Kriterium", cc.xy(1, ++rowCnt));
        builder.add(krit[1], cc.xyw(3, rowCnt++, 5));
        builder.add(kritlab[1], cc.xy(1, ++rowCnt));
        builder.add(icon[1], cc.xy(3, rowCnt));
        builder.add(button[1], cc.xyw(5, rowCnt++, 3));

        builder.addLabel("3. Kriterium", cc.xy(1, ++rowCnt));
        builder.add(krit[2], cc.xyw(3, rowCnt++, 5));
        builder.add(kritlab[2], cc.xy(1, ++rowCnt));
        builder.add(icon[2], cc.xy(3, rowCnt));
        builder.add(button[2], cc.xyw(5, rowCnt++, 3));

        builder.addLabel("4. Kriterium", cc.xy(1, ++rowCnt));
        builder.add(krit[3], cc.xyw(3, rowCnt++, 5));
        builder.add(kritlab[3], cc.xy(1, ++rowCnt));
        builder.add(icon[3], cc.xy(3, rowCnt));
        builder.add(button[3], cc.xyw(5, rowCnt++, 3));

        builder.addLabel("5. Kriterium", cc.xy(1, ++rowCnt));
        builder.add(krit[4], cc.xyw(3, rowCnt++, 5));
        builder.add(kritlab[4], cc.xy(1, ++rowCnt));
        builder.add(icon[4], cc.xy(3, rowCnt));
        builder.add(button[4], cc.xyw(5, rowCnt++, 3));

        builder.addLabel("6. Kriterium", cc.xy(1, ++rowCnt));
        builder.add(krit[5], cc.xyw(3, rowCnt++, 5));
        builder.add(kritlab[5], cc.xy(1, ++rowCnt));
        builder.add(icon[5], cc.xy(3, rowCnt));
        builder.add(button[5], cc.xyw(5, rowCnt++, 3));

        return builder.getPanel();
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String cmd = e.getActionCommand();
        for (int i = 0; i < 1; i++) {
            /*
             * if (cmd.equals("entfernen")) { int row = vorlagen.getSelectedRow(); int frage
             * = JOptionPane.showConfirmDialog(null,
             * "Wollen Sie die ausgewählte Tabellenzeile wirklich löschen?",
             * "Wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION); if (frage ==
             * JOptionPane.NO_OPTION) { return; } if (row >= 0) {
             * TableTool.loescheRow(vorlagen, row); } break; } if
             * (cmd.equals("vorlagenwahl")) { setCursor(Cursors.wartenCursor); String
             * svorlage = dateiDialog(Path.Instance.getProghome() + "vorlagen/" +
             * Reha.getAktIK()); if (!svorlage.equals("")) { datLabel.setText(svorlage); }
             * else { datLabel.setText(""); } break; } if (cmd.equals("vorlagenneu")) {
             * 
             * setCursor(Cursors.wartenCursor); String svorlage =
             * dateiDialog(Path.Instance.getProghome() + "vorlagen/" + Reha.getAktIK()); if
             * (!svorlage.equals("")) { datLabel.setText(svorlage); } else {
             * datLabel.setText(""); break; }
             * 
             * if (vorlage.getText() .equals("") || datLabel.getText() .equals("")) {
             * JOptionPane.showMessageDialog(null,
             * "Geben Sie jetzt einen Titel für die neue Text-Vorlage ein"); // return; }
             * Vector vec = new Vector(); vec.add(""); vec.add(datLabel.getText());
             * defvorlagen.addRow((Vector) vec.clone()); vorlagen.validate(); int rows =
             * defvorlagen.getRowCount();
             * 
             * // vorlagen.setCellSelectionEnabled(true); final int xrows = rows - 1;
             * SwingUtilities.invokeLater(new Runnable() {
             * 
             * @Override public void run() { vorlagen.requestFocus();
             * vorlagen.setRowSelectionInterval(xrows, xrows); startCellEditing(xrows); }
             * });
             * 
             * // vorlage.setText(""); // datLabel.setText(""); break; }
             */
            if (cmd.contains("iwahl")) {
                int wahl = Integer.valueOf(cmd.substring(cmd.length() - 1));
                setCursor(Cursors.wartenCursor);
                String sicon = dateiDialog(Path.Instance.getProghome() + "icons/");
                if (!sicon.equals("")) {
                    icon[wahl].setText(sicon);
                    kritlab[wahl].setIcon(new ImageIcon(Path.Instance.getProghome() + "icons/" + sicon));
                } else {
                    icon[wahl].setText("");
                    kritlab[wahl].setIcon(null);
                }
                break;
            }
        }
    }

    /*
     * private void startCellEditing(int row) { final int xrows = row;
     * SwingUtilities.invokeLater(new Runnable() {
     * 
     * @Override public void run() { vorlagen.editCellAt(xrows, 0); } }); }
     */
    private void doSpeichern() {
        try {
            String wert = "";
            INIFile inif = vorlagen.getInif();
            // INIFile inif = INITool.openIni(Path.Instance.getProghome() + "ini/" +
            // Reha.getAktIK() + "/", "patient.ini");
            // System.out.println(Path.Instance.getProghome()+"ini/"+Reha.getAktIK()+"/patient.ini");
            wert = (unten.isSelected() ? "1" : "0");
            SystemConfig.hmContainer.put("Patient", Integer.valueOf(wert));
            inif.setStringProperty("Container", "StarteIn", wert, null);

            wert = (optimize.isSelected() ? "1" : "0");
            SystemConfig.hmContainer.put("PatientOpti", Integer.valueOf(wert));
            inif.setStringProperty("Container", "ImmerOptimieren", wert, null);

            formok = vorlagen.saveToIni();
            /*
             * int rows = vorlagen.getRowCount();
             * 
             * formok = true; for (int i = 0; i < rows; i++) { String test = (String)
             * vorlagen.getValueAt(i, 0); if (test.equals("")) { String datei = (String)
             * vorlagen.getValueAt(i, 1); String msg = "Für Vorlagendatei " + datei +
             * " wurde kein Titel eingegeben!\nDie Vorlagen werden nicht(!!!) gespeichert.";
             * JOptionPane.showMessageDialog(null, msg); formok = false; break; } else {
             * formok = true; inif.setStringProperty("Formulare", "PatientFormulareAnzahl",
             * Integer.valueOf(rows) .toString(), null); } } if (formok) { for (int i = 0; i
             * < rows; i++) { inif.setStringProperty("Formulare", "PFormularText" + (i + 1),
             * (String) vorlagen.getValueAt(i, 0), null);
             * inif.setStringProperty("Formulare", "PFormularName" + (i + 1), (String)
             * vorlagen.getValueAt(i, 1), null); } }
             */
            for (int i = 0; i < 6; i++) {
                wert = krit[i].getText();
                inif.setStringProperty("Kriterien", "Krit" + (i + 1), wert, null);
                SystemConfig.vPatMerker.set(i, wert);

                wert = icon[i].getText();
                inif.setStringProperty("Kriterien", "Image" + (i + 1), icon[i].getText(), null);
                SystemConfig.vPatMerkerIcon.set(i,
                        (wert.equals("") ? null : new ImageIcon(Path.Instance.getProghome() + "icons/" + wert)));
                SystemConfig.vPatMerkerIconFile.set(i,
                        (wert.equals("") ? null : Path.Instance.getProghome() + "icons/" + wert));

            }
            INITool.saveIni(inif);
            JOptionPane.showMessageDialog(null, "Konfiguration erfolgrein in patient.ini gespeichert.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Fehler beim speichern der Konfiguration in patient.ini!!!!");
        }
    }

    private String dateiDialog(String pfad) {
        String sret = "";
        final JFileChooser chooser = new JFileChooser("Verzeichnis wählen");
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        final File file = new File(pfad);

        chooser.setCurrentDirectory(file);

        chooser.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName()
                     .equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)
                        || e.getPropertyName()
                            .equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                    final File f = (File) e.getNewValue();
                }
            }
        });
        chooser.setVisible(true);
        setCursor(Cursors.normalCursor);
        final int result = chooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File inputVerzFile = chooser.getSelectedFile();
            // String inputVerzStr = inputVerzFile.getPath();

            if (inputVerzFile.getName()
                             .trim()
                             .equals("")) {
                sret = "";
            } else {
                sret = inputVerzFile.getName()
                                    .trim();
            }
        } else {
            sret = ""; // vorlagenname.setText(SystemConfig.oTerminListe.NameTemplate);
        }
        chooser.setVisible(false);

        return sret;
    }

    /*
     * class MyDefaultTableModel extends DefaultTableModel { /**
     * 
     */ /*
         * private static final long serialVersionUID = 1L;
         * 
         * @Override public Class getColumnClass(int columnIndex) { return String.class;
         * }
         * 
         * @Override public boolean isCellEditable(int row, int col) { if (col == 0) {
         * return true; } else { return false; } }
         * 
         * }
         */
    /**********************************************/
    class TitelEditor extends AbstractCellEditor implements TableCellEditor {
        Object value;
        JComponent component = new JFormattedTextField();

        public TitelEditor() {
            // component = new JRtaTextField("NIX",true);
            // System.out.println("editor-Component wurde initialisiert");
            component.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent arg0) {
                    //// System.out.println("********Button in KeyPressed*********");
                    if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                        arg0.consume();
                        stopCellEditing();
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            ((JFormattedTextField) component).setText((String) value);
            ((JFormattedTextField) component).requestFocus();
            ((JFormattedTextField) component).setCaretPosition(0);
            return component;
        }

        @Override
        public Object getCellEditorValue() {

            return ((JFormattedTextField) component).getText();
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            if (anEvent instanceof MouseEvent) {
                MouseEvent me = (MouseEvent) anEvent;
                if (me.getClickCount() != 2) {
                    return false;
                }
            }
            // System.out.println("isCellEditable");
            return true;
        }

        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            // System.out.println("in schouldCellSelect"+anEvent);
            return super.shouldSelectCell(anEvent);
        }

        @Override
        public boolean stopCellEditing() {
            value = ((JFormattedTextField) component).getText();
            // System.out.println("in stopCellediting");
            super.stopCellEditing();
            return true;
        }

        public boolean startCellEditing() {
            return false;// super.startCellEditing();//false;
        }

    }

    @Override
    public void editingCanceled(ChangeEvent arg0) {

        // System.out.println("In Hauptprogramm-Listener editingCanceled");
    }

    @Override
    public void editingStopped(ChangeEvent arg0) {

        // System.out.println("In Hauptprogramm-Listener editingStopped");
    }

    @Override
    public void Abbruch() {
        SystemInit.abbrechen();
    }

    @Override
    public void Speichern() {
        doSpeichern();
    }

    @Override
    public void AddEntry(int instanceNb) {
        // TODO Auto-generated method stub
    }

    @Override
    public void RemoveEntry(int instanceNb) {
        // TODO Auto-generated method stub
    }

}
