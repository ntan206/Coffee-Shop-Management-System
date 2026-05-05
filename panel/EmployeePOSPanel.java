package panel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import dao.InvoiceDAO;
import dao.ProductDAO;
import dao.PromotionDAO;
import dao.ThanhVien_DAO;
import entity.CartItem;
import entity.Invoice;
import entity.InvoiceItem;
import entity.Product;
import entity.Promotion;
import entity.ThanhVien;
import entity.User;
import model.CartTableModel;
import pdfinvoice.PdfInvoiceExporter;

public class EmployeePOSPanel extends JPanel implements ActionListener, MouseListener {
    private final User currentUser;

    private final ProductDAO productDAO = new ProductDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final ThanhVien_DAO thanhVienDAO = new ThanhVien_DAO();
    
    // Tích hợp PromotionDAO và các biến trạng thái tính toán
    private final PromotionDAO promotionDAO = new PromotionDAO();
    private double currentDiscountPercent = 0.0;
    private String currentPromoCode = "";
    
    private BigDecimal totalGross = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal finalTotal = BigDecimal.ZERO;

    private static final String QR_RESOURCE_PATH = "/dulieu/sa.png";
    private ImageIcon cachedQrIcon = null;

    private final CartTableModel cartModel = new CartTableModel();
    private final JTable tblCart = new JTable(cartModel);
    private final JTabbedPane tabs = new JTabbedPane();

    // ── TAB 1 ──────────────────────────────────────────────────────────────────
    private final DefaultListModel<Product> menuModel = new DefaultListModel<>();
    private final JList<Product> lstMenu = new JList<>(menuModel);
    private Product selectedProduct = null;

    private final JSlider slIce   = new JSlider(0, 100, 50);
    private final JSlider slSugar = new JSlider(0, 100, 50);
    private final JSpinner spQty  = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final JTextArea txtNote = new JTextArea(3, 20);

    private final JLabel lblSelectedName  = new JLabel("Chọn món từ menu bên trái");
    private final JLabel lblSelectedPrice = new JLabel("Giá: -");
    private final JButton btnAddToCart    = new JButton("THÊM VÀO HÓA ĐƠN");

    // ── TAB 2 ──────────────────────────────────────────────────────────────────
    private final JButton  btnRemoveLine = new JButton("XÓA DÒNG");
    private final JButton  btnUpdateQty  = new JButton("CẬP NHẬT");
    private final JSpinner spNewQty      = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));

    // ── TAB 3 — TẠO THÀNH VIÊN ────────────────────────────────────────────────
    private final JLabel   lblAutoMemberCode = new JLabel("(Tự động sinh)");
    private       String   generatedMemberCode = "";
    private final JTextField txtNewMemberName = new JTextField();
    private final JComboBox<String> cbMemberType = new JComboBox<>(new String[]{"Bạc", "Vàng", "Bạch Kim"});
    private final JButton btnCreateMember = new JButton("TẠO THÀNH VIÊN");

    // ── TAB 4 (PAY) ───────────────────────────────────────────────────────────
    private final JTextField txtMember = new JTextField();
    private final JTextField txtPromo  = new JTextField();

    private final JButton   btnApplyDiscount  = new JButton("ÁP DỤNG");
    private final JLabel    lblTotal          = new JLabel("Tổng tiền: 0 VND");
    private final JLabel    lblDiscount       = new JLabel("Giảm giá: 0 VND");
    private final JLabel    lblFinalTotal     = new JLabel("THÀNH TIỀN: 0 VND");
    private final JTextArea txtPreviewReceipt = new JTextArea(12, 30);
    private final JRadioButton rbCash = new JRadioButton("Tiền mặt", true);
    private final JRadioButton rbQR   = new JRadioButton("QR");
    private final JButton   btnPay    = new JButton("THANH TOÁN");
    private final JTextArea txtReceipt = new JTextArea(10, 30);

    // ── TAB 5 — DANH SÁCH THÀNH VIÊN CHƯƠNG TRÌNH ────────────────────────────
//    private final DefaultTableModel memberTableModel = new DefaultTableModel(
//            new String[]{"Mã Thành Viên", "Tên Thành Viên", "Hạng"}, 0) {
//        @Override public boolean isCellEditable(int row, int col) { return false; }
//    };
//    private final JTable  tblMembers       = new JTable(memberTableModel);
//    private final JButton btnRefreshMembers = new JButton("🔄  LÀM MỚI");

    // ── TAB 6 — TÌM KIẾM ─────────────────────────────────────────────────────
    private final JTextField txtSearchQuery = new JTextField();
    private final JComboBox<String> cbSearchType = new JComboBox<>(
            new String[]{"Tên sản phẩm", "Mã sản phẩm", "Đơn giá (VNĐ)"});
    private final JButton btnSearch      = new JButton("🔍  TÌM KIẾM");
    private final JButton btnResetSearch = new JButton("🔄  LÀM MỚI");
    private final DefaultTableModel searchResultModel = new DefaultTableModel(
            new String[]{"Mã", "Tên", "Hạng / Nhóm", "Giá / Trạng thái"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tblSearchResult = new JTable(searchResultModel);

    // ── Palette ────────────────────────────────────────────────────────────────
    static final Color C_BG        = new Color(15,  12,  10);
    static final Color C_SURFACE   = new Color(26,  22,  18);
    static final Color C_SURFACE2  = new Color(36,  30,  24);
    static final Color C_BORDER    = new Color(60,  48,  36);
    static final Color C_GOLD      = new Color(212, 170, 90);
    static final Color C_GOLD_L    = new Color(240, 205, 130);
    static final Color C_ESPRESSO  = new Color(120, 72,  30);
    static final Color C_CREAM     = new Color(245, 235, 210);
    static final Color C_SUBTEXT   = new Color(160, 140, 110);
    static final Color C_GREEN     = new Color(80,  185, 120);
    static final Color C_RED       = new Color(220, 75,  75);
    static final Color C_ORANGE    = new Color(235, 145, 45);
    static final Color C_BLUE      = new Color(90,  150, 230);
    static final Color C_ACCENT    = new Color(200, 130, 55);

    static final Color C_MATCHA    = new Color(110, 175, 80);
    static final Color C_STRAWB    = new Color(210, 70,  100);
    static final Color C_MILK      = new Color(225, 200, 165);

    static final Color C_SILVER    = new Color(192, 192, 192);
    static final Color C_VANG      = new Color(255, 215, 0);
    static final Color C_PLATINUM  = new Color(180, 220, 230);

    static final String FONT_UI   = resolveFontName(new String[]{"Segoe UI", "Arial Unicode MS", "SansSerif"});
    static final String FONT_MONO = resolveFontName(new String[]{"Consolas", "Courier New", "Monospaced"});

    private static String resolveFontName(String[] candidates) {
        java.util.Set<String> available = new java.util.HashSet<>(
                java.util.Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : candidates) {
            if (available.contains(f)) return f;
        }
        return candidates[candidates.length - 1];
    }

    static Font ui(int style, int size)   { return new Font(FONT_UI,   style, size); }
    static Font mono(int style, int size) { return new Font(FONT_MONO, style, size); }

    public EmployeePOSPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(14, 14, 14, 14));
        setBackground(C_BG);

        // ── Cart table ────────────────────────────────────────────────────────
        tblCart.setRowHeight(38);
        tblCart.setFont(ui(Font.PLAIN, 13));
        tblCart.getTableHeader().setFont(ui(Font.BOLD, 12));
        tblCart.getTableHeader().setBackground(C_SURFACE2);
        tblCart.getTableHeader().setForeground(C_GOLD);
        tblCart.getTableHeader().setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        tblCart.setShowGrid(false);
        tblCart.setIntercellSpacing(new Dimension(0, 2));
        tblCart.setSelectionBackground(new Color(212, 170, 90, 55));
        tblCart.setSelectionForeground(C_CREAM);
        tblCart.setBackground(C_SURFACE);
        tblCart.setForeground(C_CREAM);
        tblCart.setDefaultRenderer(Object.class, new DarkStripedRenderer());
        tblCart.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCart.addMouseListener(this);

        JScrollPane cartScroll = new JScrollPane(tblCart);
        cartScroll.setBorder(BorderFactory.createEmptyBorder());
        cartScroll.getViewport().setBackground(C_SURFACE);
        cartScroll.getVerticalScrollBar().setBackground(C_SURFACE2);

        JPanel cartCard = darkCard(new BorderLayout(0, 0));
        JPanel cartHeader = makeHeader("🧾  HÓA ĐƠN TẠM THỜI", C_GOLD, C_SURFACE2);
        cartCard.add(cartHeader, BorderLayout.NORTH);
        cartCard.add(cartScroll, BorderLayout.CENTER);

        // ── Tabs ──────────────────────────────────────────────────────────────
        tabs.addTab("  ☕  Thêm Món  ",                buildAddTab());
        tabs.addTab("  ✏️  Sửa / Xóa  ",               buildEditTab());
        tabs.addTab("  👤  Thành Viên  ",               buildMemberTab());
        tabs.addTab("  💳  Thanh Toán  ",               buildPayTab());
//        tabs.addTab("  🏆  Thành Viên Chương Trình  ",  buildMemberListTab());
        tabs.addTab("  🔍  Tìm Kiếm  ",                 buildSearchTab());
        styleTabPane(tabs);

//        tabs.addChangeListener(ce -> {
//            if (tabs.getSelectedIndex() == 3) refreshPreviewReceipt();
//            if (tabs.getSelectedIndex() == 4) loadMemberList();
//        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cartCard, tabs);
        split.setResizeWeight(0.48);
        split.setDividerLocation(290);
        split.setDividerSize(5);
        split.setBorder(null);
        split.setBackground(C_BG);
        split.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override public void paint(Graphics g) {
                        g.setColor(C_BORDER);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
            }
        });
        add(split, BorderLayout.CENTER);

        // ── Events ────────────────────────────────────────────────────────────
        btnAddToCart.addActionListener(this);
        btnRemoveLine.addActionListener(this);
        btnUpdateQty.addActionListener(this);
        btnPay.addActionListener(this);
        btnCreateMember.addActionListener(this);
        btnApplyDiscount.addActionListener(this);
