package hmv;

import java.awt.im.InputMethodRequests;
import java.io.IOException;
import java.time.LocalDate;

import core.Disziplin;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.transform.Scale;

public class HmvswingPanel extends JFXPanel {
    private Context context;

    public HmvswingPanel(Context context) {
        this.context = context;
    }

    @Override
    public InputMethodRequests getInputMethodRequests() {
      try{
          return super.getInputMethodRequests();

      } catch (NullPointerException e) {
        return null;
    }
    }

    public void initGUI() {
        Platform.runLater(() -> {
            try {
                initJFXPanel(this);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        this.setVisible(true);
    }

    private void initJFXPanel(JFXPanel panel) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("HMV13.fxml"));


        Hmv  hmvorig = new Hmv(context);
        hmvorig.ausstellungsdatum = LocalDate.of(2020,05,25);
        hmvorig.dringlich = Boolean.TRUE;

        hmvorig.diag = new Diagnose(new Icd10("43.1"), new Icd10("69"),DG.INVALID,new Leitsymptomatik(DG.INVALID, Leitsymptomatik.X,"besonders gaga"));
        hmvorig.beh = new Behandlung();
        hmvorig.disziplin = Disziplin.ER;
        Hmv13 controller = new Hmv13(hmvorig, context);
        loader.setController(controller);

        double scaleFactor = 1;
        Scene scene = new Scene(loader.load(), 630 * scaleFactor, 900 * scaleFactor);
        Scale scale = new Scale(scaleFactor, scaleFactor, 0, 0);
        scene.getRoot()
             .getTransforms()
             .add(scale);
        panel.setScene(scene);
    }

}
