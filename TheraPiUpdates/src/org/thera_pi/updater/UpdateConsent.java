package org.thera_pi.updater;

import java.util.Objects;

public class UpdateConsent {
    private final boolean download;
    private final boolean update;

    public UpdateConsent(boolean download, boolean update) {
        this.download = download;
        this.update = update;
    }

    boolean allowsDownload() {
        return download;
    }

     boolean allowsInstall() {
        return update;
    }

    @Override
    public int hashCode() {
        return Objects.hash(download, update);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UpdateConsent other = (UpdateConsent) obj;
        return download == other.download && update == other.update;
    }

    @Override
    public String toString() {
        String result = download ? "herunterladen" : "";
        result += update? " und aktualisieren":"";

        return result.isEmpty()? "nix machen" : result;
    }


}
