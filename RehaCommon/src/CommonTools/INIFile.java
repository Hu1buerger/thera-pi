package CommonTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * INIFile class provides methods for manipulating (Read/Write) windows ini
 * files.
 */
public final class INIFile {

    /**
     * Logger for logging any output via logging framework.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INIFile.class);

    /**
     * Constant regex pattern that will match a COMMENT part of an INI file.
     */
    private static final Pattern commentPattern = Pattern.compile(";(?<COMMENT>.*)");
    
    /**
     * Constant regex pattern that will match a SECTION part of an INI file.
     */
    private static final Pattern sectionPattern = Pattern.compile("\\[(?<SECTION>.+)\\]");

    /**
     * Constant regex pattern that will match a key=value part of an INI file.
     * The key must be without an = sign!
     */
    private static final Pattern propertyPattern = Pattern.compile("(?<PROPERTYKEY>[^=]+)\\s*=\\s*(?<PROPERTYVALUE>.*+)");
    
    /**
     * Constant string part, used in error logging output about a parsing error of
     * an string value into the target type.
     */
    private static final String PARSE_EXCEPTION_BASE_MESSAGE = "File %s: Unable to parse the value for sectionKey %s, property %s %s ";

    /**
     * Constant field, used as String representation for a boolean <code>true</code>
     * value.
     */
    static final String INI_FILE_BOOLEAN_VALUE_STRING_TRUE = "TRUE";

    /**
     * Constant field, used as String representation for a boolean
     * <code>false</code> value.
     */
    static final String INI_FILE_BOOLEAN_VALUE_STRING_FALSE = "FALSE";

    /**
     * Constant field, used as String representation for am empty/not set comment.
     */
    static final String EMPTY_COMMENTS = "";

    /**
     * Variable to represent the date format
     */
    private String dateFormatString = "dd.MM.yyyy";

    /**
     * Variable to represent the timestamp format
     */

    private String timeStampFormatString = "dd.MM.yyyy HH:mm:ss";

    /**
     * Variable to hold the ini file name and full path
     */
    private String absoluteFileNamePath;

    /**
     * Variable to hold the sections in an ini file.
     */
    private LinkedHashMap<String, INISection> sectionMap;

    /**
     * Create a iniFile object from the file named in the parameter.
     *
     * @param absoluteFileNamePath The full path and name of the ini file to be
     *                             used.
     */

    public INIFile(String absoluteFileNamePath) {
        if (absoluteFileNamePath == null) {
            throw new NullPointerException("Parameter [absoluteFileNamePath] must not be null!");
        }

        if (!new File(absoluteFileNamePath).exists()) {
            LOG.error("Inifile does not exist: {}", absoluteFileNamePath);
        }

        this.sectionMap = new LinkedHashMap<>();
        this.absoluteFileNamePath = absoluteFileNamePath;

        // Load the specified INI file.
        if (checkFile(absoluteFileNamePath)) {
            loadFile(absoluteFileNamePath);
        }
    }

    public INIFile(String contentAsSingleString, String absoluteFileNamePath) {
        if (absoluteFileNamePath == null) {
            throw new NullPointerException("Parameter [absoluteFileNamePath] must be NOT null!");
        }
        if (!new File(absoluteFileNamePath).exists()) {
            LOG.debug("loading from Stream: {}", absoluteFileNamePath);
        }
        this.sectionMap = new LinkedHashMap<>();
        this.absoluteFileNamePath = absoluteFileNamePath;

        // read the specified INI file.
        if (contentAsSingleString != null) {
            String[] linesInFile = contentAsSingleString.split(System.lineSeparator());
            loadFile(Arrays.asList(linesInFile));
        }
    }

    /**
     * Returns the ini file name being used.
     *
     * @return the INI file name.
     */
    public String getFileName() {
        return this.absoluteFileNamePath;
    }

    /**
     * Returns the specified string property from the specified sectionKey.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be retrieved.
     * @return the string property value.
     */
    public String getStringProperty(String sectionKey, String propertyKey) {
        return getProperty(sectionKey, propertyKey, Function.identity());
    }

