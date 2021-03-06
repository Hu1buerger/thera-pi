package CommonTools;

import java.util.EventObject;

public class RehaEvent extends EventObject {

    private String sRehaEvent = "";
    private String[] sDetails = { "", "" };

    private static final long serialVersionUID = 1L;

    public static final String ERROR_EVENT = "REHA_ERROR";

    public RehaEvent(Object source) {
        super(source);
        this.source = source;
    }

    public void setRehaEvent(String sRehaEvent) {
        this.sRehaEvent = sRehaEvent;

    }

    public String getRehaEvent() {
        return sRehaEvent;

    }

    public void setDetails(String sEvent, String sKommando) {
        sDetails[0] = sEvent;
        sDetails[1] = sKommando;
    }

    public String[] getDetails() {
        return sDetails;
    }
}
