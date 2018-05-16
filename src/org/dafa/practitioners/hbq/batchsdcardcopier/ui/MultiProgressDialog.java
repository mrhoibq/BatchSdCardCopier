package org.dafa.practitioners.hbq.batchsdcardcopier.ui;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.dafa.practitioners.hbq.batchsdcardcopier.Main;
import org.dafa.practitioners.hbq.batchsdcardcopier.ui.Main.MainView;

import java.util.HashMap;
import java.util.Map;

public class MultiProgressDialog {

    private Stage dialogStage;
    private VBox rootContainer;
    private VBox root;

    private Map<String, MultiProgressDialogTask> taskMap = new HashMap<>();

    public MultiProgressDialog(Stage owner, Runnable onWindowClosedTask, MultiProgressDialogButton... buttons) {
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setResizable(true);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        dialogStage.initOwner(owner);

        rootContainer = new VBox();
        rootContainer.setMinWidth(300);

        root = new VBox(rootContainer);
        root.setPadding(new Insets(8));

        if (buttons == null || buttons.length == 0) {
            buttons = new MultiProgressDialogButton[] {
                    new MultiProgressDialogButton("OK", null)
            };
        }

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(0, 0, 10, 0));
        root.getChildren().add(separator);

        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER);
        for (MultiProgressDialogButton button : buttons) {
            Button btn = new Button(button.name);
            btn.setOnAction(event -> {
                if (button.onClickRunnable != null) {
                    button.onClickRunnable.run();
                }
                close();
            });
            buttonContainer.getChildren().add(btn);
        }
        root.getChildren().add(buttonContainer);

        Scene scene = new Scene(root);
        dialogStage.setScene(scene);

        dialogStage.setOnCloseRequest(event -> {
            Main.cleanupTestFiles();
            if (onWindowClosedTask != null) {
                onWindowClosedTask.run();
            }
        });

        Main.enableTestMode(dialogStage, ((MainView) owner.getScene().getRoot()).getPresenter());
    }

    public void show() {
        dialogStage.show();
    }

    public void close() {
        taskMap.clear();
        rootContainer.getChildren().clear();
        dialogStage.close();
    }

    public void addTask(MultiProgressDialogTask task) {
        taskMap.put(task.id, task);

        Platform.runLater(() -> {
            Label label = new Label();
            label.setFont(Font.font(label.getFont().toString(), FontWeight.BOLD, label.getFont().getSize()));
            label.textProperty().bind(task.name);

            Label subTaskLabel = new Label();
            subTaskLabel.setFont(Font.font(9));
            subTaskLabel.textProperty().bind(task.subTaskName);

            ProgressBar progressBar = new ProgressBar();
            progressBar.progressProperty().bind(task.progress);

            final VBox container = new VBox(label, subTaskLabel, progressBar);
            container.setPadding(new Insets(0, 0, 8, 0));

            root.widthProperty().addListener((observable, oldValue, newValue) -> {
                double width = newValue.doubleValue() - root.getPadding().getLeft() - root.getPadding().getRight();
                container.setMaxWidth(width);
                container.setMinWidth(width);
                container.setPrefWidth(width);
            });

            container.widthProperty().addListener((observable, oldValue, newValue) -> {
                progressBar.setMaxWidth(newValue.doubleValue());
                progressBar.setMinWidth(newValue.doubleValue());
                progressBar.setPrefWidth(newValue.doubleValue());

                label.setMaxWidth(newValue.doubleValue());
                label.setMinWidth(newValue.doubleValue());
                label.setPrefWidth(newValue.doubleValue());

                subTaskLabel.setMaxWidth(newValue.doubleValue());
                subTaskLabel.setMinWidth(newValue.doubleValue());
                subTaskLabel.setPrefWidth(newValue.doubleValue());
            });

            container.setUserData(task);
            rootContainer.getChildren().add(container);

            container.autosize();
            rootContainer.autosize();
            root.autosize();

            dialogStage.getScene().getWindow().sizeToScene();
            dialogStage.getScene().getWindow().centerOnScreen();
        });
    }

    public void removeTask(MultiProgressDialogTask task) {
        taskMap.remove(task.id);

        Platform.runLater(() -> {
            for (int n = rootContainer.getChildren().size(), i = n - 1; i >= 0; i--) {
                final Node node = rootContainer.getChildren().get(i);
                if (node.getUserData() != null && node.getUserData().equals(task)) {
                    rootContainer.getChildren().remove(i);

                    rootContainer.autosize();
                    root.autosize();

                    dialogStage.getScene().getWindow().sizeToScene();
                    dialogStage.getScene().getWindow().centerOnScreen();

                    break;
                }
            }
        });
    }

    public MultiProgressDialogTask getTaskById(String id) {
        return taskMap.get(id);
    }

    public void removeTaskById(String id) {
        MultiProgressDialogTask task = taskMap.get(id);
        if (task != null) {
            removeTask(task);
        }
    }

    public static class MultiProgressDialogTask {
        String id;
        public StringProperty name;
        public DoubleProperty progress;

        public StringProperty subTaskName = new SimpleStringProperty("");

        public MultiProgressDialogTask(String id, StringProperty name, DoubleProperty progress) {
            this.id = id;
            this.name = name;
            this.progress = progress;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            return ((MultiProgressDialogTask) obj).id.equals(id);
        }
    }

    public static class MultiProgressDialogButton {
        String name;
        Runnable onClickRunnable;
        public MultiProgressDialogButton(String name, Runnable onClickRunnable) {
            this.name = name;
            this.onClickRunnable = onClickRunnable;
        }
    }
}
