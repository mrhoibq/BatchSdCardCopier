package org.dafa.practitioners.hbq.batchsdcardcopier.services.drive;

import java.io.File;

public class OsxDriveEjector extends BaseDriveEjector {

    OsxDriveEjector() {

    }

    @Override
    public void doEject(File drive) {
        // TODO code
        System.out.println(drive.getAbsolutePath() + " has been ejected.");
    }
}
