package systemEinstellungen;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.FormLayout;

import hauptFenster.Reha;

public class SysUtilPreisregeln extends JXPanel implements KeyListener, ActionListener {
    public SysUtilPreisregeln() {
        super(new BorderLayout());
        // System.out.println("Aufruf SysUtilKalenderanlagen");
        this.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 20));
        /****/
        setBackgroundPainter(Reha.instance.compoundPainter.get("SystemInit"));
        /****/
        add(getVorlagenSeite(), BorderLayout.CENTER);
        return;
    }

    /**************
     * Beginn der Methode für die Objekterstellung und -platzierung
     *********/
    private JPanel getVorlagenSeite() {
        // 1. 2. 3. 4. 5. 6. 7. 8. 9.
        FormLayout lay = new FormLayout("right:max(60dlu;p), 4dlu, 40dlu, 4dlu, 40dlu, 4dlu, 40dlu, 4dlu, 40dlu",
                // 1. 2. 3. 4. 5. 6. 7. 8. 9. 10. 11. 12. 13. 14. 15. 16. 17. 18. 19. 20. 21.
                // 22. 23.
                "p, 2dlu, p, 10dlu,10dlu,10dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p,  10dlu ,10dlu, 10dlu, p");

        PanelBuilder builder = new PanelBuilder(lay);
        builder.setDefaultDialogBorder();
        builder.getPanel()
               .setOpaque(false);
        // CellConstraints cc = new CellConstraints();

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

    }

}
