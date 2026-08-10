package zaikoApp;

import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ZaikoList {

    public void show() {

        Stage stage = new Stage();

        // =========================
        // タイトル
        // =========================

        Label title = new Label("粉末 在庫一覧");

        title.setStyle(
                "-fx-font-size: 26px;" +
                "-fx-font-weight: bold;"
        );


        // =========================
        // 検索
        // =========================

        TextField searchField = new TextField();

        searchField.setPromptText(
                "ロット番号・粉末名・メーカーで検索"
        );

        searchField.setPrefWidth(400);


        // 在庫切れ表示
        CheckBox showZeroCheck =
                new CheckBox("在庫切れも表示");


        HBox searchArea = new HBox(
                15,
                searchField,
                showZeroCheck
        );

        searchArea.setAlignment(
                Pos.CENTER_LEFT
        );


        // =========================
        // TableView
        // =========================

        TableView<Product> table =
                new TableView<>();


        // ID
        TableColumn<Product, Integer> id =
                new TableColumn<>("在庫ID");

        id.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        id.setPrefWidth(70);


        // ロット番号
        TableColumn<Product, String> lotNumber =
                new TableColumn<>("ロット番号");

        lotNumber.setCellValueFactory(
                new PropertyValueFactory<>("lotNumber")
        );

        lotNumber.setPrefWidth(140);


        // 粉末名
        TableColumn<Product, String> powderName =
                new TableColumn<>("粉末名");

        powderName.setCellValueFactory(
                new PropertyValueFactory<>("powderName")
        );

        powderName.setPrefWidth(160);


        // メーカー
        TableColumn<Product, String> maker =
                new TableColumn<>("メーカー");

        maker.setCellValueFactory(
                new PropertyValueFactory<>("maker")
        );

        maker.setPrefWidth(160);


        // 未開封数量
        TableColumn<Product, Integer> quantity =
                new TableColumn<>("未開封数量");

        quantity.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );

        quantity.setPrefWidth(110);


        // 重量
        TableColumn<Product, Double> weight =
                new TableColumn<>("保管重量(kg)");

        weight.setCellValueFactory(
                new PropertyValueFactory<>("weight")
        );

        weight.setPrefWidth(120);


        // 入庫日
        TableColumn<Product, String> stockDate =
                new TableColumn<>("入庫日");

        stockDate.setCellValueFactory(
                new PropertyValueFactory<>("stockDate")
        );

        stockDate.setPrefWidth(120);


        table.getColumns().add(id);
        table.getColumns().add(lotNumber);
        table.getColumns().add(powderName);
        table.getColumns().add(maker);
        table.getColumns().add(quantity);
        table.getColumns().add(weight);
        table.getColumns().add(stockDate);


        // データがない場合
        table.setPlaceholder(
                new Label("該当する在庫はありません")
        );


        // =========================
        // 検索用FilteredList
        // =========================

        FilteredList<Product> filteredList =
                new FilteredList<>(
                        InventoryData.getProducts(),
                        p -> true
                );


        Runnable updateFilter = () -> {

            String keyword =
                    searchField
                            .getText()
                            .trim()
                            .toLowerCase();

            filteredList.setPredicate(product -> {

                // 在庫切れを通常は非表示
                if (!showZeroCheck.isSelected()
                        && product.getWeight() <= 0) {

                    return false;
                }

                // 検索欄が空なら表示
                if (keyword.isBlank()) {
                    return true;
                }

                // ロット番号
                if (product.getLotNumber()
                        .toLowerCase()
                        .contains(keyword)) {

                    return true;
                }

                // 粉末名
                if (product.getPowderName()
                        .toLowerCase()
                        .contains(keyword)) {

                    return true;
                }

                // メーカー
                if (product.getMaker() != null
                        && product.getMaker()
                        .toLowerCase()
                        .contains(keyword)) {

                    return true;
                }

                return false;
            });
        };


        searchField.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateFilter.run()
                );


        showZeroCheck.selectedProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updateFilter.run()
                );


        updateFilter.run();

        table.setItems(filteredList);


        // =========================
        // 在庫状態を色分け
        // =========================

        table.setRowFactory(tv ->
                new TableRow<Product>() {

                    @Override
                    protected void updateItem(
                            Product product,
                            boolean empty) {

                        super.updateItem(
                                product,
                                empty
                        );

                        if (empty || product == null) {

                            setStyle("");

                        } else if (
                                product.getWeight() <= 0) {

                            // 在庫切れ
                            setStyle(
                                    "-fx-background-color: #dddddd;"
                            );

                        } else if (
                                product.getWeight() <= 10) {

                            // 残量10kg以下
                            setStyle(
                                    "-fx-background-color: #fff3cd;"
                            );

                        } else {

                            setStyle("");
                        }
                    }
                }
        );


        // =========================
        // 在庫集計
        // =========================

        Label summaryLabel =
                new Label();


        Runnable updateSummary = () -> {

            int count =
                    filteredList.size();

            double totalWeight =
                    filteredList
                            .stream()
                            .mapToDouble(
                                    Product::getWeight
                            )
                            .sum();

            summaryLabel.setText(
                    "表示件数：" +
                    count +
                    "件　　総保管重量：" +
                    String.format(
                            "%.2f kg",
                            totalWeight
                    )
            );
        };


        filteredList.addListener(
                (ListChangeListener<Product>) change ->
                        updateSummary.run()
        );

        updateSummary.run();


        // =========================
        // 編集ボタン
        // =========================

        Button editButton =
                new Button("選択した在庫を編集");


        editButton.setOnAction(e -> {

            Product selected =
                    table
                            .getSelectionModel()
                            .getSelectedItem();

            if (selected == null) {

                showError(
                        "編集する在庫を選択してください。"
                );

                return;
            }

            ZaikoEdit edit =
                    new ZaikoEdit(selected);

            edit.show();
        });


        // =========================
        // 削除ボタン
        // =========================

        Button deleteButton =
                new Button("選択した在庫を削除");


        deleteButton.setOnAction(e -> {

            Product selected =
                    table
                            .getSelectionModel()
                            .getSelectedItem();

            if (selected == null) {

                showError(
                        "削除する在庫を選択してください。"
                );

                return;
            }


            Alert confirm =
                    new Alert(
                            Alert.AlertType.CONFIRMATION
                    );

            confirm.setTitle("削除確認");

            confirm.setHeaderText(
                    "この在庫を削除しますか？"
            );

            confirm.setContentText(
                    "在庫ID：" +
                    selected.getId() +

                    "\nロット番号：" +
                    selected.getLotNumber() +

                    "\n粉末名：" +
                    selected.getPowderName()
            );


            confirm.showAndWait()
                    .ifPresent(button -> {

                        if (button ==
                                ButtonType.OK) {

                            boolean success =
                                    Database.deleteProduct(
                                            selected.getId()
                                    );

                            if (success) {

                                InventoryData
                                        .getProducts()
                                        .setAll(
                                                Database
                                                        .loadProducts()
                                        );

                            } else {

                                showError(
                                        "在庫の削除に失敗しました。"
                                );
                            }
                        }
                    });
        });


        // =========================
        // ボタン配置
        // =========================

        HBox buttonArea =
                new HBox(
                        15,
                        editButton,
                        deleteButton
                );

        buttonArea.setAlignment(
                Pos.CENTER_LEFT
        );


        // =========================
        // 全体レイアウト
        // =========================

        VBox root =
                new VBox(15);

        root.setPadding(
                new Insets(25)
        );

        root.getChildren().addAll(
                title,
                searchArea,
                summaryLabel,
                table,
                buttonArea
        );


        Scene scene =
                new Scene(
                        root,
                        1000,
                        600
                );


        stage.setTitle(
                "粉末在庫管理 - 在庫一覧"
        );

        stage.setScene(scene);
        stage.show();
    }

    public void showLowStock() {

        Stage stage = new Stage();

        Label title = new Label("在庫不足一覧");
        title.setStyle(
                "-fx-font-size: 26px;" +
                "-fx-font-weight: bold;"
        );

        TableView<Product> table =
                new TableView<>();

        // 在庫ID
        TableColumn<Product, Integer> id =
                new TableColumn<>("在庫ID");

        id.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        // ロット番号
        TableColumn<Product, String> lot =
                new TableColumn<>("ロット番号");

        lot.setCellValueFactory(
                new PropertyValueFactory<>("lotNumber")
        );

        // 粉末名
        TableColumn<Product, String> powder =
                new TableColumn<>("粉末名");

        powder.setCellValueFactory(
                new PropertyValueFactory<>("powderName")
        );

        // メーカー
        TableColumn<Product, String> maker =
                new TableColumn<>("メーカー");

        maker.setCellValueFactory(
                new PropertyValueFactory<>("maker")
        );

        // 未開封数量
        TableColumn<Product, Integer> quantity =
                new TableColumn<>("未開封数量");

        quantity.setCellValueFactory(
                new PropertyValueFactory<>("quantity")
        );

        // 重量
        TableColumn<Product, Double> weight =
                new TableColumn<>("保管重量(kg)");

        weight.setCellValueFactory(
                new PropertyValueFactory<>("weight")
        );

        // 入庫日
        TableColumn<Product, String> date =
                new TableColumn<>("入庫日");

        date.setCellValueFactory(
                new PropertyValueFactory<>("stockDate")
        );

        table.getColumns().add(id);
        table.getColumns().add(lot);
        table.getColumns().add(powder);
        table.getColumns().add(maker);
        table.getColumns().add(quantity);
        table.getColumns().add(weight);
        table.getColumns().add(date);

        // ★10kg以下だけ抽出
        FilteredList<Product> lowStockList =
                new FilteredList<>(
                        InventoryData.getProducts(),
                        product ->
                                product.getWeight() <= 10
                );

        table.setItems(lowStockList);

        // 色分け
        table.setRowFactory(tv ->
                new TableRow<Product>() {

                    @Override
                    protected void updateItem(
                            Product product,
                            boolean empty) {

                        super.updateItem(product, empty);

                        if (empty || product == null) {

                            setStyle("");

                        } else if (
                                product.getWeight() <= 0) {

                            setStyle(
                                    "-fx-background-color: #dddddd;"
                            );

                        } else {

                            setStyle(
                                    "-fx-background-color: #fff3cd;"
                            );
                        }
                    }
                }
        );

        Label countLabel =
                new Label(
                        "不足在庫：" +
                        lowStockList.size() +
                        "件"
                );

        Button closeButton =
                new Button("閉じる");

        closeButton.setOnAction(
                e -> stage.close()
        );

        VBox root =
                new VBox(15);

        root.setPadding(
                new Insets(25)
        );

        root.getChildren().addAll(
                title,
                countLabel,
                table,
                closeButton
        );

        Scene scene =
                new Scene(root, 1000, 600);

        stage.setTitle("在庫不足一覧");
        stage.setScene(scene);
        stage.show();
    } 

    private void showError(
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}