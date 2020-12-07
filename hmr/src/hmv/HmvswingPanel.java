package hmv;

import java.awt.im.InputMethodRequests;
import java.io.IOException;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;

import javax.swing.JFrame;

import core.Adresse;
import core.Befreiung;
import core.Disziplin;
import core.Krankenkasse;
import core.Krankenversicherung;
import core.LANR;
import core.Patient;
import core.VersichertenStatus;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.transform.Scale;
import mandant.IK;
import mandant.Mandant;

public class HmvswingPanel extends JFXPanel {
    public HmvswingPanel() {
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

        EnumSet<Disziplin> disziplinen = EnumSet.of(Disziplin.ER, Disziplin.KG);
        Patient patient = new Patient(new Adresse("", "hohle gasse 5", "12345", "Baumburg"));
        patient.nachname = "Lant";
        patient.vorname = "Simon";
        Krankenkasse kk = new KrankenkasseFactory().withIk(new IK("999999999"))
                                                   .withName("donotpay")
                                                   .build();
        Befreiung befreit = new Befreiung(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31));

        patient.kv = new Krankenversicherung(Optional.of(kk), "0815", VersichertenStatus.RENTNER, befreit);

        patient.geburtstag = LocalDate.of(1904, 2, 29);

        patient.hauptarzt = Optional.of(    new ArztFactory().withNachname("Eisenbart")
                .withArztnummer(new LANR("081500000"))
                .withBsnr("000008150")
                .build());
        Context context = new Context(new Mandant("123456789", "test"), new User("bob"), disziplinen, patient);
        Hmv  hmvorig = new Hmv(context);
        hmvorig.ausstellungsdatum = LocalDate.of(2020,05,25);
        hmvorig.dringlich = Boolean.TRUE;

        hmvorig.diag = new Diagnose(new Icd10("43.1"), new Icd10("69"),"ab5",new Leitsymptomatik(Leitsymptomatik.X,"besonders gaga"));
        hmvorig.beh = new Behandlung();
        hmvorig.disziplin = Disziplin.ER;
        Hmv13 controller = new Hmv13(hmvorig, context,context.disziplinen);
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
