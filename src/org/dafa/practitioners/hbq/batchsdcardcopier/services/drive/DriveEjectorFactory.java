package org.dafa.practitioners.hbq.batchsdcardcopier.services.drive;

import org.dafa.practitioners.hbq.batchsdcardcopier.Utils;

import static org.dafa.practitioners.hbq.batchsdcardcopier.Utils.OS;

public class DriveEjectorFactory {

    public DriveEjector createDriveEjector() {
        OS os = Utils.getOS();
        if (os == OS.OSX) {
            return new OsxDriveEjector();
        } else {
            return new WindowDriveEjector();
        }
    }
}
