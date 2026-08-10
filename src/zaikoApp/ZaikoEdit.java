package zaikoApp;

import java.time.LocalDate;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ZaikoEdit {

    private Product product;

    public ZaikoEdit(Product product) {
        this.product = product;
    }

    public void show() {

        Stage stage = new Stage();

        Label title = new Label("在庫編集");
        title.setStyle(
                "-fx-font-size:24px; -fx-font-weight:bold;"
        );

     // 編集対象の在庫IDを保存
        int productId = product.getId();

        // ロット番号
        Label lotLabel =
                new Label("ロット番号");

        TextField lotField =
                new TextField(product.getLotNumber());

        // 粉末名
        Label powderLabel =
                new Label("粉末名");

        ComboBox<PowderMaster> powderCombo =
                new ComboBox<>();

        powderCombo.setItems(
                Database.loadPowderMasters()
        );

        powderCombo.setPrefWidth(200);

        // 現在の粉末を選択状態にする
        for (PowderMaster powder :
                powderCombo.getItems()) {

            if (powder.getPowderName()
                    .equals(product.getPowderName())) {

                powderCombo.setValue(powder);
                break;
            }
        }

        // メーカー
        Label makerLabel =
                new Label("メーカー");

        TextField makerField =
                new TextField(product.getMaker());

        makerField.setEditable(false);

        // 粉末変更時メーカー自動変更
        powderCombo.setOnAction(e -> {

            PowderMaster selected =
                    powderCombo.getValue();

            if (selected != null) {

                makerField.setText(
                        selected.getMaker()
                );
            }
        });

        // 未開封数量
        Label quantityLabel =
                new Label("未開封数量");

        TextField quantityField =
                new TextField(
                        String.valueOf(
                                product.getQuantity()
                        )
                );

        // 保管重量
        Label weightLabel =
                new Label("保管重量(kg)");

        TextField weightField =
                new TextField(
                        String.valueOf(
                                product.getWeight()
                        )
                );

        // 入庫日
        Label dateLabel =
                new Label("入庫日");

        DatePicker datePicker =
                new DatePicker();

        try {

            String date =
                    product.getStockDate()
                           .replace("/", "-");

            datePicker.setValue(
                    LocalDate.parse(date)
            );

        } catch (Exception e) {
            // 古いデータなどで日付を読み込めない場合
        }

        Button updateButton =
                new Button("更新");

        Button closeButton =
                new Button("キャンセル");

        // 更新
        updateButton.setOnAction(e -> {

            try {

                String newLotNumber =
                        lotField.getText().trim();

                PowderMaster selectedPowder =
                        powderCombo.getValue();

                if (newLotNumber.isBlank()) {

                    showError(
                            "ロット番号を入力してください。"
                    );

                    return;
                }

                if (selectedPowder == null) {

                    showError(
                            "粉末名を選択してください。"
                    );

                    return;
                }

                int quantity =
                        Integer.parseInt(
                                quantityField
                                        .getText()
                                        .trim()
                        );

                double weight =
                        Double.parseDouble(
                                weightField
                                        .getText()
                                        .trim()
                        );

                if (quantity < 0) {

                    showError(
                            "未開封数量は0以上にしてください。"
                    );

                    return;
                }

                if (weight < 0) {

                    showError(
                            "保管重量は0以上にしてください。"
                    );

                    return;
                }

                if (datePicker.getValue() == null) {

                    showError(
                            "入庫日を選択してください。"
                    );

                    return;
                }

                Product newProduct =
                        new Product(
                                newLotNumber,
                                selectedPowder
                                        .getPowderName(),
                                selectedPowder
                                        .getMaker(),
                                quantity,
                                weight,
                                datePicker
                                        .getValue()
                                        .toString()
                        );

                boolean success =
                        Database.updateProduct(
                                productId,
                                newProduct
                        );

                if (success) {

                    // 在庫一覧を最新状態にする
                    InventoryData
                            .getProducts()
                            .setAll(
                                    Database.loadProducts()
                            );

                    Alert alert =
                            new Alert(
                                    Alert.AlertType.INFORMATION
                            );

                    alert.setHeaderText(null);
                    alert.setContentText(
                            "在庫情報を更新しました。"
                    );

                    alert.showAndWait();

                    stage.close();

                } else {

                    showError(
                            "在庫情報を更新できませんでした。"
                    );
                }

            } catch (NumberFormatException ex) {

                showError(
                        "数量と重量には数字を入力してください。"
                );
            }
        });

        closeButton.setOnAction(
                e -> stage.close()
        );

        GridPane root =
                new GridPane();

        root.setPadding(
                new Insets(30)
        );

        root.setHgap(15);
        root.setVgap(15);

        root.add(title, 0, 0, 2, 1);

        root.add(lotLabel, 0, 1);
        root.add(lotField, 1, 1);

        root.add(powderLabel, 0, 2);
        root.add(powderCombo, 1, 2);

        root.add(makerLabel, 0, 3);
        root.add(makerField, 1, 3);

        root.add(quantityLabel, 0, 4);
        root.add(quantityField, 1, 4);

        root.add(weightLabel, 0, 5);
        root.add(weightField, 1, 5);

        root.add(dateLabel, 0, 6);
        root.add(datePicker, 1, 6);

        root.add(updateButton, 0, 7);
        root.add(closeButton, 1, 7);

        Scene scene =
                new Scene(root, 520, 480);

        stage.setTitle("在庫編集");
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