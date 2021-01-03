package hmv;

import java.awt.event.ActionListener;
import java.io.Closeable;
import java.io.IOException;
import java.util.EnumSet;


import core.Disziplin;
import core.Patient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import mandant.Mandant;
import speichern.HmvSaver;

public class Main extends Application implements Closeable{
    @Override
    public void start(Stage primaryStage) {
        try {
            stage = primaryStage;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("HMV13.fxml"));
            EnumSet<Disziplin> disziplinen = EnumSet.of(Disziplin.ER, Disziplin.KG);
            Patient patient = CoreTestDataFactory.createPatientSimonLant();
            Context context = new Context(new Mandant("123456789", "test"), new User("bob"), disziplinen, patient);
            Hmv neueHmv = CoreTestDataFactory.createHmv(context);
            Hmv13 controller = new Hmv13(neueHmv, context,context.disziplinen);
            loader.setController(controller);

            double scaleFactor = 1;
            Scene scene = new Scene(loader.load(), 630 * scaleFactor, 900 * scaleFactor);
            Scale scale = new Scale(scaleFactor, scaleFactor, 0, 0);
            scene.getRoot()
                 .getTransforms()
                 .add(scale);
            primaryStage.setResizable(true);
            primaryStage.setScene(scene);
            controller.setSpeichernListener(al);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    ActionListener al = new HMVSpeichernListener(new HmvSaver() {

        @Override
        public boolean save(Hmv hmv) {
            System.out.println("speicher entwurf" + hmv);
            return false;
        }
    }, new HmvSaver() {

        @Override
        public boolean save(Hmv hmv) {
            System.out.println("speicher in echt" + hmv);
            return false;
        }},this);

    private Stage stage;

    @Override
    public void close() throws IOException {
            stage.close();

    }
}
