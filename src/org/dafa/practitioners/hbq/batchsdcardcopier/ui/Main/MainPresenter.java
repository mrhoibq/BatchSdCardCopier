package org.dafa.practitioners.hbq.batchsdcardcopier.ui.Main;

import javafx.collections.ObservableList;
import org.dafa.practitioners.hbq.batchsdcardcopier.mvpbase.BasePresenter;
import org.dafa.practitioners.hbq.batchsdcardcopier.services.copier.BatchCopyService;
import org.dafa.practitioners.hbq.batchsdcardcopier.services.copier.BatchCopyServiceFactory;
import org.dafa.practitioners.hbq.batchsdcardcopier.services.drive.DriveEjector;
import org.dafa.practitioners.hbq.batchsdcardcopier.services.drive.DriveEjectorFactory;
import org.dafa.practitioners.hbq.batchsdcardcopier.services.drive.DriveScanner;
import org.dafa.practitioners.hbq.batchsdcardcopier.services.drive.DriveScannerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainPresenter extends BasePresenter<MvpContract.View> implements MvpContract.Presenter, DriveScanner.DrivePlugListener, BatchCopyService.BatchCopyListener {

    private DriveScanner driveScanner;
    private DriveEjector driveEjector;

    private BatchCopyServiceFactory batchCopyServiceFactory;

    private boolean isInAutoCopyMode = false;

    private ArrayList<BatchCopyService> activeBatchCopyServices = new ArrayList<>();

    MainPresenter(
            MvpContract.View view,
            DriveScannerFactory driveScannerFactory,
            DriveEjectorFactory driveEjectorFactory,
            BatchCopyServiceFactory batchCopyServiceFactory
    ) {
        super(view);
        this.batchCopyServiceFactory = batchCopyServiceFactory;

        this.driveEjector = driveEjectorFactory.createDriveEjector();

        this.driveScanner = driveScannerFactory.createDriveScanner();
        this.driveScanner.start(this);
    }

    @Override
    public void onDrivePluggedIn(File drive) {
        getView().addDrive(drive.getAbsolutePath());
        doAutoCopyIfNeeded(drive);
    }

    @Override
    public void onDriveUnplugged(File drive) {
        getView().removeDrive(drive.getAbsolutePath());
    }

    @Override
    public void doCopy() {
        final String sourcePath = getView().getSourceDirectory();
        if (sourcePath == null || sourcePath.isEmpty()) {
            getView().showError("Please select directory you want to copy!");
            return;
        }
        final File sourceDir = new File(sourcePath);
        if (!sourceDir.exists()) {
            getView().showError("Your selected directory does not exist! Please select a valid one.");
            return;
        }

        ObservableList<String> targetDrives = getView().getTargetDrives();
        if (targetDrives == null || targetDrives.isEmpty()) {
            getView().showError("Please select target drives!");
            return;
        }

        driveScanner.stop();
        BatchCopyService batchCopyService = batchCopyServiceFactory.createBatchCopyService(sourceDir, targetDrives, this);
        activeBatchCopyServices.add(batchCopyService);
        batchCopyService.start();
    }

    @Override
    public void cancelAndExit() {
        System.out.println("Cancel and exit");
        driveScanner.stop();
        for (BatchCopyService batchCopyService : activeBatchCopyServices) {
            batchCopyService.cancel();
        }
        activeBatchCopyServices.clear();
        getView().close();
    }

    @Override
    public void cancelCopying() {
        System.out.println("Batch copying cancelled.");
        for (BatchCopyService batchCopyService : activeBatchCopyServices) {
            batchCopyService.cancel();
        }
        activeBatchCopyServices.clear();
    }

    @Override
    public void toggleAutoCopyMode() {
        if (!hasSelectedValidSourceDirectory()) {
            getView().showError("Please select directory you want to copy first!");
            return;
        }

        final boolean autoCopyModeEnabled = !isInAutoCopyMode;
        System.out.println("Toggle Auto Copy mode: " + autoCopyModeEnabled);

        getView().setStartManualCopyButtonEnabled(!autoCopyModeEnabled);
        getView().setSourceDirectorySelectionEnabled(!autoCopyModeEnabled);
        if (autoCopyModeEnabled) {
            getView().setStartAutoCopyModeButtonText("Stop Auto copy mode");
        } else {
            getView().setStartAutoCopyModeButtonText("Start Auto copy mode");
        }
        isInAutoCopyMode = autoCopyModeEnabled;
    }

    private void doAutoCopyIfNeeded(File drive) {
        if (!isInAutoCopyMode || !hasSelectedValidSourceDirectory()) {
            return;
        }

        System.out.println("Drive detected. Do auto copying now: " + drive.getAbsolutePath());

        final File sourceDir = new File(getView().getSourceDirectory());
        List<String> targetDrives = Collections.singletonList(drive.getAbsolutePath());

        BatchCopyService batchCopyService = batchCopyServiceFactory.createBatchCopyService(sourceDir, targetDrives, this);
        activeBatchCopyServices.add(batchCopyService);
        batchCopyService.start();
    }

    @Override
    public void ejectSelectedDrives() {
        ObservableList<String> targetDrives = getView().getTargetDrives();
        if (targetDrives == null || targetDrives.isEmpty()) {
            getView().showError("Please select target drives!");
            return;
        }

        for (String drivePath : targetDrives) {
            File drive = new File(drivePath);
            driveEjector.eject(drive);
        }
    }

    private boolean hasSelectedValidSourceDirectory() {
        final String sourcePath = getView().getSourceDirectory();
        return sourcePath != null && !sourcePath.isEmpty()
                && new File(sourcePath).exists() && new File(sourcePath).isDirectory();
    }

    @Override
    public void onStarted(File targetDirectory) {
        getView().reportDirectoryCopingStarted(targetDirectory);
    }

    @Override
    public void onFileStarted(File targetDirectory, File file) {
        getView().reportFileStartedToCopy(targetDirectory, file);
    }

    @Override
    public void onProgressUpdated(File targetDirectory, File file, int fileProgress, int totalProgress) {
        if (totalProgress % 20 == 0) {
            System.out.println("Progress updated - " + targetDirectory.getAbsolutePath() + ": " + totalProgress + "%");
        }
        getView().reportCopyProgressUpdated(targetDirectory, file, fileProgress, totalProgress);
    }

    @Override
    public void onFileFinished(File targetDirectory, File file) {
        getView().reportFileCopyingFinished(targetDirectory, file);
    }

    @Override
    public void onFileError(File targetDirectory, File file, Throwable error) {
        System.err.println("Error copying file: " + file.getName() + ": " + error.getMessage());
    }

    @Override
    public void onCancelled(File targetDirectory) {
        System.out.println("Copying cancelled: " + targetDirectory.getAbsolutePath());
        getView().reportDirectoryCopyingCancelled(targetDirectory);
    }

    @Override
    public void onFinished(File targetDirectory) {
        System.out.println("Copying finished: " + targetDirectory.getAbsolutePath());
        getView().reportDirectoryCopyingFinished(targetDirectory);
        if (getView().isEjectDriveOnFinishEnabled()) {
            driveEjector.eject(targetDirectory);
        }
    }

    @Override
    public void onAllFinished(BatchCopyService batchCopyService, long totalCopyTime) {
        System.out.println("All copying finished: " + totalCopyTime + "ms");
        activeBatchCopyServices.remove(batchCopyService);
        // Restart drive scanner
        driveScanner.start(this);
        if (!isInAutoCopyMode || activeBatchCopyServices.isEmpty()) {
            getView().reportAllCopyingFinished(isInAutoCopyMode ? 0 : totalCopyTime);
        }
    }

    @Override
    public void onAllCancelled(BatchCopyService batchCopyService) {
        System.out.println("All Copying cancelled: " + batchCopyService);
        activeBatchCopyServices.remove(batchCopyService);
        // Restart drive scanner
        driveScanner.start(this);
        if (!isInAutoCopyMode || activeBatchCopyServices.isEmpty()) {
            getView().reportAllCopyingCancelled();
        }
    }
}
