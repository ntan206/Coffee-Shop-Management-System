package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import connectdb.DB;
import entity.Promotion;

public class PromotionDAO {
	public void insert(Promotion p) throws Exception {
	    String sql = "INSERT INTO promotions(code, name, discount_percent, start_date, end_date) VALUES(?,?,?,?,?)";
	    try (Connection c = DB.getConnection();
	         PreparedStatement ps = c.prepareStatement(sql)) {
	        ps.setString(1, p.code);
	        ps.setString(2, p.name);
	        ps.setInt(3, p.discountPercent);
	        
	        ps.setTimestamp(4, Timestamp.valueOf(p.startDate));
	        ps.setTimestamp(5, Timestamp.valueOf(p.endDate));
	        
	        ps.executeUpdate();
	    }
	}
	// Thêm vào class PromotionDAO
	public Promotion findByCode(String code) throws Exception {
	    // Chỉ lấy mã nếu còn hạn sử dụng
	    String sql = "SELECT * FROM promotions WHERE code = ? AND NOW() BETWEEN start_date AND end_date";
	    try (Connection c = DB.getConnection();
	         PreparedStatement ps = c.prepareStatement(sql)) {
	        ps.setString(1, code);
	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                Promotion p = new Promotion();
	                p.id = rs.getInt("id");
	                p.code = rs.getString("code");
	                p.name = rs.getString("name");
	                p.discountPercent = rs.getInt("discount_percent");
	                
	                Timestamp startTs = rs.getTimestamp("start_date");
	                if (startTs != null) p.startDate = startTs.toLocalDateTime();
	                
	                Timestamp endTs = rs.getTimestamp("end_date");
	                if (endTs != null) p.endDate = endTs.toLocalDateTime();
	                
	                return p;
	            }
	        }
	    }
	    return null; // Không tìm thấy hoặc hết hạn
	}

    public List<Promotion> findAll() throws Exception {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM promotions ORDER BY end_date DESC";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
        	while (rs.next()) {
        	    Promotion p = new Promotion();
        	    p.id = rs.getInt("id");
        	    p.code = rs.getString("code");
        	    p.name = rs.getString("name");
        	    p.discountPercent = rs.getInt("discount_percent");

        	    Timestamp startTs = rs.getTimestamp("start_date");
        	    if (startTs != null) {
        	        p.startDate = startTs.toLocalDateTime(); 
        	    }

        	    Timestamp endTs = rs.getTimestamp("end_date");
        	    if (endTs != null) {
        	        p.endDate = endTs.toLocalDateTime();
        	    }
        	    
        	    list.add(p);
        	}
        }
        return list;
    }
}