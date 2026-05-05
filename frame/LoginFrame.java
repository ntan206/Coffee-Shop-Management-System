package frame;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import dao.UserDAO;
import entity.User;

public class LoginFrame extends JFrame implements ActionListener {

    // ====== CardLayout ======
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    // ====== HOME ======
    private JTree tree;
    private final JButton btnQuanLy = new JButton("Quản Lý");
    private final JButton btnNhanVien = new JButton("Nhân Viên");

    // ====== LOGIN UI (giữ style của bạn) ======
    private final JTextField     txtUser  = new JTextField();
    private final JPasswordField txtPass  = new JPasswordField();
    private final JButton        btnLogin = new JButton("DANG NHAP");
    private final JButton        btnBack  = new JButton("QUAY LAI");

    private String selectedRole = "MANAGER"; // MANAGER / EMPLOYEE

    // ====== SUCCESS ======
    private final JLabel lblSuccess = new JLabel("Đăng nhập thành công -> Mở Giao Diện!", SwingConstants.CENTER);

    // ── Palette ───────────────────────────────────────────────────────────────
    static final Color C_GRAD_TOP      = new Color(255, 94,  77);
    static final Color C_GRAD_BOT      = new Color(255, 179, 71);
    static final Color C_WHITE         = Color.WHITE;
    static final Color C_PRIMARY       = new Color(235, 70,  55);
    static final Color C_ACCENT        = new Color(255, 155, 45);
    static final Color C_TEXT_DARK     = new Color(45,  28,  18);
    static final Color C_TEXT_MED      = new Color(140, 90,  60);
    static final Color C_INPUT_BG      = new Color(255, 250, 245);
    static final Color C_INPUT_BD      = new Color(225, 175, 135);
    static final Color C_INPUT_BD_FOCUS= new Color(235, 70,  55);
    static final Color C_PH            = new Color(190, 145, 115);

    public LoginFrame() {
        setTitle("Quan Ly Ca Phe - Dang Nhap");
        setSize(600, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Build cards
        mainPanel.add(buildHomePanel(), "HOME");
        mainPanel.add(buildLoginPanel(), "LOGIN");
        mainPanel.add(buildSuccessPanel(), "SUCCESS");
        setContentPane(mainPanel);

        // Default show HOME
        cardLayout.show(mainPanel, "HOME");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HOME PANEL (JTree + 2 nút như demo)
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildHomePanel() {
        JPanel home = new JPanel(new BorderLayout(10, 10));
        home.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Chọn phân hệ", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        home.add(title, BorderLayout.NORTH);

        // Tree nodes
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Hệ Thống");
        DefaultMutableTreeNode quanLyNode = new DefaultMutableTreeNode("Quản Lý");
        DefaultMutableTreeNode nhanVienNode = new DefaultMutableTreeNode("Nhân Viên");
        root.add(quanLyNode);
        root.add(nhanVienNode);

        tree = new JTree(root);
        tree.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tree.setRowHeight(28);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);

        JScrollPane sp = new JScrollPane(tree);
        home.add(sp, BorderLayout.CENTER);

        // Buttons
        styleButton(btnQuanLy, new Color(70, 130, 180));
        styleButton(btnNhanVien, new Color(34, 139, 34));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 6));
        bottom.add(btnQuanLy);
        bottom.add(btnNhanVien);
        home.add(bottom, BorderLayout.SOUTH);

        // Events
        btnQuanLy.addActionListener(e -> {
            selectedRole = "MANAGER";
            autoFill("MANAGER"); // Thêm dòng này
            cardLayout.show(mainPanel, "LOGIN");
        });

