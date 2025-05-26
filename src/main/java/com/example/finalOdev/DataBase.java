/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.finalOdev;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author hp
 */
public class DataBase {

    private String in_url;
    private String in_user;
    private String in_password;

    private Connection conn;

    private final String url = "jdbc:mysql://localhost:3306/projectmanager";
    private final String user = "root";
    private final String password = "yakup123";

    boolean connect(userProduct main_user) {
        String sql = "SELECT * FROM cafes WHERE cafe_id = ? AND cafe_username = ? AND cafe_password = ?";
        try (Connection root_conn = DriverManager.getConnection(url, user, password); PreparedStatement pstmt = root_conn.prepareStatement(sql)) {

            pstmt.setInt(1, main_user.id);
            pstmt.setString(2, main_user.username);
            pstmt.setString(3, main_user.password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Kullanıcı doğrulandı.");
                    in_user = main_user.username;
                    in_password = main_user.password;
                    in_url = "jdbc:mysql://localhost:3306/" + rs.getString("cafe_name");
                    System.out.println(in_url);
                    root_conn.close();
                    connectDB();
                    return true;

                } else {
                    System.out.println("Kullanıcı bulunamadı.");
                    root_conn.close();
                    return false;
                }
            }

        } catch (SQLException e) {
            System.out.println("com.example.finalOdev.DataBase.connect error");
        }
        return false;
    }

    private void connectDB() {
        System.out.println(in_url);

        try {
            conn = DriverManager.getConnection(in_url, in_user, in_password);
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public int getadisyonId(int tableId) {
        String query = "SELECT adisyon_id FROM adisyon WHERE tables_table_id = ? and adisyon_open=1;";
        try (PreparedStatement pqstmt = conn.prepareStatement(query)) {
            pqstmt.setInt(1, tableId);

            try (ResultSet rs = pqstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("adisyon_id");
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public tableProduct[] getTables() {

        List<tableProduct> tableList = new ArrayList<>();

        String query = """
                        SELECT 
                            t.table_id AS tables_table_id, 
                            t.table_name,
                            t.table_free,
                            COALESCE(SUM(i.item_price * (o.quantity - o.payed)), 0) AS toplam_odeme
                        FROM 
                            tables t
                        LEFT JOIN 
                            adisyon a ON t.table_id = a.tables_table_id AND adisyon_open=1
                        LEFT JOIN 
                            orders o ON a.adisyon_id = o.adisyon_id
                        LEFT JOIN 
                            items i ON o.item_id = i.item_id
                        WHERE 
                            (o.payed < o.quantity OR o.payed IS NULL)
                        GROUP BY 
                            t.table_id, t.table_name, t.table_free
                        ORDER BY 
                            t.table_name ASC;""";
        connectDB();

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                tableProduct table = new tableProduct();
                table.id = rs.getInt("tables_table_id");
                table.name = rs.getString("table_name");
                table.free = rs.getString("table_free");
                table.price = rs.getFloat("toplam_odeme");
                tableList.add(table);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return tableList.toArray(new tableProduct[0]);
    }

    public void newUser(String isim, String soyisim, String parola, String kullaniciadi, String gyeri) {

        String sql = "INSERT INTO `users` (`user_fname`, `user_lname`, `user_username`, `user_password`, `user_type`) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, isim);
            pstmt.setString(2, soyisim);
            pstmt.setString(3, kullaniciadi);
            pstmt.setString(5, gyeri);
            pstmt.setString(4, parola);

            int rowsInserted = pstmt.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("Kullanıcı başarıyla eklendi.");

            } else {
                System.out.println("Kullanıcı eklenemedi.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void newItem(String urun_ismi, String urun_fiyati, int type_id) {

        String sql = "INSERT INTO items (item_name, item_price,type_id) VALUES (?, ?,?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, urun_ismi);
            pstmt.setString(2, urun_fiyati);
            pstmt.setInt(3, type_id);

            int rowsInserted = pstmt.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("Ürün başarıyla eklendi.");
            } else {
                System.out.println("Ürün eklenemedi.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delItem(int id) {
        String sql = "UPDATE items SET is_deleted = TRUE WHERE item_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int silinen = pstmt.executeUpdate();

            if (silinen > 0) {
                System.out.println("Ürün başarıyla silindi.");
            } else {
                System.out.println("Ürün silinemedi.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void showItems(JTable tablo) {
        String query = "SELECT * FROM items WHERE is_deleted=FALSE";

        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Ürün İsmi");
            model.addColumn("Ürün Fiyatı");

            while (rs.next()) {
                Object[] row = new Object[3];
                row[0] = rs.getInt("item_id");
                row[1] = rs.getString("item_name");
                row[2] = rs.getDouble("item_price");
                model.addRow(row);
            }

            tablo.setModel(model);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
            }
        }
    }

    public itemProduct[] getOrderItems(int tableId) {
        int adisyonId = getadisyonId(tableId);
        List<itemProduct> items = new ArrayList<>();

        String sql = """
                        SELECT orders.orders_id, items.item_name, items.item_price,
                               orders.quantity, orders.payed
                        FROM orders
                        INNER JOIN items ON orders.item_id = items.item_id
                        WHERE adisyon_id = ?;
                        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, adisyonId);
            try (ResultSet rs2 = pstmt.executeQuery()) {
                while (rs2.next()) {
                    itemProduct item = new itemProduct();
                    item.id = rs2.getInt("orders_id");
                    item.name = rs2.getString("item_name");
                    item.price = rs2.getFloat("item_price");
                    item.payed = rs2.getInt("payed");
                    item.quantity = rs2.getInt("quantity");
                    items.add(item);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items.toArray(new itemProduct[0]); // ❗ null yerine boş dizi
    }

    public void pay(ArrayList<itemProduct> itemlist) {
        String sql = "UPDATE orders SET payed = ? WHERE orders_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (itemProduct item : itemlist) {
                pstmt.setInt(1, item.payed); // veya item.getPayed() varsa
                pstmt.setInt(2, item.id); // veya item.getId()
                pstmt.executeUpdate();
            }
            itemlist.clear();

        } catch (SQLException e) {
            e.printStackTrace(); // daha iyi hata yönetimi için loglama tercih edilebilir
        }
    }

    public boolean payed(int tableId) {
        int adisyonId = getadisyonId(tableId);

        String sql = "SELECT `payed`,`quantity` FROM `orders` WHERE `adisyon_id`=?; ";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, adisyonId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                if (rs.getInt("payed") < rs.getInt("quantity")) {
                    return false;
                }
            }
            return true; // Tüm siparişler ödenmişse true döner
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Hata durumunda false döner
        }
    }

    public void closeAdisyon(int tableId) {
        int adisyonId = getadisyonId(tableId);
        String sql = "UPDATE `adisyon` SET `adisyon_close_time`=?,`adisyon_open`=? WHERE `adisyon_id`=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(2, 0);
            pstmt.setInt(3, adisyonId);
            pstmt.executeUpdate();
            pstmt.close();
            String sql2 = "UPDATE `tables` SET `table_free`='free' WHERE `table_id`=?";

            try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                stmt.setInt(1, tableId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addtable(String tableName) {

        String sql = "INSERT INTO `tables`( `table_name`) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getUser(List<users> userList) {
        userList.clear();

        String sql = "SELECT user_id, user_fname,user_lname,user_type FROM users WHERE is_fired = FALSE";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql);) {
            while (rs.next()) {
                users users = new users();
                users.id = rs.getInt("user_id");
                users.isim = rs.getString("user_fname");
                users.soyisim = rs.getString("user_lname");
                users.gyeri = rs.getString("user_type");
                userList.add(users);
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    public void delUser(int user_id) {
        String sql = "UPDATE users SET is_fired = TRUE WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user_id);
            int silinen = pstmt.executeUpdate();

            if (silinen > 0) {
                System.out.println("kullanici silindi.");
            } else {
                System.out.println("kullanici silinemedi.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void newType(String type_name) {
        String sql = "INSERT INTO item_type (type_name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type_name);
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("kategori başarıyla eklendi.");
            } else {
                System.out.println("kategori eklenemedi.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void showType(JTable tablo) {
        String query = "SELECT * FROM item_type WHERE is_deleted=FALSE";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Kategori İsmi");

            while (rs.next()) {
                Object[] row = new Object[3];
                row[0] = rs.getInt("type_id");
                row[1] = rs.getString("type_name");

                model.addRow(row);
            }

            tablo.setModel(model);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delType(int type_id) {
        String sql = "UPDATE item_type SET is_fired = TRUE WHERE type_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, type_id);
            int silinen = pstmt.executeUpdate();

            if (silinen > 0) {
                System.out.println("kategori silindi.");
            } else {
                System.out.println("kategori silinemedi.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getType(List<typeProduct> typeList) {
        typeList.clear();
        String query = "SELECT * FROM item_type WHERE is_deleted=FALSE";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Kategori İsmi");

            while (rs.next()) {
                typeProduct type = new typeProduct();
                type.id = rs.getInt("type_id");
                type.name = rs.getString("type_name");
                typeList.add(type);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
