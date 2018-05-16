package org.dafa.practitioners.hbq.batchsdcardcopier.services.drive;

import java.io.File;

public interface DriveScanner {
	void start(DrivePlugListener listener);
	void stop();
	
	interface DrivePlugListener {
		void onDrivePluggedIn(File drive);
		void onDriveUnplugged(File drive);
	}
}
