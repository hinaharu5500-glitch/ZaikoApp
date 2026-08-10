package zaikoApp;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // データベース準備
    	Database.initializeDatabase();

    	InventoryData.getProducts().setAll(
    	        Database.loadProducts()
    	);

    	// 在庫不足表示
    	Label lowStockLabel = new Label();
    	lowStockLabel.setStyle(
    	        "-fx-font-size:16px;" +
    	        "-fx-font-weight:bold;" +
    	        "-fx-cursor:hand;"
    	);

    	Runnable updateStockAlert = () -> {

    	    long lowStockCount =
    	            InventoryData.getProducts()
    	                    .stream()
    	                    .filter(product ->
    	                            product.getWeight() > 0
    	                            && product.getWeight() <= 10
    	                    )
    	                    .count();

    	    long zeroStockCount =
    	            InventoryData.getProducts()
    	                    .stream()
    	                    .filter(product ->
    	                            product.getWeight() <= 0
    	                    )
    	                    .count();

    	    if (lowStockCount == 0 &&
    	            zeroStockCount == 0) {

    	        lowStockLabel.setText(
    	                "在庫状態：問題ありません"
    	        );

    	    } else {

    	        lowStockLabel.setText(
    	                "残量10kg以下：" +
    	                lowStockCount +
    	                "件　　在庫切れ：" +
    	                zeroStockCount +
    	                "件"
    	        );
    	    }
    	};

    	InventoryData.getProducts().addListener(
    	        (ListChangeListener<Product>) change -> {
    	            updateStockAlert.run();
    	        }
    	);

    	updateStockAlert.run();
    	
    	lowStockLabel.setOnMouseClicked(e -> {

    	    ZaikoList zaikoList =
    	            new ZaikoList();

    	    zaikoList.showLowStock();
    	});

    	Label title =
    	        new Label("粉末在庫管理システム");

    	title.setStyle(
    	        "-fx-font-size:24px;" +
    	        "-fx-font-weight:bold;"
    	);
    	
    	Label versionLabel =
    	        new Label(
    	                "Version " + "1.0.0"
    	        );

    	versionLabel.setStyle(
    	        "-fx-font-size:11px;" +
    	        "-fx-text-fill:#777777;"
    	);
    	
        Button listButton = new Button("在庫一覧");
        Button inButton = new Button("入庫");
        Button outButton = new Button("出庫");
        Button receiptHistoryButton = new Button("入庫履歴");
        Button historyButton = new Button("出庫履歴");
        Button masterButton =new Button("粉末マスター");
        Button workerButton =new Button("作業者マスター");
        Button endButton = new Button("終了");

        // 在庫一覧
        listButton.setOnAction(e -> {

            try {
            	ZaikoList zaikoList = new ZaikoList();
            	zaikoList.show();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // 入庫
        inButton.setOnAction(e -> {

            ZaikoAdd zaikoAdd = new ZaikoAdd();
            zaikoAdd.show();

        });

        // 出庫
        outButton.setOnAction(e -> {

            Syukko syukko = new Syukko();
            syukko.show();

        });
        
        //入庫履歴
        receiptHistoryButton.setOnAction(e -> {
            NyukoHistory history = new NyukoHistory();
            history.show();
            receiptHistoryButton.setPrefWidth(200);
        });
        
        //出庫履歴
        historyButton.setOnAction(e -> {

            SyukkoHistory history = new SyukkoHistory();
            history.show();
            historyButton.setPrefWidth(200);

        });
        
        //粉末マスター
        masterButton.setOnAction(e -> {

            PowderMasterScreen screen =
            new PowderMasterScreen();
            screen.show();
            masterButton.setPrefWidth(200);
        });
        
        //作業者マスター
        workerButton.setOnAction(e -> {

            WorkerMasterScreen screen =
            new WorkerMasterScreen();
            screen.show();
            workerButton.setPrefWidth(200);
        });

        // 終了
        endButton.setOnAction(e -> stage.close());

        listButton.setPrefWidth(200);
        inButton.setPrefWidth(200);
        outButton.setPrefWidth(200);
        endButton.setPrefWidth(200);

        VBox root = new VBox(20);

        root.setAlignment(Pos.CENTER);

        root.getChildren().addAll(
                title,
                versionLabel,
                lowStockLabel,
                listButton,
                inButton,
                outButton,
                receiptHistoryButton,
                historyButton,
                masterButton,
                workerButton,
                endButton
        );
        
        Scene scene = new Scene(root, 600, 500);

        stage.setTitle(
        		"粉末在庫管理システム" + " - Version " + "1.0.0"
        );
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}