//        btnRefreshMembers.addActionListener(this);

        btnAddToCart.addMouseListener(this);
        btnRemoveLine.addMouseListener(this);
        btnUpdateQty.addMouseListener(this);
        btnPay.addMouseListener(this);
        btnCreateMember.addMouseListener(this);
        btnApplyDiscount.addMouseListener(this);
//        btnRefreshMembers.addMouseListener(this);

        lstMenu.addMouseListener(this);

        txtReceipt.setEditable(false);
        txtReceipt.setFont(mono(Font.PLAIN, 12));

        ButtonGroup g = new ButtonGroup();
        g.add(rbCash); g.add(rbQR);

        loadMenu();
        updateTotal();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Tab builders (Giữ nguyên cấu trúc UI)
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildAddTab() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(C_BG);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        lstMenu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstMenu.setCellRenderer(new DrinkCardRenderer());
        lstMenu.setFixedCellHeight(94);
        lstMenu.setBackground(C_BG);
        lstMenu.setBorder(null);

        JScrollPane menuScroll = new JScrollPane(lstMenu);
        menuScroll.setBorder(BorderFactory.createEmptyBorder());
        menuScroll.getViewport().setBackground(C_BG);
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel leftCard = darkCard(new BorderLayout(0, 6));
        leftCard.add(makeHeader("☕  MENU", C_GOLD, C_ESPRESSO), BorderLayout.NORTH);
        leftCard.add(menuScroll, BorderLayout.CENTER);

        JPanel right = darkCard(new GridBagLayout());
        right.setBorder(new EmptyBorder(16, 18, 16, 18));

        GridBagConstraints c = new GridBagConstraints();
        c.insets  = new Insets(5, 6, 5, 6);
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        int r = 0;

        lblSelectedName.setFont(ui(Font.BOLD, 15));
        lblSelectedName.setForeground(C_GOLD_L);
        lblSelectedPrice.setFont(ui(Font.BOLD, 14));
        lblSelectedPrice.setForeground(C_GREEN);

        c.gridx=0; c.gridy=r; c.gridwidth=2; right.add(lblSelectedName, c); r++;
        c.gridx=0; c.gridy=r; c.gridwidth=2;
        JSeparator sep0 = new JSeparator();
        sep0.setForeground(C_BORDER); sep0.setBackground(C_BORDER);
        right.add(sep0, c); r++;
        c.gridx=0; c.gridy=r; c.gridwidth=2; right.add(lblSelectedPrice, c); r++;
        c.gridwidth=1;

        right.add(fieldLabel("Đá (%)"),    gbc(0,r)); styleSlider(slIce);   right.add(slIce,   gbc(1,r)); r++;
        right.add(fieldLabel("Đường (%)"), gbc(0,r)); styleSlider(slSugar); right.add(slSugar, gbc(1,r)); r++;
        right.add(fieldLabel("Số lượng"),  gbc(0,r)); styleSpinner(spQty);  right.add(spQty,   gbc(1,r)); r++;

        c.gridx=0; c.gridy=r; c.anchor=GridBagConstraints.NORTH; right.add(fieldLabel("Ghi chú"), c);
        txtNote.setFont(ui(Font.PLAIN, 13));
        txtNote.setBackground(C_SURFACE2);
        txtNote.setForeground(C_CREAM);
        txtNote.setCaretColor(C_GOLD);
        txtNote.setBorder(BorderFactory.createCompoundBorder(
                new DarkRoundBorder(C_BORDER, 8, 1), new EmptyBorder(6,8,6,8)));
        c.gridx=1; c.gridy=r; right.add(new JScrollPane(txtNote), c); r++;

        darkButton(btnAddToCart, C_ACCENT, C_CREAM);
        c.gridx=0; c.gridy=r; c.gridwidth=2; c.insets=new Insets(14,6,6,6); right.add(btnAddToCart, c);

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCard, right);
        sp.setDividerLocation(340);
        sp.setDividerSize(4);
        sp.setBorder(null);
        sp.setBackground(C_BG);

        root.add(sp, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildEditTab() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(C_BG);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel card = darkCard(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setOpaque(false);
        row.add(fieldLabel("Số lượng mới:"));
        styleSpinner(spNewQty);
        row.add(spNewQty);
        darkButton(btnUpdateQty, C_ORANGE, C_CREAM);
        row.add(btnUpdateQty);
        darkButton(btnRemoveLine, C_RED, C_CREAM);
        row.add(btnRemoveLine);
        card.add(row, BorderLayout.CENTER);

        JTextArea hint = new JTextArea(
                "Hướng dẫn:\n" +
                "  • Click 1 dòng trong bảng hóa đơn (phía trên)\n" +
                "  • Sang tab này để xóa hoặc đổi số lượng\n");
        hint.setEditable(false);
        hint.setOpaque(false);
        hint.setFont(ui(Font.ITALIC, 12));
        hint.setForeground(C_SUBTEXT);
        card.add(hint, BorderLayout.SOUTH);

        root.add(makeHeader("✏️  SỬA / XÓA", C_CREAM, C_ORANGE), BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildMemberTab() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(C_BG);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel card = darkCard(new GridBagLayout());
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets  = new Insets(8, 8, 8, 8);
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        int r = 0;

        JLabel title = new JLabel("TẠO THÀNH VIÊN MỚI");
        title.setForeground(C_GOLD_L);
        title.setFont(ui(Font.BOLD, 16));
        c.gridx=0; c.gridy=r; c.gridwidth=2;
        card.add(title, c);
        r++;

        generatedMemberCode = generateMemberCode();
        lblAutoMemberCode.setText(generatedMemberCode);
        lblAutoMemberCode.setFont(mono(Font.BOLD, 14));
        lblAutoMemberCode.setForeground(C_GOLD);
        lblAutoMemberCode.setOpaque(true);
        lblAutoMemberCode.setBackground(C_SURFACE2);
        lblAutoMemberCode.setBorder(BorderFactory.createCompoundBorder(
                new DarkRoundBorder(C_BORDER, 8, 1),
                new EmptyBorder(6, 10, 6, 10)));

        JButton btnRegen = new JButton("↺");
        btnRegen.setToolTipText("Sinh mã mới");
        darkButton(btnRegen, C_SURFACE2, C_SUBTEXT);
        btnRegen.setFont(ui(Font.BOLD, 13));
        btnRegen.setBorder(new EmptyBorder(6, 12, 6, 12));
        btnRegen.addActionListener(ev -> {
            generatedMemberCode = generateMemberCode();
            lblAutoMemberCode.setText(generatedMemberCode);
        });

        JPanel codeRow = new JPanel(new BorderLayout(6, 0));
        codeRow.setOpaque(false);
        codeRow.add(lblAutoMemberCode, BorderLayout.CENTER);
        codeRow.add(btnRegen, BorderLayout.EAST);

        c.gridwidth=1;
        c.gridx=0; c.gridy=r; card.add(fieldLabel("Mã Thành Viên"),  c);
        c.gridx=1; c.gridy=r; card.add(codeRow, c); r++;

        c.gridx=0; c.gridy=r; card.add(fieldLabel("Tên Thành Viên"), c);
        c.gridx=1; c.gridy=r; card.add(darkField(txtNewMemberName),   c); r++;

        c.gridx=0; c.gridy=r; card.add(fieldLabel("Loại Thành Viên"), c);
        c.gridx=1; c.gridy=r; styleCombo(cbMemberType); card.add(cbMemberType, c); r++;

        darkButton(btnCreateMember, C_BLUE, Color.WHITE);
        c.gridx=0; c.gridy=r; c.gridwidth=2;
        card.add(btnCreateMember, c);

        JTextArea hint = new JTextArea(
                "Mã thành viên được sinh tự động.\n" +
                "Nhấn ↺ để sinh lại mã khác nếu muốn.\n\n" +
                "Loại:\n" +
                "  • Bạc       — giảm 2%\n" +
                "  • Vàng      — giảm 5%\n" +
                "  • Bạch Kim  — giảm 10%\n");
        hint.setEditable(false);
        hint.setOpaque(false);
        hint.setFont(ui(Font.ITALIC, 12));
        hint.setForeground(C_SUBTEXT);
        hint.setBorder(new EmptyBorder(10, 8, 0, 8));

        root.add(makeHeader("👤  TẠO THÀNH VIÊN", Color.WHITE, C_BLUE), BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);
        root.add(hint, BorderLayout.SOUTH);
        return root;
    }

//    private JPanel buildMemberListTab() {
//        JPanel root = new JPanel(new BorderLayout(10, 10));
//        root.setBackground(C_BG);
//        root.setBorder(new EmptyBorder(10, 10, 10, 10));

//        tblMembers.setRowHeight(40);
//        tblMembers.setFont(ui(Font.PLAIN, 13));
//        tblMembers.getTableHeader().setFont(ui(Font.BOLD, 13));
//        tblMembers.getTableHeader().setBackground(C_SURFACE2);
//        tblMembers.getTableHeader().setForeground(C_GOLD);
//        tblMembers.getTableHeader().setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
//        tblMembers.getTableHeader().setPreferredSize(new Dimension(0, 40));
//        tblMembers.setShowGrid(false);
//        tblMembers.setIntercellSpacing(new Dimension(0, 3));
//        tblMembers.setSelectionBackground(new Color(212, 170, 90, 55));
//        tblMembers.setSelectionForeground(C_CREAM);
//        tblMembers.setBackground(C_SURFACE);
//        tblMembers.setForeground(C_CREAM);
//        tblMembers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//
//        tblMembers.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
//            @Override
//            public Component getTableCellRendererComponent(
//                    JTable table, Object value, boolean isSelected,
//                    boolean hasFocus, int row, int column) {
//                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//                setBorder(new EmptyBorder(0, 14, 0, 14));
//                setFont(ui(Font.PLAIN, 13));
//
//                if (!isSelected) {
//                    setBackground(row % 2 == 0 ? C_SURFACE : C_SURFACE2);
//                    setForeground(C_CREAM);
//                } else {
//                    setBackground(new Color(212, 170, 90, 55));
//                    setForeground(C_GOLD_L);
//                }
//
//                if (column == 2 && value != null) {
//                    String hang = value.toString();
//                    if (!isSelected) {
//                        switch (hang) {
//                            case "Bạc"      -> { setForeground(C_SILVER);   setFont(ui(Font.BOLD, 13)); }
//                            case "Vàng"     -> { setForeground(C_VANG);     setFont(ui(Font.BOLD, 13)); }
//                            case "Bạch Kim" -> { setForeground(C_PLATINUM); setFont(ui(Font.BOLD, 13)); }
//                        }
//                    }
//                }
//
//                if (column == 0) {
//                    setFont(mono(Font.BOLD, 13));
//                    if (!isSelected) setForeground(C_GOLD);
//                }
//
//                return this;
//            }
//        });
//
//        tblMembers.getColumnModel().getColumn(0).setPreferredWidth(130);
//        tblMembers.getColumnModel().getColumn(1).setPreferredWidth(220);
//        tblMembers.getColumnModel().getColumn(2).setPreferredWidth(100);
//
//        JScrollPane scroll = new JScrollPane(tblMembers);
//        scroll.setBorder(BorderFactory.createEmptyBorder());
//        scroll.getViewport().setBackground(C_SURFACE);
//        scroll.getVerticalScrollBar().setBackground(C_SURFACE2);
//
//        JPanel tableCard = darkCard(new BorderLayout(0, 0));
//        tableCard.add(scroll, BorderLayout.CENTER);
//
//        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
//        toolbar.setOpaque(false);
//        toolbar.setBorder(new EmptyBorder(0, 0, 8, 0));
//
//        JLabel lblStats = new JLabel("Đang tải...");
//        lblStats.setForeground(C_SUBTEXT);
//        lblStats.setFont(ui(Font.ITALIC, 12));
//
//        tblMembers.getModel().addTableModelListener(e -> {
//            int total = memberTableModel.getRowCount();
//            long bac  = countByHang("Bạc");
//            long vang  = countByHang("Vàng");
//            long bach  = countByHang("Bạch Kim");
//            lblStats.setText(String.format(
//                    "Tổng: %d thành viên   |   🥈 Bạc: %d   |   🥇 Vàng: %d   |   💎 Bạch Kim: %d",
//                    total, bac, vang, bach));
//        });
//
//        darkButton(btnRefreshMembers, C_SURFACE2, C_GOLD);
//        btnRefreshMembers.setBorder(new EmptyBorder(8, 18, 8, 18));
//
//        toolbar.add(lblStats, BorderLayout.CENTER);
//        toolbar.add(btnRefreshMembers, BorderLayout.EAST);
//
//        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 6));
//        legend.setOpaque(false);
//
//        legend.add(makeLegendDot(C_SILVER,   "Bạc — giảm 2%"));
//        legend.add(makeLegendDot(C_VANG,     "Vàng — giảm 5%"));
//        legend.add(makeLegendDot(C_PLATINUM, "Bạch Kim — giảm 10%"));
//
//        JPanel south = new JPanel(new BorderLayout());
//        south.setOpaque(false);
//        south.add(legend, BorderLayout.WEST);
//
//        JPanel centerWrapper = new JPanel(new BorderLayout(0, 6));
//        centerWrapper.setOpaque(false);
//        centerWrapper.add(toolbar,    BorderLayout.NORTH);
//        centerWrapper.add(tableCard,  BorderLayout.CENTER);
//        centerWrapper.add(south,      BorderLayout.SOUTH);
//
//        root.add(makeHeader("🏆  DANH SÁCH THÀNH VIÊN CHƯƠNG TRÌNH", C_GOLD_L, C_SURFACE2), BorderLayout.NORTH);
//        root.add(centerWrapper, BorderLayout.CENTER);
//
//        return root;
//    }

    private JPanel buildSearchTab() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(C_BG);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel searchBar = darkCard(new GridBagLayout());
        searchBar.setBorder(new EmptyBorder(12, 16, 12, 16));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill   = GridBagConstraints.HORIZONTAL;

        c.gridx=0; c.gridy=0; c.weightx=0;
        searchBar.add(fieldLabel("Từ khóa:"), c);

        c.gridx=1; c.gridy=0; c.weightx=1;
        darkField(txtSearchQuery);
        searchBar.add(txtSearchQuery, c);

        c.gridx=2; c.gridy=0; c.weightx=0;
        styleCombo(cbSearchType);
        searchBar.add(cbSearchType, c);

        darkButton(btnSearch, C_GOLD, C_BG);
        btnSearch.setBorder(new EmptyBorder(8, 18, 8, 18));
        c.gridx=3; c.gridy=0; c.weightx=0;
        searchBar.add(btnSearch, c);

        darkButton(btnResetSearch, C_SURFACE2, C_SUBTEXT);
        btnResetSearch.setBorder(new EmptyBorder(8, 18, 8, 18));
        c.gridx=4; c.gridy=0; c.weightx=0;
        searchBar.add(btnResetSearch, c);

        tblSearchResult.setRowHeight(38);
        tblSearchResult.setFont(ui(Font.PLAIN, 13));
        tblSearchResult.getTableHeader().setFont(ui(Font.BOLD, 13));
        tblSearchResult.getTableHeader().setBackground(C_SURFACE2);
        tblSearchResult.getTableHeader().setForeground(C_GOLD);
        tblSearchResult.getTableHeader().setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        tblSearchResult.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tblSearchResult.setShowGrid(false);
        tblSearchResult.setIntercellSpacing(new Dimension(0, 3));
        tblSearchResult.setSelectionBackground(new Color(212, 170, 90, 55));
        tblSearchResult.setSelectionForeground(C_CREAM);
        tblSearchResult.setBackground(C_SURFACE);
        tblSearchResult.setForeground(C_CREAM);
        tblSearchResult.setDefaultRenderer(Object.class, new DarkStripedRenderer());
        tblSearchResult.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(tblSearchResult);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(C_SURFACE);
        scroll.getVerticalScrollBar().setBackground(C_SURFACE2);

        JPanel tableCard = darkCard(new BorderLayout(0, 0));
        tableCard.add(scroll, BorderLayout.CENTER);

        JLabel lblHint = new JLabel("  Nhập từ khóa, chọn loại tìm và nhấn 🔍  TÌM KIẾM  (hoặc nhấn Enter)");
        lblHint.setFont(ui(Font.ITALIC, 12));
        lblHint.setForeground(C_SUBTEXT);
        lblHint.setBorder(new EmptyBorder(4, 4, 0, 0));

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 8));
        centerWrapper.setOpaque(false);
        centerWrapper.add(searchBar, BorderLayout.NORTH);
        centerWrapper.add(tableCard, BorderLayout.CENTER);
        centerWrapper.add(lblHint,   BorderLayout.SOUTH);

        root.add(makeHeader("🔍  TÌM KIẾM", C_GOLD_L, C_SURFACE2), BorderLayout.NORTH);
        root.add(centerWrapper, BorderLayout.CENTER);

        btnSearch.addActionListener(ev -> doSearch());
        btnSearch.addMouseListener(this);
        btnResetSearch.addActionListener(ev -> {
            txtSearchQuery.setText("");
            searchResultModel.setRowCount(0);
        });
        btnResetSearch.addMouseListener(this);
        txtSearchQuery.addActionListener(ev -> doSearch());

        return root;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase().trim();
    }

    private void doSearch() {
        String keyword = normalize(txtSearchQuery.getText());
        searchResultModel.setRowCount(0);

        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập từ khóa tìm kiếm.", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int type = cbSearchType.getSelectedIndex();
        try {
            List<CartItem> cartItems = cartModel.getData();
            if (cartItems.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Hóa đơn tạm thời đang trống.", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (type == 0) {
                for (CartItem it : cartItems) {
                    Product p = it.product;
                    if (p.name != null && normalize(p.name).contains(keyword)) {
                        searchResultModel.addRow(new Object[]{
                                p.code, p.name, p.category,
                                p.price != null ? p.price.toPlainString() + " ₫" : "-"
                        });
                    }
                }
            } else if (type == 1) {
                for (CartItem it : cartItems) {
                    Product p = it.product;
                    if (p.code != null && normalize(p.code).contains(keyword)) {
                        searchResultModel.addRow(new Object[]{
                                p.code, p.name, p.category,
                                p.price != null ? p.price.toPlainString() + " ₫" : "-"
                        });
                    }
                }
            } else {
                for (CartItem it : cartItems) {
                    Product p = it.product;
                    if (p.price != null && normalize(p.price.toPlainString()).contains(keyword)) {
                        searchResultModel.addRow(new Object[]{
                                p.code, p.name, p.category,
                                p.price.toPlainString() + " ₫"
                        });
                    }
                }
            }

            if (searchResultModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy kết quả nào khớp với \"" + txtSearchQuery.getText().trim() + "\".",
                        "Kết quả", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi tìm kiếm: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel makeLegendDot(Color color, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("●") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 2, 12, 12);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(14, 16); }
        };
        JLabel lbl = new JLabel(text);
        lbl.setFont(ui(Font.PLAIN, 11));
        lbl.setForeground(C_SUBTEXT);
        p.add(dot);
        p.add(lbl);
        return p;
    }

