package patientenFenster;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;

import org.jdesktop.swingx.JXPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import CommonTools.DatFunk;
import CommonTools.JCompTools;
import commonData.Rezeptvector;
import environment.Path;
import events.RehaTPEventClass;
import events.RehaTPEventListener;
import hauptFenster.Reha;

public class VoEditWindow extends JXPanel {  // implements ActionListener, KeyListener, FocusListener, RehaTPEventListener

    public boolean neu = false;
    public Vector<String> vec = null; // 'vecaktrez' aus dem rufenden Programm zum editieren oder als Kopiervorlage
    private Connection connection = null;

    private RehaTPEventClass rtp = null;

    private Rezeptvector myRezept = null;
    
    public JButton toLeft = new JButton("<");
    public JButton toRight = new JButton(">");
    private int anzMasks = 2;
    private int idxMask2020 = 0;
    private int idxOldMask = 1;
    private int idxMask = idxMask2020;

    private JXPanel[] eingabeMasken = new JXPanel[anzMasks];
    private JXPanel aktiveMaske = null;

    
    public VoEditWindow(Vector<String> vec, boolean neu, Connection connection) { 
        super();

        this.neu = neu;
        this.vec = vec;
        this.connection = connection;
        myRezept = new Rezeptvector();
        myRezept.setVec_rez(this.vec);

        setName("RezeptNeuanlage");
                
        // Voreinstellung: neue Eingabemaske (alte anhand RezDatum)
        boolean preHMR2020 = (DatFunk.TageDifferenz("01.01.2021", DatFunk.sHeute()) < 0);
        if (!this.neu) {
            String datIsSet = DatFunk.sDatInDeutsch(myRezept.getRezeptDatum());
            preHMR2020 = (DatFunk.TageDifferenz("01.01.2021", datIsSet) < 0);
        }
        if (preHMR2020) {
            idxMask = idxOldMask;
            eingabeMasken[idxOldMask] = new RezNeuanlage((Vector<String>) this.vec.clone(), this.neu, connection);
        } else {
            eingabeMasken[idxMask2020] = new RezNeuanlage2020((Vector<String>) this.vec.clone(), this.neu, connection);            
        }

        aktiveMaske = eingabeMasken[idxMask];

        setLayout(new BorderLayout());
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 0));
        add(getCardSwitchPanel(), BorderLayout.NORTH);
        this.add(aktiveMaske, BorderLayout.CENTER);
        showMask();
        validate();
        activateButtons();
    }

    private JXPanel getCardSwitchPanel() {
        JXPanel jpan = JCompTools.getEmptyJXPanel();
        jpan.setOpaque(false);
        FormLayout lay = new FormLayout(
                // 1    2            3     4 
                "15dlu,fill:0:grow,15dlu,5dlu",
                // 1  2
                "2dlu,p");
        CellConstraints cc = new CellConstraints();
        jpan.setLayout(lay);
        jpan.add(toLeft, cc.xy(1, 2));
        toLeft.addActionListener(e -> actionToLeft(e));
        toLeft.setToolTipText("zur neuen Eingabemaske wechseln");

        jpan.add(toRight, cc.xy(3, 2));
        toRight.addActionListener(e -> actiontoRight(e));
        toRight.setToolTipText("zur alten Eingabemaske wechseln");

        try {
            BufferedImage a = ImageIO.read( new File( Path.Instance.getProghome() + "icons/therapieMT1.gif" ) );
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jpan;
    }

    private void showMask() {
        aktiveMaske.setVisible(false);
        if(idxMask == idxMask2020) {
            eingabeMasken[idxMask2020] = new RezNeuanlage2020((Vector<String>) this.vec.clone(), this.neu, this.connection);
            setBackgroundPainter(Reha.instance.compoundPainter.get("ArztPanel"));
        }
        if(idxMask == idxOldMask) {
            eingabeMasken[idxOldMask] = new RezNeuanlage((Vector<String>) this.vec.clone(), this.neu, this.connection);
            setBackgroundPainter(Reha.instance.compoundPainter.get("RezNeuanlage"));
        }
        aktiveMaske = eingabeMasken[idxMask];
        this.add(aktiveMaske, BorderLayout.CENTER);
        aktiveMaske.setVisible(true);
        validate();
    }
    
    private void activateButtons() {
        if (idxMask == 0) {
            toLeft.setEnabled(false);
        } else {
            toLeft.setEnabled(true);            
        }
        if (idxMask == (anzMasks - 1)) {
            toRight.setEnabled(false);
        } else {
            toRight.setEnabled(true);            
        }
    }
    
    private Object actionToLeft(ActionEvent e) {
        idxMask--;
        if (idxMask < 0) {idxMask = anzMasks - 1;}
        showMask();
        activateButtons();
        return null;
    }

    private Object actiontoRight(ActionEvent e) {
        idxMask++;
        if (idxMask == anzMasks) {idxMask = 0;}
        showMask();
        activateButtons();
        return null;
    }
}
