package zaikoApp;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ReceiptHistory {

    // ★在庫ID
    private final SimpleIntegerProperty inventoryId;

    private final SimpleStringProperty stockDate;
    private final SimpleStringProperty lotNumber;
    private final SimpleStringProperty powderName;
    private final SimpleStringProperty maker;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty weight;

    // ★在庫IDを含めて7個受け取る
    public ReceiptHistory(
            int inventoryId,
            String stockDate,
            String lotNumber,
            String powderName,
            String maker,
            int quantity,
            double weight) {

        this.inventoryId =
                new SimpleIntegerProperty(inventoryId);

        this.stockDate =
                new SimpleStringProperty(stockDate);

        this.lotNumber =
                new SimpleStringProperty(lotNumber);

        this.powderName =
                new SimpleStringProperty(powderName);

        this.maker =
                new SimpleStringProperty(maker);

        this.quantity =
                new SimpleIntegerProperty(quantity);

        this.weight =
                new SimpleDoubleProperty(weight);
    }

    public int getInventoryId() {
        return inventoryId.get();
    }

    public String getStockDate() {
        return stockDate.get();
    }

    public String getLotNumber() {
        return lotNumber.get();
    }

    public String getPowderName() {
        return powderName.get();
    }

    public String getMaker() {
        return maker.get();
    }

    public int getQuantity() {
        return quantity.get();
    }

    public double getWeight() {
        return weight.get();
    }
}