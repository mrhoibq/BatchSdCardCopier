package org.dafa.practitioners.hbq.batchsdcardcopier.services.drive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;

public class OsxDriveScanner extends BaseDriveScanner {

	private ArrayList<String> drives = new ArrayList<>();

	OsxDriveScanner() {

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
		for (File root : listVolumes()) {
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
		File file = mountRoot != null ? mountRoot : new File("/Volumes");
		return file.listFiles(this::filter);
	}

	private boolean filter(File f) {
		if (!f.isDirectory() || f.isHidden() || !f.canWrite()) {
			return false;
		}

		FileOwnerAttributeView foav = Files.getFileAttributeView(f.toPath(),
				FileOwnerAttributeView.class);
		try {
			UserPrincipal owner = foav.getOwner();
			boolean isRoot = owner.getName().equals("root");
			if (isRoot) {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}
}
