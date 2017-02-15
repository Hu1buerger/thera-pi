package systemEinstellungen;

import gui.Cursors;
import hauptFenster.Reha;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import jxTableTools.TableTool;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;

import CommonTools.INIFile;
import CommonTools.INITool;
import CommonTools.JCompTools;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SysUtilVorlagen extends JXPanel implements ActionListener, SysInitCommon_If {
	private JXTable vorlagen;
	private MyVorlagenTableModel modvorl;
	private SysInitCommon_If ownedBy;
	private String vorlagenPfad = null;
	private String iniPfad = null;
	private String iniName = null;
	private INIFile inif = null;
	private boolean formOK = true;
	private String nbOfEntriesLabel = null;
	private String entryTextLabel = null;
	private String entryNameLabel = null;
	private String sectionLabel = null;

	public SysUtilVorlagen(SysInitCommon_If owner) {
		this.ownedBy = owner;

		modvorl = new MyVorlagenTableModel();
		modvorl.setColumnIdentifiers(new String[] {"Titel der Vorlage","Vorlagendatei"});
		vorlagen = new JXTable(modvorl);
		vorlagen.getColumn(0).setCellEditor(new TitelEditor());
		vorlagen.setSortable(false);
	}

	public JXTable getTable() {
		return vorlagen;
	}

	public JPanel getPanel() {
		//                                      1.             2.     3.     4.     5.     6.    7. 
		FormLayout lay = new FormLayout("right:max(120dlu;p), 20dlu:g, 15dlu",
       //1.  2.     3.     4.   
		"p, 10dlu, 80dlu, 2dlu, p,   0dlu , 0dlu, 0dlu, 0dlu, 0dlu, 0dlu");
		
		PanelBuilder builder = new PanelBuilder(lay);
		//PanelBuilder builder = new PanelBuilder(lay, new FormDebugPanel());		// debug mode
		//builder.setDefaultDialogBorder();			// no borders!
		builder.getPanel().setOpaque(false);
		CellConstraints cc = new CellConstraints();

		builder.addSeparator("Vorlagen-Verwaltung", cc.xyw(1, 1, 2));
		JScrollPane jscrPane = JCompTools.getTransparentScrollPane(vorlagen);
		jscrPane.getVerticalScrollBar().setUnitIncrement(15);
		jscrPane.validate();
		builder.add(jscrPane, cc.xyw(1,3,2));

		AddOrRemove addRemoveButtons = new AddOrRemove(this,1);
		builder.add(addRemoveButtons.getPanel(), cc.xyw(1, 5, 2));

		//return vorlagen;
		return builder.getPanel();
	}

	public void setVPfad(String vorlagenPfad) {
		this.vorlagenPfad = vorlagenPfad;
		
	}
	public void setIni(String iniPfad,String iniName) {
		this.iniPfad = iniPfad;
		if (!iniPfad.endsWith("/")){
			this.iniPfad = this.iniPfad+"/";
		}
		this.iniName = iniName;
	}

	public INIFile getInif() {
		return inif;
	}

	private void setInif(INIFile inif) {
		this.inif = inif;
	}

	public void activateEditing() {
		vorlagen.addMouseListener(new MouseAdapter(){		
			public void mouseClicked(MouseEvent arg0) {
				if(arg0.getClickCount()==2 && arg0.getButton()==1){
					int row = vorlagen.getSelectedRow();
					row = vorlagen.convertRowIndexToModel(row);
					int col = vorlagen.getSelectedColumn();	
					if(col==1){
						Reha.getThisFrame().setCursor(Cursors.wartenCursor);
						String svorlage = dateiDialog(vorlagenPfad);
						if(svorlage.equals("")){
							return;
						}
						modvorl.setValueAt(svorlage, row, col);
						vorlagen.validate();
					}
				}
			}	
		});
	}

	public MyVorlagenTableModel getModvorl() {
		return modvorl;
	}

	public void setModvorl(MyVorlagenTableModel modvorl) {
		this.modvorl = modvorl;
	}

	private String dateiDialog(String pfad){
		String sret = "";
		final JFileChooser chooser = new JFileChooser("Verzeichnis wählen");
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        final File file = new File(pfad);

        chooser.setCurrentDirectory(file);

        chooser.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)
                        || e.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                    //final File f = (File) e.getNewValue();
                }
            }

        });
        chooser.setVisible(true);
        Reha.getThisFrame().setCursor(Cursors.normalCursor);
        final int result = chooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File inputVerzFile = chooser.getSelectedFile();

            if(inputVerzFile.getName().trim().equals("")){
            	sret = "";
            }else{
            	sret = inputVerzFile.getName().trim();	
            }
        }else{
        	sret = ""; //vorlagenname.setText(SystemConfig.oTerminListe.NameTemplate);
        }
        chooser.setVisible(false); 

        return sret;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		for(int i = 0;i < 1;i++){
			if(cmd.equals("entfernenvorlage")){
				loescheVorlage();
				break;
			}
			if(cmd.equals("neuvorlagen")){
				addVorlage();
				break;
			}
		}
	}
	public void addVorlage() {
		Reha.getThisFrame().setCursor(Cursors.wartenCursor);
		if(vorlagenPfad.equals("")){
			return;
		}
		String svorlage = dateiDialog(vorlagenPfad);
		if(svorlage.equals("")){
			return;
		}

		Vector<String> vec = new Vector<String>();
		vec.add("");
		vec.add(svorlage);
		modvorl.addRow((Vector<?>)vec.clone());
		vorlagen.validate();
		int rows = modvorl.getRowCount(); 
		final int xrows = rows -1;
		SwingUtilities.invokeLater(new Runnable(){
		 	   public  void run(){
		 		  vorlagen.requestFocus();
		 		  vorlagen.setRowSelectionInterval(xrows, xrows);
		 		  startCellEditing(vorlagen,xrows);
		 	   }
		});
	}
	
	public void loescheVorlage() {
		int row = vorlagen.getSelectedRow();
		int frage = JOptionPane.showConfirmDialog(null, "Wollen Sie die ausgewählte Tabellenzeile wirklich löschen?", "Wichtige Benutzeranfrage", JOptionPane.YES_NO_OPTION);
		if(frage == JOptionPane.NO_OPTION){
			return;
		}
		if(row >=0){
			TableTool.loescheRow(vorlagen, row);
		}
	}

		private void startCellEditing(JXTable table,int row){
		final int xrows = row;
		final JXTable xtable = table;
		SwingUtilities.invokeLater(new Runnable(){
		 	   public  void run(){
		 		  xtable.scrollRowToVisible(xrows);
		 				xtable.editCellAt(xrows, 0);
		 	   }
		});
	}

	public void readFromIni() {
		if (getInif() == null){
			//System.out.println("SysUtilVorlagen: inifile not set");
			setInif(INITool.openIni(iniPfad, iniName));
		}
//		int forms = inif.getIntegerProperty("Formulare", "ArztFormulareAnzahl");
		int forms = getInif().getIntegerProperty(sectionLabel, nbOfEntriesLabel);
		Vector<String> vec = new Vector<String>();
		for(int i = 1; i <= forms; i++){
			vec.clear();
//			vec.add(inif.getStringProperty("Formulare","AFormularText"+i));
//			vec.add(inif.getStringProperty("Formulare","AFormularName"+i));
			vec.add(getInif().getStringProperty(sectionLabel,entryTextLabel+i));
			vec.add(getInif().getStringProperty(sectionLabel,entryNameLabel+i));
			modvorl.addRow((Vector<?>)vec.clone());
		}
		if(modvorl.getRowCount() > 0){
			vorlagen.setRowSelectionInterval(0, 0);
		}
		vorlagen.validate();
	}

	public boolean saveToIni() {
		if (getInif() == null){
			System.out.println("SysUtilVorlagen:saveToIni: inifile not set");
			//inif = INITool.openIni(iniPfad, iniName);
		}
		int rows = vorlagen.getRowCount();
		
		boolean formok = true;
		for(int i = 0;i<rows;i++){
			String test = (String)vorlagen.getValueAt(i, 0);
			if(test.equals("")){
				String datei = (String)vorlagen.getValueAt(i, 1);
				String msg = "Für Vorlagendatei "+datei+" wurde kein Titel eingegeben!\nDie Vorlagen werden nicht(!!!) gespeichert.";
				JOptionPane.showMessageDialog(null,msg);
				formok = false;
				break;
			}else{
				formok = true;
				getInif().setStringProperty(sectionLabel, nbOfEntriesLabel,Integer.valueOf(rows).toString() , null);				
			}
		}
		if(formok){
			for(int i = 0;i<rows;i++){
				getInif().setStringProperty(sectionLabel, entryTextLabel+(i+1),(String)vorlagen.getValueAt(i, 0) , null);
				getInif().setStringProperty(sectionLabel, entryNameLabel+(i+1),(String)vorlagen.getValueAt(i, 1) , null);
			}
		}
		return formok;
	}

	boolean isFormOK() {
		return formOK;
	}

	public void setLabels(String sectionLabel, String nbOfEntriesLabel, String entryBase) {
		this.nbOfEntriesLabel = nbOfEntriesLabel;
		this.entryNameLabel = entryBase+"Name";
		this.entryTextLabel = entryBase+"Text";
		this.sectionLabel = sectionLabel;
	}

	public int getSelectedRow() {
		return vorlagen.getSelectedRow();
	}

	@Override
	public void Abbruch() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Speichern() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void AddEntry(int instance) {
		addVorlage();
	}
	@Override
	public void RemoveEntry(int instance) {
		loescheVorlage();
	}

	/**********************************************/
	class TitelEditor extends AbstractCellEditor implements TableCellEditor{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Object value;
		JComponent component = new JFormattedTextField();
	   public TitelEditor(){
		   //component = new JRtaTextField("NIX",true);
		   //System.out.println("editor-Component wurde initialisiert");
		   component.addKeyListener(new KeyAdapter(){
			   public void keyPressed(KeyEvent arg0) {
					//System.out.println("********Button in KeyPressed*********");	
					if(arg0.getKeyCode()== 10){
						arg0.consume();
						stopCellEditing();
					}
			   }
		   });
	    }
		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			((JFormattedTextField)component).setText((String)value);
			((JFormattedTextField)component).setCaretPosition(0);
			return component;
		}

		@Override
		public Object getCellEditorValue() {
			return  ((JFormattedTextField)component).getText();
		}


		public boolean isCellEditable(EventObject anEvent) {
			if(anEvent instanceof MouseEvent)
	          {
	             MouseEvent me = (MouseEvent)anEvent;
	             if(me.getClickCount() != 2){
	            	 return false;
	             }
	          }
			//System.out.println("isCellEditable");
			return true;
		}


		@Override
		public boolean shouldSelectCell(EventObject anEvent) {
			//System.out.println("in schouldCellSelect"+anEvent);
			return super.shouldSelectCell(anEvent);
		}

		@Override
		public boolean stopCellEditing() {
			value = ((JFormattedTextField) component).getText();
			//System.out.println("in stopCellediting");
			super.stopCellEditing();
			return true;
		}
		public boolean startCellEditing() {
	        return false;//super.startCellEditing();//false;
		}
	}

	class MyVorlagenTableModel extends DefaultTableModel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Class<?> getColumnClass(int columnIndex) {
			   return String.class;
	       }

	    public boolean isCellEditable(int row, int col) {
	        if (col == 0){
	        	return true;
	        }else{
	        	return false;
	        }
	    }
		   
	}

}