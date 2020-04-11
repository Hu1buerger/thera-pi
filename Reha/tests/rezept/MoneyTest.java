package rezept;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class MoneyTest {

    @Test
    public void moneyContructorfromdouble() throws Exception {
        new Money(0.126);
        new Money(-0.126);
        new Money(123456.789);
        new Money(Double.MAX_VALUE - 1);
        new Money(Long.MAX_VALUE);
        new Money(-Double.MAX_VALUE);
        new Money("1234567890123456789012345678901234567890");
        // no exception yay!
    }

    @Test
    public void moneysWithsameAmountareEqual() throws Exception {
        assertEquals(new Money(1.00), new Money(1.00));
        assertEquals(new Money(1), new Money(1.0));
        assertEquals(new Money(1.00), new Money(1.0023));
        assertEquals(new Money(1.00), new Money(1.009));
        assertEquals(new Money(-1.00), new Money(-1.00));

    }

    @Test
    public void moneysWithsameAmountAndOpposingSignumAreNotEqual() throws Exception {
        assertNotEquals(new Money(1.00), new Money(-1.00));
    }

    @Test
    public void moneycanbeadded() throws Exception {

        Money money1_67 = new Money(1.67);
        Money money1_68 = new Money(1.68);
        Money money3_35 = new Money(3.35);
        Money money_neg1_68 = new Money(-1.68);

        assertEquals(money3_35, money1_67.add(money1_68));
        assertEquals(new Money(-0.01), money1_67.add(money_neg1_68));
        Money precise = new Money("1234567890123456789012345678901234567890");
        assertEquals(new Money("2469135780246913578024691357802469135780"), precise.add(precise));

    }

}
