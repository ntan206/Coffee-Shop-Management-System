package model;

import javax.swing.table.AbstractTableModel;

import entity.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartTableModel extends AbstractTableModel {
    private final String[] cols = {"Mã", "Tên", "SL", "Đá", "Đường", "Member", "Promo", "Ghi chú", "Đơn giá", "Thành tiền"};
    private final NumberFormat money = NumberFormat.getInstance(new Locale("vi","VN"));

    private final List<CartItem> data = new ArrayList<>();

    public List<CartItem> getData() { return data; }
    public CartItem getAt(int row) { return data.get(row); }

    public void add(CartItem item) {
        data.add(item);
        fireTableDataChanged();
    }

    public void removeAt(int row) {
        data.remove(row);
        fireTableDataChanged();
    }

    public void clearAll() {
        data.clear();
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }

    @Override
    public Object getValueAt(int row, int col) {
        CartItem it = data.get(row);
        return switch (col) {
            case 0 -> it.product.code;
            case 1 -> it.product.name;
            case 2 -> it.quantity;
            case 3 -> it.icePercent + "%";
            case 4 -> it.sugarPercent + "%";
            case 5 -> it.memberCode == null ? "" : it.memberCode;
            case 6 -> it.promoCode == null ? "" : it.promoCode;
            case 7 -> it.note == null ? "" : it.note;
            case 8 -> money.format(it.unitPrice);
            case 9 -> money.format(it.lineTotal);
            default -> "";
        };
    }
}