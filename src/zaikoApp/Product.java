package zaikoApp;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Product {

    private final SimpleIntegerProperty id;
    private final SimpleStringProperty lotNumber;
    private final SimpleStringProperty powderName;
    private final SimpleStringProperty maker;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty weight;
    private final SimpleStringProperty stockDate;

    // 新しく入庫するとき用
    public Product(
            String lotNumber,
            String powderName,
            String maker,
            int quantity,
            double weight,
            String stockDate) {

        this(
                0,
                lotNumber,
                powderName,
                maker,
                quantity,
                weight,
                stockDate
        );
    }

    // DBから読み込むとき用
    public Product(
            int id,
            String lotNumber,
            String powderName,
            String maker,
            int quantity,
            double weight,
            String stockDate) {

        this.id = new SimpleIntegerProperty(id);
        this.lotNumber = new SimpleStringProperty(lotNumber);
        this.powderName = new SimpleStringProperty(powderName);
        this.maker = new SimpleStringProperty(maker);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.weight = new SimpleDoubleProperty(weight);
        this.stockDate = new SimpleStringProperty(stockDate);
    }

    public int getId() {
        return id.get();
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

    public String getStockDate() {
        return stockDate.get();
    }

    @Override
    public String toString() {
        return "ID:" + getId()
                + " | " + getLotNumber()
                + " | " + getPowderName()
                + " | " + getStockDate()
                + " | " + getWeight() + "kg";
    }
}