package hmv;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.therapi.hmv.dao.DiagnosegruppeDao;
import org.therapi.hmv.dao.HeilmittelDao;
import org.therapi.hmv.entities.Diagnosegruppe;

import core.Arzt;
import core.Disziplin;
import core.Krankenkasse;
import core.Patient;
import core.VersichertenStatus;
import core.Zuzahlung;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
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
	ComboBox<Diagnosegruppe> diagnoseGruppe;

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
	ObservableList<Diagnosegruppe> alleDiaGruppen;
	

	public Hmv13(Hmv neueHmv, Context context) {
		this.hmv = neueHmv;
		this.context = context;
		moeglicheDisziplinen = context.mandant.disziplinen();

	}

	@FXML
	public void initialize() {

		initGui();

		setData();
		setFormData();

	}

	FilteredList<Diagnosegruppe> availableDiagGruppe;

	private void setFormData() {

	}

	private void setData() {

		Patient patient = hmv.patient;
		name.setText(patient.nachname);
		vorname.setText(patient.vorname);
		geboren.setValue(patient.geburtstag);

		versichertenStatus.setValue(patient.kv.getStatus());

		ik_Erbringer.setText(hmv.mandant.ikDigitString());
		enableNeededDisciplines(context);

		Iterator<Disziplin> iterator = moeglicheDisziplinen.iterator();
		if (iterator.hasNext()) {
			diszi.set(iterator.next());
		}

		erfasser.setText(hmv.angelegtvon.anmeldename);
		if (hmv.disziplin != null) {
			diszi.set(hmv.disziplin);
		}

		Optional<Krankenkasse> kk = hmv.kv.getKk();
		String kostentraegerik = "";
		if (kk.isPresent()) {
			kostentraegerik = kk.get().getIk().digitString();
			kostentraeger.setValue(kk.get().getName());
		}
		kostentraegerKennung.setText(kostentraegerik);
		versichertenNummer.setText(hmv.kv.getVersicherungsnummer());

		if (patient.hatBefreiung(LocalDate.now())) {

			zuzahlungProperty.set(Zuzahlung.BEFREIT);
		}

		Optional<Arzt> optarzt = patient.hauptarzt;
		if (optarzt.isPresent()) {
			lebenslangeArztNr.setValue(optarzt.get().getArztnummer().lanr);
			betriebsstaettenNr.setText(optarzt.get().getBsnr());
		}

		rezeptDatum.setValue(hmv.ausstellungsdatum);
		dringlich.setValue(hmv.dringlich);

		if (hmv.diag.diagnosegruppe != DG.INVALID) {
			diagnoseGruppe.setPromptText(hmv.diag.diagnosegruppe.gruppe);
		}
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

		versichertenStatus.getItems().setAll(VersichertenStatus.values());
		alleDiaGruppen = FXCollections.observableList(new DiagnosegruppeDao(context.mandant.ik()).all());
		availableDiagGruppe = new FilteredList<Diagnosegruppe>(alleDiaGruppen);

		diagnoseGruppe.setItems(availableDiagGruppe);

		diszi.addListener(new ChangeListener<Disziplin>() {

			@Override
			public void changed(ObservableValue<? extends Disziplin> observable, Disziplin oldValue,
					Disziplin newValue) {
				Predicate<? super Diagnosegruppe> diag_diszi_predicate = new Predicate<Diagnosegruppe>() {

					@Override
					public boolean test(Diagnosegruppe t) {
						return t.diszi == newValue;
					}
				};
				availableDiagGruppe.setPredicate(diag_diszi_predicate);
				Predicate<? super Heilmittel> hmDisziPredicate = new Predicate<Heilmittel>() {

					@Override
					public boolean test(Heilmittel t) {
						return t.hmr_disziplin == newValue;
					}
				};
				vorangigsHm.setPredicate(hmDisziPredicate);
				ergaenzend.setPredicate(hmDisziPredicate);

			}
		});

		Predicate<? super Heilmittel> hmVorangig = new Predicate<Heilmittel>() {

			@Override
			public boolean test(Heilmittel t) {
				return t.vorrangig;
			}
		};
		
		leitsymptomatik_kuerzel.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
				if (leitsymptomatik_kuerzel.getSelectedToggle() != null) {
					Diagnosegruppe selectedItem = diagnoseGruppe.getSelectionModel().getSelectedItem();
					if (selectedItem != null) {
						String ls = new_toggle.getUserData().toString();
						System.out.println("Leitsymptomatik" + ls);
						FilteredList<Diagnosegruppe> result = alleDiaGruppen.filtered(t -> t.diszi == diszi.get())
								.filtered(t -> t.diagnosegruppe.equals(selectedItem.diagnosegruppe))
								.filtered(t -> t.leitsymptomatik.equals(ls.toLowerCase()));
						System.out.println(result);

						if (result.isEmpty()) {
							leitsymptomatik.setText("ungültige kombination ");
						} else {
							leitsymptomatik.setText(result.get(0).leitsymptomatik_beschreibung);
						}
					}
				}
			}
		});
		
		List<Heilmittel> alleHeilmittel= new HeilmittelDao(context.mandant.ik()).all();
				//new FilteredList<Heilmittel>( FXCollections.observableList(new HeilmittelDao(context.mandant.ik()).all()));
		Map<Boolean, List<Heilmittel>> vorrang = alleHeilmittel.stream()
		        .collect(Collectors.partitioningBy(h->h.vorrangig));
		vorangigsHm = new FilteredList<Heilmittel>( FXCollections.observableList(vorrang.get(true)));
		ergaenzend = new FilteredList<Heilmittel>( FXCollections.observableList(vorrang.get(true)));
		hm_1.setItems(vorangigsHm);
		hm_2.setItems(vorangigsHm);
		hm_3.setItems(vorangigsHm);
		hm_ergaenzend.setItems(ergaenzend);
		
		
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
		toggleGroup.getToggles().forEach(t -> t.setUserData(((Node) t).getId().toUpperCase()));
	}

	private <T extends Enum<T>> void setUserDataToId(ToggleGroup toggleGroup, Class<T> class1) {
		toggleGroup.getToggles().forEach(t -> t.setUserData(Enum.valueOf(class1, ((Node) t).getId().toUpperCase())));
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
	private FilteredList<Heilmittel> vorangigsHm;
	private FilteredList<Heilmittel> ergaenzend;

	private boolean mustnotbeempty(Node node) {
		boolean empty = ((TextArea) node).getText().isEmpty();

		if (empty) {
			invalidNodes.add(node);
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
		Diagnosegruppe diaggrp = diagnoseGruppe.getSelectionModel().getSelectedItem();
		hmvOut.diag = new Diagnose(new Icd10(icd10Code_1.getText()), new Icd10(icd10Code_2.getText()),
				new DG(diaggrp.diagnosegruppe, diaggrp.diagnosegruppe_beschreibung),
				new Leitsymptomatik(DG.INVALID,
						String.valueOf(leitsymptomatik_kuerzel.getSelectedToggle().getUserData()),
						leitsymptomatik.getText()));
		hmvOut.beh = new Behandlung();
		hmvOut.nummer = hmv.nummer;
		return hmvOut;

	}

}
