package abrechnung.privat;

import static org.junit.Assert.*;

import org.junit.Test;

public class PreisanwendenStrategieTest {
@Test
public void testexamine() throws Exception {
    /** Alle alt, alle neu, splitten. */
  //preisanwenden[0] = false;
  //preisanwenden[1] = true;
  //preisanwenden[2] = false;
    boolean[] allealt = {true,false,false};
    boolean[] alleneu = {false,true,false};
    boolean[] splitten = {false,false,true};

   assertSame(PreisanwendenStrategie.alleAlt, PreisanwendenStrategie.examine(allealt ));
   assertSame(PreisanwendenStrategie.alleNeu, PreisanwendenStrategie.examine(alleneu ));
   assertSame(PreisanwendenStrategie.splitten, PreisanwendenStrategie.examine(splitten ));
}


}
