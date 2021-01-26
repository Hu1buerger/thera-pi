package therapi.updatehint.swing;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.thera_pi.updater.Version;

import therapi.updatehint.Hint;
import therapi.updatehint.HintGui;
import therapi.updatehint.HyperLInkEventHandler;

public class UpdatesMainSwing implements HintGui {

    @Override
    public void show(Hint hint) {
        String body = "<center>F\u00fcr ihre Version<br />" + new Version().number()
                + ".<br />Laden Sie bitte die Datei<br />" + hint.fileToDownload()
                                                                 .getName()
                + "<br />von dieser Webseite<br />" + "<a href=" + HyperLInkEventHandler.UPDATES_PAGE + ">"
                + HyperLInkEventHandler.UPDATES_PAGE + " </a>  <br />herunter<br /></center>"

        ;

        JEditorPane ep = new JEditorPane("text/html", "<hml><body>" + body + "</body></html>");
        ep.setEditable(false);
        ep.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType()
                     .equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    new HyperLInkEventHandler().handle(null);
                }
            }

        });
        final JDialog dialog = new JDialog();
        JOptionPane.showMessageDialog(dialog, ep, "Es gibt ein Update", JOptionPane.INFORMATION_MESSAGE);

        dialog.dispose();

    }

}
