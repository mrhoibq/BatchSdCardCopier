package org.dafa.practitioners.hbq.batchsdcardcopier;

import java.io.File;
import java.util.Locale;

public final class Utils {

	public enum OS {
		WINDOWS, OSX, LINUX
	}

	public static OS getOS() {
		final String osName = System.getProperty("os.name").toLowerCase(Locale.US);
		if (osName.contains("windows")) {
			return OS.WINDOWS;
		} else if (osName.contains("mac os x")) {
			return OS.OSX;
		} else {
			return OS.LINUX;
		}
	}

	public static boolean isOSX() {
		return getOS() == OS.OSX;
	}

	public static void deleteDir(final File dir) {
		if (dir == null || !dir.exists()) {
			return;
		}

		if (dir.isFile()) {
			//noinspection ResultOfMethodCallIgnored
			dir.delete();
			return;
		}

		File[] files = dir.listFiles();
		if (files != null && files.length > 0) {
			for (File file : files) {
				deleteDir(file);
			}
		}

		//noinspection ResultOfMethodCallIgnored
		dir.delete();
	}

	public static void deleteDirContent(final File dir) {
		if (dir == null || !dir.exists()) {
			return;
		}

		if (dir.isFile()) {
			return;
		}

		File[] files = dir.listFiles();
		if (files != null && files.length > 0) {
			for (File file : files) {
				deleteDir(file);
			}
		}
	}

	public static void deleteHiddenFiles(final File dir) {
		if (dir == null || !dir.exists()) {
			return;
		}

		if (dir.isFile()) {
			return;
		}

		File[] files = dir.listFiles();
		if (files != null && files.length > 0) {
			for (File file : files) {
				if (file.isHidden()) {
					deleteDir(file);
				}
			}
		}
	}
}
