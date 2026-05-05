package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connectdb.DB;
import entity.User;


public class UserDAO {
    public User login(String username, String password, String requiredRole) throws Exception {
        String sql = """
            SELECT id, username, role, full_name
            FROM users
            WHERE username=? AND password_hash=? AND role=?
        """;

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, requiredRole);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("full_name")
                );
            }
        }
    }
    public boolean isUsernameExists(String username) throws Exception {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    public void insertEmployee(String username, String password, String fullName) throws Exception {
        String sql = "INSERT INTO users(username, password_hash, role, full_name) VALUES(?, ?, 'EMPLOYEE', ?)";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password); // Thực tế có thể mã hóa MD5/BCrypt tại đây
            ps.setString(3, fullName);
            ps.executeUpdate();
        }
    }
}