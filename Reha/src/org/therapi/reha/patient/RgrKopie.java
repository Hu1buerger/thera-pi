package org.therapi.reha.patient;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import CommonTools.DatFunk;
import CommonTools.SqlInfo;
import environment.LadeProg;
import environment.Path;
import hauptFenster.Reha;
import hauptFenster.RehaIOServer;
import hauptFenster.ReverseSocket;
import io.RehaIOMessages;
import oOorgTools.OOTools;
import systemEinstellungen.SystemConfig;

class RgrKopie {

    public RgrKopie(String rezNum) {
        if (!RehaIOServer.rgAfIsActive) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        new LadeProg(Path.Instance.getProghome() + "OpRgaf.jar" + " " + Path.Instance.getProghome()
                                + " " + Reha.getAktIK() + " " + Reha.xport);
                        long start = System.currentTimeMillis();
                        while (!RehaIOServer.rgAfIsActive) {
                            Thread.sleep(50);
                            if ((System.currentTimeMillis() - start) > 5000) {
                                JOptionPane.showMessageDialog(null,
                                        "Kann den Suchenbefehl auf OpRgaf nicht absetzen");
                                return null;
                            }
                        }
                        new ReverseSocket().setzeRehaNachricht(RehaIOServer.rgAfreversePort,
                                "Reha#" + RehaIOMessages.MUST_REZFIND + "#" + rezNum);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }

            }.execute();
        } else {
            new ReverseSocket().setzeRehaNachricht(RehaIOServer.rgAfreversePort,
                    "Reha#" + RehaIOMessages.MUST_GOTOFRONT);
            new ReverseSocket().setzeRehaNachricht(RehaIOServer.rgAfreversePort,
                    "Reha#" + RehaIOMessages.MUST_REZFIND + "#" + rezNum);
        }
        return;
    }

}
