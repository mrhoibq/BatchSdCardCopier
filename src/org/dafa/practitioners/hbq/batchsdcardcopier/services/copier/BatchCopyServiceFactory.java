package org.dafa.practitioners.hbq.batchsdcardcopier.services.copier;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BatchCopyServiceFactory {

    private ExecutorService executorService;

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public BatchCopyService createBatchCopyService(final File sourceDir, final List<String> targetDirectories, final BatchCopyService.BatchCopyListener progressListener) {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(10);
        }
        return new BatchCopyService(executorService, sourceDir, targetDirectories, progressListener);
    }

    public void dispose() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
