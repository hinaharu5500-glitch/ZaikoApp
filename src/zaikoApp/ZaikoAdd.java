package zaikoApp;

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

public class ZaikoAdd {

    public void show() {

        Stage stage = new Stage();

        Label title = new Label("入庫");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label lotLabel = new Label("ロット番号");
        TextField lotField = new TextField();

        Label powderLabel = new Label("粉末名");

        ComboBox<PowderMaster> powderCombo = new ComboBox<>();

        powderCombo.setItems(
                Database.loadPowderMasters()
        );
       
        powderCombo.setPromptText("粉末を選択");
        powderCombo.setPrefWidth(200);

        Label makerLabel = new Label("メーカー");
        TextField makerField = new TextField();
        makerField.setEditable(false);
        powderCombo.setOnAction(e -> {

            PowderMaster selectedPowder =
                    powderCombo.getValue();

            if (selectedPowder != null) {

                makerField.setText(
                        selectedPowder.getMaker()
                );
            }
        });

        Label quantityLabel = new Label("未開封数量");
        TextField quantityField = new TextField();

        Label weightLabel = new Label("保管重量(kg)");
        TextField weightField = new TextField();

        Label dateLabel = new Label("入庫日");
        DatePicker datePicker = new DatePicker();

        Button registerButton = new Button("入庫登録");

        registerButton.setOnAction(e -> {

            try {

                // ① ロット番号を取得
                String lotNumber = lotField.getText().trim();

                // ロット番号が空欄なら登録しない
                if (lotNumber.isBlank()) {
                    showError("ロット番号を入力してください。");
                    return;
                }


                // ② 粉末マスターから選択した粉末を取得
                PowderMaster selectedPowder =
                        powderCombo.getValue();

                // 粉末を選んでいなければ登録しない
                if (selectedPowder == null) {
                    showError("粉末名を選択してください。");
                    return;
                }


                // ③ 選択した粉末から粉末名とメーカーを取得
                String powderName =
                        selectedPowder.getPowderName();

                String maker =
                        selectedPowder.getMaker();


                // ④ 未開封数量を取得
                int quantity =
                        Integer.parseInt(
                                quantityField.getText().trim()
                        );

                // マイナスは禁止
                if (quantity < 0) {
                    showError("未開封数量は0以上にしてください。");
                    return;
                }


                // ⑤ 保管重量を取得
                double weight =
                        Double.parseDouble(
                                weightField.getText().trim()
                        );

                // 0kg以下は禁止
                if (weight <= 0) {
                    showError("保管重量は0より大きい値にしてください。");
                    return;
                }


                // ⑥ 入庫日チェック
                if (datePicker.getValue() == null) {
                    showError("入庫日を選択してください。");
                    return;
                }

                String stockDate =
                        datePicker.getValue().toString();


                // ⑦ Productを作成
                Product product = new Product(
                        lotNumber,
                        powderName,
                        maker,
                        quantity,
                        weight,
                        stockDate
                );


                // ⑧ 現在庫へ保存
                // SQLiteが自動で作ったIDを受け取る
                int inventoryId =
                        Database.saveProduct(product);


                // 保存できなかった場合
                if (inventoryId == -1) {
                    showError("入庫の保存に失敗しました。");
                    return;
                }


                // ⑨ 同じIDを入庫履歴にも保存
                Database.saveReceiptHistory(
                        inventoryId,
                        product
                );


                // ⑩ DBから在庫を読み直す
                InventoryData.getProducts().setAll(
                        Database.loadProducts()
                );


                System.out.println(
                        "在庫を登録しました：" +
                        powderName +
                        " / 在庫ID：" +
                        inventoryId
                );


                // ⑪ 完了メッセージ
                Alert alert =
                        new Alert(
                                Alert.AlertType.INFORMATION
                        );

                alert.setHeaderText(null);

                alert.setContentText(
                        "入庫登録しました。\n" +
                        "在庫ID：" + inventoryId
                );

                alert.showAndWait();


                // ⑫ 入庫画面を閉じる
                stage.close();


            } catch (NumberFormatException ex) {

                showError(
                        "未開封数量と保管重量には数字を入力してください。"
                );
            }
        });
        
        Button closeButton = new Button("閉じる");

        closeButton.setOnAction(e -> stage.close());

        GridPane root = new GridPane();

        root.setPadding(new Insets(30));
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

        root.add(registerButton, 0, 7);
        root.add(closeButton, 1, 7);

        Scene scene = new Scene(root, 500, 450);

        stage.setTitle("入庫");
        stage.setScene(scene);
        stage.show();
    }
    private void showError(String message) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("入力エラー");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    }