        btnNhanVien.addActionListener(e -> {
            selectedRole = "EMPLOYEE";
            autoFill("EMPLOYEE"); // Thêm dòng này
            cardLayout.show(mainPanel, "LOGIN");
        });
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node == null) return;

                String v = node.toString();
                if ("Quản Lý".equals(v)) {
                    selectedRole = "MANAGER";
                    autoFill("MANAGER"); // Thêm dòng này
                    cardLayout.show(mainPanel, "LOGIN");
                } else if ("Nhân Viên".equals(v)) {
                    selectedRole = "EMPLOYEE";
                    autoFill("EMPLOYEE"); // Thêm dòng này
                    cardLayout.show(mainPanel, "LOGIN");
                }
            }
        });

        return home;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LOGIN PANEL (giữ style placeholder/border/button của bạn)
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildLoginPanel() {
        GradientBg bg = new GradientBg();
        bg.setLayout(new GridBagLayout());

        CardPanel card = new CardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(365, 360));
        bg.add(card);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx   = 0;
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.anchor  = GridBagConstraints.CENTER;
        int row    = 0;

        JLabel lblTitle = new JLabel("DANG NHAP", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 20));
        lblTitle.setForeground(C_PRIMARY);
        gc.gridy = row++; gc.insets = new Insets(18, 22, 8, 22);
        card.add(lblTitle, gc);

        JLabel lblRole = new JLabel("Vai tro: " + ("MANAGER".equals(selectedRole) ? "Quản Lý" : "Nhân Viên"),
                SwingConstants.CENTER);
        lblRole.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblRole.setForeground(C_TEXT_MED);
        gc.gridy = row++; gc.insets = new Insets(0, 22, 16, 22);
        card.add(lblRole, gc);

        // Username
        gc.gridy = row++; gc.insets = new Insets(0, 22, 5, 22);
        card.add(fLabel("Ten dang nhap"), gc);
        setupTextField(txtUser, "Nhap ten dang nhap...");
        gc.gridy = row++; gc.insets = new Insets(0, 22, 14, 22);
        card.add(txtUser, gc);

        // Password
        gc.gridy = row++; gc.insets = new Insets(0, 22, 5, 22);
        card.add(fLabel("Mat khau"), gc);
        setupPassField(txtPass, "Nhap mat khau...");
        gc.gridy = row++; gc.insets = new Insets(0, 22, 18, 22);
        card.add(txtPass, gc);

        // Buttons row
        JPanel rowBtn = new JPanel(new GridLayout(1, 2, 10, 0));
        rowBtn.setOpaque(false);

        setupButton(btnBack);
        setupButton(btnLogin);

        // chỉnh màu nút back nhẹ hơn
        btnBack.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = btnBack.getModel().isRollover();
                Color t = hover ? new Color(170, 170, 170) : new Color(145, 145, 145);
                Color b = hover ? new Color(210, 210, 210) : new Color(190, 190, 190);
                g2.setPaint(new GradientPaint(0, 0, t, c.getWidth(), 0, b));
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);
                g2.setColor(new Color(0,0,0,25));
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawRoundRect(0, 1, c.getWidth()-1, c.getHeight()-2, 12, 12);
                g2.dispose();
                super.paint(g, c);
            }
        });

        rowBtn.add(btnBack);
        rowBtn.add(btnLogin);

        gc.gridy = row++; gc.insets = new Insets(0, 22, 18, 22);
        card.add(rowBtn, gc);

        JLabel hint = new JLabel("Chọn phân hệ ở HOME bằng JTree hoặc nút", SwingConstants.CENTER);
        hint.setFont(new Font("Tahoma", Font.ITALIC, 11));
        hint.setForeground(C_TEXT_MED);
        gc.gridy = row++; gc.insets = new Insets(0, 22, 18, 22);
        card.add(hint, gc);

        // Events
        btnLogin.addActionListener(this);
        txtPass.addActionListener(this);

        btnBack.addActionListener(e -> {
            // clear input khi quay lại
            txtUser.setText("Nhap ten dang nhap...");
            txtUser.setForeground(C_PH);

            txtPass.setEchoChar((char) 0);
            txtPass.setText("Nhap mat khau...");
            txtPass.setForeground(C_PH);

            cardLayout.show(mainPanel, "HOME");
        });

        // mỗi lần show LOGIN, update label role
        bg.addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) {
                lblRole.setText("Vai tro: " + ("MANAGER".equals(selectedRole) ? "Quản Lý" : "Nhân Viên"));
            }
        });

        return bg;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SUCCESS PANEL (giống demo)
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildSuccessPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        lblSuccess.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSuccess.setForeground(new Color(0, 128, 0));
        p.add(lblSuccess, BorderLayout.CENTER);
        return p;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(120, 40));
    }

    // ── Helpers (giữ nguyên từ style login cũ) ───────────────────────────────

    private JLabel fLabel(String text) {
        JLabel lb = new JLabel(text);
        lb.setFont(new Font("Tahoma", Font.BOLD, 13));
        lb.setForeground(C_TEXT_DARK);
        return lb;
    }

    private void setupTextField(JTextField tf, String ph) {
        tf.setPreferredSize(new Dimension(0, 44));
        tf.setFont(new Font("Tahoma", Font.PLAIN, 14));
        tf.setBackground(C_INPUT_BG);
        tf.setForeground(C_PH);
        tf.setCaretColor(C_PRIMARY);
        tf.setText(ph);
        applyBorder(tf, C_INPUT_BD);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(ph)) { tf.setText(""); tf.setForeground(C_TEXT_DARK); }
                applyBorder(tf, C_INPUT_BD_FOCUS);
            }
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) { tf.setText(ph); tf.setForeground(C_PH); }
                applyBorder(tf, C_INPUT_BD);
            }
        });
    }

    private void setupPassField(JPasswordField pf, String ph) {
        pf.setPreferredSize(new Dimension(0, 44));
        pf.setFont(new Font("Tahoma", Font.PLAIN, 14));
        pf.setBackground(C_INPUT_BG);
        pf.setForeground(C_PH);
        pf.setCaretColor(C_PRIMARY);
        pf.setEchoChar((char) 0);
        pf.setText(ph);
        applyBorder(pf, C_INPUT_BD);
        pf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (new String(pf.getPassword()).equals(ph)) {
                    pf.setText(""); pf.setForeground(C_TEXT_DARK); pf.setEchoChar('*');
                }
                applyBorder(pf, C_INPUT_BD_FOCUS);
            }
            public void focusLost(FocusEvent e) {
                if (new String(pf.getPassword()).isEmpty()) {
                    pf.setEchoChar((char) 0); pf.setText(ph); pf.setForeground(C_PH);
                }
                applyBorder(pf, C_INPUT_BD);
            }
        });
    }

    private void applyBorder(JComponent c, Color bc) {
        c.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(bc, 10, 2),
                new EmptyBorder(8, 14, 8, 14)));
    }

    private void setupButton(JButton btn) {
        btn.setPreferredSize(new Dimension(0, 50));
        btn.setFont(new Font("Tahoma", Font.BOLD, 15));
        btn.setForeground(C_WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = btn.getModel().isRollover();
                Color t = hover ? new Color(210, 50, 35) : C_PRIMARY;
                Color b = hover ? new Color(235, 130, 30) : C_ACCENT;
                g2.setPaint(new GradientPaint(0, 0, t, c.getWidth(), 0, b));
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);
                // bottom shadow
                g2.setColor(new Color(0,0,0,30));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 1, c.getWidth()-1, c.getHeight()-2, 12, 12);
                g2.dispose();
                super.paint(g, c);
            }
        });
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.repaint(); }
            public void mouseExited(MouseEvent e)  { btn.repaint(); }
        });
    }

    // ── Logic ────────────────────────────────────────────────────────────────

    @Override public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnLogin || e.getSource() == txtPass) doLogin();
    }

    private void doLogin() {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());

        if (u.equals("Nhap ten dang nhap...")) u = "";
        if (p.equals("Nhap mat khau..."))       p = "";

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhap tai khoan/mat khau!");
            return;
        }

        try {
            User user = new UserDAO().login(u, p, selectedRole);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Sai tai khoan/mat khau hoac sai vai tro!");
                return;
            }

            // show success giống demo, rồi mở dashboard
            cardLayout.show(mainPanel, "SUCCESS");

            // delay nhỏ để thấy success (300ms), rồi mở dashboard
            Timer t = new Timer(300, ev -> {
                dispose();
                new DashboardFrame(user).setVisible(true);
            });
            t.setRepeats(false);
            t.start();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage());
        }
    }
    
    private void autoFill(String role) {
		if ("MANAGER".equals(role)) {
            txtUser.setText("admin"); 
            txtUser.setForeground(C_TEXT_DARK);
            
            txtPass.setText("123"); 
            txtPass.setForeground(C_TEXT_DARK);
            txtPass.setEchoChar('*');
        } else {
            txtUser.setText("nv01"); 
            txtUser.setForeground(C_TEXT_DARK);
            
            txtPass.setText("123"); 
            txtPass.setForeground(C_TEXT_DARK);
            txtPass.setEchoChar('*');
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Inner classes (giữ nguyên style gốc)
    // ══════════════════════════════════════════════════════════════════════════

    /** Gradient background */
    static class GradientBg extends JPanel {
        GradientBg() { setOpaque(true); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, C_GRAD_TOP, 0, getHeight(), C_GRAD_BOT));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255,255,255,38));
            g2.fillOval(-90, -90, 260, 260);
            g2.fillOval(getWidth()-140, getHeight()-170, 280, 280);
            g2.setColor(new Color(255,255,255,20));
            g2.fillOval(getWidth()-80, 5, 180, 180);
            g2.fillOval(20, getHeight()-120, 160, 160);
        }
    }

    /** White card with rounded corners, drop shadow, and gradient top bar */
    static class CardPanel extends JPanel {
        CardPanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth()-12, h = getHeight()-12, r = 22;
            for (int i = 10; i >= 1; i--) {
                g2.setColor(new Color(140, 35, 15, 5 + i*2));
                g2.fillRoundRect(i, i+3, w, h, r, r);
            }
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, r, r);
            g2.setPaint(new GradientPaint(0, 0, C_GRAD_TOP, w, 0, C_GRAD_BOT));
            g2.fillRoundRect(0, 0, w, 8, r, r);
            g2.fillRect(0, r/2, w, 8 - r/2 + 2);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Rounded border */
    static class RoundBorder extends javax.swing.border.AbstractBorder {
        private final Color c; private final int r, t;
        RoundBorder(Color c, int r, int t) { this.c=c; this.r=r; this.t=t; }
        @Override public void paintBorder(Component comp, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(t));
            g2.drawRoundRect(x+1, y+1, w-3, h-3, r, r);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(t+1,t+1,t+1,t+1); }
    }
}