package hmv;

import java.util.EnumSet;

import core.Disziplin;
import core.Patient;
import mandant.Mandant;
import specs.Contracts;

public class Context {

   public final Mandant mandant;
   public final User user;
   public final EnumSet<Disziplin> disziplinen;
   public final Patient patient;

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
