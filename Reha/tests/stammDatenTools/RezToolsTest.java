package stammDatenTools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;

import org.junit.Test;

import CommonTools.SqlInfo;
import mandant.IK;
import rezept.Rezept;
import rezept.RezeptDto;
import sql.DatenquellenFactory;

public class RezToolsTest {

    @Test
    public void testPutRezNrGetDisziplin() throws Exception {


        String anytext="anytext";
            assertEquals( "Physio", RezTools.getDisziplinFromRezNr("KG" + anytext));
            assertEquals( "Massage", RezTools.getDisziplinFromRezNr("MA" + anytext));
            assertEquals( "Ergo", RezTools.getDisziplinFromRezNr("ER" + anytext));
            assertEquals( "Logo", RezTools.getDisziplinFromRezNr("LO" + anytext));
            assertEquals( "Reha", RezTools.getDisziplinFromRezNr("RH" + anytext));
            assertEquals( "Podo", RezTools.getDisziplinFromRezNr("PO" + anytext));
            assertEquals( "Rsport", RezTools.getDisziplinFromRezNr("RS" + anytext));
            assertEquals( "Ftrain", RezTools.getDisziplinFromRezNr("FT" + anytext));

    }
    @Test
    public void testRez2Vec() {
        Vector<Vector<String>> rezFromDB = new Vector<Vector<String>>();
        SqlInfo sqlinfo = new SqlInfo();
        IK ik = new IK("123456789");
        Rezept rez = new RezeptDto(ik).byRezeptNr("ER1").orElse(new Rezept());
        try {
            sqlinfo.setConnection(new DatenquellenFactory(ik.digitString()).createConnection());
            rezFromDB = sqlinfo.holeSaetze("verordn","*","rez_nr='ER1'",  Arrays.asList(new String[] {}));
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Vector<Vector<String>> rezFromRez = RezTools.Rez2Vec(rez);
        for(int i=0;i<rezFromRez.get(0).size();i++) {
            assertTrue("Value at " + i + " should be equal",rezFromDB.get(0).get(i).equals(rezFromRez.get(0).get(i)));
        }
        
    }

}
