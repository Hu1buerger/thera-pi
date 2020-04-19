package CommonTools;

import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.sun.star.awt.XTopWindow;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.sheet.XSheetCellCursor;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.sheet.XSpreadsheets;
import com.sun.star.style.XStyle;
import com.sun.star.style.XStyleFamiliesSupplier;
import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;
import com.sun.star.text.XText;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextField;
import com.sun.star.text.XTextFieldsSupplier;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextTableCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.view.XLineCursor;

import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.spreadsheet.ISpreadsheetDocument;
import ag.ion.bion.officelayer.text.IText;
import ag.ion.bion.officelayer.text.ITextCursor;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextRange;
import ag.ion.bion.officelayer.text.ITextTableCell;
import ag.ion.bion.officelayer.text.IViewCursor;
import ag.ion.bion.officelayer.text.TextException;
import ag.ion.noa.NOAException;
import ag.ion.noa.printing.IPrinter;
import ag.ion.noa.search.ISearchResult;
import ag.ion.noa.search.SearchDescriptor;

public class OOTools {

    public static synchronized void loescheLeerenPlatzhalter(ITextDocument textDocument, ITextField placeholders) {
        try {
            IViewCursor viewCursor = textDocument.getViewCursorService()
                                                 .getViewCursor();
            viewCursor.goToRange(placeholders.getTextRange(), false);
            XController xController = textDocument.getXTextDocument()
                                                  .getCurrentController();
            XTextViewCursorSupplier xTextViewCursorSupplier = UnoRuntime.queryInterface(XTextViewCursorSupplier.class,
                    xController);
            XLineCursor xLineCursor = UnoRuntime.queryInterface(XLineCursor.class,
                    xTextViewCursorSupplier.getViewCursor());
            xLineCursor.gotoStartOfLine(false);
            xLineCursor.gotoEndOfLine(true);
            ITextCursor textCursor = viewCursor.getTextCursorFromStart();
            textCursor.goLeft((short) 1, false);
            textCursor.gotoRange(viewCursor.getTextCursorFromEnd()
                                           .getEnd(),
                    true);
            textCursor.setString("");
        } catch (java.lang.IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    public static XTextTableCursor doMergeCellsInTextTabel(XTextTable table, String startCell, String endCell) {
        // System.out.println(startCell+" / "+endCell);
        XTextTableCursor cursor = table.createCursorByCellName(startCell);
        cursor.gotoCellByName(endCell, true);
        cursor.mergeRange();
        return cursor;
    }

    public static void setOneCellProperty(ITextTableCell cell, boolean italic, boolean bold, boolean underline,
            int color, float size) {
        // ITextTableCellProperties props = cell.getProperties();
        // XPropertySet xprops = props.getXPropertySet();
        try {
            cell.getCharacterProperties()
                .setFontItalic(italic);
            cell.getCharacterProperties()
                .setFontBold(bold);
            cell.getCharacterProperties()
                .setFontUnderline(underline);
            cell.getCharacterProperties()
                .setFontColor(color);
            cell.getCharacterProperties()
                .setFontSize(size);
        } catch (TextException e) {
            e.printStackTrace();
        }

    }







    /**************************************************************************************/
    private static ArrayList<XTextRange> testePlatzhalter(XTextDocument xTextDocument) {
        if (xTextDocument == null) {
            return null;
        }
        long millis = 50;
        ArrayList<XTextRange> arrayList = new ArrayList<XTextRange>();
        try {
            XTextFieldsSupplier xTextFieldsSupplier = UnoRuntime.queryInterface(XTextFieldsSupplier.class,
                    xTextDocument);
            Thread.sleep(millis);
            XEnumerationAccess xEnumerationAccess = xTextFieldsSupplier.getTextFields();
            Thread.sleep(millis);
            XEnumeration xEnumeration = xEnumerationAccess.createEnumeration();
            while (xEnumeration.hasMoreElements()) {
                Object object = xEnumeration.nextElement();
                Thread.sleep(millis);
                XTextField xTextField = UnoRuntime.queryInterface(XTextField.class, object);
                XServiceInfo xInfo = UnoRuntime.queryInterface(XServiceInfo.class, xTextField);
                XTextRange range = xTextField.getAnchor();
                // nur die Platzhalter
                if (xInfo.supportsService("com.sun.star.text.TextField.JumpEdit") /*
                                                                                   * || xInfo.supportsService(
                                                                                   * "com.sun.star.text.TextField.User")
                                                                                   */) {
                    arrayList.add(range);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        return arrayList;
    }

    /*******************************************************************************************/
    private static boolean sucheNachPlatzhalter(ITextDocument document) {
        IText text = document.getTextService()
                             .getText();
        String stext = text.getText();
        int start = 0;
        // int end = 0;
        String dummy;
        // int sysvar = -1;
        boolean noendfound = false;
        while ((start = stext.indexOf("^")) >= 0) {
            noendfound = true;
            for (int i = 1; i < 150; i++) {
                if (stext.substring(start + i, start + (i + 1))
                         .equals("^")) {
                    dummy = stext.substring(start, start + (i + 1));
                    String sanweisung = dummy.toString()
                                             .replace("^", "");
                    Object ret = JOptionPane.showInputDialog(null,
                            "<html>Bitte Wert eingeben für: --\u003E<b> " + sanweisung + " </b> &nbsp; </html>",
                            "Platzhalter gefunden", 1);
                    if (ret == null) {
                        return true;
                        // sucheErsetze(dummy,"");
                    } else {
                        // sucheErsetze(document,dummy,((String)ret).trim(),false);
                        sucheErsetze(document, dummy, ((String) ret).trim(), true);
                        stext = text.getText();
                    }
                    noendfound = false;
                    break;
                }
            }
            if (noendfound) {
                JOptionPane.showMessageDialog(null, "Der Baustein ist fehlerhaft, eine Übernahme deshalb nicht möglich"
                        + "\n\nVermutete Ursache des Fehlers: es wurde ein Start-/Endezeichen '^' für Variable vergessen\n");
                return false;
            }
        }
        return true;
    }

    private static void sucheErsetze(ITextDocument document, String suchenach, String ersetzemit, boolean alle) {
        SearchDescriptor searchDescriptor = new SearchDescriptor(suchenach);
        searchDescriptor.setIsCaseSensitive(true);
        ISearchResult searchResult = null;
        if (alle) {
            searchResult = document.getSearchService()
                                   .findAll(searchDescriptor);
        } else {
            searchResult = document.getSearchService()
                                   .findFirst(searchDescriptor);
        }

        if (!searchResult.isEmpty()) {
            ITextRange[] textRanges = searchResult.getTextRanges();
            for (int resultIndex = 0; resultIndex < textRanges.length; resultIndex++) {
                textRanges[resultIndex].setText(ersetzemit);

            }
        }
    }

    /*******************************************************************************************/
    public static synchronized void setzePapierFormat(ITextDocument textDocument, int hoch, int breit)
            throws NoSuchElementException, WrappedTargetException, UnknownPropertyException, PropertyVetoException,
            IllegalArgumentException {
        XTextDocument xTextDocument = textDocument.getXTextDocument();
        XStyleFamiliesSupplier xSupplier = UnoRuntime.queryInterface(XStyleFamiliesSupplier.class, xTextDocument);
        XNameContainer family = UnoRuntime.queryInterface(XNameContainer.class, xSupplier.getStyleFamilies()
                                                                                         .getByName("PageStyles"));
        XStyle xStyle = UnoRuntime.queryInterface(XStyle.class, family.getByName("Standard"));
        XPropertySet xStyleProps = UnoRuntime.queryInterface(XPropertySet.class, xStyle);
        /*
         * com.sun.star.beans.Property[] props =
         * xStyleProps.getPropertySetInfo().getProperties(); for (int i = 0; i <
         * props.length; i++) { //System.out.println(props[i] .Name + " = " +
         * xStyleProps.getPropertyValue(props[i].Name)); } //z.B. für A5
         *
         */
        xStyleProps.setPropertyValue("Height", hoch);
        xStyleProps.setPropertyValue("Width", breit);
    }

    public static void setzePapierFormatCalc(ISpreadsheetDocument document, int hoch, int breit)
            throws NoSuchElementException, WrappedTargetException, UnknownPropertyException, PropertyVetoException,
            IllegalArgumentException {
        XSpreadsheetDocument xSpreadSheetDocument = document.getSpreadsheetDocument();
        XStyleFamiliesSupplier xSupplier = UnoRuntime.queryInterface(XStyleFamiliesSupplier.class,
                xSpreadSheetDocument);
        XNameContainer family = UnoRuntime.queryInterface(XNameContainer.class, xSupplier.getStyleFamilies()
                                                                                         .getByName("PageStyles"));
        XStyle xStyle = UnoRuntime.queryInterface(XStyle.class, family.getByName("Standard"));
        XPropertySet xStyleProps = UnoRuntime.queryInterface(XPropertySet.class, xStyle);
        xStyleProps.setPropertyValue("Height", hoch);
        xStyleProps.setPropertyValue("Width", breit);
    }

    public static void setzeRaenderCalc(ISpreadsheetDocument document, int oben, int unten, int links, int rechts)
            throws NoSuchElementException, WrappedTargetException, UnknownPropertyException, PropertyVetoException,
            IllegalArgumentException {
        XSpreadsheetDocument xSpreadSheetDocument = document.getSpreadsheetDocument();
        XStyleFamiliesSupplier xSupplier = UnoRuntime.queryInterface(XStyleFamiliesSupplier.class,
                xSpreadSheetDocument);
        XNameContainer family = UnoRuntime.queryInterface(XNameContainer.class, xSupplier.getStyleFamilies()
                                                                                         .getByName("PageStyles"));
        XStyle xStyle = UnoRuntime.queryInterface(XStyle.class, family.getByName("Standard"));
        XPropertySet xStyleProps = UnoRuntime.queryInterface(XPropertySet.class, xStyle);
        xStyleProps.setPropertyValue("TopMargin", oben);
        xStyleProps.setPropertyValue("BottomMargin", unten);
        xStyleProps.setPropertyValue("LeftMargin", links);
        xStyleProps.setPropertyValue("RightMargin", rechts);
    }

    public static void setzeRaender(ITextDocument textDocument, int oben, int unten, int links, int rechts)
            throws NoSuchElementException, WrappedTargetException, UnknownPropertyException, PropertyVetoException,
            IllegalArgumentException {
        XTextDocument xTextDocument = textDocument.getXTextDocument();
        XStyleFamiliesSupplier xSupplier = UnoRuntime.queryInterface(XStyleFamiliesSupplier.class, xTextDocument);
        XNameContainer family = UnoRuntime.queryInterface(XNameContainer.class, xSupplier.getStyleFamilies()
                                                                                         .getByName("PageStyles"));
        XStyle xStyle = UnoRuntime.queryInterface(XStyle.class, family.getByName("Standard"));
        XPropertySet xStyleProps = UnoRuntime.queryInterface(XPropertySet.class, xStyle);
        xStyleProps.setPropertyValue("TopMargin", oben);
        xStyleProps.setPropertyValue("BottomMargin", unten);
        xStyleProps.setPropertyValue("LeftMargin", links);
        xStyleProps.setPropertyValue("RightMargin", rechts);
    }

    /*************************************************************************/
    public static void inDenVordergrund(ITextDocument textDocumentx) {
        ITextDocument textDocument = textDocumentx;
        IFrame officeFrame = textDocument.getFrame();
        XFrame xFrame = officeFrame.getXFrame();
        XTopWindow topWindow = UnoRuntime.queryInterface(XTopWindow.class, xFrame.getContainerWindow());
        // hier beide methoden, beide sind nötig
        xFrame.activate();
        topWindow.toFront();
    }

    /*
     * workaround um ausgefülltes Formular in den Vordergrund zu holen: legt ein
     * temporäres Dokument an (neue Dokumente werden im Vordergrund geöffnet) setzt
     * anschließend den Fokus auf das 'eigentliche' Dokument und schließt das
     * temporäre Dokument wieder
     */
    public static void bringDocToFront(IDocumentService iDocumentService, ITextDocument doc,
            IDocumentDescriptor descriptor, String url) {
        final IDocumentService xService = iDocumentService;
        final ITextDocument xdoc = doc;
        final IDocumentDescriptor xDescriptor = descriptor;
        final String xUrl = url;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                IDocument dummyDocument;
                try {
                    dummyDocument = xService.loadDocument(xUrl, xDescriptor);
                    xdoc.getFrame()
                        .getXFrame()
                        .getContainerWindow()
                        .setVisible(true);
                    xdoc.getFrame()
                        .getXFrame()
                        .getComponentWindow()
                        .setFocus();
                    /*
                     * if (dummyDocument.getFrame().getXFrame().isTop()){
                     * System.out.println("dummyDocument on Top"); }else{
                     * System.out.println("dummyDocument NOT on Top"); }
                     */
                    dummyDocument.getFrame()
                                 .getXFrame()
                                 .getContainerWindow()
                                 .setVisible(false);
                    dummyDocument.close();
                } catch (NOAException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public static void druckerSetzen(ITextDocument textDocument, String drucker) {
        /**********************/
        if (drucker != null) {
            String druckerName = null;
            try {
                druckerName = textDocument.getPrintService()
                                          .getActivePrinter()
                                          .getName();
            } catch (NOAException e) {
                e.printStackTrace();
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            // Wenn nicht gleich wie im Übergebenen Parameter angegeben -> Drucker wechseln

            if (!drucker.equals(druckerName)) {
                try {
                    IPrinter printer = textDocument.getPrintService()
                                                   .createPrinter(drucker);

                    textDocument.getPrintService()
                                .setActivePrinter(printer);
                } catch (NullPointerException | NOAException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*********************** OO-Calc Funktionen *******************************/
    public static void doColWidth(ISpreadsheetDocument spreadsheetDocument, String sheetName, int col_first,
            int col_last, int width) throws NoSuchElementException, WrappedTargetException, IndexOutOfBoundsException,
            UnknownPropertyException, PropertyVetoException, IllegalArgumentException {
        XSpreadsheets spreadsheets = spreadsheetDocument.getSpreadsheetDocument()
                                                        .getSheets();
        XSpreadsheet spreadsheet1 = UnoRuntime.queryInterface(XSpreadsheet.class, spreadsheets.getByName(sheetName));
        XCellRange xCellRange = spreadsheet1.getCellRangeByPosition(0, 0, col_last, 0);
        com.sun.star.table.XColumnRowRange xColRowRange = UnoRuntime.queryInterface(
                com.sun.star.table.XColumnRowRange.class, xCellRange);
        com.sun.star.beans.XPropertySet xPropSet = null;
        com.sun.star.table.XTableColumns xColumns = xColRowRange.getColumns();
        for (int i = col_first; i <= col_last; i++) {
            Object aColumnObj = xColumns.getByIndex(i);
            xPropSet = UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, aColumnObj);
            xPropSet.setPropertyValue("Width", width);
        }
    }

    public static void doColTextAlign(ISpreadsheetDocument spreadsheetDocument, String sheetName, int col_first,
            int col_last, int col_textalign) throws NoSuchElementException, WrappedTargetException,
            IndexOutOfBoundsException, UnknownPropertyException, PropertyVetoException, IllegalArgumentException {
        XSpreadsheets spreadsheets = spreadsheetDocument.getSpreadsheetDocument()
                                                        .getSheets();
        XSpreadsheet spreadsheet1 = UnoRuntime.queryInterface(XSpreadsheet.class, spreadsheets.getByName(sheetName));
        XCellRange xCellRange = spreadsheet1.getCellRangeByPosition(0, 0, col_last, 0);
        com.sun.star.table.XColumnRowRange xColRowRange = UnoRuntime.queryInterface(
                com.sun.star.table.XColumnRowRange.class, xCellRange);
        com.sun.star.beans.XPropertySet xPropSet = null;
        com.sun.star.table.XTableColumns xColumns = xColRowRange.getColumns();
        for (int i = col_first; i <= col_last; i++) {
            Object aColumnObj = xColumns.getByIndex(i);
            xPropSet = UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, aColumnObj);
            xPropSet.setPropertyValue("HoriJustify", col_textalign);
        }
    }

    public static void doColNumberFormat(ISpreadsheetDocument spreadsheetDocument, String sheetName, int col_first,
            int col_last, int col_numberformat) throws NoSuchElementException, WrappedTargetException,
            IndexOutOfBoundsException, UnknownPropertyException, PropertyVetoException, IllegalArgumentException {
        XSpreadsheets spreadsheets = spreadsheetDocument.getSpreadsheetDocument()
                                                        .getSheets();
        XSpreadsheet spreadsheet1 = UnoRuntime.queryInterface(XSpreadsheet.class, spreadsheets.getByName(sheetName));
        XCellRange xCellRange = spreadsheet1.getCellRangeByPosition(0, 0, col_last, 0);
        com.sun.star.table.XColumnRowRange xColRowRange = UnoRuntime.queryInterface(
                com.sun.star.table.XColumnRowRange.class, xCellRange);
        com.sun.star.beans.XPropertySet xPropSet = null;
        com.sun.star.table.XTableColumns xColumns = xColRowRange.getColumns();
        for (int i = col_first; i <= col_last; i++) {
            Object aColumnObj = xColumns.getByIndex(i);
            xPropSet = UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, aColumnObj);
            xPropSet.setPropertyValue("NumberFormat", col_numberformat);
        }
    }



    public static void doCellValue(XSheetCellCursor cellCursor, int col, int row, Object value)
            throws IndexOutOfBoundsException {
        XCell cell = cellCursor.getCellByPosition(col, row);
        XText cellText;
        if (value instanceof Double) {
            cell.setValue((Double) value);
        } else if (value instanceof String) {
            cellText = UnoRuntime.queryInterface(XText.class, cell);
            cellText.setString((String) value);
        } else if (value instanceof Integer) {
            cell.setValue((Integer) value);
        } else if (value instanceof Date) {
            // System.out.println("date");
            // cell.setValue( ((Date)value).getTime());
        } else if (value instanceof Long) {
            cell.setValue((Long) value);
        }
    }



    public static void doCellFormula(XSheetCellCursor cellCursor, int col, int row, String formula)
            throws IndexOutOfBoundsException {
        XCell cell = cellCursor.getCellByPosition(col, row);
        cell.setFormula(formula);
    }

    public static void doCellColor(XSheetCellCursor cellCursor, int col, int row, int color)
            throws IndexOutOfBoundsException, UnknownPropertyException, PropertyVetoException, IllegalArgumentException,
            WrappedTargetException {
        XCell cell = cellCursor.getCellByPosition(col, row);
        UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, cell);
        com.sun.star.beans.XPropertySet xPropSet = null;
        xPropSet = UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, cell);
        xPropSet.setPropertyValue("CharColor", color);


    }

    public static void doCellFontBold(XSheetCellCursor cellCursor, int col, int row) throws IndexOutOfBoundsException,
            UnknownPropertyException, PropertyVetoException, IllegalArgumentException, WrappedTargetException {
        XCell cell = cellCursor.getCellByPosition(col, row);
        UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, cell);
        com.sun.star.beans.XPropertySet xPropSet = null;
        xPropSet = UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, cell);
        xPropSet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.BOLD);
        /*
         * Beispiele für Fonthandling xPropSet.setPropertyValue("CharFontStyleName",
         * String.valueOf("Times New Roman")); xPropSet.setPropertyValue("CharWeight",
         * new Float(com.sun.star.awt.FontWeight.NORMAL));
         * xPropSet.setPropertyValue("CharHeight", new Float(12));
         */
    }



    public static String doOODate(String datum) {
        String aDateStr = null;
        try {
            aDateStr = datum.substring(3, 5) + "/" + datum.substring(0, 2) + "/" + datum.substring(6);
        } catch (NullPointerException ex) {
            aDateStr = "";
        }
        return String.valueOf(aDateStr);
    }

    public static void doCellFontSize(XSheetCellCursor cellCursor, int col, int row, Float size)
            throws IndexOutOfBoundsException, UnknownPropertyException, PropertyVetoException, IllegalArgumentException,
            WrappedTargetException {
        XCell cell = cellCursor.getCellByPosition(col, row);
        UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, cell);
        com.sun.star.beans.XPropertySet xPropSet = null;
        xPropSet = UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, cell);
        xPropSet.setPropertyValue("CharHeight", size);
        xPropSet.setPropertyValue("CharWeight", com.sun.star.awt.FontWeight.NORMAL);
        /*
         * Beispiele für Fonthandling xPropSet.setPropertyValue("CharFontStyleName", new
         * String("Times New Roman")); xPropSet.setPropertyValue("CharWeight", new
         * Float(com.sun.star.awt.FontWeight.NORMAL));
         * xPropSet.setPropertyValue("CharHeight", new Float(12));
         */
    }

    public static void doCellFontName(XSheetCellCursor cellCursor, int col, int row, String fontname)
            throws IndexOutOfBoundsException, UnknownPropertyException, PropertyVetoException, IllegalArgumentException,
            WrappedTargetException {
        XCell cell = cellCursor.getCellByPosition(col, row);
        UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, cell);
        com.sun.star.beans.XPropertySet xPropSet = null;
        xPropSet = UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, cell);
        xPropSet.setPropertyValue("CharFontName", fontname);
        // PropSet.setPropertyValue( "CharWeight",com.sun.star.awt.FontWeight.NORMAL );
        /*
         * Beispiele für Fonthandling xPropSet.setPropertyValue("CharFontStyleName", new
         * String("Times New Roman")); xPropSet.setPropertyValue("CharWeight", new
         * Float(com.sun.star.awt.FontWeight.NORMAL));
         * xPropSet.setPropertyValue("CharHeight", new Float(12));
         */
    }

