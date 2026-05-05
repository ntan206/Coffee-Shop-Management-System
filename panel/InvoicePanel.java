package panel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import dao.InvoiceDAO;
import dao.ProductDAO;
import entity.Invoice;
import entity.InvoiceItem;
import entity.Product;
import entity.User;
import model.InvoiceItemTableModel;
import model.InvoiceTableModel;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InvoicePanel extends JPanel implements ActionListener, MouseListener {
    private final User currentUser;

    private final ProductDAO productDAO = new ProductDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();

    private final InvoiceTableModel invoiceModel = new InvoiceTableModel();
    private final JTable tblInvoices = new JTable(invoiceModel);

    private final InvoiceItemTableModel itemModel = new InvoiceItemTableModel();
    private final JTable tblItems = new JTable(itemModel);

    // invoice form
    private final JTextField txtInvoiceCode = new JTextField();
    private final JTextField txtCustomer = new JTextField();
    private final JTextArea txtNote = new JTextArea(3, 20);
    private final JComboBox<String> cboStatus = new JComboBox<>(new String[]{"PAID","CANCELLED"});
    private final JLabel lblTotal = new JLabel("Tổng: 0");

    // add item
    private final JComboBox<Product> cboProducts = new JComboBox<>();
    private final JSpinner spQty = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JButton btnAddItem = new JButton("Thêm món");
    private final JButton btnRemoveItem = new JButton("Xóa dòng");

    // actions
    private final JButton btnCreate = new JButton("Tạo HĐ");
    private final JButton btnSaveItems = new JButton("Lưu chi tiết");
    private final JButton btnReload = new JButton("Tải lại");
    private final JButton btnDelete = new JButton("Xóa HĐ");

    private Integer selectedInvoiceId = null;
    private Integer selectedInvoiceCreatedBy = null;

    public InvoicePanel(User user) {
        this.currentUser = user;

        setLayout(new BorderLayout(10,10));
        setBorder(new EmptyBorder(10,10,10,10));

        tblInvoices.setRowHeight(32);
        tblItems.setRowHeight(32);

        tblInvoices.addMouseListener(this);
        tblItems.addMouseListener(this);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildInvoicesCard(),
                buildDetailCard());
        split.setDividerLocation(720);

        add(split, BorderLayout.CENTER);

        // events
        btnCreate.addActionListener(this);
        btnSaveItems.addActionListener(this);
        btnReload.addActionListener(this);
        btnDelete.addActionListener(this);
        btnAddItem.addActionListener(this);
        btnRemoveItem.addActionListener(this);

        for (JButton b : new JButton[]{btnCreate, btnSaveItems, btnReload, btnDelete, btnAddItem, btnRemoveItem}) {
            b.addMouseListener(this);
        }

        // quyền nhân viên A: không cho xóa hóa đơn (và không sửa status)
        if ("EMPLOYEE".equals(currentUser.role)) {
            btnDelete.setEnabled(false);
            cboStatus.setEnabled(false);
        }

        loadProducts();
        loadInvoices();
    }

    private JPanel buildInvoicesCard() {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBorder(BorderFactory.createTitledBorder("Danh sách hóa đơn"));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.add(btnReload);

        if ("MANAGER".equals(currentUser.role)) top.add(btnDelete);

        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(tblInvoices), BorderLayout.CENTER);

        return p;
    }

    private JPanel buildDetailCard() {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBorder(BorderFactory.createTitledBorder("Chi tiết hóa đơn"));

        JPanel form = new JPanel(new GridLayout(0,1,6,6));
        form.add(new JLabel("Mã hóa đơn"));
        form.add(txtInvoiceCode);
        form.add(new JLabel("Khách hàng"));
        form.add(txtCustomer);
        form.add(new JLabel("Ghi chú"));
        form.add(new JScrollPane(txtNote));
        form.add(new JLabel("Trạng thái"));
        form.add(cboStatus);
        form.add(lblTotal);

        JPanel itemAdd = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        itemAdd.add(new JLabel("Sản phẩm:"));
        itemAdd.add(cboProducts);
        itemAdd.add(new JLabel("SL:"));
        itemAdd.add(spQty);
        itemAdd.add(btnAddItem);
        itemAdd.add(btnRemoveItem);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.add(btnCreate);
        actions.add(btnSaveItems);

        // nhân viên A: chỉ tạo hóa đơn + lưu chi tiết, không sửa status/xóa
        if ("EMPLOYEE".equals(currentUser.role)) {
            actions.add(new JLabel("(Nhân viên: tạo & xem hóa đơn của mình)"));
        }

        p.add(form, BorderLayout.NORTH);
        p.add(itemAdd, BorderLayout.CENTER);
        p.add(new JScrollPane(tblItems), BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout(10,10));
        wrap.add(p, BorderLayout.CENTER);
        wrap.add(actions, BorderLayout.SOUTH);
        return wrap;
    }

    private void loadProducts() {
        try {
            cboProducts.removeAllItems();
            for (Product pr : productDAO.findAllActive()) cboProducts.addItem(pr);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải sản phẩm: " + ex.getMessage());
        }
    }

    private void loadInvoices() {
        try {
            if ("MANAGER".equals(currentUser.role)) invoiceModel.setData(invoiceDAO.findAll());
            else invoiceModel.setData(invoiceDAO.findByCreator(currentUser.id));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải hóa đơn: " + ex.getMessage());
        }
    }

    private void clearInvoiceForm() {
        selectedInvoiceId = null;
        selectedInvoiceCreatedBy = null;
        txtInvoiceCode.setText("");
        txtInvoiceCode.setEnabled(true);
        txtCustomer.setText("");
        txtNote.setText("");
        cboStatus.setSelectedIndex(0);
        itemModel.setData(new ArrayList<>());
        lblTotal.setText("Tổng: 0");
        tblInvoices.clearSelection();
    }

    private BigDecimal calcTotal(List<InvoiceItem> items) {
        BigDecimal sum = BigDecimal.ZERO;
        for (InvoiceItem it : items) sum = sum.add(it.lineTotal);
        return sum;
    }

    private void refreshTotalLabel() {
        BigDecimal total = calcTotal(itemModel.getData());
        lblTotal.setText("Tổng: " + total.toPlainString());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        try {
            if (src == btnReload) {
                clearInvoiceForm();
                loadInvoices();
                return;
            }

            if (src == btnCreate) {
                onCreateInvoice();
                return;
            }

            if (src == btnAddItem) {
                onAddItem();
                return;
            }

            if (src == btnRemoveItem) {
                onRemoveItem();
                return;
            }

            if (src == btnSaveItems) {
                onSaveItems();
                return;
            }

            if (src == btnDelete) {
                onDeleteInvoice();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    private void onCreateInvoice() throws Exception {
        String code = txtInvoiceCode.getText().trim();
        if (code.isEmpty()) { JOptionPane.showMessageDialog(this, "Mã HĐ không được trống"); return; }

        Invoice inv = new Invoice();
        inv.code = code;
        inv.createdAt = LocalDateTime.now();
        inv.createdBy = currentUser.id;
        inv.customerName = txtCustomer.getText().trim();
        inv.note = txtNote.getText().trim();
        inv.status = (String) cboStatus.getSelectedItem();
        inv.totalAmount = calcTotal(itemModel.getData()); // nếu chưa add món thì 0

        int id = invoiceDAO.insertInvoice(inv);
        selectedInvoiceId = id;
        selectedInvoiceCreatedBy = currentUser.id;
        txtInvoiceCode.setEnabled(false);

        JOptionPane.showMessageDialog(this, "Tạo hóa đơn thành công! ID=" + id);
        loadInvoices();
    }

    private void onAddItem() {
        Product pr = (Product) cboProducts.getSelectedItem();
        if (pr == null) { JOptionPane.showMessageDialog(this, "Chưa có sản phẩm"); return; }

        int qty = (int) spQty.getValue();
        if (qty <= 0) { JOptionPane.showMessageDialog(this, "SL phải > 0"); return; }

        // Nếu đã có sản phẩm này trong list -> cộng qty
        List<InvoiceItem> items = itemModel.getData();
        for (InvoiceItem it : items) {
            if (it.productId == pr.id) {
                it.qty += qty;
                it.unitPrice = pr.price;
                it.lineTotal = pr.price.multiply(BigDecimal.valueOf(it.qty));
                itemModel.fireTableDataChanged();
                refreshTotalLabel();
                return;
            }
        }

        InvoiceItem it = new InvoiceItem();
        it.productId = pr.id;
        it.productCode = pr.code;
        it.productName = pr.name;
        it.qty = qty;
        it.unitPrice = pr.price;
        it.lineTotal = pr.price.multiply(BigDecimal.valueOf(qty));

        items.add(it);
        itemModel.fireTableDataChanged();
        refreshTotalLabel();
    }

    private void onRemoveItem() {
        int row = tblItems.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn 1 dòng để xóa"); return; }
        itemModel.getData().remove(row);
        itemModel.fireTableDataChanged();
        refreshTotalLabel();
    }

    private void onSaveItems() throws Exception {
        if (selectedInvoiceId == null) {
            JOptionPane.showMessageDialog(this, "Hãy tạo/chọn hóa đơn trước khi lưu chi tiết!");
            return;
        }

        // Nhân viên A: chỉ được lưu cho hóa đơn của mình (để chắc chắn)
        if ("EMPLOYEE".equals(currentUser.role) && (selectedInvoiceCreatedBy == null || selectedInvoiceCreatedBy != currentUser.id)) {
            JOptionPane.showMessageDialog(this, "Bạn chỉ được thao tác hóa đơn của mình.");
            return;
        }

        List<InvoiceItem> items = itemModel.getData();
        invoiceDAO.replaceItems(selectedInvoiceId, items);

        // Update tổng tiền
        Invoice inv = new Invoice();
        inv.id = selectedInvoiceId;
        inv.customerName = txtCustomer.getText().trim();
        inv.note = txtNote.getText().trim();
        inv.status = (String) cboStatus.getSelectedItem();
        inv.totalAmount = calcTotal(items);
        invoiceDAO.updateInvoice(inv);

        JOptionPane.showMessageDialog(this, "Đã lưu chi tiết & cập nhật tổng tiền!");
        loadInvoices();
    }

    private void onDeleteInvoice() throws Exception {
        if (selectedInvoiceId == null) { JOptionPane.showMessageDialog(this, "Chọn hóa đơn để xóa"); return; }

        // manager only (button đã disable cho employee)
        int ok = JOptionPane.showConfirmDialog(this, "Xóa hóa đơn này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        invoiceDAO.deleteInvoice(selectedInvoiceId);
        JOptionPane.showMessageDialog(this, "Đã xóa hóa đơn!");
        clearInvoiceForm();
        loadInvoices();
    }

    // MouseListener: chọn hóa đơn -> load items
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == tblInvoices) {
            int row = tblInvoices.getSelectedRow();
            if (row < 0) return;

            Invoice inv = invoiceModel.getAt(row);
            selectedInvoiceId = inv.id;
            selectedInvoiceCreatedBy = inv.createdBy;

            txtInvoiceCode.setText(inv.code);
            txtInvoiceCode.setEnabled(false);
            txtCustomer.setText(inv.customerName == null ? "" : inv.customerName);
            txtNote.setText(inv.note == null ? "" : inv.note);
            cboStatus.setSelectedItem(inv.status);

            try {
                itemModel.setData(invoiceDAO.findItems(inv.id));
                refreshTotalLabel();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết: " + ex.getMessage());
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