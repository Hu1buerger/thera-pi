package CommonTools;

/*------------------------------------------------------------------------------
 * based on
 * PACKAGE: com.freeware.inifiles
 * FILE   : iniFile.java
 * CREATED: Jun 30, 2004
 * AUTHOR : Prasad P. Khandekar
 *------------------------------------------------------------------------------
 * Change Log:
 * 05/07/2004    - Added support for date time formats.
 *                 Added support for environment variables.
 * 07/07/2004    - Added support for data type specific getters and setters.
 *                 Updated main method to reflect above changes.
 * 26/08/2004    - Added support for section level and property level comments.
 *                 Introduction of seperate class for property values.
 *                 Added addSection method.
 *                 Sections and properties now retail their order (LinkedHashMap)
 *                 Method implementation changes.
 *-----------------------------------------------------------------------------*/
//package com.freeware.inifiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * INIFile class provides methods for manipulating (Read/Write) windows ini
 * files.
 *
 * @author Prasad P. Khandekar
 * @version 1.0
 * @since 1.0
 */
public final class INIFile {
        /** Variable to hold the ini file name and full path. */
    private String mstrFile;

    /** Variable to hold inputstream of the inifile-content in a table. */
    private InputStream streamin;

    /** Variable to hold the sections in an ini file. */
    private LinkedHashMap<String, INISection> mhmapSections;

    /** Variable to hold environment variables. */
    private Properties mpropEnv;

    private Logger logger = LoggerFactory.getLogger(INIFile.class);

    /**
     * Create a iniFile object from the file named in the parameter.
     * 
     * @param pstrPathAndName The full path and name of the ini file to be used.
     */
    public INIFile(String pstrPathAndName) {
        if (pstrPathAndName == null || !new File(pstrPathAndName).exists()) {
            logger.error("Inifile does not exist:" + pstrPathAndName);
        }
        mpropEnv = getEnvVars();
        mhmapSections = new LinkedHashMap<>();
        mstrFile = pstrPathAndName;
        // Load the specified INI file.
        if (checkFile(pstrPathAndName)) {
            loadFile();
        }
    }

    INIFile(InputStream istream, String pstrPathAndName) {
        if (!new File(pstrPathAndName).exists()) {
            logger.debug("loading from Stream:" + pstrPathAndName);
        }
        mpropEnv = getEnvVars();
        mhmapSections = new LinkedHashMap<>();
        mstrFile = pstrPathAndName;
        streamin = istream;
        // read the specified INI file.
        if (streamin != null) {
            readFile();
        }
    }

    

    /**
     * Returns the ini file name being used.
     * 
     * @return the INI file name.
     */
    public String getFileName() {
        return this.mstrFile;
    }

    public InputStream getInputStream() {
        return this.streamin;
    }

    /**
     * Returns the specified string property from the specified section.
     * 
     * @param pstrSection the INI section name.
     * @param pstrProp    the property to be retrieved.
     * @return the string property value.
     */
    public String getStringProperty(String pstrSection, String pstrProp) {
        String strRet = null;
        INIProperty objProp = null;
        INISection objSec;

        objSec = this.mhmapSections.get(pstrSection);
        if (objSec != null) {
            objProp = objSec.getProperty(pstrProp);
            if (objProp != null) {
                strRet = objProp.getPropValue();
            }
        }
        return strRet;
    }

    /**
     * Returns the specified boolean property from the specified section. This
     * method considers the following values as boolean values.
     * <ol>
     * <li>YES/yes/Yes - boolean true</li>
     * <li>NO/no/No - boolean false</li>
     * <li>1 - boolean true</li>
     * <li>0 - boolean false</li>
     * <li>TRUE/True/true - boolean true</li>
     * <li>FALSE/False/false - boolean false</li>
     * </ol>
     * 
     * @param pstrSection the INI section name.
     * @param pstrProp    the property to be retrieved.
     * @return the boolean value
     */
    public Boolean getBooleanProperty(String pstrSection, String pstrProp) {
        boolean blnRet = false;
        String strVal = null;
        INIProperty objProp = null;
        INISection objSec;

        objSec = this.mhmapSections.get(pstrSection);
        if (objSec != null) {
            objProp = objSec.getProperty(pstrProp);
            if (objProp != null) {
                strVal = objProp.getPropValue()
                                .toUpperCase();
                if ("YES".equals(strVal) || "TRUE".equals(strVal) || "1".equals(strVal)) {
                    blnRet = true;
                }
            }
        }
        return blnRet;
    }

