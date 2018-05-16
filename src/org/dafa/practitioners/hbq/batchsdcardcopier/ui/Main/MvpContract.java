package org.dafa.practitioners.hbq.batchsdcardcopier.ui.Main;

import javafx.collections.ObservableList;
import org.dafa.practitioners.hbq.batchsdcardcopier.mvpbase.MvpPresenter;
import org.dafa.practitioners.hbq.batchsdcardcopier.mvpbase.MvpView;

import java.io.File;

final class MvpContract {

    interface View extends MvpView {
        void addDrive(String absolutePath);
        void removeDrive(String absolutePath);
        String getSourceDirectory();
        void showError(String message);
        ObservableList<String> getTargetDrives();
        void close();
        void reportFileStartedToCopy(File targetDirectory, File file);
        void reportCopyProgressUpdated(File targetDirectory, File file, int fileProgress, int totalProgress);
        void reportFileCopyingFinished(File targetDirectory, File file);
        void reportDirectoryCopyingCancelled(File targetDirectory);
        void reportDirectoryCopyingFinished(File targetDirectory);
        void reportAllCopyingFinished(long totalTime);
        void reportDirectoryCopingStarted(File targetDirectory);
        boolean isEjectDriveOnFinishEnabled();
        void reportAllCopyingCancelled();
        void setStartManualCopyButtonEnabled(boolean enabled);
        void setStartAutoCopyModeButtonText(String text);
        void reportDirectoryCopyingError(File targetDirectory, Throwable error);
        void setSourceDirectorySelectionEnabled(boolean enabled);
    }

    interface Presenter extends MvpPresenter<View> {
        void doCopy();
        void cancelAndExit();
        void cancelCopying();
        void toggleAutoCopyMode();
        void ejectSelectedDrives();
    }

}
