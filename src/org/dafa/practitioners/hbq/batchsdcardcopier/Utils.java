package org.dafa.practitioners.hbq.batchsdcardcopier;

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
}
