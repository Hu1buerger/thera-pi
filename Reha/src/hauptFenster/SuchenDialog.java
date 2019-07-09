package hauptFenster;



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTitledPanel;
import org.therapi.reha.patient.PatientHauptLogic;
import org.therapi.reha.patient.PatientToolBarPanel;

import CommonTools.RehaEvent;
import CommonTools.RehaEventClass;
import events.PatStammEvent;
import events.PatStammEventClass;
import events.RehaTPEvent;
import events.RehaTPEventListener;
import gui.Cursors;
import systemEinstellungen.SystemConfig;

public class SuchenDialog extends JXDialog implements RehaTPEventListener{

	private static final long serialVersionUID = 1L;
	private JXPanel jContentPane = null;
	private JXTitledPanel jXTitledPanel = null;
	private JXPanel jContent = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private JXButton jButton = null;
	private JXTable jtable = null;
	public JTextField jTextField = null;

	private JTextField jtext = null;

	private int clickX;
	private int clickY;

	private Cursor cmove = Cursors.cmove;
	private Cursor cnsize = Cursors.cnsize;
	private Cursor cnwsize = Cursors.cnwsize;
	private Cursor cnesize = Cursors.cnesize;
	private Cursor cswsize = Cursors.cswsize;
	private Cursor cwsize = Cursors.cwsize;
	private Cursor csesize = Cursors.csesize;
	private Cursor cssize = Cursors.cssize;
	private Cursor cesize = Cursors.cesize;
	private Cursor cdefault = Cursors.cdefault;

	private boolean insize;
	private int[] orgbounds = {0,0};
	private int sizeart;
	private String[] sEventDetails ={null,null};

	public JComponent focusBack = null;
	private String fname = "";  //  @jve:decl-index=0:
	public DefaultTableModel tblDataModel;
	public boolean jumpok = false;
	public  int suchart = 0;
	private PatientToolBarPanel toolBar;

	/**
	 * @param
	 */
	private PatientHauptLogic aufrufer = null;
	public SuchenDialog(JXFrame owner,JComponent focusBack,String fname,int art,PatientHauptLogic xaufrufer) {
		super(owner, (JComponent)Reha.getThisFrame().getGlassPane());
		this.focusBack = (focusBack == null ? null : focusBack);
		this.fname = fname.equals("") ? "" : fname;
		this.suchart = art;
		this.aufrufer = xaufrufer;
		toolBar = aufrufer.patientHauptPanel.patToolBarPanel;
		initialize();
		jTextField.setText(fname);
		new suchePatient().init(tblDataModel);
		this.setAlwaysOnTop(true);
		SwingUtilities.invokeLater(new Runnable(){
			@Override
            public void run(){
				setzeFocus();
			}
		});
	}
	private void setzeFocus(){
		SwingUtilities.invokeLater(new Runnable(){
			@Override
            public void run(){
				jTextField.requestFocus();
			}
		});
	}

	public void	setzeReihe(Vector<?> vec){
		tblDataModel.addRow(vec);
		jtable.validate();

	}

	@Override
    public void rehaTPEventOccurred(RehaTPEvent evt) {

		try{
			if(evt.getDetails()[0] != null){
				if(evt.getDetails()[0].equals(this.getName())){
					this.setVisible(false);
				}
			}
		}catch(NullPointerException ne){

	}

	}

