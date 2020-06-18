package rezept;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import CommonTools.SqlInfo;
import mandant.IK;
import sql.DatenquellenFactory;
import stammDatenTools.RezTools;
import systemEinstellungen.SystemPreislisten;

public class RezeptTest {
    private static RezeptDto rezDto;
    

    @BeforeClass
    public static void initForAllTests() {
        try {
            rezDto = new RezeptDto(new IK("123456789"));
        } catch (Exception e) {
            fail("Need running DB connection for these tests");
        }

    }

    @Test
    public void rezConstructor() {
        Rezept rez = new Rezept();
        assertTrue(rez.getRezNr() == null);
    }
    
    @Test
    public void rezCompareCopied() {
        Rezept rez = rezDto.byRezeptNr("ER1").orElse(new Rezept());
        // make sure we really got a rezept:
        assertTrue(rez.getRezNr() != null && !rez.getRezNr().isEmpty());
        // a deep-copy of Rezept should be same as original
        // (Well, this actually is arguable - it *may* contain other RezNr & RezID
        //      - would we still like them to be same?)
        assertTrue(rez.equals(new Rezept(rez)));
        assertTrue(rez.hashCode() == (new Rezept(rez)).hashCode());
        assertFalse(rez.equals(new Rezept()));
    }
    
    @Test
    public void rezAnzahlTermine() {
        Rezept rez = rezDto.byRezeptNr("ER1").orElse(new Rezept());
        // make sure we really got a rezept:
        assertTrue(rez.getRezNr() != null && !rez.getRezNr().isEmpty());
        assertTrue("Test-Rezept hat ein paar Termine", rez.AnzahlTermineInRezept() > 0);
        rez.setTermine(null);
        assertTrue("Nach dem Loeschen mit null sollten keine mehr drin sein", rez.AnzahlTermineInRezept() == 0);
        rez.setTermine("");
        assertTrue("Nach dem Loeschen mit leer-String sollten keine mehr drin sein", rez.AnzahlTermineInRezept() == 0);
        rez.setTermine("10.03.2020@KA@@54110,54002@2020-03-10");
        assertTrue("Nach dem Setzen sollte genau 1'er drin sein", rez.AnzahlTermineInRezept() == 1);
        assertTrue("Gesetzter Termin sollte dem gesetztem Wert entsprechen",
                    "10.03.2020@KA@@54110,54002@2020-03-10".equals(rez.getTermine()));
    }
    
    /**
     * This test has been cancelled - the current class-fieldnames are too far off from the DB-fieldnames
     * It is planned to re-org the DB (one of these days :D ) - if we keep this test in mind, we *may*
     * end up with compareable names again...
     * Until then, sleep tight ;)
     
    @Test
    public void rezFieldsToDbFieldsTest() {
        String stmt = "describe verordn";

        try {
            ResultSet rs = conn.createStatement()
                               .executeQuery(stmt);
            while (rs.next()) {
                try {
                    String fieldInClass = rs.getString(1).replaceAll("_", "");
                    Field field = Rezept.class.getDeclaredField(rs.getString(1));
                    // System.out.println("Found field " + field + " from DB in Rezepte.");
                } catch (NoSuchFieldException e) {
                    fail("DB field " + rs.getString(1) + " is not in Rezept-fields");
                }
            }
        } catch (SQLException e) {
            fail("Need running DB connection for this test");
            System.out.println("Exception: " + e.getLocalizedMessage());
            e.printStackTrace();
        }

    }
    */
    
}
