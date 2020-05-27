package rezept;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.Objects;

import CommonTools.DateTimeFormatters;
import specs.Contracts;

public class Behandlung implements Comparable<Behandlung> {
    LocalDate datum;
    String kollege;
    String unterbrechungsbegruendung;
    /** Mehrere mit kommata getrennt. */
    List<String> heilmittel;
    LocalDate erfassungsDatum;
    private DateTimeFormatter format = DateTimeFormatters.ddMMYYYYmitPunkt;
    private DateTimeFormatter sqlFormat = DateTimeFormatters.yyyyMMddmitBindestrich;

    Behandlung(String fromDB) {
        String[] parts = fromDB.split("@");
        datum = LocalDate.parse(parts[0], format);
        kollege = parts[1];
        unterbrechungsbegruendung = parts[2];
        heilmittel = Arrays.asList(parts[3].replace(" ", "")
                                           .split(","));
        erfassungsDatum = LocalDate.parse(parts[4],sqlFormat);
    }

    public Behandlung(LocalDate of, String kollege, String unterbrechungsgrund, String heilmittel) {
        this(of,kollege,unterbrechungsgrund,heilmittel,LocalDate.now());

    }

    public Behandlung(LocalDate of, String kollege, String unterbrechungsgrund, String heilmittel, LocalDate erfasstAm) {
        Contracts.require(of != null,"Date musst not be null");

        datum = of;
        this.kollege = kollege;
        unterbrechungsbegruendung = unterbrechungsgrund;
        this.heilmittel = Arrays.asList(heilmittel.replace(" ", "")
                                                  .split(","));
        erfassungsDatum = erfasstAm;
    }

    @Override
    public String toString() {
        return "Behandlung [datum=" + datum + ", kollege=" + kollege + ", unterbrechungsbegruendung="
                + unterbrechungsbegruendung + ", heilmittel=" + heilmittel + ", erfassungsDatum=" + erfassungsDatum
                + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(datum, erfassungsDatum, heilmittel, kollege, unterbrechungsbegruendung);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Behandlung other = (Behandlung) obj;
        return Objects.equals(datum, other.datum) && Objects.equals(erfassungsDatum, other.erfassungsDatum)
                && Objects.equals(heilmittel, other.heilmittel) && Objects.equals(kollege, other.kollege)
                && Objects.equals(unterbrechungsbegruendung, other.unterbrechungsbegruendung);
    }

    @Override
    public int compareTo(Behandlung o) {
        return datum.compareTo(o.datum);
    }

    public List<String> erbrachteHeilmittel() {
        return heilmittel;
    }

     static List<Behandlung> ofDbString(String termine){
        List<Behandlung> liste = new LinkedList<>();
        String[] zeilen = termine.split("\n");
        for (String fromDB : zeilen) {
            liste.add(new Behandlung(fromDB));
        }
        return liste;
    }
}
