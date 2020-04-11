package core;

import java.util.EnumSet;

public enum Disziplin {
    KG("Physio", "Physio-Rezept", "2"),
    MA("Massage", "Massage/Lymphdrainage-Rezept", "1"),
    ER("Ergo", "Ergotherapie-Rezept", "5"),
    LO("Logo", "Logopädie-Rezept", "3"),
    PO("Podo", "Podologie-Rezept", "7"),
    RS("Rsport", "Rehasport-Rezept", "8"),
    FT("Ftrain", "Funktionstraining-Rezept", ""),
    INV("invalid", "invalid", "");

    public final String medium;
    public final String lang;
    public final String hmpraefix;

    private Disziplin(String medium, String lang, String hmpraefix) {
        this.medium = medium;
        this.lang = lang;
        this.hmpraefix = hmpraefix;
    }

    public static Disziplin ofMedium(String medium) {
        for (Disziplin d : Disziplin.values()) {
            if (d.medium.equals(medium)) {
                return d;
            }
        }
        return INV;
    }

    /**
     * Returns an enumset with RH and FT.
     *
     * The enumset is recreated on each call as there is no way to create an
     * immutable enumset.
     *
     * @return
     */
    public EnumSet<Disziplin> ohneReha() {
        return EnumSet.of(KG, MA, ER, LO, PO);
    }
}
