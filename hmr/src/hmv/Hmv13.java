package hmv;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import core.Arzt;
import core.Disziplin;
import core.Krankenkasse;
import core.Patient;
import core.VersichertenStatus;
import core.Zuzahlung;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import specs.Contracts;

public class Hmv13 {



    ActionListener speichernListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            // do nothing, just don't blow up!

        }
    };

    public void setSpeichernListener(ActionListener speichernListener) {
        Contracts.require(speichernListener != null, "actionlistener must not be null");

        this.speichernListener = speichernListener;
    }

    @FXML
    ToggleGroup zuzahlung;
    private ObjectProperty<Zuzahlung> zuzahlungProperty = new SimpleObjectProperty<>();

    @FXML
    ComboBox<String> kostentraeger;

    @FXML
    TextField name;

    @FXML
    TextField vorname;

    @FXML
    DatePicker geboren;

    @FXML
    TextField kostentraegerKennung;
    @FXML
    TextField versichertenNummer;
    @FXML
    ChoiceBox<VersichertenStatus> versichertenStatus;

    @FXML
    TextField betriebsstaettenNr;
    @FXML
    ComboBox<String> lebenslangeArztNr;
    @FXML
    DatePicker rezeptDatum;

    @FXML
    TextField erfasser;
    @FXML
    RadioButton kg;
    @FXML
    RadioButton et;
    @FXML
    RadioButton er;
    @FXML
    RadioButton lo;
    @FXML
    RadioButton po;

    @FXML

    ToggleGroup disziplin;

    @FXML
    TextField icd10Code_1;
    @FXML
    TextField icd10Code_2;
    // do we need 2 textfields?
    @FXML
    TextArea icd10Code_Text;

    // this might be a combobox
    @FXML
    ComboBox<DG> diagnoseGruppe;

    @FXML
    ToggleGroup leitsymptomatik_kuerzel;
    @FXML
    TextArea leitsymptomatik;

    @FXML
    ChoiceBox<Heilmittel> hm_1;
    @FXML
    TextField hm_einheiten_1;

    @FXML
    ChoiceBox<Heilmittel> hm_2;
    @FXML
    TextField hm_einheiten_2;

    @FXML
    ChoiceBox<Heilmittel> hm_3;
    @FXML
    TextField hm_einheiten_3;

    @FXML
    ChoiceBox<Heilmittel> hm_ergaenzend;
    @FXML
    TextField hm_einheiten_ergaenzend;

    @FXML
    CheckBox therapieBericht;

    @FXML
    ToggleGroup hausbesuch;
    ObjectProperty<Hausbesuch> hb = new SimpleObjectProperty<>();

    @FXML
    TextField therapieFrequenz;

    @FXML
    TextField therapieFrequenzBis;

    @FXML
    TextField dauer;

    @FXML
    ColorPicker kalenderfarbe;

    @FXML
    CheckBox dringlicherBedarf;
    SimpleBooleanProperty dringlich = new SimpleBooleanProperty();

    @FXML
    TextArea therapieZiele;

    @FXML
    Label ik_Erbringer;

    private Context context;
    private Hmv hmv;
    private EnumSet<Disziplin> moeglicheDisziplinen = EnumSet.noneOf(Disziplin.class);
    private ObjectProperty<Disziplin> diszi = new SimpleObjectProperty<>();
    private ObjectProperty<String> symptomatik = new SimpleObjectProperty<>();

    public Hmv13(Hmv neueHmv, Context context) {
        this.hmv = neueHmv;
        this.context = context;
        moeglicheDisziplinen = context.mandant.disziplinen();
    }

    @FXML
    public void initialize() {

        initGui();
        setData();

    }

    private void setData() {

        List<DG> alle = java.util.Collections.emptyList();

        Patient patient = hmv.patient;
        name.setText(patient.nachname);
        vorname.setText(patient.vorname);
        geboren.setValue(patient.geburtstag);



        versichertenStatus.setValue(patient.kv.getStatus());


        ik_Erbringer.setText(hmv.mandant.ikDigitString());
        enableNeededDisciplines(context);

        Iterator<Disziplin> iterator = moeglicheDisziplinen.iterator();
        if(iterator.hasNext()) {
            diszi.set(iterator
                    .next());
        }


        erfasser.setText(hmv.angelegtvon.anmeldename);
        if (hmv.disziplin != null) {
            diszi.set(hmv.disziplin);
        }


        Optional<Krankenkasse> kk = hmv.kv.getKk();
        String kostentraegerik = "";
        if (kk.isPresent()) {
            kostentraegerik = kk.get()
                                .getIk()
                                .digitString();
            kostentraeger.setValue(kk.get()
                                     .getName());
        }
        kostentraegerKennung.setText(kostentraegerik);
        versichertenNummer.setText(hmv.kv.getVersicherungsnummer());

        if (patient.hatBefreiung(LocalDate.now())) {

            zuzahlungProperty.set(Zuzahlung.BEFREIT);
        }

        Optional<Arzt> optarzt = patient.hauptarzt;
        if (optarzt.isPresent()) {
            lebenslangeArztNr.setValue(optarzt.get()
                                              .getArztnummer().lanr);
            betriebsstaettenNr.setText(optarzt.get()
                                              .getBsnr());
        }

        rezeptDatum.setValue(hmv.ausstellungsdatum);
        dringlich.setValue(hmv.dringlich);
        diagnoseGruppe.setItems( FXCollections.observableArrayList(alle));

diagnoseGruppe.setEditable(false);

   ComboBoxAutoComplete<DG> autocompl = new ComboBoxAutoComplete<>(diagnoseGruppe);
        BiPredicate<DG,String> searchPredicate = new BiPredicate<DG, String>() {

            @Override
            public boolean test(DG t, String u) {
              return  t.gruppe.toLowerCase().contains(u.toLowerCase());
            }
        };
        autocompl.setSearchPredicate(searchPredicate);
        diagnoseGruppe.setPromptText(hmv.diag.diagnosegruppe.gruppe);
        icd10Code_1.setText(hmv.diag.icd10_1.schluessel);
        icd10Code_2.setText(hmv.diag.icd10_2.schluessel);
        symptomatik.setValue(hmv.diag.leitsymptomatik.kennung);
        leitsymptomatik.setText(hmv.diag.leitsymptomatik.text);
    }

    private void initGui() {
        rezeptDatum.setConverter(new SixNumbersConverter(rezeptDatum.getConverter()));

        bindTogglegroup(zuzahlung, zuzahlungProperty, Zuzahlung.class);
        bindTogglegroup(hausbesuch, hb, Hausbesuch.class);
        bindTogglegroup(disziplin, diszi, Disziplin.class);
        bindTogglegroup(leitsymptomatik_kuerzel, symptomatik);

        dringlich.bindBidirectional(dringlicherBedarf.selectedProperty());

        versichertenStatus.setConverter(new StringConverter<VersichertenStatus>() {

            @Override
            public String toString(VersichertenStatus status) {

                return status.getNummer() + " " + status;
            }

            @Override
            public VersichertenStatus fromString(String string) {
                return null;
            }
        });



        versichertenStatus.getItems()
                          .setAll(VersichertenStatus.values());
    }

    private <T extends Enum<T>> void bindTogglegroup(ToggleGroup toggleGroup, ObjectProperty<T> objectProperty,
            Class<T> enume) {
        setUserDataToId(toggleGroup, enume);
        new ToggleGroupBinding<T>(toggleGroup, objectProperty);
    }

    private <T> void bindTogglegroup(ToggleGroup toggleGroup, ObjectProperty<T> objectProperty) {
        setUserDataToId(toggleGroup);
        new ToggleGroupBinding<T>(toggleGroup, objectProperty);
    }

    private void setUserDataToId(ToggleGroup toggleGroup) {
        toggleGroup.getToggles()
                   .forEach(t -> t.setUserData(((Node) t).getId()
                                                         .toUpperCase()));
    }

    private <T extends Enum<T>> void setUserDataToId(ToggleGroup toggleGroup, Class<T> class1) {
        toggleGroup.getToggles()
                   .forEach(t -> t.setUserData(Enum.valueOf(class1, ((Node) t).getId()
                                                                              .toUpperCase())));
    }

    private void enableNeededDisciplines(Context context) {
        for (Disziplin disziplin : context.disziplinen) {
            switch (disziplin) {
            case KG:
                kg.setDisable(false);
                break;
            case PO:
                po.setDisable(false);
                break;
            case LO:
                lo.setDisable(false);
                break;
            case ER:
                er.setDisable(false);
                break;
            case ET:
                et.setDisable(false);
                break;

            default:
                // egal
                break;
            }

        }
    }

    @FXML
    private void setnewbefreiung() {

    }

    @FXML
    private void speichern() {

        markierungenAufheben();
        boolean allesOK = pruefenUndMarkierungenSetzen();

        String command = String.valueOf(allesOK);

        ActionEvent speichernEvent = new ActionEvent(hmv, ActionEvent.ACTION_PERFORMED, command);
        speichernListener.actionPerformed(speichernEvent);
    }

    private boolean pruefenUndMarkierungenSetzen() {
        name.setStyle("-fx-background-color: red;");
        boolean result = mustnotbeempty(leitsymptomatik);
        for (Node node : invalidNodes) {
            node.lookup(".content").setStyle("-fx-background-color: red;");



        }
        return result;
    }

    private final HashSet<Node> invalidNodes = new HashSet<>();
    private boolean mustnotbeempty(Node node) {
        boolean empty = ((TextArea) node).getText()
                                  .isEmpty();

        if(empty) {
            invalidNodes.add (node);
            return false;
        } else {
            return true;
        }


    }

    private void markierungenAufheben() {
        // TODO Auto-generated method stub

    }

    @FXML

    private void abbrechen() {

    }

    @FXML

    private void hmrcheck() {
        System.out.println("mimimi");
        pruefenUndMarkierungenSetzen();

    }

    Hmv toHmv() {
        Hmv hmvOut = new Hmv(context);
        hmvOut.disziplin = diszi.get();
        hmvOut.ausstellungsdatum = rezeptDatum.getValue();
        hmvOut.dringlich = dringlicherBedarf.isSelected();
        hmvOut.diag = new Diagnose(new Icd10(icd10Code_1.getText()), new Icd10(icd10Code_2.getText()),
                diagnoseGruppe.getSelectionModel().getSelectedItem(), new Leitsymptomatik(DG.INVALID, String.valueOf(leitsymptomatik_kuerzel.getSelectedToggle()
                                                                                                    .getUserData()),
                        leitsymptomatik.getText()));
        hmvOut.beh = new Behandlung();
        hmvOut.nummer = hmv.nummer;
        return hmvOut;

    }

}
