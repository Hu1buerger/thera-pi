package org.thera_pi.updates;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateDlg extends JDialog {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateDlg.class);

    private JLabel textlab;
    private Font textlabFont = new Font("Arial", Font.PLAIN, 12);

    UpdateDlg() {
        super();
        setUndecorated(true);
        setModal(false);
        this.setContentPane(getContent());
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        validate();
    }

    public JXPanel getContent() {
        JXPanel jpan = new JXPanel();
        jpan.setPreferredSize(new Dimension(400, 140));
        jpan.setBackground(Color.WHITE);
        jpan.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        FormLayout lay = new FormLayout("fill:0:grow(0.5),p,fill:0:grow(0.5)",
                "fill:0:grow(0.5),p,15dlu,p,fill:0:grow(0.5)");
        jpan.setLayout(lay);
        CellConstraints cc = new CellConstraints();
        JLabel bildlab = new JLabel(" ");
        bildlab.setIcon(new ImageIcon(TheraPiUpdates.imgtporg));
        jpan.add(bildlab, cc.xy(2, 2));
        textlab = new JLabel(" ");
        textlab.setFont(textlabFont);
        textlab.setForeground(Color.BLUE);
        jpan.add(textlab, cc.xy(2, 4, CellConstraints.CENTER, CellConstraints.CENTER));
        jpan.validate();
        return jpan;

    }

    public void setzeLabel(String labelText) {
        textlab.setText(labelText);
        textlab.getParent()
               .validate();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            LOG.error("Exception: "+ e.getMessage(),e);
        }
    }

}
