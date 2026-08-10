package zaikoApp;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SyukkoHistory {

    public void show() {

        Stage stage = new Stage();

        // =====================================
        // タイトル
        // =====================================

        Label title = new Label("出庫履歴");

        title.setStyle(
                "-fx-font-size: 24px;" +
                "-fx-font-weight: bold;"
        );


        // =====================================
        // TableView
        // =====================================

        TableView<ShipmentHistory> table =
                new TableView<>();


        // -------------------------------------
        // 履歴ID
        // -------------------------------------

        TableColumn<ShipmentHistory, Integer> idColumn =
                new TableColumn<>("履歴ID");

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        idColumn.setPrefWidth(70);


        // -------------------------------------
        // 在庫ID
        // -------------------------------------

        TableColumn<ShipmentHistory, Integer> inventoryIdColumn =
                new TableColumn<>("在庫ID");

        inventoryIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("inventoryId")
        );

        inventoryIdColumn.setPrefWidth(70);


        // -------------------------------------
        // 出庫日時
        // -------------------------------------

        TableColumn<ShipmentHistory, String> dateColumn =
                new TableColumn<>("出庫日時");

        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>("shipmentDate")
        );

        dateColumn.setPrefWidth(150);


        // -------------------------------------
        // ロット番号
        // -------------------------------------

        TableColumn<ShipmentHistory, String> lotColumn =
                new TableColumn<>("ロット番号");

        lotColumn.setCellValueFactory(
                new PropertyValueFactory<>("lotNumber")
        );

        lotColumn.setPrefWidth(120);


        // -------------------------------------
        // 粉末名
        // -------------------------------------

        TableColumn<ShipmentHistory, String> powderColumn =
                new TableColumn<>("粉末名");

        powderColumn.setCellValueFactory(
                new PropertyValueFactory<>("powderName")
        );

        powderColumn.setPrefWidth(120);


        // -------------------------------------
        // 使用数量
        // -------------------------------------

        TableColumn<ShipmentHistory, Integer> quantityColumn =
                new TableColumn<>("使用数量");

        quantityColumn.setCellValueFactory(
                new PropertyValueFactory<>("usedQuantity")
        );

        quantityColumn.setPrefWidth(90);


        // -------------------------------------
        // 使用重量
        // -------------------------------------

        TableColumn<ShipmentHistory, Double> weightColumn =
                new TableColumn<>("使用重量(kg)");

        weightColumn.setCellValueFactory(
                new PropertyValueFactory<>("usedWeight")
        );

        weightColumn.setPrefWidth(110);


        // -------------------------------------
        // 使用先
        // -------------------------------------

        TableColumn<ShipmentHistory, String> usedProductColumn =
                new TableColumn<>("使用先");

        usedProductColumn.setCellValueFactory(
                new PropertyValueFactory<>("usedProduct")
        );

        usedProductColumn.setPrefWidth(140);


        // -------------------------------------
        // 出庫者
        // -------------------------------------

        TableColumn<ShipmentHistory, String> operatorColumn =
                new TableColumn<>("出庫者");

        operatorColumn.setCellValueFactory(
                new PropertyValueFactory<>("operator")
        );

        operatorColumn.setPrefWidth(100);


        // -------------------------------------
        // 状態
        // -------------------------------------

        TableColumn<ShipmentHistory, String> statusColumn =
                new TableColumn<>("状態");

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        statusColumn.setPrefWidth(80);


        // =====================================
        // 列をTableViewへ追加
        // =====================================

        table.getColumns().add(idColumn);
        table.getColumns().add(inventoryIdColumn);
        table.getColumns().add(dateColumn);
        table.getColumns().add(lotColumn);
        table.getColumns().add(powderColumn);
        table.getColumns().add(quantityColumn);
        table.getColumns().add(weightColumn);
        table.getColumns().add(usedProductColumn);
        table.getColumns().add(operatorColumn);
        table.getColumns().add(statusColumn);


        // =====================================
        // DBから履歴を読み込み
        // =====================================

        table.setItems(
                Database.loadShipmentHistory()
        );


        // =====================================
        // 取消済をグレー表示
        // =====================================

        table.setRowFactory(tv ->
                new TableRow<ShipmentHistory>() {

                    @Override
                    protected void updateItem(
                            ShipmentHistory history,
                            boolean empty) {

                        super.updateItem(
                                history,
                                empty
                        );

                        if (empty || history == null) {

                            setStyle("");

                            return;
                        }


                        if (history.isCancelled()) {

                            setStyle(
                                    "-fx-background-color: #dddddd;" +
                                    "-fx-text-fill: #777777;"
                            );

                        } else {

                            setStyle("");
                        }
                    }
                }
        );


        // =====================================
        // 出庫取消ボタン
        // =====================================

        Button cancelButton =
                new Button("選択した出庫を取消");

        cancelButton.setPrefWidth(180);


        cancelButton.setOnAction(e -> {

            ShipmentHistory selected =
                    table
                            .getSelectionModel()
                            .getSelectedItem();


            // ---------------------------------
            // 未選択
            // ---------------------------------

            if (selected == null) {

                showError(
                        "取消する出庫履歴を選択してください。"
                );

                return;
            }


            // ---------------------------------
            // すでに取消済
            // ---------------------------------

            if (selected.isCancelled()) {

                showError(
                        "この出庫はすでに取消済です。"
                );

                return;
            }


            // ---------------------------------
            // 古い履歴で在庫IDがない
            // ---------------------------------

            if (selected.getInventoryId() <= 0) {

                showError(
                        "この履歴には在庫IDがありません。\n" +
                        "ID機能追加前の古い履歴は取消できません。"
                );

                return;
            }


            // =================================
            // 確認ダイアログ
            // =================================

            Alert confirm =
                    new Alert(
                            Alert.AlertType.CONFIRMATION
                    );

            confirm.setTitle(
                    "出庫取消確認"
            );

            confirm.setHeaderText(
                    "この出庫を取り消しますか？"
            );

            confirm.setContentText(

                    "履歴ID：" +
                    selected.getId() +

                    "\n在庫ID：" +
                    selected.getInventoryId() +

                    "\nロット番号：" +
                    selected.getLotNumber() +

                    "\n粉末名：" +
                    selected.getPowderName() +

                    "\n使用数量：" +
                    selected.getUsedQuantity() +

                    "\n使用重量：" +
                    selected.getUsedWeight() +
                    " kg" +

                    "\n使用先：" +
                    selected.getUsedProduct() +

                    "\n出庫者：" +
                    selected.getOperator()
            );


            confirm.showAndWait()
                    .ifPresent(button -> {

                        if (button ==
                                ButtonType.OK) {


                            // =============================
                            // DBで取消処理
                            // =============================

                            boolean success =
                                    Database.cancelShipment(
                                            selected.getId()
                                    );


                            if (success) {

                                // =========================
                                // 在庫一覧を再読み込み
                                // =========================

                                InventoryData
                                        .getProducts()
                                        .setAll(
                                                Database
                                                        .loadProducts()
                                        );


                                // =========================
                                // 出庫履歴も再読み込み
                                // =========================

                                table.setItems(
                                        Database
                                                .loadShipmentHistory()
                                );


                                // =========================
                                // 成功メッセージ
                                // =========================

                                Alert successAlert =
                                        new Alert(
                                                Alert.AlertType.INFORMATION
                                        );

                                successAlert.setTitle(
                                        "出庫取消完了"
                                );

                                successAlert.setHeaderText(
                                        null
                                );

                                successAlert.setContentText(
                                        "出庫を取り消しました。\n\n" +
                                        "使用数量：" +
                                        selected.getUsedQuantity() +
                                        "\n" +
                                        "使用重量：" +
                                        selected.getUsedWeight() +
                                        " kg\n\n" +
                                        "在庫へ戻しました。"
                                );

                                successAlert.showAndWait();


                            } else {

                                showError(
                                        "出庫取消に失敗しました。\n\n" +
                                        "すでに取消済、または元の在庫が" +
                                        "削除されている可能性があります。"
                                );
                            }
                        }
                    });
        });


        // =====================================
        // 更新ボタン
        // =====================================

        Button reloadButton =
                new Button("履歴を更新");

        reloadButton.setOnAction(e -> {

            table.setItems(
                    Database.loadShipmentHistory()
            );
        });


        // =====================================
        // 閉じるボタン
        // =====================================

        Button closeButton =
                new Button("閉じる");

        closeButton.setOnAction(
                e -> stage.close()
        );


        // =====================================
        // ボタン配置
        // =====================================

        HBox buttonArea =
                new HBox(
                        10,
                        cancelButton,
                        reloadButton,
                        closeButton
                );


        // =====================================
        // 全体レイアウト
        // =====================================

        VBox root =
                new VBox(15);

        root.setPadding(
                new Insets(20)
        );

        root.getChildren().addAll(
                title,
                table,
                buttonArea
        );


        // =====================================
        // Scene
        // =====================================

        Scene scene =
                new Scene(
                        root,
                        1150,
                        600
                );

        stage.setTitle(
                "出庫履歴"
        );

        stage.setScene(scene);

        stage.show();
    }


    // =========================================
    // エラー表示
    // =========================================

    private void showError(
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                "エラー"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }
}