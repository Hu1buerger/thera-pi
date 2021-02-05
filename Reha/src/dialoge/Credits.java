package dialoge;

import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.therapi.updater.Version;


class Credits extends JPanel {
    private Logger logger = LoggerFactory.getLogger(Credits.class);

    public Credits() {
        setName("credits");
        setLayout(new GridLayout(1,1,0,0));

        JTextPane creditstext = new JTextPane();
        creditstext.setEditable(false);
        StyledDocument document = creditstext.getStyledDocument();
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setAlignment(sas, StyleConstants.ALIGN_CENTER);
        SimpleAttributeSet bold = new SimpleAttributeSet();
        StyleConstants.setAlignment(bold, StyleConstants.ALIGN_CENTER);
        StyleConstants.setBold(bold, true);
        StyleConstants.setFontSize(bold, 30);

        try {

            document.insertString(document.getLength(),
                    "Thera-\u03C0 v" + new Version().number() + " \nvom\n " +new Version().getReleaseDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    bold);
            document.insertString(document.getLength(), "\nnach einer Idee von J\u00fcrgen Steinhilber", sas);
            document.setParagraphAttributes(0, document.getLength(), sas, false);
        } catch (BadLocationException e) {
            logger.error("Fehler beim einfügen der Credits.",e);
        }
        add(creditstext, "cell 0 0,alignx center,aligny center");
    }

}
