package org.dafa.practitioners.hbq.batchsdcardcopier.ui.Main;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.dafa.practitioners.hbq.batchsdcardcopier.Main;
import org.dafa.practitioners.hbq.batchsdcardcopier.Utils;
import org.dafa.practitioners.hbq.batchsdcardcopier.services.copier.BatchCopyServiceFactory;
import org.dafa.practitioners.hbq.batchsdcardcopier.services.drive.BaseDriveScanner;
import org.dafa.practitioners.hbq.batchsdcardcopier.services.drive.DriveEjectorFactory;
import org.dafa.practitioners.hbq.batchsdcardcopier.services.drive.DriveScannerFactory;
import org.dafa.practitioners.hbq.batchsdcardcopier.ui.MultiProgressDialog;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public class MainView extends AnchorPane implements MvpContract.View {
	@FXML
	TextField txtDir;
	@FXML
	Button btnSelectDir;
	@FXML
	ListView<String> lstDrives;
	@FXML
	CheckBox chkAutoEject;
	@FXML
	Button btnOk;
	@FXML
	Button btnCancel;
	@FXML
	Button btnAutoCopyMode;
	@FXML
	Button btnDeleteData;

	private Stage window;
	private MainPresenter presenter;

	private MultiProgressDialog multiProgressDialog;

	public MainView(Stage window) {
		this.window = window;

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		initialize();
	}

	public MainPresenter getPresenter() {
		return presenter;
	}

	private void initialize() {
		presenter = new MainPresenter(
				this,
				new DriveScannerFactory(),
				new DriveEjectorFactory(),
				new BatchCopyServiceFactory()
		);

		lstDrives.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		ContextMenu contextMenu = new ContextMenu();

		MenuItem openItem = new MenuItem();
		openItem.setId("open");
		openItem.setText("Open in explorer");
		openItem.setOnAction(event -> openDirInExplorer());
		contextMenu.getItems().add(openItem);

		contextMenu.getItems().add(new SeparatorMenuItem());

		MenuItem ejectItem = new MenuItem();
		ejectItem.setId("eject");
		ejectItem.setText("Eject");
		ejectItem.setOnAction(ejectMenuEventHandler);
		contextMenu.getItems().add(ejectItem);

		MenuItem deleteItem = new MenuItem();
		deleteItem.setId("delete");
		deleteItem.setText("Delete data");
		deleteItem.setOnAction(ejectMenuEventHandler);
		contextMenu.getItems().add(deleteItem);

		lstDrives.setContextMenu(contextMenu);

		lstDrives.setOnMouseClicked(click -> {
			if (click.getClickCount() == 2) {
				openDirInExplorer();
			}
		});

		btnOk.setDefaultButton(true);
		btnOk.setOnAction(event -> presenter.doCopy());

		btnSelectDir.setOnAction(event -> pickSourceDirectory());
		btnCancel.setOnAction(event -> presenter.cancelAndExit());
		btnAutoCopyMode.setOnAction(event -> presenter.toggleAutoCopyMode());
		btnDeleteData.setOnAction(event -> presenter.deleteSDCard());

		window.setOnCloseRequest(event -> {
			Main.cleanupTestFiles();
			presenter.cancelAndExit();
		});

		Main.enableTestMode(window, presenter); // TODO: For testing only

		window.addEventFilter(KeyEvent.ANY, event -> {
			if (event.isControlDown()
					&& event.isAltDown()
					&& event.isShiftDown()
					&& event.getCode() == KeyCode.D
			) {
				DirectoryChooser chooser = new DirectoryChooser();
				File selectedDir = chooser.showDialog(window);
				if (selectedDir != null && selectedDir.exists()) {
					BaseDriveScanner.mountRoot = selectedDir;
				}
			}
		});
	}

	private void pickSourceDirectory() {
		DirectoryChooser chooser = new DirectoryChooser();
		File selectedDir = chooser.showDialog(window);
		if (selectedDir != null && selectedDir.exists()) {
			txtDir.setText(selectedDir.getAbsolutePath());
		}
	}

	@Override
	public void addDrive(String absolutePath) {
		Runnable runnable = () -> lstDrives.getItems().add(absolutePath);
		Platform.runLater(runnable);
	}

	@Override
	public void removeDrive(String absolutePath) {
		Runnable runnable = () -> lstDrives.getItems().remove(absolutePath);
		Platform.runLater(runnable);
	}

	private EventHandler<ActionEvent> ejectMenuEventHandler = event -> {
		Object source = event.getSource();
		if (!(source instanceof MenuItem)) {
			return;
		}

		MenuItem itm = (MenuItem) source;
		if (itm.getId().equals("eject")) {
			for (String drive : getTargetDrives()) {
				if (Main.isTestDrive(drive)) {
					File f = new File(drive);
					if (f.exists()) {
						removeDrive(drive);
						Utils.deleteDir(f);
					}
				}
			}
			presenter.ejectSelectedDrives();

		} else if (itm.getId().equals("delete")) {
			presenter.deleteSDCard();
		}
	};

	private void openDirInExplorer() {
		if (getTargetDrives().isEmpty()) {
			return;
		}
		String drive = getTargetDrives().get(0);
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().open(new File(drive));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getSourceDirectory() {
		return txtDir.getText();
	}

	@Override
	public ObservableList<String> getTargetDrives() {
		return lstDrives.getSelectionModel().getSelectedItems();
	}

	@Override
	public boolean isEjectDriveOnFinishEnabled() {
		return chkAutoEject.isSelected();
	}

	@Override
	public void showError(String message) {
		Runnable runnable = () -> {
			Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
			alert.showAndWait();
		};
		Platform.runLater(runnable);
	}

	@Override
	public void close() {
		Main.cleanupTestFiles();
		window.close();
	}

	@Override
	public void setStartManualCopyButtonEnabled(boolean enabled) {
		Platform.runLater(() -> btnOk.setDisable(!enabled));
	}

	@Override
	public void setStartAutoCopyModeButtonText(String text) {
		Platform.runLater(() -> btnAutoCopyMode.setText(text));
	}

	@Override
	public void setSourceDirectorySelectionEnabled(boolean enabled) {
		Platform.runLater(() -> {
			btnSelectDir.setDisable(!enabled);
			txtDir.setDisable(!enabled);
		});
	}

	//////////////////////////////////////////////////////////////////////
	// REPORT COPYING PROGRESS:
	//////////////////////////////////////////////////////////////////////
	@Override
	public void reportDirectoryCopingStarted(File targetDirectory) {
		Platform.runLater(() -> {
			if (multiProgressDialog == null) {
				multiProgressDialog = new MultiProgressDialog(
						window,
						() -> presenter.cancelCopying(),
						new MultiProgressDialog.MultiProgressDialogButton("Cancel", () -> presenter.cancelCopying())
				);
				multiProgressDialog.show();
			}

			MultiProgressDialog.MultiProgressDialogTask task = new MultiProgressDialog.MultiProgressDialogTask(
					targetDirectory.getAbsolutePath(),
					new SimpleStringProperty("Copying to: " + targetDirectory.getAbsolutePath()),
					new SimpleDoubleProperty(0)
			);
			multiProgressDialog.addTask(task);
		});
	}

	@Override
	public void reportFileStartedToCopy(File targetDirectory, File file) {

	}

	@Override
	public void reportCopyProgressUpdated(File targetDirectory, File file, int fileProgress, int totalProgress) {
		if (multiProgressDialog == null) {
			return;
		}
		MultiProgressDialog.MultiProgressDialogTask task = multiProgressDialog.getTaskById(targetDirectory.getAbsolutePath());
		if (task != null) {
			Platform.runLater(() -> {
				task.subTaskName.set(file.getName() + ": " + fileProgress + "%");
				task.progress.set((double) totalProgress / 100d);
			});
		}
	}

	@Override
	public void reportFileCopyingFinished(File targetDirectory, File file) {

	}

	@Override
	public void reportDirectoryCopyingCancelled(File targetDirectory) {
		if (multiProgressDialog == null) {
			return;
		}
		Platform.runLater(() -> {
			if (multiProgressDialog == null) {
				return;
			}
			multiProgressDialog.removeTaskById(targetDirectory.getAbsolutePath());
		});
	}

	@Override
	public void reportDirectoryCopyingFinished(File targetDirectory) {
		if (multiProgressDialog == null) {
			return;
		}
		Platform.runLater(() -> multiProgressDialog.removeTaskById(targetDirectory.getAbsolutePath()));
	}

	@Override
	public void reportAllCopyingFinished(long totalCopyTime) {
		if (multiProgressDialog == null) {
			return;
		}
		Platform.runLater(() -> {
			multiProgressDialog.close();
			multiProgressDialog = null;

			if (totalCopyTime > 0) {
				final String duration = String.format("%d min, %d sec",
						TimeUnit.MILLISECONDS.toMinutes(totalCopyTime),
						TimeUnit.MILLISECONDS.toSeconds(totalCopyTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalCopyTime))
				);
				Alert alert = new Alert(Alert.AlertType.INFORMATION, "Copying finished. Total time: " + duration, ButtonType.OK);
				alert.show();
			}
		});
	}

	@Override
	public boolean showConfirm(String s) {
		Alert alert = new Alert(Alert.AlertType.WARNING, s, ButtonType.YES, ButtonType.NO);
		Optional<ButtonType> ret = alert.showAndWait();
		return (ret.isPresent() && ret.get() == ButtonType.YES);
	}

	@Override
	public void reportAllCopyingCancelled() {
		if (multiProgressDialog == null) {
			return;
		}
		Platform.runLater(() -> {
			if (multiProgressDialog == null) {
				return;
			}
			multiProgressDialog.close();
			multiProgressDialog = null;
		});
	}

	@Override
	public void reportDirectoryCopyingError(File targetDirectory, Throwable error) {
		if (multiProgressDialog == null) {
			return;
		}
		Platform.runLater(() -> {
			if (multiProgressDialog == null) {
				return;
			}
			multiProgressDialog.removeTaskById(targetDirectory.getAbsolutePath());
		});
	}

	@Override
	public void setEjectCheckboxEnabled(boolean enabled) {
		chkAutoEject.setDisable(!enabled);
	}
}
