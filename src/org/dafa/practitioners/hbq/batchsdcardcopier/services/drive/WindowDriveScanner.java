package org.dafa.practitioners.hbq.batchsdcardcopier.services.drive;

import java.io.File;
import java.util.ArrayList;

public class WindowDriveScanner extends BaseDriveScanner {

	private ArrayList<String> drives = new ArrayList<>();

	WindowDriveScanner() {

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
		// Check for newly plugged in drives:
		for (File root : File.listRoots()) {
			final String rootPath = root.getAbsolutePath();
			if (!drives.contains(rootPath)) {
				System.out.println("New drive has been plugged in: " + rootPath);
				drives.add(rootPath);
				listener.onDrivePluggedIn(root);
			}
		}
		// Remove recently unplugged drives:
		for (int n = drives.size() - 1, i = n; i >= 0; i--) {
			File drive = new File(drives.get(i));
			if (!drive.exists()) {
				System.out.println("Drive has been unplugged: " + drive.getAbsolutePath());
				drives.remove(i);
				listener.onDriveUnplugged(drive);
			}
		}

	}
}
