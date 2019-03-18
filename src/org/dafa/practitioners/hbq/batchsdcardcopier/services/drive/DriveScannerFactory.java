package org.dafa.practitioners.hbq.batchsdcardcopier.services.drive;

import org.dafa.practitioners.hbq.batchsdcardcopier.Utils;

import static org.dafa.practitioners.hbq.batchsdcardcopier.Utils.OS;

public class DriveScannerFactory {

	public DriveScanner createDriveScanner() {
		OS os = Utils.getOS();
		if (os == OS.OSX) {
			return new OsxDriveScanner();
		} else if (os == OS.LINUX) {
			return new LinuxDriveScanner();
		} else {
			return new WindowDriveScanner();
		}
	}
}
