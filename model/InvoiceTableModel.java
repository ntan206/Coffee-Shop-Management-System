package model;
import javax.swing.table.AbstractTableModel;

import entity.Invoice;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InvoiceTableModel extends AbstractTableModel {
    private final String[] cols = {"ID","Mã HD","Ngày tạo","Người tạo","Khách hàng","Tổng tiền","Trạng thái","Ghi chú"};
    private List<Invoice> data = new ArrayList<>();

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final NumberFormat money = NumberFormat.getInstance(new Locale("vi","VN"));

    public void setData(List<Invoice> list) { data = list; fireTableDataChanged(); }
    public Invoice getAt(int row) { return data.get(row); }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }

    @Override
    public Object getValueAt(int row, int col) {
        Invoice i = data.get(row);
        return switch (col) {
            case 0 -> i.id;
            case 1 -> i.code;
            case 2 -> i.createdAt == null ? "" : dtf.format(i.createdAt);
            case 3 -> i.createdByName;
            case 4 -> i.customerName;
            case 5 -> money.format(i.totalAmount);
            case 6 -> i.status;
            case 7 -> i.note;
            default -> "";
        };
    }
}