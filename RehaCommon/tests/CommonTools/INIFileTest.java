package CommonTools;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import CommonTools.INIFile.INIProperty;

public class INIFileTest {

    private static final int CURRENT_SECTION_COUNT = 7;

    private static final String STRING_SECTION = "Strings";

    private static final String INTEGER_SECTION = "Integers";

    private static final String BOOL_SECTION = "Bools";

    private static final String LONG_SECTION = "Longs";

    private static final String DOUBLE_SECTION = "Doubles";

    private static final String DATE_SECTION = "Dates";

    private static final String TIMESTAMP_SECTION = "Timestamps";

    private Path tempIniTestDirectory;

    private Path tempIniFilePath;

    private URL immutableTestFixture;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() throws IOException {
        this.tempIniFilePath = Files.createTempFile("IniFile", ".ini");
        this.tempIniTestDirectory = Files.createTempDirectory("tempTestPath");

        this.immutableTestFixture = this.getClass()
                                        .getResource("/CommonTools/INIFileTestFixture.ini");
    }

    @Test
    public void testCreateIniFileWithNullValueAsParameterFailsWithException() {
        // Arrange
        thrown.expect(NullPointerException.class);

        // Act
        new INIFile(null);

        // Assert
    }

    @Test
    public void testCreateIniFileWithEmptyStringContentAndNullValueAsParameterFailsWithException() {
        // Arrange
        thrown.expect(NullPointerException.class);

        // Act
        new INIFile("", null);

        // Assert
    }

    @Test
    public void testCreateIniFileWithNullStringContentAndValidFilePathWillBeCreated() {
        // Arrange
        String path = this.tempIniTestDirectory.toString();

        // Act
        INIFile iniFile = new INIFile(null, path);

        // Assert
        assertThat(iniFile, is(notNullValue()));
    }

    @Test
    public void testCreateIniFileWithDirectoryAsFilePath() {
        // Arrange
        String path = this.tempIniTestDirectory.toString();

        // Act
        INIFile iniFile = new INIFile(path);

        // Assert
        assertThat(iniFile, is(notNullValue()));
    }

    @Test
    public void checkLoadIniFileFromString() {
        // Arrange
        String data = "[Integers]" + System.lineSeparator() + "IntegerMax = 2147483647" + System.lineSeparator()
                + "positiveOne = 1" + System.lineSeparator() + "zero = 0" + System.lineSeparator() + "negativeOne = -1"
                + System.lineSeparator() + "IntegerMin = -2147483648" + System.lineSeparator()
                + "; toBig = 2147483647 + 1 --> Integer MaxValue + 1" + System.lineSeparator() + "tobig = 2147483648"
                + System.lineSeparator() + "; negativeToSmall = -2147483648 - 1 --> Integer MinValue + 1"
                + System.lineSeparator() + "negativeToSmall = -2147483649" + System.lineSeparator()
                + "invalid = 15Integer" + System.lineSeparator() + "" + System.lineSeparator() + "[Dates]"
                + System.lineSeparator() + "xmas = 24.12.202" + System.lineSeparator() + "future = 31.12.9999"
                + System.lineSeparator() + "past = 01.01.0001" + System.lineSeparator() + "invalid = 31.13.0000"
                + System.lineSeparator() + "" + System.lineSeparator() + "[Timestamps]" + System.lineSeparator()
                + "xmas = 24.12.2020 20:15:36.654" + System.lineSeparator() + "future = 31.12.9999 23:59:59.999"
                + System.lineSeparator() + "past = 01.01.0001 00:00:00.000" + System.lineSeparator()
                + "invalid = 31.13.0000" + System.lineSeparator();

        // Act
        INIFile iniFile = new INIFile(data, this.tempIniFilePath.toString());

        // Assert
        int allSectionNames = iniFile.getTotalSections();
        assertThat(allSectionNames, is(3));
        Map<String, INIFile.INIProperty> timestamps = iniFile.getProperties("Timestamps");
        assertThat(timestamps.size(), is(4));
        Map<String, INIFile.INIProperty> dates = iniFile.getProperties("Dates");
        assertThat(dates.size(), is(4));
        Map<String, INIFile.INIProperty> integers = iniFile.getProperties("Integers");
        assertThat(integers.size(), is(8));
    }

    @Test
    public void checkSaveToStringWillResultInSameString() {
        // Arrange
        String data = "[Integers]" + System.lineSeparator() + "IntegerMax = 2147483647" + System.lineSeparator()
                + "positiveOne = 1" + System.lineSeparator() + "zero = 0" + System.lineSeparator() + "negativeOne = -1"
                + System.lineSeparator() + "IntegerMin = -2147483648" + System.lineSeparator()
                + ";toBig = 2147483647 + 1 --> Integer MaxValue + 1" + System.lineSeparator() + "tobig = 2147483648"
                + System.lineSeparator() + ";negativeToSmall = -2147483648 - 1 --> Integer MinValue + 1"
                + System.lineSeparator() + "negativeToSmall = -2147483649" + System.lineSeparator()
                + "invalid = 15Integer" + System.lineSeparator() + "" + System.lineSeparator() + "[Dates]"
                + System.lineSeparator() + "xmas = 24.12.202" + System.lineSeparator() + "future = 31.12.9999"
                + System.lineSeparator() + "past = 01.01.0001" + System.lineSeparator() + "invalid = 31.13.0000"
                + System.lineSeparator() + "" + System.lineSeparator() + "[Timestamps]" + System.lineSeparator()
                + "xmas = 24.12.2020 20:15:36.654" + System.lineSeparator() + "future = 31.12.9999 23:59:59.999"
                + System.lineSeparator() + "past = 01.01.0001 00:00:00.000" + System.lineSeparator()
                + "invalid = 31.13.0000" + System.lineSeparator() + System.lineSeparator();

        INIFile iniFile = new INIFile(data, this.tempIniFilePath.toString());

        // Act
        String actual = iniFile.saveToString();


        // Assert
        assertThat(actual, is(data));
    }

