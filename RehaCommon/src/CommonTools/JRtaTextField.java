package CommonTools;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MaskFormatter;

public class JRtaTextField extends JFormattedTextField implements PropertyChangeListener, FocusListener, KeyListener {
    private String type = "";
    private boolean selectWhenFocus;
    private NumberFormat gleitDisplayFormat;
    private NumberFormat gleitEditFormat;
    /**
     *
     */
    private static final long serialVersionUID = 6776374376473011458L;

    public JRtaTextField(String type, boolean selectWhenFocus) {

        super();
        setRtaType(type, this, selectWhenFocus);

        this.addFocusListener(this);
        this.addKeyListener(this);
        setDisabledTextColor(Color.RED);
    }



    /*****************/
    static String toRtaUpper(String s) {
        StringBuffer sbuf = new StringBuffer();
        char[] cha = s.toCharArray();
        int i, len = s.length();

        for (i = 0; i < len; i++) {
            char ch = cha[i];
            if (ch == '\u00DF') { // sharp s
                sbuf.append("ß");
                continue;
            }
            sbuf.append(Character.toUpperCase(ch));
        }
        return sbuf.toString();
    }

    /*****************/
    public JRtaTextField(String type, boolean selectWhenFocus, String gleitkomma, String xalign) {
        super();

        new JFormattedTextField(createFormatter("#########0.00"));
        if (xalign.equals("RECHTS")) {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
        this.type = type;
        this.selectWhenFocus = selectWhenFocus;
        this.addFocusListener(this);
        this.addKeyListener(this);
        this.addPropertyChangeListener("value", this);
        setDisabledTextColor(Color.RED);
    }

    public double getDValueFromS() {
        String test;
        test = getText();
        if (test.equals("")) {
            return Double.parseDouble("0.00");
        }
        if (test.indexOf(".") < 0 && test.indexOf(",") < 0) {
            return (Double.parseDouble(test.trim() + ".00"));

        }
        double fielddbl = Double.parseDouble(test.replaceAll(",", "."));
        return fielddbl;
    }

    public void setDValueFromS(String value) {
        String wert = value.replaceAll(",", ".");

        DecimalFormat df = new DecimalFormat("#########0.00");
        setText((wert.trim()
                     .equals("") ? df.format(Double.parseDouble("0.00")) : df.format(Double.parseDouble(wert))));
        return;
    }

    public void listenerLoeschen() {
        this.removeFocusListener(this);
        this.removeKeyListener(this);
        this.removePropertyChangeListener(this);
    }

    private MaskFormatter createFormatter(String s) {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter(s);

        } catch (java.text.ParseException exc) {
            System.err.println("formatter is bad: " + exc.getMessage());
            System.exit(-1);
        }
        return formatter;
    }

    /*
     * public JRtaTextField JRtaTextField(String muster,String type,Container con){
     *
     *
     * JFormattedTextField comp = new JFormattedTextField(); return this; //return
     * comp; }
     */
    private void setRtaType(String type, JRtaTextField feld, boolean selectWhenFocus) {
        this.type = type;
        this.selectWhenFocus = selectWhenFocus;

        for (int i = 0; i < 1; i++) {
            if (type.equals("GROSS")) {
                this.setDocument(new NurGrossDocument(this));
                break;
            }
            if (type.equals("KLEIN")) {
                this.setDocument(new NurKleinDocument(this));
                break;
            }
            if (type.equals("NORMAL")) {
                // this.setDocument(new NurNormalDocument(this));
                break;
            }
            if (type.equals("ZAHLEN")) {
                this.setDocument(new NurZahlenDocument(this));
                break;
            }
            if (type.equals("STUNDEN")) {
                this.setDocument(new NurStundenDocument(this));
                break;
            }
            if (type.equals("MINUTEN")) {
                this.setDocument(new NurMinutenDocument(this));
                break;
            }
            if (type.equals("DATUM")) {
                this.setDocument(new DateFieldDocument(this, false));
                this.setInputVerifier(new DateInputVerifier());
                break;
            }
            if (type.equals("D")) {
                this.setDocument(new NurZahlenDocument(this));
                break;
            }
            if (type.equals("FL")) { // Fließkomma
                this.setDocument(new NurZahlenDocument(this));
                break;
            }
        }

    }



    public void setupFormat(int digits) {
        gleitDisplayFormat = NumberFormat.getInstance(Locale.GERMAN);
        gleitDisplayFormat.setMinimumFractionDigits(digits);
        gleitDisplayFormat.setMaximumFractionDigits(digits);
        ((DecimalFormat) gleitDisplayFormat).setDecimalSeparatorAlwaysShown(true);
        gleitEditFormat = NumberFormat.getInstance(Locale.GERMAN);
        gleitEditFormat.setMinimumFractionDigits(digits);
        gleitEditFormat.setMaximumFractionDigits(digits);
        ((DecimalFormat) gleitEditFormat).setDecimalSeparatorAlwaysShown(true);
    }

