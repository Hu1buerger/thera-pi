package theraPi;

import javax.swing.JButton;

import mandant.Mandant;

class MandantButton extends JButton {
    private Mandant mandant;

    MandantButton(Mandant mandant) {
        this.mandant = mandant;
        this.setText(mandant.name() + " - IK" + mandant.ikDigits());

    }

    Mandant mandant() {
        return mandant;
    }
}