    /**
     * Returns the specified boolean property from the specified sectionKey. This
     * <p>
     * method considers the following values as boolean values. A none existing ue
     * is considered as false.
     * <ol>
     * <li>YES/yes/Yes - boolean true</li>
     * <li>NO/no/No - boolean false</li>
     * <li>1 - boolean true</li>
     * <li>0 - boolean false</li>
     * <li>TRUE/True/true - boolean true</li>
     * <li>FALSE/False/false - boolean false</li>
     * </ol>
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be retrieved.
     * @return The boolean value of the property values, if parsing is possible, or
     *         <code>false</code>, if there is no such value existing or the value
     *         can't be interpreted as boolean.
     */

    public boolean getBooleanProperty(String sectionKey, String propertyKey) {
        Boolean result = getProperty(sectionKey, propertyKey, s -> {
            if (s == null) {
                return false;
            } else if ("YES".equalsIgnoreCase(s) || "1".equalsIgnoreCase(s)) {
                return true;
            } else if ("NO".equalsIgnoreCase(s) || "0".equalsIgnoreCase(s)) {
                return false;
            } else {
                return Boolean.parseBoolean(s);
            }
        });

        return result != null ? result : false;
    }

    /**
     * Returns the specified integer property from the specified sectionKey.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be retrieved.
     * @return the integer property value.
     */
    public Integer getIntegerProperty(String sectionKey, String propertyKey) {
        Integer result = null;
        try {
            result = getProperty(sectionKey, propertyKey, Integer::valueOf);
        } catch (NumberFormatException ex) {

            LOG.error(String.format(PARSE_EXCEPTION_BASE_MESSAGE, this.absoluteFileNamePath, sectionKey, propertyKey,
                    "as Integer value!"), ex);
        }
        return result;
    }

    /**
     * Returns the specified long property from the specified sectionKey.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be retrieved.
     * @return the long property value.
     */
    public Long getLongProperty(String sectionKey, String propertyKey) {
        Long result = null;
        try {
            result = getProperty(sectionKey, propertyKey, Long::valueOf);
        } catch (NumberFormatException ex) {

            LOG.error(String.format(PARSE_EXCEPTION_BASE_MESSAGE, this.absoluteFileNamePath, sectionKey, propertyKey,
                    "as Long value!"), ex);
        }
        return result;
    }

    /**
     * Returns the specified double property from the specified sectionKey.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be retrieved.
     * @return the double property value.
     */

    public Double getDoubleProperty(String sectionKey, String propertyKey) {
        Double result = null;
        try {
            result = getProperty(sectionKey, propertyKey, s -> {
                Double value = Double.valueOf(s);
                if (Double.isInfinite(value)) {
                    value = null;
                }
                return value;
            });
        } catch (NumberFormatException ex) {
            LOG.error(String.format(PARSE_EXCEPTION_BASE_MESSAGE, this.absoluteFileNamePath, sectionKey, propertyKey,
                    "as Double value!"), ex);
        }
        return result;
    }

    /**
     * Returns the specified property from the specified sectionKey as
     * {@link LocalDate}.
     *
     * @param sectionKey  The INI sectionKey name.
     * @param propertyKey The property to be retrieved.
     * @return The property value as {@link LocalDate} instance, or
     *         <code>null</code>, if the property value can't be retrieved or
     *         parsed.
     */

    public LocalDate getDateProperty(String sectionKey, String propertyKey) {
        LocalDate result = null;
        try {
            result = getProperty(sectionKey, propertyKey, s -> {
                DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern(this.dateFormatString);
                return LocalDate.parse(s, dtFmt);

            });
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            LOG.error(String.format(PARSE_EXCEPTION_BASE_MESSAGE, this.absoluteFileNamePath, sectionKey, propertyKey,
                    "as LocalDate with format string" + this.dateFormatString + "!"), ex);
        }
        return result;
    }

