package office;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ag.ion.bion.officelayer.application.ILazyApplicationInfo;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.application.OfficeApplicationRuntime;
import ag.ion.bion.officelayer.document.DocumentException;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.event.ITerminateEvent;
import ag.ion.bion.officelayer.event.VetoTerminateListener;
import ag.ion.bion.officelayer.runtime.IOfficeProgressMonitor;

public class OOService {
    final static CountDownLatch latch = new CountDownLatch(1);

    private static IOfficeApplication officeapplication;

    public IOfficeApplication getOfficeapplication() {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return officeapplication;
    }

    public IOfficeApplication start(String libPath, String ooPath)
            throws OfficeApplicationException, FileNotFoundException {
        Map<String, Object> config = loadConfiguration(libPath, ooPath);
        System.setProperty(IOfficeApplication.NOA_NATIVE_LIB_PATH, libPath);
        officeapplication = activate(config);
        System.out.println("activation complete");
        return officeapplication;
    }

    private IOfficeApplication activate(Map<String, Object> config) throws OfficeApplicationException {
        IOfficeApplication officeapplication2 = OfficeApplicationRuntime.getApplication(config);

        try {
            officeapplication2.activate(new OOServiceMonitor());
            officeapplication2.getDesktopService()
                              .addTerminateListener(new VetoTerminateListenerExtension());

        } catch (NullPointerException | OfficeApplicationException e) {
            e.printStackTrace();
        }

        return officeapplication2;
    }

    private Map<String, Object> loadConfiguration(String libPath, String ooPath)
            throws OfficeApplicationException, FileNotFoundException {
        File file = new File(ooPath);
        if (!file.exists()) {
            throw new FileNotFoundException("OpenOffice path \""
                                  + file.getAbsolutePath() + "\" does not exist");
        }
        ILazyApplicationInfo info = OfficeApplicationRuntime.getApplicationAssistant(libPath)
                                                            .findLocalApplicationInfo(ooPath);
        if ( info == null ) throw new FileNotFoundException(
                                                    "Couldn't find OO-App-Info in " + ooPath);
        String[] names = info.getProperties().getPropertyNames();
        boolean isLibreOffice = false;
        for (int i = 0; i < names.length; i++) {
            System.out.println(names[i] + " = " + info.getProperties()
                                                      .getPropertyValue(names[i]));
            if (info.getProperties()
                    .getPropertyValue(names[i])
                    .contains("LibreOffice")) {
                isLibreOffice = true;
            }
        }
        Map<String, Object> config = new HashMap<String, Object>();
        config.put(IOfficeApplication.APPLICATION_HOME_KEY, ooPath);
        config.put(IOfficeApplication.APPLICATION_TYPE_KEY, IOfficeApplication.LOCAL_APPLICATION);
        if (isLibreOffice) {
            config.put(IOfficeApplication.APPLICATION_ARGUMENTS_KEY, new String[] { "--nodefault", "--nologo",
                    "--nofirststartwizard", "--nocrashreport", "--norestore" });

        } else {
            config.put(IOfficeApplication.APPLICATION_ARGUMENTS_KEY,
                    new String[] { "-nodefault", "-nologo", "-nofirststartwizard", "-nocrashreport", "-norestore" });

        }
        return config;
    }

    private final class VetoTerminateListenerExtension extends VetoTerminateListener {

        @Override
        public void queryTermination(ITerminateEvent terminateEvent) {
            super.queryTermination(terminateEvent);
            IDocument[] docs = null;
            try {
                docs = getOfficeapplication().getDocumentService()
                                             .getCurrentDocuments();
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (OfficeApplicationException e) {
                e.printStackTrace();
            }
            if (docs.length == 1) {
                docs[0].close();
            }
        }
    }

    private final class OOServiceMonitor implements IOfficeProgressMonitor {
        @Override
        public void worked(int arg0) {
        }

        @Override
        public void setCanceled(boolean arg0) {
        }

        @Override
        public boolean needsDone() {
            return true;
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public void done() {
            OOService.latch.countDown();
        }

        @Override
        public void beginTask(String arg0, int arg1) {
        }

        @Override
        public void beginSubTask(String arg0) {
        }
    }
}
