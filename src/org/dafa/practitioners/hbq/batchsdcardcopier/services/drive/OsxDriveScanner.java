package org.dafa.practitioners.hbq.batchsdcardcopier.services.drive;

import java.io.File;
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
//        for (int i = 1; i <= 11; i++) {
//            if (!drives.contains("/Users/hoibq/Downloads/test" + i)) {
//                drives.add("/Users/hoibq/Downloads/test" + i);
//                listener.onDrivePluggedIn(new File("/Users/hoibq/Downloads/test" + i));
//            }
//        }
    }

    private File[] listVolumes() {
        File file = new File("/Volumes");
        return file.listFiles(f -> f.isDirectory() && !f.isHidden());
    }
}
