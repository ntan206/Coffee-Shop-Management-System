package panel;

import org.jdatepicker.JDatePicker;

import dao.InvoiceDAO;
import entity.DatePickers;
import entity.Invoice;
import model.InvoiceTableModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class RevenuePanel extends JPanel implements ActionListener, MouseListener {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();

    private final JDatePicker dpFrom = DatePickers.createDatePicker();
    private final JDatePicker dpTo   = DatePickers.createDatePicker();

    private final JButton btnCalc = new JButton("Tính doanh thu");
    private final JButton btnShow = new JButton("Xem hóa đơn");

    private final JLabel lblRevenue = new JLabel("Doanh thu: 0 VND");

    private final InvoiceTableModel model = new InvoiceTableModel();
    private final JTable table = new JTable(model);

    public RevenuePanel() {
        setLayout(new BorderLayout(10,10));
        setBorder(new EmptyBorder(10,10,10,10));

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filter.setBorder(BorderFactory.createTitledBorder("Lọc theo ngày (JDatePicker)"));

        filter.add(new JLabel("Từ:"));
        filter.add((Component) dpFrom);   // JDatePicker là Swing component
        filter.add(new JLabel("Đến:"));
        filter.add((Component) dpTo);
        filter.add(btnCalc);
        filter.add(btnShow);

        lblRevenue.setFont(lblRevenue.getFont().deriveFont(Font.BOLD, 18f));
        table.setRowHeight(32);

        add(filter, BorderLayout.NORTH);
        add(lblRevenue, BorderLayout.CENTER);
        add(new JScrollPane(table), BorderLayout.SOUTH);

        btnCalc.addActionListener(this);
        btnShow.addActionListener(this);
        btnCalc.addMouseListener(this);
        btnShow.addMouseListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Date from = DatePickers.getValue(dpFrom);
            Date to   = DatePickers.getValue(dpTo);
            if (from == null || to == null) {
                JOptionPane.showMessageDialog(this, "Chọn đủ ngày!");
                return;
            }

            if (e.getSource() == btnCalc) {
                BigDecimal rev = invoiceDAO.sumRevenue(from, to);
                lblRevenue.setText("Doanh thu: " + rev.toPlainString() + " VND");
            } else if (e.getSource() == btnShow) {
                List<Invoice> list = invoiceDAO.findByDateRange(from, to);
                model.setData(list);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    @Override public void mouseEntered(MouseEvent e) {
        Object src = e.getSource();
        if (src instanceof JButton) ((JButton) src).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
}