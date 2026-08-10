package zaikoApp;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class PowderMasterScreen {

    public void show() {

        Stage stage = new Stage();

        Label title = new Label("粉末マスター");
        title.setStyle(
                "-fx-font-size:24px; -fx-font-weight:bold;"
        );

        TextField powderField = new TextField();
        powderField.setPromptText("粉末名");

        TextField makerField = new TextField();
        makerField.setPromptText("メーカー");

        Button addButton = new Button("追加");
        Button editButton = new Button("編集");
        Button deleteButton = new Button("削除");
        Button closeButton = new Button("閉じる");

        TableView<PowderMaster> table = new TableView<>();

        TableColumn<PowderMaster, String> powderColumn =
                new TableColumn<>("粉末名");

        powderColumn.setCellValueFactory(
                new PropertyValueFactory<>("powderName")
        );

        TableColumn<PowderMaster, String> makerColumn =
                new TableColumn<>("メーカー");

        makerColumn.setCellValueFactory(
                new PropertyValueFactory<>("maker")
        );

        table.getColumns().add(powderColumn);
        table.getColumns().add(makerColumn);

        table.setItems(
                Database.loadPowderMasters()
        );
        
        final String[] selectedOldName = new String[1];

        table.getSelectionModel()
             .selectedItemProperty()
             .addListener((obs, oldSelection, newSelection) -> {

            if (newSelection != null) {

                powderField.setText(
                        newSelection.getPowderName()
                );

                makerField.setText(
                        newSelection.getMaker()
                );

                selectedOldName[0] =
                        newSelection.getPowderName();
            }
        });

        addButton.setOnAction(e -> {

            String powderName =
                    powderField.getText().trim();

            String maker =
                    makerField.getText().trim();

            if (powderName.isBlank() || maker.isBlank()) {

                Alert alert =
                        new Alert(Alert.AlertType.ERROR);

                alert.setContentText(
                        "粉末名とメーカーを入力してください。"
                );

                alert.showAndWait();
                return;
            }   

            Database.savePowderMaster(
                    powderName,
                    maker
            );

            // 一覧を更新
            table.setItems(
                    Database.loadPowderMasters()
            );

            powderField.clear();
            makerField.clear();
        });
        
        editButton.setOnAction(e -> {

            if (selectedOldName[0] == null) {

                Alert alert =
                        new Alert(Alert.AlertType.ERROR);

                alert.setHeaderText(null);
                alert.setContentText(
                        "編集する粉末を一覧から選択してください。"
                );

                alert.showAndWait();
                return;
            }

            String newPowderName =
                    powderField.getText().trim();

            String maker =
                    makerField.getText().trim();

            if (newPowderName.isBlank() ||
                    maker.isBlank()) {

                Alert alert =
                        new Alert(Alert.AlertType.ERROR);

                alert.setHeaderText(null);
                alert.setContentText(
                        "粉末名とメーカーを入力してください。"
                );

                alert.showAndWait();
                return;
            }

            Database.updatePowderMaster(
                    selectedOldName[0],
                    newPowderName,
                    maker
            );

            table.setItems(
                    Database.loadPowderMasters()
            );

            powderField.clear();
            makerField.clear();

            selectedOldName[0] = null;
        });
        
        deleteButton.setOnAction(e -> {

            PowderMaster selected =
                    table.getSelectionModel().getSelectedItem();

            if (selected == null) {

                Alert alert =
                        new Alert(Alert.AlertType.ERROR);

                alert.setHeaderText(null);
                alert.setContentText(
                        "削除する粉末を一覧から選択してください。"
                );

                alert.showAndWait();
                return;
            }

            Database.deletePowderMaster(
                    selected.getPowderName()
            );

            table.setItems(
                    Database.loadPowderMasters()
            );
        });

        closeButton.setOnAction(e -> stage.close());

        GridPane inputArea = new GridPane();

        inputArea.setHgap(10);
        inputArea.setVgap(10);

        inputArea.add(new Label("粉末名"), 0, 0);
        inputArea.add(powderField, 1, 0);

        inputArea.add(new Label("メーカー"), 0, 1);
        inputArea.add(makerField, 1, 1);

        // ボタンを横並びにする
        HBox buttonArea = new HBox(10);

        buttonArea.getChildren().addAll(
                addButton,
                editButton,
                deleteButton
        );

        // GridPaneにはbuttonAreaを1回だけ追加
        inputArea.add(buttonArea, 0, 2, 2, 1);

        VBox root = new VBox(15);

        root.setPadding(new Insets(20));

        root.getChildren().addAll(
                title,
                inputArea,
                table,
                closeButton
        );
        
        Scene scene = new Scene(root, 600, 500);

        stage.setTitle("粉末マスター");
        stage.setScene(scene);
        stage.show();
    }
}