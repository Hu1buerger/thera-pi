package hauptFenster.login;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXHeader;

import environment.Path;

public class LoginPanel extends JPanel {

    public LoginPanel(ActionListener listener) {
        initPanel(listener);
    }

    private KeyListener consumeInput = new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent e) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                pwButton.doClick();
            }

            if (e.getKeyCode() == KeyEvent.VK_CAPS_LOCK) {
                setCapsLockVisibility();

            }
        }

    };


    private void setCapsLockVisibility() {
        capsLockLabel.setVisible(Toolkit.getDefaultToolkit()
                                        .getLockingKeyState(KeyEvent.VK_CAPS_LOCK));
    }

    private JPasswordField pwTextFeld;
    private JButton pwButton;
    private JLabel capsLockLabel = new JLabel("DAS IST HIER ALLES GROSSGESTELLT");

    private void initPanel(ActionListener submitted) {
        setBorder(null);
        setLayout(new BorderLayout());
        ImageIcon imageIcon = new ImageIcon(Path.Instance.getProghome() + "icons/schluessel3.gif");
        JXHeader header = new JXHeader("Benutzer Authentifizierung",
                "In diesem Fenster geben Sie Ihr persönliches Passwort ein.\n"
                        + "Abhängig vom Ihrem Passwort, haben Sie Zugang zu allen Programmteilen "
                        + "die für Sie persönlich freigeschaltet wurden.\n"
                        + "Noch kein Passwort? Dann geben Sie bitte das Universalpasswort ein. \n\n"
                       ,
                imageIcon);
        add(header, BorderLayout.NORTH);

        JPanel jgrid = new JPanel(new GridLayout(4, 1));
        jgrid.setBorder(null);

        jgrid.add(new JLabel(""));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.ipadx = 0;

        gridBagConstraints.weightx = 1.0D;
        gridBagConstraints.weighty = 1.0D;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10);

        JPanel jp = new JPanel(new FlowLayout());
        jp.setBorder(null);

        jp.add(capsLockLabel);
        setCapsLockVisibility();
        JLabel label = new JLabel("Bitte Passwort eingeben:  ");
        jp.add(label);
        pwTextFeld = new JPasswordField();
        pwTextFeld.setPreferredSize(new Dimension(120, 25));
        pwTextFeld.addKeyListener(consumeInput);
        jp.add(pwTextFeld);

        jgrid.add(jp);

        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.anchor = GridBagConstraints.CENTER;
        gridBagConstraints2.fill = GridBagConstraints.NONE;
        gridBagConstraints2.ipadx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.weightx = 1.0D;
        gridBagConstraints2.weighty = 0.5D;
        gridBagConstraints2.insets = new Insets(10, 0, 10, 0);
        pwButton = new JButton("Benutzer authentifizieren");
        pwButton.setPreferredSize(new Dimension(160, 25));
        pwButton.addActionListener(submitted);
        JPanel butpanel = new JPanel(new FlowLayout());
        butpanel.setBorder(null);
        butpanel.add(pwButton);
        jgrid.add(butpanel);
        jgrid.add(new JLabel(""));

        add(jgrid, BorderLayout.CENTER);
        setVisible(true);
        addKeyListener(consumeInput);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                pwTextFeld.requestFocus();
            }
        });
    }

    public char[] passwort() {
        return pwTextFeld.getPassword();
    }

    public void retry() {
        pwTextFeld.requestFocus();

    }

}
