package rehaUrlaub.Tools;

import java.awt.event.ActionListener;

import javax.swing.JButton;

public class ButtonTools {

    public static JButton macheButton(String titel, String cmd, ActionListener al) {
        JButton but = new JButton(titel);
        but.setName(cmd);
        but.setActionCommand(cmd);
        but.addActionListener(al);
        return but;
    }

}
