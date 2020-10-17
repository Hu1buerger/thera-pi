package rehaBillEdit.Tools;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import CommonTools.JRtaTextField;

public class DblCellEditor extends AbstractCellEditor implements KeyListener, TableCellEditor {
    private static final long serialVersionUID = -2101893331215081609L;
    private JComponent component = new JRtaTextField("D", true, "6.2", "");
    private boolean mitMaus;

    /**
     * This method is called when editing is completed.
     * It must return the new value to be stored in the cell.
     */
    public DblCellEditor() {
        component.addKeyListener(this);
    }

    @Override
    public Object getCellEditorValue() {
        String foo;
        try {
            foo = ((JFormattedTextField) component).getText()
                                                   .replaceAll(",", ".");
            if (foo.isEmpty()) {
                foo = "0.00";
            }
        } catch (Exception ex) {
            foo = "0.00";
        }
        double i_spent_hours_on_this = Double.parseDouble(foo);
        return Double.valueOf(i_spent_hours_on_this);
    }

    /** This method is called when a cell value is edited by the user. */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // Configure the component with the specified value
        if (!mitMaus) {
            ((JFormattedTextField) component).setText(String.valueOf(value));
            ((JFormattedTextField) component).selectAll();
            ((JFormattedTextField) component).setHorizontalAlignment(SwingConstants.RIGHT);
        } else {
            final String xvalue = String.valueOf(value);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ((JFormattedTextField) component).setText(String.valueOf(xvalue)
                                                                    .replace(".", ","));
                    ((JFormattedTextField) component).selectAll();
                    ((JFormattedTextField) component).setHorizontalAlignment(SwingConstants.RIGHT);
                    ((JFormattedTextField) component).setCaretPosition(0);
                }
            });
        }

        // Return the configured component
        //// System.out.println("I've been Called!!");
        return component;
    }

    @Override
    public boolean isCellEditable(EventObject evt) {
        if (evt instanceof MouseEvent) {
            if (((MouseEvent) evt).getClickCount() == 2) {
                ((MouseEvent) evt).consume();
                mitMaus = true;
                return true;
            }
        } else {
            mitMaus = false;
            return true;
        }
        return false;
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        if (mitMaus && arg0.getKeyCode() == KeyEvent.VK_ENTER) {
            //// System.out.println("in Maus + Return gedrückt");
            fireEditingStopped();
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }
}
