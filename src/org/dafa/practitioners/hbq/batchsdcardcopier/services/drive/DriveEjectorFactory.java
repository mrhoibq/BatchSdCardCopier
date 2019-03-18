package org.dafa.practitioners.hbq.batchsdcardcopier.services.drive;

import org.dafa.practitioners.hbq.batchsdcardcopier.Utils;

import static org.dafa.practitioners.hbq.batchsdcardcopier.Utils.OS;

public class DriveEjectorFactory {

	public DriveEjector createDriveEjector() {
		OS os = Utils.getOS();
		if (os == OS.WINDOWS) {
			return new WindowDriveEjector();
		} else if (os == OS.OSX) {
			return new OsxDriveEjector();
		}
		return null;
	}
}
