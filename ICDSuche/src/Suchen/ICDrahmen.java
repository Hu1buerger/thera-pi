package Suchen;

import java.awt.Dimension;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JFrame;

import gui.LaF;
import logging.Logging;
import mandant.IK;
import sql.DatenquellenFactory;

public class ICDrahmen implements Runnable {

    private JFrame jFrame;
    Connection conn;

    public static void main(String[] args) throws SQLException {
        LaF.setPlastic();
        new Logging("icd");
        IK ik;
        if (args.length > 0) {
            ik = new IK(args[0]);
        } else {
            ik = new IK("123456789");
        }
        Connection conn = new DatenquellenFactory().with(ik).createConnection();
        System.out.println(conn.isClosed());
        ICDrahmen icd = new ICDrahmen(conn);
        icd.getJFrame();

    }

    public ICDrahmen(Connection conn) {
        this.conn = conn;

    }

    @Override
    public void run() {
        getJFrame();

    }

    public JFrame getJFrame() {
        int xWidth = 800, yWidth = 600, xPos = 200, yPos = 200;

        jFrame = new JFrame();
        jFrame.setSize(xWidth, yWidth);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jFrame.setPreferredSize(new Dimension(xWidth, yWidth));
        jFrame.setTitle("ICD-Recherche");
        jFrame.setContentPane(new ICDoberflaeche(new SqlInfo(conn)));
        
        Dimension screensize = java.awt.Toolkit.getDefaultToolkit().getScreenSize(); 
        xPos = (int)(screensize.getWidth()/2 - xWidth/2 - 50);
        jFrame.setLocation(xPos, yPos);

        jFrame.pack();
        jFrame.setVisible(true);
        return jFrame;
    }

}
