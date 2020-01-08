package org.thera_pi.common.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

public class JpegWriter {

    public static byte[] bufferedImageToByteArray(BufferedImage img) throws IOException {
    	ByteArrayOutputStream os = bufferedImageToOutputStream(img);
        if (os != null) {
            return os.toByteArray();
        } else {
            return null;
        }
    }

    public static ByteArrayOutputStream bufferedImageToOutputStream(BufferedImage img) throws IOException {
        if (img != null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            
            float quality = 1.0f;
            ImageWriter writer = null;
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
            if (iter.hasNext()) {
              writer = iter.next();
            }
            ImageOutputStream ios = ImageIO.createImageOutputStream(os);
            writer.setOutput(ios);
            ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
            iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwparam.setCompressionQuality(quality);
            writer.write(null, new IIOImage(img, null, null), iwparam);
            ios.flush();
            writer.dispose();
            ios.close();
            
            return os;
        } else {
            return null;
        }
    }

}
