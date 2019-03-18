package org.dafa.practitioners.hbq.batchsdcardcopier.services.drive;

import java.io.File;
import java.util.ArrayList;

public class LinuxDriveScanner extends BaseDriveScanner {

	private ArrayList<String> drives = new ArrayList<>();

	LinuxDriveScanner() {

	}

	@Override
	protected void onStarted() {
		super.onStarted();
		drives.clear();
	}

	@Override
	protected void onStopped() {
		super.onStopped();
		drives.clear();
	}

	@Override
	protected void scan(DrivePlugListener listener) {
		File[] volumes = listVolumes();
		if (volumes == null) {
			return;
		}

		// Check for newly plugged in drives:
		for (File root : volumes) {
			final String rootPath = root.getAbsolutePath();
			if (!drives.contains(rootPath)) {
				drives.add(rootPath);
				listener.onDrivePluggedIn(root);
			}
		}
		// Remove recently unplugged drives:
		for (int n = drives.size() - 1, i = n; i >= 0; i--) {
			File drive = new File(drives.get(i));
			if (!drive.exists()) {
				drives.remove(i);
				listener.onDriveUnplugged(drive);
			}
		}
	}

	private File[] listVolumes() {
		if (mountRoot == null) {
			return null;
		}
		return mountRoot.listFiles(f -> f.isDirectory() && !f.isHidden() && f.canWrite());
	}
}
