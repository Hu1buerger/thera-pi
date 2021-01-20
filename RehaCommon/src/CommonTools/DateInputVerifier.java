package CommonTools;

import java.awt.Color;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

class DateInputVerifier extends InputVerifier {


    DateInputVerifier(JFormattedTextField tf) {

    }

    @Override
    public boolean verify(final JComponent input) {
        return this.isAlowedDate((JFormattedTextField) input);
    }

    private Character placeholder = null;

    /**
     * Set an Empty Character for delete the Input. If Empty Character is null, a
     * valid value need to input.
     *
     * @param c Character
     */
    public void setPlaceholder(final Character c) {
        this.placeholder = c;
    }

    /**
     * Return the char for delete the input or null if delete not allowed.
     *
     * @return Character
     */
    public Character getPlaceHolder() {
        return this.placeholder;
    }

    protected boolean isAlowedDate(final JFormattedTextField input) {
        String inhalt = input.getText();
        if (inhalt.equals("  .  .    ")) {
            return true;
        }

        Calendar initDate = new GregorianCalendar();
        initDate.setTime(new Date()); // Kalender auf heute
        SimpleDateFormat datumsFormat = new SimpleDateFormat("dd.MM.yyyy");
        String currDate = datumsFormat.format(initDate.getTime());
        int jahrhundert = Integer.parseInt(currDate.substring(6,8));
        int currJahr2stellig = Integer.parseInt(currDate.substring(8));

        final DateFormat sdf = this.getDateFormat();
        try {
            String teil = inhalt.substring(6)
                                .trim();
            if (teil.length() == 0) {
                input.setText("  .  .    ");
                return true;
            }
            if (teil.length() == 2) {
                String datumFinal = inhalt.substring(0, 6)
                                          .trim();
                if (IntegerTools.trailNullAndRetInt(teil) > currJahr2stellig) {
                    jahrhundert--;
                }
                datumFinal = datumFinal + jahrhundert + teil;
                input.setText(datumFinal);
            }
            if (inhalt.length() >= 8) {
                if (inhalt.substring(6, 7)
                          .equals("0")) {
                    String korrekt = inhalt.substring(0, 6);
                    korrekt = korrekt + jahrhundert + inhalt.substring(6, 8);
                    input.setText(korrekt);
                }
            }
            final Date d = sdf.parse(input.getText());
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    input.setText(sdf.format(d));
                }
            });
            return true;
        } catch (final ParseException notValidOrDelete) {
            JOptionPane.showMessageDialog(null, "Unzul�ssige Datumseingabe");

            return false;
        }
    }

    private DateFormat getDateFormat() {
        if (Locale.getDefault()
                  .getLanguage()
                  .equals(Locale.GERMANY.getLanguage())) {
            return new SimpleDateFormat("dd.MM.yyyy");
        } else {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    }

    @Override
    public boolean shouldYieldFocus(final JComponent input) {
        if (!verify(input)) {
            input.setForeground(Color.RED);
            input.setBorder(BorderFactory.createEtchedBorder(Color.RED, new Color(255, 50, 50)));
            return false;
        } else {
            input.setBorder((Border) UIManager.getLookAndFeelDefaults()
                                              .get("TextField.border"));
            return true;
        }
    }
}