    /**
     * Returns the specified integer property from the specified section.
     * 
     * @param pstrSection the INI section name.
     * @param pstrProp    the property to be retrieved.
     * @return the integer property value.
     */
    public Integer getIntegerProperty(String pstrSection, String pstrProp) {
        Integer intRet = null;
        String strVal = null;
        INIProperty objProp = null;
        INISection objSec;

        objSec = this.mhmapSections.get(pstrSection);
        if (objSec != null) {
            objProp = objSec.getProperty(pstrProp);
            try {
                if (objProp != null) {
                    strVal = objProp.getPropValue();
                    if (strVal != null) {
                        intRet = Integer.valueOf(strVal);
                    }
                }
            } catch (NumberFormatException exIgnore) {
            } finally {
                if (objProp != null) {
                    objProp = null;
                }
            }
            objSec = null;
        }
        return intRet;
    }

    /**
     * Returns the specified long property from the specified section.
     * 
     * @param pstrSection the INI section name.
     * @param pstrProp    the property to be retrieved.
     * @return the long property value.
     */
    public Long getLongProperty(String pstrSection, String pstrProp) {
        Long lngRet = null;
        String strVal = null;
        INIProperty objProp = null;
        INISection objSec;

        objSec = this.mhmapSections.get(pstrSection);
        if (objSec != null) {
            objProp = objSec.getProperty(pstrProp);
            try {
                if (objProp != null) {
                    strVal = objProp.getPropValue();
                    if (strVal != null) {
                        lngRet = Long.valueOf(strVal);
                    }
                }
            } catch (NumberFormatException exIgnore) {
            } finally {
                if (objProp != null) {
                    objProp = null;
                }
            }
            objSec = null;
        }
        return lngRet;
    }

 

  
    

    /*------------------------------------------------------------------------------
     * Setters
    ------------------------------------------------------------------------------*/
    /**
     * Sets the comments associated with a section.
     * 
     * @param pstrSection  the section name
     * @param pstrComments the comments.
     */
    public void addSection(String pstrSection, String pstrComments) {
        INISection objSec;

        objSec = this.mhmapSections.get(pstrSection);
        if (objSec == null) {
            objSec = new INISection(pstrSection);
            this.mhmapSections.put(pstrSection, objSec);
        }
        objSec.setSecComments(delRemChars(pstrComments));
    }

    /**
     * Sets the specified string property.
     * 
     * @param pstrSection    the INI section name.
     * @param newpstrSection the new Section name to be set.
     * @pstrVal the string value to be persisted
     */

    public void renameSection(String pstrSection, String newpstrSection, String pstrComments) {
        INISection objSec;

        objSec = this.mhmapSections.get(pstrSection);
        if (objSec != null) {
            this.mhmapSections.put(newpstrSection, objSec);
            if (this.mhmapSections.containsKey(pstrSection)) {
                this.mhmapSections.remove(pstrSection);
            }
            objSec.setSecComments(delRemChars(pstrComments));
        }
    }

    /**
     * Sets the specified string property.
     * 
     * @param pstrSection the INI section name.
     * @param pstrProp    the property to be set.
     * @pstrVal the string value to be persisted
     */
    public void setStringProperty(String pstrSection, String pstrProp, String pstrVal, String pstrComments) {
        INISection objSec;

        objSec = this.mhmapSections.get(pstrSection);
        if (objSec == null) {
            objSec = new INISection(pstrSection);
            this.mhmapSections.put(pstrSection, objSec);
        }
        objSec.setProperty(pstrProp, pstrVal, pstrComments);
    }

