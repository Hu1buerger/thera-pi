package hauptFenster;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.therapi.updater.Version;

import CommonTools.SqlInfo;
import office.OOService;

public class VersionInformer {
	
	public VersionInformer() {
		String javaVersion = getJavaVersion();
		String openOfficeVersion = getOOVersion();
		String osVersion = getOsVersion();
		String sqlVersion = getSqlVersion();
		String therapiVersion = getTherapiVersion();
		
		System.out.println("Java Version:  "+javaVersion);
		System.out.println("OpenOffice Version:  "+openOfficeVersion);
		System.out.println("OS Version:  "+osVersion);
		System.out.println("SQL Version:  "+sqlVersion);
		System.out.println("Thera-Pi Version:  "+therapiVersion);
		
		JTextArea textArea = new JTextArea();
		textArea.setColumns(30);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.append("Java Version:  "+javaVersion+"\r\n");
		textArea.append("OpenOffice Version:  "+openOfficeVersion+"\r\n");
		textArea.append("OS Version:  "+osVersion+"\r\n");
		textArea.append("SQL Version:  "+sqlVersion+"\r\n");
		textArea.append("Thera-Pi Version:  "+therapiVersion+"\r\n");
		textArea.setSize(textArea.getPreferredSize().width, 3);
		JOptionPane.showMessageDialog(null, new JScrollPane(textArea), "Installierte Versionen", JOptionPane.WARNING_MESSAGE);
	}
	
	private String getJavaVersion() {
		return (String)System.getProperty("java.vendor") + " " + (String)System.getProperty("java.version");
	}
	
	private String getOsVersion() {
		return System.getProperty("os.arch") + " " + System.getProperty("os.name");
	}
	
	private String getTherapiVersion() {
		return new Version().number();
	}
	
	private String getSqlVersion() {
		return SqlInfo.holeEinzelFeld("SELECT Version()");
	}
	
	private String getOOVersion() {
		String version = "nicht vefügbar";
		try {
			version = new OOService().getOfficeapplication().getApplicationInfo().getVersion();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return version;
	}

}
