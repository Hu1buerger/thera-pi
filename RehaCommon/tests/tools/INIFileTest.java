package tools;

import static org.junit.Assert.*;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import CommonTools.INIFile;





public class INIFileTest {

	private static final int Current_Section_count = 6;
    private static final String INTEGER_SECTION = "Integer";
	private static final String BOOL_SECTION = "Bools";
	private static final String LONG_SECTION = "Longs";
	private static final String DOUBLE_SECTION = "Doubles";
    private static final String DATE_SECTION = "Dates";
	private static final String TEST_RESOURCES_DIR = "tests/resources/";
	private static final String TEST_RESOURCES_INIFILE_INI = TEST_RESOURCES_DIR +"IniFile.ini";


	@Test
	public void testINIFile() {
		new INIFile(null);
		//XXX: fails silently when File not found
		new INIFile(TEST_RESOURCES_DIR);
		//XXX: fails silently when File is in fact a directory
	}

	@Test
	public void testINIFilewithArgs() {

		new INIFile(TEST_RESOURCES_INIFILE_INI);
	}

	@Test
	public void testGetFileName() {
		INIFile myIniFile = new INIFile(TEST_RESOURCES_INIFILE_INI);
		assertEquals(TEST_RESOURCES_INIFILE_INI, myIniFile.getFileName());
	}

	@Test
	public void testGetStringProperty() {
		INIFile myIniFile = new INIFile(TEST_RESOURCES_INIFILE_INI);
		assertEquals("thisissimple", myIniFile.getStringProperty("Strings", "simpleString"));
		assertEquals("spaces in String", myIniFile.getStringProperty("Strings", "spacyString"));

	}

	@Test
	public void testGetBooleanProperty() {
		INIFile myIniFile = new INIFile(TEST_RESOURCES_INIFILE_INI);
		assertFalse("Non existing value is returned as FALSE", myIniFile.getBooleanProperty("Verzeichnisse", "Fahrdienstrohdatei").booleanValue());
		assertTrue("true is returned as TRUE", myIniFile.getBooleanProperty(BOOL_SECTION, "right").booleanValue());
		assertFalse("false is returned as FALSE", myIniFile.getBooleanProperty(BOOL_SECTION, "wrong").booleanValue());
		assertFalse("invalid value is returned as false", myIniFile.getBooleanProperty(BOOL_SECTION, "invalid").booleanValue());
		assertTrue("1 is returned as TRUE", myIniFile.getBooleanProperty(BOOL_SECTION, "one").booleanValue());
		assertFalse("0 is returned as FALSE", myIniFile.getBooleanProperty(BOOL_SECTION, "none").booleanValue());
		assertFalse("5 is returned as FALSE", myIniFile.getBooleanProperty(BOOL_SECTION, "five").booleanValue());
	}

	@Test
	public void testIntegerProperty() {
		INIFile myIniFile = new INIFile(TEST_RESOURCES_INIFILE_INI);
		assertEquals( Integer.valueOf(1), myIniFile.getIntegerProperty(INTEGER_SECTION, "one"));
		assertEquals( Integer.valueOf(2), myIniFile.getIntegerProperty(INTEGER_SECTION, "two"));
		assertEquals( Integer.valueOf(-2134), myIniFile.getIntegerProperty(INTEGER_SECTION, "negativ"));

		assertEquals( null, myIniFile.getIntegerProperty(INTEGER_SECTION, "invalid"));
		assertEquals( null, myIniFile.getIntegerProperty(INTEGER_SECTION, "toobig"));

		myIniFile.setIntegerProperty(INTEGER_SECTION, "vierganze", 4, null);
	}

