package systemTools;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import benutzer.Benutzer;
import core.User;
import dialoge.RehaSmartDialog;
import events.RehaTPEvent;
import events.RehaTPEventClass;
import hauptFenster.Reha;
import hauptFenster.login.LoginPanel;

public class PassWort {
    ActionListener submitted = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            passWortCheck(loginPanel.passwort());

        }
    };

    LoginPanel loginPanel = new LoginPanel(submitted);

    private int falscherLogin = 0;

    public PassWort() {

    };

    private void passWortCheck(char[] password) {

        int size;
        boolean check = false;
        String name = "", rechte = "", pwToTest = String.valueOf(password);
        size = Benutzer.pKollegen.size();
        for (int i = 0; i < size; i++) {
            //// System.out.println(ParameterLaden.pKollegen.get(i).get(1));
            if (pwToTest.equals(Benutzer.pKollegen.get(i)
                                                        .get(1))) {
                name = Benutzer.pKollegen.get(i)
                                               .get(0);
                rechte = Benutzer.pKollegen.get(i)
                                                 .get(2);
                //// System.out.println("Rechte = "+rechte);
                Reha.progRechte = rechte;
                Reha.getThisFrame()
                    .setTitle(Reha.Titel + Reha.Titel2 + "  -->  [Benutzer: " + name + "]");
                Reha.aktUser = name;
                Reha.currentUser = new User(name,  Integer.valueOf( Benutzer.pKollegen.get(i).get(4)));
                check = true;
                break;
            }
        }
        reactOnPwCheck(check);
    }

    private void reactOnPwCheck(boolean check) {
        if (check) {
            // Korrekter Login
            loginPanel.setName(this.grundContainer()
                                   .getName());
            RehaTPEvent rEvt = new RehaTPEvent(this);
            rEvt.setRehaEvent("PinPanelEvent");
            rEvt.setDetails(loginPanel.getName(), "ROT");
            RehaTPEventClass.fireRehaTPEvent(rEvt);
            Reha.getThisFrame()
                .setVisible(true);
            Reha.getThisFrame()
                .validate();
            this.grundContainer()
                .Schliessen();

        } else {
            JOptionPane.showMessageDialog(null, "Benutzer mit diesem Passwort ist nicht vorhanden\n\nVersuch "
                    + Integer.toString(falscherLogin + 1) + " von 3");
            loginPanel.retry();
            falscherLogin = falscherLogin + 1;

            if (falscherLogin == 3) {
                // Hier Email an Admin
                falscherLogin = 0;
                System.exit(0);
            }
        }
    }

    private RehaSmartDialog grundContainer() {
        return (RehaSmartDialog) loginPanel.getParent()
                                           .getParent()
                                           .getParent()
                                           .getParent()
                                           .getParent();
    }

    public Container getPanel() {
        return loginPanel;
    }

}
