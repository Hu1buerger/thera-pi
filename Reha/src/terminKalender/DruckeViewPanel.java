package terminKalender;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXPanel;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameContainer;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.text.HoriOrientation;
import com.sun.star.text.TextContentAnchorType;
import com.sun.star.text.VertOrientation;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;

import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.noa.graphic.GraphicInfo;
import environment.Path;
import gui.Cursors;
import hauptFenster.Reha;
import office.OOService;
import umfeld.Betriebsumfeld;

class DruckeViewPanel extends SwingWorker<Void, Void> {
    private JXPanel printPan = null;
    private BufferedImage bufimg = null;

    public void setPrintPanel(JXPanel pan) {
        this.printPan = pan;
        execute();
    }

    @Override
    protected Void doInBackground() throws Exception {
        if (printPan == null) {
            return null;
        }
        try {
            Reha.getThisFrame()
                .setCursor(Cursors.wartenCursor);
            int pixelWidth = printPan.getWidth();
            int pixelHeight = printPan.getHeight();
            bufimg = new BufferedImage(pixelWidth, pixelHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bufimg.createGraphics();
            printPan.paint(g2d);
            g2d.dispose();
            speichernQualitaet("", 1.0F);

            String url = Path.Instance.getProghome() + "vorlagen/" + Betriebsumfeld.getAktIK() + "/terminshot_.ott";
            IDocumentService documentService = new OOService().getOfficeapplication().getDocumentService();
            IDocumentDescriptor docdescript = new DocumentDescriptor();
            docdescript.setHidden(false);
            docdescript.setAsTemplate(true);
            IDocument document = null;
            document = documentService.loadDocument(url, docdescript);
            ITextDocument textDocument = (ITextDocument) document;

            boolean useStream = false;

            String imagePath = (Path.Instance.getProghome() + "ScreenShots/termin__temp.jpg").replace("\\", "/");

            // Tip aus dem NOA-Forum
            imagePath = "file:///" + imagePath;

            GraphicInfo graphicInfo = null;

            float fx = new Float(pixelWidth);
            float fy = new Float(pixelHeight);

            float verhaeltnis = fx / fy;
            float rasterx = new Float(27900.000);
            float testy = rasterx / verhaeltnis;
            float test2y = 0.00F;
            if (testy > 19000.00F) {
                test2y = 19000.00F * 100.0F / testy;
                fx = rasterx / 100 * test2y;
                fy = 19000.00F;
                // System.out.println("H�hentest = "+test2y);

            } else {
                fx = rasterx;
                fy = testy;
            }

            // System.out.println("Die neuen Maße sind X:"+new Float(fx).intValue()+" /
            // Y:"+new Float(fy).intValue());
            XMultiServiceFactory multiServiceFactory = null;
            XTextCursor xcursor = null;
            XTextCursor xTextCursor = null;
            if (!useStream) {
                // with url
                graphicInfo = new GraphicInfo(imagePath, new Float(fx).intValue(), false, new Float(fy).intValue(),
                        false, VertOrientation.TOP, HoriOrientation.LEFT, TextContentAnchorType.AS_CHARACTER);
                /*
                 * graphicInfo = new GraphicInfo(imagePath,
                 * Float.valueOf(pixelWidth).intValue(), true,
                 * Float.valueOf(pixelHeight).intValue(), true, VertOrientation.TOP,
                 * HoriOrientation.LEFT, TextContentAnchorType.AT_FRAME);
                 */
                Thread.sleep(100);

                multiServiceFactory = UnoRuntime.queryInterface(XMultiServiceFactory.class,
                        textDocument.getXTextDocument());
                XText xText = textDocument.getXTextDocument()
                                          .getText();

                xTextCursor = xText.createTextCursor();

            } else {

                // System.out.println("Pixe des Bildes = X:"+pixelWidth+" / Y:"+pixelHeight);
                // System.out.println("Seitenverhältnis = "+verhaeltnis);
                /*
                 * graphicInfo = new GraphicInfo(imagePath, new Float(fx).intValue(), false, new
                 * Float(fy).intValue(), false, VertOrientation.TOP, HoriOrientation.LEFT,
                 * TextContentAnchorType.AS_CHARACTER);
                 *
                 * System.out.println(graphicInfo.getUrl());
                 */

                // URL =
                // file:/C:/RehaVerwaltung/Reha/file:/RehaVerwaltung/ScreenShots/termin__temp.jpg

                graphicInfo = new GraphicInfo(new FileInputStream(imagePath), new Float(fx).intValue(), false,
                        new Float(fy).intValue(), false, VertOrientation.TOP, HoriOrientation.LEFT,
                        TextContentAnchorType.AS_CHARACTER);
                Thread.sleep(100);

                multiServiceFactory = UnoRuntime.queryInterface(XMultiServiceFactory.class,
                        textDocument.getXTextDocument());
                XText xText = textDocument.getXTextDocument()
                                          .getText();

                xTextCursor = xText.createTextCursor();

                // XComponentContext
                /*
                 * try{ XComponentContext xcomponentcontext = (XComponentContext)
                 * Bootstrap.createInitialComponentContext(null); XGraphic xGrafik =
                 * (XGraphic)getGraphicFromURL(xcomponentcontext, imagePath); }catch(Exception
                 * ex){ System.out.println("Exception in XComponentContext");
                 * ex.printStackTrace();
                 * System.out.println("**************Ende Exception in XComponentContext"); }
                 */

                /*
                 *
                 * Object oFCProvider =
                 * _xMCF.createInstanceWithContext("com.sun.star.ucb.FileContentProvider",
                 * this.m_xContext); XFileIdentifierConverter xFileIdentifierConverter =
                 * (XFileIdentifierConverter)
                 * UnoRuntime.queryInterface(XFileIdentifierConverter.class, oFCProvider);
                 * String sImageUrl =
                 * xFileIdentifierConverter.getFileURLFromSystemPath(_sImageSystemPath,
                 * oFile.getAbsolutePath());
                 *
                 * Object oFCProvider = multiServiceFactory.createInstanceWithContext(
                 * "com.sun.star.ucb.FileContentProvider", xText);
                 *
                 * XFileIdentifierConverter xFileIdentifierConverter =
                 * (XFileIdentifierConverter)
                 * UnoRuntime.queryInterface(XFileIdentifierConverter.class, oFCProvider);
                 * String sImageUrl =
                 * xFileIdentifierConverter.getFileURLFromSystemPath(_sImageSystemPath,
                 * oFile.getAbsolutePath()); XGraphic xGraphic = getGraphic(sImageUrl);
                 */
                Thread.sleep(100);
            }

            embedGraphic(graphicInfo, multiServiceFactory, xTextCursor);

            /*
             * ITextContentService textContentService = textDocument.getTextService()
             * .getTextContentService();
             *
             * ITextCursor textCursor = textDocument.getTextService().getText()
             * .getTextCursorService().getTextCursor();
             *
             *
             * Thread.sleep(100); ITextDocumentImage textDocumentImage = textContentService
             * .constructNewImage(graphicInfo);
             * textContentService.insertTextContent(textCursor.getEnd(), textDocumentImage);
             */

            Reha.getThisFrame()
                .setCursor(Cursors.normalCursor);
        } catch (Exception ex) {
            Reha.getThisFrame()
                .setCursor(Cursors.normalCursor);
            ex.printStackTrace();
        }

        return null;
    }

    private void embedGraphic(GraphicInfo grProps, XMultiServiceFactory xMSF, XTextCursor xCursor) {

        XNameContainer xBitmapContainer = null;
        XText xText = xCursor.getText();
        XTextContent xImage = null;
        String internalURL = null;
        String url = null;

        try {
            xBitmapContainer = UnoRuntime.queryInterface(XNameContainer.class,
                    xMSF.createInstance("com.sun.star.drawing.BitmapTable"));
            xImage = UnoRuntime.queryInterface(XTextContent.class,
                    xMSF.createInstance("com.sun.star.text.TextGraphicObject"));
            XPropertySet xProps = UnoRuntime.queryInterface(XPropertySet.class, xImage);

            url = "file:///" + Path.Instance.getProghome() + "ScreenShots/termin__temp.jpg";

            xBitmapContainer.insertByName("someID", url);
            // xBitmapContainer.insertByName("someID", grProps.getUrl());
            internalURL = AnyConverter.toString(xBitmapContainer.getByName("someID"));

            xProps.setPropertyValue("AnchorType", com.sun.star.text.TextContentAnchorType.AS_CHARACTER);
            xProps.setPropertyValue("GraphicURL", internalURL);
            xProps.setPropertyValue("Width", grProps.getWidth());
            xProps.setPropertyValue("Height", grProps.getHeight());

            xText.insertTextContent(xCursor, xImage, false);

            xBitmapContainer.removeByName("someID");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to insert Graphic");
            System.out.println(url);
        }
    }

    private void speichernQualitaet(String stitel, Float fQuality) {

        IIOImage imgq = new IIOImage(bufimg, null, null);
        ImageWriter writer = ImageIO.getImageWritersBySuffix("jpg")
                                    .next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(fQuality);
        File fimg = new File(Path.Instance.getProghome() + "ScreenShots/termin__temp.jpg");

        try {
            writer.setOutput(ImageIO.createImageOutputStream(fimg));
            writer.write(null, imgq, param);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

}
