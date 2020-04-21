package rehaBillEdit.Tools;

import javax.swing.table.DefaultTableCellRenderer;

public class MitteRenderer extends DefaultTableCellRenderer {
    /**
    *
    */
    private static final long serialVersionUID = 1L;

    public MitteRenderer() {
        super();
        setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

    }

    @Override
    public void setValue(Object value) {
        if (value instanceof String) {
            setText((String) value);
        } else if (value instanceof Integer) {
            setText(Integer.toString((Integer) value)
                           .replace(".", ""));
        } else if (value == null) {
            setText("");
        }
    }

}
