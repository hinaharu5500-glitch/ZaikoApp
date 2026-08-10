package zaikoApp;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class WorkerMasterScreen {

    public void show() {

        Stage stage = new Stage();

        Label title =
                new Label("作業者マスター");

        title.setStyle(
                "-fx-font-size:24px;" +
                "-fx-font-weight:bold;"
        );

        TextField nameField =
                new TextField();

        nameField.setPromptText(
                "作業者名"
        );

        Button addButton =
                new Button("追加");

        Button editButton =
                new Button("編集");

        Button deleteButton =
                new Button("削除");

        Button closeButton =
                new Button("閉じる");


        // =====================
        // TableView
        // =====================

        TableView<Worker> table =
                new TableView<>();

        TableColumn<Worker, Integer> idColumn =
                new TableColumn<>("ID");

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        TableColumn<Worker, String> nameColumn =
                new TableColumn<>("作業者名");

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        idColumn.setPrefWidth(80);
        nameColumn.setPrefWidth(250);

        table.getColumns().add(idColumn);
        table.getColumns().add(nameColumn);

        table.setItems(
                Database.loadWorkers()
        );


        // =====================
        // 一覧を選択
        // =====================

        table.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldValue, newValue) -> {

                            if (newValue != null) {

                                nameField.setText(
                                        newValue.getName()
                                );
                            }
                        }
                );


        // =====================
        // 追加
        // =====================

        addButton.setOnAction(e -> {

            String name =
                    nameField.getText().trim();

            if (name.isBlank()) {

                showError(
                        "作業者名を入力してください。"
                );

                return;
            }

            boolean success =
                    Database.saveWorker(name);

            if (!success) {

                showError(
                        "登録できませんでした。\n" +
                        "同じ作業者名が登録済みの可能性があります。"
                );

                return;
            }

            table.setItems(
                    Database.loadWorkers()
            );

            nameField.clear();
        });


        // =====================
        // 編集
        // =====================

        editButton.setOnAction(e -> {

            Worker selected =
                    table.getSelectionModel()
                            .getSelectedItem();

            if (selected == null) {

                showError(
                        "編集する作業者を選択してください。"
                );

                return;
            }

            String newName =
                    nameField.getText().trim();

            if (newName.isBlank()) {

                showError(
                        "作業者名を入力してください。"
                );

                return;
            }

            boolean success =
                    Database.updateWorker(
                            selected.getId(),
                            newName
                    );

            if (success) {

                table.setItems(
                        Database.loadWorkers()
                );

                nameField.clear();

            } else {

                showError(
                        "作業者を編集できませんでした。"
                );
            }
        });


        // =====================
        // 削除
        // =====================

        deleteButton.setOnAction(e -> {

            Worker selected =
                    table.getSelectionModel()
                            .getSelectedItem();

            if (selected == null) {

                showError(
                        "削除する作業者を選択してください。"
                );

                return;
            }

            boolean success =
                    Database.deleteWorker(
                            selected.getId()
                    );

            if (success) {

                table.setItems(
                        Database.loadWorkers()
                );

                nameField.clear();

            } else {

                showError(
                        "作業者を削除できませんでした。"
                );
            }
        });


        closeButton.setOnAction(
                e -> stage.close()
        );


        HBox inputArea =
                new HBox(
                        10,
                        nameField,
                        addButton,
                        editButton,
                        deleteButton
                );


        VBox root =
                new VBox(15);

        root.setPadding(
                new Insets(25)
        );

        root.getChildren().addAll(
                title,
                inputArea,
                table,
                closeButton
        );


        Scene scene =
                new Scene(
                        root,
                        600,
                        500
                );

        stage.setTitle(
                "作業者マスター"
        );

        stage.setScene(scene);
        stage.show();
    }


    private void showError(String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}