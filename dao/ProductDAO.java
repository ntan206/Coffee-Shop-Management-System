package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import connectdb.DB;
import entity.Product;

public class ProductDAO {

    public List<Product> findAll() throws Exception {
        String sql = "SELECT * FROM products ORDER BY id DESC";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return map(rs);
        }
    }

    public List<Product> findAllActive() throws Exception {
        String sql = "SELECT * FROM products WHERE active=1 ORDER BY name";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return map(rs);
        }
    }

    public void insert(Product p) throws Exception {
    	// Thêm cột image vào câu lệnh INSERT
        String sql = "INSERT INTO products(code,name,category,price,active,image) VALUES(?,?,?,?,?,?)";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.code);
            ps.setString(2, p.name);
            ps.setString(3, p.category);
            ps.setBigDecimal(4, p.price);
            ps.setBoolean(5, p.active);
            ps.setString(6, p.image); // Lưu link ảnh vào cột thứ 6
            ps.executeUpdate();
        }
    }

    public void update(Product p) throws Exception {
    	// Thêm set image=? vào câu lệnh UPDATE
        String sql = "UPDATE products SET name=?, category=?, price=?, active=?, image=? WHERE id=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.name);
            ps.setString(2, p.category);
            ps.setBigDecimal(3, p.price);
            ps.setBoolean(4, p.active);
            ps.setString(5, p.image); // Cập nhật link ảnh mới
            ps.setInt(6, p.id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws Exception {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private List<Product> map(ResultSet rs) throws Exception {
        List<Product> list = new ArrayList<>();
        while (rs.next()) {
            Product p = new Product();
            p.id = rs.getInt("id");
            p.code = rs.getString("code");
            p.name = rs.getString("name");
            p.category = rs.getString("category");
            p.price = rs.getBigDecimal("price");
            p.active = rs.getBoolean("active");
         // Thêm dòng này để lấy link ảnh từ cột 'image' trong SQL
            p.image = rs.getString("image");
            list.add(p);
        }
        return list;
    }
}