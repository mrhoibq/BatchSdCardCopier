package org.dafa.practitioners.hbq.batchsdcardcopier.services.drive;

import java.io.File;

public abstract class BaseDriveEjector implements DriveEjector {

	private boolean isSyncModeEnabled = false;

	@Override
	public void setSyncModeEnabled(boolean enabled) {
		isSyncModeEnabled = enabled;
	}

	@Override
	public void eject(File drive) {
		if (isSyncModeEnabled) {
			doEject(drive);
		} else {
			new Thread(() -> doEject(drive)).start();
		}
	}

	protected abstract void doEject(File drive);
}
