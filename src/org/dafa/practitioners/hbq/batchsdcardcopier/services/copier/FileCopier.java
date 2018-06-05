package org.dafa.practitioners.hbq.batchsdcardcopier.services.copier;

import java.io.*;

public class FileCopier {

    private static final int COPY_BUFFER_LENGTH = 512 * 1024;

    private File sourceFile;
    private File destinationFile;
    private volatile boolean cancelled = false;

    private ProgressListener progressListener;

    public FileCopier(File source, File dest) {
        sourceFile = source;
        destinationFile = dest;
    }

    public void start() {
        if (sourceFile == null || !sourceFile.exists()) {
            if (progressListener != null) {
                progressListener.onFileError(sourceFile, new RuntimeException("Source file is null or does not exist!"));
            }
            return;
        }
        if (destinationFile == null) {
            if (progressListener != null) {
                progressListener.onFileError(sourceFile, new RuntimeException("DestinationFile file is null!"));
            }
            return;
        }

        FileInputStream fis;
        try {
            fis = new FileInputStream(sourceFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (progressListener != null) {
                progressListener.onFileError(sourceFile, e);
            }
            return;
        }

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(destinationFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (progressListener != null) {
                progressListener.onFileError(sourceFile, e);
            }
            closeStream(fis);
            return;
        }

        try {
            if (progressListener != null) {
                progressListener.onFileStarted(sourceFile);
            }

            long copied = 0;
            final long total = sourceFile.length();

            byte[] buf = new byte[COPY_BUFFER_LENGTH];
            int len;
            while ((len = fis.read(buf)) > 0) {
                if (cancelled) {
                    if (progressListener != null) {
                        progressListener.onFileCancelled(sourceFile);
                    }
                    break;
                }
                fos.write(buf, 0, len);

                copied += len;
                if (progressListener != null && !cancelled) {
                    progressListener.onFileProgressUpdated(sourceFile, copied, total);
                }
            }

            if (progressListener != null && !cancelled) {
                progressListener.onFileFinished(sourceFile);
            }

        } catch (IOException e) {
            e.printStackTrace();
            if (progressListener != null) {
                progressListener.onFileError(sourceFile, e);
            }

        } finally {
            closeStream(fis);
            closeStream(fos);
        }

    }

    public void cancel() {
        cancelled = true;
    }

    private void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignore) {

            }
        }
    }

    public void setProgressListener(ProgressListener listener) {
        progressListener = listener;
    }

    public interface ProgressListener {
        void onFileStarted(File file);
        void onFileProgressUpdated(File file, long copied, long total);
        void onFileFinished(File file);
        void onFileError(File file, Throwable error);
        void onFileCancelled(File file);
    }
}
