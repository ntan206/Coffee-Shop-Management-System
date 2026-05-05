package panel;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import dao.ProductDAO;
import entity.Product;
import model.ProductTableModel;

import static panel.EmployeePOSPanel.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class ProductMenuPanel extends JPanel implements ActionListener {

    private final ProductDAO        dao   = new ProductDAO();
    private final ProductTableModel model = new ProductTableModel();
    private final JTable  table     = new JTable(model);
    private final JButton btnReload = new JButton("🔄  Tải lại menu");

    // Label thống kê nhỏ
    private final JLabel lblStats = new JLabel("Đang tải...");

    public ProductMenuPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(C_BG);

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        btnReload.addActionListener(this);
        load();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Header
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(14, 16, 10, 16));

        // Tiêu đề
        JLabel title = new JLabel("☕  MENU SẢN PHẨM");
        title.setFont(ui(Font.BOLD, 20));
        title.setForeground(C_GOLD);

        // Sub-label thống kê
        lblStats.setFont(ui(Font.ITALIC, 12));
        lblStats.setForeground(C_SUBTEXT);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(lblStats);

        // Nút reload
        styleReloadButton(btnReload);

        wrapper.add(left,      BorderLayout.WEST);
        wrapper.add(btnReload, BorderLayout.EAST);

        // Đường kẻ dưới header
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(C_BG);
        container.add(wrapper, BorderLayout.CENTER);

        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDER);
        sep.setBackground(C_BORDER);
        container.add(sep, BorderLayout.SOUTH);

        return container;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Table card
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildTableCard() {
        // ── Cấu hình bảng ────────────────────────────────────────────────
        table.setRowHeight(64);
        table.setFont(ui(Font.PLAIN, 13));
        table.setBackground(C_SURFACE);
        table.setForeground(C_CREAM);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 3));
        table.setSelectionBackground(new Color(212, 170, 90, 55));
        table.setSelectionForeground(C_CREAM);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(ui(Font.BOLD, 13));
        header.setBackground(C_SURFACE2);
        header.setForeground(C_GOLD);
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DarkHeaderRenderer());

        // Renderer dòng
        table.setDefaultRenderer(Object.class, new MenuRowRenderer());
        table.setDefaultRenderer(Icon.class,   new ImageCellRenderer());

        // Độ rộng cột
        int[] widths = {55, 70, 180, 120, 110, 90, 80};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        // Gắn renderer riêng cho cột ảnh (cột 6)
        table.getColumnModel().getColumn(6).setCellRenderer(new ImageCellRenderer());

        // ── Scroll pane ───────────────────────────────────────────────────
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(C_SURFACE);
        scroll.getVerticalScrollBar().setBackground(C_SURFACE2);
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());

        // ── Card wrapper ──────────────────────────────────────────────────
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_SURFACE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.setColor(C_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-2, getHeight()-2, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(C_BG);
        outer.setBorder(new EmptyBorder(10, 16, 16, 16));
        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Load data (bất đồng bộ)
    // ══════════════════════════════════════════════════════════════════════
    private void load() {
        lblStats.setText("Đang tải...");
        SwingWorker<List<Product>, Void> worker = new SwingWorker<>() {
            @Override protected List<Product> doInBackground() throws Exception {
                return dao.findAllActive();
            }
            @Override protected void done() {
                try {
                    List<Product> list = get();
                    model.setData(list);

                    // Thống kê theo nhóm
                    java.util.Map<String, Long> groups = new java.util.LinkedHashMap<>();
                    for (Product p : list) groups.merge(p.category, 1L, Long::sum);

                    StringBuilder sb = new StringBuilder(list.size() + " sản phẩm   |   ");
                    groups.forEach((cat, cnt) ->
                            sb.append(cat).append(": ").append(cnt).append("   "));
                    lblStats.setText(sb.toString().trim());

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                            ProductMenuPanel.this, "Lỗi: " + ex.getMessage());
                    lblStats.setText("Lỗi tải dữ liệu");
                }
            }
        };
        worker.execute();
    }

    @Override public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnReload) load();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Nút reload — style dark gold
    // ══════════════════════════════════════════════════════════════════════
    private void styleReloadButton(JButton btn) {
        btn.setFont(ui(Font.BOLD, 12));
        btn.setForeground(C_GOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = btn.getModel().isRollover()
                        ? C_GOLD.brighter() : C_SURFACE2;
                g2.setPaint(new GradientPaint(0, 0, base,
                        0, c.getHeight(), base.darker()));
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
                g2.setColor(C_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, c.getWidth()-1, c.getHeight()-1, 10, 10);
                g2.dispose();
                super.paint(g, c);
            }
        });
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.repaint(); }
            public void mouseExited(MouseEvent e)  { btn.repaint(); }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // Renderer: header
    // ══════════════════════════════════════════════════════════════════════
    static class DarkHeaderRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            setBackground(C_SURFACE2);
            setForeground(C_GOLD);
            setFont(ui(Font.BOLD, 13));
            setHorizontalAlignment(col == 0 || col == 4 || col == 5 || col == 6
                    ? CENTER : LEFT);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setOpaque(true);
            return this;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Renderer: các cột dữ liệu (không phải ảnh)
    // ══════════════════════════════════════════════════════════════════════
    static class MenuRowRenderer extends DefaultTableCellRenderer {

        private static Color badgeColor(String cat) {
            if (cat == null) return C_SUBTEXT;
            return switch (cat) {
                case "Coffee"     -> C_ESPRESSO;
                case "Tea"        -> new Color(150, 190, 80);
                case "Milk"       -> new Color(210, 175, 110);
                case "Energy"     -> new Color(220, 100, 40);
                case "Soft Drink" -> new Color(70, 150, 220);
                case "Water"      -> new Color(90, 180, 230);
                default           -> C_ACCENT;
            };
        }

        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);

            setOpaque(true);
            setBorder(new EmptyBorder(0, 14, 0, 14));

            if (!sel) {
                setBackground(row % 2 == 0 ? C_SURFACE : C_SURFACE2);
                setForeground(C_CREAM);
            } else {
                setBackground(new Color(212, 170, 90, 55));
                setForeground(C_GOLD_L);
            }

            switch (col) {
                case 0 -> {
                    setFont(mono(Font.BOLD, 12));
                    setForeground(C_SUBTEXT);
                    setHorizontalAlignment(CENTER);
                }
                case 1 -> {
                    setFont(mono(Font.BOLD, 13));
                    setForeground(sel ? C_GOLD_L : C_GOLD);
                    setHorizontalAlignment(CENTER);
                }
                case 2 -> {
                    setFont(ui(Font.BOLD, 13));
                    setForeground(sel ? C_GOLD_L : C_CREAM);
                    setHorizontalAlignment(LEFT);
                }
                case 3 -> {
                    setFont(ui(Font.BOLD, 11));
                    setForeground(badgeColor(v != null ? v.toString() : ""));
                    setHorizontalAlignment(CENTER);
                }
                case 4 -> {
                    setFont(ui(Font.BOLD, 13));
                    setForeground(sel ? C_GOLD_L : C_GREEN);
                    setHorizontalAlignment(CENTER);
                }
                case 5 -> {
                    boolean on = "ON".equals(v != null ? v.toString() : "");
                    setFont(ui(Font.BOLD, 12));
                    setForeground(on ? C_GREEN : C_RED);
                    setHorizontalAlignment(CENTER);
                    setText(on ? "✔  ON" : "✘  OFF");
                }
                default -> {
                    setFont(ui(Font.PLAIN, 13));
                    setHorizontalAlignment(LEFT);
                }
            }
            return this;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Renderer: ảnh sản phẩm (cột 6) — FIX tràn hàng
    // ══════════════════════════════════════════════════════════════════════
    static class ImageCellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean focus, int row, int col) {

            // Dùng JPanel custom — tọa độ tương đối trong paintComponent
            // nên không bao giờ tràn ra ngoài bounds cell khi scroll
            JPanel cell = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    if (v instanceof ImageIcon icon && icon.getIconWidth() > 0) {
                        // Tính khung ảnh vừa khít cell với padding 6px
                        int pad = 6;
                        int pw  = getWidth()  - pad * 2;
                        int ph  = getHeight() - pad * 2;
                        if (pw <= 0 || ph <= 0) { g2.dispose(); return; }

                        // Giữ tỉ lệ gốc của ảnh
                        int iw = icon.getIconWidth(), ih = icon.getIconHeight();
                        double scale = Math.min(pw / (double) iw, ph / (double) ih);
                        int sw = (int)(iw * scale), sh = (int)(ih * scale);

                        // Căn giữa ảnh trong cell
                        int dx = pad + (pw - sw) / 2;
                        int dy = pad + (ph - sh) / 2;

                        // Clip bo góc — giới hạn hoàn toàn trong bounds cell
                        g2.setClip(new RoundRectangle2D.Float(dx, dy, sw, sh, 8, 8));
                        g2.drawImage(icon.getImage(), dx, dy, sw, sh, null);
                        g2.setClip(null);

                        // Viền nhẹ
                        g2.setColor(C_BORDER);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawRoundRect(dx, dy, sw - 1, sh - 1, 8, 8);

                    } else {
                        // Placeholder khi chưa tải xong ảnh
                        g2.setFont(ui(Font.PLAIN, 18));
                        g2.setColor(C_BORDER);
                        FontMetrics fm = g2.getFontMetrics();
                        String txt = "🖼";
                        int tx = (getWidth()  - fm.stringWidth(txt)) / 2;
                        int ty = (getHeight() + fm.getAscent()) / 2 - 2;
                        g2.drawString(txt, tx, ty);
                    }
                    g2.dispose();
                }
            };

            cell.setBackground(row % 2 == 0 ? C_SURFACE : C_SURFACE2);
            cell.setOpaque(true);
            return cell;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Scrollbar tối tuỳ chỉnh
    // ══════════════════════════════════════════════════════════════════════
    static class DarkScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = C_BORDER;
            trackColor = C_SURFACE2;
        }
        @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
        @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
        private JButton zeroBtn() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? C_GOLD : C_BORDER);
            g2.fillRoundRect(r.x+2, r.y+2, r.width-4, r.height-4, 8, 8);
            g2.dispose();
        }
    }
}