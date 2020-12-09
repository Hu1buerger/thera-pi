package factories;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class TestFrame extends JFrame {

    public TestFrame(JPanel panel) {
        super("Testframe");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane()
             .add(panel);
setLocationRelativeTo(null);
        pack();



    }

    public void showme() {
        setVisible(true);

    }

}
