package rehaInternalFrame;

import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyVetoException;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleValue;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.event.InternalFrameEvent;

import CommonTools.RehaEvent;
import CommonTools.RehaEventClass;
import CommonTools.RehaEventListener;
import hauptFenster.AktiveFenster;
import hauptFenster.FrameSave;
import hauptFenster.Reha;

public class JTerminInternal extends JRehaInternal implements RehaEventListener {
    /**
     *
     */
    public static boolean inIniSave = false;
    private static final long serialVersionUID = -3063788551323647566L;
    RehaEventClass rEvent = null;

    public JTerminInternal(String titel, ImageIcon img, int desktop) {
        super(titel, img, desktop);
        rEvent = new RehaEventClass();
        rEvent.addRehaEventListener(this);
        addInternalFrameListener(this);
    }

    /*
     * public void internalFrameActivated(InternalFrameEvent arg0) { isActive =
     * true; TerminFenster.thisClass.getViewPanel().requestFocus(); toFront();
     * super.repaint(); frameAktivieren(super.getName()); }
     */
    @Override
    public void internalFrameClosed(InternalFrameEvent arg0) {
        if (!this.isIcon && !inIniSave) {
            inIniSave = true;
            new FrameSave((Dimension) this.getSize()
                                          .clone(),
                    (Point) this.getLocation()
                                .clone(),
                    Integer.valueOf(this.desktop), Integer.valueOf((this.getImmerGross() ? 1 : 0)),
                    String.valueOf("kalender.ini"), String.valueOf("Kalender"));
        }

        Reha.instance.desktops[this.desktop].remove(this);
        this.removeInternalFrameListener(this);
        Reha.getThisFrame()
            .requestFocus();
        Reha.instance.aktiviereNaechsten(this.desktop);
        this.removeAll();
        //// System.out.println("Lösche Termin Internal von Desktop-Pane =
        //// "+Reha.instance.desktops[this.desktop]);
        //// System.out.println("Termin-Internal geschlossen***************");

        rEvent.removeRehaEventListener(this);
        if (Reha.instance.terminpanel != null) {
            try {
                Reha.instance.terminpanel.db_Aktualisieren.interrupt();
            } catch (Exception ex) {

            }
        }
        if (Reha.instance.terminpanel != null) {
            Reha.instance.terminpanel = null;
        }

        Reha.instance.terminpanel = null;
        this.nord = null;
        this.inhalt = null;
        this.thisContent = null;
        this.dispose();
        super.dispose();
        AktiveFenster.loescheFenster(this.getName());
        Reha.instance.progLoader.loescheTermine();
        /*
         * SwingUtilities.invokeLater(new Runnable(){ public void run() { Runtime r =
         * Runtime.getRuntime(); r.gc(); long freeMem = r.freeMemory();
         * ////System.out.println("Freier Speicher nach  gc():    " + freeMem); } });
         */
    }

    @Override
    public void rehaEventOccurred(RehaEvent evt) {
        if (evt.getRehaEvent()
               .equals("REHAINTERNAL")) {
            //// System.out.println("es ist ein Reha-Internal-Event");
        }
        if (evt.getDetails()[0].equals(this.getName())) {
            if (evt.getDetails()[1].equals("#ICONIFIED")) {
                try {
                    this.setIcon(true);
                } catch (PropertyVetoException e) {

                    e.printStackTrace();
                }
                this.setActive(false);
            }
        }
    }
}

final class JDesktopIcon extends JComponent implements Accessible {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * DOCUMENT ME!
     */
    protected class AccessibleJDesktopIcon extends AccessibleJComponent implements AccessibleValue {
        private static final long serialVersionUID = 5035560458941637802L;

        /**
         * Creates a new AccessibleJDesktopIcon object.
         */
        protected AccessibleJDesktopIcon() {
            super();
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        @Override
        public AccessibleRole getAccessibleRole() {
            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        @Override
        public AccessibleValue getAccessibleValue() {
            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        @Override
        public Number getCurrentAccessibleValue() {
            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        @Override
        public Number getMaximumAccessibleValue() {
            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        @Override
        public Number getMinimumAccessibleValue() {
            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @param n DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        @Override
        public boolean setCurrentAccessibleValue(Number n) {
            return false;
        }
    }
}
