package verkauf.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import verkauf.MwSTSatz;

public class VerkaufTest {
    private final class Testartikel extends ArtikelVerkauf {
        @Override
        void update() {
        }
    }

    @Test
    public void verkaufmwstentwirdkorrektberechnet() throws Exception {
        Verkauf verk = new Verkauf();

        ArtikelVerkauf posten = new Testartikel();
        posten.setMwst(MwSTSatz.now().vollerSatz());
        posten.setPreis(100 + MwSTSatz.now().vollerSatz());
        posten.setAnzahl(1);
        verk.fuegeArtikelHinzu(posten );
        assertEquals(MwSTSatz.now().vollerSatz(), verk.getBetrag19() , 0.001);
        ArtikelVerkauf posten2 = new Testartikel();
        posten2.setMwst(MwSTSatz.now().verminderterSatz());
        posten2.setPreis(100 + MwSTSatz.now().verminderterSatz());
        posten2.setAnzahl(1);
        verk.fuegeArtikelHinzu(posten2);
        assertEquals(MwSTSatz.now().verminderterSatz(), verk.getBetrag7() , 0.001);

        verk.fuegeArtikelHinzu(posten2);

        assertEquals(MwSTSatz.now().verminderterSatz()*2, verk.getBetrag7() , 0.001);

    }






}
