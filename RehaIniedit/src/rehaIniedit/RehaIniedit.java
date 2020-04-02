package rehaIniedit;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import CommonTools.SqlInfo;
import environment.Path;
import logging.Logging;
import sql.DatenquellenFactory;

public class RehaIniedit implements WindowListener {

    public static boolean DbOk;
    JFrame jFrame;
    public static JFrame thisFrame = null;
    public Connection conn;
    public static RehaIniedit thisClass;



    public static String progHome = "C:/RehaVerwaltung/";
    public static String aktIK = "510841109";

    public static boolean testcase = false;

    public static HashMap<String, ImageIcon> symbole = new HashMap<String, ImageIcon>();

    SqlInfo sqlInfo = null;

    public static void main(String[] args) {
        new Logging("iniedit");
        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
            UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);
            UIManager.put("TabbedPane.tabsOpaque", Boolean.FALSE);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }
        if (args.length > 0) {
            if (!testcase) {
                System.out.println("hole daten aus INI-Datei " + args[0]);
                progHome = Path.Instance.getProghome();
                aktIK = args[1];
            }
        }
        RehaIniedit application = new RehaIniedit();
        thisClass = application.getInstance();
        thisClass.sqlInfo = new SqlInfo();
        thisClass.starteDB();
        thisClass.getJFrame();
    }

    private RehaIniedit getInstance() {
        return this;
    }

    private void starteDB() {
        DatenbankStarten dbstart = new DatenbankStarten();
        dbstart.run();
    }

    private JFrame getJFrame() {
        jFrame = new JFrame() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void setVisible(final boolean visible) {

                if (getState() != JFrame.NORMAL) {
                    setState(JFrame.NORMAL);
                }

                if (visible) {
                    // setDisposed(false);
                }
                if (!visible || !isVisible()) {
                    super.setVisible(visible);
                }

                if (visible) {
                    int state = super.getExtendedState();
                    state &= ~JFrame.ICONIFIED;
                    super.setExtendedState(state);
                    super.setAlwaysOnTop(true);
                    super.toFront();
                    super.requestFocus();
                    super.setAlwaysOnTop(false);
                }
            }

            @Override
            public void toFront() {
                super.setVisible(true);
                int state = super.getExtendedState();
                state &= ~JFrame.ICONIFIED;
                super.setExtendedState(state);
                super.setAlwaysOnTop(true);
                super.toFront();
                super.requestFocus();
                super.setAlwaysOnTop(false);
            }
        };
        sqlInfo.setFrame(jFrame);
        jFrame.addWindowListener(this);
        jFrame.setSize(950, 650);
        jFrame.setPreferredSize(new Dimension(950, 650));
        String sTitle = "Thera-Pi INI-Editor --> [IK: " + aktIK + "]" ;
        jFrame.setTitle(sTitle);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        jFrame.getContentPane()
              .add(new IniEditPanel());
        jFrame.pack();
        jFrame.setVisible(true);

        return jFrame;
    }

    final class DatenbankStarten implements Runnable {
        private void StarteDB() {
            final RehaIniedit obj = RehaIniedit.thisClass;

            final String sDB = "SQL";
            if (obj.conn != null) {
                try {
                    obj.conn.close();
                } catch (final SQLException e) {
                }
            }
            try {
                Class.forName("com.mysql.jdbc.Driver")
                     .newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
                System.out.println(sDB + "Treiberfehler: " + e.getMessage());
                RehaIniedit.DbOk = false;
                return;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                System.out.println(sDB + "Treiberfehler: " + e.getMessage());
                RehaIniedit.DbOk = false;
                return;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println(sDB + "Treiberfehler: " + e.getMessage());
                RehaIniedit.DbOk = false;
                return;
            }
            try {

                obj.conn = new DatenquellenFactory(aktIK).createConnection();
                RehaIniedit.thisClass.sqlInfo.setConnection(obj.conn);
                RehaIniedit.DbOk = true;
                System.out.println("Datenbankkontakt hergestellt");

            } catch (final SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
                RehaIniedit.DbOk = false;

            }
            return;
        }

        @Override
        public void run() {
            StarteDB();
        }

    }

    /*****************************************************************/

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (conn != null) {
            try {
                System.out.println("DBKontakt geschlossen");
                conn.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

}
