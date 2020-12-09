package org.therapi.reha.patient;

import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.lang.IllegalArgumentException;

import core.*;
import mandant.IK;

public class PatientMapper {
    private IK ik;
    public PatientMapper(IK mandant_ik) {
            this.ik = mandant_ik;
    }

    private static final Logger logger =LoggerFactory.getLogger(PatientMapper.class);
    public  Optional<Patient> findbyPat_intern(String pat_intern) {

        return PatientDTO.findbyPat_intern(pat_intern, ik.digitString())
                         .map(dto -> of(dto));
    }


    public Patient of(PatientDTO dto) {

        Patient patient = new Patient(new Adresse(null,dto.strasse,dto.plz,dto.ort));
        patient.db_id=dto.patIntern;
        patient. anrede =dto.anrede;
        patient. titel =dto.titel;
        patient. nachname =dto.nName;
        patient. vorname =dto.vName;

            patient.entfernung =  dto.kilometer;

        patient.hasAbweichendeAdresse = dto.abwAdress;
        if (patient.hasAbweichendeAdresse) {
            patient.vertreter = Optional.of(new Person(dto.abwAnrede, dto.abwTitel, dto.abwNName, dto.abwVName));
            patient.abweichende = Optional.of(  new Adresse("", dto.abwStrasse, dto.abwPlz, dto.abwOrt));
        }
        patient. geburtstag = dto.geboren;
        if(dto.telefonp!=null) {
            patient. privat =Optional.of(new Telefonnummer(dto.telefonp));
        }
        if(dto.telefong!=null) {
            patient. geschaeft =Optional.of( new Telefonnummer(dto.telefong));
        }
        if(dto.telefonm!=null) {
            patient.mobil =Optional.of( new Telefonnummer(dto.telefonm));
        }

        if(dto.emailA!=null) {
            patient. email =new Emailadresse(dto.emailA);
        }

        patient. akut = new Akut(dto.akutPat,  dto.akutDat,dto.akutbis);
        patient. daten = new PlanDaten(dto.termine1,dto.termine2);
        Befreiung befreiung =null;
        if(dto.befAb!=null && dto.befDat!=null) {
             befreiung = new Befreiung(dto.befAb,dto.befDat);

        } else if (dto.befreit){

            logger.debug("wenigstens ein BefreiungsDatum = null. Von: " +  dto.befAb + " Bis: " + dto.befDat + "befreit ist " + dto.befreit);
        }

        Optional<Krankenkasse> kk;
        try {
            kk = new KrankenkasseDto().findbyId(Integer.parseInt( dto.kassenid),ik);
        } catch (NumberFormatException e) {
           kk=Optional.empty();
            logger.error("kk konnte nicht angelegt werden " + dto.kassenid ,e);
        }
        try {



            patient. kv = new Krankenversicherung(kk,dto.vNummer, VersichertenStatus.of(dto.kvStatus),befreiung);
        } catch (IllegalArgumentException e1) {

            patient.kv = new Krankenversicherung(Optional.empty(), dto.vNummer,VersichertenStatus.INVALID, befreiung);
            logger.error("KV konnte nicht angelegt werden ",e1);
        }

        if(dto.therapeut!=null) {
            patient. behandler= Optional.of(new Kollege(dto.therapeut));
        }



        if(!dto.arztid.isEmpty()) {



            patient. hauptarzt = ArztDto.findbyID(dto.arztid, ik);

        }
        patient.merkmale = new Merkmale(dto.merk1,dto.merk2,dto.merk3,dto.merk4,dto.merk5,dto.merk6);

        patient.aerzte=dto.aerzte;
        if(dto.aerzte!=null) {
        patient.arztListe = ArztDto.findbyID(dto.aerzte.replaceAll("\n", "")
                .replaceAll("@@", "@")
                .split("@"), ik);
        }
        patient.patIntern=dto.patIntern;
        patient.anamnese=dto.anamnese ==null ? "":dto.anamnese;
        patient.memo =dto.patText ==null ? "":dto.patText;
        patient.anlageDatum = dto.anlDatum;
        patient. land =dto.land;
        patient. akutBeh=dto.akutBeh;
        patient. vip_Pat=dto.vipPat;
        patient. er_Janein=dto.erJanein;
        patient. er_Dat=dto.erDat;
        patient. heimbewohn=dto.heimbewohn;
        patient. numfrei1=dto.numfrei1;
        patient. numfrei2=dto.numfrei2;
        patient. abschluss=dto.abschluss;
        patient. datfrei2=dto.datfrei2;
        patient. charfrei2=dto.charfrei2;
        patient. behDauer=dto.behDauer;
        patient. ber1=dto.ber1;
        patient. ber2=dto.ber2;
        patient. ber3=dto.ber3;
        patient. ber4=dto.ber4;
        patient. jahrfrei=dto.jahrfrei;

        return patient;

    };


