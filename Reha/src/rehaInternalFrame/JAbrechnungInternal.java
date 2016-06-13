package rehaInternalFrame;

import java.awt.event.FocusListener;
import java.beans.PropertyVetoException;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;

import abrechnung.AbrechnungRezept;
import CommonTools.RehaEvent;
import CommonTools.RehaEventClass;
import CommonTools.RehaEventListener;
import hauptFenster.AktiveFenster;
import hauptFenster.Reha;

public class JAbrechnungInternal extends JRehaInternal implements FocusListener, RehaEventListener{
		/**
	 * 
	 */
	private AbrechnungRezept abrRez = null;
	
	private static final long serialVersionUID = -4989326440978535166L;
		RehaEventClass rEvent = null;
		public JAbrechnungInternal(String titel, ImageIcon img, int desktop) {
			super(titel, img, desktop);
			this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
			rEvent = new RehaEventClass();
			rEvent.addRehaEventListener(this);
		}
		@Override
		public void internalFrameClosed(InternalFrameEvent arg0) {
			//System.out.println("JInternalFrame-Kassenabrechnung aufräumen");
			Reha.instance.aktiviereNaechsten(this.desktop);
			Reha.instance.desktops[this.desktop].remove(this);
			rEvent.removeRehaEventListener(this);
			removeFocusListener(this);
			this.removeAncestorListener(this);
			final String name = this.getName();
			abrRez.cleanUp();

			SwingUtilities.invokeLater(new Runnable(){
			 	   @Override
                public  void run()
			 	   {
					AktiveFenster.loescheFenster(name);
					Reha.instance.progLoader.loescheAbrechnung();
			 	   }
			});
			//AktiveFenster.loescheFenster("Abrechnung");
			rEvent = null;


		}
		@Override
		public void rehaEventOccurred(RehaEvent evt) {
			if(evt.getRehaEvent().equals("REHAINTERNAL")){
				////System.out.println("es ist ein Reha-Internal-Event");
			}
			if(evt.getDetails()[0].equals(this.getName())){
				if(evt.getDetails()[1].equals("#ICONIFIED")){
					try {
						this.setIcon(true);
					} catch (PropertyVetoException e) {
						e.printStackTrace();
					}
					this.setActive(false);
				}
			}
		}
		public void setAbrRezInstance(AbrechnungRezept xAbrRez) {
			abrRez = xAbrRez;
		}
		

}
