package therapi.updatehint.fx;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thera_pi.updater.Version;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import therapi.updatehint.HyperLInkEventHandler;

public class UpdatesAvailable {

    static final Logger logger = LoggerFactory.getLogger(UpdatesAvailable.class);
    private @FXML
    TextFlow tf;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        Stage stage = (Stage) ((Node) (event.getSource())).getScene()
                                                          .getWindow();
        stage.close();

    }

    @FXML
    public void initialize() {

        Text ueberSchrift = new Text("F\u00fcr ihre Version \n" + new Version().number() + ".\n");
        ueberSchrift.setFont(Font.font("system", FontWeight.BOLD, FontPosture.REGULAR, 20));
        Text aufforderungAnfang = new Text("Laden Sie bitte die Datei\n");
        dateiname = new Text("");
        dateiname.setFont(Font.font("system", FontWeight.EXTRA_BOLD, FontPosture.REGULAR, 25));

        Text aufforderungMitte = new Text("von dieser Webseite\n");

        Hyperlink link = new Hyperlink(HyperLInkEventHandler.UPDATES_PAGE);
        link.setOnAction(goHandler);
        Text aufforderungEnde = new Text("\nherunter\n");

        tf.getChildren()
          .add(ueberSchrift);
        tf.getChildren()
          .add(aufforderungAnfang);
        tf.getChildren()
          .add(dateiname);
        tf.getChildren()
        .add(aufforderungMitte);
        tf.getChildren()
          .add(link);
        tf.getChildren()
          .add(aufforderungEnde);
        tf.setLineSpacing(20.0f);

        tf.setTextAlignment(TextAlignment.CENTER);

    }

    private EventHandler<ActionEvent> goHandler = new HyperLInkEventHandler();
    private Text dateiname;

    public void setFileToDownload(File updateFile) {
        dateiname.setText(updateFile.getName() + "\n");

    }
}