    PatientDTO of(Patient pat) {
        PatientDTO patientdto = new PatientDTO();
        patientdto.anrede = pat.anrede;
        patientdto.titel = pat.titel;
        patientdto.nName = pat.nachname;
        patientdto.vName = pat.vorname;
        patientdto.geboren =  pat.geburtstag;


        if (pat.abweichende.isPresent()) {
            patientdto.abwAdress = true;
            patientdto.abwAnrede = pat.vertreter.get().getAnrede();
            patientdto.abwTitel = pat.vertreter.get().getTitel();
            patientdto.abwNName = pat.vertreter.get().getnName();
            patientdto.abwVName = pat.vertreter.get().getvName();

            Adresse abWeichendeAdresse = pat.abweichende.get();
            patientdto.abwStrasse = abWeichendeAdresse.strasse;
            patientdto.abwPlz = abWeichendeAdresse.plz.toString();
            patientdto.abwOrt = abWeichendeAdresse.ort;
        }
        Krankenversicherung krankenversicherung = pat.kv;
        if(krankenversicherung.getKk().isPresent()){


        patientdto.kasse = krankenversicherung.getKk().get().getName();
        patientdto.kvNummer = krankenversicherung.getKk().get().getIk().digitString();
        patientdto.kassenid = String.valueOf(krankenversicherung.getKk().get().getId());
        }


        patientdto.kvStatus = krankenversicherung.getStatus().getNummer();
        patientdto.vNummer = krankenversicherung.getVersicherungsnummer();
        if (krankenversicherung.getBefreit().isPresent()) {
            Befreiung befreiung = krankenversicherung.getBefreit().get();
            patientdto.befreit = befreiung.istbefreit(LocalDate.now());
            patientdto.befDat = befreiung.getBis();
            patientdto.befAb = befreiung.getVon();
        }
        patientdto.klinik = pat.klinik;


        if(pat.privat.isPresent()) {
            patientdto.telefonp = pat.privat.get().nummer;
        }
        if(pat.geschaeft.isPresent()) {
            patientdto.telefong =pat.geschaeft.get().nummer;
        }
        if(pat.mobil.isPresent()) {
            patientdto.telefonm =pat.mobil.get().nummer;
        }


        patientdto.strasse = pat.wohnadresse.strasse;
        patientdto.land = pat.land;
        patientdto.plz = pat.wohnadresse.plz;
        patientdto.ort = pat.wohnadresse.ort;
        if (pat.hauptarzt.isPresent()) {
            Arzt hauptArzt = pat.hauptarzt.get();
            patientdto.arzt = hauptArzt.getNachname();
            patientdto.arztNum = hauptArzt.getArztnummer().lanr;


            patientdto.atel = hauptArzt.getTelefon();


            patientdto.afax = hauptArzt.getFax();
            patientdto.emailA = hauptArzt.getEmail1();
            patientdto.arztid = String.valueOf(hauptArzt.getId());
        }
        patientdto.patIntern = pat.patIntern;


        patientdto.anlDatum = pat.anlageDatum;;
        patientdto.akutPat = pat.akut.isAkut();
        patientdto.akutDat = pat.akut.seit;
        patientdto.akutBeh = pat.akutBeh;
        patientdto.termine1 = pat.daten.getMoeglicheTermine1();
        patientdto.termine2 = pat.daten.getMoeglicheTermine2();
        patientdto.vipPat = pat.vip_Pat;
        patientdto.erJanein = pat.er_Janein;
        patientdto.erDat = pat.er_Dat;

        patientdto.numfrei1 = pat.numfrei1;
        patientdto.numfrei2 = pat.numfrei2;
        patientdto.heimbewohn = pat.heimbewohn;
        patientdto.abschluss = pat.abschluss;
        patientdto.akutbis = pat.akut.bis;
        patientdto.datfrei2 = pat.datfrei2;
        patientdto.kilometer = pat.entfernung;
        patientdto.charfrei2 = pat.charfrei2;

        patientdto.behDauer = pat.behDauer;
        patientdto.ber1 = pat.ber1;
        patientdto.ber2 = pat.ber2;
        patientdto.ber3 = pat.ber3;
        patientdto.ber4 = pat.ber4;
        if(pat.behandler.isPresent()) {
            patientdto.therapeut = pat.behandler.get().getMatchcode();
        }
        patientdto.merk6 = pat.merkmale.sechs();
        patientdto.merk5 = pat.merkmale.fuenf();
        patientdto.merk4 = pat.merkmale.vier();
        patientdto.merk3 = pat.merkmale.drei();
        patientdto.merk2 = pat.merkmale.zwei();
        patientdto.merk1 = pat.merkmale.eins();
        patientdto.aerzte = pat.aerzte;
        patientdto.patText = pat.memo.isEmpty()?null:pat.memo;
        patientdto.anamnese = pat.anamnese.isEmpty()?null:pat.anamnese;
        patientdto.id = pat.db_id;


        patientdto.jahrfrei = pat.jahrfrei;
        patientdto.u18ignore = pat.u18ignorieren;
        return patientdto;
    }

}
