
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import office.OOTools;

public class BackslashesTest {

    @Test
    public void path() {
        assertEquals("C:/mimimi/mimim/blob", OOTools.exchangebackslashes("C:\\mimimi\\mimim\\blob"));

    }

}
