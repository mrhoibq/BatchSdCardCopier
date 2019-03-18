package org.dafa.practitioners.hbq.batchsdcardcopier.services.copier;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class DirectoryCopier implements Runnable, FileCopier.ProgressListener {

  private File sourceDir;
  private File targetDir;

  private volatile boolean cancelled = false;

  private long totalLength = 0;
  private long totalCopied = 0;

  private DirectoryCopyListener progressListener;

  private ArrayList<FileCopier> fileCopiers = new ArrayList<>();

  public DirectoryCopier(File sourceDir, File targetDir) {
    this.sourceDir = sourceDir;
    this.targetDir = targetDir;
  }

  public void setProgressListener(DirectoryCopyListener listener) {
    progressListener = listener;
  }

  public void start() {
    run();
  }

  @Override
  public void run() {
    fileCopiers.clear();
    totalCopied = 0;
    totalLength = 0;

    if (progressListener != null) {
      progressListener.onStarted(targetDir);
    }

    final File[] sources = sourceDir.listFiles(f -> f.isFile() && !f.isHidden());
    if (sources == null || sources.length == 0) {
      if (progressListener != null) {
        progressListener.onFinished(targetDir);
      }
      return;
    }

    Arrays.sort(sources);

    for (File source : sources) {
      totalLength += source.length();
    }

    for (File source : sources) {
      if (cancelled) {
        if (progressListener != null) {
          progressListener.onCancelled(targetDir);
        }
        break;
      }

      if (!targetDir.exists()) {
        if (progressListener != null) {
          progressListener.onFileError(targetDir, source, new RuntimeException("Target drive does not exist! Did "));
        }
        break;
      }

      FileCopier fileCopier = new FileCopier(source, new File(targetDir, source.getName()));
      fileCopiers.add(fileCopier);

      fileCopier.setProgressListener(this);
      fileCopier.start();
    }

    if (!cancelled && progressListener != null) {
      progressListener.onFinished(targetDir);
    }
  }

  public void cancel() {
    cancelled = true;
    if (fileCopiers != null) {
      for (FileCopier fileCopier : fileCopiers) {
        fileCopier.cancel();
      }
    }
    if (fileCopiers != null) {
      fileCopiers.clear();
    }
  }


  @Override
  public void onFileStarted(File file) {
    if (progressListener != null) {
      progressListener.onFileStarted(targetDir, file);
    }
  }

  @Override
  public void onFileError(File file, Throwable error) {
    if (progressListener != null) {
      progressListener.onFileError(targetDir, file, error);
    }
  }

  @Override
  public void onFileFinished(File file) {
    totalCopied += file.length();
    if (progressListener != null) {
      progressListener.onFileFinished(targetDir, file);
    }
  }

  @Override
  public void onFileCancelled(File file) {
    File target = new File(targetDir, file.getName());
    if (target.exists()) {
      target.delete();
    }
  }

  @Override
  public void onFileProgressUpdated(File file, long copied, long total) {
    if (progressListener != null) {
      progressListener.onProgressUpdated(
          targetDir, file,
          (int) Math.round(copied * 100d / total),
          (int) Math.round((totalCopied + copied) * 100d / totalLength)
      );
    }
  }
}