    public static void doCellDateFormatGerman(ISpreadsheetDocument spreadsheetDocument, XSheetCellCursor cellCursor,
            int col, int row, boolean jahrvierstellig) throws IndexOutOfBoundsException, UnknownPropertyException,
            PropertyVetoException, IllegalArgumentException, WrappedTargetException {
        XCell cell = cellCursor.getCellByPosition(col, row);

        com.sun.star.util.XNumberFormatsSupplier xNumberFormatsSupplier = UnoRuntime.queryInterface(
                com.sun.star.util.XNumberFormatsSupplier.class, spreadsheetDocument.getSpreadsheetDocument());
        /******************/
        if (xNumberFormatsSupplier == null) {
            System.out.println("XNumberFormatsSupplier = null");
            return;
        }
        /******************/
        com.sun.star.util.XNumberFormats xNumberFormats = xNumberFormatsSupplier.getNumberFormats();
        /******************/
        com.sun.star.util.XNumberFormatTypes xNumberFormatTypes = UnoRuntime.queryInterface(
                com.sun.star.util.XNumberFormatTypes.class, xNumberFormats);
        /******************/
        com.sun.star.lang.Locale aLocale = new com.sun.star.lang.Locale();
        int nFormat = xNumberFormatTypes.getStandardFormat(com.sun.star.util.NumberFormat.DATE, aLocale);
        /******************/
        com.sun.star.beans.XPropertySet xPropSet = UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class,
                cell);
        xPropSet.setPropertyValue("NumberFormat", nFormat - (jahrvierstellig ? 1 : 0));
    }

    /*******************************************************/
}


