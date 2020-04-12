package environment;

import java.io.File;

/**
 * Tries to guess the programs execution home based on the OS it is running on
 * and defaults to c:/RehaVerwaltung/
 *
 * This guessing is at the moment (sep.2018) scattered through the code. This
 * class is probably temporary until the program does finding its files how it
 * should
 *
 */
public enum Path {
    Instance;

    private static final String C_REHA_VERWALTUNG = "C:/RehaVerwaltung/";
    String proghome = "";
    OS currentOS = OS.WIN;

    public String getProghome() {
        return proghome;
    }

    public boolean isLinux() {
        return currentOS.is(OS.LINUX);
    }

    public boolean isWindows() {
        return currentOS.is(OS.WIN);
    }

    private void setProghome(String proghome) {
        this.proghome = proghome;
    }

    Path() {
        currentOS = determineOS();

        switch (currentOS) {
        case WIN:
            String prog = java.lang.System.getProperty("user.dir");
            String laufwerk = prog.substring(0, 2);
            setProghome(laufwerk + "/RehaVerwaltung/");
            break;
        case LINUX:
            setProghome("/opt/RehaVerwaltung/");
            break;
        case MAC:
            setProghome("/opt/RehaVerwaltung/");
            /**
             * Sollte erstmal tun, bis eine echte 'OS-X' app oder installer
             * gebaut wird.
             **/
            break;
        case UNKNOWN:
            setProghome(C_REHA_VERWALTUNG);
            break;
        default:
            System.out.println("setting Directory to default");
            setProghome(C_REHA_VERWALTUNG);
            break;
        }
        if (!new File(getProghome()).exists()) {
            String prog = java.lang.System.getProperty("user.dir");
            String path = prog.replace("Reha", "dist") +"\\";
            if (new File(path).exists()) {
                setProghome(path);
            } else
            // program wasn't started from within its installation directory, probably
            // developer.
            // we assume standardpath until this mess is fixed
            setProghome(C_REHA_VERWALTUNG);

        }
        System.out.println("Programmverzeichnis = " + getProghome());
    }

    private OS determineOS() {
        String osVersion = System.getProperty("os.name");

        if (osVersion.contains("Linux")) {
            return OS.LINUX;
        } else if (osVersion.contains("Windows")) {
            return OS.WIN;
        } else if (osVersion.contains("Mac OS X")) {
            return OS.MAC;
        }
        // this should not happen
        return OS.UNKNOWN;

    }
}
