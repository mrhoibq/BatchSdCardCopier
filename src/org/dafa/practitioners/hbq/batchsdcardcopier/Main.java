package org.dafa.practitioners.hbq.batchsdcardcopier;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.dafa.practitioners.hbq.batchsdcardcopier.ui.Main.MainPresenter;
import org.dafa.practitioners.hbq.batchsdcardcopier.ui.Main.MainView;

import java.io.File;
import java.util.ArrayList;

public class Main extends Application {

	private static TestKeyEventHandler testKeyEventHandler = null;

	@Override
	public void start(Stage primaryStage) {
		File file = new File("");
		System.out.println("Current working Dir: " + file.getAbsolutePath());

		primaryStage.setTitle("Batch SDCard Copier");
		Pane root = new MainView(primaryStage);
		Scene scene = new Scene(root, 700, 400);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	private static ArrayList<File> testDrives = new ArrayList<>();
	public static void enableTestMode(final Stage primaryStage, final MainPresenter presenter) {
		if (testKeyEventHandler == null) {
			testKeyEventHandler = new TestKeyEventHandler(presenter);
		}
		primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, testKeyEventHandler);
	}

	public static void cleanupTestFiles() {
		testKeyEventHandler = null;
        for (File drive : new ArrayList<>(testDrives)) {
            deleteDir(drive);
        }
        testDrives.clear();
	}

	public static void deleteDir(final File dir) {
		if (dir == null || !dir.exists()) {
			return;
		}

		if (dir.isFile()) {
			//noinspection ResultOfMethodCallIgnored
			dir.delete();
			return;
		}

		File[] files = dir.listFiles();
		if (files != null && files.length > 0) {
			for (File file : files) {
				deleteDir(file);
			}
		}
		//noinspection ResultOfMethodCallIgnored
		dir.delete();
	}

	public static boolean isTestDrive(String drive) {
	    for (File f : new ArrayList<>(testDrives)) {
	        if (f.getAbsolutePath().equalsIgnoreCase(drive)) {
	            return true;
            }
        }
		return false;
	}

	static class TestKeyEventHandler implements EventHandler<KeyEvent> {
		private MainPresenter presenter;

		TestKeyEventHandler(MainPresenter presenter) {
			this.presenter = presenter;
		}

		@Override
		public void handle(KeyEvent event) {
			if (event.isShiftDown() && event.isControlDown() && event.isAltDown() && event.getCode() == KeyCode.T) {
				File drive = new File("virtual-drive-" + System.currentTimeMillis());
				if (drive.mkdirs()) {
					testDrives.add(drive);
					presenter.onDrivePluggedIn(drive);
				}
			}
		}
	}
}