    /**
     * Returns the specified date property from the specified sectionKey.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be retrieved.
     * @return the date property value.
     */
    public Timestamp getTimestampProperty(String sectionKey, String propertyKey) {
        return getProperty(sectionKey, propertyKey, s -> {
            Timestamp result = null;
            try {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.timeStampFormatString);
                LocalDateTime localDateTime = LocalDateTime.parse(s, formatter);
                result = Timestamp.valueOf(localDateTime);
            } catch (IllegalArgumentException | DateTimeParseException | NullPointerException ex) {
                LOG.error(String.format(PARSE_EXCEPTION_BASE_MESSAGE, this.absoluteFileNamePath, sectionKey,
                        propertyKey, "with timestamp format " + this.timeStampFormatString + "!"), ex);
            }
            return result;
        });
    }

    protected <T> T getProperty(String sectionKey, String propertyKey, Function<String, T> converter) {
        T result = null;
        INISection section = this.sectionMap.get(sectionKey);
        if (section != null) {
            INIProperty objProp = section.getProperty(propertyKey);
            if (objProp != null) {
                String strVal = objProp.getPropValue();
                if (strVal != null) {
                    result = converter.apply(strVal);
                } else {

                    LOG.debug("No property value for property key {} available in sectionKey {}!", propertyKey,
                            sectionKey);
                }
            } else {
                LOG.debug("No property set  for sectionKey {}, property {}!", sectionKey, propertyKey);
            }
        } else {
            LOG.debug("No sectionKey with name {} available!", sectionKey);
        }
        return result;
    }

    /*------------------------------------------------------------------------------
     * Setters
    ------------------------------------------------------------------------------*/

    /**
     * Sets the comments associated with a sectionKey.
     *
     * @param sectionKey the sectionKey name
     * @param comments   the comments.
     * @return The new created section.
     */

    public INISection addSection(String sectionKey, String comments) {
        return this.sectionMap.computeIfAbsent(sectionKey, key -> new INISection(key, comments));
    }

    /**
     * Sets the specified string property.
     *
     * @param sectionKey    the INI sectionKey name.
     * @param newSectionKey the new Section name to be set.
     */

    public void renameSection(String sectionKey, String newSectionKey, String comments) {
        INISection section = this.sectionMap.get(sectionKey);
        if (section != null) {
            this.sectionMap.put(newSectionKey, section);
            this.sectionMap.remove(sectionKey);

            section.setSecComments(comments);
        }
    }

    /**
     * Sets the specified string property.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be set.
     * @param value       the string value to be persisted
     * @param comments    A comment for the property.
     */

    public void setStringProperty(String sectionKey, String propertyKey, String value, String comments) {
        setPropertyOfTypeWithComments(sectionKey, propertyKey, value, comments);
    }

    /**
     * Sets the specified string property.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be set.
     * @param value       the string value to be persisted
     */
    public void setStringProperty(String sectionKey, String propertyKey, String value) {
        setStringProperty(sectionKey, propertyKey, value, EMPTY_COMMENTS);
    }

    /**
     * Sets the specified boolean property.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be set.
     * @param value       the boolean value to be persisted
     * @param comments    A comment for the property.
     */

    public void setBooleanProperty(String sectionKey, String propertyKey, boolean value, String comments) {
        setPropertyOfTypeWithComments(sectionKey, propertyKey,
                value ? INI_FILE_BOOLEAN_VALUE_STRING_TRUE : INI_FILE_BOOLEAN_VALUE_STRING_FALSE, comments);
    }

    /**
     * Sets the specified boolean property.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be set.
     * @param value       the boolean value to be persisted
     */
    public void setBooleanProperty(String sectionKey, String propertyKey, boolean value) {
        setPropertyOfTypeWithComments(sectionKey, propertyKey,
                value ? INI_FILE_BOOLEAN_VALUE_STRING_TRUE : INI_FILE_BOOLEAN_VALUE_STRING_FALSE, EMPTY_COMMENTS);
    }

    /**
     * Sets the specified integer property.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be set.
     * @param value       the int property to be persisted.
     * @param comments    A comment for the property.
     */

    public void setIntegerProperty(String sectionKey, String propertyKey, int value, String comments) {
        setPropertyOfTypeWithComments(sectionKey, propertyKey, Integer.toString(value), comments);
    }

    /**
     * Sets the specified integer property.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be set.
     * @param value       the int property to be persisted.
     */
    public void setIntegerProperty(String sectionKey, String propertyKey, int value) {
        setIntegerProperty(sectionKey, propertyKey, value, EMPTY_COMMENTS);
    }

    /**
     * Sets the specified long property.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be set.
     * @param value       the long value to be persisted.
     * @param comments    A comment for the property.
     */

    public void setLongProperty(String sectionKey, String propertyKey, long value, String comments) {
        setPropertyOfTypeWithComments(sectionKey, propertyKey, Long.toString(value), comments);
    }

    /**
     * Sets the specified long property.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be set.
     * @param value       the long value to be persisted.
     */
    public void setLongProperty(String sectionKey, String propertyKey, long value) {
        setLongProperty(sectionKey, propertyKey, value, EMPTY_COMMENTS);
    }

    /**
     * Sets the specified double property.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be set.
     * @param value       the double value to be persisted.
     * @param comments    A comment for the property.
     */

    public void setDoubleProperty(String sectionKey, String propertyKey, double value, String comments) {
        setPropertyOfTypeWithComments(sectionKey, propertyKey, Double.toString(value), comments);
    }

    /**
     * Sets the specified double property.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be set.
     * @param value       the double value to be persisted.
     */
    public void setDoubleProperty(String sectionKey, String propertyKey, double value) {
        setDoubleProperty(sectionKey, propertyKey, value, EMPTY_COMMENTS);
    }

    /**
     * Sets the specified java.util.Date property.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be set.
     * @param value       the date value to be persisted.
     * @param comments    A comment for the property.
     */

    public void setDateProperty(String sectionKey, String propertyKey, LocalDate value, String comments) {
        setPropertyOfTypeWithComments(sectionKey, propertyKey,
                utilDateToString(value.atStartOfDay(), this.dateFormatString), comments);
    }

    /**
     * Sets the specified java.util.Date property.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be set.
     * @param value       the date value to be persisted.
     */
    public void setDateProperty(String sectionKey, String propertyKey, LocalDate value) {
        setDateProperty(sectionKey, propertyKey, value, EMPTY_COMMENTS);
    }

    /**
     * Sets the specified java.sql.Timestamp property.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be set.
     * @param value       the timestamp value to be persisted.
     * @param comments    A comment for the property.
     */
    public void setTimestampProperty(String sectionKey, String propertyKey, Timestamp value, String comments) {
        setPropertyOfTypeWithComments(sectionKey, propertyKey, timeToString(value, this.timeStampFormatString),
                comments);
    }

    /**
     * Sets the specified java.sql.Timestamp property.
     *
     * @param sectionKey  the INI sectionKey name.
     * @param propertyKey the property to be set.
     * @param value       the timestamp value to be persisted.
     */
    public void setTimestampProperty(String sectionKey, String propertyKey, Timestamp value) {
        setTimestampProperty(sectionKey, propertyKey, value, EMPTY_COMMENTS);
    }

    /**
     * Set a property value, represented as string, for a property key into an
     * section with a (maybe empty) comment.
     *
     * @param sectionKey  The section key.
     * @param propertyKey The property key.
     * @param value       The value to set.
     * @param comments    A (maybe) empty comment.
     */
    private void setPropertyOfTypeWithComments(String sectionKey, String propertyKey, String value, String comments) {
        INISection section = this.sectionMap.computeIfAbsent(sectionKey, s -> new INISection(sectionKey));
        section.setProperty(propertyKey, value, comments);
    }

    /**
     * Sets the format to be used to interpreat date values.
     *
     * @param dateFormatString the format string
     * @throws IllegalArgumentException if the if the given pattern is invalid
     */

    public void setDateFormat(String dateFormatString) {
        // check if the string is parsable. Go ahead when the format is ok, otherwise
        // throw an runtime exception.
        checkIfTemporalFormatStringIsParsable(dateFormatString);
        this.dateFormatString = dateFormatString;
    }

    /**
     * Sets the format to be used to interpret timestamp values.
     *
     * @param timeStampFormatString the format string.
     * @throws IllegalArgumentException if the if the given pattern is invalid.
     */

    public void setTimeStampFormat(String timeStampFormatString) {
        // check if the string is parsable. Go ahead when the format is ok, otherwise
        // throw a RuntimeException.
        checkIfTemporalFormatStringIsParsable(timeStampFormatString);
        this.timeStampFormatString = timeStampFormatString;
    }

    /**
     * Get the amount of sections.
     *
     * @return The amount of sections in the ini file.
     */
    public int getTotalSections() {
        return this.sectionMap.size();
    }

    /**
     * Returns a string array containing names of all sections in INI file.
     *
     * @return the string array of sectionKey names
     */
    public String[] getAllSectionNames() {
        List<String> sectionNamesList = new ArrayList<>(this.sectionMap.keySet());
        return sectionNamesList.toArray(new String[0]);
    }

    /**
     * Returns a string array containing names of all the properties under specified
     * sectionKey.
     *
     * @param sectionKey the name of the sectionKey for which names of properties is
     *                   to be retrieved.
     * @return the string array of property names.
     */

    public String[] getPropertyNames(String sectionKey) {
        String[] result = null;
        INISection section = this.sectionMap.get(sectionKey);
        if (section != null) {
            result = section.getPropNames();
        }
        return result;
    }

    /**
     * Returns a map containing all the properties under specified sectionKey.
     *
     * @param sectionKey the name of the sectionKey for which properties are to be
     *                   retrieved.
     * @return the map of properties.
     */
    public Map<String, INIProperty> getProperties(String sectionKey) {
        Map<String, INIProperty> result = null;
        INISection section = this.sectionMap.get(sectionKey);
        if (section != null) {
            result = section.getProperties();
        }
        return result;
    }

    /**
     * Removed specified property from the specified sectionKey. If the specified
     * sectionKey or the property does not exist, does nothing.
     *
     * @param sectionKey  the sectionKey name.
     * @param propertyKey the name of the property to be removed.
     */
    public void removeProperty(String sectionKey, String propertyKey) {
        INISection section = this.sectionMap.get(sectionKey);
        if (section != null) {
            section.removeProperty(propertyKey);
        }
    }

    /**
     * Removes the specified sectionKey if one exists, otherwise does nothing.
     *
     * @param sectionKey the name of the sectionKey to be removed.
     */
    public void removeSection(String sectionKey) {
        this.sectionMap.remove(sectionKey);
    }

    /**
     * Flush changes back to the disk file. If the disk file does not exists then
     * creates the new one.
     */
    public synchronized boolean save() {

        boolean result = false;

        File file = new File(this.absoluteFileNamePath);
        try (FileWriter writer = new FileWriter(file)) {
            if (this.sectionMap.size() == 0) {
                LOG.warn("Nothing to save into file {}. The job is done!", this.absoluteFileNamePath);
                return false;
            }
            if (file.exists()) {
                LOG.warn("File {} is existing, will be deleted before saving the new content!",
                        this.absoluteFileNamePath);
                boolean deleted = file.delete();
                if (deleted) {
                    LOG.debug("File {} was deleted successfully. Starting the writing of the new content!",
                            this.absoluteFileNamePath);
                } else {
                    LOG.warn("Unable to delete the file {} before saving the new content!", this.absoluteFileNamePath);
                }
            } else {
                LOG.warn("File {} is not existing yet. Start saving the new content!", this.absoluteFileNamePath);
            }

            LOG.trace("Following content will be written into the file named {}.", this.absoluteFileNamePath);
            for (Map.Entry<String, INISection> entry : this.sectionMap.entrySet()) {
                INISection section = entry.getValue();
                String sectionAsString = section.toString();
                LOG.trace(sectionAsString);
                writer.write(sectionAsString);
                writer.write(System.lineSeparator());
            }
            LOG.trace("Done! All content written into the file named {}.", this.absoluteFileNamePath);
            result = true;
        } catch (IOException ex) {
            LOG.error("Error during saving INI Content into file " + this.absoluteFileNamePath + "!", ex);
        }
        return result;
    }

    public synchronized String saveToString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, INISection> entry : this.sectionMap.entrySet()) {
            INISection section = entry.getValue();
            builder.append(section.toString())
                   .append(System.lineSeparator());
        }
        return builder.toString();
    }

    /*------------------------------------------------------------------------------
     * Helper functions
     *----------------------------------------------------------------------------*/

    /**
     * Helper function to check if the date time formats is parsable.
     *
     * @param dateFormatString the date time format string to checked.
     * @throws IllegalArgumentException if the format string is not parsable by a
     *                                  {@link DateTimeFormatter}.
     */
    private void checkIfTemporalFormatStringIsParsable(String dateFormatString) {
        DateTimeFormatter.ofPattern(dateFormatString);
    }

    /**
     * Reads the INI file and load its contentens into a sectionKey collection after
     * parsing the file line by line.
     */

    private void loadFile(String absoluteFileNamePath) {
        try (FileInputStream inputStream = new FileInputStream(absoluteFileNamePath);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            List<String> fileContent = new ArrayList<>();
            while (bufferedReader.ready()) {
                String nextLineInFile = bufferedReader.readLine();
                fileContent.add(nextLineInFile);
            }
            loadFile(fileContent);
        } catch (IOException ex) {
            LOG.error("Exception in loadFile!", ex);
            this.sectionMap.clear();
        }
    }

    private void loadFile(List<String> fileContent) {
        StringBuilder commentsBuilder = new StringBuilder();
        INISection currentSection = null;
        for (String nextLineInFile : fileContent) {
            // if the line is empty, we go ahead with the next line.
            if (lineIsEmpty(nextLineInFile)) {
                continue;
            }
            currentSection = readFile(nextLineInFile, commentsBuilder, currentSection);
        }
    }

    private INISection readFile(String nextLineInFile, StringBuilder commentsBuilder, INISection section) {

        // check if it a comment
        Matcher commentMatcher = commentPattern.matcher(nextLineInFile);
        // check if it a section
        Matcher sectionMatcher = sectionPattern.matcher(nextLineInFile);
        // check if it a property (key/value pair)
        Matcher propertyMatcher = propertyPattern.matcher(nextLineInFile);

        if (commentMatcher.matches()) {
            // if we have a comment line, we need to check if it is the first or next line.
            if (commentsBuilder == null) {
                // for a first line, the commentsBuilder is expected to be <code>null</code>
                commentsBuilder = new StringBuilder();
            }
            // in any case, we add the comment to the commentsBuilder and also add a new
            // line sign.
            aggregateCommentsLineForNextElement(commentsBuilder, commentMatcher);
            LOG.trace("Read the following comment line: {}", nextLineInFile);
        } else if (sectionMatcher.matches()) {
            // if we have a new section, we add the section, together with the previously
            // processed comments, if any.
            section = createNewSection(commentsBuilder, sectionMatcher);
            // after that, we throw away the comments processed before the section.
            commentsBuilder.setLength(0);
            LOG.trace("Read the following section: {}", section);
        } else if (propertyMatcher.matches()) {
            // if we have a new property, we add the property below the section, together
            // with the previously processed comments, if any.
            // NOTE! We expect to have a valid section here! otherwise we can't add the
            // property. If there is no section available, we throw an exception and stop
            // processing!
            INIProperty property = extractPropertyForSection(commentsBuilder, section, nextLineInFile, propertyMatcher);
            // after that, we throw away the comments processed before the section.
            commentsBuilder.setLength(0);
            LOG.trace("Read the following property: {} below section {}", property, section);
        } else {
            LOG.trace(
                    "Found something strange in the file {}. Following line isn't a comment (;COMMENT), section ([SECTION]) or property (key = value): {}. Is there a bug inside the code or the line?",
                    this.absoluteFileNamePath, nextLineInFile);
        }
        return section;
    }

    private boolean lineIsEmpty(String nextLineInFile) {
        return nextLineInFile.trim()
                             .length() == 0;
    }

    private void aggregateCommentsLineForNextElement(StringBuilder remarksBuilder, Matcher commentMatcher) {
        String comment = commentMatcher.group("COMMENT")
                                       .trim();
        remarksBuilder.append(comment)
                      .append(System.lineSeparator());
    }

    private INISection createNewSection(StringBuilder remarksBuilder, Matcher sectionMatcher) {
        String sectionKey = sectionMatcher.group("SECTION")
                                          .trim();
        String remarks = remarksBuilder != null ? remarksBuilder.toString() : EMPTY_COMMENTS;
        return this.addSection(sectionKey, remarks);
    }

    private INIProperty extractPropertyForSection(StringBuilder remarksBuilder, INISection section, String strLine,
            Matcher propertyMatcher) {
        if (section == null) {
            throw new IllegalStateException("The line " + strLine + " isn't written below a valid section!");
        }
        String propertyKey = propertyMatcher.group("PROPERTYKEY")
                                            .trim();
        String propertyValue = propertyMatcher.group("PROPERTYVALUE")
                                              .trim();
        String remarks = remarksBuilder != null ? remarksBuilder.toString() : EMPTY_COMMENTS;
        return section.setProperty(propertyKey, propertyValue, remarks);
    }

    /**
     * Helper method to check the existance of a file.
     *
     * @param pstrFile the full path and name of the file to be checked.
     * @return true if file exists, false otherwise.
     */
    private boolean checkFile(String pstrFile) {
        boolean blnRet;
        try {
            File objFile = new File(pstrFile);
            blnRet = (objFile.exists() && objFile.isFile());
        } catch (Exception e) {
            blnRet = false;
        }
        return blnRet;
    }

    /**
     * Converts a java.util.date into String
     *
     * @param localDateTime Date that need to be converted to String
     * @param formatString  The date format pattern.
     * @return String
     */

    private String utilDateToString(LocalDateTime localDateTime, String formatString) {
        String strRet;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatString);
            strRet = localDateTime.format(formatter);
        } catch (IllegalArgumentException | DateTimeException ex) {
            LOG.error("Exception in utilDateToString!", ex);
            strRet = null;
        }
        return strRet;
    }

    /**
     * Converts the given sql timestamp object to a string representation. The
     * format to be used is to be obtained from the configuration file.
     *
     * @param timestamp             the sql timestamp object to be converted.
     * @param timeStampFormatString If true formats the string using GMT timezone
     *                              otherwise using local timezone.
     * @return the formatted string representation of the timestamp.
     */

    private String timeToString(Timestamp timestamp, String timeStampFormatString) {
        String result;

        try {
            SimpleDateFormat dtFmt = new SimpleDateFormat(timeStampFormatString);
            result = dtFmt.format(timestamp);
        } catch (IllegalArgumentException | NullPointerException ex) {
            LOG.error("Exception in utilDateToString!", ex);
            result = "";
        }
        return result;
    }

    /*------------------------------------------------------------------------------
     * Private class representing the INI Section.
     *----------------------------------------------------------------------------*/

    /**
     * Class to represent the individual ini file sectionKey.
     */

    class INISection {

        /**
         * Variable to hold any comments associated with this sectionKey
         */
        private String sectionComment;

        /**
         * Variable to hold the sectionKey name.
         */
        private String sectionName;

        /**
         * Variable to hold the properties falling under this sectionKey.
         */
        private Map<String, INIProperty> iniSectionProperties;

        /**
         * Construct a new sectionKey object identified by the name specified in
         * parameter.
         *
         * @param sectionKey The new sections name.
         */
        INISection(String sectionKey) {
            this.sectionName = sectionKey;

            this.iniSectionProperties = new HashMap<>();
        }

        /**
         * Construct a new sectionKey object identified by the name specified in
         * parameter and associated comments.
         *
         * @param sectionKey The new sections name.
         * @param comments   the comments associated with this sectionKey.
         */
        INISection(String sectionKey, String comments) {
            this.sectionName = sectionKey;
            this.iniSectionProperties = new LinkedHashMap<>();
        }

        /**
         * Sets the comments associated with this sectionKey.
         *
         * @param comments the comments
         */
        void setSecComments(String comments) {
            this.sectionComment = comments;
        }

        /**
         * Removes specified property value from this sectionKey.
         *
         * @param propertyKey The name of the property to be removed.
         */
        void removeProperty(String propertyKey) {
            this.iniSectionProperties.remove(propertyKey);
        }

        /**
         * Creates or modifies the specified property value.
         *
         * @param propertyKey The name of the property to be created or modified.
         * @param pstrValue   The new value for the property.
         * @param comments    the associated comments
         */

        INIProperty setProperty(String propertyKey, String pstrValue, String comments) {
            return this.iniSectionProperties.put(propertyKey, new INIProperty(propertyKey, pstrValue, comments));
        }

        /**
         * Returns a map of all properties.
         *
         * @return a map of all properties
         */
        Map<String, INIProperty> getProperties() {
            return Collections.unmodifiableMap(this.iniSectionProperties);
        }

        /**
         * Returns a string array containing names of all the properties under this
         * sectionKey.
         *
         * @return the string array of property names.
         */
        String[] getPropNames() {
            return this.iniSectionProperties.keySet()
                                            .toArray(new String[0]);
        }

        /**
         * Returns underlying value of the specified property.
         *
         * @param propertyKey the property whose underlying value is to be retrieved.
         * @return the property value or <code>null</code>, if there is no property
         *         available for the key.
         */
        INIProperty getProperty(String propertyKey) {
            return this.iniSectionProperties.get(propertyKey);
        }

        @Override
        public String toString() {

            StringBuilder stringBuilder = new StringBuilder();
            if (this.sectionComment != null) {
                stringBuilder.append(addRemarkCharacter(this.sectionComment));
            }
            stringBuilder.append("[")
                         .append(this.sectionName)
                         .append("]")
                         .append(System.lineSeparator());
            for (INIProperty entry : this.iniSectionProperties.values()) {
                stringBuilder.append(entry);
                stringBuilder.append(System.lineSeparator());
            }
            return stringBuilder.toString();
        }
    }

    /**
     * This class represents a key value pair called property in an INI file.
     */
    class INIProperty {

        /**
         * Variable to hold name of this property
         */
        private String propertyKey;

        /**
         * Variable to hold value of this property
         */
        private String propertyValue;

        /**
         * Variable to hold comments associated with this property
         */
        private String comments;

        /**
         * Constructor
         *
         * @param propertyKey   the name of this property.
         * @param propertyValue the value of this property.
         * @param comments      the comments associated with this property.
         */
        INIProperty(String propertyKey, String propertyValue, String comments) {
            this.propertyKey = propertyKey;
            this.propertyValue = propertyValue;
            this.comments = comments;
        }

        /**
         * Returns value of this property. If value contains a reference to environment
         * variable then this reference is replaced by actual value before the value is
         * returned.
         *
         * @return the value of this property.
         */

        String getPropValue() {
            final Pattern environmentVariableRegEx = Pattern.compile("%(?<environmentVariableKey>.+)%");
            String result;
            Matcher matcher = environmentVariableRegEx.matcher(this.propertyValue);
            if (matcher.matches()) {
                LOG.debug("Property value {} could be an environment variable, we will try to resolve the value.",
                        this.propertyValue);
                String environmentVariableKey = matcher.group("environmentVariableKey");
                result = System.getenv(environmentVariableKey);
                if (result == null) {
                    LOG.debug(
                            "Property value {} could not be resolved into an environment variable. The property value itself is returned.",
                            this.propertyValue);
                    result = this.propertyValue;
                } else {
                    LOG.debug("Property value {} is environment variable with value {}.", this.propertyValue, result);
                }
            } else {
                result = this.propertyValue;
            }
            return result;
        }

        String getComments() {
            return this.comments;
        }

        @Override
        public String toString() {

            StringBuilder result = new StringBuilder();
            if (this.comments != null && !this.comments.isEmpty()) {
                result.append(addRemarkCharacter(comments));
            }
            return result.append(this.propertyKey)
                         .append(" = ")
                         .append(this.propertyValue)
                         .toString();
        }
    }

    /**
     * This function adds a remark character ';' in source string.
     *
     * @param comment source string
     * @return converted string.
     */
    private String addRemarkCharacter(String comment) {
        String[] commentSlittedByNewLine = comment.split(System.lineSeparator());
        StringBuilder commentsBlockBuilder = new StringBuilder();
        for (String commentLine : commentSlittedByNewLine) {
            commentsBlockBuilder.append(";")
                                .append(commentLine)
                                .append(System.lineSeparator());
        }
        return commentsBlockBuilder.toString();
    }
}
