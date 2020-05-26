package org.therapi.reha.patient;

import java.awt.Component;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;

import mandant.IK;
import rezept.Rezept;
import rezept.RezeptDto;

public class RezepteTable extends JTable{



    public RezepteTable(List<Rezept> rezeptListe) {
        super(new RezeptTableModel(rezeptListe));
    }


public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    List<Rezept> rezeptListe = new RezeptDto(new IK("123456789")).getAktuelleRezepteByPatNr(178);
    rezeptListe.addAll(  new RezeptDto(new IK("123456789")).getHistorischeRezepteByPatNr(178));

    JScrollPane jscrollPane = new JScrollPane(new RezepteTable(rezeptListe ));
    frame.add(jscrollPane);
    frame.pack();
    frame.setVisible(true);
}

}
