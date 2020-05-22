package systemEinstellungen;

import static core.Disziplin.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import core.Disziplin;
import mandant.IK;
public class NummernKreisTest {


    @Test (expected = Exception.class)
    public void nichtunterstuetzteDisziplinEX() throws Exception {
        NummernKreis nk = new NummernKreis(new IK("123456789"));
          nk.nextNumber(RS);


    }
    
    @Test 
    public void zweimaligerAufrufholtverschiedeneNUmmern() throws Exception {
        NummernKreis nk = new NummernKreis(new IK("123456789"));
         int ersteNr=   nk.nextNumber(RH);
          int zweiteNr= nk.nextNumber(RH);
          
          assertEquals(zweiteNr, ersteNr+1);


    }

}
