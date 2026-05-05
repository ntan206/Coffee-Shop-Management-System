package model;

import javax.swing.table.AbstractTableModel;

import entity.InvoiceItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InvoiceItemTableModel extends AbstractTableModel {
    private final String[] cols = {"Mã SP","Tên SP","SL","Đơn giá","Thành tiền"};
    private List<InvoiceItem> data = new ArrayList<>();
    private final NumberFormat money = NumberFormat.getInstance(new Locale("vi","VN"));

    public void setData(List<InvoiceItem> list) { data = list; fireTableDataChanged(); }
    public List<InvoiceItem> getData() { return data; }
    public InvoiceItem getAt(int row) { return data.get(row); }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }

    @Override
    public Object getValueAt(int row, int col) {
        InvoiceItem it = data.get(row);
        return switch (col) {
            case 0 -> it.productCode;
            case 1 -> it.productName;
            case 2 -> it.qty;
            case 3 -> money.format(it.unitPrice);
            case 4 -> money.format(it.lineTotal);
            default -> "";
        };
    }
}