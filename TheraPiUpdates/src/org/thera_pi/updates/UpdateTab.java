package org.thera_pi.updates;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.apache.commons.net.ftp.FTPFile;
import org.jdesktop.swingx.JXPanel;

import com.jgoodies.looks.windows.WindowsTabbedPaneUI;

public class UpdateTab extends JXPanel {

    private UpdatePanel tab1 ;

    UpdateTab(JFrame jFrame) {
        super();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createRaisedBevelBorder());
        JTabbedPane updateTab = new JTabbedPane();
        updateTab.setUI(new WindowsTabbedPaneUI());
        tab1 = new UpdatePanel(jFrame);
        JXPanel tab2 = new EntwicklerPanel(this);
        updateTab.add("<html>Anwenderseite</html>", tab1);
        updateTab.add("<html>Entwicklerseite</html>", tab2);

        add(updateTab, BorderLayout.CENTER);
        validate();
    }

    public void starteFTP() {
        tab1.starteFTP();
    }


    public FTPFile[] getFilesFromUpdatePanel() {
        return tab1.ffile;
    }

}
