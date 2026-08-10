package zaikoApp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Syukko {

    public void show() {

        Stage stage = new Stage();

        Label title = new Label("粉末出庫");
        title.setStyle(
                "-fx-font-size:24px; -fx-font-weight:bold;"
        );

        // 出庫する在庫を選択
        Label stockLabel =
                new Label("出庫する在庫");

        ComboBox<Product> stockCombo =
                new ComboBox<>();

        // 在庫が残っているものだけ表示
        for (Product product :
                InventoryData.getProducts()) {

            if (product.getWeight() > 0) {
                stockCombo.getItems().add(product);
            }
        }

        stockCombo.setPromptText(
                "在庫を選択"
        );

        stockCombo.setPrefWidth(400);

        // 粉末名
        Label powderLabel =
                new Label("粉末名");

        Label powderValue =
                new Label("-");

        // 現在の未開封数量
        Label currentQuantityLabel =
                new Label("現在の未開封数量");

        Label currentQuantityValue =
                new Label("-");

        // 現在重量
        Label currentWeightLabel =
                new Label("現在重量(kg)");

        Label currentWeightValue =
                new Label("-");

        // 使用数量
        Label quantityLabel =
                new Label("使用数量（袋・0可）");

        TextField quantityField =
                new TextField();

        quantityField.setPromptText(
                "開封済みから使用なら0"
        );

        // 使用重量
        Label weightLabel =
                new Label("使用重量(kg)");

        TextField weightField =
                new TextField();

        // 使用製品
        Label productLabel =
                new Label("使用製品");

        TextField productField =
                new TextField();

        // 出庫者
        Label operatorLabel =
                new Label("出庫者");

        ComboBox<Worker> operatorCombo =
                new ComboBox<>();

        operatorCombo.setItems(
                Database.loadWorkers()
        );

        operatorCombo.setPromptText(
                "出庫者を選択"
        );

        operatorCombo.setPrefWidth(200);

        Button shipmentButton =
                new Button("出庫");

        Button closeButton =
                new Button("閉じる");

        // 在庫を選択したとき
        stockCombo.setOnAction(e -> {

            Product selected =
                    stockCombo.getValue();

            if (selected != null) {

                powderValue.setText(
                        selected.getPowderName()
                );

                currentQuantityValue.setText(
                        String.valueOf(
                                selected.getQuantity()
                        )
                );

                currentWeightValue.setText(
                        String.valueOf(
                                selected.getWeight()
                        )
                );
            }
        });

        // 出庫
        shipmentButton.setOnAction(e -> {

            try {

                Product selected =
                        stockCombo.getValue();

                if (selected == null) {

                    showError(
                            "出庫する在庫を選択してください。"
                    );

                    return;
                }

                // 数量は空欄でも0
                String quantityText =
                        quantityField
                                .getText()
                                .trim();

                int usedQuantity;

                if (quantityText.isBlank()) {
                    usedQuantity = 0;
                } else {
                    usedQuantity =
                            Integer.parseInt(
                                    quantityText
                            );
                }

                if (usedQuantity < 0) {

                    showError(
                            "使用数量は0以上にしてください。"
                    );

                    return;
                }

                double usedWeight =
                        Double.parseDouble(
                                weightField
                                        .getText()
                                        .trim()
                        );

                if (usedWeight <= 0) {

                    showError(
                            "使用重量は0より大きい値にしてください。"
                    );

                    return;
                }

                String usedProduct =
                        productField
                                .getText()
                                .trim();

                if (usedProduct.isBlank()) {

                    showError(
                            "使用製品を入力してください。"
                    );

                    return;
                }

                Worker selectedWorker =
                        operatorCombo.getValue();

                if (selectedWorker == null) {

                    showError(
                            "出庫者を選択してください。"
                    );

                    return;
                }

                String operator =
                        selectedWorker.getName();

                String shipmentDate =
                        LocalDateTime.now()
                                .format(
                                        DateTimeFormatter.ofPattern(
                                                "yyyy/MM/dd HH:mm:ss"
                                        )
                                );

                boolean success =
                        Database.shipProduct(
                                selected.getId(),
                                selected.getLotNumber(),
                                selected.getPowderName(),
                                usedQuantity,
                                usedWeight,
                                usedProduct,
                                operator,
                                shipmentDate
                        );

                if (success) {

                    // SQLiteから最新データを再読込
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
                            "出庫しました。"
                    );

                    alert.showAndWait();

                    stage.close();

                } else {

                    showError(
                            "数量または重量が不足しています。"
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

        root.add(stockLabel, 0, 1);
        root.add(stockCombo, 1, 1);

        root.add(powderLabel, 0, 2);
        root.add(powderValue, 1, 2);

        root.add(currentQuantityLabel, 0, 3);
        root.add(currentQuantityValue, 1, 3);

        root.add(currentWeightLabel, 0, 4);
        root.add(currentWeightValue, 1, 4);

        root.add(quantityLabel, 0, 5);
        root.add(quantityField, 1, 5);

        root.add(weightLabel, 0, 6);
        root.add(weightField, 1, 6);

        root.add(productLabel, 0, 7);
        root.add(productField, 1, 7);

        root.add(operatorLabel, 0, 8);
        root.add(operatorCombo, 1, 8);

        root.add(shipmentButton, 0, 9);
        root.add(closeButton, 1, 9);

        Scene scene =
                new Scene(root, 700, 550);

        stage.setTitle("粉末出庫");
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