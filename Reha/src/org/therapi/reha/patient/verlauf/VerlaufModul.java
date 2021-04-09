package org.therapi.reha.patient.verlauf;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import hauptFenster.Reha;
import mandant.IK;
import sql.Datenquelle;
import sql.DatenquellenFactory;

public class VerlaufModul {

    private VerlaufPanel panel = new VerlaufPanel();
    private Verlaufgate verlaufgate;
    private int pat_id;
    private TerminGate termingate;

    public VerlaufModul(IK ik) {
        DatenquellenFactory ds = new DatenquellenFactory(ik.digitString());
        verlaufgate = new Verlaufgate(ds);
        termingate = new TerminGate(ds);
        ActionListener saveListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                verlaufgate.save(panel.verlaeufe());
                refreshData();

            }
        };
        panel.addSaveActionListener(saveListener);

        panel.addAbortActionListener(action -> refreshData());

    }

    public Component component() {
        return panel;
    }

    public void refreshData(int pat_id) {
        this.pat_id = pat_id;
        List<Verlauf> verlaufliste = verlaufgate.findByPatientId(pat_id);
        ;
        List<LocalDate> dateList = verlaufliste.stream()
                                               .map(verlauf -> verlauf.documentedDay)
                                               .collect(Collectors.toList());
        List<Termin> terminliste = termingate.findByPatientId(pat_id);
        List<Verlauf> undokumented = terminliste.stream()
                                                .filter(termin -> !dateList.contains(termin.date))
                                                .map(termin -> new Verlauf(termin, Reha.aktUser))
                                                .collect(Collectors.toList());
        verlaufliste.addAll(undokumented);

        panel.setVerlaufListe(verlaufliste);

    }

    void refreshData() {
        refreshData(pat_id);
    }

    public static void main(String[] args) {
        VerlaufModul vm = new VerlaufModul(new IK("987654321"));

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(vm.component());
        frame.pack();
        frame.setVisible(true);
        vm.refreshData(1);
    }
}