	@Test
	public void testLongProperty() {

		INIFile myIniFile = new INIFile(TEST_RESOURCES_INIFILE_INI);
		assertEquals( Long.valueOf(1), myIniFile.getLongProperty(LONG_SECTION, "one"));
		assertEquals( Long.valueOf(2), myIniFile.getLongProperty(LONG_SECTION, "two"));
		assertEquals( Long.valueOf(-2134), myIniFile.getLongProperty(LONG_SECTION, "negativ"));

		assertEquals( null, myIniFile.getLongProperty(LONG_SECTION, "toobig"));
        assertEquals( null, myIniFile.getLongProperty(LONG_SECTION, "invalid"));


        myIniFile.setLongProperty(LONG_SECTION, "15lange", 15L, null);
        assertEquals( Long.valueOf(15L), myIniFile.getLongProperty(LONG_SECTION, "15lange"));
	}

	@Test
	public void testDoubleProperty() {
		INIFile myIniFile = new INIFile(TEST_RESOURCES_INIFILE_INI);

		assertEquals( Double.valueOf(1), myIniFile.getDoubleProperty(DOUBLE_SECTION, "one"));
		assertEquals( Double.valueOf(2), myIniFile.getDoubleProperty(DOUBLE_SECTION, "two"));
		assertEquals( Double.valueOf(-6.3), myIniFile.getDoubleProperty(DOUBLE_SECTION, "negativ"));
		assertEquals( Double.valueOf(0.5), myIniFile.getDoubleProperty(DOUBLE_SECTION, "osomething"));
	    assertEquals( Double.valueOf(0.8), myIniFile.getDoubleProperty(DOUBLE_SECTION, "decimalonly"));
	    assertEquals( Double.valueOf(-0.7), myIniFile.getDoubleProperty(DOUBLE_SECTION, "negativDecimalonly"));

	    assertEquals( null, myIniFile.getDoubleProperty(DOUBLE_SECTION, "invalid"));

	    myIniFile.setDoubleProperty(DOUBLE_SECTION, "13einhalb", 13.5,null);
	    assertEquals( Double.valueOf(13.5), myIniFile.getDoubleProperty(DOUBLE_SECTION, "13einhalb"));

	}

	@Test @Ignore("the method is not used in project")
	public void testGetTimestampProperty() {
		//the method is not used in project
	}





	@Test
	public void testSetBooleanProperty() {
		INIFile myIniFile = new INIFile(TEST_RESOURCES_INIFILE_INI);

		myIniFile.setBooleanProperty(DOUBLE_SECTION, "wahr", true, null);
		myIniFile.setBooleanProperty(DOUBLE_SECTION, "falsch", false, null);
		assertTrue("true is returned as TRUE", myIniFile.getBooleanProperty(DOUBLE_SECTION, "wahr").booleanValue());
		assertFalse("false is returned as FALSE", myIniFile.getBooleanProperty(DOUBLE_SECTION, "falsch").booleanValue());

	}

	@Test
	public void testSetDateProperty() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
		LocalDate ldateIn = LocalDate.parse("2013.12.31", formatter);
		LocalDate ldateOut = LocalDate.parse("2013.01.31", formatter);

		Date dateIn = Date.from(ldateIn.atStartOfDay(ZoneId.systemDefault()).toInstant());
		Date dateOut = Date.from(ldateOut.atStartOfDay(ZoneId.systemDefault()).toInstant());

		INIFile inifile = new INIFile(null);
		inifile.setDateProperty(DOUBLE_SECTION, DATE_SECTION, dateIn, null);

