package org.dafa.practitioners.hbq.batchsdcardcopier.services.drive;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

public class OsxDriveEjector extends BaseDriveEjector {

	OsxDriveEjector() {

	}

	@Override
	protected void doEject(File drive) {
		if (drive.getAbsolutePath().toLowerCase().equals("c:\\")) {
			System.out.println("Cannot eject c:\\");
			return;
		}

		System.out.println("Ejecting " + drive.getAbsolutePath());
		try {
			ProcessBuilder pb = new ProcessBuilder().command(
					"diskutil", "unmount", drive.getAbsolutePath()
			);
			Process p = pb.start();
			DataInputStream dis = new DataInputStream(p.getInputStream());
			String line;
			do {
				//noinspection deprecation
				line = dis.readLine();
				if (line != null) {
					System.out.println(line);
				}

			} while (line != null);

			dis.close();

			p.waitFor();
			p.destroy();

			if (p.exitValue() == 0) {
				System.out.println(drive.getAbsolutePath() + " has been ejected.");
			} else {
				System.out.println("Failed to eject " + drive.getAbsolutePath() + ".");
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
