package dialoge;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import gui.Cursors;

public class DragWin extends MouseAdapter {
    private int clickX;
    private int clickY;
    private boolean insize;

    private int[] waagrecht = { 0, 0 };
    private int[] senkrecht = { 0, 0 };
    private int[] orgbounds = { 0, 0 };
    private int hilfsint = 0;
    private int sizeart;
    private Container owner;

    public DragWin(Container xowner) {
        this.owner = xowner;
    }

    private boolean WertZwischen(int punkt, int kleinerWert, int grosserWert) {
        if (punkt < kleinerWert) {
            return false;
        }
        if (punkt > grosserWert) {
            return false;
        }
        return true;
    }



    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        if (e.getY() <= 25) {
            clickY = e.getY();
            clickX = e.getX();
            hilfsint = owner.getWidth() / 2;
            waagrecht[0] = hilfsint - 15;
            waagrecht[1] = hilfsint + 15;
            hilfsint = owner.getHeight() / 2;
            senkrecht[0] = hilfsint - 15;
            senkrecht[1] = hilfsint + 15;
        }
    }

    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {
        // final java.awt.event.MouseEvent ex = e;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                owner.setCursor(Cursors.cdefault);
                clickX = -1;
                clickY = -1;
                orgbounds[0] = -1;
                orgbounds[1] = -1;
                insize = false;
                owner.setCursor(Cursors.cdefault);
                hilfsint = owner.getWidth() / 2;
                waagrecht[0] = hilfsint - 15;
                waagrecht[1] = hilfsint + 15;
                hilfsint = owner.getHeight() / 2;
                senkrecht[0] = hilfsint - 15;
                senkrecht[1] = hilfsint + 15;
            }
        });

    }

    @Override
    public void mouseDragged(java.awt.event.MouseEvent e) {
        // int center = (int) owner.getSize().getHeight();
        //// System.out.println("in Mousedragged");
        //// System.out.println("Insize = "+insize+" clickY="+clickY);
        //// System.out.println("Klick Y bei "+e.getY());
        // clickX = e.getX();
        if (!insize && clickY > 0) {
            // RehaSmartDialog.thisClass.getLocationOnScreen();
            //// System.out.println("in Mousedragged");
            final java.awt.event.MouseEvent ex = e;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    owner.setCursor(Cursors.cmove);
                    owner.setLocation(ex.getXOnScreen() - clickX, ex.getYOnScreen() - clickY);
                }
            });

        } else if (insize) {
            final java.awt.event.MouseEvent ex = e;
//			SwingUtilities.invokeLater(new Runnable(){
//				public  void run(){

            Dimension dim = owner.getSize();
            int oX = ex.getXOnScreen();
            int oY = ex.getYOnScreen();
            for (int i = 0; i < 1; i++) {
                if (sizeart == 1) { // nord-west
                    dim.width = (oX > orgbounds[0] ? dim.width - (oX - orgbounds[0]) : dim.width + (orgbounds[0] - oX));
                    dim.height = (oY > orgbounds[1] ? dim.height - (oY - orgbounds[1])
                            : dim.height + (orgbounds[1] - oY));
                    dim.width = (dim.width < 185 ? 185 : dim.width);
                    dim.height = (dim.height < 125 ? 125 : dim.height);
                    orgbounds[0] = oX;
                    orgbounds[1] = oY;
                    owner.setSize(dim);
                    owner.setLocation(ex.getXOnScreen(), ex.getYOnScreen());
                    owner.setCursor(Cursors.cnwsize);
                    break;
                }
                if (sizeart == 2) { // nord-ost
                    dim.width = (oX > orgbounds[0] ? dim.width + (oX - orgbounds[0]) : dim.width - (orgbounds[0] - oX));
                    dim.height = (oY > orgbounds[1] ? dim.height - (oY - orgbounds[1])
                            : dim.height + (orgbounds[1] - oY));
                    dim.width = (dim.width < 185 ? 185 : dim.width);
                    dim.height = (dim.height < 125 ? 125 : dim.height);
                    orgbounds[0] = oX;
                    orgbounds[1] = oY;
                    owner.setSize(dim);
                    owner.setLocation(ex.getXOnScreen() - dim.width, ex.getYOnScreen());
                    owner.setCursor(Cursors.cnesize);
                    break;
                }
                if (sizeart == 3) { // nord
                    dim.height = (oY > orgbounds[1] ? dim.height - (oY - orgbounds[1])
                            : dim.height + (orgbounds[1] - oY));
                    dim.width = (dim.width < 185 ? 185 : dim.width);
                    dim.height = (dim.height < 125 ? 125 : dim.height);
                    orgbounds[0] = oX;
                    orgbounds[1] = oY;
                    owner.setSize(dim);
                    owner.setLocation(ex.getXOnScreen() - ex.getX(), ex.getYOnScreen());
                    owner.setCursor(Cursors.cnsize);
                    break;
                }
                if (sizeart == 4) { // süd-west
                    dim.width = (oX > orgbounds[0] ? dim.width - (oX - orgbounds[0]) : dim.width + (orgbounds[0] - oX));
                    dim.height = (oY > orgbounds[1] ? dim.height + (oY - orgbounds[1])
                            : dim.height - (orgbounds[1] - oY));
                    dim.width = (dim.width < 185 ? 185 : dim.width);
                    dim.height = (dim.height < 125 ? 125 : dim.height);
                    orgbounds[0] = oX;
                    orgbounds[1] = oY;
                    owner.setSize(dim);
                    owner.setLocation(ex.getXOnScreen(), ex.getYOnScreen() - dim.height);
                    owner.setCursor(Cursors.cswsize);
                    break;
                }
                if (sizeart == 5) { // west
                    dim.width = (oX > orgbounds[0] ? dim.width - (oX - orgbounds[0]) : dim.width + (orgbounds[0] - oX));
                    dim.width = (dim.width < 185 ? 185 : dim.width);
                    dim.height = (dim.height < 125 ? 125 : dim.height);
                    orgbounds[0] = oX;
                    orgbounds[1] = oY;
                    owner.setSize(dim);
                    owner.setLocation(ex.getXOnScreen(), ex.getYOnScreen() - ex.getY());
                    owner.setCursor(Cursors.cwsize);
                    break;
                }
                if (sizeart == 6) { // süd-ost
                    dim.width = (oX > orgbounds[0] ? dim.width + (oX - orgbounds[0]) : dim.width - (orgbounds[0] - oX));
                    dim.height = (oY > orgbounds[1] ? dim.height + (oY - orgbounds[1])
                            : dim.height - (orgbounds[1] - oY));
                    dim.width = (dim.width < 185 ? 185 : dim.width);
                    dim.height = (dim.height < 125 ? 125 : dim.height);
                    orgbounds[0] = oX;
                    orgbounds[1] = oY;
                    owner.setSize(dim);
                    owner.setLocation(ex.getXOnScreen() - dim.width, ex.getYOnScreen() - dim.height);
                    owner.setCursor(Cursors.cwsize);
                    break;
                }
                if (sizeart == 7) { // s�d
                    dim.height = (oY > orgbounds[1] ? dim.height + (oY - orgbounds[1])
                            : dim.height - (orgbounds[1] - oY));
                    dim.width = (dim.width < 185 ? 185 : dim.width);
                    dim.height = (dim.height < 125 ? 125 : dim.height);
                    orgbounds[0] = oX;
                    orgbounds[1] = oY;
                    owner.setSize(dim);
                    owner.setLocation(ex.getXOnScreen() - ex.getX(), ex.getYOnScreen() - dim.height);
                    owner.setCursor(Cursors.cssize);
                    break;
                }
                if (sizeart == 8) { // ost
                    dim.width = (oX > orgbounds[0] ? dim.width + (oX - orgbounds[0]) : dim.width - (orgbounds[0] - oX));
                    dim.width = (dim.width < 185 ? 185 : dim.width);
                    dim.height = (dim.height < 125 ? 125 : dim.height);
                    orgbounds[0] = oX;
                    orgbounds[1] = oY;
                    owner.setSize(dim);
                    owner.setLocation(ex.getXOnScreen() - ex.getX(), ex.getYOnScreen() - ex.getY());
                    owner.setCursor(Cursors.cesize);
                    break;
                }

                insize = false;
                owner.setCursor(Cursors.cdefault);
            }
            // }
            // });

        } else {

            insize = false;
            owner.setCursor(Cursors.cdefault);
        }
    }

    @Override
    public void mouseMoved(java.awt.event.MouseEvent e) {
        for (int i = 0; i < 1; i++) {
            sizeart = -1;
            owner.setCursor(Cursors.cdefault);
            if ((e.getX() <= 4 && e.getY() <= 4)) { // nord-west
                insize = true;
                sizeart = 1;
                orgbounds[0] = e.getXOnScreen();
                orgbounds[1] = e.getYOnScreen();
                owner.setCursor(Cursors.cnwsize);
                break;
            }
            if ((e.getX() >= (((JComponent) e.getSource()).getWidth() - 4)) && e.getY() <= 4) {// nord-ost
                insize = true;
                sizeart = 2;
                orgbounds[0] = e.getXOnScreen();
                orgbounds[1] = e.getYOnScreen();
                owner.setCursor(Cursors.cnesize);
                break;
            }
            if (e.getY() <= 6 && WertZwischen(e.getX(), waagrecht[0], waagrecht[1])) {// nord
                insize = true;
                sizeart = 3;
                orgbounds[0] = e.getXOnScreen();
                orgbounds[1] = e.getYOnScreen();
                owner.setCursor(Cursors.cnsize);
                break;
            }
            if ((e.getX() <= 4 && e.getY() >= (((JComponent) e.getSource()).getHeight() - 4))) { // süd-west
                insize = true;
                sizeart = 4;
                orgbounds[0] = e.getXOnScreen();
                orgbounds[1] = e.getYOnScreen();
                owner.setCursor(Cursors.cswsize);
                break;
            }
            if ((e.getX() <= 6) && WertZwischen(e.getY(), senkrecht[0], senkrecht[1])) { // west
                insize = true;
                sizeart = 5;
                orgbounds[0] = e.getXOnScreen();
                orgbounds[1] = e.getYOnScreen();
                owner.setCursor(Cursors.cwsize);
                break;
            }
            if ((e.getX() >= (((JComponent) e.getSource()).getWidth() - 4)) && // süd-ost
                    e.getY() >= (((JComponent) e.getSource()).getHeight() - 4)) {
                insize = true;
                sizeart = 6;
                orgbounds[0] = e.getXOnScreen();
                orgbounds[1] = e.getYOnScreen();
                owner.setCursor(Cursors.csesize);
                break;
            }
            if (e.getY() >= (((JComponent) e.getSource()).getHeight() - 4)
                    && WertZwischen(e.getX(), waagrecht[0], waagrecht[1])) { // süd
                insize = true;
                sizeart = 7;
                orgbounds[0] = e.getXOnScreen();
                orgbounds[1] = e.getYOnScreen();
                owner.setCursor(Cursors.cssize);
                break;
            }
            if (e.getX() >= (((JComponent) e.getSource()).getWidth() - 6)
                    && WertZwischen(e.getY(), senkrecht[0], senkrecht[1])) { // ost
                insize = true;
                sizeart = 8;
                orgbounds[0] = e.getXOnScreen();
                orgbounds[1] = e.getYOnScreen();
                owner.setCursor(Cursors.cesize);
                break;
            }

            insize = false;
            sizeart = -1;
            owner.setCursor(Cursors.cdefault);

        }
    }

}
