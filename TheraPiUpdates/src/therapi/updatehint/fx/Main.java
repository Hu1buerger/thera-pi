package therapi.updatehint.fx;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import therapi.updatehint.Hint;

 class Main extends Application implements Runnable {

    private Hint hint;

     Main(Hint hinweis) {
        this.hint = hinweis;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        File updateFile = hint.fileToDownload();

        primaryStage.initStyle(StageStyle.UNDECORATED);
        URL location = getClass().getResource("UpdatesAvailable.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        UpdatesAvailable controller = fxmlLoader.<UpdatesAvailable>getController();

        controller.setFileToDownload(updateFile);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void run() {
        try {
            start(new Stage());
        } catch (IOException e) {
            LoggerFactory.getLogger(Main.class).error("Fehler beim start des UpdateHinweises",e);
        }

    }


}
