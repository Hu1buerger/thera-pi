package hmv;

import java.util.EnumSet;

import core.Disziplin;
import core.Patient;
import mandant.Mandant;
import specs.Contracts;

public class Context {

    final Mandant mandant;
    final User user;
    final EnumSet<Disziplin> disziplinen;
    final Patient patient;

    public Context(Mandant mandant, User user, Patient patient) {
        super();
        Contracts.require(mandant != null && user != null && mandant.disziplinen() != null && patient != null

                , "no null values!");
        this.mandant = mandant;
        this.user = user;
        this.disziplinen = mandant.disziplinen();
        this.patient = patient;
    }


}