//    private long countByHang(String hang) {
//        long count = 0;
//        for (int i = 0; i < memberTableModel.getRowCount(); i++) {
//            if (hang.equals(memberTableModel.getValueAt(i, 2))) count++;
//        }
//        return count;
//    }
//
//    private void loadMemberList() {
//        memberTableModel.setRowCount(0);
//        try {
//            List<ThanhVien> list = thanhVienDAO.findAll();
//            for (ThanhVien tv : list) {
//                String hangVN = switch (tv.loaiThanhVien) {
//                    case "BAC"      -> "Bạc";
//                    case "VANG"     -> "Vàng";
//                    case "BACH_KIM" -> "Bạch Kim";
//                    default         -> tv.loaiThanhVien;
//                };
//                memberTableModel.addRow(new Object[]{
//                        tv.maThanhVien,
//                        tv.tenThanhVien,
//                        hangVN
//                });
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            JOptionPane.showMessageDialog(this,
//                    "Lỗi tải danh sách thành viên: " + ex.getMessage(),
//                    "Lỗi", JOptionPane.ERROR_MESSAGE);
//        }
//    }

    private String generateMemberCode() {
        return "TV" + UUID.randomUUID().toString()
                          .replace("-", "")
                          .substring(0, 6)
                          .toUpperCase();
    }

    private JPanel buildPayTab() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(C_BG);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel discountCard = darkCard(new GridBagLayout());
        discountCard.setBorder(new EmptyBorder(14, 18, 14, 18));

        GridBagConstraints c = new GridBagConstraints();
        c.insets  = new Insets(6, 6, 6, 6);
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        c.gridx=0; c.gridy=0; c.weightx=0;
        discountCard.add(fieldLabel("Mã thành viên:"), c);
        c.gridx=1; c.gridy=0; c.weightx=1;
        discountCard.add(darkField(txtMember), c);

        c.gridx=0; c.gridy=1; c.weightx=0;
        discountCard.add(fieldLabel("Mã khuyến mãi:"), c);
        c.gridx=1; c.gridy=1; c.weightx=1;
        discountCard.add(darkField(txtPromo), c);

        darkButton(btnApplyDiscount, C_GOLD, C_BG);
        c.gridx=0; c.gridy=2; c.gridwidth=2; c.weightx=1;
        discountCard.add(btnApplyDiscount, c);

        JPanel summaryCard = darkCard(new GridBagLayout());
        summaryCard.setBorder(new EmptyBorder(12, 18, 12, 18));

        lblTotal.setFont(ui(Font.PLAIN, 14));
        lblTotal.setForeground(C_SUBTEXT);
        lblDiscount.setFont(ui(Font.PLAIN, 14));
        lblDiscount.setForeground(C_RED);
        lblFinalTotal.setFont(ui(Font.BOLD, 20));
        lblFinalTotal.setForeground(C_GREEN);

        GridBagConstraints sc = new GridBagConstraints();
        sc.insets = new Insets(4, 8, 4, 8);
        sc.fill   = GridBagConstraints.HORIZONTAL;
        sc.weightx = 1;

        sc.gridx=0; sc.gridy=0; summaryCard.add(lblTotal,    sc);
        sc.gridx=0; sc.gridy=1; summaryCard.add(lblDiscount, sc);
        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDER); sep.setBackground(C_BORDER);
        sc.gridx=0; sc.gridy=2; summaryCard.add(sep, sc);
        sc.gridx=0; sc.gridy=3; summaryCard.add(lblFinalTotal, sc);

        txtPreviewReceipt.setEditable(false);
        txtPreviewReceipt.setFont(mono(Font.PLAIN, 12));
        txtPreviewReceipt.setBackground(new Color(22, 18, 14));
        txtPreviewReceipt.setForeground(C_CREAM);
        txtPreviewReceipt.setCaretColor(C_GOLD);
        txtPreviewReceipt.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel previewCard = darkCard(new BorderLayout(0, 6));
        previewCard.add(makeHeader("📋  HÓA ĐƠN TẠM (XEM TRƯỚC)", C_GOLD_L, C_SURFACE2), BorderLayout.NORTH);
        previewCard.add(new JScrollPane(txtPreviewReceipt), BorderLayout.CENTER);

        JPanel payMethodCard = darkCard(new FlowLayout(FlowLayout.LEFT, 18, 14));
        styleRadio(rbCash); styleRadio(rbQR);
        payMethodCard.add(rbCash);
        payMethodCard.add(rbQR);
        darkButton(btnPay, C_GREEN, Color.WHITE);
        payMethodCard.add(btnPay);

        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setOpaque(false);
        leftPanel.add(discountCard,  BorderLayout.NORTH);
        leftPanel.add(summaryCard,   BorderLayout.CENTER);
        leftPanel.add(payMethodCard, BorderLayout.SOUTH);

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, previewCard);
        sp.setDividerLocation(340);
        sp.setDividerSize(4);
        sp.setBorder(null);
        sp.setBackground(C_BG);

        root.add(makeHeader("💳  THANH TOÁN", Color.WHITE, C_GREEN), BorderLayout.NORTH);
        root.add(sp, BorderLayout.CENTER);
        return root;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Hóa đơn tạm & Khuyến Mãi
    // ══════════════════════════════════════════════════════════════════════════
    private void refreshPreviewReceipt() {
        if (cartModel.getData().isEmpty()) {
            txtPreviewReceipt.setText("( Chưa có món nào trong hóa đơn )");
            lblTotal.setText("Tổng tiền: 0 VND");
            lblDiscount.setText("Giảm giá: 0 VND");
            lblFinalTotal.setText("THÀNH TIỀN: 0 VND");
            return;
        }

        String promoCode  = txtPromo.getText().trim();
        String memberCode = txtMember.getText().trim();
        
     // 1. Tính tổng tiền gốc từ giỏ hàng
        BigDecimal rawTotal = BigDecimal.ZERO;
        for (CartItem it : cartModel.getData()) {
            rawTotal = rawTotal.add(it.unitPrice.multiply(BigDecimal.valueOf(it.quantity)));
        }

        BigDecimal promoDiscountAmt = rawTotal.multiply(BigDecimal.valueOf(currentDiscountPercent / 100.0));
        BigDecimal afterPromo = rawTotal.subtract(promoDiscountAmt);

        // 3. Tính giảm giá thành viên (nếu bạn có logic này, nếu không thì afterMember = afterPromo)
        BigDecimal afterMember = applyMemberDiscount(afterPromo, memberCode);

        // 4. Tổng số tiền được giảm cuối cùng
        BigDecimal totalDiscountAmt = rawTotal.subtract(afterMember);

        lblTotal.setText("Tổng tiền: " + rawTotal.toPlainString() + " VND");
        lblDiscount.setText("Giảm giá: -" + promoDiscountAmt.toPlainString() + " VND");
        lblFinalTotal.setText("THÀNH TIỀN: " + afterMember.toPlainString() + " VND");

        StringBuilder sb = new StringBuilder();
        sb.append("===== COFFEE SHOP — HÓA ĐƠN TẠM =====\n");
        sb.append("NV     : ").append(currentUser.fullName).append("\n");
        if (!memberCode.isEmpty())
            sb.append("TV     : ").append(memberCode).append("\n");
        if (!promoCode.isEmpty())
            sb.append("PROMO  : ").append(promoCode).append("\n");
        sb.append("---------------------------------------\n");
        sb.append(String.format("%-20s %4s  %12s\n", "Tên món", "SL", "Thành tiền"));
        sb.append("---------------------------------------\n");
        for (CartItem it : cartModel.getData()) {
            BigDecimal lineTotalRaw = it.unitPrice.multiply(BigDecimal.valueOf(it.quantity));
            sb.append(String.format("%-20s %4d  %12s\n",
                    truncate(it.product.name, 20), it.quantity, lineTotalRaw.toPlainString()));
            if (it.icePercent != 50 || it.sugarPercent != 50)
                sb.append(String.format("   Đá: %d%%  Đường: %d%%\n", it.icePercent, it.sugarPercent));
            if (it.note != null && !it.note.isEmpty())
                sb.append("   Ghi chú: ").append(it.note).append("\n");
        }
        sb.append("---------------------------------------\n");
        sb.append(String.format("%-26s %12s\n", "Tổng gốc:",  rawTotal.toPlainString()));
        sb.append(String.format("%-26s %12s\n", "Giảm giá:", "-" + promoDiscountAmt.toPlainString()));
        sb.append("=======================================\n");
        sb.append(String.format("%-26s %12s\n", "THÀNH TIỀN:", afterMember.toPlainString() + " VND"));
        sb.append("=======================================\n");

        txtPreviewReceipt.setText(sb.toString());
        txtPreviewReceipt.setCaretPosition(0);
    }
    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }

    private BigDecimal applyMemberDiscount(BigDecimal amount, String memberCode) {
        if (memberCode.isEmpty()) return amount;
        try {
            ThanhVien tv = thanhVienDAO.findActiveByCode(memberCode);
            if (tv == null) return amount;
            double pct = switch (tv.loaiThanhVien) {
                case "BAC"      -> 0.02;
                case "VANG"     -> 0.05;
                case "BACH_KIM" -> 0.10;
                default         -> 0.0;
            };
            return amount.multiply(BigDecimal.valueOf(1.0 - pct));
        } catch (Exception e) {
            e.printStackTrace();
            return amount;
        }
    }

    private void loadMenu() {
        try {
            menuModel.clear();
            List<Product> list = productDAO.findAllActive();
            for (Product p : list) menuModel.addElement(p);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải menu: " + ex.getMessage());
        }
    }

    private void updateTotal() {
        totalGross = BigDecimal.ZERO;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            totalGross = totalGross.add(cartModel.getAt(i).lineTotal);
        }

        String memberCode = txtMember.getText().trim();
        
        BigDecimal promoDiscountPercent = BigDecimal.valueOf(currentDiscountPercent / 100.0);
        BigDecimal amountAfterPromo = totalGross.multiply(BigDecimal.ONE.subtract(promoDiscountPercent));
        
        finalTotal = applyMemberDiscount(amountAfterPromo, memberCode);
        
        discountAmount = totalGross.subtract(finalTotal);

        lblTotal.setText(String.format("%,.0f VND", totalGross.doubleValue()));
        lblDiscount.setText(String.format("-%,.0f VND", discountAmount.doubleValue()));
        lblFinalTotal.setText(String.format("%,.0f VND", finalTotal.doubleValue()));

        // 7. Cập nhật lại vùng xem trước hóa đơn
        refreshPreviewReceipt();
    }
    
    private BigDecimal applyPromo(BigDecimal rawTotal, String promoCode) {
        if (promoCode == null) return rawTotal;
        promoCode = promoCode.trim().toUpperCase();
        if (promoCode.isEmpty()) return rawTotal;
        if ("GIAM10".equals(promoCode)) return rawTotal.multiply(BigDecimal.valueOf(0.9));
        if ("GIAM5K".equals(promoCode)) {
            BigDecimal v = rawTotal.subtract(BigDecimal.valueOf(5000));
            return v.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : v;
        }
        return rawTotal;
    }

    private void onSelectProduct(Product p) {
        selectedProduct = p;
        if (p == null) {
            lblSelectedName.setText("Chọn món từ menu bên trái");
            lblSelectedPrice.setText("Giá: -");
            return;
        }
        lblSelectedName.setText(p.code + "  —  " + p.name);
        lblSelectedPrice.setText("Giá: " + p.price.toPlainString() + " VND");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Object src = e.getSource();

            if (src == btnApplyDiscount) {
            	String promoCode = txtPromo.getText().trim();
                
                if (promoCode.isEmpty()) {
                    currentDiscountPercent = 0.0;
                    currentPromoCode = "";
                    JOptionPane.showMessageDialog(this, "Đã hủy áp dụng mã khuyến mãi!");
                    updateTotal(); 
                    return;
                }

                try {
                    Promotion p = promotionDAO.findByCode(promoCode);

                    if (p != null) {
                        currentDiscountPercent = p.discountPercent; // Lấy % giảm giá từ DB
                        currentPromoCode = p.code;
                        
                        JOptionPane.showMessageDialog(this, "Áp dụng thành công mã " + currentPromoCode + " (-" + currentDiscountPercent + "%)");
                    } else {
                        JOptionPane.showMessageDialog(this, "Mã khuyến mãi không tồn tại, chưa đến hạn hoặc đã hết hạn!");
                        currentDiscountPercent = 0.0;
                        currentPromoCode = "";
                    }
                    
                    updateTotal(); 
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi kiểm tra mã: " + ex.getMessage());
                }
                return;
            }

//            if (src == btnRefreshMembers) {
//                loadMemberList();
//                return;
//            }

            if (src == btnCreateMember) {
                onCreateMember();
                return;
            }

            // GỘP ITEM NẾU TRÙNG LẶP SẢN PHẨM & TÙY CHỌN
            if (src == btnAddToCart) {
                if (selectedProduct == null) { JOptionPane.showMessageDialog(this, "Bạn chưa chọn món!"); return; }
                int qty = (int) spQty.getValue();
                
                boolean found = false;
                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    CartItem existing = cartModel.getAt(i);
                    // Kiểm tra trùng ID và trùng chính xác tùy chọn (đá, đường, note)
                    if (existing.product.id == selectedProduct.id &&
                        existing.icePercent == slIce.getValue() &&
                        existing.sugarPercent == slSugar.getValue() &&
                        existing.note.equals(txtNote.getText().trim())) {
                        
                        existing.quantity += qty;
                        existing.lineTotal = existing.unitPrice.multiply(BigDecimal.valueOf(existing.quantity));
                        cartModel.fireTableDataChanged();
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    CartItem it = new CartItem();
                    it.product      = selectedProduct;
                    it.memberCode   = "";
                    it.promoCode    = "";
                    it.icePercent   = slIce.getValue();
                    it.sugarPercent = slSugar.getValue();
                    it.quantity     = qty;
                    it.note         = txtNote.getText().trim();
                    it.unitPrice    = selectedProduct.price;
                    it.lineTotal    = it.unitPrice.multiply(BigDecimal.valueOf(qty));
                    cartModel.add(it);
                }

                updateTotal();
                spQty.setValue(1);
                txtNote.setText("");
                tabs.setSelectedIndex(0);
                return;
            }

            if (src == btnRemoveLine) {
                int row = tblCart.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn dòng cần xóa!"); return; }
                cartModel.removeAt(row);
                updateTotal();
                return;
            }

            if (src == btnUpdateQty) {
                int row = tblCart.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn dòng cần sửa!"); return; }
                int newQty = (int) spNewQty.getValue();
                CartItem it = cartModel.getAt(row);
                it.quantity  = newQty;
                it.lineTotal = it.unitPrice.multiply(BigDecimal.valueOf(newQty));
                cartModel.fireTableDataChanged();
                updateTotal();
                return;
            }

            if (src == btnPay) onPay();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    private void onCreateMember() throws Exception {
        String name   = txtNewMemberName.getText().trim();
        String typeVN = (String) cbMemberType.getSelectedItem();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên thành viên!");
            return;
        }

        String code   = generatedMemberCode;
        String typeDb = switch (typeVN) {
            case "Bạc"  -> "BAC";
            case "Vàng" -> "VANG";
            default     -> "BACH_KIM";
        };

        ThanhVien tv = new ThanhVien();
        tv.maThanhVien   = code;
        tv.tenThanhVien  = name;
        tv.loaiThanhVien = typeDb;

        boolean ok = thanhVienDAO.insert(tv);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Tạo thành viên thành công!\nMã: " + code);
            txtNewMemberName.setText("");

            generatedMemberCode = generateMemberCode();
            lblAutoMemberCode.setText(generatedMemberCode);

            txtMember.setText(code);
            tabs.setSelectedIndex(3);
        } else {
            JOptionPane.showMessageDialog(this, "Không tạo được thành viên.");
        }
    }

    private void onPay() throws Exception {
        if (cartModel.getData().isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Hóa đơn trống!"); 
            return; 
        }

        String promoCode  = txtPromo.getText().trim();
        String memberCode = txtMember.getText().trim();

        BigDecimal rawTotal = BigDecimal.ZERO;
        for (CartItem it : cartModel.getData())
            rawTotal = rawTotal.add(it.unitPrice.multiply(BigDecimal.valueOf(it.quantity)));

        BigDecimal discountAmt = rawTotal.multiply(BigDecimal.valueOf(currentDiscountPercent / 100.0));
        BigDecimal afterPromo = rawTotal.subtract(discountAmt);
        
        BigDecimal finalTotal = applyMemberDiscount(afterPromo, memberCode);

        String invoiceCode = "HD" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        if (rbQR.isSelected()) {
            boolean ok = showQrPaymentDialog(invoiceCode, finalTotal);
            if (!ok) { txtReceipt.setText("Đã hủy thanh toán QR."); return; }
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Xác nhận đã nhận tiền mặt?\nMã HĐ: " + invoiceCode
                    + "\nThành tiền: " + String.format("%,.0f", finalTotal.doubleValue()) + " VND",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) { txtReceipt.setText("Đã hủy thanh toán tiền mặt."); return; }
        }

        FileResult r = finalizePaidInvoiceAndExportPdf(invoiceCode, finalTotal);
        
        renderReceiptToTextArea(invoiceCode, rawTotal, finalTotal, rbQR.isSelected() ? "QR" : "CASH");
    }


    private static class FileResult { String pdfPath; int invoiceId; }

    private FileResult finalizePaidInvoiceAndExportPdf(String invoiceCode, BigDecimal finalTotal) throws Exception {
        Invoice inv = new Invoice();
        inv.code         = invoiceCode;
        inv.createdAt    = LocalDateTime.now();
        inv.createdBy    = currentUser.id;
        inv.customerName = txtMember.getText().trim();
        inv.note         = "PAY=" + (rbQR.isSelected() ? "QR" : "CASH")
                         + " PROMO=" + currentPromoCode;
        inv.totalAmount  = finalTotal;
        inv.status       = "PAID";
        int invoiceId    = invoiceDAO.insertInvoice(inv);

        List<InvoiceItem> items = new java.util.ArrayList<>();
        for (CartItem c : cartModel.getData()) {
            InvoiceItem it = new InvoiceItem();
            it.invoiceId = invoiceId; it.productId = c.product.id;
            it.qty = c.quantity; it.unitPrice = c.unitPrice; it.lineTotal = c.lineTotal;
            items.add(it);
        }
        invoiceDAO.replaceItems(invoiceId, items);

        inv.createdByName = currentUser.fullName;
        String baseDir  = System.getProperty("user.home") + "/Desktop/invoices";
        String pdfPath  = new java.io.File(baseDir, invoiceCode + ".pdf").getAbsolutePath();
        java.io.File pdf = PdfInvoiceExporter.exportReceiptPdf(pdfPath, inv, cartModel.getData());
        try { if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(pdf); } catch (Exception ignored) {}

        FileResult res = new FileResult();
        res.pdfPath   = pdf.getAbsolutePath();
        res.invoiceId = invoiceId;
        return res;
    }

    private boolean showQrPaymentDialog(String invoiceCode, BigDecimal total) {
        try {
            JLabel qrLabel = new JLabel(loadQrIcon());
            qrLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JLabel title = new JLabel("Quét QR để thanh toán");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            JLabel sub = new JLabel("Mã HĐ: " + invoiceCode + " • Thành tiền: " + total.toPlainString() + " VND");
            sub.setForeground(new Color(100, 100, 100));
            JPanel top = new JPanel();
            top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
            top.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
            top.add(title); top.add(Box.createVerticalStrut(4)); top.add(sub);
            JPanel content = new JPanel(new BorderLayout());
            content.add(top, BorderLayout.NORTH);
            content.add(qrLabel, BorderLayout.CENTER);
            Object[] options = {"Hủy", "Đã thanh toán"};
            int choice = JOptionPane.showOptionDialog(this, content, "Thanh toán QR",
                    JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
            return choice == 1;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải QR: " + ex.getMessage());
            return false;
        }
    }

    private ImageIcon loadQrIcon() throws Exception {
        if (cachedQrIcon != null) return cachedQrIcon;
        java.net.URL res = getClass().getResource(QR_RESOURCE_PATH);
        if (res == null) throw new RuntimeException("Không tìm thấy ảnh QR: " + QR_RESOURCE_PATH);
        BufferedImage img = javax.imageio.ImageIO.read(res);
        if (img == null) throw new RuntimeException("Không đọc được ảnh QR");
        int w = 320, h = (int)(img.getHeight() * (w / (double) img.getWidth()));
        cachedQrIcon = new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
        return cachedQrIcon;
    }

    private void renderReceiptToTextArea(String invoiceCode, BigDecimal rawTotal,
                                         BigDecimal finalTotal, String payMethod) {
        BigDecimal discount = rawTotal.subtract(finalTotal);
        StringBuilder sb = new StringBuilder();
        sb.append("======= COFFEE SHOP =======\n");
        sb.append("Mã HĐ : ").append(invoiceCode).append("\n");
        sb.append("NV    : ").append(currentUser.fullName).append("\n");
        sb.append("---------------------------\n");
        for (CartItem it : cartModel.getData())
            sb.append(it.product.name).append(" x").append(it.quantity)
              .append(" | ").append(it.unitPrice.multiply(BigDecimal.valueOf(it.quantity)).toPlainString()).append("\n");
        sb.append("---------------------------\n");
        sb.append("Tổng gốc : ").append(rawTotal.toPlainString()).append(" VND\n");
        sb.append("Giảm giá : -").append(discount.toPlainString()).append(" VND\n");
        sb.append("THÀNH TIỀN: ").append(finalTotal.toPlainString()).append(" VND\n");
        sb.append("PAY      : ").append(payMethod).append("\n");
        sb.append("===========================\n");
        txtReceipt.setText(sb.toString());
    }

    // ── Mouse events ──────────────────────────────────────────────────────────
    @Override public void mouseClicked(MouseEvent e) {
        if (e.getSource() == lstMenu) { onSelectProduct(lstMenu.getSelectedValue()); }
        if (e.getSource() == tblCart) {
            int row = tblCart.getSelectedRow();
            if (row >= 0) spNewQty.setValue(cartModel.getAt(row).quantity);
        }
    }
    @Override public void mouseEntered(MouseEvent e) {
        Object src = e.getSource();
        if (src instanceof JButton) ((JButton) src).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (src == lstMenu)         lstMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}

    // ══════════════════════════════════════════════════════════════════════════
    // UI Helpers
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel darkCard(LayoutManager lm) {
        JPanel p = new JPanel(lm) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(3, 5, getWidth()-4, getHeight()-4, 14, 14);
                g2.setColor(C_SURFACE);
                g2.fillRoundRect(0, 0, getWidth()-4, getHeight()-4, 14, 14);
                g2.setColor(C_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-5, getHeight()-5, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JPanel makeHeader(String text, Color fg, Color baseColor) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, baseColor, getWidth(), 0, baseColor.darker().darker()));
                g2.fillRoundRect(0, 0, getWidth()-4, getHeight(), 14, 14);
                g2.fillRect(0, getHeight()/2, getWidth()-4, getHeight()/2);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel lb = new JLabel(text);
        lb.setForeground(fg);
        lb.setFont(ui(Font.BOLD, 14));
        p.add(lb, BorderLayout.WEST);
        return p;
    }

    private void styleTabPane(JTabbedPane tp) {
        tp.setFont(ui(Font.BOLD, 12));
        tp.setBackground(C_BG);
        tp.setForeground(C_SUBTEXT);
        tp.setOpaque(false);
        tp.setBorder(null);
    }

    private JLabel fieldLabel(String text) {
        JLabel lb = new JLabel(text);
        lb.setFont(ui(Font.BOLD, 12));
        lb.setForeground(C_SUBTEXT);
        return lb;
    }

    private JTextField darkField(JTextField tf) {
        tf.setFont(ui(Font.PLAIN, 13));
        tf.setBackground(C_SURFACE2);
        tf.setForeground(C_CREAM);
        tf.setCaretColor(C_GOLD);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new DarkRoundBorder(C_BORDER, 8, 1), new EmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setFont(ui(Font.PLAIN, 13));
        cb.setBackground(C_SURFACE2);
        cb.setForeground(C_CREAM);
        cb.setBorder(new DarkRoundBorder(C_BORDER, 8, 1));
    }

    private void styleSlider(JSlider sl) {
        sl.setBackground(C_SURFACE);
        sl.setForeground(C_SUBTEXT);
        sl.setPaintTicks(true);
        sl.setPaintLabels(true);
        sl.setMajorTickSpacing(25);
        sl.setFont(ui(Font.PLAIN, 10));
    }

    private void styleSpinner(JSpinner sp) {
        sp.setFont(ui(Font.PLAIN, 13));
        sp.setBorder(new DarkRoundBorder(C_BORDER, 8, 1));
        sp.setBackground(C_SURFACE2);
        sp.setForeground(C_CREAM);
        sp.setPreferredSize(new Dimension(80, 34));
        JComponent editor = sp.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(C_SURFACE2);
            tf.setForeground(C_CREAM);
            tf.setCaretColor(C_GOLD);
        }
    }

    private void styleRadio(JRadioButton rb) {
        rb.setFont(ui(Font.BOLD, 13));
        rb.setForeground(C_CREAM);
        rb.setBackground(C_SURFACE);
        rb.setOpaque(false);
    }

    private void darkButton(JButton btn, Color bg, Color fg) {
        btn.setForeground(fg);
        btn.setFont(ui(Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        final Color hoverBg = bg.brighter();
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = btn.getModel().isRollover() ? hoverBg : bg;
                g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 40));
                g2.fillRoundRect(-2, -2, c.getWidth()+4, c.getHeight()+4, 14, 14);
                g2.setPaint(new GradientPaint(0, 0, base, 0, c.getHeight(), base.darker()));
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(2, 2, c.getWidth()-4, c.getHeight()/2, 8, 8);
                g2.dispose();
                super.paint(g, c);
            }
        });
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.repaint(); }
            public void mouseExited(MouseEvent e)  { btn.repaint(); }
        });
    }

    private GridBagConstraints gbc(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=x; c.gridy=y; c.fill=GridBagConstraints.HORIZONTAL;
        c.weightx=1; c.insets=new Insets(5,5,5,5);
        return c;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DrinkCardRenderer (Hệ thống load ảnh)
    // ══════════════════════════════════════════════════════════════════════════

    static class DrinkCardRenderer extends JPanel implements ListCellRenderer<Product> {
        private Product product;
        private boolean selected;

        private static final java.util.Map<String, ImageIcon> imageCache =
                new java.util.concurrent.ConcurrentHashMap<>();
        private static final java.util.Set<String> loadingUrls =
                java.util.Collections.synchronizedSet(new java.util.HashSet<>());

        DrinkCardRenderer() { setOpaque(false); }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends Product> list, Product p, int idx,
                boolean isSelected, boolean cellHasFocus) {
            this.product  = p;
            this.selected = isSelected;

            if (p != null && p.image != null && !p.image.isEmpty()
                    && !imageCache.containsKey(p.image)
                    && !loadingUrls.contains(p.image)) {
                loadingUrls.add(p.image);
                new Thread(() -> {
                    try {
                        java.net.URL url = new java.net.URL(p.image);
                        java.net.HttpURLConnection conn =
                                (java.net.HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(4000);
                        conn.setReadTimeout(6000);
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                        java.awt.image.BufferedImage raw =
                                javax.imageio.ImageIO.read(conn.getInputStream());
                        if (raw != null) {
                            int tw = 74, th = 78;
                            double scale = Math.min(tw / (double) raw.getWidth(),
                                                    th / (double) raw.getHeight());
                            int sw = (int)(raw.getWidth()  * scale);
                            int sh = (int)(raw.getHeight() * scale);
                            java.awt.Image scaled = raw.getScaledInstance(
                                    sw, sh, java.awt.Image.SCALE_SMOOTH);
                            imageCache.put(p.image, new ImageIcon(scaled));
                        } else {
                            imageCache.put(p.image, new ImageIcon());
                        }
                    } catch (Exception ex) {
                        imageCache.put(p.image, new ImageIcon());
                    } finally {
                        loadingUrls.remove(p.image);
                        SwingUtilities.invokeLater(list::repaint);
                    }
                }, "img-loader-" + p.code).start();
            }

            return this;
        }

        @Override public Dimension getPreferredSize() { return new Dimension(310, 90); }

        @Override
        protected void paintComponent(Graphics g) {
            if (product == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int W = getWidth()-10, H = getHeight()-6;
            Color accent = DrinkIllustration.accentColor(product);

            g2.setColor(new Color(0, 0, 0, selected ? 60 : 30));
            g2.fillRoundRect(4, 5, W, H, 14, 14);

            GradientPaint bgGrad = selected
                    ? new GradientPaint(0, 0, new Color(55, 38, 18), W, H, new Color(40, 28, 14))
                    : new GradientPaint(0, 0, new Color(32, 26, 18), W, H, new Color(26, 20, 12));
            g2.setPaint(bgGrad);
            g2.fillRoundRect(2, 2, W, H, 14, 14);

            g2.setPaint(new GradientPaint(0, 0, accent, 0, H, accent.darker()));
            g2.fillRoundRect(2, 2, 4, H, 4, 4);

            if (selected) {
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 18));
                g2.fillRoundRect(2, 2, W, H, 14, 14);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(2, 2, W, H, 14, 14);
            }

            int imgX = 10, imgY = 6, imgW = 74, imgH = 78;

            ImageIcon icon = (product.image != null && !product.image.isEmpty())
                    ? imageCache.get(product.image) : null;

            if (icon != null && icon.getIconWidth() > 0) {
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillRoundRect(imgX, imgY, imgW, imgH, 10, 10);

                int iw = icon.getIconWidth(),  ih = icon.getIconHeight();
                int dx = imgX + (imgW - iw) / 2;
                int dy = imgY + (imgH - ih) / 2;

                java.awt.Shape oldClip = g2.getClip();
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(imgX, imgY, imgW, imgH, 10, 10));
                g2.drawImage(icon.getImage(), dx, dy, null);
                g2.setClip(oldClip);

            } else {
                DrinkIllustration.paintFor(g2, product, imgX, imgY, imgW, imgH);

                if (product.image != null && !product.image.isEmpty()
                        && loadingUrls.contains(product.image)) {
                    g2.setColor(new Color(255, 255, 255, 50));
                    g2.setFont(ui(Font.PLAIN, 9));
                    g2.drawString("...", imgX + 28, imgY + imgH - 6);
                }
            }

            int tx = 95;
            g2.setColor(C_CREAM);
            g2.setFont(ui(Font.BOLD, 14));
            g2.drawString(product.name != null ? product.name : "(no name)", tx, 34);

            String code = product.code != null ? product.code : "-";
            g2.setFont(ui(Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();
            int bw = fm.stringWidth(code) + 12;
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
            g2.fillRoundRect(tx - 2, 40, bw, 17, 8, 8);
            g2.setColor(accent);
            g2.drawString(code, tx + 4, 52);

            String price = product.price != null ? product.price.toPlainString() + " ₫" : "-";
            g2.setFont(ui(Font.BOLD, 13));
            g2.setColor(C_GOLD_L);
            g2.drawString(price, tx, 74);

            g2.dispose();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DrinkIllustration (Giữ nguyên)
    // ══════════════════════════════════════════════════════════════════════════

    static class DrinkIllustration {
        static Color accentColor(Product p) {
            String k = key(p);
            if (k.contains("matcha") || k.contains("green"))                       return C_MATCHA;
            if (k.contains("tra") || k.contains("tea"))                            return new Color(190, 145, 70);
            if (k.contains("sua") || k.contains("milk"))                           return new Color(215, 180, 115);
            if (k.contains("sinh to") || k.contains("smoothie") || k.contains("dau")) return C_STRAWB;
            if (k.contains("cam") || k.contains("orange"))                         return new Color(240, 145, 30);
            if (k.contains("chanh") || k.contains("lemon"))                        return new Color(200, 210, 40);
            if (k.contains("nuoc") || k.contains("water"))                         return new Color(100, 180, 230);
            if (k.contains("bac xiu"))                                             return new Color(205, 165, 95);
            return C_ACCENT;
        }
        private static String key(Product p) {
            if (p == null) return "";
            return ((p.name != null ? p.name : "") + " " + (p.code != null ? p.code : "")).toLowerCase();
        }
        static void paintFor(Graphics2D g2, Product p, int x, int y, int w, int h) {
            String k = key(p);
            if      (k.contains("matcha") || k.contains("green"))                      drawMatchaCup(g2, x, y, w, h);
            else if (k.contains("tra") || k.contains("tea"))                           drawTeaCup(g2, x, y, w, h);
            else if (k.contains("sinh to")||k.contains("smoothie")||k.contains("dau")) drawSmoothie(g2, x, y, w, h);
            else if (k.contains("cam") || k.contains("orange"))                        drawOrangeJuice(g2, x, y, w, h);
            else if (k.contains("chanh") || k.contains("lemon"))                       drawLemonade(g2, x, y, w, h);
            else if (k.contains("nuoc") || k.contains("water"))                        drawWaterCup(g2, x, y, w, h);
            else if (k.contains("bac xiu"))                                            drawBacXiuCup(g2, x, y, w, h);
            else if (k.contains("sua") || k.contains("milk"))                          drawMilkCup(g2, x, y, w, h);
            else                                                                        drawEspressoCup(g2, x, y, w, h);
        }

        private static void drawEspressoCup(Graphics2D g, int x, int y, int w, int h) { /* ... giữ nguyên ... */ }
        private static void drawMatchaCup(Graphics2D g, int x, int y, int w, int h)   { /* ... giữ nguyên ... */ }
        private static void drawTeaCup(Graphics2D g, int x, int y, int w, int h)      { /* ... giữ nguyên ... */ }
        private static void drawSmoothie(Graphics2D g, int x, int y, int w, int h)    { /* ... giữ nguyên ... */ }
        private static void drawOrangeJuice(Graphics2D g, int x, int y, int w, int h) { /* ... giữ nguyên ... */ }
        private static void drawLemonade(Graphics2D g, int x, int y, int w, int h)    { /* ... giữ nguyên ... */ }
        private static void drawWaterCup(Graphics2D g, int x, int y, int w, int h)    { /* ... giữ nguyên ... */ }
        private static void drawBacXiuCup(Graphics2D g, int x, int y, int w, int h)   { /* ... giữ nguyên ... */ }
        private static void drawMilkCup(Graphics2D g, int x, int y, int w, int h)     { /* ... giữ nguyên ... */ }
        private static void steam(Graphics2D g, int x, int y)                         { /* ... giữ nguyên ... */ }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Inner utility classes
    // ══════════════════════════════════════════════════════════════════════════

    static class DarkStripedRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            if (!sel) {
                setBackground(row % 2 == 0 ? C_SURFACE : C_SURFACE2);
                setForeground(C_CREAM);
            } else {
                setBackground(new Color(212, 170, 90, 50));
                setForeground(C_GOLD_L);
            }
            setFont(ui(Font.PLAIN, 13));
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return this;
        }
    }

    static class DarkRoundBorder extends AbstractBorder {
        private final Color c; private final int r, t;
        DarkRoundBorder(Color c, int r, int t) { this.c=c; this.r=r; this.t=t; }
        @Override public void paintBorder(Component comp, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c); g2.setStroke(new BasicStroke(t));
            g2.drawRoundRect(x+1, y+1, w-3, h-3, r, r);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(t+2,t+2,t+2,t+2); }
    }
}