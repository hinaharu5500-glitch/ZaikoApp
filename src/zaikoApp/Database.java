package zaikoApp;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Database {

	private static final String DB_FOLDER =
	        System.getenv("LOCALAPPDATA")
	        + File.separator
	        + "ZaikoApp";

	private static final String URL =
	        "jdbc:sqlite:"
	        + DB_FOLDER
	        + File.separator
	        + "zaiko.db";
	
	public static Connection connect() throws SQLException {

	    File folder = new File(DB_FOLDER);

	    if (!folder.exists()) {
	        folder.mkdirs();
	    }

	    try {
	        Class.forName("org.sqlite.JDBC");
	    } catch (ClassNotFoundException e) {
	        throw new SQLException(
	                "SQLite JDBCドライバーが見つかりません",
	                e
	        );
	    }

	    System.out.println("DB接続先：" + URL);

	    return DriverManager.getConnection(URL);
	}

    // テーブルを作成
    public static void initializeDatabase() {

        String sql =
                "CREATE TABLE IF NOT EXISTS inventory (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "lot_number TEXT NOT NULL," +
                "powder_name TEXT NOT NULL," +
                "maker TEXT," +
                "quantity INTEGER NOT NULL," +
                "weight REAL NOT NULL," +
                "stock_date TEXT" +
                ")";

        String shipmentSql =
                "CREATE TABLE IF NOT EXISTS shipment_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "lot_number TEXT NOT NULL," +
                "powder_name TEXT NOT NULL," +
                "used_weight REAL NOT NULL," +
                "used_product TEXT NOT NULL," +
                "operator TEXT NOT NULL," +
                "shipment_date TEXT NOT NULL" +
                ")";
        String receiptSql =
                "CREATE TABLE IF NOT EXISTS receipt_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "lot_number TEXT NOT NULL," +
                "powder_name TEXT NOT NULL," +
                "maker TEXT," +
                "quantity INTEGER NOT NULL," +
                "weight REAL NOT NULL," +
                "stock_date TEXT NOT NULL" +
                ")";
        String powderMasterSql =
                "CREATE TABLE IF NOT EXISTS powder_master (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "powder_name TEXT NOT NULL UNIQUE," +
                "maker TEXT NOT NULL" +
                ")";
        String workerMasterSql =
                "CREATE TABLE IF NOT EXISTS worker_master (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "worker_name TEXT NOT NULL UNIQUE" +
                ")";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

        	stmt.execute(sql);
        	stmt.execute(shipmentSql);
        	stmt.execute(receiptSql);
        	stmt.execute(powderMasterSql);
        	stmt.execute(workerMasterSql);
        	
        	// ★既存DBにも新しい列を追加
        	if (!columnExists(
        	        conn,
        	        "receipt_history",
        	        "inventory_id")) {

        	    stmt.execute(
        	            "ALTER TABLE receipt_history " +
        	            "ADD COLUMN inventory_id INTEGER DEFAULT 0"
        	    );
        	}

        	if (!columnExists(
        	        conn,
        	        "receipt_history",
        	        "inventory_id")) {

        	    stmt.execute(
        	            "ALTER TABLE receipt_history " +
        	            "ADD COLUMN inventory_id INTEGER DEFAULT 0"
        	    );
        	}
        	
        	// ★出庫取消済フラグ
        	if (!columnExists(conn, "shipment_history", "cancelled")) {

        	    stmt.execute(
        	            "ALTER TABLE shipment_history " +
        	            "ADD COLUMN cancelled INTEGER DEFAULT 0"
        	    );
        	}
        	
            System.out.println("データベース準備完了");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 在庫を保存
    public static int saveProduct(Product product) {

        String sql =
                "INSERT INTO inventory " +
                "(lot_number, powder_name, maker, quantity, weight, stock_date) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getLotNumber());
            pstmt.setString(2, product.getPowderName());
            pstmt.setString(3, product.getMaker());
            pstmt.setInt(4, product.getQuantity());
            pstmt.setDouble(5, product.getWeight());
            pstmt.setString(6, product.getStockDate());

            pstmt.executeUpdate();

            // 今登録した在庫のIDを取得
            try (Statement stmt = conn.createStatement();
                 ResultSet rs =
                         stmt.executeQuery(
                                 "SELECT last_insert_rowid()"
                         )) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }
    
    // 在庫を読み込む
    public static ObservableList<Product> loadProducts() {

        ObservableList<Product> products =
                FXCollections.observableArrayList();

        String sql = "SELECT * FROM inventory";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

            	Product product = new Product(
            	        rs.getInt("id"),
            	        rs.getString("lot_number"),
            	        rs.getString("powder_name"),
            	        rs.getString("maker"),
            	        rs.getInt("quantity"),
            	        rs.getDouble("weight"),
            	        rs.getString("stock_date")
            	);

                products.add(product);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    // 出庫処理
    public static boolean shipProduct(
            int inventoryId,
            String lotNumber,
            String powderName,
            int usedQuantity,
            double usedWeight,
            String usedProduct,
            String operator,
            String shipmentDate) {

        String updateSql =
                "UPDATE inventory " +
                "SET quantity = quantity - ?, " +
                "weight = weight - ? " +
                "WHERE id = ? " +
                "AND quantity >= ? " +
                "AND weight >= ?";

        String historySql =
                "INSERT INTO shipment_history " +
                "(inventory_id, lot_number, powder_name, used_quantity, " +
                "used_weight, used_product, operator, shipment_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect()) {

            conn.setAutoCommit(false);

            // 在庫を減らす
            try (PreparedStatement updateStmt =
                         conn.prepareStatement(updateSql)) {

                updateStmt.setInt(1, usedQuantity);
                updateStmt.setDouble(2, usedWeight);

                // ★ロット番号ではなくID
                updateStmt.setInt(3, inventoryId);

                updateStmt.setInt(4, usedQuantity);
                updateStmt.setDouble(5, usedWeight);

                int updated = updateStmt.executeUpdate();

                if (updated == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 出庫履歴を保存
            try (PreparedStatement historyStmt =
                         conn.prepareStatement(historySql)) {

            	historyStmt.setInt(1, inventoryId);
            	historyStmt.setString(2, lotNumber);
            	historyStmt.setString(3, powderName);
            	historyStmt.setInt(4, usedQuantity);
            	historyStmt.setDouble(5, usedWeight);
            	historyStmt.setString(6, usedProduct);
            	historyStmt.setString(7, operator);
            	historyStmt.setString(8, shipmentDate);

                historyStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static ObservableList<ShipmentHistory> loadShipmentHistory() {

        ObservableList<ShipmentHistory> historyList =
                FXCollections.observableArrayList();

        String sql =
                "SELECT * FROM shipment_history ORDER BY id DESC";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

            	ShipmentHistory history =
            	        new ShipmentHistory(
            	                rs.getInt("id"),
            	                rs.getInt("inventory_id"),
            	                rs.getString("shipment_date"),
            	                rs.getString("lot_number"),
            	                rs.getString("powder_name"),
            	                rs.getInt("used_quantity"),
            	                rs.getDouble("used_weight"),
            	                rs.getString("used_product"),
            	                rs.getString("operator"),
            	                rs.getInt("cancelled")
            	        );

                historyList.add(history);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historyList;
    }
    public static void saveReceiptHistory(
            int inventoryId,
            Product product) {

        String sql =
                "INSERT INTO receipt_history " +
                "(inventory_id, lot_number, powder_name, maker, " +
                "quantity, weight, stock_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt =
                     conn.prepareStatement(sql)) {

            pstmt.setInt(
                    1,
                    inventoryId
            );

            pstmt.setString(
                    2,
                    product.getLotNumber()
            );

            pstmt.setString(
                    3,
                    product.getPowderName()
            );

            pstmt.setString(
                    4,
                    product.getMaker()
            );

            pstmt.setInt(
                    5,
                    product.getQuantity()
            );

            pstmt.setDouble(
                    6,
                    product.getWeight()
            );

            pstmt.setString(
                    7,
                    product.getStockDate()
            );

            pstmt.executeUpdate();

            System.out.println(
                    "入庫履歴を保存しました"
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static ObservableList<ReceiptHistory> loadReceiptHistory() {

        ObservableList<ReceiptHistory> historyList =
                FXCollections.observableArrayList();

        String sql =
                "SELECT * FROM receipt_history ORDER BY id DESC";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

            	ReceiptHistory history = new ReceiptHistory(
            	        rs.getInt("inventory_id"),
            	        rs.getString("stock_date"),
            	        rs.getString("lot_number"),
            	        rs.getString("powder_name"),
            	        rs.getString("maker"),
            	        rs.getInt("quantity"),
            	        rs.getDouble("weight")
            	);

                historyList.add(history);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historyList;
    }
    public static void savePowderMaster(
            String powderName,
            String maker) {

        String sql =
                "INSERT INTO powder_master " +
                "(powder_name, maker) VALUES (?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, powderName);
            pstmt.setString(2, maker);

            pstmt.executeUpdate();

            System.out.println("粉末マスターを登録しました");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static ObservableList<PowderMaster> loadPowderMasters() {

        ObservableList<PowderMaster> list =
                FXCollections.observableArrayList();

        String sql =
                "SELECT * FROM powder_master ORDER BY powder_name";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                PowderMaster powder = new PowderMaster(
                        rs.getString("powder_name"),
                        rs.getString("maker")
                );

                list.add(powder);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    } 
    public static void deletePowderMaster(String powderName) {

        String sql =
                "DELETE FROM powder_master WHERE powder_name = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, powderName);

            pstmt.executeUpdate();

            System.out.println("粉末マスターを削除しました");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void updatePowderMaster(
            String oldPowderName,
            String newPowderName,
            String maker) {

        String sql =
                "UPDATE powder_master " +
                "SET powder_name = ?, maker = ? " +
                "WHERE powder_name = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPowderName);
            pstmt.setString(2, maker);
            pstmt.setString(3, oldPowderName);

            pstmt.executeUpdate();

            System.out.println("粉末マスターを編集しました");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static boolean updateProduct(
            int id,
            Product product) {

        String sql =
                "UPDATE inventory SET " +
                "lot_number = ?, " +
                "powder_name = ?, " +
                "maker = ?, " +
                "quantity = ?, " +
                "weight = ?, " +
                "stock_date = ? " +
                "WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getLotNumber());
            pstmt.setString(2, product.getPowderName());
            pstmt.setString(3, product.getMaker());
            pstmt.setInt(4, product.getQuantity());
            pstmt.setDouble(5, product.getWeight());
            pstmt.setString(6, product.getStockDate());

            pstmt.setInt(7, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean deleteProduct(int id) {

        String sql =
                "DELETE FROM inventory WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    private static boolean columnExists(
            Connection conn,
            String tableName,
            String columnName) throws SQLException {

        String sql = "PRAGMA table_info(" + tableName + ")";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                String name = rs.getString("name");

                if (columnName.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }

        return false;
    }
    public static boolean saveWorker(String name) {

        String sql =
                "INSERT INTO worker_master " +
                "(worker_name) VALUES (?)";

        try (Connection conn = connect();
             PreparedStatement pstmt =
                     conn.prepareStatement(sql)) {

            pstmt.setString(1, name);

            pstmt.executeUpdate();

            System.out.println(
                    "作業者を登録しました：" + name
            );

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
         
        }
    }
 // 作業者一覧を読み込む
    public static ObservableList<Worker> loadWorkers() {

        ObservableList<Worker> list =
                FXCollections.observableArrayList();

        String sql =
                "SELECT * FROM worker_master " +
                "ORDER BY worker_name";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                Worker worker =
                        new Worker(
                                rs.getInt("id"),
                                rs.getString("worker_name")
                        );

                list.add(worker);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    public static boolean deleteWorker(int id) {

        String sql =
                "DELETE FROM worker_master WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt =
                     conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean updateWorker(
            int id,
            String newName) {

        String sql =
                "UPDATE worker_master " +
                "SET worker_name = ? " +
                "WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt =
                     conn.prepareStatement(sql)) {

            pstmt.setString(1, newName);
            pstmt.setInt(2, id);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 // ==============================
 // 出庫取消
 // ==============================
 public static boolean cancelShipment(int shipmentId) {

     Connection conn = null;

     try {

         conn = connect();

         // ★途中で失敗したら全部元に戻せるようにする
         conn.setAutoCommit(false);


         // =========================
         // 1. 出庫履歴を取得
         // =========================

         String selectSql =
                 "SELECT inventory_id, used_quantity, used_weight, cancelled " +
                 "FROM shipment_history " +
                 "WHERE id = ?";

         int inventoryId;
         int usedQuantity;
         double usedWeight;


         try (PreparedStatement pstmt =
                      conn.prepareStatement(selectSql)) {

             pstmt.setInt(1, shipmentId);

             try (ResultSet rs =
                          pstmt.executeQuery()) {

                 // 履歴が存在しない
                 if (!rs.next()) {

                     conn.rollback();
                     return false;
                 }


                 // すでに取消済
                 if (rs.getInt("cancelled") == 1) {

                     conn.rollback();
                     return false;
                 }


                 inventoryId =
                         rs.getInt("inventory_id");

                 usedQuantity =
                         rs.getInt("used_quantity");

                 usedWeight =
                         rs.getDouble("used_weight");
             }
         }


         // 古い履歴などで在庫IDがない場合
         if (inventoryId <= 0) {

             conn.rollback();
             return false;
         }


         // =========================
         // 2. 在庫を元に戻す
         // =========================

         String restoreSql =
                 "UPDATE inventory " +
                 "SET quantity = quantity + ?, " +
                 "weight = weight + ? " +
                 "WHERE id = ?";


         try (PreparedStatement pstmt =
                      conn.prepareStatement(restoreSql)) {

             pstmt.setInt(
                     1,
                     usedQuantity
             );

             pstmt.setDouble(
                     2,
                     usedWeight
             );

             pstmt.setInt(
                     3,
                     inventoryId
             );


             int updated =
                     pstmt.executeUpdate();


             // 在庫が削除されているなど
             if (updated == 0) {

                 conn.rollback();
                 return false;
             }
         }


         // =========================
         // 3. 履歴を取消済にする
         // =========================

         String cancelSql =
                 "UPDATE shipment_history " +
                 "SET cancelled = 1 " +
                 "WHERE id = ?";


         try (PreparedStatement pstmt =
                      conn.prepareStatement(cancelSql)) {

             pstmt.setInt(
                     1,
                     shipmentId
             );

             int updated =
                     pstmt.executeUpdate();


             if (updated == 0) {

                 conn.rollback();
                 return false;
             }
         }


         // =========================
         // 全部成功
         // =========================

         conn.commit();

         System.out.println(
                 "出庫取消完了：履歴ID " +
                 shipmentId
         );

         return true;


     } catch (SQLException e) {

         e.printStackTrace();

         if (conn != null) {

             try {

                 conn.rollback();

             } catch (SQLException ex) {

                 ex.printStackTrace();
             }
         }

         return false;


     } finally {

         if (conn != null) {

             try {

                 conn.setAutoCommit(true);
                 conn.close();

             } catch (SQLException e) {

                 e.printStackTrace();
                 
             }
         }
     }
 }
}