	public void suchDasDing(String suchkrit){
		jTextField.setText(suchkrit);
		jXTitledPanel.setTitle("Suche Patient..."+suchkrit);
		new suchePatient().init(tblDataModel);

	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {

		// Lemmi 20101212: zuletzt eingestellte und gemerkte Dimension des Suchfensters zurückholen
		Dimension dim = new Dimension(300, 400);  // Diese Defaultwerte haben keine Wirkung !

		dim.width = SystemConfig.hmPatientenSuchenDlgIni.get("fensterbreite");
		dim.height = SystemConfig.hmPatientenSuchenDlgIni.get("fensterhoehe");

		this.setSize(dim);

		this.setUndecorated(true);
		this.setTitle("Dialog-Test");
		this.setContentPane(getJContentPane());

		this.setName("PatSuchen");
		this.setModal(false);
		this.setResizable(true);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public void setzeFocusAufSucheFeld(){
		jtext.requestFocus();

	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {

			GridBagConstraints gridBagConstraints = new GridBagConstraints() ;
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = GridBagConstraints.CENTER;
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.ipadx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0D;
			gridBagConstraints.weighty = 1.0D;
			gridBagConstraints.insets = new Insets(5, 5, 5, 5);  // Lemmi: weißer Rand zwischen Fenster und innerem grauen Bereich

			JXPanel gridJx = new JXPanel();
			gridJx.setLayout(new GridBagLayout());
			gridJx.setBackground(Color.WHITE);
			gridJx.setBorder(null);

			jContentPane = new JXPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.setBorder(null);
			JXTitledPanel jtp = getJXTitledPanel();


			jContent = new JXPanel(new BorderLayout());
			jContent.setSize(new Dimension(286, 162));
			jContent.add(getJButton(), BorderLayout.SOUTH);
			JXPanel jp1 = new JXPanel(new FlowLayout());
			jp1.setBorder(null);

			// Lemmi 20101212: Das Labelfeld für einen Hinweis auf aktuelle Rezepte "mißbraucht"
			JLabel jlb = new JLabel( suchart == toolBar.getAktRezIdx() ? "<html>Nur die <b>aktuellen</b> Rezepte" : "Patient suchen: ");  // Lemmi 20101212: Prompt für die Eingabe eines Suchkriteriums
			jp1.add(jlb);

			jtext = getJTextField();
			// Lemmi 20101212: Das Such-Eingabefeld unsichtbar gemacht - wird hier nicht benötigt
			if(suchart == toolBar.getAktRezIdx()){
				jtext.setPreferredSize(new Dimension(0,0));
			} else{
				jtext.setPreferredSize(new Dimension(100,20));
			}
			jtext.addKeyListener(new java.awt.event.KeyAdapter() {
				@Override
                public void keyPressed(java.awt.event.KeyEvent e) {
					if (e.getKeyCode() == 10){   // RETURN gedrückt
						e.consume();
						new SwingWorker<Void,Void>(){

							@Override
							protected Void doInBackground() throws Exception {
								new suchePatient().init(tblDataModel);
								aufrufer.setLastRow(jtable.getSelectedRow());
								return null;
							}
						}.execute();

					}
					if (e.getKeyCode() == 27){   // ESC gedrückt
						e.consume();
						setVisible(false);
						aufrufer.setLastRow(-1);
						sucheBeenden();
					}

					if (e.getKeyCode() == 40){  // Lemmi: was meint das ?
						e.consume();
						if (jtable.getRowCount() > 0){
							jtable.requestFocus();
							if(jtable.getSelectedRow() >= 0){
								jtable.requestFocus();
							}else{
								if(jumpok){
									jtable.setRowSelectionInterval(aufrufer.getLastRow(),aufrufer.getLastRow());
								}else{
									jtable.setRowSelectionInterval(0,0);
								}

							}
						}
					}
				}
			});
			jp1.add(jtext);  // Lemmi: Suchfeld im Dialog einfügen (im Norden = oben)
			jContent.add(jp1, BorderLayout.NORTH);

			/**
			 * JXTable
			 */
			JScrollPane jscr = new JScrollPane();
			JXPanel jp2 = new JXPanel(new BorderLayout());

			Vector <String>reiheVector = new Vector<String>();
			reiheVector.addElement("Nachname");
			reiheVector.addElement("Vorname");
			reiheVector.addElement("Geboren");
			reiheVector.addElement("Pat-Nr.");
			if(suchart==toolBar.getAktRezIdx()){   // Lemmi 20101212: komplettes if mit neuer Spalte "Rezepte" ergänzt
				reiheVector.addElement("Rezepte");
			}
			tblDataModel = new DefaultTableModel();
			tblDataModel.setColumnIdentifiers(reiheVector);
			jtable = new JXTable(tblDataModel);
			this.jtable.getColumnModel().getColumn(0).setPreferredWidth(100);
			this.jtable.getColumn(3).setMinWidth(0);	// Spalte pat_intern ausblenden
			this.jtable.getColumn(3).setMaxWidth(0);	// Breite der Spalte pat_intern

			// Lemmi 20101212: Einige maximale Spaltenbreiten fixiert
			if(suchart == toolBar.getAktRezIdx()) {  // Spielereine, funktioniert alles
				this.jtable.getColumn(2).setPreferredWidth(80);  	// Geboren
				this.jtable.getColumn(2).setMaxWidth(100);  		// Geboren
				this.jtable.getColumn(4).setPreferredWidth(100);  	// Rezepte
//				this.jtable.setGridColor(Color.red);
			}


			InputMap inputMap = jtable.getInputMap(JComponent.WHEN_FOCUSED);

			inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0));
			jtable.setInputMap(JComponent.WHEN_FOCUSED,inputMap);

			jtable.setEditable(false);

			jtable.addKeyListener(new java.awt.event.KeyAdapter() {
				@Override
                public void keyPressed(java.awt.event.KeyEvent e) {
					if (e.getKeyCode() == 10){								// ENTER
						aufrufer.setLastRow(jtable.getSelectedRow());
						sucheAbfeuern();
						e.consume();
						setVisible(false);
					}
					if (e.getKeyCode() == 40 ||e.getKeyCode() == 38){		// ArrDwn, ArrUp
						//sucheAbfeuern();
					}
					if (e.getKeyCode() == KeyEvent.VK_F && e.isAltDown()){	// [Taste-??]
						jTextField.requestFocus();
					}
					if (e.getKeyCode() == 27){								// ESC
						e.consume();
						setVisible(false);
						sucheBeenden();
					}
				}
			});

			jtable.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
					SuchenDialog.this.setCursor(cdefault);
				}
				@Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
					if(e.getClickCount() == 2){
						aufrufer.setLastRow(jtable.getSelectedRow());
						sucheAbfeuern();
						e.consume();
						setVisible(false);
					}
				}
			});
			jtable.updateUI();
			jscr.setViewportView(jtable);
			jp2.add(jscr,BorderLayout.CENTER);

			jContent.add(jp2, BorderLayout.CENTER);
			gridJx.add(jContent,gridBagConstraints);
			jtp.setContentContainer(gridJx);
			jtp.validate();
			jContentPane.add(jtp, BorderLayout.CENTER);

			if(suchart == toolBar.getAktRezIdx()){
				// NOTHING to do
				// Lemmi 20101212: wir brauchen kein Suchwert-Eingabefeld im Ergebnisdialog
				jtext.setText("");
			}
			else {
				jtext.requestFocus();
			}
		}
		return jContentPane;
	}
	public void sucheAbfeuern(){
		String s1 = String.valueOf("#PATSUCHEN");
		String s2 = (String) jtable.getValueAt(jtable.getSelectedRow(), 3);
		setDetails(s1,s2) ;
		PatStammEvent pEvt = new PatStammEvent(SuchenDialog.this);
		pEvt.setPatStammEvent("PatSuchen");
		pEvt.setDetails(s1,s2,fname) ;
		PatStammEventClass.firePatStammEvent(pEvt);
		Reha.instance.lastSelectedPat = jtable.getSelectedRow();
	}
	public void sucheBeenden(){
		PatSuchenDlgIniSave();
		String s1 = String.valueOf("#SUCHENBEENDEN");
		String s2 = "";
		setDetails(s1,s2) ;
		PatStammEvent pEvt = new PatStammEvent(SuchenDialog.this);
		pEvt.setPatStammEvent("PatSuchen");
		pEvt.setDetails(s1,s2,fname) ;
		PatStammEventClass.firePatStammEvent(pEvt);
		Reha.instance.lastSelectedPat = -1;
	}
	// Lemmi 20101212: Merken der Defaultwerte für den nächsten Aufruf
	// speichere Dimension und Suchart in der INI-Datei für nächsten Aufruf
	public void PatSuchenDlgIniSave(){
		Dimension dim = SuchenDialog.this.getSize();

		SystemConfig.hmPatientenSuchenDlgIni.put("fensterbreite", dim.width);
		SystemConfig.hmPatientenSuchenDlgIni.put("fensterhoehe", dim.height);
		SystemConfig.hmPatientenSuchenDlgIni.put("suchart", suchart);

		// !! Werte landen erstmal nur in der HashMap; in der ini nur bei Speichern der 'Einstellungen -> Bedienung'!!
	}


	/**
	 * This method initializes JXTitledPanel
	 *
	 * @return org.jdesktop.swingx.JXTitledPanel
	 */
	private JXTitledPanel getJXTitledPanel() {
		if (jXTitledPanel == null) {
			jXTitledPanel = new JXTitledPanel();

			/*
			// Lemmi 20101212: Erweitert um "Patienten mit aktuellen Rezepten"
			String kriterium[]={"Nachname Vorname","Patienten-ID","Vorname Nachname",
					"Telefon privat","Telefon geschäftl.","Telefon mobil","Notizen", "Nur Patienten mit aktuellen Rezepten"};

			jXTitledPanel.setTitle("Suche Patient..."+this.fname+" nach "+kriterium[suchart]);
			*/
			String titel = "Suche ";
			String kriterium = toolBar.getKritAsString(suchart);
			if (suchart == toolBar.getAktRezIdx()){		// "Nur Patienten mit aktuellen Rezepten"
				titel = titel + kriterium;
			}else{
				titel = titel + "Patient..."+this.fname+" nach "+kriterium;
			}
			jXTitledPanel.setTitle(titel);
			jXTitledPanel.setTitleForeground(Color.WHITE);
			jXTitledPanel.setName("PatSuchen");
			JXButton jb2 = new JXButton();
			jb2.setBorder(null);
			jb2.setOpaque(false);
			jb2.setPreferredSize(new Dimension(16,16));
			jb2.setIcon(SystemConfig.hmSysIcons.get("rot"));
			jb2.addMouseListener(new java.awt.event.MouseAdapter(){
				@Override
                public void mouseClicked(java.awt.event.MouseEvent e) {		// <- Window-Close-Button
					e.consume();
					setVisible(false);
					sucheBeenden();
				}
			});
			jXTitledPanel.setRightDecoration(jb2);
			jXTitledPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
				@Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
					for(int i = 0; i < 1; i++){
						sizeart=-1;
						setCursor(cdefault);
						if ((e.getX() <= 4 && e.getY() <= 4)){ //nord-west
							insize = true;
							sizeart = 1;
							orgbounds[0]=e.getXOnScreen();
							orgbounds[1]=e.getYOnScreen();
							setCursor(cnwsize);
							break;
						}
						if( (e.getX()>=  (((JComponent) e.getSource()).getWidth()-4)) && e.getY() <= 4){//nord-ost
							insize = true;
							sizeart = 2;
							orgbounds[0]=e.getXOnScreen();
							orgbounds[1]=e.getYOnScreen();
							setCursor(cnesize);
							break;
						}
						if(e.getY() <= 4){//nord
							insize = true;
							sizeart = 3;
							orgbounds[0]=e.getXOnScreen();
							orgbounds[1]=e.getYOnScreen();
							setCursor(cnsize);
							break;
						}
						if ((e.getX() <= 4 && e.getY() >= (((JComponent) e.getSource()).getHeight()-4))){ //s�d-west
							insize = true;
							sizeart = 4;
							orgbounds[0]=e.getXOnScreen();
							orgbounds[1]=e.getYOnScreen();
							setCursor(cswsize);
							break;
						}
						if ((e.getX() <= 4)){ //west
							insize = true;
							sizeart = 5;
							orgbounds[0]=e.getXOnScreen();
							orgbounds[1]=e.getYOnScreen();
							setCursor(cwsize);
							break;
						}
						if ((e.getX()>=  (((JComponent) e.getSource()).getWidth()-4)) && //s�d-ost
								e.getY() >= (((JComponent) e.getSource()).getHeight()-4)){
							insize = true;
							sizeart = 6;
							orgbounds[0]=e.getXOnScreen();
							orgbounds[1]=e.getYOnScreen();
							setCursor(csesize);
							break;
						}
						if (e.getY() >= (((JComponent) e.getSource()).getHeight()-2)){ //s�d
							insize = true;
							sizeart = 7;
							orgbounds[0]=e.getXOnScreen();
							orgbounds[1]=e.getYOnScreen();
							setCursor(cssize);
							break;
						}
						if (e.getX() >= (((JComponent) e.getSource()).getWidth()-4)){ //ost
							insize = true;
							sizeart = 8;
							orgbounds[0]=e.getXOnScreen();
							orgbounds[1]=e.getYOnScreen();
							setCursor(cesize);
							break;
						}

						insize = false;
						sizeart = -1;
						setCursor(cdefault);
					}
				}
				@Override
                public void mouseDragged(java.awt.event.MouseEvent e) {
					if (! insize && clickY > 0){
						SuchenDialog.this.getLocationOnScreen();
						SuchenDialog.this.setLocation(e.getXOnScreen()-clickX,e.getYOnScreen()-clickY);
						SuchenDialog.this.repaint();
						setCursor(cmove);
					}else if (insize){
						Dimension dim = SuchenDialog.this.getSize();
						int oX = e.getXOnScreen();
						int oY = e.getYOnScreen();
						for(int i = 0;i<1;i++){  // Lemmi Frage: Was ist denn das für eine magische Konstruktion: Warum kein switch????
							if(sizeart==1){ //nord-west
								dim.width = (oX > orgbounds[0] ? dim.width-(oX-orgbounds[0]) : dim.width+(orgbounds[0]-oX));
								dim.height = (oY > orgbounds[1] ? dim.height-(oY-orgbounds[1]) : dim.height+(orgbounds[1]-oY));
								dim.width = (dim.width < 185 ? 185 : dim.width);
								dim.height = (dim.height < 125 ? 125 : dim.height);
								orgbounds[0] = oX;
								orgbounds[1] = oY;
								SuchenDialog.this.setSize(dim);
								SuchenDialog.this.setLocation(e.getXOnScreen(),e.getYOnScreen());
								setCursor(cnwsize);
								break;
							}
							if(sizeart==2){ //nord-ost
								dim.width = (oX > orgbounds[0] ? dim.width+(oX-orgbounds[0]) : dim.width-(orgbounds[0]-oX));
								dim.height = (oY > orgbounds[1] ? dim.height-(oY-orgbounds[1]) : dim.height+(orgbounds[1]-oY));
								dim.width = (dim.width < 185 ? 185 : dim.width);
								dim.height = (dim.height < 125 ? 125 : dim.height);
								orgbounds[0] = oX;
								orgbounds[1] = oY;
								SuchenDialog.this.setSize(dim);
								SuchenDialog.this.setLocation(e.getXOnScreen()-dim.width,e.getYOnScreen());
								setCursor(cnesize);
								break;
							}
							if(sizeart==3){ //nord
								//dim.width = (oX > orgbounds[0] ? dim.width+(oX-orgbounds[0]) : dim.width-(orgbounds[0]-oX));
								dim.height = (oY > orgbounds[1] ? dim.height-(oY-orgbounds[1]) : dim.height+(orgbounds[1]-oY));
								dim.width = (dim.width < 185 ? 185 : dim.width);
								dim.height = (dim.height < 125 ? 125 : dim.height);
								orgbounds[0] = oX;
								orgbounds[1] = oY;
								SuchenDialog.this.setSize(dim);
								SuchenDialog.this.setLocation(e.getXOnScreen()-e.getX(),e.getYOnScreen());
								setCursor(cnsize);
								break;
							}
							if(sizeart==4){ //s�d-west
								dim.width = (oX > orgbounds[0] ? dim.width-(oX-orgbounds[0]) : dim.width+(orgbounds[0]-oX));
								dim.height = (oY > orgbounds[1] ? dim.height+(oY-orgbounds[1]) : dim.height-(orgbounds[1]-oY));
								dim.width = (dim.width < 185 ? 185 : dim.width);
								dim.height = (dim.height < 125 ? 125 : dim.height);
								orgbounds[0] = oX;
								orgbounds[1] = oY;
								SuchenDialog.this.setSize(dim);
								SuchenDialog.this.setLocation(e.getXOnScreen(),e.getYOnScreen()-dim.height);
								setCursor(cswsize);
								break;
							}
							if(sizeart==5){ //west
								dim.width = (oX > orgbounds[0] ? dim.width-(oX-orgbounds[0]) : dim.width+(orgbounds[0]-oX));
								//dim.height = (oY > orgbounds[1] ? dim.height+(oY-orgbounds[1]) : dim.height-(orgbounds[1]-oY));
								dim.width = (dim.width < 185 ? 185 : dim.width);
								dim.height = (dim.height < 125 ? 125 : dim.height);
								orgbounds[0] = oX;
								orgbounds[1] = oY;
								SuchenDialog.this.setSize(dim);
								SuchenDialog.this.setLocation(e.getXOnScreen(),e.getYOnScreen()-e.getY());
								setCursor(cwsize);
								break;
							}
							if(sizeart==6){ //s�d-ost
								dim.width = (oX > orgbounds[0] ? dim.width+(oX-orgbounds[0]) : dim.width-(orgbounds[0]-oX));
								dim.height = (oY > orgbounds[1] ? dim.height+(oY-orgbounds[1]) : dim.height-(orgbounds[1]-oY));
								dim.width = (dim.width < 185 ? 185 : dim.width);
								dim.height = (dim.height < 125 ? 125 : dim.height);
								orgbounds[0] = oX;
								orgbounds[1] = oY;
								SuchenDialog.this.setSize(dim);
								SuchenDialog.this.setLocation(e.getXOnScreen()-dim.width,e.getYOnScreen()-dim.height);
								setCursor(cwsize);
								break;
							}
							if(sizeart==7){ //s�d
								//dim.width = (oX > orgbounds[0] ? dim.width+(oX-orgbounds[0]) : dim.width-(orgbounds[0]-oX));
								dim.height = (oY > orgbounds[1] ? dim.height+(oY-orgbounds[1]) : dim.height-(orgbounds[1]-oY));
								dim.width = (dim.width < 185 ? 185 : dim.width);
								dim.height = (dim.height < 125 ? 125 : dim.height);
								orgbounds[0] = oX;
								orgbounds[1] = oY;
								SuchenDialog.this.setSize(dim);
								SuchenDialog.this.setLocation(e.getXOnScreen()-e.getX(),e.getYOnScreen()-dim.height);
								setCursor(cssize);
								break;
							}
							if(sizeart==8){ //ost
								dim.width = (oX > orgbounds[0] ? dim.width+(oX-orgbounds[0]) : dim.width-(orgbounds[0]-oX));
								//dim.height = (oY > orgbounds[1] ? dim.height+(oY-orgbounds[1]) : dim.height-(orgbounds[1]-oY));
								dim.width = (dim.width < 185 ? 185 : dim.width);
								dim.height = (dim.height < 125 ? 125 : dim.height);
								orgbounds[0] = oX;
								orgbounds[1] = oY;
								SuchenDialog.this.setSize(dim);
								SuchenDialog.this.setLocation(e.getXOnScreen()-e.getX(),e.getYOnScreen()-e.getY());
								setCursor(cesize);
								break;
							}
							insize = false;
							setCursor(cdefault);
						}
					}else{
						insize = false;
						setCursor(cdefault);
					}
				}
			});

			jXTitledPanel.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
                public void mouseExited(java.awt.event.MouseEvent e) {
				}
				@Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
					clickX = -1;
					clickY = -1;
					orgbounds[0] = -1;
					orgbounds[1] = -1;
					insize = false;
					setCursor(cdefault);
				}
				@Override
                public void mousePressed(java.awt.event.MouseEvent e) {
					if (e.getY() <= 25){
						clickY = e.getY();
						clickX = e.getX();
					}
				}
			});
		}
		return jXTitledPanel;
	}

	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JXButton getJButton() {
		if (jButton == null) {
			jButton = new JXButton();
			jButton.setText("Schliessen");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
					RehaEvent rEvt = new RehaEvent(this);
					rEvt.setRehaEvent("Am Arsch lecken");
					RehaEventClass.fireRehaEvent(rEvt);
					SuchenDialog.this.setVisible(false);

					aufrufer.setLastRow(-1);
					SuchenDialog.this.dispose();
					sucheBeenden();
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setPreferredSize(new Dimension(40, 20));
		}
		return jTextField;
	}
	class suchePatient extends SwingWorker<Void,Void>{

	DefaultTableModel tblDataModel;

	public String ADS_Date(){
		if (!SystemConfig.vDatenBank.get(0).get(2).equals("ADS")){
			return "DATE_FORMAT(geboren,'%d.%m.%Y') AS geboren";
		}else{ //ADS
			return "geboren";
		}
	}

	/**
	 * Erzeugt für jeden, im Suchstring gefundenen, Umlaut einen weiteren Suchstring mit der Umschreibung des Umlautes (u. vice-versa).
	 * Vokale am Ende des Suchstrings werden als möglicher Beginn einer Umlaut-Umschreibung angesehen
	 *
	 * @param fieldname Tabellenspalte, in der gesucht wird
	 * @param val eingegebener Suchbegriff (Anfang des Namens, Vornamens)
	 * @return gibt die ODER-Verknüpfung der Suchstrings zurück (SQL-Syntax)
	 */
	public String SucheKlang(String fieldname, String val){
		ArrayList<String> sSuchPattern = new ArrayList<String>();
		String sTmp="";
		int i;

		sTmp=val.toLowerCase();
		sSuchPattern.add(val.toLowerCase());		// Original merken
		if (systemEinstellungen.SystemConfig.searchExtended()){
			// ---- könnte Ende Teil einer Umlaut-Umschreibung sein?
			if (sTmp.endsWith("a")){sSuchPattern.add(sTmp.concat("e"));} else
			if (sTmp.endsWith("o")){sSuchPattern.add(sTmp.concat("e"));} else
			if (sTmp.endsWith("u")){sSuchPattern.add(sTmp.concat("e"));} else
			if (sTmp.endsWith("s")){sSuchPattern.add(sTmp.concat("s"));} else
			if (sTmp.endsWith("s")){sSuchPattern.add(sTmp.concat("z"));}
			i = sSuchPattern.size();
			// ---- ersetzt Umlaut <-> Umschreibung
			for (int k=0; k<i; k++){
				sTmp=sSuchPattern.get(k);
				if (sTmp.indexOf("ä") >= 0){
					sSuchPattern.add(sTmp.replace("ä", "ae"));	// Umschreibung
					sSuchPattern.add(sTmp.replace("ä", "a"));	// ohne Umlaut geschrieben
				}
				if (sTmp.indexOf("ö") >= 0){
					sSuchPattern.add(sTmp.replace("ö", "oe"));
					sSuchPattern.add(sTmp.replace("ö", "o"));
				}
				if (sTmp.indexOf("ü") >= 0){
					sSuchPattern.add(sTmp.replace("ü", "ue"));
					sSuchPattern.add(sTmp.replace("ü", "u"));
				}
				if (sTmp.indexOf("ß") >= 0){
					sSuchPattern.add(sTmp.replace("ß", "ss"));
					sSuchPattern.add(sTmp.replace("ß", "sz"));
				}
				if (sTmp.indexOf("ae") >= 0){sSuchPattern.add(sTmp.replace("ae", "ä"));}
				if (sTmp.indexOf("oe") >= 0){sSuchPattern.add(sTmp.replace("oe", "ö"));}
				if (sTmp.indexOf("ue") >= 0){sSuchPattern.add(sTmp.replace("ue", "ü"));}
				if (sTmp.indexOf("ss") >= 0){sSuchPattern.add(sTmp.replace("ss", "ß"));}
				if (sTmp.indexOf("sz") >= 0){sSuchPattern.add(sTmp.replace("sz", "ß"));}
			}
		}
		// ---- Suchstring zusammensetzen
		sTmp = "";
		for (String c: sSuchPattern){
			if (sTmp.isEmpty()){
				sTmp = fieldname+" LIKE '"+c+"%'";
			}else{
				sTmp = sTmp+" OR "+fieldname+" LIKE '"+c+"%'";
			}
		}
		
		/*
		// ergibt ein absolut "ungewöhnliches" Ergebnis. 
		for (int i2 = 0; i2 < sSuchPattern.size();i2++){
			if(i2 == 2){
				//steht lediglich ein Vokal was zu einer größeren Menge nicht gewünschter Datensätze führt
				// ... damit fallen die Pat. unter den Tisch, die mit Umlaut in der DB stehen, aber nach Kassenwechsel wieder die Umschreibung auf der GK haben:
				//   st jü -> [jü, jue, ju] => v_name LIKE 'jü%' OR v_name LIKE 'jue%'		<- hier bringt's was
				//   st ju -> [ju, jue, jü] => v_name LIKE 'ju%' OR v_name LIKE 'jue%'		<- hier werden die entscheidenden Treffer ausgeschlossen :-(

				break;
			}
			if (sTmp.isEmpty()){
				sTmp = fieldname+" LIKE '"+sSuchPattern.get(i2)+"%'";
			}else{
				sTmp = sTmp+" OR "+fieldname+" LIKE '"+sSuchPattern.get(i2)+"%'";
			}
		}
		*/		

		//System.out.println("Suchstring: "+sTmp);
		return sTmp;
	}



	private void suchePatienten(){
		Statement stmt = null;
		ResultSet rs = null;
		String sstmt = "", eingabe = "";

		String[] suche = {null};
		setCursor(Cursors.wartenCursor);

		eingabe = jTextField.getText().trim();
		while  (eingabe.contains("  ") ){
			eingabe = eingabe.replace("  ", " ");
		}
		suche = eingabe.split(" ");

		if(suchart == toolBar.getNnVnIdx()){  // "Name Vorname"
			// 2015-08 McM auf Suche nach Umlaut-Umschreibung erweitert
			//         (besser mit tmp-var für Eingabe (dann reicht 1x trim ...))
			if (eingabe.contains(" ") ){
				// sstmt = "Select n_name,v_name,ADS_Date(geboren),pat_intern  from pat5 where n_name LIKE '"+StringTools.Escaped(suche[0].trim()) +"%' AND v_name LIKE '"+StringTools.Escaped(suche[1].trim())+"%' order by n_name,v_name";
				sstmt = "Select n_name,v_name,"+ADS_Date()+",pat_intern  from pat5 where ("+SucheKlang("n_name",suche[0])+") AND ("+SucheKlang("v_name",suche[1])+") order by n_name,v_name";
			}else{
				sstmt = "Select n_name,v_name,"+ADS_Date()+",pat_intern from pat5 where ("+SucheKlang("n_name",suche[0])+") order by n_name,v_name,geboren";
			}

		}else if(suchart == toolBar.getPatIdIdx()){    // "Patienten-ID"
			sstmt = "select n_name,v_name,DATE_FORMAT(geboren,'%d.%m.%Y') AS geboren,pat_intern from pat5 where pat_intern = '"+suche[0]+"' LIMIT 1";
		}else if(suchart == toolBar.getVnNnIdx()){  // "Vorname Name"  (Erweiterung von Drud) + Umlaut-Suche (McM)

			if (eingabe.contains(" ") ){
				sstmt = "Select n_name,v_name,"+ADS_Date()+",pat_intern  from pat5 where ("+SucheKlang("v_name",suche[0])+") AND ("+SucheKlang("n_name",suche[1])+") order by n_name,v_name";
			}else{
				sstmt = "Select n_name,v_name,"+ADS_Date()+",pat_intern from pat5 where ("+SucheKlang("v_name",suche[0])+") order by n_name,v_name,geboren";
			}

		}else if(suchart == toolBar.getTelPIdx()){ // Telfon privat
			sstmt = "select n_name,v_name,concat(DATE_FORMAT(geboren,'%d.%m.%Y'),'-',telefonp) as geboren,pat_intern from pat5 where telefonp LIKE '%"+suche[0]+"%' ORDER BY n_name,v_name,geboren";
		}else if(suchart == toolBar.getTelGIdx()){// Telefon geschäftilich
			sstmt = "select n_name,v_name,concat(DATE_FORMAT(geboren,'%d.%m.%Y'),'-',telefong) as geboren,pat_intern from pat5 where telefong LIKE '%"+suche[0]+"%' ORDER BY n_name,v_name,geboren";
		}else if(suchart == toolBar.getTelMIdx()){// Telefon mobil
			sstmt = "select n_name,v_name,concat(DATE_FORMAT(geboren,'%d.%m.%Y'),'-',telefonm) as geboren,pat_intern from pat5 where telefonm LIKE '%"+suche[0]+"%' ORDER BY n_name,v_name,geboren";
		}else if(suchart == toolBar.getNoteIdx()){ // In Notitzen
			if(suche.length > 1){
				//Umbuen zur oder bzw. und Suche
				if(suche[1].contains("|")){
					sstmt = "select n_name,v_name,DATE_FORMAT(geboren,'%d.%m.%Y') as geboren,pat_intern from pat5 where anamnese LIKE '%"+suche[0]+"%' OR anamnese LIKE '%"+suche[1].replace("|","")+ "%' ORDER BY n_name,v_name,geboren";
				}else{
					sstmt = "select n_name,v_name,DATE_FORMAT(geboren,'%d.%m.%Y') as geboren,pat_intern from pat5 where anamnese LIKE '%"+suche[0]+"%' AND anamnese LIKE '%"+suche[1]+ "%' ORDER BY n_name,v_name,geboren";
				}
			}else{
				sstmt = "select n_name,v_name,DATE_FORMAT(geboren,'%d.%m.%Y') as geboren,pat_intern from pat5 where anamnese LIKE '%"+suche[0]+"%' ORDER BY n_name,v_name,geboren";
			}
		}else if(suchart == toolBar.getAktRezIdx()){    		// Lemmi 20101212: Erweitert um "Nur Patienten mit aktuellen Rezepten"
//			sstmt = "select p.n_name, p.v_name, DATE_FORMAT(p.geboren,'%d.%m.%Y') AS geboren, p.pat_intern, r.rez_nr from pat5 as p INNER JOIN verordn as r ON p.pat_intern = r.pat_intern ORDER BY p.n_name asc, r.rez_nr asc";
			sstmt = "SELECT p.n_name, p.v_name, DATE_FORMAT(p.geboren,'%d.%m.%Y') AS geboren, p.pat_intern, GROUP_CONCAT(r.rez_nr ORDER BY r.rez_nr ASC SEPARATOR ', ') FROM verordn AS r INNER JOIN pat5 AS p where p.pat_intern = r.pat_intern GROUP BY p.pat_intern ORDER BY p.n_name";
		}else{
			return;
		}
		//System.out.println(sstmt);
		try {

			stmt =  Reha.instance.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE );
			try{
				rs = stmt.executeQuery(sstmt);
				Vector<String> rowVector = new Vector<String>();
				int reihen = (suchart == toolBar.getAktRezIdx() ? 5 : 4 );
				while( rs.next()){
					rowVector.clear();
					//for(int i = 1; i <= 4; i++)
					for(int i = 1; i <= reihen ; i++){  // Lemmi 20101212: optional von 4 auf 5 erweitert
						rowVector.addElement(rs.getString(i) != null ? rs.getString(i) : "");
					}
					setzeReihe((Vector<String>)rowVector.clone());
				}

				setCursor(Cursors.normalCursor);

				try{
					if(jtable.getRowCount() > 0 && aufrufer.getLastRow() >=0 && aufrufer.getLastRow() < jtable.getRowCount()){
						jtable.setRowSelectionInterval(aufrufer.getLastRow(), aufrufer.getLastRow());
						jumpok = true;
						jtable.requestFocus();

					}else{
						jumpok = false;
					}
				}catch(Exception ex){
					jumpok = false;
					aufrufer.setLastRow(-1);
					ex.printStackTrace();
				}

			}catch(SQLException ev){
				ev.getMessage();
			}

		}catch(SQLException ex) {
			ex.getMessage();
		}

		finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) { // ignore }
					rs = null;
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) { // ignore }
					stmt = null;
				}
			}

		}
	}
	public void init(DefaultTableModel tblDataModel){
		this.tblDataModel = tblDataModel;
		this.tblDataModel.setRowCount(0);
		execute();
	}
	@Override
	protected Void doInBackground() throws Exception {
		suchePatienten();
		return null;
	}
	}

	private void setDetails(String Event,String PatNummer){
			this.sEventDetails[0] = Event;
			this.sEventDetails[1] = PatNummer;
	}
	public String[] getDetails(String Event,String PatNummer){
			return this.sEventDetails;
	}
	public void fensterSchliessen(){
		this.setVisible(false);
		this.dispose();
	}

}  //  @jve:decl-index=0:visual-constraint="387,36"
