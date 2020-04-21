package rehaMail.Tools;

import java.io.IOException;
import java.io.InputStream;

import org.jdesktop.swingworker.SwingWorker;

import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.datatransfer.DataFlavor;
import com.sun.star.datatransfer.UnsupportedFlavorException;
import com.sun.star.datatransfer.XTransferable;
import com.sun.star.datatransfer.clipboard.XClipboard;
import com.sun.star.frame.XController;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.filter.RTFFilter;
import ag.ion.bion.officelayer.presentation.IPresentationDocument;
import ag.ion.bion.officelayer.spreadsheet.ISpreadsheetDocument;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.noa.NOAException;
import rehaMail.RehaMail;

public class OOTools {
    public OOTools() {

    }

    /*******************************************************************************************/

    /*******************************************************************************************/
    /*******************************************************************************************/

    public static ITextDocument starteWriterMitDatei(String url) {
        try {
            if (!RehaMail.officeapplication.isActive()) {
                RehaMail.starteOfficeApplication();
            }
            IDocumentService documentService = RehaMail.officeapplication.getDocumentService();
            DocumentDescriptor docdescript = new DocumentDescriptor();
            docdescript.setURL(url);
            docdescript.setHidden(false);
            // IDocument document =
            // documentService.constructNewDocument(IDocument.WRITER,docdescript );
            IDocument document = documentService.loadDocument(url, DocumentDescriptor.DEFAULT);
            ITextDocument textDocument = (ITextDocument) document;
            /*********************/
            XController xController = textDocument.getXTextDocument()
                                                  .getCurrentController();
            XTextViewCursorSupplier xTextViewCursorSupplier = UnoRuntime.queryInterface(XTextViewCursorSupplier.class,
                    xController);
            XTextViewCursor xtvc = xTextViewCursorSupplier.getViewCursor();
            xtvc.gotoStart(false);
            textDocument.getFrame()
                        .setFocus();

            return textDocument;

        } catch (OfficeApplicationException exception) {
            exception.printStackTrace();
        } catch (NOAException exception) {
            exception.printStackTrace();
        } catch (Throwable e) {

            e.printStackTrace();
        }
        return null;

    }

    public static ITextDocument starteWriterMitStream(InputStream is, String titel) {
        try {
            if (!RehaMail.officeapplication.isActive()) {
                RehaMail.starteOfficeApplication();
            }
            DocumentDescriptor d = new DocumentDescriptor();
            d.setTitle(titel);
            d.setFilterDefinition(RTFFilter.FILTER.getFilterDefinition(IDocument.WRITER));
            IDocumentService documentService = RehaMail.officeapplication.getDocumentService();
            IDocument document = documentService.constructNewDocument(IDocument.WRITER, DocumentDescriptor.DEFAULT);
            ITextDocument textDocument = (ITextDocument) document;
            textDocument.getViewCursorService()
                        .getViewCursor()
                        .getTextCursorFromStart()
                        .insertDocument(is, new RTFFilter());
            XController xController = textDocument.getXTextDocument()
                                                  .getCurrentController();
            XTextViewCursorSupplier xTextViewCursorSupplier = UnoRuntime.queryInterface(XTextViewCursorSupplier.class,
                    xController);
            XTextViewCursor xtvc = xTextViewCursorSupplier.getViewCursor();
            xtvc.gotoStart(false);
            textDocument.getFrame()
                        .setFocus();
            is.close();
            return textDocument;

        } catch (OfficeApplicationException exception) {
            exception.printStackTrace();
        } catch (NOAException exception) {
            exception.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        } catch (Throwable e) {

            e.printStackTrace();
        }
        return null;

    }

