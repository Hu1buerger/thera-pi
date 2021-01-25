package therapi.updatehint;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.ini4j.Ini;
import org.thera_pi.updater.HTTPRepository;
import org.thera_pi.updater.Version;
import org.thera_pi.updater.VersionsSieb;

import CommonTools.ini.INIFile;
import environment.Path;

public class Hint {

    public boolean isEnabled() {
        File iniFile = new File(Path.Instance.getProghome() + "ini" + File.separator + "tpupdateneu.ini");
        try {
            if (iniFile.exists()) {

                Ini updatesIni = new Ini(iniFile);
                String updateSetting = updatesIni.get("TheraPiUpdates", "UpdateChecken");
                return INIFile.booleanValueOf(updateSetting);

            } else {
                return true;
            }
        } catch (Exception e) {
            return true;

        }
    }

    public List<File> ladeUpdateFiles() {
        updatefiles = new VersionsSieb(new Version()).select(new HTTPRepository().filesList());
        return updatefiles;
    }

    public boolean updatesVorhanden() {
        return !updatefiles.isEmpty();

    }

    private List<File> updatefiles = Collections.emptyList();

    public File fileToDownload() {
        if (updatesVorhanden())
            return updatefiles.get(0);
        else
            throw new IllegalStateException("no Updates available");
    };

}
