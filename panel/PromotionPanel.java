package panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.jdatepicker.JDatePicker;

import dao.PromotionDAO;
import entity.DatePickers;
import entity.Promotion;

public class PromotionPanel extends JPanel {
    private final PromotionDAO dao = new PromotionDAO();
    private final JTabbedPane tabs = new JTabbedPane();

    // Components cho Tab Thêm mới
    private final JTextField txtCode = new JTextField();
    private final JTextField txtName = new JTextField();
    private final JSpinner spPercent = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
    private final JDatePicker dpStart = DatePickers.createDatePicker(); // Dùng hàm utility[cite: 13]
    private final JDatePicker dpEnd = DatePickers.createDatePicker();
    private final JButton btnSave = new JButton("Lưu Khuyến Mãi");
    private DefaultTableModel tableModel;
    private JTable table;

    public PromotionPanel() {
        setLayout(new BorderLayout());
        
        // Tab 1: Danh sách (Dùng JTable tương tự các bảng khác trong project)[cite: 15, 16]
        JPanel listTab = new JPanel(new BorderLayout(10, 10));
        listTab.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        String[] columns = {"ID", "Mã KM", "Tên chương trình", "Giảm (%)", "Bắt đầu", "Kết thúc"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        listTab.add(new JScrollPane(table), BorderLayout.CENTER);
        
       

        // Tab 2: Thêm mới / Cập nhật
        JPanel formTab = new JPanel(new BorderLayout());
        formTab.add(buildForm(), BorderLayout.NORTH);

        tabs.addTab("Danh sách Khuyến mãi", listTab);
        tabs.addTab("Tạo mã mới", formTab);
        
        add(tabs, BorderLayout.CENTER);
        btnSave.addActionListener(e -> onSave());
        
        loadData();
    }
    private void loadData() {
        try {
            tableModel.setRowCount(0); // Xóa dữ liệu cũ trên bảng giao diện
            List<Promotion> list = dao.findAll(); // Gọi hàm findAll từ DAO
            for (Promotion p : list) {
                tableModel.addRow(new Object[]{
                    p.id, p.code, p.name, p.discountPercent + "%", 
                    p.startDate, p.endDate
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Layout các trường nhập liệu
        addFormRow(p, "Mã giảm giá:", txtCode, gbc, 0);
        addFormRow(p, "Tên chương trình:", txtName, gbc, 1);
        addFormRow(p, "Phần trăm giảm (%):", spPercent, gbc, 2);
        addFormRow(p, "Ngày bắt đầu:", (Component) dpStart, gbc, 3); // Cast để hiển thị[cite: 12]
        addFormRow(p, "Ngày kết thúc:", (Component) dpEnd, gbc, 4);

        gbc.gridx = 1; gbc.gridy = 5;
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        p.add(btnSave, gbc);

        return p;
    }

    private void addFormRow(JPanel p, String label, Component comp, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.1;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.9;
        p.add(comp, gbc);
    }

    private void onSave() {
        try {
            // 1. Lấy dữ liệu từ giao diện
            String code = txtCode.getText().trim();
            String name = txtName.getText().trim();
            int discount = (int) spPercent.getValue();
            
            java.util.Date dateS = DatePickers.getValue(dpStart);
            java.util.Date dateE = DatePickers.getValue(dpEnd);

            // Kiểm tra trống
            if (code.isEmpty() || dateS == null || dateE == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập mã và chọn đầy đủ ngày tháng!");
                return;
            }

            // 3. Khởi tạo Model và chuyển đổi kiểu dữ liệu[cite : 23]
            Promotion p = new Promotion();
            p.code = code;
            p.name = name;
            p.discountPercent = discount;
            
            // Chuyển java.util.Date -> LocalDateTime
            p.startDate = new java.sql.Timestamp(dateS.getTime()).toLocalDateTime();
            p.endDate = new java.sql.Timestamp(dateE.getTime()).toLocalDateTime();

            // 4. Gọi DAO để lưu vào phpMyAdmin[cite: 21]
            dao.insert(p);
            
            JOptionPane.showMessageDialog(this, "Lưu khuyến mãi thành công!");
            
            // Clear form
            txtCode.setText("");
            txtName.setText("");
            tabs.setSelectedIndex(0);
        } catch (Exception ex) {
            ex.printStackTrace(); 
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }
}
