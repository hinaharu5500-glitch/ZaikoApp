package zaikoApp;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ShipmentHistory {

    private final SimpleIntegerProperty id;
    private final SimpleIntegerProperty inventoryId;

    private final SimpleStringProperty shipmentDate;
    private final SimpleStringProperty lotNumber;
    private final SimpleStringProperty powderName;

    private final SimpleIntegerProperty usedQuantity;
    private final SimpleDoubleProperty usedWeight;

    private final SimpleStringProperty usedProduct;
    private final SimpleStringProperty operator;

    // ★追加
    private final SimpleIntegerProperty cancelled;


    public ShipmentHistory(
            int id,
            int inventoryId,
            String shipmentDate,
            String lotNumber,
            String powderName,
            int usedQuantity,
            double usedWeight,
            String usedProduct,
            String operator,
            int cancelled) {

        this.id =
                new SimpleIntegerProperty(id);

        this.inventoryId =
                new SimpleIntegerProperty(inventoryId);

        this.shipmentDate =
                new SimpleStringProperty(shipmentDate);

        this.lotNumber =
                new SimpleStringProperty(lotNumber);

        this.powderName =
                new SimpleStringProperty(powderName);

        this.usedQuantity =
                new SimpleIntegerProperty(usedQuantity);

        this.usedWeight =
                new SimpleDoubleProperty(usedWeight);

        this.usedProduct =
                new SimpleStringProperty(usedProduct);

        this.operator =
                new SimpleStringProperty(operator);

        // ★追加
        this.cancelled =
                new SimpleIntegerProperty(cancelled);
    }


    public int getId() {
        return id.get();
    }

    public int getInventoryId() {
        return inventoryId.get();
    }

    public String getShipmentDate() {
        return shipmentDate.get();
    }

    public String getLotNumber() {
        return lotNumber.get();
    }

    public String getPowderName() {
        return powderName.get();
    }

    public int getUsedQuantity() {
        return usedQuantity.get();
    }

    public double getUsedWeight() {
        return usedWeight.get();
    }

    public String getUsedProduct() {
        return usedProduct.get();
    }

    public String getOperator() {
        return operator.get();
    }

    // ★追加
    public int getCancelled() {
        return cancelled.get();
    }

    // ★取消済か確認するため
    public boolean isCancelled() {
        return cancelled.get() == 1;
    }

    // ★画面表示用
    public String getStatus() {

        if (cancelled.get() == 1) {
            return "取消済";
        }

        return "出庫";
    }
}