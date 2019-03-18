package org.dafa.practitioners.hbq.batchsdcardcopier.services.copier;

import org.dafa.practitioners.hbq.batchsdcardcopier.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class BatchCopyService implements DirectoryCopyListener {

	private File sourceDir;
	private List<String> targetDirectories;
	private BatchCopyListener progressListener;

	private volatile boolean cancelled = false;
	private ArrayList<DirectoryCopier> dirCopiers = new ArrayList<>();
	private AtomicInteger doneCount = new AtomicInteger(0);
	private long startTime;
	private ExecutorService executor;

	BatchCopyService(ExecutorService executor, final File sourceDir, final List<String> targetDirectories, final BatchCopyListener progressListener) {
		this.sourceDir = sourceDir;
		this.targetDirectories = targetDirectories;
		this.progressListener = progressListener;
		this.executor = executor;
	}

	public void start() {
		dirCopiers = new ArrayList<>();
		cancelled = false;
		doneCount.set(0);
		startTime = 0;

		if (sourceDir == null || !sourceDir.exists() || sourceDir.isFile()) {
			return;
		}

		for (String targetPath : targetDirectories) {
			if (cancelled) {
				break;
			}

			final File targetDir = new File(targetPath);
			if (!targetDir.exists()) {
				continue;
			}

			Utils.deleteHiddenFiles(targetDir);

			DirectoryCopier directoryCopier = new DirectoryCopier(sourceDir, targetDir);
			dirCopiers.add(directoryCopier);

			directoryCopier.setProgressListener(this);
			executor.execute(directoryCopier);
		}
	}

	public void cancel() {
		cancelled = true;
		if (dirCopiers != null) {
			for (DirectoryCopier dirCopier : dirCopiers) {
				dirCopier.cancel();
			}
		}
	}

	public void cancelAndShutdown() {
		cancel();
		executor.shutdown();
	}

	@Override
	public void onStarted(File targetDirectory) {
		startTime = System.currentTimeMillis();
		System.out.println("Started to copy from " + sourceDir.getAbsolutePath() + " to " + targetDirectory.getAbsolutePath());
		progressListener.onStarted(targetDirectory);
	}

	@Override
	public void onFileStarted(File targetDirectory, File file) {
		progressListener.onFileStarted(targetDirectory, file);
	}

	@Override
	public void onProgressUpdated(File targetDirectory, File file, int fileProgress, int totalProgress) {
//                    System.out.println("onProgressUpdated: " + file.getName() + ": " + fileProgress + " / " + totalProgress);
		progressListener.onProgressUpdated(targetDirectory, file, fileProgress, totalProgress);
	}

	@Override
	public void onFileFinished(File targetDirectory, File file) {
		progressListener.onFileFinished(targetDirectory, file);
	}

	@Override
	public void onFileError(File targetDirectory, File file, Throwable error) {
		progressListener.onFileError(targetDirectory, file, error);
	}

	@Override
	public void onCancelled(File targetDirectory) {
		System.out.println("Cancelled: " + targetDirectory.getAbsolutePath());
		doneCount.incrementAndGet();
		progressListener.onCancelled(targetDirectory);
		if (doneCount.get() >= targetDirectories.size()) {
			progressListener.onAllCancelled(this);
			// executor.shutdown();
		}
	}

	@Override
	public void onFinished(File targetDirectory) {
		Utils.deleteHiddenFiles(targetDirectory);
		System.out.println("Finished copying from " + sourceDir.getAbsolutePath() + " to " + targetDirectory.getAbsolutePath());
		doneCount.incrementAndGet();
		progressListener.onFinished(targetDirectory);
		if (doneCount.get() >= targetDirectories.size()) {
			progressListener.onAllFinished(this, System.currentTimeMillis() - startTime);
			// executor.shutdown();
			startTime = 0;
		}
	}

	public interface BatchCopyListener extends DirectoryCopyListener {
		void onAllFinished(BatchCopyService batchCopyService, long totalCopyTime);
		void onAllCancelled(BatchCopyService batchCopyService);
	}
}
