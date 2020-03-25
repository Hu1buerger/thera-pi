package org.thera_pi.updates;

import java.awt.*;

import javax.swing.*;

import org.jdesktop.swingx.JXPanel;

public class JCompTools {

    /**
     * private CTor to avoid creating an instance of a class with only static
     * methods.
     */
    private JCompTools() {
        // nothing to do here
    }

    public static JScrollPane getTransparentScrollPane(JXPanel jpan) {
        JScrollPane jscr = new JScrollPane();
        jscr.setOpaque(false);
        jscr.getViewport()
            .setOpaque(false);
        jscr.setBorder(null);
        jscr.setViewportBorder(null);
        jscr.setViewportView(jpan);
        jscr.validate();
        return jscr;
    }

    public static JScrollPane getTransparentScrollPane(Component jpan) {
        JScrollPane jscr = new JScrollPane();
        jscr.setOpaque(false);
        jscr.getViewport()
            .setOpaque(false);
        jscr.setBorder(null);
        jscr.setViewportBorder(null);
        jscr.setViewportView(jpan);
        jscr.validate();
        return jscr;
    }
}