    /**
     * Sets the specified boolean property.
     * 
     * @param pstrSection the INI section name.
     * @param pstrProp    the property to be set.
     * @param pblnVal     the boolean value to be persisted
     */
    public void setBooleanProperty(String pstrSection, String pstrProp, boolean pblnVal, String pstrComments) {
        INISection objSec;

        objSec = this.mhmapSections.get(pstrSection);
        if (objSec == null) {
            objSec = new INISection(pstrSection);
            this.mhmapSections.put(pstrSection, objSec);
        }
        if (pblnVal) {
            objSec.setProperty(pstrProp, "TRUE", pstrComments);
        } else {
            objSec.setProperty(pstrProp, "FALSE", pstrComments);
        }
    }

    /**
     * Sets the specified integer property.
     * 
     * @param pstrSection the INI section name.
     * @param pstrProp    the property to be set.
     * @param pintVal     the int property to be persisted.
     */
    public void setIntegerProperty(String pstrSection, String pstrProp, int pintVal, String pstrComments) {
        INISection objSec;

        objSec = this.mhmapSections.get(pstrSection);
        if (objSec == null) {
            objSec = new INISection(pstrSection);
            this.mhmapSections.put(pstrSection, objSec);
        }
        objSec.setProperty(pstrProp, Integer.toString(pintVal), pstrComments);
    }

  
   

  

    /**
     * Sets the format to be used to interpreat date values.
     * 
     * @param pstrDtFmt the format string
     * @throws IllegalArgumentException if the if the given pattern is invalid
     */
    public void setDateFormat(String pstrDtFmt) {
        if (!checkDateTimeFormat(pstrDtFmt)) {
            throw new IllegalArgumentException("The specified date pattern is invalid!");
        }
    }

    /**
     * Sets the format to be used to interpreat timestamp values.
     * 
     * @param pstrTSFmt the format string
     * @throws IllegalArgumentException if the if the given pattern is invalid
     */
    public void setTimeStampFormat(String pstrTSFmt) {
        if (!checkDateTimeFormat(pstrTSFmt)) {
            throw new IllegalArgumentException("The specified timestamp pattern is invalid!");
        }
    }

    /**------------------------------------------------------------------------------
     * Public methods
    ------------------------------------------------------------------------------.*/
    public int getTotalSections() {
        return this.mhmapSections.size();
    }

    /**
     * Returns a string array containing names of all sections in INI file.
     * 
     * @return the string array of section names
     */
    public String[] getAllSectionNames() {
        int iCntr = 0;
        Iterator<String> iter = null;
        String[] arrRet = null;

        try {
            if (!this.mhmapSections.isEmpty()) {
                arrRet = new String[this.mhmapSections.size()];
                for (iter = this.mhmapSections.keySet()
                                              .iterator(); iter.hasNext();) {
                    arrRet[iCntr] = iter.next();
                    iCntr++;
                }
            }
        } catch (NoSuchElementException exIgnore) {
        } finally {
            if (iter != null) {
                iter = null;
            }
        }
        return arrRet;
    }

  

  

    /**
     * Removes the specified section if one exists, otherwise does nothing.
     * 
     * @param pstrSection the name of the section to be removed.
     */
    public void removeSection(String pstrSection) {
        if (this.mhmapSections.containsKey(pstrSection)) {
            this.mhmapSections.remove(pstrSection);
        }
    }

