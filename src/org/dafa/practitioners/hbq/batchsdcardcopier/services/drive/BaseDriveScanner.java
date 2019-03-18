package org.dafa.practitioners.hbq.batchsdcardcopier.services.drive;

import java.io.File;

public abstract class BaseDriveScanner implements DriveScanner, Runnable {

	public static volatile File mountRoot = null;

	private DrivePlugListener listener;
	private Thread scannerThread;

	@Override
	public synchronized void start(DrivePlugListener listener) {
		if (scannerThread == null || !scannerThread.isAlive()) {
			this.listener = listener;
			scannerThread = new Thread(this);
			scannerThread.setDaemon(true);
			scannerThread.setPriority(Thread.MIN_PRIORITY);
			scannerThread.start();
		}
	}

	@Override
	public void run() {
		while (scannerThread != null && !scannerThread.isInterrupted()) {
			scan(listener);
			sleep(2000);
		}
	}

	protected abstract void scan(DrivePlugListener listener);

	protected void onStarted() {

	}

	protected void onStopped() {

	}

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ignore) {

		}
	}

	@Override
	public synchronized void stop() {
		listener = null;
		if (scannerThread != null) {
			scannerThread.interrupt();
			scannerThread = null;
		}
	}
}
