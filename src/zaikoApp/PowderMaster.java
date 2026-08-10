package zaikoApp;

import javafx.beans.property.SimpleStringProperty;

public class PowderMaster {

    private final SimpleStringProperty powderName;
    private final SimpleStringProperty maker;

    // ★コンストラクター
    public PowderMaster(String powderName, String maker) {
        this.powderName = new SimpleStringProperty(powderName);
        this.maker = new SimpleStringProperty(maker);
    }

    public String getPowderName() {
        return powderName.get();
    }

    public String getMaker() {
        return maker.get();
    }

    @Override
    public String toString() {
        return powderName.get();
    }
}