		// FIXME it should be expected to get the same date back
		assertEquals(dateOut, inifile.getDateProperty(DOUBLE_SECTION, DATE_SECTION));
	}

	@Test @Ignore("the method is not used in project")
	public void testSetTimestampProperty() {
		//the method is not used in project
	}


	@Test @Ignore("the method is not used in project")
	public void testSetTimeStampFormat() {
		//the method is not used in project
	}

	@Test
	public void testGetTotalSections() {
	    INIFile myIniFile = new INIFile(TEST_RESOURCES_INIFILE_INI);
        assertEquals(Current_Section_count, myIniFile.getTotalSections());
	}

	@Test
	public void testGetAllSectionNames() {
	    INIFile myIniFile = new INIFile(TEST_RESOURCES_INIFILE_INI);
	    String[] names = {"Strings","Bools","Integer","Longs","Doubles","Dates"};
        assertArrayEquals(names, myIniFile.getAllSectionNames());
	}

	@Test
	public void testGetPropertyNames() {
	       INIFile myIniFile = new INIFile(TEST_RESOURCES_INIFILE_INI);
	        String[] names = {"Strings","Bools","Integer"};
	        String[] propertyNames = {"simpleString","escapedString","spacyString"};

	        assertArrayEquals(propertyNames, myIniFile.getPropertyNames(names[0]));
	}

	@Test
	public void testGetProperties() {
		INIFile expected = new INIFile(null);
		   INIFile myIniFile = new INIFile(TEST_RESOURCES_INIFILE_INI);

		   expected.addSection(BOOL_SECTION, null);
		   expected.setStringProperty(BOOL_SECTION, "right", "true", null);
		   expected.setStringProperty(BOOL_SECTION, "wrong", "false", null);
		   expected.setStringProperty(BOOL_SECTION, "invalid", "nonsense", null);
		   expected.setStringProperty(BOOL_SECTION, "none", "0", null);

		   expected.setStringProperty(BOOL_SECTION, "one", "1", null);
		   expected.setStringProperty(BOOL_SECTION, "five", "5", null);
           assertEquals(expected.getProperties(BOOL_SECTION), myIniFile.getProperties(BOOL_SECTION));
            //XXX: not equal even though values are equal. inner classINIProperty does not overwrite equals
	}

	@Test
	public void testRemoveProperty() {
		INIFile iniFile = new INIFile("") ;
		iniFile.setStringProperty(LONG_SECTION, "eins", "1", null);
		iniFile.setStringProperty(LONG_SECTION, "zwei", "2", null);
		assertEquals("2", iniFile.getStringProperty(LONG_SECTION, "zwei"));


		iniFile.removeProperty(LONG_SECTION, "zwei");

		assertEquals(null, iniFile.getStringProperty(LONG_SECTION, "zwei"));
		assertEquals("1", iniFile.getStringProperty(LONG_SECTION, "eins"));




	}

	@Test
	public void testRemoveSection() {
		INIFile iniFile = new INIFile("") ;
		iniFile.addSection(LONG_SECTION, null);
		iniFile.addSection(BOOL_SECTION, null);

		assertTrue(Arrays.asList(iniFile.getAllSectionNames()).contains(LONG_SECTION));
		iniFile.removeSection(LONG_SECTION);

		assertFalse(Arrays.asList(iniFile.getAllSectionNames()).contains(LONG_SECTION));
		assertTrue(Arrays.asList(iniFile.getAllSectionNames()).contains(BOOL_SECTION));

	}

	@Test
	public void testSave() {
		String path = TEST_RESOURCES_DIR + "deleteme.ini";
		File newFile = new File(path);
		if (newFile.exists()) {
			newFile.delete();
		}
		assertFalse(newFile.exists());
		INIFile myIniFile = new INIFile(path);
		myIniFile.save();
		assertFalse("empty File not written", newFile.exists());
		myIniFile.setStringProperty("Section", "property", "Propertyval", "comment");
		myIniFile.save();
		assertTrue(newFile.exists());
		newFile.delete();

	}


	@Test
	public void testRenameSection() {
		INIFile iniFile = new INIFile("") ;
		iniFile.addSection(LONG_SECTION, null);
		assertTrue(Arrays.asList(iniFile.getAllSectionNames()).contains(LONG_SECTION));
		iniFile.renameSection(LONG_SECTION, BOOL_SECTION, null);
		//FIXME: the sections name field is set to the new value , but the hashmap is not updated.
		assertTrue(Arrays.asList(iniFile.getAllSectionNames()).contains(LONG_SECTION));
		assertFalse(Arrays.asList(iniFile.getAllSectionNames()).contains(BOOL_SECTION));

	}


}
