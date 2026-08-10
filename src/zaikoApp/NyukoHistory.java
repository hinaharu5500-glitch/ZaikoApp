package zaikoApp;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NyukoHistory {

    public void show() {

        Stage stage = new Stage();

        TableView<ReceiptHistory> table = new TableView<>();
        
        TableColumn<ReceiptHistory, Integer> inventoryId =
                new TableColumn<>("在庫ID");

        inventoryId.setCellValueFactory(
                new PropertyValueFactory<>("inventoryId"));

        TableColumn<ReceiptHistory, String> date =
                new TableColumn<>("入庫日");
        date.setCellValueFactory(
                new PropertyValueFactory<>("stockDate"));

        TableColumn<ReceiptHistory, String> lot =
                new TableColumn<>("ロット番号");
        lot.setCellValueFactory(
                new PropertyValueFactory<>("lotNumber"));

        TableColumn<ReceiptHistory, String> powder =
                new TableColumn<>("粉末名");
        powder.setCellValueFactory(
                new PropertyValueFactory<>("powderName"));

        TableColumn<ReceiptHistory, String> maker =
                new TableColumn<>("メーカー");
        maker.setCellValueFactory(
                new PropertyValueFactory<>("maker"));

        TableColumn<ReceiptHistory, Integer> quantity =
                new TableColumn<>("数量");
        quantity.setCellValueFactory(
                new PropertyValueFactory<>("quantity"));

        TableColumn<ReceiptHistory, Double> weight =
                new TableColumn<>("入庫重量(kg)");
        weight.setCellValueFactory(
                new PropertyValueFactory<>("weight"));

        table.getColumns().add(inventoryId);
        table.getColumns().add(date);
        table.getColumns().add(lot);
        table.getColumns().add(powder);
        table.getColumns().add(maker);
        table.getColumns().add(quantity);
        table.getColumns().add(weight);

        table.setItems(
                Database.loadReceiptHistory()
        );

        Button closeButton = new Button("閉じる");

        closeButton.setOnAction(e -> stage.close());

        VBox root = new VBox(10);

        root.getChildren().addAll(
                table,
                closeButton
        );

        Scene scene = new Scene(root, 900, 500);

        stage.setTitle("入庫履歴");
        stage.setScene(scene);
        stage.show();
    }
}