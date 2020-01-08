package org.thera_pi.nebraska.gui.utils.pdf;

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.sun.pdfview.PDFFile;

public class PDFDrucker {

    public static void setup(String fname) throws IOException {

        /******************************
         *
         *
         */
        File f = new File(fname);
        FileInputStream fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();
        ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        PDFFile pdfFile = new PDFFile(bb); // Create PDF Print Page
        PDFPrintPage pages = new PDFPrintPage(pdfFile);

        // Create Print Job
        PrinterJob pjob = PrinterJob.getPrinterJob();
        PageFormat pf = PrinterJob.getPrinterJob()
                                  .defaultPage();
        final Paper paper = new Paper();
        paper.setSize(595.f, 842.f);
        paper.setImageableArea(0., 0., 595., 842.);
        int width = (int) paper.getWidth();
        int height = (int) paper.getHeight();
        System.out.println("width:" + width + " / height:" + height);
        pf.setPaper(paper);

        pjob.setJobName(f.getName());
        Book book = new Book();

        book.append(pages, pf, pdfFile.getNumPages());
        pjob.setPageable(book);

        // Sende Druckjob zum Default-Printer
        try {
            pjob.print();
        } catch (PrinterException e) {

            e.printStackTrace();
        }
        fis.close();
    }
}