    /**
     * Flush changes back to the disk file. If the disk file does not exists then
     * creates the new one.
     */
    public synchronized boolean save() {
        boolean blnRet = false;
        File objFile = null;
        String strName = null;
        String strTemp = null;
        Iterator<String> itrSec = null;
        INISection objSec = null;
        FileWriter objWriter = null;

        try {
            if (this.mhmapSections.isEmpty()) {
                return false;
            }
            objFile = new File(this.mstrFile);
            if (objFile.exists()) {
                objFile.delete();
            }
            objWriter = new FileWriter(objFile);
            itrSec = this.mhmapSections.keySet()
                                       .iterator();
            while (itrSec.hasNext()) {
                strName = itrSec.next();
                objSec = this.mhmapSections.get(strName);
                strTemp = objSec.toString();
                objWriter.write(strTemp);
                objWriter.write("\r\n");
                objSec = null;
            }
            blnRet = true;
        } catch (IOException exIgnore) {
            exIgnore.printStackTrace();
        } finally {
            if (objWriter != null) {
                closeWriter(objWriter);
                objWriter = null;
            }
            if (objFile != null) {
                objFile = null;
            }
            if (itrSec != null) {
                itrSec = null;
            }
        }
        return blnRet;
    }

    public synchronized StringBuffer saveToStringBuffer() {
        String strName = null;
        String strTemp = null;
        Iterator<String> itrSec = null;
        INISection objSec = null;
        StringBuffer buf = null;
        try {
            buf = new StringBuffer();
            itrSec = this.mhmapSections.keySet()
                                       .iterator();
            while (itrSec.hasNext()) {
                strName = itrSec.next();
                objSec = this.mhmapSections.get(strName);
                strTemp = objSec.toString();
                buf.append(strTemp);
                buf.append("\r\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return buf;
    }

    /*------------------------------------------------------------------------------
     * Helper functions
     *----------------------------------------------------------------------------*/
    /**
     * Procedure to read environment variables. Thanx to
     * http://www.rgagnon.com/howto.html for this implementation.
     */
    private Properties getEnvVars() {
        Process p = null;
        Properties envVars = new Properties();

        try {
            Runtime r = Runtime.getRuntime();
            String os = System.getProperty("os.name")
                              .toLowerCase();

            if (os.indexOf("windows 9") > -1) {
                p = r.exec("command.com /c set");
            } else if (os.indexOf("nt") > -1 || os.indexOf("windows 2000") > -1
                    || os.indexOf("windows xp") > -1) {
                p = r.exec("cmd.exe /c set");
            } else {
                // our last hope, we assume Unix (thanks to H. Ware for the fix)
                p = r.exec("env");
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                int idx = line.indexOf('=');
                String key = line.substring(0, idx);
                String value = line.substring(idx + 1);
                envVars.setProperty(key, value);
            }
        } catch (Exception exIgnore) {
        }
        return envVars;
    }

    /**
     * Helper function to check the date time formats.
     * 
     * @param pstrDtFmt the date time format string to be checked.
     * @return true for valid date/time format, false otherwise.
     */
    private boolean checkDateTimeFormat(String pstrDtFmt) {
        boolean blnRet = false;
        DateFormat objFmt = null;

        try {
            objFmt = new SimpleDateFormat(pstrDtFmt);
            blnRet = true;
        } catch (NullPointerException | IllegalArgumentException exIgnore) {
        } finally {
            if (objFmt != null) {
                objFmt = null;
            }
        }
        return blnRet;
    }

    /**
     * Reads the INI file and load its contentens into a section collection after
     * parsing the file line by line.
     */
    private void loadFile() {
        int iPos = -1;
        String strLine = null;
        String strSection = null;
        String strRemarks = null;
        BufferedReader objBRdr = null;
        FileReader objFRdr = null;
        INISection objSec = null;

        try {
            objFRdr = new FileReader(this.mstrFile);
            objBRdr = new BufferedReader(objFRdr);
            while (objBRdr.ready()) {
                iPos = -1;
                strLine = objBRdr.readLine()
                                 .trim();
                if (strLine != null && !strLine.isEmpty()) {
                    if (";".equals(strLine.substring(0, 1))) {
                        if (strRemarks == null || strRemarks.isEmpty()) {
                            strRemarks = strLine.substring(1);
                        } else {
                            strRemarks = strRemarks + "\r\n" + strLine.substring(1);
                        }
                    } else if (strLine.startsWith("[") && strLine.endsWith("]")) {
                        // Section start reached create new section
                        if (objSec != null) {
                            this.mhmapSections.put(strSection.trim(), objSec);
                        }
                        objSec = null;
                        strSection = strLine.substring(1, strLine.length() - 1);
                        objSec = new INISection(strSection.trim(), strRemarks);
                        strRemarks = null;
                    } else if ((iPos = strLine.indexOf('=')) > 0 && objSec != null) {
                        // read the key value pair 012345=789
                        objSec.setProperty(strLine.substring(0, iPos)
                                                  .trim(),
                                strLine.substring(iPos + 1)
                                       .trim(),
                                strRemarks);
                        strRemarks = null;
                    }
                }
            }
            if (objSec != null) {
                this.mhmapSections.put(strSection.trim(), objSec);
            }
        } catch (IOException | NullPointerException exIgnore) {
            this.mhmapSections.clear();
        } finally {
            if (objBRdr != null) {
                closeReader(objBRdr);
                objBRdr = null;
            }
            if (objFRdr != null) {
                closeReader(objFRdr);
                objFRdr = null;
            }
            if (objSec != null) {
                objSec = null;
            }
        }
    }

    private void readFile() {
        int iPos = -1;
        String strLine = null;
        String strSection = null;
        String strRemarks = null;
        BufferedReader objBRdr = null;
        InputStreamReader objFRdr = null;
        INISection objSec = null;

        try {
            objFRdr = new InputStreamReader(streamin);
            objBRdr = new BufferedReader(objFRdr);
            while (objBRdr.ready()) {
                iPos = -1;
                // strLine = null;
                strLine = objBRdr.readLine()
                                 .trim();
                if (strLine != null && !strLine.isEmpty()) {
                    if (";".equals(strLine.substring(0, 1))) {
                        if (strRemarks == null || strRemarks.isEmpty()) {
                            strRemarks = strLine.substring(1);
                        } else {
                            strRemarks = strRemarks + "\r\n" + strLine.substring(1);
                        }
                    } else if (strLine.startsWith("[") && strLine.endsWith("]")) {
                        // Section start reached create new section
                        if (objSec != null) {
                            this.mhmapSections.put(strSection.trim(), objSec);
                        }
                        objSec = null;
                        strSection = strLine.substring(1, strLine.length() - 1);
                        objSec = new INISection(strSection.trim(), strRemarks);
                        strRemarks = null;
                    } else if ((iPos = strLine.indexOf('=')) > 0 && objSec != null) {
                        // read the key value pair 012345=789
                        objSec.setProperty(strLine.substring(0, iPos)
                                                  .trim(),
                                strLine.substring(iPos + 1)
                                       .trim(),
                                strRemarks);
                        strRemarks = null;
                    }
                }
            }
            if (objSec != null) {
                this.mhmapSections.put(strSection.trim(), objSec);
            }
        } catch (IOException | NullPointerException e) {
            this.mhmapSections.clear();
        } finally {
            if (objBRdr != null) {
                closeReader(objBRdr);
                objBRdr = null;
            }
            if (objFRdr != null) {
                closeReader(objFRdr);
                objFRdr = null;
            }
            if (objSec != null) {
                objSec = null;
            }
        }
    }

    /**
     * Helper function to close a reader object.
     * 
     * @param pobjRdr the reader to be closed.
     */
    private void closeReader(Reader pobjRdr) {
        if (pobjRdr == null) {
            return;
        }
        try {
            pobjRdr.close();
        } catch (IOException exIgnore) {
        }
    }

    /**
     * Helper function to close a writer object.
     * 
     * @param pobjWriter the writer to be closed.
     */
    private void closeWriter(Writer pobjWriter) {
        if (pobjWriter == null) {
            return;
        }

        try {
            pobjWriter.close();
        } catch (IOException exIgnore) {
        }
    }

    /**
     * Helper method to check the existance of a file.
     * 
     * @param the full path and name of the file to be checked.
     * @return true if file exists, false otherwise.
     */
    private boolean checkFile(String pstrFile) {
        boolean blnRet = false;
        File objFile = null;

        try {
            objFile = new File(pstrFile);
            blnRet = objFile.exists() && objFile.isFile();
        } catch (Exception e) {
        } finally {
            if (objFile != null) {
                objFile = null;
            }
        }
        return blnRet;
    }

  

    /**
     * This function deletes the remark characters ';' from source string.
     * 
     * @param pstrSrc the source string
     * @return the converted string
     */
    private String delRemChars(String pstrSrc) {
        int intPos = 0;

        if (pstrSrc == null) {
            return null;
        }
        while ((intPos = pstrSrc.indexOf(';')) >= 0) {
            if (intPos == 0) {
                pstrSrc = pstrSrc.substring(intPos + 1);
            } else if (intPos > 0) {
                pstrSrc = pstrSrc.substring(0, intPos) + pstrSrc.substring(intPos + 1);
            }
        }
        return pstrSrc;
    }

    /**
     * This function adds a remark character ';' in source string.
     * 
     * @param pstrSrc source string
     * @return converted string.
     */
    private String addRemChars(String pstrSrc) {
        int intLen;
        int intPos = 0;
        int intPrev = 0;

        String strLeft = null;
        String strRight = null;

        if (pstrSrc == null) {
            return null;
        }
        while (intPos >= 0) {
            intLen = 2;
            intPos = pstrSrc.indexOf("\r\n", intPrev);
            if (intPos < 0) {
                intLen = 1;
                intPos = pstrSrc.indexOf('\n', intPrev);
                if (intPos < 0) {
                    intPos = pstrSrc.indexOf('\r', intPrev);
                }
            }
            if (intPos == 0) {
                pstrSrc = ";\r\n" + pstrSrc.substring(intPos + intLen);
                intPrev = intPos + intLen + 1;
            } else if (intPos > 0) {
                strLeft = pstrSrc.substring(0, intPos);
                strRight = pstrSrc.substring(intPos + intLen);
                if (strRight == null || strRight.isEmpty()) {
                    pstrSrc = strLeft;
                } else {
                    pstrSrc = strLeft + "\r\n;" + strRight;
                }
                intPrev = intPos + intLen + 1;
            }
        }
        if (!";".equals(pstrSrc.substring(0, 1))) {
            pstrSrc = ";" + pstrSrc;
        }
        return pstrSrc + "\r\n";
    }

    /*------------------------------------------------------------------------------
     * Main entry point to test the functionality.
     *----------------------------------------------------------------------------*/
    /**
     * The main entry point for testing.
     * 
     * @param pstrArgs the command line arguments array if any.
     */
    public static void main(String[] pstrArgs) {
        INIFile objINI;
        String strFile;

        if (pstrArgs.length == 0) {
            return;
        }

        strFile = pstrArgs[0];
        // Following call will load the strFile if one exists.
        objINI = new INIFile(strFile);

        objINI.setStringProperty("Folders", "folder1", "G:\\Temp", null);
        objINI.setStringProperty("Folders", "folder2", "G:\\Temp\\Backup", null);

        // Save changes back to strFile.
        objINI.save();
    }

    /*------------------------------------------------------------------------------
     * Private class representing the INI Section.
     *----------------------------------------------------------------------------*/
    /**
     * Class to represent the individual ini file section.
     * 
     * @author Prasad P. Khandekar
     * @version 1.0
     * @since 1.0
     */
    private class INISection {
        /** Variable to hold any comments associated with this section. */
        private String mstrComment;

        /** Variable to hold the section name. */
        private String mstrName;

        /** Variable to hold the properties falling under this section. */
        private LinkedHashMap<String, INIProperty> mhmapProps;

        /**
         * Construct a new section object identified by the name specified in parameter.
         * 
         * @param pstrSection The new sections name.
         */
        public INISection(String pstrSection) {
            this.mstrName = pstrSection;
            this.mhmapProps = new LinkedHashMap<>();
        }

        /**
         * Construct a new section object identified by the name specified in parameter
         * and associated comments.
         * 
         * @param pstrSection  The new sections name.
         * @param pstrComments the comments associated with this section.
         */
        public INISection(String pstrSection, String pstrComments) {
            this.mstrName = pstrSection;
            this.mstrComment = delRemChars(pstrComments);
            this.mhmapProps = new LinkedHashMap<>();
        }

        /**
         * Sets the comments associated with this section.
         * 
         * @param pstrComments the comments
         */
        public void setSecComments(String pstrComments) {
            this.mstrComment = delRemChars(pstrComments);
        }

    

        /**
         * Creates or modifies the specified property value.
         * 
         * @param pstrProp     The name of the property to be created or modified.
         * @param pstrValue    The new value for the property.
         * @param pstrComments the associated comments
         */
        public void setProperty(String pstrProp, String pstrValue, String pstrComments) {
            this.mhmapProps.put(pstrProp, new INIProperty(pstrProp, pstrValue, pstrComments));
        }

   

        /**
         * Returns underlying value of the specified property.
         * 
         * @param pstrProp the property whose underlying value is to be etrieved.
         * @return the property value.
         */
        public INIProperty getProperty(String pstrProp) {
            INIProperty objRet = null;

            if (this.mhmapProps.containsKey(pstrProp)) {
                objRet = this.mhmapProps.get(pstrProp);
            }
            return objRet;
        }

        /**
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            Set<String> colKeys;
            Iterator<String> iter = null;
            INIProperty objProp = null;
            StringBuilder objBuf = new StringBuilder();

            if (this.mstrComment != null) {
                objBuf.append(addRemChars(this.mstrComment));
            }
            objBuf.append("[")
                   .append(this.mstrName)
                   .append("]\r\n");
            colKeys = this.mhmapProps.keySet();
            if (colKeys != null) {
                iter = colKeys.iterator();
                if (iter != null) {
                    while (iter.hasNext()) {
                        objProp = this.mhmapProps.get(iter.next());
                        objBuf.append(objProp);
                        objBuf.append("\r\n");
                    }
                }
            }
            return objBuf.toString();
        }
    }

    /*------------------------------------------------------------------------------
     * Private class representing the INI Property.
     *----------------------------------------------------------------------------*/
    /**
     * This class represents a key value pair called property in an INI file.
     * 
     * @author Prasad P. Khandekar
     * @version 1.0
     * @since 1.0
     */
    private class INIProperty {
        /** Variable to hold name of this property. */
        private String mstrName;
        /** Variable to hold value of this property. */
        private String mstrValue;
        /** Variable to hold comments associated with this property. */
        private String mstrComments;

        /**
         * Constructor
         * 
         * @param pstrName     the name of this property.
         * @param pstrValue    the value of this property.
         * @param pstrComments the comments associated with this property.
         */
        private INIProperty(String pstrName, String pstrValue, String pstrComments) {
            this.mstrName = pstrName;
            this.mstrValue = pstrValue;
            this.mstrComments = delRemChars(pstrComments);
        }

        /**
         * Returns value of this property. If value contains a reference to environment
         * avriable then this reference is replaced by actual value before the value is
         * returned.
         * 
         * @return the value of this property.
         */
        public String getPropValue() {
            int intStart;
            int intEnd = 0;
            String strVal = null;
            String strVar = null;
            String strRet;

            strRet = this.mstrValue;
            intStart = strRet.indexOf('%');
            try {
                if (intStart >= 0) {
                    intEnd = strRet.indexOf('%', intStart + 1);
                    strVar = strRet.substring(intStart + 1, intEnd);
                    strVal = mpropEnv.getProperty(strVar);
                    if (strVal != null) {
                        strRet = strRet.substring(0, intStart) + strVal + strRet.substring(intEnd + 1);
                    }
                }
            } catch (Exception ex) {
            }
            return strRet;
        }

        /**
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            String strRet = "";

            if (this.mstrComments != null) {
                strRet = addRemChars(mstrComments);
            }
            return strRet + this.mstrName + " = " + this.mstrValue;
        }
    }
}
