package frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import entity.User;
import panel.AccountPanel;
import panel.EmployeePOSPanel;
import panel.InvoicePanel;
import panel.MemberPanel;
import panel.ProductMenuPanel;
import panel.ProductPanel;
import panel.PromotionPanel;
import panel.RevenuePanel;

public class DashboardFrame extends JFrame implements ActionListener, MouseListener {
    private final User currentUser;
    private final JButton btnLogout = new JButton("Đăng xuất");
    private final JTabbedPane tabs = new JTabbedPane();

    public DashboardFrame(User user) {
        this.currentUser = user;

        setTitle("Coffee Shop • " + user.fullName + " (" + user.role + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 760);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(12,12));
        root.setBorder(new EmptyBorder(14,14,14,14));
        setContentPane(root);

        root.add(topBar(), BorderLayout.NORTH);

        if ("MANAGER".equals(currentUser.role)) {
            // Quản lý: full quyền
            tabs.addTab("Hóa đơn", new InvoicePanel(currentUser));   // quản lý CRUD hóa đơn
            tabs.addTab("Sản phẩm", new ProductPanel());
            tabs.addTab("Doanh thu", new RevenuePanel());
            
            tabs.addTab("Nhân viên", new AccountPanel());           // Tạo tài khoản & Access DB
            tabs.addTab("Khuyến mãi", new PromotionPanel());         // Mã giảm, JDatePicker, Tabbed Pane
            tabs.addTab("Thành viên", new MemberPanel());
        } else {
            // Nhân viên: POS order + thanh toán
            tabs.addTab("Bán hàng (POS)", new EmployeePOSPanel(currentUser));
            tabs.addTab("Menu", new ProductMenuPanel()); // optional
        }

        root.add(tabs, BorderLayout.CENTER);

        btnLogout.addActionListener(this);
        btnLogout.addMouseListener(this);
    }

    private JPanel topBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230,230,230)),
                new EmptyBorder(12,12,12,12)
        ));

        JLabel title = new JLabel("Quản lý quán cà phê");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JLabel sub = new JLabel("Xin chào: " + currentUser.fullName + " • ROLE: " + currentUser.role);
        sub.setForeground(new Color(110,110,110));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(sub);

        p.add(left, BorderLayout.WEST);
        p.add(btnLogout, BorderLayout.EAST);
        return p;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnLogout) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    @Override public void mouseEntered(MouseEvent e) {
        if (e.getSource() == btnLogout) btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
}