    public String getRtaType() {
        return this.type;
    }

    public boolean getSelectOnFocus() {
        return this.selectWhenFocus;
    }

    private void focusNachUnten() {
        this.transferFocus();
        return;
    }

    private void focusNachOben() {
        this.transferFocusBackward();
        return;
    }

    private void veroeffentlicheEvent(KeyEvent event) {
        try {
            this.getParent()
                .dispatchEvent(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            if (this.getParent()
                    .getParent() != null)
                this.getParent()
                    .getParent()
                    .dispatchEvent(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            if (this.getParent()
                    .getParent()
                    .getParent() != null)
                this.getParent()
                    .getParent()
                    .getParent()
                    .dispatchEvent(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            if (this.getParent()
                    .getParent()
                    .getParent()
                    .getParent() != null)
                this.getParent()
                    .getParent()
                    .getParent()
                    .getParent()
                    .dispatchEvent(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    /*************************************************/

    private static class DateInputVerifier extends InputVerifier {
        @Override
        public boolean verify(final JComponent input) {
            if (input instanceof JRtaTextField) {
                return this.isAlowedDate((JRtaTextField) input);
            } else {
                return true;
            }
        }

        private boolean isAlowedDate(final JRtaTextField input) {
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
                if (input.getPlaceHolder() != null) {
                    String noMaskValue = null;
                    if (Locale.getDefault()
                              .getLanguage()
                              .equals(Locale.GERMANY.getLanguage())) {
                        noMaskValue = input.getText()
                                           .replace('.', input.getPlaceHolder());
                    } else {
                        noMaskValue = input.getText()
                                           .replace('-', input.getPlaceHolder());
                    }
                    for (char c : noMaskValue.toCharArray()) {
                        if (c != input.getPlaceHolder())
                            return false;
                    }
                    return true;
                }

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

    @Override
    public void propertyChange(PropertyChangeEvent arg0) {

        if (((JRtaTextField) arg0.getSource()).type.equals("FL")) {
            String zahl = ((JRtaTextField) arg0.getSource()).getText();
            if (zahl.equals("")) {
                zahl = "0,00";
            }
            zahl = zahl.replaceAll(",", "\\.");
            DecimalFormat df = new DecimalFormat("#########0.00");
            ((JRtaTextField) arg0.getSource()).setText(df.format(new Double(zahl)));
        }

    }

    @Override
    public void focusLost(FocusEvent e) {
        if (((JRtaTextField) e.getComponent()).isEditable()) {
            e.getComponent()
             .setBackground(Color.WHITE);
            if ((type == "STUNDEN" || type == "MINUTEN") && (getText().length() < 2)) {
                if (getText().length() == 1) {
                    setText((type == "MINUTEN" ? getText() + "0" : "0" + getText()));
                }
                if (getText().length() == 0) {
                    setText("00");
                }
            }
            if ((type == "NORMAL")) {
                if (getText().length() > 0) {
                    setText(StringTools.EGross(getText().trim()));
                }
            }
        }
        try {
            e.getComponent()
             .getParent()
             .dispatchEvent(e);
        } catch (java.lang.NullPointerException ex) {

            // ignore
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (((JRtaTextField) e.getComponent()).isEditable()) {
            e.getComponent()
             .setBackground(Color.YELLOW);
        }

        final FocusEvent xe = e;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                if (getSelectOnFocus()) {
                    ((JRtaTextField) xe.getComponent()).select(0, ((JRtaTextField) xe.getComponent()).getText()
                                                                                                     .length());
                } else {
                    ((JRtaTextField) xe.getComponent()).setCaretPosition(0);
                }

            }
        });
        e.getComponent()
         .getParent()
         .dispatchEvent(e);
    }

    @Override
    public void keyPressed(KeyEvent event) {
        // look for tab keys
        int code = event.getKeyCode();
        // SucheFenster in den Vordergrund stellen
        if (event.isControlDown() && code == 83) {
            veroeffentlicheEvent(event);
            event.consume();
        }
        if (code == 27) {
            veroeffentlicheEvent(event);
        }
        if (code == KeyEvent.VK_TAB || code == KeyEvent.VK_ENTER || code == 40) {
            // if no valid data entered in field, consume event
            // so that it won't be passed on to focus manager
            event.consume();
            focusNachUnten();
        }
        if (code == 38) {
            focusNachOben();
        }
        if (code == 27) {
            // ((Component) event.getSource()).getParent().dispatchEvent(event);
        }
        if (((JRtaTextField) event.getSource()).type.equals("DATUM")) {

            if (code == 127) {
                ((JRtaTextField) event.getSource()).setText("  .  .    ");
                ((JRtaTextField) event.getSource()).setCaretPosition(0);
            }
            if (code == 8) {
                ((JRtaTextField) event.getSource()).setText("  .  .    ");
                ((JRtaTextField) event.getSource()).setCaretPosition(0);
            }

        }

    }

    @Override
    public void keyReleased(KeyEvent arg0) {

    }

    @Override
    public void keyTyped(KeyEvent arg0) {

    }

    /****************** Klassen-Ende ********************/
}

/*************************************************/
class NurZahlenDocument extends javax.swing.text.PlainDocument {
    /**
    	 *
    	 */
    private static final long serialVersionUID = -29699654036665632L;
    private JTextField textField;
    private String text;

    public NurZahlenDocument(JFormattedTextField tf) {
        textField = tf;
    }

    @Override
    public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
            throws javax.swing.text.BadLocationException {
        try {
            super.insertString(offs, str, a);
            text = textField.getText();
            if ((text.length() == 1) & (text.equals("-")))
                return;
            Long.parseLong(text);
        } catch (NumberFormatException e) {
            super.remove(offs, 1);
        }
    }
}



/*************************************************/

class NurGrossDocument extends javax.swing.text.PlainDocument {
    /**
    	 *
    	 */
    private static final long serialVersionUID = -1708532746033381872L;


    public NurGrossDocument(JFormattedTextField tf) {

    }

    @Override
    public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
            throws javax.swing.text.BadLocationException {
        try {
            super.insertString(offs, JRtaTextField.toRtaUpper(str), a);

            return;
        } catch (NumberFormatException e) {
            super.remove(offs, 1);
        }

    }
}

/*************************************************/

class NurKleinDocument extends javax.swing.text.PlainDocument {
    /**
    	 *
    	 */
    private static final long serialVersionUID = -1708532746033381872L;


    public NurKleinDocument(JFormattedTextField tf) {
    }

    @Override
    public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
            throws javax.swing.text.BadLocationException {
        try {
            super.insertString(offs, str.toLowerCase(), a);
            return;
        } catch (NumberFormatException e) {
            super.remove(offs, 1);
        }

    }
}

/*************************************************/

class NurStundenDocument extends javax.swing.text.PlainDocument {
    /**
    	 *
    	 */
    private static final long serialVersionUID = -29699654036665632L;
    private JTextField textField;
    private String text;

    public NurStundenDocument(JFormattedTextField tf) {
        textField = tf;
    }

    @Override
    public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
            throws javax.swing.text.BadLocationException {
        try {
            super.insertString(offs, str, a);
            text = textField.getText();
            if (Integer.valueOf(text) > 24) {
                super.remove(offs, 1);
                return;
            }
            if ((text.length() == 1) & (text.equals("-")))
                return;
            Integer.parseInt(text);
        } catch (NumberFormatException e) {
            super.remove(offs, 1);
            // Toolkit.getDefaultToolkit().beep();
        }
    }
}

/*************************************************/
class NurMinutenDocument extends javax.swing.text.PlainDocument {
    /**
    	 *
    	 */
    private static final long serialVersionUID = -29699654036665632L;
    private JTextField textField;
    private String text;

    public NurMinutenDocument(JFormattedTextField tf) {
        textField = tf;
    }

    @Override
    public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
            throws javax.swing.text.BadLocationException {
        try {
            super.insertString(offs, str, a);
            text = textField.getText();
            if (Integer.valueOf(text) > 59) {
                super.remove(offs, 1);
                return;
            }
            if ((text.length() == 1) & (text.equals("-")))
                return;
            Integer.parseInt(text);
        } catch (NumberFormatException e) {
            super.remove(offs, 1);
        }
    }
}

/*************************************************/

/*****************************************************************/
class DateFieldDocument extends javax.swing.text.PlainDocument {
    // **** Attribute
    private static final String DREI = "0123";// Erlaubte Ziffern Tag 10er
    private static final String MONAT = "01"; // Erlaubte Zeichen Monat 10er
    private Calendar initDate = new GregorianCalendar(); // Calender fuers init
    private String initString; // Voreingestellter String
    private static int trenner1 = 2, trenner2 = 5; // Position vor dem Trenner
    private JFormattedTextField textComponent; // Für Referenz auf das TextFeld
    private int newOffset; // Caret Position bei Trennern
    private boolean init = false;
    private SimpleDateFormat datumsFormat = new SimpleDateFormat("dd.MM.yyyy"); // Konv.
    // **** Attribute Ende

    // **** Konstruktor 1
    public DateFieldDocument(JFormattedTextField textComponent, boolean datumHeute) {
        this.textComponent = textComponent; // Hiermit wird jetzt gearbeitet
        initDate.setTime(new Date()); // Kalender auf heute
        initString = datumsFormat.format(initDate.getTime()); // Nach String

        try { // Jetzt den Inhalt mit dem Datum
            if (datumHeute) {
                insertString(0, initString, null); // initialisieren
                this.init = true;
            } else {
                insertString(0, "  .  .    ", null); // initialisieren
                this.init = true;
            }
        } catch (Exception KonstrEx) {
            KonstrEx.printStackTrace();
        }
    }



    // **** Überschreiben Insert-Methode
    @Override
    public void insertString(int offset, String zeichen, AttributeSet attributeSet) throws BadLocationException {
        if (zeichen.equals(initString) || zeichen.equals("  .  .    ")) { // Wenn initString oder leeres Datum, gleich
                                                                          // rein
            if (zeichen.equals("  .  .    ")) {
                if (!this.init) {
                    super.insertString(0, "  .  .    ", attributeSet);
                } else {
                    super.remove(0, 10);
                    super.insertString(0, "  .  .    ", attributeSet);
                }
            } else {
                if (!this.init) {
                    super.insertString(0, zeichen, attributeSet);
                } else {
                    super.remove(0, 10);
                    super.insertString(0, zeichen, attributeSet);
                }
                // super.insertString(offset, zeichen, attributeSet);
            }

        } else if (zeichen.length() == 10) { // Wenn komplettes Datum, und
            if (zeichen.equals("  .  .    ")) { // richtig, dann rein
                super.remove(0, 10);
                super.insertString(0, zeichen, attributeSet);
            } else {
                super.remove(0, 10);
                super.insertString(0, zeichen, attributeSet);
            }
        } else if (zeichen.length() == 1) { // Wenn nicht, nur Einzelzeichen
            try { // annehmen
                Integer.parseInt(zeichen);
            } catch (Exception NumEx) { // Kein Integer?
                return; // Keine Verarbeitung!
            }
            if (offset == 0) { // Tage auf 10 20 30 prüfen
                if (DREI.indexOf(String.valueOf(zeichen.charAt(0))) == -1) {
                    // Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
            if (offset == 1) { // Tage 32-39 unterbinden
                if (textComponent.getText()
                                 .substring(0, 1)
                                 .equals("3")) {
                    int tag = Integer.valueOf(zeichen)
                                     .intValue();
                    if (tag > 1) {
                        // Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                }
            }
            if (offset == 1) { // Tag 00 unterbinden
                if (textComponent.getText()
                                 .substring(0, 1)
                                 .equals("0")) {
                    int tag = Integer.valueOf(zeichen)
                                     .intValue();
                    if (tag == 0) {
                        // Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                }
            }
            if (offset == 2) { // Monate auf 0x-1x prüfen
                               // (Caret links vom Trenner)
                if (MONAT.indexOf(String.valueOf(zeichen.charAt(0))) == -1) {
                    // Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
            if (offset == 3) { // Monate auf 0x-1x prüfen
                               // (Caret rechts vom Trenner)
                if (MONAT.indexOf(String.valueOf(zeichen.charAt(0))) == -1) {
                    // Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
            if (offset == 4) { // Monate 13-19 unterbinden
                if (textComponent.getText()
                                 .substring(3, 4)
                                 .equals("1")) {
                    int monat = Integer.valueOf(zeichen)
                                       .intValue();
                    if (monat > 2) {
                        // Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                }
            }
            if (offset == 4) { // Monat 00 unterbinden
                if (textComponent.getText()
                                 .substring(3, 4)
                                 .equals("0")) {
                    int monat = Integer.valueOf(zeichen)
                                       .intValue();
                    if (monat == 0) {
                        // Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                }
            }

            newOffset = offset;
            if (atSeparator(offset)) { // Wenn am trenner, dann den offset
                newOffset++; // vor dem einfügen um 1 verschieben
                textComponent.setCaretPosition(newOffset);
            }
            super.remove(newOffset, 1); // Aktuelles zeichen entfernen
            super.insertString(newOffset, zeichen, attributeSet); // Neues einfügen
        }
    }
    // **** Überschreiben Insert Ende

    // **** Überschreiben Remove
    @Override
    public void remove(int offset, int length) throws BadLocationException {
        if (atSeparator(offset))
            textComponent.setCaretPosition(offset - 1);
        else
            textComponent.setCaretPosition(offset);
    }
    // **** Überschreiben Remove Ende

    // **** Hilfsmethode für die Punkte zwischen den Feldern
    private boolean atSeparator(int offset) {
        return offset == trenner1 || offset == trenner2;
    }
    // **** Hilfsmethode Ende
}
