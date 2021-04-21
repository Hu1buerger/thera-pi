package abrechnung.privat;

import java.util.Vector;

public class Rezept {

    private Vector<String> vecaktrez;

    public Rezept(Vector<String> aktuellesRezeptVector) {
        this.vecaktrez = aktuellesRezeptVector;
    }

    String aktuellesRezept_11_artderbeh4() {
        return vecaktrez.get(11);
    }

    String aktuellesRezept_10_artderbeh3() {
        return vecaktrez.get(10);
    }

    String aktuellesRezept_9_artderbeh2() {
        return vecaktrez.get(9);
    }

    String aktuellesRezept_8_artderbeh1() {
        return vecaktrez.get(8);
    }

    String aktuellesRezept_61_einzelnabrechenbar() {
        return vecaktrez.get(61);
    }

    String aktuellesRezept_43_hausbesuch() {
        return vecaktrez.get(43);
    }

    String aktuellesRezept_16_arztID() {
        return vecaktrez.get(16);
    }

    String aktuellesRezept_37_kassenId() {
        return vecaktrez.get(37);
    }

    String aktuellesRezept_0_patintern() {
        return vecaktrez.get(0);
    }
    String aktuellesRezept_1_rezNr() {
        return vecaktrez.get(1);
    }

    String aktuellesRezept_51_pos4() {
        return vecaktrez.get(51);
    }

    String aktuellesRezept_6_Anzahl4() {
        return vecaktrez.get(6);
    }

    String aktuellesRezept_4_anzahl2() {
        return vecaktrez.get(4);
    }

    String aktuellesRezept_48_pos1() {
        return vecaktrez.get(48);
    }

    String aktuellesRezept_3_anzahl1() {
        return vecaktrez.get(3);
    }

    String aktuellesRezept_2_Rezeptdatum() {
        return vecaktrez.get(2);
    }

    String aktuellesRezept_34_termine() {
        return vecaktrez.get(34);
    }

    String aktuellesRezept_5_anzahl3() {
        return vecaktrez.get(5);
    }

    String aktuellesRezept_50_pos3() {
        return vecaktrez.get(50);
    }

    String aktuellesRezept_49_pos2() {
        return vecaktrez.get(49);
    }

    String aktuellesRezept_64_hbAnzahl() {
        return vecaktrez.get(64);
    }

    String aktuellesRezept_62_abschluss() {
        return vecaktrez.get(62)
                        .trim();
    }

}
