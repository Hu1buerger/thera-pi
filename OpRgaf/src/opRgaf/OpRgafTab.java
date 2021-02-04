package opRgaf;

import static opRgaf.OffenePosten.Type.AFR;
import static opRgaf.OffenePosten.Type.RGR;
import static opRgaf.OffenePosten.Type.VR;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.looks.windows.WindowsTabbedPaneUI;

import CommonTools.DatFunk;
import CommonTools.SqlInfo;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocumentService;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextFieldService;
import ag.ion.bion.officelayer.text.TextException;
import ag.ion.noa.NOAException;
import core.Feature;
import environment.Path;
import mandant.IK;
import office.OOService;
import opRgaf.CommonTools.DateTimeFormatters;
import opRgaf.rezept.Money;
import opRgaf.rezept.Rezeptnummer;

/** TODO: only public because of RehaIO */
class OpRgafTab extends JXPanel implements ChangeListener {
    private static final long serialVersionUID = -6012301447745950357L;

    private JTabbedPane jtb;

    private Header jxh = new Header();;
    OPRGAFGui opRgafPanel;

    private OpRgafMahnungen opRgafMahnungen;

    private IK ik;

    private Logger logger = LoggerFactory.getLogger(OpRgafTab.class);

    OpRgafTab(OpRgaf opRgaf) {
        this.ik = opRgaf.aktIK;
        setOpaque(false);
        setLayout(new BorderLayout());
        jtb = new JTabbedPane();
        jtb.setUI(new WindowsTabbedPaneUI());
        List<OffenePosten> all;
        try {
            all = new OffenePostenDTO(opRgaf.aktIK).all();
        } catch (SQLException e1) {
            all = Collections.emptyList();
            logger.error("bezug der offenen posten", e1.getErrorCode());
            JOptionPane.showMessageDialog(this, "Fehler beim Bezug der offenen Posten\n" + e1.getMessage(),
                    "Da ist was schief gelaufen", JOptionPane.ERROR_MESSAGE);
        }
        OffenePostenBuchen offenePostenBuchen = new OffenePostenBuchen(opRgaf.iniOpRgAf, opRgaf.aktIK, all);
        opRgafPanel = offenePostenBuchen;
        ActionListener kopierenListener = e -> {
            List<OffenePosten> opToCopy = (List<OffenePosten>) e.getSource();
            for (OffenePosten offenePosten : opToCopy) {
                rechnungskopie(offenePosten);
            }
        };
        offenePostenBuchen.addKopierenListener(kopierenListener);
        ActionListener ausbuchenlistener = e -> {
            List<OffenePosten> opToPay = (List<OffenePosten>) e.getSource();
            if (opToPay.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Keine Rechnung zum Ausbuchen ausgewählt");
            } else {
                for (OffenePosten offenePosten : opToPay) {
                    if (offenePosten.offen.hasSameValue(Money.ZERO)) {
                        JOptionPane.showMessageDialog(null,
                                "Rechnung " + offenePosten.rgNr + " ist bereits auf bezahlt gesetzt");
                    } else {
                        komplettAusbuchen(offenePosten, "bar".equals(e.getActionCommand()));
                    }
                }
                offenePostenBuchen.datachanged();
            }
        };
        offenePostenBuchen.addAusbuchenListener(ausbuchenlistener);

        ActionListener teilzahlen = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Payment zahlung = (Payment) e.getSource();
                teilAusbuchen(zahlung.op, "bar".equals(e.getActionCommand()), zahlung.betrag);
                offenePostenBuchen.datachanged();
            }

        };

        offenePostenBuchen.addTeilzahlenListener(teilzahlen);
        ActionListener paymentListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                OffenePosten op = (OffenePosten) e.getSource();
                new OffenePostenDTO(ik).updatePayment(op);

            }
        };
        offenePostenBuchen.addPaymentUpdateListener(paymentListener);

        jtb.addTab("Rezeptgebühr-/Ausfall-/Verkaufsrechnungen ausbuchen", offenePostenBuchen);

        opRgafMahnungen = new OpRgafMahnungen(opRgaf);
        jtb.addTab("Rezeptgebühr-/Ausfall-/Verkaufsrechnungen Mahnungen", opRgafMahnungen);

        jtb.addChangeListener(this);

        jtb.addChangeListener(jxh);
        add(jxh, BorderLayout.NORTH);
        add(jtb, BorderLayout.CENTER);

        jxh.validate();
        jtb.validate();
        validate();
    }

    private void komplettAusbuchen(OffenePosten op, boolean bar) {

        Money eingang = op.offen;

        teilAusbuchen(op, bar, eingang);

    }

    private void teilAusbuchen(OffenePosten op, boolean bar, Money eingang) {
        System.out.println("Eingang" + eingang);
        Money restbetrag = op.offen.minus(eingang);


        if (bar) {
            String ktext = op.kennung.name + "," + op.rezNummer.rezeptNummer();
            if (ktext.length() > 35) {
                ktext = ktext.substring(0, 34);
            }
            String barkassesql = "insert into kasse set einnahme='" + eingang.toPlainString()
                                                                             .replace(",", ".")
                    + "', datum='" +  op.bezahltAm + "', ktext='" + ktext + "'," + "rez_nr='"
                    + op.rezNummer.rezeptNummer() + "', PAT_INTERN='" + op.patid + "'";
            SqlInfo.sqlAusfuehren(barkassesql);
        }

        String rgafakturaSql = null;

        if (op.type == RGR) { // aus rgaffaktura ausbuchen
            rezeptBezahltSetzen(op.rezNummer.rezeptNummer());
            rgafakturaSql = "update rgaffaktura set roffen='" + restbetrag.toPlainString() + "', rbezdatum='"
                    + op.bezahltAm + "' where id ='" + op.tabellenId + "'";
        } else if (op.type == AFR) { // aus rgaffaktura ausbuchen
            rgafakturaSql = "update rgaffaktura set roffen='" + restbetrag.toPlainString() + "', rbezdatum='"
                    + op.bezahltAm + "' where id ='" + op.tabellenId + "'";
        } else if (op.type == VR) { // aus verkliste ausbuchen
            rgafakturaSql = "update verkliste set v_offen='" + restbetrag.toPlainString() + "', v_bezahldatum='"
                    + op.bezahltAm + "' where verklisteID ='" + op.tabellenId + "'";
        }

        SqlInfo.sqlAusfuehren(rgafakturaSql);
        op.offen = restbetrag;
    }

    private void rezeptBezahltSetzen(String rgaf_reznum) {
        SqlInfo.sqlAusfuehren(
                "update verordn set zzstatus='1', rez_bez='T' where rez_nr = '" + rgaf_reznum + "' LIMIT 1"); // zz:
                                                                                                              // 1-ok
        SqlInfo.sqlAusfuehren("update lza set zzstatus='1', rez_bez='T' where rez_nr = '" + rgaf_reznum + "' LIMIT 1");
    }

    private void rechnungskopie(OffenePosten op) {
        System.out.println(op);

        switch (op.type) {
        case RGR:
            rgrkopie(op);

            break;
        case AFR:
            afrkopie(op);
        default:
            break;
        }
    }

    private Void afrkopie(final OffenePosten op) {
        try {
            int id = op.tabellenId;
            Rezeptnummer rez_nr = op.rezNummer;
            String pat_intern = SqlInfo.holeEinzelFeld(
                    "select pat_intern from rgaffaktura where id='" + id + "' LIMIT 1");
            String rdatum = SqlInfo.holeEinzelFeld("select rdatum from rgaffaktura where id='" + id + "' LIMIT 1");
            AusfallRechnung ausfall = new AusfallRechnung(pat_intern, rez_nr.rezeptNummer(), op.rgNr, rdatum, ik);
            ausfall.setModal(true);
            ausfall.setLocationRelativeTo(null);
            ausfall.toFront();
            ausfall.setVisible(true);
            ausfall = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void rgrkopie(OffenePosten op) {
        String ursprungstabelle = "";
        int id = op.tabellenId;
        String rgnr = op.rgNr;
        String rez_nr = op.rezNummer.rezeptNummer();
        int pat_intern = op.patid;
        LocalDate rdatum = op.rgDatum;
        Money rezgeb = op.rgBetrag;
        Money pauschale = op.bearbeitungsGebuehr;
        Money gesamt = op.gesamtBetrag;
        new InitHashMaps();

        String test = SqlInfo.holeEinzelFeld("select id from verordn where rez_nr = '" + rez_nr + "' LIMIT 1");
        Vector<String> vecaktrez = null;
        if ("".equals(test)) {
            test = SqlInfo.holeEinzelFeld("select id from lza where rez_nr = '" + rez_nr + "' LIMIT 1");
            if (!"".equals(test)) {
                vecaktrez = SqlInfo.holeSatz("lza",
                        " anzahl1,kuerzel1,kuerzel2," + "kuerzel3,kuerzel4,kuerzel5,kuerzel6 ", "id='" + test + "'",
                        Arrays.asList(new String[] {}));
                ursprungstabelle = "lza";
            }
        } else {
            vecaktrez = SqlInfo.holeSatz("verordn",
                    " anzahl1,kuerzel1,kuerzel2," + "kuerzel3,kuerzel4,kuerzel5,kuerzel6 ", "id='" + test + "'",
                    Arrays.asList(new String[] {}));
            ursprungstabelle = "verordn";
        }
        if (vecaktrez != null) {
            String behandlungen = vecaktrez.get(0) + "*" + (!"".equals(vecaktrez.get(1)
                                                                                .trim()) ? vecaktrez.get(1) : "")
                    + (!"".equals(vecaktrez.get(2)
                                           .trim()) ? "," + vecaktrez.get(2) : "")
                    + (!"".equals(vecaktrez.get(3)
                                           .trim()) ? "," + vecaktrez.get(3) : "")
                    + (!"".equals(vecaktrez.get(4)
                                           .trim()) ? "," + vecaktrez.get(4) : "")
                    + (!"".equals(vecaktrez.get(5)
                                           .trim()) ? "," + vecaktrez.get(5) : "")
                    + (!"".equals(vecaktrez.get(6)
                                           .trim()) ? "," + vecaktrez.get(6) : "");

            String cmd = "select abwadress,id from pat5 where pat_intern='" + pat_intern + "' LIMIT 1";
            Vector<Vector<String>> adrvec = SqlInfo.holeFelder(cmd);
            String[] adressParams = null;
            String patid = adrvec.get(0)
                                 .get(1);
            PatientenAdressen patientenAdressen = new PatientenAdressen(patid);
            if ("T".equals(adrvec.get(0)
                                 .get(0))) {
                adressParams = patientenAdressen.holeAbweichendeAdresse(patid);
            } else {
                adressParams = patientenAdressen.getAdressParams(patid);
            }
            HashMap<String, String> hmRezgeb = new HashMap<>();
            hmRezgeb.put("<rgreznum>", rez_nr);
            hmRezgeb.put("<rgbehandlung>", behandlungen);
            hmRezgeb.put("<rgdatum>", DatFunk.sDatInDeutsch(SqlInfo.holeEinzelFeld(
                    "select rez_datum from " + ursprungstabelle + " where rez_nr='" + rez_nr + "' LIMIT 1")));
            hmRezgeb.put("<rgbetrag>", op.rgBetrag.toPlainString()
                                                  .replace(".", ","));
            hmRezgeb.put("<rgpauschale>", pauschale.toPlainString()
                                                   .replace(".", ","));
            hmRezgeb.put("<rggesamt>", gesamt.toPlainString()
                                             .replace(".", ","));
            hmRezgeb.put("<rganrede>", adressParams[0]);
            hmRezgeb.put("<rgname>", adressParams[1]);
            hmRezgeb.put("<rgstrasse>", adressParams[2]);
            hmRezgeb.put("<rgort>", adressParams[3]);
            hmRezgeb.put("<rgbanrede>", adressParams[4]);
            hmRezgeb.put("<rgorigdatum>", rdatum.format(DateTimeFormatters.ddMMYYYYmitPunkt));
            hmRezgeb.put("<rgnr>", rgnr);

            hmRezgeb.put("<rgpatnname>", op.kennung.name);
            hmRezgeb.put("<rgpatvname>", op.kennung.vorname);
            hmRezgeb.put("<rgpatgeboren>", op.kennung.geburtstag.format(DateTimeFormatters.ddMMYYYYmitPunkt));
            // System.out.println(hmRezgeb);
            String url = Path.Instance.getProghome() + "vorlagen/" + ik.digitString()
                    + "/RezeptgebuehrRechnung.ott.Kopie.ott";
            try {
                officeStarten(url, hmRezgeb);
            } catch (OfficeApplicationException | NOAException e) {
                e.printStackTrace();
            } catch (TextException e) {
                e.printStackTrace();
            }
        } else {
            logger.error("das Rezept: " + rez_nr
                    + " konnte nicht gefunden werden, daher kann keine Rechnungskopie erstellt werden. ");
        }
    }

    private void officeStarten(String url, HashMap<String, String> hmRezgeb)
            throws OfficeApplicationException, NOAException, TextException {
        IDocumentService documentService;
        //// System.out.println("Starte Datei -> "+url);

        documentService = new OOService().getOfficeapplication()
                                         .getDocumentService();

        IDocumentDescriptor docdescript = new DocumentDescriptor();
        docdescript.setHidden(true);
        docdescript.setAsTemplate(true);
        IDocument document;

        document = documentService.loadDocument(url, docdescript);
        ITextDocument textDocument = (ITextDocument) document;
        // OOTools.druckerSetzen(textDocument,
        // SystemConfig.hmAbrechnung.get("hmgkvrechnungdrucker"));
        ITextFieldService textFieldService = textDocument.getTextFieldService();
        ITextField[] placeholders;

        placeholders = textFieldService.getPlaceholderFields();
        String placeholderDisplayText = "";

        for (int i = 0; i < placeholders.length; i++) {
            placeholderDisplayText = placeholders[i].getDisplayText()
                                                    .toLowerCase();
            Set<Entry<String, String>> entries = hmRezgeb.entrySet();
            Iterator<Entry<String, String>> it = entries.iterator();
            while (it.hasNext()) {
                Entry<String, String> entry = it.next();
                if (((String) entry.getKey()).toLowerCase()
                                             .equals(placeholderDisplayText)) {
                    placeholders[i].getTextRange()
                                   .setText((String) entry.getValue());

                    break;
                }
            }
        }
        textDocument.getFrame()
                    .getXFrame()
                    .getContainerWindow()
                    .setVisible(true);
    }

    @Override
    public void stateChanged(ChangeEvent arg0) {
        JTabbedPane pane = (JTabbedPane) arg0.getSource();
        int sel = pane.getSelectedIndex();
        try {
            switch (sel) {
            case 0:
                // opRgafPanel.initSelection();
                break;
            case 1:
                opRgafMahnungen.initSelection();
                break;
            }
        } catch (Exception ex) {
        }

    }

    void setFirstFocus() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new InitHashMaps();
                opRgafPanel.setzeFocus();
            }
        });
    }

    void sucheRezept(String rezept) {
        opRgafPanel.sucheRezept(rezept);
    }
}
