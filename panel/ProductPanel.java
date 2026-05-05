package panel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import dao.ProductDAO;
import entity.Product;
import model.ProductTableModel;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.net.URL;

public class ProductPanel extends JPanel implements ActionListener, MouseListener {
    private final ProductDAO dao = new ProductDAO();
    private final ProductTableModel model = new ProductTableModel();
    private final JTable table = new JTable(model);

    private final JTextField txtCode = new JTextField();
    private final JTextField txtName = new JTextField();
    private final JTextField txtCategory = new JTextField();
    private final JTextField txtPrice = new JTextField();
    private final JTextField txtImage = new JTextField(); // Ô nhập link ảnh
    private final JCheckBox chkActive = new JCheckBox("Active", true);

    private final JButton btnAdd = new JButton("Thêm");
    private final JButton btnUpdate = new JButton("Sửa");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnClear = new JButton("Clear");
    private final JButton btnReload = new JButton("Tải lại");

    private Integer selectedId = null;

    public ProductPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Tăng chiều cao hàng lên 60 để hiển thị ảnh thumbnail đẹp hơn
        table.setRowHeight(60); 
        table.addMouseListener(this);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildTableCard(),
                buildFormCard());
        split.setDividerLocation(780);

        add(split, BorderLayout.CENTER);

        // Đăng ký sự kiện cho các nút
        btnAdd.addActionListener(this);
        btnUpdate.addActionListener(this);
        btnDelete.addActionListener(this);
        btnClear.addActionListener(this);
        btnReload.addActionListener(this);

        btnAdd.addMouseListener(this);
        btnUpdate.addMouseListener(this);
        btnDelete.addMouseListener(this);
        btnClear.addMouseListener(this);
        btnReload.addMouseListener(this);

        load();
    }

    private JPanel buildTableCard() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Danh sách sản phẩm"));
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildFormCard() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createTitledBorder("Thông tin sản phẩm"));

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Mã sản phẩm"));
        form.add(txtCode);
        form.add(new JLabel("Tên sản phẩm"));
        form.add(txtName);
        form.add(new JLabel("Nhóm"));
        form.add(txtCategory);
        form.add(new JLabel("Giá"));
        form.add(txtPrice);
        form.add(new JLabel("Link hình ảnh (URL)"));
        form.add(txtImage);
        form.add(chkActive);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.add(btnAdd);
        actions.add(btnUpdate);
        actions.add(btnDelete);
        actions.add(btnClear);
        actions.add(btnReload);

        p.add(form, BorderLayout.CENTER);
        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private void load() {
        try {
            model.setData(dao.findAll());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải sản phẩm: " + ex.getMessage());
        }
    }

    private void clear() {
        selectedId = null;
        txtCode.setText("");
        txtCode.setEnabled(true);
        txtName.setText("");
        txtCategory.setText("");
        txtPrice.setText("");
        txtImage.setText("");
        chkActive.setSelected(true);
        table.clearSelection();
    }

    private Product readForm(boolean requireCode) {
        String code = txtCode.getText().trim();
        String name = txtName.getText().trim();
        String cat = txtCategory.getText().trim();
        String priceStr = txtPrice.getText().trim();
        String img = txtImage.getText().trim();

        if (requireCode && code.isEmpty()) throw new IllegalArgumentException("Mã sản phẩm không được trống");
        if (name.isEmpty()) throw new IllegalArgumentException("Tên sản phẩm không được trống");
        if (cat.isEmpty()) throw new IllegalArgumentException("Nhóm không được trống");

        BigDecimal price;
        try { price = new BigDecimal(priceStr); }
        catch (Exception e) { throw new IllegalArgumentException("Giá không hợp lệ"); }

        Product p = new Product();
        p.id = selectedId == null ? 0 : selectedId;
        p.code = code;
        p.name = name;
        p.category = cat;
        p.price = price;
        p.image = img;
        p.active = chkActive.isSelected();
        return p;
    }

    // Hàm hiển thị cửa sổ ảnh lớn
    private void showFullImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Xem ảnh lớn", true);
        try {
            URL url = new URL(imageUrl);
            ImageIcon icon = new ImageIcon(url);
            
            // Lấy ảnh gốc và giới hạn chiều cao hiển thị là 500px (chiều rộng tự động co giãn)
            Image img = icon.getImage();
            JLabel label = new JLabel(new ImageIcon(img.getScaledInstance(-1, 500, Image.SCALE_SMOOTH)));
            
            dialog.add(new JScrollPane(label));
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải ảnh từ link này!");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Object src = e.getSource();
            if (src == btnReload) load();
            else if (src == btnClear) clear();
            else if (src == btnAdd) {
                Product p = readForm(true);
                dao.insert(p);
                JOptionPane.showMessageDialog(this, "Đã thêm sản phẩm!");
                clear();
                load();
            } else if (src == btnUpdate) {
                if (selectedId == null) { JOptionPane.showMessageDialog(this, "Chọn sản phẩm để sửa"); return; }
                Product p = readForm(false);
                dao.update(p);
                JOptionPane.showMessageDialog(this, "Đã cập nhật!");
                load();
            } else if (src == btnDelete) {
                if (selectedId == null) { JOptionPane.showMessageDialog(this, "Chọn sản phẩm để xóa"); return; }
                int ok = JOptionPane.showConfirmDialog(this, "Xóa sản phẩm này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (ok != JOptionPane.YES_OPTION) return;
                dao.delete(selectedId);
                JOptionPane.showMessageDialog(this, "Đã xóa!");
                clear();
                load();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == table) {
            int row = table.getSelectedRow();
            int col = table.columnAtPoint(e.getPoint()); // Xác định cột bị click
            
            if (row < 0) return;
            Product p = model.getAt(row);
            
            // Đổ dữ liệu vào form
            selectedId = p.id;
            txtCode.setText(p.code);
            txtCode.setEnabled(false);
            txtName.setText(p.name);
            txtCategory.setText(p.category);
            txtPrice.setText(p.price.toPlainString());
            txtImage.setText(p.image);
            chkActive.setSelected(p.active);

            // Kiểm tra: Click đúp (2 lần) VÀ đúng cột Hình ảnh (cột 6)
            if (e.getClickCount() == 2 && col == 6) {
                showFullImage(p.image);
            }
        }
    }

    @Override public void mouseEntered(MouseEvent e) {
        Object src = e.getSource();
        if (src instanceof JButton) ((JButton) src).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
}