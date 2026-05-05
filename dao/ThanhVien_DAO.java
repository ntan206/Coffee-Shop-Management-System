package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import connectdb.DB;
import entity.ThanhVien;

public class ThanhVien_DAO {

    public boolean insert(ThanhVien tv) throws Exception {
        String sql = "INSERT INTO members(member_code, member_name, member_type, active) VALUES(?,?,?,1)";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, tv.maThanhVien);
            ps.setString(2, tv.tenThanhVien);
            ps.setString(3, tv.loaiThanhVien);
            return ps.executeUpdate() > 0;
        }
    }

    public ThanhVien findActiveByCode(String maTV) throws Exception {
        String sql = "SELECT id, member_code, member_name, member_type, active " +
                "FROM members WHERE member_code = ? AND active = 1 LIMIT 1";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, maTV);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                ThanhVien tv = new ThanhVien();
                tv.id            = rs.getInt("id");
                tv.maThanhVien   = rs.getString("member_code");
                tv.tenThanhVien  = rs.getString("member_name");
                tv.loaiThanhVien = rs.getString("member_type");
                tv.active        = rs.getInt("active") == 1;
                return tv;
            }
        }
    }

    public List<ThanhVien> findAll() throws Exception {
        String sql = "SELECT id, member_code, member_name, member_type, active " +
                     "FROM members ORDER BY member_type, member_name";
        List<ThanhVien> list = new ArrayList<>();
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ThanhVien tv    = new ThanhVien();
                tv.id            = rs.getInt("id");
                tv.maThanhVien   = rs.getString("member_code");
                tv.tenThanhVien  = rs.getString("member_name");
                tv.loaiThanhVien = rs.getString("member_type");
                tv.active        = rs.getInt("active") == 1;
                list.add(tv);
            }
        }
        return list;
    }
}