package hmv;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

public class HmvFrame  extends JFrame {

        private static final int WIDTH = 800;
        private static final int HEIGHT = 500;

        public HmvFrame(Context context) {
            initGUI(context);
        }

        public void initGUI(Context context) {
            final HmvswingPanel panel = new HmvswingPanel(context);
            this.add(new JScrollPane( panel));
            this.setTitle("JavaFX in Swing");
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            this.setSize(WIDTH, HEIGHT);
            this.setLocationRelativeTo(null);
            // Lambda Java 8
            Platform.runLater(() -> initJFXPanel(panel));

        }

        private void initJFXPanel(HmvswingPanel panel) {
            panel.initGUI();
        }





    }


