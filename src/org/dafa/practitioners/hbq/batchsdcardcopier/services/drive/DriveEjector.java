package org.dafa.practitioners.hbq.batchsdcardcopier.services.drive;

import java.io.File;

public interface DriveEjector {
    void setSyncModeEnabled(boolean enabled);
    void eject(File drive);
}
