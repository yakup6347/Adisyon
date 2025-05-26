/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.finalOdev;

import java.sql.Statement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JTable;

/**
 *
 * @author ZEYNEP
 */
class add {

    /**
     *
     * @author ZEYNEP
     */
    private String kullanici_adi = "root";
    private String parola = "yakup123";
    private String db_ismi = "mycafe";
    private String host = "localhost";
    private int port = 3306;

    private Connection con = null;

    public add() {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + db_ismi + "?useUnicode=true&characterEncoding=utf8";
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Driver dosyası bulunamadı.");
            return;
        }

        try {
            con = DriverManager.getConnection(url, kullanici_adi, parola);
            System.out.println("Bağlantı başarılı.");
        } catch (SQLException ex) {
            System.out.println("Veritabanına bağlanırken hata oluştu: " + ex.getMessage());
        }
    }

    public void kullaniciekleme(String urun_ismi, String urun_fiyati) {
        String sql = "INSERT INTO items (item_name, item_price) VALUES (?, ?)";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, urun_ismi);
            pstmt.setString(2, urun_fiyati);

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

    public void verileriTabloyaYazdir(JTable tablo) {
        String query = "SELECT * FROM items";

        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.createStatement();  // con: bağlantı tanımlı olmalı
            rs = stmt.executeQuery(query);

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Ürün İsmi");
            model.addColumn("Ürün Fiyatı");

            while (rs.next()) {
                Object[] row = new Object[3];
                row[0] = rs.getInt("item_id");  // Veritabanında gerçekten id isimli sütun olmalı
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

    public void urunSil(int id) {
        String sql = "DELETE FROM items WHERE item_id = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
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

}
