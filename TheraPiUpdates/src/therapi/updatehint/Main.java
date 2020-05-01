package therapi.updatehint;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.thera_pi.updater.HTTPRepository;
import org.thera_pi.updater.Version;
import org.thera_pi.updater.VersionsSieb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

	static List<File> updatefiles;



    @Override
	public void start(Stage primaryStage) throws IOException {



        File updateFile=  updatefiles.get(0);

	    primaryStage.initStyle(StageStyle.UNDECORATED);
	    String resourcePath = "./UpdatesAvailable.fxml";
        URL location = getClass().getResource(resourcePath);
        FXMLLoader fxmlLoader = new FXMLLoader(location);
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        UpdatesAvailable controller = fxmlLoader.<UpdatesAvailable>getController();

        controller.setFileToDownload(updateFile);

        primaryStage.setScene(scene);
        primaryStage.show();
	}



	public static void main(String[] args) {

        updatefiles = new VersionsSieb(new Version()).select( new HTTPRepository().filesList());
        if(!updatefiles.isEmpty()) {

		launch(args);
        }
	}
}
