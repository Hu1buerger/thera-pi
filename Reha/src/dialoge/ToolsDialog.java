package dialoge;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTitledPanel;

import events.RehaTPEvent;
import events.RehaTPEventClass;
import events.RehaTPEventListener;
import hauptFenster.Reha;
import systemEinstellungen.SystemConfig;

public class ToolsDialog extends JXDialog
        implements ActionListener, MouseListener,  KeyListener, RehaTPEventListener {

    /**
     *
     */
    private static final long serialVersionUID = 5894214872643895071L;
    private JXTitledPanel jtp = null;
    private MouseAdapter mymouse = null;
    private PinPanel pinPanel = null;
    private JXPanel content = null;
    private JList jList = null;
    private RehaTPEventClass rtp = null;

    private JButton abfeuern = null;
    private MouseListener toolsMl = null;

    public ToolsDialog(JXFrame owner, String titel, JList list) {
        super(owner, (JComponent) Reha.getThisFrame()
                                      .getGlassPane());
        this.setUndecorated(true);
        this.setName("ToolsDlg");
        this.jList = list;
        this.jList.addKeyListener(this);
        // this.jList.addMouseListener(this);

        this.jtp = new JXTitledPanel();
        this.jtp.setName("ToolsDlg");
        this.mymouse = new DragWin(this);
        this.jtp.addMouseListener(mymouse);
        this.jtp.addMouseMotionListener(mymouse);
        this.jtp.addMouseListener(this);
        this.jtp.setContentContainer(getContent(list));
        this.jtp.setTitleForeground(Color.WHITE);
        this.jtp.setTitle(titel);
        this.pinPanel = new PinPanel();
        this.pinPanel.getGruen()
                     .setVisible(false);
        this.pinPanel.setName("ToolsDlg");
        this.jtp.setRightDecoration(this.pinPanel);
        this.setContentPane(jtp);
        // this.setModal(true);
        this.setResizable(false);
        this.rtp = new RehaTPEventClass();
        this.rtp.addRehaTPEventListener(this);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setzeFocus();
            }
        });
    }

    private void setzeFocus() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jList.requestFocus();
                jList.setSelectedIndex(0);
            }
        });
    }

    private JXPanel getContent(JList list) {
        content = new JXPanel(new BorderLayout());
        content.add(new JScrollPane(list), BorderLayout.CENTER);
        if ((Boolean) SystemConfig.hmPatientenWerkzeugDlgIni.get("ToolsDlgShowButton")) {
            abfeuern = new JButton("ausführen....");
            abfeuern.setActionCommand("abfeuern");
            abfeuern.addActionListener(this);
            content.add(abfeuern, BorderLayout.SOUTH);
        }
        return content;
    }

    public void activateListener() {
        toolsMl = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                if (arg0.getClickCount() == (Integer) SystemConfig.hmPatientenWerkzeugDlgIni.get(
                        "ToolsDlgClickCount")) {
                    if (((JComponent) arg0.getSource()) instanceof JList) {
                        Reha.toolsDlgRueckgabe = Integer.valueOf(jList.getSelectedIndex());
                        FensterSchliessen("dieses");
                    }
                }

            }


        };
        this.jList.addMouseListener(toolsMl);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jList.requestFocus();
            }
        });
    }



    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (arg0.getActionCommand()
                .equals("abfeuern")) {
            Reha.toolsDlgRueckgabe = Integer.valueOf(jList.getSelectedIndex());
            FensterSchliessen("dieses");
        }

    }



    @Override
    public void mouseClicked(MouseEvent arg0) {
        if (arg0.getClickCount() == (Integer) SystemConfig.hmPatientenWerkzeugDlgIni.get("ToolsDlgClickCount")) {
            if (((JComponent) arg0.getSource()) instanceof JList) {
                Reha.toolsDlgRueckgabe = Integer.valueOf(jList.getSelectedIndex());
                FensterSchliessen("dieses");
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {

    }

    @Override
    public void mouseExited(MouseEvent arg0) {

    }

    @Override
    public void mousePressed(MouseEvent arg0) {
        /*
         * if(arg0.getClickCount()== (Integer)
         * SystemConfig.hmOtherDefaults.get("ToolsDlgClickCount")){ if(
         * ((JComponent)arg0.getSource()) instanceof JList){ this.rueckgabe =
         * jList.getSelectedIndex(); FensterSchliessen("dieses"); } }
         */
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {

    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
            if (((JComponent) arg0.getSource()) instanceof JList) {
                Reha.toolsDlgRueckgabe = Integer.valueOf(jList.getSelectedIndex());
                FensterSchliessen("dieses");
            }
        }
        if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
            Reha.toolsDlgRueckgabe = Integer.valueOf(-1);
            FensterSchliessen("dieses");
        }

    }

    @Override
    public void keyReleased(KeyEvent arg0) {

    }

    @Override
    public void keyTyped(KeyEvent arg0) {

    }

    @Override
    public void rehaTPEventOccurred(RehaTPEvent evt) {
        FensterSchliessen("dieses");

    }

    private void FensterSchliessen(String welches) {
        this.jtp.removeMouseListener(this.mymouse);
        this.jtp.removeMouseMotionListener(this.mymouse);
        this.jList.removeKeyListener(this);
        this.jList.removeMouseListener(this);
        if (toolsMl != null) {
            this.jList.removeMouseListener(toolsMl);
            toolsMl = null;
        }
        if (abfeuern != null) {
            this.abfeuern.removeActionListener(this);
        }
        this.mymouse = null;
        if (this.rtp != null) {
            this.rtp.removeRehaTPEventListener(this);
            this.rtp = null;
        }
        setVisible(false);
        this.jList = null;
        this.dispose();
    }

}
