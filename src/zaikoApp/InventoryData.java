package zaikoApp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InventoryData {

    private static final ObservableList<Product> products =
            FXCollections.observableArrayList();

    public static ObservableList<Product> getProducts() {
        return products;
    }
}