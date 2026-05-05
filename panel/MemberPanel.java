package panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dao.ThanhVien_DAO;
import entity.ThanhVien;

public class MemberPanel extends JPanel {

    private final ThanhVien_DAO dao = new ThanhVien_DAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã", "Họ Tên", "Loại", "Active"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable table = new JTable(model);
    private final JTabbedPane tabs = new JTabbedPane();

    // Components nhập liệu
    private final JTextField txtCode = new JTextField();   // auto-gen
    private final JTextField txtName = new JTextField();
    private final JComboBox<String> cbType = new JComboBox<>(new String[]{"Bạc", "Vàng", "Bạch Kim"});
    private final JButton btnSave = new JButton("Lưu thành viên");

    public MemberPanel() {
        setLayout(new BorderLayout());

        // Tab 1: Danh sách thành viên
        JPanel listTab = new JPanel(new BorderLayout());
        listTab.add(new JScrollPane(table), BorderLayout.CENTER);

        // Tab 2: Thêm mới
        JPanel formTab = new JPanel(new GridBagLayout());
        formTab.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtCode.setEditable(false); // NEW: mã tự sinh, không cho nhập

        addComponent(formTab, new JLabel("Mã thành viên (tự sinh):"), txtCode, gbc, 0);
        addComponent(formTab, new JLabel("Họ và tên:"), txtName, gbc, 1);
        addComponent(formTab, new JLabel("Loại thành viên:"), cbType, gbc, 2);

        gbc.gridx = 1;
        gbc.gridy = 3;
        formTab.add(btnSave, gbc);

        tabs.addTab("Danh sách thành viên", listTab);
        tabs.addTab("Thêm / quản lý", formTab);

        add(tabs, BorderLayout.CENTER);

        // events
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromTable();
        });

        btnSave.addActionListener(e -> onSave());

        loadTableData();
        generateNextCodeToForm(); // NEW: tạo mã ngay khi mở panel
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        String code = String.valueOf(table.getValueAt(row, 0));
        String name = String.valueOf(table.getValueAt(row, 1));
        String typeVN = String.valueOf(table.getValueAt(row, 2));

        txtCode.setText(code);
        txtName.setText(name);
        cbType.setSelectedItem(typeVN);

        tabs.setSelectedIndex(1);
    }

    private void loadTableData() {
        try {
            model.setRowCount(0);
            List<ThanhVien> list = dao.findAll();
            for (ThanhVien tv : list) {
                model.addRow(new Object[]{
                        tv.maThanhVien,
                        tv.tenThanhVien,
                        toTypeVN(tv.loaiThanhVien),
                        tv.active ? "1" : "0"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách: " + e.getMessage());
        }
    }

    private void addComponent(JPanel p, JLabel label, Component comp, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        p.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        p.add(comp, gbc);
    }

    // ===== NEW: sinh mã TV dạng TV000001 dựa trên danh sách hiện có =====
    private void generateNextCodeToForm() {
        try {
            String next = generateNextMemberCode();
            txtCode.setText(next);
        } catch (Exception ex) {
            // nếu lỗi vẫn cho user nhập tay? theo yêu cầu bạn là tự sinh, nên báo lỗi luôn
            JOptionPane.showMessageDialog(this, "Không sinh được mã thành viên: " + ex.getMessage());
        }
    }

    private String generateNextMemberCode() throws Exception {
        List<ThanhVien> list = dao.findAll();

        int max = 0;
        for (ThanhVien tv : list) {
            int n = extractNumber(tv.maThanhVien);
            if (n > max) max = n;
        }
        int next = max + 1;
        return String.format("TV%06d", next);
    }

    // nhận "TV000123" -> 123; nếu mã không đúng format thì bỏ qua
    private int extractNumber(String code) {
        if (code == null) return 0;
        code = code.trim().toUpperCase();
        if (!code.startsWith("TV")) return 0;

        String digits = code.substring(2).replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;

        try {
            return Integer.parseInt(digits);
        } catch (Exception e) {
            return 0;
        }
    }

    private void onSave() {
        try {
            String code = txtCode.getText().trim().toUpperCase(); // auto-gen
            String name = txtName.getText().trim();

            if (code.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên (mã tự sinh)!");
                return;
            }

            // Check trùng mã (active). Nếu bạn muốn check trùng kể cả inactive thì cần DAO khác.
            ThanhVien exists = dao.findActiveByCode(code);
            if (exists != null) {
                // trường hợp hiếm: có người khác vừa tạo cùng mã (race condition)
                JOptionPane.showMessageDialog(this, "Mã '" + code + "' đã tồn tại. Panel sẽ sinh mã khác, bấm lưu lại.");
                generateNextCodeToForm();
                return;
            }

            ThanhVien tv = new ThanhVien();
            tv.maThanhVien = code;
            tv.tenThanhVien = name;
            tv.loaiThanhVien = toTypeDB((String) cbType.getSelectedItem());

            boolean ok = dao.insert(tv);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Không lưu được thành viên!");
                return;
            }

            JOptionPane.showMessageDialog(this, "Đã tạo thành viên: " + code);

            loadTableData();
            clearFormButKeepNextCode(); // NEW
            tabs.setSelectedIndex(0);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi thực thi: " + ex.getMessage());
        }
    }

    private void clearFormButKeepNextCode() {
        txtName.setText("");
        cbType.setSelectedIndex(0);
        generateNextCodeToForm(); // sinh mã tiếp theo cho lần tạo tiếp
    }

    // Map VN -> DB
    private String toTypeDB(String typeVN) {
        if ("Bạc".equals(typeVN)) return "BAC";
        if ("Vàng".equals(typeVN)) return "VANG";
        return "BACH_KIM";
    }

    // Map DB -> VN
    private String toTypeVN(String typeDb) {
        if (typeDb == null) return "";
        switch (typeDb) {
            case "BAC": return "Bạc";
            case "VANG": return "Vàng";
            case "BACH_KIM": return "Bạch Kim";
            default: return typeDb;
        }
    }
}