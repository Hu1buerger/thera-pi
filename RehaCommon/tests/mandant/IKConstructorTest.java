package mandant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IKConstructorTest {

    @Test
    public final void anyNineDigitPasses() throws Exception {
        new IK("000000000");
        new IK("000000123");
        new IK("000013000");
        new IK("985642132");

    }

    @Test
    public void verify() throws Exception {
        assertFalse(new IK("123456789").isValid());
        assertTrue(new IK("510841109").isValid());
        assertFalse(new IK("510814109").isValid());
    }

    @Test
    public void testName() throws Exception {
        assertFalse(IK.INVALIDIK.isValid());
    }

}
