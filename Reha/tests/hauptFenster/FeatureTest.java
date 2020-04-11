package hauptFenster;

import static org.junit.Assert.*;

import org.junit.Test;

public class FeatureTest {

    @Test
    public void aNewCreatedFeatureIsNotEnabled() throws Exception {
        Feature feature = new Feature("new");
        assertFalse(feature.isEnabled());
    }

    @Test
    public void whenAFeatureIsEnabledAnyFeatureWithTheSameNameIsEnabled() throws Exception {
        Feature originalFeature = new Feature("new");
        originalFeature.enable();
        Feature secondaryFeature = new Feature("new");
        assertTrue(secondaryFeature.isEnabled());

    }

    @Test
    public void featuresCanBeEnabledAndDisabled() throws Exception {
        Feature feature = new Feature("new");
        assertFalse(feature.isEnabled());
        feature.enable();
        assertTrue(feature.isEnabled());
        feature.disable();
        assertFalse(feature.isEnabled());
    }

    @Test
    public void featuresAreCaseinsensitive() throws Exception {

        assertEquals(new Feature("kleinGeschrieben"),new Feature("kleinGESCHRIEBEN"));

    }

}
