package org.dafa.practitioners.hbq.batchsdcardcopier.services.copier;

import java.io.File;

public interface DirectoryCopyListener {
    void onStarted(File targetDirectory);
    void onFileStarted(File targetDirectory, File file);
    void onProgressUpdated(File targetDirectory, File file, int fileProgress, int totalProgress);
    void onFileFinished(File targetDirectory, File file);
    void onFileError(File targetDirectory, File file, Throwable error);
    void onCancelled(File targetDirectory);
    void onFinished(File targetDirectory);
}