    public static ISpreadsheetDocument starteCalcMitDatei(String url) {
        try {
            if (!RehaMail.officeapplication.isActive()) {
                RehaMail.starteOfficeApplication();
            }
            IDocumentService documentService = RehaMail.officeapplication.getDocumentService();
            DocumentDescriptor docdescript = new DocumentDescriptor();
            docdescript.setURL(url);
            docdescript.setHidden(false);
            IDocument document = documentService.loadDocument(url, DocumentDescriptor.DEFAULT);
            // IDocument document = documentService.constructNewDocument(IDocument.CALC,
            // DocumentDescriptor.DEFAULT);
            ISpreadsheetDocument spreadsheetDocument = (ISpreadsheetDocument) document;
            /********************/
            spreadsheetDocument.getFrame()
                               .setFocus();
            return spreadsheetDocument;

        } catch (Throwable exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static void starteLeerenCalc() {
        try {
            IDocumentService documentService = RehaMail.officeapplication.getDocumentService();
            IDocument document = documentService.constructNewDocument(IDocument.CALC, DocumentDescriptor.DEFAULT);
            ISpreadsheetDocument spreadsheetDocument = (ISpreadsheetDocument) document;
            spreadsheetDocument.getFrame()
                               .setFocus();
        } catch (Throwable exception) {
            exception.printStackTrace();
        }
    }

    public static void starteLeerenImpress() {
        try {
            IDocumentService documentService = RehaMail.officeapplication.getDocumentService();
            IDocument document = documentService.constructNewDocument(IDocument.IMPRESS, DocumentDescriptor.DEFAULT);
            IPresentationDocument presentationDocument = (IPresentationDocument) document;
            presentationDocument.getFrame()
                                .setFocus();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    public static void ooOrgAnmelden() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws java.lang.Exception {
                IDocumentDescriptor docdescript = new DocumentDescriptor();
                docdescript.setHidden(true);
                IDocument document = null;
                ITextDocument textDocument = null;
                RehaMail.thisFrame.setCursor(RehaMail.thisClass.wartenCursor);
                try {
                    if (!RehaMail.officeapplication.isActive()) {
                        try {
                            RehaMail.starteOfficeApplication();
                        } catch (Throwable e) {

                            e.printStackTrace();
                        }
                    }

                    IDocumentService documentService = RehaMail.officeapplication.getDocumentService();
                    document = documentService.constructNewDocument(IDocument.WRITER, docdescript);
                    textDocument = (ITextDocument) document;
                    textDocument.close();
                } catch (OfficeApplicationException exception) {
                    exception.printStackTrace();
                } catch (NOAException exception) {
                    exception.printStackTrace();
                }
                return null;
            }

        }.execute();
    }

    /*******************************************************/
    public static void holeClipBoard() {
        try {
            XComponentContext xComponentContext;

            xComponentContext = com.sun.star.comp.helper.Bootstrap.bootstrap();
            XMultiComponentFactory xMultiComponentFactory;
            xMultiComponentFactory = (XMultiComponentFactory) RehaMail.officeapplication.getDocumentService();

            Object oClipboard = xMultiComponentFactory.createInstanceWithContext(
                    "com.sun.star.datatransfer.clipboard.SystemClipboard", xComponentContext);
            XClipboard xClipboard = UnoRuntime.queryInterface(XClipboard.class, oClipboard);

            // ---------------------------------------------------
            // get a list of formats currently on the clipboard
            // ---------------------------------------------------

            XTransferable xTransferable = xClipboard.getContents();

            DataFlavor[] aDflvArr = xTransferable.getTransferDataFlavors();

            // print all available formats

            // System.out.println("Reading the clipboard...");
            // System.out.println("Available clipboard formats:");

            DataFlavor aUniFlv = null;

            for (int i = 0; i < aDflvArr.length; i++) {
                // System.out.println( "MimeType: " +
                // aDflvArr[i].MimeType +
                // " HumanPresentableName: " +
                // aDflvArr[i].HumanPresentableName );

                // if there is the format unicode text on the clipboard save the
                // corresponding DataFlavor so that we can later output the string

                if (aDflvArr[i].MimeType.equals("text/plain;charset=utf-16")) {
                    aUniFlv = aDflvArr[i];
                }
            }
            try {
                if (aUniFlv != null) {
                    // System.out.println("Unicode text on the clipboard...");
                    Object aData = xTransferable.getTransferData(aUniFlv);
                }
            } catch (UnsupportedFlavorException ex) {
                System.err.println("Requested format is not available");
            }
        } catch (OfficeApplicationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (BootstrapException e) {
            e.printStackTrace();
        }

    }

}
