package dao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import connectdb.DB;
import entity.Invoice;
import entity.InvoiceItem;

public class InvoiceDAO {

    // ===== INVOICE LIST =====

    public List<Invoice> findAll() throws Exception {
        String sql = """
            SELECT i.*, u.full_name AS created_by_name
            FROM invoices i
            JOIN users u ON u.id = i.created_by
            ORDER BY i.created_at DESC
        """;
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapInvoices(rs);
        }
    }

    public List<Invoice> findByCreator(int userId) throws Exception {
        String sql = """
            SELECT i.*, u.full_name AS created_by_name
            FROM invoices i
            JOIN users u ON u.id = i.created_by
            WHERE i.created_by = ?
            ORDER BY i.created_at DESC
        """;
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapInvoices(rs);
            }
        }
    }

    // ===== INVOICE CRUD =====

    public int insertInvoice(Invoice inv) throws Exception {
        String sql = """
            INSERT INTO invoices(code, created_at, created_by, customer_name, note, total_amount, status)
            VALUES(?,?,?,?,?,?,?)
        """;
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, inv.code);
            ps.setTimestamp(2, Timestamp.valueOf(inv.createdAt));
            ps.setInt(3, inv.createdBy);
            ps.setString(4, inv.customerName);
            ps.setString(5, inv.note);
            ps.setBigDecimal(6, inv.totalAmount == null ? BigDecimal.ZERO : inv.totalAmount);
            ps.setString(7, inv.status);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void updateInvoice(Invoice inv) throws Exception {
        String sql = """
            UPDATE invoices
            SET customer_name=?, note=?, total_amount=?, status=?
            WHERE id=?
        """;
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, inv.customerName);
            ps.setString(2, inv.note);
            ps.setBigDecimal(3, inv.totalAmount == null ? BigDecimal.ZERO : inv.totalAmount);
            ps.setString(4, inv.status);
            ps.setInt(5, inv.id);
            ps.executeUpdate();
        }
    }

    public void deleteInvoice(int id) throws Exception {
        String sql = "DELETE FROM invoices WHERE id=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ===== ITEMS =====

    public void replaceItems(int invoiceId, List<InvoiceItem> items) throws Exception {
        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement del = c.prepareStatement("DELETE FROM invoice_items WHERE invoice_id=?")) {
                    del.setInt(1, invoiceId);
                    del.executeUpdate();
                }

                String ins = """
                    INSERT INTO invoice_items(invoice_id, product_id, qty, unit_price, line_total)
                    VALUES(?,?,?,?,?)
                """;
                try (PreparedStatement ps = c.prepareStatement(ins)) {
                    for (InvoiceItem it : items) {
                        ps.setInt(1, invoiceId);
                        ps.setInt(2, it.productId);
                        ps.setInt(3, it.qty);
                        ps.setBigDecimal(4, it.unitPrice);
                        ps.setBigDecimal(5, it.lineTotal);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public List<InvoiceItem> findItems(int invoiceId) throws Exception {
        String sql = """
            SELECT it.*, p.code AS product_code, p.name AS product_name
            FROM invoice_items it
            JOIN products p ON p.id = it.product_id
            WHERE it.invoice_id = ?
            ORDER BY it.id
        """;
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                List<InvoiceItem> list = new ArrayList<>();
                while (rs.next()) {
                    InvoiceItem it = new InvoiceItem();
                    it.id = rs.getInt("id");
                    it.invoiceId = rs.getInt("invoice_id");
                    it.productId = rs.getInt("product_id");
                    it.productCode = rs.getString("product_code");
                    it.productName = rs.getString("product_name");
                    it.qty = rs.getInt("qty");
                    it.unitPrice = rs.getBigDecimal("unit_price");
                    it.lineTotal = rs.getBigDecimal("line_total");
                    list.add(it);
                }
                return list;
            }
        }
    }

    // ===== REVENUE =====

    public List<Invoice> findByDateRange(Date from, Date to) throws Exception {
        String sql = """
            SELECT i.*, u.full_name AS created_by_name
            FROM invoices i
            JOIN users u ON u.id=i.created_by
            WHERE i.created_at >= ? AND i.created_at < ?
            ORDER BY i.created_at DESC
        """;
        Date toExclusive = new Date(to.getTime() + 24L * 60 * 60 * 1000);

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(toExclusive.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                return mapInvoices(rs);
            }
        }
    }

    public BigDecimal sumRevenue(Date from, Date to) throws Exception {
        String sql = """
            SELECT COALESCE(SUM(total_amount), 0) AS revenue
            FROM invoices
            WHERE status='PAID'
              AND created_at >= ? AND created_at < ?
        """;
        Date toExclusive = new Date(to.getTime() + 24L * 60 * 60 * 1000);

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(toExclusive.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal("revenue");
            }
        }
    }

    // ===== MAPPERS =====

    private List<Invoice> mapInvoices(ResultSet rs) throws Exception {
        List<Invoice> list = new ArrayList<>();
        while (rs.next()) {
            Invoice i = new Invoice();
            i.id = rs.getInt("id");
            i.code = rs.getString("code");

            Timestamp ts = rs.getTimestamp("created_at");
            i.createdAt = ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            i.createdBy = rs.getInt("created_by");
            i.createdByName = rs.getString("created_by_name");
            i.customerName = rs.getString("customer_name");
            i.note = rs.getString("note");
            i.totalAmount = rs.getBigDecimal("total_amount");
            i.status = rs.getString("status");
            list.add(i);
        }
        return list;
    }
}