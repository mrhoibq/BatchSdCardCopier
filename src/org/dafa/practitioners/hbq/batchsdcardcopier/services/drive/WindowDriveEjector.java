package org.dafa.practitioners.hbq.batchsdcardcopier.services.drive;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

public class WindowDriveEjector extends BaseDriveEjector {

    WindowDriveEjector() {

    }

    @Override
    protected void doEject(File drive) {
        if (drive.getAbsolutePath().toLowerCase().equals("c:\\")) {
            System.out.println("Cannot eject c:\\");
            return;
        }

        System.out.println("Ejecting " + drive.getAbsolutePath());
        try {
            Process p = Runtime.getRuntime().exec(
                "tools/removedrive/x64/RemoveDrive.exe " + drive.getAbsolutePath()
            );
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

            System.out.println(drive.getAbsolutePath() + " has been ejected.");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