    @Test
    public void testINIFileWithArgs() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        // Act
        new INIFile(path);

        // Arrange
    }

    @Test
    public void testGetFileName() {
        // Arrange
        String path = this.tempIniFilePath.toString();

        // Act
        INIFile myIniFile = new INIFile(path);

        // Arrange
        assertEquals(path, myIniFile.getFileName());
    }

    @Test
    public void testGetTotalSections() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        int totalSections = myIniFile.getTotalSections();

        // Assert
        assertThat(totalSections, is(CURRENT_SECTION_COUNT));
    }

    @Test
    public void testGetAllSectionNames() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);
        String[] names = { STRING_SECTION, BOOL_SECTION, INTEGER_SECTION, LONG_SECTION, DOUBLE_SECTION, DATE_SECTION,
                TIMESTAMP_SECTION };

        // Act
        String[] allSectionNames = myIniFile.getAllSectionNames();

        // Assert
        assertArrayEquals(names, allSectionNames);
    }

    @Test
    public void checkThatGettingPropertyOfUnknownSectionWillResultInNullValue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Integer integerProperty = myIniFile.getIntegerProperty("UnKnowsSection", "UnknownProperty");

        // Assert
        assertThat(integerProperty, is(nullValue()));
    }

    @Test
    public void testGetPropertyNames() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        String[] names = { "Strings", BOOL_SECTION, INTEGER_SECTION };
        String[] propertyNames = { "simpleString", "escapedString", "spacyString" };

        // Act
        String[] actual = myIniFile.getPropertyNames(names[0]);

        // Assert
        assertArrayEquals(propertyNames, actual);
    }

    @Test
    public void testRemoveProperty() {
        // Arrange
        INIFile iniFile = new INIFile("");
        iniFile.setStringProperty(LONG_SECTION, "eins", "1", null);
        iniFile.setStringProperty(LONG_SECTION, "zwei", "2", null);

        // Act
        iniFile.removeProperty(LONG_SECTION, "zwei");

        // Assert
        Map<String, INIFile.INIProperty> properties = iniFile.getProperties(LONG_SECTION);
        assertThat(properties.size(), is(1));

        INIFile.INIProperty property = properties.get("eins");
        assertThat(property.getPropValue(), is("1"));

        INIFile.INIProperty notAvailableProperty = properties.get("zwei");
        assertThat(notAvailableProperty, is(is(nullValue())));
    }

    @Test
    public void testRemoveSection() {
        INIFile iniFile = new INIFile("");
        iniFile.addSection(LONG_SECTION, null);
        iniFile.addSection(BOOL_SECTION, null);

        assertTrue(Arrays.asList(iniFile.getAllSectionNames())
                         .contains(LONG_SECTION));
        iniFile.removeSection(LONG_SECTION);

        assertFalse(Arrays.asList(iniFile.getAllSectionNames())
                          .contains(LONG_SECTION));
        assertTrue(Arrays.asList(iniFile.getAllSectionNames())
                         .contains(BOOL_SECTION));

    }

    @Test
    public void testSaveIntoNotExistingFile() throws IOException {
        // Arrange
        File newFile = File.createTempFile("IniFileSaveTest", ".ini");
        boolean deleted = newFile.delete();
        if (!deleted) {
            fail("PreConditions are not ready.");
        }

        INIFile fixture = new INIFile(newFile.getAbsolutePath());
        fixture.setLongProperty(LONG_SECTION, "TEST", Long.MAX_VALUE, "Comments");

        // Act
        fixture.save();

        // Assert
        INIFile actual = new INIFile(newFile.getAbsolutePath());
        Long actualLongValue = actual.getLongProperty(LONG_SECTION, "TEST");
        assertThat(actualLongValue, is(Long.MAX_VALUE));
    }

    @Test
    public void testRenameSection() {
        INIFile iniFile = new INIFile("");
        iniFile.addSection(LONG_SECTION, null);
        assertTrue(Arrays.asList(iniFile.getAllSectionNames())
                         .contains(LONG_SECTION));
        iniFile.renameSection(LONG_SECTION, BOOL_SECTION, null);
        // FIXME: the sections name field is set to the new value , but the hashmap is
        // not updated.
        // assertTrue(Arrays.asList(iniFile.getAllSectionNames()).contains(LONG_SECTION));
        // assertFalse(Arrays.asList(iniFile.getAllSectionNames()).contains(BOOL_SECTION));

    }

    @Test
    public void checkGetEnvironmentVariableForPropertyReference() throws Exception {
        // Arrange

        // add the expected value to the environment.
        Map<String, String> environment = System.getenv(); // this instance is immutable!
        environment = new HashMap<>(environment); // let us create a mutable one ;)
        environment.put("Hans", "Wurst");
        setEnvironment(environment);

        // create the ini file to use
        INIFile iniFile = new INIFile(this.tempIniFilePath.toString());
        iniFile.setStringProperty("CheckThis", "Out", "%Hans%", "We expect to get the JAVA_HOME value.");

        // Act
        String actual = iniFile.getStringProperty("CheckThis", "Out");

        // Assert
        assertThat(actual, is("Wurst"));
    }

    /*
     *** Property access ***
     ***********************/

    /*
     * getStringProperty
     */

    @Test
    public void checkSetUnknownStringPropertyValue() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        String propertyValue = "simpleString";

        // Act
        myIniFile.setStringProperty(sectionKey, propertyKey, propertyValue);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(propertyValue));
        assertThat(property.getComments(), is(INIFile.EMPTY_COMMENTS));
    }

    @Test
    public void checkSetUnknownStringPropertyValueWithComment() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        String comments = "MeaningfulComment";
        String propertyValue = "simpleString";

        // Act
        myIniFile.setStringProperty(sectionKey, propertyKey, propertyValue, comments);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(propertyValue));
        assertThat(property.getComments(), is(comments));
    }

    @Test
    public void checkOverwriteStringPropertyValue() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        String oldPropertyValue = "OldSimpleString";
        String newPropertyValue = "NewSimpleString";
        myIniFile.setStringProperty(sectionKey, propertyKey, oldPropertyValue);

        // Act
        myIniFile.setStringProperty(sectionKey, propertyKey, newPropertyValue);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(newPropertyValue));
        assertThat(property.getComments(), is(INIFile.EMPTY_COMMENTS));
    }

    @Test
    public void checkOverwriteStringPropertyValueWithComment() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        String oldPropertyValue = "OldSimpleString";
        String newPropertyValue = "NewSimpleString";
        String comments = "MeaningfulComment";
        myIniFile.setStringProperty(sectionKey, propertyKey, oldPropertyValue, comments);

        // Act
        myIniFile.setStringProperty(sectionKey, propertyKey, newPropertyValue, comments);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(newPropertyValue));
        assertThat(property.getComments(), is(comments));
    }

    @Test
    public void checkGetWellKnownStringPropertyValue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        String stringProperty = myIniFile.getStringProperty("Strings", "simpleString");

        // Arrange
        assertThat(stringProperty, is("thisissimple"));
    }

    @Test
    public void checkGetUnknownStringPropertyValueWillReturnNullValue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        String stringProperty = myIniFile.getStringProperty("Strings", "UnknownProperty");

        // Arrange
        assertThat(stringProperty, is(nullValue()));
    }

    /*
     * getBooleanProperty
     *
     */

    @Test
    public void checkSetUnknownBooleanPropertyValue() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        boolean propertyValue = true;

        // Act
        myIniFile.setBooleanProperty(sectionKey, propertyKey, propertyValue);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(INIFile.INI_FILE_BOOLEAN_VALUE_STRING_TRUE));
        assertThat(property.getComments(), is(INIFile.EMPTY_COMMENTS));
    }

    @Test
    public void checkSetUnknownBooleanPropertyValueWithComment() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        String comments = "MeaningfulComment";
        boolean propertyValue = true;

        // Act
        myIniFile.setBooleanProperty(sectionKey, propertyKey, propertyValue, comments);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(INIFile.INI_FILE_BOOLEAN_VALUE_STRING_TRUE));
        assertThat(property.getComments(), is(comments));
    }

    @Test
    public void checkOverwriteBooleanPropertyValue() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        myIniFile.setBooleanProperty(sectionKey, propertyKey, true);

        // Act
        myIniFile.setBooleanProperty(sectionKey, propertyKey, false);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(INIFile.INI_FILE_BOOLEAN_VALUE_STRING_FALSE));
        assertThat(property.getComments(), is(INIFile.EMPTY_COMMENTS));
    }

    @Test
    public void checkOverwriteBooleanPropertyValueWithComment() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        String comments = "MeaningfulComment";
        myIniFile.setBooleanProperty(sectionKey, propertyKey, false, comments);

        // Act
        myIniFile.setBooleanProperty(sectionKey, propertyKey, true, comments);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(INIFile.INI_FILE_BOOLEAN_VALUE_STRING_TRUE));
        assertThat(property.getComments(), is(comments));
    }

    @Test
    public void checkGetWellKnownBooleanPropertyValue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        boolean booleanProperty = myIniFile.getBooleanProperty(BOOL_SECTION, "rightUpperCase");

        // Arrange
        assertThat(booleanProperty, is(true));
    }

    @Test
    public void checkGetUnknownBooleanPropertyValueWillReturnNullValue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        boolean booleanProperty = myIniFile.getBooleanProperty(BOOL_SECTION, "UnknownProperty");

        // Arrange
        assertThat(booleanProperty, is(false));
    }

    @Test
    public void checkAllBooleanTruePropertyValueWillReturnTrue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        boolean rightUpperCaseProperty = myIniFile.getBooleanProperty(BOOL_SECTION, "rightUpperCase");
        boolean rightLowerCaseProperty = myIniFile.getBooleanProperty(BOOL_SECTION, "rightLowerCase");
        boolean rightCamelCaseProperty = myIniFile.getBooleanProperty(BOOL_SECTION, "rightCamelCase");
        boolean rightAsIntegerProperty = myIniFile.getBooleanProperty(BOOL_SECTION, "rightAsInteger");
        boolean rightStringEnglishUpperCaseProperty = myIniFile.getBooleanProperty(BOOL_SECTION,
                "rightStringEnglishUpperCase");
        boolean rightStringEnglishLowerCaseProperty = myIniFile.getBooleanProperty(BOOL_SECTION,
                "rightStringEnglishLowerCase");
        boolean rightStringEnglishCamelCaseProperty = myIniFile.getBooleanProperty(BOOL_SECTION,
                "rightStringEnglishCamelCase");

        // Assert
        assertThat(rightUpperCaseProperty, is(true));
        assertThat(rightLowerCaseProperty, is(true));
        assertThat(rightCamelCaseProperty, is(true));
        assertThat(rightAsIntegerProperty, is(true));
        assertThat(rightStringEnglishUpperCaseProperty, is(true));
        assertThat(rightStringEnglishLowerCaseProperty, is(true));
        assertThat(rightStringEnglishCamelCaseProperty, is(true));

    }

    @Test
    public void checkAllBooleanFalsePropertyValueWillReturnFalse() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        boolean wrongUpperCaseProperty = myIniFile.getBooleanProperty(BOOL_SECTION, "wrongUpperCase");
        boolean wrongLowerCaseProperty = myIniFile.getBooleanProperty(BOOL_SECTION, "wrongLowerCase");
        boolean wrongCamelCaseProperty = myIniFile.getBooleanProperty(BOOL_SECTION, "wrongCamelCase");
        boolean wrongAsIntegerProperty = myIniFile.getBooleanProperty(BOOL_SECTION, "wrongAsInteger");
        boolean wrongStringEnglishUpperCaseProperty = myIniFile.getBooleanProperty(BOOL_SECTION,
                "wrongStringEnglishUpperCase");
        boolean wrongStringEnglishLowerCaseProperty = myIniFile.getBooleanProperty(BOOL_SECTION,
                "wrongStringEnglishLowerCase");
        boolean wrongStringEnglishCamelCaseProperty = myIniFile.getBooleanProperty(BOOL_SECTION,
                "wrongStringEnglishCamelCase");

        // Assert
        assertThat(wrongUpperCaseProperty, is(false));
        assertThat(wrongLowerCaseProperty, is(false));
        assertThat(wrongCamelCaseProperty, is(false));
        assertThat(wrongAsIntegerProperty, is(false));
        assertThat(wrongStringEnglishUpperCaseProperty, is(false));
        assertThat(wrongStringEnglishLowerCaseProperty, is(false));
        assertThat(wrongStringEnglishCamelCaseProperty, is(false));
    }

    /*
     * getIntegerProperty
     */

    @Test
    public void checkSetUnknownIntegerPropertyValue() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        int propertyValue = 1;

        // Act
        myIniFile.setIntegerProperty(sectionKey, propertyKey, propertyValue);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(Integer.toString(propertyValue)));
        assertThat(property.getComments(), is(INIFile.EMPTY_COMMENTS));
    }

    @Test
    public void checkSetUnknownIntegerPropertyValueWithComment() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        String comments = "MeaningfulComment";
        int propertyValue = Integer.MIN_VALUE;

        // Act
        myIniFile.setIntegerProperty(sectionKey, propertyKey, propertyValue, comments);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(Integer.toString(Integer.MIN_VALUE)));
        assertThat(property.getComments(), is(comments));
    }

    @Test
    public void checkOverwriteIntegerPropertyValue() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        int oldPropertyValue = Integer.MIN_VALUE;
        int newPropertyValue = Integer.MAX_VALUE;
        myIniFile.setIntegerProperty(sectionKey, propertyKey, oldPropertyValue);

        // Act
        myIniFile.setIntegerProperty(sectionKey, propertyKey, newPropertyValue);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(Integer.toString(Integer.MAX_VALUE)));
        assertThat(property.getComments(), is(INIFile.EMPTY_COMMENTS));
    }

    @Test
    public void checkOverwriteIntegerPropertyValueWithComment() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        int oldPropertyValue = Integer.MIN_VALUE;
        int newPropertyValue = Integer.MAX_VALUE;
        String comments = "MeaningfulComment";
        myIniFile.setIntegerProperty(sectionKey, propertyKey, oldPropertyValue, comments);

        // Act
        myIniFile.setIntegerProperty(sectionKey, propertyKey, newPropertyValue, comments);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(Integer.toString(Integer.MAX_VALUE)));
        assertThat(property.getComments(), is(comments));
    }

    @Test
    public void checkGetWellKnownIntegerPropertyValue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Integer integerProperty = myIniFile.getIntegerProperty(INTEGER_SECTION, "IntegerMax");

        // Arrange
        assertThat(integerProperty, is(Integer.MAX_VALUE));
    }

    @Test
    public void checkGetUnknownIntegerPropertyValueWillReturnNullValue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Integer integerProperty = myIniFile.getIntegerProperty(INTEGER_SECTION, "UnknownProperty");

        // Arrange
        assertThat(integerProperty, is(nullValue()));
    }

    @Test
    public void testIntegerPropertyRange() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Integer integerMax = myIniFile.getIntegerProperty(INTEGER_SECTION, "IntegerMax");
        Integer positiveOne = myIniFile.getIntegerProperty(INTEGER_SECTION, "positiveOne");
        Integer zero = myIniFile.getIntegerProperty(INTEGER_SECTION, "zero");
        Integer negativeOne = myIniFile.getIntegerProperty(INTEGER_SECTION, "negativeOne");
        Integer integerMin = myIniFile.getIntegerProperty(INTEGER_SECTION, "IntegerMin");
        Integer tobig = myIniFile.getIntegerProperty(INTEGER_SECTION, "tobig");
        Integer negativeToSmall = myIniFile.getIntegerProperty(INTEGER_SECTION, "negativeToSmall");
        Integer invalid = myIniFile.getIntegerProperty(INTEGER_SECTION, "invalid");

        // Assert
        assertThat(integerMax, is(Integer.MAX_VALUE));
        assertThat(positiveOne, is(1));
        assertThat(zero, is(0));
        assertThat(negativeOne, is(-1));
        assertThat(integerMin, is(Integer.MIN_VALUE));
        assertThat(tobig, is(nullValue()));
        assertThat(negativeToSmall, is(nullValue()));
        assertThat(invalid, is(nullValue()));
    }

    /*
     * getLongProperty
     */

    @Test
    public void checkSetUnknownLongPropertyValue() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        long propertyValue = 1L;

        // Act
        myIniFile.setLongProperty(sectionKey, propertyKey, propertyValue);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(Long.toString(propertyValue)));
        assertThat(property.getComments(), is(INIFile.EMPTY_COMMENTS));
    }

    @Test
    public void checkSetUnknownLongPropertyValueWithComment() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        String comments = "MeaningfulComment";
        long propertyValue = Long.MIN_VALUE;

        // Act
        myIniFile.setLongProperty(sectionKey, propertyKey, propertyValue, comments);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(Long.toString(Long.MIN_VALUE)));
        assertThat(property.getComments(), is(comments));
    }

    @Test
    public void checkOverwriteLongPropertyValue() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        long oldPropertyValue = Long.MIN_VALUE;
        long newPropertyValue = Long.MAX_VALUE;
        myIniFile.setLongProperty(sectionKey, propertyKey, oldPropertyValue);

        // Act
        myIniFile.setLongProperty(sectionKey, propertyKey, newPropertyValue);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(Long.toString(Long.MAX_VALUE)));
        assertThat(property.getComments(), is(INIFile.EMPTY_COMMENTS));
    }

    @Test
    public void checkOverwriteLongPropertyValueWithComment() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        long oldPropertyValue = Long.MIN_VALUE;
        long newPropertyValue = Long.MAX_VALUE;
        String comments = "MeaningfulComment";
        myIniFile.setLongProperty(sectionKey, propertyKey, oldPropertyValue, comments);

        // Act
        myIniFile.setLongProperty(sectionKey, propertyKey, newPropertyValue, comments);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(Long.toString(Long.MAX_VALUE)));
        assertThat(property.getComments(), is(comments));
    }

    @Test
    public void checkGetWellKnownLongPropertyValue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Long integerProperty = myIniFile.getLongProperty(LONG_SECTION, "LongMax");

        // Arrange
        assertThat(integerProperty, is(Long.MAX_VALUE));
    }

    @Test
    public void checkGetUnknownLongPropertyValueWillReturnNullValue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Integer integerProperty = myIniFile.getIntegerProperty(LONG_SECTION, "UnknownProperty");

        // Arrange
        assertThat(integerProperty, is(nullValue()));
    }

    @Test
    public void testLongPropertyRange() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Long longMax = myIniFile.getLongProperty(LONG_SECTION, "LongMax");
        Long positiveOne = myIniFile.getLongProperty(LONG_SECTION, "positiveOne");
        Long zero = myIniFile.getLongProperty(LONG_SECTION, "zero");
        Long negativeOne = myIniFile.getLongProperty(LONG_SECTION, "negativeOne");
        Long longMin = myIniFile.getLongProperty(LONG_SECTION, "LongMin");

        // Assert
        assertThat(longMax, is(Long.MAX_VALUE));
        assertThat(positiveOne, is(1L));
        assertThat(zero, is(0L));
        assertThat(negativeOne, is(-1L));
        assertThat(longMin, is(Long.MIN_VALUE));
    }

    @Test
    public void testLongPropertyOutOfRange() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Long tobig = myIniFile.getLongProperty(LONG_SECTION, "tobig");
        Long negativeToSmall = myIniFile.getLongProperty(LONG_SECTION, "negativeToSmall");
        Long invalid = myIniFile.getLongProperty(LONG_SECTION, "invalid");

        // Assert
        assertThat(tobig, is(nullValue()));
        assertThat(negativeToSmall, is(nullValue()));
        assertThat(invalid, is(nullValue()));
    }

    /*
     * getDoubleProperty
     */
    @Test
    public void checkSetUnknownDoublePropertyValue() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        double propertyValue = 1.2;

        // Act
        myIniFile.setDoubleProperty(sectionKey, propertyKey, propertyValue);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(Double.toString(propertyValue)));
        assertThat(property.getComments(), is(INIFile.EMPTY_COMMENTS));
    }

    @Test
    public void checkSetUnknownDoublePropertyValueWithComment() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        String comments = "MeaningfulComment";
        double propertyValue = Double.MIN_VALUE;

        // Act
        myIniFile.setDoubleProperty(sectionKey, propertyKey, propertyValue, comments);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(Double.toString(Double.MIN_VALUE)));
        assertThat(property.getComments(), is(comments));
    }

    @Test
    public void checkOverwriteDoublePropertyValue() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        double oldPropertyValue = Double.MIN_VALUE;
        double newPropertyValue = Double.MAX_VALUE;
        myIniFile.setDoubleProperty(sectionKey, propertyKey, oldPropertyValue);

        // Act
        myIniFile.setDoubleProperty(sectionKey, propertyKey, newPropertyValue);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(Double.toString(Double.MAX_VALUE)));
        assertThat(property.getComments(), is(INIFile.EMPTY_COMMENTS));
    }

    @Test
    public void checkOverwriteDoublePropertyValueWithComment() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        double oldPropertyValue = Double.MIN_VALUE;
        double newPropertyValue = Double.MAX_VALUE;
        String comments = "MeaningfulComment";
        myIniFile.setDoubleProperty(sectionKey, propertyKey, oldPropertyValue, comments);

        // Act
        myIniFile.setDoubleProperty(sectionKey, propertyKey, newPropertyValue, comments);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is(Double.toString(Double.MAX_VALUE)));
        assertThat(property.getComments(), is(comments));
    }

    @Test
    public void checkGetWellKnownDoublePropertyValue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Double integerProperty = myIniFile.getDoubleProperty(DOUBLE_SECTION, "DoubleMax");

        // Arrange
        assertThat(integerProperty, is(Double.MAX_VALUE));
    }

    @Test
    public void checkGetUnknownDoublesPropertyValueWillReturnNullValue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Double integerProperty = myIniFile.getDoubleProperty(DOUBLE_SECTION, "UnknownProperty");

        // Arrange
        assertThat(integerProperty, is(nullValue()));
    }

    @Test
    public void testDoublePropertyRange() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Double doubleMax = myIniFile.getDoubleProperty(DOUBLE_SECTION, "DoubleMax");
        Double positiveOne = myIniFile.getDoubleProperty(DOUBLE_SECTION, "positiveOne");
        Double positiveFraction = myIniFile.getDoubleProperty(DOUBLE_SECTION, "positiveFraction");
        Double zero = myIniFile.getDoubleProperty(DOUBLE_SECTION, "zero");
        Double negativeFraction = myIniFile.getDoubleProperty(DOUBLE_SECTION, "negativeFraction");
        Double negativeOne = myIniFile.getDoubleProperty(DOUBLE_SECTION, "negativeOne");
        Double doubleMin = myIniFile.getDoubleProperty(DOUBLE_SECTION, "DoubleMin");

        // Assert
        assertThat(doubleMax, is(Double.MAX_VALUE));
        assertThat(positiveFraction, is(0.5));
        assertThat(positiveOne, is(1.0));
        assertThat(zero, is(0.0));
        assertThat(negativeFraction, is(-0.5));
        assertThat(negativeOne, is(-1.0));
        assertThat(doubleMin, is(Double.MIN_VALUE));
    }

    @Test
    public void testDoublePropertyOutOfRange() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Double tobig = myIniFile.getDoubleProperty(DOUBLE_SECTION, "tobig");
        Double invalid = myIniFile.getDoubleProperty(DOUBLE_SECTION, "invalid");

        // Assert
        assertThat(tobig, is(nullValue()));
        assertThat(invalid, is(nullValue()));
    }

    /*
     * getDateProperty
     *
     */

    @Test
    public void checkSetUnknownDatePropertyValue() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        Timestamp propertyValue = Timestamp.valueOf("2020-1-1 13:15:01.123");

        // Act
        myIniFile.setTimestampProperty(sectionKey, propertyKey, propertyValue);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is("01.01.2020 13:15:01"));
        assertThat(property.getComments(), is(INIFile.EMPTY_COMMENTS));
    }

    @Test
    public void checkSetUnknownDatePropertyValueWithComment() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        String comments = "MeaningfulComment";
        LocalDate propertyValue = LocalDate.of(2020, 01, 01);

        // Act
        myIniFile.setDateProperty(sectionKey, propertyKey, propertyValue, comments);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is("01.01.2020"));
        assertThat(property.getComments(), is(comments));
    }

    @Test
    public void checkOverwriteDatePropertyValue() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        LocalDate oldPropertyValue = LocalDate.of(2019, 12, 31);
        LocalDate newPropertyValue = LocalDate.of(2020, 01, 01);
        myIniFile.setDateProperty(sectionKey, propertyKey, oldPropertyValue);

        // Act
        myIniFile.setDateProperty(sectionKey, propertyKey, newPropertyValue);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is("01.01.2020"));
        assertThat(property.getComments(), is(INIFile.EMPTY_COMMENTS));
    }

    @Test
    public void checkOverwriteDatePropertyValueWithComment() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        LocalDate oldPropertyValue = LocalDate.of(2019, 12, 31);
        LocalDate newPropertyValue = LocalDate.of(2020, 01, 01);
        myIniFile.setDateProperty(sectionKey, propertyKey, oldPropertyValue);
        String comments = "MeaningfulComment";
        myIniFile.setDateProperty(sectionKey, propertyKey, oldPropertyValue);

        // Act
        myIniFile.setDateProperty(sectionKey, propertyKey, newPropertyValue, comments);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is("01.01.2020"));
        assertThat(property.getComments(), is(comments));
    }

    @Test
    public void checkGetWellKnownDatePropertyValue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        LocalDate property = myIniFile.getDateProperty(DATE_SECTION, "xmas");

        // Arrange
        assertThat(property, is(LocalDate.parse("2020-12-24")));
    }

    @Test
    public void checkGetUnknownDatePropertyValueWillReturnNullValue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Double integerProperty = myIniFile.getDoubleProperty(DATE_SECTION, "UnknownProperty");

        // Arrange
        assertThat(integerProperty, is(nullValue()));
    }

    @Test
    public void testDatePropertyRange() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        LocalDate future = myIniFile.getDateProperty(DATE_SECTION, "future");
        LocalDate past = myIniFile.getDateProperty(DATE_SECTION, "past");

        // Assert
        LocalDate expectedFuture = LocalDate.parse("9999-12-31");
        assertThat(future, is(expectedFuture));
        LocalDate expectedPast = LocalDate.parse("0001-01-01");
        assertThat(past, is(expectedPast));
    }

    @Test
    public void testDatePropertyOutOfRange() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Double invalid = myIniFile.getDoubleProperty(DATE_SECTION, "invalid");

        // Assert
        assertThat(invalid, is(nullValue()));
    }

    @Test
    public void checkSetValidDateFormatWillBeAccepted() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);
        myIniFile.setDateFormat("dd-yyyy-MM");
        LocalDate timestamp = LocalDate.parse("2020-12-31");

        // Act
        myIniFile.setDateProperty(DATE_SECTION, "OtherFormat", timestamp);

        // Assert
        Map<String, INIFile.INIProperty> properties = myIniFile.getProperties(DATE_SECTION);
        INIFile.INIProperty property = properties.get("OtherFormat");

        assertThat(property.getPropValue(), is("31-2020-12"));
    }

    @Test
    public void checkSetInvalidDateFormatWillThrowInvalidArgumentException() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        thrown.expect(IllegalArgumentException.class);

        // Act
        myIniFile.setDateFormat("AbCdEf");

        // Assert
    }

    /*
     *
     * getTimestampProperty
     *
     */
    @Test
    public void checkSetUnknownTimestampPropertyValue() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        Timestamp propertyValue = Timestamp.valueOf("2020-1-1 13:15:01.123");

        // Act
        myIniFile.setTimestampProperty(sectionKey, propertyKey, propertyValue);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is("01.01.2020 13:15:01"));
        assertThat(property.getComments(), is(INIFile.EMPTY_COMMENTS));
    }

    @Test
    public void checkSetUnknownTimestampPropertyValueWithComment() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        String comments = "MeaningfulComment";
        Timestamp propertyValue = Timestamp.valueOf("2020-1-1 13:15:01.123");

        // Act
        myIniFile.setTimestampProperty(sectionKey, propertyKey, propertyValue, comments);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is("01.01.2020 13:15:01"));
        assertThat(property.getComments(), is(comments));
    }

    @Test
    public void checkOverwriteTimestampPropertyValue() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        Timestamp oldPropertyValue = Timestamp.valueOf("2019-12-31 23:59:59.999");
        Timestamp newPropertyValue = Timestamp.valueOf("2020-1-1 13:15:01.123");
        myIniFile.setTimestampProperty(sectionKey, propertyKey, oldPropertyValue);

        // Act
        myIniFile.setTimestampProperty(sectionKey, propertyKey, newPropertyValue);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is("01.01.2020 13:15:01"));
        assertThat(property.getComments(), is(INIFile.EMPTY_COMMENTS));
    }

    @Test
    public void checkOverwriteTimestampPropertyValueWithComment() {
        // Arrange
        String path = this.tempIniFilePath.toString();
        INIFile myIniFile = new INIFile(path);
        String sectionKey = "SectionKey";
        String propertyKey = "PropertyKey";
        Timestamp oldPropertyValue = Timestamp.valueOf("2019-12-31 23:59:59.999");
        Timestamp newPropertyValue = Timestamp.valueOf("2020-01-01 13:15:01.123");
        myIniFile.setTimestampProperty(sectionKey, propertyKey, oldPropertyValue);
        String comments = "MeaningfulComment";
        myIniFile.setTimestampProperty(sectionKey, propertyKey, oldPropertyValue);

        // Act
        myIniFile.setTimestampProperty(sectionKey, propertyKey, newPropertyValue, comments);

        // Arrange
        Map<String, INIFile.INIProperty> propertiesInSection = myIniFile.getProperties(sectionKey);
        assertThat(propertiesInSection.size(), is(1));

        INIFile.INIProperty property = propertiesInSection.get(propertyKey);
        assertThat(property.getPropValue(), is("01.01.2020 13:15:01"));
        assertThat(property.getComments(), is(comments));
    }

    @Test
    public void checkGetWellKnownTimestampPropertyValue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Timestamp property = myIniFile.getTimestampProperty(TIMESTAMP_SECTION, "xmas");

        // Arrange
        assertThat(property, is(Timestamp.valueOf("2020-12-24 20:15:36")));
    }

    @Test
    public void checkGetUnknownTimestampPropertyValueWillReturnNullValue() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Timestamp property = myIniFile.getTimestampProperty(TIMESTAMP_SECTION, "UnknownProperty");

        // Arrange
        assertThat(property, is(nullValue()));
    }

    @Test
    public void testTimestampPropertyRange() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Timestamp future = myIniFile.getTimestampProperty(TIMESTAMP_SECTION, "future");
        Timestamp past = myIniFile.getTimestampProperty(TIMESTAMP_SECTION, "past");

        // Assert
        Timestamp expectedFuture = Timestamp.valueOf("9999-12-31 23:59:59");
        assertThat(future, is(expectedFuture));
        Timestamp expectedPast = Timestamp.valueOf("0001-01-01 00:00:00");
        assertThat(past, is(expectedPast));
    }

    @Test
    public void testTimestampPropertyOutOfRange() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Timestamp invalid = myIniFile.getTimestampProperty(TIMESTAMP_SECTION, "invalid");

        // Assert
        assertThat(invalid, is(nullValue()));
    }

    @Test
    public void checkSetValidTimeFormatWillBeAccepted() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);
        myIniFile.setTimeStampFormat("dd-yyyy-MM");
        Timestamp timestamp = Timestamp.valueOf("2020-12-31 23:59:59");

        // Act
        myIniFile.setTimestampProperty(TIMESTAMP_SECTION, "OtherFormat", timestamp);

        // Assert
        Map<String, INIFile.INIProperty> properties = myIniFile.getProperties(TIMESTAMP_SECTION);
        INIFile.INIProperty property = properties.get("OtherFormat");

        assertThat(property.getPropValue(), is("31-2020-12"));
    }

    @Test
    public void checkSetInvalidTimeFormatWillThrowInvalidArgumentException() {
        // Arrange
        String path = this.immutableTestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        thrown.expect(IllegalArgumentException.class);

        // Act
        myIniFile.setTimeStampFormat("AbCdEf");

        // Assert
    }
    
    @Test
    public void checkThatAlsoBase64EncodingsAreDetectedAsValue() {
        // Arrange
        URL base64TestFixture = this.getClass()
                                        .getResource("/CommonTools/INIFileWithBase64Encodings.ini");
        String path = base64TestFixture.getPath();
        INIFile myIniFile = new INIFile(path);

        // Act
        Map<String, INIProperty> actual = myIniFile.getProperties("TheraPiUpdates");

        // Assert
        assertThat(actual.get("UpdateFTP").getPropValue(), is("Gj3STm7R9Ks3qHLtXmFrZFxri79dUczQM/Evc1qS6upPZ3KgvBuLUA=="));
        assertThat(actual.get("UpdateVerzeichnis").getPropValue(), is("iqVqwCnLGSk="));
        assertThat(actual.get("UpdateUser").getPropValue(), is("oL5FtuHgYytvnfb0Fp1sDj+FxXrZmsPNsUc0gpUjj9wFE02sP0V4TA=="));
        assertThat(actual.get("UpdatePasswd").getPropValue(), is("PIWlJzWmjEqkz9GTgF0E5GsA/6yg+uG+sOPkuYy2I5ois0fbxkoVXw=="));
        assertThat(actual.get("UpdateEntwickler").getPropValue(), is("1"));
        assertThat(actual.get("UpdateChecken").getPropValue(), is("1"));
        assertThat(actual.get("DummyPw").getPropValue(), is("GDreeSTHhAR+O8iyrBQy9Z60ARpJHL8WlBEL8GSa4U63/hrzTjoWy8PXLiuiZ3n6"));
    }

    /*
     * ************** *** HELPER ***
     **************/

    /**
     * Set an environment variable with some magic like reflection...
     *
     * @param newEnvironment The environment variables to set for the current
     *                       process!
     * @throws Exception when ever something is going wrong in here!
     */
    private void setEnvironment(Map<String, String> newEnvironment) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newEnvironment);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField(
                    "theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newEnvironment);
        } catch (NoSuchFieldException e) {
            Class<?>[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class<?> cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newEnvironment);
                }
            }
        }
    }
}
