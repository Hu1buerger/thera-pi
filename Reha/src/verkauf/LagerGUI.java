package verkauf;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.DoubleTableCellRenderer;
import CommonTools.JCompTools;
import CommonTools.JRtaTextField;
import verkauf.model.Artikel;
import verkauf.model.Lieferant;

public class LagerGUI extends JXPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private VerkaufTab owner;
    private DefaultTableModel lgmod;
    private JXTable lgtab;
    private JScrollPane jscr;
    private Vector<String> columns;
    private ArtikelDialog adlg;
    private JRtaTextField sucheText = new JRtaTextField("nix", false);

    LagerGUI(VerkaufTab owner) {
        super();
        this.owner = owner;

        columns = new Vector<String>();

        columns.add("Artikel-ID");
        columns.add("Beschreibung");
        columns.add("VK-Preis");
        columns.add("EK-Preis");
        columns.add("MWSt");
        columns.add("Lieferant");
        columns.add("Lagerstand");
        columns.add("");

        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        this.add(getContent(), BorderLayout.CENTER);
        this.setLastRowSelected();

    }

    private JXPanel getContent() {
        JXPanel pane = new JXPanel();
        pane.setOpaque(false);

        String xwerte = "5dlu, p:g, 5dlu";
        String ywerte = "5dlu, p, 5dlu, p:g, 5dlu";

        FormLayout lay = new FormLayout(xwerte, ywerte);
        CellConstraints cc = new CellConstraints();
        pane.setLayout(lay);

        pane.add(this.owner.getToolbar(sucheText), cc.xy(2, 2));

        lgmod = new DefaultTableModel();
        lgmod.setColumnIdentifiers(columns);
        lgtab = new JXTable(lgmod);
        lgtab.getColumn(7)
             .setMinWidth(0); // \
        lgtab.getColumn(7)
             .setMaxWidth(0); // /verkartikelID ausblenden
        lgtab.setEditable(false);
        lgtab.getColumn(2)
             .setCellRenderer(new DoubleTableCellRenderer());
        lgtab.getColumn(3)
             .setCellRenderer(new DoubleTableCellRenderer());
        lgtab.getColumn(4)
             .setCellRenderer(new DoubleTableCellRenderer());
        lgtab.getColumn(6)
             .setCellRenderer(new DoubleTableCellRenderer());

        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                setzeTabDaten(Artikel.liefereArtikelDaten());
                return null;
            }

        }.execute();

        lgtab.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                if (arg0.getClickCount() == 2) {
                    owner.aktiviereFunktion(VerkaufTab.edit);
                }
            }
        });
        jscr = JCompTools.getTransparentScrollPane(lgtab);
        jscr.validate();
        pane.add(jscr, cc.xy(2, 4, CellConstraints.FILL, CellConstraints.FILL));

        pane.validate();
        return pane;
    }

    public void aktiviereFunktion(int befehl) {
        if (befehl == VerkaufTab.neu) {
            doArtikelDialog(-1);
            this.setzeTabDaten(Artikel.liefereArtikelDaten());
            // this.setLastRowSelected();
            this.selectNewestRow();
        } else if (befehl == VerkaufTab.edit) {
            if (this.lgtab.getSelectedRow() >= 0) {
                int currRow = this.lgtab.getSelectedRow();
                int realRow = this.lgtab.convertRowIndexToModel(currRow); // (echter) Zeilen-Idx im Model (falls Tabelle
                                                                          // umsortiert ist)
                int id = Integer.parseInt((String) this.lgmod.getValueAt(realRow, this.lgmod.getColumnCount() - 1));
                doArtikelDialog(id);
                this.setzeZeileNeu(id);
                this.lgtab.setRowSelectionInterval(currRow, currRow);
            } else {
                JOptionPane.showMessageDialog(null, "Wen oder was willst du ändern?");
            }
        } else if (befehl == VerkaufTab.delete) {
            int currRow = this.lgtab.getSelectedRow(); // aktuell markierte Zeile merken
            int realRow = this.lgtab.convertRowIndexToModel(currRow); // (echter) Zeilen-Idx im Model (falls Tabelle
                                                                      // umsortiert ist)
            if (realRow >= 0) {
                String currArtID = (String) this.lgmod.getValueAt(realRow, 0);
                currArtID = (String) this.lgmod.getValueAt(realRow, this.lgmod.getColumnCount() - 1);
                Artikel.loescheArtikel(
                        Integer.parseInt((String) this.lgmod.getValueAt(realRow, this.lgmod.getColumnCount() - 1)));
                this.loescheZeile(realRow);
                this.setRowSelected(currRow);
            } else {
                JOptionPane.showMessageDialog(null, "Wen oder was willst du löschen?");
            }
        } else if (befehl == VerkaufTab.suche) {
            this.setzeTabDaten(Artikel.sucheArtikelDaten(this.sucheText.getText()));
            this.setLastRowSelected();
        } else if (befehl == VerkaufTab.reload) {
            new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    setzeTabDaten(Artikel.liefereArtikelDaten());
                    setLastRowSelected();
                    return null;
                }

            }.execute();
        }
    }

    private void setzeZeileNeu(final int id) {
        final Artikel a = new Artikel(id);
        final DecimalFormat df = new DecimalFormat("0.00");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int n = 0; n < lgtab.getRowCount(); n++) {
                    // System.out.println("Bin in Zeile " + n + " Suche " + id + " finde: " +
                    // Integer.parseInt((String)lgmod.getValueAt(n, lgmod.getColumnCount()-1)));
                    if (id == Integer.parseInt((String) lgmod.getValueAt(n, lgmod.getColumnCount() - 1))) {
                        lgmod.setValueAt(a.getEan(), n, 0);
                        lgmod.setValueAt(a.getBeschreibung(), n, 1);
                        lgmod.setValueAt(df.format(a.getPreis()), n, 2);
                        lgmod.setValueAt(df.format(a.getEinkaufspreis()), n, 3);
                        lgmod.setValueAt(df.format(MwSTSatz.now( a.getMwst())), n, 4);
                        lgmod.setValueAt(new Lieferant(a.getLieferant()).toString(), n, 5);
                        lgmod.setValueAt(df.format(a.getLagerstand()), n, 6);
                        lgmod.setValueAt(String.valueOf(a.id), n, lgmod.getColumnCount() - 1);
                    }
                }
                return null;
            }
        }.execute();
    }

    private void loescheZeile(int row) {
        lgmod.removeRow(row);
    }

    private void setzeTabDaten(Vector<Vector<String>> daten) {
        final Vector<Vector<String>> d = daten;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                lgmod.setRowCount(0);
                for (int i = 0; i < d.size(); i++) {
                    lgmod.addRow(d.get(i));
                }
                lgtab.packColumn(1, 5);
                lgtab.repaint();
                return null;
            }
        }.execute();
    }

    private void doArtikelDialog(int id) {
        adlg = new ArtikelDialog(id, this.owner.holePosition(300, 300));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                adlg.setzeFocus();
            }
        });
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    long eintritt = System.currentTimeMillis();
                    while (!adlg.getTextField()
                                .hasFocus()) {
                        adlg.setzeFocus();
                        Thread.sleep(25);
                        if (System.currentTimeMillis() - eintritt > 5000) {
                            System.out.println("Zwangsausbruch aus doArtikelDialog");
                            break;
                        }
                        // System.out.println("erzwinge Focus");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }
        }.execute();
        adlg.setModal(true);
        adlg.setVisible(true);
        adlg = null;
    }

    private void setLastRowSelected() {
        if (this.lgmod.getRowCount() > 0) {
            this.lgtab.setRowSelectionInterval(this.lgmod.getRowCount() - 1, this.lgmod.getRowCount() - 1);
        }
    }

    private void setRowSelected(int currRow) {
        if (this.lgmod.getRowCount() > currRow) {
            this.lgtab.setRowSelectionInterval(currRow, currRow);
        } else {
            this.setLastRowSelected();
        }
    }

    private void selectNewestRow() {
        String lastOne = Artikel.getNeuesteArtikelID();
        for (int n = 0; n < lgtab.getRowCount(); n++) {
            if (((String) lgmod.getValueAt(n, 1)).equals(lastOne)) {
                setRowSelected(n);
                break;
            }
        }
    }

}
