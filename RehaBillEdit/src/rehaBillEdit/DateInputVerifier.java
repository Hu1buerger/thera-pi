package rehaBillEdit;

import java.awt.Color;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

import rehaBillEdit.Tools.IntegerTools;

class DateInputVerifier extends InputVerifier {
    public DateInputVerifier(JFormattedTextField tf) {
    }

    @Override
    public boolean verify(final JComponent input) {
        //// System.out.println("Input getText = "+((JRtaTextField)input).getText());
        //// System.out.println("L�nge des Inputs =
        //// "+((JRtaTextField)input).getText().length());
        return isAlowedDate((JFormattedTextField) input);
    }

    private Character placeholder;

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

    private boolean isAlowedDate(final JFormattedTextField input) {
        if ("  .  .    ".equals(input.getText())) {
            return true;
        }
        String inhalt = input.getText();
        //// System.out.println("In verify / input = "+input.getText());
        final DateFormat sdf = getDateFormat();
        try {
            String teil = inhalt.substring(6)
                                .trim();
            if (teil.isEmpty()) {
                input.setText("  .  .    ");
                return true;
            }
            if (teil.length() == 2) {
                String jahrtausend = "";
                if (IntegerTools.trailNullAndRetInt(teil) > 20) {
                    jahrtausend = inhalt.substring(0, 6)
                                        .trim()
                            + "19" + teil;
                } else {
                    jahrtausend = inhalt.substring(0, 6)
                                        .trim()
                            + "20" + teil;
                }
                input.setText(jahrtausend);
                //// System.out.println("Datum = "+jahrtausend);
            }
            if (inhalt.length() >= 8 && "0".equals(inhalt.substring(6, 7))) {
               String korrekt = inhalt.substring(0, 6);
               korrekt = korrekt + "20" + inhalt.substring(6, 8);
               input.setText(korrekt);
               //// System.out.println("korrigiertes Datum = "+korrekt);
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
            /*
             * if (((DateInputVerifier) input).getPlaceHolder() != null) { String
             * noMaskValue = null; if (Locale.getDefault ().getLanguage ().equals
             * (Locale.GERMANY.getLanguage ())) {
             * ////System.out.println("InputVerifier - Locale = Germany"); noMaskValue =
             * input.getText().replace ('.',((JFormattedField) input).getPlaceHolder ()); }
             * else { noMaskValue = input.getText().replace ('-',((JRtaTextField)
             * input).getPlaceHolder ());
             * ////System.out.println("InputVerifier - Locale = English"); } for (char c :
             * noMaskValue.toCharArray()) { if (c != ((JRtaTextField)
             * input).getPlaceHolder()) return false; } return true; }
             */
            // System.out.println("Unzul�ssige Datumseingabe");
            JOptionPane.showMessageDialog(null, "Unzulässige Datumseingabe");

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
            // input.setForeground(Color.BLACK);
            input.setBorder((Border) UIManager.getLookAndFeelDefaults()
                                              .get("TextField.border"));
            return true;
        }
    }
}
